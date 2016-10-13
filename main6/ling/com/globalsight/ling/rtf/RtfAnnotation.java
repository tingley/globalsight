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
// Copyright (c) 2005 GlobalSight Corporation. All rights reserved.
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

public class RtfAnnotation
    extends RtfCompoundObject
{
    private String m_id;
    private String m_ref;
    private RtfCompoundObject m_content;

    RtfAnnotation()
    {
        super();
    }

    // ID name read from {\*atnid ABCDE}. Word appends a number to the
    // ID to show annotations as CvdL 1, CvdL 2, etc.
    public void setId(String p_id)
    {
        m_id = p_id;
    }

    public String getId()
    {
        return m_id;
    }

    // Ref name read from {\*atnref ABCDE}. The ref links an
    // annotation to its annotation bookmark.
    public void setRef(String p_ref)
    {
        m_ref = p_ref;
    }

    public String getRef()
    {
        return m_ref;
    }

    public void Dump(PrintWriter out)
    {
        out.print("<ANNOTATION");

        if (m_ref != null)
        {
            out.print(" id='" + m_ref + "'");
        }
        out.println(">");

        for (int i = 0, max = _content.size(); i < max; i++)
        {
            ((RtfObject)_content.get(i)).Dump(out);
        }

        out.println("</ANNOTATION>");
    }

    public void toText(PrintWriter out)
    {
        // Annotations don't print their content into the main
        // document text.  Use toTextSpecial() to get the text.
    }

    public void toTextSpecial(PrintWriter out)
    {
        super.toText(out);
    }
}

