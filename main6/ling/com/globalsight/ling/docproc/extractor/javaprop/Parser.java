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
package com.globalsight.ling.docproc.extractor.javaprop;

import com.globalsight.ling.docproc.ExtractorException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * <p>Parser for Java properties.</p>
 *
 * Creation date: (7/14/2000 4:46:24 PM)
 * @author: Bill Brotherton <BR>
 */
public class Parser
{
    //
    // Private Member Variables
    //

    // FFFC, Object Replacement Character, is our one-char placeholder
    // for line continuations.
    // static public final char s_chLineSlice = '\uFFFC';

    private char m_chNextChar;
    private Reader m_InputReader;
    private LineNumberReader m_LineInputReader;
    private int m_nParserState;
    protected JPToken m_curToken;

    //
    // Constructor
    //

    /**
     * <p>Initializes the parser with an input reader object.</p>
     */
    public Parser(Reader p_InputReader)
    {
        super();

        m_nParserState = JPToken.EMPTY;

        try
        {   // get Reader
            m_InputReader = p_InputReader;
            // to be able to report errors by line number
            m_LineInputReader = new LineNumberReader(m_InputReader);

            m_chNextChar = (char)m_LineInputReader.read();
        }
        catch (Exception e)
        {
            m_chNextChar = (char)-1;
        }
    }


    /**
     * <p>Gets the next single character from the input stream.</p>
     *
     * @author Thierry Sourbier - from original Javascript parser
     * @return the next single character and consumes it.  Throws an
     * exception when the end of the stream is reached.
     * @exception JPEOFexception
     */
    protected char getNextChar()
        throws JPEOFexception
    {
        // We use a little trick here, since the parser likes to have
        // a preview of what is coming we actually read one character
        // ahead (m_chNextChar). GetNextchar sends back the previous
        // char read and reads the next one. In order not to trigger
        // prematurely the EOF exception the preview for EOF is 0.

        if (m_chNextChar == (char)-1)
        {
            throw new JPEOFexception();
        }

        char chCurrentChar = m_chNextChar;
        if (m_chNextChar == 0)
        {
            throw new JPEOFexception();
        }

        try
        {
            int nNextChar = m_LineInputReader.read();
            m_chNextChar = nNextChar == -1 ? (char)0 : (char)nNextChar;
        }
        catch (IOException e)
        {
            throw new JPEOFexception();
        }

        return chCurrentChar;
    }

    /**
     * <p>Gets the next character from the input stream but also
     * decodes escapes and carriage return line feeds.  In either
     * case, the entire sequence is consumed and added to the token
     * content.  Only the last or resulting char is returned and used
     * to make decisions about the input.  This method is used to help
     * parse everything outside of an actual property value.  When we
     * are reading an actual property value (with parseValue()), we
     * call getNextChar() directly to read the raw content.</p>
     *
     * <p>The method should only be called while we are outside of a
     * prop value.</p>
     *
     * @author: Bill Brotherton
     * @return char
     * @exception JPEOFexception
     */
    protected char getNextParsedChar()
        throws JPEOFexception
    {
        char chCurrentChar;

        chCurrentChar = getNextChar();

        m_curToken.m_strContent += chCurrentChar;

        // read full new line ( \r\n or \r or \n )
        if ((chCurrentChar == '\n' || chCurrentChar == '\r') &&
          (getPreviewChar() == '\n' || getPreviewChar() == '\r'))
        {
            chCurrentChar = getNextChar();
            m_curToken.m_strContent += chCurrentChar;
        }

        // ASCII escapes - save the escape character and read ahead
        if (chCurrentChar == '\\' &&
          (getPreviewChar() != '\n' && getPreviewChar() != '\r'))
        {
            chCurrentChar = getNextChar();
            m_curToken.m_strContent += chCurrentChar;
        }

        return chCurrentChar;
    }

