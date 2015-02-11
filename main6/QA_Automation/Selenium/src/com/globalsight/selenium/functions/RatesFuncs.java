package com.globalsight.selenium.functions;

/*
 * FileName: RatesFuncs.java
 * Author:Jester
 * Methods:newRate()  
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.Rates;
import com.globalsight.selenium.pages.Users;
import com.thoughtworks.selenium.Selenium;

public class RatesFuncs extends BasicFuncs {

	// Create Rate.
	
	public String newRate(Selenium selenium, String RateProfiles)
			throws Exception {
		selenium.click(Rates.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		String[] iRateProfiles = RateProfiles.split(",");
		String iRateName = null;
		String rateType = new String();
		String suffixName = new String();
		
		for (String iRateProfile : iRateProfiles) {
			try {
				String[] ivalue = iRateProfile.split("=");
				String iFieldName = ivalue[0].trim();
				String iFieldValue = ivalue[1].trim();
				if (iFieldName.equals("name")) {
					selenium.type(Rates.Name_TEXT_FIELD, iFieldValue);
					iRateName = iFieldValue;
				} else if (iFieldName.equals("activitytype")) {
					selenium.select(Rates.ActivityType_SELECT, "label="
							+ iFieldValue);
				}
				else if (iFieldName.equals("suffixname")) {
				    suffixName = iFieldValue;
				}
				else if (iFieldName.equals("localepair")) {
				    String[] array = iFieldValue.split(";");
				    for (String pair : array) {
				        selenium.addSelection(Rates.LocalePair_SELECTION, "label="
	                            + pair);
				    }
				}
				else if (iFieldName.equals("ratetype")) {
					selenium.select(Rates.RateType_SELECT, "label="
							+ iFieldValue);
					rateType = iFieldValue;
				}
				else {
				    selenium.type(iFieldName, iFieldValue);
				}

			} catch (Exception e) {
				Reporter.log(e.getMessage());
			}
		}
		
		if(rateType.equals("Word Count by %")) 
        {
            selenium.click(Rates.Calculate_BUTTON);
        }
		
		selenium.click(Rates.Save_BUTTON);
		if (selenium.isAlertPresent()) {
			selenium.getAlert();
			selenium.click(Rates.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} else {
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		String[] array = suffixName.split(";");

		for(String name:array) {
			if(name.length()==0)
				continue;
            Assert.assertEquals(isPresentInTable(selenium, Rates.Rates_TABLE,
                    iRateName+ "_" + name), true);
		}
//		Assert.assertEquals(isPresentInTable(selenium, Rates.Rates_TABLE,
//                    iRateName), true);

		return iRateName;
	}

	// Remove Rate.
	// added by ShenYang 2011-06-28
	public void removeRate(Selenium selenium, String iRateName)
			throws Exception {
		boolean selected = selectRadioButtonFromTable(selenium,
				Rates.Rates_TABLE, iRateName);
		if (!selected) {
			Reporter.log("Cannot find a proper Rate to remove.");
			return;
		}
		try {
			selenium.click(Rates.Remove_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			// verify if selected item is removed
			Assert.assertEquals(this.isPresentInTable(selenium,
					Rates.Rates_TABLE, iRateName), false);
		} catch (Exception e) {
			Reporter.log(e.getMessage());
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
	}

	// Edit Rate
	// added by ShenYang 2011-06-29
	public void editRate(Selenium selenium, String iRateName,
			String newRateName, String newCurrency, String newRateType)
			throws Exception {
		boolean selected = selectRadioButtonFromTable(selenium,
				Rates.Rates_TABLE, iRateName);
		if (!selected) {
			Reporter.log("Cannot find a proper Rate to edit.");
			return;
		}
		try {
			selenium.click(Rates.Edit_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.type(Rates.Name_TEXT_FIELD, newRateName);
			selenium.select(Rates.Currency_SELECT, "label="+ newCurrency);
			selenium.select(Rates.RateType_SELECT, "label="+ newRateType);
			if (newRateType == "Fixed" | newRateType == "Page"
					| newRateType == "Hourly") {
				selenium.type(newRateType, "123");
			} else if (newRateType == "Word Count") {
				selenium.type(Rates.In_Context_FIELD, "1.0");
				selenium.type(Rates.Hundred_FIELD, "2.0");
				selenium.type(Rates.BAND1_FIELD, "3.0");
				selenium.type(Rates.BAND2_FIELD, "4.0");
				selenium.type(Rates.BAND3_FIELD, "5.0");
				selenium.type(Rates.BAND4_FIELD, "6.0");
				selenium.type(Rates.No_match_FIELD, "7.0");
				selenium.type(Rates.No_Match_Repetition_FIELD, "8.0");

			} else {
				selenium.type(Rates.In_Context_FIELD + "Per", "1.0");
				selenium.type(Rates.Hundred_FIELD + "Per", "2.0");
				selenium.type(Rates.BAND1_FIELD + "Per", "3.0");
				selenium.type(Rates.BAND2_FIELD + "Per", "4.0");
				selenium.type(Rates.BAND3_FIELD + "Per", "5.0");
				selenium.type(Rates.BAND4_FIELD + "Per", "6.0");
				selenium.type(Rates.No_Match_Repetition_FIELD + "Per", "7.0");
				selenium.type(Rates.BaseRate_FIELD, "10");
				selenium.click(Rates.Calculate_BUTTON);

			}
			selenium.click(Rates.Save_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			// Verify if the change has been applied
			Assert.assertEquals(this.isPresentInTable(selenium,
					Rates.Rates_TABLE, newRateName), true);
			Assert.assertEquals(this.getColumnText(selenium, Rates.Rates_TABLE,
					newRateName, 5), newCurrency);
			Assert.assertEquals(this.getColumnText(selenium, Rates.Rates_TABLE,
					newRateName, 6), newRateType);
		} catch (Exception e) {
			Reporter.log(e.getMessage());
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
	}

	// Add new rate for selected users.
	// added by ShenYang 2011-06-29
	public void addUserNewRate(Selenium selenium, String iUserName)
			throws Exception {
		boolean selected = selectRadioButtonFromTable(selenium,
				Users.User_TABLE, iUserName);

		// if(this.getColumnText(selenium, Users.NewRole_TABLE, iRateName,
		// 1)==);
		if (!selected) {
			Reporter.log("Cannot find a proper Rate.");
			return;
		}
		try {
			selenium.click(Users.User_Edit_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(Users.Roles_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(Users.New_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
			Reporter.log(e.getMessage());
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

	}
}
