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
// class : com.tetrasix.majix.RtfRowProperties
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

import java.io.PrintWriter;
import java.util.Vector;

public class RtfRowProperties
    implements Cloneable
{
    private int _cellWidths[];

    RtfRowProperties()
    {
        _cellWidths = null;
    }

    protected Object clone()
    {
        RtfRowProperties result = new RtfRowProperties();

        if (_cellWidths != null)
        {
            result._cellWidths = new int[_cellWidths.length];

            for (int ii = 0; ii < _cellWidths.length; ii++)
            {
                result._cellWidths[ii] = _cellWidths[ii];
            }
        }

        return result;
    }

    void setCellWidths(int widths[])
    {
        _cellWidths = widths;
    }

    void setCellWidths(Vector widths)
    {
        if (widths != null)
        {
            int length = widths.size();

            _cellWidths = new int[length];

            int current = 0;
            for (int ii = 0; ii < length; ii++)
            {
                int newcurrent = Integer.parseInt(
                    widths.elementAt(ii).toString());

                _cellWidths[ii] = newcurrent - current;
                current = newcurrent;
            }
        }
        else
        {
            _cellWidths = null;
        }
    }

    int getCellWidth(int pos)
    {
        if (pos < _cellWidths.length)
        {
            return _cellWidths[pos];
        }
        else
        {
            return -1;
        }
    }

    public void Dump(PrintWriter out)
    {
        if (_cellWidths != null)
        {
            out.print(" cellwidths=\"");

            for (int ii = 0; ii < _cellWidths.length; ii++)
            {
                if (ii > 0)
                {
                    out.print(" ");
                }

                out.print(_cellWidths[ii]);
            }

            out.print("\"");
        }
    }
}

