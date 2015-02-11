/**
 *  Copyright 2014 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.dispatcher.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.bo.JobBO;
import com.globalsight.dispatcher.bo.JobTask;
import com.globalsight.dispatcher.bo.MTPLanguage;
import com.globalsight.dispatcher.dao.AccountDAO;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.dispatcher.dao.MTPLanguagesDAO;
import com.globalsight.dispatcher.dao.MTProfilesDAO;
import com.globalsight.dispatcher.util.FileUtil;

/**
 * The controller for translate XLIFF file.
 * 
 * @author Joey
 * 
 */
@Controller
@RequestMapping("/translateXLF")
public class TranslateXLFController implements AppConstants
{
    private static final Logger logger = Logger.getLogger(TranslateXLFController.class);
    private static final Map<String, JobBO> jobMap = new ConcurrentHashMap<String, JobBO>();
    private static final ExecutorService doMTExecutorService = Executors.newFixedThreadPool(10);
    AccountDAO accountDAO = DispatcherDAOFactory.getAccountDAO();
    MTProfilesDAO mtProfilesDAO = DispatcherDAOFactory.getMTPRofileDAO();
    MTPLanguagesDAO mtpLangDAO = DispatcherDAOFactory.getMTPLanguagesDAO();

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void uploadXLF(HttpServletRequest p_request, HttpServletResponse p_response) throws IOException,
            FileUploadException, JSONException
    {
        String securityCode = p_request.getParameter(JSONPN_SECURITY_CODE);
        Account account = accountDAO.getAccountBySecurityCode(securityCode);
        if(account == null)
        {
            JSONObject jsonObj = new JSONObject();   
            jsonObj.put(JSONPN_STATUS, STATUS_FAILED);
            jsonObj.put(JSONPN_ERROR_MESSAGE, "The security code is incorrect!");
            logger.error("The security code is incorrect -->" + securityCode);
            p_response.getWriter().write(jsonObj.toString());
            return;
        }
        
        File fileStorage = CommonDAO.getFileStorage();
        File tempDir = CommonDAO.getFolder(fileStorage, FOLDER_TEMP);
        String jobIDStr = "-1";
        File srcFile = null;
        JobBO job = null;
        String errorMsg = null;
        if (ServletFileUpload.isMultipartContent(p_request))
        {
            jobIDStr = RandomStringUtils.randomNumeric(10);
            List<FileItem> fileItems = new ServletFileUpload(new DiskFileItemFactory(1024 * 1024, tempDir))
                    .parseRequest(p_request);

            for (FileItem item : fileItems)
            {
                if (item.isFormField())
                {
                }
                else
                {
                    String fileName = item.getName();
                    fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                    File srcDir = CommonDAO.getFolder(fileStorage, account.getAccountName() + File.separator + jobIDStr + File.separator + XLF_SOURCE_FOLDER);
                    srcFile = new File(srcDir, fileName);
                    try
                    {
                        item.write(srcFile);
                        logger.info("Uploaded File:" + srcFile.getAbsolutePath());
                    }
                    catch (Exception e)
                    {
                        logger.error("Upload error with File:" + srcFile.getAbsolutePath(), e);
                    }
                }
            }

            // Initial JobBO
            job = new JobBO(jobIDStr, account.getId(), srcFile);
            // Prepare data for MT
            String parseMsg = parseXLF(job, srcFile);
            if (parseMsg == null)
                errorMsg = checkJobData(job);
            else
                errorMsg = parseMsg;
            if (errorMsg == null || errorMsg.trim().length() == 0)
            {
                // Do MT
                doMachineTranslation(job);
                jobMap.put(job.getJobID(), job);
            }
            else
            {
                // Cancel Job
                job = null;
            }
        }

        JSONObject jsonObj = new JSONObject();        
        if (job != null)
        {
            jsonObj.put(JSONPN_JOBID, job.getJobID());
            jsonObj.put(JSONPN_SOURCE_LANGUAGE, job.getSourceLanguage());
            jsonObj.put(JSONPN_TARGET_LANGUAGE, job.getTargetLanguage());
            jsonObj.put("sourceSegmentSize", job.getSourceSegments().length);
            logger.info("Created Job --> " + jsonObj.toString());
        }
        else
        {
            jsonObj.put(JSONPN_STATUS, STATUS_FAILED);
            jsonObj.put(JSONPN_ERROR_MESSAGE, errorMsg);
            logger.error("Failed to create Job --> " + jsonObj.toString() + ", file:" + srcFile + ", account:" + account.getAccountName());
        }
        p_response.getWriter().write(jsonObj.toString());
    }

