package com.globalsight.selenium.functions;

/*
 * FileName: AttributeGroupsFuncs.java
 * Author:Jester
 * Methods: AttributeGroupNew() 
 * 
 */

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.AttributeGroups;
import com.thoughtworks.selenium.Selenium;

public class AttributeGroupsFuncs extends BasicFuncs {

	// Create a new attribute group.
	public void newAttributeGroup(Selenium selenium, String iGroupName)
			throws Exception {
		selenium.click(AttributeGroups.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.type(AttributeGroups.Name_TEXT_FIELD, iGroupName);
		selenium.click(AttributeGroups.Save_BUTTON);
		try {
			selenium.getAlert();
			selenium.click(AttributeGroups.Cancel_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		Assert.assertEquals(
				this.isElementPresent(selenium, "link=" + iGroupName), true);
	}

	/**
	* Remove Attribute Groups
	* Author Totti
	**/
	public void removeAttributesGroup(Selenium selenium, String attributeGroupsProfile)
	{				 
				boolean result;
				try {
					result = super.selectRadioButtonFromTable(selenium,
							AttributeGroups.AttributeGroup_TABLE,attributeGroupsProfile);
					if(!result){
					    Reporter.log("Cannot find proper AttributeGroup to remove!");
					    return;
					}
					else 
					{
						selenium.click(AttributeGroups.Remove_BUTTON);
						if (selenium.isConfirmationPresent())
						{
							selenium.getConfirmation();
						}
						selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);				
					}
					Assert.assertEquals(this.isPresentInTable(selenium, AttributeGroups.AttributeGroup_TABLE, attributeGroupsProfile), false);	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
	}		
	public void editAttributesGroup(Selenium selenium, String attributeGroupsProfile, 
			String attributeGroupsProfile1)
	{				 
				boolean result;
				try {
					result = super.selectRadioButtonFromTable(selenium,
							AttributeGroups.AttributeGroup_TABLE,attributeGroupsProfile);
					if(result==true)
					{
						selenium.click(AttributeGroups.Edit_BUTTON);
						selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
						selenium.type(AttributeGroups.Name_TEXT_FIELD, attributeGroupsProfile1);
						selenium.click(AttributeGroups.Save_BUTTON);
						try {
							selenium.getAlert();
							selenium.click(AttributeGroups.Cancel_BUTTON);
							selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
						} catch (Exception e) {
							selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
						}

						
						if (selenium.isConfirmationPresent())
						{
							selenium.getConfirmation();
						}
						selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);				
					}
					else
					{
						System.out.println("error");					
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
}
