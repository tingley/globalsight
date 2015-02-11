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

import java.util.Hashtable;

/**
 * A class to encode/decode HTML character entities
 */
public class HtmlEntities
    extends BasicEntities
{
    /**
     * A list of characters that are always encoded to character
     * entities by <tt>encodeString()</tt> method. The characters
     * contained in the list are: <ul><li>&lt; <li>&gt; <li>&amp;
     * <li>&quot; </ul>
     */
    protected static final char[] mDefaultHtmlEncodeChar =
        { '<', '>', '&', '"' };

    protected static final Hashtable mHtmlCharToEntity =
        mapHtmlDefaultCharToEntity();

    protected static final Hashtable mHtmlEntityToChar =
        mapHtmlDefaultEntityToChar();

    /**
     * Constructor
     */
    public HtmlEntities()
    {
        super();

        mEncodeChar = mDefaultHtmlEncodeChar;
        mCharToEntity = mHtmlCharToEntity;
        mEntityToChar = mHtmlEntityToChar;
    }

    private static Hashtable mapHtmlDefaultCharToEntity()
    {
        Hashtable h = new Hashtable();
        h.put(new Character('<'), "&lt;");
        h.put(new Character('>'), "&gt;");
        h.put(new Character('&'), "&amp;");
        h.put(new Character('"'), "&quot;");
        return h;
    }

    private static Hashtable mapHtmlDefaultEntityToChar()
    {
        Hashtable h = new Hashtable();
        h.put("&lt;", new Character('<'));
        h.put("&gt;", new Character('>'));
        h.put("&amp;", new Character('&'));
        h.put("&quot;", new Character('"'));
        return h;
    }

    /* Test code
    public static void main(String[] arg)
    {
        HtmlEntities Html = new HtmlEntities();

        System.out.println("Test encode(char c)");
        System.out.println(Html.encode('<'));
        System.out.println(Html.encode('>'));
        System.out.println(Html.encode('&'));
        System.out.println(Html.encode('"'));
        System.out.println();

        System.out.println("Test decode(String ent)");
        System.out.println(Html.decode("&lt;"));
        System.out.println(Html.decode("&gt;"));
        System.out.println(Html.decode("&amp;"));
        System.out.println(Html.decode("&quot;"));
        System.out.println();

        System.out.println("Test encodeString(String s)");
        System.out.println(Html.encodeString("<>&\"a#b<c\u00c0\u00c1"));
        System.out.println();

        System.out.println("Test decodeString(String s)");
        System.out.println(Html.decodeString("a&lt;&gt;bc&#35;d&#22;&amp;"));
        System.out.println();
    }*/

}
