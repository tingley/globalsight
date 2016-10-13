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

public class InternalText implements Priorityable
{
    private String name = null;
    private boolean isRE = false;
    private int priority = 9;

    public InternalText()
    {
    }

    public InternalText(String name, boolean isRE)
    {
        this.name = name;
        this.isRE = isRE;
    }

    public String getName()
    {
        return name;
    }

    public boolean isRE()
    {
        return isRE;
    }

    public void setRE(boolean isRE)
    {
        this.isRE = isRE;
    }

    public void setName(String name)
    {
        this.name = name;
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
        return this.name + "|isRE=" + this.isRE  + "|priority=" + this.priority;
    }

    public static InternalText initFromElement(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("aName").item(0);
        String name = tagNameElement.getFirstChild().getNodeValue();

        NodeList isREElements = tagElement.getElementsByTagName("isRE");
        boolean isRE = false;
        if (isREElements != null && isREElements.getLength() > 0)
        {
            isRE = "true".equals(isREElements.item(0).getFirstChild().getNodeValue());
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
            catch(Exception ex)
            {
                priority = 9;
            }
        }

        InternalText it = new InternalText(name, isRE);
        it.setPriority(priority);

        return it;
    }
}
