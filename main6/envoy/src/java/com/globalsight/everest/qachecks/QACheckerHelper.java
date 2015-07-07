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
package com.globalsight.everest.qachecks;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

public class QACheckerHelper
{
    private static final Logger logger = Logger
            .getLogger(QACheckerHelper.class);

    public static int findMatches(String content, String check, boolean isRE)
    {
        int matches = 0;

        if (isRE)
        {
            Pattern p = Pattern.compile(check);
            Matcher m = p.matcher(content);
            while (m.find())
            {
                matches++;
            }
        }
        else
        {
            while (content.contains(check))
            {
                matches++;
                int index = content.indexOf(check);
                content = content.substring(0, index)
                        + content.substring(index + 1);
            }
        }

        return matches;
    }

    public static File getQAReportFile(Task p_task)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getQAReportPath(p_task));
        sb.append(getQAReportName(p_task));

        return new File(sb.toString());
    }

    public static File getQAReportWorkflowFolder(Task p_task)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getQAReportWorkflowPath(p_task));

        return new File(sb.toString());
    }

    public static String getQAReportWorkflowPath(Task p_task)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(AmbFileStoragePathUtils.getReportsDir(p_task.getCompanyId()));
        sb.append(File.separator);
        sb.append(ReportConstants.REPORT_QA_CHECKS_REPORT);
        sb.append(File.separator);
        sb.append(p_task.getJobId());
        sb.append(File.separator);
        sb.append(p_task.getWorkflow().getTargetLocale().toString());
        sb.append(File.separator);

        return sb.toString();
    }

    public static String getQAReportPath(Task p_task)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getQAReportWorkflowPath(p_task));

        sb.append(p_task.getTaskDisplayName());
        sb.append("_");
        sb.append(p_task.getId());
        sb.append(File.separator);

        return sb.toString();
    }

    public static String getQAReportName(Task p_task)
    {
        StringBuilder sb = new StringBuilder();
        String dateSuffix = new SimpleDateFormat("yyyyMMdd HHmmss")
		.format(new Date());
        
        sb.append(ReportConstants.REPORT_QA_CHECKS_REPORT);
        sb.append("_");
        sb.append(p_task.getJobName());
        sb.append("_");
        sb.append(p_task.getTaskDisplayName());
        sb.append("-");
        sb.append(p_task.getTargetLocale().toString());
        sb.append("-").append(dateSuffix);
        sb.append(ReportConstants.EXTENSION_XLSX);

        return sb.toString();
    }

    public static boolean isQAActivity(Task p_task)
    {
        return isQAActivity(p_task.getTaskName());
    }

    public static boolean isQAActivity(TaskInstance taskInstance)
    {
        String activityName = WorkflowJbpmUtil.getActivityName(taskInstance
                .getName());

        return isQAActivity(activityName);
    }

    private static boolean isQAActivity(String activityName)
    {
        Activity activity = null;
        try
        {
            activity = ServerProxy.getJobHandler().getActivity(activityName);
        }
        catch (Exception e)
        {
            logger.error("An error occurred while finding activity "
                    + activityName);
            return false;
        }
        if (activity == null)
        {
            logger.error("Can not find activity by name: " + activityName);
            return false;
        }
        if (activity.getQaChecks())
        {
            return true;
        }
        return false;
    }

    public static boolean isShowQAChecksTab(Task p_task)
    {
        if (p_task == null) return false;

        Company company = CompanyWrapper.getCompanyById(p_task.getCompanyId());
        Project project = p_task.getWorkflow().getJob().getProject();
        return company.getEnableQAChecks()
                && (project.getAllowManualQAChecks() || QACheckerHelper
                        .isQAActivity(p_task));
    }
}
