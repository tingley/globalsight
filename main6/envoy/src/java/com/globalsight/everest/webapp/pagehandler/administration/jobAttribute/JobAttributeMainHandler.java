/**
 *  Copyright 2009 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.administration.jobAttribute;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.AttributeCloneComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSummaryHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.zip.ZipIt;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class JobAttributeMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(JobAttributeMainHandler.class);
    
    static public final int BUFSIZE = 4096;
    static public final String ZIP_FILE_NAME = "AllFiles.zip";

    @ActionHandler(action = AttributeConstant.EDIT_LIST, formClass = "")
    public void saveList(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update list value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String jobId = request.getParameter("jobId");
            String jobAttributeId = request.getParameter("jobAttributeId");
            JobAttribute jobAtt;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId < 1)
            {
                String attributeId = request.getParameter("attributeId");
                Attribute attribute = HibernateUtil.get(Attribute.class, Long
                        .parseLong(attributeId));
                JobImpl job = HibernateUtil.get(JobImpl.class, Long
                        .parseLong(jobId));
                jobAtt = new JobAttribute();
                jobAtt.setJob(job);
                jobAtt.setAttribute(attribute.getCloneAttribute());
            }
            else
            {
                jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
            }

            String[] selectOptions = request.getParameter("selectOption").split(",");
            List<String> optionIds = new ArrayList<String>();

            for (String option : selectOptions)
            {
                optionIds.add(option);
            }

            jobAtt.setValue(optionIds);
            HibernateUtil.saveOrUpdate(jobAtt);

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("label", jobAtt.getListLabel());
            returnValue.put("jobAttributeId", jobAtt.getIdAsLong());
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Updating list value finished.");
    }

    @ActionHandler(action = AttributeConstant.DOWNLOAD_FILES, formClass = "")
    public void downloadFile(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Download files...");

        OutputStream out = response.getOutputStream();
        try
        {
            String jobAttributeId = request.getParameter("jobAttributeId");
            String[] fileNames = request.getParameterValues("selectFiles");
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId > 0)
            {
                JobAttribute jobAtt = HibernateUtil.get(JobAttribute.class,
                        jobAttId);
                List<File> selectFiles = new ArrayList<File>();
                List<File> files = jobAtt.getFiles();
                for (File file : files)
                {
                    for (String deleteName : fileNames)
                    {
                        if (deleteName.equals(file.getName()))
                        {
                            selectFiles.add(file);
                        }
                    }
                }
                
                if (selectFiles.size() > 0)
                {                   
                    boolean isNew = false;
                    
                    File downLoadFile = selectFiles.get(0);
                    String fileName = downLoadFile.getName();
                    
                    if (selectFiles.size() > 1)
                    {
                        downLoadFile = File.createTempFile("GSJobAttributes", ".zip");
                        fileName = ZIP_FILE_NAME;
                        isNew = true;
                        File[] allFiles = new File[selectFiles.size()];
                        for (int i = 0; i < selectFiles.size(); i++)
                        {
                            allFiles[i] = selectFiles.get(i);
                        }
                        
                        ZipIt.addEntriesToZipFile(downLoadFile, allFiles, true, "");
                    }
                    
                    response.setContentType("text/plain");
                    response.setHeader("Content-Disposition", "attachment; filename="
                            + fileName);
                    response.setHeader("Cache-Control", "no-cache");
                    response.setContentLength((int) downLoadFile.length());

                    byte[] buf = new byte[BUFSIZE];
                    int readLen = 0;

                    BufferedInputStream in = new BufferedInputStream(
                            new FileInputStream(downLoadFile));
                    while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
                    {
                        out.write(buf, 0, readLen);
                    }
                    in.close();
                    out.flush();
                    
                    if (isNew)
                    {
                        downLoadFile.delete();
                    }
                }
            }
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Download files finished.");
    }

    @ActionHandler(action = AttributeConstant.EDIT_INT, formClass = "")
    public void saveInt(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update integer value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String jobId = request.getParameter("jobId");
            String jobAttributeId = request.getParameter("jobAttributeId");
            JobAttribute jobAtt;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId < 1)
            {
                String attributeId = request.getParameter("attributeId");
                Attribute attribute = HibernateUtil.get(Attribute.class, Long
                        .parseLong(attributeId));
                JobImpl job = HibernateUtil.get(JobImpl.class, Long
                        .parseLong(jobId));
                jobAtt = new JobAttribute();
                jobAtt.setJob(job);
                jobAtt.setAttribute(attribute.getCloneAttribute());
            }
            else
            {
                jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
            }

            String intValueS = request.getParameter("intValue");
            jobAtt.setIntegerValue(jobAtt.convertedToInteger(intValueS));
            HibernateUtil.saveOrUpdate(jobAtt);

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getIntLabel());
            returnValue.put("jobAttributeId", jobAtt.getIdAsLong());
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Updating integer value finished.");
    }

    @ActionHandler(action = AttributeConstant.EDIT_TEXT, formClass = "")
    public void saveText(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update text value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String jobId = request.getParameter("jobId");
            String jobAttributeId = request.getParameter("jobAttributeId");
            JobAttribute jobAtt;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId < 1)
            {
                String attributeId = request.getParameter("attributeId");
                Attribute attribute = HibernateUtil.get(Attribute.class, Long
                        .parseLong(attributeId));
                JobImpl job = HibernateUtil.get(JobImpl.class, Long
                        .parseLong(jobId));
                jobAtt = new JobAttribute();
                jobAtt.setJob(job);
                jobAtt.setAttribute(attribute.getCloneAttribute());
            }
            else
            {
                jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
            }

            String text = request.getParameter("textValue");

            jobAtt.setStringValue(jobAtt.convertedToText(text));
            HibernateUtil.saveOrUpdate(jobAtt);

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getTextLabel());
            returnValue.put("jobAttributeId", jobAtt.getIdAsLong());
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Updating text value finished.");
    }

    @ActionHandler(action = AttributeConstant.EDIT_FLOAT, formClass = "")
    public void saveFloat(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update float value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String jobId = request.getParameter("jobId");
            String jobAttributeId = request.getParameter("jobAttributeId");
            JobAttribute jobAtt;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId < 1)
            {
                String attributeId = request.getParameter("attributeId");
                Attribute attribute = HibernateUtil.get(Attribute.class, Long
                        .parseLong(attributeId));
                JobImpl job = HibernateUtil.get(JobImpl.class, Long
                        .parseLong(jobId));
                jobAtt = new JobAttribute();
                jobAtt.setJob(job);
                jobAtt.setAttribute(attribute.getCloneAttribute());
            }
            else
            {
                jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
            }

            String floatValueS = request.getParameter("floatValue");
            jobAtt.setFloatValue(jobAtt.convertedToFloat(floatValueS));
            HibernateUtil.saveOrUpdate(jobAtt);

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getFloatLabel());
            returnValue.put("jobAttributeId", jobAtt.getIdAsLong());
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Updating float value finished.");
    }

    @ActionHandler(action = AttributeConstant.EDIT_FILE, formClass = "")
    public void saveFile(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update float value...");

        try
        {
            String jobId = request.getParameter("jobId");

            FileUploader uploader = new FileUploader();
            File file = uploader.upload(request);

            String jobAttributeId = uploader.getFieldValue("jobAttributeId");
            JobAttribute jobAtt;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId < 1)
            {
                String attributeId = uploader.getFieldValue("attributeId");
                Attribute attribute = HibernateUtil.get(Attribute.class, Long
                        .parseLong(attributeId));
                JobImpl job = HibernateUtil.get(JobImpl.class, Long
                        .parseLong(jobId));
                jobAtt = new JobAttribute();
                jobAtt.setJob(job);
                jobAtt.setAttribute(attribute.getCloneAttribute());
                HibernateUtil.save(jobAtt);
                jobAttId = jobAtt.getId();

                // Remove files.
                List<File> files = jobAtt.getFiles();
                for (File deleteFile : files)
                {
                    deleteFile.delete();
                }
            }
            else
            {
                jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
            }

            if (uploader.getName().length() > 0)
            {
                File targetFile = new File(JobAttributeFileManager
                        .getStorePath(jobAttId)
                        + "/" + uploader.getName());
                if (!file.renameTo(targetFile))
                {
                    FileUtils.copyFile(file, targetFile);
                }
            }
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toObjectJson(e.getMessage())
                    + "})";
            logger.error(e.getMessage(), e);
        }
        finally
        {
            pageReturn();
        }

        logger.debug("Updating float value finished.");
    }

    @ActionHandler(action = AttributeConstant.EDIT_DATE, formClass = "")
    public void saveDate(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update date value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String jobId = request.getParameter("jobId");
            String jobAttributeId = request.getParameter("jobAttributeId");
            JobAttribute jobAtt;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId < 1)
            {
                String attributeId = request.getParameter("attributeId");
                Attribute attribute = HibernateUtil.get(Attribute.class, Long
                        .parseLong(attributeId));
                JobImpl job = HibernateUtil.get(JobImpl.class, Long
                        .parseLong(jobId));
                jobAtt = new JobAttribute();
                jobAtt.setJob(job);
                jobAtt.setAttribute(attribute.getCloneAttribute());
            }
            else
            {
                jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
            }

            String dateString = request.getParameter("dateValue");
            jobAtt.setDateValue(jobAtt.convertedToDate(dateString));
            HibernateUtil.saveOrUpdate(jobAtt);

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getDateLabel());
            returnValue.put("jobAttributeId", jobAtt.getIdAsLong());
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toObjectJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            out.flush();
            pageReturn();
        }

        logger.debug("Updating date value finished.");
    }

    @ActionHandler(action = AttributeConstant.DELETE_FILES, formClass = "")
    public void deleteFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Delete files...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            List<String> existFiles = new ArrayList<String>();

            String jobAttributeId = request.getParameter("jobAttributeId");
            String[] fileNames = request.getParameterValues("deleteFiles");
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId > 0)
            {
                JobAttribute jobAtt = HibernateUtil.get(JobAttribute.class,
                        jobAttId);
                List<File> files = jobAtt.getFiles();
                for (File file : files)
                {
                    for (String deleteName : fileNames)
                    {
                        if (deleteName.equals(file.getName()))
                        {
                            file.delete();
                        }
                    }
                }

                existFiles = jobAtt.getDisplayFiles();

                StringBuffer s = new StringBuffer();
                Map<String, Object> returnValue = new HashMap();
                returnValue.put("files", existFiles);
                returnValue.put("label", jobAtt.getFilesLabel());
                out.write((JsonUtil.toObjectJson(returnValue))
                        .getBytes("UTF-8"));
                out.write(s.toString().getBytes("UTF-8"));
            }
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Delete files finished.");
    }

    @ActionHandler(action = AttributeConstant.GET_FILES, formClass = "")
    public void getFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Get files...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            List<String> files = new ArrayList<String>();
            String jobAttributeId = request.getParameter("jobAttributeId");
            JobAttribute jobAtt = null;
            long jobAttId = Long.parseLong(jobAttributeId);
            if (jobAttId > 0)
			{
				jobAtt = HibernateUtil.get(JobAttribute.class, jobAttId);
				files.addAll(jobAtt.getFileValuesAsStrings());
			}

            StringBuffer s = new StringBuffer();
            Map<String, Object> returnValue = new HashMap();
            returnValue.put("files", files);
            returnValue.put("label", jobAtt.getFilesLabel());
            returnValue.put("jobAttributeId", jobAtt.getIdAsLong());
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }

        logger.debug("Get files finished.");
    }

    private List getAttributes(JobImpl job)
    {
        Set<JobAttribute> jobAtts = job.getAttributes();
        return null;
    }
    
    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        JobImpl job = (JobImpl) request.getAttribute("Job");
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, job
                .getVisitbleAttributeAsList(),
                new AttributeCloneComparator(uiLocale), 10,
                JobAttributeConstant.JOB_ATTRIBUTE_LIST,
                JobAttributeConstant.JOB_ATTRIBUTES_KEY);
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws EnvoyServletException, ServletException, IOException
    {
        JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
        Job job = jobSummaryHelper.getJobByRequest(request);
        boolean isOk = jobSummaryHelper.packJobSummaryInfoView(request,
                response, request.getServletContext(), job);
        if (!isOk)
        {
            pageReturn();
        }
    }
}
