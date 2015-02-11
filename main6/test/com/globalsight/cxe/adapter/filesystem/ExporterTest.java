package com.globalsight.cxe.adapter.filesystem;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.everest.util.system.MockEnvoySystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.ClassUtil;

public class ExporterTest
{
    private Exporter exporter = null;
    private String root = null;

    @Before
    public void setUp() throws Exception
    {
        SystemConfiguration.setDebugInstance(new MockEnvoySystemConfiguration(
                new HashMap<String, String>()
                {
                    {
                        put("leverager.targetIndexing", "false");
                    }
                }));
        exporter = new Exporter(null, null);
        root = FileUtil.getResourcePath(ExporterTest.class, "testfiles");
    }

    @Test
    public void testnotUnicodeEscape()
    {
        String answerfilePath = root + "/JSUnicodeOption.js";
        String sourceFilePath = root + "/source/JSUnicodeOption.js";
        String expectedFilepath = root + "/JSUnicodeOptionExpected.js";

        copyFile(sourceFilePath, answerfilePath);
        File file = new File(answerfilePath);
        File expectedFile = new File(expectedFilepath);

        // test the method "notUnicodeEscape", this method changed the
        // answerFile's content
        ClassUtil.testMethod(exporter, "notUnicodeEscape", answerfilePath,
                "utf-8");
        Assert.assertTrue(FileUtil.fileCompareNoCareEndLining(expectedFile,
                file));
        file.delete();
    }

    @Test
    public void testconvertToHtmlEntity()
    {
        String[][] fileSets =
        {
                { "JSPEntityEscapeOption.jsp",
                        "JSPEntityEscapeOptionExpected.jsp", "iso-8859-1" },
                { "JSPEntityEscapeOption01.jsp",
                        "JSPEntityEscapeOptionExpected01.jsp", "iso-8859-1" } };

        for (int i = 0; i < fileSets.length; i++)
        {
            String answerFilePath = root + "/" + fileSets[i][0];
            String sourceFilePath = root + "/source/" + fileSets[i][0];
            String expectedFilepath = root + "/" + fileSets[i][1];
            String encoding = fileSets[i][2];

            copyFile(sourceFilePath, answerFilePath);
            File file = new File(answerFilePath);
            File expectedFile = new File(expectedFilepath);

            // test the method "convertToHtmlEntity", this method changed the
            // answerFile's content
            ClassUtil.testMethod(exporter, "convertToHtmlEntity",
                    answerFilePath, encoding);
            Assert.assertTrue(FileUtil.fileCompareNoCareEndLining(expectedFile,
                    file));
            // file.delete();
        }
    }
    
    /**
     * Test Exporter.handleExtraEscapeCharacter
     * @throws Exception 
     */
    public void testHandleExtraEscapeCharacter(String p_fileName, String p_encoding) 
         throws Exception
    {
        if (exporter == null)
        {
            setUp();
        }
        ClassUtil.testMethod(exporter, "handleExtraEscapeCharacter", p_fileName, p_encoding);
    }

    /**
     * Used to copy source file
     * 
     * @param inPath
     * @param outPath
     */
    private void copyFile(String inPath, String outPath)
    {
        try
        {
            File in = new File(inPath);
            File out = new File(outPath);
            FileInputStream fis = new FileInputStream(in);
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1)
            {
                fos.write(buf, 0, i);
            }
            fis.close();
            fos.close();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testLoadConvert()
    {
        String str = "login.note=Note\\:";
        String result = (String) ClassUtil.testMethod(exporter, "loadConvert", str);
        assertEquals("login.note=Note\\:", result);
        
        str = "login.note=Note2\\\\:";
        result = (String) ClassUtil.testMethod(exporter, "loadConvert", str);
        assertEquals("login.note=Note2\\\\:", result);
        
        str = "login.note=Note3\\r\\n\\t:";
        result = (String) ClassUtil.testMethod(exporter, "loadConvert", str);
        assertEquals("login.note=Note3\r\n\t:", result);
    }

}
