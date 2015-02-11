/**
 *  Copyright 2009, 2012 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.selenium.testcases.util;

import java.io.File;

import jodd.util.StringUtil;
import junit.framework.Assert;

import org.testng.Reporter;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.BasePage;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TmReportWebForm;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Utility class contains methods which can be used in Selenium
 * 
 * @author Vincent
 * @version 1.0
 * @since 8.2.2
 */
public class SeleniumUtils
{
	private static final int MAX_TABLE_ROW_INDEX = 50;

	public static void type(Selenium selenium, String elementLocator, String value) {
		selenium.type(elementLocator, value);
	}
	
	public static void select(Selenium selenium, String elementLocator, String value, boolean isLabel) {
		if (isLabel)
			selenium.select(elementLocator, "label=" + value);
		else
			selenium.select(elementLocator, "value=" + value);
	}
	
	/**
	 * Open specified menu item and wait for short waiting time
	 * 
	 * @param mainMenu
	 *            Main menu item
	 * @param menuItem
	 *            Sub menu item
	 */
	public static void openMenuItemAndWait(Selenium selenium, String mainMenu, String menuItem) {
        selenium.click(mainMenu);
        selenium.click(menuItem);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Open specified menu item and wait to load it
     * @param mainMenu Main menu item
     * @param menuItem Sub menu item
     * @param waitTime Time to wait, base unit is millisecond
     */
	public static void openMenuItemAndWait(Selenium selenium, String mainMenu,
            String menuItem, String waitTime)
    {
        selenium.click(mainMenu);
        selenium.click(menuItem);
        selenium.waitForPageToLoad(waitTime);
    }
    
    /**
     * Click a elementLocator and wait for short waiting time
     * @param elementLocator Button value
     */
	public static void clickAndWait(Selenium selenium, String elementLocator)
    {
        selenium.click(elementLocator);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
    
    /**
     * Click a elementLocator and wait for specified waiting time
     * @param elementLocator Button value
     * @param waitTime Wait time, the unit is millisecond
     */
	public static void clickAndWait(Selenium selenium, String elementLocator,
            String waitTime)
    {
        selenium.click(elementLocator);
        selenium.waitForPageToLoad(waitTime);
    }
    
    /**
     * Handle dialogs present in Web page such as alert, prompt and confirmation
     * @param selenium
     * @return True -- there has some dialogs being pop-up
     *
     * @version 1.0
     * @since 8.2.2
     */
	public static boolean handleDialogs(Selenium selenium) {
        boolean hasDialog = false;
        if (selenium.isAlertPresent()) {
            selenium.getAlert();
            hasDialog = true;
        }
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
            hasDialog = true;
        }
        if (selenium.isPromptPresent()) {
            selenium.getPrompt();
            hasDialog = true;
        }
        return hasDialog;
    }

    /**
     * Check if there is specified element locator in page, if there is a paged
     * table, it will go through it and check in each page of table
     * 
     * @param selenium
     *            Selenium object
     * @param elementLocator
     *            Element locator
     * @return True -- current page or in list table contains specified element
     * @throws Exception
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static boolean isElementPresent(Selenium selenium, String elementLocator)
            throws Exception
    {
        if (StringUtil.isEmpty(elementLocator))
            return false;

        elementLocator = elementLocator.trim();

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selenium.isElementPresent(elementLocator))
                    return true;
                else
                    selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            return selenium.isElementPresent(elementLocator);
        }
        catch (Exception e)
        {
            Reporter.log("Error in " + selenium.getTitle() + ", "
                    + e.toString());
        }

        return false;
    }

    /**
     * Check if there is specified content in page, if there is a paged table,
     * it will go through it and check in each page of table
     * 
     * @param selenium
     *            Selenium object
     * @param text
     *            Text content
     * @return True -- current page or in list table contains specified text
     *         content
     * @throws Exception
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static boolean isTextPresent(Selenium selenium, String text)
            throws Exception
    {
        if (StringUtil.isEmpty(text))
            return false;

        text = text.trim();

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selenium.isTextPresent(text))
                    return true;
                else
                    selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            return selenium.isTextPresent(text);
        }
        catch (Exception e)
        {
            Reporter.log("Error in " + selenium.getTitle() + ", "
                    + e.toString());
        }

        return false;
    }

	public static boolean click(Selenium selenium, String elementLocator)
            throws Exception
    {
        if (StringUtil.isEmpty(elementLocator))
            return false;

        elementLocator = elementLocator.trim();
        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selenium.isElementPresent(elementLocator))
                {
                    selenium.click(elementLocator);
                    return true;
                }
                selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            if (selenium.isElementPresent(elementLocator))
            {
                selenium.click(elementLocator);
                return true;
            }
        }
        catch (Exception e)
        {
            Reporter.log("Error in " + selenium.getTitle() + ", "
                    + e.toString());
        }

        return false;
    }

    /**
     * This Method is designed to check the radio button with no value
     * specified.
     * 
     * But you must provided the table string and the name string.
     * 
     * Author:Jester
     */
	public static boolean selectRadioButtonFromTable(Selenium selenium, String table,
            String text) throws Exception
    {

        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text))
            return false;

