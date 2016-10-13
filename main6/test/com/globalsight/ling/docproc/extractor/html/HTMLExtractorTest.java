package com.globalsight.ling.docproc.extractor.html;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;

public class HTMLExtractorTest extends BaseExtractorTestClass
{

    private static final String UTF8 = "UTF-8";
    private static final String DEFAULT_EMBEDDABLE_TAGS = "a,abbr,acronym,"
            + "b,basefont,bdo,big,blink,cite,code,del,dfn,em,font,i,img,ins,kbd,"
            + "nobr,q,s,samp,small,span,strike,strong,sub,sup,tt,u,var,wbr";
    private static final String DEFAULT_PAIRED_TAGS = "a,abbr,acronym,b,bdo,big,"
            + "blink,button,cite,code,del,dfn,em,font,i,ins,kbd,label,nobr,plaintext,"
            + "q,ruby,s,samp,select,small,span,strike,strong,sub,sup,textarea,tt,u,"
            + "var,xmp";
    private static final String DEFAULT_TRANSLATABLE_ATTRIBUTES = "abbr,accesskey,"
            + "alt,char,label,prompt,standby,summary,title";
    private static final String DEFAULT_UNPAIRED_TAGS = "br,hr,img,input,rt,wbr";
    private static final String DEFAULT_SWITCH_TAG_MAPS = "script:javascript,style:css-styles,xml:xml";
    private static final String DEFAULT_WHITE_PRESERVING_TAGS = "listing,pre";
    
    private static Extractor extractor;
    private String errorMsg = "";
    private static HashMap fileSets = new HashMap(); 
    private HtmlFilter mainFilter = null;
    private boolean generateAnswerFiles = false;
    
