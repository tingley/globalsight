package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Rates affairs.
 */

public class Rates {
	
	//Rates
	public static final String New_BUTTON="//input[@value='New...']";
	public static final String Edit_BUTTON="edit";
	public static final String Remove_BUTTON="remove";
	public static final String Rates_TABLE="//div[@id='contentLayer']//form/table//tbody//tr[2]//td//table//tbody";
	
	
	//New Rate
    public static final String Name_TEXT_FIELD = "rateName";
    public static final String ActivityType_SELECT = "activity";
    public static final String LocalePair_SELECTION = "lp";
    public static final String Currency_SELECT = "//select[@name='currency']";
    public static final String RateType_SELECT = "rateType";

	public static final String Save_BUTTON="//input[@value='Save']";
	public static final String Cancel_BUTTON="Cancel";

	
	//Edit WordCount Rate
	public static final String In_Context_FIELD = "inContextExact";
	public static final String Hundred_FIELD = "exact";
	public static final String BAND1_FIELD = "band1";
	public static final String BAND2_FIELD = "band2";
	public static final String BAND3_FIELD = "band3";
	public static final String BAND4_FIELD = "band4";
	public static final String No_match_FIELD = "nomatch";
	public static final String No_Match_Repetition_FIELD = "repetition";
	
	public static final String DecimalDigits_FIELD = "decimalDigits";
	public static final String BaseRate_FIELD = "baserate";
	public static final String Calculate_BUTTON="calculate";


}