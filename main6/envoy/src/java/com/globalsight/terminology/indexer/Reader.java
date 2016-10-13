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

import com.globalsight.terminology.indexer.ConceptXmlReaderThread;
import com.globalsight.terminology.indexer.LangXmlReaderThread;
import com.globalsight.terminology.indexer.TermXmlReaderThread;
import com.globalsight.terminology.indexer.TermReaderThread;

import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.util.ObjectPool;
import com.globalsight.util.SessionInfo;


/**
 * Implementation of the termbase reader. Reads entries from a
 * termbase.
 */
public class Reader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            Reader.class);

    static final private int BATCHSIZE = 100;

    private Termbase m_termbase;
    private SessionInfo m_session;

    private Thread m_thread = null;
    private ReaderResultQueue m_results;
    private ReaderResult m_result;

    private ObjectPool m_pool;

    public Reader(Termbase p_termbase, ObjectPool p_pool)
    {
        m_termbase = p_termbase;
        m_pool = p_pool;
    }

    /**
     * Retrieves the number of concepts from the database.
     */
    public int getConceptCount()
        throws Exception
    {
        return m_termbase.getEntryCount();
    }

    /**
     * Retrieves the number of non-empty languageGrps from the database.
     */
    public int getLanguageXmlCount(String p_language)
        throws Exception
    {
        return m_termbase.getLanguageXmlCount(p_language);
    }

    /**
     * Retrieves the number of terms in a language from the database.
     */
    public int getTermCount(String p_language)
        throws Exception
    {
        return m_termbase.getTermCount(p_language);
    }

    /**
     * Start reading termbase and producing entries.
     */
    public void startConceptXml()
    {
        startConceptXmlThread();
    }

    /**
     * Start reading termbase and producing entries.
     */
    public void startLanguageXml(String p_language)
    {
        startLangXmlThread(p_language);
    }

    /**
     * Start reading termbase and producing entries.
     */
    public void startTerm(String p_language)
    {
        startTermThread(p_language);
    }

    /**
     * Start reading termbase and producing entries.
     */
    public void startTermXml(String p_language)
    {
        startTermXmlThread(p_language);
    }

    /**
     * Stop reading and producing new entries.
     */
    public void stop()
    {
        stopThread();
    }

    /**
     * Lets the reader read in the next entry and returns true if an
     * entry is available, else false.
     */
    public boolean hasNext()
    {
        m_result = m_results.get();

        if (m_result != null)
        {
            return true;
        }

        return false;
    }

    /**
     * Retrieves the next ReaderResult, which is an Entry together
     * with a status code and error message.
     *
     * @see ReaderResult
     * @see Entry
     */
    public ReaderResult next()
    {
        return m_result;
    }

    //
    // PRIVATE METHODS
    //

    private void startConceptXmlThread()
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (BATCHSIZE);
            m_thread = new ConceptXmlReaderThread(
                m_results, m_termbase, m_pool);
            m_thread.start();
        }
    }

    private void startTermThread(String p_language)
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (BATCHSIZE);
            m_thread = new TermReaderThread(
                m_results, m_termbase, m_pool, p_language);
            m_thread.start();
        }
    }

    private void startTermXmlThread(String p_language)
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (BATCHSIZE);
            m_thread = new TermXmlReaderThread(
                m_results, m_termbase, m_pool, p_language);
            m_thread.start();
        }
    }

    private void startLangXmlThread(String p_language)
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (BATCHSIZE);
            m_thread = new LangXmlReaderThread(
                m_results, m_termbase, m_pool, p_language);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        if (m_thread != null)
        {
            m_results.consumerDone();

            m_results = null;
            m_thread = null;
        }
    }
}
