package com.globalsight.selenium.testcases.testmatrix.others;

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
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.XMLRules;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class Longevity extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private CreateJobsFuncs tmp = new CreateJobsFuncs();
	private ExportWorkflowFuncs export = new ExportWorkflowFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void longevity() throws Exception {
			
		 
		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"longevity.TCPath");
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
		String UserName;
		String Password;
		String Target_Locale_Short;
		String Source_File_Path;
		
		String filterJob = getStringToday();
    	
		int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];
		jobNames[0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i = 1; i < testCases.size(); i++) {
			File_Profile_name = testCases.get(i)[1];
			UserName = testCases.get(i)[2];
			Password = testCases.get(i)[3];
			Target_Locale_Short = testCases.get(i)[4];
			Source_File_Path = testCases.get(i)[5];
						
		
			jobNames[i] = i + "_" +  filterJob;
        	
        	
        	if ((!(Source_File_Path.isEmpty())) && (!(Source_File_Path.equalsIgnoreCase("x")))){
        		
        		ArrayList<String> sourceFiles = new ArrayList<String>();
        		String filePath_sourceFile = ConfigUtil.getConfigData("Base_Path") + Source_File_Path;
        		File file2 = new File(filePath_sourceFile);
        		AssertJUnit.assertTrue(file2.exists());
        		BufferedReader br2 = new BufferedReader(new FileReader(file2));
        		String line2;
        		while ((line2 = br2.readLine()) != null) {
        			String sourceFile = line2;
        			sourceFiles.add(sourceFile);
        		}
        		br2.close();
        		
        		String Source_File_of_sourceFiles_file = "";
        		String Source_Files = "";
        		String File_Profile_names = "";
        		
        		for (int j = 0; j < sourceFiles.size(); j++){
        			Source_File_of_sourceFiles_file = sourceFiles.get(j);
        			if (j==(sourceFiles.size() - 1)){
        				File_Profile_names = File_Profile_names + File_Profile_name;
        				Source_Files = Source_Files + Source_File_of_sourceFiles_file;
            		} else {
        				File_Profile_names = File_Profile_names + File_Profile_name + ",";
        				Source_Files = Source_Files + Source_File_of_sourceFiles_file + ",";
            		}
                }
	        	
	        
//				tmp.createJob(jobNames[i], Source_File, 
//							File_Profile_names, getProperty(testMatrixFile,"XMLtargetLocales"));
        		tmp.createJob(UserName, Password, jobNames[i], Source_Files, 
						File_Profile_names, "" );
						
						
					}
					Thread.sleep(1000);	
					
					
        	}
				
		for (int i =1; i < testCases.size(); i++) {
						export.exportWorkflow(jobNames[i], getProperty(testMatrixFile,"XMLtargetLocales"));
						
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
