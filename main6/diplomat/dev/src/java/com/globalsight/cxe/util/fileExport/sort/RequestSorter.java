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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.globalsight.cxe.util.fileExport.FileExportUtil;

public abstract class RequestSorter
{
    @SuppressWarnings("rawtypes")
    protected List<Hashtable> ms;
    
    /**
     * The keys of selected import messages. Should be used in
     * <code>getNextMessage()</code> method.
     */
    protected List<String> keys;

    /**
     * Gets the index of the key in the list.
     * 
     * @param key
     * @param ms
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static int getIndex(String key, List<Hashtable> ms)
    {
        int i = 0;
        for (i = 0; i < ms.size(); i++)
        {
            Hashtable p = ms.get(i);
            String key2 = (String) p.get("uiKey");

            if (key2.equals(key))
                break;
        }

        if (i >= ms.size())
            i = -1;
        
        return i;
    }

    /**
     * Gets the next message that change the location.
     * 
     * @param i
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected abstract Hashtable getNextMessage(int i);
    
    protected abstract boolean isUp();

    /**
     * Sorts the waiting request's priority.
     * 
     * @param key
     * @param sortPriority
     * @param sortTime
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void sortWaitingPriority(String[] keys)
    {
        if (keys == null || keys.length == 0)
            return;

        this.keys = new ArrayList<String>();
        for (String key : keys)
        {
            this.keys.add(key);
        }
        
        synchronized (FileExportUtil.LOCKER)
        {
            for (String key : keys)
            {
                Hashtable m = FileExportUtil.WAITING_REQUEST.get(key);
                if (m == null)
                    return;

                String name = FileExportUtil.getName(m);
                ms = FileExportUtil.ON_HOLD_MESSAGE.get(name);
                
                if (ms.size() < 2)
                    continue;

                int i = getIndex(key, ms);
                
                if (i < 0)
                    continue;
                
                Hashtable p2 = getNextMessage(i);
                if (p2 == null)
                    continue;
                

                // update the sort time, sort priority and sort axis.
                m.put("sortTime", p2.get("sortTime"));
                m.put("sortPriority", p2.get("sortPriority"));
                
                int axis = (Integer) (p2.get("sortAxis"));
                m.put("sortAxis", axis);
                
                int newAxis = isUp() ? axis + 1 : axis - 1;
                p2.put("sortAxis", newAxis);
            }
            
            FileExportUtil.sortWaitingMessage();
        }
    }
}
