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

import com.globalsight.cxe.adapter.msoffice.OfficeXmlConverter;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.util.edit.SegmentUtil;

public class OfficeXmlExtractorTest extends BaseExtractorTestClass
{
    // / Variables
    private static final String UTF8 = "UTF-8";
    private OfficeXmlExtractor extractor;
    private HashMap fileSets = new HashMap();
    private String excelRule = null;
    private String wordRule = null;
    private String pptRule = null;
    
    private static String excelFile = null;
    private static String wordFile = null;
    private static String pptFile = null;
    
    private static String excelDir = null;
    private static String wordDir = null;
    private static String pptDir = null;
    
    private static File pptSlide1 = null;
    private static File excelSharedStrings = null;
    private static File wordDocument;
    
    @BeforeClass
    public static void staticInit() throws Exception
    {
        System.out.println("/properties/MSXlsxXmlRule.properties : ");
        System.out.println(OfficeXmlExtractorTest.class.getResource("/properties/MSXlsxXmlRule.properties"));
        
        excelFile = FileUtil.getResourcePath(OfficeXmlExtractorTest.class, "source/xlsx_sample_document.xlsx");
        wordFile = FileUtil.getResourcePath(OfficeXmlExtractorTest.class, "source/docx_sample_document.docx");
        pptFile = FileUtil.getResourcePath(OfficeXmlExtractorTest.class, "source/pptx_sample_document.pptx");
        
        wordDir = getUnzipDir(wordFile);
        excelDir = getUnzipDir(excelFile);
        pptDir = getUnzipDir(pptFile);
        
        // do conversion here
        doConversion(wordFile, wordDir);
        doConversion(pptFile, pptDir);
        doConversion(excelFile, excelDir);
        
        pptSlide1 = new File(pptDir, "ppt/slides/slide1.xml");
        excelSharedStrings = new File(excelDir, "xl/sharedStrings.xml");
        wordDocument = new File(wordDir, "word/document.xml");
    }

    // / Public Methods
    @Before
    public void init() throws Exception
    {
        initExtractor();
        fileSets = initFileSet();
        
        excelRule = FileUtil.getResourcePath(OfficeXmlExtractorTest.class, "/properties/MSXlsxXmlRule.properties");
        wordRule = FileUtil.getResourcePath(OfficeXmlExtractorTest.class, "/properties/MSDocxXmlRule.properties");
        pptRule = FileUtil.getResourcePath(OfficeXmlExtractorTest.class, "/properties/MSPptxXmlRule.properties");
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
        org.junit.runner.JUnitCore.runClasses(OfficeXmlExtractorTest.class);
    }
    
    /**
     * Test PPT conversion
     * @throws Exception 
     */
    @Test
    public void testPptConversion() throws Exception
    {
        System.out.println(pptFile);
        System.out.println(pptDir);
        
        Assert.assertTrue(pptSlide1.exists());
    }
    
    /**
     * Test excel conversion
     * @throws Exception 
     */
    @Test
    public void testExcelConversion() throws Exception
    {
        System.out.println(excelFile);
        System.out.println(excelDir);
        
        Assert.assertTrue(excelSharedStrings.exists());
    }
    
    /**
     * Test word conversion
     * @throws Exception 
     */
    @Test
    public void testWordConversion() throws Exception
    {
        System.out.println(wordFile);
        System.out.println(wordDir);
        
        Assert.assertTrue(wordDocument.exists());
    }
    
    /**
     * Test WORD 2010 filter options
     */
    @Test
    public void testWord2010FilterOptions()
    {
        OfficeXmlHelper helper = new OfficeXmlHelper();
        helper.setParametersForTesting(OfficeXmlHelper.OFFICE_DOCX, true, false, false, false, false, false);
        String[] files = helper.getLocalizeXmlFiles(wordDir);
        System.out.println(files.length);
        System.out.println(files[files.length - 1]);
        
        Assert.assertEquals("File count is not right.", 3, files.length);
        Assert.assertTrue("The last file is not right.", files[files.length -1].endsWith("header1.xml"));
        
        helper.setParametersForTesting(OfficeXmlHelper.OFFICE_DOCX, false, false, false, false, false, false);
        files = helper.getLocalizeXmlFiles(wordDir);
        System.out.println(files.length);
        System.out.println(files[files.length - 1]);
        
        Assert.assertEquals("File count is not right.", 1, files.length);
        Assert.assertTrue("The last file is not right.", files[files.length -1].endsWith("document.xml"));
    }
    
