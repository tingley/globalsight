/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.bussiness.polling;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.BoxUtil;
import com.globalsight.smartbox.util.LockFile;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;

/**
 * Polling jobCreating.log to get job list in creating, then polling GlobalSight
 * to check job status
 * 
 * @author leon
 * 
 */
public class JobCreatePolling implements Polling
{
    private CompanyConfiguration cpConfig;
    private File jobCreatingRecord = new File(System.getProperty("user.dir")
            + File.separator + "logs" + File.separator + "jobCreating.log");
    private File jobCreatingSuccessfulRecord = new File(
            System.getProperty("user.dir") + File.separator + "logs"
                    + File.separator + "jobCreateSuccessful.log");
    private final long jobSuccessfulCheckTime;
    private String companyName;
    private String host;
    private String port;
    private String https;
    private String username;
    private String password;
    private boolean polling = true;
    private Thread thread = null;

    private List<JobInfo> jobInfosInCreating;
    private List<JobInfo> jobInfosCreateSuccessful;
    private List<JobInfo> jobInfosCreateFailed;

    public JobCreatePolling(CompanyConfiguration cpConfig)
    {
        this.cpConfig = cpConfig;
        jobSuccessfulCheckTime = cpConfig.getJobSuccessfulCheckTime();
        companyName = cpConfig.getCompany();
        host = cpConfig.getHost();
        port = cpConfig.getPort();
        https = cpConfig.getHttps();
        username = cpConfig.getUsername();
        password = cpConfig.getPassword();
    }

    @Override
    public void start()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                while (polling)
                {
                    jobCreatePollingProcess();
                    try
                    {
                        Thread.sleep(jobSuccessfulCheckTime);
                    }
                    catch (InterruptedException e)
                    {
                        String message = "InterruptedException from Job Create Polling.";
                        LogUtil.fail(message, e);
                    }
                }
            }
        };
        thread = new Thread(runnable);
        thread.setName("JobCreateSuccessfulPolling: " + companyName);
        thread.start();
    }

    @Override
    public void stop()
    {
        polling = false;
    }

    /**
     * Process for job Create polling
     * 
     * 1. Read jobCreating.log to get job list, then polling job status from GS
     * Server, update job list in creating and write to jobCreating.log
     * 
     * 2. Write successful jobs to jobCreateSuccessful.log, move original file
     * from JobCreatingBox to JobCreateSuccessfulBox
     * 
     * 3. Move failed jobs from JobCreatingBox to failedBox/Import
     */
    private void jobCreatePollingProcess()
    {
        jobInfosInCreating = new ArrayList<JobInfo>();
        jobInfosCreateSuccessful = new ArrayList<JobInfo>();
        jobInfosCreateFailed = new ArrayList<JobInfo>();

        updateJobCreatingRecord();
        BoxUtil.moveFilesFromCreatingBoxToSuccessfulBox(cpConfig,
                jobInfosCreateSuccessful);
        updateJobCreateSuccessRecord();
        BoxUtil.moveFilesFromCreatingBoxToFailedBox(cpConfig,
                jobInfosCreateFailed);
    }

    /**
     * Read jobCreating.log to get job list, then polling job status from GS
     * Server, update job list in creating and write to jobCreating.log.
     */
    private void updateJobCreatingRecord()
    {
        boolean init = WebClientHelper.init(host, port, https, username,
                password);
        if (!init)
        {
            return;
        }

        try
        {
            RandomAccessFile raf = new RandomAccessFile(jobCreatingRecord,
                    "rws");
            FileLock fileLock = LockFile.getFileLock(raf);

            while (raf.getFilePointer() < raf.length())
            {
                String record = raf.readLine();
                String[] jobRecord = record.split(",");
                JobInfo jobInfo = new JobInfo();
                jobInfo.setJobName(jobRecord[0]);
                jobInfo.setOriginFile(jobRecord[1]);
                File originFilePath = new File(jobInfo.getOriginFile());
                String fileName = originFilePath.getName();
                // Get job status
                boolean getStatus = WebClientHelper.getJobStatus(jobInfo);
                if (!getStatus)
                {
                    jobInfosInCreating.add(jobInfo);
                    continue;
                }

                String status = jobInfo.getStatus();
                String jobMsg = "FileName: " + fileName + ", JobID: "
                        + jobInfo.getId() + ", JobStatus: " + status
                        + ", JobName: " + jobInfo.getJobName();

                if ("IN_QUEUE".equals(status) || "UPLOADING".equals(status)
                        || "EXTRACTING".equals(status)
                        || "LEVERAGING".equals(status)
                        || "PROCESSING".equals(status)
                        || "PENDING".equals(status)
                        || "BATCH_RESERVED".equals(status))
                {
                    // In Creating
                    jobInfosInCreating.add(jobInfo);
                }
                else if ("INITIALIZE_FAILED".equals(status)
                        || "IMPORT_FAILED".equals(status)
                        || "CANCELLED".equals(status))
                {
                    // Create failed
                    jobInfo.setFailedFlag(true);
                    jobInfosCreateFailed.add(jobInfo);

                    LogUtil.JOBSLOG.info("Failed to create job, " + jobMsg);
                }
                else
                {
                    // Create successful
                    jobInfosCreateSuccessful.add(jobInfo);
                    LogUtil.JOBSLOG
                            .info("Job create successfully in GlobalSight Server, "
                                    + jobMsg);
                }
            }
            raf.setLength(0);
            for (JobInfo jobInfo : jobInfosInCreating)
            {
                StringBuffer str = new StringBuffer();
                str.append(jobInfo.getJobName()).append(",");
                str.append(jobInfo.getOriginFile());
                str.append("\r\n");
                raf.write(str.toString().getBytes());
            }
            fileLock.release();
            raf.close();
        }
        catch (IOException e)
        {
            String message = "File read/write error.Exception from writing job creating record file.";
            LogUtil.fail(message, e);
        }
    }

    /**
     * Write successful jobs to jobCreateSuccessful.log, move original file from
     * JobCreatingBox to JobCreateSuccessfulBox
     */
    private void updateJobCreateSuccessRecord()
    {
        // Write job create successful record to record file
        try
        {
            if (!jobCreatingSuccessfulRecord.exists())
            {
                jobCreatingSuccessfulRecord.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(
                    jobCreatingSuccessfulRecord, "rws");
            FileLock fileLock = LockFile.getFileLock(raf);
            // get the lock and lock this file

            raf.seek(raf.length());
            for (JobInfo jobInfo : jobInfosCreateSuccessful)
            {
                StringBuffer str = new StringBuffer();
                str.append(jobInfo.getId()).append(",");
                str.append(jobInfo.getJobName()).append(",");
                str.append(jobInfo.getOriginFile());
                str.append("\r\n");
                raf.write(str.toString().getBytes());
            }
            // unlock and close
            fileLock.release();
            raf.close();
        }
        catch (IOException e)
        {
            String message = "File read/write error.Exception from writing job create successful record file.";
            LogUtil.fail(message, e);
        }
    }
}
