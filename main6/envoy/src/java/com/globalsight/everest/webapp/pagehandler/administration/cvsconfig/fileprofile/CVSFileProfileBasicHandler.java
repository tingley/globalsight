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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.fileprofile.FileProfileExtension;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.cvsconfig.CVSFileProfile;
import com.globalsight.everest.cvsconfig.CVSFileProfileManagerLocal;
import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class CVSFileProfileBasicHandler extends PageHandler implements
        CVSConfigConstants
{
    private CVSServerManagerLocal manager = new CVSServerManagerLocal();

    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
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
        String action = p_request.getParameter("action");
        String projectId = "-1";

        try
        {
            if (action.equals(CVSConfigConstants.UPDATE))
            {
                String id = (String) p_request.getParameter("id");
                CVSFileProfileManagerLocal cvsManager = new CVSFileProfileManagerLocal();
                CVSFileProfile cvsfp = cvsManager.getCVSFileProfile(Long
                        .parseLong(id));
                projectId = String.valueOf(cvsfp.getProject().getId());
                sessionMgr.setAttribute(
                        CVSConfigConstants.CVS_FILE_PROFILE_KEY, cvsfp);
                p_request.setAttribute("edit", "true");
            }
            else if (action.equals("projectSelect"))
            {
                projectId = p_request.getParameter("projects");
                sessionMgr.setAttribute("projectSelect", projectId);
            }
            setFileExtensions(sessionMgr, projectId);

            sessionMgr.setAttribute("remainingLocales",
                    VendorHelper.getRemainingLocales(new ArrayList<Object>()));

            prepareListOfProjects(session, sessionMgr);

            setProjectOrDivisionLabel(sessionMgr, session);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Set the project label and JavaScript warning message based on the
     * property set in envoy.properties.
     */
    private void setProjectOrDivisionLabel(SessionManager p_sessionMgr,
            HttpSession p_session) throws Exception
    {
        // Set the label for project (Project vs. Division)
        SystemConfiguration sc = SystemConfiguration.getInstance();
        boolean isDivision = sc
                .getBooleanParameter(SystemConfigParamNames.IS_DELL);
        ResourceBundle bundle = PageHandler.getBundle(p_session);
        String projectLabel = isDivision ? "lb_division" : "lb_project";
        p_sessionMgr.setAttribute(WebAppConstants.PROJECT_LABEL,
                bundle.getString(projectLabel));

        String projectJsMsg = isDivision ? "jsmsg_select_division"
                : "jsmsg_select_project";
        p_sessionMgr.setAttribute(WebAppConstants.PROJECT_JS_MSG,
                bundle.getString(projectJsMsg));
    }

    private void prepareListOfProjects(HttpSession p_session,
            SessionManager p_sessionMgr) throws Exception
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

    private void setFileExtensions(SessionManager p_sessionMgr,
            String p_projectId)
    {
        CVSFileProfileManagerLocal fpManager = new CVSFileProfileManagerLocal();
        ArrayList<FileProfileExtension> tmp = (ArrayList<FileProfileExtension>) fpManager
                .getFileExtensions(p_projectId);
        TreeMap<String, ArrayList<FileProfileImpl>> exts = new TreeMap<String, ArrayList<FileProfileImpl>>();
        String extName = "";
        ArrayList<FileProfileImpl> fps = null;
        for (FileProfileExtension f : tmp)
        {
            extName = f.getExtension().getName();
            if (exts.containsKey(extName))
            {
                fps = exts.get(extName);
                fps.add(f.getFileProfile());
            }
            else
            {
                fps = new ArrayList<FileProfileImpl>();
                fps.add(f.getFileProfile());
            }
            SortUtil.sort(fps, new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    return ((FileProfileImpl) o1).getName()
                            .compareToIgnoreCase(
                                    ((FileProfileImpl) o2).getName());
                }
            });
            exts.put(extName, fps);
        }
        p_sessionMgr.setAttribute("fileExtensions", exts);

    }
}
