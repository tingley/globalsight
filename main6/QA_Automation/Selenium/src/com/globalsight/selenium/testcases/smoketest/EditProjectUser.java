package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditProjectUser extends BaseTestCase
{
    private ProjectsFuncs projectFuncs = new ProjectsFuncs();

    @Test
    public void editUserProject() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);

        String projectName = getProperty("project.newName");
        String addedUserName = getProperty("project.user.add");

        projectFuncs.editProjectUser(selenium, projectName, addedUserName);
    }
}
