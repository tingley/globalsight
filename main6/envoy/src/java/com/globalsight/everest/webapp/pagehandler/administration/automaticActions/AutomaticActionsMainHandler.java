package com.globalsight.everest.webapp.pagehandler.administration.automaticActions;

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

import java.io.IOException;
import java.rmi.RemoteException;

import java.util.Vector;
import java.util.Locale;
import java.util.Collection;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.globalsight.everest.autoactions.*;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.AutoActionsComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
/**
 */
public class AutomaticActionsMainHandler extends PageHandler
{
    private AutoActionManagerLocal actionManager = 
        new AutoActionManagerLocal();
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
            if(action != null) {
                if (action.equals("create"))
                {
                    createAction(p_request);
                }
                else if (action.equals("modify"))
                {
                    
                    editAction(p_request);
                }
                
                else if (action.equals("remove"))
                {
                    removeAction(p_request); 
                }
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

    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Vector activities = 
            vectorizedCollection(actionManager.getAllActions());
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, activities,
                           new AutoActionsComparator(uiLocale),
                           10,"actionList", "actionKey");
    }
    
    private void createAction(HttpServletRequest p_request) 
        throws AutoActionException, RemoteException {
        AutoAction autoAction = new AutoAction();

        String name = p_request.getParameter("name");
        String email = p_request.getParameter("email");
        String description = p_request.getParameter("description");
        String companyId = CompanyThreadLocal.getInstance().getValue();
        
        if (!actionManager.isActionExist(name)) {
            autoAction.setName(name);
            autoAction.setEmail(email);
            autoAction.setDescription(description);
            autoAction.setCompanyID(companyId);
        }
        
        actionManager.createAction(autoAction);
    }
    
    private void editAction(HttpServletRequest p_request) 
        throws AutoActionException, RemoteException {
        if(p_request.getParameter("actionID") != null) {
            long id = Integer.parseInt(p_request.getParameter("actionID"));
            AutoAction autoAction = actionManager.getActionByID(id);
       
            try {
                if(p_request.getParameter("name") !=null) {
                    autoAction.setName(p_request.getParameter("name"));
                }
                
                if(p_request.getParameter("email") !=null) {
                    autoAction.setEmail(p_request.getParameter("email"));
                }
                
                autoAction.setDescription(p_request.getParameter("description"));
                actionManager.updateAction(autoAction);
            } catch (Exception e) {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }
    }
    
    /*
     * remove the auto action.
     */
    private void removeAction(HttpServletRequest p_request) 
        throws AutoActionException, RemoteException {
        
        if(p_request.getParameter("id") != null) {
            long id = Integer.parseInt(p_request.getParameter("id"));
       
            try {
                Collection acitivities = actionManager.getActivitiesByActionID
                                         (p_request.getParameter("id"));
                
                if(acitivities != null && acitivities.size() > 0) {
                    p_request.setAttribute("canBeRemoved", "false");
                }
                else {
                    actionManager.removeAction(id);
                }
            } catch (Exception e) {
                throw new 
                    EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }
    }
}


