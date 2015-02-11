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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.reports.Constants;

/**
 * Offers some validation methods.
 */
public class Assert
{
    private static final Logger s_logger = Logger
            .getLogger(Assert.class.getName());

    /**
     * Asserts one object is not null.
     * <p>
     * 
     * A IllegalArgumentException will be throw if the object is null.
     * 
     * @param ob
     *            The object that must not be null.
     * @param name
     *            The display name of the object, it is used to create error
     *            information if the object is null.
     */
    public static void assertNotNull(Object ob, String name)
    {
        if (ob == null)
        {
            s_logger.error(name + " is null");
            throw new IllegalArgumentException(name + " is null");
        }
    }

    /**
     * Asserts a string is not empty.
     * <p>
     * A IllegalArgumentException will be throw if the String is empty.
     * <p>
     * Note: a string like " " is think to be empty.
     * 
     * @param s
     *            The object that must not be empty.
     * @param name
     *            The display name of the object, it is used to create error
     *            information if the object is null.
     */
    public static void assertNotEmpty(String s, String name)
    {
        if (s == null || s.trim().length() == 0)
        {
            ValidateException e1 = new ValidateException(name + " is empty", "msg_validate_text_empty");
            e1.addValue(name);
            s_logger.error(name + " is empty");
            throw e1;
        }
    }
    
    public static boolean assertNotEmpty(String s) {
    	if (s == null || s.trim().equals(""))
    		return false;
    	else
    		return true;
    }

    /**
     * Asseerts a string is "true" or "false".
     * <p>
     * A IllegalArgumentException will be throw if the String is empty or not
     * equals "true" or "false".
     * 
     * @param value
     *            The string that need to be "true" or "false".
     * @param name
     *            The display name of the string, used to create error message.
     */
    public static void assertIsBoolean(String value, String name)
    {
        assertNotEmpty(value, name);

        if (!"true".equals(value) && !"false".equals(value))
        {
            s_logger.error("Validate string: " + value);
            s_logger.error("Only accept 'true' or 'false'.");
            throw new IllegalArgumentException(name
                    + " is illegal, only accept 'true' or 'false'.");
        }
    }

    /**
     * Makes sure that a file is exist, others a
     * <code>IllegalArgumentException</code> with error message ("File (" +
     * <code>path</code> + ") is not exist") will be throw out.
     * 
     * @param path
     *            The path of file, can't be null.
     */
    public static void assertFileExist(String path)
    {
        assertNotNull(path, "File path");

        if (!new File(path).exists())
        {
            throw new IllegalArgumentException("File (" + path
                    + ") does not exist");
        }
    }

    /**
     * Makes sure that a file is exist, others a
     * <code>IllegalArgumentException</code> with error message ("File (" +
     * <code>path</code> + ") is not exist") will be throw out.
     * 
     * @param file
     *            The file to check, can't be null.
     */
    public static void assertFileExist(File file)
    {
        assertNotNull(file, "File");

        if (!file.exists())
        {
            throw new IllegalArgumentException("File (" + file.getPath()
                    + ") does not exist");
        }
    }

    /**
     * Asserts the expression is true.
     * 
     * @param isTrue
     *            The result of the expression.
     * @param msg
     *            Error message if the result is false.
     */
    public static void assertTrue(boolean isTrue, String msg)
    {
        if (!isTrue)
        {
            throw new IllegalArgumentException(msg);
        }
    }
    
    /**
     * Asserts the expression is true.
     * 
     * @param isTrue
     *            The result of the expression.
     * @param msg
     *            Error message if the result is false.
     */
    public static void assertFalse(boolean isFalse, String msg)
    {
        assertTrue(!isFalse, msg);
    }

