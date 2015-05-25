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
package com.globalsight.ling.docproc.extractor.xliff;

import java.util.Map;

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

    /**
     * For xliff source file,target content may be blank.In this case, the blank
     * target content should be added to translatable instead of skeleton.
     * 
     * Default "true",this is the default behaviour.
     */
    private boolean blankTextAsSkeleton = true;

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public void setBlankTextAsSkeleton(boolean p_blankTextAsSkeleton)
    {
        this.blankTextAsSkeleton = p_blankTextAsSkeleton;
    }

    public boolean getBlankTextAsSkeleton()
    {
        return this.blankTextAsSkeleton;
    }

    private static REProgram createCommentsSearchPattern(String p_pattern)
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

    abstract public void setXliffTransPart(Map attributes);

    protected boolean isXmlCommentsOnly(String p_content)
    {
        RE matcher = new RE(COMMENTS_SEARCH_PATTERN, RE.MATCH_SINGLELINE);
        return matcher.match(p_content);
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

}
