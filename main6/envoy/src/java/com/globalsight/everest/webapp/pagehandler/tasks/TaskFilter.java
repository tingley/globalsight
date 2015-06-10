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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.comment.CommentMainHandler;
import com.globalsight.everest.webapp.pagehandler.administration.comment.CommentUploadHandler;
import com.globalsight.everest.webapp.pagehandler.administration.comment.SegmentCommentHandler;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.CommentEditorPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorPageInfoHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorResourcePageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.ImageUploadPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.OptionsPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.SegmentEditorPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFPageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.download.DownloadPageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.download.SendDownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.offline.upload.UploadPageHandler;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * <class>TaskFilter</class> is used to make sure that the user has the
 * permission to operate the task.
 * <p>
 * It is used in ControlServlet.
 */
public class TaskFilter
{
    private static final Logger CATEGORY = Logger
            .getLogger(TaskFilter.class);

    // Stores handler classes that need to do checking.
    private static List<String> HANDLERS = new ArrayList<String>();
    static
    {
        HANDLERS.add(ImageUploadPageHandler.class.getName());
        HANDLERS.add(TaskListHandler.class.getName());
        HANDLERS.add(DownloadFileHandler.class.getName());
        HANDLERS.add(SendDownloadFileHandler.class.getName());
        HANDLERS.add(TaskDetailHandler.class.getName());
        HANDLERS.add(WordCountHandler.class.getName());
        HANDLERS.add(SegmentCommentHandler.class.getName());
        HANDLERS.add(EditorResourcePageHandler.class.getName());
        HANDLERS.add(CommentEditorPageHandler.class.getName());
        HANDLERS.add(DownloadPageHandler.class.getName());
        HANDLERS.add(CommentMainHandler.class.getName());
        HANDLERS.add(PreviewPDFPageHandler.class.getName());
        HANDLERS.add(RejectTaskHandler.class.getName());
        HANDLERS.add(CommentUploadHandler.class.getName());
        HANDLERS.add(UploadPageHandler.class.getName());
        HANDLERS.add(SearchTasksResultsHandler.class.getName());
        HANDLERS.add(AddCommentHandler.class.getName());
        HANDLERS.add(EditorPageInfoHandler.class.getName());
        HANDLERS.add(OptionsPageHandler.class.getName());
        HANDLERS.add(SegmentEditorPageHandler.class.getName());
        HANDLERS.add(com.globalsight.everest.webapp.pagehandler.edit.online2.EditorPageHandler.class.getName());
        HANDLERS.add(com.globalsight.everest.webapp.pagehandler.edit.online.EditorPageHandler.class.getName());
    }

    /**
     * Check the user can operate the task or not. and turn to error page if
     * user do not have the permission.
     * 
     * @param handler
     *            handler class, do the checking before
     *            handler.invokePageHandler.
     * @param p_request
     * @param p_response
     * @param p_context
     * @return false if the user can't operate the task, others will return
     *         true.
     * @throws EnvoyServletException
     * @throws RemoteException
     * @throws ServletException
     * @throws IOException
     */
    public static boolean doFilter(PageHandler handler,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws EnvoyServletException,
            RemoteException, ServletException, IOException
    {
        String handlerName = handler.getClass().getName();
        if (HANDLERS.contains(handlerName))
        {
            return filter(p_request, p_response, p_context);
        }
        return true;
    }

    /**
     * Do check.
     * 
     * @param p_request
     * @param p_response
     * @param p_context
     * @return
     * @throws EnvoyServletException
     * @throws RemoteException
     * @throws ServletException
     * @throws IOException
     */
    private static boolean filter(HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context)
            throws EnvoyServletException, RemoteException, ServletException,
            IOException
    {
        String key = authority(p_request);
        boolean result = key == null;

        if (!result)
        {
            toErrorPage(p_request, p_response, p_context, key);
        }

        return result;
    }

    /**
     * Turns to error page.
     * 
     * @param p_request
     * @param p_response
     * @param p_context
     * @throws EnvoyServletException
     * @throws RemoteException
     * @throws ServletException
     * @throws IOException
     */
    private static void toErrorPage(HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context, String key)
            throws EnvoyServletException, RemoteException, ServletException,
            IOException
    {
        HttpSession httpSession = p_request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(httpSession);

        p_request.setAttribute("badresults", bundle.getString(key));

        // remove the task from the most recently used list
        String cookie = p_request.getParameter("cookie");

        try
        {
            TaskHelper
                    .removeMRUtask(p_request, httpSession, cookie, p_response);
        }
        catch (Exception e)
        {
            // Only remove the cookie, ignore the exception.
            CATEGORY.error(e.getMessage(), e);
        }

        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher("/envoy/tasks/taskSearch.jsp");

        dispatcher.forward(p_request, p_response);
    }

    /**
     * Checks the user has the permission or not.
     * <p>
     * If the task is exit and not assign to the user, return false.
     * 
     * @param p_request
     * @return
     */
    private static String authority(HttpServletRequest p_request)
    {
        HttpSession httpSession = p_request.getSession();
        User user = TaskHelper.getUser(httpSession);
        long taskId = -1;
        Task task = null;

        String action = p_request.getParameter(WebAppConstants.TASK_ACTION);
        if (action != null
                && action.equals(WebAppConstants.TASK_ACTION_RETRIEVE))
        {
            String taskIdParam = p_request
                    .getParameter(WebAppConstants.TASK_ID);
            taskId = Long.parseLong(taskIdParam);           
        }
        else
        {
            Object ob = TaskHelper.retrieveObject(httpSession,
                    WebAppConstants.WORK_OBJECT);

            if (ob != null && ob instanceof Task)
            {
                task = (Task) ob;
                taskId = task.getId();
            }
        }

        if (taskId < 1)
        {
            return null;
        }
        
        if (task == null)
        {
            task = TaskHelper.getTask(taskId);
        }
        
        if (task == null)
        {
            return null;
        }
        
        String acceptor = task.getAcceptor();
        if (acceptor != null && acceptor.equals(user.getUserId()))
        {
            return null;
        }

        Job job = task.getWorkflow().getJob();
        job = HibernateUtil.get(JobImpl.class, job.getId());
        List<String> assignees = WorkflowJbpmUtil.getAssignees(taskId);
        assignees.add(job.getProject().getProjectManagerId());
        if (!assignees.contains(user.getUserId()))
        {
            return "msg_reassigned";
        }
        
        if (Job.ADD_FILE.equalsIgnoreCase(job.getState()) || Job.BATCHRESERVED.equalsIgnoreCase(job.getState()))
        {
            return "msg_add_file";
        }
        
        if (Job.DELETE_FILE.equalsIgnoreCase(job.getState()))
        {
            return "msg_delete_file";
        }
        
        
        return null;
    }
}
