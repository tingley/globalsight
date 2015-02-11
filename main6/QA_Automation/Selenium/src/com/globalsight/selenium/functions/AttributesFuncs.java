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
public class AttributesFuncs extends BasicFuncs
{

    /**
     * Create a new Attribute.
     */
    public String create(Selenium selenium, String attributesProfile)
            throws Exception
    {
        selenium.click(Attributes.NEW_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        String[] fields = attributesProfile.split(",");
        String displayName = null;
        String[] element = null;
        String elementName = "", elementValue = "";

        for (String field : fields)
        {
            try
            {
                element = field.split("=");
                elementName = element[0].trim();
                elementValue = element[1].trim();

                if ("internalName".equals(elementName))
                {
                    selenium.type(Attributes.INTERAL_NAME_TEXT, elementValue);
                }
                else if ("displayName".equals(elementName))
                {
                    selenium.type(Attributes.DISPLAY_NAME_TEXT, elementValue);
                    displayName = elementValue;
                }
                else if ("type".equals(elementName))
                {
                    selenium.select(Attributes.Type_SELECT, "label="
                            + elementValue);
                }
                else if ("description".equals(elementName))
                {
                    selenium.type(Attributes.DESCRIPTION_TEXT, elementValue);
                }
            }
            catch (Exception e)
            {
                Reporter.log(e.getMessage());
            }
        }
        selenium.click(Attributes.SAVE_BUTTON);

        if (selenium.isElementPresent(Attributes.INTERAL_NAME_TEXT))
        {
            selenium.getAlert();
            selenium.click(Attributes.CANCEL_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        Assert.assertEquals(
                this.isElementPresent(selenium, "link=" + displayName),
                true);
        return displayName;
    }

    /**
     * Remove Attributes Author Totti
     **/
    public void remove(Selenium selenium, String attributesProfile)
            throws Exception
    {
        String attributeName = getAttributeName(attributesProfile);

        boolean result = selectRadioButtonFromTable(selenium,
                Attributes.ATTRIBUTE_TABLE, attributeName);
        if (result)
        {
            selenium.click(Attributes.REMOVE_BUTTON);
            if (selenium.isConfirmationPresent())
            {
                selenium.getConfirmation();
            }
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        else
        {
            Reporter.log("Error: " + attributeName + " is not found.");
        }
    }

    private String getAttributeName(String attributesProfile)
    {
        String[] fields = attributesProfile.split(",");
        String[] nameField = fields[0].split("=");
        return nameField[1];
    }

    /**
     * Edit Attributes Author Totti
     **/
    public String modify(Selenium selenium, String attributesProfile)
            throws Exception
    {
        String attributeName = getAttributeName(attributesProfile);
        boolean result = selectRadioButtonFromTable(selenium,
                Attributes.ATTRIBUTE_TABLE, attributeName);
        if (result)
        {
            selenium.click(Attributes.Edit_BUTTON);
            if (selenium.isConfirmationPresent())
            {
                selenium.getConfirmation();
            }
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        else
        {
            Reporter.log("Error: " + attributeName + " is not found.");
            return attributeName;
        }

        String[] fields = attributesProfile.split(",");
        String displayName = null;
        String[] element = null;
        String elementName = "", elementValue = "";

        for (String field : fields)
        {
            element = field.split("=");
            elementName = element[0].trim();
            elementValue = element[1].trim();
            if ("internalName".equals(elementName))
            {
                selenium.type(Attributes.INTERAL_NAME_TEXT, elementValue);
            }
            else if ("displayName".equals(elementName))
            {
                selenium.type(Attributes.DISPLAY_NAME_TEXT, elementValue);
                displayName = elementValue;
            }
            else if ("type".equals(elementName))
            {
                selenium.select(Attributes.Type_SELECT, "label=" + elementValue);
            }
            else if ("description".equals(elementName))
            {
                selenium.type(Attributes.DESCRIPTION_TEXT, elementValue);
            }
        }
        selenium.click(Attributes.SAVE_BUTTON);
        if (selenium.isElementPresent(Attributes.INTERAL_NAME_TEXT))
        {
            selenium.getAlert();
            selenium.click(Attributes.CANCEL_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        Assert.assertEquals(
                this.isElementPresent(selenium, "link=" + displayName),
                true);
        return displayName;
    }
}
