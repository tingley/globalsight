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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.util.FileUtil;

public class OfficeXmlTagHelper
{
    private static final String REGEX_WR_DOCX = "(((<w:r [^>]*>)|(<w:r>)).*?</w:r>)";
    private static final String REGEX_CONTENT_DOCX = "(<w:t[^>]*>)([^<]*?)</w:t>";
    private static final String REGEX_AR_PPTX = "(((<a:r [^>]*>)|(<a:r>)).*?</a:r>)";
    private static final String REGEX_CONTENT_PPTX = "(<a:t[^>]*>)([^<]*?)</a:t>";
    private static final String REGEX_ARPR_PPTX = "<a:rPr [^>]*/?>";

    private int m_type = OfficeXmlHelper.OFFICE_DOCX;

    public OfficeXmlTagHelper(int type)
    {
        m_type = type;
    }

    /**
     * Merges the same tags to one for the xml files.
     * 
     * @param xmlFiles
     *            the paths of all xml files.
     * @param type
     *            the file type
     * @throws Exception
     */
    public void mergeTags(String[] xmlFiles) throws Exception
    {
        List<String> wrs = new ArrayList<String>();
        List<Integer> indexes = new ArrayList<Integer>();

        Pattern p = Pattern.compile(REGEX_WR_DOCX);

        if (m_type == OfficeXmlHelper.OFFICE_PPTX)
        {
            p = Pattern.compile(REGEX_AR_PPTX);
        }
        if (xmlFiles != null && xmlFiles.length > 0)
        {
            for (String xmlFile : xmlFiles)
            {
                File f = new File(xmlFile);
                String content = FileUtil.readFile(f, "utf-8");
                content = removeUnusdTags(content);

                int n = 0;
                Matcher m = p.matcher(content);
                while (m.find())
                {
                    wrs.add(m.group());
                    n = content.indexOf(m.group(), n);
                    indexes.add(n);
                }

                mergeTag(f, content, wrs, indexes);
            }
        }
    }

    /**
     * Gets the indexes of all tags that can be merged.
     * 
     * @param wrs
     *            all tags
     * @param indexes
     *            the indexes of all tags.
     * @return the indexes of all tags that can be merged.
     */
    private List<Integer> getMergeindexes(List<String> wrs,
            List<Integer> indexes)
    {
        List<Integer> mindexes = new ArrayList<Integer>();
        for (int i = 0; i < wrs.size() - 1; i++)
        {
            String s = wrs.get(i);
            if (s.length() + indexes.get(i) == indexes.get(i + 1))
            {
                mindexes.add(indexes.get(i + 1));
            }
        }

        return mindexes;
    }

    private void mergeTag(File f, String content, List<String> wrs,
            List<Integer> indexes) throws IOException
    {
        List<Integer> mergeindexes = getMergeindexes(wrs, indexes);

        for (int i = wrs.size() - 1; i > 0; i--)
        {
            String wr = wrs.get(i);
            String last = wrs.get(i - 1);

            if (mergeindexes.contains(indexes.get(i)))
            {
                String mergedTag = getMergedTags(m_type, last, wr);
                if (mergedTag != last)
                {
                    wrs.set(i - 1, mergedTag);
                    content = content.replace(last + wr, mergedTag);
                }
            }
        }

        FileUtil.writeFile(f, content, "utf-8");
    }

