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
package com.globalsight.everest.webapp;

/**
 * This interface contains all of the webapp package related constants.
 */
public interface WebAppConstants
{
    // URLs for accessing files
    // These must map the URLs defined in the web.xml
    public static final String STF_FILES_URL_MAPPING = "/globalsight/GlobalSight/SecondaryTargetFiles/";
    public static final String UNEXTRACTED_FILES_URL_MAPPING = "/globalsight/GlobalSight/UnextractedFiles/";

    // FOR MULTICOMPANY
    public static final String SELECTED_COMPANY_NAME_FOR_SUPER_PM = "selectedCompanyNameForSuperPM";

    // ACCESS RIGHT
    public static final String PERMISSIONS = "permissions";
    public static final String ACCESS_GROUP_ALL = "all";
    public static final String ACCESS_GROUP_NONE = "none";

    // HTML STUFF
    public static final String CHECKED = "CHECKED";
    public static final String SELECTED = "SELECTED";

    // constant values
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";

    //
    // Links defined in page descriptions in the EnvoyConfig.xml
    //

    // A
    public static final String ACTIVITY_PASS = "pass";
    public static final String ACTIVITY_DUPLICATE = "duplicate";
    // D
    public static final String DUMMY_LINK = "dummyLink";
    public static final String DUMMY_NAVIGATION_BEAN_NAME = "dummyNavigationBean";
    // E
    public static final String EXPENSES = "expenses";
    // L
    public static final String LOGIN_PASS = "pass";
    public static final String LOGIN_FAIL = "fail";
    public static final String SIMPLE_LOGIN_FAIL = "simpleLoginFail";
    public static final String LOGIN_TRANSLATOR = "translator";
    public static final String LOGIN_VENDOR = "vendor";

    public static final String LOGIN_PROTOCOL = "loginProtocol";
    public static final String LOGIN_PORT = "loginPort";
    public static final String LOGIN_SERVER = "loginServer";

    public static final String IS_SUPER_ADMIN = "Current_User_Is_SuperAdmin";

    // Super PM Login
    public static final String COMPANY_NAME = "companyName";
    public static final String COMPANY_NAMES = "companyNames";
    public static final String COMP_PAGE_NAME = "COMP";
    public static final String LOG4_PAGE_NAME = "LOG4";
    public static final String SUPER_ADMINISTRATOR_NAME = "SuperAdministrator";
    public static final String SUPER_PM_NAME = "SuperProjectManager";

    public static final String COMPANY_ID = "companyID";
    // P
    public static final String PASS_LINK = "pass";
    // R
    public static final String REVENUE = "revenue";
    // L10n
    public static final String LOCPROFILE_SKIP_JQ = "skipJobQueue";
    // U
    public static final String USER_PARAMS = "userParams";
    public static final String USER_PARAMS_ERROR = "userParamsError";

    //
    // constants used as request attributes or parameters
    //

    public static final String COLUMN_NUM = "columnNum";

    //
    // general constants to be used in pages across the whole system
    // specifies common parameters
    //

    public static final String CANCEL = "Cancel";

    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";
    public static final String IMPORT = "import";
    public static final String CLEAR_ALL = "clearAll";
    public static final String DONE = "done";

    public static final String TASK = "workObject";
    public static final String WORK_OBJECT = "workObject";
    public static final String TASK_ACTION = "taskAction";
    public static final String TASK_ACTION_ACCEPT = "acceptTask";
    public static final String TASK_ACTION_ACCEPTALL = "acceptAllTasks";
    public static final String TASK_ACTION_BATCH_COMPLETE_ACTIVITY = "completeActivity";
    public static final String TASK_ACTION_BATCH_COMPLETE_WORKFLOW = "completeWorkflow";
    public static final String TAST_ACTION_DOWNLOADALL = "downloadALLOfflineFiles";
    public static final String TAST_ACTION_DOWNLOADALL_COMBINED = "downloadALLOfflineFilesCombined";
    public static final String TASK_ACTION_ACCEPT_AND_DOWNLOAD = "acceptTaskAndDownload";
    public static final String TASK_ACTION_CREATE_STF = "createStf";
    public static final String TASK_ACTION_FINISH = "finishTask";
    public static final String TASK_ACTION_REJECT = "rejectTask";
    public static final String TASK_ACTION_SAVECOMMENT = "saveComment";
    public static final String TASK_ACTION_GETTASKSTATUS = "getTaskStatus";
    public static final String TASK_ACTION_GETTASKSTATUS_ALL = "getTaskStatusAll";
    public static final String TASK_ACTION_SELECTED_TASKSSTATUS = "selectedTasksStatus";
    public static final String TASK_ACTION_MODIFY_ACCOUNT = "modifyAccount";
    public static final String TASK_ACTION_RETRIEVE = "getTask";
    public static final String TASK_ACTION_SAVEDETAILS = "saveDetails";
    public static final String TASK_ACTION_APPROVE_TUV = "approveTuv";
    public static final String TASK_ACTION_TRANSLATED_TEXT_RETRIEVE = "retrieveTranslatedText";
    public static final String TASK_ACTION_DOWNLOAD_SOURCEPAGES = "downloadSourcePages";
    public static final String TASK_ACTION_SCORECARD = "scorecard";
    public static final String TASK_ACTION_SAVE_SCORECARD = "saveScorecard";
    public static final String UPDATE_LEVERAGE = "updateLeverage";

    public static final String TASK_COMMENT = "taskComment";
    public static final String TASK_DETAILPAGE_ID = "detailPageId";
    public static final String DTP_DOWNLOAD = "dtpDownload";
    public static final String DTP_UPLOAD = "dtpUpload";
    public static final String TASK_ID = "taskId";
    public static final String TASK_LIST = "taskList";
    public static final String TASK_LIST_START = "taskListStart";
    public static final String TASK_STATE = "state";
    public static final String TASK_TYPE = "taskType";
    public static final String TASK_FILE_NAME = "taskFileName";

