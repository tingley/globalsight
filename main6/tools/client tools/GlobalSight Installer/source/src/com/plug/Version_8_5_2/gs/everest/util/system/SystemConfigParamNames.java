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

package com.plug.Version_8_5_2.gs.everest.util.system;

/**
 * This interface defines the names of the Envoy system configuration
 * parameters. The symbols defined here are used with the SystemConfiguration
 * methods to read the system configuration parameter values at run time.
 */
public interface SystemConfigParamNames
{
    /**
     * System property that tells which appserver to use.
     * 
     * @see AppserverWrapperFactory for vocabulary
     */
    public static final String APPSERVER_VENDOR = "globalsight.appserver";

    /**
     * Each component's server class name
     */
    public static final String SERVER_CLASSES = "server.classes";

    // JNDI lookup string for UserTransaction
    public static final String USER_TRANSACTION = "appserver.userTransaction";

    // db connection config
    public static final String DB_USERNAME = "db.username";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_CONNECTION = "db.connection";

    // the name of the JDBC driver to use
    public static final String DB_DRIVER = "db.driver";

    // If specified to true then logging will be turned on. The
    // default is "false" - not turned on. This logs
    // acquiring/releasing connections and the SQL that is executed.
    public static final String HIBERNATE_LOGGING = "hibernate.logging";

    //
    // Parameters used by the RequestHandler
    //

    // Default SAX parser factory class name
    public static final String DFLT_SAX_PARSER_FACTORY = "parser.factory";
    public static final String VALIDATE_XML = "parser.validatexml";

    // System-wide tu types to exclude while leveraging
    public static final String LEVERAGE_EXCLUDE_TU_TYPES = "leverager.excludeTuTypes";

    // System-wide parameters for term auto replacement
    public static final String AUTO_REPLACE_TERMS = "leverager.autoReplaceTerms";

    //
    // NamedQueries derived class names
    //

    public static final String NAMED_QUERIES_CLASS_NAMES = "named.queries.class.names";

    // log4j Priority to assign to all existing GlobalSightCatagories
    // One of FATAL, ERROR, WARN, INFO, or DEBUG.
    public static final String SYSTEM_LOGGING_PRIORITY = "system.logging.priority";
    public static final String SYSTEM_LOGGING_DIRECTORY = "system.logging.directory";
    public static final String CXE_LOGGING_DIRECTORY = "cxe.logging.directory";

    //
    // JNDI parameters
    //
    // JNDI provider port number
    public static final String SERVER_PORT = "nonSSLPort";

    // JNDI provider host name
    public static final String SERVER_HOST = "server.host";

    // JNDI (username/password)
    public static final String JNDI_REMOTE_USERNAME = "remote.username";
    public static final String JNDI_REMOTE_PASSWORD = "remote.password";

    //
    // Parameters for login.
    //

    // Parameter for a user to log in to CAP
    public static final String CAP_LOGIN_URL = "cap.login.url";
    // Specifies if concurrent duplicate logins are allowed (duplicate = same
    // login name)
    public static final String CONCURRENT_DUP_LOGIN_ALLOWED = "login.concurrentDuplicate.allowed";

    //
    // Parameters for exporting
    //

    // System Parameters for Corpus TM
    public static final String CORPUS_INSTALL_KEY = "corpus.installKey";
    public static final String CORPUS_STORE_NATIVE_FORMAT = "corpus.storeNativeFormat";
    public static final String CORPUS_SHOW_ALL_TMS_TO_LPS = "corpus.showAllTmsToLPs";

    // System parameter for customer access group
    public static final String CUSTOMER_INSTALL_KEY = "customerAccessGroup.installKey";
    public static final String IS_DELL = "customerAccessGroup.isDell";

    // System Parameters for the web service
    public static final String WEBSVC_INSTALL_KEY = "websvc.installKey";

    // System Parameters for the catalyst integration
    public static final String CATALYST_INSTALL_KEY = "catalyst.installKey";

    // The parameter used for getting CAP's export servlet URL.
    public static final String CAP_SERVLET_URL = "cap.servlet.url";

    // The parameter used for getting CXE's export servlet URL.
    public static final String CXE_SERVLET_URL = "cxe.servlet.url";

    // The parameter used for getting export's directory rule type.
    public static final String DIRECTORY_RULE_TYPE = "export.dirRuleType";

    // The parameter used for getting the default export location
    public static final String DEFAULT_EXPORT_LOCATION = "export.defaultLocation";

    // The parameter to specify if the FONT_FACE for cjk languages should be
    // pre-pended too. This is done in the case where they aren't localized
    public static final String OVERRIDE_FONT_FACE_ON_EXPORT = "export.overrideCjkFontFace";

