package com.globalsight.smartbox.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class SMBHelper
{
    /**
     * Copy file to local directory
     * 
     * @param smbFile
     * @param localDir
     */
    public static void copyFileFromSmbToLocal(SmbFile smbFile, String localDir)
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            String fileName = smbFile.getName();
            File localFile = new File(localDir + File.separator + fileName);
            in = new BufferedInputStream(new SmbFileInputStream(smbFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1)
            {
                out.write(buffer);
                buffer = new byte[1024];
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload files to SMB
     * 
     * @param remoteUrl
     * @param localFilePath
     * @throws Exception
     */
    public static void copyFileFromLocalToSMB(String targetSMBDir,
            File localFile, String root)
    {
        if (localFile.isDirectory())
        {
            String localFilePath = localFile.getPath();
            localFilePath = localFilePath.replace("\\", "/");
            String smbDir = targetSMBDir
                    + localFilePath.substring(localFilePath.indexOf(root) + 1
                            + root.length(), localFilePath.length());
            SmbFile sf;
            try
            {
                sf = new SmbFile(smbDir);
                if (!sf.exists())
                {
                    sf.mkdir();
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            File[] files = localFile.listFiles();
            for (File file : files)
            {
                copyFileFromLocalToSMB(targetSMBDir, file, root);
            }
        }
        else
        {
            String localFilePath = localFile.getPath();
            localFilePath = localFilePath.replace("\\", "/");
            String smbFile = targetSMBDir
                    + localFilePath.substring(localFilePath.indexOf(root)
                            + root.length(), localFilePath.length());
            InputStream in = null;
            OutputStream out = null;
            try
            {
                SmbFile sf = new SmbFile(smbFile);
                in = new BufferedInputStream(new FileInputStream(localFile));
                out = new BufferedOutputStream(new SmbFileOutputStream(sf));
                byte[] buffer = new byte[1024];
                while (in.read(buffer) != -1)
                {
                    out.write(buffer);
                    buffer = new byte[1024];
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    out.close();
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            localFile.delete();
        }
    }
}