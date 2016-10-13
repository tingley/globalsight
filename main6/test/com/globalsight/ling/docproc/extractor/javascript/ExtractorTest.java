package com.globalsight.ling.docproc.extractor.javascript;

import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;

public class ExtractorTest extends BaseExtractorTestClass
{
    private File sourceFile = null;
    private File answerFile = null;
    private File roundtripFile = null;

    private HashMap<String, ArrayList<FileSet>> fileSets = new HashMap<String, ArrayList<FileSet>>();
    private static final String UTF8 = "UTF-8";
    private Extractor extractor;

    @Before
    public void setUp()
    {
        initExtractor();
        fileSets = initFileSet();
    }

    /**
     * JsFilterRegex in JS Filter is "", means use no filter
     */
    @Test
    public void testExtractor1()
    {
        ArrayList<FileSet> fileSet = (ArrayList<FileSet>) fileSets
                .get("testExtractor1");
        if (fileSet != null && fileSet.size() > 0)
        {
            Iterator<FileSet> it = fileSet.iterator();
            try
            {
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    sourceFile = fs.getSourceFile();
                    answerFile = fs.getAnswerFile();
                    roundtripFile = fs.getRoundtripFile();
                    // Get file content
                    String gxml = getFileContent(sourceFile, extractor, UTF8,
                            "");
                    String answerContent = getTranslatableTextContent(gxml);
                    File tmpFile = new File(answerFile.getParentFile()
                            .getAbsoluteFile()
                            + File.separator + answerFile.getName() + ".tmp");
                    // Generate files for compare
                    generateFile(tmpFile, answerContent, UTF8);
                    if (fileCompareNoCareEndLining(tmpFile, answerFile))
                    {
                        tmpFile.delete();
                        // generate target file
                        byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                        String s = new String(mergeResult, UTF8);
                        generateFile(roundtripFile, s, UTF8);
                        Assert.assertTrue(roundtripFile.exists());
                    }
                    else
                    {
                        fail("\n" + tmpFile + "\n and \n" + answerFile
                                + " not equal");
                    }
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }
        }
    }

    /**
     * JsFilterRegex in JS Filter is "alert()"
     */
    @Test
    public void testExtractor2()
    {
        ArrayList<FileSet> fileSet = (ArrayList<FileSet>) fileSets
                .get("testExtractor2");
        if (fileSet != null && fileSet.size() > 0)
        {
            Iterator<FileSet> it = fileSet.iterator();
            try
            {
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    sourceFile = fs.getSourceFile();
                    answerFile = fs.getAnswerFile();
                    roundtripFile = fs.getRoundtripFile();
                    // Get file content
                    String gxml = getFileContent(sourceFile, extractor, UTF8,
                            "alert()");
                    String answerContent = getTranslatableTextContent(gxml);
                    File tmpFile = new File(answerFile.getParentFile()
                            .getAbsoluteFile()
                            + File.separator + answerFile.getName() + ".tmp");
                    // Generate files for compare
                    generateFile(tmpFile, answerContent, UTF8);
                    if (fileCompareNoCareEndLining(tmpFile, answerFile))
                    {
                        tmpFile.delete();
                        // generate target file
                        byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                        String s = new String(mergeResult, UTF8);
                        generateFile(roundtripFile, s, UTF8);
                        Assert.assertTrue(roundtripFile.exists());
                    }
                    else
                    {
                        fail("\n" + tmpFile + "\n and \n" + answerFile
                                + " not equal");
                    }
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }
        }
    }

    /**
     * JsFilterRegex in JS Filter is "getElementById()"
     */
    @Test
    public void testExtractor3()
    {
        ArrayList<FileSet> fileSet = (ArrayList<FileSet>) fileSets
                .get("testExtractor3");
        if (fileSet != null && fileSet.size() > 0)
        {
            Iterator<FileSet> it = fileSet.iterator();
            try
            {
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    sourceFile = fs.getSourceFile();
                    answerFile = fs.getAnswerFile();
                    roundtripFile = fs.getRoundtripFile();
                    // Get file content
                    String gxml = getFileContent(sourceFile, extractor, UTF8,
                            "getElementById()");
                    String answerContent = getTranslatableTextContent(gxml);
                    File tmpFile = new File(answerFile.getParentFile()
                            .getAbsoluteFile()
                            + File.separator + answerFile.getName() + ".tmp");
                    // Generate files for compare
                    generateFile(tmpFile, answerContent, UTF8);
                    if (fileCompareNoCareEndLining(tmpFile, answerFile))
                    {
                        tmpFile.delete();
                        // generate target file
                        byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                        String s = new String(mergeResult, UTF8);
                        generateFile(roundtripFile, s, UTF8);
                        Assert.assertTrue(roundtripFile.exists());
                    }
                    else
                    {
                        fail("\n" + tmpFile + "\n and \n" + answerFile
                                + " not equal");
                    }
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }
        }
    }

    public AbstractExtractor initExtractor()
    {
        extractor = new Extractor();
        return extractor;
    }

    @Override
    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }

    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding, String JsFilterRegex) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        extractor.loadRules();
        Method method;
        try
        {
            method = extractor.getClass().getMethod("setJsFilterRegex",
                    String.class);
            method.invoke(extractor, JsFilterRegex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        extractor.extract();
        String gxml = DiplomatWriter.WriteXML(output);
        DiplomatSegmenter seg = new DiplomatSegmenter();
        gxml = seg.segment(gxml).replace("&apos;", "'");
        return gxml;
    }


    @Override
    public Output doExtract(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }

    @Override
    public HashMap<String, ArrayList<FileSet>> initFileSet()
    {
        String[][] fileSet =
        {
        { "testExtractor1", "test.js", "test1.txt", "test1.js" },
        { "testExtractor1", "test_a.js", "test_a1.txt", "test_a1.js" },
        { "testExtractor2", "test.js", "test2.txt", "test2.js" },
        { "testExtractor3", "test.js", "test3.txt", "test3.js" },
        { "testExtractor3", "test_a.js", "test_a3.txt", "test_a3.js" } };

        return formFileSets(fileSet, ExtractorTest.class);
    }

}