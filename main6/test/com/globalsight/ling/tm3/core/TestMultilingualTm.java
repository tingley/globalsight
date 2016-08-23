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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class TestMultilingualTm extends TM3Tests
{

    @BeforeClass
    public static void setup() throws Exception
    {
        init();
    }

    // Set up a bilingual TM for each test, start a fresh hibernate session, etc
    @Before
    public void beforeTest() throws Exception
    {
        Transaction tx = null;
        try
        {
            tx = HibernateUtil.getTransaction();
            System.out.println("Creating TM id " + currentTestId);
            TM3Tm<TestData> tm = manager.createMultilingualSharedTm(FACTORY, inlineAttrs(), 1);
            currentTestId = tm.getId();

            HibernateUtil.commit(tx);
        }
        catch (Exception e)
        {
            HibernateUtil.rollback(tx);
            throw e;
        }
    }

    @Test
    public void testCreateMultilingualTm() throws Exception
    {
        Transaction tx = null;
        try
        {
            tx = HibernateUtil.getTransaction();
            TM3Tm<TestData> tm2 = manager.getTm(FACTORY, currentTestId);
            assertNotNull(tm2);
            assertTrue(tm2 instanceof MultilingualSharedTm);

            cleanupTestDb(manager);

            TM3Tm<TestData> tm3 = manager.getTm(FACTORY, currentTestId);
            assertNull(tm3);
            HibernateUtil.commit(tx);
        }
        catch (Exception e)
        {
            HibernateUtil.rollback(tx);
            throw e;
        }
    }

}
