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

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStoreImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.cxe.persistence.cms.teamsite.server.TeamSiteServerPersistenceManager;
import com.globalsight.cxe.persistence.cms.teamsite.server.TeamSiteServerPersistenceManagerLocal;
import com.globalsight.cxe.persistence.cms.teamsite.server.TeamSiteServerPersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.cms.teamsite.server.TeamSiteServerEntityException;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.edit.EditUtil;

import java.io.IOException;
import java.util.Vector;
import java.math.BigDecimal;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>Handler is responsible for:</p>
 * <ol>
 * <li>Displaying the list of available content stores.</li>
 * <li>Deleting and updating content stores.</li>
 * </ol>
 */

public class ContentStoreHandler
    extends PageHandler
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            ContentStoreHandler.class.getName());

    public ContentStoreHandler()
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
        SessionManager sessionMgr= (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        Vector stores = (Vector)sessionMgr.getAttribute(TeamSiteServerConstants.CONTENT_STORES);
        String action = (String)p_request.getParameter(
            TeamSiteServerConstants.ACTION);
        Vector removedStores = new Vector();

        if (action == null)
        {
            setBasicInfoIntoSession(p_request);
            String modAction = (String)sessionMgr.getAttribute(TeamSiteServerConstants.MODIFY_ACTION);
            if(modAction != null && modAction.equals(TeamSiteServerConstants.MODIFY))
            {
                // get the store info and set it into session.
                // get the modified TeamsiteServer info and set the 
                // object into session.

                sessionMgr.setAttribute(TeamSiteServerConstants.MODIFY_ACTION, 
                                        TeamSiteServerConstants.MODIFY);
                try
                {
                    Long serverId = (Long)sessionMgr.getAttribute(TeamSiteServerConstants.SERVER_ID);
                    TeamSiteServerImpl tssi = (TeamSiteServerImpl)ServerProxy
                                             .getTeamSiteServerPersistenceManager()
                                             .readTeamSiteServer(serverId.longValue());
                    stores = new Vector();
                    for (int i=0; i < tssi.getBackingStoreIds().size(); i++)
                    {
                            Long l = (Long) tssi.getBackingStoreIds().get(i);
                            BigDecimal bd = new BigDecimal(l.doubleValue());
                            BackingStore bs = (BackingStore)ServerProxy
                                .getTeamSiteServerPersistenceManager()
                                .readBackingStore(l.longValue());
                            stores.addElement((String)bs.getName());
                    }
                }
                catch (Exception e)
                {
                        e.printStackTrace(System.out);
                }
            }
        }
        else
        {
            // This is reached if a new store is removed
            if(action.equals(TeamSiteServerConstants.REMOVE_ACTION))
            {
                if(stores!=null)
                {
                    Vector newStores = new Vector();
                    String[] checkBox = (String[])p_request.getParameterValues(
                        TeamSiteServerConstants.STORE_CHECKBOXES);
                    for(int z=0; z<stores.size(); z++)
                    {
                        boolean found = false;
                        for(int i=0; i<checkBox.length;i++)
                        {
                            if(parseInt(checkBox[i]) == z)
                            {
                                removedStores.addElement(stores.elementAt(z));
                                found = true;
                                break;
                            }
                        }
                        if(!found)
                        {
                            newStores.addElement(stores.elementAt(z));
                        }
                    }
                    stores = newStores;
                }
            }
            // This is reached if a new store is added
            else if(action.equals(TeamSiteServerConstants.ADD_ACTION))
            {
                String storeName = (String)p_request.getParameter(
                    TeamSiteServerConstants.STORE_NAME);
                //addAnotherContentStore();
                if(stores!=null)
                {
                    stores.addElement(storeName);
                }
                else
                {
                    stores = new Vector();
                    stores.addElement(storeName);
                }
            }
        }
        sessionMgr.setAttribute(TeamSiteServerConstants.REMOVED_STORES, removedStores);
        sessionMgr.setAttribute(TeamSiteServerConstants.CONTENT_STORES, stores);

        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }
    private void setBasicInfoIntoSession(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr= 
            (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        try
        {
            // We are here from Basic info. 
            String serverName = (String)p_request.getParameter(TeamSiteServerConstants.NAME_FIELD);
            String description = (String)p_request.getParameter(TeamSiteServerConstants.DESCRIPTION_FIELD);
            String os = (String)p_request.getParameter(TeamSiteServerConstants.OS_FIELD);
            String sExportPort = (String)p_request.getParameter(TeamSiteServerConstants.EXPORT_FIELD);
            String sImportPort = (String)p_request.getParameter(TeamSiteServerConstants.IMPORT_FIELD);
            String sProxyPort = (String)p_request.getParameter(TeamSiteServerConstants.PROXY_FIELD);
            String home = (String)p_request.getParameter(TeamSiteServerConstants.HOME_FIELD);
            String user = (String)p_request.getParameter(TeamSiteServerConstants.USER_FIELD);
            String userPass = (String)p_request.getParameter(TeamSiteServerConstants.USER_PASS_FIELD);
            String type = (String)p_request.getParameter(TeamSiteServerConstants.TYPE_FIELD);
            String mount = (String)p_request.getParameter(TeamSiteServerConstants.MOUNT_FIELD);
            boolean reimport = Boolean.valueOf(
                p_request.getParameter(TeamSiteServerConstants.REIMPORT_FIELD)).booleanValue(); 

            // Find out the actual integer values
            int exportPort = parseInt(sExportPort);
            int importPort = parseInt(sImportPort);
            int proxyPort = parseInt(sProxyPort);

            TeamSiteServer teamsiteServer = null;
            teamsiteServer = (TeamSiteServer)sessionMgr.getAttribute(TeamSiteServerConstants.TEAMSITE_SERVER);
            if(teamsiteServer == null)
            {
                teamsiteServer = new TeamSiteServerImpl();
            }
            teamsiteServer.setName(serverName);
            teamsiteServer.setDescription(description);
            teamsiteServer.setImportPort(importPort);
            teamsiteServer.setExportPort(exportPort);
            teamsiteServer.setProxyPort(proxyPort);
            teamsiteServer.setHome(home);
            teamsiteServer.setUser(user);
            teamsiteServer.setUserPass(userPass);
            teamsiteServer.setMount(mount);
            teamsiteServer.setOS(os);
            teamsiteServer.setType(type);
            teamsiteServer.setLocaleSpecificReimportSetting(reimport);
            sessionMgr.setAttribute(TeamSiteServerConstants.TEAMSITE_SERVER, teamsiteServer);
        }
        catch (Exception e)
        {
                e.printStackTrace(System.out);
        }
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
}


