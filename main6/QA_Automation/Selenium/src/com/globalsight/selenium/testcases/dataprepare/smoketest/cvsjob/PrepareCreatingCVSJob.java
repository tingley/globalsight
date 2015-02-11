package com.globalsight.selenium.testcases.dataprepare.smoketest.cvsjob;

import java.util.ArrayList;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CVSFuncs;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * Prepare the environment for create cvs job, some data depends on createjobs
 * 
 * @author leon
 * 
 */
public class PrepareCreatingCVSJob extends BaseTestCase
{
    private CVSFuncs cvsFuncs = new CVSFuncs();
    private String CVSServerName = "CVSServer";

    @Test
    public void prepareCVSJob()
    {

        // Create CVSServer
        createCVSServer();

        // Create CVS Module
        createCVSModule();

        // Create CVS Module Mapping(7 different type)
        createCVSModuleMapping();

        // Create CVS File Profile
        createCVSFileProfile();
    }

    /**
     * Create CVSServer
     */
    private void createCVSServer()
    {
        cvsFuncs.createCVSServer(selenium, CVSServerName);
    }

    /**
     * Create CVS Module
     */
    private void createCVSModule()
    {
        String moduleForGS01 = "module01";
        String moduleInCVSServer01 = "GSCVSAutomation/module01";
        String moduleForGS02 = "module02";
        String moduleInCVSServer02 = "GSCVSAutomation/module02";
        cvsFuncs.createCVSModule(selenium, CVSServerName, moduleForGS01,
                moduleInCVSServer01);
        cvsFuncs.createCVSModule(selenium, CVSServerName, moduleForGS02,
                moduleInCVSServer02);
    }

    /**
     * Create CVS Module Mapping(7 different type)
     */
    private void createCVSModuleMapping()
    {
        String CVSServer = "CVSServer-CVSTest";
        String sourceLocale = "English (United States) [en_US]";
        String targetLoacle = "French (France) [fr_FR]";

        ArrayList<Module> moduleList = new ArrayList<Module>();
        Module file2folder = new Module(
                "sandBox\\module01\\GSCVSAutomation\\module01\\file2folder\\en_US\\file2folder.html",
                "sandBox\\module01\\GSCVSAutomation\\module01\\file2folder\\folder",
                "*.html", "*.file2folder.html");
        Module folder2folder = new Module(
                "sandBox\\module01\\GSCVSAutomation\\module01\\folder2folder\\en_US",
                "sandBox\\module01\\GSCVSAutomation\\module01\\folder2folder\\folder",
                "*.html", "*.folder2folder.html");
        Module folder2folderdifferentmodule = new Module(
                "sandBox\\module01\\GSCVSAutomation\\module01\\folder2folderdifferentmodule\\en_US",
                "sandBox\\module02\\GSCVSAutomation\\module02\\folder2folderdifferentmodule\\folder",
                "*.html", "*.folder2folderdifferentmodule.html");
        Module subfolder2folder = new Module(
                "sandBox\\module01\\GSCVSAutomation\\module01\\subfolder2folder\\en_US",
                "sandBox\\module01\\GSCVSAutomation\\module01\\subfolder2folder\\folder",
                "*.html", "*.subfolder2folder.html");
        Module subfolder2subfolder = new Module(
                "sandBox\\module01\\GSCVSAutomation\\module01\\subfolder2subfolder\\en_US",
                "sandBox\\module01\\GSCVSAutomation\\module01\\subfolder2subfolder\\folder",
                "*.html", "*.subfolder2subfolder.html");
        Module subfolder2subfolderdifferentmodule = new Module(
                "sandBox\\module01\\GSCVSAutomation\\module01\\subfolder2subfolderdifferentmodule\\en_US",
                "sandBox\\module02\\GSCVSAutomation\\module02\\subfolder2subfolderdifferentmodule\\folder",
                "*.html", "*.subfolder2subfolderdiffmodule.html");

        moduleList.add(file2folder);
        moduleList.add(folder2folder);
        moduleList.add(folder2folderdifferentmodule);
        moduleList.add(subfolder2folder);
        moduleList.add(subfolder2subfolder);
        moduleList.add(subfolder2subfolderdifferentmodule);

        for (Module module : moduleList)
        {
            cvsFuncs.createCVSModuleMapping(selenium, CVSServer, sourceLocale,
                    targetLoacle, module.getSourceModule(),
                    module.getTargetModule(), module.getSourceName(),
                    module.getTargetName());
        }
    }

    /**
     * createCVSFileProfile();
     */
    private void createCVSFileProfile()
    {
        String project = "Template";
        String server = "CVSServer-CVSTest-module01";
        String srcLocale = "English (United States) [en_US]";
        String htmlFileProfile = "HTMLSmoke";
        cvsFuncs.createCVSFileProfiles(selenium, project, server, srcLocale,
                htmlFileProfile);
    }

    /**
     * Used for CVS module mapping
     * 
     * @author leon
     * 
     */
    private class Module
    {
        private String sourceModule;
        private String targetModule;
        private String sourceName;
        private String targetName;

        public Module(String p_sourceModule, String p_targetModule,
                String p_sourceName, String p_targetName)
        {
            sourceModule = p_sourceModule;
            targetModule = p_targetModule;
            sourceName = p_sourceName;
            targetName = p_targetName;
        }

        public String getSourceModule()
        {
            return sourceModule;
        }

        public String getTargetModule()
        {
            return targetModule;
        }

        public String getSourceName()
        {
            return sourceName;
        }

        public String getTargetName()
        {
            return targetName;
        }
    }
}
