package com.globalsight.selenium.pages;

public class TMManagement
{
	public static final String TM_SEARCH_CONTENT_TEXT = "id=tmNameFilter";
    public static final String New_BUTTON = "//input[@value='New...']";
    public static final String EDIT_BUTTON = "idModify";
    public static final String Demo = "TMId";

    public static final String TM_MANAGEMENT_TABLE = "//div[@id='contentLayer']//form//table//tbody//tr[2]//td//table//tbody";

    public static final String Import_BUTTON="id=importBtn";
    public static final String CorpusBrowser_BUTTON="//input[@id='corpusBtn']";
    public static final String Statistics_BUTTON="//input[@value='Statistics']";
    public static final String MAINTENANCE_BUTTON="//input[@id='mainBtn']";
    public static final String EXPORT_BUTTON="//input[@id='exportBtn']";
    public static final String REINDEX_BUTTON="//input[@id='reindexBtn']";
    public static final String DUPLICATE_BUTTON="//input[@id='dupBtn']";
    public static final String REMOVE_BUTTON="//input[@id='deleteBtn']";
    
    // Define New TM
    public static final String Name_TEXT_FIELD = "name";
    public static final String DOMAIN_TEXT = "domain";
    public static final String ORGANIZATION_TEXT = "organization";
    public static final String DESCRIPTION_TEXT = "description";
    public static final String RemoteTM_CHECKBOX = "idRemoteTm";
    public static final String TM_INDEX_TARGET_CHECKBOX = "id=idIndexTarget";
    public static final String Browse_BUTTON="id=idXml";
    public static final String import_path_TEXT = "id=idFilename"; 
    public static final String import_name = "sourceTmName";
    public static final String import_format = "idTtmxRtf";
    public static final String Next_BUTTON = "//input[@value='Next']";

    public static final String Cancel_BUTTON = "Cancel";
    public static final String SAVE_BUTTON = "OK";
    
    
    
    
    
    
    //TM Statistics
    public static final String TMStatistics_TAG="TM Statistics";
    public static final String TotalTUs_TEXT_FIELD="idTmTus";
    public static final String TotalTUVs_TEXT_FIELD="idTmTuvs";
    public static final String Close_BUTTON="idOk";
    
    //Corpus Browser
    public static final String CorpusBrowser_TAG="Concordance";
    public static final String FullText_RADIO="rFullText";
    public static final String FuzzyMatch_RADIO="rFuzzy";
    public static final String Search_TEXT_FIELD="queryText";
    public static final String LocalePair_SELECT="localePair";
    public static final String TranslationMemory_SELECTION="tmIndex";
    public static final String Search_BUTTON="//input[@value='Search']";
    public static final String Close_BUTTON_CORPUSBROWSER="//input[@value='Close']";
    public static final String StatusMessageTop_TEXT="statusMessageTop";
    
    //Maintenance
    public static final String SEARCH_TEXT="SourceFindText";
    public static final String SOURCE_LOCALE_SELECT="TmSourceSearchLocaleSelector";
    public static final String TARGET_LOCALE_SELECT="TmTargetSearchLocaleSelector";
    public static final String NEXT_BUTTON="Search";
    public static final String Progress_MSG="idProgress";
    
    public static final String CONTINUE_BUTTON="//input[@value='Continue']";
    public static final String Cancel_BUTTON_BOTTOM="CancelBtnBottom";
    public static final String CANCEL_TOP_BUTTON="CancelBtnTop";
    
    //Reindex
    public static final String SELECTED_TM_RADIO="idSelectedTm";
    public static final String AllTms_RADIO="idAllTms";
    public static final String REINDEX_NEXT_BUTTON="//button[@onclick='doNext();']";
    public static final String REINDEX_MESSAGE="idMessage";
    public static final String ReindexProgress_MSG="idProgress";
    //idProgressContainer
    public static final String REINDEX_OK_BUTTON="idCancelOk";
    
    //Duplicate Define New TM
    public static final String DUPLICATE_NAME_TEXT="name";
    public static final String DUPLICATE_OK_BUTTON="OK";
    public static final String DUPLICATE_CANCEL_BUTTON="Cancel";
    
    
    //Remove the TM
    public static final String ENTRIE_TM_RADIO="checkboxTm";
    public static final String REMOVE_OK_BUTTON="//input[@value='OK']";
    public static final String RemoveMessages_MSG="idMessage";
    public static final String RemoveProgress_MSG="idProgress";
    public static final String REMOVE2_OK_BUTTON="idCancelOk";
    
    //Export TM
    public static final String EXPORT_NEXT_BUTTON="//button[@onclick='doNext();']";
    public static final String EXPORT_OK_BUTTON="idCancelOk";
    public static final String DOWNLOAD_FILE_BUTTON = "idRefreshResult";
    
    public static final String EXPORT_MESSAGE="idMessages";
    public static final String EXPORT_PROGRESS_MSG="idProgress";
	
    
}
