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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;

public class XmlExtractorTest extends BaseExtractorTestClass
{
    private String defaultEncoding = "UTF-8";
    private String caseJsonXmlFile = null;

    private XmlExtractor extractor;
    private XMLRuleFilter filter;

    @Before
    public void init()
    {
        // out put some message for verify
        String userDir = System.getProperty("user.dir");
        System.out.println("user.dir : " + userDir);
        System.out.println(XmlExtractorTest.class.getResource("/"));
        System.out.println(XmlExtractorTest.class.getResource("/properties/schemarules.rng"));
        filter = initXmlFilter();
        caseJsonXmlFile = FileUtil.getResourcePath(XmlExtractorTest.class, "files/caseJsonSrc.xml");

        initExtractor();
    }

    @Test
    public void testXmContainsJsonlExtractor() throws Exception
    {
        Output output = doExtract(caseJsonXmlFile, filter);

        String gxml = DiplomatWriter.WriteXML(output);
        System.out.println(gxml);
        assertTrue(gxml
                .contains("<translatable blockId=\"1\" datatype=\"json\">JSON data here</translatable>"));
    }

    private Output doExtract(String xmlFile, XMLRuleFilter filter) throws FileNotFoundException,
            IOException
    {
        EFInputData input = createInput(xmlFile, defaultEncoding);
        Output output = new Output();

        AbstractExtractor extractor = new XmlExtractor();
        extractor.init(input, output);
        extractor.setMainFilter(filter);
        extractor.loadRules();
        extractor.extract();

        return output;
    }

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

    private XMLRuleFilter initXmlFilter()
    {
        XMLRuleFilter xmlFilter = new XMLRuleFilter();
        xmlFilter.setCompanyId(1000);
        String configXml = "<xmlFilterConfig><extendedWhitespaceChars></extendedWhitespaceChars><phConsolidation>1</phConsolidation><phTrim>1</phTrim><nonasciiAs>1</nonasciiAs><whitespaceHandleMode>2</whitespaceHandleMode><emptyTagFormat>0</emptyTagFormat><entityHandleMode>1</entityHandleMode><elementPostFilter>filter_json</elementPostFilter><elementPostFilterId>1</elementPostFilterId><cdataPostFilter>filter_json</cdataPostFilter><cdataPostFilterId>1</cdataPostFilterId><sidTagName></sidTagName><sidAttrName></sidAttrName><isCheckWellFormed>false</isCheckWellFormed><isGerateLangInfo>false</isGerateLangInfo><whitespacePreserveTags></whitespacePreserveTags><embeddedTags></embeddedTags><transAttrTags></transAttrTags><contentInclTags></contentInclTags><cdataPostfilterTags></cdataPostfilterTags><entities></entities><processIns></processIns><internalTag></internalTag><srcCmtXmlComment></srcCmtXmlComment><srcCmtXmlTag></srcCmtXmlTag></xmlFilterConfig>";
        xmlFilter.setConfigXml(configXml);
        xmlFilter.setId(4);
        xmlFilter.setFilterName("Xml Filter");
        xmlFilter.setUseXmlRule(true);
        xmlFilter.setXmlRuleId(-1);
        return xmlFilter;
    }

    @After
    public void clear()
    {
        extractor = null;
    }

    @Override
    public AbstractExtractor initExtractor()
    {
        extractor = new XmlExtractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        return null;
    }

    @Override
    public String getFileContent(File file, AbstractExtractor extractor, String encoding)
            throws Exception
    {
        return null;
    }

    @Override
    public Output doExtract(File file, AbstractExtractor extractor, String encoding)
            throws Exception
    {
        return null;
    }

    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(XmlExtractorTest.class);
    }
}
