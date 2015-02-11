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

package com.globalsight.everest.edit.offline.page.terminology;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.edit.offline.page.TerminologyHelp;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Hitlist;
import com.globalsight.terminology.exporter.HtmlWriter;
import com.globalsight.util.UTC;
import com.globalsight.util.XmlParser;

/**
 * <code>HtmlTermHelp</code> is a concrete class for <code>TermHelp</code>,
 * using Html pattern to format terminology.
 */
public class HtmlTermHelp extends TermHelp implements TerminologyHelp
{
    private static final String END = "\r\n</BODY>\r\n</HTML>";
    private static final String TOP_START = "<span style=\"display:none\">";
    private static final String TOP_END = "</span>";

//    private StringBuilder concepts = new StringBuilder();

    /**
     * The message pattern of conceptGrp. The following parameters are needed.
     * <br>
     * 1. concept id<br>
     * 2. origination <br>
     * 3. date <br>
     * 4. languageGrp
     * 
     * <p>
     * The pattern is
     * 
     * <pre>
     *     &lt;DIV class=&quot;vconceptGrp&quot;&gt;
     *     &lt;SPAN class=&quot;vfakeConceptGrp&quot;&gt;
     *       &lt;SPAN class=&quot;vconceptlabel&quot;&gt;Entry&lt;/SPAN&gt;
     *       &lt;SPAN class=&quot;vconcept&quot;&gt;{0}&lt;/SPAN&gt;
     *     &lt;/SPAN&gt;
     *     &lt;SPAN class=&quot;vtransacGrp&quot; type=&quot;origination&quot; author=&quot;{1}&quot; date=&quot;{2}&quot;&gt;
     *       &lt;SPAN CLASS=&quot;vtransaclabel&quot;&gt;Creation Date&lt;/SPAN&gt;
     *       &lt;SPAN CLASS=&quot;vtransacvalue&quot;&gt;{2} ({1})&lt;/SPAN&gt;
     *     &lt;/SPAN&gt;
     *     {3}
     *     &lt;/DIV&gt;
     * <HR width="100%"/>
     * </pre>
     */
    private static final String CONCEPT_GRP = "<DIV class=\"vconceptGrp\">\r\n"
            + "<SPAN class=\"vfakeConceptGrp\">"
            + "<SPAN class=\"vconceptlabel\">Entry</SPAN>"
            + "<SPAN class=\"vconcept\">{0}</SPAN>"
            + "</SPAN><SPAN class=\"vtransacGrp\" "
            + "type=\"origination\" author=\"{1}\" date=\"{2}\">"
            + "<SPAN CLASS=\"vtransaclabel\">Creation Date</SPAN>"
            + "<SPAN CLASS=\"vtransacvalue\">{2} ({1})</SPAN></SPAN>"
            + "{3}</DIV> <HR width=\"100%\"/>";

    /**
     * The message pattern of languageGrp. The following parameters are needed.<br>
     * 
     * 1. language, "English" for example<br>
     * 2. country, "US" for example <br>
     * 3. terminology<br>
     * 
     * <p>
     * The pattern is
     * 
     * <pre>
     *     &lt;DIV class=&quot;vlanguageGrp&quot;&gt;
     *       &lt;SPAN class=&quot;vfakeLanguageGrp&quot;&gt;
     *         &lt;SPAN class=&quot;vlanguagelabel&quot;&gt;Language&lt;/SPAN&gt;
     *         &lt;SPAN class=&quot;vlanguage&quot; unselectable=&quot;on&quot; locale=&quot;{0}&quot;&gt;{1}&lt;/SPAN&gt;
     *       &lt;/SPAN&gt;
     *       &lt;DIV class=&quot;vtermGrp&quot;&gt;
     *         &lt;DIV class=&quot;vfakeTermGrp&quot;&gt;
     *           &lt;SPAN class=&quot;vtermlabel&quot;&gt;Term&lt;/SPAN&gt;
     *           &lt;SPAN class=&quot;vterm&quot;&gt;{2}&lt;/SPAN&gt;
     *         &lt;/DIV&gt;
     *       &lt;/DIV&gt;
     *     &lt;/DIV&gt;
     * </pre>
     */
    private static final String LANGUAGE_GRP = "<DIV class=\"vlanguageGrp\">"
            + "<SPAN class=\"vfakeLanguageGrp\">"
            + "<SPAN class=\"vlanguagelabel\">Language</SPAN>"
            + "<SPAN class=\"vlanguage\" unselectable=\"on\" locale=\"{0}\">{1}</SPAN>"
            + "</SPAN>" + "<DIV class=\"vtermGrp\">"
            + "<DIV class=\"vfakeTermGrp\">"
            + "<SPAN class=\"vtermlabel\">Term</SPAN>"
            + "<SPAN class=\"vterm\">{2}</SPAN>"
            + "<DIV class=\"vfieldGrp\">"
            + "<SPAN class=\"vfieldlabel\">{4}</SPAN><br/>"
            + "<SPAN class=\"vfieldvalue\"><P>{3}</P></SPAN></DIV>" + "</DIV>" + "</DIV>"
            + "</DIV>";

