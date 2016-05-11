package com.globalsight.selenium.testcases.testmatrix.setup;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.ExportWorkflowFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.SegmentationRuleFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class TMProfiles extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private LocalePairsFuncs localepairFuncs = new LocalePairsFuncs();
	private UsersFuncs usersFuncs = new UsersFuncs();
	private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private TMProfileFuncs iTMProfileFuncs = new TMProfileFuncs();
	private TMFuncs iTMFuncs = new TMFuncs();
	private SegmentationRuleFuncs iSegmentationRuleFuncs = new SegmentationRuleFuncs();
	private LocalizationFuncs iLocalizationFuncs = new LocalizationFuncs();
	private XMLRulesFuncs iXMLRuleConfig = new XMLRulesFuncs();
	private JobActivityOperationFuncs iJobActivityOperationFuncs = new JobActivityOperationFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
	private CreateJobsFuncs tmp = new CreateJobsFuncs();
	private ExportWorkflowFuncs export = new ExportWorkflowFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testTMProfiles() throws Exception {	// Initiate filters


	openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
	          MainFrame.LOCALE_PAIRS_SUBMENU);
	
		
	if (localepairFuncs.newLocalPairs(selenium, getProperty(testMatrixFile,"TMP.SourceLocal.fr_FR_de_DE"), 
				getProperty(testMatrixFile,"TMP.TargetLocal.fr_FR_de_DE")))
	{
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.USERS_SUBMENU);
		usersFuncs.editAddUserRoles(selenium, ConfigUtil.getConfigData("anyoneName"), 
				getProperty(testMatrixFile,"TMP.SourceLocal.fr_FR_de_DE"), getProperty(testMatrixFile,"TMP.TargetLocal.fr_FR_de_DE"));
	}
	
	openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
	       MainFrame.WORKFLOWS_SUBMENU);
	workflowsFuncs.duplicateWorkFlow(selenium, getProperty(testMatrixFile,"TMP.Workflow_Name_Dup.fr_FR_de_DE"),
			getProperty(testMatrixFile,"TMP.Source_Workflow_To_Dup.For_fr_FR_de_DE"), 
			getProperty(testMatrixFile,"TMP.SourceLocal.fr_FR_de_DE"), 
			getProperty(testMatrixFile,"TMP.TargetLocal.fr_FR_de_DE"));


        
      

		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"TMPTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		String File_Profile_name;
		String TM_Profile_Name;
		String Description;
		String Source_TM_To_Import_To_StorageTM;	
		String Source_TM_To_Import_To_ReferenceTM;	
		String SRX_Rule_Set;
		String SRX_Rule_Set_File_Path;
		String Storage_TM;
		String Save_Unlocalized_Segments_to_TM;	
		String Save_Localized_Segments_to_TM;
		String Save_Wholly_Internal_Text_Segments_to_TM;
		String Save_Exact_Match_Segments_to_TM;	
		String Save_Approved_Segments_to_TM;
		String Save_Unlocalized_Segments_to_Page_TM;
		String Leverage_Localizables;
		String Leverage_Exact_Matches_Only;	
		String Apply_SID_ICE_Promotion_Only;	
		String Apply_SID_Hash_ICE_Promotion;
		String Apply_SID_Hash_Bracketed_ICE_Promotion;	
		String Leverage_Approved_translations_from_selected_Reference_TM;	
		String Leverage_In_progress_translations_from_the_Job;
		String and_from_Jobs_that_write_to_the_Storage_TM;
		String and_from_Jobs_that_write_to_selected_Reference_TM;	
		String Stop_search_after_hitting_100_match;
		String Reference_TM;
		String Type_sensitive_Leveraging;	
		String Case_sensitive_Leveraging;
		String Whitespace_sensitive_Leveraging;
		String Code_sensitive_Leveraging;	
		String Reference_TM_2;
		String Reference_TM_2_TM;	
		String Multilingual_Leveraging;	
		String Auto_Repair_Placeholders;	
		String Get_Unique_from_Multiple_Exact_Matches;	
		String Multiple_Exact_Matches;
		String Leverage_Match_Threshold;
		String Number_of_Matches;
		String Display_TM_Matches_by;	
		String Choose_Latest_Match;
		String Type_sensitive_Leveraging_2;	
		String No_Multiple_Exact_Matches;
		String TU_Attributes_Match_Prioritising_Rules;	
		String Source_Locale;
		String Target_Locale_Short;
		String Target_Locale_Long;
		String Target_Locale_Workflows;	
		String Target_Locale_codes;
		String Source_File;

				
		String filterJob = getStringToday();
