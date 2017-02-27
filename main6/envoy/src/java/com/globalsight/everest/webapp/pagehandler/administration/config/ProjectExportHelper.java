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

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports projects.
 */
public class ProjectExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Projects");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = PROJECT_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets projects info.
     */
    public static File propertiesInputProject(File projectFile, String projectId)
    {
        try
        {
            ProjectImpl project = (ProjectImpl) ServerProxy.getProjectHandler().getProjectById(
                    Long.parseLong(projectId));;
            StringBuffer buffer = new StringBuffer();
            if (project != null)
            {
                buffer.append("##Project.").append(project.getCompanyId()).append(".")
                        .append(project.getId()).append(".begin").append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".PROJECT_SEQ = ")
                        .append(project.getId()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".PROJECT_NAME = ")
                        .append(project.getName()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".DESCRIPTION = ")
                        .append(project.getDescription()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".MANAGER_USER_ID = ")
                        .append(project.getProjectManagerId()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".TERMBASE_NAME = ")
                        .append(project.getTermbaseName()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".QUOTE_USER_ID = ")
                        .append(project.getQuotePersonId()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".COMPANYID = ")
                        .append(project.getCompanyId()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".IS_ACTIVE = ")
                        .append(project.getIsActive()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".PMCOST = ")
                        .append(project.getPMCost()).append(NEW_LINE);
                AttributeSet attrSet = project.getAttributeSet();
                String attrSetName = null;
                if (attrSet != null)
                    attrSetName = attrSet.getName();
                buffer.append("Project.").append(project.getId()).append(".ATTRIBUTE_SET_NAME = ")
                        .append(attrSetName).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".POREQUIRED = ")
                        .append(project.getPoRequired()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".AUTO_ACCEPT_TRANS = ")
                        .append(project.getAutoAcceptTrans()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".AUTO_SEND_TRANS = ")
                        .append(project.getAutoSendTrans()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".REVIEWONLYAUTOACCEPT = ")
                        .append(project.getReviewOnlyAutoAccept()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".REVIEWONLYAUTOSEND = ")
                        .append(project.getReviewOnlyAutoSend()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".REVIEW_REPORT_INCLUDE_COMPACT_TAGS = ")
                        .append(project.isReviewReportIncludeCompactTags()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".AUTOACCEPTPMTASK = ")
                        .append(project.getAutoAcceptPMTask()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".CHECK_UNTRANSLATED_SEGMENTS = ")
                        .append(project.isCheckUnTranslatedSegments()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".SAVE_TRANSLATIONS_EDIT_REPORT = ")
                        .append(project.getSaveTranslationsEditReport()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".SAVE_REVIEWERS_COMMENTS_REPORT = ")
                        .append(project.getSaveReviewersCommentsReport()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".SAVE_OFFLINE_FILES = ")
                        .append(project.getSaveOfflineFiles()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".ALLOW_MANUAL_QA_CHECKS = ")
                        .append(project.getAllowManualQAChecks()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".AUTO_ACCEPT_QA_TASK = ")
                        .append(project.getAutoAcceptQATask()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId()).append(".AUTO_SEND_QA_REPORT = ")
                        .append(project.getAutoSendQAReport()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".MANUAL_RUN_DITA_CHECKS = ")
                        .append(project.getManualRunDitaChecks()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".AUTO_ACCEPT_DITA_QA_TASK = ")
                        .append(project.getAutoAcceptDitaQaTask()).append(NEW_LINE);
                buffer.append("Project.").append(project.getId())
                        .append(".AUTO_SEND_DITA_QA_REPORT = ")
                        .append(project.getAutoSendDitaQaReport()).append(NEW_LINE);
                Set<String> userIds = project.getUserIds();
                StringBuffer sb = new StringBuffer();
                for (String userId : userIds)
                {
                    sb.append(userId).append(",");
                }
                if (sb.length() > 1)
                    sb.deleteCharAt(sb.length() - 1);
                buffer.append("Project.").append(project.getId()).append(".PROJECT_USER = ")
                        .append(sb).append(NEW_LINE);
                buffer.append("##Project.").append(project.getCompanyId()).append(".")
                        .append(project.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(projectFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return projectFile;
    }

    private static void writeToFile(File projectPropertyFile, byte[] bytes)
    {
        projectPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(projectPropertyFile, true);
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