    //
    // Parameters used for manual import
    //
    public static final String CXE_DOCS_DIR = "cxe.docsDir";

    // whether manual import should behave like auto import
    public static final String IMPORT_SINGLE_BATCH = "import.manualImportSingleBatch";

    // whether manual import ui should suggest a job name
    public static final String IMPORT_SUGGEST_JOB_NAME = "import.suggestJobName";

    public static final String WEB_SERVICE_REMOTE_ADDRESS = "remote.ipAddressFilterWSvc";

    /**
     * The file storage root used for saving GlobalSight related files. This is
     * the root for files such as the secondary target files and is used as a
     * common root for storing all other GS files.
     */
    public static final String FILE_STORAGE_DIR = "fileStorage.dir";

    // ////////////////////////////////////////////////////////////////////
    // Begin: Email info
    // ////////////////////////////////////////////////////////////////////
    /**
     * The parameter used for getting the mail server used.
     */
    public static final String MAIL_SERVER = "mailserver";
    /**
     * The parameter used for getting administrator's email address.
     */
    public static final String ADMIN_EMAIL = "admin.email";
    /**
     * The parameter used for getting the email address of a default contact for
     * a customer.
     */
    public static final String DEFAULT_PM_EMAIL = "email.defaultPm";

    // ////////////////////////////////////////////////////////////////////
    // Begin: Workflow ownership
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant identifying the project manager role parameter.
     */
    public static final String PROJECT_MANAGER_ROLE = "pm.role";
    /**
     * Constant identifying the project manager access group parameter.
     */
    public static final String PROJECT_MANAGER_GROUP = "pm.accessGroup";

    public static final String LDAP_HOST = "ldap.host";
    public static final String LDAP_PORT = "ldap.port";
    public static final String LDAP_USER_NAME = "ldap.username";
    public static final String LDAP_PASSWORD = "ldap.password";
    public static final String LDAP_MIN_CONNECTIONS = "ldap.minConnections";
    public static final String LDAP_MAX_CONNECTIONS = "ldap.maxConnections";
    public static final String LDAP_BASE = "ldap.base";

    // /////////////////////////////////////////////////////////////////////
    // Begin: System Config Parameter Names.
    // /////////////////////////////////////////////////////////////////////
    public static final String ADD_LANG_META_TAG = "addLanguageMetaTag";
    public static final String DEBUG_MODE = "system.logging.priority";
    public static final String EXPORT_DIR_NAME_STYLE = "export.dirRuleType";
    public static final String WEB_SERVER_ADMIN_EMAIL_ADDRESS = "admin.email";

    public static final String HYPERLINK_COLOR_OVERRIDE = "hyperlinkColorOverride";
    public static final String HYPERLINK_COLOR = "hyperlinkColor";
    public static final String ACTIVE_HYPERLINK_COLOR = "activeHyperlinkColor";
    public static final String VISITED_HYPERLINK_COLOR = "visitedHyperlinkColor";

    // L10nProfile priority
    public static final String MAX_PRIORITY = "priority.max";
    public static final String DEFAULT_PRIORITY = "priority.default";

    // number of milliseconds between notifying localization participants
    // and re-importing a file (delays the import)
    public static final String REIMPORT_DELAY_MILLISECONDS = "reimport.delay.milliseconds";

    // specifies what type of re-import is allowed
    // NONE
    // DELAY
    // REIMPORT_NEW_TARGETS
    public static final String REIMPORT_OPTION = "reimport.option";

    public static final String DUPLICATION_OF_OBJECTS_ALLOWED = "duplicationOfObjects.allowed";

    // search and replace
    public static final String JOB_SEARCH_REPLACE_ALLOWED = "jobSearchReplace.allowed";
    // iflow
    public static final String IFLOW_USER_NAME = "iflow.username";
    public static final String IFLOW_PASSWORD = "iflow.password";
    public static final String IFLOW_HOST = "iflow.host";
    public static final String IFLOW_DB_USERNAME = "iflow.dbusername";
    public static final String IFLOW_DB_PASSWORD = "iflow.dbpassword";
    public static final String IFLOW_ADMIN_USERNAME = "iflow.adminUsername";
    public static final String IFLOW_ADMIN_PASSWORD = "iflow.adminPassword";
    public static final String IFLOW_RMI_PORT = "iflow.rmi.port";
    public static final String IFLOW_MODEL_API_LOG_FILE = "iFlowModelApi.log";
    public static final String IFLOW_LOG_PATH_PARAM = "log4j.appender.A1.File";