    /**
     * Returns the next element or token of a property file.<BR><BR>
     *
     *  - Key with no value.<BR>
     *    When a key has no value, the line terminator is both part
     *    of the valid terminator and the end-of-line. In this case
     *    we return a special token KEY_TERMINATOR_EMPTY_VALUE.
     *    This token indicates the a VALUE token will not follow.
     *    See the parseTerminator() method.<BR><BR>
     *
     *  - Line splices<BR>
     *    Line splices are thrown away. Spliced strings are extracted
     *    as one string and will be merged as one string.<BR>
     *    See the parseRawValue() method.<BR>
     *
     * @author Bill Brotherton
     * @return JPToken - the next Token.<BR>
     *         A token of type EOF is returned when the end of the
     *         input is reached.
     */
    public JPToken getNextToken()
        throws ExtractorException
    {
        char chCurrentChar;

        try
        {
            // parse for TERMINATOR
            if (m_nParserState == JPToken.PROP_KEY)
            {
                m_curToken = new JPToken();
                parseTerminator();
                return m_curToken;
            }

            // parse for raw VALUE
            if (m_nParserState == JPToken.KEY_TERMINATOR)
            {
                m_curToken = new JPToken();
                parseRawValue();
                return m_curToken;
            }

            // resume normal parsing for KEYS
            m_curToken = new JPToken();
            parseNormal();
            return m_curToken;
        }
        catch (JPEOFexception e)
        {
            // if token is not EMPTY, it means that we were stopped in
            // the middle of parsing a token.  Really it should only
            // happen if a property value is actually EOF.  The EOF
            // will be captured at the next round...
            if (m_curToken.m_nType == JPToken.EMPTY)
            {
                m_curToken.m_nType = JPToken.EOF;
                m_curToken.m_strContent = "";
            }
        }

        return m_curToken;
    }

    /**
     * <p>Returns the next character that will be returned by
     * getNextChar() without consuming it.</p>
     *
     * @author Thierry Sourbier - from original Javascript parser
     * @return char
     */
    protected char getPreviewChar()
    {
        return m_chNextChar;
    }

    /**
     * <p>This sub parser reads the bulk of the file and returns either
     * SPACES, PROP_COMMENT or PROP_KEY tokens.  A parser state flag
     * (m_nParserState) is set according to the type of token found.
     * This flag is used by the Parser() method to invoke the
     * appropriate sub parser on the next pass.</p>
     *
     * Creation date: (7/23/2000 9:36:43 PM)
     * @author: Bill Brotherton
     */
    private void parseNormal()
        throws JPEOFexception
    {
        char chCurrentChar, chPreviewChar;

        chCurrentChar = getNextParsedChar();

        chPreviewChar = getPreviewChar();

        // white space
        if (Character.isWhitespace(chCurrentChar) ||
          (chCurrentChar == '\\' &&
            (chPreviewChar == '\n' || chPreviewChar == '\r')))
        {
            m_curToken.m_nType = m_nParserState = JPToken.SPACES;

            if (chCurrentChar == '\\') //line splice
            {
                m_curToken.m_strContent += chCurrentChar;
            }

            while (Character.isWhitespace(getPreviewChar()))
            {
                getNextParsedChar();
            }

            m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
            return;
        }

        // finding comment markers
        if (chCurrentChar == '#' || chCurrentChar == '!')
        {
            m_curToken.m_nType = m_nParserState =  JPToken.PROP_COMMENT;

            while (!((getPreviewChar() == '\r') || (getPreviewChar() == '\n')))
            {
                getNextParsedChar();
            }

            m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
            return;
        }

        // finding property keys
        // we have already scanned for leading white space
        // so this must be the start of a key
        m_curToken.m_nType = m_nParserState = JPToken.PROP_KEY;
        while (!Character.isWhitespace(getPreviewChar()) &&
          (getPreviewChar() != ':') && (getPreviewChar() != '='))
        {
            getNextParsedChar();
        }

        m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
        return;
    }

