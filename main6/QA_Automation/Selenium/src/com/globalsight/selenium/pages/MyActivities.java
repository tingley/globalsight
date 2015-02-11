package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the My Activities affairs.
 */

public class MyActivities {

	//My Activities -Search
	public static final String JobName_TEXT_FIELD="nf";
	public static final String JobName_SELECT="no";
	public static final String JobID_SELECT="io";
	public static final String JobID_TEXT_FIELD="idf";
	public static final String ActivityName_TEXT_FIELD="af";
	public static final String Status_SELECT="sto";
	public static final String Company_SELECT="comanyo";
	
	public static final String SourceLocale_SELECT="sl";
	public static final String TargetLocale_SELECT="tl";
	public static final String Priority_SELECT="pro";
	public static final String AcceptStarts_TEXT_FIELD="asf";
	public static final String AcceptStarts_SELECT="aso";
	public static final String EstimatedStarts_TEXT_FIELD="aef";
	public static final String EstimatedStarts_SELECT="aeo";
	
	public static final String Search_BUTTON="Search";
	public static final String Clear_BUTTON="//input[@name='Clear']";
	
	public static final String Accept_BUTTON="AcceptAllButton";
	public static final String DetailedWordCounts_BUTTON="WordCountButton";
	public static final String Export_BUTTON="ExportButton";
	public static final String Download_BUTTON="DownloadButton";
	public static final String SearchReplace_BUTTON="SearchButton";
	public static final String MyActivities_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/form/table[1]/tbody[2]";
	
	
	//MyActivities Available
	public static final String Status_SELECTION="sto";
	public static final String JobName_SELECTION="no";
	public static final String Search_TEXT_FIELD="nf";
	
	public static final String CheckAll_LINK="link=Check All";
	public static final String ClearAll_LINK="link=Clear All";
	
	
	//job:*
	public static final String Details_LINK="link=Details";
	public static final String Comments_LINK="link=Comments";
	public static final String WorkOffline="link=Work Offline";
	public static final String Details_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr/td/table/tbody/tr[4]/td/table/tbody/tr/td[1]/form/table/tbody";
	public static final String Details_TABLE_NEXTACTIVITIES="//div[@id='contentLayer']/table[2]/tbody/tr/td/table/tbody/tr[4]/td/table/tbody/tr/td[1]/form/table[2]/tbody[3]";
	public static final String Search_TEXT_FIELD_Job="pageSearchParam";
	
	public static final String Accept_BUTTON_Job="//input[@value='Accept']";
	public static final String Reject_BUTTON="//input[@value='Reject']";
	public static final String Export_BUTTON_Job="ExportButton";
	public static final String Download_BUTTON_Job="DownloadButton";
	public static final String BacktoActivities_BUTTON="previous";
	public static final String DetailedWordCounts_BUTTON_Job="//input[@value='Detailed Word Counts...']";
	public static final String Search_BUTTON_Job="//input[@value='Search']";
	public static final String TranslatedText_BUTTON="//input[@value='Translated Text']";
	public static final String TaskCompleted_BUTTON="//input[@value='Task Completed']";
	public static final String TargetFiles_TABLE="//div[@id='data']/table/tbody";
	public static final String PopupEditor_FRAME="WebFX_PopUp";
	
	//Detailed Word Counts:****
	
	public static final String DetailedStatistics_TABLE="//div[@id='contentLayer']/form/p[1]/table/tbody/tr[2]/td/table/tbody";
	public static final String SummaryStatistics_TABLE="//div[@id='contentLayer']/form/p[2]/table/tbody/tr[2]/td/table/tbody";
	
	public static final String BacktoActivities_BUTTON_WordCounts="//input[@value='Back to Activities']";
}
