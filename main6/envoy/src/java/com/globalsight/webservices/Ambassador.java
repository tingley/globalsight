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

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.UPDATED_BY_PROJECT;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
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
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.offline.OfflineFileUploadStatus;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.page.TmxUtil;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.BasicL10nProfileInfo;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatchLingManagerLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobEditionInfo;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobGroup;
import com.globalsight.everest.jobhandler.JobHandlerWLRemote;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.JobPersistenceAccessor;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.ProjectHandlerLocal;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.projecthandler.ProjectTmTuvT;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.projecthandler.importer.ImportOptions;
import com.globalsight.everest.qachecks.DITAQACheckerHelper;
import com.globalsight.everest.qachecks.QAChecker;
import com.globalsight.everest.qachecks.QACheckerHelper;
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
import com.globalsight.everest.tm.exporter.TmxWriter;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManagerWLRemote;
import com.globalsight.everest.usermgr.LdapHelper;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.CharacterCountReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.PostReviewQAReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsSimpleReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.TranslationsEditReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSummaryHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.tm.corpus.OverridableLeverageOptions;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.ConditionNodeTargetInfo;
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
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerException;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.everest.workflowmanager.WorkflowManagerWLRemote;
import com.globalsight.everest.workflowmanager.WorkflowPersistenceAccessor;
import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.common.XmlEntities;
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
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.tm3.core.BaseTm;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.log.ActivityLog;
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
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.ServerUtil;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlParser;
import com.globalsight.util.date.DateHelper;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.AmbassadorHelper.TaskJbpmNode;
import com.globalsight.webservices.AmbassadorHelper.TaskJbpmTransition;
import com.globalsight.webservices.attribute.AddJobAttributeThread;
import com.globalsight.webservices.attribute.AttributeUtil;
import com.globalsight.webservices.attribute.Attributes;
import com.globalsight.webservices.attribute.DateJobAttributeVo;
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

    public static final String ARCHIVE_JOB = "archiveJob";
    
    public static final String GET_IMPORT_EXPORT_STATUS = "getImportExportStatus";

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
    
	public static final String EXPORT_TM = "exportTM";
	
	public static final String TM_FULL_TEXT_SEARCH = "tmFullTextSearch";
	
	public static final String TM_EXPORT_STATUS = "getTmExportStatus";
	
	public static final String CREATE_JOB_GROUP ="createJobGroup";
	
	public static final String ADD_JOB_TO_GROUP ="addJobToGroup";

    public static final String GET_JOB_EXPORT_WORKFLOW_FILES = "getJobExportWorkflowFiles";

    public static final String GET_WORK_OFFLINE_FILES = "getWorkOfflineFiles";
    public static final String UPLOAD_WORK_OFFLINE_FILES = "uploadWorkOfflineFiles";
    public static final String IMPORT_WORK_OFFLINE_FILES = "importWorkOfflineFiles";

    public static final String GENERATE_TRANSLATION_EDIT_REPORT = "generateTranslationEditReport";
    public static final String GENERATE_CHARACTER_COUNT_REPORT = "generateCharacterCountReport";
    public static final String GENERATE_REVIEWERS_COMMENT_REPORT = "generateReviewersCommentReport";
    public static final String GENERATE_REVIEWERS_COMMENT_SIMPLIFIED_REPORT = "generateReviewersCommentSimplifiedReport";
    public static final String GENERATE_POST_REVIEW_QA_REPORT = "generatePostReviewQAReport";

    public static final String GENERATE_DITA_QA_REPORT = "generateDITAQAReport";
    public static final String GENERATE_QA_CHECKS_REPORT = "generateQAChecksReport";
    public static final String DOWNLOAD_QA_CHECKS_REPORTS = "downloadQAChecksReports";
    
    public static final String GET_IN_CONTEXT_REVIEW_LINK = "getInContextReviewLink";

    public static String ERROR_JOB_NAME = "You cannot have \\, /, :, ;, *, ?, |, \", &lt;, &gt;, % or &amp; in the Job Name.";

    public static String ERROR_JOB_GROUP_NAME = "You cannot have \\, /, :, ;, ,,.,*, ?,!,$,#,@,[,],{,},(,),^,+,=,~, |, \',\", &lt;, &gt;, % or &amp; in the Job Group Name.";
    
    public static String ERROR_EXPORT_FILE_NAME = "You cannot have \\, /, :, ;, ,,.,*, ?,!,$,#,@,[,],{,},(,),^,+,=,~, |, \',\", &lt;, &gt;, % or &amp; in the Export File Name.";
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger logger = Logger.getLogger(Ambassador.class);

    // jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/
    private static String webServerDocRoot = null;

    static public final String DEFAULT_TYPE = "text";

    // store object for files and file profiles used in sending upload
    // successfully mail
    private static Hashtable dataStoreForFilesInSendingEmail = new Hashtable();
    
    // Cached jobIds that jobs creation had been started, this is used to avoid
    // job with same ID to be created repeatedly.
    // For "uploadFile...()" and "CreateJob...()" APIs only.
    private static Set<Long> cachedJobIds = Collections
            .synchronizedSet(new HashSet<Long>());

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
    
    private static final SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    /*
     * The version of desktop icon e.g. VERSION = "(3.1,8.2)" -> 3.1 is the
     * minimal version to allow access webservice, 8.2 is the current version of
     * desktop. Abandoned since 8.2.1.
     */
    private static String VERSION = "(3.1,8.5)";

    // new version used to check, since 8.2.1
    // need to be changed according to the release version each time
    private static String VERSION_NEW = "(3.1,8.5)";

    /**
     * used by checkIfInstalled() to remember whether the web service is
     * installed
     */
    // Whether the web service is installed
    private static boolean isWebServiceInstalled = false;

    private XmlEntities xmlEncoder = new XmlEntities();
    private JobSummaryHelper jobHelper = new JobSummaryHelper();
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
     * @deprecated -- this method is not well designed, not suggest to use it.
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
        String loggedUserName = getUsernameFromSession(p_accessToken);
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
     *            String User ID. This field cannot be null. 
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. This field cannot be null
     * @param p_firstName
     *            String First name. This field cannot be null
     * @param p_lastName
     *            String Last name. This field cannot be null
     * @param p_email
     *            String Email address. This field cannot be null. 
     *            If the email address is not vaild then the user's status will be set up as inactive
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_status
     *            String Status of user. This parameter is not using now, it should be null.
     * @param p_roles
     *            Roles String information of user. It uses a string with XML format to mark all roles information of user.
     *            Example:
     *              <?xml version=\"1.0\"?>
     *                <roles>
     *                  <role>
     *                    <sourceLocale>en_US</sourceLocale>
     *                    <targetLocale>de_DE</targetLocale>
     *                    <activities>
     *                      <activity>
     *                        <name>Dtp1</name>
     *                      </activity>
     *                      <activity>
     *                        <name>Dtp2</name>
     *                      </activity>
     *                    </activities>
     *                  </role>
     *                </roles>
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project.
     * @param p_projectIds
     *            String[] ID of projects which user should be included in. If p_isInAllProject is true, this will not take effect.
     *            Example: [{"1"}, {"3"}]
     * @return int Return code 
     *        0 -- Success 
     *        1 -- Invalid access token 
     *        2 -- Invalid user id 
     *        3 -- Cannot create super user
     *        4 -- User exists
     *        5 -- User does NOT exist
     *        6 -- User is NOT in the same company with logged user
     *        7 -- Invalid user password 
     *        8 -- Invalid first name 
     *        9 -- Invalid last name 
     *       10 -- Invalid email address 
     *       11 -- Invalid permission groups 
     *       12 -- Invalid project information 
     *       13 -- Invalid role information 
     *       14-- Current login user does not have enough permission
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
    public int createUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.createUser(p_accessToken, p_userId, p_password,
                p_firstName, p_lastName, p_email, p_permissionGrps, p_status,
                p_roles, p_isInAllProject, p_projectIds);
    }

    /**
     * Modify user
     * 
     * @param p_accessToken
     *            String Access token. This field cannot be null
     * @param p_userId
     *            String User ID. This field cannot be null. 
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. This field cannot be null
     * @param p_firstName
     *            String First name. This field cannot be null
     * @param p_lastName
     *            String Last name. This field cannot be null
     * @param p_email
     *            String Email address. This field cannot be null. 
     *            If the email address is not vaild then the user's status will be set up as inactive
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_status
     *            String Status of user. This parameter is not using now, it should be null.
     * @param p_roles
     *            Roles String information of user. It uses a string with XML format to mark all roles information of user.
     *            Example:
     *              <?xml version=\"1.0\"?>
     *                <roles>
     *                  <role>
     *                    <sourceLocale>en_US</sourceLocale>
     *                    <targetLocale>de_DE</targetLocale>
     *                    <activities>
     *                      <activity>
     *                        <name>Dtp1</name>
     *                      </activity>
     *                      <activity>
     *                        <name>Dtp2</name>
     *                      </activity>
     *                    </activities>
     *                  </role>
     *                </roles>
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project.
     * @param p_projectIds
     *            String[] ID of projects which user should be included in. If p_isInAllProject is true, this will not take effect.
     *            Example: [{"1"}, {"3"}]
     * @return int Return code 
     *        0 -- Success 
     *        1 -- Invalid access token 
     *        2 -- Invalid user id 
     *        3 -- Cannot create super user
     *        4 -- User exists
     *        5 -- User does NOT exist
     *        6 -- User is NOT in the same company with logged user
     *        7 -- Invalid user password 
     *        8 -- Invalid first name 
     *        9 -- Invalid last name 
     *       10 -- Invalid email address 
     *       11 -- Invalid permission groups 
     *       12 -- Invalid project information 
     *       13 -- Invalid role information 
     *       14-- Current login user does not have enough permission
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
    public int modifyUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.modifyUser(p_accessToken, p_userId, p_password,
                p_firstName, p_lastName, p_email, p_permissionGrps, p_status,
                p_roles, p_isInAllProject, p_projectIds);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void createJob(String accessToken, String jobName, String comment,
            String filePaths, String fileProfileIds, String targetLocales,
            String attributeXml) throws WebServiceException
    {
		HashMap args = new HashMap();
		args.put("accessToken", accessToken);
		args.put("jobName", jobName);
		args.put("comment", comment);
		args.put("filePaths", filePaths);
		args.put("fileProfileIds", fileProfileIds);
		args.put("targetLocales", targetLocales);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void createJob(HashMap args) throws WebServiceException
    {
        // Checks authority.
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, CREATE_JOB);
        checkPermission(accessToken, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

        // Read parameters.
        String jobName = (String) args.get("jobName");
        jobName = EditUtil.removeCRLF(jobName);
        String jobNameValidation = validateJobName(jobName);
        if (jobNameValidation != null)
        {
            throw new WebServiceException(makeErrorXml("createJob",
                    jobNameValidation));
        }
        ActivityLog.Start activityStart = null;
        Job job = null;
        try
        {
            String userName = getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", jobName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "createJob(args)", activityArgs);
            job = ServerProxy.getJobHandler().getJobByJobName(jobName);
            if (job == null)
            {
                String userId = UserUtil.getUserIdByName(userName);
                String priority = (String) args.get("priority");
    			Vector<String> fileProfileIds = new Vector<String>();
    			Object fpIdsObj = args.get("fileProfileIds");
    			if (fpIdsObj instanceof String)
    			{ 
    		        for (String fId : ((String) fpIdsObj).split("\\|"))
    		        {
    		        	fileProfileIds.add(fId);
    		        }
    			}
    			else
    			{
    				fileProfileIds = (Vector<String>) fpIdsObj;
    			}

                if (fileProfileIds != null && fileProfileIds.size() > 0)
                {
                    String fpId = (String) fileProfileIds.get(0);
                    long iFpId = Long.parseLong(fpId);
                    FileProfile fp = ServerProxy
                            .getFileProfilePersistenceManager()
                            .readFileProfile(iFpId);

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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
     * @throws NamingException 
     * @throws GeneralException 
     * @throws RemoteException 
     * @throws NumberFormatException 
     */
    @SuppressWarnings("unchecked")
	public void createJobOnInitial(HashMap args) throws WebServiceException,
    	NumberFormatException, RemoteException, GeneralException, NamingException
	{
		Job job = null;

		// Checks authority.
		String accessToken = (String) args.get("accessToken");
		checkAccess(accessToken, CREATE_JOB);
		checkPermission(accessToken, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);
		ActivityLog.Start activityStart = null;
		try
		{
			// Read parameters.
			String jobId = (String) args.get("jobId");
			job = JobCreationMonitor.loadJobFromDB(Long.parseLong(jobId));
			String jobName = job.getJobName();
			String recreateFlag = (String) args.get("recreate");
			if (!"true".equalsIgnoreCase(recreateFlag))
			{
				String msg = checkIfCreateJobCalled("createJobOnInitial",
						job.getId(), jobName);
				if (msg != null)
				{
					throw new WebServiceException(msg);
				}
			}
			cachedJobIds.add(job.getId());

			String uuId = ((JobImpl) job).getUuid();
			String comment = (String) args.get("comment");
			// filePaths
			Vector<String> filePaths = new Vector<String>();
			Object filePathsObj = args.get("filePaths");
			if (filePathsObj instanceof String)
			{
				for (String path : ((String) filePathsObj).split("\\|"))
				{
					filePaths.add(path);
				}
			}
			else
			{
				filePaths = (Vector<String>) filePathsObj;
			}
			// fileProfileIds
			Vector<String> fileProfileIds = new Vector<String>();
			Object fpIdsObj = args.get("fileProfileIds");
			if (fpIdsObj instanceof String)
			{
		        for (String fId : ((String) fpIdsObj).split("\\|"))
		        {
		        	fileProfileIds.add(fId);
		        }
			}
			else
			{
				fileProfileIds = (Vector<String>) fpIdsObj;
			}
			// targetLocales
			Vector<String> targetLocales = new Vector<String>();
			Object trgLocalesObj = args.get("targetLocales");
			if (trgLocalesObj instanceof String)
			{
				targetLocales = handleTargetLocales((String) trgLocalesObj,
						fileProfileIds.size());
			}
			else if (trgLocalesObj == null)
			{
				targetLocales = handleTargetLocales("", fileProfileIds.size());
			}
			else
			{
				targetLocales = (Vector<String>) trgLocalesObj;
			}

			String priority = (String) args.get("priority");
			String attributesXml = (String) args.get("attributes");

			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("jobId", jobId);
			activityArgs.put("jobName", jobName);
			activityArgs.put("recreate", recreateFlag);
			activityArgs.put("comment", comment);
			activityArgs.put("filePaths", filePaths);
			activityArgs.put("fileProfileIds", fileProfileIds);
			activityArgs.put("targetLocales", targetLocales);
			activityArgs.put("attributes", attributesXml);
			activityArgs.put("isJobCreatedOriginallyViaWS",
					(String) args.get("isJobCreatedOriginallyViaWS"));
			activityStart = ActivityLog.start(Ambassador.class,
					"createJobOnInitial(args)", activityArgs);
			if (filePaths != null && filePaths.size() > 0)
			{
				for (int i = 0; i < filePaths.size(); i++)
				{
					String filePath = (String) filePaths.get(i);
					String extensionMsg = checkExtensionExisted(filePath);
					if (extensionMsg != null)
					{
						throw new WebServiceException(makeErrorXml(
								"createJobOnInitial", extensionMsg));
					}
				}
			}

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

			boolean isWSFlag = true;
			if ("false"
					.equals((String) args.get("isJobCreatedOriginallyViaWS")))
			{
				isWSFlag = false;
			}

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
					filename = getRealPath(jobId, filename, srcLocale, isWSFlag);
					realFilename = AmbFileStoragePathUtils.getCxeDocDir(job
							.getCompanyId()) + File.separator + filename;
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
						 * below steps, 1. Unpack the file to folder named with
						 * xlz file name For Example, ..\testXLZFile.xlz will be
						 * unpacked to ..\testXLZFile\xlzFile01.xlf
						 * ..\testXLZFile\xlzFile02.txb
						 * 
						 * 2. add new file profiles according with reference
						 * file profile with current xlz file to all xliff files
						 * unpacked from xlz
						 * 
						 * 3. add target locales according with reference target
						 * locale with current xlz file to all xliff files
						 * unpacked from xlz
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
				logger.error(e);
				throw new WebServiceException(e.getMessage());
			}

			// Calls script if has.
			// Vector result = FileSystemUtil.execScript(files, fileProfiles,
			// targetLocales);
			Vector result = FileSystemUtil.execScript(files, fileProfiles,
					afterTargetLocales, Long.parseLong(jobId), jobName);
			Vector sFiles = (Vector) result.get(0);
			Vector sProFiles = (Vector) result.get(1);
			Vector stargetLocales = (Vector) result.get(2);
			Vector exitValues = (Vector) result.get(3);

			// cache job attributes
			List<JobAttributeVo> atts = null;
			 String companyId = CompanyThreadLocal.getInstance().getValue();

			try
			{
				if (attributesXml != null && attributesXml.length() > 0)
				{
					Attributes attributes = com.globalsight.cxe.util.XmlUtil
							.string2Object(Attributes.class, attributesXml);
					atts = (List<JobAttributeVo>) attributes.getAttributes();

					List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
					for (JobAttributeVo jobAttributeVo : atts)
					{
						jobatts.add(AttributeUtil
								.createJobAttribute(jobAttributeVo));
					}

					RuntimeCache.addJobAtttibutesCache(uuId, jobatts);
				}
				else
                {
                    AttributeSet as = ((JobImpl)job).getAttributeSet();
                    List<Attribute> jas = as.getAttributeAsList();
                    List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
                    atts = new ArrayList<JobAttributeVo>();
                  
                    for (Attribute ja : jas)
                    {
                        JobAttributeVo vo = AttributeUtil.getAttributeVo(ja
                                .getCloneAttribute());
                        atts.add(vo);
                        jobatts.add(AttributeUtil
                                .createJobAttribute(vo));
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
		catch (Exception e)
		{
			throw new WebServiceException(e.getMessage());
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}
	}

	private Vector<String> handleTargetLocales(String p_targetLocales,
			int fileSize)
    {
        Vector<String> tLocales = new Vector<String>();

        if (StringUtil.isEmpty(p_targetLocales)
				|| StringUtil.isEmpty(p_targetLocales.replace("|", "")))
		{
			for (int i = 0; i < fileSize; i++)
			{
				tLocales.add(" ");
			}
		}
		else
		{
			for (String tLocale : p_targetLocales.split("\\|"))
			{
				if (tLocale.contains(","))
				{
					String locales = "";
					for (String locale : tLocale.split(","))
					{
						locales += locale.trim() + ",";
					}
					if (locales != "" && locales.endsWith(","))
					{
						tLocales.add(locales.substring(0,
								locales.lastIndexOf(",")));
					}
				}
				else
				{
					tLocales.add(tLocale.trim());
				}
			}
		}

        return tLocales;
    }

    private String checkIfCreateJobCalled(String methodName, long jobId,
            String jobName) throws WebServiceException
    {
        // Job with "jobId" is being created or has been created, can't start
        // job creation with same job ID.
        if (cachedJobIds.contains(jobId))
        {
            String message = "Current job (jobId:" + jobId + ";jobName:"
                    + jobName
                    + ") is being created or has been created already.";
            return makeErrorXml(methodName, message);
        }
        return null;
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
     * @throws NamingException 
     * @throws GeneralException 
     * @throws RemoteException 
     * @throws JobException 
     */
    public void createEditionJob(HashMap args) throws WebServiceException, 
    	JobException, RemoteException, GeneralException, NamingException
    {
        // Checks authority.
        String accessToken = (String) args.get("accessToken");
        checkAccess(accessToken, CREATE_JOB);
        // Read parameters.
        String jobName = (String) args.get("jobName");
        String jobNameValidation = validateJobName(jobName);
        if (jobNameValidation != null)
        {
            throw new WebServiceException(makeErrorXml("createEditionJob",
                    jobNameValidation));
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
            filePath = getRealPath(jobName, filePath, srcLocale, true);
            File file = new File(AmbFileStoragePathUtils.getCxeDocDir(),
                    filePath);
            if (file.getAbsolutePath().endsWith(".xml"))
            {
                saveFileAsUTF8(file);
            }

            files.add(file);
        }

        // Calls script if has.
        Job job = ServerProxy.getJobHandler().getJobByJobName(jobName);
        Vector result = FileSystemUtil.execScript(files, fileProfiles,
                targetLocales, job.getId(), jobName);
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
            originalEncode = ImportUtil.guessEncodingByBom(file);
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
    private String validateJobName(String p_jobname)
    {
        if (StringUtil.isEmpty(p_jobname))
        {
            return "Empty job name.";
        }
        String name = p_jobname.trim();
        int index = name.lastIndexOf("_");
        if (index > -1)
        {
            name = name.substring(0, index);
        }
        if (name.length() > 100)
        {
            return "The length of job name exceeds 100 characters.";
        }

        if (!p_jobname.matches("[^\\\\/:;*?|\"<>&%]*"))
        {
            return ERROR_JOB_NAME;
        }

        return null;
    }

    private String checkExtensionExisted(String p_filePath)
    {
        if (p_filePath == null || p_filePath.trim().length() == 0)
            return "filePath is null or empty";

        try
        {
            String fileName = null;
            String path = p_filePath.replace("\\", "/");
            int index = path.lastIndexOf("/");
            if (index > -1)
            {
                fileName = path.substring(index + 1);
            }
            else
            {
                fileName = path;
            }

            index = fileName.lastIndexOf(".");
            if (index > -1)
            {
                String extension = fileName.substring(index + 1);
                if (extension != null && extension.trim().length() > 0)
                    return null;
            }
        }
        catch (Exception ignore)
        {
            return null;
        }

        return "The file " + p_filePath + " has no extension.";
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
        String jobNameValidation = validateJobName(jobName);
        if (jobNameValidation != null)
        {
            throw new WebServiceException(makeErrorXml("getUniqueJobName",
                    jobNameValidation));
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
        ActivityLog.Start activityStart = null;

        try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName",
					getUsernameFromSession(accessToken));
			activityArgs.put("jobName", jobName);
			activityStart = ActivityLog.start(Ambassador.class,
					"getUniqueJobName(args)", activityArgs);
			String sql = "SELECT ID FROM JOB WHERE NAME=?";
			connection = ConnectionPool.getConnection();
			query = connection.prepareStatement(sql);
			query.setString(1, jobName);
			results = query.executeQuery();
			if (results.next())
			{
				return getUniqueJobName(args);
			}
			else return jobName;
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
			if (activityStart != null)
			{
				activityStart.end();
			}

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
        ActivityLog.Start activityStart = null;
        boolean updateJobStateIfException = true;
        try
        {
            String jobName = (String) args.get("jobName");
            jobName = EditUtil.removeCRLF(jobName);
            String filePath = (String) args.get("filePath");
            String fileProfileId = (String) args.get("fileProfileId");
            String priority = (String) args.get("priority");
            String jobNameValidation = validateJobName(jobName);
            if (jobNameValidation != null)
            {
                throw new WebServiceException(makeErrorXml("uploadFileForInitial",
                        jobNameValidation));
            }
            String extensionMsg = checkExtensionExisted(filePath);
            if (extensionMsg != null)
            {
                throw new WebServiceException(makeErrorXml("uploadFileForInitial",
                        extensionMsg));
            }
            String userName = this.getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", jobName);
            activityArgs.put("filePath", filePath);
            activityArgs.put("fileProfileId", fileProfileId);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "uploadFile(accessToken, jobName,filePath,fileProfileId,content)",
                            activityArgs);
            activityStart = ActivityLog.start(Ambassador.class,
                    "uploadFileForInitial(args)", activityArgs);
            String userId = UserUtil.getUserIdByName(userName);
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(Long.parseLong(fileProfileId));
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

            // GBS-3367 (special case checking)
            String msg = checkIfCreateJobCalled("uploadFileForInitial",
                    Long.parseLong(jobId), jobName);
            if (msg != null)
            {
                updateJobStateIfException = false;
                throw new WebServiceException(msg);
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
            String path = getRealPath(jobId, filePath, srcLocale, true);
            writeFile(path, bytes, fp.getCompanyId());
        }
        catch (Exception e)
        {
            if (jobId != null && updateJobStateIfException)
            {
                JobCreationMonitor.updateJobState(Long.parseLong(jobId),
                        Job.IMPORTFAILED);
            }
            logger.error(e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                    "getStatus(p_accessToken, p_jobName)",
                            activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobId", p_jobId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getJobAndWorkflowInfo(p_accessToken, p_jobId)",
                    activityArgs);
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
			xml.append("\t<creator>").append(job.getCreateUser().getUserName())
					.append("</creator>\r\n");
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        String id = "";
        String status;
        try
        {
            User user = getUser(getUsernameFromSession(p_accessToken));
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", user.getUserName());
            activityArgs.put("jobName", p_jobName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getJobStatus(p_accessToken, p_jobName)", activityArgs);
            String condition = appendJobCondition(p_jobName);
            String sql = "SELECT ID, STATE FROM JOB WHERE COMPANY_ID=? AND "
                    + condition;
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setLong(1, CompanyWrapper.getCompanyByName(user.getCompanyName()).getId());
            query.setString(2, p_jobName);
            
            results = query.executeQuery();
            if (results.next())
            {
                id = results.getString(1);
                status = results.getString(2);
            }
            else
            {
                // This job is not in the table of DB, it was not created
                status = "UNKNOWN";
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
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        checkAccess(p_accessToken, GET_JOB_EXPORT_FILES);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);
        checkPermission(p_accessToken, Permission.JOBS_EXPORT);

        String jobName = p_jobName;
        ActivityLog.Start activityStart = null;
        Job job = queryJob(jobName, p_accessToken);
        long jobId = job.getId();
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
//        jobFiles.setJobId(jobId);
//        jobFiles.setJobName(jobName);

        StringBuilder prefix = new StringBuilder();
        prefix.append(getUrl()).append("/cxedocs/");
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
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "getJobExportFiles(p_accessToken, p_jobName)",
                            activityArgs);
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
                    if (StringUtil.isNotEmpty(fp.getScriptOnExport()))
                    {
                        path = handlePathForScripts(path, job);
                    }
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }

    private String getUrl()
    {
        return AmbassadorUtil.getCapLoginOrPublicUrl();
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

    private String handlePathForScripts(String path, Job job)
    {
        path = path.replace("\\", "/");
        String finalPath = path;
        // for new scripts on import/export
        if (path.contains("/PreProcessed_" + job.getId() + "_"))
        {
            finalPath = path.replace(path.substring(
                    path.lastIndexOf("/PreProcessed_" + job.getId() + "_"),
                    path.lastIndexOf("/")), "");
        }
        // compatible codes for old import/export
        else
        {
            int index = path.lastIndexOf("/");
            if (index > -1)
            {
                String fileName = path.substring(index + 1);
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                String rest = path.substring(0, index);
                if (rest.endsWith("/" + fileName))
                {
                    finalPath = rest + "." + extension;
                }
            }
        }

        return finalPath;
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
        checkAccess(p_accessToken, GET_JOB_EXPORT_WORKFLOW_FILES);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);
        checkPermission(p_accessToken, Permission.JOBS_EXPORT);
        ActivityLog.Start activityStart = null;
        String jobName = p_jobName;
        Job job = queryJob(jobName, p_accessToken);
        long jobId = job.getId();
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
//        jobFiles.setJobId(jobId);
//        jobFiles.setJobName(jobName);
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
        prefix.append(getUrl()).append("/cxedocs/");
        String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
        prefix.append(URLEncoder.encode(company, "utf-8"));
        jobFiles.setRoot(prefix.toString());

        Set<String> passoloFiles = new HashSet<String>();

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityArgs.put("workflowLocale", workflowLocale);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "getJobExportWorkflowFiles(p_accessToken, p_jobName,workflowLocale)",
                            activityArgs);
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
                    if (StringUtil.isNotEmpty(fp.getScriptOnExport()))
                    {
                        path = handlePathForScripts(path, job);
                    }
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        long jobId = job.getId();
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
            
            xml.append("<jobId>")
            		.append(jobId)
            		.append("</jobId>\r\n");
            
            xml.append("<jobName>")
            		.append(p_jobName)
            		.append("</jobName>\r\n");

            StringBuilder urlPrefix = new StringBuilder();
            urlPrefix.append(getUrl()).append("/cxedocs/");
            String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
            urlPrefix.append(URLEncoder.encode(company, "utf-8"));
            xml.append("<urlPrefix>")
                    .append(urlPrefix)
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
        long jobId = job.getId();
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
        
        xml.append("<jobId>")
				.append(jobId)
				.append("</jobId>\r\n");

        xml.append("<jobName>")
				.append(p_jobName)
				.append("</jobName>\r\n");
        
        StringBuilder urlPrefix = new StringBuilder();
        urlPrefix.append(getUrl()).append("/cxedocs/");
        String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
        urlPrefix.append(URLEncoder.encode(company, "utf-8"));
        xml.append("<urlPrefix>").append(urlPrefix).append("</urlPrefix>\r\n");

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
        long jobId = job.getId();
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
        xml.append("<jobId>").append(jobId)
        		.append("</jobId>\r\n");
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
            StringBuilder urlPrefix = new StringBuilder();
            urlPrefix.append(getUrl()).append("/cxedocs/");
            String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
            urlPrefix.append(URLEncoder.encode(company, "utf-8"));

            while (results.next())
            {
                String fileName = results.getString(1);
                String langCode = results.getString(2);
                String countryCode = results.getString(3);
                String exportSubDir = results.getString(4);
                StringBuffer locale = new StringBuffer(langCode);
                locale.append("_").append(countryCode);

                String targetFileName = File.separator + replaceLocaleInFileName(fileName,
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
        ActivityLog.Start activityStart = null;
        String jobName = p_jobName;
        String workflowLocale = p_workflowLocale;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityArgs.put("workflowLocale", p_workflowLocale);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "cancelWorkflow(p_accessToken, p_jobName,p_workflowLocale)",
                            activityArgs);
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            Job job = queryJob(jobName, p_accessToken);
            String status = job.getState();
            boolean didCancel = false;
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "cancelJob(p_accessToken, p_jobName)", activityArgs);
            String userId = UserUtil.getUserIdByName(userName);

            Job job = queryJob(p_jobName, p_accessToken);
            if (!UserUtil.isInProject(userId,
                    String.valueOf(job.getProjectId())))
                throw new WebServiceException(
                        "Current user cannot cancel the job.");

            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            logger.info("Cancelling all workflows for job " + p_jobName);
            ServerProxy.getJobHandler().cancelJob(userId, job, null);
            xml.append("<cancelStatus>\r\n");
            xml.append("\t<jobName>")
                    .append(EditUtil.encodeXmlEntities(p_jobName))
                    .append("</jobName>\r\n");
            xml.append("\t<workflowLocale>All Locales</workflowLocale>\r\n");
            xml.append("\t<status>canceled</status>\r\n");
            xml.append("</cancelStatus>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error("cancelJob()", e);
            String message = "Could not cancel job " + p_jobName;
            message = makeErrorXml("cancelJob", message);
            throw new WebServiceException(message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobId", p_jobId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "cancelJobById(p_accessToken, p_jobId)", activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobIds", p_jobIds);
            activityStart = ActivityLog.start(Ambassador.class,
                    "cancelJobs(p_accessToken, p_jobIds)", activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityArgs.put("workflowLocale", p_workflowLocale);
            activityStart = ActivityLog.start(Ambassador.class,
                    "exportWorkflow(p_accessToken, p_jobName,p_workflowLocale)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "exportJob(p_accessToken, p_jobName)", activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }
    
    /**
     * Archive the jobs specified by job IDs.
     * 
     * @param p_accessToken
     * @param p_jobIds
     *            Job IDs comma separated, like "100,101,102".
     * @return If archive success, return null. If failed, return the error jobs
     *         message.
     * @throws WebServiceException
     */
    public String archiveJob(String p_accessToken, String p_jobIds)
            throws WebServiceException
    {
        checkAccess(p_accessToken, ARCHIVE_JOB);
        checkPermission(p_accessToken, Permission.JOBS_ARCHIVE);

        User curUser = getUser(getUsernameFromSession(p_accessToken));
        Company company = getCompanyByName(curUser.getCompanyName());

        String[] jobIds = p_jobIds.split(",");
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        WorkflowManagerLocal workflowManagerLocal  = new WorkflowManagerLocal();
        HashMap<String, String> errorJobs = new HashMap<String, String>();
        boolean isArchived;
        for(String jobId: jobIds)
        {
        	try
        	{
        		isArchived = false;
                Job job = ServerProxy.getJobHandler().getJobById(
                        Long.parseLong(jobId));
        		if(job == null)
        		{
        			errorJobs.put(jobId, "the job may not exist.");
        			continue;
        		}

        		// If job is not from current user's company, ignore.
                if (company.getId() != 1
                        && company.getId() != job.getCompanyId())
                {
                    errorJobs.put(jobId, "this job belongs to another company, can not archive.");
                    continue;
                }

                isArchived = workflowManagerLocal.archive(job);
        		if(!isArchived)
        		{
        			errorJobs.put(jobId, "the job is not in \"Exported\" state and can't be archived.");
        		}
			} 
        	catch (Exception e) 
        	{
				errorJobs.put(jobId, e.getMessage());
			}
        }

        if(errorJobs.size() > 0)
        {
        	xml.append("<errorJobs>\r\n");
        	for(String jobId: errorJobs.keySet())
        	{
        		xml.append("\t<errorJob>\r\n");
        		xml.append("\t\t<jobId>").append(jobId).append("</jobId>\r\n");
        		xml.append("\t\t<errorMessage>").append(errorJobs.get(jobId)).append("</errorMessage>\r\n");
        		xml.append("\t</errorJob>\r\n");
        	}
        	xml.append("</errorJobs>\r\n");
        	return xml.toString();
        }
        else
        {
        	return null;
		}
    }
    
    /**
     * Returns number of jobs importing and number of workflows exporting.
     * 
     * @param p_accessToken
     * @return String An XML description which contains number of jobs importing 
     * 			and number of workflows exporting
     * @throws WebServiceException
     */
    public String getImportExportStatus(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_IMPORT_EXPORT_STATUS);

        User curUser = getUser(getUsernameFromSession(p_accessToken));
        Company company = getCompanyByName(curUser.getCompanyName());
        String jobsCreatingNumSql = "select count(ID) from JobImpl "
        	+ " where STATE in ('" + Job.UPLOADING + "', '" + Job.IN_QUEUE
        	+ "', '" + Job.EXTRACTING + "', '" + Job.LEVERAGING + "', '"
        	+ Job.CALCULATING_WORD_COUNTS + "', '" + Job.PROCESSING + "')"
        	+ " and COMPANY_ID = " + company.getId();
        int jobsCreatingNum = HibernateUtil.count(jobsCreatingNumSql);       
        int localesExportingNum = WorkflowExportingHelper.getExportingWorkflowNumber(
        		false, company.getId());
        
        StringBuffer xml = new StringBuffer(
        		"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");       	
        xml.append("<ImportExportStatus>\r\n");
		xml.append("\t<jobsCreating>").append(jobsCreatingNum).append("</jobsCreating>\r\n");
		xml.append("\t<localesExporting>").append(localesExportingNum).append("</localesExporting>\r\n");
    	xml.append("</ImportExportStatus>\r\n");
    	
    	return xml.toString();
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("workflowId", p_workflowId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getAcceptedTasksInWorkflow(p_accessToken, p_workflowId)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        checkAccess(p_accessToken, GET_CURRENT_TASKS);
        checkPermission(p_accessToken, Permission.ACTIVITIES_VIEW);

        Collection tasks = null;
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("workflowId", p_workflowId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getCurrentTasksInWorkflow(p_accessToken, p_workflowId)",
                    activityArgs);
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
            logger.error(GET_CURRENT_TASKS, e);
            String message = "Could not get the tasks for workflow with id "
                    + p_workflowId;
            message = makeErrorXml(GET_CURRENT_TASKS, message);
            throw new WebServiceException(message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobId", p_jobId);
            activityArgs.put("taskName", p_taskName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getTasksInJob(p_accessToken, p_jobId, p_taskName)",
                    activityArgs);
            
            if (!UserUtil.isInProject(userId,
                    String.valueOf(job.getProjectId())))
                throw new WebServiceException(
                        "Current user does not have permission to get task information");

            StringBuilder xml = new StringBuilder(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
            xml.append("<tasksInJob>\r\n");
            xml.append("\t<jobId>").append(p_jobId).append("</jobId>\r\n");
            taskInfos = ServerProxy.getTaskManager().getTasks(p_taskName,
                    p_jobId);
            Map<Long, String> taskAssignees = AmbassadorHelper
                    .getTaskAssigneesByJob(p_jobId);
            List<Long> processdefintionList = AmbassadorHelper
					.getProcessdefintion(p_jobId);
            Map<Long,TaskJbpmNode>  jbpmNodeMap = AmbassadorHelper
					.getTaskJbpmNode(processdefintionList);
			Map<Long,List<TaskJbpmTransition>> jbpmTranMap = AmbassadorHelper
					.getTaskJbpmTransition(processdefintionList);
            Object[] tasks = taskInfos == null ? null : taskInfos.toArray();
            int size = tasks == null ? -1 : tasks.length;

            connection = ConnectionPool.getConnection();

            for (int i = 0; i < size; i++)
            {
            	 List<ConditionNodeTargetInfo> conList = new ArrayList<ConditionNodeTargetInfo>();
                Task ti = (Task) tasks[i];
                String assignees = taskAssignees.get(ti.getId());

                conList = AmbassadorHelper
						.getConditionNodeTargetInfo(ti.getId(),
								jbpmNodeMap, jbpmTranMap,conList);
                buildXmlForTask(xml, ti, "\t", connection, assignees,conList);
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
                if (activityStart != null)
                {
                    activityStart.end();
                }
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
     *            -- Access token
     * @param jobIds
     *            -- job ids comma separated
     * @param p_taskName
     *            -- Task name with company id such as 'Translation1_1', if task
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

        ActivityLog.Start activityStart = null;
        Collection taskInfos = null;
        Connection connection = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            User user = getUser(userName);
            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
            String[] jobIdArray = jobIds.split(",");

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobNum", jobIdArray == null ? 0
                    : jobIdArray.length);
            activityArgs.put("jobIds", jobIds);
            activityArgs.put("taskName", p_taskName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getTasksInJobs(p_accessToken, jobIds, p_taskName)",
                    activityArgs);
            JobHandlerWLRemote jobHandlerLocal = ServerProxy.getJobHandler();
            Job job = null;

            StringBuilder xml = new StringBuilder(XML_HEAD);
            xml.append("<jobs>\r\n");
            long jobId = -1;
            connection = ConnectionPool.getConnection();

            for (String jobIdString : jobIdArray)
            {
                StringBuilder subXml = new StringBuilder();
                try
                {
                    if (StringUtil.isEmpty(jobIdString))
                        continue;

                    jobId = Long.parseLong(jobIdString.trim());
                    job = jobHandlerLocal.getJobById(jobId);
                    if (job == null)
                        continue;

                    if (!UserUtil.isInProject(userId,
                            String.valueOf(job.getProjectId())))
                    {
                        continue;
                    }

                    if (job != null)
                    {
                        subXml.append("\t<job>\r\n");

                        subXml.append("\t\t<job_id>").append(jobId)
                                .append("</job_id>\r\n");
                        subXml.append("\t\t<job_name>")
                                .append(EditUtil.encodeXmlEntities(job
                                        .getJobName()))
                                .append("</job_name>\r\n");

                        boolean isReturnAssignees = false;
                        taskInfos = ServerProxy.getTaskManager().getTasks(
                                p_taskName, jobId, isReturnAssignees);
                        Map<Long, String> taskAssignees = AmbassadorHelper
                                .getTaskAssigneesByJob(jobId);
						List<Long> processdefintionList = AmbassadorHelper
								.getProcessdefintion(jobId);
						Map<Long,TaskJbpmNode>  jbpmNodeMap = AmbassadorHelper
								.getTaskJbpmNode(processdefintionList);
						Map<Long,List<TaskJbpmTransition>> jbpmTranMap = AmbassadorHelper
								.getTaskJbpmTransition(processdefintionList);
                        Object[] tasks = taskInfos == null ? null : taskInfos
                                .toArray();
                        int size = tasks == null ? -1 : tasks.length;

                        for (int i = 0; i < size; i++)
						{
							Task ti = (Task) tasks[i];
							List<ConditionNodeTargetInfo> conList = new ArrayList<ConditionNodeTargetInfo>();
							String wfState = ti.getWorkflow().getState();
							if (Workflow.CANCELLED.equals(wfState))
								continue;
							conList = AmbassadorHelper
									.getConditionNodeTargetInfo(ti.getId(),
											jbpmNodeMap, jbpmTranMap, conList);
							String assignees = taskAssignees.get(ti.getId());
							buildXmlForTask(subXml, ti, "\t\t", connection,
									assignees, conList);
						}

                        subXml.append("\t</job>\r\n");
                    }
                    // append at last
                    xml.append(subXml);
                }
                catch (Exception e)
                {
                    continue;
                }
            }
            xml.append("</jobs>");

            return xml.toString();
        }
        catch (Exception e)
        {
            logger.error("getTasksInJobs", e);
            String message = "Could not get the tasks with the name "
                    + p_taskName + " for job with ids (" + jobIds + ")";
            return makeErrorXml("getTasksInJobs", message);
        }
        finally
        {
            DbUtil.silentReturnConnection(connection);
            if (activityStart != null)
            {
                activityStart.end();
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
            return makeErrorXml(ACCEPT_TASK, e.getMessage());
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
            return makeErrorXml(ACCEPT_TASK, message);
        }
        ActivityLog.Start activityStart = null;
        try
		{
			if (task != null)
			{
				Map<Object, Object> activityArgs = new HashMap<Object, Object>();
				activityArgs.put("loggedUserName", acceptorName);
				activityArgs.put("taskId", p_taskId);
				activityStart = ActivityLog.start(Ambassador.class,
						"acceptTask(p_accessToken,p_taskId)", activityArgs);
				if (task.getState() == Task.STATE_ACCEPTED
						|| task.getState() == Task.STATE_COMPLETED)
				{
					return makeErrorXml(ACCEPT_TASK,
							"The current task has been accepted or completed state.");
				}
				
				WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
						.getWorkflowTaskInstance(acceptor, task.getId(),
								WorkflowConstants.TASK_ALL_STATES);
				task.setWorkflowTask(wfTask);
				List allAssignees = task.getAllAssignees();
				if (allAssignees != null && allAssignees.size() > 0)
				{
					if (!allAssignees.contains(acceptor))
					{
						String message = "'"
								+ acceptor
								+ "' is not an available assignee for current task "
								+ p_taskId;
						logger.warn(message);
						return makeErrorXml(ACCEPT_TASK, message);
					}
				}
				// GS will check if the acceptor is PM or available users
				TaskHelper.acceptTask(acceptor, task);
			}
			else
			{
				return makeErrorXml(ACCEPT_TASK, "Invaild task id.");
			}
		}
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to accept task for taskId : " + p_taskId
                    + ",maybe '" + acceptor
                    + "' do not have the authority to operate the task";
            return makeErrorXml(ACCEPT_TASK, message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
			return makeErrorXml(COMPLETE_TASK, e.getMessage());
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
			return makeErrorXml(COMPLETE_TASK, msg);
		}
        catch (Exception ex)
        {
            logger.error(ex.getMessage(), ex);
        }

        // Compelte task
        String completeUserId = null;
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("taskId", p_taskId);
            activityArgs.put("destinationArrow", p_destinationArrow);
            activityStart = ActivityLog.start(Ambassador.class,
                    "completeTask(p_accessToken,p_taskId,p_destinationArrow)",
                    activityArgs);
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
                    logger.warn(message);
                    message = makeErrorXml("completeTask", message);
                    throw new WebServiceException(message);
                }
            }

            Vector conditionNodes = wfTask.getConditionNodeTargetInfos();
            if (conditionNodes != null && conditionNodes.size() > 0)
            {
                HashSet<String> arrowNames = new HashSet<String>();
                for (int i = 0; i < conditionNodes.size(); i++)
                {
                    ConditionNodeTargetInfo info = (ConditionNodeTargetInfo) conditionNodes.get(i);
                    arrowNames.add(info.getArrowName());
                }

                if (!arrowNames.contains(p_destinationArrow))
                {
                    String message = "\"" + p_destinationArrow + "\" is not a valid outgoing arrow name.";
                    logger.warn(message);
                    message = makeErrorXml("completeTask", message);
                    throw new WebServiceException(message);
                }
            }

			TaskImpl dbTask = HibernateUtil.get(TaskImpl.class, task.getId());
            ProjectImpl project = (ProjectImpl) dbTask.getWorkflow()
                    .getJob().getProject();
            WorkflowImpl workflowImpl = (WorkflowImpl) dbTask.getWorkflow();
            boolean isCheckUnTranslatedSegments = project
                    .isCheckUnTranslatedSegments();
            boolean isRequriedScore = workflowImpl.getScorecardShowType() == 1 ?
            		true : false;
            boolean isReviewOnly = dbTask.isReviewOnly();
            if (isCheckUnTranslatedSegments && !isReviewOnly)
            {
            	int percentage = SegmentTuvUtil
                        .getTranslatedPercentageForTask(task);
                if (100 != percentage)
                {
                	 rtnStr = "The task is not 100% translated and can not be completed.";
                	 return rtnStr;
                }
            }
            if(isRequriedScore && isReviewOnly)
            {
            	if(StringUtil.isEmpty(workflowImpl.getScorecardComment()))
            	{
            		rtnStr = "The task is not scored and can not be completed.";
               	 	return rtnStr;
            	}
            }

            if (task.getState() == Task.STATE_ACCEPTED)
            {
                ServerProxy.getTaskManager().completeTask(userId, task,
                        p_destinationArrow, null);
            }
            else
            {
                rtnStr = "Cannot complete this task as it is not in 'ACCEPTED' state";
            }
        }
        catch (Exception ex)
		{
			String msg = "Fail to complete task : " + p_taskId + " ; "
					+ ex.getMessage();
			logger.error(msg, ex);
			return makeErrorXml(COMPLETE_TASK, msg);
		}
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
			return makeErrorXml(REJECT_TASK, e.getMessage());
		}
        // rejector
        String rejectUserName = getUsernameFromSession(p_accessToken);
        String rejectUserId = UserUtil.getUserIdByName(rejectUserName);
        Task task = null;
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", rejectUserName);
            activityArgs.put("p_taskId", p_taskId);
            activityArgs.put("p_rejectComment", p_rejectComment);
            activityStart = ActivityLog.start(Ambassador.class,
                    "rejectTask(p_accessToken,p_taskId,p_rejectComment)",
                    activityArgs);
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
			return makeErrorXml(REJECT_TASK, message);
		}
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("objectId", p_objectId);
            activityArgs.put("objectType", p_objectType);
            activityArgs.put("userId", p_userId);
            activityArgs.put("comment", p_comment);
            activityArgs.put("fileName", p_fileName);
            activityArgs.put("access", p_access);

            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "addComment(p_accessToken,p_objectId,p_objectType,p_userId,p_comment,p_file,p_fileName,p_access)",
                            activityArgs);
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
        finally
        {

            if (activityStart != null)
            {
                activityStart.end();
            }
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
        String jobNameValidation = validateJobName(p_jobName);
        if (jobNameValidation != null)
        {
            throw new WebServiceException(makeErrorXml("addJobComment",
                    jobNameValidation));
        }

        StringBuffer errMessage = new StringBuffer(
                "Could not add the comment to the object.  ");
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);
            activityArgs.put("userId", p_userId);
            activityArgs.put("comment", p_comment);
            activityArgs.put("fileName", p_fileName);
            activityArgs.put("access", p_access);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "addJobComment(p_accessToken,p_jobName,p_userId,p_comment,p_file,p_fileName,p_access)",
                            activityArgs);

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
        finally
        {

            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("activityName", p_activityName);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("targetLocale", p_targetLocale);
            activityArgs.put("month", p_month);
            activityArgs.put("year", p_year);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "getUserUnavailabilityReport(p_accessToken,p_activityName,p_sourceLocale,p_targetLocale,p_month,p_year)",
                            activityArgs);
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
        finally
        {

            if (activityStart != null)
            {
                activityStart.end();
            }
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

            if (logger.isDebugEnabled())
            {
                logger.debug("The xml string for file profile info :"
                        + xmlStr.toString());                
            }

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
        String jobNameValidation = validateJobName(jobName);
        if (jobNameValidation != null)
        {
            throw new WebServiceException(makeErrorXml("createDocumentumJob",
                    jobNameValidation));
        }

        StringBuffer errorMessage = new StringBuffer();
        ActivityLog.Start activityStart = null;
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating a documentum job (fileProfileId ="
                        + fileProfileId + ",objectId =" + userId + ", userId ="
                        + objectId + ")");
            }

            String dcmtFileName = null;
            String attrFileName = null;
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("jobName", jobName);
            activityArgs.put("fileProfileId", fileProfileId);
            activityArgs.put("objectId", objectId);
            activityArgs.put("userId", userId);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "createDocumentumJob(p_accessToken,jobName,fileProfileId,objectId,userId)",
                            activityArgs);
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
                if (logger.isDebugEnabled())
                {
                    logger.debug("The dctm file attribute xml String :"
                            + dctmFileAttrXml);                    
                }

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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("objectId", objectId);
            activityArgs.put("jobId", jobId);
            activityArgs.put("userId", userId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "cancelDocumentumJob(p_accessToken,objectId,jobId,userId)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }

    /**
     * check if some jobs is downloadable or delete them from backup file in
     * client
     * 
     * @param p_accessToken
     * @param p_message
     * @return xml String result 
     * <jobs>
     *   <job>
     *     <name>job name</name>
     *     <status>downloadable | create_error | unknown</status>
     *   </job>
     * </jobs>
     * @throws WebServiceException
     */
    public String getDownloadableJobs(String p_accessToken, String p_msg)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_DOWNLOADABLE_JOBS);

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
                HashMap<Long, Set<String>> jobDirMap = new HashMap<Long, Set<String>>();

                Job job = null;
                String status = "";
                for (Iterator iter = jobList.iterator(); iter.hasNext();)
                {
                    status = "unknown";
                    Element jobElement = (Element) iter.next();
                    String jobName = jobElement.element("name").getText();

                    try
                    {
                        job = queryJob(jobName, p_accessToken);
                        if (job != null)
                        {
                            long companyId = job.getCompanyId();
                            if (jobDirMap.get(companyId) == null)
                            {
                                Set<String> jobDirs = new HashSet<String>();
                                File diExportedDir = AmbFileStoragePathUtils
                                        .getDesktopIconExportedDir(companyId);
                                File[] files = diExportedDir.listFiles();
                                for (File file : files)
                                {
                                    jobDirs.add(file.getName());
                                }
                                jobDirMap.put(companyId, jobDirs);
                            }

                            Set<String> jobDirs2 = jobDirMap.get(companyId);
                            /*
                             * For old jobs created before 8.4, they use job name as
                             * folder name. After 8.4, it use job id instead of job name
                             */
                            if (jobDirs2.contains(jobName))
                            {
                                status = "downloadable";                            
                            }
                            else
                            {
                                String jobIdStr = String.valueOf(job.getJobId());
                                if (jobDirs2.contains(jobIdStr))
                                {
                                    status = "downloadable";
                                }
                            }
                        }
                    }
                    catch (WebServiceException ignore)
                    {
                        
                    }

                    jobElement.element("status").setText(status);
                }
            }

            return doc.asXML();
        }
        catch (Exception e)
        {
            logger.error(GET_DOWNLOADABLE_JOBS, e);
            String message = makeErrorXml(GET_DOWNLOADABLE_JOBS, e.getMessage());
            throw new WebServiceException(message);
        }
    }

    /**
     * Get the version of WebService
     * 
     * @deprecated Abandoned since 8.2.1. Use new method getGSVersion() instead.
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
     * version check since 8.2.1.
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
                xmlFileProfile.setKnownFormatTypeId(7);
                fpManager.createFileProfile(xmlFileProfile);
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Using a xml fileprofile, id="
                        + xmlFileProfile.getId());                
            }

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
    private String getRealPath(String jobId, String filePath, String srcLocale,
            boolean hasWebserviceInPath)
    {
        StringBuffer newPath = new StringBuffer();
        newPath.append(srcLocale);
        if (hasWebserviceInPath)
        {
            newPath.append(File.separator).append("webservice");
        }
        newPath.append(File.separator).append(jobId);
        newPath.append(File.separator).append(filePath);

        return newPath.toString();
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
            connection = ConnectionPool.getConnection();
            String condition = appendJobCondition(p_jobName);

            User user = getUser(getUsernameFromSession(p_accessToken));
            long companyId = CompanyWrapper.getCompanyByName(
                    user.getCompanyName()).getId();

            String sql = null;
            if (companyId != 1)
            {
                sql = "SELECT ID FROM JOB WHERE COMPANY_ID=? AND " + condition;
                query = connection.prepareStatement(sql);
                query.setLong(1, companyId);
                query.setString(2, p_jobName);
            }
            else
            {
                sql = "SELECT ID FROM JOB WHERE " + condition;
                query = connection.prepareStatement(sql);
                query.setString(1, p_jobName);
            }

            results = query.executeQuery();
            if (results.next())
            {
                long id = results.getLong(1);
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

    private String appendJobCondition(String p_jobName)
    {
        String condition = "NAME=?";

        try
        {
            int index = p_jobName.lastIndexOf("_");
            if (index > -1)
            {
                String random = p_jobName.substring(index + 1);
                if (random != null && random.length() > 6
                        && StringUtils.isNumeric(random))
                {
                    condition = "(NAME=? OR NAME LIKE '%" + random + "')";
                }
            }
        }
        catch (Exception ignore)
        {

        }

        return condition;
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
	private void buildXmlForTask(StringBuilder xml, Task t, String tab,
			Connection connection, String assignees,
			List<ConditionNodeTargetInfo> conList) throws WebServiceException
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
            if (!StringUtil.isEmpty(assignees))
            {
                xml.append(tab).append("\t<assignees>").append(assignees)
                        .append("</assignees>\r\n");
            }
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

    	xml.append(tab).append("\t<isSkipped>")
        		.append(AmbassadorHelper.isSkippedTask(t.getId()))
        		.append("</isSkipped>\r\n");
    	
		if (conList != null && conList.size() > 0)
		{
			for (int i = 0; i < conList.size(); i++)
			{
				ConditionNodeTargetInfo cti = (ConditionNodeTargetInfo) conList
						.get(i);
				String arrowName = xmlEncoder.encodeStringBasic(cti
						.getArrowName());
				String pointTo = cti.getTargetNodeName();
				xml.append(tab).append("\t<outgoing>\r\n");
				xml.append(tab).append("\t\t<arrowName>").append(arrowName)
						.append("</arrowName>\r\n");
				xml.append(tab).append("\t\t<pointTo>").append(pointTo)
						.append("</pointTo>\r\n");
				xml.append(tab).append("\t</outgoing>\r\n");
			}
		}

        	
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
     * Get the display date and time for the given date object. Note that
     * current logged user's time zone will be used.
     * 
     * @param p_date
     *            - The date and time to be displayed.
     * @param p_tz
     *            - TimeZone of current logged user
     * @return String Formatted date
     */
    private String convertDateToString(Date p_date, TimeZone p_tz)
    {
        if (p_date == null)
        {
            return "";
        }

        Timestamp ts = new Timestamp(p_tz);
        // This will decide the returned date patten, we all use "en_IE" as our
        // locale in API, the data sample is like
        // "14/05/14 09:25:11 o'clock GMT-00:00".
        Locale enIE = new Locale("en", "IE");
        ts.setLocale(enIE);
        ts.setDate(p_date);

        return ts.toString();
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
        urlPrefix.append(AmbassadorUtil.getCapLoginOrPublicUrl());
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
        ActivityLog.Start activityStart = null;
        try
        {

            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("p_tmProfileName", p_tmProfileName);
            activityArgs.put("sid", sid);
            activityArgs.put("p_sourceLocale", p_sourceLocale);
            activityArgs.put("p_sourceSegment", p_sourceSegment);
            activityArgs.put("p_targetLocale", p_targetLocale);
            activityArgs.put("p_targetSegment", p_targetSegment);
            activityArgs.put("escapeString", escapeString);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "saveEntry(p_accessToken,p_tmProfileName,sid,p_sourceLocale,p_sourceSegment,p_targetLocale,p_targetSegment,escapeString)",
                            activityArgs);
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
                LingServerProxy.getTmCoreManager().saveToSegmentTm(tm, tus,
                        TmCoreManager.SYNC_MERGE, null);

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
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("tmProfileName", p_tmProfileName);
            activityArgs.put("string", p_string);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("escapeString", escapeString);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "searchEntries(p_accessToken,p_tmProfileName,p_string,p_sourceLocale,escapeString)",
                            activityArgs);

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
                            sourceLocale, trgLocales, levOptions)
                    .leverageResultIterator();

            long jobId = -1;// -1 is fine here.
            // In fact only ONE levMatches in this iterator.
            while (itLeverageMatches.hasNext())
            {
                LeverageMatches levMatches = (LeverageMatches) itLeverageMatches
                        .next();

                // walk through all target locales in the LeverageMatches
                Iterator itLocales = levMatches.targetLocaleIterator(jobId);
                while (itLocales.hasNext())
                {
                    GlobalSightLocale tLocale = (GlobalSightLocale) itLocales
                            .next();
                    // walk through all matches in the locale
                    Iterator itMatch = levMatches.matchIterator(tLocale, jobId);
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

            // Return NULL_XML if no TM matches whose score is higher than TM
            // threshold.
            if (localeNames.size() == 0)
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
            if (activityStart != null)
            {
                activityStart.end();
            }
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
                                levOptions);
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

                long jobId = -1; // -1 is fine here
                Iterator itLocales = levMatches.targetLocaleIterator(jobId);
                while (itLocales.hasNext())
                {
                    GlobalSightLocale targetLocale = (GlobalSightLocale) itLocales
                            .next();
                    Vector matchedTuvMapForSpecifiedTrgLocale = new Vector();

                    HashMap innerMap = new HashMap();
                    Iterator itMatch = levMatches.matchIterator(targetLocale,
                            jobId);
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
     * @deprecated use "editTu()" instead.
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
     * @deprecated use "editTu()" instead.
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
     * @deprecated use "editTu()" instead.
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("tmProfileName", p_tmProfileName);
            activityArgs.put("orgSid", p_orgSid);
            activityArgs.put("newSid", p_newSid);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("sourceSegment", p_sourceSegment);
            activityArgs.put("targetLocale", p_targetLocale);
            activityArgs.put("targetSegment", p_targetSegment);
            activityArgs.put("escapeString", escapeString);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "editEntry(p_accessToken,p_tmProfileName,p_orgSid,p_newSid,p_sourceLocale,p_sourceSegment,p_targetLocale,p_targetSegment,escapeString)",
                            activityArgs);
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
            if (activityStart != null)
            {
                activityStart.end();
            }
            HibernateUtil.closeSession();
            
        }
    }

    /**
     * Removes specified segment
     * 
     * @deprecated 
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
     * Note: If <code>deleteLocale</code> is source locale or null, the entry
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("tmProfileName", p_tmProfileName);
            activityArgs.put("string", p_string);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("deleteLocale", p_deleteLocale);
            activityArgs.put("escapeString", escapeString);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "deleteSegment(p_accessToken,p_tmProfileName,p_string,p_sourceLocale,p_deleteLocale,escapeString)",
                            activityArgs);

            ProjectTM ptm = (ProjectTM) getProjectTm(p_tmProfileName);
            if (ptm.getTm3Id() == null)
            {
                deleteTm2Segment(p_tmProfileName, p_string, p_sourceLocale,
                        p_deleteLocale);
            }
            else
            {
                deleteTm3Segment(ptm, p_string, p_sourceLocale, p_deleteLocale);
            }
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
            HibernateUtil.closeSession();
        }
    }

    private void deleteTm2Segment(String p_tmProfileName, String p_string,
            String p_sourceLocale, String p_deleteLocale)
            throws WebServiceException
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

    private void deleteTm3Segment(ProjectTM ptm, String p_string,
            String p_sourceLocale, String p_deleteLocale)
            throws WebServiceException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            String tuvTable = "tm3_tuv_shared_" + ptm.getCompanyId();

            StatementBuilder sb = new StatementBuilder();
            sb.append("SELECT tuId FROM ").append(tuvTable);
            sb.append(" WHERE content = '").append(p_string).append("' ");
            sb.append(" AND localeid = ? ").addValue(
                    getLocaleByName(p_sourceLocale).getId());
            sb.append(" AND tmid = ? ").addValue(ptm.getTm3Id());
            // Only return the first matched tu for now.
//            sb.append(" LIMIT 0,1;");
            
            List<Long> tuIds = SQLUtil.execIdsQuery(conn, sb);
            if (tuIds == null || tuIds.size() == 0)
            {
                logger.warn("deleteTm3Segment() :: do not find data to delete by current parameters.");
                return;
            }

            BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
            List<TM3Tu> tm3Tus = tm.getTu(tuIds);

            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);
            List<SegmentTmTu> segTmTus = new ArrayList<SegmentTmTu>();
            for (TM3Tu tm3Tu : tm3Tus)
            {
                segTmTus.add(TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(),
                        formatAttr, typeAttr, sidAttr, fromWsAttr,
                        translatableAttr, projectAttr));
            }

            if (p_deleteLocale == null
                    || p_sourceLocale.equalsIgnoreCase(p_deleteLocale))
            {
                try
                {
                    ptm.getSegmentTmInfo().deleteSegmentTmTus(ptm, segTmTus);
                }
                catch (Exception e)
                {
                    throw new WebServiceException(e.getMessage());
                }

                return;
            }

            long deleteLocaleId = getLocaleByName(p_deleteLocale).getId();
            for (SegmentTmTu tu : segTmTus)
            {
                List<BaseTmTuv> tuvs = tu.getTuvs();
                Set<SegmentTmTuv> movedTuvs = new HashSet<SegmentTmTuv>();
                for (BaseTmTuv tuv : tuvs)
                {
                    if (deleteLocaleId == tuv.getLocale().getId())
                    {
                        movedTuvs.add((SegmentTmTuv) tuv);
                    }
                }

                if (tu.getTuvs().size() < movedTuvs.size() + 2)
                {
                    List<SegmentTmTu> del = new ArrayList<SegmentTmTu>();
                    del.add(tu);
                    ptm.getSegmentTmInfo().deleteSegmentTmTus(ptm, del);
                }
                else if (movedTuvs.size() > 0)
                {
                    ptm.getSegmentTmInfo().deleteSegmentTmTuvs(ptm, movedTuvs);
                }
            }
        }
        catch (Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
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

    private Company getCompanyByName(String companyName)
            throws WebServiceException
    {
        String hql = "from Company where name = :name";
        HashMap map = new HashMap();
        map.put("name", companyName);
        return (Company) HibernateUtil.getFirst(hql, map);
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
        name = ImportUtil.normalizeLocale(name.trim());
        try
        {
            return ImportUtil.getLocaleByName(name);
        }
        catch (Exception e)
        {
            logger.warn("getLocaleByName() : Fail to get GlobalSightLocale by locale name: '"
                    + name + "'");
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
        // Only return the first matched tu.
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("termbaseName", p_termbaseName);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("sourceTerm", p_sourceTerm);
            activityArgs.put("targetLocale", p_targetLocale);
            activityArgs.put("targetTerm", p_targetTerm);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "saveTBEntry(p_accessToken,p_termbaseName,p_sourceLocale,p_sourceTerm,p_targetLocale,p_targetTerm)",
                            activityArgs);
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
            if (activityStart != null)
            {
                activityStart.end();
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("termbaseName", p_termbaseName);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("sourceTerm", p_sourceTerm);
            activityArgs.put("targetLocale", p_targetLocale);
            activityArgs.put("targetTerm", p_targetTerm);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "editTBEntry(p_accessToken,p_termbaseName,p_sourceLocale,p_sourceTerm,p_targetLocale,p_targetTerm,connection)",
                            activityArgs);
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

            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("termbaseName", p_termbaseName);
            activityArgs.put("searchString", p_searchString);
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityArgs.put("targetLocale", p_targetLocale);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "deleteTBEntry(p_accessToken,p_termbaseName,p_searchString,p_sourceLocale,p_targetLocale)",
                            activityArgs);
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
            if (activityStart != null)
            {
                activityStart.end();
            }
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
     *            source locale, required, like "EN_US".
     * @param targetLocale
     *            target locale, optional, like "FR_FR".
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

        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("tmName", tmName);
            activityArgs.put("companyName", companyName);
            activityArgs.put("sourceLocale", sourceLocale);
            activityArgs.put("targetLocale", targetLocale);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "getFirstTu(p_accessToken,tmName,companyName,sourceLocale,targetLocale)",
                            activityArgs);

            Company company = getCompanyByName(companyName);
            if (company == null)
            {
                throw new WebServiceException(
                        "Can not find the company with name (" + companyName + ")");
            }
            ProjectTM tm = getProjectTm(tmName, company.getIdAsLong());
            if (tm == null)
            {
                throw new WebServiceException(
                        "Can not find the tm with tm name (" + tmName
                                + ") and company name (" + companyName + ")");
            }

            GlobalSightLocale srcGSLocale = getLocaleByName(sourceLocale);
            GlobalSightLocale trgGSLocale = null;
            if (targetLocale != null && targetLocale.length() > 0)
            {
                trgGSLocale = getLocaleByName(targetLocale);
            }

            if (tm.getTm3Id() == null)
            {
                return getFirstTm2Tu(tm, srcGSLocale, trgGSLocale);
            }
            else
            {
                return getFirstTm3Tu(tm, srcGSLocale, trgGSLocale);
            }
        }
        catch (Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
            HibernateUtil.closeSession();
        }
    }

    private String getFirstTm2Tu(ProjectTM ptm, GlobalSightLocale srcGSLocale,
            GlobalSightLocale trgGSLocale) throws WebServiceException
    {
        ProjectTmTuT tu = null;
        HashMap paramsMap = new HashMap();
        String hql = null;
        if (trgGSLocale != null)
        {
            hql = "select tuv.tu from ProjectTmTuvT tuv "
                    + "where tuv.locale.id = :tId "
                    + "and tuv.tu.projectTm.id = :tmId "
                    + "and tuv.tu.sourceLocale.id = :sId "
                    + "order by tuv.tu.id asc";
            paramsMap.put("tId", trgGSLocale.getId());
        }
        else
        {
            hql = "from ProjectTmTuT tu where tu.projectTm.id = :tmId "
                    + "and tu.sourceLocale.id = :sId order by tu.id asc";
        }
        paramsMap.put("tmId", ptm.getId());
        paramsMap.put("sId", srcGSLocale.getId());
        List<ProjectTmTuT> tus = (List<ProjectTmTuT>) HibernateUtil.search(hql,
                paramsMap, 0, 1);
        if (tus == null || tus.size() == 0)
        {
            return null;
        }
        tu = tus.get(0);

        List<GlobalSightLocale> targetLocales = null;
        if (trgGSLocale != null)
        {
            targetLocales = new ArrayList<GlobalSightLocale>();
            targetLocales.add(trgGSLocale);
        }

        return tu.convertToTmx(targetLocales);
    }

    private <T> String getFirstTm3Tu(ProjectTM ptm,
            GlobalSightLocale srcGSLocale, GlobalSightLocale trgGSLocale)
            throws WebServiceException
    {
        Connection conn = null;
        try
        {
            long firstTuId = 0;
            conn = DbUtil.getConnection();

            String tuTable = "tm3_tu_shared_" + ptm.getCompanyId();
            String tuvTable = "tm3_tuv_shared_" + ptm.getCompanyId();

            StatementBuilder sb = new StatementBuilder();
            if (trgGSLocale != null)
            {
                sb.append("SELECT tuv.tuId FROM ").append(tuvTable)
                        .append(" tuv, (SELECT id FROM ").append(tuTable)
                        .append(" tu WHERE tu.tmid = ? ")
                        .addValue(ptm.getTm3Id())
                        .append(" AND tu.srcLocaleId = ? ")
                        .addValue(srcGSLocale.getId())
                        .append(" ORDER BY tu.id LIMIT 0,1000) tuids ")
                        .append("WHERE tuv.tuId = tuids.id ")
                        .append("AND tuv.localeId = ? ")
                        .addValue(trgGSLocale.getId())
                        .append(" ORDER BY tuv.tuId LIMIT 0,1;");
            }
            else
            {
                sb.append("SELECT id FROM ").append(tuTable)
                        .append(" WHERE tmid = ? ").addValue(ptm.getTm3Id())
                        .append(" AND srcLocaleId = ? ")
                        .addValue(srcGSLocale.getId())
                        .append(" ORDER BY id LIMIT 0,1;");
            }
            List<Long> tuIds = SQLUtil.execIdsQuery(conn, sb);
            if (tuIds != null && tuIds.size() > 0)
            {
                firstTuId = tuIds.get(0);
            }
    
            BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
            TM3Tu tm3Tu = tm.getTu(firstTuId);

            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);
            SegmentTmTu segTmTu = TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(),
                    formatAttr, typeAttr, sidAttr, fromWsAttr,
                    translatableAttr, projectAttr);

            List<GlobalSightLocale> targetLocales = null;
            if (trgGSLocale != null)
            {
                targetLocales = new ArrayList<GlobalSightLocale>();
                targetLocales.add(trgGSLocale);
            }

            return TmxUtil.convertToTmx(segTmTu, targetLocales);
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Search tus according to the specified tu. Only work for TM2.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param sourceLocale
     *            The source lcoale, like "EN_US"(case-insensitive).
     * @param targetLocale
     *            The target locale, like "FR_FR"(case-insensitive).
     * @param maxSize
     *            The max size of return tus.
     * @param tuIdToStart
     *            The id of specified tu. The specified tu is used to get the tm
     *            id, and it will return tus starting with the one after this
     *            tuId.
     * @return A tmx format string, including all tus' information.
     * 
     * @deprecated -- This API's parameters can't support both TM2 and TM3.
     * 
     * @throws WebServiceException
     */
    public String nextTus(String accessToken, String sourceLocale,
            String targetLocale, String maxSize, String tuIdToStart)
            throws WebServiceException
    {
        try
        {
            ProjectTmTuT tu = HibernateUtil.get(ProjectTmTuT.class,
                    Long.parseLong(tuIdToStart));
            if (tu == null)
            {
                throw new WebServiceException("Can not find tu with id :"
                        + tuIdToStart);
            }
            ProjectTM ptm = tu.getProjectTm();
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    ptm.getCompanyId());
            return nextTus(accessToken, ptm.getName(),
                    company.getCompanyName(), sourceLocale, targetLocale,
                    maxSize, tuIdToStart);
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Search tus according to the specified tu.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmName
     *            TM name, will used to get tm id.
     * @param companyName
     *            company name, will used to get tm id.
     * @param sourceLocale
     *            The source lcoale, like "EN_US"(case-insensitive).
     * @param targetLocale
     *            The target locale, like "FR_FR"(case-insensitive).
     * @param maxSize
     *            The max size of return tus.
     * @param tuIdToStart
     *            The id of specified tu. The specified tu is used to get the tm
     *            id, and it will return tus starting with the one after this
     *            tuId.
     * @return A tmx format string, including all tus' information.
     * 
     * @throws WebServiceException
     */
    public String nextTus(String accessToken, String tmName,
            String companyName, String sourceLocale,
            String targetLocale, String maxSize, String tuIdToStart)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(accessToken, "access token");
            Assert.assertNotEmpty(tmName, "tm name");
            Assert.assertNotEmpty(companyName, "company name");
            Assert.assertNotEmpty(sourceLocale, "source locale");
            Long.parseLong(tuIdToStart);
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

        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("tmName", tmName);
            activityArgs.put("companyName", companyName);
            activityArgs.put("sourceLocale", sourceLocale);
            activityArgs.put("targetLocale", targetLocale);
            activityArgs.put("maxSize", maxSize);
            activityArgs.put("tuId", tuIdToStart);
            activityStart = ActivityLog.start(Ambassador.class, "nextTus",
                    activityArgs);

            Company company = getCompanyByName(companyName);
            if (company == null)
            {
                throw new WebServiceException(
                        "Can not find the company with name (" + companyName + ")");
            }
            ProjectTM ptm = getProjectTm(tmName, company.getIdAsLong());
            if (ptm == null)
            {
                throw new WebServiceException(
                        "Can not find the tm with tm name (" + tmName
                                + ") and company name (" + companyName + ")");
            }

            GlobalSightLocale srcGSLocale = getLocaleByName(sourceLocale);
            GlobalSightLocale trgGSLocale = null;
            if (targetLocale != null && targetLocale.length() > 0)
            {
                trgGSLocale = getLocaleByName(targetLocale);
            }

            if (ptm.getTm3Id() == null)
            {
                return nextTm2Tus(ptm, srcGSLocale, trgGSLocale,
                        Long.parseLong(tuIdToStart), size);
            }
            else
            {
                return nextTm3Tus(ptm, srcGSLocale, trgGSLocale,
                        Long.parseLong(tuIdToStart), size);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
            HibernateUtil.closeSession();
        }
    }

    private String nextTm2Tus(ProjectTM ptm, GlobalSightLocale srcGSLocale,
            GlobalSightLocale trgGSLocale, long tuIdToStart, int size)
            throws WebServiceException
    {
        ProjectTmTuT tu = HibernateUtil.get(ProjectTmTuT.class, tuIdToStart);
        if (tu == null)
        {
            throw new WebServiceException("Can not find tu with id "
                    + tuIdToStart + " in current TM '" + ptm.getName() + "'.");
        }

        String hql = null;
        HashMap map = new HashMap();
        map.put("tmId", ptm.getId());
        map.put("sId", srcGSLocale.getId());
        map.put("fId", tuIdToStart);
        if (trgGSLocale != null)
        {
            hql = "select distinct tuv.tu from ProjectTmTuvT tuv where tuv.locale.id = :tId "
                    + "and tuv.tu.projectTm.id = :tmId "
                    + "and tuv.tu.sourceLocale.id = :sId "
                    + "and tuv.tu.id > :fId order by tuv.tu.id asc";
            map.put("tId", trgGSLocale.getId());
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
        if (trgGSLocale != null)
        {
            targetLocales = new ArrayList<GlobalSightLocale>();
            targetLocales.add(trgGSLocale);
        }

        StringBuilder result = new StringBuilder();
        for (ProjectTmTuT pTu : tus)
        {
            result.append(pTu.convertToTmx(targetLocales));
        }

        return result.toString();
    }

    private String nextTm3Tus(ProjectTM ptm, GlobalSightLocale srcGSLocale,
            GlobalSightLocale trgGSLocale, long tuIdToStart, int size)
            throws WebServiceException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();

            String tuTable = "tm3_tu_shared_" + ptm.getCompanyId();
            String tuvTable = "tm3_tuv_shared_" + ptm.getCompanyId();

            StatementBuilder sb = new StatementBuilder();
            if (trgGSLocale != null)
            {
                sb.append("SELECT tuv.tuId FROM ").append(tuvTable).append(" tuv,");
                sb.append(" (SELECT id FROM ").append(tuTable).append(" tu ")
                        .append("WHERE tu.tmid = ? ").addValue(ptm.getTm3Id())
                        .append(" AND tu.srcLocaleId = ? ")
                        .addValue(srcGSLocale.getId())
                        .append(" AND tu.id > ? ").addValue(tuIdToStart)
                        .append(" ORDER BY tu.id LIMIT 0, ")
                        .append(String.valueOf(10 * size)).append(") tuids ");
                sb.append(" WHERE tuv.tuId = tuids.id");
                sb.append(" AND tuv.localeId = ? ").addValue(trgGSLocale.getId());
                sb.append(" AND tuv.tmId = ? ").addValue(ptm.getTm3Id());
                sb.append(" ORDER BY tuv.tuId ");
                sb.append(" LIMIT 0, ").append(String.valueOf(size)).append(";");
            }
            else
            {
                sb.append("SELECT id FROM ").append(tuTable)
                        .append(" WHERE tmid = ? ").addValue(ptm.getTm3Id())
                        .append(" AND srcLocaleId = ? ")
                        .addValue(srcGSLocale.getId())
                        .append(" AND id > ? ").addValue(tuIdToStart)
                        .append(" ORDER BY id ")
                        .append("LIMIT 0,").append(size + ";");
            }
            List<Long> tuIds = SQLUtil.execIdsQuery(conn, sb);
            if (tuIds == null || tuIds.size() == 0)
            {
                return null;
            }
    
            BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
            List<TM3Tu> tm3Tus = tm.getTu(tuIds);

            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);
            List<SegmentTmTu> segTmTus = new ArrayList<SegmentTmTu>();
            for (TM3Tu tm3Tu : tm3Tus)
            {
                segTmTus.add(TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(),
                        formatAttr, typeAttr, sidAttr, fromWsAttr,
                        translatableAttr, projectAttr));
            }

            List<GlobalSightLocale> targetLocales = null;
            if (trgGSLocale != null)
            {
                targetLocales = new ArrayList<GlobalSightLocale>();
                targetLocales.add(trgGSLocale);
            }

            StringBuffer result = new StringBuffer();
            for (SegmentTmTu segTmTu: segTmTus)
            {
                result.append(TmxUtil.convertToTmx(segTmTu, targetLocales));
            }

            return result.toString();
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Updates a tu in database. Only work for TM2.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmx
     *            A tmx formate string inlcluding all tu information.
     * @return
     * @deprecated only work for TM2.
     * 
     * @throws WebServiceException
     */
    public String editTu(String accessToken, String tmx)
            throws WebServiceException
    {
        long tuId = -1;
        SAXReader reader = new SAXReader();
        try
        {
            Document doc = reader.read(new StringReader("<root>" + tmx
                    + "</root>"));
            List tuNodes = doc.getRootElement().selectNodes("//tu");
            if (tuNodes != null && tuNodes.size() > 0)
            {
                Iterator nodeIt = tuNodes.iterator();
                while (nodeIt.hasNext())
                {
                    Element tuEle = (Element) nodeIt.next();
                    tuId = Long.parseLong(tuEle.attributeValue(Tmx.TUID));
                    break;
                }
            }

            ProjectTmTuT tu = HibernateUtil.get(ProjectTmTuT.class, tuId);
            if (tu == null)
            {
                throw new WebServiceException("Can not find tu with id :"
                        + tuId);
            }
            ProjectTM ptm = tu.getProjectTm();
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    ptm.getCompanyId());

            return editTu(accessToken, ptm.getName(), company.getName(), tmx);
        }
        catch (Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Updates a tu in database.
     * 
     * @param accessToken
     *            To judge caller has logon or not, can not be null. you can get
     *            it by calling method <code>login(username, password)</code>.
     * @param tmName
     *            TM name, will used to get tm id.
     * @param companyName
     *            company name, will used to get tm id.
     * @param tmx
     *            A tmx formate string inlcluding all tu information.
     * @return "true" if succeed
     * @throws WebServiceException
     */
    public String editTu(String accessToken, String tmName, String companyName,
            String tmx) throws WebServiceException
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

        Company company = getCompanyByName(companyName);
        if (company == null)
        {
            throw new WebServiceException(
                    "Can not find the company with name (" + companyName + ")");
        }
        final ProjectTM ptm = getProjectTm(tmName, company.getIdAsLong());
        if (ptm == null)
        {
            throw new WebServiceException(
                    "Can not find the tm with tm name (" + tmName
                            + ") and company name (" + companyName + ")");
        }

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
                    if (ptm.getTm3Id() == null)
                    {
                        editTm2Tu(element);
                    }
                    else
                    {
                        editTm3Tu(element, ptm);
                    }
                }
                catch (Throwable ex)
                {
                    logger.error(ex.getMessage(), ex);
                    throw new ThreadDeath();
                }
            }
        };
        reader.addHandler("/tu", handler);

        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "editTu(accessToken,tmx)", activityArgs);
            reader.read(new StringReader(tmx));
        }
        catch (DocumentException e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return "true";
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
     * @param ptm 
     * @throws Exception
     */
    private void editTm2Tu(Element p_root) throws Exception
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

    private void editTm3Tu(Element p_root, ProjectTM ptm) throws Exception
    {
        String tuId = p_root.attributeValue(Tmx.TUID);
        BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
        TM3Tu tm3Tu = tm.getTu(Long.parseLong(tuId));

        TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
        TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
        TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
        TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
        TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
        TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);
        SegmentTmTu tu = TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(), formatAttr,
                typeAttr, sidAttr, fromWsAttr, translatableAttr, projectAttr);

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
        String sid = null;
        node = p_root.selectSingleNode(".//prop[@type= '"
                + Tmx.PROP_TM_UDA_SID + "']");
        if (node != null)
        {
            sid = node.getText();
            tu.setSID(sid);
        }
        // TUVs
        List nodes = p_root.elements("tuv");
        List<SegmentTmTuv> tuvsToBeUpdated = new ArrayList<SegmentTmTuv>();
        for (int i = 0; i < nodes.size(); i++)
        {
            Element elem = (Element) nodes.get(i);
            SegmentTmTuv tuv = new SegmentTmTuv();
            tuv.setSid(sid);
            TmxUtil.convertFromTmx(elem, tuv);

            // Check the locale
            List<SegmentTmTuv> savedTuvs = new ArrayList<SegmentTmTuv>();
            for (BaseTmTuv savedTuv : tu.getTuvs())
            {
                if (savedTuv.getLocale().equals(tuv.getLocale()))
                {
                    savedTuvs.add((SegmentTmTuv) savedTuv);
                }
            }

            if (savedTuvs.size() > 1)
            {
                boolean find = false;
                for (SegmentTmTuv savedTuv : savedTuvs)
                {
                    if (savedTuv.getCreationDate().getTime() == tuv
                            .getCreationDate().getTime())
                    {
                        find = true;
                        savedTuv.merge(tuv);
                        tuvsToBeUpdated.add(savedTuv);
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
                SegmentTmTuv savedTuv = savedTuvs.get(0);
                savedTuv.merge(tuv);
                tuvsToBeUpdated.add(savedTuv);
            }
        }

        ptm.getSegmentTmInfo().updateSegmentTmTuvs(ptm, tuvsToBeUpdated);
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
        ActivityLog.Start activityStart = null;
        try
        {
            // get permission set for current user
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(p_accessToken));
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", user.getUserName());
            activityArgs.put("sourceLocale", p_sourceLocale);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getTargetLocales(p_accessToken,p_sourceLocale)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("l10nID", p_l10nID);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getPriorityByID(p_accessToken,p_l10nID)", activityArgs);
            ProjectHandlerLocal handler = new ProjectHandlerLocal();
            BasicL10nProfile basicL10nProfile = (BasicL10nProfile) handler
                    .getL10nProfile(Long.parseLong(p_l10nID));
            strReturn.append(basicL10nProfile.getPriority());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotNull(p_jobId, "job id");

            checkAccess(p_accessToken, "getAttributesByJobId");
            // checkPermission(p_accessToken, Permission.JOB_ATTRIBUTE_VIEW);
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("jobId", p_jobId);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "getAttributesByJobId(p_accessToken,p_jobId)",
                            activityArgs);

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
        finally
        {

            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
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
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobId", jobId);
            activityArgs.put("attInternalName", attInternalName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "setJobAttribute(accessToken,jobId,attInternalName,value)",
                    activityArgs);

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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
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
                String loggedUserName = this
                        .getUsernameFromSession(accessToken);
                Map<Object, Object> activityArgs = new HashMap<Object, Object>();
                activityArgs.put("loggedUserName", loggedUserName);
                activityArgs.put("jobId", jobId);
                activityArgs.put("attInternalName", attInternalName);
                activityStart = ActivityLog.start(Ambassador.class,
                        "getJobAttribute(accessToken,jobId,attInternalName)",
                        activityArgs);
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
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return null;
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
        ActivityLog.Start activityStart = null;
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
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("projectId", p_projectId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getAttributesByProjectId(p_accessToken,p_projectId)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
            String jobNameValidation = validateJobName(jobName);
            if (jobNameValidation != null)
            {
                throw new WebServiceException(makeErrorXml("uploadAttributeFiles",
                        jobNameValidation));
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("fileProfileId", p_fileProfileId);
            activityArgs.put("srcLangCountry", p_srcLangCountry);
            activityArgs.put("trgLangCountry", p_trgLangCountry);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "isSupportCurrentLocalePair(p_accessToken,p_fileProfileId,p_srcLangCountry,p_trgLangCountry)",
                            activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
            String jobNameValidation = validateJobName(jobName);
            if (jobNameValidation != null)
            {
                throw new WebServiceException(makeErrorXml("uploadOriginalSourceFile",
                        jobNameValidation));
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
        ActivityLog.Start activityStart = null;
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
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("taskId", p_taskId);
            activityArgs.put("state", p_state);
            activityStart = ActivityLog.start(Ambassador.class,
                    "updateTaskState(p_accessToken,p_taskId,p_state)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
     * Import offline transkit back to update translations in system. Before
     * this, use "uploadEditionFileBack()" API to upload offline transkit to
     * server first.
     * 
     * @param p_accessToken
     * @param p_originalTaskId
     *            Task ID
     * @return String -- If the method works fine, then it will return null.
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
        ActivityLog.Start activityStart = null;
        if (fileStates != null)
        {
            if (!StringUtil.isEmpty(filename))
            {
                String loggedUserName = this
                        .getUsernameFromSession(accessToken);
                Map<Object, Object> activityArgs = new HashMap<Object, Object>();
                activityArgs.put("loggedUserName", loggedUserName);
                activityArgs.put("taskId", taskId);
                activityArgs.put("filename", filename);
                activityStart = ActivityLog
                        .start(Ambassador.class,
                                "getOfflineFileUploadStatus(accessToken,taskId,filename)",
                                activityArgs);
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

        }
        if (activityStart != null)
        {
            activityStart.end();
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
     * @deprecated -- For GS Edition feature only.
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
                // TODO: -1 does not work, but as this API is useless anymore,
                // do not fix it now.
                long jobId = -1;
                localeTuv = tuvManager.getTuvForSegmentEditor(tuId, localeId,
                        jobId);
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
                                // TODO: -1 does not work, but as this API is
                                // useless anymore, do not fix it now.
                                long jobId = -1;
                                lg_id = ServerProxy
                                        .getTuvManager()
                                        .getTuForSegmentEditor(tuId, jobId)
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
     * @deprecated -- GS Edition API.
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
        ActivityLog.Start activityStart = null;
        if (resultList != null && resultList.size() > 0)
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("userIdToDiscardJob", p_userIdToDiscardJob);
            activityArgs.put("taskId", p_taskId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "discardJob(p_accessToken,p_userIdToDiscardJob,p_taskId)",
                    activityArgs);
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
        if (activityStart != null)
        {
            activityStart.end();
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

        ActivityLog.Start activityStart = null;
        StringBuffer jobIdList = new StringBuffer();
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "fetchJobIdsPerCompany(p_accessToken)", activityArgs);

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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("offset", p_offset);
            activityArgs.put("p_count", p_count);
            activityArgs.put("isDescOrder", p_isDescOrder);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "fetchJobsByRange(p_accessToken, p_offset,p_count,p_isDescOrder)",
                            activityArgs);

            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();

            Company company = getCompanyInfo(userName);
            if (company != null)
            {
                String[] ids = jobHandler.getJobIdsByCompany(
                        String.valueOf(company.getId()), p_offset, p_count,
                        p_isDescOrder);
                if (ids != null && ids.length > 0)
                {
                    result = fetchJobsPerCompany(p_accessToken, ids, true,
                            true, false);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }

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
        ActivityLog.Start activityStart = null;

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("state", p_state);
            activityArgs.put("offset", p_count);
            activityArgs.put("count", p_count);
            activityArgs.put("isDescOrder", p_isDescOrder);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "fetchJobsByState(p_accessToken, p_state,p_offset,p_count,p_isDescOrder)",
                            activityArgs);
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();

            Company company = getCompanyInfo(userName);
            if (company != null)
            {
                String[] ids = jobHandler.getJobIdsByState(
                        String.valueOf(company.getId()), p_state, p_offset,
                        p_count, p_isDescOrder);
                if (ids != null && ids.length > 0)
                {
                    result = fetchJobsPerCompany(p_accessToken, ids, true,
                            true, false);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }

        }
    }
    
    /**
     * Get jobs according with special creator userName, offset and count of fetching
     * records in current company
     * 
     * @param p_accessToken
     *            Access token
     * @param p_creatorUserName
     *            Creator userName of job.
     * @param p_offset
     *            Begin index of records
     * @param p_count
     *            Number count of fetching records
     * @return xml string
     * @throws WebServiceException
     */
    public String fetchJobsByCreator(String p_accessToken,
    		String p_creatorUserName,int p_offset, int p_count,
    		boolean p_isDescOrder) throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_creatorUserName, "Creator userName");
            p_offset = p_offset < 1 ? 0 : p_offset - 1;
            p_count = p_count <= 0 ? 1 : p_count;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return makeErrorXml("fetchJobsByCreator", e.getMessage());
        }

        String result = null;
        ActivityLog.Start activityStart = null;

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("creatorUserName", p_creatorUserName);
            activityArgs.put("offset", p_count);
            activityArgs.put("count", p_count);
            activityArgs.put("isDescOrder", p_isDescOrder);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "fetchJobsByCreator(p_accessToken," +
                            " p_creatorUserName, p_offset, " +
                            "p_count, p_isDescOrder)",
                            activityArgs);
            JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
            String creatorUserId =  UserUtil.getUserIdByName(p_creatorUserName);
            if(creatorUserId == null)
            {
            	return makeErrorXml("fetchJobsByCreator", "Creator username " 
            			+ p_creatorUserName + " does not exist.");
            }

            Company company = getCompanyInfo(userName);
            Long companyId = company.getIdAsLong();
            if(company != null &&  
            		!CompanyWrapper.isSuperCompany(companyId.toString()))
            {
            	Company tempCompany = getCompanyInfo(p_creatorUserName);
                Long tempCompanyId = tempCompany.getIdAsLong();
                if(companyId != tempCompanyId && 
                		!CompanyWrapper.isSuperCompany(tempCompanyId.toString()))
                {
                	return makeErrorXml("fetchJobsByCreator", 
                			"Can't fetch jobs by creator in other company.");
                }
            }
            if (company != null)
            {
                String[] ids = jobHandler.getJobIdsByCreator(
                        company.getId(), creatorUserId,p_offset, p_count, p_isDescOrder);
                if (ids != null && ids.length > 0)
                {
                    result = fetchJobsPerCompany(p_accessToken, ids, true,
                            true, false);
                }
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return makeErrorXml("fetchJobsByCreator", e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }

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

            String userName = getUsernameFromSession(p_accessToken);
            Company company = getCompanyInfo(userName);
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

        ActivityLog.Start activityStart = null;
        try
        {
            String jobIds = fetchJobIdsPerCompany(p_accessToken);
            String[] ids = null;
            if (jobIds != null && jobIds.trim().length() > 0)
            {
                ids = jobIds.split(",");
            }

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", getUsernameFromSession(p_accessToken));
            activityArgs.put("jobNum", ids == null ? 0 : ids.length);
            activityArgs.put("jobIds", jobIds);
            activityStart = ActivityLog.start(Ambassador.class,
                    "fetchJobsPerCompany(p_accessToken)", activityArgs);

            if (ids != null && ids.length > 0)
            {
                return fetchJobsPerCompany(p_accessToken, ids, true, true,
                        false);
            }
            else
            {
                return "There is no jobs in current company";                
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = makeErrorXml("fetchJobsPerCompany", e.getMessage());
            throw new WebServiceException(message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();                
            }
        }
    }

    /**
     * Fetch jobs for specified jobIds.
     * 
     * @param p_accessToken
     *            -- accessToken
     * @param p_jobIds
     *            -- jobIds in array.
     * 
     * @return xml String
     * 
     * @throws WebServiceException
     */
    public String fetchJobsPerCompany(String p_accessToken, String[] p_jobIds)
            throws WebServiceException
    {
        return fetchJobsPerCompany(p_accessToken, p_jobIds, true, true, false);
    }

    /**
     * Fetch jobs for specified jobIds.
     * 
     * @param p_accessToken
     *            -- p_accessToken
     * @param p_jobIds
     *            -- jobIds in array.
     * @param p_returnSourcePageInfo
     *            -- flag to indicate if return source pages info.
     * @param p_returnWorkflowInfo
     *            -- flag to indicate if return workflows info.
     * @param p_returnJobAttributeInfo
     *            -- flag to indicate if return job attributes info.
     * 
     * @return string in XML.
     */
    public String fetchJobsPerCompany(String p_accessToken, String[] p_jobIds,
            boolean p_returnSourcePageInfo, boolean p_returnWorkflowInfo,
            boolean p_returnJobAttributeInfo) throws WebServiceException
    {
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

        checkAccess(p_accessToken, "fetchJobsPerCompany");
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        ActivityLog.Start activityStart = null;
        StringBuffer xml = new StringBuffer(XML_HEAD);
        xml.append("<Jobs>\r\n");
        try
        {
            String loggedUserName = this.getUsernameFromSession(p_accessToken);
            User loggedUserObj = this.getUser(loggedUserName);
            String loggedComName = loggedUserObj.getCompanyName();
            TimeZone tz = null;
            try
            {
                tz = ServerProxy.getCalendarManager().findUserTimeZone(
                        loggedUserObj.getUserId());
            }
            catch (Exception e)
            {
            }

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUserName);
            activityArgs.put("jobNum", p_jobIds.length);
            List<String> jobList = Arrays.asList(p_jobIds);
            activityArgs.put("jobIds", jobList);
            activityArgs.put("returnSourcePageInfo", p_returnSourcePageInfo);
            activityArgs.put("returnWorkflowInfo", p_returnWorkflowInfo);
            activityArgs.put("returnJobAttributeInfo", p_returnJobAttributeInfo);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "fetchJobsPerCompany(p_accessToken, p_jobIds, p_returnSourcePageInfo, p_returnWorkflowInfo, p_returnJobAttributeInfo",
                            activityArgs);

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

                    String singleJobXml = handleSingleJob(job, tz,
                            p_returnSourcePageInfo, p_returnWorkflowInfo,
                            p_returnJobAttributeInfo);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
        xml.append("</Jobs>\r\n");

        return xml.toString();
    }

    /**
     * Generate XML string for single job, invoked by "fetchJobsPerCompany(..)"
     * method.
     * 
     * @param p_job
     *            -- job object.
     * @param p_tz
     *            -- current logged user's TimeZone.
     * @param p_returnSourcePageInfo
     *            -- flag to indicate if return source pages info.
     * @param p_returnWorkflowInfo
     *            -- flag to indicate if return workflows info.
     * @param p_returnJobAttributeInfo
     *            -- flag to indicate if return job attributes info.
     * 
     * @return string in XML.
     */
    private String handleSingleJob(Job p_job, TimeZone p_tz,
            boolean p_returnSourcePageInfo, boolean p_returnWorkflowInfo,
            boolean p_returnJobAttributeInfo)
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
            
			// Creator
			subXML.append("\t\t<creator>")
					.append(p_job.getCreateUser().getUserName())
					.append("</creator>\r\n");

            // Create date
            subXML.append("\t\t<createDate>")
                    .append(convertDateToString(p_job.getCreateDate(), p_tz))
                    .append("</createDate>\r\n");

            // Start date
            subXML.append("\t\t<startDate>")
                    .append(convertDateToString(p_job.getStartDate(), p_tz))
                    .append("</startDate>\r\n");

            // Completed date
            subXML.append("\t\t<completedDate>")
                    .append(convertDateToString(p_job.getCompletedDate(), p_tz))
                    .append("</completedDate>\r\n");

            // Localization profile
            L10nProfile lp = p_job.getL10nProfile();
            if (lp == null)
            {
                ServerProxy.getJobHandler()
                        .getL10nProfileByJobId(p_job.getId());
            }
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

			// group
			Long groupId =(Long)p_job.getGroupId();
			if (groupId != null)
			{
				JobGroup jobGroup = HibernateUtil.get(JobGroup.class, groupId);
				subXML.append("\t\t<group>\r\n");
				try
				{
					tmpXml = new StringBuilder();
					Project project = p_job.getProject();
					tmpXml.append("\t\t\t<groupId>").append(jobGroup.getId())
							.append("</groupId>\r\n");
					tmpXml.append("\t\t\t<groupName>")
							.append(jobGroup.getName())
							.append("</groupName>\r\n");

					subXML.append(tmpXml.toString());
				}
				catch (Exception e)
				{
				}
				subXML.append("\t\t</group>\r\n");
			}
            

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
			// numOfLanguages
			subXML.append("\t\t<numOfLanguages>")
					.append(p_job.getWorkflows().size())
					.append("</numOfLanguages>\r\n");
			String soureLocale = p_job.getSourceLocale().toString();
			Set<String> handledSafeBaseFiles = new HashSet<String>();
			String externalPageId = null;
			String eventFlowXml = null;
			int numPagesDocx = 0;
			int numPagesPptx = 0;
			List<SourcePage> sourcePages = (List<SourcePage>) p_job
					.getSourcePages();
			for (SourcePage sourcePage : sourcePages)
			{
				// m_externalPageId
				externalPageId = sourcePage.getExternalPageId();
				if (externalPageId.toLowerCase().endsWith("docx")
						|| externalPageId.toLowerCase().endsWith("pptx"))
				{
					eventFlowXml = sourcePage.getRequest().getEventFlowXml();
					String safeBaseFilename = jobHelper
							.getOffice2010SafeBaseFileName(eventFlowXml);
					if (StringUtil.isNotEmpty(safeBaseFilename)
							&& !handledSafeBaseFiles.contains(safeBaseFilename))
					{
						if (externalPageId.toLowerCase().endsWith("docx"))
						{
							numPagesDocx += jobHelper.getPageCount(
									safeBaseFilename, soureLocale, "docx");
						}
						else if (externalPageId.toLowerCase().endsWith("pptx"))
						{
							numPagesPptx += jobHelper.getPageCount(
									safeBaseFilename, soureLocale, "pptx");
						}
					}

					if (StringUtil.isNotEmpty(safeBaseFilename))
					{
						handledSafeBaseFiles.add(safeBaseFilename);
					}
				}
			}
			// pptxSlideNum
			if (numPagesPptx != 0)
			{
				subXML.append("\t\t<pptxSlideNum>").append(numPagesPptx)
						.append("</pptxSlideNum>\r\n");
			}
			// docxPageNum
			if (numPagesDocx != 0)
			{
				subXML.append("\t\t<docxPageNum>").append(numPagesDocx)
						.append("</docxPageNum>\r\n");
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
                String dueDateStr = convertDateToString(p_job.getDueDate(), p_tz);
                subXML.append("\t\t<dueDate>").append(dueDateStr)
                        .append("</dueDate>\r\n");
            }
            catch (Exception e)
            {
                subXML.append("\t\t<dueDate></dueDate>\r\n");
            }

            // Source pages
            if (p_returnSourcePageInfo)
            {
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
            }

            // workflows
            if (p_returnWorkflowInfo)
            {
                try
                {
                    tmpXml = new StringBuilder();
                    tmpXml.append("\t\t<workflows>\r\n");
                    Collection wfs = p_job.getWorkflows();
                    if (wfs != null && wfs.size() > 0)
					{
						Map<Long, String> workflowNameMap = AmbassadorHelper
								.getWorkflowName(p_job.getId());
						Iterator it = wfs.iterator();
						while (it.hasNext())
						{
							tmpXml.append("\t\t\t<workflow>\r\n");
							Workflow wf = (Workflow) it.next();
							tmpXml.append("\t\t\t\t<wfId>").append(wf.getId())
									.append("</wfId>\r\n");
							String workflowName = workflowNameMap.get(wf
									.getId());
							tmpXml.append("\t\t\t\t<workflowName>").append(workflowName)
							.append("</workflowName>\r\n");
							String targetLang = wf.getTargetLocale()
									.getLanguage()
									+ "_"
									+ wf.getTargetLocale().getCountry();
							tmpXml.append("\t\t\t\t<targetLang>")
									.append(targetLang)
									.append("</targetLang>\r\n");
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

            if (p_returnJobAttributeInfo)
            {
                try
                {
                    List<JobAttribute> jobAttributes = p_job.getAllJobAttributes();
                    Attributes allAttributes = new Attributes();
                    List<DateJobAttributeVo> dataVos = new ArrayList<DateJobAttributeVo>();
                    if (jobAttributes != null)
                    {
                        for (JobAttribute attribute : jobAttributes)
                        {
                            JobAttributeVo vo = AttributeUtil
                                    .getJobAttributeVo(attribute);
                            if ("date".equals(vo.getType()))
                            {
                                dataVos.add((DateJobAttributeVo) vo);
                            }
                            else
                            {
                                allAttributes.addAttribute(vo);
                            }
                        }
                    }
                    
                    String attrs = null;
                    if (allAttributes.getAttributes().size() > 0)
                    {
                        allAttributes.sort();
                        attrs = com.globalsight.cxe.util.XmlUtil.object2String(
                                allAttributes, true);
                    }

                    if (attrs != null && !"".equals(attrs.trim()))
                    {
                        String att = attrs.substring(attrs.indexOf(">") + 1).trim();
                        att = att.substring(0, att.lastIndexOf("</attributes>"));
                        subXML.append(att);
                        appendDateVos(subXML, dataVos, p_tz);
                        subXML.append("</attributes>\r\n");
                    }
                    else if (dataVos.size() > 0)
                    {
                        subXML.append("<attributes>\r\n");
                        appendDateVos(subXML, dataVos, p_tz);
                        subXML.append("</attributes>\r\n");
                    }
                }
                catch (Exception e)
                {
                }
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

    private void appendDateVos(StringBuffer subXML,
            List<DateJobAttributeVo> dataVos, TimeZone p_tz)
    {
        for (DateJobAttributeVo dateVo : dataVos)
        {
            subXML.append("    <attributes xsi:type=\"dateJobAttributeVo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n");
            subXML.append("        <displayName>").append(dateVo.getDisplayName()).append("</displayName>\r\n");
            subXML.append("        <fromSuperCompany>").append(dateVo.isFromSuperCompany()).append("</fromSuperCompany>\r\n");
            subXML.append("        <internalName>").append(dateVo.getInternalName()).append("</internalName>\r\n");
            subXML.append("        <required>").append(dateVo.isRequired()).append("</required>\r\n");
            subXML.append("        <type>").append(dateVo.getType()).append("</type>\r\n");
            subXML.append("        <value>").append(convertDateToString(dateVo.getValue(), p_tz)).append("</value>\r\n");
            subXML.append("    </attributes>\r\n");
        }
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

        ActivityLog.Start activityStart = null;
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<WorkflowInfo>\r\n");
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_workflowId, "workflowId");

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", getUsernameFromSession(p_accessToken));
            activityArgs.put("workflowId", p_workflowId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "fetchWorkflowRelevantInfo(p_accessToken, p_workflowId)",
                    activityArgs);
            
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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

        ActivityLog.Start activityStart = null;
        StringBuffer xml = new StringBuffer(XML_HEAD);
        xml.append("<jobs>\r\n");
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", getUsernameFromSession(p_accessToken));
            activityArgs.put("jobIds", jobIds);
            activityStart = ActivityLog.start(Ambassador.class,
                    "fetchWorkflowRelevantInfoByJobs(p_accessToken, jobIds)",
                    activityArgs);

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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;

        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobId", p_jobId);
            activityArgs.put("targetLocaleId", p_targetLocaleId);
            activityArgs.put("sourcePageId", p_sourcePageId);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "fetchFileForPreview(p_accessToken, p_jobId,p_targetLocaleId,p_sourcePageId)",
                            activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
			else if ("T".equalsIgnoreCase(p_commentObjectType.trim()))
			{
				workObject = ServerProxy.getTaskManager().getTask(jobOrTaskId);
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("commentObjectType", p_commentObjectType);
            activityArgs.put("jobOrTaskId", p_jobOrTaskId);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "getCommentFiles(p_accessToken, p_commentObjectType,p_jobOrTaskId)",
                            activityArgs);
            User user = ServerProxy.getUserManager().getUserByName(userName);
            companyName = user.getCompanyName();
            companyId = ServerProxy.getJobHandler().getCompany(companyName)
                    .getId();

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
                        commentMap.put(jobComment.getId(),
                                jobComment.getComment());
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
                    .append(p_commentObjectType.equalsIgnoreCase("J") ? "job" : "task")
                    .append("</ObjectType>\r\n");
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
                            StringBuffer cfUrl = new StringBuffer(
                                    AmbassadorUtil.getCapLoginOrPublicUrl());
                            cfUrl.append("/GlobalSight/CommentReference2/")
                                    .append(subFilePath);
                            if (companyName != null
                                    && companyName.trim().length() > 0)
                            {
                                cfUrl.append("?companyName=").append(
                                        companyName);
                            }
                            result.append("\t\t\t\t<CommentFileUrl>")
                                    .append(cfUrl.toString().replaceAll("\\\\",
                                            "/"))
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
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
        return null;
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
     * @param p_tmName
     *            Project TM name to import TMX files into.
     * @param p_contentsInBytes
     *            TMX file contents in byte[].
     * 
     * @throws WebServiceException
     */
    public String uploadTmxFile(String p_accessToken, String p_fileName,
            String p_tmName, byte[] p_contentsInBytes)
            throws WebServiceException
    {
        ProjectTM tm = null;
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_fileName, "file name");
            Assert.assertNotEmpty(p_tmName, "tm name");

            checkAccess(p_accessToken, "uploadTmxFile");
            checkPermission(p_accessToken,
                    Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            tm = ServerProxy.getProjectHandler().getProjectTMByName(p_tmName, false);
            if (tm == null) {
                return makeErrorXml("uploadTmxFile", "Project TM does not exist : " + p_tmName);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("fileName", p_fileName);
            activityArgs.put("tmName", p_tmName);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                            "uploadTmxFile(accessToken, fileName, tmName, contentsInBytes)",
                            activityArgs);

            StringBuffer fsRoot = new StringBuffer(
                    AmbFileStoragePathUtils.getFileStorageDirPath(tm
                            .getCompanyId()));
            fsRoot = fsRoot.append(File.separator)
                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                    .append(File.separator).append("TmImport")
                    .append(File.separator).append(p_tmName.trim())
                    .append(File.separator).append("tmp")
                    .append(File.separator).append(p_fileName);

            writeFileToLocale(fsRoot.toString(), p_contentsInBytes);

            return null;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return makeErrorXml("uploadTmxFile", e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ProjectTM tm = null;
        try
        {
            Assert.assertNotEmpty(p_accessToken, "access token");
            Assert.assertNotEmpty(p_tmName, "tm name");

            checkAccess(p_accessToken, "importTmxFile");
            checkPermission(p_accessToken,
                    Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            tm = ServerProxy.getProjectHandler().getProjectTMByName(p_tmName,
                    false);
            if (tm == null) {
                throw new WebServiceException("Project TM does not exist : "
                        + p_tmName);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        /** importOptions */
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("tmName", p_tmName);
            activityArgs.put("syncMode", p_syncMode);
            activityStart = ActivityLog.start(Ambassador.class,
                    "importTmxFile(p_accessToken, p_tmName,p_syncMode)",
                    activityArgs);

            com.globalsight.everest.tm.importer.ImportOptions tmImportOptions = new com.globalsight.everest.tm.importer.ImportOptions();
            // syncMode : default "merge"
            tmImportOptions.setSyncMode(ImportOptions.SYNC_MERGE);
            if (ImportOptions.SYNC_MERGE.equalsIgnoreCase(p_syncMode)
                    || ImportOptions.SYNC_OVERWRITE.equalsIgnoreCase(p_syncMode)
                    || ImportOptions.SYNC_DISCARD.equalsIgnoreCase(p_syncMode))
            {
                tmImportOptions.setSyncMode(p_syncMode.toLowerCase());
            }
            // default: all -- all
            tmImportOptions.setSelectedSource("all");
            Collection selectedTargets = new ArrayList();
            selectedTargets.add("all");
            tmImportOptions.setSelectedTargets(selectedTargets);
            /** importer */
            IImportManager importer = TmManagerLocal.getProjectTmImporter(p_tmName);
            /** import tmx files one by one */
            StringBuffer fsRoot = new StringBuffer(
                    AmbFileStoragePathUtils.getFileStorageDirPath(tm
                            .getCompanyId()));
            fsRoot = fsRoot.append(File.separator)
                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                    .append(File.separator).append("TmImport")
                    .append(File.separator).append(p_tmName.trim());
            // saved tmx file directory
            String savedTmxFilePath = fsRoot.toString();
            // tmp tmx file directory
            String tmpTmxFilePath = fsRoot.append(File.separator)
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

                        ImportUtil.createInstance().saveTmFileWithValidation(tmxFile,savedFile);

                        importer.setImportOptions(tmImportOptions.getXml());
                        importer.setImportFile(savedFile.getAbsolutePath(), false);
                        String options = importer.analyzeFile();

                        importer.setImportOptions(options);
                        importer.doImport();

                        // delete tmp TMX files to avoid re-import.
                        tmxFile.delete();
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String msg = makeErrorXml("importTmxFile", e.getMessage());
            throw new WebServiceException(msg);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
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

        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("workflowId", p_workflowId);
            activityArgs.put("activity", p_activity);
            activityStart = ActivityLog
                    .start(Ambassador.class,
                    "jobsSkipActivity(p_accessToken, p_workflowId,p_activity)",
                            activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
        return null;
    }

    /**
     * Reassign task to other translators
     *  
     * @param p_accessToken
     *            String Access token
     * @param p_taskId
     *            String ID of task
     *            Example: "10"
     * @param p_users
     *            String[] Users' information who will be reassigned to. The element in the array is [{userid}].
     *            Example: ["qaadmin", "qauser"]
     * @return 
     *            Return null if the reassignment executes successfully.
     * @throws WebServiceException
     */
    public String taskReassign(String p_accessToken, String p_taskId,
            String[] p_users) throws WebServiceException
    {
        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.taskReassign(p_accessToken, p_taskId, p_users);
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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobId", p_jobId);
            activityArgs.put("wfInfos", p_wfInfos);
            activityStart = ActivityLog.start(Ambassador.class,
                    "jobsAddLanguages(p_accessToken, p_jobId,p_wfInfos)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
        ActivityLog.Start activityStart = null;
        String userName = this.getUsernameFromSession(p_accessToken);
        Map<Object, Object> activityArgs = new HashMap<Object, Object>();
        activityArgs.put("loggedUserName", userName);
        activityArgs.put("jobId", p_jobId);
        activityStart = ActivityLog.start(Ambassador.class,
                "jobsWorkflowCanBeAdded(p_accessToken, p_jobId)", activityArgs);
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
        if (activityStart != null)
        {
            activityStart.end();
        }
        return returnMsg.toString();
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
			User user = ServerProxy.getUserManager().getUserByName(
					getUsernameFromSession(p_accessToken));
			PermissionSet ps = Permission.getPermissionManager()
					.getPermissionSetForUser(user.getUserId());

			if (!ps.getPermissionFor(Permission.JOB_WORKFLOWS_DISPATCH)
					&& !ps.getPermissionFor(Permission.JOBS_DISPATCH))
			{
				String msg = "User " + user.getUserName()
						+ " does not have enough permission";
				return makeErrorXml("dispatchWorkflow", msg);
			}
			Assert.assertNotEmpty(p_accessToken, "Access token");
			Assert.assertNotEmpty(p_wfIds, "Workflow IDs");
		}
		catch (Exception e)
		{
			return makeErrorXml("dispatchWorkflow", e.getMessage());
		}

		long wfId = 0l;
		ArrayList<Long> wfIdsArray = new ArrayList<Long>();
		String[] wfIds = null;
		Workflow wf = null;
		String wfIdString = "";

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
				return makeErrorXml("dispatchWorkflow", "Invaild workflow id: "
						+ wfIdString + ",non-numeric chars.");
			}
		}
		ActivityLog.Start activityStart = null;
		try
		{
			String userName = this.getUsernameFromSession(p_accessToken);
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", userName);
			activityArgs.put("wfIds", p_wfIds);
			activityStart = ActivityLog.start(Ambassador.class,
					"dispatchWorkflow(p_accessToken, p_wfIds)", activityArgs);
			WorkflowManagerWLRemote wfm = ServerProxy.getWorkflowManager();
			String projectId = null;
			for (int i = 0; i < wfIdsArray.size(); i++)
			{
				wfId = wfIdsArray.get(i).longValue();
				try
				{
					wf = wfm.getWorkflowById(wfId);
					if (wf != null)
					{
						projectId = String.valueOf(wf.getJob().getProjectId());
						if (UserUtil.isInProject(userName, projectId))
						{
							wfm.dispatch(wf);
						}
						else
						{
							return makeErrorXml("dispatchWorkflow",
									"Invaild workflow id: " + wfId
											+ " for current user.");
						}
					}
					else
					{
						return makeErrorXml("dispatchWorkflow",
								"Invaild workflow id: " + wfId
										+ ",does not exist.");
					}
				}
				catch (WorkflowManagerException wfe)
				{
					logger.error(wfe.getMessage(), wfe);
					return makeErrorXml("dispatchWorkflow", wfe.getMessage());
				}
			}

			return null;
		}
		catch (Exception e)
		{
			message = makeErrorXml("dispatchWorkflow", e.getMessage());
			throw new WebServiceException(message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}

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
        ActivityLog.Start activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("startTime", startTime);
            activityArgs.put("projectId", projectId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getJobsByTimeRange(p_accessToken,startTime, projectId)",
                    activityArgs);
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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
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
			// job creator
			xml.append("\t\t<creator>")
					.append(job.getCreateUser().getUserName())
					.append("</creator>\r\n");
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
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("workflowId", workflowId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getWorkflowPath(p_accessToken, workflowId)", activityArgs);
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
            if (activityStart != null)
            {
                activityStart.end();
            }
            ctx.close();
        }

        xml.append("</workflow>");
        return xml.toString();
    }

    /**
     * Download offline file with xliff format.
     * 
     * Note: after 8.5.8 version, "getWorkOfflineFiles()" API can download all
     * supported offline formats, include xliff.
     * 
     * @param accessToken
     *            Access token
     * @param taskId
     *            Task ID
     * @return String file name of generated offline file, it typical is package file.
     * @throws WebServiceException
     * @throws RemoteException
     * @throws NamingException
     */
    public String downloadXliffOfflineFile(String accessToken, String taskId)
            throws RemoteException, WebServiceException, NamingException
    {
        String lockedSegEditType = "1";
        boolean isIncludeXmlNodeContextInformation = false;
        return downloadXliffOfflineFile(accessToken, taskId, lockedSegEditType,
        								isIncludeXmlNodeContextInformation);
    }

    /**
     * Download offline file with xliff format.
     * 
     * Note: after 8.5.8 version, "getWorkOfflineFiles()" API can download all
     * supported offline formats, include xliff.
     * 
     * @param accessToken
     *            Access token
     * @param taskId
     *            Task ID
     * @param lockedSegEditType param for "Allow Edit Locked Segments" option when offline download.
     *         Available values: 1, 2, 3, 4
     *         1: Allow Edit of ICE and 100% matches
     *         2: Allow Edit of ICE matches
     *         3: Allow Edit of 100% matches
     *         4: Deny Edit
     * @return String file name of generated offline file, it typical is package file.
     * @throws WebServiceException
     * @throws RemoteException
     * @throws NamingException
     */
    public String downloadXliffOfflineFile(String accessToken, String taskId,
            String lockedSegEditType, boolean isIncludeXmlNodeContextInformation)
    		throws WebServiceException, RemoteException, NamingException
    {
        Set<String> availableValues = new HashSet<String>();
        availableValues.add("1");
        availableValues.add("2");
        availableValues.add("3");
        availableValues.add("4");
        if (lockedSegEditType == null
                || !availableValues.contains(lockedSegEditType.trim()))
        {
            lockedSegEditType = "1";
        }

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
        String userName = getUsernameFromSession(accessToken);
        User user = this.getUser(userName);
        if (task == null || !user.getUserId().equals(task.getAcceptor()))
            return makeErrorXml("downloadXliffOfflineFile",
                    "Current user is not the acceptor of this task.");

        long jobId = task.getJobId();
        Job job = ServerProxy.getJobHandler().getJobById(jobId);
        ActivityLog.Start activityStart = null;
        WorkflowManagerLocal workflowManager = new WorkflowManagerLocal();
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("taskId", taskId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "downloadXliffOfflineFile(p_accessToken, taskId)",
                    activityArgs);

            // Generate offline page data to file
            File zipFile = workflowManager
                    .downloadOfflineFiles(task, job, null, lockedSegEditType,
                    		isIncludeXmlNodeContextInformation);

            // Copy "zipFile" from
            // "FStorage\[companyName]\GlobalSight\CustomerDownload" folder to
            // "DOCS\[companyName]\workOfflineDownload" folder. This is
            // unnecessary, but we will not change it for now.
            String filename = job.getJobName() + "_" + task.getSourceLocale()
                    + "_" + task.getTargetLocale() + ".zip";
            File targetFile = new File(
                    AmbFileStoragePathUtils.getCxeDocDirPath() + File.separator
                            + AmbFileStoragePathUtils.OFFLINE_FILE_DOWNLOAD_DIR
                            + File.separator + filename);
            FileUtil.copyFile(zipFile, targetFile);

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
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
        return returnXml.toString();
    }
    
    private String checkIllegalJobIds(List<Long> p_jobIdList, String p_userId)
    {
    	String illegalJobIds = "";
    	try 
    	{
    		for(Long jobId: p_jobIdList)
    		{
    			Job job = ServerProxy.getJobHandler().getJobById(jobId);
    			if(job == null || !job.getProject().getUserIds().contains(p_userId))
    			{
    				illegalJobIds = illegalJobIds + "," + jobId;
    			}
    		}
		} 
    	catch (Exception e) 
		{
    		logger.error("Error", e);
		}
    	if(illegalJobIds.length() > 0)
    	{
    		illegalJobIds = illegalJobIds.substring(1);
    	}
    	return illegalJobIds;
    }
    
    private String checkJobLocaleMatch(List<Long> jobIdList, 
    		List<GlobalSightLocale> targetLocalList)
    {
    	String notMatchJobIds = "";
		try 
		{
			for (Long jobId : jobIdList) 
			{
				Job job = ServerProxy.getJobHandler().getJobById(jobId);
				boolean isMatch = false;
				for(Workflow workflow: job.getWorkflows())
				{
					if(targetLocalList.contains(workflow.getTargetLocale()))
					{
						isMatch = true;
						break;
					}
				}
				if(!isMatch)
				{
					notMatchJobIds = notMatchJobIds + "," + jobId;
				}
			}
		} 
		catch (Exception e) 
		{
			logger.error("Error", e);
		}
		if(notMatchJobIds.length() > 0)
    	{
			notMatchJobIds = notMatchJobIds.substring(1);
    	}
    	return notMatchJobIds;
    }
    
    private String getReportsUrl(File[] files) throws FileNotFoundException, IOException
    {
    	String returnString = "";
    	if(files.length == 1)
		{
			File file = files[0];
			String superFSDir = AmbFileStoragePathUtils
						.getFileStorageDirPath(1).replace("\\", "/");
			String fullPathName = file.getAbsolutePath().replace("\\", "/");
			String path = fullPathName.substring(fullPathName.indexOf(superFSDir)
						+ superFSDir.length());
			path = path.substring(path.indexOf("/Reports/")
						+ "/Reports/".length());
			String root = AmbassadorUtil.getCapLoginOrPublicUrl()
						+ "/DownloadReports";
			returnString = root + "/" + path;
		}
		else if(files.length > 1)
		{
			Date date = new Date();
			String fullPathName = files[0].getAbsolutePath().replace("\\", "/");
            String zipFileName = fullPathName.substring(0, fullPathName.lastIndexOf("/") + 1)
            				+ ReportConstants.REPORTS_NAME + date.getTime() + ".zip";
            File zipFile = new File(zipFileName);
            ZipIt.addEntriesToZipFile(zipFile, files, true, "");
            
            String superFSDir = AmbFileStoragePathUtils
						.getFileStorageDirPath(1).replace("\\", "/");
			String path = zipFileName.substring(zipFileName.indexOf(superFSDir)
						+ superFSDir.length());
			path = path.substring(path.indexOf("/Reports/")
						+ "/Reports/".length());
			String root = AmbassadorUtil.getCapLoginOrPublicUrl()
						+ "/DownloadReports";
			returnString = root + "/" + path;
		}
    	return returnString;
    }
    
    /**
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_jobId
     *            -- job ID to get report.
     * @param p_targetLocale
     *            -- target locale. eg "zh_CN"(case insensitive).
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/TranslationsEditReport/20140219/TranslationsEditReport-(jobname_492637643)(337)-en_US_zh_CN-20140218_162543.xlsx";
     * @throws WebServiceException
     */
    public String generateTranslationEditReport(String p_accessToken, 
    		 String p_jobId, String p_targetLocale) throws WebServiceException
    {
    	checkAccess(p_accessToken, GENERATE_TRANSLATION_EDIT_REPORT);
		String returnString = "";
		try 
		{
			//get and check job ids
			Long jobId = Long.valueOf(p_jobId);
			List<Long> jobIdList = new ArrayList<Long>();
			jobIdList.add(jobId);
			String userId = UserUtil.getUserIdByName(getUsernameFromSession(p_accessToken));
			String illegalJobIds = checkIllegalJobIds(jobIdList, userId);
			if(illegalJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_TRANSLATION_EDIT_REPORT,
						"Error info: illegal job id " + illegalJobIds + " for the login user");
			}
			//get target locales
			List<GlobalSightLocale> targetLocalList = new ArrayList<GlobalSightLocale>();
			targetLocalList.add(getLocaleByName(p_targetLocale));
			//get report
			Job job = ServerProxy.getJobHandler().getJobById(jobId);
			TranslationsEditReportGenerator generator = new TranslationsEditReportGenerator(
					CompanyWrapper.getCompanyNameById(job.getCompanyId()), userId);
			File[] files = generator.generateReports(jobIdList, targetLocalList);
			returnString = getReportsUrl(files);
		} 
		catch (Exception e) 
		{
			logger.error("Error found in generateTranslationEditReport.", e);
            return makeErrorXml(GENERATE_TRANSLATION_EDIT_REPORT,
                    "Error info: " + e.toString());
		}

		return returnString;
    }
    
    /**
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_jobId
     *            -- job ID to get report.
     * @param p_targetLocale
     *            -- target locale. eg "zh_CN"(case insensitive).
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/PostReviewQAReport/20140219/PRR-(jobname_492637643)(337)-en_US_zh_CN-20140218_162543.xlsx";
     * @throws WebServiceException
     */
    public String generatePostReviewQAReport(String p_accessToken, 
            String p_jobId, String p_targetLocale) throws WebServiceException
    {
        checkAccess(p_accessToken, GENERATE_POST_REVIEW_QA_REPORT);
        String returnString = "";
        try 
        {
            //get and check job ids
            Long jobId = Long.valueOf(p_jobId);
            List<Long> jobIdList = new ArrayList<Long>();
            jobIdList.add(jobId);
            String userId = UserUtil.getUserIdByName(getUsernameFromSession(p_accessToken));
            String illegalJobIds = checkIllegalJobIds(jobIdList, userId);
            if(illegalJobIds.length() > 0)
            {
                return makeErrorXml(GENERATE_POST_REVIEW_QA_REPORT,
                        "Error info: illegal job id " + illegalJobIds + " for the login user");
            }
            //get target locales
            List<GlobalSightLocale> targetLocalList = new ArrayList<GlobalSightLocale>();
            targetLocalList.add(getLocaleByName(p_targetLocale));
            //get report
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            PostReviewQAReportGenerator generator = new PostReviewQAReportGenerator(
                    CompanyWrapper.getCompanyNameById(job.getCompanyId()), userId);
            File[] files = generator.generateReports(jobIdList, targetLocalList);
            returnString = getReportsUrl(files);
        } 
        catch (Exception e) 
        {
            logger.error("Error found in generatePostReviewQAReport.", e);
            return makeErrorXml(GENERATE_POST_REVIEW_QA_REPORT,
                    "Error info: " + e.toString());
        }
        
        return returnString;
    }

    /**
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_jobIds
     *            -- job ids.eg "11,13,45".
     * @param p_targetLocales
     *            -- target locales. eg "fr_FR,zh_CN"(case insensitive).
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/CharacterCountReport/20140219/CharacterCountReport-(jobname_492637643)(337)-en_US_zh_CN-20140218_162543.xlsx"
     *         or
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/CharacterCountReport/20140219/GSReports1416985676460.zip".
     * @throws WebServiceException
     */
    public String generateCharacterCountReport(String p_accessToken, 
    		String p_jobIds, String p_targetLocales) throws WebServiceException
    {
    	checkAccess(p_accessToken, GENERATE_CHARACTER_COUNT_REPORT);
		String returnString = "";
		try 
		{
			//get and check job ids
			List<Long> jobIdList = new ArrayList<Long>();
			for(String jobId: p_jobIds.split(","))
			{
				jobIdList.add(Long.valueOf(jobId));
			}
			String userId = UserUtil.getUserIdByName(getUsernameFromSession(p_accessToken));
			String illegalJobIds = checkIllegalJobIds(jobIdList,userId);
			if(illegalJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_CHARACTER_COUNT_REPORT,
						"Error info: illegal job id " + illegalJobIds + " for the login user");
			}
			//get target locales
			List<GlobalSightLocale> targetLocalList = new ArrayList<GlobalSightLocale>();
			for(String targetLocale: p_targetLocales.split(","))
			{
				targetLocalList.add(getLocaleByName(targetLocale));
			}
			//get report
			Job job = ServerProxy.getJobHandler().getJobById(jobIdList.get(0));
			CharacterCountReportGenerator generator = new CharacterCountReportGenerator(
					CompanyWrapper.getCompanyNameById(job.getCompanyId()), userId);
			String notMatchJobIds = checkJobLocaleMatch(jobIdList,targetLocalList);
			if(notMatchJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_CHARACTER_COUNT_REPORT,
						"Error info: the given job id: " + notMatchJobIds + " have no workflow match the given target locales.");
			}
			File[] files = generator.generateReports(jobIdList, targetLocalList);
			returnString = getReportsUrl(files);
		} 
		catch (Exception e) 
		{
			logger.error("Error found in generateCharacterCountReport.", e);
            return makeErrorXml(GENERATE_CHARACTER_COUNT_REPORT,
                    "Error info: " + e.toString());
		}

		return returnString;
    }
    
    /**
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_jobIds
     *            -- job ids.eg "11,13,45".
     * @param p_targetLocales
     *            -- target locales. eg "fr_FR,zh_CN"(case insensitive).
     * @param p_includeCompactTags
     * 
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/ReviewersCommentReport/20140219/CharacterCountReport-(jobname_492637643)(337)-en_US_zh_CN-20140218_162543.xlsx"
     *         or "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/ReviewersCommentReport/20140219/GSReports1416985676460.zip";
     * @throws WebServiceException
     */
    public String generateReviewersCommentReport(String p_accessToken, String p_jobIds, 
    		String p_targetLocales, boolean p_includeCompactTags) throws WebServiceException
    {
    	checkAccess(p_accessToken, GENERATE_REVIEWERS_COMMENT_REPORT);
		String returnString = "";
		try 
		{
			//get and check job ids
			List<Long> jobIdList = new ArrayList<Long>();
			for(String jobId: p_jobIds.split(","))
			{
				jobIdList.add(Long.valueOf(jobId));
			}
			String userId = UserUtil.getUserIdByName(getUsernameFromSession(p_accessToken));
			String illegalJobIds = checkIllegalJobIds(jobIdList,userId);
			if(illegalJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_REVIEWERS_COMMENT_REPORT,
						"Error info: illegal job id " + illegalJobIds + " for the login user");
			}
			//get target locales
			List<GlobalSightLocale> targetLocalList = new ArrayList<GlobalSightLocale>();
			for(String targetLocale: p_targetLocales.split(","))
			{
				targetLocalList.add(getLocaleByName(targetLocale));
			}
			//get report
			Job job = ServerProxy.getJobHandler().getJobById(jobIdList.get(0));
			ReviewersCommentsReportGenerator generator = new ReviewersCommentsReportGenerator(
					CompanyWrapper.getCompanyNameById(job.getCompanyId()),p_includeCompactTags, userId);
			String notMatchJobIds = checkJobLocaleMatch(jobIdList,targetLocalList);
			if(notMatchJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_REVIEWERS_COMMENT_REPORT,
						"Error info: the given job id: " + notMatchJobIds + " have no workflow for the given target locales.");
			}
			File[] files = generator.generateReports(jobIdList, targetLocalList);
			returnString = getReportsUrl(files);
		}
		catch (Exception e) 
		{
			logger.error("Error found in generateReviewersCommentReport.", e);
            return makeErrorXml(GENERATE_REVIEWERS_COMMENT_REPORT,
                    "Error info: " + e.toString());
		}

		return returnString;
    }
    
    /**
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_jobIds
     *            -- job ids.eg "11,13,45".
     * @param p_targetLocales
     *            -- target locales. eg "en_US,zh_CN"
     * @param p_includeCompactTags
     *            
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/ReviewersCommentSimplifiedReport/20140219/CharacterCountReport-(jobname_492637643)(337)-en_US_zh_CN-20140218_162543.xlsx"
     *         or "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/ReviewersCommentSimplifiedReport/20140219/GSReports1416985676460.zip";
     * @throws WebServiceException
     */
    public String generateReviewersCommentSimplifiedReport(String p_accessToken, String p_jobIds, 
    		String p_targetLocales, boolean p_includeCompactTags) throws WebServiceException 
    {
    	checkAccess(p_accessToken, GENERATE_REVIEWERS_COMMENT_SIMPLIFIED_REPORT);
		String returnString = "";
		try 
		{
			//get and check job ids
			List<Long> jobIdList = new ArrayList<Long>();
			for(String jobId: p_jobIds.split(","))
			{
				jobIdList.add(Long.valueOf(jobId));
			}
			String userId = UserUtil.getUserIdByName(getUsernameFromSession(p_accessToken));
			String illegalJobIds = checkIllegalJobIds(jobIdList,userId);
			if(illegalJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_REVIEWERS_COMMENT_SIMPLIFIED_REPORT,
						"Error info: illegal job id " + illegalJobIds + " for the login user");
			}
			//get target locales
			List<GlobalSightLocale> targetLocalList = new ArrayList<GlobalSightLocale>();
			for(String targetLocale: p_targetLocales.split(","))
			{
				targetLocalList.add(getLocaleByName(targetLocale));
			}
			//get report
			Job job = ServerProxy.getJobHandler().getJobById(jobIdList.get(0));
			ReviewersCommentsSimpleReportGenerator generator = new ReviewersCommentsSimpleReportGenerator(
					CompanyWrapper.getCompanyNameById(job.getCompanyId()),p_includeCompactTags, userId);
			String notMatchJobIds = checkJobLocaleMatch(jobIdList,targetLocalList);
			if(notMatchJobIds.length() > 0)
			{
				return makeErrorXml(GENERATE_REVIEWERS_COMMENT_SIMPLIFIED_REPORT,
						"Error info: the given job id: " + notMatchJobIds + " have no workflow for the given target locales.");
			}
			File[] files = generator.generateReports(jobIdList, targetLocalList);
			returnString = getReportsUrl(files);
		} 
		catch (Exception e) 
		{
			logger.error("Error found in generateReviewersCommentSimplifiedReport.", e);
            return makeErrorXml(GENERATE_REVIEWERS_COMMENT_SIMPLIFIED_REPORT,
                    "Error info: " + e.toString());
		}

		return returnString;
    }

    /**
     * Gets DITA QA Checks report api.
     * 
     * @param p_accessToken
     *            -- login user's token.
     * @param p_taskId
     *            -- task ID to get QA report.
     * @return -- XML string.
     *  -- If fail, it will return an xml string to tell error message;
     *  -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/$$companyName$$/GlobalSight/Reports/DITAQAChecksReport/914/zh_CN/ditaTranslation1_6315/DITAQAChecksReport-Job Name-zh_CN-20141212 125403.xlsx".
     */
    public String generateDITAQAReport(String p_accessToken, String p_taskId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GENERATE_DITA_QA_REPORT);
        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(Long.parseLong(p_taskId));
        }
        catch (Exception e)
        {
            logger.warn("Can not get task info by taskId " + p_taskId);
        }
        if (task == null)
        {
            return makeErrorXml(GENERATE_DITA_QA_REPORT,
                    "Can not find task by taskId " + p_taskId);
        }

        Company logUserCompany = getCompanyInfo(getUsernameFromSession(p_accessToken));
        if (logUserCompany.getId() != 1
                && logUserCompany.getId() != task.getCompanyId())
        {
            return makeErrorXml(GENERATE_DITA_QA_REPORT,
                    "Current user not super user or does not belong to company of this task: "
                            + p_taskId);
        }

        try
        {
            File reportFile = DITAQACheckerHelper.getDitaReportFile((TaskImpl) task);
            String superFs = AmbFileStoragePathUtils.getFileStorageDirPath(1);
            String reportFilePath = reportFile.getAbsolutePath().replace("\\", "/");
            reportFilePath = reportFilePath.substring(superFs.length() + 1);

            StringBuffer root = new StringBuffer();
            root.append(AmbassadorUtil.getCapLoginOrPublicUrl());
            root.append("/DownloadReports");
            root.append("/").append(reportFilePath);
            return root.toString();
        }
        catch (Exception e)
        {
            return makeErrorXml(GENERATE_DITA_QA_REPORT,
                    "Fail to generate report for taskId: " + p_taskId + " " + e.getMessage());
        }
    }

    /**
     * Gets QA Checks report api.
     * 
     * @param p_accessToken
     *            -- login user's token.
     * @param p_taskId
     *            -- task ID to get QA report.
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/QAChecksReport/1036/de_DE/GSPM1_375/QAChecksReport_a_413186725_GSPM1-de_DE.xlsx".
     *         
     * @throws WebServiceException
     */
    public String generateQAChecksReport(String p_accessToken, String p_taskId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GENERATE_QA_CHECKS_REPORT);

        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(
                    Long.parseLong(p_taskId));
        }
        catch (Exception e)
        {
            logger.warn("Can not get task info by taskId " + p_taskId);
        }

        if (task == null)
        {
            return makeErrorXml(GENERATE_QA_CHECKS_REPORT,
                    "Can not find task by taskId " + p_taskId);
        }

        String userName = getUsernameFromSession(p_accessToken);
        Company logUserCompany = getCompanyInfo(userName);
        if (logUserCompany.getId() != 1
                && logUserCompany.getId() != task.getCompanyId())
        {
            return makeErrorXml(
                    GENERATE_QA_CHECKS_REPORT,
                    "Current user is not super user or does not belong to the company of this task: "
                            + p_taskId);
        }

        String returning = "";
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("taskId", p_taskId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "generateQAChecksReport(p_accessToken, p_taskId)",
                    activityArgs);

            String fileUrl = null;

            QAChecker checker = new QAChecker();
            checker.runQAChecksAndGenerateReport(Long.parseLong(p_taskId));
            File qaReport = QACheckerHelper.getQAReportFile(task);

            String filestore = AmbFileStoragePathUtils.getFileStorageDirPath(
                    task.getCompanyId()).replace("\\", "/");
            String fullPathName = qaReport.getAbsolutePath().replace("\\", "/");
            String path = fullPathName.substring(fullPathName
                    .indexOf(filestore) + filestore.length());
            path = path.substring(path.indexOf("/Reports/")
                    + "/Reports/".length());
            String root = AmbassadorUtil.getCapLoginOrPublicUrl()
                    + "/DownloadReports";
            fileUrl = root + "/" + path;

            returning = fileUrl;
        }
        catch (Exception e)
        {
            logger.error(e);
            String message = "An error occurred while generating QA Checks report.";
            return makeErrorXml(GENERATE_QA_CHECKS_REPORT, message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return returning;
    }
    
	/**
	 * @param p_accessToken
	 *            -- login user's token
	 * @param jobIds
	 *            -- Job id can not be empty.It may be one or more.
	 *            Example :"206" or "206,207,208"
	 * @param workflowIds
	 *            -- Workflow id can be empty.It may be one or more.
	 *            Example : "1310" or "1310,1311,1312"
	 * @return XML string.
	 *            -- If fail, it will return null; 
	 * 			   -- If succeed, report returning is like
	 *         "http://10.10.215.110:8080/globalsight/DownloadTM/test/GlobalSight/Reports/QAChecksReport/apiDownload/QAChecksReport_(196).zip"
	 */
	public String downloadQAChecksReports(String p_accessToken, String jobIds,
			String workflowIds) throws WebServiceException
	{
		String returnFilePath = null;
		try
		{
			String[] jobIdArr = null;
			String[] workflowIdArr = null;
			Assert.assertNotEmpty(p_accessToken, "Access token");
			Assert.assertNotEmpty(jobIds, "Job id");
			if (StringUtils.isNotBlank(jobIds))
			{
				jobIdArr = jobIds.split(",");
				for (String id : jobIdArr)
				{
					Assert.assertIsInteger(id);
				}
			}
			if (StringUtils.isNotBlank(workflowIds))
			{
				workflowIdArr = workflowIds.split(",");
				for (String id : workflowIdArr)
				{
					Assert.assertIsInteger(id);
				}
			}

			Company logUserCompany = getCompanyInfo(getUsernameFromSession(p_accessToken));
			if (logUserCompany.getId() != 1)
			{
				for (String id : jobIdArr)
				{
					Job job = ServerProxy.getJobHandler().getJobById(
							Long.parseLong(id));
					if (job != null)
					{
						if (job.getCompanyId() != logUserCompany.getId())
						{
							return makeErrorXml(DOWNLOAD_QA_CHECKS_REPORTS,
									"Current user not super user or does not belong to company of this job id: "
											+ id);
						}
					}
					else
					{
						return makeErrorXml(DOWNLOAD_QA_CHECKS_REPORTS,
								"Invalid job id: " + id);
					}
				}
			}

			Set<Long> workflowIdSet = new HashSet<Long>();
			if (workflowIdArr != null)
			{
				for (String id : workflowIdArr)
				{
					workflowIdSet.add(Long.parseLong(id));
				}
			}

			Set<Long> jobIdSet = new HashSet<Long>();
			Set<Workflow> workflowSet = new HashSet<Workflow>();
			Set<Long> companyIdSet = new HashSet<Long>();
			for (String jobId : jobIdArr)
			{
				Job job = ServerProxy.getJobHandler().getJobById(
						Long.parseLong(jobId));
				companyIdSet.add(job.getCompanyId());
				workflowSet.addAll(job.getWorkflows());
				jobIdSet.add(Long.parseLong(jobId));
			}

			Map<Long, Set<File>> exportFilesMap = new HashMap<Long, Set<File>>();
			Set<String> locales = new HashSet<String>();
			for (Long companyId : companyIdSet)
			{
				Set<File> exportFilesList = new HashSet<File>();
				Company company = CompanyWrapper.getCompanyById(companyId);
				CompanyThreadLocal.getInstance().setValue(
						company.getCompanyName());
				if (company.getEnableQAChecks())
				{
					for (Workflow workflow : workflowSet)
					{
						if (workflow.getCompanyId() == companyId)
						{
							String filePath = null;
							if (workflowIdSet != null
									&& workflowIdSet.size() > 0)
							{
								if (workflowIdSet.contains(workflow.getId()))
								{
									locales.add(workflow.getTargetLocale()
											.getLocaleCode());
									filePath = WorkflowHandlerHelper
											.getExportFilePath(workflow);
									if (filePath != null)
									{
										exportFilesList.add(new File(filePath));
									}
									continue;
								}
							}
							else
							{
								locales.add(workflow.getTargetLocale()
										.getLocaleCode());
								filePath = WorkflowHandlerHelper
										.getExportFilePath(workflow);
								if (filePath != null)
								{
									exportFilesList.add(new File(filePath));
								}
							}
						}

					}
					exportFilesMap.put(companyId, exportFilesList);
				}
				else
				{
					return makeErrorXml(DOWNLOAD_QA_CHECKS_REPORTS,
							"Current log user no download QA report permissions");
				}
			}

			// zipped Folder
			returnFilePath = zippedFolder(jobIdSet, exportFilesMap, locales);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new WebServiceException(e.getMessage());
		}
		return returnFilePath;
	}

	private String zippedFolder(Set<Long> jobIdSet,
			Map<Long, Set<File>> exportFilesMap, Set<String> locales)
	{
		String fileUrl = null;
		String directory = AmbFileStoragePathUtils.getFileStorageDirPath(1)
				.replace("\\", "/")
				+ File.separator
				+ "Reports"
				+ File.separator + "apiQACheckDownload";
		new File(directory).mkdirs();

		String downloadFileName = null;
		if (jobIdSet != null && jobIdSet.size() == 1)
		{
			Long jobId = jobIdSet.iterator().next();
			downloadFileName = ReportConstants.REPORT_QA_CHECKS_REPORT + "_("
					+ jobId + ").zip";
		}
		else if (jobIdSet != null && jobIdSet.size() > 1)
		{
			String tempS = jobIdSet.toString();
			tempS = tempS.replace(" ", "");
			String jobNamesstr = tempS.substring(1, tempS.length() - 1);
			downloadFileName = ReportConstants.REPORT_QA_CHECKS_REPORT + "_("
					+ jobNamesstr + ").zip";
		}
		String zipFileName = directory + File.separator + downloadFileName;
		File zipFile = new File(zipFileName);

		Map<File, String> allEntryFileToFileNameMap = new HashMap<File, String>();
		Set<Long> keySet = exportFilesMap.keySet();
		for (Long companyId : keySet)
		{
			Set<File> exportListFiles = exportFilesMap.get(companyId);
			Map<File, String> entryFileToFileNameMap = WorkflowHandlerHelper
					.getEntryFileToFileNameMap(exportListFiles, jobIdSet,
							locales,
							AmbFileStoragePathUtils.getReportsDir(companyId)
									.getPath()
									+ File.separator
									+ ReportConstants.REPORT_QA_CHECKS_REPORT);
			allEntryFileToFileNameMap.putAll(entryFileToFileNameMap);
		}

		try
		{
			ZipIt.addEntriesToZipFile(zipFile, allEntryFileToFileNameMap, "");
			String filestore = AmbFileStoragePathUtils.getFileStorageDirPath(1)
					.replace("\\", "/");
			String fullPathName = zipFile.getAbsolutePath().replace("\\", "/");
			String path = fullPathName.substring(fullPathName
					.indexOf(filestore) + filestore.length());
			String root = AmbassadorUtil.getCapLoginOrPublicUrl()
					+ "/DownloadReports";
			fileUrl = root + path;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return fileUrl;
	}
	
    /**
     * Offline download to get reviewers comments report, translations edit
     * report or offline translation kit. For offline translation kit
     * downloading, it will follow logged user's "Download Options" as default.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to offline download file for.
     * @param p_workOfflineFileType
     *            -- 1 : Reviewer Comments Report or Translations Edit Report (this follows UI settings)
     *            -- 2 : Offline Translation Kit
     *            -- 3 : Translation Edit Report
     *            -- 4 : Reviewer Comments Report
     *            -- 5 : Reviewer Comments Report (Simplified)
     *            -- 6 : Post Review QA Report
     *
     * @return -- XML string. -- If fail, it will return an xml string to tell
     *         error message; -- If succeed, report returning is like
     *         "http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/TranslationsEditReport/20140219/ReviewersCommentsReport-(jobname_492637643)(337)-en_US_zh_CN-20140218_162543.xlsx";
     *         and offline translation kit is like
     *         "http://10.10.215.21:8080/globalsight/DownloadOfflineKit/[CompanyName]/GlobalSight/CustomerDownload/[jobName_zh_CN.zip]".
     * @throws WebServiceException
     */
    public String getWorkOfflineFiles(String p_accessToken, Long p_taskId,
            int p_workOfflineFileType) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_WORK_OFFLINE_FILES);

        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.getWorkOfflineFiles(p_accessToken, p_taskId,
                p_workOfflineFileType, false);
    }

    /**
     * Upload offline files to server.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to upload file to.
     * @param p_workOfflineFileType
     *            -- 1 : For reports like "Reviewer Comments Report", "Simplified Reviewer Comments Report", "Translations Edit Report" or "Post Review QA Report".
     *            -- 2 : Offline Translation Kit
     * @param p_fileName
     *            -- the upload file name
     * @param bytes
     *            -- file contents in bytes
     * @return    -- If succeed, return a "identifyKey" which is used to identify this uploading, a sample is "532689969".
     *            -- If fail, no key, return a standard error xml:
     *    <?xml version=\"1.0\" encoding=\"UTF-8\" ?>
     *    <errorXml>
     *        <method>uploadWorkOfflineFiles</method>
     *        <error>error message</error>
     *    </errorXml>
     * 
     * @throws WebServiceException
     */
    public String uploadWorkOfflineFiles(String p_accessToken, Long p_taskId,
            int p_workOfflineFileType, String p_fileName, byte[] bytes)
            throws WebServiceException
    {
        checkAccess(p_accessToken, UPLOAD_WORK_OFFLINE_FILES);

        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.uploadWorkOfflineFiles(p_accessToken, p_taskId,
                p_workOfflineFileType, p_fileName, bytes, false);
    }

    /**
     * Process offline file to update system.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to import file into.
     * @param p_identifyKey
     *            -- identifyKey to help locate where the uploaded file is.
     * @param p_workOfflineFileType
     *            -- 1 : For reports like "Reviewer Comments Report", "Simplified Reviewer Comments Report", "Translations Edit Report" or "Post Review QA Report".
     *            -- 2 : Offline Translation Kit
     * @return -- Empty if succeed; if fail, return corresponding message.
     * 
     * @throws WebServiceException
     */
    public String importWorkOfflineFiles(String p_accessToken, Long p_taskId,
            String p_identifyKey, int p_workOfflineFileType)
            throws WebServiceException
    {
        checkAccess(p_accessToken, IMPORT_WORK_OFFLINE_FILES);

        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.importWorkOfflineFiles(p_accessToken, p_taskId,
                p_identifyKey, p_workOfflineFileType, false);
    }

    
	/**
	 * Create job group
	 * 
	 * @param p_accessToken
	 *            -- login user's token
	 * @param groupName
	 *            --can not be empty
	 * @param projectName
	 *            --can not be empty
	 * @param sourceLocale
	 *            --can not be empty,like "de_DE"
	 * @return Create if succeed,return group name and id
	 * 
	 * @throws WebServiceException
	 */

	public String createJobGroup(String p_accessToken, String groupName,
			String projectName, String sourceLocale) throws WebServiceException
	{
		if (StringUtil.isEmpty(p_accessToken))
			return makeErrorXml(CREATE_JOB_GROUP, "Invaild access token.");
		// Check access token
		checkAccess(p_accessToken, CREATE_JOB_GROUP);

		if (StringUtil.isEmpty(groupName))
			return makeErrorXml(CREATE_JOB_GROUP, "Invaild group name.");

		String name = groupName.trim();
		if (name.length() > 100)
		{
			return makeErrorXml(CREATE_JOB_GROUP,
					"The length of job group name exceeds 100 characters.");
		}

		String specialChars = "~!@#$%^&*()+=[]\\';,./{}|\":<>?";
		for (int i = 0; i < groupName.length(); i++)
		{
			char c = groupName.charAt(i);
			if (specialChars.indexOf(c) > -1)
			{
				return makeErrorXml(CREATE_JOB_GROUP, ERROR_JOB_GROUP_NAME);
			}
		}

		if (StringUtil.isEmpty(projectName))
			return makeErrorXml(CREATE_JOB_GROUP, "Invaild project name.");

		if (StringUtil.isEmpty(sourceLocale))
			return makeErrorXml(CREATE_JOB_GROUP, "Invaild source locale.");

		User user = getUser(getUsernameFromSession(p_accessToken));
		long companyId = CompanyWrapper.getCompanyByName(user.getCompanyName())
				.getId();

		Map<String, String> map = checkGroupName(companyId, groupName);
		if (map != null && map.size() > 0)
			return makeErrorXml(CREATE_JOB_GROUP,
					"Invaild group name,name already exists: " + groupName);
		Project project = null;
		try
		{
			project = ServerProxy.getProjectHandler()
					.getProjectByNameAndCompanyId(projectName, companyId);
			if (project == null)
				return makeErrorXml(CREATE_JOB_GROUP, "Invaild project name: " + projectName);
		}
		catch (Exception e)
		{
		}

		GlobalSightLocale locale = GSDataFactory.localeFromCode(sourceLocale.trim());
		if (locale == null)
			return makeErrorXml(CREATE_JOB_GROUP, "Invaild source locale.");

		String xml = saveJobGroup(groupName, project, locale, companyId, user.getUserId());
		return xml;
	}

	private String saveJobGroup(String groupName, Project project,
			GlobalSightLocale locale, long companyId, String userId)
	{
		JobGroup group = new JobGroup();
		group.setName(groupName);
		group.setProject((ProjectImpl) project);
		group.setSourceLocale(locale);
		group.setCompanyId(companyId);
		group.setCreateDate(new Date());
		group.setCreateUserId(userId);
		try
		{
			HibernateUtil.save(group);
		}
		catch (Exception e)
		{

		}

		StringBuffer subXML = new StringBuffer();
		subXML.append("<JobGroup>\r\n");
		subXML.append("\t<id>").append(group.getId()).append("</id>\r\n");
		subXML.append("\t<name>")
				.append(EditUtil.encodeXmlEntities(groupName))
				.append("</name>\r\n");
		subXML.append("</JobGroup>\r\n");
		return subXML.toString();
	}

	private Map<String, String> checkGroupName(long companyId, String groupName)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		String sql = "SELECT JG.ID,JG.NAME FROM JOB_GROUP JG WHERE JG.COMPANY_ID = :companyId AND JG.NAME= :groupName";
		paramMap.put("companyId", companyId + "");
		paramMap.put("groupName", groupName);
		List result = (List) HibernateUtil.searchWithSql(sql.toString(),
				paramMap);
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < result.size(); i++)
		{
			Object[] bs = (Object[]) result.get(i);
			map.put(bs[1].toString(), bs[0].toString());
		}
		return map;
	}

	/**
	 * Add job to group
	 * 
	 * @param p_accessToken
	 *            -- login user's token
	 * @param groupId
	 *            --can not be empty
	 * @param jobId
	 *            --can not be empty,like "126" or "126,127,128"
	 * 
	 * @return Returns true if successful
	 * 
	 * @throws WebServiceException
	 */
	public String addJobToGroup(String p_accessToken, String groupId,
			String jobId) throws WebServiceException
	{
		if (StringUtil.isEmpty(p_accessToken))
			return makeErrorXml(ADD_JOB_TO_GROUP, "Invaild access token.");
		// Check access token
		checkAccess(p_accessToken, ADD_JOB_TO_GROUP);

		if (StringUtil.isEmpty(groupId))
			return makeErrorXml(ADD_JOB_TO_GROUP, "Invaild group id.");

		if (StringUtil.isEmpty(jobId))
			return makeErrorXml(ADD_JOB_TO_GROUP, "Invaild job id.");

		long projectId;
		JobGroup jobGroup = HibernateUtil.get(JobGroup.class,
				Long.parseLong(groupId));

		if (jobGroup == null)
			return makeErrorXml(ADD_JOB_TO_GROUP, "Invaild group id.");

		projectId = jobGroup.getProject().getId();
		String[] jobIdArr = jobId.split(",");
		String errorJobId = "";
		String existInGroup = "";
		for (String id : jobIdArr)
		{
			JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));

			if (job.getGroupId() != null)
			{
				if (existInGroup != "")
					existInGroup += ",";
				existInGroup += id;
				continue;
			}

			if (job == null || (projectId != job.getProjectId()))
			{
				if (errorJobId != "")
					errorJobId += ",";
				errorJobId += id;
			}
		}
		
		if (existInGroup.trim().length() > 0)
		{
			return makeErrorXml(ADD_JOB_TO_GROUP, "Job id (" + existInGroup
					+ ") already in the group.");
		}
		
		if (errorJobId.trim().length() > 0)
		{
			return makeErrorXml(ADD_JOB_TO_GROUP, "Invaild job id :"
					+ errorJobId);
		}

		String message = saveJobToGroup(groupId, jobId);
		return message;
	}

	private String saveJobToGroup(String groupId, String jobId)
	{
		boolean success = false;
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE JOB SET ").append("GROUP_ID = ").append(groupId)
				.append(" WHERE ID IN (").append(jobId).append(")");
		try
		{
			HibernateUtil.executeSql(sql.toString());
			success = true;
		}
		catch (Exception e)
		{
			success = false;
		}

		if (success)
			return "Added successfully !";

		return "Add Failed !";
	}
	
	/**
	 * Check TM export status by indentify key.
	 * 
	 * @param p_accessToken
	 *            -- login user's token
	 * @param p_identifyKey
	 *            -- -- identifyKey to help locate where the export file is.
	 * @return xml string 
	 * 			if failed or in progress,it is like
	 *         "<exportStatus><status>failed|inprogress</status><url></url></exportStatus>"
	 *         if finished,it is like
	 *         "<exportStatus><status>finished</status><url>http://10.10.215.27:8080/globalsight/DownloadTM/allie/GlobalSight/TmExport/846048690/file_name.zip</url></exportStatus>".
	 * @throws WebServiceException
	 * 
	 */
	public String getTmExportStatus(String p_accessToken, String p_identifyKey)
			throws WebServiceException
	{
		if (StringUtil.isEmpty(p_accessToken))
			return makeErrorXml(TM_EXPORT_STATUS, "Invaild access token.");
		// Check access token
		checkAccess(p_accessToken, TM_EXPORT_STATUS);

		if (StringUtil.isEmpty(p_identifyKey))
			return makeErrorXml(TM_EXPORT_STATUS, "Invaild identifyKey.");

		StringBuilder returnXml = new StringBuilder(XML_HEAD);
		String root = AmbassadorUtil.getCapLoginOrPublicUrl();
		String superFSDir = AmbFileStoragePathUtils.getFileStorageDirPath(1)
				.replace("\\", "/");
		String directory = ExportUtil.getExportDirectory();
		directory = directory.replace("\\", "/");
		String path = directory.substring(directory.indexOf(superFSDir)
				+ superFSDir.length());
		path = root + "/DownloadTM" + path + "/" + p_identifyKey + "/";
		String failed = directory + "/" + p_identifyKey + "/" + "failed";
		String inprogress = directory + "/" + p_identifyKey + "/"
				+ "inprogress";
		File failedFile = new File(failed);
		File inporgressFile = new File(inprogress);
		returnXml.append("<exportStatus>\r\n");
		if (failedFile.exists())
		{
			returnXml.append("\t<status>").append("failed")
					.append("</status>\r\n");
			returnXml.append("\t<url></url>\r\n");
		}
		else if (inporgressFile.exists() && !failedFile.exists())
		{
			returnXml.append("\t<status>").append("exporting")
					.append("</status>\r\n");
			returnXml.append("\t<url></url>\r\n");
		}
		else
		{
			try
			{
				File file = new File(directory + "/" + p_identifyKey);
				String fileName = file.list()[0];
				if (fileName.toLowerCase().endsWith(".xml")
						|| fileName.toLowerCase().endsWith(".tmx"))
				{
					String zipPath = directory + "/" + p_identifyKey + "/";
					String zipName = null;
					if (fileName.endsWith(".xml"))
					{
						zipName = fileName.substring(0,
								fileName.lastIndexOf(".xml"))
								+ ".zip";
						zipPath += zipName;
						path += zipName;
					}
					else if (fileName.endsWith(".tmx"))
					{
						zipName = fileName.substring(0,
								fileName.lastIndexOf(".tmx"))
								+ ".zip";
						zipPath += zipName;
						path += zipName;
					}
					String xmlPath = directory + "/" + p_identifyKey + "/"
							+ fileName;
					compressionXml(zipPath, new File(xmlPath));
				}
				else
				{
					path += fileName;
				}
			}
			catch (Exception e)
			{
				return makeErrorXml(TM_EXPORT_STATUS,
						"Compression is incorrect.");
			}

			returnXml.append("\t<status>").append("finished")
					.append("</status>\r\n");
			returnXml.append("\t<url>").append(path).append("</url>\r\n");
		}
		returnXml.append("</exportStatus>\r\n");
		return returnXml.toString();
	}
    
	/**
	 * Export TM data.
	 * 
	 * @param p_accessToken
	 *            -- login user's token
	 * @param p_tmName
	 *            -- TM name to export,can not be empty
	 * @param p_languages
	 *            -- language to export like "de_DE,fr_FR" or "fr_FR" or empty.
	 *            If empty, export all.
	 * @param p_startDate
	 *            -- start time in "yyyyMMdd" format,on this day of all time
	 *            periods will be included,can not be empty.
	 * @param p_finishDate
	 *            -- finish time in "yyyyMMdd" format,on this day of all time
	 *            periods will be included,can be empty, if empty, use current
	 *            time.
	 * @param p_exportFormat
	 *            -- export file formats: "GMX" and "TMX1.4b".
	 * @param p_exportedFileName
	 *            -- specified file name, if empty, use GlobalSight default name
	 *            like "tm_export_n.tmx" or "tm_export_n.xml".
	 * @return identifyKey -- to help locate where the exported file is.
	 * @throws WebServiceException
	 * 
	 */
	public String exportTM(String p_accessToken, String p_tmName,
			String p_languages, String p_startDate, String p_finishDate,
			String p_exportFormat, String p_exportedFileName)
			throws WebServiceException
	{
		if (StringUtil.isEmpty(p_accessToken))
			return makeErrorXml(EXPORT_TM, "Invaild access token.");
		// Check access token
		checkAccess(p_accessToken, EXPORT_TM);

		if (StringUtil.isEmpty(p_tmName))
			return makeErrorXml(EXPORT_TM, "Invaild tm name.");

		if (StringUtil.isNotEmpty(p_exportedFileName))
		{
			String specialChars = "~!@#$%^&*()+=[]\\';,./{}|\":<>?";
			for (int i = 0; i < p_exportedFileName.trim().length(); i++)
			{
				char c = p_exportedFileName.trim().charAt(i);
				if (specialChars.indexOf(c) > -1)
				{
					return makeErrorXml(EXPORT_TM, ERROR_EXPORT_FILE_NAME);
				}
			}
		}
		
		if(p_exportedFileName != null && p_exportedFileName.length() > 0)
			p_exportedFileName = p_exportedFileName.trim();

		if(p_exportedFileName != null && p_exportedFileName.length() == 0)
			p_exportedFileName = null;
		
		String identifyKey = null;
		IExportManager exporter = null;
		String options = null;
		String startDate = null;
		String finishDate = null;
		String fileType = null;

		try
		{
			exporter = TmManagerLocal.getProjectTmExporter(p_tmName);
			options = exporter.getExportOptions();
		}
		catch (Exception e)
		{
			return makeErrorXml(EXPORT_TM, "Invaild tm name.");
		}
		if (StringUtil.isEmpty(p_startDate))
		{
			return makeErrorXml(EXPORT_TM, "Invaild start date.");
		}
		else
		{
			startDate = checkDate(p_startDate);
			if (startDate.equals("error"))
			{
				return makeErrorXml(EXPORT_TM, "Invaild start date.");
			}
		}

		if (!StringUtil.isEmpty(p_finishDate))
		{
			finishDate = checkDate(p_finishDate);
			if (finishDate.equals("error"))
			{
				return makeErrorXml(EXPORT_TM, "Invaild finish date.");
			}
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			try
			{
				Date fshDate = sdf.parse(finishDate);
				Date staDate = sdf.parse(startDate);
				if (fshDate.before(staDate))
				{
					return makeErrorXml(EXPORT_TM,
							"Invaild start date and finish date.");
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}

		if (StringUtil.isNotEmpty(p_languages))
		{
			String[] languageArr = p_languages.split(",");
			for (String lang : languageArr)
			{
				lang = lang.replace("-", "_");
				GlobalSightLocale locale = GSDataFactory
						.localeFromCode(lang.trim());
				if (locale == null)
				{
					return makeErrorXml(EXPORT_TM, "Invaild language : "
							+ lang);
				}
			}
			p_languages = p_languages.replace("-", "_");
		}

		if (StringUtil.isEmpty(p_exportFormat)
				|| !p_exportFormat.trim().equalsIgnoreCase("GMX")
				&& !p_exportFormat.trim().equalsIgnoreCase("TMX1.4b"))
			return makeErrorXml(EXPORT_TM, "Invaild export format.");

		if (p_exportFormat.equalsIgnoreCase("GMX"))
		{
			fileType = "xml";
		}
		else if (p_exportFormat.equalsIgnoreCase("TMX1.4b"))
		{
			fileType = "tmx2";
		}
		if (options != null)
		{
			String directory = ExportUtil.getExportDirectory();
			identifyKey = AmbassadorUtil.getRandomFeed();
			directory = directory + "/" + identifyKey + "/" + "inprogress";
			new File(directory).mkdirs();
			options = joinXml(options, startDate, finishDate, fileType,
					p_languages, p_exportedFileName);
			try
			{
				exporter.setExportOptions(options);
				options = exporter.analyze();
				// pass down new options from client
				exporter.setExportOptions(options);
				((com.globalsight.everest.tm.exporter.ExportOptions) exporter
						.getExportOptionsObject()).setIdentifyKey(identifyKey);
				ProcessStatus status = new ProcessStatus();
				ResourceBundle bundle = PageHandler.getBundle(null);
				status.setResourceBundle(bundle);
				exporter.attachListener(status);
				exporter.doExport();
			}
			catch (Exception e)
			{
				ExportUtil.handleTmExportFlagFile(identifyKey, "failed", true);
			}
		}

		return identifyKey;
	}
	
	/**
	 * TM full text search.
	 * 
	 * @param p_accessToken
	 * @param p_string
	 *            --Search text,can not be empty.
	 * @param p_tmNames
	 *            --TM name,can not be empty.
	 * @param p_sourceLocale
	 *            --Source locale,like 'en_US',can not be empty.
	 * @param p_targetLocale
	 *            --Target locale,like 'de_DE',can not be empty.
	 * @param p_dateType
	 *            --The type of search by time,like 'create' or 'modify',can be
	 *            empty.
	 * @param p_startDate
	 *            --Start time in "yyyyMMdd" format, on this day of all time
	 *            periods will be included, can be empty.
	 * @param p_finishDate
	 *            -- Finish time in "yyyyMMdd" format, on this day of all time
	 *            periods will be included, can be empty.
	 * @param p_companyName
	 *            --Company name.If super user,compay name is subsidiaries name.
	 *            If not super user,company name is the login user of the
	 *            company.
	 * 
	 */
	public String tmFullTextSearch(String p_accessToken, String p_string,
			String p_tmNames, String p_sourceLocale, String p_targetLocale,
			String p_dateType, String p_startDate, String p_finishDate,
			String p_companyName) throws WebServiceException
	{
		if (StringUtil.isEmpty(p_accessToken))
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild access token.");
		// Check access token
		checkAccess(p_accessToken, TM_FULL_TEXT_SEARCH);

		String errorXml = checkParamters(p_accessToken, p_string, p_tmNames,
				p_sourceLocale, p_targetLocale, p_dateType,p_startDate, p_finishDate,
				p_companyName);
		if (StringUtil.isNotEmpty(errorXml))
			return errorXml;

		if (StringUtil.isEmpty(p_dateType))
			p_dateType = "create";

		StringBuffer xml = new StringBuffer(XML_HEAD);
		LocaleManager lm = ServerProxy.getLocaleManager();
		try
		{
			Date startDate = parseStartDate(p_startDate);
			Date endDate = parseEndDate(p_finishDate);
			boolean searchInSource = true;
			Company company = ServerProxy.getJobHandler().getCompany(
					p_companyName);
			GlobalSightLocale sourceGSL = lm.getLocaleByString(p_sourceLocale);
			GlobalSightLocale targetGSL = lm.getLocaleByString(p_targetLocale);
			// get all selected TMS
			ArrayList<Tm> tmList = new ArrayList<Tm>();
			String[] tmNameArray = p_tmNames.split(",");
			for (String tmName : tmNameArray)
			{
				tmList.add(getProjectTm(tmName, company.getId()));
			}
			// do search
			TmCoreManager mgr = LingServerProxy.getTmCoreManager();
			List<TMidTUid> queryResult = mgr.tmConcordanceQuery(tmList,
					p_string, searchInSource ? sourceGSL : targetGSL,
					searchInSource ? targetGSL : sourceGSL, null);

			xml.append("<segments>\r\n");
			xml.append("\t<sourceLocale>").append(sourceGSL.getDisplayName())
					.append("</sourceLocale>\r\n");
			xml.append("\t<targetLocale>").append(targetGSL.getDisplayName())
					.append("</targetLocale>\r\n");

			// Get all TUS by queryResult, then get all needed properties
			List<SegmentTmTu> tus = LingServerProxy.getTmCoreManager()
					.getSegmentsById(queryResult);
			for (int i = 0, max = tus.size(); i < max; i++)
			{
				SegmentTmTu tu = tus.get(i);
				if (tu == null)
				{
					continue;
				}
				long tuId = tu.getId();
				BaseTmTuv srcTuv = tu.getFirstTuv(sourceGSL);
				if (startDate != null || endDate != null)
				{
					if (p_dateType.equalsIgnoreCase("create"))
					{
						Date creationDate = format.parse(format.format(srcTuv
								.getCreationDate()));
						boolean checkSrcDate = checkCreatetionDate(
								creationDate, startDate, endDate);
						if (!checkSrcDate)
							continue;
					}
					else if (p_dateType.equalsIgnoreCase("modify"))
					{
						Date modifyDate = format.parse(format.format(srcTuv
								.getModifyDate()));
						boolean checkSrcDate = checkCreatetionDate(modifyDate,
								startDate, endDate);
						if (!checkSrcDate)
							continue;
					}
				}
				TmxWriter.convertTuvToTmxLevel(tu, (SegmentTmTuv) srcTuv,
						TmxWriter.TMX_LEVEL_2);
				xml.append("\t<segment>\r\n");
				xml.append("\t\t<sourceSegment>")
						.append(GxmlUtil.stripRootTag(srcTuv.getSegment()))
						.append("</sourceSegment>\r\n");
				BaseTmTuv trgTuv;
				Collection targetTuvs = tu.getTuvList(targetGSL);
				for (Iterator it = targetTuvs.iterator(); it.hasNext();)
				{
					trgTuv = (BaseTmTuv) it.next();
					TmxWriter.convertTuvToTmxLevel(tu, (SegmentTmTuv) trgTuv,
							TmxWriter.TMX_LEVEL_2);
					xml.append("\t\t<targetSegment>")
							.append(GxmlUtil.stripRootTag(trgTuv.getSegment()))
							.append("</targetSegment>\r\n");
					String sid = trgTuv.getSid();
					long tuvId = trgTuv.getId();
					if (null == sid)
					{
						sid = "N/A";
					}
					long tmId = trgTuv.getTu().getTmId();
					xml.append("\t\t<sid>")
							.append(EditUtil.encodeXmlEntities(sid))
							.append("</sid>\r\n");
					xml.append("\t\t<tmName>")
							.append(ServerProxy.getProjectHandler()
									.getProjectTMById(tmId, false).getName())
							.append("</tmName>\r\n");
				}
				xml.append("\t</segment>\r\n");
			}
			xml.append("</segments>\r\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (xml.toString().length() > 0)
			return xml.toString();

		return "No matching content !";
	}

	private boolean checkCreatetionDate(Date creationDate, Date startDate,
			Date endDate)
	{
		if (startDate != null && endDate == null)
		{
			if (!creationDate.after(startDate)
					&& !creationDate.equals(startDate))
			{
				return false;
			}
		}
		else if (startDate == null && endDate != null)
		{
			if (!creationDate.before(endDate) && creationDate.equals(endDate))
			{
				return false;
			}
		}
		else if (startDate != null && endDate != null)
		{
			if ((!creationDate.after(startDate) && !creationDate
					.equals(startDate))
					|| (!creationDate.before(endDate) && !creationDate
							.equals(endDate)))
			{
				return false;
			}
		}
		return true;
	}
    
	private Date parseStartDate(String dateStr)
	{
		SimpleDateFormat sfm1 = new SimpleDateFormat("yyyyMMdd HHmmss");
		if (StringUtil.isNotEmpty(dateStr))
		{
			try
			{
				dateStr += " 000000";
				return sfm1.parse(dateStr);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private Date parseEndDate(String dateStr)
	{
		SimpleDateFormat sfm1 = new SimpleDateFormat("yyyyMMdd HHmmss");
		if (StringUtil.isNotEmpty(dateStr))
		{
			try
			{
				dateStr += " 235959";
				return sfm1.parse(dateStr);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	private String checkParamters(String p_accessToken, String p_string,
			String p_tmNames, String p_sourceLocale, String p_targetLocale,
			String p_dateType, String p_startDate, String p_finishDate,
			String p_companyName)
	{
		if (StringUtil.isEmpty(p_string))
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild search string.");

		if (StringUtil.isEmpty(p_tmNames))
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild tm name.");

		if (StringUtil.isEmpty(p_sourceLocale))
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild source locale.");

		GlobalSightLocale sourceLocale = GSDataFactory
				.localeFromCode(p_sourceLocale);
		if (sourceLocale == null)
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild source locale.");

		if (StringUtil.isEmpty(p_targetLocale))
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild target locale.");

		GlobalSightLocale targetLocale = GSDataFactory
				.localeFromCode(p_targetLocale);
		if (targetLocale == null)
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild target locale.");
		
		if (StringUtil.isNotEmpty(p_dateType))
		{
			if (!p_dateType.equalsIgnoreCase("create")
					&& !p_dateType.equalsIgnoreCase("modify"))
				return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild date type.");
		}
		
		if (StringUtil.isEmpty(p_companyName))
			return makeErrorXml(TM_FULL_TEXT_SEARCH, "Invaild company name.");

		String userName = getUsernameFromSession(p_accessToken);
		Company logUserCompany = getCompanyInfo(userName);
		if (!CompanyWrapper.SUPER_COMPANY_ID.equals(String
				.valueOf(logUserCompany.getId())))
		{
			if (!logUserCompany.getName().equalsIgnoreCase(p_companyName))
			{
				return makeErrorXml(TM_FULL_TEXT_SEARCH,
						"Invaild company name.");
			}
			else
			{
				String[] tmNameArr = p_tmNames.split(",");
				for (String tmName : tmNameArr)
				{
					ProjectTM projectTm = getProjectTm(tmName,
							logUserCompany.getId());
					if (projectTm == null)
					{
						return makeErrorXml(TM_FULL_TEXT_SEARCH, tmName
								+ " is invaild tm name.");
					}
				}
			}
		}
		else
		{
			try
			{
				Company company = ServerProxy.getJobHandler().getCompany(
						p_companyName);
				String[] tmNameArr = p_tmNames.split(",");
				for (String tmName : tmNameArr)
				{
					ProjectTM projectTm = getProjectTm(tmName, company.getId());
					if (projectTm == null)
					{
						return makeErrorXml(TM_FULL_TEXT_SEARCH, tmName
								+ "is invaild tm name.");
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		String startDate = null;
		String finishDate = null;
		if (StringUtil.isNotEmpty(p_startDate))
		{
			startDate = checkDate(p_startDate);
			if (startDate.equals("error"))
			{
				return makeErrorXml(EXPORT_TM, "Invaild start date.");
			}
		}

		if (!StringUtil.isEmpty(p_finishDate))
		{
			finishDate = checkDate(p_finishDate);
			if (finishDate.equals("error"))
			{
				return makeErrorXml(EXPORT_TM, "Invaild finish date.");
			}
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			try
			{
				if (StringUtil.isNotEmpty(startDate))
				{
					Date fshDate = sdf.parse(finishDate);
					Date staDate = sdf.parse(startDate);
					if (fshDate.before(staDate))
					{
						return makeErrorXml(EXPORT_TM,
								"Invaild start date and finish date.");
					}
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}
	
	private ProjectTM getProjectTm(String tmName, String companyId)
	{
		ProjectTM projectTM = null;
		try
		{
			String hql = "from ProjectTM p where p.name = :name and p.companyId = :companyId";

			HashMap map = new HashMap();
			map.put("name", tmName);
			map.put("companyId", Long.parseLong(companyId));
			projectTM = (ProjectTM) HibernateUtil.getFirst(hql, map);
		}
		catch (Exception e)
		{
			String[] args = new String[1];
			args[0] = String.valueOf(tmName);
			throw new ProjectHandlerException(
					ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TM_BY_ID,
					args, e);
		}

		return projectTM;
	}
	
	private String checkDate(String strDate)
	{
		String formatDate = null;
		try
		{
			SimpleDateFormat sfm1 = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sfm2 = new SimpleDateFormat("MM/dd/yyyy");
			formatDate = sfm2.format(sfm1.parse(strDate));
		}
		catch (Exception e)
		{
			return "error";
		}
		return formatDate;
	}

	private String joinXml(String xml, String startDate, String finishDate,
			String fileType, String languages, String exportedFileName)
			throws WebServiceException
	{
		Document doc = null;
		try
		{
			doc = DocumentHelper.parseText(xml);
			Element rootElt = doc.getRootElement();
			Iterator fileIter = rootElt.elementIterator("fileOptions");
			while (fileIter.hasNext())
			{
				Element fileEle = (Element) fileIter.next();
				if (exportedFileName != null)
				{
					Element fileNameElem = fileEle.element("fileName");
					if (fileType.equals("xml"))
					{
						fileNameElem.setText(exportedFileName + ".xml");
					}
					else if (fileType.equals("tmx2"))
					{
						fileNameElem.setText(exportedFileName + ".tmx");
					}
				}
				Element fileTypeElem = fileEle.element("fileType");
				fileTypeElem.setText(fileType);
				Element fileEncodingElem = fileEle.element("fileEncoding");
				fileEncodingElem.setText("UTF-8");
			}

			Iterator selectIter = rootElt.elementIterator("selectOptions");
			while (selectIter.hasNext())
			{
				Element selectEle = (Element) selectIter.next();
				Element selectModeElem = selectEle.element("selectMode");
				Element selectLanguage = selectEle.element("selectLanguage");
				if (StringUtil.isEmpty(languages))
				{
					selectModeElem
							.setText(com.globalsight.everest.tm.exporter.ExportOptions.SELECT_ALL);
				}
				else
				{
					selectModeElem
							.setText(com.globalsight.everest.tm.exporter.ExportOptions.SELECT_FILTERED);
					selectLanguage.setText(languages);
				}
			}

			Iterator filterIter = rootElt.elementIterator("filterOptions");
			while (filterIter.hasNext())
			{
				Element filterEle = (Element) filterIter.next();
				Element createdafterElem = filterEle.element("createdafter");
				createdafterElem.setText(startDate);
				Element createdbeforeElem = filterEle.element("createdbefore");
				if (finishDate == null)
				{
					Date nowDate = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat(
							"MM/dd/yyyy HH:mm:ss");
					String nowDateStr = sdf.format(nowDate);
					createdbeforeElem.setText(nowDateStr);
				}
				else
				{
					createdbeforeElem.setText(finishDate);
				}
			}

			Iterator outputIter = rootElt.elementIterator("outputOptions");
			while (outputIter.hasNext())
			{
				Element outputEle = (Element) outputIter.next();
				Element systemFields = outputEle.element("systemFields");
				systemFields.setText("true");
			}

			String xmlDoc = doc.asXML();
			return xmlDoc.substring(xmlDoc.indexOf("<exportOptions>"));
		}
		catch (DocumentException e)
		{
			throw new WebServiceException(e.getMessage());
		}
	}

	private void compressionXml(String zipFileName, File inputFile)
	{
		try
		{
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					zipFileName));
			out.putNextEntry(new ZipEntry(inputFile.getName()));
			FileInputStream inputStream = new FileInputStream(inputFile);
			int b;
			while ((b = inputStream.read()) != -1)
			{
				out.write(b);
			}
			inputStream.close();
			inputFile.delete();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    /**
     * Get a link for in context review for specified task ID. User need not
     * logging in GlobalSight.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID
     * @return A link like "http://10.10.215.20:8080/globalsight/ControlServlet?linkName=self&pageName=inctxrvED1&secret=E127B35E1A1C1B52C742353BBA176327D7F54956B373428134DE7252182EAA0D".
     * 
     * @throws WebServiceException
     */
    public String getInContextReviewLink(String p_accessToken, String p_taskId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_IN_CONTEXT_REVIEW_LINK);
        try {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertIsInteger(p_taskId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return makeErrorXml(GET_IN_CONTEXT_REVIEW_LINK, e.getMessage());
        }

        String loggingUserName = getUsernameFromSession(p_accessToken);
        String userId = UserUtil.getUserIdByName(loggingUserName);

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
            return makeErrorXml(GET_IN_CONTEXT_REVIEW_LINK, message);
        }

        if (task == null)
        {
            return makeErrorXml(GET_IN_CONTEXT_REVIEW_LINK, "Can not get task by taskID.");
        }

        if (task.getState() == Task.STATE_COMPLETED)
        {
            return makeErrorXml(GET_IN_CONTEXT_REVIEW_LINK,
                    "The current task has been in completed state.");
        }

        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggingUserName);
            activityArgs.put("taskId", p_taskId);
            activityStart = ActivityLog.start(Ambassador.class,
                    "getInContextReviewLink(p_accessToken, p_taskId)", activityArgs);

            User pm = task.getWorkflow().getJob().getProject()
                    .getProjectManager();
            WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
                    .getWorkflowTaskInstance(userId, task.getId(),
                            WorkflowConstants.TASK_ALL_STATES);
            task.setWorkflowTask(wfTask);
            List allAssignees = task.getAllAssignees();
            if (allAssignees != null && allAssignees.size() > 0)
            {
                if (!allAssignees.contains(userId)
                        && !userId.equalsIgnoreCase(pm.getUserId()))
                {
                    String message = "'"
                            + userId
                            + "' is neither acceptor/available assignee of current task nor project manager.";
                    logger.warn(message);
                    return makeErrorXml(GET_IN_CONTEXT_REVIEW_LINK, message);
                }
            }

            StringBuffer link = new StringBuffer();
            link.append(AmbassadorUtil.getCapLoginOrPublicUrl());
            link.append("/ControlServlet?linkName=self&pageName=inctxrvED1&secret=");
            StringBuffer secret = new StringBuffer();
            secret.append("taskId=").append(p_taskId.trim())
                    .append("&nameField=").append(loggingUserName);
            link.append(AmbassadorUtil.encryptionString(secret.toString()));
            return link.toString();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to get In Context Review Link for taskId : " + p_taskId;
            return makeErrorXml(GET_IN_CONTEXT_REVIEW_LINK, message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }
}
