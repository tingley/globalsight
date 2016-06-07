package com.globalsight.selenium.testcases.testmatrix.regression;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.ExportWorkflowFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.MTProfileFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.XMLRules;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.pages.MyAccount;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class OfflineDownload_Combined extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private XMLRulesFuncs iXMLRuleConfig = new XMLRulesFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
	private TMProfileFuncs iTMProfileFuncs = new TMProfileFuncs();
	private MTProfileFuncs iMTProfileFuncs  = new MTProfileFuncs();
	private LocalizationFuncs iLocalizationFuncs  = new LocalizationFuncs();
	private TMFuncs iTMFuncs = new TMFuncs();
	private CreateJobsFuncs tmp = new CreateJobsFuncs();
	private ExportWorkflowFuncs export = new ExportWorkflowFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testOfflineDownload_Combined() throws Exception {

		
        
		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"OfflineDownload_CombinedTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		String file_Format;	
		String tM_Options;	
		String mT_matches_into_separate_TM_file;	
		String penalized_Reference_TM_Options;	
		String terminology;
		String allow_Edit_Locked_Segments;	
		String populate_100_Target_Segments;	
		String populate_Fuzzy_Target_Segments;	
		String preserve_Source_Folder_Structure;	
		String consolidate_Split_Type;	
		String include_Repeated_Segments_as_Separate_File;  	
		String nOT_include_fully_leveraged_file;
		String include_XML_Node_Context_Information;

		
		String filterJob = getStringToday();
//		String filterJob = "201511-04-115231";

    	
		int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];
		
		BasicFuncs basic = new BasicFuncs();
		if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
		
		
		for (int i = 23; i < testCases.size(); i++) {
//		for (int i = 1; i < 18; i++) {
			file_Format = testCases.get(i)[1];
			tM_Options = testCases.get(i)[2];	
			mT_matches_into_separate_TM_file = testCases.get(i)[3];	
			penalized_Reference_TM_Options = testCases.get(i)[4];	
			terminology = testCases.get(i)[5];
			allow_Edit_Locked_Segments = testCases.get(i)[6];	
			populate_100_Target_Segments = testCases.get(i)[7];	
			populate_Fuzzy_Target_Segments = testCases.get(i)[8];	
			preserve_Source_Folder_Structure = testCases.get(i)[9];	
			consolidate_Split_Type = testCases.get(i)[10];	
			include_Repeated_Segments_as_Separate_File = testCases.get(i)[11];  	
			nOT_include_fully_leveraged_file = testCases.get(i)[12];
			include_XML_Node_Context_Information = testCases.get(i)[13];
					
			
			CommonFuncs.loginSystemWithAnyone(selenium);
			
			iSelniumUtils.clickAndWait(selenium, "link=My Account");
			iSelniumUtils.clickAndWait(selenium, "//input[@value='Download Options...']");
			
			switch (file_Format)
			{
			 case "Xliff 1.2":
			 {
				 selenium.select(MyAccount.MYACCOUNT_FORMAT_SELECT, MyAccount.MYACCOUNT_FORMAT_XLIFF12_TEXT);
				 break;
			 }
			 case "Xliff 2.0":
			 {
				 selenium.select(MyAccount.MYACCOUNT_FORMAT_SELECT, MyAccount.MYACCOUNT_FORMAT_XLIFF20_TEXT);
				 break;
			 }
			 case "Optimized Bilingual TRADOS RTF":
			 {
				 selenium.select(MyAccount.MYACCOUNT_FORMAT_SELECT, MyAccount.MYACCOUNT_FORMAT_BILINGUAL_TRADOS_RTF_TEXT);
				 break;
			 }
			 case "OmegaT":
			{
				selenium.select(MyAccount.MYACCOUNT_FORMAT_SELECT, MyAccount.MYACCOUNT_FORMAT_OMEGAT_TEXT);
				break;
			}
			 case "TTX":
				{
					selenium.select(MyAccount.MYACCOUNT_FORMAT_SELECT, MyAccount.MYACCOUNT_FORMAT_TRADOS_TTX_TEXT);
					break;
				}
			 default: break;
			}
			
			switch (tM_Options)
			{
			case "Annotations":
			{
				selenium.select(MyAccount.MYACCOUNT_TMX_TYPE_SELECT, MyAccount.MYACCOUNT_TMX_TYPE_ANNOTATIONS_TEXT);
				break;
			}
			case "TMX":
			{
				selenium.select(MyAccount.MYACCOUNT_TMX_TYPE_SELECT, MyAccount.MYACCOUNT_TMX_TYPE_TMX14B_TEXT);
				break;
			}
			case "Both":
			{
				selenium.select(MyAccount.MYACCOUNT_TMX_TYPE_SELECT, MyAccount.MYACCOUNT_TMX_TYPE_BOTH_TEXT);
				break;
			}
			case "None":
			{
				selenium.select(MyAccount.MYACCOUNT_TMX_TYPE_SELECT, MyAccount.MYACCOUNT_TMX_TYPE_NONE_TEXT);
				break;
			}
			default: break;
			}
			
			
			if ((!(mT_matches_into_separate_TM_file.isEmpty()))&&(!(mT_matches_into_separate_TM_file.equalsIgnoreCase("x"))) 
					&& (!(mT_matches_into_separate_TM_file.equalsIgnoreCase("o")))){
				if (mT_matches_into_separate_TM_file.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_MT_MATCHES_INTO_SEPERATE_TM_FILE_CHECKBOX);
				else if (mT_matches_into_separate_TM_file.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_MT_MATCHES_INTO_SEPERATE_TM_FILE_CHECKBOX);
			}
			if ((!(penalized_Reference_TM_Options.isEmpty()))&&(!(penalized_Reference_TM_Options.equalsIgnoreCase("x"))) 
					&& (!(penalized_Reference_TM_Options.equalsIgnoreCase("o")))){
				if (penalized_Reference_TM_Options.equalsIgnoreCase("1"))
					selenium.click(MyAccount.MYACCOUNT_SEPARATE_TMX_WITH_PRE_PENALIZED_SOURCE);
				else if (penalized_Reference_TM_Options.equalsIgnoreCase("2"))
					selenium.click(MyAccount.MYACCOUNT_SEPARATE_TMXS_PER_REFERENCE_TM_PENALTY);
				
			}
			
			switch (terminology)
			{
			case "HTML":
			{
				selenium.select(MyAccount.MYACCOUNT_TERMINOLOGY_SELECT, MyAccount.MYACCOUNT_TERMINOLOGY_HTML_LABEL);
				break;
			}
			case "TBX":
			{
				selenium.select(MyAccount.MYACCOUNT_TERMINOLOGY_SELECT, MyAccount.MYACCOUNT_TERMINOLOGY_TBX_LABEL);
				break;
			}
			case "TRADOS":
			{
				selenium.select(MyAccount.MYACCOUNT_TERMINOLOGY_SELECT, MyAccount.MYACCOUNT_TERMINOLOGY_TRADOS_LABEL);
				break;
			}
			
			case "TEXT":
			{
				selenium.select(MyAccount.MYACCOUNT_TERMINOLOGY_SELECT, MyAccount.MYACCOUNT_TERMINOLOGY_TEXT_LABEL);
				break;
			}
			case "None":
			{
				selenium.select(MyAccount.MYACCOUNT_TERMINOLOGY_SELECT, MyAccount.MYACCOUNT_TERMINOLOGY_NONE_LABEL);
				break;
			}
			default: break;
			}
			
			switch (allow_Edit_Locked_Segments)
			{
			case "ICE and 100":
			{
				selenium.select(MyAccount.MYACCOUNT_ALLOW_EDIT_LOCKED_SEGMENTS_SELECT, MyAccount.MYACCOUNT_ALLOW_EDIT_OF_ICE_AND_100_MATCHES_TEXT);
				break;
			}
			case "ICE":
			{
				selenium.select(MyAccount.MYACCOUNT_ALLOW_EDIT_LOCKED_SEGMENTS_SELECT, MyAccount.MYACCOUNT_ALLOW_EDIT_OF_ICE_MATCHES_TEXT);
				break;
			}
			case "100":
			{
				selenium.select(MyAccount.MYACCOUNT_ALLOW_EDIT_LOCKED_SEGMENTS_SELECT, MyAccount.MYACCOUNT_ALLOW_EDIT_OF_100_MATCHES_TEXT);
				break;
			}
			case "Deny":
			{
				selenium.select(MyAccount.MYACCOUNT_ALLOW_EDIT_LOCKED_SEGMENTS_SELECT, MyAccount.MYACCOUNT_DENY_EDIT_TEXT);
				break;
			}
			default: break;
			}
			
			if ((!(populate_100_Target_Segments.isEmpty()))&&(!(populate_100_Target_Segments.equalsIgnoreCase("x"))) 
					&& (!(populate_100_Target_Segments.equalsIgnoreCase("o")))){
				if (populate_100_Target_Segments.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_POPULATE_100_TARGET_SEGMENTS_CHECKBOX);
				else if (populate_100_Target_Segments.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_POPULATE_100_TARGET_SEGMENTS_CHECKBOX);
			}
			
			if ((!(populate_Fuzzy_Target_Segments.isEmpty()))&&(!(populate_Fuzzy_Target_Segments.equalsIgnoreCase("x"))) 
					&& (!(populate_Fuzzy_Target_Segments.equalsIgnoreCase("o")))){
				if (populate_Fuzzy_Target_Segments.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_POPULATE_FUZZY_TARGET_SEGMENTS_CHECKBOX);
				else if (populate_Fuzzy_Target_Segments.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_POPULATE_FUZZY_TARGET_SEGMENTS_CHECKBOX);
			}
			
			if ((!(preserve_Source_Folder_Structure.isEmpty()))&&(!(preserve_Source_Folder_Structure.equalsIgnoreCase("x"))) 
					&& (!(preserve_Source_Folder_Structure.equalsIgnoreCase("o")))){
				if (preserve_Source_Folder_Structure.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_PRESERVE_SOURCE_FOLDER_STRUCTURE_CHECKBOX);
				else if (preserve_Source_Folder_Structure.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_PRESERVE_SOURCE_FOLDER_STRUCTURE_CHECKBOX);
			}
			
			switch (consolidate_Split_Type)
			{
			case "1":
			{
				selenium.select(MyAccount.MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_SELECT, MyAccount.MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_CONSOLIDATE_ALL_FILES_LABEL);
				break;
			}
			case "2":
			{
				selenium.select(MyAccount.MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_SELECT, MyAccount.MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_FILE_BY_FILE_LABEL);
				break;
			}
			case "3":
			{
				selenium.select(MyAccount.MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_SELECT, MyAccount.MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_SPLIT_FILE_PER_WORD_COUNT_LABEL);
				break;
			}
			
			default: break;
			}
			
			
			if ((!(include_Repeated_Segments_as_Separate_File.isEmpty()))&&(!(include_Repeated_Segments_as_Separate_File.equalsIgnoreCase("x"))) 
					&& (!(include_Repeated_Segments_as_Separate_File.equalsIgnoreCase("o")))){
				if (include_Repeated_Segments_as_Separate_File.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_INCLUDE_REPEATED_SEGMENTS_AS_SEPARATE_FILE_CHECKBOX);
				else if (include_Repeated_Segments_as_Separate_File.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_INCLUDE_REPEATED_SEGMENTS_AS_SEPARATE_FILE_CHECKBOX);
			}
			
			if ((!(nOT_include_fully_leveraged_file.isEmpty()))&&(!(nOT_include_fully_leveraged_file.equalsIgnoreCase("x"))) 
					&& (!(nOT_include_fully_leveraged_file.equalsIgnoreCase("o")))){
				if (nOT_include_fully_leveraged_file.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_NOT_INCLUDE_FULLY_LEVERAGED_FILE_CHECKBOX);
				else if (nOT_include_fully_leveraged_file.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_NOT_INCLUDE_FULLY_LEVERAGED_FILE_CHECKBOX);
			}
			
			if ((!(include_XML_Node_Context_Information.isEmpty()))&&(!(include_XML_Node_Context_Information.equalsIgnoreCase("x"))) 
					&& (!(include_XML_Node_Context_Information.equalsIgnoreCase("o")))){
				if (include_XML_Node_Context_Information.equalsIgnoreCase("true"))
					selenium.check(MyAccount.MYACCOUNT_INCLUDE_XML_NODE_CONTEXT_INFORMATION_CHECKBOX);
				else if (include_XML_Node_Context_Information.equalsIgnoreCase("false"))
					selenium.uncheck(MyAccount.MYACCOUNT_INCLUDE_XML_NODE_CONTEXT_INFORMATION_CHECKBOX);
			}
			
			iSelniumUtils.clickAndWait(selenium, MyAccount.MYACCOUNT_DOWNLOAD_OPTION_DONE_BUTTON);
			iSelniumUtils.clickAndWait(selenium, MyAccount.Save_BUTTON);
			iSelniumUtils.clickAndWait(selenium, "link=Logout");
			
			CommonFuncs.loginSystemWithAnyone(selenium);
			openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
					MainFrame.MY_ACTIVITIES_INPROGRESS_SUBMENU);
			selenium.type(MyActivities.SEARCH_ACTIVITY_JOB_NAME_TEXT,"Offline_");
        	selenium.keyDown(MyActivities.SEARCH_ACTIVITY_JOB_NAME_TEXT, "\\13");
        	selenium.keyUp(MyActivities.SEARCH_ACTIVITY_JOB_NAME_TEXT, "\\13");
//        	selenium.waitForFrameToLoad("css=table.listborder", "1000");

        	selenium.click(MyActivities.MYACTIVITIES_ALL_CHECKBOX);
//        	selenium.click(MyActivities.MYACTIVITIES_DOWNLOAD_COMBINED_BUTTON);
        	selenium.click(MyActivities.MYACTIVITIES_OFFLINE_DOWNLOAD_BUTTON);
        	selenium.waitForPageToLoad("30000");
        	
        	
//        	selenium.wait(Long.parseLong(CommonFuncs.LONG_WAIT));
        	Thread.sleep(3000);  


        	CommonFuncs.logoutSystem(selenium);
     		
		}
			
}
    @BeforeTest
    private void beforeTest() {
//    	CommonFuncs.loginSystemWithAdmin(selenium);
//    	CommonFuncs.loginSystemWithAnyone(selenium);
    }
    
    @AfterTest
    private void afterTest() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
    }
	}
