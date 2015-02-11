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
package com.globalsight.everest.page.pageexport.style.mif;

import java.util.List;

/**
 * A style class extends Italic style
 */
public class ItalicStyle extends Style
{

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getNodeName()
     */
    @Override
    protected String getStyleName()
    {
        return "i";
    }

	@Override
	public void handleStyle(String style, List<Style> styles) 
	{
		if ("[#gs-i]".equals(style))
		{
			styles.add(new ItalicStyle());
		}
		else if ("[/#gs-i]".equals(style))
		{
			for (Style s : styles)
			{
				if (s instanceof ItalicStyle)
				{
					styles.remove(s);
					break;
				}
			}
		}
	}
	
	@Override
	protected String getFontName() 
	{
		return "FAngle";
	}

	@Override
	protected String getFontValue() 
	{
		return "`Italic'";
	}
}
