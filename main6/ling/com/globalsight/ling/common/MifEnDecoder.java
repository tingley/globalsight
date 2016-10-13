package com.globalsight.ling.common;

public class MifEnDecoder extends NativeEnDecoder
{

    public String decode (String p_str)
        throws NativeEnDecoderException
    {
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
    {
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
