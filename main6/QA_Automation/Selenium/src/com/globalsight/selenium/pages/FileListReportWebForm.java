package com.globalsight.selenium.pages;

import com.globalsight.selenium.testcases.ConfigUtil;

/**
 * FileList Report
 * 
 * @author leon
 * 
 */
public class FileListReportWebForm
{
    public static final String REPORT_LINK = "link="
            + ConfigUtil.getConfigData("company") + " Detailed Word Counts by Job";
    public static final String POPUP_WINDOW_NAME = "FileListundefined1";

    public static final String PROJECT_SELECTOR = "projectNameList";
    public static final String JOBSTATUS_SELECTOR = "jobStatus";
    public static final String TARGETLOCALE_SELECTOR = "targetLocalesList";
    public static final String STARTSTIME = "csf";
    public static final String STARTSTIMEUNITS = "cso";
    public static final String ENDSTIME = "cef";
    public static final String ENDSTIMEUNITS = "ceo";
    public static final String DATEFORMAT = "dateFormat";
    
    public static final String EXPORTFORMATCSV = "//input[@name='exportFormat' and @value='csv']";
    public static final String SUBMIT_BUTTON = "submitButton";

    public static final String REPORT_FILE_NAME_XLS = "DetailedWordCountByJob.xls";
    public static final String REPORT_FILE_NAME_CSV = "DetailedWordCountByJob.csv";
}
