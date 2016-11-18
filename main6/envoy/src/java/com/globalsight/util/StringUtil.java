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

import com.globalsight.everest.company.Company;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{

    public static final String EMPTY_STRING = "";
    public static final String STRING_SEPARARTOR = ",";
    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static String transactSQLInjection(String str)
    {
        return str.replaceAll(".*([';]+|(--)+).*", " ");
    }
    public static boolean isEmpty(String s)
    {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static boolean isEmptyAndNull(String s)
    {
        return isEmpty(s) || "null".equalsIgnoreCase(s.trim());
    }

    public static boolean isNotEmptyAndNull(String s) {
        return !isEmptyAndNull(s);
    }
    
    /***
     * @deprecated just leave it here for testing purpose
     */
    private static String replaceOld(String src, String oldString,
            String newString)
    {
        if (!isEmpty(src) && oldString != null)
        {
            if (newString == null)
            {
                newString = "";
            }

            int index = src.indexOf(oldString);
            while (index > -1)
            {
                src = src.substring(0, index) + newString
                        + src.substring(index + oldString.length());
                index = src.indexOf(oldString, index + newString.length());
            }
        }

        return src;
    }

    /**
     * Get string value, default value is ""
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @return String value
     */
    public static String get(String s)
    {
        return get(s, "");
    }

    /**
     * Get string value, if string is null or empty, then return default value
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @param defaultString
     *            Default value
     * @return String value
     */
    public static String get(String s, String defaultString)
    {
        defaultString = isEmpty(defaultString) ? "" : defaultString;
        return isEmpty(s) ? defaultString : s;
    }

    /**
     * Get integer value through string, if string is null or empty, then return
     * -1 as default
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @return int value
     */
    public static int getInt(String s)
    {
        return getInt(s, -1);
    }

    /**
     * Get integer value through string, if string is null or empty, then
     * default integer value
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @param defaultValue
     *            Default int value
     * @return int value
     */
    public static int getInt(String s, int defaultValue)
    {
        if (isEmpty(s))
            return defaultValue;
        try
        {
            return Integer.parseInt(s);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * Get long value through string, if string is null or empty, then return
     * -1L as default
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @return long value
     */
    public static long getLong(String s)
    {
        return getLong(s, -1L);
    }

    /**
     * Get long value through string, if string is null or empty, then return
     * default long value
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @param defaultValue
     *            Default long value
     * @return long value
     */
    public static long getLong(String s, long defaultValue)
    {
        if (isEmpty(s))
            return defaultValue;
        try
        {
            return Long.parseLong(s);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * Get boolean value through string If string is 'yes' or 'true' either
     * uppercase or lowercase, return true. Otherwise will return false
     * 
     * @since 8.7.2
     * @param s
     *            String value
     * @return boolean Boolean value
     */
    public static boolean getBoolean(String s)
    {
        if (isEmpty(s))
            return false;
        s = s.trim();

        return "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }

    /**
     * Take place of String Replace method, refined for performance
     * @param src
     * @param oldString
     * @param newString
     * @return
     */
    public static String replace(String src, String oldString, String newString)
    {
        if (src == null || src.length() == 0 || oldString == null
                || oldString.length() == 0 || oldString.equals(newString))
            return src;

        newString = newString == null ? "" : newString;

        StringBuilder output = new StringBuilder();
        int start = 0;
        int index = src.indexOf(oldString);
        int oldLength = oldString.length();

        while (index > -1)
        {
            output.append(src.substring(start, index));
            output.append(newString);
            start = index + oldLength;
            index = src.indexOf(oldString, start);
        }

        output.append(src.substring(start));

        return output.toString();
    }
    
    /**
     * Take place of String Replace method, refined for big data
     * @param src
     * @param oldString
     * @param newString
     * @return
     */
    public static String replaceWithRE(String src, String re, String newString)
    {
        if (src == null || src.length() == 0 || re == null || re.length() == 0)
        {
            return src;
        }

        newString = newString==null ? "" : newString;

        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(src);
        StringBuilder output = new StringBuilder();
        int start = 0;
        while (m.find(start))
        {
            // Write out all characters before this matched region
            output.append(src.substring(start, m.start()));
            // Write out the replacement text. This will vary by method.
            output.append(newString);
            start = m.end();
        }
        // Handle chars after the last match
        output.append(src.substring(start));
        return output.toString();
    }
    
    public static String replaceWithRE(String src, Pattern p, Replacer replacer)
    {
        if (src == null || src.length() == 0 || p == null || replacer == null)
        {
            return src;
        }

        Matcher m = p.matcher(src);
        StringBuilder output = new StringBuilder();
        int start = 0;
        while (m.find(start))
        {
            // Write out all characters before this matched region
            output.append(src.substring(start, m.start()));
            // Write out the replacement text. This will vary by method.
            output.append(replacer.getReplaceString(m));
            start = m.end();
        }
        // Handle chars after the last match
        output.append(src.substring(start));
        return output.toString();
    }

    public static void replaceStringBuffer(StringBuffer src, String oldString,
            String newString, boolean oneTime)
    {
        if (src == null || src.length() == 0 || isEmpty(oldString) || oldString.equals(newString))
        {
            return;
        }

        newString = newString == null ? "" : newString;

        int oldLen = oldString.length();
        int newLen = newString.length();
        int index = src.indexOf(oldString);
        while (index > -1)
        {
            src = src.replace(index, index + oldLen, newString);
            index = src.indexOf(oldString, index + newLen);

            if (oneTime)
            {
                break;
            }
        }
    }

    /**
     * Get all integer values in a List through a string
     * @param src String value
     * @return List of all integer value
     */
    public static List getIncludedInts(String src)
    {
        List ints = new ArrayList();
        if (isEmpty(src))
            return ints;

        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find())
        {
            ints.add(matcher.group(0));
        }

        return ints;
    }

    /**
     * Remove BOM data
     * @param bs Bytes of content
     * @return Bytes which don't include BOM data
     */
    public static byte[] removeBom(byte[] bs)
    {
        if (bs == null || bs.length == 0)
            return bs;

        int unread = 0;

        if (bs.length > 3 && (bs[0] == (byte) 0x00) && (bs[1] == (byte) 0x00)
                && (bs[2] == (byte) 0xFE) && (bs[3] == (byte) 0xFF))
        {
            // encoding = "UTF-32BE";
            unread = 4;
        }
        else if (bs.length > 3 && (bs[0] == (byte) 0xFF)
                && (bs[1] == (byte) 0xFE) && (bs[2] == (byte) 0x00)
                && (bs[3] == (byte) 0x00))
        {
            // encoding = "UTF-32LE";
            unread = 4;
        }
        else if (bs.length > 2 && (bs[0] == (byte) 0xEF)
                && (bs[1] == (byte) 0xBB) && (bs[2] == (byte) 0xBF))
        {
            // encoding = "UTF-8";
            unread = 3;
        }
        else if (bs.length > 2 && (bs[0] == (byte) 0xFE)
                && (bs[1] == (byte) 0xFF))
        {
            // encoding = "UTF-16BE";
            unread = 2;
        }
        else if (bs.length > 1 && (bs[0] == (byte) 0xFF)
                && (bs[1] == (byte) 0xFE))
        {
            // encoding = "UTF-16LE";
            unread = 2;
        }

        if (unread > 0)
        {
            byte[] newbytes = new byte[bs.length - unread];
            for (int i = unread; i < bs.length; i++)
            {
                newbytes[i - unread] = bs[i];
            }

            return newbytes;
        }

        return bs;
    }

    /**
     * Remove BOM data
     * @param s String content
     * @param encoding Encoding
     * @return String content without BOM data
     * @throws UnsupportedEncodingException
     */
    public static String removeBom(String s, String encoding)
            throws UnsupportedEncodingException
    {
        if (isEmpty(s) || isEmpty(encoding))
            return s;

        byte[] bs = s.getBytes(encoding);
        return new String(removeBom(bs), encoding);
    }

    /**
     * Format percentage to two decimal places, like xx.xx%.
     * If the input is not a Number, return toString(), avoid throw exception.
     */
    public static String formatPCT(Object num)
    {
        if (num == null)
            return "";

        String result;
        if(num instanceof Number)
        {
            Locale en = new Locale("en");
            DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(en);
            result = df.format(((Number) num).intValue())+"%";
        }
        else
        {
            result = num.toString();
            if(!result.endsWith("%"))
            {
                result = result + "%";
            }
        }

        return result;
    }

    /**
     * Generate percentage value according with the special number
     *
     * @param p_num
     *            The decimal number
     * @return Percentage value
     */
    public static String formatPercent(float p_num)
    {
        float tmpF = (float) (((float) ((int) Math.floor(p_num * 100)) / 100));

        Locale en = new Locale("en");
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(en);
        df.applyPattern("0.00");

        return df.format(tmpF);
    }

    /**
     * Generate percentage value according with the special number and digits
     *
     * @param p_num
     *            The decimal number
     * @param p_digits
     *            The digit number after dot
     * @return Percentage value
     */
    public static String formatPercent(float p_num, int p_digits)
    {
        float tmpF = (float) (((float) ((int) Math.floor(p_num * 100)) / 100));

        Locale en = new Locale("en");
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(en);
        df.applyPattern("0.00");

        return df.format(tmpF);
    }

    /**
     * Compares 2 string, ignoring white space considerations.
     */
    public static boolean equalsIgnoreSpace(String source, String dest)
    {
        if (isEmptyAndNull(source) || isEmptyAndNull(dest))
            return false;

        source = replace(source, " ", "");
        dest = replace(dest, " ", "");

        return source.equalsIgnoreCase(dest);
    }

    /**
     * Get the sub string before the suffix string.
     * If can not find p_suffix, then return p_input.
     *
     * @param src
     *            input string
     * @param suffix
     *            special string
     *
     * @see StringUtilTest.testDelSuffix
     */
    public static String delSuffix(String src, String suffix)
    {
        if (isEmpty(src) || isEmpty(suffix))
            return get(src);

        if (src.endsWith(suffix))
        {
            int index = src.lastIndexOf(suffix);
            src = src.substring(0, index);
        }

        return src.trim();
    }

    /**
     * Join a list of Strings with a separator.  Objects are converted with
     * toString.  In the special case where the list is empty, the empty string
     * is returned.
     *
     * Be aware that splitting on the separator may not return the original
     * list of Strings.  This happens if the list is empty, or the separator
     * appears in one of the elements.
     */
    public static String join(String separator, List<? extends Object> objs)
    {
        if (objs == null)
        {
            throw new IllegalArgumentException("objs is null");
        }
        if (objs.isEmpty())
        {
            return EMPTY_STRING;
        }
        Iterator<? extends Object> i = objs.iterator();
        StringBuffer result = new StringBuffer(512);
        for (Iterator<? extends Object> iterator = objs.iterator(); iterator.hasNext();)
        {
            result.append(separator).append(iterator.next().toString());
        }
        return result.deleteCharAt(0).toString();
    }

    /** @see #join(String, List) */
    public static String join(String separator, Object... objs)
    {
        return join(separator, Arrays.asList(objs));
    }

    /**
     * Check if the value is included in the array
     * @param array
     * @param value
     * @return
     */
    public static boolean isIncludedInArray(String[] array, String value)
    {
        if (isEmpty(value) || array == null || array.length == 0)
            return false;

        for (String v : array)
        {
            if (v.equals(value))
                return true;
        }
        return false;
    }

    public static Set<String> split(String str)
    {
        return split(str, STRING_SEPARARTOR);
    }

    public static Set<String> split(String str, String separator)
    {
        Set<String> result = new HashSet<String>();
        String[] array = str.split(separator);
        for (String s : array)
        {
            if (isNotEmpty(s))
            {
                result.add(s);
            }
        }
        return result;
    }
    
    public static List<File> isDisableUploadFileType(Company company, List<File> fileList)
    {
        List<File> canNotUploadFiles = new ArrayList<File>();
        String disableUploadFileTypes = company.getDisableUploadFileTypes();
        if (isNotEmptyAndNull(disableUploadFileTypes))
        {
            Set<String> set = split(disableUploadFileTypes);
            for (int i = 0; i < fileList.size(); i++)
            {
                List<String> extensionList = new ArrayList<String>();
                try
                {
                    if (isSupportedZipFileFormat(fileList.get(i)))
                    {
                        if (CreateJobUtil.isZipFile(fileList.get(i)))
                        {
                            if (set.contains(".zip"))
                            {
                                canNotUploadFiles.add(fileList.get(i));
                                continue;
                            }
                            extensionList = unzipFile(fileList.get(i));
                        }
                        else if (CreateJobUtil.isRarFile(fileList.get(i)))
                        {
                            if (set.contains(".rar"))
                            {
                                canNotUploadFiles.add(fileList.get(i));
                                continue;
                            }
                            extensionList = unrarFile(fileList.get(i));
                        }
                        else if (CreateJobUtil.is7zFile(fileList.get(i)))
                        {
                            if (set.contains(".7z"))
                            {
                                canNotUploadFiles.add(fileList.get(i));
                                continue;
                            }
                            extensionList = un7zFile(fileList.get(i));
                        }

                        for (int j = 0; j < extensionList.size(); j++)
                        {
                            if (set.contains(extensionList.get(i)))
                            {
                                canNotUploadFiles.add(fileList.get(i));
                                break;
                            }
                        }
                    }
                    else
                    {
                        if (set.contains(getFileExtension(fileList.get(i).getName())))
                        {
                            canNotUploadFiles.add(fileList.get(i));
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
        return canNotUploadFiles;
    }
    
    private static boolean isSupportedZipFileFormat(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if ("rar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)
                || "7z".equalsIgnoreCase(extension))
        {
            return true;
        }
        return false;
    }
    
    private static List<String> unzipFile(File zipFile) throws ZipException
    {
        List<String> extensionList = new ArrayList<String>();
        ZipFile file = new ZipFile(zipFile);
        List fileHeaderList = file.getFileHeaders();
        String extension = "";
        for (int i = 0; i < fileHeaderList.size(); i++)
        {
            net.lingala.zip4j.model.FileHeader fileHeader = (net.lingala.zip4j.model.FileHeader) fileHeaderList
                    .get(i);
            if (fileHeader != null)
            {
                extension = getFileExtension(fileHeader.getFileName());
                extensionList.add(extension);
            }
        }
        return extensionList;
    }
    
    private static List<String> unrarFile(File rarFile) throws Exception
    {
        List<String> extensionList = new ArrayList<String>();
        Archive archive = new Archive(rarFile);
        FileHeader fileHeader = archive.nextFileHeader();
        String extension = "";
        while (fileHeader != null)
        {
            extension = getFileExtension(fileHeader.getFileNameString().trim());
            fileHeader = archive.nextFileHeader();
        }
        return extensionList;
    }
    
    private static List<String> un7zFile(File zip7zfile) throws Exception
    {
        List<String> extensionList = new ArrayList<String>();
        SevenZFile sevenZFile = new SevenZFile(zip7zfile);
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        String extension = "";
        while (entry != null)
        {
            extension = getFileExtension(entry.getName());
            entry = sevenZFile.getNextEntry();
        }
        return extensionList;
    }
    
    private static String getFileExtension(String fileName)
    {
        String extension = "";
        if (fileName.lastIndexOf(".") != -1)
        {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        return extension;
    }

    /**
     * Converts bytes to hex string.
     */
    public static String toHexString(byte[] bytes) {
        char[] resultCharArray = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes)
        {
            resultCharArray[index++] = HEX_DIGITS[b >>> 4 & 0xf];
            resultCharArray[index++] = HEX_DIGITS[b & 0xf];
        }
        return new String(resultCharArray);
    }

}
