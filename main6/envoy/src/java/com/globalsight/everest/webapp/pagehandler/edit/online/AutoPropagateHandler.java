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

package com.globalsight.everest.webapp.pagehandler.edit.online;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;

public class AutoPropagateHandler extends PageActionHandler
{
    private static final Logger logger = Logger
            .getLogger(AutoPropagateHandler.class);
    private AutoPropagateThread apThread = null;

    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }

    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // All actions need this parameter
        String targetPageId = request.getParameter("targetPageId");
        request.setAttribute("targetPageId", targetPageId);
        // Set "needRefreshOpener" as "no" as default.
        request.setAttribute("needRefreshOpener", "No");
    }

    @ActionHandler(action = "default", formClass = "")
    public void skipToPropagate(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        // Nothing to do.
    }

    /**
     * Propagate by "Auto-Propagate Options"
     */
    @ActionHandler(action = "propagate", formClass = "")
    public void propagate(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        User user = TaskHelper.getUser(session);
        String targetPageId = p_request.getParameter("targetPageId");

        String tuScope = p_request.getParameter("tuScope");
        String specTus = p_request.getParameter("specifiedTusField");
        String tuvScope = p_request.getParameter("tuvScope");
        String pickup = p_request.getParameter("pickup");
        // Propagate repetitions in thread.
        propagateReps(targetPageId, tuScope, specTus, tuvScope, pickup, user);

        // Clear target TUVs in cache to ensure target page will be fresh
        // correctly.
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
        state.getEditorManager().invalidateCache();

        p_request.setAttribute("tuScope", tuScope);
        p_request.setAttribute("tuvScope", tuvScope);
        p_request.setAttribute("pickup", pickup);
        p_request.setAttribute("needRefreshOpener", "Yes");
    }

    @ActionHandler(action = "getPercentage", formClass = "")
    public void getpercentage(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        p_request.setAttribute("needRefreshOpener", "Yes");

        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            p_response.setContentType("text/plain");
            out = p_response.getOutputStream();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"propagatePercentage\":");
            sb.append(apThread.getPropagatePercentage()).append("}");
            out.write(sb.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    /**
     * Propagate in separate thread for showing progress bar on UI.
     */
    public void propagateReps(final String targetPageId, final String tuScope,
            final String specTus, final String tuvScope, final String pickup,
            final User user)
    {
        apThread = new AutoPropagateThread();
        apThread.setPickup(pickup);
        apThread.setSpecTus(specTus);
        apThread.setTargetPageId(targetPageId);
        apThread.setTuScope(tuScope);
        apThread.setTuvScope(tuvScope);
        apThread.setUser(user);
        
        Thread t = new MultiCompanySupportedThread(apThread);
        t.setName("PROPAGATE " + String.valueOf(user.getUserName()));
        t.start();
    }
}
