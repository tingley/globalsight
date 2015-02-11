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

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

/**
 * <p>SegmentEditorPageHandler is responsible for:</p>
 * <ol>
 * <li>Displaying the segment editor screen.</li>
 * </ol>
 */
public class ImageUploadPageHandler extends PageHandler implements
        EditorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(ImageUploadPageHandler.class);

    //
    // Constructor
    //
    public ImageUploadPageHandler()
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
        SegmentView segmentView = (SegmentView)sessionMgr.getAttribute(
            WebAppConstants.SEGMENTVIEW);

        try
        {
            EditorHelper.uploadImage(p_request, state, segmentView);
        }
        catch (/*EnvoyServletException + RemoteException*/ Exception e)
        {
            CATEGORY.error("Image upload error", e);
        }

        long tuId  = state.getTuId();
        long tuvId = state.getTuvId();
        long subId = state.getSubId();

        Long targetPageId   = state.getTargetPageId();
        long sourceLocaleId = state.getSourceLocale().getId();
        long targetLocaleId = state.getTargetLocale().getId();

        segmentView = EditorHelper.getSegmentView(state, tuId, tuvId, subId,
            targetPageId.longValue(), sourceLocaleId, targetLocaleId);

        sessionMgr.setAttribute(WebAppConstants.SEGMENTVIEW, segmentView);

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}
