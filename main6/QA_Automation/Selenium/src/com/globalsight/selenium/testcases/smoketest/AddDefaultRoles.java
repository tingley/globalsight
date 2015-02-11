package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class AddDefaultRoles extends BaseTestCase
{

    private UsersFuncs usersFuncs = new UsersFuncs();
    private ProjectsFuncs projectsFuncs = new ProjectsFuncs();

    @Test
    public void addDefaultRolse() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);

        // Create a new project
        String projectName = projectsFuncs.newProject(selenium,
                getProperty("defaultRole.project"));
//        if (projectName != null)
//        {
//            Assert.assertEquals(projectsFuncs.isPresentInTable(selenium,
//                    Projects.PROJECT_TABLE, projectName), true);
//        }
//        else
//        {
//            Reporter.log("the project creation failed!");
//            return;
//        }
//
//        // import workflow
//        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.WORKFLOWS_SUBMENU);
//        WorkflowsFuncs WorkflowsFuncs = new WorkflowsFuncs();
//        String filePath = ConfigUtil.getConfigData("Base_Path")
//                + File.separator + getProperties("defaultRole.filePath");
//        WorkflowsFuncs.importWorkFlow(selenium, filePath,
//                getProperties("defaultRole.importProfile"),
//                "wf_suser_en_US_de_DE");

        // Create Superusers
        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithSuperAdmin(selenium);

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);
        String newUsername = usersFuncs.newSuperUsers(selenium, getProperty("defaultRole.user"));

        // Edit DefaultRoles
        usersFuncs.editDefaultRoles(selenium,
                newUsername,
                getProperty("defaultRole.sourceLocale"),
                getProperty("defaultRole.targetLocale"));

        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAdmin(selenium);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.PROJECTS_SUBMENU);
        if (projectsFuncs.selectProject(selenium, projectName))
        {
            clickAndWait(selenium, Projects.EDIT_BUTTON);
            clickAndWait(selenium, Projects.USER_BUTTON);
            clickAndWait(selenium, Projects.USER_DONE_BUTTON);
            clickAndWait(selenium, Projects.SAVE_BUTTON);
        }

        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithSuperAdmin(selenium);

        // Verify Default Roles feature works.
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);
        usersFuncs.verifyRoles(selenium, newUsername,
                getProperty("defaultRole.sourceLocale"),
                getProperty("defaultRole.targetLocale"));
        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAdmin(selenium);
    }
}
