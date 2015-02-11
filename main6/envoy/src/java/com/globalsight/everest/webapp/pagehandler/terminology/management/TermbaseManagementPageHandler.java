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

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TermbaseInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseInfo;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * <p>
 * PageHandler is responsible creating, deleting and modifying termbases.
 * </p>
 */
public class TermbaseManagementPageHandler extends PageHandler implements
        WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(TermbaseManagementPageHandler.class.getName());
    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;
    static private int NUM_PER_PAGE = 10;

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

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();

        String action = (String) p_request.getParameter(TERMBASE_ACTION);
        String tbId = (String) p_request.getParameter(RADIO_BUTTON);
        String name = null;
        String description = null;
        long companyId = -1;
        setNumberOfPerPage(p_request);

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
                com.globalsight.terminology.java.Termbase tb = HibernateUtil
                        .get(com.globalsight.terminology.java.Termbase.class,
                                Long.parseLong(tbId));
                name = tb.getName();
                companyId = tb.getCompany().getId();
                description = tb.getDescription();
                sessionMgr.setAttribute(TERMBASE_TB_ID, tbId);
                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
            }

            if (action == null)
            {
                // show main screen with list of termbases
                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTBAccessControl",
                        enableTBAccessControl);

                setTableNavigation(p_request, session,
                        getTBs(p_request, session, userId, uiLocale), new TermbaseInfoComparator(
                                uiLocale), NUM_PER_PAGE, TERMBASE_TB_NAMELIST,
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
                p_request.setAttribute("description", description);
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
                String statistics = s_manager.getStatisticsNoIndexInfo(name);

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
            else if (action.equals(TERMBASE_ACTION_DELETE))
            {
                if (tbId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=termbases");
                    return;
                }
                try
                {
                    List<ProjectImpl> list = ServerProxy.getProjectHandler()
                            .getProjectsByTermbaseDepended(name, companyId);
                    if (list != null && list.size() > 0)
                    {
                        sessionMgr.setAttribute("projectsByTermbaseDepended",
                                list);
                    }
                    else
                    {
                        ITermbaseManager m_manager = ServerProxy
                                .getTermbaseManager();
                        m_manager.delete(name, userId, "");
                        ProjectTMTBUsers ptbUsers = new ProjectTMTBUsers();
                        ptbUsers.deleteAllUsers(tbId, "TB");
                    }
                }
                catch (Exception e)
                {
                }

                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTBAccessControl",
                        enableTBAccessControl);
                setTableNavigation(p_request, session,
                        getTBs(p_request, session, userId, uiLocale), new TermbaseInfoComparator(
                                uiLocale), NUM_PER_PAGE, TERMBASE_TB_NAMELIST,
                        TERMBASE_TB_KEY);
            }
            else if (action.equals(TERMBASE_ACTION_SAVEUSERS))
            {
                String selectedField = (String) p_request
                        .getParameter("toField");

                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                projectTMTBUsers.updateUsers(tbId, "TB", selectedField);

                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTBAccessControl",
                        enableTBAccessControl);
                setTableNavigation(p_request, session,
                        getTBs(p_request, session, userId, uiLocale), new TermbaseInfoComparator(
                                uiLocale), NUM_PER_PAGE, TERMBASE_TB_NAMELIST,
                        TERMBASE_TB_KEY);
            }
        }
        catch (TermbaseException ex)
        {
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.toString());
        }
        
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
   
    private void setNumberOfPerPage(HttpServletRequest req)
    {
        String pageSize = (String) req.getParameter("numOfPageSize");

        if (StringUtil.isNotEmpty(pageSize))
        {
            try
            {
                NUM_PER_PAGE = Integer.parseInt(pageSize);
            }
            catch (Exception e)
            {
                NUM_PER_PAGE = Integer.MAX_VALUE;
            }
        }
    }

	/**
	 * Get tbs for user
	 * 
	 * @param userId
	 * @param uiLocale
	 * @return
	 * @throws RemoteException
	 * 
	 * @author Leon Song
	 * @since 8.0
	 */
    private List<TermbaseInfo> getTBs(HttpServletRequest p_request,
			HttpSession session,String userId, Locale uiLocale)
            throws RemoteException
    {
        List<TermbaseInfo> tbs = new ArrayList<TermbaseInfo>();
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperPM = UserUtil.isSuperPM(userId);
        boolean isSuperLP = UserUtil.isSuperLP(userId);

        List<String> companies = null;
        List<TermbaseInfo> allTBs = new ArrayList<TermbaseInfo>();
        try
        {
            allTBs.addAll(s_manager.getTermbaseList(uiLocale));
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }

        if ("1".equals(currentCompanyId))
        {
            companies = new ArrayList<String>();
            if (isSuperLP)
            {
                // Get all the companies the super translator worked for
                List<Project> projectList = null;
                try
                {
                    projectList = ServerProxy.getProjectHandler()
                            .getProjectsByUser(userId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                for (Iterator<Project> it = projectList.iterator(); it
                        .hasNext();)
                {
                    Project pj = (Project) it.next();
                    String companyId = String.valueOf(pj.getCompanyId());
                    if (!companies.contains(companyId))
                    {
                        companies.add(companyId);
                    }
                }
                Iterator<TermbaseInfo> itAllTBs = allTBs.iterator();
                while (itAllTBs.hasNext())
                {
                    TermbaseInfo tbi = (TermbaseInfo) itAllTBs.next();
                    if (companies.contains(tbi.getCompanyId()))
                    {
                        tbs.add(tbi);
                    }
                }

            }
            else
            {
                // Super admin
                tbs = allTBs;
            }
        }
        else
        {
            if (enableTBAccessControl && !isAdmin)
            {

                ArrayList<Long> tbListOfUser = new ArrayList<Long>();
                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                List tbIdList = projectTMTBUsers.getTList(userId, "TB");
                Iterator it = tbIdList.iterator();
                while (it.hasNext())
                {
                    long id = ((BigInteger) it.next()).longValue();
                    Termbase tb = TermbaseList.get(id);

                    if (isSuperPM)
                    {
                        if (tb != null
                                && tb.getCompanyId().equals(currentCompanyId))
                        {
                            tbListOfUser.add(id);
                        }
                    }
                    else
                    {
                        if (tb != null)
                        {
                            tbListOfUser.add(id);
                        }
                    }
                }

                Iterator<TermbaseInfo> itAllTBs = allTBs.iterator();
                while (itAllTBs.hasNext())
                {
                    TermbaseInfo tbi = (TermbaseInfo) itAllTBs.next();
                    if (tbListOfUser.contains(tbi.getTermbaseId()))
                    {
                        tbs.add(tbi);
                    }
                }
            }
            else
            {
                tbs = allTBs;
            }
        }

		filterTbByCompanyName(p_request, session, tbs);
		filterTbByName(p_request, session, tbs);
		
		SortUtil.sort(tbs, new TermbaseInfoComparator(0, uiLocale));

        return tbs;
    }

	private void filterTbByCompanyName(HttpServletRequest p_request,
			HttpSession p_session, List<TermbaseInfo> tbList) 
	{
		SessionManager sessionMgr = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		String tbCompanyFilter = (String) p_request.getParameter("tbCompanyFilter");
		
		if (tbCompanyFilter == null) 
		{
			tbCompanyFilter = (String) sessionMgr.getAttribute("tbCompanyFilter");
		}
		if (tbCompanyFilter == null) 
		{
			tbCompanyFilter = "";
		}
		sessionMgr.setAttribute("tbCompanyFilter", tbCompanyFilter.trim());
		
		if (StringUtil.isNotEmpty(tbCompanyFilter)) 
		{
			for (Iterator<TermbaseInfo> it = tbList.iterator(); it.hasNext();) 
			{
				TermbaseInfo tb = it.next();
				String companyName = CompanyWrapper.getCompanyNameById(
						tb.getCompanyId()).toLowerCase();
				if (companyName.indexOf(tbCompanyFilter.toLowerCase().trim()) == -1) 
				{
					it.remove();
				}
			}
		}

	}

    private void filterTbByName(HttpServletRequest p_request,
            HttpSession p_session,List<TermbaseInfo> tbList)
    {
		SessionManager sessionMgr = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		String tbNameFilter = (String) p_request.getParameter("tbNameFilter");

		if (tbNameFilter == null) 
		{
			tbNameFilter = (String) sessionMgr.getAttribute("tbNameFilter");
		}
		if (tbNameFilter == null) 
		{
			tbNameFilter = "";
		}
		sessionMgr.setAttribute("tbNameFilter", tbNameFilter.trim());

		 if (StringUtil.isNotEmpty(tbNameFilter))
	        {
	            for (Iterator<TermbaseInfo> it = tbList.iterator(); it.hasNext();)
	            {
	                TermbaseInfo tb = it.next();
	                if (tb.getName().toLowerCase()
	                        .indexOf(tbNameFilter.toLowerCase().trim()) == -1)
	                {
	                    it.remove();
				}
			}
		}
	}
}
