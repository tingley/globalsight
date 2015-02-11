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

package com.globalsight.everest.webapp.pagehandler.administration.glossaries;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.glossaries.GlossaryUpload;
import com.globalsight.everest.glossaries.GlossaryException;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>MainHandler is responsible for:</p>
 * <ol>
 * <li>Displaying the list of available glossaries.</li>
 * <li>Uploading new glossaries.</li>
 * <li>Deleting and updating existing glossary files.</li>
 * </ol>
 */

public class UploadHandler
    extends PageHandler
    implements GlossaryConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            UploadHandler.class.getName());

    //
    // Private Members
    //
    private GlossaryState m_state = null;

    //
    // Constructor
    //
    public UploadHandler()
    {
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
        m_state = (GlossaryState)session.getAttribute(
            WebAppConstants.GLOSSARYSTATE);

        GlossaryUpload uploader = new GlossaryUpload();

        try
        {
            uploader.doUpload(p_request);
            m_state.setMessage("");
        }
        catch (GlossaryException ex)
        {
            m_state.setMessage(ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }
}

