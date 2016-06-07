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
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();


	/*
	 * Create a new TM Profile, but first check if the TM is exists. If the TM
	 * profile has already exists. Click "Cancel" an back to the TM page.
	 */
	public String newTMProfile(Selenium selenium, String TMProfiles)
			throws Exception {
		
		String[] iTMProfiles = TMProfiles.split(",");
		String iTMProfileName = null;

		for (String iTMProfile : iTMProfiles) {
			String[] ivalue = iTMProfile.split("=");
			String iFieldName = ivalue[0].trim();
			String iFieldValue = ivalue[1].trim();

			if (iFieldName.equals("name")) {
				iTMProfileName = iFieldValue;
				selenium.type(TMProfile.TMP_SEARCH_CONTENT_TEXT, iFieldValue);
		    	selenium.keyDown(TMProfile.TMP_SEARCH_CONTENT_TEXT, "\\13");
		    	selenium.keyUp(TMProfile.TMP_SEARCH_CONTENT_TEXT, "\\13");
//		    	selenium.waitForFrameToLoad("css=table.listborder", "1000");

		   	 if (!(selenium.isElementPresent("link=" + iFieldValue))){
		   		selenium.click(TMProfile.NEW_VALUE_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				selenium.type(TMProfile.Name_TEXTFIELD, iFieldValue);
				
		   	 }
		   	 else break;
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
			if (selenium.isElementPresent(TMProfile.SAVE_BUTTON))
			{
				selenium.click(TMProfile.SAVE_BUTTON);
//			selenium.getAlert();
//			selenium.click(TMProfile.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			}
		} catch (Exception e) {

			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
		if (iTMProfileName != null) {
		Assert.assertEquals(this.isPresentInTable(selenium,
				TMProfile.TM_PROFILE_LIST_TABLE, iTMProfileName), true);}
		return iTMProfileName;
	}
	
	public void newTMProfile(Selenium selenium, String tm_Profile_Name,	
		String description,
		String sRX_Rule_Set,
		String storage_TM,
		String save_Unlocalized_Segments_to_TM,	
		String save_Localized_Segments_to_TM,
		String save_Wholly_Internal_Text_Segments_to_TM,
		String save_Exact_Match_Segments_to_TM,	
		String save_Approved_Segments_to_TM,
		String save_Unlocalized_Segments_to_Page_TM,
		String leverage_Localizables,
		String leverage_Exact_Matches_Only,	
		String apply_SID_ICE_Promotion_Only,	
		String apply_SID_Hash_ICE_Promotion,
		String apply_SID_Hash_Bracketed_ICE_Promotion,	
		String leverage_Approved_translations_from_selected_Reference_TM,	
		String leverage_In_progress_translations_from_the_Job,
		String and_from_Jobs_that_write_to_the_Storage_TM,
		String and_from_Jobs_that_write_to_selected_Reference_TM,	
		String stop_search_after_hitting_100_match,
		String reference_TM,
		String type_sensitive_Leveraging,	
		String case_sensitive_Leveraging,
		String whitespace_sensitive_Leveraging,
		String code_sensitive_Leveraging,	
		String reference_TM_2,
		String reference_TM_2_TM,	
		String multilingual_Leveraging,	
		String auto_Repair_Placeholders,	
		String get_Unique_from_Multiple_Exact_Matches,	
		String multiple_Exact_Matches,
		String leverage_Match_Threshold,
		String number_of_Matches,
		String display_TM_Matches_by,	
		String choose_Latest_Match,
		String type_sensitive_Leveraging_2,	
		String no_Multiple_Exact_Matches,
		String tU_Attributes_Match_Prioritising_Rules	
		        )
    {
		selenium.type(TMProfile.TMP_SEARCH_CONTENT_TEXT, tm_Profile_Name);
    	selenium.keyDown(TMProfile.TMP_SEARCH_CONTENT_TEXT, "\\13");
    	selenium.keyUp(TMProfile.TMP_SEARCH_CONTENT_TEXT, "\\13");
//    	selenium.waitForFrameToLoad("css=table.listborder", "1000");

   	 if (!(selenium.isElementPresent("link=" + tm_Profile_Name))){
    	clickAndWait(selenium, TMProfile.TMP_NEW_BUTTON);
        selenium.type(TMProfile.TMP_NAME_TEXT, tm_Profile_Name);
        selenium.type(TMProfile.TMP_DESCRIPTION_TEXT, description);
        
        if ((sRX_Rule_Set.isEmpty()) || (sRX_Rule_Set.equalsIgnoreCase("x")) || 
        		(sRX_Rule_Set.equalsIgnoreCase("o")) || (sRX_Rule_Set.equalsIgnoreCase("Default")))
        	selenium.select(TMProfile.TMP_SRX_RULE_SET_SELECT, TMProfile.TMP_SRX_RULE_SET_DEFAULT_TEXT);
        else 
        	selenium.select(TMProfile.TMP_SRX_RULE_SET_SELECT, sRX_Rule_Set);
			
        
        if ((!(storage_TM.isEmpty())) && (!(storage_TM.equalsIgnoreCase("x"))) 
        		&& (!(storage_TM.equalsIgnoreCase("o"))) && (!(storage_TM.equalsIgnoreCase("Default"))))
			selenium.select(TMProfile.TMP_STORAGE_TM_SELECT, storage_TM);
        
        if (save_Unlocalized_Segments_to_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_SAVE_UNLOCALIZED_SEGMENTS_TO_TM_CHECKBOX);
        else if (save_Unlocalized_Segments_to_TM.equalsIgnoreCase("no"))
        	selenium.uncheck(TMProfile.TMP_SAVE_UNLOCALIZED_SEGMENTS_TO_TM_CHECKBOX);
        
        if (save_Localized_Segments_to_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_SAVE_LOCALIZED_SEGMENTS_TO_TM_CHECKBOX);
        else if (save_Localized_Segments_to_TM.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_SAVE_LOCALIZED_SEGMENTS_TO_TM_CHECKBOX);
        
        if (save_Wholly_Internal_Text_Segments_to_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_SAVE_WHOLLY_INTERNAL_TEXT_SEGMENTS_TO_TM);
        else if (save_Wholly_Internal_Text_Segments_to_TM.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_SAVE_WHOLLY_INTERNAL_TEXT_SEGMENTS_TO_TM);
        
        if (save_Exact_Match_Segments_to_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_SAVE_EXACT_MATCH_SEGMENTS_TO_TM_CHECKBOX);
        else if (save_Exact_Match_Segments_to_TM.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_SAVE_EXACT_MATCH_SEGMENTS_TO_TM_CHECKBOX);
        
        if (save_Approved_Segments_to_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_SAVE_APPROVED_SEGMENTS_TO_TM_CHECKBOX);
        else if (save_Approved_Segments_to_TM.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_SAVE_APPROVED_SEGMENTS_TO_TM_CHECKBOX);
        
        if (save_Unlocalized_Segments_to_Page_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_SAVE_UNLOCALIZED_SEGMENTS_TO_PAGE_TM_CHECKBOX);
        else if (save_Unlocalized_Segments_to_Page_TM.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_SAVE_UNLOCALIZED_SEGMENTS_TO_PAGE_TM_CHECKBOX);      
        
        if (leverage_Localizables.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_LEVERAGE_LOCALIZABLE_CHECKBOX);
        else if (leverage_Localizables.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_LEVERAGE_LOCALIZABLE_CHECKBOX); 
        
        if (leverage_Exact_Matches_Only.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_LEVERAGE_EXACT_MATCHES_ONLY_RADIO_BUTTON);
        
        if (apply_SID_ICE_Promotion_Only.equalsIgnoreCase("yes"))
        {
         	selenium.check(TMProfile.TMP_LEVERAGE_IN_CONTEXT_MATCHES_RADIO_BUTTON);
          	selenium.check(TMProfile.TMP_APPLY_SID_ICE_PROMOTION_ONLY_RADIO_BUTTON);
        }
        
        if (apply_SID_Hash_ICE_Promotion.equalsIgnoreCase("yes"))
        {
         	selenium.check(TMProfile.TMP_LEVERAGE_IN_CONTEXT_MATCHES_RADIO_BUTTON);
          	selenium.check(TMProfile.TMP_APPLY_SID_HASH_ICE_PROMOTION_RADIO_BUTTON);
        }
        
        if (apply_SID_Hash_Bracketed_ICE_Promotion.equalsIgnoreCase("yes"))
        {
         	selenium.check(TMProfile.TMP_LEVERAGE_IN_CONTEXT_MATCHES_RADIO_BUTTON);
          	selenium.check(TMProfile.TMP_APPLY_SID_HASH_BRACKETED_ICE_PROMOTION_RADIO_BUTTON);
        }
        
        if (leverage_Approved_translations_from_selected_Reference_TM.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_LEVERAGE_APPROVED_TRANSLATIONS_FROM_SELECTED_REFERENCE_TM_CHECKBOX);
        else if (leverage_Approved_translations_from_selected_Reference_TM.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_LEVERAGE_APPROVED_TRANSLATIONS_FROM_SELECTED_REFERENCE_TM_CHECKBOX); 
        
        if (leverage_In_progress_translations_from_the_Job.equalsIgnoreCase("yes")){
        	selenium.check(TMProfile.TMP_LEVERAGE_IN_PROGRESS_TRANSLATION_FROM_THE_JOB_CHECKBOX);
        	if (and_from_Jobs_that_write_to_the_Storage_TM.equalsIgnoreCase("yes"))
        		selenium.check(TMProfile.TMP_AND_FROM_JOBS_THAT_WRITE_TO_THE_STORAGE_TM_CHECKBOX);
        	else if (and_from_Jobs_that_write_to_the_Storage_TM.equalsIgnoreCase("yes"))
        		selenium.uncheck(TMProfile.TMP_AND_FROM_JOBS_THAT_WRITE_TO_THE_STORAGE_TM_CHECKBOX);
        	
        	if (and_from_Jobs_that_write_to_selected_Reference_TM.equalsIgnoreCase("yes"))
        		selenium.check(TMProfile.TMP_AND_FROM_JOBS_THAT_WRITE_TO_SELECTED_REFERENCE_TM_CHECKBOX);
        	else if (and_from_Jobs_that_write_to_selected_Reference_TM.equalsIgnoreCase("no"))
        		selenium.uncheck(TMProfile.TMP_AND_FROM_JOBS_THAT_WRITE_TO_SELECTED_REFERENCE_TM_CHECKBOX);
        	}
        else if (leverage_In_progress_translations_from_the_Job.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_LEVERAGE_IN_PROGRESS_TRANSLATION_FROM_THE_JOB_CHECKBOX);    

        if (stop_search_after_hitting_100_match.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_STOP_SEARCH_AFTER_HITTING_100_MATCH_CHECKBOX);
        else if (stop_search_after_hitting_100_match.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_STOP_SEARCH_AFTER_HITTING_100_MATCH_CHECKBOX); 
        
        String[] iReference_TM = reference_TM.split(",");
    	    	for (int i = 0; i < iReference_TM.length; i++) {
    		selenium.addSelection(TMProfile.TMP_LEVERAGE_OPTIONS_FOR_INITIAL_IMPORT_COMBOBOX, iReference_TM[i]);
    	}
    	
    	if (type_sensitive_Leveraging.equalsIgnoreCase("no"))
    	    		selenium.uncheck(TMProfile.TMP_TYPE_SENSITIVE_LEVERAGING_CHECKBOX);
    	else if ((!(type_sensitive_Leveraging.isEmpty())) && (!(type_sensitive_Leveraging.equalsIgnoreCase("x")))&& 
    			(!(type_sensitive_Leveraging.equalsIgnoreCase("o"))) && (!(type_sensitive_Leveraging.equalsIgnoreCase("Default"))))
    		{
    			selenium.check(TMProfile.TMP_TYPE_SENSITIVE_LEVERAGING_CHECKBOX);
    			selenium.type(TMProfile.TMP_TYPE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT, type_sensitive_Leveraging);
    		}
    	
    	if (case_sensitive_Leveraging.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_CASE_SENSITIVE_LEVERAGING_CHECKBOX);
    	else if ((!(case_sensitive_Leveraging.isEmpty())) && (!(case_sensitive_Leveraging.equalsIgnoreCase("x")))&& 
    			(!(case_sensitive_Leveraging.equalsIgnoreCase("o"))) && (!(case_sensitive_Leveraging.equalsIgnoreCase("Default"))))
		{
			selenium.check(TMProfile.TMP_CASE_SENSITIVE_LEVERAGING_CHECKBOX);
			selenium.type(TMProfile.TMP_CASE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT, case_sensitive_Leveraging);
		}

    	if (whitespace_sensitive_Leveraging.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_WHITESPACE_SENSITIVE_LEVERAGING_CHECKBOX);
    	else if ((!(whitespace_sensitive_Leveraging.isEmpty())) && (!(whitespace_sensitive_Leveraging.equalsIgnoreCase("x")))&& 
    			(!(whitespace_sensitive_Leveraging.equalsIgnoreCase("o"))) && (!(whitespace_sensitive_Leveraging.equalsIgnoreCase("Default"))))
		{
			selenium.check(TMProfile.TMP_WHITESPACE_SENSITIVE_LEVERAGING_CHECKBOX);
			selenium.type(TMProfile.TMP_WHITESPACE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT, whitespace_sensitive_Leveraging);
		}
    	
    	if (code_sensitive_Leveraging.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_CODE_SENSITIVE_LEVERAGING_CHECKBOX);
    	else if ((!(code_sensitive_Leveraging.isEmpty())) && (!(code_sensitive_Leveraging.equalsIgnoreCase("x")))&& 
    			(!(code_sensitive_Leveraging.equalsIgnoreCase("o"))) && (!(code_sensitive_Leveraging.equalsIgnoreCase("Default"))))
		{
			selenium.check(TMProfile.TMP_CODE_SENSITIVE_LEVERAGING_CHECKBOX);
			selenium.type(TMProfile.TMP_CODE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT, code_sensitive_Leveraging);
		}
    	
    	if (reference_TM_2.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_REFERENCT_TM_2_CHECKBOX);
    	else if ((!(reference_TM_2.isEmpty())) && (!(reference_TM_2.equalsIgnoreCase("x")))&& 
    			(!(reference_TM_2.equalsIgnoreCase("o"))) && (!(reference_TM_2.equalsIgnoreCase("Default"))))
		{
			selenium.check(TMProfile.TMP_REFERENCT_TM_2_CHECKBOX);
			selenium.type(TMProfile.TMP_REFERENCT_TM_2_PERCENTAGE_TEXT, reference_TM_2);
		}
    	
    	if ((!(reference_TM_2_TM.isEmpty())) && (!(reference_TM_2_TM.equalsIgnoreCase("x")))&& 
    			(!(reference_TM_2_TM.equalsIgnoreCase("o"))) && (!(reference_TM_2_TM.equalsIgnoreCase("Default"))))
    	{
    		String[] iReference_TM_2_TM = reference_TM_2_TM.split(",");
    		for (int j = 0; j < iReference_TM_2_TM.length; j++) 
    		{
    			selenium.addSelection(TMProfile.TMP_REFERENCT_TM_2_COMBO_BOX, iReference_TM_2_TM[j]);
    		}
    	}
    	
    	if (multilingual_Leveraging.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_MULTILINGUAL_LEVERAGING_CHECKBOX);
        else if (multilingual_Leveraging.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_MULTILINGUAL_LEVERAGING_CHECKBOX); 
    	
    	if (auto_Repair_Placeholders.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_AUTO_REPAIR_PLACEHOLDERS_CHECKBOX);
        else if (auto_Repair_Placeholders.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_AUTO_REPAIR_PLACEHOLDERS_CHECKBOX); 
    	
    	if (get_Unique_from_Multiple_Exact_Matches.equalsIgnoreCase("yes"))
          	selenium.check(TMProfile.TMP_GET_UNIQUE_FROM_MULTIPLE_EXACT_MATCHES_CHECKBOX);
        else if (get_Unique_from_Multiple_Exact_Matches.equalsIgnoreCase("no"))
          	selenium.uncheck(TMProfile.TMP_GET_UNIQUE_FROM_MULTIPLE_EXACT_MATCHES_CHECKBOX); 
    	
    	if ((!(multiple_Exact_Matches.isEmpty())) && (!(multiple_Exact_Matches.equalsIgnoreCase("x")))&& 
    			(!(multiple_Exact_Matches.equalsIgnoreCase("o"))) && (!(multiple_Exact_Matches.equalsIgnoreCase("Default"))))
    	{
    		String[] imultiple_Exact_Matches = multiple_Exact_Matches.split(",");
    			if (imultiple_Exact_Matches[0].equalsIgnoreCase("latest"))
    	          	selenium.check(TMProfile.TMP_MULTIPLE_EXACT_MATCHES_RADIO_BUTTON_LASTEST);
    	        else if (imultiple_Exact_Matches[0].equalsIgnoreCase("oldest"))
    	          	selenium.uncheck(TMProfile.TMP_MULTIPLE_EXACT_MATCHES_RADIO_BUTTON_OLDEST); 
    	        else if (imultiple_Exact_Matches[0].equalsIgnoreCase("demoted"))
    	        {
    	        	selenium.uncheck(TMProfile.TMP_MULTIPLE_EXACT_MATCHES_RADIO_BUTTON_DEMOTED);
    	        	selenium.type(TMProfile.TMP_MULTIPLE_EXACT_MATCHES_PENALTY_TEXT,imultiple_Exact_Matches[1]);
    	        }
    			
    	}
    	
    	if ((!(leverage_Match_Threshold.isEmpty())) && (!(leverage_Match_Threshold.equalsIgnoreCase("x")))&& 
    			(!(leverage_Match_Threshold.equalsIgnoreCase("o"))) && (!(leverage_Match_Threshold.equalsIgnoreCase("Default"))))
		{
			selenium.type(TMProfile.TMP_LEVERAGE_MATCH_THRESHOLD_TEXT, leverage_Match_Threshold);
		}
    	
    	if ((!(number_of_Matches.isEmpty())) && (!(number_of_Matches.equalsIgnoreCase("x")))&& 
    			(!(number_of_Matches.equalsIgnoreCase("o"))) && (!(number_of_Matches.equalsIgnoreCase("Default"))))
		{
			selenium.type(TMProfile.TMP_NUMBER_OF_MATCHES_TEXT, number_of_Matches);
		}
    	
    	if (display_TM_Matches_by.equalsIgnoreCase("Matching_percentage"))
    		selenium.click(TMProfile.TMP_DISPLAY_TM_MATCHES_BY_RADIO_BUTTON_MATCHING_PERCENTAGE);
    	else if (display_TM_Matches_by.equalsIgnoreCase("TM_Precedence"))
    		selenium.click(TMProfile.TMP_DISPLAY_TM_MATCHES_BY_RADIO_BUTTON_TM_PRECEDENCE);
    	
    	if (choose_Latest_Match.equalsIgnoreCase("yes"))
    		selenium.check(TMProfile.TMP_DISPLAY_TM_CHOOSE_LATEST_MATCH);
    	else if (choose_Latest_Match.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_DISPLAY_TM_CHOOSE_LATEST_MATCH);
		
    	if (type_sensitive_Leveraging_2.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_TYPE_SENSITIVE_LEVERAGING_CHECKBOX_2);
    	else if ((!(type_sensitive_Leveraging_2.isEmpty())) && (!(type_sensitive_Leveraging_2.equalsIgnoreCase("x")))&& 
		(!(type_sensitive_Leveraging_2.equalsIgnoreCase("o"))) && (!(type_sensitive_Leveraging_2.equalsIgnoreCase("Default"))))
		{
			selenium.check(TMProfile.TMP_TYPE_SENSITIVE_LEVERAGING_CHECKBOX_2);
			selenium.type(TMProfile.TMP_TYPE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT_2, type_sensitive_Leveraging_2);
		}
    	
    	if (no_Multiple_Exact_Matches.equalsIgnoreCase("no"))
    		selenium.uncheck(TMProfile.TMP_NO_MULTIPLE_EXACT_MATCHES_CHECKBOX);
    	else if ((!(no_Multiple_Exact_Matches.isEmpty())) && (!(no_Multiple_Exact_Matches.equalsIgnoreCase("x")))&& 
		(!(no_Multiple_Exact_Matches.equalsIgnoreCase("o"))) && (!(no_Multiple_Exact_Matches.equalsIgnoreCase("Default"))))
		{
			selenium.check(TMProfile.TMP_NO_MULTIPLE_EXACT_MATCHES_CHECKBOX);
			selenium.type(TMProfile.TMP_NO_MULTIPLE_EXACT_MATCHES_CHECKBOX, no_Multiple_Exact_Matches);
		}
    	
    	if ((!(tU_Attributes_Match_Prioritising_Rules.isEmpty())) && (!(tU_Attributes_Match_Prioritising_Rules.equalsIgnoreCase("x")))&& 
    			(!(tU_Attributes_Match_Prioritising_Rules.equalsIgnoreCase("o"))) && (!(tU_Attributes_Match_Prioritising_Rules.equalsIgnoreCase("Default"))))
    	{
    		String[] itU_Attributes_Match_Prioritising_Rules = tU_Attributes_Match_Prioritising_Rules.split(",");
    		for (int k = 0; k < itU_Attributes_Match_Prioritising_Rules.length; k++) 
    		{
    			String[] k_itU_Attributes_Match_Prioritising_Rules = itU_Attributes_Match_Prioritising_Rules[k].split(";");
    			for (int m = 0; m < k_itU_Attributes_Match_Prioritising_Rules.length; m++)
    			{
    				String[] n_k_itU_Attributes_Match_Prioritising_Rules = k_itU_Attributes_Match_Prioritising_Rules[m].split("=");
    				switch (n_k_itU_Attributes_Match_Prioritising_Rules[0])
					{
						case "AndOr":
						{
							selenium.select(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_AND_OR_SELECT,
									n_k_itU_Attributes_Match_Prioritising_Rules[1]);
							break;
						}
						case "Attribute_Internal_name":
						{
							selenium.select(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_ATTRIBUTE_INTERNAL_NAME_AND_OR_SELECT,
									n_k_itU_Attributes_Match_Prioritising_Rules[1]);
							break;
						}
						case "Operator":
						{
							selenium.select(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_OPERATOR_SELECT,
									n_k_itU_Attributes_Match_Prioritising_Rules[1]);
							break;
						}
						
						case "Value":
						{
							if (!(n_k_itU_Attributes_Match_Prioritising_Rules[1].equalsIgnoreCase("Value_from_Job_Attribute_of_same_name")))
							selenium.select(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_VALUE_TYPE_SELECT,
									"Input Value");
							selenium.type(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_VALUE_TEXT,
									n_k_itU_Attributes_Match_Prioritising_Rules[1]);
							break;
						}	
						
						default:
							break;
					}
    				if (n_k_itU_Attributes_Match_Prioritising_Rules[0].equalsIgnoreCase("AndOr"))
    					selenium.click(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_AND_OR_SELECT);
    			}
    			selenium.click(TMProfile.TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_ADD_BUTTON);
    		}
    			
    		
    	}
    	
    	selenium.click(TMProfile.TMP_SAVE_BUTTON);
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
   }
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
			    selenium.type(TMProfile.MT_CLINTID, iFieldValue);
			    
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
		        clickAndWait(selenium,"link=" + iFieldValue);
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
