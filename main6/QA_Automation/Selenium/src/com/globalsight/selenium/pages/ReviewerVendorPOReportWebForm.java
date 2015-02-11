package com.globalsight.selenium.pages;

import com.globalsight.selenium.properties.ConfigUtil;

public class ReviewerVendorPOReportWebForm
{
    public static final String REPORT_LINK = "link="
            + ConfigUtil.getConfigData("COMPANY_NAME") + " Reviewer Vendor PO";
    public static final String POPUP_WINDOW_NAME = "ReviewerVendorPOundefined1";

    public static final String PROJECTS_SELECTOR = "projectId";
    public static final String JOBSTATUS_SELECTOR = "status";
    public static final String TARGETLOCALE_SELECTOR = "targetLang";
    public static final String ACTIVITY_SELECTOR = "activityName";
    public static final String STARTSTIME = "csf";
    public static final String STARTSTIMEUNITS = "cso";
    public static final String ENDSTIME = "cef";
    public static final String ENDSTIMEUNITS = "ceo";
    public static final String CURRENCY = "currency";
    public static final String Re_CALCULATE = "recalc";

    public static final String SUBMIT_BUTTON = "//input[@value='Submit']";

    public static final String REPORT_FILE_NAME = "ReviewerVendorPO.xls";
}
