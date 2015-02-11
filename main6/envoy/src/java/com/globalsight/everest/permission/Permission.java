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

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * This class holds all the static definitions of permissions.
 *
 * NOTE: When adding new permission constants to this class,
 * be sure to update the static section where the HashMap
 * is filled.
 */
public class Permission
{
    static private final Logger s_logger =
            Logger.getLogger(
                Permission.class);

    /**
     * Static permission definitions -- see below for mapping
     * when you add a permission, you must add it to the
     * allAllPermissions() call otherwise it won't be recognized.
     */
    static public final String LOGS_VIEW = "logs.view";
    static public final String SHUTDOWN_SYSTEM = "shutdown.system";
    static public final String LOCALE_PAIRS_VIEW = "localePairs.view";
    static public final String LOCALE_PAIRS_REMOVE = "localePairs.remove";
    static public final String LOCALE_PAIRS_NEW = "localePairs.new";
    static public final String LOCALE_NEW = "locale.new";
    static public final String ACTIVITY_TYPES_VIEW = "activityTypes.view";
    static public final String ACTIVITY_TYPES_REMOVE = "activityTypes.remove";
    static public final String ACTIVITY_TYPES_EDIT = "activityTypes.edit";
    static public final String ACTIVITY_TYPES_NEW = "activityTypes.new";
    static public final String AUTOMATIC_ACTIONS_VIEW = "automaticActions.view";
    static public final String AUTOMATIC_ACTIONS_REMOVE = "automaticActions.remove";
    static public final String AUTOMATIC_ACTIONS_EDIT = "automaticActions.edit";
    static public final String AUTOMATIC_ACTIONS_NEW = "automaticActions.new";
    static public final String CURRENCY_VIEW = "currency.view";
    static public final String CURRENCY_EDIT = "currency.edit";
    static public final String CURRENCY_NEW = "currency.new";
    static public final String COMPANY_VIEW = "company.view";
    static public final String COMPANY_EDIT = "company.edit";
    static public final String COMPANY_NEW = "company.new";
    static public final String COMPANY_REMOVE = "company.remove";
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
    static public final String SERVICE_TM_SEARCH_ENTRY = "service.tm.searchEntries";
    static public final String SERVICE_TM_CREATE_ENTRY = "service.tm.createEntries";
    static public final String SERVICE_TM_EDIT_ENTRY = "service.tm.editEntries";
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
    static public final String TEAMSITE_SERVER_VIEW = "teamsiteserver.view";
    static public final String TEAMSITE_SERVER_REMOVE = "teamsiteserver.remove";
    static public final String TEAMSITE_SERVER_CREATE = "teamsiteserver.create";
    static public final String TEAMSITE_SERVER_EDIT = "teamsiteserver.edit";
    static public final String TEAMSITE_SERVER_NEW = "teamsiteserver.new";
    static public final String TEAMSITE_PROFILES_VIEW = "teamsiteprofiles.view";
    static public final String TEAMSITE_PROFILES_REMOVE = "teamsiteprofiles.remove";
    static public final String TEAMSITE_PROFILES_NEW = "teamsiteprofiles.new";
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
    static public final String JOBS_MAKE_READY = "jobs.makeready";
    static public final String JOBS_PLANNEDCOMPDATE = "jobs.plannedcompdate";
    static public final String FILE_PROFILES_SEARCH = "fileprofiles.search";
    
    // For sla report issue
    static public final String JOBS_ESTIMATEDCOMPDATE = 
        "jobs.estimatedcompdate";
    static public final String JOBS_ESTIMATEDTRANSLATECOMPDATE = 
        "jobs.estimatedtranslatecompdate";
    
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
    static public final String JOB_WORKFLOWS_DETAIL_STATISTICS = "job.workflows.detailStatistics";
    static public final String JOB_WORKFLOWS_RATEVENDOR = "job.workflows.ratevendor";
    static public final String JOB_WORKFLOWS_ARCHIVE = "job.workflows.archive";
    static public final String JOB_WORKFLOWS_DETAILS = "job.workflows.details";
    static public final String JOB_WORKFLOWS_EXPORT = "job.workflows.export";
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
    static public final String ACTIVITIES_ACCEPT = "activities.accept"; 
    static public final String ACTIVITIES_ACCEPT_ALL = "activities.accept.all";
    static public final String ACTIVITIES_BATCH_COMPLETE_ACTIVITY = "activities.batch.complete.activity";
    static public final String ACTIVITIES_BATCH_COMPLETE_WORKFLOW = "activities.batch.complete.workflow";
    static public final String ACTIVITIES_DOWNLOAD_ALL = "activities.download.all";
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
    static public final String ACTIVITIES_JOB_COMMENTS_NEW  = "activities.jobcomments.new";  
    static public final String ACTIVITIES_JOB_COMMENTS_DOWNLOAD = "activities.jobcomments.download";
    static public final String ACTIVITIES_COMMENTS_VIEW = "activities.comments.view";
    static public final String ACTIVITIES_COMMENTS_EDIT = "activities.comments.edit";
    static public final String ACTIVITIES_COMMENTS_NEW = "activities.comments.new";
    static public final String ACTIVITIES_COMMENTS_JOB = "activities.comments.jobComments";
    static public final String ACTIVITIES_SUMMARY_STATISTICS = "activities.summaryStatistics";
    static public final String ACTIVITIES_SECONDARYTARGETFILE = "activities.secondaryTargetFile";
    static public final String ACTIVITIES_CROWDSIGHT = "activities.crowdsight";
    static public final String REPORTS_MAIN = "reports.main";
    static public final String REPORTS_ADMIN = "reports.admin";
    static public final String REPORTS_COMPOSER = "reports.composer";
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
    static public final String GET_ALL_PROJECTS = "projects.getall";
    static public final String GET_PROJECTS_I_MANAGE = "projects.getmanage";
    static public final String GET_PROJECTS_I_BELONG = "projects.getbelong";

