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

package com.globalsight.cxe.entity.filterconfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.comparator.PriorityComparator;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.LineIndex;
import com.globalsight.ling.docproc.LineString;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.TagIndex;

public class CustomTextRuleHelper
{
    private static final Logger CATEGORY = Logger
            .getLogger(CustomTextRuleHelper.class);
    private static XmlEntities m_xmlEncoder = new XmlEntities();

    public static String FIRST = "FIRST";
    public static String LAST = "LAST";

    public static String extractLines(String oriStr,
            List<CustomTextRuleBase> rules, String lineSeparator)
            throws Exception
    {
        if (rules.size() == 0)
        {
            return oriStr;
        }

        SortUtil.sort(rules, new PriorityComparator());

        boolean isMultiline = false;
        for (int i = 0; i < rules.size(); i++)
        {
            CustomTextRule rrr = (CustomTextRule) rules.get(i);

            if (rrr.getIsMultiline())
            {
                isMultiline = true;
                break;
            }
        }

        if (isMultiline)
        {
            BufferedReader lr = new BufferedReader(new StringReader(oriStr));
            StringBuffer allStr = new StringBuffer();
            List<LineString> lines = new ArrayList<LineString>();
            String line = lr.readLine();
            int lineNumber = 1;

            while (line != null)
            {
                lines.add(new LineString(line, lineNumber));
                allStr.append(line);

                line = lr.readLine();
                ++lineNumber;

                if (line != null)
                {
                    allStr.append("\n");
                }
            }

            List<LineIndex> indexes = extractLines(lines,
                    allStr.length(), rules, null);

            if (indexes == null || indexes.size() == 0)
            {
                return "";
            }
            else
            {
                StringBuffer sb = new StringBuffer();
                int start = 0;
                for (int i = 0; i < indexes.size(); i++)
                {
                    LineIndex lineIndex = indexes.get(i);

                    String s1 = allStr.substring(lineIndex.getContentStart(),
                            lineIndex.getContentEnd());

                    if (s1 != null && s1.length() > 0)
                    {
                        sb.append(s1).append(lineSeparator);;
                    }

                    start = lineIndex.getContentEnd();
                }
                
                return sb.toString();
            }
        }
        else
        {
            BufferedReader br = new BufferedReader(new StringReader(oriStr));
            String line = br.readLine();
            StringBuffer sb = new StringBuffer();

            while (line != null)
            {
                int[] index = extractOneLine(line, rules);

                if (index == null)
                {
                    // ignore
                }
                else if (index.length == 2 && index[0] < index[1])
                {
                    String oneLine = line.substring(index[0], index[1]);
                    sb.append(oneLine).append(lineSeparator);
                }

                line = br.readLine();
            }

            return sb.toString();
        }
    }

