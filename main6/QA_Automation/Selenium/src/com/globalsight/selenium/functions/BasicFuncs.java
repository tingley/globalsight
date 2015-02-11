package com.globalsight.selenium.functions;

import java.util.ArrayList;

import jodd.util.StringUtil;

import org.testng.Reporter;

import com.globalsight.selenium.pages.BasePage;
import com.thoughtworks.selenium.Selenium;

public class BasicFuncs
{
    /**
     * This method is designed to verify if the element can be found on the page
     * which have many sub-pages.
     * 
     * Author:Jester
     */
    public boolean isElementPresent(Selenium selenium, String element)
            throws Exception
    {
        if (StringUtil.isEmpty(element))
            return false;

        element = element.trim();

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
            {
                selenium.click(BasePage.FIRST_PAGE_LINK);
            }

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selenium.isElementPresent(element))
                {
                    return true;
                }
                selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            return selenium.isElementPresent(element);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }

        return false;
    }

    public boolean isTextPresent(Selenium selenium, String text)
            throws Exception
    {
        if (StringUtil.isEmpty(text))
            return false;

        text = text.trim();

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
            {
                selenium.click(BasePage.FIRST_PAGE_LINK);
            }

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selenium.isTextPresent(text))
                {
                    return true;
                }
                selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            return selenium.isTextPresent(text);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }

        return false;
    }

    /**
     * This method is designed to click the radio button if the element can be
     * found on the page.
     * 
     * Author:Jester
     */
    public boolean selectElement(Selenium selenium, String element)
            throws Exception
    {
        if (StringUtil.isEmpty(element))
            return false;

        element = element.trim();
        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selenium.isElementPresent(element))
                {
                    selenium.click(element);
                    return true;
                }
                selenium.click(BasePage.NEXT_PAGE_LINK);
            }

            if (selenium.isElementPresent(element))
            {
                selenium.click(element);
                return true;
            }
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
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
    public boolean selectRadioButtonFromTable(Selenium selenium, String table,
            String name) throws Exception
    {

        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(name))
            return false;

        table = table.trim();
        name = name.trim();

        try
        {
            if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
                selenium.click(BasePage.FIRST_PAGE_LINK);

            while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
            {
                if (selectRadioButton(selenium, table, name))
                    return true;
                else
                {
                    selenium.click(BasePage.NEXT_PAGE_LINK);
                }
            }

            return selectRadioButton(selenium, table, name);
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
        }

        return false;
    }

    private boolean selectRadioButton(Selenium selenium, String table,
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

    public String getElementInTable(Selenium selenium, String table, String name)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(name)
                || !selenium.isTextPresent(name))
            return null;

        return getElementInTable(selenium, table, name, null);
    }

    public String getElementInTable(Selenium selenium, String table,
            String name, String cellIndex)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(name)
                || !selenium.isTextPresent(name))
            return null;

        String prefix = table + "//tr[";
        String suffix = "]";
        String textCell = "//td[" + (cellIndex == null ? "2" : cellIndex)
                + "]";
        String inputCell = "//td[1]/input";
        //String Name = name.replace("_", "");
        
		int index = 1;
        String textField = "", elementField = "";
        while (index < 100)
        {
            textField = prefix + index + suffix + textCell;
            if (selenium.isElementPresent(textField))
            {
                if (name.equals(selenium.getText(textField)))
                {
                    elementField = prefix + index + suffix + inputCell;
                    break;
                }
            }
            index++;
        }

        return elementField;
    }
    
    public boolean checkNameFromTable(Selenium selenium, String table,
            String name, String cellIndex)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(name)
                || !selenium.isTextPresent(name))
            return false;

        boolean tagC=false;
        String prefix = table + "//tr[";
        String suffix = "]";
        String textCell = "//td[" + (cellIndex == null ? "2" : cellIndex)
                + "]";

		int index = 1;
        String textField = "";
        while (index < 100)
        {
            textField = prefix + index + suffix + textCell;
            if (selenium.isElementPresent(textField))
            {
                if (name.equals(selenium.getText(textField)))
                {
                    tagC=true;
                    break;
                }
            }
            index++;
        }

        return tagC;
    }

    public String getElementValueInTable(Selenium selenium, String table,
            String name)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(name)
                || !selenium.isTextPresent(name))
            return null;

        return getElementValueInTable(selenium, table, name, null);
    }

    public String getElementValueInTable(Selenium selenium, String table,
            String name, String cellIndex)
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(name)
                || !selenium.isTextPresent(name))
            return null;

        String prefix = table + "//tr[";
        String suffix = "]";
        String textCell = "//td[" + (cellIndex == null ? "2" : cellIndex)
                + "]";
        String inputCell = "//td[1]/input";

        int index = 1;
        String textField = "", elementField = "", elementValue = "";
        while (true)
        {
            textField = prefix + index + suffix + textCell;
            if (selenium.isElementPresent(textField))
            {
                if (name.equals(selenium.getText(textField)))
                {
                    elementField = prefix + index + suffix + inputCell;
                    elementValue = selenium.getValue(elementField);
                    break;
                }
            }
            else
                break;
            index++;
        }

        return elementValue;
    }

    public String getRadioValueFromTable(Selenium selenium, String table,
            String text) throws Exception
    {
        if (StringUtil.isEmpty(table) || StringUtil.isEmpty(text))
            return "";

        table = table.trim();
        text = text.trim();

        String value = "";

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
            selenium.click(BasePage.FIRST_PAGE_LINK);

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            value = getElementValueInTable(selenium, table, text);
            if (!StringUtil.isEmpty(value))
                return value;
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        return getElementValueInTable(selenium, table, text);
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
    public boolean selectRadioButtonFromTable(Selenium selenium, String iTable,
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
    public boolean isPresentInTable(Selenium selenium, String iTable,
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
    public boolean isPresentInTable(Selenium selenium, String iTable,
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

    // Author:Wally
    public void clickAndWait(Selenium selenium, String locator)
    {
        selenium.click(locator);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Select Radio Button From Table For Remove Local Pairs
     * 
     * Author:Totti
     */
    public boolean selectRadioForRemove(Selenium selenium, String iTable,
            String iName1, String iName2) throws Exception
    {

        iTable = iTable.trim();
        iName1 = iName1.trim();
        iName2 = iName2.trim();

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            try
            {
                int i = 1;
                while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
                {
                    if (selenium.getText(iTable + "//tr[" + i + "]//td[2]")
                            .trim().equals(iName1.trim())
                            && selenium
                                    .getText(iTable + "//tr[" + i + "]//td[3]")
                                    .trim().equals(iName2.trim()))
                    {
                        selenium.click(iTable + "//tr[" + i + "]//td[1]//input");
                        return true;
                    }
                    i++;
                }
            }
            catch (Exception e)
            {
                Reporter.log(e.toString());
                return false;
            }
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        try
        {
            int i = 1;
            while (selenium.isElementPresent(iTable + "/tr[" + i + "]"))
            {
                if ((selenium.getText(iTable + "//tr[" + i + "]//td[2]").trim()
                        .equals(iName1.trim()))
                        && (selenium.getText(iTable + "//tr[" + i + "]//td[3]")
                                .trim().equals(iName2.trim())))
                {
                    selenium.click(iTable + "//tr[" + i + "]//td[1]//input");
                    return true;
                }
                i++;
            }
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
            return false;
        }

        return false;
    }

    /*
     * 1. Find the row whose name equals iName, 2. Return the text in column iTd
     * of this row. Author: Shenyang Create Time: 2011-6-22
     */
    public String getColumnText(Selenium selenium, String iTable, String iName,
            int iTd) throws Exception
    {

        iName = iName.trim();
        iTable = iTable.trim();

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            try
            {
                int i = 1;
                while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
                {
                    if (selenium.getText(
                            iTable + "//tr[" + i + "]//td[" + 2 + "]").equals(
                            iName))
                    {
                        return selenium.getText(iTable + "//tr[" + i + "]//td["
                                + iTd + "]");
                    }
                    i++;
                }
            }
            catch (Exception e)
            {
                Reporter.log(e.toString());
                return null;
            }
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        try
        {
            int i = 1;
            while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
            {
                if (selenium.getText(iTable + "//tr[" + i + "]//td[" + 2 + "]")
                        .equals(iName))
                {
                    return selenium.getText(iTable + "//tr[" + i + "]//td["
                            + iTd + "]");
                }
                i++;
            }
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
            return null;
        }

        return null;
    }

    /*
     * From Job Name getting corresponding wordcount. author: Shenyang
     * 2011-08-23
     */
    public String jobgetWordCount(Selenium selenium, String iTable,
            String iName, int iTd) throws Exception
    {

        iName = iName.trim();
        iTable = iTable.trim();

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            try
            {
                int i = 1;
                while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
                {
                    if (selenium.getText(
                            iTable + "//tr[" + i + "]//td[" + 4 + "]").equals(
                            iName))
                    {
                        return selenium.getText(iTable + "//tr[" + i + "]//td["
                                + iTd + "]");
                    }
                    i++;
                }
            }
            catch (Exception e)
            {
                Reporter.log(e.toString());
                return null;
            }
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        try
        {
            int i = 1;
            while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
            {
                if (selenium.getText(iTable + "//tr[" + i + "]//td[" + 4 + "]")
                        .equals(iName))
                {
                    return selenium.getText(iTable + "//tr[" + i + "]//td["
                            + iTd + "]");
                }
                i++;
            }
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
            return null;
        }

        return null;
    }

    /**
     * author:Shenyang Use arraylist to get jobname;
     */
    public ArrayList<String> getAllColumnText(Selenium selenium, String iTable,
            int iTd) throws Exception
    {

        iTable = iTable.trim();
        ArrayList<String> resultList = new ArrayList<String>();

        if (selenium.isElementPresent(BasePage.FIRST_PAGE_LINK))
        {
            selenium.click(BasePage.FIRST_PAGE_LINK);
        }

        while (selenium.isElementPresent(BasePage.NEXT_PAGE_LINK))
        {
            try
            {
                int i = 1;
                while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
                {
                    String textGetted = selenium.getText(iTable + "//tr[" + i
                            + "]//td[" + iTd + "]");
                    resultList.add(textGetted);
                    i++;
                }
            }
            catch (Exception e)
            {
                Reporter.log(e.toString());
                return null;
            }
            selenium.click(BasePage.NEXT_PAGE_LINK);
        }

        try
        {
            int i = 1;
            while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
            {
                String textGetted = selenium.getText(iTable + "//tr[" + i
                        + "]//td[" + iTd + "]");
                resultList.add(textGetted);
                i++;
            }
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
            return null;
        }

        return resultList;
    }
}
