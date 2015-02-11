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
package com.globalsight.util.gxml;

import com.globalsight.util.GeneralException;

/**
 * An exception handling object for GXML component.
 */


public class GxmlException 
    extends GeneralException
{

    // message file name
    private static final String PROPERTY_FILE_NAME = "GxmlException";
    
    // message keys
    public final static String MSG_FAILED_TO_GET_PARSER = "FailedToGetANewParser";
    public final static String MSG_FAILED_TO_GET_INPUTSOURCE = "FailedToReadTheInput";
    public final static String MSG_FAILED_TO_PARSE_GXML= "FailedToParseTheContent";
   
    /*
     *
     */
    public GxmlException(String p_messageKey, 
                         String[] p_messageArguments, 
                         Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }

}
