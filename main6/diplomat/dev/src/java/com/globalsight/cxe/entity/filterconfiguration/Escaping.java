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

package com.globalsight.cxe.entity.filterconfiguration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.everest.util.comparator.Priorityable;
import com.globalsight.util.StringUtil;

public class Escaping implements Priorityable
{
    private String character = null;
    private boolean unEscapeOnImport = false;
    private boolean reEscapeOnExport = false;
    private int priority = 9;
    private String escape = null;
    private boolean isCheckActive = false;
    private String activeValue = null;
    private String partConentValue = null;
    private boolean startIsRegex = false;
    private boolean finishIsRegex = false;
    private String startPattern = null;
    private String finishPattern = null;
    
    public Escaping()
    {
    }

	public Escaping(String character, boolean unEscapeOnImport,
			boolean reEscapeOnExport, String escape, boolean isCheckActive,
			String activeValue, String partConentValue, boolean startIsRegex,
			String startPattern, boolean finishIsRegex, String finishPattern)
	{
		this.character = character;
		this.unEscapeOnImport = unEscapeOnImport;
		this.reEscapeOnExport = reEscapeOnExport;
		this.escape = escape;
		this.isCheckActive = isCheckActive;
		this.activeValue = activeValue;
		this.partConentValue = partConentValue;
		this.startIsRegex = startIsRegex;
		this.startPattern = startPattern;
		this.finishIsRegex = finishIsRegex;
		this.finishPattern = finishPattern;
	}

    public String getCharacter()
    {
        return character;
    }

    public void setCharacter(String character)
    {
        this.character = character;
    }

    public boolean isUnEscapeOnImport()
    {
        return unEscapeOnImport;
    }

    public void setUnEscapeOnImport(boolean unEscapeOnImport)
    {
        this.unEscapeOnImport = unEscapeOnImport;
    }

    public boolean isReEscapeOnExport()
    {
        return reEscapeOnExport;
    }

    public void setReEscapeOnExport(boolean reEscapeOnExport)
    {
        this.reEscapeOnExport = reEscapeOnExport;
    }

    public String getEscape()
	{
		return escape;
	}

	public void setEscape(String escape)
	{
		this.escape = escape;
	}
	
	public boolean isCheckActive()
	{
		return isCheckActive;
	}

	public void setCheckActive(boolean isCheckActive)
	{
		this.isCheckActive = isCheckActive;
	}

	public String getActiveValue()
	{
		return activeValue;
	}

	public void setActiveValue(String activeValue)
	{
		this.activeValue = activeValue;
	}

	public String getPartConentValue()
	{
		return partConentValue;
	}

	public void setPartConentValue(String partConentValue)
	{
		this.partConentValue = partConentValue;
	}

	public boolean isStartIsRegex()
	{
		return startIsRegex;
	}

	public void setStartIsRegex(boolean startIsRegex)
	{
		this.startIsRegex = startIsRegex;
	}

	public boolean isFinishIsRegex()
	{
		return finishIsRegex;
	}

	public void setFinishIsRegex(boolean finishIsRegex)
	{
		this.finishIsRegex = finishIsRegex;
	}

	public String getStartPattern()
	{
		return startPattern;
	}

	public void setStartPattern(String startPattern)
	{
		this.startPattern = startPattern;
	}

	public String getFinishPattern()
	{
		return finishPattern;
	}

	public void setFinishPattern(String finishPattern)
	{
		this.finishPattern = finishPattern;
	}

	public String getName()
    {
        return this.character;
    }
    
    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public String toString()
    {
        return this.character + "|unEscapeOnImport=" + this.unEscapeOnImport
                + "|reEscapeOnExport=" + this.reEscapeOnExport + "|priority="
                + this.priority;
    }

    public static Escaping initFromElement(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("aName").item(0);
        String name = tagNameElement.getFirstChild().getNodeValue();

        NodeList unEscapeOnImportElements = tagElement.getElementsByTagName("unEscapeOnImport");
        boolean unEscapeOnImport = false;
        if (unEscapeOnImportElements != null && unEscapeOnImportElements.getLength() > 0)
        {
            unEscapeOnImport = "true".equals(unEscapeOnImportElements.item(0).getFirstChild()
                    .getNodeValue());
        }
        
        NodeList reEscapeOnExportElements = tagElement.getElementsByTagName("reEscapeOnExport");
        boolean reEscapeOnExport = false;
        if (reEscapeOnExportElements != null && reEscapeOnExportElements.getLength() > 0)
        {
            reEscapeOnExport = "true".equals(reEscapeOnExportElements.item(0).getFirstChild()
                    .getNodeValue());
        }

        NodeList priorityElements = tagElement.getElementsByTagName("priority");
        int priority = 9;
        if (priorityElements != null && priorityElements.getLength() > 0)
        {
            String pp = priorityElements.item(0).getFirstChild().getNodeValue();
            try
            {
                priority = Integer.parseInt(pp);
            }
            catch (Exception ex)
            {
                priority = 9;
            }
        }
        
		Node escapeNode = tagElement.getElementsByTagName("escape").item(0);
		String escape = null;
		if (escapeNode != null && escapeNode.getFirstChild() != null)
		{
			escape = escapeNode.getFirstChild().getNodeValue();
			if (StringUtil.isEmptyAndNull(escape))
			{
				escape = "\\";
			}
		}
		else
		{
			escape = "\\";
		}

		NodeList isCheckActiveElements = tagElement
				.getElementsByTagName("isCheckActive");
		boolean isCheckActive = false;
		if (isCheckActiveElements != null
				&& isCheckActiveElements.getLength() > 0)
		{
			isCheckActive = "true".equals(isCheckActiveElements.item(0)
					.getFirstChild().getNodeValue());
		}

        String activeValue = null;
        String partConentValue = null;
        String startPattern = null;
        String finishPattern = null;
        boolean startIsRegex = false;
        boolean finishIsRegex = false;
		if (isCheckActive)
		{
			activeValue  = tagElement
					.getElementsByTagName("activeValue").item(0)
					.getFirstChild().getNodeValue();
			partConentValue = tagElement
					.getElementsByTagName("partConentValue").item(0)
					.getFirstChild().getNodeValue();

			if (StringUtil.isNotEmptyAndNull(partConentValue)
					&& partConentValue.equalsIgnoreCase("startFinishes"))
			{
				NodeList startIsRegexElements = tagElement
						.getElementsByTagName("startIsRegex");
				if (startIsRegexElements != null
						&& startIsRegexElements.getLength() > 0)
				{
					startIsRegex = "true".equals(startIsRegexElements.item(0)
							.getFirstChild().getNodeValue());
				}
				startPattern = tagElement.getElementsByTagName("startPattern")
						.item(0).getFirstChild().getNodeValue();
				
				NodeList finishIsRegexElements = tagElement
						.getElementsByTagName("finishIsRegex");
				if (finishIsRegexElements != null
						&& finishIsRegexElements.getLength() > 0)
				{
					finishIsRegex = "true".equals(finishIsRegexElements.item(0)
							.getFirstChild().getNodeValue());
				}
				finishPattern = tagElement
						.getElementsByTagName("finishPattern").item(0)
						.getFirstChild().getNodeValue();
			}
		}
		
		Escaping ee = new Escaping(name, unEscapeOnImport, reEscapeOnExport,
				escape, isCheckActive, activeValue, partConentValue,
				startIsRegex, startPattern, finishIsRegex, finishPattern);
		ee.setPriority(priority);

        return ee;
    }
}
