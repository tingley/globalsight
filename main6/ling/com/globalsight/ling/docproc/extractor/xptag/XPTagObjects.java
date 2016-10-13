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
package com.globalsight.ling.docproc.extractor.xptag;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <P>Provides a simple XPTag object model to the JavaCC-generated
 * Parser class and the extraction handler.</P>
 */
public interface XPTagObjects
{
    /** Version tag: &lt;v3.00&gt; */
    public static class Version
    {
        public String version;

        public Version(String s)
        {
            version = s;
        }

        public String toString()
        {
            return version;
        }
    }

    /** Encoding tag: &lt;e1gt; */
    public static class Encoding
    {
        public String encoding;

        public Encoding(String s)
        {
            encoding = s;
        }

        public String toString()
        {
            return encoding;
        }
    }

    /** Definition of a style sheet: @name=[...]&lt;...&gt; */
    public static class StyleDefinition
    {
        public String style;

        public StyleDefinition(String s)
        {
            style = s;
        }

        public String toString()
        {
            return style;
        }
    }

    /** Use of a style sheet: @name: */
    public static class StyleSelection
    {
        public String style;

        public StyleSelection(String s)
        {
            style = s;
        }

        public String toString()
        {
            return style;
        }
    }

    public static class Tag
    {
        public String tag;

        public Tag(String t)
        {
            tag = t;
        }

        public String toString()
        {
            return tag;
        }
    }

    /** Paragraph style &lt;*....&gt; */
    public static class ParaTag extends Tag
    {
        public ParaTag(String t)
        {
            super(t);
        }
    }

    /** Character style &lt;....&gt; */
    public static class CharTag extends Tag
    {
        public boolean isPaired = false;
        public boolean isIsolated = false;

        public CharTag(String t)
        {
            super(t);
        }
    }

    /** Special characters &lt;\\...&gt; */
    public static class SpecialTag extends Tag
    {
        public SpecialTag(String t)
        {
            super(t);
        }
    }

    /**
     * Plain text (PCDATA) node.
     */
    public static class Text
    {
        public String text;

        public Text(String t)
        {
            text = t;
        }

        public String toString()
        {
            return text;
        }
    }

    /**
     * End of line indicator (paragraph break).
     */
    public static class Newline
    {
        public String text;

        public Newline(String n)
        {
            text = n;
        }

        public String toString()
        {
            return text;
        }
    }
}
