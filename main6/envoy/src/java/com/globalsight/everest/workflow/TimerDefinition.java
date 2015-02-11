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

import java.util.Vector;
import java.io.Serializable;

/**
 * TimerDefinition is a wrapper for workflow template's timer information.  
 * The timer information include:
 * 1. Duration type - a boolean that determines whether it's "Accept" or 
 *    "Complete" duration.
 * 2. Timer type - The type of the timer (relative, absolute, periodic).
 * 3. Timer Aciton type - The type of the timer action (i.e. send mail).
 * 4. WorkflowDataItem - An object that wraps user-defined attributes used 
 *    for timer information.
 */
public class TimerDefinition
implements Serializable
{
    
	private static final long serialVersionUID = 1L;
	//
    // PRIVATE MEMBER VARIABLES
    //
    private String m_timerPrefix;
    private int m_timerType;
    private int m_timerAction;
    private Vector m_dataItemRef;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * TimerDefinition constructor.
     * @param p_isAccept flag indicating that a timer prefix of "ACCEPT"
     * should be used.
     * @param p_dataItemRef a vector of WorkflowDataItems containing user
     * attributes.
     */
    public TimerDefinition(boolean p_isAccept, 
                           Vector p_dataItemRef)
    {
        this(p_isAccept,
             WorkflowConstants.RELATIVE, 
             WorkflowConstants.SENDMAIL,
             p_dataItemRef);
    }

    /**
     * TimerDefinition constructor.
     * @param p_isAccept flag indicating that a timer prefix of "ACCEPT"
     * should be used.
     * @param p_timerType the timer type (relative, absolute, or periodic).  
     * Currently we only support the relative type.
     * @param p_timerAction the timer's action type (send mail, script, or 
     * escalate).  Currently send mail is the only supported type.
     * @param p_dataItemRef a vector of WorkflowDataItems containing user
     * attributes.
     */
    public TimerDefinition(boolean p_isAccept,
                           int p_timerType, 
                           int p_timerAction, 
                           Vector p_dataItemRef)
    {
        this((p_isAccept ? WorkflowConstants.ACCEPT : WorkflowConstants.COMPLETE),
             p_timerType,
             p_timerAction,
             p_dataItemRef);
    }

    /**
     * TimerDefinition constructor.
     * @param p_timerPrefix the timer prefix of "ACCEPT" that should be used
     * in the timer name.
     * @param p_timerType the timer type (relative, absolute, or periodic).  
     * Currently we only support the relative type.
     * @param p_timerAction the timer's action type (send mail, script, or 
     * escalate).  Currently send mail is the only supported type.
     * @param p_dataItemRef a vector of WorkflowDataItems containing user
     * attributes.
     */
    public TimerDefinition(String p_timerPrefix,
                           int p_timerType, 
                           int p_timerAction, 
                           Vector p_dataItemRef)
    {
        m_timerPrefix = p_timerPrefix;
        m_timerType = p_timerType;
        m_timerAction = p_timerAction;
        m_dataItemRef = p_dataItemRef;
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Get the prefix of the timer name.
     * @return The timer prefix.
     */
    public String getTimerPrefix()
    {
        return m_timerPrefix;
    }


    /**
     * Get the list of DataItemRefs (user-defined attributes).
     * @return A list of data item refs.
     */
    public Vector getDataItemRefs()
    {
        return m_dataItemRef;
    }


    /**
     * Get the timer type (Relative, Absolute, or Periodic).
     * @return The timer type.
     */
    public int getTimerType()
    {
        return m_timerType;
    }

    /**
     * Get the timer action type (send mail, escalate, or script).
     * @return Get the timer action's type.
     */
    public int getTimerAction()
    {
        return m_timerAction;
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(", m_timerPrefix=");
        sb.append(m_timerPrefix);
        sb.append(", m_timerType=");
        sb.append(m_timerType);
        sb.append(", m_timerAction=");
        sb.append(m_timerAction);
        return sb.toString();
    }
}
