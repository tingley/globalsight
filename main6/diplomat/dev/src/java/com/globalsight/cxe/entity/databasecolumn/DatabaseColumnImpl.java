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
package com.globalsight.cxe.entity.databasecolumn;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Database Column Class Implementation
 */
public class DatabaseColumnImpl extends PersistentObject
    implements DatabaseColumn
{

    private static final long serialVersionUID = 7863080329902793370L;
    //
    // PUBLIC CONSTANTS (for TOPLink)
    //
    public static final String M_COLUMN_NO = "m_columnNo";
    public static final String M_COLUMN_NAME = "m_columnName";
    public static final String M_LABEL = "m_label";
    public static final String M_TABLE_NAME = "m_tableName";
    public static final String M_DB_PROFILE_ID = "m_dbProfileId";
    public static final String M_XML_RULE_ID = "m_xmlRuleId";
    public static final String M_FORMAT_TYPE = "m_formatType";
    public static final String M_CONTENT_MODE = "m_contentMode";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private long    m_columnNo;
    private String  m_columnName;
    private String  m_label;
    private String  m_tableName;
    private Long    m_dbProfileId;
    private Long    m_xmlRuleId;
    private long    m_formatType;
    private int     m_contentMode;

    /**
     *  Default constructor used by TopLink only
     */
    public DatabaseColumnImpl ()
    {
        super();

        m_columnNo = 0;
        m_columnName = null;
        m_label = null;
        m_tableName = null;
        m_dbProfileId = null;
        m_xmlRuleId = null;
        m_formatType = 0;
        m_contentMode = 0;
    }

    /**
     * Constructor that supplies all attributes for database profile object.
     *
     * @param p_columnNo Column number
     * @param p_columnName Column name
     * @param p_label Column display label
     * @param p_tableName Column table name
     * @param p_dbProfileId Database profile id
     * @param p_xmlRuleId XML rule file id
     * @param p_formatType Known format type
     * @param p_contenMode column content mode
     */
    public DatabaseColumnImpl (long     p_columnNo,
                               String   p_columnName,
                               String   p_label,
                               String   p_tableName,
                               long     p_dbProfileId,
                               long     p_xmlRuleId,
                               long     p_formatType,
                               int      p_contentMode)
    {
        super();

        m_columnNo = p_columnNo;
        m_columnName = p_columnName;
        m_label = p_label;
        m_tableName = p_tableName;
        setDatabaseProfileId(p_dbProfileId);
        setXmlRuleId(p_xmlRuleId);
        m_formatType = p_formatType;
        m_contentMode = p_contentMode;
    }

    /**
     * Constructs an DatabaseColumnImpl object from a DatabaseColumn object
     *
     * @param p_dbColumn Another DatabaseColumn object
     **/
    public DatabaseColumnImpl(DatabaseColumn p_dbColumn)
    {
        super();

        m_columnNo = p_dbColumn.getColumnNumber();
        m_columnName = p_dbColumn.getColumnName();
        m_label = p_dbColumn.getLabel();
        m_tableName = p_dbColumn.getTableName();
        setDatabaseProfileId(p_dbColumn.getDatabaseProfileId());
        setXmlRuleId(p_dbColumn.getXmlRuleId());
        m_formatType = p_dbColumn.getFormatType();
        m_contentMode = p_dbColumn.getContentMode();
    }

    /**
     * Return the column number
     *
     * @return column number
     */
    public long getColumnNumber()
    {
        return m_columnNo;
    }

    /**
     * Set column number
     *
     * @param p_colNo column number
     */
    public void setColumnNumber(long p_colNo)
    {
        m_columnNo = p_colNo;
    }

    /**
     * Return the column name
     *
     * @return column name
     */
    public String getColumnName()
    {
        return m_columnName;
    }


    /**
     * Set column name
     *
     * @param p_colName column name
     */
    public void setColumnName(String p_colName)
    {
        m_columnName = p_colName;
    }


    /**
     * Return the column label used for display
     *
     * @return column label
     */
    public String getLabel()
    {
        return m_label;
    }

    /**
     * Set the column's display label
     *
     * @param p_label column label
     */
    public void setLabel(String p_label)
    {
        m_label = p_label;
    }

    /**
     * Return the table name of column
     *
     * @return column's table name
     */
    public String getTableName()
    {
        return m_tableName;
    }

    /**
     * Set table name of column
     *
     * @param p_tableName table name
     */
    public void setTableName(String p_tableName)
    {
        m_tableName = p_tableName;
    }

    /**
     * Return the database profile id
     *
     * @return database profile id
     */
    public long getDatabaseProfileId()
    {
        if (m_dbProfileId == null)
        {
            return 0;
        }
        else
        {
            return m_dbProfileId.longValue();
        }
    }

    /**
     * Set database profile id
     *
     * @param p_id database profile id
     */
    public void setDatabaseProfileId(long p_id)
    {
        if (p_id == 0)
        {
            m_dbProfileId = null;
        }
        else
        {
            m_dbProfileId = new Long(p_id);
        }

    }

    /**
     * Return XML rule id
     *
     * @return  XML rule id
     */
    public long getXmlRuleId()
    {
        if (m_xmlRuleId == null)
        {
            return 0;
        }
        else
        {
            return m_xmlRuleId.longValue();
        }
    }

    /**
     * Set XML rule id
     *
     * @param p_id  XML rule id
     */
    public void setXmlRuleId(long p_id)
    {
        if (p_id == 0)
        {
            m_xmlRuleId = null;
        }
        else
        {
            m_xmlRuleId = new Long(p_id);
        }
    }


    /**
     * Return known format type
     *
     * @return  known format type
     */
    public long getFormatType()
    {
        return m_formatType;
    }

    /**
     * Set format type
     *
     * @param p_formatType  known format type
     */
    public void setFormatType(long p_formatType)
    {
        m_formatType = p_formatType;
    }

    /**
     * Return column content mode
     *
     * @return  column content mode
     */
    public int getContentMode()
    {
        return m_contentMode;
    }

    /**
     * Set column content mode
     *
     * @param p_mode  column content mode
     */
    public void setContentMode(int p_mode)
    {
        m_contentMode = p_mode;
    }

    /**
     * Return string representation of object
     *
     * @return string representation of object
     */
    public String toString()
    {
        return m_columnName;
    }

    public long getColumnNo()
    {
        return m_columnNo;
    }

    public void setColumnNo(long no)
    {
        m_columnNo = no;
    }

    public Long getDbProfileId()
    {
        return m_dbProfileId;
    }

    public void setDbProfileId(Long profileId)
    {
        m_dbProfileId = profileId;
    }
}
