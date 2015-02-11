package com.globalsight.selenium.testcases.testmatrix;

import java.util.ArrayList;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class testFilters extends BaseTestCase
{
    private TMFuncs tmFuncs = new TMFuncs();
    private TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();
    private LocalizationFuncs localizationFuncs = new LocalizationFuncs();
    private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();

    /*
     * Create xls/word File Profile.
     */

    @Test
    public void prepareFilterJob() throws Exception
    {
        // Create TM
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        tmFuncs.newTM(selenium,
                getDataInCase("TMPREPARE"));

        // Create TMProfile
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

        tmProfileFuncs.newTMProfile(selenium,
                getDataInCase("TMPROFILEPROPARE"));

        // create Localization Profile
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        localizationFuncs.create2(selenium,
                getDataInCase("LocalizationPROPARE"));

        // create File Profile
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.FILE_PROFILES_SUBMENU);

        ArrayList<String> array = new ArrayList<String>();
        array.add(getDataInCase("preparejob_file_profile_names"));
        array.add(getDataInCase("preparejob_localization_names"));
        array.add(getDataInCase("preparejob_source_file_format"));
        array.add(getDataInCase("preparejob_file_extensions"));
        array.add(getDataInCase("preparejob_file_descriptions"));

        fileProfileFuncs.setup(array);
        fileProfileFuncs.create(selenium);
    }
}
