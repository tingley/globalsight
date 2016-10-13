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
//===============================================================
// package : com.tetrasix.majix.rtf
// class : com.tetrasix.majix.RtfStyleSheet
//===============================================================
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is TetraSys code.
//
// The Initial Developer of the Original Code is TetraSys..
// Portions created by TetraSys are
// Copyright (C) 1998-2000 TetraSys. All Rights Reserved.
//
// Contributor(s):
//===============================================================
package com.globalsight.ling.rtf;


import java.io.PrintWriter;

import java.util.Iterator;
import java.util.TreeMap;

public class RtfStyleSheet
{
    private TreeMap m_styles;

    public RtfStyleSheet()
    {
        m_styles = new TreeMap ();
    }

    public void defineParagraphStyle(int code, String name, int next,
        int basedon, RtfTextProperties props)
    {
        m_styles.put(new Integer(code),
            new ParagraphStyleDefinition(code, name, next, basedon, props));
    }

    public void defineCharacterStyle(int code, String name, int next,
        int basedon, boolean isAdditive, RtfTextProperties props)
    {
        m_styles.put(new Integer(code),
            new CharacterStyleDefinition(code, name, next, basedon,
                isAdditive, props));
    }

    public StyleDefinition getStyle(int code)
    {
        StyleDefinition style =
            (StyleDefinition)m_styles.get(new Integer(code));

        return style;
    }

    public String getStyleName(int code)
    {
        StyleDefinition style =
            (StyleDefinition)m_styles.get(new Integer(code));

        if (style != null)
        {
            return style.getName();
        }
        else
        {
            return null;
        }
    }

    public boolean isParagraphStyle(int code)
    {
        StyleDefinition style =
            (StyleDefinition)m_styles.get(new Integer(code));

        return isParagraphStyle(style);
    }

    public boolean isParagraphStyle(StyleDefinition style)
    {
        if (style != null)
        {
            return style instanceof ParagraphStyleDefinition;
        }

        return false;
    }

    public boolean isCharacterStyle(int code)
    {
        StyleDefinition style =
            (StyleDefinition)m_styles.get(new Integer(code));

        return isCharacterStyle(style);
    }

    public boolean isCharacterStyle(StyleDefinition style)
    {
        if (style != null)
        {
            return style instanceof CharacterStyleDefinition;
        }

        return false;
    }

    public RtfTextProperties getTextProperties(int code)
    {
        StyleDefinition style =
            (StyleDefinition)m_styles.get(new Integer(code));

        if (style != null)
        {
            return style.getTextProperties();
        }
        else
        {
            return new RtfTextProperties();
        }
    }

    /**
     * Styles can inherit from each other, so this method returns the
     * text properties that are ultimately in effect when a given
     * style is in use.
     */
    public RtfTextProperties getEffectiveTextProperties(int code)
    {
        RtfTextProperties result = null;

        StyleDefinition style =
            (StyleDefinition)m_styles.get(new Integer(code));
        StyleDefinition basedOnStyle;

        if (style != null)
        {
            result = style.getTextProperties();
            result = (RtfTextProperties)result.clone();

            RtfTextProperties props = style.getTextProperties();

            while (style.isBasedOn())
            {
                basedOnStyle = (StyleDefinition)m_styles.get(
                    new Integer(style.getBasedOn()));

                if (basedOnStyle == null)
                {
                    break;
                }

                result = result.merge(basedOnStyle.getTextProperties());

                style = basedOnStyle;
            }
        }

        if (result == null)
        {
            return new RtfTextProperties();
        }

        return result;
    }

    public Iterator getStyleDefinitions()
    {
        return m_styles.values().iterator();
    }

    public void Dump(PrintWriter out)
    {
        out.println("<stylesheet>");

        Iterator styles = m_styles.values().iterator();
        while (styles.hasNext())
        {
            StyleDefinition style = (StyleDefinition)styles.next();
            style.Dump(out);
        }

        out.println("</stylesheet>");
    }

