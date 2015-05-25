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

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * An abstract class to encode/decode character entities or numeric character
 * references.
 */
public abstract class Entities
{
    /**
     * Decode a character entity or a numeric character reference into a
     * character
     * 
     * @param ent
     *            a character entity or a numeric character reference
     * @return decoded character. If <tt>ent</tt> is mal formed or cannot be
     *         found in the character entity list, the method returns U+FFFF,
     *         non-existing code in Unicode.
     */
    public abstract char decode(String ent);

    /**
     * Decode only a basic character entity into a character
     * 
     * @param ent
     *            a character entity or a numeric character reference
     * @return decoded character. If <tt>ent</tt> is mal formed or cannot be
     *         found in the character entity list, the method returns U+FFFF,
     *         non-existing code in Unicode.
     */
    public abstract char decodeBasicEntity(String ent);

    /**
     * Decode a numeric character reference to a character
     * 
     * @param s
     *            numeric character reference to be encoded
     * @param prefix
     *            prefix string of the nemeric character reference
     * @param postfix
     *            postfix string of the nemeric character reference
     * @param radix
     *            radix for encoding (i.e. 16 for hex, 10 for decimal)
     * @param caseSensitive
     *            indicates the case sensitibity of the prefix and postfix
     *            string
     * @return decoded character. If <tt>s</tt> is not a valid char ref, the
     *         function returns <tt>null</tt>.
     */
    final protected Character decodeNumRef(String s, String prefix,
            String postfix, int radix, boolean caseSensitive)
    {
        Character c = null;
        boolean b;

        if (caseSensitive)
        {
            b = s.substring(0, prefix.length()).equals(prefix)
                    && s.substring(s.length() - postfix.length()).equals(
                            postfix);
        }
        else
        {
            b = s.substring(0, prefix.length()).equalsIgnoreCase(prefix)
                    && s.substring(s.length() - postfix.length())
                            .equalsIgnoreCase(postfix);
        }

        if (b)
        {
            try
            {
                c = new Character((char) Integer.parseInt(
                        s.substring(prefix.length(),
                                s.length() - postfix.length()), radix));
            }
            catch (NumberFormatException e)
            {
                c = null;
            }

        }

        return c;
    }

    /**
     * Decode characters in the given string. The character entities included in
     * <tt>decodeExcludeList</tt> will not be decoded. All numeric character
     * references will be decoded.
     * 
     * @param s
     *            a string to be decoded
     * @param decodeExcludeList
     *            a list of character entities that will not be decoded
     * @return decoded string
     */
    public abstract String decodeString(String s, String[] decodeExcludeList);

