package com.globalsight.selenium.testcases.testmatrix;

//Author: Nicole Chen, Created on 2011-09-22

import junit.framework.Assert;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TerminologyElements;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TBMaintenanceSearchFieldTest extends BaseTestCase {

	private TerminologyFuncs maintain = new TerminologyFuncs();
	private static final String FIELD_SEARCH_RESULT_TABLE = "//table[@id='idTable']/tbody";
	String iTBName = ConfigUtil.getDataInCase(getClassName(), "tb_name");
	String searchOption = "";
	String fieldLevel = "Term";
	String fieldLang = "English";
	String fieldName = "";
	String searchStr = "";

	@Test
	public void terminologyFieldSearch() throws Exception {

		boolean createTB = ConfigUtil.getDataInCase(getClassName(), "create_tb").equalsIgnoreCase("Yes");
		if (createTB) {
			terminologyPreparation();
		}

		selenium.click(MainFrame.SETUP_MENU);
		selenium.click(MainFrame.TERMINOLOGY_SUBMENU);

		String[] fieldNames;
		String[] searchStrings;

		// Case sensitive and whole word only test
		searchStr = ConfigUtil.getDataInCase(getClassName(), "search_str_option");
		searchStrings = searchStr.split("\\|");

		for (String str : searchStrings) {
			searchOption = str.split(",")[0].trim();
			searchStr = str.split(",")[1].trim();
			maintain.maintenanceSearchField(selenium, iTBName, searchOption, fieldLevel, fieldLang, fieldName,
					searchStr);

			// Check if Check if there returns any entries
			Assert.assertTrue(selenium.isElementPresent(FIELD_SEARCH_RESULT_TABLE + "/tr[2]"));

			// Check the string match the search option
			String content = selenium.getText(FIELD_SEARCH_RESULT_TABLE + "/tr[2]/td[3]");
			searchResultsVerify(content, "");
		}
		selenium.click(TerminologyElements.PREVIOUS_BUTTON);

		// Search for text field test
		String fields = ConfigUtil.getDataInCase(getClassName(), "search_str_fields_name");
		fieldNames = fields.split("\\|");
		searchOption = ConfigUtil.getDataInCase(getClassName(), "search_option");

		for (String field : fieldNames) {
			fieldLevel = field.split(",")[0].trim();
			fieldLang = field.split(",")[1].trim();
			fieldName = field.split(",")[2].trim();
			// specify the search string
			searchStr = ConfigUtil.getDataInCase(getClassName(), "search_str_text_field");
			if (searchStr.equals("")) {
				searchStr = fieldName + "_Search";
			}

			maintain.maintenanceSearchField(selenium, iTBName, searchOption, fieldLevel, fieldLang, fieldName,
					searchStr);

			// Check if Check if there returns any entries
			Assert.assertTrue(selenium.isElementPresent(FIELD_SEARCH_RESULT_TABLE + "/tr[2]"));

			// Check Search results and type name
			String content = selenium.getText(FIELD_SEARCH_RESULT_TABLE + "/tr[2]/td[3]");
			String type = selenium.getText(FIELD_SEARCH_RESULT_TABLE + "/tr[2]/td[4]");
			searchResultsVerify(content, type);

		}
		selenium.click(TerminologyElements.PREVIOUS_BUTTON);

		// Search for attribute field test
		searchOption = ConfigUtil.getDataInCase(getClassName(), "search_atrribute_option");
		fieldLevel = ConfigUtil.getDataInCase(getClassName(), "search_attribute_level");
		fieldLang = ConfigUtil.getDataInCase(getClassName(), "search_attribute_lang");
		fieldNames = ConfigUtil.getDataInCase(getClassName(), "search_attribute_type").split("\\|");
		for (String fieldtype : fieldNames) {
			// specify the search strings for different types
			if (fieldtype.equalsIgnoreCase("Part of Speech")) {
				searchStrings = ConfigUtil.getDataInCase(getClassName(), "search_attribute_value_property").split(",");
			} else
				searchStrings = ConfigUtil.getDataInCase(getClassName(),
						"search_attribute_value_" + fieldtype.toLowerCase()).split(",");
			fieldName = fieldtype;
			for (String str : searchStrings) {
				searchStr = str;
				maintain.maintenanceSearchField(selenium, iTBName, searchOption, fieldLevel, fieldLang, fieldtype,
						searchStr);

				// Check if Check if there returns any entries
				Assert.assertTrue(selenium.isElementPresent(FIELD_SEARCH_RESULT_TABLE + "/tr[2]"));

				// Check Search results and type name
				String content = selenium.getText(FIELD_SEARCH_RESULT_TABLE + "/tr[2]/td[3]");
				String type = selenium.getText(FIELD_SEARCH_RESULT_TABLE + "/tr[2]/td[4]");
				searchResultsVerify(content, type);
			}
		}
		selenium.click(TerminologyElements.PREVIOUS_BUTTON);
	}

	/*
	 * Create a termbase and import testing data.
	 */
	private void terminologyPreparation() {
		selenium.click(MainFrame.SETUP_MENU);
		selenium.click(MainFrame.TERMINOLOGY_SUBMENU);

		String importDir = ConfigUtil.getConfigData("Base_Path")
				+ ConfigUtil.getDataInCase(getClassName(), "importdata_dir");
		String fileName = ConfigUtil.getDataInCase(getClassName(), "tb_file_name");

		try {
			boolean found = maintain.selectRadioButtonFromTable(selenium, TerminologyElements.MAIN_TABLE, iTBName);

			if (found) {
				maintain.remove(selenium, iTBName);
			}

			maintain.create(selenium, iTBName, "", "", "");
			maintain.importData(selenium, importDir, fileName, iTBName, "", "");
		} catch (Exception e) {
			Reporter.log("Fail to prepare termbase");
			return;
		}
	}

	/*
	 * Verify the field content, field type match the search condition
	 */
	private void searchResultsVerify(String fieldContent, String type) {
		if (searchOption.equalsIgnoreCase("Both"))
			Assert.assertTrue(fieldContent.equals(searchStr));
		else if (searchOption.equalsIgnoreCase("Word Only"))
			Assert.assertTrue(fieldContent.equalsIgnoreCase(searchStr));
		else if (searchOption.equalsIgnoreCase("Match Case"))
			Assert.assertTrue(fieldContent.contains(searchStr));
		else
			Assert.assertTrue(fieldContent.toLowerCase().contains(searchStr.toLowerCase()));

		if (!type.equals("")) {
			if (fieldName.equalsIgnoreCase("Part Of Speech"))
				Assert.assertEquals("pos", type);
			else
				Assert.assertEquals(fieldName.toLowerCase(), type);
		}
	}
}
