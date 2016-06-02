package com.globalsight.selenium.testcases.testmatrix.datasource;

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
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.XMLRules;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class XMLFilter extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private XMLRulesFuncs iXMLRuleConfig = new XMLRulesFuncs();
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
	public void testXMLFilter() throws Exception {
		// Initiate filters
        iSelniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);
  
		if (!(selenium.isElementPresent(FilterConfiguration.IMPORT_BUTTON))){
			iSelniumUtils.openMenuItemAndWait(selenium,
	                MainFrame.SETUP_MENU,
	                MainFrame.PERMISSION_GROUPS_SUBMENU);
			selenium.click(PermissionGroups.ADMINISTRATOR_LINK);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click("name="+PermissionGroups.Permissions_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(PermissionGroups.DATA_SOURCE_EXPAND);
			selenium.click(PermissionGroups.DATA_SOURCE_FILTER_CONFIGURATION_EXPAND);
			selenium.click(PermissionGroups.EXPORT_FILTERS_CHECKBOX);
			selenium.click(PermissionGroups.IMPORT_FILTERS_CHECKBOX);
			selenium.click("name=" + PermissionGroups.Done_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click("name=" + PermissionGroups.Save_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(MainFrame.LOG_OUT_LINK);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			CommonFuncs.loginSystemWithAdmin(selenium);
		}
			
		 iSelniumUtils.openMenuItemAndWait(selenium,
	                MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILTER_CONFIGURATION_SUBMENU);
		 
		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"XMLFilterTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		
		
		String File_Profile_name;;
		String Filter_Name;
		String Option;
		String XML_Rule;
		String Convert_HTML_Entity_For_Export;
		String Import_Export_Entities;
		String Extended_Whitespace_Characters;
		String Placeholder_Consolidation;
		String Placeholder_Trimming;
		String Save_non_ASCII_Characters_As;
		String Whitespace_Handling;
		String Empty_Tag_Format;
		String Element_post_filter;
		String CDATA_post_filter;
		String SID_Support;
		String Check_Well_Formedness;
		String Generate_Language_Information;
		String Base_Text_Filter_Internal_Text;
		String Base_Text_Filter_Escaping;
		String Base_Text_post_filter;
		String Embeddable_Tags;
		String Preserve_Whitespace_Tags;
		String Translatable_Attribute_Tags;
		String Content_Inclusion_Tags;
		String CDATA_post_filter_tags;
		String Entities;
		String Processing_Instructions;
		String Internal_Tags;
		String Source_Comment_from_XMLComment;
		String Source_Comment_from_XMLTag;
		String Source_File;
		String XML_Rule_File;
		String Import_Filter;
		String Empty_File_Profile_Name = "";
		String Filter_2_File_Profile_Name = "";
		String Empty_With_Rule_File_Profile_Name ="";
		

		
		String filterJob = getStringToday();
//		String filterJob = "201511-04-115231";

    	
		int ljobNames = testCases.size();
		String[][] jobNames = new String[ljobNames][];
		jobNames[0] = new String[1];
		jobNames[0][0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i = 1; i < testCases.size(); i++) {
//		for (int i = 33; i < 43; i++) {
			File_Profile_name = testCases.get(i)[1];
			Filter_Name = testCases.get(i)[2];
			Option = testCases.get(i)[3];
			XML_Rule = testCases.get(i)[4];
			Convert_HTML_Entity_For_Export = testCases.get(i)[5];
			Import_Export_Entities = testCases.get(i)[6];
			Extended_Whitespace_Characters = testCases.get(i)[7];
			Placeholder_Consolidation = testCases.get(i)[8];
			Placeholder_Trimming = testCases.get(i)[9];
			Save_non_ASCII_Characters_As = testCases.get(i)[10];
			Whitespace_Handling = testCases.get(i)[11];
			Empty_Tag_Format = testCases.get(i)[12];
			Element_post_filter = testCases.get(i)[13];
			CDATA_post_filter = testCases.get(i)[14];
			SID_Support = testCases.get(i)[15];
			Check_Well_Formedness = testCases.get(i)[16];
			Generate_Language_Information = testCases.get(i)[17];
			Base_Text_Filter_Internal_Text = testCases.get(i)[18];
			Base_Text_Filter_Escaping = testCases.get(i)[19];
			Base_Text_post_filter = testCases.get(i)[20];
			Embeddable_Tags = testCases.get(i)[21];
			Preserve_Whitespace_Tags = testCases.get(i)[22];
			Translatable_Attribute_Tags = testCases.get(i)[23];
			Content_Inclusion_Tags = testCases.get(i)[24];
			CDATA_post_filter_tags = testCases.get(i)[25];
			Entities = testCases.get(i)[26];
			Processing_Instructions = testCases.get(i)[27];
			Internal_Tags = testCases.get(i)[28];
			Source_Comment_from_XMLComment = testCases.get(i)[29];
			Source_Comment_from_XMLTag = testCases.get(i)[30];
			Source_File = testCases.get(i)[31];
			XML_Rule_File = testCases.get(i)[32];
			Import_Filter = testCases.get(i)[33];

			
			
			Empty_File_Profile_Name = "XML_Empty";
			Empty_With_Rule_File_Profile_Name = "XML_Empty_With_Rule";
			Filter_2_File_Profile_Name = "XML_Filter_2";
						
			if (File_Profile_name.equalsIgnoreCase("XML_Empty")) Empty_File_Profile_Name = File_Profile_name;
			if (File_Profile_name.equalsIgnoreCase("XML_Empty_With_Rule")) Empty_With_Rule_File_Profile_Name = File_Profile_name;
			if (File_Profile_name.equalsIgnoreCase("XML_Filter_2")) Filter_2_File_Profile_Name = File_Profile_name;
			
			if (!(XML_Rule.isEmpty())){
				if ((!(XML_Rule.equalsIgnoreCase("x"))) && (!(XML_Rule.equalsIgnoreCase("o")))){
					iSelniumUtils.openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU, MainFrame.XML_RULES_SUBMENU);
					if (!(basic.isTextPresent(selenium, XML_Rule))){
						String XML_Rule_File_Path = ConfigUtil.getConfigData("Base_Path") + XML_Rule_File;
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
				}
			}
			
			if ((!(Import_Filter.isEmpty()))&&(!(Import_Filter.equalsIgnoreCase("x"))) 
					&& (!(Import_Filter.equalsIgnoreCase("o")))){
				
				String filePath_Import_Filter = ConfigUtil.getConfigData("Base_Path")
						+ Import_Filter;
				File file_Import_Filter = new File(filePath_Import_Filter);
				AssertJUnit.assertTrue(file_Import_Filter.exists());
				BufferedReader br_Import_Filter = new BufferedReader(new FileReader(file_Import_Filter));
				
				String line_Import_Filter;
				while ((line_Import_Filter = br_Import_Filter.readLine()) != null) {
					String[] XML_Import_Filter_Filter_Name = line_Import_Filter.split("=");
					String tmp_2 = XML_Import_Filter_Filter_Name[0];
					String[] First_XML_Import_Filter_Filter_Name = tmp_2.split("\\.");
					if (First_XML_Import_Filter_Filter_Name.length==3){
						if ((First_XML_Import_Filter_Filter_Name[0].trim().equalsIgnoreCase("xml_rule_filter")) && 
							(First_XML_Import_Filter_Filter_Name[2].trim().equalsIgnoreCase("FILTER_NAME")))
						Filter_Name = XML_Import_Filter_Filter_Name[1].trim();
					}
				}
				br.close();
			}

			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			iFilterConfig.XMLfilterOperation(selenium, Filter_Name,XML_Rule,
						Convert_HTML_Entity_For_Export,
						Import_Export_Entities,
						Extended_Whitespace_Characters,
						Placeholder_Consolidation,
						Placeholder_Trimming,
						Save_non_ASCII_Characters_As,
						Whitespace_Handling,
						Empty_Tag_Format,
						Element_post_filter,
						CDATA_post_filter,
						SID_Support,
						Check_Well_Formedness,
						Generate_Language_Information,
						Base_Text_Filter_Internal_Text,
						Base_Text_Filter_Escaping,
						Base_Text_post_filter,
						Embeddable_Tags,
						Preserve_Whitespace_Tags,
						Translatable_Attribute_Tags,
						Content_Inclusion_Tags,
						CDATA_post_filter_tags,
						Entities,
						Processing_Instructions,
						Internal_Tags,
						Source_Comment_from_XMLComment,
						Source_Comment_from_XMLTag,
						Import_Filter
);
	
			
			
			// create File Profile
//	        SeleniumUtils.
			
			openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);

	        ArrayList<String> array = new ArrayList<String>();
	        array.add(File_Profile_name);
	        array.add("Description: xml filter test");
	        array.add(getProperty(testMatrixFile,"XMLFitler.localizationProfiles"));
	        array.add("XML");
	        array.add(Filter_Name);
	        array.add("x");
	        array.add("xml");
	        

	        fileProfileFuncs.setup(array);
	        fileProfileFuncs.create(selenium);
	        
	       
        	if (Option.equalsIgnoreCase("Filter_2")) {
        		jobNames[i] = new String[3];
        		jobNames[i][0] = i + "_" + Filter_Name + "_" + filterJob;
            	jobNames[i][1] = i + "_" + Filter_Name + "_Empty_Filter"+ "_" + filterJob;
        		jobNames[i][2] = i + "_" + Filter_Name + "_Filter_2_"+ "_" + filterJob;
        	} else if (Filter_Name.length()>=22)
        	{
        		if (Filter_Name.substring(0, 22).equalsIgnoreCase("Import_Export_Entities")){
            		jobNames[i] = new String[1];
            		jobNames[i][0] = i + "_" + Filter_Name + "_" + filterJob;
            	}  	else {
            		jobNames[i] = new String[2];
            		jobNames[i][0] = i + "_" + Filter_Name + "_" + filterJob;
                	jobNames[i][1] = i + "_" + Filter_Name + "_Empty_Filter"+ "_" + filterJob;
            	} 
        	}
        	else 
        	{
        		jobNames[i] = new String[2];
        		jobNames[i][0] = i + "_" + Filter_Name + "_" + filterJob;
            	jobNames[i][1] = i + "_" + Filter_Name + "_Empty_Filter"+ "_" + filterJob;
        	} 
        	
	        
//	        jobNames[i] = "XML_" + Filter_Name + "_" + filterJob + i;
        	if ((!(Source_File.isEmpty())) && (!(Source_File.equalsIgnoreCase("x")))){
        		
        		String[] Source_Files = Source_File.split(",");
        		String File_Profile_names = "";
        		String Empty_With_Rule_File_Profile_Names = "";
        		String Empty_File_Profile_Names = "";
        		String Filter_2_File_Profile_Names = "";
        		for(int k=0;k<Source_Files.length;k++)
                {
        			if (k==(Source_Files.length - 1)){
        				File_Profile_names = File_Profile_names + File_Profile_name;
            			Empty_With_Rule_File_Profile_Names = Empty_With_Rule_File_Profile_Names + Empty_With_Rule_File_Profile_Name;
            			Empty_File_Profile_Names = Empty_File_Profile_Names + Empty_File_Profile_Name;
            			Filter_2_File_Profile_Names = Filter_2_File_Profile_Names + Filter_2_File_Profile_Name;
        			} else {
        				File_Profile_names = File_Profile_names + File_Profile_name + ",";
            			Empty_With_Rule_File_Profile_Names = Empty_With_Rule_File_Profile_Names + Empty_With_Rule_File_Profile_Name + ",";
            			Empty_File_Profile_Names = Empty_File_Profile_Names + Empty_File_Profile_Name + ",";
            			Filter_2_File_Profile_Names = Filter_2_File_Profile_Names + Filter_2_File_Profile_Name + ",";
        			}
                }
	        	for (int j=0; j<jobNames[i].length; j++){
	        		
	        		
					switch (j)
					{
						case 0:{
							tmp.createJob(jobNames[i][j], Source_File, 
									File_Profile_names, getProperty(testMatrixFile,"XMLtargetLocales"));
							break;
						}
						case 1:{
							if ((!(XML_Rule.isEmpty())) && (!(XML_Rule.equalsIgnoreCase("x"))) &&  (!(XML_Rule.equalsIgnoreCase("o")))){
								tmp.createJob(jobNames[i][j], Source_File, 
										Empty_With_Rule_File_Profile_Names, getProperty(testMatrixFile,"XMLtargetLocales"));
								break;
							} else {
								tmp.createJob(jobNames[i][j], Source_File, 
										 Empty_File_Profile_Names, getProperty(testMatrixFile,"XMLtargetLocales"));
								break;
							}

						}
						case 2:{
							tmp.createJob(jobNames[i][j], Source_File, 
									Filter_2_File_Profile_Names, getProperty(testMatrixFile,"XMLtargetLocales"));
							break;
						}
						default: {break;}
						
						
					}
					Thread.sleep(3000);	
					}
					
        	}
				
			
        	
			}

		
		
		for (int i =1; i < testCases.size(); i++) {
//		for (int i =33; i < 43; i++) {
						for (int j=0; j<jobNames[i].length; j++){
							export.exportWorkflow(jobNames[i][j], getProperty(testMatrixFile,"XMLtargetLocales"));
						}
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