    public static String getMergedTags(int filetype, String f1, String f2)
    {
        if (filetype == OfficeXmlHelper.OFFICE_DOCX)
        {
            if (f1.indexOf("<w:br/>") > 0 || f2.indexOf("<w:br/>") > 0)
                return f1;

            if (f1.indexOf("<w:b/>") > 0 && f2.indexOf("<w:b/>") < 0)
                return f1;

            if (f2.indexOf("<w:b/>") > 0 && f1.indexOf("<w:b/>") < 0)
                return f1;

            if (f1.indexOf("</w:t>") > 0 && f2.indexOf("</w:t>") < 0)
                return f1;

            if (f2.indexOf("</w:t>") > 0 && f1.indexOf("</w:t>") < 0)
                return f1;

            String firstTemp = f1;
            String secondTemp = f2;
            String first = f1;
            String second = f2;

            firstTemp = firstTemp.replaceAll("<w:r>", "");
            firstTemp = firstTemp.replaceAll("<w:r [^>]*>", "");
            firstTemp = firstTemp.replaceAll("<w:t>[^<]*?</w:t>", "");
            firstTemp = firstTemp.replaceAll("<w:t [^>]*>[^<]*?</w:t>", "");

            secondTemp = secondTemp.replaceAll("<w:r>", "");
            secondTemp = secondTemp.replaceAll("<w:r [^>]*>", "");
            secondTemp = secondTemp.replaceAll("<w:t>[^<]*?</w:t>", "");
            secondTemp = secondTemp.replaceAll("<w:t [^>]*>[^<]*?</w:t>", "");

            boolean isOrderChanged = false;
            if ("</w:r>".equals(firstTemp))
            {
                isOrderChanged = true;
            }

            if (!isOrderChanged)
            {
                String temp = secondTemp.replaceAll(
                        "<w:rFonts w:hint=\"[^\"]*\"/>", "");
                temp = temp.replaceAll(" w:hint=\"[^\"]*\"", "");
                if (temp.equals(firstTemp))
                {
                    isOrderChanged = true;
                }
            }

            if (isOrderChanged)
            {
                String temp = firstTemp;
                firstTemp = secondTemp;
                secondTemp = temp;

                first = f2;
                second = f1;
            }

            boolean matched = secondTemp.equals(firstTemp) || isOrderChanged;

            if (!matched && "</w:r>".equals(secondTemp))
            {
                matched = true;
            }

            if (!matched)
            {
                String temp = firstTemp.replaceAll(
                        "<w:rFonts w:hint=\"[^\"]*\"/>", "");
                if (temp.equals(secondTemp))
                {
                    matched = true;
                }
            }

            if (matched)
            {
                String content1 = getContent(filetype, first);
                String content2 = getContent(filetype, second);

                String wt = getTextTag(filetype, first);
                if (wt.length() == 0)
                {
                    return first;
                }

                String newContent = isOrderChanged ? content2 + content1
                        : content1 + content2;
                first = first.replace(wt + content1, wt + newContent);

                if (first.indexOf("xml:space=\"preserve\"") < 0
                        && second.indexOf("xml:space=\"preserve\"") > 0)
                {
                    first = first
                            .replace("<w:t", "<w:t xml:space=\"preserve\"");
                }

                if (first.indexOf("<w:r>") > 0 && second.indexOf("<w:r>") < 0)
                {
                    Pattern p = Pattern.compile("<w:r[^>]*>");
                    Matcher m = p.matcher(second);
                    if (m.find())
                    {
                        first = first.replace("<w:r>", m.group());
                    }
                }
            }

            return first;
        }
        else if (filetype == OfficeXmlHelper.OFFICE_PPTX)
        {
            String firstTemp = f1;
            String secondTemp = f2;
            String first = f1;
            String second = f2;

            firstTemp = firstTemp.replaceAll("<a:r>", "");
            firstTemp = firstTemp.replaceAll("<a:r [^>]*>", "");
            firstTemp = firstTemp.replaceAll("<a:t>[^<]*?</a:t>", "");
            firstTemp = firstTemp.replaceAll("<a:t [^>]*>[^<]*?</a:t>", "");
            firstTemp = firstTemp.replaceAll("<a:t/>", "");
            firstTemp = firstTemp.replaceAll("</a:rPr>", "");

            secondTemp = secondTemp.replaceAll("<a:r>", "");
            secondTemp = secondTemp.replaceAll("<a:r [^>]*>", "");
            secondTemp = secondTemp.replaceAll("<a:t>[^<]*?</a:t>", "");
            secondTemp = secondTemp.replaceAll("<a:t [^>]*>[^<]*?</a:t>", "");
            secondTemp = secondTemp.replaceAll("<a:t/>", "");
            secondTemp = secondTemp.replaceAll("</a:rPr>", "");

            boolean isOrderChanged = false;

            // a:rPr attributes : b blank, u under-line, sz size, base line, i incline, strike 
            String regex_b = " b=\"([^\"]*)\"";
            String regex_u = " u=\"([^\"]*)\"";
            String regex_sz = " sz=\"([^\"]*)\"";
            String regex_baseline = " baseline=\"([^\"]*)\"";
            String regex_i = " i=\"([^\"]*)\"";
            String regex_strike = " strike=\"([^\"]*)\"";
            boolean isBSame = isArPrAttributeSame(firstTemp, secondTemp, regex_b);
            boolean isUSame = isArPrAttributeSame(firstTemp, secondTemp, regex_u);
            boolean isSZSame = isArPrAttributeSame(firstTemp, secondTemp, regex_sz);
            boolean isBaselineSame = isArPrAttributeSame(firstTemp, secondTemp, regex_baseline);
            boolean isISame = isArPrAttributeSame(firstTemp, secondTemp, regex_i);
            boolean isStrikeSame = isArPrAttributeSame(firstTemp, secondTemp, regex_strike);


            if (isBSame && isUSame && isSZSame && isBaselineSame && isISame && isStrikeSame)
            {
                String arPr1 = getARPR(firstTemp);
                String arPr2 = getARPR(secondTemp);
                firstTemp = firstTemp.replaceAll(arPr1, "");
                secondTemp = secondTemp.replaceAll(arPr2, "");
            }
            
            if ("</a:r>".equals(firstTemp) && isSameColor(firstTemp, secondTemp, first, second))
            {
                isOrderChanged = true;
            }

            if (!isOrderChanged)
            {
                String temp = secondTemp.replaceAll(
                        "<a:solidFill>[\\s\\S]*</a:solidFill>", "");
                temp = temp.replaceAll("<a:latin [^>]*/>", "");
                temp = temp.replaceAll("<a:cs [^>]*/>", "");
                if (temp.equals(firstTemp) && isSameColor(firstTemp, secondTemp, first, second))
                {
                    isOrderChanged = true;
                }
            }

            if (isOrderChanged)
            {
                String temp = firstTemp;
                firstTemp = secondTemp;
                secondTemp = temp;

                first = f2;
                second = f1;
            }

            boolean matched = secondTemp.equals(firstTemp) || isOrderChanged;

            if (!matched)
            {
                String temp = firstTemp.replaceAll(
                        "<a:solidFill>[\\s\\S]*</a:solidFill>", "");
                temp = temp.replaceAll("<a:latin [^>]*/>", "");
                temp = temp.replaceAll("<a:cs [^>]*/>", "");
                if (temp.equals(secondTemp) && isSameColor(firstTemp, secondTemp, first, second))
                {
                    matched = true;
                }
            }

            if (matched)
            {
                String content1 = getContent(filetype, first);
                String content2 = getContent(filetype, second);
                String c1trim = content1.trim();
                String c2trim = content2.trim();                
                boolean isC1trimEmpty = "".equals(c1trim);
                boolean isC2trimEmpty = "".equals(c2trim);

                String at1 = getTextTag(filetype, first);
                if (at1.length() == 0 && !isC1trimEmpty)
                {
                    return first;
                }

                String at2 = getTextTag(filetype, second);
                if (at2.length() == 0 && !isC2trimEmpty)
                {
                    return first;
                }

                String newContent = isOrderChanged ? content2 + content1 : content1 + content2;

                if (isC1trimEmpty || isC2trimEmpty)
                {
                    if (isC1trimEmpty)
                    {
                        first = second.replace(at2 + content2, at2 + newContent);
                    }
                    else
                    {
                        first = first.replace(at1 + content1, at1 + newContent);
                    }
                }
                else
                {

                    first = first.replace(at1 + content1, at1 + newContent);

                    if (first.indexOf("<a:r>") > 0 && second.indexOf("<a:r>") < 0)
                    {
                        Pattern p1 = Pattern.compile("<a:r[^>]*>");
                        Matcher m = p1.matcher(second);
                        if (m.find())
                        {
                            first = first.replace("<a:r>", m.group());
                        }
                    }
                }
            }
            
            return first;
        }
        return f1;
    }
    
