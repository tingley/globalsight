package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

public class TranslatedTextHandler extends PageHandler
{
    private static final Logger s_logger = Logger
            .getLogger(TranslatedTextHandler.class);

    @Override
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession(false);
            String action = (String) p_request.getParameter("action");
            Long jobId = new Long(
                    (String) p_request
                            .getParameter(JobManagementHandler.JOB_ID));

            Job job = ServerProxy.getJobHandler().getJobById(jobId);

            if ("retrieveTranslatedText".equals(action))
            {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = null;
                Collection<SourcePage> sourcePages = job.getSourcePages();
                String result = "";
                for (SourcePage sourcePage : sourcePages)
                {
                    Set<TargetPage> tarPages = sourcePage.getTargetPages();
                    for (TargetPage targetPage : tarPages)
                    {
                        long targetPageId = targetPage.getId();
                        int percent = SegmentTuvUtil
                                .getTranslatedPercentageForTargetPage(targetPageId);
                        jsonObject = new JSONObject();
                        jsonObject.put("sourceId", sourcePage.getId()+"_"+String.valueOf(targetPage.getLocaleId()));
                        jsonObject.put("percent", percent);

                        jsonArray.add(jsonObject);
                    }
                }
                result = jsonArray.toJSONString();
                p_response.getWriter().write(result);
                return;
            }
            else
            {
                String wfids = (String) p_request
                        .getParameter(JobManagementHandler.WF_ID);
                Hashtable hash = new Hashtable();
                StringTokenizer st = new StringTokenizer(wfids, " ");
                while (st.hasMoreTokens())
                { 
                    hash.put(st.nextToken(), "1");
                }

                List sublist = new ArrayList();

                for (Workflow wf : job.getWorkflows())
                {
                    if (hash.get(String.valueOf(wf.getId())) != null)
                    {
                        sublist.add(wf);
                    }
                }
                Collections.sort(sublist, new WorkflowComparator(Locale.getDefault()));
                p_request.setAttribute("shortOrFullPageNameDisplay",
                        getShortOrFullPageNameDisplay(session));
                p_request.setAttribute("workflows", sublist);
                p_request.setAttribute("jobName", job.getJobName());
                p_request.setAttribute("sourcePages", job.getSourcePages());
                p_request.setAttribute(JobManagementHandler.JOB_ID, jobId+"");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private String getShortOrFullPageNameDisplay(HttpSession session)
    {
        UserParameter param = PageHandler.getUserParameter(session,
                UserParamNames.PAGENAME_DISPLAY);
        return param.getValue();
    }
}
