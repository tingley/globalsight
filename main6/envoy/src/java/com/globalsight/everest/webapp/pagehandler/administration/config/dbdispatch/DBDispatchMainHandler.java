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
package com.globalsight.everest.webapp.pagehandler.administration.config.dbdispatch;
// Envoy packages
import com.globalsight.cxe.entity.dbconnection.DBConnectionImpl;
import com.globalsight.cxe.entity.dbconnection.DBDispatchImpl;
import com.globalsight.cxe.persistence.dbconnection.DBConnectionPersistenceManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.util.comparator.DBDispatchesComparator;
import com.globalsight.util.GeneralException;
//Sun
import java.io.IOException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * DBDispatchMainHandler, A page handler to produce the entry page (index.jsp) for DataSources management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class DBDispatchMainHandler extends PageHandler
{
    public static final String DB_KEY = "dbImport";
    public static final String DB_LIST = "dbImports";
    public static final String NAMES = "names";
    public static final long MILLIS_PER_DAY = 86400000;

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
            if ("cancel".equals(action))
            {
                clearSessionExceptTableInfo(session, DB_KEY);
            }
            else if ("new".equals(action) || "duplicate".equals(action))
            {
                createDispatch(p_request, session);
                clearSessionExceptTableInfo(session, DB_KEY);
            }
            else if ("edit".equals(action))
            {
                updateDispatch(p_request, session);
                clearSessionExceptTableInfo(session, DB_KEY);
            }
            dataForTable(p_request, session);
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
     * Get list of all db import profiles.
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Collection dispatches =
            ServerProxy.getDBConnectionPersistenceManager().getAllDBDispatches();
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        Hashtable pairs = getDBConnectionPairs();
        SessionManager sessionMgr = (SessionManager)
            p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute("dbConnectionPairs", pairs);
        setTableNavigation(p_request, p_session, new ArrayList(dispatches),
                       new DBDispatchesComparator(uiLocale, pairs),
                       10,
                       DB_LIST, DB_KEY);
    }

    /**
     * Edit a DBDispatch.
     */
    private void updateDispatch(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager)
            p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        DBDispatchImpl db = (DBDispatchImpl) sessionMgr.getAttribute(DB_KEY);
        getParams(p_request, db);
        ServerProxy.getDBConnectionPersistenceManager().updateDBDispatch(db);
    }

    /**
     * Create a DBDispatch.
     */
    private void createDispatch(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        DBDispatchImpl db = new DBDispatchImpl();
        getParams(p_request, db);
        ServerProxy.getDBConnectionPersistenceManager().createDBDispatch(db);
    }

    /**
     * Get parameters from the request.
     */
    private void getParams(HttpServletRequest p_request, DBDispatchImpl p_db)
    {
        String name = p_request.getParameter("nameField");
        String desc = p_request.getParameter("descField");
        String pending = p_request.getParameter("pendingField");
        String records = p_request.getParameter("recordsField");
        String batch = p_request.getParameter("batchField");
        String force = p_request.getParameter("forceField");
        Long millis = new Long(force);
        String connection = p_request.getParameter("connectionField");
        p_db.setName(name);
        p_db.setDescription(desc);
        p_db.setTableName(pending);
        p_db.setRecordsPerPage(Long.parseLong(records));
        p_db.setPagesPerBatch(Long.parseLong(batch));
        p_db.setMaxElapsedMillis(millis.longValue() * MILLIS_PER_DAY);
        p_db.setConnectionId(Long.parseLong(connection));
    }

    // create hashtable of DBConnection names and ids
    public Hashtable getDBConnectionPairs() throws EnvoyServletException
    {
        //Call server
        Vector dbconnectionpairs = null;

        try
        {
            dbconnectionpairs = vectorizedCollection(
                ServerProxy.getDBConnectionPersistenceManager().getAllDBConnections());
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

        int rowSize = dbconnectionpairs.size();
        Hashtable dbconnection_pair = new Hashtable();
        for (int i=0; i<rowSize; i++)
        {
            DBConnectionImpl dbconnection = (DBConnectionImpl)dbconnectionpairs.elementAt(i);
	        dbconnection_pair.put(new Long((long)dbconnection.getId()), dbconnection.getName());
        }
        return dbconnection_pair;
    }

}
