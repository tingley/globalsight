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


import com.globalsight.cxe.entity.previewurl.PreviewUrl;
import com.globalsight.cxe.entity.previewurl.PreviewUrlImpl;
import com.globalsight.cxe.persistence.previewurl.PreviewUrlPersistenceManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.rmi.RemoteException;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PreviewUrlBasicHandler extends PageHandler
{
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
            setNames(p_request);
            if ("edit".equals(action))
            {
                // Fetch the PreviewUrl to edit and store in session
                SessionManager sessionMgr = (SessionManager)
                    session.getAttribute(WebAppConstants.SESSION_MANAGER);
                String id = p_request.getParameter("id");
                PreviewUrl preview = ServerProxy.getPreviewUrlPersistenceManager().
                    getPreviewUrl(Long.parseLong(id));
                sessionMgr.setAttribute(PreviewUrlMainHandler.PREVIEW_KEY, preview);

                sessionMgr.setAttribute("edit", "true");
            }
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
     * Get list of all PreviewUrl names.  Needed in jsp to determine duplicate names.
     */
    private void setNames(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        ArrayList list = (ArrayList)
            ServerProxy.getPreviewUrlPersistenceManager().getAllPreviewUrls();
        ArrayList names = new ArrayList();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                PreviewUrlImpl preview = (PreviewUrlImpl)list.get(i);
                names.add(preview.getName());
            }
        }
        p_request.setAttribute(PreviewUrlMainHandler.NAMES, names);
    }
}


