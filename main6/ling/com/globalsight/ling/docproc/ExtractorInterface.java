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

import com.globalsight.cxe.entity.filterconfiguration.Filter;

public interface ExtractorInterface
{
    public void extract()
        throws ExtractorException;

    /**
     * Set references to the input and output objects.
     * @param p_input com.globalsight.ling.docproc.EFInputData
     * @param p_output com.globalsight.ling.docproc.Output
     */
    public void init(EFInputData p_input, Output p_output)
        throws ExtractorException;

    public void loadRules()
        throws ExtractorException;

    /** Provide an alternate way to load rules*/
    public void loadRules(String p_rules)
        throws ExtractorException;
    
    /**
     * Set the filter for this extractor
     * @param p_filter
     * @throws ExtractorException
     */
    public void setMainFilter(Filter p_filter);
}