    static public final String ACCOUNT_DOWNLOAD_ALL_OFFLINE_FILES = "account.download.all.offline.files";
    static public final String ACCOUNT_NOTIFICATION_SYSTEM = "account.notification.system";
    static public final String ACCOUNT_NOTIFICATION_WFMGMT = "account.notification.wfmgmt";
    static public final String ACCOUNT_NOTIFICATION_GENERAL = "account.notification.general";
    static public final String SOURCE_PAGE_EDIT = "sourcepage.edit";
    static public final String COMMENT_ACCESS_RESTRICTED = "comment.access.restricted";
    static public final String JOB_SOURCE_WORDCOUNT_TOTAL = "job.source.wordcount.total";

    static public final String COMMENT_EDIT_ALL_COMMENTS = "comment.access.editallcomments";
    static public final String FILE_PROFILES_SEE_ALL = "fileprofiles.seeAll";
    static public final String USERS_VIEW_SEE_ALL = "users.view.seeAll";

    //add a permission for each report!
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
    static public final String REPORTS_LANGUAGE_SIGN_OFF = "reports.language.sign.off";
    static public final String REPORTS_CHARACTER_COUNT = "reports.character.count";
    
    static public final String REPORTS_TRANSLATION_PROGRESS = "reports.translation.progress";

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
    
    // For "Grey out the edit buttons once the quote approve has been selected" issue
    static public final String JOB_COSTING_REEDIT = "job.costing.reedit";
    
    // For " Quotation process for WebEx " issue
    static public final String JOB_QUOTE_APPROVE = "job.quote.approve";
    static public final String JOB_QUOTE_PONUMBER_VIEW = "job.quote.ponumber.view";
    static public final String JOB_QUOTE_PONUMBER_EDIT = "job.quote.ponumber.edit";
    
    static public final String IN_CONTEXT_MATCH = "tmProfile.in.context.match";
    
    //  For " add download button to my activities " issue    
    static public final String ACTIVITIES_DOWNLOAD = "activities.download";
    static public final String CHANGE_OWN_PASSWORD = "activities.change.own.password";
    static public final String SERVICE_TM_GET_ALL_TMPROFILES = "service.tm.getAllTMProfiles";
    static public final String SERVICE_TB_CREATE_ENTRY = "service.tb.createEntries";
    static public final String SERVICE_TB_SEARCH_ENTRY = "service.tb.searchEntries";
    static public final String SERVICE_TB_EDIT_ENTRY = "service.tb.editEntries";
    static public final String SERVICE_TB_GET_ALL_TB = "service.tb.getAllTermbases";
    
    static public final String CONNECT_TO_CVS = "desktopicon.connect.cvs";
    
    //For CVS function
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
    
    //For attribute
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
    

    //For locale languages
    static public final String UILOCALE_VIEW = "uilocale.view";
    static public final String UILOCALE_REMOVE = "uilocale.remove";
    static public final String UILOCALE_DOWNLOAD_RES = "uilocale.downloadres";
    static public final String UILOCALE_UPLOAD_RES = "uilocale.uploadres";
    static public final String UILOCALE_SET_DEFAULT = "uilocale.setdefault";
    static public final String UILOCALE_NEW = "uilocale.new";
      
    //For filter configuration.
    static public final String FILTER_CONFIGURATION_VIEW = "filter.configuration";
    static public final String FILTER_CONFIGURATION_REMOVE_FILTERS = "filter.configuration.remove.filters";
    static public final String FILTER_CONFIGURATION_ADD_FILTER = "filter.configuration.add.filter";
    static public final String FILTER_CONFIGURATION_EDIT_FILTER = "filter.configuration.edit.filter";
    
    static public final String GSEDITION_VIEW = "gsedition.view";
    static public final String GSEDITION_REMOVE = "gsedition.remove";
    static public final String GSEDITION_EDIT = "gsedition.edit";
    static public final String GSEDITION_NEW = "gsedition.new";
    static public final String GSEDITION_ACTIONS_VIEW = "gseditionActions.view";
    static public final String GSEDITION_ACTIONS_REMOVE = "gseditionActions.remove";
    static public final String GSEDITION_ACTIONS_EDIT = "gseditionActions.edit";
    static public final String GSEDITION_ACTIONS_NEW = "gseditionActions.new";
    
    static public final String ADD_SOURCE_FILES = "sourceFiles.add";
    static public final String DELETE_SOURCE_FILES = "sourceFiles.delete";
    static public final String EDIT_SOURCE_FILES = "sourceFiles.edit";

    //For BlogSmith. Added by Vincent, 2010-03-31
    static public final String RSS_READER = "rss.reader";
    static public final String RSS_JOB = "rss.job";
    
    static public final String SET_DEFAULT_ROLES = "admin.setDefaultRoles";
    
    //For Job Search
    static public final String JOB_SCOPE_MYPROJECTS = "jobscope.myProjects";

    static public final String UPDATE_LEVERAGE = "updateLeverage";
    static public final String UPDATE_WORD_COUNTS = "jobs.updateWordCounts";
    
