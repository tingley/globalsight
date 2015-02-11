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
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.MyAccountFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for GBS-1766
 * Failed to import tmx with "isTranslate" and "innerTextNodeIndex" attributes.
 * 
 * Reporter        Dawn Cao 
 * Assignee        Walter Xu 
 * Affects version 8.0 
 * Fix version     8.0
 * 
 * @author Vincent Yan, 2012-2-22
 * @version 1.0
 * @since 8.2.2
 */
public class GBS1766Test extends BaseTestCase
{
    @Test
    public void testGBS1766() throws Exception
    {
        MyAccountFuncs myAccountFuncs = new MyAccountFuncs();
        myAccountFuncs.changeDefaultEditor(selenium, "Inline");

        // Create job
        String jobName = getDataInCase("jobName");
        String filePaths = getDataInCase("filePaths");
        String fileProfiles = getDataInCase("fileProfiles");
        String targetLocales = getDataInCase("targetLocales");

        SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles,
                targetLocales);

        // Login with Translator, accept and finish task
        CommonFuncs.loginSystemWithAnyone(selenium);

        String generatedJobName = CreatedJob.getCreatedJobName(jobName);

        boolean isActivityCreatedSucc = SeleniumTestHelper
                .isActivityCreatedSucc(selenium, generatedJobName);
        if (!isActivityCreatedSucc)
            return;

        SeleniumUtils.clickAndWait(selenium, "link=" + generatedJobName);

        SeleniumUtils.clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);

        // Open the MainEditor
        selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/a");
        selenium.waitForPopUp(JobEditors.INLINE_EDITOR_TAG,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name=" + JobEditors.INLINE_EDITOR_TAG);

        selenium.click(JobEditors.INLINE_SEGMENT_TABLE + "/p[8]");
        selenium.selectFrame(JobEditors.INLINE_EDITOR_FRAME);
        selenium.type("//body", getDataInCase("translatedString"));
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.click(JobEditors.CHANGE_SAVE_BUTTON);
        Assert.assertEquals(
                selenium.getText(JobEditors.INLINE_SEGMENT_TABLE + "/p[8]"),
                getDataInCase("translatedString"));

        selenium.click(JobEditors.INLINE_CLOSE_BUTTON);
        selenium.selectWindow(null);

        SeleniumUtils
                .clickAndWait(selenium, MyActivities.TASK_COMPLETED_BUTTON);
        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
        }

        // Login with PM, accept and finish the whole job
        // TODO Below codes may be modified according with different work flow
        //      definition 
        CommonFuncs.loginSystemWithPM(selenium);
        isActivityCreatedSucc = SeleniumTestHelper.isActivityCreatedSucc(
                selenium, generatedJobName);
        if (!isActivityCreatedSucc)
            return;

        SeleniumUtils.clickAndWait(selenium, "link=" + generatedJobName);
        SeleniumUtils.clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);
        SeleniumUtils
                .clickAndWait(selenium, MyActivities.TASK_COMPLETED_BUTTON);

        Thread.sleep(10000);

        CommonFuncs.loginSystemWithAdmin(selenium);

        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);

        SeleniumUtils.type(selenium, MyJobs.SEARCH_JOB_NAME_TEXT,
                generatedJobName);
        SeleniumUtils.clickAndWait(selenium, MyJobs.SEARCH_BUTTON);

        SeleniumUtils.click(selenium, "link=" + generatedJobName);
        SeleniumUtils.selectRadioButtonFromTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("workflow"));

        SeleniumUtils.clickAndWait(selenium, JobDetails.EXPORT_BUTTON);

        SeleniumUtils.click(selenium, JobDetails.EXPORT_EXECUTE_BUTTON);

        exportTM();

        // Re-import TM
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);
        TMFuncs tmFuncs = new TMFuncs();
        tmFuncs.importTM(selenium, getDataInCase("tm.import"));

        exportTM();
    }

    private void exportTM() throws Exception, InterruptedException
    {
        // Export TM
        String tmName = getDataInCase("tmName");
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);
        boolean isSelected = SeleniumUtils.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, tmName);
        if (isSelected)
        {
            SeleniumUtils.clickAndWait(selenium, TMManagement.EXPORT_BUTTON);
            SeleniumUtils.clickAndWait(selenium,
                    TMManagement.EXPORT_NEXT_BUTTON);

            SeleniumUtils.clickAndWait(selenium,
                    TMManagement.EXPORT_NEXT_BUTTON);

            selenium.waitForCondition("var imsg=selenium.getText(\""
                    + TMManagement.EXPORT_PROGRESS_MSG
                    + "\"); imsg==\"1 entries (100%)\"", CommonFuncs.SHORT_WAIT);
            Assert.assertEquals(selenium.getText(TMManagement.EXPORT_MESSAGE),
                    getDataInCase("tm.export.verify"));
            selenium.click(TMManagement.DOWNLOAD_FILE_BUTTON);
            Thread.sleep(5000);
            SeleniumUtils.clickAndWait(selenium, TMManagement.EXPORT_OK_BUTTON);
        }
        else
        {
            Reporter.log("Cannot select the " + tmName + " TM");
            return;
        }
    }
}
