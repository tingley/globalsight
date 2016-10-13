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
package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;

import com.globalsight.everest.cvsconfig.CVSModule;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class CVSModuleCheckoutBasicHandler extends PageHandler
{
    private CVSServerManagerLocal manager = new CVSServerManagerLocal();

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context)
            throws ServletException, IOException, EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession(false);
            String action = p_request.getParameter("action");
            SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
            String id = (String) p_request.getParameter("id");
            if (CVSConfigConstants.CHECKOUT.equals(action))
            {
                CVSModule module = manager.getModule(Long.parseLong(id));
                module.setLastCheckout(new java.util.Date().toString());
                manager.updateModule(module);
                sessionMgr.setAttribute(CVSConfigConstants.CVS_MODULE_KEY, module);
            }

        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        catch (HibernateException e)
        {
            throw new EnvoyServletException(e);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

}
