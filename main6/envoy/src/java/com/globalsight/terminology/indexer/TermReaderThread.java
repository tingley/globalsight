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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.util.ObjectPool;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.util.SqlUtil;

/**
 * Reads entries from a termbase and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class TermReaderThread extends Thread
{
    private static final Logger CATEGORY = Logger
            .getLogger(TermReaderThread.class);

    private ReaderResultQueue m_results;
    private Termbase m_termbase;
    private ObjectPool m_pool;
	private String m_language;

    //
    // Constructor
    //
    public TermReaderThread (ReaderResultQueue p_queue, 
		Termbase p_termbase, ObjectPool p_pool, String p_language)
    {
        m_results = p_queue;
        m_termbase = p_termbase;
        m_pool = p_pool;
		m_language = p_language;
    }

    //
    // Thread methods
    //
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run()
    {
        ReaderResult result = null;

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("TermReaderThread: start reading TB " +
                    m_termbase.getName());
            }

			com.globalsight.terminology.java.Termbase tbase = 
                HibernateUtil.get(com.globalsight.terminology.java.Termbase.class, m_termbase.getId());
            String hql = "from TbTerm tm where tm.tbLanguage.concept.termbase=:tbase " 
                + "and tm.tbLanguage.name=:planguage ";
            HashMap map = new HashMap();
            map.put("tbase", tbase);
            map.put("planguage", SqlUtil.quote(SqlUtil.quote(m_language)));
            
            Collection terms = HibernateUtil.search(hql, map);
            Iterator ite = terms.iterator();

			while (ite.hasNext())
			{
			    result = m_results.hireResult();
			    TbTerm tt = (TbTerm) ite.next();

				IndexObject object = (IndexObject)m_pool.getInstance();
				object.m_cid = tt.getTbLanguage().getConcept().getId();
				object.m_tid = tt.getId();
				object.m_text = tt.getTermContent();
			
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
			CATEGORY.error("Error reading terms", ignore);

			if (result == null)
			{
				result = m_results.hireResult();
			}

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
                CATEGORY.debug("TermReaderThread: done.");
            }
        }
    }
}
