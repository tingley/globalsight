package com.globalsight.selenium.testcases.smoketest;

import java.io.File;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class Terminology extends BaseTestCase
{
    private TerminologyFuncs terminologyFuncs;

    @Test
    public void createAndImport() throws Exception
    {
        navigateToTermbase();
        terminologyFuncs = new TerminologyFuncs();

        String importDir = ConfigUtil.getConfigData("Base_Path")
                + getProperty("tb.import.dir");

        File dir = new File(importDir);
        if (dir.isDirectory())
        {
            String[] subFolderNames = dir.list();
            for (String termBaseName : subFolderNames)
            {
                termBaseName = termBaseName.toLowerCase();
                String fileNameString = getProperty("tb." + termBaseName
                        + ".filenames");
                String fileLanguageString = getProperty("tb." + termBaseName
                        + ".languages");
                String fileLanguageSort = getProperty("tb." + termBaseName
                        + ".languages.sort");
                String fileFields = getProperty("tb." + termBaseName
                        + ".fields");
                String termNumber = getProperty("tb." + termBaseName
                        + ".termNumber");

                String[] fileNames = fileNameString == null ? null : fileNameString
                        .split("\\|");
                String[] fileLangs = fileLanguageString == null ? null : fileLanguageString
                        .split("\\|");
                String[] fileLangSorts = fileLanguageSort == null ? null : fileLanguageSort
                        .split("\\|");
                String[] fileLangFields = fileFields == null ? null : fileFields
                        .split("\\|");
                String[] termNumbers = termNumber == null ? null : termNumber
                        .split("\\|");

                if (fileNames != null)
                {
                    for (int i = 0; i < fileNames.length; i++)
                    {
                        String newTermBaseName = "";
                        String newTermBaseLang = "";
                        String newTermBaseLangSort = "";
                        String newTermBaseLangFields = "";

                        try
                        {
                            newTermBaseName = termBaseName + (i + 1);
                            if (fileLangSorts != null)
                                newTermBaseLangSort = fileLangSorts[i];
                            if (fileLangFields != null)
                                newTermBaseLangFields = fileLangFields[i];
                            if (fileLangs != null)
                                newTermBaseLang = fileLangs[i];
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            newTermBaseLang = "";
                        }
                        terminologyFuncs.create(selenium, newTermBaseName,
                                newTermBaseLang, newTermBaseLangSort,
                                newTermBaseLangFields);
                        terminologyFuncs.importData(selenium, importDir,
                                fileNames[i], newTermBaseName, termBaseName,
                                newTermBaseLang);
                        terminologyFuncs.statistics(selenium, newTermBaseName,
                                termNumbers[i]);
                    }
                }
            }
        }
    }

    /**
     * navigate to termbase page
     */
    private void navigateToTermbase()
    {
        selenium.open(getProperty("tb.url"));
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
