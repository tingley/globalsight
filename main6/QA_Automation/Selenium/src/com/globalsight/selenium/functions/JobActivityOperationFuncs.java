package com.globalsight.selenium.functions;

import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.thoughtworks.selenium.Selenium;

/**
 * job dispach, discard
 * 
 * Activity accept, complete.
 * 
 * @author leon
 * 
 */
public class JobActivityOperationFuncs extends BasicFuncs
{
    private BasicFuncs basicFuncs = new BasicFuncs();
    /**
     * dispatch job
     * 
     * @param selenium
     * @param userName
     * @param jobName
     * @throws Exception
     */
    public void dispatchJob(Selenium selenium, String userName, String jobName, String[] workflows)
    {

        CommonFuncs.login(selenium, userName, "password");
        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        selenium.click("link=" + jobName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 0; i < workflows.length; i++)
        {
            try
            {
                basicFuncs.selectRadioButtonFromTable(selenium,
                        JobDetails.Workflows_TABLE, workflows[i]);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        selenium.click(JobDetails.Dispatch_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        CommonFuncs.logoutSystem(selenium);
    }

    /**
     * 
     * Discard job
     * 
     * @param selenium
     * @param userName
     * @param jobName
     * @param workflows
     * @throws Exception
     */
    public void discardJob(Selenium selenium, String userName, String jobName,
            String[] workflows)
    {

        CommonFuncs.login(selenium, userName, "password");
        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click("link=" + jobName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 0; i < workflows.length; i++)
        {
            try
            {
                basicFuncs.selectRadioButtonFromTable(selenium,
                        JobDetails.Workflows_TABLE, workflows[i]);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        selenium.click(JobDetails.Discard_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        CommonFuncs.logoutSystem(selenium);
    }

    /**
     * Accept activity
     * 
     * @param selenium
     * @param userName
     * @param activityName
     */
    public void acceptActivity(Selenium selenium, String userName,
            String activityName)
    {
        CommonFuncs.login(selenium, userName, "password");
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.Available_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click("link=" + activityName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(MyActivities.Accept_BUTTON_Job);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        CommonFuncs.logoutSystem(selenium);
    }

    /**
     * Complete activity
     * 
     * @param selenium
     * @param userName
     * @param activityName
     */
    public void completeActivity(Selenium selenium, String userName,
            String activityName)
    {
        CommonFuncs.login(selenium, userName, "password");
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.InProgress2_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click("link=" + activityName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(MyActivities.TaskCompleted_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.getConfirmation();

        CommonFuncs.logoutSystem(selenium);
    }
}
