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
import com.globalsight.cxe.message.CxeMessage;
import java.io.InputStream;
import java.io.IOException;

/**
 * The CxeProcessor interface is allowed to do pre or post processing
 * to Event Flow XML and content within CXE.
 */
public interface CxeProcessor
{
    /**
     * <P>Performs processing (pre or post) on the contained
     * eventFlowXml and content. Returns a CxeMessage
     * containing the possibly modified EventFlowXml and possibly
     * new MessageData. The CxeMessage parameters may also be modified.
     *
     * <P>NOTE: Please ensure that this does not throw any exceptions.
     * It is your responsibility to handle whatever goes wrong during
     * your processing.
     * 
     * @param p_cxeMessage
     *               cxemessage
     * @return modified cxemessage
     */
    public CxeMessage process (CxeMessage p_cxeMessage);
}

