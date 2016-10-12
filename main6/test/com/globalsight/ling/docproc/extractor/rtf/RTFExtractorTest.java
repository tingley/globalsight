package com.globalsight.ling.docproc.extractor.rtf;

import static org.junit.Assert.fail;

import java.io.File;
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
import com.globalsight.ling.docproc.extractor.html.Extractor;

public class RTFExtractorTest extends BaseExtractorTestClass
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
     * This Unit Test is used for testing the process in GlobalSight for RTF
     * file. When create a job with RTF file, it will be converted to HTML files
     * by MsOffice word 2007 converter, then the HTML files will be extracted by
     * HTML Extractor. In this test, the files in source folder are from the
     * result of Converter.
     * 
     */
    @Test
    public void testExtractor()
    {
        ArrayList<FileSet> fileSet = fileSets.get("testExtractor");
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
        extractor = new Extractor();
        extractor.setIgnoreInvalidHtmlTags(true);
        extractor.setFilterTableName("RTF");
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
                { "testExtractor", "1303372686359PlanAction.html",
                        "1303372686359PlanAction.txt",
                        "1303372686359PlanAction.html" },
                { "testExtractor", "1303373204765Welocalize_Company.html",
                        "1303373204765Welocalize_Company.txt",
                        "1303373204765Welocalize_Company.html" } };

        return formFileSets(fileSet, RTFExtractorTest.class);
    }
}
