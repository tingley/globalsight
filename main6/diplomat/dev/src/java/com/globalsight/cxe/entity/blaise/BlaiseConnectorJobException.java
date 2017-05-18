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
package com.globalsight.cxe.entity.blaise;

import com.globalsight.util.GeneralException;

public class BlaiseConnectorJobException extends GeneralException
{
    private static final long serialVersionUID = -659428742804702121L;

    final static String PROPERTY_FILE_NAME = "BlaiseConnectorJobException";

    public static final String EXCEPTION_UPLOAD = "UploadException";
    public static final String EXCEPTION_COMPLETE = "CompleteException";

    public BlaiseConnectorJobException(String p_key, String[] p_args, Exception p_exception)
    {
        super(p_key, p_args, p_exception, PROPERTY_FILE_NAME);
    }
}
