package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.LocalizationElements;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class LocalizationFuncs extends BasicFuncs
{
    public void create(Selenium selenium, String names, String tmProfile)
    {
        create(selenium, names, tmProfile, true);
    }

    public void create(Selenium selenium, String names, String tmProfile,
            boolean isAutoDispatch)
    {
        String[] local_name = names.split(",");
        for (int i = 0; i < local_name.length; i++)
        {
            clickAndWait(selenium, LocalizationElements.MAIN_NEW_BUTTON);
            
            // input info
            selenium.type(LocalizationElements.NEW_NAME_TEXT, local_name[i]);
            selenium.select(LocalizationElements.NEW_TMP_SELECT, "label="
                    + tmProfile);
            selenium.select(LocalizationElements.NEW_PROJECT_SELECT,
                    "label=Template");
            selenium.select(LocalizationElements.NEW_SOURCE_LOCALE_SELECT,
                    "label=English (United States) [en_US]");

            // Default selection is Automatic Dispatch
            if (isAutoDispatch)
            {
                selenium.select(LocalizationElements.WF_Dispatch,
                        "label=Automatic");
            }
            else
            {
                selenium.select(LocalizationElements.WF_Dispatch,
                        "label=Manual");
            }

            //selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
            if (selenium.isAlertPresent())
            {
                Reporter.log(selenium.getAlert());
                clickAndWait(selenium, LocalizationElements.NEW_CANCEL_BUTTON);
                continue;
            }
            //selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            // attach workflow
            try
            {/*
                for (int j = 1; j < 100; j++)
                {
                    selenium.select(
                            LocalizationElements.ATTACH_TARGET_LOCALE_SELECT,
                            "index=" + j);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    selenium.click(LocalizationElements.ATTACH_RADIO);
                    selenium.click(LocalizationElements.ATTACH_ATTACH_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }*/
            	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT1, "value=1006");
            	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT2, "value=1000");
            	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT3, "value=1009");
            	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT4, "value=1003");
            }
            catch (Exception e)
            {
            }
            clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
        }

    }

    public void create2(Selenium selenium, String str) throws Exception
    {
        clickAndWait(selenium, LocalizationElements.MAIN_NEW_BUTTON);
        String[] array = str.split(",");
        String targetStr = new String();

        for (String localization : array)
        {
            String[] ivalue = localization.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            // input info
            if (iFieldName.equals("name"))
            {
                selenium.type(LocalizationElements.NEW_NAME_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("description"))
            {
                selenium.type(LocalizationElements.NEW_DESCRIPTION_TEXT,
                        iFieldValue);
            }
            else if (iFieldName.equals("optionalscript"))
            {
                selenium.type(LocalizationElements.SQLSCRIPT, iFieldValue);
            }
            else if (iFieldName.equals("tmprofile"))
            {
                selenium.select(LocalizationElements.NEW_TMP_SELECT, "label="
                        + iFieldValue);
            }
            else if (iFieldName.equals("project"))
            {
                selenium.select(LocalizationElements.NEW_PROJECT_SELECT,
                        "label=" + iFieldValue);
            }
            else if (iFieldName.equals("priority"))
            {
                selenium.select(LocalizationElements.JOBPRIORITY, "label="
                        + iFieldValue);
            }
            else if (iFieldName.equals("source"))
            {
                selenium.select(LocalizationElements.NEW_SOURCE_LOCALE_SELECT,
                        "label=" + iFieldValue);
            }
            else if (iFieldName.equals("wfDispatch"))
            {
                selenium.select(LocalizationElements.WF_Dispatch, "label="
                        + iFieldValue);
            }
            else if (iFieldName.equals("target"))
            {
                targetStr = iFieldValue;
            }
        }

//        selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
//
//        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        String[] targetArray = targetStr.split(";");

        for (String target : targetArray)
        {
            String[] temp = target.split("\\|\\|");
			if (temp[0].equals("French (France) [fr_FR]")) {
				selenium.select(
						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT1,
						"label=" + temp[1]);
			} else if (temp[0].equals("German (Germany) [de_DE]")) {
				selenium.select(
						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT2,
						"label=" + temp[1]);
			} else if (temp[0].equals("Italian (Italy) [it_IT]")) {
				selenium.select(
						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT3,
						"label=" + temp[1]);
			} else if (temp[0].equals("Spanish (Spain) [es_ES]")) {
				selenium.select(
						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT4,
						"label=" + temp[1]);
			} else {
				selenium.select(
						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT5,
						"label=" + temp[1]);
			}
            
        }

        // attach workflow
        /*
         * try { for (int j = 1; j < 100; j++) { selenium.select(
         * LocalizationElements.ATTACH_TARGET_LOCALE_SELECT, "index=" + j);
         * selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
         * selenium.click(LocalizationElements.ATTACH_RADIO);
         * selenium.click(LocalizationElements.ATTACH_ATTACH_BUTTON);
         * selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); } } catch
         * (Exception e) { }
         */
        clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
    }

    // added by ShenYang 2011-06-25

    public void removeWorkflow(Selenium selenium, String iLocName,
            String iWFName) throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium,
                LocalizationElements.Localization_TABLE, iLocName);

        if (!selected)
        {
            Reporter.log("Cannot find the Localization Name.");
            return;
        }
        try
        {
            selenium.click("link=" + iLocName);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            /*selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);*/

            // Remove selected workflow
           /* boolean selectWorkflow = selectRadioButtonFromTable(selenium,
                    LocalizationElements.Loc_Workflow_TABLE, iWFName);
            if (!selectWorkflow)
            {
                Reporter.log("Cannot find the Workflow.");
                return;
            }*/

            selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT1, "value=-1");
        	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT2, "value=-1");
        	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT3, "value=-1");
        	selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT4, "value=-1");
            // Verify if removed
            // Assert.assertEquals(this.isPresentInTable(selenium,LocalizationElements.Loc_Workflow_TABLE,
            // iWFName), false);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }

    }

    // added by ShenYang 2011-06-25

    public void editWorkflow(Selenium selenium, String iLocName, String iWFName)
            throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium,
                LocalizationElements.Localization_TABLE, iLocName);

        if (!selected)
        {
            Reporter.log("Cannot find the Localization Name.");
            return;
        }
        try
        {
            selenium.click(LocalizationElements.Edit_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            // edit selected workflow

            boolean selectWorkflow = selectRadioButtonFromTable(selenium,
                    LocalizationElements.Loc_Workflow_TABLE, iWFName);
            if (!selectWorkflow)
            {
                Reporter.log("Cannot find the Workflow.");
                return;
            }
            selenium.click(LocalizationElements.WF_Edit_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            String newWName = selenium
                    .getText(LocalizationElements.Target_Locale_TABLE
                            + "/tr[3]/td[2]");
            selenium.click(LocalizationElements.Target_Locale_TABLE
                    + "/tr[3]/td[1]//input");
            selenium.click(LocalizationElements.ATTACH_ATTACH_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            clickAndWait(selenium, LocalizationElements.WF_Save_BUTTON);

            // Verify
            selectRadioButtonFromTable(selenium,
                    LocalizationElements.Localization_TABLE, iLocName);
            selenium.click(LocalizationElements.Edit_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            Assert.assertEquals(this.isPresentInTable(selenium,
                    LocalizationElements.Loc_Workflow_TABLE, newWName), true);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }

    }

    // added by ShenYang 2011-06-25

    public void addWorkflow(Selenium selenium, String localizationProfileName,
            String targetLocale) throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium,
                LocalizationElements.Localization_TABLE,
                localizationProfileName);
        if (!selected)
        {
            Reporter.log("Cannot find the Localization Profile to edit.");
            return;
        }
        try
        {
            selenium.click(LocalizationElements.Edit_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(LocalizationElements.WF_Add_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.select(LocalizationElements.ATTACH_TARGET_LOCALE_SELECT,
                    targetLocale);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            String iWFName = selenium
                    .getText(LocalizationElements.Target_Locale_TABLE + "/tr["
                            + 2 + "]/td[2]");
            selenium.click(LocalizationElements.Target_Locale_TABLE + "/tr["
                    + 2 + "]/td[1]//input");

            selenium.click(LocalizationElements.ATTACH_ATTACH_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            // Verify if added
            selectRadioButtonFromTable(selenium,
                    LocalizationElements.Localization_TABLE,
                    localizationProfileName);
            selenium.click(LocalizationElements.Edit_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            Assert.assertEquals(this.isPresentInTable(selenium,
                    LocalizationElements.Loc_Workflow_TABLE, iWFName), true);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }
    }
}
