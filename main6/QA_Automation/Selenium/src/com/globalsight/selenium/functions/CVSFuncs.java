package com.globalsight.selenium.functions;

import com.globalsight.selenium.pages.CVSFileProfiles;
import com.globalsight.selenium.pages.CVSJobProgress;
import com.globalsight.selenium.pages.CVSModule;
import com.globalsight.selenium.pages.CVSModuleMapping;
import com.globalsight.selenium.pages.CVSServer;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * CVS Server create CREATE
 * 
 * CVS Module create CREATE
 * 
 * CVS Module mapping CREATE
 * 
 * CVS File Profile CREATE
 * 
 * CVS JOB CREATE
 * 
 * @author leon
 * 
 */
public class CVSFuncs
{
    private BasicFuncs basicFuncs = new BasicFuncs();

    /**
     * Create CVSServer
     * 
     * @param selenium
     * @param CVSServerName
     */
    public void createCVSServer(Selenium selenium, String cvsServerName)
    {

        String cvsServer_HOST = ConfigUtil.getConfigData("CVSServer_HOST");
        String cvsServerRepositoryName = ConfigUtil
                .getConfigData("CVSServerRepositoryName");
        String cvsServerUserName = ConfigUtil
                .getConfigData("CVSServerUserName");
        String cvsServerUSerPassword = ConfigUtil
                .getConfigData("CVSServerUSerPassword");
        String sandBox = ConfigUtil.getConfigData("Sandbox");

        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSSERVERS_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSServer.NEW_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(CVSServer.SERVERNAME, cvsServerName);
        selenium.type(CVSServer.HOSTIP, cvsServer_HOST);
        selenium.type(CVSServer.REPOSITORY, cvsServerRepositoryName);
        selenium.type(CVSServer.LOGINUSER, cvsServerUserName);
        selenium.type(CVSServer.LOGINPASSWORD, cvsServerUSerPassword);
        selenium.type(CVSServer.LOGINPASSWORDCONFIRM, cvsServerUSerPassword);
        selenium.type(CVSServer.SANDBOX, sandBox);
        selenium.click(CVSServer.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Create CVS Module
     * 
     * @param selenium
     */
    public void createCVSModule(Selenium selenium, String cvsServerName,
            String moduleNameForGS, String moduleNameInCVSServer)
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSMODULES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSModule.New_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(CVSModule.SELECTSERVER, "label=" + cvsServerName);
        selenium.type(CVSModule.SELFNAME, moduleNameForGS);
        selenium.type(CVSModule.MODULENAME, moduleNameInCVSServer);
        selenium.click(CVSModule.ADDMODULE_BUTTON);
        selenium.click(CVSModule.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        try
        {
            basicFuncs.selectRadioButtonFromTable(selenium,
                    CVSModule.CVSMODULE_TABLE, moduleNameForGS);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.click(CVSModule.CHECKOUT_BUTTON);
        selenium.getConfirmation();
        selenium.chooseOkOnNextConfirmation();
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSModule.DONE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Create CVS Module Mapping
     * 
     * @param selenium
     * @param CVSServer
     * @param sourceLocale
     * @param targetLoacle
     * @param sourceModule
     * @param targetModule
     * @param sourceName
     * @param targetName
     */
    public void createCVSModuleMapping(Selenium selenium, String cvsServer,
            String sourceLocale, String targetLoacle, String sourceModule,
            String targetModule, String sourceName, String targetName)
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSMODULEMAPPINGS_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSModuleMapping.New_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(CVSModuleMapping.CVSServer, cvsServer);
        selenium.select(CVSModuleMapping.SOURCELOCALE, sourceLocale);
        selenium.type(CVSModuleMapping.SOURCEMODULE, sourceModule);
        selenium.click(CVSModuleMapping.TARGETLOCALE0);
        selenium.type(CVSModuleMapping.TARGETMODULE0, targetModule);
        selenium.click(CVSModuleMapping.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        try
        {
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
        selenium.select(CVSModuleMapping.TARGETLOCALE, targetLoacle);

        selenium.click(CVSModuleMapping.NEWFILERENAME_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(CVSModuleMapping.SOURCENAME_INPUT, sourceName);
        selenium.type(CVSModuleMapping.TARGETNAME_INPUT, targetName);
        selenium.click(CVSModuleMapping.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSModuleMapping.SUBFOLDER_BUTTON);
        selenium.click(CVSModuleMapping.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Create CVS File Profiles
     * 
     * @param selenium
     * @param project
     * @param server
     * @param srcLocale
     * @param htmlFileProfile
     */
    public void createCVSFileProfiles(Selenium selenium, String project,
            String server, String srcLocale, String htmlFileProfile)
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.CVSFILEPROFILES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSFileProfiles.New_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(CVSFileProfiles.PROJECTS, "label=" + project);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(CVSFileProfiles.SERVERS, "label=" + server);
        selenium.select(CVSFileProfiles.SOURCELOCALES, "label=" + srcLocale);
        selenium.select(CVSFileProfiles.HTMFILEPROFILE, "label="
                + htmlFileProfile);
        selenium.click(CVSFileProfiles.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Create cvs job
     * 
     * @param selenium
     */
    public void createCVSJob(Selenium selenium, String jobName,
            String sourceLocale, String project, String cvsModule,
            String targetLocale)
    {
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.CreateCVSJob);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(CVSJobProgress.JOBNAME, jobName);
        selenium.select(CVSJobProgress.SRCLOCALES, "label=" + sourceLocale);
        selenium.select(CVSJobProgress.PROJECTS, "label=" + project);
        selenium.select(CVSJobProgress.CVSMODULE, "label=" + cvsModule);
        selenium.click(CVSJobProgress.NEXT_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CVSJobProgress.SLELECTALLTESTFILES);
        selenium.click(CVSJobProgress.NEXT2_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        for (int i = 1; i < 21; i++)
        {
            selenium.click("//div[@id='Filehtml']/div[" + i + "]");
            selenium.click(CVSJobProgress.MAP_BUTTON);
        }
        selenium.click(CVSJobProgress.NEXT3_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(CVSJobProgress.JOBNAME, jobName);
        selenium.removeAllSelections(CVSJobProgress.TARGETLOCALES);
        selenium.addSelection(CVSJobProgress.TARGETLOCALES, targetLocale);
        selenium.click(CVSJobProgress.CREATEJOB_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