        table = table.trim();
        text = text.trim();

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selectRadioButton(selenium, table, text))
                    return true;
                else
                {
                    selenium.click(BasePage.NEXT_PAGE_LINK);
                }
            }

            return selectRadioButton(selenium, table, text);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }

        return false;
    }

	private static boolean selectRadioButton(Selenium selenium, String table,
            String name)
    {
        String element = getElementInTable(selenium, table, name);
        if (StringUtil.isNotEmpty(element))
        {
            selenium.click(element);
            return true;
        }
        else
            return false;
    }

    /**
     * Get the element locator from table with text content
     * 
     * @param selenium
     *            Selenium object
     * @param table
     *            XPath of table element
     * @param text
     *            Text content
     * @return String Element locator, null is return if there is no element
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static String getElementInTable(Selenium selenium, String table, String text)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text)
                || !selenium.isTextPresent(text))
            return null;

        return getElementInTable(selenium, table, text, null);
    }

    /**
     * Get the element locator from table with text content in specified cell
     * 
     * @param selenium
     *            Selenium object
     * @param table
     *            XPath of table element
     * @param text
     *            Text content
     * @param columnIndex
     *            Column index of table cell, default is 2
     * @return String Element locator, null is returned if there is no element
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static String getElementInTable(Selenium selenium, String table,
            String text, String columnIndex)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text)
                || !selenium.isTextPresent(text))
            return null;

        String elementField = null;

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            String prefix = table + "//tr[";
            String suffix = "]";
            String textCell = "//td["
                    + (columnIndex == null ? "2" : columnIndex) + "]";
            String inputCell = "//td[1]/input";
            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                elementField = getElementFromTablePage(selenium, text, prefix,
                        suffix, textCell, inputCell);

                if (StringUtil.isNotEmpty(elementField))
                    break;
                else
                    selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            if (StringUtil.isEmpty(elementField))
            {
                elementField = getElementFromTablePage(selenium, text, prefix,
                        suffix, textCell, inputCell);
            }
        }
        catch (Exception e)
        {
            Reporter.log("Error in " + selenium.getTitle() + ", "
                    + e.toString());
        }

        return elementField;
    }

	private static String getElementFromTablePage(Selenium selenium, String text,
            String prefix, String suffix, String textCell, String inputCell)
    {
        int index = 1;
        String textField = "";
        String elementField = null;

        while (true)
        {
            textField = prefix + index + suffix + textCell;
            if (selenium.isElementPresent(textField))
            {
                if (text.equals(selenium.getText(textField)))
                {
                    elementField = prefix + index + suffix + inputCell;
                    break;
                }
            }
            index++;
			if (index > MAX_TABLE_ROW_INDEX)
				break;
        }

        return elementField;
    }

	private static String getElementValueFromTablePage(Selenium selenium, String text,
            String prefix, String suffix, String textCell, String inputCell)
    {
        int index = 1;
        String textField = "";
        String elementField = null;
        String elementValue = null;

        while (true)
        {
            textField = prefix + index + suffix + textCell;
            if (selenium.isElementPresent(textField))
            {
                if (text.equals(selenium.getText(textField)))
                {
                    elementField = prefix + index + suffix + inputCell;
                    elementValue = selenium.getValue(elementField);
                    break;
                }
            }
            index++;
			if (index > MAX_TABLE_ROW_INDEX)
				break;
        }

        return elementValue;
    }

    /**
     * Get the value attribute of element for radio button with text content
     * 
     * @param selenium
     *            Selenium object
     * @param table
     *            XPath of table element
     * @param text
     *            Text content
     * @return String Value of radio button, null is return if there is no
     *         element
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static String getElementValueFromTable(Selenium selenium, String table,
            String text)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text)
                || !selenium.isTextPresent(text))
            return null;

        return getElementValueFromTable(selenium, table, text, null);
    }

    /**
     * Get the value attribute of element for radio button with text content in
     * specified column cell.
     * 
     * @param selenium
     *            Selenium object
     * @param table
     *            XPath of table element
     * @param text
     *            Text content
     * @param columnIndex
     *            Column index of table cell, default is 2
     * @return String Value of radio button, null is return if there is no
     *         element
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static String getElementValueFromTable(Selenium selenium, String table,
            String text, String columnIndex)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text)
                || !selenium.isTextPresent(text))
            return null;

        String elementValue = null;

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            String prefix = table + "//tr[";
            String suffix = "]";
            String textCell = "//td["
                    + (columnIndex == null ? "2" : columnIndex) + "]";
            String inputCell = "//td[1]/input";
            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                elementValue = getElementValueFromTablePage(selenium, text,
                        prefix, suffix, textCell, inputCell);

                if (StringUtil.isNotEmpty(elementValue))
                    break;
                else
                    selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            if (StringUtil.isEmpty(elementValue))
            {
                elementValue = getElementValueFromTablePage(selenium, text,
                        prefix, suffix, textCell, inputCell);
            }
        }
        catch (Exception e)
        {
            Reporter.log("Error in " + selenium.getTitle() + ", "
                    + e.toString());
        }

        return elementValue;
    }

    /**
     * Get the value attribute of element for radio button with text content
     * 
     * @param selenium
     *            Selenium object
     * @param table
     *            XPath of table element
     * @param text
     *            Text content
     * @return String Value of radio button, null is return if there is no
     *         element
     * 
     * @version 1.0
     * @since 8.2.2
     */
	public static String getRadioValueFromTable(Selenium selenium, String table,
            String text) throws Exception
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text))
            return null;

        table = table.trim();
        text = text.trim();

        return getElementValueFromTable(selenium, table, text);
    }

    /**
     * This Method is designed to check the radio button with no value
     * specified.
     * 
     * But you must provided the table string and the name string and the TD
     * number.
     * 
     * Author:Jester
     */
	public static boolean selectRadioButtonFromTable(Selenium selenium, String iTable,
            String iName, int iTd) throws Exception
    {
        if (StringUtil.isEmpty(iTable) || StringUtil.isEmpty(iName))
            return false;

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        iTable = iTable.trim();
        iName = iName.trim();
        String element = "";

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            element = getElementInTable(selenium, iTable, iName,
                    String.valueOf(iTd));
            if (!StringUtil.isEmpty(element))
            {
                selenium.click(element);
                return true;
            }
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        element = getElementInTable(selenium, iTable, iName,
                String.valueOf(iTd));
        if (!StringUtil.isEmpty(element))
        {
            selenium.click(element);
            return true;
        }
        else
            return false;
    }

    /**
     * This Method is designed to check if the item presents in the table.
     * 
     * But you must provided the table string and the name string. Author:Jester
     */
	public static boolean isPresentInTable(Selenium selenium, String iTable,
            String iName) throws Exception
    {

        iTable = iTable.trim();
        iName = iName.trim();

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            if (selenium.isTextPresent(iName))
                return true;
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        return selenium.isTextPresent(iName);
    }

    /**
     * This Method is designed to check if the item presents in the table.
     * 
     * But you must provided the table string and the name string and the TD
     * number. Author:Jester
     */
	public static boolean isPresentInTable(Selenium selenium, String iTable,
            String iName, int iTd) throws Exception
    {

        iTable = iTable.trim();
        iName = iName.trim();

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        String element = "";
        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            element = getElementInTable(selenium, iTable, iName,
                    String.valueOf(iTd));
            if (StringUtil.isNotEmpty(element))
            {
                return true;
            }
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        element = getElementInTable(selenium, iTable, iName,
                String.valueOf(iTd));
        return StringUtil.isNotEmpty(element);
    }
	
    private int takePicture(Selenium selenium, int fileIndex)
    {
        Number x = selenium
                .getElementPositionLeft(TmReportWebForm.TMREPORT_TABLE_NAME);
        Number y = selenium
                .getElementPositionTop(TmReportWebForm.TMREPORT_TABLE_NAME);
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\TmReport\\TmReport_" + fileIndex + ".jpg");
        
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\TmReport\\TmReport_" + fileIndex
                        + ".jpg");
        Assert.assertTrue(file.exists());
        
        return fileIndex++;
        
    }

    /**
     * Check if the value is true value. True will be returned if the value is
     * as 'true', '1', 'on' or 'yes' whatever its case.
     * 
     * @param s Value
     * @return boolean True will be returned if the value string is 'true', '1',
     * 		   'on' or 'yes' without case
     */
    public static boolean isTureValue(String s) {
    	if (StringUtil.isEmpty(s))
    		return false;
    	
    	s = s.trim();
    	if ("true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s) || "on".equalsIgnoreCase(s))
    		return true;
    	else {
			return false;
		}
    }
    
    /**
     * Check if the value is false value. True will be returned if the value is
     * as 'false', '0', 'off' or 'no' whatever its case.
     * 
     * @param s Value
     * @return boolean True will be returned if the value string is 'false', '0',
     * 		   'off' or 'no' without case
     */
    public static boolean isFalseValue(String s) {
    	return !isTureValue(s);
    }

    public static void relogin(Selenium selenium, String userName, String password) {
    	selenium.click(MainFrame.LOG_OUT_LINK);
    	
    	CommonFuncs.login(selenium, userName, password);
    }
}
