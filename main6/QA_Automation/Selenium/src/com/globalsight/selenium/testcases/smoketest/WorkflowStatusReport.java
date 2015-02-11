package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.WorkflowStatusReportWebForm;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Workflow Status Report
 * 
 * @author leon
 * 
 */
public class WorkflowStatusReport extends BaseTestCase
{
    private int i = 0;

    @Test
    public void generateReport()
    {
        selenium.click(MainFrame.REPORTS_MENU);
        selenium.click(MainFrame.REPORTS_MAIN_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(WorkflowStatusReportWebForm.REPORT_LINK);
        selenium.selectWindow(WorkflowStatusReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(WorkflowStatusReportWebForm.Submit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.windowMaximize();

        takePicture();

        while (selenium.isEditable(WorkflowStatusReportWebForm.NEXT_BUTTON))
        {
            selenium.click(WorkflowStatusReportWebForm.NEXT_BUTTON);
            takePicture();
        }
    }

    /**
     * Init options
     */
    private void initOptions()
    {
        String className = getClass().getName();
        String workflowStatus = ConfigUtil.getDataInCase(className,
                "workflowStatus");
        String ProjectManager = ConfigUtil.getDataInCase(className,
                "ProjectManager");
        String currency = ConfigUtil.getDataInCase(className, "currency");

        selenium.select(WorkflowStatusReportWebForm.WFSTATUS, workflowStatus);
        selenium.select(WorkflowStatusReportWebForm.PROJECTMANAGER, ProjectManager);
        selenium.select(WorkflowStatusReportWebForm.CURRENCY, currency);
    }

    /**
     * Take pictures
     * 
     * @param i
     */
    private void takePicture()
    {
        Number x = 0;
        Number y = 0;
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\WorkflowStatusReport\\WorkflowStatusReport_" + i
                + ".jpg");
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\WorkflowStatusReport\\WorkflowStatusReport_"
                        + i + ".jpg");
        Assert.assertTrue(file.exists());
        i++;
    }
}
