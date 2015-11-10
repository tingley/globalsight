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

public class UpRequestSorter extends RequestSorter
{

    @SuppressWarnings("rawtypes")
    @Override
    protected Hashtable getNextMessage(int i)
    {
        i--;

        // ignore the message that has been updated.
        while (i > -1)
        {
            if (!keys.contains(ms.get(i).get("uiKey")))
                break;

            i--;
        }
        
        if (i < 0)
            return null;

        return ms.get(i);
    }

    @Override
    protected boolean isUp()
    {
        return true;
    }

}
