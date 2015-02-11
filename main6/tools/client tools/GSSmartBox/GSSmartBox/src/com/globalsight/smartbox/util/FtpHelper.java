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

/**
 * FTP Utility, such as Upload and Download file from FTP.
 * 
 * @author Joey
 */
public class FtpHelper
{
    protected FTPClient ftpClient = null;
    protected String host;
    protected String username;
    protected String password;
    protected int port = 21;

    public FtpHelper(String host, String username, String password)
    {
        this(host, 21, username, password);
    }
    
    public FtpHelper(String host, int port, String username, String password)
    {
        this.host = host;
        this.username = username;
        this.password = password;
        setPort(port);
    }
    
    public boolean testConnect()
    {
        boolean connect = ftpConnect();
        ftpClose();
        return connect;
    }

    protected boolean ftpConnect()
    {
        try
        {
            boolean result ;
            if (ftpClient == null || !ftpClient.isConnected())
            {
                ftpClient = new FTPClient();
                ftpClient.connect(host, port);
                result = ftpClient.login(username, password);
                // Sets Binary File Type for ZIP File.  
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // Set Buffer Size to speed up download/upload file.
                ftpClient.setBufferSize(102400);
            }
            else
            {
                result = ftpClient.isConnected();
            }
            
            return result;
        }
        catch (Exception e)
        {
            String message = "Failed to connect to FTP Server: " + host
                    + ", FTPUsername:" + username + ", FTPPassword:" + password
                    + ", FTPServerPort:" + port + ".";
            LogUtil.fail(message, e);
            return false;
        }
    }

    public void ftpCreateDir(String p_path)
    {
        if (ftpConnect())
        {
            try
            {
                ftpClient.makeDirectory(p_path);
            }
            catch (IOException e)
            {
                LogUtil.fail("Create Directory error by path:" + p_path, e);
            }
            finally
            {
                ftpClose();
            }
        }
    }
    
    public boolean ftpDirExists(String p_path)
    {
        if (ftpConnect())
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
                ftpClose();
            }
        }

        return true;
    }

    public boolean ftpClose()
    {
        try
        {
            ftpClient.disconnect();
        }
        catch (Exception e)
        {
            String message = "Ftp close exception";
            LogUtil.fail(message, e);
            return false;
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
        if (ftpConnect())
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
                ftpClose();
            }
        }

        return result;
    }

    public boolean ftpRename(String p_target, String p_file)
    {
        if (ftpConnect())
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
                ftpClose();
            }
        }

        return true;
    }

   
    public InputStream ftpDownloadFile(String p_remoteFileName)
    {
        InputStream is = null;
        if (ftpConnect())
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
                ftpClose();
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
        if (ftpConnect())
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
                ftpClose();
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
        if (ftpConnect())
        {
            try
            {
                String remoteFileName = p_remoteFileName.replace("\\", "/").replace("//", "/");
                ftpCreateDirectoryTree(remoteFileName.substring(0, remoteFileName.lastIndexOf("/")));
                FileInputStream localFIS = new FileInputStream(p_file);
                boolean result = ftpClient.storeFile(remoteFileName, localFIS);
                localFIS.close();
                LogUtil.info("Upload file successfully, FTP File:[" + remoteFileName + "]");
                return result;
            }
            catch (IOException e)
            {
                String message = "Failed to upload file, FTP File:["
                        + p_remoteFileName + "], locale File:["
                        + p_file.getPath() + "].";
                LogUtil.FAILEDLOG.error(message);
                return false;
            }
            finally
            {
                ftpClose();
            }
        }
        
        return false;
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
        for (int i = 1; i < temp.length; i++)
        {
            path.append("/").append(temp[i]);
            directories.add(path.toString());
        }
        
        for (String dir : directories)
        {
            if (!dir.isEmpty())
            {
                if (dirExists)
                {
                    dirExists = ftpClient.changeWorkingDirectory(dir);
                }
                if (!dirExists)
                {
                    if (!ftpClient.makeDirectory(dir))
                    {
                        throw new IOException(
                                "Unable to create remote directory '" + dir
                                        + "'.  error='"
                                        + ftpClient.getReplyString() + "'");
                    }
                    if (!ftpClient.changeWorkingDirectory(dir))
                    {
                        throw new IOException(
                                "Unable to change into newly created remote directory '"
                                        + dir + "'.  error='"
                                        + ftpClient.getReplyString() + "'");
                    }
                }
            }
        }
    }

    // Deletes a file on FTP Server.
    public boolean ftpDeleteFile(String p_fileName)
    {
        if (ftpConnect())
        {
            try
            {
                return ftpClient.deleteFile(p_fileName);
            }
            catch (IOException e)
            {
                LogUtil.fail("Delete File Error: " + p_fileName, e);
            }
            
            ftpClose();
        }
        
        return false;
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
