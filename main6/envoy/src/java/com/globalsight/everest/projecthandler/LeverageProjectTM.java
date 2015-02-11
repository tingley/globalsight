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
package com.globalsight.everest.projecthandler;

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 * 
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF GLOBALSIGHT
 * CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT IN CONFIDENCE.
 * INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED OR DISCLOSED IN WHOLE OR
 * IN PART EXCEPT AS PERMITTED BY WRITTEN AGREEMENT SIGNED BY AN OFFICER OF
 * GLOBALSIGHT CORPORATION.
 * 
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER SECTIONS 104
 * AND 408 OF TITLE 17 OF THE UNITED STATES CODE. UNAUTHORIZED USE, COPYING OR
 * OTHER REPRODUCTION IS PROHIBITED BY LAW.
 */
import com.globalsight.everest.persistence.PersistentObject;

public class LeverageProjectTM extends PersistentObject
{
    private static final long serialVersionUID = 2132890189336723368L;
    private long m_projectTmId = -1;
    private TranslationMemoryProfile tmProfile = null;
    private int m_projectTmIndex = -1;

    public LeverageProjectTM()
    {
    }

    public void setProjectTmId(long p_projectTmId)
    {
        m_projectTmId = p_projectTmId;
    }

    public long getProjectTmId()
    {
        return m_projectTmId;
    }

    public void setTMProfile(TranslationMemoryProfile p_tmProfile)
    {
        tmProfile = p_tmProfile;
    }

    public TranslationMemoryProfile getTMProfile()
    {
        return tmProfile;
    }

    public void setProjectTmIndex(int projectTmIndex)
    {
        m_projectTmIndex = projectTmIndex;
    }

    public int getProjectTmIndex()
    {
        return m_projectTmIndex;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LeverageProjectTM other = (LeverageProjectTM) obj;
        if (m_projectTmId != other.m_projectTmId)
            return false;
        if (m_projectTmIndex != other.m_projectTmIndex)
            return false;
        if (tmProfile == null)
        {
            if (other.tmProfile != null)
            {
                return false;
            }
        }
        else if (other.tmProfile == null)
        {
            return false;
        }
        else if (tmProfile.getId() != other.tmProfile.getId())
        {
            return false;
        }
            
        return true;
    }
}
