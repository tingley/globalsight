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
package com.globalsight.everest.workflow;

//i-Flow
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;


/**
 * A helper class used by WorkFlowAdapterProcessImpl and WorkFlowAdapterTemplateImpl.
 */
public class WorkflowAdapterHelper

{
    /**
     * Find the valid (non-removed) number of branch specs in a condition node.
     * @param p_wfBranchSpecs - A list of branch specs.
     * @return An integer representing the number of branches in a condition node.
     */
    public static int findBranchSize (List p_wfBranchSpecs)
   {
       int size = 0;
       for (int i=0;i<p_wfBranchSpecs.size();i++)
       {
           WorkflowBranchSpec p_wfBranchSpec = 
               (WorkflowBranchSpec)p_wfBranchSpecs.get(i); 

           if (p_wfBranchSpec.getStructuralState() != 
               WorkflowConstants.REMOVED)
           {           
               size++;
           }
       }
       return size;
   }

    
   
    
    /**
     * Get the maximum number for the given array of integers.
     * @param p_array - An array of integers.
     * @return The max number of the array.
     */
    public static int getMaxSeq(int [] p_array)
    {
        int maxNumber =-1;
        for (int i=0;i<p_array.length;i++)
        {
            if (maxNumber < p_array[i])
            {
                maxNumber = p_array[i];
            }
        }
        return maxNumber;

    }
    
    public static int getMaxSeq(List p_wfTaskInstances)
	{
		int maxNumber = -1;
		Iterator it = p_wfTaskInstances.iterator();
		while (it.hasNext())
		{
			WorkflowTask taskInstance = (WorkflowTask) it
					.next();
			if (taskInstance.getType() != WorkflowConstants.ACTIVITY)
			{
				continue;
			}

			if (maxNumber < taskInstance.getSequence())
			{
				maxNumber = taskInstance.getSequence();
			}
		}

		return maxNumber;
	}

    /**
     * returns true if costing is enabled
     * 
     * @return boolean true is enabled
     */
    public static boolean isCostingEnabled()
    throws Exception
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            return sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        }
        catch(Exception e)
        {
            throw new Exception("WorkflowAdapterHelper::Error in getting costing data " + e);
        }
    }
    /**
     * returns true if revenue is enabled
     * @return boolean true is enabled
     */
    public static boolean isRevenueEnabled()
    throws Exception
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            return sc.getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);
        }
        catch(Exception e)
        {
            throw new Exception("WorkflowAdapterHelper::Error in getting costing data " + e);
        }
    }

    /**
     * Parses the string argument as a signed decimal integer. Upon a 
     * NumberFormatException, the default value argument is returned.
     */
    static int parseInt(String p_value, int p_defaultValue)
    {
        try
        {
            p_defaultValue = Integer.parseInt(p_value);
        }
        catch (NumberFormatException ne)
        {
        }

        return p_defaultValue;
    }

    /**
     * Parses the string argument as a signed decimal long. Upon a 
     * NumberFormatException, the default value argument is returned.
     */
    static long parseLong(String p_value, long p_defaultValue)
    {
        try
        {
            p_defaultValue = Long.parseLong(p_value);
        }
        catch (NumberFormatException ne)
        {
        }

        return p_defaultValue;
    }
}
