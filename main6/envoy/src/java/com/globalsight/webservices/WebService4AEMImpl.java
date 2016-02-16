/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.webservices;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;

@WebService(endpointInterface = "com.globalsight.webservices.WebService4AEM", serviceName = "WebService4AEM", portName = "WebService4AEMPort")
public class WebService4AEMImpl extends Ambassador implements WebService4AEM
{
    private static final Logger logger = Logger
            .getLogger(WebService4AEMImpl.class);

    @Override
    public String uploadFileForInitial(WrapHashMap map)
            throws WebServiceException
    {
        HashMap<String, Object> args = map.getInputData();
        String jobId = this.uploadFileForInitial(args);

        try
        {
            Job job = ServerProxy.getJobHandler()
                    .getJobById(Long.parseLong(jobId));

            if (job.getJobType() == null)
            {
                job.setJobType("aem_gs_translator");
                HibernateUtil.update(job);
            }
        }
        catch (Exception ex)
        {
            // ignore this - avoid to stop job, just effect request scope
        }

        return jobId;
    }

    @Override
    public String dispatchJob(String p_accessToken, String p_jobName)
            throws WebServiceException
    {
        String message = "";
        // Validate inputting parameters
        User user = null;
        try
        {
            user = ServerProxy.getUserManager()
                    .getUserByName(getUsernameFromSession(p_accessToken));
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(user.getUserId());

            if (!ps.getPermissionFor(Permission.JOB_WORKFLOWS_DISPATCH)
                    && !ps.getPermissionFor(Permission.JOBS_DISPATCH))
            {
                String msg = "User " + user.getUserName()
                        + " does not have enough permission";
                return makeErrorXml("dispatchJob", msg);
            }
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_jobName, "Job name");
        }
        catch (Exception e)
        {
            return makeErrorXml("dispatchJob", e.getMessage());
        }

        // WebServicesLog.Start activityStart = null;
        Object activityStart = null;
        try
        {
            String userName = this.getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", p_jobName);

            try
            {
                Class webServicesLogClass = Class
                        .forName("com.globalsight.webservices.WebServicesLog");
                Method startMethod = webServicesLogClass.getMethod("start",
                        Class.class, String.class, Map.class);
                activityStart = startMethod.invoke(null,
                        WebService4AEMImpl.class,
                        "dispatchJob(p_accessToken, p_jobName)", activityArgs);
            }
            catch (Exception ex)
            {
                // ignore this
            }

            Job job = ServerProxy.getJobHandler().getJobByJobName(p_jobName);
            ServerProxy.getJobHandler().dispatchJob(job);

            return null;
        }
        catch (Exception e)
        {
            message = makeErrorXml("dispatchJob", e.getMessage());
            throw new WebServiceException(message);
        }
        finally
        {
            if (activityStart != null)
            {
                try
                {
                    Class webServicesLogStartClass = Class.forName(
                            "com.globalsight.webservices.WebServicesLog$Start");
                    Method endMethod = webServicesLogStartClass
                            .getMethod("end");
                    endMethod.invoke(activityStart);
                }
                catch (Exception ex)
                {
                    // ignore this
                }
            }
        }
    }

    @Override
    public String getJobNameById(String p_accessToken, long jobId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_JOB_STATUS);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);
        
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(jobId);

            return job.getJobName();
        }
        catch (Exception ex)
        {
            // ignore this - avoid to stop job, just eff
        }

        return "";
    }
}
