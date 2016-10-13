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
package com.globalsight.config;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * Exception class for System Parameter entity.
 */
public class UserParameterEntityException
    extends GeneralException
{
    /**
     * Constructor that wraps an existing exception with
     * UserParameterEntityException.
     *
     * @param p_exception Original exception object.
     */
    public UserParameterEntityException(Exception p_exception)
    {
        super(COMP_PERSISTENCE, EX_SQL, p_exception);
    }

    /**
     * Constructor that wraps an existing exception with
     * UserParameterEntityException.
     *
     * @param p_msg Exception message
     * @param p_exception Original exception object.
     */
    public UserParameterEntityException(String p_msg,
        Exception p_exception)
    {
        super(p_msg, p_exception);
    }
}
