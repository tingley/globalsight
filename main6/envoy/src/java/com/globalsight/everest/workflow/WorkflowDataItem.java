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
package com.globalsight.everest.workflow;


import java.io.Serializable;


/**
 * WorkflowDataItem is a wrapper for iflow's DataItemRef (user-defined attributes).  
 * Note:  In order to create a WorkflowDataItem object for a timer or email 
 * related action types, you need to follow these instructions:
 *
 * 1. Creating user-defined attributes for a "timer":
 * WorkflowDataItem di = new WorkflowDataItem(true, "6000"); 
 * The boolean value "true" indicates that this attribute is for the "Accept" 
 * duration with a value of 6000 milliseconds.  For a "Complete" duration, the 
 * boolean value should be false.
 *
 * 2. Creating user-defined attributes for email action type of a timer:
 * WorkflowDataItem di = new WorkflowDataItem(String.valueOf(WorkflowConstants.MAIL_TO), 
 *                                           WorkflowConstants.STRING, 
 *                                           "someone@gs.com");
 * The first argument is basically a temporary attribute name which is used in the 
 * WorkflowTemplateHelper for distinguishing between different action types 
 * (i.e. mail to, mail from, ...).
 * Note that the temporary name should ALWAYS be one of the valid WorkflowConstants 
 * variables for "send mail" action type of a timer.
 * 
 */
public class WorkflowDataItem 
implements Serializable
{
    
	private static final long serialVersionUID = -2169377717237809381L;
	//
    // PRIVATE MEMBER VARIABLES
    //
    private String m_name = null;
    private String m_type = null;
    private String m_value = null;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * WorkflowDataItem constructor. Create user-defined process attributes for
     * a timer.
     *
     * @param p_isAccept determines whether it's an Accept duration (duration 
     * for accepting the task vs. "Complete" which is duration for task 
     * completion).
     * @param p_value the value of the attribute.    
     */
    public WorkflowDataItem(boolean p_isAccept, String p_value)
    {                
        this((p_isAccept ? WorkflowConstants.ACCEPT : WorkflowConstants.COMPLETE),
             WorkflowConstants.LONG,
             p_value);
    }

    /**
     * WorkflowDataItem constructor. Create user-defined process attributes for
     * a timer.
     *
     * @param p_name the name of the attribute.
     * @param p_value the value of the attribute.    
     */
    public WorkflowDataItem(String p_name, String p_value)
    {                
        this(p_name,
             WorkflowConstants.LONG,
             p_value);
    }

    /**
     * WorkflowDataItem constructor. Create user-defined process attributes.
     *
     * @param p_name - The name of the attribute.
     * @param p_type - The type of the attribute.
     * @param p_value - The value of the attribute.
     */
    public WorkflowDataItem(String p_name, String p_type, String p_value)
    {
        m_name = p_name;
        m_type = p_type;
        m_value = p_value;
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Get the name of the data item.
     * @return The workflow data item's name.
     */
    public String getName()
    {
        return m_name;
    }


    /**
     * Get the type of the data item value.
     * @return The type of the data item value.
     */
    public String getType()
    {
        return m_type;
    }


    /**
     * Set the data item's value to be the specified value.
     * @param p_value - The value to be set.
     */
    public void setValue(String p_value)
    {
        m_value = p_value;
    }


    /**
     * Get the data item's value.
     * @return The value of the data item.
     */
    public String getValue()
    {
        return m_value;
    }    


    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(", m_name=");
        sb.append(m_name);
        sb.append(", m_type=");
        sb.append(m_type);
        sb.append(", m_value=");
        sb.append(m_value);
        return sb.toString();
    } 
}
