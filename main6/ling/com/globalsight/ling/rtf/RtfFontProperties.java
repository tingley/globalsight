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
// class : com.tetrasix.majix.RtfFontProperties
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

public class RtfFontProperties
    implements Cloneable
{
    String _name;
    String _altname;
    String _family;

    /**
     * Charset code: 0=ANSI 1=Default 2=Symbol 3=Invalid 77=Mac
     * 128=Shift Jis 129=Hangul 130=Johab 134=GB2312 136=Big5
     * 161=Greek 162=Turkish 163=Vietnamese 177=Hebrew 178=Arabic
     * 179=Arabic Traditional 180=Arabic user 181=Hebrew user
     * 186=Baltic 204=Russian 222=Thai 238=Eastern European
     * 254=PC 437 255=OEM.
     */
    int _charset = -1;

    /**
     * \fprqN: Default pitch 0, Fixed pitch 1, Variable pitch 2.
     */
    int _pitch = 0;

    String _panose;

    protected Object clone()
    {
        RtfFontProperties properties = new RtfFontProperties();

        properties.setName(getName());
        properties.setAltName(getAltName());
        properties.setFamily(getFamily());
        properties.setCharset(getCharset());
        properties.setPitch(getPitch());
        properties.setPanose(getPanose());

        return properties;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    public void setAltName(String name)
    {
        _altname = name;
    }

    public String getAltName()
    {
        return _altname;
    }

    public void setFamily(String family)
    {
        _family = family;
    }

    public String getFamily()
    {
        return _family;
    }

    public void setCharset(int charset)
    {
        _charset = charset;
    }

    public int getCharset()
    {
        return _charset;
    }

    public void setPitch(int pitch)
    {
        _pitch = pitch;
    }

    public int getPitch()
    {
        return _pitch;
    }

    public void setPanose(String panose)
    {
        _panose = panose;
    }

    public String getPanose()
    {
        return _panose;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        // buf.append(_name);

        buf.append("charset='");
        buf.append(_charset);
        buf.append("' ");

        if (_family != null)
        {
            buf.append("family='");
            buf.append(_family);
            buf.append("' ");
        }

        if (_pitch > 0)
        {
            buf.append("pitch='");
            buf.append(_pitch);
            buf.append("' ");
        }

        if (_panose != null)
        {
            buf.append("panose='");
            buf.append(_panose);
            buf.append("' ");
        }

        if (_altname != null)
        {
            buf.append("alt='");
            buf.append(_altname);
            buf.append("' ");
        }

        return buf.toString();
    }

    public String toRtf()
    {
        StringBuffer buf = new StringBuffer();

        // buf.append(_name);

        if (_family != null)
        {
            buf.append("\\");
            buf.append(_family);
        }

        buf.append("\\fcharset");
        buf.append(_charset);

        if (_pitch > 0)
        {
            buf.append("\\fprq");
            buf.append(_pitch);
        }

        if (_panose != null)
        {
            buf.append("{\\*\\panose ");
            buf.append(_panose);
            buf.append("}");
        }
        else
        {
            buf.append(" ");
        }

        buf.append(_name);

        if (_altname != null)
        {
            buf.append("{\\*\\falt ");
            buf.append(_altname);
            buf.append("}");
        }

        buf.append(";");

        return buf.toString();
    }
}
