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
package com.globalsight.smartbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File Utility class
 * 
 * @author leon
 * 
 */
public class FileUtil
{

    public static final String UTF8 = "UTF-8";
    public static final String UTF16 = "UTF-16";
    public static final String UTF16LE = "UTF-16LE";
    public static final String UTF16BE = "UTF-16BE";
    public static final String UTF32LE = "UTF-32LE";
    public static final String UTF32BE = "UTF-32BE";

    /**
     * Copy file or directory to Directory
     * 
     * @param targetDir
     * @param filePath
     * @throws IOException
     */
    public static void copyToDir(String targetDir, String filePath)
            throws IOException
    {
        File file = new File(filePath);
        if (file.isDirectory())
        {
            copyDir(targetDir, filePath);
        }
        else
        {
            copyFileToDir(targetDir, file);
        }
    }

    /**
     * Copy directory to another directory
     * 
     * @param targetDir
     * @param sourceDir
     * @throws IOException
     */
    public static void copyDir(String targetDir, String sourceDir)
            throws IOException
    {
        File targetFile = new File(targetDir);
        File file = new File(sourceDir);
        copyFileToDir(
                targetFile.getAbsolutePath() + File.separator + file.getName(),
                listFile(file));
    }

    /**
     * Copy many files to one directory
     * 
     * @param targetDir
     * @param filePath
     * @throws IOException
     */
    public static void copyFileToDir(String targetDir, String[] filePath)
            throws IOException
    {
        File targetFile = new File(targetDir);
        if (!targetFile.exists())
        {
            targetFile.mkdir();
        }
        else
        {
            if (!targetFile.isDirectory())
            {
                return;
            }
        }
        for (String path : filePath)
        {
            File file = new File(path);
            if (file.isDirectory())
            {
                copyFileToDir(targetDir + File.separator + file.getName(),
                        listFile(file));
            }
            else
            {
                copyFileToDir(targetDir, file);
            }
        }
    }

    /**
     * Copy one file to a directory
     * 
     * @param targetFile
     * @param file
     * @param newName
     * @throws IOException
     */
    public static void copyFileToDir(String targetDir, File file)
            throws IOException
    {
        String newFile = targetDir + File.separator + file.getName();
        File tFile = new File(newFile);
        copyFile(tFile, file);
    }

    /**
     * Copy file to file
     * 
     * @param targetFile
     * @param file
     * @throws IOException
     */
    public static void copyFile(File targetFile, File file) throws IOException
    {

        InputStream is = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int i = 0;
        while ((i = is.read(buffer)) != -1)
        {
            fos.write(buffer, 0, i);
        }
        is.close();
        fos.close();
    }

    /**
     * Copy file(InputStream) to file
     * 
     * @param targetFile
     * @param file
     * @throws IOException
     */
    public static void copyFile(File targetFile, InputStream file)
            throws IOException
    {

        FileOutputStream fos = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int i = 0;
        while ((i = file.read(buffer)) != -1)
        {
            fos.write(buffer, 0, i);
        }
        file.close();
        fos.close();
    }

    /**
     * Delete file
     * 
     * @param path
     */
    public static void deleteFile(String path)
    {
        deleteFile(new File(path));
    }

    /**
     * Delete file or directory
     * 
     * @param file
     */
    public static void deleteFile(File file)
    {
        if (!file.exists())
        {
            return;
        }
        if (!file.delete())
        {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    if (!files[i].delete())
                        deleteFile(files[i]);
                }
                else
                {
                    files[i].delete();
                }
            }
            file.delete();
        }
    }

    /**
     * List all the files path
     * 
     * @param dir
     * @return
     */
    public static String[] listFile(File dir)
    {
        String absolutPath = dir.getAbsolutePath();
        String[] paths = dir.list();
        String[] files = new String[paths.length];
        for (int i = 0; i < paths.length; i++)
        {
            files[i] = absolutPath + File.separator + paths[i];
        }
        return files;
    }

    /**
     * Try to guess the file encoding.
     * 
     * Only guess encodings of "UTF-8", "UTF-16" or "UTF-16BE".
     * 
     * @param file
     *            The file needed to guess the encoding.
     * @return The encoding, may be null.
     * @throws IOException
     */
    public static String guessEncoding(File file) throws IOException
    {
        byte[] b = readFile(file, 4);
        String guess = null;

        if (b[0] == (byte) 0xef && b[1] == (byte) 0xbb && b[2] == (byte) 0xbf)
            guess = UTF8;
        else if (b[0] == (byte) 0xff && b[1] == (byte) 0xfe
                && b[2] == (byte) 0x00 && b[3] == (byte) 0x00)
            guess = UTF32LE;
        else if (b[0] == (byte) 0x00 && b[1] == (byte) 0x00
                && b[2] == (byte) 0xfe && b[3] == (byte) 0xff)
            guess = UTF32BE;
        else if (b[0] == (byte) 0xff && b[1] == (byte) 0xfe)
            guess = UTF16LE;
        else if (b[0] == (byte) 0xfe && b[1] == (byte) 0xff)
            guess = UTF16BE;

        return guess;
    }

    /**
     * Reads some bytes from the file.
     * 
     * @param file
     * @param size
     * @return
     * @throws IOException
     */
    private static byte[] readFile(File file, int size) throws IOException
    {
        byte[] b = new byte[size];
        FileInputStream fin = null;

        try
        {
            fin = new FileInputStream(file);
            fin.read(b, 0, size);
        }
        finally
        {
            if (fin != null)
            {
                fin.close();
            }
        }
        return b;
    }

    /**
     * Writes the BOM(Byte Order Mark) to the file.
     * 
     * @param p_outputStream
     * @param encoding
     * @throws IOException
     */
    public static void writeBom(OutputStream outputStream, String encoding)
            throws IOException
    {
        if (outputStream != null && encoding != null)
        {
            byte[] b = null;

            if (UTF8.equals(encoding))
            {
                b = new byte[3];
                b[0] = (byte) 0xef;
                b[1] = (byte) 0xbb;
                b[2] = (byte) 0xbf;
            }
            else if (UTF16LE.equals(encoding))
            {
                b = new byte[2];
                b[0] = (byte) 0xff;
                b[1] = (byte) 0xfe;
            }
            else if (UTF16BE.equals(encoding))
            {
                b = new byte[2];
                b[0] = (byte) 0xfe;
                b[1] = (byte) 0xff;
            }
            else if (UTF32LE.equals(encoding))
            {
                b = new byte[4];
                b[0] = (byte) 0xff;
                b[1] = (byte) 0xfe;
                b[2] = (byte) 0x00;
                b[3] = (byte) 0x00;
            }
            else if (UTF32BE.equals(encoding))
            {
                b = new byte[4];
                b[0] = (byte) 0x00;
                b[1] = (byte) 0x00;
                b[2] = (byte) 0xfe;
                b[3] = (byte) 0xff;
            }

            if (b != null)
            {
                outputStream.write(b);
            }
        }
    }
}