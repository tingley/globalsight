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
package com.globalsight.ling.docproc.merger.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.merger.fm.GSContentColor;

/**
 * This helper class to resolve the mapping relationship for color and match
 * result
 */
public class HtmlPreviewerHelper
{
    private static Pattern m_ps = Pattern.compile(ExportHelper.RE_GS_COLOR_S);
    private static Pattern m_pe = Pattern.compile(ExportHelper.RE_GS_COLOR_E);
    private static Pattern m_pspan = Pattern.compile("<span[^>]+style='[^>']*(color\\:)[^>']+'[^>]*>");
    
    /**
     * Remove GS color tag from content
     * 
     * @param content
     * @return
     */
    public static String removeGSColorTag(String content)
    {
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_S);
        Pattern pe = Pattern.compile(ExportHelper.RE_GS_COLOR_E);

        Matcher ms = ps.matcher(content);
        while (ms.find())
        {
            String ori = ms.group();
            content = content.replace(ori, "");
        }

        Matcher me = pe.matcher(content);
        while (me.find())
        {
            String ori = me.group();
            content = content.replace(ori, "");
        }

        return content;
    }

    /**
     * Replace GS color tag to html color tag
     * 
     * @param content
     * @return
     */
    public static String processGSColorTag(String content)
    {
        if (content.contains(ExportHelper.GS_COLOR_S)
                && content.contains(ExportHelper.GS_COLOR_E))
        {
            // remove style='color:#4F81BD' style='color:#4F81BD;'
            Matcher mspan = m_pspan.matcher(content);
            while (mspan.find())
            {
                String ori = mspan.group();
                String color = mspan.group(1);
                String newspan = ori.replace(color, "gscolor:");
                content = content.replace(ori, newspan);
            }
            
            Matcher ms = m_ps.matcher(content);
            while (ms.find())
            {
                String ori = ms.group();
                String color = ms.group(1);
                String newColor = "<font color='" + color + "'>";
                content = content.replace(ori, newColor);
            }

            Matcher me = m_pe.matcher(content);
            while (me.find())
            {
                String ori = me.group();
                content = content.replace(ori, "</font>");
            }
        }
        
        return content;
    }

    /**
     * Add GS color tags into segments for preview
     * 
     * @param format
     * @param tuvContent
     * @param start
     * @param end
     * @return
     */
    public static String addGSColorForSegment(String format, String tuvContent,
            String start, String end, boolean isInddOrIdml)
    {
        int index_s = tuvContent.indexOf(">") + 1;
        int index_e = tuvContent.length() - 10;
        Vector<Integer> starts = new Vector<Integer>();
        Vector<Integer> ends = new Vector<Integer>();
        boolean isOfficeXml = IFormatNames.FORMAT_OFFICE_XML.equals(format);

        if (isOfficeXml)
        {
            return addGSColorTag(tuvContent, start, end);
        }
        // indd xml
        else if (isInddOrIdml)
        {
            String eee = end + " ";
            return addGSColorTag(tuvContent, start, eee);
        }
        // FM mif files
        else if (IFormatNames.FORMAT_MIF.equals(format))
        {
            return addGSColorTag(tuvContent, start, end);
        }
        // add color tag to contain all text
        else
        {
            starts.add(index_s);
            ends.add(index_e);
            
            String eee = end;
            StringBuffer sb = new StringBuffer(tuvContent);
            for (int i = starts.size() - 1; i >= 0; i--)
            {
                int s = starts.get(i);
                int e = ends.get(i);

                while (sb.charAt(e) == '(' && sb.charAt(e - 1) == '(')
                {
                    e = e - 1;
                }

                sb.insert(e, eee);
                sb.insert(s, start);
            }

            return sb.toString();
        }
    }

    private static String addGSColorTag(String tuvContent, String start,
            String end)
    {
        int index_s;
        int index_e;
        //For a issue that add bold style for pptx and preview,  
        // ((GS_COLOR_START) will displayed in content.
        Pattern p = Pattern.compile("(</[^>]*>)([^<]+)<");
        Matcher m = p.matcher(tuvContent);
        
        while(m.find())
        {
            String all = m.group();
            String pre = m.group(1);
            if ("</sub>".equals(pre))
            {
                continue;
            }
            
            String c = m.group(2);
            String newStr = pre + start + c + end + "<";
            
            tuvContent = tuvContent.replace(all, newStr);
        }
        
        index_s = tuvContent.indexOf(">") + 1;
        index_e = tuvContent.length() - 10;
        String s1 = tuvContent.substring(0, index_s);
        String sss = tuvContent.substring(index_s, index_e);
        String s2 = tuvContent.substring(index_e);
        if (!sss.startsWith("<") && !sss.startsWith(start))
        {
            int index_lt = sss.indexOf("<");
            String newsss = null;
            if (index_lt > -1)
            {
                newsss = start + sss.substring(0, index_lt) + end
                        + sss.substring(index_lt);
            }
            else
            {
                newsss = start + sss + end;
            }
            
            tuvContent = s1 + newsss + s2;
        }
        
        index_s = tuvContent.indexOf(">") + 1;
        index_e = tuvContent.length() - 10;
        s1 = tuvContent.substring(0, index_s);
        sss = tuvContent.substring(index_s, index_e);
        s2 = tuvContent.substring(index_e);
        if (!sss.endsWith(">") && !sss.endsWith(end))
        {
            int index_gt = sss.lastIndexOf(">");
            String newsss = null;
            if (index_gt > -1)
            {
                newsss = sss.substring(0, index_gt + 1) + start
                        + sss.substring(index_gt + 1) + end;
            }
            else
            {
                newsss = start + sss + end;
            }
            
            tuvContent = s1 + newsss + s2;
        }
        
        return tuvContent;
    }

    /**
     * Move GS color tags to right place
     * 
     * @param content
     * @return
     */
    public static String fixOfficePreviewXml(String content)
    {
        String newContent = removerPrChange(content);
        newContent = moveColorsToWT(newContent, false);
        newContent = moveColorsToWT(newContent, true);
        newContent = moveEndColorsToRealEnd(newContent);
        //newContent = addStartColorTasIfNeed(newContent);
        return newContent;
    }

    private static String removerPrChange(String content)
    {
        Pattern ps = Pattern.compile("<w:rPrChange[^>]*>(.*?)</w:rPrChange>");

        Matcher ms = ps.matcher(content);
        while (ms.find())
        {
            String ori = ms.group();
            content = content.replace(ori, "");
        }

        return content;
    }

    private static String addStartColorTasIfNeed(String content)
    {
        String re = "<w:t[^>]*>([^<]*)</w:t>";

        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();

        Pattern pe = Pattern.compile(re);
        Matcher me = pe.matcher(content);
        while (me.find())
        {
            int s = me.start() - 1;
            int e = me.end();
            String co = me.group(1);
            if (!co.startsWith("((GS_COLOR_"))
            {
                starts.add(s);
                ends.add(e);
            }
        }

        if (starts.size() != 0)
        {
            for (int i = starts.size() - 1; i >= 0; i--)
            {
                int s = starts.get(i);
                int e = ends.get(i);
                String pre = content.substring(0, s + 1);
                String end = content.substring(e);
                String wtcontent = content.substring(s + 1, e);
                int index = wtcontent.indexOf(">");
                String color = null;
                List<GSContentColor> gscolors = null;

                if ((color = getFirstGSColor(wtcontent)) != null)
                {
                    color = color;
                }
                else if ((gscolors = getGSColor(pre)) != null
                        && gscolors.size() > 0)
                {
                    color = gscolors.get(gscolors.size() - 1).getColor();
                }
                else if ((gscolors = getGSColor(end)) != null
                        && gscolors.size() > 0)
                {
                    color = gscolors.get(0).getColor();
                }

                if (color != null)
                {
                    String temp = ExportHelper.GS_COLOR_S + "(" + color + "))";
                    content = pre + wtcontent.substring(0, index) + temp
                            + wtcontent.substring(index) + end;
                }
            }
        }

        return content;
    }

    private static String moveEndColorsToRealEnd(String content)
    {
        String re = ExportHelper.RE_GS_COLOR_E;

        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();

        Pattern pe = Pattern.compile(re);
        Matcher me = pe.matcher(content);
        while (me.find())
        {
            int s = me.start() - 1;
            int e = me.end();

            if (content.charAt(e) != '<' && content.charAt(e) != '(')
            {
                starts.add(s);
                ends.add(e);
            }
        }

        if (starts.size() != 0)
        {
            for (int i = starts.size() - 1; i >= 0; i--)
            {
                int s = starts.get(i);
                int e = ends.get(i);
                String pre = content.substring(0, s + 1);
                String end = content.substring(e);
                String color = content.substring(s + 1, e);

                int wtindex_e = end.indexOf("</w:t>");
                if (wtindex_e != -1)
                {
                    content = pre + end.substring(0, wtindex_e) + color
                            + end.substring(wtindex_e);
                }
            }
        }

        return content;
    }

    private static String moveColorsToWT(String content, boolean isStarts)
    {
        String re = isStarts ? ExportHelper.RE_GS_COLOR_S
                : ExportHelper.RE_GS_COLOR_E;
        String otherRe = isStarts ? ExportHelper.RE_GS_COLOR_E
                : ExportHelper.RE_GS_COLOR_S;

        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        List<Integer> otherstarts = new ArrayList<Integer>();
        List<Integer> otherends = new ArrayList<Integer>();

        Pattern pe = Pattern.compile(re);
        Matcher me = pe.matcher(content);
        while (me.find())
        {
            int s = me.start() - 1;
            int e = me.end();

            if (content.charAt(s) == '>' && content.charAt(e) == '<')
            {
                starts.add(s);
                ends.add(e);
            }
        }

        Pattern otherpe = Pattern.compile(otherRe);
        Matcher otherme = otherpe.matcher(content);
        while (otherme.find())
        {
            int s = otherme.start() - 1;
            int e = otherme.end();
            otherstarts.add(s);
            otherends.add(e);
        }

        if (starts.size() != 0)
        {
            if (isStarts)
            {
                for (int i = 0; i < starts.size(); i++)
                {
                    int s = starts.get(i);
                    int e = ends.get(i);
                    String pre = content.substring(0, s + 1);
                    String end = content.substring(e);
                    String color = content.substring(s + 1, e);

                    int wtindex_p = pre.lastIndexOf("</w:t>");
                    int wtindex_e = -1;
                    int wtindex_e0 = end.indexOf("<w:t ");
                    int wtindex_e1 = end.indexOf("<w:t>");
                    // != -1 !!
                    if (wtindex_e0 == -1 && wtindex_e1 != -1)
                    {
                        wtindex_e = wtindex_e1 + 5;
                    }
                    else if (wtindex_e0 != -1 && wtindex_e1 == -1)
                    {
                        wtindex_e = end.indexOf(">", wtindex_e0) + 1;
                    }
                    else if (wtindex_e0 < wtindex_e1)
                    {
                        wtindex_e = end.indexOf(">", wtindex_e0) + 1;
                    }
                    else if (wtindex_e0 > wtindex_e1)
                    {
                        wtindex_e = wtindex_e1 + 5;
                    }

                    // if there is end tag exists before me, move the end
                    if (wtindex_p != -1
                            && wtindex_e != -1
                            && getCountBetweenStartAndEnd(wtindex_p, e
                                    + wtindex_e, starts) == 1
                            && otherends.contains(wtindex_p))
                    {
                        content = pre + end.substring(0, wtindex_e) + color
                                + end.substring(wtindex_e);
                    }
                    else if (wtindex_p != -1)
                    {
                        content = pre.substring(0, wtindex_p) + color
                                + pre.substring(wtindex_p) + end;
                    }
                    else
                    {
                        if (wtindex_e != -1)
                        {
                            content = pre + end.substring(0, wtindex_e) + color
                                    + end.substring(wtindex_e);
                        }
                    }
                }
            }
            else
            {
                for (int i = starts.size() - 1; i >= 0; i--)
                {
                    int s = starts.get(i);
                    int e = ends.get(i);
                    String pre = content.substring(0, s + 1);
                    String end = content.substring(e);
                    String color = content.substring(s + 1, e);
                    int wtindex_p = pre.lastIndexOf("</w:t>");
                    int wtindex_e = -1;
                    int wtindex_e0 = end.indexOf("<w:t ");
                    int wtindex_e1 = end.indexOf("<w:t>");

                    if (wtindex_e0 == -1 && wtindex_e1 != -1)
                    {
                        wtindex_e = wtindex_e1 + 5;
                    }
                    else if (wtindex_e0 != -1 && wtindex_e1 == -1)
                    {
                        wtindex_e = end.indexOf(">", wtindex_e0) + 1;
                    }
                    else if (wtindex_e0 < wtindex_e1)
                    {
                        wtindex_e = end.indexOf(">", wtindex_e0) + 1;
                    }
                    else if (wtindex_e0 > wtindex_e1)
                    {
                        wtindex_e = wtindex_e1 + 5;
                    }

                    // if there is start tag exists, move the pre
                    if (wtindex_p != -1
                            && wtindex_e != -1
                            && getCountBetweenStartAndEnd(wtindex_p, e
                                    + wtindex_e, starts) == 1
                            && otherstarts.contains(e + wtindex_e))
                    {
                        content = pre.substring(0, wtindex_p) + color
                                + pre.substring(wtindex_p) + end;
                    }
                    // if there is start tag exists behind me, move the pre
                    else if (wtindex_p != -1
                            && wtindex_e != -1
                            && getCountBetweenStartAndEnd(wtindex_p, e
                                    + wtindex_e, starts) == 1
                            && getCountBetweenStartAndEnd(wtindex_p, e
                                    + wtindex_e, otherstarts) == 1
                            && getValueBetweenStartAndEnd(wtindex_p, e
                                    + wtindex_e, starts) < getValueBetweenStartAndEnd(
                                        wtindex_p, e + wtindex_e, otherstarts))
                    {
                        content = pre.substring(0, wtindex_p) + color
                                + pre.substring(wtindex_p) + end;
                    }
                    else if (wtindex_e != -1)
                    {
                        content = pre + end.substring(0, wtindex_e) + color
                                + end.substring(wtindex_e);
                    }
                    else
                    {
                        if (wtindex_p != -1)
                        {
                            content = pre.substring(0, wtindex_p) + color
                                    + pre.substring(wtindex_p) + end;
                        }
                    }
                }
            }
        }

        return content;
    }

    private static int getCountBetweenStartAndEnd(int start, int end,
            List<Integer> indexList)
    {
        int count = 0;

        if (indexList == null || indexList.size() == 0)
        {
            return count;
        }

        for (Integer index : indexList)
        {
            if (index >= start && index <= end)
            {
                count++;
            }
        }

        return count;
    }

    private static int getValueBetweenStartAndEnd(int start, int end,
            List<Integer> indexList)
    {
        if (indexList == null || indexList.size() == 0)
        {
            return -1;
        }

        for (Integer index : indexList)
        {
            if (index >= start && index <= end)
            {
                return index;
            }
        }

        return -1;
    }

    /**
     * Get the first color from color tag
     * 
     * @param content
     * @return
     */
    public static String getFirstGSColor(String content)
    {
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_S);
        Pattern pe = Pattern.compile(ExportHelper.RE_GS_COLOR_E);

        Matcher ms = ps.matcher(content);
        if (ms.find())
        {
            String color = ms.group(1);
            return color;
        }

        Matcher me = pe.matcher(content);
        if (me.find())
        {
            String color = me.group(1);
            return color;
        }

        return null;
    }

    /**
     * get all color tags from content
     * 
     * @param content
     * @return
     */
    public static List<GSContentColor> getGSColor(String content)
    {
        List<GSContentColor> result = new ArrayList<GSContentColor>();
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_S);
        Pattern pe = Pattern.compile(ExportHelper.RE_GS_COLOR_E);
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        List<String> startcolors = new ArrayList<String>();
        List<String> endcolors = new ArrayList<String>();

        Matcher ms = ps.matcher(content);
        while (ms.find())
        {
            String color = ms.group(1);
            startcolors.add(color);
            starts.add(ms.end());
        }

        Matcher me = pe.matcher(content);
        while (me.find())
        {
            ends.add(me.start());
            String color = me.group(1);
            endcolors.add(color);
        }

        if (starts.size() != 0 && starts.size() == ends.size())
        {
            int s0 = starts.get(0);
            int e0 = ends.get(0);

            if (e0 > s0)
            {
                for (int i = 0; i < starts.size(); i++)
                {
                    int si = starts.get(i);
                    int ei = ends.get(i);
                    String scolor = startcolors.get(i);
                    String c = content.substring(si, ei);
                    GSContentColor gsc = new GSContentColor(c, scolor);
                    result.add(gsc);
                }
            }
            else
            {
                String c = content.substring(0, e0);
                String ecolor = endcolors.get(0);
                GSContentColor gsc = new GSContentColor(c, ecolor);
                result.add(gsc);
                for (int i = 1; i < ends.size(); i++)
                {
                    int si = starts.get(i - 1);
                    int ei = ends.get(i);
                    String scolor = startcolors.get(i - 1);
                    c = content.substring(si, ei);
                    gsc = new GSContentColor(c, scolor);
                    result.add(gsc);
                }

                int last = starts.size() - 1;
                int smax = starts.get(last);
                String scolor = startcolors.get(last);
                c = content.substring(smax);
                gsc = new GSContentColor(c, scolor);
                result.add(gsc);
            }
        }

        return result;
    }

    public static String removeGSColorDefine(String content)
    {
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_DEFINE);

        Matcher ms = ps.matcher(content);
        if (ms.find())
        {
            String ori = ms.group();
            content = content.replace(ori, "");
        }

        return content;
    }

    /**
     * get definition colors from GS color definition tag
     * 
     * @param content
     * @return
     */
    public static String getGSColorDefineColors(String content)
    {
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_DEFINE);

        Matcher ms = ps.matcher(content);
        if (ms.find())
        {
            String colors = ms.group(1);
            return colors;
        }

        return null;
    }

    /**
     * get definition tag for GS colors
     * 
     * @param content
     * @return
     */
    public static String getGSColorDefine(String content)
    {
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_DEFINE);

        Matcher ms = ps.matcher(content);
        if (ms.find())
        {
            String colors = ms.group();
            return colors;
        }

        return null;
    }
}
