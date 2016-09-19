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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.mail.MailerConstants;

public class JobStatePostThread extends Thread implements Runnable
{
    static private final Logger s_logger = Logger.getLogger(JobStatePostThread.class);

    private CloseableHttpClient httpClient = null;

    private Job job;
    private String previousState;
    private String currentState;

    private static ConcurrentHashMap<Long, List<String>> jobStateInfo = new ConcurrentHashMap<Long, List<String>>();

    // Avoid post multiple times for same state value.
    private static List<String> CARED_JOB_STATES = new ArrayList<String>();
    static
    {
        CARED_JOB_STATES.add(Job.IN_QUEUE);
        CARED_JOB_STATES.add(Job.EXTRACTING);
        CARED_JOB_STATES.add(Job.LEVERAGING);
        CARED_JOB_STATES.add(Job.PROCESSING);
    }
    
    public JobStatePostThread(Job job, String previousState, String currentState)
    {
        super();
        this.job = job;
        this.previousState = previousState;
        this.currentState = currentState;
    }

    @Override
    public void run()
    {
        List<String> finishedStates = jobStateInfo.get(job.getId());
        if (finishedStates != null && finishedStates.contains(currentState)
                && CARED_JOB_STATES.contains(currentState))
        {
            return;
        }

        if (finishedStates == null)
        {
            finishedStates = new ArrayList<String>();
        }
        finishedStates.add(currentState);
        jobStateInfo.put(job.getId(), finishedStates);

        jobStatePost(job, previousState, currentState);
    }

    private synchronized void jobStatePost(Job job, String previousState, String currentState)
    {
        long jobId = job.getId();
        try
        {
            s_logger.info("Begin to post job state transition info for job: " + jobId);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("jobId", jobId);
            jsonObj.put("jobName", job.getJobName());
            jsonObj.put("previousState", previousState);
            jsonObj.put("currentState", currentState);
            s_logger.info("job transition post info: " + jsonObj);

            L10nProfile l10nProfile = job.getL10nProfile();
            long wfStatePostId = l10nProfile.getWfStatePostId();
            WorkflowStatePosts wfStatePost = ServerProxy.getProjectHandler().getWfStatePostProfile(
                    wfStatePostId);

            doPost(wfStatePost, jsonObj);
        }
        catch (Exception e)
        {
            s_logger.error("jobStatePost error:", e);
        }
        finally
        {
            shutdownHttpClient();
            HibernateUtil.closeSession();
            s_logger.info("End to post job state transition info for job: " + jobId);
        }
    }

    private void doPost(WorkflowStatePosts wfStatePost, JSONObject message)
    {
        int num = wfStatePost.getRetryNumber();
        CloseableHttpClient httpClient = getHttpClient();
        for (int i = 0; i < num + 1; i++)
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
                    s_logger.error("Post job transition info error:", e);
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
                                    MailerConstants.JOB_STATE_POST_FAILURE_SUBJECT,
                                    MailerConstants.JOB_STATE_POST_FAILURE_MESSAGE,
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
        s_logger.warn("Fail to post job transition info, status code is: " + statusCode);
        if (statusCode == 400)
        {
            s_logger.warn("Job state post failure: The request payload data failed validation!");
        }
        else if (statusCode == 401)
        {
            s_logger.warn("Job state post failure: Not authorized. The secret value is incorrect!");
        }
        else if (statusCode == 405)
        {
            s_logger.warn("Job state post failure: The request did not use the POST method!");
        }
        else if (statusCode == 500)
        {
            s_logger.warn("Job state post failure: Database error!");
        }
    }
}

