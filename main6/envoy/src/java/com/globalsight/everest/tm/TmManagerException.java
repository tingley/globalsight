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
package com.globalsight.everest.tm;

import com.globalsight.util.GeneralException;

/**
 * An exception thrown during the process of using the TmManager api.
 */
public class TmManagerException
    extends GeneralException
{
    public TmManagerException(Exception p_exception)
    {
        super(p_exception);
    }
    
    /**
     * Create a TmManagerException with the specified message.
     *
     * @param p_messageKey The key to the message located in the
     * property file.
     * @param p_messageArguments An array of arguments needed for the
     * message.  This can be null.
     * @param p_originalException The original exception that this one
     * is wrapping.  This can be null.
     */
    public TmManagerException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
