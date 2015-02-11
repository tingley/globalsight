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

// DOM3 import org.w3c.dom.UserDataHandler;

/**
 * DOMNodeImpl.
 * 
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a
 *         href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a>
 *         (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMNodeImpl implements org.w3c.dom.Node
{

    /**
     * Wrapped tidy node.
     */
    protected Node adaptee;

    /**
     * Intantiates a new DOM node.
     * 
     * @param adaptee
     *            wrapped Tidy node
     */
    protected DOMNodeImpl(Node adaptee)
    {
        this.adaptee = adaptee;
    }

    /**
     * @see org.w3c.dom.Node#getNodeValue
     */
    public String getNodeValue()
    {
        String value = ""; // BAK 10/10/2000 replaced null

        if (adaptee.type == Node.TEXT_NODE || adaptee.type == Node.CDATA_TAG
                || adaptee.type == Node.COMMENT_TAG
                || adaptee.type == Node.PROC_INS_TAG)
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
     * @see org.w3c.dom.Node#setNodeValue
     */
    public void setNodeValue(String nodeValue)
    {
        if (adaptee.type == Node.TEXT_NODE || adaptee.type == Node.CDATA_TAG
                || adaptee.type == Node.COMMENT_TAG
                || adaptee.type == Node.PROC_INS_TAG)
        {
            byte[] textarray = TidyUtils.getBytes(nodeValue);
            adaptee.textarray = textarray;
            adaptee.start = 0;
            adaptee.end = textarray.length;
        }
    }

    /**
     * @see org.w3c.dom.Node#getNodeName
     */
    public String getNodeName()
    {
        return adaptee.element;
    }

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    public short getNodeType()
    {
        short result = -1;
        switch (adaptee.type)
        {
            case Node.ROOT_NODE:
                result = org.w3c.dom.Node.DOCUMENT_NODE;
                break;
            case Node.DOCTYPE_TAG:
                result = org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
                break;
            case Node.COMMENT_TAG:
                result = org.w3c.dom.Node.COMMENT_NODE;
                break;
            case Node.PROC_INS_TAG:
                result = org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE;
                break;
            case Node.TEXT_NODE:
                result = org.w3c.dom.Node.TEXT_NODE;
                break;
            case Node.CDATA_TAG:
                result = org.w3c.dom.Node.CDATA_SECTION_NODE;
                break;
            case Node.START_TAG:
            case Node.START_END_TAG:
                result = org.w3c.dom.Node.ELEMENT_NODE;
                break;
        }
        return result;
    }

    /**
     * @see org.w3c.dom.Node#getParentNode
     */
    public org.w3c.dom.Node getParentNode()
    {
        // Attributes are not children in the DOM, and do not have parents
        if (adaptee.parent != null)
        {
            return adaptee.parent.getAdapter();
        }
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getChildNodes
     */
    public org.w3c.dom.NodeList getChildNodes()
    {
        return new DOMNodeListImpl(adaptee);
    }

    /**
     * @see org.w3c.dom.Node#getFirstChild
     */
    public org.w3c.dom.Node getFirstChild()
    {
        if (adaptee.content != null)
        {
            return adaptee.content.getAdapter();
        }
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getLastChild
     */
    public org.w3c.dom.Node getLastChild()
    {
        if (adaptee.last != null)
        {
            return adaptee.last.getAdapter();
        }
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getPreviousSibling
     */
    public org.w3c.dom.Node getPreviousSibling()
    {
        if (adaptee.prev != null)
        {
            return adaptee.prev.getAdapter();
        }
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getNextSibling
     */
    public org.w3c.dom.Node getNextSibling()
    {
        if (adaptee.next != null)
        {
            return adaptee.next.getAdapter();
        }
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getAttributes
     */
    public org.w3c.dom.NamedNodeMap getAttributes()
    {
        return new DOMAttrMapImpl(adaptee.attributes);
    }

    /**
     * @see org.w3c.dom.Node#getOwnerDocument
     */
    public org.w3c.dom.Document getOwnerDocument()
    {
        Node node = this.adaptee;
        if (node != null && node.type == Node.ROOT_NODE)
        {
            return null;
        }

        while (node != null && node.type != Node.ROOT_NODE)
        {
            node = node.parent;
        }

        if (node != null)
        {
            return (org.w3c.dom.Document) node.getAdapter();
        }
        return null;
    }

    /**
     * @see org.w3c.dom.Node#insertBefore
     */
    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild,
            org.w3c.dom.Node refChild)
    {
        // TODO - handle newChild already in tree

        if (newChild == null)
        {
            return null;
        }
        if (!(newChild instanceof DOMNodeImpl))
        {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl) newChild;

        if (this.adaptee.type == Node.ROOT_NODE)
        {
            if (newCh.adaptee.type != Node.DOCTYPE_TAG
                    && newCh.adaptee.type != Node.PROC_INS_TAG)
            {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        else if (this.adaptee.type == Node.START_TAG)
        {
            if (newCh.adaptee.type != Node.START_TAG
                    && newCh.adaptee.type != Node.START_END_TAG
                    && newCh.adaptee.type != Node.COMMENT_TAG
                    && newCh.adaptee.type != Node.TEXT_NODE
                    && newCh.adaptee.type != Node.CDATA_TAG)
            {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        if (refChild == null)
        {
            this.adaptee.insertNodeAtEnd(newCh.adaptee);
            if (this.adaptee.type == Node.START_END_TAG)
            {
                this.adaptee.setType(Node.START_TAG);
            }
        }
        else
        {
            Node ref = this.adaptee.content;
            while (ref != null)
            {
                if (ref.getAdapter() == refChild)
                {
                    break;
                }
                ref = ref.next;
            }
            if (ref == null)
            {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                        "refChild not found");
            }
            Node.insertNodeBeforeElement(ref, newCh.adaptee);
        }

        return newChild;
    }

    /**
     * @see org.w3c.dom.Node#replaceChild
     */
    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild,
            org.w3c.dom.Node oldChild)
    {
        // TODO - handle newChild already in tree

        if (newChild == null)
        {
            return null;
        }
        if (!(newChild instanceof DOMNodeImpl))
        {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl) newChild;

        if (this.adaptee.type == Node.ROOT_NODE)
        {
            if (newCh.adaptee.type != Node.DOCTYPE_TAG
                    && newCh.adaptee.type != Node.PROC_INS_TAG)
            {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        else if (this.adaptee.type == Node.START_TAG)
        {
            if (newCh.adaptee.type != Node.START_TAG
                    && newCh.adaptee.type != Node.START_END_TAG
                    && newCh.adaptee.type != Node.COMMENT_TAG
                    && newCh.adaptee.type != Node.TEXT_NODE
                    && newCh.adaptee.type != Node.CDATA_TAG)
            {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        if (oldChild == null)
        {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    "oldChild not found");
        }

        Node n;
        Node ref = this.adaptee.content;
        while (ref != null)
        {
            if (ref.getAdapter() == oldChild)
            {
                break;
            }
            ref = ref.next;
        }
        if (ref == null)
        {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    "oldChild not found");
        }
        newCh.adaptee.next = ref.next;
        newCh.adaptee.prev = ref.prev;
        newCh.adaptee.last = ref.last;
        newCh.adaptee.parent = ref.parent;
        newCh.adaptee.content = ref.content;
        if (ref.parent != null)
        {
            if (ref.parent.content == ref)
            {
                ref.parent.content = newCh.adaptee;
            }
            if (ref.parent.last == ref)
            {
                ref.parent.last = newCh.adaptee;
            }
        }
        if (ref.prev != null)
        {
            ref.prev.next = newCh.adaptee;
        }
        if (ref.next != null)
        {
            ref.next.prev = newCh.adaptee;
        }
        for (n = ref.content; n != null; n = n.next)
        {
            if (n.parent == ref)
            {
                n.parent = newCh.adaptee;
            }
        }

        return oldChild;
    }

    /**
     * @see org.w3c.dom.Node#removeChild
     */
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild)
    {
        if (oldChild == null)
        {
            return null;
        }

        Node ref = this.adaptee.content;
        while (ref != null)
        {
            if (ref.getAdapter() == oldChild)
            {
                break;
            }
            ref = ref.next;
        }
        if (ref == null)
        {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    "refChild not found");
        }
        Node.discardElement(ref);

        if (this.adaptee.content == null && this.adaptee.type == Node.START_TAG)
        {
            this.adaptee.setType(Node.START_END_TAG);
        }

        return oldChild;
    }

    /**
     * @see org.w3c.dom.Node#appendChild
     */
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild)
    {
        // TODO - handle newChild already in tree

        if (newChild == null)
        {
            return null;
        }
        if (!(newChild instanceof DOMNodeImpl))
        {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl) newChild;

        if (this.adaptee.type == Node.ROOT_NODE)
        {
            if (newCh.adaptee.type != Node.DOCTYPE_TAG
                    && newCh.adaptee.type != Node.PROC_INS_TAG)
            {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        else if (this.adaptee.type == Node.START_TAG)
        {
            if (newCh.adaptee.type != Node.START_TAG
                    && newCh.adaptee.type != Node.START_END_TAG
                    && newCh.adaptee.type != Node.COMMENT_TAG
                    && newCh.adaptee.type != Node.TEXT_NODE
                    && newCh.adaptee.type != Node.CDATA_TAG)
            {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        this.adaptee.insertNodeAtEnd(newCh.adaptee);

        if (this.adaptee.type == Node.START_END_TAG)
        {
            this.adaptee.setType(Node.START_TAG);
        }

        return newChild;
    }

    /**
     * @see org.w3c.dom.Node#hasChildNodes
     */
    public boolean hasChildNodes()
    {
        return (adaptee.content != null);
    }

    /**
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    public org.w3c.dom.Node cloneNode(boolean deep)
    {
        Node node = adaptee.cloneNode(deep);
        node.parent = null;
        return node.getAdapter();
    }

    /**
     * Do nothing: text nodes in html documents are important and jtidy already
     * removes useless text during parsing.
     * 
     * @see org.w3c.dom.Node#normalize()
     */
    public void normalize()
    {
        // do nothing
    }

    /**
     * DOM2 - not implemented.
     * 
     * @see #isSupported(java.lang.String, java.lang.String)
     */
    public boolean supports(String feature, String version)
    {
        return isSupported(feature, version);
    }

    /**
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI()
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix()
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Node#setPrefix(java.lang.String)
     */
    public void setPrefix(String prefix) throws DOMException
    {
        // The namespace prefix of this node, or null if it is
        // unspecified. When it is defined to be null, setting it has
        // no effect, including if the node is read-only.

        // do nothing
    }

    /**
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName()
    {
        return getNodeName();
    }

    /**
     * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
     */
    public boolean isSupported(String feature, String version)
    {
        return false;
    }

    /**
     * @see org.w3c.dom.Node#hasAttributes
     */
    public boolean hasAttributes()
    {
        // contributed by dlp@users.sourceforge.net
        return this.adaptee.attributes != null;
    }

    /**
     * @todo DOM level 3 compareDocumentPosition() Not implemented.
     * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
     */
    public short compareDocumentPosition(org.w3c.dom.Node other)
            throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                "DOM method not supported");
    }

    /**
     * @todo DOM level 3 getBaseURI() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getBaseURI()
     */
    public String getBaseURI()
    {
        return null;
    }

    /**
     * @todo DOM level 3 getFeature() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
     */
    public Object getFeature(String feature, String version)
    {
        return null;
    }

    /**
     * @todo DOM level 3 getTextContent() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getTextContent()
     */
    public String getTextContent() throws DOMException
    {
        return null;
    }

    /**
     * @todo DOM level 3 getUserData() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getUserData(java.lang.String)
     */
    public Object getUserData(String key)
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
     */
    public boolean isDefaultNamespace(String namespaceURI)
    {
        return false;
    }

    /**
     * @todo DOM level 3 isEqualNode() Not implemented. Returns false.
     * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
     */
    public boolean isEqualNode(org.w3c.dom.Node arg)
    {
        return false;
    }

    /**
     * @todo DOM level 3 isSameNode() Not implemented. Returns false.
     * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
     */
    public boolean isSameNode(org.w3c.dom.Node other)
    {
        return false;
    }

    /**
     * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
     */
    public String lookupNamespaceURI(String prefix)
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
     */
    public String lookupPrefix(String namespaceURI)
    {
        return null;
    }

    /**
     * @todo DOM level 3 setTextContent() Not implemented. Throws
     *       NO_MODIFICATION_ALLOWED_ERR
     * @see org.w3c.dom.Node#setTextContent(java.lang.String)
     */
    public void setTextContent(String textContent) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "Node is read only");
    }

    /**
     * @todo DOM level 3 setUserData() Not implemented. Returns null.
     * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object,
     *      org.w3c.dom.UserDataHandler)
     */
    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        return null;
    }
}