    /**
     * Check if these two tag have same color, and other style, e.g. link
     */
    private static boolean isSameColor(String firstTemp, String secondTemp, String f1, String f2)
    {
        String content1 = getContent(OfficeXmlHelper.OFFICE_PPTX, f1);
        String content2 = getContent(OfficeXmlHelper.OFFICE_PPTX, f2);
        String c1trim = content1.trim();
        String c2trim = content2.trim();                
        boolean isC1trimEmpty = "".equals(c1trim);
        boolean isC2trimEmpty = "".equals(c2trim);
        
        if (isC1trimEmpty || isC2trimEmpty)
        {
            return true;
        }
        
        // color
        String colorkey = "<a:srgbClr val=";
        if (firstTemp.contains(colorkey) && !secondTemp.contains(colorkey)
                || secondTemp.contains(colorkey) && !firstTemp.contains(colorkey))
        {
            return false;
        }
        
        // link
        String hlinkkey = "<a:hlinkClick ";
        if (firstTemp.contains(hlinkkey) && !secondTemp.contains(hlinkkey)
                || secondTemp.contains(hlinkkey) && !firstTemp.contains(hlinkkey))
        {
            return false;
        }
        
        return true;
    }

    private static String getARPR(String firstTemp)
    {
        Pattern p = Pattern.compile(REGEX_ARPR_PPTX);
        Matcher m1 = p.matcher(firstTemp);

        String arPr1 = null;
        if (m1.find())
        {
            arPr1 = m1.group();
        }

        return arPr1;
    }