    /**
     * Test PPT 2010 filter options
     */
    @Test
    public void testPPT2010FilterOptions()
    {
        OfficeXmlHelper helper = new OfficeXmlHelper();
        helper.setParametersForTesting(OfficeXmlHelper.OFFICE_PPTX, false, true, true, true, true, true);
        String[] files = helper.getLocalizeXmlFiles(pptDir);
        System.out.println(files.length);
        System.out.println(files[files.length - 1]);
        
        Assert.assertEquals("File count is not right.", 20, files.length);
        Assert.assertTrue("The last file is not right.", files[files.length -1].endsWith("handoutMaster1.xml"));
        
        helper.setParametersForTesting(OfficeXmlHelper.OFFICE_PPTX, false, false, false, false, false, false);
        files = helper.getLocalizeXmlFiles(pptDir);
        System.out.println(files.length);
        System.out.println(files[files.length - 1]);
        
        Assert.assertEquals("File count is not right.", 6, files.length);
        Assert.assertTrue("The last file is not right.", files[files.length -1].endsWith("slide6.xml"));
    }

    // //////////////////////////////////////////////////////////////////////
    // GBS-1930 : Base Filter Unittest: Office 2010
    // GBS-1931 : Filter Configuration Unittests: Office 2010
    // ////////////////////////////////////////////////////////////////////
    /**
     * Test office 2010 file extractor and merger.
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
    
    /**
     * Test extract word 2010 header and footer.
     */
    @Test
    public void testExtractWordHeader()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets.get("testExtractWordHeader");
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
                            Assert.assertTrue(s.contains("sample document for testing purpose"));
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
    
    /**
     * Test extract ppt 2010 files.
     */
    @Test
    public void testExtractPPTFiles()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets.get("testExtractPPTFiles");
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
                            Assert.assertTrue(s.contains("GlobalSight"));
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
        extractor = new OfficeXmlExtractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet = {
                { "testExtractor", "docx_sample_document.docx.0/word/document.xml",
                        "docx_sample_document.documentxml.txt",
                        "docx_sample_document.documentxml.xml" },
                { "testExtractor", "xlsx_sample_document.xlsx.1/xl/sharedStrings.xml",
                        "xlsx_sample_document.sharedStringsxml.txt",
                        "xlsx_sample_document.sharedStringsxml.xml" },
                { "testExtractor", "pptx_sample_document.pptx.2/ppt/slides/slide1.xml",
                        "pptx_sample_document.slide1xml.txt", "pptx_sample_document.slide1xml.xml" },
                { "testExtractWordHeader", "docx_sample_document.docx.0/word/header1.xml",
                        "docx_sample_document.header1.xml.txt",
                        "docx_sample_document.header1.xml.xml" },
                { "testExtractWordHeader", "docx_sample_document.docx.0/word/footer1.xml",
                        "docx_sample_document.footer1.xml.txt",
                        "docx_sample_document.footer1.xml.xml" },
                { "testExtractPPTFiles", "pptx_sample_document.pptx.2/ppt/notesSlides/notesSlide1.xml",
                        "pptx_sample_document.notesSlide1.xml.txt",
                        "pptx_sample_document.notesSlide1.xml.xml" },
                { "testExtractPPTFiles", "pptx_sample_document.pptx.2/ppt/slideMasters/slideMaster1.xml",
                        "pptx_sample_document.slideMaster1.xml.txt",
                        "pptx_sample_document.slideMaster1.xml.xml" },
                { "testExtractPPTFiles", "pptx_sample_document.pptx.2/ppt/slideLayouts/slideLayout1.xml",
                        "pptx_sample_document.slideLayout1.xml.txt",
                        "pptx_sample_document.slideLayout1.xml.xml" },
                { "testExtractPPTFiles", "pptx_sample_document.pptx.2/ppt/notesMasters/notesMaster1.xml",
                        "pptx_sample_document.notesMaster1.xml.txt",
                        "pptx_sample_document.notesMaster1.xml.xml" },
                { "testExtractPPTFiles", "pptx_sample_document.pptx.2/ppt/handoutMasters/handoutMaster1.xml",
                        "pptx_sample_document.handoutMaster1.xml.txt",
                        "pptx_sample_document.handoutMaster1.xml.xml" } };

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
        // set office-xml as input type
        input.setType(30);
        Output output = new Output();

        OfficeXmlExtractor extractor = new OfficeXmlExtractor();
        extractor.init(input, output);
        extractor.setFormat();
        extractor.loadRules();
        extractor.extract();

        return output;
    }

    private Output doSegment(Output output)
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
        if (xmlFileName.contains(".docx"))
        {
            return wordRule;
        }
        
        if (xmlFileName.contains(".xlsx"))
        {
            return excelRule;
        }
        
        if (xmlFileName.contains(".pptx"))
        {
            return pptRule;
        }
        
        return "";
    }
    
    private static String getUnzipDir(String p_filepath)
    {
        int type = p_filepath.endsWith("docx") ? 0 : ( p_filepath.endsWith("xlsx") ? 1 : 2 );
        String dirName = p_filepath + "." + type;
        return dirName;
    }
    
    private static void doConversion(String officeFile, String dirName) throws Exception
    {
        OfficeXmlConverter oxc = new OfficeXmlConverter();
        oxc.convertOfficeToXml(officeFile, dirName);
    }
}

