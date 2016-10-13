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

import com.globalsight.everest.edit.online.OnlineEditorException;
import com.globalsight.everest.edit.online.imagereplace.ImageReplace;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;


/**
 * <p>SegmentEditorPageHandler is responsible for:</p>
 * <ol>
 * <li>Displaying the segment editor screen.</li>
 * </ol>
 */
public class ImageUploadPageHandler2
    extends PageHandler
    implements EditorConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
          ImageUploadPageHandler2.class);

    //
    // Constructor
    //
    public ImageUploadPageHandler2()
    {
    }

    //
    // Interface Methods: PageHandler
    //

    /**
    * Invokes this PageHandler
    *
    * @param p_thePageDescriptor the page desciptor
    * @param p_theRequest the original request sent from the browser
    * @param p_theResponse the original response object
    * @param p_context context the Servlet context
    */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
      HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
      ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        dispatchJSP(p_thePageDescriptor, p_theRequest, p_theResponse, p_context);
        super.invokePageHandler(p_thePageDescriptor, p_theRequest, 
                                p_theResponse, p_context);
    }

    //
    // Private Methods
    //
    // Invoke the correct JSP for this page
    private void dispatchJSP(WebPageDescriptor p_thePageDescriptor,
      HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
      ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_theRequest.getSession();

        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState)sessionMgr.getAttribute(
            WebAppConstants.EDITORSTATE);

        long tuId  = state.getTuId();
        long tuvId = state.getTuvId();
        long subId = state.getSubId();
        long sourceLocaleId = state.getSourceLocale().getId();
        long targetLocaleId = state.getTargetLocale().getId();

        // this file has to be deleted
        SegmentView segmentView = new SegmentView();

        sessionMgr.setAttribute(WebAppConstants.SEGMENTVIEW, segmentView);        
    }

}
