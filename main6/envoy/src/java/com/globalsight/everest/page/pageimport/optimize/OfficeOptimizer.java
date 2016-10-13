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
package com.globalsight.everest.page.pageimport.optimize;

import com.globalsight.everest.page.pageimport.DocsTagUtil;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.docproc.IFormatNames;

public class OfficeOptimizer extends Optimizer 
{

	@Override
	protected boolean accept(String tuDataType, String fileName,
			String pageDataType) 
	{
        if (tuDataType == null)
            return false;

		return IFormatNames.FORMAT_OFFICE_XML.equals(tuDataType);
	}

	@Override
	protected void setGxml(TuvImpl tuv, String gxml, long p_jobId) 
	{
        if (gxml != null)
        {
            gxml = removeTagForSpace(gxml);
            gxml = mergeOneBpt(gxml);
            gxml = removeTags(tuv, gxml, p_jobId);
            gxml = mergeMultiTags(gxml);
            gxml = removeAllPrefixAndSuffixTags(tuv, gxml, p_jobId);
        }

        tuv.setGxml(gxml);
//    remove style tag for office 1.0
//        tuv = DocsTagUtil.handleSpecialTag(tuv, p_jobId);
	}
}
