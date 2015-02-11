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

package com.globalsight.util.edit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.machineTranslation.MTHelper;

public class SegmentUtil
{
    private List TAGS;
    private String REGEX_HEAD = "<bpt[^>]*i=\"([^\"]*)\"[^>]*>&lt;span class={0}([^<]*)&gt;</bpt>";
    private String REGEX_ALL = "<bpt[^>]*i=\"{0}\"[^>]*>.*?</bpt>(.*?)<ept[^>]*i=\"{0}\"[^>]*>.*?</ept>";
    private String REGEX_ALL_2 = "<bpt[^>]*i=\"{0}\"[^>]*/>(.*?)<ept[^>]*i=\"{0}\"[^>]*/>";
    
    private String NOTCOUNT_TAG_1 = "&lt;" + XML_NOTCOUNT_TAG + "&gt;";
    private String NOTCOUNT_TAG_2 = "&lt;/" + XML_NOTCOUNT_TAG + "&gt;";
    private String NOTCOUNT_TAG_3 = "<" + XML_NOTCOUNT_TAG + ">";
    private String NOTCOUNT_TAG_4 = "</" + XML_NOTCOUNT_TAG + ">";
    
    public static String XML_NOTCOUNT_TAG = "not_translate";
    
    private static Pattern HTML_TAG_PATTERN = 
        Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
    
    /**
     * Gets a new class.
     * 
     * <p>
     * <code>p_styles</code> is reads from <code>untranslatableWordCharacterStyles</code> of
     * <code>properties/WordExtractor.properties</code>. <code>p_styles</code> only used for 
     * word files.
     * 
     * @param p_styles
     */
    public SegmentUtil(String p_styles)
    {
        TAGS = new ArrayList();
        if (p_styles != null)
        {
            String[] styles = p_styles.split(",");

            for (int i = 0; i < styles.length; i++)
            {
                TAGS.add(styles[i]);
            }
        }
    }
    
    public List<String> getInternalWords(String src)
    {
        List<String> words = new ArrayList<String>();
        List<String> ids = new ArrayList<String>();
        ids.addAll(InternalTextUtil.getInternalIndex(src));

        for (int i = 0; i < ids.size(); i++)
        {
            String id = (String) ids.get(i);
            Object[] ob = { id };
            String regex = MessageFormat.format(REGEX_ALL, ob);
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(src);

            while (matcher.find())
            {
                String s = matcher.group(1);
                s = s.replaceAll("<[^/]*>.*?</.*?>", "");
                words.add(s);
            }
            
            String regex2 = MessageFormat.format(REGEX_ALL_2, ob);
            Pattern pattern2 = Pattern.compile(regex2, Pattern.DOTALL);
            Matcher matcher2 = pattern2.matcher(src);

            while (matcher2.find())
            {
                String s = matcher2.group(1);
                s = s.replaceAll("<[^/]*>.*?</.*?>", "");
                words.add(s);
            }
        }
        
        return words;
    }

    /**
     * Gets all words that not need to count and translate in the
     * <code>src</code>.
     * 
     * @param src
     * @return
     */
    public List getNotTranslateWords(String src)
    {
        List words = new ArrayList();
        List<String> ids = new ArrayList<String>();

        // doc.
        for (int i = 0; i < TAGS.size(); i++)
        {
            String tag = (String) TAGS.get(i);
            tag = tag.trim();
            
            if (tag.length() > 0)
            {
                Object[] ob = { tag };
                String regex = MessageFormat.format(REGEX_HEAD, ob);
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(src);
                while (matcher.find())
                {
                    ids.add(matcher.group(1));
                }
            }
        }

        for (int i = 0; i < ids.size(); i++)
        {
            String id = (String) ids.get(i);
            Object[] ob = { id };
            String regex = MessageFormat.format(REGEX_ALL, ob);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(src);

            while (matcher.find())
            {
                String s = matcher.group(1);
                s = s.replaceAll("<[^/]*>.*?</.*?>", "");
                words.add(s);
            }
        }

        // xml.
        // the src may be begin with <not_translate> or &lt;not_translate&gt;, make it same. 
        src = src.replaceAll(NOTCOUNT_TAG_3, NOTCOUNT_TAG_1);
        src = src.replaceAll(NOTCOUNT_TAG_4, NOTCOUNT_TAG_2);
        String regex = "\\&lt;{0}\\&gt;(.*?)\\&lt;/{0}&gt;";     
        regex = MessageFormat.format(regex, new String[]{XML_NOTCOUNT_TAG});
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        
        while (matcher.find())
        {
            String s = matcher.group(0);
            s = s.replaceAll(NOTCOUNT_TAG_1, NOTCOUNT_TAG_3);
            s = s.replaceAll(NOTCOUNT_TAG_2, NOTCOUNT_TAG_4);
            words.add(s);
        }
        
        return words;
    }

    public void setUntranslateStyle(String p_styles)
    {
        TAGS = new ArrayList();
        String[] styles = p_styles.split(",");

        for (int i = 0; i < styles.length; i++)
        {
            TAGS.add(styles[i]);
        }
    }
    
    public static String restoreSegment(String segment, String localCode) {
        String restoreStr = MTHelper.wrappText(segment, localCode);
        restoreStr = MTHelper.revertXlfSegment(restoreStr,localCode);
        
        return restoreStr;
    }

