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

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.log.ActivityLog;
import com.globalsight.util.edit.EditUtil;

@WebService(endpointInterface = "com.globalsight.webservices.WebService4AEM", serviceName = "WebService4AEM", portName = "WebService4AEMPort")
public class WebService4AEMImpl extends Ambassador implements WebService4AEM
{
    @Override
    public String uploadFileForInitial(WrapHashMap map)
            throws WebServiceException
    {
        return this.uploadFileForInitial(map.getInputData());
    }

    public String initializeJob(String accessToken, String p_jobName,
            String priority, String fileProfileId) throws WebServiceException
    {
        checkAccess(accessToken, CREATE_JOB);
        checkPermission(accessToken, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

        String jobName = EditUtil.removeCRLF(p_jobName);
        String jobNameValidation = validateJobName(jobName);
        if (jobNameValidation != null)
        {
            throw new WebServiceException(
                    makeErrorXml("createJob", jobNameValidation));
        }
        ActivityLog.Start activityStart = null;
        Job job = null;
        try
        {
            String userName = getUsernameFromSession(accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("jobName", jobName);
            activityStart = ActivityLog.start(Ambassador.class,
                    "createJob(args)", activityArgs);
            job = ServerProxy.getJobHandler().getJobByJobName(jobName);
            if (job == null)
            {
                String userId = UserUtil.getUserIdByName(userName);
                long iFpId = Long.parseLong(fileProfileId);
                FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                        .readFileProfile(iFpId);

                job = JobCreationMonitor.initializeJob(jobName, userId,
                        fp.getL10nProfileId(), priority, Job.UPLOADING);
            }

            return job.getJobName();
        }
        catch (Exception e)
        {
            throw new WebServiceException(makeErrorXml("createJob",
                    "Cannot create a job because " + e.getMessage()));
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }
}
