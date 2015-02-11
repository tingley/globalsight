package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

public class WordFilterTest extends BaseTestCase {
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();


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

        String internaltagname = getDataInCase("AddTags");
        String interName = getDataInCase("InternalFilterName");
        String iFilterName = getDataInCase("HTMLFilterName");
        String cpf = getDataInCase("InternalTextPostFilter");
        String str = getDataInCase("AddingTag");
        String wFilterName = getDataInCase("wordFilterName");
        String fpname = getDataInCase("fileProfileNamesStr");
        String wordCountResultPath_jobDetails = ConfigUtil
                .getConfigData("Base_Path_Result")
                + getDataInCase("WordCountPath");
        String[] fp = fpname.split(",");

        iFilterConfig.InternalText(selenium, internaltagname, interName);
        iFilterConfig.htmlFilter(selenium, iFilterName, cpf, str);
        iFilterConfig.wordFilter(selenium, wFilterName);

		// Add filter to the corresponding file profile
		selenium.click(MainFrame.DATA_SOURCES_MENU);
		selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		SeleniumUtils.selectRadioButtonFromTable(selenium, FileProfile.MAIN_TABLE, fp[0]);
		selenium.click("link=" + fp[0]);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.select(FileProfile.FILTER_SELECT, "label=" + wFilterName);
		selenium.click(FileProfile.SAVE_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ ConfigUtil.getDataInCase(getClassName(), "FilterTestCasePath");
		File file = new File(filePath);
		Assert.assertTrue(file.exists());
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
			unextractableWordParagraphStyles = testCases.get(i)[6];
			unextractableWordCharacterStyles = testCases.get(i)[7];
			selectedInternalTextStyles = testCases.get(i)[8];

			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			iFilterConfig.wordFilterOperation(selenium, wFilterName, headerInfo, toolTips, tableofContent,
					contentPostFilter, internalTextPostFilter, unextractableWordParagraphStyles,
					unextractableWordCharacterStyles, selectedInternalTextStyles);

			tmp.createJob(filterJob + i, ConfigUtil.getDataInCase(getClassName(), "SourceFile"),
					ConfigUtil.getDataInCase(getClassName(), "fileProfileNamesStr"),
					ConfigUtil.getDataInCase(getClassName(), "targetLocales"));
			Thread.sleep(1000);
			jobNames[i] = filterJob + i;

		}

		// for (int i=1; i<testCases.size(); i++)
		// {
		// jobNames[i]="wordJob201110-18-130859"+i;
		// }

		for (int i = 1; i < testCases.size(); i++) {

			selenium.click(MainFrame.MY_JOBS_MENU);
			selenium.click(MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			selenium.select(MyJobs.JobName_SELECTION, MyJobs.JobName_Slection_Ends_With);
			selenium.type("name=" + MyJobs.SEARCH_JOB_NAME_TEXT, jobNames[i]);
			selenium.click(MyJobs.SEARCH_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			BasicFuncs basic = new BasicFuncs();
			if (selenium.isElementPresent("link=" + jobNames[i])) {
				String wordCountGot = basic.jobgetWordCount(selenium, MyJobs.MyJobs_AllStatus_TABLE, jobNames[i], 7);

				wordcount = wordcount + " \r\n" + jobNames[i] + ", " + wordCountGot;
			}
		}

		FileWriter writer = new FileWriter(wordCountResultPath_jobDetails, true);
		writer.write(wordcount);
		writer.close();

	}
}
