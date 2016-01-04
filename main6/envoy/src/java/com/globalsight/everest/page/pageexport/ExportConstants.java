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
package com.globalsight.everest.page.pageexport;

/**
 * This class contains the export related constants that are used
 * by classes responsible for export process and CAP's export servlet.
 */
public class ExportConstants
{
    /**
     * Constant used for identifying UTF8 code set.
     */
    public static final String UTF8 = "UTF8";

    /**
     * Constant used for identifying ISO Latin code set.
     */
    public static final String ISO_LATIN = "ISO8859_1";

    /**
     * Constant identifying a default segment.  This constant is using during
     * the page template population for a dynamic preview.  All of the segment
     * place holders that are not part of the preview segment(s) will be
     * replaced by this constant.
     */
    public static final String DEFAULT_SEGMENT = "GLOBALSIGHT_UNKNOWN_SEGMENT";

    /**
     * Parameter value used to set an unknown ABSOLUTE_EXPORT_PATH. If a given
     * adapter does not provide an ABSOLUTE_EXPORT_PATH parameter, this value
     * will be used to set the ABSOLUTE_EXPORT_PATH parameter (see cap uploader).
     */
    public static final String ABSOLUTE_EXPORT_PATH_UNKNOWN = "ExportAbsolutePathUnknown";


    //////////////////////////////////////////////////////////////////////
    //  Begin: Export parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used for identifying a CXE request type parameter.
     */
    public static final String CXE_REQUEST_TYPE = "CxeRequestType";

    /**
     * Constant used for identifying the original request of CXE from CAP
     */
    public static final String ORIG_CXE_REQUEST_TYPE = "OriginalCxeRequestType";


    /**
     * Constant used for identifying a Data source type
     */
    public static final String DATA_SOURCE_TYPE = "DataSourceType";

    /**
     * Constant used for identifying a Data source type for Mediasurface
     */
    public static final String MEDIASURFACE = "mediasurface";


    /**
     * Constant used for identifying an EventFlowXML parameter.
     */
    public static final String EVENT_FLOW_XML = "EventFlowXML";

    /**
     * Constant used for identifying an export location parameter.
     */
    public static final String EXPORT_LOCATION = "ExportLocation";

    /**
     * Constant used for identifying the absolute export path parameter
     * It's values should be either ABSOLUTE_EXPORT_PATH_UNKNOWN or a valid path.
     */
    public static final String ABSOLUTE_EXPORT_PATH = "AbsoluteExportPath";

    /**
     * Constant used for identifying the Sub Component parameter flag
     * It's value should be either "true" or "false"
     */
    public static final String IS_COMPONENT_PAGE = "IsComponentPage";


    /**
     * Temp path where a copy of the exported file was written to
     */
    public static final String TEMP_EXPORT_PATH = "tempExportPath";

    /**
     * Constant used for identifying a locale specific subdir
     */
    public static final String LOCALE_SUBDIR = "LocaleSubDir";

    /**
     * Constant used for identifying a GXML formatted page parameter.
     */
    public static final String GXML = "Gxml";
    /**
     * Constant used for identifying a message id parameter.
     */
    public static final String MESSAGE_ID = "MessageId";

    /**
     * The total number of pages in this export batch.
     */
    public static final String PAGE_COUNT = "PgCnt";

    /**
     * The page number of this page in the export batch
     */
    public static final String PAGE_NUM = "PgNum";

    /**
     * The export batch ID
     */
    public static final String EXPORT_BATCH_ID = "ExpBatchId";

    /**
     * The time that the file was written back to the source repository
     */
    public static final String EXPORTED_TIME = "ExportedTime";

    /**
     * Constant used for identifying response details parameter.
     */
    public static final String RESPONSE_DETAILS = "ResponseDetails";

    /**
     * Constant used for identifying response type (i.e. success/failure)
     * parameter.
     */
    public static final String RESPONSE_TYPE = "ResponseType";

    /**
     * Constant used for identifying a target code set parameter.
     */
    public static final String TARGET_CODESET = "TargetCodeset";

    /**
     * Constant used for identifying a target locale parameter.
     */
    public static final String TARGET_LOCALE = "TargetLocale";

    public static final String BOM_TYPE = "bomType";
    public static final int NO_UTF_BOM = 0;
    public static final int UTF_BOM_PRESERVE = 1;
    public static final int UTF_BOM_ADD = 2;
    public static final int UTF_BOM_REMOVE = 3;
    public static final int NOT_SELECTED = -1;
    
    public static final int UTF8_WITH_BOM = 1;
    public static final int UTF16_LE = 2;
    public static final int UTF16_BE = 3;

    /**
     * Constant used for identifying a Tuv id parameter which is used
     * during a dynamic preview.
     */
    public static final String TUV_ID = "TuvId";

    /**
     * Constant used as a parameter for identifying UI locale for a
     * dynamic preview.
     */
    public static final String UI_LOCALE = "UiLocale";

    /**
     * Constant used as a parameter for a replaced image file name.
     */
    public static final String IMAGE_FILENAME = "ImageFilename";

    /**
     * Constant used as a parameter for a replaced image data.
     */
    public static final String IMAGE_DATA = "ImageData";


    //////////////////////////////////////////////////////////////////////
    //  End: Export parameters
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Types for export parameters
    //////////////////////////////////////////////////////////////////////

    /**
     * Constant used for identifying an automatic export type (this
     * is basically one of the values of CXE_REQUEST_TYPE).
     */
    public static final String AUTOMATIC_EXPORT = "1";

    /**
     * Constant used for identifying a manual export type (this
     * is basically one of the values of CXE_REQUEST_TYPE).
     */
    public static final String MANUAL_EXPORT = "2";

    /**
     * Constant identifying a preview request type.
     * This is basically one of the values of CxeRequestType (this
     * is basically one of the values of CXE_REQUEST_TYPE).
     */
    public static final String PREVIEW = "3";

    /**
     * Constant identifying an export for update request type.
     * This is basically one of the values of CxeRequestType (this
     * is basically one of the values of CXE_REQUEST_TYPE).
     */
    public static final String EXPORT_FOR_UPDATE = "4";

    /**
     * Constant identifying an export for creation of secondary
     * target files.
     */
    public static final String EXPORT_FOR_STF_CREATION = "5";

    /**
     * Constant identifying a secondary target file export.
     */
    public static final String EXPORT_STF = "6";

    /**
     * Constant representing the directory type as language
     * based (this is basically one of the values of EXPORT_DIRECTORY).
     */
    public static final String LANGUAGE_DIRECTORY = "LANGUAGE";

    /**
     * Constant representing the directory type as locale
     * based (this is basically one of the values of EXPORT_DIRECTORY).
     */
    public static final String LOCALE_DIRECTORY = "LOCALE";
    
    /**
     * Constant representing the directory type as export
     * based (this is basically one of the values of EXPORT_DIRECTORY).
     */
    public static final String EXPORT_DIRECTORY = "EXPORT";

    /**
     * Constant identifying a successful action (valid result).  This
     * value is used as a valid value for a RESPONSE_TYPE.
     */
    public static final String SUCCESS = "1";

    /**
     * Constant identifying a failure (invalid result).  This
     * value is used as a valid value for a RESPONSE_TYPE.
     */
    public static final String FAILURE = "2";

    //////////////////////////////////////////////////////////////////////
    //  End: Types for export parameters
    //////////////////////////////////////////////////////////////////////
}