    @RequestMapping("/checkStatus")
    public void checkXLFStatus(HttpServletRequest p_request, HttpServletResponse p_response) throws JSONException, IOException
    {
        String jobID = p_request.getParameter(JSONPN_JOBID);
        JobBO job = jobMap.get(jobID);
        if (job != null)
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(JSONPN_JOBID, jobID);
            jsonObj.put(JSONPN_STATUS, job.getStatus());
            p_response.getWriter().write(jsonObj.toString());
        }
        else
        {
            p_response.getWriter().write("Can't find the job with id:" + jobID);
        }
    }

    @RequestMapping("/download")
    public void downloadXLF(HttpServletRequest p_request, HttpServletResponse p_response) throws IOException, JSONException
    {
        String jobID = p_request.getParameter(JSONPN_JOBID);  
        String securityCode = p_request.getParameter(JSONPN_SECURITY_CODE);
        Account account = accountDAO.getAccountBySecurityCode(securityCode);
        if(account == null)
        {
            JSONObject jsonObj = new JSONObject();   
            jsonObj.put(JSONPN_STATUS, STATUS_FAILED);
            jsonObj.put(JSONPN_ERROR_MESSAGE, "The security code is incorrect!");
            logger.error("Download fail, due the security code is incorrect -->" + securityCode);
            p_response.getWriter().write(jsonObj.toString());
            return;
        }        
        
        try
        {
            File fileStorage = CommonDAO.getFileStorage();
            File trgFolder = new File(fileStorage, account.getAccountName() + "/" + jobID + "/target");
            File trgFile = trgFolder.listFiles()[0];
            FileUtil.sendFile(trgFile, null, p_response, false);
            logger.info("Download File:" + trgFile.getAbsolutePath());
            jobMap.remove(jobID);
            return;
        }
        catch (Exception e)
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(JSONPN_JOBID, jobID);
            jsonObj.put(JSONPN_STATUS, STATUS_FAILED);
            jsonObj.put(JSONPN_ERROR_MESSAGE, "Download XLIFF file error.");
            p_response.getWriter().write(jsonObj.toString());
        }
    }
    
    @RequestMapping(value = "/getLanguagesByAccountName")
    public void getLanguage(HttpServletRequest p_request, HttpServletResponse p_response) throws JSONException, IOException
    {
        JSONArray jsonArray = new JSONArray();
        String accountName = p_request.getParameter(JSONPN_ACCOUNT_NAME); 
        Set<MTPLanguage> langs = mtpLangDAO.getMTPLanguageByAccount(accountName);
        for(MTPLanguage lang : langs)
        {
            jsonArray.put(getJSONObjec(lang));
        }
        
        p_response.getWriter().write(jsonArray.toString());
    }
    
    @RequestMapping(value = "/getAccount")
    public void getAccount(HttpServletRequest p_request, HttpServletResponse p_response) throws JSONException,
            IOException
    {
        String securityCode = p_request.getParameter(JSONPN_SECURITY_CODE);
        Account account = accountDAO.getAccountBySecurityCode(securityCode);

        if (account != null)
        {
            p_response.getWriter().write(account.toJSON());
        }
        else
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(JSONPN_STATUS, STATUS_FAILED);
            jsonObj.put(JSONPN_ERROR_MESSAGE, "No Account matched by securityCode:" + securityCode);
            p_response.getWriter().write(jsonObj.toString());
        }
    }

    public JSONObject getJSONObjec(MTPLanguage p_lang)
    {
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("id", p_lang.getId());
            obj.put("name", p_lang.getName());
            obj.put("accountName", p_lang.getAccountName());
            obj.put("sourceLocale", p_lang.getSrcLocale());
            obj.put("targetLocale", p_lang.getTrgLocale());
            obj.put("MTProfileName", p_lang.getMtProfile().getMtProfileName());            
        }
        catch (Exception e)
        {
        }
        
        return obj;
    }
    
    private String parseXLF(JobBO p_job, File p_srcFile)
    {
        if (p_srcFile == null || !p_srcFile.exists())
            return "File not exits.";

        String srcLang, trgLang;
        List<String> srcSegments = new ArrayList<String>();

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document read_doc = builder.build(p_srcFile);
            // Get Root Element
            Element root = read_doc.getRootElement();
            Namespace namespace = root.getNamespace();
            Element fileElem = root.getChild("file", namespace);
            // Get Source/Target Language
            srcLang = fileElem.getAttributeValue(XLF_SOURCE_LANGUAGE);
            trgLang = fileElem.getAttributeValue(XLF_TARGET_LANGUAGE);
            List<?> list = fileElem.getChild("body", namespace).getChildren("trans-unit", namespace);
            for (int i = 0; i < list.size(); i++)
            {
                Element tuElem = (Element) list.get(i);
                Element srcElem = tuElem.getChild("source", namespace);
                // Get Source Segment 
                if (srcElem != null && srcElem.getContentSize() > 0)
                {
                    String source = getInnerXMLString(srcElem);
                    srcSegments.add(source);
                }
            }
            
            p_job.setSourceLanguage(srcLang);
            p_job.setTargetLanguage(trgLang);
            p_job.setSourceSegments(srcSegments);
        }
        catch (Exception e)
        {
            String msg = "Parse XLIFF file error.";
            logger.error(msg, e);
            return msg;
        }
        
        return null;
    }

    private void doMachineTranslation(JobBO p_job)
    {
        if (!p_job.canDoJob())
            return;
        
        JobTask task = new JobTask(p_job);
        doMTExecutorService.submit(task);
    }

    public static void updateJobMap(JobBO p_job)
    {
        jobMap.put(p_job.getJobID(), p_job);
    }
    
    private String checkJobData(JobBO p_job)
    {
        StringBuffer result = new StringBuffer();
        if (p_job == null)
        {
            result.append("Parse file error.");
            return result.toString();
        }

        if (p_job.getSourceLanguage() == null || p_job.getSourceLanguage().trim().length() == 0)
        {
            result.append("The source language is incorrect.\n");
        }
        if (p_job.getTargetLanguage() == null || p_job.getTargetLanguage().trim().length() == 0)
        {
            result.append("The target language is incorrect.\n");
        }
        if (p_job.getSourceSegments() == null || p_job.getSourceSegments().length == 0)
        {
            result.append("The source segments is incorrect.\n");
        }
        
        GlobalSightLocale srcLocale = CommonDAO.getGlobalSightLocaleByShortName(p_job.getSourceLanguage());
        GlobalSightLocale trgLocale = CommonDAO.getGlobalSightLocaleByShortName(p_job.getTargetLanguage());
        MTPLanguage mtpLanguge = DispatcherDAOFactory.getMTPLanguagesDAO().getMTPLanguage(srcLocale, trgLocale, p_job.getAccountId());
        if (mtpLanguge == null)
        {
            result.append("Can not find the matched Language settings.\n");
        }
        
        return result.toString();
    }
    
    /**
     * get the inner XML inside an element as a string. This is done by
     * converting the XML to its string representation, then extracting the
     * subset between beginning and end tags.
     * @param element
     * @return textual body of the element, or null for no inner body
     */
    public String getInnerXMLString(Element element)
    {
        String elementString = new XMLOutputter().outputString(element);
        int start, end;
        start = elementString.indexOf(">") + 1;
        end = elementString.lastIndexOf("</");
        if (end > 0)
        {
            StringBuilder result = new StringBuilder();
            for (String part : elementString.substring(start, end).split("\r|\n"))
            {
                result.append(part.trim());
            }
            return result.toString();
        }
        else
            return "";
    }
}