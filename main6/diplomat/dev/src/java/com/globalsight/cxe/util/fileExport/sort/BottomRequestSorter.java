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
package com.globalsight.cxe.util.fileExport.sort;

import java.util.Hashtable;

public class BottomRequestSorter extends RequestSorter
{
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Hashtable getNextMessage(int i)
    {
        Hashtable p = ms.get(ms.size() - 1);
        Hashtable p2 = new Hashtable();
        
        // update the sort time, sort priority and sort axis.
        p2.put("sortTime", (Long)(p.get("sortTime")) + 1);
        p2.put("sortPriority", p.get("sortPriority"));
        p2.put("sortAxis", 1);
        
        return p2;
    }

    @Override
    protected boolean isUp()
    {
        return false;
    }

}
