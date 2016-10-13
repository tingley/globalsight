package com.globalsight.ling.docproc.extractor.idml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConfigParser;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor;
import com.globalsight.util.edit.SegmentUtil;

public class IdmlExtractorTest extends BaseExtractorTestClass
{
    // / Variables
    private static final String UTF8 = "UTF-8";
    private IdmlExtractor extractor;
    private HashMap fileSets = new HashMap();
    private String idmlRuleFile = null;

    // / Public Methods
    @Before
    public void init()
    {
        initExtractor();
        fileSets = initFileSet();
        idmlRuleFile = FileUtil.getResourcePath(IdmlExtractorTest.class, "files/idmlrule.properties");
    }

    @After
    public void clear()
    {
        extractor = null;
        fileSets = new HashMap();
    }

    /**
     * Main method, copy necessary properties to Project_Root\bin\properties for
     * run this class in eclipse
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(IdmlExtractorTest.class);
    }

    // / Private Methods
    private EFInputData createInput(String fileUrl, String ruleFile, String encoding)
            throws FileNotFoundException, IOException
    {
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        java.util.Locale locale = new java.util.Locale("en", "US");
        input.setLocale(locale);
        input.setURL("file:" + fileUrl);
        input.setRules(FileUtil.readRuleFile(ruleFile, UTF8));

        return input;
    }

    private Output doExtract(String xmlFile, String ruleFile)
            throws FileNotFoundException, IOException
    {
        EFInputData input = createInput(xmlFile, ruleFile, UTF8);
        // set idml as input type
        input.setType(33);
        Output output = new Output();

        IdmlExtractor extractor = new IdmlExtractor();
        extractor.init(input, output);
        extractor.setFormat();
        extractor.loadRules();
        extractor.extract();

        return output;
    }

    private Output doSegment(Output output) throws Exception
    {
        // Convert C0 control codes to PUA characters to avoid XML
        // parser error
        DiplomatCtrlCharConverter dc = new DiplomatCtrlCharConverter();
        dc.convertChars(output);
        Output newOut = dc.getOutput();

        // do segment
        DiplomatSegmenter ds = new DiplomatSegmenter();
        ds.setPreserveWhitespace(true);
        ds.segment(newOut);

        return ds.getOutput();
    }

    // //////////////////////////////////////////////////////////////////////
    // GBS-1918 Base Filter Unittest: IDML
    // GBS-1919 Filter Configuration Unittests: IDML
    // ////////////////////////////////////////////////////////////////////
    /**
     * Test IDML file extractor and merger.
     */
    @Test
    public void testExtractor()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets.get("testExtractor");
            if (fileSet != null && fileSet.size() > 0)
            {
                Iterator it = fileSet.iterator();
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    File sourceFile = fs.getSourceFile();
                    File answerFile = fs.getAnswerFile();
                    File roundtripFile = fs.getRoundtripFile();

                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // extract source file with default xml filter
                        Output output = doExtract(sourceFile.getAbsolutePath(), idmlRuleFile);
                        // do segment
                        output = doSegment(output);
                        // get translatable text content
                        String resultContent = getTranslatableTextContent(output);
                        // generate result file for compare purpose
                        File tmpResultFile = new File(answerFile.getParentFile().getAbsolutePath()
                                + File.separator + sourceFile.getName() + ".tmp");
                        generateFile(tmpResultFile, resultContent, UTF8);

                        // compare result file to answer file
                        if (fileCompareNoCareEndLining(tmpResultFile, answerFile))
                        {
                            tmpResultFile.delete();
                            
                            // generate target file
                            String gxml = DiplomatWriter.WriteXML(output);
                            byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                            String s = new String(mergeResult, UTF8);
                            
                            // restore invalid unicode char for merged result
                            s = SegmentUtil.restoreInvalidUnicodeChar(s);
                            generateFile(roundtripFile, s, UTF8);
                            Assert.assertTrue(roundtripFile.exists());
                            Assert.assertTrue(s.contains("&#x100088;"));
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n" + answerFile + " not equal");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

    }

    @Override
    public AbstractExtractor initExtractor()
    {
        extractor = new IdmlExtractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet = {
                { "testExtractor", "idml_test_unzipcontent.xml", "idml_test_unzipcontent.txt",
                        "idml_test_unzipcontent.xml" },
                { "testExtractor", "idml_test_withfileinfo_unzipcontent.xml",
                        "idml_test_withfileinfo_unzipcontent.txt",
                        "idml_test_withfileinfo_unzipcontent.xml" } };

        return formFileSets(fileSet, IdmlExtractorTest.class);
    }

    @Override
    public String getFileContent(File file, AbstractExtractor extractor, String encoding)
    {
        return null;
    }

    @Override
    public Output doExtract(File file, AbstractExtractor extractor, String encoding)
    {
        return null;
    }
}
