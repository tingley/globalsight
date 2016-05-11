package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the My Activities affairs.
 */

public class MyActivities implements BasePage
{

	public static final String TABLE_TABLE="//table[@id='dataList']/tbody";
    // My Activities -Search
    public static final String SEARCH_JOB_NAME_TEXT = "jobNameFilter";
    public static final String SEARCH_JOB_CONDITION_SELECT = "no";
    public static final String SEARCH_JOB_ID_SELECT = "io";
    public static final String SEARCH_JOB_ID_TEXT = "idf";
    public static final String SEARCH_ACTIVITY_NAME_TEXT = "af";
    public static final String SEARCH_STATUS_SELECT = "sto";
    public static final String SEARCH_COMPANY_SELECT = "comanyo";

    public static final String SEARCH_JOB_CONDITION_BEGIN_WITH = "begins with";
    public static final String SEARCH_JOB_CONDITION_END_WITH = "ends with";
    public static final String SEARCH_JOB_CONDITION_CONTAINS = "contains";

    public static final String SEARCH_SOURCE_LOCALE_SELECT = "sl";
    public static final String SEARCH_TARGET_LOCALE_SELECT = "tl";
    public static final String SEARCH_PRIORITY_SELECT = "pro";
    public static final String SEARCH_ACCEPT_START_DATE_TEXT = "asf";
    public static final String SEARCH_ACCEPT_START_DATE_SELECT = "aso";
    public static final String SEARCH_ESTIMATED_START_DATE_TEXT = "aef";
    public static final String SEARCH_ESTIMATED_START_DATE_SELECT = "aeo";

    public static final String ACCEPT_ALL_BUTTON = "AcceptAllButton";
    public static final String DETAILED_WORD_COUNTS_BUTTON = "WordCountButton";
    public static final String EXPORT_BUTTON = "ExportButton";
    public static final String EXPORT_DOWNLOAD_BUTTON = "exportDownloadBtn";
    public static final String EXPORT_DOWNLOAD_CANCEL_OK_BUTTON = "idExportDownloadCancelOkDownload";

    public static final String DOWNLOAD_BUTTON = "DownloadButton";
    public static final String SEARCH_AND_REPLACE_BUTTON = "SearchButton";
    public static final String MY_ACTIVITIES_TABLE = "//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/form/table/tbody[2]";
    public static final String CANCEL_OK_DOWNLOAD_BUTTON = "idCancelOkDownload";
    
    public static final String MYACTIVITIES_LIST_TARGET_LOCALE_FILTER_SELECT = "id=targetLocaleFilter";


    // MyActivities Available
    public static final String CHECK_ALL_LINK = "link=Check All";
    public static final String CLEAR_ALL_LINK = "link=Clear All";

    // job:*
    public static final String ACTIVITY_DETAILS_LINK = "link=Details";
    public static final String ACTIVITY_COMMENTS_LINK = "link=Comments";
    public static final String WORK_OFFLINE_TAB = "link=Work Offline";
    public static final String ACTIVITY_DETAILS_TABLE = "//div[@id='contentLayer']/table[2]/tbody/tr/td/table/tbody/tr[4]/td/table/tbody/tr/td[1]/form/table/tbody";
    public static final String ACTIVITY_DETAILS_TABLE_NEXTACTIVITIES = "//div[@id='contentLayer']/table[2]/tbody/tr/td/table/tbody/tr[4]/td/table/tbody/tr/td[1]/form/table[2]/tbody[3]";
    public static final String ACTIVITY_DETAILS_SEARCH_TEXT = "pageSearchParam";

    public static final String ACCEPT_JOB_BUTTON = "//input[@value='Accept']";
    public static final String REJECT_BUTTON = "//input[@value='Reject']";
    public static final String BACK_TO_ACTIVITY_BUTTON = "previous";
    public static final String DETAILED_WORD_COUNT_IN_JOB_BUTTON = "//input[@value='Detailed Word Counts...']";
    public static final String TRANSLATED_TEXT_BUTTON = "//input[@value='Translated Text']";
    public static final String TASK_COMPLETED_BUTTON = "//input[@value='Task Completed']";
    public static final String TARGET_FILES_TABLE = "//div[@id='data']/table/tbody";
    public static final String POPUP_EDITOR_FRAME = "WebFX_PopUp";

    // Detailed Word Counts:****

    public static final String DETAILED_STATISTICS_TABLE = "//div[@id='contentLayer']/form/p[1]/table/tbody/tr[2]/td/table/tbody";
    public static final String SUMMARY_STATISTICS_TABLE = "//div[@id='contentLayer']/form/p[2]/table/tbody/tr[2]/td/table/tbody";

    public static final String BACK_TO_ACTIVITIES_FROM_WORD_COUNT_BUTTON = "//input[@value='Back to Activities']";
    
    public static final String TASK_ID_CHECKBOX = "taskId";
    public static final String DOWNLOAD_BUTTON_ACTIVITY_LIST = "downloadBtn";
}
