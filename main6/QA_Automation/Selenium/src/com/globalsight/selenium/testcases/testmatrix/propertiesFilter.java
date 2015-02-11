package com.globalsight.selenium.testcases.testmatrix;

import junit.framework.Assert;

import org.testng.Reporter;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.smoketest.MyJobsDetailsDispatch;

public class propertiesFilter extends BaseTestCase {
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private BasicFuncs basic = new BasicFuncs();
	private TestMatrixJobPrepare matri = new TestMatrixJobPrepare();
	private MyJobsDetailsDispatch jobAction = new MyJobsDetailsDispatch();

	String wPropertiesName = ConfigUtil.getDataInCase(getClassName(), "FilterName");
	boolean bEnableSID = Boolean.parseBoolean(ConfigUtil.getDataInCase(getClassName(), "enableSID"));
	boolean bUnicodeEsp = Boolean.parseBoolean(ConfigUtil.getDataInCase(getClassName(), "enableUnicodeEsp"));
	boolean bPresvTrailSpace = Boolean.parseBoolean(ConfigUtil.getDataInCase(getClassName(), "preserveTrailingSpace"));
	String secondaryFilterArray = ConfigUtil.getDataInCase(getClassName(), "secondaryFilter");
	String[] secondaryFilter = secondaryFilterArray.split(",");
	String sIntnlTextFilterArray = ConfigUtil.getDataInCase(getClassName(), "internalTextFilter");
	String[] sIntnlTextFilter = sIntnlTextFilterArray.split(",");
	String sIntnlText = ConfigUtil.getDataInCase(getClassName(), "InternalTexts");
	String fpname = ConfigUtil.getDataInCase(matri.getClassName(), "preparejob_file_profile_names");
	String[] fp = fpname.split(",");
	String dir = ConfigUtil.getDataInCase(getClassName(), "JobFileDir");;
	String targetLocales = ConfigUtil.getDataInCase(matri.getClassName(), "jobTargetLocales");

	@Test
	public void initPropertiesFilter() throws Exception {

		selenium.click(MainFrame.DATA_SOURCES_MENU);
		selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		// new internal text filter
		iFilterConfig.InternalText(selenium, sIntnlText,
				ConfigUtil.getDataInCase(getClassName(), "internalTextFilterName"));

		// new a properties filter
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_IMG);
		if (selenium.isElementPresent("link=" + wPropertiesName)) {
			Reporter.log("The javaproperties filter " + wPropertiesName + " has already exists!");
		} else {
			// Create the Java Properties Filter
			selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_ADD_BUTTON);
			selenium.type(FilterConfiguration.JAVA_PROPERTIES_FILTER_NAME_TEXT, wPropertiesName);
			selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SAVE_BUTTON);

			Assert.assertEquals(selenium.isElementPresent("link=" + wPropertiesName), true);
		}
		;

		// edit the filter according to properties and create job for verify
		for (int i = 0; i < secondaryFilter.length; i++) {
			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			iFilterConfig.propertiesFilter(selenium, wPropertiesName, bEnableSID, bUnicodeEsp, bPresvTrailSpace,
					secondaryFilter[i], sIntnlTextFilter[i]);

			// Add filter to the corresponding file profile
			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			basic.selectRadioButtonFromTable(selenium, FileProfile.MAIN_TABLE, fp[2]);
			selenium.click(FileProfile.EDIT_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.select(FileProfile.FILTER_SELECT, "label=" + wPropertiesName);
			selenium.click(FileProfile.SAVE_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			String filterJob = "propertiesJob";

			CreateJobsFuncs tmp = new CreateJobsFuncs();
			tmp.createJob(filterJob + i, dir, fp[2], targetLocales);
			Thread.sleep(20000);

			selenium.click(MainFrame.MY_JOBS_MENU);
			selenium.click(MainFrame.MY_JOBS_READY_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(MainFrame.Search_BUTTON);
			selenium.click(MainFrame.Search_BUTTON);

			String WordcountExpect = ConfigUtil.getDataInCase(getClassName(), "WordCount");
			String[] WordCount = WordcountExpect.split(",");

			String wordCountGot = basic.jobgetWordCount(selenium, MyJobs.MyJobs_Ready_TABLE, filterJob, 7);
			// int correctWordCount = 39; //Expected wordcounts for the job
			if (!WordCount[i].equals(wordCountGot)) {
				System.out.println("Wrong test case for properties ");
			}

			Assert.assertEquals(WordCount[i], wordCountGot);
		}

	}
}