//		String filterJob = "HTMLJob201507-29-162119";

    	int ljobNames = testCases.size();
		String[] jobNames = new String[ljobNames];
		jobNames[0] = "Case title";
		String[][] Target_Locales_For_Wordcount = new String[ljobNames][];
		
		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i =1; i < testCases.size(); i++) {
			File_Profile_name = testCases.get(i)[1].trim();
			TM_Profile_Name = testCases.get(i)[2].trim();
			Description = testCases.get(i)[3].trim();
			Source_TM_To_Import_To_StorageTM = testCases.get(i)[4].trim();	
			Source_TM_To_Import_To_ReferenceTM = testCases.get(i)[5].trim();
			SRX_Rule_Set = testCases.get(i)[6].trim();
			SRX_Rule_Set_File_Path = testCases.get(i)[7].trim();
			Storage_TM = testCases.get(i)[8].trim();
			Save_Unlocalized_Segments_to_TM = testCases.get(i)[9].trim();	
			Save_Localized_Segments_to_TM = testCases.get(i)[10].trim();
			Save_Wholly_Internal_Text_Segments_to_TM = testCases.get(i)[11].trim();
			Save_Exact_Match_Segments_to_TM = testCases.get(i)[12].trim();	
			Save_Approved_Segments_to_TM = testCases.get(i)[13].trim();
			Save_Unlocalized_Segments_to_Page_TM = testCases.get(i)[14].trim();
			Leverage_Localizables = testCases.get(i)[15].trim();
			Leverage_Exact_Matches_Only = testCases.get(i)[16].trim();	
			Apply_SID_ICE_Promotion_Only = testCases.get(i)[17].trim();	
			Apply_SID_Hash_ICE_Promotion = testCases.get(i)[18].trim();
			Apply_SID_Hash_Bracketed_ICE_Promotion = testCases.get(i)[19].trim();	
			Leverage_Approved_translations_from_selected_Reference_TM = testCases.get(i)[20].trim();	
			Leverage_In_progress_translations_from_the_Job = testCases.get(i)[21].trim();
			and_from_Jobs_that_write_to_the_Storage_TM = testCases.get(i)[22].trim();
			and_from_Jobs_that_write_to_selected_Reference_TM = testCases.get(i)[23].trim();	
			Stop_search_after_hitting_100_match = testCases.get(i)[24].trim();
			Reference_TM = testCases.get(i)[25].trim();
			Type_sensitive_Leveraging = testCases.get(i)[26].trim();	
			Case_sensitive_Leveraging = testCases.get(i)[27].trim();
			Whitespace_sensitive_Leveraging = testCases.get(i)[28].trim();
			Code_sensitive_Leveraging = testCases.get(i)[29].trim();	
			Reference_TM_2 = testCases.get(i)[30].trim();
			Reference_TM_2_TM = testCases.get(i)[31].trim();	
			Multilingual_Leveraging = testCases.get(i)[32].trim();	
			Auto_Repair_Placeholders = testCases.get(i)[33].trim();	
			Get_Unique_from_Multiple_Exact_Matches = testCases.get(i)[34].trim();	
			Multiple_Exact_Matches = testCases.get(i)[35].trim();
			Leverage_Match_Threshold = testCases.get(i)[36].trim();
			Number_of_Matches = testCases.get(i)[37].trim();
			Display_TM_Matches_by = testCases.get(i)[38].trim();	
			Choose_Latest_Match = testCases.get(i)[39].trim();
			Type_sensitive_Leveraging_2 = testCases.get(i)[40].trim();	
			No_Multiple_Exact_Matches = testCases.get(i)[41].trim();
			TU_Attributes_Match_Prioritising_Rules = testCases.get(i)[42].trim();	
			Source_Locale = testCases.get(i)[43].trim();
			Target_Locale_Short = testCases.get(i)[44].trim();
			Target_Locale_Long = testCases.get(i)[45].trim();
			Target_Locale_Workflows = testCases.get(i)[46].trim();
			Target_Locale_codes = testCases.get(i)[47].trim();
			Source_File = testCases.get(i)[48].trim();
			
			
			if ((SRX_Rule_Set.equalsIgnoreCase("Default")) || (SRX_Rule_Set.equalsIgnoreCase("o")) || SRX_Rule_Set.isEmpty())
	        {
				SRX_Rule_Set = TMProfile.TMP_SRX_RULE_SET_DEFAULT_TEXT;
	        } 
	        	else if (!(SRX_Rule_Set.equalsIgnoreCase("x")))
	        	{
	        		iSelniumUtils.openMenuItemAndWait(selenium,
	    			MainFrame.DATA_SOURCES_MENU,
	    			MainFrame.SegmentationRules_SUBMENU);
	    			if (!(selenium.isElementPresent("link=" + SRX_Rule_Set)))
	    			{
	    				iSegmentationRuleFuncs.uploadSRXRule(selenium, SRX_Rule_Set, ConfigUtil.getConfigData("Base_Path")
	    						+  SRX_Rule_Set_File_Path);
	    			}
	        	}
			
			
			if ((Storage_TM.equalsIgnoreCase("Default")) || (Storage_TM.equalsIgnoreCase("o")) || 
					Storage_TM.isEmpty() || (Storage_TM.equalsIgnoreCase("x")))
	        {
				Storage_TM =  ConfigUtil.getConfigData("Base_Path")
						+ getProperty(testMatrixFile,"TMP_TM_Name");;
	        }
			
			iSelniumUtils.openMenuItemAndWait(selenium,
		    			MainFrame.SETUP_MENU,
		    			MainFrame.TRANSLATION_MEMORY_SUBMENU);
	    	iTMFuncs.newTM(selenium, Storage_TM, Description, "no");
	    	
	    	if (!((Source_TM_To_Import_To_StorageTM.equalsIgnoreCase("Default"))|| (Source_TM_To_Import_To_StorageTM.equalsIgnoreCase("o")) || 
					Source_TM_To_Import_To_StorageTM.isEmpty() || (Source_TM_To_Import_To_StorageTM.equalsIgnoreCase("x"))))
				{
	    			iTMFuncs.importTM(selenium, Storage_TM, ConfigUtil.getConfigData("Base_Path")
    						+  Source_TM_To_Import_To_StorageTM);
				}
	    	
	    	
	    	String[] iReference_TM = Reference_TM.split(",");
	    	String[] iSource_TM_To_Import_To_ReferenceTM = Source_TM_To_Import_To_ReferenceTM.split(",");
	    	
	    	for (int iRefTM_num = 0; iRefTM_num < iReference_TM.length; iRefTM_num++) {
	    		if ((iReference_TM[iRefTM_num].equalsIgnoreCase("Default")) || (iReference_TM[iRefTM_num].equalsIgnoreCase("o")) || 
	    				iReference_TM[iRefTM_num].isEmpty() || (iReference_TM[iRefTM_num].equalsIgnoreCase("x")))
		        {
	    			iReference_TM[iRefTM_num] = getProperty(testMatrixFile,"TMP_TM_Name");;
		        }
				
				iSelniumUtils.openMenuItemAndWait(selenium,
			    			MainFrame.SETUP_MENU,
			    			MainFrame.TRANSLATION_MEMORY_SUBMENU);
		    	iTMFuncs.newTM(selenium, iReference_TM[iRefTM_num], Description, "no");
		    	
		    	if (!((iSource_TM_To_Import_To_ReferenceTM[iRefTM_num].equalsIgnoreCase("Default"))|| (iSource_TM_To_Import_To_ReferenceTM[iRefTM_num].equalsIgnoreCase("o")) || 
		    			iSource_TM_To_Import_To_ReferenceTM[iRefTM_num].isEmpty() || (iSource_TM_To_Import_To_ReferenceTM[iRefTM_num].equalsIgnoreCase("x"))))
					{
		    			iTMFuncs.importTM(selenium, iReference_TM[iRefTM_num], ConfigUtil.getConfigData("Base_Path")
	    						+  iSource_TM_To_Import_To_ReferenceTM[iRefTM_num]);
					}
		    	if (iRefTM_num==0) Reference_TM = iReference_TM[iRefTM_num].trim();
		    	else Reference_TM = Reference_TM + "," + iReference_TM[iRefTM_num].trim();
	    	}
	    	
	    	if (Save_Wholly_Internal_Text_Segments_to_TM.equalsIgnoreCase("yes") || Save_Wholly_Internal_Text_Segments_to_TM.equalsIgnoreCase("no"))
	    	{
	    	
	    		String XML_Rule = "GBS-4239";
	    		iSelniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.XML_RULES_SUBMENU);
				if (!(basic.isTextPresent(selenium, XML_Rule))){
					String XML_Rule_File_Path = ConfigUtil.getConfigData("Base_Path") + getProperty(testMatrixFile,"TMP.XML_Rule_File");
					File rule_file = new File(XML_Rule_File_Path);
					AssertJUnit.assertTrue(rule_file.exists());
					BufferedReader br_rule = new BufferedReader(new FileReader(rule_file));
					String XML_Rule_line;
					String XMLJ_Rule_String = "";
					while ((XML_Rule_line = br_rule.readLine()) != null) {
						XMLJ_Rule_String = XMLJ_Rule_String + XML_Rule_line;
					}
					br_rule.close();
					iXMLRuleConfig.newXMLRule(selenium, XML_Rule, XMLJ_Rule_String);
	    		}
				iSelniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
					      MainFrame.FILTER_CONFIGURATION_SUBMENU);
			
			iFilterConfig.XMLfilterOperation(selenium, getProperty(testMatrixFile,"TMP.XML_Filter_Name"), XML_Rule,
					"x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x","x",
					getProperty(testMatrixFile,"TMP.XML_InternalTag"),"x","x","x");
	    	}
	    	
	    	openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
				      MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);
			
			iTMProfileFuncs.newTMProfile(selenium, TM_Profile_Name, 
				Description,
				SRX_Rule_Set,
				Storage_TM,
				Save_Unlocalized_Segments_to_TM,	
				Save_Localized_Segments_to_TM,
				Save_Wholly_Internal_Text_Segments_to_TM,
				Save_Exact_Match_Segments_to_TM,	
				Save_Approved_Segments_to_TM,
				Save_Unlocalized_Segments_to_Page_TM,
				Leverage_Localizables,
				Leverage_Exact_Matches_Only,	
				Apply_SID_ICE_Promotion_Only,	
				Apply_SID_Hash_ICE_Promotion,
				Apply_SID_Hash_Bracketed_ICE_Promotion,	
				Leverage_Approved_translations_from_selected_Reference_TM,	
				Leverage_In_progress_translations_from_the_Job,
				and_from_Jobs_that_write_to_the_Storage_TM,
				and_from_Jobs_that_write_to_selected_Reference_TM,
				Stop_search_after_hitting_100_match,
				Reference_TM,
				Type_sensitive_Leveraging,	
				Case_sensitive_Leveraging,
				Whitespace_sensitive_Leveraging,
				Code_sensitive_Leveraging,	
				Reference_TM_2,
				Reference_TM_2_TM,	
				Multilingual_Leveraging,	
				Auto_Repair_Placeholders,	
				Get_Unique_from_Multiple_Exact_Matches,	
				Multiple_Exact_Matches,
				Leverage_Match_Threshold,
				Number_of_Matches,
				Display_TM_Matches_by,	
				Choose_Latest_Match,
				Type_sensitive_Leveraging_2,	
				No_Multiple_Exact_Matches,
				TU_Attributes_Match_Prioritising_Rules
				);
			
			openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
			      MainFrame.LOCALIZATION_PROFILES_SUBMENU);
			iLocalizationFuncs.create(selenium, TM_Profile_Name, TM_Profile_Name, Source_Locale, 
					Target_Locale_Workflows, Target_Locale_codes, true);
			
			
			
			
			// create File Profile
