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

package com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.xmldtd.XmlDtd;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.comment.CommentImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.mail.MailerConstants;

/**
 * A util class, used to manage xml dtd validation.
 */
public class XmlDtdManager
{
    static private final Logger logger = Logger.getLogger(XmlDtdManager.class);
    private static final String JOB_COMMENT_DTD_FAILED = "The following pages "
            + "failed to pass the XML DTD validation during {0}:{1}";
    public static final String OFF_LINE_IMPORT = "off-line file import";
    public static final String IMPORT = "import";
    public static final String EXPORT = "export";

    /**
     * Adds a job comment automatically, to record some xml files have failed to
     * pass the xml dtd validation.
     * 
     * @param job
     * @param files
     */
    public static void addComment(Job job, List<String> files, String operation)
    {
        if (files == null || files.size() == 0)
        {
            return;
        }

        try
        {
            StringBuilder commentString = new StringBuilder();
            for (String path : files)
            {
                commentString.append("<br>").append(path);
            }

            CommentImpl comment = new CommentImpl();
            comment.setCommentString(MessageFormat.format(
                    JOB_COMMENT_DTD_FAILED, operation, commentString.toString()));

            comment.setCreateDate(new Date());
            comment.setCreatorId("System");
            comment.setObject(job);

            HibernateUtil.save(comment);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Gets all definded xml dtds according to company id.
     * 
     * @return
     */
    public static List<?> getAllXmlDtd()
    {
        String hql = "from XmlDtdImpl x";
        HashMap<String, Long> map = null;

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " where x.companyId = :companyId";
            map = new HashMap<String, Long>();
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }

    public static XmlDtdImpl getXmlDtdByName(String name)
    {
        String hql = "from XmlDtdImpl x where x.name = :name";
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and x.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return (XmlDtdImpl) HibernateUtil.getFirst(hql, map);
    }

    private static Set<User> getWorkflowManagers(SourcePage sourcePage)
    {
        Set<User> users = new HashSet<User>();
        Set<TargetPage> targetPages = sourcePage.getTargetPages();
        if (targetPages != null)
        {
            for (TargetPage page : targetPages)
            {
                users.addAll(getWorkflowManagers(page));
            }
        }

        return users;
    }

    private static Set<User> getWorkflowManagers(TargetPage targetPage)
    {
        List wfManagerIds = targetPage.getWorkflowInstance()
                .getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER);
        int size = wfManagerIds.size();

        Set<User> users = new HashSet<User>();

        // notify all workflow managers (if any)
        for (int i = 0; i < size; i++)
        {
            try
            {
                User user = ServerProxy.getUserManager().getUser(
                        (String) wfManagerIds.get(i));
                users.add(user);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
        return users;
    }

    private static XmlDtd getXmlDtd(SourcePage sourcePage)
    {
        XmlDtd xmlDtd = null;
        if (sourcePage != null)
        {
            Request request = sourcePage.getRequest();
            long fileProfileId = request.getDataSourceId();
            FileProfileImpl fileProfile = HibernateUtil.get(
                    FileProfileImpl.class, fileProfileId);
            if (fileProfile != null)
            {
                xmlDtd = fileProfile.getXmlDtd();
            }
        }

        return xmlDtd;
    }

    private static void sendEmail(SourcePage sourcePage, String operation)
            throws Exception
    {
        Job job = sourcePage.getRequest().getJob();
        String companyIdStr = String.valueOf(job.getCompanyId());
        L10nProfile l10nProfile = job.getL10nProfile();
        Project project = ServerProxy.getProjectHandler().getProjectById(
                l10nProfile.getProjectId());
        GlobalSightLocale gslocale = sourcePage.getGlobalSightLocale();
        SystemConfiguration config = SystemConfiguration.getInstance();
        String capLoginUrl = config
                .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

        String[] messageArguments = new String[7];

        messageArguments[0] = operation;
        messageArguments[1] = job.getJobName();
        messageArguments[2] = project.getName();
        messageArguments[3] = sourcePage.getDataSourceType();
        messageArguments[4] = l10nProfile.getName();
        messageArguments[5] = sourcePage.getExternalPageId();
        messageArguments[6] = capLoginUrl;

        WorkflowTemplateInfo wfti = l10nProfile
                .getWorkflowTemplateInfo(gslocale);

        Set<User> users = new HashSet<User>();
        Set<TargetPage> targetPages = sourcePage.getTargetPages();
        for (TargetPage page : targetPages)
        {
            users.addAll(getWorkflowManagers(page));
        }

        if (wfti.notifyProjectManager())
        {
            String userName = project.getProjectManagerId();
            User user = ServerProxy.getUserManager().getUser(userName);
            users.add(user);
        }

        for (User user : users)
        {
            ServerProxy.getMailer().sendMailFromAdmin(user, messageArguments,
                    MailerConstants.DTD_VALIDATE_FAILED_SUBJECT,
                    "dtdFailedMessage", companyIdStr);
        }
    }

    /**
     * Send email to workflow managers and project managers.
     * 
     * @param targetPage
     * @param operation
     * @throws Exception
     */
    public static void sendEmail(TargetPage targetPage, String operation)
            throws Exception
    {
        Job job = targetPage.getWorkflowInstance().getJob();
        String companyIdStr = String.valueOf(job.getCompanyId());
        L10nProfile l10nProfile = job.getL10nProfile();
        Project project = ServerProxy.getProjectHandler().getProjectById(
                l10nProfile.getProjectId());
        GlobalSightLocale gslocale = targetPage.getGlobalSightLocale();
        SystemConfiguration config = SystemConfiguration.getInstance();
        String capLoginUrl = config
                .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

        String[] messageArguments = new String[7];
        messageArguments[0] = operation;
        messageArguments[1] = job.getJobName();
        messageArguments[2] = project.getName();
        messageArguments[3] = targetPage.getDataSourceType();
        messageArguments[4] = l10nProfile.getName();
        messageArguments[5] = targetPage.getExternalPageId();
        messageArguments[6] = capLoginUrl;

        Set<User> users = new HashSet<User>();
        users.addAll(getWorkflowManagers(targetPage));

        WorkflowTemplateInfo wfti = l10nProfile
                .getWorkflowTemplateInfo(gslocale);
        if (wfti.notifyProjectManager())
        {
            String userName = project.getProjectManagerId();
            User user = ServerProxy.getUserManager().getUser(userName);
            users.add(user);
        }

        for (User user : users)
        {
            ServerProxy.getMailer().sendMailFromAdmin(user, messageArguments,
                    MailerConstants.DTD_VALIDATE_FAILED_SUBJECT,
                    "dtdFailedMessage", companyIdStr);
        }
    }

    /**
     * Validates all xml files included in the job.
     * <P>
     * The action will be done during import.
     * 
     * @param job
     */
    public static void validateJob(Job job)
    {
        if (job == null)
        {
            return;
        }

        Collection<SourcePage> sourcePages = job.getSourcePages();
        List<String> failedFiles = new ArrayList<String>();

        for (SourcePage sourcePage : sourcePages)
        {
            XmlDtd xmlDtd = getXmlDtd(sourcePage);
            if (xmlDtd != null)
            {
                File xml = sourcePage.getFile();
                if (xml != null && xml.getName().endsWith(".xml"))
                {
                    try
                    {
                        validateXmlFile(xmlDtd.getId(), xml);
                    }
                    catch (DtdException e)
                    {
                        if (xmlDtd.isAddComment())
                        {
                            failedFiles.add(sourcePage.getExternalPageId());
                        }

                        if (xmlDtd.isSendEmail())
                        {
                            getWorkflowManagers(sourcePage);
                            try
                            {
                                sendEmail(sourcePage, IMPORT);
                            }
                            catch (Exception e1)
                            {
                                logger.error(
                                        "Failed to send DTD validation failure email",
                                        e1);
                            }
                        }
                    }
                }
            }
        }

        addComment(job, failedFiles, IMPORT);
    }

    /**
     * Validate a target page.
     * <p>
     * The page must be an xml file.
     * 
     * @param targetPageId
     * @param operation
     * @throws IOException
     */
    public static void validateTargetPage(TargetPage targetPage,
            String operation)
    {
        if (targetPage == null)
        {
            return;
        }

        List<TargetPage> pages = new ArrayList<TargetPage>();
        pages.add(targetPage);
        validateTargetPages(pages, operation);
    }

    /**
     * Validate a target page.
     * <p>
     * The page must be an xml file.
     * 
     * @param targetPageId
     * @param operation
     * @throws IOException
     */
    public static void validateTargetPages(List<TargetPage> targetPages,
            String operation)
    {
        if (targetPages == null || targetPages.size() == 0)
        {
            return;
        }

        List<String> files = new ArrayList<String>();
        Job job = null;
        for (TargetPage targetPage : targetPages)
        {
            SourcePage sourcePage = targetPage.getSourcePage();
            Request request = sourcePage.getRequest();
            XmlDtd xmlDtd = getXmlDtd(sourcePage);

            if (xmlDtd != null)
            {
                ExportHelper helper = new ExportHelper();

                try
                {
                    File xml = helper.getTargetXmlPage(targetPage.getId(),
                            CxeMessageType.XML_IMPORTED_EVENT);
                    validateXmlFile(xmlDtd.getId(), xml);
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage(), e);
                }
                catch (DtdException e)
                {
                    if (xmlDtd.isAddComment())
                    {
                        files.add(targetPage.getDisplayPageName());
                        if (job == null)
                        {
                            job = request.getJob();
                        }
                    }

                    if (xmlDtd.isSendEmail())
                    {
                        try
                        {
                            sendEmail(targetPage, operation);
                        }
                        catch (Exception e1)
                        {
                            logger.error(
                                    "Failed to send DTD validation failure email",
                                    e1);
                        }
                    }
                }
            }
        }

        addComment(job, files, OFF_LINE_IMPORT);
    }

    /**
     * Validates xml files with specified dtd file.
     * 
     * @param id
     *            The xml dtd id.
     * @param file
     *            The xml file need to validate.
     * @throws DtdException
     */
    public static void validateXmlFile(long id, File file) throws DtdException
    {
        Assert.assertFileExist(file);
        if (file.getName().endsWith(".xml"))
        {
            logger.debug("File: " + file.getPath());
            File dtdFile = DtdFileManager.getDtdFile(id, file);
            if (dtdFile != null && dtdFile.exists())
            {
                logger.debug("DTD: " + dtdFile.getPath());
                SAXReader reader = new SAXReader();
                DtdEntityResolver resolver = new DtdEntityResolver(dtdFile);
                reader.setEntityResolver(resolver);
                reader.setValidation(true);
                Document document;
                try
                {
                    document = reader.read(file);
                    document.clearContent();
                    logger.debug("Successful");
                }
                catch (Exception e)
                {
                    logger.info("DTD validation failed: " + e.getMessage());
                    throw new DtdException(e);
                }
            }
        }
    }
}
