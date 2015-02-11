package com.globalsight.smartbox.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbFile;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FTPConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.bo.SMBConfiguration;

/**
 * Directory box operation
 * 
 * @author leon
 * 
 */
public class BoxUtil
{
    /**
     * Move files from Inbox to JobCreatingBox, used for Inbox Polling
     * 
     * If use FTP, need to download files to locale inbox from FTP
     * 
     * @param cpConfig
     * @return
     */
    public static List<File> moveFilesFromInboxToJobCreatingBox(
            CompanyConfiguration cpConfig)
    {
        String jobCreatingBox = cpConfig.getJobCreatingBox();
        String inBox = cpConfig.getInbox();
        List<File> filesJobCreating = new ArrayList<File>();

        FTPConfiguration ftpConfig = cpConfig.getFtpConfig();
        if (ftpConfig != null)
        {
            downloadFileToInboxFromFTP(inBox, ftpConfig);
        }

        SMBConfiguration smbConfig = cpConfig.getSmbConfig();
        if (smbConfig != null)
        {
            downloadFileToInboxFromSMB(inBox, smbConfig);
        }

        File inbox = new File(inBox);
        File[] files = inbox.listFiles();
        for (File file : files)
        {
            try
            {
                FileUtil.copyFileToDir(jobCreatingBox, file);
            }
            catch (IOException e)
            {
                // The file may be in creating or used by other user
                continue;
            }
            // Delete file in inBox
            FileUtil.deleteFile(file);
            File newFile = new File(jobCreatingBox + File.separator
                    + file.getName());
            filesJobCreating.add(newFile);
        }
        return filesJobCreating;
    }

    /**
     * Download files from FTP to Inbox
     * 
     * @param inBox
     * @param ftpConfig
     */
    private static void downloadFileToInboxFromFTP(String inBox,
            FTPConfiguration ftpConfig)
    {
        String ftpHost = ftpConfig.getFtpHost();
        String ftpUsername = ftpConfig.getFtpUsername();
        String ftpPassword = ftpConfig.getFtpPassword();
        String ftpInbox = ftpConfig.getFtpInbox();
        FtpHelper ftpHelper = new FtpHelper(ftpHost, ftpUsername, ftpPassword);
        boolean ftpInit = ftpHelper.testConnect();
        if (ftpInit)
        {
            List<String> files = new ArrayList<String>();
            files = ftpHelper.ftpList(ftpInbox);
            for (String fileName : files)
            {
                if ((ftpInbox + "/.").equals(fileName)
                        || (ftpInbox + "/..").equals(fileName)
                        || (ftpInbox + "/...").equals(fileName))
                {
                    continue;
                }
                File targetFile = null;
                try
                {
                    InputStream is = ftpHelper.ftpDownloadFile(fileName);
                    if (is == null)
                    {
                        continue;
                    }
                    String name = fileName.substring(fileName.lastIndexOf("/"));
                    targetFile = new File(inBox + File.separator + name);
                    FileUtil.copyFile(targetFile, is);
                    ftpHelper.ftpDelete(fileName);
                }
                catch (IOException e)
                {
                    // The file may be used by other user
                    if (targetFile.exists())
                    {
                        FileUtil.deleteFile(targetFile);
                    }
                    continue;
                }
            }
        }
    }

