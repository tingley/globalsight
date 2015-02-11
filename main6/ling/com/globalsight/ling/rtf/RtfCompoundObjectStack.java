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
// class : com.tetrasix.majix.RtfCompoundObjectStack
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



class RtfCompoundObjectStack {

    RtfCompoundObjectStackCell _top;

    RtfCompoundObjectStack()
    {
        _top = null;
    }

    void push(RtfCompoundObject state)
    {
        _top = new RtfCompoundObjectStackCell(_top, state);
    }

    RtfCompoundObject pop()
    {
        RtfCompoundObjectStackCell save = _top;

        if (_top != null)
        {
            _top = _top.getNextCell();
        }

        if (save != null)
        {
            return save.getRtfObject();
        }
        else
        {
            return null;
        }
    }

    boolean empty()
    {
        return _top == null;
    }

    RtfCompoundObject top()
    {
        if (_top != null)
        {
            return _top.getRtfObject();
        }
        else
        {
            return null;
        }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        RtfCompoundObjectStackCell cell = _top;

        while (cell != null)
        {
            buf.append(cell.toString());
            cell = cell.getNextCell();
        }

        return buf.toString();
    }

}


class RtfCompoundObjectStackCell
{
    private RtfCompoundObject _rtfobject;
    private RtfCompoundObjectStackCell _nextcell;

    RtfCompoundObjectStackCell(RtfCompoundObjectStackCell cell,
        RtfCompoundObject rtfobject)
    {
        _rtfobject = rtfobject;
        _nextcell = cell;
    }

    RtfCompoundObjectStackCell getNextCell()
    {
        return _nextcell;
    }

    RtfCompoundObject getRtfObject()
    {
        return _rtfobject;
    }

    public String toString()
    {
        return _rtfobject.toString();
    }
}

