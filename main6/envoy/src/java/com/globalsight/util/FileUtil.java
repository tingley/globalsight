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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * A util class, let operate file more easy.
 * 
 */
public class FileUtil
{
    public static String lineSeparator = java.security.AccessController
            .doPrivileged(new sun.security.action.GetPropertyAction(
                    "line.separator"));
    static private final Logger logger = Logger.getLogger(FileUtil.class);

    static public final String NOT_UTF = "Not UTF";
    static public final String UTF8 = "UTF-8";
    static public final String UTF16 = "UTF-16";
    static public final String UTF16LE = "UTF-16LE";
    static public final String UTF16BE = "UTF-16BE";
    static public final int NOT_UTF_TYPE = 0;
    static public final int UTF8_TYPE = 1;
    static public final int UTF16LE_TYPE = 2;
    static public final int UTF16BE_TYPE = 3;
    static public final int UTF16_TYPE = 4;
    static public final ArrayList<String> UTF_FORMATS = new ArrayList<String>();

    static
    {
        if (UTF_FORMATS.size() == 0)
        {
            UTF_FORMATS.add(NOT_UTF);
            UTF_FORMATS.add(UTF8);
            UTF_FORMATS.add(UTF16LE);
            UTF_FORMATS.add(UTF16BE);
            UTF_FORMATS.add(UTF16);
        }
    }

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

