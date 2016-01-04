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
package com.globalsight.everest.permission;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class holds all the static definitions of permissions.
 * 
 * NOTE: When adding new permission constants to this class, be sure to update
 * the static section where the HashMap is filled.
 */
public class Permission
{
    static private final Logger logger = Logger.getLogger(Permission.class);

    /**
     * Static permission definitions -- see below for mapping when you add a
     * permission, you must add it to the allAllPermissions() call otherwise it
     * won't be recognized.
     */
    static public final String LOGS_VIEW = "logs.view";
    static public final String OPERATION_LOG_VIEW = "operationLog.view";
    static public final String SHUTDOWN_SYSTEM = "shutdown.system";
    static public final String LOCALE_PAIRS_VIEW = "localePairs.view";
    static public final String LOCALE_PAIRS_REMOVE = "localePairs.remove";
    static public final String LOCALE_PAIRS_NEW = "localePairs.new";
    static public final String LOCALE_PAIRS_EXPORT = "localePairs.export";
    static public final String LOCALE_PAIRS_IMPORT = "localePairs.import";
    static public final String LOCALE_NEW = "locale.new";
    static public final String ACTIVITY_TYPES_VIEW = "activityTypes.view";
    static public final String ACTIVITY_TYPES_REMOVE = "activityTypes.remove";
    static public final String ACTIVITY_TYPES_EDIT = "activityTypes.edit";
    static public final String ACTIVITY_TYPES_NEW = "activityTypes.new";
//    static public final String AUTOMATIC_ACTIONS_VIEW = "automaticActions.view";
//    static public final String AUTOMATIC_ACTIONS_REMOVE = "automaticActions.remove";
//    static public final String AUTOMATIC_ACTIONS_EDIT = "automaticActions.edit";
//    static public final String AUTOMATIC_ACTIONS_NEW = "automaticActions.new";
    static public final String CURRENCY_VIEW = "currency.view";
    static public final String CURRENCY_EDIT = "currency.edit";
    static public final String CURRENCY_NEW = "currency.new";
    static public final String COMPANY_VIEW = "company.view";
    static public final String COMPANY_EDIT = "company.edit";
    static public final String COMPANY_NEW = "company.new";
    static public final String COMPANY_REMOVE = "company.remove";
    static public final String COMPANY_MIGRATE = "company.migrate";
    static public final String PROJECTS_MANAGE = "projects.manage";
    static public final String PROJECTS_MANAGE_WORKFLOWS = "projects.manage.workflows";
    static public final String CALENDAR_ADMINISTER = "calendar.administer";
    static public final String WORKFLOW_CANCEL = "workflow.cancel";
    static public final String RATES_VIEW = "rates.view";
    static public final String RATES_EDIT = "rates.edit";
    static public final String RATES_NEW = "rates.new";
    static public final String SYS_CAL_VIEW = "systemCalendar.view";
    static public final String SYS_CAL_DUP = "systemCalendar.dup";
    static public final String SYS_CAL_DEFAULT = "systemCalendar.default";
    static public final String SYS_CAL_REMOVE = "systemCalendar.remove";
    static public final String SYS_CAL_EDIT = "systemCalendar.edit";
    static public final String SYS_CAL_NEW = "systemCalendar.new";
    static public final String USER_CAL_VIEW = "userCalendar.view";
    static public final String USER_CAL_EDIT = "userCalendar.edit";
    static public final String USER_CAL_EDIT_YOURS = "userCalendar.edit.yours";
    static public final String HOLIDAY_VIEW = "holiday.view";
    static public final String HOLIDAY_REMOVE = "holiday.remove";
    static public final String HOLIDAY_EDIT = "holiday.edit";
    static public final String HOLIDAY_NEW = "holiday.new";
    static public final String PERMGROUPS_VIEW = "permgroups.view";
    static public final String PERMGROUPS_REMOVE = "permgroups.remove";
    static public final String PERMGROUPS_EDIT = "permgroups.edit";
    static public final String PERMGROUPS_NEW = "permgroups.new";
    static public final String PERMGROUPS_DETAILS = "permgroups.details";
    static public final String USERS_VIEW = "users.view";
    static public final String USERS_REMOVE = "users.remove";
    static public final String USERS_EDIT = "users.edit";
    static public final String USERS_EDIT_ASSIGN_ANY_PERMGROUPS = "users.edit.assignAnyPermGroups";
    static public final String USERS_ACCESS_CCEMAIL = "users.access.ccEmail";
    static public final String USERS_ACCESS_BCCEMAIL = "users.access.bccEmail";
    static public final String USERS_NEW = "users.new";
    static public final String USERS_IMPORT = "users.import";
    static public final String USERS_EXPORT = "users.export";
    static public final String USERS_PROJECT_MEMBERSHIP = "users.projects.membership";
    static public final String TM_VIEW = "tm.view";
    static public final String TM_BROWSER = "tm.browser";
    static public final String TM_STATS = "tm.stats";
    static public final String TM_MAINTENANCE = "tm.maintenance";
    static public final String TM_IMPORT = "tm.import";
    static public final String TM_EXPORT = "tm.export";
    static public final String TM_REINDEX = "tm.reindex";
    static public final String TM_DUPLICATE = "tm.duplicate";
    static public final String TM_EDIT = "tm.edit";
    static public final String TM_NEW = "tm.new";
    static public final String TM_DELETE = "tm.delete";
    static public final String TM_SEARCH = "tm.search";
    static public final String TM_DELETE_ENTRY = "tm.deleteEntries";
    static public final String TM_ADD_ENTRY = "tm.addEntries";
    static public final String TM_EDIT_ENTRY = "tm.editEntries";
    static public final String TM_SEARCH_ADVANCED = "tm.search.advanced";

    static public final String TM_ENABLE_TM_ATTRIBUTES = "tm.enableTMAttributes";
    static public final String TMP_VIEW = "tmp.view";
    static public final String TMP_EDIT = "tmp.edit";
    static public final String TMP_NEW = "tmp.new";
    static public final String TMP_REMOVE = "tmp.remove";
    static public final String TERMINOLOGY_VIEW = "terminology.view";
    static public final String TERMINOLOGY_STATS = "terminology.stats";
    static public final String TERMINOLOGY_INDEXES = "terminology.indexes";
    static public final String TERMINOLOGY_REMOVE = "terminology.remove";
    static public final String TERMINOLOGY_DUPLICATE = "terminology.duplicate";
    static public final String TERMINOLOGY_EDIT = "terminology.edit";
    static public final String TERMINOLOGY_NEW = "terminology.new";
    static public final String TERMINOLOGY_BROWSE = "terminology.browse";
    static public final String TERMINOLOGY_IMPORT = "terminology.import";
    static public final String TERMINOLOGY_EXPORT = "terminology.export";
    static public final String TERMINOLOGY_MAINTENANCE = "terminology.maintenance";
    static public final String TERMINOLOGY_INPUT_MODELS = "terminology.inputModels";
    static public final String TERMINOLOGY_SEARCH = "terminology.search";
    static public final String PROJECTS_VIEW = "projects.view";
    static public final String PROJECTS_IMPORT = "projects.import";
    static public final String PROJECTS_EXPORT = "projects.export";
    static public final String PROJECTS_EDIT = "projects.edit";
    static public final String PROJECTS_EDIT_PM = "projects.edit.pm";
    static public final String PROJECTS_NEW = "projects.new";
    static public final String PROJECTS_REMOVE = "projects.remove";
    static public final String WORKFLOWS_VIEW = "workflows.view";
    static public final String WORKFLOWS_DUPLICATE = "workflows.duplicate";
    static public final String WORKFLOWS_REMOVE = "workflows.remove";
    static public final String WORKFLOWS_EDIT = "workflows.edit";
    static public final String WORKFLOWS_NEW = "workflows.new";
    static public final String LOCPROFILES_VIEW = "locprofiles.view";
    static public final String LOCPROFILES_REMOVE = "locprofiles.remove";
    static public final String LOCPROFILES_DUP = "locprofiles.duplicate";
    static public final String LOCPROFILES_DETAILS = "locprofiles.details";
    static public final String LOCPROFILES_EDIT = "locprofiles.edit";
    static public final String LOCPROFILES_NEW = "locprofiles.new";
    static public final String SUPPORT_FILES_VIEW = "supportfiles.view";
    static public final String SUPPORT_FILES_REMOVE = "supportfiles.remove";
    static public final String SUPPORT_FILES_UPLOAD = "supportfiles.upload";
    static public final String SNIPPET_IMPORT = "snippet.import";
    static public final String SYSTEM_PARAMS = "system.parameters";
    static public final String FILE_PROFILES_VIEW = "fileprofiles.view";
    static public final String FILE_PROFILES_REMOVE = "fileprofiles.remove";
    static public final String FILE_PROFILES_EDIT = "fileprofiles.edit";
    static public final String FILE_PROFILES_NEW = "fileprofiles.new";
    static public final String FILE_EXT_VIEW = "fileextention.view";
    static public final String FILE_EXT_NEW = "fileextention.new";
    static public final String FILE_EXT_REMOVE = "fileextention.remove";
    static public final String XMLRULE_VIEW = "xmlrule.view";
    static public final String XMLRULE_DUP = "xmlrule.duplicate";
    static public final String XMLRULE_EDIT = "xmlrule.edit";
    static public final String XMLRULE_NEW = "xmlrule.new";
    static public final String XMLRULE_REMOVE = "xmlrule.remove";
    static public final String XMLDTD_VIEW = "xmldtd.view";
    static public final String XMLDTD_EDIT = "xmldtd.edit";
    static public final String XMLDTD_NEW = "xmldtd.new";
    static public final String XMLDTD_REMOVE = "xmldtd.remove";
    static public final String SGMLRULE_VIEW = "sgmlrule.view";
    static public final String SGMLRULE_UPLOAD = "sgmlrule.upload";
    static public final String SGMLRULE_CREATE = "sgmlrule.create";
    static public final String SGMLRULE_REMOVE = "sgmlrule.remove";
    static public final String SGMLRULE_EDIT = "sgmlrule.edit";
    static public final String IMPORT = "import";
    static public final String SERVICEWARE_IMPORT = "serviceware.import";
    static public final String SNIPPET_ADD = "snippet.add";
    static public final String SNIPPET_EDIT = "snippet.edit";
//    static public final String TEAMSITE_SERVER_VIEW = "teamsiteserver.view";
//    static public final String TEAMSITE_SERVER_REMOVE = "teamsiteserver.remove";
//    static public final String TEAMSITE_SERVER_CREATE = "teamsiteserver.create";
//    static public final String TEAMSITE_SERVER_EDIT = "teamsiteserver.edit";
//    static public final String TEAMSITE_SERVER_NEW = "teamsiteserver.new";
//    static public final String TEAMSITE_PROFILES_VIEW = "teamsiteprofiles.view";
//    static public final String TEAMSITE_PROFILES_REMOVE = "teamsiteprofiles.remove";
//    static public final String TEAMSITE_PROFILES_NEW = "teamsiteprofiles.new";
    static public final String DATABASE_INTEGRATION = "database.integration";
    static public final String EXPORT_LOC_VIEW = "exportloc.view";
    static public final String EXPORT_LOC_REMOVE = "exportloc.remove";
    static public final String EXPORT_LOC_DEFAULT = "exportloc.default";
    static public final String EXPORT_LOC_EDIT = "exportloc.edit";
    static public final String EXPORT_LOC_NEW = "exportloc.new";
    static public final String VIGNETTE_IMPORT = "vignette.import";
    static public final String DOCUMENTUM_IMPORT = "documentum.import";
    static public final String CORPUS_ALIGNER_VIEW = "corpusaligner.view";
    static public final String CORPUS_ALIGNER_CREATE = "corpusaligner.create";
    static public final String CORPUS_ALIGNER_DOWNLOAD = "corpusaligner.download";
    static public final String CORPUS_ALIGNER_UPLOAD = "corpusaligner.upload";
    static public final String JOB_SCOPE_ALL = "jobscope.all";
    static public final String JOBS_VIEW = "jobs.view";
    static public final String JOBS_SEARCH_REPLACE = "jobs.searchreplace";
    static public final String JOBS_CHANGE_WFM = "jobs.changewfm";
    static public final String JOB_CHANGE_NAME = "job.changename";
    static public final String JOBS_DISCARD = "jobs.discard";
    static public final String JOBS_CLEAR_ERRORS = "jobs.clearerrors";
    static public final String JOBS_DISPATCH = "jobs.dispatch";
    static public final String JOBS_EXPORT = "jobs.export";
    static public final String JOBS_EXPORT_SOURCE = "jobs.exportsource";
    static public final String JOBS_REEXPORT = "jobs.reexport";
    static public final String JOBS_DETAILS = "jobs.details";
    static public final String JOBS_VIEW_ERROR = "jobs.viewerror";
    static public final String JOBS_ARCHIVE = "jobs.archive";
    static public final String JOBS_DOWNLOAD = "jobs.download";
    static public final String JOBS_EXPORT_DOWNLOAD = "jobs.export.download";
    static public final String JOBS_MAKE_READY = "jobs.makeready";
    static public final String JOBS_RECREATE = "jobs.recreate";
    static public final String JOBS_PLANNEDCOMPDATE = "jobs.plannedcompdate";
    //GBS-3692
    static public final String JOBS_GROUP = "jobs.group";
    static public final String JOBS_NEWGROUP = "jobs.newgroup";
    static public final String JOBS_REMOVEGROUP = "jobs.removegroup";
    static public final String JOBS_ADDJOBTOGROUP = "jobs.addjobtogroup";
    static public final String JOBS_REMOVEJOBFROMGROUP = "jobs.removejobfromgroup";
//    static public final String FILE_PROFILES_SEARCH = "fileprofiles.search";//GBS-2875

