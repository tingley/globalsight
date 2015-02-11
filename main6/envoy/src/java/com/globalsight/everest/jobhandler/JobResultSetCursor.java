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
package com.globalsight.everest.jobhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class JobResultSetCursor
{
    List m_sc;
    int m_absolute;
    int m_relative;
    int m_first;
    int m_last;
    int m_previous;
    int m_next;
    int m_size;
    List m_jobList;

    public JobResultSetCursor(int p_size)
    {
        m_size = p_size;
        m_jobList = new Vector();

    }

    public List getJobCollection()
    {
        if (m_sc.size() > 0)
        {
            int n = m_size > m_sc.size() ? m_sc.size() : m_size;
            m_jobList = m_sc.subList(0, n);
            m_sc = m_sc.subList(n, m_sc.size());
        }
        return m_jobList;
    }

    public void setScrollableCursor(Collection p_sc)
    {
        m_sc = new ArrayList(p_sc);
    }

    public void setScrollableCursorSize(int p_size)
    {
        m_size = p_size;
    }

    public boolean isLast()
    {
        return m_sc.size() == 0;
    }
}
