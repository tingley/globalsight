package com.globalsight.ling.docproc.extractor.msoffice;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
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
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.SegmentUtil;

public class WordExtractorTest extends BaseExtractorTestClass
{
    // / Variables
    private static final String UTF8 = "UTF-8";
    private WordExtractor extractor;
    private HashMap fileSets = new HashMap();
    private static List<Long> m_addedDocFilter;
    private static final boolean TOC_TO_BE_TRANSLATED = true;

    @BeforeClass
    public static void staticInit() throws Exception
    {
        m_addedDocFilter = new ArrayList<Long>();
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

    @AfterClass
    public static void staticClear()
    {
        // clean added doc filter
        if (m_addedDocFilter != null && !m_addedDocFilter.isEmpty())
        {
            for (Long fid : m_addedDocFilter)
            {
                try
                {
                    Filter fff = FilterHelper.getFilter(FilterConstants.MSOFFICEDOC_TABLENAME, fid);
                    HibernateUtil.delete(fff);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Main method, copy necessary properties to Project_Root\bin\properties for
     * run this class in eclipse
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(WordExtractorTest.class);
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
                        Output output = doExtract(sourceFile.getAbsolutePath(), (long) -1);
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
     * Related to GBS-2108
     * 
     * Added by Leon
     * 
     */
    @Test
    public void testExtractor02()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets.get("testExtractor02");
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
                        long docFilterId = createDocFilter(false, false, "",
                                "DONOTTRANSLATE_para,tw4winExternal,tw4winInternal",
                                "tw4winExternal",
                                "DONOTTRANSLATE_para,tw4winExternal,tw4winInternal", "", "",
                                (long) -2);
                        // extract source file with default filter
                        Output output = doExtract(sourceFile.getAbsolutePath(), docFilterId);
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
                            Assert.assertTrue(s.contains("Simple Company"));
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n" + answerFile + " not equal");
                        }
                    }
                    else
                    {
                        fail("Missing source file: " + sourceFile);
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
     * Related to GBS-1206
     * 
     */
    @Test
    public void testExtractor03()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets.get("testExtractor03");
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
                        long docFilterId = createDocFilter(false, false,
                                "tw4winExternal,DONOTTRANSLATE_para",
                                "DONOTTRANSLATE_para,tw4winExternal,tw4winInternal",
                                "tw4winExternal",
                                "DONOTTRANSLATE_para,tw4winExternal,tw4winInternal",
                                "tw4winInternal,DONOTTRANSLATE_char",
                                "tw4winInternal,DONOTTRANSLATE_char", (long) -2);
                        // extract source file with default filter
                        Output output = doExtract(sourceFile.getAbsolutePath(), docFilterId);
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
                            Assert.assertTrue(s.contains(">DONOTTRANSLATE_char<"));
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n" + answerFile + " not equal");
                        }
                    }
                    else
                    {
                        fail("Missing source file: " + sourceFile);
                    }
                }
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

    }

