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

 
package com.globalsight.everest.costing;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.globalsight.everest.persistence.PersistentObject;



/**
 * This abstract class represents a surcharge placed on a cost.
 * All subclasses of surcharges should implement the specified
 * method that is used by the costing engine to determine the
 * surcharge amount to add to the cost.
 * 
 * The name of every Surcharge is Unique.
 */
public abstract class Surcharge
    extends PersistentObject
{
    private static String[] c_allTypes;

    /**
     * Valid types of surcharges
     */
    public static final String FLAT_FEE = "FlatSurcharge";
    public static final String PERCENTAGE = "PercentageSurcharge";

    public static final String TYPE_FLAT = "F";
    public static final String TYPE_PERCENTAGE = "P";

    // back-pointer to Cost object for TOPLink use (persist foreign key)
    private Cost m_cost;

    static
    {
        c_allTypes = new String[2];
        c_allTypes[0] = FLAT_FEE;
        c_allTypes[1] = PERCENTAGE;
    }


    /**
     * Returns all the valid types of surcharges.
     */
    public static String[] getTypes()
    {
        return c_allTypes;
    }   

    /**
     * Returns the type of surcharge in string format.
     * This is from the static strings specified above in this superclass.
     */
    abstract public String getType();

    /**
     * Returns the amount of surcharge to be added to the cost.
     * 
     * @param p_totalCost The cost to apply the surcharge to.
     *                    A NULL could be passed in if the class that 
     *                   implements this doesn't need to know the total cost
     *                   (like flat fees).
     */
    abstract public Money surchargeAmount(Money p_cost);

    public Cost getCost()
    {
    	return m_cost;
    }
    
    public void setCost(Cost p_cost)
    {
        m_cost = p_cost;
    }
    
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(m_name);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        Surcharge other = (Surcharge) obj;
        if (m_name == null || !m_name.equals(other.m_name))
            return false;

        return true;
    }
}
