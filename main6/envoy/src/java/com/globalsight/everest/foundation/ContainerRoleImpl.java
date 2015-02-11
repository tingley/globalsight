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

// java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 */
public class ContainerRoleImpl extends RoleImpl implements ContainerRole, Serializable {

    private List userIds = new ArrayList();

    public ContainerRoleImpl() 
    {
        super();
    }

    public Vector getUsers() 
    {
    	Vector users = new Vector();
    	users.addAll(userIds);
        return users;
    }

    public boolean addUsers(Vector p_users) {
        return userIds.addAll(p_users);
    }

    public boolean removeUsers(Vector p_users) {
        return userIds.removeAll(p_users);
    }

    public String toString() 
    {
        return super.toString()
                + " m_users=" + (userIds!=null?userIds.toString():"null")
                ;
    }

    /**
     * Override the getName method to calculate name if not specified.
     */
    public String getName() 
    {
        String roleName = super.getName();
        if (roleName == null || roleName.length() <= 0)
        {
            StringBuffer name = new StringBuffer();
            name.append(getActivity().getId());  name.append(" ");
            name.append(getActivity().getName()); name.append(" ");
            name.append(getSourceLocale()); name.append(" ");
            name.append(getTargetLocale());
            roleName = name.toString();
            setName(roleName);
        }
        return roleName;
    }


    /**
     * Determines whether this container role has at least one user
     * associated with it.
     * @return True if the container role is valid (user associated with it),
     * otherwise it'll return false;
     */
    public boolean isContainerRoleValid()
    {
        return userIds != null && userIds.size() > 0;
    }

	public List getUserIds() {
		return userIds;
	}

	public void setUserIds(List userIds) {
		this.userIds = userIds;
	}

}

