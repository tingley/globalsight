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
package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class ModuleMappingNewHandler extends PageHandler implements
        ModuleMappingConstants
{
    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    private static final Logger logger = Logger
            .getLogger(ModuleMappingNewHandler.class.getName());

    private CVSServerManagerLocal manager = new CVSServerManagerLocal();

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        try
        {
            Vector sourceLocales = ServerProxy.getLocaleManager()
                    .getAllSourceLocales();
            Vector targetLocales = new Vector();

            SortUtil.sort(sourceLocales, new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                            .compareToIgnoreCase(
                                    ((GlobalSightLocale) o2)
                                            .getDisplayName(Locale.US));
                }
            });

            String cvsServer = p_request.getParameter("cvsServer");
            cvsServer = cvsServer == null ? "" : cvsServer;
            String sourceModule = p_request.getParameter("sourceModule");
            sourceModule = sourceModule == null ? "" : sourceModule;
            String sourceLocale = p_request.getParameter("sourceLocale");
            long sourceLocaleId = -1L;
            if (sourceLocale != null)
                sourceLocaleId = Long.parseLong(sourceLocale);

            if (action.equals("Change"))
            {
                if (sourceLocaleId > 0)
                {
                    targetLocales = ServerProxy.getLocaleManager()
                            .getTargetLocales(
                                    ServerProxy.getLocaleManager()
                                            .getLocaleById(sourceLocaleId));
                    SortUtil.sort(targetLocales, new Comparator()
                    {
                        public int compare(Object o1, Object o2)
                        {
                            return ((GlobalSightLocale) o1).getDisplayName(
                                    Locale.US).compareToIgnoreCase(
                                    ((GlobalSightLocale) o2)
                                            .getDisplayName(Locale.US));
                        }
                    });
                }
            }

            p_request.setAttribute(SOURCE_LOCALE_PAIRS, sourceLocales);
            p_request.setAttribute(TARGET_LOCALE_PAIRS, targetLocales);
            p_request.setAttribute("cvsServer", cvsServer);
            p_request.setAttribute("sourceLocale", sourceLocale);
            p_request.setAttribute("sourceModule", sourceModule);

            ArrayList servers = (ArrayList) manager.getAllServer();
            p_request.setAttribute(CVSConfigConstants.CVS_SERVER_LIST, servers);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
}
