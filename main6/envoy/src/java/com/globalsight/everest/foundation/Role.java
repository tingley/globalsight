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

package com.globalsight.everest.foundation;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.workflow.Activity;

import java.util.Collection;

/**
 * Provides the interface for what a generic role
 * is in System4.  It consists of an activity
 * and the source/target locales that the user can
 * perform the activity for.
 */
public interface Role
{
    public boolean isActive();
    
    public String getName();
    public void setName(String p_name);

    public Activity getActivity();
    public void setActivity(Activity p_activity);

    public String getSourceLocale();
    public void setSourceLocale(String p_sourceLocale);

    public String getTargetLocale();
    public void setTargetLocale(String p_targetLocale);

    // set and get the state of the Role.
    // the valid states are listed in User.State
    public int getState();
    public void setState(int p_state);

    public Collection getRates();
    public void setRates(Collection p_rates);
    public void addRate(Rate p_rate);
    public void removeRate(Rate p_rate);

    public String toString();
    public boolean isRoleValid();

    // returns true if the role object passed in is EXACTLY
    // the same as this one - the number of rates and all.
    public boolean equals(Object o);
}

