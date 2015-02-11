package com.globalsight.selenium.testcases.testmatrix;

import junit.framework.Assert;

import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class POFilter extends BaseTestCase{
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    private TestMatrixJobPrepare matri = new TestMatrixJobPrepare();
        
    String wPOName = ConfigUtil.getDataInCase(getClassName(), "FilterName");
    String sJSFuncText = ConfigUtil.getDataInCase(getClassName(), "JSFunctionText");
    String[] UnicodeEsp = ConfigUtil.getDataInCase(getClassName(), "enableUnicodeEsp").split(",");
    String fpname = ConfigUtil.getDataInCase(matri.getClassName(), "preparejob_file_profile_names");
    String[] fp = fpname.split(",");
    String dir = "FilterTestData\\";
    String workflow = ConfigUtil.getDataInCase(matri.getClassName(), "preparejob_workflow_name");
	String filterJobOri = ConfigUtil.getDataInCase(getClassName(), "filterjob");

   	
    @Test
    public void initJSFilter() throws Exception{
       
    	selenium.click(MainFrame.DATA_SOURCES_MENU);
	   	selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
	   	selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); 
	   	selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
    
	   	//judge whether the js filter is existed or not
	   	selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
	   	selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_ADD_BUTTON);
		if (selenium.isElementPresent("link=" + wPOName)) {
			Reporter.log("The javaproperties filter " + wPOName
					+ " has already exists!");
		} 
		else 
		{
			
			for (int i=0;i<2;i++)
			{
				String id=i+"";
				boolean bUniCodeEscape = Boolean.parseBoolean(UnicodeEsp[i]);
				// Create the Java Script Filter
				selenium.click(MainFrame.DATA_SOURCES_MENU);
			   	selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			   	selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT); 
			   	selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.JAVASCRIPT_FILTER_NAME_TEXT,
						wPOName+id);
				selenium.type(FilterConfiguration.JAVASCRIPT_FILTER_FUNCTION_TEXT, sJSFuncText);
				if (bUniCodeEscape)
				{
					selenium.check(FilterConfiguration.JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX);
				}
				else selenium.uncheck(FilterConfiguration.JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX);
				
				selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_SAVE_BUTTON);

				Assert.assertEquals(selenium.isElementPresent("link="
						+ wPOName+id), true);
				
				
				//Add filter to the corresponding file profile
				selenium.click(MainFrame.DATA_SOURCES_MENU);
			   	selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
			   	selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			   	basic.selectRadioButtonFromTable(selenium, FileProfile.MAIN_TABLE, fp[10]);
			   	selenium.click(FileProfile.EDIT_BUTTON);
			   	selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			   	selenium.select(FileProfile.FILTER_SELECT,
		            "label=" + wPOName+id);
			   	selenium.click(FileProfile.SAVE_BUTTON);
			   	selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			   	
			   	//create job and verify options
			    String filterJob = filterJobOri +id;
		        CreateJobsFuncs tmp = new CreateJobsFuncs();
		        tmp.createJob(filterJob,fp[i], "Welocalize_Company.js",dir);
		        Thread.sleep(20000);
		       
		        selenium.click(MainFrame.MY_JOBS_MENU);
		        selenium.click(MainFrame.MY_JOBS_INPROGRESS_SUBMENU);
		        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		        selenium.click(MainFrame.Search_BUTTON);
		        selenium.click(MainFrame.Search_BUTTON);
		        
		        //verify wordcount
		        String wordCountGot = basic.jobgetWordCount(selenium, MyJobs.MyJobs_InProgress_TABLE, filterJob, 7);

		        String expectWC = "12";
		        if (!expectWC.equals(wordCountGot))
		        {
		        	System.out.println("Wrong test case for js");
		        }
		        Assert.assertEquals(wordCountGot, "12");
		        
		        //Anyone user accepts task
		        selenium.click(MainFrame.LOG_OUT_LINK);
		        CommonFuncs.loginSystemWithAnyone(selenium);
		        
				selenium.click(MainFrame.MY_ACTIVITIES_MENU);
				selenium.click(MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

				selenium.click("link="+filterJob);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
				selenium.click(MyActivities.ACCEPT_JOB_BUTTON);
				
				//modify the segment with unicode characters and export
				// Open the MainEditor
				selenium.click(MyActivities.TARGET_FILES_TABLE + "/tr/td[2]/div/a");
				selenium.waitForPopUp(JobEditors.MAIN_EDITOR_TAG, CommonFuncs.SHORT_WAIT);
				selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);

				// Open SegmentsEditor
				selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
				selenium.selectFrame(JobEditors.CONTENT_FRAME);
				selenium.selectFrame(JobEditors.TARGET_FRAME);
				selenium.selectFrame(JobEditors.CONTENT_FRAME);
				selenium.click(JobEditors.Segments_TABLE + "/tr[2]/td[2]/a");
				selenium.waitForPopUp(JobEditors.SEGMENT_EDITOR_TAG,
						CommonFuncs.SHORT_WAIT);
				// edit the content.
				selenium.selectWindow("name=" + JobEditors.SEGMENT_EDITOR_TAG);
				selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
				selenium.selectFrame(JobEditors.TARGET_FRAME);
				selenium.type(JobEditors.EDIT_TEXT,
						ConfigUtil.getDataInCase(getClassName(), "STRINGPOPUP"));

				// Close the SegmentsEditor
				selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
				selenium.selectFrame(JobEditors.MENU_WINDOW);
				selenium.click(JobEditors.SEGMENT_CLOSE_LINK);

				// Verify the change worked.
				selenium.selectWindow("name=" + JobEditors.MAIN_EDITOR_TAG);
				selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
				selenium.selectFrame(JobEditors.CONTENT_FRAME);
				selenium.selectFrame(JobEditors.TARGET_FRAME);
				selenium.selectFrame(JobEditors.CONTENT_FRAME);

				// Close the MainEditor
				selenium.selectFrame(JobEditors.RELATIVE_TOP_FRAME);
				selenium.selectFrame(JobEditors.MENU_WINDOW);
				selenium.click(JobEditors.SEGMENT_CLOSE_LINK);
				selenium.selectWindow(null);
				
				//log out anyone user and lognin as admin again
				selenium.click(MainFrame.LOG_OUT_LINK);
		        CommonFuncs.loginSystemWithAdmin(selenium);
		        
		        // from My jobs list and export workflow
		        selenium.click(MainFrame.MY_JOBS_MENU);
				selenium.click(MainFrame.MY_JOBS_INPROGRESS_SUBMENU);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

				selenium.click("link="+filterJob);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
				System.out.println(workflow);

				basic.selectRadioButtonFromTable(selenium,
						JobDetails.WORKFLOWS_TABLE,workflow);
							
				selenium.click(JobDetails.EXPORT_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
				selenium.click(JobDetails.EXPORT_EXECUTE_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				
				System.out.println("The job is exported. Please check the unicode escape manually");
				
			}
		}
			
    }
}
