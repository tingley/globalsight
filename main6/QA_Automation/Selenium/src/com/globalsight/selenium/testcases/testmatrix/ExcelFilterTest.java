package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfileElements;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class ExcelFilterTest extends BaseTestCase
{
    private Selenium selenium;
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private testMatrixJobPrepare matri = new testMatrixJobPrepare();
    
    String testCaseName = ConfigUtil.getDataInCase(getClass().getName(),"AddTags");
    String testCaseName1 = ConfigUtil.getDataInCase(getClass().getName(),"AddTags1");
    String interName = ConfigUtil.getDataInCase(getClassName(), "InternalFilterName");
    String interName1 = ConfigUtil.getDataInCase(getClassName(), "InternalFilterName1");
    String iFilterName = ConfigUtil.getDataInCase(getClassName(), "Testing");
    String cpf = ConfigUtil.getDataInCase(getClassName(), "InternalTextPostFilter");
    String str = ConfigUtil.getDataInCase(getClassName(), "AddingTag");
    String exc = ConfigUtil.getDataInCase(getClassName(), "ExcelFilterName");
    String fpname = ConfigUtil.getDataInCase(matri.getClassName(), "preparejob_file_profile_names");
    String[] fp = fpname.split(",");
    String dir = "FilterTestData\\";
    
    @Test
    public void initfilter() throws Exception{
    	/*
    	 * Initiate all filters used in testing xls jobs.
    	 */
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FilterConfiguration_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); 
        iFilterConfig.InternalText(selenium, testCaseName, interName);
        iFilterConfig.InternalText(selenium, testCaseName1, interName1);
        iFilterConfig.htmlFilter(selenium, iFilterName, cpf, str);
        iFilterConfig.excelFilter(selenium, exc, iFilterName, interName1);
        
        //Add filter to the corresponding file profile
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FileProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        basic.selectRadioButtonFromTable(selenium, FileProfileElements.MAIN_TABLE, fp[1]);
        selenium.click(FileProfileElements.Edit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(FileProfileElements.filterOption_SELECT,
                "label=" + exc);
        selenium.click(FileProfileElements.NEW_SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        //Read all test cases to "testcases", and create xls job with corresponding filter.
        ArrayList<String[]> testCases = new ArrayList<String []>();
        String filePath = ConfigUtil.getConfigData("Base_Path") + ConfigUtil.getDataInCase(getClassName(), "FilterTestCasePath");
        File file = new File(filePath);
        Assert.assertTrue(file.exists());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine()) != null){
        	String[] testCase = line.split("\t");
        	testCases.add(testCase);
        }
        br.close();
        
        String contentPostFilter;
        String embeddableTags;
        String internalTags;
        String translatableAttibute;
        String internalTextFilter;
        String internalTextPostFilter;
        String internalTextPostFilterChoose;
        String correctWordCount;
        String filterJob = "excelJob";
        
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FilterConfiguration_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        for(int i=0; i<testCases.size(); i++)
        {
        	contentPostFilter = testCases.get(i)[0];
            embeddableTags= testCases.get(i)[1];
            internalTags= testCases.get(i)[2];
            translatableAttibute= testCases.get(i)[3];
            internalTextFilter= testCases.get(i)[4];
            internalTextPostFilter= testCases.get(i)[5];
            internalTextPostFilterChoose= testCases.get(i)[6];
            correctWordCount = testCases.get(i)[7];
            
            selenium.click(MainFrame.DataSources_MENU);
            selenium.click(MainFrame.FilterConfiguration_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); 
            iFilterConfig.filterOperation(selenium,contentPostFilter,embeddableTags,internalTags,translatableAttibute,internalTextFilter,internalTextPostFilter,internalTextPostFilterChoose);
            
            CreateJobsFuncs tmp = new CreateJobsFuncs();
            tmp.create(filterJob+i,"FilterSourceFile.xls",dir);
            Thread.sleep(20000);
            
            
            //getWordCount operation
            selenium.click(MainFrame.MyJobs_MENU);
            selenium.click(MainFrame.Ready_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(MainFrame.Search_BUTTON);
            selenium.click(MainFrame.Search_BUTTON);
            
           String wordCountGot = basic.jobgetWordCount(selenium, MyJobs.MyJobs_Ready_TABLE, filterJob+i, 7);
            if(!correctWordCount.equals(wordCountGot))
            {
                System.out.println("Wrong test case: "+(i+1));
            }

            Assert.assertEquals(correctWordCount, wordCountGot);
           
        }
    }
    @BeforeMethod
    public void beforeMethod() {
        CommonFuncs.loginSystemWithAdmin(selenium);
        
    }

    @AfterMethod
    public void afterMethod() {
        selenium.click(MainFrame.LogOut_LINK);
    }

    @BeforeTest
    public void beforeTest() {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest() {
        CommonFuncs.endSelenium(selenium);
    }

}
