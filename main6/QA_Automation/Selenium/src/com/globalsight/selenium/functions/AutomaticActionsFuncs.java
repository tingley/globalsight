package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.AutomaticActions;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: AutomaticActionsFuncs.java
 * Author:Jester
 * Methods: AutomaticActionsNew() 
 * 
 */

public class AutomaticActionsFuncs extends BasicFuncs {
	/**
	 * Create a new automatic actions.
	 */
	public void newAutomaticAction(Selenium selenium, String iActionProfiles)
			throws Exception {
		selenium.click(AutomaticActions.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		
		String[] iActionProfile = iActionProfiles.split(",");
		String iActionName = null;

		for (String iActions : iActionProfile) {
			try {
				String[] ivalue = iActions.split("=");
				String iFieldName = ivalue[0].trim();
				String iFieldValue = ivalue[1].trim();

				if (iFieldName.equals("name")) {
					selenium.type(AutomaticActions.Name_TEXT_FIELD, iFieldValue);
					iActionName = iFieldValue;
				} else if (iFieldName.equals("emailaddress")) {
					selenium.type(AutomaticActions.EamilAddress_TEXT_FIELD,
							iFieldValue);
				} else if (iFieldName.equals("description")) {
					selenium.type(AutomaticActions.Description_TEXT_FIELD,
							iFieldValue);
				}
			} catch (Exception e) {
				Reporter.log(e.getMessage());
			}
		}

		selenium.click(AutomaticActions.Save_BUTTON);
		try {
			selenium.getAlert();
			selenium.click(AutomaticActions.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		if (iActionName != null) {
			Assert.assertEquals(this.isPresentInTable(selenium,
					AutomaticActions.AutomaticActions_TABLE, iActionName), true);
		}
	}
	public void removeAutomaticAction(Selenium selenium, String iActionName)
			throws Exception {

		boolean result = selectRadioButtonFromTable(selenium,
					AutomaticActions.AutomaticActions_TABLE, iActionName);
			if(!result){
			    Reporter.log("Cannot find the proper AutomaticAction to remove!");
			    return;
			}
			else
			{
				selenium.click(AutomaticActions.Remove_BUTTON);
				if (selenium.isConfirmationPresent()) {
					selenium.getConfirmation();
				}
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
			}
			Assert.assertEquals(this.isPresentInTable(selenium,
                    AutomaticActions.AutomaticActions_TABLE, iActionName), false);
		}
	

	public void editAutomaticAction(Selenium selenium, String iActionName, String iNewName)
			throws Exception {

		boolean result = selectRadioButtonFromTable(selenium,
					AutomaticActions.AutomaticActions_TABLE, iActionName);
			if (!result) 
			{Reporter.log("Cannot find proper AutomaticAction to edit!");
			 return;
			}
			else
			{			
				selenium.click(AutomaticActions.Edit_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
			    selenium.type(AutomaticActions.Name_TEXT_FIELD, iNewName);
						
				selenium.type(AutomaticActions.EamilAddress_TEXT_FIELD,	"@");
				selenium.type(AutomaticActions.Description_TEXT_FIELD, "t");
			}
				selenium.click(AutomaticActions.Save_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				if (selenium.isConfirmationPresent())
				{
					selenium.getConfirmation();
				}
		  Assert.assertEquals(this.isPresentInTable(selenium,
	                    AutomaticActions.AutomaticActions_TABLE, iNewName), true);
	 } 
		
	}

