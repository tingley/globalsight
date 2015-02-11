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

public class FloatCondition extends PersistentObject implements Condition
{
    private static final long serialVersionUID = 1856887519822521234L;
    private Integer definition;
    private Float max;
    private Float min;

    public Integer getDefinition()
    {
        return definition;
    }

    public void setDefinition(Integer definition)
    {
        this.definition = definition;
    }

    public Float getMax()
    {
        return max;
    }

    public void setMax(Float max)
    {
        this.max = max;
    }

    public Float getMin()
    {
        return min;
    }

    public void setMin(Float min)
    {
        this.min = min;
    }

    @Override
    public String getType()
    {
        return Attribute.TYPE_FLOAT;
    }

    @Override
    public void updateCondition(HttpServletRequest request, Attribute attribute)
    {
        Condition condition = attribute.getCondition();
        FloatCondition floatCondition;
        if (condition instanceof FloatCondition)
        {
            floatCondition = (FloatCondition) condition;
        }
        else
        {
            floatCondition = new FloatCondition();
            attribute.setCondition(floatCondition);
        }

        String max = request.getParameter("floatMax");
        if (max != null && max.length() > 0)
        {
            floatCondition.setMax(Float.valueOf(max));
        }
        else
        {
            floatCondition.setMax(null);
        }

        String min = request.getParameter("floatMin");
        if (min != null && min.length() > 0)
        {
            floatCondition.setMin(Float.valueOf(min));
        }
        else
        {
            floatCondition.setMin(null);
        }
        
        String definition = request.getParameter("definition");
        if (definition != null && definition.length() > 0)
        {
            floatCondition.setDefinition(Integer.valueOf(definition));
        }
        else
        {
            floatCondition.setDefinition(null);
        }
    }

    @Override
    public Condition getCloneCondition()
    {
        FloatCondition condition = new FloatCondition();
        condition.setMax(this.max);
        condition.setMin(this.min);
        condition.setDefinition(this.definition);
        return condition;
    }
}
