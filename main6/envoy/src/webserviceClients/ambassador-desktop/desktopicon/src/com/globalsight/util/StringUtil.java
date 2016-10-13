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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{

    public static final String EMPTY_STRING = "";

    public static boolean isEmpty(String s)
    {
        return s == null || s.trim().length() == 0;
    }

    public static String replace(String src, String oldString, String newString)
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
     * Compares the numbers in two strings.
     */
    public static int compareStringNum(String first, String second)
            throws Exception
    {
        String[] firstS = first.split("\\.");
        String[] secondS = second.split("\\.");
        try
        {
            int length = firstS.length > secondS.length ? firstS.length
                    : secondS.length;
            for (int i = 0; i < length; i++)
            {
                if (i < firstS.length)
                {
                    int firstNumber = Integer.parseInt(firstS[i]);
                    if (i < secondS.length)
                    {
                        int secondNumber = Integer.parseInt(secondS[i]);
                        if (firstNumber > secondNumber)
                        {
                            return 1;
                        }
                        else if (firstNumber < secondNumber)
                        {
                            return -1;
                        }
                    }
                    else
                    {
                        return 1;
                    }
                }
                else
                {
                    return -1;
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            throw new Exception(nfe);
        }

        return 0;
    }
}
