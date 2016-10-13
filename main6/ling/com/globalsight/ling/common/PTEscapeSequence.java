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

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;

/**
 * Responsible for encoding and decoding escape sequences in plain text.
 */
public class PTEscapeSequence
    extends NativeEnDecoder
{
    /*
     * Converts standard escape sequences to their native forms.
     */
    public String decode (String p_str)
        throws NativeEnDecoderException
    {
        /* FOR NOW NOTHING IS CONVERTED FOR THE USER FOR PLAIN TEXT
           IF YOU CHANGE THIS ALSO CHANGE ENCODE.

           char aChar;
           Character c;

           int len = p_str.length();
           StringBuffer result = new StringBuffer(len);


           for(int x=0; x<len; )
           {
           aChar = p_str.charAt(x++);
           if (aChar == '\\')
           {
           aChar = p_str.charAt(x++);
           if (aChar == 't') aChar = '\t';
           else if (aChar == 'r') aChar = '\r';
           else if (aChar == 'n') aChar = '\n';
           else if (aChar == 'f') aChar = '\f';
           result.append(aChar);
           }
           else
           result.append(aChar);
           }

           return result.toString();
        */

        return p_str;

    }

    public String decode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * Converts control characters to character escapes.
     * Note: The generic Merger should have removed all TMX and XML escapes.
     */
    public String encode(String p_str)
    // throws NativeEnDecoderException
    {
        /* FOR NOW NOTHING IS CONVERTED FOR THE USER FOR PLAIN TEXT
           IF YOU CHANGE THIS ALSO CHANGE DECODE.

           char aChar;
           int len = p_str.length();
           StringBuffer result = new StringBuffer(len*2);

           for(int x=0; x<len; )
           {
           aChar = p_str.charAt(x++);
           switch(aChar)
           {

           case '\t':result.append('\\'); result.append('t');
           continue;
           case '\n':result.append('\\'); result.append('n');
           continue;
           case '\r':result.append('\\'); result.append('r');
           continue;
           case '\f':result.append('\\'); result.append('f');
           continue;

           default:
           result.append(aChar);
           break;
           }
           }
           return result.toString();
        */

        return p_str;
    }

    public String encode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encodeWithEncodingCheck(String p_NativeString)
        throws NativeEnDecoderException
    {
        StringBuffer sbuf = new StringBuffer(p_NativeString);

        for (int i = 0; i < sbuf.length(); i++)
        {
            char c = sbuf.charAt(i);
            if (!encChecker.canConvert(c))
            {
                sbuf.setCharAt(i, checkEucJpOddity(c));
            }
        }

        return sbuf.toString();
    }

    public String encodeWithEncodingCheckForSkeleton(String p_NativeString)
        throws NativeEnDecoderException
    {
        return encodeWithEncodingCheck(p_NativeString);
    }

    public String encodeWithEncodingCheck(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * Checks if the target encoding is EUC-JP and the following
     * characters are used. If so, converts them to characters that
     * can be converted to EUC-JP. This hack may apply only for JDK
     * 1.3. The successor JDK may fix the "problem".
     *
     * PARALLEL TO            U+2225
     * FULLWIDTH HYPHEN-MINUS U+ff0d
     * FULLWIDTH CENT SIGN    U+ffe0
     * FULLWIDTH POUND SIGN   U+ffe1
     * FULLWIDTH NOT SIGN     U+ffe2
     */
    private char checkEucJpOddity(char c)
        throws NativeEnDecoderException
    {
        char ret = '\uffff'; // non character

        String encoding = encChecker.getEncoding().toLowerCase();

        if (encoding.startsWith("euc") && encoding.endsWith("jp"))
        {
            if (c == '\u2225')      // PARALLEL TO
            {
                ret = '\u2016';     // DOUBLE VERTICAL LINE
            }
            else if (c == '\uff0d') // FULLWIDTH HYPHEN-MINUS
            {
                ret = '\u2212';     // MINUS SIGN
            }
            else if (c == '\uffe0') // FULLWIDTH CENT SIGN
            {
                ret = '\u00a2';     // CENT SIGN
            }
            else if (c == '\uffe1') // FULLWIDTH POUND SIGN
            {
                ret = '\u00a3';     // POUND SIGN
            }
            else if (c == '\uffe2') // FULLWIDTH NOT SIGN
            {
                ret = '\u00ac';     // NOT SIGN
            }
        }

        if (ret == '\uffff')
        {
            throw new NativeEnDecoderException(
                "Illegal character: U+" + Integer.toHexString(c) +
                " for " + encoding);
        }

        return ret;
    }
}
