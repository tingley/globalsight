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
package com.globalsight.everest.edit.online;

import com.globalsight.util.gxml.GxmlElement;

import java.io.Serializable;

/**
 * <p>A data class that contains segment version information: the
 * segment/subflow content and task name.</p>
 */
public class SegmentVersion implements Serializable
{
    private String m_segment  = null;
    private String m_taskName = null;
    private String m_lastModifyUser = null;

    public SegmentVersion(String p_segment, String p_taskName)
    {
        m_segment  = p_segment;
        m_taskName = p_taskName;
    }

    public String getSegment()
    {
        return m_segment;
    }

    public String getTaskName()
    {
        return m_taskName;
    }
    
    public String getLastModifyUser()
    {
        return m_lastModifyUser;
    }
    
    public void setLastModifyUser(String user) {
        m_lastModifyUser =  user;
    }
}
