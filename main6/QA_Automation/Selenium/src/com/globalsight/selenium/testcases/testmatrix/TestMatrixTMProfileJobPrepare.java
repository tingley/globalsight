package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;

public class TestMatrixTMProfileJobPrepare extends BaseTestCase
{
    @Test
    public void prepareMatrixJob() throws Exception
    {
//        // Create Local Pairs
//        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.LOCALE_PAIRS_SUBMENU);
//        LocalePairsFuncs localePairsFuncs = new LocalePairsFuncs();
//        localePairsFuncs.newLocalPairs(selenium,
//                getDataInCase("sourceLocale1"), getDataInCase("targetLocale1"));
//
//        localePairsFuncs.newLocalPairs(selenium,
//                getDataInCase("sourceLocale2"), getDataInCase("targetLocale2"));
//
//        localePairsFuncs.newLocalPairs(selenium,
//                getDataInCase("sourceLocale3"), getDataInCase("targetLocale3"));
//
//        // Add User Roles
//        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.USERS_SUBMENU);
//        UsersFuncs usersFuncs = new UsersFuncs();
//        usersFuncs.editAddUserRoles(selenium, getDataInCase("user"));
//
//        // Create TM
//        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.TRANSLATION_MEMORY_SUBMENU);
//        TMFuncs tmFuncs = new TMFuncs();
//        tmFuncs.newTM(selenium, getDataInCase("TM0"));
//        tmFuncs.newTM(selenium, getDataInCase("TMS1"));
//        tmFuncs.newTM(selenium, getDataInCase("TMR1"));
//        tmFuncs.newTM(selenium, getDataInCase("TMR2"));
//        tmFuncs.newTM(selenium, getDataInCase("TMS21"));
//        tmFuncs.newTM(selenium, getDataInCase("TMS22"));
//        tmFuncs.newTM(selenium, getDataInCase("TMS23"));
//        tmFuncs.newTM(selenium, getDataInCase("TMS24"));
//
//        // Create TMProfile
//        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);
        TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();
//        tmProfileFuncs.newTMProfile(selenium, getDataInCase("TMP"));
//
//        tmProfileFuncs.createMTOptions(selenium, getDataInCase("MTOptons1"),
//                null);
//
//        // Duplicate workflow to frde, fren
//        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.WORKFLOWS_SUBMENU);
//        WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
//        workflowsFuncs.duplicateWorkFlow(selenium,
//                getDataInCase("newWorkflowName"),
//                getDataInCase("workflowTemplate"), getDataInCase("project"),
//                getDataInCase("source"), getDataInCase("target"));
//
//        // create Localization Profile
//        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
//        LocalizationFuncs localizationFuncs = new LocalizationFuncs();
//        localizationFuncs.create2(selenium, getDataInCase("L10n"));
//        localizationFuncs.create2(selenium, getDataInCase("L10nFR"));
//
//        // create File Profile
//        SeleniumUtils.openMenuItemAndWait(selenium,
//                MainFrame.DATA_SOURCES_MENU, MainFrame.FILE_PROFILES_SUBMENU);
//        FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
//
//        ArrayList<String> array = new ArrayList<String>();
//        array.add(getDataInCase("preparejob_file_profile_names"));
//        array.add(getDataInCase("preparejob_localization_names"));
//        array.add(getDataInCase("preparejob_source_file_format"));
//        array.add(getDataInCase("preparejob_file_extensions"));
//        array.add(getDataInCase("preparejob_file_descriptions"));
//
//        fileProfileFuncs.setup(array);
//        fileProfileFuncs.create(selenium);

        // Create a new job with google mt and export it to create TM data
        CreateJobsFuncs createJobsFuncs = new CreateJobsFuncs();
        String jobNameExt = null;
        String sourceFile = null;

        ArrayList<String[]> testCases = new ArrayList<String[]>();
        String filePath = ConfigUtil.getConfigData("Base_Path")
                + getDataInCase("TMPTestCasePath");
        File file = new File(filePath);
        Assert.assertTrue(file.exists());

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] testCase = line.split("\t");
            testCases.add(testCase);
        }
        br.close();

        int sizeOfTestCases = testCases.size();
        Assert.assertTrue(sizeOfTestCases > 0);

        String baseJobName = "TMPTestCase_";
        String jobName = "";
        String[] jobNames = new String[sizeOfTestCases];

        for (int i = 1; i < sizeOfTestCases; i++)
        {
            SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                    MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

            boolean selected = SeleniumUtils.selectRadioButtonFromTable(
                    selenium, TMProfile.TM_PROFILE_LIST_TABLE,
                    getDataInCase("TMP_For_TestCase_Prepare"));
            Assert.assertTrue(selected);

            SeleniumUtils.clickAndWait(selenium, TMProfile.EDIT_BUTTON);

            tmProfileFuncs.TMPOperation(selenium, testCases.get(i));

            switch (i)
            {
                case 1:
                    jobNameExt = "TMS1";
                    sourceFile = "SourceFilesForTMS1";
                    tmProfileFuncs.createMTOptions(selenium,
                            getDataInCase("MTOptons1"), null);
                    break;
                case 2:
                    jobNameExt = "TMR1";
                    sourceFile = "SourceFilesForTMR1";
                    break;
                case 3:
                    jobNameExt = "TMR2";
                    sourceFile = "SourceFilesForTMR2";
                    break;
                case 4:
                    jobNameExt = "TMS21";
                    sourceFile = "SourceFilesForTMS21";
                    tmProfileFuncs.createMTOptions(selenium,
                            getDataInCase("MTOptonsTMS21"), null);
                    break;
                case 5:
                    jobNameExt = "TMS22";
                    sourceFile = "SourceFilesForTMS21";
                    break;
                case 6:
                    jobNameExt = "TMS23";
                    sourceFile = "SourceFilesForTMS21";
                    break;
                case 7:
                    jobNameExt = "TMS24";
                    sourceFile = "SourceFilesForTMS21";
                    break;
                default:
            }

            // Create job
            jobName = createJobsFuncs.createJob(baseJobName + jobNameExt,
                    getDataInCase(sourceFile),
                    getDataInCase("fileProfileNamesStr"),
                    getDataInCase("targetLocales"));

            jobNames[i] = jobName;
        }

        CommonFuncs.loginSystemWithAnyone(selenium);

        JobActivityOperationFuncs opFuncs = new JobActivityOperationFuncs();
        for (int i = 1; i < sizeOfTestCases; i++)
        {
            // Accept and complete the activity
            opFuncs.acceptActivity(selenium, jobNames[i]);
            opFuncs.completeActivity(selenium, jobNames[i]);
            opFuncs.acceptActivity(selenium, jobNames[i]);
            opFuncs.completeActivity(selenium, jobNames[i]);
        }
    }
}
