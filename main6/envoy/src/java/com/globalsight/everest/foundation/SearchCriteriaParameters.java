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

import java.io.Serializable;
import java.util.Map;
import java.util.Hashtable;

/**
 * This class is a set of search criteria that is passed around
 * between client and server objects.  Clients set the required
 * search criteria using various setXXX() methods in its subclasses
 * and servers extract the search criteria using getParameters().
 */
public class SearchCriteriaParameters
    implements Serializable
{

    private static final long serialVersionUID = -3727777619972504749L;
    
    // Public constants
    public static final String GREATER_THAN = "GT";
    public static final String EQUALS = "EQ";
    public static final String LESS_THAN = "LT";
    public static final String GREATER_EQUALS = "GE";
    public static final String LESS_EQUALS = "LE";
    
    public static final String BEGINS_WITH = "BW";
    public static final String CONTAINS = "CT";
    public static final String ENDS_WITH = "EW";

    public static final String HOURS_AGO = "HA";
    public static final String DAYS_AGO = "DA";
    public static final String WEEKS_AGO = "WA";
    public static final String MONTHS_AGO = "MA";
    public static final String NOW = "NOW";

    public static final String HOURS_FROM_NOW = "HFN";
    public static final String DAYS_FROM_NOW = "DFN";
    public static final String WEEKS_FROM_NOW = "WFN";
    public static final String MONTHS_FROM_NOW = "MFN";
    
    // a map that contains the search criteria
    private Map m_parameters;

    private boolean m_isCaseSensitive;

    /**
     * Constructor.  Creates the search criteria map.
     */
    public SearchCriteriaParameters()
    {
        m_parameters = new Hashtable();
    }

    /**
     * This method is used by a server to extract the search criteria
     * specified by the client.
     *
     * @return a two dimensional array of objects that represent the
     * search criteria.  The first column is an Integer id that
     * identifies what type of criteria it is (e.g. release version
     * number), the second column is the value of the criteria.
     */
    public Map getParameters()
    {
        return m_parameters;
    }

    /**
     * Determines whether the case sensitivity should be taken into
     * consideration during the search process.
     */
    public boolean isCaseSensitive()
    {
        return m_isCaseSensitive;
    }


    /**
     * Set the flag for a search ignoring the case.
     */
    public void isCaseSensitive(boolean p_isCaseSensitive)
    {
        m_isCaseSensitive = p_isCaseSensitive;
    }

    /**
     * Adds the key and value to the parameters map.
     */
    protected void addElement(int p_id, Object p_value)
    {
    	if (p_value instanceof String) {
    		p_value = String.valueOf(p_value).trim();
    	}
        m_parameters.put(new Integer(p_id), p_value);
    }
}
