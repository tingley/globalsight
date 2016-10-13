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

package com.globalsight.ling.rtf;

import java.io.PrintWriter;

/**
 * This Class represents bookmarks in text. Bookmarks come in 3
 * flavors: starting and ending bookmark, and location bookmark.
 * Location bookmarks surround no text.
 *
 * The analyzer reads starting and ending bookmarks as they appear in
 * the file. The optimizer then collapses consecutive starting &
 * ending bookmarks into a location bookmark.
 */
public class RtfBookmark
    extends RtfObject
{
	private boolean _start;
    private String _name;
	private IntTriState _firstCol = new IntTriState(0);
	private IntTriState _lastCol = new IntTriState(0);

	private boolean _isLocation;

    public RtfBookmark(boolean start)
    {
		_start = start;
		_isLocation = false;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

	public void setIsLocation()
	{
		_isLocation = true;
	}

	public boolean isStart()
	{
		return _start;
	}

	public boolean isEnd()
	{
		return !_start;
	}

	public boolean isLocation()
	{
		return _isLocation;
	}

    public boolean isFirstColSet()
    {
		return _firstCol.isSet();
    }

    public int getFirstCol()
    {
		return _firstCol.getValue();
    }

    public void setFirstCol(int value)
    {
		_firstCol.setValue(value);
    }

    public boolean isLastColSet()
    {
		return _lastCol.isSet();
    }

    public int getLastCol()
    {
		return _lastCol.getValue();
    }

    public void setLastCol(int value)
    {
		_lastCol.setValue(value);
    }

	public String toRtf()
	{
		StringBuffer result = new StringBuffer();

		result.append("{\\*\\");
		if (_start)
		{
			result.append("bkmkstart");
		}
		else
		{
			result.append("bkmkend");
		}

		if (isFirstColSet())
		{
			result.append("\\bkmkcolf");
			result.append(getFirstCol());
		}
		if (isLastColSet())
		{
			result.append("\\bkmkcoll");
			result.append(getLastCol());
		}

		result.append(" ");
		result.append(_name);
		result.append("}");

		// A location bookmark is just a start and end bookmark with
		// no intervening text.
		if (_isLocation)
		{
			result.append("{\\*\\bkmkend ");
			result.append(_name);
			result.append("}");
		}

		return result.toString();
	}

    public void Dump(PrintWriter out)
    {
        out.print("<bookmark name='" + _name + "' type='" + 
			(_start ? "start" : "end") + "' " +
			(isFirstColSet() ? "firstcol='" + getFirstCol() + "'" : "") +
			(isLastColSet() ? "lastcol='" + getLastCol() + "'" : "") +
			"/>");
    }
}