    /**
     * Generates the content between the body tag.
     * <p>
     * The content will be formated as xml, so the content was wraped before
     * formating and unwraped after formating.
     */
    @Override
    public String getContent()
    {
        StringBuilder concepts = new StringBuilder();
        concepts.append(TOP_START);
        
        for (Hitlist.Hit src : getSortedSource())
        {
            String languageGrp = getLanguageGrp(src, getEntries().get(src), src.getDescXML());
            String concept = MessageFormat.format(CONCEPT_GRP, src
                    .getConceptId(), getUserName(), UTC.valueOf(new Date()),
                    languageGrp);
            concepts.append(concept);
        }

        concepts.append(TOP_END);

        String result = XmlUtil.format(concepts.toString());
        int first = result.indexOf(TOP_START) + TOP_START.length();
        int end = result.indexOf(TOP_END);
        if(end > first)
        {
            concepts.append(result.substring(first, end));
        }

        return concepts.toString();
    }

    @Override
    public String getEnd()
    {
        return END;
    }

    @Override
    public String getHead()
    {
        StringBuilder head = new StringBuilder();
        head.append(HtmlWriter.HTML_HEADER_START);
        head.append(HtmlWriter.HTML_TITLE_START);
        head.append("Terminology printed ").append(UTC.valueOf(new Date()));
        head.append(HtmlWriter.HTML_TITLE_END);
        head.append(HtmlWriter.HTML_STYLESHEET);
        head.append(HtmlWriter.HTML_END_HEADER_BODY_START);

        return head.toString();
    }

    @Override
    public String getConcept(String languageGrp, long conceptId)
    {
        return null;
    }

    public HtmlTermHelp()
    {
        super();
        setFormat(false);
    }

    @Override
    public String getLanguage(Locale locale, String terminology, String xml)
    {
        ArrayList fieldList = new ArrayList();
        ArrayList valueList = new ArrayList();
        String htmlCotent = "";
        
        if(xml == null || xml.trim().equals("")) {
            htmlCotent = "<SPAN class=\"vfieldlabel\"></SPAN><br/>" 
                         + "<SPAN class=\"vfieldvalue\"><P></P></SPAN>";
        }
        else {
            try{
                xml = "<desc>" + xml + "</desc>";
                Entry entry = new Entry(xml);
                Document dom = entry.getDom();
                Element root = dom.getRootElement();
                List descrip = root.selectNodes("//desc/descripGrp/descrip");
                
                for(int i = 0; i < descrip.size(); i++) {
                    Element content = (Element) descrip.get(i);
                    String field = content.attributeValue("type");
                    String xmlContent = content.getStringValue();
                    htmlCotent =  htmlCotent 
                         + "<SPAN class=\"vfieldlabel\">" + field + "</SPAN><br/>" 
                         + "<SPAN class=\"vfieldvalue\"><P>" + xmlContent + "</P></SPAN>";
                }
            }
            catch(Exception e) {}
        }
        
        String lanString = "<DIV class=\"vlanguageGrp\">"
            + "<SPAN class=\"vfakeLanguageGrp\">"
            + "<SPAN class=\"vlanguagelabel\">Language</SPAN>"
            + "<SPAN class=\"vlanguage\" unselectable=\"on\" locale=\"" 
            + locale.getDisplayLanguage(LOCALE) + "\">" 
            + locale.getCountry().toUpperCase()+ "</SPAN>"
            + "</SPAN>" + "<DIV class=\"vtermGrp\">"
            + "<DIV class=\"vfakeTermGrp\">"
            + "<SPAN class=\"vtermlabel\">Term</SPAN>"
            + "<SPAN class=\"vterm\">" + terminology + "</SPAN>"
            + "<DIV class=\"vfieldGrp\">"
            + htmlCotent
            + "</DIV></DIV></DIV></DIV>";
        
        return lanString;
        /*
        return MessageFormat.format(LANGUAGE_GRP, locale
                .getDisplayLanguage(LOCALE), locale.getCountry().toUpperCase(),
                terminology, xmlContent, field);
                */
    }
}