    // Offline - Download related parameters
    public static final String DOWNLOAD_ACTION = "downloadAction";
    public static final String DOWNLOAD_ACTION_BACK = "downloadBack";
    public static final String DOWNLOAD_ACTION_CANCEL = "downloadCancel";
    public static final String DOWNLOAD_ACTION_REFRESH = "downloadRefresh";
    public static final String DOWNLOAD_ACTION_DONE = "downloadDone";
    public static final String DOWNLOAD_ACTION_START_DOWNLOAD = "startDownload";
    public static final String DOWNLOAD_MANAGER = "downloadManager";
    public static final String DOWNLOAD_PARAMS = "downloadParams";
    public static final String DOWNLOAD_STATUS = "downloadStatus";

    // Offline - Upload related parameters
    public static final String UPLOAD_ACTION = "uploadAction";
    public static final String UPLOAD_ACTION_BACK = "uploadBack";
    public static final String UPLOAD_ACTION_CANCEL = "uploadCancel";
    public static final String UPLOAD_ACTION_CANCE_PROGRESS = "uploadProgressCancel";
    public static final String UPLOAD_ACTION_CONFIRM_CONTINUE = "uploadConfirmContinue";
    public static final String UPLOAD_ACTION_REFRESH = "uploadRefresh";
    public static final String UPLOAD_ACTION_DONE = "uploadDone";
    public static final String UPLOAD_ACTION_PROGRESS = "uploadProgress";
    public static final String UPLOAD_ACTION_START_UPLOAD = "startUpload";
    public static final String REPORT_TYPE = "reportType";
    public static final String LANGUAGE_SIGN_OFF = "languageSignOff";
    public static final String TRANSLATION_EDIT = "translationEdit";
    public static final String POST_REVIEW_QA = "postReviewQA";
    public static final String TRANSLATION_VERIFICATION = "translationVerification";

    public static final String UPLOAD_ACTION_UPLOAD_REPORT = "uploadReport";
    public static final String DOWNLOAD_ACTION_START_DOWNLOAD_TER = "downloadTER";
    public static final String DOWNLOAD_ACTION_START_DOWNLOAD_LSO = "downloadLSO";

    public static final String UPLOAD_MANAGER = "uploadManager";
    public static final String UPLOAD_PARAMS = "uploadParams";
    public static final String UPLOAD_STATUS = "uploadStatus";
    public static final String UPLOAD_ORIGIN = "origin";
    public static final String UPLOAD_FROMSIMPLEUPLOAD = "fromSimpleUpload";
    public static final String UPLOAD_FROMTASKUPLOAD = "fromTaskUpload";

    public static final String EXPORT_LOCATION_ACTION = "exportAction";
    public static final String EXPORT_LOCATION_ACTION_REMOVE = "remove";
    public static final String EXPORT_LOCATION_ACTION_DEFAULT = "makeDefault";
    public static final String EXPORT_LOCATION_ACTION_NEW = "new";
    public static final String EXPORT_LOCATION_ACTION_MODIFY = "modify";
    public static final String EXPORT_LOCATION_NEW_NAME = "newName";
    public static final String EXPORT_LOCATION_NEW_LOCATION = "newLocation";
    public static final String EXPORT_LOCATION_NEW_DESCRIPTION = "newDescription";
    public static final String EXPORT_LOCATION_MODIFY_ID = "modifyId";
    public static final String EXPORT_LOCATION_MODIFY_NAME = "modifyName";
    public static final String EXPORT_LOCATION_MODIFY_LOCATION = "modifyLocation";
    public static final String EXPORT_LOCATION_MODIFY_DESCRIPTION = "modifyDescription";

    public static final String JOB_COMMENTS = "JOBCOMMENTS";
    public static final String PAGE_NAME = "pageName";
    public static final String TARGET_PAGES = "targetPages";
    public static final String TARGET_PAGE_ID = "targetPageId";
    public static final String TARGET_PAGE_NAME = "targetPageName";
    public static final String TARGETVIEW_LOCALE = "trgViewLocale";
    public static final String REVIEW_MODE = "reviewMode";

    public static final String UID = "UID_";

    public static final String APPLET = "applet";
    public static final String APPLET_DIRECTORY_SESSION_NAME_RANDOM = "rand";
    public static final String LOGIN_NAME_FIELD = "nameField";
    static public final String PASSWORD_NAME_FIELD = "passwordField";
    public static final String LOGIN_EMAIL_FIELD = "emailField";
    public static final String INITIAL_SCREEN = "initial";

    //
    // For error handling
    //

    public static final String ERROR_BEAN_NAME = "error";
    public static final String ERROR_PAGE = "/envoy/common/error.jsp";
    public static final String ACTIVITY_ERROR_PAGE = "/envoy/common/activityError.jsp";

    //
    // Fields carried by SessionManager or Request
    //

    public static final String ACTIVITY_TO_PROCESS = "activityToProcess";
    public static final String GROUP_NAME = "groupname";
    public static final String DEFAULT_LOCALE_RESOURCE_NAME = "LocaleResource";
    public static final String ENVOY_CONFIG_FILE = "/resources/EnvoyConfig.xml";
    public static final String GROUP_USER_SEARCH_PARAMS = "groupUserSearchParams";
    public static final String SESSION_MANAGER = "sessionManager";
    public static final String SOURCE_PAGE_ID = "sourcePageId";
    public static final String SOURCE_PAGE = "sourcePage";
    public static final String SSL_FAIL = "sslFail";
    public static final String JOB_ID = "jobId";

