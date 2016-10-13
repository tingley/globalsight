package com.globalsight.ling.docproc.extractor.html;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.CtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.L10nContent;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.merger.jsp.JspPostMergeProcessor;

public class JSPExtractorTest<E> extends BaseExtractorTestClass
{
    private File sourceFile = null;
    private File answerFile = null;
    private File roundtripFile = null;

    private HashMap<String, ArrayList<FileSet>> fileSets = new HashMap<String, ArrayList<FileSet>>();
    private static final String UTF8 = "UTF-8";
    private JSPExtractor extractor;

    @Before
    public void setUp()
    {
        initExtractor();
        fileSets = initFileSet();
    }

    /**
     * Test extractor
     */
    @Test
    public void testJSPExtractor01()
    {
        ArrayList<FileSet> fileSet = fileSets.get("testJSPExtractor01");
        if (fileSet != null && fileSet.size() > 0)
        {
            Iterator<FileSet> it = fileSet.iterator();
            try
            {
                while (it.hasNext())
                {
                    FileSet fs = it.next();
                    sourceFile = fs.getSourceFile();
                    answerFile = fs.getAnswerFile();
                    roundtripFile = fs.getRoundtripFile();

                    String gxml = getFileContent(sourceFile, extractor, "utf-8");
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
                fail("error");
            }
        }
    }

    /**
     * Test Merge with the option: Add Additional Head(true)
     */
    @Test
    public void testJSPExtractor02()
    {
        ArrayList<FileSet> fileSet = fileSets.get("testJSPExtractor02");
        if (fileSet != null && fileSet.size() > 0)
        {
            Iterator<FileSet> it = fileSet.iterator();
            try
            {
                while (it.hasNext())
                {
                    FileSet fs = it.next();
                    sourceFile = fs.getSourceFile();
                    answerFile = fs.getAnswerFile();
                    roundtripFile = fs.getRoundtripFile();

                    String gxml = getFileContent(sourceFile, extractor, "utf-8");
                    // generate target file
                    byte[] mergeResult = getTargetFileContent(gxml, UTF8, true);
                    String s = new String(mergeResult, UTF8);
                    File tmpFile = new File(roundtripFile.getParentFile()
                            .getAbsoluteFile()
                            + File.separator + roundtripFile.getName() + ".tmp");
                    generateFile(tmpFile, s, UTF8);
                    Assert.assertTrue(fileCompareNoCareEndLining(tmpFile,
                            roundtripFile));
                }
            }
            catch (Exception e)
            {
                fail("error");
            }
        }
    }

    /**
     * Test Merge with the option: Add Additional Head(false)
     */
    @Test
    public void testJSPExtractor03()
    {
        ArrayList<FileSet> fileSet = fileSets.get("testJSPExtractor03");
        if (fileSet != null && fileSet.size() > 0)
        {
            Iterator<FileSet> it = fileSet.iterator();
            try
            {
                while (it.hasNext())
                {
                    FileSet fs = it.next();
                    sourceFile = fs.getSourceFile();
                    answerFile = fs.getAnswerFile();
                    roundtripFile = fs.getRoundtripFile();

                    String gxml = getFileContent(sourceFile, extractor, "utf-8");
                    // generate target file
                    byte[] mergeResult = getTargetFileContent(gxml, UTF8, false);
                    String s = new String(mergeResult, UTF8);
                    File tmpFile = new File(roundtripFile.getParentFile()
                            .getAbsoluteFile()
                            + File.separator + roundtripFile.getName() + ".tmp");
                    generateFile(tmpFile, s, UTF8);
                    Assert.assertTrue(fileCompareNoCareEndLining(tmpFile,
                            roundtripFile));
                }
            }
            catch (Exception e)
            {
                fail("error");
            }
        }
    }

    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();
        String gxml = DiplomatWriter.WriteXML(output);
        DiplomatSegmenter seg = new DiplomatSegmenter();
        gxml = seg.segment(gxml).replace("&apos;", "'");
        return gxml;
    }

    public AbstractExtractor initExtractor()
    {
        extractor = new JSPExtractor();
        extractor.setIgnoreInvalidHtmlTags(true);
        return extractor;
    }

    @Override
    public Output doExtract(File file, AbstractExtractor extractor,
            String encoding)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<String, ArrayList<FileSet>> initFileSet()
    {
        String[][] fileSet =
        {
                { "testJSPExtractor01", "JSPExtractor.jsp", "JSPExtractor.txt",
                        "JSPExtractor.jsp" },
                { "testJSPExtractor02", "JSPWithHead.jsp", "JSPWithHead02.txt",
                        "JSPWithHead_02.jsp" },
                { "testJSPExtractor02", "JSPWithNoHead.jsp",
                        "JSPWithNoHead02.txt", "JSPWithNoHead_02.jsp" },
                { "testJSPExtractor03", "JSPWithHead.jsp", "JSPWithHead03.txt",
                        "JSPWithHead_03.jsp" },
                { "testJSPExtractor03", "JSPWithNoHead.jsp",
                        "JSPWithNoHead03.txt", "JSPWithNoHead_03.jsp" } };

        return formFileSets(fileSet, JSPExtractorTest.class);
    }

    /**
     * Generate target file with the given GXML, used to test the
     * addAdditionalHead option in JSPFilter
     * 
     * @param gxml
     * @return
     * @throws TranscoderException
     */
    private byte[] getTargetFileContent(String gxml, String encoding,
            boolean addAdditionalHead) throws TranscoderException
    {
        L10nContent l10ncontent = new L10nContent();
        DiplomatMerger merger = new DiplomatMerger();
        merger.setFilterId(-1);
        merger.init(gxml, l10ncontent);
        merger.setKeepGsa(false);
        merger.setTargetEncoding(encoding);

        boolean isUseSecondaryFilter = false;
        boolean convertHtmlEntry = false;
        merger.setIsUseSecondaryFilter(isUseSecondaryFilter);
        merger.setConvertHtmlEntryFromSecondFilter(convertHtmlEntry);
        merger.merge();

        String gxml1 = l10ncontent.getL10nContent();
        l10ncontent.setL10nContent(CtrlCharConverter.convertToCtrl(gxml1));
        String processed = postMergeProcess(l10ncontent.getL10nContent(),
                merger.getDocumentFormat(), encoding, addAdditionalHead);

        if (processed != null)
        {
            l10ncontent.setL10nContent(processed);
        }
        return l10ncontent.getTranscodedL10nContent(encoding);
    }

    /**
     * Used to test the addAdditionalHead option in JSPFilter
     * 
     * @param p_content
     * @param p_format
     * @param p_ianaEncoding
     * @param addAdditionalHead
     *            the option in JSP Filter
     * @return
     * @throws DiplomatMergerException
     */
    private String postMergeProcess(String p_content, String p_format,
            String p_ianaEncoding, boolean addAdditionalHead)
            throws DiplomatMergerException
    {
        ExtractorRegistry registry = ExtractorRegistry.getObject();

        int formatId = registry.getFormatId(p_format);
        if (formatId == -1)
        {
            return null;
        }

        String strClass = registry.getPostMergeClasspath(formatId);

        PostMergeProcessor processor = null;
        try
        {
            processor = (PostMergeProcessor) Class.forName(strClass)
                    .newInstance();
            ((JspPostMergeProcessor) processor)
                    .setAddAdditionalHead(addAdditionalHead);
        }
        catch (Exception e)
        {
            throw new DiplomatMergerException(
                    "PostMergeProcessorCreationFailure", null, e);
        }
        return processor.process(p_content, p_ianaEncoding);
    }

}
