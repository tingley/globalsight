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


/**
 * Tidy implementation of org.w3c.dom.NamedNodeMap.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMAttrMapImpl implements org.w3c.dom.NamedNodeMap
{

    /**
     * wrapped com.globalsight.ling.jtidy.AttVal.
     */
    private AttVal first;

    /**
     * instantiates a new DOMAttrMapImpl for the given AttVal.
     * @param firstAttVal wrapped AttVal
     */
    protected DOMAttrMapImpl(AttVal firstAttVal)
    {
        this.first = firstAttVal;
    }

    /**
     * @see org.w3c.dom.NamedNodeMap#getNamedItem(java.lang.String)
     */
    public org.w3c.dom.Node getNamedItem(String name)
    {
        AttVal att = this.first;
        while (att != null)
        {
            if (att.attribute.equals(name))
            {
                break;
            }
            att = att.next;
        }
        if (att != null)
        {
            return att.getAdapter();
        }

        return null;
    }

    /**
     * @see org.w3c.dom.NamedNodeMap#item
     */
    public org.w3c.dom.Node item(int index)
    {
        int i = 0;
        AttVal att = this.first;
        while (att != null)
        {
            if (i >= index)
            {
                break;
            }
            i++;
            att = att.next;
        }
        if (att != null)
        {
            return att.getAdapter();
        }

        return null;
    }

    /**
     * @see org.w3c.dom.NamedNodeMap#getLength
     */
    public int getLength()
    {
        int len = 0;
        AttVal att = this.first;
        while (att != null)
        {
            len++;
            att = att.next;
        }
        return len;
    }

    /**
     * @todo DOM level 2 setNamedItem() Not implemented. Throws NOT_SUPPORTED_ERR.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     */
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node arg) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "DOM method not supported");
    }

    /**
     * @see org.w3c.dom.NamedNodeMap#removeNamedItem
     */
    public org.w3c.dom.Node removeNamedItem(String name) throws DOMException
    {
        AttVal att = this.first;
        AttVal previous = null;

        while (att != null)
        {
            if (att.attribute.equals(name))
            {
                if (previous == null)
                {
                    this.first = att.getNext();
                }
                else
                {
                    previous.setNext(att.getNext());
                }

                break;
            }
            previous = att;
            att = att.next;
        }

        if (att != null)
        {
            return att.getAdapter();
        }

        throw new DOMException(DOMException.NOT_FOUND_ERR, "Named item " + name + "Not found");
    }

    /**
     * Not supported, returns <code>DOMException.NOT_SUPPORTED_ERR</code>.
     * @see org.w3c.dom.NamedNodeMap#getNamedItemNS(java.lang.String, java.lang.String)
     */
    public org.w3c.dom.Node getNamedItemNS(String namespaceURI, String localName)
    {
        // NOT_SUPPORTED_ERR: May be raised if the implementation does not support the feature "XML" and the language
        // exposed through the Document does not support XML Namespaces (such as HTML 4.01).
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "DOM method not supported");
    }

    /**
     * Not supported, returns <code>DOMException.NOT_SUPPORTED_ERR</code>.
     * @see org.w3c.dom.NamedNodeMap#setNamedItemNS(org.w3c.dom.Node)
     */
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node arg) throws org.w3c.dom.DOMException
    {
        // NOT_SUPPORTED_ERR: May be raised if the implementation does not support the feature "XML" and the language
        // exposed through the Document does not support XML Namespaces (such as HTML 4.01).
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "DOM method not supported");
    }

    /**
     * Not supported, returns <code>DOMException.NOT_SUPPORTED_ERR</code>.
     * @see org.w3c.dom.NamedNodeMap#removeNamedItemNS(java.lang.String, java.lang.String)
     */
    public org.w3c.dom.Node removeNamedItemNS(String namespaceURI, String localName) throws org.w3c.dom.DOMException
    {
        // NOT_SUPPORTED_ERR: May be raised if the implementation does not support the feature "XML" and the language
        // exposed through the Document does not support XML Namespaces (such as HTML 4.01).
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "DOM method not supported");
    }

}
