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
package com.globalsight.ling.docproc.extractor.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;

/**
 * <P>
 * Provides the JavaCC-generated Parser class with a simple HTML object model
 * allowing tags inside tags for ColdFusion (and later XSP).
 * </P>
 * 
 * Overview:
 * 
 * HtmlElement | |-------- SimpleTag -- CFTag -- CFScript | | | \----- CFQuery |
 * |-------- Tag -------- Script | | | |---------- Style | | | \---------- Java
 * | |-------- EndTag | |-------- Xsp (to be moved under SimpleTag) | |--------
 * Comment ---- CFComment | |-------- PidComment | |-------- Declaration |
 * \-------- ProcessingInstruction
 * 
 * 
 * SimpleTag carries an AttributeList. Tag carries an ExtendedAttributeList.
 */
public interface HtmlObjects
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
            return (hasValue ? name.length() + 1 + value.length() : name
                    .length());
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
     * A simple attribute list that contains only objects of type Attribute.
     */
    public static class AttributeList extends ArrayList
    {
        private static final long serialVersionUID = 4796666070683308322L;

        public void addAttribute(Attribute a)
        {
            this.add(a);
        }

        public String toString()
        {
            StringBuffer res = new StringBuffer();
            boolean first = true;

            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Attribute o = (Attribute) it.next();

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
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Attribute attr = (Attribute) it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return attr;
                }
            }

            return null;
        }

        public boolean isDefined(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Attribute attr = (Attribute) it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return true;
                }
            }

            return false;
        }

        public boolean hasValue(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Attribute attr = (Attribute) it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return attr.hasValue();
                }
            }

            return false;
        }

        public String getValue(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Attribute attr = (Attribute) it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    return attr.getValue();
                }
            }

            return "";
        }

        public void removeAttribute(String p_attrib)
        {
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Attribute attr = (Attribute) it.next();

                if (attr.getName().equalsIgnoreCase(p_attrib))
                {
                    it.remove();
                    return;
                }
            }
        }
    }

    /**
     * ExtendedAttributeList holds a list of normal Attributes and embedded
     * SimpleTags. Any text nodes inside SimpleTags will be parsed as Attributes
     * without value by the HTML grammar. Later we may want to add Comments (or
     * CFComments) to this list to support ColdFusion 5.0's generous occurences
     * of comments.
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

            for (Iterator it = this.iterator(); it.hasNext();)
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
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Object o = it.next();

                if (o instanceof Attribute)
                {
                    Attribute attr = (Attribute) o;

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
            for (Iterator it = this.iterator(); it.hasNext();)
            {
                Object o = (Attribute) it.next();

                if (o instanceof Attribute)
                {
                    Attribute attr = (Attribute) o;

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
    public static abstract class HtmlElement
    {
        /**
         * Helper field for tmx pairing: a tag is paired if it has a
         * corresponding opening or closing tag in a segment.
         */
        public boolean isPaired = false;

        /**
         * Helper field for tmx pairing: if a tag is paired, this is a unique
         * int that links both tags, like the TMX "i" attribute.
         */
        public int partnerId = -1;

        /**
         * Helper field for tmx pairing: a tag is isolated if it must be a
         * paired tag but has no pairing partner in a segment.
         */
        public boolean isIsolated = false;

        // Flag for checking whether the tag or text is from the office embedded
        // HTML contents or the raw HTML content.
        // Default false, indicates from raw HTML content. (for GBS-2073)
        public boolean isFromOfficeContent = false;

        // Flag for checking whether the tag has been put into translatable or
        // skeleton.
        public boolean isInTranslatable = false;

        /**
         * Flag for checking whether the tag or text is included in internal
         * style
         */
        public boolean isInternalStyleContent = false;
    }

    /**
     * A simple tag with simple attribute list, which can be added to extended
     * attribute lists. Also represents a base class for all ColdFusion and
     * other tags that themselves cannot be interrupted by other tags.
     */
    public static class SimpleTag extends HtmlElement
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
     * Coldfusion start tag. An intermediate base class from which all special
     * ColdFusion tags are derived (CFScript and CFQuery).
     */
    public static class CFTag extends SimpleTag
    {
        public String original;

        public CFTag(String t, AttributeList a, boolean b, String o)
        {
            super(t, a, b);

            original = o;
        }

        public String toString()
        {
            return original;
        }
    }

    /**
     *
     */
    public static class EmbeddedXspTag extends SimpleTag
    {
        public String original;

        public EmbeddedXspTag(String t, String o)
        {
            super(t, null, false);

            original = o;
        }

        public String toString()
        {
            return original;
        }
    }

    public static class CFScript extends CFTag
    {
        public String text;
        public int iLine; // line&col of tag start
        public int iCol;

        public CFScript(String t, AttributeList a, boolean b, String o,
                String p_script, int p_line, int p_col)
        {
            super(t, a, b, o);
            text = p_script;
            iLine = p_line;
            iCol = p_col;
        }

        public String getText()
        {
            return text;
        }
    }

    public static class CFQuery extends CFTag
    {
        public String text;
        public int iLine; // line&col of tag start
        public int iCol;

        public CFQuery(String t, AttributeList a, boolean b, String o,
                String p_text, int p_line, int p_col)
        {
            super(t, a, b, o);
            text = p_text;
            iLine = p_line;
            iCol = p_col;
        }

        public String getText()
        {
            return text;
        }
    }

    /**
     * Represents a "<%...%>" JSP/ASP tag.
     */
    public static class Xsp extends HtmlElement
    {
        public String text;
        public int iLine; // line&col of tag start
        public int iCol;

        public Xsp(String s, int p_line, int p_col)
        {
            super();
            text = s;
            iLine = p_line;
            iCol = p_col;
        }

        public String getText()
        {
            return text;
        }

        public String toString()
        {
            return "<%" + text + "%>";
        }
    }

    /**
     * Represents a HTML start tag. Stores the tag name and a list of extended
     * attributes, which can contain other tags (like ColdFusion or XSP tags).
     * If isClosed == true, this tag was closed ("/>").
     * 
     * An intermediate base class from which all special HTML tags are derived
     * (Script, Style, and Java).
     */
    public static class Tag extends HtmlElement
    {
        public String tag;
        public ExtendedAttributeList attributes;
        public boolean isClosed;
        public String original;
        public int iLine; // line&col of tag start
        public int iCol;
        private boolean ignore;

        public boolean isMerged;

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

        public boolean isIgnore()
        {
            return ignore;
        }

        public void setIgnore(boolean ignore)
        {
            this.ignore = ignore;
        }
    }

    public static class Script extends Tag
    {
        public String text;

        public Script(String t, ExtendedAttributeList a, boolean b, String o,
                String s, int l, int c)
        {
            super(t, a, b, o, l, c);
            text = s;
        }
    }

    public static class Java extends Tag
    {
        public String text;

        public Java(String t, ExtendedAttributeList a, boolean b, String o,
                String s, int l, int c)
        {
            super(t, a, b, o, l, c);
            text = s;
        }
    }

    public static class Style extends Tag
    {
        public String text;

        public Style(String t, ExtendedAttributeList a, boolean b, String o,
                String s, int l, int c)
        {
            super(t, a, b, o, l, c);
            text = s;
        }
    }

    /**
     * HTML end tag. Stores only the tag name.
     */
    public static class EndTag extends HtmlElement
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
     * HTML comments.
     */
    public static class Comment extends HtmlElement
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
     * Same as Comment but cannot inherit from it like CfComment does because
     * PidComments must be preserved inside segments and we'd have to modify
     * ExtractionHandler too much.
     */
    public static class PidComment extends HtmlElement
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
     * ColdFusion comments using three dashes and allowing embedding.
     */
    public static class CfComment extends Comment
    {
        public CfComment(String c)
        {
            super(c);
        }

        public String toString()
        {
            return "<!---" + comment + "--->";
        }
    }

    /**
     * HTML declarations and MS Office conditional comments.
     */
    public static class Declaration extends HtmlElement
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
     * HTML/SGML/XML processing instructions. The inner value is the PI
     * including the trailing "?". See the HTML grammar.
     */
    public static class PI extends HtmlElement
    {
        public String instruction;

        public PI(String d)
        {
            super();
            instruction = d;
        }

        public String toString()
        {
            return "<?" + instruction + ">"; // see html grammar
        }
    }

    /**
     * Plain text (PCDATA) node.
     */
    public static class Text extends HtmlElement
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
    public static class Newline extends HtmlElement
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

    /**
     * Internal Text
     */
    public static class InternalText extends HtmlElement
    {
        public String tag;

        public String internalText;

        public String original;

        public InternalText(String ori)
        {
            super();
            original = ori;
            parseString();
        }

        private void parseString()
        {
            Pattern p = Pattern.compile(InternalTextHelper.REG_INTERNAL_TEXT);
            Matcher m = p.matcher(original);
            if (m.find())
            {
                tag = m.group(1);
                internalText = m.group(2);
            }
        }

        public String toString()
        {
            return original;
        }
    }
}
