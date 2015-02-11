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
import java.lang.reflect.Method;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FTPConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.bo.SMBConfiguration;
import com.globalsight.smartbox.util.BoxUtil;
import com.globalsight.smartbox.util.LockFile;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;

/**
 * Polling jobCreateSuccessful.log to get successful job list, then polling
 * GlobalSight to check job status
 * 
 * @author Leon
 * 
 */
public class JobDownloadPolling implements Polling
{
    private CompanyConfiguration cpConfig;
    private final long downloadCheckTime;
    private File jobCreatingSuccessfulRecord = new File(
            System.getProperty("user.dir") + File.separator + "logs"
                    + File.separator + "jobCreateSuccessful.log");
    private String failedbox;
    private String outbox;
    private FTPConfiguration ftpConfig;
    private SMBConfiguration smbConfig;
    private String companyName;
    private String postProcessClass;
    private String tempBox;
    private String host;
    private String port;
    private String https;
    private String username;
    private String password;
    private boolean polling = true;
    private Thread thread = null;

    private List<JobInfo> jobInfosCreateSuccess;
    private List<JobInfo> jobInfosCreateFailed;

    public JobDownloadPolling(CompanyConfiguration cpConfig)
    {
        this.cpConfig = cpConfig;
        downloadCheckTime = cpConfig.getDownloadCheckTime();
        companyName = cpConfig.getCompany();
        postProcessClass = cpConfig.getPostProcessClass();
        tempBox = cpConfig.getTempBox();
        host = cpConfig.getHost();
        port = cpConfig.getPort();
        https = cpConfig.getHttps();
        username = cpConfig.getUsername();
        password = cpConfig.getPassword();
        failedbox = cpConfig.getFailedBox();
        outbox = cpConfig.getOutbox();
        ftpConfig = cpConfig.getFtpConfig();
        smbConfig = cpConfig.getSmbConfig();
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
                    jobDownloadProcess();
                    try
                    {
                        Thread.sleep(downloadCheckTime);
                    }
                    catch (InterruptedException e)
                    {
                        String message = "InterruptedException from Job Download Polling.";
                        LogUtil.fail(message, e);
                    }
                }
            }
        };
        thread = new Thread(runnable);
        thread.setName("JobDownloadPolling: " + companyName);
        thread.start();
    }

    @Override
    public void stop()
    {
        polling = false;
    }

    /**
     * Job Download polling
     */
    private void jobDownloadProcess()
    {
        // Get successful job record from record file
        jobInfosCreateSuccess = new ArrayList<JobInfo>();
        jobInfosCreateFailed = new ArrayList<JobInfo>();
        updateSuccessfulJobRecord();
        BoxUtil.moveFilesFromSuccessfulBoxToFailedBox(cpConfig,
                jobInfosCreateFailed);
        if (ftpConfig != null)
        {
            BoxUtil.uploadFailedboxFilesToFTP(failedbox, ftpConfig);
            BoxUtil.uploadOutboxFilesToFTP(outbox, ftpConfig);
            // Clear empty directories
            BoxUtil.clearEmptyDirectories(new File(outbox));
        }

        if (smbConfig != null)
        {
            BoxUtil.uploadFailedboxFilesToSMB(failedbox, smbConfig);
            BoxUtil.uploadOutboxFilesToSMB(outbox, smbConfig);
            // Clear empty directories
            BoxUtil.clearEmptyDirectories(new File(outbox));
        }

    }

    private void updateSuccessfulJobRecord()
    {
        boolean init = WebClientHelper.init(host, port, https, username, password);
        if (!init)
        {
            return;
        }

        try
        {
            RandomAccessFile raf = new RandomAccessFile(
                    jobCreatingSuccessfulRecord, "rws");
            FileLock fileLock = LockFile.getFileLock(raf);

            List<JobInfo> successfulJobRecords = new ArrayList<JobInfo>();
            while (raf.getFilePointer() < raf.length())
            {
                String record = raf.readLine();
                String[] jobRecord = record.split(",");
                JobInfo jobInfo = new JobInfo();
                jobInfo.setId(jobRecord[0]);
                jobInfo.setJobName(jobRecord[1]);
                jobInfo.setOriginFile(jobRecord[2]);
                successfulJobRecords.add(jobInfo);
            }

            // Fetch job status and download exported jobs, then invoke post
            // process class
            fetchJobStatusAndExport(successfulJobRecords);

            raf.setLength(0);
            for (JobInfo jobInfo : jobInfosCreateSuccess)
            {
                StringBuffer str = new StringBuffer();
                str.append(jobInfo.getId()).append(",");
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
            String message = "File read/write error.Exception from writing job creating successful record file.";
            LogUtil.fail(message, e);
        }
    }

    /**
     * Fetch the status of the successful created jobs, if the status is
     * Exported, download the jobs and invoke post process class.
     * 
     * @param successfulJobRecords
     */
    private void fetchJobStatusAndExport(List<JobInfo> successfulJobRecords)
    {
        for (JobInfo jobInfo : successfulJobRecords)
        {
            File originFile = new File(jobInfo.getOriginFile());
            String originFileName = originFile.getName();

            boolean getStatus = WebClientHelper.getJobStatus(jobInfo);
            if (!getStatus)
            {
                jobInfosCreateSuccess.add(jobInfo);
                continue;
            }

            String status = jobInfo.getStatus();
            String jobName = jobInfo.getJobName();
            String jobId = jobInfo.getId();
            String jobMsg = "FileName: " + originFileName + ", JobName: "
                    + jobName + ", JobId: " + jobId;

            if ("EXPORT_FAILED".equals(status) || "CANCELED".equals(status))
            {
                jobInfo.setFailedFlag(true);
                jobInfosCreateFailed.add(jobInfo);
            }
            else if ("EXPORTED".equals(status) || "ARCHIVED".equals(status))
            {
                // Download file from GlobalSight
                LogUtil.info("Start to download file from GlobalSight Server, "
                        + jobMsg);

                StringBuffer server = new StringBuffer();
                server.append("on".equals(https) ? "https://" : "http://");
                server.append(host);
                server.append(":");
                server.append(port);
                server.append("/");

                boolean jobDownload = WebClientHelper.jobDownload(jobInfo,
                        tempBox, server.toString());
                if (!jobDownload)
                {
                    jobInfosCreateSuccess.add(jobInfo);
                    continue;
                }

                LogUtil.info("Download file successfully from GlobalSight Server, "
                        + jobMsg);
                LogUtil.info("Downloaded file handing by post process class: "
                        + originFileName);
                // Invoke post process to handing target files
                try
                {
                    Class<?> postProcess = Class.forName(postProcessClass);
                    Method processMethod = postProcess.getDeclaredMethod(
                            "process", JobInfo.class,
                            CompanyConfiguration.class);
                    boolean processSuccess = (Boolean) processMethod.invoke(
                            postProcess.newInstance(), jobInfo, cpConfig);
                    if (!processSuccess)
                    {
                        jobInfosCreateSuccess.add(jobInfo);
                    }
                    else
                    {
                        // Move file to Outbox, delete unused files
                        boolean moved = BoxUtil.moveFinalResultFilesToOutBox(
                                cpConfig, jobInfo);
                        if (!moved)
                        {
                            jobInfosCreateSuccess.add(jobInfo);
                        }
                        else
                        {
                            LogUtil.JOBSLOG.info("Export job successfully, "
                                    + jobMsg);
                        }
                    }
                }
                catch (Exception e)
                {
                    String message = "Exception from Post Process Class Mapping: "
                            + cpConfig.getPostProcessClass() + ".";
                    LogUtil.fail(message, e);
                    jobInfosCreateSuccess.add(jobInfo);
                }
            }
            else
            {
                jobInfosCreateSuccess.add(jobInfo);
            }
        }
    }
}
