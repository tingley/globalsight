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

package com.globalsight.everest.tuv;

import com.globalsight.everest.persistence.PersistentObject;

public class RemovedPrefixTag extends PersistentObject
{
    private static final long serialVersionUID = 8394101654198460932L;
    private TuImpl tu;
    private long tuId;
    private String string;

    public void setTu(TuImpl p_tu)
    {
        tuId = p_tu.getId();
        tu = p_tu;
        if (tu != null)
        {
            tuId = tu.getId();
        }
    }

    public long getTuId()
    {
        return tuId;
    }

    public void setTuId(long tuId)
    {
        this.tuId = tuId;
    }

    public String getString()
    {
        return string;
    }

    public void setString(String string)
    {
        this.string = string;
    }
}
