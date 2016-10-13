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

public class TextCondition extends PersistentObject implements Condition
{
    private static final long serialVersionUID = -8364973139190719455L;
    private Integer length;

    public Integer getLength()
    {
        return length;
    }

    public void setLength(Integer length)
    {
        this.length = length;
    }

    @Override
    public String getType()
    {
        return Attribute.TYPE_TEXT;
    }

    @Override
    public void updateCondition(HttpServletRequest request, Attribute attribute)
    {
        Condition condition = attribute.getCondition();
        TextCondition textCondition;
        if (condition instanceof ListCondition)
        {
            textCondition = (TextCondition) condition;
        }
        else
        {
            textCondition = new TextCondition();
            attribute.setCondition(textCondition);
        }
        
        String length = request.getParameter("textLength");
        if (length != null && length.length() > 0)
        {
            textCondition.setLength(Integer.parseInt(length));
        }
        else
        {
            textCondition.setLength(null);
        }
    }

    @Override
    public Condition getCloneCondition()
    {
        TextCondition condition = new TextCondition();
        condition.setLength(length);
        return condition;
    }
}
