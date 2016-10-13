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
import java.util.List;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;

public class JobCreationQuery
{
    static private final String s_requestListQuery = "select r1.* from job j1, request r1 where "
            + "j1.id = r1.job_id and j1.id = ?";

    private PreparedStatement m_ps;

    public JobCreationQuery()
    {
    }

    /**
     * Returns a List of Request objects that belong to the given job. This
     * method may return an empty list, but never null.
     */
    public List getRequestListByJobId(long p_jobId) throws PersistenceException
    {
        Connection connection = null;
        ArrayList requestList = new ArrayList();
        ResultSet rs = null;

        try
        {
            connection = PersistenceService.getInstance()
                    .getConnectionForImport();

            m_ps = connection.prepareStatement(s_requestListQuery);
            m_ps.setLong(1, p_jobId);

            rs = m_ps.executeQuery();

            while (rs.next())
            {
                RequestImpl r = new RequestImpl();

                r.setId(rs.getLong("ID"));
                String type = rs.getString("TYPE");

                // s_logger.debug("The request type is " + type);

                if (type.equals("EXTRACTED_LOCALIZATION_REQUEST"))
                {
                    r.setType(Request.EXTRACTED_LOCALIZATION_REQUEST);
                }
                else if (type.equals("UNEXTRACTED_LOCALIZATION_REQUEST"))
                {
                    r.setType(Request.UNEXTRACTED_LOCALIZATION_REQUEST);
                }
                else if (type.equals("REQUEST_WITH_CXE_ERROR"))
                {
                    r.setType(Request.REQUEST_WITH_CXE_ERROR);
                }
                else if (type.equals("REQUEST_WITH_IMPORT_ERROR"))
                {
                    r.setType(Request.REQUEST_WITH_IMPORT_ERROR);
                }

                r.setSourcePageId(rs.getLong("PAGE_ID"));

                String batchId = rs.getString("BATCH_ID");
                if (isBatch(batchId))
                {
                    long pageCount = rs.getLong("BATCH_PAGE_COUNT");
                    long pageNumber = rs.getLong("BATCH_PAGE_NUMBER");
                    long docPageCount = rs.getLong("BATCH_DOC_PAGE_COUNT");
                    long docPageNumber = rs.getLong("BATCH_DOC_PAGE_NUMBER");

                    BatchInfo bi = new BatchInfo(batchId, pageCount,
                            pageNumber, docPageCount, docPageNumber);

                    r.setBatchInfo(bi);
                }

                requestList.add(r);
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_ps != null)
                {
                    m_ps.close();
                }
                if (rs != null)
                {
                    rs.close();
                }
            }
            catch (Throwable ignore)
            {
            }

            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Throwable ignore)
            {
            }
        }

        return requestList;
    }

    private boolean isBatch(String p_batchId)
    {
        boolean isBatch = true;

        if (p_batchId == null || p_batchId.length() == 0)
        {
            isBatch = false;
        }

        return isBatch;
    }
}
