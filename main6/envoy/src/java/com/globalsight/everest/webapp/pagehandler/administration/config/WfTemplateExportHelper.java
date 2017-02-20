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
import java.util.List;
import java.util.Set;

import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;

/**
 * Exports workflows.
 */
public class WfTemplateExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Workflows");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = WORKFLOW_TEMPLATE_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets workflows info.
     */
    public static File propertiesInputWfTemplate(File wfPropertyFile, String wfId)
    {
        try
        {
            WorkflowTemplateInfo wfti = ServerProxy.getProjectHandler()
                    .getWorkflowTemplateInfoById(Long.parseLong(wfId));
            StringBuffer buffer = new StringBuffer();
            if (wfti != null)
            {
                buffer.append("##WorkflowTemplateInfo.").append(wfti.getCompanyId()).append(".")
                        .append(wfti.getId()).append(".begin").append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".ID = ")
                        .append(wfti.getId()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".NAME = ")
                        .append(wfti.getName()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".DESCRIPTION = ").append(wfti.getDescription()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".PROJECT_ID = ").append(wfti.getProject().getId())
                        .append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".SOURCE_LOCALE_ID = ").append(wfti.getSourceLocale().getId())
                        .append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".TARGET_LOCALE_ID = ").append(wfti.getTargetLocale().getId())
                        .append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".CHAR_SET = ")
                        .append(wfti.getCodeSet()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".IFLOW_TEMPLATE_ID = ").append(wfti.getWorkflowTemplateId())
                        .append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".IS_ACTIVE = ")
                        .append(wfti.getIsActive()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".NOTIFY_PM = ")
                        .append(wfti.isNotifyPm()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".TYPE = ")
                        .append(wfti.getWorkflowType()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId()).append(".COMPANYID = ")
                        .append(wfti.getCompanyId()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".SCORECARD_SHOWTYPE = ").append(wfti.getScorecardShowType())
                        .append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.")
                        .append(wfti.getId())
                        .append(".PERPLEXITY_ID = ")
                        .append(wfti.getPerplexityService() != null ? wfti.getPerplexityService()
                                .getId() : null).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".PERPLEXITY_KEY = ").append(wfti.getPerplexityKey())
                        .append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".PERPLEXITY_SOURCE_THRESHOLD = ")
                        .append(wfti.getPerplexitySourceThreshold()).append(NEW_LINE);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".PERPLEXITY_TARGET_THRESHOLD = ")
                        .append(wfti.getPerplexityTargetThreshold()).append(NEW_LINE);
                // exports workflow managers
                List<String> wfms = wfti.getWorkflowManagerIds();
                StringBuffer wfmIds = new StringBuffer();
                for (String wfm : wfms)
                {
                    wfmIds.append(wfm).append(",");
                }
                if (wfmIds.length() > 0)
                    wfmIds.deleteCharAt(wfmIds.length() - 1);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".WORKFLOW_MANAGER_ID = ").append(wfmIds).append(NEW_LINE);
                // exports leverage locales
                Set<GlobalSightLocale> loclaes = wfti.getLeveragingLocales();
                StringBuffer localeIds = new StringBuffer();
                for (GlobalSightLocale locale : loclaes)
                {
                    localeIds.append(locale.getId()).append(",");
                }
                if (localeIds.length() > 0)
                    localeIds.deleteCharAt(localeIds.length() - 1);
                buffer.append("WorkflowTemplateInfo.").append(wfti.getId())
                        .append(".LEVERAGE_LOCALES = ").append(localeIds).append(NEW_LINE);
                buffer.append("##WorkflowTemplateInfo.").append(wfti.getCompanyId()).append(".")
                        .append(wfti.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                WorkflowTemplate workflowTemplate = WorkflowTemplateHandlerHelper
                        .getWorkflowTemplateById(wfti.getWorkflowTemplateId());
                wfti.setWorkflowTemplate(workflowTemplate);
                writeToFile(wfPropertyFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return wfPropertyFile;
    }

    private static void writeToFile(File wfPropertyFile, byte[] bytes)
    {
        wfPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(wfPropertyFile, true);
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
