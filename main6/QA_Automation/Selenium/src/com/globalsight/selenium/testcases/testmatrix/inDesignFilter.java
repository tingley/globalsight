package com.globalsight.selenium.testcases.testmatrix;

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

public class inDesignFilter extends BaseTestCase
{
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private TestMatrixJobPrepare matri = new TestMatrixJobPrepare();

    String wInddName = ConfigUtil.getDataInCase(getClassName(),
            "idndFilterName");
    String sParaConv = ConfigUtil.getDataInCase(getClassName(), "inddPara");

    String fpname = ConfigUtil.getDataInCase(matri.getClassName(),
            "preparejob_file_profile_names");
    String[] fp = fpname.split(",");
    String dir = ConfigUtil.getDataInCase(getClassName(), "JobFileDir");;
    String targetLocales = ConfigUtil.getDataInCase(matri.getClassName(),
            "jobTargetLocales");

    @Test
    public void initInddFilter() throws Exception
    {

        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
        selenium.click(FilterConfiguration.INDD_FILTER_ADD_BUTTON);

        selenium.type(
                FilterConfiguration.INDD_FILTER_NAME_TEXT,
                wInddName);
        selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON);

        String filterJob = "inddJob";

        String[] sPara = sParaConv.split(";");
        {
            for (int i = 0; i < sPara.length; i++)
            {
                String[] array = sPara[i].split(",");
                boolean sTrnHidden = Boolean.parseBoolean(array[0]);
                boolean sTrnMaster = Boolean.parseBoolean(array[1]);
                boolean sFileInfo = Boolean.parseBoolean(array[2]);
                boolean sIgForceLine = Boolean.parseBoolean(array[3]);
                boolean sIgNonBreak = Boolean.parseBoolean(array[4]);
                String correctWordCount = array[5];

                // Initiate filters
                selenium.click(MainFrame.DATA_SOURCES_MENU);
                selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                iFilterConfig.editInddFilter(selenium, wInddName, sTrnHidden,
                        sTrnMaster, sFileInfo, sIgForceLine, sIgNonBreak);

                // Add filter to the corresponding file profile
                selenium.click(MainFrame.DATA_SOURCES_MENU);
                selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                basic.selectRadioButtonFromTable(selenium,
                        FileProfile.MAIN_TABLE, fp[1]);
                selenium.click(FileProfile.EDIT_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                selenium.select(FileProfile.FILTER_SELECT, "label=" + wInddName);
                selenium.click(FileProfile.SAVE_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                CreateJobsFuncs tmp = new CreateJobsFuncs();
                tmp.createJob(filterJob + i, dir, fp[1], targetLocales);
                Thread.sleep(20000);

                selenium.click(MainFrame.MY_JOBS_MENU);
                selenium.click(MainFrame.MY_JOBS_INPROGRESS_SUBMENU);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                selenium.click(MainFrame.Search_BUTTON);
                selenium.click(MainFrame.Search_BUTTON);

                String wordCountGot = basic.jobgetWordCount(selenium,
                        MyJobs.MyJobs_Ready_TABLE, filterJob + i, 7);
                if (!correctWordCount.equals(wordCountGot))
                {
                    System.out.println("Wrong test case: " + (i + 1));
                }

                Assert.assertEquals(correctWordCount, wordCountGot);
            }
        }
    }
}
