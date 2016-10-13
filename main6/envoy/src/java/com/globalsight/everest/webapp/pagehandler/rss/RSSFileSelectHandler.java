package com.globalsight.everest.webapp.pagehandler.rss;

/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 */
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParameterPersistenceManagerLocal;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import com.globalsight.util.zip.ZipIt;

/**
 * FileSystemViewHandler is responsible persisting the customer upload info and
 * also writing the uploaded files to the upload directory under the appropriate
 * source locale.
 */

public class RSSFileSelectHandler extends PageHandler
{
    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    private static final Logger s_logger = Logger
            .getLogger("RSSFileSelectHandler");

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    public RSSFileSelectHandler()
    {
        super();
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        if (!"previous".equals(action) && !"cancel".equals(action)
                && action != null)
        {
            // first store the basic info from the first page
            saveBasicInfo(p_request);
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Invokes this page handler for an applet request object.
     * 
     * @param p_isGet
     *            - Determines whether the request is a get or post.
     * @param thePageDescriptor
     *            the description of the page to be produced
     * @param theRequest
     *            the original request sent from the browser
     * @param theResponse
     *            the original response object
     * @param context
     *            the Servlet context
     * @return A vector of serializable objects to be passed to applet.
     */
    public Vector invokePageHandlerForApplet(boolean p_isDoGet,
            WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context, HttpSession p_session)
            throws ServletException, IOException, EnvoyServletException
    {
        Vector retVal = null;
        if (p_isDoGet)
        {
            retVal = getDisplayData(p_theRequest, p_session);
        }
        else
        {
            retVal = saveData(p_theRequest, p_session);
        }
        return retVal;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    // Get all the info required to be displayed on the graphical workflow UI.
    // The info required for the dialog boxes for each node should also be
    // included.
    private Vector getDisplayData(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        ResourceBundle bundle = getBundle(p_session);
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        Vector objs = new Vector();

        HashMap uiObjects = new HashMap();
        // UI labels/messages
        uiObjects.put("addAllForUpload", bundle.getString("lb_add_all_upload"));
        uiObjects.put("addForUpload", bundle.getString("lb_add_upload"));
        uiObjects.put("cancelBtn", bundle.getString("lb_cancel"));
        uiObjects.put("modified", bundle.getString("lb_modified"));
        uiObjects.put("name", bundle.getString("lb_name"));
        uiObjects.put("previousBtn", bundle.getString("lb_previous"));
        uiObjects.put("remove", bundle.getString("lb_remove"));
        uiObjects.put("removeAll", bundle.getString("lb_remove_all"));
        uiObjects.put("selectedFiles", bundle.getString("lb_selected_files"));
        uiObjects.put("size", bundle.getString("lb_size"));
        uiObjects.put("upload", bundle.getString("lb_upload"));
        uiObjects.put("warning", bundle.getString("msg_confirm_upload_cancel"));

        // images
        uiObjects.put("moveDown", "/images/moveDownArrow.gif");
        uiObjects.put("moveUp", "/images/moveUpArrow.gif");

        // misc. objects
        uiObjects.put("userTimeZone", (TimeZone) p_session
                .getAttribute(WebAppConstants.USER_TIME_ZONE));
        uiObjects.put("userLocale",
                (Locale) p_session.getAttribute(WebAppConstants.UILOCALE));

        objs.add(uiObjects);

        return objs;
    }

    /**
     * Get the upload path (base docs dir\src locale\jobName_timestamp).
     */
    private String getUploadPath(String p_jobName, String p_sourceLocale,
            Date p_uploadDate)
    {
        // format the time with server's default time zone
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");

        StringBuffer sb = new StringBuffer();
        // sb.append(getCXEBaseDir());
        sb.append(AmbFileStoragePathUtils.getCxeDocDir());
        sb.append(File.separator);
        sb.append(p_sourceLocale);
        sb.append(File.separator);
        sb.append(sdf.format(p_uploadDate));
        sb.append("_");
        sb.append(p_jobName);
        return sb.toString();
    }

    /**
     * Get the source locale's display name in the form of language/country.
     */
    private String getDisplaySourceLocale(SessionManager p_sessionMgr)
    {
        Locale srcLocale = LocaleWrapper.getLocale((String) p_sessionMgr
                .getAttribute("srcLocale"));
        String displayName = srcLocale.getDisplayName(Locale.US);

        p_sessionMgr.setAttribute("srcLocale", displayName);
        return displayName;
    }

    /**
     * Get the display name of the selected target locales.
     */
    private String getDisplayTargetLocale(SessionManager p_sessionMgr)
    {
        String value = (String) p_sessionMgr.getAttribute("targLocales");

        String[] targetLocales = value.split(",");

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < targetLocales.length; i++)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }

            Locale l = LocaleWrapper.getLocale(targetLocales[i]);
            sb.append(l.getDisplayName(Locale.US));
        }

        value = sb.toString();

        p_sessionMgr.setAttribute("targLocales", value);
        return value;
    }

