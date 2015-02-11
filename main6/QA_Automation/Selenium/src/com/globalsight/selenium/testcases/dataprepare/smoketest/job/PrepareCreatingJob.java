package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

/*
 * TestCaseName: CreateRates.java
 * Author:Jester
 * Tests:Create_Rates()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-8  First Version  Jester
 */

import java.util.ArrayList;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

/**
 * Prepare for Creating Job
 * 
 * @author leon
 * 
 */
public class PrepareCreatingJob extends BaseTestCase
{

    private Selenium selenium;
    private RatesFuncs iRatesFuncs = new RatesFuncs();
    private UsersFuncs iUsersFuncs = new UsersFuncs();
    TMProfileFuncs iTMProfileFuncs = new TMProfileFuncs();
    TMFuncs iTMFuncs = new TMFuncs();
    private TerminologyFuncs term = new TerminologyFuncs();
    private ProjectsFuncs iProjectsFuncs = new ProjectsFuncs();
    private LocalizationFuncs localizationFuncs = new LocalizationFuncs();
    private FilterConfigurationFuncs iFilterConfigurationFuncs = new FilterConfigurationFuncs();
    private FileProfileFuncs fp = new FileProfileFuncs();
    private WorkflowsFuncs wf = new WorkflowsFuncs();
    String testCaseName = getClass().getName();
   
    @Test
    public void prepareCreatingJob() throws Exception
    {
        // create Rate
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Rates_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        iRatesFuncs.newRate(selenium,
                ConfigUtil.getDataInCase(testCaseName, "TRANSRATE"));

        iRatesFuncs.newRate(selenium,
                ConfigUtil.getDataInCase(testCaseName, "VIEWERRATE"));
        // create user
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Users_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        iUsersFuncs.newUsers(selenium,
                ConfigUtil.getDataInCase(testCaseName, "USERPREPARE"));

        iUsersFuncs.newUsers(selenium,
                ConfigUtil.getDataInCase(testCaseName, "USERPREPARE2"));

        // create TM
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        iTMFuncs.newTM(selenium,
                ConfigUtil.getDataInCase(testCaseName, "TMPREPARE"));
        iTMFuncs.importTM(selenium,
                ConfigUtil.getDataInCase(testCaseName, "TMIMFILEPREPARE"));

        // create TM Profile
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemoryProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        iTMProfileFuncs.newTMProfile(selenium,
                ConfigUtil.getDataInCase(testCaseName, "TMPROFILEPROPARE"));

        // create Termbase
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Terminology_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        term.create(selenium,
                ConfigUtil.getDataInCase(testCaseName, "TERMBASEPROPARE"));

        // create project
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Projects_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(Projects.New_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(Projects.ProjectManager_SELECT,
                ConfigUtil.getConfigData("pm"));
        iProjectsFuncs.newProject(selenium,
                ConfigUtil.getDataInCase(testCaseName, "PROJECTPROPARE"));
        //import workflow
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Workflows_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        wf.importWorkFlow(selenium, ConfigUtil.getPath(getClassName(), "FILEPATH"), ConfigUtil.getDataInCase(testCaseName, "IMPORTPROFILE"),"Pwf_en_US_de_DE");
        
        // create Localization Profile
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.LocalizationProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        localizationFuncs.create2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "LocalizationPROPARE"));
        // create filter
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FilterConfiguration_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        iFilterConfigurationFuncs.newFilters2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "FILTERSDOC"),
                "msofficedoc");
        iFilterConfigurationFuncs.newFilters2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "FILTERSExcel"),
                "msofficeexcel");
        iFilterConfigurationFuncs.newFilters2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "FILTERSPowerPoint"),
                "msofficepowerpoint");
        iFilterConfigurationFuncs.newFilters2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "FILTERSOpenOffice"),
                "openoffice");

        iFilterConfigurationFuncs.newFilters2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "FILTERSxlsword2010"),
                "msoffice2010");

        iFilterConfigurationFuncs.newFilters2(selenium,
                ConfigUtil.getDataInCase(testCaseName, "FILTERSpptx2010"),
                "msoffice2010");

        // create File Profile
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FileProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        ArrayList<String> array = new ArrayList<String>();
        array.add("preparejob_file_profile_names");
        array.add("preparejob_localization_names");
        array.add("preparejob_source_file_format");
        array.add("preparejob_file_extensions");
        array.add("preparejob_file_descriptions");

        fp.setup(array);
        fp.create(selenium, testCaseName);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
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
}
