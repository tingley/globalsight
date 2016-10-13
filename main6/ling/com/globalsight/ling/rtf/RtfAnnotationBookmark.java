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

/**
 * This Class represents annotation bookmarks in text. An annotation
 * bookmark is a simple start and end marker somewhere in text with an
 * ID pointing to the annotation that was inserted.
 *
 * The analyzer reads starting and ending bookmarks as they appear in
 * the file.
 */
public class RtfAnnotationBookmark
    extends RtfObject
{
	private boolean _start;
    private String _name;

    public RtfAnnotationBookmark(boolean start)
    {
		_start = start;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

	public boolean isStart()
	{
		return _start;
	}

	public boolean isEnd()
	{
		return !_start;
	}

	public String toRtf()
	{
		StringBuffer result = new StringBuffer();

		result.append("{\\*\\");
		if (_start)
		{
			result.append("atrfstart ");
		}
		else
		{
			result.append("atrfend ");
		}

		result.append(_name);
		result.append("}");

		return result.toString();
	}

    public void Dump(PrintWriter out)
    {
		if (_start)
		{
			out.print("<annotationbookmarkstart name='" + _name + "' />");
		}
		else
		{
			out.print("<annotationbookmarkend name='" + _name + "' />");
		}
    }
}
