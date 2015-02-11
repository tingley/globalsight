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

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

public class AmbassadorDwUpException
    extends GeneralException
{
    /**
     * Constructor.
     * @param p_originalException
     */
    public AmbassadorDwUpException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * Constructor.
     * @param p_exceptionId Reason for the exception.
     * @param p_originalException
     */
    public AmbassadorDwUpException(int p_exceptionId,
        Exception p_originalException)
    {
        super(GeneralExceptionConstants.COMP_LING,
            p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance using the given component and exception
     * identification.
     *
     * @param p_exceptionId Reason for the exception.
     * @param p_message Explanation of the exception.
     */
    public AmbassadorDwUpException(int p_exceptionId, String p_message)
    {
        super(GeneralExceptionConstants.COMP_LING, p_exceptionId, p_message);
    }

    public AmbassadorDwUpException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
