/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.smartbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.globalsight.smartbox.bo.FTPConfiguration;

/**
 * FTP Utility, such as Upload and Download file from FTP.
 * 
 * @author Joey
 */
public class FtpHelper
{
    protected String host;
    protected String username;
    protected String password;
    protected int port = 21;

    private static int MAX_FTP_CONNECTION_NUM = 3;
    private static int busyFtpClientNumber = 0;

    public FtpHelper(String host, int port, String username, String password)
    {
        this.host = host;
        this.username = username;
        this.password = password;
        setPort(port);
    }

    public FtpHelper(FTPConfiguration ftpConfig)
    {
        this.host = ftpConfig.getFtpHost();
        this.username = ftpConfig.getFtpUsername();
        this.password = ftpConfig.getFtpPassword();
        setPort(ftpConfig.getFtpPort());
    }

    public boolean testConnect()
    {
        FTPClient ftpClient = null;
        try
        {
            ftpClient = getFtpClient();
            if (ftpClient != null && ftpClient.isConnected())
            {
                return true;
            }
        }
        catch (Exception e)
        {            
        }
        finally
        {
            closeFtpClient(ftpClient);
        }
        return false;
    }

    public boolean ftpCreateDir(String p_path)
    {
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                return ftpClient.makeDirectory(p_path);
            }
            catch (IOException e)
            {
                LogUtil.fail("Create Directory error by path:" + p_path, e);
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }

