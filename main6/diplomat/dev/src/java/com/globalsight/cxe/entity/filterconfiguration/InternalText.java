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

public class InternalText
{
    private String name = null;
    private boolean isRE = false;

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

    public String toString()
    {
        return this.name + "|isRE=" + this.isRE;
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

        InternalText it = new InternalText(name, isRE);

        return it;
    }
}