    private static boolean isArPrAttributeSame(String firstTemp, String secondTemp, String regex_b)
    {
        Pattern p = Pattern.compile(REGEX_ARPR_PPTX);
        Matcher m1 = p.matcher(firstTemp);
        String b1 = null;
        String arPr1 = null;
        if (m1.find())
        {
            arPr1 = m1.group();
            Pattern p1 = Pattern.compile(regex_b);
            m1 = p1.matcher(firstTemp);
            if (m1.find())
            {
                b1 = m1.group(1);
            }
        }

        Matcher m2 = p.matcher(secondTemp);
        String b2 = null;
        String arPr2 = null;
        if (m2.find())
        {
            arPr2 = m2.group();
            Pattern p2 = Pattern.compile(regex_b);
            m2 = p2.matcher(secondTemp);
            if (m2.find())
            {
                b2 = m2.group(1);
            }
        }

        boolean isBSame = b1 != null && b2 != null && b1.equals(b2);

        if (!isBSame)
        {
            isBSame = b1 == null && b2 == null && arPr1 != null && arPr2 != null;
        }

        if (!isBSame)
        {
            isBSame = (b1 == null && "0".equals(b2)) || ("0".equals(b1) && b2 == null)
                    && arPr1 != null && arPr2 != null;
        }

        return isBSame;
    }

    private static String getTextTag(int filetype, String all)
    {
        Pattern p = null;
        if (filetype == OfficeXmlHelper.OFFICE_DOCX)
        {
            p = Pattern.compile(REGEX_CONTENT_DOCX);
        }
        else if (filetype == OfficeXmlHelper.OFFICE_PPTX)
        {
            p = Pattern.compile(REGEX_CONTENT_PPTX);
        }
        Matcher m = p.matcher(all);
        if (m.find())
        {
            return m.group(1);
        }
        return "";
    }

    private static String getContent(int filetype, String all)
    {
        Pattern p = null;
        if (filetype == OfficeXmlHelper.OFFICE_DOCX)
        {
            p = Pattern.compile(REGEX_CONTENT_DOCX);
        }
        else if (filetype == OfficeXmlHelper.OFFICE_PPTX)
        {
            p = Pattern.compile(REGEX_CONTENT_PPTX);
        }
        Matcher m = p.matcher(all);
        if (m.find())
        {
            return m.group(2);
        }

        return "";
    }

    private String removeUnusdTags(String content)
    {
        if (m_type == OfficeXmlHelper.OFFICE_DOCX)
        {
            String temp = content.replace(
                    "<w:proofErr w:type=\"spellStart\"/>", "");
            temp = temp.replace("<w:proofErr w:type=\"spellEnd\"/>", "");
            temp = temp.replace("<w:proofErr w:type=\"gramStart\"/>", "");
            temp = temp.replace("<w:proofErr w:type=\"gramEnd\"/>", "");
            temp = temp.replace("<w:lastRenderedPageBreak/>", "");
            return temp;
        }
        return content;
    }
}
