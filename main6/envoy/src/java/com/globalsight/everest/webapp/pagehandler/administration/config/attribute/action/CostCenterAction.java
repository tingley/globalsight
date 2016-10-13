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

package com.globalsight.everest.webapp.pagehandler.administration.config.attribute.action;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.JobAttribute;

public class CostCenterAction implements AttributeAction
{
    static private final Logger logger = 
            Logger.getLogger(CostCenterAction.class);

    private static final String NAME = "protect_cost_center";
    private boolean isSeted = true;

    public void run(JobAttribute jobAttribute)
    {
        if (jobAttribute == null)
            return;

        if (NAME.equals(jobAttribute.getAttribute().getName()))
        {
            String value = jobAttribute.getStringValue();
            if (value == null || value.length() == 0)
            {
                isSeted = false;
            }
        }
    }

    public boolean isSeted()
    {
        return isSeted;
    }
}
