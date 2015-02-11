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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.exporter.ExportOptions;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.command.EntryOperation;
import com.globalsight.terminology.command.EntryOperationImpl;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryFilter;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.util.SessionInfo;

/**
 * Reads entries from a termbase and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class ReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ReaderThread.class);

    private ReaderResultQueue m_results;
    private ExportOptions m_options;
    private Termbase m_termbase;
    private SessionInfo m_session;

    private EntryFilter m_filter = null;

    //
    // Constructor
    //
    public ReaderThread (ReaderResultQueue p_queue, ExportOptions p_options,
        Termbase p_termbase, SessionInfo p_session)
    {
        m_results = p_queue;
        m_options = p_options;
        m_termbase = p_termbase;
        m_session = p_session;

        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)m_options;
        m_filter = new EntryFilter(options.getFilterOptions());
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
            ArrayList entryIds = getEntryIds();
            EntryOperation eo = new EntryOperationImpl();
            
            com.globalsight.terminology.exporter.ExportOptions options =
                (com.globalsight.terminology.exporter.ExportOptions)m_options;
            String selectMode = options.getSelectMode();
            String selectLanguages = "";
            if (!selectMode.equals(options.SELECT_ALL))
            {
            	selectLanguages = options.getSelectLanguage();
            }

            for (int i = 0; i < entryIds.size(); ++i)
            {
                result = m_results.hireResult();

                long entryId = ((Long)entryIds.get(i)).longValue();
                String entryXml;

                try
                {
                	if (m_options.getFileType() != null
                            && m_options
                                    .getFileType()
                                    .equalsIgnoreCase(
                                            com.globalsight.terminology.exporter.ExportOptions.TYPE_TBX))
                    {
                        entryXml = eo.getTbxEntryForExport(m_termbase.getId(),
                                entryId, 0, selectLanguages, "", m_session);
                    }
                    else
                    {
                        entryXml = eo.getEntryForExport(m_termbase.getId(),
                                entryId, 0, selectLanguages, "", m_session);
                    }

                    if (!applyFilter(entryXml))
                    {
                        // Object does not satisfy filter conditions. Skip.
                        m_results.fireResult(result);
                        continue;
                    }

                    if(entryXml.length() == 0)
                    	continue;

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

    //
    // PRIVATE METHODS
    //

    private boolean applyFilter(String p_entryXml)
    {
        if (m_filter.isSwFiltering())
        {
            // Mon Jan 24 15:38:07 2005 CvdL: too bad, we parse the entry
            // twice: here and again in the XxxWriter classes.
            try
            {
                Entry entry = new Entry(p_entryXml);

                return m_filter.evaluateSwFilter(entry);
            }
            catch (Throwable ex)
            {
                CATEGORY.error("internal error when filtering: ", ex);
            }
        }

        return true;
    }

    private ArrayList getEntryIds() throws TermbaseException
    {
        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)m_options;

        String selectMode = options.getSelectMode();
        String selectLanguage = options.getSelectLanguage();

        if (selectMode.equals(options.SELECT_ALL))
        {
            return m_termbase.getEntryIds(m_filter);
        }
        else
        {
            return m_termbase.getEntryIds(selectLanguage, m_filter);
        }
    }
}