    /**
     * An identifier for a combination of job and workflow name which is used as
     * a key for a session attribute.
     */
    public static final String JOB_AND_WORKFLOW_NAME = "jobAndWorkflowName";

    /* Used by User module */
    public static final String USER = "userObject";
    public static final String USER_NAME = "userId";
    public static final String USER_PASSWORD = "password";
    public static final String USER_PASSWORD_CONFIRM = "passwordConfirm";
    public static final String USER_LAST_NAME = "lastName";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_PHONE = "phone";
    public static final String USER_EMAIL = "email";
    public static final String USER_ADDRESS = "address";
    public static final String USER_UI_LOCALE = "uiLocale";

    public static final String USER_ACTION = "action";
    public static final String USER_ACTION_ADD_ANOTHER_LOCALES = "addNewLocales";
    public static final String USER_ACTION_NEW_LOCALES = "newLocales";
    public static final String USER_ACTION_MODIFY_LOCALES = "modifyLocales";
    public static final String USER_ACTION_CANCEL_LOCALES = "cancelLocales";
    public static final String USER_ACTION_CREATE_USER = "createUser";
    public static final String USER_ACTION_MODIFY_USER = "modifyUser";
    public static final String USER_ACTION_MODIFY2_USER = "modify2User";
    public static final String USER_ACTION_MODIFY_USER_CONTACT = "modifyContactInfoUser";
    public static final String USER_ACTION_MODIFY_USER_PROJECTS = "modifyProjectsUser";
    public static final String USER_ACTION_SEARCH_USER = "searchUser";
    public static final String USER_ACTION_SET_RATE = "setRate";
    public static final String USER_ACTION_SET_SOURCE = "setSource";
    public static final String USER_ACTION_CANCEL_FROM_ACTIVITIES = "cancelActivities";
    public static final String USER_ACTION_PREVIOUS = "previous";
    public static final String USER_ACTION_REMOVE_ROLES = "removeUserRoles";

    public static final String CREATE_USER_WRAPPER = "createUserWrapper";
    public static final String MODIFY_USER_WRAPPER = "modifyUserWrapper";

    public static final String ADDED_NOTIFICATION_OPTIONS = "addedNotificationOptions";
    public static final String AVAILABLE_NOTIFICATION_OPTIONS = "availableNotificationOptions";

    public static final String CMS_USER_INFO = "cmsUserInfoObject";
    public static final String CMS_USER_NAME = "cmsUsername";
    public static final String CMS_PASSWORD = "cmsPassword";
    public static final String USER_TIME_ZONE = "userTimeZone";
    public static final String UILOCALE = "uiLocale";
    public static final String LOCALIZATION_PARTICIPANT = "LocalizationParticipant";

    public static final String USER_IDS = "userIds";
    public static final String USER_NAMES = "userNames";
    public static final String SELECTED_COMPANY_ID = "selectedCompanyID";
    public static final String USER_ACTION_IMPORT = "importUser";
    public static final String USER_ACTION_EXPORT = "exportUser";

    /**
     * An identifier for a project id that's used as a key for a session manager
     * attribute
     */
    public static final String PROJECT_ID = "projectId";

    public static final String PROJECT_ERROR = "PROJECT_ERROR";

    /**
     * Identifier used for a project name.
     */
    public static final String PROJECT_NAME = "projectName";

    /**
     * Identifier used for a project lable (project vs. division)
     */
    public static final String PROJECT_LABEL = "projectLabel";

    /**
     * Identifier used for a project's JavaScript warning message.
     */
    public static final String PROJECT_JS_MSG = "projectJsMsg";

    /**
     * An identifier for a project manager used as a key for a session
     * attribute.
     */
    public static final String IS_PROJECT_MANAGER = "isProjectManager";

    /**
     * An identifier for an assignee used as a key for a session attribute.
     */
    public static final String IS_ASSIGNEE = "isAssignee";

    /**
     * An identifier for the refresh link used as a key for a session attribute.
     */
    public static final String IS_REFRESH = "isRefresh";

    /**
     * An identifiers for myActivities default sort - used as a keys for a
     * session attributes.
     */
    public static final String MYACT_COL_SORT_ID = "myActColSortId";
    public static final String MYACT_SORT_ASC = "myActSortAscending";

    //
    // glossary fields
    //

    /**
     * Session attribute name for
     * {@link com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState}
     * object.
     */
    public static final String GLOSSARYSTATE = "GLOSSARYSTATE";

    //
    // comment reference fields
    //

    /**
     * Session attribute name for
     * {@link com.globalsight.everest.webapp.pagehandler.administration.comment.CommentState}
     * object.
     */
    public static final String COMMENT_REFERENCE_CANCEL = "cancel";
    public static final String COMMENT_REFERENCE_ACTION_DONE = "done";
    public static final String COMMENT_REFERENCE_ACTION_CANCEL = "cancel";
    public static final String COMMENT_REFERENCE_RESTRICTED_ACCESS = "Restricted";
    public static final String COMMENT_REFERENCE_RESTRICTED = "restricted";
    public static final String COMMENT_REFERENCE_GENERAL_ACCESS = "General";
    public static final String COMMENT_REFERENCE_NO_DELETE = "0";
    public static final String COMMENT_REFERENCE_DELETE = "1";
    public static final String COMMENT_REFERENCE_TRUE = "true";
    public static final String COMMENT_REFERENCE_FALSE = "false";
    public static final String COMMENT_REFERENCE_TEMP_DIR = "tmp";
    public static final String COMMENT_REFERENCE_TASK_COMMENT = "taskComment";
    public static final int PARTIAL_COMMENT_LENGTH = 20;

    // Constant for paging mechanism
    public static final int NUMBER_OF_PAGES_IN_GROUP = 11;

    //
    // editor fields
    //

