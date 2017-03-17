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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports workflow state post profiles.
 */
public class WfStatePostProfileExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("WorkflowStatePostProfiles");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = WORKFLOW_STATE_POST_PROFILE_FILE_NAME + userName + "_"
                + sdf.format(new Date()) + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets workflow state post profiles info.
     */
    public static File exportWfStatePostProfile(File wfStatePostPropertyFile, String id)
    {
        try
        {
            WorkflowStatePosts wfStatePost = HibernateUtil.get(WorkflowStatePosts.class,
                    Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            if (wfStatePost != null)
            {
                buffer.append("##WorkflowStatePostProfile.").append(wfStatePost.getCompanyId())
                        .append(".").append(wfStatePost.getId()).append(".begin").append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".ID = ").append(wfStatePost.getId()).append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".NAME = ").append(wfStatePost.getName()).append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".DESCRIPTION = ").append(wfStatePost.getDescription())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".LISTENER_URL = ").append(wfStatePost.getListenerURL())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".SECRET_KEY = ").append(wfStatePost.getSecretKey())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".TIMEOUT_PERIOD = ").append(wfStatePost.getTimeoutPeriod())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".RETRY_NUMBER = ").append(wfStatePost.getRetryNumber())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".NOTIFY_EMAIL = ").append(wfStatePost.getNotifyEmail())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".COMPANY_ID = ").append(wfStatePost.getCompanyId())
                        .append(NEW_LINE);
                buffer.append("WorkflowStatePostProfile.").append(wfStatePost.getId())
                        .append(".POST_JOB_CHANGE = ").append(wfStatePost.isPostJobChange())
                        .append(NEW_LINE);
                buffer.append("##WorkflowStatePostProfile.").append(wfStatePost.getCompanyId())
                        .append(".").append(wfStatePost.getId()).append(".end").append(NEW_LINE)
                        .append(NEW_LINE);

                writeToFile(wfStatePostPropertyFile, buffer.toString().getBytes());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return wfStatePostPropertyFile;
    }

    private static void writeToFile(File wfStatePostPropertyFile, byte[] bytes)
    {
        wfStatePostPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(wfStatePostPropertyFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {

            }
        }
    }
}
