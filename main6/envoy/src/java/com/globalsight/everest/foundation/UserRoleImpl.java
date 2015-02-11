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

public class UserRoleImpl
    extends RoleImpl
    implements UserRole
{
    private String m_user = null;
    private String m_cost = null;
    private String m_rate = null;

    public UserRoleImpl()
    {
    }

    public String getUser()
    {
        return m_user;
    }

    public void setUser(String p_user)
    {
        m_user = p_user;
    }

    public String getCost()
    {
        return m_cost;
    }

    public void setCost(String p_cost)
    {
        m_cost = p_cost;
    }

    public String getRate()
    {
        return m_rate;
    }

    public void setRate(String p_rate)
    {
        m_rate = p_rate;
    }

    public String getName()
    {
        String roleName = super.getName();

        if (roleName == null || roleName.length() <= 0)
        {
            StringBuffer name = new StringBuffer();

            name.append(getActivity().getId());  name.append(" ");
            name.append(getActivity().getName()); name.append(" ");
            name.append(getSourceLocale()); name.append(" ");
            name.append(getTargetLocale());  name.append(" ");
            name.append(m_user);

            roleName = name.toString();
            setName(roleName);
        }

        return roleName;
    }

    public String toString()
    {
        return super.toString() +
            " m_user=" + (m_user != null ? m_user.toString() : "null") +
            " m_rate=" + (m_rate != null ? m_rate.toString() : "null") +
            " m_cost=" + (m_cost != null ? m_cost.toString() : "null");
    }

    /**
     * Compare if the objects are equal.
     * First compare the super class "Role" and then check just
     * the "rate" object too since it is just one for a
     * user role.
     */
    public boolean equals(Object o)
    {
        UserRole ur = (UserRole)o;
        boolean theSame = super.equals(ur);
        if (theSame)
        {
            if (getRate() != null && ur.getRate() != null)
            {
                if (!getRate().equals(ur.getRate()))
                {
                    theSame = false;
                }
            }
            else
            {
                if ((getRate() == null && ur.getRate() != null) ||
                    (getRate() != null && ur.getRate() == null))
                {
                    theSame = false;
                }
            }
        }
        return theSame;
    }
}
