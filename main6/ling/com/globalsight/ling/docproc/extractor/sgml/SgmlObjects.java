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
package com.globalsight.ling.docproc.extractor.sgml;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <P>Provides the JavaCC-generated Parser class with a simple SGML
 * object model.</P>
 *
 * Overview:
 *
 * SgmlElement
 *     |
 *     |-------- SimpleTag
 *     |
 *     |-------- Tag -------- Script
 *     |          |
 *     |          |---------- Style
 *     |          |
 *     |          \---------- Java
 *     |
 *     |-------- EndTag
 *     |
 *     |-------- Comment
 *     |
 *     |-------- Declaration
 *     |
 *     \-------- ProcessingInstruction
 *
 */
public interface SgmlObjects
{
    /**
     * A simple attribute with a NAME and possibly a VALUE.
     */
    public static class Attribute
    {
        public String name;
        public String value;
        public boolean hasValue;

        public Attribute(String n)
        {
            name = n;
            hasValue = false;
        }

        public Attribute(String n, String v)
        {
            name = n;
            value = v;
            hasValue = true;
        }

        public int getLength()
        {
            return (hasValue ? name.length() + 1 + value.length() :
                name.length());
        }

        public String toString()
        {
            return (hasValue ? name + "=" + value : name);
        }

        public boolean hasValue()
        {
            return hasValue;
        }

        public String getValue()
        {
            return (hasValue ? value : "");
        }

        public String getName()
        {
            return name;
        }

        public void deleteValue()
        {
            value = null;
            hasValue = false;
        }
    }

    /**
     * A simple attribute list that contains only objects of type
     * Attribute.
     */
    public static class AttributeList extends ArrayList
    {
        public void addAttribute(Attribute a)
        {
            this.add(a);
        }

        public String toString()
        {
            StringBuffer res = new StringBuffer();
            boolean first = true;

            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Attribute o = (Attribute)it.next();

                if (!first)
                {
                    res.append(" ");
                }
                first = false;

                res.append(o.toString());
            }

            return res.toString();
        }

