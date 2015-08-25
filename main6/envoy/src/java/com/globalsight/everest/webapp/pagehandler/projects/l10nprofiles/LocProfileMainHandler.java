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
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocProfileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.log.OperationLog;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

/**
 * LocProfileMainHandler, A page handler to produce the entry page(index.jsp)
 * for L10nProfile management.
 */
public class LocProfileMainHandler extends PageHandler implements
        LocProfileStateConstants
{

    private static int numPerPage = 20;
    long wfStatePostProfileId = -1;
    String m_userId;
    // Category for log4j logging.
    private static final Logger CATEGORY = Logger
            .getLogger(LocProfileMainHandler.class.getName());

    /**
     * Invokes this PageHandler
     * <p>
     * 
     * @param p_pageDescriptor
     *            the page descriptor
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
        HttpSession session = p_request.getSession(false);
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        String action = p_request.getParameter("action");
        if ("save".equals(action))
        {
            createOrModifyL10nProfile(p_request);
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
            return;
        }
        else if ("saveDup".equals(action))
        {
            duplicateProfile(p_request);
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
            return;
        }
        else if ("remove".equals(action))
        {
            doRemove(p_request, session);
        }
        else if ("ajax".equals(action))
        {
            String message = checkPreReqData(p_request, session);
            p_response.setContentType("text/html;charset=UTF-8");
            p_response.getWriter().write(message);
            return;
        }
        try
        {
            dataForTable(p_request, session);
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

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void createOrModifyL10nProfile(HttpServletRequest p_request)
            throws IOException
    {
        if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
            return;

        if (p_request.getParameter("Edit") != null)
        {
            modifyL10nProfile(p_request, getBasicL10NProfiles(p_request));
        }
        else
        {// new L10nProfiles
            createL10nProfile(getBasicL10NProfiles(p_request));
        }
    }

    private void duplicateProfile(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
            return;

        HttpSession session = p_request.getSession();
        String lpId = (String) p_request.getParameter("DupLocProfile");
        String list = (String) p_request.getParameter("localePairs");
        String name = (String) p_request.getParameter("nameTF").trim();
        try
        {
            ArrayList<LocalePair> alist = new ArrayList<LocalePair>();
            StringTokenizer st = new StringTokenizer(list, ",");
            while (st.hasMoreTokens())
            {
                String id = st.nextToken();
                alist.add(ServerProxy.getLocaleManager().getLocalePairById(
                        Long.parseLong(id)));
            }

            LocProfileHandlerHelper.duplicateL10nProfile(Long.parseLong(lpId),
                    alist, name, getBundle(session));
            for (LocalePair localePair : alist)
            {
                String importWorkflowTemplateName = generateName(name,localePair);
                OperationLog.log(m_userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_WORKFLOW,
                        importWorkflowTemplateName);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("The exception is " + e);
            throw new EnvoyServletException(e);
        }
    }

    private String generateName(String name, LocalePair localePair)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("_");
        sb.append(localePair.getSource().toString());
        sb.append("_");
        sb.append(localePair.getTarget().toString());
        return sb.toString();
    }

    private void doRemove(HttpServletRequest p_request, HttpSession p_session)
            throws EnvoyServletException, IOException
    {
        if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
            return;
        try
        {
            String id = (String) p_request.getParameter(RADIO_BUTTON);
            BasicL10nProfile locprofile = (BasicL10nProfile) LocProfileHandlerHelper
                    .getL10nProfile(Long.parseLong(id));
            String deps = LocProfileHandlerHelper.checkForDependencies(
                    locprofile, PageHandler.getBundle(p_session));
            boolean isActive = locprofile.getIsActive();
            if (deps == null)
            {
                if (isActive)
                {
                    LocProfileHandlerHelper.removeL10nProfile(locprofile);
                    OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                            OperationLog.COMPONET_L10N_PROFILE,
                            locprofile.getName());
                }
            }
            else
            {
                SessionManager sessionMgr = (SessionManager) p_session
                        .getAttribute(WebAppConstants.SESSION_MANAGER);
                sessionMgr.setAttribute(DEPENDENCIES, deps);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private BasicL10nProfile getBasicL10NProfiles(HttpServletRequest p_request)
            throws EnvoyServletException, IOException
    {
        String value;
        String name = (String) p_request.getParameter("LocProfileName");
        value = (String) p_request.getParameter("LocProfileProjectId");
        long projectId = Long.parseLong(value);

        value = (String) p_request.getParameter("locTMProfileId");
        long tmProfileId = Long.parseLong(value);

        value = (String) p_request.getParameter("JobPriority");
        int priority = Integer.parseInt(value);

        value = (String) p_request.getParameter("SourceLocaleId");
        long sourceLocaleId = Long.parseLong(value);
        
         value = p_request.getParameter("wfStatePostProfileId");
        if (StringUtil.isNotEmpty(value))
        {
            wfStatePostProfileId = Long.parseLong(value);
        }

        int TMChoice = -1;
        value = (String) p_request.getParameter("LocProfileTMUsageId");
        int TMEditType = Integer.parseInt(value);
        switch (TMEditType)
        {
            case 0: // Use TM -- No
                TMChoice = L10nProfile.NO_TM;
                break;
            case 1: // Use TM -- Deny edit of ICE and 100% lock segments
            case 2: // Use TM -- Allow edit of ICE and 100% lock segments
                TMChoice = L10nProfile.REGULAR_TM_WITH_PAGE_TM;
                break;
            default:
                break;
        }

        boolean automaticDispatch = false;
        value = (String) p_request.getParameter("AutomaticDispatch");
        automaticDispatch = Boolean.parseBoolean(value);

        // load the fields that do not need a value
        String description = (String) p_request
                .getParameter("LocProfileDescription");

        boolean runSQLScript;
        String SQLScript = null;
        if (p_request.getParameter("LocProfileSQLScript") == null)
        {
            runSQLScript = false;
        }
        else
        {
            runSQLScript = true;
            SQLScript = (String) p_request.getParameter("LocProfileSQLScript");
            if (SQLScript.length() == 0)
            {
                runSQLScript = false;
            }
        }

        // determine whether to create or modify BasicL10nProfile
        BasicL10nProfile locprofile = new BasicL10nProfile(name);
        // fill in user inputs
        locprofile.setSourceLocale(LocProfileHandlerHelper
                .getLocaleById(sourceLocaleId));
        locprofile.setDescription(description);
        locprofile.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));
        Project project = null;
        try
        {
            // set the project - in case it was changed.
            // the id will be set too.
            project = ServerProxy.getProjectHandler().getProjectById(projectId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to find the project associated with "
                    + projectId);
        }
        locprofile.setProject(project);
        locprofile.setAutomaticDispatch(automaticDispatch);
        locprofile.setRunScriptAtJobCreation(runSQLScript);
        locprofile.setJobCreationScriptName(SQLScript);
        locprofile.setTmChoice(TMChoice);
        locprofile.setTMEditType(TMEditType);
        locprofile.setPriority(priority);
        locprofile.setWfStatePostId(wfStatePostProfileId);
        ArrayList<String[]> workflowIds = readyWorkflowIds(p_request);
        try
        {
            for (String[] workflow : workflowIds)
            {
                WorkflowTemplateInfo workflowTemplateInfo = ServerProxy
                        .getProjectHandler().getWorkflowTemplateInfoById(
                                Long.parseLong(workflow[0]));
                workflowTemplateInfo
                        .setMtProfileId(Long.parseLong(workflow[1]));
                locprofile.addWorkflowTemplateInfo(workflowTemplateInfo);
            }

            TranslationMemoryProfile tmProfile = ServerProxy
                    .getProjectHandler().getTMProfileById(tmProfileId, false);
            locprofile.addTMProfile(tmProfile);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return locprofile;
    }

    private ArrayList<String[]> readyWorkflowIds(HttpServletRequest p_request)
    {
        Enumeration parameterNames = p_request.getParameterNames();
        ArrayList<String[]> workflowIds = new ArrayList<String[]>();
        while (parameterNames.hasMoreElements())
        {
            String[] workflowId;
            String parameterName = (String) parameterNames.nextElement();
            if (parameterName.trim().startsWith("TargetLocaleId_"))
            {
                workflowId = p_request.getParameterValues(parameterName);
                if (workflowId.length < 2 || workflowId[0].equals("-1"))
                    continue;
                workflowIds.add(workflowId);
            }
        }
        return workflowIds;
    }

    private Vector<WorkflowInfos> getWorkflowInfos(HttpServletRequest p_request)
    {
        Vector<WorkflowInfos> workflowInfos = new Vector<WorkflowInfos>();
        Enumeration parameterNames = p_request.getParameterNames();
        while (parameterNames.hasMoreElements())
        {
            String workflowId;
            String mtProfileId;
            String targetLocaleId;
            GlobalSightLocale target = null;
            String parameterName = (String) parameterNames.nextElement();
            long locProfileId = Long.parseLong(p_request
                    .getParameter("EditLocProfileId"));
            if (parameterName.trim().startsWith("TargetLocaleId_"))
            {
                String[] ids = p_request.getParameterValues(parameterName);
                if (ids.length < 2 || ids[0].equals("-1"))
                    continue;
                workflowId = ids[0];
                mtProfileId = ids[1];
                targetLocaleId = parameterName.substring(15);
                target = (GlobalSightLocale) LocProfileHandlerHelper
                        .getLocaleById(Long.parseLong(targetLocaleId));
                workflowInfos.add(new WorkflowInfos(locProfileId, Long
                        .parseLong(workflowId), Long.parseLong(mtProfileId),
                        true, target));

            }
        }
        return workflowInfos;
    }

    private void modifyL10nProfile(HttpServletRequest p_request,
            BasicL10nProfile locprofile)
    {
        long originalLocId = Long.parseLong(p_request
                .getParameter("EditLocProfileId"));
        LocProfileHandlerHelper.modifyL10nProfile(locprofile,
                getWorkflowInfos(p_request), originalLocId);
        OperationLog.log(m_userId, OperationLog.EVENT_EDIT,
                OperationLog.COMPONET_L10N_PROFILE, locprofile.getName());
    }

    private void createL10nProfile(BasicL10nProfile locprofile)
    {
        LocProfileHandlerHelper.addL10nProfile(locprofile);
        OperationLog.log(m_userId, OperationLog.EVENT_ADD,
                OperationLog.COMPONET_L10N_PROFILE, locprofile.getName());
    }

    /**
     * Before being able to create a Rate, certain objects must exist. Check
     * that here.
     */
    private String checkPreReqData(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        SessionManager sessMgr = (SessionManager) p_request.getSession()
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessMgr.getAttribute(WebAppConstants.USER);
        List allProjects = LocProfileHandlerHelper
                .getAllProjectNamesForManagedUser(user);
        List allSrcLocales = LocProfileHandlerHelper
                .getAllSourceLocales(uiLocale);

        if (allProjects == null || allProjects.size() < 1
                || allSrcLocales == null || allSrcLocales.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            boolean addcomma = false;
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            if (allProjects == null || allProjects.size() < 1)
            {
                message.append(bundle.getString("lb_projects"));
                addcomma = true;
            }
            if (allSrcLocales == null || allSrcLocales.size() < 1)
            {
                if (addcomma)
                    message.append(", ");
                message.append(bundle.getString("lb_currency"));
            }
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            return message.toString();
        }
        else
            return "nomessage";
    }

    /**
     * Get list of all profiles, sorted appropriately
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        Vector locprofiles = LocProfileHandlerHelper.getAllL10nProfilesForGUI(
                getFilterParameters(p_request), uiLocale);
        setNumberPerPage(p_request);

        setTableNavigation(p_request, p_session, locprofiles,
                new LocProfileComparator(uiLocale), numPerPage,
                LocProfileStateConstants.LOCPROFILE_LIST,
                LocProfileStateConstants.LOCPROFILE_KEY);
    }

    private void setNumberPerPage(HttpServletRequest req)
    {
        String pageSize = (String) req.getParameter("numOfPageSize");
        if (!StringUtil.isEmpty(pageSize))
        {
            try
            {
                numPerPage = Integer.parseInt(pageSize);
            }
            catch (Exception e)
            {
                numPerPage = Integer.MAX_VALUE;
            }
        }
    }

    private String[] getFilterParameters(HttpServletRequest p_request)
    {
        String action = p_request.getParameter("action");
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String name = p_request.getParameter("L10nProfilesNameFilter");
        String company = p_request
                .getParameter("L10nProfilesCompanyNameFilter");
        String tmp = p_request.getParameter("L10nProfilesTMPFilter");
        String project = p_request.getParameter("L10nProfilesProjectFilter");

        if (!FILTER_SEARCH.equals(action)
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            name = (String) sessionMgr.getAttribute("L10nProfilesNameFilter");
            company = (String) sessionMgr
                    .getAttribute("L10nProfilesCompanyNameFilter");
            tmp = (String) sessionMgr.getAttribute("L10nProfilesTMPFilter");
            project = (String) sessionMgr
                    .getAttribute("L10nProfilesProjectFilter");
        }
        if (FILTER_SEARCH.equals(action))
        {
            // Go to page #1 if current action is filter searching.
            sessionMgr.setAttribute(LOCPROFILE_KEY
                    + TableConstants.LAST_PAGE_NUM, Integer.valueOf(1));
        }
        name = name == null ? "" : name;
        company = company == null ? "" : company;
        tmp = tmp == null ? "" : tmp;
        project = project == null ? "" : project;
        sessionMgr.setAttribute("L10nProfilesNameFilter", name);
        sessionMgr.setAttribute("L10nProfilesCompanyNameFilter", company);
        sessionMgr.setAttribute("L10nProfilesTMPFilter", tmp);
        sessionMgr.setAttribute("L10nProfilesProjectFilter", project);
        String[] filterParam =
        { name, company, tmp, project };
        return filterParam;
    }
}
