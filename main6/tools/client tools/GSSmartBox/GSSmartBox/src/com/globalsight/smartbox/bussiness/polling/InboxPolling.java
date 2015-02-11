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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.BoxUtil;
import com.globalsight.smartbox.util.LockFile;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;

/**
 * 
 * Polling inbox folder to get files to create job
 * 
 * @author Leon
 * 
 */
public class InboxPolling implements Polling
{
    private CompanyConfiguration cpConfig;
    private File jobCreatingRecord = new File(System.getProperty("user.dir")
            + File.separator + "logs" + File.separator + "jobCreating.log");
    private final long fileCheckToCreateJobTime;
    private String companyName;
    private String preProcessClass;
    private String host;
    private String port;
    private String https;
    private String username;
    private String password;
    private boolean polling = true;
    private Thread thread = null;
    private String suspendCommitJob = null;

    private List<JobInfo> creatingJobInfos;
    private List<JobInfo> failedJobInfos;

    public InboxPolling(CompanyConfiguration cpConfig)
    {
        this.cpConfig = cpConfig;
        fileCheckToCreateJobTime = cpConfig.getFileCheckToCreateJobTime();
        companyName = cpConfig.getCompany();
        preProcessClass = cpConfig.getPreProcessClass();
        host = cpConfig.getHost();
        port = cpConfig.getPort();
        https = cpConfig.getHttps();
        username = cpConfig.getUsername();
        password = cpConfig.getPassword();
        suspendCommitJob = cpConfig.getSuspendCommitJob();
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
                    if ("YES".equalsIgnoreCase(suspendCommitJob))
                    {
                        boolean serverStatus = WebClientHelper
                                .isServerImportingOrExporting();
                        if (!serverStatus)
                        {
                            inboxPollingProcess();
                        }
                    }
                    else
                    {
                        inboxPollingProcess();
                    }

                    try
                    {
                        Thread.sleep(fileCheckToCreateJobTime);
                    }
                    catch (InterruptedException e)
                    {
                        String message = "InterruptedException from Inbox Polling.";
                        LogUtil.fail(message, e);
                    }
                }
            }
        };
        
        thread = new Thread(runnable);
        thread.setName("InboxPolling: " + companyName);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                new InboxPolling(cpConfig).start();
            }
        });
        thread.start();
    }

    @Override
    public void stop()
    {
        polling = false;
    }

    /**
     * Process for InBox Polling
     * 
     * @return
     * @throws Exception
     */
    private void inboxPollingProcess()
    {
        boolean init = WebClientHelper.init(host, port, https, username, password);
        if (!init)
        {
            return;
        }

        List<File> filesJobCreating = BoxUtil.moveFilesFromInboxToJobCreatingBox(cpConfig);
        creatingJobInfos = new ArrayList<JobInfo>();
        failedJobInfos = new ArrayList<JobInfo>();

        // Create job
        createJob(filesJobCreating);
        // Move files to failedBox for failed jobs
        BoxUtil.moveFilesFromCreatingBoxToFailedBox(cpConfig, failedJobInfos);
        // Write creating job info to record file
        writeRecordFile();
       
        // Delete temp directory during create job
        BoxUtil.deleteSubFile(cpConfig.getTempBox());
        //BoxUtil.deleteTempDir(creatingJobInfos, failedJobInfos);
    }

    /**
     * Create job
     * 
     * @param filesJobCreating
     *            Files List for Job Creating
     */
    private void createJob(List<File> filesJobCreating)
    {
        for (File file : filesJobCreating)
        {
            String fileName = file.getName();
            // Invoke PreProcess class
            JobInfo jobInfo = null;
            try
            {
                LogUtil.info("File handing by prepare process class: " + fileName);
                Class<?> preProcess = Class.forName(preProcessClass);
                Method processMethod = preProcess.getDeclaredMethod("process",
                        String.class, CompanyConfiguration.class);
                jobInfo = (JobInfo) processMethod.invoke(
                        preProcess.newInstance(), file.getPath(), cpConfig);
            }
            catch (Exception e)
            {
                String message = "Exception from prepare process class mapping: "
                        + cpConfig.getPreProcessClass()
                        + ";Failed to create job, FileName: " + fileName;
                LogUtil.fail(message, e);
                failedJobInfos.add(jobInfo);
                continue;
            }

            if (jobInfo.isFailedFlag())
            {
                // File format or file handing error
                failedJobInfos.add(jobInfo);
                continue;
            }

            LogUtil.info("File handing successfully: " + fileName);
            Vector<String> sourceFiles = jobInfo.getSourceFiles();
            HashMap<String, String> sourceMap = jobInfo.getSourceMap();
            if (sourceMap.size() > 0 && sourceMap.size() < sourceFiles.size())
            {
                LogUtil.info("File find some need to be checked in: " + fileName);
                Vector<String> failFiles = new Vector<String>();
                JobInfo failInfo = new JobInfo();
                failInfo.setFailedFlag(true);
                failInfo.setJobName(jobInfo.getJobName());
                failInfo.setOriginFile(jobInfo.getOriginFile());
                for (Iterator it = sourceFiles.iterator(); it.hasNext();)
                {
                    String sf = (String) it.next();
                    if (null == sourceMap.get(sf))
                    {
                        failFiles.add(sf);
                        it.remove();
                    }
                }

                failInfo.setSourceFiles(failFiles);
                failedJobInfos.add(failInfo);
            }
            
            LogUtil.info("Starting to create job: " + fileName);
            boolean createJobSuccess = WebClientHelper.createJob(jobInfo);
            if (!createJobSuccess)
            {
                failedJobInfos.add(jobInfo);
                continue;
            }
            LogUtil.info("Job is in creating in GlobalSight Server: " + fileName);
            creatingJobInfos.add(jobInfo);
        }
    }

    /**
     * Write creating job info to jobCreating.log file
     * 
     * @param jobInfos
     */
    private void writeRecordFile()
    {
        // Write creating job record to record file
        try
        {
            if (!jobCreatingRecord.exists())
            {
                jobCreatingRecord.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(jobCreatingRecord, "rws");
            // get the lock and lock this file
            FileLock fileLock = LockFile.getFileLock(raf);
            raf.seek(raf.length());
            for (JobInfo jobInfo : creatingJobInfos)
            {
                StringBuffer str = new StringBuffer();
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
            String message = "File read/write error.Exception from writing job creating record file.";
            LogUtil.fail(message, e);
        }
    }
}