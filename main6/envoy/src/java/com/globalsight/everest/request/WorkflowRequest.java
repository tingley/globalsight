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

import java.util.Collection;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.util.GeneralException;

public interface WorkflowRequest
{
    /**
     * The localization request types that are valid. They are greater than 0.
     */
    public static final int ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB = 1;

    public static final int WORKFLOW_REQUEST_FAILURE = -1;

    /**
     * Return the ID of this request
     * 
     * @return long The primary key of request object
     */
    public long getId();

    public Collection getWorkflowTemplateList();

    public GeneralException getException();

    /**
     * @see Request.getExceptionAsString
     */
    public String getExceptionAsString();

    public void addWorkflowTemplate(WorkflowTemplateInfo p_workflowTemplateInfo);

    public void setException(GeneralException p_exception);

    /**
     * Returns the job that this request is associated with. Could be null if it
     * hasn't been assigned to a job yet.
     */
    public Job getJob();

    public void setJob(Job p_job);

    public void setType(int p_type);

    /**
     * @see Request.getType()
     */
    public int getType();
}
