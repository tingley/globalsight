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

package com.globalsight.everest.jobhandler.jobcreation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.util.fileImport.FileImportUtil;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Provides batch job capability by counting incoming requests and determining
 * if all requests for a job have been received.
 */
class BatchMonitor
{
    static private Logger c_logger = Logger
            .getLogger(BatchMonitor.class);

    /**
     * RequestMap takes a list of individual page requests and determines if all
     * requests for the original import request have been received. Multiple
     * pages may have been created for Office files like PowerPoint where one
     * document results in multiple slides.
     * 
     * This class is used during import when CXE sends messages for pages that
     * may have been created from a single document. It needs to determine if
     * all requests have been received for the job. It does so by examining the
     * original page count (e.g. 3) and the document page counts per original
     * page (e.g. 2 of 10). The job can proceed only if all pages have been
     * received.
     */
    private class RequestMap
    {
        private long m_totalPages = -1;
        private HashMap m_docsToPageCount = new HashMap();
        private HashMap m_docsToPages = new HashMap();
        private String batchId = null;

        RequestMap(Collection p_requests)
        {
            for (Iterator it = p_requests.iterator(); it.hasNext();)
            {
                Request request = (Request) it.next();
                BatchInfo info = request.getBatchInfo();
                batchId = info.getBatchId();

                // Must be the same for all requests in a batch.
                m_totalPages = info.getPageCount();

                Long curPage = new Long(info.getPageNumber());
                Long curDocPageCount = new Long(info.getDocPageCount());
                Long curDocPageNum = new Long(info.getDocPageNumber());
                m_docsToPageCount.put(curPage, curDocPageCount);

                ArrayList reqs = (ArrayList) m_docsToPages.get(curPage);
                if (reqs == null)
                {
                    reqs = new ArrayList();
                    m_docsToPages.put(curPage, reqs);
                }

                reqs.add(curDocPageNum);
            }
            
            if (batchId != null)
            {
                List<BatchInfo> bis = FileImportUtil.CANCELED_REQUEST.get(batchId);
                if (bis != null && bis.size() > 0)
                {
                    for (BatchInfo bi : bis)
                    {
                        Long curPage = new Long(bi.getPageNumber());
                        Long curDocPageCount = new Long(bi.getDocPageCount());
                        Long curDocPageNum = new Long(bi.getDocPageNumber());
                        m_docsToPageCount.put(curPage, curDocPageCount);

                        ArrayList reqs = (ArrayList) m_docsToPages.get(curPage);
                        if (reqs == null)
                        {
                            reqs = new ArrayList();
                            m_docsToPages.put(curPage, reqs);
                        }

                        reqs.add(curDocPageNum);
                    }
                }
            }
        }

        boolean isComplete()
        {
            // Must have received requests for all original pages
            if (m_totalPages != m_docsToPageCount.size())
            {
                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("BatchMonitor: waiting for " + m_totalPages
                            + " documents, have " + m_docsToPageCount.size());
                }

                return false;
            }

            // For all original pages (documents), must have received
            // all sub-pages (slides).
            for (Iterator it = m_docsToPages.keySet().iterator(); it.hasNext();)
            {
                Long doc = (Long) it.next();
                Long docCount = (Long) m_docsToPageCount.get(doc);

                ArrayList reqs = (ArrayList) m_docsToPages.get(doc);
                if (reqs.size() != docCount.intValue())
                {
                    if (c_logger.isDebugEnabled())
                    {
                        c_logger.debug("BatchMonitor: waiting for " + docCount
                                + " pages of document " + doc + ", have "
                                + reqs.size());
                    }

                    return false;
                }
            }

            if (c_logger.isDebugEnabled())
            {
                c_logger.debug("BatchMonitor: job is complete (" + m_totalPages
                        + " documents)");
            }
            
            if (batchId != null)
            {
                FileImportUtil.CANCELED_REQUEST.remove(batchId);
            }

            return true;
        }
    }

    //
    // Constructor
    //

    BatchMonitor()
    {
    }

    /**
     * Returns a batch job in BATCHRESERVED state that matches the batch id. Or
     * null if one doesn't exist.
     * 
     * @param p_batchInfo
     *            The batch information to use when looking for the job.
     * 
     * @return An existing job or NULL if it couldn't find one.
     */
    Job findBatchJob(BatchInfo p_batchInfo) throws JobCreationException
    {
        Job result = null;

        // Search for an existing job with the specified batch
        // information in BATCH_RESERVED state.
        Vector args = new Vector();
        args.add(p_batchInfo.getBatchId());
        Collection jobs = null;

        try
        {
            String hql = "select distinct j from JobImpl j inner join j.requestSet r "
                    + "where j.state = 'BATCH_RESERVED' "
                    + "and r.batchInfo.batchId = :bId";

            Map map = new HashMap();
            map.put("bId", p_batchInfo.getBatchId());
            jobs = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error("Error when querying for a batch job.", pe);

            String errorArgs[] = new String[1];
            errorArgs[0] = p_batchInfo.getBatchId();
            throw new JobCreationException(
                    JobCreationException.MSG_QUERY_FOR_BATCH_JOB_FAILED,
                    errorArgs, pe);
        }

        // one was found
        if (jobs.size() > 0)
        {
            result = (Job) jobs.iterator().next();
        }

        return result;
    }

    /**
     * Verify if the Job has all its requests.
     * 
     * @param p_job
     *            The batch job to examine and determine if all requests have
     *            been received.
     * @return 'true' if the job contains all its request for batching. 'false'
     *         if the job is still incomplete
     */
    boolean isBatchComplete(Job p_job)
    {
        RequestMap map = new RequestMap(p_job.getRequestList());
        return map.isComplete();
    }

    /**
     * Verify if the Job has all its requests.
     * 
     * @param p_requests
     *            a list of requests of a batch job to examine and determine if
     *            all requests have been received.
     * @return 'true' if the job contains all its request for batching. 'false'
     *         if the job is still incomplete
     */
    boolean isBatchComplete(List p_requests)
    {
        RequestMap map = new RequestMap(p_requests);
        return map.isComplete();
    }

    /**
     * Generate a job name for a batch job from the user specified name. It
     * doesn't have to be unique if it is user specified.
     * 
     * If a user defined one wasn't provided return null. Must generate the job
     * name like non-batch jobs.
     * 
     * @param p_request
     *            The request that will be added to the job which contains
     *            information to use for naming the job.
     * 
     * @return Job name or null if the user didn't specify one.
     */
    String generateJobName(Request p_request)
    {
        String jobName = "(anonymous job)";
        String jobPrefixName = p_request.getBatchInfo().getJobPrefixName();

        // just use the job prefix name as the entire name; don't need
        // to make it unique
        if (jobPrefixName != null)
        {
            jobName = jobPrefixName;
        }

        return jobName;
    }
}
