/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.selenium.testcases.office2010;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;

/**
 * Automation test case for GBS-2256
 * styles setting not working well for office 2010 filter.
 * 
 * Reporter         Jessie Li
 * Assignee         Wayne Zou
 * Affects version  8.1.1
 * Fix version      8.2.1
 *
 * @author Vincent Yan
 * @date 2012/02/06
 */

@Test(groups = { "all", "GBS2256" })
public class GBS2256Test extends BaseTestCase {
	/*
	 * Common variables initialization.
	 */
	private boolean usingExist = false;
	private FileProfileFuncs fpFuncs = new FileProfileFuncs();

	@Test
	public void testGBS2256() throws Exception {
		String usingExistStr = getDataInCase("usingExist");
		usingExist = Boolean.parseBoolean(usingExistStr);

		// Add office 2010 filter
		addOffice2010Filters();

		// Add file profile
		FileProfile fileProfile = fpFuncs.getFileProfileInfo(testCaseName);
		SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

		// Create job
		String jobName = getDataInCase("jobName");
		String filePaths = getDataInCase("filePaths");
		String fileProfiles = getDataInCase("fileProfiles");
		String targetLocales = getDataInCase("targetLocales");

		String createdJobName = SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles, targetLocales);

		// Verify word count
		String expectedWordCount = getDataInCase("expectedWordCount");
		Assert.assertTrue(SeleniumTestHelper.verifyWordCount(selenium, createdJobName, expectedWordCount));

		// Verify segment states
		// Open the MainEditor
		selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/a");
		selenium.waitForPopUp(JobEditors.MAIN_EDITOR_TAG, CommonFuncs.SHORT_WAIT);
		selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);

		// Open SegmentsEditor
		selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
		selenium.selectFrame(JobEditors.CONTENT_FRAME);
		selenium.selectFrame(JobEditors.TARGET_FRAME);
		selenium.selectFrame(JobEditors.CONTENT_FRAME);

		// Check segment status
		Assert.assertTrue(selenium.isElementPresent(JobEditors.Segments_TABLE
				+ "/tr/td[2]/span[@class='editorSegment']"));
		Assert.assertTrue(selenium.isElementPresent(JobEditors.Segments_TABLE
				+ "/tr[2]/td[2]/span[@class='editorSegmentRepetition']"));
		Assert.assertTrue(selenium.isElementPresent(JobEditors.Segments_TABLE
				+ "/tr[3]/td[2]/span[@class='editorSegment']"));

		// Close the MainEditor
		selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
		selenium.selectFrame(JobEditors.MENU_WINDOW);
		selenium.click(JobEditors.SEGMENT_CLOSE_LINK);
		selenium.selectWindow(null);

		// Export job
		String workflow = getDataInCase("workflow");
		SeleniumTestHelper.exportJob(selenium, createdJobName, workflow);

		// Download job and compare source and target files
		SeleniumTestHelper.downloadAndCompare(createdJobName, filePaths);
	}

	/**
	 * Create a new office 2010 filter If the filter name defined in the
	 * properties file, a new office 2010 filter suffix with time stamp will be
	 * created.
	 * 
	 * @return
	 * @throws Exception
	 */
	private void addOffice2010Filters() throws Exception {
		openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILTER_CONFIGURATION_SUBMENU);

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		String filterName = getDataInCase("filterName");

		// Need to create new Office 2010 filter
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);

		// Create the office 2010 Filter
		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);

		selenium.type(FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT, filterName);
		selenium.type(FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT, getDataInCase("filterDesc"));
		selenium.select(FilterConfiguration.OFFICE_2010_FILTER_WORD_STYLE_SELECT, "index=0");
		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_CHECKALL_CHECKBOX);
		selenium.select(FilterConfiguration.OFFICE_2010_FILTER_WORD_STYLE_SELECT, "index=1");
		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_CHECKALL_CHECKBOX);

		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
		if (selenium.isAlertPresent()) {
			selenium.getAlert();
			selenium.click(FilterConfiguration.HTML_FILTER_CANCEL_BUTTON);
		}

		// selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		Assert.assertTrue(selenium.isTextPresent(filterName));
	}
}
