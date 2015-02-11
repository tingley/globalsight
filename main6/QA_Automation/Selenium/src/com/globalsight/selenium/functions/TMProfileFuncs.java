package com.globalsight.selenium.functions;

import org.testng.Assert;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.properties.ConfigUtil;
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
		selenium.click(TMProfile.New_BUTTON);
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
			selenium.click(TMProfile.Save_BUTTON);
//			selenium.getAlert();
//			selenium.click(TMProfile.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {

			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		Assert.assertEquals(this.isPresentInTable(selenium,
				TMProfile.TMProfiles_TABLE, iTMProfileName), true);
		return iTMProfileName;
	}
	
	public void createMTOptions(Selenium selenium, String MTOptionsProfiles, String className)
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
					iButton = TMProfile.MTOptions_BUTTON;
				}
				else if (iFieldValue.equals("TDA")){
					iButton = TMProfile.TDAOptions_BUTTON;
				}
				else {
					Assert.assertTrue(false, "mt_tda should be MT or TDA");
				}
				 
			} else if (iFieldName.equals("tmname")) {
				boolean selected = selectRadioButtonFromTable(selenium, TMProfile.TMProfiles_TABLE, iFieldValue);
		        if (!selected)
		        {
					Assert.assertTrue(false, "Cannot find a proper FileProfile name to edit.");
		        }
		        clickAndWait(selenium,iButton);
			} else if (iFieldName.equals("mtengine")) {
				selenium.select(TMProfile.MTEngine_SELECT, "label=" + iFieldValue);
				mtEnginName = iFieldValue;
			} else if (iFieldName.equals("overwrride_non_exact")){
				if (iFieldValue.equals("true")){
					if(selenium.isChecked(TMProfile.MTOverride_Non_Exact_CHECKBOX)){
						overrideNonExact = true;
					} else {
						selenium.click(TMProfile.MTOverride_Non_Exact_CHECKBOX);
						overrideNonExact = true;
					}
				} else {
					if(selenium.isChecked(TMProfile.MTOverride_Non_Exact_CHECKBOX)){
						selenium.click(TMProfile.MTOverride_Non_Exact_CHECKBOX);
						overrideNonExact = false;
					} else {
						overrideNonExact = false;
					}
				}
			} else if (iFieldName.equals("mt_leveraging")) {
				if(overrideNonExact){
					if(iFieldValue.equals("true")){
						selenium.click(TMProfile.MT_Leveraging_Radio);
						leveragingAvailable = true;
					} else {
						selenium.click(TMProfile.MTAuto_Commit_To_Tm_RADIO);
						leveragingAvailable = false;
					}
				}
			} else if (iFieldName.equals("penalty")) {
				if (leveragingAvailable){
					selenium.type(TMProfile.MT_Leveraging_Penalty_T, iFieldValue);
				}
			} else if (iFieldName.equals("show_in_eidtor")){
				if(iFieldValue.equals("true")){
					selenium.check(TMProfile.MTShow_In_Editor_CHECKBOX);	
				} else {
					selenium.uncheck(TMProfile.MTShow_In_Editor_CHECKBOX);
				}
			} else if (iFieldName.equals("tda_URL"))
			{
				if (!(selenium.isChecked(TMProfile.TDA_Enable_CHECKBOX))){
					selenium.click(TMProfile.TDA_Enable_CHECKBOX);
				}
				selenium.type(TMProfile.TDA_HostName_TEXTFEILD, iFieldValue);
				mtEnginName = "TDA";
			} else if (iFieldName.equals("tda_username")){
				selenium.type(TMProfile.TDA_UserName_TEXTFEILD, iFieldValue);
			} else if (iFieldName.equals("tda_password")){
				selenium.type(TMProfile.TDA_Password_TEXTFEILD, iFieldValue);
			}
			
		}
		if (mtEnginName.equals("Asia_Online")){
			selenium.type(TMProfile.AO_URL_TEXTFIELD, ConfigUtil.getDataInCase(className, "AoMtUrl"));
			selenium.type(TMProfile.AO_Port_TEXTFIELD, ConfigUtil.getDataInCase(className, "AoMtPort"));
			selenium.type(TMProfile.AO_UserName_TEXTFIELD, ConfigUtil.getDataInCase(className, "AoMtUsername"));
			selenium.type(TMProfile.AO_Password_TEXTFIELD, ConfigUtil.getDataInCase(className, "AoMtPassword"));
			selenium.type(TMProfile.AO_Account_Number_TEXTFIELD, ConfigUtil.getDataInCase(className, "AoMtAccountNumber"));
			selenium.click(TMProfile.AO_Next_BUTTON);
			if (selenium.isElementPresent(TMProfile.AO_ErrorMessage_Table)){
					Assert.assertTrue(false, "Incorrect setting from Asia Online.");
		            return;
		        	
			} else if (selenium.isAlertPresent()){
				selenium.getAlert();
				Assert.assertTrue(false, "Incorrect setting form Asia Online.");
			}
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.select(TMProfile.AO_Domain_Combination,  "label=" + ConfigUtil.getDataInCase(className, "AODomain_Combination"));
		} 
		
		selenium.click(TMProfile.MT_TDA_Save_BUTTON);
		Thread.sleep(30000);
		
		try {
			selenium.getAlert();
			selenium.click(TMProfile.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {

			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
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
				boolean selected = selectRadioButtonFromTable(selenium, TMProfile.TMProfiles_TABLE, iFieldValue);
		        if (!selected)
		        {
					Assert.assertTrue(false, "Cannot find a proper currency to edit.");
		        }
		        clickAndWait(selenium,TMProfile.Edit_BUTTON);
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
			} else if(iFieldName.equals("description")) {
			    selenium.type(TMProfile.Description, 
			    		iFieldValue);
			} else if(iFieldName.equals("threshold")) {
			    selenium.type(TMProfile.Threshold_TEXTFEILD, 
			    		iFieldValue);
			}
			
		}
		
		selenium.click(TMProfile.Save_BUTTON);
		
		try {
			selenium.getAlert();
			selenium.click(TMProfile.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
		
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		}
	
}
