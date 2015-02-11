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

import org.apache.log4j.Logger;

import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.util.SessionInfo;


import java.util.*;

/**
 * Reads entries from a termbase and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 * 
 *@deprecated This thread is not in use any more, deprecate it.  
 */
public class ReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ReaderThread.class);

    private ReaderResultQueue m_results;
    private Termbase m_termbase;
    private SessionInfo m_session;

    //
    // Constructor
    //
    public ReaderThread (ReaderResultQueue p_queue, Termbase p_termbase,
        SessionInfo p_session)
    {
        m_results = p_queue;
        m_termbase = p_termbase;
        m_session = p_session;
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
                CATEGORY.debug("ReaderThread: start reading TB " +
                    m_termbase.getName());
            }

            // Simple, not optimal: get all entry ids, then read each
            // entry and output.
            ArrayList entryIds = m_termbase.getEntryIds();

            for (int i = 0; i < entryIds.size(); ++i)
            {
                result = m_results.hireResult();

                long entryId = ((Long)entryIds.get(i)).longValue();
                String entryXml;

                try
                {
                    entryXml = m_termbase.getEntry(entryId, m_session);
                    result.setResultObject(entryXml);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("ReaderThread: new result " + (i+1));
                    }
                }
                catch (TermbaseException ex)
                {
                    result.setError(ex.toString());

                    CATEGORY.error("ReaderThread: error " + (i+1), ex);
                }

                boolean done = m_results.put(result);
                result = null;

                if (done)
                {
                    // reader died, cleanup & return.
                    return;
                }
            }
        }
        catch (Throwable ignore)
        {
            // Should not happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            CATEGORY.error("unexpected error", ignore);
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
                CATEGORY.debug("ReaderThread: done.");
            }
        }
    }
}

