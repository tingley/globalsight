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
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.util.SortUtil;
import com.globalsight.util.TagIndex;

public class CustomTextRuleHelper
{
    private static final Logger CATEGORY = Logger
            .getLogger(CustomTextRuleHelper.class);
    private static XmlEntities m_xmlEncoder = new XmlEntities();

    public static String FIRST = "FIRST";
    public static String LAST = "LAST";

    public static String extractLines(String oriStr,
            List<CustomTextRule> rules, String lineSeparator) throws Exception
    {
        if (rules.size() == 0)
        {
            return oriStr;
        }

        SortUtil.sort(rules, new PriorityComparator());
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
            else if (index.length == 2)
            {
                String oneLine = line.substring(index[0], index[1]);
                sb.append(oneLine).append(lineSeparator);
            }

            line = br.readLine();
        }

        return sb.toString();
    }

    public static int[] extractOneLine(String line, List<CustomTextRule> rules)
    {
        if (line == null || line.length() == 0)
        {
            return null;
        }

        if (rules.size() == 0)
        {
            return new int[] { 0, line.length() };
        }

        for (int i = 0; i < rules.size(); i++)
        {
            CustomTextRule rule = rules.get(i);
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
                    Matcher lastMatch = null;
                    while (m.find())
                    {
                        lastMatch = m;
                    }

                    if (lastMatch != null)
                    {
                        extractIndexStart = lastMatch.end();
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

                        i0 = line.indexOf(startStr, i0);
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
                            while (m.find(startIndex))
                            {
                                find = find + 1;
                                startIndex = m.end();

                                if (find == number)
                                {
                                    extractIndexFinish = m.start();
                                    finishMatch = true;
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
                            int i0 = line.indexOf(finishStr, startIndex);
                            while (i0 != -1)
                            {
                                find = find + 1;

                                if (find == number)
                                {
                                    extractIndexFinish = i0;
                                    finishMatch = true;
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
}
