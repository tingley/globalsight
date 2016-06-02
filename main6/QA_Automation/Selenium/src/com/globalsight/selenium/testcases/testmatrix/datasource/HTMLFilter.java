package com.globalsight.selenium.testcases.testmatrix.datasource;

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
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class HTMLFilter extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private LocalePairsFuncs localepairFuncs = new LocalePairsFuncs();
	private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
	private LocalizationFuncs iLocalizationFuncs = new LocalizationFuncs();
	private UsersFuncs usersFuncs = new UsersFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
	private CreateJobsFuncs tmp = new CreateJobsFuncs();
	private ExportWorkflowFuncs export = new ExportWorkflowFuncs();
	private TMFuncs iTMFuncs = new TMFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testHTMLFilter() throws Exception {

        
      

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"HTMLFilterTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		String File_Profile_name;;
		String Filter_Name;
		String Option;
		String Base_Text_Filter_Internal_Text;
		String Base_Text_Filter_Escaping;
		String Convert_HTML_Entity_For_Export;
		String Ignore_Invalid_HTML_Tags;
		String Add_rtl_directionality;
		String Whitespace_Handling;
		String Localize_Function;
		String Base_Text_post_filter;
		String Embeddable_Tags;	
		String Internal_Tag;
		String Paired_Tags;
		String Switch_Tag_Map;
		String Translatable_Attribute;
		String Unpaired_Tags;
		String White_Preserving_Tags;
		String Source_File;

		
		String filterJob = getStringToday();
//		String filterJob = "HTMLJob201507-29-162119";

    	
		int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];
		jobNames[0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		
		iSelniumUtils.openMenuItemAndWait(selenium,
    			MainFrame.SETUP_MENU,
    			MainFrame.TRANSLATION_MEMORY_SUBMENU);
		
		iTMFuncs.importTM(selenium, getProperty(testMatrixFile,"HTMLReferenceTMName"), 
				ConfigUtil.getConfigData("Base_Path") +	getProperty(testMatrixFile,"HTMLRTLTMFile"));
		
				
		// Initiate filters
        iSelniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

		
		//Create Filter
		for (int i = 2; i < testCases.size(); i++) {
			File_Profile_name = testCases.get(i)[1];
			Filter_Name = testCases.get(i)[2];
			Option = testCases.get(i)[3];
			Base_Text_Filter_Internal_Text = testCases.get(i)[4];
			Base_Text_Filter_Escaping = testCases.get(i)[5];
			Convert_HTML_Entity_For_Export = testCases.get(i)[6];
			Ignore_Invalid_HTML_Tags = testCases.get(i)[7];
			Add_rtl_directionality =  testCases.get(i)[8];
			Whitespace_Handling = testCases.get(i)[9];
			Localize_Function = testCases.get(i)[10];
			Base_Text_post_filter = testCases.get(i)[11];
			Embeddable_Tags = testCases.get(i)[12];
			Internal_Tag = testCases.get(i)[13];
			Paired_Tags = testCases.get(i)[14];
			Switch_Tag_Map = testCases.get(i)[15];
			Translatable_Attribute = testCases.get(i)[16];
			Unpaired_Tags = testCases.get(i)[17];
			White_Preserving_Tags = testCases.get(i)[18];
			Source_File = testCases.get(i)[19];
			

			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			if (Option.equalsIgnoreCase("RTL_YES") || Option.equalsIgnoreCase("RTL_NO"))
			{
				iFilterConfig.HTMLfilterOperation(selenium, Filter_Name, Option, White_Preserving_Tags);
			}
			else if (Option.equalsIgnoreCase("All_options"))
			{
				iFilterConfig.HTMLfilterOperation(selenium, Filter_Name, Base_Text_Filter_Internal_Text, Base_Text_Filter_Escaping, Convert_HTML_Entity_For_Export, 
						Ignore_Invalid_HTML_Tags, Add_rtl_directionality,
						Whitespace_Handling, Localize_Function, Base_Text_post_filter, Embeddable_Tags, Internal_Tag,
						Paired_Tags, Switch_Tag_Map, Translatable_Attribute, Unpaired_Tags, White_Preserving_Tags);
			} else if(!(Option.isEmpty()))
			{
				iFilterConfig.HTMLfilterOperation(selenium, Filter_Name, Option, White_Preserving_Tags);
			}
			
			
			// create File Profile
//	        SeleniumUtils.
	        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);

	        ArrayList<String> array = new ArrayList<String>();
	        array.add(File_Profile_name);
	        array.add("Description: html filter test");
	        array.add(getProperty(testMatrixFile,"HTMLFitler.localizationProfiles"));
	        array.add("HTML");
	        array.add(Filter_Name);
	        array.add("x");
	        array.add("htm;html");
	        

	        fileProfileFuncs.setup(array);
	        fileProfileFuncs.create(selenium);
	        
            jobNames[i] = i + "_" + Filter_Name + "_" + filterJob;
//            jobNames[i] = i + "_" + Filter_Name + "_" + "201604-20-093739";

            if (Option.equalsIgnoreCase("RTL_YES") || Option.equalsIgnoreCase("RTL_NO"))
			{
            	tmp.createJob(jobNames[i], Source_File, 
    					File_Profile_name, getProperty(testMatrixFile,"HTMLRTL.targetLocalesForAR"));
			}
			else 
			{
				tmp.createJob(jobNames[i], Source_File, 
					File_Profile_name, getProperty(testMatrixFile,"HTMLtargetLocales"));
//			tmp.createJob(filterJob + i, getProperty(testMatrixFile,"HTMLSourceFile"),
//					File_Profile_name, getProperty(testMatrixFile,"HTMLtargetLocales"));
			}
			Thread.sleep(1000);
			

		}
	    
		for (int i =1; i < testCases.size(); i++) {
			
			
			
			if (jobNames[i].contains("_rtl_"))
			{
				export.exportWorkflow(jobNames[i], getProperty(testMatrixFile,"HTMLRTL.targetLocalesForAR"));
			}
			else
			{
				export.exportWorkflow(jobNames[i], getProperty(testMatrixFile,"HTMLtargetLocales"));
			}
		}
	}
    @BeforeTest
    private void beforeTest() {
    	CommonFuncs.loginSystemWithAdmin(selenium);
    }
    
    @AfterTest
    private void afterTest() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
    }
}
