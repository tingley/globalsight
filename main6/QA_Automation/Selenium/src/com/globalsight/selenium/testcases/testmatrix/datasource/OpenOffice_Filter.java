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

public class OpenOffice_Filter extends BaseTestCase {
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
	public void testWord_2007_Filter() throws Exception {
		// Initiate filters
        iSelniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        
      

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"OpenOffice_FilterTCPath");
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
		String Translate_Header_Information;
		String Unextractable_Word_Character_Styles;
		String Unextractable_Word_Paragraph_Styles;
		String Source_File;
		String Empty_File_Profile_Name = "";
		
		
		String filterJob = "Open Office" + getStringToday();

    	
		int ljobNames = testCases.size();
		String[][] jobNames = new String[ljobNames][];
		jobNames[0] = new String[1];
		jobNames[0][0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i = 1; i < testCases.size(); i++) {
			
			File_Profile_name = testCases.get(i)[1];
			Filter_Name = testCases.get(i)[2];
			Translate_Header_Information = testCases.get(i)[3];
			Unextractable_Word_Paragraph_Styles = testCases.get(i)[4];
			Unextractable_Word_Character_Styles = testCases.get(i)[5];
			Source_File = testCases.get(i)[6];


			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			iFilterConfig.openOfficeFilterOperation(selenium, Filter_Name, Translate_Header_Information,
					Unextractable_Word_Paragraph_Styles, Unextractable_Word_Character_Styles);
		            		
			
			// create File Profile
//	        SeleniumUtils.
	        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);

	        ArrayList<String> array = new ArrayList<String>();
	        array.add(File_Profile_name);
	        array.add("Description:Open Office filter test");
	        array.add(getProperty(testMatrixFile,"OpenOffice_Fitler.localizationProfiles"));
	        array.add("OpenOffice document");
	        array.add(Filter_Name);
	        array.add("x");
	        array.add("odp;ods;odt");
	        

	        fileProfileFuncs.setup(array);
	        fileProfileFuncs.create(selenium);
	        
	        if (i==1) Empty_File_Profile_Name = File_Profile_name;
	        jobNames[i] = new String[2];
	        jobNames[i][0] = i + "_" + Filter_Name + "_" + filterJob;
        	jobNames[i][1] = i + "_" + Filter_Name + "_Empty_Filter"+ "_" + filterJob;

        	if ((!(Source_File.isEmpty())) && (!(Source_File.equalsIgnoreCase("x")))){
        		
	        	tmp.createJob(jobNames[i][0], Source_File, 
									File_Profile_name, getProperty(testMatrixFile,"OpenOffice_targetLocales"));
				tmp.createJob(jobNames[i][1], Source_File, 
										 Empty_File_Profile_Name, getProperty(testMatrixFile,"OpenOffice_targetLocales"));
				}
					
        	}
				
		for (int i =1; i < testCases.size(); i++) {
			for (int j=0; j<jobNames[i].length; j++){
				export.exportWorkflow(jobNames[i][j], getProperty(testMatrixFile,"OpenOffice_targetLocales"));
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
