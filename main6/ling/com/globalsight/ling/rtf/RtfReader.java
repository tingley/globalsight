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
//===============================================================
// package : com.tetrasix.majix.rtf
// class : com.tetrasix.majix.RtfReader
//===============================================================
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is TetraSys code.
//
// The Initial Developer of the Original Code is TetraSys..
// Portions created by TetraSys are
// Copyright (C) 1998-2000 TetraSys. All Rights Reserved.
//
// Contributor(s):
//===============================================================
package com.globalsight.ling.rtf;

import java.io.*;
import java.io.Reader;

public class RtfReader
{
    //
    // Private Members
    //

    private PushbackReader _in;
    private StringBuffer _buf;
    private int _depth;
    private String _filename;
    private long _nbtoken;

    private int _line;

    //
    // Constructor
    //

    /**
     * Default constructor: caller must call one of the init() methods
     * to use this object.
     */
    public RtfReader()
    {
    }

    public RtfReader(String p_filename)
        throws FileNotFoundException
    {
        init(p_filename);
    }

    public RtfReader(Reader p_reader)
    {
        init(p_reader);
    }

    //
    // Public Methods
    //

    public void init(String p_filename)
        throws FileNotFoundException
    {
        _in = new PushbackReader(new InputStreamReader(
            new BufferedInputStream(new FileInputStream(p_filename))), 10);
        _buf = new StringBuffer();
        _line = 1;
        _depth = 0;
        _filename = p_filename;
        _nbtoken = 0;
    }

    public void init(Reader p_reader)
    {
        _in = new PushbackReader(p_reader, 10);
        _buf = new StringBuffer();
        _line = 1;
        _depth = 0;
        _filename = "<stream>";
        _nbtoken = 0;
    }

    public void exit()
    {
        if (_in != null)
        {
            try
            {
                _in.close();
            }
            catch (Throwable ignore)
            {
            }

            _in = null;
            _buf = null;
            _filename = null;
        }
    }

    public String getFileName()
    {
        return _filename;
    }

    public int getLine()
    {
        return _line;
    }

    public int getDepth()
    {
        return _depth;
    }

    public long getTokenCount()
    {
        return _nbtoken;
    }

    public RtfToken getNextToken()
        throws IOException
    {
        long now = System.currentTimeMillis();

        try
        {
        RtfToken tok;

        _buf.setLength(0);
        _nbtoken++;

        for (;;)
        {
            int ch = _in.read();

            if (ch == -1)
            {
                return null;
            }
            else if (ch == '\\')
            {
                if (_buf.length() > 0)
                {
                    _in.unread(ch);
                    String data = _buf.toString();
                    _buf.setLength(0);
                    return new RtfToken(data);
                }

                ch = _in.read();

                if (ch == '*')
                {
                    return new RtfToken(RtfToken.ASTERISK);
                }
                else if (ch == '\'')
                {
                    char ch1 = (char) _in.read();
                    char ch2 = (char) _in.read();
                    ch = Character.digit(ch1, 16) * 16 + Character.digit(ch2, 16);
                    _buf.append((char)ch);
                    
                    ch = _in.read();

                    while (ch == '\\')
                    {
                        char ch3 = (char) _in.read();

                        if (ch3 != '\'')
                        {
                            _in.unread(ch3);
                            break;
                        }

                        ch1 = (char) _in.read();
                        ch2 = (char) _in.read();
                        ch = Character.digit(ch1, 16) * 16
                                + Character.digit(ch2, 16);
                        _buf.append((char) ch);

                        ch = _in.read();

                    }
                    
                    _in.unread(ch);
                    
                    String data = _buf.toString();
                    return new RtfToken(data);
                }
                else if (ch == '\\')
                {
                    return new RtfToken("\\");
                }
                else if (ch == '~')
                {
                    return new RtfToken(RtfToken.CONTROLWORD, "~", null);
                }
                else if (ch == '-')
                {
                    return new RtfToken(RtfToken.CONTROLWORD, "-", null);
                }
                else if (ch == '{')
                {
                    return new RtfToken("{");
                }
                else if (ch == '}')
                {
                    return new RtfToken("}");
                }
                else if (ch == '\n')
                {
                    _line++;
                    return new RtfToken(RtfToken.CONTROLWORD, "par", null);
                }
                else if (ch == '\r')
                {
                    return new RtfToken(RtfToken.CONTROLWORD, "par", null);
                }

                while (Character.isLetter((char)ch))
                {
                    _buf.append((char)ch);
                    ch = _in.read();
                }
                String name = _buf.toString();
                _buf.setLength(0);

                if (ch == '-')
                {
                    _buf.append((char)ch);
                    ch = _in.read();
                }

                while (Character.isDigit((char)ch))
                {
                    _buf.append((char)ch);
                    ch = _in.read();
                }

                if (! Character.isSpaceChar((char)ch))
                {
                    // Note: last space technically belongs to control
                    _in.unread(ch);
                }

                String data = _buf.toString();
                _buf.setLength(0);

                return new RtfToken(RtfToken.CONTROLWORD, name, data);
            }
            else if (ch == '{')
            {
                if (_buf.length() > 0)
                {
                    _in.unread(ch);

                    String data = _buf.toString();
                    return new RtfToken(data);
                }

                _depth++;

                return new RtfToken(RtfToken.OPENGROUP);
            }
            else if (ch == '}')
            {
                if (_buf.length() > 0)
                {
                    _in.unread(ch);

                    String data = _buf.toString();
                    return new RtfToken(data);
                }

                _depth--;

                return new RtfToken(RtfToken.CLOSEGROUP);
            }
            else if (ch == '\r')
            {
            }
            else if (ch == '\n')
            {
                _line++;

                if (_buf.length() > 0)
                {
                    String data = _buf.toString();
                    return new RtfToken(data);
                }
            }
            else
            {
                _buf.append((char)ch);
            }
        }
    }
        finally
        {
            long then = System.currentTimeMillis();

            if (then - now > 1000) System.err.println("Tokenizer took " + (then - now) + "ms buflen=" + _buf.length());
        }
    }
}
