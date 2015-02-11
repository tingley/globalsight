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

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;

import com.globalsight.everest.servlet.EnvoyServletException;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import com.globalsight.everest.workflowmanager.Workflow;

import com.globalsight.util.GlobalSightLocale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import java.rmi.RemoteException; 

public class ExportErrorHandler
    extends JobDetailsHandler
{
    //  static class variables
    private static Logger s_logger =
        Logger.getLogger(
            ExportErrorHandler.class.getName());

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param p_ageDescriptor the description of the page to be produced.
     * @param p_request original request sent from the browser.
     * @param p_response original response object.
     * @param p_context the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_descriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, RemoteException, EnvoyServletException
    {
        NavigationBean exportedBean =
            new NavigationBean(JobManagementHandler.EXPORTED_BEAN,
                               p_descriptor.getPageName());
        p_request.setAttribute(JobManagementHandler.EXPORTED_BEAN,
                               exportedBean);

        getJobDetailsInfo(p_request);

        // turn off cache.  do both.  "pragma" for the older browsers.
        p_response.setHeader("Pragma", "no-cache"); //HTTP 1.0
        p_response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
        p_response.addHeader("Cache-Control", "no-store"); // tell proxy not to cache
        p_response.addHeader("Cache-Control", "max-age=0"); // stale right away

        // forward to the jsp page.
        RequestDispatcher dispatcher = 
            p_context.getRequestDispatcher(p_descriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

    protected Job getJobDetailsInfo(HttpServletRequest p_request)
    throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale)session.
            getAttribute(WebAppConstants.UILOCALE);
        Workflow wf = 
            WorkflowHandlerHelper.
            getWorkflowById(Long.parseLong(p_request.
                                           getParameter(JobManagementHandler.
                                                        ERROR_WF_PARAM)));
        GlobalSightLocale targetLocale = wf.getTargetLocale();
        p_request.setAttribute(JobManagementHandler.TRGT_LOCALE_SCRIPTLET,
                               targetLocale.getDisplayName(uiLocale));
        return super.getJobDetailsInfo(p_request, s_isCostingEnabled, s_isRevenueEnabled);
    }

    // This method gets called from getJobDetailsInfo function.
    protected String getJobContentInfo(Job p_job, HttpServletRequest p_request)
    throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale)session.
            getAttribute(WebAppConstants.UILOCALE);
        ResourceBundle bundle = getBundle(session);
        Workflow wf = WorkflowHandlerHelper.
            getWorkflowById(Long.parseLong(p_request.
                                           getParameter(JobManagementHandler.
                                                        ERROR_WF_PARAM)));
        StringBuffer sb = new StringBuffer();
        List targetPages = new ArrayList(wf.getTargetPages());
        for (int i=0; i<targetPages.size(); i++)
        {
            TargetPage curPage = (TargetPage)targetPages.get(i);
            String state = curPage.getPageState();

            sb.append("<TR VALIGN=TOP BGCOLOR=\"");
            sb.append(i%2==0 ? "#FFFFFF" : "#EEEEEE");
            sb.append("\" ");
            sb.append("CLASS=\"");
            sb.append((state.equals(PageState.EXPORT_FAIL) ?
                       "warningText" : 
                       "standardText"));
            sb.append("\">\n");
            sb.append("<TD><INPUT TYPE=CHECKBOX NAME=page");
            sb.append(" VALUE=pageId_");
            sb.append(curPage.getId());
            sb.append("_wfId_");
            sb.append(Long.toString(wf.getId()));
            sb.append("></TD>");
            sb.append("<TD>");
            sb.append(curPage.getExternalPageId());
            sb.append("</TD>\n");
            sb.append("<TD>");
            sb.append(state);
            sb.append("</TD>\n");
            sb.append("<TD>");
            
            if (state.equals(PageState.EXPORT_FAIL))
            {
                sb.append(lookupExportErrorMessage(curPage, uiLocale));
            }
            else
            {
                sb.append("");
            }
            sb.append("</TD>\n");
            sb.append("</TR>\n");
        }
        return sb.toString();
    }

    /**
    * Looks up the error message for a given target page
    * @param targetPage -- the target page
    * @param uiLocale -- the user's UI locale
    * @return the localized export error message
    */
    private String lookupExportErrorMessage(TargetPage p_targetPage, Locale p_uiLocale)
    {
        String exportError = "";
        
        try {
            if (p_targetPage.getExportError() != null)
                //get only the toplevel message for the UI
                exportError = p_targetPage.getExportError().getOwnMessage(p_uiLocale);
        }
        catch (Exception e)
        {
            s_logger.error("Problem getting export error from target page: ", e);
            exportError = "Unknown Error";
        }
        
        return exportError;
    }
}