    // public methods
    public Output doExtract(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }

    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        input.setType(1); // represent html
        extractor.init(input, output);
        extractor.setMainFilter(mainFilter);
        extractor.loadRules();
        extractor.extract();
        String gxml = DiplomatWriter.WriteXML(output);
        DiplomatSegmenter seg = new DiplomatSegmenter();
        gxml = seg.segment(gxml);
        return gxml;
    }

    public AbstractExtractor initExtractor()
    {
        extractor = new Extractor();
        return extractor;
    }

    public HashMap initFileSet()
    {
        String[][] fileSet =
        {
            {"test_Default_Filter", "Default_Filter.html", "Default_Filter.txt", "Default_Filter.html"},
            {"test_Convert_HTML_Entity_For_Export", "Entity_For_Export.html", "Entity_For_Export.txt", "Entity_For_Export.html"},
            {"test_Ignore_Invalid_HTML_Tags", "Ignore_Invalid_HTML_Tags.htm", "Ignore_Invalid_HTML_Tags.txt", "Ignore_Invalid_HTML_Tags.htm"},
            {"test_Localize_Function", "Localize_Function.html", "Localize_Function.txt", "Localize_Function.html"},
            {"test_Embeddable_Tags", "Embeddable_Tags.html", "Embeddable_Tags.txt", "Embeddable_Tags.html"},
            {"test_Paired_Tags", "Paired_Tags.html", "Paired_Tags.txt", "Paired_Tags.html"},
            {"test_Unpaired_Tags", "Unpaired_Tags.html", "Unpaired_Tags.txt", "Unpaired_Tags.html"},
            {"test_Switch_Tag_Map", "Switch_Tag_Map.html", "Switch_Tag_Map.txt", "Switch_Tag_Map.html"},
            {"test_Whitespace_Preserving_Tags", "Whitespace_Preserving_Tags.html", "Whitespace_Preserving_Tags.txt", "Whitespace_Preserving_Tags.html"},
            {"test_Translatable_Attributes", "Translatable_Attributes.html", "Translatable_Attributes.txt", "Translatable_Attributes.html"},
            {"test_Internal_Tags", "Internal_Tags.html", "Internal_Tags.txt", "Internal_Tags.html"}
        };
        
        return formFileSets(fileSet, HTMLExtractorTest.class);
    }
    
    // private methods
    private HtmlFilter initMainFilter()
    {
        HtmlFilter htmlFilter = new HtmlFilter();
        htmlFilter.setPlaceHolderTrim("embeddable_tags");
        htmlFilter.setDefaultEmbeddableTags(DEFAULT_EMBEDDABLE_TAGS);
        htmlFilter.setDefaultPairedTags(DEFAULT_PAIRED_TAGS);
        htmlFilter.setDefaultUnpairedTags(DEFAULT_UNPAIRED_TAGS);
        htmlFilter.setDefaultSwitchTagMaps(DEFAULT_SWITCH_TAG_MAPS);
        htmlFilter.setDefaultWhitePreservingTags(DEFAULT_WHITE_PRESERVING_TAGS);
        htmlFilter.setDefaultTranslatableAttributes(DEFAULT_TRANSLATABLE_ATTRIBUTES);
        
        htmlFilter.setFilterName("HTML Filter");
        htmlFilter.setJsFunctionText("");
        htmlFilter.setNonTranslatableMetaAttributes("");
        htmlFilter.setPairedTags("");
        htmlFilter.setSwitchTagMaps("");
        htmlFilter.setTranslatableAttributes("");
        htmlFilter.setUnpairedTags("");
        htmlFilter.setWhitePreservingTags("");
        htmlFilter.setEmbeddableTags("");
        return htmlFilter;
    }
    
    private boolean handle(List fileSet)
    {
        boolean returnValue = false;
        try
        {
            if (fileSet != null && fileSet.size() > 0)
            {
                for (int i = 0; i < fileSet.size(); i++)
                {
                    FileSet fs = (FileSet) fileSet.get(i);
                    File sourceFile = fs.getSourceFile();
                    File answerFile = fs.getAnswerFile();
                    File roundtripFile = fs.getRoundtripFile();

                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        String gxml = getFileContent(sourceFile, extractor, UTF8);
                        String answerContent = getTranslatableTextContent(gxml);
                        if (generateAnswerFiles)
                        {
                            // Generate Answer files
                            generateFile(answerFile, answerContent, UTF8);
                        }
                        if (!answerFile.exists())
                        {
                            errorMsg += "The file compared to :"
                                    + answerFile.getName()
                                    + " doesn't exist \n";
                            break;
                        }
                        File tmpFile = new File(answerFile.getPath()
                                + ".tmp");
                        generateFile(tmpFile, answerContent, UTF8);
                        if (fileCompareNoCareEndLining(tmpFile, answerFile))
                        {
                            tmpFile.delete();
                            // generate target file
                            CxeMessageType cmt = CxeMessageType
                                    .getCxeMessageType(CxeMessageType.HTML_LOCALIZED_EVENT);
                            CxeMessage cxeMessage = new CxeMessage(cmt);
                            setCxeMessage(cxeMessage);
                            byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                            String s = new String(mergeResult, UTF8);
                            generateFile(roundtripFile, s, UTF8);
                            returnValue = roundtripFile.exists();
                        }
                        else
                        {
                            errorMsg += tmpFile + "\n and \n" + answerFile
                                    + " not equal \n";
                        }
                    }
                }
            }
            else 
            {
                fail("File doesn't exist.");
            }
        }
        catch (Exception e)
        {
            errorMsg += e.getMessage() + "\n";
        }
        return returnValue;
    }
    
    // Test methods
    @Before
    public void setUp()
    {
        initExtractor();
        mainFilter = initMainFilter();
        fileSets = initFileSet();
        errorMsg = "";
    }
    
    @After
    public void destroy()
    {
        mainFilter = null;
    }
    
    /**
     * Test default html filter.
     */
    @Test
    public void test_Default_Filter()
    {
        mainFilter.setConvertHtmlEntry(true);
        mainFilter.setIgnoreInvalideHtmlTags(true);
        mainFilter.setJsFunctionText("l10n");
        mainFilter.setEmbeddableTags(DEFAULT_EMBEDDABLE_TAGS);
        mainFilter.setPairedTags(DEFAULT_PAIRED_TAGS);
        mainFilter.setUnpairedTags(DEFAULT_UNPAIRED_TAGS);
        mainFilter.setSwitchTagMaps(DEFAULT_SWITCH_TAG_MAPS);
        mainFilter.setWhitePreservingTags(DEFAULT_WHITE_PRESERVING_TAGS);
        mainFilter.setTranslatableAttributes(DEFAULT_TRANSLATABLE_ATTRIBUTES);
        
        extractor.setInternalTags(mainFilter.getInternalTags());
        extractor.setIgnoreInvalidHtmlTags(true);
        extractor.setJsFunctionText("l10n");
        setConvertHtmlEntityForHtml(true);
        
        ExtractionRules rules = new ExtractionRules(mainFilter);
        extractor.m_rules = rules;

        ArrayList<FileSet> fileSet = (ArrayList) fileSets
                .get("test_Default_Filter");
        boolean result = handle(fileSet);
        Assert.assertTrue(errorMsg, result);
    }
    
    /**
     * The filter works when creating target file.
     */
    @Test
    public void test_Convert_HTML_Entity_For_Export()
    {
        boolean flag = true;
        extractor.setIgnoreInvalidHtmlTags(true);
        mainFilter.setConvertHtmlEntry(flag);
        setConvertHtmlEntityForHtml(flag);
        
        ExtractionRules rules = new ExtractionRules(mainFilter);
        extractor.m_rules = rules;
        
        ArrayList<FileSet> fileSet = (ArrayList) fileSets
                .get("test_Convert_HTML_Entity_For_Export");
        boolean result = handle(fileSet);
        Assert.assertTrue(errorMsg, result);
    }
    
    /**
     * The test file -- Ignore_Invalid_HTML_Tag.html -- contains invalid tags.
     * If set "Ignore Invalid HTML Tags" to false, the process should fail;
     * If set "Ignore Invalid HTML Tags" to true, the process should succeed.
     */
    @Test
    public void test_Ignore_Invalid_HTML_Tags() // done
    {
        ArrayList<FileSet> fileSet = (ArrayList) fileSets
                .get("test_Ignore_Invalid_HTML_Tags");
        extractor.setIgnoreInvalidHtmlTags(true);
        boolean result = handle(fileSet);
        Assert.assertTrue(errorMsg, result);
        
        errorMsg = "";
        
        extractor.setIgnoreInvalidHtmlTags(false);
        result = handle(fileSet);
        Assert.assertFalse(errorMsg, result);
    }
    
    /**
     * The test file -- "Localize_Function.html" -- contains javascript functions.
     * The alert part should be translated, and the confirm part should not be
     * translated.
     */
    @Test
    public void test_Localize_Function() // done
    {
        try
        {
            extractor.setIgnoreInvalidHtmlTags(true);
            extractor.setJsFunctionText("alert");
            mainFilter.setJsFunctionText("alert");
            mainFilter.setSwitchTagMaps("script:javascript");
            ExtractionRules rules = new ExtractionRules(mainFilter);
            extractor.m_rules = rules;
            ArrayList<FileSet> fileSet = (ArrayList) fileSets
                    .get("test_Localize_Function");
            if (fileSet != null)
            {
                File sourceFile = ((FileSet) fileSet.get(0)).getSourceFile();
                String gxml = getFileContent(sourceFile, extractor, UTF8);
                String tmpString = getTranslatableTextContent(gxml);
                
                Assert.assertTrue(tmpString.contains("This is a book."));
                Assert.assertFalse(tmpString.contains("Are you sure?"));
                boolean result = handle(fileSet);
                Assert.assertTrue(errorMsg, result);
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    /**
     * The test file -- Embeddable_Tags.html -- contains 'a' and 'b' tags.
     * Embeddable Tags only contains a, so the link will be transfered to inline tags.
     * And the 'b' tag should be added into skeleton.
     * 
     * Check answer file for the result.
     */
    @Test
    public void test_Embeddable_Tags() // done
    {
        mainFilter.setEmbeddableTags("a");
        extractor.setIgnoreInvalidHtmlTags(true);
        ExtractionRules rules = new ExtractionRules(mainFilter);
        extractor.m_rules = rules;

        ArrayList<FileSet> fileSet = (ArrayList) fileSets
                .get("test_Embeddable_Tags");
        boolean result = handle(fileSet);
        Assert.assertTrue(errorMsg, result);
    }
    
    /**
     * The test file -- Paired_Tags.html -- contains a paired tag "ww".
     * So in gxml, the tag should start with bpt and end with bpt.
     */
    @Test
    public void test_Paired_Tags() // done
    {
        try
        {
            extractor.setIgnoreInvalidHtmlTags(true);
            mainFilter.setEmbeddableTags("ww");
            mainFilter.setPairedTags("ww");
            ExtractionRules rules = new ExtractionRules(mainFilter);
            extractor.m_rules = rules;
            
            ArrayList<FileSet> fileSet = (ArrayList) fileSets
                    .get("test_Paired_Tags");
            if (fileSet != null)
            {
                File sourceFile = ((FileSet) fileSet.get(0)).getSourceFile();
                String gxml = getFileContent(sourceFile, extractor, UTF8);
                
                Assert.assertTrue(gxml.contains("<bpt") && gxml.contains("<ept"));
                boolean result = handle(fileSet);
                Assert.assertTrue(errorMsg, result);
            }
            else 
            {
                fail("File doesn't exist.");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    /**
     * The test file -- Unpaired_Tags.html -- contains a br tag,
     * so the tag should be transfered to inline tags with ph.
     */
    @Test
    public void test_Unpaired_Tags() // done
    {
        try
        {
            extractor.setIgnoreInvalidHtmlTags(true);
            mainFilter.setUnpairedTags("br");
            mainFilter.setEmbeddableTags("br");
            ExtractionRules rules = new ExtractionRules(mainFilter);
            extractor.m_rules = rules;

            ArrayList<FileSet> fileSet = (ArrayList) fileSets
                    .get("test_Unpaired_Tags");
            if (fileSet != null)
            {
                File sourceFile = ((FileSet) fileSet.get(0)).getSourceFile();
                String gxml = getFileContent(sourceFile, extractor, UTF8);

                Assert.assertTrue(gxml.contains("ph"));
                boolean result = handle(fileSet);
                Assert.assertTrue(errorMsg, result);
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    /**
     * The test file -- Switch_Tag_Map.html -- contains 2 functions,
     * each of which contains an alert and a confirm. The alerts and 
     * confirms should all be translated.
     */
    @Test
    public void test_Switch_Tag_Map() // done
    {
        extractor.setIgnoreInvalidHtmlTags(true);
        mainFilter.setSwitchTagMaps("script:javascript");
        ExtractionRules rules = new ExtractionRules(mainFilter);
        extractor.m_rules = rules;

        ArrayList<FileSet> fileSet = (ArrayList) fileSets
                .get("test_Switch_Tag_Map");
        boolean result = handle(fileSet);
        Assert.assertTrue(errorMsg, result);
    }
    
    /**
     * The test file -- Whitespace_Preserving_Tags.html -- contains a pre tag.
     */
    @Test
    public void test_Whitespace_Preserving_Tags() // done
    {
        try
        {
            extractor.setIgnoreInvalidHtmlTags(true);
            mainFilter.setWhitePreservingTags("pre");
            ExtractionRules rules = new ExtractionRules(mainFilter);
            extractor.m_rules = rules;
            
            ArrayList<FileSet> fileSet = (ArrayList) fileSets
                    .get("test_Whitespace_Preserving_Tags");
            if (fileSet != null)
            {
                File sourceFile = ((FileSet) fileSet.get(0)).getSourceFile();
                String gxml = getFileContent(sourceFile, extractor, UTF8);
                String tmpString = getTranslatableTextContent(gxml);
                
                Assert.assertFalse(tmpString.contains("The text format will not change."));
                boolean result = handle(fileSet);
                Assert.assertTrue(errorMsg, result);
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    /**
     * The test file -- Translatable_Attributes.html -- contains an input,
     * and the value of the input is "Textbox".
     * "Textbox" should be translated.
     */
    @Test
    public void test_Translatable_Attributes() // done
    {
        try
        {
            extractor.setIgnoreInvalidHtmlTags(true);
            mainFilter.setTranslatableAttributes("value");
            ExtractionRules rules = new ExtractionRules(mainFilter);
            extractor.m_rules = rules;

            ArrayList<FileSet> fileSet = (ArrayList) fileSets
                    .get("test_Translatable_Attributes");
            if (fileSet != null)
            {
                File sourceFile = ((FileSet) fileSet.get(0)).getSourceFile();
                String gxml = getFileContent(sourceFile, extractor, UTF8);
                String tmpString = getTranslatableTextContent(gxml);
                
                Assert.assertTrue(tmpString.contains("Textbox"));
                boolean result = handle(fileSet);
                Assert.assertTrue(errorMsg, result);
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    /**
     * The test file -- Internal_Tags.html
     */
    @Test
    public void test_Internal_Tags()
    {
        try
        {
            extractor.setIgnoreInvalidHtmlTags(true);
            mainFilter.setInternalTagMaps("&lt;w&gt;,&lt;a&gt;,&lt;b&gt;");// <a><b><w>
            extractor.setInternalTags(mainFilter.getInternalTags());
            ExtractionRules rules = new ExtractionRules(mainFilter);
            extractor.m_rules = rules;

            ArrayList<FileSet> fileSet = (ArrayList) fileSets
                    .get("test_Internal_Tags");
            if (fileSet != null)
            {
                File sourceFile = ((FileSet) fileSet.get(0)).getSourceFile();
                String gxml = getFileContent(sourceFile, extractor, UTF8);
                Assert.assertTrue(gxml.contains("bpt internal=\"yes\""));
                boolean result = handle(fileSet);
                Assert.assertTrue(errorMsg, result);
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
}
