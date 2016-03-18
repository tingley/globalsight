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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;

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
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState.PagePair;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReader;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

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
        EditorState state = (EditorState) sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        String taskId = p_request.getParameter(WebAppConstants.TASK_ID);
        String srcPageId = p_request.getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String trgPageId = p_request.getParameter(WebAppConstants.TARGET_PAGE_ID);
        String jobId = p_request.getParameter(WebAppConstants.JOB_ID);
        String isFromActivity = (String) sessionMgr.getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
        String openEditorType = p_request.getParameter("openEditorType");
        
        User user = TaskHelper.getUser(session);
        String action = p_request.getParameter(WebAppConstants.USER_ACTION);
        if (StringUtil.isNotEmpty(action))
        {
            if ("getPictureData".equalsIgnoreCase(action))
            {
                if (isFromActivity != null && isFromActivity.equalsIgnoreCase("yes"))
                {
                    getPictureFromActivity(p_response, user.getUserId(), taskId, srcPageId,
                            trgPageId);
                    return;
                }
                else
                {
                    getPictureFromJob(p_response, jobId, srcPageId, trgPageId);
                    return;
                }
            }
            else if (WebAppConstants.UPLOAD_ACTION_START_UPLOAD.equals(action))
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
                    initializeFromActivity(p_request, user.getUserId(), taskId, uiLocale);
                }
                catch (Exception e)
                {
                    CATEGORY.info(e.getMessage());
                }
            }
            else if ("refresh".equalsIgnoreCase(action))
            {
                refresh(p_request, user.getUserId(), taskId, uiLocale);
            }
            else if ("switchTargetLocale".equalsIgnoreCase(action))
            {
                setCurrentPageFromJob(p_request.getSession(), state, srcPageId, jobId);
                initializeFromJob(p_request, state, uiLocale);
            }
        }
        else
        {
            state = new EditorState();
            state.setOpenEditorType(openEditorType);
            if (StringUtil.isNotEmptyAndNull(taskId) && StringUtil.isNotEmpty(srcPageId)
                    && StringUtil.isNotEmpty(trgPageId))
            {
                initializeFromActivity(p_request, user.getUserId(), taskId, uiLocale);
                setCurrentPageFromActivity(p_request, state, user.getUserId(), taskId, srcPageId);

                p_request.setAttribute(WebAppConstants.TASK_ID, taskId);
                sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);
                sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_ID, trgPageId);
                sessionMgr.setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "yes");
            }
            else if (StringUtil.isNotEmpty(jobId) && StringUtil.isNotEmpty(srcPageId))
            {
                setCurrentPageFromJob(p_request.getSession(), state, srcPageId, jobId);
                initializeFromJob(p_request, state, uiLocale);

                sessionMgr.setAttribute(WebAppConstants.JOB_ID, jobId);
                sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);
                sessionMgr.setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "no");
            }
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    private void getPictureFromActivity(HttpServletResponse p_response, String p_userId,
            String p_taskId, String p_srcPageId, String p_trgPageId)
    {
        JSONObject mainJson = new JSONObject();
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

                    String name = unextractedFile.getName();
                    String suffix = name.substring(name.lastIndexOf(".")+1, name.length());
                    if (suffix != null)
                    {
                        mainJson.put("targetImageSuffix", suffix);
                    }

                    mainJson.put("targetImagePath", tgurl);
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
                    mainJson.put("sourceImagePath", spUrl);
                }
            }

            p_response.setContentType("text/html;charset=UTF-8");
            ServletOutputStream out = p_response.getOutputStream();
            out.write(mainJson.toString().getBytes("UTF-8"));
            out.close();
        }
        catch (Exception e)
        {
            CATEGORY.info(e.getMessage());
        }
    }
    
    private void getPictureFromJob(HttpServletResponse p_response, String p_jobId,
            String p_srcPageId, String p_trgPageId)
    {
        JSONObject mainJson = new JSONObject();
        try
        {
            Iterator it, it1;
            SourcePage srcPage;
            TargetPage trgPage;
            UnextractedFile unextractedTrg, unextractedSrc;
            Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(p_jobId));

            for (it = job.getSourcePages().iterator(); it.hasNext();)
            {
                srcPage = (SourcePage) it.next();
                
                if (srcPage.getId() != Long.parseLong(p_srcPageId))
                    continue;
                
                for (it1 = srcPage.getTargetPages().iterator(); it1.hasNext();)
                {
                    trgPage = (TargetPage) it1.next();
                    
                    if (trgPage.getId() != Long.parseLong(p_trgPageId))
                        continue;

                    unextractedSrc = (UnextractedFile) srcPage.getPrimaryFile();
                    String spName = unextractedSrc.getStoragePath().replace("\\", "/");
                    String spUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + spName;
                    spUrl = URLEncoder.encodeUrlStr(spUrl);
                    spUrl = spUrl.replace("%2F", "/");

                    unextractedTrg = (UnextractedFile) trgPage.getPrimaryFile();
                    String pageName = unextractedTrg.getStoragePath().replace("\\", "/");
                    String tgurl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + pageName;
                    tgurl = URLEncoder.encodeUrlStr(tgurl);
                    tgurl = tgurl.replace("%2F", "/");

                    String name = unextractedTrg.getName();
                    if (name.lastIndexOf(".") != -1)
                    {
                        mainJson.put("targetImageSuffix",
                                name.substring(name.lastIndexOf(".") + 1, name.length()));
                    }
                    mainJson.put("sourceImagePath", spUrl);
                    mainJson.put("targetImagePath", tgurl);
                }
            }

            p_response.setContentType("text/html;charset=UTF-8");
            ServletOutputStream out = p_response.getOutputStream();
            out.write(mainJson.toString().getBytes("UTF-8"));
            out.close();
        }
        catch (Exception e)
        {
            CATEGORY.info(e);
        }
    }
    
    private void initializeFromActivity(HttpServletRequest p_request, String p_userId,
            String p_taskId, Locale p_uiLocale) throws EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession();
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            Task task = ServerProxy.getTaskManager().getTask(p_userId, Long.parseLong(p_taskId),
                    WorkflowConstants.TASK_ALL_STATES);
            String sourceLanguage = task.getSourceLocale().getDisplayName(p_uiLocale);
            String targetLanguage = task.getTargetLocale().getDisplayName(p_uiLocale);

            p_request.setAttribute("sourceLanguage", sourceLanguage);
            p_request.setAttribute("targetLanguage", targetLanguage);

            boolean isCanUpload = false;
            int taskState = task.getState();
            if (taskState == Task.STATE_ACCEPTED && TaskImpl.TYPE_TRANSLATE == task.getType())
            {
                isCanUpload = true;
            }
            p_request.setAttribute("isCanUpload", String.valueOf(isCanUpload));
            sessionMgr.setAttribute("targetLocale", task.getTargetLocale());
        }
        catch (Exception e)
        {
            CATEGORY.info(e);
        }
    }
    
    private void initializeFromJob(HttpServletRequest p_request, EditorState p_state,
            Locale p_uiLocale) throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        try
        {
            if (StringUtil.isNotEmpty(p_request.getParameter("trgViewLocale")))
            {
                p_state.setTargetViewLocale(EditorHelper.getLocale(p_request
                        .getParameter("trgViewLocale")));

                sessionMgr.setAttribute("trgViewLocale",
                        EditorHelper.getLocale(p_request.getParameter("trgViewLocale"))
                                .getDisplayName());
            }
            GlobalSightLocale viewLocale = p_state.getTargetViewLocale();
            Vector<GlobalSightLocale> trgLocales = p_state.getJobTargetLocales();
            if (viewLocale == null)
            {
                p_state.setTargetViewLocale((GlobalSightLocale) trgLocales.elementAt(0));
            }

            long targetPageId = p_state.getCurrentPage().getTargetPageId(
                    p_state.getTargetViewLocale());
            p_request.setAttribute("sourceLanguage",
                    p_state.getSourceLocale().getDisplayName(p_uiLocale));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID,
                    String.valueOf(p_state.getCurrentPage().getSourcePageId()));
            sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_ID, String.valueOf(targetPageId));
            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, p_state);
        }
        catch (Exception e)
        {
            CATEGORY.info(e);
        }
    }
    
    public void refresh(HttpServletRequest p_request,String p_userId,String p_taskId,Locale uiLocale)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
        String value = p_request.getParameter("refresh");
        int i_direction = 0;
        if (!value.startsWith("0"))
            i_direction = Integer.parseInt(value);
        boolean fromActivity = false;
        String att = (String) sessionMgr.getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
        if (att != null && att.equals("yes"))
        {
            fromActivity = true;
        }
        if (i_direction == -1) // previous file
        {
            previousPage(state, p_request.getSession(), fromActivity);
        }
        else if (i_direction == 1) // next file
        {
            nextPage(state, p_request.getSession(), fromActivity);
        }
        
        if (fromActivity)
        {
            PagePair currentPage = state.getCurrentPage();
            String sourcePageId = String.valueOf(currentPage.getSourcePageId());
            String targetPageId = String.valueOf(currentPage
                    .getTargetPageId((GlobalSightLocale) sessionMgr.getAttribute("targetLocale")));
            initializeFromActivity(p_request, p_userId, p_taskId, uiLocale);
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, sourcePageId);
            sessionMgr.setAttribute(WebAppConstants.TASK_ID, p_taskId);
            sessionMgr.setAttribute(WebAppConstants.TARGET_PAGE_ID, targetPageId);
        }
        else
        {
            initializeFromJob(p_request,state,uiLocale);
        }
    }
    
    private void setCurrentPageFromActivity(HttpServletRequest p_request, EditorState p_state,
            String p_userId, String p_taskId, String p_srcPageId)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(p_userId, Long.parseLong(p_taskId),
                    WorkflowConstants.TASK_ALL_STATES);
            EditorHelper.setPagesInActivity(p_state, task);
            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, p_state);
        }
        catch (Exception e)
        {
            CATEGORY.info(e.getMessage());
        }

        ArrayList pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(session, pages);
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;
        int offset = 0;
        boolean foundPage = false;
        boolean allEmptyBefore = true;
        boolean allEmptyAfter = true;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId=" + p_srcPageId);
            }

            if (!foundPage && pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                foundPage = true;
                offset = i_offset;
                continue;
            }

            if (foundPage && allEmptyAfter)
            {
                allEmptyAfter = false;
                break;
            }
            else if (!foundPage && allEmptyBefore)
            {
                allEmptyBefore = false;
            }
        }

        p_state.setIsFirstPage(offset == 1);
        p_state.setIsLastPage(pages.size() == offset);

        if (allEmptyBefore)
        {
            p_state.setIsFirstPage(true);
        }

        if (allEmptyAfter)
        {
            p_state.setIsLastPage(true);
        }
    }
    
    private void setCurrentPageFromJob(HttpSession p_session, EditorState p_state,
            String p_srcPageId,String p_jobId)
    {
        try
        {
            SessionManager sessionMgr = (SessionManager) p_session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(p_jobId));
            EditorHelper.setPagesInJob(p_state, job);
            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, p_state);
        }
        catch (Exception e)
        {
            CATEGORY.info(e.getMessage());
        }
        
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId);
            }

            if (pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_offset == 1);
        p_state.setIsLastPage(pages.size() == i_offset);
    }
    
    private List<EditorState.PagePair> getPagePairList(HttpSession p_session,
            List<EditorState.PagePair> pages)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List<Long> sourcePageIdList = (List<Long>) sessionMgr
                .getAttribute("sourcePageIdList");

        List<EditorState.PagePair> newPages = new ArrayList<EditorState.PagePair>();
        if (sourcePageIdList != null && sourcePageIdList.size() > 0)
        {
            for (int i = 0; i < pages.size(); i++)
            {
                EditorState.PagePair page = pages.get(i);
                if (sourcePageIdList.contains(page.getSourcePageId()))
                {
                    newPages.add(page);
                }
            }
        }
        else
        {
            newPages = pages;
        }
        return newPages;
    }
    
    private void previousPage(EditorState p_state, HttpSession p_session,
            boolean p_fromActivity) throws EnvoyServletException
    {
        ArrayList<EditorState.PagePair> pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
        int i_index = pages.indexOf(p_state.getCurrentPage());

        if (p_fromActivity)
        {
            boolean foundNonempty = false;
            boolean allEmptyBefore = true;
            while (i_index > 0)
            {
                --i_index;
                EditorState.PagePair pp = (EditorState.PagePair) pages
                        .get(i_index);

                if (!foundNonempty)
                {
                    p_state.setCurrentPage(pp);
                    p_state.setIsFirstPage(i_index == 0);
                    p_state.setIsLastPage(false);
                    foundNonempty = true;
                    continue;
                }

                if (foundNonempty && allEmptyBefore)
                {
                    allEmptyBefore = false;
                    break;
                }

            }
            if (foundNonempty && allEmptyBefore)
            {
                p_state.setIsFirstPage(true);
            }
        }
        else
        {
            if (i_index > 0)
            {
                --i_index;

                p_state.setCurrentPage((EditorState.PagePair) pages
                        .get(i_index));

                p_state.setIsFirstPage(i_index == 0);
                p_state.setIsLastPage(false);
            }
        }

    }
    
    private void nextPage(EditorState p_state, HttpSession p_session,
            boolean p_fromActivity) throws EnvoyServletException
    {
        ArrayList<EditorState.PagePair> pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
        int i_index = pages.indexOf(p_state.getCurrentPage());

        if (p_fromActivity)
        {
            boolean foundNonempty = false;
            boolean allEmptyAfter = true;

            while (i_index >= 0 && i_index < (pages.size() - 1))
            {
                ++i_index;

                EditorState.PagePair pp = (EditorState.PagePair) pages
                        .get(i_index);

                if (!foundNonempty)
                {
                    p_state.setCurrentPage(pp);
                    p_state.setIsFirstPage(false);
                    p_state.setIsLastPage(i_index == (pages.size() - 1));
                    foundNonempty = true;
                    continue;
                }

                if (foundNonempty && allEmptyAfter)
                {
                    allEmptyAfter = false;
                    break;
                }

            }
            if (foundNonempty && allEmptyAfter)
            {
                p_state.setIsLastPage(true);
            }
        }

        else
        {
            if (i_index >= 0 && i_index < (pages.size() - 1))
            {
                ++i_index;

                p_state.setCurrentPage((EditorState.PagePair) pages
                        .get(i_index));

                p_state.setIsFirstPage(false);
                p_state.setIsLastPage(i_index == (pages.size() - 1));
            }
        }
    }
}