    /**
     * Session attribute name for
     * {@link com.globalsight.everest.webapp.pagehandler.edit.online.EditorState
     * EditorState} object.
     */
    public static final String EDITORSTATE = "EDITORSTATE";

    /**
     * Session attribute name for
     * {@link com.globalsight.everest.edit.online.SegmentView SegmentView}
     * object.
     */
    public static final String SEGMENTVIEW = "SEGMENTVIEW";

    /**
     * Session attribute name for
     * {@link com.globalsight.everest.edit.online.CommentView CommentView}
     * object.
     */
    public static final String COMMENTVIEW = "COMMENTVIEW";

    /**
     * Session attribute name for
     * {@link com.globalsight.everest.edit.online.PageInfo PageInfo} object.
     */
    public static final String PAGEINFO = "PAGEINFO";

    /**
     * Request attribute name for Paragraph Editor.
     */
    public static final String PARAGRAPH_EDITOR = "paragraph_editor";
    public static final String GXML_EDITOR = "gxml_editor";

    //
    // Fields read from System Configuration, initilized at runtime
    //

    // /public static final String HOST = RuntimeInitializer.HOST;
    public static final int HTTP_PORT = WebAppConstantsHelper.HTTP_PORT;
    // public static final int HTTPS_PORT = WebAppConstantsHelper.HTTPS_PORT;

    //
    // Terminology fields
    //
    public static final String TERMBASE = "TERMBASE";
    public static final String TERMBASE_NAME = "TERMBASE_NAME";
    public static final String TERMBASE_SOURCE = "SOURCE";
    public static final String TERMBASE_TARGET = "TARGET";
    public static final String TERMBASE_QUERY = "QUERY";
    public static final String TERMBASE_TERMBASEID = "TERMBASEID";
    public static final String TERMBASE_CONCEPTID = "CONCEPTID";
    public static final String TERMBASE_TERMID = "TERMID";
    public static final String TERMBASE_DEFINITION = "TERMBASE_DEFINITION";
    public static final String TERMBASE_IMPORTER = "TERMBASE_IMPORTER";
    public static final String TERMBASE_IMPORT_OPTIONS = "importoptions";
    public static final String TERMBASE_EXPORTER = "TERMBASE_EXPORTER";
    public static final String TERMBASE_EXPORT_OPTIONS = "exportoptions";
    public static final String TERMBASE_INDEXER = "TERMBASE_INDEXER";
    public static final String TERMBASE_INDEXING_STATUS = "TERMBASE_INDEXING";
    public static final String TERMBASE_STATISTICS = "statistics";
    public static final String TERMBASE_STATUS = "status";
    public static final String TERMBASE_REINDEX_STATUS = "reindex_status";
    public static final String TERMBASE_USERDATA = "TERMBASE_USERDATA";
    public static final String TERMBASE_INPUTMODEL = "TERMBASE_INPUTMODEL";
    public static final String TERMBASE_SEARCHCONDITION = "TERMBASE_SEARCHCONDITION";
    public static final String TERMBASE_SEARCHRESULTS = "TERMBASE_SEARCHRESULTS";
    public static final String TERMBASE_SEARCHREPLACER = "TERMBASE_SEARCHREPLACER";
    public static final String TERMBASE_SEARCH = "search";
    public static final String TERMBASE_REPLACE = "replace";
    public static final String TERMBASE_LEVEL = "level";
    public static final String TERMBASE_LEVEL_ENTRY = "levelentry";
    public static final String TERMBASE_LEVEL_CONCEPT = "levelconcept";
    public static final String TERMBASE_LEVEL_LANGUAGE = "levellanguage";
    public static final String TERMBASE_LEVEL_TERM = "levelterm";
    public static final String TERMBASE_LANGUAGE = "language";
    public static final String TERMBASE_FIELD = "field";
    public static final String TERMBASE_FIELDNAME = "fieldname";
    public static final String TERMBASE_CASEINSENSITIVE = "caseinsensitive";
    public static final String TERMBASE_SMARTREPLACE = "smartreplace";
    public static final String TERMBASE_REPLACEINDEX = "replaceindex";

    public static final String TERMBASE_ACTION = "action";
    public static final String TERMBASE_ACTION_NEW = "new";
    public static final String TERMBASE_ACTION_MODIFY = "modify";
    public static final String TERMBASE_ACTION_CLONE = "clone";
    public static final String TERMBASE_ACTION_DELETE = "delete";
    public static final String TERMBASE_ACTION_IMPORT = "import";
    public static final String TERMBASE_ACTION_EXPORT = "export";
    public static final String TERMBASE_ACTION_INDEX = "index";
    public static final String TERMBASE_ACTION_SAVE = "save";
    public static final String TERMBASE_ACTION_CANCEL = "cancel";
    public static final String TERMBASE_ACTION_USERS = "users";
    public static final String TERMBASE_ACTION_SAVEUSERS = "saveUsers";
    public static final String TERMBASE_ACTION_CANCEL_IMPORT = "cancelImport";
    public static final String TERMBASE_ACTION_CANCEL_IMPORT_TEST = "cancelImportTest";
    public static final String TERMBASE_ACTION_CANCEL_EXPORT = "cancelExport";
    public static final String TERMBASE_ACTION_CANCEL_SEARCH = "cancelSearch";
    public static final String TERMBASE_ACTION_REFRESH = "refresh";
    public static final String TERMBASE_ACTION_DONE = "done";
    public static final String TERMBASE_ACTION_UPLOAD_FILE = "upload";
    public static final String TERMBASE_ACTION_UPLOAD_IMPORT_EXCEL_FILE = "uploadimportexcel";
    public static final String TERMBASE_ACTION_ANALYZE_FILE = "analyze";
    public static final String TERMBASE_ACTION_ANALYZE_TERMBASE = "analyze";
    public static final String TERMBASE_ACTION_SET_IMPORT_OPTIONS = "setImportOptions";
    public static final String TERMBASE_ACTION_SET_EXPORT_OPTIONS = "setExportOptions";
    public static final String TERMBASE_ACTION_TEST_IMPORT = "testImport";
    public static final String TERMBASE_ACTION_START_IMPORT = "startImport";
    public static final String TERMBASE_ACTION_START_EXPORT = "startExport";
    public static final String TERMBASE_ACTION_START_TBX_IMPORT = "startTbxImport";
    public static final String TERMBASE_ACTION_STATISTICS = "statistics";
    public static final String TERMBASE_ACTION_INPUT_MODELS = "inputModels";
    public static final String TERMBASE_ACTION_MAINTENANCE = "maintenance";
    public static final String TERMBASE_ACTION_SEARCH = "search";
    public static final String TERMBASE_ACTION_REPLACE = "replace";
    public static final String TERMBASE_ACTION_REPLACEALL = "replaceall";
    public static final String TERMBASE_ACTION_SHOWPREVIOUS = "showprevious";
    public static final String TERMBASE_ACTION_SHOWNEXT = "shownext";

