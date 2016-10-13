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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.globalsight.diplomat.util.database.DbAccessException;
import com.globalsight.diplomat.util.database.DbAccessor;

/**
 * Concrete extension of the DbAccessor class.
 * <p>
 * Adds functionality to permit the serialization and de-serialization of
 * TaskClassifiers to/from the database.  TaskClassifiers reside in a private
 * table in the Diplomat database, so all connections are through the default
 * connection profile.
 */
public class TaskClassifierDbAccessor
    extends DbAccessor
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String TABLE_NAME = "TASK_CLASSIFIER";
    private static final String ID = "DB_DISPATCH_PROFILE_ID";
    private static final String SERIALIZATION = "SERIALIZATION";
    private static final String WHERE_CLAUSE = " WHERE " + ID + " =?" ;
    private static final String SELECT_SQL =
        "SELECT * FROM " + TABLE_NAME + WHERE_CLAUSE;
    private static final String UPDATE_SQL =
        "UPDATE " + TABLE_NAME + " SET " + SERIALIZATION + " = ? " + 
        WHERE_CLAUSE;
    private static final String COUNT_SQL =
        "SELECT COUNT(*) FROM " + TABLE_NAME + WHERE_CLAUSE;
    private static final String INSERT_SQL =
        "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?)";

    //
    // PUBLIC STATIC METHODS
    //
    /**
     * Read the task classifier corresponding to the given task queue profile
     * id.  If none exists return a new task classifier.
     *
     * @param p_taskQueueProfileId the id of the profile to load.
     *
     * @return the task classifier.
     *
     * @throws DbAccessException if any database access failure occurs.
     */
    public static TaskClassifier readTaskClassifier(long p_taskQueueProfileId)
    throws DbAccessException
    {
        TaskClassifier tc = null;
        try
        {
            Connection conn = getConnection();
            if (classifierExists(conn, p_taskQueueProfileId))
            {
                PreparedStatement pst = conn.prepareStatement(SELECT_SQL);
                pst.setLong(1,p_taskQueueProfileId);
                ResultSet rs = pst.executeQuery();
                if (rs.next())
                {
                    tc = deserialize(readBlob(rs, SERIALIZATION));
                }
                pst.close();
            }
            returnConnection(conn);
            if (tc == null)
            {
                tc = new TaskClassifier();
            }
        }
        catch (Exception e)
        {
            throwDbException(p_taskQueueProfileId, e, "load");
        }
        return tc;
    }

    /**
     * Serialize the given task classifier for the given task queue profile
     * id, write the result to the database.
     *
     * @param p_classifier the classifier to serialize.
     * @param p_taskQueueProfileId the id of the task queue profile to
     * associate with
     *
     * @throws DbAccessException if unable to save the classifier.
     */
    public static void saveTaskClassifier(TaskClassifier p_classifier,
                                          long p_taskQueueProfileId)
    throws DbAccessException
    {
        try
        {
            Connection conn = getConnection();
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            updateInsert(conn, p_classifier, p_taskQueueProfileId);
            conn.commit();
            conn.setAutoCommit(autoCommit);
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throwDbException(p_taskQueueProfileId, e, "save");
        }
    }

    //
    // PRIVATE STATIC SUPPORT METHODS
    //
    /* Return true if there is a classifier associated with the given task */
    /* queue profile id. We do this to avoid loading the blob. */
    private static boolean classifierExists(Connection p_conn, long p_id)
    throws SQLException
    {
        boolean exists = false;
        PreparedStatement pst = p_conn.prepareStatement(COUNT_SQL);
        pst.setLong(1,p_id);
        ResultSet rs = pst.executeQuery();
        if (rs.next())
        {
            long count = rs.getLong(1);
            exists = count > 0;
        }
        pst.close();
        return exists;
    }

    /* Throw an exception describing the problem */
    private static void throwDbException(long p_id, Exception p_ex, String p_str)
    throws DbAccessException
    {
        throw new DbAccessException("Unable to " + p_str + 
                                    " task classifier for profile id=" + p_id,
                                    p_ex);
    }

    /* Try to update the given serialized string at the appropriate place in */
    /* the database; if update fails due to missing id, try insert. */
    private static void updateInsert(Connection p_conn, TaskClassifier p_tc, long p_id)
    throws IOException, SQLException, DbAccessException
    {
        byte[] serializedTaskClassifier = serialize(p_tc);
        if (classifierExists(p_conn, p_id))
        {
            update(p_conn, serializedTaskClassifier, p_id);
        }
        else
        {
            insert(p_conn, serializedTaskClassifier, p_id);
        }
    }

    /* Update the contents of an existing record.  This requires writing out */
    /* a blob object. */
    private static void update(Connection p_conn, byte[] p_serialized, long p_id)
    throws SQLException, DbAccessException, IOException
    {
        PreparedStatement pst = p_conn.prepareStatement(UPDATE_SQL);
        InputStream in = new ByteArrayInputStream(p_serialized);
        pst.setBinaryStream(1, in, in.available());
        pst.setLong(2, p_id);
        pst.executeQuery();
        pst.close();
    }

    /* Since the record does not exist, we first create a new empty record; */
    /* then we continue as if updating an existing record. This is "safe" */
    /* because it is wrapped inside a transaction. */
    private static void insert(Connection p_conn, byte[] p_serialized, long p_id)
    throws SQLException, DbAccessException, IOException
    {
        PreparedStatement pst = p_conn.prepareStatement(INSERT_SQL);
	//for some reason, an id field was added to this table. just use ID=1
	//since there is only one task_classifier for the whole system currently
	    pst.setLong(1, p_id);
        pst.setLong(2, p_id);
        InputStream in = new ByteArrayInputStream(p_serialized);
        pst.setBinaryStream(3, in, in.available());
        pst.executeUpdate();
        pst.close();
        update(p_conn, p_serialized, p_id);
    }

    /* Serialize the given task classifier into an array of bytes. */
    private static byte[] serialize(TaskClassifier p_tc)
    throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p_tc);
        oos.flush();
        byte[] ba = baos.toByteArray();
        oos.close();
        baos.close();
        return ba;
    }

    /* De-serialize the given byte array into a task classifier. */
    private static TaskClassifier deserialize(byte[] bytes)
    throws ClassNotFoundException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        TaskClassifier tc = (TaskClassifier)ois.readObject();
        ois.close();
        bais.close();
        return tc;
    }
}

