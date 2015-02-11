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
package com.globalsight.ling.docproc.extractor.plaintext;

import java.util.Hashtable;

/**
 * <p>Defines a Token (and various token IDs) which are returned from
 * the parsers getNextToken() method.</p>
 */
public class PTToken
{

    /**
     * ID for an empty token.
     */
    public static final int EMPTY                   = 0;

    /**
     * ID for an unkown token.
     */
    public static final int UNKNOWN                 = 1;

    /**
     * ID for a carriage return token.
     */
    public static final int LINEBREAK               = 2;

    /**
     * ID for a line feed token.
     */
    public static final int LF                      = 3;

    /**
     * ID for a form feed token.
     */
    public static final int FF                      = 4;

    /**
     * ID for a tab token.
     */
    public static final int TAB                     = 5;

    /**
     * ID for a non-breaking space.
     */
    public static final int NBSPACE                 = 6;

    /**
     * ID for a spcace token.
     */
    public static final int SPACE                   = 7;

    /**
     * ID for a text token.
     */
    public static final int TEXT                    = 8;

    /**
     * ID for an end-of-file token.
     */
    public static final int EOF                     = 9;

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
     * <p>The position of the token within the segment. The parser
     * does not fill this in.  Once the extractor is holding a
     * complete segment, an extractor method fills it in.</p>
     */
    public int m_nPos;


    /**
     * <p>Mapping of characters to tokens.</p>
     */
    public Hashtable m_mapCharToType = mapCharToType();

    /**
     * Constructor.
     */
    public PTToken()
    {
        super();
    }

    /**
     * Contructor.
     * @param p_nType int - Token ID.
     * @param p_sContent - Token content.
     */
    public PTToken(int p_nType, String p_sContent)
    {
        m_nType = p_nType;
        m_strContent = p_sContent;
    }

    /**
     * Maps characters to Token types.
     * @return Hashtable
     */
    private Hashtable mapCharToType()
    {
        Hashtable h = new Hashtable();
        h.put(new Character('\u000c'), new Integer(PTToken.FF));
        h.put(new Character('\n'),     new Integer(PTToken.LINEBREAK)) ;
        h.put(new Character('\r'),     new Integer(PTToken.LF));
        h.put(new Character('\t'),     new Integer(PTToken.TAB));
        h.put(new Character('\u00a0'), new Integer(PTToken.NBSPACE));
        return h;
    }
}
