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

package com.globalsight.everest.webapp.pagehandler.administration.config.remoteip;

import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Deals with some web requirements about access remote ip filter for
 * webservice.
 */
public class RemoteIpBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(RemoteIpBasicHandler.class);

    private ThreadLocal<Long> ID = new ThreadLocal<Long>();

    /**
     * Checks that the ip address is valid and not exist in the database.
     * 
     * @see RemoteIpManager#isExist(String)
     * @see RemoteIpManager#isValidIp(String)
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = RemoteIpConstant.VALIDATE_IP, formClass = "")
    public void validateIp(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Validating ip address...");

        ResourceBundle resource = getBundle(request.getSession());
        PrintWriter out = response.getWriter();

        try
        {
            String ip = request.getParameter("ip");
            String id = request.getParameter("id");
            if (id == null)
                id = "-1";
            
            if (ip != null && ip.length() > 0)
            {
                if (!RemoteIpManager.isValidIp(ip))
                    out.write(resource.getString("jsmsg_invalid_ip"));

                if (RemoteIpManager.isExist(ip, Long.parseLong(id)))
                    out.write(resource.getString("jsmsg_exist_ip"));
            }
            else
            {
                out.write(resource.getString("jsmsg_null_ip"));
            }

            logger.debug("Validating ip address finished");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    /**
     * Sets remote ip filters for display table.
     */
    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        if (ID.get() != null)
        {
            request.setAttribute(RemoteIpConstant.REMOTE_IP, HibernateUtil.get(
                    RemoteIp.class, ID.get()));
        }
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
		// gbs-1389: restrict direct access to remote ip page/operation if it is
		// not super company user (no remote ip configuration permission)..
    	HttpSession session = request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		try
		{
			if (!userPerms.getPermissionFor(Permission.SYSTEM_PARAMS)) 
			{
				request.setAttribute("restricted_access", true);
				response.sendRedirect(request.getContextPath());
				return;
			}
		}
		catch (Exception e)
		{
			logger.error("Error happens when redirecting restricted access.");
		}
		
        ID.set(null);

        String id = request.getParameter("id");
        if (id != null)
        {
            RemoteIp remoteIP = HibernateUtil.get(RemoteIp.class, Long
                    .parseLong(id));
            if (remoteIP != null)
            {
                ID.set(Long.parseLong(id));
            }
        }
    }
}
