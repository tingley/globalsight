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

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{

    public static final String EMPTY_STRING = "";

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
        {
            return src;
        }

        if (newString == null)
        {
            newString = "";
        }

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

        if (newString == null)
        {
            newString = "";
        }

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
        if (src == null || oldString == null || oldString.equals(newString))
        {
            return;
        }

        if (newString == null)
        {
            newString = "";
        }

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

    public static List getIncludedInts(String src)
    {
        List ints = new ArrayList();
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find())
        {
            String s = matcher.group(0);
            ints.add(s);
        }

        return ints;
    }

    // public static void main(String[] args)
    // {
    // String s = "12*2*45";
    // System.out.println(replace(s, "2*", "2*2"));
    // }

    public static byte[] removeBom(byte[] bs)
    {
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

    public static String removeBom(String s, String encoding)
            throws UnsupportedEncodingException
    {

        byte[] bs = s.getBytes(encoding);
        return new String(removeBom(bs), encoding);
    }

    /**
     * Format percentage to two decimal places, like xx.xx%.
     * If the input is not a Number, return toString(), avoid throw exception.
     */
    public static String formatPCT(Object num)
    {
        String result;
        if(num instanceof Number)
        {
            Locale en = new Locale("en");
            DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(en);
//            df.applyPattern("0.00");

            // Changes default Rounding Mode
//            df.setRoundingMode(RoundingMode.DOWN);
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

//        String pattern = "##.";
//        for (int i = 0; i < p_digits; i++)
//        {
//            pattern = pattern.concat("0");
//        }
//
//        DecimalFormat ndf = new DecimalFormat(pattern);

        return df.format(tmpF);
    }

    /**
     * Compares 2 string, ignoring white space considerations.
     */
    public static boolean equalsIgnoreSpace(String p_str1, String p_str2)
    {
        if (p_str1 == null || p_str2 == null)
            return false;

        String[] arr1 = p_str1.split("\\s");
        String[] arr2 = p_str2.split("\\s");
        StringBuffer sb1 = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        for (String temp : arr1)
        {
            sb1.append(temp);
        }
        for (String temp : arr2)
        {
            sb2.append(temp);
        }

        return sb1.toString().equalsIgnoreCase(sb2.toString());
    }

    /**
     * Get the sub string before the suffix string.
     * If can not find p_suffix, then return p_input.
     *
     * @param p_input
     *            input string
     * @param p_suffix
     *            special string
     *
     * @see StringUtilTest.testDelSuffix
     */
    public static String delSuffix(String p_input, String p_suffix)
    {
        if (p_input == null || p_input.trim().length() == 0)
        {
            return "";
        }

        if (p_input.endsWith(p_suffix))
        {
            int index = p_input.lastIndexOf(p_suffix);
            p_input = p_input.substring(0, index);
        }

        return p_input.trim();
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
        StringBuilder r = new StringBuilder(i.next().toString());
        while (i.hasNext()) {
            r.append(separator);
            r.append(i.next().toString());
        }
        return r.toString();
    }

    /** @see #join(String, List) */
    public static String join(String separator, Object... objs)
    {
        return join(separator, Arrays.asList(objs));
    }

    /**
     * Check if the value is included in the array
     * @param strArray
     * @param value
     * @return
     */
    public static boolean isIncludedInArray(String[] strArray, String value)
    {
        if (strArray == null || value == null)
        {
            return false;
        }

        for (int i = 0; i < strArray.length; i++)
        {
            String str = strArray[i];
            if (value.equals(str))
            {
                return true;
            }
        }

        return false;
    }

    public static Set<String> split(String p_str)
    {
        return split(p_str, ",");
    }

    public static Set<String> split(String p_str, String p_sep)
    {
        Set<String> result = new HashSet<String>();
        String[] arr = p_str.split(p_sep);
        for (String str : arr)
        {
            if (str != null && str.trim().length() > 0)
            {
                result.add(str);
            }
        }
        return result;
    }
}
