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
package com.globalsight.cxe.entity.blaise;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class BlaiseConnectorJob extends PersistentObject
{
    private static final long serialVersionUID = -933803792285804414L;

    private static Logger logger = Logger.getLogger(BlaiseConnectorJob.class);

    // Available values for "uploadXliffState" and "completeState".
    public static String FAIL = "fail";
    public static String SUCCEED = "succeed";
    public static String SUCCEED_CLOSED = "succeed(already closed)";

    private long blaiseConnectorId;
    private long blaiseEntryId;
    private long jobId;
    private String uploadXliffState;
    private String completeState;
    // GBS-4749
    private long sourcePageId;
    private GeneralException m_uploadException = null;
    private GeneralException m_completeException = null;

    public long getBlaiseConnectorId()
    {
        return blaiseConnectorId;
    }

    public void setBlaiseConnectorId(long blaiseConnectorId)
    {
        this.blaiseConnectorId = blaiseConnectorId;
    }

    public long getBlaiseEntryId()
    {
        return blaiseEntryId;
    }

    public void setBlaiseEntryId(long blaiseEntryId)
    {
        this.blaiseEntryId = blaiseEntryId;
    }

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }

    public long getJobId()
    {
        return jobId;
    }

    public String getUploadXliffState()
    {
        return uploadXliffState;
    }

    public void setUploadXliffState(String uploadXliffState)
    {
        this.uploadXliffState = uploadXliffState;
    }

    public String getCompleteState()
    {
        return completeState;
    }

    public void setCompleteState(String completeState)
    {
        this.completeState = completeState;
    }

    public long getSourcePageId()
    {
        return sourcePageId;
    }

    public void setSourcePageId(long sourcePageId)
    {
        this.sourcePageId = sourcePageId;
    }

    public String getUploadExceptionAsString()
    {
        String uploadExpStr = null;
        if (m_uploadException != null)
        {
            try
            {
                uploadExpStr = m_uploadException.serialize();
            }
            catch (GeneralException e)
            {
                logger.error("Failed to serialize the upload exception", e);
            }
        }
        return uploadExpStr;
    }

    public void setUploadExceptionAsString(String p_uploadExceptionXml)
    {
        setUploadException(p_uploadExceptionXml);
    }

    public void setUploadException(GeneralException p_uploadException)
    {
        m_uploadException = p_uploadException;
    }

    public String getCompleteExceptionAsString()
    {
        String completeExpStr = null;
        if (m_completeException != null)
        {
            try
            {
                completeExpStr = m_completeException.serialize();
            }
            catch (GeneralException e)
            {
                logger.error("Failed to serialize the complete exception", e);
            }
        }
        return completeExpStr;
    }

    public void setCompleteExceptionAsString(String p_completeExceptionXml)
    {
        setCompleteException(p_completeExceptionXml);
    }

    public void setCompleteException(GeneralException p_completeException)
    {
        m_completeException = p_completeException;
    }

    private void setUploadException(String p_uploadExceptionXml)
    {
        if (StringUtil.isNotEmpty(p_uploadExceptionXml))
        {
            try
            {
                GeneralException uploadException = GeneralException
                        .deserialize(p_uploadExceptionXml);

                setUploadException(uploadException);
            }
            catch (GeneralException e)
            {
                logger.error("Failed to deserialize the upload exception xml", e);
            }
        }
    }

    private void setCompleteException(String p_completeExceptionXml)
    {
        if (StringUtil.isNotEmpty(p_completeExceptionXml))
        {
            try
            {
                GeneralException completeException = GeneralException
                        .deserialize(p_completeExceptionXml);

                setCompleteException(completeException);
            }
            catch (GeneralException e)
            {
                logger.error("Failed to deserialize the complete exception xml", e);
            }
        }
    }
}
