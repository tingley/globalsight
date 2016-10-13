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

package com.globalsight.everest.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

public class PriorityComparator implements Comparator<Object>,
        Serializable
{

    @Override
    public int compare(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
        {
            return 0;
        }

        if (o1 == null)
        {
            return -1;
        }

        if (o2 == null)
        {
            return 1;
        }

        if (o1 instanceof Priorityable && o2 instanceof Priorityable)
        {
            Priorityable p1 = (Priorityable) o1;
            Priorityable p2 = (Priorityable) o2;
            
            if (p1.getPriority() == p2.getPriority())
            {
                return p1.getName().compareTo(p2.getName());
            }
            else
            {
                return p1.getPriority() - p2.getPriority();
            }
        }
        else
        {
            return o1.equals(o2) ? 0 : -1;
        }
    }

}
