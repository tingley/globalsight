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

package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.usermgr.UserRoleInfo;
import com.globalsight.everest.util.comparator.UserRoleComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class Modify2Handler extends PageHandler
{
    public static final String MOD_ROLES = "modRoles";

    public static final String SOURCE_LOCALE = "sourceLocale";

    public static final String TARGET_LOCALE = "targetLocale";

    public static final String COMPANY_ID = "companyId";

    /**
     * Invokes this PageHandler.
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        // Get the session manager.
        HttpSession session = p_theRequest.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                .getAttribute(UserConstants.MODIFY_USER_WRAPPER);

        String action = p_theRequest.getParameter(USER_ACTION);

        if (action != null && action.equals(USER_ACTION_MODIFY_LOCALES))
        {
            // We're back to here from MOD3. Extract source, target, and
            // cost map, and add them into the wrapper. Clear turds off
            // the session Mgr.
            String sourceLocale = UserUtil.extractSourceLocale(p_theRequest);
            String targetLocale[] = UserUtil.extractTargetLocales(p_theRequest);
            Hashtable activityCostMap = new Hashtable();
            activityCostMap = UserUtil.generateActivityCostMap(p_theRequest);

            for (int i = 0; i < targetLocale.length; i++)
            {
            	String companyId = (String) p_theRequest.getParameter(COMPANY_ID);
            	if ("null".equals(companyId))
            		companyId = wrapper.getCurCompanyId();
            	
				wrapper.addUserRoles(sourceLocale, targetLocale[i],
						activityCostMap, companyId);
            }
            setRolesData(session, p_theRequest, wrapper);
        }
        else if (action != null
                && (action.equals(USER_ACTION_CANCEL_FROM_ACTIVITIES) || action
                        .equals("self")))
        {
            // this came from MOD3 cancel button - do nothing
            // or if came from sort/next/prev
            setRolesData(session, p_theRequest, wrapper);
        }
        else if (action != null && action.equals(USER_ACTION_REMOVE_ROLES))
        {
            String tmp = p_theRequest.getParameter("radioBtn");
            if (!StringUtil.isEmpty(tmp))
            {
                String[] value = tmp.split(",");
                String sourceLocale = value[0];
                String targetLocale = value[1];
                String companyId = value[2];
                Vector roles = wrapper.getTmpRoles();
                Hashtable sourceTargetMap = wrapper.getTmpSourceTargetMap();

                wrapper.removeRoles(roles, sourceTargetMap, sourceLocale,
                        targetLocale, companyId);
            }

            setRolesData(session, p_theRequest, wrapper);
        }
        else
        {
            // We're here from MOD1, and we need to get the base data from
            // the user.
            UserUtil.extractUserData(p_theRequest, wrapper, false);

            // Now get the roles data
            setRolesData(session, p_theRequest, wrapper);
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
                p_theResponse, p_context);
    }

    /**
     * set roles data for the page each role pair has a company id
     */
    private void setRolesData(HttpSession session, HttpServletRequest request,
            ModifyUserWrapper wrapper) throws UserManagerException,
            RemoteException, GeneralException
    {
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        Collection roles = wrapper.getTmpRoles();
        ArrayList data = new ArrayList();

        if (roles != null)
        {
            Iterator it = roles.iterator();
            HashSet hs = new HashSet();
            while (it.hasNext())
            {
                Role role = (Role) it.next();
                String sourceLocale = role.getSourceLocale();
                String targetLocale = role.getTargetLocale();
                String companyId = String.valueOf(role.getActivity()
                        .getCompanyId());
                // make sure there is no repeated locale pairs
                if (hs.contains(sourceLocale + targetLocale + companyId))
                {
                    continue;
                }
                else
                {
                    hs.add(sourceLocale + targetLocale + companyId);
                }
                UserRoleInfo userRole = new UserRoleInfo();
                userRole.setSourceDisplayName(((GlobalSightLocale) UserHandlerHelper
                        .getLocaleByString(sourceLocale))
                        .getDisplayName(uiLocale));

                userRole.setTargetDisplayName(((GlobalSightLocale) UserHandlerHelper
                        .getLocaleByString(targetLocale))
                        .getDisplayName(uiLocale));
                userRole.setSource(sourceLocale);
                userRole.setTarget(targetLocale);
                userRole.setCompanyId(companyId);
                data.add(userRole);
            }
        }

        setTableNavigation(request, session, data, new UserRoleComparator(
                uiLocale),
                10, // change this to be configurable!
                "numPerPage", "numPages", "userLocalePairs", "sorting",
                "reverseSort", "pageNum", "lastPageNum", "listSize");
    }

    /*
     * @deprecated use setRoleData instead
     */
    private void setData(HttpSession session, HttpServletRequest request,
            ModifyUserWrapper wrapper) throws EnvoyServletException
    {
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        ArrayList data = populateData(wrapper.getTmpSourceTargetMap(), uiLocale);
        try
        {
            setTableNavigation(request, session, data, new UserRoleComparator(
                    uiLocale),
                    10, // change this to be configurable!
                    "numPerPage", "numPages", "userLocalePairs", "sorting",
                    "reverseSort", "pageNum", "lastPageNum", "listSize");
        }
        catch (Exception e)// Config exception (already has message key...)
        {
            throw new EnvoyServletException(e);
        }
    }

    private ArrayList populateData(Hashtable p_map, Locale p_uiLocale)
            throws EnvoyServletException
    {

        Vector vLines = new Vector();

        // Build up the "lines" array with each distinct source/target pair.
        Enumeration eKeys = p_map.keys();
        while (eKeys.hasMoreElements())
        {
            String sourceLocale = (String) eKeys.nextElement();
            Vector vTargets = (Vector) p_map.get(sourceLocale);

            Enumeration eTargets = vTargets.elements();
            while (eTargets.hasMoreElements())
            {
                String targetLocale = (String) eTargets.nextElement();

                String[] pair =
                { sourceLocale, targetLocale };
                vLines.addElement(pair);
            }
        }

        ArrayList data = new ArrayList();

        for (int i = 0; i < vLines.size(); i++)
        {
            String[] curLine = (String[]) vLines.elementAt(i);

            UserRoleInfo userRole = new UserRoleInfo();

            userRole.setSourceDisplayName(((GlobalSightLocale) UserHandlerHelper
                    .getLocaleByString(curLine[0])).getDisplayName(p_uiLocale));
            userRole.setTargetDisplayName(((GlobalSightLocale) UserHandlerHelper
                    .getLocaleByString(curLine[1])).getDisplayName(p_uiLocale));
            userRole.setSource(curLine[0]);
            userRole.setTarget(curLine[1]);
            // Fenshid
            // if (p_map instanceof HashtableWithCompanyId) {
            // userRole.setCompanyId(((HashtableWithCompanyId) p_map)
            // .getCompanyId());
            // }
            data.add(userRole);
        }
        return data;
    }
}