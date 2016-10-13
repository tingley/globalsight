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
package com.globalsight.ling.docproc.extractor.msoffice;

import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.extractor.html.DynamicRules;

import java.util.*;

/**
 * Dynamic extraction rules that overwrite static rules loaded from
 * property files (Tags, Styles).
 */
public class DynamicWordRules
    extends DynamicRules
{
    //
    // Constructor: overwrites default rules in base class
    //

	public DynamicWordRules()
    {
        loadProperties("properties/WordExtractor");
    }

	public void loadRulesFromFilter(long filterId)
	{
		if (filterId > 0)
		{
			MSOfficeDocFilter filter = null;
			try
			{
				filter = (MSOfficeDocFilter)FilterHelper.getFilter(FilterConstants.MSOFFICEDOC_TABLENAME, filterId);
			}
			catch(Exception e)
			{
				throw new ExtractorException(e);
			}
			
			if (filter != null)
			{
				String unextractableWordParaStyles = filter.getUnextractableWordParagraphStyles(); 
				String unextractableWordCharStyles = filter.getUnextractableWordCharacterStyles();
				String selectedInternalTextStyles = filter.getSelectedInternalTextStyles();
				clearAndFillWordStyleMapWithFilters(unextractableWordParaStyles, m_unextractableWordParaStyles, false);
				clearAndFillWordStyleMapWithFilters(unextractableWordCharStyles, m_unextractableWordCharStyles, false);
				clearAndFillWordStyleMapWithFilters(selectedInternalTextStyles, m_selectedInternalTextStyles, true);
			}
		}
	}
	
    static protected final void clearAndFillWordStyleMapWithFilters(String value, HashMap map, boolean enable)
    {
    	map.clear();
        StringTokenizer tok = new StringTokenizer (value, ",");
        while (tok.hasMoreTokens())
        {
            String style = tok.nextToken().trim();
            style = normalizeWordStyle(style);
            map.put(style, Boolean.valueOf(enable));
        }
    }
}
