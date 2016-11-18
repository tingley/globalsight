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
    static final String SHA_256 = "SHA-256";
    static final String SHA_512 = "SHA-512";
    static final String AES = "AES";
    static final String PREFIX_SHA = "{sha}";


    /**
     * Verify if password is fit for strength password rules
     * @param password Input password which need to be verified
     * @param minLength Minimum length of password should be
     * @return if the password is strong enough (fit for at least 3 rules), will return true. Otherwise, false will be returned.
     */
    public static boolean isStrongPassword(String password, int minLength)
    {
        if (StringUtil.isEmpty(password) || password.length() < minLength)
            return false;

        Pattern[] patterns = {
                Pattern.compile("\\d"),             //Contain digits 0 - 9
                Pattern.compile("[a-z]"),           //Contain lowercase characters
                Pattern.compile("[A-Z]"),          //Contain uppercase characters
                Pattern.compile("\\W")            //Contain special characters like '$','%'...
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

    /**
     * Check if password is correct
     * Currently, GlobalSight uses different ways to generate/process password which maybe
     * generated from different way. This means there are ways to test if the password fit for
     * any one of different encryption algorithms.
     * All ways to check password are list as below,
     * 1. Plain text
     * 2. MD5. It adds prefix string "{MD5}" before md5 string which is coded using Apache Base64 tool
     * 3. SHA. It adds prefix string "{sha}" before sha string which is codes using Apache Base64 tool
     * 4. AES. It uses
     * @param password
     * @param storedPassword
     * @return
     */
    public static boolean checkPassword(String password, String storedPassword)
    {
        //TODO: After some later builds, we can same the algorithms in generating/checking password
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

            if (AES(password).equals(storedPassword))
                return true;

            if (MD5(password).equals(storedPassword))
                return true;

            if (SHA(password, SHA_256).equals(storedPassword) || SHA(password, SHA_512).equals(storedPassword))
                return true;
        }

        return false;
    }

    public static String AES(String password) {
        if (StringUtil.isEmpty(password))
            return password;

        try
        {
            //TODO: It's better to move detail codes from AmbassadorUtil.encryptionString() to here
            password = AmbassadorUtil.encryptionString(password);
        }
        catch (Exception e)
        {
        }
        return password;
    }

    /**
     * Encrypt password using MD5 algorithms
     * @param passwd
     * @return
     * @throws NoSuchAlgorithmException
     * @deprecated Uing MD5(String) to instead of this method
     */
    public static String encryptMD5Password(String passwd) throws NoSuchAlgorithmException
    {
        if (StringUtil.isEmpty(passwd))
            return passwd;

        byte[] md5Msg = MessageDigest.getInstance("MD5").digest(
                passwd.getBytes());
        return PREFIX_MD5 + new String(new org.apache.commons.codec.binary.Base64().encode(md5Msg));
    }

    /**
     * Encrypt password using SHA algorithms
     * @param passwd
     * @return
     * @throws NoSuchAlgorithmException
     * @deprecated Using SHA(String password, String encryptType) instead of this method
     */
    public static String encryptSHAPassword(String passwd) throws NoSuchAlgorithmException
    {
        if (StringUtil.isEmpty(passwd))
            return passwd;

        byte[] shaMsg = MessageDigest.getInstance(SHA).digest(
                passwd.getBytes());
        return PREFIX_SHA + new String(new org.apache.commons.codec.binary.Base64().encode(shaMsg));
    }

    public static String MD5(final String strText)
    {
        String result = null;
        try
        {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(strText.getBytes());
            result = StringUtil.toHexString(md5);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static String SHA256(final String strText)
    {
        return SHA(strText, SHA_256);
    }

    public static String SHA512(final String strText)
    {
        return SHA(strText, SHA_512);
    }

    /**
     * Encrypt string with SHA algorithms.
     * To enhance the security, this method use SHA-256/SHA-512 to encrypt string
     * @param string String to be encrypted
     * @param type Algorithms type, can be set to SHA-256 or SHA-512
     * @return Encrypted string
     */
    private static String SHA(final String string, final String type)
    {
        String strResult = null;

        if (string != null && string.length() > 0)
        {
            try
            {
                MessageDigest messageDigest = MessageDigest.getInstance(type);
                messageDigest.update(string.getBytes());
                byte byteBuffer[] = messageDigest.digest();

                strResult = StringUtil.toHexString(byteBuffer);
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }

        return strResult;
    }
}
