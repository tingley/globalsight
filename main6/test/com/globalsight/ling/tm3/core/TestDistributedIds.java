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
package com.globalsight.ling.tm3.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.DistributedId;

public class TestDistributedIds
{

    @BeforeClass
    public static void init() throws Exception
    {
        Connection conn = null;
        Statement s = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            s = conn.createStatement();
            s.execute("DELETE FROM tm3_id where tableName in ('id1', 'testMultipleThreads')");
            conn.commit();
        }
        finally
        {
            s.close();
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Test
    public void testDistributedIds() throws Exception
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            DistributedId id1 = new DistributedId("id1", 100);
            // Get some ids
            for (int i = 1; i <= 1000; i++)
            {
                long next = id1.getId(conn);
                assertEquals(i, next);
            }
            id1.destroy(conn);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    // Test multiple threads simultaneously accessing a single id
    @Test
    public void testTenThreads() throws Exception
    {
        testMultipleThreads(10, 10000, 100, 3000);
    }

    // @Test
    public void testHundredThreads() throws Exception
    {
        testMultipleThreads(100, 100000, 25, 5000);
    }

    public void testMultipleThreads(int numThreads, int numVals, int increment,
            int timeout) throws Exception
    {
        int[] values = new int[numVals];
        for (int i = 0; i < numVals; i++)
        {
            values[i] = -1;
        }
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < numThreads; i++)
        {
            threads.add(new Thread(new IDFetcher("testMultipleThreads", values,
                    i, numVals / numThreads, increment)));
            threads.get(i).run();
        }
        Thread.sleep(timeout); // Better way to do this?
        for (int i = 0; i < numVals; i++)
        {
            if (values[i] == -1)
            {
                fail("values[" + i + "] was never set");
            }
        }
        Connection conn = DbUtil.getConnection();
        conn.setAutoCommit(false);
        try
        {
            new DistributedId("testMultipleThreads").destroy(conn);
            conn.commit();
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    static class IDFetcher implements Runnable
    {
        private int[] values;
        private DistributedId id;
        private int self;
        private int count;

        public IDFetcher(String idName, int[] values, int self, int count,
                int increment)
        {
            this.values = values;
            this.id = new DistributedId(idName, increment);
            this.self = self;
            this.count = count;
        }

        @Override
        public void run()
        {
            Connection conn = null;
            try
            {
                conn = DbUtil.getConnection();
                for (int i = 0; i < count; i++)
                {
                    int next = (int) id.getId(conn);
                    if (values[next - 1] != -1)
                    {
                        fail("(Thread " + self + "): values[" + next
                                + "] already written by thread " + values[self]);
                    }
                    values[next - 1] = self;
                }
            }
            catch (Exception e)
            {
                fail("(Thread " + self + "): " + e.getMessage());
                throw new RuntimeException(e);
            }
            finally
            {
                DbUtil.silentReturnConnection(conn);
            }
        }

    }
}
