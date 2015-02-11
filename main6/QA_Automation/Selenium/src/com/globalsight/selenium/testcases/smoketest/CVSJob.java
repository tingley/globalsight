package com.globalsight.selenium.testcases.smoketest;

import java.io.File;
import junit.framework.Assert;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.properties.ConfigUtil;


/**
 * This class is used for test the job result, depend on the Prepare Creating
 * Job and CreatCVSJob.
 * 
 * This test case is only check the job results file have been committed to CVS
 * Server with right file name in the right directory.
 * 
 * @author leon
 * 
 */
public class CVSJob
{ 
    @Test
    public void CVSJobCheck()
    {
        String cvsServerHost = ConfigUtil.getConfigData("CVSServer_HOST");
        String cvsSharedDirectory = "\\\\" + cvsServerHost
                + "\\GSCVSAutomation\\";
        File file2folder = new File(
                cvsSharedDirectory
                        + "module01\\file2folder\\folder\\file2folder.file2folder.html,v");
        File folder2folder = new File(
                cvsSharedDirectory
                        + "module01\\folder2folder\\folder\\folder2folder.folder2folder.html,v");
        File nomapping = new File(cvsSharedDirectory
                + "module01\\nomapping\\en_US\\fr_FR\\nomapping.html,v");
        File subfolder2folder01 = new File(
                cvsSharedDirectory
                        + "module01\\subfolder2folder\\folder\\subfolder2folder01.subfolder2folder.html,v");
        File subfolder2folder02 = new File(
                cvsSharedDirectory
                        + "module01\\subfolder2folder\\folder\\folder\\subfolder2folder02.subfolder2folder.html,v");
        File subfolder2subfolder01 = new File(
                cvsSharedDirectory
                        + "module01\\subfolder2subfolder\\folder\\subfolder2subfolder01.subfolder2subfolder.html,v");
        File subfolder2subfolder02 = new File(
                cvsSharedDirectory
                        + "module01\\subfolder2subfolder\\folder\\folder\\subfolder2subfolder02.subfolder2subfolder.html,v");
        File folder2folderdifferentmodule = new File(
                cvsSharedDirectory
                        + "module02\\folder2folderdifferentmodule\\folder\\folder2FolderDifferentModule.folder2folderdifferentmodule.html,v");
        File subfolder2subfolderdifferentmodule01 = new File(
                cvsSharedDirectory
                        + "module02\\subfolder2subfolderdifferentmodule\\folder\\subfolder2subFolderDifferentModule01.subfolder2subfolderdiffmodule.html,v");
        File subfolder2subfolderdifferentmodule02 = new File(
                cvsSharedDirectory
                        + "module02\\subfolder2subfolderdifferentmodule\\folder\\folder\\subfolder2subFolderDifferentModule02.subfolder2subfolderdiffmodule.html,v");

        Assert.assertTrue(file2folder.exists());
        Assert.assertTrue(folder2folder.exists());
        Assert.assertTrue(nomapping.exists());
        Assert.assertTrue(subfolder2folder01.exists());
        Assert.assertTrue(subfolder2folder02.exists());
        Assert.assertTrue(subfolder2subfolder01.exists());
        Assert.assertTrue(subfolder2subfolder02.exists());
        Assert.assertTrue(folder2folderdifferentmodule.exists());
        Assert.assertTrue(subfolder2subfolderdifferentmodule01.exists());
        Assert.assertTrue(subfolder2subfolderdifferentmodule02.exists());

        // Delete files
        FileRead fileRead = new FileRead();
        file2folder.delete();
        folder2folder.delete();
        fileRead.deleteDirectory(cvsSharedDirectory
                + "module01\\nomapping\\en_US\\fr_FR");
        subfolder2folder01.delete();
        subfolder2folder02.delete();
        subfolder2subfolder01.delete();
        subfolder2subfolder02.delete();
        folder2folderdifferentmodule.delete();
        subfolder2subfolderdifferentmodule01.delete();
        subfolder2subfolderdifferentmodule02.delete();            
    }

}
