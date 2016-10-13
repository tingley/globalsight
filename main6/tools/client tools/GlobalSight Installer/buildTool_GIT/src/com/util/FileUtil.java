/**
 *  Copyright 2009 Welocalize, Inc. 
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
package com.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.Main;

/**
 * A util class, let operate file more easy.
 * 
 */
public class FileUtil
{
    private static Logger log = Logger.getLogger(FileUtil.class);
    private static String ZIP_FILE_PATH = "script/windows/zip.bat";
    private static String UNZIP_FILE_PATH = "script/windows/unzip.bat";
    public static String lineSeparator = Main.isInLinux() ? "\n" : "\r\n";

    /**
     * Copies one file to a specified file.
     * 
     * @param src
     * @param dst
     * @throws Exception
     */
    public static void copyFile(File src, File dst) throws Exception
    {
        File parent = dst.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try
        {
            int len = 0;
            byte[] buf = new byte[1024];
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dst);
            while ((len = fis.read(buf)) != -1)
            {
                fos.write(buf, 0, len);
            }
            fos.flush();
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }

            if (fos != null)
            {
                fos.close();
            }
        }
    }

    /**
     * Gets all files under the specified fold.
     * 
     * @param root
     *            the specified fold
     * @return A list includes all files under the specified fold.
     */
    public static List<File> getAllFiles(File root)
    {
        return getAllFiles(root, null);
    }

    /**
     * Gets all files under the specified fold.
     * 
     * @param root
     *            the specified fold
     * @return A list includes all files under the specified fold.
     */
    public static List<File> getAllFiles(File root, FileFilter filter)
    {
        List<File> files = new ArrayList<File>();
        if (root.isFile())
        {
            if (filter == null || filter.accept(root))
            {
                files.add(root);
            }
        }
        else
        {
            File[] fs = root.listFiles();
            for (File f : fs)
            {
                files.addAll(getAllFiles(f, filter));
            }
        }

        return files;
    }

    public static void unzip(String root, String zipFileName)
    {
        if (Main.isInLinux())
        {
            String[] cmd =
            { "sh", "./script/linux/unzip.sh", root, zipFileName};
            try
            {
                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        else
        {
            try
            {
                File unzipFile = new File(UNZIP_FILE_PATH);
                BufferedWriter out = 
                        new BufferedWriter(
                                new OutputStreamWriter(new FileOutputStream(
                                        unzipFile), "UTF-8"));               
                out.write("cd /D \"" + root + "\"" + lineSeparator);
                out.write("jar -xfv " + zipFileName);
                out.flush();
                out.close();
                
                String[] cmd =
                { UNZIP_FILE_PATH };

                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }            
        }
    }
    
    public static void writeFile(String content, File file) throws IOException
    {
        File parent = file.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }
        
        FileOutputStream out = null;
        
        try
        {
            out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.flush();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }
    
    /**
     * Reads some bytes from the file.
     * 
     * @param file
     * @param size
     * @return
     * @throws IOException
     */
    public static String readFile(File file) throws IOException
    {
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(file);
            byte[] b = new byte[in.available()];
            in.read(b, 0, b.length);
            return new String(b);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    public static void zip(String root, String zipFileName)
    {
        File file = new File(root);
        
        if (Main.isInLinux())
        {
            try
            {
                zipFileName = zipFileName.replace("(", "\\(");
                zipFileName = zipFileName.replace(")", "\\)");
                FileWriter out = new FileWriter(new File("./script/linux/zip.sh"));
                out.write("cd \"" + root + "\"" + lineSeparator);
                out.write("zip -r " + zipFileName);
                for(String f : file.list())
                {
                    out.write(" " + f);
                }
                
                out.flush();
                out.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
            
            String[] cmd =
            { "sh", "./script/linux/zip.sh"};
            try
            {
                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        else
        {
            String[] cmd =
            { ZIP_FILE_PATH };
            try
            {
                BufferedWriter out = 
                    new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(
                                    ZIP_FILE_PATH), "UTF-8"));  
                out.write("cd /D " + root + "\r\n");
                out.write("jar -cfMv " + zipFileName);
                
                for(String f : file.list())
                {
                    out.write(" " + f);
                }
                
                out.flush();
                out.close();
                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
}