    /*
     * After parse, the entity will become de-entity code, so can't distinguish
     * the original code or by-parse code, so instead them before parse, and
     * recover them after parse.
     */
    public static String protectEntity(String str) {
        str = str.replaceAll("&lt;", "_xliff_lt_tag");
        str = str.replaceAll("&gt;", "_xliff_gt_tag");
        str = str.replaceAll("&amp;", "_xliff_amp_tag");
        str = str.replaceAll("&quot;", "_xliff_quot_tag");
        str = str.replaceAll("&apos;", "_xliff_apos_tag");
        str = str.replaceAll("&#xa;", "_xliff_xa_tag");
        str = str.replaceAll("&#xd;", "_xliff_xd_tag");
        str = str.replaceAll("&#x9;", "_xliff_x9_tag");
        
        return str;
    }
    
    /*
     * Some letter is protected in the AbstractExtractor for can't be
     * transformed when xml parse, so after parse ,need to recover them to the
     * original letter
     */
    public static String restoreEntity(String str)
    {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("&lt;");
        arrayList.add("&gt;");
        arrayList.add("&amp;");
        arrayList.add("&quot;");
        arrayList.add("&apos;");
        arrayList.add("&#xa;");
        arrayList.add("&#xd;");
        arrayList.add("&#x9;");

        return restoreEntity(str, arrayList);
    }

    public static String restoreEntity(String str, ArrayList arrayList)
    {
        if (arrayList.contains("&lt;"))
        {
            str = str.replaceAll("_xliff_lt_tag", "&lt;");
        }

        if (arrayList.contains("&gt;"))
        {
            str = str.replaceAll("_xliff_gt_tag", "&gt;");
        }

        if (arrayList.contains("&amp;"))
        {
            str = str.replaceAll("_xliff_amp_tag", "&amp;");
        }

        if (arrayList.contains("&quot;"))
        {
            str = str.replaceAll("_xliff_quot_tag", "&quot;");
        }

        if (arrayList.contains("&apos;"))
        {
            str = str.replaceAll("_xliff_apos_tag", "&apos;");
        }

        if (arrayList.contains("&#xa;"))
        {
            str = str.replaceAll("_xliff_xa_tag", "&#xa;");
        }
        
        if (arrayList.contains("&#xd;"))
        {
            str = str.replaceAll("_xliff_xd_tag", "&#xd;");
        }
        
        if (arrayList.contains("&#x9;"))
        {
            str = str.replaceAll("_xliff_x9_tag", "&#x9;");
        }

        return str;
    }
    
    /*
     * only re-entity such below code
     */
    public static String protectEntity2(String str) {
        str = str.replaceAll("&amp;", "&amp;amp;");
        str = str.replaceAll("&quot;", "&amp;quot;");
        str = str.replaceAll("&apos;", "&amp;apos;");
        str = str.replaceAll("&#xd;", "&amp;#xd;");
        str = str.replaceAll("&#x9;", "&amp;#x9;");
        str = str.replaceAll("&#xa;", "&amp;#xa;");
        str = str.replaceAll("&lt;", "&amp;lt;");
        str = str.replaceAll("&gt;", "&amp;gt;");
        
        return str;
    }
    
    public static String protectInvalidUnicodeChar(String str)
    {
        str = str.replaceAll("&#x100088;", "<gs_xml_placeholder type=\"InvalidUnicodeChar\" value=\"100088\"/>");
        return str;
    }
    
    public static String restoreInvalidUnicodeChar(String str)
    {
        str = str.replaceAll("<gs_xml_placeholder type=\"InvalidUnicodeChar\" value=\"100088\"/>", "&#x100088;");
        return str;
    }
    
    /**
     * Replace the HTML tag with place holder in segment node
     */
    public static void replaceHtmltagWithPH(SegmentNode p_node){
        String segment = p_node.getSegment();
        p_node.setSegment(replaceHtmltagWithPH(segment));
    }
    
    /**
     * Replace the HTML tag with place holder in segment
     * 
     * @param p_str
     *            segment string
     */
    public static String replaceHtmltagWithPH(String p_str)
    {
        String result = p_str.replace("&lt;", "<").replace("&gt;", ">");
        Matcher m = HTML_TAG_PATTERN.matcher(result);
        int index = 1;
        String htmlTag;

        while (m.find())
        {
            htmlTag = m.group(0);
            String replace = handleInlineTag(htmlTag, index);
            result = StringUtils.replace(result, htmlTag, replace, 1);
            if (result.contains(replace))
            {
                index++;
            }
        }

        return result;
    }

    /**
     * Transfer tags into inline tags with &lt;ph&gt;
     * 
     * @param p_str
     *            the String which need protected/inline
     * @param p_index
     *            the place holder index
     */
    private static String handleInlineTag(String p_str, int p_index)
    {
        String tagType = "phOfGS";
        StringBuffer stuff = new StringBuffer();
        XmlEntities m_xmlEncoder = new XmlEntities();
        stuff.append("<ph type=\"" + tagType + "\" id=\"" + p_index + "\""
                + " x=\"" + p_index++ + "\">");
        stuff.append(m_xmlEncoder.encodeStringBasic(p_str));
        stuff.append("</ph>");
        return stuff.toString();
    }
}
