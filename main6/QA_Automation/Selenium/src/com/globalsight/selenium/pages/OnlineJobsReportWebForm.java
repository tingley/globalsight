package com.globalsight.selenium.pages;

import com.globalsight.selenium.properties.ConfigUtil;

/**
 * OnlineJobs Report
 * 
 * @author leon
 * 
 */
public class OnlineJobsReportWebForm
{
    public static final String REPORT_LINK = "link="
            + ConfigUtil.getConfigData("COMPANY_NAME") + " Online Jobs";
    public static final String POPUP_WINDOW_NAME = "OnlineJobsundefined1";

    public static final String PROJECT_SELECTOR = "projectId";
    public static final String JOBSTATUS_SELECTOR = "status";
    public static final String TARGETLOCALE_SELECTOR = "targetLocalesList";

    public static final String RECALC_SELECTOR = "recalc";
    public static final String DISPLAYJOBID_SLECTOR = "jobIdVisible";
    public static final String INCLUDEREVIEW_CHECKBOX = "review";

    public static final String YEARREPORT = "yearReport";
    public static final String YEAR = "year";

    public static final String DETAILREPORT = "detailReport";
    public static final String STARTSTIME = "csf";
    public static final String STARTSTIMEUNITS = "cso";
    public static final String ENDSTIME = "cef";
    public static final String ENDSTIMEUNITS = "ceo";

    public static final String DATEFORMAT = "dateFormat";
    public static final String CURRENCY = "currency";

    public static final String MATCHES = "reportStyle";

    public static final String SUBMIT_BUTTON = "//input[@value='Submit']";

    public static final String REPORT_FILE_NAME = "OnlineJobs.xls";
}
