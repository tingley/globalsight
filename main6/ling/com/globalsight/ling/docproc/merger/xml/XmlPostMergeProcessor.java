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
package com.globalsight.ling.docproc.merger.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterChecker;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;

/**
 * This class post processes a merged XML document.
 */
public class XmlPostMergeProcessor implements PostMergeProcessor
{
    private static Logger c_category = Logger
            .getLogger(XmlPostMergeProcessor.class.getName());

    private boolean generateLang = false;
    private boolean generateEncoding = false;
    private Locale locale = null;
    private XmlFilterHelper xmlFilterHelper = null;

    public void setGenerateLang(boolean generateLang)
    {
        this.generateLang = generateLang;
    }

    public void setGenerateEncoding(boolean generateEncoding)
    {
        this.generateEncoding = generateEncoding;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    public void setXmlFilterHelper(XmlFilterHelper p_xmlFilterHelper)
    {
        this.xmlFilterHelper = p_xmlFilterHelper;
    }

    private String localeToString(Locale locale)
    {
        String st = locale.toString();

        if (st != null)
        {
            return st.replaceAll("_", "-");
        }
        else
        {
            return null;
        }
    }

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String,
     *      java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
            throws DiplomatMergerException
    {
        String processed = null;
        try
        {
            String content = p_content;
            // add xml:lang first if needed
            if (generateLang && locale != null)
            {
                String langStr = "xml:lang=\"" + localeToString(locale) + "\"";

                // add xml:lang to existed nodes
                try
                {
                    String langRE = "<[^<>]+(\\s+xml:lang\\s*=\\s*['\"][^'\"]*['\"])[^<>]*>";
                    int matchFlags = Pattern.MULTILINE;
                    Pattern pattern = Pattern.compile(langRE, matchFlags);
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find())
                    {
                        StringBuffer buffer = new StringBuffer(content.length());
                        String part0 = null;
                        String part2 = null;
                        int lastEnd = 0;
                        // replace all exists xml:lang into new one
                        do
                        {
                            int start = matcher.start();
                            int end = matcher.end();
                            part2 = content.substring(end);
                            part0 = content.substring(lastEnd, start);
                            lastEnd = end;

                            String sub = matcher.group();
                            boolean isExclude = false;
                            int indexOfSpace = sub.indexOf(" ");
                            if (indexOfSpace != -1)
                            {
                                String tagName = sub.substring(1, indexOfSpace);
                                String cloaseTagRE = "<.+/\\s*>";
                                Pattern closeTagPattern = Pattern.compile(
                                        cloaseTagRE, Pattern.MULTILINE
                                                | Pattern.DOTALL);
                                Matcher closeTagMatcher = closeTagPattern
                                        .matcher(sub);
                                boolean isClosedTag = closeTagMatcher.find();
                                String tempXml = isClosedTag ? sub : sub + "</"
                                        + tagName + ">";
                                Node rootNode = getRootNode(tempXml);

                                isExclude = (xmlFilterHelper != null) ? xmlFilterHelper
                                        .isExclude(rootNode) : false;
                            }

                            String subNew = isExclude ? sub : sub.replaceAll(
                                    "(xml:lang\\s*=\\s*['\"][^'\"]*['\"])",
                                    langStr);
                            buffer.append(part0);
                            buffer.append(subNew);
                        } while (matcher.find());

                        buffer.append(part2);
                        content = buffer.toString();
                    }
                }
                catch (Exception e)
                {
                    c_category
                            .error("Generate xml:lang to root element failed. ");
                    throw new DiplomatMergerException(e);
                }

                // add xml:lang to root
                try
                {
                    Node rootNode = getRootNode(content);

                    if (rootNode != null)
                    {
                        String rootName = rootNode.getNodeName();
                        RegExMatchInterface rootMatch = RegEx.matchSubstring(
                                content, ".*(<" + rootName + "[^>]*>).*");

                        if (rootMatch != null)
                        {
                            int rootSt = rootMatch.beginOffset(1);
                            int rootEn = rootMatch.endOffset(1);
                            StringBuffer rootStr = new StringBuffer(
                                    rootMatch.group(1));
                            if (rootStr.indexOf(" xml:lang=") == -1)
                            {
                                int insertIndex = ("<" + rootName).length();
                                String newRootStr = rootStr.insert(insertIndex,
                                        " " + langStr).toString();
                                content = content.substring(0, rootSt)
                                        + newRootStr
                                        + content.substring(rootEn);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    c_category
                            .error("Generate xml:lang to root element failed. ");
                    throw new DiplomatMergerException(e);
                }
            }

            // add encoding if needed.
            String encodingStr = " encoding=\"" + p_IanaEncoding + "\"";
            RegExMatchInterface match = RegEx
                    .matchSubstring(
                            content,
                            "<\\?xml(\\s+version\\s*=\\s*['\"][^'\"]*['\"])(\\s+encoding\\s*=\\s*['\"][^'\"]*['\"])?([^>]*)\\?>");

            // add XML declaration if there isn't one.
            if (match == null)
            {
                processed = content;
            }
            else
            {
                // overwrite the encoding declaration
                String version = match.group(1);
                String encoding = match.group(2);
                String rest = match.group(3);
                String replacement = (encoding == null) ? "<?xml" + version
                        + rest + "?>" : "<?xml" + version + encodingStr + rest
                        + "?>";

                processed = RegEx.substituteAll(content, "<\\?xml\\s[^>]+\\?>",
                        replacement);
            }
        }
        catch (RegExException e)
        {
            // shouldn't happen
            c_category.error(e.getMessage(), e);
        }
        return processed;
    }

    private Node getRootNode(String p_text) throws SAXException, IOException
    {
        String content = new StringBuffer(p_text).toString();
        content = content.replaceAll("&[\\S]+;", "&gt;");
        XmlFilterChecker checker = new XmlFilterChecker();
        DOMParser parser = new DOMParser();
        parser.setEntityResolver(checker);
        parser.setErrorHandler(checker);

        parser.parse(new InputSource(new StringReader(content)));

        Node doc = parser.getDocument();
        NodeList nodes = doc.getChildNodes();
        Node rootNode = null;
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                rootNode = node;
            }
        }
        return rootNode;
    }

}
