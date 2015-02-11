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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm2.indexer.Reindexer;

/**
 * <p>
 * This PageHandler is responsible for reindexing TMs.
 * </p>
 */

public class TmReindexHandler extends PageHandler implements WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmReindexHandler.class);

    //
    // Static Members
    //
    static private ProjectHandler /* TmManager */s_manager = null;

    //
    // Constructor
    //
    public TmReindexHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler()/* getTmManager() */;
            }
            catch (Exception ex)
            {
                CATEGORY.error("Initialization failed.", ex);
            }
        }
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        boolean isSuperPM = UserUtil.isSuperPM(userId);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTMAccessControl = currentCompany
                .getEnableTMAccessControl();

        sessionMgr.setAttribute("isAdmin", isAdmin);
        sessionMgr.setAttribute("isSuperAdmin", isSuperAdmin);

        String action = (String) p_request.getParameter(TM_ACTION);

        try
        {
            if (action.equals(TM_ACTION_REINDEX))
            {
                String tmid = (String) p_request.getParameter(RADIO_TM_ID);
                String tmName = "";
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }

                if (tmid != null)
                {
                    Tm tm = s_manager.getProjectTMById(Long.parseLong(tmid),
                            false);
                    tmName = tm.getName();
                    sessionMgr.setAttribute(TM_TM_ID, tmid);
                }
                sessionMgr.setAttribute(TM_TM_NAME, tmName);

            }
            else if (action.equals(TM_ACTION_REINDEX_START))
            {
                // initialize with -1 (reindex all tms)
                long tmId = -1;

                String tmChoice = (String) p_request.getParameter(TM_TM_CHOICE);
                if (tmChoice.equals("idSelectedTm"))
                {
                    String tmIdStr = (String) sessionMgr.getAttribute(TM_TM_ID);
                    tmId = Long.parseLong(tmIdStr);
                }

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Reindex TM (tm id = " + tmId + ")");
                }

                // Start reindex in a separate thread.
                Collection<ProjectTM> tms = new ArrayList<ProjectTM>();
                if (tmId == -1)
                {
                    if (enableTMAccessControl)
                    {
                        /**
                         * Enable TM Access Control for administrator, get all
                         * the TMs except(TM3 and Remote TM) for others, get the
                         * TMs he can access, except(TM3 and Remote TM)
                         * 
                         */
                        if (isAdmin || isSuperAdmin)
                        {
                            Collection<ProjectTM> allTMs = ServerProxy
                                    .getProjectHandler().getAllProjectTMs();
                            Iterator it = allTMs.iterator();
                            while (it.hasNext())
                            {
                                ProjectTM tm = (ProjectTM) it.next();
                                if (tm.getTm3Id() != null || tm.getIsRemoteTm())
                                {
                                    continue;
                                }
                                tms.add(tm);
                            }

                        }
                        else
                        {
                            ProjectTMTBUsers ptUsers = new ProjectTMTBUsers();
                            List tmIdList = ptUsers.getTList(userId, "TM");
                            Iterator it = tmIdList.iterator();
                            while (it.hasNext())
                            {
                                ProjectTM tm = null;
                                try
                                {
                                    tm = ServerProxy
                                            .getProjectHandler()
                                            .getProjectTMById(
                                                    ((BigInteger) it.next())
                                                            .longValue(),
                                                    false);
                                    if (tm.getTm3Id() != null
                                            || tm.getIsRemoteTm())
                                        continue;
                                }
                                catch (Exception e)
                                {
                                    throw new EnvoyServletException(e);
                                }
                                if (isSuperPM)
                                {
                                    if (String.valueOf(tm.getCompanyId())
                                            .equals(currentCompanyId))
                                    {
                                        tms.add(tm);
                                    }
                                }
                                else
                                {
                                    tms.add(tm);
                                }
                            }

                        }
                    }
                    else
                    {
                        // disable TM access control
                        Collection<ProjectTM> allTMs = ServerProxy
                                .getProjectHandler().getAllProjectTMs();
                        Iterator it = allTMs.iterator();
                        while (it.hasNext())
                        {
                            ProjectTM tm = (ProjectTM) it.next();
                            if (tm.getTm3Id() != null || tm.getIsRemoteTm())
                            {
                                continue;
                            }
                            tms.add(tm);
                        }
                    }
                }
                else
                {
                    tms.add(ServerProxy.getProjectHandler().getProjectTMById(
                            tmId, false));
                }
                Reindexer reindexer = LingServerProxy.getTmCoreManager()
                        .getReindexer(tms);
                reindexer.setResourceBundle(getBundle(session));
                reindexer.initReplacingMessage();
                sessionMgr.setAttribute(TM_REINDEXER, reindexer);

                reindexer.start();
            }
            else if (action.equals(TM_ACTION_CANCEL_REINDEX))
            {
                Reindexer reindexer = (Reindexer) sessionMgr
                        .getAttribute(TM_REINDEXER);
                reindexer.cancelProcess();
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Tm reindex error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
}
