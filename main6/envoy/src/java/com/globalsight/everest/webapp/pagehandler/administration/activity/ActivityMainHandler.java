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
package com.globalsight.everest.webapp.pagehandler.administration.activity;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.ActivityComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.dependencychecking.ActivityDependencyChecker;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

/**
 */
public class ActivityMainHandler extends PageHandler implements
        ActivityConstants
{
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
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (ActivityConstants.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session,
                        ActivityConstants.ACTIVITY_KEY);
            }
            else if (ActivityConstants.CREATE.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request,
                        FormUtil.Forms.NEW_ACTIVITY_TYPE))
                {
                    createActivity(p_request);
                }
            }
            else if (ActivityConstants.EDIT.equals(action))
            {
                modifyActivity(session, p_request);
                clearSessionExceptTableInfo(session,
                        ActivityConstants.ACTIVITY_KEY);
            }
            else if (ActivityConstants.REMOVE.equals(action))
            {
                removeActivity(session, p_request);
            }

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
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        Vector activities = getAllActivities();

        // Filter activities by company name
        filterActivitiesByCompanyName(p_request, p_session, activities);
        // Filter activities by activity name
        filterActivitiesByActivityName(p_request, p_session, activities);

        // Get the number per page
        int numPerPage = getNumPerPage(p_request, p_session);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, activities,
                new ActivityComparator(uiLocale), numPerPage, ACTIVITY_LIST,
                ACTIVITY_KEY);
    }

    /**
     * Create an Activity.
     */
    private Activity createActivity(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException
    {
        // create the activity.
        Activity act = new Activity();
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String actDisplayName = p_request.getParameter(ActivityConstants.NAME);
        act.setDisplayName(actDisplayName);
        act.setName(actDisplayName + "_" + companyId);
        act.setDescription(p_request.getParameter(ActivityConstants.DESC));
        act.setType(getType(p_request));
        act.setIsEditable(getIsEditable(p_request));
        act.setCompanyId(Long.parseLong(companyId));
        act.setUseType(p_request.getParameter("useTypeField"));
        act.setQaChecks("on".equalsIgnoreCase(p_request
                .getParameter("qaChecks")));
        act.setAutoCompleteActivity("true".equalsIgnoreCase(p_request
                .getParameter("isAutoCompleteActivity")));
        act.setAfterJobCreation(p_request.getParameter("afterJobCreation"));
        act.setAfterJobDispatch(p_request.getParameter("afterJobDispatch"));
        act.setAfterActivityStart(p_request.getParameter("afterActivityStart"));

        String isDitaAct = p_request
                .getParameter(ActivityConstants.IS_DITA_QA_CHECK_ACTIVITY);
        act.setRunDitaQAChecks(false);
        if ("on".equalsIgnoreCase(isDitaAct))
        {
            act.setRunDitaQAChecks(true);
        }

        return ServerProxy.getJobHandler().createActivity(act);
    }

    private Vector getAllActivities() throws RemoteException, NamingException,
            GeneralException
    {
        return vectorizedCollection(ServerProxy.getJobHandler()
                .getAllActivities());
    }

    private void modifyActivity(HttpSession p_session,
            HttpServletRequest p_request) throws RemoteException,
            NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Activity act = (Activity) sessionMgr
                .getAttribute(ActivityConstants.ACTIVITY);
        act.setDescription(p_request.getParameter(ActivityConstants.DESC));
        act.setType(getType(p_request));
        act.setIsEditable(getIsEditable(p_request));
        act.setAutoCompleteActivity("true".equalsIgnoreCase(p_request
                .getParameter("isAutoCompleteActivity")));
        act.setAfterJobCreation(p_request.getParameter("afterJobCreation"));
        act.setAfterJobDispatch(p_request.getParameter("afterJobDispatch"));
        act.setAfterActivityStart(p_request.getParameter("afterActivityStart"));

        Company company = ServerProxy.getJobHandler()
                .getCompanyById(act.getCompanyId());
        // If "Enable QA Checks" is disabled, ensure its original setting will
        // not be changed.
        if (company.getEnableQAChecks())
        {
            act.setQaChecks(false);
        }
        if ("on".equalsIgnoreCase(p_request.getParameter("qaChecks")))
        {
            act.setQaChecks(true);
        }

        // If "Enable DITA Checks" is disabled, ensure its original setting will
        // not be changed.
        if (company.getEnableDitaChecks())
        {
            act.setRunDitaQAChecks(false);
        }
        if ("on".equalsIgnoreCase(p_request
                .getParameter(ActivityConstants.IS_DITA_QA_CHECK_ACTIVITY)))
        {
            act.setRunDitaQAChecks(true);
        }

        ServerProxy.getJobHandler().modifyActivity(act);
    }

    /**
     * Remove an Activity if there are no dependencies.
     */
    private void removeActivity(HttpSession p_session,
            HttpServletRequest p_request) throws RemoteException,
            NamingException, GeneralException
    {
        String name = (String) p_request.getParameter("name");
        Activity act = (Activity) ServerProxy.getJobHandler().getActivity(name);
        String deps = checkForDependencies(act, p_session);
        if (deps == null)
        {
            ServerProxy.getJobHandler().removeActivity(act);
        }
        else
        {
            SessionManager sessionMgr = (SessionManager) p_session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            sessionMgr.setAttribute(DEPENDENCIES, deps);
        }
    }

    /**
     * Returns the activity type based on the request's information.
     */
    private int getType(HttpServletRequest p_request)
    {
        int type = Activity.TYPE_TRANSLATE;

        String typeStr = new String(
                p_request.getParameter(ActivityConstants.TYPE));
        if (ActivityConstants.REVIEW_NOT_EDITABLE.equals(typeStr)
                || ActivityConstants.REVIEW_EDITABLE.equals(typeStr))
        {
            type = Activity.TYPE_REVIEW;
        }

        return type;
    }

    /**
     * Returns the whether this activity is editable based on the request's
     * information.
     */
    private boolean getIsEditable(HttpServletRequest p_request)
    {
        boolean isEditable = true;
        String typeStr = new String(
                p_request.getParameter(ActivityConstants.TYPE));

        if (typeStr.equals(ActivityConstants.REVIEW_NOT_EDITABLE))
        {
            isEditable = false;
        }
        return isEditable;
    }

    /**
     * Check if any objects have dependencies on this L10nProfile. This should
     * be called BEFORE attempting to remove a Profile.
     * <p>
     * 
     * @param p_activity
     * @param session
     * @return
     * @exception EnvoyServletException
     *                Failed to look for dependencies for the profile. The cause
     *                is indicated by the exception message.
     * @exception RemoteException
     * @exception GeneralException
     */
    private String checkForDependencies(Activity p_activity, HttpSession session)
            throws RemoteException, GeneralException
    {
        ResourceBundle bundle = PageHandler.getBundle(session);
        ActivityDependencyChecker depChecker = new ActivityDependencyChecker();
        Hashtable catDeps = depChecker.categorizeDependencies(p_activity);

        // now convert the hashtable into a Vector of Strings
        StringBuffer deps = new StringBuffer();
        if (catDeps.size() == 0)
            return null;

        deps.append("<span class=\"errorMsg\">");
        Object[] args =
        { bundle.getString("lb_activity_type") };
        deps.append(MessageFormat.format(bundle.getString("msg_dependency"),
                args));

        for (Enumeration e = catDeps.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            deps.append("<p>*** " + bundle.getString(key) + " ***<br>");
            Vector values = (Vector) catDeps.get(key);
            for (int i = 0; i < values.size(); i++)
            {
                deps.append((String) values.get(i));
                deps.append("<br>");
            }
        }
        deps.append("</span>");
        return deps.toString();
    }

    private int getNumPerPage(HttpServletRequest p_request,
            HttpSession p_session)
    {
        int result = 10;

        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String activityNumPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(activityNumPerPage))
        {
            activityNumPerPage = (String) sessionManager
                    .getAttribute("activityNumPerPage");
        }

        if (activityNumPerPage != null)
        {
            sessionManager.setAttribute("activityNumPerPage",
                    activityNumPerPage.trim());
            if ("all".equalsIgnoreCase(activityNumPerPage))
            {
                result = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    result = Integer.parseInt(activityNumPerPage);
                }
                catch (NumberFormatException ignore)
                {
                    result = 10;
                }
            }
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    private void filterActivitiesByCompanyName(HttpServletRequest p_request,
            HttpSession p_session, Vector p_activities)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String actCompanyFilterValue = p_request
                .getParameter(ActivityConstants.FILTER_COMPANY_NAME);
        if (actCompanyFilterValue == null)
        {
            actCompanyFilterValue = (String) sessionManager
                    .getAttribute(ActivityConstants.FILTER_COMPANY_NAME);
        }
        if (actCompanyFilterValue == null)
        {
            actCompanyFilterValue = "";
        }
        sessionManager.setAttribute(ActivityConstants.FILTER_COMPANY_NAME,
                actCompanyFilterValue.trim());

        if (!StringUtil.isEmpty(actCompanyFilterValue))
        {
            for (Iterator it = p_activities.iterator(); it.hasNext();)
            {
                Activity activity = (Activity) it.next();
                String comName = CompanyWrapper.getCompanyNameById(
                        activity.getCompanyId()).toLowerCase();
                if (comName.indexOf(actCompanyFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void filterActivitiesByActivityName(HttpServletRequest p_request,
            HttpSession p_session, Vector p_activities)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String actNameFilterValue = p_request
                .getParameter(ActivityConstants.FILTER_ACTIVITY_NAME);
        if (actNameFilterValue == null)
        {
            actNameFilterValue = (String) sessionManager
                    .getAttribute(ActivityConstants.FILTER_ACTIVITY_NAME);
        }
        if (actNameFilterValue == null)
        {
            actNameFilterValue = "";
        }
        sessionManager.setAttribute(ActivityConstants.FILTER_ACTIVITY_NAME,
                actNameFilterValue.trim());

        if (!StringUtil.isEmpty(actNameFilterValue))
        {
            for (Iterator it = p_activities.iterator(); it.hasNext();)
            {
                Activity activity = (Activity) it.next();
                if (activity.getDisplayName().toLowerCase()
                        .indexOf(actNameFilterValue.trim().toLowerCase()) == -1)
                {
                    it.remove();
                }
            }
        }
    }
}
