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
package com.globalsight.everest.webapp.pagehandler.administration.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.DefinedAttributeComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeConstant;
import com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeFileManager;
import com.globalsight.util.Assert;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class SetAttributeHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(SetAttributeHandler.class);
    public static final String ATTRIBUTES = "attributes";
    public static final String JOB_ATTRIBUTES = "jobAttributes";
    private SessionManager sessionMgr;
    private String uuid = null;

    @ActionHandler(action = AttributeConstant.EDIT_LIST, formClass = "")
    public void saveList(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update list value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String attributeName = request.getParameter("attributeName");
            JobAttribute jobAtt = getJobAttributeByName(attributeName);
            String[] selectOptions = request.getParameterValues("selectOption");
            List<String> optionValues = new ArrayList<String>();

            for (String option : selectOptions)
            {
                optionValues.add(option);
            }

            jobAtt.setValue(optionValues, false);

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("label", jobAtt.getListLabel());
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

    private List<Attribute> getAllAttributes()
    {
        Map<String, Attribute> attributeMaps = (Map<String, Attribute>) sessionMgr
                .getAttribute(ATTRIBUTES);
        if (attributeMaps == null)
        {
            initDate();
            attributeMaps = (Map<String, Attribute>) sessionMgr
                    .getAttribute(ATTRIBUTES);
        }

        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.addAll(attributeMaps.values());

        return attributes;
    }

    private Attribute getAttributeByName(String name)
    {
        Map<String, Attribute> attribues = (Map<String, Attribute>) sessionMgr
                .getAttribute(ATTRIBUTES);
        if (attribues == null)
        {
            attribues = new HashMap<String, Attribute>();
            sessionMgr.setAttribute(ATTRIBUTES, attribues);
            FileProfile selectedFP = (FileProfile) sessionMgr
                    .getAttribute(MapFileProfileToFileHandler.FIRST_SELECTED_FP);
            Set<Attribute> atts = AttributeManager
                    .getAttributesByFileProfile(selectedFP);
            for (Attribute att : atts)
            {
                attribues.put(att.getName(), att);
            }
        }

        return attribues.get(name);
    }

    private JobAttribute getJobAttributeByName(String name)
    {
        Map<String, JobAttribute> attribues = (Map<String, JobAttribute>) sessionMgr
                .getAttribute(JOB_ATTRIBUTES);
        if (attribues == null)
        {
            attribues = new HashMap<String, JobAttribute>();
            sessionMgr.setAttribute(JOB_ATTRIBUTES, attribues);
        }

        JobAttribute jobAttribute = attribues.get(name);
        if (jobAttribute == null)
        {
            jobAttribute = new JobAttribute();
            Attribute att = getAttributeByName(name);
            if (att == null)
            {
                logger.error("Can not find attribute with name: " + name);
                return jobAttribute;
            }

            jobAttribute.setAttribute(att.getCloneAttribute());
            attribues.put(att.getName(), jobAttribute);
        }

        return jobAttribute;
    }

    @ActionHandler(action = AttributeConstant.NEXT, formClass = "")
    public void nextPage(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();
        try
        {
            Map<String, JobAttribute> attribues = (Map<String, JobAttribute>) sessionMgr
                    .getAttribute(JOB_ATTRIBUTES);
            for (JobAttribute att : attribues.values())
            {

                if (att.getAttribute().isRequired())
                {
                    boolean isSet = att.isSet();
                    if (Attribute.TYPE_FILE.equals(att.getType()))
                    {
                        String path = JobAttributeFileManager
                                .getStorePath(uuid)
                                + "/" + att.getAttribute().getName();
                        isSet = getFiles(path).size() > 0;
                    }
                    else
                    {
                        isSet = att.isSet();
                    }

                    if (!isSet)
                    {
                        Assert.assertFalse(true, bundle
                                .getString("msg_required_attribute_lost"));
                    }
                }
            }
        }
        catch (ValidateException ve)
        {
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
    }

    private String getPath(String attributeName)
    {
        return attributeName + this.uuid;
    }

    @ActionHandler(action = AttributeConstant.EDIT_INT, formClass = "")
    public void saveInt(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update integer value...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String attributeName = request.getParameter("attributeName");
            JobAttribute jobAtt = getJobAttributeByName(attributeName);

            String intValueS = request.getParameter("intValue");
            jobAtt.setIntegerValue(jobAtt.convertedToInteger(intValueS));

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getIntLabel());
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
            String attributeName = request.getParameter("attributeName");
            JobAttribute jobAtt = getJobAttributeByName(attributeName);

            String text = request.getParameter("textValue");
            jobAtt.setStringValue(jobAtt.convertedToText(text));

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getTextLabel());
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
            String attributeName = request.getParameter("attributeName");
            JobAttribute jobAtt = getJobAttributeByName(attributeName);

            String floatValueS = request.getParameter("floatValue");
            jobAtt.setFloatValue(jobAtt.convertedToFloat(floatValueS));

            Map<String, Object> returnValue = new HashMap();
            returnValue.put("value", jobAtt.getFloatLabel());
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

    private static List<String> getFiles(String root)
    {
        File rootFile = new File(root);
        if (rootFile.exists())
        {
            ArrayList<String> names = new ArrayList<String>();
            List<File> files = FileUtil.getAllFiles(rootFile);
            for (File file : files)
            {
                names.add(file.getName());
            }
            return names;
        }
        else
        {
            return new ArrayList<String>();
        }
    }

    public static String getFileLabel(String root)
    {
        StringBuffer label = new StringBuffer();
        List<String> files = getFiles(root);
        for (String file : files)
        {
            if (label.length() > 0)
            {
                label.append("<br>");
            }
            label.append(EditUtil.encodeHtmlEntities(file));
        }

        return label.toString();
    }

    @ActionHandler(action = AttributeConstant.EDIT_FILE, formClass = "")
    public void saveFile(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update float value...");
        try
        {
            FileUploader uploader = new FileUploader();
            File file = uploader.upload(request);
            String attributeName = uploader.getFieldValue("attributeName");
            String root = JobAttributeFileManager.getStorePath(uuid) + "/"
                    + attributeName;
            
            if (uploader.getName().length() > 0)
            {
                File targetFile = new File(root + "/" + uploader.getName());
                if (!file.renameTo(targetFile))
                {
                    FileUtils.copyFile(file, targetFile);
                }
            }
        }
        catch (Exception e)
        {
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
            String attributeName = request.getParameter("attributeName");
            JobAttribute jobAtt = getJobAttributeByName(attributeName);

            String dateString = request.getParameter("dateValue");
            jobAtt.setDateValue(jobAtt.convertedToDate(dateString));

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

            String[] fileNames = request.getParameterValues("deleteFiles");
            String attributeName = request.getParameter("attributeName");
            JobAttribute jobAtt = getJobAttributeByName(attributeName);
            String root = JobAttributeFileManager.getStorePath(uuid) + "/"
                    + attributeName;
            File rootFile = new File(root);

            if (rootFile.exists())
            {
                List<File> files = FileUtil.getAllFiles(rootFile);

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
                StringBuffer s = new StringBuffer();
                Map<String, Object> returnValue = new HashMap();
                returnValue.put("files", getFiles(root));
                returnValue.put("label", getFileLabel(root));
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

    private void initDate()
    {
        Map<String, Attribute> attribues = new HashMap<String, Attribute>();
        sessionMgr.setAttribute(ATTRIBUTES, attribues);
        FileProfile selectedFP = (FileProfile) sessionMgr
                .getAttribute(MapFileProfileToFileHandler.FIRST_SELECTED_FP);
        Set<Attribute> atts = AttributeManager
                .getAttributesByFileProfile(selectedFP);
        for (Attribute att : atts)
        {
            attribues.put(att.getName(), att);
        }

        Map<String, JobAttribute> jobAttribues = new HashMap<String, JobAttribute>();
        sessionMgr.setAttribute(JOB_ATTRIBUTES, jobAttribues);
        for (Attribute att : atts)
        {
            JobAttribute jobAtt = new JobAttribute();
            jobAtt.setAttribute(att.getCloneAttribute());
            jobAttribues.put(att.getName(), jobAtt);
        }
    }

    @ActionHandler(action = AttributeConstant.GET_FILES, formClass = "")
    public void getFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Get files...");

        ServletOutputStream out = response.getOutputStream();
        try
        {
            String attributeName = request.getParameter("attributeName");
            String root = JobAttributeFileManager.getStorePath(uuid) + "/"
                    + attributeName;

            List<String> files = getFiles(root);
            Map<String, Object> returnValue = new HashMap();
            returnValue.put("label", getFileLabel(root));
            returnValue.put("files", files);
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

    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, getAllAttributes(),
                new DefinedAttributeComparator(uiLocale), 10,
                JobAttributeConstant.JOB_ATTRIBUTE_LIST,
                JobAttributeConstant.JOB_ATTRIBUTES_KEY);
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
        request.setAttribute("hasAttributes", getAllAttributes().size() > 0);
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        sessionMgr = (SessionManager) request.getSession().getAttribute(
                SESSION_MANAGER);
        uuid = (String) sessionMgr.getAttribute("uuid");
        if (uuid == null)
        {
            uuid = JobImpl.createUuid();
            sessionMgr.setAttribute("uuid", uuid);
        }
    }
}
