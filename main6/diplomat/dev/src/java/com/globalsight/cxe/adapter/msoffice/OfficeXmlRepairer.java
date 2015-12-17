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
package com.globalsight.cxe.adapter.msoffice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.util.StringUtil;

/**
 * This is a util class that used to repair exporting office 2010 files.
 */
public class OfficeXmlRepairer
{
    static private final Logger logger = Logger
            .getLogger(OfficeXmlRepairer.class);

    private static String s_wpStartTag = "<w:p ";
    private static String s_wpEndTag = "</w:p>";
    private static String s_wpPrStartTag = "<w:pPr";
    private static String s_wpPrEndTag = "</w:pPr>";
    private static String s_wbidiTag = "<w:bidi/>";
    private static String s_gtMark = ">";

    private static String s_wrStartTag = "<w:r ";
    private static String s_wrEndTag = "</w:r>";
    private static String s_wrPrStartTag = "<w:rPr";
    private static String s_wrPrEndTag = "</w:rPr>";
    private static String s_wrtlTag = "<w:rtl/>";

    private static String s_arStartTag = "<a:r ";
    private static String s_arEndTag = "</a:r>";
    private static String s_arPrStartTag = "<a:rPr";
    private static String s_arPrEndTag = "</a:rPr>";
    private static String s_artlTag = "<a:rtl/>";

    private static String s_apStartTag = "<a:p ";
    private static String s_apStartTag2 = "<a:p>";
    private static String s_apEndTag = "</a:p>";
    private static String s_apPrRtl = "rtl=\"1\"";
    private static String s_apPrStartTag = "<a:pPr";

    private static String s_cellXfsStartTag = "<cellXfs ";
    private static String s_cellXfsEndTag = "</cellXfs>";
    private static String s_xfStartTag = "<xf ";
    private static String s_xfEndTag = "</xf>";

    public static void repair(String path)
    {
        try
        {
            List<OfficeRepairer> repairers = new ArrayList<OfficeRepairer>();
            repairers.add(new WordRepairer(path));
            repairers.add(new PptxRepairer(path));
            repairers.add(new ExcelRepairer(path));

            for (OfficeRepairer repairer : repairers)
            {
                repairer.repairFiles();
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public static String fixRtlLocale(String xmlContent, String locale)
    {

        if (xmlContent == null || xmlContent.length() == 0)
        {
            return xmlContent;
        }

        String lang = locale;
        if (locale != null)
        {
            lang = locale.replace("_", "-");
        }

        xmlContent = xmlContent.trim();

        if (xmlContent.endsWith("</w:document>"))
        {
            String result = fixRtlDocumentXml(xmlContent, lang);

            return result;
        }
        else if (xmlContent.endsWith("</w:ftr>")
                || xmlContent.endsWith("</w:hdr>")
                || xmlContent.endsWith("</w:comments>")
                || xmlContent.endsWith("</w:footnotes>")
                || xmlContent.endsWith("</w:endnotes>"))
        {
            String result = fixRtlDocumentXml(xmlContent, lang);

            return result;
        }
        else if (xmlContent.endsWith("</dgm:dataModel>"))
        {
            String result = fixRtlSlideXmlAP(xmlContent, lang);

            return result;
        }
        else if (xmlContent.contains("</p:sld>"))
        {
            return fixRtlSlideXmlAP(xmlContent, lang);
        }
        else if (xmlContent.contains("</styleSheet>"))
        {
            return fixRtlSheetStylesXml(xmlContent, lang);
        }

        return xmlContent;
    }

    /**
     * For Right to Left languages in office XML
     * 
     * @param xmlContent
     * @return
     */
    public static String fixRtlLocale(String xmlContent)
    {
        return fixRtlLocale(xmlContent, null);
    }

    private static String fixRtlDocumentXml(String xmlContent, String locale)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        String valueToAdd = s_wbidiTag;
        String startTag = s_wpStartTag;
        String endTag = s_wpEndTag;
        String rprStartTag = s_wpPrStartTag;
        String rprEndTag = s_wpPrEndTag;

        String rrr = addStyleTag(src, valueToAdd, startTag, endTag,
                rprStartTag, rprEndTag);

        return rrr;
    }

    private static String addStyleTag(StringBuffer src, String valueToAdd,
            String startTag, String endTag, String rprStartTag, String rprEndTag)
    {
        StringBuffer result = new StringBuffer();
        List<String> starts = new ArrayList<String>();
        starts.add(startTag);
        starts.add(startTag.trim() + s_gtMark);

        StringIndex si = StringIndex.getValueBetween(src, 0, starts, endTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            StringBuffer vsb = new StringBuffer();

            // add w:bidi for w:p in w:document XML
            if (v.contains(valueToAdd))
            {
                vsb.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0,
                        rprStartTag, s_gtMark);
                if (tempSi == null)
                {
                    int endIndex = 0;
                    if (!v.startsWith("<") && !v.trim().startsWith("<"))
                    {
                        // add the rest part of w:r or w:p like
                        // w:rsidRPr="0065076E">
                        String sss = v.substring(0, 1);
                        tempSi = StringIndex.getValueBetween(temp, 0, sss,
                                s_gtMark);
                        vsb.append(tempSi.allValue);

                        endIndex = tempSi.end + 1;
                    }
                    vsb.append(rprStartTag).append(s_gtMark);
                    vsb.append(valueToAdd).append(rprEndTag);
                    vsb.append(temp.substring(endIndex));
                }
                else
                {
                    vsb.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        vsb.append(tempV.substring(0, tempV.length() - 1));
                        vsb.append(s_gtMark);
                        vsb.append(valueToAdd);
                        vsb.append(rprEndTag);
                    }
                    else
                    {
                        vsb.append(tempV);
                        vsb.append(s_gtMark);
                        vsb.append(valueToAdd);
                    }
                    vsb.append(temp.substring(tempSi.end + 1));
                }
            }

            boolean needToAddRTL = needToAddDocRTL(true, vsb.toString(),
                    s_wrtlTag);
            if (needToAddRTL)
            {
                vsb = splitEnglishAndAddRtl(vsb);
                result.append(vsb);
            }
            else
            {
                result.append(vsb);
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, starts, endTag);
        }

        result.append(src);

        String rrr = result.toString();
        return rrr;
    }

