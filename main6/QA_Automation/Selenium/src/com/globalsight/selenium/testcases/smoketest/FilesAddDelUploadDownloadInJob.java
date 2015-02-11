package com.globalsight.selenium.testcases.smoketest;

import java.io.File;
import junit.framework.Assert;

import org.openqa.selenium.io.Zip;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * Files Delete Upload Download In Job, for add(applet) operation, add no test
 * case.
 * 
 * Job:filesJob(data prepare)
 * 
 * @author leon
 * 
 */
public class FilesAddDelUploadDownloadInJob extends BaseTestCase
{
    private BasicFuncs basicFuncs = new BasicFuncs();
    private String jobNameLink = "link=filesJob";

    /**
     * Download/upload file
     * 
     * @throws Exception
     */
    @Test
    public void downloadUploadFile() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_READY_SUBMENU);

        clickAndWait(selenium, jobNameLink);

        selenium.click(JobDetails.FIRSTSOURCEFILE_CHECKBOX);
        selenium.click(JobDetails.DOWNLOAD_BUTTON);

        // Wait for the download progress finish.
        Thread.sleep((long) 10000);

        // Read loaded file and unzip
        FileRead fileRead = new FileRead();
        File zipFile = fileRead.getFile("AllFiles.zip");
        Assert.assertTrue(zipFile.exists());
        File allFilesDirectory = new File(zipFile.getParentFile()
                .getAbsolutePath() + "\\" + "AllFiles");
        Zip zip = new Zip();
        zip.unzip(zipFile, allFilesDirectory);

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
        Thread.sleep((long) 10000);
        selenium.refresh();
        Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                JobDetails.SOURCEFILESPAGE_TABLE, "HTMLSmoke", 2));
        Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                JobDetails.SOURCEFILESPAGE_TABLE, "105", 3));
        // Delete file
        zipFile.delete();
        fileRead.deleteDirectory(zipFile.getParentFile().getAbsolutePath()
                + "\\" + "AllFiles");
    }

    /**
     * delete file
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods =
    { "downloadUploadFile" })
    public void deleteFile() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_READY_SUBMENU);

        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(JobDetails.FIRSTSOURCEFILE_CHECKBOX);
        selenium.click(JobDetails.DELETEFILES_BUTTON);
        selenium.getConfirmation();
        Thread.sleep((long) 10000);
        selenium.refresh();
        Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                JobDetails.SOURCEFILESPAGE_TABLE, "HTMLSmoke", 2));
        Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                JobDetails.SOURCEFILESPAGE_TABLE, "105", 3));
    }
}
