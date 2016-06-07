package com.globalsight.selenium.functions;

/*
 * FileName: FilterConfigurationFuncs.java
 * Author:Jester
 * Methods: newFilters();removeFilters() 
 * 
 */
import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.TMProfile;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class FilterConfigurationFuncs {

	/*
	 * Create a new filter with the new value.
	 */
	public void newFilters(Selenium selenium, String Filters) throws Exception {

		String[] iFilters = Filters.split(",");
		String iFilterName = null;

		for (String iFilter : iFilters) {
			try {
				iFilterName = iFilter + "1";

				if (iFilter.equals("html")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.HTML_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The html filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the html filter.
						selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.HTML_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.HTML_FILTER_BASE_FONT_CHECKBOX);
						selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.HtmlFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("indesign")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.INDD_FILTER_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The indesign filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the indesign/IDML Filter
						selenium.click(FilterConfiguration.INDD_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.INDD_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.InDesignIDMLFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("internaltext")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.BASE_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The internaltext filter " + iFilterName
								+ " has already exists!");
					} else {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.BASE_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("javaproperties")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The javaproperties filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the Java Properties Filter
						selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.JAVA_PROPERTIES_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SAVE_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.JavaPropertiesFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("javascript")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The java script filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the java script Filter
						selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.JAVASCRIPT_FILTER_NAME_TEXT,
								iFilterName);
						selenium.type(
								FilterConfiguration.JAVASCRIPT_FILTER_FUNCTION_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_SAVE_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.JavaScriptFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("jsp")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.JSP_FILTER_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The jsp filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the jsp Filter
						selenium.click(FilterConfiguration.JSP_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.JSP_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.JSP_FILTER_SAVE_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.JspFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msoffice2010")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msoffice2010 filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the ms office 2010 Filter
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msofficedoc")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msofficedoc filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the ms office doc Filter
						selenium.click(FilterConfiguration.OFFICE_DOC_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.OFFICE_WORD_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msofficeexcel")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msofficeexcel filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the ms office excel Filter
						selenium.click(FilterConfiguration.OFFICE_XLS_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.OFFICE_EXCEL_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_SAVE_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msofficepowerpoint")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msofficepowerpoint filter "
								+ iFilterName + " has already exists!");
					} else {
						// Create the ms office power point Filter
						selenium.click(FilterConfiguration.OFFICE_PPT_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.OFFICE_POWERPOINT_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_SAVE_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}

				} else if (iFilter.equals("openoffice")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.OPEN_OFFICE_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The openoffice filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the open office Filter
						selenium.click(FilterConfiguration.OPEN_OFFICE_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.OPENOFFICE_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.OPENOFFICE_FILTER_SAVE_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("portableobject")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.PO_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The portableobject filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the portable object Filter
						selenium.click(FilterConfiguration.PO_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.PO_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.PO_FILTER_SAVE_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}

				} else if (iFilter.equals("xml")) {
					selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
					selenium.click(FilterConfiguration.XML_FILTER_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The xml filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the xml Filter
						selenium.click(FilterConfiguration.XML_FILTER_ADD_BUTTON);
						selenium.type(
								FilterConfiguration.XML_FILTER_NAME_TEXT,
								iFilterName);
						selenium.click(FilterConfiguration.XML_FILTER_SAVE_BUTTON);

						// Check it

						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				}
			} catch (Exception e) {
				Reporter.log(e.getMessage());
			}
		}
	}

	public void newFilters2(Selenium selenium, String str, String iFilter)
			throws Exception {

		String[] array = str.split(",");
		String iFilterName = null;

		String[] ivalueTemp = array[0].split("=");
		String iFieldValueTemp = ivalueTemp[1].trim();// for checking if
														// fieldValue already
														// exist

		try {
			iFilterName = iFilter + "1";

			if (iFilter.equals("html")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.HTML_FILTER_IMG);

				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The html filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the html filter.
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.HTML_FILTER_NAME_TEXT,
									iFieldValue);
						}
					}

					selenium.click(FilterConfiguration.HTML_FILTER_BASE_FONT_CHECKBOX);
					selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);

				}
			} else if (iFilter.equals("indesign")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.INDD_FILTER_IMG);

				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The indesign filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the indesign/IDML Filter
					selenium.click(FilterConfiguration.INDD_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.INDD_FILTER_NAME_TEXT,
							iFilterName);
					selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON);

				}
			} else if (iFilter.equals("javaproperties")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_IMG);
				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The javaproperties filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the Java Properties Filter
					selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.JAVA_PROPERTIES_FILTER_NAME_TEXT,
							iFilterName);
					selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("javascript")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_IMG);

				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The java script filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the java script Filter
					selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.JAVASCRIPT_FILTER_NAME_TEXT,
							iFilterName);
					selenium.type(FilterConfiguration.JAVASCRIPT_FILTER_FUNCTION_TEXT,
							iFilterName);
					selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("jsp")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.JSP_FILTER_IMG);

				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The jsp filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the jsp Filter
					selenium.click(FilterConfiguration.JSP_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.JSP_FILTER_NAME_TEXT,
							iFilterName);
					selenium.click(FilterConfiguration.JSP_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("msoffice2010")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_IMG);

				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msoffice2010 filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the ms office 2010 Filter
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("o2010FilterDesc", iFieldValue);
						} else if (iFieldName.equals("headerTranslate")) {
							selenium.check("headerTranslate");
						} else if (iFieldName.equals("masterTranslate")) {
							selenium.check("masterTranslate");
						}
					}

					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("msofficedoc")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_IMG);

				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msofficedoc filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the ms office doc Filter
					selenium.click(FilterConfiguration.OFFICE_DOC_FILTER_ADD_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.OFFICE_WORD_FILTER_NAME_TEXT,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("docDesc", iFieldValue);
						} else if (iFieldName.equals("seconderyFilter")) {
							selenium.select("docContentPostFilterSelect",
									"label=" + iFieldValue);
						} else if (iFieldName.equals("headerTranslate")) {
							selenium.click("docHeaderTranslate");
						}
					}

					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("msofficeexcel")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_IMG);
				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msofficeexcel filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the ms office excel Filter
					selenium.click(FilterConfiguration.OFFICE_XLS_FILTER_ADD_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.OFFICE_EXCEL_FILTER_NAME_TEXT,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("excelDesc", iFieldValue);
						} else if (iFieldName.equals("seconderyFilter")) {
							selenium.select("excelContentPostFilterSelect",
									"label=" + iFieldValue);
						}
					}

					selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("msofficepowerpoint")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_IMG);
				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msofficepowerpoint filter "
							+ iFieldValueTemp + " has already exists!");
				} else {
					// Create the ms office power point Filter
					selenium.click(FilterConfiguration.OFFICE_PPT_FILTER_ADD_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.OFFICE_POWERPOINT_FILTER_NAME_TEXT,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("pptFilterDes", iFieldValue);
						} else if (iFieldName.equals("seconderyFilter")) {
							selenium.select("pptContentPostFilterSelect",
									"label=" + iFieldValue);
						}
					}

					selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_SAVE_BUTTON);
				}

			} else if (iFilter.equals("openoffice")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.OPEN_OFFICE_FILTER_IMG);
				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The openoffice filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the open office Filter
					selenium.click(FilterConfiguration.OPEN_OFFICE_FILTER_ADD_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.OPENOFFICE_FILTER_NAME_TEXT,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("ooFilterDesc", iFieldValue);
						} else if (iFieldName.equals("headerTranslate")) {
							selenium.check("headerTranslate");
						}
					}

					selenium.click(FilterConfiguration.OPENOFFICE_FILTER_SAVE_BUTTON);
				}
			} else if (iFilter.equals("portableobject")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.PO_FILTER_IMG);
				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The portableobject filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the portable object Filter
					selenium.click(FilterConfiguration.PO_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.PO_FILTER_NAME_TEXT,
							iFilterName);
					selenium.click(FilterConfiguration.PO_FILTER_SAVE_BUTTON);
				}

			} else if (iFilter.equals("xml")) {
				selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
				selenium.click(FilterConfiguration.XML_FILTER_IMG);
				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The xml filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the xml Filter
					selenium.click(FilterConfiguration.XML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.XML_FILTER_NAME_TEXT,
							iFilterName);
					selenium.click(FilterConfiguration.XML_FILTER_SAVE_BUTTON);
				}
			}

			// selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		} catch (Exception e) {
			Reporter.log(e.getMessage());
		}

	}

	/*
	 * Initiate html filter used in excel filters.
	 */
	public void htmlFilter(Selenium selenium, String iFilterName, String cpf,
			String str) throws Exception {

		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);

		selenium.type(FilterConfiguration.HTML_FILTER_NAME_TEXT,
				iFilterName);
		htmlPostFilterChoose(selenium, cpf);

		String[] group = str.split(";");
		for (int i = 0; i < group.length; i++) {
			String[] array = group[i].split("\\|\\|");
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, array[0]);
			selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		}
		selenium.click(FilterConfiguration.DELETE_VALUE_BUTTON);

		if (selenium.isAlertPresent()) {
			selenium.getAlert();
		} else
			selenium.click(FilterConfiguration.HTML_FILTER_TAG_SAVE_BUTTON);
		for (int i = 0; i < group.length; i++) 
		{
			String[] array = group[i].split("\\|\\|");
			
			if (array[0].equals("Convert HTML Entity For Export"))
			{
				if (array[1].equals("true"))
				{
					selenium.check(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
				} else
				{
					selenium.uncheck(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
				}
			} else if (array[0].equals("Ignore Invalid HTML Tags"))
			{
				if (array[1].equals("true"))
				{
					selenium.check(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
				} else
				{
					selenium.uncheck(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
				}
			}else if (array[0].equals("Localize Function"))
			{
				if (array[1].equals("true"))
				{
					selenium.check(FilterConfiguration.HTML_FILTER_LOCALIZE_FUNTION_TEXT);
				} else
				{
					selenium.uncheck(FilterConfiguration.HTML_FILTER_LOCALIZE_FUNTION_TEXT);
				}
			}else if (array[0].equals("Internal Text post-filter:"))
			{
				if (array[1].equals("true"))
				{
					selenium.check(FilterConfiguration.HTML_FILTER_INTERNAL_TEXT_POST_FILTER_SELECT);
				} else
				{
					selenium.uncheck(FilterConfiguration.HTML_FILTER_INTERNAL_TEXT_POST_FILTER_SELECT);
				}
			}else 
			{
				String[] tagarray = array[1].split(",");
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, array[0]);
				for (String temp : tagarray) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON);

					if (array[0].equals("Internal Tag")) {
						selenium.type(
								FilterConfiguration.HTML_FILTER_TAG_INTERNAL_NAME_TEXT,
								temp);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_INTERNAL_ADD_BUTTON);
					}

					else {
						selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, temp);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
					}
				}
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			
		}
		selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
	}

	public void defaultHtmlFilter2(Selenium selenium, String iFilterName, boolean convEntity, 
			boolean ignoreInv, String l10nFunc, String cpf, String str ) throws Exception {
		
//		selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
//		selenium.click(FilterConfiguration.HtmlFilter_BUTTON);
//
//		selenium.type(FilterConfiguration.FilterName_HtmlFilter_TEXT_FIELD,
//				iFilterName);
		
		// config "Convert HTML Entity For Export"
		if (convEntity){
			selenium.check(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
		
		// config "Ignore Invalid HTML Tags"
		if (!ignoreInv) {
			selenium.uncheck(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
		}
		else selenium.check(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
		// config "Localize Function"
		selenium.type(FilterConfiguration.HTML_FILTER_LOCALIZE_FUNTION_TEXT, l10nFunc);
		
		// config "Internal Text Filter"
		if (cpf.equals("X")||cpf.equals("Choose")) {
			selenium.select(FilterConfiguration.HTML_FILTER_INTERNAL_TEXT_POST_FILTER_SELECT,
					"label=" + "Choose");
		} 
		else htmlPostFilterChoose(selenium, cpf);
		

		// config tags
		String[] group = str.split(";");
		for (int i = 0; i < group.length; i++) {
			String[] array = group[i].split("\\|\\|");
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, array[0]);
			selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		}
		
		// Note: For default html configuration, we'll use default tags. 
		//Please uncomment below line for the specific tag test. 
		// selenium.click(FilterConfiguration.Html_Tags_Delete_BUTTON);
		//
//		if (selenium.isAlertPresent()) {
//			selenium.getAlert();
//		} else
//			selenium.click(FilterConfiguration.Html_Delete_Save_BUTTON);
		
		for (int i = 0; i < group.length; i++) {
			String[] array = group[i].split("\\|\\|");
			String[] tagarray = array[1].split(",");
			String[] maptag = tagarray[0].split(":");
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, array[0]);
				
			for (String temp : tagarray) {
				if (!temp.equals("X")){
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON);
					
					if (array[0].equals("Internal Tag")) {
						selenium.type(
								FilterConfiguration.HTML_FILTER_TAG_INTERNAL_NAME_TEXT,
								temp);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_INTERNAL_ADD_BUTTON);
						
						
					}
					
					else if (array[0].equals("Switch Tag Map")) {
						selenium.type(FilterConfiguration.HTML_FILTER_TAG_MAP_KEY_TEXT, maptag[0]);
						selenium.type(FilterConfiguration.HTML_FILTER_TAG_MAP_VALUE_TEXT, maptag[1]);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_MAP_SAVE_BUTTON);
					}

					else {
						selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, temp);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
					}
					
						
				}
				
			}
			selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		}
		selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
	}
	
			
	public void htmlPostFilterChoose(Selenium selenium, String opt) {
		selenium.select(FilterConfiguration.HTML_FILTER_INTERNAL_TEXT_POST_FILTER_SELECT, "label="
				+ opt);
	}

	/*
	 * Initiate internal text filter.
	 */
	public void InternalText(Selenium selenium, String str, String interName) {
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
		selenium.type(FilterConfiguration.BASE_FILTER_NAME_TEXT,interName);

		String[] tagarray = str.split(",");
		for (String tempStr : tagarray) {
			String[] tag = tempStr.split("\\|\\|");
			selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
			selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, tag[0]);
			selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, tag[2]);
			if (tag[1].equals("true")) {
				selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_RE_CHECKBOX);
			}
			selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
		}
		selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);
		selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
	}

	    /*
    	 * Initiate excel filter.
    	 */
	public void excelFilter(Selenium selenium, String fname, String conName,
			String interName) {
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.OFFICE_XLS_FILTER_ADD_BUTTON);
		selenium.type(FilterConfiguration.OFFICE_EXCEL_FILTER_NAME_TEXT,
				fname);
		selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_SELECT, "label="
				+ conName);
		selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_SELECT,
				"label=" + interName);
		selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_SAVE_BUTTON);
		if (selenium.isAlertPresent())
			selenium.getAlert();
	}
	public void powerpointFilter(Selenium selenium, String fname, String conName,
			String interName) {
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.OFFICE_PPT_FILTER_ADD_BUTTON);
		selenium.type(FilterConfiguration.OFFICE_POWERPOINT_FILTER_NAME_TEXT,
				fname);
		selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT, "label="
				+ conName);
		selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TEXT_SELECT,
				"label=" + interName);
		selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_SAVE_BUTTON);
		if (selenium.isAlertPresent())
			selenium.getAlert();
	}
	
	public void wordFilter(Selenium selenium, String fname){
	    selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
	    selenium.click(FilterConfiguration.OFFICE_DOC_FILTER_ADD_BUTTON);
	    selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_NAME_TEXT, fname);
	    selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
