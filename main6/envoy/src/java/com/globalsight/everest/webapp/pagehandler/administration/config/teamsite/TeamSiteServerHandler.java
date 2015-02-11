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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStoreImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TeamSiteServerComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * TeamSiteServerHandler is the page handler responsible for displaying a list
 * of workflow templates and perform actions supported by the UI (JSP).
 */

public class TeamSiteServerHandler extends PageHandler implements
        TeamSiteServerConstants
{

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // //////////////////////////////////////////////////////////////////
    public TeamSiteServerHandler()
    {
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
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
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        if (REMOVE_ACTION.equals(p_request.getParameter(ACTION)))
        {
            long sId = (new Long(p_request.getParameter(SERVER_ID)))
                    .longValue();
            removeTeamSiteServer(sId);
        }
        else if (CREATE_ACTION.equals(p_request.getParameter(ACTION)))
        {
            long sId = (new Long(p_request.getParameter(SERVER_ID)))
                    .longValue();
            createCgiScripts(sId);
        }
        else if (CANCEL_ACTION.equals(p_request.getParameter(ACTION)))
        {
            // clean session manager
            clearSessionManager(session);
        }
        else if (SAVE_ACTION.equals(p_request.getParameter(ACTION)))
        {
            if (MODIFY.equals(sessionMgr.getAttribute(MODIFY_ACTION)))
            {
                updateTeamSiteServer(p_request, session);
            }
            else
            {
                saveTeamSiteServer(p_request, session);
            }
        }

        selectServersForDisplay(p_request, session);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Clear the session manager
     * 
     * @param p_session
     *            - The client's HttpSession where the session manager is
     *            stored.
     */
    private void clearSessionManager(HttpSession p_session)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(CONTENT_STORES, null);
        sessionMgr.setAttribute(OLD_STORES, null);
        sessionMgr.setAttribute(BRANCH_STORES, null);
        sessionMgr.setAttribute(TEAMSITE_SERVER, null);
        sessionMgr.setAttribute(MODIFY_ACTION, null);
        sessionMgr.setAttribute(CREATE_ACTION, null);
        sessionMgr.setAttribute(SERVER_ID, null);
        sessionMgr.clear();
    }

    private void selectServersForDisplay(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        List servers = null;
        try
        {
            servers = new ArrayList(ServerProxy
                    .getTeamSiteServerPersistenceManager()
                    .getAllTeamSiteServers());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        TeamSiteServerComparator comp = new TeamSiteServerComparator(uiLocale);
        // p_request.setAttribute(SERVERS,
        // sortedVectorizedCollection(servers, comp));
        int numOfServersPerPage = 10;
        try
        {
            numOfServersPerPage = SystemConfiguration.getInstance()
                    .getIntParameter(SystemConfigParamNames.NUM_WFT_PER_PAGE);
        }
        catch (Exception e)
        {
            numOfServersPerPage = 10;
        }

        setTableNavigation(p_request, p_session, servers, comp,
                numOfServersPerPage, TeamSiteServerConstants.NUM_PER_PAGE,
                TeamSiteServerConstants.NUM_OF_PAGES,
                TeamSiteServerConstants.SERVERS,
                TeamSiteServerConstants.SORTING,
                TeamSiteServerConstants.REVERSE_SORT, PAGE_NUM,
                TeamSiteServerConstants.LAST_PAGE_NUM,
                TeamSiteServerConstants.TOTAL_SERVERS);

    }

    // Create CGI scripts for the selected TeamSite server
    static void createCgiScripts(long p_serverId) throws EnvoyServletException
    {
        try
        {
            TeamSiteServer ts = ServerProxy
                    .getTeamSiteServerPersistenceManager().readTeamSiteServer(
                            p_serverId);
            createScripts(ts);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private static String concatPath(String parent, String child)
    {
        File path = new File(parent, child);
        return path.getPath();
    }

    static void createScripts(TeamSiteServer p_ts) throws GeneralException,
            EnvoyServletException
    {
        Hashtable tsHash = new Hashtable();
        String serverName = null;
        String port = null;
        String nonSSLPort = null;
        String sslPort = null;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            // get the server name
            String capLoginURL = sc
                    .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);
            sslPort = sc.getStringParameter(SystemConfigParamNames.SSL_PORT);
            nonSSLPort = sc
                    .getStringParameter(SystemConfigParamNames.NON_SSL_PORT);

            URL url = new URL(capLoginURL);
            serverName = url.getHost();

            // boolean useSSL = sc
            // .getBooleanParameter(SystemConfigParamNames.USE_SSL);
            String installDir = sc
                    .getStringParameter(SystemConfigParamNames.INSTALLATION_DATA_DIRECTORY);
            String destDir = "teamsite/" + p_ts.getName() + "/";
            // compose the TranslateServlet URL
            StringBuffer translateServletUrl = new StringBuffer();
            port = sc.getStringParameter(SystemConfigParamNames.NON_SSL_PORT);
            translateServletUrl.append("http://").append(serverName)
                    .append(":");
            translateServletUrl.append(nonSSLPort);
            // }
            translateServletUrl.append("/globalsight/TranslateServlet?");
            tsHash.put(TRANSLATE_URL, translateServletUrl.toString());
            tsHash.put(TEAMSITE_HOME, p_ts.getHome());
            tsHash.put(TEAMSITE_MASTER, p_ts.getUser());
            tsHash.put(TEAMSITE_SERVER_NAME, p_ts.getName());
            tsHash.put(SERVER_HOST, serverName);
            tsHash.put(SERVER_PORT, port);
            tsHash.put(TEAMSITE_MOUNT_DIR, p_ts.getMount());
            Vector store = new Vector(ServerProxy
                    .getTeamSiteServerPersistenceManager()
                    .getBackingStoresByTeamSiteServer(p_ts));
            for (int z = 0; z < store.size(); z++)
            {
                BackingStore bs = (BackingStore) store.elementAt(z);
                String storeName = bs.getName();
                tsHash.put(TEAMSITE_STORE_DIR, storeName);
                processFile(
                        concatPath(installDir,
                                "teamsite/tsautoimport/properties/teamsiteParams.properties.template"),
                        concatPath(installDir, destDir
                                + "tsautoimport/properties/" + storeName
                                + "/teamsiteParams.properties"), tsHash);
            }

            // Process the files that will reside on the TeamSite NT server
            if (p_ts.getOS().equals(WINDOWS))
            {
                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightGlobalization.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightGlobalization.ipl"), tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightImport.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightImport.ipl"), tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightImportStatusReporter.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightImportStatusReporter.ipl"),
                        tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightImportTimerService.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightImportTimerService.ipl"), tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightTranslation.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightTranslation.ipl"), tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightDcrGen.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightDcrGen.ipl"), tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightExternalTask.tmpl"),
                        concatPath(
                                installDir,
                                destDir
                                        + "tsautoimport/bin/GlobalSightExternalTask.ipl"),
                        tsHash);
            }

            if (p_ts.getOS().equals(UNIX))
            {
                // Process the files that will reside on the TeamSite SOLARIS
                // server
                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightGlobalization.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightGlobalization.cgi"), tsHash);
                // The GlobalSightImport.cgi is created from cgiwrap.c
                // which calls GlobalSightImport2.cgi
                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightImport2.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightImport2.cgi"), tsHash);
                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightImportStatusReporter.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightImportStatusReporter.cgi"),
                        tsHash);
                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightTranslation.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightTranslation.cgi"), tsHash);
                processFile(concatPath(installDir, "teamsite/Makefile.tmpl"),
                        concatPath(installDir, destDir + "Makefile"), tsHash);
                // The GlobalSightDcrGen.cgi is created from cgiwrap.c
                // which calls GlobalSightDcrGen2.cgi
                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightDcrGen2.tmpl"),
                        concatPath(installDir, destDir
                                + "GlobalSightDcrGen2.cgi"), tsHash);

                processFile(
                        concatPath(installDir,
                                "teamsite/GlobalSightExternalTask.tmpl"),
                        concatPath(
                                installDir,
                                destDir
                                        + "tsautoimport/bin/GlobalSightExternalTask.cgi"),
                        tsHash);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private static void processFile(String sourceFileStr, String destFileStr,
            Hashtable p_tsHash) throws IOException
    {
        File sourceFile = new File(sourceFileStr);
        File destFile = new File(destFileStr);

        destFile.getParentFile().mkdirs();
        destFile.createNewFile();

        try
        {
            BufferedReader in = new BufferedReader(new FileReader(sourceFile));
            BufferedWriter out = new BufferedWriter(new FileWriter(destFile));

            String str, newstr;

            while ((str = in.readLine()) != null)
            {
                if (str.startsWith("#")) // It's a comment
                {
                    newstr = str;
                }
                else
                {
                    newstr = str;

                    // Iterate over the array to see if the string matches
                    // *any* of the install keys
                    for (Enumeration e = p_tsHash.keys(); e.hasMoreElements();)
                    {
                        String key = (String) e.nextElement();
                        String pattern = "%%" + key + "%%";
                        Object replaceObj = p_tsHash.get(key);
                        String replace = replaceObj.toString();

                        if (str.indexOf(pattern) == -1) // no match
                        {
                            continue;
                        }

                        newstr = replace(str, pattern, replace);
                        str = newstr;
                    }
                }
                out.write(newstr);
                out.newLine();
            }
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    // Replacing Substrings in a String
    private static String replace(String str, String pattern, String replace)
    {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0)
        {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    // save the teamsite server
    static void saveTeamSiteServer(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        TeamSiteServer teamsiteServer = (TeamSiteServer) sessionMgr
                .getAttribute(TEAMSITE_SERVER);
        try
        {
            teamsiteServer.setCompanyId(Long.parseLong(CompanyThreadLocal
                    .getInstance().getValue()));

            Vector stores = (Vector) sessionMgr.getAttribute(CONTENT_STORES);
            Vector storeIds = new Vector();
            for (int i = 0; i < stores.size(); i++)
            {
                BackingStoreImpl backingStore = new BackingStoreImpl();
                backingStore.setName((String) stores.elementAt(i));
                backingStore = (BackingStoreImpl) ServerProxy
                        .getTeamSiteServerPersistenceManager()
                        .createBackingStore(backingStore);
                storeIds.add(new BigDecimal(backingStore.getId()));
            }
            teamsiteServer.setBackingStoreIds(storeIds);
            ServerProxy.getTeamSiteServerPersistenceManager()
                    .createTeamSiteServer(teamsiteServer);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

    // update the teamsite server
    static void updateTeamSiteServer(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession();
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            TeamSiteServer tss = (TeamSiteServer) sessionMgr
                    .getAttribute(TEAMSITE_SERVER);

            Vector oldStores = (Vector) sessionMgr
                    .getAttribute(TeamSiteServerConstants.OLD_STORES);
            Vector removedStores = (Vector) sessionMgr
                    .getAttribute(TeamSiteServerConstants.REMOVED_STORES);
            Map storeMap = new HashMap();
            for (int d = 0; d < oldStores.size(); d++)
            {
                BackingStore bs = (BackingStore) oldStores.elementAt(d);
                storeMap.put(bs.getName(), bs);
            }

            Vector stores = (Vector) sessionMgr.getAttribute(CONTENT_STORES);
            Vector storeIds = new Vector();
            for (int i = 0; i < stores.size(); i++)
            {
                boolean found = false;
                BackingStore bs = null;
                // find out if the store already exists
                for (int x = 0; x < oldStores.size(); x++)
                {
                    bs = (BackingStore) oldStores.elementAt(x);
                    if (bs.getName().equals((String) stores.elementAt(i)))
                    {
                        // Store exists
                        found = true;
                        break;
                    }
                }
                if (found)
                {
                    // Backing store already exists.
                    storeIds.add(new BigDecimal(bs.getId()));
                }
                else
                {
                    BackingStoreImpl backingStore = new BackingStoreImpl();
                    backingStore.setName((String) stores.elementAt(i));
                    backingStore = (BackingStoreImpl) ServerProxy
                            .getTeamSiteServerPersistenceManager()
                            .createBackingStore(backingStore);
                    storeIds.add(new BigDecimal(backingStore.getId()));
                }
            }
            tss.setBackingStoreIds(storeIds);
            // remove the stores from backing stores
            for (int d = 0; d < removedStores.size(); d++)
            {
                removeTeamSiteStore((BackingStore) storeMap.get(removedStores
                        .elementAt(d)));
            }
            HibernateUtil.update(tss);
        }
        catch (PersistenceException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

    // remove
    private static void removeTeamSiteServer(long p_serverId)
    {
        try
        {
            ServerProxy.getTeamSiteServerPersistenceManager()
                    .deleteTeamSiteServer(
                            ServerProxy.getTeamSiteServerPersistenceManager()
                                    .readTeamSiteServer(p_serverId));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void removeTeamSiteStore(BackingStore p_backingStore)
    {
        try
        {
            ServerProxy.getTeamSiteServerPersistenceManager()
                    .deleteBackingStore(p_backingStore);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////

}
