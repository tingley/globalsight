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
			selenium.type(FilterConfiguration.BASE_FILTER_PRIORITY, tag[2]);
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
		selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_CHECKBOX, "label="
				+ conName);
		selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_CHECKBOX,
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
						FilterConfiguration.HTML_FILTER_TAG_EMBEDDABLE);
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, 
						FilterConfiguration.HTML_FILTER_TAG_EMBEDDABLE);
				selenium.check(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}

			if (internalTags.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT, 
						FilterConfiguration.HTML_FILTER_TAG_INTERNAL);
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			else {
			selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
					FilterConfiguration.HTML_FILTER_TAG_INTERNAL);
			selenium.check(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
			
			if (translatableAttibute.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
						FilterConfiguration.HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE);
				selenium.uncheck(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			} else {
				selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
						FilterConfiguration.HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE);
				selenium.check(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
				selenium.click(FilterConfiguration.HTML_FILTER_CHECK_ALL_CHECKBOX);
			}
            if (embeddableTags.equalsIgnoreCase("x")
                    && internalTags.equalsIgnoreCase("x")
                    && translatableAttibute.equalsIgnoreCase("x"))
            {
                selenium.select(FilterConfiguration.HTML_FILTER_TAG_TYPE_SELECT,
                		FilterConfiguration.HTML_FILTER_TAG_PAIRED);
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
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_CHECKBOX,
							"label=" + htmlFilterName);
				else
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_CHECKBOX,
							"label=Choose");
				if (internalTextPostFilter.equalsIgnoreCase("on"))
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_CHECKBOX,
							"label=" + interName1);
				else
					selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_CHECKBOX,
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

    public void wordFilterOperation(Selenium selenium, String filterName, String headerInfo,
            String toolTips, String tableofContent, String contentPostFilter,
            String internalTextPostFilter, String unextractableWordParagraphStyles, 
            String unextractableWordCharacterStyles, String selectedInternalTextStyles)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        selenium.click("link=" + filterName);
        if (headerInfo.equalsIgnoreCase("o"))
            selenium.check(FilterConfiguration.OFFICE_WORD_FILTER_HEADER_INFO_CHECKBOX);
        else selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_HEADER_INFO_CHECKBOX);
        if (toolTips.equalsIgnoreCase("o"))
            selenium.check(FilterConfiguration.OFFICE_WORD_FILTER_TOOLTIPS_CHECKBOX);
        else selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_TOOLTIPS_CHECKBOX);
        if (tableofContent.equalsIgnoreCase("o"))
            selenium.check(FilterConfiguration.OFFICE_WORD_FILTER_TOC_TRANSLATE_CHECKBOX);
        else selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_TOC_TRANSLATE_CHECKBOX);
        if (contentPostFilter.isEmpty())
        	selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_CONTENT_POST_FILTER_SELECT, "Choose");
        else selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_CONTENT_POST_FILTER_SELECT, contentPostFilter);
        if (internalTextPostFilter.isEmpty())
        	selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_TEXT_POST_FILTER_SELECT, "Choose");
        else selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_TEXT_POST_FILTER_SELECT, internalTextPostFilter);
        if (unextractableWordParagraphStyles.isEmpty())
        {
            selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_PARAGRAPH);
            selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
            
        }
        else 
        {
         	selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_PARAGRAPH);
            selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
            
           	String[] iStyles = unextractableWordParagraphStyles.split(",");
    			for(int i=0; i<iStyles.length; i++)
    		        {
    					selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_ADD_BUTTON);
    		            selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_NAME_TEXT, iStyles[i]);
    		            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON);
    		        }

            
         }
        if (unextractableWordCharacterStyles.isEmpty())
        {
	        selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_CHARACTER);
	        selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
        }
	    else 
	    {
	     	selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_CHARACTER);
	        selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
	        
	       	String[] iStyles = unextractableWordCharacterStyles.split(",");
				for(int i=0; i<iStyles.length; i++)
			        {
						selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_ADD_BUTTON);
			            selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_NAME_TEXT, iStyles[i]);
			            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON);
			        }
	
	        
         }
        if (selectedInternalTextStyles.isEmpty())
        {
	        selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_INTERNAL_TEXT);
	        selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
        
        }
	    else 
	    {
	     	selenium.select(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SELECT, FilterConfiguration.OFFICE_WORD_FILTER_STYLE_INTERNAL_TEXT);
	        selenium.uncheck(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON);
	        selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn);
	        
	       	String[] iStyles = selectedInternalTextStyles.split(",");
				for(int i=0; i<iStyles.length; i++)
			        {
						selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_ADD_BUTTON);
			            selenium.type(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_NAME_TEXT, iStyles[i]);
			            selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON);
			        }
	
	        
	    }
       selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
   }
    
    public void excelFilterOperation(Selenium selenium, String filterName, 
            String toolTips, String contentPostFilter,
            String internalTextPostFilter)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        selenium.click("link=" + filterName);
        if (toolTips.equalsIgnoreCase("o"))
            selenium.check(FilterConfiguration.OFFICE_EXCEL_FILTER_TOOLTIPS_CHECKBOX);
        else selenium.uncheck(FilterConfiguration.OFFICE_EXCEL_FILTER_TOOLTIPS_CHECKBOX);
        if (contentPostFilter.isEmpty())
        	selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_CHECKBOX, "Choose");
        else selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_CHECKBOX, contentPostFilter);
        if (internalTextPostFilter.isEmpty())
        	selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_CHECKBOX, "Choose");
        else selenium.select(FilterConfiguration.OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_CHECKBOX, internalTextPostFilter);

       selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
   }
    public void powerpointFilterOperation(Selenium selenium, String filterName, 
            String toolTips, String contentPostFilter,
            String internalTextPostFilter)
    {
        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        selenium.click("link=" + filterName);
        if (toolTips.equalsIgnoreCase("o"))
            selenium.check(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TOOLTIPS_CHECKBOX);
        else selenium.uncheck(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TOOLTIPS_CHECKBOX);
        if (contentPostFilter.isEmpty())
        	selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT, "Choose");
        else selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT, contentPostFilter);
        if (internalTextPostFilter.isEmpty())
        	selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TEXT_SELECT, "Choose");
        else selenium.select(FilterConfiguration.OFFICE_POWERPOINT_FILTER_TEXT_SELECT, internalTextPostFilter);

       selenium.click(FilterConfiguration.OFFICE_WORD_FILTER_SAVE_BUTTON); 
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
