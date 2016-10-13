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
package com.globalsight.everest.webapp.pagehandler.administration.config.dbconnection;

// Envoy packages
import com.globalsight.cxe.entity.dbconnection.DBConnection;
import com.globalsight.cxe.entity.dbconnection.DBConnectionImpl;
import com.globalsight.cxe.persistence.dbconnection.DBConnectionPersistenceManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.DBConnectionComparator;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.util.GeneralException;

//Sun
import java.io.IOException;

import java.rmi.RemoteException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DBConnectionMainHandler extends PageHandler
{
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
            if (DBConnectionConstants.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session, DBConnectionConstants.DB_KEY);
            }
            else if (DBConnectionConstants.NEW.equals(action))
            {
                createConnection(p_request, session);
                clearSessionExceptTableInfo(session, DBConnectionConstants.DB_KEY);
            }
            else if (DBConnectionConstants.EDIT.equals(action))
            {
                updateConnection(p_request, session);
                clearSessionExceptTableInfo(session, DBConnectionConstants.DB_KEY);
            }
            else if (DBConnectionConstants.TEST.equals(action))
            {
                testConnection(p_request, session);
                clearSessionExceptTableInfo(session, DBConnectionConstants.DB_KEY);
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
     * Update DB Connection.
     */
    private void updateConnection(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager)
            p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        DBConnectionImpl db = (DBConnectionImpl)
            sessionMgr.getAttribute(DBConnectionConstants.DB_KEY);
        getParams(p_request, db);
        ServerProxy.getDBConnectionPersistenceManager().updateDBConnection(db);
    }

    /**
     * Create a DB Connection.
     */
    private void createConnection(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        DBConnectionImpl db = new DBConnectionImpl();
        getParams(p_request, db);
        ServerProxy.getDBConnectionPersistenceManager().createDBConnection(db);
    }

    /**
     * Get request params and update DBConnection.
     */
    private void getParams(HttpServletRequest p_request, DBConnectionImpl p_db)
    {
        String name = p_request.getParameter("nameField");
        String desc = p_request.getParameter("descField");
        String driver = p_request.getParameter("driverField");
        String connection = p_request.getParameter("connectionField");
        String username = p_request.getParameter("usernameField");
        String password = p_request.getParameter("passwordField");
        p_db.setName(name);
        p_db.setDescription(desc);
        p_db.setDriver(driver);
        p_db.setConnection(connection);
        p_db.setUserName(username);
        p_db.setPassword(password);
    }

    //test the DBConnection received and return its status
    private void testConnection(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException
    {
        String id = p_request.getParameter("id");
        DBConnection dbconnection = null;
        try {
            dbconnection = ServerProxy.getDBConnectionPersistenceManager().
                    readDBConnection(Long.parseLong(id));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        p_request.setAttribute("id", id);
        ResourceBundle bundle = getBundle(p_session);
        String name = dbconnection.getName();
        String driver = dbconnection.getDriver();
        String connectUrl = dbconnection.getConnection();
        String userName = dbconnection.getUserName();
        String password = dbconnection.getPassword();

        Connection connection = null;
        Statement statement = null;
        Properties props = new Properties();
        props.put("user", userName);
        props.put("password", password);
        try
        {
            // We need to load the thin client jdbc driver
            //DriverManager.getConnection() is prone to deadlocks
            // Code to avoid deadlock
            int timeout = 5;
            DriverManager.setLoginTimeout(timeout);
            Driver d = (Driver)Class.forName(driver).newInstance();
            connection = d.connect(connectUrl, props );
            statement = connection.createStatement();
            p_request.setAttribute("testResults", bundle.getString("msg_connection_successful"));
        }
        catch (ClassNotFoundException e)
        {
            p_request.setAttribute("testResults", bundle.getString("msg_invalid_driver"));
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            p_request.setAttribute("testResults", bundle.getString("msg_bad_connection"));
        }
        catch (IllegalAccessException ia)
        {
            ia.printStackTrace();
            p_request.setAttribute("testResults", bundle.getString("msg_invalid_driver"));
        }
        catch (Error er)
        {
            er.printStackTrace();
            p_request.setAttribute("testResults", bundle.getString("msg_bad_connection"));
        }
        catch (Exception ie)
        {
            ie.printStackTrace();
            p_request.setAttribute("testResults", bundle.getString("msg_invalid_driver"));
        }
        finally
        {
            // close the connection
            try
            {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            }
            catch (SQLException closeError)
            {
                closeError.printStackTrace();
            }
        }
    }
    /**
     * Get list of all connections.
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Collection dbconnections =
            ServerProxy.getDBConnectionPersistenceManager().getAllDBConnections();
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, new ArrayList(dbconnections),
                       new DBConnectionComparator(uiLocale),
                       10,
                       DBConnectionConstants.DB_LIST, DBConnectionConstants.DB_KEY);
    }
}
