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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * DbAccessor is an abstract class meant to provide common functionality for
 * classes which save/load objects to/from the database. All public methods on
 * the concrete subclass should be declared static only.
 * <p>
 * This class defines several protected static methods for reading and writing
 * CLOBs and BLOBs. See TaskClassifierDbAccessor for a detailed example of how
 * to access a BLOB field. (Accessing a CLOB field is analagous.)
 */
public abstract class DbAccessor
{
    //
    // PRIVATE CONSTANTS
    //
    private static final int EOF = -1;
    private static final String BLOB = "BLOB";
    private static final String TEXT = "TEXT";
    private static final String READ = "read";

    //
    // PROTECTED STATIC SUPPORT METHODS
    //
    /* Obtain a default connection to the database. */
    protected static Connection getConnection() throws DbAccessException
    {
        return connection(null);
    }

    /* Obtain a connection to the database, based on the connection profile */
    /* with the given id. */
    protected static Connection getConnection(long p_profileId)
            throws DbAccessException
    {
        return connection(new Long(p_profileId));
    }

    /* Return the given connection back to the connection pool. */
    protected static void returnConnection(Connection p_conn)
    {
        try
        {
            ConnectionPool.returnConnection(p_conn);
        }
        catch (Exception e)
        {
            // ignore the exception
        }
    }

    /**
     * Read the BLOB from the given result set, accessed via the given column
     * index. Return the contents of the BLOB as a byte array.
     * <p>
     * Assumption: it is assumed that the given result set has at least one row,
     * and that the internal row pointer is already pointing at the row
     * containing the desired BLOB column.
     * 
     * @param p_rs
     *            the result set to read from.
     * @param p_columnIndex
     *            the integer index of the desired column in the result set.
     * 
     * @return a byte array representing the contents of the BLOB
     * 
     * @throws DbAccessException
     *             if any database access error occurs.
     */
    public static byte[] readBlob(ResultSet p_rs, int p_columnIndex)
            throws DbAccessException
    {
        byte[] result = null;
        try
        {
            result = readBlobFromInputStream(p_rs
                    .getBinaryStream(p_columnIndex));
        }
        catch (Exception e)
        {
            throwBlobReadException("" + p_columnIndex, e);
        }
        return result;
    }

    /**
     * Read the BLOB from the given result set, accessed via the given column
     * name. Return the contents of the BLOB as a byte array.
     * <p>
     * Assumption: it is assumed that the given result set has at least one row,
     * and that the internal row pointer is already pointing at the row
     * containing the desired BLOB column.
     * 
     * @param p_rs
     *            the result set to read from.
     * @param p_columnName
     *            the String name of the desired column in the result set.
     * 
     * @return a byte array representing the contents of the BLOB
     * 
     * @throws DbAccessException
     *             if any database access error occurs.
     */
    public static byte[] readBlob(ResultSet p_rs, String p_columnName)
            throws DbAccessException
    {
        byte[] result = null;
        try
        {
            result = readBlobFromInputStream(p_rs.getBinaryStream(p_columnName));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throwBlobReadException(p_columnName, e);
        }

        return result;
    }

    /**
     * Read Blob from input stream.
     * 
     * @param is
     * @return
     * @throws IOException
     */
    private static byte[] readBlobFromInputStream(InputStream is)
            throws IOException
    {
        byte[] result = new byte[0];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = is.read(buffer)) != EOF)
        {
            baos.write(buffer, 0, bytesRead);
        }
        baos.flush();
        baos.close();
        is.close();
        result = baos.toByteArray();

        return result;
    }

    /**
     * Read the CLOB from the given result set, accessed via the given column
     * index. Return the contents of the CLOB as a string.
     * <p>
     * Assumption: it is assumed that the given result set has at least one row,
     * and that the internal row pointer is already pointing at the row
     * containing the desired BLOB column.
     * 
     * @param p_rs
     *            the result set to read from.
     * @param p_columnIndex
     *            the integer index of the desired column in the result set.
     * 
     * @return a string representing the contents of the CLOB
     * 
     * @throws DbAccessException
     *             if any database access error occurs.
     */
    public static String readClob(ResultSet p_rs, int p_columnIndex)
            throws DbAccessException
    {
        String result = null;
        try
        {
            result = p_rs.getString(p_columnIndex);
        }
        catch (Exception e)
        {
            throwClobReadException("" + p_columnIndex, e);
        }
        return result;
    }

    /**
     * Read the CLOB from the given result set, accessed via the given column
     * name. Return the contents of the CLOB as a string.
     * <p>
     * Assumption: it is assumed that the given result set has at least one row,
     * and that the internal row pointer is already pointing at the row
     * containing the desired BLOB column.
     * 
     * @param p_rs
     *            the result set to read from.
     * @param p_columnName
     *            the name of the desired column in the result set.
     * 
     * @return a string representing the contents of the CLOB
     * 
     * @throws DbAccessException
     *             if any database access error occurs.
     */
    public static String readClob(ResultSet p_rs, String p_columnName)
            throws DbAccessException
    {
        String result = null;
        try
        {
            result = p_rs.getString(p_columnName);
        }
        catch (Exception e)
        {
            throwClobReadException(p_columnName, e);
        }
        return result;
    }

    /* Generalized connection obtainer. */
    private static Connection connection(Long p_id) throws DbAccessException
    {
        Connection conn = null;
        try
        {
            conn = (p_id == null ? ConnectionPool.getConnection()
                    : ConnectionPool.getConnection(p_id.longValue()));
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to obtain connection", e);
        }
        return conn;
    }

    /* Throw a generic LOB exception */
    private static void throwLobException(String p_lobType, String p_access,
            String p_column, Exception p_ex) throws DbAccessException
    {
        throw new DbAccessException("Unable to " + p_access + " " + p_lobType
                + " at column " + p_column, p_ex);
    }

    /* Throw a generic blob read exception. */
    private static void throwBlobReadException(String p_column, Exception p_ex)
            throws DbAccessException
    {
        throwLobException(BLOB, READ, p_column, p_ex);
    }

    /* Throw a generic clob read exception. */
    private static void throwClobReadException(String p_column, Exception p_ex)
            throws DbAccessException
    {
        throwLobException(TEXT, READ, p_column, p_ex);
    }
}
