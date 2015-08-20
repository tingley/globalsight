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
package com.globalsight.ling.docproc.extractor.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Utility class used in the XML Extractor.
 */
public abstract class OutputWriter
{
    public static final int NO_OUTPUT = 0;
    public static final int SKELETON = 1;
    public static final int TRANSLATABLE = 2;
    public static final int LOCALIZABLE = 3;

    private static final REProgram COMMENTS_SEARCH_PATTERN = createCommentsSearchPattern("^\\s*(<ph type=\"comment\">[^<]*?</ph>\\s*)*$");

    private static final REProgram OPENING_PH = createCommentsSearchPattern("<ph type=\"comment\">");

    private static final REProgram CLOSING_PH = createCommentsSearchPattern("</ph>");

    private String sid = null;
    private XmlFilterHelper m_xmlFilterHelper = null;
    private boolean m_isPreserveWS = false;

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public XmlFilterHelper getXmlFilterHelper()
    {
        return m_xmlFilterHelper;
    }

    public void setXmlFilterHelper(XmlFilterHelper xmlFilterHelper)
    {
        this.m_xmlFilterHelper = xmlFilterHelper;
    }

    public boolean isPreserveWhiteSpace()
    {
        return m_isPreserveWS;
    }

    public void setPreserveWhiteSpace(boolean isPreserveWS)
    {
        m_isPreserveWS = isPreserveWS;
    }

    public static REProgram createCommentsSearchPattern(String p_pattern)
    {
        REProgram pattern = null;
        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }
        return pattern;
    }

    abstract public void flush();

    abstract public int getOutputType();

    abstract public void append(String content);

    protected boolean isXmlCommentsOnly(String p_content)
    {
        RE matcher = new RE(COMMENTS_SEARCH_PATTERN, RE.MATCH_SINGLELINE);
        return matcher.match(p_content);
    }

    protected boolean isTmxTagsOnly(String p_content)
    {
        Pattern p = Pattern.compile("<sub[^>]*>([^<]*?)</sub>");
        Matcher m = p.matcher(p_content);
        while (m.find())
        {
            String s = m.group(1);
            if (!Text.isBlank(s))
                return false;
        }

        DiplomatSegmenter seg = new DiplomatSegmenter();
        String noTags = seg.removeTags(p_content);

        if (noTags == null)
        {
            return true;
        }
        else
        {
            return Text.isBlank(noTags);
        }
    }

    protected String removePhs(String p_content)
    {
        RE matcher = new RE();
        matcher.setProgram(OPENING_PH);
        matcher.setMatchFlags(RE.MATCH_SINGLELINE);
        String substed = matcher.subst(p_content, "");

        matcher.setProgram(CLOSING_PH);
        matcher.setMatchFlags(RE.MATCH_SINGLELINE);
        return matcher.subst(substed, "");
    }

    protected String getPrefixBlank(String str)
    {
        if (str == null || str.length() == 0)
            return "";

        StringBuffer preBlank = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t')
            {
                preBlank.append(c);
            }
            else
            {
                break;
            }
        }
        return preBlank.toString();
    }

    protected String getSuffixBlank(String str)
    {
        if (str == null || str.length() == 0)
            return "";

        StringBuffer suffixBlank = new StringBuffer();
        for (int i = str.length() - 1; i >= 0; i--)
        {
            char c = str.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t')
            {
                suffixBlank.insert(0, c);
            }
            else
            {
                break;
            }
        }
        return suffixBlank.toString();
    }
}
