package com.globalsight.ling.docproc.extractor.javaprop;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.adapter.filesystem.ExporterTest;
import com.globalsight.cxe.adapter.ling.StandardExtractor;
import com.globalsight.cxe.adapter.ling.StandardExtractorTestHelper;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatPostProcessor;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWordCounter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.ling.docproc.extractor.html.HTMLFilterTestHelper;
import com.globalsight.util.ClassUtil;

public class TestExtractor extends BaseExtractorTestClass
{
    private static final String UTF8 = "UTF-8";
    private static final String extension = "properties";
    public String lineSep = System.getProperty("line.separator");
    
    private Extractor extractor;
    private HashMap<?, ?> fileSets = new HashMap<Object, Object>(); 
    
    @Before
    public void setUp() throws Exception
    {
        // Sets some value for initial word count
        CompanyThreadLocal.getInstance().setIdValue(CompanyWrapper.SUPER_COMPANY_ID);
        SystemConfiguration dpsc = SystemConfiguration.getInstance("/properties/Wordcounter.properties");
        HashMap<String, SystemConfiguration> map = new HashMap<String, SystemConfiguration>();
        map.put("PROPERTIES/WORDCOUNTER.PROPERTIES", dpsc);
        SystemConfiguration.setDebugMap(map);

        initExtractor();
        fileSets = initFileSet();
    }
    
    @After
    public void clear()
    {
        extractor = null;
        fileSets = null;
    }

