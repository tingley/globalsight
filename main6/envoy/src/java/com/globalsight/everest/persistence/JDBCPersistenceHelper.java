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


package com.globalsight.everest.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

public class JDBCPersistenceHelper
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            JDBCPersistenceHelper.class.getName());

    private int MAX_ELEM = 4095;
    private long JDBCPOOL_ID = -1L;
    private long IMPORTPOOL_ID = -100L;
    private static final String SQL_SEQ_SELECT =
        "select count from sequence where name=?";
    private static final String SQL_SEQ_UPDATE =
        "update sequence set count=count+? where name=?";

    public JDBCPersistenceHelper()
    {
    }


    private String[] convertVectorToCharArray(Vector p_stringArgs)
    {
        Iterator it = null;
        it = p_stringArgs.iterator();
        String[] charArray = new String[p_stringArgs.size()];
        int i = 0;
        while (it.hasNext())
        {
            String value = (String)it.next();
            charArray[i] = value;
            i++;
        }
        return charArray;
    }

    private double[] doubleArray(Vector p_numberArgs)
    {
        Iterator it = p_numberArgs.iterator();
        double[] numberArray = new double[p_numberArgs.size()];
        int i = 0;
        while (it.hasNext())
        {
            numberArray[i] =  ((Number)it.next()).doubleValue();
            //CATEGORY.debug("The value of the number is " + numberArray[i]);
            i++;
        }
        return numberArray;
    }

    /**
     * Returns the last sequence number for the group of sequence numbers
     * for the number of objects.
     * <br>
     * @param p_numberOfObjects -- the number of objects requiring a sequence number
     * @param p_sequenceName -- the name of the row in the sequence table
     * @return long -- the last sequence number in the group
     * @throws PersistenceException
     */
    public synchronized long getSequenceNumber(long p_numberOfObjects,
        String p_sequenceName)
        throws PersistenceException
    {
        long sequenceNumber = 0;

        //allow only one thread to get a sequence number at a time
        Connection connection = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        boolean originalState = true;
        try
        {
            connection = getConnection();
            originalState = connection.getAutoCommit();
            connection.setAutoCommit(false);

            //First update the table with the new sequence number
            ps = connection.prepareStatement(SQL_SEQ_UPDATE);
            ps.setLong(1, p_numberOfObjects);
            ps.setString(2, p_sequenceName);
            ps.executeUpdate();

            //now get the current sequence value
            ps2 = connection.prepareStatement(SQL_SEQ_SELECT);
            ps2.setString(1, p_sequenceName);
            rs = ps2.executeQuery();
            if (rs.next())
                sequenceNumber = rs.getLong(1);
            connection.commit();
        }
        catch (Exception e)
        {
            try {
                connection.rollback();
                throw new PersistenceException(e);
            }
            catch (Exception sqle)
            {
                throw new PersistenceException(e);
            }
        }
        finally
        {
            if (ps != null)
            {
                try {ps.close();}
                catch(Exception e){}
            }

            if (ps2 != null)
            {
                try {ps2.close();}
                catch(Exception e){}
            }

            if (rs != null)
            {
                try {rs.close();}
                catch(Exception e){}
            }

            try {connection.setAutoCommit(originalState);}
            catch (Exception e) {}

            try { ConnectionPool.returnConnection(connection);}
            catch (Exception cpe){}
        }

        // CATEGORY.debug("The sequence name is " + p_sequenceName);
        // CATEGORY.debug("The number of objects is " + p_numberOfObjects);
        // CATEGORY.debug("The sequence number is " + sequenceNumber);
        return sequenceNumber;
    }


    private Collection splitParamForUpdate(Vector p_param)
    {
        return splitParam(p_param, MAX_ELEM);
    }

    private Collection splitParam(List p_param, int arrayMax)
    {
        Collection result = new Vector();
        int size = p_param.size();
        int top = 0;
        int end = arrayMax;

        while (end < size)
        {
            while (((Number)p_param.get(end - 1)).longValue() != -1)
            {
                end--;
            }

            result.add(new Vector(p_param.subList(top, end)));

            top = end;
            end = top + arrayMax;
        }

        result.add(new Vector(p_param.subList(top, size)));

        return result;
    }

    public Connection getConnection() throws ConnectionPoolException
    {
        Connection connection = null;

        connection= ConnectionPool.getConnection(JDBCPOOL_ID);

        return connection;
    }
    public Connection getConnectionForImport() throws ConnectionPoolException
    {
        Connection connection = null;
        connection = ConnectionPool.getConnection(IMPORTPOOL_ID);
        return connection;
    }
    public void returnConnection(Connection p_connection)
        throws ConnectionPoolException
    {
        ConnectionPool.returnConnection(p_connection);
    }
}
