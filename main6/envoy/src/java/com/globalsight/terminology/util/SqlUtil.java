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

package com.globalsight.terminology.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.diplomat.util.database.DbAccessor;
import com.globalsight.everest.persistence.PersistenceService;

/**
 * <p>
 * SQL Utilities.
 * </p>
 */
public class SqlUtil
{
    /** Static class, private constructor. */
    private SqlUtil()
    {
    }

    /** Connection to the persistence service and the database. */
    private static PersistenceService s_database = null;

    /**
     * Allocates a SQL connection; call fireConnection() to return it to the
     * pool.
     * 
     * @throws Execption
     *             if no connection could be allocated.
     */
    public static Connection hireConnection() throws SQLException
    {
        Connection result = null;

        try
        {
            if (s_database == null)
            {
                s_database = PersistenceService.getInstance();
            }

            result = s_database.getConnection();
        }
        catch (Exception ex)
        {
            throw new SQLException(ex.getMessage());
        }

        return result;
    }

    /**
     * Returns a SQL connection allocated with hireConnection() to the pool.
     */
    public static void fireConnection(Connection p_conn)
    {
        try
        {
            if (p_conn != null)
            {
                s_database.returnConnection(p_conn);
            }
        }
        catch (Exception ex)
        { /* ignore */
        }
    }

    /** Silently closes the result set */
    public static void silentClose(ResultSet p_resultSet)
    {
        PersistenceService.silentClose(p_resultSet);
    }

    /** Silently closes the statement */
    public static void silentClose(PreparedStatement p_statement)
    {
        PersistenceService.silentClose(p_statement);
    }

    /**
     * <p>
     * Escapes single quotes (') in strings by two single quotes ('').
     * </p>
     */
    public static String quote(String p_string)
    {
        char quote = '\'';

        if (p_string.length() == 0 || p_string.indexOf(quote) == -1)
        {
            return p_string;
        }

        StringBuffer sb = new StringBuffer(p_string.length() + 2);

        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);

            sb.append(ch);

            if (ch == quote)
            {
                sb.append(quote);
            }
        }

        return sb.toString();
    }

    final private static char[] s_digits = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * Converts a byte array to a (Oracle &amp; SQL Server) hex string.
     * 
     * @param maxlen
     *            maximum count of bytes to convert (for database column size
     *            restrictions).
     */
    public static String toHex(byte[] bytes, int maxlen)
    {
        int len = bytes.length;

        if (maxlen > 0)
        {
            len = Math.min(bytes.length, maxlen);
        }

        StringBuffer result = new StringBuffer(len * 2);

        for (int i = 0; i < len; ++i)
        {
            byte b = bytes[i];
            result.append(s_digits[(b & 0xf0) >>> 4]);
            result.append(s_digits[b & 0x0f]);
        }

        return result.toString();
    }

    static public String readClob(ResultSet p_rs, String p_columnName)
            throws SQLException
    {
        try
        {
            return DbAccessor.readClob(p_rs, p_columnName);
        }
        catch (Throwable ex)
        {
            throw new SQLException(ex.toString());
        }
    }

    /**
     * A replacement for clumsy inline expressions: returns a string to be
     * inserted in an INSERT or UPDATE statement depending on whether the string
     * can be written out inline or needs a separate statement for the CLOB.
     * 
     * @return 
     * <pre>
     *   Return blank string '' instead of "empty_clob()" for MySql
     *   or the properly quoted string value.
     */
    static public String getClobInitializer(String p_value, boolean p_needClob)
    {
        StringBuffer result = new StringBuffer();

        if (p_needClob)
        {
        	result.append("''");
        }
        else
        {
            result.append("'");
            result.append(quote(p_value));
            result.append("'");
        }

        return result.toString();
    }

    /**
     * Returns an "in" list expression from the objects passed in.
     * 
     * @return "('1','2','3'...)".
     */
    static public String getInList(List p_objects)
    {
        StringBuffer result = new StringBuffer();

        result.append("('");

        for (int i = 0, max = p_objects.size(); i < max; i++)
        {
            Object o = p_objects.get(i);

            result.append(o);

            if (i < max - 1)
            {
                result.append("','");
            }
        }

        result.append("')");

        return result.toString();
    }
}
