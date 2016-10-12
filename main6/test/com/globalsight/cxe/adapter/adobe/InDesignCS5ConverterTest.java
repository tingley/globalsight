package com.globalsight.cxe.adapter.adobe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Priority;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.util.file.FileWaiter;

public class InDesignCS5ConverterTest
{
    private static final String STATUS_FILE_SUFFIX = ".status";
    private static final String disableMsg = "Unit test from InDesignConverterTest is disabled,"
            + " you can set value (\"test.idcs5.enable\", \"test.idcs5.enable\")"
            + " in user.build.properties to enable it.";
    
    private static String propertyEnable = "test.idcs5.enable";
    private static String propertyWinfiles = "test.idcs5.winfiles";

    private boolean enableTest = false;
    private File winfileFolder = null;
    private String sourceLocale = null;
    private String targetLocale = null;

    @BeforeClass
    public static void staticInit()
    {
        // for debug purpose
        //System.setProperty(propertyEnable, "true");
        //System.setProperty(propertyWinfiles, "E:\\Builds\\winfiles_cs5");
    }

    @Before
    public void init()
    {
        String enable = System.getProperty(propertyEnable);
        String winfiles = System.getProperty(propertyWinfiles);
        
        System.out.println(propertyEnable + " = " + enable);
        System.out.println(propertyWinfiles + " = " + winfiles);

        if ("true".equalsIgnoreCase(enable))
        {
            enableTest = true;
        }

        if (winfiles == null)
        {
            enableTest = false;
        }
        else
        {
            winfileFolder = new File(winfiles);
            if (!winfileFolder.exists())
            {
                enableTest = false;
            }
        }

        if (enableTest)
        {
            sourceLocale = "en_UK";
            targetLocale = "zh_CN";
        }
    }

    @After
    public void clean()
    {
        enableTest = false;
        winfileFolder = null;
    }

    @Test
    public void testConvert() throws Exception
    {
        if (!enableTest)
        {
            System.out.println(disableMsg);
            Assert.assertTrue(true);
            return;
        }

        String statusFile = null;

        try
        {
            String toFolder = winfileFolder.getPath() + "/indd/" + sourceLocale;
            String[] fileList = { "import/indd_cs5_sample_document.indd" };
            String cmdFile = "import/indd_cs5_sample_document.im_command";

            statusFile = copyFilesToWinfiles(toFolder, fileList, cmdFile);
            FileWaiter fw = new FileWaiter(10 * 1000, 10 * 60 * 1000, statusFile, true);
            fw.waitForFile();

            File statusF = new File(statusFile);
            String status = statusInfo(statusF);
            if (status != null)
            {
                throw new Exception(status);
            }
            else
            {
                String xmlFile = FileUtils.getPrefix(statusFile) + ".xml";
                String content = FileUtils.read(new File(xmlFile), "UTF-8");
                
                Assert.assertTrue(content.contains("Welocalize - About Us"));
            }
        }
        finally
        {
            cleanTestFiles(statusFile);
        }
    }

    @Test
    public void testConvertBack() throws Exception
    {
        if (!enableTest)
        {
            System.out.println(disableMsg);
            Assert.assertTrue(true);
            return;
        }

        String statusFile = null;

        try
        {
            String toFolder = winfileFolder.getPath() + "/indd/" + targetLocale;
            String[] fileList = { "export/indd_cs5_sample_document.indd",
                    "export/indd_cs5_sample_document.xml", "export/indd_cs5_sample_document.xmp" };
            String cmdFile = "export/indd_cs5_sample_document.ex_command";

            statusFile = copyFilesToWinfiles(toFolder, fileList, cmdFile);
            FileWaiter fw = new FileWaiter(10 * 1000, 10 * 60 * 1000, statusFile, true);
            fw.waitForFile();

            File statusF = new File(statusFile);
            String status = statusInfo(statusF);
            if (status != null)
            {
                throw new Exception(status);
            }
            else
            {
                Assert.assertTrue(true);
            }
        }
        finally
        {
            cleanTestFiles(statusFile);
        }
    }

    /**
     * Read status file
     * @param p_file
     * @return null if no error
     */
    private static String statusInfo(File p_file)
    {
        BufferedReader br = null;
        String errorLine = null;
        try
        {
            br = new BufferedReader(new FileReader(p_file));
            errorLine = br.readLine();
            errorLine = errorLine.substring(6); // error=
            int error = Integer.parseInt(errorLine);

            return error == 0 ? null : errorLine;
        }
        catch (NumberFormatException nfe)
        {
            return errorLine;
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        finally
        {
            FileUtils.closeSilently(br);
        }
    }

    /**
     * Copy files to winfiles folder
     * @return status file path
     * @throws Exception
     */
    private String copyFilesToWinfiles(String toFolder, String[] fileList, String cmdFile)
            throws Exception
    {
        File toF = new File(toFolder);

        if (toF.exists() && toF.isFile())
        {
            toF.delete();
        }

        if (!toF.exists())
        {
            toF.mkdirs();
        }

        File command = new File(FileUtil.getResourcePath(InDesignCS5ConverterTest.class, cmdFile));
        String statusFileName = toF.getPath() + "/" + FileUtils.getPrefix(command.getName()) + STATUS_FILE_SUFFIX;

        for (String fstr : fileList)
        {
            File f = new File(FileUtil.getResourcePath(InDesignCS5ConverterTest.class, fstr));
            FileUtils.copyFile(f, new File(toF, f.getName()));
        }

        // copy command file in the last
        FileUtils.copyFile(command, new File(toF, command.getName()));

        return statusFileName;
    }

    /**
     * Clean conversion files
     * @param statusFile
     */
    private void cleanTestFiles(String statusFile)
    {
        String prefix = FileUtils.getPrefix(statusFile);

        FileUtils.deleteSilently(prefix + STATUS_FILE_SUFFIX);
        FileUtils.deleteSilently(prefix + ".xml");
        FileUtils.deleteSilently(prefix + ".indd");
        FileUtils.deleteSilently(prefix + ".xmp");
        FileUtils.deleteSilently(prefix + ".im_command");
        FileUtils.deleteSilently(prefix + ".ex_command");
        FileUtils.deleteSilently(prefix + ".pdf");
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(InDesignCS5ConverterTest.class);
    }

}
