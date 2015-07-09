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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.WorkflowStatePostComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.util.StringUtil;

public class WorkflowStatePostHandler extends PageHandler implements
        WorkflowStatePostConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(WorkflowStatePostHandler.class.getName());
    private static int numPerPage = 20;

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws EnvoyServletException,
            ServletException, IOException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = p_request.getParameter("action");
        if (SAVE_ACTION.equals(action))
        {
            WorkflowStatePosts wfStatePost = getBasicWfStatePostProfile(p_request);
            String companyId = CompanyThreadLocal.getInstance().getValue();
            wfStatePost.setCompanyId(Long.parseLong(companyId));
            createWfStatePostProfile(wfStatePost);
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=workflowStatePost");
            return;
        }
        else if ("check".equals(action))
        {
            Map<String, Object> map = WorkflowStatePostHandlerHelper
                    .checkWfStatePostProfile(p_request);
            ServletOutputStream out = p_response.getOutputStream();
            out.write(JsonUtil.toJson(map).getBytes("UTF-8"));
            out.close();
            return;
        }
        else if ("remove".equals(action))
        {
            String wfStatePostId = p_request.getParameter("radioBtn");
            if (StringUtil.isNotEmpty(wfStatePostId))
            {
                removeWorkflowStatePost(Long.parseLong(wfStatePostId));
            }
        }
        else if (MODIFY_ACTION.equals(action))
        {
            WorkflowStatePosts wfStatePosts = getBasicWfStatePostProfile(p_request);
            WorkflowStatePosts wfStatePost = (WorkflowStatePosts) sessionMgr
                    .getAttribute(WF_STATE_POST_INFO);
            wfStatePosts.setCompanyId(wfStatePost.getCompanyId());
            editWfStatePostProfile(p_request, wfStatePosts);
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=workflowStatePost");
            return;
        }
        try
        {
            dataForTable(p_request, session, getFilterParameters(p_request));
        }
        catch (Exception ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        finally
        {
            // Call parent invokePageHandler() to set link beans and invoke JSP
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
    }

    private WorkflowStatePosts getBasicWfStatePostProfile(
            HttpServletRequest p_request)
    {
        String value;
        WorkflowStatePosts wfStatePost = new WorkflowStatePosts();
        HttpSession session = p_request.getSession(false);
        String name = (String) p_request.getParameter(NAME_FIELD);
        String description = (String) p_request.getParameter(DESCRIPTION_FIELD);
        String listenerURL = (String) p_request.getParameter(LISTENERURL_FIELD);
        String secretKey = (String) p_request.getParameter(SECRETKEY_FIELD);
        value = (String) p_request.getParameter(TIMEOUT_FIELD);
        int timeoutPeriod = Integer.parseInt(value);
        value = (String) p_request.getParameter(RETRY_TIME_FIELD);
        int retryNumber = Integer.parseInt(value);
        String notifyEmail = (String) p_request
                .getParameter(NOTIFY_EMAIL_FIELD);

        wfStatePost.setName(name);
        wfStatePost.setDescription(description);
        wfStatePost.setListenerURL(listenerURL);
        wfStatePost.setSecretKey(secretKey);
        wfStatePost.setTimeoutPeriod(timeoutPeriod);
        wfStatePost.setRetryNumber(retryNumber);
        wfStatePost.setNotifyEmail(notifyEmail);
        return wfStatePost;
    }

    private void editWfStatePostProfile(HttpServletRequest p_request,
            WorkflowStatePosts wfstaPosts)
    {
        if (p_request.getParameter("wfStatePostId") != null)
        {
            long wfStatePostInfoId = Long.parseLong(p_request
                    .getParameter("wfStatePostId"));
            wfstaPosts.setId(wfStatePostInfoId);
            WorkflowStatePostHandlerHelper.modifyWfStatePostProfile(wfstaPosts);
        }
    }

    /**
     * @param p_request
     */
    private void createWfStatePostProfile(WorkflowStatePosts wfStatePost)
    {
        WorkflowStatePostHandlerHelper.createWfStatePostProfile(wfStatePost);
    }

    private String[] getFilterParameters(HttpServletRequest p_request)
    {
        String action = p_request.getParameter("action");
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String name = p_request.getParameter("nameFilter");
        String listenerURL = p_request.getParameter("listenerURLFilter");
        String company = p_request.getParameter("wfStatePostCompanyNameFilter");
        if (!FILTER_SEARCH.equals(action)
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            name = (String) sessionMgr.getAttribute("nameFilter");
            listenerURL = (String) sessionMgr.getAttribute("listenerURLFilter");
            company = (String) sessionMgr
                    .getAttribute("wfStatePostCompanyNameFilter");
        }
        if (FILTER_SEARCH.equals(action))
        {
            // Go to page #1 if current action is filter searching
            sessionMgr.setAttribute(WFSPPROFILE_KEY
                    + TableConstants.LAST_PAGE_NUM, Integer.valueOf(1));
        }
        name = name == null ? "" : name;
        listenerURL = listenerURL == null ? "" : listenerURL;
        company = company == null ? "" : company;
        sessionMgr.setAttribute("nameFilter", name);
        sessionMgr.setAttribute("listenerURLFilter", listenerURL);
        sessionMgr.setAttribute("wfStatePostCompanyNameFilter", company);
        String[] filterParam = { name, listenerURL, company };
        return filterParam;
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, String[] filterParams)
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        List<WorkflowStatePosts> wfStatePosts = null;
        wfStatePosts = WorkflowStatePostHandlerHelper
                .getAllWfStatePostForGUI(filterParams);
        setNumberPerPage(p_request);
        setTableNavigation(p_request, p_session, wfStatePosts,
                new WorkflowStatePostComparator(uiLocale), numPerPage,
                WorkflowStatePostConstants.WFSPPROFILE_LIST,
                WorkflowStatePostConstants.WFSPPROFILE_KEY);
    }

    private void setNumberPerPage(HttpServletRequest p_request)
    {
        String pageSize = (String) p_request.getParameter("numOfPageSize");
        if (!StringUtil.isEmpty(pageSize))
        {
            try
            {
                numPerPage = Integer.parseInt(pageSize);
            }
            catch (Exception e)
            {
                numPerPage = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Remove the selected workflow state post profile.
     * 
     * @param wfStatePostId
     * @exception ServletException
     * @exception IOException
     * @exception EnvoyServletException
     */
    private void removeWorkflowStatePost(long wfStatePostId)
    {
        WorkflowStatePostHandlerHelper.removeWorkflowStatePost(wfStatePostId);
    }
}
