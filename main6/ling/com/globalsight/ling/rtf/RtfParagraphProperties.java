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
// class : com.tetrasix.majix.RtfParagraphProperties
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


import java.io.*;

public class RtfParagraphProperties
    implements Cloneable
{
    public static final int STYLE_SIMPLE = 0;
    public static final int STYLE_NUMBERED = 1;
    public static final int STYLE_BULLET = 2;

    private int _style = 0;
    private int _numstyle = STYLE_SIMPLE;
    private String _bullet = null;
    private boolean _par = false;
    private boolean _ltrpar = true;
    private boolean _intbl = false;

    // TODO
    void reset()
    {
        _style = 0;
        _numstyle = STYLE_SIMPLE;
        _bullet = null;
        _par = false;
        _ltrpar = true;
        _intbl = false;
    }

    protected Object clone()
    {
        RtfParagraphProperties result = new RtfParagraphProperties();

        result.setStyle(getStyle());
        result.setNumStyle(getNumStyle());
        result.setBullet(getBullet());
        result.setPar(getPar());
        result.setInTable(getInTable());

        return result;
    }

    void setStyle(int style)
    {
        _style = style;
    }

    public int getStyle()
    {
        return _style;
    }

    void setNumStyle(int numstyle)
    {
        _numstyle = numstyle;
    }

    public int getNumStyle()
    {
        return _numstyle;
    }

    void setBullet(String bullet)
    {
        _bullet = bullet;
    }

    public String getBullet()
    {
        return _bullet;
    }

    void setPar(boolean par)
    {
        _par = par;
    }

    public boolean getPar()
    {
        return _par;
    }

    void setLtrPar()
    {
        _ltrpar = true;
    }

    void setRtlPar()
    {
        _ltrpar = false;
    }

    public boolean getLtrPar()
    {
        return _ltrpar;
    }

    void setInTable(boolean intable)
    {
        _intbl = intable;
    }

    public boolean getInTable()
    {
        return _intbl;
    }

    public String toRtf()
    {
        StringBuffer result = new StringBuffer();

        if (_ltrpar == true)
        {
            result.append("\\ltrpar");
        }
        else
        {
            result.append("\\rtlpar");
        }

        result.append("\\s");
        result.append(_style);

        if (_intbl == true)
        {
            result.append("\\intbl");
        }

        return result.toString();
    }

    public void Dump(PrintWriter out)
    {
        if (_style > 0)
        {
            out.print(" style=\"" + _style + "\"");
        }
        if (_bullet != null)
        {
            out.print(" bullet=\"" + _bullet + "\"");
        }
        if (_numstyle > 0)
        {
            out.print(" numstyle=\"" + _numstyle + "\"");
        }
    }
}
