package com.globalsight.selenium.functions;

/*
 * FileName: XMLRulesFuncs.java
 * Author:Jester
 * Methods:XMLRuleNew()  
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.Rates;
import com.globalsight.selenium.pages.XMLRules;
import com.thoughtworks.selenium.Selenium;

public class XMLRulesFuncs extends BasicFuncs {

	/*
	 * Create a new XML Rule
	 */
	public void newXMLRule(Selenium selenium, String iRuleName, String iRule)
			throws Exception {
		selenium.click(XMLRules.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.type(XMLRules.Name_TEXT_FIELD, iRuleName);
		selenium.type(XMLRules.Rules_TEXT_FIELD, iRule);

		selenium.click(XMLRules.Validate_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.chooseOkOnNextConfirmation(); // To handle a popup window in
												// selenium, we can use this
												// command.

		if (selenium.isEditable(XMLRules.Save_BUTTON)) {
			selenium.click(XMLRules.Save_BUTTON);
		}
		if (selenium.isAlertPresent()) {
			selenium.getAlert();
			selenium.click(XMLRules.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		// Check if the Rule has been exists.
		Assert.assertEquals(
				isPresentInTable(selenium, XMLRules.XMLRules_TABLE, iRuleName),
				true);
	}

	public void editXMLRule(Selenium selenium, String iRuleName,
			String typeStr, String iRule) throws Exception {
		boolean selected = selectRadioButtonFromTable(selenium,
				XMLRules.XMLRules_TABLE, iRuleName);
		if (!selected) {
			Reporter.log("Cannot find the RuleName.");
			return;
		}
		try {
			selenium.click(XMLRules.Edit_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			selenium.type(XMLRules.Description_TEXT_FIELD, typeStr);
			selenium.type(XMLRules.Rules_TEXT_FIELD, iRule);
			selenium.click(XMLRules.Validate_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			selenium.chooseOkOnNextConfirmation();
			if (selenium.isEditable(XMLRules.Save_BUTTON)) {
				selenium.click(XMLRules.Save_BUTTON);
			}
			if (selenium.isAlertPresent()) {
				selenium.getAlert();
				selenium.click(XMLRules.Cancel_BUTTON);
	            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			}
		} catch (Exception e) {
			Reporter.log(e.getMessage());
		}

		// Check if the rule has been edited.
		Assert.assertEquals(this.getColumnText(selenium,
				XMLRules.XMLRules_TABLE, iRuleName, 3), typeStr);
	}
	
	public void removeRule(Selenium selenium, String iRuleName) throws Exception{
		boolean selected = selectRadioButtonFromTable(selenium,
				XMLRules.XMLRules_TABLE, iRuleName);
		if (!selected) {
			Reporter.log("Cannot find the RuleName.");
			return;
		}
		try{
			selenium.click(XMLRules.Remove_BUTTON);
			selenium.getConfirmation();
			Assert.assertEquals(this.isPresentInTable(selenium,
					XMLRules.XMLRules_TABLE, iRuleName), false);
			}catch (Exception e) {
				Reporter.log(e.getMessage());
			}
		
		
	}

}
