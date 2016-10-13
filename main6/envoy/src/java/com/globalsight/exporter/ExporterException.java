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

package com.globalsight.exporter;

import com.globalsight.util.GeneralException;


/**
 * <p>The Exporter Exception class.</p>
 */
public class ExporterException
    extends GeneralException
{
    /**
     * Exporter related messages are stored in the following
     * property file.
     */
    private final static String PROPERTY_FILE_NAME = "ExporterException";

    // message keys

    public final static String MSG_XML_ERROR = "xmlErrorMessage";
    public final static String MSG_NO_READER_TYPE = "noReaderTypeSpecified";
    // Args: 1 - the type
    //       2 - the importer that doesn't support the type
    public final static String READER_TYPE_NOT_SUPPORTED = "readerNotSupported";

    // Args: 1 - reason as string
    public final static String MSG_EXPORT_FAILED = "importFailed";
    // Args: 1 - reason as string
    public final static String MSG_INVALID_EXPORT_OPTIONS
        = "invalidExportOptions";

    /**
     * @see GeneralException#GeneralException(String,String[],Exception,String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. Can be null.
     * @param p_originalException Original exception that caused the
     * error.  Can be null.
     * @param p_propertyFileName Property file base name. If the
     * property file is LingMessage.properties, the parameter should
     * be "LingMessage".
     */
    public ExporterException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }
}
