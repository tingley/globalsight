package com.globalsight.selenium.functions;

/*
 * FileName: TMFuncs.java
 * Author:Jester
 * Methods: TMnew() 
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-13  First Version  Jester
 * 2011-6-2   updated the TMnew() Jester
 */

import java.io.File;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class TMFuncs extends BasicFuncs {
	/*
	 * Create a New TM and input some values if they are not null. If the TM
	 * already exists, click "Cancel" and back to the TM page.
	 */
	private TerminologyFuncs tbfunc = new TerminologyFuncs();
	
	public String newTM(Selenium selenium, String TMProfiles) throws Exception {

		
		
		String[] iTMProfiles = TMProfiles.split(",");
		String iTMName = null;

		for (String iTMProfile : iTMProfiles) {
			try {
				String[] ivalue = iTMProfile.split("=");
				String iFieldName = ivalue[0].trim();
				String iFieldValue = ivalue[1].trim();

				if (iFieldName.equals("name")) {
					
					selenium.type(TMManagement.TM_SEARCH_CONTENT_TEXT, iFieldValue);
			    	selenium.keyDown(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
			    	selenium.keyUp(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
//			    	selenium.waitForFrameToLoad("css=table.listborder", "1000");

			    	if (!(selenium.isElementPresent("link=" + iFieldValue))){
			    		selenium.click(TMManagement.New_BUTTON);
			    		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			    		selenium.type(TMManagement.Name_TEXT_FIELD, iFieldValue);
			    		iTMName = iFieldValue;
			    	}
			    	else break;
				} else if (iFieldName.equals("domain")) {
					selenium.type(TMManagement.DOMAIN_TEXT, iFieldValue);
				} else if (iFieldName.equals("organization")) {
					selenium.type(TMManagement.ORGANIZATION_TEXT,
							iFieldValue);
				} else if (iFieldName.equals("description")) {
					selenium.type(TMManagement.DESCRIPTION_TEXT,
							iFieldValue);
				} else if (iFieldName != null) {
					selenium.click(TMManagement.RemoteTM_CHECKBOX);
				}

			} catch (Exception e) {
				Reporter.log(e.getMessage());
			}

		}
		if (selenium.isElementPresent(TMManagement.SAVE_BUTTON)) {
			selenium.click(TMManagement.SAVE_BUTTON);
		}

        if (selenium.isAlertPresent())
        {
			selenium.getAlert();
			selenium.click(TMManagement.Cancel_BUTTON);
		}

		if (iTMName != null) {
			Assert.assertEquals(this.isPresentInTable(selenium,
					TMManagement.TM_MANAGEMENT_TABLE, iTMName), true);
		}
		return iTMName;
    	
	}
	
	public void newTM(Selenium selenium, String tMName, String description, String index) throws Exception {

		selenium.type(TMManagement.TM_SEARCH_CONTENT_TEXT, tMName);
    	selenium.keyDown(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
    	selenium.keyUp(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
//    	selenium.waitForFrameToLoad("css=table.listborder", "1000");

    	if (!(selenium.isElementPresent("link=" + tMName))){
    		selenium.click(TMManagement.New_BUTTON);
    		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.type(TMManagement.Name_TEXT_FIELD, tMName.trim());
		selenium.type(TMManagement.DESCRIPTION_TEXT, description);
		if (index.equalsIgnoreCase("yes")) selenium.check(TMManagement.TM_INDEX_TARGET_CHECKBOX);
		selenium.click(TMManagement.SAVE_BUTTON);

        if (selenium.isAlertPresent())
        {
			selenium.getAlert();
			selenium.click(TMManagement.Cancel_BUTTON);
		}
	 }
	}
	
	public void importTM(Selenium selenium, String TMProfiles) throws Exception {
	    String[] iTMProfiles = TMProfiles.split(",");
	    for (String iTMProfile : iTMProfiles) {
	        String[] ivalue = iTMProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();
            
            if (iFieldName.equals("tmName")) {
                boolean selected = selectRadioButtonFromTable(selenium, TMManagement.TM_MANAGEMENT_TABLE, iFieldValue);
                if (!selected)
                {
                    Reporter.log("Cannot find a tm to import.");
                    return;
                }
                Thread.sleep(15000);
                clickAndWait(selenium,"//input[@value='Import...']");
                
            } else if (iFieldName.equals("import_file")) {
                
                String filePath = ConfigUtil.getConfigData("Base_Path");
                filePath = filePath + File.separator + "TM" + File.separator + iFieldValue;
                selenium.type(TMManagement.import_path_TEXT, filePath);
            } else if (iFieldName.equals("sourceTmName")) {
                selenium.type("sourceTmName",iFieldValue);
            } else if (iFieldName.equals("fileFormat")) {
                selenium.click("idTmx2");
            }
	    }
	    
	    clickAndWait(selenium, "//input[@value='Next']");
	    Thread.sleep(15000);
	    clickAndWait(selenium, "Import");
	    
	    String importDone = "var pro = window.document.getElementById('idProgress').innerHTML;" +
        "pro.indexOf('100%')!=-1;";
	    selenium.waitForCondition(importDone, CommonFuncs.LONG_WAIT);
        // import done
        clickAndWait(selenium, "idCancelOk");
	    
	}
	
    public void statistic(Selenium selenium, String tmName) throws Exception
    {
    	
        boolean selected = selectRadioButtonFromTable(selenium, TMManagement.TM_MANAGEMENT_TABLE, tmName);
        if (!selected)
        {
            Reporter.log("Cannot find a proper TM to do statistics.");
            return;
        }
        tbfunc.clickForModalDialog(selenium, TMManagement.Statistics_BUTTON);

    }
    
    public void importTM(Selenium selenium, String tMName, String filePath) throws Exception {
	    
//	    boolean selected = selectRadioButtonFromTable(selenium, TMManagement.TM_MANAGEMENT_TABLE, tMName);
//            if (!selected)
//               {
//                    Reporter.log("Cannot find a tm to import.");
//                    return;
//                }
//        Thread.sleep(15000);
    	
    	selenium.type(TMManagement.TM_SEARCH_CONTENT_TEXT, tMName);
    	selenium.keyDown(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
    	selenium.keyUp(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
//    	selenium.waitForFrameToLoad("css=table.listborder", "1000");

    	 if (!(selenium.isElementPresent("link=" + tMName))){
    		 newTM(selenium, tMName);
    		 selenium.type(TMManagement.TM_SEARCH_CONTENT_TEXT, tMName);
    		 selenium.keyDown(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
    		 selenium.keyUp(TMManagement.TM_SEARCH_CONTENT_TEXT, "\\13");
    	 }
    	 
    	 boolean selected = selectRadioButtonFromTable(selenium, TMManagement.TM_MANAGEMENT_TABLE, tMName);
    	 if (!selected)
           {
                Reporter.log("Cannot find a tm to import.");
                return;
            }
    	 Thread.sleep(15000);
		 clickAndWait(selenium,TMManagement.Import_BUTTON);        
	     selenium.click(TMManagement.Browse_BUTTON);
	     selenium.type(TMManagement.import_path_TEXT, filePath);
	     clickAndWait(selenium, TMManagement.Next_BUTTON);
		 Thread.sleep(15000);
		 clickAndWait(selenium, "Import");
		   
		 String importDone = "var pro = window.document.getElementById('idProgress').innerHTML;" +
	     "pro.indexOf('100%')!=-1;";
		 selenium.waitForCondition(importDone, CommonFuncs.LONG_WAIT);
	     // import done
	     clickAndWait(selenium, "idCancelOk");
    	  
	    
	}
	

}
