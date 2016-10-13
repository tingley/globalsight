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
package com.globalsight.cxe.message;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;

/** The CxeMessage class represents a message that is passed
 *  between CXE components or adapters
 */
public class CxeMessage implements Serializable
{
    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private CxeMessageType m_messageType; //CxeMessageType
    private HashMap m_parameters;
    private EventFlowXml m_eventFlowObject;
    private MessageData m_messageData;
    private boolean m_deleteMessageData; //whether the message data should be deleted on a call to free()

    //////////////////////////////////////
    // Constructors                     //
    //////////////////////////////////////
    /**
     * Creates a CxeMessage object of the given message type.
     * 
     * @param p_messageType
     *               a defined CxeMessageType value
     */
    public CxeMessage (CxeMessageType p_messageType)
    {
        m_messageType = p_messageType;
        m_eventFlowObject = null;
        m_messageData = null;
        m_parameters = new HashMap();
        m_deleteMessageData = false;
    }

    //////////////////////////////////////
    // Public Methods                   //
    //////////////////////////////////////

    /**
     * Returns the CxeMessageType object
     * 
     * @return message type
     */
    public CxeMessageType getMessageType()
    {
        return m_messageType;
    }


    /**
     * Returns the hashmap of additional parameters
     * associated with this CxeMessage
     * 
     * @return parameters
     */
    public HashMap getParameters()
    {
        return m_parameters;
    }

    /**
     * Sets the hashmap of additional parameters
     * associated with this CxeMessage
     */
    public void setParameters(HashMap p_parameters)
    {
        m_parameters = p_parameters;
    }

    /**
     * Gets the MessageData object associated with this
     * CxeMessage object
     * 
     * @return MessageData
     */
    public MessageData getMessageData()
    {
        return m_messageData;
    }



    /**
     * Deletes the current MessageData object and resets
     * the MessageData object associated with this CxeMessage
     * object to be the new one passed in.
     * 
     * @param p_messageData
     *               a MessageData object. This must not be the same object
     *               as what getMessageData() currently returns otherwise
     *               the CxeMessage's MessageData object may actually be a deleted
     *               MessageData
     */
    public void setMessageData (MessageData p_messageData) throws IOException
    {
        if (m_messageData != null)
            m_messageData.delete();

        m_messageData = p_messageData;
    }


    /**
     * Gets the EventFlowXml
     * 
     * @return String
     */
    public String getEventFlowXml()
    {
        return XmlUtil.object2String(m_eventFlowObject);
    }

    /**
     * Sets the EventFlowXml
     * 
     * @param p_eventFlowXml
     *               new EventFlowXml
     */
    public void setEventFlowXml(String p_eventFlowXml)
    {
        m_eventFlowObject = XmlUtil.string2Object(EventFlowXml.class, p_eventFlowXml);
    }



    /**
     * Sets whether this CxeMessage should delete is corresponding
     * message data during a call to free()
     * 
     * @param p_deleteMessageData
     *               true | false
     */
    public void setDeleteMessageData(boolean p_deleteMessageData)
    {
        m_deleteMessageData = p_deleteMessageData;
    }



    /**
     * Frees any resources, connections, deletes corresponding message data files
     * if necessary.
     * @exception IOException
     */
    public void free() throws IOException
    {
        if (m_deleteMessageData && m_messageData != null)
                m_messageData.delete();
    }

    /**
     * @return the m_eventFlowObject
     */
    public EventFlowXml getEventFlowObject()
    {
        return m_eventFlowObject;
    }

    /**
     * @param m_eventFlowObject the m_eventFlowObject to set
     */
    public void setEventFlowObject(EventFlowXml m_eventFlowObject)
    {
        this.m_eventFlowObject = m_eventFlowObject;
    }
}

