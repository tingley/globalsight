package com.globalsight.selenium.pages;

import com.globalsight.selenium.testcases.ConfigUtil;

public class ActivityDurationReportWebForm
{
    public static final String REPORT_LINK = "link="
            + ConfigUtil.getConfigData("company") + " Activity Duration";
    public static final String POPUP_WINDOW_NAME = "ActivityDurationundefined1";
    public static final String PROJECTS_SELECTOR = "projectId";
    public static final String JOBSTATUS_SELECTOR = "status";
    public static final String TARGETLOCALE_SELECTOR = "targetLocalesList";
    public static final String STARTSTIME = "csf";
    public static final String STARTSTIMEUNITS = "cso";
    public static final String ENDSTIME = "cef";
    public static final String ENDSTIMEUNITS = "ceo";
    public static final String DATEFORMAT = "dateFormat";
    
    public static final String SUBMIT_BUTTON = "//input[@value='Submit']";
    
    public static final String REPORT_FILE_NAME = "ActivityDuration.xls";
}