    // cxe
    public static final String CXE_USE_SSL = "useSSL";
    public static final String CXE_NON_SSL_PORT = "nonSSLPort";
    public static final String CXE_SSL_PORT = "SSLPort";

    public static final String USE_SSL = "enable.proxy.ssl";
    public static final String USE_SSL_FORCE_REDIRECT = "useSSLForceRedirect";
    public static final String NON_SSL_PORT = "nonSSLPort";
    public static final String SSL_PORT = "SSLPort";

    public static final String USE_PROXY = "useProxy";
    public static final String PROXY_SERVER_NAME = "proxy.server.name";
    public static final String PROXY_SERVER_PORT = "proxy.server.port";

    /**
     * Parameter used for enabling/disabling the system level notification.
     */
    public static final String SYSTEM_NOTIFICATION_ENABLED = "systemNotification.enabled";
    /**
     * Parameter used for enabling/disabling the warning/deadline notification.
     */
    public static final String USE_WARNING_THRESHOLDS = "warningThresholds.enabled";

    /**
     * Parameter used for determining the warning threshold for event
     * notification.
     */
    public static final String TIMER_THRESHOLD = "timer.threshold";

    public static final String WEB_SERVER_DOC_ROOT = "webserver.docroot";

    public static final String DATABASE_INSTALL_KEY = "database.installKey";

    // for the desktop adapters (quark/frame)
    public static final String QUARK_INSTALL_KEY = "quark.installKey";
    public static final String FRAME_INSTALL_KEY = "frame.installKey";
    public static final String COPYFLOW_INSTALL_KEY = "quarkmac.installKey";

    public static final String CXE_NTCS_DIR = "cxe.ntcsDir";

    // for the PDF adapters
    public static final String PDF_INSTALL_KEY = "pdf.installKey";
    public static final String PDF_CONV_DIR = "pdf.dir";

    // for the ms office adapters
    public static final String WORD_INSTALL_KEY = "word.installKey";
    public static final String EXCEL_INSTALL_KEY = "excel.installKey";
    public static final String POWERPOINT_INSTALL_KEY = "powerpoint.installKey";
    public static final String MSOFFICE_CONV_DIR = "msoffice.dir";
    public static final String MSOFFICE2003_CONV_DIR = "msoffice2003.dir";

    // for the adobe adapters
    public static final String INDD_INSTALL_KEY = "indd.installKey";
    public static final String ILLUSTRATOR_INSTALL_KEY = "illustrator.installKey";
    public static final String ADOBE_CONV_DIR = "adobe.dir";
    public static final String ADOBE_CONV_DIR_CS3 = "adobe.cs3.dir";
    public static final String ADOBE_CONV_DIR_CS4 = "adobe.cs4.dir";
    public static final String ADOBE_CONV_DIR_CS5 = "adobe.cs5.dir";
    public static final String ADOBE_CONV_DIR_CS5_5 = "adobe.cs5.5.dir";

    // for the passolo adapters
    public static final String PASSOLO_INSTALL_KEY = "passolo.installKey";
    public static final String PASSOLO_CONV_DIR = "passolo.dir";

    /**
     * Parameter used to determine a list for file extensions that should be
     * handled differently upon an import failure (when extracted).
     */
    public static final String HANDLE_IMPORT_FAILURE = "formatType.handleImportFailure";

    //
    // Parameters for GUI
    //

    // boolean for whether to automatically refresh the task and job lists
    public static final String REFRESH_UI_LISTS = "cap.refreshUiLists";
    // refresh interval in seconds
    public static final String REFRESH_RATE = "cap.refreshRate";

    // boolean for whether to automatically refresh the Progress Bar
    public static final String REFRESH_PROGRESS = "cap.refreshProgress";
    // progress bar refresh interval in seconds
    public static final String PROGRESS_REFRESH_RATE = "cap.refreshProgressRate";

    /**
     * Number of workflow templates to be displayed per page
     */
    public static final String NUM_WFT_PER_PAGE = "workflowTemplatesPerPage";

    public static final String NUM_TMPROFILES_PER_PAGE = "tmProfilesPerPage";

    /**
     * Number of calendaring related objects to be displayed per page.
     */
    public static final String CALENDERING_DISPLAY_PER_PAGE = "calendaringDisplayPerPage";

    /**
     * Number of vendors displayed per page
     */
    public static final String VENDORS_PER_PAGE = "vendorsPerPage";
    public static final String PROJECTS_PER_PAGE = "projectsPerPage";
    public static final String ROLES_PER_PAGE = "rolesPerPage";

