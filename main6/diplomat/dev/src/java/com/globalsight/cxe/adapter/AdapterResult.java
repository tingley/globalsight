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
package com.globalsight.cxe.adapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.message.CxeMessage;

/**
 * An AdapterResult is a simple "struct" object that holds the result from calling an Adapter's
 * handleMessage() method. Currently this is just the CxeMessage, but this might grow to include
 * items associated with a CxeMessage.
 */
public class AdapterResult implements Serializable
{
    //////////////////////////////////////
    // Public Members                   //
    //////////////////////////////////////
    /**
     * The actual CxeMessage object that is the result
     * of an adapter's operation
     */
    public CxeMessage cxeMessage = null;
    
    private List<CxeMessage> cxeMessages = new ArrayList<CxeMessage>();

    //////////////////////////////////////
    // Constructors                     //
    //////////////////////////////////////
    /**
     * Creates an empty AdapterResult
     */
    public AdapterResult ()
    {
    }

    /**
     * Creates an AdapterResult
     * 
     * @param p_nextAdapter
     *                  the logical name of the next adapter or recipient
     * @param p_msg the cxe message
     */
    public AdapterResult(CxeMessage p_cxeMessage)
    {
        cxeMessage = p_cxeMessage;
    }
    
    public void addMsg(CxeMessage p_cxeMessage)
    {
    	cxeMessages.add(p_cxeMessage);
    }
    
    public void addAllMsg(List<CxeMessage> p_cxeMessage)
    {
    	cxeMessages.addAll(p_cxeMessage);
    }
    
    public List<CxeMessage> getMsgs()
    {
    	return cxeMessages;
    }
}

