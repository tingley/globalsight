package com.globalsight.selenium.testcases.testmatrix;

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
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.thoughtworks.selenium.Selenium;

public class WordFilterTest extends BaseTestCase {
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;

	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testWordFilter() throws Exception {
		// Initiate filters
        SeleniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        String internaltagname = getProperty(testMatrixFile,"AddTags");
        String interName = getProperty(testMatrixFile,"InternalFilterName");
        String iFilterName = getProperty(testMatrixFile,"HTMLFilterName");
        String cpf = getProperty(testMatrixFile,"InternalTextPostFilter");
        String str = getProperty(testMatrixFile,"AddingTag");
        String wFilterName = getProperty(testMatrixFile,"wordFilterName");
        String fpname = getProperty(testMatrixFile,"fileProfileNamesStr");
        String wordCountResultPath_jobDetails = ConfigUtil
                .getConfigData("Base_Path_Result")
                + getProperty(testMatrixFile,"WordCountPath");
        String[] fp = fpname.split(",");

        iFilterConfig.InternalText(selenium, internaltagname, interName);
        iFilterConfig.htmlFilter(selenium, iFilterName, cpf, str);
        iFilterConfig.wordFilter(selenium, wFilterName);

		// Add filter to the corresponding file profile
		selenium.click(MainFrame.DATA_SOURCES_MENU);
		selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		BasicFuncs.selectRadioButtonFromTable(selenium, true, FileProfile.SEARCH_CONTENT_TEXT, fp[0]);
		selenium.click("link=" + fp[0]);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.select(FileProfile.FILTER_SELECT, "label=" + wFilterName);
		selenium.click(FileProfile.SAVE_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"FilterTestCasePath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();

		String headerInfo;
		String toolTips;
		String tableofContent;
		String contentPostFilter;
		String internalTextPostFilter;
		String base_Text_Filter_Internal_Text;
		String base_Text_Filter_Escaping;
		String unextractableWordParagraphStyles;
		String unextractableWordCharacterStyles;
		String selectedInternalTextStyles;
		String filterJob = "wordJob" + getStringToday();

		CreateJobsFuncs tmp = new CreateJobsFuncs();
		int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];

		SimpleDateFormat format = new SimpleDateFormat();
		String time = format.format(new Date());
		String wordcount = "\r\n" + "\r\n" + time;

		jobNames[0] = "Case title";

		for (int i = 1; i < testCases.size(); i++) {
			headerInfo = testCases.get(i)[1];
			toolTips = testCases.get(i)[2];
			tableofContent = testCases.get(i)[3];
			contentPostFilter = testCases.get(i)[4];
			internalTextPostFilter = testCases.get(i)[5];
			base_Text_Filter_Internal_Text = testCases.get(i)[6];
			base_Text_Filter_Escaping =testCases.get(i)[7];
			unextractableWordParagraphStyles = testCases.get(i)[8];
			unextractableWordCharacterStyles = testCases.get(i)[9];
			selectedInternalTextStyles = testCases.get(i)[10];

			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			iFilterConfig.wordFilterOperation(selenium, wFilterName, headerInfo, toolTips, tableofContent,
					contentPostFilter, internalTextPostFilter, base_Text_Filter_Internal_Text, base_Text_Filter_Escaping,
		            unextractableWordParagraphStyles,
					unextractableWordCharacterStyles, selectedInternalTextStyles);
			

			tmp.createJob(filterJob + i, ConfigUtil.getDataInCase(getClassName(), "SourceFile"),
					ConfigUtil.getDataInCase(getClassName(), "fileProfileNamesStr"),
					ConfigUtil.getDataInCase(getClassName(), "targetLocales"));
			Thread.sleep(1000);
			jobNames[i] = filterJob + i;

		}
//		for (int i = 1; i < testCases.size(); i++) {
//			jobNames[i]="wordJob201407-02-155555"+i;
//		}
//
//		// for (int i=1; i<testCases.size(); i++)
//		// {
//		// jobNames[i]="wordJob201110-18-130859"+i;
//		// }

		selenium.click(MainFrame.MY_JOBS_MENU);
		selenium.click(MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		
		for (int i = 1; i < testCases.size(); i++) {

	
				

			BasicFuncs basic = new BasicFuncs();
			basic.selectRadioButtonFromTable(selenium, true, MyJobs.SEARCH_JOB_NAME_TEXT, jobNames[i]);
			if (selenium.isElementPresent("link=" + jobNames[i])) {
				String wordCountGot = basic.jobgetWordCount(selenium, true, MyJobs.MyJobs_AllStatus_TABLE, jobNames[i], 7);

				wordcount = wordcount + " \r\n" + jobNames[i] + ", " + wordCountGot;
			}
		}

		FileWriter writer = new FileWriter(wordCountResultPath_jobDetails, true);
		writer.write(wordcount);
		writer.close();

	}
}