    // For sla report issue
    static public final String JOBS_ESTIMATEDCOMPDATE = "jobs.estimatedcompdate";
    static public final String JOBS_ESTIMATEDTRANSLATECOMPDATE = "jobs.estimatedtranslatecompdate";

    static public final String JOB_COMMENTS_VIEW = "job.comments.view";
    static public final String JOB_COMMENTS_EDIT = "job.comments.edit";
    static public final String JOB_COMMENTS_NEW = "job.comments.new";
    static public final String JOB_COSTING_VIEW = "job.costing.view";

    static public final String ACTIVITIES_COMMENTS_DOWNLOAD = "activities.comments.download";

    // for job costing issue
    static public final String COSTING_EXPENSE_VIEW = "job.costing.expense.view";
    static public final String COSTING_REVENUE_VIEW = "job.costing.revenue.view";

    static public final String JOB_COSTING_EDIT = "job.costing.edit";
    static public final String JOB_COSTING_REPORT = "job.costing.report";
    static public final String JOB_FILES_VIEW = "job.files.view";
    static public final String JOB_FILES_EDIT = "job.files.edit";
    static public final String JOB_FILES_DOWNLOAD = "job.files.download";
    // for quote email issue
    static public final String JOB_QUOTE_VIEW = "job.quote.view";
    static public final String JOB_QUOTE_SEND = "job.quote.send";
    static public final String JOB_QUOTE_STATUS_VIEW = "job.quote.status.view";
    static public final String JOB_WORKFLOWS_VIEW = "job.workflows.view";
    static public final String JOB_WORKFLOWS_DISCARD = "job.workflows.discard";
    static public final String JOB_WORKFLOWS_VIEW_ERROR = "job.workflows.viewerror";
    static public final String JOB_WORKFLOWS_WORDCOUNT = "job.workflows.wordcount";
    static public final String JOB_WORKFLOWS_TRANSLATED_TEXT = "job.workflows.translated.text";
    static public final String JOB_WORKFLOWS_DETAIL_STATISTICS = "job.workflows.detailStatistics";
    static public final String JOB_WORKFLOWS_RATEVENDOR = "job.workflows.ratevendor";
    static public final String JOB_WORKFLOWS_ARCHIVE = "job.workflows.archive";
    static public final String JOB_WORKFLOWS_DETAILS = "job.workflows.details";
    static public final String JOB_WORKFLOWS_EXPORT = "job.workflows.export";
    static public final String JOB_WORKFLOWS_EXPORT_DOWNLOAD = "job.workflows.export.download";
    static public final String JOB_WORKFLOWS_ADD = "job.workflows.add";
    static public final String JOB_WORKFLOWS_EDIT = "job.workflows.edit";
    static public final String JOB_WORKFLOWS_DISPATCH = "job.workflows.dispatch";
    static public final String JOB_WORKFLOWS_REASSIGN = "job.workflows.reassign";
    static public final String JOB_WORKFLOWS_SKIP = "job.workflows.skip";
    static public final String JOB_WORKFLOWS_ESTCOMPDATE = "job.workflows.estcompdate";
    static public final String JOB_WORKFLOWS_ESTREVIEWSTART = "job.workflows.estreviewstart";
    static public final String JOB_WORKFLOWS_PLANNEDCOMPDATE = "job.workflows.plannedcompdate";
    static public final String JOB_WORKFLOWS_EDITEXPORTLOC = "job.workflows.editexportloc";
    static public final String JOB_WORKFLOWS_SUMMARY_STATISTICS = "job.workflows.summaryStatistics";
    static public final String JOB_WORKFLOWS_PRIORITY = "job.workflows.priority";
    static public final String ACTIVITY_DASHBOARD_VIEW = "activity.dashboard.view";
    static public final String ACTIVITIES_VIEW = "activities.view";
    static public final String ACTIVITIES_OFFLINEUPLOAD = "activities.offlineUpload";
    static public final String ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY = "activities.offlineUpload.fromAnyActivity";
    static public final String ACTIVITIES_ACCEPT = "activities.accept";
    static public final String ACTIVITIES_ACCEPT_ALL = "activities.accept.all";
    static public final String ACTIVITIES_BATCH_COMPLETE_ACTIVITY = "activities.batch.complete.activity";
    static public final String ACTIVITIES_BATCH_COMPLETE_WORKFLOW = "activities.batch.complete.workflow";
    static public final String ACTIVITIES_DOWNLOAD_ALL = "activities.download.all";
    static public final String ACTIVITIES_DOWNLOAD_COMBINED = "activities.download.combined";
    static public final String ACTIVITIES_REJECT_BEFORE_ACCEPTING = "activities.rejectBeforeAccepting";
    static public final String ACTIVITIES_REJECT_AFTER_ACCEPTING = "activities.rejectAfterAccepting";
    static public final String ACTIVITIES_EXPORT = "activities.export";
    static public final String ACTIVITIES_EXPORT_INPROGRESS = "activities.export.inprogress";
    static public final String ACTIVITIES_WORKOFFLINE = "activities.workoffline";
    static public final String ACTIVITIES_UPLOAD_SUPPORT_FILES = "activities.upload.supportfiles";
    static public final String ACTIVITIES_SEARCHREPLACE = "activities.searchreplace";
    static public final String ACTIVITIES_DETAIL_STATISTICS = "activities.detailStatistics";
    static public final String ACTIVITIES_FILES_VIEW = "activities.files.view";
    static public final String ACTIVITIES_FILES_EDIT = "activities.files.edit";
    static public final String ACTIVITIES_JOB_COMMENTS_VIEW = "activities.jobcomments.view";
    static public final String ACTIVITIES_JOB_COMMENTS_EDIT = "activities.jobcomments.edit";
    static public final String ACTIVITIES_JOB_COMMENTS_NEW = "activities.jobcomments.new";
    static public final String ACTIVITIES_JOB_COMMENTS_DOWNLOAD = "activities.jobcomments.download";
    static public final String ACTIVITIES_COMMENTS_VIEW = "activities.comments.view";
    static public final String ACTIVITIES_COMMENTS_EDIT = "activities.comments.edit";
    static public final String ACTIVITIES_COMMENTS_NEW = "activities.comments.new";
    static public final String ACTIVITIES_COMMENTS_JOB = "activities.comments.jobComments";
    static public final String ACTIVITIES_SUMMARY_STATISTICS = "activities.summaryStatistics";
    static public final String ACTIVITIES_SECONDARYTARGETFILE = "activities.secondaryTargetFile";
    static public final String ACTIVITIES_CROWDSIGHT = "activities.crowdsight";
    static public final String ACTIVITIES_TM_SEARCH = "activities.tm.search";
    static public final String ACTIVITIES_TB_SEARCH = "activities.tb.search";
    static public final String REPORTS_MAIN = "reports.main";
   // static public final String REPORTS_ADMIN = "reports.admin";
   // static public final String REPORTS_COMPOSER = "reports.composer";
    static public final String REPORTS_CUSTOM_EXTERNAL = "reports.custom.external";
    static public final String REPORTS_CUSTOM = "reports.custom";
    static public final String VENDORS_NEW = "vendors.new";
    static public final String VENDORS_VIEW = "vendors.view";
    static public final String VENDORS_EDIT = "vendors.edit";
    static public final String VENDORS_REMOVE = "vendors.remove";
    static public final String VENDORS_DETAILS = "vendors.details";
    static public final String VENDORS_CUSTOMFORM = "vendors.customform";
    static public final String VENDORS_RATING_NEW = "vendors.rating.new";
    static public final String VENDORS_RATING_VIEW = "vendors.rating.view";
    static public final String VENDORS_RATING_EDIT = "vendors.rating.edit";
    static public final String VENDORS_RATING_REMOVE = "vendors.rating.remove";
    static public final String CONTENT_MANAGER = "contentmanager";
    static public final String CUSTOMER_UPLOAD = "customer.upload";
    static public final String CUSTOMER_UPLOAD_VIA_WEBSERVICE = "customer.upload.via.webservice";
    static public final String GET_ALL_PROJECTS = "projects.getall";
    static public final String GET_PROJECTS_I_MANAGE = "projects.getmanage";
    static public final String GET_PROJECTS_I_BELONG = "projects.getbelong";

//    static public final String ACCOUNT_DOWNLOAD_ALL_OFFLINE_FILES = "account.download.all.offline.files";
    static public final String ACCOUNT_NOTIFICATION_SYSTEM = "account.notification.system";
    static public final String ACCOUNT_NOTIFICATION_WFMGMT = "account.notification.wfmgmt";
    static public final String ACCOUNT_NOTIFICATION_GENERAL = "account.notification.general";
    static public final String ACCOUNT_NOTIFICATION_NOMATCHES = "account.notification.noMatchesInJobEmail";
    static public final String ACCOUNT_NOTIFICATION_REPETITIONS = "account.notification.repetitionsInJobEmail";
    static public final String SOURCE_PAGE_EDIT = "sourcepage.edit";
    static public final String COMMENT_ACCESS_RESTRICTED = "comment.access.restricted";
    static public final String JOB_SOURCE_WORDCOUNT_TOTAL = "job.source.wordcount.total";

