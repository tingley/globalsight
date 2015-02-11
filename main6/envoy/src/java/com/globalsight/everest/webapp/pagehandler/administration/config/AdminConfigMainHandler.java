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

package com.globalsight.everest.webapp.pagehandler.administration.config;

// Envoy packages
import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.config.SystemParameter;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class AdminConfigMainHandler extends PageHandler {

    public static final String SLASH = "/";
    
    private String[] sysParams = new String[] {
            SystemConfigParamNames.HIBERNATE_LOGGING,
            //Comment out this parameter because seems this parameter can be 
            //configured during install and here it cannot work.
            //SystemConfigParamNames.SYSTEM_LOGGING_PRIORITY,
            //SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED,
            
            SystemConfigParamNames.CUSTOMER_INSTALL_KEY,
            SystemConfigParamNames.MAIL_SERVER,
    };
    
    private String[] specialSysParams = new String[] {
            SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL
    };
    
    /**
     * @param pageDescriptor
     *            the description of the page to be produced
     * @param request
     *            the original request sent from the browser
     * @param response
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException {
    	
		HttpSession session = p_request.getSession(false);
		// gbs-1389: restrict direct access to admin system parameter page
		// without the permission.
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.SYSTEM_PARAMS)) 
		{
			p_response.sendRedirect(p_request.getContextPath());
			return;
		}
        if (this.needUpdate(p_request)) {
            setSystemParameters(p_request);
        }
        getSystemParameters(p_request);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
    
    private boolean needUpdate(HttpServletRequest p_request)
    {
        return (p_request.getParameter(sysParams[0]) != null ||
                p_request.getParameter(specialSysParams[0]) != null);
    }

    /**
     * @param p_request
     * @return
     * @throws Exception
     */
    private boolean isSuperAdmin(HttpServletRequest p_request) {
        SessionManager sm=(SessionManager)p_request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
        String userId=((User)sm.getAttribute(WebAppConstants.USER)).getUserId();
        try {
            return UserUtil.isSuperAdmin(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getSystemParameters(HttpServletRequest p_request)
            throws EnvoyServletException {
        for (int i = 0; i < sysParams.length; i++) {
            p_request.setAttribute(sysParams[i], getSystemParameter(sysParams[i])
                    .getValue());
        }
        
        for (int i = 0; i < specialSysParams.length; i++)
        {
            p_request.setAttribute(specialSysParams[i], this.getSystemParameter(specialSysParams[i]).getValue());
        }
    }

    private void setSystemParameters(HttpServletRequest p_request)
            throws EnvoyServletException {
        for (int i = 0; i < sysParams.length; i++) {
            this.setSystemParameter(p_request, sysParams[i]);
        }
        for (int i = 0; i < specialSysParams.length; i++)
        {
            this.setSystemParameter(p_request, specialSysParams[i]);
        }
        
//        //For special system parameters
//        String paramName = SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL;
//        String paramValue = p_request.getParameter(paramName);
//        paramValue = paramValue + "/24";
//        SystemParameter sysParam = getSystemParameter(SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL);
//        sysParam.setValue(paramValue);
//        updateSystemParameter(sysParam);
    }
    
    private void setSystemParameter(HttpServletRequest p_request, String paramName)
    {
        SystemParameter sysParam = getSystemParameter(paramName);
        sysParam.setValue(p_request.getParameter(paramName));
        updateSystemParameter(sysParam);
    }

    private SystemParameter getSystemParameter(String p_name)
            throws EnvoyServletException {
        try {
            return ServerProxy.getSystemParameterPersistenceManager()
                    .getAdminSystemParameter(p_name);
        } catch (RemoteException re) {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    private SystemParameter updateSystemParameter(
            SystemParameter p_systemParameter) throws EnvoyServletException {
        try {
            return ServerProxy.getSystemParameterPersistenceManager()
                    .updateAdminSystemParameter(p_systemParameter);
        } catch (RemoteException re) {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }
}