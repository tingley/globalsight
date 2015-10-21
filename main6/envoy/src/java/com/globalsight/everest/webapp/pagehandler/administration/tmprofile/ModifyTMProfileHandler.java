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
package com.globalsight.everest.webapp.pagehandler.administration.tmprofile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.customAttribute.TMPAttribute;
import com.globalsight.cxe.entity.customAttribute.TMPAttributeManager;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TMPAttributeComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

public class ModifyTMProfileHandler extends PageHandler implements
        TMProfileConstants
{
    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    public ModifyTMProfileHandler()
    {
        super();
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        if (p_request.getParameter(TMProfileConstants.TM_PROFILE_ID) == null
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
            return;
        }
        setInfoInSession(p_request);

        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        boolean isSuperPM = UserUtil.isSuperPM(userId);
        sessionMgr.setAttribute("isAdmin", isAdmin);
        sessionMgr.setAttribute("isSuperAdmin", isSuperAdmin);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company curremtCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTMAccessControl = curremtCompany
                .getEnableTMAccessControl();
        sessionMgr.setAttribute(TMProfileConstants.TM_ENABLE_ACCESS_CONTROL,
                enableTMAccessControl);
        // if TM Access Control is enable, for others(not admin), get TMs this
        // user can access
        List tmList = new ArrayList();
        if (enableTMAccessControl)
        {
            // TM Access Control is enable
            if (!isAdmin && !isSuperAdmin)
            {
                if (isSuperPM)
                {
                    String companyId = CompanyThreadLocal.getInstance()
                            .getValue();
                    ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                    List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                    Iterator it = tmIdList.iterator();
                    while (it.hasNext())
                    {
                        ProjectTM tm = null;
                        try
                        {
                            tm = ServerProxy
                                    .getProjectHandler()
                                    .getProjectTMById(
                                            ((BigInteger) it.next())
                                                    .longValue(),
                                            false);
                        }
                        catch (Exception e)
                        {
                            throw new EnvoyServletException(e);
                        }
                        if (String.valueOf(tm.getCompanyId()).equals(companyId))
                        {
                            tmList.add(tm);
                        }
                    }
                }
                else
                {
                    ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                    List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                    Iterator it = tmIdList.iterator();
                    while (it.hasNext())
                    {
                        ProjectTM tm = null;
                        try
                        {
                            tm = ServerProxy
                                    .getProjectHandler()
                                    .getProjectTMById(
                                            ((BigInteger) it.next())
                                                    .longValue(),
                                            false);
                        }
                        catch (Exception e)
                        {
                            throw new EnvoyServletException(e);
                        }
                        tmList.add(tm);
                    }
                }

            }

        }

        sessionMgr.setAttribute("tmsOfUser", tmList);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Set necessary information in the session. This is when they are creating
     * a NEW workflow.
     * 
     * @param p_request
     * @exception EnvoyServletException
     */
    private void setInfoInSession(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        // get the template id first (for edit, or duplicate actions)
        String id = (String) p_request
                .getParameter(TMProfileConstants.TM_PROFILE_ID);
        if (id != null)
        {
            addTMProfileToRequest(p_request, id);
        }
    }

    /**
     * This method is used during modify translation memory profile. In this
     * case, all required info should be stored in session manager. If values
     * are modified, they'll also be updated in session manager.
     * 
     * @param p_request
     * @param p_tmProfileId
     * @exception EnvoyServletException
     */
    private void addTMProfileToRequest(HttpServletRequest p_request,
            String p_tmProfileId) throws EnvoyServletException
    {
        long tmProfileId = -1;
        // This should be part of a util class (after adding paging to a util
        // class, i'll add
        // this one too.
        try
        {
            tmProfileId = Long.parseLong(p_tmProfileId);
        }
        catch (NumberFormatException nfe)
        {
        }
        // get wf template info based on the id
        TranslationMemoryProfile tmProfile = TMProfileHandlerHelper
                .getTMProfileById(tmProfileId);

        HttpSession session = p_request.getSession(false);
		Locale uiLocale = (Locale) session
				.getAttribute(WebAppConstants.UILOCALE);
		if (uiLocale == null)
			uiLocale = Locale.ENGLISH;
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // TM_PROFILE
        sessionMgr.setAttribute(TMProfileConstants.TM_PROFILE, tmProfile);
        // Action type (edit or duplicate)
        String actionType = (String) p_request.getParameter(ACTION);
        sessionMgr.setAttribute(ACTION, actionType);

		List<String> allAtt = TMPAttributeManager.getAvailableAttributenames();
        p_request.setAttribute(TMP_AVAILABLE_ATTS,
                TMPAttributeManager.toOneStr(allAtt));

        List<TMPAttribute> tmpas = tmProfile.getAllTMPAttributes();
        SortUtil.sort(tmpas, new TMPAttributeComparator(
				TMPAttributeComparator.ORDER, uiLocale));
        p_request.setAttribute(TMP_TMP_ATTS, TMPAttributeManager.toOne(tmpas));
    }
}
