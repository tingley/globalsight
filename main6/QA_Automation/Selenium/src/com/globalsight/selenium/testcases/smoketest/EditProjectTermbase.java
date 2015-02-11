package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditProjectTermbase extends BaseTestCase
{
    private ProjectsFuncs projectFuncs = new ProjectsFuncs();

    @Test
    public void editTermBase() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);

        String projectName = getProperty("project.newName");
        String termbaseName = getProperty("project.termbase");

        projectFuncs.editProjectTermbase(selenium, projectName, termbaseName);
    }
}