    /**
     * Decode characters in the given string. The character entities included in
     * <tt>decodeExcludeList</tt> will not be decoded. All numeric character
     * references will be decoded.
     * <p>
     * This function is intended to be used in <tt>public static String
     * decodeString(String s, String[] decodeExcludeList)</tt>
     * 
     * @param s
     *            a string to be decoded
     * @param decodeExcludeList
     *            a list of character entities that will not be decoded
     * @param patternString
     *            regular expression to search for entities in <tt>s</tt>
     * @param basic
     *            if <tt>basic</tt> is <tt>true</tt>, only basic character
     *            entities are decoded
     * @return decoded string
     */
    final protected String decodeString(String s, String[] decodeExcludeList,
            String patternString, boolean basic)
    {
        RE pattern = null;

        try
        {
            pattern = new RE(patternString, RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e)
        {
            // Should throw an error so CAP import knows.
            System.err.println("pilot error in regex");
        }

        int last_index = 0;
        StringBuffer ret = new StringBuffer();

        while (pattern.match(s, last_index))
        {
            ret.append(s.substring(last_index, pattern.getParenStart(0)));
            last_index = pattern.getParenEnd(0);

            String result = pattern.getParen(0);
            char c = '\uffff';

            if (decodeExcludeList != null)
            {
                int i;
                for (i = 0; i < decodeExcludeList.length; i++)
                {
                    if (decodeExcludeList[i].equals(result))
                    {
                        break;
                    }
                }
                if (i >= decodeExcludeList.length)
                {
                    c = basic ? decodeBasicEntity(result) : decode(result);
                }
            }
            else
            {
                c = basic ? decodeBasicEntity(result) : decode(result);
            }

            if (c == '\uffff')
            {
                ret.append(result);
            }
            else
            {
                ret.append(c);
            }
        }

        ret.append(s.substring(last_index));

        return ret.toString();
    }

    /**
     * Decode only basic entities in the given string. No numeric character
     * regerences are decoded.
     * 
     * @param s
     *            a string to be decoded
     * @return decoded string
     */
    public abstract String decodeStringBasic(String s);

    /**
     * Encode a character to a numeric character reference or a cahracter
     * entity.
     * 
     * @param c
     *            a character to be encoded
     * @param entity
     *            <tt>c</tt> will be encoded as a character entity if
     *            <tt>entiry</tt> is <tt>true</tt>, otherwise <tt>c</tt> will be
     *            a numeric character reference. If <tt>entity</tt> is true and
     *            the corresponding entity can not be found, the function
     *            returns a numeric character reference.
     * @return the result of encode.
     */
    public abstract String encode(char c, boolean entity);

    /**
     * Encode a character to a numeric character reference
     * 
     * @param c
     *            character to be encoded
     * @param prefix
     *            prefix string of the nemeric character reference
     * @param postfix
     *            postfix string of the nemeric character reference
     * @param radix
     *            radix for encoding (i.e. 16 for hex, 10 for decimal)
     * @return nemeric character reference
     */
    final protected String encodeNumRef(char c, String prefix, String postfix,
            int radix)
    {
        StringBuffer result = new StringBuffer();

        result.append(prefix);
        result.append(Integer.toString(c, radix));
        result.append(postfix);

        return result.toString();
    }

    /**
     * Encode characters in the given string. Only characters given in
     * <tt>encodeCarList</tt> will be encoded. Characters will be encoded as
     * numeric character references or character entities.
     * 
     * @param s
     *            a string to be encoded
     * @param encodeCharList
     *            a list of characters which will be encoded .
     * @return encoded string
     */
    public String encodeString(String s, char[] encodeCharList)
    {
        final int len = s.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);

            if (testRange(c, encodeCharList))
            {
                result.append(encode(c, true));
            }
            else
            {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Encode characters in the given string. Only a range of characters given
     * by <tt>first</tt> and <tt>last</tt> will be encoded. Characters will be
     * encoded as numeric character references or character entities.
     * 
     * @param s
     *            a string to be encoded
     * @param first
     *            a first character to be included in the range of characters to
     *            be encoded. If <tt>first</tt> is greater than <tt>last</tt>,
     *            no any characters will be encoded.
     * @param last
     *            a last character to be included in the range of characters to
     *            be encoded. If <tt>last</tt> is less than <tt>first</tt>, no
     *            any characters will be encoded.
     * @return encoded string
     */
    public String encodeString(String s, char first, char last)
    {
        final int len = s.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);

            if (testRange(c, first, last))
            {
                result.append(encode(c, true));
            }
            else
            {
                result.append(c);
            }

        }

        return result.toString();
    }

    /**
     * Encode characters in the given string acording to <tt>codeset</tt>. Only
     * the characters that cannot be converted into <tt>codeset</tt> will be
     * encoded. Characters will be encoded as numeric character references or
     * character entities.
     * 
     * @param s
     *            a string to be encoded
     * @param codeset
     *            codeset to be used to determine the characters that doesn't
     *            exist in it. <tt>codeset</tt> must be either IANA registered
     *            character set name or Java supported character encoding name.
     * @return encoded string
     */
    final public String encodeString(String s, String codeset) throws Exception
    {
        throw new Exception(
                "Entities.encodeString(String s, String codeset) is not yet implemented!");
    }

    /**
     * Encode basic character entities in the given string.
     * 
     * @param s
     *            a string to be encoded
     * @return encoded string
     */
    public abstract String encodeStringBasic(String s);

    /**
     * Test if a character is in the range of conversion
     * 
     * @param c
     *            character to be tested.
     * @param charList
     *            list of character to be converted
     * @return <tt>true</tt> if the character is in the range, otherwise
     *         <tt>false</tt>
     */
    protected abstract boolean testRange(char c, char[] charList);

    /**
     * Test if a character is in the range of conversion
     * 
     * @param c
     *            character to be tested.
     * @param first
     *            a first character to be included in the range of characters to
     *            be encoded. If <tt>first</tt> is greater than <tt>last</tt>,
     *            no any characters will be encoded.
     * @param last
     *            a last character to be included in the range of characters to
     *            be encoded. If <tt>last</tt> is less than <tt>first</tt>, no
     *            any characters will be encoded.
     * @return <tt>true</tt> if the character is in the range, otherwise
     *         <tt>false</tt>
     */
    protected abstract boolean testRange(char c, char first, char last);
}
