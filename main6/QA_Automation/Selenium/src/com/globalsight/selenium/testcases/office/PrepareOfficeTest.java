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

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Automation test case for Office set Preparation for setting up some basic
 * configurations to run all Office set tests
 * 
 * @author Vincent Yan, 2012/02/08
 * @version 1.0
 * @since 8.2.2
 */

public class PrepareOfficeTest extends BaseTestCase
{
    private boolean usingExist = false;

    @Test
    public void prepareOfficeTest() throws Exception
    {
        ConfigUtil
                .setPropertyFile(PropertyFileConfiguration.PREPARE_OFFICE_TEST_PROPERTIES);

        String usingExistStr = ConfigUtil.getProperty("usingExist");
        usingExist = Boolean.parseBoolean(usingExistStr);
        String data = "";

        // Locale Pair
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALE_PAIRS_SUBMENU);
        LocalePairsFuncs localePairsFuncs = new LocalePairsFuncs();
        localePairsFuncs.newLocalPairs(selenium, getProperty("localePairs.sourceLocale"),
                getProperty("localePairs.targetLocale"));
        
        // Create User
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);
        UsersFuncs usersFuncs = new UsersFuncs();
        usersFuncs.newUsers(selenium, getProperty("user"));

        // Create TM
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);
        TMFuncs tmFuncs = new TMFuncs();
        tmFuncs.newTM(selenium, getProperty("tm"));
        tmFuncs.newTM(selenium, getProperty("tm_mt"));

        // Create TMP
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);
        TMProfileFuncs tmpFuncs = new TMProfileFuncs();
        data = getProperty("tmProfile");
        tmpFuncs.newTMProfile(selenium, data);
        
        data = getProperty("tmProfile_MT");
        tmpFuncs.newTMProfile(selenium, data);

        HashMap<String, String> details = SeleniumTestHelper.splitData(data);
        boolean isElementSelected = SeleniumUtils.selectRadioButtonFromTable(
                selenium, TMProfile.TM_PROFILE_LIST_TABLE, details.get("name"));

        Assert.assertEquals(isElementSelected, true);

        if (isElementSelected)
        {
            // Set up the MT information
            SeleniumUtils.clickAndWait(selenium, TMProfile.MT_OPTIONS_BUTTON);
            SeleniumUtils.select(selenium, TMProfile.MT_ENGINE_SELECT,
                    getProperty("mtEngine"), true);
            if (SeleniumUtils.isTureValue(getProperty("mtOverride")))
            {
                SeleniumUtils.click(selenium,
                        TMProfile.MT_OVERRIDE_NON_EXTRACT_CHECKBOX);
                if (SeleniumUtils.isTureValue(getProperty("mtAutoCommit")))
                    SeleniumUtils
                            .click(selenium,
                                    TMProfile.MT_OVERRIDE_NON_EXTRACT_AUTO_COMMIT_RADIO);
                else
                {
                    if (SeleniumUtils
                            .isTureValue(getProperty("mtSensitiveLeveraging")))
                    {
                        SeleniumUtils
                                .click(selenium,
                                        TMProfile.MT_OVERRIDE_NON_EXTRACT_LEVERAGING_RADIO);
                        SeleniumUtils
                                .type(selenium,
                                        TMProfile.MT_OVERRIDE_NON_EXTRACT_LEVERAGING_PENALTY_TEXT,
                                        getProperty("mtLeveragingPenalty"));
                    }
                }
            }
            SeleniumUtils
                    .type(selenium, TMProfile.MT_URL, getProperty("mtUrl"));
            SeleniumUtils.type(selenium, TMProfile.MT_CLINTID,
                    getProperty("idMsMtClientid"));
			SeleniumUtils.type(selenium, TMProfile.MT_CLINT_SECRET,
					getProperty("idMsMtClientSecret"));
            SeleniumUtils.type(selenium, TMProfile.MT_CATEGORY,
                    getProperty("mtCategory"));
            SeleniumUtils.clickAndWait(selenium, TMProfile.MT_TEST_HOST_BUTTON);

            Assert.assertFalse(SeleniumUtils.isTextPresent(selenium,
                    "Error: Can not connect to MS Translator engine."));

            SeleniumUtils.clickAndWait(selenium, TMProfile.OK_BUTTON);
        }

        // Create project
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);
        ProjectsFuncs projectsFuncs = new ProjectsFuncs();
        data = getProperty("project");
        projectsFuncs.newProject(selenium, data);
        details = SeleniumTestHelper.splitData(data);

        String projectName = details.get("projectname");

        // Create Workflow
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.WORKFLOWS_SUBMENU);
        WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
        workflowsFuncs.duplicateWorkFlow(selenium,
                getProperty("workflowDuplicatePrefix"),
                getProperty("workflowDuplicateOriginal"), projectName,
                "English (United States) [en_US]",
                getProperty("workflowDuplicateTargetLocales"));

        // Create Localization Profile
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        LocalizationFuncs localizationFuncs = new LocalizationFuncs();
        localizationFuncs.create2(selenium, getProperty("localization"));
        localizationFuncs.create2(selenium, getProperty("localization_MT"));

        // Create Filter
        SeleniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);
        SeleniumUtils.click(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
        SeleniumUtils.type(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT,
                getProperty("filterName"));
        SeleniumUtils.type(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT,
                getProperty("filterDesc"));
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_PPT_NOTES_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_LAYOUT_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_PPT_NOTES_MASTER_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_PPT_HANDOUT_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_EXCEL_TAB_NAMES_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_TOOLTIPS_TRANSLATE_CHECKBOX);

        SeleniumUtils.click(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);

        SeleniumUtils.click(selenium, FilterConfiguration.EXPAND_ALL_BUTTON);
        Assert.assertTrue(SeleniumUtils.isTextPresent(selenium,
                getProperty("filterName")));
        
        
        SeleniumUtils.click(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
        SeleniumUtils.type(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT,
                getProperty("ppt.filterName"));
        SeleniumUtils.type(selenium,
                FilterConfiguration.OFFICE_2010_FILTER_DESC_TEXT,
                getProperty("ppt.filterDesc"));
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX);
        SeleniumUtils
                .click(selenium,
                        FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX);
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_STYLE_ADD_BUTTON);
        SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_STYLE_NAME_TEXT, getProperty("ppt.filter.attr1"));
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_STYLE_SAVE_BUTTON);
        
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_STYLE_ADD_BUTTON);
        SeleniumUtils.type(selenium, FilterConfiguration.OFFICE_2010_FILTER_STYLE_NAME_TEXT, getProperty("ppt.filter.attr2"));
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_STYLE_SAVE_BUTTON);
        
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_CHECKALL_CHECKBOX);
        SeleniumUtils.click(selenium, FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);

        // Create File Profile
        FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
        
        // File Profile with filter, without MT
        FileProfile fileProfile = fileProfileFuncs
                .getFileProfileInfo(testCaseName);
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

        // File Profile without filter and MT
        fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName, "1");
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

        // File Profile with filter and MT
        fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName, "2");
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

        // File Profile without filter and MT
        fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName, "3");
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

        // File Profile without filter and MT, for GBS-2263
        fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName, "4");
        SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);
    }
}
