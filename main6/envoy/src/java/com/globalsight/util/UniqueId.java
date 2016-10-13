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

package com.globalsight.util;


import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.server.UID;

import org.apache.log4j.Logger;


/**
 * Represents a unique identifier.  It is unique in the world. 
 */
public final class UniqueId implements Serializable
{
    private static final Logger CATEGORY =
            Logger.getLogger(
            UniqueId.class.getName());
    
    private static String IP_ADDRESS = null;
    static
    {
        try
        {
            IP_ADDRESS = 
                    InetAddress.getLocalHost().getHostAddress();
        }
        catch (Exception e)
        {
            CATEGORY.error("UniqueId constructor " + e.toString(), e);
            IP_ADDRESS = "0.0.0.0";
        }
    }

    private String m_uid = null;

    public UniqueId()
    {
        m_uid = new UID().toString() + " " + IP_ADDRESS;
    }


    /**
     * Compares two UniqueId for content equality.
     * @param p_object - the Object to compare with.
     * @return true if these Objects are equal; false otherwise.
     */
    public boolean equals(Object p_object)
    {
        if (p_object instanceof UniqueId)
        {
            return (m_uid == ((UniqueId)p_object).m_uid);
        }
        return false;  
    }


    /** Returns a hashcode for the UniqueId. 
     * Two UniqueIds will have the same hashcode if they are 
     * equal with respect to their content.
     * @return the hashcode.
     */
    public int hashCode()
    {
        return m_uid.hashCode();
    }
    
    /**
     * Returns the string representation of the UniqueId.
     * @return Returns the string representation of the UniqueId.
     */
    public String toString()
    {
        return m_uid;
    }  
}
