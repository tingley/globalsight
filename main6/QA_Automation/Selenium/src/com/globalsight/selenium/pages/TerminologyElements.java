package com.globalsight.selenium.pages;

public class TerminologyElements
{
    public static final String MAIN_NEW_BUTTON = "//input[@id='idNew']";
    public static final String MAIN_IMPORT_BUTTON = "//input[@id='idImport']";
    public static final String MAIN_EXPORT_BUTTON = "//input[@id='idExport']";
    public static final String MAIN_STATISTICS_BUTTON = "//input[@id='idStatistics']";
    public static final String MAIN_INDEX_BUTTON = "//input[@id='idIndexes']";
    public static final String MAIN_REMOVE_BUTTON = "//input[@id='idRemove']";
    public static final String MAIN_DUPLICATE_BUTTON = "//input[@id='idClone']";
    public static final String MAIN_EDIT_BUTTON = "//input[@id='idModify']";
    public static final String MAIN_BROWSE_BUTTON = "//input[@id='idBrowse']";
    public static final String MAIN_MAINTENANCE_BUTTON = "//input[@id='idMaintenance']";
    public static final String MAIN_SEARCHTERMS_BUTTON = "//input[@id='idSearchTerm']";
    public static final String MAIN_BROWSER_BUTTON="idBrowse";
    public static final String MAIN_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";

    // import page
    public static final String IMP_FILE_TEXT = "//input[@id='idFilename']";
    public static final String IMP_XML_RADIO = "//input[@id='idXml']";
    public static final String IMP_TBX_RADIO = "//input[@id='idTbx']";
    public static final String IMP_EXCEL_RADIO = "//input[@id='idExcel']";
    public static final String IMP_TRADOS_RADIO = "//input[@id='idMtf']";
    public static final String IMP_CSV_RADIO = "//input[@id='idCsv']";
    public static final String IMP_NEXT_BUTTON = "//input[@id='nextButton']";
    public static final String IMP_IMPORT_BUTTON_3 = "//input[@id='importButton']";
    public static final String IMP_TAB_RADIO = "//input[@id='idTab']";
    public static final String IMP_SEMICOLON_RADIO = "//input[@id='idSemicolon']";
    public static final String IMP_COMMA_RADIO = "//input[@id='idComma']";
    public static final String IMP_SPACE_RADIO = "//input[@id='idSpace']";
    public static final String IMP_OTHER_RADIO = "//input[@id='idOther']";
    public static final String IMP_OTHER_TEXT = "//input[@id='idDelimitText']";
    public static final String IMP_SKIP_FIRST_LINE_CHECKBOX = "//input[@id='idIgnoreHeader']";

    // import Options
    public static final String IMP_ADD_ALL_RADIO = "//input[@id='idSync1']";
    public static final String IMP_SYNCHRONIZE_ON_CONCEPT_ID_RADIO = "//input[@id='idSync2']";
    public static final String IMP_SYNCHRONIZE_ON_LANGUAGE_RADIO = "//input[@id='idSync3']";
    public static final String IMP_OVERWRITE_EXISTING_CONCEPTS_CONCEPT_RADIO = "//input[@id='idSync2O']";
    public static final String IMP_MERGE_EXISTING_CONCEPTS_CONCEPT_RADIO = "//input[@id='idSync2M']";
    public static final String IMP_DISCARD_NEW_CONCEPTS_CONCEPT_RADIO = "//input[@id='idSync2D']";
    public static final String IMP_OVERWRITE_EXISTING_CONCEPTS_LANGUAGE_RADIO = "//input[@id='idSync3O']";
    public static final String IMP_MERGE_EXISTING_CONCEPTS_LANGUAGE_RADIO = "//input[@id='idSync3M']";
    public static final String IMP_DISCARD_NEW_CONCEPTS_LANGUAGE_RADIO = "//input[@id='idSync3D']";
    public static final String IMP_DISCARD_RADIO = "//input[@id='idNosync3D']";
    public static final String IMP_ADD_RADIO = "//input[@id='idNosync3A']";
    public static final String IMP_IMPORT_BUTTON_2 = "//input[@name='Import']";
    public static final String IMP_NEXT_BUTTON_2 = "//input[@name='Next']";

