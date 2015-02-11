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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.workflow.Activity;

public class RoleImpl extends PersistentObject implements Role
{
    private static final long serialVersionUID = 4305908032791819351L;

    private Activity m_activity = null;
    private String m_sourceLocale = null;
    private String m_targetLocale = null;
    private List m_rates = new ArrayList();
    private int m_state = User.State.CREATED;

    public RoleImpl()
    {
    }
    
    public List getRateSet()
    {
    	return m_rates;
    }
    
    public void setRateSet(List rates)
    {
    	m_rates = rates;
    }

    public boolean isActive()
    {
        if (m_state == User.State.ACTIVE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public Activity getActivity()
    {
        return m_activity;
    }

    public void setActivity(Activity p_activity)
    {
        m_activity = p_activity;
    }

    public String getSourceLocale()
    {
        return m_sourceLocale;
    }

    public void setSourceLocale(String p_sourceLocale)
    {
        m_sourceLocale = p_sourceLocale;
    }

    public String getTargetLocale()
    {
        return m_targetLocale;
    }

    public void setTargetLocale(String p_targetLocale)
    {
        m_targetLocale = p_targetLocale;
    }

    // set and get the state of the Role.
    // the valid states are listed in User.State
    public int getState()
    {
        return m_state;
    }

    public void setState(int p_state)
    {
        // if a valid state set it
        switch (p_state)
        {
            case User.State.CREATED:
            case User.State.ACTIVE:
            case User.State.DEACTIVE:
            case User.State.DELETED:
                m_state = p_state;
                break;
            default:
                // just leave as it is since the
                // passed in state isn't recognized
        }
    }

    public Collection getRates()
    {
        return m_rates;
    }

    public void setRates(Collection p_rates)
    {
        if (p_rates != null)
        {
            m_rates = new ArrayList();
            m_rates.addAll(p_rates);
        }
    }

    public void addRate(Rate p_rate)
    {
        if (p_rate != null)
        {
            m_rates.add(p_rate);
        }
    }

    public void removeRate(Rate p_rate)
    {
        m_rates.remove(p_rate);
    }

    public String toString()
    {
        return "Role m_name=" + (getName() != null ? getName() : "null")
                + " m_activity="
                + (m_activity != null ? m_activity.getName() : "null")
                + " m_sourceLocale="
                + (m_sourceLocale != null ? m_sourceLocale : "null")
                + " m_targetLocale="
                + (m_targetLocale != null ? m_targetLocale : "null")
                + " m_rates= "
                + (m_rates != null ? m_rates.toString() : "null");
    }

    public boolean isRoleValid()
    {
        if (m_activity != null && m_sourceLocale != null
                && m_targetLocale != null)
        {
            return true;
        }

        return false;
    }

    // returns true if the role object passed in is EXACTLY
    // the same as this one - the number of rates and all.
    public boolean equals(Object o)
    {
        boolean theSame = false;
        Role role = (Role) o;

        // if source, target and activity are the same
        // then check the rates
        if (getSourceLocale().toString().equalsIgnoreCase(
                role.getSourceLocale().toString())
                && getTargetLocale().toString().equalsIgnoreCase(
                        role.getTargetLocale().toString())
                && getActivity().getName().equals(role.getActivity().getName())
                && getState() == role.getState())
        {
            Collection rates1 = getRates();
            Collection rates2 = role.getRates();
            if (rates1 != null && rates1.size() > 0)
            {
                // if the same number of rates - compare the rates
                if (rates2 != null && rates1.size() == rates2.size())
                {
                    boolean ratesTheSame = true;
                    // go through all while found is true
                    // as soon as one isn't found then they aren't equal
                    for (Iterator ri1 = rates1.iterator(); ri1.hasNext()
                            && ratesTheSame == true;)
                    {
                        Rate r1 = (Rate) ri1.next();
                        boolean found = false;
                        for (Iterator ri2 = rates2.iterator(); ri2.hasNext()
                                && !found;)
                        {
                            Rate r2 = (Rate) ri2.next();
                            if (r1.getId() == r2.getId())
                            {
                                found = true;
                            }
                        }
                        // a rate wasn't found so they aren't the same
                        if (!found)
                        {
                            ratesTheSame = false;
                        }
                    }
                    if (ratesTheSame == true)
                    {
                        theSame = true;
                    }
                }
            }
            else
            // rates1 == null
            {
                if (rates2 == null || rates2.size() == 0)
                {
                    theSame = true;
                }
            }
        } // end of - if source locale, target locale and activity the same
        return theSame;
    }
}