    public static void copyFolder(File sF, File tF) throws IOException
    {
        String sP = sF.getAbsolutePath().replace("\\", "/");
        String tP = tF.getAbsolutePath().replace("\\", "/");

        List<File> alls = getAllFiles(sF);
        for (File f : alls)
        {
            String path = f.getAbsolutePath().replace("\\", "/");
            path = path.replace(sP, tP);

            copyFile(f, new File(path));
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
     * Get all files and directories under the specified fold.
     * 
     * @param root
     * @return
     */
    public static List<File> getAllFilesAndFolders(File root,
            boolean containEmpty)
    {
        Assert.assertFileExist(root);

        List<File> files = new ArrayList<File>();

        if (containEmpty || (!containEmpty && !isEmpty(root)))
        {
            files.add(root);
        }

        if (root.isDirectory())
        {
            File[] fs = root.listFiles();
            for (File f : fs)
            {
                if (containEmpty || (!containEmpty && !isEmpty(f)))
                {
                    files.addAll(getAllFilesAndFolders(f, containEmpty));
                }
            }
        }

        return files;
    }

    /**
     * Check whether a directory is empty
     * 
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

    public static File changeExtension(File file, String extension)
    {
        String name = file.getName();
        String nameWithoutExtension = name.substring(0, name.lastIndexOf("."));
        String newName = nameWithoutExtension + extension;

        File newFile = new File(file.getParent(), newName);
        if (file.renameTo(newFile))
        {
            return newFile;
        }

        return file;
    }

    public static byte[] readFile(File file, int size) throws IOException
    {
        return readFile(new FileInputStream(file), size);
    }

    /**
     * Reads bytes from given input stream with specified length.
     */
    public static byte[] readFile(InputStream in, int size) throws IOException
    {
        byte[] b = new byte[size];
        try
        {
            in.read(b, 0, size);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }

        return b;
    }

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

    public static void writeFileWithBom(File file, String content,
            String encoding) throws IOException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
        }

        FileOutputStream out = null;

        try
        {
            out = new FileOutputStream(file);
            writeBom(out, encoding);
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
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
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
                    logger.error(e.getMessage(), e);
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
                    logger.error(e.getMessage(), e);
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

        if (fileEncoding == null)
        {
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

            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            byte[] b = null;

            if (UTF8.equals(encoding))
            {
                if (buf[0] != (byte) 0xef && buf[1] != (byte) 0xbb
                        && buf[2] != (byte) 0xbf)
                {
                    b = new byte[buf.length + 3];
                    b[0] = (byte) 0xef;
                    b[1] = (byte) 0xbb;
                    b[2] = (byte) 0xbf;
                    System.arraycopy(buf, 0, b, 3, buf.length);
                }
            }
            else if (UTF16LE.equals(encoding))
            {
                if (buf[0] != (byte) 0xff && buf[1] != (byte) 0xfe)
                {
                    b = new byte[buf.length + 2];
                    b[0] = (byte) 0xff;
                    b[1] = (byte) 0xfe;
                    System.arraycopy(buf, 0, b, 2, buf.length);
                }
            }
            else if (UTF16BE.equals(encoding))
            {
                if (buf[0] != (byte) 0xfe && buf[1] != (byte) 0xff)
                {
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
     * Writes a string to a file, creating the directory if necessary. The write
     * is done atomically by writing to a temporary file, then renaming the
     * temporary file to the final name. This avoids anyone trying to read the
     * file while it is being written. The temporary file ends in .tmp.
     */
    public static void writeFileAtomically(File file, String content,
            String encoding) throws IOException
    {
        File parent = file.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }

        File tmpFile = File.createTempFile("GSFileUtilWFA", ".tmp", parent);
        try
        {
            Writer out = new OutputStreamWriter(new FileOutputStream(tmpFile),
                    encoding);
            out.write(content);
            out.flush();
            out.close();
            if (!tmpFile.renameTo(file))
            {
                throw new IOException("Failed to rename " + tmpFile + " to "
                        + file);
            }
        }
        finally
        {
            deleteTempFile(tmpFile);
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
        boolean findEncoding = false;

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
                    findEncoding = true;
                }
                else
                {
                    String guessedEncoding = guessEncoding(file);
                    if (guessedEncoding != null)
                    {
                        encoding = guessedEncoding;
                        findEncoding = true;
                    }
                }

                break;
            }
        }

        return findEncoding ? encoding : "UTF-8";
    }

    public static String unUnicode(String s)
    {
        char[] in = s.toCharArray();
        int off = 0;
        int len = s.length();
        char[] convtBuf = s.toCharArray();

        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end)
        {
            aChar = in[off++];
            if (aChar == '\\')
            {
                aChar = in[off++];
                if (aChar == 'u')
                {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        aChar = in[off++];
                        switch (aChar)
                        {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                }
                else
                {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    out[outLen++] = aChar;
                }
            }
            else
            {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }

    /**
     * Delete a temporary file. If the file does not exist, we assume it has
     * aready been cleaned up and succeed quietly. Warns but does not throw an
     * exception if deletion fails. This method should not be called on a
     * directory.
     * 
     * Use this method rather than just calling File.delete because it will log
     * a warning, and may in the future take other measures to delete the file
     * if it can't immediately.
     */
    public static void deleteTempFile(File tmpFile)
    {
        if (!tmpFile.exists())
        {
            return;
        }
        if (!tmpFile.delete())
        {
            logger.warn("Failed to delete temporary file " + tmpFile
                    + "; something is probably holding it open");
        }
    }

    /**
     * Deletes the file
     * 
     * @param f
     */
    public static void deleteFile(File f)
    {
        if (!f.exists())
            return;

        if (f.isDirectory())
        {
            File[] fs = f.listFiles();
            for (File cf : fs)
            {
                deleteFile(cf);
            }
        }

        f.delete();
    }

    /**
     * Verify if the format is an UTF format
     * 
     * @param p_format
     *            format
     * @return boolean If the string is UTF-8, UTF-16, UTF-16LE, UTF-16BE then
     *         return true, otherwise false
     * @since 8.2
     */
    public static boolean isUTFFormat(String p_format)
    {
        if (StringUtil.isEmpty(p_format))
            return false;

        return UTF_FORMATS.contains(p_format);
    }

    /**
     * Get corresponding UTF type string according with its type value
     * 
     * @param p_type
     *            UTF type value
     * @return java.lang.String UTF string such as UTF-8, UTF-16LE, UTF-16BE
     *         etc.
     * @since 8.2
     */
    public static String getUTFFormat(int p_type)
    {
        if (p_type < 0 || p_type >= UTF_FORMATS.size())
            return null;
        else
            return UTF_FORMATS.get(p_type);
    }

    /**
     * Validate if special file needs to process the BOM information For now, we
     * consider 2 types of file, HTML and XML
     * 
     * @param p_fileName
     *            filename
     * @return boolean Return true if the file is type of HTML or XML or RESX,
     *         otherwise return false;
     * @since 8.2
     */
    public static boolean isNeedBOMProcessing(String p_fileName)
    {
        if (StringUtil.isEmpty(p_fileName))
            return false;

        String tmp = p_fileName.toLowerCase();
        return tmp.endsWith(".htm") || tmp.endsWith(".html")
                || tmp.endsWith(".xml") || tmp.endsWith(".resx");
    }

    public static boolean isWindowsReturnMethod(String p_filename)
    {
        if (StringUtil.isEmpty(p_filename))
            return false;

        String tmp = "";
        boolean isWindowsReturnMethod = false;
        byte[] buf = new byte[4096];
        try
        {
            File file = new File(p_filename);
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            while (!isWindowsReturnMethod && (bis.read(buf) != -1))
            {
                tmp = new String(buf);
                if (tmp.indexOf("\r\n") != -1)
                {
                    isWindowsReturnMethod = true;
                    break;
                }
            }
            bis.close();
        }
        catch (Exception e)
        {
            return false;
        }
        return isWindowsReturnMethod;
    }

    /**
     * Check if the filename contains Windows path separator.
     * 
     * @param p_filename
     *            The source filename
     * @return boolean true -- using Windows path separator, default value false
     *         -- using Unix/Linux/Mac path separator.
     */
    public static boolean isWindowsPathSeparator(String p_filename)
    {
        if (StringUtil.isEmpty(p_filename))
            return true;
        if (p_filename.indexOf("/") != -1)
            return false;
        else if (p_filename.indexOf("\\") != -1)
            return true;
        return false;
    }

    /**
     * Change file separator to fit for current OS
     * 
     * @param p_filename
     *            Filename
     * @return String Filename with common file separator by OS
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static String commonSeparator(String p_filename)
    {
        if (StringUtil.isEmpty(p_filename))
            return p_filename;
        String common = StringUtil.replace(p_filename, "\\", File.separator);
        common = StringUtil.replace(common, "/", File.separator);
        return common;
    }

    /**
     * Get the id of a file
     * 
     * @param data
     * @return
     */
    public static String getFileNo(String data)
    {
        byte[] bytes = getMD5(data);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(bytes, 0, 8);
        return String.valueOf(Math.abs(buf.getLong(0)));
    }

    private static byte[] getMD5(String s)
    {
        MessageDigest digest;
        try
        {
            digest = java.security.MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e); // can't happen
        }
        digest.update(s.getBytes(Charset.forName("UTF-8")));
        return digest.digest();
    }
}
