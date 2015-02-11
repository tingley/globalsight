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
package com.globalsight.cxe.adapter.database.target;

/**
 * DbColumnProxy, represents the data that must be written back to a single
 * column in the database, along with other such columns in the same record,
 * according to the rules of the put-back SQL.
 */
public class DbColumnProxy
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_name;
    private String m_tableName;
    private String m_content;

    /**
     * Creates a new instance of a writeable column.
     */
    public DbColumnProxy()
    {
        super();
        m_name = "";
        m_tableName = "";
        m_content = "";
    }

    /**
     * Return the value of the name field.
     * 
     * @return the current value.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Set the value of the name field.
     * 
     * @param p_str the new value.
     */
    public void setName(String p_str)
    {
        m_name = p_str;
    }

    /**
     * Return the value of the table name field.
     * 
     * @return the current value.
     */
    public String getTableName()
    {
        return m_tableName;
    }

    /**
     * Set the value of the table name field.
     * 
     * @param p_str the new value.
     */
    public void setTableName(String p_str)
    {
        m_tableName = p_str;
    }

    /**
     * Return the value of the content field.
     * 
     * @return the current value.
     */
    public String getContent()
    {
        return m_content;
    }

    /**
     * Set the value of the content field.
     * 
     * @param p_str the new value.
     */
    public void setContent(String p_str)
    {
        m_content = p_str;
    }

    /**
     * Return a string representation of the writeable column.
     *
     * @return a description of the receiver.
     */
    public String toString()
    {
        return ("DbColumnProxy [name=" + getName() +
                ", tableName=" + getTableName() +
                ", content=\"" + getContent() + "\"]");
    }
}

