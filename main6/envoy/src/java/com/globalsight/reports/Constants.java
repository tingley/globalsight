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
package com.globalsight.reports;


public class Constants 
{
    // WorkFlowStatusReport (request attribute keys)
    public final static String WORKFLOWSTATUS_REPORT_KEY = "workflowstatusReport";
    public final static String PROJECT_MGR_ARRAY = "projectMgrArray";
    public final static String PROJECT_MGR_DEFVALUE = "pm_defvalue";
    public final static String WFSTATUS_ARRAY = "wfstatusArray";
    public final static String CURRENCY_ARRAY = "currencyArray";
    public final static String CURRENCY_ARRAY_LABEL = "currencyArraylabel";
    public final static String PIVOT_CURRENCY_PM_DEFVALUE = "pivotCurrency_defvalue";
    
    public final static String PROJECT_MGR_LABEL = "projectMgrLabel";
    public final static String WFSTATUS_LABEL = "wfstatusLabel";
    public final static String CURRENCY_DISPLAYNAME_LABEL = "currencyLabel";
    public final static String WORKFLOW_REPORT_DATA = "workflowReportData";
    public final static String WORKFLOW_REPORT_DATASOURCE = "workflowReportDataSource";
    
    // WorkFlowStatusReport (resource bunddle keys)
    public final static String PROJECT_MGR = "projectMgr";
    public final static String WFSTATUS = "wfstatus";
    public final static String CURRENCY = "currency";
    public final static String CRITERIA_ALLPMS = "criteria_allPms";
    public final static String TOTALJOBSNUM = "totalNumJobs";
    public final static String REPORT_TITLE = "txtReportTitle";
    
    // For all Reports (XML nodes keys)
    public static final String REPORT_CONFIG_FILE = "/resources/ReportConfig.xml";
    public final static String REPORTMODULE_NODE_XPATH = "//WebApplication/ReportModule";
    public final static String REPORTHANDLER_NODE = "ReportHandler";
    public final static String REPORTURL_NODE_XPATH = "//WebApplication/ReportUrl";
    
    public final static String REPORTNAME_ATTRIBUTE = "reportName";
    public final static String CLASS_ATTRIBUTE = "class";
    public final static String JSPNAME_ATTRIBUTE = "jspName";
    public final static String TARGETURL_ATTRIBUTE = "targetUrl";
    
    // For all Reports (Common use)
    public final static String REPORT_PAGE_NAME = "reportPageName";
    public final static String REPORT_ACT = "act";
    public final static String REPORT_ACT_TURNPAGE = "turnpage";
    public final static String REPORT_ACT_PREP = "Prepare";
    public final static String REPORT_ACT_CREATE = "Create";
    public final static String REPORT_TXT_FOOT = "txtFooter";
    public final static String REPORT_TXT_DATEFORMAT = "yyyy/MM/dd HH:mm z";
    public final static String REPORT_TXT_PAGENUM = "txtPageNumber";
    public final static String REPORT_DATA_WRAP = "DataWrap";

    // AvgPerCompPeport (request attribute keys)
    public final static String AVGPERCOMP_REPORT_KEY = "avgpercompReport";
    public final static String AVGPERCOMP_REPORT_DATA = "avgpercompReportData";
    public final static String AVGPERCOMP_REPORT_DATASOURCE = "avgpercompReportDataSource";
    
    // JobDetailsReport
    public final static String JOBDETAILS_REPORT_KEY = "jobDetailsReport";
    public final static String JOB_RADIO_LABEL_MAP = "jobRadioLabelMap";
    public final static String JOB_SELECT_LABEL_MAP = "jobSelectLabelMap";
    public final static String JOB_SELECT_NAME_MAP = "jobSelectNameMap";
    
    public final static String JOB_STATUS_LABEL = "jobStatusLabel";
    public final static String JOB_STATUS_SESSION_MAP = "jobStatusSessionMap";
    public final static String DISPATCHED_JOBID_ARRAY = "dispatchedJobidArray";
    public final static String LOCALIZED_JOBID_ARRAY = "localizedJobidArray";
    public final static String EXPORTED_JOBID_ARRAY = "exportedJobidArray";
    public final static String ARCHIVED_JOBID_ARRAY = "archivedJobidArray";
    public final static String JOB_DETAILS_REPORT_DATA = "jobDetailsReportData";
    public final static String JOB_LIST_MAP ="jobListMap";
    public final static String CRITERIA_FORM = "criteriaForm";
    public final static String JOB_FORM = "jobForm";
    public final static String PAGE_TITLE = "pageTitle";
    