    static public final String COMMENT_EDIT_ALL_COMMENTS = "comment.access.editallcomments";
    static public final String FILE_PROFILES_SEE_ALL = "fileprofiles.seeAll";
    static public final String USERS_VIEW_SEE_ALL = "users.view.seeAll";

    // add a permission for each report!
    static public final String REPORTS_WORD_COUNT = "reports.word_count";
    static public final String REPORTS_JOB_COST = "reports.job_cost";
    static public final String REPORTS_TM = "reports.tm";
    static public final String REPORTS_WF_STATUS = "reports.wf_status";
    static public final String REPORTS_JOB_DETAILS = "reports.job_details";
    static public final String REPORTS_AVG_PER_COMP = "reports.avg_per_comp";
    static public final String REPORTS_MISSING_TERMS = "reports.missing_terms";
    static public final String REPORTS_TERM_AUDIT = "reports.term_audit";
    static public final String REPORTS_CUSTOMIZE = "reports.customize";
    static public final String REPORTS_DELL_JOB_STATUS = "reports.dell.job_status";
    static public final String REPORTS_DELL_ACT_DUR = "reports.dell.act_dur";
    static public final String REPORTS_DELL_ONLINE_JOBS = "reports.dell.online_jobs";
    static public final String REPORTS_DELL_ONLINE_JOBS_FOR_IP_TRANSLATOR = "reports.dell.online_jobs_for_ip_translator";
    static public final String REPORTS_DELL_ONLINE_REVIEW_STATUS = "reports.dell.online_review_status";
    static public final String REPORTS_DELL_ONLINE_JOBS_RECALC = "reports.dell.online_jobs.recalculate";
    static public final String REPORTS_DELL_VENDOR_PO = "reports.dell.vendor_po";
    static public final String REPORTS_COMMENT = "reports.comment";
    static public final String REPORTS_DELL_REVIEWER_VENDOR_PO = "reports.dell.reviewer.vendor_po";
    static public final String REPORTS_DELL_FILE_LIST = "reports.dell.file_list";
    // For Lisa QA report issue
    static public final String REPORTS_REVIEWER_LISA_QA = "reports.reviewer.lisa_qa";

    static public final String REPORTS_COMMENTS_ANALYSIS = "reports.comments.analysis";
    static public final String REPORTS_TRANSLATIONS_EDIT = "reports.translations.edit";
    static public final String REPORTS_POST_REVIEW_QA = "reports.post.review.qa";
    static public final String REPORTS_TRANSLATIONS_VERIFICATION = "reports.translations.verification";
    static public final String REPORTS_LANGUAGE_SIGN_OFF = "reports.language.sign.off";
    static public final String REPORTS_LANGUAGE_SIGN_OFF_SIMPLE = "reports.language.sign.off.simple";
    static public final String REPORTS_CHARACTER_COUNT = "reports.character.count";

    static public final String REPORTS_TRANSLATION_PROGRESS = "reports.translation.progress";
    static public final String REPORTS_SUMMARY = "reports.summary";

    // For sla report issue
    static public final String REPORTS_SLA = "reports.sla";

    // For Segmentation Rule
    static public final String SEGMENTATIONRULE_VIEW = "segmentationrule.view";
    static public final String SEGMENTATIONRULE_NEW = "segmentationrule.new";
    static public final String SEGMENTATIONRULE_EDIT = "segmentationrule.edit";
    static public final String SEGMENTATIONRULE_EXPORT = "segmentationrule.export";
    static public final String SEGMENTATIONRULE_REMOVE = "segmentationrule.remove";
    static public final String SEGMENTATIONRULE_DUP = "segmentationrule.duplicate";

    // For implemented comments check report issue
    static public final String REPORTS_IMPLEMENTED_COMMENTS_CHECK = "reports.implemented.comments.check";

    // For "Add job id into online job report" issue
    static public final String REPORTS_DELL_ONLINE_JOBS_ID = "reports.dell.online_jobs.id";

    // For job attribute report
    static public final String JOB_ATTRIBUTE_REPORT = "reports.jobAttribute";

    // For "Grey out the edit buttons once the quote approve has been selected"
    // issue
    static public final String JOB_COSTING_REEDIT = "job.costing.reedit";

    // For " Quotation process for WebEx " issue
    static public final String JOB_QUOTE_APPROVE = "job.quote.approve";
    static public final String JOB_QUOTE_PONUMBER_VIEW = "job.quote.ponumber.view";
    static public final String JOB_QUOTE_PONUMBER_EDIT = "job.quote.ponumber.edit";

    static public final String IN_CONTEXT_MATCH = "tmProfile.in.context.match";

    // For " add download button to my activities " issue
    static public final String ACTIVITIES_DOWNLOAD = "activities.download";
    static public final String ACTIVITIES_EXPORT_DOWNLOAD = "activities.export.download";
    static public final String CHANGE_OWN_PASSWORD = "activities.change.own.password";
    static public final String CHANGE_OWN_EMAIL = "activities.change.own.email";

    static public final String MTP_VIEW = "mtp.view";
    static public final String MTP_NEW = "mtp.new";
    static public final String MTP_EDIT = "mtp.edit";
    static public final String MTP_REMOVE = "mtp.remove";
    static public final String MTP_EXPORT = "mtp.export";
    static public final String MTP_IMPORT = "mtp.import";

    static public final String SERVICE_TM_GET_ALL_TMPROFILES = "service.tm.getAllTMProfiles";
    static public final String SERVICE_TB_CREATE_ENTRY = "service.tb.createEntries";
    static public final String SERVICE_TB_SEARCH_ENTRY = "service.tb.searchEntries";
    static public final String SERVICE_TB_EDIT_ENTRY = "service.tb.editEntries";
    static public final String SERVICE_TB_GET_ALL_TB = "service.tb.getAllTermbases";

    static public final String CONNECT_TO_CVS = "desktopicon.connect.cvs";

    // For CVS function
    static public final String CVS_ADMIN = "cvs.admin";
    static public final String CVS_OPERATE = "cvs.operate";

    static public final String CVS_Servers = "cvs.servers";
    static public final String CVS_Servers_NEW = "cvs.servers.new";
    static public final String CVS_Servers_EDIT = "cvs.servers.edit";
    static public final String CVS_Servers_REMOVE = "cvs.servers.remove";

    static public final String CVS_MODULES = "cvs.modules";
    static public final String CVS_MODULES_NEW = "cvs.modules.new";
    static public final String CVS_MODULES_EDIT = "cvs.modules.edit";
    static public final String CVS_MODULES_REMOVE = "cvs.modules.remove";
    static public final String CVS_MODULES_CHECKOUT = "cvs.modules.checkout";

    static public final String CVS_MODULE_MAPPING = "cvs.module.mapping";
    static public final String CVS_MODULE_MAPPING_NEW = "cvs.module.mapping.new";
    static public final String CVS_MODULE_MAPPING_EDIT = "cvs.module.mapping.edit";
    static public final String CVS_MODULE_MAPPING_REMOVE = "cvs.module.mapping.remove";

