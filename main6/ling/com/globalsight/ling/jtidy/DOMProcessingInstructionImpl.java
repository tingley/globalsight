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
package com.globalsight.ling.jtidy;

import org.w3c.dom.DOMException;
import org.w3c.dom.UserDataHandler;

/**
 * DOMProcessingInstructionImpl.
 * 
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a
 *         href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a>
 *         (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMProcessingInstructionImpl extends DOMNodeImpl implements
        org.w3c.dom.ProcessingInstruction
{

    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        return null;
    }

    /**
     * Instantiates a new DOM processing instruction.
     * 
     * @param adaptee
     *            wrapped Tidy node
     */
    protected DOMProcessingInstructionImpl(Node adaptee)
    {
        super(adaptee);
    }

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    public short getNodeType()
    {
        return org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE;
    }

    /**
     * @todo DOM level 2 getTarget() Not implemented. Returns null.
     * @see org.w3c.dom.ProcessingInstruction#getTarget
     */
    public String getTarget()
    {
        return null;
    }

    /**
     * @see org.w3c.dom.ProcessingInstruction#getData
     */
    public String getData()
    {
        return getNodeValue();
    }

    /**
     * @see org.w3c.dom.ProcessingInstruction#setData(java.lang.String)
     */
    public void setData(String data) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "Node is read only");
    }

}
