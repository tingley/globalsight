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
package com.globalsight.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.log.GlobalSightCategory;

/**
 * A util class, let operate file more easy.
 * 
 */
public class FileUtil
{
    public static String lineSeparator = java.security.AccessController
            .doPrivileged(new sun.security.action.GetPropertyAction(
                    "line.separator"));
    static private final GlobalSightCategory logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(FileUtil.class);

    static public final String UTF8 = "UTF-8";
    static public final String UTF16LE = "UTF-16LE";
    static public final String UTF16BE = "UTF-16BE";

    /**
     * Copies one file to a specified file.
     * 
     * @param src
     * @param dst
     * @throws IOException
     * @throws Exception
     */
    public static void copyFile(File src, File dst) throws IOException
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
        Assert.assertFileExist(root);

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
     * Reads some bytes from the file.
     * 
     * @param file
     * @param size
     * @return
     * @throws IOException
     */
    public static byte[] readFile(File file, int size) throws IOException
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
    
    /**
     * Gets file content with specified encoding.
     * 
     * @param file
     *            The file to read. Must be exist.
     * @param encoding
     *            The specified encoding. Can not be null.
     * 
     * @return file content.
     * @throws Exception
     */
    public static String readFile(File file, String encoding) throws Exception
    {
        FileInputStream fin = new FileInputStream(file);
        byte[] b = new byte[fin.available()];
        fin.read(b);
        fin.close();
        
        return new String(b, encoding);
    }

    /**
     * Reads some bytes from the file.
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
    
    public static void writeFile(File file, String content, String encoding) throws IOException
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
     * Reads some bytes from the file.
     * 
     * @param file
     * @param size
     * @return
     * @throws IOException
     */
    public static void appendFile(File file, String content)
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
        }

        FileWriter out = null;

        try
        {
            out = new FileWriter(file, true);
            out.write(content);
        }
        catch(IOException e)
        {
            logger.error(e);
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.flush();
                    out.close();
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Try to guess the file encoding.
     * <p>
     * 
     * Only guees encodings of "UTF-8", "UTF-16" or "UTF-16BE".
     * 
     * @param file
     *            The file needed to guess the encoding.
     * @return The encoding, may be null.
     * @throws IOException
     */
    public static String guessEncoding(File file) throws IOException
    {
        byte[] b = readFile(file, 3);
        String guess = null;

        if (b[0] == (byte) 0xef && b[1] == (byte) 0xbb && b[2] == (byte) 0xbf)
            guess = UTF8;
        else if (b[0] == (byte) 0xff && b[1] == (byte) 0xfe)
            guess = UTF16LE;
        else if (b[0] == (byte) 0xfe && b[1] == (byte) 0xff)
            guess = UTF16BE;

        return guess;
    }

    /**
     * Writes the BOM(Byte Order Mark) to the file.
     * 
     * @param p_outputStream
     * @param encoding
     */
    public static void writeBom(OutputStream outputStream, String encoding)
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

            if (b != null)
            {
                try
                {
                    outputStream.write(b);
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
            }
        }
    }
    
    /**
     * Adds BOM to the the file
     * 
     * @param file
     * @param encoding
     * @throws IOException
     */
    public static void addBom(File file, String encoding) throws IOException
    {
        String fileEncoding = guessEncoding(file);
        
        if(fileEncoding == null) {
            FileInputStream in = new FileInputStream(file);
            byte[] buf;
            try
            {
                buf = new byte[in.available()];
                in.read(buf, 0, buf.length);
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
            
            BufferedOutputStream bos = 
                new BufferedOutputStream(new FileOutputStream(file));
            byte[] b = null;
            
            if (UTF8.equals(encoding))
            {
                if(buf[0] !=(byte) 0xef && buf[1] !=(byte) 0xbb && buf[2] !=(byte) 0xbf) {
                    b = new byte[buf.length + 3];
                    b[0] = (byte) 0xef;
                    b[1] = (byte) 0xbb;
                    b[2] = (byte) 0xbf;
                    System.arraycopy(buf, 0, b, 3, buf.length);
                }
            }
            else if (UTF16LE.equals(encoding))
            {
                if(buf[0] !=(byte) 0xff && buf[1] !=(byte) 0xfe) {
                    b = new byte[buf.length + 2];
                    b[0] = (byte) 0xff;
                    b[1] = (byte) 0xfe;
                    System.arraycopy(buf, 0, b, 2, buf.length);
                }
            }
            else if (UTF16BE.equals(encoding))
            {
                if(buf[0] !=(byte) 0xfe && buf[1] !=(byte) 0xff) {
                    b = new byte[buf.length + 2];
                    b[0] = (byte) 0xfe;
                    b[1] = (byte) 0xff;
                    System.arraycopy(buf, 0, b, 2, buf.length);
                }
            }

            try
            {
                bos.write(b, 0, b.length);
            }
            finally
            {
                if (bos != null)
                {
                    bos.flush();
                    bos.close();
                }
            }
        }
    }

    /**
     * Try to find the encoding of a xml file.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static String getEncodingOfXml(File file) throws IOException
    {
        byte[] bs = readFile(file, 150);
        String encoding = "utf-8";

        Map chars = Charset.availableCharsets();
        Set keys = chars.keySet();
        Iterator iterator = keys.iterator();

        Pattern pattern = Pattern.compile("encoding=\"([^\"]*?)\"");

        while (iterator.hasNext())
        {
            encoding = (String) iterator.next();
            String s = new String(bs, encoding);

            // If "<?xml " can be recognized.
            if (s.indexOf("<?xml ") > -1)
            {
                // If the file has assigned the encoding, return the
                // assigned recoding.
                Matcher matcher = pattern.matcher(s);
                if (matcher.find())
                {
                    encoding = matcher.group(1);
                }
                else
                {
                    String guessedEncoding = guessEncoding(file);
                    if (guessedEncoding != null)
                    {
                        encoding = guessedEncoding;
                    }
                }

                break;
            }
        }

        return encoding;
    }
}
