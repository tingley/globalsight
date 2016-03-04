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
package com.globalsight.everest.webapp.pagehandler.edit.online4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReader;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.util.GlobalSightLocale;

/**
 * <p>
 * EditorPageHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the editor screen with both source and target page.</li>
 * <li>Showing the Segment Editor.</li>
 * </ol>
 */
public class EditorImageHandler extends PageHandler implements EditorConstants
{
    private static final Logger CATEGORY = Logger.getLogger(EditorImageHandler.class);
    
    public EditorImageHandler()
    {
        super();
    }

    /**
     * Prepares the EditorState object that all invocations of this PageHandler
     * require. (Almost) All Main Editor pages (me_xx.jsp) go through this
     * handler.
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
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        String taskId = p_request.getParameter(WebAppConstants.TASK_ID);
        String srcPageId = p_request.getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String trgPageId = p_request.getParameter(WebAppConstants.TARGET_PAGE_ID);
        String jobId = p_request.getParameter(WebAppConstants.JOB_ID);
        String trgId = p_request.getParameter("trgId");
        
        User user = TaskHelper.getUser(session);
        Boolean assigneeValue = (Boolean) TaskHelper.retrieveObject(session,
                WebAppConstants.IS_ASSIGNEE);
        boolean isAssignee = assigneeValue == null ? true : assigneeValue.booleanValue();
        String action = p_request.getParameter(WebAppConstants.UPLOAD_ACTION);
        if (action != null && WebAppConstants.UPLOAD_ACTION_START_UPLOAD.equals(action))
        {
            MultipartFormDataReader reader = new MultipartFormDataReader();
            User p_user = (User) sessionMgr.getAttribute(USER);
            File p_tempFile = reader.uploadToTempFile(p_request);
            PageManager pm = ServerProxy.getPageManager();
            TargetPage tp = pm.getTargetPage(Long.parseLong(trgPageId));
            UnextractedFile uf = (UnextractedFile) tp.getPrimaryFile();
            try
            {
                ServerProxy.getNativeFileManager().save(uf, p_tempFile, p_user);
            }
            catch (Exception e)
            {
                CATEGORY.info(e.getMessage());
            }
        }


        if (taskId != null && srcPageId != null && trgPageId != null)
        {
            sessionMgr.setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "yes");
            // store jobId, target language and source page id for Lisa QA
            // report
            Task theTask = (Task) TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
            initializeFromActivity(p_request, user.getUserId(), taskId, srcPageId,
                    trgPageId, isAssignee, uiLocale);

            sessionMgr.setAttribute(WebAppConstants.JOB_ID, Long.toString(theTask.getJobId()));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);
            sessionMgr.setAttribute(WebAppConstants.TASK_ID, taskId);
            sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_ID, trgPageId);
        }
        else if (p_request.getParameter("action") != null
                && "refresh".equals(p_request.getParameter("action"))
                || (jobId != null && srcPageId != null))
        {
            TaskHelper.storeObject(session, IS_ASSIGNEE, new Boolean(isAssignee));
            sessionMgr.setAttribute(WebAppConstants.JOB_ID, Long.parseLong(jobId));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);
            initializeFromJob(p_request,jobId,srcPageId,trgId,uiLocale,user);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    private void initializeFromActivity(HttpServletRequest p_request, String p_userId,
            String p_taskId, String p_srcPageId, String p_trgPageId, boolean p_isAssignee,
            Locale p_uiLocale) throws EnvoyServletException
    {
        try
        {
            Task task = ServerProxy.getTaskManager().getTask(p_userId, Long.parseLong(p_taskId),
                    WorkflowConstants.TASK_ALL_STATES);
            List<TargetPage> targetPages = task.getTargetPages();
            for (TargetPage targetPage : targetPages)
            {
                if (targetPage.getId() == Long.parseLong(p_trgPageId))
                {
                    UnextractedFile unextractedFile = (UnextractedFile) targetPage.getPrimaryFile();
                    String pageName = unextractedFile.getStoragePath().replace("\\", "/");
                    String tgurl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + pageName;
                    tgurl = URLEncoder.encodeUrlStr(tgurl);
                    tgurl = tgurl.replace("%2F", "/");
                    p_request.setAttribute("targetImagePath", tgurl);
                }
            }

            List<SourcePage> unExtractedSrcs = task.getSourcePages(PrimaryFile.UNEXTRACTED_FILE);
            for (SourcePage sp : unExtractedSrcs)
            {
                if (Long.parseLong(p_srcPageId) == sp.getId())
                {
                    UnextractedFile unextractedSrc = (UnextractedFile) sp.getPrimaryFile();
                    String spName = unextractedSrc.getStoragePath().replace("\\", "/");
                    String spUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + spName;
                    spUrl = URLEncoder.encodeUrlStr(spUrl);
                    spUrl = spUrl.replace("%2F", "/");
                    p_request.setAttribute("sourceImagePath", spUrl);
                }
            }
            String sourceLanguage = task.getSourceLocale().getDisplayName(p_uiLocale);
            String targetLanguage = task.getTargetLocale().getDisplayName(p_uiLocale);

            p_request.setAttribute("sourceLanguage", sourceLanguage);
            p_request.setAttribute("targetLanguage", targetLanguage);
            
            boolean isCanUpload = false;
            int state = task.getState();
            if (state == Task.STATE_ACCEPTED && TaskImpl.TYPE_TRANSLATE == task.getType())
            {
                isCanUpload = true;
            }
            p_request.setAttribute("isCanUpload", String.valueOf(isCanUpload));
        }
        catch (Exception e)
        {
            CATEGORY.info(e);
        }
    }
    
    private void initializeFromJob(HttpServletRequest p_request, String p_jobId,
            String p_srcPageId, String trgId, Locale p_uiLocale, User p_user)
            throws EnvoyServletException
    {
        try
        {
            Iterator it1, it2;
            SourcePage srcPage;
            TargetPage trgPage;
            UnextractedFile unextractedTrg,unextractedSrc;
            GlobalSightLocale trgLocale;
            int count = 0;
            List<GlobalSightLocale> targetLocalesList = new ArrayList<GlobalSightLocale>();
            Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(p_jobId));
            for (it1 = job.getWorkflows().iterator(); it1.hasNext();)
            {
                Workflow workflow = (Workflow) it1.next();
                trgLocale = workflow.getTargetLocale();
                targetLocalesList.add(trgLocale);
               
                count++;
                for (it2 = workflow.getTargetPages().iterator(); it2.hasNext();)
                {
                    trgPage = (TargetPage) it2.next();
                    srcPage = trgPage.getSourcePage();
                    if (srcPage.getId() == Long.parseLong(p_srcPageId))
                    {
                        if (trgId != null && trgLocale.getId() != Long.parseLong(trgId))
                        {
                            continue;
                        }
                        unextractedTrg = (UnextractedFile) trgPage.getPrimaryFile();
                        String pageName = unextractedTrg.getStoragePath().replace("\\", "/");
                        String tgurl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + pageName;
                        tgurl = URLEncoder.encodeUrlStr(tgurl);
                        tgurl = tgurl.replace("%2F", "/");

                        unextractedSrc = (UnextractedFile) srcPage.getPrimaryFile();
                        String spName = unextractedSrc.getStoragePath().replace("\\", "/");
                        String spUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + spName;
                        spUrl = URLEncoder.encodeUrlStr(spUrl);
                        spUrl = spUrl.replace("%2F", "/");

                        if (count == 1 || (trgId != null && trgLocale.getId() == Long.parseLong(trgId)))
                        {
                            p_request.setAttribute("targetImagePath", tgurl);
                            p_request.setAttribute("sourceImagePath", spUrl);
                            p_request.setAttribute("currentLocaleId", trgLocale.getId());
                            p_request.setAttribute("sourceLanguage", srcPage
                                    .getGlobalSightLocale().getDisplayName(p_uiLocale));
                            p_request.setAttribute("targetLanguage", trgPage
                                    .getGlobalSightLocale().getDisplayName(p_uiLocale));
                        }
                    }
                }
            }
            p_request.setAttribute("targetLocalesList", targetLocalesList);
        }
        catch (Exception e)
        {
            CATEGORY.info(e);
        }
    }
}