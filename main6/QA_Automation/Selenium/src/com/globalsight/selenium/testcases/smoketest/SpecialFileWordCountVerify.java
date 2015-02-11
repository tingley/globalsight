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
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class SpecialFileWordCountVerify extends BaseTestCase
{   
    private Selenium selenium;
    private BasicFuncs basic = new BasicFuncs();
    String wordCorrect = ConfigUtil.getDataInCase(getClass().getName(),"WordCount");
    String[] wc = wordCorrect.split(",");
    String xliff = "xliffJob";
    String po = "poJob";
    String dir = "JobCreate\\";
    
    @Test
    public void xlfJob() throws Exception{
        //Create xliff and po job.
        CreateJobsFuncs tmp = new CreateJobsFuncs();
        tmp.create(xliff,"Welocalize_Company.xlf",dir);
        Thread.sleep(20000);
        tmp.create(po,"Welocalize_Company.po",dir);
        Thread.sleep(20000);
        //Verify job word count.
        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(MainFrame.Search_BUTTON);
        selenium.click(MainFrame.Search_BUTTON);
        String xlfwordAct = basic.jobgetWordCount(selenium, MyJobs.MyJobs_Ready_TABLE, xliff, 7);
        String powordAct = basic.jobgetWordCount(selenium, MyJobs.MyJobs_Ready_TABLE, po, 7);
        Assert.assertEquals(wc[0], xlfwordAct);
        Assert.assertEquals(wc[1], powordAct);
    }
    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithPM(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }

     @BeforeTest
    public void beforeTest()
    {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest()
    {
        CommonFuncs.endSelenium(selenium);
    }
}
