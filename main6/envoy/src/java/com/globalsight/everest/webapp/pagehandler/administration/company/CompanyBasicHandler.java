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
package com.globalsight.everest.webapp.pagehandler.administration.company;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.login.LoginMainHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.util.GeneralException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
public class CompanyBasicHandler extends PageHandler implements CompanyConstants {
    private static Logger s_logger = Logger.getLogger(CompanyBasicHandler.class.getName());
    
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
        p_request.setAttribute(SystemConfigParamNames.ENABLE_SSO, getSSOEnable());
        p_request.setAttribute(SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED, 
                ServerProxy.getMailer().isSystemNotificationEnabled());
        
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
        try
        {
            if (action.equals(CompanyConstants.CREATE))
            {
            	// gbs-1389: restrict direct access to create company without
				// create company permission.
            	if (!userPerms.getPermissionFor(Permission.COMPANY_NEW)) 
        		{
            		if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            		{
            			p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
            		}
            		else
            		{
            			p_response.sendRedirect(p_request.getContextPath());
            		}
        			return;
        		}
                setCompanyNames(p_request);
            }
            else if (action.equals(CompanyConstants.EDIT))
            {
            	// gbs-1389: restrict direct access to edit company without
				// edit company permission.
            	if (!userPerms.getPermissionFor(Permission.COMPANY_EDIT)) 
        		{
            		if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            		{
            			p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
            		}
            		else
            		{
            			p_response.sendRedirect(p_request.getContextPath());
            		}
        			return;
        		}
                String name = (String)p_request.getParameter("name");
                SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
                Company company = (Company)ServerProxy.getJobHandler().getCompany(name);
                sessionMgr.setAttribute(CompanyConstants.COMPANY, company);
                p_request.setAttribute("edit", "true");
            }
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
     * Get list of all company names.  Needed in jsp to determine duplicate names.
     */
    private void setCompanyNames(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        ArrayList list = (ArrayList)ServerProxy.getJobHandler().getAllCompanies();
        ArrayList names = new ArrayList();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Company company = (Company)list.get(i);
                names.add(company.getName());
            }
        }
        p_request.setAttribute(CompanyConstants.NAMES, names);
        
        String defEmail = ServerProxy.getSystemParameterPersistenceManager()
            .getAdminSystemParameter(SystemConfigParamNames.ADMIN_EMAIL)
            .getValue();
        p_request.setAttribute(CompanyConstants.EMAIL, defEmail);
    }
    
    private String getSSOEnable()
    {
        String temp = "false";
        try
        {
            temp = ServerProxy.getSystemParameterPersistenceManager()
                    .getAdminSystemParameter(SystemConfigParamNames.ENABLE_SSO)
                    .getValue();
        }
        catch (Exception e)
        {
            s_logger.error("There is an error when getting ENABLE_SSO from System Parameter");
        }
        return temp;
    }
}
