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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.util.GeneralException;

public class WorkflowRequestImpl extends PersistentObject implements
        WorkflowRequest
{
    private static final long serialVersionUID = -3484484593045117019L;

    // static class variables
    private static Logger c_logger = Logger
            .getLogger(WorkflowRequestImpl.class.getName());

    // constants used for TOPLink queries - should match the values below
    public static final String ID = M_ID;

    private int m_type;

    private String typeStr; // Used by Hibernate, please change the value of
    // m_type and typeStr at the same time

    private Collection m_workflowTemplateList = new ArrayList();

    private GeneralException m_exception = null;

    private String exceptionXml = null;

    private Job m_job = null;

    public WorkflowRequestImpl()
    {
    }

    public Collection getWorkflowTemplateList()
    {
        return m_workflowTemplateList;
    }

    public void addWorkflowTemplate(WorkflowTemplateInfo p_workflowTemplateInfo)
    {
        m_workflowTemplateList.add(p_workflowTemplateInfo);
    }

    /**
     * @see Request.setException(String)
     */
    public void setException(String p_exceptionXml)
            throws RequestHandlerException
    {

        if (p_exceptionXml != null && p_exceptionXml.length() > 0)
        {
            exceptionXml = p_exceptionXml; // For Hibernate use
            try
            {
                GeneralException exception = GeneralException
                        .deserialize(p_exceptionXml);

                setException(exception);
            }
            catch (GeneralException ge)
            {
                c_logger
                        .error("Failed to deserialize the "
                                + "exception xml to add to request "
                                + this.getId(), ge);
                String[] args = new String[3];
                args[0] = Long.toString(this.getId());
                args[1] = p_exceptionXml;
                throw new RequestHandlerException(
                        RequestHandlerException.MSG_PAGE_ATTRIBUTE_CAN_NOT_BE_UPDATED,
                        args, ge);
            }

        }
    }

    /**
     * @see Request.setException(GeneralException p_exception)
     */
    public void setException(GeneralException p_exception)
    {
        m_exception = p_exception;
    }

    public void setJob(Job p_job)
    {
        m_job = p_job;
    }

    /**
     * @see Request.getException()
     */
    public GeneralException getException()
    {
        return m_exception;
    }

    public void setType(int p_type)
    {
        m_type = p_type;

        // Convert the data For storing by Hibernate
        if (p_type == -1)
        {
            typeStr = "WORKFLOW_REQUEST_FAILURE";
        }
        else if (p_type == 1)
        {
            typeStr = "ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB";
        }
    }

    /**
     * @see Request.getType()
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * @see Request.getExceptionAsString
     */
    public String getExceptionAsString()
    {
        String expString = new String();

        if (m_exception != null)
        {
            try
            {
                expString = m_exception.serialize();
            }
            catch (GeneralException ge)
            {
                c_logger.error("Failed to serialize the exception of request "
                        + this.getId(), ge);
            }
        }

        return expString;
    }

    /**
     * Returns the job that this request is associated with. Could be null if it
     * hasn't been assigned to a job yet.
     */
    public Job getJob()
    {
        return m_job;
    }

    // Useed by Hibernate
    public String getTypeStr()
    {
        return typeStr;
    }

    public void setTypeStr(String typeStr)
    {
        this.typeStr = typeStr;
        if (typeStr != null && typeStr.equals("WORKFLOW_REQUEST_FAILURE"))
        {
            setType(-1);
        }
        else if (typeStr != null
                && typeStr.equals("ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB"))
        {
            setType(1);
        }
    }

    public String getExceptionXml()
    {
        return exceptionXml;
    }

    public void setExceptionXml(String exceptionXml)
    {
        setException(exceptionXml);
    }

    public void setWorkflowTemplateList(Collection templateList)
    {
        m_workflowTemplateList = templateList;
    }

}
