package com.globalsight.selenium.testcases.testmatrix.datasource;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;

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
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class IDML_Filter extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
	private CreateJobsFuncs tmp = new CreateJobsFuncs();
	private ExportWorkflowFuncs export = new ExportWorkflowFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testFrameMaker_Filter() throws Exception {
		// Initiate filters
        iSelniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        
      

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"IDML_FilterTCPath");
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
		String Translate_Hidden_Layers;
		String Translate_Master_Layers;
		String Translate_File_Information;
		String Translate_Hyperlinks;
		String Translate_Hidden_Conditional_Text;
		String Ignore_Tracking_and_Kerning;
		String Ignore_Forced_Line_Breaks;
		String Ignore_Nonbreaking_Space;
		String Source_File;
		String Empty_File_Profile_Name = "";
		String No_Filter_Profile_Name = "";
		
		
		String filterJob = "IDML" + getStringToday();

    	CreateJobsFuncs tmp = new CreateJobsFuncs();
		int ljobNames = testCases.size();
		String[][] jobNames = new String[ljobNames][];
		jobNames[0] = new String[1];
		jobNames[0][0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i = 1; i < testCases.size(); i++) {
			
			File_Profile_name = testCases.get(i)[1];
			Filter_Name = testCases.get(i)[2];
			Translate_Hidden_Layers = testCases.get(i)[3];
			Translate_Master_Layers = testCases.get(i)[4];
			Translate_File_Information = testCases.get(i)[5];
			Translate_Hyperlinks = testCases.get(i)[6];
			Translate_Hidden_Conditional_Text = testCases.get(i)[7];
			Ignore_Tracking_and_Kerning = testCases.get(i)[8];
			Ignore_Forced_Line_Breaks = testCases.get(i)[9];
			Ignore_Nonbreaking_Space = testCases.get(i)[10];
			Source_File = testCases.get(i)[11];
			
			
			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			iFilterConfig.IDMLFilterOperation(selenium, Filter_Name, 
					Translate_Hidden_Layers, Translate_Master_Layers, Translate_File_Information,
					Translate_Hyperlinks, Translate_Hidden_Conditional_Text, Ignore_Tracking_and_Kerning, 
					Ignore_Forced_Line_Breaks, Ignore_Nonbreaking_Space);
		            		
			
			// create File Profile
//	        SeleniumUtils.
	        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);
	        if (!(File_Profile_name.contains(","))){
		        ArrayList<String> array = new ArrayList<String>();
		        array.add(File_Profile_name);
		        array.add("Description: IDML 9 filter test");
		        array.add(getProperty(testMatrixFile,"IDML_Fitler.localizationProfiles"));
		        array.add("InDesign Markup (IDML)");
		        array.add(Filter_Name);
		        array.add("x");
		        array.add("idml");
	        
		        fileProfileFuncs.setup(array);
		        fileProfileFuncs.create(selenium);
	        }
	        
	        
	        if (File_Profile_name.contains("Empty")) Empty_File_Profile_Name = File_Profile_name;
	        if (File_Profile_name.contains("NoFilter")) No_Filter_Profile_Name = File_Profile_name;
	        if (No_Filter_Profile_Name.isEmpty()){
	        	jobNames[i] = new String[2];
	        	jobNames[i][0] = i + "_" + File_Profile_name + "_" + filterJob;
	        	jobNames[i][1] = i + "_" + File_Profile_name + "_Empty_Filter"+ "_" + filterJob;
	        	}
	        else {
	        	jobNames[i] = new String[3];
		        jobNames[i][0] = i + "_" + File_Profile_name + "_" + filterJob;
	        	jobNames[i][1] = i + "_" + File_Profile_name + "_Empty_Filter"+ "_" + filterJob;
	        	jobNames[i][2] = i + "_" + File_Profile_name + "_No_Filter"+ "_" + filterJob;
	        }
        	if ((!(Source_File.isEmpty())) && (!(Source_File.equalsIgnoreCase("x")))){
        		for (int k=0; k < jobNames[i].length; k++){
        			if (k==0) tmp.createJob(jobNames[i][0], Source_File, 
									File_Profile_name, getProperty(testMatrixFile,"IDML_targetLocales"));
        			if (k==1) tmp.createJob(jobNames[i][1], Source_File, 
										 Empty_File_Profile_Name, getProperty(testMatrixFile,"IDML_targetLocales"));
        			if (k==2) tmp.createJob(jobNames[i][2], Source_File, 
						No_Filter_Profile_Name, getProperty(testMatrixFile,"IDML_targetLocales"));
        		}
        	}
					
        	}
				
			
		for (int i =1; i < testCases.size(); i++) {
			for (int j=0; j<jobNames[i].length; j++){
				export.exportWorkflow(jobNames[i][j], getProperty(testMatrixFile,"IDML_targetLocales"));
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
