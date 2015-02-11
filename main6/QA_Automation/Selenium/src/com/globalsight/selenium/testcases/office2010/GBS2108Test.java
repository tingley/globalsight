/**
 * Copyright 2009, 2012 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.selenium.testcases.office2010;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;

/**
 * Automation Test for GBS-2108 
 * DOC/RTF - Unnecessary G1 Tags resulting into Fuzzies
 * 
 * Reporter         Nilesh Rawal
 * Assignee         Leon Song
 * Affects version  8.1.1
 * Fix version      8.2
 *
 * @author Vincent Yan
 * @date 2012/02/06
 */

public class GBS2108Test extends BaseTestCase {
	private boolean usingExist = false;
	private FileProfileFuncs fpFuncs = new FileProfileFuncs();

	@Test
	public void testGBS2108() throws Exception {
		String usingExistStr = getDataInCase("usingExist");
		usingExist = Boolean.parseBoolean(usingExistStr);

		//Add office 2010 filter
		addOffice2010Filter();
		
		//Add file profile
		FileProfile fileProfile = fpFuncs.getFileProfileInfo(testCaseName);
		SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

		//Create job
		String jobName = getDataInCase("jobName");
		String filePaths = getDataInCase("filePaths");
		String fileProfiles = getDataInCase("fileProfiles");
		String targetLocales = getDataInCase("targetLocales");

		String createdJobName = SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles, targetLocales);

		//Verify word count
		String expectedWordCount = getDataInCase("expectedWordCount");
		Assert.assertTrue(SeleniumTestHelper.verifyWordCount(selenium, createdJobName, expectedWordCount));

		//Verify segment count
		String expectedSegmentCount = getDataInCase("expectedSegmentCount");
		Assert.assertTrue(SeleniumTestHelper.verifySegmentCount(selenium, createdJobName, expectedSegmentCount));

		//Export job
		String workflow = getDataInCase("workflow");
		SeleniumTestHelper.exportJob(selenium, createdJobName, workflow);

		//Download and compare source and target files
		SeleniumTestHelper.downloadAndCompare(createdJobName, filePaths);
	}

	/**
	 * Add office 2010 filter for GBS-2108
	 * Create an office2010 filter, add a unextractable character style 
	 * "tw4winExternal" and check it.
	 */
	private void addOffice2010Filter() {
		openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILTER_CONFIGURATION_SUBMENU);

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		String filterName = getDataInCase("filterName");
		boolean exist = selenium.isTextPresent(filterName);
		if (exist && usingExist)
			return;

		// Need to create new Office 2010 filter
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);

		// Create the office 2010 Filter
		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);

		selenium.type(FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT, filterName);
		selenium.type(FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT, getDataInCase("filterDesc"));
		selenium.select(FilterConfiguration.OFFICE_2010_FILTER_WORD_STYLE_SELECT, "index=1");
		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_STYLE_ADD_BUTTON);
		selenium.type(FilterConfiguration.OFFICE_2010_FILTER_STYLE_NAME_TEXT, getDataInCase("addStyleName"));
		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_STYLE_SAVE_BUTTON);
		selenium.click("//div[@id='o2010StyleContent']/table/tbody/tr[4]/td/input");

		selenium.click(FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
		if (selenium.isAlertPresent()) {
			selenium.getAlert();
			selenium.click(FilterConfiguration.OFFICE_2010_FILTER_CANCEL_BUTTON);
		}

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		Assert.assertTrue(selenium.isTextPresent(filterName));
	}
}