    /**
     * <p>This sub parser is invoked imeadiatly after reading a
     * TERMINATOR token to parse the property value that follows the
     * terminator.</p>
     *
     * <p>
     *  - Line splices<br>
     *    Line splices are thrown away. Spliced strings are extracted
     *    as one string and will also be merged as one continuous string.
     * </p>
     *
     * <p>After reading the value, the string is passed to a general
     * escape sequence decoder.</p>
     *
     * <p>The parser state flag (m_nParserState) is set to PROP_VALUE.
     * This flag is used by the Parser() method to invoke the
     * appropriate sub parser on the next pass.</p>
     *
     * Creation date: (7/23/2000 9:36:43 PM)
     * @author: Bill Brotherton
     */
    private void parseRawValue()
        throws JPEOFexception,
               ExtractorException
    {
        char chCurrentChar;
        String tmp = new String();

        m_curToken.m_nType = JPToken.KEY_VALUE;
        m_nParserState = JPToken.EMPTY;

        try
        {
            do // read entire raw value
            {
                // NOTE:
                // A key followed by an empty value condition like (key[\n])
                // is handled by parseTerminator() - and it is not extracted

                chCurrentChar = getNextChar();

                // Line continuation
                // We throw out the line continuation sequence and
                // make it one line on merge.
                if (chCurrentChar == '\\' &&
                  (getPreviewChar() == '\r' || getPreviewChar() == '\n'))
                {
                    // tmp += Parser.s_chLineSlice;

                    // read 1 line only, safely!, and ignore it
                    if (getPreviewChar() == '\r')
                    {
                        /* tmp += */ getNextChar();
                        if (getPreviewChar() == '\n')
                        {
                            /* tmp += */ getNextChar();
                        }
                    }
                    else /* if (getPreviewChar() == '\n') */
                    {
                        /*tmp += */ getNextChar();
                    }

                    while (getPreviewChar() == ' ' ||
                      getPreviewChar() == '\t')
                    {
                        /*tmp += */ getNextChar();
                    }
                }
                else
                {
                    tmp += chCurrentChar;
                }
            }
            while ((getPreviewChar() != '\r') && (getPreviewChar() != '\n'));

            m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
            m_curToken.m_strContent = tmp;

            return ;
        }
        catch (JPEOFexception e)
        {
            if (tmp.length() == 0)
            {
                m_curToken.m_nType = m_nParserState = JPToken.EOF;
            }
            else
            {
                m_curToken.m_strContent = tmp;
            }
            m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
            return;
        }
    }

    /**
     * <p>This sub parser is invoked imeadiatly after reading a KEY
     * token. The token type can be set to either KEY_TERMINATOR or
     * KEY_TERMINATOR_EMPTY_VALUE.</p>
     *
     * <p>
     *  - An empty Key with no property value.<br>
     *    When a key has no value, the line terminator is both part of
     *    the valid terminator and the end-of-line marker. In this
     *    case we return a special token KEY_TERMINATOR_EMPTY_VALUE.
     *    This token indicates that a VALUE token will not follow.
     * </p>
     *
     * <p>The parser state flag (m_nParserState) is set according to
     * the type of token found.  This flag is used by the Parser()
     * method to invoke the appropriate sub parser on the next
     * pass.</p>
     *
     * Creation date: (7/23/2000 9:36:43 PM)
     * @author: Bill Brotherton
     */
    private void parseTerminator()
        throws JPEOFexception
    {
        char chResult;

        m_curToken.m_nType = m_nParserState = JPToken.KEY_TERMINATOR;
        chResult = getNextParsedChar();

        // newline as terminator
        if (chResult == '\r' || chResult == '\n' ||
          getPreviewChar() == '\r' || getPreviewChar() == '\n')
        {
            if (chResult != '\r' && chResult != '\n')
            {
                getNextParsedChar();
            }

            m_curToken.m_nType = m_nParserState =
              JPToken.KEY_TERMINATOR_EMPTY_VALUE;
            m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
            return;
        }

        // leading white space
        if (Character.isWhitespace(getPreviewChar()))
        {
            chResult = getNextParsedChar();

            while (Character.isWhitespace(getPreviewChar()))
            {
                chResult = getNextParsedChar();
            }
        }

        // find possible termination markers
        if (getPreviewChar() == ':' || getPreviewChar() == '=')
        {
            chResult = getNextParsedChar();

            // check for trailing white space
            while (Character.isWhitespace(getPreviewChar()))
            {
                chResult = getNextParsedChar();
            }
        }


        // newline terminator
        if (chResult == '\r' || chResult == '\n')
        {
            m_curToken.m_nType = m_nParserState =
              JPToken.KEY_TERMINATOR_EMPTY_VALUE;
        }

        m_curToken.m_nInputLineNumber = m_LineInputReader.getLineNumber();
        return;
    }


