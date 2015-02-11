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

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import com.globalsight.ling.docproc.extractor.html.Extractor;
import com.globalsight.ling.docproc.extractor.html.DynamicRules;
import com.globalsight.ling.docproc.extractor.html.ExtractionRules;

import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;

public class WordExtractor
    extends Extractor
{
    public WordExtractor()
    {
        super();
    }

    //
    // Overwrite Abstract Methods
    //
    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_WORD_HTML);
    }

    public void loadRules()
        throws ExtractorException
    {
    	DynamicWordRules dynamicRules = new DynamicWordRules();
        setDynamicRules(dynamicRules);
        super.loadRules();
        
        long fliterId = getMSOfficeDocFilterId();
		if (fliterId > 0)
		{
			dynamicRules.loadRulesFromFilter(fliterId);
		}
    }
}