    private static List<StringBuffer> splitEnglishWt(String src)
    {
        List<StringBuffer> splits = new ArrayList<StringBuffer>();

        Pattern p = Pattern.compile("(<w:t[^>]*>)(.*?)</w:t>");
        Matcher m = p.matcher(src);
        if (m.find())
        {
            int n = m.start();
            String before = src.substring(0, n) + m.group(1);
            String v = m.group(2);
            String after = src.substring(m.end());

            List<SplitString> rs = split(v);

            for (SplitString r : rs)
            {
                StringBuffer sb = new StringBuffer();

                if (r.isNeedRtl())
                {
                    StringBuffer temp = new StringBuffer(before);
                    StringIndex tempSi = StringIndex.getValueBetween(temp, 0,
                            s_wrPrStartTag, s_gtMark);
                    if (tempSi == null)
                    {
                        int endIndex = 0;
                        if (!before.startsWith("<")
                                && !before.trim().startsWith("<"))
                        {
                            // add the rest part of w:r or w:p like
                            // w:rsidRPr="0065076E">
                            String sss = before.substring(0, 1);
                            tempSi = StringIndex.getValueBetween(temp, 0, sss,
                                    s_gtMark);
                            sb.append(tempSi.allValue);

                            endIndex = tempSi.end + 1;
                        }
                        sb.append(s_wrPrStartTag).append(s_gtMark);
                        sb.append(s_wrtlTag).append(s_wrPrEndTag);
                        sb.append(temp.substring(endIndex));
                    }
                    else
                    {
                        sb.append(temp.substring(0, tempSi.start));
                        String tempV = tempSi.value;
                        if (tempV.endsWith("/"))
                        {
                            sb.append(tempV.substring(0, tempV.length() - 1));
                            sb.append(s_gtMark);
                            sb.append(s_wrtlTag);
                            sb.append(s_wrPrEndTag);
                        }
                        else
                        {
                            sb.append(tempV);
                            sb.append(s_gtMark);
                            sb.append(s_wrtlTag);
                        }
                        sb.append(temp.substring(tempSi.end + 1));
                    }
                }
                else
                {
                    sb.append(before);
                }

                sb.append(r.getString());
                sb.append("</w:t>");
                sb.append(after);
                splits.add(sb);
            }
        }

        return splits;
    }

