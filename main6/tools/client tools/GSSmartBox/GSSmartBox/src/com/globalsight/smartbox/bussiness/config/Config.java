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
package com.globalsight.smartbox.bussiness.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import jcifs.smb.SmbFile;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FTPConfiguration;
import com.globalsight.smartbox.bo.SMBConfiguration;
import com.globalsight.smartbox.util.FtpHelper;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;

/**
 * 
 * Configuration when GSSmartBox start
 * 
 * @author leon
 * 
 */
public class Config
{
    // Configuration file path
    private final String CONFIGFILEPATH = System.getProperty("user.dir")
            + File.separator + ConfigConstants.CONFIG_FILE;

    /**
     * Get parameters of configuration and do validate
     * 
     * @return
     */
    public CompanyConfiguration init()
    {
        ResourceBundle r = getResourceBundle();
        if (r == null)
        {
            return null;
        }
        String company = r.getString(ConfigConstants.COMPANY);

        String paramName = ConfigConstants.BASE_DIR;
        String baseDir = r.getString(paramName);
        // Check the base directory exists
        if (!validatePath(baseDir))
        {
            String message = "Configuration error for \"" + paramName
                    + "\" in GSSmartBox.conf.";
            LogUtil.FAILEDLOG.error(message);
            return null;
        }

        paramName = ConfigConstants.INBOX;
        String inbox = getRealPath(paramName, baseDir, r.getString(paramName));
       
        paramName = ConfigConstants.OUTBOX;
        String outbox = getRealPath(paramName, baseDir, r.getString(paramName));

        paramName = ConfigConstants.JOB_CREATING_BOX;
        String jobCreatingBox = getRealPath(paramName, baseDir,
                r.getString(paramName));
        
        paramName = ConfigConstants.INBOX4XLZ;
        String inbox4XLZ ="";
        String jobCreatingBox4XLZ="";
        if (r.containsKey(paramName))
        {
            inbox4XLZ = getRealPath(paramName, baseDir, r.getString(paramName));
            jobCreatingBox4XLZ = getRealPath(paramName, baseDir,
                    r.getString(ConfigConstants.JOB_CREATING_BOX)
                            + File.separator + r.getString(paramName));
        }
        paramName = ConfigConstants.JOB_CREATE_SUCCESSFUL_BOX;
        String jobCreateSuccessfulBox = getRealPath(paramName, baseDir,
                r.getString(paramName));

        paramName = ConfigConstants.FAILED_BOX;
        String failedBox = getRealPath(paramName, baseDir,
                r.getString(paramName));

        paramName = ConfigConstants.TEMP_BOX;
        String tempBox = getRealPath(paramName, baseDir, r.getString(paramName));

        // FTP Configuration
        FTPConfiguration ftpConfiguration = null;
        boolean useFTP = "yes".equals(r.getString(ConfigConstants.USEFTP));
        boolean ftpValidate = true;
        if (useFTP)
        {
            String ftpHost = r.getString(ConfigConstants.FTPHOST);
            String ftpUsername = r.getString(ConfigConstants.FTPUSERNAME);
            String ftpPassword = r.getString(ConfigConstants.FTPPASSWORD);
            String ftpInbox = r.getString(ConfigConstants.FTPINBOX);
            String ftpOutbox = r.getString(ConfigConstants.FTPOUTBOX);
            String ftpFailedbox = r.getString(ConfigConstants.FTPFAILEDBOX);
            int ftpPort = 21;
            if (r.containsKey(ConfigConstants.FTPPORT))
            {
                ftpPort = Integer.valueOf(r.getString(ConfigConstants.FTPPORT));
            }

            ftpValidate = ftpValidate(ftpHost, ftpPort, ftpUsername, ftpPassword,
                    ftpInbox, ftpOutbox, ftpFailedbox);
            if (ftpValidate)
            {
                ftpConfiguration = new FTPConfiguration(useFTP, ftpHost, ftpPort,
                        ftpUsername, ftpPassword, ftpInbox, ftpOutbox,
                        ftpFailedbox);
            }
        }

        // SMB Configuration
        SMBConfiguration smbConfiguration = null;
        boolean useSMB = "yes".equals(r.getString(ConfigConstants.USESMB));
        boolean smbValidate = true;
        if (useSMB)
        {
            String smbHost = r.getString(ConfigConstants.SMBHOST);
            String smbUsername = r.getString(ConfigConstants.SMBUSERNAME);
            String smbPassword = r.getString(ConfigConstants.SMBPASSWORD);

            String temp = "smb://";
            if ("".equals(smbUsername.trim()))
            {
                // no user access
                temp = "smb://" + smbHost;
            }
            else
            {
                // user access
                temp = "smb://" + smbUsername + ":" + smbPassword + "@"
                        + smbHost;
            }

            String smbInbox = temp + r.getString(ConfigConstants.SMBINBOX)
                    + "/";
            String smbOutbox = temp + r.getString(ConfigConstants.SMBOUTBOX)
                    + "/";
            String smbFailedbox = temp
                    + r.getString(ConfigConstants.SMBFAILEDBOX) + "/";
            smbValidate = smbValidate(smbInbox, smbOutbox, smbFailedbox);
            if (smbValidate)
            {
                smbConfiguration = new SMBConfiguration(useFTP, smbInbox,
                        smbOutbox, smbFailedbox);
            }
        }

        if (useFTP && useSMB)
        {
            // Can not use FTP and SMB together
            String message = "Can not use FTP and SMB together.";
            LogUtil.FAILEDLOG.error(message);
            return null;
        }

        // Check all the directory is right
        if (inbox == null || outbox == null || jobCreatingBox == null
                || jobCreateSuccessfulBox == null || failedBox == null
                || tempBox == null || !ftpValidate || !smbValidate)
        {
            return null;
        }

        if (useFTP)
        {
            // Create FTP directory for failedBox and outBox
            failedBox = failedBox + File.separator + "ftp";
            File failedBoxDir = new File(failedBox);
            failedBoxDir.mkdir();
            outbox = outbox + File.separator + "ftp";
            File outboxDir = new File(outbox);
            outboxDir.mkdir();
        }

        if (useSMB)
        {
            // Create SMB directory for failedBox and outBox
            failedBox = failedBox + File.separator + "smb";
            File failedBoxDir = new File(failedBox);
            failedBoxDir.mkdir();
            outbox = outbox + File.separator + "smb";
            File outboxDir = new File(outbox);
            outboxDir.mkdir();
        }

        // Create Import and Export folder under failed box in local
        String failedBoxImport = failedBox + File.separator + "Import";
        File failedBoxImportDir = new File(failedBoxImport);
        failedBoxImportDir.mkdir();
        String failedBoxExport = failedBox + File.separator + "Export";
        File failedBoxExportDir = new File(failedBoxExport);
        failedBoxExportDir.mkdir();

        String host = r.getString(ConfigConstants.HOST);
        String port = r.getString(ConfigConstants.PORT);
        String https = r.getString(ConfigConstants.HTTPS);
        String username = r.getString(ConfigConstants.USERNAME);
        String password = r.getString(ConfigConstants.PASSWORD);
        // Check the url, username and password is right
        boolean webServiceInit = WebClientHelper.init(host, port, https,
                username, password);
        if (!webServiceInit)
        {
            return null;
        }

        String processCase = r.getString(ConfigConstants.PROCESS_CASE);
        String preProcessClass = "com.globalsight.smartbox.bussiness.process."
                + processCase + "PreProcess";
        String postProcessClass = "com.globalsight.smartbox.bussiness.process."
                + processCase + "PostProcess";
        long fileCheckToCreateJobTime = Long.valueOf(r
                .getString(ConfigConstants.FILECHECKTOCREATEJOBTIME)) * 1000;
        long downloadCheckTime = Long.valueOf(r
                .getString(ConfigConstants.DOWNLOADCHECKTIME)) * 1000;

        String sourceLocale = r.getString(ConfigConstants.SOURCELOCALE);
        String targetLocales = r.getString(ConfigConstants.TARGETLOCALE);
        targetLocales = targetLocales.replace(", ", ",");

        String fpConfig = r.getString(ConfigConstants.FPCONFIG);
        Map<String, String> extension2fp = initFileProfileConfiguration(
                ConfigConstants.FPCONFIG, fpConfig);
        if (extension2fp == null)
        {
            return null;
        }

        CompanyConfiguration cpConfig = new CompanyConfiguration(company,
                baseDir, inbox, inbox4XLZ, jobCreatingBox4XLZ, outbox,
                jobCreatingBox, jobCreateSuccessfulBox, failedBox, tempBox,
                preProcessClass, postProcessClass, host, port, https, username,
                password, fileCheckToCreateJobTime, downloadCheckTime,
                sourceLocale, targetLocales, extension2fp, ftpConfiguration,
                smbConfiguration);

        // Set to current configuration
        return cpConfig;
    }