    @Test
    public void testTocTranslate()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets.get("TranslateTocExtractor");
            int i = 0;
            if (fileSet != null && fileSet.size() > 0)
            {
                Iterator it = fileSet.iterator();
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    File sourceFile = fs.getSourceFile();
                    File answerFile = fs.getAnswerFile();

                    if (!sourceFile.exists() || !sourceFile.isFile()) {
                        fail("Missing the source file or it is not a file.");
                    }
                    
                    // extract source file with default filter
                    Output output;
                    if (i == 1 || i == 3)
                    {
                        output = doExtract(sourceFile.getAbsolutePath(), -1,
                                TOC_TO_BE_TRANSLATED);
                    }
                    else
                    {
                        output = doExtract(sourceFile.getAbsolutePath(), -1,
                                !TOC_TO_BE_TRANSLATED);
                    }

                    // do segment
                    output = doSegment(output);
                    // get translatable text content
                    String resultContent = getTranslatableTextContent(output);
                    // generate result file for compare purpose
                    File tmpResultFile = new File(answerFile.getParentFile()
                            .getAbsolutePath()
                            + File.separator
                            + sourceFile.getName() + ".tmp");
                    generateFile(tmpResultFile, resultContent, UTF8);

                    // compare result file to answer file
                    if (fileCompareNoCareEndLining(tmpResultFile, answerFile))
                    {
                        tmpResultFile.delete();
                        Assert.assertTrue(true);
                    }
                    else
                    {
                        fail("\n" + tmpResultFile + "\n and \n" + answerFile
                                + " are not equal!");
                    }
                        
                    i++;
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
        extractor = new WordExtractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet = {
                { "testExtractor", "doc2003_sample_document.htm",
                        "doc2003_sample_document.htm.txt", "doc2003_sample_document.htm" },
                { "testExtractor02", "gbs2108.html", "gbs2108.html.txt", "gbs2108.html" },
                { "TranslateTocExtractor", "doc2003_notoc.htm", "doc2003_notoc.htm.txt",
                        "doc2003_notoc.ht" },
                { "TranslateTocExtractor", "doc2003_toc.htm", "doc2003_toc.htm.txt",
                        "doc2003_toc.htm" },
                { "TranslateTocExtractor", "docx_not_toc.htm", "docx_not_toc.htm.txt",
                        "docx_toc.htm" },
                { "TranslateTocExtractor", "docx_toc.htm", "docx_toc.htm.txt", "docx_toc.htm" },
                { "testExtractor03", "InternalStyle_0.html", "InternalStyle_0.html.txt",
                        "InternalStyle_0.html" } };

        return formFileSets(fileSet, WordExtractorTest.class);
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
    private EFInputData createInput(String fileUrl, String encoding) throws FileNotFoundException,
            IOException
    {
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        java.util.Locale locale = new java.util.Locale("en", "US");
        input.setLocale(locale);
        input.setURL("file:" + fileUrl);

        return input;
    }

    private Output doExtract(String xmlFile, long docFilterId) throws FileNotFoundException,
            IOException
    {
        return doExtract(xmlFile, docFilterId, false);
    }

    private Output doExtract(String xmlFile, long docFilterId, boolean isTocTranslated)
            throws FileNotFoundException, IOException
    {
        EFInputData input = createInput(xmlFile, UTF8);
        // set word as input type
        input.setType(23);
        Output output = new Output();

        WordExtractor extractor = new WordExtractor();
        extractor.init(input, output);
        extractor.setFormat();

        if (docFilterId > 0)
        {
            extractor.setMSOfficeDocFilterId(docFilterId);
        }
        
        if (!isTocTranslated)
        {
            MSOfficeDocFilter filter = (MSOfficeDocFilter) extractor.getMainFilter();

            if (filter == null)
            {
                filter = new MSOfficeDocFilter();
            }

            filter.setTocTranslate(isTocTranslated);
            extractor.setMainFilter(filter);
        }

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

    /**
     * Added by leon
     * 
     * This method is used for create doc filter with needed options
     * 
     * Added by Leon
     * 
     * @param headerTranslate
     * @param altTranslate
     * @param selectParaStyles
     * @param allParaStyles
     * @param selectCharStyles
     * @param allCharStyles
     * @param contentPostFilterId
     * @return
     */
    private long createDocFilter(boolean headerTranslate, boolean altTranslate,
            String selectParaStyles, String allParaStyles, String selectCharStyles,
            String allCharStyles, String selectInternalStyle, String allInternalStyle,
            long contentPostFilterId)
    {
        String filterName = "docFilterUnitTest" + Math.random();
        MSOfficeDocFilter filter = new MSOfficeDocFilter();
        // use the company(1001) id
        filter.setCompanyId((long) 1001);
        filter.setFilterName(filterName);
        filter.setFilterDescription("Used for unit test");
        filter.setHeaderTranslate(false);
        filter.setAltTranslate(false);
        filter.setParaStyles(selectParaStyles, allParaStyles);
        filter.setCharStyles(selectCharStyles, allCharStyles);
        filter.setInTextStyles(selectInternalStyle, allInternalStyle);
        filter.setContentPostFilterId(-2);
        filter.setContentPostFilterTableName("");
        HibernateUtil.saveOrUpdate(filter);
        long id = filter.getId();
        if (!m_addedDocFilter.contains(new Long(id)))
        {
            m_addedDocFilter.add(new Long(id));
        }
        return id;
    }
}
