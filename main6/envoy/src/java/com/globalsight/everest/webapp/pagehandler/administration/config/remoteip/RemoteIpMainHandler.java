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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.util.comparator.RemoteIpComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Deals with some requirements about accessing remote ip filter for
 * webservices.
 */
public class RemoteIpMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(RemoteIpMainHandler.class);

    /**
     * Handes adding remote ip filter requirement.
     * <p>
     * If the ip address is invalid or the ip has been exist in database, the
     * remote ip filter will not be added, and caller can't get any information
     * about it.
     * <p>
     * Because nothing happened if the ip address has been exist in database,
     * there are no problem with resubmit form.
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = RemoteIpConstant.SAVE, formClass = "com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIp", loadFromDb = false)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        logger.debug("Saving remote ip...");

        RemoteIp remoteIp = (RemoteIp) form;

        if (RemoteIpManager.isValidIp(remoteIp.getIp()))
        {
            // Not new remote ip or the ip is not exist in database.
            if (remoteIp.getId() > 0
                    || !RemoteIpManager.isExist(remoteIp.getIp(), remoteIp
                            .getId()))
            {
                HibernateUtil.saveOrUpdate(remoteIp);
            }
        }

        logger.debug("Saving remote ip finished");
    }

    /**
     * Removes some remote ip filters from database according to filter id.
     * <p>
     * For nothing happened if the remote ip filter has been deleted, there are
     * no problem with resubmet form.
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = RemoteIpConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Removing remote ip...");

        String[] ids = request.getParameterValues("selectedIp");
        for (String id : ids)
        {
            RemoteIp remoteIp = HibernateUtil.get(RemoteIp.class, Long
                    .parseLong(id));
            HibernateUtil.delete(remoteIp);
        }

        logger.debug("Removing remote ip finished");
    }

    /**
     * Sets remote ip filters for display table.
     */
    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        logger.debug("Setting table vales...");

        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, RemoteIpManager.getRemoteIps(),
                new RemoteIpComparator(uiLocale), 10,
                RemoteIpConstant.REMOTE_IP_LIST, RemoteIpConstant.REMOTE_IP_KEY);

        logger.debug("Setting table vales finished");
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    	// gbs-1389: restrict direct access to remote ip page/operation if it is
		// not super company user (no remote ip configuration permission).
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
		
    }

}
