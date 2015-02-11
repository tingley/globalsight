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
package com.globalsight.everest.projecthandler.exporter;


import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.exporter.ExporterException;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.globalsight.util.SessionInfo;


/**
 * Reads entries from a database and produces result objects by
 * putting ReaderResult objects into a ReaderResultQueue.
 */
public class ReaderThread
    extends Thread
{
    static private final Logger s_logger =
        Logger.getLogger(
            ReaderThread.class);

    /**
     * When reading data from the database, read BATCH_READ_SIZE
     * entries at a time.
     */

    private ReaderResultQueue m_results;
    private ExportOptions m_options;
    private Project m_project;

    //
    // Constructor
    //
    public ReaderThread (ReaderResultQueue p_queue, ExportOptions p_options,
        Project p_project, SessionInfo p_session)
    {
        m_results = p_queue;
        m_options = p_options;
        m_project = p_project;
        this.setName("Project Data Reader Thread");
    }

    //
    // Thread methods
    //
    public void run()
    {
        ReaderResult result = null;

        try
        {
            s_logger.debug("ReaderThread: start reading project data");

            ArrayList entries = getEntries();
            int size = entries.size();
            
            for (int i = 0; i < size ; i++)
            {
                result = m_results.hireResult();
                
                // Output each object to result queue.
                try
                {
                    ReservedTime entry = (ReservedTime)entries.get(i);

                    if (entry == null)
                    {
                        break;
                    }

                    result.setResultObject(entry);
                }
                catch (Exception ex)
                {
                    result.setError(ex.toString());

                    s_logger.error("ReaderThread: error in batch " +i, ex);
                }

                boolean done = m_results.put(result);
                result = null;

                if (done)
                {
                    // writer died, cleanup & return.
                    return;
                }
            }
        }
        catch (Throwable ignore)
        {
            // Should not happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            s_logger.error("unexpected error", ignore);
        }
        finally
        {
            if (result != null)
            {
                m_results.fireResult(result);
            }

            m_results.producerDone();
            m_results = null;

            s_logger.debug("ReaderThread: done.");
        }
    }

    //
    // PRIVATE METHODS
    //

    /**
     * Retrieves a list of TU ids (as Long objects) that need to be
     * exported from the TM.
     */
    private ArrayList getEntries()
        throws ExporterException
    {
        ExportOptions options = m_options;

        
        try
        {
            String mode = options.getSelectMode();
            String filter = options.getSelectFilter();

            if (mode.equals(ExportOptions.SELECT_ALL))
            {
                return getAllEntries();
            }
            else if (mode.equals(ExportOptions.SELECT_FILTERED))
            {
                return getFilteredEntries(filter);
            }
            else
            {
                String msg = "invalid select mode `" + mode + "'";

                s_logger.error(msg);

                m_options.setError(msg);
            }
        }
        catch (Exception ex)
        {
            s_logger.error("export error", ex);

            m_options.setError(ex.getMessage());
        }

        return null;
    }

    //
    // Database Helpers
    //

    /*
     * Retrieves all activity/event enteries of a project's users.
     */
    private ArrayList getAllEntries()
        throws Exception
    {
        String usernames = getUserIds();
        Collection val = getReservedTimesForProjectUsers(usernames);
        
        return new ArrayList(val);
    }

    /*
     * Retrieves all activity/event enteries of a particular user.
     */
    private ArrayList getFilteredEntries(String p_filter)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("'");
        sb.append(p_filter);
        sb.append("'");
        
        Collection val = getReservedTimesForProjectUsers(sb.toString());
        
        return new ArrayList(val);
    }

    /*
     * Get reserved times for the project users. 
     */
    private Collection getReservedTimesForProjectUsers(String p_usernames)
        throws Exception
    {
        try
        {
            if (p_usernames == null)
            {
                return null;
            }
            
            String sql = createStatement(p_usernames);
            HibernateUtil.searchWithSql(sql, null, ReservedTime.class);
            
            return HibernateUtil.searchWithSql(sql, null, ReservedTime.class);            
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get reserved times for: "+p_usernames, e);
            throw e;
        }
    }

    /*
     * Get the project's user ids as a comma separated string.
     */
    private String getUserIds()
    {
        Object[] userIds = m_project.getUserIds().toArray();
        int length = userIds.length;
        

        if (length <= 0)
        {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < length ; i++)
        {
            sb.append("'");
            sb.append(userIds[i]);
            sb.append("'");
            if (i < length - 1)
            {
                sb.append(",");
            }
        }
        
        return sb.toString();
    }

    /*
     * Create an sql statement for getting reserved times for the give user ids. 
     */
    private String createStatement(String p_usernames)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("select rt.* from RESERVED_TIME rt, USER_CALENDAR uc ");
        sb.append("where rt.USER_CALENDAR_ID = uc.ID and ");
        sb.append("uc.OWNER_USER_ID in(");
        sb.append(p_usernames);
        sb.append(") order by uc.OWNER_USER_ID");

        return sb.toString();
    }
}
