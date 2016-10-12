package com.globalsight.cxe.adapter.openoffice;

import java.io.File;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.everest.util.system.MockEnvoySystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;

public class OpenOfficeTagHelperTest
{
    private String odpContentXml = null;
    private String odsContentXml = null;
    private String odtContentXml = null;

    @Before
    public void init()
    {
        SystemConfiguration.setDebugInstance(new MockEnvoySystemConfiguration(
                new HashMap<String, String>()
                {
                    {
                        put("nokey", "novalue");
                    }
                }));

        odpContentXml = FileUtil.getResourcePath(OpenOfficeTagHelperTest.class,
                "files/odp/content.xml");
        odsContentXml = FileUtil.getResourcePath(OpenOfficeTagHelperTest.class,
                "files/ods/content.xml");
        odtContentXml = FileUtil.getResourcePath(OpenOfficeTagHelperTest.class,
                "files/odt/content.xml");
    }

    @Test
    public void testMergeOdp()
    {
        try
        {
            String keyWords = "By GlobalSight";
            File f = new File(odpContentXml);
            OpenOfficeHelper.replcaeFileSpace(f);
            String content = com.globalsight.util.FileUtil.readFile(f, "utf-8");
            String newContent = OpenOfficeTagHelper.mergeTag(content);
            Assert.assertTrue(newContent.contains(keyWords));
        }
        catch (Exception e)
        {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testMergeOds()
    {
        try
        {
            String keyWords = "Ireland,";
            File f = new File(odsContentXml);
            OpenOfficeHelper.replcaeFileSpace(f);
            String content = com.globalsight.util.FileUtil.readFile(f, "utf-8");
            String newContent = OpenOfficeTagHelper.mergeTag(content);
            Assert.assertTrue(newContent.contains(keyWords));
        }
        catch (Exception e)
        {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testMergeOdt()
    {
        try
        {
            String keyWords = "China and";
            File f = new File(odtContentXml);
            OpenOfficeHelper.replcaeFileSpace(f);
            String content = com.globalsight.util.FileUtil.readFile(f, "utf-8");
            String newContent = OpenOfficeTagHelper.mergeTag(content);
            Assert.assertTrue(newContent.contains(keyWords));
        }
        catch (Exception e)
        {
            Assert.fail(e.toString());
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(OpenOfficeTagHelperTest.class);
    }

}
