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
package com.globalsight.everest.workflowmanager;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.WorkflowArrowInstance;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.mail.MailerConstants;

public class WfStatePostThread implements Runnable
{
    static private final Logger s_logger = Logger.getLogger(WfStatePostThread.class);

    private CloseableHttpClient httpClient = null;

    private Task task;
    private String destinationArrow;
    private boolean isDispatch;

    public WfStatePostThread(Task task, String destinationArrow, boolean isDispatch)
    {
        super();
        this.task = task;
        this.destinationArrow = destinationArrow;
        this.isDispatch = isDispatch;
    }

    @Override
    public void run()
    {
        wfStatePost(task, destinationArrow, isDispatch);
    }

    private synchronized void wfStatePost(Task p_task, String p_destinationArrow, boolean isDispatch)
    {
        long taskId = p_task.getId();
        try
        {
            s_logger.info("Begin to post workflow state transition info for task: " + taskId);

            JSONObject jsonObj = getNotifyMessage(p_task, destinationArrow, isDispatch);
            s_logger.info("workflow transition post info: " + jsonObj);
            L10nProfile l10nProfile = ServerProxy.getJobHandler().getL10nProfileByJobId(
                    p_task.getJobId());
            long wfStatePostId = l10nProfile.getWfStatePostId();
            WorkflowStatePosts wfStatePost = ServerProxy.getProjectHandler().getWfStatePostProfile(
                    wfStatePostId);

            doPost(wfStatePost, jsonObj);
        }
        catch (Exception e)
        {
            s_logger.error("wfStatePost error:", e);
        }
        finally
        {
            shutdownHttpClient();
            HibernateUtil.closeSession();
            s_logger.info("End to post workflow state transition info for task: " + taskId);
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject getNotifyMessage(Task p_task, String p_destinationArrow, boolean isDispatch)
            throws Exception
    {
        String toArrowName = p_destinationArrow;
        JSONObject jsonObj = new JSONObject();
        long jobId = p_task.getJobId();
        if (isDispatch)
        {
            jsonObj.put("prevActivity", "start");
            WorkflowTaskInstance firstTask = ServerProxy.getWorkflowServer()
                    .getWorkflowTaskInstance(p_task.getWorkflow().getId(), p_task.getId());
            jsonObj.put("currActivity", firstTask.getActivityDisplayName());
            Vector<WorkflowArrowInstance> arrows3 = firstTask.getIncomingArrows();
            for (WorkflowArrowInstance arrow3 : arrows3)
            {
                WorkflowTaskInstance srcNode = (WorkflowTaskInstance) arrow3.getSourceNode();
                if (srcNode.getType() == WorkflowConstants.START)
                {
                    toArrowName = arrow3.getName();
                }
            }
        }
        else
        {
            WorkflowTaskInstance nextTask = ServerProxy.getWorkflowServer().nextNodeInstances(
                    p_task, p_destinationArrow, null);
            if (nextTask != null)
            {
                jsonObj.put("currActivity", nextTask.getActivityDisplayName());
            }
            else
            {
                jsonObj.put("currActivity", "exit");
            }
            if (StringUtils.isEmpty(p_destinationArrow))
            {
                if (nextTask != null)
                {
                    Vector<WorkflowArrowInstance> arrows = nextTask.getIncomingArrows();
                    if (arrows != null && arrows.size() == 1)
                    {
                        toArrowName = arrows.get(0).getName();
                    }
                    else
                    {
                        for (WorkflowArrowInstance arrow : arrows)
                        {
                            toArrowName = determineIncomingArrow(arrow, p_task.getId());
                            if (toArrowName != null)
                                break;
                        }
                    }
                }
                else
                {
                    WorkflowTaskInstance currentTask = ServerProxy.getWorkflowServer()
                            .getWorkflowTaskInstance(p_task.getWorkflow().getId(), p_task.getId());
                    Vector<WorkflowArrowInstance> arrows2 = currentTask.getOutgoingArrows();
                    for (WorkflowArrowInstance arrow2 : arrows2)
                    {
                        toArrowName = determineOutgoingArrow(arrow2);
                        if (toArrowName != null)
                            break;
                    }
                }
            }
            jsonObj.put("prevActivity", p_task.getTaskDisplayName());
        }
        jsonObj.put("arrowText", toArrowName);
        jsonObj.put("jobId", jobId);
        jsonObj.put("jobName", p_task.getJobName());
        jsonObj.put("workflowId", p_task.getWorkflow().getIdAsLong());
        jsonObj.put("sourceLocale", p_task.getSourceLocale().toString());
        jsonObj.put("targetLocale", p_task.getTargetLocale().toString());

        return jsonObj;
    }

    @SuppressWarnings("unchecked")
    private String determineIncomingArrow(WorkflowArrowInstance arrow, long p_originalTaskId)
    {
        String toArrowName = null;
        WorkflowTaskInstance srcNode = (WorkflowTaskInstance) arrow.getSourceNode();
        if (srcNode.getType() == WorkflowConstants.ACTIVITY
                && srcNode.getTaskId() == p_originalTaskId)
        {
            toArrowName = arrow.getName();
        }
        else if (srcNode.getType() == WorkflowConstants.CONDITION)
        {
            Vector<WorkflowArrowInstance> arrows = srcNode.getIncomingArrows();
            for (WorkflowArrowInstance arrow2 : arrows)
            {
                WorkflowTaskInstance srcNode2 = (WorkflowTaskInstance) arrow2.getSourceNode();
                if (srcNode2.getType() == WorkflowConstants.ACTIVITY
                        && srcNode2.getTaskId() == p_originalTaskId)
                {
                    toArrowName = arrow.getName();
                    break;
                }
            }
        }

        return toArrowName;
    }

    @SuppressWarnings("unchecked")
    private String determineOutgoingArrow(WorkflowArrowInstance arrow)
    {
        String toArrowName = null;
        WorkflowTaskInstance trgNode = (WorkflowTaskInstance) arrow.getTargetNode();
        if (trgNode.getType() == WorkflowConstants.STOP)
        {
            toArrowName = arrow.getName();
        }
        else if (trgNode.getType() == WorkflowConstants.CONDITION)
        {
            Vector<WorkflowArrowInstance> arrows = trgNode.getOutgoingArrows();
            for (WorkflowArrowInstance arrow2 : arrows)
            {
                WorkflowTaskInstance trgNode2 = (WorkflowTaskInstance) arrow2.getTargetNode();
                if (trgNode2.getType() == WorkflowConstants.STOP)
                {
                    toArrowName = arrow2.getName();
                    break;
                }
            }
        }

        return toArrowName;
    }

    private void doPost(WorkflowStatePosts wfStatePost, JSONObject message)
    {
        int num = wfStatePost.getRetryNumber();
        CloseableHttpClient httpClient = getHttpClient();
        for (int i = 0; i < num; i++)
        {
            try
            {
                String listenerUrl = wfStatePost.getListenerURL();
                String secretKey = wfStatePost.getSecretKey();
                HttpPost httpPost = new HttpPost(listenerUrl);
                httpPost.setHeader(HttpHeaders.AUTHORIZATION, secretKey);
                RequestConfig config = RequestConfig.custom()
                        .setConnectionRequestTimeout(wfStatePost.getTimeoutPeriod() * 1000)
                        .setConnectTimeout(wfStatePost.getTimeoutPeriod() * 1000)
                        .setSocketTimeout(wfStatePost.getTimeoutPeriod() * 1000).build();
                httpPost.setConfig(config);
                StringEntity reqEntity = new StringEntity(message.toString());
                reqEntity.setContentEncoding("UTF-8");
                reqEntity.setContentType("application/json");
                httpPost.setEntity(reqEntity);
                HttpResponse response = null;
                try
                {
                    response = httpClient.execute(httpPost);
                }
                catch (Exception e)
                {
                    s_logger.error("Post workflow transition info error:", e);
                }
                finally
                {
                    if (response != null)
                    {
                        EntityUtils.consumeQuietly(response.getEntity());
                    }                    
                }
                if (response != null)
                {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 204)
                    {
                        break;
                    }
                    else
                    {
                        logPostFailureInfo(statusCode);
                        if (StringUtils.isNotEmpty(wfStatePost.getNotifyEmail()) && (i == num))
                        {
                            String recipient = wfStatePost.getNotifyEmail();
                            long companyId = wfStatePost.getCompanyId();
                            String[] messageArguments =
                            { wfStatePost.getName(), wfStatePost.getListenerURL(), message.toString() };
                            ServerProxy.getMailer().sendMailFromAdmin(recipient, messageArguments,
                                    MailerConstants.WORKFLOW_STATE_POST_FAILURE_SUBJECT,
                                    MailerConstants.WORKFLOW_STATE_POST_FAILURE_MESSAGE,
                                    String.valueOf(companyId));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error(e);
            }
        }
    }

    CookieSpecProvider easySpecProvider = new CookieSpecProvider()
    {
        public CookieSpec create(HttpContext context)
        {
            return new BrowserCompatSpec()
            {
                @Override
                public void validate(Cookie cookie, CookieOrigin origin)
                        throws MalformedCookieException
                {
                    // Oh, I am easy
                }
            };
        }
    };

    Registry<CookieSpecProvider> reg = RegistryBuilder.<CookieSpecProvider> create()
            .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
            .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
            .register("mySpec", easySpecProvider).build();

    RequestConfig requestConfig = RequestConfig.custom().setCookieSpec("mySpec").build();

    private CloseableHttpClient getHttpClient()
    {
        if (httpClient == null)
        {
            httpClient = HttpClients.custom().setDefaultCookieSpecRegistry(reg)
                    .setDefaultRequestConfig(requestConfig).build();
        }

        return httpClient;
    }

    private void shutdownHttpClient()
    {
        if (httpClient == null)
            return;

        try
        {
            httpClient.close();
        }
        catch (IOException e)
        {
            s_logger.error("Fail to close httpclient", e);
        }
    }

    private static void logPostFailureInfo(int statusCode)
    {
        s_logger.warn("Fail to post workflow transition info, status code is: " + statusCode);
        if (statusCode == 400)
        {
            s_logger.warn("Workflow state post failure: The request payload data failed validation!");
        }
        else if (statusCode == 401)
        {
            s_logger.warn("Workflow state post failure: Not authorized. The secret value is incorrect!");
        }
        else if (statusCode == 405)
        {
            s_logger.warn("Workflow state post failure: The request did not use the POST method!");
        }
        else if (statusCode == 500)
        {
            s_logger.warn("Workflow state post failure: Database error!");
        }
    }
}
