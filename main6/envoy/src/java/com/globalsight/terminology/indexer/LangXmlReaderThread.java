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

import com.globalsight.util.ObjectPool;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.util.SqlUtil;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;

/**
 * Reads entries from a termbase and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class LangXmlReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            LangXmlReaderThread.class);

    private ReaderResultQueue m_results;
    private Termbase m_termbase;
    private ObjectPool m_pool;
    private String m_language;

    //
    // Constructor
    //
    public LangXmlReaderThread (ReaderResultQueue p_queue,
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
    public void run()
    {
        ReaderResult result = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("LangXmlReaderThread: start reading TB " +
                    m_termbase.getName());
            }

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            // Retrieve language level XML (only if set)
            stmt = conn.createStatement();
            rset = stmt.executeQuery(
                "select CID, LID, XML from TB_LANGUAGE " +
                "where TBid=" + m_termbase.getId() +
                "  and Name = '" + SqlUtil.quote(m_language) + "'" +
                "  and not XML is null");

            while (rset.next())
            {
                result = m_results.hireResult();

                IndexObject object = (IndexObject)m_pool.getInstance();
                object.m_cid = rset.getLong(1);
                object.m_tid = rset.getLong(2);
                object.m_text = SqlUtil.readClob(rset, "XML");

                result.setResultObject(object);

                boolean done = m_results.put(result);
                result = null;

                if (done)
                {
                    // reader died, cleanup & return.
                    break;
                }
            }

            conn.commit();
        }
        catch (Throwable ignore)
        {
            try { conn.rollback(); } catch (Exception ex) {}

            CATEGORY.error("Error reading Concept XML", ignore);

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
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);

            if (result != null)
            {
                m_results.fireResult(result);
            }

            m_results.producerDone();
            m_results = null;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("LangXmlReaderThread: done.");
            }
        }
    }
}