    static public final String CVS_FILE_PROFILES = "cvs.file.profiles";
    static public final String CVS_FILE_PROFILES_NEW = "cvs.file.profiles.new";
    static public final String CVS_FILE_PROFILES_EDIT = "cvs.file.profiles.edit";
    static public final String CVS_FILE_PROFILES_REMOVE = "cvs.file.profiles.remove";

    // For attribute
    static public final String ATTRIBUTE_VIEW = "attribute.view";
    static public final String ATTRIBUTE_NEW = "attribute.new";
    static public final String ATTRIBUTE_REMOVE = "attribute.remove";
    static public final String ATTRIBUTE_EDIT = "attribute.edit";
    static public final String ATTRIBUTE_GROUP_VIEW = "attributeGroup.view";
    static public final String ATTRIBUTE_GROUP_NEW = "attributeGroup.new";
    static public final String ATTRIBUTE_GROUP_REMOVE = "attributeGroup.remove";
    static public final String ATTRIBUTE_GROUP_EDIT = "attributeGroup.edit";
    static public final String JOB_ATTRIBUTE_VIEW = "job.attribute.view";
    static public final String JOB_ATTRIBUTE_EDIT = "job.attribute.edit";

    // For locale languages
    static public final String UILOCALE_VIEW = "uilocale.view";
    static public final String UILOCALE_REMOVE = "uilocale.remove";
    static public final String UILOCALE_DOWNLOAD_RES = "uilocale.downloadres";
    static public final String UILOCALE_UPLOAD_RES = "uilocale.uploadres";
    static public final String UILOCALE_SET_DEFAULT = "uilocale.setdefault";
    static public final String UILOCALE_NEW = "uilocale.new";

    // For filter configuration.
    static public final String FILTER_CONFIGURATION_VIEW = "filter.configuration";
    static public final String FILTER_CONFIGURATION_REMOVE_FILTERS = "filter.configuration.remove.filters";
    static public final String FILTER_CONFIGURATION_ADD_FILTER = "filter.configuration.add.filter";
    static public final String FILTER_CONFIGURATION_EDIT_FILTER = "filter.configuration.edit.filter";
    static public final String FILTER_CONFIGURATION_EXPORT_FILTERS = "filter.configuration.export.filters";
    static public final String FILTER_CONFIGURATION_IMPORT_FILTERS = "filter.configuration.import.filters";

    static public final String GSEDITION_VIEW = "gsedition.view";
    static public final String GSEDITION_REMOVE = "gsedition.remove";
    static public final String GSEDITION_EDIT = "gsedition.edit";
    static public final String GSEDITION_NEW = "gsedition.new";
//    static public final String GSEDITION_ACTIONS_VIEW = "gseditionActions.view";
//    static public final String GSEDITION_ACTIONS_REMOVE = "gseditionActions.remove";
//    static public final String GSEDITION_ACTIONS_EDIT = "gseditionActions.edit";
//    static public final String GSEDITION_ACTIONS_NEW = "gseditionActions.new";

    static public final String ADD_SOURCE_FILES = "sourceFiles.add";
    static public final String DELETE_SOURCE_FILES = "sourceFiles.delete";
    static public final String EDIT_SOURCE_FILES = "sourceFiles.edit";

    // For BlogSmith.
    static public final String RSS_READER = "rss.reader";
    static public final String RSS_JOB = "rss.job";

    static public final String SET_DEFAULT_ROLES = "admin.setDefaultRoles";

    // For Job Search
    static public final String JOB_SCOPE_MYPROJECTS = "jobscope.myProjects";

    static public final String JOB_UPDATE_LEVERAGE = "jobs.updateLeverage";
    static public final String JOB_UPDATE_WORD_COUNTS = "jobs.updateWordCounts";
    static public final String ACTIVITIES_UPDATE_LEVERAGE = "activities.updateLeverage";
    
    // for COTI Api
    static public final String COTI_JOB = "coti.job";

    // For Job creation
    static public final String CREATE_JOB = "createjob";
    static public final String CREATE_JOB_NO_APPLET = "createJobNoApplet";
    
    //For Job Scorecard
    static public final String EDIT_SCORECARD = "editScorecard";
    static public final String REPORTS_SCORECARD = "reports.scorecard";
    static public final String VIEW_SCORECARD = "viewScorecard";
    static public final String ELOQUA = "eloqua";
    static public final String MIND_TOUCH = "mindtouch";
    static public final String GIT_CONNECTOR = "gitConnector";
    static public final String BLAISE_CONNECTOR = "blaiseConnector";

    // Limit the range of global LP permissions,super LocalizationParticipant
    // user can only edit below permissions.
    static public final String[] GLOBAL_LP_PERMS =
    { ACTIVITIES_VIEW, ACTIVITIES_ACCEPT, ACTIVITIES_REJECT_BEFORE_ACCEPTING,
            ACTIVITIES_REJECT_AFTER_ACCEPTING, ACTIVITIES_WORKOFFLINE,
            ACTIVITIES_SEARCHREPLACE, ACTIVITIES_FILES_VIEW,
            ACTIVITIES_FILES_EDIT, ACTIVITIES_COMMENTS_VIEW,
            ACTIVITIES_COMMENTS_EDIT, CONTENT_MANAGER,
            ACCOUNT_NOTIFICATION_GENERAL, ACTIVITIES_EXPORT,
            ACTIVITIES_EXPORT_INPROGRESS, ACTIVITIES_DOWNLOAD,
            ACTIVITIES_ACCEPT_ALL, ACTIVITIES_DOWNLOAD_ALL,
            ACTIVITIES_UPLOAD_SUPPORT_FILES, ACTIVITIES_DETAIL_STATISTICS,
            ACTIVITIES_SUMMARY_STATISTICS, SOURCE_PAGE_EDIT,
            COMMENT_ACCESS_RESTRICTED, ACTIVITIES_COMMENTS_NEW,
            ACTIVITIES_COMMENTS_JOB, ACTIVITIES_COMMENTS_DOWNLOAD,
            ACTIVITIES_SECONDARYTARGETFILE, USERS_VIEW, CHANGE_OWN_EMAIL,
            ACTIVITIES_JOB_COMMENTS_VIEW, ACTIVITIES_JOB_COMMENTS_EDIT,
            ACTIVITIES_JOB_COMMENTS_NEW, ACTIVITIES_JOB_COMMENTS_DOWNLOAD,
            REPORTS_TRANSLATIONS_EDIT, REPORTS_MAIN, REPORTS_LANGUAGE_SIGN_OFF,
            REPORTS_LANGUAGE_SIGN_OFF_SIMPLE, REPORTS_CHARACTER_COUNT,
            REPORTS_SCORECARD, REPORTS_DELL_FILE_LIST, ACTIVITY_DASHBOARD_VIEW,
            ACTIVITIES_BATCH_COMPLETE_ACTIVITY,
            ACTIVITIES_BATCH_COMPLETE_WORKFLOW, ACTIVITIES_UPDATE_LEVERAGE,
            ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY, TM_VIEW, TM_SEARCH,
            ACTIVITIES_TM_SEARCH, ACTIVITIES_TB_SEARCH, TERMINOLOGY_VIEW,
            TERMINOLOGY_SEARCH, ACTIVITIES_DOWNLOAD_COMBINED,
            ACTIVITIES_EXPORT_DOWNLOAD, REPORTS_POST_REVIEW_QA,
            REPORTS_TRANSLATIONS_VERIFICATION };

