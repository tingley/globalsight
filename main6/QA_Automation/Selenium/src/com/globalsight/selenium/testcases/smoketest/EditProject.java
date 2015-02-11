package com.globalsight.selenium.testcases.smoketest;

//author : ShenYang   2011-06-27
//edit project name and project manager.

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditProject extends BaseTestCase
{
    private ProjectsFuncs projectFuncs = new ProjectsFuncs();

    @Test
    public void editProject() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);

        String projectName = getProperty("project.name");
        String newProjectName = getProperty("project.newName");
        String newProjectManager = ConfigUtil.getConfigData("admin");

        projectFuncs.editProject(selenium, projectName, newProjectName,
                newProjectManager);
    }
}
