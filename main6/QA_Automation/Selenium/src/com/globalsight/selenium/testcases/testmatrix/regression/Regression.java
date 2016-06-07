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
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.SegmentationRuleFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class Regression extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private LocalePairsFuncs localepairFuncs = new LocalePairsFuncs();
	private UsersFuncs usersFuncs = new UsersFuncs();
	private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private TMProfileFuncs iTMProfileFuncs = new TMProfileFuncs();
	private TMFuncs iTMFuncs = new TMFuncs();
	private SegmentationRuleFuncs iSegmentationRuleFuncs = new SegmentationRuleFuncs();
	private LocalizationFuncs iLocalizationFuncs = new LocalizationFuncs();
	private XMLRulesFuncs iXMLRuleConfig = new XMLRulesFuncs();
	private JobActivityOperationFuncs iJobActivityOperationFuncs = new JobActivityOperationFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testTMProfiles() throws Exception {	// Initiate filters

        
      

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"TMPTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		String File_Profile_name;
		String Source_File_Format;
		String Regression_SourceFile;
				
		String filterJob = getStringToday();

    	CreateJobsFuncs tmp = new CreateJobsFuncs();
		int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];
		jobNames[0] = "Case title";
		String[][] Target_Locales_For_Wordcount = new String[ljobNames][];
		
		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i =1; i < testCases.size(); i++) {
			File_Profile_name = testCases.get(i)[1].trim();
			Source_File_Format = testCases.get(i)[2].trim();
			Regression_SourceFile = testCases.get(i)[3].trim();
			
			iSelniumUtils.openMenuItemAndWait(selenium,
	    			MainFrame.SETUP_MENU,
	    			MainFrame.TRANSLATION_MEMORY_SUBMENU);
			iTMFuncs.importTM(selenium, getProperty(testMatrixFile,"Regression.ReferenceTMName"), ConfigUtil.getConfigData("Base_Path")
    						+  getProperty(testMatrixFile,"Regression.Source_TM_To_Import_To_StorageTM"));
			
	    				
			// create File Profile
//	        SeleniumUtils.
	        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);

	        ArrayList<String> array = new ArrayList<String>();
	        array.add(File_Profile_name);
	        array.add("Description: TM Profile test");
	        array.add(getProperty(testMatrixFile,"Regression.localizationProfiles"));
	        String source_File_Format = Regression_SourceFile.substring(Regression_SourceFile.indexOf('.') + 1, Regression_SourceFile.length());
	        if (source_File_Format.equalsIgnoreCase("js"))
	        {
	        	array.add("Javascript");
		        array.add("x");
		        array.add("x");
		        array.add("js");
	        } else if (source_File_Format.equalsIgnoreCase("html") || source_File_Format.equalsIgnoreCase("htm")) 
	        {
	        	array.add("HTML");
		        array.add("x");
		        array.add("x");
		        array.add("htm;html");
	        } else if (source_File_Format.equalsIgnoreCase("xml"))
	        {
	        	array.add("XML");
		        array.add(getProperty(testMatrixFile,"TMP.XML_Filter_Name"));
		        array.add("x");
		        array.add("xml");
	        }
	        else if (source_File_Format.equalsIgnoreCase("docx") || source_File_Format.equalsIgnoreCase("pptx")
	        		|| source_File_Format.equalsIgnoreCase("xlsx"))
	        {
	        	array.add("Office 2010 Extractor v2.0");
		        array.add("x");
		        array.add("x");
		        array.add("docx;pptx;xlsx");
	        }
	        

	        fileProfileFuncs.setup(array);
	        fileProfileFuncs.create(selenium);
	        
	        jobNames[i] = i + File_Profile_name + "_" + filterJob;

			tmp.createJob(jobNames[i], Regression_SourceFile, 
					File_Profile_name, getProperty(testMatrixFile,"Regression.TargetLocales"));
			Thread.sleep(1000);
			
			String[] iTarget_Locales = Target_Locale_Long.split(";");
			Target_Locales_For_Wordcount[i-1] = new String[iTarget_Locales.length];
			for (int j = 0; j < iTarget_Locales.length; j++)
			{
				
				Target_Locales_For_Wordcount[i-1][j] = iTarget_Locales[j].trim();
			}
			

		}
		
		
		//input wordcount result to txt file  
		for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, "jobDetails",
            		ConfigUtil.getConfigData("Base_Path_Result")
    				+ getProperty(testMatrixFile,"TMPWordCountPath_jobDetails"), Target_Locales_For_Wordcount[i-1], jobNames[i]);
        }
		

        selenium.click(MainFrame.LOG_OUT_LINK);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        CommonFuncs.loginSystemWithAnyone(selenium);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, "activityList",
            		ConfigUtil.getConfigData("Base_Path_Result")
    				+ getProperty(testMatrixFile,"TMPWordCountPath_activityList"), Target_Locales_For_Wordcount[i-1], jobNames[i]);
        }

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, "activityDetails",
            		ConfigUtil.getConfigData("Base_Path_Result")
    				+ getProperty(testMatrixFile,"TMPWordCountPath_activityDetails"), Target_Locales_For_Wordcount[i-1],
                            jobNames[i]);
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