    /**
     * You should add any new permissions to this call so that the permission
     * can be registered upon startup.
     * 
     * NOTE: DO NOT ADD PERMISSIONS IN LOGICAL GROUPINGS, ONLY ADD NEW
     * PERMISSIONS TO THE BOTTOM OF THE LIST AND DO NOT REORDER THIS LIST
     * EVER!!!!
     */
    static private boolean addAllPermissions()
    {
        boolean added = false;
        // BEGIN -- add your permissions here.
        // SEE NOTE ABOVE!!!!!!!
        // Just add any new perm you have to the BOTTOM of this list
        // not near any logically similar permission.
        added = addPermission(1, LOGS_VIEW) || added;
        added = addPermission(2, SHUTDOWN_SYSTEM) || added;
        added = addPermission(3, LOCALE_PAIRS_VIEW) || added;
        added = addPermission(4, LOCALE_PAIRS_REMOVE) || added;
        added = addPermission(5, LOCALE_PAIRS_NEW) || added;
        added = addPermission(6, ACTIVITY_TYPES_VIEW) || added;
        added = addPermission(7, ACTIVITY_TYPES_REMOVE) || added;
        added = addPermission(8, ACTIVITY_TYPES_EDIT) || added;
        added = addPermission(9, ACTIVITY_TYPES_NEW) || added;
        added = addPermission(10, CURRENCY_VIEW) || added;
        added = addPermission(11, CURRENCY_EDIT) || added;
        added = addPermission(12, CURRENCY_NEW) || added;
        added = addPermission(13, PROJECTS_MANAGE) || added;
        added = addPermission(14, PROJECTS_MANAGE_WORKFLOWS) || added;
        added = addPermission(15, CALENDAR_ADMINISTER) || added;
        added = addPermission(16, WORKFLOW_CANCEL) || added;
        added = addPermission(17, RATES_VIEW) || added;
        added = addPermission(18, RATES_EDIT) || added;
        added = addPermission(19, RATES_NEW) || added;
        added = addPermission(20, SYS_CAL_VIEW) || added;
        added = addPermission(21, SYS_CAL_DUP) || added;
        added = addPermission(22, SYS_CAL_DEFAULT) || added;
        added = addPermission(23, SYS_CAL_REMOVE) || added;
        added = addPermission(24, SYS_CAL_EDIT) || added;
        added = addPermission(25, SYS_CAL_NEW) || added;
        added = addPermission(26, USER_CAL_VIEW) || added;
        added = addPermission(27, USER_CAL_EDIT) || added;
        added = addPermission(28, USER_CAL_EDIT_YOURS) || added;
        added = addPermission(29, HOLIDAY_VIEW) || added;
        added = addPermission(30, HOLIDAY_REMOVE) || added;
        added = addPermission(31, HOLIDAY_EDIT) || added;
        added = addPermission(32, HOLIDAY_NEW) || added;
        added = addPermission(33, PERMGROUPS_VIEW) || added;
        added = addPermission(34, PERMGROUPS_REMOVE) || added;
        added = addPermission(35, PERMGROUPS_EDIT) || added;
        added = addPermission(36, PERMGROUPS_NEW) || added;
        added = addPermission(37, USERS_VIEW) || added;
        added = addPermission(38, USERS_EDIT) || added;
        added = addPermission(39, USERS_REMOVE) || added;
        added = addPermission(40, USERS_NEW) || added;
        added = addPermission(41, USERS_PROJECT_MEMBERSHIP) || added;
        added = addPermission(42, TM_VIEW) || added;
        added = addPermission(43, TM_BROWSER) || added;
        added = addPermission(44, TM_STATS) || added;
        added = addPermission(45, TM_MAINTENANCE) || added;
        added = addPermission(46, TM_IMPORT) || added;
        added = addPermission(47, TM_EXPORT) || added;
        added = addPermission(48, TM_REINDEX) || added;
        added = addPermission(49, TM_DUPLICATE) || added;
        added = addPermission(50, TM_EDIT) || added;
        added = addPermission(51, TM_NEW) || added;
        added = addPermission(52, TMP_VIEW) || added;
        added = addPermission(53, TMP_EDIT) || added;
        added = addPermission(54, TMP_NEW) || added;
        added = addPermission(55, TERMINOLOGY_VIEW) || added;
        added = addPermission(56, TERMINOLOGY_STATS) || added;
        added = addPermission(57, TERMINOLOGY_INDEXES) || added;
        added = addPermission(58, TERMINOLOGY_REMOVE) || added;
        added = addPermission(59, TERMINOLOGY_DUPLICATE) || added;
        added = addPermission(60, TERMINOLOGY_EDIT) || added;
        added = addPermission(61, TERMINOLOGY_NEW) || added;
        added = addPermission(62, TERMINOLOGY_BROWSE) || added;
        added = addPermission(63, TERMINOLOGY_IMPORT) || added;
        added = addPermission(64, TERMINOLOGY_EXPORT) || added;
        added = addPermission(65, TERMINOLOGY_MAINTENANCE) || added;
        added = addPermission(66, TERMINOLOGY_INPUT_MODELS) || added;
        added = addPermission(67, PROJECTS_VIEW) || added;
        added = addPermission(68, PROJECTS_IMPORT) || added;
        added = addPermission(69, PROJECTS_EXPORT) || added;
        added = addPermission(70, PROJECTS_EDIT) || added;
        added = addPermission(71, PROJECTS_EDIT_PM) || added;
        added = addPermission(72, PROJECTS_NEW) || added;
        added = addPermission(73, WORKFLOWS_VIEW) || added;
        added = addPermission(74, WORKFLOWS_DUPLICATE) || added;
        added = addPermission(75, WORKFLOWS_REMOVE) || added;
        added = addPermission(76, WORKFLOWS_EDIT) || added;
        added = addPermission(77, WORKFLOWS_NEW) || added;
        added = addPermission(78, LOCPROFILES_VIEW) || added;
        added = addPermission(79, LOCPROFILES_REMOVE) || added;
        added = addPermission(80, LOCPROFILES_DETAILS) || added;
        added = addPermission(81, LOCPROFILES_DUP) || added;
        added = addPermission(82, LOCPROFILES_EDIT) || added;
        added = addPermission(83, LOCPROFILES_NEW) || added;
        added = addPermission(84, SNIPPET_IMPORT) || added;
        added = addPermission(85, SYSTEM_PARAMS) || added;
        added = addPermission(86, SUPPORT_FILES_VIEW) || added;
        added = addPermission(87, SUPPORT_FILES_REMOVE) || added;
        added = addPermission(88, SUPPORT_FILES_UPLOAD) || added;
        added = addPermission(89, FILE_PROFILES_VIEW) || added;
        added = addPermission(90, FILE_PROFILES_REMOVE) || added;
        added = addPermission(91, FILE_PROFILES_EDIT) || added;
        added = addPermission(92, FILE_PROFILES_NEW) || added;
        added = addPermission(93, FILE_EXT_VIEW) || added;
        added = addPermission(94, FILE_EXT_NEW) || added;
        added = addPermission(95, XMLRULE_VIEW) || added;
        added = addPermission(96, XMLRULE_DUP) || added;
        added = addPermission(97, XMLRULE_EDIT) || added;
        added = addPermission(98, XMLRULE_NEW) || added;
        added = addPermission(99, SGMLRULE_VIEW) || added;
        added = addPermission(100, SGMLRULE_UPLOAD) || added;
        added = addPermission(101, SGMLRULE_CREATE) || added;
        added = addPermission(102, SGMLRULE_REMOVE) || added;
        added = addPermission(103, SGMLRULE_EDIT) || added;
        added = addPermission(104, IMPORT) || added;
        added = addPermission(105, SERVICEWARE_IMPORT) || added;
        added = addPermission(106, SNIPPET_ADD) || added;
        added = addPermission(107, SNIPPET_EDIT) || added;
//        added = addPermission(108, TEAMSITE_SERVER_VIEW) || added;
//        added = addPermission(109, TEAMSITE_SERVER_REMOVE) || added;
//        added = addPermission(110, TEAMSITE_SERVER_CREATE) || added;
//        added = addPermission(111, TEAMSITE_SERVER_EDIT) || added;
//        added = addPermission(112, TEAMSITE_SERVER_NEW) || added;
//        added = addPermission(113, TEAMSITE_PROFILES_VIEW) || added;
//        added = addPermission(114, TEAMSITE_PROFILES_REMOVE) || added;
//        added = addPermission(115, TEAMSITE_PROFILES_NEW) || added;
        added = addPermission(116, DATABASE_INTEGRATION) || added;
        added = addPermission(117, EXPORT_LOC_VIEW) || added;
        added = addPermission(118, EXPORT_LOC_REMOVE) || added;
        added = addPermission(119, EXPORT_LOC_DEFAULT) || added;
        added = addPermission(120, EXPORT_LOC_EDIT) || added;
        added = addPermission(121, EXPORT_LOC_NEW) || added;
        added = addPermission(122, VIGNETTE_IMPORT) || added;
        added = addPermission(123, CORPUS_ALIGNER_VIEW) || added;
        added = addPermission(124, CORPUS_ALIGNER_CREATE) || added;
        added = addPermission(125, CORPUS_ALIGNER_DOWNLOAD) || added;
        added = addPermission(126, CORPUS_ALIGNER_UPLOAD) || added;
        added = addPermission(127, JOB_SCOPE_ALL) || added;
        added = addPermission(128, JOBS_VIEW) || added;
        added = addPermission(129, JOBS_SEARCH_REPLACE) || added;
        added = addPermission(130, JOBS_CHANGE_WFM) || added;
        added = addPermission(131, JOBS_DISCARD) || added;
        added = addPermission(132, JOBS_DISPATCH) || added;
        added = addPermission(133, JOBS_EXPORT_SOURCE) || added;
        added = addPermission(134, JOBS_EXPORT) || added;
        added = addPermission(135, JOBS_REEXPORT) || added;
        added = addPermission(136, JOBS_DETAILS) || added;
        added = addPermission(137, JOBS_VIEW_ERROR) || added;
        added = addPermission(138, JOBS_ARCHIVE) || added;
        added = addPermission(139, JOBS_DOWNLOAD) || added;
        added = addPermission(140, JOBS_MAKE_READY) || added;
        added = addPermission(141, JOBS_PLANNEDCOMPDATE) || added;
        added = addPermission(142, JOB_COMMENTS_VIEW) || added;
        added = addPermission(143, JOB_COMMENTS_EDIT) || added;
        added = addPermission(144, JOB_COSTING_VIEW) || added;
        added = addPermission(145, JOB_COSTING_EDIT) || added;
        added = addPermission(146, JOB_COSTING_REPORT) || added;
        added = addPermission(147, JOB_FILES_VIEW) || added;
        added = addPermission(148, JOB_FILES_EDIT) || added;
        added = addPermission(149, JOB_WORKFLOWS_VIEW) || added;
        added = addPermission(150, JOB_WORKFLOWS_DISCARD) || added;
        added = addPermission(151, JOB_WORKFLOWS_VIEW_ERROR) || added;
        added = addPermission(152, JOB_WORKFLOWS_WORDCOUNT) || added;
        added = addPermission(153, JOB_WORKFLOWS_RATEVENDOR) || added;
        added = addPermission(154, JOB_WORKFLOWS_ARCHIVE) || added;
        added = addPermission(155, JOB_WORKFLOWS_DETAILS) || added;
        added = addPermission(156, JOB_WORKFLOWS_EXPORT) || added;
        added = addPermission(157, JOB_WORKFLOWS_ADD) || added;
        added = addPermission(158, JOB_WORKFLOWS_EDIT) || added;
        added = addPermission(159, JOB_WORKFLOWS_DISPATCH) || added;
        added = addPermission(160, JOB_WORKFLOWS_ESTCOMPDATE) || added;
        added = addPermission(161, JOB_WORKFLOWS_PLANNEDCOMPDATE) || added;
        added = addPermission(162, JOB_WORKFLOWS_EDITEXPORTLOC) || added;
        added = addPermission(163, ACTIVITIES_VIEW) || added;
        added = addPermission(164, ACTIVITIES_ACCEPT) || added;
        added = addPermission(165, ACTIVITIES_EXPORT) || added;
        added = addPermission(166, ACTIVITIES_EXPORT_INPROGRESS) || added;
        added = addPermission(167, ACTIVITIES_WORKOFFLINE) || added;
        added = addPermission(168, ACTIVITIES_UPLOAD_SUPPORT_FILES) || added;
        added = addPermission(169, ACTIVITIES_SEARCHREPLACE) || added;
        added = addPermission(170, ACTIVITIES_FILES_VIEW) || added;
        added = addPermission(171, ACTIVITIES_FILES_EDIT) || added;
        added = addPermission(172, ACTIVITIES_COMMENTS_VIEW) || added;
        added = addPermission(173, ACTIVITIES_COMMENTS_EDIT) || added;
        added = addPermission(174, REPORTS_MAIN) || added;
        //added = addPermission(175, REPORTS_ADMIN) || added;
        //added = addPermission(176, REPORTS_COMPOSER) || added;
        added = addPermission(177, VENDORS_NEW) || added;
        added = addPermission(178, VENDORS_VIEW) || added;
        added = addPermission(179, VENDORS_EDIT) || added;
        added = addPermission(180, VENDORS_REMOVE) || added;
        added = addPermission(181, VENDORS_DETAILS) || added;
        added = addPermission(182, VENDORS_CUSTOMFORM) || added;
        added = addPermission(183, VENDORS_RATING_NEW) || added;
        added = addPermission(184, VENDORS_RATING_VIEW) || added;
        added = addPermission(185, VENDORS_RATING_EDIT) || added;
        added = addPermission(186, VENDORS_RATING_REMOVE) || added;
        added = addPermission(187, CONTENT_MANAGER) || added;
        added = addPermission(188, CUSTOMER_UPLOAD) || added;
        added = addPermission(189, DOCUMENTUM_IMPORT) || added;
        added = addPermission(190, GET_ALL_PROJECTS) || added;
        added = addPermission(191, GET_PROJECTS_I_MANAGE) || added;
        added = addPermission(192, GET_PROJECTS_I_BELONG) || added;
        added = addPermission(193, REPORTS_CUSTOM_EXTERNAL) || added;
        added = addPermission(194, PERMGROUPS_DETAILS) || added;
        added = addPermission(195, ACTIVITIES_COMMENTS_NEW) || added;
        added = addPermission(196, JOB_COMMENTS_NEW) || added;
        added = addPermission(197, ACCOUNT_NOTIFICATION_SYSTEM) || added;
        added = addPermission(198, ACCOUNT_NOTIFICATION_WFMGMT) || added;
        added = addPermission(199, ACCOUNT_NOTIFICATION_GENERAL) || added;
        added = addPermission(200, SOURCE_PAGE_EDIT) || added;
        added = addPermission(201, COMMENT_ACCESS_RESTRICTED) || added;
        added = addPermission(202, USERS_EDIT_ASSIGN_ANY_PERMGROUPS) || added;
        added = addPermission(203, JOB_SOURCE_WORDCOUNT_TOTAL) || added;
        added = addPermission(204, JOBS_CLEAR_ERRORS) || added;
        added = addPermission(205, JOB_WORKFLOWS_REASSIGN) || added;
        added = addPermission(206, TM_DELETE) || added;
        added = addPermission(207, COMMENT_EDIT_ALL_COMMENTS) || added;
        added = addPermission(208, FILE_PROFILES_SEE_ALL) || added;
        added = addPermission(209, REPORTS_WORD_COUNT) || added;
        added = addPermission(210, REPORTS_CUSTOM) || added;
        added = addPermission(211, REPORTS_JOB_COST) || added;
        added = addPermission(212, REPORTS_TM) || added;
        added = addPermission(213, REPORTS_WF_STATUS) || added;
        added = addPermission(214, REPORTS_JOB_DETAILS) || added;
        added = addPermission(215, REPORTS_AVG_PER_COMP) || added;
        added = addPermission(216, REPORTS_MISSING_TERMS) || added;
        added = addPermission(217, REPORTS_TERM_AUDIT) || added;
        added = addPermission(218, REPORTS_DELL_JOB_STATUS) || added;
        added = addPermission(219, REPORTS_DELL_ACT_DUR) || added;
        added = addPermission(220, REPORTS_DELL_ONLINE_JOBS) || added;
        added = addPermission(221, REPORTS_DELL_ONLINE_REVIEW_STATUS) || added;
        added = addPermission(222, JOB_WORKFLOWS_ESTREVIEWSTART) || added;
        added = addPermission(223, JOB_WORKFLOWS_DETAIL_STATISTICS) || added;
        added = addPermission(224, JOB_WORKFLOWS_SUMMARY_STATISTICS) || added;
        added = addPermission(225, ACTIVITIES_DETAIL_STATISTICS) || added;
        added = addPermission(226, ACTIVITIES_SUMMARY_STATISTICS) || added;
        added = addPermission(227, REPORTS_DELL_ONLINE_JOBS_RECALC) || added;
        added = addPermission(228, REPORTS_DELL_VENDOR_PO) || added;
        added = addPermission(229, REPORTS_COMMENT) || added;
        added = addPermission(230, REPORTS_DELL_REVIEWER_VENDOR_PO) || added;
        added = addPermission(231, COMPANY_VIEW) || added;
        added = addPermission(232, COMPANY_EDIT) || added;
        added = addPermission(233, COMPANY_NEW) || added;
        added = addPermission(234, COMPANY_REMOVE) || added;
        added = addPermission(235, REPORTS_CUSTOMIZE) || added;
        added = addPermission(236, JOB_QUOTE_VIEW) || added;
        added = addPermission(237, JOB_QUOTE_SEND) || added;
        added = addPermission(238, JOB_QUOTE_STATUS_VIEW) || added;
        added = addPermission(239, COSTING_EXPENSE_VIEW) || added;
        added = addPermission(240, COSTING_REVENUE_VIEW) || added;
        added = addPermission(241, REPORTS_REVIEWER_LISA_QA) || added;
        // For sla report issue
        added = addPermission(242, JOBS_ESTIMATEDCOMPDATE) || added;
        added = addPermission(243, JOBS_ESTIMATEDTRANSLATECOMPDATE) || added;
        added = addPermission(244, REPORTS_SLA) || added;

        added = addPermission(245, ACTIVITIES_REJECT_BEFORE_ACCEPTING) || added;
        added = addPermission(246, ACTIVITIES_REJECT_AFTER_ACCEPTING) || added;

        // For "Add job id into online job report" issue
        added = addPermission(247, REPORTS_DELL_ONLINE_JOBS_ID) || added;

        // For
        // "Grey out the edit buttons once the quote approve has been selected"
        // issue
        added = addPermission(248, JOB_COSTING_REEDIT) || added;

        // "File Profiles Search" is not required (GBS-2875)
        //added = addPermission(249, FILE_PROFILES_SEARCH) || added;

        // For " Quotation process for WebEx " issue
        added = addPermission(250, JOB_QUOTE_APPROVE) || added;
        added = addPermission(251, JOB_QUOTE_PONUMBER_EDIT) || added;
        added = addPermission(252, JOB_QUOTE_PONUMBER_VIEW) || added;

        // For " add download button to my activities " issue
        added = addPermission(253, ACTIVITIES_DOWNLOAD) || added;

        // For add accept all button to my activities
        added = addPermission(254, ACTIVITIES_ACCEPT_ALL) || added;

        // For CC&BCC email access.
        added = addPermission(255, USERS_ACCESS_CCEMAIL) || added;
        added = addPermission(256, USERS_ACCESS_BCCEMAIL) || added;

        added = addPermission(257, REPORTS_COMMENTS_ANALYSIS) || added;
        added = addPermission(258, REPORTS_TRANSLATIONS_EDIT) || added;
        added = addPermission(259, REPORTS_LANGUAGE_SIGN_OFF) || added;

        added = addPermission(260, REPORTS_TRANSLATION_PROGRESS) || added;

        added = addPermission(261, ACTIVITIES_COMMENTS_JOB) || added;

        // For Implemented Comments Check
        added = addPermission(262, REPORTS_IMPLEMENTED_COMMENTS_CHECK) || added;

        // Segmentation Rule
        added = addPermission(263, SEGMENTATIONRULE_VIEW) || added;
        added = addPermission(264, SEGMENTATIONRULE_NEW) || added;
        added = addPermission(265, SEGMENTATIONRULE_EDIT) || added;
        added = addPermission(266, SEGMENTATIONRULE_EXPORT) || added;
        added = addPermission(267, SEGMENTATIONRULE_REMOVE) || added;
        added = addPermission(268, SEGMENTATIONRULE_DUP) || added;

        added = addPermission(269, REPORTS_CHARACTER_COUNT) || added;
        added = addPermission(270, IN_CONTEXT_MATCH) || added;

        /* skip activities */
        added = addPermission(271, JOB_WORKFLOWS_SKIP) || added;

        added = addPermission(272, LOCALE_NEW) || added;

        added = addPermission(273, ACTIVITIES_COMMENTS_DOWNLOAD) || added;
        added = addPermission(274, CHANGE_OWN_PASSWORD) || added;

        added = addPermission(278, SERVICE_TM_GET_ALL_TMPROFILES) || added;

        added = addPermission(279, SERVICE_TB_SEARCH_ENTRY) || added;
        added = addPermission(280, SERVICE_TB_CREATE_ENTRY) || added;
        added = addPermission(281, SERVICE_TB_EDIT_ENTRY) || added;
        added = addPermission(282, SERVICE_TB_GET_ALL_TB) || added;

        added = addPermission(283, ACTIVITIES_DOWNLOAD_ALL) || added;

        added = addPermission(284, FILE_EXT_REMOVE) || added;

//        added = addPermission(285, ACCOUNT_DOWNLOAD_ALL_OFFLINE_FILES) || added;

        added = addPermission(286, JOB_FILES_DOWNLOAD) || added;

        added = addPermission(287, CONNECT_TO_CVS) || added;

        added = addPermission(288, XMLRULE_REMOVE) || added;

        added = addPermission(289, TMP_REMOVE) || added;

        added = addPermission(290, PROJECTS_REMOVE) || added;

        added = addPermission(291, REPORTS_DELL_FILE_LIST) || added;

        // For Filter configuration
        added = addPermission(292, FILTER_CONFIGURATION_VIEW) || added;
        added = addPermission(293, FILTER_CONFIGURATION_REMOVE_FILTERS)
                || added;
        added = addPermission(294, FILTER_CONFIGURATION_ADD_FILTER) || added;
        added = addPermission(295, FILTER_CONFIGURATION_EDIT_FILTER) || added;

        // added = addPermission(ACTIVITIES_CROWDSIGHT) || added;

        // XML DTD
        added = addPermission(296, XMLDTD_VIEW) || added;
        added = addPermission(297, XMLDTD_EDIT) || added;
        added = addPermission(298, XMLDTD_NEW) || added;
        added = addPermission(299, XMLDTD_REMOVE) || added;
        added = addPermission(300, CVS_ADMIN) || added;
        added = addPermission(301, CVS_OPERATE) || added;

        // AUTOMATIC ACTION
//        added = addPermission(302, AUTOMATIC_ACTIONS_VIEW) || added;
//        added = addPermission(303, AUTOMATIC_ACTIONS_REMOVE) || added;
//        added = addPermission(304, AUTOMATIC_ACTIONS_EDIT) || added;
//        added = addPermission(305, AUTOMATIC_ACTIONS_NEW) || added;

        added = addPermission(306, JOB_WORKFLOWS_PRIORITY) || added;

        added = addPermission(307, UILOCALE_VIEW) || added;
        added = addPermission(308, UILOCALE_REMOVE) || added;
        added = addPermission(309, UILOCALE_DOWNLOAD_RES) || added;
        added = addPermission(310, UILOCALE_UPLOAD_RES) || added;
        added = addPermission(311, UILOCALE_SET_DEFAULT) || added;
        added = addPermission(312, UILOCALE_NEW) || added;
        added = addPermission(313, ACTIVITIES_SECONDARYTARGETFILE) || added;

        added = addPermission(314, ATTRIBUTE_VIEW) || added;
        added = addPermission(315, ATTRIBUTE_NEW) || added;
        added = addPermission(316, ATTRIBUTE_REMOVE) || added;
        added = addPermission(317, ATTRIBUTE_EDIT) || added;
        added = addPermission(318, ATTRIBUTE_GROUP_NEW) || added;
        added = addPermission(319, ATTRIBUTE_GROUP_REMOVE) || added;
        added = addPermission(320, ATTRIBUTE_GROUP_EDIT) || added;
        added = addPermission(321, JOB_ATTRIBUTE_VIEW) || added;
        added = addPermission(322, JOB_ATTRIBUTE_EDIT) || added;
        added = addPermission(323, ATTRIBUTE_GROUP_VIEW) || added;

        added = addPermission(324, ACTIVITIES_JOB_COMMENTS_VIEW) || added;
        added = addPermission(325, ACTIVITIES_JOB_COMMENTS_EDIT) || added;
        added = addPermission(326, ACTIVITIES_JOB_COMMENTS_NEW) || added;
        added = addPermission(327, ACTIVITIES_JOB_COMMENTS_DOWNLOAD) || added;

        // For BlogSmith. Added by Vincent, 2010-03-31
        added = addPermission(328, RSS_READER) || added;
        added = addPermission(329, RSS_JOB) || added;

        added = addPermission(330, JOB_ATTRIBUTE_REPORT) || added;

        // GSEdition
        added = addPermission(331, GSEDITION_VIEW) || added;
        added = addPermission(332, GSEDITION_REMOVE) || added;
        added = addPermission(333, GSEDITION_EDIT) || added;
        added = addPermission(334, GSEDITION_NEW) || added;

        // GSEdition ACTION
//        added = addPermission(335, GSEDITION_ACTIONS_VIEW) || added;
//        added = addPermission(336, GSEDITION_ACTIONS_REMOVE) || added;
//        added = addPermission(337, GSEDITION_ACTIONS_EDIT) || added;
//        added = addPermission(338, GSEDITION_ACTIONS_NEW) || added;

        added = addPermission(339, CVS_Servers) || added;
        added = addPermission(340, CVS_Servers_NEW) || added;
        added = addPermission(341, CVS_Servers_EDIT) || added;
        added = addPermission(342, CVS_Servers_REMOVE) || added;
        added = addPermission(343, CVS_MODULES) || added;
        added = addPermission(344, CVS_MODULES_NEW) || added;
        added = addPermission(345, CVS_MODULES_EDIT) || added;
        added = addPermission(346, CVS_MODULES_REMOVE) || added;
        added = addPermission(347, CVS_MODULES_CHECKOUT) || added;
        added = addPermission(348, CVS_MODULE_MAPPING) || added;
        added = addPermission(349, CVS_MODULE_MAPPING_NEW) || added;
        added = addPermission(350, CVS_MODULE_MAPPING_EDIT) || added;
        added = addPermission(351, CVS_MODULE_MAPPING_REMOVE) || added;
        added = addPermission(352, CVS_FILE_PROFILES) || added;
        added = addPermission(353, CVS_FILE_PROFILES_NEW) || added;
        added = addPermission(354, CVS_FILE_PROFILES_EDIT) || added;
        added = addPermission(355, CVS_FILE_PROFILES_REMOVE) || added;
        // SEE NOTE ABOVE!!!!

        added = addPermission(356, SET_DEFAULT_ROLES) || added;

        added = addPermission(357, ADD_SOURCE_FILES) || added;
        added = addPermission(358, DELETE_SOURCE_FILES) || added;
        added = addPermission(359, EDIT_SOURCE_FILES) || added;

        added = addPermission(360, JOB_SCOPE_MYPROJECTS) || added;
        added = addPermission(361, ACTIVITY_DASHBOARD_VIEW) || added;
        added = addPermission(362, ACTIVITIES_BATCH_COMPLETE_ACTIVITY) || added;
        added = addPermission(363, ACTIVITIES_BATCH_COMPLETE_WORKFLOW) || added;

        added = addPermission(364, ACTIVITIES_UPDATE_LEVERAGE) || added;
        added = addPermission(365, JOB_UPDATE_WORD_COUNTS) || added;

        // Unused permission(ACTIVITIES_OFFLINEUPLOAD), do not delete
        added = addPermission(366, ACTIVITIES_OFFLINEUPLOAD) || added;

        // For GBS-2113, create job permission
        added = addPermission(367, CREATE_JOB) || added;

        // For GBS-2394, upload file via web service permission
        added = addPermission(368, CUSTOMER_UPLOAD_VIA_WEBSERVICE) || added;

        added = addPermission(369, JOB_UPDATE_LEVERAGE) || added;

        // For GBS-2393
        added = addPermission(370, ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY)
                || added;
        added = addPermission(371, TM_SEARCH) || added;

        added = addPermission(372, TERMINOLOGY_SEARCH) || added;
        added = addPermission(373, ACTIVITIES_TM_SEARCH) || added;
        added = addPermission(374, ACTIVITIES_TB_SEARCH) || added;

        // TUV Attributes
        added = addPermission(375, TM_ENABLE_TM_ATTRIBUTES) || added;

        // GBS-2591: Add New Words and Repetition counts in Job Created email.
        added = addPermission(376, ACCOUNT_NOTIFICATION_NOMATCHES) || added;
        added = addPermission(377, ACCOUNT_NOTIFICATION_REPETITIONS) || added;

        // for GBS-2300
        added = addPermission(378, USERS_IMPORT) || added;
        added = addPermission(379, USERS_EXPORT) || added;

        added = addPermission(380, TM_ADD_ENTRY) || added;
        added = addPermission(381, TM_EDIT_ENTRY) || added;
        added = addPermission(382, TM_DELETE_ENTRY) || added;
        added = addPermission(383, TM_SEARCH_ADVANCED) || added;

        // GBS-2384
        added = addPermission(384, COMPANY_MIGRATE) || added;
        added = addPermission(385, REPORTS_SUMMARY) || added;
        added = addPermission(386, ACTIVITIES_DOWNLOAD_COMBINED) || added;
        added = addPermission(387, CHANGE_OWN_EMAIL) || added;

        added = addPermission(388, MTP_VIEW) || added;
        added = addPermission(389, MTP_NEW) || added;
        added = addPermission(390, MTP_EDIT) || added;
        added = addPermission(391, MTP_REMOVE) || added;
        added = addPermission(392, JOB_CHANGE_NAME) || added;
        
        // For GBS-3339, New CreateJob feature that does not use Java applet
        added = addPermission(393, CREATE_JOB_NO_APPLET) || added;
        // GBS-3389: recreate job in pending job list
        added = addPermission(394, JOBS_RECREATE) || added;
        //GBS-3450: Export/Import filter configurations from server to server.
        added = addPermission(395, FILTER_CONFIGURATION_EXPORT_FILTERS) || added;
        added = addPermission(396, FILTER_CONFIGURATION_IMPORT_FILTERS) || added;
        
        added = addPermission(397, REPORTS_LANGUAGE_SIGN_OFF_SIMPLE) || added;

        //Export/Import Locale Pairs from server to server.
        added = addPermission(398, LOCALE_PAIRS_EXPORT) || added;
        added = addPermission(399, LOCALE_PAIRS_IMPORT) || added;
        //Export/Import MachineTranslation Profiles from server to server.
        added = addPermission(400, MTP_EXPORT) || added;
        added = addPermission(401, MTP_IMPORT) || added;
        added = addPermission(402, COTI_JOB) || added;
        
        added = addPermission(403, EDIT_SCORECARD) || added;
        added = addPermission(404, REPORTS_SCORECARD) || added;
        added = addPermission(405, VIEW_SCORECARD) || added;
        added = addPermission(406, OPERATION_LOG_VIEW) || added;
        added = addPermission(407, ELOQUA) || added;
        added = addPermission(408, REPORTS_DELL_ONLINE_JOBS_FOR_IP_TRANSLATOR) || added;
        
        added = addPermission(409, ACTIVITIES_EXPORT_DOWNLOAD) || added;
        added = addPermission(410, JOBS_EXPORT_DOWNLOAD) || added;
        added = addPermission(411, JOB_WORKFLOWS_EXPORT_DOWNLOAD) || added;
        added = addPermission(412, JOBS_GROUP) || added;
        added = addPermission(413, JOBS_NEWGROUP) || added;
        added = addPermission(414, JOBS_REMOVEGROUP) || added;
        added = addPermission(415, JOBS_ADDJOBTOGROUP) || added;
        added = addPermission(416, JOBS_REMOVEJOBFROMGROUP) || added;
        added = addPermission(417, MIND_TOUCH) || added;
        added = addPermission(418, REPORTS_POST_REVIEW_QA) || added;
        added = addPermission(419, GIT_CONNECTOR) || added;
        added = addPermission(420, REPORTS_TRANSLATIONS_VERIFICATION) || added;
        added = addPermission(422, JOB_WORKFLOWS_TRANSLATED_TEXT) || added;
        added = addPermission(423, BLAISE_CONNECTOR) || added;

        return added;
    }

