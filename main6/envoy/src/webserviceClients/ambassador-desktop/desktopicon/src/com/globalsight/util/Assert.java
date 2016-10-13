package com.globalsight.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;

public class Assert
{
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
                String msg = obj + " is less than " + min
                        + ". Please correct it";
                JOptionPane.showMessageDialog(null, msg);
                throw new IllegalArgumentException(msg);
            }
        }

        if (max != null)
        {
            if (obj > max)
            {
                String msg = obj + " is greater than " + max
                        + ". Please correct it";
                JOptionPane.showMessageDialog(null, msg);
                throw new IllegalArgumentException(msg);
            }
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
                String msg = obj + " is less than " + min
                        + ". Please correct it";
                JOptionPane.showMessageDialog(null, msg);
                throw new IllegalArgumentException(msg);
            }
        }

        if (max != null)
        {
            if (obj > max)
            {
                String msg = obj + " is greater than " + max
                        + ". Please correct it";
                JOptionPane.showMessageDialog(null, msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }

    public static void assertIsInteger(String s)
    {
        try
        {
            Integer.parseInt(s.trim());
        }
        catch (Exception e)
        {
            String msg = s.trim() + " can not be converted into an integer";
            JOptionPane.showMessageDialog(null, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertIsFloat(String s)
    {
        try
        {
            Float.parseFloat(s.trim());
        }
        catch (Exception e)
        {
            String msg = s.trim() + " can not be converted into a float";
            JOptionPane.showMessageDialog(null, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertIsDate(String s, String format)
    {
        if (format == null)
        {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            sdf.parse(s);
        }
        catch (ParseException e)
        {
            String msg = s.trim() + " can not be converted into a date";
            JOptionPane.showMessageDialog(null, msg);
            throw new IllegalArgumentException(msg);
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
            String msg = "The length of \"" + text + "\" is greater than "
                    + length;
            JOptionPane.showMessageDialog(null, msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
