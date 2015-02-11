package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import junit.framework.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
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

public class WordFilterTest extends BaseTestCase{
    private Selenium selenium;
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private testMatrixJobPrepare matri = new testMatrixJobPrepare();
    
    String internaltagname = ConfigUtil.getDataInCase(getClass().getName(),"AddTags");
    String interName = ConfigUtil.getDataInCase(getClass().getName(),"InternalFilterName");
    String iFilterName = ConfigUtil.getDataInCase(getClass().getName(), "HTMLFilterName");
    String cpf = ConfigUtil.getDataInCase(getClassName(), "InternalTextPostFilter");
    String str = ConfigUtil.getDataInCase(getClassName(), "AddingTag");
    String wFilterName = ConfigUtil.getDataInCase(getClassName(), "wordFilterName");
    String fpname = ConfigUtil.getDataInCase(matri.getClassName(), "preparejob_file_profile_names");
    String[] fp = fpname.split(",");
    String dir = "FilterTestData\\";
    
    @Test
    public void initwordfilter() throws Exception{
        //Initiate filters
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FilterConfiguration_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); 
        iFilterConfig.InternalText(selenium, internaltagname, interName);
        iFilterConfig.htmlFilter(selenium, iFilterName, cpf, str);
        iFilterConfig.wordFilter(selenium, wFilterName);
        
        //Add filter to the corresponding file profile
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FileProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        basic.selectRadioButtonFromTable(selenium, FileProfileElements.MAIN_TABLE, fp[0]);
        selenium.click(FileProfileElements.Edit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(FileProfileElements.filterOption_SELECT,
                "label=" + wFilterName);
        selenium.click(FileProfileElements.NEW_SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        //Read all test cases to "testcases", and create word job with corresponding filter.
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
        
        String headerInfo;
        String toolTips;
        String tableofContent;
        String contentPostFilter;
        String internalTextPostFilter;
        String myheading2;
        String myheading3;
        String dontTrans;
        String correctWordCount;
        String filterJob = "wordJob";
        for (int i = 0; i < testCases.size(); i++)
        {
            headerInfo = testCases.get(i)[0];
            toolTips = testCases.get(i)[1];
            tableofContent = testCases.get(i)[2];
            contentPostFilter = testCases.get(i)[3];
            internalTextPostFilter = testCases.get(i)[4];
            myheading2 = testCases.get(i)[5];
            myheading3 = testCases.get(i)[6];
            dontTrans = testCases.get(i)[7];
            correctWordCount = testCases.get(i)[8];
            
            selenium.click(MainFrame.DataSources_MENU);
            selenium.click(MainFrame.FilterConfiguration_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); 

            iFilterConfig.wordFilterOperation(selenium, headerInfo, toolTips,
                    tableofContent, contentPostFilter, internalTextPostFilter,
                    myheading2, myheading3, dontTrans);
            
            CreateJobsFuncs tmp = new CreateJobsFuncs();
            tmp.create(filterJob+i,"TOC 32.docx",dir);
            Thread.sleep(20000);
           
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
