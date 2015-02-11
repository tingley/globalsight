package com.globalsight.selenium.functions;

import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyAccount;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName:MyAccountFuncs.java
 * Author:Jester
 * Methods:changeDefaultEditor()  
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-23  First Version  Jester
 */

public class MyAccountFuncs extends BasicFuncs{
	
	/*
	 * Change default editor used in the job processing. 
	 * 
	 * The value you provided must be "Inline" or "Popup".
	 * 
	 */
	
	public void changeDefaultEditor(Selenium selenium, String iEditor)
	{
		selenium.click(MainFrame.MyAccount_LINK);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(MyAccount.AccountOptions_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		if (iEditor.equals("Inline"))
		{
			selenium.click(MyAccount.InlineEditor_RADIO);
		}
		else if(iEditor.equals("Popup"))
		{
			selenium.click(MyAccount.PopupEditor_RADIO);
		}
		
		selenium.click(MyAccount.Done_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(MyAccount.Save_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}
}
