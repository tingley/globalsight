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

package com.globalsight.everest.webapp.pagehandler.edit;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.util.GeneralException;

import java.rmi.RemoteException;

/**
 * A class which contains helper methods that are shared by online/offline.
 */
public class EditCommonHelper
{

    /** Creates new EditorCommonHelper */
    public EditCommonHelper()
    {
    }

    /**
     * <p>Verifies that the user is still able to work on a
     * task. Throws a EnvoyServletException if not and the editor(s)
     * should be closed.  This method is used during upload which 
     * assumes that a task has already been accepted.</p>
     *
     * <p>A task (activity) is not valid anymore when (1) it is not
     * active anymore; (2) it is active but assigned to a different
     * user.
     */
    static public void verifyTask(String p_userId,
        String p_taskId)
        throws EnvoyServletException
    {
        try
        {
            // Error if assigned to different user
            Task task = 
                ServerProxy.getTaskManager().getTask(
                    p_userId, Long.parseLong(p_taskId), 
                    WorkflowConstants.TASK_ACCEPTED);

            // Error if discarded
            if (task.getState() == Task.STATE_DEACTIVE)
            {
                String[] arg = { p_taskId };

                throw new
                    TaskException(TaskException.MSG_FAILED_TO_GET_TASK,
                    arg, new Exception ("Task is not active anymore"));
            };
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e.getMessageKey(),
                e.getMessageArguments(), e, e.getPropertyFileName());
        }
        catch (RemoteException e)
        {
            throw new
                EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }
}
