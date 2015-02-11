package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.ActivityType;
import com.thoughtworks.selenium.Selenium;

public class ActivityTypeFuncs extends BasicFuncs {
	/*
	 * Create the local pairs with the values provided.
	 */
	public void newActivityType(Selenium selenium, String iActivityName) throws Exception {
	
		String[] str = iActivityName.split(",");
		for(String strname: str)
		{
		    selenium.click(ActivityType.New_BUTTON);
	        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.type(ActivityType.Name_TEXT_FIELD, strname);
		selenium.type(ActivityType.Description_TEXT_FIELD, strname);
		if(strname.equals("Translate"))
		    selenium.click(ActivityType.Translate_RADIO);
		else if(strname.equals("ReviewEditable"))
		    selenium.click(ActivityType.ReviewEditable_RADIO);
		else if(strname.equals("GSEditionActions"))
		    selenium.click(ActivityType.GSEditionActions_RADIO);
		else if(strname.equals("ReviewOnly"))
		    selenium.click(ActivityType.ReviewOnly_RADIO);
		else if (strname.equals("AutomaticActions"))
		    selenium.click(ActivityType.AutomaticActions_RADIO);
		
		selenium.click(ActivityType.Save_BUTTON);
		try {
			selenium.getAlert();
			selenium.click(ActivityType.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		Assert.assertTrue(this.isPresentInTable(selenium,
				ActivityType.Activity_TABLE, strname));
		}
	}

	//author Shenyang 2011-6-22
	public void ActivityTypeEdit(Selenium selenium, String iActivtiyName, String typeStr, String newActivityType) throws Exception {
		
		boolean selected = selectRadioButtonFromTable(selenium, ActivityType.Activity_TABLE, iActivtiyName);
        if (!selected)
        {
            Reporter.log("Cannot find a proper ActivityType to edit.");
            return;
        }
    
        try {
        	selenium.click(ActivityType.Edit_BUTTON);
	        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        	selenium.type(ActivityType.Description_TEXT_FIELD, typeStr);
	        selenium.click(newActivityType);
	        selenium.click(ActivityType.Save_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
			Reporter.log(e.getMessage());
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
        Assert.assertEquals(this.getColumnText(selenium,
				ActivityType.Activity_TABLE, iActivtiyName, 3), typeStr);
	}
	
	//author Shenyang 2011-6-22
	public void ActivityTypeRemove(Selenium selenium, String iActivityName) throws Exception {
	    String[] rname = iActivityName.split(",");
		for(String activity: rname)
		{
		 boolean selected = selectRadioButtonFromTable(selenium, ActivityType.Activity_TABLE, activity);
	        if (!selected)
	        {
	            Reporter.log("Cannot find a proper ActivityType to edit.");
	            return;
	        }

	        try {
	        	selenium.click(ActivityType.Remove_BUTTON);
	        	Assert.assertEquals((selenium.getConfirmation().matches("^Are you sure you want to remove this Activity[\\s\\S]$")),true);
		        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			} catch (Exception e) {
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			}

	        Assert.assertEquals(this.isPresentInTable(selenium,
					ActivityType.Activity_TABLE, activity), false);
		}
	}
}