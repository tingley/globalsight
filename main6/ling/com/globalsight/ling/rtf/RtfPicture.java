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
// class : com.tetrasix.majix.RtfPicture
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

public class RtfPicture
    extends RtfObject
{
    String _data = null;
    int _counter = -1;

    String _pictdata = null;
    String _picttype = null;
    IntTriState _picw = new IntTriState(0);
    IntTriState _pich = new IntTriState(0);
    IntTriState _picwgoal = new IntTriState(0);
    IntTriState _pichgoal = new IntTriState(0);
    IntTriState _picscalex = new IntTriState(100);
    IntTriState _picscaley = new IntTriState(100);
    IntTriState _piccropt = new IntTriState(0);
    IntTriState _piccropb = new IntTriState(0);
    IntTriState _piccropr = new IntTriState(0);
    IntTriState _piccropl = new IntTriState(0);

    RtfPicture(int counter)
    {
        _data = null;
        _counter = counter;
    }

    RtfPicture(String data)
    {
        _data = data;
        _counter = -1;
    }

    public String getData()
    {
        return _data;
    }

    public int getCounter()
    {
        return _counter;
    }

    public void setPictType(String type)
    {
        _picttype = type;
    }

    public String getPictType()
    {
        return _picttype;
    }

    public void setPictData(String data)
    {
        _pictdata = data;
    }

    public String getPictData()
    {
        return _pictdata;
    }

    public boolean isPicwSet()
    {
        return _picw.isSet();
    }

    public int getPicw()
    {
        return _picw.getValue();
    }

    public void setPicw(int value)
    {
        _picw.setValue(value);
    }

    public boolean isPichSet()
    {
        return _pich.isSet();
    }

    public int getPich()
    {
        return _pich.getValue();
    }

    public void setPich(int value)
    {
        _pich.setValue(value);
    }

    public boolean isPicwgoalSet()
    {
        return _picwgoal.isSet();
    }

    public int getPicwgoal()
    {
        return _picwgoal.getValue();
    }

    public void setPicwgoal(int value)
    {
        _picwgoal.setValue(value);
    }

    public boolean isPichgoalSet()
    {
        return _pichgoal.isSet();
    }

    public int getPichgoal()
    {
        return _pichgoal.getValue();
    }

    public void setPichgoal(int value)
    {
        _pichgoal.setValue(value);
    }

    public boolean isPicscalexSet()
    {
        return _picscalex.isSet();
    }

    public int getPicscalex()
    {
        return _picscalex.getValue();
    }

    public void setPicscalex(int value)
    {
        _picscalex.setValue(value);
    }

    public boolean isPicscaleySet()
    {
        return _picscaley.isSet();
    }

    public int getPicscaley()
    {
        return _picscaley.getValue();
    }

    public void setPicscaley(int value)
    {
        _picscaley.setValue(value);
    }

    public boolean isPiccroptSet()
    {
        return _piccropt.isSet();
    }

    public int getPiccropt()
    {
        return _piccropt.getValue();
    }

    public void setPiccropt(int value)
    {
        _piccropt.setValue(value);
    }

    public boolean isPiccropbSet()
    {
        return _piccropb.isSet();
    }

    public int getPiccropb()
    {
        return _piccropb.getValue();
    }

    public void setPiccropb(int value)
    {
        _piccropb.setValue(value);
    }

    public boolean isPiccroprSet()
    {
        return _piccropr.isSet();
    }

    public int getPiccropr()
    {
        return _piccropr.getValue();
    }

    public void setPiccropr(int value)
    {
        _piccropr.setValue(value);
    }

    public boolean isPiccroplSet()
    {
        return _piccropl.isSet();
    }

    public int getPiccropl()
    {
        return _piccropl.getValue();
    }

    public void setPiccropl(int value)
    {
        _piccropl.setValue(value);
    }


    public void Dump(PrintWriter out)
    {
        out.print("<pict count=\"" + String.valueOf(_counter) + "\"/>");
    }

    /*
      public void generate(XmlGenerator gen, XmlWriter out,
        XmlGeneratorContext context)
      {
      gen.rtfgenerate(this, out, context);
      }
    */
}

