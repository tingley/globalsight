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
import java.util.Set;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports localization profiles.
 */
public class LocProfileExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append(" Localization Profiles");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = LOC_PROFILE_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets localization profiles info.
     */
    public static File propertiesInputLP(File locPropertyFile, String profileId)
    {
        try
        {
            BasicL10nProfile locProfile = (BasicL10nProfile) ServerProxy.getProjectHandler()
                    .getL10nProfile(Long.parseLong(profileId));
            StringBuffer buffer = new StringBuffer();
            if (locProfile != null)
            {
                buffer.append("##LocalizationProfile.").append(locProfile.getCompanyId())
                        .append(".").append(locProfile.getId()).append(".begin").append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId()).append(".ID = ")
                        .append(locProfile.getId()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId()).append(".NAME = ")
                        .append(locProfile.getName()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DESCRIPTION = ").append(locProfile.getDescription())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".PRIORITY = ").append(locProfile.getPriority()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".SOURCE_LOCALE_ID = ")
                        .append(locProfile.getSourceLocale().getId()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".PROJECT_ID = ").append(locProfile.getProjectId())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".IS_AUTO_DISPATCH = ").append(locProfile.isAutoDispatch())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".USE_MT_ON_JOB_CREATION = ")
                        .append(locProfile.getUseMtOnJobCreation()).append(NEW_LINE);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".TIMESTAMP = ").append(df.format(locProfile.getTimestamp()))
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_CONDITION = ")
                        .append(locProfile.getDispatchCriteria().getCondition()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_WORD_COUNT = ")
                        .append(locProfile.getDispatchCriteria().getVolume().getVolume())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_INTERVAL = ")
                        .append(locProfile.getDispatchCriteria().getTimer().getInterval())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_TIME_UNIT = ")
                        .append(locProfile.getDispatchCriteria().getTimer().getUnit())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_ABSOLUTE_DAYS = ")
                        .append(locProfile.getDispatchCriteria().getTimer().getDaysAsString())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_TIMER_TYPE = ")
                        .append(locProfile.getDispatchCriteria().getTimer().getTimerType())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".DISPATCH_START_TIME = ")
                        .append(locProfile.getDispatchCriteria().getTimer().getStartTime())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".IS_SCRIPT_RUN_AT_JOB_CREATION = ")
                        .append(locProfile.runScriptAtJobCreation()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".JOB_CREATION_SCRIPT_NAME = ")
                        .append(locProfile.getJobCreationScriptName()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".TM_CHOICE = ").append(locProfile.getTmChoice()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".IS_EXACT_MATCH_EDIT = ").append(locProfile.isExactMatchEditing())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".TM_EDIT_TYPE = ").append(locProfile.getTMEditType())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.")
                        .append(locProfile.getId())
                        .append(".JOB_EXCLUDE_TU_TYPES = ")
                        .append(locProfile.getTranslationMemoryProfile()
                                .getJobExcludeTuTypesAsString()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".WF_STATE_POST_ID = ").append(locProfile.getWfStatePostId())
                        .append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".IS_ACTIVE = ").append(locProfile.getIsActive()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".COMPANYID = ").append(locProfile.getCompanyId()).append(NEW_LINE);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".TM_PROFILE_ID = ")
                        .append(locProfile.getTranslationMemoryProfile().getId()).append(NEW_LINE);
                Set<WorkflowTemplateInfo> wftiList = locProfile.getWorkflowTemplates();
                StringBuffer sb = new StringBuffer();
                for (WorkflowTemplateInfo wfti : wftiList)
                {
                    sb.append(wfti.getId()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                buffer.append("LocalizationProfile.").append(locProfile.getId())
                        .append(".WORKFLOW_TEMPLATE_IDS = ").append(sb).append(NEW_LINE);
                buffer.append("##LocalizationProfile.").append(locProfile.getCompanyId())
                        .append(".").append(locProfile.getId()).append(".end").append(NEW_LINE)
                        .append(NEW_LINE);

                writeToFile(locPropertyFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return locPropertyFile;
    }

    private static void writeToFile(File locPropertyFile, byte[] bytes)
    {
        locPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(locPropertyFile, true);
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
