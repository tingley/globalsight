/**
 *  Copyright 2011 Welocalize, Inc. 
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
package com.globalsight.everest.taskmanager;

/**
 * Task Business Object, 
 * which wrapped the TaskImpl and will be used in Business Layer.
 */
public class TaskBO extends TaskImpl
{
    private static final long serialVersionUID = 1L;
    // Task upload status(taskUploading/...)
    private String uploadStatus = null;
    
    public TaskBO(long taskId)
    {
        this.setId(taskId);
    }
    
    public TaskBO(TaskImpl p_task)
    {
        setAcceptedDate(p_task.getAcceptedDate());
        setAcceptor(p_task.getAcceptor());
        setCompanyId(p_task.getCompanyId());
        setCompletedDate(p_task.getCompletedDate());        
        setEstimatedAcceptanceDate(p_task.getEstimatedAcceptanceDate());
        setEstimatedCompletionDate(p_task.getEstimatedCompletionDate());
        setExpenseRate(p_task.getExpenseRate());        
        setId(p_task.getId());
        setIsActive(p_task.isActive());
        setName(p_task.getName());
        setProjectManagerName(p_task.getProjectManagerName());        
        setRateSelectionCriteria(p_task.getRateSelectionCriteria());
        setRatings(p_task.getRatings());
        setRevenueRate(p_task.getRevenueRate());        
        setState(p_task.getState());
        setStfCreationState(p_task.getStfCreationState());
        setTaskComments(p_task.getTaskComments());
        setTaskTuvs(p_task.getTaskTuvs());
        setTaskType(p_task.getTaskType());
        setTimestamp(p_task.getTimestamp());
        setType(p_task.getType());        
        setWorkflow(p_task.getWorkflow());
        setWorkflowTask(p_task.getWorkflowTask());
        setWorkSet(p_task.getWorkSet());
    }
    
    public String getUploadStatus()
    {
        return uploadStatus;
    }

    public void setUploadStatus(String p_uploadStatus)
    {
        uploadStatus = p_uploadStatus;
    }
}
