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
//
// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//
package com.globalsight.ling.rtf;


import java.io.*;
//import com.tetrasix.majix.xml.*;

/**
 * Preserves the data of a control that we don't recognize.
 */
public class RtfControl
    extends RtfObject
{
    String _name = null;
    String _data = null;

    RtfControl(String name)
    {
        _name = name;
    }

    RtfControl(String name, String data)
    {
        _name = name;
        _data = data;
    }

    public String getName()
    {
        return _name;
    }

    public String getParam()
    {
        return _name;
    }

    public String getData()
    {
        return _data;
    }

    public String toRtf()
    {
        StringBuffer result = new StringBuffer();

        result.append("\\");
        result.append(_name);

        if (_data != null && _data.length() > 0)
        {
            result.append(_data);
        }

        if (!_name.equals("*"))
        {
            result.append(" ");
        }

        return result.toString();
    }

    public void Dump(PrintWriter out)
    {
        out.print("<control name=\"" + _name + "\"");

        if (_data != null && _data.length() > 0)
        {
            out.print(" data=\"" + _data + "\"");
        }

        out.println(">");
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */
}

