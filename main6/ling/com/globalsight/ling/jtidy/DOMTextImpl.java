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
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * DOMTextImpl.
 * 
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a
 *         href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a>
 *         (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMTextImpl extends DOMCharacterDataImpl implements
        org.w3c.dom.Text
{

    /**
     * Instantiates a new DOM text node.
     * 
     * @param adaptee
     *            wrapped Tidy node
     */
    protected DOMTextImpl(Node adaptee)
    {
        super(adaptee);
    }

    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getNodeName
     */
    public String getNodeName()
    {
        return "#text";
    }

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    public short getNodeType()
    {
        return org.w3c.dom.Node.TEXT_NODE;
    }

    /**
     * @todo DOM level 2 splitText() Not supported. Throws
     *       NO_MODIFICATION_ALLOWED_ERR.
     * @see org.w3c.dom.Text#splitText(int)
     */
    public org.w3c.dom.Text splitText(int offset) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "Not supported");
    }

    /**
     * @todo DOM level 3 getWholeText() Not implemented. Returns null.
     * @see org.w3c.dom.Text#getWholeText()
     */
    public String getWholeText()
    {
        return null;
    }

    /**
     * @todo DOM level 3 isElementContentWhitespace() Not implemented. Returns
     *       false.
     * @see org.w3c.dom.Text#isElementContentWhitespace()
     */
    public boolean isElementContentWhitespace()
    {
        return false;
    }

    /**
     * @todo DOM level 3 replaceWholeText() Not implemented. Returns the same
     *       node.
     * @see org.w3c.dom.Text#isElementContentWhitespace()
     */
    public Text replaceWholeText(String content) throws DOMException
    {
        return this;
    }
}
