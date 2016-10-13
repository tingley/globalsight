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

package com.globalsight.persistence.jobcreation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.persistence.PersistenceCommand;

/**
 * Given a list of requests that have failed to import, removes the requests and
 * the source page objects that are still associated with them. Since the source
 * pages have failed to import, they don't have any other data associated with
 * them (tuv, leverage matches etc).
 */
public class RemoveRequestFromJobCommand extends PersistenceCommand
{
    private static Logger c_logger = Logger
            .getLogger(RemoveRequestFromJobCommand.class.getName());

    private static final String DELETE_REQUEST_SQL = "delete from request where id = ?";

    private static final String DELETE_SOURCE_PAGE_SQL = "delete from source_page where id = ?";

    private static final String UPDATE_JOB_PAGE_COUNT_SQL = "update job set page_count = page_count - ? where id = ?";

    private static final String FIND_SOURCE_PAGE_DEPENDENCY_SQL = "select lg_id from source_page_leverage_group where sp_id = ?";

    private static final String UPDATE_REQUEST_PAGE_COUNT = "update request set BATCH_PAGE_COUNT = BATCH_PAGE_COUNT - ? where JOB_ID = ?";

    private PreparedStatement m_psRequest;
    private PreparedStatement m_psSourcepage;
    private PreparedStatement m_psUpdateJob;
    private PreparedStatement m_psCheckDependency;
    private PreparedStatement m_psUpdatePageCount;

    // holds the list of commands to remove dependancies
    private ArrayList m_pageDependencyCommands;
    private Collection m_requests;
    private long m_jobId;

    // store for the dependency calls
    private Connection m_connection;

    //
    // CONSTRUCTOR
    //

    public RemoveRequestFromJobCommand(long p_jobId, Collection p_requests)
    {
        m_jobId = p_jobId;
        m_requests = p_requests;
        // Holds all the DeleteSourcePageDependanciesCommands for
        // all the pages that have dependencies.
        // The maximum will be the same number as requests.
        m_pageDependencyCommands = new ArrayList(m_requests.size());
    }

    //
    // INTERFACE METHODS
    //

    /**
     * Overwrites PersistenceObject.persistObjects and adds cleanup calls.
     */
    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            m_connection = p_connection;
            super.persistObjects(p_connection);
        }
        finally
        {
            try
            {
                if (m_psSourcepage != null)
                    m_psSourcepage.close();
                if (m_psRequest != null)
                    m_psRequest.close();
                if (m_psUpdateJob != null)
                    m_psUpdateJob.close();
                if (m_psCheckDependency != null)
                    m_psCheckDependency.close();
                if (m_psUpdatePageCount != null)
                    m_psUpdatePageCount.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_psSourcepage = p_connection.prepareStatement(DELETE_SOURCE_PAGE_SQL);
        m_psRequest = p_connection.prepareStatement(DELETE_REQUEST_SQL);
        m_psUpdateJob = p_connection
                .prepareStatement(UPDATE_JOB_PAGE_COUNT_SQL);
        m_psCheckDependency = p_connection
                .prepareStatement(FIND_SOURCE_PAGE_DEPENDENCY_SQL);
        m_psUpdatePageCount = p_connection
                .prepareStatement(UPDATE_REQUEST_PAGE_COUNT);
    }

    public void setData() throws Exception
    {
        RequestImpl request = null;
        int numberOfErrorRequests = m_requests.size();
        for (Iterator it = m_requests.iterator(); it.hasNext();)
        {
            request = (RequestImpl) it.next();

            m_psSourcepage.setLong(1, request.getSourcePageId());
            m_psSourcepage.addBatch();

            m_psRequest.setLong(1, request.getId());
            m_psRequest.addBatch();

            // query for the source page and check
            // if it has dependencies
            // create a command to delete the dependencies and
            // add to the list
            m_psCheckDependency.setLong(1, request.getSourcePageId());
            ResultSet rs = m_psCheckDependency.executeQuery();
            // if there is atleast one in the resultset - there is a dependency
            if (rs.next())
            {
                DeleteSourcePageDependenciesCommand delSPCommand = new DeleteSourcePageDependenciesCommand(
                        request.getSourcePageId());
                m_pageDependencyCommands.add(delSPCommand);
            }
        }
        if (request != null)
        {
            m_psUpdateJob.setInt(1, numberOfErrorRequests);
            m_psUpdateJob.setLong(2, m_jobId);

            m_psUpdatePageCount.setInt(1, numberOfErrorRequests);
            m_psUpdatePageCount.setLong(2, m_jobId);
        }
    }

    public void batchStatements() throws Exception
    {
        // first remove all dependancies to the
        for (int i = 0; i < m_pageDependencyCommands.size(); i++)
        {
            ((DeleteSourcePageDependenciesCommand) m_pageDependencyCommands
                    .get(i)).persistObjects(m_connection);
        }

        m_psRequest.executeBatch();
        m_psSourcepage.executeBatch();
        m_psUpdateJob.executeUpdate();
        m_psUpdatePageCount.executeUpdate();
    }
}
