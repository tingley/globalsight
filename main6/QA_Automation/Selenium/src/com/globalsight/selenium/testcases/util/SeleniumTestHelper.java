/**
 * Copyright 2009, 2012 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.selenium.testcases.util;

import java.util.HashMap;

import jodd.util.StringUtil;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * @author Vincent
 * 
 */
public class SeleniumTestHelper
{
    public static final String FIRST_RECORD_IN_MYACTIVITIES_TABLE = "//table[@class='list']/tbody[2]/tr[2]/td[6]";

    public static String createJob(String jobName, String filePaths,
            String fileProfiles, String targetLocales)
    {
        CreateJobsFuncs createJobsFuncs = new CreateJobsFuncs();
        return createJobsFuncs.createJob(jobName, filePaths, fileProfiles,
                targetLocales);
    }

    public static boolean verifyWordCount(Selenium selenium, String jobName,
            String expectedWordCount) throws Exception
    {
        if (searchActivity(selenium, jobName))
        {
            // Check word count
            String wordCountStr = selenium
                    .getText(FIRST_RECORD_IN_MYACTIVITIES_TABLE);

            Assert.assertEquals(wordCountStr, expectedWordCount);

            return expectedWordCount.equals(wordCountStr);
        }
        else
        {
            return false;
        }
    }

    public static boolean searchActivity(Selenium selenium, String jobName)
            throws Exception
    {
        SeleniumUtils.openMenuItemAndWait(selenium,
                MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        selenium.type("nf", jobName);
        SeleniumUtils.clickAndWait(selenium, "Search");

        return SeleniumUtils.isElementPresent(selenium,
                FIRST_RECORD_IN_MYACTIVITIES_TABLE);
    }

    public static void exportJob(Selenium selenium, String jobName,
            String workflow) throws Exception
    {
        // Open processing job list page
        SeleniumUtils.openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);

        // Open job detail page
        SeleniumUtils.click(selenium, "link=" + jobName);

        // Check specified workflow to export
        SeleniumUtils.selectRadioButtonFromTable(selenium,
                JobDetails.WORKFLOWS_TABLE, workflow);
        SeleniumUtils.clickAndWait(selenium, JobDetails.EXPORT_BUTTON);
        SeleniumUtils.clickAndWait(selenium, JobDetails.EXPORT_EXECUTE_BUTTON);
    }

    public static void download(String jobName) throws Exception
    {
        DownloadUtil.download(jobName);
    }

    public static void downloadAndCompare(String jobName, String filePaths)
            throws Exception
    {
        DownloadUtil.download(jobName);
        CompareUtil.generateCompareReport(filePaths);
    }

    public static boolean verifySegmentCount(Selenium selenium, String jobName,
            String expectedSegmentCount) throws Exception
    {
        if (searchActivity(selenium, jobName))
        {
            SeleniumUtils.clickAndWait(selenium, "link=" + jobName);

            selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/a");
            selenium.waitForPopUp(JobEditors.MAIN_EDITOR_TAG,
                    CommonFuncs.SHORT_WAIT);
            selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);

            // Open SegmentsEditor
            selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
            selenium.selectFrame(JobEditors.CONTENT_FRAME);
            selenium.selectFrame(JobEditors.Source_FRAME);
            selenium.selectFrame(JobEditors.CONTENT_FRAME);

            String testSegmentCount = selenium.getValue("segmentCount");

            selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
            selenium.selectFrame(JobEditors.MENU_WINDOW);
            selenium.click(JobEditors.SEGMENT_CLOSE_LINK);
            selenium.selectWindow(null);

            Assert.assertEquals(testSegmentCount, expectedSegmentCount);

            return expectedSegmentCount.equals(testSegmentCount);
        }
        else
        {
            return false;
        }
    }

    public static void addFileProfile(Selenium selenium,
            FileProfile fileProfile, boolean usingExist)
    {
        FileProfileFuncs fpFuncs = new FileProfileFuncs();
        boolean exist = fpFuncs.existFileProfile(fileProfile.getName());
        if (exist && usingExist)
            return;
        else
        {
            Reporter.log("File profile [" + fileProfile.getName()
                    + "] has already exist");
        }

        fpFuncs.create(selenium, fileProfile);
    }

    /**
     * Split string into HashMap<String,String> format For example, data =
     * name=Jason,age=18,sex=male..... will be returned as, ("name", "Jason")
     * ("age", "18") ("sex", "male) ...
     * 
     * @param data
     *            Original string
     * @return java.util.HashMap<String,String> Split data
     */
    public static HashMap<String, String> splitData(String data)
    {
        HashMap<String, String> splitData = new HashMap<String, String>();
        if (StringUtil.isEmpty(data))
            return splitData;
        String[] split = StringUtil.split(data, ",");
        int index = -1;
        String key, value;
        for (String s : split)
        {
            index = s.indexOf("=");
            if (index > 0)
            {
                key = s.substring(0, index);
                value = s.substring(index + 1);
                splitData.put(key, value);
            }
        }

        return splitData;
    }

    public static boolean isJobCreatedSucc(Selenium selenium, String jobName)
            throws Exception
    {
        int checkTimes = 30;
        long waitTime = 60000;
        String checkTimesStr = ConfigUtil.getConfigData("checkTimes");
        String waitTimeStr = ConfigUtil.getConfigData("middleWait");

        try
        {
            checkTimes = Integer.parseInt(checkTimesStr);
            waitTime = Long.parseLong(waitTimeStr);
        }
        catch (Exception e)
        {
            checkTimes = 30;
            waitTime = 60000;
        }

        String jobNameLink = "link=" + jobName;
        SeleniumUtils.openMenuItemAndWait(selenium,
                MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);
        int times = 0;
        while (times < checkTimes)
        {
            SeleniumUtils.type(selenium, MyJobs.SEARCH_JOB_NAME_TEXT,
                    jobName);
            SeleniumUtils.click(selenium, MyJobs.SEARCH_BUTTON);

            if (SeleniumUtils.isElementPresent(selenium, jobNameLink))
            {
                if (SeleniumUtils.isTextPresent(selenium, "Ready") || SeleniumUtils.isTextPresent(selenium, "In Progress"))
                    return true;
                else
                    return false;
            }

            Thread.sleep(waitTime);
            times++;
        }

        return false;
    }
    
    public static boolean isActivityCreatedSucc(Selenium selenium, String jobName)
            throws Exception
    {
        int checkTimes = 30;
        long waitTime = 60000;
        String checkTimesStr = ConfigUtil.getConfigData("checkTimes");
        String waitTimeStr = ConfigUtil.getConfigData("middleWait");

        try
        {
            checkTimes = Integer.parseInt(checkTimesStr);
            waitTime = Long.parseLong(waitTimeStr);
        }
        catch (Exception e)
        {
            checkTimes = 30;
            waitTime = 60000;
        }

        String jobNameLink = "link=" + jobName;
        SeleniumUtils.openMenuItemAndWait(selenium,
                MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);
        int times = 0;
        while (times < checkTimes)
        {
            SeleniumUtils.type(selenium, MyJobs.SEARCH_JOB_NAME_TEXT,
                    jobName);
            SeleniumUtils.click(selenium, MyJobs.SEARCH_BUTTON);

            if (SeleniumUtils.isElementPresent(selenium, jobNameLink))
            {
                return true;
            }

            Thread.sleep(waitTime);
            times++;
        }

        return false;
    }

}
