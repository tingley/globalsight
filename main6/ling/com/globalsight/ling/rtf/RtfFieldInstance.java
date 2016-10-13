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
// class : com.tetrasix.majix.RtfFieldInstance
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
//import com.tetrasix.majix.xml.*;

public class RtfFieldInstance
    extends RtfObject
{
    String _fieldname;
    String _fieldresult;
    String _parameters;
    private RtfTextProperties _properties;

    RtfFieldInstance(String data, RtfTextProperties p_properties)
    {
        int pos = data.indexOf(' ');

        if (pos <= 0)
        {
            _fieldname = data;
            _parameters = "";
        }
        else
        {
            _fieldname = data.substring(0, pos);
            _parameters = data.substring(pos + 1).trim();

            _parameters = RtfSymbol.escapeBackslash(_parameters);
        }

        _fieldresult = null;
        _properties = p_properties;
    }

    RtfFieldInstance(
        String data, String result, RtfTextProperties p_properties)
    {
        this(data, p_properties);
        _fieldresult = result;
        _properties = p_properties;
    }

    public String getFieldName()
    {
        return _fieldname;
    }

    public String getParameters()
    {
        return _parameters;
    }

    public void setResult(String result)
    {
        _fieldresult = result;
    }

    public String getResult()
    {
        return _fieldresult;
    }

    public RtfTextProperties getProperties()
    {
        return _properties;
    }


    // The methods below don't reflect the newly added
    // RtfTextProperties field

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("<field name='");
        result.append(_fieldname);
        result.append("' params='");
        result.append(_parameters);
        result.append("'");

        if (_fieldresult != null)
        {
            result.append(" result=\'");
            result.append(_fieldresult);
            result.append("'");
        }

        result.append(">");

        return result.toString();
    }

    public String toRtf()
    {
        StringBuffer result = new StringBuffer();

        result.append("{\\field");
        result.append("{\\*\\fldinst ");
        result.append(_fieldname);
        result.append(" ");
        result.append(_parameters);
        result.append("}{\\fldrslt \\f0 PRESS CTRL-A F9 TO UPDATE FIELDS}}\n");

        return result.toString();
    }

    public void Dump(PrintWriter out)
    {
        out.print("<field name='" + _fieldname + "' params='" + _parameters + "'");

        if (_fieldresult != null)
        {
            out.print(" result=\'" + _fieldresult + "'");
        }

        out.print(">");
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */
}