    /**
     * When a sentence contains both English and Latin, Adding RTL to whole
     * sentence will cause display issue in word 2013.
     * 
     * The fix is to split English content from the sentence and not to add RTL
     * for it. (A better fix is to pick up the Latin content from the sentence,
     * but there seems to be no good method to make it.)
     * 
     * @since GBS-4185
     */
    private static StringBuffer splitEnglishAndAddRtl(StringBuffer content)
    {
        StringBuffer sb = new StringBuffer();

        StringIndex si = StringIndex.getValueBetween(content, 0, s_wrStartTag,
                s_wrEndTag);
        while (si != null)
        {
            String before = content.substring(0, si.start);
            String v = si.value;
            String after = content.substring(si.end);

            sb.append(before);
            List<StringBuffer> wts = splitEnglishWt(v);
            if (wts.size() > 1)
            {
                for (int i = 0; i < wts.size(); i++)
                {
                    sb.append(wts.get(i));
                    if (i != wts.size() - 1)
                    {
                        sb.append(s_wrEndTag);
                        sb.append(s_wrStartTag);
                    }
                }
            }
            else
            {
                sb.append(v);
            }
            sb.append(after);

            content.delete(0, content.length());
            content.append(after);
            si = StringIndex.getValueBetween(content, 0, s_wrStartTag,
                    s_wrEndTag);
        }

        return sb;
    }

    private static List<SplitString> split(String s)
    {
        ArrayList<SplitString> result = new ArrayList<SplitString>();
        Pattern p = Pattern
                .compile("[\\w\\pP]+[\\pP\\w\\s]*[\\w\\pP]+|[a-zA-Z]");
        Matcher m = p.matcher(s);
        int n = 0;
        while (m.find(n))
        {
            int start = m.start();
            int end = m.end();
            if (start != n)
            {
                result.add(new SplitString(s.substring(n, start), true));
            }
            n = end;
            result.add(new SplitString(m.group(), false));
        }

        if (n < s.length())
        {
            result.add(new SplitString(s.substring(n), true));
        }

        return result;
    }

