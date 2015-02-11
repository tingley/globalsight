package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateFileExtension.java
 * Author:Jester
 * Tests:Create_FileExtension()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

import org.testng.Assert;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.FileExtensionFuncs;
import com.globalsight.selenium.pages.FileExtensions;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateFileExtension extends BaseTestCase
{

    /*
     * Common variables initialization.
     */
    private FileExtensionFuncs fileExtensionFuncs = new FileExtensionFuncs();

    @Test
    public void createFileExtension() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILE_EXTENSION_SUBMENU);

        String extension = getProperty("file.extension");
        fileExtensionFuncs.newFileExtension(selenium, extension);

        Assert.assertEquals(fileExtensionFuncs.isPresentInTable(selenium,
                FileExtensions.MAIN_TABLE, extension), true);
    }
}
