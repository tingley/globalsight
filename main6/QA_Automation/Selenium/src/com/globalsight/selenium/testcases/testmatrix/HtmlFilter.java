package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import junit.framework.Assert;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class HtmlFilter extends BaseTestCase
{
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private TestMatrixJobPrepare matri = new TestMatrixJobPrepare();

    String wHtmlName = ConfigUtil.getDataInCase(getClassName(), "FilterName");
    String wInTxtFilterName = ConfigUtil.getDataInCase(getClassName(),
            "InTxtFilterName");
    String internaltagname = ConfigUtil.getDataInCase(getClassName(),
            "InternalText");
    String ChooseInternalTextPostFilter = ConfigUtil.getDataInCase(
            getClassName(), "ChooseInternalTextPostFilter");
    String str = ConfigUtil.getDataInCase(getClassName(), "AddingTag");
    String l10nFunc = ConfigUtil.getDataInCase(getClassName(), "l10nFunc");
    boolean bConvertEntity = Boolean.parseBoolean(ConfigUtil.getDataInCase(
            getClassName(), "convertEntity"));
    boolean bIgnoreInvTags = Boolean.parseBoolean(ConfigUtil.getDataInCase(
            getClassName(), "ignoreInvTags"));

    String fpname = ConfigUtil.getDataInCase(matri.getClassName(),
            "preparejob_file_profile_names");
    String[] fp = fpname.split(",");

    String filterJob = ConfigUtil
            .getDataInCase(getClassName(), "FilterJobName");
    String DefaultWC = ConfigUtil.getDataInCase(getClassName(), "DefaultWC");
    String dir = ConfigUtil.getDataInCase(getClassName(), "JobFileDir");;
    String targetLocales = ConfigUtil.getDataInCase(matri.getClassName(),
            "jobTargetLocales");

    @Test
    public void initHtmlFilter() throws Exception
    {
        // Initiate filters
        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // new internal text filter for html
        iFilterConfig.InternalText(selenium, internaltagname, wInTxtFilterName);
        // new default html filter
        selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
        selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);

        selenium.type(FilterConfiguration.HTML_FILTER_NAME_TEXT,
                wHtmlName);

        // check all default tags list
        String[] defaultTagList = ConfigUtil.getDataInCase(getClassName(),
                "DefaultTagList").split(",");
        for (int i = 0; i < 7; i++)
        {
            selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
                    defaultTagList[i]);
            selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
            selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
        }
        selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
        System.out.println("The default html filter is created.");

        // Add filter to the corresponding file profile
        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        basic.selectRadioButtonFromTable(selenium, FileProfile.MAIN_TABLE,
                fp[0]);
        selenium.click(FileProfile.EDIT_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(FileProfile.FILTER_SELECT, "label=" + wHtmlName);
        selenium.click(FileProfile.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // read testmatrix
        ArrayList<String[]> testCases = new ArrayList<String[]>();
        String filePath = ConfigUtil.getConfigData("Base_Path")
                + ConfigUtil
                        .getDataInCase(getClassName(), "FilterTestCasePath");
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

        String convEntity;
        String ignrInvalidtags;
        String l10nFunctxt;
        String intrnlTextFilter;
        String embedTags;
        String internalTags;
        String pariedTags;
        String switchTags;
        String translateAttribute;
        String unpairedTags;
        String correctWordCount;

        for (int i = 0; i < testCases.size(); i++)
        {
            convEntity = testCases.get(i)[0];
            ignrInvalidtags = testCases.get(i)[1];
            l10nFunctxt = testCases.get(i)[2];
            intrnlTextFilter = testCases.get(i)[3];
            embedTags = testCases.get(i)[4];
            internalTags = testCases.get(i)[5];
            pariedTags = testCases.get(i)[6];
            switchTags = testCases.get(i)[7];
            translateAttribute = testCases.get(i)[8];
            correctWordCount = testCases.get(i)[9];

            boolean bConEntity = Boolean.parseBoolean(convEntity);
            boolean bIgnInvTag = Boolean.parseBoolean(ignrInvalidtags);
            if (intrnlTextFilter.equals("O"))
            {
                intrnlTextFilter = wInTxtFilterName;
            }

            // convert string to actual action for html filter -
            String str1 = "Embeddable Tags||" + embedTags + ";"
                    + "Internal Tag||" + internalTags + ";" + "Paired Tags||"
                    + pariedTags + ";" + "Switch Tag Map||" + switchTags + ";"
                    + "Translatable Attribute||" + translateAttribute;

            System.out.println(str1);

            selenium.click(MainFrame.DATA_SOURCES_MENU);
            selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            // edit html filter configuration here

            selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
            selenium.click("link=" + wHtmlName);
            iFilterConfig.defaultHtmlFilter2(selenium, wHtmlName, bConEntity,
                    bIgnInvTag, l10nFunctxt, intrnlTextFilter, str1);

            CreateJobsFuncs tmp = new CreateJobsFuncs();
            tmp.createJob(filterJob + i, dir, fp[0], targetLocales);
            Thread.sleep(20000);

            selenium.click(MainFrame.MY_JOBS_MENU);
            selenium.click(MainFrame.MY_JOBS_INPROGRESS_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(MainFrame.Search_BUTTON);
            selenium.click(MainFrame.Search_BUTTON);

            String wordCountGot = basic.jobgetWordCount(selenium,
                    MyJobs.MyJobs_InProgress_TABLE, filterJob + i, 7);
            System.out.println(wordCountGot);
            if (!correctWordCount.equals(wordCountGot))
            {
                System.out.println("Wrong test case: " + (i + 1));
            }

            Assert.assertEquals(DefaultWC, wordCountGot);
        }

    }
}
