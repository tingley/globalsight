package com.globalsight.ling.docproc.extractor.openoffice;

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

import com.globalsight.cxe.adapter.openoffice.OpenOfficeConverter;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.ling.docproc.extractor.openoffice.OpenOfficeExtractor;
import com.globalsight.util.edit.SegmentUtil;

public class OpenOfficeExtractorTest extends BaseExtractorTestClass
{
    // / Variables
    private static final String UTF8 = "UTF-8";
    private OpenOfficeExtractor extractor;
    private HashMap fileSets = new HashMap();
    private String odtRule = null;
    private String odsRule = null;
    private String odpRule = null;
    
    private static String odtFile = null;
    private static String odsFile = null;
    private static String odpFile = null;
    
    private static String odtDir = null;
    private static String odsDir = null;
    private static String odpDir = null;
    
    private static File odtContentXml = null;
    private static File odsContentXml = null;
    private static File odpContentXml = null;
    
    @BeforeClass
    public static void staticInit() throws Exception
    {
        System.out.println("/properties/OdtXmlRule.properties : ");
        System.out.println(OpenOfficeExtractorTest.class.getResource("/properties/OdtXmlRule.properties"));
        
        odtFile = FileUtil.getResourcePath(OpenOfficeExtractorTest.class, "source/odt_sample_document.odt");
        odsFile = FileUtil.getResourcePath(OpenOfficeExtractorTest.class, "source/ods_sample_document.ods");
        odpFile = FileUtil.getResourcePath(OpenOfficeExtractorTest.class, "source/odp_sample_document.odp");
        
        odtDir = getUnzipDir(odtFile);
        odsDir = getUnzipDir(odsFile);
        odpDir = getUnzipDir(odpFile);
        
        // do conversion here
        doConversion(odtFile, odtDir);
        doConversion(odsFile, odsDir);
        doConversion(odpFile, odpDir);
        
        odtContentXml = new File(odtDir, "content.xml");
        odsContentXml = new File(odsDir, "content.xml");
        odpContentXml = new File(odpDir, "content.xml");
    }

    // / Public Methods
    @Before
    public void init() throws Exception
    {
        initExtractor();
        fileSets = initFileSet();
        
        odtRule = FileUtil.getResourcePath(OpenOfficeExtractorTest.class, "/properties/OdtXmlRule.properties");
        odsRule = FileUtil.getResourcePath(OpenOfficeExtractorTest.class, "/properties/OdsXmlRule.properties");
        odpRule = FileUtil.getResourcePath(OpenOfficeExtractorTest.class, "/properties/OdpXmlRule.properties");
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
        org.junit.runner.JUnitCore.runClasses(OpenOfficeExtractorTest.class);
    }
    
    /**
     * Test open document presentation conversion
     * @throws Exception 
     */
    @Test
    public void testPptConversion() throws Exception
    {
        System.out.println(odpFile);
        System.out.println(odpDir);
        
        Assert.assertTrue(odpContentXml.exists());
    }
    
    /**
     * Test open document spreadsheets conversion
     * @throws Exception 
     */
    @Test
    public void testOdsConversion() throws Exception
    {
        System.out.println(odsFile);
        System.out.println(odsDir);
        
        Assert.assertTrue(odsContentXml.exists());
    }
    
    /**
     * Test open document text conversion
     * @throws Exception 
     */
    @Test
    public void testOdtConversion() throws Exception
    {
        System.out.println(odtFile);
        System.out.println(odtDir);
        
        Assert.assertTrue(odtContentXml.exists());
    }

    // //////////////////////////////////////////////////////////////////////
    // GBS-1932 : Base Filter Unittest: Open Office
    // GBS-1933 : Filter Configuration Unittests: Open Office
    // ////////////////////////////////////////////////////////////////////
    /**
     * Test open office file extractor and merger.
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
                        Output output = doExtract(sourceFile.getAbsolutePath(), getRuleFile(sourceFile.getPath()));
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
                            Assert.assertTrue(s.contains("Sample Document"));
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
        extractor = new OpenOfficeExtractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet = {
                { "testExtractor", "odt_sample_document.odt.0/content.xml",
                        "odt_sample_document.contentxml.txt",
                        "odt_sample_document.contentxml.xml" },
                { "testExtractor", "ods_sample_document.ods.1/content.xml",
                        "ods_sample_document.contentxml.txt",
                        "ods_sample_document.contentxml.xml" },
                { "testExtractor", "odp_sample_document.odp.2/content.xml",
                        "odp_sample_document.contentxml.txt",
                        "odp_sample_document.contentxml.xml" }};

        return formFileSets(fileSet, OpenOfficeExtractorTest.class);
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
        // set open office as input type
        input.setType(28);
        Output output = new Output();

        OpenOfficeExtractor extractor = new OpenOfficeExtractor();
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
    
    private String getRuleFile(String xmlFileName)
    {
        if (xmlFileName.contains(".odt."))
        {
            return odtRule;
        }
        
        if (xmlFileName.contains(".ods."))
        {
            return odsRule;
        }
        
        if (xmlFileName.contains(".odp."))
        {
            return odpRule;
        }
        
        return "";
    }
    
    private static String getUnzipDir(String p_filepath)
    {
        int type = p_filepath.endsWith("odt") ? 0 : ( p_filepath.endsWith("ods") ? 1 : 2 );
        String dirName = p_filepath + "." + type;
        return dirName;
    }
    
    private static void doConversion(String officeFile, String dirName) throws Exception
    {
        OpenOfficeConverter oxc = new OpenOfficeConverter();
        oxc.convertOdToXml(officeFile, dirName);
    }
}
