package com.globalsight.selenium.functions;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.WordCount;
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
    public void dispatchJob(Selenium selenium, String jobName, String[] workflows)
    {

        selenium.click(MainFrame.MY_JOBS_MENU);
        selenium.click(MainFrame.MY_JOBS_READY_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        selenium.click("link=" + jobName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 0; i < workflows.length; i++)
        {
            try
            {
                basicFuncs.selectRadioButtonFromTable(selenium,
                        JobDetails.WORKFLOWS_TABLE, workflows[i]);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        selenium.click(JobDetails.DISPATCH_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
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
    
  
    
    /*Get word count from details word count page.
     * @param selenium
     * @param pageName for the strings "jobDetails", "activityList" and "activityDetails" 
     * @param filePathName
     * @param workflows
     * @param jobName
     */
    public void wordcount(Selenium selenium, String pageName, String filePathName, 
    		String[] workflows, String jobName)
    {
    	SimpleDateFormat format=new SimpleDateFormat();  
        String time=format.format(new Date());  
        String wcDetailed = "\r\n"+"\r\n"+time;
    	String wcSummary = "\r\n"+"\r\n"+time; 
    	String Next_LINK = "link=Next";
    	String index = "0";
    	
    	if (pageName.equals("jobDetails"))
    	{	
    		selenium.click(MainFrame.MY_JOBS_MENU);
            selenium.click(MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.type("id="+ MyJobs.SEARCH_JOB_NAME_TEXT, jobName);
    		selenium.keyDown("id="+MyJobs.SEARCH_JOB_NAME_TEXT, "13");
    		selenium.keyUp("id="+MyJobs.SEARCH_JOB_NAME_TEXT, "13");
    		
            if (selenium.isElementPresent("link=" + jobName))
    		{
    			selenium.click("link=" + jobName);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    		} else return;
    		
            
            for (int n = 0; n < workflows.length; n++)
            {
            	try
                {
                	
            		index = basicFuncs.selectRadioButton_String(selenium,
                          JobDetails.WORKFLOWS_TABLE, workflows[n]);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                	selenium.click("//tbody[@id='jobWorkflowDisplayList']/tr[" + index + "]/td[3]/a");
                selenium.waitForPageToLoad("30000");

                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                wcDetailed=wcDetailed + " \r\n" + jobName + "," + "JobDetails, Detailed, ";
                wcSummary=wcSummary+ " \r\n" + jobName + "," + "JobDetails, Summary, ";
                int i = 1;
                int col_detailed = 9;
                int col_summary = 9;
                
                while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE + "tr[" + i +"]/td[2]"))
        		{
        			wcDetailed=wcDetailed + " \r\n";
            		wcSummary=wcSummary+" \r\n";
            		for (; col_detailed<15; col_detailed++)
                    {
                    	if (!(selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE + "tr[2]/td[" + col_detailed +"]"))) break; 
                    }
    	    		for (int j=1; j<col_detailed; j++)
    	    		{
    	    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE + "tr[" + i +"]/td" + "[" + j +"]") + ",";
    	    	   	}
    	    		for (; col_summary<15; col_summary++)
                    {
                    	if (!(selenium.isElementPresent(WordCount.Summary_Statistics_TABLE + "tr[2]/td[" + col_summary + "]"))) break; 
                    }
    	    		for (int j=1; j<col_summary; j++)
    				{
    		    		wcSummary = wcSummary + selenium.getText(WordCount.Summary_Statistics_TABLE + "tr[" + i +"]/td" + "[" + j +"]") + ",";
    			   	}
        		i++;
        		}
        		i++;
        		while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE  + "tr[" + i +"]/td[2]"))
        		{
        			wcDetailed=wcDetailed + " \r\n";
            		wcSummary=wcSummary+" \r\n";
            		
    	    		for (int j=1; j<11; j++)
    	    		{
    	    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE  + "[" + i +"]/td" + "[" + j +"]") + ",";
    	    	   	}
    	    			
    	    		for (int j=1; j<10; j++)
    				{
    		    		wcSummary = wcSummary + selenium.getText(WordCount.Detailed_Statistics_TABLE + "[" + i +"]/td" + "[" + j +"]") + ",";
    			   	}
        		i++;
        		}
        		

    			while (selenium.isElementPresent(Next_LINK)) 
    			{
    				selenium.click(Next_LINK);
    				i=1;
    				while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE  + "[" + i +"]/td[2]"))
    	    		{
    	    			wcDetailed=wcDetailed + " \r\n";
                		wcSummary=wcSummary+" \r\n";
                		
    		    		for (int j=1; j<11; j++)
    		    		{
    		    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE  + "[" + i +"]/td" + "[" + j +"]") + ",";
    		    	   	}
    		    			
    		    		for (int j=1; j<10; j++)
    					{
    			    		wcSummary = wcSummary + selenium.getText(WordCount.Detailed_Statistics_TABLE  + "[" + i +"]/td" + "[" + j +"]") + ",";
    				   	}
    	    		i++;
    	    		}
    	    		i++;
    	    		while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE + "[" + i +"]/td[2]"))
    	    		{
    	    			wcDetailed=wcDetailed + " \r\n";
                		wcSummary=wcSummary+" \r\n";
                		
    		    		for (int j=1; j<11; j++)
    		    		{
    		    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE + "[" + i +"]/td" + "[" + j +"]") + ",";
    		    	   	}
    		    			
    		    		for (int j=1; j<10; j++)
    					{
    			    		wcSummary = wcSummary + selenium.getText(WordCount.Detailed_Statistics_TABLE + "[" + i +"]/td" + "[" + j +"]") + ",";
    				   	}
    	    		i++;
    	    		}
    			}
        		selenium.click(WordCount.Back_to_Job_Details);
        		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        		
            }
            
    		
    		
    	} else if (pageName.equals("activityList"))
    	{
    		selenium.click(MainFrame.MY_ACTIVITIES_MENU);
            selenium.click(MainFrame.MY_ACTIVITIES_ALL_STATUS_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

           
            selenium.type("id="+ MyJobs.SEARCH_JOB_NAME_TEXT, jobName);
    		selenium.keyDown("id="+MyJobs.SEARCH_JOB_NAME_TEXT, "13");
    		selenium.keyUp("id="+MyJobs.SEARCH_JOB_NAME_TEXT, "13");
    		

    		if (selenium.isElementPresent("link=" + jobName))
    		{
    			for (int m = 0; m < workflows.length; m++)
    	    		{
    	            	selenium.select(MyActivities.MYACTIVITIES_LIST_TARGET_LOCALE_FILTER_SELECT, workflows[m]);
    	            	if (selenium.isElementPresent("link=" + jobName))
    	            	{
    	            		try
    	                    {
    	        				int activityLine = basicFuncs.getCheckBoxLineValueInTable(selenium, MyActivities.TABLE_TABLE, jobName, "5");
    	        				selenium.click("xpath=(//input[@name='"+MyActivities.TASK_ID_CHECKBOX + "'])[" +
    	        						Integer.toString(activityLine)
    	        						+ "]");
    	        				selenium.click("//table[@id='dataList']/tbody/tr/td[9]/a");
    	        				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    	        				wcDetailed=wcDetailed + " \r\n" + jobName + "," + "ActivityList, Detailed, " + workflows[m]+ ",";
            					wcSummary=wcSummary+" \r\n" + jobName + "," + "ActivityList, Summary, " + workflows[m]+ ",";
                            	for (int j=1; j<3; j++)
                            	{
                            		wcDetailed=wcDetailed + " \r\n";
                            		wcSummary=wcSummary+" \r\n";
                            		
                            		int col_detailed = 9;
                                    int col_summary = 9;
                                     
                                    for (; col_detailed<15; col_detailed++)
                                     {
                                     	if (!(selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE + "tr[2]/td[" + col_detailed +"]"))) break; 
                                     }
                            		for (int i=1; i<col_detailed; i++)
	            		    		{
	            		    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE + "tr[" + j + "]/td" + 
	            		    					"[" + i +"]") + ",";
	            		    		}
                            		for (; col_summary<15; col_summary++)
                                    {
                                    	if (!(selenium.isElementPresent(WordCount.Summary_Statistics_TABLE + "tr[2]/td[" + col_summary +"]"))) break; 
                                    }
	            			    	for (int i=1; i<col_summary; i++)
	            					{
	            			    		wcSummary = wcSummary + selenium.getText(WordCount.Summary_Statistics_TABLE + "tr[" + j + "]/td" +
	            			    				"[" + i +"]")+ ",";
	            				   	}
                            		
                            	}
            					
            			    	
            					
            				}
    	            		catch (Exception e)
    	                    {
    	                        // TODO Auto-generated catch block
    	                        e.printStackTrace();
    	                    }
                		
                	} else return;
    	            	selenium.click(WordCount.Back_to_Activities);
			    		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			    		selenium.select(MyActivities.MYACTIVITIES_LIST_TARGET_LOCALE_FILTER_SELECT, "Choose...");
                }
                
                
	        }
            
        }

    	else if (pageName.equals("activityDetails"))
    	{
    		
    		
    		selenium.click(MainFrame.MY_ACTIVITIES_MENU);
            selenium.click(MainFrame.MY_ACTIVITIES_ALL_STATUS_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.type("id="+ MyJobs.SEARCH_JOB_NAME_TEXT, jobName);
    		selenium.keyDown("id="+MyJobs.SEARCH_JOB_NAME_TEXT, "13");
    		selenium.keyUp("id="+MyJobs.SEARCH_JOB_NAME_TEXT, "13");

    		if (selenium.isElementPresent("link=" + jobName))
    		{
    			for (int p = 0; p < workflows.length; p++)
	    		{
	            	selenium.select(MyActivities.MYACTIVITIES_LIST_TARGET_LOCALE_FILTER_SELECT, workflows[p]);
	            	if (selenium.isElementPresent("link=" + jobName))
	            	{
	            		try
	                    {
	            			selenium.click("link=" + jobName);
	        				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	        				selenium.click(MyActivities.DETAILED_WORD_COUNT_IN_JOB_BUTTON);
		            		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		    	            int i=1;
    	                	wcDetailed=wcDetailed + " \r\n" + jobName + "," + "ActivityDetails, Detailed, " + workflows[p]+ ",";
        					wcSummary=wcSummary+" \r\n" + jobName + "," + "ActivityDetails, Summary, " + workflows[p]+ ",";
        					
        					int col_detailed = 9;
                            int col_summary = 9;
        					
    			    		while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td[2]"))
    			    		{
    			    			
    			    			wcDetailed=wcDetailed + " \r\n";
                        		wcSummary=wcSummary+" \r\n";
                        		for (; col_detailed<15; col_detailed++)
                                {
                                	if (!(selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE_ActivityDetails  + "tr[2]/td[" + col_detailed +"]"))) break; 
                                }
    				    		for (int j=1; j<col_detailed; j++)
    				    		{
    				    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
    				    	   	}
    				    		for (; col_summary<15; col_summary++)
                                {
                                	if (!(selenium.isElementPresent(WordCount.Summary_Statistics_TABLE_ActivityDetails  + "tr[2]/td[" + col_summary +"]"))) break; 
                                }	
    				    		for (int j=1; j<col_summary; j++)
    							{
    					    		wcSummary = wcSummary + selenium.getText(WordCount.Summary_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
    						   	}
    			    		i++;
    			    		}
    			    		i++;
    			    		while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td[2]"))
    			    		{
    			    			wcDetailed=wcDetailed + " \r\n";
                        		wcSummary=wcSummary+" \r\n";
                        		
    				    		for (int j=1; j<col_detailed; j++)
    				    		{
    				    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
    				    	   	}
    				    			
    				    		for (int j=1; j<col_summary; j++)
    							{
    					    		wcSummary = wcSummary + selenium.getText(WordCount.Summary_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
    						   	}
    			    		i++;
    			    		}
    			    		

        					while (selenium.isElementPresent(Next_LINK)) 
        					{
        						selenium.click(Next_LINK);
        						i=1;
        						while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td[2]"))
	    			    		{
	    			    			wcDetailed=wcDetailed + " \r\n";
	                        		wcSummary=wcSummary+" \r\n";
	                        		
	    				    		for (int j=1; j<col_detailed; j++)
	    				    		{
	    				    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
	    				    	   	}
	    				    			
	    				    		for (int j=1; j<col_summary; j++)
	    							{
	    					    		wcSummary = wcSummary + selenium.getText(WordCount.Summary_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
	    						   	}
	    			    		i++;
	    			    		}
	    			    		i++;
	    			    		while (selenium.isElementPresent(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td[2]"))
	    			    		{
	    			    			wcDetailed=wcDetailed + " \r\n";
	                        		wcSummary=wcSummary+" \r\n";
	                        		
	    				    		for (int j=1; j<col_detailed; j++)
	    				    		{
	    				    			wcDetailed = wcDetailed + selenium.getText(WordCount.Detailed_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
	    				    	   	}
	    				    			
	    				    		for (int j=1; j<col_summary; j++)
	    							{
	    					    		wcSummary = wcSummary + selenium.getText(WordCount.Summary_Statistics_TABLE_ActivityDetails + "tr[" + i +"]/td" + "[" + j +"]") + ",";
	    						   	}
	    			    		i++;
	    			    		}
        					}
    			    		selenium.click(WordCount.Back_to_Activities);
    			    		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    			    		selenium.click(WordCount.Back_to_Activities);
    			    		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    			    		selenium.select(MyActivities.MYACTIVITIES_LIST_TARGET_LOCALE_FILTER_SELECT, "Choose...");
        				}
	            		catch (Exception e)
	                    {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
            	}
	
            }
       }
	            
    }
    
    	try  
        {  
			String fileName=filePathName; 
			FileWriter writer=new FileWriter(fileName,true);  
            writer.write(wcDetailed + wcSummary);  
            writer.close();  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    	
    }
    
    public void discardJob(Selenium selenium, String jobName,  String[] workflows)
    {

        selenium.click(MainFrame.MY_JOBS_MENU);
        selenium.click(MainFrame.MY_JOBS_READY_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click("link=" + jobName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 0; i < workflows.length; i++)
        {
            try
            {
                basicFuncs.selectRadioButtonFromTable(selenium,
                        JobDetails.WORKFLOWS_TABLE, workflows[i]);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        selenium.click(JobDetails.DISCARD_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

    }

    /**
     * Accept activity
     * 
     * @param selenium
     * @param userName
     * @param activityName
     */
    public void acceptActivity(Selenium selenium, String activityName)
    {
        selenium.click(MainFrame.MY_ACTIVITIES_MENU);
        selenium.click(MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(MyActivities.SEARCH_JOB_NAME_TEXT, activityName);
        selenium.click(MyActivities.SEARCH_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        selenium.click("link=" + activityName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(MyActivities.ACCEPT_JOB_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

    }

    /**
     * Complete activity
     * 
     * @param selenium
     * @param userName
     * @param activityName
     */
    public void completeActivity(Selenium selenium, String activityName)
    {
        selenium.click(MainFrame.MY_ACTIVITIES_MENU);
        selenium.click(MainFrame.MY_ACTIVITIES_INPROGRESS_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(MyActivities.SEARCH_JOB_NAME_TEXT, activityName);
        selenium.click(MyActivities.SEARCH_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        selenium.click("link=" + activityName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(MyActivities.TASK_COMPLETED_BUTTON);
        
//        selenium.getConfirmation();
        try
        {
        	selenium.getConfirmation();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
