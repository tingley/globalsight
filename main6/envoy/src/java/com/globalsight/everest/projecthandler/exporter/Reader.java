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

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.exporter.ReaderThread;

import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IReader;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.everest.projecthandler.Project;

import com.globalsight.util.SessionInfo;


// should be in common util package
import com.globalsight.terminology.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Implementation of the export reader. Reads entries from the DB.
 */
public class Reader
    implements IReader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            Reader.class);

    private Project m_project;
    private ExportOptions m_options;
    private SessionInfo m_session;
    private int m_entryCount;

    private ReaderThread m_thread = null;
    private ReaderResultQueue m_results;
    private ReaderResult m_result;

    public Reader(ExportOptions p_options, Project p_project,
        SessionInfo p_session)
    {
        m_project = p_project;
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
     * Analyzes export options and TM and returns an updated
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
        com.globalsight.everest.projecthandler.exporter.ExportOptions options =
            (com.globalsight.everest.projecthandler.exporter.ExportOptions)m_options;

        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (100);
            m_thread = new ReaderThread(m_results, options,
                m_project, m_session);
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
        com.globalsight.everest.projecthandler.exporter.ExportOptions options =
            (com.globalsight.everest.projecthandler.exporter.ExportOptions)m_options;

        // TODO

        try
        {
            String mode = options.getSelectMode();
            String filter = options.getSelectFilter();
            int count = -1;

            if (mode.equals(options.SELECT_ALL))
            {
                count = getEntryCount();

                m_options.setStatus(m_options.ANALYZED);
                m_options.setExpectedEntryCount(count);
            }
            else if (mode.equals(options.SELECT_FILTERED))
            {
                count = getFilteredEntryCount(filter);

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
        catch (/*Exporter*/Exception ex)
        {
            CATEGORY.error("analysis error", ex);

            m_options.setError(ex.getMessage());
        }

        return m_options;
    }

    //
    // Database Helpers
    //

    /**
     * Gets the overall number of entries to export.
     */
    private int getEntryCount()
        throws SQLException
    {
    	/**
    	 * TODO The business logic is not clear at here. Need to implement at future.
    	 */
    	
    	return 42;
    	
//        int result = 0;
//
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rset = null;
//
//        try
//        {
//            conn = SqlUtil.hireConnection();
//            stmt = conn.createStatement();
//
//            /* TODO
//            rset = stmt.executeQuery();
//
//            if (rset.next())
//            {
//                result = rset.getInt(1);
//            }
//            */
//            result = 42;
//
//            conn.commit();
//        }
//        catch (SQLException e)
//        {
//            try { conn.rollback(); } catch (Throwable ignore) {}
//            CATEGORY.warn("can't read data", e);
//
//            throw e;
//        }
//        finally
//        {
//            try
//            {
//                if (rset != null) rset.close();
//                if (stmt != null) stmt.close();
//            }
//            catch (Throwable ignore) {}
//
//            SqlUtil.fireConnection(conn);
//        }
//
//        return result;
    }

    /**
     * Gets the number of TUs that have a TUV in a given language.
     *
     * @param p_locale a locale string like "en_US".
     */
    private int getFilteredEntryCount(String p_filter)
        throws Exception
    {
        int result = 0;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();

            /* TODO
            rset = stmt.executeQuery();

            if (rset.next())
            {
                result = rset.getInt(1);
            }
            */
            result = 42;

            conn.commit();
        }
        catch (/*SQL*/Exception e)
        {
            try { conn.rollback(); } catch (Throwable ignore) {}
            CATEGORY.warn("can't read TM data", e);

            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

            SqlUtil.fireConnection(conn);
        }

        return result;
    }
}
