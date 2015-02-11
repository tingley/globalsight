package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CVSFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.CVSFileProfiles;
import com.globalsight.selenium.pages.CVSModule;
import com.globalsight.selenium.pages.CVSModuleMapping;
import com.globalsight.selenium.pages.CVSServer;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * This class is used for testing
 * 
 * CVSServer/CVS Module/CVS Module Mapping/CVS File Profiles create delete edit
 * 
 * @author leon
 * 
 */
public class CVSBasic
{
    private Selenium selenium;
    private BasicFuncs basicFuncs;
    private CVSFuncs cvsFuncs;
    private String cvsServerName = "cvsSeverTest";

    private String CVSServerNameInCVSMapping = "cvsSeverTest-CVSTest";
    private String moduleForGS = "moduleTest";
    private String moduleInCVSServer = "GSCVSAutomation/moduleTest";

    private String project = "Template";
    private String serverInFileProfile = "cvsSeverTest-CVSTest-moduleTest";
    private String srcLocale = "English (United States) [en_US]";
    private String htmlFileProfile = "HTMLSmoke";

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        CommonFuncs.loginSystemWithAdmin(selenium);
        basicFuncs = new BasicFuncs();
        cvsFuncs = new CVSFuncs();
    }

    @AfterClass
    public void afterClass()
    {
        selenium.stop();
    }

    /**
     * Create and edit CVS Server
     */
    @Test
    public void cvsServer()
    {
        // Create
        cvsFuncs.createCVSServer(selenium, "null");
        // Check the CVS Server have been created
        String CVSServer_HOST = ConfigUtil.getConfigData("CVSServer_HOST");
        String CVSServerRepositoryName = ConfigUtil
                .getConfigData("CVSServerRepositoryName");
        String CVSServerUserName = ConfigUtil
                .getConfigData("CVSServerUserName");
        String Sandbox = ConfigUtil.getConfigData("Sandbox");
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, "null", 2));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, CVSServer_HOST, 4));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, CVSServerRepositoryName, 5));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, CVSServerUserName, 6));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, Sandbox, 7));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Edit
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSServer.CVSServer_Table, "null");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.click(CVSServer.EDIT_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.getConfirmation();
        selenium.type(CVSServer.SERVERNAME, cvsServerName);
        selenium.click(CVSServer.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, cvsServerName, 2));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Create CVS Module
     */
    @Test(dependsOnMethods =
    { "cvsServer" })
    public void cvsModule()
    {
        // Create
        cvsFuncs.createCVSModule(selenium, cvsServerName, moduleForGS,
                moduleInCVSServer);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleForGS, 2));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleInCVSServer, 3));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModule.CVSMODULE_TABLE, cvsServerName, 4));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Create/Edit CVS module Mapping
     */
    @Test(dependsOnMethods =
    { "cvsModule" })
    public void cvsModuleMapping()
    {
        String sourceModule = "sandBox\\module01\\GSCVSAutomation\\moduleTest\\en_US";
        String targetModule = "sandBox\\module01\\GSCVSAutomation\\moduleTest\\folder";
        String sourceName = "*.html";
        String targetName = "*123.html";
        String sourceLocale = "English (United States) [en_US]";
        String targetLoacle = "French (France) [fr_FR]";
        cvsFuncs.createCVSModuleMapping(selenium, CVSServerNameInCVSMapping,
                sourceLocale, targetLoacle, sourceModule, targetModule,
                sourceName, targetName);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, sourceModule, 3));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, targetModule, 5));
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, sourceModule, 3);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.click(CVSModuleMapping.EDIT_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(CVSModuleMapping.SOURCEMODULE, "edit");
        selenium.type(CVSModuleMapping.TARGETMODULE, "edit");
        selenium.click(CVSModuleMapping.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 3));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 5));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Create/Edit CVS File Profile
     */
    @Test(dependsOnMethods =
    { "cvsModuleMapping" })
    public void createCVSFileProfiles()
    {
        cvsFuncs.createCVSFileProfiles(selenium, project, serverInFileProfile,
                srcLocale, htmlFileProfile);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, project, 2));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, moduleForGS, 3));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, "html", 4));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, htmlFileProfile, 5));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Delete test
     */
    @Test(dependsOnMethods =
    { "createCVSFileProfiles" })
    public void delete()
    {
        // delete file profile
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSFILEPROFILES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, project);
            selenium.click(CVSFileProfiles.REMOVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, project));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // delete module mapping
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSMODULEMAPPINGS_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 3);
            selenium.click(CVSModuleMapping.REMOVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 3));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // delete module
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSMODULES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleForGS);
            selenium.click(CVSModule.REMOVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleForGS));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // delete cvs server
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSSERVERS_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSServer.CVSServer_Table, cvsServerName);
            selenium.click(CVSServer.REMOVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, cvsServerName));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