    /**
     * Download files from SMB to Inbox
     * 
     * @param inBox
     * @param ftpConfig
     */
    private static void downloadFileToInboxFromSMB(String inBox,
            SMBConfiguration smbConfig)
    {
        String smbInbox = smbConfig.getSMBInbox();
        SmbFile smbDir;
        try
        {
            smbDir = new SmbFile(smbInbox);
            SmbFile[] files = smbDir.listFiles();
            for (SmbFile sf : files)
            {
                if (sf.isDirectory())
                {
                    continue;
                }
                SMBHelper.copyFileFromSmbToLocal(sf, inBox);
                sf.delete();
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Move failed file from job creating box to failed(Import) box, used for
     * Inbox Polling and JobCreate Polling
     * 
     * @param cpConfig
     * @param failedJobInfos
     */
    public static void moveFilesFromCreatingBoxToFailedBox(
            CompanyConfiguration cpConfig, List<JobInfo> failedJobInfos)
    {
        String failedBox = cpConfig.getFailedBox();
        String failedImportBox = failedBox + File.separator + "Import";
        moveJobFiles(failedJobInfos, failedImportBox);
    }

    /**
     * Upload files from local failed box to ftp failed box, used for job
     * Download polling
     * 
     * @param cpConfig
     */
    public static void uploadFailedboxFilesToFTP(String failedboxPath,
            FTPConfiguration ftpConfig)
    {
        String ftpHost = ftpConfig.getFtpHost();
        String ftpUsername = ftpConfig.getFtpUsername();
        String ftpPassword = ftpConfig.getFtpPassword();
        String ftpFailedbox = ftpConfig.getFtpFailedbox();
        FtpHelper ftpHelper = new FtpHelper(ftpHost, ftpUsername, ftpPassword);
        boolean ftpInit = ftpHelper.testConnect();
        if (ftpInit)
        {
            File failedBox = new File(failedboxPath);
            String root = failedboxPath.replace("\\", "/");
            File[] files = failedBox.listFiles();
            for (File file : files)
            {
                ftpHelper.ftpUploadDirectory(ftpFailedbox, file, root);
            }

        }
    }

    /**
     * Upload files from local failed box to SMB failed box, used for job
     * Download polling
     * 
     * @param cpConfig
     */
    public static void uploadFailedboxFilesToSMB(String failedboxPath,
            SMBConfiguration smbConfig)
    {
        String smbFailedbox = smbConfig.getSMBFailedbox();
        File failedBox = new File(failedboxPath);
        String root = failedboxPath.replace("\\", "/");
        File[] files = failedBox.listFiles();
        for (File file : files)
        {
            SMBHelper.copyFileFromLocalToSMB(smbFailedbox, file, root);
        }
    }

    /**
     * Upload files from local failed box to ftp failed box, used for job
     * Download polling
     * 
     * @param cpConfig
     */
    public static void uploadOutboxFilesToFTP(String outboxPath,
            FTPConfiguration ftpConfig)
    {
        String ftpHost = ftpConfig.getFtpHost();
        String ftpUsername = ftpConfig.getFtpUsername();
        String ftpPassword = ftpConfig.getFtpPassword();
        String ftpOutbox = ftpConfig.getFtpOutbox();
        FtpHelper ftpHelper = new FtpHelper(ftpHost, ftpUsername, ftpPassword);
        boolean ftpInit = ftpHelper.testConnect();
        if (ftpInit)
        {
            File outBox = new File(outboxPath);
            String root = outboxPath.replace("\\", "/");
            File[] files = outBox.listFiles();
            for (File file : files)
            {
                ftpHelper.ftpUploadDirectory(ftpOutbox, file, root);
            }
        }
    }

    /**
     * Upload files from local failed box to smb failed box, used for job
     * Download polling
     * 
     * @param cpConfig
     */
    public static void uploadOutboxFilesToSMB(String outboxPath,
            SMBConfiguration smbConfig)
    {
        String smbOutbox = smbConfig.getSMBOutbox();
        File outBox = new File(outboxPath);
        String root = outboxPath.replace("\\", "/");
        File[] files = outBox.listFiles();
        for (File file : files)
        {
            SMBHelper.copyFileFromLocalToSMB(smbOutbox, file, root);
        }
    }

    /**
     * Delete temp files, used for Inbox Polling
     */
    public static void deleteTempDir(List<JobInfo> creatingJobInfos,
            List<JobInfo> failedJobInfos)
    {
        List<JobInfo> jobInfos = new ArrayList<JobInfo>();
        jobInfos.addAll(creatingJobInfos);
        jobInfos.addAll(failedJobInfos);
        for (JobInfo jobInfo : jobInfos)
        {
            String tempFile = jobInfo.getTempFile();
            if (tempFile != null)
            {
                FileUtil.deleteFile(tempFile);
            }
        }
    }

    /**
     * Move files from creating box to local Job Create Successful Box, used for
     * JobCreate Polling
     * 
     */
    public static void moveFilesFromCreatingBoxToSuccessfulBox(
            CompanyConfiguration cpConfig,
            List<JobInfo> jobInfosCreateSuccessful)
    {
        String jobCreateSuccessfulBox = cpConfig.getJobCreateSuccessfulBox();
        moveJobFiles(jobInfosCreateSuccessful, jobCreateSuccessfulBox);
    }

    /**
     * Move failed file from successful box to failed(Export) box, used for Job
     * Download Polling
     * 
     */
    public static void moveFilesFromSuccessfulBoxToFailedBox(
            CompanyConfiguration cpConfig, List<JobInfo> failedJobInfos)
    {

        String failedExportBox = cpConfig.getFailedBox();
        moveJobFiles(failedJobInfos, failedExportBox);
    }

    /**
     * Move final result file to Outbox, and delete temp files and target files
     * from GS, used for Download Polling
     * 
     * @param cpConfig
     * @param jobInfo
     * @return
     */
    public static boolean moveFinalResultFilesToOutBox(
            CompanyConfiguration cpConfig, JobInfo jobInfo)
    {
        String outbox = cpConfig.getOutbox();
        String finalResultFile = jobInfo.getFinalResultFile();
        String tempFile = jobInfo.getTempFile();

        try
        {
            FileUtil.copyToDir(outbox, finalResultFile);
            if (tempFile != null)
            {
                FileUtil.deleteFile(tempFile);
            }
        }
        catch (IOException e)
        {
            String message = "Failed to move file, Job Name: "
                    + jobInfo.getJobName() + ", Job Id: " + jobInfo.getId();
            LogUtil.fail(message, e);
            return false;
        }
        finally
        {
            // Delete the loaded directory from GS
            String tempDir = cpConfig.getTempBox() + File.separator
                    + jobInfo.getJobName();
            FileUtil.deleteFile(tempDir);
        }
        return true;
    }

    /**
     * Move job files
     * 
     * @param failedJobInfos
     * @param targetDir
     */
    private static void moveJobFiles(List<JobInfo> jobInfos, String targetDir)
    {
        for (JobInfo jobInfo : jobInfos)
        {
            File file = new File(jobInfo.getOriginFile());
            try
            {
                FileUtil.copyFileToDir(targetDir, file);
                FileUtil.deleteFile(file);
            }
            catch (IOException e)
            {
                String message = "File read/write error when move job files.";
                LogUtil.fail(message, e);
            }
            jobInfo.setOriginFile(targetDir + File.separator + file.getName());
        }
    }

    /**
     * With no files
     * 
     * @param directory
     * @return
     */
    private static boolean isEmptyDirectory(File directory)
    {
        List<File> files = getAllFiles(directory);
        for (File file : files)
        {
            if (file.isFile())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Clear Empty directory
     * 
     * @param directory
     */
    public static void clearEmptyDirectories(File directory)
    {
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                if (isEmptyDirectory(file))
                {
                    FileUtil.deleteFile(file);
                }
                else
                {
                    clearEmptyDirectories(file);
                }
            }
        }
    }

    /**
     * get all files in this folder
     * 
     * @param dir
     * @return
     */
    private static List<File> getAllFiles(File dir)
    {
        List<File> list = new ArrayList<File>();
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isFile())
                {
                    list.add(file);
                }
                else
                {
                    list.addAll(getAllFiles(file));
                }

            }
        }
        return list;
    }
}
