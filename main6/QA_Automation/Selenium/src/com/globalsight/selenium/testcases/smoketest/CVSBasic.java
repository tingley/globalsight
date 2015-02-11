package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CVSFuncs;
import com.globalsight.selenium.pages.CVSFileProfiles;
import com.globalsight.selenium.pages.CVSModule;
import com.globalsight.selenium.pages.CVSModuleMapping;
import com.globalsight.selenium.pages.CVSServer;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * This class is used for testing
 * 
 * CVSServer/CVS Module/CVS Module Mapping/CVS File Profiles create delete edit
 * 
 * @author leon
 * 
 */
public class CVSBasic extends BaseTestCase
{
    private BasicFuncs basicFuncs = new BasicFuncs();
    private CVSFuncs cvsFuncs = new CVSFuncs();
    private String cvsServerName = "cvsSeverTest";

    private String CVSServerNameInCVSMapping = "cvsSeverTest-CVSTest";
    private String moduleForGS = "moduleTest";
    private String moduleInCVSServer = "GSCVSAutomation/moduleTest";

    private String project = "Template";
    private String serverInFileProfile = "cvsSeverTest-CVSTest-moduleTest";
    private String srcLocale = "English (United States) [en_US]";
    private String htmlFileProfile = "HTMLSmoke";

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
        String Sandbox = ConfigUtil.getConfigData("sandbox");
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
            Reporter.log("Error found in CVSBasic. " + e.getMessage());
            return;
        }

        // Edit
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSServer.CVSServer_Table, "null");
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic. " + e.getMessage());
            return;
        }
        clickAndWait(selenium, CVSServer.EDIT_BUTTON);
        selenium.getConfirmation();
        selenium.type(CVSServer.SERVERNAME, cvsServerName);
        clickAndWait(selenium, CVSServer.SAVE_BUTTON);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, cvsServerName, 2));
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic. " + e.getMessage());
        }
    }

    /**
     * Create CVS Module
     */
    @Test(dependsOnMethods = { "cvsServer" })
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
            Reporter.log("Error found in CVSBasic.cvsModule(). "
                    + e.getMessage());
        }
    }

    /**
     * Create/Edit CVS module Mapping
     */
    @Test(dependsOnMethods = { "cvsModule" })
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
            Reporter.log("Error found in CVSBasic.cvsModuleMapping(). "
                    + e.getMessage());
            return;
        }
        clickAndWait(selenium, CVSModuleMapping.EDIT_BUTTON);
        selenium.type(CVSModuleMapping.SOURCEMODULE, "edit");
        selenium.type(CVSModuleMapping.TARGETMODULE, "edit");
        clickAndWait(selenium, CVSModuleMapping.SAVE_BUTTON);
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 3));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 5));
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic.cvsModuleMapping(). "
                    + e.getMessage());
        }
    }

    /**
     * Create/Edit CVS File Profile
     */
    @Test(dependsOnMethods = { "cvsModuleMapping" })
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
            Reporter.log("Error found in CVSBasic.createCVSFileProfiles(). "
                    + e.getMessage());
        }
    }

    /**
     * Delete test
     */
    @Test(dependsOnMethods = { "createCVSFileProfiles" })
    public void delete()
    {
        // delete file profile
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.CVS_FILE_PROFILES_SUBMENU);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, project);
            clickAndWait(selenium, CVSFileProfiles.REMOVE_BUTTON);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSFileProfiles.CVSFILEPROFILE_TABLE, project));
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic.delete(). " + e.getMessage());
            return;
        }
        // delete module mapping
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.CVS_MODULE_MAPPINGS_SUBMENU);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 3);
            clickAndWait(selenium, CVSModuleMapping.REMOVE_BUTTON);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSModuleMapping.CVSMODULEMAPPINGS_TABLE, "edit", 3));
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic.delete(). " + e.getMessage());
            return;
        }
        // delete module
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.CVS_MODULES_SUBMENU);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleForGS);
            clickAndWait(selenium, CVSModule.REMOVE_BUTTON);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleForGS));
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic.delete(). " + e.getMessage());
            return;
        }
        // delete cvs server
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.CVS_SERVERS_SUBMENU);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSServer.CVSServer_Table, cvsServerName);
            clickAndWait(selenium, CVSServer.REMOVE_BUTTON);
            selenium.getConfirmation();
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    CVSServer.CVSServer_Table, cvsServerName));
        }
        catch (Exception e)
        {
            Reporter.log("Error found in CVSBasic.delete(). " + e.getMessage());
            return;
        }
    }
}
