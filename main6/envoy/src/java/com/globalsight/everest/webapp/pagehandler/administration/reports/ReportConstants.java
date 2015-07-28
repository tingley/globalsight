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

    public static final String ACTION_QA_REPORT_GENERATE = "generateQAReport";
    public static final String ACTION_QA_REPORT_GET = "getQAReport";
    public static final String ACTION_QA_REPORT_UPLOAD = "uploadQAReport";
    public static final String ACTION_QA_REPORT_CANCEL = "cancelQAReport";

    public static final String GENERATE_DITA_REPORT = "generateDitaReport";
    public static final String GET_DITA_REPORT = "getDitaReport";

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

    public static final String ACTION_UPLOAD = "upload";

    // Reports Type
    public static final String ACTIVITY_DURATION_REPORT = "ActivityDurationReport";
    public static final String ONLINE_JOBS_REPORT = "OnlineJobsReport";
    public static final String ONLINE_JOBS_REPORT_FOR_IPTRANSLATOR = "OnlineJobsReportForIPTranslator";
    public static final String ONLINE_JOBS_REPORT_FOR_IPTRANSLATOR_ABBREVIATION = "OnlineJobsReport4IPTranslator";

    public static final String DETAILED_WORDCOUNTS_REPORT = "DetailedWordCountsReport";

    public static final String REVIEWERS_COMMENTS_REPORT = "ReviewersCommentsReport";
    public static final String REVIEWERS_COMMENTS_REPORT_ABBREVIATION = "RCR";

    public static final String REVIEWERS_COMMENTS_SIMPLE_REPORT = "ReviewersCommentsSimpleReport";
    public static final String REVIEWERS_COMMENTS_SIMPLE_REPORT_ABBREVIATION = "RCSR";

    public static final String COMMENTS_ANALYSIS_REPORT = "CommentsAnalysisReport";
    public static final String SCORECARD_REPORT = "ScorecardReport";
    public static final String CHARACTER_COUNT_REPORT = "CharacterCountReport";
    public static final String TRANSLATIONS_EDIT_REPORT = "TranslationsEditReport";
    public static final String POST_REVIEW_QA_REPORT = "PostReviewQAReport";
    public static final String TRANSLATION_VERIFICATION_REPORT = "TranslationVerificationReport";

    public static final String TRANSLATIONS_EDIT_REPORT_ABBREVIATION = "TER";
    public static final String SUMMARY_REPORT = "SummaryReport";
    public static final String POST_REVIEW_REPORT_ABBREVIATION = "PRR";
    public static final String TRANSLATIONS_VERIFICATION_REPORT_ABBREVIATION = "TVR";

    public static final String COMMENTS_REPORT = "CommentsReport";
    public static final String JOB_STATUS_REPORT = "JobStatusReport";
    public static final String ONLINE_REVIEW_STATUS_REPORT = "OnlineReviewStatusReport";
    public static final String VENDOR_PO_REPORT = "VendorPOReport";
    public static final String REVIEWER_VENDOR_PO_REPORT = "ReviewerVendorPOReport";
    public static final String TRANSLATION_PROGRESS_REPORT = "TranslationProgressReport";
    public static final String TRANSLATION_SLA_PERFORMANCE_REPORT = "TranslationSLAPerformanceReport";
    public static final String CUSTOMIZEREPORTS_REPORT = "CustomizeReports";

    public static final String IMPLEMENTED_COMMENTS_CHECK_REPORT = "ImplementedCommentsCheckReport";
    public static final String IMPLEMENTED_COMMENTS_CHECK_REPORT_ABBREVIATION = "ICCR";

    public static final String JOB_ATTRIBUTE_REPORT = "JobAttributeReport";

    public static final String REPORT_QA_CHECKS_REPORT = "QAChecksReport";
    public static final String PREFIX_QA_CHECKS_REPORT = "QCR";

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
            put(TRANSLATIONS_EDIT_REPORT, TRANSLATIONS_EDIT_REPORT_ABBREVIATION);
            put(IMPLEMENTED_COMMENTS_CHECK_REPORT,
                    IMPLEMENTED_COMMENTS_CHECK_REPORT_ABBREVIATION);
            put(ONLINE_JOBS_REPORT_FOR_IPTRANSLATOR,
                    ONLINE_JOBS_REPORT_FOR_IPTRANSLATOR_ABBREVIATION);
            put(POST_REVIEW_QA_REPORT, POST_REVIEW_REPORT_ABBREVIATION);
            put(TRANSLATION_VERIFICATION_REPORT,
                    TRANSLATIONS_VERIFICATION_REPORT_ABBREVIATION);
        }
    };

    public static final String ERROR_PAGE = "/envoy/administration/reports/error.jsp";
}