    // Limit the range of global LP permissions,super LocalizationParticipant
    // user can only edit below permissions.
    static public final String[] GLOBAL_LP_PERMS = {
                    ACTIVITIES_VIEW, ACTIVITIES_ACCEPT, 
                    ACTIVITIES_REJECT_BEFORE_ACCEPTING,
                    ACTIVITIES_REJECT_AFTER_ACCEPTING,
                    ACTIVITIES_WORKOFFLINE, ACTIVITIES_SEARCHREPLACE,
                    ACTIVITIES_FILES_VIEW, ACTIVITIES_FILES_EDIT,
                    ACTIVITIES_COMMENTS_VIEW, ACTIVITIES_COMMENTS_EDIT,
                    CONTENT_MANAGER, ACCOUNT_NOTIFICATION_GENERAL, 
                    ACTIVITIES_EXPORT, ACTIVITIES_EXPORT_INPROGRESS,
                    ACTIVITIES_DOWNLOAD, ACTIVITIES_ACCEPT_ALL, 
                    ACCOUNT_DOWNLOAD_ALL_OFFLINE_FILES,  
                    ACTIVITIES_DOWNLOAD_ALL,
                    ACTIVITIES_UPLOAD_SUPPORT_FILES,
                    ACTIVITIES_DETAIL_STATISTICS,
                    ACTIVITIES_SUMMARY_STATISTICS,
                    SOURCE_PAGE_EDIT,
                    COMMENT_ACCESS_RESTRICTED,
                    ACTIVITIES_COMMENTS_NEW,
                    ACTIVITIES_COMMENTS_JOB,
                    ACTIVITIES_COMMENTS_DOWNLOAD,
                    ACTIVITIES_SECONDARYTARGETFILE,
                    ACTIVITIES_JOB_COMMENTS_VIEW,
                    ACTIVITIES_JOB_COMMENTS_EDIT,
                    ACTIVITIES_JOB_COMMENTS_NEW,
                    ACTIVITIES_JOB_COMMENTS_DOWNLOAD,
                    REPORTS_TRANSLATIONS_EDIT,
                    REPORTS_CHARACTER_COUNT,
                    ACTIVITY_DASHBOARD_VIEW,
                    ACTIVITIES_BATCH_COMPLETE_ACTIVITY,
                    ACTIVITIES_BATCH_COMPLETE_WORKFLOW,
                    UPDATE_LEVERAGE
     };

