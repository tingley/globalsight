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
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for GBS-1758
 * PowerPoint 2010 incorrectly exposing attributes for translation
 * 
 * Reporter         Chase Tingley
 * Assignee         Malcolm Liu
 * Affects version  7.1.8.4
 * Fix version      8.0
 *
 * @author  Vincent Yan, 2012-2-27
 * @version 1.0
 * @since   8.2.2
 */
public class GBS1758Test extends BaseTestCase
{
    @Test
    public void testGBS1758() throws Exception {
        // Create job
        String jobName = getDataInCase("jobName");
        String filePaths = getDataInCase("filePaths");
        String fileProfiles = getDataInCase("fileProfiles");
        String targetLocales = getDataInCase("targetLocales");
    
        SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles,
                targetLocales);
        String generatedJobName = CreatedJob.getCreatedJobName(jobName);
        String expectedWordCount = getDataInCase("expectedWordCount");
        
        // Login with Translator
        String userName = getDataInCase("username");
        String company = ConfigUtil.getConfigData("company");
        SeleniumUtils.relogin(selenium, company + userName, "password");

        boolean isActivityCreatedSucc = SeleniumTestHelper
                .isActivityCreatedSucc(selenium, generatedJobName);
        if (!isActivityCreatedSucc)
            return;

        SeleniumTestHelper.verifyWordCount(selenium, generatedJobName, expectedWordCount);
        
        CommonFuncs.loginSystemWithAdmin(selenium);
        
        SeleniumTestHelper.exportJob(selenium, generatedJobName, getDataInCase("workflow"));
        SeleniumTestHelper.downloadAndCompare(generatedJobName, filePaths);
    }
}
