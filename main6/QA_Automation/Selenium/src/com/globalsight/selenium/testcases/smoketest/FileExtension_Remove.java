package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileExtensionFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class FileExtension_Remove {
	 /*
     * Common variables initialization.
     */
    private Selenium selenium;
    private FileExtensionFuncs iFileExtensionFuncs = new FileExtensionFuncs();
    String testCaseName = getClass().getName();

    @Test
    public void FileExtensionRemove() throws Exception
    {
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FileExtension_SUBMENU);
        /*selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);*/

        iFileExtensionFuncs.removeFileExtension(selenium, ConfigUtil
                .getDataInCase(testCaseName, "EXTENSION"));
   }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);

    }

    @AfterMethod
    public void afterMethod()
    {
        selenium.click(MainFrame.LogOut_LINK);
    }

    @BeforeClass
    public void beforeClass()
    {
    }

    @AfterClass
    public void afterClass()
    {
    }

    @BeforeTest
    public void beforeTest()
    {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest()
    {
        CommonFuncs.endSelenium(selenium);
    }

    @BeforeSuite
    public void beforeSuite()
    {
    }

    @AfterSuite
    public void afterSuite()
    {
    }
}
