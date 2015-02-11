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

import com.globalsight.diplomat.util.database.SqlParameterSubstituter;

import java.util.HashMap;
import java.util.Vector;

/**
 * DbRecordProxy, represents a row of data that must be written back to the
 * database, according to the rules of the Preview or Final put-back SQL.
 */
public class DbRecordProxy
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String SOURCE = "sourceLanguage";
    private static final String TARGET = "targetLanguage";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_rpid;
    private String m_acqSqlParm;
    private String m_srcLang;
    private String m_tgtLang;
    private String m_sqlType;
    private Vector m_cols;
    private transient HashMap m_parms;

    /**
     * Creates a new instance of a writeable column.
     */
    public DbRecordProxy()
    {
        super();
        m_rpid = "";
        m_acqSqlParm = "";
        m_srcLang = "";
        m_tgtLang = "";
        m_sqlType = "";
        m_cols = new Vector();
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Return the value of the record profile id field.
     * 
     * @return the current value.
     */
    public String getRecordProfileId()
    {
        return m_rpid;
    }

    /**
     * Set the value of the record profile id field.
     * 
     * @param p_str the new value.
     */
    public void setRecordProfileId(String p_str)
    {
        m_rpid = p_str;
    }

    /**
     * Return the value of the acquisition Sql parameter field.
     * 
     * @return the current value.
     */
    public String getAcquisitionSqlParameter()
    {
        return m_acqSqlParm;
    }

    /**
     * Set the value of the acquisition Sql parameter field.
     * 
     * @param p_str the new value.
     */
    public void setAcquisitionSqlParameter(String p_str)
    {
        m_acqSqlParm = p_str;
    }

    /**
     * Return the value of the source language field.
     * 
     * @return the current value.
     */
    public String getSourceLanguage()
    {
        return m_srcLang;
    }

    /**
     * Set the value of the source language field.
     * 
     * @param p_str the new value.
     */
    public void setSourceLanguage(String p_str)
    {
        m_srcLang = p_str;
    }

    /**
     * Return the value of the target language field.
     * 
     * @return the current value.
     */
    public String getTargetLanguage()
    {
        return m_tgtLang;
    }

    /**
     * Set the value of the target language field.
     * 
     * @param p_str the new value.
     */
    public void setTargetLanguage(String p_str)
    {
        m_tgtLang = p_str;
    }

    /**
     * Return the value of the SQL type field.
     * 
     * @return the current value.
     */
    public String getSqlType()
    {
        return m_sqlType;
    }

    /**
     * Set the value of the SQL type field.
     * 
     * @param p_str the new value.
     */
    public void setSqlType(String p_str)
    {
        m_sqlType = p_str;
    }

    /**
     * Return the value of the columns field.
     * 
     * @return the current value.
     */
    public Vector getColumns()
    {
        return m_cols;
    }

    /**
     * Set the value of the columns field.
     * 
     * @param p_vec the new value.
     */
    public void setColumns(Vector p_vec)
    {
        m_cols = (p_vec == null ? new Vector() : p_vec);
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return a hashmap containing all key-value pairs for which sql
     * substitution may be required.
     *
     * @return a hashmap with values corresponding to variable names
     *
     */
    public HashMap parameterMap()
    {
        if (m_parms == null)
        {
            buildParameterMap();
        }
        return m_parms;
    }

    /**
     * Return the size of the record, i.e. the number of columns it has.
     *
     * @return the number of columns.
     */
    public int size()
    {
        return m_cols.size();
    }

    /**
     * Add the given column proxy to the collection.
     *
     * @param p_col the column to add.
     */
    public void addColumn(DbColumnProxy p_col)
    {
        m_cols.addElement(p_col);
    }

    /**
     * Return a long string representation of the record proxy.
     *
     * @return a description of the receiver.
     */
    public String detailString()
    {
        return (prefix() + columnDetails() + suffix());
    }

    /**
     * Return a short string representation of the record proxy.
     *
     * @return a description of the receiver.
     */
    public String toString()
    {
        return (prefix() + ", size=" + size() + " columns" + suffix());
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Build the hashmap containing the parameter key-value pairs. */
    private void buildParameterMap()
    {
        m_parms = new HashMap();
        String acqSqlParm = getAcquisitionSqlParameter();
        Vector acqParms = (acqSqlParm == null || acqSqlParm.length() == 0) ?
            new Vector() :
            SqlParameterSubstituter.tokenize(acqSqlParm);
        for (int i = 0 ; i < acqParms.size() ; i++)
        {
            m_parms.put("" + (i + 1), acqParms.elementAt(i));
        }
        m_parms.put(SOURCE, getSourceLanguage());
        m_parms.put(TARGET, getTargetLanguage());
        Vector cols = getColumns();
        for (int i = 0 ; i < cols.size() ; i++)
        {
            DbColumnProxy cp = (DbColumnProxy)cols.elementAt(i);
            m_parms.put(cp.getTableName() + "." + cp.getName(), cp.getContent());
        }
    }

    /* toString() & detailString(): return a prefix string */
    private String prefix()
    {
        return "DbRecordProxy [recordProfileId=" + getRecordProfileId() +
            ", acqSqlParm=\"" + getAcquisitionSqlParameter() + "\"" +
            ", sqlType=" + getSqlType() +
            ", src=" + getSourceLanguage() + 
            ", tgt=" + getTargetLanguage();
    }

    /* toString() & detailString(): return a suffix string */
    private String suffix()
    {
        return "]";
    }

    /* detailString(): return a string with the column details */
    private String columnDetails()
    {
        return (", columns=" + m_cols);
    }
}

