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
package com.globalsight.cxe.adapter.database.source;

import com.globalsight.diplomat.util.database.ColumnAssociation;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.Vector;

/**
 * ResultSetProxy is closely associated with a particular RecordProfile.  When
 * a record profile's acquisition SQL is executed, it generates a result set,
 * whose structure needs to be determined and represented in a general way.
 * The ResultSetProxy provides this functionality by maintaining a vector of
 * ColumnAssociations, each of which maps a column name to a value.
 */
public class ResultSetProxy
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_acqSql;
    private Vector m_assocList;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Create a new proxy, initialized from the contents of the given
     * result set.
     */
    public ResultSetProxy(String p_sql, ResultSet p_rs)
    {
        super();
        m_acqSql = p_sql;
        buildAssociations(p_rs);
    }

    /**
     * Return the column association located at the given index, or null if the
     * index is out of range.
     *
     * @param p_index the index of the desired association.
     *
     * @return the desired association or null.
     */
    public ColumnAssociation associationAt(int p_index)
    {
        ColumnAssociation ca = null;
        if (p_index > -1 && p_index < m_assocList.size())
        {
            ca = (ColumnAssociation)m_assocList.elementAt(p_index);
        }
        return ca;
    }

    /**
     * Return the key for the column association located at the given index,
     * or null if the index is out of range.
     *
     * @param p_index the index of the desired key.
     *
     * @return the desired key or null.
     */
    public String keyAt(int p_index)
    {
        ColumnAssociation ca = associationAt(p_index);
        return (ca == null ? null : ca.getKey());
    }

    /**
     * Return the value for the column association located at the given index,
     * or null if the index is out of range.
     *
     * @param p_index the index of the desired key.
     *
     * @return the desired value or null.
     */
    public String valueAt(int p_index)
    {
        ColumnAssociation ca = associationAt(p_index);
        return (ca == null ? null : ca.getValue());
    }

    /**
     * Return the width for the column association located at the given index,
     * or -1 if the index is out of range.
     *
     * @param p_index the index of the desired key.
     *
     * @return the desired width or null.
     */
    public int widthAt(int p_index)
    {
        ColumnAssociation ca = associationAt(p_index);
        return (ca == null ? -1 : ca.getWidth());
    }

    /**
     * Return the number of associations in the result set proxy.
     *
     * @return the size of the proxy.
     */
    public int size()
    {
        return m_assocList.size();
    }

    /**
     * Return a string representation of the receiver.
     *
     * @return a description of the receiver.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ResultSetProxy [sql=\"" + m_acqSql + "\", associations=[");
        int size = size();
        for (int i = 0; i < size ; i++)
        {
            ColumnAssociation ca = associationAt(i);
            sb.append("\"" + ca.getKey() + "\"->\"" + ca.getValue() + "\"");
            if (i < size - 1)
            {
                sb.append(", ");
            }
        }
        sb.append("]]");
        return sb.toString();
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Build a new list of column associations from the contents of the */
    /* given result set. */
    private void buildAssociations(ResultSet p_rs)
    {
        m_assocList = new Vector();
        try
        {
            ResultSetMetaData rsmd = p_rs.getMetaData();
            if (p_rs.next())
            {
                for (int i = 1 ; i <= rsmd.getColumnCount() ; i++)
                {
                    String result = p_rs.getString(i);
                    if (result == null)
                    {
                        result = "";
                    }
                    addAssociation(rsmd.getColumnName(i), result, rsmd.getColumnDisplaySize(i));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /* Construct a new association from the given key value pair and add it */
    /* to the list.*/
    private void addAssociation(String p_key, String p_value, int p_width)
    {
        ColumnAssociation ca = new ColumnAssociation(p_key, p_value, p_width);
        m_assocList.addElement(ca);
    }
}

