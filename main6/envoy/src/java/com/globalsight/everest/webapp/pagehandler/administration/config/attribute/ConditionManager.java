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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FileCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;

public class ConditionManager
{
    private static List<Condition> conditons = new ArrayList<Condition>();
    
    static
    {
        conditons.add(new ListCondition());
        conditons.add(new FileCondition());
        conditons.add(new IntCondition());
        conditons.add(new TextCondition());
        conditons.add(new FloatCondition());
        conditons.add(new DateCondition());
    }
    
    public static void updateCondition(HttpServletRequest request, Attribute attribute)
    {
        String type = (String) request.getParameter("type");
        for (Condition condtion : conditons)
        {
            if (condtion.getType().equals(type))
            {
                condtion.updateCondition(request, attribute);
                break;
            }
        }
    }
}
