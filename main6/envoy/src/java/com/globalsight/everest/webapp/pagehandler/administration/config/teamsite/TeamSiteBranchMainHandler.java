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
package com.globalsight.everest.webapp.pagehandler.administration.config.teamsite;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TeamSiteBranchComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

/**
 * TeamSiteBranchPageHandler, A page handler to produce the entry
 * page(index.jsp) for TeamSite Branch anagement.
 * <p>
 * 
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class TeamSiteBranchMainHandler extends PageHandler
{
    private static final int NEW = 1;
    private static final int REMOVE = 4;

    public static final String NEW_URL = "newURL";
    public static final String TARGET_LOCALES = "TargetLangs";
    public static final String TEAMSITE_DIRS = "TeamSiteDirs";
    public static final String TEAMSITE_SOURCE_BRANCH = "TeamSiteSourceBranch";
    public static final String TEAMSITE_TARGET_BRANCH = "TeamSiteTargetBranch";
    public static final String TEAMSITE_TARGET_LANGUAGE = "TeamSiteTargetLanguage";
    public static final String TEAMSITE_SERVER = "TeamSiteServer";
    public static final String TEAMSITE_STORE = "TeamSiteStore";
    public static final String TS_KEY = "tsProfile";
    public static final String TS_LIST = "tsProfiles";

    /**
     * Invokes this PageHandler
     * <p>
     * 
     * @param p_thePageDescriptor
     *            the page descriptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        // Get the session manager.
        HttpSession session = p_theRequest.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = p_theRequest.getParameter("action");

        // gather the user input
        String tl = (String) p_theRequest
                .getParameter(TEAMSITE_TARGET_LANGUAGE);
        String sb = (String) sessionMgr.getAttribute(TEAMSITE_SOURCE_BRANCH);
        String tb = (String) p_theRequest.getParameter(TEAMSITE_TARGET_BRANCH);
        String server = (String) sessionMgr.getAttribute(TEAMSITE_SERVER);
        String store = (String) sessionMgr.getAttribute(TEAMSITE_STORE);

        if ((tl != null) && (sb != null) && (tb != null) && (server != null)
                && (store != null))
        {
            Integer Itmp = new Integer(tl);
            Integer IServer = new Integer(server);
            Integer IStore = new Integer(store);
            TeamSiteBranch TSBUpdate = new TeamSiteBranch(sb, Itmp.intValue(),
                    tb, IServer.intValue(), IStore.intValue());
            // update the database
            try
            {
                ServerProxy.getTeamSiteDBManager().createBranch(TSBUpdate);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(
                        EnvoyServletException.EX_REMOTE, e);
            }
        }
        else if ("remove".equals(action))
        {
            try
            {
                String id = p_theRequest.getParameter("id");
                long tsbId = Long.parseLong(id);
                Collection branches = ServerProxy.getTeamSiteDBManager()
                        .getAllBranches();
                TeamSiteBranch branch = null;
                // loop thru and find the branch
                // (there really needs to be a method to fetch based on id)
                for (Iterator iter = branches.iterator(); iter.hasNext();)
                {
                    branch = (TeamSiteBranch) iter.next();
                    if (branch.getId() == tsbId)
                        break;
                }
                ServerProxy.getTeamSiteDBManager().removeBranch(branch);
            }
            catch (Exception e2)
            {
                throw new EnvoyServletException(e2);
            }
        }
        try
        {
            dataForTable(p_theRequest, session);
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

        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
                p_theResponse, p_context);
    }

    /**
     * Get list of all connections.
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();

        Collection branches = ServerProxy.getTeamSiteDBManager()
                .getAllBranches();
        if (branches == null)
        {
            branches = new ArrayList();
        }

        Iterator it = branches.iterator();
        while (it.hasNext())
        {
            TeamSiteBranch tsb = (TeamSiteBranch) it.next();
            TeamSiteServer ts = ServerProxy
                    .getTeamSiteServerPersistenceManager().readTeamSiteServer(
                            tsb.getTeamSiteServerId());

            if (!currentCompanyId.equalsIgnoreCase(String.valueOf(ts
                    .getCompanyId())))
            {
                it.remove();
            }
        }

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, new ArrayList(branches),
                new TeamSiteBranchComparator(uiLocale), 10, TS_LIST, TS_KEY);
        checkPreReqData(p_request, p_session);
    }

    /**
     * Before being able to create a Teamsite Profile, certain objects must
     * exist. Check that here.
     */
    private void checkPreReqData(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        // Let's get all the TeamSite Servers
        Collection servers = null;
        try
        {
            servers = ServerProxy.getTeamSiteServerPersistenceManager()
                    .getAllTeamSiteServers();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        if (servers == null || servers.size() == 0)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            message.append(bundle.getString("lb_teamsite_ts_server"));
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }
}
