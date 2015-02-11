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
package com.globalsight.reports.util;

/**
* Holds a value and its description
*/
public class LabeledValueHolder
{
    /**
    * Creates a ValueHolder with the given object and label
    * @param p_value -- the true object to hold
    * @param p_description -- a  label
    */
    public LabeledValueHolder(Object p_value, String p_label)
    {
        m_value = p_value;
        m_label = p_label;
    }

    /**
    * Constructs an empty value holder
    * <br>
    */
    public LabeledValueHolder()
    {
        m_value = null;
        m_label = null;
    }

    /**
    * Returns the label
    * <br>
    * @return String
    */
    public String toString()
    {
        return m_label;
    }

    /**
    * Gets the value being held
    * <br>
    * @return String
    */
    public Object getValue()
    {
        return m_value;
    }
    
    /**
    * Sets the value being held
    * <br>
    */
    public void setValue(Object p_value)
    {
        m_value = p_value;
    }
    /**
    * Gets the label of the value being held
    * <br>
    * @return String
    */
    public String getLabel()
    {
        return m_label;
    }
    
    /**
    * Sets the label
    * <br>
    */
    public void setLabel(String p_label)
    {
        m_label = p_label;
    }

    private Object m_value;
    private String m_label;
}

