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

package com.globalsight.terminology.exporter;

import org.apache.log4j.Logger;

import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IReader;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.terminology.EntryFilter;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.util.SessionInfo;


/**
 * Implementation of the termbase reader. Reads entries from a
 * termbase.
 */
public class Reader
    implements IReader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            Reader.class);

    private ExportOptions m_options;
    private Termbase m_termbase;
    private SessionInfo m_session;
    private int m_entryCount;

    private ReaderThread m_thread = null;
    private ReaderResultQueue m_results;
    private ReaderResult m_result;

    public Reader(ExportOptions p_options, Termbase p_termbase,
        SessionInfo p_session)
    {
        m_termbase = p_termbase;
        m_session = p_session;

        setExportOptions(p_options);
    }

    /**
     * Sets new export options for this reader.
     */
    public void setExportOptions(ExportOptions p_options)
    {
        m_options = p_options;
    }

    /**
     * Analyzes export options and termbase and returns an updated
     * ExportOptions object with a status whether the options are
     * syntactically correct, the number of expected entries to be
     * exported, and column descriptors in case of CSV files.
     */
    public ExportOptions analyze()
    {
        m_options = doAnalyze();

        return m_options;
    }

    /**
     * Start reading termbase and producing entries.
     */
    public void start()
    {
        // Ensure the thread is running
        startThread();
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

    /**
     * Stop reading and producing new entries.
     */
    public void stop()
    {
        stopThread();
    }

    //
    // PRIVATE METHODS
    //

    private void startThread()
    {
        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)m_options;

        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (100);
            m_thread = new ReaderThread(m_results, options,
                m_termbase, m_session);
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

    /**
     * Retrieves the expected entry count from the database.
     */
    private ExportOptions doAnalyze()
    {
        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)m_options;

        try
        {
            String mode = options.getSelectMode();
            String lang = options.getSelectLanguage();
            int count = -1;

            EntryFilter filter = new EntryFilter(options.getFilterOptions());

            if (mode.equals(options.SELECT_ALL))
            {
                // TODO: add db filtering for better UI feedback
                count = m_termbase.getTermCount(lang, filter, true);

                m_options.setStatus(m_options.ANALYZED);
                m_options.setExpectedEntryCount(count);
            }
            else if (mode.equals(options.SELECT_LANGUAGE))
            {
                // TODO: add db filtering for better UI feedback
                count = m_termbase.getTermCount(lang, filter, true);

                m_options.setStatus(m_options.ANALYZED);
                m_options.setExpectedEntryCount(count);
            }
            else
            {
                String msg = "invalid select mode `" + mode + "'";

                CATEGORY.error(msg);

                m_options.setError(msg);
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("analysis error", ex);
            m_options.setError(ex.getMessage());
        }

        return m_options;
    }
}
