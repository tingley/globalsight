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

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * This class handles the Basic Info screen for TeamSite Servers
 */

public class BasicTeamSiteServerHandler extends PageHandler
        implements TeamSiteServerConstants
{

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public BasicTeamSiteServerHandler()
    {
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Override Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        String action = (String)p_request.getParameter(
            TeamSiteServerConstants.ACTION);

        clearSessionManager(session);
        setInfoInSession(p_request);


        if(action.equals(TeamSiteServerConstants.EDIT_ACTION))
        {
            sessionMgr.setAttribute(TeamSiteServerConstants.MODIFY_ACTION, 
                                    TeamSiteServerConstants.MODIFY);
        }

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    ////////////////////////////////////////////////////////////////////// 
    /**
     * Clear the session manager
     * 
     * @param p_session - The client's HttpSession where the 
     * session manager is stored.
     */
    private void clearSessionManager(HttpSession p_session)
    {
        SessionManager sessionMgr = 
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(CONTENT_STORES, null);
        sessionMgr.setAttribute(BRANCH_STORES, null);
        sessionMgr.setAttribute(OLD_STORES, null);
        sessionMgr.setAttribute(TEAMSITE_SERVER, null);
        sessionMgr.setAttribute(MODIFY_ACTION, null);
        sessionMgr.setAttribute(CREATE_ACTION, null);
        sessionMgr.setAttribute(SERVER_ID, null);
        sessionMgr.clear();
    }

    /**
     * Set necessary information in the session. 
     * 
     * @param p_request
     * @exception EnvoyServletException
     */
    private void setInfoInSession(HttpServletRequest p_request)
    throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = 
            (Locale)session.getAttribute(WebAppConstants.UILOCALE);

        Vector operatingSystems = new Vector();
        operatingSystems.add(TeamSiteServerConstants.UNIX);
        operatingSystems.add(TeamSiteServerConstants.WINDOWS);
 
        Vector userTypes = new Vector();
        userTypes.add(TeamSiteServerConstants.MASTER);
        userTypes.add(TeamSiteServerConstants.ADMINISTRATOR);

        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);

        // now set the values in the request
        sessionMgr.setAttribute(OPERATING_SYSTEMS, operatingSystems);
        sessionMgr.setAttribute(USER_TYPES, userTypes);
        
        // get the template id first (for edit action)
        String id = p_request.getParameter(SERVER_ID);
        if (id != null)
        {
            addTeamSiteServerToSession(p_request, id);            
        }
    }

    /**
     * This method is used during modify teamsite server.
     * In this case, all required info should be stored in 
     * session manager.  If values are modified, they'll also be
     * updated in session manager.
     * 
     * @param p_request
     * @param p_tsId
     * @exception EnvoyServletException
     */
    private void addTeamSiteServerToSession(HttpServletRequest p_request, String p_tsId)
        throws EnvoyServletException
    {
        long tsId = -1;
        try
        {
            tsId = Long.parseLong(p_tsId);
            HttpSession session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager)session
                .getAttribute(SESSION_MANAGER);

            TeamSiteServerImpl storedTssi = (TeamSiteServerImpl)sessionMgr
                .getAttribute(TEAMSITE_SERVER);

            // get teamsite server based on the id
            TeamSiteServerImpl tssi = storedTssi == null ?
                (TeamSiteServerImpl)ServerProxy.getTeamSiteServerPersistenceManager()
                                     .readTeamSiteServer(tsId):
                storedTssi;

            // pass the id to the request (to be used as part of the next url
            sessionMgr.setAttribute(SERVER_ID, new Long(tssi.getId()));

            //TEAMSITE_SERVER
            sessionMgr.setAttribute(TEAMSITE_SERVER, tssi);

            // Let's get the Content store ids and put them
            // onto session for later use
            Vector stores = new Vector(ServerProxy
                .getTeamSiteServerPersistenceManager()
                .getBackingStoresByTeamSiteServer(tssi));
            sessionMgr.setAttribute(OLD_STORES, stores);


            //Store the information about stores
            // that cannot be removed
            Vector branchStores = new Vector();
            Vector branches = null;
            GlobalSightLocale target_locale = null;
            branches = vectorizedCollection(ServerProxy.getTeamSiteDBManager().getAllBranches());
            int rowSize = branches.size();
            for (int i = 0 ; i < rowSize ; i++)
            {
                TeamSiteBranch branch = (TeamSiteBranch)branches.elementAt(i);
                if(tsId == branch.getTeamSiteServerId())
                {
                    TeamSiteServer ts = ServerProxy.getTeamSiteServerPersistenceManager()
                                        .readTeamSiteServer(branch.getTeamSiteServerId());
                    BackingStore store = ServerProxy.getTeamSiteServerPersistenceManager()
                                        .readBackingStore(branch.getTeamSiteStoreId());
                    branchStores.addElement((String)store.getName());
                }
            }
            sessionMgr.setAttribute(BRANCH_STORES, branchStores);

            // Action type (edit)
            String actionType = (String)p_request.getParameter(ACTION);
            sessionMgr.setAttribute(ACTION, actionType);
        }
        catch (NumberFormatException nfe)
        {
        }
        catch (Exception e)
        {
        }
    }    
}
