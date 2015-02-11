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
 * Automation test case for GBS-2305
 * missing spaces in final exported file office 2010 filter
 * 
 * Reporter         Andrew Gibbons
 * Assignee         Wayne Zou
 * Affects version  None
 * Fix version      8.2.1
 *
 * @author  Vincent Yan, 2012-2-15
 * @version 1.0
 * @since   8.2.2
 */
public class GBS2305Test extends BaseTestCase
{
    @Test
    public void testGBS2305() throws Exception {
        // Create job
        String jobName = getDataInCase("jobName");
        String filePaths = getDataInCase("filePaths");
        String fileProfiles = getDataInCase("fileProfiles");
        String targetLocales = getDataInCase("targetLocales");

        SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles,
                targetLocales);

        // Login with Translator
        String userName = getDataInCase("username");
        String company = ConfigUtil.getConfigData("company");
        SeleniumUtils.relogin(selenium, company + userName, "password");

        String generatedJobName = CreatedJob.getCreatedJobName(jobName);

        boolean isActivityCreatedSucc = SeleniumTestHelper
                .isActivityCreatedSucc(selenium, generatedJobName);
        if (!isActivityCreatedSucc)
            return;
        
        String expectedWordCount = getDataInCase("expectedWordCount");
        SeleniumTestHelper.verifyWordCount(selenium, generatedJobName, expectedWordCount);
        
        SeleniumUtils.clickAndWait(selenium, "link=" + generatedJobName);

        SeleniumUtils.clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);

        SeleniumUtils
                .clickAndWait(selenium, MyActivities.TASK_COMPLETED_BUTTON);
        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
        }

        CommonFuncs.loginSystemWithAdmin(selenium);

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
}
