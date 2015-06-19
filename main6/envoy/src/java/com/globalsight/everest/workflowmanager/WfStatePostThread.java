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

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.WorkflowArrowInstance;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.util.mail.MailerConstants;

public class WfStatePostThread implements Runnable
{

    static private final Logger s_logger = Logger
            .getLogger(WfStatePostThread.class);

    private Task task;
    private String destinationArrow;

    public WfStatePostThread(Task task, String destinationArrow)
    {
        super();
        this.task = task;
        this.destinationArrow = destinationArrow;
    }

    @Override
    public void run()
    {
        wfStatePost(task, destinationArrow);
    }

    private void wfStatePost(Task p_task, String p_destinationArrow)
    {
        String toArrowName = p_destinationArrow;
        JSONObject jsonObj = new JSONObject();
        long jobId = p_task.getJobId();
        try
        {
            WorkflowTaskInstance nextTask = ServerProxy.getWorkflowServer()
                    .nextNodeInstances(p_task, p_destinationArrow, null);
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
                    Vector<WorkflowArrowInstance> arrows = nextTask
                            .getIncomingArrows();
                    if (arrows != null && arrows.size() == 1)
                    {
                        toArrowName = arrows.get(0).getName();
                    }
                    else
                    {
                        for (WorkflowArrowInstance arrow : arrows)
                        {
                            toArrowName = determineIncomingArrow(arrow,
                                    p_task.getId());
                            if (toArrowName != null)
                                break;
                        }
                    }
                }
                else
                {
                    WorkflowTaskInstance currentTask = ServerProxy
                            .getWorkflowServer().getWorkflowTaskInstance(
                                    p_task.getWorkflow().getId(),
                                    p_task.getId());
                    Vector<WorkflowArrowInstance> arrows2 = currentTask
                            .getOutgoingArrows();
                    for (WorkflowArrowInstance arrow2 : arrows2)
                    {
                        toArrowName = determineOutgoingArrow(arrow2);
                        if (toArrowName != null)
                            break;
                    }
                }
            }
            jsonObj.put("arrowText", toArrowName);
            jsonObj.put("jobId", jobId);
            jsonObj.put("jobName", p_task.getJobName());
            jsonObj.put("workflowId", p_task.getWorkflow().getIdAsLong());
            jsonObj.put("sourceLocale", p_task.getSourceLocale().toString());
            jsonObj.put("targetLocale", p_task.getTargetLocale().toString());
            jsonObj.put("prevaAtivity", p_task.getTaskDisplayName());

            L10nProfile l10nProfile = ServerProxy.getJobHandler()
                    .getL10nProfileByJobId(jobId);
            long wfStatePostId = l10nProfile.getWfStatePostId();
            WorkflowStatePosts wfStatePost = ServerProxy.getProjectHandler()
                    .getWfStatePostProfile(wfStatePostId);
            doPost(wfStatePost, jsonObj);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        s_logger.info("arrow name =============" + toArrowName);
    }

    @SuppressWarnings("unchecked")
    private String determineIncomingArrow(WorkflowArrowInstance arrow,
            long p_originalTaskId)
    {
        String toArrowName = null;
        WorkflowTaskInstance srcNode = (WorkflowTaskInstance) arrow
                .getSourceNode();
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
                WorkflowTaskInstance srcNode2 = (WorkflowTaskInstance) arrow2
                        .getSourceNode();
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

    private String determineOutgoingArrow(WorkflowArrowInstance arrow)
    {
        String toArrowName = null;
        WorkflowTaskInstance trgNode = (WorkflowTaskInstance) arrow
                .getTargetNode();
        if (trgNode.getType() == WorkflowConstants.STOP)
        {
            toArrowName = arrow.getName();
        }
        else if (trgNode.getType() == WorkflowConstants.CONDITION)
        {
            Vector<WorkflowArrowInstance> arrows = trgNode.getOutgoingArrows();
            for (WorkflowArrowInstance arrow2 : arrows)
            {
                WorkflowTaskInstance trgNode2 = (WorkflowTaskInstance) arrow2
                        .getTargetNode();
                if (trgNode2.getType() == WorkflowConstants.STOP)
                {
                    toArrowName = arrow2.getName();
                    break;
                }
            }
        }

        return toArrowName;
    }

    private static void doPost(WorkflowStatePosts wfStatePost,
            JSONObject message)
    {
        DefaultHttpClient client = new DefaultHttpClient();
        String listenerUrl = wfStatePost.getListenerURL();
        String secretKey = wfStatePost.getSecretKey();
        HttpPost post = new HttpPost(listenerUrl);
        post.setHeader(HttpHeaders.AUTHORIZATION, secretKey);
        StringEntity s;
        try
        {
            s = new StringEntity(message.toString());
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != 204)
            {
                if (StringUtils.isNotEmpty(wfStatePost.getRetryNumber()))
                {
                    int num = Integer.valueOf(wfStatePost.getRetryNumber());
                    for (int i = 0; i < num; i++)
                    {
                        doPost(wfStatePost, message);
                    }
                }
                if (StringUtils.isNotEmpty(wfStatePost.getName()))
                {
                    String recipient = wfStatePost.getNotifyEmail();
                    long companyId = wfStatePost.getCompanyId();
                    String[] messageArguments ={ message.toString() };
                    ServerProxy
                            .getMailer()
                            .sendMailFromAdmin(
                                    recipient,
                                    messageArguments,
                                    MailerConstants.WORKFLOW_STATE_POST_FAILURE_SUBJECT,
                                    MailerConstants.WORKFLOW_STATE_POST_FAILURE_MESSAGE,
                                    String.valueOf(companyId));
                }
                loggerTDAInfo(res);
                return;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void loggerTDAInfo(HttpResponse res)
    {
        if (res.getStatusLine().getStatusCode() == 400)
        {
            s_logger.info("The request payload data failed validation!");
        }
        else if (res.getStatusLine().getStatusCode() == 401)
        {
            s_logger.info("Not authorized. The secret value is incorrect!");
        }
        else if (res.getStatusLine().getStatusCode() == 405)
        {
            s_logger.info("The request did not use the POST method!");
        }
        else if (res.getStatusLine().getStatusCode() == 500)
        {
            s_logger.info("Database error!");
        }
    }
}