//	        SeleniumUtils.
	        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);

	        ArrayList<String> array = new ArrayList<String>();
	        array.add(File_Profile_name);
	        array.add("Description: TM Profile test");
	        array.add(TM_Profile_Name);
	        String source_File_Format = Source_File.substring(Source_File.indexOf('.') + 1, Source_File.length());
	        if (source_File_Format.equalsIgnoreCase("js"))
	        {
	        	array.add("Javascript");
		        array.add("x");
		        array.add("x");
		        array.add("js");
	        } else if (source_File_Format.equalsIgnoreCase("html") || source_File_Format.equalsIgnoreCase("htm")) 
	        {
	        	array.add("HTML");
		        array.add("x");
		        array.add("x");
		        array.add("htm;html");
	        } else if (source_File_Format.equalsIgnoreCase("xml"))
	        {
	        	array.add("XML");
		        array.add(getProperty(testMatrixFile,"TMP.XML_Filter_Name"));
		        array.add("x");
		        array.add("xml");
	        }
	        

	        fileProfileFuncs.setup(array);
	        fileProfileFuncs.create(selenium);
	        
	        jobNames[i] = i + "_"+ File_Profile_name + "_" + filterJob;
//	        jobNames[i] = TM_Profile_Name + "_201601-14-102635" + i;
	        
			tmp.createJob(jobNames[i], Source_File, 
					File_Profile_name, Target_Locale_Short);
			Thread.sleep(1000);
			
			String[] iTarget_Locales = Target_Locale_Long.split(";");
			Target_Locales_For_Wordcount[i-1] = new String[iTarget_Locales.length];
			for (int j = 0; j < iTarget_Locales.length; j++)
			{
				
				Target_Locales_For_Wordcount[i-1][j] = iTarget_Locales[j].trim();
			}
			

		}
		
		
		//input wordcount result to txt file  
		for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, "jobDetails",
            		ConfigUtil.getConfigData("Base_Path_Result")
    				+ getProperty(testMatrixFile,"TMPWordCountPath_jobDetails"), Target_Locales_For_Wordcount[i-1], jobNames[i]);
        }
		

        selenium.click(MainFrame.LOG_OUT_LINK);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        CommonFuncs.loginSystemWithAnyone(selenium);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, "activityList",
            		ConfigUtil.getConfigData("Base_Path_Result")
    				+ getProperty(testMatrixFile,"TMPWordCountPath_activityList"), Target_Locales_For_Wordcount[i-1], jobNames[i]);
        }

        for (int i = 1; i < testCases.size(); i++)
        {
            iJobActivityOperationFuncs.wordcount(selenium, "activityDetails",
            		ConfigUtil.getConfigData("Base_Path_Result")
    				+ getProperty(testMatrixFile,"TMPWordCountPath_activityDetails"), Target_Locales_For_Wordcount[i-1],
                            jobNames[i]);
        }
        
        for (int i =1; i < testCases.size(); i++) {
				export.exportWorkflow(jobNames[i], getProperty(testMatrixFile,"XMLtargetLocales"));
			}
        
        for (int i =1; i < testCases.size(); i++) {
				export.exportWorkflow(jobNames[i], getProperty(testMatrixFile,"XMLtargetLocales"));
			
}

		
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
