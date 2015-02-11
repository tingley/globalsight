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
package com.globalsight.everest.tm.searchreplace;

import java.io.Serializable;
import java.util.Collection;

public class TaskInfo
    extends JobInfo
    implements Serializable
{
    private long m_id;
    private String m_name;

    public TaskInfo()
    {
    }

    public void setTaskId(long p_id)
    {
        m_id = p_id;
    }

    public void setTaskName(String p_name)
    {
        m_name = p_name;
    }

    public long getTaskId()
    {
        return m_id;
    }

    public String getTaskName()
    {
        return m_name;
    }
}
