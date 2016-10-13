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

import java.util.Vector;

/**
 * Helper class that associates the correct, fully expanded, update and insert
 * Sql with a particular connection id.
 */
public class SqlHolder
{
    //
    // PRIVATE CONSTANTS (USED FOR PARSING)
    //
    private static final char DELIM = ';';
    private static final char QUOTE = '\'';

    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_updateSql;
    private String m_insertSql;
    private long m_connectionId;
    
    //
    // PUBLIC CONSTRUCTORS
    //
    public SqlHolder(String p_updateSql, String p_insertSql, long p_connectionId)
    {
        super();
        m_updateSql = p_updateSql;
        m_insertSql = p_insertSql;
        m_connectionId = p_connectionId;
    }

    /**
     * Return the update sql.
     */
    public String getUpdateSql()
    {
        return m_updateSql;
    }

    /**
     * Return the insert sql.
     */
    public String getInsertSql()
    {
        return m_insertSql;
    }

    /**
     * Return the connection id..
     */
    public long getConnectionId()
    {
        return m_connectionId;
    }

    /**
     * Return the insert SQL as an array of individual SQL statements.
     */
    public String[] parsedInsertSql()
    {
        return parseSql(getInsertSql());
    }

    /**
     * Return the update SQL as an array of individual SQL statements.
     */
    public String[] parsedUpdateSql()
    {
        return parseSql(getUpdateSql());
    }
    
    //
    // PRIVATE SUPPORT METHODS
    //
    /* convert the given string into an array of one or more strings */
    private String[] parseSql(String p_sql)
    {
        Vector v = new Vector();

        boolean inQuotes = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < p_sql.length() ; i++)
        {
            char ch = p_sql.charAt(i);

            if (ch == QUOTE)
            {
                inQuotes = !inQuotes;
            }

            if ((ch == DELIM) && !inQuotes)
            {
                addTrimmedString(v, sb.toString());
                sb = new StringBuffer();
                inQuotes = false;
            }
            else
            {
                sb.append(ch);
            }
        }
        addTrimmedString(v, sb.toString());
        return toStringArray(v);
    }

    /* trim the given string; if non-empty, add it to the given vector */
    private void addTrimmedString(Vector p_vector, String p_string)
    {
        String trimmed = p_string.trim();
        if (trimmed.length() > 0)
        {
            p_vector.addElement(trimmed);
        }
    }

    /* convert the given vector into an array of strings; return the result */
    private String[] toStringArray(Vector p_vector)
    {
        int size = p_vector.size();
        String[] sv = new String[size];
        for (int i = 0 ; i < size ; i++)
        {
            sv[i] = ((String)p_vector.elementAt(i)).trim();
        }
        return sv;
    }
}

