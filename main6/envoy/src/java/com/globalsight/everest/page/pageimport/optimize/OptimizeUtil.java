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

import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.tuv.TuvImpl;

public class OptimizeUtil 
{
	private static List<Optimizer> optimizers = new ArrayList<Optimizer>();
	static
	{
		optimizers.add(new OfficeOptimizer());
		optimizers.add(new OpenOfficeOptimizer());
		optimizers.add(new IdmlOptimizer());
		optimizers.add(new OfficeHtmlOptimizer());
		optimizers.add(new MifOptimizer());
		optimizers.add(new NormolOptimizer());
	}
	
    public void setGxml(TuvImpl tuv, String gxml, String tuDataType,
            String fileName, String pageDataType, long p_jobId)
	{
		for (Optimizer o : optimizers)
		{
            if (o.setGxml(tuv, gxml, tuDataType, fileName, pageDataType,
                    p_jobId))
			{
				break;
			}
		}
	}
}