    // MissingTermsReport
    public final static String MISSINGTERMS_REPORT_KEY = "missingtermsReport";
    public final static String MISSINGTERMS_REPORT_DATA = "missingtermsReportData";
    public final static String PAGEDATAWRAP_HASHMAP = "pageDataWrapHashMap";
    public final static String TERMBASE_NAME = "termbaseName";
    public final static String TERMBASELANG_LABEL = "termbaseLangLabel";
    public final static String TERMBASE_LABEL = "termbaseLabel";
    public final static String TERMBASE_LANGS = "termbaseLangs";
    public final static String TERMBASE_LANG_LABLES = "termbaseLangLables";
    public final static String TERMBASE_HASHMAP = "termbaseHashMap";
    public final static String TERMBASE = "termbase";
    public final static String LANGUAGE = "language";
    public final static String REPORT_SHOWPAGE_PAGEID = "pageId";
    public final static String MISSING = "missing";
    public final static String NOMISSING = "noMissing";
    public final static String TOTALMISSING = "totalMissing";
    public final static String ENTRY = "entry";
    
    // TMReport
    public final static String TM_REPORT_KEY = "tmReport";
    public final static String TM_REPORT_CURRENT_PAGE_LIST = "tmReportCurrentPageList";
    public final static String TM_REPORT_PAGE_NUM = "pagenum";
    
    //CostsByLocaleReport
    public final static String COSTS_BY_LOCALE_REPORT_KEY = "costsByLocaleReport";
    public final static String COSTS_BY_LOCALE_REPORT_CURRENT_PAGE_LIST = "costsByLocaleReportCurrentPageList";
    public final static String COSTS_BY_LOCALE_REPORT_PAGE_NUM = "pagenum";
    
  //CostsByLocaleReport
    public final static String TASK_DURATION_REPORT_KEY = "taskDurationReport";
    public final static String TASK_DURATION_REPORT_CURRENT_PAGE_LIST = "taskDurationReportCurrentPageList";
    public final static String TASK_DURATION_REPORT_PAGE_NUM = "pagenum";
    
    // TermAudit Report
    public final static String TERMAUDIT_REPORT_KEY = "termauditReport";
    public final static String PARAM_STARTDATE_LABEL = "startDate_display";
    public final static String PARAM_STARTDATE = "startDate";
    public final static String PARAM_ENDDATE_LABEL = "endDate_display";
    public final static String PARAM_ENDDATE = "endDate";
    public final static String PARAM_LANGUAGE_LABEL = "languageLabel";
    public final static String PARAM_LANGUAGE = "language";
    public final static String PARAM_LANGUAGE_LABELS = "languageLabels";
    public final static String PARAM_SELECTEDLANG = "selectedLang";
    public final static String PERIODSTART = "periodStart";
    public final static String PERIODEND = "periodEnd";
    public final static String DATARESOURCE = "dataResource";
    public final static String LANG = "lang";
    
    // CostingReport
    public final static String COSTING_REPORT_KEY = "costingReport";
    public final static String CONTENT_TYPE_LABEL = "label";
    public final static String CONTENT_TYPE_FIELD = "field";
    public final static String CONTENT_TYPE_NOTE = "note";
    public final static String CONTENT_TYPE_INTETER_ARRAY = "integerarray";
    public final static String CONTENT_TYPE_TABLE_MODEL = "tablemodel";
    public final static String CONTENT_TYPE_TITLE = "title";
    public final static String CONTENT_TYPE_TABLE = "table";
    public final static String ACTIVITY_TABLE = "activityTable";
    public final static String PAGENAME_TABLE = "pagenameTable";
    public final static String SURCHARGES_TABLE = "surchargesTable";
    public final static int FIRST_PAGE_NUM = 1;
    public static final String DESCRIPTION = "description";

}