        return false;
    }

    public boolean ftpDirExists(String p_path)
    {
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                return ftpClient.changeWorkingDirectory(p_path);
            }
            catch (Exception e)
            {
                return false;
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }

        return true;
    }

    /**
     * Gets File Array in FTP Server by FTP Directory.
     * 
     * @param directory
     *            FTP Directory
     */
    public FTPFile[] ftpFileList(String p_directory)
    {
        FTPFile[] result = {};
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                result = ftpClient.listFiles(p_directory);
            }
            catch (IOException e)
            {
                String message = "Ftp get name list error.";
                LogUtil.fail(message, e);
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }

        return result;
    }

    public boolean ftpRename(String p_target, String p_file)
    {
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                ftpClient.rename(p_file, p_target);
            }
            catch (IOException e)
            {
                return false;
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }

        return true;
    }
   
    public InputStream ftpDownloadFile(String p_remoteFileName)
    {
        InputStream is = null;
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                is = ftpClient.retrieveFileStream(p_remoteFileName);
            }
            catch (IOException e)
            {
                String message = "Failed to download file from InBox(in FTP), fileName: "
                        + p_remoteFileName + ".";
                LogUtil.FAILEDLOG.error(message);
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }

        return is;
    }

    /**
     * Downloads file from FTP to Locale.
     * 
     * @param p_remoteFileName
     *            File Name in FTP Server
     * @param p_localFileName
     *            File Name in Locale
     */
    public void ftpDownloadFile(String p_remoteFileName, String p_localFileName)
    {
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                FileOutputStream local = new FileOutputStream(new File(
                        p_localFileName));
                ftpClient.retrieveFile(p_remoteFileName, local);
                local.close();
            }
            catch (IOException e)
            {
                String message = "Failed to download file from InBox(in FTP), fileName: "
                        + p_remoteFileName + ".";
                LogUtil.FAILEDLOG.error(message);
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }
    }
   
    public void ftpUploadDirectory(String p_remoteDir, File p_sourceFile,
            String p_sourceFilePath)
    {
        if (p_sourceFile.isDirectory())
        {
            File[] files = p_sourceFile.listFiles();
            for (File file : files)
            {
                ftpUploadDirectory(p_remoteDir, file, p_sourceFilePath);
            }
        }
        else
        {
            String srcPath = p_sourceFilePath.replace("\\", "/");
            String temp = p_sourceFile.getPath();
            temp = temp.replace("\\", "/");
            temp = temp.substring(temp.indexOf(srcPath) + srcPath.length());
            String remoteFileName = p_remoteDir + temp;
            boolean uploaded = ftpUpload(remoteFileName, p_sourceFile);
            if (uploaded)
            {
                // File have been uploaded to FTP , delete unused file
                FileUtil.deleteFile(p_sourceFile);
            }
        }
    }
    
    /**
     * Upload Locale file to FTP Server
     * 
     * @param p_remoteFileName
     *            FTP Directory
     * @param p_file
     *            Locale File
     * @return
     */
    public boolean ftpUpload(String p_remoteFileName, File p_file)
    {
        try
        {
            String remoteFile = p_remoteFileName.replace("\\", "/").replace("//", "/");
            int index = remoteFile.lastIndexOf("/");
            if (index > -1)
            {
                String remoteDirectory = remoteFile.substring(0, index);
                ftpCreateDirectoryTree(remoteDirectory);
            }

            boolean isSucceed = ftpStoreFile(remoteFile, p_file);
            if (isSucceed)
            {
                LogUtil.info("Upload file successfully, FTP File:["
                        + remoteFile + "]");
            }
            return isSucceed;
        }
        catch (IOException e)
        {
            String message = "Failed to upload file, FTP File:["
                    + p_remoteFileName + "], locale File:[" + p_file.getPath()
                    + "].";
            LogUtil.FAILEDLOG.error(message);
            LogUtil.fail(message, e);

            return false;
        }
    }

    /**
     * Creates an arbitrary directory hierarchy on the remote ftp server.
     * 
     * @param dirTree
     *            the directory tree only delimited with / chars. No file name!
     * @throws Exception
     */
    private void ftpCreateDirectoryTree(String p_dirTree) throws IOException
    {
        boolean dirExists = true;

        // Gets the relative ftp path.
        List<String> directories = new ArrayList<String>();
        String[] temp = p_dirTree.split("/"); 
        StringBuffer path = new StringBuffer();
        for (int i = 0; i < temp.length; i++)
        {
            if (temp[i].trim().length() > 0)
            {
                path.append("/").append(temp[i]);
                directories.add(path.toString());
            }
        }
        
        for (String dir : directories)
        {
            if (!dir.isEmpty())
            {
                if (dirExists)
                {
                    dirExists = ftpDirExists(dir);
                }
                if (!dirExists)
                {
                    if (!ftpCreateDir(dir))
                    {
                        throw new IOException(
                                "Unable to create remote directory '" + dir
                                        + "'.");
                    }
                    if (!ftpDirExists(dir))
                    {
                        throw new IOException(
                                "Unable to change into newly created remote directory '"
                                        + dir + "'.");
                    }
                }
            }
        }
    }

    // Deletes a file on FTP Server.
    public boolean ftpDeleteFile(String p_fileName)
    {
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            try
            {
                return ftpClient.deleteFile(p_fileName);
            }
            catch (IOException e)
            {
                LogUtil.fail("Delete File Error: " + p_fileName, e);
            }
            finally
            {
                closeFtpClient(ftpClient);
            }
        }

        return false;
    }

    public boolean ftpStoreFile(String remoteFileName, File localFile)
    {
        FTPClient ftpClient = getFtpClient();
        if (ftpClient != null && ftpClient.isConnected())
        {
            FileInputStream localFIS = null;
            try
            {
                localFIS = new FileInputStream(localFile);
                return ftpClient.storeFile(remoteFileName, localFIS);
            }
            catch (IOException e)
            {
                LogUtil.fail("Store File Error: " + remoteFileName, e);
            }
            finally
            {
                try
                {
                    localFIS.close();
                }
                catch (IOException e)
                {
                    LogUtil.fail(
                            "Fail to close FileInputStream: "
                                    + localFile.getAbsolutePath(), e);
                }
                closeFtpClient(ftpClient);
            }
        }

        return false;
    }

    public synchronized FTPClient getFtpClient()
    {
        FTPClient client = null;
        try
        {
            boolean loopFlag = true;
            while (loopFlag)
            {
                if (busyFtpClientNumber < MAX_FTP_CONNECTION_NUM)
                {
                    client = initFtpClient();
                    busyFtpClientNumber++;
                    break;
                }
                else
                {
                    LogUtil.GSSMARTBOXLOG.warn("ATTENTION:: Busy FTPClients exceed 3 !!!");
                    Thread.sleep(1000);
                }
            }
        }
        catch (Exception e)
        {
            String message = "Failed to connect to FTP Server: " + host
                    + ", FTPUsername:" + username + ", FTPPassword:" + password
                    + ", FTPServerPort:" + port + ".";
            LogUtil.fail(message, e);
            return null;
        }

        return client;
    }

    private FTPClient initFtpClient() throws IOException
    {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(host, port);
        ftpClient.login(username, password);

        // Sets Binary File Type for ZIP File.
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        // Set Buffer Size to speed up download/upload file.
        ftpClient.setBufferSize(102400);

        return ftpClient;
    }

    public synchronized boolean closeFtpClient(FTPClient ftpClient)
    {
        if (ftpClient == null)
            return true;

        try
        {
            ftpClient.disconnect();
            if (busyFtpClientNumber > 0)
                busyFtpClientNumber--;
        }
        catch (Exception e)
        {
            String message = "Ftp close exception";
            LogUtil.fail(message, e);
            return false;
        }
        return true;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int p_port)
    {
        if (p_port > 0)
        {
            port = p_port;
        }
    }
}
