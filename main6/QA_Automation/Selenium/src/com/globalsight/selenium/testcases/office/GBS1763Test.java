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
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.MyAccountFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyAccount;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for GBS-1763
 * Office 2010 Filter - Missing spaces in exported target file.
 * 
 * Reporter         Dawn Cao
 * Assignee         Wayne Zou
 * Affects version  8.0
 * Fix version      8.1
 *
 * @author  Vincent Yan, 2012-2-24
 * @version 1.0
 * @since   8.2.2
 */
public class GBS1763Test extends BaseTestCase
{
    private boolean usingExist = false;
    
    @Test
    public void testGBS1763() throws Exception {
        // Create Office 2010 filter for GBS-1763
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILTER_CONFIGURATION_SUBMENU);
        SeleniumUtils.click(selenium, FilterConfiguration.EXPAND_ALL_BUTTON);
        String filterName = getDataInCase("filterName");
        if (!selenium.isTextPresent(filterName)) {
            // Create new filter for GBS-1763
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
            SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT, filterName);
            SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT, getDataInCase("filterDesc"));
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX);
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_CHECKALL_CHECKBOX);
            SeleniumUtils.select(selenium, FilterConfiguration.OFFICE_2010_FILTER_WORD_STYLE_SELECT, "Unextractable Word Character Styles", true);
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_CHECKALL_CHECKBOX);
            
            SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
        }
        
        // Create new file profile for GBS-1763 using with filter
        FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
        FileProfile fileProfile = fileProfileFuncs
                .getFileProfileInfo(testCaseName);
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, true);
        
        // Create job
        String jobName = getDataInCase("jobName");
        String filePaths = getDataInCase("filePaths");
        String fileProfiles = getDataInCase("fileProfiles");
        String targetLocales = getDataInCase("targetLocales");

        SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles,
                targetLocales);
 
        // Login with Translator, accept and finish task
        CommonFuncs.loginSystemWithAnyone(selenium);
        
        MyAccountFuncs myAccountFuncs = new MyAccountFuncs();
        myAccountFuncs.changeDefaultEditor(selenium, "Inline");

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

        selenium.click(JobEditors.INLINE_SEGMENT_TABLE + "/p[84]");
        selenium.selectFrame(JobEditors.INLINE_EDITOR_FRAME);
        selenium.type("//body", getDataInCase("translatedString"));
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.click(JobEditors.CHANGE_SAVE_BUTTON);
        Assert.assertEquals(
                selenium.getText(JobEditors.INLINE_SEGMENT_TABLE + "/p[84]"),
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
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }
        Thread.sleep(10000);

        CommonFuncs.loginSystemWithAdmin(selenium);

        SeleniumTestHelper.exportJob(selenium, generatedJobName, getDataInCase("workflow"));
        SeleniumTestHelper.downloadAndCompare(generatedJobName, filePaths);
    }
}
