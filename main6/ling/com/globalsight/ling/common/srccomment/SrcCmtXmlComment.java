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

package com.globalsight.ling.common.srccomment;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SrcCmtXmlComment
{
    private String name = null;
    private boolean isRE = false;

    public SrcCmtXmlComment()
    {
    }

    public SrcCmtXmlComment(String name, boolean isRE)
    {
        this.name = name;
        this.isRE = isRE;
    }

    public String getName()
    {
        return name;
    }

    public boolean isRE()
    {
        return isRE;
    }

    public void setRE(boolean isRE)
    {
        this.isRE = isRE;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return this.name + "|isRE=" + this.isRE;
    }

    // //////////////////////////////////////////////
    // static methods
    // //////////////////////////////////////////////

    public static SrcCmtXmlComment initFromElement(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("aName").item(0);
        String name = tagNameElement.getFirstChild().getNodeValue();

        NodeList isREElements = tagElement.getElementsByTagName("isRE");
        boolean isRE = false;
        if (isREElements != null && isREElements.getLength() > 0)
        {
            isRE = "true".equals(isREElements.item(0).getFirstChild().getNodeValue());
        }

        SrcCmtXmlComment sc = new SrcCmtXmlComment(name, isRE);

        return sc;
    }

    public static String getSrcCommentContent(List<SrcCmtXmlComment> srcComments, String text)
    {
        String matchedText = null;

        if (srcComments != null && srcComments.size() > 0 && text != null)
        {
            text = text.trim();

            for (SrcCmtXmlComment sc : srcComments)
            {
                if (sc.isRE())
                {
                    Pattern p = Pattern.compile(sc.getName());
                    Matcher m = p.matcher(text);
                    if (m.matches())
                    {
                        String temp = null;
                        try
                        {
                            temp = m.group(1);
                        }
                        catch (Exception e)
                        {
                            temp = m.group();
                        }

                        matchedText = updateSrcComment(matchedText, temp);
                    }
                }
                else
                {
                    if (text.contains(sc.getName()))
                    {
                        matchedText = updateSrcComment(matchedText, sc.getName());
                    }
                }
            }
        }

        return matchedText;
    }

    private static String updateSrcComment(String matchedText, String newValue)
    {
        if (matchedText == null || matchedText.length() == 0)
        {
            return newValue;
        }
        else if (newValue == null || newValue.length() == 0)
        {
            return matchedText;
        }
        else if (matchedText.contains(newValue))
        {
            return matchedText;
        }
        else
        {
            return newValue;
        }
    }
}