    public static final String TERMBASE_ACTION_TERM_SEARCH = "termSearch";
    public static final String TERMBASE_ACTION_TERM_SEARCH_ORDERING = "ordering";
    public static final String TERMBASE_ACTION_TERM_SEARCH_PAGING = "paging";

    public static final String TERMBASE_ACTION_LOAD_OBJECT = "loadObject";
    public static final String TERMBASE_ACTION_CREATE_OBJECT = "createObject";
    public static final String TERMBASE_ACTION_MODIFY_OBJECT = "modifyObject";
    public static final String TERMBASE_ACTION_REMOVE_OBJECT = "removeObject";
    public static final String TERMBASE_ACTION_MAKE_DEFAULT_OBJECT = "defaultObject";
    public static final String TERMBASE_ACTION_UNSET_DEFAULT_OBJECT = "unsetDefaultObject";
    public static final String TERMBASE_ACTION_SHOW_PROGRESS = "showProgress";

    public static final String TERMBASE_TB_ID = "id";
    public static final String TERMBASE_TB_NAME = "name";
    public static final String TERMBASE_TB_NAMELIST = "namelist";
    public static final String TERMBASE_TB_KEY = "termbase";

    public static final String TERMBASE_OBJECT_NAMELIST = "object_namelist";
    public static final String TERMBASE_OBJECT_TYPE = "object_type";
    public static final String TERMBASE_OBJECT_USER = "object_user";
    public static final String TERMBASE_OBJECT_NAME = "object_name";
    public static final String TERMBASE_OBJECT_VALUE = "object_value";

    public static final String TERMBASE_XML = "xml";
    public static final String TERMBASE_TBX = "tbx";

    public static final String TERMBASE_ERROR = "TERMBASE_ERROR";

    //
    // Snippet Library and Add/Delete Constants
    //
    public static final String SNIPPET_LIBRARY = "snippetlibrary";

    public static final String SNIPPET_ACTION_GETGENERICSNIPPET = "getgenericsnippet";
    public static final String SNIPPET_ACTION_GETSNIPPET = "getsnippet";
    public static final String SNIPPET_ACTION_GETSNIPPETS = "getsnippets";
    public static final String SNIPPET_ACTION_GETPAGE = "getpage";

    public static final String SNIPPET_ACTION_CREATESNIPPET = "createsnippet";
    public static final String SNIPPET_ACTION_CREATESNIPPETGETSNIPPET = "createsnippetgetsnippet";
    public static final String SNIPPET_ACTION_MODIFYSNIPPET = "modifysnippet";
    public static final String SNIPPET_ACTION_MODIFYSNIPPETGETSNIPPET = "modifysnippetgetsnippet";
    public static final String SNIPPET_ACTION_REMOVESNIPPET = "removesnippet";

    public static final String SNIPPET_ACTION_ADDSNIPPETGETPAGE = "addsnippetgetpage";
    public static final String SNIPPET_ACTION_MODIFYSNIPPETGETPAGE = "modifysnippetgetpage";
    public static final String SNIPPET_ACTION_REMOVESNIPPETGETPAGE = "removesnippetgetpage";
    public static final String SNIPPET_ACTION_DELETECONTENTGETPAGE = "deletecontentgetpage";
    public static final String SNIPPET_ACTION_UNDELETECONTENTGETPAGE = "undeletecontentgetpage";

    public static final String SNIPPET_ACTION_GETGENERICNAMES = "getgenericnames";
    public static final String SNIPPET_ACTION_GETSNIPPETSBYLOCALE = "getsnippetsbylocale";

    //
    // TM Management Constants
    //
    public static final String TM_ACTION_CANCEL_SEARCH = "cancelSearch";
    public static final String TM_SEARCH_STATE_PARAM = "SearchState";
    public static final String TM_SEARCH_STATE_NORMAL = "Normal";
    public static final String TM_SEARCH_STATE_HIGHLIGHT = "Highlight";
    public static final String TM_SEARCH_STATE_NEXT = "Next";
    public static final String TM_SEARCH_STATE_PREV = "Previous";
    public static final String TM_SEARCHRESULTS = "TM_SEARCHRESULTS";
    public static final String TM_SEARCHREPLACER = "TM_SEARCHREPLACER";
    public static final String TM_SEARCH = "search";
    public static final String TM_REPLACE = "replace";

