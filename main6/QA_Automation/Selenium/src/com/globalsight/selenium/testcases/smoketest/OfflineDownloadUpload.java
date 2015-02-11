package com.globalsight.selenium.testcases.smoketest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import junit.framework.Assert;

import org.openqa.selenium.io.Zip;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.OfflineDownload;
import com.globalsight.selenium.pages.OfflineUpload;
import com.thoughtworks.selenium.Selenium;

/**
 * Test the offline download and upload
 * 
 * @author leon
 * 
 */
public class OfflineDownloadUpload
{
    private Selenium selenium;
    private String jobName = "OfflineDownloadUploadJob";
    private String jobNameLink = "link=OfflineDownloadUploadJob";

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        CommonFuncs.loginSystemWithAnyone(selenium);
    }

    @AfterClass
    public void afterClass()
    {
        selenium.stop();
    }

    /**
     * Test with:
     * 
     * Format: Bilingual TRADOS&reg; RTF
     * 
     * Insert resource as:Annotations
     * 
     */
    @Test
    public void offlineDownloadUploadBilingualRTF()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);
        selenium.click(OfflineDownload.POPULATEFUZZYCHECKBOX);
        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check RTF file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));
        File inboxFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile01.html.rtf");
        File inboxFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile02.txt.rtf");
        Assert.assertTrue(inboxFile1.exists());
        Assert.assertTrue(inboxFile2.exists());

        // Upload file
        uploadFile(inboxFile1);

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Test with:
     * 
     * Format: RTF (list view)
     * 
     * Insert resource as:Link
     * 
     * Terminology:GlobalSight Format
     * 
     */
    @Test
    public void offlineDownloadUploadRTFListView()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);

        selenium.select(OfflineDownload.FORMAT, "label=RTF (list view)");
        selenium.select(OfflineDownload.TMXTYPESELECTOR, "label=Link");
        selenium.select(OfflineDownload.TERMTYPESELECTOR,
                "label=GlobalSight Format");

        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));
        File inboxFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile01.html.rtf");
        File inboxFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile02.txt.rtf");
        File terminologyFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile01.html.xml");
        File terminologyFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile02.txt.xml");

        Assert.assertTrue(inboxFile1.exists());
        Assert.assertTrue(inboxFile2.exists());
        Assert.assertTrue(terminologyFile1.exists());
        Assert.assertTrue(terminologyFile2.exists());

        // Upload file
        uploadFile(inboxFile1);

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Test with:
     * 
     * Format: RTF (paragraph view)
     * 
     * Insert resource as:TMX File - Plain Text
     * 
     * Terminology:HTML
     * 
     */
    @Test
    public void offlineDownloadUploadRTFParagraphView()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);

        selenium.select(OfflineDownload.FORMAT, "label=RTF (paragraph view)");
        selenium.select(OfflineDownload.TMXTYPESELECTOR,
                "label=TMX File - Plain Text");
        selenium.select(OfflineDownload.TERMTYPESELECTOR, "label=HTML");

        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check result file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));
        File inboxFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile01.html.rtf");
        File inboxFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile02.txt.rtf");
        File terminologyFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile01.html.html");
        File terminologyFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile02.txt.html");
        File tmxFile1 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName
                + "\\tmx\\plain text\\OfflineDownloadUploadTestFile01.html.tmx");
        File tmxFile2 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName
                + "\\tmx\\plain text\\OfflineDownloadUploadTestFile02.txt.tmx");

        Assert.assertTrue(inboxFile1.exists());
        Assert.assertTrue(inboxFile2.exists());
        Assert.assertTrue(terminologyFile1.exists());
        Assert.assertTrue(terminologyFile2.exists());
        Assert.assertTrue(tmxFile1.exists());
        Assert.assertTrue(tmxFile2.exists());

        // Upload file
        uploadFile(inboxFile1);

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Test with:
     * 
     * Format: Text
     * 
     * Placeholder Format:Verbose
     * 
     * Insert resource as:TMX File - 1.4b
     * 
     * Terminology:TBX
     * 
     */
    @Test
    public void offlineDownloadUploadText()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);

        selenium.select(OfflineDownload.FORMAT, "label=Text");
        selenium.select(OfflineDownload.PLACEHOLDER, "label=Verbose");
        selenium.select(OfflineDownload.TMXTYPESELECTOR,
                "label=TMX File - 1.4b");
        selenium.select(OfflineDownload.TERMTYPESELECTOR, "label=TBX");

        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));
        File inboxFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile01.html.txt");
        File inboxFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile02.txt.txt");
        File terminologyFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile01.html.tbx");
        File terminologyFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile02.txt.tbx");

        File tmxFile1 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName + "\\tmx\\1.4b\\OfflineDownloadUploadTestFile01.html.tmx");
        File tmxFile2 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName + "\\tmx\\1.4b\\OfflineDownloadUploadTestFile02.txt.tmx");

        Assert.assertTrue(inboxFile1.exists());
        Assert.assertTrue(inboxFile2.exists());
        Assert.assertTrue(terminologyFile1.exists());
        Assert.assertTrue(terminologyFile2.exists());
        Assert.assertTrue(tmxFile1.exists());
        Assert.assertTrue(tmxFile2.exists());

        // Check the content
        String contentWithNoTag = "Sample Document";
        String contentWithTag = "[font1][bold]This is a sample page for Demo purposes.[/font1][/bold]";
        String fileContent = fileRead.getFileContent(inboxFile1);
        Assert.assertTrue(fileContent.indexOf(contentWithNoTag) > 0);
        Assert.assertTrue(fileContent.indexOf(contentWithTag) > 0);

        // Upload file
        uploadFile(inboxFile1);

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Test with:
     * 
     * Format: TTX
     * 
     * Placeholder Format:Verbose
     * 
     * Insert resource as:TMX File - both
     * 
     * 
     * 
     */
    @Test
    public void offlineDownloadUploadTTX()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);

        selenium.select(OfflineDownload.FORMAT, "label=TTX");
        selenium.select(OfflineDownload.TMXTYPESELECTOR,
                "label=TMX File - both");
        try
        {
            selenium.select(OfflineDownload.TERMTYPESELECTOR,
                    URLDecoder.decode("label=TRADOS MultiTerm*", "en_US"));
        }
        catch (UnsupportedEncodingException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));
        File inboxFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile01.html.ttx");
        File inboxFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile02.txt.ttx");
        File terminologyFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile01.html.xml");
        File terminologyFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\terminology\\OfflineDownloadUploadTestFile02.txt.xml");

        File tmxFile1 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName + "\\tmx\\1.4b\\OfflineDownloadUploadTestFile01.html.tmx");
        File tmxFile2 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName + "\\tmx\\1.4b\\OfflineDownloadUploadTestFile02.txt.tmx");
        File tmxFile3 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName
                + "\\tmx\\plain text\\OfflineDownloadUploadTestFile01.html.tmx");
        File tmxFile4 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName
                + "\\tmx\\plain text\\OfflineDownloadUploadTestFile02.txt.tmx");

        Assert.assertTrue(inboxFile1.exists());
        Assert.assertTrue(inboxFile2.exists());
        Assert.assertTrue(terminologyFile1.exists());
        Assert.assertTrue(terminologyFile2.exists());
        Assert.assertTrue(tmxFile1.exists());
        Assert.assertTrue(tmxFile2.exists());
        Assert.assertTrue(tmxFile3.exists());
        Assert.assertTrue(tmxFile4.exists());

        // Check the content
        String contentWithNoTag = "<Tuv Lang=\"EN-US\">Sample Document</Tuv><Tuv Lang=\"FR-FR\">Sample Document</Tuv>";
        String contentWithTagSource = "<Tuv Lang=\"EN-US\"><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:f1\">[f1]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:strong\">[strong]</ut>This is a sample page for Demo purposes.<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:strong\">[/strong]</ut><ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:f1\">[/f1]</ut></Tuv>";
        String contentWithTagTarget = "<Tuv Lang=\"FR-FR\"><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:f1\">[f1]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:b\">[b]</ut>This is a sample page for Demo purposes.<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:f1\">[/f1]</ut><ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:b\">[/b]</ut></Tuv>";

        String fileContent = fileRead.getFileContent(inboxFile1);
        Assert.assertTrue(fileContent.indexOf(contentWithNoTag) > 0);
        Assert.assertTrue(fileContent.indexOf(contentWithTagSource) > 0);
        Assert.assertTrue(fileContent.indexOf(contentWithTagTarget) > 0);
        // Upload file
        uploadFile(inboxFile1);

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Test with:
     * 
     * Format: Xliff 1.2
     * 
     * Insert resource as:None
     * 
     * Terminology:None
     * 
     */
    @Test
    public void offlineDownloadUploadXLF()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);

        selenium.select(OfflineDownload.FORMAT, "label=Xliff 1.2");
        selenium.select(OfflineDownload.TMXTYPESELECTOR, "label=None");

        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));
        File inboxFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile01.html.xlf");
        File inboxFile2 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName
                + "\\inbox\\OfflineDownloadUploadTestFile02.txt.xlf");

        Assert.assertTrue(inboxFile1.exists());
        Assert.assertTrue(inboxFile2.exists());

        // Check the content
        String contentWithNoTagSource = "<source>Sample Document</source>";
        String contentWithNoTagTarget = "<target state=\"new\">Sample Document</target>";
        String contentWithTagSource = "<source><bpt i=\"1\" type=\"font\" x=\"1\">&lt;font size=\"3\" face=\"Arial, Helvetica, sans-serif\"&gt;</bpt><bpt i=\"2\" type=\"strong\" x=\"2\">&lt;strong&gt;</bpt>This is a sample page for Demo purposes.<ept i=\"2\">&lt;/strong&gt;</ept><ept i=\"1\">&lt;/font&gt;</ept></source>";
        String contentWithTagTarget = "<target state=\"new\"><bpt i=\"1\" type=\"font\" x=\"1\">&lt;font size=\"3\" face=\"Arial, Helvetica, sans-serif\"&gt;</bpt><bpt i=\"3\" type=\"bold\">&lt;b&gt;</bpt>This is a sample page for Demo purposes.<ept i=\"1\">&lt;/font&gt;</ept><ept i=\"3\">&lt;/b&gt;</ept></target>";
        String fileContent = fileRead.getFileContent(inboxFile1);
        Assert.assertTrue(fileContent.indexOf(contentWithNoTagSource) > 0);
        Assert.assertTrue(fileContent.indexOf(contentWithNoTagTarget) > 0);
        Assert.assertTrue(fileContent.indexOf(contentWithTagSource) > 0);
        Assert.assertTrue(fileContent.indexOf(contentWithTagTarget) > 0);

        // Upload file
        uploadFile(inboxFile1);

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Test with:
     * 
     * Format: Bilingual TRADOS&reg; RTF
     * 
     * Consolidate TMX Files
     * 
     * Insert resource as:TMX File - both
     * 
     * Consolidate Terminology Files
     * 
     * Terminology:HTML
     * 
     */
    @Test
    public void offlineDownloadUploadConsolidate()
    {
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // This job name is from the Temp properties file when data prepare
        selenium.click(jobNameLink);

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(JobDetails.WorkOffline);

        selenium.select(OfflineDownload.TMXTYPESELECTOR,
                "label=TMX File - both");
        selenium.click(OfflineDownload.CONSOLODATETMXCHECKBOX);
        selenium.select(OfflineDownload.TERMTYPESELECTOR, "label=HTML");
        selenium.click(OfflineDownload.CONSOLIDATETERMCHECKBOX);

        selenium.click(OfflineDownload.STARTDOWNLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

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
        File file = fileRead.getNewestFile();
        Assert.assertTrue(file.exists());
        Zip zip = new Zip();
        try
        {
            zip.unzip(file, file.getParentFile());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check file exists
        String fileName = file.getName().substring(0,
                file.getName().indexOf("."));

        File terminologyFile1 = new File(file.getParentFile().getAbsolutePath()
                + "\\" + fileName + "\\terminology\\" + jobName + ".html");
        File tmxFile1 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName + "\\tmx\\1.4b\\" + jobName + ".tmx");
        File tmxFile2 = new File(file.getParentFile().getAbsolutePath() + "\\"
                + fileName + "\\tmx\\plain text\\" + jobName + ".tmx");

        Assert.assertTrue(terminologyFile1.exists());
        Assert.assertTrue(tmxFile1.exists());
        Assert.assertTrue(tmxFile2.exists());

        // Delete file
        file.delete();
        fileRead.deleteDirectory(file.getParentFile().getAbsolutePath() + "\\"
                + fileName);
    }

    /**
     * Upload file
     * 
     * @param file
     */
    public void uploadFile(File file)
    {
        // Upload file
        selenium.click(OfflineDownload.DOWNLOADAGAIN);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(OfflineUpload.UPLOAD);
        try
        {
            Thread.sleep(15000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.type(OfflineUpload.FILEFILED, file.getAbsolutePath());
        selenium.click(OfflineUpload.STARTUPLOAD);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        // Check if there is some error
        Assert.assertFalse(selenium.isElementPresent(OfflineUpload.UPLOADERROR));
    }
}
