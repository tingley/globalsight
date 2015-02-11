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

import com.globalsight.everest.tuv.TuvImpl;

public class IdmlOptimizer extends Optimizer 
{

	@Override
	protected boolean accept(String tuDataType, String fileName,
			String pageDataType) 
	{
		int index = fileName.lastIndexOf('.');
        if (index < 0)
            return false;

        String type = fileName.substring(index);
        return ".idml".equalsIgnoreCase(type);
	}

	@Override
	protected void setGxml(TuvImpl tuv, String gxml, long p_jobId) 
	{
        if (gxml != null)
        {
            gxml = mergeOneBpt(gxml);
            gxml = removeTags(tuv, gxml, p_jobId);
            gxml = mergeMultiTags(gxml);
            gxml = mergePh(gxml);

            gxml = removeAllPrefixAndSuffixTags(tuv, gxml, p_jobId);
        }

        tuv.setGxml(gxml);
	}
}
