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



/**
 * This class contains the attributes required for ProjectUpdateListener.
 */
public class ProjectUpdateMessage
    implements java.io.Serializable
{
    private String m_modifier = null;
    private String m_previousPm = null;
    private String m_currentPm = null;
    private Long m_projectId = null;
    
    
    //////////////////////////////////////////////////////////////////////
    //  Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Construct a project update message that will be passed to the JMS listener.
     * @param p_modifier - The project modifier's username.
     * @param p_previousPm - The username of the project's previous PM.
     * @param p_currentPm - The username of the project's current PM.
     * @param p_projectId - The id of the modified project.
     */
    public ProjectUpdateMessage(String p_modifier, String p_previousPm,
                                String p_currentPm, Long p_projectId)
    {
        m_modifier = p_modifier;
        m_previousPm = p_previousPm;
        m_currentPm = p_currentPm;
        m_projectId = p_projectId;
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Local Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the username of the project's modifier.
     */
    public String getProjectModifier()
    {
        return m_modifier;
    }

    /**
     * Get the username of the project's previous PM.
     */
    public String getPreviousProjectManager()
    {
        return m_previousPm;
    }

    /**
     * Get the username of the project's current PM.
     */
    public String getCurrentProjectManager()
    {
        return m_currentPm;
    }

    /**
     * Get the project's id as long.
     */
    public Long getProjectId()
    {
        return m_projectId;
    }
}
