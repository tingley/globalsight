package com.globalsight.selenium.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jodd.util.StringUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;
import com.thoughtworks.selenium.Selenium;

public class ExportWorkflowFuncs extends BasicFuncs
{
    /**
     * Create Job
     * 
     * @param jobName
     * @param filesStr
     * @param fileProfileNamesStr
     * @param targetLocales
     */
	
	private ArrayList<String> propertyNameArray = new ArrayList<String>();
	
    public Boolean exportWorkflow(String jobName, String workflowLocales)
    {
    	if (StringUtil.isEmpty(jobName))
    		return null;
    	
    	String[] i_workflowLocales = workflowLocales.split(",");
    	int a = i_workflowLocales.length;
				
		for (int j = 0; j < i_workflowLocales.length; j++) {
			
			String newJobName = null;
	        try
	        {
	       
	            String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
	                    + "/globalsight/services/AmbassadorWebService?wsdl";
	
	            AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
	            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
	            String token = service.login(
	                    ConfigUtil.getConfigData("pmName"),
	                    ConfigUtil.getConfigData("pmPassword"));
	            
	            try
	                {
	                    service.exportWorkflow(token, jobName, i_workflowLocales[j]);
	                }
	                catch (Exception e)
	                {
	                    e.printStackTrace();
	                    Reporter.log(e.getMessage());
	                }
	            
	        }
		
        catch (Exception e)
        {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }

		}
        
        return true;
    }

}
