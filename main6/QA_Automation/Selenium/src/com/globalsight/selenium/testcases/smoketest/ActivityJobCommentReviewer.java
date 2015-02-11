package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.MyAccountFuncs;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: ActivityJobComment.java 
 * Author:Jester 
 * Tests:addComment()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class ActivityJobCommentReviewer extends BaseTestCase
{

    /**
     * Common Variables
     */
    CreateJobs createJobs = new CreateJobs();

    @Test
    public void addComment() throws InterruptedException
    {
        MyAccountFuncs myAccountFuncs = new MyAccountFuncs();
        myAccountFuncs.changeDefaultEditor(selenium, "Popup");
        
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_INPROGRESS_SUBMENU);

        String jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        // open MainEditor
        selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/a");
        selenium.waitForPopUp(JobEditors.MAIN_EDITOR_TAG,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);

        // Open CommentEditor
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);
        selenium.selectFrame(JobEditors.TARGET_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);
        selenium.click(JobEditors.Segments_TABLE + "/tr/td[3]/a");
        //selenium.click(JobEditors.CommentEditor_TAG);
        selenium.selectWindow(JobEditors.CommentEditor_WINDOW);

        // Fill the comment.
        selenium.select(JobEditors.Priority_SELECTION,
                getDataInCase("priority"));
        selenium.select(JobEditors.Category_SELECTION,
                getDataInCase("category"));
        selenium.type(JobEditors.Title_TEXT_FIELD, getDataInCase("title"));
        selenium.type(JobEditors.Comment_TEXT_FIELD, getDataInCase("comment"));

        selenium.click(JobEditors.OK_BUTTON);

        // Check if the column exists.
        selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.TARGET_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);

        Assert.assertEquals(
                selenium.isElementPresent(JobEditors.Segments_TABLE
                        + "/tr/td[2]/img"), true);

        // Check if the comments exits.

        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.Review_FRAME);

        Assert.assertNotNull(selenium.getText(JobEditors.Comments_TABLE
                + "/tr/td[2]"));

        // close the mainEditor.
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.MENU_WINDOW);
        selenium.click(JobEditors.Close_LINK);
        selenium.selectWindow(null);
        
        myAccountFuncs.changeDefaultEditor(selenium, "Inline");
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.login(selenium, ConfigUtil.getConfigData("company")
                + ConfigUtil.getConfigData("reviewerName"),
                ConfigUtil.getConfigData("reviewerPassword"));
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }
}
