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

package com.globalsight.everest.webapp.pagehandler.tasks;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class TaskCompleteThread implements Runnable
{
    private String userId;
    private Task task;
    private String destinationArrow;
    private String companyId;
    
    public TaskCompleteThread(String userId, Task task,
            String destinationArrow, String companyId)
    {
        super();
        this.userId = userId;
        this.task = task;
        this.destinationArrow = destinationArrow;
        this.companyId = companyId;
    }

    @Override
    public void run()
    {
        CompanyThreadLocal.getInstance().setIdValue(companyId);
        try
        {
            TaskHelper.completeTask(userId, task, destinationArrow);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

}
