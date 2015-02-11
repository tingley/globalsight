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
package com.globalsight.ling.common;

import java.util.Hashtable;

/**
 * A class to encode/decode XML character entities or
 * numeric character references.
 * numeric character references take two forms as below.<br><br>
 * <tt>'&amp;#' [0-9]+ ';'</tt>  # decimal representation<br>
 * <tt>'&amp;#x' [0-9a-fA-F]+ ';'</tt>  # hex representation<br>
 */
public class XmlEntities
    extends Entities
{
    /**
     * A list of characters that are always encoded to character
     * entities by <tt>encodeString()</tt> method. The characters
     * contained in the list are: <ul><li>&lt; <li>&gt; <li>&amp;
     * <li>&apos; <li>&quot;</ul>.
     */
    protected static final char[] mDefaultXmlEncodeChar =
        {'<', '>', '&', '\'', '"'};
    private boolean m_useDefaultXmlEncoderChar = true;

    // These objects must be Hashtables since this class gets loaded
    // by the Online Editor Applet.
    private static final Hashtable
        mDefaultCharToEntity = mapXmlDefaultCharToEntity();
    private static final Hashtable
        mDefaultEntityToChar = mapXmlDefaultEntityToChar();


    /**
     * Constructor
     */
    public XmlEntities()
    {
        super();
    }


    /**
     * Decode a XML character entity or a numeric character reference
     * into a character
     * @param ent a XML predefined five character entity or a XML/HTML
     * numeric character reference
     * @return decoded character. If <tt>ent</tt> is mal formed or
     * cannot be found in the character entity list, the method
     * returns U+FFFF, non-existing code in Unicode.
     */
    final public char decode(String ent)
    {
        Character c = (Character)mDefaultEntityToChar.get(ent);
        if (c == null)
        {
            if (ent.length() > 3) // numeric ref must be more than 3 chars
            {
                c = decodeNumRef(ent, "&#x", ";", 16, true);
                if (c == null)
                {
                    c = decodeNumRef(ent, "&#", ";", 10, true);
                    if (c == null)
                    {
                        c = new Character('\uffff');
                    }
                }

            }
            else
            {
                c = new Character('\uffff');
            }
        }

        return c.charValue();
    }


    /**
     * Decode only a basic character entity into a character
     * @param ent a character entity or a numeric character reference
     * @return decoded character. If <tt>ent</tt> is mal formed or
     * cannot be found in the character entity list, the method
     * returns U+FFFF, non-existing code in Unicode.
     */
    final public char decodeBasicEntity(String ent)
    {
        char c = '\uffff';
        Character ch = (Character)mDefaultEntityToChar.get(ent);

        if (ch != null)
        {
            c = ch.charValue();
        }

        return c;
    }


    /**
     * Decode characters in the given string. The character entities
     * included in <tt>decodeExcludeList</tt> will not be decoded. All
     * numeric character references will be decoded.
     * @param s a string to be decoded
     * @param decodeExcludeList a list of character entities that will
     * not be decoded
     * @return decoded string
     */
    final public String decodeString(String s, String[] decodeExcludeList)
    {
        if (s.indexOf('&') >= 0)
        {
            return decodeString(s, decodeExcludeList, "&[^;]+;", false);
        }

        return s;
    }


    /**
     * Decode only basic entities in the given string. No numeric
     * character regerences are decoded.
     * @param s a string to be decoded
     * @return decoded string
     */
    final public String decodeStringBasic(String s)
    {
        if (s.indexOf('&') >= 0)
        {
            return decodeString(s, null, "&[^;]+;", true);
        }

        return s;
    }


    /**
     * Encode a character to a XML numeric character reference or a
     * predefined XML cahracter entity. The fundamental five
     * characters (&lt;, &gt;, &amp;, &quot; and &#39;) are always
     * converted to the coresponding entities.
     * @param c a character to be encoded
     * @param entity not used
     * @return the result of encode. If it's a nemeirc character
     * reference, it takes hex form.
     */
    final public String encode(char c, boolean entity)
    {
        String s = (String)mDefaultCharToEntity.get(new Character(c));

        if (s == null)
        {
            s = encodeNumRef(c, "&#x", ";", 16);
        }

        return s;
    }


    /**
     * Encode characters in the given string. Only characters given in
     * <tt>encodeCarList</tt> will be encoded. Characters will be
     * encoded as numeric character references.  The characters in the
     * default list, <tt>mDefaultXmlEncodeChar</tt>, will be always
     * encoded as a predifined character entiry.
     * @param s a string to be encoded
     * @param encodeCharList a list of characters which are encoded in
     * <tt>s</tt>. Even if <tt>encodeCharList</tt> is null, the
     * characters in the default list, <tt>mDefaultXmlEncodeChar</tt>,
     * will be encoded.
     * @return encoded string
     */
    final public String encodeString(String s, char[] encodeCharList)
    {
        return super.encodeString(s, encodeCharList);
    }


    /**
     * Encode characters in the given string. Only a range of
     * characters given by <tt>first</tt> and <tt>last</tt> will be
     * encoded. Characters will be encoded as numeric character
     * references unless the character is in the list of
     * <tt>mDefaultXmlEncodeChar</tt>. They are encoded to predefined
     * XML character entities.  The characters in the default list,
     * <tt>mDefaultXmlEncodeChar</tt>, will be always encoded even if
     * they are not in the range of <tt>first</tt> and <tt>last</tt>.
     * @param s a string to be encoded
     * @param first a first character to be included in the range of
     * characters to be encoded. If <tt>first</tt> is greater than
     * <tt>last</tt>, no any characters will be encoded.
     * @param last a last character to be included in the range of
     * characters to be encoded. If <tt>last</tt> is less than
     * <tt>first</tt>, no any characters will be encoded.
     * @return encoded string
     */
    final public String encodeString(String s, char first, char last)
    {
        return super.encodeString(s, first, last);
    }


    /**
     * Encode basic character entities in the given string.
     * @param s a string to be encoded
     * @return encoded string
     */
    final public String encodeStringBasic(String s)
    {
        return encodeString(s, (char[])null);
    }


    /**
     * Test if a character is in the range of conversion
     * @param c character to be tested.
     * @param charList list of character to be converted
     * @return <tt>true</tt> if the character is in the range,
     * otherwise <tt>false</tt>
     */
    protected boolean testRange(char c, char[] charList)
    {
        boolean b = false;

        if (m_useDefaultXmlEncoderChar)
        {
            for (int i = 0; i < mDefaultXmlEncodeChar.length; i++)
            {
                if (c == mDefaultXmlEncodeChar[i])
                {
                    b = true;
                    break;
                }
            }
        }

        if (!b && charList != null)
        {
            for (int i = 0; i < charList.length; i++)
            {
                if (c == charList[i])
                {
                    b = true;
                    break;
                }
            }
        }

        return b;
    }


    /**
     * Test if a character is in the range of conversion
     * @param c character to be tested.
     * @param first a first character to be included in the range of
     * characters to be encoded. If <tt>first</tt> is greater than
     * <tt>last</tt>, no any characters will be encoded.
     * @param last a last character to be included in the range of
     * characters to be encoded. If <tt>last</tt> is less than
     * <tt>first</tt>, no any characters will be encoded.
     * @return <tt>true</tt> if the character is in the range,
     * otherwise <tt>false</tt>
     */
    final protected boolean testRange(char c, char first, char last)
    {
        boolean b = false;

        if (m_useDefaultXmlEncoderChar)
        {
            for (int i = 0; i < mDefaultXmlEncodeChar.length; i++)
            {
                if (c == mDefaultXmlEncodeChar[i])
                {
                    b = true;
                    break;
                }
            }
        }

        if (!b && c >= first && c <= last)
        {
            b = true;
        }

        return b;
    }


    /**
     * Set this flag to false to not use the default XML chars
     * in the converters. Default is "true".
     * {'<', '>', '&', '\'', '"'};
     */
    final public void setUseDefaultXmlEncoderChar(boolean p_flag)
    {
        m_useDefaultXmlEncoderChar = p_flag;
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


    /*
       public static void main(String[] arg)
       {
       XmlEntities xml = new XmlEntities();

       System.out.println("Test encode(char c)");
       System.out.println(xml.encode('<', true));
       System.out.println(xml.encode('>', true));
       System.out.println(xml.encode('&', true));
       System.out.println(xml.encode('\'', true));
       System.out.println(xml.encode('"', true));
       System.out.println(xml.encode('\u3745', true));
       System.out.println();

       System.out.println("Test decode(String ent)");
       System.out.println(xml.decode("&lt;"));
       System.out.println(xml.decode("&gt;"));
       System.out.println(xml.decode("&amp;"));
       System.out.println(xml.decode("&apos;"));
       System.out.println(xml.decode("&quot;"));
       System.out.println(xml.decode("&QUOT;"));
       System.out.println(xml.decode("&#37;"));
       System.out.println(xml.decode("&#x25;"));
       System.out.println(xml.decode("&#g25;"));
       System.out.println();

       char[] ca = {'#', '\u00c0'};
       System.out.println("Test encodeString(String s, char[] encodeCharList)");
       System.out.println(xml.encodeString("a\u000b#b<c\u00c0\u00c1", ca));
       System.out.println(xml.encodeStringBasic("a#b<c\u00c0\u00c1"));
       System.out.println();

       System.out.println("Test encodeString(String s, char first, char last)");
       System.out.println(xml.encodeString("azyb<c\u00c0\u00c1", 'z', '\u00c0'));
       System.out.println();

       String[] list = {"&lt;", "&#22;"} ;
       System.out.println("Test decodeString(String s, String[] decodeExcludeList)");
       System.out.println(xml.decodeString("a&lt;&gt;bc&#35;d&#22;&LT;", list));
       System.out.println(xml.decodeStringBasic("a&lt;&gt;bc&#35;d&#22;&LT;"));
       System.out.println();
       }
    */
}
