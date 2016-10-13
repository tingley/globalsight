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

package com.globalsight.everest.edit.offline;

/**
 *
 */
public interface AmbassadorDwUpExceptionConstants
{
    public static final int INVALID_FILE_FORMAT     = -404;
    public static final int INVALID_ENCODING        = -405;
    public static final int INVALID_FILE_NAME       = -406;
    public static final int INVALID_PDATA_INIT      = -407;
    public static final int INVALID_GXML            = -408;
    public static final int INVALID_PTAG            = -409;
    public static final int ERROR_PTAG_JOBGENERATOR = -410;

    public static final int WRITER_INVALID_PARAMETER  = -500;
    public static final int WRITER_IO_ERROR           = -501;
    public static final int WRITER_UNKNOWN_ENCODING   = -502;
    public static final int WRITER_RESOURCE_NOT_FOUND = -503;
    public static final int WRITER_FATAL_ERROR        = -504;
    public static final int WRITER_PTAG_PARSE_ERROR   = -505;

    public static final int READ_FATAL_ERROR          = -600;

    public static final int GENERAL_IO_WRITE_ERROR    = -700;
    public static final int GENERAL_IO_READ_ERROR     = -701;


    /**
     * RTFWriter: the file's locale could not be mapped to a Windows
     * LCID or the locale is not supported by RTF. Value -800.
     */
    public static final int RTF_LOCALE_NOT_SUPPORTED = -800;

    /**
     * RTFWriter: the default font for a locale or the fallback font
     * Arial is missing in the properties file. Value -801.
     */
    public static final int RTF_MISSING_FONTSPEC = -801;

    /**
     * RTFWriter: the encoding for the source language's default font
     * could not be found in the properties file. Value -802.
     */
    public static final int RTF_MISSING_FONT_ENCODING = -802;

    /**
     * RTFWriter: catchall when a required property other than a
     * fontspec is not found in the properties file. Value -803.
     */
    public static final int RTF_MISSING_RESOURCE = -803;


    /**
     * DownloadParam: One or more download paramters are invalid.
     */
    public static final int DOWNLOAD_INVALID_PARAM = -900;
}
