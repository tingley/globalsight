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
package com.globalsight.ling.tw;

import java.util.*;

/**
 * A class to encode/decode XML character entities
 */
public class XmlEntities
    extends BasicEntities
{
    /**
     * a list of characters that are always encoded to character
     * entities by <tt>encodeString()</tt> method. The characters
     * contained in the list are: <ul><li>&lt; <li>&gt; <li>&amp;
     * <li>&apos; <li>&quot; </ul>
     */

    protected static final char[] mDefaultXmlEncodeChar =
        { '<', '>', '&', '\'', '"' };
    protected static final Hashtable mXmlCharToEntity =
        mapXmlDefaultCharToEntity();
    protected static final Hashtable mXmlEntityToChar =
        mapXmlDefaultEntityToChar();

    /**
     * Constructor
     */
    public XmlEntities()
    {
        super();

        mEncodeChar   = mDefaultXmlEncodeChar;
        mCharToEntity = mXmlCharToEntity;
        mEntityToChar = mXmlEntityToChar;
    }

    private static Hashtable mapXmlDefaultCharToEntity()
    {
        Hashtable h = new Hashtable();

        h.put(new Character('<'), "&lt;");
        h.put(new Character('>'), "&gt;");
        h.put(new Character('&'), "&amp;");
        h.put(new Character('\''), "&apos;");
        h.put(new Character('"'), "&quot;");

        return h;
    }

    private static Hashtable mapXmlDefaultEntityToChar()
    {
        Hashtable h = new Hashtable();

        h.put("&lt;", new Character('<'));
        h.put("&gt;", new Character('>'));
        h.put("&amp;", new Character('&'));
        h.put("&apos;", new Character('\''));
        h.put("&quot;", new Character('"'));

        return h;
    }

    /* Test code
    public static void main(String[] arg)
    {
        XmlEntities xml = new XmlEntities();

        System.out.println("Test encode(char c)");
        System.out.println(xml.encode('<'));
        System.out.println(xml.encode('>'));
        System.out.println(xml.encode('&'));
        System.out.println(xml.encode('\''));
        System.out.println(xml.encode('"'));
        System.out.println();

        System.out.println("Test decode(String ent)");
        System.out.println(xml.decode("&lt;"));
        System.out.println(xml.decode("&gt;"));
        System.out.println(xml.decode("&amp;"));
        System.out.println(xml.decode("&apos;"));
        System.out.println(xml.decode("&quot;"));
        System.out.println();

        System.out.println("Test encodeString(String s)");
        System.out.println(xml.encodeString("a#b<c\u00c0\u00c1"));
        System.out.println();

        System.out.println("Test decodeString(String s)");
        System.out.println(xml.decodeString("a&lt;&gt;bc&#35;d&#22;&amp;"));
        System.out.println();
    }
    */

}
