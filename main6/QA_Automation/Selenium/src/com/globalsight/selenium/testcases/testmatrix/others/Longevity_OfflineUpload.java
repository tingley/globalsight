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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.OfflineDownloadUploadFuncs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;
import com.thoughtworks.selenium.Selenium;

public class Longevity_OfflineUpload extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private OfflineDownloadUploadFuncs tmp = new OfflineDownloadUploadFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void longevity_OfflineUpload() throws Exception {
			
		 
		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"longevity_OfflineUpload.TCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		
		
		String JobID;
		String TaskID;
		String UserName;
		String Password;
		String workOfflineFileType;
		String Upload_Files;
		
		int lofflineUploadFiles = testCases.size();
		String[] offlineUploadFiles = new String[lofflineUploadFiles];
		offlineUploadFiles[0] = "Case title";

		for (int i = 1; i < testCases.size(); i++) {
			JobID = testCases.get(i)[1];
			TaskID = testCases.get(i)[2];
			UserName = testCases.get(i)[3];
			Password = testCases.get(i)[4];
			workOfflineFileType = testCases.get(i)[5];
			Upload_Files = testCases.get(i)[6];
						
		
			if ((!(Upload_Files.isEmpty())) && (!(Upload_Files.equalsIgnoreCase("x")))){
        		
        		
        		String[] offlinefineUploadFiles = Upload_Files.split(",");
                
                	for (int j = 0; j < offlinefineUploadFiles.length; j++) {
       
        		tmp.OfflineUploadImport(UserName, Password, Long.parseLong(TaskID), Integer.parseInt(workOfflineFileType), 
        				ConfigUtil.getConfigData("Base_Path") + offlinefineUploadFiles[j]);
						
						
					}
					Thread.sleep(1000);	
					
					
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
