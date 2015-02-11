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

import CoffeeTable.Grid.GridData;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.foundation.ContainerRoleImpl;

import com.globalsight.everest.jobhandler.Job;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;

import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;

import com.globalsight.util.date.DateHelper;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import com.globalsight.log.GlobalSightCategory;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class ModifyWorkflowHandler
extends PageHandler
{
    private static final GlobalSightCategory s_logger =
        (GlobalSightCategory)GlobalSightCategory.
        getLogger(ModifyWorkflowHandler.class.getName());
    /**
     * Invokes this EntryPageHandler object.
     * <p>
     * @param thePageDescriptor the description of the page to be produced
     * @param theRequest the original request sent from the browser
     * @param theResponse original response object
     * @param context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        // place the wf id on the session so the applet can receive it.
        SessionManager sessionMgr =
            (SessionManager)p_request.getSession(false).
            getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(JobManagementHandler.WF_ID,
                                p_request.getParameter(JobManagementHandler.
                                                       WF_ID));
        sessionMgr.setAttribute(JobManagementHandler.JOB_ID,
                                new Long(p_request.getParameter(JobManagementHandler.
                                                                JOB_ID)));

        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }

    /**
     * Invokes this EntryPageHandler object.  This is used for applets.
     *
     * @param p_isGet - Determines whether the request is a get or post.
     * @param thePageDescriptor the description of the page to be produced
     * @param theRequest the original request sent from the browser
     * @param theResponse the original response object
     * @param context the Servlet context
     * @return A vector of serializable objects to be passed to applet.
     */
    public Vector invokePageHandlerForApplet(boolean p_isDoGet,
                                             WebPageDescriptor p_thePageDescriptor,
                                             HttpServletRequest p_theRequest,
                                             HttpServletResponse p_theResponse,
                                             ServletContext p_context,
                                             HttpSession p_session)
    throws ServletException, IOException, EnvoyServletException
    {
        Vector retVal = null;
        if (p_isDoGet)
        {
            retVal = doGet(p_session);
        }
        else
        {
            retVal = doPost(p_theRequest, p_session);
        }
        return retVal;
    }

    private Vector doGet(HttpSession p_session)
    throws EnvoyServletException
    {
        // create the resource java bean
        ResourceBundle bundle = getBundle(p_session);
        String[] header =
        {
            bundle.getString("lb_activity"),
            bundle.getString("lb_person_role"),
            bundle.getString("lb_time_accept"),
            bundle.getString("lb_time_complete")
        };
        String[] labels =
        {
            bundle.getString("applet_new"),
            bundle.getString("applet_newb"),
            bundle.getString("applet_modify"), 
            bundle.getString("applet_modifyb"),
            bundle.getString("applet_modifyx"), 
            bundle.getString("applet_remove"),
            bundle.getString("applet_removeb"), 
            bundle.getString("applet_removex"),
            bundle.getString("applet_cancel"), 
            bundle.getString("applet_cancelb"),
            bundle.getString("applet_save"), 
            bundle.getString("applet_saveb"),
            bundle.getString("applet_savex"), 
            bundle.getString("lb_edit_workflow_instance")
        };                
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        String wfId = (String)sessionMgr.getAttribute(JobManagementHandler.
                                                      WF_ID);

        Vector objs = new Vector();
        boolean isReady = false;
        GridData gridData = new GridData(0, header.length);
        if (wfId != null)
        {
            Workflow workflow = WorkflowHandlerHelper.
                getWorkflowById(p_session.getId(), Long.parseLong(wfId));
            isReady = workflow.READY_TO_BE_DISPATCHED.equals(workflow.getState());
            WorkflowInstance instance = workflow.getIflowInstance();

            Vector workflowTasks = instance.getWorkflowInstanceTasks();
            gridData = new GridData(workflowTasks.size(), 7);
            Collections.sort(workflowTasks, 
                             new WorkflowTaskInstanceComparator());
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("THE WF " + wfId);
            }
            String dAbbr = bundle.getString("lb_abbreviation_day");
            String hAbbr = bundle.getString("lb_abbreviation_hour");
            String mAbbr = bundle.getString("lb_abbreviation_minute");
            for (int i = 0 ; i < workflowTasks.size() ; i++)
            {
                WorkflowTaskInstance task = 
                    (WorkflowTaskInstance)workflowTasks.elementAt(i);
                // TomyD -- temp fix to only display the first role
                String[] roles = task.getRoles();
                Vector row = new Vector();
                task.getSequence();
                row.addElement(task.getActivity());
                row.addElement(roles[0]);
                row.addElement(DateHelper.daysHoursMinutes(task.getAcceptTime(),
                                                           dAbbr, hAbbr, mAbbr));
                row.addElement(DateHelper.daysHoursMinutes(task.getCompletedTime(),
                                                           dAbbr, hAbbr, mAbbr));
                row.addElement("");
                row.addElement(task);
                row.addElement(new Integer(task.getTaskState()));
                gridData.setRowData(i + 1, row);
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("The row is " + row + " TASK STATE " +
                                   task.getTaskState());
                    s_logger.debug("The initiator of the task is " + 
                                   task.getInitiator() +
                                   ":: The role is " + task.getRolesAsString());
                }
            }
        }
        objs.addElement(header);
        objs.addElement(labels);
        objs.addElement(gridData);
        objs.addElement(getDataForDialog(p_session));
        objs.addElement(new Boolean(isReady));
        return objs;
    }

    private Vector doPost(HttpServletRequest p_request,
                          HttpSession p_session)
    throws EnvoyServletException, IOException
    {
        Vector outData = null;
        SessionManager sessionMgr = (SessionManager)
                                    p_session.getAttribute(SESSION_MANAGER);    
        String wfId = (String)sessionMgr.getAttribute(
                                                     JobManagementHandler.WF_ID);
        String jobId = (String)sessionMgr.getAttribute(
                                                      JobManagementHandler.JOB_ID);
        Workflow workflow = WorkflowHandlerHelper.getWorkflowById(
                                                                 p_session.getId(), Long.parseLong(wfId));
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("THE JOB ID IN THE SESSION IS " + jobId);
        }
        Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(jobId));
        try
        {
            ObjectInputStream inputFromApplet = 
                new ObjectInputStream(p_request.getInputStream());
            Vector inData = (Vector)inputFromApplet.readObject();
            if (inData != null) // if this is null the command is cancel.
            {
                String command = (String)inData.elementAt(0);
                // return data in order to populate user or role.
                if (command.equals("user") || command.equals("role"))
                {
                    outData = new Vector();
                    outData.addElement(getDataForRole((String)
                                                      inData.elementAt(1),
                                                      command.equals("user"),
                                                      job.getL10nProfileId(),
                                                      workflow.getTargetLocale()));
                }
                if (command.equals("save"))
                {   // save the modified workflows.
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Saving in progress: "
                                         + "inData=" 
                                         + inData.toString()); 
                    }
                    modifyWorkflow(p_session, 
                                   workflow.getIflowInstance(), 
                                   (Vector)inData.elementAt(1));
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            throw new EnvoyServletException(
                                           EnvoyServletException.EX_GENERAL, ex);

        }
        return outData;
    }

    private void modifyWorkflow(HttpSession p_session, 
                                WorkflowInstance preModifyInstance,
                                Vector updatedTaskList)
    throws EnvoyServletException 
    {
        SessionManager sessionMgr = (SessionManager)
                                    p_session.getAttribute(SESSION_MANAGER);
        User user = (User)sessionMgr.getAttribute(USER);
        WorkflowInstance postModifyInstance =
            new WorkflowInstance(preModifyInstance.getId(), 
                                 preModifyInstance.getName(),
                                 preModifyInstance.getDescription(), 
                                 updatedTaskList);
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("modifyWorkflow: "
                             + " p_session.getId()=" 
                             + (p_session.getId()!=null?p_session.getId():"null")
                             + " postModifyInstance=" 
                             + (postModifyInstance!=null?
                                postModifyInstance.toString():"null")
                             + " user.getUserId()=" 
                             + (user.getUserId()!=null?user.getUserId():"null")
                            );
        }
        WorkflowHandlerHelper.modifyWorkflow(p_session.getId(), 
                                             postModifyInstance, user.getUserId(), null);
    }

    private GridData getDataForRole(String p_activityName, 
                                    boolean p_isUser,
                                    long p_l10nProfileId, GlobalSightLocale p_targetLocale)
    throws EnvoyServletException
    {
        // obtain the l10nprofile for source locale.
        L10nProfile l10nProfile = 
            LocProfileHandlerHelper.getL10nProfile(p_l10nProfileId);
        GridData gridData = null;
        if (p_isUser)
        {
            // obtain the roles to be turned into griddata.
            Collection usersCollection = 
                LocProfileHandlerHelper.getUserRoles(p_activityName,
                                                     l10nProfile.getSourceLocale().toString(), 
                                                     p_targetLocale.toString());
            gridData = new GridData(usersCollection == null ? 0 
                                    : usersCollection.size(), 3);
            if (usersCollection != null)
            {
                Vector users = vectorizedCollection(usersCollection);
                for (int i=0; i<users.size(); i++)
                {
                    Vector row = new Vector();
                    UserRoleImpl userRole = (UserRoleImpl)users.get(i);
                    User user = LocProfileHandlerHelper.getUser(
                                                               userRole.getUser());
                    row.addElement(user.getFirstName());
                    row.addElement(user.getLastName());
                    row.addElement(user.getUserId());                            
                    row.addElement(userRole.getName());
                    gridData.setRowHeaderData(i+1, userRole.getName());
                    gridData.setRowData(i+1, row);
                }
            }
        }
        else
        {
            Collection rolesCollection = 
                LocProfileHandlerHelper.getContainerRoles(
                                                         p_activityName,
                                                         l10nProfile.getSourceLocale().toString(), 
                                                         p_targetLocale.toString());
            gridData = new GridData(rolesCollection == null ? 0 
                                    : rolesCollection.size(), 3);
            if (rolesCollection != null)
            {
                Vector roles = vectorizedCollection(rolesCollection);
                for (int i=0; i<roles.size(); i++)
                {
                    Vector row = new Vector();
                    ContainerRoleImpl containerRole = 
                        (ContainerRoleImpl)roles.get(i);
                    row.addElement(containerRole.getName());
                    row.addElement("");
                    row.addElement("");
                    gridData.setRowData(i+1, row);
                }
            }
        }
        return gridData;
    }

    private Vector getDataForDialog(HttpSession p_session)
    throws EnvoyServletException
    {
        ResourceBundle bundle = getBundle(p_session);
	Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
        String[] dialogLabels =
        {
            bundle.getString("msg_new_task"),                                    //0
            bundle.getString("msg_edit_activity"),                             //1
            bundle.getString("lb_activity_type") + bundle.getString("lb_colon"), //2
            bundle.getString("lb_time_complete") + bundle.getString("lb_colon"), //3
            bundle.getString("lb_participant") + bundle.getString("lb_colon"),   //4
            bundle.getString("lb_select_user"),                                  //5
            bundle.getString("lb_all_qualified_users"),                          //6
            bundle.getString("lb_days"),                                         //7
            bundle.getString("lb_choose"),                                       //8
            bundle.getString("lb_time_accept") + bundle.getString("lb_colon"),   //9
            bundle.getString("lb_abbreviation_day"),                             //10
            bundle.getString("lb_abbreviation_hour"),                            //11
            bundle.getString("lb_abbreviation_minute"),                           //12
            bundle.getString("lb_rate") + bundle.getString("lb_colon"),           //13
            bundle.getString("lb_choose")                                         //14
        };
        String[] headerLabels =
        {
            bundle.getString("lb_first_name"),
            bundle.getString("lb_last_name"),
            bundle.getString("lb_user_name")
        };
        String[] buttonLabels =
        {
            bundle.getString("applet_save"),
            bundle.getString("applet_saveb"),
            bundle.getString("applet_savex"),
            bundle.getString("applet_cancel"),
            bundle.getString("applet_cancelb")
        };                                
        Vector dialogData = new Vector();
        dialogData.addElement(dialogLabels);       
        dialogData.addElement(headerLabels);       
        dialogData.addElement(buttonLabels);       
        dialogData.addElement(LocProfileHandlerHelper.getAllActivities(uiLocale));
        dialogData.addElement(messages(bundle));                          
        
        if (isCostingEnabled())
        {
            SessionManager sessionMgr = (SessionManager)
                                        p_session.getAttribute(SESSION_MANAGER);    
            String wfId = (String)sessionMgr.getAttribute(
                                                         JobManagementHandler.WF_ID);
            String jobId = (String)sessionMgr.getAttribute(
                                                          JobManagementHandler.JOB_ID);

            Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(jobId));
            Workflow workflow = WorkflowHandlerHelper.
                getWorkflowById(p_session.getId(), Long.parseLong(wfId));
            
            dialogData.addElement(LocProfileHandlerHelper.
                                  getRatesForLocale(job.getSourceLocale(), workflow.getTargetLocale()));
        }
        return dialogData;
    }

    private String[] messages(ResourceBundle p_bundle)
    {
        String[] array =
        {
            p_bundle.getString("msg_loc_profiles_time_restrictions")
        };
        return array;
    }

    private boolean isCostingEnabled()
    {
        boolean costingEnabled = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            costingEnabled = sc.getBooleanParameter(
                SystemConfigParamNames.COSTING_ENABLED);
        }
        catch (GeneralException ge)
        {
            // assumes costing is disabled.
        }
        return costingEnabled;
    }
}
