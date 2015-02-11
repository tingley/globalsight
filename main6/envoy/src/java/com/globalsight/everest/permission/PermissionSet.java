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
package com.globalsight.everest.permission;

import java.util.BitSet;

import org.apache.log4j.Logger;

/**
 * Represents the set of permissions that a user or a PermissionGroup allows.
 * The permission set only says what permissions a user has, not what
 * permissions they are denied.
 */
public class PermissionSet extends java.util.BitSet
{
    private static final long serialVersionUID = -1324128016050781434L;

    private static final Logger s_logger = Logger
            .getLogger(PermissionSet.class);

    /** Default constructor. */
    public PermissionSet()
    {
        super();
    }

    /**
     * Constructs a PermissionSet from a string representation of a
     * permissionSet in the format "|1|2|3|" where 1,2,3 are the ids of
     * permissions allowed.
     */
    public PermissionSet(String p_permissionSetString)
    {
        super();

        if (p_permissionSetString == null || p_permissionSetString.length() < 3)
        {
            return;
        }

        try
        {
            String ids[] = p_permissionSetString.split("\\|");

            for (int i = 0; i < ids.length; i++)
            {
                try
                {
                    int bit = Integer.parseInt(ids[i]);
                    this.set(bit);                    
                }
                catch (Exception ignore)
                {
                    
                }
            }
        }
        catch (Exception ex)
        {
            s_logger.error("Invalid argument given to PermissionSet()", ex);

            throw new IllegalArgumentException(
                    "Invalid argument given to PermissionSet(): "
                            + ex.getMessage());
        }
    }

    /**
     * Returns a boolean that is true if the user has the given permission, else
     * false. The permission name must be a permission defined in
     * Permission.java If the permission does not exist, then an error is
     * logged, but false is returned.
     */
    public boolean getPermissionFor(String p_permissionName)
    {
        try
        {
            int bit = Permission.getBitValueForPermission(p_permissionName);
            return this.get(bit);
        }
        catch (PermissionException ex)
        {
            s_logger.error("Failed to find the requested permission '"
                    + p_permissionName + "': " + ex.getMessage());

            return false;
        }
    }

    /**
     * Sets the given permission to true or false.
     * 
     * If the permission name is not valid, then an exception is logged but no
     * exception is thrown.
     * 
     * @param p_permissionName --
     *            permission name (from Permission.java)
     * @param p_state --
     *            state to set the given permission to
     */
    public void setPermissionFor(String p_permissionName, boolean p_state)
    {
        try
        {
            int bit = Permission.getBitValueForPermission(p_permissionName);
            this.set(bit, p_state);
        }
        catch (PermissionException ex)
        {
            s_logger.error("Failed to set the requested permission '"
                    + p_permissionName + "': " + ex.getMessage());
        }
    }

    /**
     * Returns true if this permission set is a subset of the other permission
     * set. For example, if this set is {3,4} and the other set is {3,4,5} then
     * this would return true. If the other set is {4,5,6} then this would
     * return false. If the other set is {3,4} then it returns true.
     * 
     * @param p_other
     *            some other permission set
     * @return boolean
     */
    public boolean isSubSet(PermissionSet p_other)
    {
        // if this set is greater than the other, than no
        if (this.cardinality() > p_other.cardinality())
        {
            return false;
        }

        BitSet a = (BitSet) this.clone();

        a.and(p_other);

        if (a.equals(this))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the PermissionSet as a String in the format: |1|2|3| where 1,2,3
     * are permission ids.
     */
    public String toString()
    {
        String oldFormat = super.toString();
        String s = oldFormat.substring(1, oldFormat.length() - 1);
        String ids[] = s.split(",");
        StringBuffer newFormat = new StringBuffer("|");

        for (int i = 0; i < ids.length; i++)
        {
            String id = ids[i].trim();
            newFormat.append(id).append("|");
        }

        return newFormat.toString();
    }
}
