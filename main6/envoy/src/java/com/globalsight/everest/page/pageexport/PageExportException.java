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

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * Exception class for PageExport Listener.
 */
public class PageExportException
    extends GeneralException
    implements GeneralExceptionConstants
{

    // message keys - for new GeneralException
    public final static String MSG_FAILED_TO_EXPORT_PAGE =
        "FailedToExportPage";

    // message file name
    private static final String PROPERTY_FILE_NAME = "PageExportException";

    /**
     * Constructs a PageExportException using the messageKey,
     * arguments, and wrapped exception
     */
    public PageExportException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }
}

