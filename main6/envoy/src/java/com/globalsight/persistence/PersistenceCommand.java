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

import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * This abstract class is an implementation of the "Command" Design Pattern for
 * persistence purposes. Subclasses of this class will different implementations
 * of the persistObjects() method. The persistObjects() method is an instance of
 * the "Template Method" Design Pattern.
 */
public abstract class PersistenceCommand
{
    private static Logger s_logger = Logger.getLogger(PersistenceCommand.class);

    public PersistenceCommand()
    {
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
    }

    public abstract void createPreparedStatement(Connection p_connection)
            throws Exception;

    public abstract void setData() throws Exception;

    public abstract void batchStatements() throws Exception;

    public long allocateSequenceNumberRange(SequenceStore p_seqStore)
    {
        long sequenceNumberValue = p_seqStore.getSequenceNumberValue();
        long numberOfObjects = p_seqStore.getNumberOfObjects();
        long primaryKey;
        primaryKey = Math.abs((sequenceNumberValue - numberOfObjects) + 1);
        return primaryKey;
    }

    /**
     * Closes a Statement and logs out any problems with it.
     */
    public static void close(Statement p_statement)
    {
        if (p_statement != null)
        {
            try
            {
                p_statement.close();
            }
            catch (Exception e)
            {
                s_logger.error("Problem closing statement", e);
            }
        }
    }

    /**
     * Closes a Statement and logs out any problems with it.
     */
    public static void close(PreparedStatementBatch p_batch)
    {
        if (p_batch != null)
        {
            p_batch.closeAll();
        }
    }

    /**
     * Closes a result set and logs out any problems with it.
     */
    public static void close(ResultSet p_resultSet)
    {
        if (p_resultSet != null)
        {
            try
            {
                p_resultSet.close();
            }
            catch (Exception e)
            {
                s_logger.error("Problem closing result set", e);
            }
        }
    }

    /**
     * Closes a Writer and logs out any problems with it.
     */
    public static void close(Writer p_writer)
    {
        if (p_writer != null)
        {
            try
            {
                p_writer.close();
            }
            catch (Exception e)
            {
                s_logger.error("Problem closing writer", e);
            }
        }
    }
}
