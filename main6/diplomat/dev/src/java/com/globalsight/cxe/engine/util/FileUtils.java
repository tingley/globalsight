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
package com.globalsight.cxe.engine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class FileUtils
{

    private static final Logger logger = Logger.getLogger(FileUtils.class);

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     * <p>
     * Syntax such as <code>file:///my%20docs/file.txt</code> will be
     * correctly decoded to <code>/my docs/file.txt</code>.
     * 
     * @param url
     *            the file URL to convert, null returns null
     * @return the equivalent <code>File</code> object, or <code>null</code>
     *         if the URL's protocol is not <code>file</code>
     * @throws IllegalArgumentException
     *             if the file is incorrectly encoded
     */
    public static File toFile(URL url)
    {
        if (url == null || !url.getProtocol().equals("file"))
        {
            return null;
        }

        try
        {
            String filename = url.toURI().getPath().replace('/',
                    File.separatorChar);
            int pos = 0;
            while ((pos = filename.indexOf('%', pos)) >= 0)
            {
                if (pos + 2 < filename.length())
                {
                    String hexStr = filename.substring(pos + 1, pos + 3);
                    char ch = (char) Integer.parseInt(hexStr, 16);
                    filename = filename.substring(0, pos) + ch
                            + filename.substring(pos + 3);
                }
            }
            return new File(filename);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        
        return null;
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
    
    /**
     * Check whether a directory is empty
     * @param f
     * @return
     */
    public static boolean isEmpty(File f)
    {
        boolean empty = true;
        
        if (f.isFile())
        {
            return false;
        }
        File[] files = f.listFiles();
        for (File file : files)
        {
            if (!empty)
            {
                break;
            }
            else
            {
                if (file.isDirectory())
                {
                    empty = isEmpty(file);
                }
                else
                {
                    empty = false;
                }
            }
        }
        return empty;
    }
    
    public static void main(String[] args)
    {
        System.out.println(isEmpty(new File("C:\\New Folder")));
    }
    
    /**
     * Get the number of files under a directory
     * @param directory directory
     * @return file count
     */
    public static int getFileNo(File directory)
    {
        int count = 0;
        File[] files = directory.listFiles();
        count = files.length;
        for (File file : files)
        {
            if (file.isDirectory())
            {
                count += getFileNo(file);
                count--;
            }
        }
        return count;
    }

    public static void write(File file, String data) throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        try
        {
            out.write(data.getBytes());
        }
        finally
        {
            closeSilently(out);
        }
    }

    public static void write(File file, String data, String encoding)
            throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        try
        {
            out.write(data.getBytes(encoding));
        }
        finally
        {
            closeSilently(out);
        }
    }

    public static String read(String fileName) throws IOException
    {
        // InputStream in =
        // Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        return read(new FileInputStream(fileName));
    }

    public static String read(File file) throws IOException
    {
        return read(new FileInputStream(file));
    }

    public static String read(InputStream in) throws IOException
    {
        return new String(readAsByte(in));
    }

    public static String read(File file, String encoding) throws IOException
    {
        return read(new FileInputStream(file), encoding);
    }

    public static String read(InputStream in, String encoding)
            throws IOException
    {
        return new String(readAsByte(in), encoding);
    }

    private static byte[] readAsByte(InputStream in) throws IOException
    {
        byte[] content = new byte[(int) in.available()];
        try
        {
            in.read(content, 0, content.length);
        }
        finally
        {
            closeSilently(in);
        }
        return content;
    }

    public static void closeSilently(InputStream in)
    {
        try
        {
            if (in != null)
                in.close();
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot close inputstream: " + in);
            }
        }
    }

    public static void closeSilently(OutputStream out)
    {
        try
        {
            if (out != null)
                out.close();
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot close outputstream: " + out);
            }
        }
    }

    public static void closeSilently(Reader reader)
    {
        try
        {
            if (reader != null)
                reader.close();
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot close reader: " + reader);
            }
        }
    }

    public static void closeSilently(Writer writer)
    {
        try
        {
            if (writer != null)
                writer.close();
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot close writer: " + writer);
            }
        }
    }

    public static void deleteSilently(String filename)
    {
        try
        {
            if (filename != null)
                new File(filename).delete();
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot delete file: " + filename, e);
            }
        }
    }

    public static void deleteAllFilesSilently(String p_filename)
    {
        try
        {
            if (p_filename != null)
            {
                File file = new File(p_filename);
                if (file.isDirectory())
                {
                    String files[] = file.list();
                    for (int i = 0; i < files.length; i++)
                    {
                        deleteAllFilesSilently(p_filename + File.separator
                                + files[i]);
                    }
                }
                deleteSilently(p_filename);
            }
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot delete folder: " + p_filename, e);
            }
        }
    }

    /**
     * Returns base name of the specified file
     * 
     * <pre>
     * getBaseName(&quot;dir/file.txt&quot;) returns &quot;file.txt&quot;
     * </pre>
     * 
     * @param p_filename
     *            the file name
     * @return
     */
    public static String getBaseName(String p_filename)
    {
        return p_filename.substring(p_filename.lastIndexOf(File.separator) + 1);
    }

    /**
     * Returns the part of the specified filename after removing the suffix.
     * 
     * <pre>
     * getPrefix(&quot;dir/file.txt&quot;) returns &quot;dir/file&quot;
     * getPrefix(&quot;file.txt&quot;) returns &quot;file&quot;
     * </pre>
     * 
     * @param p_filename
     * @return
     */
    public static String getPrefix(String p_filename)
    {
        int index = p_filename.lastIndexOf('.');
        return index >= 0 ? p_filename.substring(0, index) : p_filename;
    }

    public static String getSuffix(String p_filename)
    {
        return p_filename.substring(p_filename.lastIndexOf('.') + 1);
    }

    public static String relativePath(String p_absolutePath,
            String p_currentPath)
    {
        // assert absolutePath.startsWith(currentPath)
        int index = p_currentPath.length();
        if (!p_currentPath.endsWith(File.separator))
        {
            index++;
        }
        return p_absolutePath.substring(index);
    }

    public static String concatPath(String parent, String child)
    {
        return new File(parent, child).getAbsolutePath();
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
}
