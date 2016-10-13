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
package com.globalsight.ling.docproc;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

public class ExtractorException
    extends GeneralException
{
    public ExtractorException(int p_exceptionId)
    {
        super(GeneralExceptionConstants.COMP_EXTRACTOR, p_exceptionId);
    }

    public ExtractorException(int p_exceptionId,
        Exception p_originalException)
    {
        super(GeneralExceptionConstants.COMP_EXTRACTOR,
            p_exceptionId, p_originalException);
    }

    public ExtractorException(int p_exceptionId, String p_message)
    {
        super(GeneralExceptionConstants.COMP_EXTRACTOR,
            p_exceptionId, p_message);
    }

    public ExtractorException(Exception p_originalException)
    {
        super(p_originalException);
    }
    
    public String toString()
    {
        // Insert code to print the receiver here.  This implementation
        // forwards the message to super. You may replace or supplement
        // this.
        return super.toString();
    }
}
