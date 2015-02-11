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
//
// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//
package com.globalsight.ling.common;

import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;

import java.util.Hashtable;

public class RtfEnDecoder
    extends NativeEnDecoder
{
    private static final Hashtable
        WINDOWS1252_UNICODE_MAP = mapWindows1252ToUnicode();
    private static final Hashtable
        WINDOWS1251_UNICODE_MAP = mapWindows1251ToUnicode();
    private static final Hashtable
        WINDOWS1256_UNICODE_MAP = mapWindows1256ToUnicode();
    private static final Hashtable
        WINDOWS1250_UNICODE_MAP = mapWindows1250ToUnicode();

    public String decode(String p_nativeString)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String decode(String p_nativeString, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * This method assumes a \\uc0 control is in effect, meaning a
     * Unicode control \\uNNNNN is followed by 0 ANSI characters.
     */
    public String encode(String p_nativeString)
        throws NativeEnDecoderException
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nativeString.length(); i < max; i++)
        {
            char ch = p_nativeString.charAt(i);
            short code = (short)ch;

            if (0x00 <= code && code < 0x10)
            {
                result.append("\\'0");
                result.append(Integer.toString(code, 16));
            }
            else if (code == 0x5c || code == 0x7b || code == 0x7d) // {,\,}
            {
                result.append("\\");
                result.append(ch);
            }
            else if (0x20 <= code && code < 0x80)
            {
                result.append(ch);
            }
            else // if (code > 0xff || code < 0)
            {
                result.append("\\u");
                result.append(code);
                result.append(" ");
            }
        }

        return result.toString();
    }

    public String encode(String p_nativeString, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * This method encodes the string plus checks the encoding.
     * Designed to be used for merging.
     */
    public String encodeWithEncodingCheck(String p_nativeString)
        throws NativeEnDecoderException
    {
        return encode(p_nativeString);
    }

    public String encodeWithEncodingCheck(String p_nativeString,
        String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encodeWithEncodingCheckForSkeleton(String p_NativeString)
        throws NativeEnDecoderException
    {
        return super.encodeWithEncodingCheckForSkeleton(p_NativeString);
    }

    /**
     * Converts Windows 1252 characters (0x80 - 0x9f) to Unicode,
     * which Java doesn't. Grrr.
     */
    public static Character hackWindows1252char(Character p_char)
    {
        Character c = (Character)WINDOWS1252_UNICODE_MAP.get(p_char);

        if (c == null)
        {
            c = p_char;
        }

        return c;
    }

    /**
     * Converts Windows 1251 characters (0x80 - 0x9f) to Unicode,
     * which Java doesn't. Grrr.
     */
    public static Character hackWindows1251char(Character p_char)
    {
        Character c = (Character)WINDOWS1251_UNICODE_MAP.get(p_char);

        if (c == null)
        {
            c = p_char;
        }

        return c;
    }

    /**
     * Converts Windows 1256 characters (0x80 - 0x9f) to Unicode,
     * which Java doesn't. Grrr.
     */
    public static Character hackWindows1256char(Character p_char)
    {
        Character c = (Character)WINDOWS1256_UNICODE_MAP.get(p_char);

        if (c == null)
        {
            c = p_char;
        }

        return c;
    }

    /**
     * Converts Windows 1250 characters (0x80 - 0xf9) to Unicode,
     * which Java doesn't. Grrr.
     */
    public static Character hackWindows1250char(Character p_char)
    {
        Character c = (Character)WINDOWS1250_UNICODE_MAP.get(p_char);

        if (c == null)
        {
            c = p_char;
        }

        return c;
    }    
    
    /**
     * Copied from HtmlEntities.java where this code indicates Java
     * really needs to get its conversion tables straight. Grrr.
     */
    private static Hashtable mapWindows1252ToUnicode()
    {
        Hashtable h = new Hashtable();
        // EURO SIGN
        h.put(new Character('\u0080'), new Character('\u20AC'));
        // 0x81            //UNDEFINED
        // SINGLE LOW-9 QUOTATION MARK
        h.put(new Character('\u0082'), new Character('\u201A'));
        // LATIN SMALL LETTER F WITH HOOK
        h.put(new Character('\u0083'), new Character('\u0192'));
        // DOUBLE LOW-9 QUOTATION MARK
        h.put(new Character('\u0084'), new Character('\u201E'));
        // HORIZONTAL ELLIPSIS
        h.put(new Character('\u0085'), new Character('\u2026'));
        // DAGGER
        h.put(new Character('\u0086'), new Character('\u2020'));
        // DOUBLE DAGGER
        h.put(new Character('\u0087'), new Character('\u2021'));
        // MODIFIER LETTER CIRCUMFLEX ACCENT
        h.put(new Character('\u0088'), new Character('\u02C6'));
        // PER MILLE SIGN
        h.put(new Character('\u0089'), new Character('\u2030'));
        // LATIN CAPITAL LETTER S WITH CARON
        h.put(new Character('\u008A'), new Character('\u0160'));
        // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        h.put(new Character('\u008B'), new Character('\u2039'));
        // LATIN CAPITAL LIGATURE OE
        h.put(new Character('\u008C'), new Character('\u0152'));
        // 0x8D            //UNDEFINED
        // LATIN CAPITAL LETTER Z WITH CARON
        h.put(new Character('\u008E'), new Character('\u017D'));
        // 0x8F            //UNDEFINED
        // 0x90            //UNDEFINED
        // LEFT SINGLE QUOTATION MARK
        h.put(new Character('\u0091'), new Character('\u2018'));
        // RIGHT SINGLE QUOTATION MARK
        h.put(new Character('\u0092'), new Character('\u2019'));
        // LEFT DOUBLE QUOTATION MARK
        h.put(new Character('\u0093'), new Character('\u201C'));
        // RIGHT DOUBLE QUOTATION MARK
        h.put(new Character('\u0094'), new Character('\u201D'));
        // BULLET
        h.put(new Character('\u0095'), new Character('\u2022'));
        // EN DASH
        h.put(new Character('\u0096'), new Character('\u2013'));
        // EM DASH
        h.put(new Character('\u0097'), new Character('\u2014'));
        // SMALL TILDE
        h.put(new Character('\u0098'), new Character('\u02DC'));
        // TRADE MARK SIGN
        h.put(new Character('\u0099'), new Character('\u2122'));
        // LATIN SMALL LETTER S WITH CARON
        h.put(new Character('\u009A'), new Character('\u0161'));
        // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        h.put(new Character('\u009B'), new Character('\u203A'));
        // LATIN SMALL LIGATURE OE
        h.put(new Character('\u009C'), new Character('\u0153'));
        // 0x9D            // UNDEFINED
        // LATIN SMALL LETTER Z WITH CARON
        h.put(new Character('\u009E'), new Character('\u017E'));
        // LATIN CAPITAL LETTER Y WITH DIAERESIS
        h.put(new Character('\u009F'), new Character('\u0178'));
        
        return h;
    }

    /**
     * Conversion table for Win1251 (Cyrillic). Java really needs to
     * get its conversion tables straight. Grrr.
     */
    private static Hashtable mapWindows1251ToUnicode()
    {
        Hashtable h = new Hashtable();
        // Cyrillic Capital Letter Dje
        h.put(new Character('\u0080'), new Character('\u0402'));
        // Cyrillic Capital Letter Gje
        h.put(new Character('\u0081'), new Character('\u0403'));
        // SINGLE LOW-9 QUOTATION MARK
        h.put(new Character('\u0082'), new Character('\u201A'));
        // Cyrillic Small Letter Gje
        h.put(new Character('\u0083'), new Character('\u0453'));
        // DOUBLE LOW-9 QUOTATION MARK
        h.put(new Character('\u0084'), new Character('\u201E'));
        // HORIZONTAL ELLIPSIS
        h.put(new Character('\u0085'), new Character('\u2026'));
        // DAGGER
        h.put(new Character('\u0086'), new Character('\u2020'));
        // DOUBLE DAGGER
        h.put(new Character('\u0087'), new Character('\u2021'));
        // Euro Sign
        h.put(new Character('\u0088'), new Character('\u20AC'));
        // PER MILLE SIGN
        h.put(new Character('\u0089'), new Character('\u2030'));
        // Cyrillic Capital Letter Lje
        h.put(new Character('\u008A'), new Character('\u0409'));
        // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        h.put(new Character('\u008B'), new Character('\u2039'));
        // Cyrillic Capital Letter Nje
        h.put(new Character('\u008C'), new Character('\u040A'));
        // Cyrillic Capital Letter Kje
        h.put(new Character('\u008D'), new Character('\u040C'));
        // Cyrillic Capital Letter Tshe
        h.put(new Character('\u008E'), new Character('\u040B'));
        // Cyrillic Capital Letter Dzhe
        h.put(new Character('\u008F'), new Character('\u040F'));
        // Cyrillic Small Letter Dje
        h.put(new Character('\u0090'), new Character('\u0452'));
        // LEFT SINGLE QUOTATION MARK
        h.put(new Character('\u0091'), new Character('\u2018'));
        // RIGHT SINGLE QUOTATION MARK
        h.put(new Character('\u0092'), new Character('\u2019'));
        // LEFT DOUBLE QUOTATION MARK
        h.put(new Character('\u0093'), new Character('\u201C'));
        // RIGHT DOUBLE QUOTATION MARK
        h.put(new Character('\u0094'), new Character('\u201D'));
        // BULLET
        h.put(new Character('\u0095'), new Character('\u2022'));
        // EN DASH
        h.put(new Character('\u0096'), new Character('\u2013'));
        // EM DASH
        h.put(new Character('\u0097'), new Character('\u2014'));
        // 0x98 unassigned
        // TRADE MARK SIGN
        h.put(new Character('\u0099'), new Character('\u2122'));
        // Cyrillic Small Letter Lje
        h.put(new Character('\u009A'), new Character('\u0459'));
        // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        h.put(new Character('\u009B'), new Character('\u203A'));
        // Cyrillic Small Letter Nje
        h.put(new Character('\u009C'), new Character('\u045A'));
        // Cyrillic Small Letter Kje
        h.put(new Character('\u009D'), new Character('\u045C'));
        // Cyrillic Small Letter Tshe
        h.put(new Character('\u009E'), new Character('\u045B'));
        // Cyrillic Small Letter Dzhe
        h.put(new Character('\u009F'), new Character('\u045F'));
        
        h.put(new Character('\u00A0'), new Character('\u00A0'));
        h.put(new Character('\u00A1'), new Character('\u040E'));
        h.put(new Character('\u00A2'), new Character('\u045E'));
        h.put(new Character('\u00A3'), new Character('\u0408'));
        h.put(new Character('\u00A4'), new Character('\u00A4'));
        h.put(new Character('\u00A5'), new Character('\u0490'));
        h.put(new Character('\u00A6'), new Character('\u00A6'));
        h.put(new Character('\u00A7'), new Character('\u00A7'));
        h.put(new Character('\u00A8'), new Character('\u0401'));
        h.put(new Character('\u00A9'), new Character('\u00A9'));
        h.put(new Character('\u00AA'), new Character('\u0404'));
        h.put(new Character('\u00AB'), new Character('\u00AB'));
        h.put(new Character('\u00AC'), new Character('\u00AC'));
        h.put(new Character('\u00AD'), new Character('\u00AD'));
        h.put(new Character('\u00AE'), new Character('\u00AE'));
        h.put(new Character('\u00AF'), new Character('\u0407'));
        
        h.put(new Character('\u00B0'), new Character('\u00B0'));
        h.put(new Character('\u00B1'), new Character('\u00B1'));
        h.put(new Character('\u00B2'), new Character('\u0406'));
        h.put(new Character('\u00B3'), new Character('\u0456'));
        h.put(new Character('\u00B4'), new Character('\u0491'));
        h.put(new Character('\u00B5'), new Character('\u00B5'));
        h.put(new Character('\u00B6'), new Character('\u00B6'));
        h.put(new Character('\u00B7'), new Character('\u00B7'));
        h.put(new Character('\u00B8'), new Character('\u0451'));
        h.put(new Character('\u00B9'), new Character('\u2116'));
        h.put(new Character('\u00BA'), new Character('\u0454'));
        h.put(new Character('\u00BB'), new Character('\u00BB'));
        h.put(new Character('\u00BC'), new Character('\u0458'));
        h.put(new Character('\u00BD'), new Character('\u0405'));
        h.put(new Character('\u00BE'), new Character('\u0455'));
        h.put(new Character('\u00BF'), new Character('\u0457'));
        
        h.put(new Character('\u00C0'), new Character('\u0410'));
        h.put(new Character('\u00C1'), new Character('\u0411'));
        h.put(new Character('\u00C2'), new Character('\u0412'));
        h.put(new Character('\u00C3'), new Character('\u0413'));
        h.put(new Character('\u00C4'), new Character('\u0414'));
        h.put(new Character('\u00C5'), new Character('\u0415'));
        h.put(new Character('\u00C6'), new Character('\u0416'));
        h.put(new Character('\u00C7'), new Character('\u0417'));
        h.put(new Character('\u00C8'), new Character('\u0418'));
        h.put(new Character('\u00C9'), new Character('\u0419'));
        h.put(new Character('\u00CA'), new Character('\u041A'));
        h.put(new Character('\u00CB'), new Character('\u041B'));
        h.put(new Character('\u00CC'), new Character('\u041C'));
        h.put(new Character('\u00CD'), new Character('\u041D'));
        h.put(new Character('\u00CE'), new Character('\u041E'));
        h.put(new Character('\u00CF'), new Character('\u041F'));
        
        h.put(new Character('\u00D0'), new Character('\u0420'));
        h.put(new Character('\u00D1'), new Character('\u0421'));
        h.put(new Character('\u00D2'), new Character('\u0422'));
        h.put(new Character('\u00D3'), new Character('\u0423'));
        h.put(new Character('\u00D4'), new Character('\u0424'));
        h.put(new Character('\u00D5'), new Character('\u0425'));
        h.put(new Character('\u00D6'), new Character('\u0426'));
        h.put(new Character('\u00D7'), new Character('\u0427'));
        h.put(new Character('\u00D8'), new Character('\u0428'));
        h.put(new Character('\u00D9'), new Character('\u0429'));
        h.put(new Character('\u00DA'), new Character('\u042A'));
        h.put(new Character('\u00DB'), new Character('\u042B'));
        h.put(new Character('\u00DC'), new Character('\u042C'));
        h.put(new Character('\u00DD'), new Character('\u042D'));
        h.put(new Character('\u00DE'), new Character('\u042E'));
        h.put(new Character('\u00DF'), new Character('\u042F'));
        
        h.put(new Character('\u00E0'), new Character('\u0430'));
        h.put(new Character('\u00E1'), new Character('\u0431'));
        h.put(new Character('\u00E2'), new Character('\u0432'));
        h.put(new Character('\u00E3'), new Character('\u0433'));
        h.put(new Character('\u00E4'), new Character('\u0434'));
        h.put(new Character('\u00E5'), new Character('\u0435'));
        h.put(new Character('\u00E6'), new Character('\u0436'));
        h.put(new Character('\u00E7'), new Character('\u0437'));
        h.put(new Character('\u00E8'), new Character('\u0438'));
        h.put(new Character('\u00E9'), new Character('\u0439'));
        h.put(new Character('\u00EA'), new Character('\u043A'));
        h.put(new Character('\u00EB'), new Character('\u043B'));
        h.put(new Character('\u00EC'), new Character('\u043C'));
        h.put(new Character('\u00ED'), new Character('\u043D'));
        h.put(new Character('\u00EE'), new Character('\u043E'));
        h.put(new Character('\u00EF'), new Character('\u043F'));
        
        h.put(new Character('\u00F0'), new Character('\u0440'));
        h.put(new Character('\u00F1'), new Character('\u0441'));
        h.put(new Character('\u00F2'), new Character('\u0442'));
        h.put(new Character('\u00F3'), new Character('\u0443'));
        h.put(new Character('\u00F4'), new Character('\u0444'));
        h.put(new Character('\u00F5'), new Character('\u0445'));
        h.put(new Character('\u00F6'), new Character('\u0446'));
        h.put(new Character('\u00F7'), new Character('\u0447'));
        h.put(new Character('\u00F8'), new Character('\u0448'));
        h.put(new Character('\u00F9'), new Character('\u0449'));
        h.put(new Character('\u00FA'), new Character('\u044A'));
        h.put(new Character('\u00FB'), new Character('\u044B'));
        h.put(new Character('\u00FC'), new Character('\u044C'));
        h.put(new Character('\u00FD'), new Character('\u044D'));
        h.put(new Character('\u00FE'), new Character('\u044E'));
        h.put(new Character('\u00FF'), new Character('\u044F'));
        
        return h;
    }

    /**
     * Conversion table for Win1256 (Arabic). Java really needs to
     * get its conversion tables straight. Grrr.
     */
    private static Hashtable mapWindows1256ToUnicode()
    {
        Hashtable h = new Hashtable();
        // EURO SIGN
        h.put(new Character('\u0080'), new Character('\u20AC'));
        // Arabic Letter Peh
        h.put(new Character('\u0081'), new Character('\u067E'));
        // SINGLE LOW-9 QUOTATION MARK
        h.put(new Character('\u0082'), new Character('\u201A'));
        // LATIN SMALL LETTER F WITH HOOK
        h.put(new Character('\u0083'), new Character('\u0192'));
        // DOUBLE LOW-9 QUOTATION MARK
        h.put(new Character('\u0084'), new Character('\u201E'));
        // HORIZONTAL ELLIPSIS
        h.put(new Character('\u0085'), new Character('\u2026'));
        // DAGGER
        h.put(new Character('\u0086'), new Character('\u2020'));
        // DOUBLE DAGGER
        h.put(new Character('\u0087'), new Character('\u2021'));
        // MODIFIER LETTER CIRCUMFLEX ACCENT
        h.put(new Character('\u0088'), new Character('\u02C6'));
        // PER MILLE SIGN
        h.put(new Character('\u0089'), new Character('\u2030'));
        // Arabic Letter Tteh
        h.put(new Character('\u008A'), new Character('\u0679'));
        // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        h.put(new Character('\u008B'), new Character('\u2039'));
        // LATIN CAPITAL LIGATURE OE
        h.put(new Character('\u008C'), new Character('\u0152'));
        // Arabic Letter Tcheh
        h.put(new Character('\u008D'), new Character('\u0686'));
        // Arabic Letter Jeh
        h.put(new Character('\u008E'), new Character('\u0698'));
        // Arabic Letter Ddal
        h.put(new Character('\u008F'), new Character('\u0688'));
        // Arabic Letter Gaf
        h.put(new Character('\u0090'), new Character('\u06AF'));
        // LEFT SINGLE QUOTATION MARK
        h.put(new Character('\u0091'), new Character('\u2018'));
        // RIGHT SINGLE QUOTATION MARK
        h.put(new Character('\u0092'), new Character('\u2019'));
        // LEFT DOUBLE QUOTATION MARK
        h.put(new Character('\u0093'), new Character('\u201C'));
        // RIGHT DOUBLE QUOTATION MARK
        h.put(new Character('\u0094'), new Character('\u201D'));
        // BULLET
        h.put(new Character('\u0095'), new Character('\u2022'));
        // EN DASH
        h.put(new Character('\u0096'), new Character('\u2013'));
        // EM DASH
        h.put(new Character('\u0097'), new Character('\u2014'));
        // Arabic Letter Keheh
        h.put(new Character('\u0098'), new Character('\u06A9'));
        // TRADE MARK SIGN
        h.put(new Character('\u0099'), new Character('\u2122'));
        // Arabic Letter Rreh
        h.put(new Character('\u009A'), new Character('\u0691'));
        // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        h.put(new Character('\u009B'), new Character('\u203A'));
        // LATIN SMALL LIGATURE OE
        h.put(new Character('\u009C'), new Character('\u0153'));
        // 0x9D            // UNDEFINED
        // 0x9E            // UNDEFINED
        // Arabic Letter Noon Ghunna
        h.put(new Character('\u009F'), new Character('\u06BA'));
        
        h.put(new Character('\u00A0'), new Character('\u00A0'));
        h.put(new Character('\u00A1'), new Character('\u060C'));
        h.put(new Character('\u00A2'), new Character('\u00A2'));
        h.put(new Character('\u00A3'), new Character('\u00A3'));
        h.put(new Character('\u00A4'), new Character('\u00A4'));
        h.put(new Character('\u00A5'), new Character('\u00A5'));
        h.put(new Character('\u00A6'), new Character('\u00A6'));
        h.put(new Character('\u00A7'), new Character('\u00A7'));
        h.put(new Character('\u00A8'), new Character('\u00A8'));
        h.put(new Character('\u00A9'), new Character('\u00A9'));
        h.put(new Character('\u00AA'), new Character('\u06BE'));
        h.put(new Character('\u00AB'), new Character('\u00AB'));
        h.put(new Character('\u00AC'), new Character('\u00AC'));
        h.put(new Character('\u00AD'), new Character('\u00AD'));
        h.put(new Character('\u00AE'), new Character('\u00AE'));
        h.put(new Character('\u00AF'), new Character('\u00AF'));
        
        
        h.put(new Character('\u00B0'), new Character('\u00B0'));
        h.put(new Character('\u00B1'), new Character('\u00B1'));
        h.put(new Character('\u00B2'), new Character('\u00B2'));
        h.put(new Character('\u00B3'), new Character('\u00B3'));
        h.put(new Character('\u00B4'), new Character('\u00B4'));
        h.put(new Character('\u00B5'), new Character('\u00B5'));
        h.put(new Character('\u00B6'), new Character('\u00B6'));
        h.put(new Character('\u00B7'), new Character('\u00B7'));
        h.put(new Character('\u00B8'), new Character('\u00B8'));
        h.put(new Character('\u00B9'), new Character('\u00B9'));
        h.put(new Character('\u00BA'), new Character('\u061B'));
        h.put(new Character('\u00BB'), new Character('\u00BB'));
        h.put(new Character('\u00BC'), new Character('\u00BC'));
        h.put(new Character('\u00BD'), new Character('\u00BD'));
        h.put(new Character('\u00BE'), new Character('\u00BE'));
        h.put(new Character('\u00BF'), new Character('\u061F'));
        
        h.put(new Character('\u00C0'), new Character('\u06C1'));
        h.put(new Character('\u00C1'), new Character('\u0621'));
        h.put(new Character('\u00C2'), new Character('\u0622'));
        h.put(new Character('\u00C3'), new Character('\u0623'));
        h.put(new Character('\u00C4'), new Character('\u0624'));
        h.put(new Character('\u00C5'), new Character('\u0625'));
        h.put(new Character('\u00C6'), new Character('\u0626'));
        h.put(new Character('\u00C7'), new Character('\u0627'));
        h.put(new Character('\u00C8'), new Character('\u0628'));
        h.put(new Character('\u00C9'), new Character('\u0629'));
        h.put(new Character('\u00CA'), new Character('\u062A'));
        h.put(new Character('\u00CB'), new Character('\u062B'));
        h.put(new Character('\u00CC'), new Character('\u062C'));
        h.put(new Character('\u00CD'), new Character('\u062D'));
        h.put(new Character('\u00CE'), new Character('\u062E'));
        h.put(new Character('\u00CF'), new Character('\u045F'));
        
        h.put(new Character('\u00D0'), new Character('\u0630'));
        h.put(new Character('\u00D1'), new Character('\u0631'));
        h.put(new Character('\u00D2'), new Character('\u0632'));
        h.put(new Character('\u00D3'), new Character('\u0633'));
        h.put(new Character('\u00D4'), new Character('\u0634'));
        h.put(new Character('\u00D5'), new Character('\u0635'));
        h.put(new Character('\u00D6'), new Character('\u0636'));
        h.put(new Character('\u00D7'), new Character('\u00D7'));
        h.put(new Character('\u00D8'), new Character('\u0637'));
        h.put(new Character('\u00D9'), new Character('\u0638'));
        h.put(new Character('\u00DA'), new Character('\u0639'));
        h.put(new Character('\u00DB'), new Character('\u063A'));
        h.put(new Character('\u00DC'), new Character('\u0640'));
        h.put(new Character('\u00DD'), new Character('\u0641'));
        h.put(new Character('\u00DE'), new Character('\u0642'));
        h.put(new Character('\u00DF'), new Character('\u0443'));
        
        h.put(new Character('\u00E0'), new Character('\u00E0'));
        h.put(new Character('\u00E1'), new Character('\u0644'));
        h.put(new Character('\u00E2'), new Character('\u00E2'));
        h.put(new Character('\u00E3'), new Character('\u0645'));
        h.put(new Character('\u00E4'), new Character('\u0646'));
        h.put(new Character('\u00E5'), new Character('\u0647'));
        h.put(new Character('\u00E6'), new Character('\u0648'));
        h.put(new Character('\u00E7'), new Character('\u00E7'));
        h.put(new Character('\u00E8'), new Character('\u00E8'));
        h.put(new Character('\u00E9'), new Character('\u00E9'));
        h.put(new Character('\u00EA'), new Character('\u00EA'));
        h.put(new Character('\u00EB'), new Character('\u00EB'));
        h.put(new Character('\u00EC'), new Character('\u0649'));
        h.put(new Character('\u00ED'), new Character('\u064A'));
        h.put(new Character('\u00EE'), new Character('\u00EE'));
        h.put(new Character('\u00EF'), new Character('\u00EF'));
        
        h.put(new Character('\u00F0'), new Character('\u064B'));
        h.put(new Character('\u00F1'), new Character('\u064C'));
        h.put(new Character('\u00F2'), new Character('\u064D'));
        h.put(new Character('\u00F3'), new Character('\u064E'));
        h.put(new Character('\u00F4'), new Character('\u00F4'));
        h.put(new Character('\u00F5'), new Character('\u064F'));
        h.put(new Character('\u00F6'), new Character('\u0650'));
        h.put(new Character('\u00F7'), new Character('\u00F7'));
        h.put(new Character('\u00F8'), new Character('\u0651'));
        h.put(new Character('\u00F9'), new Character('\u00F9'));
        h.put(new Character('\u00FA'), new Character('\u0652'));
        h.put(new Character('\u00FB'), new Character('\u00FB'));
        h.put(new Character('\u00FC'), new Character('\u06FC'));
        h.put(new Character('\u00FD'), new Character('\u200E'));
        h.put(new Character('\u00FE'), new Character('\u200F'));
        h.put(new Character('\u00FF'), new Character('\u06D2'));
        
        return h;
    }
    
    /**
     * Conversion table for Win1250 (Arabic). Java really needs to
     * get its conversion tables straight. Grrr.
     */
    private static Hashtable mapWindows1250ToUnicode()
    {
        Hashtable h = new Hashtable();

        h.put(new Character('\u0080'), new Character('\u20AC'));
        // h.put(new Character('\u0081'), new Character('\u067E'));
        h.put(new Character('\u0082'), new Character('\u201A'));
        // h.put(new Character('\u0083'), new Character('\u0192'));
        h.put(new Character('\u0084'), new Character('\u201E'));
        h.put(new Character('\u0085'), new Character('\u2026'));
        h.put(new Character('\u0086'), new Character('\u2020'));
        h.put(new Character('\u0087'), new Character('\u2021'));
        // h.put(new Character('\u0088'), new Character('\u02C6'));
        h.put(new Character('\u0089'), new Character('\u2030'));
        h.put(new Character('\u008A'), new Character('\u0160'));
        h.put(new Character('\u008B'), new Character('\u2039'));
        h.put(new Character('\u008C'), new Character('\u015A'));
        h.put(new Character('\u008D'), new Character('\u0164'));
        h.put(new Character('\u008E'), new Character('\u017D'));
        h.put(new Character('\u008F'), new Character('\u0179'));

        // h.put(new Character('\u0090'), new Character('\u06AF'));
        h.put(new Character('\u0091'), new Character('\u2018'));
        h.put(new Character('\u0092'), new Character('\u2019'));
        h.put(new Character('\u0093'), new Character('\u201C'));
        h.put(new Character('\u0094'), new Character('\u201D'));
        h.put(new Character('\u0095'), new Character('\u2022'));
        h.put(new Character('\u0096'), new Character('\u2013'));
        h.put(new Character('\u0097'), new Character('\u2014'));
        // h.put(new Character('\u0098'), new Character('\u06A9'));
        h.put(new Character('\u0099'), new Character('\u2122'));
        h.put(new Character('\u009A'), new Character('\u0661'));
        h.put(new Character('\u009B'), new Character('\u203A'));
        h.put(new Character('\u009C'), new Character('\u015B'));
        h.put(new Character('\u009D'), new Character('\u0165'));
        h.put(new Character('\u009F'), new Character('\u017A'));
        
        h.put(new Character('\u00A0'), new Character('\u00A0'));
        h.put(new Character('\u00A1'), new Character('\u02C7'));
        h.put(new Character('\u00A2'), new Character('\u02D8'));
        h.put(new Character('\u00A3'), new Character('\u0141'));
        h.put(new Character('\u00A4'), new Character('\u00A4'));
        h.put(new Character('\u00A5'), new Character('\u0104'));
        h.put(new Character('\u00A6'), new Character('\u00A6'));
        h.put(new Character('\u00A7'), new Character('\u00A7'));
        h.put(new Character('\u00A8'), new Character('\u00A8'));
        h.put(new Character('\u00A9'), new Character('\u00A9'));
        h.put(new Character('\u00AA'), new Character('\u015E'));
        h.put(new Character('\u00AB'), new Character('\u00AB'));
        h.put(new Character('\u00AC'), new Character('\u00AC'));
        h.put(new Character('\u00AD'), new Character('\u00AD'));
        h.put(new Character('\u00AE'), new Character('\u00AE'));
        h.put(new Character('\u00AF'), new Character('\u017B'));
        
        h.put(new Character('\u00B0'), new Character('\u00B0'));
        h.put(new Character('\u00B1'), new Character('\u00B1'));
        h.put(new Character('\u00B2'), new Character('\u02DB'));
        h.put(new Character('\u00B3'), new Character('\u0142'));
        h.put(new Character('\u00B4'), new Character('\u00B4'));
        h.put(new Character('\u00B5'), new Character('\u00B5'));
        h.put(new Character('\u00B6'), new Character('\u00B6'));
        h.put(new Character('\u00B7'), new Character('\u00B7'));
        h.put(new Character('\u00B8'), new Character('\u00B8'));
        h.put(new Character('\u00B9'), new Character('\u0105'));
        h.put(new Character('\u00BA'), new Character('\u015F'));
        h.put(new Character('\u00BB'), new Character('\u00BB'));
        h.put(new Character('\u00BC'), new Character('\u013D'));
        h.put(new Character('\u00BD'), new Character('\u02DD'));
        h.put(new Character('\u00BE'), new Character('\u013E'));
        h.put(new Character('\u00BF'), new Character('\u017C'));
        
        h.put(new Character('\u00C0'), new Character('\u0154'));
        h.put(new Character('\u00C1'), new Character('\u00C1'));
        h.put(new Character('\u00C2'), new Character('\u02C2'));
        h.put(new Character('\u00C3'), new Character('\u0102'));
        h.put(new Character('\u00C4'), new Character('\u00C4'));
        h.put(new Character('\u00C5'), new Character('\u0139'));
        h.put(new Character('\u00C6'), new Character('\u0106'));
        h.put(new Character('\u00C7'), new Character('\u00C7'));
        h.put(new Character('\u00C8'), new Character('\u010C'));
        h.put(new Character('\u00C9'), new Character('\u00C9'));
        h.put(new Character('\u00CA'), new Character('\u0118'));
        h.put(new Character('\u00CB'), new Character('\u00CB'));
        h.put(new Character('\u00CC'), new Character('\u011A'));
        h.put(new Character('\u00CD'), new Character('\u00CD'));
        h.put(new Character('\u00CE'), new Character('\u00CE'));
        h.put(new Character('\u00CF'), new Character('\u010E'));
        
        h.put(new Character('\u00D0'), new Character('\u0110'));
        h.put(new Character('\u00D1'), new Character('\u0148'));
        h.put(new Character('\u00D2'), new Character('\u0147'));
        h.put(new Character('\u00D3'), new Character('\u00D3'));
        h.put(new Character('\u00D4'), new Character('\u00D4'));
        h.put(new Character('\u00D5'), new Character('\u0150'));
        h.put(new Character('\u00D6'), new Character('\u00D6'));
        h.put(new Character('\u00D7'), new Character('\u00D7'));
        h.put(new Character('\u00D8'), new Character('\u0158'));
        h.put(new Character('\u00D9'), new Character('\u016E'));
        h.put(new Character('\u00DA'), new Character('\u00DA'));
        h.put(new Character('\u00DB'), new Character('\u0170'));
        h.put(new Character('\u00DC'), new Character('\u00DC'));
        h.put(new Character('\u00DD'), new Character('\u00DD'));
        h.put(new Character('\u00DE'), new Character('\u0162'));
        h.put(new Character('\u00DF'), new Character('\u00DF'));
        
        h.put(new Character('\u00E0'), new Character('\u0155'));
        h.put(new Character('\u00E1'), new Character('\u00E1'));
        h.put(new Character('\u00E2'), new Character('\u00E2'));
        h.put(new Character('\u00E3'), new Character('\u00E3'));
        h.put(new Character('\u00E4'), new Character('\u00E4'));
        h.put(new Character('\u00E5'), new Character('\u013A'));
        h.put(new Character('\u00E6'), new Character('\u0107'));
        h.put(new Character('\u00E7'), new Character('\u00E7'));
        h.put(new Character('\u00E8'), new Character('\u010D'));
        h.put(new Character('\u00E9'), new Character('\u00E9'));
        h.put(new Character('\u00EA'), new Character('\u0119'));
        h.put(new Character('\u00EB'), new Character('\u00EB'));
        h.put(new Character('\u00EC'), new Character('\u011B'));
        h.put(new Character('\u00ED'), new Character('\u00ED'));
        h.put(new Character('\u00EE'), new Character('\u01EE'));
        h.put(new Character('\u00EF'), new Character('\u010F'));
        
        h.put(new Character('\u00F0'), new Character('\u0111'));
        h.put(new Character('\u00F1'), new Character('\u0144'));
        h.put(new Character('\u00F2'), new Character('\u0148'));
        h.put(new Character('\u00F3'), new Character('\u00F3'));
        h.put(new Character('\u00F4'), new Character('\u00F4'));
        h.put(new Character('\u00F5'), new Character('\u0151'));
        h.put(new Character('\u00F6'), new Character('\u00F6'));
        h.put(new Character('\u00F7'), new Character('\u00F7'));
        h.put(new Character('\u00F8'), new Character('\u0159'));
        h.put(new Character('\u00F9'), new Character('\u016F'));
        h.put(new Character('\u00FA'), new Character('\u00FA'));
        h.put(new Character('\u00FB'), new Character('\u0171'));
        h.put(new Character('\u00FC'), new Character('\u00FC'));
        h.put(new Character('\u00FD'), new Character('\u00FD'));
        h.put(new Character('\u00FE'), new Character('\u0063'));
        h.put(new Character('\u00FF'), new Character('\u02D9'));
        
        return h;
    }
}