    public abstract class StyleDefinition
    {
        int _code;
        String _styleName;
        int _next;
        int _basedon;
        boolean _additive;
        RtfTextProperties _textProperties;

        StyleDefinition(int code, String name, int next, int basedon,
            RtfTextProperties textProperties)
        {
            _code           = code;
            _next           = next;
            _basedon        = basedon;
            _styleName      = name;
            _additive       = false;
            _textProperties = textProperties;
        }

        StyleDefinition(int code, String name, int next, int basedon,
            boolean isAdditive, RtfTextProperties textProperties)
        {
            _code           = code;
            _next           = next;
            _basedon        = basedon;
            _styleName      = name;
            _additive       = isAdditive;
            _textProperties = textProperties;
        }

        public int getCode()
        {
            return _code;
        }

        public int getNext()
        {
            return _next;
        }

        public boolean isBasedOn()
        {
            return _basedon != -1;
        }

        public int getBasedOn()
        {
            return _basedon;
        }

        public String getName()
        {
            return _styleName;
        }

        public boolean isAdditive()
        {
            return _additive;
        }

        public RtfTextProperties getTextProperties()
        {
            return _textProperties;
        }

        abstract public void Dump(PrintWriter out);
    }

    public class ParagraphStyleDefinition
        extends StyleDefinition
    {
        ParagraphStyleDefinition(int code, String name, int next, int basedon,
            RtfTextProperties textProperties)
        {
            super(code, name, next, basedon, textProperties);
        }

        public void Dump(PrintWriter out)
        {
            out.println("<style code=\"" + _code + "\"" +
                ((_textProperties.isBold()) ? " bold=\"1\"" : "") +
                ((_textProperties.isItalic()) ? " italic=\"1\"" : "") +
                ((_textProperties.isHidden()) ? " hidden=\"1\"" : "") +
                ((_textProperties.isUnderlined()) ? " underlined=\"1\"" : "") +
                ((_textProperties.getColor() == -1) ? " color=\"" +
                    _textProperties.getColorName() + "\"" : "") +
                ((_textProperties.getFont() != -1) ? " font=\"" +
                    _textProperties.getFont() + "\"" : "") +
                ((_textProperties.getFontSize() != -1) ? " fontSize=\"" +
                    _textProperties.getFontSize() + "\"" : "") +
                ((_textProperties.getLang() != -1) ? " lang=\"" +
                    _textProperties.getLang() + "\"" : "") +
                ">" + _styleName + "</style>");
        }
    }

    public class CharacterStyleDefinition
        extends StyleDefinition
    {
        CharacterStyleDefinition(int code, String name, int next, int basedon,
            boolean isAdditive, RtfTextProperties textProperties)
        {
            // TODO: Maybe character styles are based on paragraph
            // style 0 by default, instead of the document default "no
            // style" 222?
            super(code, name, next, basedon, isAdditive, textProperties);
        }

        public void Dump(PrintWriter out)
        {
            out.println("<cstyle code=\"" + _code + "\"" +
                ((_textProperties.isBold()) ? " bold=\"1\"" : "") +
                ((_textProperties.isItalic()) ? " italic=\"1\"" : "") +
                ((_textProperties.isHidden()) ? " hidden=\"1\"" : "") +
                ((_textProperties.isUnderlined()) ? " underlined=\"1\"" : "") +
                ((_textProperties.getColor() != -1) ? " color=\"" +
                    _textProperties.getColorName() + "\"" : "") +
                ((_textProperties.getFont() != -1) ? " font=\"" +
                    _textProperties.getFont() + "\"" : "") +
                ((_textProperties.getFontSize() != -1) ? " fontSize=\"" +
                    _textProperties.getFontSize() + "\"" : "") +
                ((_textProperties.getLang() != -1) ? " lang=\"" +
                    _textProperties.getLang() + "\"" : "") +
                ">" + _styleName + "</cstyle>");
        }
    }
}
