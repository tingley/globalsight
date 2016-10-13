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
// class : com.tetrasix.majix.RtfParagraph
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

public class RtfParagraph
    extends RtfBlock
{
    private RtfParagraphProperties _properties;

    RtfParagraph()
    {
        _properties = new RtfParagraphProperties();
    }

    /**
     * Overwrite RtfCompoundObject's add routine.  RtfParagraphs
     * consist of a list of RtfText objects.  We merge adjacent ANSI
     * text objects (for later charset conversion) if they share the
     * same text properties and leave Unicode chars as is.
     */
    public void add(RtfText object, boolean isUnicode)
    {
        // First text to be added? Just add.
        if (size() == 0)
        {
            if (isUnicode)
            {
                add(object);
            }
            else
            {
                // TODO: convert ANSI data.
                add(object);
            }
        }
        else
        {
            RtfObject previous = getObject(size() - 1);

            if (previous instanceof RtfText)
            {
                RtfText prev = (RtfText)previous;
                if (prev.getProperties().equals(object.getProperties()))
                {
                    if (isUnicode)
                    {
                        prev.setData(prev.getData() + object.getData());
                    }
                    else
                    {
                        // TODO: convert ANSI data.
                        prev.setData(prev.getData() + object.getData());
                    }
                }
                else
                {
                    add(object);
                }
            }
            else
            {
                add(object);
            }
        }
    }

    public void setProperties(RtfParagraphProperties properties)
    {
        if (properties != null)
        {
            _properties = (RtfParagraphProperties)properties.clone();
        }
    }

    public RtfParagraphProperties getProperties()
    {
        return _properties;
    }

    public void toText(PrintWriter out)
    {
        super.toText(out);
        out.println();
    }

    public void Dump(PrintWriter out)
    {
        out.print("<p");
        if (_properties != null)
        {
            _properties.Dump(out);
        }
        out.print(">");
        super.Dump(out);
        out.println("</p>");
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */

}

