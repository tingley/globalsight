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

// globalsight
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.PersistenceCommand;

/**
 * This class performs the queries and updates necessary to update the workflows
 * and source/target pages of a job to IMPORT_FAIL if necessary. It looks at the
 * target pages associated with the workflow and determines if the workflow
 * and/or the source page should fail import. Also if a source page is marked as
 * FAIL_IMPORT then all of its targets are marked too. This is used with the
 * reimport option REIMPORT_NEW_TARGETS
 */
public class UpdateWorkflowAndPageStatesCommand extends PersistenceCommand
{
    private static final String QUERY_FAILED_SOURCE_PAGES = "(select source_page_id from target_page where "
            + "workflow_iflow_instance_id = ? and state = '"
            + PageState.IMPORT_FAIL + "')";
    // query for all the workflow ids associated with a particular job
    private static final String QUERY_WORKFLOW_IDS = "select iflow_instance_id from workflow wtp where job_id = ?";

    // get the number of target pages that failed import within a workflow
    private static final String QUERY_FAILED_TARGET_PAGE_COUNT = "select count(*) from target_page where state = '"
            + PageState.IMPORT_FAIL + "' and workflow_iflow_instance_id = ?";

    // get the number of target pages total in a workflow
    private static final String QUERY_TARGET_PAGE_COUNT = "select count(*) from target_page where workflow_iflow_instance_id = ?";

    // update a workflow with the IMPORT_FAIL state
    private static final String UPDATE_WORKFLOW_STATE_FAIL = "update workflow set state = '"
            + Workflow.IMPORT_FAILED
            + "', timestamp = ? where iflow_instance_id = ?";

    // update a source page with the IMPORT_FAIL state
    private static final String UPDATE_SOURCE_PAGE_STATE_FAIL = "update source_page set state = '"
            + PageState.IMPORT_FAIL
            + "', timestamp = ? where id in "
            + QUERY_FAILED_SOURCE_PAGES;

    // update a target page with the IMPORT_FAIL state
    // only update if not already set to IMPORT_FAIL
    // no need to do it then
    private static final String UPDATE_TARGET_PAGES_STATE_FAIL = "update target_page set state = '"
            + PageState.IMPORT_FAIL
            + "', timestamp = ? where state != '"
            + PageState.IMPORT_FAIL
            + "' and source_page_id in "
            + QUERY_FAILED_SOURCE_PAGES;

    // query the request that should be updated with an import failure
    // only return the ones that aren't already marked as import failures
    private static final String QUERY_REQUEST_ID_BY_FAILED_SOURCE_PAGE = "select id from request where page_id in "
            + QUERY_FAILED_SOURCE_PAGES
            + " and type = 'EXTRACTED_LOCALIZATION_REQUEST'";

    private PreparedStatement m_psQueryWfIds; // query the ids of the workflows
                                              // in a job
    private PreparedStatement m_psQueryFailedCount; // query the failed target
                                                    // page count for a workflow
    private PreparedStatement m_psQueryCount; // query the target page count for
                                              // a workflow
    private PreparedStatement m_psQueryFailedRequestIds;

    private PreparedStatement m_psSourcePageFailedCommand;
    private PreparedStatement m_psTargetPageFailedCommand;
    private PreparedStatement m_psWorkflowFailedCommand;

    private Job m_job = null;
    // Hold the list of ids of requests that need to be marked as a failure.
    // The requests are not updated in this command.
    private List<Long> m_failedRequestIds = new ArrayList<Long>();
    private static final Logger c_logger = Logger
            .getLogger(UpdateWorkflowAndPageStatesCommand.class.getName());

