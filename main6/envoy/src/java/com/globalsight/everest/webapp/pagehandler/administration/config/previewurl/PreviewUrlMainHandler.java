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
package com.globalsight.everest.webapp.pagehandler.administration.config.previewurl;

import com.globalsight.cxe.entity.previewurl.PreviewUrlImpl;
import com.globalsight.cxe.persistence.previewurl.PreviewUrlPersistenceManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.PreviewUrlComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * PreviewUrlPageHandler, A page handler to produce the entry page(index.jsp) for PreviewUrl management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class PreviewUrlMainHandler extends PageHandler
{
    public static final String PREVIEW_KEY = "previewUrl";
    public static final String PREVIEW_LIST = "previewUrls";
    public static final String NAMES = "previewNames";
    

    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if ("cancel".equals(action))
            {
                clearSessionExceptTableInfo(session, PREVIEW_KEY);
            }
            else if ("new".equals(action))
            {
                createPreviewUrl(p_request, session);
                clearSessionExceptTableInfo(session, PREVIEW_KEY);
            }
            else if ("edit".equals(action))
            {
                updatePreviewUrl(p_request, session);
                clearSessionExceptTableInfo(session, PREVIEW_KEY);
            }
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    /**
     * Update a Preview Url.
     */
    private void updatePreviewUrl(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager)
            p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        PreviewUrlImpl preview = (PreviewUrlImpl) sessionMgr.getAttribute(PREVIEW_KEY);
        getParams(p_request, preview);
        ServerProxy.getPreviewUrlPersistenceManager().updatePreviewUrl(preview);
    }

    /**
     * Create a Preview Url.
     */
    private void createPreviewUrl(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        PreviewUrlImpl preview = new PreviewUrlImpl();
        getParams(p_request, preview);
        ServerProxy.getPreviewUrlPersistenceManager().createPreviewUrl(preview);
    }

    /**
     * Get request params and update PreviewUrl.
     */
    private void getParams(HttpServletRequest p_request, PreviewUrlImpl p_preview)
    {
        p_preview.setName(p_request.getParameter("nameField"));
        p_preview.setDescription(p_request.getParameter("descField"));
        p_preview.setContent(p_request.getParameter("ruleField"));
    }

    /**
     * Get list of all db import profiles.
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Collection previewUrls =
             ServerProxy.getPreviewUrlPersistenceManager().getAllPreviewUrls();
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        SessionManager sessionMgr = (SessionManager)
            p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        setTableNavigation(p_request, p_session, new ArrayList(previewUrls),
                       new PreviewUrlComparator(uiLocale),
                       10,
                       PREVIEW_LIST, PREVIEW_KEY);
    }

}

