package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class SpecialFileWordCountVerify extends BaseTestCase
{   
//    private BasicFuncs basicFuncs = new BasicFuncs();
//    String wordCorrect = getDataInCase("wordCount");
//    String[] wc = wordCorrect.split(",");
//    String xliff = "xliffJob";
//    String po = "poJob";
//    String dir = "JobCreate\\";
//    
//    @Test
//    public void xlfJob() throws Exception{
//        //Create xliff and po job.
//        CreateJobsFuncs createJobsFuncs = new CreateJobsFuncs();
//        createJobsFuncs.create(xliff,"Welocalize_Company.xlf",dir);
//        Thread.sleep(20000);
//        createJobsFuncs.create(po,"Welocalize_Company.po",dir);
//        Thread.sleep(20000);
//        //Verify job word count.
//        selenium.click(MainFrame.MY_JOBS_MENU);
//        selenium.click(MainFrame.MY_JOBS_READY_SUBMENU);
//        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
//        selenium.click(MainFrame.Search_BUTTON);
//        selenium.click(MainFrame.Search_BUTTON);
//        String xlfwordAct = basicFuncs.jobgetWordCount(selenium, MyJobs.MyJobs_Ready_TABLE, xliff, 7);
//        String powordAct = basicFuncs.jobgetWordCount(selenium, MyJobs.MyJobs_Ready_TABLE, po, 7);
//        Assert.assertEquals(wc[0], xlfwordAct);
//        Assert.assertEquals(wc[1], powordAct);
//    }
//    @BeforeMethod
//    public void beforeMethod()
//    {
//        CommonFuncs.loginSystemWithPM(selenium);
//    }
//
//    @AfterMethod
//    public void afterMethod()
//    {
//        CommonFuncs.logoutSystem(selenium);
//    }
//
//     @BeforeTest
//    public void beforeTest()
//    {
//        selenium = CommonFuncs.getSelenium();
//    }
//
//    @AfterTest
//    public void afterTest()
//    {
//        CommonFuncs.endSelenium(selenium);
//    }
}