    /**
     * Constructor to use when updating a collection of new target pages and the
     * old ones.
     */
    public UpdateWorkflowAndPageStatesCommand(Job p_job)
    {
        m_job = p_job;
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_psWorkflowFailedCommand != null)
                    m_psWorkflowFailedCommand.close();
                if (m_psSourcePageFailedCommand != null)
                    m_psSourcePageFailedCommand.close();
                if (m_psTargetPageFailedCommand != null)
                    m_psTargetPageFailedCommand.close();
                if (m_psQueryWfIds != null)
                    m_psQueryWfIds.close();
                if (m_psQueryFailedCount != null)
                    m_psQueryFailedCount.close();
                if (m_psQueryCount != null)
                    m_psQueryCount.close();
                if (m_psQueryFailedRequestIds != null)
                    m_psQueryFailedRequestIds.close();
            }
            catch (Exception e)
            {
            }
        }

    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_psQueryWfIds = p_connection.prepareStatement(QUERY_WORKFLOW_IDS);
        m_psQueryFailedCount = p_connection
                .prepareStatement(QUERY_FAILED_TARGET_PAGE_COUNT);
        m_psQueryCount = p_connection.prepareStatement(QUERY_TARGET_PAGE_COUNT);
        m_psQueryFailedRequestIds = p_connection
                .prepareStatement(QUERY_REQUEST_ID_BY_FAILED_SOURCE_PAGE);
        m_psWorkflowFailedCommand = p_connection
                .prepareStatement(UPDATE_WORKFLOW_STATE_FAIL);
        m_psSourcePageFailedCommand = p_connection
                .prepareStatement(UPDATE_SOURCE_PAGE_STATE_FAIL);
        m_psTargetPageFailedCommand = p_connection
                .prepareStatement(UPDATE_TARGET_PAGES_STATE_FAIL);
    }

    /**
     * Figure out what state to set the workflows in and possibly source page.
     * Depends on the grouping of target pages and their states.
     */
    public void setData() throws Exception
    {
        List wfIds = getWorkflowIds();
        for (Iterator i = wfIds.iterator(); i.hasNext();)
        {
            long wfId = ((Long) i.next()).longValue();

            // check if all the target pages of the workflow are marked as
            // import fail.
            int numOfFailedPages = numberOfFailedTargetPages(wfId);
            int totalNumOfPages = numberOfTargetPages(wfId);
            // if all the pages failed change the workflow state
            if (numOfFailedPages == totalNumOfPages)
            {
                m_psWorkflowFailedCommand.setDate(1,
                        new Date(System.currentTimeMillis()));
                m_psWorkflowFailedCommand.setLong(2, wfId);
                m_psWorkflowFailedCommand.addBatch();

                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Workflow "
                            + wfId
                            + " has failed import since all of "
                            + "its target pages have failed.  Changing the state.");                    
                }
            }
            // else if there were some error pages - change the source page
            // of each of the failed targets to FAIL.
            // there can't be a mixture of failed and successful pages within
            // a workflow.
            else if (numOfFailedPages > 0 && numOfFailedPages < totalNumOfPages)
            {
                m_psSourcePageFailedCommand.setDate(1,
                        new Date(System.currentTimeMillis()));
                m_psSourcePageFailedCommand.setLong(2, wfId);
                m_psSourcePageFailedCommand.addBatch();

                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Workflow "
                            + wfId
                            + " hasn't failed import but some of "
                            + " its source pages have since some of their target failed import.");                    
                }
                m_psTargetPageFailedCommand.setDate(1,
                        new Date(System.currentTimeMillis()));
                m_psTargetPageFailedCommand.setLong(2, wfId);
                m_psTargetPageFailedCommand.addBatch();

                // add the request ids that should fail to the member list
                addToFailedRequestIds(wfId);
            }
        }
    }

    public void batchStatements() throws Exception
    {
        // update the appropriate workflows with failures
        m_psWorkflowFailedCommand.executeBatch();
        m_psSourcePageFailedCommand.executeBatch();
        m_psTargetPageFailedCommand.executeBatch();
    }

    /**
     * Return an array of all the ids of the requests that should be marked as
     * failed. The ids are of type Long.
     */
    public List<Long> getFailedRequestsById()
    {
        return m_failedRequestIds;
    }

    /**
     * Get all the workflow ids that this job is associated with.
     */
    private List getWorkflowIds() throws PersistenceException
    {
        List<Long> wfIdsList = new ArrayList<Long>();
        try
        {
            m_psQueryWfIds.setLong(1, m_job.getId());
            ResultSet rs = m_psQueryWfIds.executeQuery();
            while (rs.next())
            {
                long wfId = rs.getLong(1);
                wfIdsList.add(new Long(wfId));
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }

        return wfIdsList;
    }

    /**
     * Returns the number of total target pages in a workflow.
     */
    private int numberOfTargetPages(long p_wfId) throws PersistenceException
    {
        int numOfTargetPages = 0;
        try
        {
            m_psQueryCount.setLong(1, p_wfId);
            ResultSet rs = m_psQueryCount.executeQuery();
            if (rs.next())
            {
                numOfTargetPages = rs.getInt(1);
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }

        return numOfTargetPages;
    }

    /**
     * Returns the number of failed total target pages in a workflow.
     */
    private int numberOfFailedTargetPages(long p_wfId)
            throws PersistenceException
    {
        int numOfFailedTargetPages = 0;
        try
        {
            m_psQueryFailedCount.setLong(1, p_wfId);
            ResultSet rs = m_psQueryFailedCount.executeQuery();
            if (rs.next())
            {
                numOfFailedTargetPages = rs.getInt(1);
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }

        return numOfFailedTargetPages;
    }

    /**
     * Adds the request ids that should be marked as fail to the data member
     * that stores failed request ids. This method looks at all requests
     * associated with a particular workflow.
     */
    private void addToFailedRequestIds(long p_wfId) throws PersistenceException
    {
        try
        {
            m_psQueryFailedRequestIds.setLong(1, p_wfId);
            ResultSet rs = m_psQueryFailedRequestIds.executeQuery();
            while (rs.next())
            {
                long failedRequestId = rs.getLong(1);
                m_failedRequestIds.add(new Long(failedRequestId));

                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Request "
                            + failedRequestId
                            + " must be marked as failed "
                            + " since its source page has been marked as failed.");                    
                }
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
    }
}
