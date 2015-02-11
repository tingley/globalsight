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

import com.globalsight.everest.foundation.Role;

import java.util.Vector;


public interface ContainerRole extends Role {
    public static final String ROLE_TYPE_VALUE = "C";

    public Vector getUsers();
    public boolean addUsers(Vector p_users);
    public boolean removeUsers(Vector p_users);

    /**
     * Determines whether this container role has at least one user
     * associated with it.
     * @return True if the container role is valid (user associated with it),
     * otherwise it'll return false;
     */
    boolean isContainerRoleValid();

}