    /**
     * Save the files to the docs directory.
     */
    private Vector saveData(HttpServletRequest p_request, HttpSession p_session)
            throws EnvoyServletException, IOException
    {
        Vector outData = null;
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        String jobName = (String) sessionMgr.getAttribute("jobName");
        String srcLocale = (String) sessionMgr.getAttribute("srcLocale");

        TimeZone tz = (TimeZone) p_session
                .getAttribute(WebAppConstants.USER_TIME_ZONE);
        long uploadDateInLong = System.currentTimeMillis();
        Date uploadDate = new Date(uploadDateInLong);
        String uploadPath = getUploadPath(jobName, srcLocale, uploadDate);
        try
        {
            boolean isMultiPart = FileUpload.isMultipartContent(p_request);
            if (isMultiPart)
            {
                DiskFileUpload upload = new DiskFileUpload();
                List items = upload.parseRequest(p_request);

                List files = null;
                Iterator iter = items.iterator();
                while (iter.hasNext())
                {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField())
                    {
                        // not used for now...
                    }
                    else
                    {
                        StringBuffer sb = new StringBuffer();
                        sb.append(uploadPath);
                        sb.append(File.separator);
                        sb.append("GS_");
                        sb.append(System.currentTimeMillis());
                        sb.append(".zip");
                        String fileName = sb.toString();
                        ;
                        File f = new File(fileName);
                        f.getParentFile().mkdirs();
                        item.write(f);
                        files = ZipIt.unpackZipPackage(fileName);

                        boolean deleted = f.delete();
                        sessionMgr.setAttribute("numOfFiles",
                                String.valueOf(files.size()));
                    }
                }
                // now update the job name to include the timestamp
                String newJobName = uploadPath.substring(
                        uploadPath.lastIndexOf(File.separator) + 1,
                        uploadPath.length());
                sessionMgr.setAttribute("jobName", newJobName);

                saveJobNote(sessionMgr, newJobName, uploadDateInLong);

                sendUploadCompletedEmail(files, sessionMgr, uploadDate, tz,
                        (Locale) p_session
                                .getAttribute(WebAppConstants.UILOCALE));
            }

            return outData;
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    private void saveJobNote(SessionManager p_sessionMgr, String p_newJobName,
            long p_uploadDateInLong)
    {
        // save job note into <docs_folder>\<company_name>\<newJobName>.txt
        // read it in
        // com.globalsight.everest.jobhandler.jobcreation.JobAdditionEngine.createNewJob()
        // the content format is <userName>,<Date long type>,<note>
        try
        {
            String jobNote = (String) p_sessionMgr.getAttribute("notes");
            if (jobNote != null && jobNote.trim().length() != 0)
            {
                File jobnotesFile = new File(
                        AmbFileStoragePathUtils.getCxeDocDir(), p_newJobName
                                + ".txt");
                if (jobnotesFile.exists())
                {
                    jobnotesFile.delete();
                }
                if (jobnotesFile.createNewFile())
                {
                    char token = ',';
                    User user = (User) p_sessionMgr
                            .getAttribute(WebAppConstants.USER);
                    // save name for security
                    StringBuffer sb = new StringBuffer(user.getUserName());
                    sb.append(token);
                    sb.append(p_uploadDateInLong);
                    sb.append(token);
                    sb.append(jobNote);
                    FileUtils.write(jobnotesFile, sb.toString(), "utf-8");
                }
            }
        }
        catch (Exception e)
        {
            // do nothing but write log, because this exception
            // is not important
            s_logger.info(
                    "Error when save "
                            + p_newJobName
                            + " job's notes (added when uploading) into GlobalSight File System",
                    e);
        }
    }

    /**
     * Notify the uploader and customer's default PM about the new upload.
     */
    private void sendUploadCompletedEmail(List p_fileNames,
            SessionManager p_sessionMgr, Date p_uploadDate,
            TimeZone p_userTimeZone, Locale p_userLocale)
    {
        try
        {
            // get source locale before it gets formatted as display string
            String srcLocale = (String) p_sessionMgr.getAttribute("srcLocale");
            String projectName = (String) p_sessionMgr
                    .getAttribute(WebAppConstants.PROJECT_NAME);
            Project project = ServerProxy.getProjectHandler().getProjectByName(
                    projectName);
            User pm = UserHandlerHelper.getUser(project.getProjectManagerId());
            String companyIdStr = String.valueOf(project.getCompanyId());

            User user = (User) p_sessionMgr.getAttribute(WebAppConstants.USER);
            String[] messageArguments = new String[8];
            messageArguments[1] = (String) p_sessionMgr.getAttribute("jobName");
            messageArguments[2] = getDisplaySourceLocale(p_sessionMgr);
            // messageArguments[3] = getDisplayTargetLocale(p_sessionMgr);
            messageArguments[4] = (String) p_sessionMgr.getAttribute("notes");

            // Prepare the project label and name since project can be
            // displayed as either "Division" or "Project"
            StringBuffer sb = new StringBuffer();
            sb.append(p_sessionMgr.getAttribute(WebAppConstants.PROJECT_LABEL));
            sb.append(": ");
            sb.append(projectName);
            messageArguments[5] = sb.toString();

            sb = new StringBuffer();
            sb.append(user.getUserName());
            sb.append(" (");
            sb.append(user.getEmail());
            sb.append(")");
            messageArguments[6] = sb.toString();

            sb = new StringBuffer();
            sb.append(File.separator);
            sb.append(srcLocale);
            sb.append(File.separator);
            sb.append(messageArguments[1]);
            sb.append(File.separator);
            messageArguments[7] = sb.toString();

            Timestamp time = new Timestamp(p_userTimeZone);
            time.setLocale(p_userLocale);
            time.setDate(p_uploadDate);
            messageArguments[0] = time.toString();

            // add it to session manager for the upload confirmation page
            p_sessionMgr.setAttribute("uploader", messageArguments[6]);
            p_sessionMgr.setAttribute("path", messageArguments[7]);
            p_sessionMgr.setAttribute("uploadTime", messageArguments[0]);

            writeResultToLogFile(p_fileNames, messageArguments);

            if (!m_systemNotificationEnabled)
            {
                return;
            }
			UserParameterPersistenceManagerLocal uppml = new UserParameterPersistenceManagerLocal();
			UserParameter up = uppml.getUserParameter(user.getUserId(),
					UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
			if (up != null && up.getIntValue() == 1) {
            ServerProxy.getMailer().sendMailFromAdmin(user, messageArguments,
                    MailerConstants.CUSTOMER_UPLOAD_COMPLETED_SUBJECT,
                    MailerConstants.CUSTOMER_UPLOAD_COMPLETED_MESSAGE,
                    companyIdStr);
			}
            // get the default PM's email address (could be a group alias)
            SystemConfiguration sc = SystemConfiguration.getInstance();
            String recipient = sc.getStringParameter(sc.DEFAULT_PM_EMAIL);
            boolean pmEmailNotification = sc
                    .getBooleanParameter(sc.PM_EMAIL_NOTIFICATION);
            if (!pmEmailNotification)
            {
                return;
            }

            if (recipient == null || recipient.length() == 0)
            {
                s_logger.error("There was no GlobalSight project manager email address for customer upload notification.");

                return;
            }
            time.setTimeZone(TimeZone.getDefault());
            time.setLocale(Locale.getDefault());
            messageArguments[0] = time.toString();
            // send an email to the default PM
			ServerProxy.getMailer().sendMailFromAdmin(recipient,
					messageArguments,
					MailerConstants.CUSTOMER_UPLOAD_COMPLETED_SUBJECT,
					MailerConstants.CUSTOMER_UPLOAD_COMPLETED_MESSAGE,
					companyIdStr);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to send the file upload completion emails.",
                    e);
        }
    }

    /**
     * Save the data from the basic info page into the session.
     */
    private void saveBasicInfo(HttpServletRequest p_request)
    {
        SessionManager sessionMgr = (SessionManager) p_request.getSession()
                .getAttribute(SESSION_MANAGER);
        String jobName = p_request.getParameter("jobField");
        if (jobName != null)
        {
            try
            {
                jobName = URLDecoder.decode(jobName, "UTF-8");
            }
            catch (Exception e)
            {
                s_logger.error("Failed to decode jobName: " + jobName, e);
            }
        }
        sessionMgr.setAttribute("jobName", jobName);
        sessionMgr.setAttribute("srcLocale",
                p_request.getParameter("srcLocales"));
        sessionMgr.setAttribute("targLocales",
                p_request.getParameter("targLocaleList"));
        String projectInfo = (String) p_request.getParameter("projects");
        String[] infos = projectInfo.split(",");
        sessionMgr.setAttribute(WebAppConstants.PROJECT_ID, infos[0]);
        sessionMgr.setAttribute(WebAppConstants.PROJECT_NAME, infos[1]);
        sessionMgr.setAttribute("notes", p_request.getParameter("notesField"));
        sessionMgr.setAttribute("isUpload", Boolean.valueOf(true));
        sessionMgr.setAttribute("path",
                File.separator + p_request.getParameter("srcLocales")
                        + File.separator + jobName);
    }

    private void writeResultToLogFile(List p_fileNames,
            String[] p_messageArguments)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append("Upload time: ");
        sb.append(p_messageArguments[0]);
        sb.append("\r\n");
        sb.append("Job name: ");
        sb.append(p_messageArguments[1]);
        sb.append("\r\n");
        sb.append("Source locale: ");
        sb.append(p_messageArguments[2]);
        sb.append("\r\n");
        sb.append("Job Notes: ");
        sb.append(p_messageArguments[4]);
        sb.append("\r\n");
        sb.append(p_messageArguments[5]);
        sb.append("\r\n");
        sb.append("Uploaded by: ");
        sb.append(p_messageArguments[6]);
        sb.append("\r\n");
        sb.append("Uploaded files: ");
        sb.append(p_fileNames);
        sb.append("\r\n");

        s_logger.info(sb.toString());
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////
}
