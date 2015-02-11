package com.globalsight.selenium.testcases.smoketest;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.openqa.selenium.io.Zip;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.thoughtworks.selenium.Selenium;

/**
 * Files Delete Upload Download In Job, for add(applet) operation, add no test
 * case.
 * 
 * Job:filesJob(data prepare)
 * 
 * @author leon
 * 
 */
public class FilesAddDelUploadDownloadInJob
{
    private Selenium selenium;
    private BasicFuncs basicFuncs;
    private String jobNameLink = "link=filesJob";

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        basicFuncs = new BasicFuncs();
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterClass
    public void afterClass()
    {
        selenium.stop();
    }

    /**
     * Download/upload file
     */
    @Test
    public void downloadUploadFile()
    {
        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(JobDetails.FIRSTSOURCEFILE_CHECKBOX);
        selenium.click(JobDetails.DOWNLOAD_BUTTON);
        // Wait for the download progress finish.
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Read loaded file and unzip
        FileRead fileRead = new FileRead();
        File zipFile = fileRead.getFile("AllFiles.zip");
        Assert.assertTrue(zipFile.exists());
        File allFilesDirectory = new File(zipFile.getParentFile()
                .getAbsolutePath() + "\\" + "AllFiles");
        Zip zip = new Zip();
        try
        {
            zip.unzip(zipFile, allFilesDirectory);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // check the loaded file
        String[] files = allFilesDirectory.list();
        File file = new File(zipFile.getParentFile().getAbsolutePath() + "\\"
                + "AllFiles\\" + files[0]);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.getName().indexOf("demo_company.html") > 0);

        // Upload file

        selenium.click(JobDetails.UPLOAD_BUTTON);
        selenium.type(JobDetails.UPLOADFILEPATH_INPUT, file.getAbsolutePath());
        selenium.click(JobDetails.UPLOADDIALOG_BUTTON);
        // Wait for the upload progress finish.
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.refresh();
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    JobDetails.SOURCEFILESPAGE_TABLE, "HTMLSmoke", 2));
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    JobDetails.SOURCEFILESPAGE_TABLE, "105", 3));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Delete file
        zipFile.delete();
        fileRead.deleteDirectory(zipFile.getParentFile().getAbsolutePath()
                + "\\" + "AllFiles");
    }

    /**
     * delete file
     */
    @Test(dependsOnMethods =
    { "downloadUploadFile" })
    public void deleteFile()
    {
        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(JobDetails.FIRSTSOURCEFILE_CHECKBOX);
        selenium.click(JobDetails.DELETEFILES_BUTTON);
        selenium.getConfirmation();
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.refresh();
        try
        {
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    JobDetails.SOURCEFILESPAGE_TABLE, "HTMLSmoke", 2));
            Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                    JobDetails.SOURCEFILESPAGE_TABLE, "105", 3));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
