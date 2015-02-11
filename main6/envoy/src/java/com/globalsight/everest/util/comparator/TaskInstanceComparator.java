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

import java.util.Locale;

import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * This class is used to compare TaskInstance objects.
 */
public class TaskInstanceComparator extends StringComparator
{
    private static final long serialVersionUID = 1L;
    public static final int TASK_INSTANCE_ID = 0;

    public TaskInstanceComparator(Locale p_locale)
    {
        super(p_locale);
    }

    public TaskInstanceComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two TaskInstance objects.
     */
    public int compare(Object p_A, Object p_B)
    {
        TaskInstance a = (TaskInstance) p_A;
        TaskInstance b = (TaskInstance) p_B;

        int rv;

        switch (m_type)
        {
            default:
            case TASK_INSTANCE_ID:
                long along = a.getId();
                long blong = b.getId();
                if (along > blong)
                    rv = 1;
                else if (along == blong)
                    rv = 0;
                else
                    rv = -1;
                break;
        }

        return rv;
    }
}