    /** This is a temporary singleton. */
    static private PermissionManager s_permissionManager = null;

    /** Hashmap which maps permission names to real ids. */
    static private HashMap<String, Long> s_idMap = new HashMap<String, Long>();

    /** The permission XML. **/
    // static private String s_permissionXml = null;

    /** SQL Statements. */
    static private final String SQL_SELECT_ALL = "Select id, name from permission";
    // Use max(id)+1 instead of permission_seq to ensure permission ids
    // continuous
    // now use mysql to ensure the continous
    static private final String SQL_INSERT_PERM = "insert into PERMISSION values(?, ?)";

    static private final String SQL_INSERT_FIRST_PERM = "insert into permission values(1, 'logs.view')";

    static private final String PERMISSION_XML_URL = "/globalsight/envoy/administration/permission/permissionXml.jsp";

    static private final String SQL_SELECT_300TH = "select name from permission where id = 300";
    static private final String SQL_EMPTY_PERMISSION_TABLE = "delete from permission";
    static private final String SQL_SELECT_PERMISSION_SET_FROM_PERMISSION_GROUP = "select a.id, a.permission_set from permissiongroup as a";
    static private final String SQL_UPDATE_PERMISSION_SET = "update permissiongroup set permission_set = ? where id = ?";

    /*
     * The default permission groups. This is a carryover from the original
     * Access Groups, and should not be used. This can continue to be used by
     * old code that checks to see if someone is "an Admin" or "a PM" , etc. But
     * eventually that old code should all be changed to check for the specific
     * permission of interest, and not for the person's group
     */
    static public final String GROUP_ADMINISTRATOR = "Administrator";
    static public final String GROUP_PROJECT_MANAGER = "ProjectManager";
    static public final String GROUP_LOCALE_MANAGER = "LocaleManager";
    static public final String GROUP_WORKFLOW_MANAGER = "WorkflowManager";
    static public final String GROUP_LOCALIZATION_PARTICIPANT = "LocalizationParticipant";
    static public final String GROUP_CUSTOMER = "Customer";
    static public final String GROUP_VENDOR_ADMIN = "VendorAdministrator";
    static public final String GROUP_VENDOR_MANAGER = "VendorManager";
    static public final String GROUP_VENDOR_VIEWER = "VendorViewer";

