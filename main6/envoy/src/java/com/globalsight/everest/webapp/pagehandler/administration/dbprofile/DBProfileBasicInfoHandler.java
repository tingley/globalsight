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

// Envoy packages
import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.databaseprofile.DatabaseProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

/**
 * DBProfileBasicInfoHandler, A page handler to produce the entry page(index.jsp) for DataSources management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class DBProfileBasicInfoHandler extends PageHandler
{

  // Category for log4j logging.
  private static final Logger CATEGORY =
    Logger.getLogger(DBProfileBasicInfoHandler.class.getName());


    /**
     * Invokes this PageHandler
     * <p>
     * @param p_thePageDescriptor the page descriptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
        HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
        ServletContext p_context) throws ServletException, IOException, EnvoyServletException
    {
        SessionManager sessionMgr = 
            (SessionManager)p_theRequest.getSession().getAttribute(
                WebAppConstants.SESSION_MANAGER);
        String action = p_theRequest.getParameter("action");
        if ("edit".equals(action))
        {
            try
            {
                String id = p_theRequest.getParameter("id");
                DatabaseProfile profile = ServerProxy.getDatabaseProfilePersistenceManager().
                    getDatabaseProfile(Long.parseLong(id));
                sessionMgr.setAttribute("ModDBProfile", profile);
            }
            catch (RemoteException re)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
            }
            catch (GeneralException ge)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
            }
        }

        // list user input
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("DBProfileName "+sessionMgr.getAttribute("DBProfileName"));
            CATEGORY.debug("DBProfileDescription "+sessionMgr.getAttribute("DBProfileDescription"));
            CATEGORY.debug("locProfile "+sessionMgr.getAttribute("locProfile"));
            CATEGORY.debug("acquisitionConn "+sessionMgr.getAttribute("acquisitionConn"));
            CATEGORY.debug("acquisitionSQL "+sessionMgr.getAttribute("acquisitionSQL"));
            CATEGORY.debug("previewURL "+sessionMgr.getAttribute("previewURL"));
            CATEGORY.debug("previewConn "+sessionMgr.getAttribute("previewConn"));
            CATEGORY.debug("previewInsertSQL "+sessionMgr.getAttribute("previewInsertSQL"));
            CATEGORY.debug("previewUpdateSQL "+sessionMgr.getAttribute("previewUpdateSQL"));
            CATEGORY.debug("finalConn "+sessionMgr.getAttribute("finalConn"));
            CATEGORY.debug("finalInsertSQL "+sessionMgr.getAttribute("finalInsertSQL"));
            CATEGORY.debug("finalUpdateSQL "+sessionMgr.getAttribute("finalUpdateSQL"));
            CATEGORY.debug("DBColumns "+sessionMgr.getAttribute("DBColumns"));
        }   
        super.invokePageHandler(p_thePageDescriptor, p_theRequest, 
                                p_theResponse, p_context);
    }    
}
