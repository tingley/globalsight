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

//javax
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandlerLocal;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

public class ImportErrorHandler extends JobDetailsHandler
{
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
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        NavigationBean pendingBean = new NavigationBean(
                JobManagementHandler.PENDING_BEAN,
                p_thePageDescriptor.getPageName());
        p_request.setAttribute(JobManagementHandler.PENDING_BEAN, pendingBean);
        NavigationBean detailsBean = new NavigationBean(
                JobManagementHandler.DETAILS_BEAN,
                p_thePageDescriptor.getPageName());
        p_request.setAttribute(JobManagementHandler.DETAILS_BEAN, detailsBean);
        getJobDetailsInfo(p_request, s_isCostingEnabled, s_isRevenueEnabled);
        String jobId = (String) p_request
                .getAttribute(JobManagementHandler.JOB_ID);
        Job job = HibernateUtil.get(JobImpl.class, Long.parseLong(jobId));
        p_request.setAttribute("allPageError",
                Boolean.toString(JobHandlerLocal.containsAllErrors(job)));

        // turn off cache. do both. "pragma" for the older browsers.
        p_response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        p_response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
        p_response.addHeader("Cache-Control", "no-store"); // tell proxy not to
                                                           // cache
        p_response.addHeader("Cache-Control", "max-age=0"); // stale right away

        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_thePageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

    // This method gets called from getJobDetailsInfo function.
    protected String getJobContentInfo(Job p_job, HttpServletRequest p_request)
            throws EnvoyServletException
    {
        // retrieving the ui - locale
        Locale uiLocale = (Locale) p_request.getSession()
                .getAttribute(UILOCALE);

        StringBuffer sB = new StringBuffer();
        List sourcePages = new ArrayList(p_job.getSourcePages());
        for (int i = 0; i < sourcePages.size(); i++)
        {
            SourcePage curPage = (SourcePage) sourcePages.get(i);
            sB.append("<TR BGCOLOR=\"" + (i % 2 == 0 ? "#FFFFFF" : "#EEEEEE")
                    + "\">\n");
            // Page Name
            sB.append("<TD VALIGN=\"BOTTOM\" style='word-wrap:break-word;'><SPAN CLASS=\"standardText\">"
                    + curPage.getExternalPageId() + "</SPAN></TD>\n");
            // Word Count
            sB.append("<TD ALIGN=\"CENTER\" VALIGN=\"BOTTOM\"><SPAN CLASS=\"standardText\">"
                    + curPage.getWordCount() + "</SPAN></TD>\n");
            // Status
            String state = curPage.getPageState();
            sB.append("<TD VALIGN=\"BOTTOM\"><SPAN CLASS=\""
                    + (state.equals(PageState.IMPORT_FAIL) ? TABLE_ENTRY_RED
                            : TABLE_ENTRY) + "\">" + state + "</SPAN></TD>\n");
            // Message
            GeneralException exception = curPage.getRequest().getException();
            sB.append("<TD VALIGN=\"BOTTOM\" style='word-wrap:break-word;'><SPAN CLASS=\"standardText\">"
                    + ((state.equals(PageState.IMPORT_FAIL) || (curPage
                            .getRequest().getType() < 0)) ? (exception == null ? GeneralExceptionConstants.DEFAULT_MSG_STRING
                            : exception.getTopLevelMessage(uiLocale))
                            : GeneralExceptionConstants.DEFAULT_MSG_STRING)
                    + "</SPAN></TD>\n");
            sB.append("</TR>");
        }
        return sB.toString();
    }

}
