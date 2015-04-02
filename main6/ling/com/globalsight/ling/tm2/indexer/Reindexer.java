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
package com.globalsight.ling.tm2.indexer;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.util.progress.ProcessMonitor;

/**
 * Reindexer class is responsible for reindexing TM.
 * 
 * Note that since this runs as its own thread, it takes ownership of its
 * session and cleans it up when it's done.
 */

public class Reindexer extends MultiCompanySupportedThread implements
        ProcessMonitor, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger c_logger = Logger.getLogger(Reindexer.class);

    private Collection<Tm> m_tms;

    private int m_segTotalCount = 0;

    private volatile int m_counter = 0;

    private boolean m_done = false;

    private boolean m_error = false;

    private String m_message;

    private boolean m_interrupted = false;

    // Injected Session to use while reindexing
    private Session m_session;

    private boolean indexTarget = false;

    /**
     * Reindexer constructor.
     * 
     * @param p_tmId
     *            TM id to index. If -1, all TMs in the system will be indexed.
     */
    public Reindexer(Session session, Collection<Tm> tms)
            throws LingManagerException
    {
        super();
        this.m_session = session;
        this.m_tms = tms;
    }

    /** Indexing body */
    public void run()
    {
        super.run();

        try
        {
            // Calculate the count of all segments to be reindexed
            for (Tm tm : m_tms)
            {
                m_segTotalCount += tm.getSegmentTmInfo()
                        .getSegmentCountForReindex(tm);
            }

            boolean userCanceled = false;

            // TODO: pass the connection/session
            for (Tm tm : m_tms)
            {
                m_message = getStringFormattedFromBundle("lb_tm_indexing_msg",
                        "Indexing \"{0}\"...", tm.getName());
                boolean success = tm.getSegmentTmInfo().reindexTm(tm, this,
                        indexTarget);
                if (!success)
                {
                    // canceled
                    m_message = getStringFromBundle(
                            "lb_tm_index_cancel_by_uses",
                            "Indexing has been cancelled by user request.");
                    userCanceled = true;
                    break;
                }
            }

            if (userCanceled == false)
            {
                synchronized (this)
                {
                    if (m_counter < m_segTotalCount)
                    {
                        // GBS-3901, counter not equals total because of ignored
                        // bad data. Set to total to make the percentage
                        // correct.
                        m_counter = m_segTotalCount;
                    }
                    m_message = getStringFromBundle(
                            "lb_tm_index_finish_success",
                            "Indexing has successfully finished.");
                    m_done = true;
                }
            }
        }
        catch (Throwable e)
        {
            c_logger.error("An error occured while indexing", e);

            synchronized (this)
            {
                m_message = getStringFromBundle("lb_tm_index_error_occur",
                        "An error occured while indexing")
                        + ": "
                        + e.getMessage();
                m_done = true;
                m_error = true;
            }
        }
        finally
        {
            m_done = true;
        }
    }

    public boolean getInterrupted()
    {
        return m_interrupted;
    }

    /**
     * Return the count of reindexed segments.
     * 
     * @return
     */
    public int getCounter()
    {
        return m_counter;
    }

    public void incrementCounter(int howMany)
    {
        m_counter += howMany;
    }

    /**
     * Return the completion percentage (0-100).
     */
    public int getPercentage()
    {
        int percent = 0; // returns 100 when m_segTotalCount == 0
        if (m_segTotalCount != 0)
        {
            percent = Math.round(((float) m_counter / m_segTotalCount) * 100);
        }

        return percent;
    }

    // ProcessMonitor methods

    /**
     * Method for getting a status if the process has finished (either
     * successfully, with error or canceled by user request)
     */
    public boolean hasFinished()
    {
        synchronized (this)
        {
            return m_done;
        }
    }

    /**
     * This must be called after setResourceBundle and before start to avoid a
     * window where getReplacingMessage returns null.
     */
    public void initReplacingMessage()
    {
        this.m_message = getStringFromBundle("lb_tm_index_prepare",
                "Preparing to reindex.");
    }

    /**
     * Method for getting a message that replaces an existing message in UI.
     * This is typically used to get a message that shows the current status
     */
    public String getReplacingMessage()
    {
        synchronized (this)
        {
            return m_message;
        }
    }

    /**
     * Method for getting messages that are appended in UI. This is typically
     * used to get messages that cumulatively displayed e.g. items so far done.
     */
    public List getAppendingMessages()
    {
        return null;
    }

    /**
     * Returns true if an error has occured and the replacing message contains
     * error message.
     */
    public boolean isError()
    {
        synchronized (this)
        {
            return m_error;
        }
    }

    synchronized public void cancelProcess()
    {
        m_interrupted = true;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("\r\n");
        sb.append("m_tms = ").append(m_tms).append("\r\n");
        sb.append("m_segTotalCount = ").append(m_segTotalCount).append("\r\n");
        sb.append("m_counter = ").append(m_counter).append("\r\n");
        sb.append("m_done = ").append(m_done).append("\r\n");
        sb.append("m_message = ").append(m_message).append("\r\n");
        sb.append("m_interrupted = ").append(m_interrupted).append("\r\n");

        return sb.toString();
    }

    public boolean isIndexTarget()
    {
        return indexTarget;
    }

    public void setIndexTarget(boolean indexTarget)
    {
        this.indexTarget = indexTarget;
    }

}