    /* The old group modules */
    static public final String GROUP_MODULE_GLOBALSIGHT = "GlobalSight";
    static public final String GROUP_MODULE_VENDOR_MANAGER = "VendorManagement";

    /**
     * Should be called during GlobalSight startup to initialize permissions.
     * This will load all permissions from the database, add only the new
     * permissions, and re-load permissions into the hashap.
     */
    static public void initialize()
    {
        updatePermissions();
        loadPermissions();

        if (addAllPermissions())
        {
            // new perms were added, so re-load
            loadPermissions();
        }

        logger.info("Initialized to contain " + s_idMap.size()
                + " flexible user permissions.");
    }

    /**
     * Returns the PermissionXML which describes all the permissions in the
     * system and their hierarchy.
     * 
     * @return String of XML
     */
    static public synchronized String getPermissionXml()
    {
        String s_permissionXml = null;
        if (s_permissionXml == null)
        {
            try
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();

                // http is ok for a reference to this host
                StringBuffer permXmlUrl = new StringBuffer("http://");
                permXmlUrl
                        .append(sc
                                .getStringParameter(SystemConfigParamNames.SERVER_HOST));
                permXmlUrl.append(":");
                permXmlUrl
                        .append(sc
                                .getStringParameter(SystemConfigParamNames.SERVER_PORT));
                permXmlUrl.append(PERMISSION_XML_URL);
                String companyId = CompanyThreadLocal.getInstance().getValue();
                permXmlUrl.append("?companyId=").append(companyId);
                logger.info("permXmlUrl : " + permXmlUrl);

                URL u = new URL(permXmlUrl.toString());
                InputStream is = u.openStream();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String s = null;
                StringBuffer sb = new StringBuffer();

                while ((s = br.readLine()) != null)
                {
                    sb.append(s).append("\r\n");
                }
                br.close();

                s_permissionXml = sb.toString();
                if (logger.isDebugEnabled())
                {
                    logger.debug("s_permissionXml" + s_permissionXml);                    
                }
            }
            catch (Exception ex)
            {
                logger.error("Failed to read permission xml.", ex);
                s_permissionXml = null;
            }
        }

