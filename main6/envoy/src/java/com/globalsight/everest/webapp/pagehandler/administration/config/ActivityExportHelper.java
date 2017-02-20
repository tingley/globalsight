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

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports activity types.
 */
public class ActivityExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("ActivityTypes");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = ACTIVITY_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets activity types info.
     */
    public static File exportActivities(File actPropertyFile, String p_activityName)
    {
        try
        {
            Activity activity = ServerProxy.getJobHandler().getActivity(p_activityName);
            StringBuffer buffer = new StringBuffer();
            if (activity != null)
            {
                buffer.append("##ActivityType.").append(activity.getCompanyId()).append(".")
                        .append(activity.getId()).append(".begin").append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".ID = ")
                        .append(activity.getId()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".NAME = ")
                        .append(activity.getName()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".DISPLAY_NAME = ")
                        .append(activity.getDisplayName()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".COMPANY_ID = ")
                        .append(activity.getCompanyId()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".DESCRIPTION = ")
                        .append(activity.getDescription()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".IS_ACTIVE = ")
                        .append(activity.getIsActive()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".USER_TYPE = ")
                        .append(activity.getUseType()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".TYPE = ")
                        .append(Activity.typeAsString(activity.getType())).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".IS_EDITABLE = ")
                        .append(activity.getIsEditable()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".QA_CHECKS = ")
                        .append(activity.getQaChecks()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId())
                        .append(".RUN_DITA_QA_CHECKS = ").append(activity.getRunDitaQAChecks())
                        .append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId())
                        .append(".AUTO_COMPLETE_ACTIVITY = ")
                        .append(activity.getAutoCompleteActivity()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId()).append(".COMPLETE_TYPE = ")
                        .append(activity.getCompleteType()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId())
                        .append(".AFTER_JOB_CREATION = ").append(activity.getAfterJobCreation())
                        .append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId())
                        .append(".AFTER_JOB_DISPATCH = ").append(activity.getAfterJobDispatch())
                        .append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId())
                        .append(".AFTER_ACTIVITY_START = ")
                        .append(activity.getAfterActivityStart()).append(NEW_LINE);
                buffer.append("ActivityType.").append(activity.getId())
                        .append(".COMPLETE_SCHEDULE = ").append(activity.getCompleteSchedule())
                        .append(NEW_LINE);
                buffer.append("##ActivityType.").append(activity.getCompanyId()).append(".")
                        .append(activity.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(actPropertyFile, buffer.toString().getBytes());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return actPropertyFile;
    }

    private static void writeToFile(File actPropertyFile, byte[] bytes)
    {
        actPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(actPropertyFile, true);
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
