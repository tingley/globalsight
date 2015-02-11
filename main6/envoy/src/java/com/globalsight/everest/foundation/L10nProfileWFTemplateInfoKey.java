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
package com.globalsight.everest.foundation;

import java.io.Serializable;

public class L10nProfileWFTemplateInfoKey implements Serializable
{
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private long l10nProfileId;
    private long wfTemplateId;
    private long mtProfileId;

    public long getMtProfileId()
    {
        return mtProfileId;
    }

    public void setMtProfileId(long mtProfileId)
    {
        this.mtProfileId = mtProfileId;
    }


    public long getL10nProfileId()
    {
        return l10nProfileId;
    }

    public void setL10nProfileId(long profileId)
    {
        l10nProfileId = profileId;
    }

    public long getWfTemplateId()
    {
        return wfTemplateId;
    }

    public void setWfTemplateId(long wfTemplateId)
    {
        this.wfTemplateId = wfTemplateId;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object paramObject)
    {
        return super.equals(paramObject);
    }
}
