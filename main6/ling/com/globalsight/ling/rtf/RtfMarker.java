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
// Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
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

import com.globalsight.ling.rtf.RtfControl;

import java.io.*;

/**
 * <p>RtfMarkers are RtfSymbols representing markers that are followed
 * by a group containing more information, like footnote markers and
 * annotation markers. Markers appear inside segments (AFAIK).</p>
 *
 * <p>RtfMarkers do not inherit from RtfControl due to coding practices:
 * I'd rather get a compile time or runtime error for markers than the
 * default behavior inherited from (or for) RtfControls.</p>
 */
public class RtfMarker
    extends RtfObject
{
    private String _name = null;
    private RtfTextProperties _properties;

    RtfMarker(String name, RtfTextProperties properties)
    {
        _name = name;
		_properties = properties;
    }

    public String getName()
    {
        return _name;
    }

    public RtfTextProperties getProperties()
    {
        return _properties;
    }

    public String toRtf()
    {
        StringBuffer result = new StringBuffer();

        result.append("\\");
        result.append(_name);
		result.append(" ");

        return result.toString();
    }

    public void Dump(PrintWriter out)
    {
        out.print("<marker name=\"" + _name + "\">");
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */
}

