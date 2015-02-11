package com.globalsight.selenium.testcases;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;


public class BaseTestCase
{
    protected Selenium selenium = CommonFuncs.getSelenium();
    protected String testCaseName = getTestCaseName();
    
    public String getClassName()
    {
        return getClass().getName();
    }
    
    public String getTestCaseName() {
        return getClassName();
    }
    
    /**
     * Open specified menu item and wait for short waiting time
     * @param mainMenu Main menu item
     * @param menuItem Sub menu item
     */
    public void openMenuItemAndWait(Selenium selenium, String mainMenu, String menuItem) {
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
    public void openMenuItemAndWait(Selenium selenium, String mainMenu, String menuItem, String waitTime) {
        selenium.click(mainMenu);
        selenium.click(menuItem);
        selenium.waitForPageToLoad(waitTime);
    }
    
    /**
     * Click a elementLocator and wait for short waiting time
     * @param elementLocator Button value
     */
    public void clickAndWait(Selenium selenium, String elementLocator)
    {
        selenium.click(elementLocator);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
    
    /**
     * Click a elementLocator and wait for specified waiting time
     * @param elementLocator Button value
     * @param waitTime Wait time, the unit is millisecond
     */
    public void clickAndWait(Selenium selenium, String elementLocator, String waitTime)
    {
        selenium.click(elementLocator);
        selenium.waitForPageToLoad(waitTime);
    }
    
    public String getDataInCase(String key) {
        return ConfigUtil.getDataInCase(testCaseName, key);
    }

    public String getDataInCase(String testCaseName, String key) {
        return ConfigUtil.getDataInCase(testCaseName, key);
    }
    
    public String getProperty(String propertyName) {
        return ConfigUtil.getProperty(propertyName);
    }

    @BeforeSuite
    public void beforeSuite() {
        CommonFuncs.loginSystemWithAdmin(selenium);
    }
    
    @AfterSuite
    public void afterSuite() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
        CommonFuncs.endSelenium(selenium);
    }

}
