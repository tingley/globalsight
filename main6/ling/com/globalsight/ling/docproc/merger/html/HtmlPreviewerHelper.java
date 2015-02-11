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

    public static String processGSColorTag(String content)
    {
        Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_S);
        Pattern pe = Pattern.compile(ExportHelper.RE_GS_COLOR_E);

        Matcher ms = ps.matcher(content);
        while (ms.find())
        {
            String ori = ms.group();
            String color = ms.group(1);
            String newColor = "<font color='" + color + "'>";
            content = content.replace(ori, newColor);
        }

        Matcher me = pe.matcher(content);
        while (me.find())
        {
            String ori = me.group();
            content = content.replace(ori, "</font>");
        }

        return content;
    }
    
    public static String addGSColorForSegment(String format, String tuvContent, String start, String end)
    {
        int index_s = tuvContent.indexOf(">") + 1;
        int index_e = tuvContent.length() - 10;
        Vector<Integer> starts = new Vector<Integer>();
        Vector<Integer> ends = new Vector<Integer>();

        if (IFormatNames.FORMAT_OFFICE_XML.equals(format))
        {
            if (tuvContent.contains("</bpt>") 
                    && (tuvContent.contains("<ept>") || tuvContent.contains("<ept ")))
            {
                while(true)
                {
                    int index0 = tuvContent.indexOf("</bpt>", index_s);
                    if (index0 == -1)
                    {
                        break;
                    }
                    index_s = index0 + 6;
                    
                    int index1 = tuvContent.indexOf("<ept>", index_s);
                    int index2 = tuvContent.indexOf("<ept ", index_s);

                    if (index1 == -1 && index2 == -1)
                    {
                        break;
                    }
                    
                    if (index1 == -1)
                    {
                        index_e = index2;
                    }
                    else if (index2 == -1)
                    {
                        index_e = index1;
                    }
                    else
                    {
                        index_e = index1 < index2 ? index1 : index2;
                    }
                    
                    starts.add(index_s);
                    ends.add(index_e);
                }
            }
            else
            {
                if (tuvContent.contains("</bpt>"))
                {
                    index_s = tuvContent.indexOf("</bpt>") + 6;
                }
    
                if (tuvContent.contains("<ept>") || tuvContent.contains("<ept "))
                {
                    int index1 = tuvContent.lastIndexOf("<ept>");
                    int index2 = tuvContent.lastIndexOf("<ept ");
    
                    if (index1 < index2)
                    {
                        index_e = index2;
                    }
                    else if (index1 > index2)
                    {
                        index_e = index1;
                    }
                }
                
                starts.add(index_s);
                ends.add(index_e);
            }
        }
        else
        {
            starts.add(index_s);
            ends.add(index_e);
        }
        
        StringBuffer sb = new StringBuffer(tuvContent);
        for(int i = starts.size() - 1; i >= 0; i--)
        {
            int s = starts.get(i);
            int e = ends.get(i);
            
            sb.insert(e, end);
            sb.insert(s, start);
        }

        return sb.toString();
    }

    public static String fixOfficePreviewXml(String content)
    {
        String newContent = moveColorsToWT(content, ExportHelper.RE_GS_COLOR_E, false);
        newContent = moveColorsToWT(newContent, ExportHelper.RE_GS_COLOR_S, true);
        return newContent;
    }

    private static String moveColorsToWT(String content, String re, boolean isStarts)
    {
        Pattern pe = Pattern.compile(re);
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();

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

        if (starts.size() != 0)
        {
            for (int i = starts.size() - 1; i >= 0; i--)
            {
                int s = starts.get(i);
                int e = ends.get(i);
                String pre = content.substring(0, s + 1);
                String end = content.substring(e);
                String color = content.substring(s + 1, e);

                if (isStarts)
                {
                    int wtindex = pre.lastIndexOf("</w:t>");
                    if (wtindex != -1)
                    {
                        content = pre.substring(0, wtindex) + color + pre.substring(wtindex) + end;
                    }
                    else
                    {
                        // != -1 !!
                        int wtindex0 = end.indexOf("<w:t ");
                        int wtindex1 = end.indexOf("<w:t>");
                        if (wtindex0 == -1 && wtindex1 != -1)
                        {
                            wtindex = wtindex1 + 5;
                        }
                        else if (wtindex0 != -1 && wtindex1 == -1)
                        {
                            wtindex = end.indexOf(">", wtindex0) + 1;
                        }
                        else if (wtindex0 < wtindex1)
                        {
                            wtindex = end.indexOf(">", wtindex0) + 1;
                        }
                        else if (wtindex0 > wtindex1)
                        {
                            wtindex = wtindex1 + 5;
                        }

                        if (wtindex != -1)
                        {
                            content = pre + end.substring(0, wtindex) + color
                                    + end.substring(wtindex);
                        }
                    }
                }
                else
                {
                    int wtindex = -1;
                    int wtindex0 = end.indexOf("<w:t ");
                    int wtindex1 = end.indexOf("<w:t>");

                    if (wtindex0 == -1 && wtindex1 != -1)
                    {
                        wtindex = wtindex1 + 5;
                    }
                    else if (wtindex0 != -1 && wtindex1 == -1)
                    {
                        wtindex = end.indexOf(">", wtindex0) + 1;
                    }
                    else if (wtindex0 < wtindex1)
                    {
                        wtindex = end.indexOf(">", wtindex0) + 1;
                    }
                    else if (wtindex0 > wtindex1)
                    {
                        wtindex = wtindex1 + 5;
                    }

                    if (wtindex != -1)
                    {
                        content = pre + end.substring(0, wtindex) + color + end.substring(wtindex);
                    }
                    else
                    {
                        wtindex = pre.lastIndexOf("</w:t>");
                        if (wtindex != -1)
                        {
                            content = pre.substring(0, wtindex) + color + pre.substring(wtindex)
                                    + end;
                        }
                    }
                }
            }
        }

        return content;
    }

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
