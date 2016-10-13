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
 * <p>An abstract class to encode/decode character entities or numeric
 * character references.</p>
 *
 * <p>This class is designed for TW applet.</P>
 */
public abstract class BasicEntities
{

    protected char[] mEncodeChar;
    protected Hashtable mCharToEntity;
    protected Hashtable mEntityToChar;

    /**
     * Decode only a basic character entity into a character
     * @param ent a character entity
     * @return decoded character. If <tt>ent</tt> is malformed or
     * cannot be found in the character entity list, the method
     * returns U+FFFF, non-existing code in Unicode.
     */
    public char decode(String ent)
    {
        Character c = (Character)mEntityToChar.get(ent);

        if (c == null)
        {
            return '\uffff';
        }
        else
        {
            return c.charValue();
        }
    }

    /**
     * Decode only basic entities in the given string. No numeric
     * character references are decoded.
     * @param s a string to be decoded
     * @return decoded string
     */
    public String decodeString(String s)
    {
        int len = s.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);

            if (s.charAt(i) == '&')
            {
                int j;
                for (j = i + 1; j < len; j++)
                {
                    if (s.charAt(j) == ';')
                    {
                        c = decode(s.substring(i, j + 1));

                        if (c == '\uffff')
                        {
                            result.append(s.substring(i, j + 1));
                        }
                        else
                        {
                            result.append(c);
                        }

                        break;
                    }
                }

                if (j >= len)
                {
                    result.append(s.substring(i));
                }

                i = j;
            }
            else
            {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Encode a character to a basic character entity.
     * @param c a character to be encoded
     * @return the result of encode.
     */
    public String encode(char c)
    {
        String s = (String)mCharToEntity.get(new Character(c));

        if (s == null)
        {
            s = String.valueOf(c);
        }

        return s;
    }

    /**
     * Encode basic character entities in the given string.
     * @param s a string to be encoded
     * @return encoded string
     */
    public String encodeString(String s)
    {
        int len = s.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);
            String ent = null;

            for (int j = 0; j < mEncodeChar.length; j++)
            {
                if (c == mEncodeChar[j])
                {
                    ent = encode(c);
                    break;
                }
            }

            if (ent != null)
            {
                result.append(ent);
            }
            else
            {
                result.append(c);
            }
        }

        return result.toString();
    }
}