    /**
     * You should add any new permissions to this call so that the
     * permission can be registered upon startup.
     *
     * NOTE: DO NOT ADD PERMISSIONS IN LOGICAL GROUPINGS,
     * ONLY ADD NEW PERMISSIONS TO THE BOTTOM OF THE LIST
     * AND DO NOT REORDER THIS LIST EVER!!!!
     */
    static private boolean addAllPermissions()
    {
        boolean added = false;
        // BEGIN -- add your permissions here.
        // SEE NOTE ABOVE!!!!!!!
        // Just add any new perm you have to the BOTTOM of this list
        // not near any logically similar permission.
        added = addPermission(LOGS_VIEW) || added;
        added = addPermission(SHUTDOWN_SYSTEM) || added;
        added = addPermission(LOCALE_PAIRS_VIEW) || added;
        added = addPermission(LOCALE_PAIRS_REMOVE) || added;
        added = addPermission(LOCALE_PAIRS_NEW) || added;
        added = addPermission(ACTIVITY_TYPES_VIEW) || added;
        added = addPermission(ACTIVITY_TYPES_REMOVE) || added;
        added = addPermission(ACTIVITY_TYPES_EDIT) || added;
        added = addPermission(ACTIVITY_TYPES_NEW) || added;
        added = addPermission(CURRENCY_VIEW) || added;
        added = addPermission(CURRENCY_EDIT) || added;
        added = addPermission(CURRENCY_NEW) || added;
        added = addPermission(PROJECTS_MANAGE) || added;
        added = addPermission(PROJECTS_MANAGE_WORKFLOWS) || added;
        added = addPermission(CALENDAR_ADMINISTER) || added;
        added = addPermission(WORKFLOW_CANCEL) || added;
        added = addPermission(RATES_VIEW) || added;
        added = addPermission(RATES_EDIT) || added;
        added = addPermission(RATES_NEW) || added;
        added = addPermission(SYS_CAL_VIEW) || added;
        added = addPermission(SYS_CAL_DUP) || added;
        added = addPermission(SYS_CAL_DEFAULT) || added;
        added = addPermission(SYS_CAL_REMOVE) || added;
        added = addPermission(SYS_CAL_EDIT) || added;
        added = addPermission(SYS_CAL_NEW) || added;
        added = addPermission(USER_CAL_VIEW) || added;
        added = addPermission(USER_CAL_EDIT) || added;
        added = addPermission(USER_CAL_EDIT_YOURS) || added;
        added = addPermission(HOLIDAY_VIEW) || added;
        added = addPermission(HOLIDAY_REMOVE) || added;
        added = addPermission(HOLIDAY_EDIT) || added;
        added = addPermission(HOLIDAY_NEW) || added;
        added = addPermission(PERMGROUPS_VIEW) || added;
        added = addPermission(PERMGROUPS_REMOVE) || added;
        added = addPermission(PERMGROUPS_EDIT) || added;
        added = addPermission(PERMGROUPS_NEW) || added;
        added = addPermission(USERS_VIEW) || added;
        added = addPermission(USERS_EDIT) || added;
        added = addPermission(USERS_REMOVE) || added;
        added = addPermission(USERS_NEW) || added;
        added = addPermission(USERS_PROJECT_MEMBERSHIP) || added;
        added = addPermission(TM_VIEW) || added;
        added = addPermission(TM_BROWSER) || added;
        added = addPermission(TM_STATS) || added;
        added = addPermission(TM_MAINTENANCE) || added;
        added = addPermission(TM_IMPORT) || added;
        added = addPermission(TM_EXPORT) || added;
        added = addPermission(TM_REINDEX) || added;
        added = addPermission(TM_DUPLICATE) || added;
        added = addPermission(TM_EDIT) || added;
        added = addPermission(TM_NEW) || added;
        added = addPermission(TMP_VIEW) || added;
        added = addPermission(TMP_EDIT) || added;
        added = addPermission(TMP_NEW) || added;
        added = addPermission(TERMINOLOGY_VIEW) || added;
        added = addPermission(TERMINOLOGY_STATS) || added;
        added = addPermission(TERMINOLOGY_INDEXES) || added;
        added = addPermission(TERMINOLOGY_REMOVE) || added;
        added = addPermission(TERMINOLOGY_DUPLICATE) || added;
        added = addPermission(TERMINOLOGY_EDIT) || added;
        added = addPermission(TERMINOLOGY_NEW) || added;
        added = addPermission(TERMINOLOGY_BROWSE) || added;
        added = addPermission(TERMINOLOGY_IMPORT) || added;
        added = addPermission(TERMINOLOGY_EXPORT) || added;
        added = addPermission(TERMINOLOGY_MAINTENANCE) || added;
        added = addPermission(TERMINOLOGY_INPUT_MODELS) || added;
        added = addPermission(PROJECTS_VIEW) || added;
        added = addPermission(PROJECTS_IMPORT) || added;
        added = addPermission(PROJECTS_EXPORT) || added;
        added = addPermission(PROJECTS_EDIT) || added;
        added = addPermission(PROJECTS_EDIT_PM) || added;
        added = addPermission(PROJECTS_NEW) || added;
        added = addPermission(WORKFLOWS_VIEW) || added;
        added = addPermission(WORKFLOWS_DUPLICATE) || added;
        added = addPermission(WORKFLOWS_REMOVE) || added;
        added = addPermission(WORKFLOWS_EDIT) || added;
        added = addPermission(WORKFLOWS_NEW) || added;
        added = addPermission(LOCPROFILES_VIEW) || added;
        added = addPermission(LOCPROFILES_REMOVE) || added;
        added = addPermission(LOCPROFILES_DETAILS) || added;
        added = addPermission(LOCPROFILES_DUP) || added;
        added = addPermission(LOCPROFILES_EDIT) || added;
        added = addPermission(LOCPROFILES_NEW) || added;
        added = addPermission(SNIPPET_IMPORT) || added;
        added = addPermission(SYSTEM_PARAMS) || added;
        added = addPermission(SUPPORT_FILES_VIEW) || added;
        added = addPermission(SUPPORT_FILES_REMOVE) || added;
        added = addPermission(SUPPORT_FILES_UPLOAD) || added;
        added = addPermission(FILE_PROFILES_VIEW) || added;
        added = addPermission(FILE_PROFILES_REMOVE) || added;
        added = addPermission(FILE_PROFILES_EDIT) || added;
        added = addPermission(FILE_PROFILES_NEW) || added;
        added = addPermission(FILE_EXT_VIEW) || added;
        added = addPermission(FILE_EXT_NEW) || added;
        added = addPermission(XMLRULE_VIEW) || added;
        added = addPermission(XMLRULE_DUP) || added;
        added = addPermission(XMLRULE_EDIT) || added;
        added = addPermission(XMLRULE_NEW) || added;
        added = addPermission(SGMLRULE_VIEW) || added;
        added = addPermission(SGMLRULE_UPLOAD) || added;
        added = addPermission(SGMLRULE_CREATE) || added;
        added = addPermission(SGMLRULE_REMOVE) || added;
        added = addPermission(SGMLRULE_EDIT) || added;
        added = addPermission(IMPORT) || added;
        added = addPermission(SERVICEWARE_IMPORT) || added;
        added = addPermission(SNIPPET_ADD) || added;
        added = addPermission(SNIPPET_EDIT) || added;
        added = addPermission(TEAMSITE_SERVER_VIEW) || added;
        added = addPermission(TEAMSITE_SERVER_REMOVE) || added;
        added = addPermission(TEAMSITE_SERVER_CREATE) || added;
        added = addPermission(TEAMSITE_SERVER_EDIT) || added;
        added = addPermission(TEAMSITE_SERVER_NEW) || added;
        added = addPermission(TEAMSITE_PROFILES_VIEW) || added;
        added = addPermission(TEAMSITE_PROFILES_REMOVE) || added;
        added = addPermission(TEAMSITE_PROFILES_NEW) || added;
        added = addPermission(DATABASE_INTEGRATION) || added;
        added = addPermission(EXPORT_LOC_VIEW) || added;
        added = addPermission(EXPORT_LOC_REMOVE) || added;
        added = addPermission(EXPORT_LOC_DEFAULT) || added;
        added = addPermission(EXPORT_LOC_EDIT) || added;
        added = addPermission(EXPORT_LOC_NEW) || added;
        added = addPermission(VIGNETTE_IMPORT) || added;
        added = addPermission(CORPUS_ALIGNER_VIEW) || added;
        added = addPermission(CORPUS_ALIGNER_CREATE) || added;
        added = addPermission(CORPUS_ALIGNER_DOWNLOAD) || added;
        added = addPermission(CORPUS_ALIGNER_UPLOAD) || added;
        added = addPermission(JOB_SCOPE_ALL) || added;
        added = addPermission(JOBS_VIEW) || added;
        added = addPermission(JOBS_SEARCH_REPLACE) || added;
        added = addPermission(JOBS_CHANGE_WFM) || added;
        added = addPermission(JOBS_DISCARD) || added;
        added = addPermission(JOBS_DISPATCH) || added;
        added = addPermission(JOBS_EXPORT_SOURCE) || added;
        added = addPermission(JOBS_EXPORT) || added;
        added = addPermission(JOBS_REEXPORT) || added;
        added = addPermission(JOBS_DETAILS) || added;
        added = addPermission(JOBS_VIEW_ERROR) || added;
        added = addPermission(JOBS_ARCHIVE) || added;
        added = addPermission(JOBS_DOWNLOAD) || added;
        added = addPermission(JOBS_MAKE_READY) || added;
        added = addPermission(JOBS_PLANNEDCOMPDATE) || added;
        added = addPermission(JOB_COMMENTS_VIEW) || added;
        added = addPermission(JOB_COMMENTS_EDIT) || added;
        added = addPermission(JOB_COSTING_VIEW) || added;
        added = addPermission(JOB_COSTING_EDIT) || added;
        added = addPermission(JOB_COSTING_REPORT) || added;
        added = addPermission(JOB_FILES_VIEW) || added;
        added = addPermission(JOB_FILES_EDIT) || added;
        added = addPermission(JOB_WORKFLOWS_VIEW) || added;
        added = addPermission(JOB_WORKFLOWS_DISCARD) || added;
        added = addPermission(JOB_WORKFLOWS_VIEW_ERROR) || added;
        added = addPermission(JOB_WORKFLOWS_WORDCOUNT) || added;
        added = addPermission(JOB_WORKFLOWS_RATEVENDOR) || added;
        added = addPermission(JOB_WORKFLOWS_ARCHIVE) || added;
        added = addPermission(JOB_WORKFLOWS_DETAILS) || added;
        added = addPermission(JOB_WORKFLOWS_EXPORT) || added;
        added = addPermission(JOB_WORKFLOWS_ADD) || added;
        added = addPermission(JOB_WORKFLOWS_EDIT) || added;
        added = addPermission(JOB_WORKFLOWS_DISPATCH) || added;
        added = addPermission(JOB_WORKFLOWS_ESTCOMPDATE) || added;
        added = addPermission(JOB_WORKFLOWS_PLANNEDCOMPDATE) || added;
        added = addPermission(JOB_WORKFLOWS_EDITEXPORTLOC) || added;
        added = addPermission(ACTIVITIES_VIEW) || added;
        added = addPermission(ACTIVITIES_ACCEPT) || added;
        added = addPermission(ACTIVITIES_EXPORT) || added;
        added = addPermission(ACTIVITIES_EXPORT_INPROGRESS) || added;
        added = addPermission(ACTIVITIES_WORKOFFLINE) || added;
        added = addPermission(ACTIVITIES_UPLOAD_SUPPORT_FILES) || added;
        added = addPermission(ACTIVITIES_SEARCHREPLACE) || added;
        added = addPermission(ACTIVITIES_FILES_VIEW) || added;
        added = addPermission(ACTIVITIES_FILES_EDIT) || added;
        added = addPermission(ACTIVITIES_COMMENTS_VIEW) || added;
        added = addPermission(ACTIVITIES_COMMENTS_EDIT) || added;
        added = addPermission(REPORTS_MAIN) || added;
        added = addPermission(REPORTS_ADMIN) || added;
        added = addPermission(REPORTS_COMPOSER) || added;
        added = addPermission(VENDORS_NEW) || added;
        added = addPermission(VENDORS_VIEW) || added;
        added = addPermission(VENDORS_EDIT) || added;
        added = addPermission(VENDORS_REMOVE) || added;
        added = addPermission(VENDORS_DETAILS) || added;
        added = addPermission(VENDORS_CUSTOMFORM) || added;
        added = addPermission(VENDORS_RATING_NEW) || added;
        added = addPermission(VENDORS_RATING_VIEW) || added;
        added = addPermission(VENDORS_RATING_EDIT) || added;
        added = addPermission(VENDORS_RATING_REMOVE) || added;
        added = addPermission(CONTENT_MANAGER) || added;
        added = addPermission(CUSTOMER_UPLOAD) || added;
        added = addPermission(DOCUMENTUM_IMPORT) || added;
        added = addPermission(GET_ALL_PROJECTS) || added;
        added = addPermission(GET_PROJECTS_I_MANAGE) || added;
        added = addPermission(GET_PROJECTS_I_BELONG) || added;
        added = addPermission(REPORTS_CUSTOM_EXTERNAL) || added;
        added = addPermission(PERMGROUPS_DETAILS) || added;
        added = addPermission(ACTIVITIES_COMMENTS_NEW) || added;
        added = addPermission(JOB_COMMENTS_NEW) || added;
        added = addPermission(ACCOUNT_NOTIFICATION_SYSTEM) || added;
        added = addPermission(ACCOUNT_NOTIFICATION_WFMGMT) || added;
        added = addPermission(ACCOUNT_NOTIFICATION_GENERAL) || added;
        added = addPermission(SOURCE_PAGE_EDIT) || added;
        added = addPermission(COMMENT_ACCESS_RESTRICTED) || added;
        added = addPermission(USERS_EDIT_ASSIGN_ANY_PERMGROUPS) || added;
        added = addPermission(JOB_SOURCE_WORDCOUNT_TOTAL) || added;
        added = addPermission(JOBS_CLEAR_ERRORS) || added;
        added = addPermission(JOB_WORKFLOWS_REASSIGN) || added;
        added = addPermission(TM_DELETE) || added;
        added = addPermission(COMMENT_EDIT_ALL_COMMENTS) || added;
        added = addPermission(FILE_PROFILES_SEE_ALL) || added;
        added = addPermission(REPORTS_WORD_COUNT) || added;
        added = addPermission(REPORTS_CUSTOM) || added;
        added = addPermission(REPORTS_JOB_COST) || added;
        added = addPermission(REPORTS_TM) || added;
        added = addPermission(REPORTS_WF_STATUS) || added;
        added = addPermission(REPORTS_JOB_DETAILS) || added;
        added = addPermission(REPORTS_AVG_PER_COMP) || added;
        added = addPermission(REPORTS_MISSING_TERMS) || added;
        added = addPermission(REPORTS_TERM_AUDIT) || added;
        added = addPermission(REPORTS_DELL_JOB_STATUS) || added;
        added = addPermission(REPORTS_DELL_ACT_DUR) || added;
        added = addPermission(REPORTS_DELL_ONLINE_JOBS) || added;
        added = addPermission(REPORTS_DELL_ONLINE_REVIEW_STATUS) || added;
        added = addPermission(JOB_WORKFLOWS_ESTREVIEWSTART) || added;
        added = addPermission(JOB_WORKFLOWS_DETAIL_STATISTICS) || added;
        added = addPermission(JOB_WORKFLOWS_SUMMARY_STATISTICS) || added;
        added = addPermission(ACTIVITIES_DETAIL_STATISTICS) || added;
        added = addPermission(ACTIVITIES_SUMMARY_STATISTICS) || added;
        added = addPermission(REPORTS_DELL_ONLINE_JOBS_RECALC) || added;
        added = addPermission(REPORTS_DELL_VENDOR_PO) || added;
        added = addPermission(REPORTS_COMMENT) || added;
        added = addPermission(REPORTS_DELL_REVIEWER_VENDOR_PO) || added;
        added = addPermission(COMPANY_VIEW) || added;
        added = addPermission(COMPANY_EDIT) || added;
        added = addPermission(COMPANY_NEW) || added;
        added = addPermission(COMPANY_REMOVE) || added;
        added = addPermission(REPORTS_CUSTOMIZE) || added;
        added = addPermission(JOB_QUOTE_VIEW) || added;
        added = addPermission(JOB_QUOTE_SEND) || added;
        added = addPermission(JOB_QUOTE_STATUS_VIEW) || added;
        added = addPermission(COSTING_EXPENSE_VIEW) || added;
        added = addPermission(COSTING_REVENUE_VIEW) || added;
        added = addPermission(REPORTS_REVIEWER_LISA_QA) || added;
        // For sla report issue
        added = addPermission(JOBS_ESTIMATEDCOMPDATE) || added;
        added = addPermission(JOBS_ESTIMATEDTRANSLATECOMPDATE) || added;
        added = addPermission(REPORTS_SLA) || added;
        
        added = addPermission(ACTIVITIES_REJECT_BEFORE_ACCEPTING) || added;
        added = addPermission(ACTIVITIES_REJECT_AFTER_ACCEPTING) || added;
        
        // For "Add job id into online job report"  issue
        added = addPermission(REPORTS_DELL_ONLINE_JOBS_ID) || added;
        
        // For "Grey out the edit buttons once the quote approve has been selected" issue
        added = addPermission(JOB_COSTING_REEDIT) || added;
        
        // For "FileProfiles Search" issue" 
        added = addPermission(FILE_PROFILES_SEARCH) || added; 
               
        
        // For " Quotation process for WebEx " issue
        added = addPermission(JOB_QUOTE_APPROVE) || added;
        added = addPermission(JOB_QUOTE_PONUMBER_EDIT) || added;
        added = addPermission(JOB_QUOTE_PONUMBER_VIEW) || added;

        //  For " add download button to my activities " issue
        added = addPermission(ACTIVITIES_DOWNLOAD) || added;
      
        //For add accept all button to my activities
        added = addPermission(ACTIVITIES_ACCEPT_ALL) || added;
        
        //For CC&BCC email access.
        added = addPermission(USERS_ACCESS_CCEMAIL) || added;
        added = addPermission(USERS_ACCESS_BCCEMAIL) || added;

        added = addPermission(REPORTS_COMMENTS_ANALYSIS) || added;
        added = addPermission(REPORTS_TRANSLATIONS_EDIT) || added;
        added = addPermission(REPORTS_LANGUAGE_SIGN_OFF) || added;
        
        added = addPermission(REPORTS_TRANSLATION_PROGRESS) || added;
        
        added = addPermission(ACTIVITIES_COMMENTS_JOB) || added;
        
        // For Implemented Comments Check 
        added = addPermission(REPORTS_IMPLEMENTED_COMMENTS_CHECK) || added;
        
        // Segmentation Rule
        added = addPermission(SEGMENTATIONRULE_VIEW) || added;
        added = addPermission(SEGMENTATIONRULE_NEW) || added;
        added = addPermission(SEGMENTATIONRULE_EDIT) || added;
        added = addPermission(SEGMENTATIONRULE_EXPORT) || added;
        added = addPermission(SEGMENTATIONRULE_REMOVE) || added;
        added = addPermission(SEGMENTATIONRULE_DUP) || added;
        
        added = addPermission(REPORTS_CHARACTER_COUNT) || added;
        added = addPermission(IN_CONTEXT_MATCH) || added;
        
        /* skip activities */
        added= addPermission(JOB_WORKFLOWS_SKIP) || added;
        
        added= addPermission(LOCALE_NEW) || added;

        added= addPermission(ACTIVITIES_COMMENTS_DOWNLOAD) || added;
        added= addPermission(CHANGE_OWN_PASSWORD) || added;
        
        added= addPermission(SERVICE_TM_SEARCH_ENTRY) || added;
        added= addPermission(SERVICE_TM_CREATE_ENTRY) || added;
        added= addPermission(SERVICE_TM_EDIT_ENTRY) || added;
        
        added= addPermission(SERVICE_TM_GET_ALL_TMPROFILES) || added;

        added= addPermission(SERVICE_TB_SEARCH_ENTRY) || added;
        added= addPermission(SERVICE_TB_CREATE_ENTRY) || added;
        added= addPermission(SERVICE_TB_EDIT_ENTRY) || added;
        added= addPermission(SERVICE_TB_GET_ALL_TB) || added;

        added= addPermission(ACTIVITIES_DOWNLOAD_ALL) || added;

        added = addPermission(FILE_EXT_REMOVE) || added;
        
        added = addPermission(ACCOUNT_DOWNLOAD_ALL_OFFLINE_FILES) || added;

        added = addPermission(JOB_FILES_DOWNLOAD) || added;
        
        added = addPermission(CONNECT_TO_CVS) || added;
        
        added = addPermission(XMLRULE_REMOVE) || added;
        
        added = addPermission(TMP_REMOVE) || added;
        
        added = addPermission(PROJECTS_REMOVE) || added;
        
        added = addPermission(REPORTS_DELL_FILE_LIST) || added;
        
        //For Filter configuration
        added = addPermission(FILTER_CONFIGURATION_VIEW) || added;
        added = addPermission(FILTER_CONFIGURATION_REMOVE_FILTERS) || added;
        added = addPermission(FILTER_CONFIGURATION_ADD_FILTER) || added;
        added = addPermission(FILTER_CONFIGURATION_EDIT_FILTER) || added;
        
        //added = addPermission(ACTIVITIES_CROWDSIGHT) || added;
        
        //XML DTD
        added = addPermission(XMLDTD_VIEW) || added;
        added = addPermission(XMLDTD_EDIT) || added;
        added = addPermission(XMLDTD_NEW) || added;
        added = addPermission(XMLDTD_REMOVE) || added;
        added = addPermission(CVS_OPERATE) || added;

        //AUTOMATIC ACTION
        added = addPermission(AUTOMATIC_ACTIONS_VIEW) || added;
        added = addPermission(AUTOMATIC_ACTIONS_REMOVE) || added;
        added = addPermission(AUTOMATIC_ACTIONS_EDIT) || added;
        added = addPermission(AUTOMATIC_ACTIONS_NEW) || added;
        //END
        added = addPermission(JOB_WORKFLOWS_PRIORITY) || added;
        
        added = addPermission(UILOCALE_VIEW) || added;
        added = addPermission(UILOCALE_REMOVE) || added;
        added = addPermission(UILOCALE_DOWNLOAD_RES) || added;
        added = addPermission(UILOCALE_UPLOAD_RES) || added;
        added = addPermission(UILOCALE_SET_DEFAULT) || added;
        added = addPermission(UILOCALE_NEW) || added;
        added = addPermission(ACTIVITIES_SECONDARYTARGETFILE) || added;
        
        added = addPermission(ATTRIBUTE_VIEW) || added;
        added = addPermission(ATTRIBUTE_NEW) || added;
        added = addPermission(ATTRIBUTE_REMOVE) || added;
        added = addPermission(ATTRIBUTE_EDIT) || added;
        added = addPermission(ATTRIBUTE_GROUP_NEW) || added;
        added = addPermission(ATTRIBUTE_GROUP_REMOVE) || added;
        added = addPermission(ATTRIBUTE_GROUP_EDIT) || added;
        added = addPermission(JOB_ATTRIBUTE_VIEW) || added;
        added = addPermission(JOB_ATTRIBUTE_EDIT) || added;
        added = addPermission(ATTRIBUTE_GROUP_VIEW) || added;
        
        added = addPermission(ACTIVITIES_JOB_COMMENTS_VIEW) || added;
        added = addPermission(ACTIVITIES_JOB_COMMENTS_EDIT) || added;
        added = addPermission(ACTIVITIES_JOB_COMMENTS_NEW) || added;
        added = addPermission(ACTIVITIES_JOB_COMMENTS_DOWNLOAD) || added;
        
        //For BlogSmith. Added by Vincent, 2010-03-31
        added = addPermission(RSS_READER) || added;
        added = addPermission(RSS_JOB) || added;
        
        added = addPermission(JOB_ATTRIBUTE_REPORT) || added;
        
        //GSEdition
        added = addPermission(GSEDITION_VIEW) || added;
        added = addPermission(GSEDITION_REMOVE) || added;
        added = addPermission(GSEDITION_EDIT) || added;
        added = addPermission(GSEDITION_NEW) || added;
        
        //GSEdition ACTION
        added = addPermission(GSEDITION_ACTIONS_VIEW) || added;
        added = addPermission(GSEDITION_ACTIONS_REMOVE) || added;
        added = addPermission(GSEDITION_ACTIONS_EDIT) || added;
        added = addPermission(GSEDITION_ACTIONS_NEW) || added;
        
        added = addPermission(CVS_Servers) || added;
        added = addPermission(CVS_Servers_NEW) || added;
        added = addPermission(CVS_Servers_EDIT) || added;
        added = addPermission(CVS_Servers_REMOVE) || added;
        added = addPermission(CVS_MODULES) || added;
        added = addPermission(CVS_MODULES_NEW) || added;
        added = addPermission(CVS_MODULES_EDIT) || added;
        added = addPermission(CVS_MODULES_REMOVE) || added;
        added = addPermission(CVS_MODULES_CHECKOUT) || added;
        added = addPermission(CVS_MODULE_MAPPING) || added;
        added = addPermission(CVS_MODULE_MAPPING_NEW) || added;
        added = addPermission(CVS_MODULE_MAPPING_EDIT) || added;
        added = addPermission(CVS_MODULE_MAPPING_REMOVE) || added;
        added = addPermission(CVS_FILE_PROFILES) || added;
        added = addPermission(CVS_FILE_PROFILES_NEW) || added;
        added = addPermission(CVS_FILE_PROFILES_EDIT) || added;
        added = addPermission(CVS_FILE_PROFILES_REMOVE) || added;
        //SEE NOTE ABOVE!!!!
        
        added = addPermission(SET_DEFAULT_ROLES) || added;
        
        added = addPermission(ADD_SOURCE_FILES) || added;
        added = addPermission(DELETE_SOURCE_FILES) || added;
        added = addPermission(EDIT_SOURCE_FILES) || added;
        
        added = addPermission(JOB_SCOPE_MYPROJECTS)||added;
        added = addPermission(ACTIVITY_DASHBOARD_VIEW)||added;
        added = addPermission(ACTIVITIES_BATCH_COMPLETE_ACTIVITY) || added;
        added = addPermission(ACTIVITIES_BATCH_COMPLETE_WORKFLOW) || added;
        
		added = addPermission(UPDATE_LEVERAGE) || added;
        added = addPermission(UPDATE_WORD_COUNTS) || added;

        return added;
    }