    /**
     * Get ResourceBundle
     * 
     * @return
     */
    private ResourceBundle getResourceBundle()
    {
        ResourceBundle r = null;
        try
        {
            InputStream in = new BufferedInputStream(new FileInputStream(
                    CONFIGFILEPATH));
            r = new PropertyResourceBundle(in);
        }
        catch (FileNotFoundException e)
        {
            String message = "Can not find configuration file--GSSmartBox.conf.";
            LogUtil.fail(message, e);
        }
        catch (IOException e)
        {
            String message = "Can not read configuration file--GSSmartBox.conf.";
            LogUtil.fail(message, e);
        }
        return r;
    }

    /**
     * Get the real path GSSmartBox will use
     * 
     * @param basePath
     * @param path
     * @return
     */
    private String getRealPath(String paramName, String basePath, String path)
    {
        String newPath = path;
        if (!validatePath(path))
        {
            newPath = basePath + File.separator + path;
            File file = new File(newPath);
            if (!file.exists())
            {
                if (!file.mkdir())
                {
                    String message = "Configuration error for \"" + paramName
                            + "\" in GSSmartBox.conf.";
                    LogUtil.FAILEDLOG.error(message);
                    newPath = null;
                }
            }
        }
        return newPath;
    }

    /**
     * validate the path exists
     * 
     * @param path
     * @return
     */
    private boolean validatePath(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    /**
     * Init file profile config
     */
    private Map<String, String> initFileProfileConfiguration(String paramName,
            String fpConfig)
    {
        Map<String, String> extension2fp = new HashMap<String, String>();
        try
        {
            if (fpConfig != null && !"".equals(fpConfig))
            {
                String[] fpcs = fpConfig.split(",");
                for (int i = 0; i < fpcs.length; i++)
                {
                    String[] e2fp = fpcs[i].split("\\|");
                    String extension = e2fp[0].trim();
                    String fp = e2fp[1].trim();
                    if (extension2fp.get(extension) != null)
                    {
                        String message = "Configuration error for \""
                                + paramName
                                + "\" in GSSmartBox.conf: duplicate file extensions(."
                                + extension + ")";
                        LogUtil.FAILEDLOG.error(message);
                        return null;
                    }
                    extension2fp.put(extension, fp);
                }
            }
        }
        catch (Exception e)
        {
            String message = "Configuration error for \"" + paramName
                    + "\" in GSSmartBox.conf.";
            LogUtil.FAILEDLOG.error(message);
            return null;
        }

        return extension2fp;
    }

    /**
     * Validate ftp server and ftp directory
     * 
     * @param ftpHost
     * @param ftpPort
     * @param ftpUsername
     * @param ftpPassword
     * @return
     */
    private boolean ftpValidate(String ftpHost, int ftpPort, String ftpUsername,
            String ftpPassword, String ftpInbox, String ftpOutbox,
            String ftpFailedbox)
    {
        // Validate FTP server
        FtpHelper ftpHelper = new FtpHelper(ftpHost, ftpPort,
                ftpUsername, ftpPassword);
        boolean serverAvaliable = ftpHelper.testConnect();
        if (!serverAvaliable)
        {
            return false;
        }
        boolean dirExists = true;

        dirExists = ftpHelper.ftpDirExists(ftpInbox);
        if (!dirExists)
        {
            String message = "Configuration error for \"FTPInbox\" in GSSmartBox.conf.";
            LogUtil.FAILEDLOG.error(message);
            return false;
        }

        dirExists = ftpHelper.ftpDirExists(ftpOutbox);
        if (!dirExists)
        {
            String message = "Configuration error for \"FTPOutbox\" in GSSmartBox.conf.";
            LogUtil.FAILEDLOG.error(message);
            return false;
        }

        dirExists = ftpHelper.ftpDirExists(ftpFailedbox);
        if (!dirExists)
        {
            String message = "Configuration error for \"FTPFailedbox\" in GSSmartBox.conf.";
            LogUtil.FAILEDLOG.error(message);
            return false;
        }
        // Create Import and Export folder unbder failedbox in FTP
        if (!ftpHelper.ftpDirExists(ftpFailedbox + "/Import"))
        {
            ftpHelper.ftpCreateDir(ftpFailedbox + "/Import");
        }
        if (!ftpHelper.ftpDirExists(ftpFailedbox + "/Export"))
        {
            ftpHelper.ftpCreateDir(ftpFailedbox + "/Export");
        }

        return true;
    }

    /**
     * Validate SMB directory
     * 
     * @param ftpHost
     * @param ftpUsername
     * @param ftpPassword
     * @return
     */
    private boolean smbValidate(String smbInbox, String smbOutbox,
            String smbFailedbox)
    {
        try
        {
            // Validate SMB server
            SmbFile sfInbox = new SmbFile(smbInbox);
            if (!sfInbox.exists())
            {
                String message = "Configuration error for \"SMBInbox\" in GSSmartBox.conf, please check the SMB Configuration(host, username, password, SMBInbox).";
                LogUtil.FAILEDLOG.error(message);
                return false;
            }

            SmbFile sfOutbox = new SmbFile(smbOutbox);
            if (!sfOutbox.exists())
            {
                String message = "Configuration error for \"SMBOutbox\" in GSSmartBox.conf, please check the SMB Configuration(host, username, password, SMBOutbox).";
                LogUtil.FAILEDLOG.error(message);
                return false;
            }

            SmbFile sfFailedbox = new SmbFile(smbFailedbox);
            if (!sfFailedbox.exists())
            {
                String message = "Configuration error for \"SMBFailedbox\" in GSSmartBox.conf, please check the SMB Configuration(host, username, password, Failedbox).";
                LogUtil.FAILEDLOG.error(message);
                return false;
            }

            // Create Import and Export folder unbder failedbox in SMB
            SmbFile sfFailedboxImport = new SmbFile(smbFailedbox + "/Import");
            if (!sfFailedboxImport.exists())
            {
                sfFailedboxImport.mkdir();
            }
            SmbFile sfFailedboxExport = new SmbFile(smbFailedbox + "/Export");
            if (!sfFailedboxExport.exists())
            {
                sfFailedboxExport.mkdir();
            }
        }
        catch (Exception e)
        {
            String message = "SMB Config error.";
            LogUtil.FAILEDLOG.error(message);
            return false;
        }

        return true;
    }
}
