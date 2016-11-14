package com.globalsight.util;

import com.globalsight.webservices.AmbassadorUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Utils for security such as password validation and creation
 * @author Vincent Yan
 * @date 11/11/2016
 * @since 8.7.3
 */
public class SecurityUtil
{
    static final String MD5 = "MD5";
    static final String PREFIX_MD5 = "{MD5}";
    static final String SHA = "SHA";
    static final String PREFIX_SHA = "{sha}";
    static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static boolean isStrongPassword(String password, int minLength)
    {
        if (StringUtil.isEmpty(password) || password.length() < minLength)
            return false;

        Pattern[] patterns = {
                Pattern.compile("\\d"),
                Pattern.compile("[a-z]"),
                Pattern.compile("[A-Z]"),
                Pattern.compile("\\W")
        };

        int score = 0;
        for (Pattern pattern : patterns)
        {
            if (pattern.matcher(password).find())
                score++;
        }

        switch (score)
        {
            case 1:
            case 2:
                return false;
            case 3:
            case 4:
                return true;
        }
        return false;
    }

    public static boolean isSame(String pass1, String pass2)
    {
        return pass1 != null ? pass1.equals(pass2) : pass2 == null;
    }

    public static boolean checkPassword(String password, String storedPassword)
    {
        if (isSame(password, storedPassword))
            return true;

        if (StringUtil.isNotEmpty(password))
        {
            try
            {
                if (encryptMD5Password(password).equals(storedPassword))
                    return true;
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
            try
            {
                if (encryptSHAPassword(password).equals(storedPassword))
                    return true;
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static String encryptPassword(String password) {
        try
        {
            password = AmbassadorUtil.encryptionString(password);
        }
        catch (Exception e)
        {
        }
        return password;
    }

    public static String encryptMD5Password(String passwd) throws NoSuchAlgorithmException
    {
        byte[] md5Msg = MessageDigest.getInstance(MD5).digest(
                passwd.getBytes());
        return PREFIX_MD5 + byteArrayToHex(md5Msg);
    }

    public static String encryptSHAPassword(String passwd) throws NoSuchAlgorithmException
    {
        byte[] shaMsg = MessageDigest.getInstance(SHA).digest(
                passwd.getBytes());
        return PREFIX_SHA + byteArrayToHex(shaMsg);
    }

    public static String MD5(final String strText)
    {
        String result = null;
        try
        {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(strText.getBytes());
            result = byteArrayToHex(md5);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static String SHA256(final String strText)
    {
        return SHA(strText, "SHA-256");
    }

    public static String SHA512(final String strText)
    {
        return SHA(strText, "SHA-512");
    }

    private static String SHA(final String strText, final String strType)
    {
        String strResult = null;

        if (strText != null && strText.length() > 0)
        {
            try
            {
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                messageDigest.update(strText.getBytes());
                byte byteBuffer[] = messageDigest.digest();

                strResult = byteArrayToHex(byteBuffer);
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    public static String byteArrayToHex(byte[] byteArray)
    {
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray)
        {
            resultCharArray[index++] = HEX_DIGITS[b >>> 4 & 0xf];
            resultCharArray[index++] = HEX_DIGITS[b & 0xf];
        }
        return new String(resultCharArray);
    }
}
