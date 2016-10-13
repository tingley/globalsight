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
package com.globalsight.everest.request;

import java.io.Serializable;

/**
 * Stores the information about a request that is part of a batch.
 */
public class BatchInfo
    implements Serializable
{
    //
    // Public Data Members - TOPLink query keys need to match data members
    //

    private static final long serialVersionUID = -3341004218255298892L;

    static public final String BATCH_ID = "m_batchId";

    //
    // Private Data Members
    //

    // unique identifier for the batch job
    private String m_batchId;
    // the number of pages in the batch
    private long m_pageCount;
    // the page number for this page
    private long m_pageNumber;
    // the number of pages in the current document
    private long m_docPageCount;
    // the page number of this page in the current document
    private long m_docPageNumber;
    // optional job prefix name
    private String m_jobPrefixName = "";

    /**
     * Default constructor - used by TOPLink
     */
    public BatchInfo()
    {}

    /**
     * Constructor with no job name specified
     */
    public BatchInfo(String p_batchId, long p_pageCount, long p_pageNumber,
        long p_docPageCount, long p_docPageNumber)
    {
        m_batchId = p_batchId;
        m_pageCount = p_pageCount;
        m_pageNumber = p_pageNumber;
        m_docPageCount = p_docPageCount;
        m_docPageNumber = p_docPageNumber;
    }

    public BatchInfo(String p_batchId, long p_pageCount, long p_pageNumber,
        long p_docPageCount, long p_docPageNumber, String p_jobPrefixName)
    {
        this(p_batchId, p_pageCount, p_pageNumber, p_docPageCount, p_docPageNumber);
        m_jobPrefixName = p_jobPrefixName;
    }

    public String getBatchId()
    {
        return m_batchId;
    }

    public long getPageCount()
    {
        return m_pageCount;
    }

    public long getPageNumber()
    {
        return m_pageNumber;
    }

    public long getDocPageCount()
    {
        return m_docPageCount;
    }

    public long getDocPageNumber()
    {
        return m_docPageNumber;
    }

    public String getJobPrefixName()
    {
        return m_jobPrefixName;
    }


    public String toString()
    {
        StringBuffer sb = new StringBuffer("BatchInfo");

        sb.append(" m_batchId=");
        sb.append(m_batchId);
        sb.append(" m_pageCount=");
        sb.append(m_pageCount);
        sb.append(" m_pageNumber=");
        sb.append(m_pageNumber);
        sb.append(" m_docPageCount=");
        sb.append(m_docPageCount);
        sb.append(" m_docPageNumber=");
        sb.append(m_docPageNumber);
        sb.append(" m_jobPrefixName=");
        sb.append(m_jobPrefixName);

        return sb.toString();
    }

    public void setBatchId(String id)
    {
        m_batchId = id;
    }

    public void setPageCount(long count)
    {
        m_pageCount = count;
    }

    public void setPageNumber(long number)
    {
        m_pageNumber = number;
    }

    public void setDocPageCount(long pageCount)
    {
        m_docPageCount = pageCount;
    }

    public void setDocPageNumber(long pageNumber)
    {
        m_docPageNumber = pageNumber;
    }

    public void setJobPrefixName(String prefixName)
    {
        m_jobPrefixName = prefixName;
    }
};
