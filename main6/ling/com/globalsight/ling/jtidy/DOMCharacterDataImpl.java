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
 * Tidy implementation of org.w3c.dom.CharacterData.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMCharacterDataImpl extends DOMNodeImpl implements org.w3c.dom.CharacterData
{

    /**
     * Instantiates a new DOMCharacterDataImpl which wraps the given Node.
     * @param adaptee wrapped node.
     */
    protected DOMCharacterDataImpl(Node adaptee)
    {
        super(adaptee);
    }

    /**
     * @see org.w3c.dom.CharacterData#getData
     */
    public String getData() throws DOMException
    {
        return getNodeValue();
    }

    /**
     * @see org.w3c.dom.CharacterData#getLength
     */
    public int getLength()
    {
        int len = 0;
        if (adaptee.textarray != null && adaptee.start < adaptee.end)
        {
            len = adaptee.end - adaptee.start;
        }
        return len;
    }

    /**
     * @see org.w3c.dom.CharacterData#substringData
     */
    public String substringData(int offset, int count) throws DOMException
    {
        int len;
        String value = null;
        if (count < 0)
        {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "Invalid length");
        }
        if (adaptee.textarray != null && adaptee.start < adaptee.end)
        {
            if (adaptee.start + offset >= adaptee.end)
            {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "Invalid offset");
            }
            len = count;
            if (adaptee.start + offset + len - 1 >= adaptee.end)
            {
                len = adaptee.end - adaptee.start - offset;
            }

            value = TidyUtils.getString(adaptee.textarray, adaptee.start + offset, len);
        }
        return value;
    }

    /**
     * Not supported.
     * @see org.w3c.dom.CharacterData#setData
     */
    public void setData(String data) throws DOMException
    {
        // NOT SUPPORTED
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.CharacterData#appendData
     */
    public void appendData(String arg) throws DOMException
    {
        // NOT SUPPORTED
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.CharacterData#insertData
     */
    public void insertData(int offset, String arg) throws DOMException
    {
        // NOT SUPPORTED
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.CharacterData#deleteData
     */
    public void deleteData(int offset, int count) throws DOMException
    {
        // NOT SUPPORTED
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.CharacterData#replaceData
     */
    public void replaceData(int offset, int count, String arg) throws DOMException
    {
        // NOT SUPPORTED
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

}
