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
// class : com.tetrasix.majix.RtfToken
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

public class RtfToken
{
    static public final int BACKSLASH = 0;
    static public final int CONTROLWORD = 1;
    static public final int SPECIAL = 2;
    static public final int CONTROLSYMBOL = 3;    //
    static public final int OPENGROUP = 4;        // }
    static public final int CLOSEGROUP = 5;       // {
    static public final int DATA = 6;             //
    static public final int ASTERISK = 7;         // \*

    private int _type;      // token type (one of the constants before)
    private String _name;   // name of the command, if any
    private String _data;   // parameter or textual data

    public RtfToken(int type, String name, String data)
    {
        _type = type;
        _name = name;
        _data = data;
    }

    public RtfToken(int type)
    {
        _type = type;
    }

    public RtfToken(String data)
    {
        _type = this.DATA;
        _data = data;
    }

    public int getType()
    {
        return _type;
    }

    public String getName()
    {
        return _name;
    }

    public String getData()
    {
        return _data;
    }

    public String toString()
    {
        switch (_type) {

        case BACKSLASH:
            return "\\";

        case CONTROLWORD:
            return "\\" + _name + _data;

        case SPECIAL:
            return "SPECIAL";

        case CONTROLSYMBOL:
            return "CONTROLSYMBOL";

        case OPENGROUP:
            return "{";

        case CLOSEGROUP:
            return "}";

        case DATA:
            return _data;

        default:
            return null;
        }
    }
}