    /**
     * <p>Test wrapper to simply run and test the parser in a
     * stand-alone fashion.</p>
     *
     * Creation date: (7/25/2000 3:48:13 PM)
     */
    public static void main(String args[])
    {
        java.io.FileReader r;
        //java.io.FileWriter wDupOut;
        java.io.OutputStreamWriter wDupOut;
        java.io.FileWriter wPropOut;
        java.io.PrintWriter pPropOut;

        try
        {
            // input
            java.io.File fin = new java.io.File("C:\\work\\ling\\test\\globalsight\\ling\\docproc\\extractor\\javaprop\\TestFiles\\jp_self_generating.properties");
            r = new java.io.FileReader(fin);

            Parser parser = new Parser(r);

            // output
            java.io.File fDupOut = new java.io.File("C:\\work\\ling\\test\\globalsight\\ling\\docproc\\extractor\\javaprop\\TestFiles\\dupout.txt");
            java.io.FileOutputStream fos =  new java.io.FileOutputStream(fDupOut);
            wDupOut = new java.io.OutputStreamWriter(fos, "UTF8");


            java.io.File fPropOut = new java.io.File("C:\\work\\ling\\test\\globalsight\\ling\\docproc\\extractor\\javaprop\\TestFiles\\javapropout.txt");
            wPropOut =  new java.io.FileWriter(fPropOut);
            pPropOut =  new java.io.PrintWriter(wPropOut);

            parser.getNextToken();

            while(parser.m_curToken.m_nType != JPToken.EOF)
            {

                wDupOut.write(parser.m_curToken.m_strContent);

                if(parser.m_curToken.m_nType == JPToken.PROP_KEY )
                {
                    pPropOut.write(parser.m_curToken.m_strContent +"|");
                }

                if(parser.m_curToken.m_nType == JPToken.KEY_TERMINATOR )
                {
                    pPropOut.write(parser.m_curToken.m_strContent + "|");
                }

                if(parser.m_curToken.m_nType == JPToken.KEY_TERMINATOR_EMPTY_VALUE )
                {
                    pPropOut.write(parser.m_curToken.m_strContent);
                }

                if(parser.m_curToken.m_nType == JPToken.KEY_VALUE)
                {
                    pPropOut.println(parser.m_curToken.m_strContent);
                    // pPropOut.write(parser.m_curToken.m_strContent);
                }

                if(parser.m_curToken.m_nType == JPToken.EMPTY)
                {

                }

                if(parser.m_curToken.m_nType == JPToken.UNKNOWN)
                {

                }

                if(parser.m_curToken.m_nType == JPToken.PROP_COMMENT)
                {
                    // pPropOut.write(parser.m_curToken.m_strContent);
                }

                if(parser.m_curToken.m_nType == JPToken.SPACES)
                {
                    // pPropOut.write(parser.m_curToken.m_strContent);
                }

                parser.getNextToken();
            }

            r.close();
            wDupOut.close();
            wPropOut.close();
            pPropOut.close();
        }
        catch(Exception e)
        {
            System.out.println( e );
            //e.printStackTrace();
        }
    }
}
