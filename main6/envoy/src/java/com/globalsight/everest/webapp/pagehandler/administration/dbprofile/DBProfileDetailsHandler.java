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
package com.globalsight.everest.webapp.pagehandler.administration.dbprofile;

import org.apache.log4j.Logger;

// Envoy packages
import com.globalsight.cxe.entity.databaseprofile.DatabaseProfile;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.util.collections.HashtableValueOrderWalker;
import com.globalsight.util.GeneralException;
//Sun
import java.io.IOException;
import java.util.ResourceBundle;
import java.rmi.RemoteException;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * DBProfileDetailsHandler, A page handler to produce the entry page(index.jsp) for DataSources management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class DBProfileDetailsHandler extends PageHandler
{

    /**
     * Invokes this PageHandler
     * <p>
     * @param p_thePageDescriptor the page descriptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context) throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        ResourceBundle bundle = getBundle(session);
        SessionManager sessionMgr = 
            (SessionManager)p_request.getSession().getAttribute(
                WebAppConstants.SESSION_MANAGER);
        try
        {
            String id = p_request.getParameter("id");
            DatabaseProfile profile = ServerProxy.getDatabaseProfilePersistenceManager().
                getDatabaseProfile(Long.parseLong(id));
            p_request.setAttribute("profile", profile);
            p_request.setAttribute("dbcolumns",
                ServerProxy.getDatabaseColumnPersistenceManager().getDatabaseColumns(
                Long.parseLong(id)));
            DBProfileColumnHandler dbpch = new DBProfileColumnHandler();
            p_request.setAttribute("modePairs", dbpch.getModePairs(bundle));
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }    
}
