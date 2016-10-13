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
// class : com.tetrasix.majix.RtfTextProperties
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

public class RtfTextProperties
    implements Cloneable
{
    BooleanTriState _bold = new BooleanTriState(false);
    BooleanTriState _italic = new BooleanTriState(false);
    BooleanTriState _underlined = new BooleanTriState(false);
    BooleanTriState _hidden = new BooleanTriState(false);
    BooleanTriState _subscript = new BooleanTriState(false);
    BooleanTriState _superscript = new BooleanTriState(false);
    IntTriState _color = new IntTriState(0);
    IntTriState _bgcolor = new IntTriState(0);
    IntTriState _charStyle = new IntTriState(-1);
    IntTriState _font = new IntTriState(-1);
    IntTriState _fontSize = new IntTriState(-1);
    IntTriState _lang = new IntTriState(-1);

    /**
     * \\uc: how many (ANSI) chars to skip after a Unicode char.
     * Default is 1 according to the spec.
     */
    int _skipCount = 1;

    protected Object clone()
    {
        RtfTextProperties result = new RtfTextProperties();

        if (isBoldSet())        result.setBold(isBold());
        if (isItalicSet())      result.setItalic(isItalic());
        if (isUnderlinedSet())  result.setUnderlined(isUnderlined());
        if (isHiddenSet())      result.setHidden(isHidden());
        if (isSubscriptSet())   result.setSubscript(isSubscript());
        if (isSuperscriptSet()) result.setSuperscript(isSuperscript());
        if (isColorSet())       result.setColor(getColor());
        if (isBgColorSet())     result.setBgColor(getBgColor());
        if (isCharStyleSet())   result.setCharStyle(getCharStyle());

        if (isFontSet())        result.setFont(getFont());
        if (isFontSizeSet())    result.setFontSize(getFontSize());
        if (isLangSet())        result.setLang(getLang());

        // TODO: need to rethink the skip count.
        result.setSkipCount(getSkipCount());

        return result;
    }

    protected void reset()
    {
        _bold.setValueNotSet();
        _italic.setValueNotSet();
        _underlined.setValueNotSet();
        _hidden.setValueNotSet();
        _subscript.setValueNotSet();
        _superscript.setValueNotSet();
        _color.setValueNotSet();
        _bgcolor.setValueNotSet();
        _charStyle.setValueNotSet();
        _font.setValueNotSet();
        _fontSize.setValueNotSet();
        _lang.setValueNotSet();
    }

    public boolean equals(Object p_other)
    {
        if (p_other != null && p_other instanceof RtfTextProperties)
        {
            RtfTextProperties o1 = this;
            RtfTextProperties o2 = (RtfTextProperties)p_other;

            if (o1._bold.equals(o2._bold) &&
                o1._italic.equals(o2._italic) &&
                o1._underlined.equals(o2._underlined) &&
                o1._hidden.equals(o2._hidden) &&
                o1._subscript.equals(o2._subscript) &&
                o1._superscript.equals(o2._superscript) &&
                o1._color.equals(o2._color) &&
                o1._bgcolor.equals(o2._bgcolor) &&
                o1._charStyle.equals(o2._charStyle) &&
                o1._font.equals(o2._font) &&
                o1._fontSize.equals(o2._fontSize) &&
                o1._lang.equals(o2._lang))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>For styles that are based on other styles, merges the
     * current text properties (this) with the properties of the style
     * it is based on.</p>
     *
     * <p>This is a destructive modification, to preserve any original
     * objects, use the clone() method.</p>
     */
    protected RtfTextProperties merge(RtfTextProperties p_other)
    {
        if (!isBoldSet() && p_other.isBoldSet())
        {
            setBold(p_other.isBold());
        }
        if (!isItalicSet() && p_other.isItalicSet())
        {
            setItalic(p_other.isItalic());
        }
        if (!isUnderlinedSet() && p_other.isUnderlinedSet())
        {
            setUnderlined(p_other.isUnderlined());
        }
        if (!isHiddenSet() && p_other.isHiddenSet())
        {
            setHidden(p_other.isHidden());
        }
        if (!isSubscriptSet() && p_other.isSubscriptSet())
        {
            setSubscript(p_other.isSubscript());
        }
        if (!isSuperscriptSet() && p_other.isSuperscriptSet())
        {
            setSuperscript(p_other.isSuperscript());
        }

        if (!isColorSet() && p_other.isColorSet())
        {
            setColor(p_other.getColor());
        }
        if (!isBgColorSet() && p_other.isBgColorSet())
        {
            setBgColor(p_other.getBgColor());
        }
        if (!isCharStyleSet() && p_other.isCharStyleSet())
        {
            setCharStyle(p_other.getCharStyle());
        }
        if (!isFontSet() && p_other.isFontSet())
        {
            setFont(p_other.getFont());
        }
        if (!isFontSizeSet() && p_other.isFontSizeSet())
        {
            setFontSize(p_other.getFontSize());
        }
        if (!isLangSet() && p_other.isLangSet())
        {
            setLang(p_other.getLang());
        }

        // TODO
        setSkipCount(p_other.getSkipCount());

        return this;
    }

    //
    // Setters and getters
    //

    public void setBold(boolean bold)
    {
        _bold.setValue(bold);
    }

    public boolean isBoldSet()
    {
        return _bold.isSet();
    }

    public boolean isBold()
    {
        return _bold.getValue();
    }

    public void unsetBold()
    {
        _bold.setValueNotSet();
    }

    public void setItalic(boolean italic)
    {
        _italic.setValue(italic);
    }

    public boolean isItalic()
    {
        return _italic.getValue();
    }

    public boolean isItalicSet()
    {
        return _italic.isSet();
    }

    public void unsetItalic()
    {
        _italic.setValueNotSet();
    }

    public void setUnderlined(boolean underlined)
    {
        _underlined.setValue(underlined);
    }

    public boolean isUnderlined()
    {
        return _underlined.getValue();
    }

    public boolean isUnderlinedSet()
    {
        return _underlined.isSet();
    }

    public void unsetUnderlined()
    {
        _underlined.setValueNotSet();
    }

    public void setHidden(boolean hidden)
    {
        _hidden.setValue(hidden);
    }

    public boolean isHidden()
    {
        return _hidden.getValue();
    }

    public boolean isHiddenSet()
    {
        return _hidden.isSet();
    }

    public void unsetHidden()
    {
        _hidden.setValueNotSet();
    }

    public void setSubscript(boolean subscript)
    {
        _subscript.setValue(subscript);
    }

    public boolean isSubscript()
    {
        return _subscript.getValue();
    }

    public boolean isSubscriptSet()
    {
        return _subscript.isSet();
    }

    public void unsetSubscript()
    {
        _subscript.setValueNotSet();
    }

    public void setSuperscript(boolean superscript)
    {
        _superscript.setValue(superscript);
    }

    public boolean isSuperscript()
    {
        return _superscript.getValue();
    }

    public boolean isSuperscriptSet()
    {
        return _superscript.isSet();
    }

    public void unsetSuperscript()
    {
        _superscript.setValueNotSet();
    }

    public void setCharStyle(int style)
    {
        _charStyle.setValue(style);
    }

    public int getCharStyle()
    {
        return _charStyle.getValue();
    }

    public boolean isCharStyleSet()
    {
        return _charStyle.isSet();
    }

    public void unsetCharStyle()
    {
        _charStyle.setValueNotSet();
    }

    public void setFont(int font)
    {
        _font.setValue(font);
    }

    public int getFont()
    {
        return _font.getValue();
    }

    public boolean isFontSet()
    {
        return _font.isSet();
    }

    public void unsetFont()
    {
        _font.setValueNotSet();
    }

    public void setFontSize(int fontSize)
    {
        _fontSize.setValue(fontSize);
    }

    public int getFontSize()
    {
        return _fontSize.getValue();
    }

    public boolean isFontSizeSet()
    {
        return _fontSize.isSet();
    }

    public void unsetFontSize()
    {
        _fontSize.setValueNotSet();
    }

    public void setLang(int lang)
    {
        _lang.setValue(lang);
    }

    public int getLang()
    {
        return _lang.getValue();
    }

    public boolean isLangSet()
    {
        return _lang.isSet();
    }

    public void unsetLang()
    {
        _lang.setValueNotSet();
    }

    public void setColor(int color)
    {
        _color.setValue(color);
    }

    public int getColor()
    {
        return _color.getValue();
    }

    public boolean isColorSet()
    {
        return _color.isSet();
    }

    public void unsetColor()
    {
        _color.setValueNotSet();
    }

    public void setBgColor(int color)
    {
        _bgcolor.setValue(color);
    }

    public int getBgColor()
    {
        return _bgcolor.getValue();
    }

    public boolean isBgColorSet()
    {
        return _bgcolor.isSet();
    }

    public void unsetBgColor()
    {
        _bgcolor.setValueNotSet();
    }

    public void setSkipCount(int skipCount)
    {
        _skipCount = skipCount;
    }

    public int getSkipCount()
    {
        return _skipCount;
    }

    //
    // Support Methods
    //

    public String getColorName()
    {
        return getColorNamefromCode(_color.getValue());
    }

    static String getColorNamefromCode(int color)
    {
        if (color == 0)
        {
            return "Default";
        }
        else if (color == 1)
        {
            return "Black";
        }
        else if (color == 2)
        {
            return "Blue";
        }
        else if (color == 3)
        {
            return "Aqua";
        }
        else if (color == 4)
        {
            return "Lime";
        }
        else if (color == 5)
        {
            return "Fuschia";
        }
        else if (color == 6)
        {
            return "Red";
        }
        else if (color == 7)
        {
            return "Yellow";
        }
        else if (color == 8)
        {
            return "White";
        }
        else if (color == 9)
        {
            return "Navy";
        }
        else if (color == 10)
        {
            return "Teal";
        }
        else if (color == 11)
        {
            return "Green";
        }
        else if (color == 12)
        {
            return "Purple";
        }
        else if (color == 13)
        {
            return "Maroon";
        }
        else if (color == 14)
        {
            return "Olive";
        }
        else if (color == 15)
        {
            return "Gray";
        }
        else if (color == 16)
        {
            return "Silver";
        }
        else
        {
            return "";
        }
    }

    public String getColorCode()
    {
        int color = _color.getValue();

        if (color == 1)
        {
            return "cA";
        }
        else if (color == 2)
        {
            return "cB";
        }
        else if (color == 3)
        {
            return "cC";
        }
        else if (color == 4)
        {
            return "cD";
        }
        else if (color == 5)
        {
            return "cE";
        }
        else if (color == 6)
        {
            return "cF";
        }
        else if (color == 7)
        {
            return "cG";
        }
        else if (color == 8)
        {
            return "cH";
        }
        else if (color == 9)
        {
            return "cI";
        }
        else if (color == 10)
        {
            return "cJ";
        }
        else if (color == 11)
        {
            return "cK";
        }
        else if (color == 12)
        {
            return "cL";
        }
        else if (color == 13)
        {
            return "cM";
        }
        else if (color == 14)
        {
            return "cN";
        }
        else if (color == 15)
        {
            return "cO";
        }
        else if (color == 16)
        {
            return "cP";
        }
        else
        {
            return "";
        }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        boolean empty = true;

        if (isBoldSet())
        {
            buf.append("bold ");
            empty = false;
        }
        if (isItalicSet())
        {
            buf.append("italic ");
            empty = false;
        }
        if (isUnderlinedSet())
        {
            buf.append("underlined ");
            empty = false;
        }
        if (isHiddenSet())
        {
            buf.append("hidden ");
            empty = false;
        }
        if (isSubscriptSet())
        {
            buf.append("sub ");
            empty = false;
        }
        if (isSuperscriptSet())
        {
            buf.append("super ");
            empty = false;
        }
        if (isColorSet() && getColor() != 0)
        {
            buf.append(getColorName());
            buf.append(" ");
            empty = false;
        }
        if (isFontSet())
        {
            buf.append("font=");
            buf.append(getFont());
            buf.append(" ");
            empty = false;
        }
        if (isFontSizeSet())
        {
            buf.append("fontsize=");
            buf.append(getFontSize());
            buf.append(" ");
            empty = false;
        }
        if (isLangSet())
        {
            buf.append("lang=");
            buf.append(getLang());
            buf.append(" ");
            empty = false;
        }

        if (empty)
        {
            return "plain";
        }
        else
        {
            return buf.toString();
        }
    }

    public String toRtf()
    {
        StringBuffer buf = new StringBuffer();

        if (isBoldSet())
        {
            buf.append(isBold() ? "\\b" : "\\b0");
        }
        if (isItalicSet())
        {
            buf.append(isItalic() ? "\\i" : "\\i0");
        }
        if (isUnderlinedSet())
        {
            buf.append(isUnderlined() ? "\\ul" : "\\ul0");
        }
        if (isHiddenSet())
        {
            buf.append(isHidden() ? "\\v" : "\\v0");
        }
        if (isSubscriptSet())
        {
            buf.append(isSubscript() ? "\\sub" : "");
        }
        if (isSuperscriptSet())
        {
            buf.append(isSuperscript() ? "\\super" : "");
        }
        if (isColorSet())
        {
            buf.append("\\cf");
            buf.append(getColor());
        }
        if (isBgColorSet())
        {
            buf.append("\\cb");
            buf.append(getBgColor());
        }
        if (isFontSet())
        {
            buf.append("\\f");
            buf.append(getFont());
        }
        if (isFontSizeSet())
        {
            buf.append("\\fs");
            buf.append(getFontSize());
        }
        if (isLangSet())
        {
            buf.append("\\lang");
            buf.append(getLang());
        }

        if (buf.length() > 0)
        {
            buf.append(" ");
        }

        return buf.toString();
    }
}
