package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class FileProfile_Remove extends BaseTestCase
{
    private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();;

    @Test
    public void removeFileProfile() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILE_PROFILES_SUBMENU);

        fileProfileFuncs.remove(selenium, getProperty("fileProfile.names"));
    }
}