    /** This is a temporary singleton. */
    static private PermissionManager s_permissionManager = null;

    /** Hashmap which maps permission names to real ids. */
    static private HashMap s_idMap = new HashMap();

    /** The permission XML. **/
    //static private String s_permissionXml = null;

    /** SQL Statements. */
    static private final String SQL_SELECT_ALL = "Select id, name from permission";
    //Use max(id)+1 instead of permission_seq to ensure permission ids continuous
    // now use mysql to ensure the continous
    static private final String SQL_INSERT_PERM =
        "insert into PERMISSION values(null, ?)";

    static private final String SQL_INSERT_FIRST_PERM = 
        "insert into permission values(1, 'logs.view')";

    static private final String PERMISSION_XML_URL =
        "/globalsight/envoy/administration/permission/permissionXml.jsp";

    /*
     * The default permission groups. This is a carryover from
     * the original Access Groups, and should not be used. This can continue
     * to be used by old code that checks to see if someone is "an Admin" or
     * "a PM" , etc. But eventually that old code should all be changed to check
     * for the specific permission of interest, and not for the person's group
     */
    static public final String GROUP_ADMINISTRATOR            = "Administrator";
    static public final String GROUP_PROJECT_MANAGER          = "ProjectManager";
    static public final String GROUP_LOCALE_MANAGER           = "LocaleManager";
    static public final String GROUP_WORKFLOW_MANAGER         = "WorkflowManager";
    static public final String GROUP_LOCALIZATION_PARTICIPANT = "LocalizationParticipant";
    static public final String GROUP_CUSTOMER                 = "Customer";
    static public final String GROUP_VENDOR_ADMIN             = "VendorAdministrator";
    static public final String GROUP_VENDOR_MANAGER           = "VendorManager";
    static public final String GROUP_VENDOR_VIEWER            = "VendorViewer";

