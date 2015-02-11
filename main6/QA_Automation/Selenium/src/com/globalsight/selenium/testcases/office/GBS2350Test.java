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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for GBS-2350
 * Extracted segments are too short to translate if use PowerPoint 2010 file profile
 * 
 * Reporter         Erica Li
 * Assignee         Wayne Zou
 * Affects version  8.2
 * Fix version      8.2.1
 *
 * @author  Vincent Yan, 2012-2-15
 * @version 1.0
 * @since   8.2.2
 */
public class GBS2350Test extends BaseTestCase
{
    @Test
    public void testGBS2350() throws Exception {
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
        
        SeleniumUtils.clickAndWait(selenium, "link=" + generatedJobName);

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
                + "/tr[2]/td[2]/span[@class='editorSegment']"));
        Assert.assertTrue(selenium.isElementPresent(JobEditors.Segments_TABLE
                + "/tr[3]/td[2]/span[@class='editorSegment']"));
        Assert.assertTrue(selenium.isElementPresent(JobEditors.Segments_TABLE
                + "/tr[4]/td[2]/span[@class='editorSegment']"));
        Assert.assertTrue(selenium.isElementPresent(JobEditors.Segments_TABLE
                + "/tr[5]/td[2]/span[@class='editorSegment']"));

        // Close the MainEditor
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.MENU_WINDOW);
        selenium.click(JobEditors.SEGMENT_CLOSE_LINK);
        selenium.selectWindow(null);
    }
}
