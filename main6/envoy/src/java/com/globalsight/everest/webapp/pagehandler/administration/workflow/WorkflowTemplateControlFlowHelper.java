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

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.dependencychecking.WorkflowTemplateDependencyChecker;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * WorkflowTemplateControlFlowHelper, A page flow helper that 
 * checks wfti dependencies and then dispatches the user
 * to the correct JSP.
 */
class WorkflowTemplateControlFlowHelper
    implements ControlFlowHelper, WebAppConstants, WorkflowTemplateConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            WorkflowTemplateControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;
    String m_userId;

    public WorkflowTemplateControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then 
     * returns the name of the link to follow
     * 
     * @return 
     * @exception EnvoyServletException
     */
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        HttpSession p_session = m_request.getSession(false);
        m_userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        String destinationPage = null;

        // action should only be null for paging purposes
        if (m_request.getParameter(ACTION) == null) 
        {
            destinationPage = "self";
        }
        else if (m_request.getParameter(ACTION).equals(NEW_ACTION)) 
        {
            destinationPage = "new1";
        }
        else if (m_request.getParameter(ACTION).equals(EDIT_ACTION)) 
        {
            destinationPage = "modify";
        }
        else if (m_request.getParameter(ACTION).equals(DUPLICATE_ACTION)) 
        {
            destinationPage = "duplicate";
        }
        else if (m_request.getParameter(ACTION).equals(SAVE_ACTION)) 
        {
            destinationPage = "save";
        }
        else if (m_request.getParameter(ACTION).equals(IMPORT_ACTION)) 
        {
            destinationPage = "_import";
        }
        else if (m_request.getParameter(ACTION).equals(EXPORT_ACTION)) 
        {
            destinationPage = "self";
        }
        else if (m_request.getParameter(ACTION).equals(FILTER_SEARCH)) 
        {
            destinationPage = "self";
        }
        else if (m_request.getParameter(ACTION).equals(REMOVE_ACTION))
        {  
            String wftiId = m_request.getParameter("wfTemplateInfoId");
            Vector deps = null;
            Vector<String> nonDependency = new Vector<String>();
            deps = checkForDependencies(wftiId, p_session, nonDependency);
            if (nonDependency.size() > 0)
            {
                for (String nonDependencyId : nonDependency)
                {
                    try
                    {
                        removeTemplate(nonDependencyId);
                    }
                    catch (ServletException se)
                    {
                        throw new EnvoyServletException(se);
                    }
                    catch (IOException ie)
                    {
                        throw new EnvoyServletException(ie);
                    }
                    destinationPage = "self";
                }
            }
            if(deps.size() > 0) 
            {
            	//format error message
            	StringBuffer error_msg_bf = new StringBuffer();
           		error_msg_bf.append("<span class=\"errorMsg\">");
           		for (int k=0; k<deps.size(); k++)
           		{
           			if ( k == 0 ) {
           				error_msg_bf.append(deps.get(k) + "<br>");
           			} else if ( k == 1 ) {
           				error_msg_bf.append("<p>" + deps.get(k) + "<br>");
           			} else {
           				error_msg_bf.append(deps.get(k) + "<br>" );
           			}
           		}
           		error_msg_bf.append("</span>");
           		
//            			error_msg_bf.append("<p>***WorkFlows***<br>");
//            			for (int i=0; i<associatedWF.size(); i++){
//            				WorkflowTemplateInfo wf2 = (WorkflowTemplateInfo) associatedWF.get(i);
//            				error_msg_bf.append(wf2.getName()+"<br>");
//            			}
//            			error_msg_bf.append("<br>");
//             		}
//            		
//            		if (assocaitedL18nProfiles != null && assocaitedL18nProfiles.size() > 0) {
//            			error_msg_bf.append("***Localization Profiles***<br>");
//            			for (int j=0; j<assocaitedL18nProfiles.size(); j++) {
//            				L10nProfile l10nProfile = (L10nProfile) assocaitedL18nProfiles.get(j);
//            				error_msg_bf.append(l10nProfile.getName() + "<br>");
//            			}
//            		}
//            		
//            		error_msg_bf.append("</span>");
            		
                SessionManager sessionMgr =
                    (SessionManager)p_session.getAttribute(SESSION_MANAGER);
                sessionMgr.setAttribute(DEPENDENCIES, error_msg_bf.toString());
                destinationPage = "dependencies";
            }
        }

        return destinationPage;
    }

    /**
     * Check if any objects have dependencies on this Workflow Template.
     * This should be called BEFORE attempting to remove a Workflow Template.
     * <p>
     * 
     * @param p_wftiId
     * @param session
     * @param nonDependency 
     * @param dependency 
     * @return 
     * @exception EnvoyServletException
     *                   Failed to look for dependencies for the profile.
     *                   The cause is indicated by the exception message.
     */
    private Vector checkForDependencies(String p_wftiId, HttpSession session, Vector<String> nonDependency)
        throws EnvoyServletException
    {
        try
        {
            Vector deps = new Vector();
            ResourceBundle bundle = PageHandler.getBundle(session);
            String[] wftiIds = p_wftiId.trim().split(" ");
            for (String wftiId : wftiIds)
            {
                WorkflowTemplateInfo wfti = getWorkflowTemplateInfo(wftiId);

                WorkflowTemplateDependencyChecker depChecker = new WorkflowTemplateDependencyChecker();
                Hashtable catDeps = depChecker.categorizeDependencies(wfti);

                // Now convert the hashtable into a Vector of Strings

                if (catDeps.size() > 0)
                {
                    if (deps.isEmpty())
                    {
                        Object[] args =
                        { bundle.getString("lb_workflow_templates") };
                        deps.add(MessageFormat.format(
                                bundle.getString("msg_dependency_workflow"),
                                args));
                    }
                }
                else
                {
                    nonDependency.add(wftiId);
                }
                for (Enumeration e = catDeps.keys(); e.hasMoreElements();)
                {

                    String key = (String) e.nextElement();
                     if (deps.lastIndexOf("\n*** " + bundle.getString(key)
                            + " ***\n") == -1)
                    {
                        deps.add("\n*** " + bundle.getString(key) + " ***\n");
                    }
                    deps.add(wfti.getName() + " : ");
                    Vector values = (Vector) catDeps.get(key);
                    for (int i = 0; i < values.size(); i++)
                    {
                        deps.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp"+(String) values.get(i) + "\n");
                    }
                    deps.add("\n");
                }
            }
            return deps;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }


    /**
     * Remove the selected template.
     * 
     * @param wftiId
     * @exception ServletException
     * @exception IOException
     * @exception EnvoyServletException
     */
    private void removeTemplate(String wftiId)
        throws ServletException, IOException, EnvoyServletException
    {        
        try
        {
            WorkflowTemplateInfo wfti = getWorkflowTemplateInfo(wftiId);
            if (wfti.getIsActive())
            {
                ServerProxy.getProjectHandler().removeWorkflowTemplate(
                        Long.parseLong(wftiId));
                OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                        OperationLog.COMPONET_WORKFLOW, wfti.getName());
            }
        }
	catch (GeneralException ge)
	{
	    throw new EnvoyServletException(ge);
	}
	catch (RemoteException re)
	{
	    throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
	}
        catch (NamingException ne)
        {
	    throw new EnvoyServletException(ne);
        }
    } 


    /**
     * Remove the selected template.
     * 
     * @param wftiId
     * @return 
     * @exception ServletException
     * @exception IOException
     * @exception EnvoyServletException
     */
    private WorkflowTemplateInfo getWorkflowTemplateInfo(String wftiId)
        throws ServletException, IOException, EnvoyServletException
    {   
        WorkflowTemplateInfo wfti = null;    
        try
        {
            wfti = ServerProxy.getProjectHandler().
                getWorkflowTemplateInfoById(Long.parseLong(wftiId));
        }
	catch (GeneralException ge)
	{
	    throw new EnvoyServletException(ge);
	}
	catch (RemoteException re)
	{
	    throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
	}
        catch (NamingException ne)
        {
	    throw new EnvoyServletException(ne);
        }
        return wfti;
    }    
}