    public static void assertIsInteger(String s)
    {
        try
        {
            Integer.parseInt(s.trim());
        }
        catch (Exception e)
        {
            try
            {
                Float f = Float.parseFloat(s);
                if (f < Integer.MIN_VALUE)
                {
                    ValidateException e1 = new ValidateException(
                            "The minimum value is -2147483648.",
                            "msg_validate_too_small");
                    e1.addValue("-2147483648");
                    throw e1;
                }
                if (f > Integer.MAX_VALUE)
                {
                    ValidateException e1 = new ValidateException(
                            "The maximum value is 2147483647.",
                            "msg_validate_too_big");
                    e1.addValue("2147483647");
                    throw e1;
                }
            }
            catch (ValidateException e2)
            {
                throw e2;
            }
            catch (Exception e3)
            {
                
            }
            ValidateException e1 = new ValidateException(s.trim()
                    + " can not be converted into an integer",
                    "msg_validate_not_int");
            e1.addValue(s.trim());
            throw e1;
        }
    }

    public static void assertIsFloat(String s)
    {
        try
        {
            float f = Float.parseFloat(s.trim());
            if (f < Float.MAX_VALUE * -1)
            {
                ValidateException e1 = new ValidateException(
                        "The minimum value is -3.4028235E38",
                        "msg_validate_too_small");
                e1.addValue("-3.4028235E38");
                throw e1;
            }
            
            if (f > Float.MAX_VALUE)
            {
                ValidateException e1 = new ValidateException(
                        "The maximum value is 3.4028235E38.",
                        "msg_validate_too_big");
                e1.addValue("3.4028235E38");
                throw e1;
            }
        }
        catch (ValidateException e1)
        {
            throw e1;
        }
        catch (Exception e)
        {
            ValidateException e1 = new ValidateException(s.trim()
                    + " can not be converted into a float",
                    "msg_validate_not_float");
            e1.addValue(s.trim());
            throw e1;
        }
    }

    public static void assertIsDate(String s, String format)
    {
        if (format == null)
        {
            format = Constants.REPORT_TXT_DATEFORMAT;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            sdf.parse(s);
        }
        catch (ParseException e)
        {
            ValidateException e1 = new ValidateException(s.trim()
                    + " can not be converted into a date",
                    "msg_validate_not_date");
            e1.addValue(s.trim());
            throw e1;
        }
    }

    public static void assertFloatBetween(Float obj, Float min, Float max)
    {
        if (obj == null)
        {
            return;
        }

        if (min != null)
        {
            if (obj < min)
            {
                ValidateException e1 = new ValidateException(obj
                        + " is less than " + min, "msg_validate_less");
                e1.addValue(obj.toString());
                e1.addValue(min.toString());
                throw e1;
            }
        }

        if (max != null)
        {
            if (obj > max)
            {
                ValidateException e1 = new ValidateException(obj
                        + " is greater than " + max, "msg_validate_greater");
                e1.addValue(obj.toString());
                e1.addValue(max.toString());
                throw e1;
            }
        }
    }

    public static void assertIntBetween(Integer obj, Integer min, Integer max)
    {
        if (obj == null)
        {
            return;
        }

        if (min != null)
        {
            if (obj < min)
            {
                ValidateException e1 = new ValidateException(obj
                        + " is less than " + min, "msg_validate_less");
                e1.addValue(obj.toString());
                e1.addValue(min.toString());
                throw e1;
            }
        }

        if (max != null)
        {
            if (obj > max)
            {
                ValidateException e1 = new ValidateException(obj
                        + " is greater than " + max, "msg_validate_greater");
                e1.addValue(obj.toString());
                e1.addValue(max.toString());
                throw e1;
            }
        }
    }
    
    public static void assertTextNotTooLong(String text, Integer length)
    {
        if (text == null || length == null)
        {
            return;
        }

        if (text.length() > length)
        {
            ValidateException e1 = new ValidateException("The length of \"" + text
                    + "\" is greater than " + length, "msg_validate_text_length");
            e1.addValue(text);
            e1.addValue(length.toString());
            throw e1;
        }
    }
    
}
