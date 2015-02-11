package com.globalsight.smartbox.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

import jcifs.smb.SmbFile;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FTPConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.bo.SMBConfiguration;

/**
 * Directory box operation
 * 
 * @author Leon
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
        String inBox = cpConfig.getInbox();
        String inBox4XLZ = cpConfig.getInbox4XLZ();
        String jobCreatingBox = cpConfig.getJobCreatingBox();
        List<File> filesJobCreating = new ArrayList<File>();

        FTPConfiguration ftpConfig = cpConfig.getFtpConfig();
        if (ftpConfig != null)
        {
            downloadFileToInboxFromFTP(cpConfig, ftpConfig);
        }

        SMBConfiguration smbConfig = cpConfig.getSmbConfig();
        if (smbConfig != null)
        {
            downloadFileToInboxFromSMB(cpConfig, smbConfig);
        }

        moveFiles(jobCreatingBox, inBox, filesJobCreating);
        if (null != inBox4XLZ && inBox4XLZ != "")
        {
            moveFiles(cpConfig.getJobCreatingBox4XLZ(), inBox4XLZ, filesJobCreating);
        }
        return filesJobCreating;
    }

    private static void moveFiles(String jobCreatingBox, String inBox,
            List<File> filesJobCreating)
    {
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
    }

    /**
     * Download files from FTP to Inbox
     * 
     * @param inBox
     * @param ftpConfig
     */
    private static void downloadFileToInboxFromFTP(CompanyConfiguration cpConfig,
            FTPConfiguration ftpConfig)
    {
        FtpHelper ftpHelper = new FtpHelper(ftpConfig);
        boolean ftpInit = ftpHelper.testConnect();
        if (ftpInit)
        {
            String ftpInbox = ftpConfig.getFtpInbox();
            String inBox = cpConfig.getInbox();
            copyFtpFile(ftpHelper, ftpInbox, inBox);
            String inbox4XLZ = cpConfig.getInbox4XLZ();
            if (null != inbox4XLZ && !"".equals(inbox4XLZ))
            {
                String rootDir = ftpInbox.substring(0, ftpInbox.lastIndexOf("/"));
                String ftpInbox4XLZ = rootDir + inbox4XLZ.substring(inbox4XLZ.lastIndexOf(File.separator));
                if (!ftpHelper.ftpDirExists(ftpInbox4XLZ))
                {
                    ftpHelper.ftpCreateDir(ftpInbox4XLZ);
                }
                copyFtpFile(ftpHelper, ftpInbox4XLZ, inbox4XLZ);
            }
        }
    }

    private static void copyFtpFile(FtpHelper ftpHelper, String ftpInbox,
            String inBox)
    {
        FTPFile[] files = ftpHelper.ftpFileList(ftpInbox);
        for (FTPFile file : files)
        {
            String fileName = file.getName();
            if (fileName == null || fileName.matches("\\.*"))
            {
                continue;
            }
            String remoteFilePath = ftpInbox + "/" + fileName;
            String localeFilePath = inBox + File.separator + fileName;
            ftpHelper.ftpDownloadFile(remoteFilePath, localeFilePath);
            ftpHelper.ftpDeleteFile(remoteFilePath);
        }
    }

    /**
     * Download files from SMB to Inbox
     * 
     * @param inBox
     * @param ftpConfig
     */
    private static void downloadFileToInboxFromSMB(CompanyConfiguration cpConfig,
            SMBConfiguration smbConfig)
    {
        String smbInbox = smbConfig.getSMBInbox();
        copySMBFile(cpConfig.getInbox(), smbInbox);
        String inbox4XLZ = cpConfig.getInbox4XLZ();
        if (null != inbox4XLZ && !"".equals(inbox4XLZ))
        {
            String rootDir = smbInbox.substring(0, smbInbox.length() - 2);
            rootDir = rootDir.substring(0, rootDir.lastIndexOf("/") + 1);
            String sbmInbox4XLZ = rootDir + inbox4XLZ.substring(inbox4XLZ.lastIndexOf(File.separator) + 1) + "/";
            copySMBFile(inbox4XLZ, sbmInbox4XLZ);
        }
    }

    /**
     * Copy SMB File to Locale.
     * 
     * @param localeDir
     *            Locale File Directory
     * @param smbURL
     *            SMB File Directory
     */
    private static void copySMBFile(String localeDir, String smbURL)
    {
        try
        {
            SmbFile smbDir = new SmbFile(smbURL);
            if (smbDir.exists())
            {
                SmbFile[] files = smbDir.listFiles();
                for (SmbFile sf : files)
                {
                    if (sf.isDirectory())
                    {
                        continue;
                    }
                    SMBHelper.copyFileFromSmbToLocal(sf, localeDir);
                    sf.delete();
                }
            }
        }
        catch (Exception e)
        {
            String message = "Failed to Copy SMB Files:" + smbURL;
            LogUtil.fail(message, e);
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
        FtpHelper ftpHelper = new FtpHelper(ftpConfig);
        boolean ftpInit = ftpHelper.testConnect();
        if (ftpInit)
        {
            File failedBox = new File(failedboxPath);
            File[] files = failedBox.listFiles();
            for (File file : files)
            {
                ftpHelper.ftpUploadDirectory(ftpConfig.getFtpFailedbox(), file,
                        failedboxPath);
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
        String ftpOutbox = ftpConfig.getFtpOutbox();
        FtpHelper ftpHelper = new FtpHelper(ftpConfig);
        boolean ftpInit = ftpHelper.testConnect();
        if (ftpInit)
        {
            File outBox = new File(outboxPath);
            File[] files = outBox.listFiles();
            for (File file : files)
            {
                ftpHelper.ftpUploadDirectory(ftpOutbox, file, outboxPath);
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
    private static void moveJobFiles(List<JobInfo> jobInfos, String origintargetDir)
    {
        String targetDir=origintargetDir;
        for (JobInfo jobInfo : jobInfos)
        {
           
            File file = new File(jobInfo.getOriginFile());
            if(file.isDirectory()){
                //separator for unzip folder which fail and successful named the some.
                //maybe used for we don't know the fail and successful  
                targetDir=origintargetDir+File.separator+jobInfo.getJobName();
                File dir=new File(targetDir);
                dir.mkdir();
            }
            else{                
                moveFiles(targetDir, jobInfo, file);
                continue;
            }
            //if it not a folder it will run the bellow code.
            if(null!=jobInfo.getSourceFiles()&&jobInfo.getSourceFiles().size()>0){
               
                    for(String sf:jobInfo.getSourceFiles()){
                        file = new File(sf);
                        //they don't need to copy the root folder
                        if(file.isDirectory())continue;
                        try
                        {
                            FileUtil.copyFileToDir(targetDir, file);
                            //the first fail come in the folder we left.
                            file.delete();
                        }
                        catch (IOException e)
                        {
                            String message = "File read/write error when move job files.";
                            LogUtil.fail(message, e);
                            continue;
                        }
                          
                    }
                
                  
            }
            else{                
                try
                {
                    for(File subfile:file.listFiles()){
                        FileUtil.copyFileToDir(targetDir, subfile);
                        if(subfile.isDirectory())continue;
                    }
                    //the second successful come in the folder we delete.
                    FileUtil.deleteFile(file);
                }
                catch (IOException e)
                {
                    String message = "File read/write error when move job files.";
                    LogUtil.fail(message, e);
                }
               
            }
            jobInfo.setOriginFile(targetDir); 
            continue;
            
        }
    }

    private static void moveFiles(String targetDir, JobInfo jobInfo, File file)
    {
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

    public static void deleteSubFile(String tempBox)
    {
        File temp = new File(tempBox);
        File[] files = temp.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }
    }
}
