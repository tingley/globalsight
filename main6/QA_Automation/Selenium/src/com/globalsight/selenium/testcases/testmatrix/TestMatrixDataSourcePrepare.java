package com.globalsight.selenium.testcases.testmatrix;

import java.util.ArrayList;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

public class TestMatrixDataSourcePrepare extends BaseTestCase
{
    private TMFuncs tmFuncs = new TMFuncs();
    private TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();
    private LocalizationFuncs localizationFuncs = new LocalizationFuncs();
    private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
    private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
    
    @Test
    public void prepareMatrixJob() throws Exception
    {
        // Create TM
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        tmFuncs.newTM(selenium, getProperty(testMatrixFile,"DataSource.tmprepare"));

        // Create TMProfile
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

        tmProfileFuncs
                .newTMProfile(selenium, getProperty(testMatrixFile,"DataSource.tmProfile"));

        // create Localization Profile
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        localizationFuncs.create2(selenium,
        		getProperty(testMatrixFile,"DataSource.localizationProfile"));

        // create File Profile
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILE_PROFILES_SUBMENU);

        ArrayList<String> array = new ArrayList<String>();
        array.add(getProperty(testMatrixFile,"DataSource.fileProfile.names"));
        array.add(getProperty(testMatrixFile,"DataSource.fileProfile.localizationProfiles"));
        array.add(getProperty(testMatrixFile,"DataSource.fileProfile.sourceFileFormats"));
        array.add(getProperty(testMatrixFile,"DataSource.fileProfile.fileExtensions"));
        array.add(getProperty(testMatrixFile,"DataSource.fileProfile.descriptions"));

        fileProfileFuncs.setup(array);
        fileProfileFuncs.create(selenium);
    }
}
