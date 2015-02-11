package com.globalsight.selenium.testcases.smoketest;

import java.util.ArrayList;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateFileProfile extends BaseTestCase
{

    private FileProfileFuncs fp = new FileProfileFuncs();
    
    @Test
    public void createFileProfile()
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILE_PROFILES_SUBMENU);
        
        ArrayList<String> array = new ArrayList<String>();
        array.add(getProperty("fileProfile.names"));
        array.add(getProperty("fileProfile.localizationProfiles"));
        array.add(getProperty("fileProfile.sourceFileFormats"));
        array.add(getProperty("fileProfile.fileExtensions"));
        array.add(getProperty("fileProfile.fileDescriptions"));
        
        fp.setup(array);
        fp.create(selenium);
    }
}