    public static final String TM_SOURCE_FIND_TEXT = "SourceFindText";
    public static final String TM_SOURCE_FIND_MATCH_CASE = "SourceFindMatchCase";
    public static final String TM_SOURCE_SEARCH_LOCALES = "TmSourceSearchLocales";
    public static final String TM_SOURCE_SEARCH_LOCALE_SELECTOR = "TmSourceSearchLocaleSelector";
    public static final String TM_SOURCE_SEARCH_LOCALE = "TmSourceSearchLocale";
    public static final String TM_TARGET_FIND_TEXT = "TargetFindText";
    public static final String TM_TARGET_FIND_MATCH_CASE = "TargetFindMatchCase";
    public static final String TM_TARGET_REPLACE_TEXT = "TargetReplaceText";
    public static final String TM_TARGET_SEARCH_LOCALES = "TmTargetSearchLocales";
    public static final String TM_TARGET_SEARCH_LOCALE_SELECTOR = "TmTargetSearchLocaleSelector";
    public static final String TM_TARGET_SEARCH_LOCALE = "TmTargetSearchLocale";
    public static final String TM_REPLACE_SEGMENT_CHKBOX = "TmReplaceSegmentCheckbox";
    public static final String TM_CONCORDANCE_MANAGER = "TmConcordanceManager";
    public static final String TM_CONCORDANCE_SEARCH_RESULTS_HTML = "TmConcordanceSearchResultsHtml";
    public static final String TM_CONCORDANCE_SEARCH_RESULTS = "TmConcordanceSearchResults";
    public static final String TM_CONCORDANCE_REPLACE_RESULTS_HTML = "TmConcordanceReplaceResultsHtml";
    public static final String TM_CONCORDANCE_REPLACE_RESULTS = "TmConcordanceReplaceResults";

    // More TM Management Constants
    public static final String TM = "TM";
    public static final String TM_NAME = "TM_NAME";
    public static final String TM_TMID = "TMID";
    public static final String TM_DEFINITION = "TM_DEFINITION";
    public static final String TM_PROJECT = "TM_PROJECT";
    public static final String TM_IMPORTER = "TM_IMPORTER";
    public static final String TM_IMPORT_OPTIONS = "importoptions";
    public static final String TM_EXPORTER = "TM_EXPORTER";
    public static final String TM_EXPORT_OPTIONS = "exportoptions";
    public static final String TM_STATISTICS = "statistics";

    public static final String TM_ACTION = "action";
    public static final String TM_ACTION_NEW = "new";
    public static final String TM_ACTION_MODIFY = "modify";
    public static final String TM_ACTION_CLONE = "clone";
    public static final String TM_ACTION_DELETE = "delete";
    public static final String TM_ACTION_DELETE_LANGUAGE = "deleteTMLanguage";
    public static final String TM_ACTION_DELETE_TULISTING = "deleteTUListing";
    public static final String TM_ACTION_IMPORT = "import";
    public static final String TM_ACTION_EXPORT = "export";
    public static final String TM_ACTION_USERS = "users";
    public static final String TM_ACTION_SAVEUSERS = "saveUsers";
    public static final String TM_ACTION_REFRESH = "refresh";
    public static final String TM_ACTION_CONVERT = "convert";
    public static final String TM_ACTION_CONVERT_CANCEL = "cancelConvert";
    public static final String TM_ACTION_VALIDATION_REFRESH = "validationRefresh";
    public static final String TM_ACTION_CONVERT_REFRESH = "convertRefresh";
    public static final String TM_ACTION_SAVE = "save";
    public static final String TM_ACTION_START_IMPORT = "startImport";
    public static final String TM_ACTION_TEST_IMPORT = "testImport";
    public static final String TM_ACTION_CANCEL = "cancel";
    public static final String TM_ACTION_CANCEL_EXPORT = "cancelExport";
    public static final String TM_ACTION_CANCEL_IMPORT = "cancelImport";
    public static final String TM_ACTION_CANCEL_VALIDATION = "cancelValidation";
    public static final String TM_ACTION_CANCEL_CONVERT = "cancelConvert";
    public static final String TM_ACTION_CANCEL_IMPORT_TEST = "cancelImportTest";
    public static final String TM_ACTION_DONE = "done";
    public static final String TM_ACTION_UPLOAD_FILE = "upload";
    public static final String TM_ACTION_IMPORT_FILE = "importFile";
    public static final String TM_ACTION_ANALYZE_FILE = "analyze";
    public static final String TM_ACTION_ANALYZE_TM = "analyzetm";
    public static final String TM_ACTION_SET_IMPORT_OPTIONS = "setImportOptions";
    public static final String TM_ACTION_SET_EXPORT_OPTIONS = "setExportOptions";
    public static final String TM_ACTION_START_EXPORT = "startExport";
    public static final String TM_ACTION_STATISTICS = "statistics";
    public static final String TM_ACTION_MAINTENANCE = "maintenance";
    public static final String TM_ACTION_REINDEX = "reindex";
    public static final String TM_ACTION_REINDEX_START = "reindexStart";
    public static final String TM_ACTION_CANCEL_REINDEX = "reindexCancel";
    public static final String TM_ACTION_DELETE_TUV = "deleteTuv";
    public static final String TM_ACTION_DELETE_TU = "deleteTu";
    public static final String TM_IMPORT_STATUS = "status";
    public static final String TM_REINDEXER = "reindexer";
    public static final String TM_REMOVER = "tmRemover";
    public static final String TM_ACTION_TM_SEARCH = "tmSearchPage";
    public static final String TM_ACTION_SEARCH = "tmSearch";
    public static final String TM_ACTION_REFRESH_PAGE = "refreshPage";
    public static final String TM_ACTION_DELETE_ENTRIES = "deleteEntries";
    public static final String TM_ACTION_ADD_ENTRY = "addEntry";
    public static final String TM_ACTION_EDIT_ENTRY = "editEntry";
    public static final String TM_ACTION_SAVE_ENTRY = "saveEntry";
    public static final String TM_ACTION_APPLY_REPLACE = "applyReplace";
    public static final String TM_ACTION_SEARCH_LEVERAGE_FROM = "leverageFrom";