    public static int[] extractOneLine(String line,
            List<CustomTextRuleBase> rules)
    {
        if (line == null || line.length() == 0)
        {
            return null;
        }
        
        if (rules == null)
        {
            return null;
        }

        if (rules.size() == 0)
        {
            return new int[] { 0, line.length() };
        }

        for (int i = 0; i < rules.size(); i++)
        {
            CustomTextRuleBase rule = rules.get(i);
            boolean startMatch = false;
            boolean finishMatch = false;
            int extractIndexStart = -1;
            int extractIndexFinish = -1;

            String startStr = rule.getStartString();
            String finishStr = rule.getFinishString();
            String startOcc = rule.getStartOccurrence();
            String finishOcc = rule.getFinishOccurrence();

            if (rule.getStartIsRegEx())
            {
                Pattern p = Pattern.compile(startStr);
                Matcher m = p.matcher(line);

                if (FIRST.equals(startOcc))
                {
                    if (m.find())
                    {
                        extractIndexStart = m.end();
                        startMatch = true;
                    }
                }
                else if (LAST.equals(startOcc))
                {
                    int lastEnd = -1;
                    while (m.find())
                    {
                        lastEnd = m.end();
                    }

                    if (lastEnd != -1)
                    {
                        extractIndexStart = lastEnd;
                        startMatch = true;
                    }
                }
                else
                {
                    int number = Integer.parseInt(startOcc);
                    int find = 0;
                    while (m.find())
                    {
                        find = find + 1;

                        if (find == number)
                        {
                            extractIndexStart = m.end();
                            startMatch = true;
                            break;
                        }
                    }
                }
            }
            else
            {
                if (FIRST.equals(startOcc))
                {
                    int i0 = line.indexOf(startStr);
                    if (i0 != -1)
                    {
                        extractIndexStart = i0 + startStr.length();
                        startMatch = true;
                    }
                }
                else if (LAST.equals(startOcc))
                {
                    int i0 = line.lastIndexOf(startStr);
                    if (i0 != -1)
                    {
                        extractIndexStart = i0 + startStr.length();
                        startMatch = true;
                    }
                }
                else
                {
                    int number = Integer.parseInt(startOcc);
                    int find = 0;
                    int i0 = line.indexOf(startStr);
                    while (i0 != -1)
                    {
                        find = find + 1;

                        if (find == number)
                        {
                            extractIndexStart = i0 + startStr.length();
                            startMatch = true;
                            break;
                        }

                        i0 = line.indexOf(startStr, i0 + startStr.length());
                    }
                }
            }

            if (startMatch)
            {
                int startIndex = extractIndexStart;

                // there is no more string after start string
                if (startIndex >= line.length())
                {
                    return null;
                }
                // finish string is empty, extract all after start string
                else if (finishStr == null || finishStr.length() == 0)
                {
                    extractIndexFinish = line.length();
                    finishMatch = true;
                }
                // find the index of finish string's
                else
                {
                    if (rule.getFinishIsRegEx())
                    {
                        Pattern p = Pattern.compile(finishStr);
                        Matcher m = p.matcher(line);

                        if (FIRST.equals(finishOcc))
                        {
                            if (m.find(startIndex))
                            {
                                extractIndexFinish = m.start();
                                finishMatch = true;
                            }
                        }
                        else if (LAST.equals(finishOcc))
                        {
                            int lastMatch_start = -1;
                            while (m.find(startIndex))
                            {
                                lastMatch_start = m.start();
                                startIndex = m.end();
                            }

                            if (lastMatch_start != -1)
                            {
                                extractIndexFinish = lastMatch_start;
                                finishMatch = true;
                            }
                        }
                        else
                        {
                            int number = Integer.parseInt(finishOcc);
                            int find = 0;
                            while (m.find())
                            {
                                find = find + 1;

                                if (find == number)
                                {
                                    extractIndexFinish = m.start();
                                    finishMatch =  (extractIndexFinish > extractIndexStart);
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        if (FIRST.equals(finishOcc))
                        {
                            int i0 = line.indexOf(finishStr, startIndex);
                            if (i0 != -1)
                            {
                                extractIndexFinish = i0;
                                finishMatch = true;
                            }
                        }
                        else if (LAST.equals(finishOcc))
                        {
                            String sub = line.substring(startIndex);
                            int i0 = sub.lastIndexOf(finishStr);
                            if (i0 != -1)
                            {
                                extractIndexFinish = startIndex + i0;
                                finishMatch = true;
                            }
                        }
                        else
                        {
                            int number = Integer.parseInt(finishOcc);
                            int find = 0;
                            int i0 = line.indexOf(finishStr);
                            while (i0 != -1)
                            {
                                find = find + 1;

                                if (find == number)
                                {
                                    extractIndexFinish = i0;
                                    finishMatch =  (extractIndexFinish > extractIndexStart);
                                    break;
                                }

                                i0 = line.indexOf(finishStr,
                                        i0 + finishStr.length());
                            }
                        }
                    }
                }
            }

            if (startMatch && finishMatch)
            {
                return new int[] { extractIndexStart, extractIndexFinish };
            }
        }

        return null;
    }

    public static List<LineIndex> extractLines(List<LineString> lines,
            int length, List<CustomTextRuleBase> p_customTextRules,
            List<CustomTextRuleBase> p_customSidRules)
    {
        List<LineIndex> result = new ArrayList<LineIndex>();
        int processedChars = 0;
        
        // process lines
        for(int j = 0; (j < lines.size() && processedChars < length);)
        {
            LineString lineString = lines.get(j);
            String nextString = getNextAllString(lines, j + 1);
            int extractIndexStart = -1;
            int extractIndexFinish = -1;
            boolean isMultiline = false;
            boolean startMatch = false;
            boolean finishMatch = false;
            
            // extract one line rule by rule
            for (int i = 0; i < p_customTextRules.size(); i++)
            {
                CustomTextRuleBase rule = p_customTextRules.get(i);
                startMatch = false;
                finishMatch = false;
                extractIndexStart = -1;
                extractIndexFinish = -1;
                isMultiline = rule.getIsMultiline();
                
                String line = (isMultiline ? nextString : lineString.getLine());

                String startStr = rule.getStartString();
                String finishStr = rule.getFinishString();
                String startOcc = rule.getStartOccurrence();
                String finishOcc = rule.getFinishOccurrence();

                if (rule.getStartIsRegEx())
                {
                    Pattern p = isMultiline ? Pattern.compile(startStr, Pattern.MULTILINE | Pattern.DOTALL) : Pattern.compile(startStr);
                    Matcher m = p.matcher(line);

                    if (FIRST.equals(startOcc))
                    {
                        if (m.find())
                        {
                            extractIndexStart = processedChars + m.end();
                            startMatch = true;
                        }
                    }
                    else if (LAST.equals(startOcc))
                    {
                        int lastEnd = -1;
                        while (m.find())
                        {
                            lastEnd = m.end();
                        }

                        if (lastEnd != -1)
                        {
                            extractIndexStart = processedChars + lastEnd;
                            startMatch = true;
                        }
                    }
                    else
                    {
                        int number = Integer.parseInt(startOcc);
                        int find = 0;
                        while (m.find())
                        {
                            find = find + 1;

                            if (find == number)
                            {
                                extractIndexStart = processedChars + m.end();
                                startMatch = true;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    if (FIRST.equals(startOcc))
                    {
                        int i0 = line.indexOf(startStr);
                        if (i0 != -1)
                        {
                            extractIndexStart = processedChars + i0 + startStr.length();
                            startMatch = true;
                        }
                    }
                    else if (LAST.equals(startOcc))
                    {
                        int i0 = line.lastIndexOf(startStr);
                        if (i0 != -1)
                        {
                            extractIndexStart = processedChars + i0 + startStr.length();
                            startMatch = true;
                        }
                    }
                    else
                    {
                        int number = Integer.parseInt(startOcc);
                        int find = 0;
                        int i0 = line.indexOf(startStr);
                        while (i0 != -1)
                        {
                            find = find + 1;

                            if (find == number)
                            {
                                extractIndexStart = processedChars + i0 + startStr.length();
                                startMatch = true;
                                break;
                            }

                            i0 = line.indexOf(startStr, i0 + startStr.length());
                        }
                    }
                }

                if (startMatch)
                {
                    int startIndex = extractIndexStart - processedChars;

                    // there is no more string after start string
                    if (startIndex >= line.length())
                    {
                        break;
                    }
                    // finish string is empty, extract all after start string
                    else if (finishStr == null || finishStr.length() == 0)
                    {
                        extractIndexFinish = processedChars + line.length();
                        finishMatch = true;
                    }
                    // find the index of finish string's
                    else
                    {
                        if (rule.getFinishIsRegEx())
                        {
                            Pattern p = isMultiline ? Pattern.compile(finishStr, Pattern.MULTILINE | Pattern.DOTALL) : Pattern.compile(finishStr);;
                            Matcher m = p.matcher((line));

                            if (FIRST.equals(finishOcc))
                            {
                                if (m.find(startIndex))
                                {
                                    extractIndexFinish =processedChars +  m.start();
                                    finishMatch = true;
                                }
                            }
                            else if (LAST.equals(finishOcc))
                            {
                                int lastMatch_start = -1;
                                while (m.find(startIndex))
                                {
                                    lastMatch_start = m.start();
                                    startIndex = m.end();
                                }

                                if (lastMatch_start != -1)
                                {
                                    extractIndexFinish = processedChars + lastMatch_start;
                                    finishMatch = true;
                                }
                            }
                            else
                            {
                                int number = Integer.parseInt(finishOcc);
                                int find = 0;
                                while (m.find())
                                {
                                    find = find + 1;

                                    if (find == number)
                                    {
                                        extractIndexFinish = processedChars + m.start();
                                        finishMatch =  (extractIndexFinish > extractIndexStart);
                                        break;
                                    }
                                }
                            }
                        }
                        else
                        {
                            if (FIRST.equals(finishOcc))
                            {
                                int i0 = line.indexOf(finishStr, startIndex);
                                if (i0 != -1)
                                {
                                    extractIndexFinish = processedChars + i0;
                                    finishMatch = true;
                                }
                            }
                            else if (LAST.equals(finishOcc))
                            {
                                String sub = line.substring(startIndex);
                                int i0 = sub.lastIndexOf(finishStr);
                                if (i0 != -1)
                                {
                                    extractIndexFinish = processedChars + startIndex + i0;
                                    finishMatch = true;
                                }
                            }
                            else
                            {
                                int number = Integer.parseInt(finishOcc);
                                int find = 0;
                                int i0 = line.indexOf(finishStr);
                                while (i0 != -1)
                                {
                                    find = find + 1;

                                    if (find == number)
                                    {
                                        extractIndexFinish = processedChars + i0;
                                        finishMatch =  (extractIndexFinish > extractIndexStart);
                                        break;
                                    }

                                    i0 = line.indexOf(finishStr,
                                            i0 + finishStr.length());
                                }
                            }
                        }
                    }
                }

                if (startMatch && finishMatch)
                {
                     break;
                }
            }
            
            if (startMatch && finishMatch)
            {
                LineIndex lineIndex = null;
                
                // extract sid from first line
                int pcharCount = processedChars;
                int sidStart = -1;
                int sidEnd = -1;
                int jj = j;
                if (p_customSidRules != null && p_customSidRules.size() > 0)
                {
                    // find the first line, which is not empty
                    String l0 = lineString.getLine();
                    if (l0.trim().length() == 0 && isMultiline)
                    {
                        if (jj < lines.size())
                        {
                            jj = jj + 1;
                            LineString temp = lines.get(jj);
                            l0 = temp.getLine();

                            while (l0.trim().length() == 0 && jj < lines.size())
                            {
                                jj = jj + 1;
                                temp = lines.get(jj);
                                l0 = temp.getLine();
                            }
                        }
                    }
                    
                    int[] sidIndex = extractOneLine(l0,
                            p_customSidRules);

                    if (sidIndex != null && sidIndex.length == 2)
                    {
                        for(int jjj = j; jjj < jj; jjj++)
                        {
                            LineString temp = lines.get(jjj);
                            pcharCount = temp.getLine().length() + pcharCount;
                            
                            //\n
                            pcharCount = pcharCount + 1;
                        }
                        
                        sidStart = pcharCount + sidIndex[0];
                        sidEnd = pcharCount + sidIndex[1];
                    }
                }
                
                if (isMultiline)
                {
                    lineIndex = new LineIndex(extractIndexStart,
                            extractIndexFinish);

                    int lineCount = 0;

                    String sub = nextString.substring(0, extractIndexFinish
                            - processedChars);
                    BufferedReader br = new BufferedReader(
                            new StringReader(sub));
                    try
                    {
                        while (br.readLine() != null)
                        {
                            lineCount = lineCount + 1;
                        }
                    }
                    catch (IOException e)
                    {
                        // ignore
                    }

                    int lastJ = j;
                    j = j + lineCount;

                    while (lastJ < j)
                    {
                        LineString _lineString = lines.get(lastJ);
                        processedChars = processedChars
                                + _lineString.getLine().length() + 1;
                        
                        lastJ++;
                    }
                }
                else
                {
                    lineIndex = new LineIndex(extractIndexStart,
                            extractIndexFinish);

                    processedChars = processedChars
                            + lineString.getLine().length() + 1;
                    j = j + 1;
                }
                
                lineIndex.setSidStart(sidStart);
                lineIndex.setSidEnd(sidEnd);
                
                result.add(lineIndex);
            }
            else
            {
                processedChars = processedChars + lineString.getLine().length() + 1;
                j = j + 1;
            }
            
        }
        
        return result;
    }
    
    private static String getNextAllString(List<LineString> lines, int startLine)
    {
        StringBuffer sb = new StringBuffer();
        
        for (LineString lineString : lines)
        {
            if (lineString.getLineNumber() >= startLine)
            {
                sb.append(lineString.getLine());
                
                if (lineString.getLineNumber() < lines.size())
                {
                    sb.append("\n");
                }
            }
        }
        
        return sb.toString();
    }
}
