package com.globalsight.everest.webapp.applet.createjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import netscape.javascript.JSObject;

public class CreateJobUtil
{
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    /**
     * Check if a file is a zip file
     * @param file
     * @return true if is a zip file
     */
    public static boolean isZipFile(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if (extension != null && extension.equalsIgnoreCase("zip"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Get the file extension of a file
     * @param file
     * @return file extension
     */
    public static String getFileExtension(File file)
    {
        if (file != null)
        {
            String fileName = file.getName();
            if (fileName.lastIndexOf(".") != -1)
            {
                String extension = fileName
                        .substring(fileName.lastIndexOf(".") + 1);
                return extension;
            }
            else
            {
                return "";
            }
        }
        else
        {
            return null;
        }
    }
    
    public static List<ZipEntry> getFilesInZipFile(File file)
    {
        List<ZipEntry> filesInZip = new ArrayList<ZipEntry>();
        ZipInputStream zin = null;
        try
        {
            String zipFileFullPath = file.getPath();
            zin = new ZipInputStream(new FileInputStream(zipFileFullPath));
            ZipEntry zipEntry = null;
            while ((zipEntry = zin.getNextEntry()) != null)
            {
                if (zipEntry.isDirectory())
                {
                    continue;
                }
                filesInZip.add(zipEntry);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (zin != null)
            {
                try
                {
                    zin.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return filesInZip;
    }
    
    /**
     * Run a javascript function defined on the jsp
     * @param window
     * @param functionName
     * @param parameters
     */
    public static Object runJavaScript(JSObject window, String functionName,
            Object[] parameters)
    {
        Object ret = window.call(functionName, parameters);
        return ret;
    }
    
    /**
     * Get the id of a file
     * @param data
     * @return
     */
    public static String getFileId(String data) {
        byte[] bytes = getMD5(data);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(bytes, 0, 8);
        return String.valueOf(Math.abs(buf.getLong(0)));
    }
    
    private static byte[] getMD5(String s) {
        MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // can't happen
        }
        digest.update(s.getBytes(UTF8));
        return digest.digest();
    }
}
