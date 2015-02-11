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
package com.globalsight.everest.webapp.pagehandler.edit.online;

import com.globalsight.everest.persistence.PersistentObject;

public class SegmentKey extends PersistentObject
{
    private static final long serialVersionUID = 8118018563765842409L;

    private String previousKey = null;
    private String currentKey = null;
    private String nextKey = null;
    
    public SegmentKey(String p_currentKey)
    {
        this.currentKey = p_currentKey;
    }
    
    public SegmentKey(String p_previousKey, String p_currentKey,
            String p_nextKey)
    {
        this.previousKey = p_previousKey;
        this.currentKey = p_currentKey;
        this.nextKey = p_nextKey;
    }
    
    public String getPreviousKey()
    {
        return previousKey;
    }
    
    public void setPreviousKey(String previousKey)
    {
        this.previousKey = previousKey;
    }
    
    public String getCurrentKey()
    {
        return currentKey;
    }
    public void setCurrentKey(String currentKey)
    {
        this.currentKey = currentKey;
    }
    
    public String getNextKey()
    {
        return nextKey;
    }
    
    public void setNextKey(String nextKey)
    {
        this.nextKey = nextKey;
    }
    
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof SegmentKey)
        {
            SegmentKey segKey = (SegmentKey) obj;
            String currentKey = segKey.getCurrentKey();
            if (currentKey != null && currentKey.equals(this.currentKey))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public int hashCode()
    {
        return 0;
    }
    
    public String toString()
    {
        return "CurrentKey : " + currentKey + "; PreviousKey : " + previousKey
                + "; NextKey : " + nextKey;
    }
    
}
