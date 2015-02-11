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

package com.globalsight.everest.webapp.pagehandler.terminology.management;

import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseInfo;
import com.globalsight.terminology.TermbaseList;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TermbaseInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>PageHandler is responsible creating, deleting and modifying
 * termbases.</p>
 */

public class TermbaseManagementPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            TermbaseManagementPageHandler.class.getName());

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseManagementPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getTermbaseManager();
            }
            catch (GeneralException ex)
            {
                // ignore.
            }
        }
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale)session.getAttribute(
            WebAppConstants.UILOCALE);

        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();
        
        String action = (String)p_request.getParameter(TERMBASE_ACTION);
        String tbId   = (String)p_request.getParameter(RADIO_BUTTON);
        String name = null;

        // Do some cleanup here for import/export etc.
        sessionMgr.removeElement(TERMBASE_EXPORTER);
        sessionMgr.removeElement(TERMBASE_EXPORT_OPTIONS);
        sessionMgr.removeElement(TERMBASE_IMPORTER);
        sessionMgr.removeElement(TERMBASE_IMPORT_OPTIONS);
        sessionMgr.removeElement(TERMBASE_USERDATA);

        try
        {
            if (tbId != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbId));
                sessionMgr.setAttribute(TERMBASE_TB_ID, tbId);
                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
            }

            if (action == null)
            {
                // show main screen with list of termbases
                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTBAccessControl", enableTBAccessControl);
                setTableNavigation(p_request, session, getTBs(userId,uiLocale),
                           new TermbaseInfoComparator(uiLocale),
                           10,
                           TERMBASE_TB_NAMELIST,
                           TERMBASE_TB_KEY);
            }
            else if (action.equals(TERMBASE_ACTION_NEW))
            {
                // show screen to create new termbase: pass an empty (or,
                // default) definition.
                String definition = s_manager.getDefaultDefinition();

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_MODIFY))
            {
            	if (tbId == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                // load existing definition and display for modification
                String definition = s_manager.getDefinition(name, false);

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_CLONE))
            {
            	if (tbId == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                // load existing definition and display for cloning
                String definition = s_manager.getDefinition(name, true);

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_IMPORT))
            {
            	if (tbId == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                CATEGORY.error("TERMBASE_ACTION_INPUT_IMPORT");

                // load existing definition and display for modification
                String definition = s_manager.getDefinition(name, false);

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_EXPORT))
            {
            	if (tbId == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                CATEGORY.error("TERMBASE_ACTION_INPUT_EXPORT");

                // load existing definition and display for modification
                String definition = s_manager.getDefinition(name, false);

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_STATISTICS))
            {
                // load existing definition and display for modification
                String statistics = s_manager.getStatistics(name);

                sessionMgr.setAttribute(TERMBASE_STATISTICS, statistics);
            }
            else if (action.equals(TERMBASE_ACTION_INPUT_MODELS))
            {
            	if (tbId == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                CATEGORY.error("TERMBASE_ACTION_INPUT_MODELS");

                // load existing definition and display for modification
                String definition = s_manager.getDefinition(name, false);

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if(action.equals(TERMBASE_ACTION_DELETE)) 
            {
            	if (tbId == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                try{
                    ArrayList list = 
                        (ArrayList)ServerProxy.getProjectHandler().getProjectsByTermbaseDepended(name);
                    
                    if(list != null && list.size() > 0) {
                        sessionMgr.setAttribute("projectsByTermbaseDepended", list);
                    }
                    else 
                    {
                        ITermbaseManager m_manager = ServerProxy.getTermbaseManager();
                        m_manager.delete(name, userId, "");
                        ProjectTMTBUsers ptbUsers = new ProjectTMTBUsers();
                        ptbUsers.deleteAllUsers(tbId, "TB");
                    }
                }
                catch(Exception e) {}
                
				sessionMgr.setAttribute("isAdmin", isAdmin);
				sessionMgr.setAttribute("enableTBAccessControl", enableTBAccessControl);
                setTableNavigation(p_request, session, getTBs(userId,uiLocale),
                           new TermbaseInfoComparator(uiLocale),
                           10,
                           TERMBASE_TB_NAMELIST,
                           TERMBASE_TB_KEY);
            }
            else if(action.equals(TERMBASE_ACTION_SAVEUSERS))
            {
                String selectedField = (String)p_request.getParameter("toField");
                
                ProjectTMTBUsers projectTMTBUsers= new ProjectTMTBUsers();
                projectTMTBUsers.updateUsers(tbId, "TB", selectedField);
                
                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTBAccessControl", enableTBAccessControl);
                setTableNavigation(p_request, session, getTBs(userId,uiLocale),
                           new TermbaseInfoComparator(uiLocale),
                           10,
                           TERMBASE_TB_NAMELIST,
                           TERMBASE_TB_KEY);
            }
        }
        catch (TermbaseException ex)
        {
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    /**
     * Get tbs for user
     * @param userId
     * @param uiLocale
     * @return
     * @throws RemoteException
     * 
     * @author Leon Song
     * @since 8.0
     */
    private ArrayList getTBs(String userId, Locale uiLocale)
            throws RemoteException
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();
        ArrayList names = s_manager.getTermbaseList(uiLocale);
        ArrayList tbs = new ArrayList();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        if (enableTBAccessControl)
        {
            if (isAdmin || isSuperAdmin)
            {
                tbs = names;
            }
            else
            {
                ProjectTMTBUsers ptmUsers = new ProjectTMTBUsers();
                List tbIds = ptmUsers.getTList(userId, "TB");
                Iterator it = tbIds.iterator();
                while (it.hasNext())
                {
                    Termbase tb = TermbaseList.get(((BigInteger) it.next())
                            .longValue());
                    if (tb != null)
                    {
                        TermbaseInfo tbi = new TermbaseInfo(tb.getId(),
                                tb.getName(), tb.getDescription(),
                                tb.getCompanyId());
                        tbs.add(tbi);
                    }
                }
            }
        }
        else
        {
            tbs = names;
        }
        return tbs;
    }
}

