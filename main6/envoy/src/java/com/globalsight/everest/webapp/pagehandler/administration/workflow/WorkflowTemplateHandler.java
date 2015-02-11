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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.projecthandler.WfTemplateSearchParameters;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * WorkflowTemplateHandler is the page handler responsible for displaying a list
 * of workflow templates and perform actions supported by the UI (JSP).
 */

public class WorkflowTemplateHandler extends PageHandler
    implements WorkflowTemplateConstants
{
    
    //non user related state
    private int m_numOfWfsPerPage; //number of workflow templates per page

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    ////////////////////////////////////////////////////////////////////
    public WorkflowTemplateHandler()
    {
	try 
        {
            m_numOfWfsPerPage = SystemConfiguration.getInstance()
            .getIntParameter(SystemConfigParamNames.NUM_WFT_PER_PAGE);
	}
	catch (Exception e)
	{
	    m_numOfWfsPerPage = 10;
	}
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Override Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr =
                (SessionManager) session.getAttribute(SESSION_MANAGER);
        WfTemplateSearchParameters params =
            (WfTemplateSearchParameters)sessionMgr.getAttribute("searchParams");
        if (isPost(p_request))
        {
            String action = p_request.getParameter(ACTION);

            if (CANCEL_ACTION.equals(action))
            {
                clearSessionExceptTableInfo(session, KEY);
                sessionMgr.setAttribute("searchParams", params);
            }
            else if (SAVE_ACTION.equals(action))
            {
                saveDuplicates(p_request, session);
            }
            else if (SEARCH_ACTION.equals(action))
            {
                params = getSearchCriteria(p_request, false);
            }
            else if (ADV_SEARCH_ACTION.equals(action))
            {
                params = getSearchCriteria(p_request, true);
            }
            else if (IMPORT_ACTION.equals(action))
            {
                importWorkFlow(p_request, session);
            }
            else if (EXPORT_ACTION.equals(action))
            {
                exportWorkFlow(p_request, p_response, session);
                return;
            }
        }

        selectTemplatesForDisplay(p_request, session, params);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    private void importWorkFlow(HttpServletRequest p_request,
			HttpSession session)
	{
		FileUploadHelper o_upload = new FileUploadHelper();
		try
		{
			o_upload.doUpload(p_request);
			String list = o_upload.getFieldValue("localePairs");
			String name = o_upload.getFieldValue("nameTF");
			String projectId = o_upload.getFieldValue("project");
			String fileName = o_upload.getSavedFilepath();
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new File(fileName));
			ArrayList<LocalePair> alist = new ArrayList<LocalePair>();
			StringTokenizer st = new StringTokenizer(list, ",");
			while (st.hasMoreTokens())
			{
				String id = (String) st.nextToken();
				alist.add(ServerProxy.getLocaleManager().getLocalePairById(
						Long.parseLong(id)));
			}

			WorkflowTemplateHandlerHelper.importWorkflowTemplateInfo(
					doc, alist, name, projectId,
					getBundle(session));
		}
		catch (Exception e)
		{
			throw new EnvoyServletException(e);
		}

	}
    
	private void exportWorkFlow(HttpServletRequest p_request,
			HttpServletResponse p_response, HttpSession session)
	{
		String wfTemplateId = p_request.getParameter(WF_TEMPLATE_INFO_ID);
		WorkflowTemplateInfo template = WorkflowTemplateHandlerHelper
				.getWorkflowTemplateInfoById(Long
						.parseLong(wfTemplateId));
		String templateName = template.getName();
		String templateFileName = AmbFileStoragePathUtils
				.getWorkflowTemplateXmlDir().getAbsolutePath()
				+ File.separator + templateName + WorkflowConstants.SUFFIX_XML;
		File file = new File(templateFileName);
//		if (!file.exists()) {
//			throw new EnvoyServletException("");
//		}
		CommentFilesDownLoad download = new CommentFilesDownLoad();
		download.sendFileToClient(p_request, p_response, 
				templateName + WorkflowConstants.SUFFIX_XML, file);
	}
	/**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new WorkflowTemplateControlFlowHelper(p_request, p_response);
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Local Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Search for workflows with certain criteria.
     */
    private WfTemplateSearchParameters getSearchCriteria(HttpServletRequest p_request,
                                                         boolean advSearch)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);
        WfTemplateSearchParameters params = new WfTemplateSearchParameters();
        String buf = p_request.getParameter("nameOptions");
        params.setWorkflowNameCondition(buf);
        params.setWorkflowName(p_request.getParameter("nameField"));
        if (advSearch)
        {
            try 
            {
                buf = (String)p_request.getParameter("srcLocale");
                if (!buf.equals("-1"))
                {
                    params.setSourceLocale(ServerProxy.getLocaleManager().getLocaleById(
                                            Long.parseLong(buf)));
                }
                buf = (String)p_request.getParameter("targLocale");
                if (!buf.equals("-1"))
                {
                    params.setTargetLocale(ServerProxy.getLocaleManager().getLocaleById(
                                            Long.parseLong(buf)));
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            if (!p_request.getParameter("project").equals(""))
                params.setProjectId(p_request.getParameter("project"));
        }
        sessionMgr.setAttribute("searchParams", params);
        return params;
    }

    private void selectTemplatesForDisplay(HttpServletRequest p_request,
                                           HttpSession p_session,
                                           WfTemplateSearchParameters p_params)
        throws ServletException, IOException, EnvoyServletException
    {
        List templates = null;
        try
        {        
            if (p_params == null)
            {
                templates = WorkflowTemplateHandlerHelper.getAllWorkflowTemplateInfos();
            }
            else
            {
                templates = (List) ServerProxy.getProjectHandler().findWorkflowTemplates(p_params);
            } 
            
        }
        catch(Exception e)
        {
            throw new EnvoyServletException(e);
        }
        Locale uiLocale = (Locale)p_session.getAttribute(WebAppConstants.UILOCALE);
	    WorkflowTemplateInfoComparator comp = new WorkflowTemplateInfoComparator(uiLocale);
        setTableNavigation(p_request, p_session, templates, comp, 
        					m_numOfWfsPerPage, TEMPLATES, KEY);
                            
    }

    /* Convert the given string into an integer value; if null, or an error */
    /* occurs, return the default value instead (always 1) */
    private int parseInt(String p_string)
    {
        int intVal = 1;
        if (p_string != null)
        {
            try
            {
                intVal  = Integer.parseInt(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return intVal;
    }

    private void saveDuplicates(HttpServletRequest p_request, HttpSession session)
    throws EnvoyServletException
    {
        try {
            String wftiId = (String)session.getAttribute(WF_TEMPLATE_INFO_ID);
            String list = (String)p_request.getParameter("localePairs");
            String name = (String)p_request.getParameter("nameTF");
            ArrayList alist = new ArrayList();
            StringTokenizer st = new StringTokenizer(list, ",");
            while (st.hasMoreTokens()) {
                String id = (String) st.nextToken();
                alist.add(ServerProxy.getLocaleManager().getLocalePairById(Long.parseLong(id)));
            }
        
        
            WorkflowTemplateHandlerHelper.duplicateWorkflowTemplateInfo(
                                                Long.parseLong(wftiId),
                                                alist,
                                                name,
                                                getBundle(session));
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        }
    }


    //////////////////////////////////////////////////////////////////////
    //  End: Local Methods
    //////////////////////////////////////////////////////////////////////

}
