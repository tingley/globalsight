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
package com.globalsight.cxe.adapter.database;

/**
 * Represents an association between PaginatedResultSetXml and EventFlowXml
 * for a particular grouping of individual tasks.
 */
public class TaskXml
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_prsXml;
    private String m_efXml;
    private long m_createTime;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Create an initialized instance.
     */
    public TaskXml()
    {
        m_prsXml = "";
        m_efXml = "";
        m_createTime = System.currentTimeMillis();
    }

    /**
     * Return the paginated result set xml string.
     *
     * @return the value of the string.
     */
    public String getPaginatedResultSetXml()
    {
        return m_prsXml;
    }

    /**
     * Set the value of the paginated result set xml.
     *
     * @param p_string the new value to be used.
     */
    public void setPaginatedResultSetXml(String p_string)
    {
        m_prsXml = p_string;
    }

    /**
     * Return the event flow xml string.
     *
     * @return the value of the string.
     */
    public String getEventFlowXml()
    {
        return m_efXml;
    }

    /**
     * Set the value of the event flow xml.
     *
     * @param p_string the new value to be used.
     */
    public void setEventFlowXml(String p_string)
    {
        m_efXml = p_string;
    }
    
    /**
     * Return the creation time in milliseconds since 1/1/1900.
     *
     * @return the creation time.
     */
    public long getCreationTime()
    {
        return m_createTime;
    }

    /**
     * Return a string representation of the TaskXml.
     */
    public String toString()
    {
        return ("TaskXml (" + 
                getCreationTime() + 
                ") [[PRS_XML=\"" + 
                getPaginatedResultSetXml() + 
                "\"] [EF_XML=\"" + 
                getEventFlowXml() + "\"]");
    }
}
