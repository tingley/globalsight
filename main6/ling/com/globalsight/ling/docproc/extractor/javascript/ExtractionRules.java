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
package com.globalsight.ling.docproc.extractor.javascript;

import com.globalsight.ling.docproc.extractor.html.DynamicRules;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;

import java.util.*;

/**
 * <P>Guides Javascript extraction by specifying rules which strings
 * to extract.
 *
 * <P>TODO.
 */
public class ExtractionRules
{
    /**
     * <p>Map that holds exceptional extraction rules.  If null, no
     * rules have been loaded. (Unused for now.)</p>
     */
    private DynamicRules m_rules = null;

    //
    // Constructor
    //
    public ExtractionRules()
    {
    }

    /**
     * <p>Loads rules to guide extraction process from a string.</p>
     */
    public final void loadRules(String p_rules)
        throws ExtractorException
    {
        // not implemented yet
    }

    /**
     * <p>Loads rules to guide extraction process from an object.</p>
     */
    public final void loadRules(Object p_rules)
        throws ExtractorException
    {
        if (p_rules != null && p_rules instanceof DynamicRules)
        {
            m_rules = (DynamicRules)p_rules;
        }
    }
}
