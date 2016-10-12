package com.globalsight.ling.docproc.extractor.rc;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatPostProcessor;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;

public class ExtractorTest extends BaseExtractorTestClass
{
    private static final String UTF8 = "UTF-8";
    private static final String extension = "rc";
    
    private Extractor extractor;
    private HashMap fileSets = new HashMap(); 
    
    @Before
    public void setUp() throws Exception
    {
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
     * Test RC file extractor and merger.
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
                        // extract source file
                        Output output = doExtract(sourceFile, extractor, UTF8);
                        // get translatable text content
                        String resultContent = getTranslatableTextContent(output);
                        // generate result file for compare purpose
                        File tmpResultFile = new File(answerFile
                                .getParentFile().getAbsolutePath()
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
                            generateFile(roundtripFile, s, UTF8);
                            Assert.assertTrue(roundtripFile.exists());
                            fileCompareNoCareEndLining(roundtripFile, sourceFile);
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n" + answerFile
                                    + " not equal");
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

    /**
     * An empty method from parent class.
     */
    @Override
    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }
    
    /**
     * Do extract, segmentation, handle "x","nbsp" etc.
     * @throws Exception 
     */
    @Override
    public Output doExtract(File file, AbstractExtractor extractor,
            String encoding) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        // Read from URL
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        // RC loadRules() does nothing.
        extractor.loadRules();

        // Extract this file
        extractor.extract();

        //(Not required)
        DiplomatCtrlCharConverter dc = new DiplomatCtrlCharConverter();
        dc.convertChars(output);
        output = dc.getOutput();
        
        // Segment the output(Required)
        DiplomatSegmenter seg = new DiplomatSegmenter();
        seg.segment(output);
        
        // Word count recalculate.(Not required)
//        DiplomatWordCounter wc = new DiplomatWordCounter();
//        wc.setLocalizableWordcount(0);
//        wc.countDiplomatDocument(output);
//        output = wc.getOutput();

        // CvdL new step to wrap nbsp and fix the "x" attributes.(Not required)
        DiplomatPostProcessor pp = new DiplomatPostProcessor();
        pp.setFormatName(extension);
        pp.postProcess(output);
        output = pp.getOutput();

        return output;
    }

    /**
     * Initiate the RC extractor.
     */
    @Override
    public AbstractExtractor initExtractor()
    {
        extractor = new Extractor();
        return extractor;
    }
    
    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet =
        {
            {"testExtractor", "5AxisRes.rc", "5AxisRes.txt", "5AxisRes.rc"},
            {"testExtractor", "WSPS.RC", "WSPS.txt", "WSPS.RC"},
            {"testExtractor", "MillOperRes.rc", "MillOperRes.txt", "MillOperRes.rc"}
        };
        
        return formFileSets(fileSet, ExtractorTest.class);
    }
}
