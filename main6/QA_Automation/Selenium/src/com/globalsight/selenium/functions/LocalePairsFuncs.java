package com.globalsight.selenium.functions;

import org.testng.Reporter;

import com.globalsight.selenium.pages.LocalePairs;
import com.globalsight.selenium.pages.TMProfile;
import com.thoughtworks.selenium.Selenium;

public class LocalePairsFuncs extends BasicFuncs {
	/**
	 * Author Totti
	 */
	public void localPairsRemove(Selenium selenium, String iSourceLocale,
			String iTargetLocale) throws Exception 
	{
		boolean result;

		result = super.selectRadioForRemove(selenium,
				LocalePairs.LocalPairs_TABLE, iSourceLocale, iTargetLocale);
		if (result == true) {
			Reporter.getCurrentTestResult();
		} else {
			System.out.println("error");
		}
		selenium.click(LocalePairs.Remove);
		if (selenium.isConfirmationPresent()) {
		    selenium.getConfirmation();
		    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
	}
	/**
	 * Create the local pairs with the values provided.
	 */
	public boolean newLocalPairs(Selenium selenium, String iSourceLocale, String iTargetLocale) {
		selenium.select(LocalePairs.LocalPairs_Num_Of_Page_Size, "label=All");
		selenium.waitForPageToLoad("30000");
		selenium.type(LocalePairs.LOCALPAIRS_SEARCH_SOURSE_TEXT, iSourceLocale);
    	selenium.keyDown(LocalePairs.LOCALPAIRS_SEARCH_SOURSE_TEXT, "\\13");
    	selenium.keyUp(LocalePairs.LOCALPAIRS_SEARCH_SOURSE_TEXT, "\\13");
    	selenium.type(LocalePairs.LOCALPAIRS_SEARCH_TARGET_TEXT, iTargetLocale);
    	selenium.keyDown(LocalePairs.LOCALPAIRS_SEARCH_TARGET_TEXT, "\\13");
    	selenium.keyUp(LocalePairs.LOCALPAIRS_SEARCH_TARGET_TEXT, "\\13");
    	
//		int localeNumber = Integer.parseInt(selenium.getText("//div[@id='contentLayer']/form/table[2]/tbody/tr/td/div/b[2]"));
//		for (int i = 1; i<localeNumber; i++){
//			if (selenium.getText(
//					LocalePairs.LocalPairs_TABLE_2+"/tr[" + i +"]/td[2]").equals(iSourceLocale)){
//				if (selenium.getText(LocalePairs.LocalPairs_TABLE_2+"/tr[" + i +"]/td[3]").equals(iTargetLocale))
//						return;
//			}
//		}
		
    	if (selenium.isElementPresent(LocalePairs.LocalPairs_FIRST_SOURCE_TABLE)){
    	if (selenium.getText(
				LocalePairs.LocalPairs_FIRST_SOURCE_TABLE).equals(iSourceLocale)){
			if (selenium.getText(LocalePairs.LocalPairs_FIRST_TARGET_TABLE).equals(iTargetLocale))
					return false;
		}
    	}
		selenium.click(LocalePairs.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.select(LocalePairs.SourceLocale_SELECT, iSourceLocale);
		selenium.select(LocalePairs.TargetLocale_SELECT, iTargetLocale);
		selenium.click(LocalePairs.Save_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		return true;
	}
}
