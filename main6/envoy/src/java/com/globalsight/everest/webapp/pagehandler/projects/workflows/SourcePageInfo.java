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


package com.globalsight.everest.webapp.pagehandler.projects.workflows;

public class SourcePageInfo
{

    private long m_id;
    private String m_pageName;
    private int m_wordCount;
    private int m_overridenWordCount;
    private boolean m_isWordCountOverriden = false;

    public SourcePageInfo(long p_id, String p_pageName, int p_wordCount)
    {
        m_id = p_id;
        m_pageName = p_pageName;
        m_wordCount = p_wordCount;
    }

    public SourcePageInfo(long p_id, String p_pageName, 
                          int p_wordCount, boolean p_overrideWordCount)
    {
        m_id = p_id;
        m_pageName = p_pageName;
        m_isWordCountOverriden = p_overrideWordCount;
        if (m_isWordCountOverriden)
        {
            m_overridenWordCount = p_wordCount;
        }
        else
        {
            m_wordCount = p_wordCount;
        }
    }   

    public void overrideWordCount(int p_overridenWordCount)
    {
        m_overridenWordCount = p_overridenWordCount;
        m_isWordCountOverriden = true;
    }

    public void clearWordCountOverride()
    {
        m_isWordCountOverriden = false;
    }

    public long getId()
    {
        return m_id;
    }

    public String getPageName()
    {
        return m_pageName;
    }

    public int getWordCount()
    {
        if (isWordCountOverriden())
        {
            return m_overridenWordCount;
        }
        else
        {
            return m_wordCount;
        }
    }

    public boolean isWordCountOverriden()
    {
        return m_isWordCountOverriden;
    }                  
}