        return s_permissionXml;
    }

    /**
     * Temporary way to get the PermissionManager.
     * 
     * @return PermissionManager
     */
    static public PermissionManager getPermissionManager()
    {
        if (s_permissionManager == null)
        {
            try
            {
                s_permissionManager = new PermissionManagerLocal();
            }
            catch (Exception ex)
            {
                logger.error("Failed to create PermissionManager", ex);
            }
        }

        return s_permissionManager;
    }

    /** Returns the mapped integer value to the given permission. */
    static int getBitValueForPermission(String p_perm)
            throws PermissionException
    {
        Long id = (Long) s_idMap.get(p_perm);

        if (id == null)
        {
            throw new PermissionException("Permission '" + p_perm
                    + "' does not exist.");
        }

        // possible impedance mismatch...
        return (int) id.longValue();
    }

    /**
     * There is a permission id problem since 7.1.8.0. This function is to
     * resolve the problem.
     */
    static private void updatePermissions()
    {
        String name = null;
        List<?> list = HibernateUtil.searchWithSql(SQL_SELECT_300TH, null);
        if (list != null && list.size() > 0)
        {
            name = (String) list.get(0);
        }
        if (name != null && name.equals(CVS_OPERATE))
        {
            try
            {
                HibernateUtil.executeSql(SQL_EMPTY_PERMISSION_TABLE);
                updateUnbalancedPermissionGroupSet();
            }
            catch (Exception e)
            {
                logger.error("Error to empty permission table.");
            }
        }
    }

    /**
     * Update Table permissiongroup. If permission id is greater than 300, the
     * id should plus 1. Then update permission_set to new string.
     */
    private static void updateUnbalancedPermissionGroupSet()
    {
        Connection c = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt1 = null;
        ResultSet rs = null;
        try
        {
            c = ConnectionPool.getConnection();
            c.setAutoCommit(false);
            stmt = c.prepareStatement(SQL_SELECT_PERMISSION_SET_FROM_PERMISSION_GROUP);
            stmt1 = c.prepareStatement(SQL_UPDATE_PERMISSION_SET);
            rs = stmt.executeQuery();

            while (rs.next())
            {
                long id = rs.getLong(1);
                String permissionSet = rs.getString(2);
                String[] permissionIdArray = permissionSet.split("\\|");

                StringBuffer newPermissionSet = new StringBuffer();
                for (String permissionId : permissionIdArray)
                {
                    if (StringUtils.isNotEmpty(permissionId))
                    {
                        long lId = Long.parseLong(permissionId);
                        if (lId >= 300)
                        {
                            lId += 1;
                        }
                        newPermissionSet.append("|").append(lId);
                    }
                }
                newPermissionSet.append("|");
                stmt1.setString(1, newPermissionSet.toString());
                stmt1.setLong(2, id);
                stmt1.addBatch();
            }

            stmt1.executeBatch();
            c.commit();
        }
        catch (Exception e)
        {
            logger.error("Failed to update permission_group from database.", e);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentClose(stmt1);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Clears and fills the s_idMap with the current permissions from the
     * database.
     */
    static private void loadPermissions()
    {
        Connection c = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        s_idMap.clear();

        try
        {
            c = ConnectionPool.getConnection();
            stmt = c.prepareStatement(SQL_SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next())
            {
                long id = rs.getLong(1);
                String name = rs.getString(2);
                // the key is the permission name
                s_idMap.put(name, new Long(id));

                if (logger.isDebugEnabled())
                {
                    logger.debug("Adding permission to map: " + id + "," + name);
                }
            }
        }
        catch (Exception ex)
        {
            logger.error("Failed to load permissions from database.", ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Adds the given permission to the database PERMISSION table if it does not
     * already exist in the map. This does update one by one, but this method
     * should almost never be called except on startup and if new permissions
     * were actually added, so there is no reason to batch.
     * 
     * @param p_added
     *            set to true if anything was added
     * @param p_perm
     *            permission name (should be above defined constant)
     */
    static private boolean addPermission(long id, String p_perm)
    {
        boolean added = false;

        Connection c = null;
        PreparedStatement stmt = null;

        try
        {
            c = ConnectionPool.getConnection();
            c.setAutoCommit(false);

            if (s_idMap.isEmpty())
            {
                stmt = c.prepareStatement(SQL_INSERT_FIRST_PERM);
                stmt.executeUpdate();
                s_idMap.put(p_perm, new Long(1));

                if (logger.isDebugEnabled())
                {
                    logger.debug("Added " + p_perm + " to the table.");
                }
            }
            else if (!s_idMap.containsKey(p_perm))
            {
                stmt = c.prepareStatement(SQL_INSERT_PERM);
                stmt.setLong(1, id);
                stmt.setString(2, p_perm);
                stmt.executeUpdate();

                added = true;

                if (logger.isDebugEnabled())
                {
                    logger.debug("Added " + p_perm + " to the table.");
                }
            }

            c.commit();
        }
        catch (Exception ex)
        {
            logger.error("Failed to add permission" + p_perm
                    + " to the database.", ex);
            added = false;
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentReturnConnection(c);
        }

        return added;
    }

    public static HashMap<String, Long> getAllPermissions()
    {
        return s_idMap;
    }
}