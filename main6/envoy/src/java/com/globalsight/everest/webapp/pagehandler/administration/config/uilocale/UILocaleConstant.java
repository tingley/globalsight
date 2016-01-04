/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.administration.config.uilocale;

/**
 * This class defines common constants used across classes dealing with ui
 * locale.
 */
public class UILocaleConstant
{

    public static final String ADD = "add";
    public static final String SETDEFAULT = "setdefault";
    public static final String REMOVE = "remove";
    public static final String DOWNLOAD_RES = "downloadres";
    public static final String UPLOAD_RES = "uploadres";
    public static final String PREVIOUS = "previous";
    public static final String SAVE = "save";
    public static final String CANCEL = "cancel";
    public static final String UPLOAD = "upload";

    public static final String UILOCALES = "uilocalesForConfig";
    public static final String UILOCALE_KEY = "uilocalekeyForConfig";
    public static final String UILOCALE_DEFAULT = "uilocaledefaultForConfig";
    public static final String AVAILABLE_UILOCALES = "AVAILABLEuILOCALESForAdd";

    public static final String UPLOAD_RESULT = "uploadresult";
    public static final String UPLOAD_MSG = "uploadmsg";
    public static final String ENCODING = "fileencoding";
    public static final String LAST_ENCODING = "lastencodingentered";

    public static final String[] PROPERTIES_FILE_NAMES_EARLIB = {
            "AdapterCommonMsg", "AdobeApplicationAdapterMsg",
            "AmbassadorDwUpException", "CalendarManagerException",
            "CapAdapterMsg", "CatalystAdapterMsg", "CmsUserManagerException",
            "CommentException", "CopyFlowAdapterMsg", "CostingException",
            "DatabaseAdapterMsg", "DBConnectionPersistenceManager",
            "DependencyCheckException", "DesktopApplicationAdapterMsg",
            "DiplomatMergerException", "DocumentumPersistenceManagerException",
            "EditorMatchTypeLabels", "EnvoyServletException",
            "EventSchedulerException", "ExceptionResource",
            "ExportEventObserverException", "FileExtensionEntityException",
            "FileImportException", "FileProfileEntityException",
            "FileProfilePersistenceManager", "FileSystemAdapterMsg",
            "GlossaryException", "GxmlException", "ImporterException",
            "InddAdapterMsg", "JobCreationException", "JobException",
            "LingAdapterMsg", "LingManagerException", "LocaleManagerException",
            "LocaleResource", "MailerException", "MediasurfaceAdapterMsg",
            "MicrosoftApplicationAdapterMsg",
            "MultipartFormDataReaderException", "NativeFileManagerException",
            "OfflineEditorManagerException",
            "OfflinePageDataGeneratorException", "OnlineEditorException",
            "PageException", "PageExportException", "PageSegmentsException",
            "PassoloAdapterMsg", "PdfAdapterMsg", "PersistenceException",
            "ProjectHandlerException", "ReimporterException",
            "RequestHandlerException", "SecondaryTargetFileException",
            "SecurityManagerException", "SnippetException",
            "StandardExtractorAgent", "StandardMergerAgent",
            "StatisticsException", "SystemParameterEntityException",
            "SystemStartupException", "TaskException",
            "TemplateException",
            "TermbaseException", "TmManagerException",
            "UploadPageSaverException", "UserManagerException",
            "VendorException", "VignetteAdapterMsg", "WindowsPEAdapterMsg",
            "WorkflowException", "WorkflowManagerException",
            "EmailMessageResource" };

    public static final String[] PROPERTIES_FILE_NAMES_WARREPORT = {
            "avgPercentCompletion", "common", "costing", "costsByLocaleReport",
            "jobDetails", "jobTable", "missingTerms", "showReports",
            "taskDurationReport", "termAudit", "tmReport", "workflowStatus",
            "workflowTable" };
}
