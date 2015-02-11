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
package com.globalsight.smartbox.bo;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Configuration for company
 * 
 * @author leon
 * 
 */
public class CompanyConfiguration
{
    private String company = "";
    private String baseDir = "";
    private String inbox = "";
    private String outbox = "";
    private String jobCreatingBox = "";
    private String jobCreateSuccessfulBox = "";
    private String failedBox = "";
    private String tempBox = "";

    private String preProcessClass = "";
    private String postProcessClass = "";

    private String host = "";
    private String port = "";
    private String https = "off";
    private String username = "";
    private String password = "";

    // Time to check InBox folder to create job
    private long fileCheckToCreateJobTime;
    // Time to check job status(Exported) to download
    private long downloadCheckTime;

    private String sourceLocale = "";
    private String targetLocale = "";

    private FTPConfiguration ftpConfig = null;

    private SMBConfiguration smbConfig = null;

    // File Profile
    private Map<String, String> extension2fp = new HashMap<String, String>();

    // Time to check job status(successful)
    private final long jobSuccessfulCheckTime = 30000;

    public CompanyConfiguration(String company, String baseDir, String inbox,
            String outbox, String jobCreatingBox,
            String jobCreateSuccessfulBox, String failedBox, String tempBox,
            String preProcessClass, String postProcessClass, String host,
            String port, String https, String username, String password,
            long fileCheckToCreateJobTime, long downloadCheckTime,
            String sourceLocale, String targetLocale,
            Map<String, String> extension2fp, FTPConfiguration ftpConfig,
            SMBConfiguration smbConfig)
    {
        this.company = company;
        this.baseDir = baseDir;
        this.inbox = inbox;
        this.outbox = outbox;
        this.jobCreatingBox = jobCreatingBox;
        this.jobCreateSuccessfulBox = jobCreateSuccessfulBox;
        this.failedBox = failedBox;
        this.tempBox = tempBox;
        this.preProcessClass = preProcessClass;
        this.postProcessClass = postProcessClass;
        this.host = host;
        this.port = port;
        this.https = https;
        this.username = username;
        this.password = password;
        this.fileCheckToCreateJobTime = fileCheckToCreateJobTime;
        this.downloadCheckTime = downloadCheckTime;
        this.extension2fp = extension2fp;
        this.sourceLocale = sourceLocale;
        this.targetLocale = targetLocale;
        this.ftpConfig = ftpConfig;
        this.smbConfig = smbConfig;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(String baseDir)
    {
        this.baseDir = baseDir;
    }

    public String getInbox()
    {
        return inbox;
    }

    public void setInbox(String inbox)
    {
        this.inbox = inbox;
    }

    public String getOutbox()
    {
        return outbox;
    }

    public void setOutbox(String outbox)
    {
        this.outbox = outbox;
    }

    public String getJobCreatingBox()
    {
        return jobCreatingBox;
    }

    public void setJobCreatingBox(String jobCreatingBox)
    {
        this.jobCreatingBox = jobCreatingBox;
    }

    public String getJobCreateSuccessfulBox()
    {
        return jobCreateSuccessfulBox;
    }

    public void setJobCreateSuccessfulBox(String jobCreateSuccessfulBox)
    {
        this.jobCreateSuccessfulBox = jobCreateSuccessfulBox;
    }

    public String getFailedBox()
    {
        return failedBox;
    }

    public void setFailedBox(String failedBox)
    {
        this.failedBox = failedBox;
    }

    public String getTempBox()
    {
        return tempBox;
    }

    public void setTempBox(String tempBox)
    {
        this.tempBox = tempBox;
    }

    public String getPreProcessClass()
    {
        return preProcessClass;
    }

    public void setPreProcessClass(String preProcessClass)
    {
        this.preProcessClass = preProcessClass;
    }

    public String getPostProcessClass()
    {
        return postProcessClass;
    }

    public void setPostProcessClass(String postProcessClass)
    {
        this.postProcessClass = postProcessClass;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public String getHttps()
    {
        return https;
    }

    public void setHttps(String https)
    {
        this.https = https;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public long getFileCheckToCreateJobTime()
    {
        return fileCheckToCreateJobTime;
    }

    public void setFileCheckToCreateJobTime(long fileCheckToCreateJobTime)
    {
        this.fileCheckToCreateJobTime = fileCheckToCreateJobTime;
    }

    public long getDownloadCheckTime()
    {
        return downloadCheckTime;
    }

    public void setDownloadCheckTime(long downloadCheckTime)
    {
        this.downloadCheckTime = downloadCheckTime;
    }

    public long getJobSuccessfulCheckTime()
    {
        return jobSuccessfulCheckTime;
    }

    public String getSourceLocale()
    {
        return sourceLocale;
    }

    public void setSourceLocale(String sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public Map<String, String> getExtension2fp()
    {
        return extension2fp;
    }

    public void setExtension2fp(Map<String, String> extension2fp)
    {
        this.extension2fp = extension2fp;
    }

    public FTPConfiguration getFtpConfig()
    {
        return ftpConfig;
    }

    public void setFtpConfig(FTPConfiguration ftpConfig)
    {
        this.ftpConfig = ftpConfig;
    }

    public SMBConfiguration getSmbConfig()
    {
        return smbConfig;
    }

    public void setSmbConfig(SMBConfiguration smbConfig)
    {
        this.smbConfig = smbConfig;
    }
}