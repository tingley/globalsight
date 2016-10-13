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

package com.globalsight.everest.page.pageimport;

import java.util.ArrayList;

import com.globalsight.everest.page.TemplatePart;

/**
 * A SubProcessResult contains returned data fields for template
 * generation sub-process, such as
 * processOnGxmlRootForDetailAndReview() and
 * processOnGxmlRootForExport().
*/
public class SubProcessResult
{
    private int tuPointer;
    private int partOrder;
    private ArrayList<TemplatePart> templateParts;
    private String rest;

    // used for Gxml file where tuPointer and partOrder are the same
    // except for the last part
    public SubProcessResult(int p_tuPointer,
            ArrayList<TemplatePart> p_templateParts, String p_rest)
    {
        tuPointer = p_tuPointer;
        templateParts = p_templateParts;
        rest = p_rest;
    }

    public SubProcessResult(int p_tuPointer, int p_partOrder,
            ArrayList<TemplatePart> p_templateParts, String p_rest)
    {
        tuPointer = p_tuPointer;
        partOrder = p_partOrder;
        templateParts = p_templateParts;
        rest = p_rest;
    }

    public void setRest(String p_rest)
    {
        rest = p_rest;
    }

    public void setTuPointer(int p_tuPointer)
    {
        tuPointer = p_tuPointer;
    }

    public void setTemplateParts(ArrayList<TemplatePart> p_templateParts)
    {
        templateParts = p_templateParts;
    }

    public String getRest()
    {
        return rest;
    }

    public int getTuPointer()
    {
        return tuPointer;
    }

    public int getPartOrder()
    {
        return partOrder;
    }

    public ArrayList<TemplatePart> getTemplateParts()
    {
        return templateParts;
    }
}

