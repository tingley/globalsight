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



package com.globalsight.everest.edit.offline.upload;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.ling.common.CodesetMapper;


/**
 * Read in a format two file as a byte stream and try to determine
 * it's codeset.  We first look for a BOM. If we find one we assume
 * the encoding is UTF-16.  If not, we scan down to the encoding field
 * in the header and use that to create the Reader.
*/
public class FormatTwoEncodingSniffer
    implements AmbassadorDwUpConstants, AmbassadorDwUpExceptionConstants
{
    static private final int UTF8     = 1;
    static private final int UTF16_LE = 2;
    static private final int UTF16_BE = 3;
    static private final int UTF32_LE = 4;
    static private final int UTF32_BE = 5;
    static private final int UNKNOWN  = 6;

    private BufferedInputStream m_input = null;
    private BufferedReader m_reader = null;

    /**
     * FormatTwoEncodingSniffer constructor comment.
     */
    public FormatTwoEncodingSniffer()
    {
        super();
    }

    /**
     * Sets up buffered input stream.
     */
    private void loadFile(InputStream p_inputStream)
    {
        m_input = new BufferedInputStream(p_inputStream);
        m_input.mark(0);
    }

    /**
     * Reads and identifies what should be the BOM from the file.
     * @return int a contant identifying the BOM
     */
    private int identifyBom()
        throws AmbassadorDwUpException
    {
        int byte1 = 0;
        int byte2 = 0;
        int byte3 = 0;
        int byte4 = 0;

        try
        {
            byte1 = m_input.read();
            byte2 = m_input.read();
            byte3 = m_input.read();
            byte4 = m_input.read();

            m_input.reset();
        }
        catch (IOException ex)
        {
            throw new AmbassadorDwUpException(READ_FATAL_ERROR, ex);
        }

        if (byte1 == -1 || byte2 == -1 || byte3 == -1 || byte4 == -1)
        {
            // too short for upload file
            throw new AmbassadorDwUpException("PrematureEofFound", null, null);
        }

        if (byte1 == 0x00 && byte2 == 0x00 && byte3 == 0xFE && byte4 == 0xFF)
        {
            return UTF32_BE;
        }
        else if (byte1 == 0xFF && byte2 == 0xFE && byte3 == 0x00 && byte4 == 0x00)
        {
            return UTF32_LE;
        }
        else if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF)
        {
            return UTF8;
        }
        else if (byte1 == 0xFE && byte2 == 0xFF)
        {
            return UTF16_BE;
        }
        else if (byte1 == 0xFF && byte2 == 0xFE)
        {
            return UTF16_LE;
        }

        return UNKNOWN;
    }

    /**
     * Attempts to read encoding value from the header.
     * @return String the Java encoding
     */
    private String getEncodingFromHeader()
        throws AmbassadorDwUpException
    {
        String encoding = null;

        // find the encoding
        try
        {
            // read the signature
            byte[] bytes = new byte[SIGNATURE.length()];
            int len = m_input.read(bytes, 0, SIGNATURE.length());
            if (len != SIGNATURE.length())
            {
                throw new AmbassadorDwUpException("PrematureEofFound",
                    null, null);
            }
            
            // start with "<?xml " or not
            if (bytes[0] == 60 && bytes[1] == 0 && bytes[2] == 63
                    && bytes[3] == 0 && bytes[4] == 120 && bytes[5] == 0
                    && bytes[6] == 109 && bytes[7] == 0 && bytes[8] == 108
                    && bytes[9] == 0 && bytes[10] == 32 && bytes[11] == 0)
            {
                encoding = "UTF-16LE";
            }
            else
            {
                skipNewlines();

                bytes = new byte[HEADER_ENCODING_KEY.length()];
                len = m_input.read(bytes, 0, HEADER_ENCODING_KEY.length());
                if (len != HEADER_ENCODING_KEY.length())
                {
                    throw new AmbassadorDwUpException("PrematureEofFound",
                            null, null);
                }

                encoding = readEncoding();
            }
            
            m_input.reset();
        }
        catch (IOException ex)
        {
            throw new AmbassadorDwUpException(READ_FATAL_ERROR, ex);
        }

        // should be an IANA name - convert it to a legal Java encoding
        String javaEncoding = CodesetMapper.getJavaEncoding(encoding.trim());
        if (javaEncoding == null)
        {
            throw new AmbassadorDwUpException(READ_FATAL_ERROR,
                "Java Encoding not found for: " + encoding);
        }

        return javaEncoding;
    }

    /**
     * Determines the encoding of the upload file and returns a reader
     * using the appropriate encoding.
     *
     * The method first tries to read the Byte Order Mark to determine
     * if the file is UnicodeBig or UnicodeLittle.  If it is neither,
     * it attempts to read into the file to get the encoding value
     * from the header.
     *
     * @return BufferedReader
     * @param p_inputStream the input stream passed in from the servlet.
     */
    public InputStreamReader convertFileToUnicode(InputStream p_inputStream)
        throws AmbassadorDwUpException
    {
        loadFile(p_inputStream);

        switch (identifyBom())
        {
        case UTF16_LE:
            return makeReader("UnicodeLittle");
        case UTF16_BE:
            return makeReader("UnicodeBig");
        case UTF8:
            return makeReader("UTF8");
        case UTF32_LE:
            throw new AmbassadorDwUpException(READ_FATAL_ERROR,
                "Unsupported Java Encoding (UTF32 Little Endian)");
        case UTF32_BE:
            throw new AmbassadorDwUpException(READ_FATAL_ERROR,
                "Unsupported Java Encoding (UTF32 Big Endian)");
        default:
        {
            String encoding = getEncodingFromHeader();

            if (encoding == null)
            {
                return null;
            }

            return makeReader(encoding);
        }
        }
    }

    /**
     * Create a reader with the given Java encoding.
     * @param p_encoding a valid Java encoding.
     */
    private InputStreamReader makeReader(String p_encoding)
        throws AmbassadorDwUpException
    {
        InputStreamReader streamReader = null;

        try
        {
            streamReader = new InputStreamReader(m_input, p_encoding);
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new AmbassadorDwUpException(READ_FATAL_ERROR, ex);
        }

        return streamReader;
    }

    // NOTE: assumes that we are positioned to read the value.
    private String readEncoding()
        throws AmbassadorDwUpException, IOException
    {
        String encoding = "";
        boolean eol = false;
        int c;
        int cnt = 0;

        // Read up to the first newline (or 20 characters).

        // NOTE: The EncodingSniffer is used during the file detection
        // process.  So we cannot simply read till we find a new line.
        // We may actually be in the process of trying to detect the
        // file format. At this point we would be trying to detect a
        // plain text upload but in fact we may be working with an
        // unextracted file. So here we restrict the search to 20
        // characters. The encoding value is less than this.
        while ((c = m_input.read()) != -1 && cnt < 20)
        {
            if ((char)c == '\n' || (char)c == '\r')
            {
                eol = true;
                break;
            }

            encoding += (char)c;
            cnt++;
        }

        if (!eol)
        {
            // probably not our plain text format or it got corrupted
            throw new AmbassadorDwUpException("PrematureEofFound", null, null);
        }

        return encoding;
    }

    /**
     * Helper function to aid parsing the header.
     * @exception java.io.IOException The exception description.
     */
    private void skipNewlines()
        throws AmbassadorDwUpException, IOException
    {
        int c;
        boolean eol = true;

        // skip past the newlines
        while ((c = m_input.read()) != -1)
        {
            if ((char)c != '\n' && (char)c != '\r')
            {
                // you have to understand that this character is thrown away!
                eol = false;
                break;
            }
        }

        if (eol)
        {
            // probably not the correct file or it got corrupted
            throw new AmbassadorDwUpException("PrematureEofFound", null, null);
        }
    }
}
