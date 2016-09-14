package com.globalsight.everest.webapp.pagehandler.login;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.util.ClassUtil;

public class PasswordTest
{

    @Test
    public final void testGenerater()
    {
        Password password = new Password();
        // Random password can not have "<".
        String pass = "";
        for (int i = 0; i < 1000; i++)
        {
            pass = (String) ClassUtil.testMethod(password, "generater", 8);
            Assert.assertTrue(pass.indexOf("<") == -1);
        }
    }
}