    public static final String TM_TM_ID = "id";
    public static final String TM_TM_NAME = "name";
    public static final String TM_TM_DOMAIN = "domain";
    public static final String TM_TM_ORGANIZATION = "organization";
    public static final String TM_TM_DESCRIPTION = "description";
    public static final String TM_TM_REMOTE_TM = "remoteTm";
    public static final String TM_TM_GS_EDITON = "gsEdition";
    public static final String TM_TM_REMOTE_TM_PROFILE = "remoteTmProfile";
    public static final String TM_TM_STATUS = "status";
    public static final String TM_UPLOAD_STATUS = "uploadStatus";
    public static final String TM_TM_CHOICE = "oTm";
    public static final String TM_DELETED_SEGMENTS = "deletedSegments";
    public static final String TM_NOT_DELETED_SEGMENTS = "notDeletedSegments";
    public static final String TM_EXIST_NAMES = "existNames";
    public static final String TM_TM3_ID = "tm3id";
    public static final String TM_TYPE = "tmType";
    public static final String TM_ATTRIBUTE = "tmAttribute";
    public static final String TM_AVAILABLE_ATTS = "tmAvailableAtts";
    public static final String TM_TM_ATTS = "tmTMAtts";
    public static final String TMP_AVAILABLE_ATTS = "tmpAvailableAtts";
    public static final String TMP_TMP_ATTS = "tmpTMPAtts";

    public static final String TM_LIST = "tms";
    public static final String TM_KEY = "tm";

    public static final String TM_NAMELIST = "namelist";

    public static final String TM_XML = "xml";

    public static final String TM_ERROR = "TM_ERROR";

    //
    // Aligner Package Creation Constants
    //
    public static final String GAP_OPTIONS = "gapoptions";
    public static final String GAP_FORMATTYPES = "gapformattypes";
    public static final String GAP_RULES = "gaprules";
    public static final String GAP_LOCALES = "gaplocales";
    public static final String GAP_ENCODINGS = "gapencodings";
    public static final String GAP_EXTENSIONS = "gapextensions";
    public static final String GAP_PACKAGE = "gappackage";
    public static final String GAP_PACKAGES = "gappackages";
    public static final String GAP_PACKAGE_NAMES = "gappackagenames";
    public static final String GAP_PACKAGEINFO = "gappackageinfo";
    public static final String GAP_TMS = "gaptms";
    public static final String GAP_FILELIST = "gapfilelist";
    public static final String GAP_FOLDERSRC = "gapfoldersrc";
    public static final String GAP_FOLDERTRG = "gapfoldertrg";
    public static final String GAP_CURRENTFOLDERSRC = "gapcurrentfoldersrc";
    public static final String GAP_CURRENTFOLDERTRG = "gapcurrentfoldertrg";

    public static final String GAP_ACTION = "gapaction";
    public static final String GAP_ACTION_NEWPACKAGE = "newpackage";
    public static final String GAP_ACTION_SELECTFILES = "selectfiles";
    public static final String GAP_ACTION_ALIGNOPTIONS = "alignoptions";
    public static final String GAP_ACTION_CREATEPACKAGE = "createpackage";
    public static final String GAP_ACTION_REMOVEPACKAGE = "removepackage";

    public static final String GAP_ACTION_DOWNLOADPACKAGE = "downloadpackage";
    public static final String GAP_ACTION_SHOWERRORS = "showerrors";

    public static final String GAP_ACTION_UPLOADPACKAGE = "uploadpackage";

    public static final String GAP_ERROR = "GAP_ERROR";

    // Scheduler Constants

    public static final String CRON_ERROR = "cronError";

    public static final String CRON_ACTION = "cronAction";
    public static final String CRON_ACTION_SCHEDULE = "cronActionSchedule";
    public static final String CRON_ACTION_UNSCHEDULE = "cronActionUnschedule";
    public static final String CRON_ACTION_LIST = "cronActionList";
    public static final String CRON_ACTION_LISTALL = "cronActionListAll";

    public static final String CRON_SCHEDULE = "cronSchedule";
    public static final String CRON_OBJECT_NAME = "cronObjectName";
    public static final String CRON_OBJECT_ID = "cronObjectId";
    public static final String CRON_EVENT = "cronEvent";

    public static final String CRON_EVENT_REINDEX_TERMBASE = "cronReindexTB";
    public static final String CRON_EVENT_REINDEX_TM = "cronReindexTM";

    public static final String CRON_MINUTES = "cronMinutes";
    public static final String CRON_HOURS = "cronHours";
    public static final String CRON_DAYSOFMONTH = "cronDaysOfMonth";
    public static final String CRON_MONTHS = "cronMonths";
    public static final String CRON_DAYSOFWEEK = "cronDaysOfWeek";
    public static final String CRON_DAYOFYEAR = "cronDayOfYear";
    public static final String CRON_WEEKOFMONTH = "cronWeekOfMonth";
    public static final String CRON_WEEKOFYEAR = "cronWeekOfYear";
    public static final String CRON_YEAR = "cronYear";

    public static final String CRON_BACKPOINTER = "cronBackPtr";

    // virtual directories
    /**
     * The toplevel dir before any virtual directories in the file storage area.
     */
    public static final String VIRTUALDIR_TOPLEVEL = "GlobalSight";

    /**
     * The virtual dir for uploaded images for image replace.
     */
    public static final String VIRTUALDIR_IMAGE_REPLACE = "/UploadedImages";

    /**
     * The virtual dir or alias that points to CXE docs.
     */
    public static final String VIRTUALDIR_CXEDOCS = "/cxedocs/";

    public static final String VIRTUALDIR_CXEDOCS2 = "/cxedocs2/";

