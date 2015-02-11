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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.bo.JobBO;
import com.globalsight.dispatcher.bo.JobTask;
import com.globalsight.dispatcher.dao.AccountDAO;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;

/**
 * The controller for translate XLF file.
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
    private AccountDAO accountDAO = DispatcherDAOFactory.getAccountDAO();

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void uploadXLF(HttpServletRequest p_request, HttpServletResponse p_response) throws IOException,
            FileUploadException, JSONException
    {
        String securityCode = p_request.getParameter(JSONPN_SECURITY_CODE);
        Account account = accountDAO.getAccountBySecurityCode(securityCode);
        if(account == null)
        {
            JSONObject jsonObj = new JSONObject();   
            jsonObj.put(JSONPN_STATUS, STATUS_FAIl);
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
                    File srcDir = CommonDAO.getFolder(fileStorage, account.getId() + File.separator + jobIDStr + File.separator + XLF_SOURCE_FOLDER);
                    srcFile = new File(srcDir, fileName);
                    try
                    {
                        item.write(srcFile);
                        logger.info("Uploaded File:" + srcFile.getAbsolutePath());
                    }
                    catch (Exception e)
                    {
                        logger.error("Upload error with file:" + srcFile.getAbsolutePath(), e);
                    }
                }
            }

            // Initial JobBO
            job = new JobBO(jobIDStr, account.getId(), srcFile);
            // Prepare data for MT
            parseXLF(job, srcFile);
            errorMsg = checkJobData(job);
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
            jsonObj.put(JSONPN_STATUS, STATUS_FAIl);
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
        String msg = null;
        String securityCode = p_request.getParameter(JSONPN_SECURITY_CODE);
        Account account = accountDAO.getAccountBySecurityCode(securityCode);
        if(account == null)
        {
            JSONObject jsonObj = new JSONObject();   
            jsonObj.put(JSONPN_STATUS, STATUS_FAIl);
            jsonObj.put(JSONPN_ERROR_MESSAGE, "The security code is incorrect!");
            logger.error("Download fail, due the security code is incorrect -->" + securityCode);
            p_response.getWriter().write(jsonObj.toString());
            return;
        }        
        
        try
        {
            File fileStorage = CommonDAO.getFileStorage();
            File trgFolder = new File(fileStorage, account.getId() + "/" + jobID + "/target");
            File trgFile = trgFolder.listFiles()[0];
            sendFile(trgFile, null, p_response, false);
            logger.info("Download File:" + trgFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            msg = "Download xlf file error.";
        }
        
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSONPN_JOBID, jobID);
        jsonObj.put(JSONPN_ERROR_MESSAGE, msg);
        p_response.getWriter().write(jsonObj.toString());
    }

    private void parseXLF(JobBO p_job, File p_srcFile)
    {
        if (p_srcFile == null || !p_srcFile.exists())
            return;

        String srcLang, trgLang;
        List<String> srcSegments = new ArrayList<String>();

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document read_doc = builder.build(p_srcFile);
            // Get Root Element
            Element root = read_doc.getRootElement();
            Element fileElem = root.getChild("file");
            // Get Source/Target Language
            srcLang = fileElem.getAttributeValue(XLF_SOURCE_LANGUAGE);
            trgLang = fileElem.getAttributeValue(XLF_TARGET_LANGUAGE);
            List<?> list = fileElem.getChild("body").getChildren("trans-unit");
            for (int i = 0; i < list.size(); i++)
            {
                Element e = (Element) list.get(i);
                // Get Source Text
                String source = e.getChildText("source");
                srcSegments.add(source);
            }
            
            p_job.setSourceLanguage(srcLang);
            p_job.setTargetLanguage(trgLang);
            p_job.setSourceSegments(srcSegments);
        }
        catch (Exception e)
        {
            logger.error("Parse XLF file error: ", e);
        }
    }

    private void doMachineTranslation(JobBO p_job)
    {
        if (!p_job.canDoJob())
            return;
        
        p_job.setStatus(STATUS_RUNNING);
        jobMap.put(p_job.getJobID(), p_job);
        
        JobTask task = new JobTask(p_job);
        doMTExecutorService.submit(task);
    }

    /**
     * Send File to Client
     * 
     * @param p_file
     *            file
     * @param p_fileName
     *            attached file name. If NULL, use p_file.getName().
     * @param p_contentType
     *            response content type
     * @param p_response
     *            response
     * @param p_isDelete
     *            whether delete the input file(p_file).
     * @throws IOException
     * @throws ServletException
     */
    protected void sendFile(File p_file, String p_attachFileName, HttpServletResponse p_response, 
            boolean p_isDelete) 
            throws IOException, ServletException
    {
        BufferedInputStream buf = null;
        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            String attachFileName = p_attachFileName;
            if(attachFileName == null || attachFileName.trim().length() == 0)
                attachFileName = p_file.getName();
            
            p_response.setContentType(getMIMEType(p_file));
            p_response.setHeader("Content-Disposition", "attachment; filename=\"" + attachFileName + "\"");
            p_response.setHeader("Expires", "0");
            p_response.setHeader("Cache-Control", "must-revalidate, post-check=0,pre-check=0");
            p_response.setHeader("Pragma", "public");
            p_response.setContentLength((int) p_file.length());
            FileInputStream fis = new FileInputStream(p_file);
            buf = new BufferedInputStream(fis);
            int readBytes = 0;
            while ((readBytes = buf.read()) != -1)
            {
                out.write(readBytes);
            }
        }
        catch (IOException ioe)
        {
            throw new ServletException(ioe.getMessage());
        }
        finally
        {
            if (buf != null)
                buf.close();
            if (out != null)
                out.close();
            if (p_isDelete)
                p_file.delete();
        }
    }
    
    /**
     * Get Internet Media Type.
     * Wiki: http://en.wikipedia.org/wiki/Internet_media_type
     */
    public static String getMIMEType(File file)
    {
        if (file == null)
            return "";

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".xlf") || fileName.endsWith(".xliff"))
        {
            return "application/xml";
        }
        else if (fileName.endsWith(".xml"))
        {
            return "application/xml";
        }
        else if (fileName.endsWith(".zip"))
        {
            return "application/zip";
        }
        else if(fileName.endsWith(".csv"))
        {
            return "application/csv";
        }
        else if (fileName.endsWith(".xls"))
        {
            return "application/msexcel";
        }
        else if (fileName.endsWith(".ppt"))
        {
            return "application/mspowerpoint";
        }
        else if (fileName.endsWith(".doc"))
        {
            return "application/msword";
        }
        else if (fileName.endsWith(".xlsx"))
        {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        else if (fileName.endsWith(".pptx"))
        {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        else if (fileName.endsWith(".docx"))
        {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        else if (fileName.endsWith(".pdf"))
        {
            return "application/pdf";
        }

        return "";
    }

    public static void setJobMap(JobBO p_job)
    {
        jobMap.put(p_job.getJobID(), p_job);
    }
    
    private static String checkJobData(JobBO p_job)
    {
        StringBuffer result = new StringBuffer();
        if (p_job == null)
        {
            result.append("Parse file error.");
            return result.toString();
        }

        if (p_job.getSourceLanguage() == null || p_job.getSourceLanguage().trim().length() == 0)
        {
            result.append("Please set the source language.\n");
        }
        if (p_job.getTargetLanguage() == null || p_job.getTargetLanguage().trim().length() == 0)
        {
            result.append("Please set the target language.\n");
        }
        if (p_job.getSourceSegments() == null || p_job.getSourceSegments().length == 0)
        {
            result.append("Please set the source segments.\n");
        }
        
        return result.toString();
    }
}