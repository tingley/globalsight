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

public class Escaping implements Priorityable
{
    private String character = null;
    private boolean unEscapeOnImport = false;
    private boolean reEscapeOnExport = false;
    private int priority = 9;

    public Escaping()
    {
    }

    public Escaping(String character, boolean unEscapeOnImport,
            boolean reEscapeOnExport)
    {
        this.character = character;
        this.unEscapeOnImport = unEscapeOnImport;
        this.reEscapeOnExport = reEscapeOnExport;
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

        Escaping ee = new Escaping(name, unEscapeOnImport, reEscapeOnExport);
        ee.setPriority(priority);

        return ee;
    }
}