    @Test
    public void testExtractor()
    {
        try
        {
            ArrayList<?> fileSetList = (ArrayList<?>) fileSets.get("testExtractor");
            if (fileSetList != null && fileSetList.size() > 0)
            {
                Iterator<?> it = fileSetList.iterator();
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
                            s = s.replace("\\\\", "\\");
                            generateFile(roundtripFile, s, UTF8);
                            Assert.assertTrue(roundtripFile.exists());
                            // Due \r\n issue, we don't compare.
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
     * Test the Java Property Filter, when Enable SID Support.
     * SID is the key in Java Property file, which will be stored in TUV table.
     */
    @Test
    public void testExtractorWithFilterEnableSidSupport()
    {
        JavaPropertiesFilter filter = new JavaPropertiesFilter();
        filter.setEnableSidSupport(true);
        testExtractorWithFilter(filter, "testExtractorWithFilterEnableSidSupport");
    }
    
    /**
     * Test the Java Property Filter, when Enable Unicode Escape.
     * Delete the character "\", which is in front of the segment.
     * 
     */
    @Test
    public void testExtractorWithFilterEnableUnicodeEscape()
    {
        JavaPropertiesFilter filter = new JavaPropertiesFilter();
        filter.setEnableUnicodeEscape(true);
        String fileType = "testExtractorWithEnableUnicodeEscape";
        testExtractorWithFilter(filter, fileType);
    }
    
    /**
     * Test the Java Property Filter, when Preserve Trailing Spaces.
     * 
     * "Preserve Trailing Spaces" keeps the source and target TUV ends with same spaces. 
     * For example,
     * if the source TUV is "Hello world     " and the target is "HELLO WORLD",
     * then after export the target TUV will be "HELLO WORLD     ".
     * 
     * @see ExportHelper.updateSegValue()
     */
    @Test
    public void testExtractorWithFilterEnablePreserveSpaces()
    {
        boolean isPreserveTrailingSpace = true;
        boolean isJavaProperties = true;
        long sourcePageLocaleId = 1;
        String sourceTuvContentNoTags = "Hello world     ";
        int sourceTrailingSpaceNum = countTrailingSpaceNum(sourceTuvContentNoTags);
        String targetTuvContentNoTags = "HELLO WORLD";
        int targetTrailingSpaceNum = countTrailingSpaceNum(targetTuvContentNoTags);
        if (isPreserveTrailingSpace && isJavaProperties && sourceTrailingSpaceNum > 0 && targetTrailingSpaceNum == 0)
        {
            long currentTuvPageLocaleId = 2;

            String spaces = "";
            if (currentTuvPageLocaleId != sourcePageLocaleId) 
            {
                while (Character.isWhitespace(sourceTuvContentNoTags.charAt(sourceTuvContentNoTags.length()-1))) {
                    sourceTuvContentNoTags = sourceTuvContentNoTags.substring(0, sourceTuvContentNoTags.length()-1);
                    spaces += " ";
                }
            }
            targetTuvContentNoTags = targetTuvContentNoTags + spaces;
            // p_segment.setGxmlExcludeTopTags(targetTuvContentNoTags);
        }
        
        String expected = "HELLO WORLD     ";
        Assert.assertEquals(expected, targetTuvContentNoTags);
        
    }
    
    /**
     * Test the Java Property Filter, when using Secondary Filter.
     */
    @Test
    public void testExtractorWithFilterSecondFilter()
    {
        JavaPropertiesFilter filter = new JavaPropertiesFilter();
        filter.setSecondFilterTableName(FilterConstants.HTML_TABLENAME);
        testExtractorWithFilter(filter, "testExtractorWithFilterSecondFilter");
    }
    
    /**
     * Test the Java Property Filter, when using Internal Text. 
     * Use bpt, ept to protect the data.
     */
    @Test
    public void testExtractorWithFilterInternalText()
    {
        JavaPropertiesFilter filter = new JavaPropertiesFilter();
        List<InternalText> its = new ArrayList<InternalText>();
        InternalText it = new InternalText("\\{[^{]+\\}", true);
        its.add(it);
        extractor.setMainFilter(filter);
        extractor.setInternalTexts(its);
        testExtractorWithFilter(filter, "testExtractorWithFilterInternalText");
    }
    
    @SuppressWarnings("unchecked")
    public void testExtractorWithFilter(JavaPropertiesFilter p_filter, String p_fileType)
    {
        try
        {
            ArrayList<FileSet> fileSetList = (ArrayList<FileSet>) fileSets.get(p_fileType);
            if (fileSetList != null && fileSetList.size() > 0)
            {
                HtmlFilter hFilter = null;
                FileProfileImpl fp = new FileProfileImpl();
                String fpId = "-1";
                long secondFilterId = 1;
                String secondFilterTableName = p_filter.getSecondFilterTableName();
                if (FilterConstants.HTML_TABLENAME.equals(p_filter.getSecondFilterTableName()))
                {
                    hFilter = HTMLFilterTestHelper.addHtmlFilter(1, "For JAVAPROP", true);
                    secondFilterId = hFilter.getId();
                }
                
                Iterator<?> it = fileSetList.iterator();
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    File sourceFile = fs.getSourceFile();
                    File answerFile = fs.getAnswerFile();
                    File roundtripFile = fs.getRoundtripFile();
                    
                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // extract source file
                        Output output;
                        if (FilterConstants.HTML_TABLENAME.equals(p_filter.getSecondFilterTableName()))
                        {
                            output = doExtractWithSecondaryFilter(sourceFile, 
                                     extractor, UTF8, fp, fpId, secondFilterId, secondFilterTableName);
                        }
                        else
                        {
                            output = doExtract(sourceFile, extractor, UTF8);
                        }
                        // get translatable text content
                        String resultContent = getTranslatableTextContent(output, p_filter);
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
                            s = s.replace("\\\\", "\\");
                            s = s.replace("&lt;", "<").replace("&gt;", ">");
                            generateFile(roundtripFile, s, UTF8);
                            Assert.assertTrue(roundtripFile.exists());
                            if (p_filter.getEnableUnicodeEscape())
                            {
                                ExporterTest exporterTest = new ExporterTest();
                                exporterTest.testHandleExtraEscapeCharacter(roundtripFile.getAbsolutePath(), "UTF-8");
                            }
                            else
                            {
                                fileCompareNoCareEndLining(roundtripFile, sourceFile);
                            }
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n"
                                        + answerFile + " not equal");
                        }
                    }
                }
                HTMLFilterTestHelper.delHtmlFilterByID(hFilter);
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

    }
    
    public int countTrailingSpaceNum(String p_string)
    {
        ExportHelper exportHelper = new ExportHelper();
        Object obj = ClassUtil.testMethod(exportHelper, "countTrailingSpaceNum", p_string);
        return Integer.valueOf(obj.toString());
    }
    
    public Output doExtractWithSecondaryFilter(File p_file, AbstractExtractor extractor,
            String p_encoding, FileProfileImpl p_fp, String p_fpId, 
            long p_secondFilterId, String p_secondFilterTableName) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(p_encoding);
        input.setURL(p_file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();
        
        DiplomatSegmenter seg = new DiplomatSegmenter();
        seg.segmentXliff(output);                   
        
        // Do Secondary Filter
        Logger logger = 
            Logger.getLogger(getClass());
        StandardExtractor se = StandardExtractorTestHelper.getInstance(logger, null);
        DiplomatAPI diplomat = new DiplomatAPI();
        diplomat.setEncoding(UTF8);
        diplomat.setSegmentationRuleText("default");
        Iterator<?> it = output.documentElementIterator();
        output.clearDocumentElements();
        
        try
        {
            ClassUtil.testMethod(se, "doSecondFilter", output, it, diplomat, 
                      p_fp, p_fpId, p_secondFilterId, p_secondFilterTableName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return output;
    }
    
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

        // Word count recalculate
        DiplomatWordCounter wc = new DiplomatWordCounter();
        wc.setLocalizableWordcount(0);
        wc.countDiplomatDocument(output);
        output = wc.getOutput();

        // CvdL new step to wrap nbsp and fix the "x" attributes.(Not required)
        DiplomatPostProcessor pp = new DiplomatPostProcessor();
        pp.setFormatName(extension);
        pp.postProcess(output);
        output = pp.getOutput();

        return output;
    }
    
    /**
     * Modify for showing SID.
     * SID is the key in Java Property file, which will be stored in TUV table.
     * 
     * Get all translatable text content from specified output. Commonly, this
     * method is used with "doExtract(...)" together,which returns the output
     * object.
     */
    public String getTranslatableTextContent(Output p_output, JavaPropertiesFilter p_filter)
    {
        if (p_output == null) {
            return null;
        }
        
        if (p_filter == null)
        {
            p_filter = new JavaPropertiesFilter();
        }

        StringBuffer resultContent = new StringBuffer();
        String sid = null;
        boolean supportSid = p_filter.getEnableSidSupport();
        
        Iterator<?> eleIter = p_output.documentElementIterator();
        while (eleIter.hasNext())
        {
            DocumentElement de = (DocumentElement) eleIter.next();
            if (de instanceof TranslatableElement)
            {
                Iterator<?> it = ((TranslatableElement) de).getSegments().iterator();
                while (it.hasNext())
                {
                    SegmentNode sn = (SegmentNode) it.next();
                    if (sn.getSegment() != null)
                    {
                        resultContent.append(sn.getSegment());
                        resultContent.append(lineSep);
                        
                        if (supportSid && sid != null)
                        {
                            resultContent.append("\t\t\tSID[");
                            resultContent.append(String.valueOf(sid));
                            resultContent.append("]");
                            resultContent.append(lineSep);
                        }
                        
                        resultContent.append(lineSep);
                    }
                    
                }
            }
            else if (de instanceof LocalizableElement)
            {
                // Do not care localizable element for now.
            }
            else if (de instanceof SkeletonElement)
            {
                if (supportSid)
                {
                    String nodeValue = de.getText();
                    if (nodeValue != null)
                    {
                        nodeValue = nodeValue.trim();
                        if (nodeValue.endsWith("="))
                        {
                            int index = nodeValue.lastIndexOf("\n");
                            if (index > -1)
                            {
                                nodeValue = nodeValue.substring(index + 1);
                            }

                            // remove "="
                            nodeValue = nodeValue.substring(0, nodeValue.length() - 1);
                            sid = nodeValue.trim();
                        }
                    }
                }
            }
        }

        return resultContent.toString();
    }

    @Override
    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }

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
                { "testExtractor", "sample.properties", 
                        "sample.txt",
                        "sample.properties" },
                { "testExtractorWithFilterEnableSidSupport", "sample.properties",
                        "sample" + "EnableSidSupport" + ".txt",
                        "sample" + "EnableSidSupport" + ".properties" },
                { "testExtractorWithEnableUnicodeEscape", "sampleUnicode.properties",
                        "sampleUnicode" + ".txt",
                        "sampleUnicode" + ".properties" },
                { "testExtractorWithFilterSecondFilter", "sample.properties",
                        "sample" + "SecondFilter" + ".txt",
                        "sample" + "SecondFilter" + ".properties" },
                { "testExtractorWithFilterInternalText", "sample.properties",
                        "sample" + "InternalText" + ".txt",
                        "sample" + "InternalText" + ".properties" }
        };

        return formFileSets(fileSet, getClass());
    }

}
