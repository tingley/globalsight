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

import java.util.*;

/**
 * A class to encode/decode HTML character entities or
 * numeric character references. The current list of HTML character
 * entities can be found at <a
 * href="http://www.w3.org/TR/html4/sgml/entities.html">http://www.w3.org/TR/html4/sgml/entities.html</a>. XML/HTML
 * numeric character references take two forms as below.<br><br>
 * <tt>'&amp;#' [0-9]+ ';'</tt>  # decimal representation<br>
 * <tt>'&amp;#x' [0-9a-fA-F]+ ';'</tt>  # hex representation<br>
 */
public class HtmlEntities
    extends Entities
{
    public static final HashMap mHtmlCharToEntity = mapHtmlCharToEntity();
    public static final HashMap mHtmlEntityToChar = mapHtmlEntityToChar();
    public static final HashMap
        mDefaultCharToEntity = mapHtmlDefaultCharToEntity();
    public static final HashMap
        mDefaultEntityToChar = mapHtmlDefaultEntityToChar();
    private static final HashMap
        WINDOWS1252_UNICODE_MAP = mapWindows1252ToUnicode();
    private boolean m_useDefaultHtmlEncoderChar = true;


    /**
     * a list of characters that are always encoded to character
     * entities by <tt>encodeString()</tt> method. The characters
     * contained in the list are: <ul><li>&lt; <li>&gt; <li>&amp;
     * <li>&quot; </ul>
     */
    protected static final char[] m_defaultHtmlEncodeChar =
        {'<', '>', '&', '"'};

    /**
     * Constructor
     */
    public HtmlEntities()
    {
        super();
    }

    /**
     * Decode a HTML character entity or a numeric character reference
     * into a character
     * @param ent a HTML character entity or a XML/HTML numeric
     * character reference
     * @return decoded character. If <tt>ent</tt> is mal formed or
     * cannot be found in the character entity list, the method
     * returns U+FFFF, non-existing code in Unicode.
     */
    final public char decode(String ent)
    {
        Character c = (Character)mDefaultEntityToChar.get(ent);
        if (c == null)
        {
            c = (Character)mHtmlEntityToChar.get(ent);
        }

        if (c == null)
        {
            if (ent.length() > 3) // numeric ref must be more than 3 chars
            {
                char x = ent.charAt(2);
                if (x == 'x' || x == 'X')
                {
                    c = decodeNumRef(ent, "&#x", ";", 16, false);
                }
                else
                {
                    c = decodeNumRef(ent, "&#", ";", 10, false);
                }

                if (c == null)
                {
                    c = new Character('\uffff');
                }
                else
                {
                    c = hackWindows1252Ncr(c);
                }
            }
            else
            {
                c = new Character('\uffff');
            }
        }

        return c.charValue();
    }

    /**
     * Decode only a basic character entity into a character
     * @param ent a character entity or a numeric character reference
     * @return decoded character. If <tt>ent</tt> is mal formed or
     * cannot be found in the character entity list, the method
     * returns U+FFFF, non-existing code in Unicode.
     */
    final public char decodeBasicEntity(String ent)
    {
        char c = '\uffff';
        Character ch = (Character)mDefaultEntityToChar.get(ent);

        if (ch != null)
        {
            c = ch.charValue();
        }

        return c;
    }

    /**
     * Decode characters in the given string. The character entities
     * included in <tt>decodeExcludeList</tt> will not be decoded. All
     * numeric character references will be decoded.
     * @param s a string to be decoded
     * @param decodeExcludeList a list of character entities that will
     * not be decoded
     * @return decoded string
     */
    final public String decodeString(String s, String[] decodeExcludeList)
    {
        if (s.indexOf('&') >= 0)
        {
            // hack: handle "&nbsp" (missing semi-colon)
            return decodeString(s, decodeExcludeList,
                "(&[^;]+;)|(&nbsp)", false);
        }

        return s;
    }

    /**
     * Decode only basic entities in the given string. No numeric
     * character references are decoded.
     * @param s a string to be decoded
     * @return decoded string
     */
    final public String decodeStringBasic(String s)
    {
        if (s.indexOf('&') >= 0)
        {
            // hack: handle "&nbsp" (missing semi-colon)
            return decodeString(s, null, "(&[^;]+;)|(&nbsp)", true);
        }

        return s;
    }

    /**
     * Encode a character to a HTML character entity or a numeric
     * character reference
     * @param c a character to be encoded
     * @param entity <tt>c</tt> will be encoded as a HTML character
     * entity if <tt>entity</tt> is <tt>true</tt>, otherwise
     * <tt>c</tt> will be a numeric character reference. If
     * <tt>entity</tt> is true and the corresponding entity can not be
     * found, the function returns a numeric character reference. The
     * Fundamental four characters (&lt;, &gt;, &amp; and &quot;) are
     * always converted to the corresponding entities even if
     * <tt>entity</tt> is <tt>false</tt>.
     * @return the result of encode. If it's a numeric character
     * reference, it takes hex form.
     */
    final public String encode(char c, boolean entity)
    {
        Character cc = new Character(c);

        String s = (String) mDefaultCharToEntity.get(cc);

        if (s == null && entity)
        {
            s = (String) mHtmlCharToEntity.get(cc);
        }

        if (s == null)
        {
            s = encodeNumRef(c, "&#x", ";", 16);
        }

        return s;
    }

    /**
     * Encode characters in the given string. Only characters given in
     * <tt>encodeCarList</tt> will be encoded. Characters will be
     * encoded as HTML chracter entities. Numeric character references
     * are used only when the character entity is not defined for the
     * character. The characters in the default list,
     * <tt>m_defaultHtmlEncodeChar</tt>, will be always encoded.
     * @param s a string to be encoded
     * @param encodeCharList a list of characters which will be
     * encoded . Even if <tt>encodeCharList</tt> is null, the
     * characters in the default list,
     * <tt>m_defaultHtmlEncodeChar</tt>, will be encoded.
     * @return encoded string
     */
    final public String encodeString(String s, char[] encodeCharList)
    {
        return super.encodeString(s, encodeCharList);
    }

    /**
     * Encode characters in the given string. Only a range of
     * characters given by <tt>first</tt> and <tt>last</tt> will be
     * encoded. Characters will be encoded as HTML chracter
     * entities. Numeric character references are used only when the
     * character entity is not defined for the character. The
     * characters in the default list,
     * <tt>m_defaultHtmlEncodeChar</tt>, will be always encoded even if
     * they are not in the range of <tt>first</tt> and <tt>last</tt>.
     * @param s a string to be encoded
     * @param first a first character to be included in the range of
     * characters to be encoded. If <tt>first</tt> is greater than
     * <tt>last</tt>, no any characters will be encoded.
     * @param last a last character to be included in the range of
     * characters to be encoded. If <tt>last</tt> is less than
     * <tt>first</tt>, no any characters will be encoded.
     * @return encoded string
     */
    final public String encodeString(String s, char first, char last)
    {
        return super.encodeString(s, first, last);
    }

    /**
     * Encode basic character entities in the given string.
     * @param s a string to be encoded
     * @return encoded string
     */
    final public String encodeStringBasic(String s)
    {
        return encodeString(s, (char[])null);
    }

    /**
     * Test if a character is in the range of conversion
     * @param c character to be tested.
     * @param charList list of character to be converted
     * @return <tt>true</tt> if the character is in the range,
     * otherwise <tt>false</tt>
     */
    final protected boolean testRange(char c, char[] charList)
    {
        boolean b = false;

        if (m_useDefaultHtmlEncoderChar)
        {
            for (int i = 0; i < m_defaultHtmlEncodeChar.length; i++)
            {
                if (c == m_defaultHtmlEncodeChar[i])
                {
                    b = true;
                    break;
                }
            }
        }

        if (!b && charList != null)
        {
            for (int i = 0; i < charList.length; i++)
            {
                if (c == charList[i])
                {
                    b = true;
                    break;
                }
            }
        }

        return b;
    }

    /**
     * Test if a character is in the range of conversion
     * @param c character to be tested.
     * @param first a first character to be included in the range of
     * characters to be encoded. If <tt>first</tt> is greater than
     * <tt>last</tt>, no any characters will be encoded.
     * @param last a last character to be included in the range of
     * characters to be encoded. If <tt>last</tt> is less than
     * <tt>first</tt>, no any characters will be encoded.
     * @return <tt>true</tt> if the character is in the range,
     * otherwise <tt>false</tt>
     */
    final protected boolean testRange(char c, char first, char last)
    {
        boolean b = false;

        if (m_useDefaultHtmlEncoderChar)
        {
            for (int i = 0; i < m_defaultHtmlEncodeChar.length; i++)
            {
                if (c == m_defaultHtmlEncodeChar[i])
                {
                    b = true;
                    break;
                }
            }
        }

        if (!b && c >= first && c <= last)
        {
            b = true;
        }

        return b;
    }

    /**
     * Set this flag to false to not use the default HTML chars
     * in the converters. Default is "true".
     * {'<', '>', '&', '"'}
     */
    final protected void setUseDefaultHtmlEncoderChar(boolean p_flag)
    {
        m_useDefaultHtmlEncoderChar = p_flag;
    }


    // convert Windows 1252 characters (0x80 - 0x9f) to Unicode
    private static Character hackWindows1252Ncr(Character p_char)
    {
        Character c = (Character)WINDOWS1252_UNICODE_MAP.get(p_char);

        if (c == null)
        {
            c = p_char;
        }

        return c;
    }


    private static HashMap mapHtmlCharToEntity()
    {
        HashMap h = new HashMap();
        // ISO-8859-1 entities
        h.put(new Character('\u00a0'), "&nbsp;");
        h.put(new Character('\u00a1'), "&iexcl;");
        h.put(new Character('\u00a2'), "&cent;");
        h.put(new Character('\u00a3'), "&pound;");
        h.put(new Character('\u00a4'), "&curren;");
        h.put(new Character('\u00a5'), "&yen;");
        h.put(new Character('\u00a6'), "&brvbar;");
        h.put(new Character('\u00a7'), "&sect;");
        h.put(new Character('\u00a8'), "&uml;");
        h.put(new Character('\u00a9'), "&copy;");
        h.put(new Character('\u00aa'), "&ordf;");
        h.put(new Character('\u00ab'), "&laquo;");
        h.put(new Character('\u00ac'), "&not;");
        h.put(new Character('\u00ad'), "&shy;");
        h.put(new Character('\u00ae'), "&reg;");
        h.put(new Character('\u00af'), "&macr;");
        h.put(new Character('\u00b0'), "&deg;");
        h.put(new Character('\u00b1'), "&plusmn;");
        h.put(new Character('\u00b2'), "&sup2;");
        h.put(new Character('\u00b3'), "&sup3;");
        h.put(new Character('\u00b4'), "&acute;");
        h.put(new Character('\u00b5'), "&micro;");
        h.put(new Character('\u00b6'), "&para;");
        h.put(new Character('\u00b7'), "&middot;");
        h.put(new Character('\u00b8'), "&cedil;");
        h.put(new Character('\u00b9'), "&sup1;");
        h.put(new Character('\u00ba'), "&ordm;");
        h.put(new Character('\u00bb'), "&raquo;");
        h.put(new Character('\u00bc'), "&frac14;");
        h.put(new Character('\u00bd'), "&frac12;");
        h.put(new Character('\u00be'), "&frac34;");
        h.put(new Character('\u00bf'), "&iquest;");
        h.put(new Character('\u00c0'), "&Agrave;");
        h.put(new Character('\u00c1'), "&Aacute;");
        h.put(new Character('\u00c2'), "&Acirc;");
        h.put(new Character('\u00c3'), "&Atilde;");
        h.put(new Character('\u00c4'), "&Auml;");
        h.put(new Character('\u00c5'), "&Aring;");
        h.put(new Character('\u00c6'), "&AElig;");
        h.put(new Character('\u00c7'), "&Ccedil;");
        h.put(new Character('\u00c8'), "&Egrave;");
        h.put(new Character('\u00c9'), "&Eacute;");
        h.put(new Character('\u00ca'), "&Ecirc;");
        h.put(new Character('\u00cb'), "&Euml;");
        h.put(new Character('\u00cc'), "&Igrave;");
        h.put(new Character('\u00cd'), "&Iacute;");
        h.put(new Character('\u00ce'), "&Icirc;");
        h.put(new Character('\u00cf'), "&Iuml;");
        h.put(new Character('\u00d0'), "&ETH;");
        h.put(new Character('\u00d1'), "&Ntilde;");
        h.put(new Character('\u00d2'), "&Ograve;");
        h.put(new Character('\u00d3'), "&Oacute;");
        h.put(new Character('\u00d4'), "&Ocirc;");
        h.put(new Character('\u00d5'), "&Otilde;");
        h.put(new Character('\u00d6'), "&Ouml;");
        h.put(new Character('\u00d7'), "&times;");
        h.put(new Character('\u00d8'), "&Oslash;");
        h.put(new Character('\u00d9'), "&Ugrave;");
        h.put(new Character('\u00da'), "&Uacute;");
        h.put(new Character('\u00db'), "&Ucirc;");
        h.put(new Character('\u00dc'), "&Uuml;");
        h.put(new Character('\u00dd'), "&Yacute;");
        h.put(new Character('\u00de'), "&THORN;");
        h.put(new Character('\u00df'), "&szlig;");
        h.put(new Character('\u00e0'), "&agrave;");
        h.put(new Character('\u00e1'), "&aacute;");
        h.put(new Character('\u00e2'), "&acirc;");
        h.put(new Character('\u00e3'), "&atilde;");
        h.put(new Character('\u00e4'), "&auml;");
        h.put(new Character('\u00e5'), "&aring;");
        h.put(new Character('\u00e6'), "&aelig;");
        h.put(new Character('\u00e7'), "&ccedil;");
        h.put(new Character('\u00e8'), "&egrave;");
        h.put(new Character('\u00e9'), "&eacute;");
        h.put(new Character('\u00ea'), "&ecirc;");
        h.put(new Character('\u00eb'), "&euml;");
        h.put(new Character('\u00ec'), "&igrave;");
        h.put(new Character('\u00ed'), "&iacute;");
        h.put(new Character('\u00ee'), "&icirc;");
        h.put(new Character('\u00ef'), "&iuml;");
        h.put(new Character('\u00f0'), "&eth;");
        h.put(new Character('\u00f1'), "&ntilde;");
        h.put(new Character('\u00f2'), "&ograve;");
        h.put(new Character('\u00f3'), "&oacute;");
        h.put(new Character('\u00f4'), "&ocirc;");
        h.put(new Character('\u00f5'), "&otilde;");
        h.put(new Character('\u00f6'), "&ouml;");
        h.put(new Character('\u00f7'), "&divide;");
        h.put(new Character('\u00f8'), "&oslash;");
        h.put(new Character('\u00f9'), "&ugrave;");
        h.put(new Character('\u00fa'), "&uacute;");
        h.put(new Character('\u00fb'), "&ucirc;");
        h.put(new Character('\u00fc'), "&uuml;");
        h.put(new Character('\u00fd'), "&yacute;");
        h.put(new Character('\u00fe'), "&thorn;");
        h.put(new Character('\u00ff'), "&yuml;");

        // symbols, mathematical symbols, and Greek letters
        h.put(new Character('\u0192'), "&fnof;");

        h.put(new Character('\u0391'), "&Alpha;");
        h.put(new Character('\u0392'), "&Beta;");
        h.put(new Character('\u0393'), "&Gamma;");
        h.put(new Character('\u0394'), "&Delta;");
        h.put(new Character('\u0395'), "&Epsilon;");
        h.put(new Character('\u0396'), "&Zeta;");
        h.put(new Character('\u0397'), "&Eta;");
        h.put(new Character('\u0398'), "&Theta;");
        h.put(new Character('\u0399'), "&Iota;");
        h.put(new Character('\u039a'), "&Kappa;");
        h.put(new Character('\u039b'), "&Lambda;");
        h.put(new Character('\u039c'), "&Mu;");
        h.put(new Character('\u039d'), "&Nu;");
        h.put(new Character('\u039e'), "&Xi;");
        h.put(new Character('\u039f'), "&Omicron;");
        h.put(new Character('\u03a0'), "&Pi;");
        h.put(new Character('\u03a1'), "&Rho;");
        // There is no \u03a2
        h.put(new Character('\u03a3'), "&Sigma;");
        h.put(new Character('\u03a4'), "&Tau;");
        h.put(new Character('\u03a5'), "&Upsilon;");
        h.put(new Character('\u03a6'), "&Phi;");
        h.put(new Character('\u03a7'), "&Chi;");
        h.put(new Character('\u03a8'), "&Psi;");
        h.put(new Character('\u03a9'), "&Omega;");

        h.put(new Character('\u03b1'), "&alpha;");
        h.put(new Character('\u03b2'), "&beta;");
        h.put(new Character('\u03b3'), "&gamma;");
        h.put(new Character('\u03b4'), "&delta;");
        h.put(new Character('\u03b5'), "&epsilon;");
        h.put(new Character('\u03b6'), "&zeta;");
        h.put(new Character('\u03b7'), "&eta;");
        h.put(new Character('\u03b8'), "&theta;");
        h.put(new Character('\u03b9'), "&iota;");
        h.put(new Character('\u03ba'), "&kappa;");
        h.put(new Character('\u03bb'), "&lambda;");
        h.put(new Character('\u03bc'), "&mu;");
        h.put(new Character('\u03bd'), "&nu;");
        h.put(new Character('\u03be'), "&xi;");
        h.put(new Character('\u03bf'), "&omicron;");
        h.put(new Character('\u03c0'), "&pi;");
        h.put(new Character('\u03c1'), "&rho;");
        h.put(new Character('\u03c2'), "&sigmaf;");
        h.put(new Character('\u03c3'), "&sigma;");
        h.put(new Character('\u03c4'), "&tau;");
        h.put(new Character('\u03c5'), "&upsilon;");
        h.put(new Character('\u03c6'), "&phi;");
        h.put(new Character('\u03c7'), "&chi;");
        h.put(new Character('\u03c8'), "&psi;");
        h.put(new Character('\u03c9'), "&omega;");

        h.put(new Character('\u03d1'), "&thetasym;");
        h.put(new Character('\u03d2'), "&upsih;");
        h.put(new Character('\u03d6'), "&piv;");
        h.put(new Character('\u2022'), "&bull;");
        h.put(new Character('\u2026'), "&hellip;");
        h.put(new Character('\u2032'), "&prime;");
        h.put(new Character('\u2033'), "&Prime;");
        h.put(new Character('\u203e'), "&oline;");
        h.put(new Character('\u2044'), "&frasl;");
        h.put(new Character('\u2118'), "&weierp;");
        h.put(new Character('\u2111'), "&image;");
        h.put(new Character('\u211c'), "&real;");
        h.put(new Character('\u2122'), "&trade;");
        h.put(new Character('\u2135'), "&alefsym;");
        h.put(new Character('\u2190'), "&larr;");
        h.put(new Character('\u2191'), "&uarr;");
        h.put(new Character('\u2192'), "&rarr;");
        h.put(new Character('\u2193'), "&darr;");
        h.put(new Character('\u2194'), "&harr;");

        h.put(new Character('\u21b5'), "&crarr;");
        h.put(new Character('\u21d0'), "&lArr;");
        h.put(new Character('\u21d1'), "&uArr;");
        h.put(new Character('\u21d2'), "&rArr;");
        h.put(new Character('\u21d3'), "&dArr;");
        h.put(new Character('\u21d4'), "&hArr;");

        h.put(new Character('\u2200'), "&forall;");
        h.put(new Character('\u2202'), "&part;");
        h.put(new Character('\u2203'), "&exist;");
        h.put(new Character('\u2205'), "&empty;");
        h.put(new Character('\u2207'), "&nabla;");
        h.put(new Character('\u2208'), "&isin;");
        h.put(new Character('\u2209'), "&notin;");
        h.put(new Character('\u220b'), "&ni;");
        h.put(new Character('\u220f'), "&prod;");
        h.put(new Character('\u2211'), "&sum;");
        h.put(new Character('\u2212'), "&minus;");
        h.put(new Character('\u2217'), "&lowast;");
        h.put(new Character('\u221a'), "&radic;");
        h.put(new Character('\u221d'), "&prop;");
        h.put(new Character('\u221e'), "&infin;");
        h.put(new Character('\u2220'), "&ang;");
        h.put(new Character('\u2227'), "&and;");
        h.put(new Character('\u2228'), "&or;");
        h.put(new Character('\u2229'), "&cap;");
        h.put(new Character('\u222a'), "&cup;");
        h.put(new Character('\u222b'), "&int;");
        h.put(new Character('\u2234'), "&there4;");
        h.put(new Character('\u223c'), "&sim;");
        h.put(new Character('\u2245'), "&cong;");
        h.put(new Character('\u2248'), "&asymp;");
        h.put(new Character('\u2260'), "&ne;");
        h.put(new Character('\u2261'), "&equiv;");
        h.put(new Character('\u2264'), "&le;");
        h.put(new Character('\u2265'), "&ge;");
        h.put(new Character('\u2282'), "&sub;");
        h.put(new Character('\u2283'), "&sup;");
        h.put(new Character('\u2284'), "&nsub;");
        h.put(new Character('\u2286'), "&sube;");
        h.put(new Character('\u2287'), "&supe;");
        h.put(new Character('\u2295'), "&oplus;");
        h.put(new Character('\u2297'), "&otimes;");
        h.put(new Character('\u22a5'), "&perp;");
        h.put(new Character('\u22c5'), "&sdot;");
        h.put(new Character('\u2308'), "&lceil;");
        h.put(new Character('\u2309'), "&rceil;");
        h.put(new Character('\u230a'), "&lfloor;");
        h.put(new Character('\u230b'), "&rfloor;");
        h.put(new Character('\u2329'), "&lang;");
        h.put(new Character('\u232a'), "&rang;");
        h.put(new Character('\u25ca'), "&loz;");
        h.put(new Character('\u2660'), "&spades;");
        h.put(new Character('\u2663'), "&clubs;");
        h.put(new Character('\u2665'), "&hearts;");
        h.put(new Character('\u2666'), "&diams;");

        h.put(new Character('\u0152'), "&OElig;");
        h.put(new Character('\u0153'), "&oelig;");
        h.put(new Character('\u0160'), "&Scaron;");
        h.put(new Character('\u0161'), "&scaron;");
        h.put(new Character('\u0178'), "&Yuml;");
        h.put(new Character('\u02c6'), "&circ;");
        h.put(new Character('\u02dc'), "&tilde;");
        h.put(new Character('\u2002'), "&ensp;");
        h.put(new Character('\u2003'), "&emsp;");
        h.put(new Character('\u2009'), "&thinsp;");
        h.put(new Character('\u200c'), "&zwnj;");
        h.put(new Character('\u200d'), "&zwj;");
        h.put(new Character('\u200e'), "&lrm;");
        h.put(new Character('\u200f'), "&rlm;");
        h.put(new Character('\u2013'), "&ndash;");
        h.put(new Character('\u2014'), "&mdash;");
        h.put(new Character('\u2018'), "&lsquo;");
        h.put(new Character('\u2019'), "&rsquo;");
        h.put(new Character('\u201a'), "&sbquo;");
        h.put(new Character('\u201c'), "&ldquo;");
        h.put(new Character('\u201d'), "&rdquo;");
        h.put(new Character('\u201e'), "&bdquo;");
        h.put(new Character('\u2020'), "&dagger;");
        h.put(new Character('\u2021'), "&Dagger;");
        h.put(new Character('\u2030'), "&permil;");
        h.put(new Character('\u2039'), "&lsaquo;");
        h.put(new Character('\u203a'), "&rsaquo;");
        h.put(new Character('\u20ac'), "&euro;");

        return h;
    }

    private static HashMap mapHtmlDefaultCharToEntity()
    {
        HashMap h = new HashMap();
        h.put(new Character('<'), "&lt;");
        h.put(new Character('>'), "&gt;");
        h.put(new Character('&'), "&amp;");
        h.put(new Character('"'), "&quot;");
        //Low version IE does not support "&apos;".
        h.put(new Character('\''), "&#39;");
        return h;
    }

    private static HashMap mapHtmlDefaultEntityToChar()
    {
        HashMap h = new HashMap();
        h.put("&lt;", new Character('<'));
        h.put("&gt;", new Character('>'));
        h.put("&amp;", new Character('&'));
        h.put("&quot;", new Character('"'));
        h.put("&apos;", new Character('\''));
        h.put("&LT;", new Character('<'));
        h.put("&GT;", new Character('>'));
        h.put("&AMP;", new Character('&'));
        h.put("&QUOT;", new Character('"'));
        h.put("&APOS;", new Character('\''));
        return h;
    }

    private static final HashMap mapHtmlEntityToChar()
    {
        HashMap h = new HashMap();
        // ISO-8859-1 entities
        h.put("&nbsp;", new Character('\u00a0'));
        h.put("&nbsp", new Character('\u00a0')); // hack for malformed entity
        h.put("&iexcl;", new Character('\u00a1'));
        h.put("&cent;", new Character('\u00a2'));
        h.put("&pound;", new Character('\u00a3'));
        h.put("&curren;", new Character('\u00a4'));
        h.put("&yen;", new Character('\u00a5'));
        h.put("&brvbar;", new Character('\u00a6'));
        h.put("&sect;", new Character('\u00a7'));
        h.put("&uml;", new Character('\u00a8'));
        h.put("&copy;", new Character('\u00a9'));
        h.put("&ordf;", new Character('\u00aa'));
        h.put("&laquo;", new Character('\u00ab'));
        h.put("&not;", new Character('\u00ac'));
        h.put("&shy;", new Character('\u00ad'));
        h.put("&reg;", new Character('\u00ae'));
        h.put("&macr;", new Character('\u00af'));
        h.put("&deg;", new Character('\u00b0'));
        h.put("&plusmn;", new Character('\u00b1'));
        h.put("&sup2;", new Character('\u00b2'));
        h.put("&sup3;", new Character('\u00b3'));
        h.put("&acute;", new Character('\u00b4'));
        h.put("&micro;", new Character('\u00b5'));
        h.put("&para;", new Character('\u00b6'));
        h.put("&middot;", new Character('\u00b7'));
        h.put("&cedil;", new Character('\u00b8'));
        h.put("&sup1;", new Character('\u00b9'));
        h.put("&ordm;", new Character('\u00ba'));
        h.put("&raquo;", new Character('\u00bb'));
        h.put("&frac14;", new Character('\u00bc'));
        h.put("&frac12;", new Character('\u00bd'));
        h.put("&frac34;", new Character('\u00be'));
        h.put("&iquest;", new Character('\u00bf'));
        h.put("&Agrave;", new Character('\u00c0'));
        h.put("&Aacute;", new Character('\u00c1'));
        h.put("&Acirc;", new Character('\u00c2'));
        h.put("&Atilde;", new Character('\u00c3'));
        h.put("&Auml;", new Character('\u00c4'));
        h.put("&Aring;", new Character('\u00c5'));
        h.put("&AElig;", new Character('\u00c6'));
        h.put("&Ccedil;", new Character('\u00c7'));
        h.put("&Egrave;", new Character('\u00c8'));
        h.put("&Eacute;", new Character('\u00c9'));
        h.put("&Ecirc;", new Character('\u00ca'));
        h.put("&Euml;", new Character('\u00cb'));
        h.put("&Igrave;", new Character('\u00cc'));
        h.put("&Iacute;", new Character('\u00cd'));
        h.put("&Icirc;", new Character('\u00ce'));
        h.put("&Iuml;", new Character('\u00cf'));
        h.put("&ETH;", new Character('\u00d0'));
        h.put("&Ntilde;", new Character('\u00d1'));
        h.put("&Ograve;", new Character('\u00d2'));
        h.put("&Oacute;", new Character('\u00d3'));
        h.put("&Ocirc;", new Character('\u00d4'));
        h.put("&Otilde;", new Character('\u00d5'));
        h.put("&Ouml;", new Character('\u00d6'));
        h.put("&times;", new Character('\u00d7'));
        h.put("&Oslash;", new Character('\u00d8'));
        h.put("&Ugrave;", new Character('\u00d9'));
        h.put("&Uacute;", new Character('\u00da'));
        h.put("&Ucirc;", new Character('\u00db'));
        h.put("&Uuml;", new Character('\u00dc'));
        h.put("&Yacute;", new Character('\u00dd'));
        h.put("&THORN;", new Character('\u00de'));
        h.put("&szlig;", new Character('\u00df'));
        h.put("&agrave;", new Character('\u00e0'));
        h.put("&aacute;", new Character('\u00e1'));
        h.put("&acirc;", new Character('\u00e2'));
        h.put("&atilde;", new Character('\u00e3'));
        h.put("&auml;", new Character('\u00e4'));
        h.put("&aring;", new Character('\u00e5'));
        h.put("&aelig;", new Character('\u00e6'));
        h.put("&ccedil;", new Character('\u00e7'));
        h.put("&egrave;", new Character('\u00e8'));
        h.put("&eacute;", new Character('\u00e9'));
        h.put("&ecirc;", new Character('\u00ea'));
        h.put("&euml;", new Character('\u00eb'));
        h.put("&igrave;", new Character('\u00ec'));
        h.put("&iacute;", new Character('\u00ed'));
        h.put("&icirc;", new Character('\u00ee'));
        h.put("&iuml;", new Character('\u00ef'));
        h.put("&eth;", new Character('\u00f0'));
        h.put("&ntilde;", new Character('\u00f1'));
        h.put("&ograve;", new Character('\u00f2'));
        h.put("&oacute;", new Character('\u00f3'));
        h.put("&ocirc;", new Character('\u00f4'));
        h.put("&otilde;", new Character('\u00f5'));
        h.put("&ouml;", new Character('\u00f6'));
        h.put("&divide;", new Character('\u00f7'));
        h.put("&oslash;", new Character('\u00f8'));
        h.put("&ugrave;", new Character('\u00f9'));
        h.put("&uacute;", new Character('\u00fa'));
        h.put("&ucirc;", new Character('\u00fb'));
        h.put("&uuml;", new Character('\u00fc'));
        h.put("&yacute;", new Character('\u00fd'));
        h.put("&thorn;", new Character('\u00fe'));
        h.put("&yuml;", new Character('\u00ff'));

        // symbols, mathematical symbols, and Greek letters
        h.put("&fnof;", new Character('\u0192'));

        h.put("&Alpha;", new Character('\u0391'));
        h.put("&Beta;", new Character('\u0392'));
        h.put("&Gamma;", new Character('\u0393'));
        h.put("&Delta;", new Character('\u0394'));
        h.put("&Epsilon;", new Character('\u0395'));
        h.put("&Zeta;", new Character('\u0396'));
        h.put("&Eta;", new Character('\u0397'));
        h.put("&Theta;", new Character('\u0398'));
        h.put("&Iota;", new Character('\u0399'));
        h.put("&Kappa;", new Character('\u039a'));
        h.put("&Lambda;", new Character('\u039b'));
        h.put("&Mu;", new Character('\u039c'));
        h.put("&Nu;", new Character('\u039d'));
        h.put("&Xi;", new Character('\u039e'));
        h.put("&Omicron;", new Character('\u039f'));
        h.put("&Pi;", new Character('\u03a0'));
        h.put("&Rho;", new Character('\u03a1'));
        // There is no \u03a2
        h.put("&Sigma;", new Character('\u03a3'));
        h.put("&Tau;", new Character('\u03a4'));
        h.put("&Upsilon;", new Character('\u03a5'));
        h.put("&Phi;", new Character('\u03a6'));
        h.put("&Chi;", new Character('\u03a7'));
        h.put("&Psi;", new Character('\u03a8'));
        h.put("&Omega;", new Character('\u03a9'));

        h.put("&alpha;", new Character('\u03b1'));
        h.put("&beta;", new Character('\u03b2'));
        h.put("&gamma;", new Character('\u03b3'));
        h.put("&delta;", new Character('\u03b4'));
        h.put("&epsilon;", new Character('\u03b5'));
        h.put("&zeta;", new Character('\u03b6'));
        h.put("&eta;", new Character('\u03b7'));
        h.put("&theta;", new Character('\u03b8'));
        h.put("&iota;", new Character('\u03b9'));
        h.put("&kappa;", new Character('\u03ba'));
        h.put("&lambda;", new Character('\u03bb'));
        h.put("&mu;", new Character('\u03bc'));
        h.put("&nu;", new Character('\u03bd'));
        h.put("&xi;", new Character('\u03be'));
        h.put("&omicron;", new Character('\u03bf'));
        h.put("&pi;", new Character('\u03c0'));
        h.put("&rho;", new Character('\u03c1'));
        h.put("&sigmaf;", new Character('\u03c2'));
        h.put("&sigma;", new Character('\u03c3'));
        h.put("&tau;", new Character('\u03c4'));
        h.put("&upsilon;", new Character('\u03c5'));
        h.put("&phi;", new Character('\u03c6'));
        h.put("&chi;", new Character('\u03c7'));
        h.put("&psi;", new Character('\u03c8'));
        h.put("&omega;", new Character('\u03c9'));

        h.put("&thetasym;", new Character('\u03d1'));
        h.put("&upsih;", new Character('\u03d2'));
        h.put("&piv;", new Character('\u03d6'));
        h.put("&bull;", new Character('\u2022'));
        h.put("&hellip;", new Character('\u2026'));
        h.put("&prime;", new Character('\u2032'));
        h.put("&Prime;", new Character('\u2033'));
        h.put("&oline;", new Character('\u203e'));
        h.put("&frasl;", new Character('\u2044'));
        h.put("&weierp;", new Character('\u2118'));
        h.put("&image;", new Character('\u2111'));
        h.put("&real;", new Character('\u211c'));
        h.put("&trade;", new Character('\u2122'));
        h.put("&alefsym;", new Character('\u2135'));
        h.put("&larr;", new Character('\u2190'));
        h.put("&uarr;", new Character('\u2191'));
        h.put("&rarr;", new Character('\u2192'));
        h.put("&darr;", new Character('\u2193'));
        h.put("&harr;", new Character('\u2194'));

        h.put("&crarr;", new Character('\u21b5'));
        h.put("&lArr;", new Character('\u21d0'));
        h.put("&uArr;", new Character('\u21d1'));
        h.put("&rArr;", new Character('\u21d2'));
        h.put("&dArr;", new Character('\u21d3'));
        h.put("&hArr;", new Character('\u21d4'));

        h.put("&forall;", new Character('\u2200'));
        h.put("&part;", new Character('\u2202'));
        h.put("&exist;", new Character('\u2203'));
        h.put("&empty;", new Character('\u2205'));
        h.put("&nabla;", new Character('\u2207'));
        h.put("&isin;", new Character('\u2208'));
        h.put("&notin;", new Character('\u2209'));
        h.put("&ni;", new Character('\u220b'));
        h.put("&prod;", new Character('\u220f'));
        h.put("&sum;", new Character('\u2211'));
        h.put("&minus;", new Character('\u2212'));
        h.put("&lowast;", new Character('\u2217'));
        h.put("&radic;", new Character('\u221a'));
        h.put("&prop;", new Character('\u221d'));
        h.put("&infin;", new Character('\u221e'));
        h.put("&ang;", new Character('\u2220'));
        h.put("&and;", new Character('\u2227'));
        h.put("&or;", new Character('\u2228'));
        h.put("&cap;", new Character('\u2229'));
        h.put("&cup;", new Character('\u222a'));
        h.put("&int;", new Character('\u222b'));
        h.put("&there4;", new Character('\u2234'));
        h.put("&sim;", new Character('\u223c'));
        h.put("&cong;", new Character('\u2245'));
        h.put("&asymp;", new Character('\u2248'));
        h.put("&ne;", new Character('\u2260'));
        h.put("&equiv;", new Character('\u2261'));
        h.put("&le;", new Character('\u2264'));
        h.put("&ge;", new Character('\u2265'));
        h.put("&sub;", new Character('\u2282'));
        h.put("&sup;", new Character('\u2283'));
        h.put("&nsub;", new Character('\u2284'));
        h.put("&sube;", new Character('\u2286'));
        h.put("&supe;", new Character('\u2287'));
        h.put("&oplus;", new Character('\u2295'));
        h.put("&otimes;", new Character('\u2297'));
        h.put("&perp;", new Character('\u22a5'));
        h.put("&sdot;", new Character('\u22c5'));
        h.put("&lceil;", new Character('\u2308'));
        h.put("&rceil;", new Character('\u2309'));
        h.put("&lfloor;", new Character('\u230a'));
        h.put("&rfloor;", new Character('\u230b'));
        h.put("&lang;", new Character('\u2329'));
        h.put("&rang;", new Character('\u232a'));
        h.put("&loz;", new Character('\u25ca'));
        h.put("&spades;", new Character('\u2660'));
        h.put("&clubs;", new Character('\u2663'));
        h.put("&hearts;", new Character('\u2665'));
        h.put("&diams;", new Character('\u2666'));

        h.put("&OElig;", new Character('\u0152'));
        h.put("&oelig;", new Character('\u0153'));
        h.put("&Scaron;", new Character('\u0160'));
        h.put("&scaron;", new Character('\u0161'));
        h.put("&Yuml;", new Character('\u0178'));
        h.put("&circ;", new Character('\u02c6'));
        h.put("&tilde;", new Character('\u02dc'));
        h.put("&ensp;", new Character('\u2002'));
        h.put("&emsp;", new Character('\u2003'));
        h.put("&thinsp;", new Character('\u2009'));
        h.put("&zwnj;", new Character('\u200c'));
        h.put("&zwj;", new Character('\u200d'));
        h.put("&lrm;", new Character('\u200e'));
        h.put("&rlm;", new Character('\u200f'));
        h.put("&ndash;", new Character('\u2013'));
        h.put("&mdash;", new Character('\u2014'));
        h.put("&lsquo;", new Character('\u2018'));
        h.put("&rsquo;", new Character('\u2019'));
        h.put("&sbquo;", new Character('\u201a'));
        h.put("&ldquo;", new Character('\u201c'));
        h.put("&rdquo;", new Character('\u201d'));
        h.put("&bdquo;", new Character('\u201e'));
        h.put("&dagger;", new Character('\u2020'));
        h.put("&Dagger;", new Character('\u2021'));
        h.put("&permil;", new Character('\u2030'));
        h.put("&lsaquo;", new Character('\u2039'));
        h.put("&rsaquo;", new Character('\u203a'));
        h.put("&euro;", new Character('\u20ac'));

        return h;
    }


    /**
     * Character conversion map from Windows 1252 0x80 - 0x9f to Unicode.
     * Data taken from unicode.org.
     * http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1252.TXT
     */
    private static HashMap mapWindows1252ToUnicode()
    {
        HashMap h = new HashMap();
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

    /*
    public static void main(String[] arg)
    {
        HtmlEntities html = new HtmlEntities();

        System.out.println("Test HtmlEntities");
        System.out.println(html.encodeStringBasic("&lt;"));
        // use a real long string here to test performance
        String test = html.encodeStringBasic("<x:ExcelWorkbook>");
        System.out.println("encoding done");
        test = html.decodeStringBasic(test);
        System.out.println("decoding done");
    }
    */
}

