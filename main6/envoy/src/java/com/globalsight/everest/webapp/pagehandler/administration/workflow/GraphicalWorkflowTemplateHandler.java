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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

//GlobalSight
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.util.GlobalSightLocale;

/**
 * GraphicalWorkflowTemplateHandler is responsible for displaying the graphical
 * workflow jsp (for workflow template) and providing support for the applet
 * requests.
 */

public class GraphicalWorkflowTemplateHandler extends PageHandler implements
        WorkflowTemplateConstants
{
    String m_userId;
    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    public GraphicalWorkflowTemplateHandler()
    {
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

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
        // first store the info from the first page in session manager.
        storeInfoInSessionManager(p_request);
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Invokes this EntryPageHandler object. This is used for applets.
     * 
     * @param p_isGet
     *            - Determines whether the request is a get or post.
     * @param thePageDescriptor
     *            the description of the page to be produced
     * @param theRequest
     *            the original request sent from the browser
     * @param theResponse
     *            the original response object
     * @param context
     *            the Servlet context
     * @return A vector of serializable objects to be passed to applet.
     */
    public Vector invokePageHandlerForApplet(boolean p_isDoGet,
            WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context, HttpSession p_session)
            throws ServletException, IOException, EnvoyServletException
    {
        Vector retVal = null;
        if (p_isDoGet)
        {
            retVal = getDisplayData(p_theRequest, p_session);
        }
        else
        {
            retVal = postAction(p_theRequest, p_session);
        }
        return retVal;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    // store the info from the first page in the session manager
    private void storeInfoInSessionManager(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = WorkflowTemplateHandlerHelper
                .getSessionManager(p_request);

        // always check for the object (whether it's new or existing)
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WF_TEMPLATE_INFO);
        // only if we're in modify mode
        String wfId = p_request.getParameter(TEMPLATE_ID);
        if (wfId != null)
        {
            // update editable fields
            wfti.setName(p_request.getParameter(NAME_FIELD));
            wfti.setDescription(p_request.getParameter(DESCRIPTION_FIELD));
            wfti.setCodeSet(p_request.getParameter(ENCODING_FIELD));
            wfti.setScorecardShowType(Integer
            		.parseInt(p_request.getParameter(SCORECARD_SHOW_TYPE)));

            // get the workflow managers that have been chosen
            List wfMgrIds = new ArrayList();
            String wfMgrIdString = (String) p_request
                    .getParameter(CHOSEN_WORKFLOW_MANAGERS);
            StringTokenizer tokenizer = new StringTokenizer(wfMgrIdString, ",");
            String wfMgrId;
            if (tokenizer.hasMoreTokens())
            {
                wfMgrId = tokenizer.nextToken();
                wfMgrIds.add(wfMgrId);
                while (tokenizer.hasMoreTokens())
                {
                    wfMgrId = tokenizer.nextToken();
                    wfMgrIds.add(wfMgrId);
                }
            }

            wfti.setWorkflowManagerIds(wfMgrIds);

            wfti.notifyProjectManager(Boolean.valueOf(
                    p_request.getParameter(NOTIFICATION_FIELD)).booleanValue());
            sessionMgr.setAttribute(TEMPLATE_ID, wfId);
            if (wfti.getId() > 0)
            {
                sessionMgr
                        .setAttribute(WF_TEMPLATE_INFO_ID, wfti.getIdAsLong());
            }
        }
        storeNewInfo(p_request, sessionMgr, wfti);
    }

    private int[] split(String str, char x)
    {
        Vector v = new Vector();
        String str1 = new String();
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == x)
            {
                v.add(str1);
                str1 = new String();
            }
            else
            {
                str1 += str.charAt(i);
            }
        }
        v.add(str1);
        int array[];
        array = new int[v.size()];
        for (int i = 0; i < array.length; i++)
        {
            Integer num = new Integer((String) v.elementAt(i));
            array[i] = num.intValue();
        }
        return array;

    }

    // store the info of the new workflow template info
    @SuppressWarnings("unchecked")
	private void storeNewInfo(HttpServletRequest p_request,
            SessionManager sessionMgr, WorkflowTemplateInfo p_wfti)
            throws EnvoyServletException
    {
        String localePairId = p_request.getParameter(LOCALE_PAIR_FIELD);
        String storedLocalePairId = (String) sessionMgr
                .getAttribute(LOCALE_PAIR);
        Vector leveragedObjects = (Vector) sessionMgr
                .getAttribute(LEVERAGE_OBJ);
        Vector newLeverageObjects = new Vector();

        long lpId = -1;
        try
        {
            lpId = Long.parseLong(localePairId);
        }
        catch (NumberFormatException e)
        {
        }

        if (lpId != -1)
        {
            LocalePair lp = WorkflowTemplateHandlerHelper
                    .getLocalePairById(lpId);
            GlobalSightLocale tgt = lp.getTarget();
            for (int i = 0; i < leveragedObjects.size(); i++)
            {
            	if (tgt.getLanguageCode().endsWith("no")
            			||tgt.getLanguageCode().endsWith("nb")
            			||tgt.getLanguageCode().endsWith("nn"))
            	{
            		if( ((GlobalSightLocale) leveragedObjects.elementAt(i))
                            .getLanguageCode().equals("no")||  
                            ((GlobalSightLocale) leveragedObjects.elementAt(i))
                            .getLanguageCode().equals("nb")||
                            ((GlobalSightLocale) leveragedObjects.elementAt(i))
                            .getLanguageCode().equals("nn"))
            		{
						newLeverageObjects.addElement(leveragedObjects
								.elementAt(i));
            		}
            	}
            	else
            	{
            		 if (tgt.getLanguageCode().equals(
                             ((GlobalSightLocale) leveragedObjects.elementAt(i))
                                     .getLanguageCode()))
                     {
                         newLeverageObjects
                                 .addElement(leveragedObjects.elementAt(i));
                     }
            	}
               
            }
        }
        else
        {
            newLeverageObjects = leveragedObjects;
        }

        String leverage = (String) p_request.getParameter("leveragedLocales");
        leverage = leverage.substring(0, leverage.lastIndexOf(","));

        int leverageOrder[] = split(leverage, ',');
        Set<LeverageLocales> leveragedLocales = new HashSet<LeverageLocales>();

        for (int k = 0; k < leverageOrder.length; k++)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) newLeverageObjects
                    .elementAt(leverageOrder[k]);
            LeverageLocales leverageLocales = new LeverageLocales(gsl);
            leveragedLocales.add(leverageLocales);
        }

        // coming from first page for the first time (or changing all fields
        // during create)
        if (p_wfti == null || p_wfti.getId() < 0)
        {
            p_wfti = null; // gc since new object will be stored in SM
            LocalePair lp = WorkflowTemplateHandlerHelper
                    .getLocalePairById(lpId);
            long pId = Long.valueOf(p_request.getParameter(PROJECT_FIELD))
                    .longValue();
            Project project = WorkflowTemplateHandlerHelper
                    .getProjectById(Long.valueOf(
                            p_request.getParameter(PROJECT_FIELD)).longValue());

            // get the workflow managers that have been chosen
            List<String> wfMgrIds = new ArrayList<String>();
            String wfMgrIdString = (String) p_request
                    .getParameter(CHOSEN_WORKFLOW_MANAGERS);
            StringTokenizer tokenizer = new StringTokenizer(wfMgrIdString, ",");
            String wfMgrId;
            if (tokenizer.hasMoreTokens())
            {
                wfMgrId = tokenizer.nextToken();
                wfMgrIds.add(wfMgrId);
                while (tokenizer.hasMoreTokens())
                {
                    wfMgrId = tokenizer.nextToken();
                    wfMgrIds.add(wfMgrId);
                }
            }

            WorkflowTemplateInfo wfti = new WorkflowTemplateInfo(
                    p_request.getParameter(NAME_FIELD),
                    p_request.getParameter(DESCRIPTION_FIELD), project,
                    Boolean.valueOf(p_request.getParameter(NOTIFICATION_FIELD))
                            .booleanValue(), wfMgrIds, lp.getSource(),
                    lp.getTarget(), p_request.getParameter(ENCODING_FIELD),
                    leveragedLocales,Integer.parseInt
                    		(p_request.getParameter(SCORECARD_SHOW_TYPE)));
            wfti.setWorkflowType(p_request.getParameter(WORKFLOW_TYPE_FIELD));
            sessionMgr.setAttribute(WF_TEMPLATE_INFO, wfti);
            sessionMgr.setAttribute(LOCALE_PAIR, localePairId);
            if (wfti.getId() > 0)
            {
                sessionMgr
                        .setAttribute(WF_TEMPLATE_INFO_ID, wfti.getIdAsLong());
            }
        }
        // partial store is done when the locale pair id has not been
        // change on UI (when user clicks on "Next" and then "Previous"
        // buttons during an edit).
        else if (storedLocalePairId != null)
        {
            p_wfti.setName(p_request.getParameter(NAME_FIELD));
            p_wfti.setDescription(p_request.getParameter(DESCRIPTION_FIELD));
            p_wfti.setCodeSet(p_request.getParameter(ENCODING_FIELD));
            p_wfti.notifyProjectManager(Boolean.valueOf(
                    p_request.getParameter(NOTIFICATION_FIELD)).booleanValue());
            p_wfti.setLeveragingLocalesSet(leveragedLocales);
            p_wfti.setScorecardShowType(Integer
            		.parseInt(p_request.getParameter(SCORECARD_SHOW_TYPE)));

            sessionMgr.setAttribute(WF_TEMPLATE_INFO, p_wfti);
            if (p_wfti.getId() > 0)
            {
                sessionMgr.setAttribute(WF_TEMPLATE_INFO_ID,
                        p_wfti.getIdAsLong());
            }
        }
    }

    // Get all the info required to be displayed on the graphical workflow UI.
    // The info required for the dialog boxes for each node should also be
    // included.
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Vector getDisplayData(HttpServletRequest p_request,
            HttpSession p_appletSession) throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) p_appletSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String wfId = (String) sessionMgr.getAttribute(TEMPLATE_ID);

        // create the resource java bean
        ResourceBundle bundle = getBundle(p_appletSession);
        Vector objs = new Vector();

        // all images
        Hashtable imageHash = new Hashtable();
        imageHash.put("gpact", "/images/graphicalworkflow/gpact.gif");
        imageHash.put("gpexit", "/images/graphicalworkflow/gpexit.gif");
        imageHash.put("gpcond", "/images/graphicalworkflow/gpcond.gif");
        imageHash.put("gpand", "/images/graphicalworkflow/gpand.gif");
        imageHash.put("gparrow", "/images/graphicalworkflow/gparrow.gif");
        imageHash.put("pointer", "/images/graphicalworkflow/pointer.gif");
        imageHash.put("gpor", "/images/graphicalworkflow/gpor.gif");
        imageHash.put("gpsub", "/images/graphicalworkflow/gpsub.gif");
        imageHash.put("gpcancel", "/images/graphicalworkflow/gpcancel.gif");
        imageHash.put("gpsave", "/images/graphicalworkflow/gpsave.gif");
        imageHash.put("gpprint", "/images/graphicalworkflow/print.gif");
        imageHash.put("visible",
                WorkflowTemplateHandlerHelper.areAndOrNodesEnabled());
        // default data item ref for contional node.
        String dataItemRefName = bundle
                .getString(WorkflowConstants.CONDITION_UDA);

        objs.addElement(imageHash); // 0
        objs.addElement(dataItemRefName); // 1
        objs.addElement(getDataForDialog(sessionMgr, p_appletSession)); // 2
        // if the template id is null, we'll be in create mode (blank page).
        // Otherwise, a user has clicked on a workflow template within a table
        // and has passed the id for us.
        if (wfId != null)
        {
            addWorkflowInfo(wfId, objs); // 3
        }
        else
        {
            objs.addElement(new WorkflowTemplate()); // 3
        }
        objs.addElement(p_appletSession.getAttribute(WebAppConstants.UILOCALE));// 4

        return objs;
    }

    // perform the Post action (i.e. save, return data based on UI request,...)
    private Vector postAction(HttpServletRequest p_request,
            HttpSession p_appletSession) throws EnvoyServletException,
            IOException
    {
        Vector outData = null;
        SessionManager sessionMgr = (SessionManager) p_appletSession
                .getAttribute(SESSION_MANAGER);
        m_userId = (String) p_appletSession.getAttribute(WebAppConstants.USER_NAME);
        try
        {
            ObjectInputStream inputFromApplet = new ObjectInputStream(
                    p_request.getInputStream());
            Vector inData = (Vector) inputFromApplet.readObject();
            if (inData != null)
            { // if this is null the command is cancel.
                String command = (String) inData.elementAt(0);
                boolean isUserRole = command.equals("user");
                // return data in order to populate user or role.
                if (isUserRole || command.equals("role"))
                {
                    outData = new Vector();
                    outData.addElement(getDataForRole(
                            (String) inData.elementAt(1), isUserRole,
                            sessionMgr));
                }
                else if (command.equals("save"))
                {
                    // save the modified workflows.
                    saveWorkflow(sessionMgr, p_appletSession,
                            (WorkflowTemplate) inData.elementAt(1));
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ex);

        }
        return outData;
    }

    /**
     * Gets grid data for user role table of the activity property dialog.
     */
    @SuppressWarnings("rawtypes")
    private List<Object[]> getDataForRole(String p_activityName,
            boolean p_isUser, SessionManager p_sessionMgr)
            throws EnvoyServletException
    {
        // get source and target locale from WorkflowTemplateInfo stored in
        // session manager
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) p_sessionMgr
                .getAttribute(WF_TEMPLATE_INFO);

        GlobalSightLocale srcLocale = wfti.getSourceLocale();
        GlobalSightLocale targetLocale = wfti.getTargetLocale();
        List<Object[]> userRoles = null;
        if (p_isUser)
        {
            // obtain the roles to be turned into grid data.
            Collection usersCollection = WorkflowTemplateHandlerHelper
                    .getUserRoles(p_activityName, srcLocale.toString(),
                            targetLocale.toString());

            if (usersCollection != null)
            {
                Set projectUserIds = wfti.getProject().getUserIds();
                Vector<UserRoleImpl> usersInProject = new Vector<UserRoleImpl>();

                // filter out the users that aren't in the project
                for (Iterator i = usersCollection.iterator(); i.hasNext();)
                {
                    UserRoleImpl userRole = (UserRoleImpl) i.next();
                    if (projectUserIds.contains(userRole.getUser()))
                    {
                        usersInProject.add(userRole);
                    }
                }
                userRoles = new ArrayList<Object[]>(usersInProject.size());

                for (int i = 0; i < usersInProject.size(); i++)
                {
                    UserRoleImpl userRole = (UserRoleImpl) usersInProject
                            .get(i);
                    User user = WorkflowTemplateHandlerHelper.getUser(userRole
                            .getUser());
                    if (user != null)
                    {
                        String[] role = new String[6];
                        role[0] = user.getFirstName();
                        role[1] = user.getLastName();
                        role[2] = user.getUserName();
                        // 3 - place holder for calendaring
                        // since the wf instance needs this and uses
                        // same WorkflowTaskDialog code
                        role[3] = null;
                        role[4] = userRole.getName();
                        role[5] = userRole.getRate();
                        userRoles.add(role);                        
                    }
                }
            }
        }
        else
        {
            ContainerRole containerRole = WorkflowTemplateHandlerHelper
                    .getContainerRole(p_activityName, srcLocale.toString(),
                            targetLocale.toString(), wfti.getProject().getId());

            if (containerRole != null)
            {
                userRoles = new ArrayList<Object[]>(1);
                String[] role =
                { containerRole.getName() };
                userRoles.add(role);
            }
        }
        return userRoles;
    }

    // get the data required for the activity dialog
    private Hashtable getDataForDialog(SessionManager sessionMgr,
            HttpSession p_session) throws EnvoyServletException
    {
        ResourceBundle bundle = getBundle(p_session);
        Locale uiLocale = (Locale) p_session.getAttribute(UILOCALE);

        // Start Dialog data
        return WorkflowTemplateHandlerHelper.getDataForDialog(bundle, uiLocale,
                (WorkflowTemplateInfo) sessionMgr
                        .getAttribute(WF_TEMPLATE_INFO));
    }

    /*
     * Add the collection of workflow activities and the boolean that determines
     * whether a workflow can be modified.
     */
    private void addWorkflowInfo(String p_wfId, Vector p_displayInfo)
            throws EnvoyServletException
    {
        WorkflowTemplate wft = WorkflowTemplateHandlerHelper
                .getWorkflowTemplateById(Long.parseLong(p_wfId));
        p_displayInfo.addElement(wft);
    }

    // save the workflow...
    private void saveWorkflow(SessionManager p_sessionMgr,
            HttpSession p_session, WorkflowTemplate p_workflowTemplate)
            throws EnvoyServletException
    {
        // first get the workflow template info
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) p_sessionMgr
                .getAttribute(WF_TEMPLATE_INFO);

        // add the name and desc
        p_workflowTemplate.setName(wfti.getName());
        p_workflowTemplate.setDescription(wfti.getDescription());

        WorkflowTemplateHandlerHelper.saveWorkflowTemplateInfo(wfti,
                p_workflowTemplate, m_userId);

        clearWorkflowSessionExceptTableInfo(p_session, KEY);
    }

    public void clearWorkflowSessionExceptTableInfo(HttpSession p_session,
            String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);

        Integer sortType = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.SORTING);
        Boolean reverseSort = (Boolean) sessionMgr.getAttribute(p_key
                + TableConstants.REVERSE_SORT);
        Integer lastPage = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.LAST_PAGE_NUM);
        String nameField = (String) sessionMgr.getAttribute("nameField");
        String srcLocale = (String) sessionMgr.getAttribute("srcLocale");
        String targLocale = (String) sessionMgr.getAttribute("targLocale");
        String project = (String) sessionMgr.getAttribute("project");
        String companyName = (String) sessionMgr.getAttribute("companyName");
        
        sessionMgr.clear();

        sessionMgr.setAttribute(p_key + TableConstants.SORTING, sortType);
        sessionMgr.setAttribute(p_key + TableConstants.REVERSE_SORT,
                reverseSort);
        sessionMgr.setAttribute(p_key + TableConstants.LAST_PAGE_NUM, lastPage);
        sessionMgr.setAttribute("nameField", nameField);
        sessionMgr.setAttribute("srcLocale", srcLocale);
        sessionMgr.setAttribute("targLocale", targLocale);
        sessionMgr.setAttribute("project", project);
        sessionMgr.setAttribute("companyName", companyName);
    }
}
