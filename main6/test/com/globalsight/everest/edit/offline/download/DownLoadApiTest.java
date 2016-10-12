package com.globalsight.everest.edit.offline.download;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

public class DownLoadApiTest
{
    private DownLoadApi api = new DownLoadApi();
    
    @Test
    public void testGetMtTmxName() throws Exception{
        
        Method method = api.getClass().getDeclaredMethod("getMtTmxName", String.class);
        method.setAccessible(true);
        String jobName = "test2_2546";
        Object res = method.invoke(api, jobName);
        Assert.assertEquals("test2_MT_2546", res);
    }
}