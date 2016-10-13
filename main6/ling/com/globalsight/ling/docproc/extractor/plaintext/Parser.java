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

import com.globalsight.ling.docproc.ExtractorException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * <p>Parser for plain text.</p>
 */
public class Parser
{
    // Buffer for the preview character.
    private char m_chNextChar;

    // The base imput stream.
    private Reader m_InputReader;

    // The base imput stream as a LineNumberReader.
    private LineNumberReader m_LineInputReader;

    /**
     * <p>Constructor - initializes the parser with an input stream.</p>
     *
     * @param: p_InputReader - the parser's input stream.
     */
    public Parser(Reader p_InputReader)
    {
        super();

        try
        {
            // get Reader
            m_InputReader = p_InputReader;

            // ...to be able to report errors by line number
            m_LineInputReader = new LineNumberReader(m_InputReader);

            // load first char
            m_chNextChar = (char)m_LineInputReader.read();
        }
        catch (Exception e)
        {
            m_chNextChar = (char)-1;
        }
    }

    /**
     * <p>Returns the next character from the input stream and
     * consumes it.  Throws an exception when the end of the stream is
     * reached.</p>
     *
     * @return the next character.
     * @exception PTEOFexception
     */
    protected char getNextChar()
        throws PTEOFexception
    {
        // We use a little trick here, since the parser likes to have
        // a preview of what is coming we actually read one character
        // ahead (m_chNextChar). GetNextchar sends back the previous
        // char read and reads the next one.

        if (m_chNextChar == (char)-1)
        {
            throw new PTEOFexception();
        }

        char chCurrentChar = m_chNextChar;

        try
        {
            m_chNextChar = (char)m_LineInputReader.read();

            //  int nNextChar = m_LineInputReader.read();
            //  m_chNextChar = nNextChar == -1? (char)0:(char)nNextChar;
        }
        catch (IOException e)
        {
            throw new PTEOFexception();
        }

        return chCurrentChar;
    }

    /**
     * <p>Returns the next token found.</p>
     *
     * @return PTToken - the next Token.<BR>
     *         A token of type EOF is returned when the end of
     *         the input is reached.
     */
    public PTToken getNextToken()
        throws ExtractorException
    {
        char chNext;
        PTToken Token = new PTToken();
        Integer nType;

        try
        {
            while (true)
            {
                chNext = getNextChar();
                Token.m_strContent = Token.m_strContent + chNext;
                Token.m_nLineNumber = m_LineInputReader.getLineNumber();

                // is this something we want to tag ?
                nType = (Integer)Token.m_mapCharToType.get(
                  new Character(chNext));

                if (nType != null)
                {
                    Token.m_nType = nType.intValue();
                    return Token;
                }
                else // otherwise its a text chunk
                {
                    Token.m_nType = PTToken.TEXT;
                    nType = (Integer)Token.m_mapCharToType.get(
                      new Character(getPreviewChar()));

                    if (nType != null)
                    {
                        return Token;
                    }
                }
            }
        }
        catch (PTEOFexception e)
        {
            if (Token.m_nType == PTToken.EMPTY)
            {
                Token.m_nType = PTToken.EOF;
                Token.m_strContent = "";
            }
        }
        return Token;
    }

    /**
     * <p>Returns the next character that will be returned from the
     * next call to getNextChar().</p>
     *
     * @return char - the next chararcter.
     */
    protected char getPreviewChar()
    {
        return m_chNextChar;
    }
}
