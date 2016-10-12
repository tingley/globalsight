package com.globalsight.ling.docproc.extractor.fm;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.FMFilter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.SpecificFileFilter;

public class ExtractorTest extends BaseExtractorTestClass
{
    private String sourceRoot = null;
    private String answerRoot = null;
    private String roundtripRoot = null;
    
    private static final String UTF8 = "UTF-8";
    private static final String extension = "mif";
    
    private Extractor extractor;
    private FMFilter mainFilter = null;
    
    /**
     * true: create answer files
     * false: don't create answer files
     */
    private boolean generateAnswerFiles = false;
    
    @Before
    public void setUp()
    {
        mainFilter = initFmFilter();
        initExtractor();
        
        sourceRoot = getResourcePath(ExtractorTest.class, "source");
        answerRoot = getResourcePath(ExtractorTest.class, "answers");
        roundtripRoot = getResourcePath(ExtractorTest.class, "roundtrip");
    }
    
    @Test
    public void testExtractor()
    {
        File source = new File(sourceRoot);
        File[] sourceFiles = source.listFiles(new SpecificFileFilter(extension));
        for (int i = 0; i < sourceFiles.length; i++)
        {
            try
            {
                // Read source files
                File sourceFile = sourceFiles[i];
                // Get file content
                String gxml = getFileContent(sourceFile, extractor, UTF8);
                String answerContent = getTranslatableTextContent(gxml);
                
                String fileName = sourceFile.getName();
                String answerFileName = fileName.substring(0, fileName
                        .lastIndexOf(".")) + ".txt";
                File answerFile = new File(answerRoot + File.separator + answerFileName);
                File tmpFile = new File(answerRoot + File.separator + answerFileName + ".tmp");
                if (generateAnswerFiles){
                    // Generate Answer files
                    generateFile(answerFile, answerContent, UTF8);
                }
                if (!answerFile.exists())
                {
                    fail("The file compared to :" + answerFile.getName() + " doesn't exist");
                }
                // Generate files for compare
                generateFile(tmpFile, answerContent, UTF8);
                if (fileCompareNoCareEndLining(tmpFile, answerFile))
                {
                    tmpFile.delete();
                    // generate target file
                    File rountTipFile = new File(roundtripRoot + File.separator + fileName);
                    CxeMessageType cmt = CxeMessageType
                            .getCxeMessageType(CxeMessageType.MIF_LOCALIZED_EVENT);
                    CxeMessage cxeMessage = new CxeMessage(cmt);
                    setCxeMessage(cxeMessage);
                    byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                    String s = new String(mergeResult, UTF8);
                    generateFile(rountTipFile, s, UTF8);
                    Assert.assertTrue(rountTipFile.exists());
                }
                else
                {
                    fail("\n" + tmpFile + "\n and \n" + answerFile + " not equal");
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
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
        extractor.setMainFilter(mainFilter);
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
    public HashMap initFileSet()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    private FMFilter initFmFilter()
    {
        FMFilter ff = new FMFilter();
        ff.setExposeLeftMasterPage(true);
        ff.setExposeOtherMasterPage(true);
        ff.setExposeRightMasterPage(true);
        return ff;
    }
    
}
