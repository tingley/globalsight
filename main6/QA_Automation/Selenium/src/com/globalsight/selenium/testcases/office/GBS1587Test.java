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

package com.globalsight.selenium.testcases.office;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.OfflineDownload;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for GBS-1587
 * office 2010 to extract master data
 * 
 * Reporter         Andrew Gibbons
 * Assignee         Wayne Zou
 * Affects version  None
 * Fix version      7.1.8.3
 *
 * @author  Vincent Yan, 2012-2-27
 * @version 1.0
 * @since   8.2.2
 */
public class GBS1587Test extends BaseTestCase
{
    @Test
    public void testGBS1587() throws Exception {
        // Create office 2010 filter for GBS-1587
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILTER_CONFIGURATION_SUBMENU);
        String filterName = getDataInCase("filterName");
        SeleniumUtils.click(selenium, FilterConfiguration.EXPAND_ALL_BUTTON);
        boolean exist = SeleniumUtils.isTextPresent(selenium, filterName);
        if (!exist) {
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
            SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT, filterName);
            SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT, getDataInCase("filterDesc"));
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_PPT_HANDOUT_TRANSLATE_CHECKBOX);
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_PPT_NOTES_MASTER_TRANSLATE_CHECKBOX);
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX);
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
        }
        
        // Create file profile
        FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
        FileProfile fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName);
        fileProfileFuncs.create(selenium, fileProfile);
        
        // Create job
        String jobName = getDataInCase("jobName");
        String filePaths = getDataInCase("filePaths");
        String fileProfiles = getDataInCase("fileProfiles");
        String targetLocales = getDataInCase("targetLocales");
    
        SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles,
                targetLocales);
        String generatedJobName = CreatedJob.getCreatedJobName(jobName);

        // Login with Translator
        String userName = getDataInCase("username");
        String company = ConfigUtil.getConfigData("company");
        SeleniumUtils.relogin(selenium, company + userName, "password");

        boolean isActivityCreatedSucc = SeleniumTestHelper
                .isActivityCreatedSucc(selenium, generatedJobName);
        if (!isActivityCreatedSucc)
            return;
        
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU, MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);
        SeleniumUtils.clickAndWait(selenium, "link=" + generatedJobName);
        SeleniumUtils.click(selenium, MyActivities.ACCEPT_JOB_BUTTON);

        SeleniumUtils.clickAndWait(selenium, MyActivities.WORK_OFFLINE_TAB);
        SeleniumUtils.clickAndWait(selenium, OfflineDownload.START_DOWNLOAD_BUTTON);
        while (true) {
            if (selenium.isElementPresent(OfflineDownload.DONE_BUTTON))
                break;
        }
        
        SeleniumUtils.clickAndWait(selenium, OfflineDownload.DONE_BUTTON);
    }

}
