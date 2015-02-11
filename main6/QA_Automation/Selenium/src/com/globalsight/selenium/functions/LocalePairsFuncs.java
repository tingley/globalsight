package com.globalsight.selenium.functions;

import org.testng.Reporter;

import com.globalsight.selenium.pages.LocalePairs;
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
		selenium.getConfirmation();
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}
	/**
	 * Create the local pairs with the values provided.
	 */
	public void newLocalPairs(Selenium selenium, String iSourceLocale,
			String iTargetLocale) {
		selenium.click(LocalePairs.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.select(LocalePairs.SourceLocale_SELECT, iSourceLocale);
		selenium.select(LocalePairs.TargetLocale_SELECT, iTargetLocale);
		selenium.click(LocalePairs.Save_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}
}
