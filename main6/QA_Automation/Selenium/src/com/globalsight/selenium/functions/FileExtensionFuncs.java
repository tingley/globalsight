package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FileExtensions;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: FileExtensionFuncs.java
 * Author:Jester
 * Methods:FileExtensionNew()  
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

public class FileExtensionFuncs extends BasicFuncs {
	
	private static final String MAIN_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";
	// Create a new File Extension.
	public void newFileExtension(Selenium selenium, String iFileExtensionName) {
		selenium.click(FileExtensions.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.type(FileExtensions.Extension_TEXT_FIELD, iFileExtensionName);
		selenium.click(FileExtensions.Save_BUTTON);

		if (selenium.isElementPresent(FileExtensions.Extension_TEXT_FIELD)) {
			Assert.assertEquals(
					"The File Extension you have entered already exists.",
					selenium.getAlert());
			Reporter.log("The File Extension " + iFileExtensionName
					+ " has ready exists!");
			selenium.click(FileExtensions.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
	}
	public void removeFileExtension(Selenium selenium, String iFileExtensionName)throws Exception
	{
		boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, iFileExtensionName);
        if (!selected)
        {
            Reporter.log("Cannot find a proper file extension to remove.");
            return;
        }
    	selenium.click(FileExtensions.Remove_BUTTON);
    	if (selenium.isConfirmationPresent()) {
    	    boolean actual = selenium.getConfirmation().equals("Do you really want to remove this File Extension from the system?");
    	    Assert.assertEquals(actual,true);
    	}
        
    	selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, iFileExtensionName);
        if (!selected)
        {
        	Reporter.log("The file extension was removed successfully.");
        	return;
        }
	}
}