    /**
     * Parameters for costing.
     */
    public static final String COSTING_ENABLED = "jobCosting.enabled";
    public static final String REVENUE_ENABLED = "revenue.enabled";
    public static final String PIVOT_CURRENCY = "jobCosting.pivotCurrency";
    public static final String COSTING_LOCKDOWN = "jobCosting.lockFinishedCost";

    /**
     * Parameter for sort order of comments
     */
    public static final String COMMENTS_SORTING = "comments.sorting";

    /**
     * Parameters for add/delete locale specific content
     */
    public static final String ADD_DELETE_ENABLED = "addDelete.enabled";

    /**
     * Parameters for reports
     */
    public static final String REPORTS_ENABLED = "reports.enabled";

    /**
     * Parameters for Email Authentication
     */
    public static final String EMAIL_AUTHENTICATION_ENABLED = "email.authentication.enabled";
    public static final String ACCOUNT_USERNAME = "account.username";
    public static final String ACCOUNT_PASSWORD = "account.password";

    /** Vignette Constants */
    public static final String VIGNETTE_INSTALL_KEY = "vignette.installKey";

    /** ServiceWare Constants */
    public static final String SERVICEWARE_INSTALL_KEY = "serviceware.installKey";

    /** Documentum Constants */
    public static final String DOCUMENTUM_INSTALL_KEY = "documentum.installKey";

    /** CMS (Mediasurface) Constants */
    public static final String CMS_INSTALL_KEY = "cms.installKey";
    public static final String CMS_UI_HOST = "cms.ui.host";
    public static final String CMS_UI_PORT = "cms.ui.port";
    public static final String CMS_CONTENT_SERVER_URL = "cms.contentServerUrl";
    public static final String CMS_CONTENT_SERVER_NAME = "cms.contentServerName";
    public static final String CMS_CONTENT_SERVER_PORT = "cms.contentServerPort";

    /** Analyze script */
    public static final String ANALYZE_SCRIPT_RUN = "analyze_script.run";
    public static final String ANALYZE_SCRIPT_INTERVAL = "analyze_script.interval";

    /**
     * Parameter representing the supported UI locales (in db system_parameter)
     */
    public static final String UI_LOCALES = "ui.locales";
    /**
     * Parameter representing the default UI locale (in db system_parameter)
     */
    public static final String DEFAULT_UI_LOCALE = "default.ui.locale";

    /**
     * Parameter used to determine whether the AND/OR nodes should be visible
     * (in envoy.properties)
     */
    public static final String AND_OR_NODES = "and.or.nodes";

    /**
     * Parameter enabling anonymous access to termbases in the viewer.
     */
    public static final String ANONYMOUS_TERMBASES = "anonymous.termbases";

    /**
     * Parameter to determine if the PM email notification check-box in Workflow
     * Template screen should be selected by default (during workflow template
     * creation only).
     */
    public static final String PM_EMAIL_NOTIFICATION = "pm.email.notification";

    /**
     * Parameters for specifying how many records to display per page on the My
     * Jobs and My Activities pages.
     */
    public static final String RECORDS_PER_PAGE_JOBS = "recordsPerPage.jobs";
    public static final String RECORDS_PER_PAGE_TASKS = "recordsPerPage.tasks";

    /**
     * This parameter is used for the My Jobs UI where jobs with creation date
     * up to the specified value of this parameter will be displayed (i.e. jobs
     * within the past 30 days).
     */
    public static final String MY_JOBS_DAYS_RETRIEVED = "myJobs.daysRetrieved";

    /**
     * Parameter that sets whether report access is checked to see if the user
     * has a valid session.
     */
    public static final String REPORTS_CHECK_ACCESS = "reports.checkAccess";

    /**
     * For old "Dell_Review" activity, change to support Multi-Company, Some
     * this Activity can be configurable for every new Company. The report will
     * only display activity has this name
     */
    public static final String REPORTS_ACTIVITY = "reports.activity";

    /**
     * Parameter that sets whether the ExportBatchEvent information can be
     * removed after a completed export.
     */
    public static final String EXPORT_REMOVE_INFO_ENABLED = "export.remove_info.enabled";

    // // Calendar Manager ////
    /**
     * Parameter used for the installation key of the Calendar component.
     */
    public static final String CALENDAR_INSTALL_KEY = "calendar.installKey";
    /**
     * Parameter used to determine the start time of the event responsible for
     * calendar related cleanup (i.e. removal of reserved times)
     */
    public static final String CALENDAR_CLEANUP_START_TIME = "calendar.cleanupStartTime";

