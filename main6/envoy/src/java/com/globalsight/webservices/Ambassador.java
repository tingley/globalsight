/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.webservices;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.cxe.adapter.documentum.DocumentumOperator;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.comment.CommentFile;
import com.globalsight.everest.comment.CommentManagerWLRemote;
import com.globalsight.everest.comment.CommentUpload;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistoryImpl;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.Money;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.offline.OfflineFileUploadStatus;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.BasicL10nProfileInfo;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatchLingManagerLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobEditionInfo;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobHandlerWLRemote;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.JobPersistenceAccessor;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerLocal;
import com.globalsight.everest.projecthandler.ProjectHandlerWLRemote;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.projecthandler.ProjectTmTuvT;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.projecthandler.importer.ImportOptions;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.taskmanager.TaskPersistenceAccessor;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManagerLocal;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManagerWLRemote;
import com.globalsight.everest.usermgr.LdapHelper;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.usermgr.UserManagerWLRemote;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.tm.corpus.OverridableLeverageOptions;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowException;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.everest.workflow.WorkflowProcessAdapter;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.ArrorInfo;
import com.globalsight.everest.workflowmanager.TaskJbpmUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowAdditionSender;
import com.globalsight.everest.workflowmanager.WorkflowManagerException;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.everest.workflowmanager.WorkflowManagerWLRemote;
import com.globalsight.everest.workflowmanager.WorkflowPersistenceAccessor;
import com.globalsight.importer.IImportManager;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Hitlist;
import com.globalsight.terminology.Hitlist.Hit;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.Entry;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.RegexUtil;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.ServerUtil;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlParser;
import com.globalsight.util.date.DateHelper;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.attribute.AddJobAttributeThread;
import com.globalsight.webservices.attribute.AttributeUtil;
import com.globalsight.webservices.attribute.Attributes;
import com.globalsight.webservices.attribute.JobAttributeVo;
import com.globalsight.webservices.vo.JobFiles;

/**
 * WebService APIs of GlobalSight handles web services related to projects,
 * jobs, workflows, import, export,setup, etc. for GlobalSight
 * 
 * NOTE: The web service that Apache Axis generates will be named
 * AmbassadorService
 */
public class Ambassador extends AbstractWebService
{
    // Method names
    public static final String CANCEL_JOB_BY_ID = "cancelJobById";

    public static final String GET_USER_INFO = "getUserInfo";

    public static final String GET_ACCEPTED_TASKS = "getAcceptedTasksInWorkflow";

    public static final String GET_CURRENT_TASKS = "getCurrentTasksInWorkflow";

    public static final String GET_TASKS = "getTasksInJob";

    public static final String ACCEPT_TASK = "acceptTask";

    public static final String COMPLETE_TASK = "completeTask";

    public static final String REJECT_TASK = "rejectTask";

    public static final String ADD_COMMENT = "addComment";

    public static final String GET_ALL_PROJECTS_BY_USER = "getAllProjectsByUser";

    public static final String CREATE_JOB = "createJob";

    public static final String GET_UNIQUE_JOB_NAME = "getUniqueJobName";

    public static final String UPLOAD_FILE = "uploadFile";

    public static final String GET_STATUS = "getStatus";

    public static final String GET_JOB_AND_WORKFLOW_INFO = "getJobAndWorkflowInfo";

    public static final String GET_JOB_STATUS = "getJobStatus";

    public static final String GET_LOCALIZED_DOCUMENTS = "getLocalizedDocuments";

    public static final String CANCEL_WORKFLOW = "cancelWorkflow";

    public static final String CANCEL_JOB = "cancelJob";

    public static final String EXPORT_WORKFLOW = "exportWorkflow";

    public static final String EXPORT_JOB = "exportJob";

    public static final String GET_USER_UNAVAILABILITY_REPORT = "getUserUnavailabilityReport";

    public static final String PASS_DCTMACCOUNT = "passDCTMAccount";

    public static final String CREATE_DTCMJOB = "createDocumentumJob";

    public static final String GET_FILE_PROFILEINFOEX = "getFileProfileInformationEx";

    public static final String CANCEL_DCTMJOB = "cancelDocumentumJob";

    public static final String GET_DOWNLOADABLE_JOBS = "getDownloadableJobs";

    public static final String GET_VERSION = "getVersion";

    public static final String DOWNLOAD_COMMENT_FILES = "downloadJobOrTaskCommentFiles";

    public static final String GET_JOBS_BY_TIME_RANGE = "getJobsByTimeRange";

    public static final String GET_WORKFLOW_PATH = "getWorkflowPath";

    public static final String DOWNLOAD_XLIFF_OFFLINE_FILE = "downloadXliffOfflineFile";

    public static final String GET_JOB_EXPORT_FILES = "getJobExportFiles";

    public static final String GET_JOB_EXPORT_WORKFLOW_FILES = "getJobExportWorkflowFiles";

    public static String ERROR_JOB_NAME = "You cannot have \\, /, :, ;, *, ?, |, \", &lt;, &gt;, % or &amp; in the Job Name.";

    private static final Logger logger = Logger.getLogger(Ambassador.class);

    private static String capLoginUrl = null;

    // jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/
    private static String webServerDocRoot = null;

    static public final String DEFAULT_TYPE = "text";

    private static Hashtable dctmInfo = new Hashtable();

    // store object for files and file profiles used in sending upload
    // successfully mail
    private static Hashtable dataStoreForFilesInSendingEmail = new Hashtable();

    private static String NOT_IN_DB = "This job is not ready for query: ";

    private final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";

    private final static String ENTRY_XML = "\r\n\t<entry>"
            + "\r\n\t\t<tm id={0}>{1}</tm>"
            + "\r\n\t\t<percentage>{2}%</percentage>"
            + "\r\n\t\t<sid>{3}</sid>" + "\r\n\t\t<source>"
            + "\r\n\t\t\t<locale>{4}</locale>"
            + "\r\n\t\t\t<segment>{5}</segment>" + "\r\n\t\t</source>"
            + "\r\n\t\t<target>" + "\r\n\t\t\t<locale>{6}</locale>"
            + "\r\n\t\t\t<segment>{7}</segment>" + "\r\n\t\t</target>"
            + "\r\n\t</entry>";

    private final static String ENTRY_XML_SAVE = "<entry>"
            + "\r\n\t<sid>{0}</sid>"
            + "\r\n\t<source>\r\n\t\t<locale>{1}</locale>\r\n\t\t"
            + "{2}\r\n\t</source>\r\n\t"
            + "<target>\r\n\t\t<locale>{3}</locale>\r\n\t\t"
            + "{4}\r\n\t</target>\r\n</entry>";

    private final static String NULL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "\r\n<entries>\r\n\t<entry>\r\n\t\t"
            + "<percentage>0%</percentage>\r\n\t</entry>\r\n</entries>";

    /*
     * The version of desktop icon e.g. VERSION = "(3.1,8.2)" -> 3.1 is the
     * minimal version to allow access webservice, 8.2 is the current version of
     * desktop. Abandoned since 8.2.1.
     */
    private static String VERSION = "(3.1,8.2)";

    // new version used to check, since 8.2.1
    // need to be changed according to the release version each time
    private static String VERSION_NEW = "(3.1,8.2.3)";

    /**
     * used by checkIfInstalled() to remember whether the web service is
     * installed
     */
    // Whether the web service is installed
    private static boolean isWebServiceInstalled = false;

    private Hashtable<String, String> importingStatus = null;

    /**
     * Check if the installation key for the WebService is correct
     * 
     * @return boolean Return true if the installation key is correct, otherwise
     *         return false.
     */
    public static boolean isInstalled()
    {
        // String expectedKey = "WSVC-" + "GS".hashCode() + "-"
        // + "wsdl".hashCode();
        isWebServiceInstalled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.WEBSVC_INSTALL_KEY);

        return isWebServiceInstalled;
    }

    static
    {
        try
        {
            isInstalled();
            SystemConfiguration config = SystemConfiguration.getInstance();
            capLoginUrl = config.getStringParameter("cap.login.url");

            webServerDocRoot = config
                    .getStringParameter(SystemConfigParamNames.WEB_SERVER_DOC_ROOT);
            if (!(webServerDocRoot.endsWith("/") || webServerDocRoot
                    .endsWith("\\")))
            {
                webServerDocRoot = webServerDocRoot + "/";
            }
        }
        catch (Exception ne)
        {
            logger.error("Failed to find environment value " + ne);
        }
    }

    /**
     * Constructs a GlobalSight WebService object.
     */
    public Ambassador()
    {
        logger.info("Creating new GlobalSight Web Service object.");
    }

    /**
     * Logs into the WebService. Returns an access token and company name
     * 
     * The format of returning string is 'AccessToken+_+CompanyName'.
     * 
     * @param p_username
     *            Username used to log in
     * 
     * @param p_password
     *            Password used to log in
     * 
     * @return java.lang.String Access token and company name which user works
     *         for
     * 
     * @exception WebServiceException
     */
    public String login(String p_username, String p_password)
            throws WebServiceException
    {
        String accessToken = this.doLogin(p_username, p_password);
        String separator = "+_+";
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(p_username);
            return accessToken + separator + user.getCompanyName();
        }
        catch (Exception e)
        {
            String errorMsg = makeErrorXml("login", e.getMessage());
            throw new WebServiceException(errorMsg);
        }
    }

    /**
     * Says Hello. Trivial web method.
     * 
     * @return String Message to welcome
     * 
     * @exception WebServiceException
     */
    public String helloWorld() throws WebServiceException
    {
        checkIfInstalled();
        return "Hello from the Welocalize GlobalSight Web service.";
    }

    /**
     * Returns an XML description of the current set of file profiles.
     * 
     * @param p_accessToken
     *            String Access Token for invoking method
     * 
     * @return java.lang.String Returns an XML description which contains
     *         current set of file profiles
     * 
     * @throws WebServiceException
     */
    public String getFileProfileInformation(String p_accessToken)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return makeResponseXml("getFileProfileInformation", false,
                    "Access token is invaild").toString();
        }

        checkAccess(p_accessToken, "getFileProfileInformation");
        // checkPermission(p_accessToken, Permission.FILE_PROFILES_SEE_ALL);

        ArrayList fileProfileIds = new ArrayList();
        ArrayList fileProfileDescriptions = new ArrayList();
        ArrayList fileProfileNames = new ArrayList();
        queryDatabaseForFileProfileInformation(fileProfileIds,
                fileProfileDescriptions, fileProfileNames);

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<fileProfileInformation>\r\n");
        for (int i = 0; i < fileProfileIds.size(); i++)
        {
            xml.append("<fileProfile>");
            xml.append("\t<id>").append(fileProfileIds.get(i).toString())
                    .append("</id>\r\n");
            xml.append("\t<name>").append(fileProfileNames.get(i).toString())
                    .append("</name>\r\n");
            xml.append("\t<description>")
                    .append(fileProfileDescriptions.get(i).toString())
                    .append("</description>\r\n");
            xml.append("</fileProfile>");
        }
        xml.append("</fileProfileInformation>\r\n");
        return xml.toString();
    }

    /**
     * Returns an XML description containing project information
     * 
     * @param p_accessToken
     * 
     * @return java.lang.String An XML description format which contains all
     *         projects information
     * 
     * @exception WebServiceException
     */
    public String getAllProjects(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "getAllProjects");
        checkPermission(p_accessToken, Permission.GET_ALL_PROJECTS);

        Collection c = null;
        try
        {
            c = ServerProxy.getProjectHandler().getAllProjects();
        }
        catch (Exception e)
        {
            String message = "Unable to get all projects";
            logger.error(message, e);
            message = makeErrorXml("getAllProjects", message);
            throw new WebServiceException(message);
        }
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<ProjectInformation>\r\n");
        Iterator it = c.iterator();
        while (it.hasNext())
        {
            Project project = (Project) it.next();
            xml.append("<Project>\r\n");
            xml.append("\t<id>").append(project.getId()).append("</id>\r\n");
            xml.append("\t<name>").append(project.getName())
                    .append("</name>\r\n");
            if (project.getDescription() == null
                    || project.getDescription().length() < 1)
            {
                xml.append("\t<description>").append("N/A")
                        .append("</description>\r\n");
            }
            else
            {
                xml.append("\t<description>").append(project.getDescription())
                        .append("</description>\r\n");
            }
            xml.append("\t<projectmanager>")
                    .append(project.getProjectManagerId())
                    .append("</projectmanager>\r\n");
            xml.append("</Project>\r\n");
        }
        xml.append("</ProjectInformation>\r\n");
        return xml.toString();

    }

    /**
     * Returns an XML description containing activity type information
     * 
     * @param p_accessToken
     * 
     * @return java.lang.String An XML Description which contains all current
     *         activity types information
     * 
     * @throws WebServiceException
     */
    public String getAllActivityTypes(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "getAllActivityTypes");
        checkPermission(p_accessToken, Permission.ACTIVITY_TYPES_VIEW);

        Collection c = null;
        try
        {
            c = ServerProxy.getJobHandler().getAllActivities();
        }
        catch (Exception e)
        {
            String message = "Unable to get all activities";
            logger.error(message, e);
            message = makeErrorXml("getAllActivities", message);
            throw new WebServiceException(message);
        }
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<ActivityInformation>\r\n");
        Iterator it = c.iterator();
        while (it.hasNext())
        {
            Activity activity = (Activity) it.next();
            xml.append("<Activity>\r\n");
            xml.append("\t<id>").append(activity.getId()).append("</id>\r\n");
            xml.append("\t<name>").append(activity.getName())
                    .append("</name>\r\n");
            if (activity.getDescription() == null
                    || activity.getDescription().length() < 1)
            {
                xml.append("\t<description>").append("N/A")
                        .append("</description>\r\n");
            }
            else
            {
                xml.append("\t<description>").append(activity.getDescription())
                        .append("</description>\r\n");
            }
            xml.append("</Activity>\r\n");
        }
        xml.append("</ActivityInformation>\r\n");
        return xml.toString();
    }

    /**
     * Returns an XML description containing all user information, including ID,
     * first name, last name and status
     * 
     * @return java.lang.String Returns an XML description which contains all
     *         current users information
     * 
     * @exception WebServiceException
     */
    public String getAllUsers(String p_accessToken) throws WebServiceException
    {
        checkAccess(p_accessToken, "getAllUsers");
        checkPermission(p_accessToken, Permission.USERS_VIEW);

        Vector v = null;
        try
        {
            v = ServerProxy.getUserManager().getUsersForCurrentCompany();
        }
        catch (Exception e)
        {
            String message = "Unable to get all users";
            logger.error(message, e);
            message = makeErrorXml("getAllUsers", message);
            throw new WebServiceException(message);
        }
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<userInformation>\r\n");
        Iterator it = v.iterator();
        while (it.hasNext())
        {
            User user = (User) it.next();
            xml.append("<user>\r\n");
            xml.append("\t<id>").append(user.getUserId()).append("</id>\r\n");
            xml.append("\t<name>").append(user.getUserName())
                    .append("</name>\r\n");
            xml.append("\t<firstName>").append(user.getFirstName())
                    .append("</firstName>\r\n");
            xml.append("\t<lastName>").append(user.getLastName())
                    .append("</lastName>\r\n");
            xml.append("\t<status>")
                    .append(LdapHelper.getStateAsString(user.getState()))
                    .append("</status>\r\n");
            xml.append("</user>\r\n");
        }
        xml.append("</userInformation>\r\n");
        return xml.toString();

    }

    /**
     * Returns an XML description of the user information specified by the user
     * id.
     * 
     * The information will be contained as below: ID, First name, Last name,
     * Title, Email, Status, Default UI locale and all permissions the user has.
     * 
     * @param p_accessToken
     * 
     * @param p_userId
     *            Special User ID
     * 
     * @return java.lang.String An XML description which contains user's detail
     *         information.
     * 
     * @throws WebServiceException
     */
    public String getUserInfo(String p_accessToken, String p_userId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_USER_INFO);
        checkPermission(p_accessToken, Permission.USERS_VIEW);

        // Logged User object
        String loggedUserName = this.getUsernameFromSession(p_accessToken);
        User loggedUserObj = this.getUser(loggedUserName);
        String loggedCompanyName = loggedUserObj.getCompanyName();

        // User to retrieve information
        User user = null;
        Collection userRoles = null;
        try
        {
            user = ServerProxy.getUserManager().getUser(p_userId);
            userRoles = ServerProxy.getUserManager().getUserRoles(user);
        }
        catch (Exception e)
        {
            String message = "Failed to get user object by userId : "
                    + p_userId;
            logger.error(message, e);
            message = makeErrorXml("getUserInfo", message);
            throw new WebServiceException(message);
        }

        // Current logged user and the user to get info must belong to the same
        // company.
        if (!UserUtil.isSuperAdmin(p_userId))
        {
            if (loggedCompanyName != null
                    && !loggedCompanyName.equalsIgnoreCase(user
                            .getCompanyName()))
            {
                String msg = "Current logged user '" + loggedUserName
                        + "' and the user '" + p_userId
                        + "' are NOT in the same company.";
                logger.error(msg);
                msg = makeErrorXml("getUserInfo", msg);
                throw new WebServiceException(msg);
            }
        }

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<userInformation>\r\n");
        //
        xml.append("\t<userId>").append(user.getUserId())
                .append("</userId>\r\n");
        xml.append("\t<userName>").append(user.getUserName())
                .append("</userName>\r\n");
        xml.append("\t<firstName>").append(user.getFirstName())
                .append("</firstName>\r\n");
        xml.append("\t<lastName>").append(user.getLastName())
                .append("</lastName>\r\n");
        if (user.getTitle() != null
                && user.getTitle().equalsIgnoreCase("null") == false
                && user.getTitle().length() > 0)
        {
            xml.append("\t<title>").append(user.getTitle())
                    .append("</title>\r\n");
        }
        xml.append("\t<status>")
                .append(LdapHelper.getStateAsString(user.getState()))
                .append("</status>\r\n");
        xml.append("\t<companyName>").append(user.getCompanyName())
                .append("</companyName>\r\n");
        // Contact Info
        xml.append("\t<contactInfo>\r\n");
        String address = user.getAddress();
        if (address != null && address.trim().length() > 0
                && "null".equalsIgnoreCase(address) == false)
        {
            xml.append("\t\t<address>").append(address)
                    .append("</address>\r\n");
        }
        String homePhone = user.getHomePhoneNumber();
        if (homePhone != null && homePhone.trim().length() > 0
                && "null".equalsIgnoreCase(homePhone) == false)
        {
            xml.append("\t\t<homePhone>").append(homePhone)
                    .append("</homePhone>\r\n");
        }
        String workPhone = user.getOfficePhoneNumber();
        if (workPhone != null && workPhone.trim().length() > 0
                && "null".equalsIgnoreCase(workPhone) == false)
        {
            xml.append("\t\t<workPhone>").append(workPhone)
                    .append("</workPhone>\r\n");
        }
        String cellPhone = user.getCellPhoneNumber();
        if (cellPhone != null && cellPhone.trim().length() > 0
                && "null".equalsIgnoreCase(cellPhone) == false)
        {
            xml.append("\t\t<cellPhone>").append(cellPhone)
                    .append("</cellPhone>\r\n");
        }
        String faxPhone = user.getFaxPhoneNumber();
        if (faxPhone != null && faxPhone.trim().length() > 0
                && "null".equalsIgnoreCase(faxPhone) == false)
        {
            xml.append("\t\t<faxPhone>").append(faxPhone)
                    .append("</faxPhone>\r\n");
        }
        xml.append("\t\t<email>").append(user.getEmail())
                .append("</email>\r\n");
        String ccEmail = user.getCCEmail();
        if (ccEmail != null && ccEmail.trim().length() > 0
                && "null".equalsIgnoreCase(ccEmail) == false)
        {
            xml.append("\t\t<CCEmail>").append(ccEmail)
                    .append("</CCEmail>\r\n");
        }
        String bccEmail = user.getBCCEmail();
        if (bccEmail != null && bccEmail.trim().length() > 0
                && "null".equalsIgnoreCase(bccEmail) == false)
        {
            xml.append("\t\t<BCCEmail>").append(bccEmail)
                    .append("</BCCEmail>\r\n");
        }
        xml.append("\t\t<defaultUILocale>").append(user.getDefaultUILocale())
                .append("</defaultUILocale>\r\n");
        xml.append("\t</contactInfo>\r\n");
        // projects
        List projectList = UserHandlerHelper.getProjectsByUser(p_userId);
        if (projectList != null)
        {
            xml.append("\t<projects>\r\n");
            Iterator it = projectList.iterator();
            while (it.hasNext())
            {
                Project project = (Project) it.next();
                xml.append("\t\t<project>\r\n");
                xml.append("\t\t\t<projectId>").append(project.getId())
                        .append("</projectId>\r\n");
                xml.append("\t\t\t<projectName>").append(project.getName())
                        .append("</projectName>\r\n");
                try
                {
                    long companyId = project.getCompanyId();
                    String companyName = ServerProxy.getJobHandler()
                            .getCompanyById(companyId).getCompanyName();
                    xml.append("\t\t\t<projectCompanyId>").append(companyName)
                            .append("</projectCompanyId>\r\n");
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
                xml.append("\t\t</project>\r\n");
            }
            xml.append("\t</projects>\r\n");
        }
        // roles
        if (userRoles != null && userRoles.size() > 0)
        {
            xml.append("\t<roles>\r\n");
            List lpList = new ArrayList();
            Iterator rolesIter = userRoles.iterator();
            while (rolesIter.hasNext())
            {
                Role role = (Role) rolesIter.next();
                String lp = role.getSourceLocale() + "-"
                        + role.getTargetLocale();
                if (!lpList.contains(lp))
                {
                    lpList.add(lp);
                }
            }
            for (int i = 0; i < lpList.size(); i++)
            {
                xml.append("\t\t<role>").append(lpList.get(i))
                        .append("</role>\r\n");
            }
            xml.append("\t</roles>\r\n");
        }
        // permission groups
        xml.append("\t<permissionGroups>\r\n");
        try
        {
            Collection groups = Permission.getPermissionManager()
                    .getAllPermissionGroupsForUser(p_userId);
            Iterator iter = groups.iterator();
            while (iter.hasNext())
            {
                PermissionGroup pg = (PermissionGroup) iter.next();
                xml.append("\t\t<group>").append(pg.getName())
                        .append("</group>\r\n");
            }
            xml.append("\t</permissionGroups>\r\n");
        }
        catch (Exception pe)
        {
            throw new WebServiceException(pe.getMessage());
        }
        xml.append("</userInformation>\r\n");
        return xml.toString();
    }

    /**
     * Create new user
     * 
     * @param p_accessToken
     *            String Access token. This field cannot be null
     * @param p_userId
     *            String User ID, It's the same as user name. This field cannot
     *            be null
     * @param p_password
     *            String Password. This field cannot be null
     * @param p_firstName
     *            String First name. This field cannot be null
     * @param p_lastName
     *            String Last name. This field cannot be null
     * @param p_email
     *            String Email address. This field cannot be null. If the email
     *            address is not vaild then the user's status will be set up as
     *            inactive.
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to. The
     *            element in the array is the name of permission group.
     * @param p_status
     *            String Status
     * @param p_roles
     *            Roles String information of user. It uses a string with XML
     *            format to mark all roles information of user.
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project.
     * @param p_projectIds
     *            String[] Projects which user is included. If p_isInAllProject
     *            is true, this will not take effect.
     * @return int Return code 0 -- Success 1 -- Invaild access token 2 --
     *         Invaild user id 3 -- Invaild user password 4 -- Invaild first
     *         name 5 -- Invaild last name 6 -- Invaild email address 7 --
     *         Invaild permission groups 8 -- Invaild company name 9 -- Invaild
     *         project information 10 -- Invaild role information -1 -- Unknow
     *         exception
     * @throws WebServiceException
     */
    public int createUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        // Check input arguments
        if (!Assert.assertNotEmpty(p_accessToken))
            return 1;
        if (!Assert.assertNotEmpty(p_userId))
            return 2;
        if (!Assert.assertNotEmpty(p_password))
            return 3;
        if (!Assert.assertNotEmpty(p_firstName))
            return 4;
        if (!Assert.assertNotEmpty(p_lastName))
            return 5;
        if (!Assert.assertNotEmpty(p_email) || !RegexUtil.validEmail(p_email))
            return 6;
        if (p_permissionGrps == null || p_permissionGrps.length == 0)
            return 7;

        checkAccess(p_accessToken, "createUser");
        checkPermission(p_accessToken, Permission.USERS_NEW);

        try
        {
            // Get current user as requesting user
            User currentUser = getUser(getUsernameFromSession(p_accessToken));

            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            PermissionManager permissionManager = Permission
                    .getPermissionManager();

            Company company = ServerProxy.getJobHandler().getCompany(
                    currentUser.getCompanyName());
            if (company == null)
                return 8;
            long companyId = company.getId();

            // Set up basic user information
            User user = userManager.createUser();
            user.setUserName(p_userId);
            user.setFirstName(p_firstName);
            user.setLastName(p_lastName);
            user.setEmail(p_email);
            user.setPassword(p_password);
            user.setCompanyName(currentUser.getCompanyName());
            user.isInAllProjects(p_isInAllProject);

            // Set up project information
            ArrayList projectIds = new ArrayList();
            Project project = null;
            ProjectHandlerWLRemote projectManager = ServerProxy
                    .getProjectHandler();
            if (p_isInAllProject)
            {
                // user is in all projects
                List projects = (List) projectManager.getAllProjects();
                if (projects == null)
                    return 9;
                for (int i = 0; i < projects.size(); i++)
                {
                    project = (Project) projects.get(i);
                    projectIds.add(project.getIdAsLong());
                }
            }
            else
            {
                // user is in some special projects
                long projectId = 0l;
                if (p_projectIds != null)
                {
                    try
                    {
                        for (int i = 0; i < p_projectIds.length; i++)
                        {
                            projectId = Long.parseLong(p_projectIds[i]);
                            project = projectManager.getProjectById(projectId);
                            if (project == null)
                                return 9;
                            projectIds.add(project.getIdAsLong());
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                        return 9;
                    }
                }
            }

            List roles = parseRoles(user, p_roles);
            if (roles == null)
                return 10;

            // Check the argument of permssion groups
            // Get all permission groups in special company
            List permissions = (List) permissionManager
                    .getAllPermissionGroupsByCompanyId(String
                            .valueOf(companyId));
            HashMap<String, PermissionGroup> curPermissions = new HashMap<String, PermissionGroup>();
            PermissionGroup pg = null;
            for (int i = 0; i < permissions.size(); i++)
            {
                pg = (PermissionGroup) permissions.get(i);
                curPermissions.put(pg.getName(), pg);
            }

            // Check the argument of permssion groups
            String permission = "";
            for (int i = 0; i < p_permissionGrps.length; i++)
            {
                permission = p_permissionGrps[i];
                if (!curPermissions.containsKey(permission))
                    return 7;
            }

            // Add user
            userManager.addUser(currentUser, user, projectIds, null, roles);

            // Set up user's permission groups
            ArrayList users = new ArrayList(1);
            users.add(p_userId);
            for (int i = 0; i < p_permissionGrps.length; i++)
            {
                permission = p_permissionGrps[i];
                permissionManager.mapUsersToPermissionGroup(users,
                        curPermissions.get(permission));
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return -1;
        }

        return 0;
    }

    /**
     * Modify user
     * 
     * @param p_accessToken
     *            String Access token. This field cannot be null
     * @param p_userId
     *            String User ID, It's the same as user name. This field cannot
     *            be null
     * @param p_password
     *            String Password. This field cannot be null
     * @param p_firstName
     *            String First name. This field cannot be null
     * @param p_lastName
     *            String Last name. This field cannot be null
     * @param p_email
     *            String Email address. This field cannot be null. If the email
     *            address is not vaild then the user's status will be set up as
     *            inactive.
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to. The
     *            element in the array is the name of permission group.
     * @param p_status
     *            String Status
     * @param p_roles
     *            Roles String information of user. It uses a string with XML
     *            format to mark all roles information of user.
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project.
     * @param p_projectIds
     *            String[] Projects which user is included. If p_isInAllProject
     *            is true, this will not take effect.
     * @return int Return code 0 -- Success 1 -- Invaild access token 2 --
     *         Invaild user id or user is not exist. 3 -- Invaild user password
     *         4 -- Invaild first name 5 -- Invaild last name 6 -- Invaild email
     *         address 7 -- Invaild permission groups 8 -- Invaild company name
     *         9 -- Invaild project information 10 -- Invaild role information
     *         11 -- User does not exist 12 -- Current logged user and the user
     *         to be updated do not belong to the same company. -1 -- Unknow
     *         exception
     * @throws WebServiceException
     */
    public int modifyUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        // Check input arguments
        if (!Assert.assertNotEmpty(p_accessToken))
            return 1;
        if (!Assert.assertNotEmpty(p_userId))
            return 2;
        if (p_password != null && p_password.trim().equals(""))
            return 3;
        if (p_firstName != null && p_firstName.trim().equals(""))
            return 4;
        if (p_lastName != null && p_lastName.trim().equals(""))
            return 5;
        if ((p_email != null && p_email.trim().equals(""))
                || !RegexUtil.validEmail(p_email))
            return 6;
        if (p_permissionGrps == null || p_permissionGrps.length == 0)
            return 7;

        checkAccess(p_accessToken, "modifyUser");
        checkPermission(p_accessToken, Permission.USERS_EDIT);

        try
        {
            // Get current user as requesting user
            User currentUser = getUser(getUsernameFromSession(p_accessToken));

            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            PermissionManager permissionManager = Permission
                    .getPermissionManager();

            Company company = ServerProxy.getJobHandler().getCompany(
                    currentUser.getCompanyName());
            if (company == null)
                return 8;
            long companyId = company.getId();

            // Set up basic user information
            User user = userManager.getUser(p_userId);
            if (user == null)
                return 11;
            if (!user.getCompanyName().equals(currentUser.getCompanyName()))
            {
                return 12;
            }
            if (p_firstName != null)
                user.setFirstName(p_firstName);
            if (p_lastName != null)
                user.setLastName(p_lastName);
            if (p_email != null)
                user.setEmail(p_email);
            if (p_password != null)
                user.setPassword(p_password);
            user.isInAllProjects(p_isInAllProject);

            // Set up project information
            ArrayList projectIds = new ArrayList();
            Project project = null;
            ProjectHandlerWLRemote projectManager = ServerProxy
                    .getProjectHandler();
            if (p_isInAllProject)
            {
                // user is in all projects
                List projects = (List) projectManager.getAllProjects();
                if (projects == null)
                    return 9;
                for (int i = 0; i < projects.size(); i++)
                {
                    project = (Project) projects.get(i);
                    projectIds.add(project.getIdAsLong());
                }
            }
            else
            {
                // user is in some special projects
                long projectId = 0l;
                if (p_projectIds != null)
                {
                    try
                    {
                        for (int i = 0; i < p_projectIds.length; i++)
                        {
                            projectId = Long.parseLong(p_projectIds[i]);
                            project = projectManager.getProjectById(projectId);
                            if (project == null)
                                return 9;
                            projectIds.add(project.getIdAsLong());
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                        return 9;
                    }
                }
            }

            List roles = parseRoles(user, p_roles);
            if (roles == null)
                return 10;

            // Check the argument of permssion groups
            // Get all permission groups in special company
            List permissions = (List) permissionManager
                    .getAllPermissionGroupsByCompanyId(String
                            .valueOf(companyId));
            HashMap<String, PermissionGroup> curPermissions = new HashMap<String, PermissionGroup>();
            PermissionGroup pg = null;
            for (int i = 0; i < permissions.size(); i++)
            {
                pg = (PermissionGroup) permissions.get(i);
                curPermissions.put(pg.getName(), pg);
            }

            // Check the argument of permssion groups
            String permission = "";
            ArrayList updatePermissions = new ArrayList();
            for (int i = 0; i < p_permissionGrps.length; i++)
            {
                permission = p_permissionGrps[i];
                if (!curPermissions.containsKey(permission))
                    return 7;
                else
                    updatePermissions.add(curPermissions.get(permission));
            }

            // Modify user
            userManager.modifyUser(currentUser, user, projectIds, null, roles);

            // Set up user's permission groups
            updatePermissionGroups(p_userId, updatePermissions);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return -1;
        }

        return 0;
    }

    /**
     * Parse roles' information from XML format string The XML format string is
     * like below, <?xml version=\"1.0\"?> <roles> <role>
     * <sourceLocale>en_US</sourceLocale> <targetLocale>de_DE</targetLocale>
     * <activities> <activity> <name>Dtp1</name> </activity> <activity>
     * <name>Dtp2</name> </activity> </activities> </role> </roles>
     * 
     * @param p_user
     *            User
     * @param p_xml
     *            Roles' information
     * @return
     */
    private List parseRoles(User p_user, String p_xml)
    {
        ArrayList<UserRole> roles = new ArrayList<UserRole>();
        if (p_xml == null || p_xml.trim().equals(""))
            return roles;
        try
        {
            XmlParser parser = new XmlParser();
            Document doc = parser.parseXml(p_xml);
            Element root = doc.getRootElement();
            List rolesList = root.elements();
            String sourceLocale, targetLocale, activityId, activityName, activityDisplayName, activityUserType, activityType;
            Activity activity = null;
            UserRole role = null;
            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            JobHandlerWLRemote jobManager = ServerProxy.getJobHandler();
            if (rolesList.size() > 0)
            {
                for (Iterator iter = rolesList.iterator(); iter.hasNext();)
                {
                    Element roleElement = (Element) iter.next();
                    sourceLocale = roleElement.element("sourceLocale")
                            .getText();
                    targetLocale = roleElement.element("targetLocale")
                            .getText();

                    List activitiesList = roleElement.elements("activities");
                    for (Iterator iter1 = activitiesList.iterator(); iter1
                            .hasNext();)
                    {
                        Element activitiesElement = (Element) iter1.next();

                        List activityList = activitiesElement.elements();
                        for (Iterator iter2 = activityList.iterator(); iter2
                                .hasNext();)
                        {
                            Element activityElement = (Element) iter2.next();
                            activityName = activityElement.element("name")
                                    .getText();
                            activity = jobManager
                                    .getActivityByDisplayName(activityName);

                            role = userManager.createUserRole();
                            ((Role) role).setActivity(activity);
                            ((Role) role).setSourceLocale(sourceLocale);
                            ((Role) role).setTargetLocale(targetLocale);
                            role.setUser(p_user.getUserId());
                            roles.add(role);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return null;
        }
        return roles;
    }

    /**
     * Update user's permission groups
     * 
     * @param p_userId
     *            User ID
     * @param p_permissionGrps
     *            Permission groups
     * @throws EnvoyServletException
     */
    private void updatePermissionGroups(String p_userId, List p_permissionGrps)
            throws EnvoyServletException
    {
        ArrayList changed = (ArrayList) p_permissionGrps;
        if (changed == null)
            return;
        ArrayList existing = (ArrayList) PermissionHelper
                .getAllPermissionGroupsForUser(p_userId);
        if (existing == null && changed.size() == 0)
            return;

        ArrayList list = new ArrayList(1);
        list.add(p_userId);
        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            if (existing == null)
            {
                // just adding new perm groups
                for (int i = 0; i < changed.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    manager.mapUsersToPermissionGroup(list, pg);
                }
            }
            else
            {
                // need to determine what to add and what to remove.
                // Loop thru old list and see if perm is in new list. If not,
                // remove it.
                for (int i = 0; i < existing.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) existing.get(i);
                    boolean found = false;
                    for (int j = 0; j < changed.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) changed.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.unMapUsersFromPermissionGroup(list, pg);
                }

                // Loop thru new list and see if perm is in old list. If not,
                // add it.
                for (int i = 0; i < changed.size(); i++)
                {
                    boolean found = false;
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    for (int j = 0; j < existing.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) existing.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.mapUsersToPermissionGroup(list, pg);
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns an XML description containing all locale pairs information
     * 
     * @param p_accessToken
     * 
     * @return java.lang.String An XML description which contains all locale
     *         pairs information
     * 
     * @throws WebServiceException
     */
    public String getAllLocalePairs(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "getAllLocalePairs");
        checkPermission(p_accessToken, Permission.LOCALE_PAIRS_VIEW);

        Vector v = null;
        try
        {
            v = ServerProxy.getLocaleManager().getSourceTargetLocalePairs();
        }
        catch (Exception e)
        {
            String message = "Unable to get all locale pairs";
            logger.error(message, e);
            message = makeErrorXml("getAllLocalePairs", message);
            throw new WebServiceException(message);
        }
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<LocalePairInformation>\r\n");
        Iterator it = v.iterator();
        while (it.hasNext())
        {
            LocalePair lp = (LocalePair) it.next();
            xml.append("<localePair>\r\n");
            xml.append("\t<id>").append(lp.getId()).append("</id>\r\n");
            xml.append("\t<sourceLocale>\r\n");
            xml.append("\t<code>").append(lp.getSource().toString())
                    .append("</code>\r\n");
            xml.append("\t<displayName>")
                    .append(lp.getSource().getDisplayName())
                    .append("</displayName>\r\n");
            xml.append("\t</sourceLocale>\r\n");
            xml.append("\t<targetLocale>\r\n");
            xml.append("\t<code>").append(lp.getTarget().toString())
                    .append("</code>\r\n");
            xml.append("\t<displayName>")
                    .append(lp.getTarget().getDisplayName())
                    .append("</displayName>\r\n");
            xml.append("\t</targetLocale>\r\n");
            xml.append("</localePair>\r\n");
        }
        xml.append("</LocalePairInformation>\r\n");
        return xml.toString();

    }

    /**
     * Returns an XML description containing project information according by
     * current user
     * 
     * This method will return projects information which are in charged by
     * current user.
     * 
     * @param p_accessToken
     * 
     * @return java.lang.String An XML description which contains all projects
     *         information that current user managed.
     * 
     * @throws WebServiceException
     */
    public String getAllProjectsByUser(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_ALL_PROJECTS_BY_USER);
        // checkPermission(p_accessToken, Permission.GET_ALL_PROJECTS);

        List projects = null;
        try
        {
            String username = getUsernameFromSession(p_accessToken);
            User user = ServerProxy.getUserManager().getUserByName(username);
            projects = ServerProxy.getProjectHandler()
                    .getProjectInfosManagedByUser(user,
                            Permission.GROUP_MODULE_GLOBALSIGHT);
        }
        catch (Exception e)
        {
            String message = "Unable to get all projects infos managed by user";
            logger.error(message, e);
            message = makeErrorXml("getAllProjectsByUser", message);
            throw new WebServiceException(message);
        }
        Iterator it = projects.iterator();
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<ProjectInformation>\r\n");
        while (it.hasNext())
        {
            ProjectInfo pi = (ProjectInfo) it.next();
            xml.append("<project>\r\n");
            xml.append("\t<id>").append(pi.getProjectId()).append("</id>\r\n");
            xml.append("\t<name>").append(pi.getName()).append("</name>\r\n");
            if (pi.getDescription() == null || pi.getDescription().length() < 1)
            {
                xml.append("\t<description>").append("N/A")
                        .append("</description>\r\n");
            }
            else
            {
                xml.append("\t<description>").append(pi.getDescription())
                        .append("</description>\r\n");
            }
            xml.append("</project>\r\n");
        }
        xml.append("</ProjectInformation>\r\n");
        return xml.toString();
    }

    /**
     * To create a job
     * 
     * @param accessToken
     * @param jobName
     *            String Job name
     * @param comment
     *            String Job comment
     * @param filePaths
     *            String Path of files which are contained in job, split by "|"
     * @param fileProfileIds
     *            String ID of file profiles, split by "|"
     * @param targetLocales
     *            String Target locales which like to be translated, split by
     *            "|"
     * @throws WebServiceException
     */
    public void createJob(String accessToken, String jobName, String comment,
            String filePaths, String fileProfileIds, String targetLocales)
            throws WebServiceException
    {
        createJob(accessToken, jobName, comment, filePaths, fileProfileIds,
                targetLocales, null);
    }

    /**
     * To create a job
     * 
     * @param accessToken
     * @param jobName
     *            String Job name
     * @param comment
     *            String Job comment
     * @param filePaths
     *            String Path of files which are contained in job, split by "|"
     * @param fileProfileIds
     *            String ID of file profiles, split by "|"
     * @param targetLocales
     *            String Target locales which like to be translated, split by
     *            "|"
     * @param attributeXml
     *            String Attributes used to create job
     * @throws WebServiceException
     */
    public void createJob(String accessToken, String jobName, String comment,
            String filePaths, String fileProfileIds, String targetLocales,
            String attributeXml) throws WebServiceException
    {
        Vector fPaths = new Vector();
        for (String path : filePaths.split("\\|"))
        {
            fPaths.add(path);
        }

        Vector fIds = new Vector();
        for (String fId : fileProfileIds.split("\\|"))
        {
            fIds.add(fId);
        }

        Vector tLocales = new Vector();
        for (String tLocale : targetLocales.split("\\|"))
        {
            tLocales.add(tLocale);
        }

        HashMap args = new HashMap();
        args.put("accessToken", accessToken);
        args.put("jobName", jobName);
        args.put("comment", comment);
        args.put("filePaths", fPaths);
        args.put("fileProfileIds", fIds);
        args.put("targetLocales", tLocales);
        args.put("attributes", attributeXml);

        createJob(args);
    }

    /**
     * Creates a job.
     * 
     * <p>
     * Make sure that all files has been uploaded to the service.
     * 
     * <p>
     * The following informations need included args.
     * <ul>
     * <li>accessToken String</li>
     * <li>jobName String Job name</li>
     * <li>comment String Job comment</li>
     * <li>filePaths Vector(String) Path of files which are contained in job</li>
     * <li>fileProfileIds Vector(String) ID of file profiles</li>
     * <li>targetLocales Vector(String) Target locales which like to be
     * translated</li>
     * <li>priority String Job priority, the default value is 3</li>
     * </ul>
     * 
     * @param args
     * @throws WebServiceException
     */
    public void createJob(HashMap args) throws WebServiceException
    {
        // Checks authority.
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, CREATE_JOB);
        checkPermission(accessToken, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

        // Read parameters.
        String jobName = (String) args.get("jobName");
        if (!validateJobName(jobName))
        {
            throw new WebServiceException(makeErrorXml("createJob",
                    ERROR_JOB_NAME));
        }

        Job job = null;
        try
        {
            job = ServerProxy.getJobHandler().getJobByJobName(jobName);
            if (job == null)
            {
                String userName = getUsernameFromSession(accessToken);
                String userId = UserUtil.getUserIdByName(userName);
                String priority = (String) args.get("priority");
                Vector fileProfileIds = (Vector) args.get("fileProfileIds");
                if (fileProfileIds != null && fileProfileIds.size() > 0)
                {
                    String fpId = (String) fileProfileIds.get(0);
                    long iFpId = Long.parseLong(fpId);
                    FileProfile fp = ServerProxy
                            .getFileProfilePersistenceManager()
                            .readFileProfile(iFpId);
                    long l10nProfileId = fp.getL10nProfileId();

                    job = JobCreationMonitor.initializeJob(jobName, userId,
                            fp.getL10nProfileId(), priority, Job.PROCESSING);
                }
            }

            args.put("jobId", String.valueOf(job.getId()));

            createJobOnInitial(args);
        }
        catch (Exception e)
        {
            throw new WebServiceException(makeErrorXml("createJob",
                    "Cannot create a job because " + e.getMessage()));
        }
    }

    private void changeFileListByXliff(String p_filename,
            String p_targetLocale, FileProfile p_fileProfile,
            Vector p_fileProfileList, Vector p_fileList,
            Vector p_afterTargetLocales)
    {
        Hashtable<String, FileProfile> splitFiles = new Hashtable<String, FileProfile>();
        XliffFileUtil.processMultipleFileTags(splitFiles, p_filename,
                p_fileProfile);
        if (splitFiles != null && splitFiles.size() > 0)
        {
            for (Iterator<String> iterator = splitFiles.keySet().iterator(); iterator
                    .hasNext();)
            {
                String tmp = iterator.next();
                p_fileList.add(new File(AmbFileStoragePathUtils.getCxeDocDir(),
                        tmp));
                p_fileProfileList.add(p_fileProfile);
                p_afterTargetLocales.add(p_targetLocale);
            }
        }
    }

    /**
     * Creates a job on the instance initialized during upload process.
     * <p>
     * From GBS-2137.
     */
    public void createJobOnInitial(HashMap args) throws WebServiceException
    {
        Job job = null;

        // Checks authority.
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, CREATE_JOB);
        checkPermission(accessToken, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

        // Read parameters.
        String jobId = (String) args.get("jobId");
        job = JobCreationMonitor.loadJobFromDB(Long.parseLong(jobId));
        String jobName = job.getJobName();
        String uuId = ((JobImpl) job).getUuid();
        String comment = (String) args.get("comment");
        Vector filePaths = (Vector) args.get("filePaths");
        Vector fileProfileIds = (Vector) args.get("fileProfileIds");
        Vector targetLocales = (Vector) args.get("targetLocales");
        String priority = (String) args.get("priority");
        String attributesXml = (String) args.get("attributes");

        FileProfilePersistenceManager fppm = null;
        String fpId = "";
        long iFpId = 0l;
        String filename = "", realFilename = "", tmpFilename = "";
        String zipDir = "";
        String vTargetLocale = "";
        FileProfile fp = null, referenceFP = null;
        File file = null, tmpFile = null;
        Vector fileProfiles = new Vector();
        Vector files = new Vector();
        Vector afterTargetLocales = new Vector();
        ArrayList<String> zipFiles = null;
        long referenceFPId = 0l;

        try
        {
            fppm = ServerProxy.getFileProfilePersistenceManager();

            for (int i = 0; i < filePaths.size(); i++)
            {
                vTargetLocale = (String) targetLocales.get(i);

                fpId = (String) fileProfileIds.get(i);
                iFpId = Long.parseLong(fpId);
                fp = fppm.readFileProfile(iFpId);
                referenceFPId = fp.getReferenceFP();
                referenceFP = fppm.readFileProfile(referenceFPId);

                filename = (String) filePaths.get(i);
                filename = filename.replace('\\', File.separatorChar);
                String srcLocale = findSrcLocale(fpId);
                filename = getRealPath(jobId, filename, srcLocale);
                realFilename = AmbFileStoragePathUtils.getCxeDocDir()
                        + File.separator + filename;
                file = new File(realFilename);
                if (file.getAbsolutePath().endsWith(".xml"))
                {
                    saveFileAsUTF8(file);
                }
                // indicates this is an "XLZ" format file profile
                if (48 == fp.getKnownFormatTypeId())
                {
                    /**
                     * Process XLZ file If file extension is 'xlz', then do
                     * below steps, 1. Unpack the file to folder named with xlz
                     * file name For Example, ..\testXLZFile.xlz will be
                     * unpacked to ..\testXLZFile\xlzFile01.xlf
                     * ..\testXLZFile\xlzFile02.txb
                     * 
                     * 2. add new file profiles according with reference file
                     * profile with current xlz file to all xliff files unpacked
                     * from xlz
                     * 
                     * 3. add target locales according with reference target
                     * locale with current xlz file to all xliff files unpacked
                     * from xlz
                     * 
                     * NOTE: We just process xliff files for now, ignore any
                     * other types of files.
                     */
                    zipDir = realFilename.substring(0,
                            realFilename.lastIndexOf("."));
                    zipFiles = ZipIt.unpackZipPackage(realFilename, zipDir);
                    String relativePath = filename.substring(0,
                            filename.lastIndexOf("."));
                    String tmp = "";
                    for (String f : zipFiles)
                    {
                        tmpFilename = zipDir + File.separator + f;
                        tmpFile = new File(tmpFilename);
                        if (XliffFileUtil.isXliffFile(f))
                        {
                            tmp = relativePath + File.separator + f;
                            changeFileListByXliff(tmp, vTargetLocale,
                                    referenceFP, fileProfiles, files,
                                    afterTargetLocales);
                        }
                    }
                }
                else if (39 == fp.getKnownFormatTypeId())
                {
                    changeFileListByXliff(filename, vTargetLocale, fp,
                            fileProfiles, files, afterTargetLocales);
                }
                else
                {
                    fileProfiles.add(fp);
                    files.add(file);
                    afterTargetLocales.add(vTargetLocale);
                }
            }
        }
        catch (Exception e)
        {
            if (job != null)
            {
                JobCreationMonitor.updateJobState(job, Job.IMPORTFAILED);
            }
            logger.error("Get file profile failed with exception "
                    + e.getMessage());
            throw new WebServiceException(
                    "Get file profile failed with exception " + e.getMessage());
        }

        // Calls script if has.
        // Vector result = FileSystemUtil.execScript(files, fileProfiles,
        // targetLocales);
        Vector result = FileSystemUtil.execScript(files, fileProfiles,
                afterTargetLocales);
        Vector sFiles = (Vector) result.get(0);
        Vector sProFiles = (Vector) result.get(1);
        Vector stargetLocales = (Vector) result.get(2);
        Vector exitValues = (Vector) result.get(3);

        // cache job attributes
        List<JobAttributeVo> atts = null;
        String companyId = null;

        try
        {
            if (attributesXml != null && attributesXml.length() > 0)
            {
                Attributes attributes = com.globalsight.cxe.util.XmlUtil
                        .string2Object(Attributes.class, attributesXml);
                atts = (List<JobAttributeVo>) attributes.getAttributes();
                companyId = CompanyThreadLocal.getInstance().getValue();

                List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
                for (JobAttributeVo jobAttributeVo : atts)
                {
                    jobatts.add(AttributeUtil
                            .createJobAttribute(jobAttributeVo));
                }

                RuntimeCache.addJobAtttibutesCache(uuId, jobatts);
            }
        }
        catch (Exception e)
        {
            // log this exception to avoid break job creation
            logger.error("Get JobAttribute failed with exception.", e);
        }

        // Sends events to cxe.
        int pageCount = sFiles.size();
        for (int i = 0; i < pageCount; i++)
        {
            File realFile = (File) sFiles.get(i);
            FileProfile realProfile = (FileProfile) sProFiles.get(i);
            String targetLocale = (String) stargetLocales.get(i);
            String path = realFile.getPath();
            String relativeName = path.substring(AmbFileStoragePathUtils
                    .getCxeDocDir().getPath().length() + 1);

            try
            {
                publishEventToCxe(jobId, jobName, i + 1, pageCount, 1, 1,
                        relativeName, Long.toString(realProfile.getId()),
                        targetLocale, (Integer) exitValues.get(i), priority);
            }
            catch (Exception e)
            {
                logger.error("Create job(" + jobName
                        + ") failed with exception " + e.getMessage());
                throw new WebServiceException("Create job(" + jobName
                        + ") failed with exception " + e.getMessage());
            }
        }

        // set job attribute
        if (atts != null && atts.size() > 0)
        {
            AddJobAttributeThread thread = new AddJobAttributeThread(uuId,
                    companyId);
            thread.setJobAttributeVos(atts);
            thread.createJobAttributes();
        }

        // Send email at the end.
        sendUploadCompletedEmail(filePaths, fileProfileIds, accessToken,
                jobName, comment, new Date());
    }

    /**
     * Creates a GS Edition job.
     * 
     * <p>
     * Make sure that all files has been uploaded to the service.
     * 
     * <p>
     * The following informations need included args.
     * <ul>
     * <li>accessToken String</li>
     * <li>jobName String Job name</li>
     * <li>filePaths Vector(String) Path of files which are contained in job</li>
     * <li>fileProfileIds Vector(String) IDs of file profile</li>
     * <li>targetLocales Vector(String) Target locales which like to be
     * translated</li>
     * 
     * <li>taskId on original GS server(String)</li>
     * <li>job comment Vector(String)</li>,original activity comments.
     * <li>segment comment Vector(String)</li>
     * 
     * <li>original GS server WSDL url(String)</li>
     * <li>username String</li>
     * <li>password String</li>
     * 
     * </ul>
     * 
     * @param args
     * @return
     * @throws WebServiceException
     */
    public void createEditionJob(HashMap args) throws WebServiceException
    {
        // Checks authority.
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, CREATE_JOB);

        // Read parameters.
        String jobName = (String) args.get("jobName");
        if (!validateJobName(jobName))
        {
            throw new WebServiceException(makeErrorXml("createEditionJob",
                    ERROR_JOB_NAME));
        }

        // String comment = null;//no need here
        Vector filePaths = (Vector) args.get("filePaths");
        Vector fileProfileIds = (Vector) args.get("fileProfileIds");
        Vector targetLocales = (Vector) args.get("targetLocales");
        String priority = "3";// default 3

        String originalTaskId = (String) args.get("taskId");
        String originalEndpoint = (String) args.get("wsdlUrl");
        String originalUserName = (String) args.get("userName");
        String originalPassword = (String) args.get("password");
        Vector jobComments = (Vector) args.get("jobComments");
        HashMap segComments = (HashMap) args.get("segComments");

        // Gets fileProfiles according to id list.
        Vector fileProfiles = new Vector();
        try
        {
            FileProfilePersistenceManager fppm = ServerProxy
                    .getFileProfilePersistenceManager();
            Iterator iFileProfileIds = fileProfileIds.iterator();
            while (iFileProfileIds.hasNext())
            {
                String id = (String) iFileProfileIds.next();
                FileProfile fileProfile = fppm.readFileProfile(Long
                        .parseLong(id));
                fileProfiles.add(fileProfile);
            }
        }
        catch (Exception e1)
        {
            logger.error("Get file profile failed with exception "
                    + e1.getMessage());
            throw new WebServiceException(
                    "Get file profile failed with exception " + e1.getMessage());
        }

        // Gets all files included in the job.
        Vector files = new Vector();
        Iterator iFilePaths = filePaths.iterator();
        Iterator iFileProfileIds = fileProfileIds.iterator();
        while (iFilePaths.hasNext())
        {
            String filePath = (String) iFilePaths.next();
            // change the '\' in the file path to get used to the Linux env.
            filePath = filePath.replace('\\', File.separatorChar);
            String fileProfileId = (String) iFileProfileIds.next();
            String srcLocale = findSrcLocale(fileProfileId);
            filePath = getRealPath(jobName, filePath, srcLocale);
            File file = new File(AmbFileStoragePathUtils.getCxeDocDir(),
                    filePath);
            if (file.getAbsolutePath().endsWith(".xml"))
            {
                saveFileAsUTF8(file);
            }

            files.add(file);
        }

        // Calls script if has.
        Vector result = FileSystemUtil.execScript(files, fileProfiles,
                targetLocales);
        Vector sFiles = (Vector) result.get(0);
        Vector sProFiles = (Vector) result.get(1);
        Vector stargetLocales = (Vector) result.get(2);
        Vector exitValues = (Vector) result.get(3);

        // Sends events to cxe.
        int pageCount = sFiles.size();
        for (int i = 0; i < pageCount; i++)
        {
            File realFile = (File) sFiles.get(i);
            FileProfile realProfile = (FileProfile) sProFiles.get(i);
            String targetLocale = (String) stargetLocales.get(i);
            String path = realFile.getPath();
            String relativeName = path.substring(AmbFileStoragePathUtils
                    .getCxeDocDir().getPath().length() + 1);
            String userName = getUsernameFromSession(accessToken);
            String userId = UserUtil.getUserIdByName(userName);

            try
            {
                publishEventToCxe(jobName, jobName, i + 1, pageCount, 1, 1,
                        relativeName, Long.toString(realProfile.getId()),
                        userId, targetLocale, (Integer) exitValues.get(i),
                        priority, originalTaskId, originalEndpoint,
                        originalUserName, originalPassword, jobComments,
                        segComments);
            }
            catch (Exception e)
            {
                logger.error("Create job(" + jobName
                        + ") failed with exception " + e.getMessage());
                throw new WebServiceException("Create job(" + jobName
                        + ") failed with exception " + e.getMessage());
            }
        }

    }

    /**
     * Saves specified file with UTF8 format
     * 
     * @param file
     *            The file which need to be saved with UTF8 format
     */
    private void saveFileAsUTF8(File file)
    {
        String originalEncode = "";
        try
        {
            originalEncode = ImportUtil.guessEncoding(file);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
        if (originalEncode == null && file.isFile())
        {
            logger.warn("Can not get the encoding of file: " + file
                    + ", please check whether the encoding of file: " + file
                    + " is unicode.");
        }
        else
        {
            ImportUtil.saveFileAsUTF8(file.getAbsolutePath(), originalEncode);
        }
    }

    /**
     * Gets a unique job name.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * 
     * @return job name used in database.
     */
    public String getUniqueJobName(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_UNIQUE_JOB_NAME);
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("accessToken", p_accessToken);
        args.put("jobName", p_jobName);

        return getUniqueJobName(args);
    }

    /**
     * validate job name with "[\\w+-]{1,}" pattern (character, '+', '-')
     * 
     * @param p_jobname
     * @return
     */
    private boolean validateJobName(String p_jobname)
    {
        if (StringUtil.isEmpty(p_jobname) || p_jobname.trim().length() > 120)
            return false;

        return p_jobname.matches("[^\\\\/:;*?|\"<>&%]*");
    }

    /**
     * Gets a unique job name.
     * 
     * @param args
     *            accessToken String jobName String
     * 
     * @return job name used in database.
     */
    public String getUniqueJobName(HashMap args) throws WebServiceException
    {
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, GET_UNIQUE_JOB_NAME);

        String jobName = (String) args.get("jobName");
        if (!validateJobName(jobName))
        {
            throw new WebServiceException(makeErrorXml("getUniqueJobName",
                    ERROR_JOB_NAME));
        }

        String randomStr = String.valueOf((new Random()).nextInt(999999999));
        while (randomStr.length() < 9)
        {
            randomStr = "0" + randomStr;
        }

        jobName = jobName + "_" + randomStr;

        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;

        try
        {
            String sql = "SELECT ID FROM JOB WHERE NAME=?";
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setString(1, jobName);
            results = query.executeQuery();
            if (results.next())
            {
                return getUniqueJobName(args);
            }
            else
                return jobName;
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database to get job.";
            logger.error(message, cpe);
            message = makeErrorXml("getUniqueJobName", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for job.";
            logger.error(message, sqle);
            message = makeErrorXml("getUniqueJobName", message);
            throw new WebServiceException(message);
        }
        catch (WebServiceException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            String message = "Unable to get job information from System4.";
            logger.error(message, e);
            message = makeErrorXml("getUniqueJobName", message);
            throw new WebServiceException(message);
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }
    }

    /**
     * Uploads a file to service.
     * 
     * <p>
     * Following informations must be included in <code>args</code>.
     * <ul>
     * <li>accessToken --- String</li>
     * <li>bytes --------- byte[]</li>
     * <li>filePath ------ String</li>
     * <li>fileProfileId - String</li>
     * <li>jobName ------- String</li>
     * </ul>
     * 
     * @param args
     * @throws WebServiceException
     */
    public void uploadFile(HashMap args) throws WebServiceException
    {
        try
        {
            uploadFileForInitial(args);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(makeErrorXml("uploadFile", e.getMessage()));
        }
    }

    /**
     * Uploads a file to service.
     * <p>
     * An initial job will be created in database. From GBS-2137.
     */
    public String uploadFileForInitial(HashMap args) throws WebServiceException
    {
        Job job = null;
        String jobId = null;

        // Checks authority.
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, UPLOAD_FILE);

        checkPermission(accessToken, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

        try
        {
            String jobName = (String) args.get("jobName");
            if (!validateJobName(jobName))
            {
                throw new WebServiceException(makeErrorXml(
                        "uploadFileForInitial", ERROR_JOB_NAME));
            }
            
            String userName = getUsernameFromSession(accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            String filePath = (String) args.get("filePath");
            String fileProfileId = (String) args.get("fileProfileId");
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(Long.parseLong(fileProfileId));
            String priority = (String) args.get("priority");
            if (priority == null)
            {
                long l10nProfileId = fp.getL10nProfileId();
                BasicL10nProfile blp = HibernateUtil.get(
                        BasicL10nProfile.class, l10nProfileId);
                priority = String.valueOf(blp.getPriority());
            }
            
            jobId = (String) args.get("jobId");
            // validate if there is job with current job name
            if (StringUtil.isEmpty(jobId))
            {
                job = ServerProxy.getJobHandler().getJobByJobName(jobName);
                if (job != null)
                    jobId = String.valueOf(job.getId());
                else
                {
                    // for GBS-2137, initialize the job with "UPLOADING" state
                    job = JobCreationMonitor.initializeJob(jobName, userId,
                            fp.getL10nProfileId(), priority, Job.UPLOADING);
                    jobId = String.valueOf(job.getId());
                }
            }
            if (!isInSameCompany(userName, fp.getCompanyId())
                    && !UserUtil.isSuperPM(userId))
            {
                String message = makeErrorXml(
                        "uploadFile",
                        "Current user cannot upload file with the file profile which is in other company.");
                throw new WebServiceException(message);
            }

            byte[] bytes = (byte[]) args.get("bytes");

            // change the '\' in the file path to get used to the Linux env.
            filePath = filePath.replace('\\', File.separatorChar);
            // Save file.
            String srcLocale = findSrcLocale(fileProfileId);
            String path = getRealPath(jobId, filePath, srcLocale);
            writeFile(path, bytes, fp.getCompanyId());
        }
        catch (Exception e)
        {
            if (jobId != null)
            {
                JobCreationMonitor.updateJobState(Long.parseLong(jobId),
                        Job.IMPORTFAILED);
            }
            logger.error(e.getMessage());
            throw new WebServiceException(e.getMessage());
        }
        return jobId;
    }

    /**
     * Uploads a file to service
     * 
     * @param accessToken
     * @param jobName
     *            String Job name
     * @param filePath
     *            String Absolute Path of file
     * @param fileProfileId
     *            String ID of selected file profile
     * @param content
     *            String Job content
     * @throws WebServiceException
     */
    public void uploadFile(String accessToken, String jobName, String filePath,
            String fileProfileId, String content) throws WebServiceException
    {
        
        try
        {
            Assert.assertNotEmpty(accessToken, "Access token");
            Assert.assertNotEmpty(jobName, "Job name");
            Assert.assertNotEmpty(filePath, "File path");
            Assert.assertNotEmpty(fileProfileId, "File profile Id");
            Assert.assertNotNull(content, "Content");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(makeErrorXml("uploadFile",
                    e.getMessage()));
        }
        
        byte[] bytes;
        try
        {
            bytes = content.getBytes("utf-8");
            
            HashMap args = new HashMap();
            args.put("accessToken", accessToken);
            args.put("jobName", jobName);
            args.put("filePath", filePath);
            args.put("fileProfileId", fileProfileId);
            args.put("bytes", bytes);
            
            uploadFileForInitial(args);
        }
        catch (Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Uploads a file to service
     * 
     * @param accessToken
     * @param jobName
     *            String Job name
     * @param filePath
     *            String Absolute Path of file
     * @param fileProfileId
     *            String ID of selected file profile
     * @param content
     *            byte[] Job content
     * @throws WebServiceException
     */
    public void uploadFile(String accessToken, String jobName, String filePath,
            String fileProfileId, byte[] content) throws WebServiceException
    {
        HashMap args = new HashMap();
        args.put("accessToken", accessToken);
        args.put("jobName", jobName);
        args.put("filePath", filePath);
        args.put("fileProfileId", fileProfileId);
        args.put("bytes", content);
        
        uploadFileForInitial(args);
    }

    /**
     * Returns the status of the given job in XML. Also includes the cost
     * information
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @return String An XML description which contains job status and cost
     *         information
     * @throws WebServiceException
     */
    public String getStatus(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_STATUS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        String jobName = p_jobName;
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            Job job = queryJob(jobName, p_accessToken);
            String status = job.getState();
            float estimatedCost = (float) 0.0;
            float finalCost = (float) 0.0;
            boolean isOverrideCost = false;
            Currency pivotCurrency = null;
            boolean costingEnabled = false;
            String pivotCurrencyName = "USD";
            try
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();
                if (sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED) == true)
                {
                    costingEnabled = true;
                    pivotCurrency = ServerProxy.getCostingEngine()
                            .getPivotCurrency();
                    pivotCurrencyName = pivotCurrency.getDisplayName();
                    // calculate expenses
                    Cost cost = ServerProxy.getCostingEngine().calculateCost(
                            job, pivotCurrency, true, Cost.EXPENSE);
                    if (cost != null)
                    {
                        Money m = cost.getEstimatedCost();
                        if (m != null)
                            estimatedCost = m.getAmount();
                        m = cost.getFinalCost();
                        if (m != null)
                            finalCost = m.getAmount();
                        m = cost.getOverrideCost();
                        if (m != null)
                        {
                            finalCost = m.getAmount();
                            isOverrideCost = true;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                logger.error(
                        "Failed to get costing information for getStatus() web service call for job "
                                + jobName, e);
            }

            xml.append("<jobStatus>\r\n");
            xml.append("\t<jobName>")
                    .append(EditUtil.encodeXmlEntities(jobName))
                    .append("</jobName>\r\n");
            xml.append("\t<jobId>").append(job.getId()).append("</jobId>\r\n");
            xml.append("\t<status>").append(status).append("</status>\r\n");
            if (costingEnabled)
            {
                xml.append("\t<cost>\r\n");
                xml.append("\t\t<currency>").append(pivotCurrencyName)
                        .append("</currency>\r\n");
                xml.append("\t\t\t<expense>\r\n");
                xml.append("\t\t\t\t<estimatedCost>").append(estimatedCost)
                        .append("</estimatedCost>\r\n");
                xml.append("\t\t\t\t<finalCost isOverrideCost=\"").append(
                        isOverrideCost);
                xml.append("\">").append(finalCost).append("</finalCost>\r\n");
                xml.append("\t\t\t</expense>\r\n");
                xml.append("\t</cost>\r\n");
            }

            xml.append("</jobStatus>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            if (!e.getMessage().startsWith(NOT_IN_DB))
            {
                logger.error("getStatus()", e);
            }
            String message = "Could not get information for job " + jobName;
            message = makeErrorXml("getStatus", message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Returns general information about a job and its workflows.
     * 
     * @param p_accessToken
     * @param p_jobId
     *            String ID of Job
     * @return String An XML description which contains job information and its
     *         workflow information
     * @throws WebServiceException
     */
    public String getJobAndWorkflowInfo(String p_accessToken, long p_jobId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_JOB_AND_WORKFLOW_INFO);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_VIEW);

        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            User user = ServerProxy.getUserManager().getUser(userId);
            Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
            Assert.assertFalse(
                    !isInSameCompany(userName,
                            String.valueOf(job.getCompanyId())),
                    "Cannot access the job which is not in the same company with current user");

            String status = job.getState();
            status = new String(status.getBytes(), "ISO8859-1");

            xml.append("<jobInfo>\r\n");
            xml.append("\t<id>").append(p_jobId).append("</id>\r\n");
            xml.append("\t<name>")
                    .append(EditUtil.encodeXmlEntities(job.getJobName()))
                    .append("</name>\r\n");
            xml.append("\t<status>").append(status).append("</status>\r\n");
            xml.append("\t<createDate>").append(job.getCreateDate().toString())
                    .append("</createDate>\r\n");
            xml.append("\t<priority>").append(job.getPriority())
                    .append("</priority>\r\n");
            xml.append("\t<sourceLocale>")
                    .append(job.getSourceLocale().getDisplayName())
                    .append("</sourceLocale>\r\n");
            xml.append("\t<state>").append(job.getState())
                    .append("</state>\r\n");
            xml.append("\t<pageCount>").append(job.getPageCount())
                    .append("</pageCount>\r\n");
            // Source pages
            xml.append("\t<sourcePages>\r\n");
            List sps = new ArrayList(job.getSourcePages());
            SourcePage sp = null;
            for (int i = 0; i < sps.size(); i++)
            {
                sp = (SourcePage) sps.get(i);
                xml.append("\t\t<sourcePage>\r\n");
                xml.append("\t\t\t<id>").append(sp.getId()).append("</id>\r\n");
                xml.append("\t\t\t<externalPageId>")
                        .append(replaceAndString(sp.getExternalPageId()))
                        .append("</externalPageId>\r\n");
                xml.append("\t\t\t<wordCount>").append(sp.getWordCount())
                        .append("</wordCount>\r\n");
                xml.append("\t\t</sourcePage>\r\n");
            }
            xml.append("\t</sourcePages>\r\n");

            xml.append("\t<sourceWordCount>").append(job.getWordCount())
                    .append("</sourceWordCount>\r\n");

            Collection wfs = job.getWorkflows();

            xml.append("\t<workflowInfo>\r\n");

            long l10nprofile = 0l;
            Workflow w = null;
            TranslationMemoryProfile tmp = null;
            TaskInstance taskInstance = null;
            String currentTaskName = "";
            TimeZone timeZone = ServerProxy.getCalendarManager()
                    .findUserTimeZone(userId);
            Timestamp ts = new Timestamp(Timestamp.DATE, timeZone);
            Locale uiLocale = new Locale(user.getDefaultUILocale());
            ts.setLocale(uiLocale);

            for (Iterator wfi = wfs.iterator(); wfi.hasNext();)
            {
                currentTaskName = "";
                w = (Workflow) wfi.next();
                l10nprofile = w.getJob().getL10nProfileId();

                xml.append("\t\t<workflow>\r\n");
                xml.append("\t\t\t<id>").append(w.getId()).append("</id>\r\n");
                xml.append("\t\t\t<targetLocale>")
                        .append(w.getTargetLocale().getDisplayName())
                        .append("</targetLocale>\r\n");
                xml.append("\t\t\t<state>").append(w.getState())
                        .append("</state>\r\n");
                xml.append("\t\t\t<dispatchDate>")
                        .append(w.getDispatchedDate() == null ? "" : w
                                .getDispatchedDate().toString())
                        .append("</dispatchDate>\r\n");

                // currentActivity
                taskInstance = WorkflowManagerLocal.getCurrentTask(w.getId());
                if (taskInstance != null)
                {
                    currentTaskName = TaskJbpmUtil
                            .getTaskDisplayName(taskInstance.getName());
                }
                xml.append("\t\t\t<currentActivity>").append(currentTaskName)
                        .append("</currentActivity>\r\n");

                xml.append("\t\t\t<estimatedTranslateCompletionDate>");
                if (w.getEstimatedTranslateCompletionDate() != null)
                {
                    ts.setDate(w.getEstimatedTranslateCompletionDate());
                    xml.append(ts);
                    xml.append(" ");
                    xml.append(ts.getHour() + ":");
                    if (ts.getMinute() < 10)
                    {
                        xml.append("0");
                    }
                    xml.append(ts.getMinute());
                    xml.append(" ");
                    xml.append(ts.getTimeZone().getDisplayName(uiLocale));
                }
                else
                {
                    xml.append("--");
                }
                xml.append("</estimatedTranslateCompletionDate>\r\n");

                xml.append("\t\t\t<estimatedCompletionDate>");
                if (w.getEstimatedCompletionDate() != null)
                {
                    ts.setDate(w.getEstimatedCompletionDate());
                    xml.append(ts);
                    xml.append(" ");
                    xml.append(ts.getHour() + ":");
                    if (ts.getMinute() < 10)
                    {
                        xml.append("0");
                    }
                    xml.append(ts.getMinute());
                    xml.append(" ");
                    xml.append(ts.getTimeZone().getDisplayName(uiLocale));
                }
                else
                {
                    xml.append("--");
                }
                xml.append("</estimatedCompletionDate>\r\n");

                if (w.getCompletedDate() != null)
                {
                    xml.append("\t\t\t<completeDate>")
                            .append(w.getCompletedDate().toString())
                            .append("</completeDate>\r\n");
                }

                tmp = ServerProxy.getProjectHandler()
                        .getL10nProfile(l10nprofile)
                        .getTranslationMemoryProfile();

                xml.append("\t\t\t<isInContextMatch>")
                        .append(tmp.getIsContextMatchLeveraging())
                        .append("</isInContextMatch>\r\n");
                xml.append("\t\t\t<percentageComplete>")
                        .append(w.getPercentageCompletion())
                        .append("</percentageComplete>\r\n");
                xml.append("\t\t\t<targetWordCount total=\"")
                        .append(w.getTotalWordCount()).append("\">\r\n");
                xml.append("\t\t\t\t<contextMatch>")
                        .append(w.getContextMatchWordCount())
                        .append("</contextMatch>\r\n");
                xml.append("\t\t\t\t<segmentTmMatch>")
                        .append(w.getSegmentTmWordCount())
                        .append("</segmentTmMatch>\r\n");
                xml.append("\t\t\t\t<lowFuzzyMatch>")
                        .append(w.getLowFuzzyMatchWordCount())
                        .append("</lowFuzzyMatch>\r\n");
                xml.append("\t\t\t\t<medFuzzyMatch>")
                        .append(w.getMedFuzzyMatchWordCount())
                        .append("</medFuzzyMatch>\r\n");
                xml.append("\t\t\t\t<medHiFuzzyMatch>")
                        .append(w.getMedHiFuzzyMatchWordCount())
                        .append("</medHiFuzzyMatch>\r\n");
                xml.append("\t\t\t\t<hiFuzzyMatch>")
                        .append(w.getHiFuzzyMatchWordCount())
                        .append("</hiFuzzyMatch>\r\n");
                xml.append("\t\t\t\t<repetitionMatch>")
                        .append(w.getRepetitionWordCount())
                        .append("</repetitionMatch>\r\n");
                xml.append("\t\t\t\t<noMatch>").append(w.getNoMatchWordCount())
                        .append("</noMatch>\r\n");
                xml.append("\t\t\t\t<noExactMatch>")
                        .append(w.getTotalExactMatchWordCount())
                        .append("</noExactMatch>\r\n");
                xml.append("\t\t\t\t<inContextMatch>")
                        .append(w.getInContextMatchWordCount())
                        .append("</inContextMatch>\r\n");
                xml.append("\t\t\t</targetWordCount>\r\n");
                xml.append("\t\t</workflow>\r\n");
            }
            xml.append("\t</workflowInfo>\r\n");
            xml.append("</jobInfo>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_JOB_AND_WORKFLOW_INFO, e);
            String message = "Could not get information for job " + p_jobId;
            message = makeErrorXml(GET_JOB_AND_WORKFLOW_INFO, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Get job status by job name, added by Leon for GBS-2239(GSSmartBox)
     * 
     * @param p_accessToken
     * @param p_jobName
     * @return
     * @throws WebServiceException
     */
    public String getJobStatus(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_JOB_STATUS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        String id = "";
        String status;
        try
        {
            User user = getUser(getUsernameFromSession(p_accessToken));
            String sql = "SELECT ID, STATE FROM JOB WHERE NAME=? AND COMPANY_ID=?";
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setString(1, p_jobName);
            query.setLong(2,
                    CompanyWrapper.getCompanyByName(user.getCompanyName())
                            .getId());
            results = query.executeQuery();
            if (results.next())
            {
                id = results.getString(1);
                status = results.getString(2);
            }
            else
            {
                // This job is not in the table of DB, it was not created
                status = "IN_QUEUE";
            }
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database to get job status.";
            logger.error(message, cpe);
            message = makeErrorXml("queryJob", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for job status.";
            logger.error(message, sqle);
            message = makeErrorXml("queryJob", message);
            throw new WebServiceException(message);
        }
        catch (WebServiceException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            String message = "Unable to get job information from System4.";
            logger.error(message, e);
            message = makeErrorXml("queryJob", message);
            throw new WebServiceException(message);
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<job>\r\n");
        xml.append("<id>");
        xml.append(id);
        xml.append("</id>\r\n");
        xml.append("<name>");
        xml.append(p_jobName);
        xml.append("</name>\r\n");
        xml.append("<status>");
        xml.append(status);
        xml.append("</status>\r\n");
        xml.append("</job>\r\n");

        return xml.toString();
    }

    /**
     * Get exported job files (do not care if the workflow is "EXPORTED")
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * @return String String in XML format contains information of exported job
     *         files
     * @throws WebServiceException
     */
    public String getJobExportFiles(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_LOCALIZED_DOCUMENTS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);
        checkPermission(p_accessToken, Permission.JOBS_EXPORT);

        String jobName = p_jobName;
        Job job = queryJob(jobName, p_accessToken);
        String jobCompanyId = String.valueOf(job.getCompanyId());
        if (!isInSameCompany(getUsernameFromSession(p_accessToken),
                jobCompanyId))
            throw new WebServiceException(
                    "Cannot access the job which is not in the same company with current user");

        String status = job.getState();
        if (status == null)
        {
            throw new WebServiceException("Job " + jobName + " does not exist.");
        }

        JobFiles jobFiles = new JobFiles();

        StringBuilder prefix = new StringBuilder();
        SystemConfiguration config = SystemConfiguration.getInstance();
        boolean usePublicUrl = "true".equalsIgnoreCase(config
                .getStringParameter("cap.public.url.enable"));
        if (usePublicUrl)
        {
            prefix.append(config.getStringParameter("cap.public.url"));
        }
        else
        {
            prefix.append(capLoginUrl);
        }
        prefix.append("/cxedocs/");
        String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
        prefix.append(URLEncoder.encode(company, "utf-8"));
        jobFiles.setRoot(prefix.toString());

        Set<String> passoloFiles = new HashSet<String>();
        long fileProfileId = -1l;
        FileProfile fp = null;
        FileProfilePersistenceManager fpManager = null;
        boolean isXLZFile = false;

        try
        {
            fpManager = ServerProxy.getFileProfilePersistenceManager();
        }
        catch (Exception e)
        {
            logger.error("Cannot get file profile manager.", e);
            return makeErrorXml(GET_JOB_EXPORT_FILES,
                    "Cannot get file profile manager." + e.getMessage());
        }
        try
        {
            for (Workflow w : job.getWorkflows())
            {
                if (Workflow.CANCELLED.equals(w.getState())
                        || Workflow.PENDING.equals(w.getState())
                        || Workflow.EXPORT_FAILED.equals(w.getState())
                        || Workflow.IMPORT_FAILED.equals(w.getState()))
                    continue;

                ArrayList<String> fileList = new ArrayList<String>();
                for (TargetPage page : w.getTargetPages())
                {
                    SourcePage sPage = page.getSourcePage();
                    if (sPage != null && sPage.isPassoloPage())
                    {
                        String p = sPage.getPassoloFilePath();
                        p = p.replace("\\", "/");
                        p = p.substring(p.indexOf("/") + 1);
                        passoloFiles.add(p);
                        if (fileList.contains(p))
                            continue;
                        else
                        {
                            fileList.add(p);
                        }

                        continue;
                    }
                    fileProfileId = sPage.getRequest().getFileProfileId();
                    fp = fpManager.getFileProfileById(fileProfileId, false);
                    if (fpManager.isXlzReferenceXlfFileProfile(fp.getName()))
                        isXLZFile = true;

                    String path = page.getExternalPageId();
                    path = path.replace("\\", "/");
                    int index = path.indexOf("/");
                    path = path.substring(index);
                    path = getRealFilePathForXliff(path, isXLZFile);

                    if (fileList.contains(path))
                        continue;
                    else
                    {
                        fileList.add(path);
                    }
                    StringBuffer allPath = new StringBuffer();
                    allPath.append(page.getGlobalSightLocale());
                    for (String s : path.split("/"))
                    {
                        if (s.length() > 0)
                        {
                            allPath.append("/").append(
                                    URLEncoder.encode(s, "utf-8"));
                        }
                    }
                    jobFiles.addPath(allPath.toString());

                    isXLZFile = false;
                }
            }

            for (String path : passoloFiles)
            {
                StringBuffer allPath = new StringBuffer();
                allPath.append("passolo");
                for (String s : path.split("/"))
                {
                    if (s.length() > 0)
                    {
                        allPath.append("/").append(
                                URLEncoder.encode(s, "utf-8"));
                    }
                }
                jobFiles.addPath(allPath.toString());
            }

            return com.globalsight.cxe.util.XmlUtil.object2String(jobFiles);
        }
        catch (Exception e)
        {
            logger.error("Error found in getJobExportFiles.", e);
            return makeErrorXml(GET_JOB_EXPORT_FILES, e.getMessage());
        }
    }

    private String getRealFilePathForXliff(String path, boolean isXLZFile)
    {
        if (StringUtil.isEmpty(path))
            return path;

        int index = -1;
        index = path.lastIndexOf(".sub/");
        if (index > 0)
        {
            // one big xliff file is split to some sub-files
            path = path.substring(0, index);
        }
        if (isXLZFile)
        {
            path = path.substring(0, path.lastIndexOf("/"));
            path += ".xlz";
        }

        return path;
    }

    /**
     * Return exported files information for job's "EXPORTED" state workflows.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * @param workflowLocale
     *            Locale of workflow, it can accept fr_FR, fr-FR, fr_fr formats
     *            If it is null, all "EXPORTED" workflows' exported files info
     *            will be returned.
     * @return String String in XML format contains all exported files list of
     *         job according with special workflow locale
     * @throws WebServiceException
     */
    public String getJobExportWorkflowFiles(String p_accessToken,
            String p_jobName, String workflowLocale) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_LOCALIZED_DOCUMENTS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);
        checkPermission(p_accessToken, Permission.JOBS_EXPORT);

        String jobName = p_jobName;
        Job job = queryJob(jobName, p_accessToken);
        String jobCompanyId = String.valueOf(job.getCompanyId());
        if (!isInSameCompany(getUsernameFromSession(p_accessToken),
                jobCompanyId))
            throw new WebServiceException(
                    "Cannot access the job which is not in the same company with current user");

        String status = job.getState();
        if (status == null)
        {
            throw new WebServiceException("Job " + jobName + " does not exist.");
        }

        JobFiles jobFiles = new JobFiles();
        long fileProfileId = -1l;
        FileProfile fp = null;
        FileProfilePersistenceManager fpManager = null;
        boolean isXLZFile = false;

        try
        {
            fpManager = ServerProxy.getFileProfilePersistenceManager();
        }
        catch (Exception e)
        {
            logger.error("Cannot get file profile manager.", e);
            return makeErrorXml(GET_JOB_EXPORT_WORKFLOW_FILES,
                    "Cannot get file profile manager." + e.getMessage());
        }

        StringBuilder prefix = new StringBuilder();
        SystemConfiguration config = SystemConfiguration.getInstance();
        boolean usePublicUrl = "true".equalsIgnoreCase(config
                .getStringParameter("cap.public.url.enable"));
        if (usePublicUrl)
        {
            prefix.append(config.getStringParameter("cap.public.url"));
        }
        else
        {
            prefix.append(capLoginUrl);
        }
        prefix.append("/cxedocs/");
        String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
        prefix.append(URLEncoder.encode(company, "utf-8"));
        jobFiles.setRoot(prefix.toString());

        Set<String> passoloFiles = new HashSet<String>();

        try
        {
            for (Workflow w : job.getWorkflows())
            {
                if (StringUtil.isEmpty(workflowLocale))
                {
                    // need to download all 'Exported' workflow files
                    if (!Workflow.EXPORTED.equals(w.getState()))
                        continue;
                }
                else
                {
                    // download workflow files
                    if (!isWorkflowOfLocaleExported(w, workflowLocale))
                        continue;
                }
                ArrayList<String> fileList = new ArrayList<String>();
                for (TargetPage page : w.getTargetPages())
                {
                    SourcePage sPage = page.getSourcePage();
                    if (sPage != null && sPage.isPassoloPage())
                    {
                        String p = sPage.getPassoloFilePath();
                        p = p.replace("\\", "/");
                        p = p.substring(p.indexOf("/") + 1);
                        passoloFiles.add(p);
                        if (fileList.contains(p))
                            continue;
                        else
                        {
                            fileList.add(p);
                        }

                        continue;
                    }

                    fileProfileId = sPage.getRequest().getFileProfileId();
                    fp = fpManager.getFileProfileById(fileProfileId, false);
                    if (fpManager.isXlzReferenceXlfFileProfile(fp.getName()))
                        isXLZFile = true;

                    String path = page.getExternalPageId();
                    path = path.replace("\\", "/");
                    int index = path.indexOf("/");
                    path = path.substring(index);
                    path = getRealFilePathForXliff(path, isXLZFile);

                    if (fileList.contains(path))
                        continue;
                    else
                    {
                        fileList.add(path);
                    }

                    StringBuffer allPath = new StringBuffer();
                    allPath.append(page.getGlobalSightLocale());
                    for (String s : path.split("/"))
                    {
                        if (s.length() > 0)
                        {
                            allPath.append("/").append(
                                    URLEncoder.encode(s, "utf-8"));
                        }
                    }
                    jobFiles.addPath(allPath.toString());
                }
            }

            for (String path : passoloFiles)
            {
                StringBuffer allPath = new StringBuffer();
                allPath.append("passolo");
                for (String s : path.split("/"))
                {
                    if (s.length() > 0)
                    {
                        allPath.append("/").append(
                                URLEncoder.encode(s, "utf-8"));
                    }
                }
                jobFiles.addPath(allPath.toString());
            }

            return com.globalsight.cxe.util.XmlUtil.object2String(jobFiles);
        }
        catch (Exception e)
        {
            logger.error("Error found in " + GET_JOB_EXPORT_WORKFLOW_FILES, e);
            return makeErrorXml(GET_JOB_EXPORT_WORKFLOW_FILES, e.getMessage());
        }
    }

    private boolean isWorkflowOfLocaleExported(Workflow workflow, String locale)
    {
        if (workflow == null || StringUtil.isEmpty(locale))
            return false;

        String workflowState = workflow.getState();
        if (!workflowState.equals(Workflow.EXPORTED))
            return false;

        String lowerWorkflowLocale = workflow.getTargetLocale().toString()
                .toLowerCase();
        String lowerLocale = locale.replace('-', '_').toLowerCase();
        if (lowerWorkflowLocale.equals(lowerLocale))
            return true;
        else
            return false;
    }

    /**
     * To check if the exported file is ready to be read/download
     * 
     * @param jobCompanyId
     *            Company Id of user which created the job
     * @param filename
     *            filename of exported file including path
     * @return boolean If the exported file is ready, then return true.
     */
    private boolean isFileExported(String jobCompanyId, String filename)
    {
        if (StringUtil.isEmpty(jobCompanyId) || StringUtil.isEmpty(filename))
            return false;

        String baseDocDir = AmbFileStoragePathUtils
                .getCxeDocDirPath(jobCompanyId);
        filename = baseDocDir + File.separator + filename;
        String tmpFilename = filename + ".tmp";
        File exportedFile = null, tmpFile = null;
        exportedFile = new File(filename);
        tmpFile = new File(tmpFilename);
        if (exportedFile.renameTo(tmpFile))
        {
            tmpFile.renameTo(exportedFile);
            tmpFile.delete();
            return true;
        }
        else
            return false;
    }

    /**
     * @deprecated Returns an XML description containing URLs to a localized
     *             document for each workflow in the desired job if the job has
     *             been exported.
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @return String An XML description which contains URLs to a localized
     *         document for each workflow
     * @throws WebServiceException
     */
    public String getLocalizedDocuments(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_LOCALIZED_DOCUMENTS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        String jobName = p_jobName;
        Job job = queryJob(jobName, p_accessToken);
        String status = job.getState();
        if (status == null)
        {
            throw new WebServiceException("Job " + jobName + " does not exist.");
        }

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<localizedDocuments>\r\n");
        if ("EXPORTED".equalsIgnoreCase(status))
        {
            Collection workflows = job.getWorkflows();
            if (workflows == null || workflows.size() < 1)
            {
                throw new WebServiceException("workflows does not exist.");
            }

            String urlPrefix = determineUrlPrefix(CompanyWrapper
                    .getCompanyNameById(job.getCompanyId()));
            xml.append("<urlPrefix>")
                    .append(EditUtil.encodeXmlEntities(urlPrefix))
                    .append("</urlPrefix>\r\n");

            Iterator iterator = workflows.iterator();

            while (iterator.hasNext())
            {
                Workflow workflow = (Workflow) iterator.next();
                String targetLocale = workflow.getTargetLocale().toString();
                xml.append("<targetLocale>")
                        .append(EditUtil.encodeXmlEntities(targetLocale))
                        .append("</targetLocale>\r\n");
            }
        }

        xml.append("</localizedDocuments>\r\n");
        return xml.toString();
    }

    /**
     * @deprecated Returns an XML description containing URLs to a localized
     *             document for each workflow in the desired job if the job has
     *             been exported.
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @param p_wfId
     *            Workflow ID
     * @return String An XML description which contains URLs to a localized
     *         document for each workflow
     */
    public String getLocalizedDocuments(String p_accessToken, String p_jobName,
            String p_wfId) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_LOCALIZED_DOCUMENTS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        String jobName = p_jobName;
        Job job = queryJob(jobName, p_accessToken);
        String status = job.getState();
        if (status == null)
        {
            throw new WebServiceException("Job " + jobName + " does not exist.");
        }

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");

        xml.append("<localizedDocuments>\r\n");
        Workflow wf = null;

        try
        {
            wf = ServerProxy.getWorkflowManager().getWorkflowById(
                    Long.parseLong(p_wfId));
        }
        catch (Exception e)
        {
            throw new WebServiceException("Failed to get workflow "
                    + e.getMessage());
        }

        // if (wf == null || !wf.getState().equals("EXPORTED"))
        // {
        // throw new
        // WebServiceException("workflows does not exist or its state is not EXPORTED.");
        // }

        String urlPrefix = determineUrlPrefix(CompanyWrapper
                .getCompanyNameById(job.getCompanyId()));
        xml.append("<urlPrefix>").append(EditUtil.encodeXmlEntities(urlPrefix))
                .append("</urlPrefix>\r\n");

        String targetLocale = wf.getTargetLocale().toString();
        xml.append("<targetLocale>")
                .append(EditUtil.encodeXmlEntities(targetLocale))
                .append("</targetLocale>\r\n");

        xml.append("</localizedDocuments>\r\n");
        return xml.toString();
    }

    /**
     * @deprecated Returns an XML description containing URLs to a localized
     *             document for each workflow in the desired job if the job has
     *             been exported.
     * 
     * @param p_accessToken
     * @param p_jobName
     *            Job name
     * @return String An XML description which contains URLs to a localized
     *         document for each workflow if the job has been exported
     * @throws WebServiceException
     */
    public String getLocalizedDocuments_old(String p_accessToken,
            String p_jobName) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_LOCALIZED_DOCUMENTS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        String jobName = p_jobName;
        Job job = queryJob(jobName, p_accessToken);
        String status = job.getState();
        if (status == null)
            throw new WebServiceException("Job " + jobName + " does not exist.");

        // query all the target pages for the workflows for this job
        StringBuffer sql = new StringBuffer();
        sql.append(
                "select source_page.external_page_id, locale.iso_lang_code, ")
                .append("locale.iso_country_code, target_page.export_sub_dir")
                .append(" from job, workflow, request, source_page, locale, target_page")
                .append(" where job.name=?")
                .append(" and workflow.job_id=job.id")
                .append(" and request.job_id=job.id")
                .append(" and source_page.id=request.page_id")
                .append(" and locale.id=workflow.target_locale_id")
                .append(" and source_page.id=target_page.source_page_id")
                .append(" and workflow.iflow_instance_id = target_page.workflow_iflow_instance_id")
                .append(" and target_page.state =?");

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<localizedDocuments>\r\n");
        xml.append("<jobName>").append(EditUtil.encodeXmlEntities(jobName))
                .append("</jobName>\r\n");
        xml.append("<jobStatus>").append(status).append("</jobStatus>\r\n");

        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql.toString());
            query.setString(1, jobName);
            query.setString(2, "EXPORTED");
            results = query.executeQuery();
            boolean gotSomeResult = false;
            String urlPrefix = determineUrlPrefix(CompanyWrapper
                    .getCompanyNameById(job.getCompanyId()));

            while (results.next())
            {
                String fileName = results.getString(1);
                String langCode = results.getString(2);
                String countryCode = results.getString(3);
                String exportSubDir = results.getString(4);
                StringBuffer locale = new StringBuffer(langCode);
                locale.append("_").append(countryCode);

                String targetFileName = replaceLocaleInFileName(fileName,
                        exportSubDir, locale.toString());
                targetFileName = targetFileName.replace('\\', '/');
                String encodedTargetFileName = "";
                // String[] names = targetFileName.split("/");
                // for(int i = 0; i < names.length; i++)
                // {
                // encodedTargetFileName = encodedTargetFileName + "/" +
                // URLEncoder.encode(names[i]);
                // }
                encodedTargetFileName = URLEncoder
                        .encodeUrlString(targetFileName);
                xml.append("<targetPage locale=\"").append(locale.toString())
                        .append("\"");
                Locale l = new Locale(langCode, countryCode);
                xml.append(" localeDisplayName=\"").append(l.getDisplayName())
                        .append("\">");
                xml.append(urlPrefix).append(targetFileName);
                xml.append("</targetPage>\r\n");
                gotSomeResult = true;
            }

            if (!gotSomeResult)
                throw new WebServiceException("No documents for job name "
                        + jobName);

        }
        catch (ConnectionPoolException cpe)
        {
            logger.error(
                    "Unable to connect to database to get localized documents.",
                    cpe);
        }
        catch (SQLException sqle)
        {
            logger.error("Unable to query DB for localized documents.", sqle);
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }

        xml.append("</localizedDocuments>\r\n");
        return xml.toString();
    }

    /**
     * Cancels the job. If p_workflowLocale is null then all workflows are
     * canceled, otherwise the specific workflow corresponding to the locale is
     * canceled.
     * 
     * @param p_jobName
     *            -- name of job
     * @param p_workflowLocale
     *            -- locale of workflow to cancel
     * @return String
     * @exception WebServiceException
     */
    public String cancelWorkflow(String p_accessToken, String p_jobName,
            String p_workflowLocale) throws WebServiceException
    {
        checkAccess(p_accessToken, CANCEL_WORKFLOW);
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_DISCARD);

        String jobName = p_jobName;
        String workflowLocale = p_workflowLocale;
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            Job job = queryJob(jobName, p_accessToken);
            String status = job.getState();
            boolean didCancel = false;
            String userName = this.getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            if (workflowLocale == null)
            {
                // cancel the whole job
                logger.info("Cancelling all workflows for job " + jobName);
                ServerProxy.getJobHandler().cancelJob(userId, job, null);
                didCancel = true;
            }
            else
            {
                if (!status.equals(Job.DISPATCHED)
                        && !status.equals(Job.READY_TO_BE_DISPATCHED))
                    throw new WebServiceException(
                            "You can only discard workflows that are in the following states:DISPATCHED or READY_TO_BE_DISPATCHED");

                // cancel just one workflow
                Locale locale = GlobalSightLocale
                        .makeLocaleFromString(workflowLocale);
                Object[] workflows = job.getWorkflows().toArray();
                logger.info("Job " + jobName + " has " + workflows.length
                        + " workflow.");
                for (int i = 0; i < workflows.length; i++)
                {
                    Workflow w = (Workflow) workflows[i];
                    Locale wLocale = w.getTargetLocale().getLocale();
                    if (locale.equals(wLocale))
                    {
                        logger.info("Cancelling workflow " + workflowLocale
                                + " for job " + jobName);
                        ServerProxy.getWorkflowManager().cancel(userId, w);
                        didCancel = true;
                        break;
                    }
                }
            }

            if (didCancel == false)
                throw new Exception("No workflow for locale " + workflowLocale);

            xml.append("<cancelStatus>\r\n");
            xml.append("\t<jobName>")
                    .append(EditUtil.encodeXmlEntities(jobName))
                    .append("</jobName>\r\n");
            if (workflowLocale == null)
                xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
            else
                xml.append("\t<workflowLocale>").append(workflowLocale)
                        .append("</workflowLocale>\r\n");
            xml.append("\t<status>canceled</status>\r\n");
            xml.append("</cancelStatus>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error("cancelWorkflow()", e);
            String message = "Could not cancel workflow for job " + jobName;
            message = makeErrorXml("cancelWorkflow", message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Cancels the job
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @return An XML description which contains canceling information
     * @throws WebServiceException
     */
    public String cancelJob(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, CANCEL_JOB);
        checkPermission(p_accessToken, Permission.JOBS_DISCARD);

        String jobName = p_jobName;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);

            Job job = queryJob(jobName, p_accessToken);
            if (!UserUtil.isInProject(userId,
                    String.valueOf(job.getProjectId())))
                throw new WebServiceException(
                        "Current user cannot cancel the job.");

            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            logger.info("Cancelling all workflows for job " + jobName);
            ServerProxy.getJobHandler().cancelJob(userId, job, null);
            xml.append("<cancelStatus>\r\n");
            xml.append("\t<jobName>")
                    .append(EditUtil.encodeXmlEntities(jobName))
                    .append("</jobName>\r\n");
            xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
            xml.append("\t<status>canceled</status>\r\n");
            xml.append("</cancelStatus>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error("cancelJob()", e);
            String message = "Could not cancel job " + jobName;
            message = makeErrorXml("cancelJob", message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Cancels the job and all of its workflow - job specified by its id.
     * 
     * @param p_accessToken
     * @param p_jobId
     *            String ID of job
     * @return An XML description which contains canceling information
     * @throws WebServiceException
     */
    public String cancelJobById(String p_accessToken, long p_jobId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, CANCEL_JOB_BY_ID);
        checkPermission(p_accessToken, Permission.JOBS_DISCARD);

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
            if (!UserUtil.isInProject(userId,
                    String.valueOf(job.getProjectId())))
                throw new WebServiceException(
                        "Current user cannot cancel the job.");

            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            logger.info("Cancelling all workflows for job " + p_jobId);
            ServerProxy.getJobHandler().cancelJob(userId, job, null);
            xml.append("<cancelStatus>\r\n");
            xml.append("\t<jobId>").append(p_jobId).append("</jobId>\r\n");
            xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
            xml.append("\t<status>canceled</status>\r\n");
            xml.append("</cancelStatus>\r\n");
            return xml.toString();
        }
        catch (JobException je)
        {
            StringBuffer messageBuf = new StringBuffer(
                    "Unable to cancel the job ");
            messageBuf.append(p_jobId);

            // couldn't find the user specified
            if (je.getMessageKey().equals(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_ID))
            {
                messageBuf.append(" The job couldn't be found.");
            }
            String message = messageBuf.toString();
            logger.error(message, je);
            message = makeErrorXml(CANCEL_JOB_BY_ID, message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(CANCEL_JOB_BY_ID, e);
            String message = "Could not cancel job " + p_jobId;
            message = makeErrorXml(CANCEL_JOB_BY_ID, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Cancels multiple jobs and all of their workflow in one time.
     * 
     * @param p_accessToken
     * @param p_jobIds
     *            String ID of jobs, "," to split
     * @return An XML description which contains canceling information
     * @throws WebServiceException
     * @author Vincent Yan, 2011/01/17
     */
    public String cancelJobs(String p_accessToken, String p_jobIds)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_jobIds, "Job IDs");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("cancelJobs", e.getMessage());
            throw new WebServiceException(message);
        }

        checkAccess(p_accessToken, CANCEL_JOB_BY_ID);
        checkPermission(p_accessToken, Permission.JOBS_DISCARD);

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            String[] jobIds = p_jobIds.split(",");
            String sJobId = "";
            long jobId = 0L;
            Job job = null;
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("<cancelStatus>\r\n");

            for (int i = 0; i < jobIds.length; i++)
            {
                sJobId = jobIds[i].trim();
                jobId = Long.parseLong(sJobId);
                job = jobHandler.getJobById(jobId);

                if (!UserUtil.isInProject(userId,
                        String.valueOf(job.getProjectId())))
                    throw new WebServiceException(
                            "Current user cannot cancel the job.");

                logger.info("Cancelling all workflows for job " + jobId);

                jobHandler.cancelJob(userId, job, null);

                xml.append("\t<jobId>").append(jobId).append("</jobId>\r\n");
                xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
                xml.append("\t<status>canceled</status>\r\n");
            }

            xml.append("</cancelStatus>\r\n");
            return xml.toString();
        }
        catch (JobException je)
        {
            StringBuffer messageBuf = new StringBuffer(
                    "Unable to cancel the job ");
            messageBuf.append(p_jobIds);

            // couldn't find the user specified
            if (je.getMessageKey().equals(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_ID))
            {
                messageBuf.append(" The job couldn't be found.");
            }
            String message = messageBuf.toString();
            logger.error(message, je);
            message = makeErrorXml(CANCEL_JOB_BY_ID, message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(CANCEL_JOB_BY_ID, e);
            String message = "Could not cancel job " + p_jobIds;
            message = makeErrorXml(CANCEL_JOB_BY_ID, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Exports the job. If p_workflowLocale is null then all pages for all
     * workflows are exported, otherwise the specific workflow corresponding to
     * the locale is exported.
     * 
     * @param p_jobName
     *            -- name of job
     * @param p_workflowLocale
     *            -- locale of workflow to export
     * @return String
     * @exception WebServiceException
     */
    public String exportWorkflow(String p_accessToken, String p_jobName,
            String p_workflowLocale) throws WebServiceException
    {
        checkAccess(p_accessToken, EXPORT_WORKFLOW);
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_EXPORT);

        String jobName = p_jobName;
        String workflowLocale = p_workflowLocale;
        String returnXml = "";
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            Job job = queryJob(jobName, p_accessToken);
            Object[] workflows = job.getWorkflows().toArray();
            long projectId = job.getL10nProfile().getProject().getId();
            User projectMgr = ServerProxy.getProjectHandler()
                    .getProjectById(projectId).getProjectManager();
            boolean didExport = false;

            if (workflowLocale == null)
            {
                // export all workflow
                logger.info("Exporting all " + workflows.length
                        + " workflows for job " + jobName);
                for (int i = 0; i < workflows.length; i++)
                {
                    Workflow w = (Workflow) workflows[i];
                    if (!w.getState().equals(Workflow.IMPORT_FAILED)
                            && !w.getState().equals(Workflow.CANCELLED))
                    {
                        exportSingleWorkflow(job, w, projectMgr);
                    }
                }
                didExport = true;
            }
            else
            {
                // export just one workflow
                Locale locale = GlobalSightLocale
                        .makeLocaleFromString(workflowLocale);
                logger.info("Job " + jobName + " has " + workflows.length
                        + " workflow.");
                for (int i = 0; i < workflows.length; i++)
                {
                    Workflow w = (Workflow) workflows[i];
                    Locale wLocale = w.getTargetLocale().getLocale();
                    if (locale.equals(wLocale))
                    {
                        exportSingleWorkflow(job, w, projectMgr);
                        didExport = true;
                        break;
                    }
                }
            }

            if (didExport == false)
                throw new Exception("No workflow for locale " + workflowLocale);

            xml.append("<exportStatus>\r\n");
            xml.append("\t<jobName>")
                    .append(EditUtil.encodeXmlEntities(jobName))
                    .append("</jobName>\r\n");
            if (workflowLocale == null)
                xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
            else
                xml.append("\t<workflowLocale>").append(workflowLocale)
                        .append("</workflowLocale>\r\n");
            xml.append("\t<status>Export Request Sent</status>\r\n");
            xml.append("</exportStatus>\r\n");
            returnXml = xml.toString();
        }
        catch (Exception e)
        {
            logger.error("exportWorkflow()", e);
            String message = "Could not export workflow for job " + jobName;
            message = makeErrorXml("exportWorkflow", message);
            throw new WebServiceException(message);
        }
        return returnXml;
    }

    /**
     * Exports the job specified by job name
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @return String An XML description which contains the returned message to
     *         export job
     * @throws WebServiceException
     */
    public String exportJob(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, EXPORT_JOB);
        checkPermission(p_accessToken, Permission.JOBS_EXPORT);

        String jobName = p_jobName;
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            Job job = queryJob(jobName, p_accessToken);
            Object[] workflows = job.getWorkflows().toArray();
            long projectId = job.getL10nProfile().getProject().getId();
            User projectMgr = ServerProxy.getProjectHandler()
                    .getProjectById(projectId).getProjectManager();

            // export all workflow
            logger.info("Exporting all " + workflows.length
                    + " workflows for job " + jobName);
            for (int i = 0; i < workflows.length; i++)
            {
                Workflow w = (Workflow) workflows[i];
                if (!w.getState().equals(Workflow.IMPORT_FAILED)
                        && !w.getState().equals(Workflow.CANCELLED))
                {
                    exportSingleWorkflow(job, w, projectMgr);
                }
            }
            xml.append("<exportStatus>\r\n");
            xml.append("\t<jobName>")
                    .append(EditUtil.encodeXmlEntities(jobName))
                    .append("</jobName>\r\n");
            xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
            xml.append("\t<status>Export Request Sent</status>\r\n");
            xml.append("</exportStatus>\r\n");
            return xml.toString();

        }
        catch (Exception e)
        {
            logger.error("exportJob()", e);
            String message = "Could not export job " + jobName;
            message = makeErrorXml("exportJob", message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Returns basic information about all the accepted tasks in the specified
     * workflow.
     * 
     * @param p_accessToken
     * @param p_workflowId
     *            ID of workflow
     * @return String An XML description which contains basic information about
     *         all the accepted tasks in the specified workflow
     * @throws WebServiceException
     */
    public String getAcceptedTasksInWorkflow(String p_accessToken,
            long p_workflowId) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_ACCEPTED_TASKS);
        checkPermission(p_accessToken, Permission.ACTIVITIES_ACCEPT);

        Collection taskInfos = null;
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("<acceptedTasks>\r\n");
            xml.append("<workflowId>").append(p_workflowId)
                    .append("</workflowId>\r\n");
            taskInfos = ServerProxy.getTaskManager()
                    .getAcceptedTaskInfosInWorkflow(p_workflowId);
            for (Iterator i = taskInfos.iterator(); i.hasNext();)
            {
                TaskInfo ti = (TaskInfo) i.next();
                xml.append("\t<task>\r\n");
                xml.append("\t\t<id>").append(ti.getId()).append("</id>\r\n");
                xml.append("\t\t<name>").append(ti.getName())
                        .append("</name>\r\n");
                xml.append("\t\t<state>").append(ti.getStateAsString())
                        .append("</state>\r\n");
                xml.append("\t\t<acceptByDate>")
                        .append(ti.getAcceptByAsString())
                        .append("</acceptByDate>\r\n");
                // these are accepted tasks so the date and user id should be
                // set
                xml.append("\t\t<acceptedDate>")
                        .append(ti.getAcceptedDateAsString())
                        .append("</acceptedDate>\r\n");

                xml.append("\t\t<accepter>\r\n");
                // get user information about the user who accepted the task
                UserInfo ui = getUserInfo(ti.getAcceptor());
                xml.append("\t\t\t<userid>").append(ui.getUserName())
                        .append("</userid>\r\n");
                xml.append("\t\t\t<firstName>").append(ui.getFirstName())
                        .append("</firstName>\r\n");
                xml.append("\t\t\t<lastName>").append(ui.getLastName())
                        .append("</lastName>\r\n");
                xml.append("\t\t\t<title>").append(ui.getTitle())
                        .append("</title>\r\n");
                xml.append("\t\t\t<email>").append(ui.getEmailAddress())
                        .append("</email>\r\n");
                xml.append("\t\t</accepter>\r\n");

                xml.append("\t\t<completeByDate>")
                        .append(ti.getCompleteByAsString())
                        .append("</completeByDate>\r\n");
                if (ti.getCompletedDateAsString() != null)
                {
                    xml.append("\t\t<completedDate>")
                            .append(ti.getCompletedDateAsString())
                            .append("</completedDate>\r\n");
                }
                xml.append("\t</task>\r\n");

            }
            xml.append("</acceptedTasks>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_ACCEPTED_TASKS, e);
            String message = "Could not get the accepted tasks in workflow "
                    + p_workflowId;
            message = makeErrorXml(GET_ACCEPTED_TASKS, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Get basic information about the current tasks of a workflow specified by
     * the given id. Note that a current task could be either in the active
     * state or accepted state. There could also be more than one current task
     * for branching workflows.
     * 
     * @param p_accessToken
     *            - The access token received from the login.
     * @param p_workflowId
     *            - The id of the workflow for which its tasks are queried.
     * 
     * @return The basic info about the tasks in XML format.
     */
    public String getCurrentTasksInWorkflow(String p_accessToken,
            long p_workflowId) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_TASKS);
        checkPermission(p_accessToken, Permission.ACTIVITIES_VIEW);

        Collection tasks = null;
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("  <tasksInWorkflow>\r\n");
            xml.append("    <workflowId>").append(p_workflowId)
                    .append("</workflowId>\r\n");
            tasks = ServerProxy.getTaskManager().getCurrentTasks(p_workflowId);
            Object[] taskArray = tasks == null ? null : tasks.toArray();
            int size = taskArray == null ? -1 : taskArray.length;

            for (int i = 0; i < size; i++)
            {
                Task ti = (Task) taskArray[i];
                xml.append("\t<task>\r\n");
                xml.append("\t\t<id>").append(ti.getId()).append("</id>\r\n");
                xml.append("\t\t<name>").append(ti.getTaskName())
                        .append("</name>\r\n");
                xml.append("\t\t<state>").append(ti.getStateAsString())
                        .append("</state>\r\n");
                xml.append("\t</task>\r\n");
            }

            xml.append("  </tasksInWorkflow>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_TASKS, e);
            String message = "Could not get the tasks for workflow with id "
                    + p_workflowId;
            message = makeErrorXml(GET_CURRENT_TASKS, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Get basic information about the tasks based on the given task name for
     * the job specified by the job id. Note that a job could have more than one
     * workflow and each workflow could have more than one task with the same
     * name.
     * 
     * @param p_accessToken
     *            - The access token received from the login.
     * @param p_jobId
     *            - The id of the job for which its workflows might
     * @param p_taskName
     *            - The name for the task(s) to be searched for. have a task
     *            with the specified name. Can be null.
     * 
     * @return The basic info about the tasks in XML format.
     */
    public String getTasksInJob(String p_accessToken, long p_jobId,
            String p_taskName) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_TASKS);
        checkPermission(p_accessToken, Permission.ACTIVITIES_VIEW);

        Collection taskInfos = null;
        Connection connection = null;
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            if (!UserUtil.isInProject(userId,
                    String.valueOf(job.getProjectId())))
                throw new WebServiceException(
                        "Current user does not have permission to get task information");

            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("<tasksInJob>\r\n");
            xml.append("\t<jobId>").append(p_jobId).append("</jobId>\r\n");
            taskInfos = ServerProxy.getTaskManager().getTasks(p_taskName,
                    p_jobId);
            Object[] tasks = taskInfos == null ? null : taskInfos.toArray();
            int size = tasks == null ? -1 : tasks.length;

            connection = ConnectionPool.getConnection();

            for (int i = 0; i < size; i++)
            {
                Task ti = (Task) tasks[i];
                buildXmlForTask(xml, ti, "\t", connection);
            }
            xml.append("</tasksInJob>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_TASKS, e);
            String message = "Could not get the tasks with the name "
                    + p_taskName + " for job with id " + p_jobId;
            message = makeErrorXml(GET_TASKS, message);
            throw new WebServiceException(message);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (Exception e2)
            {
                logger.error("Cannot release database connection correctly.",
                        e2);
            }
        }
    }

    /**
     * Get tasks info with batch of job ids
     * 
     * @param p_accessToken
     *            Access token
     * @param jobIds
     *            Batch of job ids, split with comma
     * @param p_taskName
     *            Task name with company id such as 'Translation1_1', if task
     *            name is null then return all types of tasks
     * @return String XML format string
     * @throws WebServiceException
     * 
     * @author Vincent Yan, 2012/08/08
     * @since 8.2.3
     */
    public String getTasksInJobs(String p_accessToken, String jobIds,
            String p_taskName) throws WebServiceException
    {
        if (StringUtil.isEmpty(p_accessToken) || StringUtil.isEmpty(jobIds))
            return makeErrorXml("getTasksInJobs(String, String, String)",
                    "Invaild parameter.");

        try
        {
            checkPermission(p_accessToken, Permission.ACTIVITIES_VIEW);
        }
        catch (Exception e)
        {
            return makeErrorXml("getTasksInJobs", e.getMessage());
        }

        Collection taskInfos = null;
        Connection connection = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);

            String[] jobIdArray = jobIds.split(",");

            JobHandlerWLRemote jobHandlerLocal = ServerProxy.getJobHandler();
            Job job = null;

            StringBuffer xml = new StringBuffer(XML_HEAD);
            xml.append("<jobs>\r\n");
            long jobId = -1;
            connection = ConnectionPool.getConnection();

            for (String jobIdString : jobIdArray)
            {
                if (StringUtil.isEmpty(jobIdString))
                    continue;
                try
                {
                    jobId = Long.parseLong(jobIdString.trim());
                    job = jobHandlerLocal.getJobById(Long.valueOf(jobIdString));
                    if (job == null)
                        continue;

                    if (!UserUtil.isInProject(userId,
                            String.valueOf(job.getProjectId())))
                    {
                        continue;
                    }
                }
                catch (Exception e)
                {
                    continue;
                }

                if (job != null)
                {
                    xml.append("\t<job>\r\n");

                    xml.append("\t\t<job_id>").append(jobIdString)
                            .append("</job_id>\r\n");
                    xml.append("\t\t<job_name>")
                            .append(EditUtil.encodeXmlEntities(job.getJobName()))
                            .append("</job_name>\r\n");

                    taskInfos = ServerProxy.getTaskManager().getTasks(
                            p_taskName, jobId);
                    Object[] tasks = taskInfos == null ? null : taskInfos
                            .toArray();
                    int size = tasks == null ? -1 : tasks.length;

                    for (int i = 0; i < size; i++)
                    {
                        Task ti = (Task) tasks[i];
                        buildXmlForTask(xml, ti, "\t\t", connection);
                    }

                    xml.append("\t</job>\r\n");
                }
            }
            xml.append("</jobs>");

            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_TASKS, e);
            String message = "Could not get the tasks with the name "
                    + p_taskName + " for job with id (" + jobIds + ")";
            return makeErrorXml(GET_TASKS, message);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (Exception e2)
            {
                logger.error("Cannot release database connection correctly.",
                        e2);
            }
        }
    }

    /**
     * Accept specified task.
     * 
     * @param p_accessToken
     *            The access token received from the login.
     * @param p_taskId
     *            Task Id to be accepted.
     * 
     * @throws WebServiceException
     */
    public String acceptTask(String p_accessToken, String p_taskId)
            throws WebServiceException
    {
        String rtnString = "success";
        checkAccess(p_accessToken, ACCEPT_TASK);
        checkPermission(p_accessToken, Permission.ACTIVITIES_ACCEPT);

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertIsInteger(p_taskId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml(ACCEPT_TASK, e.getMessage());
            throw new WebServiceException(message);
        }

        String acceptorName = getUsernameFromSession(p_accessToken);
        String acceptor = UserUtil.getUserIdByName(acceptorName);

        Task task = null;
        try
        {
            task = TaskHelper.getTask(Long.parseLong(p_taskId));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to get task object by taskId : "
                    + p_taskId;
            message = makeErrorXml(ACCEPT_TASK, message);
            throw new WebServiceException(message);
        }

        try
        {
            if (task != null)
            {
                // GS will check if the acceptor is PM or available users
                TaskHelper.acceptTask(acceptor, task);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to accept task for taskId : " + p_taskId
                    + ",maybe '" + acceptor
                    + "' do not have the authority to operate the task";
            message = makeErrorXml(ACCEPT_TASK, message);
            throw new WebServiceException(message);
        }

        return rtnString;
    }

    /**
     * Complete task
     * 
     * @param p_accessToken
     *            The access token received from the login.
     * @param p_taskId
     *            Task Id to be completed.
     * @param p_destinationArrow
     *            This points to the next activity. Null if this task has no
     *            condition node.
     * @throws WebServiceException
     */
    public String completeTask(String p_accessToken, String p_taskId,
            String p_destinationArrow) throws WebServiceException
    {
        String rtnStr = "success";
        checkAccess(p_accessToken, "completeTask");
        checkPermission(p_accessToken, Permission.ACTIVITIES_ACCEPT);

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertIsInteger(p_taskId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml(COMPLETE_TASK, e.getMessage());
            throw new WebServiceException(message);
        }

        String userName = this.getUsernameFromSession(p_accessToken);
        String userId = UserUtil.getUserIdByName(userName);

        // Task object
        TaskManager taskManager = ServerProxy.getTaskManager();
        Task task = null;
        try
        {
            task = taskManager.getTask(Long.parseLong(p_taskId));
        }
        catch (RemoteException re)
        {
            String msg = "Fail to get task object by taskId : " + p_taskId;
            logger.error(msg, re);
            msg = makeErrorXml("completeTask", msg);
            throw new WebServiceException(msg);
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
        }

        // Compelte task
        String completeUserId = null;
        try
        {
            // Find the user to complete task.
            WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
                    .getWorkflowTaskInstance(userId, task.getId(),
                            WorkflowConstants.TASK_ALL_STATES);
            task.setWorkflowTask(wfTask);
            List allAssignees = task.getAllAssignees();
            if (allAssignees != null && allAssignees.size() > 0)
            {
                if (!allAssignees.contains(userId))
                {
                    String message = "'"
                            + userName
                            + "' is not an available assignee for current task "
                            + p_taskId;
                    logger.error(message);
                    message = makeErrorXml("completeTask", message);
                    throw new WebServiceException(message);
                }
            }

            if (task.getState() == Task.STATE_ACCEPTED)
            {
                ServerProxy.getTaskManager().completeTask(userId, task,
                        p_destinationArrow, null);
            }
            else
            {
                rtnStr = "Can't complete this task as it is not in 'ACCEPTED' state";
            }

        }
        catch (Exception ex)
        {
            String msg = "Fail to complete task : " + p_taskId + " ; "
                    + ex.getMessage();
            logger.error(msg, ex);
            msg = makeErrorXml("completeTask", msg);
            throw new WebServiceException(msg);
        }

        return rtnStr;
    }

    /**
     * Reject specified task.
     * 
     * @param p_accessToken
     *            The access token received from the login.
     * 
     * @param p_taskId
     *            Task Id to be accepted.
     * 
     * @param p_rejectComment
     *            Reject comment.
     * 
     * @throws WebServiceException
     */
    public String rejectTask(String p_accessToken, String p_taskId,
            String p_rejectComment) throws WebServiceException
    {
        String rtnStr = "success";
        checkAccess(p_accessToken, REJECT_TASK);
        checkPermission(p_accessToken,
                Permission.ACTIVITIES_REJECT_AFTER_ACCEPTING);

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertIsInteger(p_taskId);
            Assert.assertNotEmpty(p_rejectComment, "Reject comment");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml(REJECT_TASK, e.getMessage());
            throw new WebServiceException(message);
        }
        // rejector
        String rejectUserName = getUsernameFromSession(p_accessToken);
        String rejectUserId = UserUtil.getUserIdByName(rejectUserName);
        Task task = null;
        try
        {
            WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
                    .getWorkflowTaskInstance(rejectUserId,
                            Long.parseLong(p_taskId),
                            WorkflowConstants.TASK_ALL_STATES);
            task = (Task) HibernateUtil.get(TaskImpl.class,
                    Long.parseLong(p_taskId));
            task.setWorkflowTask(wfTask);

            String rejectComment = EditUtil.utf8ToUnicode(p_rejectComment);
            if (task.getState() == Task.STATE_ACTIVE
                    || task.getState() == Task.STATE_ACCEPTED)
            {
                TaskHelper.rejectTask(rejectUserId, task, rejectComment);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to reject task by taskId : " + p_taskId;
            message = makeErrorXml(REJECT_TASK, message);
            throw new WebServiceException(message);
        }

        return rtnStr;
    }

    /**
     * Adds a comment
     * <p>
     * 
     * @param p_accessToken
     *            - the accessToken received from login()
     * @param p_objectId
     *            - The id of the object (Job or Task) to add the comment too.
     *            The object must be DISPATCHED or part of a DISPATCHED job.
     * @param p_objectType
     *            - The type of the object that p_objectId refers to. 1 = JOB 3
     *            = TASK
     * @param p_userId
     *            - A valid user's user id that is adding the comment.
     * @param p_comment
     *            - A comment to add to the task.
     * @param p_file
     *            - A file which was attached.
     * @param p_fileName
     *            - file name of attached file.
     * @param p_access
     *            - access specification of attachment file Restricted = Only
     *            the Project Manager can view this file. General = All
     *            Participants of the Task can view this file.
     */
    public String addComment(String p_accessToken, long p_objectId,
            int p_objectType, String p_userId, String p_comment, byte[] p_file,
            String p_fileName, String p_access) throws WebServiceException
    {
        checkAccess(p_accessToken, ADD_COMMENT);

        StringBuffer errMessage = new StringBuffer(
                "Could not add the comment to the object.  ");

        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            long projectId = 0l;

            WorkObject object = null;
            if (p_objectType == Comment.JOB)
            {
                object = JobPersistenceAccessor.getJob(p_objectId, true);

                // checkPermission(p_accessToken, Permission.JOB_COMMENTS_NEW);
                projectId = ((Job) object).getProjectId();
                if (!UserUtil.isInProject(userId, String.valueOf(projectId)))
                    throw new WebServiceException(
                            "Current user cannot access the job");
            }
            else if (p_objectType == Comment.TASK)
            {
                object = TaskPersistenceAccessor.getTask(p_objectId, true);
            }
            else if (p_objectType == Comment.WORKFLOW)
            {
                object = WorkflowPersistenceAccessor
                        .getWorkflowById(p_objectId);

                // checkPermission(p_accessToken,
                // Permission.ACTIVITIES_COMMENTS_NEW);
                projectId = ((Workflow) object).getJob().getProjectId();
                if (!UserUtil.isInProject(userId, String.valueOf(projectId)))
                    throw new WebServiceException(
                            "Current user cannot access the job");
            }

            // save out the main part of the comment
            Comment comment = ServerProxy.getCommentManager().saveComment(
                    object,
                    p_objectId,
                    ServerProxy.getUserManager().getUser(p_userId)
                            .getUserName(), p_comment);
            // if there are attachments
            if (p_file != null && p_file.length > 0)
            {
                String access = CommentUpload.GENERAL;
                if (p_access != null
                        && p_access.equals(CommentUpload.RESTRICTED))
                {
                    access = p_access;
                }

                StringBuffer finalPath = new StringBuffer(
                        AmbFileStoragePathUtils.getCommentReferenceDir()
                                .getAbsolutePath());
                finalPath.append(File.separator).append(comment.getId())
                        .append(File.separator).append(access);

                File tempFile = new File(finalPath.toString(), p_fileName);
                tempFile.getParentFile().mkdirs();
                tempFile.createNewFile();
                DataOutputStream out = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(tempFile)));
                out.write(p_file);
                out.close();
            }

            StringBuffer xml = new StringBuffer();
            xml.append("<addCommentStatus>\r\n");
            xml.append("\t<objectId>").append(p_objectId)
                    .append("</objectId>\r\n");
            xml.append("\t<objectType>").append(p_objectType)
                    .append("</objectType>\r\n");
            xml.append("\t<status>successful</status>\r\n");
            xml.append("</addCommentStatus>\r\n");
            return xml.toString();
        }
        catch (GeneralException ge)
        {
            // couldn't find the job specified
            if (ge.getMessageKey().equals(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_ID))
            {
                errMessage.append("Failed to find job " + p_objectId);
            }
            else if (ge.getMessageKey().equals(
                    WorkflowException.MSG_FAILED_TO_GET_WORK_ITEM))
            {
                errMessage.append("Failed to find workflow " + p_objectId);
            }
            else if (ge.getMessageKey().equals(
                    TaskException.MSG_FAILED_TO_GET_TASK))
            {
                errMessage.append("Failed to find task " + p_objectId);
            }

            logger.error(ADD_COMMENT, ge);
            String message = makeErrorXml(ADD_COMMENT, errMessage.toString());
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(ADD_COMMENT, e);
            String message = makeErrorXml(ADD_COMMENT,
                    errMessage.append(e.getMessage()).toString());
            throw new WebServiceException(message);
        }
    }

    /**
     * Adds a comment
     * <p>
     * 
     * @param p_accessToken
     *            - the accessToken received from login()
     * @param p_objectId
     *            - The id of the object (Job or Task) to add the comment too.
     *            The object must be DISPATCHED or part of a DISPATCHED job.
     * @param p_objectType
     *            - The type of the object that p_objectId refers to. 1 = JOB 3
     *            = TASK
     * @param p_userId
     *            - A valid user's user id that is adding the comment.
     * @param p_comment
     *            - A comment to add to the task.
     * @param p_file
     *            - A file which was attached.
     * @param p_fileName
     *            - file name of attached file.
     * @param p_access
     *            - access specification of attachment file Restricted = Only
     *            the Project Manager can view this file. General = All
     *            Participants of the Task can view this file.
     */
    public String addJobComment(String p_accessToken, String p_jobName,
            String p_userId, String p_comment, byte[] p_file,
            String p_fileName, String p_access) throws WebServiceException
    {
        checkAccess(p_accessToken, ADD_COMMENT);
        if (!validateJobName(p_jobName))
        {
            throw new WebServiceException(makeErrorXml("addJobComment",
                    ERROR_JOB_NAME));
        }

        StringBuffer errMessage = new StringBuffer(
                "Could not add the comment to the object.  ");

        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            User user = ServerProxy.getUserManager().getUser(userId);

            String fileName = null;
            File file = null;
            String baseDocDir = AmbFileStoragePathUtils.getCxeDocDir()
                    .getAbsolutePath();
            String commentDir = baseDocDir.concat(File.separator).concat(
                    p_jobName);

            fileName = commentDir.concat(".txt");
            file = new File(fileName);
            FileWriter fout = new FileWriter(file);
            StringBuilder comment = new StringBuilder();
            comment.append(user.getUserName()).append(",")
                    .append(System.currentTimeMillis()).append(",");
            comment.append(p_comment);
            fout.write(comment.toString());
            fout.close();

            // if there are attachments
            if (p_file != null && p_file.length > 0)
            {
                String access = CommentUpload.GENERAL;
                if (p_access != null
                        && p_access.equals(CommentUpload.RESTRICTED))
                {
                    access = p_access;
                }

                file = new File(commentDir, p_fileName);
                file.getParentFile().mkdirs();
                file.createNewFile();
                DataOutputStream out = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(file)));
                out.write(p_file);
                out.close();
            }

            StringBuffer xml = new StringBuffer();
            xml.append("<addCommentStatus>\r\n");
            xml.append("\t<status>successful</status>\r\n");
            xml.append("</addCommentStatus>\r\n");
            return xml.toString();
        }
        catch (GeneralException ge)
        {
            // couldn't find the job specified

            logger.error("addJobComment", ge);
            String message = makeErrorXml("addJobComment",
                    errMessage.toString());
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error("addJobComment", e);
            String message = makeErrorXml("addJobComment",
                    errMessage.append(e.getMessage()).toString());
            throw new WebServiceException(message);
        }
    }

    /**
     * Get an XML report on user's unavailability. The users are determined
     * based on the given activity name, source locale, and target locale. And
     * the range of the report is for the whole specified month (of the given
     * year).
     * 
     * @param p_accessToken
     *            - The access token received from the login.
     * @param p_activityName
     *            - The activity name for which a user can be assigned to
     *            (depending on the role).
     * @param p_sourceLocale
     *            - The source locale specified in a role.
     * @param p_targetLocale
     *            - The target locale specified in a role.
     * @param p_month
     *            - The month for which the report is requested. Note that the
     *            value for the month is based on Java's "zero-based" month
     *            numbering system (so August is month 7 and NOT month 8).
     * @param p_year
     *            - The year for which the report is generated for the given
     *            month.
     * 
     * @return An XML report of user unavailibity for a particular container
     *         role.
     */
    public String getUserUnavailabilityReport(String p_accessToken,
            String p_activityName, String p_sourceLocale,
            String p_targetLocale, int p_month, int p_year)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_USER_UNAVAILABILITY_REPORT);
        checkPermission(p_accessToken, Permission.REPORTS_MAIN);

        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("<userAvailabilityReport>\r\n");
            xml.append("<users>\r\n");

            List userInfos = ServerProxy.getUserManager().getUserInfos(
                    p_activityName, p_sourceLocale, p_targetLocale);

            java.util.Map map = ServerProxy.getCalendarManager()
                    .userUnavailabilityReport(userInfos, p_month, p_year);

            int size = userInfos == null ? -1 : userInfos.size();

            for (int i = 0; i < size; i++)
            {
                UserInfo ui = (UserInfo) userInfos.get(i);
                buildXmlForUserUnavailability(xml, ui, (List) map.get(ui));
            }
            xml.append("</users>\r\n");
            xml.append("</userAvailabilityReport>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_USER_UNAVAILABILITY_REPORT, e);
            String message = "Could not get the user availability report "
                    + " for activity, source locale, and target locale "
                    + p_activityName + ",  " + p_sourceLocale + ",  "
                    + p_targetLocale;
            message = makeErrorXml(GET_USER_UNAVAILABILITY_REPORT, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Pass the DCTM account to GlobalSight side, used to read or write the DCTM
     * server.
     * 
     * @param p_accessToken
     * @param docBase
     * @param dctmUserName
     * @param dctmPassword
     * @return
     * @throws WebServiceException
     */
    public String passDCTMAccount(String p_accessToken, String docBase,
            String dctmUserName, String dctmPassword)
            throws WebServiceException
    {

        checkAccess(p_accessToken, PASS_DCTMACCOUNT);
        try
        {
            logger.info("Starting to save dctm account");
            String userId = DocumentumOperator.getInstance().saveDCTMAccount(
                    docBase, dctmUserName, dctmPassword);
            logger.info("Finish to save dctm account");
            return userId;
        }
        catch (Exception e)
        {
            logger.error(PASS_DCTMACCOUNT, e);
            String message = "Could not save Documentum account "
                    + " for docBase, dctmUserName, dctmPassword" + docBase
                    + ", " + dctmUserName + ", " + dctmPassword;
            message = makeErrorXml(PASS_DCTMACCOUNT, message);
            throw new WebServiceException(message.toString());
        }
    }

    /**
     * Get all of the FileProfile information from GlobalSight side as Xml
     * string.
     * 
     * @param p_accessToken
     * @return String An XML description which contains all file profiles
     * @throws WebServiceException
     */
    public String getFileProfileInfoEx(String p_accessToken)
            throws WebServiceException
    {

        String username = "";
        try
        {
            username = getUsernameFromSession(p_accessToken);
        }
        catch (RuntimeException e1)
        {
            // do nothing
        }
        checkAccess(p_accessToken, GET_FILE_PROFILEINFOEX);
        // checkPermission(p_accessToken, Permission.FILE_PROFILES_VIEW);
        StringBuffer errorMessage = new StringBuffer();
        try
        {
            StringBuffer xmlStr = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            xmlStr.append("<fileProfileInfo>\r\n");

            try
            {
                Collection fileProfiles_all = null;
                Collection fileProfiles_filt = new ArrayList();
                FileProfilePersistenceManager fileProfileManager = ServerProxy
                        .getFileProfilePersistenceManager();
                fileProfiles_all = fileProfileManager.getAllFileProfiles();
                // filter by user
                if (!username.equals(""))
                {
                    User user = getUser(username);
                    List uProjects = null;
                    // *********************** For GBS-390************
                    // get all projects current user belongs to.
                    uProjects = ServerProxy.getProjectHandler()
                            .getProjectsByUser(user.getUserId());

                    for (Iterator ifp = fileProfiles_all.iterator(); ifp
                            .hasNext();)
                    {
                        FileProfile fp = (FileProfile) ifp.next();
                        Project fpProj = getProject(fp);

                        // get the project and check if it is in the group of
                        // user's projects
                        if (uProjects.contains(fpProj))
                        {
                            fileProfiles_filt.add(fp);
                        }
                    }
                    // ***********************************************
                }
                else
                {
                    fileProfiles_filt = fileProfiles_all;
                }

                // Generate the xml string per file profile.
                Iterator iter = fileProfiles_filt.iterator();
                while (iter.hasNext())
                {
                    FileProfile fileProfile = (FileProfile) iter.next();
                    generateFileProfileInfoXml(fileProfile, xmlStr,
                            fileProfileManager);
                }
            }
            catch (Exception ex)
            {
                errorMessage
                        .append("Failed to get all of FileProfile info as a xml String");
                logger.error(
                        "Failed to get all of FileProfile info as a xml String",
                        ex);
            }

            xmlStr.append("</fileProfileInfo>\r\n");
            logger.debug("The xml string for file profile info :"
                    + xmlStr.toString());
            return xmlStr.toString();
        }
        catch (Exception e)
        {
            String message = makeErrorXml(GET_FILE_PROFILEINFOEX,
                    errorMessage.toString());
            throw new WebServiceException(message);
        }
    }

    /**
     * Create a job for Documentum CMS, one Documentum file for one job.
     * 
     * @param p_accessToken
     *            - The access token received from the login.
     * @param jobName
     *            - The Job name.
     * @param fileProfileId
     *            - The id of the file profile to be used.
     * @param objectId
     *            - The Documentum object Id(a dctm file), read this object to
     *            get the translable content.
     * @param userId
     *            - The primary key of a table used to save The DCTM user
     *            account.
     * @throws WebServiceException
     */
    public void createDocumentumJob(String p_accessToken, String jobName,
            String fileProfileId, String objectId, String userId)
            throws WebServiceException
    {

        checkAccess(p_accessToken, CREATE_DTCMJOB);
        if (!validateJobName(jobName))
        {
            throw new WebServiceException(makeErrorXml("createDocumentumJob",
                    ERROR_JOB_NAME));
        }

        StringBuffer errorMessage = new StringBuffer();
        try
        {
            logger.debug("Creating a documentum job (fileProfileId ="
                    + fileProfileId + ",objectId =" + userId + ", userId ="
                    + objectId + ")");
            String dcmtFileName = null;
            String attrFileName = null;

            // Get file name from Documentum via objectId.
            dcmtFileName = DocumentumOperator.getInstance().getObjectName(
                    userId, objectId);
            attrFileName = dcmtFileName + ".attribute";
            if (dcmtFileName == null)
            {
                dcmtFileName = "";
                logger.warn("dcmt name is null");
                // throw new WebServiceException("Can't get the file name via
                // objectId from documentum");
            }

            // Get the dctm file attribute to be translatable content as a xml
            // string.
            String dctmFileAttrXml = DocumentumOperator.getInstance()
                    .generateAttributesXml(userId, objectId);
            if (dctmFileAttrXml == null || dctmFileAttrXml.length() == 0)
            {
                // throw new WebServiceException("Can't get the dctm file
                // attribute as a xml String");
                // create a unique batch ID
                String batchId = jobName
                        + Long.toString(System.currentTimeMillis());
                CxeProxy.importFromDocumentum(objectId, dcmtFileName, jobName,
                        batchId, fileProfileId, new Integer(1), new Integer(1),
                        new Integer(1), new Integer(1), false, null, userId);
                logger.info("Trying to import a documentum file");
            }
            else
            {
                logger.debug("The dctm file attribute xml String :"
                        + dctmFileAttrXml);

                // One job includes two files(documentum file, xml attribute
                // file),
                // so hard code here.
                Integer pageCount = new Integer(2);
                // create a unique batch ID
                String batchId = jobName
                        + Long.toString(System.currentTimeMillis());
                // Get the fileprofile id used to translate xml attribute file.
                String xmlFPId = getXmlFileProfile(fileProfileId);
                if (xmlFPId == null)
                {
                    errorMessage.append("Can't get a xml Fileprofile");
                    throw new WebServiceException("Can't get a xml Fileprofile");
                }

                CxeProxy.importFromDocumentum(objectId, dcmtFileName, jobName,
                        batchId, fileProfileId, pageCount, Integer.valueOf(1),
                        Integer.valueOf(1), Integer.valueOf(1), false, null,
                        userId);
                logger.info("Trying to import a documentum file");

                CxeProxy.importFromDocumentum(objectId, attrFileName, jobName,
                        batchId, xmlFPId, pageCount, Integer.valueOf(2),
                        Integer.valueOf(1), Integer.valueOf(1), true,
                        dctmFileAttrXml, userId);
                logger.info("Trying to import a documentum attribute file");

            }
        }
        catch (Exception ex)
        {
            logger.error("Failed to create a documentum job", ex);
            errorMessage.append(" Failed to create a documentum job");
            String message = makeErrorXml(CREATE_DTCMJOB,
                    errorMessage.toString());
            throw new WebServiceException(message);
        }

    }

    /**
     * Cancel the Documentum job using objectId and jobId.
     * 
     * @param p_accessToken
     *            - The access token received from the login.
     * @param objectId
     *            - The DCTM document object id.
     * @param jobId
     *            - The GlobalSight job id.
     * @param userId
     *            - The primary key of a table used to save The DCTM user
     *            account.
     * @throws WebServiceException
     */
    public void cancelDocumentumJob(String p_accessToken, String objectId,
            String jobId, String userId) throws WebServiceException
    {

        checkAccess(p_accessToken, CANCEL_DCTMJOB);
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String uid = UserUtil.getUserIdByName(userName);
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long.valueOf(jobId).longValue());

            logger.info("Cancelling all workflows for job " + jobId);
            ServerProxy.getJobHandler().cancelJob(uid, job, null);

            DocumentumOperator.getInstance().cleanCustomAttrs(userId, objectId);
        }
        catch (JobException je)
        {
            StringBuffer messageBuf = new StringBuffer(
                    "Unable to cancel the job ");
            messageBuf.append(jobId);

            // couldn't find the user specified
            if (je.getMessageKey().equals(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_ID))
            {
                messageBuf.append(" The job couldn't be found.");
            }
            String message = messageBuf.toString();
            logger.error(message, je);
            message = makeErrorXml(CANCEL_JOB_BY_ID, message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(CANCEL_JOB_BY_ID, e);
            String message = "Could not cancel job " + jobId;
            message = makeErrorXml(CANCEL_JOB_BY_ID, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * check if some jobs is downloadable or delete them from backup file in
     * client
     * 
     * @param p_accessToken
     * @param p_message
     * @return xml String result <jobs> <job> <name></name> <status>downloadable
     *         | create_error | unknown</status> </job> </jobs>
     * @throws WebServiceException
     */
    public String getDownloadableJobs(String p_accessToken, String p_msg)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_DOWNLOADABLE_JOBS);
        // checkPermission(p_accessToken, Permission.JOBS_DOWNLOAD);

        p_msg = StringUtil.replace(p_msg, "&", "&amp;");
        XmlParser parser = new XmlParser();
        Document doc = null;
        try
        {
            doc = parser.parseXml(p_msg);
        }
        catch (Exception e)
        {
            throw new WebServiceException(
                    makeErrorXml(
                            "getDownloadableJobs",
                            "Invalid xml content in parameter p_msg. "
                                    + e.getMessage()));
        }

        try
        {

            Element root = doc.getRootElement();
            List jobList = root.elements();
            if (jobList.size() > 0)
            {
                File diExportedDir = AmbFileStoragePathUtils
                        .getDesktopIconExportedDir();
                File[] files = diExportedDir.listFiles();
                StringBuffer fileNamesSB = new StringBuffer();
                for (int i = 0; i < files.length; i++)
                {
                    fileNamesSB.append(files[i].getName()).append("/");
                }
                String fileNames = fileNamesSB.toString();
                Job job = null;
                String status = "unknown";
                
                for (Iterator iter = jobList.iterator(); iter.hasNext();)
                {
                    Element jobElement = (Element) iter.next();
                    String jobName = jobElement.element("name").getText();
                    job = ServerProxy.getJobHandler().getJobByJobName(jobName);
                    if (job != null && fileNames.indexOf(String.valueOf(job.getJobId())) != -1)
                        status = "downloadable";
                    jobElement.element("status").setText(status);
                }
            }

            return doc.asXML();
        }
        catch (Exception e)
        {
            logger.error(GET_DOWNLOADABLE_JOBS, e);
            String message = e.getMessage();
            message = makeErrorXml(GET_DOWNLOADABLE_JOBS, message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Get the version of WebService
     * 
     * @deprecated Abandoned from 8.2.1. Use new method getGSVersion() instead.
     */
    public String getVersion(String p_accessToken) throws WebServiceException
    {
        try
        {
            return VERSION;
        }
        catch (Exception e)
        {
            logger.error(GET_VERSION, e);
            throw new WebServiceException("Fail to return version to client");
        }
    }

    /**
     * Gets the version for checking by desktop icon. Use this method for the
     * version check from 8.2.1.
     */
    public String getGSVersion(String p_accessToken) throws WebServiceException
    {
        try
        {
            return VERSION_NEW;
        }
        catch (Exception e)
        {
            logger.error(GET_VERSION, e);
            throw new WebServiceException("Failed to return version to client");
        }
    }

    // //////////////////
    // Private Methods
    // //////////////////

    /**
     * Generate xml string per a File Profile.
     * 
     * @param fileProfile
     *            Entity of file profile
     * @param xmlStr
     *            XML description containing file profile information
     * @param fileProfileManager
     *            Manage class for file profile
     */
    private void generateFileProfileInfoXml(FileProfile fileProfile,
            StringBuffer xmlStr,
            FileProfilePersistenceManager fileProfileManager)
    {

        // Add basic information for a specified file profile.
        xmlStr.append("\t<fileProfile>\r\n");
        xmlStr.append("\t\t<id>").append(fileProfile.getId())
                .append("</id>\r\n");
        xmlStr.append("\t\t<name>").append(fileProfile.getName())
                .append("</name>\r\n");
        xmlStr.append("\t\t<l10nprofile>")
                .append(fileProfile.getL10nProfileId())
                .append("</l10nprofile>\r\n");
        xmlStr.append("\t\t<sourceFileFormat>")
                .append(fileProfile.getKnownFormatTypeId())
                .append("</sourceFileFormat>\r\n");
        xmlStr.append("\t\t<description>");
        if (fileProfile.getDescription() == null)
        {
            xmlStr.append("N/A").append("</description>\r\n");
        }
        else
        {
            xmlStr.append(fileProfile.getDescription()).append(
                    "</description>\r\n");
        }

        // Add file extensions information for a specified file profile.
        try
        {
            Vector fileExtensionIds = fileProfile.getFileExtensionIds();
            xmlStr.append("\t\t<fileExtensionInfo>\r\n");
            for (int i = 0; i < fileExtensionIds.size(); i++)
            {
                Long fileExtensionId = (Long) fileExtensionIds.get(i);
                FileExtension fileExtension = fileProfileManager
                        .readFileExtension(fileExtensionId.longValue());
                xmlStr.append("\t\t\t<fileExtension>")
                        .append(fileExtension.getName())
                        .append("</fileExtension>\r\n");
            }
            xmlStr.append("\t\t</fileExtensionInfo>\r\n");

            // Add locales information for a specified file profile.
            long l10nProfileId = fileProfile.getL10nProfileId();
            ProjectHandler projectHandler = ServerProxy.getProjectHandler();
            L10nProfile l10nProfile = projectHandler
                    .getL10nProfile(l10nProfileId);
            GlobalSightLocale sourceLocale = l10nProfile.getSourceLocale();
            GlobalSightLocale[] targetLocales = l10nProfile.getTargetLocales();
            xmlStr.append("\t\t<localeInfo>\r\n");
            xmlStr.append("\t\t\t<sourceLocale>")
                    .append(sourceLocale.toString())
                    .append("</sourceLocale>\r\n");
            for (int i = 0; i < targetLocales.length; i++)
            {
                GlobalSightLocale targetLocale = targetLocales[i];
                xmlStr.append("\t\t\t<targetLocale>")
                        .append(targetLocale.toString())
                        .append("</targetLocale>\r\n");
            }
            xmlStr.append("\t\t</localeInfo>\r\n");
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
        }

        xmlStr.append("\t</fileProfile>\r\n");
    }

    /**
     * Get a xml FileProfile with the same localization profile as a given
     * FileProfile.
     * 
     * This xml fileprofile, including xml extension, is used to translate a
     * documentum file attributes.
     * 
     * @param fpId
     *            - A given FileProfile.
     * @return String - a xml FileProfile Id.
     */
    private String getXmlFileProfile(String fpId)
    {

        FileProfile xmlFileProfile = null;
        try
        {
            FileProfilePersistenceManager fpManager = ServerProxy
                    .getFileProfilePersistenceManager();
            FileProfile oriFileProfile = fpManager.getFileProfileById(Long
                    .valueOf(fpId).longValue(), false);
            long l10nProfileId = oriFileProfile.getL10nProfileId();

            // Try to find a xml file profile with the same l10nProfile Id, and
            // xml format.
            Collection fileProfiles = fpManager.getAllFileProfiles();
            Iterator iter = fileProfiles.iterator();
            while (iter.hasNext())
            {
                FileProfile fp = (FileProfile) iter.next();
                if (fp.getL10nProfileId() == l10nProfileId
                        && fp.getKnownFormatTypeId() == 7)
                {
                    xmlFileProfile = fp;
                    break;
                }
            }

            // Can't find any file profile on condition, create a new one on
            // request.
            if (xmlFileProfile == null)
            {
                String fpName = "DCMT_XML_FP_"
                        + String.valueOf(System.currentTimeMillis());
                xmlFileProfile = new FileProfileImpl(oriFileProfile);
                Vector fileExts = new Vector();
                // Add *.xml file extension.
                fileExts.add(Long.valueOf(14));
                xmlFileProfile.setFileExtensionIds(fileExts);
                xmlFileProfile.setName(fpName);
                xmlFileProfile.setXmlRuleFileId(0);
                xmlFileProfile.setKnownFormatTypeId(7);
                fpManager.createFileProfile(xmlFileProfile);
            }

            logger.debug("Using a xml fileprofile, id="
                    + xmlFileProfile.getId());
            return String.valueOf(xmlFileProfile.getId());
        }
        catch (Exception ex)
        {
            logger.error("Failed to get xml file profile", ex);
            return null;
        }
    }

    /**
     * Append the date range for an event by updating the format based on the
     * following criteria: 1. One day events such as holidays would be displayed
     * as one single date (i.e. 4/21/05) 2. Recurring times for some event such
     * as a daily meeting (4/2/05 - 4/5/05 (12:00:00 AM PST - 1:00:00 AM PDT) 3.
     * All dates that are non-recurring (i.e. 4/21/05 1:00:00 PM PDT - 4/21/05
     * 2:00:00 PM PDT)
     * 
     * @param p_xmlStringBuffer
     *            - The generated XML string buffer.
     * @param p_reservedTime
     *            - The event object.
     */
    private void appendEventDate(StringBuffer p_xmlStringBuffer,
            ReservedTime p_reservedTime)
    {
        // reset the locale and time zone to server's default one.
        Timestamp start = p_reservedTime.getStartTimestamp();
        start.setLocale(null);
        start.setTimeZone(null);
        Timestamp end = p_reservedTime.getEndTimestamp();
        end.setLocale(null);
        end.setTimeZone(null);

        p_xmlStringBuffer.append("\t\t\t<date>");
        boolean sameDay = start.isSameDay(end);
        boolean zeroHour = start.getHour() == 0 && end.getHour() == 0;

        // if it's just a one day event
        if (sameDay && zeroHour)
        {
            p_xmlStringBuffer.append(start);
        }
        // for events such as a daily meeting from 10 to 12.
        else if (!sameDay && start.getHour() != end.getHour()
                && p_reservedTime.getTaskId() == null)
        {
            String startDateAsString = start.toString();
            String endDateAsString = end.toString();

            p_xmlStringBuffer.append(startDateAsString.substring(0,
                    startDateAsString.indexOf(" ")));
            p_xmlStringBuffer.append(" - ");
            p_xmlStringBuffer.append(endDateAsString.substring(0,
                    endDateAsString.indexOf(" ")));
            p_xmlStringBuffer.append(" (");
            p_xmlStringBuffer.append(startDateAsString.substring(
                    startDateAsString.indexOf(" ") + 1,
                    startDateAsString.length()));
            p_xmlStringBuffer.append(" -");
            p_xmlStringBuffer.append(endDateAsString.substring(
                    endDateAsString.indexOf(" "), endDateAsString.length()));
            p_xmlStringBuffer.append(")");
        }
        else
        {
            p_xmlStringBuffer.append(start);
            p_xmlStringBuffer.append(" - ");
            p_xmlStringBuffer.append(end);
        }

        p_xmlStringBuffer.append("</date>\r\n");
    }

    /**
     * Gets the user info XML for the given user
     * 
     * @param p_userId
     *            User's ID
     * @return UserInfo Entity of UserInfo
     * @exception WebServiceException
     */
    private UserInfo getUserInfo(String p_userId) throws WebServiceException
    {
        UserInfo ui = null;
        try
        {
            ui = ServerProxy.getUserManager().getUserInfo(p_userId);
        }
        catch (UserManagerException ume)
        {
            StringBuffer messageBuf = new StringBuffer(
                    "Unable to get information for user ");
            messageBuf.append(p_userId);

            // couldn't find the user specified
            if (ume.getMessageKey().equals(
                    UserManagerException.MSG_GET_USER_ERROR))
            {
                messageBuf.append(".  The user couldn't be found.");
            }
            String message = messageBuf.toString();
            logger.error(message, ume);
            message = makeErrorXml(GET_USER_INFO, message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            String message = "Unable to get information for user " + p_userId;
            logger.error(message, e);
            message = makeErrorXml(GET_USER_INFO, message);
            throw new WebServiceException(message);
        }
        return ui;
    }

    /**
     * Exports all target pages of the given workflow
     * 
     * @param p_job
     *            Entity of Job
     * @param p_workflow
     *            Entity of Workflow
     * @param p_user
     *            Specified user information
     * @throws Exception
     */
    private void exportSingleWorkflow(Job p_job, Workflow p_workflow,
            User p_user) throws Exception
    {
        List targetPages = p_workflow.getTargetPages();
        ArrayList pageIds = new ArrayList();
        for (int j = 0; j < targetPages.size(); j++)
        {
            TargetPage tp = (TargetPage) targetPages.get(j);
            pageIds.add(tp.getIdAsLong());
        }
        ExportParameters ep = new ExportParameters(p_workflow);
        boolean isTargetPage = true;
        ArrayList wfIds = new ArrayList();
        wfIds.add(p_workflow.getIdAsLong());
        Long taskId = null;

        logger.info("Exporting workflow  "
                + p_workflow.getTargetLocale().toString() + " for job "
                + p_job.getJobName());
        long exportBatchId = ServerProxy.getExportEventObserver()
                .notifyBeginExportTargetBatch(p_job, p_user, pageIds, wfIds,
                        taskId, ExportBatchEvent.INTERIM_PRIMARY);
        ServerProxy.getPageManager().exportPage(ep, pageIds, isTargetPage,
                exportBatchId);
    }

    /**
     * Queries the database for the latest file profile information. Fills the
     * two array lists with the ids and (name)descriptions
     * 
     * @param p_fileProfileIds
     *            Array of file profile IDs
     * @param p_fileProfileDescriptions
     *            Array of descriptions for file profile
     * @param p_fileProfileNames
     *            Array of names for file profile
     * @throws WebServiceException
     */
    private void queryDatabaseForFileProfileInformation(
            ArrayList p_fileProfileIds, ArrayList p_fileProfileDescriptions,
            ArrayList p_fileProfileNames) throws WebServiceException
    {
        Iterator fileProfileIter = null;
        try
        {
            Collection results = ServerProxy.getFileProfilePersistenceManager()
                    .getAllFileProfiles();
            fileProfileIter = results.iterator();
        }
        catch (Exception e)
        {
            String message = "Unable to get file profiles from db.";
            logger.error(message, e);
            message = makeErrorXml("queryDatabaseForFileProfileInformation",
                    message);
            throw new WebServiceException(message);
        }
        FileProfile fileProfile = null;
        while (fileProfileIter.hasNext())
        {
            fileProfile = (FileProfile) fileProfileIter.next();
            p_fileProfileIds.add(Long.toString(fileProfile.getId()));

            p_fileProfileNames.add(fileProfile.getName());
            String desc = fileProfile.getDescription();
            if (desc == null || desc.length() < 1)
            {
                desc = "N/A";
            }
            p_fileProfileDescriptions.add(desc);
        }
    }

    /**
     * Gets the path of the file in service.
     * 
     * @param jobId
     *            The name of job
     * @param filePath
     *            File path
     * @param srcLocale
     *            Source locale
     * @return
     */
    private String getRealPath(String jobId, String filePath, String srcLocale)
    {
        String newPath = new StringBuffer(srcLocale).append(File.separator)
                .append("webservice").append(File.separator).append(jobId)
                .append(File.separator).append(filePath).toString();

        return newPath;
    }

    /**
     * Writes the content to the file(according to path).
     * 
     * <p>
     * Notice that if the file has been exist and has some content, the new
     * conten in <code>bytes</code> will be writed and the end. <br>
     * For example, call the method like the follows:<br>
     * <code>
     *     writeFile("c:/a.txt", "a".getBytes());<br>
     *     writeFile("c:/a.txt", "b".getBytes());<br>
     * </code> There will be a file "c:/a.txt", and the content is "ab".<br>
     * 
     * @param path
     * @param bytes
     * @throws WebServiceException
     */
    private void writeFile(String path, byte[] bytes, long companyId)
            throws WebServiceException
    {
        File newFile = new File(
                AmbFileStoragePathUtils.getCxeDocDir(companyId), path);
        newFile.getParentFile().mkdirs();
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(newFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
            logger.error("Could not copy uploaded file to the docs directory.",
                    e);
            String message = "Could not copy uploaded file to the docs directory."
                    + e.getMessage();
            message = makeErrorXml("copyFileToDocsDirectory", message);
            throw new WebServiceException(message);
        }
        finally
        {
            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (IOException e)
            {
                logger.error(
                        "Could not copy uploaded file to the docs directory.",
                        e);
                String message = "Could not copy uploaded file to the docs directory."
                        + e.getMessage();
                message = makeErrorXml("copyFileToDocsDirectory", message);
                throw new WebServiceException(message);
            }
        }
    }

    /**
     * Gets out the Job object corresponding to the job name assuming that is
     * unique
     * 
     * @param p_jobName
     *            Job name
     * @param p_accessToken
     * @return Job Return job object if there exist.
     * @exception WebServiceException
     */
    private Job queryJob(String p_jobName, String p_accessToken)
            throws WebServiceException
    {
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            User user = getUser(getUsernameFromSession(p_accessToken));
            long id = 0;
            String sql = "SELECT ID FROM JOB WHERE NAME=? AND COMPANY_ID=?";
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setString(1, p_jobName);
            query.setLong(2,
                    CompanyWrapper.getCompanyByName(user.getCompanyName())
                            .getId());
            results = query.executeQuery();
            if (results.next())
            {
                id = results.getLong(1);
                Job job = ServerProxy.getJobHandler().getJobById(id);
                return job;
            }
            else
            {
                String message = NOT_IN_DB + p_jobName;
                message = makeErrorXml("queryJob", message);
                /*
                 * Do not change this Exception message "This job is not ready
                 * for query", because getStatus() will deal with it in
                 * catch(Exception e) and Desktop Icon
                 * 
                 * com/globalsight/action/AddCommentAction.java :
                 * executeWithThread(String args[])
                 */
                throw new WebServiceException(message);
            }
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database to get job status.";
            logger.error(message, cpe);
            message = makeErrorXml("queryJob", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for job status.";
            logger.error(message, sqle);
            message = makeErrorXml("queryJob", message);
            throw new WebServiceException(message);
        }
        catch (WebServiceException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            String message = "Unable to get job information from System4.";
            logger.error(message, e);
            message = makeErrorXml("queryJob", message);
            throw new WebServiceException(message);
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }
    }

    /**
     * Gets CXE to import the given file
     * 
     * @param p_jobName
     * @param p_batchId
     * @param p_pageNum
     * @param p_pageCount
     * @param p_docPageNum
     * @param p_docPageCount
     * @param p_fileName
     * @param p_fileProfileId
     * @param p_importInitiatorId
     * @param p_targetLocales
     * @param p_exitValueByScript
     * @param p_priority
     * @param p_originalTaskId
     * @param p_originalEndpoint
     * @param p_originalUserName
     * @param p_originalPassword
     * @param p_jobComments
     * @param p_segComments
     * @throws Exception
     */
    private void publishEventToCxe(String p_jobName, String p_batchId,
            int p_pageNum, int p_pageCount, int p_docPageNum,
            int p_docPageCount, String p_fileName, String p_fileProfileId,
            String p_importInitiatorId, String p_targetLocales,
            Integer p_exitValueByScript, String p_priority,
            String p_originalTaskId, String p_originalEndpoint,
            String p_originalUserName, String p_originalPassword,
            Vector p_jobComments, HashMap p_segComments) throws Exception
    {
        String key = p_batchId + p_fileName + p_pageNum;
        CxeProxy.setTargetLocales(key, p_targetLocales);
        logger.info("Publishing import request to CXE for file " + p_fileName);

        CxeProxy.importFromFileSystem(p_fileName, p_jobName, p_batchId,
                p_fileProfileId, Integer.valueOf(p_pageCount),
                Integer.valueOf(p_pageNum), Integer.valueOf(p_docPageCount),
                Integer.valueOf(p_docPageNum), Boolean.TRUE,
                CxeProxy.IMPORT_TYPE_L10N, p_importInitiatorId,
                p_exitValueByScript, p_priority, p_originalTaskId,
                p_originalEndpoint, p_originalUserName, p_originalPassword,
                p_jobComments, p_segComments);
    }

    /**
     * Gets CXE to import the given file
     * 
     * @param p_jobName
     * @param jobUuid
     * @param p_batchId
     * @param p_pageNum
     * @param p_pageCount
     * @param p_docPageNum
     * @param p_docPageCount
     * @param p_fileName
     * @param p_fileProfileId
     * @param p_importInitiatorId
     * @param p_targetLocales
     * @param p_exitValueByScript
     * @param p_priority
     * @throws Exception
     */
    private void publishEventToCxe(String p_jobName, String jobUuid,
            String p_batchId, int p_pageNum, int p_pageCount, int p_docPageNum,
            int p_docPageCount, String p_fileName, String p_fileProfileId,
            String p_importInitiatorId, String p_targetLocales,
            Integer p_exitValueByScript, String p_priority) throws Exception
    {
        String key = p_batchId + p_fileName + p_pageNum;
        CxeProxy.setTargetLocales(key, p_targetLocales);
        logger.info("Publishing import request to CXE for file " + p_fileName);
        CxeProxy.importFromFileSystem(p_fileName, p_jobName, jobUuid,
                p_batchId, p_fileProfileId, Integer.valueOf(p_pageCount),
                Integer.valueOf(p_pageNum), Integer.valueOf(p_docPageCount),
                Integer.valueOf(p_docPageNum), Boolean.TRUE,
                CxeProxy.IMPORT_TYPE_L10N, p_importInitiatorId,
                p_exitValueByScript, p_priority);
    }

    /**
     * Publishes to CXE to create a job.
     */
    private void publishEventToCxe(String p_jobId, String p_batchId,
            int p_pageNum, int p_pageCount, int p_docPageNum,
            int p_docPageCount, String p_fileName, String p_fileProfileId,
            String p_targetLocales, Integer p_exitValueByScript,
            String p_priority) throws Exception
    {
        String key = p_batchId + p_fileName + p_pageNum;
        CxeProxy.setTargetLocales(key, p_targetLocales);
        logger.info("Publishing import request to CXE for file " + p_fileName);
        CxeProxy.importFromFileSystem(p_fileName, p_jobId, p_batchId,
                p_fileProfileId, Integer.valueOf(p_pageCount),
                Integer.valueOf(p_pageNum), Integer.valueOf(p_docPageCount),
                Integer.valueOf(p_docPageNum), Boolean.TRUE, Boolean.FALSE,
                CxeProxy.IMPORT_TYPE_L10N, p_exitValueByScript, p_priority);
    }

    /**
     * Takes the given file name relative to the docs directory and replaces the
     * leading locale specific directory with the locale corresponding to the
     * language and country codes.
     * 
     * @param p_fileName
     * @param p_exportSubDir
     * @param p_locale
     * @return
     */
    private String replaceLocaleInFileName(String p_fileName,
            String p_exportSubDir, String p_locale)
    {
        int index = p_fileName.indexOf('/');
        if (index == -1)
            index = p_fileName.indexOf('\\');

        String targetFileName = p_exportSubDir + p_fileName.substring(index);

        return ExportHelper.transformExportedFilename(targetFileName, p_locale);
    }

    /**
     * Build the XML and populate it with basic info of a task.
     * 
     * @param xml
     *            XML description
     * @param t
     *            Task object
     * @throws WebServiceException
     */
    private void buildXmlForTask(StringBuffer xml, Task t, String tab,
            Connection connection) throws WebServiceException
    {
        xml.append(tab).append("<task>\r\n");
        xml.append(tab).append("\t<id>").append(t.getId()).append("</id>\r\n");
        xml.append(tab).append("\t<workflowId>")
                .append(t.getWorkflow().getId()).append("</workflowId>\r\n");
        xml.append(tab).append("\t<name>").append(t.getTaskName())
                .append("</name>\r\n");
        xml.append(tab).append("\t<state>").append(t.getStateAsString())
                .append("</state>\r\n");
        xml.append(tab).append("\t<type>").append(t.getTaskType())
                .append("</type>\r\n");

        if (t.getAcceptor() != null && t.getAcceptor().length() > 0)
        {
            xml.append(tab).append("\t<accepter>\r\n");
            // get user information about the user who accepted the task
            UserInfo ui = getUserInfo(t.getAcceptor());
            xml.append(tab).append("\t\t<userid>").append(ui.getUserName())
                    .append("</userid>\r\n");
            xml.append(tab).append("\t\t<firstName>").append(ui.getFirstName())
                    .append("</firstName>\r\n");
            xml.append(tab).append("\t\t<lastName>").append(ui.getLastName())
                    .append("</lastName>\r\n");
            if (ui.getTitle() != null && ui.getTitle().length() > 0
                    && !"null".equalsIgnoreCase(ui.getTitle()))
            {
                xml.append(tab).append("\t\t<title>").append(ui.getTitle())
                        .append("</title>\r\n");
            }
            xml.append(tab).append("\t\t<email>").append(ui.getEmailAddress())
                    .append("</email>\r\n");
            xml.append(tab).append("\t</accepter>\r\n");
        }
        else
        // not accepted yet
        {
            xml.append(tab).append("\t<assignees>")
                    .append(t.getPossibleAssignee()).append("</assignees>\r\n");
        }

        xml.append(tab).append("\t<estimatedAcceptanceDate>")
                .append(convertDateToString(t.getEstimatedAcceptanceDate()))
                .append("</estimatedAcceptanceDate>\r\n");

        xml.append(tab).append("\t<acceptedDate>")
                .append(convertDateToString(t.getAcceptedDate()))
                .append("</acceptedDate>\r\n");

        xml.append(tab).append("\t<estimatedCompletionDate>")
                .append(convertDateToString(t.getEstimatedCompletionDate()))
                .append("</estimatedCompletionDate>\r\n");

        xml.append(tab).append("\t<completedDate>")
                .append(convertDateToString(t.getCompletedDate()))
                .append("</completedDate>\r\n");

        String availableDate = getTaskAvailableDate(t, connection);
        xml.append(tab).append("\t<availableDate>").append(availableDate)
                .append("</availableDate>\r\n");

        xml.append(tab).append("</task>\r\n");
    }

    private String getTaskAvailableDate(Task t, Connection connection)
    {
        String availableDate = "null";
        if (t == null)
            return availableDate;

        long workflowId = t.getWorkflow().getId();
        long taskId = t.getId();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            if (connection == null)
                connection = ConnectionPool.getConnection();

            StringBuilder sql = new StringBuilder();
            java.sql.Timestamp tmp = null;
            Date acceptedDate = t.getAcceptedDate();
            sql.append("SELECT MAX(Completed_Date) FROM Task_Info");
            sql.append(" WHERE Workflow_ID=? AND State=?");
            if (acceptedDate != null)
            {
                sql.append(" AND Accepted_Date<?");
            }
            pstmt = connection.prepareStatement(sql.toString());
            pstmt.setLong(1, workflowId);
            pstmt.setString(2, Task.COMPLETED);
            if (acceptedDate != null)
                pstmt.setTimestamp(3,
                        new java.sql.Timestamp(acceptedDate.getTime()));

            rs = pstmt.executeQuery();

            if (rs.next())
            {
                tmp = rs.getTimestamp(1);
                if (tmp != null)
                    availableDate = convertDateToString(new Date(tmp.getTime()));
                else
                    availableDate = convertDateToString(t.getWorkflow()
                            .getDispatchedDate());
            }
        }
        catch (Exception e)
        {
            logger.error("Error found.", e);
        }
        finally
        {
            ConnectionPool.silentClose(pstmt);
        }

        return availableDate;
    }

    /**
     * Build the user unavailability XML and populate it with all the events for
     * each user.
     * 
     * @param xml
     *            XML description
     * @param p_userInfo
     *            User's information
     * @param p_reservedTimes
     *            List of events
     * @throws WebServiceException
     */
    private void buildXmlForUserUnavailability(StringBuffer xml,
            UserInfo p_userInfo, List p_reservedTimes)
            throws WebServiceException
    {
        xml.append("\t<user>\r\n");
        xml.append("\t\t<fullName>").append(p_userInfo.getFullName())
                .append("</fullName>\r\n");
        xml.append("\t\t<username>").append(p_userInfo.getUserName())
                .append("</username>\r\n");
        xml.append("\t\t<events>\r\n");

        int size = p_reservedTimes == null ? -1 : p_reservedTimes.size();
        for (int i = 0; i < size; i++)
        {
            ReservedTime rt = (ReservedTime) p_reservedTimes.get(i);
            xml.append("\t\t\t<event>\r\n");

            xml.append("\t\t\t<name>").append(rt.getSubject())
                    .append("</name>\r\n");
            xml.append("\t\t\t<type>").append(rt.getType())
                    .append("</type>\r\n");

            appendEventDate(xml, rt);

            xml.append("\t\t\t</event>\r\n");
        }
        xml.append("\t\t</events>\r\n");
        xml.append("\t</user>\r\n");
    }

    /**
     * Get the display date and time for the given date object. Note that
     * system's default time zone and locale will be used.
     * 
     * @param p_date
     *            - The date and time to be displayed.
     * @return String Formatted date
     */
    private String convertDateToString(Date p_date)
    {
        if (p_date == null)
        {
            return "";
        }

        return DateHelper.getFormattedDateAndTime(p_date);
    }

    /**
     * Determines the URL prefix to stick in front of files relative to the docs
     * directory so that they can be read via the web.
     * 
     * @param p_companyName
     *            Company name
     * @return String URL prefix for specified company
     */
    private String determineUrlPrefix(String p_companyName)
    {
        StringBuffer urlPrefix = new StringBuffer();
        urlPrefix.append(capLoginUrl);
        urlPrefix.append("/cxedocs/");
        urlPrefix.append(URLEncoder.encode(p_companyName));
        return urlPrefix.toString();
    }

    /**
     * Looks up the src locale of the l10nprofile corresponding to this file
     * profile
     * 
     * @param p_fileProfileId
     *            -- the file profile ID
     * @return String -- the source locale abbreviation
     * @exception WebServiceException
     */
    private String findSrcLocale(String p_fileProfileId)
            throws WebServiceException
    {
        String errorMsg = null;
        String sourceLocale = null;

        try
        {
            long fpid = 0;
            try
            {
                fpid = Long.parseLong(p_fileProfileId);
            }
            catch (Exception e)
            {
                errorMsg = "The parameter fileProfileId is not numeric: "
                        + p_fileProfileId;
                throw new Exception(errorMsg, e);
            }

            FileProfile fp = null;
            try
            {
                if (fpid != 0)
                {
                    FileProfilePersistenceManager fppm = ServerProxy
                            .getFileProfilePersistenceManager();
                    fp = fppm.readFileProfile(fpid);
                }
                if (fp == null)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                errorMsg = "Fail to get FileProfile by fileProfileId: " + fpid;
                throw new Exception(errorMsg, e);
            }

            long lpid = 0;
            try
            {
                if (fp != null)
                {
                    lpid = fp.getL10nProfileId();
                    ProjectHandler ph = ServerProxy.getProjectHandler();
                    L10nProfile lp = ph.getL10nProfile(lpid);
                    sourceLocale = lp.getSourceLocale().toString();
                }
            }
            catch (Exception e)
            {
                errorMsg = "Fail to get source locale by l10nProfile Id: "
                        + lpid;
                throw new Exception(errorMsg, e);
            }

            return sourceLocale;
        }
        catch (Exception e)
        {
            String message = makeErrorXml("findSrcLocale", e.getMessage());
            throw new WebServiceException(message);
        }
    }

    /**
     * This checks whether the web service is installed. The websvc.installKey
     * system parameter value is checked against the expected value. If not
     * installed, then an exception is thrown
     * 
     * @exception WebServiceException
     * @wlws:exclude
     */
    protected void checkIfInstalled() throws WebServiceException
    {
        if (!isWebServiceInstalled)
            throw new WebServiceException("Web services is not installed.");
    }

    /**
     * Get the project that the file profile is associated with.
     * 
     * @param p_fp
     *            File profile information
     * @return Project Project information which is associated with specified
     *         file profile
     */
    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to get the project that file profile "
                            + p_fp.toString() + " is associated with.", e);
            // just leave and return NULL
        }
        return p;
    }

    /**
     * Sends email to user after completing the process of uploading
     * 
     * @param p_accessToken
     * @param p_comment
     *            Job comment
     * @param date
     *            Date
     * @param jobName
     *            Job name
     * @param pageParas
     *            Parameters of current page
     * @param fileProfileId
     *            ID of file profile
     * @param relativeFileName
     *            File name
     */
    private void sendUploadCompletedEmail(String p_accessToken,
            String p_comment, Date date, String jobName, int[] pageParas,
            String fileProfileId, String relativeFileName)
    {
        String key_files = jobName + "_files";
        String key_fileprofiles = jobName + "_fps";

        List files = (List) dataStoreForFilesInSendingEmail.get(key_files);
        if (files == null)
        {
            files = new ArrayList();
            dataStoreForFilesInSendingEmail.put(key_files, files);
        }
        files.add(relativeFileName);

        List fileprofileIds = (List) dataStoreForFilesInSendingEmail
                .get(key_fileprofiles);
        if (fileprofileIds == null)
        {
            fileprofileIds = new ArrayList();
            dataStoreForFilesInSendingEmail.put(key_fileprofiles,
                    fileprofileIds);
        }
        fileprofileIds.add(fileProfileId);

        // if all the files has been upload successfully, then send mail
        if (pageParas[0] == pageParas[1])
        {
            sendUploadCompletedEmail(files, fileprofileIds, p_accessToken,
                    jobName, p_comment, date);
        }
    }

    /**
     * Please invoke sendUploadCompletedEmail(String p_accessToken, String
     * p_comment, Date date, String jobName, int[] pageParas, String
     * fileProfileId, String relativeFileName) to send mail, not this method.
     * Notify the uploader and customer's default PM about the new upload. #{0}
     * The upload date and time #{1} The upload name (will become job name) #{2}
     * The description used for the job. #{3} The project to be used for import
     * (label is either division or project) #{4} The name of the person who
     * uploaded the files #{5} The files and file profiles be uploaded #{6} The
     * name and email of Recipient.
     * 
     * @param p_fileNames
     *            List of file names
     * @param p_fpIds
     *            List of file profile IDs
     * @param p_accessToken
     * @param p_jobName
     *            Job name
     * @param p_jobComment
     *            Job comment
     * @param p_uploadDate
     *            Date for uploading
     */
    private void sendUploadCompletedEmail(List p_fileNames, List p_fpIds,
            String p_accessToken, String p_jobName, String p_jobComment,
            Date p_uploadDate)
    {
        try
        {
            User user = getUser(getUsernameFromSession(p_accessToken));
            Object[] projects = getProjectsFromFPIds(p_fpIds);
            int projectsLength = projects.length;
            String companyIdStr = String.valueOf(((Project) projects[0])
                    .getCompanyId());

            String[] messageArguments = new String[7];
            messageArguments[1] = p_jobName;
            messageArguments[2] = p_jobComment;

            // Prepare the project label and name since project can be
            // displayed as either "Division" or "Project"
            StringBuffer sb = new StringBuffer();
            sb.append("Project: ");
            for (int i = 0; i < projectsLength; i++)
            {
                sb.append(((Project) projects[i]).getName());
                if (i != projectsLength - 1)
                    sb.append(", ");
            }
            messageArguments[3] = sb.toString();

            sb = new StringBuffer();
            sb.append(user.getUserName());
            sb.append(" (");
            sb.append(user.getEmail());
            sb.append(")");
            messageArguments[4] = sb.toString();

            sb = new StringBuffer();
            int filesLength = p_fileNames.size();
            if (filesLength > 1)
                sb.append("\r\n");
            for (int i = 0; i < filesLength; i++)
            {
                // en_us\webservice\test\test.txt (fileprofile_1)
                sb.append(p_fileNames.get(i))
                        .append("  (")
                        .append(ServerProxy
                                .getFileProfilePersistenceManager()
                                .readFileProfile(
                                        Long.parseLong(p_fpIds.get(i)
                                                .toString())).getName())
                        .append(")");
                if (i != filesLength - 1)
                    sb.append("\r\n");
            }
            messageArguments[5] = sb.toString();
            messageArguments[6] = user.getSpecialNameForEmail();

            Timestamp time = new Timestamp();
            time.setLocale(getUserLocal(user));
            time.setDate(p_uploadDate);
            messageArguments[0] = time.toString();

            writeResultToLogFile(messageArguments);

            boolean m_systemNotificationEnabled = EventNotificationHelper
                    .systemNotificationEnabled();
            if (!m_systemNotificationEnabled)
            {
                return;
            }

            // send mail to uploader
            ServerProxy.getMailer().sendMailFromAdmin(user, messageArguments,
                    MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
                    MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE,
                    companyIdStr);

            // get the PMs address (could be a group alias)
            List pms = new ArrayList();
            boolean add = true;
            for (int i = 0; i < projectsLength; i++)
            {
                User u = UserHandlerHelper.getUser(((Project) projects[i])
                        .getProjectManagerId());
                if (u == null)
                {
                    logger.error("Can not get project manager for DesktopIcon upload notification by project "
                            + (Project) projects[i]);
                    return;
                }
                if (u.getUserId().equals(user.getUserId()))
                {
                    add = false;
                }
                else
                {
                    for (Iterator iter = pms.iterator(); iter.hasNext();)
                    {
                        User element = (User) iter.next();
                        if (u.getUserId().equals(element.getUserId()))
                            add = false;
                    }
                }
                if (add)
                    pms.add(u);
            }
            if (pms == null || pms.isEmpty())
            {
                if (add)
                {
                    logger.error("There was no GlobalSight project manager email address for DesktopIcon upload notification.");
                }

                return;
            }

            // send an email to the PMs
            for (Iterator iter = pms.iterator(); iter.hasNext();)
            {
                User u = (User) iter.next();
                time = new Timestamp();
                time.setLocale(getUserLocal(u));
                time.setDate(p_uploadDate);
                messageArguments[0] = time.toString();
                messageArguments[6] = u.getSpecialNameForEmail();

                ServerProxy.getMailer().sendMailFromAdmin(u, messageArguments,
                        MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
                        MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE,
                        companyIdStr);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to send the file upload completion emails.", e);
        }
    }

    /**
     * Write message arguments to log file
     * 
     * @param p_messageArguments
     */
    private void writeResultToLogFile(String[] p_messageArguments)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append("Upload time: ");
        sb.append(p_messageArguments[0]);
        sb.append("\r\n");
        sb.append("Job name: ");
        sb.append(p_messageArguments[1]);
        sb.append("\r\n");
        sb.append("Job Description: ");
        sb.append(p_messageArguments[2]);
        sb.append("\r\n");
        sb.append("Uploaded by: ");
        sb.append(p_messageArguments[4]);
        sb.append("\r\n");
        sb.append("Uploaded files: \r\n");
        sb.append(p_messageArguments[5]);
        sb.append("\r\n");

        logger.info(sb.toString());
    }

    /**
     * Returns projects which are associated with the list of file profile IDs
     * 
     * @param p_fps
     *            List of file profile IDs
     * @return Object[] Projects which are associated with the specified list of
     *         file profile IDs
     * @throws Exception
     */
    private Object[] getProjectsFromFPIds(List p_fps) throws Exception
    {
        List projects = new ArrayList();
        int len = p_fps.size();
        for (int i = 0; i < len; i++)
        {
            String fileProfileId = (String) p_fps.get(i);
            long fpid = Long.parseLong(fileProfileId);
            FileProfilePersistenceManager fppm = ServerProxy
                    .getFileProfilePersistenceManager();
            FileProfile fp = fppm.readFileProfile(fpid);
            Project p = getProject(fp);
            boolean add = true;
            for (Iterator iter = projects.iterator(); iter.hasNext();)
            {
                Project e = (Project) iter.next();
                if (e.getId() == p.getId())
                    add = false;
            }
            if (add)
                projects.add(p);
        }

        return projects.toArray();
    }

    /**
     * Get default UI locale information for specified user
     * 
     * @param p_user
     *            User information
     * @return Locale Default UI locale for the specified user
     */
    private Locale getUserLocal(User p_user)
    {
        String dl = p_user.getDefaultUILocale();
        if (dl == null)
            return new Locale("en", "US");
        else
        {
            try
            {
                String language = dl.substring(0, dl.indexOf("_"));
                String country = dl.substring(dl.indexOf("_") + 1);
                country = (country == null) ? "" : country;

                return new Locale(language, country);
            }
            catch (Exception e)
            {
                return new Locale("en", "US");
            }
        }
    }

    /**
     * Returns the value to which the specified key is mapped in m_store
     * 
     * @param p_key
     * @return
     */
    private Object get(Object p_key)
    {
        return dataStoreForFilesInSendingEmail.get(p_key);
    }

    /**
     * Maps the specified key to the specified value in this hashtable. Neither
     * the key nor the value can be null. The value can be retrieved by calling
     * the get method with a key that is equal to the original key.
     * 
     * @param p_key
     * @param p_value
     * @param p_overwrite
     * @return
     */
    private void put(Object p_key, Object p_value, boolean p_overwrite)
    {
        if (p_overwrite || !dataStoreForFilesInSendingEmail.containsKey(p_key))
        {
            dataStoreForFilesInSendingEmail.put(p_key, p_value);
        }
    }

    /**
     * Calls <code>sid.trim()</code>. Then return null if the length is 0.
     * 
     * @param sid
     * @return
     */
    private String clearSid(String sid)
    {
        if (sid != null)
        {
            sid = sid.trim();
            if (sid.length() == 0 || "null".equalsIgnoreCase(sid))
            {
                sid = null;
            }
        }

        return sid;
    }

    /**
     * Override method provided for previous version
     * 
     * @deprecated
     */
    public void saveEntry(String p_accessToken, String p_tmProfileName,
            String p_sourceLocale, String p_sourceSegment,
            String p_targetLocale, String p_targetSegment)
            throws WebServiceException
    {
        saveEntry(p_accessToken, p_tmProfileName, null, p_sourceLocale,
                p_sourceSegment, p_targetLocale, p_targetSegment, "false");
    }

    /**
     * Saves one entry to tm.
     * 
     * @param p_accessToken
     * @param p_tmProfileName
     * @param sid
     * @param p_sourceLocale
     * @param p_sourceSegment
     * @param p_targetLocale
     * @param p_targetSegment
     * @param isEscape
     *            : boolean type
     * @return String
     * @throws WebServiceException
     */
    public String saveEntry(String p_accessToken, String p_tmProfileName,
            String sid, String p_sourceLocale, String p_sourceSegment,
            String p_targetLocale, String p_targetSegment, boolean isEscape)
            throws WebServiceException
    {
        String escapeString = "false";
        if (isEscape)
        {
            escapeString = "true";
        }

        String rtn = saveEntry(p_accessToken, p_tmProfileName, sid,
                p_sourceLocale, p_sourceSegment, p_targetLocale,
                p_targetSegment, escapeString);

        return rtn;
    }

    /**
     * Saves one entry to tm.
     * <p>
     * Segents, locals need use same order, and the first will be source. If the
     * source segment is exist in the specified tm, a exception will be throw
     * out.
     * <p>
     * Following is a sample example of how to use the method.<br>
     * 
     * <pre>
     * List&lt;String&gt; locales = new ArrayList&lt;String&gt;();
     * List&lt;String&gt; segments = new ArrayList&lt;String&gt;();
     * locales.add(&quot;en_US&quot;);
     * segments.add(&quot;source segment&quot;);
     * locales.add(&quot;fr_FR&quot;);
     * segments.add(&quot;target segment&quot;);
     * 
     * String entry = ambassador.saveEntry(accessToken, tmProfileName, locales,
     *         segments);
     * </pre>
     * 
     * An example XML response is:
     * 
     * <pre>
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
     * &lt;entry&gt;
     * &lt;source&gt;
     *     &lt;locale&gt;en_US&lt;/locale&gt;
     *     &lt;segment&gt;source &lt;bpt i=&quot;1&quot; type=&quot;bold&quot; x=&quot;1&quot;&gt;&lt;b&gt;&lt;/bpt&gt;content&lt;ept i=&quot;1&quot;&gt;&lt;/b&gt;&lt;/ept&gt;&lt;/segment&gt;
     * &lt;/source&gt;
     * &lt;target&gt;
     *     &lt;locale&gt;fr_FR&lt;/locale&gt;
     *     &lt;segment&gt;target &lt;bpt i=&quot;1&quot; type=&quot;bold&quot; x=&quot;1&quot;&gt;&lt;b&gt;&lt;/bpt&gt;content&lt;ept i=&quot;1&quot;&gt;&lt;/b&gt;&lt;/ept&gt;&lt;/segment&gt;
     * &lt;/target&gt;
     * &lt;/entry&gt;
     * </pre>
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmProfileName
     *            The name of tm profile, can not be null.
     * @param sid
     *            The sid.
     * @param sourceLocale
     *            The source lcoale.
     * @param sourceSegment
     *            The source string.
     * @param targetLocal
     *            The target locale.
     * @param targetSegment
     *            The target string.
     * @param escapeString
     *            Is convert all the escapable characters into their string
     *            (escaped) equivalents or not, the value must be 'true' or
     *            'false'.
     * 
     * @return A string, include segment information of the entry.
     * 
     * @see #login(username, password)
     */
    public String saveEntry(String p_accessToken, String p_tmProfileName,
            String sid, String p_sourceLocale, String p_sourceSegment,
            String p_targetLocale, String p_targetSegment, String escapeString)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_tmProfileName, "tm profile name");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            Assert.assertNotEmpty(p_sourceSegment, "source string");
            Assert.assertNotNull(p_targetLocale, "target locale");
            if (escapeString == null
                    || escapeString.trim().length() == 0
                    || (!"true".equals(escapeString.trim()) && !"false"
                            .equals(escapeString.trim())))
            {
                escapeString = "false";
            }
            escapeString = escapeString.toLowerCase();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "saveEntry");
        checkPermission(p_accessToken, Permission.TM_ADD_ENTRY);

        sid = clearSid(sid);

        boolean escape = Boolean.parseBoolean(escapeString);
        p_sourceSegment = wrapSegment(p_sourceSegment, escape);
        p_targetSegment = wrapSegment(p_targetSegment, escape);
        if (!escape)
        {
            p_sourceSegment = repairSegment(p_sourceSegment);
            p_targetSegment = repairSegment(p_targetSegment);
        }

        SegmentTmTu tu = new SegmentTmTu();
        tu.setTranslatable();
        tu.setFormat("plaintext");
        tu.setType("text");

        SegmentTmTuv sourceTuv = new SegmentTmTuv();
        SegmentTmTuv targetTuv = new SegmentTmTuv();

        sourceTuv.setTu(tu);
        targetTuv.setTu(tu);

        GlobalSightLocale sourceLocale = getLocaleByName(p_sourceLocale);
        GlobalSightLocale targetLocale = getLocaleByName(p_targetLocale);

        tu.setSourceLocale(sourceLocale);
        sourceTuv.setLocale(sourceLocale);
        targetTuv.setLocale(targetLocale);

        sourceTuv.setCreationUser(Tmx.DEFAULT_USER);
        sourceTuv.setCreationDate(new java.sql.Timestamp(new Date().getTime()));
        sourceTuv.setSegment(p_sourceSegment);
        sourceTuv.setSid(sid);

        targetTuv.setCreationUser(Tmx.DEFAULT_USER);
        targetTuv.setCreationDate(new java.sql.Timestamp(new Date().getTime()));
        targetTuv.setSegment(p_targetSegment);
        targetTuv.setSid(sid);

        tu.addTuv(sourceTuv);
        tu.addTuv(targetTuv);

        List tus = new ArrayList();
        tus.add(tu);

        Tm tm = getProjectTm(p_tmProfileName);
        try
        {
            LingServerProxy.getTmCoreManager().saveToSegmentTm(tm, tus,
                    TmCoreManager.SYNC_MERGE, null);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        StringBuilder returnString = new StringBuilder(XML_HEAD);
        String entryXml = MessageFormat.format(ENTRY_XML_SAVE, sid,
                sourceLocale.toString(), p_sourceSegment,
                targetLocale.toString(), p_targetSegment);
        if (sid == null)
        {
            entryXml = entryXml.replaceAll("\r\n\t<sid>.*?</sid>", "");
        }
        returnString.append(entryXml);
        return returnString.toString();
    }

    /**
     * Try to repair the segment.
     * <p>
     * Will throw out a WebServiceException if the format is wrong and can not
     * be repaired.
     * <p>
     * 
     * @see SegmentHandler
     * @see #validateSegment(Element, IntHolder)
     * 
     * @param s
     *            The segment to be repaired
     * @return The repaired segment
     * @throws WebServiceException
     */
    private String repairSegment(String s) throws WebServiceException
    {
        Assert.assertNotEmpty(s, "segment");
        SAXReader reader = new SAXReader();
        SegmentHandler segmentHandler = new SegmentHandler(s);
        reader.addHandler("/segment", segmentHandler);
        try
        {
            reader.read(new StringReader(s));
            if (segmentHandler.hasError())
            {
                throw new WebServiceException(segmentHandler.getError());
            }
            return segmentHandler.getSegment();
        }
        catch (DocumentException e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Parses a segment string, and try to repair it.
     * <p>
     * Remember to check out the error is not null.
     */
    private class SegmentHandler implements ElementHandler
    {
        private String segment = null;
        private String error = null;

        public SegmentHandler(String segment)
        {
            this.segment = segment;
        }

        public String getSegment()
        {
            return segment;
        }

        public boolean hasError()
        {
            return error != null;
        }

        public String getError()
        {
            return error;
        }

        @Override
        public void onEnd(ElementPath path)
        {
            Element element = path.getCurrent();
            element.detach();
            try
            {
                validateSegment(element, new IntHolder(1));
                this.segment = "<segment>" + ImportUtil.getInnerXml(element)
                        + "</segment>";
            }
            catch (Exception e)
            {
                error = e.getMessage();
            }
        }

        @Override
        public void onStart(ElementPath path)
        {

        }
    }

    /**
     * Validates the segment, and try to repair it if the format is wrong.
     * 
     * <p>
     * Will throw out a exception if the format is wrong and can not be
     * repaired.
     * 
     * @param p_seg
     *            The segment string to validate
     * @param p_x_count
     *            The value of x
     * @return Repaired segment
     * @throws Exception
     */
    private Element validateSegment(Element p_seg, IntHolder p_x_count)
            throws Exception
    {
        String attr;

        List elems = p_seg.elements();

        for (Iterator it = elems.iterator(); it.hasNext();)
        {
            Element elem = (Element) it.next();
            String name = elem.getName();

            if (name.equals("bpt"))
            {
                attr = elem.attributeValue("x"); // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("i"); // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                            "A <bpt> tag is lacking the mandatory i attribute.");
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", DEFAULT_TYPE);
                }
            }
            else if (name.equals("ept"))
            {
                attr = elem.attributeValue("i"); // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                            "A <ept> tag is lacking the mandatory i attribute.");
                }
            }
            else if (name.equals("it"))
            {
                attr = elem.attributeValue("x"); // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("pos"); // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                            "A <it> tag is lacking the mandatory pos attribute.");
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", DEFAULT_TYPE);
                }
            }
            else if (name.equals("ph"))
            {
                attr = elem.attributeValue("x"); // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", DEFAULT_TYPE);
                }

                // GXML doesn't care about assoc, just preserve it.
                // attr = elem.attributeValue("assoc");
            }
            else if (name.equals("ut"))
            {
                // TMX level 2 does not allow UT. We can either remove
                // it, or look inside and guess what it may be.
                it.remove();
                continue;
            }

            // Recurse into any subs.
            validateSubs(elem, p_x_count);
        }

        return p_seg;
    }

    /**
     * Validates the sub elements inside a TMX tag. This means adding a <sub
     * locType="..."> attribute.
     * 
     * @param p_elem
     * @param p_x_count
     * @throws Exception
     */
    private void validateSubs(Element p_elem, IntHolder p_x_count)
            throws Exception
    {
        List subs = p_elem.elements("sub");

        for (int i = 0, max = subs.size(); i < max; i++)
        {
            Element sub = (Element) subs.get(i);
            validateSegment(sub, p_x_count);
        }
    }

    /**
     * Override method provided for previous version
     * 
     * @deprecated
     * @param p_accessToken
     * @param p_tmProfileName
     * @param p_string
     * @param p_sourceLocale
     * @return
     * @throws WebServiceException
     */
    public String searchEntries(String p_accessToken, String p_tmProfileName,
            String p_string, String p_sourceLocale) throws WebServiceException
    {
        String rtn = searchEntries(p_accessToken, p_tmProfileName, p_string,
                p_sourceLocale, "false");

        return rtn;
    }

    /**
     * Override method : the last parameter is 'boolean' type
     * 
     * @param p_accessToken
     * @param p_tmProfileName
     * @param p_string
     * @param p_sourceLocale
     * @param isEscape
     * @return
     * @throws WebServiceException
     */
    public String searchEntries(String p_accessToken, String p_tmProfileName,
            String p_string, String p_sourceLocale, boolean isEscape)
            throws WebServiceException
    {
        String escapeString = "false";
        if (isEscape)
        {
            escapeString = "true";
        }
        String rtn = searchEntries(p_accessToken, p_tmProfileName, p_string,
                p_sourceLocale, escapeString);

        return rtn;
    }

    /**
     * Searchs entries in tm.
     * 
     * An example XML response is:
     * 
     * <pre>
     * &lt;entries&gt;
     *     &lt;entry&gt;
     *         &lt;percentage&gt;100%&lt;/percentage&gt;
     *         &lt;source&gt;
     *           &lt;locale&gt;en_US&lt;/sourceLocale&gt;
     *           &lt;segment&gt;source_100%&lt;/segment&gt;
     *         &lt;/source&gt;
     *         &lt;target&gt;
     *           &lt;locale&gt;fr_FR&lt;/sourceLocale&gt;
     *           &lt;segment&gt;target_fr_FR&lt;/segment&gt;
     *         &lt;/target&gt;
     *     &lt;/entry&gt;
     * 
     *     &lt;entry&gt;
     *         &lt;percentage&gt;90%&lt;/percentage&gt;
     *         &lt;source&gt;
     *           &lt;locale&gt;en_US&lt;/sourceLocale&gt;
     *           &lt;segment&gt;source_90%&lt;/segment&gt;
     *         &lt;/source&gt;
     *         &lt;target&gt;
     *           &lt;locale&gt;de_DE&lt;/sourceLocale&gt;
     *           &lt;segment&gt;target_de_DE&lt;/segment&gt;
     *         &lt;/target&gt;
     *     &lt;/entry&gt;
     * lt;/entries&gt;
     * </pre>
     * 
     * @param tmProfileName
     *            The name of tm profile, can not be null.
     * @param string
     *            Search entries will according to it, can not be null.
     * @param sourceLocale
     *            The locale of segment, can not be null.
     * @param escapeString
     *            Is convert all the escapable characters into their string
     *            (escaped) equivalents or not, the value must be 'true' or
     *            'false'.
     * @return The search result, may be null.
     * @throws WebServiceException
     * 
     * @see #login(username, password)
     */
    public String searchEntries(String p_accessToken, String p_tmProfileName,
            String p_string, String p_sourceLocale, String escapeString)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_tmProfileName, "tm profile name");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            Assert.assertNotEmpty(p_string, "source string");
            if (escapeString == null
                    || escapeString.trim().length() == 0
                    || (!"true".equals(escapeString.trim()) && !"false"
                            .equals(escapeString.trim())))
            {
                escapeString = "false";
            }
            escapeString = escapeString.toLowerCase();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "searchEntries");
        // checkPermission(p_accessToken, Permission.SERVICE_TM_SEARCH_ENTRY);

        StringBuilder returnString = new StringBuilder(XML_HEAD);
        returnString.append("<entries>");

        LeverageMatchResults levMatchResult = null;
        Vector localePairs = null;
        try
        {
            localePairs = ServerProxy.getLocaleManager()
                    .getSourceTargetLocalePairs();
        }
        catch (Exception e)
        {
            String message = "Unable to get all locale pairs";
            logger.error(message, e);
            throw new WebServiceException(message);
        }

        GlobalSightLocale sourceLocale = getLocaleByName(p_sourceLocale);
        Session session = null;

        try
        {
            session = HibernateUtil.getSession();
            Leverager leverager = new Leverager(session);
            LeveragingLocales levLocales = new LeveragingLocales();

            Map<String, List<LeveragedTuv>> storedTuvs = new HashMap<String, List<LeveragedTuv>>();
            ArrayList trgLocales = new ArrayList();
            Iterator it = localePairs.iterator();
            while (it.hasNext())
            {
                LocalePair localePair = (LocalePair) it.next();
                if (localePair.getSource().equals(sourceLocale))
                {
                    GlobalSightLocale targetLocale = localePair.getTarget();
                    trgLocales.add(targetLocale);
                    levLocales.setLeveragingLocale(targetLocale, null);
                }
            }

            TranslationMemoryProfile tmp = TMProfileHandlerHelper
                    .getTMProfileByName(p_tmProfileName);
            if (tmp == null)
            {
                String message = "Unable to get translation memory profile:"
                        + p_tmProfileName;
                logger.error(message);
                throw new WebServiceException(message);
            }
            ProjectTM ptm = ServerProxy.getProjectHandler().getProjectTMById(
                    tmp.getProjectTmIdForSave(), false);
            String companyId = String.valueOf(ptm.getCompanyId());

            Set tmNamesOverride = new HashSet();
            Vector<LeverageProjectTM> tms = tmp.getProjectTMsToLeverageFrom();
            for (LeverageProjectTM tm : tms)
            {
                tmNamesOverride.add(tm.getProjectTmId());
            }

            OverridableLeverageOptions levOptions = new OverridableLeverageOptions(
                    tmp, levLocales);

            int threshold = (int) tmp.getFuzzyMatchThreshold();
            levOptions.setMatchThreshold(threshold);
            levOptions.setTmsToLeverageFrom(tmNamesOverride);
            boolean isTmProcedence = tmp.isTmProcendence();
            String segment = wrapSegment(p_string,
                    Boolean.valueOf(escapeString));
            PageTmTu tu = new PageTmTu(-1, -1, "plaintext", "text", true);
            PageTmTuv tuv = new PageTmTuv(-1, segment, sourceLocale);
            tuv.setTu(tu);
            tuv.setExactMatchKey();
            tu.addTuv(tuv);

            Iterator<LeverageMatches> itLeverageMatches = LingServerProxy
                    .getTmCoreManager()
                    .leverageSegments(Collections.singletonList(tuv),
                            sourceLocale, trgLocales, levOptions, companyId)
                    .leverageResultIterator();

            // In fact only ONE levMatches in this iterator.
            while (itLeverageMatches.hasNext())
            {
                LeverageMatches levMatches = (LeverageMatches) itLeverageMatches
                        .next();

                // walk through all target locales in the LeverageMatches
                Iterator itLocales = levMatches.targetLocaleIterator(companyId);
                while (itLocales.hasNext())
                {
                    GlobalSightLocale tLocale = (GlobalSightLocale) itLocales
                            .next();
                    // walk through all matches in the locale
                    Iterator itMatch = levMatches.matchIterator(tLocale,
                            companyId);
                    while (itMatch.hasNext())
                    {
                        LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();

                        if (matchedTuv.getScore() < threshold)
                        {
                            continue;
                        }

                        List<LeveragedTuv> tuvs = storedTuvs.get(tLocale
                                .toString());
                        if (tuvs == null)
                        {
                            tuvs = new ArrayList<LeveragedTuv>();
                            storedTuvs.put(tLocale.toString(), tuvs);
                        }

                        storedTuvs.get(tLocale.toString()).add(matchedTuv);
                    }
                }
            }

            Set<String> localeNames = storedTuvs.keySet();
            if (localeNames.size() > 0)
            {
                Connection connection = null;
                try
                {
                    connection = ConnectionPool.getConnection();
                    String tmName = "";

                    for (String name : localeNames)
                    {
                        List<LeveragedTuv> matchedTuvs = storedTuvs.get(name);
                        Collections.sort(
                                matchedTuvs,
                                getMatchedTuvComparator(levOptions,
                                        isTmProcedence));
                        int size = Math.min(matchedTuvs.size(),
                                (int) tmp.getNumberOfMatchesReturned());
                        for (int i = 0; i < size; i++)
                        {
                            LeveragedTuv matchedTuv = matchedTuvs.get(i);
                            BaseTmTuv sourceTuv = matchedTuv.getSourceTuv();

                            long tmId = sourceTuv.getTu().getTmId();
                            logger.info("tmId : " + tmId);
                            try
                            {
                                tmName = getProjectTmName(tmId, connection);
                            }
                            catch (Exception e)
                            {
                                logger.error("Cannot get tm name.", e);
                            }
                            logger.info("tmName : " + tmName);
                            String strTmId = "'" + tmId + "'";

                            String entryXml = MessageFormat.format(ENTRY_XML,
                                    strTmId, tmName, matchedTuv.getScore(),
                                    sourceTuv.getSid(), sourceTuv.getLocale(),
                                    sourceTuv.getSegmentNoTopTag(),
                                    matchedTuv.getLocale(),
                                    matchedTuv.getSegmentNoTopTag());

                            if (sourceTuv.getSid() == null
                                    || sourceTuv.getSid().length() == 0)
                            {
                                entryXml = entryXml.replaceAll(
                                        "\r\n\t\t<sid>.*?</sid>", "");
                            }

                            returnString.append(entryXml);
                        }
                        // Remained trgLocales have no tm matches better than
                        // threshold
                        for (int i = 0; i < trgLocales.size(); i++)
                        {
                            GlobalSightLocale trgLocale = (GlobalSightLocale) trgLocales
                                    .get(i);
                            if (trgLocale.toString().equals(name))
                            {
                                trgLocales.remove(trgLocale);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error("Error found in searchEntries(...).", e);
                }
                finally
                {
                    ConnectionPool.returnConnection(connection);
                }
            }

            // Use MT to get target content for rest target locales
            MachineTranslator mtEngine = initMachineTranslator(tmp);
            boolean hasMTResult = false;
            if (mtEngine != null && trgLocales != null && trgLocales.size() > 0)
            {
                for (int i = 0; i < trgLocales.size(); i++)
                {
                    GlobalSightLocale trgLocale = (GlobalSightLocale) trgLocales
                            .get(i);
                    boolean isSupportedByMT = isLocalePairSupportedByMT(
                            mtEngine, sourceLocale, trgLocale);
                    if (isSupportedByMT)
                    {
                        if (hasMTResult == false)
                        {
                            hasMTResult = true;
                        }
                        String srcString = segment.replace("<segment>", "")
                                .replace("</segment>", "");
                        String translatedString = mtEngine.translate(
                                sourceLocale.getLocale(),
                                trgLocale.getLocale(), srcString);
                        if (translatedString != null
                                && !translatedString.equals(srcString))
                        {
                            int mtMatchPercentage = (int) tmp
                                    .getMtConfidenceScore();

                            String entryXml = MessageFormat.format(ENTRY_XML,
                                    "'MT!'", tmp.getMtEngine(),
                                    mtMatchPercentage, "no sid",
                                    sourceLocale.getLocale(), srcString,
                                    trgLocale.getLocale(), translatedString);
                            entryXml = entryXml.replaceAll(
                                    "\r\n\t\t<sid>.*?</sid>", "");
                            returnString.append(entryXml);
                        }
                    }
                }
            }

            // Return NULL_XML if no TM matches whose score is higher than TM
            // threshold, and
            // no MT results (MT not used or all target locales are not
            // supported by MT).
            if (localeNames.size() == 0
                    && (tmp.getUseMT() == false || hasMTResult == false))
            {
                return NULL_XML;
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            try
            {
                if (session != null)
                {
                    session.close();
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new WebServiceException(e.getMessage());
            }
        }

        returnString.append("\r\n</entries>");
        return returnString.toString();
    }

    /**
     * Remote leveraging.
     * 
     * @param p_accessToken
     *            Access token.
     * @param p_remoteTmProfileId
     *            Tm profile id on remote server
     * @param p_segmentMap
     *            (OriginalTuvId:Segment) map
     * @param p_sourceLocaleId
     *            Source locale Id
     * @param p_btrgLocal2LevLocalesMap
     *            (Target locale Id:leverage locales Ids with comma seperated)
     *            map
     * @param p_translatable
     *            True:translatable segments;False:localizable segments
     * @param p_escapeString
     *            If escape string.
     * @return leveraged results in map.
     * 
     * @throws WebServiceException
     */
    public HashMap searchEntriesInBatch(String p_accessToken,
            Long p_remoteTmProfileId, Map p_segmentMap, Long p_sourceLocaleId,
            Map p_btrgLocal2LevLocalesMap, Boolean p_translatable,
            Boolean p_escapeString) throws WebServiceException
    {
        checkAccess(p_accessToken, "searchEntriesInBatch");
        // checkPermission(p_accessToken, Permission.SERVICE_TM_SEARCH_ENTRY);

        HashMap originalTuvId2MatchesMap = new HashMap();
        Session session = null;
        try
        {
            session = HibernateUtil.getSession();

            Leverager leverager = new Leverager(session);
            LocaleManager localeManager = ServerProxy.getLocaleManager();
            ProjectHandler projectHandler = ServerProxy.getProjectHandler();

            // source locale
            GlobalSightLocale sourceLocale = null;
            sourceLocale = localeManager.getLocaleById(p_sourceLocaleId);

            // target locales and leverage locales
            ArrayList trgLocales = new ArrayList();
            LeveragingLocales levLocales = new LeveragingLocales();
            if (p_btrgLocal2LevLocalesMap != null
                    && p_btrgLocal2LevLocalesMap.size() > 0)
            {
                Iterator iter = p_btrgLocal2LevLocalesMap.entrySet().iterator();
                while (iter.hasNext())
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    long trgLocaleId = ((Long) entry.getKey()).longValue();
                    GlobalSightLocale trgLocale = localeManager
                            .getLocaleById(trgLocaleId);
                    trgLocales.add(trgLocale);

                    String levLocaleIds = (String) entry.getValue();
                    StringTokenizer st = new StringTokenizer(levLocaleIds, ",");
                    while (st.hasMoreElements())
                    {
                        long levLocaleId = Long.parseLong((String) st
                                .nextElement());
                        GlobalSightLocale levLocale = localeManager
                                .getLocaleById(levLocaleId);
                        Set leveragingLocales = null;
                        try
                        {
                            leveragingLocales = levLocales
                                    .getLeveragingLocales(levLocale);
                        }
                        catch (Exception e)
                        {
                        }
                        levLocales.setLeveragingLocale(levLocale,
                                leveragingLocales);
                    }
                }
            }

            // tm profile
            TranslationMemoryProfile tmp = TMProfileHandlerHelper
                    .getTMProfileById(p_remoteTmProfileId);
            if (tmp == null)
            {
                String message = "Unable to get translation memory profile by id:"
                        + p_remoteTmProfileId;
                logger.error(message);
                throw new WebServiceException(message);
            }

            ProjectTM ptm = ServerProxy.getProjectHandler().getProjectTMById(
                    tmp.getProjectTmIdForSave(), false);
            String companyId = String.valueOf(ptm.getCompanyId());
            // tmIdsOverride
            Set tmIdsOverride = new HashSet();
            Vector<LeverageProjectTM> tms = tmp.getProjectTMsToLeverageFrom();
            for (LeverageProjectTM tm : tms)
            {
                ProjectTM projectTm = (ProjectTM) projectHandler
                        .getProjectTMById(tm.getProjectTmId(), false);
                if (projectTm.getIsRemoteTm() == false)
                {
                    tmIdsOverride.add(tm.getProjectTmId());
                }
            }

            // levOptions & leverageDataCenter
            OverridableLeverageOptions levOptions = new OverridableLeverageOptions(
                    tmp, levLocales);
            int threshold = (int) tmp.getFuzzyMatchThreshold();
            levOptions.setMatchThreshold(threshold);
            levOptions.setTmsToLeverageFrom(tmIdsOverride);
            boolean isTmProcedence = tmp.isTmProcendence();

            // find the source tuvs
            List<PageTmTuv> sourceTuvs = new ArrayList<PageTmTuv>();
            Iterator segmentsIter = p_segmentMap.entrySet().iterator();
            while (segmentsIter.hasNext())
            {
                Map.Entry entry = (Map.Entry) segmentsIter.next();
                long srcTuvId = ((Long) entry.getKey()).longValue();
                String segment = (String) entry.getValue();
                segment = wrapSegment(segment, p_escapeString.booleanValue());
                PageTmTu tu = new PageTmTu(-1, -1, "plaintext", "text",
                        p_translatable);
                PageTmTuv tuv = new PageTmTuv(srcTuvId, segment, sourceLocale);
                tuv.setTu(tu);
                tuv.setExactMatchKey();
                tu.addTuv(tuv);
                sourceTuvs.add(tuv);
            }

            // Leverage
            LeverageDataCenter leverageDataCenter = null;
            try
            {
                leverageDataCenter = LingServerProxy.getTmCoreManager()
                        .leverageSegments(sourceTuvs, sourceLocale, trgLocales,
                                levOptions, companyId);
            }
            catch (Exception e)
            {
                logger.error("Failed to leverage segments.", e);
            }

            Iterator itLeverageMatches = leverageDataCenter
                    .leverageResultIterator();
            while (itLeverageMatches.hasNext())
            {
                // one "LeverageMatches" represents one segment matches
                LeverageMatches levMatches = (LeverageMatches) itLeverageMatches
                        .next();
                long originalTuvId = levMatches.getOriginalTuv().getId();
                HashMap trgLocaleMatchesMap = new HashMap();

                Iterator itLocales = levMatches.targetLocaleIterator(companyId);
                while (itLocales.hasNext())
                {
                    GlobalSightLocale targetLocale = (GlobalSightLocale) itLocales
                            .next();
                    Vector matchedTuvMapForSpecifiedTrgLocale = new Vector();

                    HashMap innerMap = new HashMap();
                    Iterator itMatch = levMatches.matchIterator(targetLocale,
                            companyId);
                    while (itMatch.hasNext())
                    {
                        LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();

                        HashMap matchInfoMap = new HashMap();
                        String subId = ((SegmentTmTu) levMatches
                                .getOriginalTuv().getTu()).getSubId();
                        matchInfoMap.put("subId", subId);
                        String matchedSegment = matchedTuv.getSegmentNoTopTag();
                        matchedSegment = matchedTuv.getSegment();
                        matchInfoMap.put("matchedSegment", matchedSegment);
                        String matchType = matchedTuv.getMatchState().getName();
                        matchInfoMap.put("matchType", matchType);
                        int orderNum = matchedTuv.getOrder();
                        matchInfoMap.put("orderNum", orderNum);
                        float score = matchedTuv.getScore();
                        matchInfoMap.put("score", score);
                        // source string from TM
                        ProjectTmTuvT tmTuv = HibernateUtil.get(
                                ProjectTmTuvT.class, matchedTuv.getId());
                        String tmSource = "";
                        if (tmTuv != null)
                        {
                            try
                            {
                                tmSource = tmTuv.getTu().getSourceTuv()
                                        .getSegmentString();
                            }
                            catch (Exception ex)
                            {
                            }
                        }
                        matchInfoMap.put("tmSourceStr", tmSource);
                        int matchedTableType = LeverageMatchLingManagerLocal
                                .getMatchTableType(matchedTuv);
                        matchInfoMap.put("matchedTableType", matchedTableType);

                        matchedTuvMapForSpecifiedTrgLocale.add(matchInfoMap);
                    }
                    innerMap.put(targetLocale.getId(),
                            matchedTuvMapForSpecifiedTrgLocale);

                    trgLocaleMatchesMap.putAll(innerMap);
                }

                originalTuvId2MatchesMap
                        .put(originalTuvId, trgLocaleMatchesMap);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            try
            {
                if (session != null)
                {
                    session.close();
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new WebServiceException(e.getMessage());
            }
        }

        return originalTuvId2MatchesMap;
    }

    /**
     * Initializes the MT engine that the PageManager will use during
     * leveraging.
     * 
     * @param p_tmProfile
     * @return
     */
    private MachineTranslator initMachineTranslator(
            TranslationMemoryProfile p_tmProfile)
    {
        String engineClass = null;
        MachineTranslator mt = null;

        try
        {
            if (!p_tmProfile.getUseMT())
            {
                logger.info("Not using machine translation during leveraging.");
            }
            else
            {
                String mtEngineName = p_tmProfile.getMtEngine();
                if (mtEngineName != null
                        && mtEngineName.equalsIgnoreCase("Google"))
                {
                    engineClass = "com.globalsight.machineTranslation.google.GoogleProxy";
                }
                else if (mtEngineName != null
                        && mtEngineName.equalsIgnoreCase("ProMT"))
                {
                    engineClass = "com.globalsight.machineTranslation.promt.ProMTProxy";
                }
                else if (mtEngineName != null
                        && mtEngineName.equalsIgnoreCase("MS_Translator"))
                {
                    engineClass = "com.globalsight.machineTranslation.mstranslator.MSTranslatorProxy";
                }

                mt = (MachineTranslator) Class.forName(engineClass)
                        .newInstance();

                logger.info("Using machine translation engine: "
                        + mt.getEngineName() + " for searchEntries API.");
            }
        }
        catch (Exception ex)
        {
            logger.error(
                    "Could not initialize machine translation engine from class "
                            + engineClass, ex);
        }

        return mt;
    }

    /**
     * Checks to see if the locale pair is supported by the MT engine
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @return true | false
     */
    private boolean isLocalePairSupportedByMT(MachineTranslator p_mt,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
    {
        boolean isSupported = false;
        if (p_mt == null)
        {
            return false;
        }

        try
        {
            isSupported = p_mt.supportsLocalePair(p_sourceLocale.getLocale(),
                    p_targetLocale.getLocale());
        }
        catch (Exception e)
        {
            logger.error("Failed to find if locale pair ("
                    + p_sourceLocale.getLocale() + "->"
                    + p_targetLocale.getLocale() + " is supported by MT "
                    + p_mt.getEngineName());
        }

        return isSupported;
    }

    /**
     * Gets the comparator for matched TUVs
     * 
     * @param leverageOptions
     * @param isTmProcedence
     * @return
     */
    private Comparator<LeveragedTuv> getMatchedTuvComparator(
            final OverridableLeverageOptions leverageOptions,
            final boolean isTmProcedence)
    {
        return new Comparator<LeveragedTuv>()
        {
            @Override
            public int compare(LeveragedTuv tuv1, LeveragedTuv tuv2)
            {
                long tmId1 = tuv1.getTu().getTmId();
                long tmId2 = tuv2.getTu().getTmId();
                int projectIndex1 = getProjectIndex(tmId1);
                int projectIndex2 = getProjectIndex(tmId2);
                float result = 0.0f;
                if (isTmProcedence)
                {
                    result = projectIndex1 - projectIndex2;
                    if (result == 0)
                    {
                        result = tuv2.getScore() - tuv1.getScore();
                    }
                }
                else
                {
                    result = tuv2.getScore() - tuv1.getScore();
                    if (result == 0)
                    {
                        result = projectIndex1 - projectIndex2;
                    }
                }
                return (int) result;
            }

            private int getProjectIndex(long tmId)
            {
                return leverageOptions.getTmIndexsToLeverageFrom().get(tmId);
            }
        };
    }

    /**
     * Override method provided for previous version
     * 
     * @deprecated
     * @param p_accessToken
     * @param p_tmProfileName
     * @param p_sourceLocale
     * @param p_sourceSegment
     * @param p_targetLocale
     * @param p_targetSegment
     * @throws WebServiceException
     */
    public void editEntry(String p_accessToken, String p_tmProfileName,
            String p_sourceLocale, String p_sourceSegment,
            String p_targetLocale, String p_targetSegment)
            throws WebServiceException
    {
        editEntry(p_accessToken, p_tmProfileName, null, null, p_sourceLocale,
                p_sourceSegment, p_targetLocale, p_targetSegment, "false");
    }

    /**
     * Edits exists entry
     * 
     * @param p_accessToken
     * @param p_tmProfileName
     * @param p_orgSid
     * @param p_newSid
     * @param p_sourceLocale
     * @param p_sourceSegment
     * @param p_targetLocale
     * @param p_targetSegment
     * @param isEscape
     * @throws WebServiceException
     */
    public void editEntry(String p_accessToken, String p_tmProfileName,
            String p_orgSid, String p_newSid, String p_sourceLocale,
            String p_sourceSegment, String p_targetLocale,
            String p_targetSegment, boolean isEscape)
            throws WebServiceException
    {
        String escapeString = "false";
        if (isEscape)
        {
            escapeString = "true";
        }

        editEntry(p_accessToken, p_tmProfileName, p_orgSid, p_newSid,
                p_sourceLocale, p_sourceSegment, p_targetLocale,
                p_targetSegment, escapeString);
    }

    /**
     * Edits a entry.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmProfileName
     *            The name of tm profile, can not be null.
     * @param orgSid
     *            The original sid.
     * @param newSid
     *            The new sid.
     * @param sourceLocale
     *            The source lcoale.
     * @param sourceSegment
     *            The source string.
     * @param targetLocal
     *            The target locale.
     * @param targetSegment
     *            The target string.
     * @param escapeString
     *            Is convert all the escapable characters into their string
     *            (escaped) equivalents or not, the value must be 'true' or
     *            'false'.
     * @return Error message if has, will be null if success.
     * @throws WebServiceException
     * 
     * @see #login(username, password)
     */
    public void editEntry(String p_accessToken, String p_tmProfileName,
            String p_orgSid, String p_newSid, String p_sourceLocale,
            String p_sourceSegment, String p_targetLocale,
            String p_targetSegment, String escapeString)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_tmProfileName, "tm profile name");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            Assert.assertNotEmpty(p_sourceSegment, "source string");
            Assert.assertNotEmpty(p_targetLocale, "target locale");
            if (escapeString == null
                    || escapeString.trim().length() == 0
                    || (!"true".equals(escapeString.trim()) && !"false"
                            .equals(escapeString.trim())))
            {
                escapeString = "false";
            }
            escapeString = escapeString.toLowerCase();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "editEntry");
        checkPermission(p_accessToken, Permission.TM_EDIT_ENTRY);

        try
        {
            long targetLocaleId = getLocaleByName(p_targetLocale).getId();
            p_orgSid = clearSid(p_orgSid);
            p_newSid = clearSid(p_newSid);

            boolean escape = Boolean.parseBoolean(escapeString);
            p_sourceSegment = wrapSegment(p_sourceSegment, escape);
            p_targetSegment = wrapSegment(p_targetSegment, escape);
            if (!escape)
            {
                p_targetSegment = repairSegment(p_targetSegment);
            }

            ProjectTmTuT tu = getTu(p_tmProfileName, p_sourceSegment,
                    p_sourceLocale, p_orgSid);
            Set<ProjectTmTuvT> tuvs = tu.getTuvs();

            ProjectTmTuvT editTuv = null;

            for (ProjectTmTuvT tuv : tuvs)
            {
                tuv.setSid(p_newSid);

                if (editTuv != null && editTuv.getId() > tuv.getId())
                {
                    continue;
                }

                if (tuv.getLocale().getId() == targetLocaleId)
                {
                    editTuv = tuv;
                }
            }

            if (editTuv == null)
            {
                throw new WebServiceException(
                        "The specified entry do not have the target locale:"
                                + p_targetLocale);
            }

            editTuv.setSegmentString(p_targetSegment);

            HibernateUtil.save(tu);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Removes specified segment
     * 
     * @param p_accessToken
     * @param p_tmProfileName
     *            TM profile name
     * @param p_string
     * @param p_sourceLocale
     *            Source locale of TM profile
     * @param p_deleteLocale
     *            Locale which need to be removed
     * @throws WebServiceException
     */
    public void deleteSegment(String p_accessToken, String p_tmProfileName,
            String p_string, String p_sourceLocale, String p_deleteLocale)
            throws WebServiceException
    {
        deleteSegment(p_accessToken, p_tmProfileName, p_string, p_sourceLocale,
                p_deleteLocale, "false");
    }

    /**
     * Removes specified segment
     * 
     * @param p_accessToken
     * @param p_tmProfileName
     *            TM profile name
     * @param p_string
     * @param p_sourceLocale
     *            Source locale of TM profile
     * @param p_deleteLocale
     *            Locale which need to be removed
     * @param isEscape
     *            Is convert all the escapable characters into their string
     *            (escaped) equivalents or not
     * @throws WebServiceException
     */
    public void deleteSegment(String p_accessToken, String p_tmProfileName,
            String p_string, String p_sourceLocale, String p_deleteLocale,
            boolean isEscape) throws WebServiceException
    {
        String escapeString = "false";
        if (isEscape)
        {
            escapeString = "true";
        }

        deleteSegment(p_accessToken, p_tmProfileName, p_string, p_sourceLocale,
                p_deleteLocale, escapeString);
    }

    /**
     * Delete a segment or entire entry.
     * <p>
     * If <code>deleteLocale</code> is not found in the specified tm, a
     * exception will be throwed.
     * <p>
     * Note: If code>deleteLocale</code> is source locale or null, the entry
     * will be delete.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmProfileName
     *            The name of tm profile, can not be null.
     * @param string
     *            The source string, can not be null.
     * @param sourceLocale
     *            The source locale, can not be null.
     * @param deleteLocale
     *            the locale of the segment to be deleted. If it is set to null
     *            or the source locale, the entire entry including the source
     *            segment and all the target segments will be deleted from
     *            GlobalSight TM.
     * @param escapeString
     *            Is convert all the escapable characters into their string
     *            (escaped) equivalents or not, the value must be 'true' or
     *            'false'.
     * @throws WebServiceException
     */
    public void deleteSegment(String p_accessToken, String p_tmProfileName,
            String p_string, String p_sourceLocale, String p_deleteLocale,
            String escapeString) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_tmProfileName, "tm profile name");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            Assert.assertNotEmpty(p_string, "source string");
            if (escapeString == null
                    || escapeString.trim().length() == 0
                    || (!"true".equals(escapeString.trim()) && !"false"
                            .equals(escapeString.trim())))
            {
                escapeString = "false";
            }
            escapeString = escapeString.toLowerCase();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        p_sourceLocale = ImportUtil.normalizeLocale(p_sourceLocale);
        p_string = wrapSegment(p_string, Boolean.parseBoolean(escapeString));

        checkAccess(p_accessToken, "editEntry");
        checkPermission(p_accessToken, Permission.TM_EDIT_ENTRY);

        try
        {
            ProjectTmTuT tu = getTu(p_tmProfileName, p_string, p_sourceLocale);
            if (p_deleteLocale == null
                    || p_sourceLocale.equalsIgnoreCase(p_deleteLocale))
            {
                try
                {
                    HibernateUtil.delete(tu);
                }
                catch (Exception e)
                {
                    throw new WebServiceException(e.getMessage());
                }

                return;
            }
            long deleteLocaleId = getLocaleByName(p_deleteLocale).getId();
            Set<ProjectTmTuvT> tuvs = tu.getTuvs();
            Set<ProjectTmTuvT> movedTuvs = new HashSet<ProjectTmTuvT>();
            try
            {
                for (ProjectTmTuvT tuv : tuvs)
                {
                    if (deleteLocaleId == tuv.getLocale().getId())
                    {
                        movedTuvs.add(tuv);
                    }
                }

                if (tu.getTuvs().size() < movedTuvs.size() + 2)
                {
                    HibernateUtil.delete(tu);
                }
                else
                {
                    for (ProjectTmTuvT tuv : movedTuvs)
                    {
                        tu.removeTuv(tuv);
                        HibernateUtil.delete(tuv);
                    }
                    HibernateUtil.save(tu);
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new WebServiceException(e.getMessage());
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Check if current user has the specified permission
     * 
     * @param accessToken
     * @param permission
     *            Permission information
     * @throws WebServiceException
     */
    private void checkPermission(String accessToken, String permission)
            throws WebServiceException
    {
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(accessToken));
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(user.getUserId());

            if (!ps.getPermissionFor(permission))
            {
                String msg = "User " + user.getUserName()
                        + " does not have enough permission";
                throw new WebServiceException(msg);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Wraps segment with <code>segment</code> tag.
     * <p>
     * At first, convert all the escapable characters in the given string into
     * their string (escaped) equivalents if escapeString is setted to ture.
     * Then add <code>"<segment>"</code> to the first and
     * <code>"</segment>"</code> to the end.
     * 
     * @param segment
     * @param escapeString
     * @return
     */
    private String wrapSegment(String segment, boolean escapeString)
    {
        if (segment == null)
        {
            segment = "";
        }

        if (escapeString)
        {
            segment = XmlUtil.escapeString(segment);
        }

        return "<segment>" + segment + "</segment>";
    }

    /**
     * Gets ProjectTM information
     * 
     * @param tmName
     *            TM name
     * @param companyId
     *            ID of company
     * @return
     */
    private ProjectTM getProjectTm(String tmName, Long companyId)
    {
        String hql = "from ProjectTM p where p.name = :name and p.companyId = :companyId";
        HashMap params = new HashMap();
        params.put("name", tmName);
        params.put("companyId", companyId);

        return (ProjectTM) HibernateUtil.getFirst(hql, params);
    }

    private ProjectTM getProjectTm(long id)
    {
        String hql = "from ProjectTM p where id = :id";
        HashMap params = new HashMap();
        params.put("id", id);
        return (ProjectTM) HibernateUtil.getFirst(hql, params);
    }

    /**
     * Gets the ProjectTM with the specified TM profile name
     * 
     * @param p_tmProfileName
     *            TM profile name
     * @return
     * @throws WebServiceException
     */
    private Tm getProjectTm(String p_tmProfileName) throws WebServiceException
    {
        TranslationMemoryProfile tmProfile = TMProfileHandlerHelper
                .getTMProfileByName(p_tmProfileName);
        if (tmProfile == null)
        {
            throw new WebServiceException("Unable to get tm profile by name: "
                    + p_tmProfileName);
        }

        return getProjectTm(tmProfile.getProjectTmIdForSave());
    }

    /**
     * Gets locale with specified name
     * 
     * @param name
     *            Locale name
     * @return GlobalSightLocale Locale information with the specified name
     * @throws WebServiceException
     */
    private GlobalSightLocale getLocaleByName(String name)
            throws WebServiceException
    {
        name = ImportUtil.normalizeLocale(name);
        try
        {
            return ImportUtil.getLocaleByName(name);
        }
        catch (Exception e)
        {
            throw new WebServiceException("Unable to get locale: " + name);
        }
    }

    /**
     * Gets Tu information associated with speicified TM profile, segment,
     * source locale and SID
     * 
     * @param tmProfileName
     *            TM profile name
     * @param sourceString
     *            Segment
     * @param sourceLocale
     *            Source Locale
     * @param sid
     *            SID information
     * @return ProjectTmTuT Tu information
     * @throws WebServiceException
     */
    private ProjectTmTuT getTu(String tmProfileName, String sourceString,
            String sourceLocale, String sid) throws WebServiceException
    {
        Assert.assertNotNull(tmProfileName, "tm profile name");
        Assert.assertNotNull(sourceString, "source string");
        Assert.assertNotNull(sourceLocale, "source locale");

        String hql = "select p.tu from ProjectTmTuvT p "
                + "where p.locale.id = :lId and p.tu.projectTm.id=:tmId "
                + "and p.tu.sourceLocale.id = :lId "
                + "and p.segmentString = :sourceString ";
        if (sid == null)
        {
            hql += "and p.sid is null";
        }
        else
        {
            hql += "and p.sid = :sid";
        }

        Map map = new HashMap();
        map.put("lId", getLocaleByName(sourceLocale).getId());
        map.put("tmId", getProjectTm(tmProfileName).getId());
        map.put("sourceString", sourceString);

        if (sid != null)
        {
            map.put("sid", sid);
        }
        List tus = HibernateUtil.search(hql, map);

        if (tus.size() == 0)
        {
            throw new WebServiceException("No any entry was found with locale("
                    + sourceLocale + "), source string(" + sourceString
                    + ") and sid(" + sid + ")");
        }

        return (ProjectTmTuT) tus.get(0);
    }

    /**
     * Gets Tu information associated with speicified TM profile, segment and
     * source locale
     * 
     * @param tmProfileName
     *            TM profile name
     * @param sourceString
     *            Segment
     * @param sourceLocale
     *            Source Locale
     * @return ProjectTmTuT Tu information
     * @throws WebServiceException
     */
    private ProjectTmTuT getTu(String tmProfileName, String sourceString,
            String sourceLocale) throws WebServiceException
    {
        Assert.assertNotNull(tmProfileName, "tm profile name");
        Assert.assertNotNull(sourceString, "source string");
        Assert.assertNotNull(sourceLocale, "source locale");

        String hql = "select p.tu from ProjectTmTuvT p "
                + "where p.locale.id = :lId and p.tu.projectTm.id=:tmId "
                + "and p.tu.sourceLocale.id = :lId "
                + "and p.segmentString = :sourceString ";

        Map map = new HashMap();
        map.put("lId", getLocaleByName(sourceLocale).getId());
        map.put("tmId", getProjectTm(tmProfileName).getId());
        map.put("sourceString", sourceString);

        List tus = HibernateUtil.search(hql, map);

        if (tus.size() == 0)
        {
            throw new WebServiceException("No any entry was found with locale("
                    + sourceLocale + "), source string(" + sourceString + ")");
        }

        return (ProjectTmTuT) tus.get(0);
    }

    /**
     * Gets TM name associated with the specified TM ID
     * 
     * @param tmId
     *            ID of TM
     * @return String Name of project TM
     * @throws WebServiceException
     */
    private String getProjectTmName(long tmId, Connection connection)
            throws WebServiceException
    {
        PreparedStatement query = null;
        ResultSet results = null;
        String tmName = "";

        String sql = " select id, name, company_id, organization, "
                + "description, creation_date, domain, creation_user "
                + "from project_tm where id = " + tmId;
        logger.info("getProjectTmName():sql: " + sql);

        try
        {
            if (connection == null)
                connection = ConnectionPool.getConnection();

            query = connection.prepareStatement(sql);
            results = query.executeQuery();
            while (results.next())
            {
                tmName = results.getString(2);
            }
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database.";
            logger.error(message, cpe);
            message = makeErrorXml("in searchEntries()", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for TM info.";
            logger.error(message, sqle);
            message = makeErrorXml("in searchEntries()", message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("in searchEntries()", e.getMessage());
            throw new WebServiceException(message);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
        }

        return tmName;
    }

    /**
     * Get all TM profiles by current logged user
     * 
     * @param p_accessToken
     * @return String all tm profiles
     * @throws WebServiceException
     */
    public String getAllTMProfiles(String p_accessToken)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getAllTMProfiles");
        checkPermission(p_accessToken, Permission.TMP_VIEW);

        List allTMProfiles = null;
        try
        {
            allTMProfiles = TMProfileHandlerHelper.getAllTMProfiles();
        }
        catch (Exception e)
        {
            String message = "Unable to get all TM profiles.";
            logger.error(message, e);
            message = makeErrorXml("getAllTMProfiles", message);
            throw new WebServiceException(message);
        }

        StringBuffer sbXML = new StringBuffer(XML_HEAD);
        sbXML.append("<TMProfileInformation>\r\n");
        for (int i = 0; i < allTMProfiles.size(); i++)
        {
            TranslationMemoryProfile profile = (TranslationMemoryProfile) allTMProfiles
                    .get(i);
            if (profile != null)
            {
                long id = profile.getId();
                String name = profile.getName();
                String description = profile.getDescription();
                long storageTMId = profile.getProjectTmIdForSave();
                String TMName = "";
                try
                {
                    ProjectTM tm = ServerProxy.getProjectHandler()
                            .getProjectTMById(storageTMId, false);
                    TMName = tm.getName();
                }
                catch (Exception ex)
                {
                    String msg = "can't get tm object by id :" + storageTMId;
                    logger.error(msg, ex);
                }

                sbXML.append("\t<TMProfile>\r\n");
                sbXML.append("\t\t<id>" + id + "</id>\r\n");
                sbXML.append("\t\t<name>" + name + "</name>\r\n");
                sbXML.append("\t\t<description>" + description
                        + "</description>\r\n");
                sbXML.append("\t\t<storageTMName>" + TMName
                        + "</storageTMName>\r\n");
                sbXML.append("\t\t<referenceTMGrp>\r\n");
                Vector<LeverageProjectTM> tms = profile
                        .getProjectTMsToLeverageFrom();
                for (LeverageProjectTM lp_tm : tms)
                {
                    long projectTmId = lp_tm.getProjectTmId();
                    String refTmName = "";
                    try
                    {
                        ProjectTM tm = ServerProxy.getProjectHandler()
                                .getProjectTMById(projectTmId, false);
                        refTmName = tm.getName();
                    }
                    catch (Exception e)
                    {
                        // do nothing
                    }
                    sbXML.append("\t\t\t<referenceTM id=\"" + projectTmId
                            + "\">" + refTmName + "</referenceTM>\r\n");
                }
                sbXML.append("\t\t</referenceTMGrp>\r\n");
                sbXML.append("\t</TMProfile>\r\n");
            }
        }
        sbXML.append("</TMProfileInformation>\r\n");

        return sbXML.toString();
    }

    /**
     * Returns all Termbases associated with current user
     * 
     * @param p_accessToken
     * @return String An XML description which contains information for all
     *         termbases that are associated with current user
     * @throws WebServiceException
     */
    public String getAllTermbases(String p_accessToken)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getAllTermbases");
        checkPermission(p_accessToken, Permission.SERVICE_TB_GET_ALL_TB);

        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;

        StringBuffer sbXML = new StringBuffer(XML_HEAD);
        sbXML.append("<TermbaseInformation>\r\n");
        try
        {
            StringBuffer sbSql = new StringBuffer(
                    "select TBID, TB_NAME, TB_DESCRIPTION, TB_DEFINITION, COMPANYID from TB_TERMBASE ");
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                sbSql.append(" order by tbid asc ");
            }
            else
            {
                sbSql.append(" where COMPANYID = " + currentCompanyId
                        + " order by tbid asc ");
            }

            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sbSql.toString());
            results = query.executeQuery();

            while (results.next())
            {
                long tbid = results.getInt("TBID");
                String tbName = results.getString("TB_NAME");
                String des = results.getString("TB_DESCRIPTION");
                String definition = results.getString("TB_DEFINITION");
                long companyId = results.getInt("COMPANYID");
                com.globalsight.everest.company.Company com = (com.globalsight.everest.company.Company) ServerProxy
                        .getJobHandler().getCompanyById(companyId);
                String companyName = com.getCompanyName();

                sbXML.append("\t<Termbase>\r\n");
                sbXML.append("\t\t<id>" + tbid + "</id>\r\n");
                sbXML.append("\t\t<name>" + tbName + "</name>\r\n");
                sbXML.append("\t\t<description>" + des + "</description>\r\n");
                sbXML.append("\t\t<companyName>" + companyName
                        + "</companyName>\r\n");
                sbXML.append("\t</Termbase>\r\n");
            }
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database.";
            logger.error(message, cpe);
            message = makeErrorXml("getAllTermbases", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for all termbases.";
            logger.error(message, sqle);
            message = makeErrorXml("getAllTermbases", message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            String message = "Fail to get all TBs.";
            logger.error(message, e);
            message = makeErrorXml("getAllTermbases", message);
            throw new WebServiceException(message);
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }

        sbXML.append("</TermbaseInformation>\r\n");
        return sbXML.toString();
    }

    /**
     * Save one TB entry to termbase
     * <p>
     * All parameters can't be null. If the TB entry to save has been existed,
     * then edit the target term according to the source term.
     * 
     * @param p_accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param p_termbaseName
     *            The name of termbase, can not be null
     * @param p_sourceLocale
     *            The source locale to save, can not be null
     * @param p_sourceTerm
     *            The source term content to be added, can not be null
     * @param p_targetLocale
     *            The target locale to save, can not be null
     * @paream p_targetTerm The target term content to be added
     * @throws WebServiceException
     * 
     * @see #login(username, password)
     */
    public void saveTBEntry(String p_accessToken, String p_termbaseName,
            String p_sourceLocale, String p_sourceTerm, String p_targetLocale,
            String p_targetTerm) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_termbaseName, "termbase name");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            Assert.assertNotEmpty(p_sourceTerm, "source term");
            Assert.assertNotEmpty(p_targetLocale, "target locale");
            Assert.assertNotEmpty(p_targetTerm, "target term");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "saveTBEntry");
        checkPermission(p_accessToken, Permission.SERVICE_TB_CREATE_ENTRY);

        Termbase tb = getTermbase(p_termbaseName);

        // get SessionInfo object
        SessionInfo si = null;
        try
        {
            User user = ServerProxy.getUserManager().getUser(
                    getUsernameFromSession(p_accessToken));
            si = new SessionInfo(user.getUserId(), "");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();

            // get source lang name
            p_sourceLocale = ImportUtil.normalizeLocale(p_sourceLocale);
            String source_lang = p_sourceLocale.substring(0, 2);
            List sourceLangNameList = getLangNameByLocale(source_lang, tb,
                    connection);
            if (sourceLangNameList.size() == 0)
            {
                sourceLangNameList = getLangNameByLocale(source_lang, null,
                        connection);
            }
            String source_langName = source_lang;
            if (sourceLangNameList.size() > 0)
            {
                source_langName = (String) sourceLangNameList.get(0);
            }
            // get target lang name
            p_targetLocale = ImportUtil.normalizeLocale(p_targetLocale);
            String target_lang = p_targetLocale.substring(0, 2);
            List targetLangNameList = getLangNameByLocale(p_targetLocale, tb,
                    connection);
            if (targetLangNameList.size() == 0)
            {
                targetLangNameList = getLangNameByLocale(p_targetLocale, null,
                        connection);
            }
            String target_langName = target_lang;
            if (targetLangNameList.size() > 0)
            {
                target_langName = (String) targetLangNameList.get(0);
            }

            boolean isExist = isExistInTermbase(tb, source_langName,
                    p_sourceTerm, target_langName, p_targetTerm, connection);
            if (isExist == false)
            {
                StringBuffer sbEntry = new StringBuffer(XML_HEAD);
                sbEntry.append("<conceptGrp>\r\n");

                sbEntry.append("\t<languageGrp>\r\n");
                sbEntry.append("\t\t<language name='" + source_langName
                        + "' locale='" + p_sourceLocale.substring(3, 5)
                        + "'/>\r\n");
                sbEntry.append("\t\t<termGrp>\r\n");
                sbEntry.append("\t\t\t<term>" + p_sourceTerm + "</term>\r\n");
                sbEntry.append("\t\t</termGrp>\r\n");
                sbEntry.append("\t</languageGrp>\r\n");

                sbEntry.append("\t<languageGrp>\r\n");
                sbEntry.append("\t\t<language name='" + target_langName
                        + "' locale='" + p_targetLocale.substring(3, 5)
                        + "'/>\r\n");
                sbEntry.append("\t\t<termGrp>\r\n");
                sbEntry.append("\t\t\t<term>" + p_targetTerm + "</term>\r\n");
                sbEntry.append("\t\t</termGrp>\r\n");
                sbEntry.append("\t</languageGrp>\r\n");

                sbEntry.append("</conceptGrp>\r\n");

                // s_logger.info("entry = " + sbEntry.toString());

                tb.addEntry(sbEntry.toString(), si);
            }
            else
            {
                editTBEntry(p_accessToken, p_termbaseName, p_sourceLocale,
                        p_sourceTerm, p_targetLocale, p_targetTerm, connection);
            }
        }
        catch (Exception e)
        {
            logger.error("Error found in editEntry().", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException e)
            {
                logger.error("Cannot release database connection correctly.", e);
            }
        }
    }

    /**
     * Search terms in termbase
     * 
     * @param p_paccessToken
     *            the accessToken received from login() method; Not null;
     * @param p_termbaseName
     *            the termbase name to search in; can not be null.
     * @param p_searchString
     *            the string to be used to search; can not be null.
     * @param p_sourceLocale
     *            the source locale the search string will be searched in. Not
     *            null;
     * @param p_targetLocale
     *            Search for this locale,can be null.If null,all matched
     *            language/terms are returned.
     * @param p_matchType
     *            1:exact matching search; 2:fuzzy matching search
     * @return String: xml file format
     * 
     * @throws WebServiceException
     */
    public String searchTBEntries(String p_accessToken, String p_termbaseName,
            String p_searchString, String p_sourceLocale,
            String p_targetLocale, double p_matchType)
            throws WebServiceException
    {
        // 1.Parameters check
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_termbaseName, "termbase name");
            Assert.assertNotEmpty(p_searchString, "search string");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            String matchType = new Double(p_matchType).toString();
            Assert.assertNotEmpty(matchType, "match type");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "searchTBEntries");
        // checkPermission(p_accessToken, Permission.SERVICE_TB_SEARCH_ENTRY);

        // 2.Get termbase by termbase name
        Termbase searchTB = null;
        try
        {
            if (p_termbaseName != null && !"".equals(p_termbaseName.trim()))
            {
                searchTB = TermbaseList.get(p_termbaseName);
            }
        }
        catch (Exception e)
        {
            String msg = "Can't find termbase for " + p_termbaseName;
            msg = makeErrorXml("searchTBEntries", msg);
            throw new WebServiceException(msg);
        }

        // 3.Valid source language name list
        String searchType = (p_matchType == 1 ? "exact" : "fuzzy");
        List validSrcLangNameList = new ArrayList();
        List validTrgLangNameList = new ArrayList();
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            validSrcLangNameList = getValidLangNameList(p_sourceLocale,
                    searchTB, searchType, connection);

            // 4.Valid target language name list (can be empty)
            if (p_targetLocale != null && !"".equals(p_targetLocale.trim()))
            {
                validTrgLangNameList = getValidLangNameList(p_targetLocale,
                        searchTB, searchType, connection);
            }
        }
        catch (Exception e)
        {
            logger.error("Error found in searchEntries(..).", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (Exception e2)
            {
                logger.error("Cannot release database connection correctly.",
                        e2);
            }
        }

        // 5.Search every termbase and form the reresult xml.
        StringBuffer sbXML = new StringBuffer(XML_HEAD);
        sbXML.append("<tbEntries>\r\n");

        if (validSrcLangNameList != null && validSrcLangNameList.size() > 0)
        {
            Iterator srcLangIter = validSrcLangNameList.iterator();
            while (srcLangIter.hasNext())
            {
                String source_locale = (String) srcLangIter.next();
                String target_locale = null;

                // "p_targetLocale" is null or empty
                if (p_targetLocale == null || "".equals(p_targetLocale.trim()))
                {
                    String emptyTrgSearch = searchHitlist(source_locale, null,
                            p_searchString, searchType, searchTB);
                    sbXML.append(emptyTrgSearch == null ? "" : emptyTrgSearch);
                }
                else
                {
                    // "p_targetLocale" is not null
                    if (validTrgLangNameList != null
                            && validTrgLangNameList.size() > 0)
                    {
                        Iterator trgLangIter = validTrgLangNameList.iterator();
                        while (trgLangIter.hasNext())
                        {
                            target_locale = (String) trgLangIter.next();
                            String searchResult = searchHitlist(source_locale,
                                    target_locale, p_searchString, searchType,
                                    searchTB);
                            sbXML.append(searchResult == null ? ""
                                    : searchResult);
                        }
                    }
                }
            }
        }

        sbXML.append("</tbEntries>\r\n");

        return sbXML.toString();
    }

    /**
     * For fuzzy search, the language name must be defined in TB languages.
     */
    private List getValidLangNameList(String p_langName, Termbase p_searchTB,
            String p_searchType, Connection connection)
    {
        if (p_langName == null || p_searchTB == null || p_searchType == null)
        {
            return new ArrayList();
        }

        List<String> result = new ArrayList();

        List candidateLangNameList = getLangNameByLocale(p_langName,
                p_searchTB, connection);
        if (candidateLangNameList != null && candidateLangNameList.size() > 0)
        {
            // If exact search, don't check if "p_locale" is in the definition
            // of TB(Exact search does not need indexing)
            if ("exact".equals(p_searchType))
            {
                result = candidateLangNameList;
            }
            else
            {
                Iterator iter = candidateLangNameList.iterator();
                while (iter.hasNext())
                {
                    String langName = (String) iter.next();
                    String locale = p_searchTB.getLocaleByLanguage(langName);
                    // Current TB has defined this language name.
                    if (locale != null)
                    {
                        result.add(langName);
                    }
                }
            }
        }

        result = filterLangNameList(p_langName, result);

        return result;
    }

    /**
     * Filter the language name list by specified locale. Termbase does not care
     * "country" code, but for some locales, must differ the "country" such as
     * "zh_CN" and "zh_TW".
     * 
     * @param p_locale
     * @param p_langNameList
     * @return
     */
    private List filterLangNameList(String p_locale, List p_langNameList)
    {
        if (p_locale == null || p_langNameList == null
                || p_langNameList.size() == 0)
        {
            return p_langNameList;
        }

        List result = new ArrayList();

        String lang = "";
        String country = "";
        p_locale = ImportUtil.normalizeLocale(p_locale);
        int index = p_locale.indexOf("_");
        if (index == -1)
        {
            index = p_locale.indexOf("-");
        }
        if (index > -1)
        {
            lang = p_locale.substring(0, index);
            country = p_locale.substring(index + 1, p_locale.length());
        }

        Iterator it = p_langNameList.iterator();
        while (it.hasNext())
        {
            String langName = (String) it.next();

            boolean flag = true;
            // "zh_CN" && "zh_TW"
            if ("zh".equals(lang) && "CN".equalsIgnoreCase(country)
                    && langName.length() >= 5
                    && "zh".equalsIgnoreCase(langName.substring(0, 2))
                    && "TW".equalsIgnoreCase(langName.substring(3, 5)))
            {
                flag = false;
            }
            if ("zh".equals(lang) && "TW".equalsIgnoreCase(country)
                    && langName.length() >= 5
                    && "zh".equalsIgnoreCase(langName.substring(0, 2))
                    && "CN".equalsIgnoreCase(langName.substring(3, 5)))
            {
                flag = false;
            }
            // TODO:we can add more "flag" judgement here for other langs.
            // ...

            if (flag)
            {
                result.add(langName);
            }
        }

        return result;
    }

    /**
     * For "searchTBEntries(...)" API only.
     */
    private String searchHitlist(String p_srcLang, String p_trgLang,
            String p_searchString, String p_searchType, Termbase p_searchTB)
    {
        if (p_srcLang == null || p_searchType == null || p_searchTB == null)
        {
            return null;
        }

        StringBuffer sbXML = new StringBuffer();
        Hitlist hitList = p_searchTB.searchHitlist(p_srcLang, p_trgLang,
                p_searchString, p_searchType, 300, 0);
        Iterator hitIter = hitList.getHits().iterator();
        while (hitIter.hasNext())
        {
            Hit hit = (Hit) hitIter.next();
            sbXML.append("\t<tbEntry>\r\n");
            sbXML.append("\t\t<tbName>" + p_searchTB.getName()
                    + "</tbName>\r\n");
            TbConcept concept = HibernateUtil.get(TbConcept.class,
                    hit.getConceptId());
            Iterator langIter = concept.getLanguages().iterator();
            while (langIter.hasNext())
            {
                TbLanguage tl = (TbLanguage) langIter.next();
                Iterator termsIter = tl.getTerms().iterator();
                while (termsIter.hasNext())
                {
                    TbTerm term = (TbTerm) termsIter.next();
                    String termContent = term.getTermContent();
                    String language = term.getLanguage();
                    boolean isSrc = false;
                    if (language.equals(p_srcLang))
                    {
                        isSrc = true;
                    }
                    // If target language is not empty, only write source and
                    // target terms; If target language is empty, write all.
                    if (p_trgLang == null || "".equals(p_trgLang)
                            || language.equalsIgnoreCase(p_trgLang)
                            || language.equalsIgnoreCase(p_srcLang))
                    {
                        sbXML.append("\t\t<term isSrc=\"" + isSrc + "\">\r\n");
                        sbXML.append("\t\t\t<lang_name>" + language
                                + "</lang_name>\r\n");
                        sbXML.append("\t\t\t<termContent>" + termContent
                                + "</termContent>\r\n");
                        sbXML.append("\t\t</term>\r\n");
                    }
                }
            }
            sbXML.append("\t</tbEntry>\r\n");
        }

        return sbXML.toString();
    }

    /**
     * Edit term info in termbase
     * 
     * @param p_accessToken
     *            the accessToken received from login() method; Not null;
     * @param p_termbaseName
     *            the termbase name to create entry in; Not null;
     * @param p_sourceLocale
     *            the source locale to be searched in; Not null;
     * @param p_sourceTerm
     *            the source term to be used to search in source contents; Not
     *            null;
     * @param p_targetLocale
     *            the target Locale to be edited; Not null;
     * @param p_targetTerm
     *            the target term to replace the old term content for
     *            p_targetLocale; Not null;
     * 
     * @throws WebServiceException
     */
    public void editTBEntry(String p_accessToken, String p_termbaseName,
            String p_sourceLocale, String p_sourceTerm, String p_targetLocale,
            String p_targetTerm, Connection connection)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_termbaseName, "termbase name");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
            Assert.assertNotEmpty(p_sourceTerm, "source term");
            Assert.assertNotEmpty(p_targetLocale, "target locale");
            Assert.assertNotEmpty(p_targetTerm, "target term");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        p_sourceTerm = p_sourceTerm.replace("'", "\\'");
        p_targetTerm = p_targetTerm.replace("'", "\\'");

        checkAccess(p_accessToken, "editTBEntry");
        checkPermission(p_accessToken, Permission.SERVICE_TB_EDIT_ENTRY);

        // get termbase object
        Termbase tb = getTermbase(p_termbaseName);
        long tbid = tb.getId();

        PreparedStatement query = null;
        ResultSet results = null;
        long cid = -1;

        boolean needCloseConnection = false;
        try
        {
            if (connection == null)
            {
                connection = ConnectionPool.getConnection();
                needCloseConnection = true;
            }

            connection.setAutoCommit(false);
            String sourceLangName = "";
            String targetLangName = "";
            if (getLangNameByLocale(p_sourceLocale, tb, connection).size() > 0)
            {
                sourceLangName = (String) getLangNameByLocale(p_sourceLocale,
                        tb, connection).get(0);
            }
            else
            {
                String msg = "can't find language name for " + p_sourceLocale;
                msg = makeErrorXml("editTBEntry", msg);
                throw new WebServiceException(msg);
            }

            if (getLangNameByLocale(p_targetLocale, tb, connection).size() > 0)
            {
                targetLangName = (String) getLangNameByLocale(p_targetLocale,
                        tb, connection).get(0);
            }
            else
            {
                String msg = "can't find language name for " + p_targetLocale;
                msg = makeErrorXml("editTBEntry", msg);
                throw new WebServiceException(msg);
            }

            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append("select cid from tb_term " + " where lang_name = '"
                    + targetLangName + "' and cid in ("
                    + " SELECT CID FROM TB_TERM WHERE TBID = " + tbid
                    + " AND LANG_NAME = '" + sourceLangName + "' "
                    + " AND TERM = '" + p_sourceTerm + "')");

            logger.info("SQL for 'cid' :" + sbSQL.toString() + "\r\n");
            query = connection.prepareStatement(sbSQL.toString());
            results = query.executeQuery();
            // get the first CID default
            if (results.next())
            {
                cid = results.getLong("CID");
            }

            if (cid != -1)
            {
                String strSQL = "UPDATE TB_TERM SET TERM = '" + p_targetTerm
                        + "'" + " WHERE LANG_NAME = '" + targetLangName + "'"
                        + " AND CID = " + cid;

                logger.info("SQL for updating TB_TERM = " + strSQL + "\r\n");

                query = connection.prepareStatement(strSQL);
                query.execute();
            }
            else
            {
                String msg = "no matched term found.";
                throw new WebServiceException(msg);
            }
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database.";
            logger.error(message, cpe);
            message = makeErrorXml("editTBEntry", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for TB entries.";
            logger.error(message, sqle);
            message = makeErrorXml("editTBEntry", message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("editTBEntry", e.getMessage());
            throw new WebServiceException(message);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            if (needCloseConnection)
            {
                ConnectionPool.silentReturnConnection(connection);
            }
        }
    }

    /**
     * Delete an entry from termbase
     * 
     * @param p_accessToken
     * @param p_termbaseName
     * @param p_searchString
     * @param p_sourceLocale
     * @param p_targetLocale
     * @throws WebServiceException
     */
    public void deleteTBEntry(String p_accessToken, String p_termbaseName,
            String p_searchString, String p_sourceLocale, String p_targetLocale)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_termbaseName, "termbase name");
            Assert.assertNotEmpty(p_searchString, "search locale");
            Assert.assertNotEmpty(p_sourceLocale, "source locale");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "deleteTBEntry");
        checkPermission(p_accessToken, Permission.SERVICE_TB_EDIT_ENTRY);

        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;

        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            // get termbase object
            Termbase tb = getTermbase(p_termbaseName);
            long tbid = -1;
            if (tb != null)
            {
                tbid = tb.getId();
            }

            String source_lang_name = "";
            if (getLangNameByLocale(p_sourceLocale, tb, connection).size() > 0)
            {
                source_lang_name = (String) getLangNameByLocale(p_sourceLocale,
                        tb, connection).get(0);
            }
            else
            {
                String msg = "can't find language name for " + p_sourceLocale;
                msg = makeErrorXml("deleteTBEntry", msg);
                throw new WebServiceException(msg);
            }

            String target_lang_name = "";
            if (p_targetLocale != null && !"".equals(p_targetLocale.trim()))
            {
                if (getLangNameByLocale(p_targetLocale, tb, connection).size() > 0)
                {
                    target_lang_name = (String) getLangNameByLocale(
                            p_targetLocale, tb, connection).get(0);
                }
                else
                {
                    String msg = "can't find language name for "
                            + p_targetLocale;
                    msg = makeErrorXml("deleteTBEntry", msg);
                    throw new WebServiceException(msg);
                }
            }

            String sql1 = "SELECT TBID, TID, LID, CID, LANG_NAME, TERM FROM TB_TERM "
                    + " WHERE TBID = "
                    + tbid
                    + " AND LANG_NAME = '"
                    + source_lang_name
                    + "'"
                    + " AND TERM = '"
                    + p_searchString
                    + "'";
            long cid = -1;

            query = connection.prepareStatement(sql1);
            results = query.executeQuery();
            if (results.next())
            {
                cid = results.getInt("CID");
            }
            else
            {
                String msg = "No matched term is found!";
                throw new WebServiceException(msg);
            }

            String sql2 = "SELECT LANG_NAME FROM TB_TERM WHERE CID = " + cid
                    + " AND TBID = " + tbid;
            query = connection.prepareStatement(sql2);
            results = query.executeQuery();
            int langNum = 0;
            boolean isTargetLang = false;
            while (results.next())
            {
                langNum++;
                if (!"".equals(target_lang_name.trim())
                        && results.getString("LANG_NAME").equals(
                                target_lang_name))
                {
                    isTargetLang = true;
                }
            }

            String sql3 = "";
            String sql4 = "";
            String sql5 = "";
            // delete all terms
            if (target_lang_name == null
                    || target_lang_name.equals(source_lang_name)
                    || (target_lang_name != null
                            && !target_lang_name.equals(source_lang_name)
                            && isTargetLang == true && langNum <= 2))
            {
                sql3 = "DELETE FROM TB_TERM WHERE TBID = " + tbid
                        + " AND CID = " + cid;
                sql4 = "DELETE FROM TB_LANGUAGE WHERE TBID = " + tbid
                        + " AND CID = " + cid;
                sql5 = "DELETE FROM TB_CONCEPT WHERE TBID = " + tbid
                        + " AND CID = " + cid;
            }
            // delete target term related data only
            if (target_lang_name != null
                    && !target_lang_name.equals(source_lang_name)
                    && isTargetLang == true && langNum > 2)
            {
                sql3 = "DELETE FROM TB_TERM WHERE TBID = " + tbid
                        + " AND CID = " + cid + " AND LANG_NAME = '"
                        + target_lang_name + ";";
                sql4 = "DELETE FROM TB_LANGUAGE WHERE TBID = " + tbid
                        + " AND CID = " + cid + " AND NAME = '"
                        + target_lang_name + "'";
                sql5 = "";
            }
            // there is no data to be deleted
            if (target_lang_name != null
                    && !target_lang_name.equals(source_lang_name)
                    && isTargetLang == false)
            {
                String msg = "no target term is found!";
                throw new WebServiceException(msg);
            }
            if (!"".equals(sql3))
            {
                query = connection.prepareStatement(sql3);
                query.execute();
            }
            if (!"".equals(sql4))
            {
                query = connection.prepareStatement(sql4);
                query.execute();
            }
            if (!"".equals(sql5))
            {
                query = connection.prepareStatement(sql5);
                query.execute();
            }
            connection.commit();
        }
        catch (Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }
    }

    /**
     * Get a Termbase object by termbase name
     * 
     * @param p_termbaseName
     * @return Termbase object
     * @throws WebServiceException
     */
    private Termbase getTermbase(String p_termbaseName)
            throws WebServiceException
    {
        // get termbase object
        Termbase tb = null;
        try
        {
            tb = TermbaseList.get(p_termbaseName);
        }
        catch (Exception e)
        {
            String message = "Fail to get a termbase for termbase :"
                    + p_termbaseName;
            logger.error(message, e);
            throw new WebServiceException(message);
        }

        return tb;
    }

    /**
     * Get language name according to input locale
     * 
     * @param queryLocale
     *            to be input by user
     * @return lang name
     */
    private List getLangNameByLocale(String queryLocale, Termbase tb,
            Connection connection)
    {
        List langList = new ArrayList();

        if (queryLocale == null || "".equals(queryLocale.trim()))
        {
            return langList;
        }

        String lang_country = ImportUtil.normalizeLocale(queryLocale);
        String lang = "";
        String country = "";
        try
        {
            lang = lang_country.substring(0, 2);// en
            // country = lang_country.substring(3,5);//US
        }
        catch (Exception ex)
        {
            String msg = "invalid locale : " + queryLocale;
            logger.error(msg, ex);
        }

        PreparedStatement query = null;
        ResultSet results = null;

        try
        {
            if (connection == null)
                connection = ConnectionPool.getConnection();

            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append("SELECT DISTINCT NAME FROM TB_LANGUAGE "
                    + " WHERE LOCALE LIKE '%" + lang + "%' ");
            if (tb != null)
            {
                sbSQL.append(" AND TBID = " + tb.getId());
            }
            query = connection.prepareStatement(sbSQL.toString());
            results = query.executeQuery();
            while (results.next())
            {
                langList.add(results.getString("NAME"));
            }
        }
        catch (Exception e)
        {
            String message = "Fail to get lang_name for " + queryLocale;
            logger.error(message, e);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
        }

        return langList;
    }

    /**
     * Get a most possible language name in the candidated "p_langNameList" for
     * specified locale (need "country" matched). For "exact" search,a language
     * name that is not in the definition is allowed as exact search need not
     * indexing; For "fuzzy" search,the language name must be in the definition.
     * 
     * @param p_langNameList
     *            - All language name list in DB.
     * @param p_locale
     *            - The languge name to search.
     * @param p_tb
     *            - The termbase to search.
     * @param p_searchType
     *            - "exact" or "fuzzy".
     * 
     * @return
     */
    private String getMostPossibleLocale(List<String> p_langNameList,
            String p_locale, Termbase p_tb, String p_searchType)
    {
        if (p_langNameList == null || p_langNameList.size() == 0
                || p_locale == null)
        {
            return null;
        }

        // If exact search, don't check if "p_locale" is in the definition of
        // TB(Exact search does not need indexing)
        List validLangNameList = new ArrayList();
        if ("exact".equalsIgnoreCase(p_searchType))
        {
            validLangNameList = p_langNameList;
        }
        else
        {
            Iterator iter1 = p_langNameList.iterator();
            while (iter1.hasNext() && p_tb != null)
            {
                String langName = (String) iter1.next();
                String locale = p_tb.getLocaleByLanguage(langName);
                if (locale != null)
                {
                    validLangNameList.add(langName);
                }
            }
        }

        // Find a more possible language name country matched.
        String result = null;
        if (validLangNameList.size() > 0)
        {
            // Default the first one
            result = (String) validLangNameList.get(0);
            String locale = ImportUtil.normalizeLocale(p_locale);
            String country = null;
            int index = locale.indexOf("_");
            if (index == -1)
            {
                index = locale.indexOf("-");
            }
            if (index > -1 && index + 1 < locale.length())
            {
                country = locale.substring(index + 1, locale.length());
            }

            Iterator iter2 = validLangNameList.iterator();
            while (iter2.hasNext())
            {
                String langName = (String) iter2.next();
                if (langName.endsWith(country))
                {
                    result = langName;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Get research rusults in XML format for searchEntries() API
     * 
     * @param ResultSet
     *            rs
     * @return String : xml format
     * @deprecated it was for "searchTBEntries(...)",no use now.
     */
    private String getResearchResultsInXML(ResultSet rs)
    {
        StringBuffer sbXML = new StringBuffer(XML_HEAD);
        sbXML.append("<tbEntries>\r\n");
        try
        {
            int count_mark = 0;
            long cid_mark = -1;
            while (rs.next())
            {
                count_mark++;
                long tbid = rs.getInt("TBID");
                Termbase tb = TermbaseList.get(tbid);
                String termbaseName = tb.getName();

                long cid = rs.getInt("CID");
                String lang_name = rs.getString("LANG_NAME");
                String term = rs.getString("TERM");
                String isSrc = rs.getString("ISSRC");

                if (cid_mark == -1)
                {
                    sbXML.append("\t<tbEntry>\r\n");
                    sbXML.append("\t\t<tbName>" + termbaseName
                            + "</tbName>\r\n");
                }
                if (cid_mark != -1 && cid_mark != cid)
                {
                    sbXML.append("\t</tbEntry>\r\n");
                    sbXML.append("\t<tbEntry>\r\n");
                    sbXML.append("\t\t<tbName>" + termbaseName
                            + "</tbName>\r\n");
                }
                sbXML.append("\t\t<term isSrc=\"" + isSrc + "\">\r\n");
                sbXML.append("\t\t\t<lang_name>" + lang_name
                        + "</lang_name>\r\n");
                sbXML.append("\t\t\t<termContent>" + term
                        + "</termContent>\r\n");
                sbXML.append("\t\t</term>\r\n");

                cid_mark = cid;//
            }
            if (count_mark > 0)
            {
                sbXML.append("\t</tbEntry>\r\n");
            }
            else
            {
                sbXML.append("\tNo matched results.\r\n");
            }
            sbXML.append("</tbEntries>\r\n");
        }
        catch (SQLException sqle)
        {
            String message = "Fail to transfer TB entries info to XML format.";
            logger.error(message, sqle);
            message = makeErrorXml("searchTBEntries", message);
        }

        return sbXML.toString();
    }

    /**
     * Releases the resource created to DB operations
     * 
     * @param results
     *            Resource for ResultSet object
     * @param query
     *            PreparedStatement object for querying
     * @param connection
     *            Database connection
     */
    private void releaseDBResource(ResultSet results, PreparedStatement query,
            Connection connection)
    {
        // close ResultSet
        if (results != null)
        {
            try
            {
                results.close();
            }
            catch (Exception e)
            {
                logger.error("Closing ResultSet", e);
            }
        }
        // close PreparedStatement
        if (query != null)
        {
            try
            {
                query.close();
            }
            catch (Exception e)
            {
                logger.error("Closing query", e);
            }
        }
        // close Connection
        returnConnection(connection);
    }

    /**
     * judge if the specified term pairs in termbase
     * 
     * @param tb
     *            Termbase
     * @param p_sourceLangName
     * @param p_sourceTerm
     * @param p_targetLangName
     * @param p_targetTerm
     * @return
     * @throws WebServiceException
     */
    private boolean isExistInTermbase(Termbase tb, String p_sourceLangName,
            String p_sourceTerm, String p_targetLangName, String p_targetTerm,
            Connection connection) throws WebServiceException
    {
        boolean isExist = false;

        PreparedStatement query = null;
        ResultSet results = null;

        String sql = " select cid from tb_term where lang_name = '"
                + p_targetLangName + "' " + " and cid in ("
                + " select cid from tb_term where tbid = " + tb.getId()
                + " and lang_name = '" + p_sourceLangName + "' "
                + " and term = '" + p_sourceTerm + "')";

        logger.info("isExistInTermbase():sql: " + sql);
        try
        {
            if (connection == null)
                connection = ConnectionPool.getConnection();

            query = connection.prepareStatement(sql);
            results = query.executeQuery();
            while (results.next())
            {
                isExist = true;
            }
        }
        catch (ConnectionPoolException cpe)
        {
            String message = "Unable to connect to database.";
            logger.error(message, cpe);
            message = makeErrorXml("saveTBEntry", message);
            throw new WebServiceException(message);
        }
        catch (SQLException sqle)
        {
            String message = "Unable to query DB for TB entries.";
            logger.error(message, sqle);
            message = makeErrorXml("saveTBEntry", message);
            throw new WebServiceException(message);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("saveTBEntry", e.getMessage());
            throw new WebServiceException(message);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
        }

        return isExist;
    }

    /**
     * Gets the first tu with the source locale and target locale in the
     * specified tm.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmName
     *            TM name, will used to get tm id.
     * @param companyName
     *            company name, will used to get tm id.
     * @param sourceLocale
     *            source locale.
     * @param targetLocale
     *            target locale.
     * @return either -1 for no TU's to fetch or TMX format of TU which has the
     *         min id
     * 
     * @throws WebServiceException
     */
    public String getFirstTu(String accessToken, String tmName,
            String companyName, String sourceLocale, String targetLocale)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(accessToken, "access token");
            Assert.assertNotEmpty(tmName, "tm name");
            Assert.assertNotEmpty(companyName, "company name");
            Assert.assertNotEmpty(sourceLocale, "source locale");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(accessToken, "getFirstTu");
        checkPermission(accessToken, Permission.TM_SEARCH);

        ProjectTmTuT tu;
        List<GlobalSightLocale> targetLocales;
        try
        {
            String hql = "from Company where name = :name";
            HashMap map = new HashMap();
            map.put("name", companyName);
            Company company = (Company) HibernateUtil.getFirst(hql, map);
            if (company == null)
            {
                throw new WebServiceException(
                        "Can not find the company with name (" + companyName
                                + ")");
            }
            ProjectTM tm = getProjectTm(tmName, company.getIdAsLong());
            if (tm == null)
            {
                throw new WebServiceException(
                        "Can not find the tm with tm name (" + tmName
                                + ") and company name (" + companyName + ")");
            }
            tu = null;
            map = new HashMap();
            if (targetLocale != null && targetLocale.length() > 0)
            {
                hql = "select tuv.tu from ProjectTmTuvT tuv "
                        + "where tuv.locale.id = :tId "
                        + "and tuv.tu.projectTm.id = :tmId "
                        + "and tuv.tu.sourceLocale.id = :sId "
                        + "order by tuv.tu.id asc";
                map.put("tId", getLocaleByName(targetLocale).getId());
            }
            else
            {
                hql = "from ProjectTmTuT tu where tu.projectTm.id = :tmId "
                        + "and tu.sourceLocale.id = :sId order by tu.id asc";
            }
            map.put("tmId", tm.getId());
            map.put("sId", getLocaleByName(sourceLocale).getId());
            List<ProjectTmTuT> tus = (List<ProjectTmTuT>) HibernateUtil.search(
                    hql, map, 0, 1);
            if (tus == null || tus.size() == 0)
            {
                return null;
            }
            tu = tus.get(0);
            targetLocales = null;
            if (targetLocale != null && targetLocale.trim().length() > 0)
            {
                targetLocales = new ArrayList<GlobalSightLocale>();
                targetLocales.add(getLocaleByName(targetLocale));
            }

            return tu.convertToTmx(targetLocales);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Search tus according to the specified tu.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param sourceLocale
     *            The source lcoale.
     * @param targetLocale
     *            The target locale.
     * @param maxSize
     *            The max size of return tus.
     * @param tuId
     *            The id of specified tu. The specified tu is used to get the tm
     *            id.
     * @return A tmx format string, including all tus' information.
     * 
     * @throws WebServiceException
     */
    public String nextTus(String accessToken, String sourceLocale,
            String targetLocale, String maxSize, String tuId)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(accessToken, "access token");
            Assert.assertNotEmpty(sourceLocale, "source locale");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(accessToken, "nextTus");
        checkPermission(accessToken, Permission.TM_SEARCH);

        int size = Integer.parseInt(maxSize);
        if (size < 1)
        {
            size = 1;
        }
        if (size > 10000)
        {
            size = 10000;
        }

        StringBuilder result;
        try
        {
            // Gets specified tu.
            ProjectTmTuT tu = HibernateUtil.get(ProjectTmTuT.class,
                    Long.parseLong(tuId));
            if (tu == null)
            {
                throw new WebServiceException("Can not find tu with id: "
                        + tuId);
            }
            String hql = null;
            HashMap map = new HashMap();
            map.put("tmId", tu.getProjectTm().getId());
            map.put("sId", getLocaleByName(sourceLocale).getId());
            map.put("fId", tu.getId());
            if (targetLocale != null && targetLocale.length() > 0)
            {
                hql = "select tuv.tu from ProjectTmTuvT tuv where tuv.locale.id = :tId "
                        + "and tuv.tu.projectTm.id = :tmId "
                        + "and tuv.tu.sourceLocale.id = :sId "
                        + "and tuv.tu.id > :fId order by tuv.tu.id asc";
                map.put("tId", getLocaleByName(targetLocale).getId());
            }
            else
            {
                hql = "from ProjectTmTuT tu where tu.projectTm.id = :tmId "
                        + "and tu.sourceLocale.id = :sId "
                        + "and tu.id > :fId order by tu.id asc";
            }
            List<ProjectTmTuT> tus = (List<ProjectTmTuT>) HibernateUtil.search(
                    hql, map, 0, size);
            if (tus == null || tus.size() == 0)
            {
                return "-1";
            }
            List<GlobalSightLocale> targetLocales = null;
            if (targetLocale != null)
            {
                targetLocales = new ArrayList<GlobalSightLocale>();
                targetLocales.add(getLocaleByName(targetLocale));
            }
            result = new StringBuilder();
            for (ProjectTmTuT pTu : tus)
            {
                result.append(pTu.convertToTmx(targetLocales));
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }

        return result.toString();
    }

    /**
     * Normalizes the spelling of the "lang" elements.
     * 
     * @param p_tu
     *            Element
     * @throws Exception
     */
    private void normalizeTu(Element p_tu) throws Exception
    {
        // Header default source lang normalized when header is read.
        // Locales read from m_options were normalized by TmxReader.

        String lang = p_tu.attributeValue(Tmx.SRCLANG);
        if (lang != null)
        {
            lang = ImportUtil.normalizeLocale(lang);
            p_tu.addAttribute(Tmx.SRCLANG, lang);
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");
        for (int i = 0, max = nodes.size(); i < max; i++)
        {
            Element elem = (Element) nodes.get(i);

            lang = elem.attributeValue(Tmx.LANG);
            lang = ImportUtil.normalizeLocale(lang);

            elem.addAttribute(Tmx.LANG, lang);
        }
    }

    /**
     * Validates a TU by checking it contains a TUV in a source language that
     * should be imported. Also checks if there are more than 2 TUVs.
     * 
     * @param p_tu
     *            Element
     * @throws Exception
     */
    private void validateTu(Element p_tu) throws Exception
    {
        boolean b_found = false;

        String tuvLang = null;
        String srcLang = p_tu.attributeValue(Tmx.SRCLANG);
        if (srcLang == null)
        {
            srcLang = "en_US";
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");

        if (nodes.size() < 2)
        {
            throw new Exception(
                    "TU contains less than 2 TUVs (after filtering), ignoring");
        }

        for (int i = 0; i < nodes.size(); i++)
        {
            Element elem = (Element) nodes.get(i);
            tuvLang = elem.attributeValue(Tmx.LANG);
            if (tuvLang.equalsIgnoreCase(srcLang))
            {
                b_found = true;
                break;
            }
        }

        if (!b_found)
        {
            throw new Exception("TU is missing TUV in source language "
                    + srcLang);
        }
    }

    /**
     * Converts a DOM TU to a GS SegmentTmTu, thereby converting any TMX format
     * specialities as best as possible.
     * 
     * @param p_root
     *            Element
     * @throws Exception
     */
    private void reconvert(Element p_root) throws Exception
    {
        // Original TU id, if known
        String id = p_root.attributeValue(Tmx.TUID);
        ProjectTmTuT tu = null;
        try
        {
            if (id != null && id.length() > 0)
            {
                long lid = Long.parseLong(id);
                tu = HibernateUtil.get(ProjectTmTuT.class, lid);
                if (tu == null)
                {
                    throw new Exception("Can not find tu with id: " + lid);
                }
            }
            else
            {
                throw new Exception("Can not find tu id");
            }
            // Datatype of the TU (html, javascript etc)
            String format = p_root.attributeValue(Tmx.DATATYPE);
            if (format == null || format.length() == 0)
            {
                format = "html";
            }
            tu.setFormat(format.trim());
            // Locale of Source TUV (use default from header)
            String lang = p_root.attributeValue(Tmx.SRCLANG);
            if (lang == null || lang.length() == 0)
            {
                lang = "en_US";
            }
            String locale = ImportUtil.normalizeLocale(lang);
            LocaleManagerLocal manager = new LocaleManagerLocal();
            tu.setSourceLocale(manager.getLocaleByString(locale));
            // Segment type (text, css-color, etc)
            String segmentType = "text";
            Node node = p_root.selectSingleNode(".//prop[@type = '"
                    + Tmx.PROP_SEGMENTTYPE + "']");
            if (node != null)
            {
                segmentType = node.getText();
            }
            tu.setType(segmentType);
            // Sid
            node = p_root.selectSingleNode(".//prop[@type= '"
                    + Tmx.PROP_TM_UDA_SID + "']");
            if (node != null)
            {
                tu.setSid(node.getText());
            }
            // TUVs
            List nodes = p_root.elements("tuv");
            for (int i = 0; i < nodes.size(); i++)
            {
                Element elem = (Element) nodes.get(i);
                ProjectTmTuvT tuv = new ProjectTmTuvT();
                tuv.reconvertFromTmx(elem, tu);

                // Check the locale
                List<ProjectTmTuvT> savedTuvs = new ArrayList<ProjectTmTuvT>();
                for (ProjectTmTuvT savedTuv : tu.getTuvs())
                {
                    if (savedTuv.getLocale().equals(tuv.getLocale()))
                    {
                        savedTuvs.add(savedTuv);
                    }
                }

                if (savedTuvs.size() == 0)
                {
                    throw new WebServiceException(
                            "Can not find tuv with tu id: " + tu.getId()
                                    + ", locale: " + tuv.getLocale());
                }

                // More than one tuv have the locale, than go to check the
                // create
                // date.
                if (savedTuvs.size() > 1)
                {
                    boolean find = false;
                    for (ProjectTmTuvT savedTuv : savedTuvs)
                    {
                        if (savedTuv.getCreationDate().getTime() == tuv
                                .getCreationDate().getTime())
                        {
                            find = true;
                            savedTuv.merge(tuv);
                            HibernateUtil.save(savedTuv);
                        }
                    }
                    if (!find)
                    {
                        throw new WebServiceException(
                                "Can not find tuv with tu id: " + tu.getId()
                                        + ", locale: " + tuv.getLocale()
                                        + ", creation date:"
                                        + tuv.getCreationDate());
                    }
                }
                else
                {
                    ProjectTmTuvT savedTuv = savedTuvs.get(0);
                    savedTuv.merge(tuv);
                    HibernateUtil.save(savedTuv);
                }
            }
            HibernateUtil.save(tu);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Updates a tu in database.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmx
     *            A tmx formate string inlcluding all tu information.
     * @return
     * @throws WebServiceException
     */
    public String editTu(String accessToken, String tmx)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(accessToken, "access token");
            Assert.assertNotEmpty(tmx, "tmx format");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(accessToken, "editEntry");
        checkPermission(accessToken, Permission.TM_EDIT_ENTRY);

        SAXReader reader = new SAXReader();

        ElementHandler handler = new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();
                element.detach();

                try
                {
                    normalizeTu(element);
                    validateTu(element);
                    reconvert(element);
                }
                catch (Throwable ex)
                {
                    logger.error(ex.getMessage(), ex);
                    throw new ThreadDeath();
                }
            }
        };

        reader.addHandler("/tu", handler);

        try
        {
            reader.read(new StringReader(tmx));
        }
        catch (DocumentException e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return "true";
    }

    /**
     * Returns all permissions information for current user
     * 
     * @param p_accessToken
     * @return String An XML description which contains all permissions
     *         information for current user
     * @throws WebServiceException
     */
    public String getAllPermissionsByUser(String p_accessToken)
            throws WebServiceException
    {
        String strReturn = "";
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getAllPermissionsByUser");

        try
        {
            // get permission set for current user
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(p_accessToken));
            String userId = user.getUserId();
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(userId);
            // convert permissions into Map (permissionName:bitValue)
            HashMap allPermsInMap = getAllPermissionsInMap(ps);
            // get returned XML
            strReturn = getAllPermissionsInXML(allPermsInMap);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return strReturn;
    }

    /**
     * Gets all permissions information for specified set of permissions
     * 
     * @param ps
     *            Permission set
     * @return HashMap Collection of permissions
     */
    private HashMap getAllPermissionsInMap(PermissionSet ps)
    {
        HashMap mapReturn = new HashMap();
        HashMap allPerms = Permission.getAllPermissions();
        Set keySet = allPerms.keySet();

        if (ps != null)
        {
            String strPS = ps.toString();
            // s_logger.info("PermissionSet for current user : " + strPS);

            String s = strPS.substring(1, strPS.length() - 1);
            String ids[] = s.split("\\|");
            for (int i = 0; i < ids.length; i++)
            {
                Iterator keyIt = keySet.iterator();
                long id = Long.parseLong(ids[i]);
                while (keyIt.hasNext())
                {
                    String key = (String) keyIt.next();
                    long tempId = ((Long) allPerms.get(key)).longValue();
                    if (tempId == id)
                    {
                        mapReturn.put(key, Long.valueOf(tempId));
                    }
                }
            }
        }

        return mapReturn;
    }

    /**
     * Generates XML description with the map of permissions
     * 
     * @param map
     *            Contains collection of permissions
     * @return String An XML description which contains all permissions
     *         information
     */
    private String getAllPermissionsInXML(HashMap map)
    {
        StringBuffer sbXML = new StringBuffer(XML_HEAD);
        sbXML.append("<Permissions>\r\n");
        if (map != null && map.size() > 0)
        {
            Set<Map.Entry<String, Long>> enterySet = map.entrySet();
            for (Map.Entry<String, Long> ent : enterySet)
            {
                String key = ent.getKey();
                String id = (ent.getValue()).toString();
                sbXML.append("\t<PermissionGrp>\r\n");
                sbXML.append("\t\t<id>" + id + "</id>\r\n");
                sbXML.append("\t\t<name>" + key + "</name>\r\n");
                sbXML.append("\t</PermissionGrp>\r\n");
            }
        }
        sbXML.append("</Permissions>\r\n");

        return sbXML.toString();
    }

    /**
     * Gets all source locales associated with current user
     * 
     * @param p_accessToken
     * @return String An XML description which contains all source locales
     *         information that were defined by current user
     * @throws WebServiceException
     */
    public String getSourceLocales(String p_accessToken)
            throws WebServiceException
    {
        StringBuffer strReturn = new StringBuffer(XML_HEAD);
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getSourceLocale");

        try
        {
            // get permission set for current user
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(p_accessToken));
            String userId = user.getUserId();
            if (!UserUtil.isInPermissionGroup(userId,
                    Permission.GROUP_ADMINISTRATOR)
                    && !UserUtil.isInPermissionGroup(userId,
                            Permission.GROUP_PROJECT_MANAGER))
                throw new WebServiceException(
                        "Current user has not permissions to get all source locales");

            LocaleManagerLocal lml = new LocaleManagerLocal();
            ArrayList locales = new ArrayList(
                    lml.getAllSourceLocalesByCompanyId(CompanyWrapper
                            .getCompanyIdByName(user.getCompanyName())));
            GlobalSightLocale locale = null;
            strReturn.append("<root>\r\n");
            for (int i = 0; i < locales.size(); i++)
            {
                locale = (GlobalSightLocale) locales.get(i);
                strReturn.append("<locale>\r\n<id>").append(locale.getId())
                        .append("</id>\r\n");
                strReturn.append("<name>").append(locale.getDisplayName())
                        .append("</name>\r\n</locale>\r\n");
            }
            strReturn.append("</root>");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return strReturn.toString();
    }

    /**
     * Gets target locales which are associated with speicfied source locale
     * 
     * @param p_accessToken
     * @param p_sourceLocale
     *            Source locale information
     * @return String An XML description which contains all target locales
     *         associated with specified source locale in current company
     * @throws WebServiceException
     */
    public String getTargetLocales(String p_accessToken, String p_sourceLocale)
            throws WebServiceException
    {
        StringBuffer strReturn = new StringBuffer(XML_HEAD);
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getTargetLocales");

        try
        {
            // get permission set for current user
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(p_accessToken));
            String userId = user.getUserId();
            if (!UserUtil.isInPermissionGroup(userId,
                    Permission.GROUP_ADMINISTRATOR)
                    && !UserUtil.isInPermissionGroup(userId,
                            Permission.GROUP_PROJECT_MANAGER))
                throw new WebServiceException(
                        "Current user has not permissions to get target locales");

            LocaleManagerLocal lml = new LocaleManagerLocal();
            GlobalSightLocale sourceLocale = lml
                    .getLocaleByString(p_sourceLocale);
            ArrayList locales = new ArrayList(lml.getTargetLocalesByCompanyId(
                    sourceLocale,
                    CompanyWrapper.getCompanyIdByName(user.getCompanyName())));
            GlobalSightLocale locale = null;
            strReturn.append("<root>\r\n");
            for (int i = 0; i < locales.size(); i++)
            {
                locale = (GlobalSightLocale) locales.get(i);
                strReturn.append("<locale>\r\n<id>").append(locale.getId())
                        .append("</id>\r\n");
                strReturn.append("<name>").append(locale.getDisplayName())
                        .append("</name>\r\n</locale>\r\n");
            }
            strReturn.append("</root>");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return strReturn.toString();
    }

    /**
     * Gets the URI for database connection
     * 
     * @param p_accessToken
     * @return String URI for database connection
     * @throws WebServiceException
     */
    public String getConnection(String p_accessToken)
            throws WebServiceException
    {
        StringBuffer strReturn = new StringBuffer();
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getConnection");
        checkPermission(p_accessToken, Permission.CVS_MODULE_MAPPING);

        try
        {
            Properties pr = new Properties();
            InputStream is = ConnectionPool.class
                    .getResourceAsStream("/properties/db_connection.properties");
            pr.load(is);
            String url = pr.getProperty("connect_string");
            String username = pr.getProperty("user_name");
            String password = pr.getProperty("password");
            strReturn.append(url)
                    .append("?useUnicode=true&characterEncoding=UTF-8")
                    .append(",").append(username).append(",").append(password);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return strReturn.toString();
    }

    /**
     * Gets the priority by speicified L10N
     * 
     * @param p_accessToken
     * @param p_l10nID
     *            ID of L10N
     * @return String priority associated with speicified L10N
     * @throws WebServiceException
     */
    public String getPriorityByID(String p_accessToken, String p_l10nID)
            throws WebServiceException
    {
        StringBuffer strReturn = new StringBuffer();
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getTargetLocales");

        try
        {
            ProjectHandlerLocal handler = new ProjectHandlerLocal();
            BasicL10nProfile basicL10nProfile = (BasicL10nProfile) handler
                    .getL10nProfile(Long.parseLong(p_l10nID));
            String userId = getUsernameFromSession(p_accessToken);
            if (!UserUtil.isSuperAdmin(userId)
                    && !UserUtil.isSuperPM(userId)
                    && !UserUtil.isInPermissionGroup(userId,
                            Permission.GROUP_ADMINISTRATOR)
                    && !UserUtil.isInPermissionGroup(userId,
                            Permission.GROUP_PROJECT_MANAGER))
                throw new WebServiceException(
                        "Current user has not permissions to get the priority information");
            strReturn.append(basicL10nProfile.getPriority());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return strReturn.toString();
    }

    /**
     * Gets all attributes information with specified job ID
     * 
     * @param p_accessToken
     * @param p_jobId
     *            ID of job
     * @return String An XML description which contains all attributes
     *         information with specified job
     * @throws WebServiceException
     */
    public String getAttributesByJobId(String p_accessToken, long p_jobId)
            throws WebServiceException
    {
        return getAttributesByJobId(p_accessToken, Long.valueOf(p_jobId));
    }

    /**
     * Gets all attributes information with specified job ID
     * 
     * @param p_accessToken
     * @param p_jobId
     *            ID of job
     * @return String An XML description which contains all attributes
     *         information with specified job
     * @throws WebServiceException
     */
    public String getAttributesByJobId(String p_accessToken, Long p_jobId)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotNull(p_jobId, "project id");

            checkAccess(p_accessToken, "getAttributesByJobId");
            // checkPermission(p_accessToken, Permission.JOB_ATTRIBUTE_VIEW);

            JobImpl job = HibernateUtil.get(JobImpl.class, p_jobId);
            Assert.assertFalse(job == null, "Can not find job by id: "
                    + p_jobId);
            Assert.assertFalse(
                    !isInSameCompany(getUsernameFromSession(p_accessToken),
                            String.valueOf(job.getCompanyId())),
                    "Cannot access the job which is not in the same company with current user");

            List<JobAttribute> jobAttributes = job.getAllJobAttributes();

            Attributes allAttributes = new Attributes();
            if (jobAttributes != null)
            {
                for (JobAttribute attribute : jobAttributes)
                {
                    JobAttributeVo vo = AttributeUtil
                            .getJobAttributeVo(attribute);
                    allAttributes.addAttribute(vo);
                }
            }

            allAttributes.sort();

            return com.globalsight.cxe.util.XmlUtil.object2String(
                    allAttributes, true);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Sets job attribute.
     * 
     * @param accessToken
     * @param jobId
     * @param attInternalName
     * @param value
     *            <ul>
     *            <li>Text: String.
     *            <li>Float: Float.
     *            <li>Integer: Integer.
     *            <li>Date: Date.
     *            <li>Choice List: String or List&lt;String&gt;
     *            <li>Files: Map&lt;String, byte[]&gt;. the key is file name and
     *            the value is file content.
     *            </ul>
     * @throws WebServiceException
     */
    public void setJobAttribute(String accessToken, long jobId,
            String attInternalName, Object value) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(accessToken, "access token");
            Assert.assertNotEmpty(attInternalName, "attribute internal name");

            checkAccess(accessToken, "setJobAttribute");
            checkPermission(accessToken, Permission.JOB_ATTRIBUTE_EDIT);

            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
            Assert.assertFalse(job == null, "Can not find job by id: " + jobId);

            String userName = getUsernameFromSession(accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            if (!isInSameCompany(userName, String.valueOf(job.getCompanyId())))
            {
                String message = "Current user is not in the same company with the job.";
                throw new WebServiceException(makeErrorXml("setJobAttribute",
                        message));
            }

            List<JobAttribute> jobAtts = job.getAllJobAttributes();
            JobAttribute jobAttribute = null;
            boolean find = false;
            for (JobAttribute jobAtt : jobAtts)
            {
                if (attInternalName.equals(jobAtt.getAttribute().getName())
                        && value != null)
                {
                    jobAtt.setValue(value, false);
                    jobAttribute = jobAtt;
                    HibernateUtil.saveOrUpdate(jobAtt);
                    break;
                }
            }

            if (jobAttribute == null)
            {
                throw new IllegalArgumentException(
                        "Can not find job attribte by internal name: "
                                + attInternalName);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Gets attribute value with specified job id and attribute internal name.
     * 
     * @param accessToken
     * @param jobId
     *            ID of job
     * @param attInternalName
     *            Attribute internal name
     * @return An XML description which contains attribute information with
     *         specified job and attribute name
     * @throws WebServiceException
     */
    public String getJobAttribute(String accessToken, long jobId,
            String attInternalName) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(attInternalName, "access token");
            checkPermission(accessToken, Permission.JOB_ATTRIBUTE_VIEW);

            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
            Assert.assertFalse(job == null, "Can not find job by id: " + jobId);
            Assert.assertFalse(
                    !isInSameCompany(getUsernameFromSession(accessToken),
                            String.valueOf(job.getCompanyId())),
                    "Cannot access the job which is not in the same company with current user");

            List<JobAttribute> jobAttributes = job.getAllJobAttributes();

            if (jobAttributes != null)
            {
                for (JobAttribute attribute : jobAttributes)
                {
                    if (attInternalName.equals(attribute.getAttribute()
                            .getName()))
                    {
                        JobAttributeVo vo = AttributeUtil
                                .getJobAttributeVo(attribute);
                        return com.globalsight.cxe.util.XmlUtil.object2String(
                                vo, true);
                    }
                }
            }

            throw new WebServiceException("Can not find the job attribute");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Gets the information of all attributes with specified project
     * 
     * @param p_accessToken
     * @param p_projectId
     *            ID of project
     * @return An XML description which contains information of all attributes
     *         with specified project
     * @throws WebServiceException
     */
    public String getAttributesByProjectId(String p_accessToken,
            long p_projectId) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            checkAccess(p_accessToken, "getTargetLocales");
            // checkPermission(p_accessToken, Permission.PROJECTS_VIEW);

            ProjectImpl project = HibernateUtil.get(ProjectImpl.class,
                    p_projectId);

            Assert.assertFalse(project == null, "Can not find project by id: "
                    + p_projectId);

            String userName = getUsernameFromSession(p_accessToken);

            Assert.assertFalse(
                    !isInSameCompany(userName,
                            String.valueOf(project.getCompanyId())),
                    "Cannot access the project which is not in the same company with current user");

            AttributeSet attributeSet = project.getAttributeSet();
            Attributes allAttributes = new Attributes();
            if (attributeSet != null)
            {
                Set<Attribute> attributes = attributeSet.getAttributes();
                for (Attribute attribute : attributes)
                {
                    JobAttributeVo vo = AttributeUtil.getAttributeVo(attribute
                            .getCloneAttribute());
                    allAttributes.addAttribute(vo);
                }
            }

            allAttributes.sort();

            return com.globalsight.cxe.util.XmlUtil.object2String(
                    allAttributes, true);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Gets project ID with specified file profile
     * 
     * @param p_accessToken
     * @param p_fileProfileId
     *            ID of file profile
     * @return long ID of project which is associated by the file profile
     * @throws WebServiceException
     */
    public long getProjectIdByFileProfileId(String p_accessToken,
            Long p_fileProfileId) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotNull(p_fileProfileId, "file profile id");
            // checkPermission(p_accessToken, Permission.PROJECTS_VIEW);
            FileProfile fp = HibernateUtil.get(FileProfileImpl.class,
                    p_fileProfileId, false);
            Assert.assertFalse(fp == null, "Can not get fileprofile by id: "
                    + p_fileProfileId);

            Project project = getProject(fp);
            Assert.assertFalse(project == null,
                    "Can not get project by file profile id: "
                            + p_fileProfileId);
            return project.getId();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Gets project ID with specified file profile
     * 
     * @param p_accessToken
     * @param p_fileProfileId
     *            ID of file profile
     * @return long ID of project which is associated by the file profile
     * @throws WebServiceException
     */
    public long getProjectIdByFileProfileId(String p_accessToken,
            long p_fileProfileId) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            // checkPermission(p_accessToken, Permission.PROJECTS_VIEW);

            FileProfile fp = HibernateUtil.get(FileProfileImpl.class,
                    Long.valueOf(p_fileProfileId), false);
            Assert.assertFalse(fp == null, "Can not get fileprofile by id: "
                    + p_fileProfileId);

            Project project = getProject(fp);
            Assert.assertFalse(project == null,
                    "Can not get project by file profile id: "
                            + p_fileProfileId);
            return project.getId();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Uploads attribute files.
     * 
     * @param p_accessToken
     * @param jobName
     * @param attInternalName
     * @param fileName
     * @param bytes
     * @throws WebServiceException
     */
    public void uploadAttributeFiles(String p_accessToken, String jobName,
            String attInternalName, String fileName, byte[] bytes)
            throws WebServiceException
    {
        try
        {
            checkAccess(p_accessToken, "updateAttributeFiles");
            if (!validateJobName(jobName))
            {
                throw new WebServiceException(makeErrorXml(
                        "uploadAttributeFiles", ERROR_JOB_NAME));
            }

            String path = jobName + "/" + attInternalName + "/" + fileName;
            writeAttributeFile(path, bytes);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Writes attribute file to the attribute directory.
     * 
     * @param path
     *            Attribute file path to save
     * @param bytes
     *            Content of attributes
     * @throws WebServiceException
     */
    private void writeAttributeFile(String path, byte[] bytes)
            throws WebServiceException
    {
        File newFile = new File(AmbFileStoragePathUtils.getJobAttributeDir(),
                path);
        newFile.getParentFile().mkdirs();
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(newFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
            logger.error(
                    "Could not copy uploaded file to the attribute directory.",
                    e);
            String message = "Could not copy uploaded file to the attribute directory."
                    + e.getMessage();
            message = makeErrorXml("copyFileToAttributeDirectory", message);
            throw new WebServiceException(message);
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                logger.error(
                        "Could not copy uploaded file to the attribute directory.",
                        e);
                String message = "Could not copy uploaded file to the attribute directory."
                        + e.getMessage();
                message = makeErrorXml("copyFileToAttributeDirectory", message);
                throw new WebServiceException(message);
            }
        }
    }

    /**
     * Get xliff file profiles id-name map.
     * 
     * @param p_accessToken
     * @return HashMap (Long:String)
     * @throws WebServiceException
     */
    public HashMap getXliffFileProfile(String p_accessToken)
            throws WebServiceException
    {
        HashMap xliffFPMap = new HashMap();
        checkAccess(p_accessToken, "getXliffFileProfile");
        checkPermission(p_accessToken, Permission.FILE_PROFILES_VIEW);

        Iterator fileProfileIter = null;
        try
        {
            Collection results = ServerProxy.getFileProfilePersistenceManager()
                    .getAllFileProfiles();
            fileProfileIter = results.iterator();
        }
        catch (Exception e)
        {
            String message = "Unable to get file profiles from db.";
            logger.error(message, e);
            message = makeErrorXml("getXliffFileProfile", message);
            throw new WebServiceException(message);
        }

        while (fileProfileIter.hasNext())
        {
            FileProfile fp = (FileProfile) fileProfileIter.next();
            long knownFormatTypeId = fp.getKnownFormatTypeId();
            // 39 : Xliff
            if (knownFormatTypeId == 39)
            {
                boolean hasXlfAsExtension = false;
                Vector fileExtensionIds = fp.getFileExtensionIds();
                Iterator iter = null;
                if (fileExtensionIds != null && fileExtensionIds.size() > 0)
                {
                    iter = fileExtensionIds.iterator();
                }
                if (iter == null)
                    continue;
                while (iter.hasNext())
                {
                    long fileExtensionId = ((Long) iter.next()).longValue();
                    try
                    {
                        FileExtensionImpl fileExtension = ServerProxy
                                .getFileProfilePersistenceManager()
                                .getFileExtension(fileExtensionId);
                        String extName = fileExtension.getName();
                        if (extName != null
                                && ("xlf".equals(extName) || "xliff"
                                        .equals(extName)))
                        {
                            hasXlfAsExtension = true;
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }

                if (hasXlfAsExtension)
                {
                    xliffFPMap.put(fp.getId(), fp.getName());
                }
            }
        }

        return xliffFPMap;
    }

    /**
     * Judge if there is workflow defined for specified source-target locales in
     * specified file profile.
     * 
     * @param p_accessToken
     *            String
     * @param p_fileProfileId
     * @param p_srcLangCountry
     * @param p_trgLangCountry
     * @return "yes" or "no"
     * @throws WebServiceException
     */
    public String isSupportCurrentLocalePair(String p_accessToken,
            String p_fileProfileId, String p_srcLangCountry,
            String p_trgLangCountry) throws WebServiceException
    {
        String yesOrNo = "no";
        checkAccess(p_accessToken, "isSupportCurrentLocalePair");

        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);

            long fpID = Long.parseLong(p_fileProfileId);
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .getFileProfileById(fpID, false);
            if (!isInSameCompany(userName, fp.getCompanyId())
                    && !UserUtil.isSuperAdmin(userId)
                    && !UserUtil.isSuperPM(userId))
            {
                throw new WebServiceException(
                        makeErrorXml(
                                "isSupportCurrentLocalePair",
                                "Current user has not permissions or in the same company with the file profile."));
            }
            long l10nProfileId = fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    l10nProfileId);
            Collection wfInfos = lp.getWorkflowTemplateInfos();
            Iterator wfInfosIter = wfInfos.iterator();
            while (wfInfosIter.hasNext())
            {
                WorkflowTemplateInfo wfInfo = (WorkflowTemplateInfo) wfInfosIter
                        .next();
                GlobalSightLocale srcLocale = wfInfo.getSourceLocale();
                String srcLangCountry = srcLocale.getLanguage() + "_"
                        + srcLocale.getCountry();
                GlobalSightLocale trgLocale = wfInfo.getTargetLocale();
                String trgLangCountry = trgLocale.getLanguage() + "_"
                        + trgLocale.getCountry();

                if (srcLangCountry.equals(p_srcLangCountry)
                        && trgLangCountry.equals(p_trgLangCountry))
                {
                    yesOrNo = "yes";
                    break;
                }
            }

        }
        catch (Exception ex)
        {
            String message = "Fail to judge if file profile(ID:"
                    + p_fileProfileId + ") has workflow for "
                    + p_srcLangCountry + "-" + p_trgLangCountry;
            logger.error(message, ex);
            message = makeErrorXml("getWorkFlowInfo", message);
            throw new WebServiceException(message);
        }

        return yesOrNo;
    }

    /**
     * Upload original files to GlobalSight Edition server.
     * 
     * <p>
     * Following informations must be included in <code>args</code>.
     * <ul>
     * <li>accessToken --- String</li>
     * <li>jobName ------- String</li>
     * <li>targetLocale ----- String</li>
     * <li>filePath -------String (with file name in it)</li>
     * <li>bytes --------- byte[]</li>
     * <li>filePath ------ String</li>
     * </ul>
     * 
     * @param args
     * @throws WebServiceException
     */
    public String uploadOriginalSourceFile(HashMap args)
            throws WebServiceException
    {
        try
        {
            // Checks authority.
            String accessToken = (String) args.get("accessToken");
            checkAccess(accessToken, "uploadOriginalSourceFile");
            checkPermission(accessToken,
                    Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            // Reads parameters.
            String jobName = (String) args.get("jobName");
            if (!validateJobName(jobName))
            {
                throw new WebServiceException(makeErrorXml(
                        "uploadOriginalSourceFile", ERROR_JOB_NAME));
            }

            String targetLocale = (String) args.get("targetLocale");// like
                                                                    // "fr_FR"
            String filePath = (String) args.get("filePath");// with file name in
                                                            // it
            byte[] bytes = (byte[]) args.get("bytes");

            // Save file.
            StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                    .getInstance().getStringParameter(
                            SystemConfigParamNames.FILE_STORAGE_DIR));

            // The full path is like this:
            // Welocalize\FileStorage\qa\GlobalSight\OriginalSourceFile\<target_locale>\<file_name_with_extension>
            fileStorageRoot = fileStorageRoot.append(File.separator)
                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                    .append(File.separator)
                    .append(WebAppConstants.ORIGINAL_SORUCE_FILE)
                    .append(File.separator).append(jobName)
                    .append(File.separator).append(targetLocale)
                    .append(File.separator).append(filePath);

            writeFileToLocale(fileStorageRoot.toString(), bytes);

            return null;
        }
        catch (Exception e)
        {
            String message = makeErrorXml("uploadOriginalSourceFile",
                    e.getMessage());
            return message;
        }
    }

    /**
     * Upload task attached files to edition server as job reference files.
     * 
     * @param p_args
     *            Collections of files information
     * @throws WebServiceException
     */
    public String uploadCommentReferenceFiles(HashMap p_args)
            throws WebServiceException
    {
        try
        {
            String accessToken = (String) p_args.get("accessToken");
            checkAccess(accessToken, "uploadCommentReferenceFiles");
            checkPermission(accessToken,
                    Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            String fileName = (String) p_args.get("fileName");
            String originalTaskId = (String) p_args.get("originalTaskId");
            String wsdlUrl = (String) p_args.get("wsdlUrl");
            byte[] bytes = (byte[]) p_args.get("bytes");
            String access = (String) p_args.get("access");
            if (access == null
                    || (!access.equals(CommentUpload.GENERAL) && !access
                            .equals(CommentUpload.RESTRICTED)))
            {
                access = CommentUpload.GENERAL;
            }

            // search comment id
            long commentId = 0;
            if (originalTaskId != null && wsdlUrl != null)
            {
                StringBuffer hql = new StringBuffer();
                hql.append("from CommentImpl");
                List newCommentList = HibernateUtil.search(hql.toString());
                if (newCommentList != null && newCommentList.size() > 0)
                {
                    Iterator iter = newCommentList.iterator();
                    while (iter.hasNext())
                    {
                        Comment comment = (Comment) iter.next();
                        String _originalId = comment.getOriginalId();
                        String _originalWsdlUrl = comment.getOriginalWsdlUrl();
                        if (originalTaskId.equals(_originalId)
                                && wsdlUrl.equals(_originalWsdlUrl))
                        {
                            commentId = comment.getId();
                        }
                    }
                }
            }

            // Save file to comment reference path
            StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                    .getInstance().getStringParameter(
                            SystemConfigParamNames.FILE_STORAGE_DIR));

            // The full path is like this:
            // Welocalize\FileStorage\qa\GlobalSight\CommentReference\<comment_id>\<access>\<file_name_with_extension>
            fileStorageRoot = fileStorageRoot.append(File.separator)
                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                    .append(File.separator).append("CommentReference")
                    .append(File.separator).append(commentId)
                    .append(File.separator).append(access)
                    .append(File.separator).append(fileName);

            writeFileToLocale(fileStorageRoot.toString(), bytes);

            return null;
        }
        catch (Exception e)
        {
            logger.error("Error found in uploadCommentReferenceFiles", e);
            return makeErrorXml("uploadCommentReferenceFiles", e.getMessage());
        }
    }

    /**
     * Write file
     * 
     * @param p_filePath
     *            File path to save
     * @param p_bytes
     *            Content which need to be saved
     * @throws WebServiceException
     */
    private void writeFileToLocale(String p_filePath, byte[] p_bytes)
            throws WebServiceException
    {
        File newFile = new File(p_filePath);
        newFile.getParentFile().mkdirs();
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(newFile, true);
            fos.write(p_bytes);
        }
        catch (Exception e)
        {
            logger.error(
                    "Could not copy uploaded file to the specified directory.",
                    e);
            String message = "Could not copy uploaded file to the specified directory."
                    + e.getMessage();
            throw new WebServiceException(message);
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                logger.error("Fail to close FileOutPutSteam.", e);
                String message = "Fail to close FileOutPutSteam."
                        + e.getMessage();
                throw new WebServiceException(message);
            }
        }
    }

    /**
     * Update specified task status to specified state
     * 
     * @param p_taskId
     *            : task Id
     * 
     * @param p_state
     *            : task state number, available values: 3 : "ACTIVE" (for
     *            "Available" tasks) 8 : "ACCEPTED" (for "In Progress" tasks) 81
     *            : "DISPATCHED_TO_TRANSLATION" 82 : "IN_TRANSLATION" 83 :
     *            "TRANSLATION_COMPLETED" -1 : "COMPLETED" (for "Finished"
     *            tasks) 4 : "DEACTIVE" (for "Rejected" tasks)
     * @see com.globalsight.everest.taskmanager.Task
     * 
     *      As the three state are all task inner state, no need more actions
     *      such as recalculating estimated completed time etc.
     * 
     */
    public void updateTaskState(String p_accessToken, String p_taskId,
            String p_state) throws WebServiceException
    {
        checkAccess(p_accessToken, "updateTaskState");
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_EDIT);

        long taskId = 0;
        int state = 0;
        try
        {
            taskId = Long.parseLong(p_taskId);
            state = Integer.parseInt(p_state);
        }
        catch (NumberFormatException e)
        {
            logger.error("Invalid taskId or state.", e);
            String message = makeErrorXml("updateActivityState",
                    "Invalid taskId or state.");
            throw new WebServiceException(message);
        }

        try
        {
            Task task = TaskPersistenceAccessor.getTask(taskId, true);
            task.setState(state);

            TaskPersistenceAccessor.updateTask(task);
        }
        catch (TaskException te)
        {
            String msg = "Failed to update task status for task Id : "
                    + p_taskId + " and state : " + p_state;
            logger.error(msg, te);
            msg = makeErrorXml("updateActivityState", msg);
            throw new WebServiceException(msg);
        }
    }

    /**
     * Upload translated files to server for offline uploading purpose.
     * 
     * This should be invoked before importOfflineTargetFiles() API.
     * 
     * @param p_accessToken
     *            The String of access token.
     * @param p_originalTaskId
     *            The String of task id which is from the target server.
     * @param p_bytes
     *            The contents in bytes to be uploaded.
     * @throws WebServiceException
     */
    public String uploadEditionFileBack(String p_accessToken,
            String p_originalTaskId, String p_fileName, byte[] p_bytes)
            throws WebServiceException
    {
        try
        {
            checkAccess(p_accessToken, "uploadEditionFileBack");
            checkPermission(p_accessToken,
                    Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            // Save file to comment reference path
            StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                    .getInstance().getStringParameter(
                            SystemConfigParamNames.FILE_STORAGE_DIR));

            // The full path is like this:
            // Welocalize\FileStorage\qa\GlobalSight\tmp\<original_task_id>\<file_name_with_extension>
            fileStorageRoot = fileStorageRoot.append(File.separator)
                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                    .append(File.separator).append("tmp")
                    .append(File.separator).append(p_originalTaskId)
                    .append(File.separator).append(p_fileName);

            File newFile = new File(fileStorageRoot.toString());

            if (newFile.exists())
            {
                newFile.delete();
            }

            writeFileToLocale(fileStorageRoot.toString(), p_bytes);

            return null;
        }
        catch (Exception e)
        {
            logger.error("Error found in uploadEditionFileBack", e);
            return makeErrorXml("uploadEditionFileBack", e.getMessage());
        }
    }

    /**
     * Offline uploading support.
     * 
     * Before invoking this, uploadEditionFileBack() API should be invoked.
     * After invoking this, sendSegmentCommentBack() API should be invoked.
     * 
     * @param p_accessToken
     * @param p_originalTaskId
     *            Task ID
     * @return String If the method works fine, then it will return null.
     *         Otherwise it will return error message.
     * @throws WebServiceException
     */
    public String importOfflineTargetFiles(String p_accessToken,
            String p_originalTaskId) throws WebServiceException
    {
        String userName = null;
        User userObj = null;
        Task task = null;
        try
        {
            checkAccess(p_accessToken, "importOfflineTargetFiles");

            // User object
            userName = this.getUsernameFromSession(p_accessToken);
            userObj = this.getUser(userName);

            // Task object
            TaskManager taskManager = ServerProxy.getTaskManager();
            task = taskManager.getTask(Long.parseLong(p_originalTaskId));
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
            return makeErrorXml("importOfflineTargetFiles",
                    "Cannot get Task info. " + ex.getMessage());
        }

        // OfflineEditManager
        OfflineEditManager OEM = null;
        try
        {
            OEM = ServerProxy.getOfflineEditManager();
            OEM.attachListener(new OEMProcessStatus());
        }
        catch (Exception e)
        {
            logger.error("importOfflineTargetFiles", e);
            return makeErrorXml("importOfflineTargetFiles",
                    "Cannot get OfflineEditManager instancee " + e.getMessage());
        }

        OfflineFileUploadStatus status = OfflineFileUploadStatus.getInstance();

        StringBuilder errorMessage = new StringBuilder(XML_HEAD);

        // uploaded files path
        StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                .getInstance().getStringParameter(
                        SystemConfigParamNames.FILE_STORAGE_DIR));
        // The full path is like this:
        // Welocalize\FileStorage\qa\GlobalSight\tmp\<original_task_id>
        fileStorageRoot = fileStorageRoot.append(File.separator)
                .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                .append(File.separator).append("tmp").append(File.separator)
                .append(p_originalTaskId);

        File parentFilePath = new File(fileStorageRoot.toString());
        File[] files = parentFilePath.listFiles();
        File file = null;
        String fileName = null;
        if (files != null && files.length > 0)
        {
            for (int i = 0; i < files.length; i++)
            {
                file = files[i];
                fileName = file.getName();
                try
                {
                    OEM.processUploadPage(file, userObj, task, fileName);
                }
                catch (Exception e)
                {
                    logger.error("Cannot handle file " + fileName, e);
                    status.addFileState(Long.valueOf(p_originalTaskId),
                            fileName, "Failed");
                    errorMessage.append("<file>\r\n");
                    errorMessage.append("\t<name>\r\n");
                    errorMessage.append("\t\t").append(fileName)
                            .append("</name>\r\n");
                    errorMessage.append("\t<error>\r\n");
                    errorMessage.append("\t\t").append(e.getMessage())
                            .append("</error>\r\n");
                }
            }

            // If there is error in handling files, then return message and
            // do not need to change workflow as below
            if (!errorMessage.toString().equals(XML_HEAD))
            {
                return errorMessage.toString();
            }

            // if current task/activity has no "condition",advance to next
            // activity
            // automatically.(GBS-1244)
            String destinationArrow = null;
            String availableUserId = null;
            try
            {
                // Find the user to complete task.
                WorkflowTaskInstance wfTask = ServerProxy
                        .getWorkflowServer()
                        .getWorkflowTaskInstance(
                                UserUtil.getUserIdByName(userName),
                                task.getId(), WorkflowConstants.TASK_ALL_STATES);
                task.setWorkflowTask(wfTask);
                List allAssignees = task.getAllAssignees();
                if (allAssignees != null && allAssignees.size() > 0)
                {
                    availableUserId = (String) allAssignees.get(0);
                }
                else
                {
                    availableUserId = UserUtil.getUserIdByName(userName);
                }

                List condNodeInfo = task.getConditionNodeTargetInfos();
                if (condNodeInfo == null
                        || (condNodeInfo != null && condNodeInfo.size() < 1))
                {
                    ServerProxy.getTaskManager().completeTask(availableUserId,
                            task, destinationArrow, null);
                }
            }
            catch (Exception ex)
            {
                return makeErrorXml("importOfflineTargetFiles",
                        "Cannot change workflow status. " + ex.getMessage());
            }
        }
        else
        {
            logger.info("There is no any files in upload path.");
            return makeErrorXml("importOfflineTargetFiles",
                    "Cannot find any files in uploading path");
        }

        return null;
    }

    /**
     * Offline uploading support.
     * 
     * Before invoking this, uploadEditionFileBack() API should be invoked.
     * After invoking this, sendSegmentCommentBack() API should be invoked.
     * 
     * @param p_accessToken
     * @param p_originalTaskId
     *            Task ID
     * @return String If the method works fine, then it will return null.
     *         Otherwise it will return error message.
     * @throws WebServiceException
     */
    public String importOfflineKitFiles(String p_accessToken,
            String p_originalTaskId) throws WebServiceException
    {
        String userName = null;
        User userObj = null;
        Task task = null;
        try
        {
            checkAccess(p_accessToken, "importOfflineTargetFiles");

            // User object
            userName = this.getUsernameFromSession(p_accessToken);
            userObj = this.getUser(userName);

            // Task object
            TaskManager taskManager = ServerProxy.getTaskManager();
            task = taskManager.getTask(Long.parseLong(p_originalTaskId));
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
            return makeErrorXml("importOfflineTargetFiles",
                    "Cannot get Task info. " + ex.getMessage());
        }

        // OfflineEditManager
        OfflineEditManager OEM = null;
        try
        {
            OEM = ServerProxy.getOfflineEditManager();
            OEM.attachListener(new OEMProcessStatus());
        }
        catch (Exception e)
        {
            logger.error("importOfflineTargetFiles", e);
            return makeErrorXml("importOfflineTargetFiles",
                    "Cannot get OfflineEditManager instancee " + e.getMessage());
        }

        OfflineFileUploadStatus status = OfflineFileUploadStatus.getInstance();

        StringBuilder errorMessage = new StringBuilder(XML_HEAD);

        // uploaded files path
        StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                .getInstance().getStringParameter(
                        SystemConfigParamNames.FILE_STORAGE_DIR));
        // The full path is like this:
        // Welocalize\FileStorage\qa\GlobalSight\tmp\<original_task_id>
        fileStorageRoot = fileStorageRoot.append(File.separator)
                .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                .append(File.separator).append("tmp").append(File.separator)
                .append(p_originalTaskId);

        File parentFilePath = new File(fileStorageRoot.toString());
        File[] files = parentFilePath.listFiles();
        File file = null, tmpFile = null;
        String fileName = null, tmp = "";
        if (files != null && files.length > 0)
        {
            for (int i = 0; i < files.length; i++)
            {
                file = files[i];
                tmp = file.getAbsolutePath();
                tmp = tmp.substring(tmp.lastIndexOf("."));
                try
                {
                    tmpFile = File.createTempFile(p_originalTaskId, tmp);
                    FileUtils.copyFile(file, tmpFile);
                }
                catch (IOException e1)
                {
                    logger.error("File access error. ", e1);
                }
                fileName = tmpFile.getName();
                status.addFilenameAlias(file.getName(), fileName);
                try
                {
                    OEM.processUploadPage(tmpFile, userObj, task, fileName);
                    file.delete();
                    if (file.exists()) {
                        logger.error("File " + file.getAbsolutePath() + " cannot be deleted.");
                    }
                }
                catch (Exception e)
                {
                    logger.error("Cannot handle file " + fileName, e);
                    status.addFileState(Long.valueOf(p_originalTaskId),
                            fileName, "Failed");
                    errorMessage.append("<file>\r\n");
                    errorMessage.append("\t<name>\r\n");
                    errorMessage.append("\t\t").append(fileName)
                            .append("</name>\r\n");
                    errorMessage.append("\t<error>\r\n");
                    errorMessage.append("\t\t").append(e.getMessage())
                            .append("</error>\r\n");
                }
            }
        }
        else
        {
            logger.info("There is no any files in upload path.");
            return makeErrorXml("importOfflineTargetFiles",
                    "Cannot find any files in uploading path");
        }

        if (!errorMessage.toString().equals(XML_HEAD))
            return makeErrorXml("importOfflineKitFiles", errorMessage.toString());
        else
            return null;
    }

    /**
     * Get file handling process status when uploading offline kit
     * 
     * @param accessToken
     *            Access token
     * @param taskId
     *            Task id
     * @param filename
     *            Filename of uploading offline kit
     * @return String File handling status with XML format
     * @throws WebServiceException
     */
    public String getOfflineFileUploadStatus(String accessToken, String taskId,
            String filename) throws WebServiceException
    {
        checkAccess(accessToken, "getOfflineFileUploadStatus");

        OfflineFileUploadStatus status = OfflineFileUploadStatus.getInstance();
        StringBuilder xml = new StringBuilder(XML_HEAD);
        Long lTaskId = Long.valueOf(taskId);
        HashMap<String, String> fileStates = status.getFileStates(lTaskId);
        // if (fileStates == null) {
        // fileStates = status.getFileStates(Long.valueOf(-1));
        // }

        ArrayList<String> files = new ArrayList<String>();

        if (fileStates != null)
        {
            if (!StringUtil.isEmpty(filename))
            {
                String[] fileArray = filename.split(",");
                for (String file : fileArray)
                {
                    if (StringUtil.isEmpty(file))
                        continue;
                    files.add(file);
                }
                if (files.size() > 0)
                {
                    xml.append("<fileStatus>\r\n");
                    for (String file : files)
                    {
                        xml.append("\t<file>").append(file)
                                .append("</file>\r\n");
                        xml.append("\t<status>")
                                .append(EditUtil.encodeHtmlEntities(fileStates
                                        .get(file))).append("</status>\r\n");
                    }
                    xml.append("</fileStatus>");
                }
            }
            // else
            // {
            // // Get all files in task
            // for (Iterator<String> iterator = fileStates.keySet().iterator();
            // iterator
            // .hasNext();)
            // {
            // files.add(iterator.next());
            // }
            // }

        }
        return xml.toString();
    }

    /**
     * Send segment comments back to original GlobalSight server. Only issues
     * that are created in original server will be sent back with added issue
     * history.
     * 
     * This method should be invoked after importOfflineTargetFiles() API.
     * 
     * @param p_accessToken
     * @param p_segmentComments
     *            Segment comments in HashMap(tuId:comment Vector) Vector
     *            structer: ISSUE table:(0-9) ID, ISSUE_OBJECT_ID,
     *            ISSUE_OBJECT_TYPE, CREATE_DATE, CREATOR_USER_ID, TITLE,
     *            PRIORITY, STATUS, LOGICAL_KEY, CATEGORY,
     *            ISSUE_EDITION_RELATION table:(10) ORIGINAL_ISSUE_HISTORY_ID
     *            ISSUE_HISTORY table:(11) Issue_History Vector (include one or
     *            more Vector, and every Vector includes all info from
     *            "issue_history" table in sequence), TARGET_LOCALE_ID (12)
     * 
     * @throws WebServiceException
     */
    public void sendSegmentCommentBack(String p_accessToken,
            HashMap p_segmentComments) throws WebServiceException
    {
        checkAccess(p_accessToken, "sendSegmentCommentBack");

        String companyId = (String) p_segmentComments.get("companyId");
        TuvManagerWLRemote tuvManager = ServerProxy.getTuvManager();
        CommentManagerWLRemote commentManager = ServerProxy.getCommentManager();

        Iterator iter = null;
        if (p_segmentComments != null && p_segmentComments.size() > 0)
        {
            iter = p_segmentComments.entrySet().iterator();
        }

        while (iter != null && iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            // get originalTuvId by originalTuId and localeId
            long tuId = ((Long) entry.getKey()).longValue();
            Tuv localeTuv = null;
            HashMap issueCommentMap = (HashMap) entry.getValue();
            long localeId = ((Long) issueCommentMap.get("localeId"))
                    .longValue();
            long tuvId = -1;
            try
            {
                localeTuv = tuvManager.getTuvForSegmentEditor(tuId, localeId,
                        Long.parseLong(companyId));
                tuvId = localeTuv.getId();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }

            // get Issue by tuvId if it exists.
            IssueImpl issue = null;
            long issueId = -1;
            String hql = "from IssueImpl i where i.levelObjectId = ?";
            Iterator it = HibernateUtil.search(hql, Long.valueOf(tuvId))
                    .iterator();
            if (it != null && it.hasNext())
            {
                issue = (IssueImpl) it.next();
                issueId = issue.getId();
            }

            // long _issueId = ((Long)
            // issueCommentMap.get("IssueID")).longValue();
            // long _issueObjectId = ((Long)
            // issueCommentMap.get("LevelObjectId")).longValue();
            String issueObjectType = (String) issueCommentMap
                    .get("LevelObjectType");
            // java.util.Date createDate = (java.util.Date)
            // issueCommentMap.get("CreateDate");
            String creatorUserId = (String) issueCommentMap.get("CreatorId");
            String title = (String) issueCommentMap.get("Title");
            String priority = (String) issueCommentMap.get("Priority");
            String status = (String) issueCommentMap.get("Status");
            // String logicalKey = (String) issueCommentMap.get("LogicalKey");
            String category = (String) issueCommentMap.get("Category");

            Vector issueHistoriesVector = (Vector) issueCommentMap
                    .get("HistoryVec");
            // The issue exists originally.
            if (issueId > 0)
            {
                // First, update the issue only, but not update issue history.
                try
                {
                    commentManager.editIssue(issueId, title, priority, status,
                            category, null, null);
                }
                catch (Exception e)
                {
                    logger.error(
                            "Failed to editIssue for issueId : " + issueId, e);
                }

                // "originalHistoryIdOnServerA_newHistoryIdOnServerB" pairs
                List historyIdPairList = new ArrayList();
                String originalIssueHistoryIds = (String) issueCommentMap
                        .get("OriginalIssueHistoryId");
                if (originalIssueHistoryIds != null
                        && !"".equals(originalIssueHistoryIds))
                {
                    StringTokenizer st = new StringTokenizer(
                            originalIssueHistoryIds, ",");
                    while (st != null && st.hasMoreElements())
                    {
                        String issueHistoryIdPair = (String) st.nextElement();
                        historyIdPairList.add(issueHistoryIdPair);
                    }
                }

                // save new history comments only,no need to update origianl
                // comments.
                if (issueHistoriesVector != null
                        && issueHistoriesVector.size() > 0)
                {
                    // reply to this issue one by one (all newly added)
                    for (int j = 0; j < issueHistoriesVector.size(); j++)
                    {
                        HashMap issueHistoryMap = (HashMap) issueHistoriesVector
                                .get(j);
                        String reportedBy = (String) issueHistoryMap
                                .get("ReportedBy");
                        String comment = (String) issueHistoryMap
                                .get("Comment");
                        long historyIdOnServerB = ((Long) issueHistoryMap
                                .get("HistoryID")).longValue();

                        // check if current issueHistory has existed on serverA.
                        boolean isExistOriginally = false;
                        List historyListOnServerA = issue.getHistory();
                        if (historyListOnServerA != null
                                && historyListOnServerA.size() > 0)
                        {
                            Iterator historyIterOnServerA = historyListOnServerA
                                    .iterator();
                            while (historyIterOnServerA.hasNext())
                            {
                                IssueHistoryImpl ih = (IssueHistoryImpl) historyIterOnServerA
                                        .next();
                                long historyIdOnServerA = ih.getDbId();

                                if (historyIdPairList.size() > 0)
                                {
                                    for (int m = 0; m < historyIdPairList
                                            .size(); m++)
                                    {
                                        // In
                                        // "originalHistoryId_newHistoryIdonServerB"
                                        // format.
                                        String historyPair = (String) historyIdPairList
                                                .get(m);
                                        int index = historyPair.indexOf("_");
                                        long _historyIdOnServerA = Long
                                                .parseLong(historyPair
                                                        .substring(0, index));
                                        long _historyIdOnServerB = Long
                                                .parseLong(historyPair
                                                        .substring(index + 1));
                                        if (_historyIdOnServerA == historyIdOnServerA
                                                && _historyIdOnServerB == historyIdOnServerB)
                                        {
                                            isExistOriginally = true;
                                        }
                                    }
                                }
                            }
                        }

                        // if not exist, add the history to current issue.
                        if (isExistOriginally == false)
                        {
                            // commentManager.editIssue(issueId, title,
                            // priority, status, category, null, null);
                            try
                            {
                                commentManager.replyToIssue(issueId, title,
                                        priority, status, category, reportedBy,
                                        comment);
                            }
                            catch (Exception e)
                            {
                                logger.error(
                                        "Failed to replyToIssue for issueId : "
                                                + issueId, e);
                            }
                        }
                    }
                }
            }
            // The issue is new.
            else
            {
                if (issueHistoriesVector != null
                        && issueHistoriesVector.size() > 0)
                {
                    long newIssueId = -1;

                    // reply to this issue one by one (all newly added)
                    for (int j = issueHistoriesVector.size() - 1; j > -1; j--)
                    {
                        HashMap issueHistoryMap = (HashMap) issueHistoriesVector
                                .get(j);

                        String reportedBy = (String) issueHistoryMap
                                .get("ReportedBy");
                        String comment = (String) issueHistoryMap
                                .get("Comment");

                        // Add the issue first
                        if (j == issueHistoriesVector.size() - 1)
                        {
                            long lg_id = -1;
                            try
                            {
                                lg_id = ServerProxy
                                        .getTuvManager()
                                        .getTuForSegmentEditor(tuId,
                                                Long.parseLong(companyId))
                                        .getLeverageGroupId();
                                long sourcePageId = -1;
                                sourcePageId = ServerProxy.getPageManager()
                                        .getSourcePageByLeverageGroupId(lg_id)
                                        .getId();
                                long targetPageId = ServerProxy
                                        .getPageManager()
                                        .getTargetPage(sourcePageId, localeId)
                                        .getId();

                                String logicalKey = CommentHelper
                                        .makeLogicalKey(targetPageId, tuId,
                                                tuvId, 0);

                                Issue _issue = commentManager.addIssue(
                                        Issue.TYPE_SEGMENT, tuvId, title,
                                        priority, status, category,
                                        creatorUserId, comment, logicalKey);
                                newIssueId = _issue.getId();
                            }
                            catch (Exception e)
                            {
                                logger.error(
                                        "Failed to add new issue for tuvId : "
                                                + tuvId, e);
                            }
                        }
                        // reply one by one
                        else
                        {
                            // add issue successfully when j==0
                            if (newIssueId > 0)
                            {
                                try
                                {
                                    commentManager.replyToIssue(newIssueId,
                                            title, priority, status, category,
                                            reportedBy, comment);
                                }
                                catch (Exception e)
                                {
                                    logger.error(
                                            "Failed to replyToIssue for issueId : "
                                                    + newIssueId, e);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Discard corresponding jobs on target server if activity on source server
     * is "Skipped". Note: If there are more than one job are created on target
     * server for one activity/task on origianl server, discard them all one by
     * one.
     * 
     * @param p_accessToken
     * @param p_userIdToDiscardJob
     *            String: userId who will dicard job(s) on target server.
     * @param p_taskId
     *            String: taskId on origianl server which is "skipped".
     * @throws WebServiceException
     */
    public void discardJob(String p_accessToken, String p_userIdToDiscardJob,
            String p_taskId) throws WebServiceException
    {
        checkAccess(p_accessToken, "discardJob");
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_DISCARD);

        String hql = "from JobEditionInfo a where a.originalTaskId = ?";
        List resultList = HibernateUtil.search(hql, new Long(p_taskId));
        Iterator it = null;
        if (resultList != null && resultList.size() > 0)
        {
            it = resultList.iterator();
            while (it.hasNext())
            {
                JobEditionInfo jei = (JobEditionInfo) it.next();
                long jobIdToDiscard = -1;
                try
                {
                    jobIdToDiscard = Long.valueOf(jei.getJobId());
                }
                catch (Exception e)
                {
                }

                Job jobToBeDiscard = null;
                try
                {
                    jobToBeDiscard = ServerProxy.getJobHandler().getJobById(
                            jobIdToDiscard);
                }
                catch (Exception e)
                {
                    String msg = "Failed to get job by jobId : "
                            + jobIdToDiscard;
                    logger.error(msg, e);
                    // msg = msg + e.getMessage();
                    // msg = makeErrorXml("discardJob", msg);
                    // throw new WebServiceException(msg);
                }

                String jobState = null;
                if (jobToBeDiscard != null)
                {
                    WorkflowHandlerHelper.cancelJob(p_userIdToDiscardJob,
                            jobToBeDiscard, jobState);
                }
            }
        }
    }

    /**
     * Get TimeZone by userId Note: web service API for Java.
     * 
     * @throws WebServiceException
     */
    public String getUserTimeZone(String p_accessToken, String p_userName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "getUserTimeZone");

        TimeZone timeZone = null;
        try
        {
            timeZone = ServerProxy.getCalendarManager().findUserTimeZone(
                    UserUtil.getUserIdByName(p_userName));
        }
        catch (Exception e)
        {
            logger.error("Failed to get user time zone. ", e);
            timeZone = TimeZone.getDefault();
        }

        return timeZone.getID();
    }

    /**
     * Get server version such as 7.1.7.2. For GS edition feature,it need to be
     * run on 7.1.7.2 or upper servers.
     * 
     * @param p_accessToken
     * @return
     * @throws WebServiceException
     */
    public String getServerVersion(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "getServerVersion");

        String version = ServerUtil.getVersion();

        return version == null ? "unknown" : version;
    }

    /**
     * Check if the given permission is existed on target server. This is used
     * to check if certain feature has been deployed on server.
     * 
     * @param p_accessToken
     * @param p_permissionName
     *            : defined in "Permission" file.
     * @return
     * @throws WebServiceException
     */
    public String isExistedPermission(String p_accessToken,
            String p_permissionName) throws WebServiceException
    {
        checkAccess(p_accessToken, "isExistedPermission");
        checkPermission(p_accessToken, Permission.PERMGROUPS_VIEW);

        boolean isSupport = false;
        HashMap allPermissionsMap = Permission.getAllPermissions();
        if (allPermissionsMap != null && allPermissionsMap.size() > 0)
        {
            isSupport = allPermissionsMap.keySet().contains(p_permissionName);
        }

        return (isSupport == true ? "true" : "false");
    }

    /**
     * Get the company information current logged user belongs to.
     * 
     * @param p_accessToken
     * 
     * @return the company info which current logged user belongs to.
     * 
     * @throws WebServiceException
     */
    public String fetchCompanyInfo(String p_accessToken)
            throws WebServiceException
    {
        String message = "";
        checkAccess(p_accessToken, "fetchCompanyInfo");

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            message = makeErrorXml("fetchCompanyInfo", e.getMessage());
            throw new WebServiceException(message);
        }

        // User object
        Company company = null;

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);

            // Validate if current user is in administrator group
            ArrayList pers = new ArrayList(Permission.getPermissionManager()
                    .getAllPermissionGroupNamesForUser(
                            UserUtil.getUserIdByName(userName)));
            if (!pers.contains(Permission.GROUP_ADMINISTRATOR))
            {
                message = makeErrorXml("fetchCompanyInfo",
                        "Current user is not administrator, cannot get the company information.");
                throw new WebServiceException(message);
            }

            User userObj = this.getUser(userName);
            String companyName = userObj.getCompanyName();
            company = ServerProxy.getJobHandler().getCompany(companyName);
        }
        catch (Exception e)
        {
            message = "Unable to get the company info for current logged user";
            logger.error(message, e);
            message = makeErrorXml("fetchCompanyInfo", message);
            throw new WebServiceException(message);
        }

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<Company>\r\n");
        xml.append("\t<id>").append(company.getId()).append("</id>\r\n");
        xml.append("\t<name>").append(company.getCompanyName())
                .append("</name>\r\n");
        xml.append("\t<description>")
                .append(company.getDescription() == null ? "N/A" : company
                        .getDescription()).append("</description>\r\n");
        xml.append("\t<enableIPFilter>").append(company.getEnableIPFilter())
                .append("</enableIPFilter>\r\n");
        xml.append("</Company>\r\n");

        return xml.toString();
    }

    /**
     * Get all job IDs in current company.
     * 
     * @param p_accessToken
     * 
     * @return : all job IDs in current company in "id1,id2,..." format.
     * 
     * @throws WebServiceException
     */
    public String fetchJobIdsPerCompany(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "fetchJobIdsPerCompany");
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobIdsPerCompany",
                    e.getMessage());
            throw new WebServiceException(message);
        }

        String loggedUserName = this.getUsernameFromSession(p_accessToken);
        User loggedUserObj = this.getUser(loggedUserName);
        String loggedComName = loggedUserObj.getCompanyName();

        StringBuffer jobIdList = new StringBuffer();
        try
        {
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            JobSearchParameters jsParm = new JobSearchParameters();
            // all jobs for all companies to be filtered
            Collection allJobs = jobHandler.getJobs(jsParm);
            if (allJobs != null && allJobs.size() > 0)
            {
                Iterator allJobsIter = allJobs.iterator();
                while (allJobsIter.hasNext())
                {
                    Job job = (Job) allJobsIter.next();
                    long comId = job.getCompanyId();
                    String jobComName = ServerProxy.getJobHandler()
                            .getCompanyById(comId).getCompanyName();
                    if (loggedComName != null && jobComName != null
                            && loggedComName.equals(jobComName))
                    {
                        if (jobIdList.length() == 0)
                        {
                            jobIdList.append("" + job.getId());
                        }
                        else
                        {
                            jobIdList.append("," + job.getId());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            String message = "Failed to retrive all job IDs for current company";
            logger.error(message, e);
            message = makeErrorXml("fetchJobIdsPerCompany", message);
            throw new WebServiceException(message);
        }

        return jobIdList.toString();
    }

    /**
     * Get jobs according with special offset and count of fetching records in
     * current company
     * 
     * @param p_accessToken
     *            Access token
     * @param p_offset
     *            Begin index of records
     * @param p_count
     *            Number count of fetching records
     * @return xml string
     * @throws WebServiceException
     * @author Vincent Yan, 2011/01/12
     */
    public String fetchJobsByRange(String p_accessToken, int p_offset,
            int p_count, boolean p_isDescOrder) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            p_offset = p_offset < 1 ? 0 : p_offset - 1;
            p_count = p_count <= 0 ? 1 : p_count;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsByRange", e.getMessage());
            throw new WebServiceException(message);
        }

        String result = null;

        try
        {
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();

            String userName = getUsernameFromSession(p_accessToken);
            Company company = getCompanyInfo(userName);
            if (company != null)
            {
                String[] ids = jobHandler.getJobIdsByCompany(
                        String.valueOf(company.getId()), p_offset, p_count,
                        p_isDescOrder);
                if (ids != null && ids.length > 0)
                {
                    result = fetchJobsPerCompany(p_accessToken, ids);
                }
            }
            return result;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsByRange", e.getMessage());
            throw new WebServiceException(message);
        }
    }

    /**
     * Get jobs according with special state, offset and count of fetching
     * records in current company
     * 
     * @param p_accessToken
     *            Access token
     * @param p_state
     *            State of job, such as DISPATCHED, PENDING etc.
     * @param p_offset
     *            Begin index of records
     * @param p_count
     *            Number count of fetching records
     * @return xml string
     * @throws WebServiceException
     * @author Vincent Yan, 2011/01/12
     */
    public String fetchJobsByState(String p_accessToken, String p_state,
            int p_offset, int p_count, boolean p_isDescOrder)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_state, "Job state");
            p_offset = p_offset < 1 ? 0 : p_offset - 1;
            p_count = p_count <= 0 ? 1 : p_count;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsByState", e.getMessage());
            throw new WebServiceException(message);
        }

        String result = null;

        try
        {
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();

            String userId = getUsernameFromSession(p_accessToken);
            Company company = getCompanyInfo(userId);
            if (company != null)
            {
                String[] ids = jobHandler.getJobIdsByState(
                        String.valueOf(company.getId()), p_state, p_offset,
                        p_count, p_isDescOrder);
                if (ids != null && ids.length > 0)
                {
                    result = fetchJobsPerCompany(p_accessToken, ids);
                }
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsByState", e.getMessage());
            throw new WebServiceException(message);
        }
    }

    /**
     * Get counts of job under every state
     * 
     * @param p_accessToken
     *            Access token
     * @return xml string
     * @throws WebServiceException
     * @author Vincent Yan, 2011/01/17
     */
    public String getCountsByJobState(String p_accessToken)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("getCountsByJobState", e.getMessage());
            throw new WebServiceException(message);
        }

        StringBuilder sb = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        HashMap<String, Integer> counts = null;

        try
        {
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();

            String userId = getUsernameFromSession(p_accessToken);
            Company company = getCompanyInfo(userId);
            if (company != null)
            {
                counts = jobHandler.getCountsByJobState(String.valueOf(company
                        .getId()));
                if (counts != null)
                {
                    sb.append("\t<counts>\r\n");
                    Set<String> keys = counts.keySet();
                    for (String state : keys)
                    {
                        sb.append("\t\t<countByState>\r\n");
                        sb.append("\t\t\t<state>").append(state)
                                .append("</state>\r\n");
                        sb.append("\t\t\t<count>").append(counts.get(state))
                                .append("</count>\r\n");
                        sb.append("\t\t</countByState>\r\n");
                    }
                    sb.append("\t</counts>\r\n");
                }
            }

            return sb.toString();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("getCountsByJobState", e.getMessage());
            throw new WebServiceException(message);
        }
    }

    /**
     * Get company info which user exists
     * 
     * @return Company Company info which user exists
     */
    private Company getCompanyInfo(String p_userName)
    {
        if (StringUtil.isEmpty(p_userName))
            return null;

        try
        {
            User user = getUser(p_userName);
            if (user != null)
                return ServerProxy.getJobHandler().getCompany(
                        user.getCompanyName());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Fetch all jobs in current company with XML format
     * 
     * @param p_accessToken
     * @return Jobs' information as XML format
     * @throws WebServiceException
     */
    public String fetchJobsPerCompany(String p_accessToken)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsPerCompany", e.getMessage());
            throw new WebServiceException(message);
        }

        checkAccess(p_accessToken, "fetchJobsPerCompany");
        try
        {
            String jobIds = fetchJobIdsPerCompany(p_accessToken);
            if (jobIds != null && jobIds.trim().length() > 0)
            {
                String[] ids = jobIds.split(",");
                return fetchJobsPerCompany(p_accessToken, ids);
            }
            else
                return "There is no jobs in current company";
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsPerCompany", e.getMessage());
            throw new WebServiceException(message);
        }
    }

    /**
     * Fetch jobs which belong to current user's company.
     * 
     * @param p_accessToken
     *            : accessToken
     * @param p_jobIds
     *            : jobIds in array. The job IDs can be retrieved by
     *            fetchJobIdsPerCompany() API.
     * 
     * @return xml String
     * 
     * @throws WebServiceException
     */
    public String fetchJobsPerCompany(String p_accessToken, String[] p_jobIds)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "fetchJobsPerCompany");
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            if (p_jobIds == null || p_jobIds.length == 0)
            {
                throw new Exception("jobIds are null or empty!");
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsPerCompany", e.getMessage());
            throw new WebServiceException(message);
        }

        StringBuffer xml = new StringBuffer(XML_HEAD);
        xml.append("<Jobs>\r\n");
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            User loggedUserObj = this.getUser(loggedUserName);
            String loggedComName = loggedUserObj.getCompanyName();

            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            // handle job one by one;if jobId is invalid or does not belong to
            // current company,ignore it.
            for (int i = 0; i < p_jobIds.length; i++)
            {
                try
                {
                    long jobID = Long.parseLong(p_jobIds[i]);
                    Job job = jobHandler.getJobById(jobID);
                    if (job == null)
                        continue;

                    if (!isInSameCompany(loggedUserName,
                            String.valueOf(job.getCompanyId())))
                    {
                        if (!UserUtil.isSuperAdmin(loggedUserName)
                                && !UserUtil.isSuperPM(loggedUserName))
                        {
                            continue;
                        }
                    }

                    String singleJobXml = handleSingleJob(job);
                    xml.append(singleJobXml);
                }
                catch (Exception e)
                {

                }
            }
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
            String msg = makeErrorXml("fetchJobsPerCompany", ex.getMessage());
            throw new WebServiceException(msg);
        }
        xml.append("</Jobs>\r\n");

        return xml.toString();
    }

    /**
     * Handle single job.
     * 
     * @param p_job
     *            : The job to be handled.
     * @param p_rah
     *            : "RemoteAccessHistory" object for "fetchJobsPerCompany()"
     *            API.
     * @param p_jobIdsHaveReturned
     *            : Vector which contains all job ids that have been returned or
     *            handled.
     * 
     * @return : HashMap
     */
    private String handleSingleJob(Job p_job)
    {
        StringBuffer subXML = new StringBuffer();
        StringBuilder tmpXml = new StringBuilder();

        if (p_job == null)
        {
            return "";
        }

        subXML.append("\t<Job>\r\n");

        try
        {
            subXML.append("\t\t<id>").append(p_job.getJobId())
                    .append("</id>\r\n");
            subXML.append("\t\t<name>")
                    .append(EditUtil.encodeXmlEntities(p_job.getJobName()))
                    .append("</name>\r\n");
            subXML.append("\t\t<state>").append(p_job.getState())
                    .append("</state>\r\n");

            // Display state
            try
            {
                subXML.append("\t\t<displayState>")
                        .append(p_job.getDisplayStateByLocale(new Locale("en",
                                "US"))).append("</displayState>\r\n");
            }
            catch (Exception e)
            {
                subXML.append("\t\t<displayState></displayState>\r\n");
            }

            // Priority
            subXML.append("\t\t<priority>").append(p_job.getPriority())
                    .append("</priority>\r\n");

            // Create date
            subXML.append("\t\t<createDate>")
                    .append(DateHelper.getFormattedDateAndTime(
                            p_job.getCreateDate(), null))
                    .append("</createDate>\r\n");

            // Start date
            if (p_job.getStartDate() == null)
            {
                subXML.append("\t\t<startDate>")
                        .append(DateHelper.getFormattedDateAndTime(
                                p_job.getCreateDate(), null))
                        .append("</startDate>\r\n");
            }
            else
            {
                subXML.append("\t\t<startDate>")
                        .append(convertDateToString(p_job.getStartDate()))
                        .append("</startDate>\r\n");
            }

            // Completed date
            subXML.append("\t\t<completedDate>")
                    .append(convertDateToString(p_job.getCompletedDate()))
                    .append("</completedDate>\r\n");

            // Localization profile
            L10nProfile lp = ServerProxy.getJobHandler().getL10nProfileByJobId(
                    p_job.getId());
            subXML.append("\t\t<localizationProfile>\r\n");
            if (lp != null && lp.getId() > 0)
            {
                subXML.append("\t\t\t<localizationProfileId>")
                        .append(lp.getId())
                        .append("</localizationProfileId>\r\n");
                subXML.append("\t\t\t<localizationProfileName>")
                        .append(lp.getName())
                        .append("</localizationProfileName>\r\n");
            }
            subXML.append("\t\t</localizationProfile>\r\n");

            // Project
            subXML.append("\t\t<project>\r\n");
            try
            {
                tmpXml = new StringBuilder();
                Project project = p_job.getProject();
                tmpXml.append("\t\t\t<projectId>").append(project.getId())
                        .append("</projectId>\r\n");
                tmpXml.append("\t\t\t<projectName>").append(project.getName())
                        .append("</projectName>\r\n");

                subXML.append(tmpXml.toString());
            }
            catch (Exception e)
            {
            }
            subXML.append("\t\t</project>\r\n");

            // Word count
            try
            {
                subXML.append("\t\t<wordcount>").append(p_job.getWordCount())
                        .append("</wordcount>\r\n");
            }
            catch (Exception e)
            {
                subXML.append("\t\t<wordcount></wordcount>\r\n");
            }

            // Source locale
            try
            {
                String srcLang = p_job.getSourceLocale().getLanguage() + "_"
                        + p_job.getSourceLocale().getCountry();
                subXML.append("\t\t<sourceLang>").append(srcLang)
                        .append("</sourceLang>\r\n");
            }
            catch (Exception e)
            {
                subXML.append("\t\t<sourceLang></sourceLang>\r\n");
            }

            // Due date
            try
            {
                String dueDateStr = DateHelper.getFormattedDateAndTime(
                        p_job.getDueDate(), null);
                subXML.append("\t\t<dueDate>").append(dueDateStr)
                        .append("</dueDate>\r\n");
            }
            catch (Exception e)
            {
                subXML.append("\t\t<dueDate></dueDate>\r\n");
            }

            // Source pages
            try
            {
                tmpXml = new StringBuilder();
                Iterator sfIt = p_job.getSourcePages().iterator();
                tmpXml.append("\t\t<sourcePages>\r\n");
                while (sfIt.hasNext())
                {
                    SourcePage sp = (SourcePage) sfIt.next();
                    tmpXml.append("\t\t\t<sourcePage>\r\n");
                    tmpXml.append("\t\t\t\t<sourcePageId>").append(sp.getId())
                            .append("</sourcePageId>\r\n");
                    tmpXml.append("\t\t\t\t<externalPageId>")
                            .append(replaceAndString(sp.getExternalPageId()))
                            .append("</externalPageId>\r\n");
                    tmpXml.append("\t\t\t</sourcePage>\r\n");
                }

                tmpXml.append("\t\t</sourcePages>\r\n");

                subXML.append(tmpXml.toString());
            }
            catch (Exception e)
            {
                subXML.append("\t\t<sourcePages>\r\n");
                subXML.append("\t\t</sourcePages>\r\n");
            }

            try
            {
                // workflows
                tmpXml = new StringBuilder();
                tmpXml.append("\t\t<workflows>\r\n");
                Collection wfs = p_job.getWorkflows();
                if (wfs != null && wfs.size() > 0)
                {
                    Iterator it = wfs.iterator();
                    while (it.hasNext())
                    {
                        tmpXml.append("\t\t\t<workflow>\r\n");
                        Workflow wf = (Workflow) it.next();
                        tmpXml.append("\t\t\t\t<wfId>").append(wf.getId())
                                .append("</wfId>\r\n");
                        String targetLang = wf.getTargetLocale().getLanguage()
                                + "_" + wf.getTargetLocale().getCountry();
                        tmpXml.append("\t\t\t\t<targetLang>")
                                .append(targetLang).append("</targetLang>\r\n");
                        tmpXml.append("\t\t\t</workflow>\n");
                    }
                }
                tmpXml.append("\t\t</workflows>\r\n");

                subXML.append(tmpXml.toString());
            }
            catch (Exception e)
            {
                subXML.append("\t\t<workflows>\r\n");
                subXML.append("\t\t</workflows>\r\n");
            }
        }
        catch (Exception e)
        {
            String msg = "Failed to handle single job for 'fetchJobsPerCompany'.";
            logger.error(msg, e);
        }

        subXML.append("\t</Job>\r\n");

        return subXML.toString();
    }

    /**
     * Fetch workflow info for specified workflowId
     * 
     * <P>
     * Information returned: Info of current workflow; Wordcount Summary; Job
     * comments; Task/Activity comments.
     * </P>
     * 
     * @param p_accessToken
     * @param p_workflowId
     * @return String in XML format.
     * 
     * @throws WebServiceException
     */
    public String fetchWorkflowRelevantInfo(String p_accessToken,
            String p_workflowId) throws WebServiceException
    {
        checkAccess(p_accessToken, "fetchWorkflowRelevantInfo");
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_VIEW);

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<WorkflowInfo>\r\n");
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_workflowId, "workflowId");

            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            WorkflowManagerWLRemote wfManager = ServerProxy
                    .getWorkflowManager();

            Workflow wf = wfManager.getWorkflowById((new Long(p_workflowId))
                    .longValue());
            if (wf == null)
            {
                String msg = "Can't find workflow for workflowId : "
                        + p_workflowId;
                throw new Exception(msg);
            }

            Job job = wf.getJob();
            long l10nProfileId = wf.getJob().getFileProfile()
                    .getL10nProfileId();

            /** Info of current workflow */
            xml.append("\t<workflowId>").append(p_workflowId)
                    .append("</workflowId>\r\n");
            String trgLocale = wf.getTargetLocale().getLanguage() + "_"
                    + wf.getTargetLocale().getCountry();
            xml.append("\t<targetLocale>").append(trgLocale)
                    .append("</targetLocale>\r\n");
            xml.append("\t<state>").append(wf.getState())
                    .append("</state>\r\n");
            xml.append("\t<percentageCompletion>")
                    .append(wf.getPercentageCompletion())
                    .append("</percentageCompletion>\r\n");
            // currentActivity
            TaskInstance taskInstance = WorkflowManagerLocal.getCurrentTask(wf
                    .getId());
            String currentTaskName = "";
            if (taskInstance != null)
            {
                currentTaskName = TaskJbpmUtil.getTaskDisplayName(taskInstance
                        .getName());
            }
            xml.append("\t<currentActivity>").append(currentTaskName)
                    .append("</currentActivity>\r\n");
            // estimatedTranslateCompletionDate
            Date estimatedTransCompDate = wf
                    .getEstimatedTranslateCompletionDate();
            String temp = DateHelper.getFormattedDateAndTime(
                    estimatedTransCompDate, null);
            xml.append("\t<estimatedTranslateCompletionDate>").append(temp)
                    .append("</estimatedTranslateCompletionDate>\r\n");
            // estimatedCompletionDate
            Date estimatedCompDate = wf.getEstimatedCompletionDate();
            String temp2 = DateHelper.getFormattedDateAndTime(
                    estimatedCompDate, null);
            xml.append("\t<estimatedCompletionDate>").append(temp2)
                    .append("</estimatedCompletionDate>\r\n");
            xml.append("\t<workflowPriority>").append(wf.getPriority())
                    .append("</workflowPriority>\r\n");

            /** Wordcount Summary */
            xml.append("\t<wordCountSummary>\r\n");
            // leverageOption
            String leverageOption = "unknown";
            boolean isInContextMatch = false;
            boolean isDefaultContextMatch = false;
            try
            {
                TranslationMemoryProfile tmp = ServerProxy.getProjectHandler()
                        .getL10nProfile(l10nProfileId)
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    isInContextMatch = tmp.getIsContextMatchLeveraging();
                    isDefaultContextMatch = PageHandler
                            .isDefaultContextMatch(job);
                }
                if (isInContextMatch)
                {
                    leverageOption = "Leverage in context matches";
                }
                else if (isDefaultContextMatch)
                {
                    leverageOption = "Default";
                }
                else
                {
                    leverageOption = "100% match only";
                }
            }
            catch (Exception e)
            {

            }
            xml.append("\t\t<leverageOption>").append(leverageOption)
                    .append("</leverageOption>\r\n");
            // 100%
            int wc = 0;
            if (isInContextMatch)
            {
                wc = wf.getSegmentTmWordCount();
            }
            else if (isDefaultContextMatch)
            {
                wc = wf.getTotalExactMatchWordCount()
                        - wf.getContextMatchWordCount();
            }
            else
            {
                wc = wf.getTotalExactMatchWordCount();
            }
            xml.append("\t\t<100%>").append(wc).append("</100%>\r\n");
            // 95%-99%
            xml.append("\t\t<95%-99%>")
                    .append(wf.getThresholdHiFuzzyWordCount())
                    .append("</95%-99%>\r\n");
            // 85%-94%
            xml.append("\t\t<85%-94%>")
                    .append(wf.getThresholdMedHiFuzzyWordCount())
                    .append("</85%-94%>\r\n");
            // 75%-84%
            xml.append("\t\t<75%-84%>")
                    .append(wf.getThresholdMedFuzzyWordCount())
                    .append("</75%-84%>\r\n");
            // noMatch (50%-74%)
            xml.append("\t\t<noMatch>")
                    .append(wf.getThresholdNoMatchWordCount())
                    .append("</noMatch>\r\n");
            // Repetitions
            xml.append("\t\t<repetitions>")
                    .append(wf.getRepetitionWordCount())
                    .append("</repetitions>\r\n");
            // In Context Matches
            if (isInContextMatch)
            {
                xml.append("\t\t<InContextMatches>")
                        .append(wf.getInContextMatchWordCount())
                        .append("</InContextMatches>\r\n");
            }
            // Context Matches
            if (isDefaultContextMatch)
            {
                xml.append("\t\t<ContextMatches>")
                        .append(wf.getContextMatchWordCount())
                        .append("</ContextMatches>\r\n");
            }
            // total
            xml.append("\t\t<total>").append(wf.getTotalWordCount())
                    .append("</total>\r\n");
            xml.append("\t</wordCountSummary>\r\n");

            /** Job comments */
            List commentsList = job.getJobComments();
            if (commentsList != null && commentsList.size() > 0)
            {
                xml.append("\t<jobComments>\r\n");
                Iterator commentsIt = commentsList.iterator();
                while (commentsIt.hasNext())
                {
                    Comment comment = (Comment) commentsIt.next();
                    xml.append("\t\t<jobComment>\r\n");
                    xml.append("\t\t\t<jobCommentId>").append(comment.getId())
                            .append("</jobCommentId>\r\n");

                    temp = comment.getComment();
                    temp = StringUtil.isEmpty(temp) ? "" : XmlUtil
                            .escapeString(temp);

                    xml.append("\t\t\t<jobCommentContent>").append(temp)
                            .append("</jobCommentContent>\r\n");
                    // comment files
                    xml.append("\t\t\t<jobCommentFiles>\r\n");
                    String generalPath = getCommentPath(comment.getId(),
                            CommentUpload.GENERAL);
                    String generalCommentFileXML = getJobCommentsXML(
                            generalPath, CommentUpload.GENERAL);
                    if (generalCommentFileXML != null
                            && generalCommentFileXML.trim().length() > 0)
                    {
                        xml.append(generalCommentFileXML);
                    }
                    String restrictedPath = getCommentPath(comment.getId(),
                            CommentUpload.RESTRICTED);
                    String restrictedCommentFileXML = getJobCommentsXML(
                            restrictedPath, CommentUpload.RESTRICTED);
                    if (restrictedCommentFileXML != null
                            && restrictedCommentFileXML.trim().length() > 0)
                    {
                        xml.append(restrictedCommentFileXML);
                    }
                    xml.append("\t\t\t</jobCommentFiles>\r\n");
                    xml.append("\t\t</jobComment>\r\n");
                }
                xml.append("\t</jobComments>\r\n");
            }

            /** Task/Activity comments */
            Iterator tasksIt = null;
            try
            {
                tasksIt = wf.getTasks().values().iterator();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
            if (tasksIt != null)
            {
                while (tasksIt.hasNext())
                {
                    Task task = (Task) tasksIt.next();
                    List taskComments = task.getTaskComments();
                    if (taskComments != null && taskComments.size() > 0)
                    {
                        xml.append("\t<taskComments>\r\n");
                        Iterator taskCommentIt = taskComments.iterator();
                        while (taskCommentIt.hasNext())
                        {
                            Comment comment = (Comment) taskCommentIt.next();
                            xml.append("\t\t<taskComment>\r\n");
                            xml.append("\t\t\t<taskCommentId>")
                                    .append(comment.getId())
                                    .append("</taskCommentId>\r\n");

                            temp = comment.getComment();
                            temp = StringUtil.isEmpty(temp) ? "" : XmlUtil
                                    .escapeString(temp);

                            xml.append("\t\t\t<taskCommentContent>")
                                    .append(temp)
                                    .append("</taskCommentContent>\r\n");
                            xml.append("\t\t\t<taskCommentFiles>\r\n");
                            String generalPath = getCommentPath(
                                    comment.getId(), CommentUpload.GENERAL);
                            String generalCommentFileXML = getTaskCommentsXML(
                                    generalPath, CommentUpload.GENERAL);
                            if (generalCommentFileXML != null
                                    && generalCommentFileXML.trim().length() > 0)
                            {
                                xml.append(generalCommentFileXML);
                            }
                            String restrictedPath = getCommentPath(
                                    comment.getId(), CommentUpload.RESTRICTED);
                            String restrictedCommentFileXML = getTaskCommentsXML(
                                    restrictedPath, CommentUpload.RESTRICTED);
                            if (restrictedCommentFileXML != null
                                    && restrictedCommentFileXML.trim().length() > 0)
                            {
                                xml.append(restrictedCommentFileXML);
                            }
                            xml.append("\t\t\t</taskCommentFiles>\r\n");
                            xml.append("\t\t</taskComment>\r\n");
                        }
                        xml.append("\t</taskComments>\r\n");
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
            String msg = makeErrorXml("fetchWorkflowRelevantInfo",
                    ex.getMessage());
            throw new WebServiceException(msg);
        }
        xml.append("</WorkflowInfo>\r\n");

        return xml.toString();
    }

    /**
     * Get workflow info with batch job ids
     * 
     * @author Vincent Yan, 2012/08/08
     * @since 8.2.3
     * 
     * @param p_accessToken
     *            Access token
     * @param jobIds
     *            String contains batch of job ids, split by comma
     * @return String XML format string contains workflow info
     * @throws WebServiceException
     * 
     */
    public String fetchWorkflowRelevantInfoByJobs(String p_accessToken,
            String jobIds) throws WebServiceException
    {
        if (StringUtil.isEmpty(p_accessToken) || StringUtil.isEmpty(jobIds))
            return makeErrorXml("fetchWorkflowRelevantInfoByJobs",
                    "Invaild parameter");

        checkAccess(p_accessToken, "fetchWorkflowRelevantInfoByJobs");
        try
        {
            checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_VIEW);
        }
        catch (Exception e)
        {
            return makeErrorXml("fetchWorkflowRelevantInfoByJobs",
                    e.getMessage());
        }

        StringBuffer xml = new StringBuffer(XML_HEAD);
        xml.append("<jobs>\r\n");
        try
        {
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            WorkflowManagerWLRemote wfManager = ServerProxy
                    .getWorkflowManager();
            Job job = null;
            ArrayList<Workflow> workflows = null;

            String[] jobIdArray = jobIds.split(",");
            long jobId = 0;
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);

            for (String jobIdString : jobIdArray)
            {
                if (StringUtil.isEmpty(jobIdString))
                    continue;
                try
                {
                    jobId = Long.parseLong(jobIdString.trim());
                }
                catch (Exception e)
                {
                    continue;
                }

                job = jobHandler.getJobById(jobId);
                if (job == null)
                    continue;

                if (!isInSameCompany(userName,
                        String.valueOf(job.getCompanyId())))
                {
                    if (!UserUtil.isSuperAdmin(userId)
                            && !UserUtil.isSuperPM(userId))
                    {
                        continue;
                    }
                }

                if (job != null)
                {
                    xml.append("\t<job>\r\n");
                    xml.append("\t\t<job_id>").append(jobIdString)
                            .append("</job_id>\r\n");
                    xml.append("\t\t<job_name>")
                            .append(EditUtil.encodeXmlEntities(job.getJobName()))
                            .append("</job_name>\r\n");

                    workflows = new ArrayList<Workflow>(job.getWorkflows());
                    if (workflows != null && workflows.size() > 0)
                    {
                        xml.append("\t\t\t<workflows>\r\n");
                        for (Workflow workflow : workflows)
                        {
                            xml.append("\t\t\t\t<workflow>\r\n");
                            xml.append(generateWorkflowInfo(workflow,
                                    "\t\t\t\t\t"));
                            xml.append("\t\t\t\t</workflow>\r\n");
                        }
                        xml.append("\t\t\t</workflows>\r\n");
                    }
                    else
                    {
                        xml.append("\t\t\t<workflows>\r\n");
                        xml.append("\t\t\t</workflows>\r\n");
                    }

                    xml.append("\t</job>\r\n");
                }
            }
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
            return makeErrorXml("fetchWorkflowRelevantInfo", ex.getMessage());
        }

        xml.append("</jobs>");

        return xml.toString();
    }

    private String generateWorkflowInfo(Workflow workflow, String tab)
    {
        StringBuilder xml = new StringBuilder();

        /** Info of current workflow */
        // Workflow id
        xml.append(tab).append("<workflow_id>").append(workflow.getId())
                .append("</workflow_id>\r\n");
        // target locale
        xml.append(tab).append("<target_locale>")
                .append(workflow.getTargetLocale())
                .append("</target_locale>\r\n");
        // workflow state
        xml.append(tab).append("<workflow_state>").append(workflow.getState())
                .append("</workflow_state>\r\n");
        // percentage of completion
        xml.append(tab).append("<percentage_completion>")
                .append(workflow.getPercentageCompletion())
                .append("</percentage_completion>\r\n");

        // current activity
        TaskInstance taskInstance = WorkflowManagerLocal
                .getCurrentTask(workflow.getId());
        String currentTaskName = "";
        if (taskInstance != null)
        {
            currentTaskName = TaskJbpmUtil.getTaskDisplayName(taskInstance
                    .getName());
        }
        xml.append(tab).append("<current_activity>").append(currentTaskName)
                .append("</current_activity>\r\n");

        // estimatedTranslateCompletionDate
        Date tmpDate = workflow.getEstimatedTranslateCompletionDate();
        String temp = DateHelper.getFormattedDateAndTime(tmpDate, null);
        xml.append(tab).append("<estimated_translate_completion_date>")
                .append(temp)
                .append("</estimated_translate_completion_date>\r\n");

        // estimatedCompletionDate
        tmpDate = workflow.getEstimatedCompletionDate();
        temp = DateHelper.getFormattedDateAndTime(tmpDate, null);
        xml.append(tab).append("<estimated_completion_date>").append(temp)
                .append("</estimated_completion_date>\r\n");

        // workflow priority
        xml.append(tab).append("<workflow_priority>")
                .append(workflow.getPriority())
                .append("</workflow_priority>\r\n");

        /** Wordcount Summary */
        xml.append(tab).append("<word_counts>\r\n");

        // leverageOption
        String leverageOption = "unknown";
        boolean isInContextMatch = false;
        boolean isDefaultContextMatch = false;
        try
        {
            // TranslationMemoryProfile tmp = ServerProxy.getProjectHandler()
            // .getL10nProfile(workflow.getJob().getL10nProfileId())
            // .getTranslationMemoryProfile();
            // if (tmp != null)
            // {
            // isInContextMatch = tmp.getIsContextMatchLeveraging();
            // isDefaultContextMatch = PageHandler
            // .isDefaultContextMatch(workflow.getJob());
            // }
            Job job = workflow.getJob();
            if (job.DEFAULT_CONTEXT.equals(job.getLeverageOption()))
            {
                isDefaultContextMatch = true;
            }
            else if (job.IN_CONTEXT.equals(job.getLeverageOption()))
            {
                isInContextMatch = true;
            }
            if (isInContextMatch)
            {
                leverageOption = "Leverage in context matches";
            }
            else if (isDefaultContextMatch)
            {
                leverageOption = "Default";
            }
            else
            {
                leverageOption = "Match 100 Percent";
            }
        }
        catch (Exception e)
        {

        }
        xml.append(tab).append("\t<leverage_option>").append(leverageOption)
                .append("</leverage_option>\r\n");

        // 100%
        int wc = 0;
        if (isInContextMatch)
        {
            wc = workflow.getSegmentTmWordCount();
        }
        else if (isDefaultContextMatch)
        {
            wc = workflow.getTotalExactMatchWordCount()
                    - workflow.getContextMatchWordCount();
        }
        else
        {
            wc = workflow.getTotalExactMatchWordCount();
        }
        xml.append(tab).append("\t<match_100_percent>").append(wc)
                .append("</match_100_percent>\r\n");
        // 95%-99%
        xml.append(tab).append("\t<match_95_percent-99_percent>")
                .append(workflow.getThresholdHiFuzzyWordCount())
                .append("</match_95_percent-99_percent>\r\n");
        // 85%-94%
        xml.append(tab).append("\t<match_85_percent-94_percent>")
                .append(workflow.getThresholdMedHiFuzzyWordCount())
                .append("</match_85_percent-94_percent>\r\n");
        // 75%-84%
        xml.append(tab).append("\t<match_75_percent-84_percent>")
                .append(workflow.getThresholdMedFuzzyWordCount())
                .append("</match_75_percent-84_percent>\r\n");
        // noMatch (50%-74%)
        xml.append(tab).append("\t<no_match>")
                .append(workflow.getThresholdNoMatchWordCount())
                .append("</no_match>\r\n");
        // Repetitions
        xml.append(tab)
                .append("\t<repetitions>")
                .append(workflow.getRepetitionWordCount())
                .append("</repetitions>\r\n");
        // In Context Matches
        if (isInContextMatch)
        {
            xml.append(tab).append("\t<in_context_match>")
                    .append(workflow.getInContextMatchWordCount())
                    .append("</in_context_match>\r\n");
        }
        // Context Matches
        if (isDefaultContextMatch)
        {
            xml.append(tab).append("\t<context_match>")
                    .append(workflow.getContextMatchWordCount())
                    .append("</context_match>\r\n");
        }
        // total
        xml.append(tab).append("\t<total>")
                .append(workflow.getTotalWordCount()).append("</total>\r\n");
        xml.append(tab).append("</word_counts>\r\n");

        /** Job comments */
        List commentsList = workflow.getJob().getJobComments();
        if (commentsList != null && commentsList.size() > 0)
        {
            xml.append(tab).append("<job_comments>\r\n");
            Iterator commentsIt = commentsList.iterator();
            while (commentsIt.hasNext())
            {
                Comment comment = (Comment) commentsIt.next();
                xml.append(tab).append("\t<job_comment>\r\n");
                xml.append(tab).append("\t\t<job_comment_id>")
                        .append(comment.getId())
                        .append("</job_comment_id>\r\n");

                temp = comment.getComment();
                temp = StringUtil.isEmpty(temp) ? "" : XmlUtil
                        .escapeString(temp);

                xml.append(tab).append("\t\t<job_comment_content>")
                        .append(temp).append("</job_comment_content>\r\n");
                // comment files
                xml.append(tab).append("\t\t<job_comment_files>\r\n");
                String generalPath = getCommentPath(comment.getId(),
                        CommentUpload.GENERAL);
                String generalCommentFileXML = getJobCommentsXML(generalPath,
                        CommentUpload.GENERAL);
                if (generalCommentFileXML != null
                        && generalCommentFileXML.trim().length() > 0)
                {
                    xml.append(tab).append(generalCommentFileXML);
                }
                String restrictedPath = getCommentPath(comment.getId(),
                        CommentUpload.RESTRICTED);
                String restrictedCommentFileXML = getJobCommentsXML(
                        restrictedPath, CommentUpload.RESTRICTED);
                if (restrictedCommentFileXML != null
                        && restrictedCommentFileXML.trim().length() > 0)
                {
                    xml.append(tab).append(restrictedCommentFileXML);
                }
                xml.append(tab).append("\t\t</job_comment_files>\r\n");
                xml.append(tab).append("\t</job_comment>\r\n");
            }
            xml.append(tab).append("</job_comments>\r\n");
        }

        /** Task/Activity comments */
        Iterator tasksIt = null;
        try
        {
            tasksIt = workflow.getTasks().values().iterator();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        if (tasksIt != null)
        {
            while (tasksIt.hasNext())
            {
                Task task = (Task) tasksIt.next();
                List taskComments = task.getTaskComments();
                if (taskComments != null && taskComments.size() > 0)
                {
                    xml.append(tab).append("<task_comments>\r\n");
                    Iterator taskCommentIt = taskComments.iterator();
                    while (taskCommentIt.hasNext())
                    {
                        Comment comment = (Comment) taskCommentIt.next();
                        xml.append(tab).append("\t<task_comment>\r\n");
                        xml.append(tab).append("\t\t<task_comment_id>")
                                .append(comment.getId())
                                .append("</task_comment_id>\r\n");

                        temp = comment.getComment();
                        temp = StringUtil.isEmpty(temp) ? "" : XmlUtil
                                .escapeString(temp);

                        xml.append(tab).append("\t\t<task_comment_content>")
                                .append(temp)
                                .append("</task_comment_content>\r\n");
                        xml.append(tab).append("\t\t<task_comment_files>\r\n");
                        String generalPath = getCommentPath(comment.getId(),
                                CommentUpload.GENERAL);
                        String generalCommentFileXML = getTaskCommentsXML(
                                generalPath, CommentUpload.GENERAL);
                        if (generalCommentFileXML != null
                                && generalCommentFileXML.trim().length() > 0)
                        {
                            xml.append(tab).append(generalCommentFileXML);
                        }
                        String restrictedPath = getCommentPath(comment.getId(),
                                CommentUpload.RESTRICTED);
                        String restrictedCommentFileXML = getTaskCommentsXML(
                                restrictedPath, CommentUpload.RESTRICTED);
                        if (restrictedCommentFileXML != null
                                && restrictedCommentFileXML.trim().length() > 0)
                        {
                            xml.append(tab).append(restrictedCommentFileXML);
                        }
                        xml.append(tab).append("\t\t</task_comment_files>\r\n");
                        xml.append(tab).append("\t</task_comment>\r\n");
                    }
                    xml.append(tab).append("</task_comments>\r\n");
                }
            }
        }
        return xml.toString();

    }

    private String getCommentPath(long p_commentId, String p_access)
    {
        StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                .getInstance().getStringParameter(
                        SystemConfigParamNames.FILE_STORAGE_DIR));

        fileStorageRoot.append(File.separator)
                .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                .append(File.separator).append("CommentReference")
                .append(File.separator).append(p_commentId)
                .append(File.separator).append(p_access);

        return fileStorageRoot.toString();
    }

    /**
     * Get job comment files info in XML format.
     * 
     * @param p_parentDir
     * @param p_access
     * @return
     */
    private String getJobCommentsXML(String p_parentDir, String p_access)
    {
        StringBuffer subXML = new StringBuffer();

        File dir = new File(p_parentDir);
        if (dir.exists() && dir.isDirectory())
        {
            File[] subFiles = dir.listFiles();
            for (int k = 0; k < subFiles.length; k++)
            {
                File file = subFiles[k];
                if (file.exists() && file.isFile())
                {
                    subXML.append("\t\t\t\t<jobCommentFile>\r\n");
                    subXML.append("\t\t\t\t\t<jobCommentFileName>")
                            .append(XmlUtil.escapeString(file.getName()))
                            .append("</jobCommentFileName>\r\n");
                    subXML.append("\t\t\t\t\t<jobCommentAccess>")
                            .append(p_access).append("</jobCommentAccess>\r\n");
                    subXML.append("\t\t\t\t</jobCommentFile>\r\n");
                }
            }
        }

        return subXML.toString();
    }

    /**
     * Get task comment files info in XML format.
     * 
     * @param p_parentDir
     * @param p_access
     * @return
     */
    private String getTaskCommentsXML(String p_parentDir, String p_access)
    {
        StringBuffer subXML = new StringBuffer();

        File dir = new File(p_parentDir);
        if (dir.exists() && dir.isDirectory())
        {
            File[] subFiles = dir.listFiles();
            for (int k = 0; k < subFiles.length; k++)
            {
                File file = subFiles[k];
                if (file.exists() && file.isFile())
                {
                    subXML.append("\t\t\t\t<taskCommentFile>\r\n");
                    subXML.append("\t\t\t\t\t<taskCommentFileName>")
                            .append(XmlUtil.escapeString(file.getName()))
                            .append("</taskCommentFileName>\r\n");
                    subXML.append("\t\t\t\t\t<taskCommentAccess>")
                            .append(p_access)
                            .append("</taskCommentAccess>\r\n");
                    subXML.append("\t\t\t\t</taskCommentFile>\r\n");
                }
            }
        }

        return subXML.toString();
    }

    /**
     * Download exported file in translation for preview purpose.
     * 
     * <P>
     * The exported file may be in translation or has been finished. The file
     * url in server will be returned, and also "downloadable" is returned too.
     * If the file is not downloadable, "exportWorkflow()" or "exportJob()" APIs
     * should be invoked first.
     * </P>
     * 
     * @param p_accessToken
     * @param p_jobId
     * @param p_targetLocaleId
     * @param p_sourcePageId
     * @return String : XML format
     * 
     * @throws WebServiceException
     */
    public String fetchFileForPreview(String p_accessToken, String p_jobId,
            String p_targetLocaleId, String p_sourcePageId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "fetchFileForPreview");
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<exportedFileInfo>\r\n");
        try
        {
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            WorkflowManagerWLRemote wfManager = ServerProxy
                    .getWorkflowManager();

            Job job = jobHandler.getJobById((new Long(p_jobId)).longValue());
            // this is like this:
            // http://<host>:<port>/globalsight/cxedocs/<companyName>
            String urlPrefix = determineUrlPrefix(CompanyWrapper
                    .getCompanyNameById(job.getCompanyId()));
            Iterator wfIt = job.getWorkflows().iterator();
            while (wfIt.hasNext())
            {
                Workflow wf = (Workflow) wfIt.next();
                GlobalSightLocale targetLocale = wf.getTargetLocale();
                // this is the wf wanted
                if (targetLocale.getId() == (new Long(p_targetLocaleId)
                        .longValue()))
                {
                    Iterator targetPagesIt = wf.getAllTargetPages().iterator();
                    while (targetPagesIt.hasNext())
                    {
                        TargetPage tp = (TargetPage) targetPagesIt.next();
                        SourcePage sp = tp.getSourcePage();
                        // this is the source page wanted
                        if (sp.getId() == (new Long(p_sourcePageId).longValue()))
                        {
                            xml.append("\t<exportedFile>\r\n");
                            // fileUrl
                            String extPageId = sp.getExternalPageId();
                            int separatorIndex = extPageId
                                    .indexOf(File.separator);
                            extPageId = extPageId.substring(separatorIndex + 1);
                            String trgLocale = targetLocale.getLanguage() + "_"
                                    + targetLocale.getCountry();
                            StringBuffer subXml = new StringBuffer(urlPrefix);
                            subXml.append(File.separator).append(trgLocale)
                                    .append(File.separator).append(extPageId);
                            xml.append("\t\t<fileUrl>")
                                    .append(EditUtil.encodeXmlEntities(subXml
                                            .toString()))
                                    .append("</fileUrl>\r\n");
                            // downloadable
                            String cxeDocPath = AmbFileStoragePathUtils
                                    .getCxeDocDirPath();
                            StringBuffer path = new StringBuffer(cxeDocPath);
                            path.append(File.separator).append(trgLocale)
                                    .append(File.separator).append(extPageId);
                            File file = new File(path.toString());
                            xml.append("\t\t<downloadable>")
                                    .append(file.exists())
                                    .append("</downloadable>\r\n");
                            xml.append("\t</exportedFile>\r\n");
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
            String msg = makeErrorXml("fetchFileForPreview", ex.getMessage());
            throw new WebServiceException(msg);
        }
        xml.append("</exportedFileInfo>\r\n");

        return xml.toString();
    }

    /**
     * Get comment files for specified job or task Id.
     * 
     * @param p_accessToken
     *            Access token obtained from login.
     * @param p_commentObjectType
     *            Indicate job or task type. "J": job; "T": task.
     * @param p_jobOrTaskId
     * 
     * @return String in XML format.
     * 
     * @throws WebServiceException
     */
    public String getCommentFiles(String p_accessToken,
            String p_commentObjectType, String p_jobOrTaskId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, DOWNLOAD_COMMENT_FILES);
        checkPermission(p_accessToken, Permission.ACTIVITIES_COMMENTS_DOWNLOAD);

        // check parameters
        try
        {
            Assert.assertNotEmpty(p_commentObjectType, "Comment object type");
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertIsInteger(p_jobOrTaskId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml(DOWNLOAD_COMMENT_FILES,
                    e.getMessage());
            throw new WebServiceException(message);
        }

        // job or task
        Object workObject = null;
        try
        {
            long jobOrTaskId = Long.parseLong(p_jobOrTaskId);
            if ("J".equalsIgnoreCase(p_commentObjectType.trim()))
            {
                workObject = ServerProxy.getJobHandler()
                        .getJobById(jobOrTaskId);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to retrieve job or task object for job or task ID : "
                    + p_jobOrTaskId;
            message = makeErrorXml(message, e.getMessage());
            throw new WebServiceException(message);
        }

        // companyId
        long companyId = -1;
        String companyName = null;
        try
        {
            String userId = super.getUsernameFromSession(p_accessToken);
            User user = ServerProxy.getUserManager().getUser(userId);
            companyName = user.getCompanyName();
            companyId = ServerProxy.getJobHandler().getCompany(companyName)
                    .getId();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }

        // comment id_comment map
        Map commentMap = new HashMap();
        if (workObject != null && workObject instanceof Job)
        {
            Job job = (Job) workObject;
            List jobComments = job.getJobComments();
            if (jobComments != null)
            {
                Iterator jobCommentIter = jobComments.iterator();
                while (jobCommentIter.hasNext())
                {
                    Comment jobComment = (Comment) jobCommentIter.next();
                    commentMap.put(jobComment.getId(), jobComment.getComment());
                }
            }
        }
        else if (workObject != null && workObject instanceof Task)
        {
            Task task = (Task) workObject;
            List taskCommentList = task.getTaskComments();
            if (taskCommentList != null)
            {
                Iterator taskCommentIter = taskCommentList.iterator();
                while (taskCommentIter.hasNext())
                {
                    Comment taskComment = (Comment) taskCommentIter.next();
                    commentMap.put(taskComment.getId(),
                            taskComment.getComment());
                }
            }
        }

        // access & saved
        String access = WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS;

        //
        StringBuffer result = new StringBuffer(XML_HEAD);
        result.append("<CommentFilesInformation>\r\n");
        result.append("\t<WorkObjectId>").append(p_jobOrTaskId)
                .append("</WorkObjectId>\r\n");
        result.append("\t<ObjectType>")
                .append(p_commentObjectType.equalsIgnoreCase("J") ? "job"
                        : "task").append("</ObjectType>\r\n");
        result.append("\t<Comments>\r\n");
        if (commentMap != null && commentMap.size() > 0)
        {
            Iterator entries = commentMap.entrySet().iterator();
            while (entries.hasNext())
            {
                result.append("\t\t<Comment>\r\n");
                Map.Entry entry = (Map.Entry) entries.next();
                long commentId = (Long) entry.getKey();
                String comment = (String) entry.getValue();

                comment = StringUtil.isEmpty(comment) ? "" : XmlUtil
                        .escapeString(comment);

                result.append("\t\t\t<CommentId>").append(commentId)
                        .append("</CommentId>\r\n");
                result.append("\t\t\t<CommentContent>").append(comment)
                        .append("</CommentContent>\r\n");

                ArrayList commentFileList = null;
                try
                {
                    if (companyId != -1)
                    {
                        commentFileList = ServerProxy.getCommentManager()
                                .getCommentReferences(
                                        String.valueOf(commentId), access,
                                        String.valueOf(companyId));
                    }
                    else
                    {
                        commentFileList = ServerProxy.getCommentManager()
                                .getCommentReferences(
                                        String.valueOf(commentId), access);
                    }

                }
                catch (Exception e)
                {

                }

                if (commentFileList != null && commentFileList.size() > 0)
                {
                    result.append("\t\t\t<CommentFiles>\r\n");
                    Iterator commIter = commentFileList.iterator();
                    while (commIter.hasNext())
                    {
                        CommentFile cf = (CommentFile) commIter.next();
                        String cfPath = cf.getAbsolutePath();
                        int index = cfPath.indexOf("CommentReference");
                        String subFilePath = "";
                        if (index > -1)
                        {
                            subFilePath = cfPath.substring(index
                                    + "CommentReference".length() + 1);
                        }
                        StringBuffer cfUrl = new StringBuffer(capLoginUrl);
                        cfUrl.append("/GlobalSight/CommentReference2/").append(
                                subFilePath);
                        if (companyName != null
                                && companyName.trim().length() > 0)
                        {
                            cfUrl.append("?companyName=").append(companyName);
                        }
                        result.append("\t\t\t\t<CommentFileUrl>")
                                .append(cfUrl.toString()
                                        .replaceAll("\\\\", "/"))
                                .append("</CommentFileUrl>\r\n");
                    }
                    result.append("\t\t\t</CommentFiles>\r\n");
                }
                result.append("\t\t</Comment>\r\n");
            }
        }
        result.append("\t</Comments>\r\n");
        result.append("</CommentFilesInformation>\r\n");

        return result.toString();
    }

    /**
     * Get all project TM information.
     * 
     * <P>
     * This API will not return remote tms.
     * </P>
     * 
     * @param p_accessToken
     * @return all project TM information in XML format.
     * 
     * @throws WebServiceException
     */
    public String getAllProjectTMs(String p_accessToken)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "getAllProjectTMs");
        // checkPermission(p_accessToken,
        // Permission.SERVICE_TM_GET_ALL_TMPROFILES);

        Collection allProjectTMs = null;
        try
        {
            allProjectTMs = ServerProxy.getProjectHandler().getAllProjectTMs();
        }
        catch (Exception e)
        {
            String message = "Unable to get all Project TMs.";
            logger.error(message, e);
            message = makeErrorXml("getAllProjectTMs", message);
            throw new WebServiceException(message);
        }

        StringBuffer sbXML = new StringBuffer(XML_HEAD);
        sbXML.append("<ProjectTMInformation>\r\n");
        if (allProjectTMs != null && allProjectTMs.size() > 0)
        {
            Iterator allProjectTMsIt = allProjectTMs.iterator();
            while (allProjectTMsIt.hasNext())
            {
                ProjectTM tm = (ProjectTM) allProjectTMsIt.next();
                // Remote TM won't be returned
                if (tm.getIsRemoteTm() == false)
                {
                    sbXML.append("\t<ProjectTM>\r\n");
                    sbXML.append("\t\t<id>").append(tm.getId())
                            .append("</id>\r\n");
                    sbXML.append("\t\t<name>").append(tm.getName())
                            .append("</name>\r\n");
                    sbXML.append("\t\t<domain>").append(tm.getDomain())
                            .append("</domain>\r\n");
                    sbXML.append("\t\t<organization>")
                            .append(tm.getOrganization())
                            .append("</organization>\r\n");
                    sbXML.append("\t\t<description>")
                            .append(tm.getDescription())
                            .append("</description>\r\n");
                    sbXML.append("\t</ProjectTM>\r\n");
                }
            }
        }

        sbXML.append("</ProjectTMInformation>\r\n");

        return sbXML.toString();
    }

    /**
     * Upload TMX files to server.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_fileName
     *            File name which will be uploaded to server.
     * @param p_contentsInBytes
     *            TMX file contents in byte[].
     * 
     * @throws WebServiceException
     */
    public String uploadTmxFile(String p_accessToken, String p_fileName,
            String p_tmName, byte[] p_contentsInBytes)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_accessToken, "file name");
            Assert.assertNotEmpty(p_accessToken, "tm name");

            checkAccess(p_accessToken, "uploadTmxFile");
            checkPermission(p_accessToken,
                    Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            StringBuffer realPathName = new StringBuffer(webServerDocRoot);
            realPathName.append("_Imports_").append(File.separator)
                    .append("TMX").append(File.separator)
                    .append(p_tmName.trim()).append(File.separator)
                    .append("tmp").append(File.separator).append(p_fileName);

            writeFileToLocale(realPathName.toString(), p_contentsInBytes);

            return null;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return makeErrorXml("uploadTmxFile", e.getMessage());
        }
    }

    /**
     * Import TMX files into specified project tm.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_tmName
     *            Project TM name to import TMX files into.
     * @param p_syncMode
     *            Synchronization options : merge, overwrite, discard. Default
     *            "merge".
     * 
     * @throws WebServiceException
     */
    public void importTmxFile(String p_accessToken, String p_tmName,
            String p_syncMode) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_accessToken, "tm name");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "importTmxFile");
        checkPermission(p_accessToken,
                Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

        /** importOptions */

        com.globalsight.everest.tm.importer.ImportOptions tmImportOptions = new com.globalsight.everest.tm.importer.ImportOptions();
        // syncMode : default "merge"
        tmImportOptions.setSyncMode(ImportOptions.SYNC_MERGE);
        if (p_syncMode != null && !"".equals(p_syncMode.trim()))
        {
            if (p_syncMode.equalsIgnoreCase(ImportOptions.SYNC_MERGE)
                    || p_syncMode
                            .equalsIgnoreCase(ImportOptions.SYNC_OVERWRITE)
                    || p_syncMode.equalsIgnoreCase(ImportOptions.SYNC_DISCARD))
            {
                tmImportOptions.setSyncMode(p_syncMode.toLowerCase());
            }
        }
        // default: all -- all
        tmImportOptions.setSelectedSource("all");
        Collection selectedTargets = new ArrayList();
        selectedTargets.add("all");
        tmImportOptions.setSelectedTargets(selectedTargets);

        /** importer */
        IImportManager importer = TmManagerLocal.getProjectTmImporter(p_tmName);

        /** import tmx files one by one */
        StringBuffer tmxFilePath = new StringBuffer(webServerDocRoot);
        tmxFilePath.append("_Imports_").append(File.separator).append("TMX")
                .append(File.separator).append(p_tmName.trim());
        // saved tmx file directory
        String savedTmxFilePath = tmxFilePath.toString();
        // tmp tmx file directory
        String tmpTmxFilePath = tmxFilePath.append(File.separator)
                .append("tmp").toString();
        File tmxFileDir = new File(tmpTmxFilePath);
        if (tmxFileDir.exists() && tmxFileDir.isDirectory())
        {
            File[] tmxFiles = tmxFileDir.listFiles();
            if (tmxFiles != null && tmxFiles.length > 0)
            {
                for (int i = 0; i < tmxFiles.length; i++)
                {
                    File tmxFile = tmxFiles[i];
                    File savedFile = new File(savedTmxFilePath,
                            tmxFile.getName());
                    try
                    {
                        // validate tmx file and copy its contents to another
                        // file
                        ImportUtil.createInstance().saveTmFileWithValidation(
                                tmxFile, savedFile);
                        // analyze tmx file
                        importer.setImportOptions(tmImportOptions.getXml());
                        importer.setImportFile(savedFile.getAbsolutePath(),
                                false);
                        String options = importer.analyzeFile();
                        // do import
                        importer.setImportOptions(options);
                        importer.doImport();
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                        String msg = makeErrorXml("importTmxFile",
                                e.getMessage());
                        throw new WebServiceException(msg);
                    }
                    // delete tmp TMX files to avoid re-import.
                    tmxFile.delete();
                }
            }
        }
    }

    public String jobsSkipActivity(String p_accessToken, String p_workflowId,
            String p_activity) throws WebServiceException
    {
        String userName = "";
        String returnMsg = null;
        long wfId = 0l;

        // Validate the input arguments
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_workflowId, "workflow Id");
            Assert.assertNotEmpty(p_activity, "activity");

            wfId = Long.parseLong(p_workflowId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        userName = getUsernameFromSession(p_accessToken);
        String userId = UserUtil.getUserIdByName(userName);
        checkAccess(p_accessToken, "jobsSkipActivity");
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_SKIP);

        ArrayList list = new ArrayList();
        Entry<String, String> entry = new Entry<String, String>(p_workflowId,
                p_activity);
        list.add(entry);
        try
        {
            Workflow wf = ServerProxy.getWorkflowManager()
                    .getWorkflowById(wfId);
            if (wf == null
                    || (!UserUtil.isInProject(userId,
                            String.valueOf(wf.getJob().getProjectId())) && !isInSameCompany(
                            userName, String.valueOf(wf.getCompanyId()))))
            {
                returnMsg = makeErrorXml("jobsSkipActivity",
                        "Current user are not in the same company or in project with the job.");
                throw new WebServiceException(returnMsg);
            }

            ServerProxy.getWorkflowServer().setSkipActivity(list, userId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            returnMsg = makeErrorXml("jobsSkipActivity", e.getMessage());
            throw new WebServiceException(returnMsg);
        }
        return null;
    }

    class NewAssignee
    {
        String m_displayRoleName = null;

        String[] m_roles = null;

        boolean m_isUserRole = false;

        NewAssignee(String[] p_roles, String p_displayRoleName,
                boolean p_isUserRole)
        {
            m_displayRoleName = p_displayRoleName;
            m_roles = p_roles;
            m_isUserRole = p_isUserRole;
        }
    }

    /**
     * Reassign task to other translators
     * 
     * @param p_accessToken
     *            Access token
     * @param p_workflowId
     *            ID of workflow
     * @param p_targetLocale
     *            Target locale of workflow which like to reassign
     * @param p_users
     *            Users' information who will be reassigned to The format of
     *            this array of string is, user id,user name
     * @return
     * @throws WebServiceException
     */
    public String jobsReassign(String p_accessToken, String p_workflowId,
            String p_targetLocale, String[] p_users) throws WebServiceException
    {
        String returnMsg = "";
        long wfId = 0l;
        int userLength = 0;

        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_workflowId, "Workflow Id");
            Assert.assertNotEmpty(p_targetLocale, "Target locale");
            wfId = Long.parseLong(p_workflowId);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        if (p_users == null || p_users.length == 0)
        {
            throw new WebServiceException("Users is null");
        }

        checkAccess(p_accessToken, "jobsReassign");
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_REASSIGN);

        try
        {
            userLength = p_users.length;
            Workflow wf = ServerProxy.getWorkflowManager()
                    .getWorkflowById(wfId);
            String srcLocale = wf.getJob().getSourceLocale().toString();
            String targLocale = p_targetLocale;
            List tasks = getTasksInWorkflow(p_workflowId);
            Hashtable taskUserHash = new Hashtable();
            Hashtable taskSelectedUserHash = new Hashtable();

            updateUsers(tasks, taskUserHash, taskSelectedUserHash, wf);

            Enumeration keys = taskUserHash.keys();
            HashMap roleMap = new HashMap();
            String taskId = "", displayRole = "";
            Task task = null;
            ContainerRole containerRole = null;
            Activity activity = null;
            String[] userInfos = null, roles = null;
            Vector newAssignees = null;

            while (keys.hasMoreElements())
            {
                task = (Task) keys.nextElement();
                taskId = String.valueOf(task.getId());
                activity = ServerProxy.getJobHandler()
                        .getActivityByCompanyId(task.getTaskName(),
                                String.valueOf(task.getCompanyId()));
                containerRole = ServerProxy.getUserManager().getContainerRole(
                        activity, srcLocale, targLocale);

                newAssignees = new Vector();
                roles = new String[userLength];
                for (int k = 0; k < userLength; k++)
                {
                    userInfos = p_users[k].split(",");
                    roles[k] = containerRole.getName() + " " + userInfos[0];
                    if (k == userLength - 1)
                    {
                        displayRole += userInfos[1];
                    }
                    else
                    {
                        displayRole += userInfos[1] + ",";
                    }
                }
                newAssignees.addElement(new NewAssignee(roles, displayRole,
                        true));
                roleMap.put(taskId, newAssignees);
            }

            boolean shouldModifyWf = false;
            WorkflowInstance wi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(wfId);

            Vector wfiTasks = wi.getWorkflowInstanceTasks();

            int sz = tasks == null ? -1 : wfiTasks.size();
            for (int j = 0; j < sz; j++)
            {
                WorkflowTaskInstance wti = (WorkflowTaskInstance) wfiTasks
                        .get(j);
                newAssignees = (Vector) roleMap.get(String.valueOf(wti
                        .getTaskId()));

                if (newAssignees != null)
                {
                    for (int r = 0; r < newAssignees.size(); r++)
                    {
                        NewAssignee na = (NewAssignee) newAssignees
                                .elementAt(r);
                        if (na != null
                                && !areSameRoles(wti.getRoles(), na.m_roles))
                        {
                            shouldModifyWf = true;
                            wti.setRoleType(na.m_isUserRole);
                            wti.setRoles(na.m_roles);
                            wti.setDisplayRoleName(na.m_displayRoleName);
                        }
                    }
                }

            }

            // modify one workflow at a time and reset the flag
            if (shouldModifyWf)
            {
                shouldModifyWf = false;
                ServerProxy.getWorkflowManager().modifyWorkflow(null, wi, null,
                        null);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return null;
    }

    /**
     * Add workflows of other languages to job
     * 
     * @param p_accessToken
     * @param p_jobId
     *            Job's id
     * @param p_wfInfos
     *            Workflows of other languages
     * @return
     * @throws WebServiceException
     */
    public String jobsAddLanguages(String p_accessToken, long p_jobId,
            String p_wfInfos) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_wfInfos, "Workflow of languages");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "jobsAddLanguages");
        checkPermission(p_accessToken, Permission.JOB_WORKFLOWS_ADD);

        try
        {
            String[] wfInfoArray = p_wfInfos.split(",");
            WorkflowHandlerHelper.validateStateOfPagesByJobId(p_jobId);
            ArrayList wfInfos = new ArrayList();
            for (int i = 0; i < wfInfoArray.length; i++)
            {
                wfInfos.add(Long.decode(wfInfoArray[i]));
            }
            WorkflowAdditionSender sender = new WorkflowAdditionSender(wfInfos,
                    p_jobId);
            sender.sendToAddWorkflows();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(makeErrorXml("jobsSkipActivity",
                    e.getMessage()));
        }

        return null;
    }

    /**
     * Get available workflows which can be added to selected job
     * 
     * @param p_accessToken
     * @param p_jobId
     * @return
     * @throws WebServiceException
     */
    public String jobsWorkflowCanBeAdded(String p_accessToken, long p_jobId)
            throws WebServiceException
    {
        StringBuilder returnMsg = new StringBuilder();
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        Job job = WorkflowHandlerHelper.getJobById(p_jobId);
        // first validate the state of the existing pages of the job
        WorkflowHandlerHelper.validateStateOfPagesInJob(job);

        List wfInfos = (List) WorkflowHandlerHelper
                .getWorkflowTemplateInfos(job);
        /**
         * here remove DTP workflow Templated, since currently Adding DTP
         * worklfow in a in progress job is not supported.
         */
        for (Iterator it = wfInfos.iterator(); it.hasNext();)
        {
            WorkflowTemplateInfo wfTemplate = (WorkflowTemplateInfo) it.next();
            if (!WorkflowTypeConstants.TYPE_DTP.equals(wfTemplate
                    .getWorkflowType()))
            {
                returnMsg.append(wfTemplate.getId()).append(",");
            }
        }
        if (returnMsg.length() > 1)
            returnMsg.deleteCharAt(returnMsg.length() - 1);

        return returnMsg.toString();
    }

    /**
     * Fetch segments with tm matches according with workflow and source page
     * ids
     * 
     * @param p_accessToken
     * @param p_workflowId
     * @param p_sourcePageIds
     * @return
     * @throws WebServiceException
     */
    public String fetchSegmentsZipped(String p_accessToken,
            String p_workflowId, String p_sourcePageIds)
            throws WebServiceException
    {
        String returnMsg = "";
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_workflowId, "Workflow Id");
            Assert.assertNotEmpty(p_sourcePageIds, "Source page Id");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        checkAccess(p_accessToken, "fetchSegmentsZipped");
        checkPermission(p_accessToken, Permission.ACTIVITIES_ACCEPT);

        long wfId = 0l;
        try
        {
            wfId = Long.parseLong(p_workflowId);
            Workflow wf = ServerProxy.getWorkflowManager()
                    .getWorkflowById(wfId);
            String userName = getUsernameFromSession(p_accessToken);
            User user = ServerProxy.getUserManager().getUserByName(userName);
            String uiLocale = wf.getTargetLocale().toString();

            ArrayList tasks = (ArrayList) ServerProxy.getTaskManager()
                    .getCurrentTasks(wfId);
            if (tasks == null)
            {
                return makeErrorXml("fetchSegmentsZipped",
                        "There is no tasks in workflow.");
            }
            Task task = null;
            boolean isAcceptor = false;
            String jobName;
            List<Boolean> canUseUrlList = new ArrayList<Boolean>();
            int downloadEditAll = -1;
            Vector excludeTypes = null;
            int editorId = -1;
            int platformId = -1;
            String encoding = null;
            int ptagFormat = -1;
            int fileFormat = -1;
            int resInsMode = -1;
            List pageIdList = new ArrayList();
            List pageNameList = new ArrayList();
            List primarySourceFiles = null;
            List supportFileList = null;
            List stfList = null;

            Activity act = new Activity();
            String[] pageIds = null;

            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("<fetchedFileInfo>\r\n");

            for (int i = 0; i < tasks.size(); i++)
            {
                task = (Task) tasks.get(i);
                if (UserUtil.getUserIdByName(userName).equals(
                        task.getAcceptor()))
                {
                    isAcceptor = true;
                    jobName = task.getWorkflow().getJob().getJobName();
                    xml.append("\t<jobName>")
                            .append(EditUtil.encodeXmlEntities(jobName))
                            .append("</jobName>\r\n");
                    if (jobName == null || jobName.trim().length() == 0)
                        jobName = "NoJobName";
                    String urlPrefix = determineUrlPrefix(CompanyWrapper
                            .getCompanyNameById(task.getWorkflow().getJob()
                                    .getCompanyId()));
                    // activity type
                    try
                    {
                        act = ServerProxy.getJobHandler().getActivity(
                                task.getTaskName());
                    }
                    catch (Exception e)
                    {
                    }

                    // create page id and name list
                    xml.append("\t<workflowId>").append(wfId)
                            .append("</workflowId>\r\n");
                    xml.append("\t<taskId>").append(task.getId())
                            .append("</taskId>\r\n");
                    xml.append("\t<pageId>").append(p_sourcePageIds)
                            .append("</pageId>\r\n");
                    pageIds = p_sourcePageIds.split(",");
                    getPageIdList(task, pageIds, pageIdList, pageNameList);
                    if (pageIdList != null && pageIdList.size() <= 0)
                    {
                        pageIdList = pageNameList = null;
                    }

                    // can use url list (legacy stuff we never used but are
                    // keeping for the future)
                    if (pageIdList != null)
                    {
                        for (int j = 0; j < pageIdList.size(); j++)
                        {
                            canUseUrlList.add(Boolean.FALSE);
                        }
                    }

                    Iterator it = task.getWorkflow()
                            .getTargetPages(PrimaryFile.UNEXTRACTED_FILE)
                            .iterator();
                    while (it.hasNext())
                    {
                        if (primarySourceFiles == null)
                            primarySourceFiles = new ArrayList();
                        TargetPage aPTF = (TargetPage) it.next();
                        primarySourceFiles.add(aPTF.getSourcePage()
                                .getIdAsLong());
                    }

                    // get stf list
                    it = task.getWorkflow().getSecondaryTargetFiles()
                            .iterator();
                    while (it.hasNext())
                    {
                        if (stfList == null)
                            stfList = new ArrayList();
                        SecondaryTargetFile aSTF = (SecondaryTargetFile) it
                                .next();
                        stfList.add(aSTF.getIdAsLong());
                    }

                    // get download options for pages if pages are included
                    if (pageIdList != null)
                    {
                        // allow exact match editing
                        L10nProfile l10nProfile = task.getWorkflow().getJob()
                                .getL10nProfile();

                        downloadEditAll = AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED;

                        excludeTypes = l10nProfile
                                .getTranslationMemoryProfile()
                                .getJobExcludeTuTypes();
                        editorId = AmbassadorDwUpConstants.EDITOR_XLIFF;
                        String osname = System.getProperty("os.name");
                        if (osname != null)
                        {
                            if (osname.indexOf("Windows") != -1)
                            {
                                platformId = AmbassadorDwUpConstants.PLATFORM_WIN32;
                            }
                            else if (osname.indexOf("Mac") != -1)
                            {
                                platformId = AmbassadorDwUpConstants.PLATFORM_MAC;
                            }
                            else
                            {
                                platformId = AmbassadorDwUpConstants.PLATFORM_UNIX;
                            }
                        }
                        encoding = "UTF-8";
                        ptagFormat = PseudoConstants.PSEUDO_COMPACT;
                        fileFormat = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF;
                        resInsMode = AmbassadorDwUpConstants.MAKE_RES_TMX_PLAIN;
                    }

                    String displayExactMatch = null;

                    DownloadParams params = new DownloadParams(jobName, null,
                            "", Long.toString(wfId),
                            Long.toString(task.getId()), pageIdList,
                            pageNameList, canUseUrlList, primarySourceFiles,
                            stfList, editorId, platformId, encoding,
                            ptagFormat, uiLocale, task.getSourceLocale(),
                            task.getTargetLocale(), true, fileFormat,
                            excludeTypes, downloadEditAll, supportFileList,
                            resInsMode, user);

                    params.setActivityType(act.getDisplayName());
                    params.setJob(task.getWorkflow().getJob());
                    params.setTermFormat("termGlobalsight");
                    params.setConsolidateTermFiles(false);
                    params.setPopulate100(true);
                    params.setPopulateFuzzy(true);
                    params.setDisplayExactMatch(displayExactMatch);
                    params.setSessionId(null);
                    params.setConsolidateTmxFiles(false);
                    params.setNeedConsolidate(false);
                    params.setChangeCreationIdForMTSegments(false);

                    params.verify();

                    OEMProcessStatus status = new OEMProcessStatus(params);
                    OfflineEditManager odm = ServerProxy
                            .getOfflineEditManager();
                    odm.attachListener(status);
                    odm.processDownloadRequest(params);

                    File tmpFile = null;
                    String downloadFileName = "";
                    if (status != null)
                    {
                        tmpFile = (File) status.getResults();
                        while (tmpFile == null)
                        {
                            tmpFile = (File) status.getResults();
                        }
                        if (params.isSupportFilesOnlyDownload())
                        {
                            downloadFileName = params.getTruncatedJobName()
                                    + AmbassadorDwUpConstants.FILE_NAME_BREAK
                                    + getTargetLocaleCode(params)
                                    + AmbassadorDwUpConstants.FILE_NAME_BREAK
                                    + AmbassadorDwUpConstants.SUPPORTFILES_PACKAGE_SUFFIX
                                    + ".zip";
                        }
                        else
                        {
                            downloadFileName = params.getTruncatedJobName()
                                    + AmbassadorDwUpConstants.FILE_NAME_BREAK
                                    + getTargetLocaleCode(params) + ".zip";
                        }
                        String cxeDocPath = AmbFileStoragePathUtils
                                .getCxeDocDirPath();
                        String targetFile = cxeDocPath + File.separator
                                + downloadFileName;
                        String tmp = "";
                        int count = 0;
                        int k = 0;
                        for (k = cxeDocPath.length() - 1; k >= 0; k--)
                        {
                            if (cxeDocPath.charAt(k) == '/')
                                count++;
                            if (count == 2)
                                break;
                        }
                        tmp = cxeDocPath.substring(k + 1);
                        FileUtil.copyFile(tmpFile, new File(targetFile));
                        returnMsg += capLoginUrl + "/" + tmp + "/"
                                + downloadFileName;
                        xml.append("\t<fileUrl>").append(returnMsg)
                                .append("</fileUrl>\r\n");
                    }
                }
            }
            xml.append("</fetchedFileInfo>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return makeErrorXml("fetchSegmentsZipped", e.getMessage());
            // throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Get all source page id according with idList
     * 
     * @param task
     * @param idList
     *            id of source page
     * @param p_pageIdList
     *            source page id
     * @param p_pageNameList
     *            external page id
     * @throws EnvoyServletException
     */
    private void getPageIdList(Task task, String[] idList, List p_pageIdList,
            List p_pageNameList) throws EnvoyServletException
    {

        if (idList != null)
        {
            Arrays.sort(idList);
        }

        Long pageId = null;
        SourcePage page = null;

        for (int i = 0; idList != null && i < idList.length; i++)
        {
            try
            {
                // Note: download is driven by the source page ids and the
                // target locale
                pageId = new Long(idList[i]);
                page = (SourcePage) ServerProxy.getPageManager().getSourcePage(
                        pageId.longValue());
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            p_pageIdList.add(pageId);
            p_pageNameList.add(page.getExternalPageId());
        }
    }

    /**
     * Get the target locale
     * 
     * @param p_downloadParams
     * @return
     */
    private String getTargetLocaleCode(DownloadParams p_downloadParams)
    {
        String targetLocale = p_downloadParams.getTargetLocale().getLanguage()
                + "_" + p_downloadParams.getTargetLocale().getCountryCode();

        return targetLocale;
    }

    /**
     * Check if the company info of job or project is the same with current user
     */
    private boolean isInSameCompany(String p_userName, String p_companyId)
    {
        if (p_userName == null || p_userName.trim().equals(""))
            return false;
        if (p_companyId == null || p_companyId.trim().equals(""))
            return false;
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(p_userName);
            String userCompanyId = ServerProxy.getJobHandler()
                    .getCompany(user.getCompanyName()).getIdAsLong().toString();
            return userCompanyId.equals(p_companyId) ? true : false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean isInSameCompany(String p_userName, long p_companyId)
    {
        return isInSameCompany(p_userName, String.valueOf(p_companyId));
    }

    /**
     * Get tasks in specified workflow
     * 
     * @param p_wfId
     *            workflow ID
     * @return ArrayList Collection of tasks which is in the specified workflow
     * @throws WebServiceException
     */
    private ArrayList getTasksInWorkflow(String p_wfId)
            throws WebServiceException
    {
        ArrayList<Task> tasks = new ArrayList<Task>();
        long wfId = 0l;

        // Validate workflow ID
        if (p_wfId == null || p_wfId.trim().length() == 0)
            return tasks;
        try
        {
            wfId = Long.parseLong(p_wfId);
        }
        catch (NumberFormatException nfe)
        {
            throw new WebServiceException("Wrong workflow ID");
        }

        try
        {
            WorkflowInstance workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(wfId);
            Workflow workflow = ServerProxy.getWorkflowManager()
                    .getWorkflowByIdRefresh(wfId);
            Hashtable tasksInWF = workflow.getTasks();

            // get the NodeInstances of TYPE_ACTIVITY
            List<WorkflowTaskInstance> nodesInPath = workflowInstance
                    .getDefaultPathNode();

            for (WorkflowTaskInstance task : nodesInPath)
            {
                Task taskInfo = (Task) tasksInWF.get(task.getTaskId());

                if (taskInfo.reassignable())
                {
                    tasks.add(taskInfo);
                }
            }
            return tasks;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Get the list of users for each Review-Only activity.
     */
    @SuppressWarnings("unchecked")
    private void updateUsers(List p_tasks, Hashtable p_taskUserHash,
            Hashtable p_taskSelectedUserHash, Workflow p_wf)
            throws GeneralException, RemoteException
    {
        Project proj = p_wf.getJob().getL10nProfile().getProject();
        for (Iterator iter = p_tasks.iterator(); iter.hasNext();)
        {
            Hashtable userHash = new Hashtable();
            Hashtable selectedUserHash = new Hashtable();
            Task task = (Task) iter.next();

            List selectedUsers = null;
            long taskId = task.getId();
            WorkflowTaskInstance wfTask = p_wf.getIflowInstance()
                    .getWorkflowTaskById(taskId);
            String[] roles = wfTask.getRoles();
            String[] userIds = ServerProxy.getUserManager()
                    .getUserIdsFromRoles(roles, proj);
            if ((userIds != null) && (userIds.length > 0))
            {
                selectedUsers = ServerProxy.getUserManager().getUserInfos(
                        userIds);
            }

            // get all users for this task and locale pair.
            List userInfos = ServerProxy.getUserManager().getUserInfos(
                    task.getTaskName(), task.getSourceLocale().toString(),
                    task.getTargetLocale().toString());
            Set projectUserIds = null;
            if (proj != null)
            {
                projectUserIds = proj.getUserIds();
            }

            if (userInfos == null)
                continue;

            for (Iterator iter2 = userInfos.iterator(); iter2.hasNext();)
            {
                UserInfo userInfo = (UserInfo) iter2.next();
                // filter user by project
                if (projectUserIds != null)
                {
                    String userId = userInfo.getUserId();
                    // if the specified user is contained in the project
                    // then add to the Hash.
                    if (projectUserIds.contains(userId))
                    {
                        userHash.put(userInfo.getUserId(), userInfo);
                    }
                }
            }
            p_taskUserHash.put(task, userHash);
            if (selectedUsers == null)
                continue;

            for (Iterator iter3 = selectedUsers.iterator(); iter3.hasNext();)
            {
                UserInfo ta = (UserInfo) iter3.next();
                selectedUserHash.put(ta.getUserId(), ta);
            }
            p_taskSelectedUserHash.put(task, selectedUserHash);
        }
    }

    /**
     * Determines whether the two array of roles contain the same set of role
     * names.
     */
    private boolean areSameRoles(String[] p_workflowRoles,
            String[] p_selectedRoles)
    {
        // First need to sort since Arrays.equals() requires
        // the parameters to be sorted
        Arrays.sort(p_workflowRoles);
        Arrays.sort(p_selectedRoles);
        return Arrays.equals(p_workflowRoles, p_selectedRoles);
    }

    /**
     * To dispatch workflows. Each workflow can be dispatched only when its
     * state is READY_TO_BE_DISPATCHED
     * 
     * @param p_accessToken
     *            Access token
     * @param p_wfIds
     *            String of one or more workflow IDs Using "," to split more
     *            workflow IDs
     * @return If success, then return null. If fail, return error message
     * @throws WebServiceException
     */
    public String dispatchWorkflow(String p_accessToken, String p_wfIds)
            throws WebServiceException
    {
        String message = "";

        // Validate inputting parameters
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_wfIds, "Workflow IDs");
        }
        catch (Exception e)
        {
            message = makeErrorXml("dispatchWorkflow", "Invaild parameters");
            throw new WebServiceException(message);
        }

        // User muse be in Administrator or PM group
        String username = getUsernameFromSession(p_accessToken);
        if (!UserUtil.isInPermissionGroup(username,
                Permission.GROUP_PROJECT_MANAGER)
                && !UserUtil.isInPermissionGroup(username,
                        Permission.GROUP_ADMINISTRATOR))
            throw new WebServiceException(makeErrorXml(
                    "dispatchWorkflow",
                    "User ".concat(username).concat(
                            " do NOT have permission to run this")));

        long wfId = 0l;
        ArrayList<Long> wfIdsArray = new ArrayList<Long>();
        String[] wfIds = null;
        Workflow wf = null;
        String wfIdString = "";
        ArrayList<String> failIdsArrayList = new ArrayList<String>();

        wfIds = p_wfIds.split(",");
        int length = wfIds.length;
        for (int i = 0; i < length; i++)
        {
            wfIdString = wfIds[i].trim();
            if (wfIdString.equals(""))
                continue;
            try
            {
                wfId = Long.parseLong(wfIdString);
                wfIdsArray.add(Long.valueOf(wfId));
            }
            catch (NumberFormatException nfe)
            {
                failIdsArrayList.add(wfIdString);
            }
        }
        try
        {
            WorkflowManagerWLRemote wfm = ServerProxy.getWorkflowManager();
            String projectId = null;
            for (int i = 0; i < wfIdsArray.size(); i++)
            {
                wfId = wfIdsArray.get(i).longValue();
                try
                {
                    wf = wfm.getWorkflowById(wfId);
                    projectId = String.valueOf(wf.getJob().getProjectId());
                    if (UserUtil.isInProject(username, projectId))
                        wfm.dispatch(wf);
                    else
                        failIdsArrayList.add(String.valueOf(wfId));
                }
                catch (WorkflowManagerException wfe)
                {
                    logger.error(wfe.getMessage(), wfe);
                    failIdsArrayList.add(String.valueOf(wfId));
                }
            }
            if (failIdsArrayList != null && failIdsArrayList.size() > 0)
            {
                message = "Invaild inputting workflow ID -- "
                        .concat(failIdsArrayList.toString());
                message = makeErrorXml("dispatchWorkflow", message);
                throw new WebServiceException(message);
            }
            return null;
        }
        catch (Exception e)
        {
            message = makeErrorXml("dispatchWorkflow", e.getMessage());
            throw new WebServiceException(message);
        }
    }

    private String replaceAndString(String p_str)
    {
        if (p_str == null || p_str.trim().equals(""))
            return "";
        StringBuffer sb = new StringBuffer();
        char[] chars = p_str.toCharArray();
        for (char c : chars)
        {
            if (c == '&')
                sb.append("&amp;");
            else
                sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Get jobs info created after start time
     * 
     * @param accessToken
     *            Access token
     * @param startTime
     *            Start time, it can be '2d', '8h' and '2d8h' format
     * @return String XML format string
     * @throws WebServiceException
     * 
     * @author Vincent Yan
     * @since 8.2.3
     */
    public String getJobsByTimeRange(String accessToken, String startTime)
            throws WebServiceException
    {
        return getJobsByTimeRange(accessToken, startTime, 0);
    }

    /**
     * Get jobs info created after start time and in one project
     * 
     * @param accessToken
     *            Access token
     * @param startTime
     *            Start time, it can be '2d', '8h' and '2d8h' format
     * @parma projectId Id of project
     * @return String XML format string
     * @throws WebServiceException
     * 
     * @author Vincent Yan
     * @since 8.2.3
     */
    public String getJobsByTimeRange(String accessToken, String startTime,
            long projectId) throws WebServiceException
    {
        StringBuilder xml = new StringBuilder(XML_HEAD);
        xml.append("<jobs>\r\n");
        boolean canRun = true;
        try
        {
            if (StringUtil.isEmpty(accessToken)
                    || StringUtil.isEmpty(startTime)
                    || !validateTimeRange(startTime) || projectId < 0)
            {
                return makeErrorXml("getJobsByTimeRange",
                        "Invaild time range parameter.");
            }
            int hours = getHours(startTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 0 - hours);

            User user = getUser(getUsernameFromSession(accessToken));
            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());

            JobSearchParameters searchParameters = new JobSearchParameters();
            if (projectId > 0)
                searchParameters.setProjectId(String.valueOf(projectId));

            searchParameters.setCreationStart(calendar.getTime());

            String hql = "from JobImpl j where j.createDate>='"
                    + sdf.format(calendar.getTime()) + "'";
            if (!CompanyWrapper.isSuperCompanyName(user.getCompanyName()))
            {
                long companyId = CompanyWrapper.getCompanyByName(
                        user.getCompanyName()).getId();
                hql += " and j.companyId=" + companyId;
            }
            Collection collection = HibernateUtil.search(hql);

            // Collection collection =
            // ServerProxy.getJobHandler().getJobs(searchParameters);
            if (collection != null && collection.size() > 0)
            {
                ArrayList<JobImpl> jobs = new ArrayList<JobImpl>(collection);
                Job job = null;
                for (JobImpl ji : jobs)
                {
                    job = (Job) ji;
                    if (job == null)
                        continue;

                    if (projectId > 0 && projectId != job.getProjectId())
                        continue;

                    if (!isInSameCompany(user.getUserName(),
                            String.valueOf(job.getCompanyId()))
                            && !UserUtil.isSuperAdmin(user.getUserId())
                            && !UserUtil.isSuperPM(user.getUserId()))
                        continue;

                    xml.append(getJobInfo(job));
                }
            }
        }
        catch (Exception e)
        {
            return makeErrorXml("getJobsByTimeRange",
                    "Cannot get jobs correctly. " + e.getMessage());
        }

        xml.append("</jobs>\r\n");
        return xml.toString();
    }

    private int getHours(String startTime)
    {
        int hours = 0, index = 0, days = 0;
        String lowerString = startTime.toLowerCase(), tmp = "";
        try
        {
            if ((index = lowerString.indexOf("d")) > 0)
            {
                tmp = lowerString.substring(0, index);
                days = Integer.parseInt(tmp);
                hours = days * 24;
                if (lowerString.indexOf("h", index) > 0)
                {
                    tmp = lowerString.substring(index + 1,
                            lowerString.length() - 1);
                    hours += Integer.parseInt(tmp);
                }
            }
            else
            {
                tmp = lowerString.substring(0, lowerString.length() - 1);
                hours = Integer.parseInt(tmp);
            }
            return hours;
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    private boolean validateTimeRange(String startTime)
    {
        int dayIndex = -1, hourIndex = -1;
        String lowerString = startTime.toLowerCase();
        String tmp = "";
        dayIndex = lowerString.indexOf("d");
        hourIndex = lowerString.indexOf("h");
        if (dayIndex == -1 && hourIndex == -1)
            return false;
        if (dayIndex == 0 || hourIndex == 0)
            return false;

        if (dayIndex > 0)
        {
            if (lowerString.indexOf("d", dayIndex + 1) > -1)
                return false;
            if (hourIndex > 0 && (hourIndex - dayIndex) < 2)
                return false;
        }

        if (hourIndex > 0)
        {
            if (lowerString.indexOf("h", hourIndex + 1) > -1)
                return false;
        }
        return true;
    }

    private String getJobInfo(Job job) throws WebServiceException
    {
        StringBuilder xml = new StringBuilder();

        try
        {
            xml.append("\t<job>\r\n");
            // job id
            xml.append("\t\t<id>").append(job.getId()).append("</id>\r\n");
            // job name
            xml.append("\t\t<name>")
                    .append(EditUtil.encodeXmlEntities(job.getJobName()))
                    .append("</name>\r\n");
            // job state
            xml.append("\t\t<state>").append(job.getState())
                    .append("</state>\r\n");
            // job create date
            xml.append("\t\t<create_date>")
                    .append(DateHelper.getFormattedDateAndTime(
                            job.getCreateDate(), null))
                    .append("</create_date>\r\n");
            // company id
            xml.append("\t\t<company_id>").append(job.getCompanyId())
                    .append("</company_id>\r\n");
            // company name
            xml.append("\t\t<company_name>")
                    .append(CompanyWrapper.getCompanyNameById(job
                            .getCompanyId())).append("</company_name>\r\n");
            // project id
            xml.append("\t\t<project_id>").append(job.getProjectId())
                    .append("</project_id>\r\n");
            // project name
            xml.append("\t\t<project_name>").append(job.getProject().getName())
                    .append("</project_name>\r\n");
            // job priority
            xml.append("\t\t<priority>").append(job.getPriority())
                    .append("</priority>\r\n");
            // source locale
            xml.append("\t\t<source_locale>").append(job.getSourceLocale())
                    .append("</source_locale>\r\n");
            // page count
            xml.append("\t\t<page_count>").append(job.getPageCount())
                    .append("</page_count>\r\n");
            // word count
            xml.append("\t\t<word_count>").append(job.getWordCount())
                    .append("</word_count>\r\n");

            Collection wfs = job.getWorkflows();

            xml.append("\t\t<workflows>\r\n");

            Workflow workflow = null;
            TaskInstance taskInstance = null;
            String currentTaskName = "", tmp = "";

            for (Iterator wfi = wfs.iterator(); wfi.hasNext();)
            {
                currentTaskName = "";
                workflow = (Workflow) wfi.next();

                tmp = getWorkflowInfo(workflow, "\t\t\t");
                xml.append(tmp);
            }
            xml.append("\t\t</workflows>\r\n");
            xml.append("\t</job>\r\n");

            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error(GET_JOB_AND_WORKFLOW_INFO, e);
            String message = "Could not get information for job " + job.getId();
            message = makeErrorXml("getJobInfo", message);
            throw new WebServiceException(message);
        }
    }

    /**
     * Generate workflow info in xml format
     * 
     * @param workflow
     *            workflow object
     * @param tab
     *            Tab string as prefix, such as '\t\t'
     * @return String workflow info in xml format
     */
    private String getWorkflowInfo(Workflow workflow, String tab)
    {
        StringBuilder xml = new StringBuilder();
        TaskInstance taskInstance = null;

        // workflow
        xml.append(tab).append("<workflow>\r\n");
        // workflow id
        xml.append(tab).append("\t<workflow_id>").append(workflow.getId())
                .append("</workflow_id>\r\n");
        // workflow state
        xml.append(tab).append("\t<workflow_state>")
                .append(workflow.getState()).append("</workflow_state>\r\n");

        xml.append(tab).append("\t<target_locale>")
                .append(workflow.getTargetLocale())
                .append("</target_locale>\r\n");
        xml.append(tab)
                .append("\t<dispatch_date>")
                .append(workflow.getDispatchedDate() == null ? "" : DateHelper
                        .getFormattedDateAndTime(workflow.getDispatchedDate(),
                                null)).append("</dispatch_date>\r\n");
        // tasks
        Hashtable<Long, Task> tasks = (Hashtable<Long, Task>) workflow
                .getTasks();
        Rate rate = null;
        if (tasks == null || tasks.size() == 0)
        {
            xml.append(tab).append("\t<tasks>\r\n").append(tab)
                    .append("\t</tasks>\r\n");
        }
        else
        {
            xml.append(tab).append("\t<tasks>\r\n");

            // each task
            Long taskId = null;
            Task task = null;
            String tmp = "";
            for (Iterator<Long> ids = tasks.keySet().iterator(); ids.hasNext();)
            {
                taskId = ids.next();
                // task = tasks.get(taskId);
                try
                {
                    task = ServerProxy.getTaskManager().getTask(taskId);
                }
                catch (Exception e)
                {
                }

                tmp = getTaskInfo(task, tab + "\t\t");
                xml.append(tmp);
            }

            xml.append(tab).append("\t</tasks>\r\n");
        }

        // current activity
        taskInstance = WorkflowManagerLocal.getCurrentTask(workflow.getId());
        String currentTaskName = "";
        if (taskInstance != null)
        {
            currentTaskName = TaskJbpmUtil.getTaskDisplayName(taskInstance
                    .getName());
            xml.append(tab).append("\t<current_activity>")
                    .append(currentTaskName).append("</current_activity>\r\n");
        }

        /** Wordcount Summary */
        xml.append(tab).append("\t<word_counts>\r\n");
        // leverageOption
        String leverageOption = "unknown";
        boolean isInContextMatch = false;
        boolean isDefaultContextMatch = false;
        try
        {
            // TranslationMemoryProfile tmprofile = ServerProxy
            // .getProjectHandler()
            // .getL10nProfile(workflow.getJob().getL10nProfileId())
            // .getTranslationMemoryProfile();
            // if (tmprofile != null)
            // {
            // isInContextMatch = tmprofile.getIsContextMatchLeveraging();
            // isDefaultContextMatch = PageHandler
            // .isDefaultContextMatch(workflow.getJob());
            // }

            Job job = workflow.getJob();
            if (job.DEFAULT_CONTEXT.equals(job.getLeverageOption()))
            {
                isDefaultContextMatch = true;
            }
            else if (job.IN_CONTEXT.equals(job.getLeverageOption()))
            {
                isInContextMatch = true;
            }

            if (isInContextMatch)
            {
                leverageOption = "Leverage in context matches";
            }
            else if (isDefaultContextMatch)
            {
                leverageOption = "Default";
            }
            else
            {
                leverageOption = "100% match only";
            }
        }
        catch (Exception e)
        {

        }
        xml.append(tab).append("\t\t<leverage_option>").append(leverageOption)
                .append("</leverage_option>\r\n");
        // 100%
        int wc = 0;
        if (isInContextMatch)
        {
            wc = workflow.getSegmentTmWordCount();
        }
        else if (isDefaultContextMatch)
        {
            wc = workflow.getTotalExactMatchWordCount()
                    - workflow.getContextMatchWordCount();
        }
        else
        {
            wc = workflow.getTotalExactMatchWordCount();
        }
        xml.append(tab).append("\t\t<match_100_percent>").append(wc)
                .append("</match_100_percent>\r\n");
        // 95%-99%
        xml.append(tab).append("\t\t<match_95_percent-99_percent>")
                .append(workflow.getThresholdHiFuzzyWordCount())
                .append("</match_95_percent-99_percent>\r\n");
        // 85%-94%
        xml.append(tab).append("\t\t<match_85_percent-94_percent>")
                .append(workflow.getThresholdMedHiFuzzyWordCount())
                .append("</match_85_percent-94_percent>\r\n");
        // 75%-84%
        xml.append(tab).append("\t\t<match_75_percent-84_percent>")
                .append(workflow.getThresholdMedFuzzyWordCount())
                .append("</match_75_percent-84_percent>\r\n");
        // noMatch (50%-74%)
        xml.append(tab).append("\t\t<no_match>")
                .append(workflow.getThresholdNoMatchWordCount())
                .append("</no_match>\r\n");
        // Repetitions
        xml.append(tab)
                .append("\t\t<repetitions>")
                .append(workflow.getRepetitionWordCount())
                .append("</repetitions>\r\n");
        // In Context Matches
        if (isInContextMatch)
        {
            xml.append(tab).append("\t\t<in_context_matches>")
                    .append(workflow.getInContextMatchWordCount())
                    .append("</in_context_matches>\r\n");
        }
        // Context Matches
        if (isDefaultContextMatch)
        {
            xml.append(tab).append("\t\t<context_matches>")
                    .append(workflow.getContextMatchWordCount())
                    .append("</context_matches>\r\n");
        }
        // total
        xml.append(tab).append("\t\t<total>")
                .append(workflow.getTotalWordCount()).append("</total>\r\n");
        xml.append(tab).append("\t</word_counts>\r\n");

        if (workflow.getCompletedDate() != null)
        {
            xml.append(tab)
                    .append("\t<complete_date>")
                    .append(DateHelper.getFormattedDateAndTime(
                            workflow.getCompletedDate(), null))
                    .append("</complete_date>\r\n");
        }

        xml.append(tab).append("</workflow>\r\n");

        return xml.toString();
    }

    /**
     * Generate task info in xml format
     * 
     * @param task
     *            task object
     * @param tab
     *            tab string as prefix, such as '\t\t'
     * @return String task info in xml format
     */
    private String getTaskInfo(Task task, String tab)
    {
        StringBuilder xml = new StringBuilder();
        String tmp = "";
        Rate rate = null;

        WorkflowTaskInstance wfTask;
        try
        {
            wfTask = ServerProxy.getWorkflowServer().getWorkflowTaskInstance(
                    task.getWorkflow().getId(), task.getId());
            task.setWorkflowTask(wfTask);
        }
        catch (Exception e)
        {
        }

        xml.append(tab).append("<task>\r\n");

        // task id
        xml.append(tab).append("\t<task_id>").append(task.getId())
                .append("</task_id>\r\n");
        // task name
        xml.append(tab).append("\t<task_name>").append(task.getTaskName())
                .append("</task_name>\r\n");
        // task duration hours
        xml.append(tab).append("\t<task_duration_hours>")
                .append(task.getDurationString())
                .append("</task_duration_hours>\r\n");
        // task state
        tmp = task.getStateAsString() == null ? "" : task.getStateAsString();
        xml.append(tab).append("\t<task_state>").append(tmp)
                .append("</task_state>\r\n");
        // task accept date
        tmp = task.getAcceptedDate() == null ? "" : DateHelper
                .getFormattedDateAndTime(task.getAcceptedDate(), null);
        xml.append(tab).append("\t<task_accept_date>").append(tmp)
                .append("</task_accept_date>\r\n");
        // task assignees
        if (task.getAllAssignees() == null
                || task.getAllAssignees().size() == 0)
            tmp = task.getPossibleAssignee();
        else
            tmp = task.getAllAssigneesAsString();
        xml.append(tab).append("\t<task_assignees>").append(tmp)
                .append("</task_assignees>\r\n");
        // task acceptor
        tmp = task.getAcceptor() == null ? "" : UserUtil.getUserNameById(task
                .getAcceptor());
        xml.append(tab).append("\t<task_acceptor>").append(tmp)
                .append("</task_acceptor>\r\n");
        // task expense rate
        rate = task.getExpenseRate();
        tmp = rate == null ? "" : String.valueOf(rate.getId());
        xml.append(tab).append("\t<task_expense_rate_id>").append(tmp)
                .append("</task_expense_rate_id>\r\n");
        // task revenue rate
        rate = task.getRevenueRate();
        tmp = rate == null ? "" : String.valueOf(rate.getId());
        xml.append(tab).append("\t<task_revenue_rate_id>").append(tmp)
                .append("</task_revenue_rate_id>\r\n");

        xml.append(tab).append("</task>\r\n");

        return xml.toString();
    }

    /**
     * Get all localization profiles
     * 
     * @param accessToken
     *            Access token
     * @return String XML format string
     * @throws WebServiceException
     * 
     * @author Vincent Yan
     * @since 8.2.3
     */
    public String getAllL10NProfiles(String accessToken)
            throws WebServiceException
    {
        if (StringUtil.isEmpty(accessToken))
            return makeErrorXml("getAllL10NProfiles", "Invaild access token");

        checkAccess(accessToken, "getAllL10NProfiles");

        StringBuilder xml = new StringBuilder(XML_HEAD);
        xml.append("<l10n_profiles>\r\n");
        try
        {
            User user = getUser(getUsernameFromSession(accessToken));
            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
            ArrayList<BasicL10nProfileInfo> profiles = new ArrayList<BasicL10nProfileInfo>(
                    ServerProxy.getProjectHandler().getAllL10nProfilesForGUI());
            if (profiles != null && profiles.size() > 0)
            {
                for (BasicL10nProfileInfo lp : profiles)
                {
                    xml.append("\t<l10n_profile>\r\n");
                    xml.append("\t\t<id>").append(lp.getProfileId())
                            .append("</id>\r\n");
                    xml.append("\t\t<name>").append(lp.getName())
                            .append("</name>\r\n");
                    xml.append("\t</l10n_profile>\r\n");
                }
            }
        }
        catch (Exception e)
        {
            return makeErrorXml("getAllL10NProfiles", e.getMessage());
        }
        xml.append("</l10n_profiles>");
        return xml.toString();
    }

    /**
     * Get workflow path with specified id
     * 
     * @param p_accessToken
     *            Access token
     * @param workflowId
     *            Id of workflow
     * @return String XML format string
     * @throws RemoteException
     * @throws WebServiceException
     * 
     * @author Vincent Yan
     * @since 8.2.3
     */
    public String getWorkflowPath(String p_accessToken, long workflowId)
            throws RemoteException, WebServiceException
    {
        if (StringUtil.isEmpty(p_accessToken))
            return makeErrorXml("getWorkflowPath", "Invaild access token.");
        checkAccess(p_accessToken, "getWorkflowPath");

        String userName = getUsernameFromSession(p_accessToken);
        String userId = UserUtil.getUserIdByName(userName);
        Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(
                workflowId);

        if (wf == null)
        {
            return makeErrorXml("getWorkflowPath",
                    "Invaild workflow which is not exist.");
        }

        if (!isInSameCompany(userName, String.valueOf(wf.getCompanyId())))
        {
            if (!UserUtil.isSuperAdmin(userId) && !UserUtil.isSuperPM(userId))
            {
                return makeErrorXml("getWorkflowPath",
                        "Current user have not permissions to access the workflow.");
            }
        }

        StringBuilder xml = new StringBuilder(XML_HEAD);
        xml.append("<workflow>\r\n");
        xml.append("\t<id>").append(workflowId).append("</id>\r\n");

        List<WorkflowTaskInstance> taskList = new ArrayList<WorkflowTaskInstance>();
        JbpmContext ctx = null;
        WorkflowInstance workflowInstance = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx
                    .getProcessInstance(workflowId);
            workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(processInstance);

            Vector tasks = workflowInstance.getWorkflowInstanceTasks();
            WorkflowTaskInstance[] tasksArray = WorkflowJbpmUtil
                    .convertToArray(tasks);
            ArrorInfo arror = null;
            ArrayList<WfTaskInfo> tmp = new ArrayList<WfTaskInfo>(ServerProxy
                    .getWorkflowServer().timeDurationsInDefaultPath("Exit",
                            workflowId, -1));
            Task task = null;
            for (WfTaskInfo taskInfo : tmp)
            {
                task = ServerProxy.getTaskManager().getTask(taskInfo.getId());
                xml.append("\t<task>\r\n");
                xml.append("\t\t<id>").append(task.getId()).append("</id>\r\n");
                xml.append("\t\t<name>").append(task.getTaskDisplayName())
                        .append("</name>\r\n");
                xml.append("\t\t<state>").append(task.getStateAsString())
                        .append("</state>\r\n");
                xml.append("\t</task>\r\n");
            }
        }
        catch (Exception e)
        {
            return makeErrorXml("getWorkflowPath", "Error: " + e.getMessage());
        }
        finally
        {
            ctx.close();
        }

        xml.append("</workflow>");
        return xml.toString();
    }

    /**
     * Download offline file with xliff format
     * 
     * @param accessToken
     *            Access token
     * @param taskId
     *            Task ID
     * @return String file name of generated offline file, it typical is package
     *         file
     * @throws WebServiceException
     * @throws RemoteException
     * @throws NamingException
     */
    public String downloadXliffOfflineFile(String accessToken, String taskId)
            throws WebServiceException, RemoteException, NamingException
    {
        if (StringUtil.isEmpty(accessToken))
            return makeErrorXml(DOWNLOAD_XLIFF_OFFLINE_FILE,
                    "Invaild access token.");
        if (StringUtil.isEmpty(taskId) || Long.parseLong(taskId) < 1)
            return makeErrorXml(DOWNLOAD_XLIFF_OFFLINE_FILE, "Invaild task id.");

        // Check access token
        checkAccess(accessToken, "downloadXliffOfflineFile");
        // Check user's permission
        checkPermission(accessToken, Permission.ACTIVITIES_WORKOFFLINE);

        StringBuilder returnXml = new StringBuilder(XML_HEAD);
        long taskID = Long.parseLong(taskId);
        Task task = ServerProxy.getTaskManager().getTask(taskID);
        String user = getUsernameFromSession(accessToken);
        if (task == null || !user.equals(task.getAcceptor()))
            return makeErrorXml("downloadXliffOfflineFile",
                    "Current user is not the acceptor of this task.");

        long jobId = task.getJobId();
        Job job = ServerProxy.getJobHandler().getJobById(jobId);

        WorkflowManagerLocal workflowManager = new WorkflowManagerLocal();
        try
        {
            // Generate offline page data to file
            workflowManager.downloadOfflineFiles(task, job, null);

            // Copy offline file from $WebRoot$\_Exports_\QA\ to
            // $DocDir$\download\
            // Filename is like test2_589424576_en_US_de_DE.zip
            String directory = ExportUtil.getExportDirectory();
            String companyId = CompanyThreadLocal.getInstance().getValue();
            String companyName = ServerProxy.getJobHandler()
                    .getCompanyById(Long.parseLong(companyId)).getName();
            String filename = job.getJobName() + "_" + task.getSourceLocale()
                    + "_" + task.getTargetLocale() + ".zip";

            File temp = new File(directory + companyName, filename);
            File targetFile = new File(
                    AmbFileStoragePathUtils.getCxeDocDirPath() + File.separator
                            + AmbFileStoragePathUtils.OFFLINE_FILE_DOWNLOAD_DIR
                            + File.separator + filename);
            FileUtil.copyFile(temp, targetFile);

            // Generate response xml content
            returnXml.append("<offlineFiles>\r\n");
            String urlPrefix = determineUrlPrefix(CompanyWrapper
                    .getCompanyNameById(job.getCompanyId()));

            returnXml.append("\t").append(urlPrefix).append("/")
                    .append(AmbFileStoragePathUtils.OFFLINE_FILE_DOWNLOAD_DIR)
                    .append("/").append(EditUtil.encodeXmlEntities(filename))
                    .append("\r\n");
            returnXml.append("</offlineFiles>\r\n");
        }
        catch (Exception e)
        {
            logger.error("Error found in downloadXliffOfflineFile.", e);
            return makeErrorXml(DOWNLOAD_XLIFF_OFFLINE_FILE,
                    "Error info: " + e.toString());
        }
        return returnXml.toString();
    }
}