    // done
    public static final String OK_BUTTON = "//input[@value='OK']";

    // create page
    public static final String NEW_NAME_TEXT = "idName";
    public static final String NEW_DESC_TEXTAREA = "idDescription";
    public static final String NEW_SAVE_BUTTON = "OK";
    public static final String NEW_ADD_LANGUAGE_BUTTON = "idAdd";
    public static final String NEW_REMOVE_LANGUAGE_BUTTON = "//input[@value='Remove' and @onclick='removeLanguage()']";
    public static final String NEW_ADD_FIELD_BUTTON = "//input[@name='idAdd' and @onclick='newField()']";
                                                      //input[@name='idAdd' and @value='Add' and @type='BUTTON' and @onclick='newField()']
    public static final String NEW_CANCEL_BUTTON = "Cancel";
    public static final String NEW_SORT_ORDER_RADIO_1 = "//input[@name='radioBtn' and @onclick='fnCheckbox(1)']";
    public static final String NEW_SORT_ORDER_RADIO_2 = "//input[@name='radioBtn' and @onclick='fnCheckbox(2)']";
    public static final String NEW_SORT_BY_LANGUAGE_SELECT = "idLocale";
    public static final String NEW_SORT_BY_LOCALE_SELECT = "idLocaleCountry";
    public static final String NEW_FIELD_NAME_TEXT = "idName";

    //edit page
    public static final String MODIFY_FIELD_BUTTON = "//input[@value='Modify']";
    public static final String MODIFY_FIELD_OK_BUTTON = "//button[@onclick='doClose(true);']";
    
    //maintenance page
    public static final String SEARCHFOR_FIELD = "idSearch";
    public static final String CONCEPT_LEVEL_BUTTON = "idLevelConcept";
    public static final String CONCEPT_LEVEL_SELECT = "idFieldConcept";
    public static final String SEARCH_BUTTON = "idSearchBtn";
    public static final String REPLACE_OK_BUTTON = "replaceButton";
    public static final String REPLACEWITH_FILED = "idReplace";
    public static final String ENTRY_CHECK 	= "checkbox0";
    
    // export page
    public static final String EXP_NEXT_BUTTON = "//input[@value='Next']";
    public static final String EXP_EXPORT_BUTTON = "//input[@value='Export...']";
    public static final String EXP_DOWNLOAD_BUTTON = "//input[@id='idRefreshResult']";
    public static final String EXP_OK_BUTTON = "//input[@id='idCancelOk']";
    
    // statistics page
    public static final String STAT_CLOSE_BUTTON = "//input[@id='idOk']";
    
    //index page
    public static final String IDX_SAVE_BUTTON = "//input[@id='idSave']";
    public static final String IDX_REINDEX_BUTTON = "//input[@id='idReindex']";
    public static final String IDX_INPROGRESS_TEXT = "//input[@id='idProgress2']";
    public static final String IDX_PREVIOUS_BUTTON = "//input[@id='idPrevious']";
    public static final String IDX_FINISH_OK_BUTTON = "//input[@id='idCancelOk']";
    
    
    //termbase browser
    
    public static final String TermbaseViewer_TAG="tbviewer";
    public static final String Source_SELECT="idSource";
    public static final String Target_SELECT="idTarget";
    public static final String Query_TEXT_FIELD="idQuery";
    public static final String SearchType_SELECT="searchType";
    public static final String Execute_BUTTON="idExecute";
    public static final String TermsFound_LIST="//div[@id='idHitList']/ul";
    public static final String TermDetails_TEXT_FIELD="//div[@id='idViewerEntry']";
    public static final String CloseTermbase_IMG="//span[@id='idCloseWindow']/img";
    
    //Search Terms
    
    public static final String Source_Locale="id_sourcelocale";
    public static final String Target_Locale="id_targetlocale";
    public static final String Select_Termbases="id_tbnames";
    public static final String MatchType="id_matchtype";
    public static final String SearchForTerm="id_searchstr";
    public static final String Search_Button="Submit";
    public static final String Search_Term__Table= "//div[@id='contentLayer']/form/table/tbody";
    
}
