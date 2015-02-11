/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.docproc.extractor.xml;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConfigParser;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor;

public class ExtractorTest extends BaseExtractorTestClass
{
    // / Variables
    private String defaultEncoding = "UTF-8";
    private String xmlRuleFile = null;
    private String caseSimpleXmlFile = null;
    private String caseAttrXmlFile = null;
    private String caseAttrInlineXmlFile = null;
    private String case1719XmlFile = null;

    private static final String UTF8 = "UTF-8";
    private Extractor extractor;
    private HashMap fileSets = new HashMap();

    // / Public Methods
    @Before
    public void init()
    {
        // out put some message for verify
        String userDir = System.getProperty("user.dir");
        System.out.println("user.dir : " + userDir);
        System.out.println(ExtractorTest.class.getResource("/"));
        System.out.println(ExtractorTest.class
                .getResource("/properties/schemarules.rng"));

        xmlRuleFile = FileUtil.getResourcePath(ExtractorTest.class,
                "files/XmlRule.xml");
        caseSimpleXmlFile = FileUtil.getResourcePath(ExtractorTest.class,
                "files/caseSimpleSrc.xml");
        caseAttrXmlFile = FileUtil.getResourcePath(ExtractorTest.class,
                "files/caseAttrSrc.xml");
        caseAttrInlineXmlFile = FileUtil.getResourcePath(ExtractorTest.class,
                "files/caseAttrInlineSrc.xml");
        case1719XmlFile = FileUtil.getResourcePath(ExtractorTest.class,
                "files/case1719Src.xml");

        // GBS-1924 Base Filter Unittest: XML Filter
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
     * Case 1, simple extract without rule and filter
     */
    @Test
    public void testSimpleExtract() throws Exception
    {
        Output output = doExtract(caseSimpleXmlFile, xmlRuleFile, null);

        String gxml = DiplomatWriter.WriteXML(output);
        System.out.println(gxml);
        assertTrue(gxml
                .contains("<translatable blockId=\"1\">CDATA data here</translatable>"));

        Output segout = doSegment(output);
        String segGxml = DiplomatWriter.WriteXML(segout);
        System.out.println(segGxml);
        assertTrue(segGxml
                .contains("<segment segmentId=\"1\">CDATA data here</segment>"));
    }

    /**
     * Case 2, test Attribute, is inline = no
     * 
     * @throws Exception
     */
    @Test
    public void testAttribute() throws Exception
    {
        Output output = doExtract(caseAttrXmlFile, xmlRuleFile, null);

        String gxml = DiplomatWriter.WriteXML(output);
        System.out.println(gxml);
        assertTrue(gxml
                .contains("<translatable blockId=\"2\">bbb title</translatable>"));
    }

    /**
     * Case 3, test Attribute inline, is inline = yes
     * 
     * @throws Exception
     */
    @Test
    public void testAttributeInline() throws Exception
    {
        Output output = doExtract(caseAttrInlineXmlFile, xmlRuleFile, null);

        String gxml = DiplomatWriter.WriteXML(output);
        System.out.println(gxml);
        assertTrue(gxml
                .contains("<sub locType=\"translatable\">bbb title</sub>"));
    }

    /**
     * For GBS-1719, Test xml content with &amp; entity
     * 
     * @throws Exception
     */
    @Test
    public void testAmpEntity() throws Exception
    {
        Output output = doExtract(case1719XmlFile, xmlRuleFile, null);

        String gxml = DiplomatWriter.WriteXML(output);
        System.out.println(gxml);
        assertTrue(gxml.contains("News &amp; Press"));
    }

    @Test
    public void testSwitchToHtml() throws FileNotFoundException, IOException
    {
        String ori = "test internal <GS-INTERNAL-TEXT>#message here#</GS-INTERNAL-TEXT>text";
        EFInputData input = createInput(caseSimpleXmlFile, null,
                defaultEncoding);
        Output output = new Output();
        AbstractExtractor extractor = new Extractor();
        extractor.init(input, output);
        Output outp = extractor.switchExtractor(ori,
                ExtractorRegistry.FORMAT_HTML);
        outp = doSegment(outp);
        String resultContent = getTranslatableTextContent(outp);
        System.out.println(resultContent);

        assertTrue(resultContent + " does not contain internal=\"yes\"",
                resultContent.contains("internal=\"yes\""));
    }

    /**
     * Main method, copy necessary properties to Project_Root\bin\properties for
     * run this class in eclipse
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(ExtractorTest.class);
    }

    // / Private Methods
    private EFInputData createInput(String fileUrl, String ruleFile,
            String encoding) throws FileNotFoundException, IOException
    {
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        java.util.Locale locale = new java.util.Locale("en", "US");
        input.setLocale(locale);
        input.setURL("file:" + fileUrl);
        input.setRules(FileUtil.readRuleFile(ruleFile, defaultEncoding));

        return input;
    }

    private Output doExtract(String xmlFile, String ruleFile,
            XMLRuleFilter filter) throws FileNotFoundException, IOException
    {
        EFInputData input = createInput(xmlFile, ruleFile, defaultEncoding);
        Output output = new Output();

        AbstractExtractor extractor = new Extractor();
        extractor.init(input, output);
        extractor.setMainFilter(filter);
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

    // //////////////////////////////////////////////////////////////////////
    // GBS-1924 Base Filter Unittest: XML Filter
    // ////////////////////////////////////////////////////////////////////
    /**
     * Test XML file extractor and merger.
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
                        Output output = doExtract(sourceFile.getAbsolutePath(),
                                null, new XMLRuleFilter());
                        // do segment
                        output = doSegment(output);
                        // get translatable text content
                        String resultContent = getTranslatableTextContent(output);
                        // generate result file for compare purpose
                        File tmpResultFile = new File(answerFile
                                .getParentFile().getAbsolutePath()
                                + File.separator
                                + sourceFile.getName()
                                + ".tmp");
                        generateFile(tmpResultFile, resultContent, UTF8);

                        // compare result file to answer file
                        if (fileCompareNoCareEndLining(tmpResultFile,
                                answerFile))
                        {
                            tmpResultFile.delete();

                            // generate target file
                            String gxml = DiplomatWriter.WriteXML(output);
                            byte[] mergeResult = getTargetFileContent(gxml,
                                    UTF8);
                            String s = new String(mergeResult, UTF8);
                            generateFile(roundtripFile, s, UTF8);
                            Assert.assertTrue(roundtripFile.exists());
                            fileCompareNoCareEndLining(roundtripFile,
                                    sourceFile);
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n"
                                    + answerFile + " not equal");
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

    // //////////////////////////////////////////////////////////////////////
    // GBS-1925 Filter Configuration Unittests: XML Filter
    // ////////////////////////////////////////////////////////////////////
    /**
     * Test XML file extractor and merger with filter
     */
    @Test
    public void testExtractorWithFilter()
    {
        try
        {
            ArrayList fileSet = (ArrayList) fileSets
                    .get("testExtractorWithFilter");
            if (fileSet != null && fileSet.size() > 0)
            {
                Iterator it = fileSet.iterator();
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    File sourceFile = fs.getSourceFile();
                    File answerFile = fs.getAnswerFile();
                    File roundtripFile = fs.getRoundtripFile();
                    String sourceFileName = sourceFile.getName();

                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // extract source file with default xml filter
                        Output output = doExtract(sourceFile.getAbsolutePath(),
                                null, createXMLFilterByFileName(sourceFileName));
                        // do segment
                        output = doSegment(output);
                        // get translatable text content
                        String resultContent = getTranslatableTextContent(output);
                        String gxml = DiplomatWriter.WriteXML(output);
                        System.out.println(resultContent);
                        // generate result file for compare purpose
                        File tmpResultFile = new File(answerFile
                                .getParentFile().getAbsolutePath()
                                + File.separator
                                + sourceFile.getName()
                                + ".tmp");
                        generateFile(tmpResultFile, resultContent, UTF8);

                        // compare result file to answer file
                        if (fileCompareNoCareEndLining(tmpResultFile,
                                answerFile))
                        {
                            tmpResultFile.delete();

                            // generate target file
                            byte[] mergeResult = getTargetFileContent(gxml,
                                    UTF8);
                            String s = new String(mergeResult, UTF8);
                            // special check for some option
                            if ("sampleNonAsciiChar.xml"
                                    .equalsIgnoreCase(sourceFileName))
                            {
                                Method m = XmlFilterHelper.class
                                        .getDeclaredMethod(
                                                "saveNonAsciiAsNumberEntity",
                                                String.class);
                                m.setAccessible(true);
                                Object result = m.invoke(new XmlFilterHelper(
                                        null), s);
                                String exportContent = result.toString();
                                Assert.assertTrue(exportContent
                                        .contains("This is aaa &#25991;&#26412;."));
                            }
                            else if ("sampleWhiteSpace.xml"
                                    .equalsIgnoreCase(sourceFileName))
                            {
                                Assert.assertTrue(s
                                        .contains("<aaa> I am a long sentence. </aaa>"));
                            }
                            else if ("sampleSid.xml"
                                    .equalsIgnoreCase(sourceFileName))
                            {
                                Assert.assertTrue(gxml
                                        .contains("<translatable sid=\"007\" "));
                            }
                            else if ("sampleGenerateLan.xml"
                                    .equalsIgnoreCase(sourceFileName))
                            {
                                XmlPostMergeProcessor processor = new XmlPostMergeProcessor();
                                processor.setGenerateEncoding(false);
                                processor.setGenerateLang(true);
                                processor.setLocale(Locale.CHINA);

                                String newS = processor.process(s, "UTF-8");
                                Assert.assertTrue(newS
                                        .contains("<sample xml:lang=\"zh-CN\">"));
                            }
                            else if ("sampleEntities.xml"
                                    .equalsIgnoreCase(sourceFileName))
                            {
                                Assert.assertTrue(s
                                        .contains("<aaa>This is copy entity ©.</aaa>"));
                            }
                            else
                            {
                                generateFile(roundtripFile, s, UTF8);
                                Assert.assertTrue(roundtripFile.exists());
                                fileCompareNoCareEndLining(roundtripFile,
                                        sourceFile);
                            }
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n"
                                    + answerFile + " not equal");
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

    private XMLRuleFilter createXMLFilterByFileName(String name)
            throws Exception
    {
        XMLRuleFilter f = new XMLRuleFilter();
        String configXml = XmlFilterConfigParser.nullConfigXml;

        if ("sampleExWsChars.xml".equalsIgnoreCase(name))
        {
            configXml = XmlFilterConfigParser.toXml("Z",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("samplePhCon.xml".equalsIgnoreCase(name))
        {
            JSONArray embTags = new JSONArray(
                    "[{tagName : \"b\", itemid : 1, enable : true, attributes : []}, {tagName : \"i\", itemid : 2, enable : true, attributes : []}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_ADJACENT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(), embTags,
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray());
        }
        else if ("samplePhTrim.xml".equalsIgnoreCase(name))
        {
            JSONArray embTags = new JSONArray(
                    "[{tagName : \"b\", itemid : 1, enable : true, attributes : []}, {tagName : \"i\", itemid : 2, enable : true, attributes : []}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DO,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(), embTags,
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray());
        }
        else if ("sampleNonAsciiChar.xml".equalsIgnoreCase(name))
        {
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_ENTITY,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleWhiteSpace.xml".equalsIgnoreCase(name))
        {
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_COLLAPSE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleEmptyTag.xml".equalsIgnoreCase(name))
        {
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_OPEN, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleSid.xml".equalsIgnoreCase(name))
        {
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "aaa", "id", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleGenerateLan.xml".equalsIgnoreCase(name))
        {
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "True", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("samplePreserveWsTags.xml".equalsIgnoreCase(name))
        {
            // set white space handle as WHITESPACE_HANDLE_COLLAPSE first
            JSONArray preserveWsTags = new JSONArray(
                    "[{tagName : \"bbb\", itemid : 1, enable : true, attributes : []}, {tagName : \"ccc\", itemid : 2, enable : true, attributes : []}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_COLLAPSE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", preserveWsTags,
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleTransAttTags.xml".equalsIgnoreCase(name))
        {
            JSONArray transAttTags = new JSONArray(
                    "[{tagName : \"ccc\", itemid : 1, enable : true, attributes : [], transAttributes : [{aName : \"sampleAttribute\", itemid : 1}], transAttrSegRule : 2}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), transAttTags, new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleContentIncTags.xml".equalsIgnoreCase(name))
        {
            JSONArray contentIncTags = new JSONArray(
                    "[{tagName : \"bbb\", inclType : 2, itemid : 1, enable : true, attributes : [{aName : \"translate\", aOp : \"equal\", aValue : \"no\", itemid : 2}]}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), contentIncTags,
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleCDATAPostTags.xml".equalsIgnoreCase(name))
        {
            // do not extract CDATA
            JSONArray CDATAPostTags = new JSONArray(
                    "[{aName : \"nocdata\", itemid : 1, enable : true, translatable : false, postFilterId: -2, postFilterTableName : \"\", cdataConditions : [{itemid : 2, aType : \"cdatacontent\", aOp : \"match\", aValue : \".*\"}]}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    CDATAPostTags, new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray());
        }
        else if ("sampleEntities.xml".equalsIgnoreCase(name))
        {
            JSONArray entities = new JSONArray(
                    "[{aName : \"copy\", itemid : 1, saveAs : 1, entityCode : 169,  enable : true, entityType : 0}]");
            configXml = XmlFilterConfigParser
                    .toXml("", XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                            XmlFilterConfigParser.PH_TRIM_DONOT,
                            XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                            XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                            XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "",
                            "-1", "", "-1", "", "", "False", "False",
                            new JSONArray(), new JSONArray(), new JSONArray(),
                            new JSONArray(), new JSONArray(), entities,
                            new JSONArray(), new JSONArray(), new JSONArray(),
                            new JSONArray());
        }
        else if ("samplePI.xml".equalsIgnoreCase(name))
        {
            JSONArray pis = new JSONArray(
                    "[{aName : \"mypi\", itemid : 1, handleType : 1, enable : true}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_DONOT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), pis, new JSONArray(),
                    new JSONArray(), new JSONArray());
        }
        else if ("sampleInternalTag.xml".equalsIgnoreCase(name))
        {
            JSONArray internalTag = new JSONArray(
                    "[{tagName:\"aaa\", itemid:1, enable:true, attributes:[{aName:\"internal\", aValue:\"yes\", aOp:\"equal\", itemid:2}]}]");
            configXml = XmlFilterConfigParser.toXml("",
                    XmlFilterConfigParser.PH_CONSOLIDATE_ADJACENT,
                    XmlFilterConfigParser.PH_TRIM_DONOT,
                    XmlFilterConfigParser.NON_ASCII_AS_CHARACTER,
                    XmlFilterConfigParser.WHITESPACE_HANDLE_PRESERVE,
                    XmlFilterConfigParser.EMPTY_TAG_FORMAT_CLOSE, "", "-1", "",
                    "-1", "", "", "False", "False", new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    new JSONArray(), new JSONArray(), new JSONArray(),
                    internalTag, new JSONArray(), new JSONArray());
        }

        f.setConfigXml(configXml);

        return f;
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
                { "testExtractor", "sample.xml", "sample.txt", "sample.xml" },
                { "testExtractorWithFilter", "sampleExWsChars.xml",
                        "sampleExWsChars.txt", "sampleExWsChars.xml" },
                { "testExtractorWithFilter", "samplePhCon.xml",
                        "samplePhCon.txt", "samplePhCon.xml" },
                { "testExtractorWithFilter", "samplePhTrim.xml",
                        "samplePhTrim.txt", "samplePhTrim.xml" },
                { "testExtractorWithFilter", "sampleNonAsciiChar.xml",
                        "sampleNonAsciiChar.txt", "sampleNonAsciiChar.xml" },
                { "testExtractorWithFilter", "sampleWhiteSpace.xml",
                        "sampleWhiteSpace.txt", "sampleWhiteSpace.xml" },
                { "testExtractorWithFilter", "sampleEmptyTag.xml",
                        "sampleEmptyTag.txt", "sampleEmptyTag.xml" },
                { "testExtractorWithFilter", "sampleSid.xml", "sampleSid.txt",
                        "sampleSid.xml" },
                { "testExtractorWithFilter", "sampleGenerateLan.xml",
                        "sampleGenerateLan.txt", "sampleGenerateLan.xml" },
                { "testExtractorWithFilter", "samplePreserveWsTags.xml",
                        "samplePreserveWsTags.txt", "samplePreserveWsTags.xml" },
                { "testExtractorWithFilter", "sampleTransAttTags.xml",
                        "sampleTransAttTags.txt", "sampleTransAttTags.xml" },
                { "testExtractorWithFilter", "sampleContentIncTags.xml",
                        "sampleContentIncTags.txt", "sampleContentIncTags.xml" },
                { "testExtractorWithFilter", "sampleCDATAPostTags.xml",
                        "sampleCDATAPostTags.txt", "sampleCDATAPostTags.xml" },
                { "testExtractorWithFilter", "sampleEntities.xml",
                        "sampleEntities.txt", "sampleEntities.xml" },
                { "testExtractorWithFilter", "sampleInternalTag.xml",
                        "sampleInternalTag.txt", "sampleInternalTag.xml" },
                { "testExtractorWithFilter", "samplePI.xml", "samplePI.txt",
                        "samplePI.xml" } };

        return formFileSets(fileSet, ExtractorTest.class);
    }

    @Override
    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }

    @Override
    public Output doExtract(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }
}