//	    String[] stylearray = sty.split(";");
//
//	    for(int i=0; i<stylearray.length; i++){
//	        String[] array = stylearray[i].split("\\|\\|");
//            String[] tagarray = array[1].split(",");
//            selenium.select(FilterConfiguration.Style_Choose, array[0]);
//            for (String temp : tagarray){
//                selenium.click(FilterConfiguration.Style_Add_BUTTON);
//                selenium.type(FilterConfiguration.Style_Add, temp);
//                selenium.click(FilterConfiguration.Style_Save_BUTTON);
//            }
//            selenium.uncheck(FilterConfiguration.MS_Doc_CheckAll);
//            selenium.click(FilterConfiguration.MS_Doc_CheckAll);
//	    }
	    selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON);
	    
	}

	public void filterOperation(Selenium selenium, String interName, String interName1, String htmlFilterName, String filterName, String contentPostFilter,
			String embeddableTags, String internalTags,
			String translatableAttibute, String internalTextFilter,
			String internalTextPostFilter, String internalTextPostFilterChoose,
			String excelOrPpt) 
	{

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);

		//if (contentPostFilter.equalsIgnoreCase("on")) { 
			selenium.click("link=" + htmlFilterName);

			if (embeddableTags.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, 
						FilterConfiguration.HTML_FILTER_TAG_EMBEDDABLE_LABEL);
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, 
						FilterConfiguration.HTML_FILTER_TAG_EMBEDDABLE_LABEL);
				selenium.check(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}

			if (internalTags.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, 
						FilterConfiguration.HTML_FILTER_TAG_INTERNAL_LABEL);
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			else {
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
					FilterConfiguration.HTML_FILTER_TAG_INTERNAL_LABEL);
			selenium.check(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			if (translatableAttibute.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
						FilterConfiguration.HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE_LABEL);
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
						FilterConfiguration.HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE_LABEL);
				selenium.check(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
            if (embeddableTags.equalsIgnoreCase("x")
                    && internalTags.equalsIgnoreCase("x")
                    && translatableAttibute.equalsIgnoreCase("x"))
            {
                selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
                		FilterConfiguration.HTML_FILTER_TAG_PAIRED_LABEL);
                selenium.check(FilterConfiguration.HTML_FILTER_A_CHECKBOX);
                selenium.check(FilterConfiguration.HTML_FILTER_TAG1_CHECKBOX);
            }

			if (internalTextFilter.equalsIgnoreCase("o"))
				htmlPostFilterChoose(selenium, interName);
			else
				htmlPostFilterChoose(selenium, "Choose");
			
			selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
		//}

		//if (internalTextPostFilter.equalsIgnoreCase("on")) {
			// modify internal text filter
			selenium.click("link=" + interName1);
			if (internalTextPostFilterChoose.equalsIgnoreCase("o")) {
				selenium.uncheck(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);// uncheck,click
				selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);
			} else {
				selenium.check(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);
			}
			selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
		//}

		// operate excel ppt filter
			if (excelOrPpt.equals("excel"))
			{
				selenium.click("link=" + filterName);
				if (contentPostFilter.equalsIgnoreCase("on"))
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_SELECT,
							"label=" + htmlFilterName);
				else
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_SELECT,
							"label=Choose");
				if (internalTextPostFilter.equalsIgnoreCase("on"))
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_SELECT,
							"label=" + interName1);
				else
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_SELECT,
							"label=Choose");
				selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_SAVE_BUTTON);
			}else if (excelOrPpt.equals("powerpoint"))
			{
				selenium.click("link=" + filterName);
				if (contentPostFilter.equalsIgnoreCase("on"))
					selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT,
							"label=" + htmlFilterName);
				else
					selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT,
							"label=Choose");
				if (internalTextPostFilter.equalsIgnoreCase("on"))
					selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TEXT_SELECT,
							"label=" + interName1);
				else
					selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TEXT_SELECT,
							"label=Choose");
				selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_SAVE_BUTTON);
			}
		
	}

	

	 public void IDMLFilterOperation(Selenium selenium, String indd_Filter_Name, 
			 String translate_Hidden_Layers, String translate_Master_Layers, String translate_File_Information,
			 String translate_Hyperlinks, String translate_Hidden_Conditional_Text, String ignore_Tracking_and_Kerning, 
			 String ignore_Forced_Line_Breaks, String ignore_Nonbreaking_Space)
	    {
	    	 selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
	       
	    	if ((selenium.isElementPresent("link=" + indd_Filter_Name)) || (indd_Filter_Name.isEmpty())
	    			|| (indd_Filter_Name.equalsIgnoreCase("x"))){
	 			return;
	 		} else {
	 			selenium.click(FilterConfiguration.INDD_FILTER_ADD_BUTTON);
	 			selenium.type(
	 					FilterConfiguration.INDD_FILTER_NAME_TEXT,
	 					indd_Filter_Name);
	 			
	 		}
	 		if ((!(translate_Hidden_Layers.isEmpty())) && (!(translate_Hidden_Layers.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX);
	 		
	 		if ((!(translate_Master_Layers.isEmpty())) && (!(translate_Master_Layers.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX);
	 		else if (translate_Master_Layers.isEmpty() || translate_Master_Layers.equalsIgnoreCase("x"))
	 			selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX);
	 			
	 		
	 		if ((!(translate_File_Information.isEmpty())) && (!(translate_File_Information.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX);
	 		
	 		if ((!(translate_Hyperlinks.isEmpty())) && (!(translate_Hyperlinks.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_HYPERLINKS_CHECKBOX);
	 		
	 		if ((!(translate_Hidden_Conditional_Text.isEmpty())) && (!(translate_Hidden_Conditional_Text.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_CONDITIONAL_TEXT_CHECKBOX);
	 		else if (translate_Hidden_Conditional_Text.isEmpty() || translate_Hidden_Conditional_Text.equalsIgnoreCase("x"))
	 			selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_CONDITIONAL_TEXT_CHECKBOX);
	 		
	 		if ((!(ignore_Tracking_and_Kerning.isEmpty())) && (!(ignore_Tracking_and_Kerning.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_IGNORE_TRACKING_AND_KERNING_TEXT_CHECKBOX);
	 		
	 		if ((!(ignore_Forced_Line_Breaks.isEmpty())) && (!(ignore_Forced_Line_Breaks.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX);
	 		
	 		if ((!(ignore_Nonbreaking_Space.isEmpty())) && (!(ignore_Nonbreaking_Space.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX);
	 		
	 		selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON); 
	         	        
	   }
	 
	 public void FrameMakerFilterOperation(Selenium selenium, String frameMaker_Filter_Name, 
			 String translate_Left_Master_Page, String translate_Right_Master_Page, String translate_Other_Master_Page)
	    {
	    	 selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
	       
	    	if ((selenium.isElementPresent("link=" + frameMaker_Filter_Name)) || (frameMaker_Filter_Name.isEmpty())
	    			|| (frameMaker_Filter_Name.equalsIgnoreCase("x"))){
	 			return;
	 		} else {
	 			selenium.click(FilterConfiguration.FRAME_MAKER_FILTER_ADD_BUTTON);
	 			selenium.type(
	 					FilterConfiguration.FM_9_FILTER_NAME_TEXT,
	 					frameMaker_Filter_Name);
	 		}
	 		if ((!(translate_Left_Master_Page.isEmpty())) && (!(translate_Left_Master_Page.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.FM_9_TRANSLATE_LEFT_MASTER_PAGE_CHECKBOX);
	 		
	 		if ((!(translate_Right_Master_Page.isEmpty())) && (!(translate_Right_Master_Page.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.FM_9_TRANSLATE_RIGHT_MASTER_PAGE_CHECKBOX);
	 		
	 		if ((!(translate_Other_Master_Page.isEmpty())) && (!(translate_Other_Master_Page.equalsIgnoreCase("x"))))
	 			selenium.check(FilterConfiguration.FM_9_TRANSLATE_OTHER_MASTER_PAGE_CHECKBOX);
	 		
	 		selenium.click(FilterConfiguration.FM_9_FILTER_SAVE_BUTTON); 
	         	        
	   }
	 
	 
	public void HTMLfilterOperation (Selenium selenium, String htmlFilterName, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping, String convert, String ignore, 
			String Add_rtl_directionality, String whitespaceHandling, 
			String localizeFunction, String baseTextFilter, String embeddableTags, String internalTag, String pairedTags, 
			String switchTagMap, String translatableAttribute, String unpairedTags, String whitePreservingTags) 
	

	{

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		
		if (selenium.isElementPresent("link=" + baseTextFilter)){
			
		} else {
			selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.BASE_FILTER_NAME_TEXT,
					baseTextFilter);
			if((!(base_Text_Filter_Internal_Text.isEmpty()))||(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
				String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
				
				selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
				for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
					selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
					selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
					selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
					selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
				}
				selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

			}
			
			if((!(base_Text_Filter_Escaping.isEmpty()))||(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
				String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
				
				selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
				for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
					selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
					selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
					selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
					selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
					selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
					selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
				}
				selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

			}
			selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
		}
		
		if (selenium.isElementPresent("link=" + htmlFilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.HTML_FILTER_NAME_TEXT,
					htmlFilterName);
		}

		if (convert.equalsIgnoreCase("O")){
			selenium.check(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
		} else {
			selenium.uncheck(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
		}
		
		if (ignore.equalsIgnoreCase("O")){
			selenium.check(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
		} else {
			selenium.uncheck(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
		}
		
		if (Add_rtl_directionality.equalsIgnoreCase("O")){
			selenium.check(FilterConfiguration.HTML_FILTER_ADD_RTL_DIRECTIONALITY_CHECKBOX);
		} else {
			selenium.uncheck(FilterConfiguration.HTML_FILTER_ADD_RTL_DIRECTIONALITY_CHECKBOX);
		}
		
		if (whitespaceHandling.equalsIgnoreCase("Preserve")){
			selenium.click(FilterConfiguration.HTML_FILTER_WHITESPACE_HANDLING_RADIO_2 + 1 + "]");
		} else if(whitespaceHandling.equalsIgnoreCase("Collapse")){
			selenium.click(FilterConfiguration.HTML_FILTER_WHITESPACE_HANDLING_RADIO_1);
		}
		
		if (!(localizeFunction.isEmpty())&&(!(localizeFunction.equalsIgnoreCase("x"))&&!(localizeFunction.equalsIgnoreCase("o")))){
			selenium.type(FilterConfiguration.HTML_FILTER_LOCALIZE_FUNTION_TEXT, localizeFunction);
		}
		
		if (!(baseTextFilter.isEmpty())){
			if (!(baseTextFilter.equalsIgnoreCase("X"))){
				selenium.select(FilterConfiguration.HTML_FILTER_BASE_TEXT_POST_FILTER_SELECT, "label=" + baseTextFilter);
			}
		}
		
		if (!(embeddableTags.isEmpty())){
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_EMBEDDABLE_LABEL);
			
			if (embeddableTags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(embeddableTags.equalsIgnoreCase("x"))){
				String[] iembeddableTags = embeddableTags.split(",");
				
				
				for (int i = 0; i < iembeddableTags.length; i++) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
					selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, iembeddableTags[i]);
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
					if (selenium.isTextPresent("Tag Name:")) {
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
					}
				}
				
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			
		}
		
		if (!pairedTags.isEmpty()){
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_PAIRED_LABEL);
			
			if (pairedTags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(pairedTags.equalsIgnoreCase("x"))){
				String[] ipairedTags = pairedTags.split(",");
				
				for (int i = 0; i < ipairedTags.length; i++) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
					selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, ipairedTags[i]);
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
					if (selenium.isTextPresent("Tag Name:")) {
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
					}
			}
			
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
		}
		
		if (!unpairedTags.isEmpty()){
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_UNPAIRED_LABEL);
			
			if (unpairedTags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(unpairedTags.equalsIgnoreCase("x"))){
				String[] iunpairedTags = unpairedTags.split(",");
				
				for (int i = 0; i < iunpairedTags.length; i++) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
					selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, iunpairedTags[i]);
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
					if (selenium.isTextPresent("Tag Name:")) {
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
					}
				}
				
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			
		}
		
		if (!switchTagMap.isEmpty()){
			
			
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_SWITCH_MAP_LABEL);
			
			if (switchTagMap.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(switchTagMap.equalsIgnoreCase("x"))){
				String[] iswitchTagMap = switchTagMap.split(",");
			
			for (int i = 0; i < iswitchTagMap.length; i++) {
				selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
				
				String[] iswitchTagMapValue = iswitchTagMap[i].split(";");
				selenium.type(FilterConfiguration.HTML_FILTER_TAG_MAP_KEY_TEXT, iswitchTagMapValue[0]);
				selenium.type(FilterConfiguration.HTML_FILTER_TAG_MAP_VALUE_TEXT, iswitchTagMapValue[1]);
				selenium.click(FilterConfiguration.HTML_FILTER_TAG_SAVE_BUTTON_SWITCHTAG);
				
				if (selenium.isTextPresent("Tag Key:")) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
				}
			}
			
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
		}
		
		if (!whitePreservingTags.isEmpty()){
							
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_WHITE_PRESERVING_LABEL);
			
			if (whitePreservingTags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(whitePreservingTags.equalsIgnoreCase("x"))){
			
				String[] iwhitePreservingTags = whitePreservingTags.split(",");
			
				for (int i = 0; i < iwhitePreservingTags.length; i++) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
					selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, iwhitePreservingTags[i]);
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
					if (selenium.isTextPresent("Tag Key:")) {
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
					}
			}
			
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		}
		}
		
		if (!translatableAttribute.isEmpty()){
			
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE_LABEL);
			
			if (translatableAttribute.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(translatableAttribute.equalsIgnoreCase("x"))){
			
				String[] itranslatableAttribute = translatableAttribute.split(",");
			for (int i = 0; i < itranslatableAttribute.length; i++) {
				selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
				selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, itranslatableAttribute[i]);
				selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
				if (selenium.isTextPresent("Tag Key:")) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
				}
			}
			
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		}
		}
		
		if (!internalTag.isEmpty()){
			
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_INTERNAL_LABEL);
			
			if (internalTag.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(internalTag.equalsIgnoreCase("x"))){
				String[] iinternalTag = internalTag.split(",");
				
			for (int i = 0; i < iinternalTag.length; i++) {
				selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
				selenium.type(FilterConfiguration.HTML_FILTER_TAG_INTERNAL_NAME_TEXT, iinternalTag[i]);
				selenium.click(FilterConfiguration.HTML_FILTER_TAG_SAVE_BUTTON_INTERNAL);
				if (selenium.isTextPresent("Internal Tag:")) {
					selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
				}
			}
			
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			}	
		
			
			selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
//			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			

	}
	
	public void HTMLfilterOperation (Selenium selenium, String htmlFilterName, String option, String whitePreservingTags) 
	

	{
		String[] iwhitePreservingTags = whitePreservingTags.split(",");
		
		
		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		
		switch (option)
		{
			case "Default": {return;}
			case "No_filter": {return;}

			case "Default_Options": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				break;
			}
			case "RTL_YES": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.check(FilterConfiguration.HTML_FILTER_ADD_RTL_DIRECTIONALITY_CHECKBOX);
				break;
			}
			case "RTL_NO": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.uncheck(FilterConfiguration.HTML_FILTER_ADD_RTL_DIRECTIONALITY_CHECKBOX);
				break;
			}
			case "Invalid_YES": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.check(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
				break;
			}
			
			case "Invalid_NO": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.uncheck(FilterConfiguration.HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX);
				break;
			}
			case "Collapse": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.click(FilterConfiguration.HTML_FILTER_WHITESPACE_HANDLING_RADIO_1);
				break;
			}
			case "Preserve": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.click(FilterConfiguration.HTML_FILTER_WHITESPACE_HANDLING_RADIO_2 + 1 + "]");
				break;
			}
			case "Collapse_Tag": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_WHITE_PRESERVING_LABEL);
				for (int i = 0; i < iwhitePreservingTags.length; i++) {
					if (!(iwhitePreservingTags[i].equalsIgnoreCase("o") || iwhitePreservingTags[i].equalsIgnoreCase("x"))){		
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
						selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, iwhitePreservingTags[i]);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
						if (selenium.isTextPresent("Tag Key:")) {
								selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
						}
					}
				}
					selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
					break;
			}
			case "Preserve_Tag": {
				if (selenium.isElementPresent("link=" + htmlFilterName)){
					return;
				} else {
					selenium.click(FilterConfiguration.HTML_FILTER_ADD_BUTTON);
					selenium.type(
							FilterConfiguration.HTML_FILTER_NAME_TEXT,
							htmlFilterName);
				}
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CONVERT_ENTITY_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_WHITESPACE_HANDLING_RADIO_2 + 1 + "]");
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_WHITE_PRESERVING_LABEL);
				for (int i = 0; i < iwhitePreservingTags.length; i++) {
					if (!(iwhitePreservingTags[i].equalsIgnoreCase("o") || iwhitePreservingTags[i].equalsIgnoreCase("x"))){
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_BUTTON_2);
						selenium.type(FilterConfiguration.HTML_FILTER_TAG_NAME_TEXT, iwhitePreservingTags[i]);
						selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_SAVE_BUTTON);
						if (selenium.isTextPresent("Tag Key:")) {
							selenium.click(FilterConfiguration.HTML_FILTER_TAG_ADD_CANCEL_BUTTON);
						}
					}
				}
					selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
					break;
			}
			default: {return;}
				
			}
					
		selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_EMBEDDABLE_LABEL);
		selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_PAIRED_LABEL);
		selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
		selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.HTML_FILTER_TAG_UNPAIRED_LABEL);
		selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			
			selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
