package com.globalsight.selenium.testcases.testmatrix;

import junit.framework.Assert;

import org.testng.Reporter;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class JSPFilter extends BaseTestCase
{
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private TestMatrixJobPrepare matri = new TestMatrixJobPrepare();

    String wJSPName = ConfigUtil.getDataInCase(getClassName(), "FilterName");
    String[] AddHeader = ConfigUtil.getDataInCase(getClassName(), "AddHead")
            .split(",");
    String[] EntityEsp = ConfigUtil.getDataInCase(getClassName(),
            "enableEntityEsp").split(",");
    String fpname = ConfigUtil.getDataInCase(matri.getClassName(),
            "preparejob_file_profile_names");
    String[] fp = fpname.split(",");
    String dir = ConfigUtil.getDataInCase(getClassName(), "JobFileDir");;
    String targetLocales = ConfigUtil.getDataInCase(matri.getClassName(),
            "jobTargetLocales");
    String workflow = ConfigUtil.getDataInCase(matri.getClassName(),
            "preparejob_workflow_name");
    String filterJobOri = ConfigUtil.getDataInCase(getClassName(), "filterjob");

    @Test
    public void initJSPFilter() throws Exception
    {

        // init filter page
        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);

        // judge whether the js filter is existed or not
        selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
        selenium.click(FilterConfiguration.JSP_FILTER_ADD_BUTTON);
        if (selenium.isElementPresent("link=" + wJSPName))
        {
            Reporter.log("The javaproperties filter " + wJSPName
                    + " has already exists!");
        }
        else
        {

            for (int i = 0; i < 4; i++)
            {
                String id = i + "";
                boolean bAddHeader = Boolean.parseBoolean(AddHeader[i]);
                boolean bEntityEscape = Boolean.parseBoolean(EntityEsp[i]);
                // Create the Java Script Filter
                selenium.click(MainFrame.DATA_SOURCES_MENU);
                selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
                selenium.click(FilterConfiguration.JSP_FILTER_ADD_BUTTON);
                selenium.type(
                        FilterConfiguration.JSP_FILTER_NAME_TEXT,
                        wJSPName + id);

                if (bAddHeader)
                {
                    selenium.check(FilterConfiguration.JSP_FILTER_ADD_ADDITIONAL_HEAD_CHECKBOX);
                }
                else
                    selenium.uncheck(FilterConfiguration.JSP_FILTER_ADD_ADDITIONAL_HEAD_CHECKBOX);

                if (bEntityEscape)
                {
                    selenium.check(FilterConfiguration.JSP_FILTER_ESCAPE_ENTITY_CHECKBOX);
                }
                else
                    selenium.uncheck(FilterConfiguration.JSP_FILTER_ESCAPE_ENTITY_CHECKBOX);

                selenium.click(FilterConfiguration.JSP_FILTER_SAVE_BUTTON);

                Assert.assertEquals(
                        selenium.isElementPresent("link=" + wJSPName + id),
                        true);

                // Add filter to the corresponding file profile
                selenium.click(MainFrame.DATA_SOURCES_MENU);
                selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                basic.selectRadioButtonFromTable(selenium,
                        FileProfile.MAIN_TABLE, fp[4]);
                selenium.click(FileProfile.EDIT_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                selenium.select(FileProfile.FILTER_SELECT, "label=" + wJSPName
                        + id);
                selenium.click(FileProfile.SAVE_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                // create job and verify options
                String filterJob = filterJobOri + id;
                CreateJobsFuncs tmp = new CreateJobsFuncs();
                tmp.createJob(filterJob + i, dir, fp[7], targetLocales);
                Thread.sleep(20000);

                selenium.click(MainFrame.MY_JOBS_MENU);
                selenium.click(MainFrame.MY_JOBS_INPROGRESS_SUBMENU);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                selenium.click(MainFrame.Search_BUTTON);
                selenium.click(MainFrame.Search_BUTTON);

                // verify wordcount
                String wordCountGot = basic.jobgetWordCount(selenium,
                        MyJobs.MyJobs_InProgress_TABLE, filterJob, 7);

                String expectWC = "10";
                if (!expectWC.equals(wordCountGot))
                {
                    System.out.println("Wrong test case for jsp");
                }
                Assert.assertEquals(wordCountGot, "10");

                selenium.click("link=" + filterJob);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                System.out.println(workflow);

                basic.selectRadioButtonFromTable(selenium,
                        JobDetails.WORKFLOWS_TABLE, workflow);

                selenium.click(JobDetails.EXPORT_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                selenium.click(JobDetails.EXPORT_EXECUTE_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                System.out
                        .println("The job is exported. Please check the entity escape and add header manually");

            }
        }

    }
}
