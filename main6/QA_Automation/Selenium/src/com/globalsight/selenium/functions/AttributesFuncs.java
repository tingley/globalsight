package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.Attributes;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: AttributesFuncs.java
 * Author:Jester
 * Methods: AttributesNew() 
 * 
 */
public class AttributesFuncs extends BasicFuncs {

	/**
	 * Create a new Attribute.
	 */
	public String newAttributes(Selenium selenium, String attributesProfile)
			throws Exception {
		selenium.click(Attributes.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		String[] iattributesProfile = attributesProfile.split(",");
		String iAttributeDisplayName = null;

		for (String iattributes : iattributesProfile) {
			try {
				String[] ivalue = iattributes.split("=");
				String iFieldName = ivalue[0].trim();
				String iFieldValue = ivalue[1].trim();
				if (iFieldName.equals("internalname")) {
					selenium.type(Attributes.InteralName_TEXT_FIELD,
							iFieldValue);
				} else if (iFieldName.equals("displayname")) {
					selenium.type(Attributes.DisplayName_TEXT_FIELD,
							iFieldValue);
					iAttributeDisplayName = iFieldValue;
				} else if (iFieldName.equals("type")) {
					selenium.select(Attributes.Type_SELECT, "label="
							+ iFieldValue);
				} else if (iFieldName.equals("description")) {
					selenium.type(Attributes.Description_TEXT_FIELD,
							iFieldValue);
				}
			} catch (Exception e) {
				Reporter.log(e.getMessage());
			}
		}
		selenium.click(Attributes.Save_BUTTON);
		if (selenium.isElementPresent(Attributes.InteralName_TEXT_FIELD)) {
			selenium.getAlert();
			selenium.click(Attributes.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
		Assert.assertEquals(
				this.findElementsOnTables(selenium, "link="
						+ iAttributeDisplayName), true);
		return iAttributeDisplayName;
	}
	/**
	* Remove Attributes
	* Author Totti
	**/
	public void removeAttributes(Selenium selenium, String attributesProfile) throws Exception
	{
		String[] iattributesProfile = attributesProfile.split(","); 
		for (String iattributes : iattributesProfile) {
				String[] ivalue = iattributes.split("=");
				String iFieldName = ivalue[1].trim();
				boolean result=super.selectRadioButtonFromTable(selenium,Attributes.Attribute_TABLE,iFieldName);
				if(result==true)
				{
					selenium.click(Attributes.Remove_Button);
					if (selenium.isConfirmationPresent())
					{
						selenium.getConfirmation();
					}
					selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);		
					break;
				}
				else
				{
					System.out.println("error");					
				}			
		}		
	}

	/**
	* Edit Attributes
	* Author Totti
	**/
	public String editAttributes(Selenium selenium, String attributesProfile)
			throws Exception {
		
		String[] iattributesProfile = attributesProfile.split(","); 
		String iAttributeDisplayName = null;
		for (String iattributes : iattributesProfile) {
		
				String[] ivalue = iattributes.split("=");
				String iFieldName = ivalue[1].trim();
				boolean result=super.selectRadioButtonFromTable(selenium,Attributes.Attribute_TABLE,iFieldName);
				if(result==true)
				{
					selenium.click(Attributes.Edit_BUTTON);
					if (selenium.isConfirmationPresent())
					{
						selenium.getConfirmation();
					}
					selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);		
					break;
				}
				else
				{
					System.out.println("error");					
				}			
		}				
		for (String iattributes : iattributesProfile) {
				String[] ivalue = iattributes.split("=");
				String iFieldName = ivalue[0].trim();
				String iFieldValue = ivalue[1].trim();
				if (iFieldName.equals("internalname")) {
					selenium.type(Attributes.InteralName_TEXT_FIELD,
							iFieldValue);
				} else if (iFieldName.equals("displayname")) {
					selenium.type(Attributes.DisplayName_TEXT_FIELD,
							iFieldValue);
					iAttributeDisplayName = iFieldValue;
				} else if (iFieldName.equals("type")) {
					selenium.select(Attributes.Type_SELECT, "label="
							+ iFieldValue);
				} else if (iFieldName.equals("description")) {
					selenium.type(Attributes.Description_TEXT_FIELD,
							iFieldValue);
				}		
		}
		selenium.click(Attributes.Save_BUTTON);
		if (selenium.isElementPresent(Attributes.InteralName_TEXT_FIELD)) {
			selenium.getAlert();
			selenium.click(Attributes.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
		Assert.assertEquals(
				this.findElementsOnTables(selenium, "link="
						+ iAttributeDisplayName), true);
		return iAttributeDisplayName;
}
}
