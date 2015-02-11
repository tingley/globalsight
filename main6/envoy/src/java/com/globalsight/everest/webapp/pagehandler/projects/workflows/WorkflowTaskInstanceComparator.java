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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;
/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

// java
import java.io.Serializable;
import java.util.Comparator;

// globalsight
import com.globalsight.everest.workflow.WorkflowTaskInstance;

/**
 * Comparator implementation to enable sorting of Jobs objects.
 */
public class WorkflowTaskInstanceComparator 
    implements Comparator, Serializable
{
    private static final int lessThan = -1;
    private static final int greaterThan = 1;
    private static final int equalTo = 0;

    public int compare(Object p_object1, Object p_object2)
    {
	if (!(p_object1 instanceof WorkflowTaskInstance))
	    throw new ClassCastException();
	if (!(p_object2 instanceof WorkflowTaskInstance))
	    throw new ClassCastException();

	WorkflowTaskInstance task1 = (WorkflowTaskInstance)p_object1;
	WorkflowTaskInstance task2 = (WorkflowTaskInstance)p_object2;
	
	if (task1 != null && task2 != null)
	{
	    if (task1.getSequence() < task2.getSequence())
		return lessThan;
	    else if (task1.getSequence() > task2.getSequence())
		return greaterThan;
	    else
		return equalTo;
	}
	return equalTo;
    }

    public boolean equals(Object p_object)
    {
        if (p_object == this) 
            return true;
	return false;
    }
}	





