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

import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;

import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.SegmentProtectionManager;
import com.globalsight.everest.edit.online.CommentView;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * <p>CommentEditorPageHandler is responsible for:</p>
 * <ol>
 * <li>Displaying the comment editor as part of the online editor.</li>
 * <li>Handling segment comment persistence.</li>
 * </ol>
 */

public class CommentEditorPageHandler
    extends PageHandler
    implements EditorConstants
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            CommentEditorPageHandler.class);

    //
    // Constructor
    //
    public CommentEditorPageHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
    * Invokes this PageHandler
    *
    * @param p_pageDescriptor the page desciptor
    * @param p_request the original request sent from the browser
    * @param p_response the original response object
    * @param p_context context the Servlet context
    */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
      HttpServletRequest p_request, HttpServletResponse p_response,
      ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();

        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState)sessionMgr.getAttribute(
            WebAppConstants.EDITORSTATE);

        String value;
        if ((value = p_request.getParameter("tuId")) != null)
        {
            state.setTuId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("tuvId")) != null)
        {
            state.setTuvId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("subId")) != null)
        {
            state.setSubId(Long.parseLong(value));
        }
        

        long tuId  = state.getTuId();
        long tuvId = state.getTuvId();
        long subId = state.getSubId();

        long commentId = -1;
        if ((value = p_request.getParameter("commentId")) != null)
        {
            commentId = Long.parseLong(value);
        }

        CommentView view = EditorHelper.getCommentView(state, commentId,
            tuId, tuvId, subId);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("CommentView for " +
                tuId + "_" + tuvId + "_" + subId + " (id=" + commentId +
                ") is " + view);
        }
        
        if ((value = p_request.getParameter("commentUpload")) != null)
        {
            try{
                FileUploadHelper o_upload = new FileUploadHelper();
                p_request.setAttribute("fileName", "tuv_" + Long.toString(view.getTuvId()));
                p_request.setAttribute("filePath", "terminologyImg");
                o_upload.doUpload(p_request);
            }
                catch(Exception e) {}
        }

        sessionMgr.setAttribute(WebAppConstants.COMMENTVIEW, view);

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}
