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

package com.globalsight.everest.webapp.pagehandler.administration.config.attribute;

public class AttributeBasicForm
{
    private String name;
    private String id;
    private String displayName;
    private String textLength;
    private String intMin;
    private String intMax;
    private String floatMin;
    private String floatMax;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getTextLength()
    {
        return textLength;
    }

    public void setTextLength(String textLength)
    {
        this.textLength = textLength;
    }

    public String getIntMin()
    {
        return intMin;
    }

    public void setIntMin(String intMin)
    {
        this.intMin = intMin;
    }

    public String getIntMax()
    {
        return intMax;
    }

    public void setIntMax(String intMax)
    {
        this.intMax = intMax;
    }

    public String getFloatMin()
    {
        return floatMin;
    }

    public void setFloatMin(String floatMin)
    {
        this.floatMin = floatMin;
    }

    public String getFloatMax()
    {
        return floatMax;
    }

    public void setFloatMax(String floatMax)
    {
        this.floatMax = floatMax;
    }
}
