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

package com.globalsight.cxe.entity.customAttribute;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.persistence.PersistentObject;

public class IntCondition extends PersistentObject implements Condition
{
    private static final long serialVersionUID = 1380123073531095214L;
    private Integer max;
    private Integer min;

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

    @Override
    public String getType()
    {
        return Attribute.TYPE_INTEGER;
    }

    @Override
    public void updateCondition(HttpServletRequest request, Attribute attribute)
    {
        Condition condition = attribute.getCondition();
        IntCondition intCondition;
        if (condition instanceof IntCondition)
        {
            intCondition = (IntCondition) condition;
        }
        else
        {
            intCondition = new IntCondition();
            attribute.setCondition(intCondition);
        }

        
        String max = request.getParameter("intMax");
        if (max != null && max.length() > 0)
        {
            intCondition.setMax(Integer.valueOf(max));
        }
        else
        {
            intCondition.setMax(null);
        }

        String min = request.getParameter("intMin");
        if (min != null && min.length() > 0)
        {
            intCondition.setMin(Integer.valueOf(min));
        }
        else
        {
            intCondition.setMin(null);
        }
    }

    @Override
    public Condition getCloneCondition()
    {
        IntCondition condition = new IntCondition();
        condition.setMax(this.max);
        condition.setMin(this.min);
        return condition;
    }
}