    /* The old group modules */
    static public final String GROUP_MODULE_GLOBALSIGHT =
        "GlobalSight";
    static public final String GROUP_MODULE_VENDOR_MANAGER =
        "VendorManagement";

    /**
     * Should be called during GlobalSight startup
     * to initialize permissions. This will load all
     * permissions from the database, add only the new permissions,
     * and re-load permissions into the hashap.
     */
    static public void initialize()
    {
        loadPermissions();

        if (addAllPermissions())
        {
            // new perms were added, so re-load
            loadPermissions();
        }

        s_logger.info("Initialized to contain " + s_idMap.size() +
                      " flexible user permissions.");
    }


    /**
     * Returns the PermissionXML which describes all the permissions
     * in the system and their hierarchy.
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

                //http is ok for a reference to this host
                StringBuffer permXmlUrl = new StringBuffer("http://");
                permXmlUrl.append(sc.getStringParameter(
                    SystemConfigParamNames.SERVER_HOST));
                permXmlUrl.append(":");
                permXmlUrl.append(sc.getStringParameter(
                    SystemConfigParamNames.SERVER_PORT));
                permXmlUrl.append(PERMISSION_XML_URL);
                String companyId = CompanyThreadLocal.getInstance().getValue();
                permXmlUrl.append("?companyId=").append(companyId);
                s_logger.info("permXmlUrl : " + permXmlUrl);
                
                URL u = new URL(permXmlUrl.toString());
                InputStream is = u.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String s = null;
                StringBuffer sb = new StringBuffer();

                while ((s = br.readLine()) != null)
                {
                    sb.append(s).append("\r\n");
                }
                br.close();

                s_permissionXml = sb.toString();
                s_logger.debug("s_permissionXml" + s_permissionXml);
            }
            catch (Exception ex)
            {
                s_logger.error ("Failed to read permission xml.", ex);
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
                s_logger.error("Failed to create PermissionManager", ex);
            }
        }

        return s_permissionManager;
    }

    /** Returns the mapped integer value to the given permission. */
    static int getBitValueForPermission(String p_perm)
        throws PermissionException
    {
        Long id = (Long)s_idMap.get(p_perm);

        if (id == null)
        {
            throw new PermissionException("Permission '" + p_perm +
                                          "' does not exist.");
        }

        // possible impedance mismatch...
        return(int)id.longValue();
    }

