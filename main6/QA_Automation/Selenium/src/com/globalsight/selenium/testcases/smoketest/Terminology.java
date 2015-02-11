package com.globalsight.selenium.testcases.smoketest;

import java.io.File;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class Terminology
{
    private Selenium selenium;
    private TerminologyFuncs term;
    
    @BeforeClass
    public void beforeClass()
    {
//        selenium = CommonFuncs.initSelenium();
        
    }
    
    @AfterClass
    public void afterClass()
    {
//        term = null;
//        selenium.stop();
    }
    
    @BeforeMethod
    public void BeforeMethod()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
//        navigateToTermbase();
//        if (!selenium.getTitle().equals("Terminology Management"))
//        {
//            CommonFuncs.loginSystemWithAdmin(selenium);
//            navigateToTermbase();
//        }
//        Assert.assertEquals(selenium.getTitle(), "Terminology Management");
    }
    @AfterMethod
    public void afterMethod() {
        CommonFuncs.logoutSystem(selenium);
    }
    @BeforeTest
    public void beforeTest() {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest() {
        CommonFuncs.endSelenium(selenium);
       
    }
    
    @Test
    public void createAndImport() throws Exception
    {
        navigateToTermbase();
        term = new TerminologyFuncs();
        String className = getClass().getName();
        String importDir = ConfigUtil.getConfigData("Base_Path")
                + ConfigUtil.getDataInCase(className, "termbase_file_dir");
        File dir = new File(importDir);
        if (dir.isDirectory())
        {
            String[] subFolderNames = dir.list();
            for (String termBaseName : subFolderNames)
            {
                String fileNameString = ConfigUtil.getDataInCase(className, termBaseName + "_file_names");
                String fileLanguageString = ConfigUtil.getDataInCase(className, termBaseName + "_file_lang");
                String fileLanguageSort = ConfigUtil.getDataInCase(className, termBaseName + "_file_lang_sort");
                String fileFields = ConfigUtil.getDataInCase(className, termBaseName + "_file_fields");
                String termNumber = ConfigUtil.getDataInCase(className, termBaseName + "_term_number");
                
                String[] fileNames = fileNameString == null ? null : fileNameString.split("\\|");
                String[] fileLangs = fileLanguageString == null ? null : fileLanguageString.split("\\|");
                String[] fileLangSorts = fileLanguageSort == null ? null : fileLanguageSort.split("\\|");
                String[] fileLangFields = fileFields == null ? null : fileFields.split("\\|");
                String[] termNumbers = termNumber == null ? null : termNumber.split("\\|");
                
                if (fileNames != null)
                {
                    for (int i = 0; i < fileNames.length; i++)
                    {
                        String newTermBaseName = "";
                        String newTermBaseLang = "";
                        String newTermBaseLangSort = "";
                        String newTermBaseLangFields = "";
                        
                        try
                        {
                            newTermBaseName = termBaseName + (i + 1);
                            if (fileLangSorts != null) newTermBaseLangSort = fileLangSorts[i];
                            if (fileLangFields != null) newTermBaseLangFields = fileLangFields[i];
                            if (fileLangs != null) newTermBaseLang = fileLangs[i];
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            newTermBaseLang = "";
                        }
                        term.create(selenium, newTermBaseName, newTermBaseLang,
                                newTermBaseLangSort, newTermBaseLangFields);
                        String filePath = ConfigUtil.getConfigData("Base_Path")
                                + ConfigUtil.getDataInCase(className, "termbase_file_dir");
                        term.importData(selenium, filePath, fileNames[i],
                                newTermBaseName, termBaseName, newTermBaseLang);
                        term.statistics(selenium, newTermBaseName, termNumbers[i]);
                    }
                }
            }
        }
    }
    
    
    /**
     * navigate to termbase page
     */
    private void navigateToTermbase()
    {
        selenium.open(ConfigUtil.getDataInCase(getClass().getName(), "terminology_url"));
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
