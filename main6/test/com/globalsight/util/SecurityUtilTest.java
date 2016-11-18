package com.globalsight.util;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2016/11/15.
 */
public class SecurityUtilTest
{
    @Test
    public void testEncryptPassword()
    {
        assertEquals("BE732951EA1C03CF117B1C5D9B0F5527", SecurityUtil.AES
                ("password"));
    }

    @Test
    public void testEncryptMD5Password()
    {
        try
        {
            assertEquals("{MD5}X03MO1qnZdYdgyfeuILPmQ==", SecurityUtil.encryptMD5Password
                    ("password"));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncryptSHAPassword()
    {
        try
        {
            assertEquals("{sha}W6ph5Mm5Pz8GgiULbPgzG37mj9g=", SecurityUtil
                    .encryptSHAPassword("password"));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsStrongPassword()
    {
        assertEquals(true, SecurityUtil.isStrongPassword("1234567Ab", 8));
        assertEquals(true, SecurityUtil.isStrongPassword("1234567Abc$", 8));
        assertEquals(true, SecurityUtil.isStrongPassword("AbcccdDc$", 8));

        assertEquals(false, SecurityUtil.isStrongPassword("1234567", 8));
        assertEquals(false, SecurityUtil.isStrongPassword("12345678", 8));
        assertEquals(false, SecurityUtil.isStrongPassword("1234567A", 8));
        assertEquals(false, SecurityUtil.isStrongPassword("1234567$", 8));
        assertEquals(false, SecurityUtil.isStrongPassword("AaaaBbbbbb", 8));
    }

    @Test
    public void testIsSame()
    {
        assertEquals(true, SecurityUtil.isSame(null, null));
        assertEquals(true, SecurityUtil.isSame("test", "test"));

        assertEquals(false, SecurityUtil.isSame(null, "test"));
        assertEquals(false, SecurityUtil.isSame("test", null));
        assertEquals(false, SecurityUtil.isSame("test", "test1"));
    }

    @Test
    public void testMD5()
    {
        assertEquals("5F4DCC3B5AA765D61D8327DEB882CF99", SecurityUtil.MD5("password"));
    }

    @Test
    public void testSHA()
    {
        assertEquals("5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8",
                SecurityUtil.SHA256("password"));
        assertEquals
                ("B109F3BBBC244EB82441917ED06D618B9008DD09B3BEFD1B5E07394C706A8BB980B1D7785E5976EC049B46DF5F1326AF5A2EA6D103FD07C95385FFAB0CACBC86", SecurityUtil.SHA512("password"));
    }

    @Test
    public void testCheckPassword() {
        assertEquals(true, SecurityUtil.checkPassword("password", "password"));
        assertEquals(true, SecurityUtil.checkPassword("password", "BE732951EA1C03CF117B1C5D9B0F5527"));
        assertEquals(true, SecurityUtil.checkPassword("password", "{MD5}X03MO1qnZdYdgyfeuILPmQ=="));
    }
}
