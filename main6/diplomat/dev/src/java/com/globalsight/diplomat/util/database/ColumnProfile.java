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
package com.globalsight.diplomat.util.database;

import java.util.Vector;

/**
 * ColumnProfile, represents the metadata for a column in a result set.
 * The column profile describes important things about a particular column
 * in the result set.  Although declared a public class, this class is used
 * exclusively as a contained class by RecordProfile.
 */
public class ColumnProfile
implements ProfileConstants
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private long m_recProfId;
    private long m_colNumber;
    private String m_tblName;
    private String m_type;
    private long m_ruleId;
    private int m_mode;
    private String m_label;
    
    //
    // Public Constructors
    //
    /**
     * Create an initialized instance of the column profile.
     */
    public ColumnProfile()
    {
        super();
        m_recProfId = -1;
        m_colNumber = -1;
        m_tblName = "";
        m_type = "";
        m_ruleId = -1;
        m_mode = TRANSLATABLE;
        m_label = "";
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Return the id of the record profile that "owns" this column profile.
     *
     * @return the current id.
     */
    public long getRecordProfileId()
    {
        return m_recProfId;
    }

    /**
     * Set the record profile id.
     *
     * @param p_id the new id.
     */
    void setRecordProfileId(long p_id)
    {
        m_recProfId = p_id;
    }
    
    /**
     * Return the index number for the column.
     *
     * @return the current number.
     */
    public long getColumnNumber()
    {
        return m_colNumber;
    }

    /**
     * Set the index number for this column..
     *
     * @param p_number the new number.
     */
    void setColumnNumber(long p_number)
    {
        m_colNumber = p_number;
    }

    /**
     * Return the name of the table the column came from.
     *
     * @return the current table name.
     */
    public String getTableName()
    {
        return m_tblName;
    }

    /**
     * Set the table name for this column.
     *
     * @param p_name the new value.
     */
    void setTableName(String p_name)
    {
        m_tblName = p_name;
    }

    /**
     * Return the data type for this column.
     *
     * @return the current data type.
     */
    public String getDataType()
    {
        return m_type;
    }

    /**
     * Set the data type for this column..
     *
     * @param p_type the new value.
     */
    void setDataType(String p_type)
    {
        m_type = p_type;
    }

    /**
     * Return the id of the rule used to process xml.
     *
     * @return the current rule id.
     */
    public long getRuleId()
    {
        return m_ruleId;
    }

    /**
     * Set the id of the rule used to process xml.
     *
     * @param p_id the new id.
     */
    void setRuleId(long p_id)
    {
        m_ruleId = p_id;
    }
        
    /**
     * Return the content mode.
     *
     * @return the current mode.
     */
    public int getContentMode()
    {
        return m_mode;
    }

    /**
     * Set the content mode.
     *
     * @param p_mode the new value.
     */
    void setContentMode(int p_mode)
    {
        m_mode = p_mode;
    }

    /**
     * Return the descriptive label for this column..
     *
     * @return the current label.
     */
    public String getLabel()
    {
        return m_label;
    }

    /**
     * Set the label for this column.
     *
     * @param p_label the new value.
     */
    void setLabel(String p_label)
    {
        m_label = p_label;
    }

    //
    // SUPPORT METHODS
    //
    /**
     * Return a string representation of the record profile.
     *
     * @return a description of the receiver.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ColumnProfile [RecProfId=");
        sb.append(getRecordProfileId());
        sb.append(", Type=");
        sb.append(getDataType());
        sb.append(", Mode=");
        sb.append(CONTENT_MODES[getContentMode() - 1]);
        sb.append(", Label=");
        sb.append(getLabel());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Return a detailed string representation of the record profile.
     *
     * @return a description of the receiver.
     */
    public String detailString()
    {
        return toString();
    }
}
