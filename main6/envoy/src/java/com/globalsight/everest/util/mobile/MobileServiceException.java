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

package com.globalsight.everest.util.mobile;

/**
* Simple exception class for the web services
*/
public class MobileServiceException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an empty exception
     */
    public MobileServiceException()
    {
    }

    /**
     * Constructs an exception with the given message.
     * 
     * @param p_message the reason for the exception
     */
    public MobileServiceException(String p_message)
    {
        super(p_message);
    }
    
    public MobileServiceException(String p_message, Exception ex)
    {
        super(p_message, ex);
    }
}

