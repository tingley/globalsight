/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import com.globalsight.everest.workflowmanager.Workflow;

/**
 * The Status Object for creating PDF.
 */
public class JobDetailsPDFsBO
{
    public static final String STATUS_DEFAULT = "lb_pdf_state_default";
    public static final String STATUS_IN_PROGRESS = "lb_inprogress";
    public static final String STATUS_DONE = "lb_done";
    
    private long workflowId;
    private String wokflowState;
    private String targetLocaleDisplayName;
    private int totalWordCount;
    private long totalPDFFileNumber;
    private long existPDFFileNumber;
    private String status = STATUS_DEFAULT;
    private String statusDisplayName;

    public JobDetailsPDFsBO(Workflow p_workflow)
    {
        workflowId = p_workflow.getId();
        wokflowState = p_workflow.getState();
    }

    public long getTotalPDFFileNumber()
    {
        return totalPDFFileNumber;
    }

    public void setTotalPDFFileNumber(long totalPDFFiles)
    {
        this.totalPDFFileNumber = totalPDFFiles;
    }

    public long getExistPDFFileNumber()
    {
        return existPDFFileNumber;
    }

    public void setExistPDFFileNumber(long existPDFFiles)
    {
        this.existPDFFileNumber = existPDFFiles;
    }

    public String getTargetLocaleDisplayName()
    {
        return targetLocaleDisplayName;
    }

    public void setTargetLocaleDisplayName(String targetLocaleDisplayName)
    {
        this.targetLocaleDisplayName = targetLocaleDisplayName;
    }

    public int getTotalWordCount()
    {
        return totalWordCount;
    }

    public void setTotalWordCount(int totalWordCount)
    {
        this.totalWordCount = totalWordCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    
    public String getStatusDisplayName()
    {
        if(statusDisplayName == null)
            return status;
        
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName)
    {
        this.statusDisplayName = statusDisplayName;
    }

    public long getWorkflowId()
    {
        return workflowId;
    }

    public String getWokflowState()
    {
        return wokflowState;
    }
    
    public String toJSON()
    {
        StringBuffer json = new StringBuffer("{");
        json.append("\"workflowId\":").append(workflowId).append(",");
        json.append("\"totalPDFFileNumber\":").append(totalPDFFileNumber).append(",");
        json.append("\"existPDFFileNumber\":").append(existPDFFileNumber).append(",");
        json.append("\"status\":\"").append(status).append("\"");
        json.append("}");
        return json.toString();
    }
}
