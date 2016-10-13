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
package com.globalsight.everest.customform;

// globalsight
import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class represents a custom field which contains the name and value of the
 * custom field. The type is stored in the custom form XML itself.
 */
public class CustomField extends PersistentObject
{
    private static final long serialVersionUID = 3199093437787312908L;

    public final static String M_VALUE = "m_value";

    // private data members

    /**
     * The key's format is <section name>.<field name>
     */
    private String m_key = null;
    private String m_value = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////

    /**
     * Constructor
     */
    public CustomField()
    {
        super();
    }

    public CustomField(String p_key, String p_value)
    {
        super();
        m_key = p_key;
        m_value = p_value;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the key of this custom field.
     * 
     * @return The custom field's key.
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Set the key of the custom field.
     * 
     * @param p_key
     *            The unique key to the custom field.
     */
    public void setKey(String p_key)
    {
        m_key = p_key;
    }

    /**
     * Get the section name that this field is in. It is the first part of the
     * key.
     */
    public String getSectionName()
    {
        int index = getKey().indexOf('.');
        if (index == -1)
        {
            index = getKey().length();
        }
        return getKey().substring(0, index);
    }

    /**
     * Get the field's name - it is the second part of the key.
     */
    public String getFieldName()
    {
        String fieldName = null;
        int index = getKey().indexOf('.');
        if (index != -1)
        {
            fieldName = getKey().substring(index + 1);
        }
        return fieldName;
    }

    /**
     * Get the value of the custom field.
     * 
     * @return The value of the custom field.
     */
    public String getValue()
    {
        return m_value;
    }

    /**
     * Set the value of the custom field.
     * 
     * @param p_value
     *            The value of the custom field.
     */
    public void setValue(String p_value)
    {
        m_value = p_value;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////
}
