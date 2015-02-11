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

package com.globalsight.terminology.indexer;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.util.ObjectPool;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.java.TbConcept;

/**
 * Reads entries from a termbase and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class ConceptXmlReaderThread extends Thread
{
    private static final Logger CATEGORY = Logger
            .getLogger(ConceptXmlReaderThread.class);

    private ReaderResultQueue m_results;
    private Termbase m_termbase;
    private ObjectPool m_pool;

    //
    // Constructor
    //
    public ConceptXmlReaderThread (ReaderResultQueue p_queue,
        Termbase p_termbase, ObjectPool p_pool)
    {
        m_results = p_queue;
        m_termbase = p_termbase;
        m_pool = p_pool;
    }

    //
    // Thread methods
    //
    public void run()
    {
        ReaderResult result = null;

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("ConceptXmlReaderThread: start reading TB " +
                    m_termbase.getName());
            }

            String hql = "select tc from TbConcept tc where tc.termbase.id=" 
                + m_termbase.getId();
            Iterator ite = HibernateUtil.search(hql).iterator();

            while(ite.hasNext()) {
                result = m_results.hireResult();
                IndexObject object = (IndexObject)m_pool.getInstance();
                TbConcept tc = (TbConcept)ite.next();
                object.m_cid = tc.getId();
                object.m_tid = 0;
                object.m_text = tc.getXml();
                
                result.setResultObject(object);

                boolean done = m_results.put(result);
                result = null;

                if (done)
                {
                    // reader died, cleanup & return.
                    break;
                }
            }
        }
        catch (Throwable ignore)
        {
            result.setError(ignore.toString());
            m_results.put(result);
            result = null;
        }
        finally
        {
            if (result != null)
            {
                m_results.fireResult(result);
            }

            m_results.producerDone();
            m_results = null;

            HibernateUtil.closeSession();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("ConceptXmlReaderThread: done.");
            }
        }
    }
}
