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

package com.globalsight.ui.attribute.vo;

import javax.xml.bind.annotation.XmlRootElement;

import com.globalsight.util.Assert;

@XmlRootElement
public class FloatJobAttributeVo extends JobAttributeVo
{
    private Float max;
    private Float min;
    private Float value;

    public Float getMin()
    {
        return min;
    }

    public void setMin(Float min)
    {
        this.min = min;
    }

    public Float getMax()
    {
        return max;
    }

    public void setMax(Float max)
    {
        this.max = max;
    }

    public Float getValue()
    {
        return value;
    }

    public void setValue(Float value)
    {
        this.value = value;
    }

    public String getLabel()
    {
        if (value == null)
            return "";

        return value.toString();
    }

    public Float convertedToFloat(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        Assert.assertIsFloat(s);
        Float f = Float.parseFloat(s);
        Assert.assertFloatBetween(f, min, max);
        return f;
    }
    
    public boolean isSetted()
    {
        return value != null;
    }
}
