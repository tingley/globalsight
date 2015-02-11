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
package com.globalsight.ling.docproc.extractor.po;

import java.util.Hashtable;

/**
 * <p>
 * Defines a Token (and various token IDs) which are returned from the parsers
 * getNextToken() method.
 * </p>
 */
public class POToken
{

    /**
     * ID for an empty token.
     */
    public static final int EMPTY = 0;

    /**
     * ID for an unkown token.
     */
    public static final int UNKNOWN = 1;

    /**
     * ID for a carriage return token.
     */
    public static final int LINEBREAK = 2;

    /**
     * ID for a line feed token.
     */
    public static final int LF = 3;

    /**
     * ID for a form feed token.
     */
    public static final int FF = 4;

    /**
     * ID for a tab token.
     */
    public static final int TAB = 5;

    /**
     * ID for a non-breaking space.
     */
    public static final int NBSPACE = 6;

    /**
     * ID for a spcace token.
     */
    public static final int SPACE = 7;

    /**
     * ID for a text token.
     */
    public static final int TEXT = 8;

    /**
     * ID for an end-of-file token.
     */
    public static final int EOF = 9;

    /**
     * Holds a token type ID.
     */
    public int m_nType = EMPTY;

    /**
     * Holds the content (or value) of the token.
     */
    public String m_strContent = "";

    /**
     * Holds the line number the token was found on.
     */
    public int m_nLineNumber = 0;

    /**
     * <p>
     * The position of the token within the segment. The parser does not fill
     * this in. Once the extractor is holding a complete segment, an extractor
     * method fills it in.
     * </p>
     */
    public int m_nPos;

    /**
     * Translated-string start with msgstr in PO File.
     */
    public static final String LINEBREAK_STR = "\n";
    public static final String COMMENT = "#";
    public static final String MSGCTXT = "msgctxt";
    public static final String MSGSID = "msgid";
    public static final String MSGSID_PLURAL = "msgid_plural";

    public static final String MSGSTR = "msgstr";
    public static final String MSGSTR_PLURAL = "msgstr[";
    public static final String MSGID_EMPTY = "msgid \"\"";
    public static final String QuotationMark = "\"";

    /**
     * <p>
     * Mapping of characters to tokens.
     * </p>
     */
    public Hashtable m_mapCharToType = mapCharToType();

    /**
     * Constructor.
     */
    public POToken()
    {
        super();
    }

    /**
     * Contructor.
     * @param p_nType int - Token ID.
     * @param p_sContent - Token content.
     */
    public POToken(int p_nType, String p_sContent)
    {
        m_nType = p_nType;
        m_strContent = p_sContent;
    }

    /**
     * Maps characters to Token types.
     * 
     * @return Hashtable
     */
    private Hashtable mapCharToType()
    {
        Hashtable h = new Hashtable();
        h.put(new Character('\u000c'), new Integer(POToken.FF));
        h.put(new Character('\n'), new Integer(POToken.LINEBREAK));
        h.put(new Character('\r'), new Integer(POToken.LF));
        h.put(new Character('\t'), new Integer(POToken.TAB));
        h.put(new Character('\u00a0'), new Integer(POToken.NBSPACE));
        return h;
    }
}
