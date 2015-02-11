package com.globalsight.smartbox.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;

public class FtpHelper
{
    private FtpClient ftpClient = null;
    private String host;
    private String username;
    private String password;

    /**
     * Create a connection
     * 
     * @param host
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public FtpHelper(String host, String username, String password)
    {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * Test connect
     * 
     * @return
     */
    public boolean testConnect()
    {
        boolean connect = ftpConnect();
        ftpClose();
        return connect;
    }

    /**
     * Create a connection
     * 
     * @param host
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    private boolean ftpConnect()
    {
        try
        {
            ftpClient = new FtpClient();
            ftpClient.openServer(host);
            ftpClient.login(username, password);
            ftpClient.binary();
        }
        catch (Exception e)
        {
            String message = "Failed to connect to FTP Server: " + host
                    + ", user:" + username + ", password:" + password + ".";
            LogUtil.fail(message, e);
            return false;
        }
        return true;
    }

    /**
     * Check the dir is existing, used for GSSmartBox init
     * 
     * @param host
     * @param username
     * @param password
     * @return
     */
    public boolean ftpDirExists(String path)
    {
        if (ftpConnect())
        {
            try
            {
                ftpClient.cd(path);
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

    /**
     * Close connection
     * 
     * @return
     */
    public boolean ftpClose()
    {
        try
        {
            ftpClient.closeServer();
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
     * Get file list
     * 
     * @param fc
     * @return
     * @throws IOException
     * @throws Exception
     */
    public List<String> ftpList(String directory)
    {
        List<String> fileNames = new ArrayList<String>();
        if (ftpConnect())
        {
            try
            {
                DataInputStream is = new DataInputStream(
                        ftpClient.nameList(directory));
                String fileName;
                while ((fileName = is.readLine()) != null)
                {
                    fileNames.add(fileName);
                }
                is.close();
            }
            catch (Exception e)
            {
                String message = "Failed to get file list from inbox in FTP.";
                LogUtil.fail(message, e);
            }
            finally
            {
                ftpClose();
            }
        }

        return fileNames;
    }

    /**
     * Rename file, move file to another directory in ftp
     * 
     * @param target
     * @param file
     * @return
     */
    public boolean ftpRename(String target, String file)
    {
        if (ftpConnect())
        {
            try
            {
                ftpClient.rename(file, target);
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

    /**
     * Download file
     * 
     * @param fc
     * @param filename
     * @return
     * @throws IOException
     * @throws Exception
     */
    public InputStream ftpDownloadFile(String fileName)
    {
        InputStream is = null;
        if (ftpConnect())
        {
            try
            {
                is = ftpClient.get(fileName);
            }
            catch (IOException e)
            {
                String message = "Failed to download file from InBox(in FTP), fileName: "
                        + fileName + ".";
                LogUtil.FAILEDLOG.error(message);
            }
        }

        return is;
    }

    /**
     * Create directory in ftp
     * 
     * @param path
     */
    public void ftpCreateDir(String path)
    {
        if (ftpConnect())
        {
            ftpClient.sendServer("MKD " + path + "\r\n");
            ftpClose();
        }
    }

    /**
     * Upload file to ftp
     * 
     * @param targetFile
     * @param fileName
     * @return
     * @throws IOException
     */
    private boolean ftpUpload(String targetFile, File file)
    {
        if (ftpConnect())
        {
            try
            {
                TelnetOutputStream os = ftpClient.put(targetFile);
                FileInputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int c;
                while ((c = is.read(bytes)) != -1)
                {
                    os.write(bytes, 0, c);
                }
                is.close();
                os.close();
            }
            catch (IOException e)
            {
                return false;
            }
            finally
            {
                ftpClose();
            }
            return true;
        }
        return false;
    }

    /**
     * Upload directory to ftp
     * 
     * @param targetFile
     * @param fileName
     * @return
     * @throws IOException
     */
    public void ftpUploadDirectory(String targetDir, File sourceFile,
            String root)
    {
        if (sourceFile.isDirectory())
        {
            String sourceFilePath = sourceFile.getPath();
            sourceFilePath = sourceFilePath.replace("\\", "/");
            String ftpDir = targetDir
                    + sourceFilePath.substring(sourceFilePath.indexOf(root)
                            + root.length(), sourceFilePath.length());
            if (!ftpDirExists(ftpDir))
            {
                ftpCreateDir(ftpDir);
            }
            File[] files = sourceFile.listFiles();
            for (File file : files)
            {
                ftpUploadDirectory(targetDir, file, root);
            }
        }
        else
        {
            String sourceFilePath = sourceFile.getPath();
            sourceFilePath = sourceFilePath.replace("\\", "/");
            String ftpFile = targetDir
                    + sourceFilePath.substring(sourceFilePath.indexOf(root)
                            + root.length(), sourceFilePath.length());
            boolean uploaded = ftpUpload(ftpFile, sourceFile);
            if (uploaded)
            {
                // File have been uploaded to FTP , delete unused file
                FileUtil.deleteFile(sourceFile);
            }
        }
    }

    /**
     * Delete file
     * 
     * @param fc
     * @param filename
     * @return
     * @throws IOException
     */
    public boolean ftpDelete(String filename)
    {
        if (ftpConnect())
        {
            ftpClient.sendServer("dele " + filename + "\r\n");
            ftpClose();
        }
        return true;
    }
}
