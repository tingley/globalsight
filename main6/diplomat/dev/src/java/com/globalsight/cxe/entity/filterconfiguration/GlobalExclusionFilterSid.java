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
package com.globalsight.cxe.entity.filterconfiguration;

import net.sf.json.JSONObject;

public class GlobalExclusionFilterSid
{
    private String sid;
    private boolean sidIsRegEx = false;
    
    public GlobalExclusionFilterSid(JSONObject ob)
    {
        sid = ob.getString("sid");
        sidIsRegEx = ob.getBoolean("sidIsRegEx");
    }
    
    public String getSid()
    {
        return sid;
    }
    
    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public boolean isSidIsRegEx()
    {
        return sidIsRegEx;
    }

    public void setSidIsRegEx(boolean sidIsRegEx)
    {
        this.sidIsRegEx = sidIsRegEx;
    }
}
