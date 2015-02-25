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

import org.apache.log4j.Logger;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

public class DITAQACheckerHelper
{
    private static final Logger logger = Logger.getLogger(DITAQACheckerHelper.class);

    /**
     * Get the DITA report file. If the report has been generated, return it; if
     * not exist, generate then return it.
     * 
     * @param p_task
     * @return
     * @throws Exception
     */
    public static File getDitaReportFile(TaskImpl p_task) throws Exception
    {
        File wanted = null;

        // For DITA task, the report file should have been generated before
        // accept. For normal task, it will not.
        File reportDir = DITAQACheckerHelper.getReportFileDir(p_task);
        if (reportDir.exists() && reportDir.isDirectory())
        {
            File[] files = reportDir.listFiles();
            if (files != null && files.length > 0)
            {
                wanted = files[0];
            }
        }

        if (wanted == null || !wanted.isFile())
        {
            DITAQAChecker qaChecker = new DITAQAChecker();
            wanted = qaChecker.runQAChecksAndGenerateReport(p_task.getId());
        }

        return wanted;
    }

    /**
     * The full path is like:
     * "[FStorage]\$$CompanyName$$\GlobalSight\Reports\DITAQAChecksReport\871\zh_CN\ditaReview_1235"
     * 
     * "871" is job ID, "ditaReview_1235" means activity name and task ID.
     */
    public static File getReportFileDir(Task p_task)
    {
        File reportDirFile = AmbFileStoragePathUtils.getReportsDir(p_task
                .getCompanyId());
        String taskName = p_task.getTaskName();
        if (taskName.lastIndexOf("_") > 0)
        {
            taskName = taskName.substring(0, taskName.lastIndexOf("_"));
        }
        taskName += "_" + p_task.getId();

        StringBuffer result = new StringBuffer();
        result.append(reportDirFile.getAbsolutePath());
        result.append(File.separator);
        result.append(DITAQAChecker.DITA_QA_CHECKS_REPORT);
        result.append(File.separator);
        result.append(p_task.getJobId());
        result.append(File.separator);
        result.append(p_task.getTargetLocale().toString());
        result.append(File.separator);
        result.append(taskName);

        File dirFile = new File(result.toString());
        dirFile.mkdirs();

        return dirFile;
    }

    /**
     * Check if current task should run DITA QA checks automatically (before
     * task is accepted).
     */
    public static boolean isDitaQaActivity(Task p_task)
    {
        return isDITAQAActivity(p_task.getTaskName());
    }

    public static boolean isDitaQaActivity(TaskInstance taskInstance)
    {
        String activityName = WorkflowJbpmUtil.getActivityName(taskInstance
                .getName());

        return isDITAQAActivity(activityName);
    }

    private static boolean isDITAQAActivity(String activityName)
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
        if (activity.getRunDitaQAChecks())
        {
            return true;
        }
        return false;
    }

    public static boolean isShowDITAChecksTab(Task p_task)
    {
        if (p_task == null) return false;

        Company company = CompanyWrapper.getCompanyById(p_task.getCompanyId());
        Project project = p_task.getWorkflow().getJob().getProject();
        return company.getEnableDitaChecks()
                && (project.getManualRunDitaChecks() || DITAQACheckerHelper
                        .isDitaQaActivity(p_task));
    }
}
