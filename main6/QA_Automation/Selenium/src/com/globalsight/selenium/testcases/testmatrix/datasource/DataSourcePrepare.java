package com.globalsight.selenium.testcases.testmatrix.datasource;

import java.util.ArrayList;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.pages.LocalizationElements;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.thoughtworks.selenium.Selenium;

public class DataSourcePrepare extends BaseTestCase
{
    private TMFuncs tmFuncs = new TMFuncs();
    private LocalePairsFuncs localepairFuncs = new LocalePairsFuncs();
    private TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();
    private LocalizationFuncs localizationFuncs = new LocalizationFuncs();
    private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
    private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
	private UsersFuncs usersFuncs = new UsersFuncs();
	
    private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
    
    @Test
    public void prepareMatrixJob() throws Exception
    {
    	
    	
    	
        // Create TM
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        tmFuncs.newTM(selenium, getDataInCase("DataSource.tmprepare"));
        
        // Create TMProfile
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);
        
        tmProfileFuncs
                .newTMProfile(selenium, getDataInCase("DataSource.tmProfile"));

        
       
        // create Localization Profile
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        localizationFuncs.create2(selenium,
        		getDataInCase("DataSource.localizationProfile"));
        
       
        
        //add new localizes
        String[] LocaleNames = getDataInCase("DataSource.NewLocale").split(",");
       		
				
       for (int i = 0; i < LocaleNames.length; i++){
    	   
    	   openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                   MainFrame.LOCALE_PAIRS_SUBMENU);
           if (localepairFuncs.newLocalPairs(selenium, getDataInCase("DataSource.SourceLocal." + LocaleNames[i]), 
           		getDataInCase("DataSource.TargetLocal."+LocaleNames[i])))
   		{
   			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.USERS_SUBMENU);
   			usersFuncs.editAddUserRoles(selenium, ConfigUtil.getConfigData("anyoneName"), 
   					getDataInCase("DataSource.SourceLocal."+LocaleNames[i]),
   					getDataInCase("DataSource.TargetLocal."+LocaleNames[i]));
   			
   			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
   				       MainFrame.WORKFLOWS_SUBMENU);
   				workflowsFuncs.duplicateWorkFlow(selenium, getDataInCase("DataSource.Workflow_Name_Dup."+LocaleNames[i]),
   						getDataInCase("DataSource.Source_Workflow_To_Dup."+LocaleNames[i]), 
   						getDataInCase("DataSource.SourceLocal."+LocaleNames[i]), 
   						getDataInCase("DataSource.TargetLocal."+LocaleNames[i]));
   			
   			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
 	                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
   			
   			localizationFuncs.addWorkflow(selenium, getDataInCase("DataSource.l10n.name."+LocaleNames[i]), 
   		    	getDataInCase("DataSource.Workflow_Name."+LocaleNames[i]), getDataInCase("DataSource.workflow.addTargetCode."+LocaleNames[i]));
   		}
       }
//      
//        
//        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.LOCALE_PAIRS_SUBMENU);
//        if (localepairFuncs.newLocalPairs(selenium, getDataInCase("localization.SourceLocalForAR"), 
//        		getDataInCase("localization.TargetLocalForAR")))
//		{
//			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.USERS_SUBMENU);
//			usersFuncs.editAddUserRoles(selenium, ConfigUtil.getConfigData("anyoneName"), 
//					getDataInCase("localization.SourceLocalForAR"),
//					getDataInCase("localization.TargetLocalForAR"));
//			
//			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//				       MainFrame.WORKFLOWS_SUBMENU);
//				workflowsFuncs.duplicateWorkFlow(selenium, getDataInCase("localization.Workflow_Name_Dup_AR"),
//						getDataInCase("localization.Source_Workflow_To_Dup_For_AR"), 
//						getDataInCase("localization.SourceLocalForAR"), 
//						getDataInCase("localization.TargetLocalForAR"));
//			localizationFuncs.addWorkflow(selenium, getDataInCase("localization.name"), 
//		    	getDataInCase("localization.Workflow_Name_AR"), getDataInCase("localization.workflow.addTargetCodeForAR"));
//		}
//        
//        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//                MainFrame.LOCALE_PAIRS_SUBMENU);
//        if (localepairFuncs.newLocalPairs(selenium, getDataInCase("localization.SourceLocalForJA"), 
//        		getDataInCase("localization.TargetLocalForJA")))
//		{
//			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.USERS_SUBMENU);
//			usersFuncs.editAddUserRoles(selenium, ConfigUtil.getConfigData("anyoneName"), 
//					getDataInCase("localization.SourceLocalForJA"),
//					getDataInCase("localization.TargetLocalForJA"));
//			
//			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//				       MainFrame.WORKFLOWS_SUBMENU);
//			workflowsFuncs.duplicateWorkFlow(selenium, getDataInCase("localization.Workflow_Name_Dup_JA"),
//					getDataInCase("localization.Source_Workflow_To_Dup_For_JA"), 
//					getDataInCase("localization.SourceLocalForJA"), 
//					getDataInCase("localization.TargetLocalForJA"));
//			
//			 openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
//	                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
//			localizationFuncs.addWorkflow(selenium, getDataInCase("localization.name"), 
//		        	getDataInCase("localization.Workflow_Name_JA"), getDataInCase("localization.workflow.addTargetCodeForJA"));
//		}
       
			

//        // create File Profile
////        SeleniumUtils.
//        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
//                MainFrame.FILE_PROFILES_SUBMENU);
//
//        ArrayList<String> array = new ArrayList<String>();
//        array.add(getDataInCase("DataSource.fileProfile.names"));
//        array.add(getDataInCase("DataSource.fileProfile.descriptions"));
//        array.add(getDataInCase("DataSource.fileProfile.localizationProfiles"));
//        array.add(getDataInCase("DataSource.fileProfile.sourceFileFormats"));
//        array.add(getDataInCase("DataSource.fileProfile.filters"));
//        array.add(getDataInCase("DataSource.fileProfile.fileExtensions"));
//        
//        
//
//
//        fileProfileFuncs.setup(array);
//        
//        // html filter
//        
//        fileProfileFuncs.create(selenium);
    }
    @BeforeTest
    private void beforeTest() {
    	CommonFuncs.loginSystemWithAdmin(selenium);
    }
    
    @AfterTest
    private void afterTest() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
    }
}