//			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
	}
	
	public void XMLfilterOperation (
			Selenium selenium, String xmlFilterName, String xML_Rule,
			String convert_HTML_Entity_For_Export,
			String import_Export_Entities,
			String extended_Whitespace_Characters,
			String placeholder_Consolidation,
			String placeholder_Trimming,
			String save_non_ASCII_Characters_As,
			String whitespace_Handling,
			String empty_Tag_Format,
			String element_post_filter,
			String cDATA_post_filter,
			String sID_Support,
			String check_Well_Formedness,
			String generate_Language_Information,
			String base_Text_Filter_Internal_Text,
			String base_Text_Filter_Escaping,
			String base_Text_post_filter,
			String embeddable_Tags,
			String preserve_Whitespace_Tags,
			String translatable_Attribute_Tags,
			String content_Inclusion_Tags,
			String cDATA_post_filter_tags,
			String entities,
			String processing_Instructions,
			String internal_Tags,
			String source_Comment_from_XML_Comment,
			String source_Comment_from_XMLTag,
			String import_Filter
			) 
	

	{

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		if ((!(import_Filter.isEmpty()))&&(!(import_Filter.equalsIgnoreCase("x"))) 
				&& (!(import_Filter.equalsIgnoreCase("o")))){
			if (selenium.isElementPresent("link=" + xmlFilterName)){
				return;
			} else {
				selenium.click(FilterConfiguration.IMPORT_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				selenium.type(FilterConfiguration.IMPORT_DIRECTORY_TYPE,
						ConfigUtil.getConfigData("Base_Path") + import_Filter);
				selenium.click(FilterConfiguration.IMPORT_UPLOAD_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.MEDIUM_WAIT);
				selenium.click(FilterConfiguration.IMPORT_UPLOAD_OK_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);		
				return;
			}
			
				}
		if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		
		
		
		if (selenium.isElementPresent("link=" + xmlFilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.XML_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.XML_FILTER_NAME_TEXT,
					xmlFilterName);
		}
		
		if ((!(xML_Rule.isEmpty())) && (!(xML_Rule.equalsIgnoreCase("x"))) && (!(xML_Rule.equalsIgnoreCase("o")))){
				selenium.select(FilterConfiguration.XML_FILTER_XML_RULE_SELECT,xML_Rule);
			}
			
		
		if (selenium.isElementPresent(FilterConfiguration.XML_FILTER_CONVERT_ENTITY_CHECKBOX)){
		if (convert_HTML_Entity_For_Export.equalsIgnoreCase("o")){
			selenium.check(FilterConfiguration.XML_FILTER_CONVERT_ENTITY_CHECKBOX);
		} else {
			selenium.uncheck(FilterConfiguration.XML_FILTER_CONVERT_ENTITY_CHECKBOX);
		}
		}
		if (selenium.isElementPresent(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT)){
		if (!(import_Export_Entities.isEmpty()) && (!(import_Export_Entities.equalsIgnoreCase("x"))) 
				&& (!(import_Export_Entities.equalsIgnoreCase("o")))){
			switch (import_Export_Entities)
			{
				
				case "1": {
					selenium.select(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT, "label=" + "As XML <>&");
					break;
				}
				case "2": {
					selenium.select(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT, "label=" + "As XML <'>&\"");
					break;
				}
				case "3": {
					selenium.select(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT, "label=" + "As XHTML <>&");
					break;
				}
				case "4": {
					selenium.select(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT, "label=" + "As XHTML <'>&\"");
					break;
				}
				case "5": {
					selenium.select(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT, "label=" + "As XHTML Entities");
					break;
				}
				default: {
					selenium.select(FilterConfiguration.XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT, "label=" + "As XML <>&");
					break;
					}
			}
		
		}
		}
		
		
		if (!(extended_Whitespace_Characters.isEmpty())){
			if (!(extended_Whitespace_Characters.equalsIgnoreCase("x"))){
				selenium.type(FilterConfiguration.XML_FILTER_EXTENDED_WHITESPACE_CHARACTERS_TEXT, extended_Whitespace_Characters);
			}
		}
			
		if (!(placeholder_Consolidation.isEmpty())){
				switch (placeholder_Consolidation)
				{
					default: {break;}
					case "Do not consolidate": {
						selenium.select(FilterConfiguration.XML_FILTER_PLACEHOLDER_CONSOLIDATION_SELECT, 
								FilterConfiguration.XML_FILTER_PLACEHOLDER_CONSOLIDATION_DO_NOT_CONSOLIDATE_LABEL);
						break;
					}
					case "Consolidate adjacent": {
						selenium.select(FilterConfiguration.XML_FILTER_PLACEHOLDER_CONSOLIDATION_SELECT, 
								FilterConfiguration.XML_FILTER_PLACEHOLDER_CONSOLIDATION_CONSOLIDATE_ADJACENT_LABEL);
						break;
					}
					case "Consolidate adjacent ignore whitespace": {
						selenium.select(FilterConfiguration.XML_FILTER_PLACEHOLDER_CONSOLIDATION_SELECT, 
								FilterConfiguration.XML_FILTER_PLACEHOLDER_CONSOLIDATION_CONSOLIDATE_ADJACENT_IGNORE_WHITESPACE_LABEL);
						break;
					}
				}
					
			
		}
		if (!(placeholder_Trimming.isEmpty())){
			switch (placeholder_Trimming)
			{
				default: {break;}
				case "Do not trim": {
					selenium.select(FilterConfiguration.XML_FILTER_PLACEHOLDER_TRIMMING_SELECT, 
							FilterConfiguration.XML_FILTER_PLACEHOLDER_TRIMMING_DO_NOT_TRIM_LABEL);
					break;
				}
				case "Trim": {
					selenium.select(FilterConfiguration.XML_FILTER_PLACEHOLDER_TRIMMING_SELECT, 
							FilterConfiguration.XML_FILTER_PLACEHOLDER_TRIMMING_TRIM_SELECT);
					break;
				}
			}
		}
		if (!(save_non_ASCII_Characters_As.isEmpty())){
			if (save_non_ASCII_Characters_As.equalsIgnoreCase("Numeric Entity")){
				selenium.click(FilterConfiguration.XML_FILTER_SAVE_NON_ASCII_CHARACTERS_AS_2_RADIO);
			} else if (save_non_ASCII_Characters_As.equalsIgnoreCase("Character")){
				selenium.click(FilterConfiguration.XML_FILTER_SAVE_NON_ASCII_CHARACTERS_AS_1_RADIO);
			}
		}
		if(!(whitespace_Handling.isEmpty())){
			if (whitespace_Handling.equalsIgnoreCase("Preserve")){
				selenium.click(FilterConfiguration.XML_FILTER_WHITESPACE_HANDLING_2_RADIO);
			} else if (whitespace_Handling.equalsIgnoreCase("Collapse into Single Whitespace")) {
				selenium.click(FilterConfiguration.XML_FILTER_WHITESPACE_HANDLING_1_RADIO);
			}
		}
		if (!(empty_Tag_Format.isEmpty())){
			if (empty_Tag_Format.equalsIgnoreCase("Open")){
				selenium.click(FilterConfiguration.XML_FILTER_EMPTY_TAG_FORMAT_1_RADIO);
			} else if (empty_Tag_Format.equalsIgnoreCase("Close")){
				selenium.click(FilterConfiguration.XML_FILTER_EMPTY_TAG_FORMAT_2_RADIO);
			} else if (empty_Tag_Format.equalsIgnoreCase("Preserve as source")){
				selenium.click(FilterConfiguration.XML_FILTER_EMPTY_TAG_FORMAT_3_RADIO);
			}
		}
		if (!(element_post_filter.isEmpty())){
			if ((!(element_post_filter.equalsIgnoreCase("x"))) && (!(element_post_filter.equalsIgnoreCase("o")))){
				selenium.select(FilterConfiguration.XML_FILTER_ELEMENT_POST_FILTER_SELECT, "label=" + element_post_filter);
			}
		}
		if (!(cDATA_post_filter.isEmpty())){
			if ((!(cDATA_post_filter.equalsIgnoreCase("x"))) && (!(cDATA_post_filter.equalsIgnoreCase("o")))){
				selenium.select(FilterConfiguration.XML_FILTER_CDATA_POST_FILTER_SELECT, "label=" + cDATA_post_filter);
			}
		}
		if (!(sID_Support.isEmpty())){
			if ((!(sID_Support.equalsIgnoreCase("x"))) && (!(sID_Support.equalsIgnoreCase("o")))){
				
				String[] isID_Support = sID_Support.split(",");
				selenium.type(FilterConfiguration.XML_FILTER_SID_SUPPORT_TAG_NAME_TEXT, isID_Support[0]);
				selenium.type(FilterConfiguration.XML_FILTER_SID_SUPPORT_ATT_NAME_TEXT, isID_Support[1]);
			}
		}
		if (!(check_Well_Formedness.isEmpty())){
			if (check_Well_Formedness.equalsIgnoreCase("o")){
				selenium.check(FilterConfiguration.XML_FILTER_CHECK_WELL_FORMEDNESS_CHECKBOX);
			}
		}
		if (!(generate_Language_Information.isEmpty())){
			if (generate_Language_Information.equalsIgnoreCase("o")){
				selenium.check(FilterConfiguration.XML_FILTER_GENERATE_LANGUAGE_INFORMATION_CHECKBOX);
			}
		}
		if (!(base_Text_post_filter.isEmpty())){
			if ((!(base_Text_post_filter.equalsIgnoreCase("x"))) && (!(base_Text_post_filter.equalsIgnoreCase("o")))){
				selenium.select(FilterConfiguration.XML_FILTER_Base_TEXT_POST_FILTER_SELECT, "label=" + base_Text_post_filter);
			}
		}

		if (!(embeddable_Tags.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_EMBEDDABLE_LABEL);
			
			if (embeddable_Tags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(embeddable_Tags.equalsIgnoreCase("x"))){
				String[] iembeddable_Tags = embeddable_Tags.split(",");
				
				for (int i = 0; i < iembeddable_Tags.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_iembeddable_Tags = iembeddable_Tags[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_NAME_TEXT, i_iembeddable_Tags[0]);
					
					for (int j = 1; j < i_iembeddable_Tags.length; j++) {
						
						if (i_iembeddable_Tags[j].contains("=")){
							String[] j_iembeddable_Tags = i_iembeddable_Tags[j].split("=");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_iembeddable_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_iembeddable_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_iembeddable_Tags[j].contains("<>")){
							String[] j_iembeddable_Tags = i_iembeddable_Tags[j].split("<>");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_iembeddable_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_iembeddable_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_iembeddable_Tags[j].contains("~")){
							String[] j_iembeddable_Tags = i_iembeddable_Tags[j].split("~");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_iembeddable_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_iembeddable_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} 
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			
		}
	}
		if (!(translatable_Attribute_Tags.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE_LABEL);
			
			if (translatable_Attribute_Tags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(translatable_Attribute_Tags.equalsIgnoreCase("x"))){
				String[] itranslatable_Attribute_Tags = translatable_Attribute_Tags.split(",");
				
				for (int i = 0; i < itranslatable_Attribute_Tags.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_itranslatable_Attribute_Tags = itranslatable_Attribute_Tags[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_NAME_TEXT, i_itranslatable_Attribute_Tags[0]);
					for (int j = 0; j < i_itranslatable_Attribute_Tags.length; j++) {
						
						if (i_itranslatable_Attribute_Tags[j].contains("=")){
							String[] j_itranslatable_Attribute_Tags = i_itranslatable_Attribute_Tags[j].split("=");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_itranslatable_Attribute_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_itranslatable_Attribute_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_itranslatable_Attribute_Tags[j].contains("<>")){
							String[] j_itranslatable_Attribute_Tags = i_itranslatable_Attribute_Tags[j].split("<>");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_itranslatable_Attribute_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_itranslatable_Attribute_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_itranslatable_Attribute_Tags[j].contains("~")){
							String[] j_itranslatable_Attribute_Tags = i_itranslatable_Attribute_Tags[j].split("~");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_itranslatable_Attribute_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_itranslatable_Attribute_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (!(i_itranslatable_Attribute_Tags[j].isEmpty())){
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_NAME_TEXT, i_itranslatable_Attribute_Tags[j]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_ATT_ADD_BUTTON);
						}
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			}
		}	
	
		if (!(content_Inclusion_Tags.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_CONTENT_INCLUSION_LABEL);
			
			if (content_Inclusion_Tags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(content_Inclusion_Tags.equalsIgnoreCase("x"))){
				String[] icontent_Inclusion_Tags = content_Inclusion_Tags.split(",");
				
				for (int i = 0; i < icontent_Inclusion_Tags.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_icontent_Inclusion_Tags = icontent_Inclusion_Tags[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_NAME_TEXT, i_icontent_Inclusion_Tags[0]);
					for (int j = 0; j < i_icontent_Inclusion_Tags.length; j++) {
						
						if (i_icontent_Inclusion_Tags[j].contains("=")){
							String[] j_icontent_Inclusion_Tags = i_icontent_Inclusion_Tags[j].split("=");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_icontent_Inclusion_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_icontent_Inclusion_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_icontent_Inclusion_Tags[j].contains("<>")){
							String[] j_icontent_Inclusion_Tags = i_icontent_Inclusion_Tags[j].split("<>");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_icontent_Inclusion_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_icontent_Inclusion_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_icontent_Inclusion_Tags[j].contains("~")){
							String[] j_icontent_Inclusion_Tags = i_icontent_Inclusion_Tags[j].split("~");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_icontent_Inclusion_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_icontent_Inclusion_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_icontent_Inclusion_Tags[j].contains("Exclude")){
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_TYPE_RADIO_EXCLUDE);
						}else if (i_icontent_Inclusion_Tags[j].contains("Include")){
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_TYPE_RADIO_INCLUDE);
						}
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			}
		}	
		if (!(entities.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_ENTITIES_LABEL);
			
			if (entities.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(entities.equalsIgnoreCase("x"))){
				String[] ientities = entities.split(",");
				
				for (int i = 0; i < ientities.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_ientities = ientities[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_NAME_TEXT, i_ientities[0]);
					if (i_ientities[1].equalsIgnoreCase("PlaceHolder")){
						selenium.select(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_SELECT, 
								FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_PLACEHOLDER_LABEL);
					} else if (i_ientities[1].equalsIgnoreCase("Text")){
						selenium.select(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_SELECT, 
								FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_TEXT_LABEL);
						selenium.type(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_CODE_TEXT, i_ientities[2]);
						if (i_ientities[3].equalsIgnoreCase("Entity")){
							selenium.click(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_SAVE_AS_ENTITY_RADIO);
						}else if (i_ientities[3].equalsIgnoreCase("Character")){
							selenium.click(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_SAVE_AS_CHARACTER_RADIO);
						}
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_CONFIGURED_ENTITY_SAVE_BUTTON);
				}
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			}
		}
		if (!(processing_Instructions.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTIONS_LABEL);
			
			if (processing_Instructions.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(processing_Instructions.equalsIgnoreCase("x"))){
				String[] iprocessing_Instructions = processing_Instructions.split(",");
				
				for (int i = 0; i < iprocessing_Instructions.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_iprocessing_Instructions = iprocessing_Instructions[i].split(";");
					String temp = i_iprocessing_Instructions[0];
					selenium.type(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_NAME_TEXT, i_iprocessing_Instructions[0]);
					if (i_iprocessing_Instructions[1].equalsIgnoreCase("As Markup")){
						selenium.select(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_SELECT, 
								FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_AS_MARKUP_LABEL);
					} else if (i_iprocessing_Instructions[1].equalsIgnoreCase("As Embeddable Markup")){
						selenium.select(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_SELECT, 
								FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_AS_EMBEDDABLE_MARKUP_LABEL);
					} else if (i_iprocessing_Instructions[1].equalsIgnoreCase("Remove from Target")){
						selenium.select(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_SELECT, 
								FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_REMOVE_FROM_TARGET_LABEL);
					} else if (i_iprocessing_Instructions[1].equalsIgnoreCase("Extract for translation")){
						selenium.select(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_SELECT, 
								FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_EXTRACT_FOR_TRANSLATION_LABEL);
						if (i_iprocessing_Instructions.length>2){
							for (int j = 2; j<i_iprocessing_Instructions.length; j++){
								selenium.type(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_ATT_TEXT, i_iprocessing_Instructions[j]);
								selenium.click(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_ATT_ADD_BUTTON);	
							}
							
						}
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_PROCESSING_INSTRUCTION_SAVE_BUTTON);
				}
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			}
		}					
		
		if (!(preserve_Whitespace_Tags.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_PRESERVE_WHITESPACE_LABEL);
			
			if (preserve_Whitespace_Tags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(preserve_Whitespace_Tags.equalsIgnoreCase("x"))){
				String[] ipreserve_Whitespace_Tags = preserve_Whitespace_Tags.split(",");
				
				for (int i = 0; i < ipreserve_Whitespace_Tags.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_ipreserve_Whitespace_Tags = ipreserve_Whitespace_Tags[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_NAME_TEXT, i_ipreserve_Whitespace_Tags[0]);
					
					for (int j = 1; j < i_ipreserve_Whitespace_Tags.length; j++) {
						
						if (i_ipreserve_Whitespace_Tags[j].contains("=")){
							String[] j_ipreserve_Whitespace_Tags = i_ipreserve_Whitespace_Tags[j].split("=");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_ipreserve_Whitespace_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_ipreserve_Whitespace_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_ipreserve_Whitespace_Tags[j].contains("<>")){
							String[] j_ipreserve_Whitespace_Tags = i_ipreserve_Whitespace_Tags[j].split("<>");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_ipreserve_Whitespace_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_ipreserve_Whitespace_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_ipreserve_Whitespace_Tags[j].contains("~")){
							String[] j_ipreserve_Whitespace_Tags = i_ipreserve_Whitespace_Tags[j].split("~");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_ipreserve_Whitespace_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_ipreserve_Whitespace_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} 
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			
		}
	}
		if (!(internal_Tags.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_INTERNAL_Tag_LABEL);
			
			if (internal_Tags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(internal_Tags.equalsIgnoreCase("x"))){
				String[] iinternal_Tags = internal_Tags.split(",");
				
				for (int i = 0; i < iinternal_Tags.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_iinternal_Tags = iinternal_Tags[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_NAME_TEXT, i_iinternal_Tags[0]);
					
					for (int j = 1; j < i_iinternal_Tags.length; j++) {
						
						if (i_iinternal_Tags[j].contains("=")){
							String[] j_iinternal_Tags = i_iinternal_Tags[j].split("=");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_iinternal_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_iinternal_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_iinternal_Tags[j].contains("<>")){
							String[] j_iinternal_Tags = i_iinternal_Tags[j].split("<>");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_iinternal_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_iinternal_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_iinternal_Tags[j].contains("~")){
							String[] j_iinternal_Tags = i_iinternal_Tags[j].split("~");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_iinternal_Tags[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_iinternal_Tags[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} 
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			
		}
	}
		if (!(cDATA_post_filter_tags.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_LABEL);
			
			if (cDATA_post_filter_tags.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(cDATA_post_filter_tags.equalsIgnoreCase("x"))){
				String[] icDATA_post_filter_tags = cDATA_post_filter_tags.split(",");
				
				for (int i = 0; i < icDATA_post_filter_tags.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_icDATA_post_filter_tags = icDATA_post_filter_tags[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_NAME_TEXT, i_icDATA_post_filter_tags[0]);
					
					for (int j = 1; j < i_icDATA_post_filter_tags.length; j++) {
						
						if (i_icDATA_post_filter_tags[j].contains(":")){
							String[] j_icDATA_post_filter_tags = i_icDATA_post_filter_tags[j].split(":");
							if (j_icDATA_post_filter_tags[0].equalsIgnoreCase("Condition")){
								selenium.type(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_COND_RES_TEXT, j_icDATA_post_filter_tags[1]);
								selenium.click(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_ADD_BUTTON);	
							} else if (j_icDATA_post_filter_tags[0].equalsIgnoreCase("Post-Filter")){
								selenium.select(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_FILTER_SELECT, j_icDATA_post_filter_tags[1]);
							} else if (j_icDATA_post_filter_tags[0].equalsIgnoreCase("Translatable")){
								if (j_icDATA_post_filter_tags[1].equalsIgnoreCase("True")){
									selenium.check(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_TRANSLATABLE_CHECKBOX);
								} else if (j_icDATA_post_filter_tags[1].equalsIgnoreCase("False")){
									selenium.uncheck(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_TRANSLATABLE_CHECKBOX);
								}
									
							}
						}
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_CDATA_POST_FILTER_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			
		}
	}
		if (!(source_Comment_from_XML_Comment.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, 
					FilterConfiguration.XML_FILTER_TAG_SOURCE_COMMENT_FROM_XML_COMMENT_LABEL);
			
			if (source_Comment_from_XML_Comment.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(source_Comment_from_XML_Comment.equalsIgnoreCase("x"))){
				String[] isource_Comment_from_XML_Comment = source_Comment_from_XML_Comment.split(",");
				
				for (int i = 0; i < isource_Comment_from_XML_Comment.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_isource_Comment_from_XML_Comment = isource_Comment_from_XML_Comment[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_NAME, i_isource_Comment_from_XML_Comment[0]);
					if (i_isource_Comment_from_XML_Comment.length>1){
						if (i_isource_Comment_from_XML_Comment[1].equalsIgnoreCase("True"))
							selenium.check(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_IS_RE_CHECKBOX);
						else if (i_isource_Comment_from_XML_Comment[1].equalsIgnoreCase("False"))
							selenium.uncheck(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_IS_RE_CHECKBOX);
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			
		}
	}		
			
		if (!(source_Comment_from_XMLTag.isEmpty())){
			selenium.select(FilterConfiguration.XML_FILTER_TAG_TYPE_SELECT, 
					FilterConfiguration.XML_FILTER_TAG_SOURCE_COMMENT_FROM_XML_TAG_LABEL);
			
			if (source_Comment_from_XMLTag.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(source_Comment_from_XMLTag.equalsIgnoreCase("x"))){
				String[] isource_Comment_from_XMLTag = source_Comment_from_XMLTag.split(",");
				
				for (int i = 0; i < isource_Comment_from_XMLTag.length; i++) {
					selenium.click(FilterConfiguration.XML_FILTER_TAG_ADD_BUTTON);
					String[] i_isource_Comment_from_XMLTag = isource_Comment_from_XMLTag[i].split(";");
					selenium.type(FilterConfiguration.XML_FILTER_TAG_NAME_TEXT, i_isource_Comment_from_XMLTag[0]);
					for (int j = 0; j < i_isource_Comment_from_XMLTag.length; j++) {
						
						if (i_isource_Comment_from_XMLTag[j].contains("=")){
							String[] j_isource_Comment_from_XMLTag = i_isource_Comment_from_XMLTag[j].split("=");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_isource_Comment_from_XMLTag[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_isource_Comment_from_XMLTag[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_isource_Comment_from_XMLTag[j].contains("<>")){
							String[] j_isource_Comment_from_XMLTag = i_isource_Comment_from_XMLTag[j].split("<>");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_isource_Comment_from_XMLTag[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_isource_Comment_from_XMLTag[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_isource_Comment_from_XMLTag[j].contains("~")){
							String[] j_isource_Comment_from_XMLTag = i_isource_Comment_from_XMLTag[j].split("~");
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ITEM_TEXT, j_isource_Comment_from_XMLTag[0]);
							selenium.select(FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_SELECT, 
									FilterConfiguration.XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_COND_ATT_RES_TEXT, j_isource_Comment_from_XMLTag[1]);
							selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_ATT_ADD_BUTTON);
						} else if (i_isource_Comment_from_XMLTag[j].contains(":")){
							String[] j_isource_Comment_from_XMLTag = i_isource_Comment_from_XMLTag[j].split(":");
							selenium.click(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_TAG_FROM_ATT_RADIO);
							selenium.type(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_TAG_FROM_ATT_TEXT,j_isource_Comment_from_XMLTag[1]);
						} else if (i_isource_Comment_from_XMLTag[j].contains("From Tag")){
							selenium.click(FilterConfiguration.XML_FILTER_TAG_SRC_CMT_FROM_XML_TAG_FROM_TAG_RADIO);
						}
					}
					selenium.click(FilterConfiguration.XML_FILTER_TAG_COND_SAVE_BUTTON);
			}
			selenium.click(FilterConfiguration.XML_FILTER_CHECK_ALL_CHECKBOX);
			
			}
		}		
			
			selenium.click(FilterConfiguration.XML_FILTER_SAVE_BUTTON);
//			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			if (selenium.isAlertPresent()) 
			Assert.assertEquals(selenium.getAlert(), FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE);

			

	}
	
	public void Office_2010_filterOperation (
			Selenium selenium, String office_2010_FilterName, String translate_Header_Footer_Information,
			String translate_Footnotes_Endnotes,
			String translate_PPT_Speakers_Notes,
			String translate_PPT_Slide_Master,
			String translate_PPT_Slide_Layout,
			String translate_PPT_Notes_Master,
			String translate_PPT_Handout_Master,
			String translate_Excel_Tab_Names,
			String translate_Hidden_Text,
			String translate_ToolTips,
			String translate_URLs,
			String translate_Table_Of_Content,
			String translate_Comment,
			String excel_Segment_Order_for_v20_only,
			String content_post_filter,
			String base_Text_post_filter,
			String base_Text_Filter_Internal_Text,
			String base_Text_Filter_Escaping,
			String unextractable_Word_Character_Styles,
			String unextractable_Word_Paragraph_Styles,
			String unextractable_Excel_Cell_Styles,
			String word_Internal_Text_Character_Styles,
			String excel_Internal_Text_Cell_Styles
			) 
	

	{

		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
		
		
		if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		
		
		
		if (selenium.isElementPresent("link=" + office_2010_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.OFFICE_2010_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.OFFICE_2010_FILTER_NAME_TEXT,
					office_2010_FilterName);
		}
		if ((!(translate_Header_Footer_Information.isEmpty())) && (!(translate_Header_Footer_Information.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX);
		
		if ((!(translate_Footnotes_Endnotes.isEmpty())) && (!(translate_Header_Footer_Information.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_FOOTER_NOTE_TRANSLATE_CHECKBOX);
		
		if ((!(translate_PPT_Speakers_Notes.isEmpty())) && (!(translate_PPT_Speakers_Notes.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_PPT_SPEAKERS_NOTES_TRANSLATE_CHECKBOX);
		
		if ((!(translate_PPT_Slide_Master.isEmpty())) && (!(translate_PPT_Slide_Master.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX);
		
		if ((!(translate_PPT_Slide_Layout.isEmpty())) && (!(translate_PPT_Slide_Layout.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_LAYOUT_TRANSLATE_CHECKBOX);
		
		if ((!(translate_PPT_Notes_Master.isEmpty())) && (!(translate_PPT_Notes_Master.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_PPT_NOTES_MASTER_TRANSLATE_CHECKBOX);
		
		if ((!(translate_PPT_Handout_Master.isEmpty())) && (!(translate_PPT_Handout_Master.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_PPT_HANDOUT_TRANSLATE_CHECKBOX);
		
		
		if ((!(translate_Excel_Tab_Names.isEmpty())) && (!(translate_Excel_Tab_Names.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_EXCEL_TAB_NAMES_TRANSLATE_CHECKBOX);
		
		if ((!(translate_Hidden_Text.isEmpty())) && (!(translate_Hidden_Text.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_HIDDEN_TEXT_TRANSLATE_CHECKBOX);
		
		if ((!(translate_ToolTips.isEmpty())) && (!(translate_ToolTips.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_TOOLTIPS_TRANSLATE_CHECKBOX);
		
		
		if ((!(translate_URLs.isEmpty())) && (!(translate_URLs.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_URL_TRANSLATE_CHECKBOX);
		
		if ((!(translate_Table_Of_Content.isEmpty())) && (!(translate_Table_Of_Content.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_TOC_TRANSLATE_CHECKBOX);
		
		if ((!(translate_Comment.isEmpty())) && (!(translate_Comment.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_2010_FILTER_COMMENT_TRANSLATE_CHECKBOX);
		
		if (!(excel_Segment_Order_for_v20_only.isEmpty())){
			switch (excel_Segment_Order_for_v20_only)
			{
				default: {break;}
				case "Do not order": {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_EXCEL_SEGMENT_ORDER_FOR_V20_ONLY_RADEO_1);
					break;
				}
				case "By row": {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_EXCEL_SEGMENT_ORDER_FOR_V20_ONLY_RADEO_2);
					break;
				}
				case "By column": {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_EXCEL_SEGMENT_ORDER_FOR_V20_ONLY_RADEO_3);
					break;
				}
			}
		}
		
		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
			
			if  (content_post_filter.equalsIgnoreCase("o")){
				selenium.select(FilterConfiguration.OFFICE_2010_FILTER_CONTENT_POST_FILTER_SELECT, 
						FilterConfiguration.OFFICE_2010_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL);
			} else selenium.select(FilterConfiguration.OFFICE_2010_FILTER_CONTENT_POST_FILTER_SELECT, content_post_filter);
			
		}
			
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		
		
		
		
		
		
		
		if (!(unextractable_Word_Character_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_2010_FILTER_TAG_TYPE_SELECT, FilterConfiguration.OFFICE_2010_FILTER_TAG_UNEXTRACTABLE_WORD_CHARACTER_STYLES_LABEL);
			
			if (unextractable_Word_Character_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			} else if(!(unextractable_Word_Character_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Word_Character_Styles = unextractable_Word_Character_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Word_Character_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Word_Character_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_SAVE_BUTTON);
					
					}
					
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			}
			
			
		}
		
		if (!(unextractable_Word_Paragraph_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_2010_FILTER_TAG_TYPE_SELECT, FilterConfiguration.OFFICE_2010_FILTER_TAG_UNEXTRACTABLE_WORD_PARAGRAPH_STYLES_LABEL);
			
			if (unextractable_Word_Paragraph_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			} else if(!(unextractable_Word_Paragraph_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Word_Paragraph_Styles = unextractable_Word_Paragraph_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Word_Paragraph_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Word_Paragraph_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_SAVE_BUTTON);
					
					}
					
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			}
			
			
		}
		
		if (!(unextractable_Excel_Cell_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_2010_FILTER_TAG_TYPE_SELECT, FilterConfiguration.OFFICE_2010_FILTER_TAG_UNEXTRACTABLE_EXCEL_CELL_STYLES_LABEL);
			
			if (unextractable_Excel_Cell_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			} else if(!(unextractable_Excel_Cell_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Excel_Cell_Styles = unextractable_Excel_Cell_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Excel_Cell_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Excel_Cell_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_SAVE_BUTTON);
					
					}
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			}
			
			
		}
		
		if (!(word_Internal_Text_Character_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_2010_FILTER_TAG_TYPE_SELECT, FilterConfiguration.OFFICE_2010_FILTER_TAG_WORD_INTERNAL_TEXT_CHARACTER_STYLES_LABEL);
			
			if (word_Internal_Text_Character_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			} else if(!(word_Internal_Text_Character_Styles.equalsIgnoreCase("x"))){
				String[] iword_Internal_Text_Character_Styles = word_Internal_Text_Character_Styles.split(",");
				
				for (int i = 0; i < iword_Internal_Text_Character_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_NAME_TEXT, iword_Internal_Text_Character_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_SAVE_BUTTON);
					
					}
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			}
			
			
		}
		if (!(excel_Internal_Text_Cell_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_2010_FILTER_TAG_TYPE_SELECT, FilterConfiguration.OFFICE_2010_FILTER_TAG_EXCEL_INTERNAL_TEXT_CELL_STYLES_LABEL);
			
			if (excel_Internal_Text_Cell_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			} else if(!(excel_Internal_Text_Cell_Styles.equalsIgnoreCase("x"))){
				String[] iexcel_Internal_Text_Cell_Styles = excel_Internal_Text_Cell_Styles.split(",");
				
				for (int i = 0; i < iexcel_Internal_Text_Cell_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_NAME_TEXT, iexcel_Internal_Text_Cell_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_SAVE_BUTTON);
					
					}
				selenium.click(FilterConfiguration.OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX);
			}
			
			
		}
		
		
		
			
			selenium.click(FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
//			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			 if (selenium.isAlertPresent()) 
		            selenium.getAlert();
		        if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
		        	selenium.close();
//			Assert.assertEquals(selenium.getAlert(), FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE);

			

	}
	
    public void wordFilterOperation(Selenium selenium, String word_2007_FilterName, String translate_Header_Information,
            String translate_ToolTips, String translate_Table_Of_Content, String content_post_filter,
            String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping,
            String unextractable_Word_Paragraph_Styles, 
            String unextractable_Word_Character_Styles, String internal_Text_Character_Styles)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		

		if (selenium.isElementPresent("link=" + word_2007_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.OFFICE_DOC_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.OFFICE_WORD_FILTER_NAME_TEXT,
					word_2007_FilterName);
		}
		if ((!(translate_Header_Information.isEmpty())) && (!(translate_Header_Information.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_WORD_FILTER_HEADER_CHECKBOX);
		
		if ((!(translate_ToolTips.isEmpty())) && (!(translate_ToolTips.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_WORD_FILTER_TOOLTIPS_CHECKBOX);
		
		if ((!(translate_Table_Of_Content.isEmpty())) && (!(translate_Table_Of_Content.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OFFICE_WORD_FILTER_TOC_TRANSLATE_CHECKBOX);
		
		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
			
			if  (content_post_filter.equalsIgnoreCase("o")){
				selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_CONTENT_POST_FILTER_SELECT, 
						FilterConfiguration.OFFICE_WORD_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL);
			} else selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_CONTENT_POST_FILTER_SELECT, content_post_filter);
			
		}
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		
		if (!(unextractable_Word_Paragraph_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_PARAGRAPH);
			
			if (unextractable_Word_Paragraph_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(unextractable_Word_Paragraph_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Word_Paragraph_Styles = unextractable_Word_Paragraph_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Word_Paragraph_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Word_Paragraph_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON);
					
					}
					
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			
		}
		
		if (!(unextractable_Word_Character_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_CHARACTER);
			
			if (unextractable_Word_Character_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(unextractable_Word_Character_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Word_Character_Styles = unextractable_Word_Character_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Word_Character_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Word_Character_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON);
					
					}
					
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			
		}
		
		if (!(internal_Text_Character_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_CHARACTER);
			
			if (internal_Text_Character_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(internal_Text_Character_Styles.equalsIgnoreCase("x"))){
				String[] iinternal_Text_Character_Styles = internal_Text_Character_Styles.split(",");
				
				for (int i = 0; i < iinternal_Text_Character_Styles.length; i++) {
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_ADD_BUTTON);
					selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_TAG_STYLES_NAME_TEXT, iinternal_Text_Character_Styles[i]);
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON);
					
					}
				selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			
		}
		
		
		
		selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
        	selenium.close();
        
   }
    
    public void excelFilterOperation(Selenium selenium, String excel_2007_FilterName, 
    		String translate_ToolTips, String translate_Excel_Tab_Names, String content_post_filter,
    		String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping)
    {
    	 selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
         if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
 			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
 				
 			} else {
 				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
 				selenium.type(
 						FilterConfiguration.BASE_FILTER_NAME_TEXT,
 						base_Text_post_filter);
 				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
 					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
 					
 					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
 					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
 						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
 						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
 						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
 						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
 					}
 					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

 				}
 				
 				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
 					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
 					
 					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
 					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
 						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
 						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
 						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
 						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
 						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
 						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
 					}
 					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

 				}
 				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
 			}
 			
 		}
 		

 		if (selenium.isElementPresent("link=" + excel_2007_FilterName)){
 			return;
 		} else {
 			selenium.click(FilterConfiguration.OFFICE_XLS_FILTER_ADD_BUTTON);
 			selenium.type(
 					FilterConfiguration.OFFICE_EXCEL_FILTER_NAME_TEXT,
 					excel_2007_FilterName);
 		}
 		if ((!(translate_ToolTips.isEmpty())) && (!(translate_ToolTips.equalsIgnoreCase("x"))))
 			selenium.check(FilterConfiguration.OFFICE_EXCEL_FILTER_TOOLTIPS_CHECKBOX);
 		
 		if ((!(translate_Excel_Tab_Names.isEmpty())) && (!(translate_Excel_Tab_Names.equalsIgnoreCase("x"))))
 			selenium.check(FilterConfiguration.OFFICE_EXCEL_FILTER_TABNAMES_CHECKBOX);
 		
 		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
 			
 			if  (content_post_filter.equalsIgnoreCase("o")){
 				selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_SELECT, 
 						FilterConfiguration.OFFICE_WORD_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL);
 			} else selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_SELECT, content_post_filter);
 			
 		}
 		
 		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
 			selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
 		
 		selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_SAVE_BUTTON); 
         if (selenium.isAlertPresent()) 
             selenium.getAlert();
         if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
         	selenium.close();
        
   }
    public void powerpointFilterOperation(Selenium selenium, String powerpoint_2007_FilterName, 
    		String translate_ToolTips, String translate_Notes, String content_post_filter,
    		String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping)
    {
        	 selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
             if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
     			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
     				
     			} else {
     				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
     				selenium.type(
     						FilterConfiguration.BASE_FILTER_NAME_TEXT,
     						base_Text_post_filter);
     				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
     					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
     					
     					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
     					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
     						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
     						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
     						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
     						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
     					}
     					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

     				}
     				
     				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
     					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
     					
     					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
     					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
     						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
     						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
     						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
     						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
     						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
     						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
     					}
     					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

     				}
     				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
     			}
     			
     		}
     		

     		if (selenium.isElementPresent("link=" + powerpoint_2007_FilterName)){
     			return;
     		} else {
     			selenium.click(FilterConfiguration.OFFICE_PPT_FILTER_ADD_BUTTON);
     			selenium.type(
     					FilterConfiguration.OFFICE_POWERPOINT_FILTER_NAME_TEXT,
     					powerpoint_2007_FilterName);
     		}
     		if ((!(translate_ToolTips.isEmpty())) && (!(translate_ToolTips.equalsIgnoreCase("x"))))
     			selenium.check(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TOOLTIPS_CHECKBOX);
     		
     		if ((!(translate_Notes.isEmpty())) && (!(translate_Notes.equalsIgnoreCase("x"))))
     			selenium.check(FilterConfiguration.OFFICE_POWERPOINT_FILTER_NOTES_CHECKBOX);
     		
     		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
     			
     			if  (content_post_filter.equalsIgnoreCase("o")){
     				selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT, 
     						FilterConfiguration.OFFICE_WORD_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL);
     			} else selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT, content_post_filter);
     			
     		}
     		
     		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
     			selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TEXT_SELECT, base_Text_post_filter);
     		
     		selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_SAVE_BUTTON); 
             if (selenium.isAlertPresent()) 
                 selenium.getAlert();
             if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
             	selenium.close();
    	
    	
        
   }

    
    
    public void openOfficeFilterOperation(Selenium selenium, String openOffice_FilterName, String translate_Header_Information,
            String unextractable_Word_Paragraph_Styles, String unextractable_Word_Character_Styles)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
       
		if (selenium.isElementPresent("link=" + openOffice_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.OPEN_OFFICE_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.OPENOFFICE_FILTER_NAME_TEXT,
					openOffice_FilterName);
		}
		if ((!(translate_Header_Information.isEmpty())) && (!(translate_Header_Information.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.OPENOFFICE_FILTER_HEADER_INFO_CHECKBOX);
		
		if (!(unextractable_Word_Paragraph_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OPENOFFICE_FILTER_STYLE_SELECT, FilterConfiguration.OPENOFFICE_FILTER_STYLE_PARAGRAPH_VALUE);
			
			if (unextractable_Word_Paragraph_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OPENOFFICE_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(unextractable_Word_Paragraph_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Word_Paragraph_Styles = unextractable_Word_Paragraph_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Word_Paragraph_Styles.length; i++) {
					selenium.click(FilterConfiguration.OPENOFFICE_FILTER_STYLE_ADD_BUTTON);
					selenium.type(FilterConfiguration.OPENOFFICE_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Word_Paragraph_Styles[i]);
					selenium.click(FilterConfiguration.OPENOFFICE_FILTER_STYLE_SAVE_BUTTON);
					
					}
					
					selenium.click(FilterConfiguration.OPENOFFICE_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			
		}
		
		if (!(unextractable_Word_Character_Styles.isEmpty())){
			selenium.select(FilterConfiguration.OPENOFFICE_FILTER_STYLE_SELECT, FilterConfiguration.OPENOFFICE_FILTER_STYLE_CHARACTER_VALUE);
			
			if (unextractable_Word_Character_Styles.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.OPENOFFICE_FILTER_CHECK_ALL_CHECKBOX);
			} else if(!(unextractable_Word_Character_Styles.equalsIgnoreCase("x"))){
				String[] iunextractable_Word_Character_Styles = unextractable_Word_Character_Styles.split(",");
				
				for (int i = 0; i < iunextractable_Word_Character_Styles.length; i++) {
					selenium.click(FilterConfiguration.OPENOFFICE_FILTER_STYLE_ADD_BUTTON);
					selenium.type(FilterConfiguration.OPENOFFICE_FILTER_TAG_STYLES_NAME_TEXT, iunextractable_Word_Character_Styles[i]);
					selenium.click(FilterConfiguration.OPENOFFICE_FILTER_STYLE_SAVE_BUTTON);
					
					}
					
					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			
		}

		
		
		
		selenium.click(FilterConfiguration.OPENOFFICE_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
        
   }
    
    public void plainTextFilterOperation(Selenium selenium, String plain_Text_FilterName, 
            String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping,
            String content_post_filter, String custom_Text_Rule, String custom_SID_Rule)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (!(selenium.isElementPresent("link=" + base_Text_post_filter))){
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		

		if (selenium.isElementPresent("link=" + plain_Text_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.PLAIN_TEXT_FILTER_NAME_TEXT,
					plain_Text_FilterName);
		}
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.PLAIN_TEXT_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
			
			if  (content_post_filter.equalsIgnoreCase("o")){
				selenium.select(FilterConfiguration.PLAIN_TEXT_FILTER_SECONDARY_FILTER_SELECT, 
						FilterConfiguration.PLAIN_TEXT_FILTER_SECONDARY_FILTER_DEFAULT_FILTER_LABEL);
			} else selenium.select(FilterConfiguration.PLAIN_TEXT_FILTER_SECONDARY_FILTER_SELECT, content_post_filter);
			
		}
		
		if (!(custom_Text_Rule.isEmpty())){
			selenium.select(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_SELECT, FilterConfiguration.PLAIN_TEXT_FILTER_RULE_SELECT_VALUE_CUSTOM_TEXT_RULE);
			
			if (custom_Text_Rule.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_CHECK_ALL_CHECKBOX);
			} else if(!(custom_Text_Rule.equalsIgnoreCase("x"))){
				String[] icustom_Text_Rule = custom_Text_Rule.split(",");
				
				for (int i = 0; i < icustom_Text_Rule.length; i++) {
					selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_ADD_BUTTON);
					String[] i_icustom_Text_Rule = icustom_Text_Rule[i].split(";");
					
					for (int j = 0; j < i_icustom_Text_Rule.length; j++) {
						
						String[] j_icustom_Text_Rule = i_icustom_Text_Rule[j].split(":=");
						switch (j_icustom_Text_Rule[0])
						{
							case "StartString":
							{
								selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_STR_TEXT, j_icustom_Text_Rule[1]);
								break;
							}
							case "Start_Is_RegEx":
							{
								if (j_icustom_Text_Rule[1].equalsIgnoreCase("yes"))
									selenium.check(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_IS_REGEX_CHECKBOX);
								break;
							}
							case "Start_Occurrence":
							{
								if (j_icustom_Text_Rule[1].equalsIgnoreCase("FIRST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_FIRST_RADIO_BUTTON);
								else if (j_icustom_Text_Rule[1].equalsIgnoreCase("LAST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_LAST_RADIO_BUTTON);
								else if (!(j_icustom_Text_Rule[1].isEmpty()))
								{	selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_CUSTOM_RADIO_BUTTON);
									selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_CUSTOM_TEXT, j_icustom_Text_Rule[1]);
								}
								break;
							}
							
							case "Finish_String":
							{
								selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_STR_TEXT, j_icustom_Text_Rule[1]);
								break;
							}	
							
							case "Finish_Is_RegEx":
							{
								if (j_icustom_Text_Rule[1].equalsIgnoreCase("yes"))
									selenium.check(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_IS_REGEX_CHECKBOX);
								break;
							}
							
							case "Finish_Occurrence":
							{
								if (j_icustom_Text_Rule[1].equalsIgnoreCase("FIRST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_FIRST_RADIO_BUTTON);
								else if (j_icustom_Text_Rule[1].equalsIgnoreCase("LAST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_LAST_RADIO_BUTTON);
								else if (!(j_icustom_Text_Rule[1].isEmpty()))
								{	selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_CUSTOM_RADIO_BUTTON);
									selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_CUSTOM_TEXT, j_icustom_Text_Rule[1]);
								}
								break;
							}
							
							case "Multiline":
							{
								if (j_icustom_Text_Rule[1].equalsIgnoreCase("yes"))
									selenium.check(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_IS_MULTILINE_TEXT);
								break;
							}
							
							case "Priority":
							{
								selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_PRIORITY_TEXT, j_icustom_Text_Rule[1]);
								break;
							}
							
							default:
								break;
						}
						
					}
					
					 selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_SAVE_BUTTON);
				}
			}
		}
		
		if (!(custom_SID_Rule.isEmpty())){
			selenium.select(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_SELECT, FilterConfiguration.PLAIN_TEXT_FILTER_RULE_SELECT_VALUE_CUSTOM_SID_RULE);
			
			if (custom_SID_Rule.equalsIgnoreCase("o")){
				selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_CHECK_ALL_CHECKBOX);
			} else if(!(custom_SID_Rule.equalsIgnoreCase("x"))){
				String[] icustom_SID_Rule = custom_SID_Rule.split(",");
				
				for (int i = 0; i < icustom_SID_Rule.length; i++) {
					selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_ADD_BUTTON);
					String[] i_icustom_SID_Rule = icustom_SID_Rule[i].split(";");
					
					for (int j = 0; j < i_icustom_SID_Rule.length; j++) {
						
						String[] j_icustom_SID_Rule = i_icustom_SID_Rule[j].split(":=");
						switch (j_icustom_SID_Rule[0])
						{
							case "StartString":
							{
								selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_STR_TEXT, j_icustom_SID_Rule[1]);
								break;
							}
							case "Start_Is_RegEx":
							{
								if (j_icustom_SID_Rule[1].equalsIgnoreCase("yes"))
									selenium.check(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_IS_REGEX_CHECKBOX);
								break;
							}
							case "Start_Occurrence":
							{
								if (j_icustom_SID_Rule[1].equalsIgnoreCase("FIRST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_FIRST_RADIO_BUTTON);
								else if (j_icustom_SID_Rule[1].equalsIgnoreCase("LAST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_LAST_RADIO_BUTTON);
								else if (!(j_icustom_SID_Rule[1].isEmpty()))
								{	selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_CUSTOM_RADIO_BUTTON);
									selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_CUSTOM_TEXT, j_icustom_SID_Rule[1]);
								}
								break;
							}
							
							case "Finish_String":
							{
								selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_STR_TEXT, j_icustom_SID_Rule[1]);
								break;
							}	
							
							case "Finish_Is_RegEx":
							{
								if (j_icustom_SID_Rule[1].equalsIgnoreCase("yes"))
									selenium.check(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_IS_REGEX_CHECKBOX);
								break;
							}
							
							case "Finish_Occurrence":
							{
								if (j_icustom_SID_Rule[1].equalsIgnoreCase("FIRST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_FIRST_RADIO_BUTTON);
								else if (j_icustom_SID_Rule[1].equalsIgnoreCase("LAST"))
									selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_LAST_RADIO_BUTTON);
								else if (!(j_icustom_SID_Rule[1].isEmpty()))
								{	selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_CUSTOM_RADIO_BUTTON);
									selenium.type(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_CUSTOM_TEXT, j_icustom_SID_Rule[1]);
								}
								break;
							}
							
							default:
								break;
						}
						
					}
					selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_SAVE_BUTTON);
				}
			}
		}
		selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_RULE_CHECK_ALL_CHECKBOX);
		selenium.click(FilterConfiguration.PLAIN_TEXT_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
        	selenium.close();
        
   }
    
    public void poFilter(Selenium selenium, String po_FilterName, String content_post_filter,
            String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping
            )
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		

		if (selenium.isElementPresent("link=" + po_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.PO_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.PO_FILTER_NAME_TEXT,
					po_FilterName);
		}
	
		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
			
			if  (content_post_filter.equalsIgnoreCase("o")){
				selenium.select(FilterConfiguration.PO_FILTER_SECONDARY_FILTER_SELECT, 
						FilterConfiguration.PO_FILTER_SECONDARY_FILTER_DEFAULT_FILTER_LABEL);
			} else selenium.select(FilterConfiguration.PO_FILTER_SECONDARY_FILTER_SELECT, content_post_filter);
			
		}
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.PO_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		


		
		
		selenium.click(FilterConfiguration.PO_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
        	selenium.close();
        
   }
    
    public void qaFilterOperation(Selenium selenium, String qa_FilterName, 
            String qa_Rule, String source_equal_to_Target, String target_string_expansion_of_or_more)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
       int length_of_QA_rule = 0;
       int line_of_source_equal_to_target;
       int target_string_expansion;
       line_of_source_equal_to_target = length_of_QA_rule + 3;
       target_string_expansion = length_of_QA_rule +4;

		if (selenium.isElementPresent("link=" + qa_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.QA_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.QA_FILTER_NAME_TEXT,
					qa_FilterName);
		}
		
		if (!(qa_Rule.isEmpty())){
			
			if ((!(qa_Rule.equalsIgnoreCase("o"))) & (!(qa_Rule.equalsIgnoreCase("x")))){
				String[] iqa_Rule = qa_Rule.split(",");
				length_of_QA_rule = iqa_Rule.length;
				line_of_source_equal_to_target = length_of_QA_rule + 3;
				target_string_expansion = length_of_QA_rule +4;
				
				
				
				for (int i = 0; i < iqa_Rule.length; i++) {
					selenium.click(FilterConfiguration.QA_FILTER_RULE_ADD_BUTTON);
					String[] i_iqa_Rule = iqa_Rule[i].split(";");
					
					for (int j = 0; j < i_iqa_Rule.length; j++) {
						
						String[] j_iqa_Rule = i_iqa_Rule[j].split(":=");
						switch (j_iqa_Rule[0])
						{
							case "Check":
							{
								selenium.type(FilterConfiguration.QA_FILTER_RULE_CHECK_TEXT, j_iqa_Rule[1]);
								break;
							}
							case "Check_Is_RegEx":
							{
								if (j_iqa_Rule[1].equalsIgnoreCase("yes"))
									selenium.check(FilterConfiguration.QA_FILTER_RULE_CHECK_IS_REGEX_CHECKBOX);
								break;
							}
							case "Description":
							{
								selenium.type(FilterConfiguration.QA_FILTER_RULE_DESCRIPTION_TEXT, j_iqa_Rule[1]);
								break;
							}
							
							case "Priority":
							{
								selenium.type(FilterConfiguration.QA_FILTER_RULE_PRIORITY_TEXT, j_iqa_Rule[1]);
								break;
							}	
							
							case "Exceptions":
							{
								selenium.click(FilterConfiguration.QA_FILTER_RULE_ADD_EXCEPTION_BUTTON);
								String[] j_iqa_Rule_Exceptions = j_iqa_Rule[1].split(":_:");
								for (int k = 0; k < j_iqa_Rule_Exceptions.length; k++) {
									String[] k_j_iqa_Rule_Exceptions = j_iqa_Rule_Exceptions[k].split(":::");
									selenium.type(FilterConfiguration.QA_FILTER_RULE_EXCEPTION_EXCEPTION_TEXT, k_j_iqa_Rule_Exceptions[0]);
									if (k_j_iqa_Rule_Exceptions[1].equalsIgnoreCase("yes"))
										selenium.check(FilterConfiguration.QA_FILTER_RULE_EXCEPTION_EXCEPTION_IS_REGEX_CHECKBOX);
									selenium.select(FilterConfiguration.QA_FILTER_RULE_EXCEPTION_EXCEPTION_LANGUAGE_SELECT, k_j_iqa_Rule_Exceptions[2]);
									selenium.click(FilterConfiguration.QA_FILTER_RULE_EXCEPTION_EXCEPTION_SAVE_BUTTON);
									
								}
								
							}
							
							default:
								break;
						}
						
					}
					
					 selenium.click(FilterConfiguration.QA_FILTER_RULE_SAVE_BUTTON);
				}
				selenium.click(FilterConfiguration.QA_FILTER_RULE_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.QA_FILTER_SOURCE_EQUAL_TO_TARGET_STRING_EXPANSION_OF_CHECKBOX + line_of_source_equal_to_target + "]/td/input");
				selenium.click(FilterConfiguration.QA_FILTER_SOURCE_EQUAL_TO_TARGET_STRING_EXPANSION_OF_CHECKBOX + target_string_expansion + "]/td/input");
			}
			
		}
		
		
		if ((!(source_equal_to_Target.isEmpty())) & (!(source_equal_to_Target.equalsIgnoreCase("x"))))
			selenium.click(FilterConfiguration.QA_FILTER_SOURCE_EQUAL_TO_TARGET_STRING_EXPANSION_OF_CHECKBOX + line_of_source_equal_to_target + "]/td/input");
		
		if ((!(target_string_expansion_of_or_more.isEmpty())) & (!(target_string_expansion_of_or_more.equalsIgnoreCase("x"))))
			selenium.click(FilterConfiguration.QA_FILTER_SOURCE_EQUAL_TO_TARGET_STRING_EXPANSION_OF_CHECKBOX + target_string_expansion + "]/td/input");
		
			
		selenium.click(FilterConfiguration.QA_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
      
        
   }
    
    public void newInddFilter(Selenium selenium, String iFilterName, boolean sTrnHidden, boolean sTrnMaster,
    		boolean sFileInfo, boolean sIgForceLine, boolean sIgNonBreak) throws Exception {
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.INDD_FILTER_ADD_BUTTON);
		
		selenium.type(FilterConfiguration.INDD_FILTER_NAME_TEXT,
				iFilterName);
		
		// config "Translate Hidden Layers"
		if (sTrnHidden){
			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX);
		
		// config Translate Master Layers
		if (sTrnMaster){
			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX);
		
		// config Translate File Information
		if (sFileInfo){
			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX);
		
		// config Ignore Forced Line Breaks
		if (sIgForceLine){
			selenium.check(FilterConfiguration.INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX);
		
		// config Ignore Nonbreaking Space
		if (sIgNonBreak){
			selenium.check(FilterConfiguration.INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX);
		
		selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON);
    }
	
    public void editInddFilter(Selenium selenium, String iFilterName, boolean sTrnHidden, boolean sTrnMaster,
    		boolean sFileInfo, boolean sIgForceLine, boolean sIgNonBreak) throws Exception {
		selenium.click(FilterConfiguration.COLLAPSE_ALL_BUTTON);
		selenium.click(FilterConfiguration.INDD_FILTER_ADD_BUTTON);
		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        selenium.click("link="+iFilterName);
				
		// config "Translate Hidden Layers"
		if (sTrnHidden){
			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX);
		
		// config Translate Master Layers
		if (sTrnMaster){
			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX);
		
		// config Translate File Information
		if (sFileInfo){
			selenium.check(FilterConfiguration.INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX);
		
		// config Ignore Forced Line Breaks
		if (sIgForceLine){
			selenium.check(FilterConfiguration.INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX);
		
		// config Ignore Nonbreaking Space
		if (sIgNonBreak){
			selenium.check(FilterConfiguration.INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX);
		
		selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON);
    }

    public void propertiesFilter(Selenium selenium, String properties_FilterName, String enable_SID_Support,
            String enable_Unicode_Escape, String preserve_Trailing_Spaces, String content_post_filter,
            String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping
            )
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		

		if (selenium.isElementPresent("link=" + properties_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.JAVA_PROPERTIES_FILTER_NAME_TEXT,
					properties_FilterName);
		}
		if ((!(enable_SID_Support.isEmpty())) && (!(enable_SID_Support.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.JAVA_PROPERTIES_FILTER_SID_SUPPORT_CHECKBOX);
		
		if ((!(enable_Unicode_Escape.isEmpty())) && (!(enable_Unicode_Escape.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.JAVA_PROPERTIES_FILTER_UNICODE_ESCAPE_CHECKBOX);
		
		if ((!(preserve_Trailing_Spaces.isEmpty())) && (!(preserve_Trailing_Spaces.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.JAVA_PROPERTIES_FILTER_PRESERVE_TRAILING_SPACE_CHECKBOX);
		
		if ((!(content_post_filter.isEmpty())) && (!(content_post_filter.equalsIgnoreCase("x")))){
			
			if  (content_post_filter.equalsIgnoreCase("o")){
				selenium.select(FilterConfiguration.JAVA_PROPERTIES_FILTER_SECONDARY_FILTER_SELECT, 
						FilterConfiguration.JAVA_PROPERTIES_FILTER_SECONDARY_FILTER_DEFAULT_FILTER_LABEL);
			} else selenium.select(FilterConfiguration.JAVA_PROPERTIES_FILTER_SECONDARY_FILTER_SELECT, content_post_filter);
			
		}
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.JAVA_PROPERTIES_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		


		
		
		selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        if (selenium.isTextPresent(FilterConfiguration.OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE))
        	selenium.close();
        
   }
    
    public void propertiesFilter(Selenium selenium, String iFilterName, boolean bSID, boolean bUnicodeEsp,
    		boolean bPresvTrailSpace, String iSecFilter, String iIntlTextFilter) throws Exception {
    	
    	selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
    	
        selenium.click("link="+iFilterName);
		
		//config options
		if (bSID)
		{
			selenium.check(FilterConfiguration.JAVA_PROPERTIES_FILTER_SID_SUPPORT_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.JAVA_PROPERTIES_FILTER_SID_SUPPORT_CHECKBOX);
		
		if (bUnicodeEsp)
		{
			selenium.check(FilterConfiguration.JAVA_PROPERTIES_FILTER_UNICODE_ESCAPE_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.JAVA_PROPERTIES_FILTER_UNICODE_ESCAPE_CHECKBOX);
		
		if (bPresvTrailSpace)
		{
			selenium.check(FilterConfiguration.JAVA_PROPERTIES_FILTER_PRESERVE_TRAILING_SPACE_CHECKBOX);
		}
		else selenium.uncheck(FilterConfiguration.JAVA_PROPERTIES_FILTER_PRESERVE_TRAILING_SPACE_CHECKBOX);
		
		//config secondary filter
		selenium.select("secondaryFilterSelect", "label=" + iSecFilter);
		
		//config internal text filter
		selenium.select("java_properties_filter_baseFilterSelect", "label=" + iIntlTextFilter);
		
		selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SAVE_BUTTON);
		if (selenium.isAlertPresent())
			selenium.getAlert();
    }
	
    public void javascriptFitler(Selenium selenium, String iFilterName, String JSFunctionText, boolean bEnableUnicodeEsp) throws Exception 
    {
    	selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
    	
        selenium.click("link="+iFilterName);
        
        selenium.type(FilterConfiguration.JAVASCRIPT_FILTER_FUNCTION_TEXT, JSFunctionText);
        
        if (bEnableUnicodeEsp)
        {
        	selenium.check(FilterConfiguration.JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX);
        }
        else selenium.uncheck(FilterConfiguration.JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX);
    }
    public void javascriptFitler(Selenium selenium, String js_FilterName, String js_FunctionText, String enable_Unicode_Escape, 
    		String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping) throws Exception 
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		

		if (selenium.isElementPresent("link=" + js_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.JAVASCRIPT_FILTER_NAME_TEXT,
					js_FilterName);
		}
		if (!js_FunctionText.isEmpty()) 
			selenium.type(FilterConfiguration.JAVASCRIPT_FILTER_FUNCTION_TEXT, js_FunctionText);
		if ((!(enable_Unicode_Escape.isEmpty())) && (!(enable_Unicode_Escape.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX);
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.JAVASCRIPT_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
        
   }
    		
    public void jspFitler(Selenium selenium, String js_FilterName, String add_Additional_Head, String enable_Entity_Escape, 
    		String base_Text_post_filter, String base_Text_Filter_Internal_Text, String base_Text_Filter_Escaping) throws Exception 
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        if ((!(base_Text_post_filter.isEmpty()))&&(!(base_Text_post_filter.equalsIgnoreCase("x")))){
			if (selenium.isElementPresent("link=" + base_Text_post_filter)){ 
				
			} else {
				selenium.click(FilterConfiguration.BASE_FILTER_ADD_BUTTON);
				selenium.type(
						FilterConfiguration.BASE_FILTER_NAME_TEXT,
						base_Text_post_filter);
				if((!(base_Text_Filter_Internal_Text.isEmpty()))&&(!(base_Text_Filter_Internal_Text.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Internal_Text = base_Text_Filter_Internal_Text.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_INTERNAL_TEXT);
					for (int i = 0; i < ibase_Text_Filter_Internal_Text.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_CONTENT_NAME_TEXT, ibase_Text_Filter_Internal_Text[i]);
						selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY_TEXT, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_CONTENT_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				
				if((!(base_Text_Filter_Escaping.isEmpty()))&&(!(base_Text_Filter_Escaping.equalsIgnoreCase("x")))){
					String[] ibase_Text_Filter_Escaping = base_Text_Filter_Escaping.split(",");
					
					selenium.select(FilterConfiguration.BASE_FILTER_SELECT, FilterConfiguration.BASE_FILTER_ESCAPING);
					for (int i = 0; i < ibase_Text_Filter_Escaping.length; i++) {
						selenium.click(FilterConfiguration.BASE_FILTER_ADD_CONTENT_BUTTON);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT, ibase_Text_Filter_Escaping[i]);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_IMPORT_CHECKBOX);
						selenium.check(FilterConfiguration.BASE_FILTER_ESCAPING_EXPORT_CHECKBOX);
						selenium.type(FilterConfiguration.BASE_FILTER_ESCAPING_PRIORITY, Integer.toString(i+1));
						selenium.click(FilterConfiguration.BASE_FILTER_ESCAPING_SAVE_BUTTON);
					}
					selenium.click(FilterConfiguration.BASE_FILTER_CHECK_ALL_CHECKBOX);

				}
				selenium.click(FilterConfiguration.BASE_FILTER_SAVE_BUTTON);
			}
			
		}
		

		if (selenium.isElementPresent("link=" + js_FilterName)){
			return;
		} else {
			selenium.click(FilterConfiguration.JSP_FILTER_ADD_BUTTON);
			selenium.type(
					FilterConfiguration.JSP_FILTER_NAME_TEXT,
					js_FilterName);
		}
		if ((!(add_Additional_Head.isEmpty())) && (!(add_Additional_Head.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.JSP_FILTER_ADD_ADDITIONAL_HEAD_CHECKBOX);
		if ((!(enable_Entity_Escape.isEmpty())) && (!(enable_Entity_Escape.equalsIgnoreCase("x"))))
			selenium.check(FilterConfiguration.JSP_FILTER_ESCAPE_ENTITY_CHECKBOX);
		
		if ((!(base_Text_post_filter.isEmpty())) && (!(base_Text_post_filter.equalsIgnoreCase("x")))&& (!(base_Text_post_filter.equalsIgnoreCase("o"))))
			selenium.select(FilterConfiguration.JSP_FILTER_TEXT_POST_FILTER_SELECT, base_Text_post_filter);
		
		selenium.click(FilterConfiguration.JSP_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
        
   }
    	
    
    /* remove filters.
	 */
	public void removeFilters(Selenium selenium, String Filters) {
		String[] iFilters = Filters.split(",");
		boolean iRemove = false;
		selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);

		int i = 1;
		while (selenium
				.isElementPresent(FilterConfiguration.FILTER_TABLE
						+ "/tr[" + i + "]")) {

			if (selenium
					.isElementPresent(FilterConfiguration.FILTER_TABLE
							+ "/tr[" + i + "]//a")) {
				if (selenium
						.isElementPresent(FilterConfiguration.FILTER_TABLE
								+ "/tr[" + i + "]/td[2]/div/table/tbody/tr")) {

					int j = 1;
					while (selenium
							.isElementPresent(FilterConfiguration.FILTER_TABLE
									+ "/tr["
									+ i
									+ "]/td[2]/div/table/tbody/tr[" + j + "]")) {
						for (String iFilter : iFilters) {
							if (selenium
									.getText(
											FilterConfiguration.FILTER_TABLE
													+ "/tr["
													+ i
													+ "]/td[2]/div/table/tbody/tr["
													+ j + "]").equals(iFilter)) {
								selenium.click(FilterConfiguration.FILTER_TABLE
										+ "/tr["
										+ i
										+ "]/td[2]/div/table/tbody/tr["
										+ j
										+ "]//input");
								iRemove = true;
								break;
							}
						}
						j++;
					}
				}
			}
			i++;
		}

		if (iRemove) {
			selenium.click(FilterConfiguration.REMOVE_VALUE_BUTTON);
			if (selenium.isConfirmationPresent()) {
				selenium.getConfirmation();
			}
			if (selenium.isAlertPresent()) {
				selenium.getAlert();
			}
		} else {
			Reporter.log("The Filters "
					+ iFilters
					+ " all can't be found in the filters! Please verify it first");
		}
	}

	public void editFilters(Selenium selenium, String Filters) throws Exception {

		String[] iFilters = Filters.split(",");
		String iFilterName = null;

		for (String iFilter : iFilters) {
			try {
				iFilterName = iFilter + "1";

				if (iFilter.equals("html")) {
					selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click("link=" + iFilterName);
						selenium.click(FilterConfiguration.HTML_FILTER_A_CHECKBOX);
						selenium.click(FilterConfiguration.HTML_FILTER_SAVE_BUTTON);
					} else {
						Reporter.log("The html filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("indesign")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click("link=" + iFilterName);
						selenium.click(FilterConfiguration.INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX);
						selenium.click(FilterConfiguration.INDD_FILTER_SAVE_BUTTON);
					} else {
						Reporter.log("The indd/idml filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("javaproperties")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click("link=" + iFilterName);
						selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SID_SUPPORT_CHECKBOX);
						selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_UNICODE_ESCAPE_CHECKBOX);
						selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_PRESERVE_TRAILING_SPACE_CHECKBOX);
						selenium.click(FilterConfiguration.JAVA_PROPERTIES_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The javaproperties filter " + iFilterName
								+ " is modified!");
					} else {
						Reporter.log("The javaproperties filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("javascript")) {

					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click(("link=" + iFilterName));
						selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX);
						selenium.click(FilterConfiguration.JAVASCRIPT_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The java script filter " + iFilterName
								+ " is modified!");
					} else {
						Reporter.log("The java script filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("jsp")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click(("linnk=" + iFilterName));
						selenium.click(FilterConfiguration.JSP_FILTER_ADD_ADDITIONAL_HEAD_CHECKBOX);
						selenium.click(FilterConfiguration.JSP_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The jsp filter " + iFilterName
								+ " has been modified!");
					} else {
						Reporter.log("The jsp filter " + iFilterName
								+ " doesn't exist1");
					}
				}

				else if (iFilter.equals("msoffice2010")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX);
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX);
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_STYLE_ADD_BUTTON);
						selenium.type(FilterConfiguration.OFFICE_2010_FILTER_STYLE_NAME_TEXT,
								"AddSyles");
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_STYLE_SAVE_BUTTON);
						selenium.click(FilterConfiguration.OFFICE_2010_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The msoffice2010 filter " + iFilterName
								+ " has been modified!");
					} else {
						Reporter.log("The msoffice2010 filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("msofficedoc")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click((FilterConfiguration.OFFICE_WORD_FILTER_HEADER_CHECKBOX));
						selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The msofficedoc filter " + iFilterName
								+ " has been modified!");
					} else {
						Reporter.log("The msofficedoc filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("msofficeexcel")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click(FilterConfiguration.OFFICE_EXCEL_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The msofficeexcel filter " + iFilterName
								+ " has been modified!");
					} else {
						Reporter.log("The msofficeexcel filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("msofficepowerpoint")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_EXTRACT_ALT_CHECKBOX);
						selenium.click(FilterConfiguration.OFFICE_POWERPOINT_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The msofficepowerpoint filter "
								+ iFilterName + " has been modified!");
					} else {
						Reporter.log("The msofficepowerpoint filter "
								+ iFilterName + " doesn't exist!");
					}
				}

				else if (iFilter.equals("openoffice")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click((FilterConfiguration.OPENOFFICE_FILTER_HEADER_INFO_CHECKBOX));
						selenium.click(FilterConfiguration.OPENOFFICE_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The openoffice filter " + iFilterName
								+ " has been modified!");
					} else {
						Reporter.log("The openoffice filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("portableobject")) {
					String secondaryFilter = "HTML_Filter(Defualt)";
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.select(
								FilterConfiguration.PO_FILTER_SECONDARY_FILTER_SELECT,
								"label=" + secondaryFilter);
						selenium.click(FilterConfiguration.PO_FILTER_SAVE_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
						Reporter.log("The portableobject filter " + iFilterName
								+ " has been modified!");
					} else {
						Reporter.log("The portableobject filter " + iFilterName
								+ " doesn't exist!");
					}
				}

			} catch (Exception e) {
				Reporter.log(e.getMessage());
			}
		}
	}

}
