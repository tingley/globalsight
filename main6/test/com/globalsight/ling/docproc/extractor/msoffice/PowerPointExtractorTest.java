package com.globalsight.ling.docproc.extractor.msoffice;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.util.system.MockEnvoySystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.util.edit.SegmentUtil;

public class PowerPointExtractorTest extends BaseExtractorTestClass
{
    // / Variables
    private static final String UTF8 = "UTF-8";
    private PowerPointExtractor extractor;
    private HashMap fileSets = new HashMap();
    
    @BeforeClass
    public static void staticInit() throws Exception
    {
    }

    // / Public Methods
    @Before
    public void init() throws Exception
    {
        SystemConfiguration.setDebugInstance(new MockEnvoySystemConfiguration(
                new HashMap<String, String>()
                {
                    {
                        put("profile.level.company", "nofiles");
                    }
                }));
        
        initExtractor();
        fileSets = initFileSet();
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
        org.junit.runner.JUnitCore.runClasses(PowerPointExtractorTest.class);
    }

    // //////////////////////////////////////////////////////////////////////
    // GBS-1948 : Base Filter Unittest: Office 2003 and 2007 
    // ////////////////////////////////////////////////////////////////////
    /**
     * Test office 2003/2007 file extractor and merger.
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
                        // extract source file with default filter
                        Output output = doExtract(sourceFile.getAbsolutePath());
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
                            System.out.println(s);
                            Assert.assertTrue(s.contains("Sample Document"));
                            // for GBS-2272 lastCR
                            Assert.assertTrue(s.contains("mso-special-format:lastCR;display:none'>&#13;</span></div>"));
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
        extractor = new PowerPointExtractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet = {
                { "testExtractor", "ppt2003_sample_document.files/slide0001.htm",
                        "ppt2003_sample_document.ppt.slide0001.htm.txt",
                        "ppt2003_sample_document.ppt.slide0001.htm" }};

        return formFileSets(fileSet, OfficeXmlExtractorTest.class);
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
    
 // / Private Methods
    private EFInputData createInput(String fileUrl, String encoding)
            throws FileNotFoundException, IOException
    {
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        java.util.Locale locale = new java.util.Locale("en", "US");
        input.setLocale(locale);
        input.setURL("file:" + fileUrl);

        return input;
    }

    private Output doExtract(String xmlFile)
            throws FileNotFoundException, IOException
    {
        EFInputData input = createInput(xmlFile, UTF8);
        // set word as input type
        input.setType(24);
        Output output = new Output();

        PowerPointExtractor extractor = new PowerPointExtractor();
        extractor.init(input, output);
        extractor.setFormat();
        extractor.loadRules();
        extractor.useDefaultRules();
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
 }

