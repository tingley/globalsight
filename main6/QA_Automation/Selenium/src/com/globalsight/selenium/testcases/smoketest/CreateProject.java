package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateProject.java
 * Author:Jester
 * Tests:Create_Project()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

import org.testng.Assert;

import org.testng.Reporter;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateProject extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private ProjectsFuncs projectsFuncs = new ProjectsFuncs();

    @Test
    public void createProject() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);

        String project = projectsFuncs.newProject(selenium,
                getProperty("project.project"));

        if (project != null)
        {
            Assert.assertEquals(projectsFuncs.isPresentInTable(selenium,
                    Projects.PROJECT_TABLE, project), true);
        }
        else
        {
            Reporter.log("the project creation failed!");
        }
    }
}
