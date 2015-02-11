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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * A util class, let operate file more easy.
 * 
 */
public class FileUtil
{
    private static Logger log = Logger.getLogger(FileUtil.class);
    private static final Pattern SQL_VERSION = Pattern
            .compile(".*(\\d{1,}\\.\\d*\\.\\d*\\.\\d*\\.\\d*).*\\.sql");

    private static final Pattern SQL_VERSION_2 = Pattern
            .compile(".*(\\d{1,}\\.\\d*\\.\\d*\\.\\d*)\\.\\d*.*\\.sql");
    
    public static final String TEMP_DIRECTORY = System
            .getProperty("java.io.tmpdir");
    
    public static String lineSeparator;
    {
    	if (isInLinux())
    	{
    		lineSeparator = "\n";
    	}
    	else
    	{
    		lineSeparator = "\r\n";
    	}
    }
    
    public static boolean isInLinux()
    {
        String os = System.getProperty("os.name");

        return os.startsWith("Linux");
    }

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

    /**
     * Gets a file filter to select files which name end with "sql".
     * 
     * @return
     */
    private static FileFilter getSqlFileFilter()
    {
        return new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith("sql");
            }
        };
    }

    /**
     * All sql file name must include the version information. Distill the
     * information from the name.
     * 
     * @param file
     * @return the version information
     */
    public static String getFileVersion(File file)
    {
        Assert.assertFileExist(file);

        Matcher m = SQL_VERSION.matcher(file.getName());

        if (!m.find())
        {
            throw new IllegalArgumentException(
                    "Can not get version from sql file: " + file.getName());
        }

        return m.group(1);
    }

    public static String getSqlVersion(File file)
    {
        Assert.assertFileExist(file);

        Matcher m = SQL_VERSION_2.matcher(file.getName());

        if (!m.find())
        {
            throw new IllegalArgumentException(
                    "Can not get version from sql file: " + file.getName());
        }

        return m.group(1);
    }
    
    public static StringBuilder readFileAsStringBuilder(File file)
    {
        BufferedReader in = null;
        StringBuilder content = new StringBuilder();
        try
        {
            in = new BufferedReader(new FileReader(file));
            String s = in.readLine();

            while (s != null)
            {
                content.append(s).append(lineSeparator);
                s = in.readLine();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        
        return content;
    }
    
    public static String readFile(File file)
    {
        return readFileAsStringBuilder(file).toString();
    }
    
    /**
     * Write content to the file.
     * 
     * @param file
     * @param size
     * @return
     * @throws IOException
     */
    public static void writeFile(File file, String content) throws IOException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
        }

        FileWriter out = null;

        try
        {
            out = new FileWriter(file);
            out.write(content);

        }
        finally
        {
            if (out != null)
            {
                out.flush();
                out.close();
            }
        }
    }
    
    public static void writeFile(File file, String content, String encoding)
            throws IOException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
        }

        FileOutputStream out = null;

        try
        {
            out = new FileOutputStream(file);
            out.write(content.getBytes(encoding));
        }
        finally
        {
            if (out != null)
            {
                out.flush();
                out.close();
            }
        }
    }
    
    /**
     * Deletes the file
     * 
     * @param f
     */
    public static void deleteFile(File f)
    {
    	log.info("Deleting file: " + f.getAbsolutePath());
    	
        if (!f.exists())
        {
        	log.error("Can not find the file: " + f.getAbsolutePath());
        	 return;
        }

        if (f.isDirectory())
        {
            File[] fs = f.listFiles();
            for (File cf : fs)
            {
                deleteFile(cf);
            }
        }

        if (!f.delete())
        {
        	log.error("Can not delete file: " + f.getAbsolutePath());
        }
        else
        {
        	log.info("Deleted the file: " + f.getAbsolutePath());
        }
    }
    
    public static String readFile(File file, String encoding)
            throws IOException
    {
        return readFile(new FileInputStream(file), encoding);
    }

    /**
     * Reads the given input stream to a string content.
     */
    public static String readFile(InputStream in, String encoding)
            throws IOException
    {
        try
        {
            byte[] b = new byte[in.available()];
            in.read(b);
            return new String(b, encoding);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    /**
     * Copies one file to a specified file.
     * 
     * @param src
     * @param dst
     * @throws Exception
     */
    public static void copyAllFile(File src, File dst) throws Exception
    {
    	List<File> fs = getAllFiles(src);
    	String sPath = src.getAbsolutePath();
    	String tPath = dst.getAbsolutePath();
    	
    	for (File f : fs)
    	{
    		String p = f.getAbsolutePath();
    		p = p.replace(sPath, tPath);
    		copyFile(f, new File(p));
    	}
    }
}
