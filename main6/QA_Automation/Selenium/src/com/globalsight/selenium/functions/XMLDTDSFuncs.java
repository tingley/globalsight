package com.globalsight.selenium.functions;

import org.testng.Assert;

import com.globalsight.selenium.pages.XMLDTDS;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: XMLDTDSFuncs.java
 * Author:Jester
 * Methods: XMLDTDNew(),XMLDTDRemove() 
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-2   First Draft    Jester
 */
public class XMLDTDSFuncs extends BasicFuncs {

	/*
	 * Create a new XML DTD
	 */
	public String newXMLDTD(Selenium selenium, String XMLDTDProfiles)
			throws Exception {
		selenium.click(XMLDTDS.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		String[] iXMLDTDProfiles = XMLDTDProfiles.split(",");
		String iXMLDTDName = null;

		for (String iXMLDTDProfile : iXMLDTDProfiles) {
			String[] ivalue = iXMLDTDProfile.split("=");
			String iFieldName = ivalue[0].trim();
			String iFieldValue = ivalue[1].trim();

			if (iFieldName.equals("name")) {
				selenium.type(XMLDTDS.Name_TEXT_FIELD, iFieldValue);
				iXMLDTDName = iFieldValue;
			} else if (iFieldName.equals("description")) {
				selenium.type(XMLDTDS.Description_TEXT_FIELD, iFieldValue);
			}
		}
		selenium.click(XMLDTDS.Save_BUTTON);
		// selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		if (selenium.isAlertPresent()) {
			selenium.getAlert();
			selenium.click(XMLDTDS.Return_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} else {
			selenium.click(XMLDTDS.Return_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
		Assert.assertEquals(
				findElementsOnTables(selenium, "link=" + iXMLDTDName), true);
		return iXMLDTDName;
	}

	/*
	 * Remove the new XML DTD
	 */
	public boolean XMLDTDRemove(Selenium selenium, String iXMLDTDName)
			throws Exception {
		if (selectRadioButtonFromTable(selenium, XMLDTDS.XMLDTDS_TABLE,
				iXMLDTDName)) {
			selenium.click(XMLDTDS.Remove_BUTTON);
			try {
				selenium.getConfirmation();
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			} catch (Exception e) {
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			}
		}
		Assert.assertEquals(
				findElementsOnTables(selenium, "link=" + iXMLDTDName), false);
		return false;
	}
}
