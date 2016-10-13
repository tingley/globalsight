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
package com.globalsight.persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ClobLinkage associates the name of a string-returning method with
 * a specific column within a specific table.  It also encapsulates the 
 * functionality of invoking the method on an object of the appropriate
 * type to return the string that will be written to the CLOB field in the
 * database.
 *
 * This class is restricted to package level view.
 */
class ClobLinkage
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private boolean m_isWorm;
    private String m_table;
    private String m_column;
    private Method m_getter;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Return an initialized clob connector.
     *
     * @param p_tableName the name of the database table containing the CLOB
     * column
     * @param p_columnName the name of the column within the table
     * @param p_clobGetterMethodName the name of the method that returns
     * the string from the entity that will be written to the database.
     * @param p_entityClass the class of the object on which the getter
     * method is defined.
     *
     * @return a new clob connector
     *
     * @throws NoSuchMethodException if the given p_clobGetterMethodName does
     * not exist on the given p_entityClass
     */
    ClobLinkage(String p_tableName,
                String p_columnName,
                String p_clobGetterMethodName,
                Class p_entityClass,
                boolean p_isWorm)
    throws NoSuchMethodException
    {
        super();
        m_table = p_tableName;
        m_column = p_columnName;
        m_getter = p_entityClass.getMethod(p_clobGetterMethodName, new Class[] {});
        m_isWorm = p_isWorm;
    }

    //
    // PACKAGE LEVEL ACCESSORS
    //
    /**
     * Return the table name.
     *
     * @return the table name.
     */
    String getTableName()
    {
        return m_table;
    }

    /**
     * Return the column name.
     *
     * @return the column name.
     */
    String getColumnName()
    {
        return m_column;
    }

    /**
     * Return the getter method.
     *
     * @return the getter method.
     */
    Method getGetter()
    {
        return m_getter;
    }

    /**
     * Return the clob string from the given object.
     *
     * @return the string to be written to the CLOB field.
     *
     * @throws InvocationTargetException if the method cannot be invoked
     * on the given object
     * @throws IllegalAccessException if the method on the target does not
     * have the appropriate visibility
     */
    String getClobStringFrom(Object p_obj)
    throws InvocationTargetException, IllegalAccessException
    {
        String str = (String)(m_getter.invoke(p_obj, new Object[] {}));
        return (str == null ? "" : str);
    }

    /**
     * Return true if the linkage is a read-only linkage -- i.e. it can be
     * written once (during insert) but not updated.
     *
     * @return the value of the read-only flag
     */

    boolean isWorm()
    {
        return m_isWorm;
    }

    //
    // PUBLIC OVERRIDES
    //
    /**
     * Return a string representation of the connector.
     *
     * @return a short description.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        if (isWorm())
        {
            sb.append(" (WRITE-ONCE-READ-MANY)");
        }
        sb.append(" {TableName=");
        sb.append(getTableName());
        sb.append(", ColumnName=");
        sb.append(getColumnName());
        sb.append(", Method=");
        sb.append(getGetter());
        sb.append("}");
        return sb.toString();
    }

    /**
     * Return true if and only if the three attributes match exactly.
     *
     * @param p_obj the object against which this connector is to be
     * compared
     *
     * @return whether the two objects are "equal" or not.
     */
    public boolean equals(Object p_obj)
    {
        boolean isSame = false;
        try
        {
            ClobLinkage cl = (ClobLinkage)p_obj;
            isSame =
                ((getColumnName().equals(cl.getColumnName())) &&
                 (getTableName().equals(cl.getTableName())) &&
                 (getGetter().toString().equals(cl.getGetter().toString())));
        }
        catch (Exception e)
        {
            // ignore -- any exception means they aren't equal
        }
        return isSame;
    }
}
