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

import java.util.TimerTask;
import java.sql.Connection;

import org.apache.log4j.Logger;


/**
 * Used to close unused connection.
 * 
 * <p>
 * When a connection return to pool, a task will be created, later, the task
 * will try to close the connection. The time according to
 * <code> com.globalsight.diplomat.util.database.ConnectionPool.MAX_CONNECTION_WAIT_TIME</code>,
 * and it can be set in <code>properties/db_connection.properties</code>.
 */
public class CloseConnectionTask extends TimerTask
{
    private ConnectionPool pool;
    private Connection conn;

    private static final Logger CATEGORY = Logger
            .getLogger(CloseConnectionTask.class.getName());

    public CloseConnectionTask(ConnectionPool pool, Connection conn)
    {
        this.pool = pool;
        this.conn = conn;
    }

    public void run()
    {
        try
        {
            pool.closeUnallocatedConnection(conn); //Try to close the connection.
        }
        catch (Throwable e)
        {
            CATEGORY.error("Cannot close connection", e);
        }
    }
}
