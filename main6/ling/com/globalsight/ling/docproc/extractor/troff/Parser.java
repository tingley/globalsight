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
package com.globalsight.ling.docproc.extractor.troff;

import com.globalsight.ling.docproc.extractor.troff.ExtractionHandler;

import com.globalsight.ling.docproc.ExtractorException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * <p>Parser for troff files.</p>
 */
public class Parser
{
    // Buffer for the preview character.
    private char m_chNextChar;

    // The base imput stream.
    private Reader m_inputReader;

    // The base imput stream as a LineNumberReader.
    private LineNumberReader m_lineInputReader;

    private ExtractionHandler m_handler;

    /**
     * <p>Constructor - initializes the parser with an input stream.</p>
     *
     * @param p_inputReader - the parser's input stream.
     */
    public Parser(Reader p_inputReader, ExtractionHandler p_handler)
    {
        m_handler = p_handler;

        try
        {
            // get Reader
            m_inputReader = p_inputReader;

            // ...to be able to report errors by line number
            m_lineInputReader = new LineNumberReader(m_inputReader);

            // load first char
            m_chNextChar = (char)m_lineInputReader.read();
        }
        catch (Exception e)
        {
            m_chNextChar = (char)-1;
        }
    }

    public void parse()
        throws ExtractorException
    {
        Token token;

        m_handler.handleStart();

        while (true)
        {
            token = getNextToken();

            if (token.m_nType == Token.EOF)
            {
                break;
            }

            m_handler.handleToken(token);
        }

        m_handler.handleFinish();
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

    /**
     * <p>Returns the next character from the input stream and
     * consumes it.  Throws an exception when the end of the stream is
     * reached.</p>
     *
     * @return the next character.
     * @exception EOFException on end of file
     */
    protected char getNextChar()
        throws EOFException
    {
        // We use a little trick here, since the parser likes to have
        // a preview of what is coming we actually read one character
        // ahead (m_chNextChar). GetNextchar sends back the previous
        // char read and reads the next one.

        if (m_chNextChar == (char)-1)
        {
            throw new EOFException();
        }

        char chCurrentChar = m_chNextChar;

        try
        {
            m_chNextChar = (char)m_lineInputReader.read();
        }
        catch (IOException e)
        {
            throw new EOFException();
        }

        return chCurrentChar;
    }

    /**
     * <p>Returns the next token found.</p>
     *
     * @return PTToken - the next Token.<BR> A token of type EOF is
     * returned when the end of the input is reached.
     */
    public Token getNextToken()
        throws ExtractorException
    {
        char chNext;
        Token token = new Token();
        Integer nType;

        try
        {
            while (true)
            {
                chNext = getNextChar();
                token.m_strContent = token.m_strContent + chNext;
                token.m_nLineNumber = m_lineInputReader.getLineNumber();

                // is this something we want to tag ?
                nType = (Integer)token.m_mapCharToType.get(
                    new Character(chNext));

                if (nType != null)
                {
                    token.m_nType = nType.intValue();
                    return token;
                }
                else // otherwise its a text chunk
                {
                    token.m_nType = Token.TEXT;
                    nType = (Integer)token.m_mapCharToType.get(
                        new Character(getPreviewChar()));

                    if (nType != null)
                    {
                        return token;
                    }
                }
            }
        }
        catch (EOFException e)
        {
            if (token.m_nType == Token.EMPTY)
            {
                token.m_nType = Token.EOF;
                token.m_strContent = "";
            }
        }

        return token;
    }
}
