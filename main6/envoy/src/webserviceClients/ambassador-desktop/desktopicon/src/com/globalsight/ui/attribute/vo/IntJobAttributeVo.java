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
public class IntJobAttributeVo extends JobAttributeVo
{
    private Integer max;
    private Integer min;
    private Integer value;

    public Integer getMax()
    {
        return max;
    }

    public void setMax(Integer max)
    {
        this.max = max;
    }

    public Integer getMin()
    {
        return min;
    }

    public void setMin(Integer min)
    {
        this.min = min;
    }

    public Integer getValue()
    {
        return value;
    }

    public void setValue(Integer value)
    {
        this.value = value;
    }
    
    public String getLabel()
    {
        if (value == null)
            return "";
        
        return value.toString();
    }
    
    public Integer convertedToInteger(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        Assert.assertIsInteger(s);
        Integer value = Integer.parseInt(s);
        Assert.assertIntBetween(value, min, max);

        return value;
    }
    
    public boolean isSetted()
    {
        return value != null;
    }
}
