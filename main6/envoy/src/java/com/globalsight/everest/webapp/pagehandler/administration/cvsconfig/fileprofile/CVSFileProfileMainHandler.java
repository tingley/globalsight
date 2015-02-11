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
package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.fileprofile;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.cvsconfig.CVSConfigException;
import com.globalsight.everest.cvsconfig.CVSFileProfile;
import com.globalsight.everest.cvsconfig.CVSFileProfileComparator;
import com.globalsight.everest.cvsconfig.CVSFileProfileManagerLocal;
import com.globalsight.everest.cvsconfig.CVSModule;
import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class CVSFileProfileMainHandler extends PageHandler
{
    private CVSFileProfileManagerLocal manager = new CVSFileProfileManagerLocal();
    private static final Logger logger = Logger
            .getLogger(CVSFileProfileMainHandler.class.getName());

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);

            ResourceBundle bundle = getBundle(session);
            HashMap<String, String> params = null;

            String action = p_request.getParameter("action");
            if (CVSConfigConstants.CREATE.equals(action))
            {
                create(p_request);
            }
            else if (CVSConfigConstants.UPDATE.equals(action))
            {
                update(p_request);
            }
            else if (CVSConfigConstants.REMOVE.equals(action))
            {
                remove(p_request);
            }
            else if ("search".equals(action))
            {
                String s_project, s_module, s_srcLocale, s_fileExt;
                s_project = p_request.getParameter("s_project");
                s_module = p_request.getParameter("s_module");
                s_srcLocale = p_request.getParameter("s_sourceLocale");
                s_fileExt = p_request.getParameter("s_fileExt");

                params = new HashMap<String, String>();
                params.put("project", s_project);
                params.put("module", s_module);
                params.put("sourceLocale", s_srcLocale);
                params.put("fileExt", s_fileExt);
            }
            sessionMgr.setAttribute("searchParams", params);
            setSearchInfo(session, sessionMgr);
            dataForTable(p_request, session, params);

        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        catch (HibernateException e)
        {
            throw new EnvoyServletException(e);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, HashMap<String, String> p_params)
            throws RemoteException, NamingException, GeneralException
    {
        // SessionManager sessionMgr =
        // (SessionManager)p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        // ArrayList<CVSFileProfile> cvsFileProfiles =
        // (ArrayList<CVSFileProfile>)manager.getAllCVSFileProfiles();
        // sessionMgr.setAttribute(CVSConfigConstants.CVS_FILE_PROFILE_LIST,
        // cvsFileProfiles);

        Vector data = vectorizedCollection(manager
                .getAllCVSFileProfiles(p_params));
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, data,
                new CVSFileProfileComparator(uiLocale), 10,
                CVSConfigConstants.CVS_FILE_PROFILE_LIST,
                CVSConfigConstants.CVS_FILE_PROFILE_KEY);
    }

    /**
     * Create new CVS file profile
     * 
     * @param p_request
     * @throws CVSConfigException
     * @throws RemoteException
     */
    private void create(HttpServletRequest p_request)
            throws CVSConfigException, RemoteException
    {
        CVSFileProfile cvsfp = null;
        HttpSession session = null;
        ArrayList<CVSFileProfile> fileProfiles = new ArrayList<CVSFileProfile>();
        try
        {
            String projectId, moduleId;
            ProjectImpl project = null;
            CVSModule module = null;

            CVSServerManagerLocal cvsManager = new CVSServerManagerLocal();
            projectId = p_request.getParameter("projects");
            moduleId = p_request.getParameter("servers");
            project = (ProjectImpl) ServerProxy.getProjectHandler()
                    .getProjectById(Long.parseLong(projectId));
            module = cvsManager.getModule(Long.parseLong(moduleId));
            long companyId = CompanyWrapper.getCurrentCompanyIdAsLong();

            String indexStr = p_request.getParameter("fpsize");
            if (indexStr != null && !indexStr.trim().equals(""))
            {
                int index = Integer.parseInt(indexStr);
                String fileExt = "", fp = "";
                for (int i = 0; i < index; i++)
                {
                    fileExt = p_request.getParameter("fileext" + i);
                    fp = p_request.getParameter("fp" + i);

                    if (fp != null && !fp.trim().equals("-1"))
                    {
                        cvsfp = new CVSFileProfile();
                        cvsfp.setProject(project);
                        cvsfp.setCompanyId(companyId);
                        cvsfp.setSourceLocale(p_request
                                .getParameter("srcLocales"));
                        cvsfp.setModule(module);
                        cvsfp.setFileExt(fileExt);
                        cvsfp.setFileProfile((FileProfileImpl) ServerProxy
                                .getFileProfilePersistenceManager()
                                .getFileProfileById(Long.parseLong(fp), false));

                        fileProfiles.add(cvsfp);
                    }
                }
            }
            manager.add(fileProfiles);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }

    }

    private void update(HttpServletRequest p_request)
            throws CVSConfigException, RemoteException
    {
        CVSFileProfile cvsfp = null;
        HttpSession session = null;
        try
        {
            session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            cvsfp = (CVSFileProfile) sessionMgr
                    .getAttribute(CVSConfigConstants.CVS_FILE_PROFILE_KEY);
            sessionMgr.setAttribute(CVSConfigConstants.CVS_FILE_PROFILE_KEY,
                    null);

            String projectId, moduleId;
            ProjectImpl project = null;
            CVSModule module = null;

            CVSServerManagerLocal cvsManager = new CVSServerManagerLocal();
            moduleId = p_request.getParameter("servers");
            module = cvsManager.getModule(Long.parseLong(moduleId));
            long companyId = CompanyWrapper.getCurrentCompanyIdAsLong();

            String fileExt = "", fp = "";
            fileExt = p_request.getParameter("fileext");
            fp = p_request.getParameter("fp");

            if (fp != null && !fp.trim().equals("-1"))
            {
                cvsfp.setCompanyId(companyId);
                cvsfp.setSourceLocale(p_request.getParameter("srcLocales"));
                cvsfp.setModule(module);
                cvsfp.setFileExt(fileExt);
                cvsfp.setFileProfile((FileProfileImpl) ServerProxy
                        .getFileProfilePersistenceManager().getFileProfileById(
                                Long.parseLong(fp), false));

                manager.update(cvsfp);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

    private void remove(HttpServletRequest p_request)
            throws CVSConfigException, RemoteException
    {
        try
        {
            HttpSession session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            ResourceBundle bundle = getBundle(session);

            CVSFileProfileManagerLocal manager = new CVSFileProfileManagerLocal();
            long id = Long.parseLong(p_request.getParameter("id"));
            manager.remove(id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

    private void setSearchInfo(HttpSession p_session,
            SessionManager p_sessionMgr) throws ProjectHandlerException,
            RemoteException, GeneralException, NamingException
    {
        // now get the projects.
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        User user = (User) p_sessionMgr.getAttribute(WebAppConstants.USER);

        List projectInfos = ServerProxy.getProjectHandler()
                .getProjectInfosByUser(user.getUserId());

        if (projectInfos != null)
        {
            if (projectInfos.size() > 0)
            {
                ProjectComparator pc = new ProjectComparator(uiLocale);
                SortUtil.sort(projectInfos, pc);
            }
            p_sessionMgr.setAttribute("projectInfos", projectInfos);
        }

        CVSServerManagerLocal manager = new CVSServerManagerLocal();
        ArrayList<CVSServer> servers = (ArrayList<CVSServer>) manager
                .getAllServer();
        SortUtil.sort(servers, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((CVSServer) o1).getName().compareToIgnoreCase(
                        ((CVSServer) o2).getName());
            }
        });
        p_sessionMgr.setAttribute("cvsservers", servers);

        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector sources = localeMgr.getAllSourceLocales();
        SortUtil.sort(sources, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                        .compareToIgnoreCase(
                                ((GlobalSightLocale) o2)
                                        .getDisplayName(Locale.US));
            }
        });
        p_sessionMgr.setAttribute("sourceLocales", sources);

    }

}