        public Attribute getAttribute(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Attribute attr = (Attribute)it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return attr;
                }
            }

            return null;
        }

        public boolean isDefined(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Attribute attr = (Attribute)it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return true;
                }
            }

            return false;
        }

        public boolean hasValue(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Attribute attr = (Attribute)it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return attr.hasValue();
                }
            }

            return false;
        }

        public String getValue(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Attribute attr = (Attribute)it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return attr.getValue();
                }
            }

            return "";
        }

        public void removeAttribute(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Attribute attr = (Attribute)it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    it.remove();
                    return;
                }
            }
        }
    }

    /**
     * ExtendedAttributeList holds a list of normal Attributes and
     * embedded SimpleTags. Any text nodes inside SimpleTags will be
     * parsed as Attributes without value by the SGML grammar.
     */
    public static class ExtendedAttributeList extends AttributeList
    {
        // addAttribute inherited from AttributeList.

        public void addSimpleTag(SimpleTag t)
        {
            this.add(t);
        }

        public void addEndTag(EndTag t)
        {
            this.add(t);
        }

        public String toString()
        {
            StringBuffer res = new StringBuffer();
            boolean first = true;

            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Object o = it.next();

                if (!first)
                {
                    res.append(" ");
                }
                first = false;

                res.append(o.toString());
            }

            return res.toString();
        }

        public boolean isDefined(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Object o = it.next();

                if (o instanceof Attribute)
                {
                    Attribute attr = (Attribute)o;

                    if (attr.getName().equalsIgnoreCase(p_attrib))
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        public String getValue(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext(); )
            {
                Object o = (Attribute)it.next();

                if (o instanceof Attribute)
                {
                    Attribute attr = (Attribute)o;

                    if (attr.getName().equalsIgnoreCase(p_attrib))
                    {
                        return attr.getValue();
                    }
                }
            }

            return "";
        }
    }

    /**
     * The base class for all simple and extended tags.
     */
    public static abstract class SgmlElement
    {
        /**
         * Helper field for tmx pairing: a tag is paired if it has a
         * corresponding opening or closing tag in a segment.
         */
        public boolean isPaired = false;

        /**
         * Helper field for tmx pairing: if a tag is paired, this is a
         * unique int that links both tags, like the TMX "i"
         * attribute.
         */
        public int partnerId = -1;

        /**
         * Helper field for tmx pairing: a tag is isolated if it must
         * be a paired tag but has no pairing partner in a segment.
         */
        public boolean isIsolated = false;
    }

    /**
     * A simple tag with simple attribute list, which can be added to
     * extended attribute lists.  Also represents a base class for all
     * ColdFusion and other tags that themselves cannot be interrupted
     * by other tags.
     */
    public static class SimpleTag extends SgmlElement
    {
        public String tag;
        public AttributeList attributes;
        public boolean isClosed;

        public SimpleTag(String t, AttributeList a, boolean b)
        {
            tag = t;
            attributes = a;
            isClosed = b;
        }

        public String toString()
        {
            StringBuffer res = new StringBuffer();

            res.append("<");
            res.append(tag);

            if (attributes.size() > 0)
            {
                res.append(" ");
                res.append(attributes.toString());
            }

            if (isClosed)
            {
                res.append("/>");
            }
            else
            {
                res.append(">");
            }

            return res.toString();
        }

        public boolean isDefinedAttribute(String p_attrib)
        {
            return attributes.isDefined(p_attrib);
        }
    }

    /**
     * Represents a SGML start tag. Stores the tag name and a list of
     * extended attributes, which can contain other tags (like
     * ColdFusion or XSP tags). If isClosed == true, this tag was
     * closed ("/>").
     *
     * An intermediate base class from which all special SGML tags are
     * derived (Script, Style, and Java).
     */
    public static class Tag extends SgmlElement
    {
        public String tag;
        public ExtendedAttributeList attributes;
        public boolean isClosed;
        public String original;
        public int iLine;                       // line&col of tag start
        public int iCol;

        public Tag(String t, ExtendedAttributeList a, boolean b, String o,
            int p_line, int p_col)
        {
            super();
            tag = t;
            attributes = a;
            isClosed = b;
            original = o;
            iLine = p_line;
            iCol = p_col;
        }

        public String toString()
        {
            return original;
        }

        public boolean isDefinedAttribute(String p_attrib)
        {
            return attributes.isDefined(p_attrib);
        }
    }

    public static class Script extends Tag
    {
        public String text;

        public Script(String t, ExtendedAttributeList a, boolean b,
            String o, String s, int l, int c)
        {
            super(t, a, b, o, l, c);
            text = s;
        }
    }

    public static class Java extends Tag
    {
        public String text;

        public Java(String t, ExtendedAttributeList a, boolean b,
            String o, String s, int l, int c)
        {
            super(t, a, b, o, l, c);
            text = s;
        }
    }

    public static class Style extends Tag
    {
        public String text;

        public Style (String t, ExtendedAttributeList a, boolean b,
            String o, String s, int l, int c)
        {
            super(t, a, b, o, l, c);
            text = s;
        }
    }

    /**
     * SGML end tag.  Stores only the tag name.
     */
    public static class EndTag extends SgmlElement
    {
        public String tag;

        public EndTag(String t)
        {
            super();
            tag = t;
        }

        public String toString()
        {
            return "</" + tag + ">";
        }
    }

    /**
     * SGML comments.
     */
    public static class Comment extends SgmlElement
    {
        public String comment;

        public Comment(String c)
        {
            super();
            comment = c;
        }

        public String getComment()
        {
            return comment;
        }

        public String toString()
        {
            return "<!--" + comment + "-->";
        }
    }

    /**
     * Same as Comment but cannot inherit from it like CfComment does
     * because PidComments must be preserved inside segments and we'd
     * have to modify ExtractionHandler too much.
     */
    public static class PidComment extends SgmlElement
    {
        public String comment;

        public PidComment(String c)
        {
            super();
            comment = c;
        }

        public String getComment()
        {
            return comment;
        }

        public String toString()
        {
            return "<!--" + comment + "-->";
        }
    }

    /**
     * SGML declarations.
     */
    public static class Declaration extends SgmlElement
    {
        public String declaration;

        public Declaration(String d)
        {
            super();
            declaration = d;
        }

        public String toString()
        {
            return "<!" + declaration + ">";
        }
    }

    /**
     * HTML/SGML/XML processing instructions. The inner value is the
     * PI including the trailing "?". See the SGML grammar.
     */
    public static class PI extends SgmlElement
    {
        public String instruction;

        public PI(String d)
        {
            super();
            instruction = d;
        }

        public String toString()
        {
            return "<?" + instruction + ">"; // see sgml grammar
        }
    }

    /**
     * Plain text (PCDATA) node.
     */
    public static class Text extends SgmlElement
    {
        public String text;

        public Text(String t)
        {
            super();
            text = t;
        }

        public String toString()
        {
            return text;
        }
    }

    /**
     * End of line indicator.
     */
    public static class Newline extends SgmlElement
    {
        public String text;

        public Newline(String n)
        {
            super();
            text = n;
        }

        public String toString()
        {
            return text;
        }
    }
}
