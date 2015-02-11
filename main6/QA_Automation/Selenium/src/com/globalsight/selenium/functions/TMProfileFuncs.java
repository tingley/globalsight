package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.AsiaOnlineMT;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: TMProfileFuncs.java
 * Author:Jester
 * Methods: TMProfilenew() 
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-2   First Draft    Jester
 */

public class TMProfileFuncs extends BasicFuncs {

	/*
	 * Create a new TM Profile, but first check if the TM is exists. If the TM
	 * profile has already exists. Click "Cancel" an back to the TM page.
	 */
	public String newTMProfile(Selenium selenium, String TMProfiles)
			throws Exception {
		selenium.click(TMProfile.NEW_VALUE_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		String[] iTMProfiles = TMProfiles.split(",");
		String iTMProfileName = null;

		for (String iTMProfile : iTMProfiles) {
			String[] ivalue = iTMProfile.split("=");
			String iFieldName = ivalue[0].trim();
			String iFieldValue = ivalue[1].trim();

			if (iFieldName.equals("name")) {
				selenium.type(TMProfile.Name_TEXTFIELD, iFieldValue);
				iTMProfileName = iFieldValue;
			} else if (iFieldName.equals("SRXruleset")) {
				selenium.select(TMProfile.SRXRuleSet_SELECT, "label="
						+ iFieldValue);
			} else if (iFieldName.equals("storageTM")) {
				selenium.select(TMProfile.StorageTM_SELECT, "label="
						+ iFieldValue);
			} else if (iFieldName.equals("referenceTM")) {
				selenium.select(TMProfile.ReferenceTMs_LABAL, "label="
						+ iFieldValue);
			} else if(iFieldName.equals("description")) {
			    selenium.type(TMProfile.Description, 
			    		iFieldValue);
			} else if(iFieldName.equals("threshold")) {
			    selenium.type(TMProfile.Threshold_TEXTFEILD, 
			    		iFieldValue);
			}
			
		}

		
		
		try {
			selenium.click(TMProfile.SAVE_BUTTON);
//			selenium.getAlert();
//			selenium.click(TMProfile.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {

			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		Assert.assertEquals(this.isPresentInTable(selenium,
				TMProfile.TM_PROFILE_LIST_TABLE, iTMProfileName), true);
		return iTMProfileName;
	}
	
	public void createMTOptions(Selenium selenium, String MTOptionsProfiles, AsiaOnlineMT asiaOnlineMT)
			throws Exception {
		
		String[] iMTOptionsProfiles = MTOptionsProfiles.split(",");
		boolean leveragingAvailable = false;
		boolean overrideNonExact = false;
		String mtEnginName = null;
		String iButton = null;
		
		for (String iMTOptionsProfile : iMTOptionsProfiles) 
		{
			
			String[] ivalue = iMTOptionsProfile.split("=");
			String iFieldName = ivalue[0].trim();
			String iFieldValue = ivalue[1].trim();
			
			if (iFieldName.equals("mt_tda")){
				if (iFieldValue.equals("MT")){
					iButton = TMProfile.MT_OPTIONS_BUTTON;
				}
				else if (iFieldValue.equals("TDA")){
					iButton = TMProfile.TDAOptions_BUTTON;
				}
				else {
					Assert.assertTrue(false, "mt_tda should be MT or TDA");
				}
				 
			} else if (iFieldName.equals("tmname")) {
				boolean selected = selectRadioButtonFromTable(selenium, TMProfile.TM_PROFILE_LIST_TABLE, iFieldValue);
		        if (!selected)
		        {
					Assert.assertTrue(false, "Cannot find a proper FileProfile name to edit.");
		        }
		        clickAndWait(selenium,iButton);
			} else if (iFieldName.equals("mtengine")) {
				selenium.select(TMProfile.MT_ENGINE_SELECT, "label=" + iFieldValue);
				mtEnginName = iFieldValue;
			} else if (iFieldName.equals("overwrride_non_exact")){
				if (iFieldValue.equals("true")){
					if(selenium.isChecked(TMProfile.MT_OVERRIDE_NON_EXTRACT_CHECKBOX)){
						overrideNonExact = true;
					} else {
						selenium.click(TMProfile.MT_OVERRIDE_NON_EXTRACT_CHECKBOX);
						overrideNonExact = true;
					}
				} else {
					if(selenium.isChecked(TMProfile.MT_OVERRIDE_NON_EXTRACT_CHECKBOX)){
						selenium.click(TMProfile.MT_OVERRIDE_NON_EXTRACT_CHECKBOX);
						overrideNonExact = false;
					} else {
						overrideNonExact = false;
					}
				}
			} else if (iFieldName.equals("mt_leveraging")) {
				if(overrideNonExact){
					if(iFieldValue.equals("true")){
						selenium.click(TMProfile.MT_OVERRIDE_NON_EXTRACT_LEVERAGING_RADIO);
						leveragingAvailable = true;
					} else {
						selenium.click(TMProfile.MT_OVERRIDE_NON_EXTRACT_AUTO_COMMIT_RADIO);
						leveragingAvailable = false;
					}
				}
			} else if (iFieldName.equals("penalty")) {
				if (leveragingAvailable){
					selenium.type(TMProfile.MT_OVERRIDE_NON_EXTRACT_LEVERAGING_PENALTY_TEXT, iFieldValue);
				}
			} else if (iFieldName.equals("show_in_eidtor")){
				if(iFieldValue.equals("true")){
					selenium.check(TMProfile.MT_SHOW_IN_EDITOR_CHECKBOX);	
				} else {
					selenium.uncheck(TMProfile.MT_SHOW_IN_EDITOR_CHECKBOX);
				}
			} else if (iFieldName.equals("tda_URL"))
			{
				if(iFieldValue.equals("NoTDA"))
				{
					if (selenium.isChecked(TMProfile.TDA_Enable_CHECKBOX)){
						selenium.click(TMProfile.TDA_Enable_CHECKBOX);
					}else
					{
						selenium.click(TMProfile.TDA_Enable_CHECKBOX);
						selenium.type(TMProfile.TDA_HostName_TEXTFEILD, "test");
						selenium.type(TMProfile.TDA_UserName_TEXTFEILD, "test");
						selenium.type(TMProfile.TDA_Password_TEXTFEILD, "test");
						selenium.click(TMProfile.TDA_Enable_CHECKBOX);
					}
				}else
				{
					if (!(selenium.isChecked(TMProfile.TDA_Enable_CHECKBOX))){
						selenium.click(TMProfile.TDA_Enable_CHECKBOX);
					}
					selenium.type(TMProfile.TDA_HostName_TEXTFEILD, iFieldValue);
				}
				mtEnginName = "TDA";
			} else if (iFieldName.equals("tda_username")){
				selenium.type(TMProfile.TDA_UserName_TEXTFEILD, iFieldValue);
			} else if (iFieldName.equals("tda_password")){
				selenium.type(TMProfile.TDA_Password_TEXTFEILD, iFieldValue);
			} else if (iFieldName.equals("mtAppId")) {
			    selenium.type(TMProfile.MT_APPID, iFieldValue);
			    
	            SeleniumUtils.clickAndWait(selenium, TMProfile.MT_TEST_HOST_BUTTON);

	            Assert.assertFalse(SeleniumUtils.isTextPresent(selenium,
	                    "Error: Can not connect to MS Translator engine."));
			}
		}
		if (mtEnginName.equals("Asia_Online")){
			selenium.type(TMProfile.AO_URL_TEXTFIELD, asiaOnlineMT.getHost());
			selenium.type(TMProfile.AO_Port_TEXTFIELD, asiaOnlineMT.getPort());
			selenium.type(TMProfile.AO_UserName_TEXTFIELD, asiaOnlineMT.getUsername());
			selenium.type(TMProfile.AO_Password_TEXTFIELD, asiaOnlineMT.getPassword());
			selenium.type(TMProfile.AO_Account_Number_TEXTFIELD, asiaOnlineMT.getAccountNumber());
			selenium.click(TMProfile.OK_BUTTON);
			if (selenium.isElementPresent(TMProfile.AO_ErrorMessage_Table)){
					Assert.assertTrue(false, "Incorrect setting from Asia Online.");
		            return;
		        	
			} else if (selenium.isAlertPresent()){
				selenium.getAlert();
				Assert.assertTrue(false, "Incorrect setting form Asia Online.");
			}
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.select(TMProfile.AO_Domain_Combination,  "label=" + asiaOnlineMT.getDomainCombination());
		} 
		
		SeleniumUtils.clickAndWait(selenium, TMProfile.OK_BUTTON);
		
		try {
		    if (selenium.isAlertPresent()) {
    			selenium.getAlert();
    			selenium.click(TMProfile.CANCEL_BUTTON);
    			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		    }
		} catch (Exception e) {
		    Reporter.log("Error: " + getClass().getName() + ", " + e.toString());
		}
		
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}
	public void editTMProfile(Selenium selenium, String TMProfiles)
			throws Exception {
		
		String[] iTMProfiles = TMProfiles.split(",");
		for (String iTMProfile : iTMProfiles) {
			String[] ivalue = iTMProfile.split("=");
			String iFieldName = ivalue[0].trim();
			String iFieldValue = ivalue[1].trim();
		
			if (iFieldName.equals("tmname")) {
				boolean selected = selectRadioButtonFromTable(selenium, TMProfile.TM_PROFILE_LIST_TABLE, iFieldValue);
		        if (!selected)
		        {
					Assert.assertTrue(false, "Cannot find a proper currency to edit.");
		        }
		        clickAndWait(selenium,TMProfile.EDIT_BUTTON);
			}
			if (iFieldName.equals("name")) {
				selenium.type(TMProfile.Name_TEXTFIELD, iFieldValue);
			} else if (iFieldName.equals("SRXruleset")) {
				selenium.select(TMProfile.SRXRuleSet_SELECT, "label="
						+ iFieldValue);
			} else if (iFieldName.equals("storageTM")) {
				selenium.select(TMProfile.StorageTM_SELECT, "label="
						+ iFieldValue);
			} else if (iFieldName.equals("referenceTM")) {
				selenium.select(TMProfile.ReferenceTMs_LABAL, "label="
						+ iFieldValue);
				selenium.removeAllSelections(TMProfile.reference_TM_Combo);
			} else if(iFieldName.equals("description")) {
			    selenium.type(TMProfile.Description, 
			    		iFieldValue);
			} else if(iFieldName.equals("threshold")) {
			    selenium.type(TMProfile.Threshold_TEXTFEILD, 
			    		iFieldValue);
			}
			
		}
		
		selenium.click(TMProfile.SAVE_BUTTON);
		
		try {
			if (selenium.isElementPresent(TMProfile.confirmChangeDialog))
			{
				selenium.click("css=#confirmChangeDialog > #div_button_arrangeTm > center > input[type=\"submit\"]");	
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			}
			
		} catch (Exception e) {
		
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
//		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		}
	
	
	
	/*
	 * Used in the case  /Selenium/src/com/globalsight/selenium/testcases/tmprofile/testMatrixJobPrepare.java
	 */
	public void TMPOperation(Selenium selenium, String[] testCases) 
	{
		String sRX_Rule_Set; 
		String storageTM;
        String save_unlocalized_segments_to_TM;
        String Save_unlocalized_segments_to_Page_TM;
        String exclude_Item_Types;
        String leverage_Localizables;
        String leverage_Default_Exact_Incontext_Matches;
        String leverage_Approved_translations_from_selected_Reference_TMs;
        String leverage_In_progress_translations_from_the_Job;
        String and_from_Jobs_that_write_to_the_Storage_TM;
        String and_from_Jobs_that_write_to_selected_Reference_TMs;
        String reference_TMs;
        String type_sensitive_Leveraging;
        String penalty1;
        String case_sensitive_Leveraging;
        String penalty2;
        String whitespace_sensitive_Leveraging;
        String penalty3;
        String code_sensitive_Leveraging;
        String penalty4;
        String reference_TM;
        String penalty5;
        String reference_TM_Combo;
        String multilingual_Leveraging;
        String auto_Repair_Placeholders;
        String multiple_Exact_Matches;
        String penalty6;
        String leverage_Match_Threshold;
        String number_of_Matches;
        String display_TM_Matches_by;
        String choose_Latest_Match;
        String type_sensitive_Leveraging2;
        String penalty7;
        String no_Multiple_Exact_Matches;
        String penalty8;
        
        sRX_Rule_Set = testCases[1].trim();
        storageTM = testCases[2].trim();
    	save_unlocalized_segments_to_TM = testCases[3].trim();
    	Save_unlocalized_segments_to_Page_TM = testCases[4].trim();
    	exclude_Item_Types = testCases[5].trim();
    	leverage_Localizables = testCases[6].trim();
    	leverage_Default_Exact_Incontext_Matches = testCases[7].trim();
    	leverage_Approved_translations_from_selected_Reference_TMs = testCases[8].trim();
    	leverage_In_progress_translations_from_the_Job = testCases[9].trim();
    	and_from_Jobs_that_write_to_the_Storage_TM = testCases[10].trim();
    	and_from_Jobs_that_write_to_selected_Reference_TMs = testCases[11].trim();
    	reference_TMs = testCases[12].trim();
    	type_sensitive_Leveraging = testCases[13].trim();
    	penalty1 = testCases[14].trim();
    	case_sensitive_Leveraging = testCases[15].trim();
    	penalty2 = testCases[16].trim();
    	whitespace_sensitive_Leveraging = testCases[17].trim();
    	penalty3 = testCases[18].trim();
    	code_sensitive_Leveraging = testCases[19].trim();
    	penalty4 = testCases[20].trim();
    	reference_TM = testCases[21].trim();
    	penalty5 = testCases[22].trim();
    	reference_TM_Combo = testCases[23].trim();
    	multilingual_Leveraging = testCases[24].trim();
    	auto_Repair_Placeholders = testCases[25].trim();
    	multiple_Exact_Matches = testCases[26].trim();
    	penalty6 = testCases[27].trim();
    	leverage_Match_Threshold = testCases[28].trim();
    	number_of_Matches = testCases[29].trim();
    	display_TM_Matches_by = testCases[30].trim();
    	choose_Latest_Match = testCases[31].trim();
    	type_sensitive_Leveraging2 = testCases[32].trim();
    	penalty7 = testCases[33].trim();
    	no_Multiple_Exact_Matches = testCases[34].trim();
    	penalty8 = testCases[35].trim();
		
		selenium.select(TMProfile.SRXRuleSet_SELECT, "label=" + sRX_Rule_Set);
		selenium.select(TMProfile.StorageTM_SELECT, storageTM);
		if (save_unlocalized_segments_to_TM.equalsIgnoreCase("o")) {
				selenium.check(TMProfile.Save_Unlocalized_Segments_to_TM);
			} else if (save_unlocalized_segments_to_TM.equalsIgnoreCase("x")) {
				selenium.uncheck(TMProfile.Save_Unlocalized_Segments_to_TM);
			}
		if (Save_unlocalized_segments_to_Page_TM.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.Save_unlocalized_segments_to_Page_TM);
		} else if (Save_unlocalized_segments_to_Page_TM.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.Save_unlocalized_segments_to_Page_TM);
		}
		if (!exclude_Item_Types.isEmpty()) {
			selenium.type(TMProfile.exclude_Item_Types, 
					selenium.getText(TMProfile.exclude_Item_Types)+exclude_Item_Types);
		}
		if (leverage_Localizables.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.leverage_Localizables);
		} else if (leverage_Localizables.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.leverage_Localizables);
		}
		if (leverage_Default_Exact_Incontext_Matches.equalsIgnoreCase("oxx")) {
			selenium.check(TMProfile.leverage_Default_Matches);
		} else if(leverage_Default_Exact_Incontext_Matches.equalsIgnoreCase("xox")) {
			selenium.uncheck(TMProfile.leverage_Exact_Matches_only);
		} else if(leverage_Default_Exact_Incontext_Matches.equalsIgnoreCase("xxo")) {
			selenium.uncheck(TMProfile.leverage_Incontext_Matches);
		}
		if (leverage_Approved_translations_from_selected_Reference_TMs.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.leverage_Approved_translations_from_selected_Reference_TMs);
		} else if (leverage_Approved_translations_from_selected_Reference_TMs.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.leverage_Approved_translations_from_selected_Reference_TMs);
		}
		if (leverage_In_progress_translations_from_the_Job.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.leverage_In_progress_translations_from_the_Job);
		} else if (leverage_In_progress_translations_from_the_Job.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.leverage_In_progress_translations_from_the_Job);
		}
		if (and_from_Jobs_that_write_to_the_Storage_TM.equalsIgnoreCase("o")) {
			if(selenium.isChecked(TMProfile.leverage_In_progress_translations_from_the_Job))
				selenium.check(TMProfile.and_from_Jobs_that_write_to_the_Storage_TM);
		} else if (and_from_Jobs_that_write_to_the_Storage_TM.equalsIgnoreCase("x")){
			if(selenium.isChecked(TMProfile.leverage_In_progress_translations_from_the_Job))
				selenium.uncheck(TMProfile.and_from_Jobs_that_write_to_the_Storage_TM);
		}
		if (and_from_Jobs_that_write_to_selected_Reference_TMs.equalsIgnoreCase("o")) {
			if(selenium.isChecked(TMProfile.leverage_In_progress_translations_from_the_Job))
				selenium.check(TMProfile.and_from_Jobs_that_write_to_selected_Reference_TMs);
		} else if (and_from_Jobs_that_write_to_selected_Reference_TMs.equalsIgnoreCase("x")){
			if(selenium.isChecked(TMProfile.leverage_In_progress_translations_from_the_Job))
				selenium.uncheck(TMProfile.and_from_Jobs_that_write_to_selected_Reference_TMs);
		}
		if (!reference_TMs.isEmpty()) {
			String[] ireference_TMs = reference_TMs.split(",");
			for(int i=0; i<ireference_TMs.length; i++)
	        {
				if (i==0) 
					selenium.select(TMProfile.reference_TMs, ireference_TMs[i]);
				else
					selenium.addSelection(TMProfile.reference_TMs, "label=" + ireference_TMs[i]);
	        }
		}
		if (type_sensitive_Leveraging.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.type_sensitive_Leveraging);
		} else if (type_sensitive_Leveraging.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.type_sensitive_Leveraging);
		}
		if (!penalty1.isEmpty()) {
			selenium.type(TMProfile.penalty1,penalty1);
		}
		if (case_sensitive_Leveraging.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.case_sensitive_Leveraging);
		} else if (case_sensitive_Leveraging.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.case_sensitive_Leveraging);
		}
		if (!penalty2.isEmpty()) {
			selenium.type(TMProfile.penalty2,penalty2);
		}
		if (whitespace_sensitive_Leveraging.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.whitespace_sensitive_Leveraging);
		} else if (whitespace_sensitive_Leveraging.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.whitespace_sensitive_Leveraging);
		}
		if (!penalty3.isEmpty()) {
			selenium.type(TMProfile.penalty3,penalty3);
		}
		if (code_sensitive_Leveraging.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.code_sensitive_Leveraging);
		} else if (code_sensitive_Leveraging.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.code_sensitive_Leveraging);
		}
		if (!penalty4.isEmpty()) {
			selenium.type(TMProfile.penalty4,penalty4);
		}
		if (reference_TM.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.reference_TM);
		} else if (reference_TM.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.reference_TM);
		}
		if (!penalty5.isEmpty()) {
			selenium.type(TMProfile.penalty5,penalty5);
		}
		if (reference_TM_Combo.isEmpty()) {
			selenium.removeAllSelections(TMProfile.reference_TM_Combo);
		} else {
			String[] ireference_TM = reference_TM_Combo.split(",");
			for(int i=0; i<ireference_TM.length; i++)
		        {
					if (i==0) 
						selenium.select(TMProfile.reference_TM_Combo, ireference_TM[i]);
					else
						selenium.addSelection(TMProfile.reference_TM_Combo, "label=" + ireference_TM[i]);
		        }
		}
		if (multilingual_Leveraging.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.multilingual_Leveraging);
		} else if (multilingual_Leveraging.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.multilingual_Leveraging);
		}
		if (auto_Repair_Placeholders.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.auto_Repair_Placeholders);
		} else if (auto_Repair_Placeholders.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.auto_Repair_Placeholders);
		}
		
		if (multiple_Exact_Matches.equalsIgnoreCase("oxx")) {
			selenium.click(TMProfile.multiple_Exact_Matches_Latest);
		} else if (multiple_Exact_Matches.equalsIgnoreCase("xox")){
			selenium.click(TMProfile.multiple_Exact_Matches_Oldest);
		}else if (multiple_Exact_Matches.equalsIgnoreCase("xxo")){
			selenium.click(TMProfile.multiple_Exact_Matches_Demoted);
		}
		if (!penalty6.isEmpty()) {
			selenium.type(TMProfile.penalty6,penalty2);
		}
		if (!leverage_Match_Threshold.isEmpty()) {
			selenium.type(TMProfile.leverage_Match_Threshold,leverage_Match_Threshold);
		}
		if (!number_of_Matches.isEmpty()) {
			selenium.type(TMProfile.number_of_Matches,number_of_Matches);
		}
		if (display_TM_Matches_by.equalsIgnoreCase("ox")) {
			selenium.click(TMProfile.display_TM_Matches_by_percentage);
		} else if (display_TM_Matches_by.equalsIgnoreCase("xo")){
			selenium.click(TMProfile.display_TM_Matches_by_procendence);
		}
		if (choose_Latest_Match.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.choose_Latest_Match);
		} else if (choose_Latest_Match.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.choose_Latest_Match);
		}
		if (type_sensitive_Leveraging2.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.type_sensitive_Leveraging2);
		} else if (type_sensitive_Leveraging2.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.type_sensitive_Leveraging2);
		}
		if (!penalty7.isEmpty()) {
			selenium.type(TMProfile.penalty7,penalty2);
		}
		if (no_Multiple_Exact_Matches.equalsIgnoreCase("o")) {
			selenium.check(TMProfile.no_Multiple_Exact_Matches);
		} else if (no_Multiple_Exact_Matches.equalsIgnoreCase("x")){
			selenium.uncheck(TMProfile.no_Multiple_Exact_Matches);
		}
		if (!penalty8.isEmpty()) {
			selenium.type(TMProfile.penalty8,penalty2);
		}

		selenium.click(TMProfile.SAVE_BUTTON);
		
		try {
			if(selenium.isElementPresent(TMProfile.confirmChangeDialog)) 
				selenium.click("css=#confirmChangeDialog > #div_button_arrangeTm > center > input[type=\"submit\"]");
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
		} catch (Exception e) {
				
			
		}
		
		
//		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}
}
