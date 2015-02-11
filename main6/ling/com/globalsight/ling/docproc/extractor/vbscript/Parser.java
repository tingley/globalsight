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
package com.globalsight.ling.docproc.extractor.vbscript;

import com.globalsight.ling.docproc.*;
import java.lang.Character.*;
import java.io.*;

/**
 * A simple VBScript parser.
 */
public class Parser
{
    /** The next char to be read */
    private char   m_chNextChar;
    private Reader m_InputReader = null;

    public Parser(Reader p_InputReader)
    {
        super();

        try
        {   // get Reader
            m_InputReader = p_InputReader;
            m_chNextChar = (char)m_InputReader.read();
        }
        catch (Exception e)
        {
            m_chNextChar = (char)-1;
        }
    }

    /**
     * <p>Sends back the next character and consumes it.</p>
     *
     * @return char the next character..
     * @exception VBEOFexception The end of the input has been reached.
     */
    protected char getNextChar()
        throws VBEOFexception
    {
        // We use a little trick here, since the parser like to have a
        // preview of what's coming we actually read one character
        // ahead (m_chNextChar). GetNextchar sends back the previous
        // char read and reads the next one. In order not to trigger
        // prematurely the EOF exception the preview for EOF is 0.
        char chCurrentChar = m_chNextChar;

        if (m_chNextChar == 0)
        {
            throw new VBEOFexception();
        }

        try
        {
            int nNextChar = m_InputReader.read();
            m_chNextChar = nNextChar == -1 ? (char)0 : (char)nNextChar;
        }
        catch (IOException e)
        {
            throw new VBEOFexception();

        }
        return chCurrentChar;
    }

    /**
     * <p>Sends back the next element of the JavaScript. We are only
     * interested in what could make a difference for string
     * extraction to recognize when strings are concatenated, etc.</p>
     *
     * @return VBToken the next Token.  A token of type EOF is
     * returned when the end of the input has been reached.
     */
    public VBToken getNextToken()
    {
        VBToken result = new VBToken();
        char chCurrentChar;

        try
        {
            chCurrentChar = getNextChar();
            result.m_strContent += chCurrentChar;
            result.m_nType = VBToken.UNKNOWN;

            // the new line event needs to be separated from white
            // spaces since we need it to specify the end of an
            // in-line comment, e.g. "//blabla "
            if (chCurrentChar == '\r' || chCurrentChar == '\n')
            {
                result.m_nType =  VBToken.NEWLINE;  // it's the new line
                while (getPreviewChar() == '\r' || getPreviewChar() == '\n')
                {
                    result.m_strContent += getNextChar();
                }
                return result;
            }


            if (Character.isWhitespace(chCurrentChar))
            {
                result.m_nType = VBToken.SPACES;
                while (Character.isWhitespace(getPreviewChar()))
                {
                    result.m_strContent += getNextChar();
                }
                return result;
            }

            // finding the comments marks
            if (chCurrentChar == '\'')
            {
                result.m_nType =  VBToken.COMMENT;  // found /* pattern

                while (getPreviewChar() != '\r' && getPreviewChar() != '\n')
                {
                    result.m_strContent += getNextChar();
                }

                return result;
            }

            //strings
            if (chCurrentChar == '"')
            {
                result = readString (chCurrentChar);
                return result;
            }

            // reading various token
            if (Character.isJavaIdentifierStart(chCurrentChar))
            {
                result.m_nType = VBToken.MISC_KEYWORD;

                while (Character.isJavaIdentifierPart(getPreviewChar()))
                {
                    result.m_strContent += getNextChar();
                }

                if (result.m_strContent.equalsIgnoreCase("return"))
                {
                    result.m_nType = VBToken.RETURN;
                }
                return result;
            }

            // finding the assignment
            else if (chCurrentChar == '=')
            {
                result.m_nType = VBToken.ASSIGN;
                return result;
            }

            // finding the <> operator...
            else if (chCurrentChar == '<')
            {
                if (getPreviewChar() != '>')
                {
                    result.m_nType = VBToken.MISC_OPERATOR;
                }
                else
                {
                    result.m_nType = VBToken.MISC_OPERATOR;
                }
                return result;
            }

            // finding the concatenation operator...
            else if (chCurrentChar == '+' || chCurrentChar == '&')
            {
                result.m_nType = VBToken.MISC_OPERATOR;
                return result;
            }
            else
            {
                // finding opening and closing brakets
                switch (chCurrentChar)
                {
                    case '(' : result.m_nType = VBToken.OPEN_PARENTHESIS;
                        break;
                    case ')' : result.m_nType = VBToken.CLOSE_PARENTHESIS;
                        break;
                    case '[' : result.m_nType = VBToken.OPEN_SQBRAQUET;
                        break;
                    case ']' : result.m_nType = VBToken.CLOSE_SQBRAQUET;
                        break;
                    default  : result.m_nType = VBToken.UNKNOWN;
                        break;
                }
                return result;
            }

        }
        catch (VBEOFexception e)
        {
            // if the result is different from zero, it means we got
            // stopped in the middle of parsing a token.  Really it
            // should only happen if we get an un-closed string...
            // the EOF will be caught at the next round...
            if (result.m_nType == VBToken.EMPTY)
            {
                result.m_nType = VBToken.EOF;
                result.m_strContent = "";
            }
        }

        return result;
    }

    /**
     * <p>Sends back the character returned from getNextChar(),
     * without consuming it.</p>
     */
    protected char getPreviewChar()
    {
        return m_chNextChar;
    }

    /**
     * <p>Parses a VBScript string and returns it, including
     * delimiters.</p>
     *
     * @param p_chDelimiter char Delimiter used for the string;
     * typically ' or ".
     */
    protected VBToken readString(char p_chDelimiter)
        throws VBEOFexception
    {
        VBToken result = new VBToken();

        try
        {
            result.m_nType = VBToken.STRING;
            result.m_strContent += p_chDelimiter;

            while (getPreviewChar() != p_chDelimiter)
            {
                result.m_strContent += getNextChar();
            }

            result.m_strContent += getNextChar();

            return result;
        }
        catch (VBEOFexception e)
        {
            // unclosed string - this is not normal.  This happens
            // with VBScripts comments that starts with ' any way we
            // don't want to lose data. this allows a round trip-safe
            // stuff (extraction is fucked up)
            return result;
        }
    }
}
