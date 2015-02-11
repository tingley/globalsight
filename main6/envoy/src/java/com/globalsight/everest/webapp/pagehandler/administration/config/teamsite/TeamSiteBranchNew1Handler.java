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

// Envoy packages
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStoreImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.cxe.util.cms.teamsite.TeamSiteExchange;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * TeamSiteBranchNew1Handler, A page handler to launch the new teamsite page.
 * <p>
 * @version 1.0
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */

public class TeamSiteBranchNew1Handler extends PageHandler
{

    public TeamSiteBranchNew1Handler()
    {
    }

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
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        dispatchJSP(p_thePageDescriptor, p_theRequest, p_theResponse, p_context);
        super.invokePageHandler(p_thePageDescriptor, p_theRequest, p_theResponse, p_context);
    }

    /**
    * Invoke the correct JSP for this page
    */
    protected void dispatchJSP(WebPageDescriptor p_thePageDescriptor,
                HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
                ServletContext p_context) throws ServletException, IOException, EnvoyServletException
    {
        // use dummy link, real link will be determined after the user navigates out of the page
        NavigationBean aNavigationBean = new NavigationBean(WebAppConstants.DUMMY_LINK, p_thePageDescriptor.getPageName());
        p_theRequest.setAttribute(WebAppConstants.DUMMY_NAVIGATION_BEAN_NAME, aNavigationBean);


        String action = (String)p_theRequest.getParameter(TeamSiteServerConstants.ACTION);
        String sServerId = (String)p_theRequest.getParameter(TeamSiteServerConstants.SERVERS);
        String sStoreId = (String)p_theRequest.getParameter(TeamSiteServerConstants.STORES);

        long serverId = parseLong(sServerId);
        long storeId = parseLong(sStoreId);
        TeamSiteServer server = null;
        BackingStore store = null;
        try
        {        
           server = (TeamSiteServer)ServerProxy.getTeamSiteServerPersistenceManager()
                                .readTeamSiteServer(serverId);
           store = (BackingStore)ServerProxy.getTeamSiteServerPersistenceManager()
                                .readBackingStore(storeId);
        }
        catch(Exception e)
        {
           throw new EnvoyServletException(e);
        }

        // obtain the directory structure from the TeamSite server
        //String dirents = "main/DemoBranch/subBranch0/subBranch1/IWdummy-_-_-main/DemoBranch/subBranch0/subBranch2/IWdummy-_-_-main/Metro/subBranch0/IWdummy-_-_-";

        //String startingPath = TeamSiteExchange.defaultStartingPath();
        String startingPath = server.getMount() + "/" + store.getName();

        // choose a separator that will not be in a directory name
        String separator = "-_-_-";

        // construct the arguments for the TeamSite Server CGI script
        String ref_tlign = ".raw";
        String ref_ign = "STAGING"+separator+"EDITION"+separator+"WORKAREA";
        String ignore_dirs = "1";

        String dirents = TeamSiteExchange.retrieveDirs(startingPath, ref_tlign, ref_ign, ignore_dirs, separator, server);
        if(dirents == null)
        {
            dirents = "Error";
        }

        // notify session of TeamSite directories
        HttpSession session = p_theRequest.getSession();
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(TeamSiteBranchMainHandler.TEAMSITE_DIRS, dirents);        
        sessionMgr.setAttribute(TeamSiteBranchMainHandler.TEAMSITE_SERVER, sServerId);        
        sessionMgr.setAttribute(TeamSiteBranchMainHandler.TEAMSITE_STORE, sStoreId);        
    }
    /* Convert the given string into an integer value; if null, or an error */
    /* occurs, return the default value instead (always 0) */
    private int parseInt(String p_string)
    {
        int intVal = 0;
        if (p_string != null)
        {
            try
            {
                intVal  = Integer.parseInt(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return intVal;
    }

    /* Convert the given string into an integer value; if null, or an error */
    /* occurs, return the default value instead (always 0) */
    private long parseLong(String p_string)
    {
        long longVal = 0;
        if (p_string != null)
        {
            try
            {
                longVal  = Long.parseLong(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return longVal;
    }


}
