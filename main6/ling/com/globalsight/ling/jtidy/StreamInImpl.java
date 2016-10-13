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
package com.globalsight.ling.jtidy;

import java.io.IOException;
import java.io.InputStream;

import com.globalsight.ling.jtidy.EncodingUtils.GetBytes;


/**
 * Input Stream Implementation. This implementation is from the c version of tidy and it doesn't take advantage of java
 * readers.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class StreamInImpl implements StreamIn
{

    /**
     * number of characters kept in buffer.
     */
    private static final int CHARBUF_SIZE = 5;

    /**
     * needed for error reporting.
     */
    private Lexer lexer;

    /**
     * character buffer.
     */
    private int[] charbuf = new int[CHARBUF_SIZE];

    /**
     * actual position in buffer.
     */
    private int bufpos;

    /**
     * Private unget buffer for the raw bytes read from the input stream. Normally this will only be used by the UTF-8
     * decoder to resynchronize the input stream after finding an illegal UTF-8 sequences. But it can be used for other
     * purposes when reading bytes in ReadCharFromStream.
     */
    private char[] rawBytebuf = new char[CHARBUF_SIZE];

    /**
     * actual position in rawBytebuf.
     */
    private int rawBufpos;

    /**
     * has a raw byte been pushed into stack?
     */
    private boolean rawPushed;

    /**
     * looking for an UTF BOM?
     */
    private boolean lookingForBOM = true;

    /**
     * has end of stream been reached?
     */
    private boolean endOfStream;

    private boolean pushed;

    private int tabs;

    /**
     * tab size in chars.
     */
    private int tabsize;

    /**
     * FSM for ISO2022.
     */
    private int state;

    /**
     * Encoding.
     */
    private int encoding;

    /**
     * current column number.
     */
    private int curcol;

    /**
     * last column.
     */
    private int lastcol;

    /**
     * current line number.
     */
    private int curline;

    /**
     * input stream.
     */
    private InputStream stream;

    /**
     * Getter.
     */
    private GetBytes getBytes;

    /**
     * Avoid mapping values > 127 to entities.
     */
    private boolean rawOut;

    /**
     * Instatiates a new StreamInImpl.
     * @param stream input stream
     * @param configuration Configuration
     */
    public StreamInImpl(InputStream stream, Configuration configuration)
    {
        this.stream = stream;
        this.charbuf[0] = '\0';
        this.tabsize = configuration.tabsize;
        this.curline = 1;
        this.curcol = 1;
        this.encoding = configuration.getInCharEncoding();
        this.rawOut = configuration.rawOut;
        this.state = EncodingUtils.FSM_ASCII;
        this.getBytes = new GetBytes()
        {

            StreamInImpl in;

            GetBytes setStreamIn(StreamInImpl in)
            {
                this.in = in;
                return this;
            }

            public void doGet(int[] buf, int[] count, boolean unget)
            {
                in.readRawBytesFromStream(buf, count, unget);
            }
        } // set the StreamInImpl instance directly
            .setStreamIn(this);
    }

    /**
     * @see com.globalsight.ling.jtidy.StreamIn#getCurcol()
     */
    public int getCurcol()
    {
        return this.curcol;
    }

    /**
     * @see com.globalsight.ling.jtidy.StreamIn#getCurline()
     */
    public int getCurline()
    {
        return this.curline;
    }

    /**
     * Setter for <code>lexer</code>.
     * @param lexer The lexer to set.
     */
    public void setLexer(Lexer lexer)
    {
        this.lexer = lexer;
    }

    /**
     * @see com.globalsight.ling.jtidy.StreamIn#readChar()
     */
    public int readChar()
    {
        int c;

        if (this.pushed)
        {
            c = this.charbuf[--(this.bufpos)];
            if ((this.bufpos) == 0)
            {
                this.pushed = false;
            }

            if (c == '\n')
            {
                this.curcol = 1;
                this.curline++;
            }
            else
            {
                this.curcol++;
            }

            return c;
        }

        this.lastcol = this.curcol;

        if (this.tabs > 0)
        {
            this.curcol++;
            this.tabs--;
            return ' ';
        }

        while (true)
        {
            c = readCharFromStream();

            if (c < 0)
            {
                return END_OF_STREAM;
            }

            if (c == '\n')
            {
                this.curcol = 1;
                this.curline++;
                break;
            }

            // #427663 - map '\r' to '\n' - Andy Quick 11 Aug 00
            if (c == '\r')
            {
                c = readCharFromStream();
                if (c != '\n')
                {
                    if (c != END_OF_STREAM) // EOF fix by Terry Teague 12 Aug 01
                    {
                        ungetChar(c);
                    }
                    c = '\n';
                }
                this.curcol = 1;
                this.curline++;
                break;
            }

            if (c == '\t')
            {
                this.tabs = this.tabsize - ((this.curcol - 1) % this.tabsize) - 1;
                this.curcol++;
                c = ' ';
                break;
            }

            // strip control characters, except for Esc
            if (c == '\033')
            {
                break;
            }
            else if (c == '\015' && !lexer.configuration.xmlTags) //Form Feed is allowed in HTML
            {
                break;
            }
            else if (0 < c && c < 32)
            {
                continue; // discard control char
            }

            // watch out for chars that have already been decoded such as
            // IS02022, UTF-8 etc, that don't require further decoding
            if (rawOut
                || this.encoding == Configuration.ISO2022
                || this.encoding == Configuration.UTF8
                || this.encoding == Configuration.SHIFTJIS // #431953 - RJ
                || this.encoding == Configuration.BIG5) // #431953 - RJ
            {
                this.curcol++;
                break;
            }

            // handle surrogate pairs
            if ((this.encoding == Configuration.UTF16LE)
                || (this.encoding == Configuration.UTF16)
                || (this.encoding == Configuration.UTF16BE))
            {
                if (c > EncodingUtils.MAX_UTF8_FROM_UCS4)
                {
                    // invalid UTF-16 value
                    this.lexer.report.encodingError(this.lexer, Report.INVALID_UTF16 | Report.DISCARDED_CHAR, c);
                    c = 0;
                }
                // high surrogate
                else if (c >= EncodingUtils.UTF16_LOW_SURROGATE_BEGIN && c <= EncodingUtils.UTF16_LOW_SURROGATE_END)
                {
                    int n, m;

                    n = c;

                    m = readCharFromStream();
                    if (m < 0)
                    {
                        return END_OF_STREAM;
                    }
                    // low surrogate
                    if (m >= EncodingUtils.UTF16_HIGH_SURROGATE_BEGIN && m <= EncodingUtils.UTF16_HIGH_SURROGATE_END)
                    {
                        // pair found, recombine them
                        c = (n - EncodingUtils.UTF16_LOW_SURROGATE_BEGIN)
                            * 0x400
                            + (m - EncodingUtils.UTF16_HIGH_SURROGATE_BEGIN)
                            + 0x10000;

                        // check for invalid pairs
                        if (((c & 0x0000FFFE) == 0x0000FFFE)
                            || ((c & 0x0000FFFF) == 0x0000FFFF)
                            || (c < EncodingUtils.UTF16_SURROGATES_BEGIN))
                        {
                            this.lexer.report
                                .encodingError(this.lexer, Report.INVALID_UTF16 | Report.DISCARDED_CHAR, c);
                            c = 0;
                        }
                    }
                    else
                    {
                        // not a valid pair
                        this.lexer.report.encodingError(this.lexer, Report.INVALID_UTF16 | Report.DISCARDED_CHAR, c);
                        c = 0;
                        // should we unget the just read char?
                    }
                }
                else
                {
                    // no recombination needed
                }
            }

            if (this.encoding == Configuration.MACROMAN)
            {
                c = EncodingUtils.decodeMacRoman(c);
            }

            // produced e.g. as a side-effect of smart quotes in Word
            // but can't happen if using MACROMAN encoding
            if (127 < c && c < 160)
            {
                int c1 = 0;
                int replaceMode;

                // set error position just before offending character
                this.lexer.lines = this.curline;
                this.lexer.columns = this.curcol;

                if ((this.encoding == Configuration.WIN1252)
                    || (this.lexer.configuration.replacementCharEncoding == Configuration.WIN1252))
                {
                    c1 = EncodingUtils.decodeWin1252(c);
                }
                else if (this.lexer.configuration.replacementCharEncoding == Configuration.MACROMAN)
                {
                    c1 = EncodingUtils.decodeMacRoman(c);
                }

                replaceMode = TidyUtils.toBoolean(c1) ? Report.REPLACED_CHAR : Report.DISCARDED_CHAR;

                if ((c1 == 0) && (this.encoding == Configuration.WIN1252) || (this.encoding == Configuration.MACROMAN))
                {
                    this.lexer.report.encodingError(this.lexer, Report.VENDOR_SPECIFIC_CHARS | replaceMode, c);
                }
                else if ((this.encoding != Configuration.WIN1252) && (this.encoding != Configuration.MACROMAN))
                {
                    this.lexer.report.encodingError(this.lexer, Report.INVALID_SGML_CHARS | replaceMode, c);
                }

                c = c1;
            }

            if (c == 0)
            {
                continue; // illegal char is discarded
            }

            this.curcol++;
            break;
        }

        return c;
    }

    /**
     * @see com.globalsight.ling.jtidy.StreamIn#ungetChar(int)
     */
    public void ungetChar(int c)
    {
        this.pushed = true;
        if (this.bufpos >= CHARBUF_SIZE)
        {
            // pop last element
            System.arraycopy(this.charbuf, 0, this.charbuf, 1, CHARBUF_SIZE - 1);
            this.bufpos--;
        }
        this.charbuf[(this.bufpos)++] = c;

        if (c == '\n')
        {
            --this.curline;
        }

        this.curcol = this.lastcol;
    }

    /**
     * @see com.globalsight.ling.jtidy.StreamIn#isEndOfStream()
     */
    public boolean isEndOfStream()
    {
        return this.endOfStream;
    }

    /**
     * @see com.globalsight.ling.jtidy.StreamIn#readCharFromStream()
     */
    public int readCharFromStream()
    {
        int c;
        int[] n = new int[]{0};
        int[] tempchar = new int[1];
        int[] count = new int[]{1};

        readRawBytesFromStream(tempchar, count, false);
        if (count[0] <= 0)
        {
            endOfStream = true;
            return END_OF_STREAM;
        }

        c = tempchar[0];

        if (lookingForBOM
            && (this.encoding == Configuration.UTF16
                || this.encoding == Configuration.UTF16LE
                || this.encoding == Configuration.UTF16BE || this.encoding == Configuration.UTF8))
        {
            // check for a Byte Order Mark
            int c1, bom;

            lookingForBOM = false;

            if (c == END_OF_STREAM)
            {
                lookingForBOM = false;
                endOfStream = true;
                return END_OF_STREAM;
            }

            count[0] = 1;
            readRawBytesFromStream(tempchar, count, false);
            c1 = tempchar[0];

            bom = (c << 8) + c1;

            if (bom == EncodingUtils.UNICODE_BOM_BE)
            {
                // big-endian UTF-16
                if (this.encoding != Configuration.UTF16 && this.encoding != Configuration.UTF16BE)
                {
                    this.lexer.report.encodingError(this.lexer, Report.ENCODING_MISMATCH, Configuration.UTF16BE);
                    // non-fatal error
                }
                this.encoding = Configuration.UTF16BE;
                this.lexer.configuration.setInCharEncoding(Configuration.UTF16BE);
                return EncodingUtils.UNICODE_BOM; // return decoded BOM
            }
            else if (bom == EncodingUtils.UNICODE_BOM_LE)
            {
                // little-endian UTF-16
                if (this.encoding != Configuration.UTF16 && this.encoding != Configuration.UTF16LE)
                {
                    this.lexer.report.encodingError(this.lexer, Report.ENCODING_MISMATCH, Configuration.UTF16LE);
                    // non-fatal error
                }
                this.encoding = Configuration.UTF16LE;
                this.lexer.configuration.setInCharEncoding(Configuration.UTF16LE);
                return EncodingUtils.UNICODE_BOM; // return decoded BOM
            }
            else
            {
                int c2;

                count[0] = 1;
                readRawBytesFromStream(tempchar, count, false);
                c2 = tempchar[0];

                if (((c << 16) + (c1 << 8) + c2) == EncodingUtils.UNICODE_BOM_UTF8)
                {
                    // UTF-8
                    this.encoding = Configuration.UTF8;
                    if (this.encoding != Configuration.UTF8)
                    {
                        this.lexer.report.encodingError(this.lexer, Report.ENCODING_MISMATCH, Configuration.UTF8);
                        // non-fatal error
                    }
                    this.lexer.configuration.setInCharEncoding(Configuration.UTF8);
                    return EncodingUtils.UNICODE_BOM; // return decoded BOM
                }

                // the 2nd and/or 3rd bytes weren't what we were expecting, so unget the extra 2 bytes
                rawPushed = true;

                if ((rawBufpos + 1) >= CHARBUF_SIZE)
                {
                    System.arraycopy(rawBytebuf, 2, rawBytebuf, 0, CHARBUF_SIZE - 2);
                    rawBufpos -= 2;
                }
                // make sure the bytes are pushed in the right order
                rawBytebuf[rawBufpos++] = (char) c2;
                rawBytebuf[rawBufpos++] = (char) c1;
                // drop through to code below, with the original char

            }
        }

        this.lookingForBOM = false;

        // A document in ISO-2022 based encoding uses some ESC sequences called "designator" to switch character sets.
        // The designators defined and used in ISO-2022-JP are: "ESC" + "(" + ? for ISO646 variants "ESC" + "$" + ? and
        // "ESC" + "$" + "(" + ? for multibyte character sets Where ? stands for a single character used to indicate the
        // character set for multibyte characters. Tidy handles this by preserving the escape sequence and setting the
        // top bit of each byte for non-ascii chars. This bit is then cleared on output. The input stream keeps track of
        // the state to determine when to set/clear the bit.

        if (this.encoding == Configuration.ISO2022)
        {
            if (c == 0x1b) // ESC
            {
                this.state = EncodingUtils.FSM_ESC;
                return c;
            }

            switch (this.state)
            {
                case EncodingUtils.FSM_ESC :
                    if (c == '$')
                    {
                        this.state = EncodingUtils.FSM_ESCD;
                    }
                    else if (c == '(')
                    {
                        this.state = EncodingUtils.FSM_ESCP;
                    }
                    else
                    {
                        this.state = EncodingUtils.FSM_ASCII;
                    }
                    break;

                case EncodingUtils.FSM_ESCD :
                    if (c == '(')
                    {
                        this.state = EncodingUtils.FSM_ESCDP;
                    }
                    else
                    {
                        this.state = EncodingUtils.FSM_NONASCII;
                    }
                    break;

                case EncodingUtils.FSM_ESCDP :
                    this.state = EncodingUtils.FSM_NONASCII;
                    break;

                case EncodingUtils.FSM_ESCP :
                    this.state = EncodingUtils.FSM_ASCII;
                    break;

                case EncodingUtils.FSM_NONASCII :
                    c |= 0x80;
                    break;

                default :
                    //
                    break;
            }

            return c;
        }

        if (this.encoding == Configuration.UTF16LE)
        {
            int c1;

            count[0] = 1;
            readRawBytesFromStream(tempchar, count, false);
            if (count[0] <= 0)
            {
                endOfStream = true;
                return END_OF_STREAM;
            }
            c1 = tempchar[0];

            n[0] = (c1 << 8) + c;

            return n[0];
        }

        // UTF-16 is big-endian by default
        if ((this.encoding == Configuration.UTF16) || (this.encoding == Configuration.UTF16BE))
        {
            int c1;

            count[0] = 1;
            readRawBytesFromStream(tempchar, count, false);
            if (count[0] <= 0)
            {
                endOfStream = true;
                return END_OF_STREAM;
            }
            c1 = tempchar[0];

            n[0] = (c << 8) + c1;

            return n[0];
        }

        if (this.encoding == Configuration.UTF8)
        {
            // deal with UTF-8 encoded char
            int[] count2 = new int[]{0};

            // first byte "c" is passed in separately
            boolean err = EncodingUtils.decodeUTF8BytesToChar(n, c, new byte[0], this.getBytes, count2, 0);
            if (!err && (n[0] == END_OF_STREAM) && (count2[0] == 1)) /* EOF */
            {
                endOfStream = true;
                return END_OF_STREAM;
            }
            else if (err)
            {
                /* set error position just before offending character */
                this.lexer.lines = this.curline;
                this.lexer.columns = this.curcol;

                this.lexer.report.encodingError(this.lexer, (short) (Report.INVALID_UTF8 | Report.REPLACED_CHAR), n[0]);
                n[0] = 0xFFFD; /* replacement char */
            }

            return n[0];
        }

        // #431953 - start RJ
        // This section is suitable for any "multibyte" variable-width character encoding in which a one-byte code is
        // less than 128, and the first byte of a two-byte code is greater or equal to 128. Note that Big5 and ShiftJIS
        // fit into this kind, even though their second byte may be less than 128

        if ((this.encoding == Configuration.BIG5) || (this.encoding == Configuration.SHIFTJIS))
        {
            if (c < 128)
            {
                return c;
            }
            else if ((this.encoding == Configuration.SHIFTJIS) && (c >= 0xa1 && c <= 0xdf))
            {
                // 461643 - fix suggested by Rick Cameron 14 Sep 01
                // for Shift_JIS, the values from 0xa1 through 0xdf represent singe-byte characters (U+FF61 to U+FF9F -
                // half-shift Katakana)
                return c;
            }
            else
            {
                int c1;
                count[0] = 1;
                readRawBytesFromStream(tempchar, count, false);

                if (count[0] <= 0)
                {
                    endOfStream = true;
                    return END_OF_STREAM;
                }

                c1 = tempchar[0];
                n[0] = (c << 8) + c1;
                return n[0];
            }
        }
        // #431953 - end RJ
        n[0] = c;

        return n[0];
    }

    /**
     * Read raw bytes from stream, return <= 0 if EOF; or if "unget" is true, Unget the bytes to re-synchronize the
     * input stream Normally UTF-8 successor bytes are read using this routine.
     * @param buf character buffer
     * @param count number of bytes to read
     * @param unget unget bytes
     */
    protected void readRawBytesFromStream(int[] buf, int[] count, boolean unget)
    {

        try
        {
            for (int i = 0; i < count[0]; i++)
            {
                if (unget)
                {

                    int c = this.stream.read();

                    // should never get here; testing for 0xFF, a valid char, is not a good idea
                    if (c == END_OF_STREAM) // || buf[i] == (unsigned char)EndOfStream
                    {
                        count[0] = -i;
                        return;
                    }

                    rawPushed = true;

                    if (rawBufpos >= CHARBUF_SIZE)
                    {
                        System.arraycopy(rawBytebuf, 1, rawBytebuf, 0, CHARBUF_SIZE - 1);
                        rawBufpos--;
                    }
                    rawBytebuf[rawBufpos++] = (char) buf[i];
                }
                else
                {
                    if (rawPushed)
                    {
                        buf[i] = rawBytebuf[--rawBufpos];
                        if (rawBufpos == 0)
                        {
                            rawPushed = false;
                        }
                    }
                    else
                    {
                        int c = this.stream.read();
                        if (c == END_OF_STREAM)
                        {
                            count[0] = -i;
                            break;
                        }
                        buf[i] = (char) c;
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("StreamInImpl.readRawBytesFromStream: " + e.toString());
        }
        return;
    }

}
