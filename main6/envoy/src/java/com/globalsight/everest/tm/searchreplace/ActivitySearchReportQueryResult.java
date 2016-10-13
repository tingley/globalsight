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
import java.util.List;

public class ActivitySearchReportQueryResult implements Serializable
{
    private static final long serialVersionUID = 4284318774443005429L;

    private List<TaskInfo> m_taskInfos;

    public ActivitySearchReportQueryResult(List<TaskInfo> p_taskInfos)
    {
        m_taskInfos = p_taskInfos;
    }

    public void setTaskInfos(List<TaskInfo> p_taskInfos)
    {
        m_taskInfos = p_taskInfos;
    }

    public List<TaskInfo> getTaskInfos()
    {
        return m_taskInfos;
    }
}
