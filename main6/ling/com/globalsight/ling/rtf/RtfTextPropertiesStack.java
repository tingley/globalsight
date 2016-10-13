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
// class : com.tetrasix.majix.RtfTextPropertiesStack
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

class RtfTextPropertiesStack
{
    //
    // Inner Classes
    //

    static private class RtfTextPropertiesStackCell
    {
        private RtfTextProperties _properties;
        private RtfTextPropertiesStackCell _nextcell;

        RtfTextPropertiesStackCell(RtfTextPropertiesStackCell cell,
            RtfTextProperties properties)
        {
            _properties = properties;
            _nextcell = cell;
        }

        RtfTextPropertiesStackCell getNextCell()
        {
            return _nextcell;
        }

        public RtfTextProperties getProperties()
        {
            return _properties;
        }

        public String toString()
        {
            return _properties.toString();
        }
    }

    //
    // Members
    //

    RtfTextPropertiesStackCell _top;

    //
    // Constructor
    //

    RtfTextPropertiesStack()
    {
        _top = null;
    }

    //
    // Public Methods
    //

    void push(RtfTextProperties properties)
    {
        _top = new RtfTextPropertiesStackCell(_top, properties);
    }

    RtfTextProperties pop()
    {
        RtfTextPropertiesStackCell save = _top;

        if (_top != null)
        {
            _top = _top.getNextCell();
        }
        else
        {
            System.err.println("_theTextPropertiesStack is empty!");
        }

        return (save != null) ? save.getProperties() : new RtfTextProperties();
    }

    boolean empty()
    {
        return _top == null;
    }

    RtfTextProperties top()
    {
        if (_top != null)
        {
            return _top.getProperties();
        }
        else
        {
            return null;
        }
    }

    public int size()
    {
        int result = 0;
        RtfTextPropertiesStackCell cell = _top;

        while (cell != null)
        {
            result++;

            cell = cell.getNextCell();
        }

        return result;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        RtfTextPropertiesStackCell cell = _top;

        while (cell != null)
        {
            buf.append(cell.toString());
            cell = cell.getNextCell();
            if (cell != null)
            {
                buf.append(" : ");
            }
        }

        return buf.toString();
    }
}
