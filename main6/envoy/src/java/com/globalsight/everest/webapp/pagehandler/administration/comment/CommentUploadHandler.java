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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import com.globalsight.everest.comment.*;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.ServletUtil;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.FormUtil;
import com.globalsight.util.StringUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * <p>
 * MainHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the list of available commentreferences.</li>
 * <li>Uploading new Comment References.</li>
 * <li>Deleting and updating existing Comment Reference files.</li>
 * </ol>
 */

public class CommentUploadHandler extends PageHandler implements
        CommentConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(CommentUploadHandler.class.getName());

    //
    // Private Members
    //
    private CommentState m_state = null;

    //
    // Constructor
    //
    public CommentUploadHandler()
    {
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String commentStr = ServletUtil.getValue(p_request, "taskComment");
        if (commentStr != null)
            sessionMgr.setAttribute("taskComment", commentStr);

        User user = TaskHelper.getUser(session);
        
        if (m_state == null)
        {
            m_state = new CommentState();
        }
        String toTask = p_request.getParameter("toTask");
        String toJob = p_request.getParameter("toJob");
        if(toTask != null)
        {
            String taskIdParam = p_request.getParameter(TASK_ID);
            String taskStateParam = p_request.getParameter(TASK_STATE);
            long taskId = TaskHelper.getLong(taskIdParam);
            int taskState = TaskHelper.getInt(taskStateParam, -10);// -10 as
            
            Task task = TaskHelper.getTask(user.getUserId(), taskId,taskState);
            // Save the task to session
            TaskHelper.storeObject(session, WORK_OBJECT, task);
        }
        else if (toJob != null)
        {
            String jobId = p_request.getParameter("jobId");
            long contextMenuJobId = Long.valueOf(jobId);
            Job contextMenuJob = WorkflowHandlerHelper
                    .getJobById(contextMenuJobId);
            TaskHelper.storeObject(session, WebAppConstants.WORK_OBJECT,
                    contextMenuJob);
        }
        String commentId = p_request.getParameter("commentId");
        if(commentId != null && commentId != ""){
            Comment comment = TaskHelper.getComment(session, Long.parseLong(commentId));
            sessionMgr.setAttribute("comment", comment);
        }
        
        WorkObject wo = (WorkObject) TaskHelper.retrieveObject(session,
                WORK_OBJECT);
        if (wo == null)
        {
            EnvoyServletException e = new EnvoyServletException(
                    "WorkObjectNotFound", null, null);
            CATEGORY.error(e.getMessage(), e);
            throw e;
        }

//        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        String userId = user.getUserId();
        String wId = "";
        if (wo != null)
        {
            long companyId = -1;
            if (wo instanceof Task)
            {
                Task task = (Task) wo;
                wId = (new Long(task.getId())).toString();

                companyId = ((Task) wo).getCompanyId();
            }
            else if (wo instanceof Job)
            {
                Job job = (Job) wo;
                wId = (new Long(job.getId())).toString();

                companyId = ((Job) wo).getCompanyId();
            }
            else if (wo instanceof Workflow)
            {
                Workflow wf = (Workflow) wo;
                wId = (new Long(wf.getId())).toString();

                companyId = ((Workflow) wo).getCompanyId();
            }

            CompanyThreadLocal.getInstance().setIdValue(
                    String.valueOf(companyId));
        }
        String tmpDir = WebAppConstants.COMMENT_REFERENCE_TEMP_DIR + wId
                + userId;

        String action = p_request.getParameter(TASK_ACTION);

        if (action == null)
        {
            String value = (String) p_request
                    .getParameter(CommentConstants.DELETE);
            String valueSet = WebAppConstants.COMMENT_REFERENCE_DELETE;
            if (value == null)
            {
                valueSet = WebAppConstants.COMMENT_REFERENCE_NO_DELETE;
            }

            if (valueSet.equals(WebAppConstants.COMMENT_REFERENCE_NO_DELETE)
                    || value.equals(WebAppConstants.COMMENT_REFERENCE_NO_DELETE))
            {
                CommentUpload uploader = new CommentUpload();

                try
                {
                    uploader.doUpload(p_request);
                    m_state.setMessage("");
                }
                catch (CommentException ex)
                {
                    m_state.setMessage(ex.getMessage());
                }
            }
            else
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request,
                        FormUtil.Forms.EDIT_COMMENT)
                        && value.equals(WebAppConstants.COMMENT_REFERENCE_DELETE))
                {
                    String fileTypes = p_request.getParameter("fileTypes");
                    String[] typeArr = null;
                    if (StringUtil.isNotEmptyAndNull(fileTypes))
                    {
                        typeArr = fileTypes.split(",");
                    }
                    editCommentReferences(
                            p_request.getParameterValues(CommentConstants.FILE_CHECKBOXES),
                            typeArr, tmpDir, sessionMgr);
                }
            }
        }
        else if (StringUtil.isNotEmptyAndNull(action)
                && action.equalsIgnoreCase(CHECK_UPLOAD_FILE_TYPE))
        {
            ResourceBundle bundle = PageHandler.getBundle(session);
            String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
            p_response.setContentType("text/html;charset=UTF-8");
            ServletOutputStream out = p_response.getOutputStream();
            CommentUpload uploader = new CommentUpload();
            List<File> canNotUploadFiles = uploader
                    .checkUploadFileType(p_request, currentCompanyId);
            if (canNotUploadFiles != null && canNotUploadFiles.size() > 0)
            {
                out.write(((bundle.getString("lb_message_check_upload_file_type") + CompanyWrapper
                        .getCompanyById(currentCompanyId).getDisableUploadFileTypes()))
                        .getBytes("UTF-8"));
                for (File file : canNotUploadFiles)
                {
                    file.delete();
                }
            }
            else
            {
                out.write(("notContain").getBytes("UTF-8"));
            }
            return;
        }

        getCommentReferences(tmpDir, sessionMgr);
        FormUtil.addSubmitToken(p_request, FormUtil.Forms.EDIT_COMMENT);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Some references are in the tmp dir, some in the permanent dir. This will
     * get both.
     */
    private ArrayList getCommentReferences(String tmpDir,
            SessionManager sessionMgr) throws EnvoyServletException
    {
        ArrayList commentReferences = null;
        ArrayList deletedReferences = (ArrayList) sessionMgr
                .getAttribute("deletedReferences");
        if (deletedReferences == null)
            deletedReferences = new ArrayList();

        Comment comment = (Comment) sessionMgr.getAttribute("comment");

        try
        {
            CommentManager mgr = ServerProxy.getCommentManager();
            commentReferences = mgr.getCommentReferences(tmpDir,
                    WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS);
            // check if existing comment

            if (comment != null)
            {
                ArrayList more = mgr.getCommentReferences(
                        Long.toString(comment.getId()),
                        WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS,
                        true);
                // loop through the existing list and see if any were deleted.
                // if so, don't add to list
                for (int i = 0; i < more.size(); i++)
                {
                    CommentFile cf = (CommentFile) more.get(i);

                    if (!deletedReferences.contains(cf))
                    {
                        commentReferences.add(cf);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        String commentId = "";
        if (comment != null)
            commentId = Long.toString(comment.getId());
        sessionMgr.setAttribute("commentId", commentId);

        sessionMgr.setAttribute("commentReferences", commentReferences);
        sessionMgr.setAttribute("deletedReferences", deletedReferences);
        return commentReferences;
    }

    /**
     * Calls deleteCommentReference() for each Comment Reference file whose id
     * is specified in the array of strings.
     */
    private void editCommentReferences(String[] deleteIndexs,
            String[] fileTypes, String tmpDir, SessionManager sessionMgr)
            throws EnvoyServletException
    {
        ArrayList commentReferences = getCommentReferences(tmpDir, sessionMgr);
        ArrayList deletedReferences = (ArrayList) sessionMgr
                .getAttribute("deletedReferences");
        
        Vector<String> changeToGeneralStr = new Vector<String>();
        Vector<String> changeToRestrictStr = new Vector<String>();
        Vector<String> changeToSupportStr = new Vector<String>();
        if (fileTypes != null && fileTypes.length > 0)
        {
            for(int i=0;i<fileTypes.length;i++)
            {
                String[] types = fileTypes[i].toString().split("_");
                if (StringUtil.isNotEmpty(types[1]))
                {
                    if ("General".equalsIgnoreCase(types[1]))
                    {
                        changeToGeneralStr.add(types[0]);
                    }
                    else if ("Restrict".equalsIgnoreCase(types[1]))
                    {
                        changeToRestrictStr.add(types[0]);
                    }
                    else if ("Support".equalsIgnoreCase(types[1]))
                    {
                        changeToSupportStr.add(types[0]);
                    }
                }
            }
        }

        if (deletedReferences == null)
            deletedReferences = new ArrayList();
        try
        {
            Vector deleteFiles = new Vector();
            if (deleteIndexs != null)
            {
                for (int i = 0; i < deleteIndexs.length; ++i)
                {
                    CommentFile item = (CommentFile) commentReferences
                            .get(Integer.parseInt(deleteIndexs[i]));
                    deleteFiles.addElement(item);
                    if (item.isSaved())
                    {
                        deletedReferences.add(item);
                    }
                }
            }

            Vector<CommentFile> changeToRestricts = new Vector<CommentFile>();
            Vector<CommentFile> changeToGenerals = new Vector<CommentFile>();
            Vector<CommentFile> changeToSupports = new Vector<CommentFile>();
            for (int i = 0; i < commentReferences.size(); i++)
            {
                CommentFile item = (CommentFile) commentReferences.get(i);
                if (deleteFiles.contains(item))
                {
                    continue;
                }

                if (changeToGeneralStr.size() > 0
                        && changeToGeneralStr.contains(String.valueOf(item.getAbsolutePath()
                                .hashCode())))
                {
                    changeToGenerals.add(item);
                }
                else if (changeToRestrictStr.size() > 0
                        && changeToRestrictStr.contains(String.valueOf(item.getAbsolutePath()
                                .hashCode())))
                {
                    changeToRestricts.add(item);
                }
                else if (changeToSupportStr.size() > 0
                        && changeToSupportStr.contains(String.valueOf(item.getAbsolutePath()
                                .hashCode())))
                {
                    changeToSupports.add(item);
                }
            }

            sessionMgr.setAttribute("deletedReferences", deletedReferences);

            String commentId = (String) sessionMgr.getAttribute("commentId");
            deleteCommentReferences(deleteFiles, tmpDir, commentId);
            updateToGeneralFiles(changeToGenerals, tmpDir, commentId);
            updateToStrictFiles(changeToRestricts, tmpDir, commentId);
            updateToSupportFiles(changeToSupports,tmpDir,commentId);
        }
        catch (Exception ex)
        {
            // Well, as long as the file does not exist now we're happy.
            // If we couldn't delete it it'll show up again in the list.
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Could not delete Comment Reference file: " + ex);
            }
        }
    }

    /**
     * Calls the remote server to delete a Comment Reference file.
     */
    private void deleteCommentReferences(Vector fileList, String tmpDir,
            String commentId) throws EnvoyServletException
    {
        try
        {
            for (int i = 0; i < fileList.size(); i++)
            {
                CommentFile item = (CommentFile) fileList.elementAt(i);
                if (item.isSaved())
                {
                    ServerProxy.getCommentManager().deleteCommentReference(
                            item, commentId);
                }
                else
                {
                    ServerProxy.getCommentManager().deleteCommentReference(
                            item, tmpDir);
                }
            }
        }
        catch (Exception ex)
        {
            // Well, as long as the file does not exist now we're happy.
            // If we couldn't delete it it'll show up again in the list.
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Could not delete Comment Reference file: " + ex);
            }
        }
    }

    private void updateToGeneralFiles(Vector<CommentFile> files, String tmpDir,
            String commentId)
    {
        try
        {
            for (CommentFile file : files)
            {
                String dir = file.isSaved() ? commentId : tmpDir;
                ServerProxy.getCommentManager().changeToGeneral(file, dir);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
        }
    }

    private void updateToStrictFiles(Vector<CommentFile> files, String tmpDir,
            String commentId)
    {
        try
        {
            for (CommentFile file : files)
            {
                String dir = file.isSaved() ? commentId : tmpDir;
                ServerProxy.getCommentManager().changeToRestrict(file, dir);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
        }
    }
    
    private void updateToSupportFiles(Vector<CommentFile> files, String tmpDir,
            String commentId)
    {
        try
        {
            for (CommentFile file : files)
            {
                String dir = file.isSaved() ? commentId : tmpDir;
                ServerProxy.getCommentManager().changeToSupport(file, dir);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
        }
    }

}
