package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileExtensionFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class FileExtension_Remove extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private FileExtensionFuncs fileExtensionFuncs = new FileExtensionFuncs();

    @Test
    public void FileExtensionRemove() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILE_EXTENSION_SUBMENU);

        fileExtensionFuncs.removeFileExtension(selenium,
                getProperty("file.extension"));
    }
}
