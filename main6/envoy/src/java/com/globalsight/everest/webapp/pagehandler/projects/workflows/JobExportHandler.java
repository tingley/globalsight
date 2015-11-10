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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

// GlobalSight
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

/**
 * This page handler displays the Export page where you choose which workflows
 * you want to export. The actual exporting of the selected workflows is handled
 * by JobExportControlFlowHelpre.java.
 */
public class JobExportHandler extends PageHandler
{
    private static final Logger c_logger = Logger
            .getLogger(JobExportHandler.class.getName());

    public static final String PARAM_DELAY = "delay";

    // constants used for distinguishing primary and secondary radio buttons
    public static final String PRIMARY_PREFIX = "p";
    public static final String SECONDARY_PREFIX = "s";

    // request attributes
    public static final String ATTR_SOURCE_PAGES = "attrSourcePages";
    public static final String ATTR_JOB = "attrJob";

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * 
     * @param p_ageDescriptor
     *            the description of the page to be produced.
     * @param p_request
     *            original request sent from the browser.
     * @param p_response
     *            original response object.
     * @param p_context
     *            the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {

        String pageName = p_pageDescriptor.getPageName();
        NavigationBean baseBean = new NavigationBean(
                JobManagementHandler.EXPORT_BEAN, pageName);
        NavigationBean completeBean = new NavigationBean(
                JobManagementHandler.LOCALIZED_BEAN, pageName);
        NavigationBean exportedBean = new NavigationBean(
                JobManagementHandler.EXPORTED_BEAN, pageName);
        HttpSession session = p_request.getSession(false);

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        p_request.setAttribute(JobManagementHandler.EXPORT_BEAN, baseBean);
        p_request.setAttribute(JobManagementHandler.LOCALIZED_BEAN,
                completeBean);
        p_request
                .setAttribute(JobManagementHandler.EXPORTED_BEAN, exportedBean);

        if (!"true"
                .equals(p_request
                        .getParameter(JobManagementHandler.EXPORT_MULTIPLE_ACTIVITIES_PARAM)))
        {
            Job job = getJobDetailsInfo(p_request, uiLocale);
            p_request.setAttribute(ATTR_JOB, job);

            if ("true"
                    .equals(p_request
                            .getParameter(JobManagementHandler.EXPORT_FOR_UPDATE_PARAM)))
                p_request.setAttribute(ATTR_SOURCE_PAGES, job.getSourcePages());
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    public static List activeWorkflows(Job p_job) throws EnvoyServletException
    {
        List wfs = new ArrayList();
        Iterator it = WorkflowHandlerHelper.getWorkflowsByJobId(
                p_job.getJobId()).iterator();
        while (it.hasNext())
        {
            Workflow wf = (Workflow) it.next();
            String wfState = wf.getState();
            // return only workflows that can be exported
            if (wfState.equals(Workflow.DISPATCHED)
                    || wfState.equals(Workflow.ARCHIVED)
                    || wfState.equals(Workflow.EXPORT_FAILED)
                    || wfState.equals(Workflow.EXPORTED)
                    || wfState.equals(Workflow.LOCALIZED)
                 	|| wfState.equals(Workflow.READY_TO_BE_DISPATCHED))
            {
                wfs.add(wf);
            }
        }
        return wfs;
    }

    /**
     * We get here to display Job or Workflow details, so determine which and
     * obtain the job and populate the appropriate values. in the request.
     * 
     * @param p_request
     * @param p_uiLocale
     * @return
     * @exception EnvoyServletException
     */
    private Job getJobDetailsInfo(HttpServletRequest p_request,
            Locale p_uiLocale) throws EnvoyServletException
    {
        Job job = null;
        String param = p_request.getParameter(JobManagementHandler.JOB_ID);
        if (param != null)
        {
            job = WorkflowHandlerHelper.getJobById(Long.parseLong(param));
        }
        else
        {
            param = p_request.getParameter(JobManagementHandler.WF_ID);
            Workflow workflow = WorkflowHandlerHelper.getWorkflowById(Long
                    .parseLong(param));
            job = workflow.getJob();
        }
        String taskId = p_request.getParameter(WebAppConstants.TASK_ID);
        p_request.setAttribute(WebAppConstants.TASK_ID,taskId);
        
        String taskState = p_request.getParameter(WebAppConstants.TASK_STATE);
        p_request.setAttribute(WebAppConstants.TASK_STATE,taskState);
        p_request.setAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET,
                job.getJobName());

        L10nProfile l10nProfile = job.getL10nProfile();
        p_request.setAttribute(JobManagementHandler.L10NPROFILE_NAME_SCRIPTLET,
                l10nProfile.getName());
        Project project = job.getProject();
        p_request.setAttribute(JobManagementHandler.PROJECT_NAME_SCRIPTLET,
                project.getName());

        User initiator = job.getCreateUser();
        if (initiator == null)
        {
            initiator = project.getProjectManager();
        }

        // initiator
        p_request.setAttribute(JobManagementHandler.JOB_INITIATOR_SCRIPTLET,
                initiator.getUserName());

        GlobalSightLocale sourceLocale = job.getSourceLocale();
        p_request.setAttribute(JobManagementHandler.SRC_LOCALE_SCRIPTLET,
                sourceLocale.getDisplayName(p_uiLocale));
        return job;
    }

    public static String decodeIDFromParam(String p_paramName)
    {
        return p_paramName.substring(p_paramName.indexOf("_") + 1);
    }

    public static boolean isValidState(String p_workflowState)
    {
		return (p_workflowState.equals(Workflow.LOCALIZED)
				|| p_workflowState.equals(Workflow.DISPATCHED)
				|| p_workflowState.equals(Workflow.EXPORT_FAILED)
				|| p_workflowState.equals(Workflow.EXPORTED)
				|| p_workflowState.equals(Workflow.ARCHIVED)
				|| p_workflowState.equals(Workflow.READY_TO_BE_DISPATCHED));
    }

    /**
     * Looks up the known format type for the associated file profile and
     * returns true if it's an MS Office document.
     * 
     * @param p_targetPage
     * @exception Exception
     */
    public static boolean isMicrosoftOffice(TargetPage p_targetPage)
            throws EnvoyServletException
    {
        try
        {
            return SourcePage.isMicrosoftOffice(p_targetPage.getSourcePage());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Looks up the known format type for the associated file profile and
     * returns true if it's a native RTF document.
     * 
     * @param p_targetPage
     * @exception Exception
     */
    public static boolean isNativeRtf(TargetPage p_targetPage)
            throws EnvoyServletException
    {
        try
        {
            return SourcePage.isNativeRtf(p_targetPage.getSourcePage());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Looks up the known format type for the associated file profile and
     * returns true if it's an unextracted file
     * 
     * @param p_targetPage
     * @return
     * @exception Exception
     */
    public static boolean isUnextracted(TargetPage p_targetPage)
            throws EnvoyServletException
    {
        try
        {
            long fileProfileId = p_targetPage.getSourcePage().getRequest()
                    .getDataSourceId();
            KnownFormatType format = SourcePage
                    .getFormatTypeByFpId(fileProfileId);
            if (format.getName().equals("Un-extracted"))
                return true;
            else
                return false;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Override getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @param p_request
     * @param p_response
     * @return The name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return new JobExportControlFlowHelper(p_request, p_response);
    }
}
