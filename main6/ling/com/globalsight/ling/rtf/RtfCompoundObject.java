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
// class : com.tetrasix.majix.RtfCompoundObject
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


import java.util.ArrayList;
import java.util.Iterator;

import java.io.PrintWriter;

public class RtfCompoundObject
    extends RtfObject
{
    protected ArrayList _content;

    RtfCompoundObject()
    {
        _content = new ArrayList();
    }

    public void add(RtfObject object)
    {
        _content.add(object);
    }

    public int size()
    {
        return _content.size();
    }

    public Iterator getIterator()
    {
        return _content.iterator();
    }

    public RtfObject getObject(int pos)
    {
        return (RtfObject)_content.get(pos);
    }

    public String getData()
    {
        StringBuffer buf = new StringBuffer();

        for (int ii = 0; ii < _content.size(); ii++)
        {
            buf.append(((RtfObject)_content.get(ii)).getData());
        }

        return buf.toString();
    }

    public void Dump(PrintWriter out)
    {
        out.println("<GROUP>");

        for (int ii = 0; ii < _content.size(); ii++)
        {
            ((RtfObject)_content.get(ii)).Dump(out);
        }

        out.println("</GROUP>");
    }

    public void toText(PrintWriter out)
    {
        for (int ii = 0; ii < _content.size(); ii++)
        {
            ((RtfObject)_content.get(ii)).toText(out);
        }
    }
}

