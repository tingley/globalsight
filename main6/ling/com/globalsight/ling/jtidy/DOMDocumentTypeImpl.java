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

import org.w3c.dom.UserDataHandler;

/**
 * DOMDocumentTypeImpl.
 * 
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a
 *         href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a>
 *         (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMDocumentTypeImpl extends DOMNodeImpl implements
        org.w3c.dom.DocumentType
{

    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        return null;
    }

    /**
     * Instantiates a new DOM document type.
     * 
     * @param adaptee
     *            Tidy Node
     */
    protected DOMDocumentTypeImpl(Node adaptee)
    {
        super(adaptee);
    }

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    public short getNodeType()
    {
        return org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
    }

    /**
     * @see org.w3c.dom.Node#getNodeName
     */
    public String getNodeName()
    {
        return getName();
    }

    /**
     * @see org.w3c.dom.DocumentType#getName
     */
    public String getName()
    {
        String value = null;
        if (adaptee.type == Node.DOCTYPE_TAG)
        {

            if (adaptee.textarray != null && adaptee.start < adaptee.end)
            {
                value = TidyUtils.getString(adaptee.textarray, adaptee.start,
                        adaptee.end - adaptee.start);
            }
        }
        return value;
    }

    /**
     * @todo DOM level 2 getEntities() Not implemented. Returns null.
     * @see org.w3c.dom.DocumentType#getEntities()
     */
    public org.w3c.dom.NamedNodeMap getEntities()
    {
        return null;
    }

    /**
     * @todo DOM level 2 getNotations() Not implemented. Returns null.
     * @see org.w3c.dom.DocumentType#getNotations()
     */
    public org.w3c.dom.NamedNodeMap getNotations()
    {
        return null;
    }

    /**
     * @todo DOM level 2 getPublicId() Not implemented. Returns null.
     * @see org.w3c.dom.DocumentType#getPublicId()
     */
    public String getPublicId()
    {
        return null;
    }

    /**
     * @todo DOM level 2 getSystemId() Not implemented. Returns null.
     * @see org.w3c.dom.DocumentType#getSystemId()
     */
    public String getSystemId()
    {
        return null;
    }

    /**
     * @todo DOM level 2 getInternalSubset() Not implemented. Returns null.
     * @see org.w3c.dom.DocumentType#getInternalSubset()
     */
    public String getInternalSubset()
    {
        return null;
    }

}