    /**
     * The virtual dir or alias that pionts to fileStorage comment reference.
     */
    static public final String COMMENT_REFERENCE = "CommentReference";
    static public final String COMMENT_REFERENCE2 = "CommentReference2";

    /**
     * The virtual dir for support files.
     */
    public static final String VIRTUALDIR_SUPPORTFILES = "/SupportFiles/";

    /**
     * This identifies a session variable that holds whether the last activity
     * the user clicked on (task details) was review only
     */
    public static final String IS_LAST_ACTIVITY_REVIEW_ONLY = "isLastActivityReviewOnly";

    public static final String ORIGINAL_SORUCE_FILE = "OriginalSourceFile";

    /**
     * Constants used in customize reports page.
     */
    public static final String ACTION_JOB_RANGE = "jobRange";
    public static final String ACTION_JOB_INFO = "jobInfo";
    public static final String ACTION_JOB_CANCEL = "cancel";
    public static final String CUSTOMIZE_REPORTS_JOB_INFO_PARAM_XML = "jobInfoParamXml";
    public static final String CUSTOMIZE_REPORTS_PARAMMAP = "paramMap";
    public static final String CUSTOMIZE_REPORTS_JOB_LIST = "jobList";
    public static final String CUSTOMIZE_REPORTS_TARGETLOCALE_LIST = "targetLocaleList";
    public static final String CUSTOMIZE_REPORTS_PROJECT_LIST = "projectList";
    // These two key used as key in paramMap
    public static final String JOB_RANGE_PARAM = "jobRangeParam";
    public static final String JOB_INFO_PARAM = "jobInfoParam";
    public static final String TARGET_LOCALE_PARAM = "targetLocaleParam";
    public static final String WORKFLOW_STATUS_PARAM = "workflowStatusParam";
    public static final String LABEL_BUNDLE_PARAM = "labelBundleParam";
    public static final String DATE_FORMAT_PARAM = "dateFormatParam";

    // For "Lisa QA report" issue
    public static String TARGET_LANGUAGE = "targetLang";
    public static String DATE_FORMAT = "dateFormat";

    // For "Logon to task detail page from email"
    public static String LOGIN_FROM = "loginFrom";
    public static String LOGIN_FROM_EMAIL = "fromEmail";

    public static String LOGIN_FORWARD_URL = "forwardUrl";

    // For activity page navigation
    public static String IS_FROM_ACTIVITY = "isfromactivity";

    public static String DOWLOAD_DELAY_TIME_TABLE = "downloadDelayTimeTable";
    public static String TASK_COMPLETE_DELAY_TIME_TABLE = "taskCompleteDelayTimeTable";

    public static String IS_USE_IN_CONTEXT = "isUseInContext";
    public static String LEVERAGE_EXACT_ONLY = "exactMatchOnly";
    public static String IS_IN_CONTEXT_MATCH = "isInContextMatch";

    public static final String REQUEST_METHOD_GET = "get";
    public static final String RADIO_BUTTON = "radioBtn";
    public static final String RADIO_TM_ID = "TMId";
    public static final String DASHBOARD_ACTIVITY = "activityDashboard";
    public static final String EXPORTING_WORKFLOW_NUMBER = "exportingWorkflowNumber";
    public static final String IS_ADMIN = "isAdmin";

    public static final String PROPAGATE_ACTION = "propagateAction";
    public static final String PROPAGATE_ACTION_FIND = "findRepeatedSegments";
    public static final String PROPAGATE_ACTION_UNMARK = "unmarkRepeatedSegments";
    public static final String PTAGS_ACTION_FIND = "findPTagsSegments";
    public static final String PTAGS_ACTION_UNMARK = "unmarkPTagsSegments";

    public static final String TM_STATUS_DEFAULT = "";
    public static final String TM_STATUS_CONVERTING = "Converting";
    public static final String TM_STATUS_CONVERTED_SUCCESS = "Converted";
    public static final String TM_STATUS_CONVERTED_FAIL = "Failed";
    public static final String TM_STATUS_CONVERTED_CANCELLED = "Cancelled";
    public static final String TM_STATUS_CONVERTED_STOPPED = "Conversion Stopped";

    public static final String GS_EDITION_ALL = "allGSEdition";

    public static final String JOB_GROUP_NAME = "name";
    public static final String JOB_GROUP_PROJECT = "project";
    public static final String JOB_GROUP_SOURCELOCAL = "sourceLocale";
    public static final String JOB_GROUP_EXISTNAMES = "existNames";
    public static final String JOB_GROUP_SAVE = "save";
    public static final String JOB_GROUP_PAGING_SCRIPTLET = "pagingJSPData";
    public static final String JOB_GROUP_DETAIL = "jobList";
    public static final String NUM_OF_JOB_GROUPS = "numOfJobGroups";
    public static final String JOB_GROUP_LIST_START = "jobGroupListStart";
    public static final String JOB_GROUP_SORT_PARAM = "sort";
    public static final String JOB_GROUP_SELECT_RADIO_BTN = "selectedIds";
    public static final String JOB_GROUP_SORT_COLUMN = "sortColumn";
    public static final String JOB_GROUP_SORT_ASCENDING = "sortAscending";
    public static final String JOB_GROUP_CHECK = "checkJobGroup";
    
    public static final String TM_ACTION_DELETE_BAD_TUV = "deleteBadTuv";
    public static final String TM_PARAM_DELETE_BAD_TUV_BYID  = "deleteBadTucById";
    public static final String TM_PARAM_DELETE_BAD_TUV_BYSOURCE  = "deleteBadTucBySource";
    /*
     * Common actions in web UI
     */
    public static final String ACTION_STRING = "action";
    public static final String FILTER_SEARCH = "filterSearch";
    public static final String ACTION_NEW = "new";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_CANCEL = "cancel";
}