    /**
     * Determines the calendar cleanup recurrance (i.e. daily/weekly/monthly)
     */
    public static final String CALENDAR_CLEANUP_RECURRANCE = "calendar.cleanupRecurrance";

    /**
     * The cleanup period is used to cleanup calendar related objects that are
     * older than the given period in envoy.properties (based on the fired
     * event's date)
     */
    public static final String CALENDAR_CLEANUP_PERIOD = "calendar.cleanupPeriod";

    public static final String GLOBALSIGHT_HOME_DIRECTORY = "globalsight.home";
    public static final String INSTALLATION_DATA_DIRECTORY = "install.data.dir";

    // The first three parameters have been moved to TMProfileConstants.java,
    // only the last one is left here for now
    // public static final String
    // MT_OVERRIDE_MATCHES="machineTranslation.overrideNonExactMatches";
    // public static final String MT_ENGINE = "machineTranslation.engine";
    // public static final String MT_AUTOCOMMIT_TO_TM =
    // "machineTranslation.autoCommitToTM";
    public static final String MT_SHOW_IN_EDITOR = "machineTranslation.showInEditor";

    /** Process Runner related properties **/
    public static final String PROCESS_RUNNER_WAIT_FOR_OUTPUT = "processRunner.waitForOutput";

    /** Online Editor related properties */
    public static final String PARAGRAPH_EDITOR_INSTALL_KEY = "editor.installKey";
    public static final String GXML_EDITOR_INSTALL_KEY = "editor.gxml.installKey";

    /** Terminology related properties */
    public static final String TERMINOLOGY_FEATURES_INSTALL_KEY = "terminology.features.installKey";

    /** Vendor Management install key ***/
    public static final String VENDOR_MANAGEMENT_INSTALL_KEY = "vendorManagement.installKey";

    /** Corpus Aligner install key */
    public static final String CORPUS_ALIGNER_INSTALL_KEY = "corpusAligner.installKey";

    // TM tokenizer
    public static final String TM_TOKENIZER = "tm.tokenizer";

    // Shutdown UI properties
    public static final String SHUTDOWN_UI_ENABLED = "shutdown.ui.enabled";
    public static final String SHUTDOWN_UI_BANNER_ENABLED = "shutdown.ui.banner.enabled";
    public static final String SHUTDOWN_UI_MSG = "shutdown.ui.msg";

    // localizables word count
    public static final String COUNT_LOCALIZABLE = "wordcount.localizable";

    // Day(s) after the deadline to notify the PM/User
    public static final String OVERDUE_PM_DAY = "overdue.pm.day";
    public static final String OVERDUE_USER_DAY = "overdue.user.day";

    // For "Additional functionality quotation" issue
    public static final String PER_FILE_CHARGE01_KEY = "per.file.charge01.value";
    public static final String PER_FILE_CHARGE02_KEY = "per.file.charge02.value";
    public static final String PER_JOB_CHARGE_KEY = "per.job.charge.value";

    // Add parameters for master layer translate switch
    public static final String INDESIGN_MASTER_TRANSLATE = "indesign.master.translate";
    public static final String PPT_MASTER_TRANSLATE = "ppt.master.translate";
    public static final String ADOBE_XMP_TRANSLATE = "adobe.xmp.translate";

    // Add parameters for open office
    public static final String OPENOFFICE_INSTALL_KEY = "openoffice.install.key";
    public static final String OPENOFFICE_INSTALL_DIR = "openoffice.install.dir";

    // Add parameters for ms office
    // public static final String MS_OFFICE_INSTALL_KEY =
    // "msoffice.2010.install.key";
    // public static final String MS_OFFICE_INSTALL_DIR =
    // "msoffice.2010.install.dir";
    //
    // public static final String IDML_INSTALL_KEY = "idml.install.key";
    // public static final String IDML_INSTALL_DIR = "idml.install.dir";

    // Delay time for download job after exporting and task complete after
    // upload on offline
    public static final String DOWNLOAD_JOB_DELAY_TIME = "download.delay.time.after.exporting";
    public static final String TASK_COMPLETE_DELAY_TIME = "task.complete.delay.time.after.upload";

    // Add parameter for Single sign-on
    public static final String ENABLE_SSO = "sso.enabled";

    // for win pe
    public static final String WINDOWS_PE_INSTALL_KEY = "winpe.installKey";
    public static final String WINDOWS_PE_DIR = "winpe.dir";

    // Server Instance ID
    public static final String SERVER_INSTANCE_ID = "server.instance.id";

    // maxMyJobsThreads
    public static final String MAX_MY_JOBS_THREADS = "maxMyJobsThreads";
}
