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
package com.globalsight.persistence.pageimport.delayedimport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.ling.tm2.persistence.DbUtil;

public class DelayedImportQuery
{
    private String m_selectJobOfPage = "select j.* from job j, request r "
            + "where j.id = r.job_id and r.page_id = ? ";
    private String m_selectJobIdOfPage = "select j.id from job j, request r "
            + "where j.id = r.job_id and r.page_id =  ?";
    private PreparedStatement m_psJobOfPage = null;
    private PreparedStatement m_psJobIdOfPage = null;

    public DelayedImportQuery()
    {
    }

    public long findJobIdOfPage(long p_sourcePageId)
            throws PersistenceException
    {
        Connection connection = null;
        ResultSet rs = null;
        long jobId = 0;
        try
        {
            connection = DbUtil.getConnection();
            m_psJobIdOfPage = connection.prepareStatement(m_selectJobIdOfPage);
            m_psJobIdOfPage.setLong(1, p_sourcePageId);
            rs = m_psJobIdOfPage.executeQuery();
            if (rs.next())
            {
                jobId = rs.getLong(1);
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
        	DbUtil.silentClose(rs);
        	DbUtil.silentClose(m_psJobIdOfPage);
        	DbUtil.silentReturnConnection(connection);
        }

        return jobId;
    }

    public Job findJobOfPage(long p_sourcePageId) throws PersistenceException
    {
        Connection connection = null;
        ResultSet rs = null;
        Job job = null;
        try
        {
            connection = DbUtil.getConnection();
            m_psJobOfPage = connection.prepareStatement(m_selectJobOfPage);
            m_psJobOfPage.setLong(1, p_sourcePageId);
            rs = m_psJobOfPage.executeQuery();
            job = processResultSet(rs);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
        	DbUtil.silentClose(rs);
        	DbUtil.silentClose(m_psJobOfPage);
        	DbUtil.silentReturnConnection(connection);
        }

        return job;
    }

    private JobImpl processResultSet(ResultSet p_rs) throws Exception
    {
        JobImpl job = null;
        if (p_rs.next())
        {
            job = new JobImpl();
            job.setId(p_rs.getLong(1));
            job.setName(p_rs.getString(2));
            job.setState(p_rs.getString(3));
            job.setCreateDate(p_rs.getDate(4));
            job.setPriority(p_rs.getInt(5));
            boolean isWordCountReached = false;
            String wordCountReached = p_rs.getString(6);
            if (wordCountReached.equals("Y"))
            {
                isWordCountReached = true;
            }
            else if (wordCountReached.equals("N"))
            {
                isWordCountReached = false;
            }
            job.setWordCountReached(isWordCountReached);
        }
        return job;
    }
}
