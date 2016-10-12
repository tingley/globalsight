package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.GlobalSightLocale;

public class LocProfileMainHandlerTest
{
    private LocProfileMainHandler handler = new LocProfileMainHandler();
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown(){
        
    }
    
    @Test
    public void testGetDownloadFileName(){
        ArrayList<File> files = new ArrayList<File>();
        File file = new File("D:/TEST/L10nProfileWfTemplateReport-20160906 153903.xlsx");
        files.add(file);
        Object resultObject = (String) ClassUtil.testMethod(handler, "getDownloadFileName", files);
        Assert.assertEquals("L10nProfileWfTemplateReport-20160906 153903.xlsx", resultObject);
    }

}
