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
package com.globalsight.everest.segmentationhelper;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Iterator;
import org.dom4j.io.SAXReader;


/**
 * XmlLoader is responssible to loader a segmentation rule file writen in xml
 * format, and transfer it into SegmentationRule datastructure.
 * 
 * @author holden.cai
 * 
 */

public class XmlLoader
{

    static private final Logger CATEGORY = Logger
            .getLogger(XmlLoader.class);

    /**
     * Transfer xml file into Document.
     * 
     * @param file
     */
    private static Document parserWithSAX(File file) throws Exception
    {
        SAXReader xmlReader = new SAXReader();
        Document doc = null;
        try
        {
            doc = xmlReader.read(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return doc;
    }

    public static SegmentationRule parseSegmentationRule(String p_xmlText) throws Exception
    {
        Document doc = parseWithSAX(p_xmlText);
        Element root = doc.getRootElement();
        SegmentationRule segmentationRule = new SegmentationRule();
        SrxHeader header = new SrxHeader();
        HashMap<String, String> formathandle = new HashMap<String, String>();
        HashMap<String, ArrayList<Rule>> langrules = new HashMap<String, ArrayList<Rule>>();
        ArrayList<LanguageMap> langruleMap = new ArrayList<LanguageMap>();

        segmentationRule.setRootName(root.getQualifiedName());
        // Now it only supports SRX2.0.
        segmentationRule.setVersion(root.attributeValue("version"));

        Element headerElement = root.element("header");
        Element bodyElement = root.element("body");
        Element maprulesElement = bodyElement.element("maprules");
        // to get SRX header informations
        if (headerElement != null)
        {
            // option segmentsubflows
            String segsub = headerElement.attributeValue("segmentsubflows");
            if (segsub == null)
            {
                // Take default value.
                header.isSegmentsubflows(true);
            }
            else
            {
                header.isSegmentsubflows(!segsub.equalsIgnoreCase("no"));
            }

            // option cascade
            String cascade = headerElement.attributeValue("cascade");
            if (cascade == null)
            {
                header.isCascade(false);
            }
            else
            {
                header.isCascade(!cascade.equalsIgnoreCase("no"));
            }

            // elements : formathandle
            List formatList = headerElement.elements("formathandle");
            Iterator formatIter = formatList != null ? formatList.iterator() : (new ArrayList()).iterator();
            // If the header does not contain formathandle elements
            // we use the default values
            if (!formatIter.hasNext())
            {
                formathandle.put("start", "no");
                formathandle.put("end", "yes");
                formathandle.put("isolated", "no");
            }
            // If the header contains formathandle elements
            // we use the values specified by formathandle elements
            else
            {
                while (formatIter.hasNext())
                {
                    Element formatElement = (Element) formatIter.next();
                    String type = formatElement.attributeValue("type");
                    String include = formatElement.attributeValue("include");
                    formathandle.put(type, include);
                }
            }
            header.setFormatHandle(formathandle);
            
            // okpsrx extension
            Element okpOptions = headerElement.element("options");
            Element okpSample = headerElement.element("sample");
            Element okpRangeRule = headerElement.element("rangeRule");
            
            if (okpOptions != null)
            {
                String oneSegmentIncludesAll = okpOptions.attributeValue("oneSegmentIncludesAll");
                String trimLeadingWs = okpOptions.attributeValue("trimLeadingWhitespaces");
                String trimTrailingWs = okpOptions.attributeValue("trimTrailingWhitespaces");
                
                header.setOneSegmentIncludesAll("yes".equalsIgnoreCase(oneSegmentIncludesAll));
                header.setTrimLeadingWhitespaces("yes".equalsIgnoreCase(trimLeadingWs));
                header.setTrimTrailingWhitespaces("yes".equalsIgnoreCase(trimTrailingWs));
            }
            
            if (okpSample != null)
            {
                String language = okpSample.attributeValue("language");
                String useMappedRules = okpSample.attributeValue("useMappedRules");
                String sample = okpSample.getText();
                
                header.setSample(sample);
                header.setSampleLanguage(language);
                header.setUseMappedRulesForSample(!"no".equalsIgnoreCase(useMappedRules));
            }
            
            if (okpRangeRule != null)
            {
                String rangeRule = okpRangeRule.getText();
                header.setRangeRule(rangeRule);
            }
        }

        if (bodyElement != null)
        {
            Element languagerulesElement = bodyElement.element("languagerules");
            if (languagerulesElement != null)
            {
                Iterator languageruleIter = languagerulesElement.elementIterator();
                while (languageruleIter.hasNext())
                {
                    Element languageruleElement = (Element) languageruleIter.next();
                    String languageName = languageruleElement.attributeValue("languagerulename");
                    Iterator ruleIter = languageruleElement.elementIterator();
                    ArrayList<Rule> rules = new ArrayList<Rule>();
                    while (ruleIter.hasNext())
                    {
                        Element ruleSub = (Element) ruleIter.next();
                        String breakvalue = ruleSub.attributeValue("break");
                        String beforebreak = ruleSub.elementText("beforebreak");
                        String afterbreak = ruleSub.elementText("afterbreak");
                        Rule rule = new Rule();
                        if (breakvalue == null)
                        {
                            // Take default value.
                            rule.isBreak(true);
                        }
                        else
                        {
                            if (breakvalue.equalsIgnoreCase("no"))
                            {
                                rule.isBreak(false);
                            }
                            else
                            {
                                rule.isBreak(true);
                            }
                        }

                        // System.out.println(rule.getBreak());
                        rule.setAfterBreak(afterbreak);
                        rule.setBeforeBreak(beforebreak);
                        rules.add(rule);
                    }
                    langrules.put(languageName, rules);
                }
            }// end languageruleElement
        }// end bodyElement

        if (maprulesElement != null)
        {
            Iterator languagemapIter = maprulesElement.elementIterator();
            while (languagemapIter.hasNext())
            {
                Element languagemapElement = (Element) languagemapIter.next();
                String languagepattern = languagemapElement.attributeValue("languagepattern");
                String languagerulename = languagemapElement.attributeValue("languagerulename");
                LanguageMap langMap = new LanguageMap();
                langMap.setLanguagePattern(languagepattern);
                langMap.setLanguageruleName(languagerulename);
                langruleMap.add(langMap);
            }
        }

        segmentationRule.setHeader(header);
        segmentationRule.setLanguageMap(langruleMap);
        segmentationRule.setRules(langrules);
        return segmentationRule;
    }

    /**
     * Transfer xml text into Document.
     * 
     * @param xmltext
     */
    private static Document parseWithSAX(String xmltext) throws Exception
    {
        Document doc = null;
        StringReader sr = new StringReader(xmltext);
        SAXReader xmlReader = new SAXReader();
        try
        {
            doc = xmlReader.read(sr);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        return doc;
    }
}
