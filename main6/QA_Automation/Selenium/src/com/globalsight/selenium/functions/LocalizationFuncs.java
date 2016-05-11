package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.LocalizationElements;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.pages.Workflows;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class LocalizationFuncs extends BasicFuncs
{
    public void create(Selenium selenium, String names, String tmProfile, String targetLocalName, String targetLocalCode) throws Exception
    {
        create(selenium, names, tmProfile, targetLocalName, targetLocalCode, true);
    }

    
    public void create(Selenium selenium, String names, String tmProfile,
    		String targetLocalName, String targetLocalCode,
            boolean isAutoDispatch) throws Exception
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

            selenium.select("id="+targetLocalCode, "label="+targetLocalName);
  
            clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
        }
        
        boolean selected = selectRadioButtonFromTable(selenium,true,LocalizationElements.L10nProfilesNameFilter, names);
        if (selected)
        {
        	Reporter.log("The L10nProfile was created successfully.");
        	return;
        }
        
    }
    
    public void create(Selenium selenium, String names, String tmProfile, String sourceLocalCode, 
    		String targetLocalNames, String targetLocalCodes,
            boolean isAutoDispatch) throws Exception
    {
        String[] local_name = names.split(",");
        String[] itmProfile = tmProfile.split(",");
        
        for (int i = 0; i < local_name.length; i++)
        {
        	selenium.type(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, local_name[i]);
        	selenium.keyDown(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
        	selenium.keyUp(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
//        	selenium.waitForFrameToLoad("css=table.listborder", "1000");

        	if (!(selenium.isElementPresent("link=" + local_name[i]))){
        		
	            clickAndWait(selenium, LocalizationElements.MAIN_NEW_BUTTON);
	            
	            // input info
	            selenium.type(LocalizationElements.NEW_NAME_TEXT, local_name[i]);
	            selenium.select(LocalizationElements.NEW_TMP_SELECT, "label="
	                    + itmProfile[i]);
	            selenium.select(LocalizationElements.NEW_PROJECT_SELECT,
	                    "label=Template");
	            selenium.select(LocalizationElements.NEW_SOURCE_LOCALE_SELECT,
	            		sourceLocalCode);
	
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
	
	            String[] jtargetLocalCodes = targetLocalCodes.split(";");
	            String[] jtargetLocalNames = targetLocalNames.split(";");
	            for (int j=0; j < jtargetLocalCodes.length; j++)
	            {
	            	selenium.select("id="+jtargetLocalCodes[j], "label="+jtargetLocalNames[j].trim());	
	            }
	            
	            
	  
	            clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
        	
	        	selenium.type(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, local_name[i]);
	        	selenium.keyDown(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
	        	selenium.keyUp(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
	        	if (selenium.isElementPresent("link=" + local_name[i])) 
	        		Reporter.log("The L10nProfile was created successfully.");
	        	
        }
        }
        
    }

    public void create2(Selenium selenium, String str) throws Exception
    {
        
        String[] array = str.split(",");
        String targetCode = new String();
        String targetStr = new String();
        try {
        for (String localization : array)
        {
            String[] ivalue = localization.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            // input info
            if (iFieldName.equals("name"))
            {
            	
            	
            	selenium.type(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, iFieldValue);
		    	selenium.keyDown(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
		    	selenium.keyUp(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
//		    	selenium.waitForFrameToLoad("css=table.listborder", "1000");
		    	if (!(selenium.isElementPresent("link=" + iFieldValue))){
	            	clickAndWait(selenium, LocalizationElements.MAIN_NEW_BUTTON);
	            	selenium.type(LocalizationElements.NEW_NAME_TEXT, iFieldValue);
		    	} else return;
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
            else if (iFieldName.equals("TargetCode"))
            {
            	targetCode = iFieldValue;
            }
            else if (iFieldName.equals("target"))
            {
                targetStr = iFieldValue;
            }
            
        }
        } catch (Exception e) {
			Reporter.log(e.getMessage());
		}
        

//        selenium.click(LocalizationElements.NEW_NEXT_BUTTON);
//
//        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        String[] targetArray = targetStr.split(";");
        String[] targetCodeArray = targetCode.split(";");
//        int L = targetArray.length;
//        
//        for (int target : L)
//        {
//        	selenium.select(targetCodeArray[0], targetArray[0]);
//        }

//        for (String target : targetArray)
//        {
//            String[] temp = target.split("\\|\\|");
//            
//            selenium.select("id="+temp[target], "label="+temp[target]);
//            
//			if (temp[0].equals("French (France) [fr_FR]")) {
//				selenium.select(
//						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT1,
//						"label=" + temp[1]);
//			} else if (temp[0].equals("German (Germany) [de_DE]")) {
//				selenium.select(
//						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT2,
//						"label=" + temp[1]);
//			} else if (temp[0].equals("Italian (Italy) [it_IT]")) {
//				selenium.select(
//						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT3,
//						"label=" + temp[1]);
//			} else if (temp[0].equals("Spanish (Spain) [es_ES]")) {
//				selenium.select(
//						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT4,
//						"label=" + temp[1]);
//			} else {
//				selenium.select(
//						LocalizationElements.ATTACH_TARGET_LOCALE_SELECT5,
//						"label=" + temp[1]);
//			}
//            
//        }

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
        	try{
            	selenium.select(targetCodeArray[0], targetArray[0]);
        if (selenium.isElementPresent(LocalizationElements.ATTACH_SAVE_BUTTON))
        	clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
        	} catch (Exception e) {
        		Reporter.log(e.getMessage());
        	}

    }


    public void create(Selenium selenium, String l10nName, String tmProfile, String projects,
    		String priority, String sourceLocal, String usingTranslationMemory, 
    		boolean isAutoDispatch,	String targetLocalCodes, String workflowName
            ) throws Exception
    {
    	String[] itargetLocalCodes = targetLocalCodes.split(",");
        String[] iWFName = workflowName.split(",");
        
    	try {
    		selenium.type(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, l10nName);
        	selenium.keyDown(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
        	selenium.keyUp(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
//        	selenium.waitForFrameToLoad("css=table.listborder", "1000");

        	if (!(selenium.isElementPresent("link=" + l10nName))){
        		
	            clickAndWait(selenium, LocalizationElements.MAIN_NEW_BUTTON);
	            
	            // input info
	            selenium.type(LocalizationElements.NEW_NAME_TEXT, l10nName);
	            selenium.select(LocalizationElements.NEW_TMP_SELECT, "label=" + tmProfile);
	            selenium.select(LocalizationElements.NEW_PROJECT_SELECT, "label=" + projects);
	            selenium.select(LocalizationElements.NEW_SOURCE_LOCALE_SELECT, "lable=" + sourceLocal);
	            		
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
	    		
	        for (int i = 0; i < itargetLocalCodes.length; i++)
	        {
	        	
		            selenium.select("id="+itargetLocalCodes[i], "label="+iWFName[i].trim());	
		            
	        }  
		            
		  
		            clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);
	        	
		        	selenium.type(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, l10nName);
		        	selenium.keyDown(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
		        	selenium.keyUp(LocalizationElements.LOCALIZATION_PROFILE_SEARCH_CONTENT_TEXT, "\\13");
		        	if (selenium.isElementPresent("link=" + l10nName))
		        		Reporter.log("The L10nProfile was created successfully.");
	        	
	        	
	        }
    	}
    	catch (Exception e)
        {
            Reporter.log(e.toString());
        }
    	
        
    }
    
	// added by ShenYang 2011-06-25

    public void removeWorkflow(Selenium selenium, String iLocName, String iWFLangCode,
            String iWFName) throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium,true,
                LocalizationElements.L10nProfilesNameFilter, iLocName);

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
            
            selenium.select("id="+iWFLangCode, "label=None");



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
    		String iWFName, String targetLocalCode) throws Exception
    {
  
        try
        {
            boolean selected = selectRadioButtonFromTable(selenium,true,
            		LocalizationElements.L10nProfilesNameFilter,localizationProfileName);
            
                    
            if (!selected)
            {
            	
                Reporter.log("Cannot find the Localization Profile to edit.");
                return;
            }
            selenium.click("link="+localizationProfileName);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.select("id="+targetLocalCode, "label="+iWFName);
            
            clickAndWait(selenium, LocalizationElements.ATTACH_SAVE_BUTTON);

            // Verify if added
            selenium.click("link="+localizationProfileName);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            
            Assert.assertEquals(selenium.getSelectedLabel("id="+targetLocalCode), iWFName);
            
            
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
            return;
        }
    }
}
