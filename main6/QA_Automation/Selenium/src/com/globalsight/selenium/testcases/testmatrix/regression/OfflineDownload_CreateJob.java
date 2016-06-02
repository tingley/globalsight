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
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class OfflineDownload_CreateJob extends BaseTestCase {
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
	public void testOfflineDownload_CreateJob() throws Exception {

		// Create TM
//        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.TRANSLATION_MEMORY_SUBMENU);
//        iTMFuncs.newTM(selenium, getProperty(testMatrixFile,"OfflineDownload_ReferenceTMName"), "", "no");
//        iTMFuncs.newTM(selenium, getProperty(testMatrixFile,"OfflineDownload_StorageTMName"), "", "no");
//        iTMFuncs.importTM(selenium, getProperty(testMatrixFile,"OfflineDownload_ReferenceTMName"), ConfigUtil.getConfigData("Base_Path")
//				+  getProperty(testMatrixFile,"OfflineDownload_ReferenceTMFile"));
//        // Create TMProfile
//        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);
//        
//        iTMProfileFuncs
//                .newTMProfile(selenium, getProperty(testMatrixFile,"OfflineDownload.tmProfile"));
        
        
		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"OfflineDownload_CreateJobTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		String job_Name;
		String file_Profile_name;	
		String mT_Profile;
		String l10n_Profile;
		String offline_SourceFile;

		String filterJob = getStringToday();
//		String filterJob = "201511-04-115231";

    	
		int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];
		
//		jobNames[0][0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		

		
		//Create Filter
		for (int i = 1; i < testCases.size(); i++) {
//		for (int i = 1; i < 13; i++) {
			job_Name = testCases.get(i)[1];
			file_Profile_name = testCases.get(i)[2];
			mT_Profile = testCases.get(i)[3];
			l10n_Profile = testCases.get(i)[4];
			offline_SourceFile = testCases.get(i)[5];
						
//			// create MT Profile
//			if (mT_Profile.equalsIgnoreCase("Fuzzy_MT"))
//				iMTProfileFuncs.newMTProfile_MSMT(selenium, mT_Profile, mT_Profile, "90", "", "true", "false", "", "", 
//					getProperty(testMatrixFile,"OfflineDownload_mS_Translator_URL"), getProperty(testMatrixFile,"OfflineDownload_client_ID"), 
//					getProperty(testMatrixFile,"OfflineDownload_client_Secret"), "", 
//					"", "", "", "");
//			else if (mT_Profile.equalsIgnoreCase("100_MT"))
//				iMTProfileFuncs.newMTProfile_MSMT(selenium, mT_Profile, mT_Profile, "100", "", "true", "false", "", "", 
//						getProperty(testMatrixFile,"OfflineDownload_mS_Translator_URL"), getProperty(testMatrixFile,"OfflineDownload_client_ID"), 
//						getProperty(testMatrixFile,"OfflineDownload_client_Secret"), "", 
//						"", "", "", "");
//				else if (mT_Profile.equalsIgnoreCase("Ignore_TM_MT"))
//					iMTProfileFuncs.newMTProfile_MSMT(selenium, mT_Profile, mT_Profile, "100", "true", "true", "false", "", "", 
//							getProperty(testMatrixFile,"OfflineDownload_mS_Translator_URL"), getProperty(testMatrixFile,"OfflineDownload_client_ID"), 
//							getProperty(testMatrixFile,"OfflineDownload_client_Secret"), "", 
//							"", "", "", "");
//					
//			// create Localization Profile
//	        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//	                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
//	        iLocalizationFuncs.create2(selenium,
//	        		getDataInCase("DataSource.localizationProfile"));
//	        
//	      
//	        
//			iLocalizationFuncs.create(selenium, l10n_Profile, getProperty(testMatrixFile,"OfflineDownload_TMProfileName"), 
//					"Template","3",getProperty(testMatrixFile,"LocalPairs.en"),"",true,
//	        		getProperty(testMatrixFile,"OfflineDownload_targetLocalCode"),
//	        		getProperty(testMatrixFile,"OfflineDownload_targetLocalName"),
//	        		mT_Profile);
//			
//			
//			
//			
//			// create File Profile
////	        SeleniumUtils.
//			
//			openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
//	                MainFrame.FILE_PROFILES_SUBMENU);
//
//	        ArrayList<String> array = new ArrayList<String>();
//	        array.add(file_Profile_name);
//	        array.add("Description: offline download filter test");
//	        array.add(l10n_Profile);
//	        array.add("HTML");
//	        array.add("x");
//	        array.add("x");
//	        array.add("html");
//	        
//
//	        fileProfileFuncs.setup(array);
//	        fileProfileFuncs.create(selenium);
//	        
	       
        	
	        
	        jobNames[i] = job_Name + "_" + filterJob + "_" + i;
        	if ((!(offline_SourceFile.isEmpty())) && (!(offline_SourceFile.equalsIgnoreCase("x")))){
        		
        		String[] Source_Files = offline_SourceFile.split(",");
        		String File_Profile_names = "";
        		for(int k=0;k<Source_Files.length;k++)
                {
        			if (k==(Source_Files.length - 1)){
        				File_Profile_names = File_Profile_names + file_Profile_name;
            			
        			} else {
        				File_Profile_names = File_Profile_names + file_Profile_name + ",";
            			
        			}
                }
	        	
	        		
					
							tmp.createJob(jobNames[i], offline_SourceFile, 
									File_Profile_names, getProperty(testMatrixFile,"XMLtargetLocales"));
					
					Thread.sleep(1000);	
				
					
        	}
				
			
        	
			}

		
		
//		for (int i =1; i < testCases.size(); i++) {
		for (int i =1; i < 13; i++) {
						
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
