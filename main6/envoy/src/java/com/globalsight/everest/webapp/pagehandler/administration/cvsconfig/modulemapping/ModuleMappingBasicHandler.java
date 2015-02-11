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
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMapping;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingManagerLocal;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingRename;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingRenameComparator;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class ModuleMappingBasicHandler extends PageHandler implements
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
    private ModuleMappingManagerLocal manager = new ModuleMappingManagerLocal();

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
            if (action.equals(UPDATE))
            {
                String id = (String) p_request.getParameter("id");
                ModuleMapping moduleMapping = (ModuleMapping) manager
                        .getModuleMapping(Long.parseLong(id));
                sessionMgr.setAttribute(MODULE_MAPPING_KEY, moduleMapping);
                p_request.setAttribute("edit", "true");
                setCVSServers(session);
                dataForTable(p_request, session, moduleMapping);
            }
            else if (action.equals(REMOVE_RENAME))
            {
                // Remove an exist target file rename
                String id = (String) p_request.getParameter("id");
                ModuleMappingRename mmr = manager.getModuleMappingRename(Long
                        .parseLong(id));
                long mmid = mmr.getModuleMapping().getId();
                manager.removeRename(mmr);
                ModuleMapping mm = manager.getModuleMapping(mmid);
                dataForTable(p_request, session, mm);
                p_request.setAttribute("edit", "true");
            }
            else if (action.equals(UPDATE_RENAME))
            {
                // Save module mapping rename
                ModuleMapping moduleMapping = (ModuleMapping) sessionMgr
                        .getAttribute(MODULE_MAPPING_KEY);
                ModuleMappingRename mmr = (ModuleMappingRename) sessionMgr
                        .getAttribute(MODULE_MAPPING_RENAME_KEY);
                if (mmr == null)
                {
                    mmr = new ModuleMappingRename();
                    mmr.setModuleMapping(moduleMapping);
                    mmr.setSourceName((String) p_request
                            .getParameter(ModuleMappingConstants.SOURCE_NAME));
                    mmr.setTargetName((String) p_request
                            .getParameter(ModuleMappingConstants.TARGET_NAME));
                    manager.addModuleMappingRename(mmr);
                }
                else
                {
                    mmr.setSourceName((String) p_request
                            .getParameter(ModuleMappingConstants.SOURCE_NAME));
                    mmr.setTargetName((String) p_request
                            .getParameter(ModuleMappingConstants.TARGET_NAME));
                    manager.updateModuleMappingRename(mmr);
                }
                p_request.setAttribute("edit", "true");
                dataForTable(p_request, session,
                        manager.getModuleMapping(moduleMapping.getId()));
            }
            setModuleMapping(p_request);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
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

    /**
     * Get list of all company names. Needed in jsp to determine duplicate
     * names.
     */
    private void setModuleMapping(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException
    {
        Vector sourceLocales = ServerProxy.getLocaleManager()
                .getAllSourceLocales();
        Vector targetLocales = ServerProxy.getLocaleManager()
                .getAllTargetLocales();
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
        SortUtil.sort(targetLocales, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                        .compareToIgnoreCase(
                                ((GlobalSightLocale) o2)
                                        .getDisplayName(Locale.US));
            }
        });

        p_request.setAttribute(SOURCE_LOCALE_PAIRS, sourceLocales);
        p_request.setAttribute(TARGET_LOCALE_PAIRS, targetLocales);
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, ModuleMapping p_mm) throws RemoteException,
            NamingException, GeneralException
    {
        Set renames = p_mm.getFileRenames();
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute(
                ModuleMappingConstants.MODULE_MAPPING_RENAME_LIST, renames);
        Vector servers = vectorizedCollection(renames);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, servers,
                new ModuleMappingRenameComparator(uiLocale), 10,
                ModuleMappingConstants.MODULE_MAPPING_RENAME_LIST,
                ModuleMappingConstants.MODULE_MAPPING_RENAME_KEY);
    }

    private void setCVSServers(HttpSession p_session)
    {
        CVSServerManagerLocal cvsManager = new CVSServerManagerLocal();
        ArrayList servers = (ArrayList) cvsManager.getAllServer();
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute(CVSConfigConstants.CVS_SERVER_LIST, servers);
    }

}
