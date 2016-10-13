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

import java.io.PrintWriter;

public class RtfFootnote
    extends RtfCompoundObject
{
    boolean _endnote = false;

    RtfFootnote()
    {
        super();
    }

    void setEndnote(boolean endnote)
    {
        _endnote = endnote;
    }

    public boolean isEndnote()
    {
        return _endnote;
    }

    public void Dump(PrintWriter out)
    {
        out.println("<FOOTNOTE>");

        for (int ii = 0; ii < _content.size(); ii++)
        {
            ((RtfObject)_content.get(ii)).Dump(out);
        }

        out.println("</FOOTNOTE>");
    }

    public void toText(PrintWriter out)
    {
        // Footnotes don't print their content into the main
        // document text.  Use toTextSpecial() to get the text.
    }

    public void toTextSpecial(PrintWriter out)
    {
        super.toText(out);
    }
}

