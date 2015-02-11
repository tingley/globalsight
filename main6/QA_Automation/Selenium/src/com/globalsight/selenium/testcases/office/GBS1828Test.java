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

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for GBS-1828
 * Word2010- DONOTTRANSLATE_char sometimes excludes text it shouldn't.
 * 
 * Reporter         Jessie Li
 * Assignee         Donald Pan
 * Affects version  8.0
 * Fix version      None
 *
 * @author  Vincent Yan, 2012-2-16
 * @version 1.0
 * @since   8.2.2
 */
public class GBS1828Test extends BaseTestCase
{
    @Test
    public void testGBS1828() throws Exception {
        // Create office 2010 filter for GBS-1828
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILTER_CONFIGURATION_SUBMENU);
        FilterConfigurationFuncs fcFuncs = new FilterConfigurationFuncs();
        String filterName = getDataInCase("filterName");
        SeleniumUtils.click(selenium, FilterConfiguration.EXPAND_ALL_BUTTON);
        boolean exist = SeleniumUtils.isTextPresent(selenium, filterName);
        if (exist)
            fcFuncs.removeFilters(selenium, filterName);
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
        SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT, filterName);
        SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT, getDataInCase("filterDesc"));
        SeleniumUtils.select(selenium, FilterConfiguration.OFFICE_2010_FILTER_WORD_STYLE_SELECT, "unextractableWordCharacterStyles", false);
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_DONOTTRANSLATE_CHAR_CHECKBOX);
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
        
        // Create file profile
        FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
        FileProfile fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName);
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, true);
        
        // Create job with filter 
        String jobName = getDataInCase("jobName");
        String filePaths = getDataInCase("filePaths");
        String fileProfiles = getDataInCase("fileProfiles");
        String targetLocales = getDataInCase("targetLocales");
        SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles,
                targetLocales);
        
        // Create job without filter
        String jobName1 = getDataInCase("jobName1");
        filePaths = getDataInCase("filePaths1");
        fileProfiles = getDataInCase("fileProfiles1");
        targetLocales = getDataInCase("targetLocales1");
        SeleniumTestHelper.createJob(jobName1, filePaths, fileProfiles, targetLocales);

        // Login with Translator
        String userName = getDataInCase("username");
        String company = ConfigUtil.getConfigData("company");
        SeleniumUtils.relogin(selenium, company + userName, "password");

        String generatedJobName = CreatedJob.getCreatedJobName(jobName);
        String generatedJobName1 = CreatedJob.getCreatedJobName(jobName1);
        
        String expectedWordCount = getDataInCase("expectedWordCount");
        String expectedWordCount1 = getDataInCase("expectedWordCount1");

        verifyAndFinishActivity(generatedJobName, expectedWordCount);
        verifyAndFinishActivity(generatedJobName1, expectedWordCount1);

        CommonFuncs.loginSystemWithAdmin(selenium);

        exportAndDownloadJob(generatedJobName, filePaths);
        exportAndDownloadJob(generatedJobName1, filePaths);
    }

    private void exportAndDownloadJob(String generatedJobName, String filePaths)
            throws Exception
    {
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);

        SeleniumUtils
                .type(selenium, MyJobs.SEARCH_JOB_NAME_TEXT, generatedJobName);
        SeleniumUtils.clickAndWait(selenium, MyJobs.SEARCH_BUTTON);

        SeleniumUtils.click(selenium, "link=" + generatedJobName);
        SeleniumUtils.selectRadioButtonFromTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("workflow"));

        SeleniumUtils.clickAndWait(selenium, JobDetails.EXPORT_BUTTON);

        SeleniumUtils.click(selenium, JobDetails.EXPORT_EXECUTE_BUTTON);

        SeleniumTestHelper.downloadAndCompare(generatedJobName, filePaths);
    }

    private void verifyAndFinishActivity(String generatedJobName, String expectedWordCount)
            throws Exception
    {
        boolean isActivityCreatedSucc = SeleniumTestHelper
                .isActivityCreatedSucc(selenium, generatedJobName);
        if (!isActivityCreatedSucc)
            return;
        
        SeleniumTestHelper.verifyWordCount(selenium, generatedJobName, expectedWordCount);
        
        SeleniumUtils.clickAndWait(selenium, "link=" + generatedJobName);

        SeleniumUtils.clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);

        SeleniumUtils
                .clickAndWait(selenium, MyActivities.TASK_COMPLETED_BUTTON);
        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
        }
    }
}
