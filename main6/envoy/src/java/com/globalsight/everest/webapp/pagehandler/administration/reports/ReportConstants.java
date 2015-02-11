package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.util.HashMap;
import java.util.Map;

public interface ReportConstants
{
    public static final String EXTENSION_XLSX = ".xlsx";
    public static final String REPORTS_SUB_DIR = "Reports";
    public static final String REPORTS_NAME = "GSReports";
    public static final String GENERATE_REPORT = "generateReport";
    public static final String GENERATE_REPORTS = "generateReports";
    public static final String GET_REPORT = "getReport";
    public static final String PARENT_NODE = "parentNode";
    
    public static final String ACTION_GET_PERCENT = "getPercent";
    public static final String ACTION_GET_REPORTSDATA = "getReportsData";
    public static final String ACTION_CHECK_SOURCE_LOCALE = "checkSourceLocale";
    public static final String ACTION_CANCEL_REPORTS = "cancelReports";
    public static final String ACTION_CANCEL_REPORTS_FROMRECENTREPORTS = "cancelReportsFromRecentReports";
    public static final String ACTION_CANCEL_REPORT = "cancelReport";
    public static final String ACTION_REFRESH_PROGRESS = "refreshProgress";
    public static final String ACTION_GENERATE_SUMMARY_PERCENT = "generateSummaryReport";
    
    public static final String ACTION_VIEW = "view";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DOWNLOAD = "download";
    
    // Reports Type
    public static final String ACTIVITY_DURATION_REPORT = "ActivityDurationReport";
    public static final String ONLINE_JOBS_REPORT = "OnlineJobsReport";
    public static final String DETAILED_WORDCOUNTS_REPORT = "DetailedWordCountsReport";
    
    public static final String REVIEWERS_COMMENTS_REPORT = "ReviewersCommentsReport";
    public static final String REVIEWERS_COMMENTS_REPORT_ABBREVIATION = "RCR";
    
    public static final String REVIEWERS_COMMENTS_SIMPLE_REPORT = "ReviewersCommentsSimpleReport";
    public static final String REVIEWERS_COMMENTS_SIMPLE_REPORT_ABBREVIATION = "RCSR";
    
    public static final String COMMENTS_ANALYSIS_REPORT = "CommentsAnalysisReport";
    public static final String CHARACTER_COUNT_REPORT = "CharacterCountReport";
    public static final String TRANSLATIONS_EDIT_REPORT = "TranslationsEditReport";
    public static final String TRANSLATIONS_EDIT_REPORT_ABBREVIATION = "TER";
    public static final String SUMMARY_REPORT = "SummaryReport";
    
    public static final String COMMENTS_REPORT = "CommentsReport";
    public static final String JOB_STATUS_REPORT = "JobStatusReport"; 
    public static final String ONLINE_REVIEW_STATUS_REPORT = "OnlineReviewStatusReport"; 
    public static final String VENDOR_PO_REPORT = "VendorPOReport"; 
    public static final String REVIEWER_VENDOR_PO_REPORT = "ReviewerVendorPOReport";
    public static final String TRANSLATION_PROGRESS_REPORT = "TranslationProgressReport"; 
    public static final String TRANSLATION_SLA_PERFORMANCE_REPORT = "TranslationSLAPerformanceReport";
    public static final String CUSTOMIZEREPORTS_REPORT = "CustomizeReports";
    public static final String IMPLEMENTED_COMMENTS_CHECK_REPORT = "ImplementedCommentsCheckReport";
    public static final String JOB_ATTRIBUTE_REPORT = "JobAttributeReport"; 
    
    // Attribute name in request/session
    public static final String JOB_IDS = "inputJobIDS";
    public static final String REPORT_TYPE = "reportType";
    public static final String REPORTJOBINFO_LIST = "reportsJobInfoList";
    public static final String TARGETLOCALE_LIST = "targetLocalesList";
    public static final String PROJECT_LIST = "reportsProjectList";
    public static final String L10N_PROFILES = "l10nProfiles";
    public static final String SUB_DIR = "subDir";
    
    // Excel parameter name
    public static final String CATEGORY_LIST = "categoryList";
    
    public static final Map<String, String> reportNameMap = new HashMap<String, String>()
    {
        private static final long serialVersionUID = -4245555507397871284L;
        {
            put(REVIEWERS_COMMENTS_REPORT,
                    REVIEWERS_COMMENTS_REPORT_ABBREVIATION);
            put(REVIEWERS_COMMENTS_SIMPLE_REPORT,
                    REVIEWERS_COMMENTS_SIMPLE_REPORT_ABBREVIATION);
            put(TRANSLATIONS_EDIT_REPORT,
            		TRANSLATIONS_EDIT_REPORT_ABBREVIATION);
        }
    };

    public static final String ERROR_PAGE = "/envoy/administration/reports/error.jsp";
}
