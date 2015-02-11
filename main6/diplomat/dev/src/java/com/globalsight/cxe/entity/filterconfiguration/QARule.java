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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QARule
{
    private String check = null;
    private boolean isRE = false;
    private String description = null;
    private int priority = 255;
    private List<QARuleException> exceptions = new ArrayList<QARuleException>();

    public QARule()
    {
    }

    public QARule(String check, boolean isRE, String description, int priority)
    {
        this.check = check;
        this.isRE = isRE;
        this.description = description;
        this.priority = priority;
    }

    public static QARule initFromElement(Element tagElement)
    {
        Node checkElement = tagElement.getElementsByTagName("check").item(0);
        String check = checkElement.getFirstChild().getNodeValue();

        boolean isRE = false;
        Node isREElement = tagElement.getElementsByTagName("isRE").item(0);
        isRE = "true".equals(isREElement.getFirstChild().getNodeValue());

        Node descElement = tagElement.getElementsByTagName("description").item(
                0);
        String description = descElement.getFirstChild().getNodeValue();

        Node priorityElement = tagElement.getElementsByTagName("priority")
                .item(0);
        String p = priorityElement.getFirstChild().getNodeValue();

        int priority = 255;
        try
        {
            priority = Integer.parseInt(p);
        }
        catch (Exception ex)
        {
        }

        QARule qaRule = new QARule(check, isRE, description, priority);

        List<QARuleException> exceptions = new ArrayList<QARuleException>();
        NodeList exceptionsElements = tagElement
                .getElementsByTagName("exceptions");
        if (exceptionsElements != null && exceptionsElements.getLength() > 0)
        {
            for (int i = 0; i < exceptionsElements.getLength(); i++)
            {
                Node node = exceptionsElements.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element element = (Element) node;
                QARuleException exception = QARuleException
                        .initFromElement(element);
                exceptions.add(exception);
            }
            qaRule.setExceptions(exceptions);
        }

        return qaRule;
    }

    public String getCheck()
    {
        return check;
    }

    public void setCheck(String check)
    {
        this.check = check;
    }

    public boolean isRE()
    {
        return isRE;
    }

    public void setRE(boolean isRE)
    {
        this.isRE = isRE;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public List<QARuleException> getExceptions()
    {
        return exceptions;
    }

    public void setExceptions(List<QARuleException> exceptions)
    {
        this.exceptions = exceptions;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("check=");
        sb.append(this.check);
        sb.append("|isRE=");
        sb.append(this.isRE);
        sb.append("|description=");
        sb.append(this.description);
        sb.append("|priority=");
        sb.append(this.priority);
        if (exceptions.size() > 0)
        {
            sb.append("|exceptions=[");
            for (QARuleException exception : exceptions)
            {
                sb.append(exception.toString());
                sb.append(", ");
            }
            sb.append("]");
        }

        return sb.toString();
    }
}
