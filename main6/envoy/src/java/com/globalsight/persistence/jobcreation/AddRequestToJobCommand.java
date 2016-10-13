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

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.persistence.PersistenceCommand;

public class AddRequestToJobCommand extends PersistenceCommand
{
    private static Logger c_logger = Logger
            .getLogger(AddRequestToJobCommand.class.getName());

    private static final String m_updateRequest = "update request set job_id = ? where id = ?";

    private static final String m_updateJob = "update job set page_count = page_count + 1 where id = ?";

    private static final String m_selectWordCountJobId = "select distinct j.id from job j, request r, source_page sp where "
            + "j.id = r.job_id and sp.id = r.page_id and j.state = ? and "
            + "r.l10n_profile_id = ? and sp.data_source_type = ?";

    private static final String m_selectBatchJobId = "select distinct r.job_id from request r where r.batch_id = ? and r.job_id is not NULL";

    private static final String m_selectJobId = "select distinct j.id from job j where j.name = ?";

    private PreparedStatement m_psUpdateRequest;
    private PreparedStatement m_psUpdateJob;
    private PreparedStatement m_psBatchJobId;
    private PreparedStatement m_psJobId;
    private PreparedStatement m_psWordCountJobId;
    private RequestImpl m_request;
    private int m_noOfRowsUpdated = 0;
    private long m_jobId;

    public AddRequestToJobCommand(RequestImpl p_request)
    {
        m_request = p_request;
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            super.persistObjects(p_connection);
        }
        finally
        {
            try
            {
                if (m_psUpdateRequest != null)
                    m_psUpdateRequest.close();
                if (m_psUpdateJob != null)
                    m_psUpdateJob.close();
                if (m_psBatchJobId != null)
                    m_psBatchJobId.close();
                if (m_psJobId != null)
                    m_psJobId.close();
                if (m_psWordCountJobId != null)
                    m_psWordCountJobId.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_psUpdateRequest = p_connection.prepareStatement(m_updateRequest);
        m_psUpdateJob = p_connection.prepareStatement(m_updateJob);

        if (m_request.getBatchInfo() != null)
        {
            m_psBatchJobId = p_connection.prepareStatement(m_selectBatchJobId);
            m_psJobId = p_connection.prepareStatement(m_selectJobId);
        }
        else
        {
            m_psWordCountJobId = p_connection
                    .prepareStatement(m_selectWordCountJobId);
        }
    }

    public void setData() throws Exception
    {
        ResultSet rs = null;
        ResultSet rs2 = null;
        try
        {
            if (m_psWordCountJobId != null)
            {
                m_psWordCountJobId.setString(1, "PENDING");
                m_psWordCountJobId.setLong(2, m_request.getL10nProfile()
                        .getId());
                m_psWordCountJobId.setString(3, m_request.getDataSourceType());

                rs = m_psWordCountJobId.executeQuery();
                boolean isJob = false;
                if (rs.next())
                {
                    m_jobId = rs.getLong(1);
                    isJob = true;

                }

                if (isJob)
                {
                    m_psUpdateRequest.setLong(1, m_jobId);
                    m_psUpdateRequest.setLong(2, m_request.getId());

                    m_psUpdateJob.setLong(1, m_jobId);
                }
            }
            else
            {
                // m_psBatchJobId.setString(1, "BATCH_RESERVED");
                m_psBatchJobId.setString(1, m_request.getBatchInfo()
                        .getBatchId());

                rs = m_psBatchJobId.executeQuery();
                boolean isJob = false;
                if (rs.next())
                {
                    m_jobId = rs.getLong(1);
                    isJob = true;
                }
                else
                {
                    m_psJobId.setString(1, m_request.getBatchInfo()
                            .getBatchId());
                    rs2 = m_psJobId.executeQuery();
                    if (rs2.next())
                    {
                        m_jobId = rs2.getLong(1);
                        isJob = true;
                    }
                }

                if (isJob)
                {
                    m_psUpdateRequest.setLong(1, m_jobId);
                    m_psUpdateRequest.setLong(2, m_request.getId());

                    m_psUpdateJob.setLong(1, m_jobId);
                }
            }
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {
                }
            }

            if (rs2 != null)
            {
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    public void batchStatements() throws Exception
    {
        if (m_jobId > 0)
        {
            m_noOfRowsUpdated = m_psUpdateRequest.executeUpdate();
            if (m_noOfRowsUpdated > 0)
            {
                m_psUpdateJob.executeUpdate();
            }
        }
    }

    public int getNumberOfRowsUpdated()
    {
        return m_noOfRowsUpdated;
    }

    public long getJobId()
    {
        return m_jobId;
    }
}
