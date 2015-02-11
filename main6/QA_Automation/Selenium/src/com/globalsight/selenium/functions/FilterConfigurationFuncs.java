package com.globalsight.selenium.functions;

/*
 * FileName: FilterConfigurationFuncs.java
 * Author:Jester
 * Methods: newFilters();removeFilters() 
 * 
 */
import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FilterConfiguration;
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
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.HtmlFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The html filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the html filter.
						selenium.click(FilterConfiguration.HtmlFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_HtmlFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Basefont_CHECKBOX);
						selenium.click(FilterConfiguration.Save_HtmlFilter_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.HtmlFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("indesign")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.InDesignIDMLFilter_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The indesign filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the indesign/IDML Filter
						selenium.click(FilterConfiguration.InDesignIDMLFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_InDesignIDMLFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_InDesignIDMLFilter_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.InDesignIDMLFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("internaltext")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.InternalTextFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The internaltext filter " + iFilterName
								+ " has already exists!");
					} else {
						selenium.click(FilterConfiguration.InternalTextFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_Internaltext_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_Internaltext_BUTTON);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("javaproperties")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.JavaPropertiesFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The javaproperties filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the Java Properties Filter
						selenium.click(FilterConfiguration.JavaPropertiesFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_JavaPropertiesFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_JavaPropertiesFilter_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.JavaPropertiesFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("javascript")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.JavaScriptFilter_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The java script filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the java script Filter
						selenium.click(FilterConfiguration.JavaScriptFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_JavaScriptFilter_TEXT_FIELD,
								iFilterName);
						selenium.type(
								FilterConfiguration.JSFunction_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_JavaScriptFilter_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.JavaScriptFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("jsp")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.JspFilter_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The jsp filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the jsp Filter
						selenium.click(FilterConfiguration.JspFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_JspFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_JspFilter_BUTTON);

						// Check it
						// selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
						// selenium.click(FilterConfiguration.JspFilter_IMG);
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msoffice2010")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.MS2010Filter_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msoffice2010 filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the ms office 2010 Filter
						selenium.click(FilterConfiguration.MS2010Filter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_MS2010Filter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_MS2010Filter_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msofficedoc")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.MSDocFilter_IMG);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msofficedoc filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the ms office doc Filter
						selenium.click(FilterConfiguration.MSDocFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_MSDocFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_MSDocFilter_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msofficeexcel")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.MSExcelFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msofficeexcel filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the ms office excel Filter
						selenium.click(FilterConfiguration.MSExcelFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_MSExcelFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_MSExcelFilter_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("msofficepowerpoint")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.MSPowerPointFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The msofficepowerpoint filter "
								+ iFilterName + " has already exists!");
					} else {
						// Create the ms office power point Filter
						selenium.click(FilterConfiguration.MSPowerPointFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_MSPowerPointFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_MSPowerPointFilter_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}

				} else if (iFilter.equals("openoffice")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.OpenOfficeFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The openoffice filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the open office Filter
						selenium.click(FilterConfiguration.OpenOfficeFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_OpenOfficeFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_OpenOfficeFilter_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}
				} else if (iFilter.equals("portableobject")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.PortableObjectFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The portableobject filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the portable object Filter
						selenium.click(FilterConfiguration.PortableObjectFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_PortableObjectFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_PortableObjectFilter_BUTTON);

						// Check it
						Assert.assertEquals(selenium.isElementPresent("link="
								+ iFilterName), true);
					}

				} else if (iFilter.equals("xml")) {
					selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
					selenium.click(FilterConfiguration.XmlFilter_IMG);
					if (selenium.isElementPresent("link=" + iFilterName)) {
						Reporter.log("The xml filter " + iFilterName
								+ " has already exists!");
					} else {
						// Create the xml Filter
						selenium.click(FilterConfiguration.XmlFilter_BUTTON);
						selenium.type(
								FilterConfiguration.FilterName_XmlFilter_TEXT_FIELD,
								iFilterName);
						selenium.click(FilterConfiguration.Save_XmlFilter_BUTTON);

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
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.HtmlFilter_IMG);

				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The html filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the html filter.
					selenium.click(FilterConfiguration.HtmlFilter_BUTTON);
					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.FilterName_HtmlFilter_TEXT_FIELD,
									iFieldValue);
						}
					}

					selenium.click(FilterConfiguration.Basefont_CHECKBOX);
					selenium.click(FilterConfiguration.Save_HtmlFilter_BUTTON);

				}
			} else if (iFilter.equals("indesign")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.InDesignIDMLFilter_IMG);

				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The indesign filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the indesign/IDML Filter
					selenium.click(FilterConfiguration.InDesignIDMLFilter_BUTTON);
					selenium.type(
							FilterConfiguration.FilterName_InDesignIDMLFilter_TEXT_FIELD,
							iFilterName);
					selenium.click(FilterConfiguration.Save_InDesignIDMLFilter_BUTTON);

				}
			} else if (iFilter.equals("javaproperties")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.JavaPropertiesFilter_IMG);
				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The javaproperties filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the Java Properties Filter
					selenium.click(FilterConfiguration.JavaPropertiesFilter_BUTTON);
					selenium.type(
							FilterConfiguration.FilterName_JavaPropertiesFilter_TEXT_FIELD,
							iFilterName);
					selenium.click(FilterConfiguration.Save_JavaPropertiesFilter_BUTTON);
				}
			} else if (iFilter.equals("javascript")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.JavaScriptFilter_IMG);

				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The java script filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the java script Filter
					selenium.click(FilterConfiguration.JavaScriptFilter_BUTTON);
					selenium.type(
							FilterConfiguration.FilterName_JavaScriptFilter_TEXT_FIELD,
							iFilterName);
					selenium.type(FilterConfiguration.JSFunction_TEXT_FIELD,
							iFilterName);
					selenium.click(FilterConfiguration.Save_JavaScriptFilter_BUTTON);
				}
			} else if (iFilter.equals("jsp")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.JspFilter_IMG);

				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The jsp filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the jsp Filter
					selenium.click(FilterConfiguration.JspFilter_BUTTON);
					selenium.type(
							FilterConfiguration.FilterName_JspFilter_TEXT_FIELD,
							iFilterName);
					selenium.click(FilterConfiguration.Save_JspFilter_BUTTON);
				}
			} else if (iFilter.equals("msoffice2010")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.MS2010Filter_IMG);

				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msoffice2010 filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the ms office 2010 Filter
					selenium.click(FilterConfiguration.MS2010Filter_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.FilterName_MS2010Filter_TEXT_FIELD,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("o2010FilterDesc", iFieldValue);
						} else if (iFieldName.equals("headerTranslate")) {
							selenium.check("headerTranslate");
						} else if (iFieldName.equals("masterTranslate")) {
							selenium.check("masterTranslate");
						}
					}

					selenium.click(FilterConfiguration.Save_MS2010Filter_BUTTON);
				}
			} else if (iFilter.equals("msofficedoc")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.MSDocFilter_IMG);

				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msofficedoc filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the ms office doc Filter
					selenium.click(FilterConfiguration.MSDocFilter_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.FilterName_MSDocFilter_TEXT_FIELD,
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

					selenium.click(FilterConfiguration.Save_MSDocFilter_BUTTON);
				}
			} else if (iFilter.equals("msofficeexcel")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.MSExcelFilter_IMG);
				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msofficeexcel filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the ms office excel Filter
					selenium.click(FilterConfiguration.MSExcelFilter_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.FilterName_MSExcelFilter_TEXT_FIELD,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("excelDesc", iFieldValue);
						} else if (iFieldName.equals("seconderyFilter")) {
							selenium.select("excelContentPostFilterSelect",
									"label=" + iFieldValue);
						}
					}

					selenium.click(FilterConfiguration.Save_MSExcelFilter_BUTTON);
				}
			} else if (iFilter.equals("msofficepowerpoint")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.MSPowerPointFilter_IMG);
				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The msofficepowerpoint filter "
							+ iFieldValueTemp + " has already exists!");
				} else {
					// Create the ms office power point Filter
					selenium.click(FilterConfiguration.MSPowerPointFilter_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.FilterName_MSPowerPointFilter_TEXT_FIELD,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("pptFilterDes", iFieldValue);
						} else if (iFieldName.equals("seconderyFilter")) {
							selenium.select("pptContentPostFilterSelect",
									"label=" + iFieldValue);
						}
					}

					selenium.click(FilterConfiguration.Save_MSPowerPointFilter_BUTTON);
				}

			} else if (iFilter.equals("openoffice")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.OpenOfficeFilter_IMG);
				if (selenium.isElementPresent("link=" + iFieldValueTemp)) {
					Reporter.log("The openoffice filter " + iFieldValueTemp
							+ " has already exists!");
				} else {
					// Create the open office Filter
					selenium.click(FilterConfiguration.OpenOfficeFilter_BUTTON);

					for (String tempStr : array) {
						String[] ivalue = tempStr.split("=");
						String iFieldName = ivalue[0].trim();
						String iFieldValue = ivalue[1].trim();

						if (iFieldName.equals("name")) {
							selenium.type(
									FilterConfiguration.FilterName_OpenOfficeFilter_TEXT_FIELD,
									iFieldValue);
						} else if (iFieldName.equals("description")) {
							selenium.type("ooFilterDesc", iFieldValue);
						} else if (iFieldName.equals("headerTranslate")) {
							selenium.check("headerTranslate");
						}
					}

					selenium.click(FilterConfiguration.Save_OpenOfficeFilter_BUTTON);
				}
			} else if (iFilter.equals("portableobject")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.PortableObjectFilter_IMG);
				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The portableobject filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the portable object Filter
					selenium.click(FilterConfiguration.PortableObjectFilter_BUTTON);
					selenium.type(
							FilterConfiguration.FilterName_PortableObjectFilter_TEXT_FIELD,
							iFilterName);
					selenium.click(FilterConfiguration.Save_PortableObjectFilter_BUTTON);
				}

			} else if (iFilter.equals("xml")) {
				selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
				selenium.click(FilterConfiguration.XmlFilter_IMG);
				if (selenium.isElementPresent("link=" + iFilterName)) {
					Reporter.log("The xml filter " + iFilterName
							+ " has already exists!");
				} else {
					// Create the xml Filter
					selenium.click(FilterConfiguration.XmlFilter_BUTTON);
					selenium.type(
							FilterConfiguration.FilterName_XmlFilter_TEXT_FIELD,
							iFilterName);
					selenium.click(FilterConfiguration.Save_XmlFilter_BUTTON);
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

		selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
		selenium.click(FilterConfiguration.HtmlFilter_BUTTON);

		selenium.type(FilterConfiguration.FilterName_HtmlFilter_TEXT_FIELD,
				iFilterName);
		htmlPostFilterChoose(selenium, cpf);

		String[] group = str.split(";");
		for (int i = 0; i < group.length; i++) {
			String[] array = group[i].split("\\|\\|");
			selenium.select(FilterConfiguration.Html_Choosing_Box, array[0]);
			selenium.uncheck(FilterConfiguration.Html_CheckAll);
			selenium.click(FilterConfiguration.Html_CheckAll);
		}
		selenium.click(FilterConfiguration.Html_Tags_Delete_BUTTON);

		if (selenium.isAlertPresent()) {
			selenium.getAlert();
		} else
			selenium.click(FilterConfiguration.Html_Delete_Save_BUTTON);
		for (int i = 0; i < group.length; i++) {
			String[] array = group[i].split("\\|\\|");
			String[] tagarray = array[1].split(",");
			selenium.select(FilterConfiguration.Html_Choosing_Box, array[0]);
			for (String temp : tagarray) {
				selenium.click(FilterConfiguration.Html_Add_Tag_BUTTON);

				if (array[0].equals("Internal Tag")) {
					selenium.type(
							FilterConfiguration.Html_Add_InternalTag_Field,
							temp);
					selenium.click(FilterConfiguration.Html_Add_InternalTag_Save_BUTTON);
				}

				else {
					selenium.type(FilterConfiguration.Html_Add_Tag_Field, temp);
					selenium.click(FilterConfiguration.Html_Add_Tag_Save_BUTTON);
				}
			}
			selenium.uncheck(FilterConfiguration.Html_CheckAll);
			selenium.click(FilterConfiguration.Html_CheckAll);
		}
		selenium.click(FilterConfiguration.Save_HtmlFilter_BUTTON);
	}

	public void htmlPostFilterChoose(Selenium selenium, String opt) {
		selenium.select(FilterConfiguration.InternalText_Post_Filter, "label="
				+ opt);
	}

	/*
	 * Initiate internal text filter.
	 */
	public void InternalText(Selenium selenium, String str, String interName) {
		selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
		selenium.click(FilterConfiguration.InternalTextFilter_BUTTON);
		selenium.type(FilterConfiguration.FilterName_Internaltext_TEXT_FIELD,
				interName);

		String[] tagarray = str.split(",");
		for (String tempStr : tagarray) {
			String[] tag = tempStr.split("\\|\\|");
			selenium.click(FilterConfiguration.Internaltext_ADD_BUTTON);
			selenium.type(FilterConfiguration.Internaltext_Type_Content, tag[0]);
			if (tag[1].equals("true")) {
				selenium.click(FilterConfiguration.Internaltext_IS_RE);
			}
			selenium.click(FilterConfiguration.Internaltext_Content_Save_BUTTON);
		}
		selenium.click(FilterConfiguration.Internaltext_CheckAll);
		selenium.click(FilterConfiguration.Save_Internaltext_BUTTON);
	}

	    /*
    	 * Initiate excel filter.
    	 */
	public void excelFilter(Selenium selenium, String fname, String conName,
			String interName) {
		selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
		selenium.click(FilterConfiguration.MSExcelFilter_BUTTON);
		selenium.type(FilterConfiguration.FilterName_MSExcelFilter_TEXT_FIELD,
				fname);
		selenium.select(FilterConfiguration.Content_PostFilter_Choose, "label="
				+ conName);
		selenium.select(FilterConfiguration.InternalText_PostFilter_Choose,
				"label=" + interName);
		selenium.click(FilterConfiguration.Save_MSExcelFilter_BUTTON);
		if (selenium.isAlertPresent())
			selenium.getAlert();
	}
	
	public void wordFilter(Selenium selenium, String fname){
	    selenium.click(FilterConfiguration.CollapseAll_CHECKBOX);
	    selenium.click(FilterConfiguration.MSDocFilter_BUTTON);
	    selenium.type(FilterConfiguration.FilterName_MSDocFilter_TEXT_FIELD, fname);
	    selenium.uncheck(FilterConfiguration.MS_Doc_CheckAll);
        selenium.click(FilterConfiguration.MS_Doc_CheckAll);
        selenium.click(FilterConfiguration.MS_Delete_BUTTON);
        selenium.click(FilterConfiguration.MS_Delete_Save_BUTTOn);
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
	    selenium.click(FilterConfiguration.Save_MSDocFilter_BUTTON);
	    
	}

	public void filterOperation(Selenium selenium, String contentPostFilter,
			String embeddableTags, String internalTags,
			String translatableAttibute, String internalTextFilter,
			String internalTextPostFilter, String internalTextPostFilterChoose) {

		selenium.click(FilterConfiguration.ExpnadAll_CHECKBOX);

		//if (contentPostFilter.equalsIgnoreCase("on")) { 
			selenium.click("link=Excel-Filter");

			if (embeddableTags.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.Html_Choosing_Box,
						"Embeddable Tags");
				selenium.uncheck(FilterConfiguration.Html_CheckAll);
				selenium.click(FilterConfiguration.Html_CheckAll);
			} else {
				selenium.select(FilterConfiguration.Html_Choosing_Box,
						"Embeddable Tags");
				selenium.check(FilterConfiguration.Html_CheckAll);
				selenium.click(FilterConfiguration.Html_CheckAll);
			}

			if (internalTags.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.Html_Choosing_Box,
						"Internal Tag");
				selenium.uncheck(FilterConfiguration.Html_CheckAll);
				selenium.click(FilterConfiguration.Html_CheckAll);
			}
			else {
			selenium.select(FilterConfiguration.Html_Choosing_Box,
					"Internal Tag");
			selenium.check(FilterConfiguration.Html_CheckAll);
			selenium.click(FilterConfiguration.Html_CheckAll);
			}
			
			if (translatableAttibute.equalsIgnoreCase("o")) {
				selenium.select(FilterConfiguration.Html_Choosing_Box,
						"Translatable Attribute");
				selenium.uncheck(FilterConfiguration.Html_CheckAll);
				selenium.click(FilterConfiguration.Html_CheckAll);
			} else {
				selenium.select(FilterConfiguration.Html_Choosing_Box,
						"Translatable Attribute");
				selenium.check(FilterConfiguration.Html_CheckAll);
				selenium.click(FilterConfiguration.Html_CheckAll);
			}
            if (embeddableTags.equalsIgnoreCase("x")
                    && internalTags.equalsIgnoreCase("x")
                    && translatableAttibute.equalsIgnoreCase("x"))
            {
                selenium.select(FilterConfiguration.Html_Choosing_Box,
                        "label=Paired Tags");
                selenium.check(FilterConfiguration.a_CHECKBOX);
                selenium.check(FilterConfiguration.tag1_CHECKBOX);
            }

			if (internalTextFilter.equalsIgnoreCase("o"))
				htmlPostFilterChoose(selenium, "html");
			else
				htmlPostFilterChoose(selenium, "Choose");
			
			selenium.click(FilterConfiguration.Save_HtmlFilter_BUTTON);
		//}

		//if (internalTextPostFilter.equalsIgnoreCase("on")) {
			// modify internal text filter
			selenium.click("link=Excel");
			if (internalTextPostFilterChoose.equalsIgnoreCase("o")) {
				selenium.uncheck(FilterConfiguration.Internaltext_CheckAll);// uncheck,click
				selenium.click(FilterConfiguration.Internaltext_CheckAll);
			} else {
				selenium.check(FilterConfiguration.Internaltext_CheckAll);
				selenium.click(FilterConfiguration.Internaltext_CheckAll);
			}
			selenium.click(FilterConfiguration.Save_Internaltext_BUTTON);
		//}

		// operate excel filter
		selenium.click("link=My Excel - 1");
		if (contentPostFilter.equalsIgnoreCase("on"))
			selenium.select(FilterConfiguration.Content_PostFilter_Choose,
					"label=Excel-Filter");
		else
			selenium.select(FilterConfiguration.Content_PostFilter_Choose,
					"label=Choose");
		if (internalTextPostFilter.equalsIgnoreCase("on"))
			selenium.select(FilterConfiguration.InternalText_PostFilter_Choose,
					"label=Excel");
		else
			selenium.select(FilterConfiguration.InternalText_PostFilter_Choose,
					"label=Choose");
		selenium.click(FilterConfiguration.Save_MSExcelFilter_BUTTON);
	}

    public void wordFilterOperation(Selenium selenium, String headerInfo,
            String toolTips, String tableofContent, String contentPostFilter,
            String internalTextPostFilter, String myheading2,
            String myheading3, String dontTrans)
    {
        selenium.click(FilterConfiguration.ExpnadAll_CHECKBOX);
        selenium.click("link=Doc 1");
        if (headerInfo.equalsIgnoreCase("o"))
            selenium.check("docHeaderTranslate");
        else selenium.uncheck("docHeaderTranslate");
        if (toolTips.equalsIgnoreCase("o"))
            selenium.check("docAltTranslate");
        else selenium.uncheck("docAltTranslate");
        if (tableofContent.equalsIgnoreCase("o"))
            selenium.check("TOCTranslate");
        else selenium.uncheck("TOCTranslate");
        if (contentPostFilter.equalsIgnoreCase("o"))
            selenium.select("docContentPostFilterSelect", "label=2114html");
        else selenium.select("docContentPostFilterSelect", "label=Choose");
        if (internalTextPostFilter.equalsIgnoreCase("o"))
            selenium.select("ms_office_doc_filter_baseFilterSelect", "label=2114");
        else selenium.select("ms_office_doc_filter_baseFilterSelect", "label=Choose");
        if (myheading2.equalsIgnoreCase("o"))
            {
            selenium.uncheck(FilterConfiguration.MS_Doc_CheckAll);
            selenium.click(FilterConfiguration.MS_Doc_CheckAll);
            selenium.click(FilterConfiguration.MS_Delete_BUTTON);
            selenium.click(FilterConfiguration.MS_Delete_Save_BUTTOn);
            
            selenium.click(FilterConfiguration.Style_Add_BUTTON);
            selenium.type(FilterConfiguration.Style_Add, "MyHeading2");
            selenium.click(FilterConfiguration.Style_Save_BUTTON);
            }
        else {
            selenium.uncheck(FilterConfiguration.MS_Doc_CheckAll);
            selenium.click(FilterConfiguration.MS_Doc_CheckAll);
            selenium.click(FilterConfiguration.MS_Delete_BUTTON);
            selenium.click(FilterConfiguration.MS_Delete_Save_BUTTOn);
                }
        if (myheading3.equalsIgnoreCase("o"))
        {
            selenium.click(FilterConfiguration.Style_Add_BUTTON);
            selenium.type(FilterConfiguration.Style_Add, "MyHeading3");
            selenium.click(FilterConfiguration.Style_Save_BUTTON);
        }
        if (dontTrans.equalsIgnoreCase("o"))
        {
            selenium.click(FilterConfiguration.Style_Add_BUTTON);
            selenium.type(FilterConfiguration.Style_Add, "DoNotTrans");
            selenium.click(FilterConfiguration.Style_Save_BUTTON);
        }
        selenium.uncheck(FilterConfiguration.MS_Doc_CheckAll);
        selenium.click(FilterConfiguration.MS_Doc_CheckAll);
        selenium.click(FilterConfiguration.Save_MSDocFilter_BUTTON); 
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
   }

	/*
	 * remove filters.
	 */
	public void removeFilters(Selenium selenium, String Filters) {
		String[] iFilters = Filters.split(",");
		boolean iRemove = false;
		selenium.click(FilterConfiguration.ExpnadAll_CHECKBOX);

		int i = 1;
		while (selenium
				.isElementPresent(FilterConfiguration.FiltersConfiguration_TABLE
						+ "/tr[" + i + "]")) {

			if (selenium
					.isElementPresent(FilterConfiguration.FiltersConfiguration_TABLE
							+ "/tr[" + i + "]//a")) {
				if (selenium
						.isElementPresent(FilterConfiguration.FiltersConfiguration_TABLE
								+ "/tr[" + i + "]/td[2]/div/table/tbody/tr")) {

					int j = 1;
					while (selenium
							.isElementPresent(FilterConfiguration.FiltersConfiguration_TABLE
									+ "/tr["
									+ i
									+ "]/td[2]/div/table/tbody/tr[" + j + "]")) {
						for (String iFilter : iFilters) {
							if (selenium
									.getText(
											FilterConfiguration.FiltersConfiguration_TABLE
													+ "/tr["
													+ i
													+ "]/td[2]/div/table/tbody/tr["
													+ j + "]").equals(iFilter)) {
								selenium.click(FilterConfiguration.FiltersConfiguration_TABLE
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
			selenium.click(FilterConfiguration.Remove_BUTTON);
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
					selenium.click(FilterConfiguration.ExpnadAll_CHECKBOX);

					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click("link=" + iFilterName);
						selenium.click(FilterConfiguration.a_CHECKBOX);
						selenium.click(FilterConfiguration.Save_HtmlFilter_BUTTON);
					} else {
						Reporter.log("The html filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("indesign")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click("link=" + iFilterName);
						selenium.click(FilterConfiguration.ignoreLineBreak_CHECKBOX);
						selenium.click(FilterConfiguration.Save_InDesignIDMLFilter_BUTTON);
					} else {
						Reporter.log("The indd/idml filter " + iFilterName
								+ " doesn't exist!");
					}
				}

				else if (iFilter.equals("javaproperties")) {
					if (selenium.isElementPresent("link=" + iFilterName)) {
						selenium.click("link=" + iFilterName);
						selenium.click(FilterConfiguration.enableSIDSupport_CHECKBOX);
						selenium.click(FilterConfiguration.enableUnicodeEscap_CHECKBOX);
						selenium.click(FilterConfiguration.preserveTrailingSpaces_CHECKBOX);
						selenium.click(FilterConfiguration.Save_JavaPropertiesFilter_BUTTON);
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
						selenium.click(FilterConfiguration.enableUnicodeEscape_CHECKBOX);
						selenium.click(FilterConfiguration.Save_JavaScriptFilter_BUTTON);
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
						selenium.click(FilterConfiguration.addAdditionalHead_CHECKBOX);
						selenium.click(FilterConfiguration.Save_JspFilter_BUTTON);
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
						selenium.click(FilterConfiguration.headerTranslate_CHECKBOX);
						selenium.click(FilterConfiguration.masterTranslate_CHECKBOX);
						selenium.click(FilterConfiguration.add_MS2010Filter_BUTTON);
						selenium.type(FilterConfiguration.addStyle_TEXT_FIELD,
								"AddSyles");
						selenium.click(FilterConfiguration.Save_AddStyle2010_BUTTON);
						selenium.click(FilterConfiguration.Save_MS2010Filter_BUTTON);
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
						selenium.click((FilterConfiguration.HeaderInformation_MSDocFilter_CHECKBOX));
						selenium.click(FilterConfiguration.Save_MSDocFilter_BUTTON);
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
						selenium.click(FilterConfiguration.Save_MSExcelFilter_BUTTON);
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
						selenium.click(FilterConfiguration.AltPPT_CHECKBOX);
						selenium.click(FilterConfiguration.Save_MSPowerPointFilter_BUTTON);
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
						selenium.click((FilterConfiguration.headerInformationOpenOffice_CHECKBOX));
						selenium.click(FilterConfiguration.Save_OpenOfficeFilter_BUTTON);
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
								FilterConfiguration.SecondaryFilterPO_SELECT,
								"label=" + secondaryFilter);
						selenium.click(FilterConfiguration.Save_PortableObjectFilter_BUTTON);
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
