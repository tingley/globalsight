package com.globalsight.selenium.functions;

import java.io.File;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.TerminologyElements;
import com.globalsight.selenium.testcases.smoketest.SearchTermforTermbase;
import com.thoughtworks.selenium.Selenium;

public class TerminologyFuncs extends BasicFuncs
{
    private static final String MAIN_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";
    private static final String TB_LANG_TABLE = "//div[@id='contentLayer']/table/tbody/tr[5]/td[2]/form/table/tbody";
    private static final String COLUMN_DETAIL = "//div[@id='contentLayer']/table[@id='idColumns']/tbody";
    private static final String STATISTICS_TABLE = "//div[@id='idStatistics']/table/tbody";
    private static final String FIELD_TABLE = "//div[@id='contentLayer']/table/tbody/tr[8]/td[2]/form/table/tbody";
    private static final String Filed_Search_Result_TABLE = "//table[@id='idTable']/tbody";
    
    /**
     * Public method to create a new termbase
     * @param selenium a running selenium
     * @param termBaseName new termbase name
     * @param language new language string of new termbase
     * @param langSort 
     * @param fields new field names of termbase
     * @throws Exception
     */
    public void create(Selenium selenium, String termBaseName, String language, String langSort, String fields) throws Exception
    {
        // click the new button
        clickAndWait(selenium, TerminologyElements.MAIN_NEW_BUTTON);
        // step 1
        inputNameAndDiscription(selenium, termBaseName, "");
        // step 2
        defineLanguages(selenium, language, langSort);
        // step 3
        defineFields(selenium, fields);
        
        selenium.click(TerminologyElements.NEW_SAVE_BUTTON);
        if (selenium.isAlertPresent())
        {
//            Reporter.log(selenium.getAlert());
            Assert.assertTrue(false, selenium.getAlert());
            selenium.click(TerminologyElements.NEW_CANCEL_BUTTON);
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
    
    public void create(Selenium selenium, String termBaseStr) {
        
        // click the new button
        clickAndWait(selenium, TerminologyElements.MAIN_NEW_BUTTON);
        String[] itermBases = termBaseStr.split(",");
        
        for (String termbase : itermBases) {
            String[] ivalue = termbase.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();
            
            if (iFieldName.equals("name")) {
                selenium.type("idName", iFieldValue);
            } else if (iFieldName.equals("description")) {
                selenium.type("idDescription", iFieldValue);
            }
        }
        
        selenium.click(TerminologyElements.NEW_SAVE_BUTTON);
        if (selenium.isAlertPresent())
        {
            Reporter.log(selenium.getAlert());
            selenium.click(TerminologyElements.NEW_CANCEL_BUTTON);
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /**
     * Statistic of a termbase
     * @param selenium
     * @param termNumbers 
     * @throws Exception 
     */
    public void statistics(Selenium selenium, String termBaseName, String termNumbers) throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, termBaseName);
        if (!selected)
        {
            Reporter.log("Cannot find a proper termbase to do statistics.");
            return;
        }
        clickForModalDialog(selenium, TerminologyElements.MAIN_STATISTICS_BUTTON);
        String[] statisticsData = termNumbers.split(";");
        for (String data : statisticsData)
        {
            String langName = data.substring(0, data.indexOf(","));
            String termNo = data.substring(data.indexOf(",") + 1);
            
            int j = 1;
            while (j <= statisticsData.length)
            {
                if (selenium.getText(STATISTICS_TABLE + "/tr[" + j + "]/td[1]").equals(langName))
                {
                    String no = selenium.getText(STATISTICS_TABLE + "/tr[" + j + "]/td[2]");
                    Assert.assertTrue(no.equals(termNo));
                    break;
                }
                j++;
            }
        }
        
        selenium.click(TerminologyElements.STAT_CLOSE_BUTTON);
        selenium.selectWindow(null);
    }
    
    /**
     * Public method to import data into an existing termbase
     * @param selenium A running selenium
     * @param filePath The imported file directory 
     * @param fileName The imported file name
     * @param termBaseName The termbase name to do import
     * @param dir Folder name of the imported file, so it is the name of termbase
     * @throws Exception
     */
    public void importData(Selenium selenium, String filePath,
            String fileName, String termBaseName, String dir, String newLang) throws Exception
    {
        // select a terminology
        boolean checked = selectRadioButtonFromTable(selenium, MAIN_TABLE, termBaseName);
        if (!checked)
        {
            Reporter.log("Cannot find a proper termbase to import data.");
            return;
        }
        // import
        clickAndWait(selenium, TerminologyElements.MAIN_IMPORT_BUTTON);
        
        String importDone = "var pro = window.document.getElementById('idProgress').innerHTML;" +
        		"pro.indexOf('100%')!=-1;";
        String indexDone = "var pro = window.document.getElementById('idProgress2_reindex').innerHTML;" +
                "pro.indexOf('100%')!=-1;";
        
        // type in import file directory
        selenium.type(TerminologyElements.IMP_FILE_TEXT, filePath
                + File.separator + dir + File.separator + fileName);
        if (fileName.toLowerCase().endsWith(".xml")
                || fileName.toLowerCase().endsWith(".tbx"))
        {
            if (dir.toLowerCase().equals("trados"))
            {
                selenium.click(TerminologyElements.IMP_TRADOS_RADIO);
            }
            clickAndWait(selenium, TerminologyElements.IMP_NEXT_BUTTON);
            // import options
            clickAndWait(selenium, TerminologyElements.IMP_IMPORT_BUTTON_2);
        }
        else if (fileName.toLowerCase().endsWith(".csv")
                || fileName.toLowerCase().endsWith(".txt"))
        {
            selenium.click(TerminologyElements.IMP_COMMA_RADIO);
            selenium.check(TerminologyElements.IMP_SKIP_FIRST_LINE_CHECKBOX);
            clickAndWait(selenium, TerminologyElements.IMP_NEXT_BUTTON);
            // Verify CSV Columns
            editColumnDetail(selenium, newLang);
            clickAndWait(selenium, TerminologyElements.IMP_NEXT_BUTTON_2);
            // import options
            clickAndWait(selenium, TerminologyElements.IMP_IMPORT_BUTTON_2);
        }
        else if (fileName.toLowerCase().endsWith(".xls")
                || fileName.toLowerCase().endsWith(".xlsx")) 
        {
            clickAndWait(selenium, TerminologyElements.IMP_IMPORT_BUTTON_3);
        }
        selenium.waitForCondition(importDone, CommonFuncs.LONG_WAIT);
        selenium.waitForCondition(indexDone, CommonFuncs.LONG_WAIT);
        Thread.sleep(15000);
        // import done
        clickAndWait(selenium, TerminologyElements.OK_BUTTON);
    }
    
    /**
     * Export a termbase and download
     * @param selenium
     * @param termBaseName
     * @throws Exception
     */
    public void export(Selenium selenium, String termBaseName) throws Exception
    {
        boolean check = selectRadioButtonFromTable(selenium, MAIN_TABLE, termBaseName);
        if (!check)
        {
            Reporter.log("Cannot find the termbase to do export operation.");
            return;
        }
        clickAndWait(selenium, TerminologyElements.MAIN_EXPORT_BUTTON);
        
        clickAndWait(selenium, TerminologyElements.EXP_NEXT_BUTTON);
        
        clickAndWait(selenium, TerminologyElements.EXP_EXPORT_BUTTON);
        
        String exportDone = "var pro = window.document.getElementById('idProgress').innerHTML;" +
            "pro.indexOf('100%')!=-1;";
        selenium.waitForCondition(exportDone, CommonFuncs.LONG_WAIT);
        
        selenium.click(TerminologyElements.EXP_DOWNLOAD_BUTTON);
        clickAndWait(selenium, TerminologyElements.EXP_OK_BUTTON);
    }
 
    /**
     * ReIndex the termbase
     */
    public void reindex(Selenium selenium, String termBaseName) throws Exception
    {
    	boolean check = selectRadioButtonFromTable(selenium, MAIN_TABLE, termBaseName);
    	if (!check)
    	{
    		Reporter.log("Cannot find the termbase to reindex.");
    		return;
    	}
    	clickAndWait(selenium,TerminologyElements.MAIN_INDEX_BUTTON);
    	clickAndWait(selenium,TerminologyElements.IDX_SAVE_BUTTON);
    	clickAndWait(selenium,TerminologyElements.IDX_REINDEX_BUTTON);
    	Thread.sleep(10000);
    	boolean INPROGRESS = TerminologyElements.IDX_INPROGRESS_TEXT.contains("(100%)");
    	if (!INPROGRESS)
    	{
    		Reporter.log("Termbase index is wrong.");
    		Assert.assertTrue(false, selenium.getAlert());
    		return;
    	}
    	selenium.click(TerminologyElements.IDX_FINISH_OK_BUTTON);
    	selenium.click(TerminologyElements.IDX_PREVIOUS_BUTTON);
    }
    
    public void remove(Selenium selenium, String termBaseName) throws Exception
    {
    	boolean check =selectRadioButtonFromTable(selenium, MAIN_TABLE, termBaseName);
    	if (!check)
    	{
    		Reporter.log("Cannot find the termbase to remove.");
    		return;
    	}
    	clickAndWait(selenium,TerminologyElements.MAIN_REMOVE_BUTTON);
    	boolean actual = selenium.getConfirmation().equals("Removing a termbase will delete all data.\nDo you want to continue?");
    	Assert.assertEquals(actual,true);
    	
    	check =selectRadioButtonFromTable(selenium, MAIN_TABLE, termBaseName);
    	if (!check)
    	{
    		Reporter.log("The termbase was removed successfully");
    	}
    	else {Reporter.log("ERROR: Termbase remove feature doesn't work!");}
    }
    
    //added by ShenYang  2011-07-04
    public void duplicate(Selenium selenium, String iTBName, String dupName) throws Exception{
    	boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, iTBName);
    	if (!selected)
    	{
    		Reporter.log("Cannot find the termbase to duplicate.");
    		return;
    	}
    	clickAndWait(selenium, TerminologyElements.MAIN_DUPLICATE_BUTTON);
    	//inputNameAndDiscription(selenium, dupName, "For duplicate test.");
    	selenium.type(TerminologyElements.NEW_NAME_TEXT, dupName);
    	selenium.type(TerminologyElements.NEW_DESC_TEXTAREA, "For duplicate test.");
    	selenium.click(TerminologyElements.NEW_SAVE_BUTTON);
        if (selenium.isAlertPresent())
        {
            Reporter.log(selenium.getAlert());
            selenium.click(TerminologyElements.NEW_CANCEL_BUTTON);
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        //verify if duplicate success
        Assert.assertEquals(isPresentInTable(selenium, MAIN_TABLE, dupName), true);    	
    }
    
    //added by ShenYang  2011-07-04
    
   public void edit(Selenium selenium, String editProfile) throws Exception{
	   //editProfile fomat as: oldTBName|newTBName|newDescription|languageAndLocaleToAdd|languageSortToAdd|oldFieldName|newFieldName,newFieldType,allowedValue
	   String[] editProfileParts = editProfile.split("\\|");
	   
	   String oldTBName = editProfileParts[0].trim();
	   String newTBName = editProfileParts[1].trim();
	   String newDescription = editProfileParts[2].trim();
	   String[] languageAndLocale = editProfileParts[3].trim().split(",");
	   String languageSort = editProfileParts[4].trim();
	   String oldFieldName = editProfileParts[5].trim();
	   String newFieldName = editProfileParts[6].trim();
	   
	   boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, oldTBName);
		if (!selected)
		{
			Reporter.log("Cannot find the termbase to edit.");
			return;
		}
		clickAndWait(selenium, TerminologyElements.MAIN_EDIT_BUTTON);
		selenium.type(TerminologyElements.NEW_NAME_TEXT, newTBName);
    	selenium.type(TerminologyElements.NEW_DESC_TEXTAREA, newDescription);
		
    	addlanguageInTable(selenium, languageAndLocale[0], languageAndLocale[1],languageSort);
    	
    	selected = selectRadioButtonFromTable(selenium, FIELD_TABLE, oldFieldName);
		if (!selected)
		{
			Reporter.log("Cannot find the fieldname to edit.");
			return;
		}
		clickForModalDialog(selenium, TerminologyElements.MODIFY_FIELD_BUTTON);
		//selenium.click(TerminologyElements.MODIFY_FIELD_BUTTON);
		selenium.type("idName", newFieldName);
		selenium.close();
		selenium.selectWindow(null);
		acceptModalValue(selenium, TerminologyElements.MODIFY_FIELD_BUTTON, new String[] { newFieldName }, "field");
		
		selenium.click(TerminologyElements.NEW_SAVE_BUTTON);
        if (selenium.isAlertPresent())
        {
            Reporter.log(selenium.getAlert());
            selenium.click(TerminologyElements.NEW_CANCEL_BUTTON);
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        Assert.assertEquals(isPresentInTable(selenium, MAIN_TABLE, newTBName), true);
   	
   }
   
   //added by ShenYang   2011-07-04
   public void maintenance(Selenium selenium, String iTBName, String fieldName, String searchStr, String newStr) throws Exception{
	   boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, iTBName);
   	if (!selected)
   	{
   		Reporter.log("Cannot find the termbase to Maintenance.");
   		return;
   	}
   	clickAndWait(selenium, TerminologyElements.MAIN_MAINTENANCE_BUTTON);
   	selenium.type(TerminologyElements.SEARCHFOR_FIELD, searchStr);
   	selenium.click(TerminologyElements.CONCEPT_LEVEL_BUTTON);
   	selenium.select(TerminologyElements.CONCEPT_LEVEL_SELECT, fieldName);
   	selenium.click(TerminologyElements.SEARCH_BUTTON);
   	//Verify if search results has been displayed in bottom table and then change it.
   	boolean y = selenium.isElementPresent(Filed_Search_Result_TABLE+"/tr[2]/td[3]");
    if(y==true){
   		selenium.type(TerminologyElements.REPLACEWITH_FILED, newStr);
   		selenium.click(TerminologyElements.ENTRY_CHECK);
   		selenium.click(TerminologyElements.REPLACE_OK_BUTTON);
   		//verify if filed content string has been changed
   		selenium.type(TerminologyElements.SEARCHFOR_FIELD, newStr);
   		selenium.select(TerminologyElements.CONCEPT_LEVEL_SELECT, fieldName);
   		selenium.click(TerminologyElements.SEARCH_BUTTON);
   		Assert.assertEquals(selenium.isElementPresent(Filed_Search_Result_TABLE+"/tr[2]/td[3]"), true);
   	}
    else Reporter.log("Cannot find the corresponding field content entry.");
    
    }

    // ###################################################################################################
    // ###################################       private methods       ###################################
    // ###################################################################################################
    /**
     * Step 1 of creating termbase
     */
    private void inputNameAndDiscription(Selenium selenium, String newTermBaseName, String desc)
    {
        selenium.type(TerminologyElements.NEW_NAME_TEXT, newTermBaseName);
    }
    
    /**
     * Step 2 of creating termbase
     * @throws Exception 
     */
    private void defineLanguages(Selenium selenium, String language, String languageSort) throws Exception
    {
        if (language != null && language.length() != 0)
        {
            String[] newLangs = language.split(";");
            for (int i = 0; i < newLangs.length; i++)
            {
                String[] langAndLocale = newLangs[i].split(",");
                if (selenium.isTextPresent(langAndLocale[0]))
                {
                    deleteLanguageInTable(selenium, langAndLocale[0]);
                }
                addlanguageInTable(selenium, langAndLocale[0], langAndLocale[1], languageSort);
            }
        }
    }
    
    /**
     * Step 3 of creating termbase
     */
    private void defineFields(Selenium selenium, String fields)
    {
        if (fields != null && fields.length() != 0)
        {
            String[] fieldArray = fields.split(",");
            for (String field : fieldArray)
            {
                addFields(selenium, field);
            }
        }
    }
    
    /**
     * Delete default languages in termbase language table
     * @param selenium
     * @param languageName
     * @throws Exception
     */
    private void deleteLanguageInTable(Selenium selenium, String languageName) throws Exception
    {
        boolean checked = selectRadioButtonFromTable(selenium, TB_LANG_TABLE, languageName);
        if (!checked)
        {
            Reporter.log("Cannot find a proper language to delete.");
            return;
        }
        selenium.click(TerminologyElements.NEW_REMOVE_LANGUAGE_BUTTON);
    }
    
    /**
     * Add new language in termbase language table
     * @param selenium
     * @param languageName
     * @param locale
     * @param languageSort 
     */
    private void addlanguageInTable(Selenium selenium, String languageName, String locale, String languageSort)
    {
        // open child page
        clickForModalDialog(selenium, TerminologyElements.NEW_ADD_LANGUAGE_BUTTON);
        // input new language
        selenium.type("idName", languageName);
        
        String select = "";
        if (languageSort.equals("language"))
        {
            selenium.click(TerminologyElements.NEW_SORT_ORDER_RADIO_1);
            selenium.select(TerminologyElements.NEW_SORT_BY_LANGUAGE_SELECT, "label=" + locale);
            select = selenium.getEval("window.document.getElementById('idLocale').value");
        }
        else if (languageSort.equals("locale")) 
        {
            selenium.click(TerminologyElements.NEW_SORT_ORDER_RADIO_2);
            selenium.select(TerminologyElements.NEW_SORT_BY_LOCALE_SELECT, "label=" + locale);
            select = selenium.getEval("window.document.getElementById('idLocaleCountry').value");
        }
        
        String text = selenium.getEval("window.document.getElementById('idName').value");
        selenium.close();
        selenium.selectWindow(null);
        // input information
        acceptModalValue(selenium, TerminologyElements.NEW_ADD_LANGUAGE_BUTTON, new String[] { text, select }, "language");
    }
    
    /**
     * Add new fields when creating new termbase
     * @param selenium
     * @param field
     */
    private void addFields(Selenium selenium, String field)
    {
        clickForModalDialog(selenium, TerminologyElements.NEW_ADD_FIELD_BUTTON);
        selenium.type(TerminologyElements.NEW_FIELD_NAME_TEXT, field);
        selenium.close();
        selenium.selectWindow(null);
        // input information
        acceptModalValue(selenium, TerminologyElements.NEW_ADD_FIELD_BUTTON, new String[] { field }, "field");
        
    }
    
    /**
     * Edit column details when importing csv files
     * @param selenium
     */
    private void editColumnDetail(Selenium selenium, String newLang)
    {
        String columnsCount = selenium.getEval("window.document.getElementById('idColumns').rows.length");
        String[] lang = null;
        
        if (newLang.length() != 0)
        {
            String[] tmp = newLang.split(";");
            lang = new String[tmp.length];
            for (int i = 0; i < tmp.length; i++)
            {
                lang[i] = tmp[i].substring(0, tmp[i].indexOf(","));
            }
        }
        
        for (int i = 1; i < Integer.parseInt(columnsCount); i++)
        {
            String Map_Column_to_Type = "";
            String Column_Name = selenium.getText(COLUMN_DETAIL + "/tr[" + i + "]/td[1]");
            String term_Language = Column_Name;
            if (i == 1 || i == 2)
            {
                Map_Column_to_Type = "term";
                term_Language = lang[i - 1];
            }
            else 
            {
                Map_Column_to_Type = "concepttext-" + Column_Name.toLowerCase().replace(" ", "_");
            }
            
            acceptModalValue(selenium, "//input[@name='propId" + i + "']",
                    new String[] { String.valueOf(i - 1), Column_Name, Map_Column_to_Type, term_Language },
                    "column");
        }
    }
    
    /**
     * Open new window for showmodeldialog
     * @param selenium
     * @param locator
     */
    public void clickForModalDialog(Selenium selenium, String locator)
    {
        String overrideShowModalDialogJs = "if(selenium.browserbot.getCurrentWindow().showModalDialog){";
        overrideShowModalDialogJs += "selenium.browserbot.getCurrentWindow().showModalDialog = function(sURL, vArguments, sFeatures){";
        overrideShowModalDialogJs += "selenium.browserbot.getCurrentWindow().open(sURL, 'modal', sFeatures);";
        overrideShowModalDialogJs += "};}";
        // showModalDialog methods overwrite
        selenium.getEval(overrideShowModalDialogJs);
        selenium.click(locator);
        selenium.selectWindow("name=modal");
    }
    
    private void acceptModalValue(Selenium selenium, String locator, String[] values, String tableName)
    {
        String overrideShowModalDialogJs = "if(selenium.browserbot.getCurrentWindow().showModalDialog){";
        overrideShowModalDialogJs += "selenium.browserbot.getCurrentWindow().showModalDialog = function accept(){";
        overrideShowModalDialogJs += generateModalDialogReturnObject(values, tableName);
        overrideShowModalDialogJs += "};}";
        // showModalDialog methods overwrite
        selenium.getEval(overrideShowModalDialogJs);
        selenium.click(locator);
    }
    
    private String generateModalDialogReturnObject(String[] values, String tableName)
    {
        StringBuffer returnObject = new StringBuffer();
        if (tableName.equals("language"))
        {
            returnObject.append("var oLanguage = new window.Language('" + values[0] +"','" + values[1] + "',true,false);");
            returnObject.append("return oLanguage;");
        }
        else if (tableName.equals("field")) 
        {
            returnObject.append("var oField = new window.Field('" + values[0] + "','text','text',false,'');");
            returnObject.append("return oField;");
        }
        else if (tableName.equals("column")) 
        {
            returnObject.append("var oProperties = new window.Properties("+ values[0] + ");");
            returnObject.append("oProperties.name = '" + values[1] + "';");
            returnObject.append("oProperties.type = '" + values[2] + "';");
            returnObject.append("oProperties.associatedColumn = -1;");
            returnObject.append("oProperties.termLanguage = '" + values[3] + "';");
            returnObject.append("return oProperties;");
        }
        return returnObject.toString();
    }
    public String searchterms(Selenium selenium, String item, int iTd) throws Exception
    {
    	 	BasicFuncs basicfuncs=new BasicFuncs();
    	 	boolean result=basicfuncs.isPresentInTable(selenium, TerminologyElements.Search_Term__Table, item,iTd);
    	 	String info=null;
    	 	if (result==true)
    	 	{
    	 		info="The term is in the termbase";
    	 	}
    	 	else{
    	 		info="The term is not in the termbase";
    	 	}
    	 	return info;
    }
}
