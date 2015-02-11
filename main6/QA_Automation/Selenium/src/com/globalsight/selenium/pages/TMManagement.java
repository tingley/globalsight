package com.globalsight.selenium.pages;

public class TMManagement
{
    public static final String New_BUTTON = "idNew";
    public static final String Edit_BUTTON = "idModify";
    public static final String Demo = "TMId";

    public static final String TMMangement_TABLE = "//div[@id='contentLayer']//form//table//tbody//tr[2]//td//table//tbody";

    
    public static final String CorpusBrowser_BUTTON="idCorpusBrowser";
    public static final String Statistics_BUTTON="idStatistics";
    public static final String Maintenance_BUTTON="idMaintenance";
    public static final String Export_BUTTON="idExport";
    public static final String Reindex_BUTTON="idReindex";
    public static final String Duplicate_BUTTON="idClone";
    public static final String Remove_BUTTON="idRemove";
    
    // Define New TM
    public static final String Name_TEXT_FIELD = "name";
    public static final String Domain_TEXT_FIELD = "domain";
    public static final String Organization_TEXT_FIELD = "organization";
    public static final String Description_TEXT_FIELD = "description";
    public static final String RemoteTM_CHECKBOX = "idRemoteTm";
    public static final String import_path = "//input[@id='idFilename']"; 
    public static final String import_name = "sourceTmName";
    public static final String import_format = "idTtmxRtf";

    public static final String Cancel_BUTTON = "Cancel";
    public static final String Save_BUTTON = "OK";
    
    
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
    public static final String FIND_TEXTFIELD="SourceFindText";
    public static final String InSourceLocale_SELECT="TmSourceSearchLocaleSelector";
    public static final String ShowTargetLocale_SELECT="TmTargetSearchLocaleSelector";
    public static final String Next_BUTTON="Search";
    public static final String Progress_MSG="idProgress";
    
    public static final String Continue_BUTTON="//input[@value='Continue']";
    public static final String Cancel_BUTTON_BOTTOM="CancelBtnBottom";
    public static final String Cancel_BUTTON_TOP="CancelBtnTop";
    
    //Reindex
    public static final String SelectedTM_RADIO="idSelectedTm";
    public static final String AllTms_RADIO="idAllTms";
    public static final String Next_BUTTON_REINDEX="//button[@onclick='doNext();']";
    public static final String ReindexMessages_MSG="idMessage";
    public static final String ReindexProgress_MSG="idProgress";
    //idProgressContainer
    public static final String OK_BUTTON_REINDEX="idCancelOk";
    
    //Duplicate Define New TM
    public static final String Name_TEXT_FIELD_DUPLICATE="name";
    public static final String Ok_BUTTON_DUPLICATE="OK";
    public static final String Cancel_BUTTON_DUPLICATE="Cancel";
    
    
    //Remove the TM
    public static final String EntrieTM_RADIO="checkboxTm";
    public static final String OK_BUTTON_REMOVE="//input[@value='OK']";
    public static final String RemoveMessages_MSG="idMessage";
    public static final String RemoveProgress_MSG="idProgress";
    public static final String OK_BUTTON_REMOVE2="idCancelOk";
    
    //Export TM
    public static final String Next_BUTTON_EXPORT="//button[@onclick='doNext();']";
    public static final String OK_BUTTON_EXPORT="idCancelOk";
    public static final String Download_File_BUTTON = "idRefreshResult";
    
    public static final String ExportMessages_MSG="idMessages";
    public static final String ExportProgress_MSG="idProgress";
    
}
