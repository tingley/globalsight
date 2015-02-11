package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMProfileTestCase extends BaseTestCase
{
    private TMProfileFuncs iTMProfileFuncs = new TMProfileFuncs();
    private JobActivityOperationFuncs iJobActivityOperationFuncs = new JobActivityOperationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private TestMatrixTMProfileJobPrepare matri = new TestMatrixTMProfileJobPrepare();

    public static String getStringToday()
    {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    @Test
    public void createJob() throws Exception
    {
        // Read all test cases to "testcases", and create job with corresponding
        // TMP.
        ArrayList<String[]> testCases = new ArrayList<String[]>();
        String filePath = ConfigUtil.getConfigData("Base_Path")
                + ConfigUtil.getDataInCase(getClassName(), "TMPTestCasePath");
        String baseResultPath = ConfigUtil.getConfigData("Base_Path_Result");
        String wordCountResultPath_jobDetails = baseResultPath
                + ConfigUtil.getDataInCase(getClassName(),
                        "TMPWordCountPath_jobDetails");
        String wordCountResultPath_activityList = baseResultPath
                + ConfigUtil.getDataInCase(getClassName(),
                        "TMPWordCountPath_activityList");
        String wordCountResultPath_activityDetails = baseResultPath
                + ConfigUtil.getDataInCase(getClassName(),
                        "TMPWordCountPath_activityDetails");

        String[] workflows = ConfigUtil.getDataInCase(getClass().getName(),
                "Workflow").split(",");

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
        String tMPJob = "TMPCase" + getStringToday();
        selenium.click(MainFrame.SETUP_MENU);
        selenium.click(MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        int ljobNames = testCases.size();
        String[] jobNames = new String[ljobNames];

        jobNames[0] = "Case title";

        // iTMProfileFuncs.createMTOptions(selenium,
        // ConfigUtil.getDataInCase(getClassName(), "MTOptions"), className);

        for (int i = 1; i < testCases.size(); i++)
        {
            boolean selected = basic.selectRadioButtonFromTable(selenium,
                    TMProfile.TM_PROFILE_LIST_TABLE,
                    ConfigUtil.getDataInCase(getClassName(), "TMP"));
            if (!selected)
            {
                Assert.assertTrue("Cannot find a proper TM Profile to edit.",
                        false);
            }
            basic.clickAndWait(selenium, TMProfile.EDIT_BUTTON);

            iTMProfileFuncs.TMPOperation(selenium, testCases.get(i));

            CreateJobsFuncs tmp = new CreateJobsFuncs();
            tmp.createJob(tMPJob + i, ConfigUtil.getDataInCase(getClassName(),
                    "SourceFile"), ConfigUtil.getDataInCase(getClassName(),
                    "fileProfileNamesStr"), ConfigUtil.getDataInCase(
                    getClassName(), "targetLocales"));
            Thread.sleep(60000);
            jobNames[i] = tMPJob + i;
        }

        // for (int i=1; i<testCases.size(); i++)
        // {
        // jobNames[i]="TMPCase201110-14-110308"+i;
        // }

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, ConfigUtil
                    .getDataInCase(getClassName(), "WordCountOf_jobDetails"),
                    wordCountResultPath_jobDetails, workflows, jobNames[i]);
        }

        selenium.click(MainFrame.LOG_OUT_LINK);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        CommonFuncs.loginSystemWithAnyone(selenium);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, ConfigUtil
                    .getDataInCase(getClassName(), "WordCountOf_ActivityList"),
                    wordCountResultPath_activityList, workflows, jobNames[i]);
        }

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs
                    .wordcount(selenium, ConfigUtil.getDataInCase(
                            getClassName(), "WordCountOf_activityDetails"),
                            wordCountResultPath_activityDetails, workflows,
                            jobNames[i]);
        }

        // selenium.click(MainFrame.LogOut_LINK);
        // selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

}
