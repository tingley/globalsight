package com.globalsight.selenium.testcases.smoketest;

import java.util.ArrayList;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class CreateFileProfile extends BaseTestCase
{

    private Selenium selenium;
    private FileProfileFuncs fp;
    
    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        fp = new FileProfileFuncs();
    }
    
    @AfterClass
    public void afterClass()
    {
        fp = null;
        selenium.stop();
    }
    
    @BeforeMethod
    public void BeforeMethod()
    {
        try
        {
            navigateToFileProfile();
            if (!selenium.getTitle().equals("Terminology Management"))
            {
                CommonFuncs.loginSystemWithAdmin(selenium);
                navigateToFileProfile();
            }
        }
        catch (Exception e)
        {
            CommonFuncs.loginSystemWithAdmin(selenium);
            navigateToFileProfile();
        }
    }
    
    @Test
    public void createFileProfile()
    {
        ArrayList<String> array = new ArrayList<String>();
        array.add("file_profile_names");
        array.add("localization_names");
        array.add("source_file_format");
        array.add("file_extensions");
        array.add("file_descriptions");
        fp.setup(array);
        fp.create(selenium, getClassName());
    }

    private void navigateToFileProfile()
    {
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FileProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
