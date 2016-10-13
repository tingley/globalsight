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

public class QARuleDefault
{
    private String check = null;
    private String description = null;
    private int targetExpansion = -1;

    public static final String SOURCE_EQUAL_TO_TARGET = "Source equal to Target";
    public static final String TARGET_STRING_EXPANSION_OF = "Target string expansion of";

    public QARuleDefault()
    {
    }

    public QARuleDefault(String check)
    {
        this.check = check;
        this.description = check;
    }

    public static QARuleDefault initFromElement(Element tagElement)
    {
        Node checkElement = tagElement.getElementsByTagName("check").item(0);
        String check = checkElement.getFirstChild().getNodeValue();

        QARuleDefault qaRuleDefault = new QARuleDefault(check);

        NodeList teElements = tagElement
                .getElementsByTagName("targetExpansion");
        if (teElements != null && teElements.getLength() > 0)
        {
            Node targetExpansionElement = teElements.item(0);
            String te = targetExpansionElement.getFirstChild().getNodeValue();

            int targetExpansion = 255;
            try
            {
                targetExpansion = Integer.parseInt(te);
            }
            catch (Exception ex)
            {
            }
            qaRuleDefault.setTargetExpansion(targetExpansion);
        }

        return qaRuleDefault;
    }

    public String getCheck()
    {
        return check;
    }

    public void setCheck(String check)
    {
        this.check = check;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getTargetExpansion()
    {
        return targetExpansion;
    }

    public void setTargetExpansion(int targetExpansion)
    {
        this.targetExpansion = targetExpansion;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("check=");
        sb.append(this.check);
        sb.append("|description=");
        sb.append(this.description);

        return sb.toString();
    }
}
