package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
 * TestCaseName: MyActivitiesJobEditorVerify.java 
 * Author:Jester
 * Tests:verifyPopupEditor()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class MyActivitiesJobEditorVerify extends BaseTestCase
{
    /*
     * Common Variables
     */
    private MyAccountFuncs myAccountFuncs = new MyAccountFuncs();
    private CreateJobs createJobs = new CreateJobs();
    private String jobName = null;

    public void verifyPopupEditor()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_INPROGRESS_SUBMENU);

        jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        // Open the MainEditor
        selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/a");
        selenium.waitForPopUp(JobEditors.MAIN_EDITOR_TAG,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);

        // Open SegmentsEditor
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);
        selenium.selectFrame(JobEditors.TARGET_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);
        selenium.click(JobEditors.Segments_TABLE + "/tr[2]/td[2]/a");
        selenium.waitForPopUp(JobEditors.SEGMENT_EDITOR_TAG,
                CommonFuncs.SHORT_WAIT);

        // edit the content.
        selenium.selectWindow("name=" + JobEditors.SEGMENT_EDITOR_TAG);
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.TARGET_FRAME);
        selenium.selectFrame(JobEditors.EDITOR_FRAME);
        selenium.type(JobEditors.EDIT_TEXT, getDataInCase("string"));

        // Close the SegmentsEditor
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.MENU_WINDOW);
        selenium.click(JobEditors.SEGMENT_CLOSE_LINK);

        // Verify the change worked.
        selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);
        selenium.selectFrame(JobEditors.TARGET_FRAME);
        selenium.selectFrame(JobEditors.CONTENT_FRAME);
        Assert.assertEquals(
                selenium.getText(JobEditors.Segments_TABLE + "/tr[2]/td[2]/a"),
                ConfigUtil.getDataInCase(getClassName(), "verifyString"));

        // Close the MainEditor
        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
        selenium.selectFrame(JobEditors.MENU_WINDOW);
        selenium.click(JobEditors.SEGMENT_CLOSE_LINK);
        selenium.selectWindow(null);
    }

    @Test
    public void verifyInlineEditor()
    {
        myAccountFuncs.changeDefaultEditor(selenium, "Inline");

        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_INPROGRESS_SUBMENU);
        
        jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        // Open the MainEditor
        selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/a");
        selenium.waitForPopUp(JobEditors.INLINE_EDITOR_TAG,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name=" + JobEditors.INLINE_EDITOR_TAG);

        selenium.click(JobEditors.INLINE_SEGMENT_TABLE + "/p[2]");
        if (selenium.isAlertPresent()) {
        	selenium.getAlert();
        } else {
	        selenium.selectFrame(JobEditors.INLINE_EDITOR_FRAME);
	        selenium.type("//body", getDataInCase("stringLine"));
	        selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
	        selenium.click(JobEditors.CHANGE_SAVE_BUTTON);
	        Assert.assertEquals(
	                selenium.getText(JobEditors.INLINE_SEGMENT_TABLE + "/p[2]"),
	                getDataInCase("stringLine"));
        }
        selenium.click(JobEditors.INLINE_CLOSE_BUTTON);
        selenium.selectWindow(null);

        //myAccountFuncs.changeDefaultEditor(selenium, "Popup");
    }

    @BeforeClass
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
    }

    @AfterClass
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }
}