    /**
     * Clears and fills the s_idMap with the current permissions from
     * the database.
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

                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Adding permission to map: " + id + "," + name);
                }
            }
        }
        catch (Exception ex)
        {
            s_logger.error("Failed to load permissions from database.", ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Adds the given permission to the database PERMISSION table if
     * it does not already exist in the map.
     * This does update one by one, but this method should almost
     * never be called except on startup and if new permissions were
     * actually added, so there is no reason to batch.
     *
     * @param p_added set to true if anything was added
     * @param p_perm  permission name (should be above defined constant)
     */
    static private boolean addPermission(String p_perm)
    {
        boolean added = false;
        
        Connection c = null;
        PreparedStatement stmt = null;
        
        try
        {
            c = ConnectionPool.getConnection();
                
            if (s_idMap.isEmpty())
            {
                stmt = c.prepareStatement(SQL_INSERT_FIRST_PERM);
                stmt.executeUpdate();
                s_idMap.put(p_perm, new Long(1));
                
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Added " + p_perm + " to the table.");
                }
            }
            else if (!s_idMap.containsKey(p_perm))
            {
                stmt = c.prepareStatement(SQL_INSERT_PERM);
                stmt.setString(1, p_perm);
                stmt.executeUpdate();
                
                added = true;
                
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Added " + p_perm + " to the table.");
                }
            }
            
        }
        catch (Exception ex)
        {
            s_logger.error("Failed to add permission" + p_perm +
                               " to the database.", ex);
            added = false;
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentReturnConnection(c);
        }
        
        return added;
    }
    
    public static HashMap getAllPermissions(){
        return s_idMap;
    }
}