    private static String addStyleTag2(StringBuffer src, String valueToAdd,
            String startTag, String endTag, String rprStartTag, String rprEndTag)
    {
        StringBuffer result = new StringBuffer();
        List<String> starts = new ArrayList<String>();
        starts.add(startTag);
        starts.add(startTag.trim() + s_gtMark);

        StringIndex si = StringIndex.getValueBetween(src, 0, starts, endTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);
            StringBuffer vsb = new StringBuffer();

            // add w:bidi for w:p in w:document XML
            if (v.contains(valueToAdd))
            {
                vsb.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0,
                        rprStartTag, s_gtMark);
                if (tempSi == null)
                {
                    int endIndex = 0;
                    if (!v.startsWith("<") && !v.trim().startsWith("<"))
                    {
                        // add the rest part of w:r or w:p like
                        // w:rsidRPr="0065076E">
                        String sss = v.substring(0, 1);
                        tempSi = StringIndex.getValueBetween(temp, 0, sss,
                                s_gtMark);
                        vsb.append(tempSi.allValue);

                        endIndex = tempSi.end + 1;
                    }
                    vsb.append(rprStartTag).append(s_gtMark);
                    vsb.append(valueToAdd).append(rprEndTag);
                    vsb.append(temp.substring(endIndex));
                }
                else
                {
                    vsb.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        vsb.append(tempV.substring(0, tempV.length() - 1));
                        vsb.append(s_gtMark);
                        vsb.append(valueToAdd);
                        vsb.append(rprEndTag);
                    }
                    else
                    {
                        vsb.append(tempV);
                        vsb.append(s_gtMark);
                        vsb.append(valueToAdd);
                    }
                    vsb.append(temp.substring(tempSi.end + 1));
                }
            }

            result.append(vsb);

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, starts, endTag);
        }

        result.append(src);

        String rrr = result.toString();
        return rrr;
    }

    private static boolean needToAddDocRTL(boolean doMoreCheck, String v,
            String addValue)
    {
        if (v.contains(addValue))
        {
            return false;
        }

        if (doMoreCheck)
        {
            boolean isDebugEnable = logger.isDebugEnabled();
            for (int i = 0; i < v.length(); i++)
            {
                char c = v.charAt(i);
                int type = Character.getType(c);
                if (isDebugEnable)
                {
                    logger.debug(c + " type: " + type);
                }

                if (type == Character.OTHER_LETTER)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static String fixRtlSlideXmlAP(String xmlContent, String locale)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();
        List<String> starts = new ArrayList<String>();
        starts.add(s_apStartTag);
        starts.add(s_apStartTag2);

        StringIndex si = StringIndex
                .getValueBetween(src, 0, starts, s_apEndTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            StringBuffer vsb = new StringBuffer();

            // add rtl="1" for a:p in p:sld XML
            if (v.contains(s_apPrRtl))
            {
                vsb.append(v);
            }
            else if (v.contains("rtl=\"0\""))
            {
                v = StringUtil.replace(v, "rtl=\"0\"", s_apPrRtl);
                vsb.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0,
                        s_apPrStartTag, s_gtMark);
                if (tempSi == null)
                {
                    if (before.endsWith(s_apStartTag2))
                    {
                        vsb.append("<a:pPr rtl=\"1\"/>");
                        vsb.append(temp);
                    }
                    else
                    {
                        String sss = v.substring(0, 1);
                        tempSi = StringIndex.getValueBetween(temp, 0, sss,
                                s_gtMark);
                        vsb.append(sss);
                        vsb.append(tempSi.value);
                        vsb.append(s_gtMark);
                        vsb.append("<a:pPr rtl=\"1\"/>");
                        vsb.append(temp.substring(tempSi.end + 1));
                    }
                }
                else
                {
                    vsb.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        vsb.append(tempV.substring(0, tempV.length() - 1));
                        vsb.append(" rtl=\"1\"/");
                    }
                    else
                    {
                        vsb.append(tempV);
                        vsb.append(" rtl=\"1\"");
                    }
                    vsb.append(temp.substring(tempSi.end));
                }
            }

            boolean needToAddRTL = needToAddDocRTL(true, vsb.toString(),
                    s_wrtlTag);
            if (needToAddRTL)
            {
                String addRTL = addStyleTag2(vsb, s_artlTag, s_arStartTag,
                        s_arEndTag, s_arPrStartTag, s_arPrEndTag);
                addRTL = replaceAttribute(addRTL, locale, "<a:rPr", "lang=\"");
                result.append(addRTL);
            }
            else
            {
                result.append(vsb);
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, s_apStartTag, s_apEndTag);
            if (si == null)
                si = StringIndex.getValueBetween(src, 0, s_apStartTag2,
                        s_apEndTag);
        }

        result.append(src);

        return result.toString();
    }

    private static String replaceAttribute(String ccc, String newValue,
            String startTag, String attStart)
    {
        if (newValue == null)
        {
            return ccc;
        }

        StringBuffer src = new StringBuffer(ccc);
        StringBuffer result = new StringBuffer();
        StringIndex si = StringIndex
                .getValueBetween(src, 0, startTag, s_gtMark);
        String after = null;

        while (si != null)
        {
            String b = src.substring(0, si.start);
            String v = si.value;
            after = src.substring(si.end);

            result.append(b);
            if (v.contains(attStart))
            {
                int index1 = v.indexOf(attStart) + attStart.length();
                int index2 = v.indexOf("\"", index1);

                if (index1 != -1 && index2 != -1)
                {
                    result.append(v.substring(0, index1));
                    result.append(newValue);
                    result.append(v.substring(index2));
                }
                else
                {
                    result.append(v);
                }

            }
            else
            {
                result.append(v);
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, startTag, s_gtMark);
        }

        result.append(src);

        return result.toString();
    }

    private static String fixRtlSheetStylesXml(String xmlContent, String locale)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();
        StringIndex si = StringIndex.getValueBetween(src, 0, s_cellXfsStartTag,
                s_cellXfsEndTag);
        String before = src.substring(0, si.start);
        String xfs = si.value;
        String after = src.substring(si.end);
        result.append(before);

        StringBuffer temp = new StringBuffer(xfs);
        si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_gtMark);
        while (si != null)
        {
            String b = temp.substring(0, si.start);
            String v = si.value;
            String a = temp.substring(si.end);

            if (v.endsWith("/"))
            {
                result.append(b);
                result.append(v.substring(0, v.length() - 1));
                result.append("><alignment readingOrder=\"2\"/></xf");
            }
            else
            {
                si = StringIndex.getValueBetween(temp, 0, s_xfStartTag,
                        s_xfEndTag);
                b = temp.substring(0, si.start);
                v = si.value;
                a = temp.substring(si.end);
                result.append(b);

                if (v.contains(" readingOrder=\"2\""))
                {
                    result.append(v);
                }
                else if (v.contains(" readingOrder=\"1\""))
                {
                    result.append(v.replace(" readingOrder=\"1\"",
                            " readingOrder=\"2\""));
                }
                else if (v.contains("<alignment "))
                {
                    result.append(v.replace("<alignment ",
                            "<alignment readingOrder=\"2\" "));
                }
                else
                {
                    result.append(v);
                    result.append("<alignment readingOrder=\"2\"/>");
                }
            }

            temp.delete(0, temp.length());
            temp.append(a);
            si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_gtMark);
        }

        result.append(temp);
        result.append(after);

        return result.toString();
    }
}
