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

public class CustomTextRuleSid implements CustomTextRuleBase
{
    private String startString = null;
    private boolean startIsRegEx = false;
    private String startOccurrence = null;
    private String finishString = null;
    private boolean finishIsRegEx = false;
    private String finishOccurrence = null;

    public CustomTextRuleSid()
    {
    }

    public CustomTextRuleSid(String startString, boolean startIsRegEx,
            String startOccurrence, String finishString, boolean finishIsRegEx,
            String finishOccurrence)
    {
        this.startString = startString;
        this.startIsRegEx = startIsRegEx;
        this.startOccurrence = startOccurrence;

        this.finishString = finishString;
        this.finishIsRegEx = finishIsRegEx;
        this.finishOccurrence = finishOccurrence;
    }

    public String getStartString()
    {
        return startString;
    }

    public void setStartString(String startString)
    {
        this.startString = startString;
    }

    public boolean getStartIsRegEx()
    {
        return startIsRegEx;
    }

    public void setStartIsRegEx(boolean startIsRegEx)
    {
        this.startIsRegEx = startIsRegEx;
    }

    public String getStartOccurrence()
    {
        return startOccurrence;
    }

    public void setStartOccurrence(String startOccurrence)
    {
        this.startOccurrence = startOccurrence;
    }

    public String getFinishString()
    {
        return finishString;
    }

    public void setFinishString(String finishString)
    {
        this.finishString = finishString;
    }

    public boolean getFinishIsRegEx()
    {
        return finishIsRegEx;
    }

    public void setFinishIsRegEx(boolean finishIsRegEx)
    {
        this.finishIsRegEx = finishIsRegEx;
    }

    public String getFinishOccurrence()
    {
        return finishOccurrence;
    }

    public void setFinishOccurrence(String finishOccurrence)
    {
        this.finishOccurrence = finishOccurrence;
    }

    /*
     * String startString , boolean startIsRegEx , String startOccurrence ,
     * String finishString , boolean finishIsRegEx , String finishOccurrence ,
     */
    public static CustomTextRuleSid initFromElement(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("startString")
                .item(0);
        String startString = tagNameElement.getFirstChild().getNodeValue();

        Node startOccurrenceElement = tagElement.getElementsByTagName(
                "startOccurrence").item(0);
        String startOccurrence = startOccurrenceElement.getFirstChild()
                .getNodeValue();

        Node finishStringElement = tagElement.getElementsByTagName(
                "finishString").item(0);
        String finishString = finishStringElement.getFirstChild() == null ? ""
                : finishStringElement.getFirstChild().getNodeValue();

        Node finishOccurrenceElement = tagElement.getElementsByTagName(
                "finishOccurrence").item(0);
        String finishOccurrence = finishOccurrenceElement.getFirstChild()
                .getNodeValue();

        NodeList startIsRegExElements = tagElement
                .getElementsByTagName("startIsRegEx");
        boolean startIsRegEx = false;
        if (startIsRegExElements != null
                && startIsRegExElements.getLength() > 0)
        {
            startIsRegEx = "true".equals(startIsRegExElements.item(0)
                    .getFirstChild().getNodeValue());
        }

        NodeList finishIsRegExElements = tagElement
                .getElementsByTagName("finishIsRegEx");
        boolean finishIsRegEx = false;
        if (finishIsRegExElements != null
                && finishIsRegExElements.getLength() > 0)
        {
            finishIsRegEx = "true".equals(finishIsRegExElements.item(0)
                    .getFirstChild().getNodeValue());
        }

        CustomTextRuleSid ee = new CustomTextRuleSid(startString, startIsRegEx,
                startOccurrence, finishString, finishIsRegEx, finishOccurrence);

        return ee;
    }

    public String getName()
    {
        return startString;
    }

    @Override
    public boolean getIsMultiline()
    {
        return false;
    }
}
