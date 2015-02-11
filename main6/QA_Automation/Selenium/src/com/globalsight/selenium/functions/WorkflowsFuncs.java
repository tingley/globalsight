package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;
import com.globalsight.selenium.pages.Workflows;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: WorkflowsFuncs.java
 * Author:Jester
 * Methods: importWorkflow() 
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-28   First Draft    Jester
 */
public class WorkflowsFuncs extends BasicFuncs {

	/*
	 * This method used to import the workflow from the exist .xml file.
	 * Author:Jester
	 */
	public static final String MAIN_TABLE = "//div[@id='contentLayer']/form/p/table/tbody/tr[2]/td/table/tbody";
		
	//note: 1. when exported the template workflow, use needs to manually click save. 
	public void exportWorkflow(Selenium selenium,String workflowTemplate) throws Exception
	{
		boolean check = selectRadioButtonFromTable(selenium,MAIN_TABLE,workflowTemplate);
		if (!check)
		{
			Reporter.log("Cannot find the workflow template to export!");
            return;
		}
		selenium.click(Workflows.Export_BUTTON);
		
	}
	
	public void importWorkFlow(Selenium selenium,String iFile,String ImportProfiles,String verifyname) throws Exception
	{
		selenium.click(Workflows.Import_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.type(Workflows.FileToImport_TEXT_FIELD, iFile);

		String[] iImportProfiles = ImportProfiles.split(",");
		String iWorkflowName = null;
			
		for (String iImportProfile :  iImportProfiles)
		{
			String[] ivalue = iImportProfile.split("=");
			String iFieldName = ivalue[0].trim();
			String iFieldValue = ivalue[1].trim();
			
			if (iFieldName.equals("name"))
			{
				selenium.type(Workflows.Name_TEXT_FIELD_IMPORT, iFieldValue);
				iWorkflowName=iFieldValue;
			}
			else if(iFieldName.equals("project"))
			{
				selenium.select(Workflows.Project_SELECT, iFieldValue);
			}
			else if(iFieldName.equals("sourcelocale"))
			{
				selenium.select(Workflows.SourceLocale_SELECTION, iFieldValue);
			}
			else if(iFieldName.equals("targetlocale"))
			{
			        String[] array = iFieldValue.split(";");
                    for (String pair : array) {
                        selenium.addSelection(Workflows.TargetLocale_SELECTION, pair);
                    }
//				selenium.addSelection(Workflows.TargetLocale_SELECTION, iFieldValue);
			}
			else
			{
				Reporter.log("The field name "+iFieldName+" can't be found on this page! Please verify it first.");
			}
		}
		
		selenium.click(Workflows.Add_BUTTON);
		selenium.click(Workflows.Save_BUTTON);
		
		if (selenium.isAlertPresent())
		{
			selenium.getAlert();
			selenium.click(Workflows.Cancel_BUTTON);
			if (selenium.isConfirmationPresent())
			{
				selenium.getConfirmation();
			}
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
		
		Assert.assertTrue(isPresentInTable(selenium, Workflows.Workflows_TABLE, verifyname));
	}

	
	
	public void duplicateWorkFlow(Selenium selenium,String newName, String workflowTemplate) throws Exception 
	{
		
		boolean check = selectRadioButtonFromTable(selenium,MAIN_TABLE,workflowTemplate);
		if (!check)
		{
			Reporter.log("Cannot find the workflow template to duplicate!");
            return;
		}
		selenium.click(Workflows.Duplicate_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.type(Workflows.Name_TEXT_FIELD_DUPLICATE, newName);
		selenium.select(Workflows.SourceLocle_SELECTION_DUPLICATE,
                ConfigUtil.getConfigData("en_US"));
        selenium.addSelection(Workflows.TargetLocale_SELECTION_DUPLICATE,
                ConfigUtil.getConfigData("fr_FR"));
		selenium.click(Workflows.Add_BUTTON_DUPLICATE);
		selenium.click(Workflows.Save_BUTTON_DUPLICATE);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}
	
	public void duplicateWorkFlow(Selenium selenium, String iFile, String workflowTemplate,String Project, String source, String target) throws Exception 
	{
		
		boolean check = selectRadioButtonFromTable(selenium,MAIN_TABLE,workflowTemplate);
		if (!check)
		{
			Reporter.log("Cannot find the workflow template to duplicate!");
            return;
		}
		selenium.click(Workflows.Duplicate_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.type(Workflows.Name_TEXT_FIELD_DUPLICATE, iFile);
		selenium.select(Workflows.Project_SELECTION_DUPLICATE, Project);
        selenium.select(Workflows.SourceLocle_SELECTION_DUPLICATE, source);
        String[] itargets = target.split(",");
        for (String itarget :  itargets)
		{
			selenium.addSelection(Workflows.TargetLocale_SELECTION_DUPLICATE, itarget);
		}
		selenium.click(Workflows.Add_BUTTON_DUPLICATE);
		selenium.click(Workflows.Save_BUTTON_DUPLICATE);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		boolean selected = isPresentInTable(selenium, MAIN_TABLE, "workflowDuplicate");		
		if (selected)
		{
			Reporter.log("Duplicated workflow is added.");
		}
	}
	
	public void duplicateWorkFlow(Selenium selenium,String iFile, String workflowTemplate, String source, String target) throws Exception 
	{
		
		boolean check = selectRadioButtonFromTable(selenium,MAIN_TABLE,workflowTemplate);
		if (!check)
		{
			Reporter.log("Cannot find the workflow template to duplicate!");
            return;
		}
		selenium.click(Workflows.Duplicate_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.type(Workflows.Name_TEXT_FIELD_DUPLICATE, iFile);
        selenium.select(Workflows.SourceLocle_SELECTION_DUPLICATE, source);
        String[] itargets = target.split(",");
        for (String itarget :  itargets)
		{
			selenium.addSelection(Workflows.TargetLocale_SELECTION_DUPLICATE, itarget);
		}
		selenium.click(Workflows.Add_BUTTON_DUPLICATE);
		selenium.click(Workflows.Save_BUTTON_DUPLICATE);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		boolean selected = isPresentInTable(selenium, MAIN_TABLE, "workflowDuplicate");		
		if (selected)
		{
			Reporter.log("Duplicated workflow is added.");
		}
	}
	public void removeWorkFlow(Selenium selenium, String workflowDuplicate) throws Exception
	{
		boolean check = selectRadioButtonFromTable(selenium,MAIN_TABLE,workflowDuplicate);
		if (!check)
		{
			Reporter.log("Cannot find the workflow to remove!");
			return;
		}
		selenium.click(Workflows.RemoveWF_BUTTON);
		boolean actual = selenium.getConfirmation().equals("Are you sure you want to remove this Workflow?");
    	Assert.assertEquals(actual,true);
    	
    	boolean selected = isPresentInTable(selenium, MAIN_TABLE, "workflowDuplicate");
        if (!selected)
        {
        	Reporter.log("The workflow was removed successfully.");
        	return;
        }
	}
}
