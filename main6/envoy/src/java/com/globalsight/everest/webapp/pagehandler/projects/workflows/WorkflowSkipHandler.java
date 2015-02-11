package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.SkipActivityVo;
import com.globalsight.util.FormUtil;

public class WorkflowSkipHandler extends PageHandler
{

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

        String workflowId = p_request.getParameter(JobManagementHandler.WF_ID);
        String jobId = p_request.getParameter(JobManagementHandler.JOB_ID);
        
        String[] workflowIds = workflowId.split(" ");

        Locale uiLocale = (Locale)p_request.getSession().getAttribute(WebAppConstants.UILOCALE);
        List<SkipActivityVo> list = ServerProxy.getWorkflowManager()
                .getLocalActivity(workflowIds, uiLocale);

        p_request.setAttribute("skiplist", list);
        p_request.setAttribute(JobManagementHandler.JOB_ID, jobId);
        FormUtil.addSubmitToken(p_request, FormUtil.Forms.SKIP_ACTIVITIES);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

}
