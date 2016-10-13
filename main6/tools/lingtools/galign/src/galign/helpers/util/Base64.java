/*
 * @(#)Codecs.java                  0.2-2 23/03/1997
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996,1997  Ronald Tschalaer
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *  Ronald.Tschalaer@psi.ch
 *
 *  Modification:
 *  -   Modified to remove use of deprecated methods and using
 *  -   ISO8859_1 encdoing.
 */

/**
 * Performs conversions to and from the base-64 encoding as defined by
 * RFC-1521 (Section 5.2)
 *
 * <P>
 * <font size=-1>
 * The algorithims for this class were taken from the HTTPClient.Codecs
 * class as provided by the HTTPClient library from
 * <a href="mailto:ronald@innovation.ch">Ronald Tschalaer</a>. This code
 * has been redistributed and modified under the GNU Library General Public
 * License as published by the Free Software Foundation.
 * <P>
 * For a copy of the GNU Library General Public License please write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA
 * </font>
 *
 * @author Bob Mason
 * @version $Id: Base64.java,v 1.1 2009/04/14 15:40:59 yorkjin Exp $
 */

package galign.helpers.util;

import java.io.UnsupportedEncodingException;

public class Base64 {
    //-------------------------------------
    /** Class version string */

    public static final String CLASS_VERSION =
        "$Id: Base64.java,v 1.1 2009/04/14 15:40:59 yorkjin Exp $";

    private static byte[] sBase64EncMap = null;
    private static byte[] sBase64DecMap = null;

    static {
        // rfc-521: Base64 Alphabet
        byte[] map =
        {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D',
         (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H',
         (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
         (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P',
         (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
         (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
         (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b',
         (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
         (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
         (byte) 'k', (byte) 'l', (byte) 'm',  (byte) 'n',
         (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r',
         (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
         (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
         (byte) '0', (byte) '1', (byte) '2', (byte) '3',
         (byte) '4', (byte) '5', (byte) '6', (byte) '7',
         (byte) '8', (byte) '9', (byte) '+', (byte) '/'};
        sBase64EncMap = map;
        sBase64DecMap = new byte[128];
        for (int idx=0; idx<sBase64EncMap.length; idx++)
            sBase64DecMap[sBase64EncMap[idx]] = (byte) idx;
    }

    /**
     * This class isn't meant to be instantiated.
     */
    private Base64() {
    }

    /**
     * This method encodes the given string using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param pData the string
     * @return the base64-encoded String
     */
    public final static String encodeToString(String pData) {
        if (pData == null)  return  null;

        try
        {
            return new String(encodeToByteArray(pData), "ISO8859_1");
        }
        catch (UnsupportedEncodingException e)
        {
            // doesn't reach here
            return null;
        }
    }

    /**
     * This method encodes the given byte array using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param pData the string
     * @return the base64-encoded String
     */
    public final static String encodeToString(byte [] pData) {
        if (pData == null)  return  null;

        try
        {
            return new String(encodeToByteArray(pData), "ISO8859_1");
        }
        catch (UnsupportedEncodingException e)
        {
            // doesn't reach here
            return null;
        }
    }

    /**
     * This method encodes the given String using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param  pData the data
     * @return the base64-encoded <var>pData</var>
     */
    public final static byte[] encodeToByteArray(String pData) {
        if (pData == null)  return  null;

        byte data[] = new byte[pData.length()];
        try
        {
            data = pData.getBytes("ISO8859_1");
        }
        catch (UnsupportedEncodingException e)
        {
            // doesn't reach here
        }

        return encodeToByteArray(data);
    }

    /**
     * This method encodes the given byte[] using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param  pData the data
     * @return the base64-encoded <var>pData</var>
     */
    public final static byte[] encodeToByteArray(byte[] pData) {
        if (pData == null)  return  null;

        int sidx, didx;
        byte dest[] = new byte[((pData.length+2)/3)*4];


        // 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
        for (sidx=0, didx=0; sidx < pData.length-2; sidx += 3) {
            dest[didx++] = sBase64EncMap[(pData[sidx] >>> 2) & 077];
            dest[didx++] = sBase64EncMap[(pData[sidx+1] >>> 4) & 017 |
                                        (pData[sidx] << 4) & 077];
            dest[didx++] = sBase64EncMap[(pData[sidx+2] >>> 6) & 003 |
                                        (pData[sidx+1] << 2) & 077];
            dest[didx++] = sBase64EncMap[pData[sidx+2] & 077];
        }
        if (sidx < pData.length) {
            dest[didx++] = sBase64EncMap[(pData[sidx] >>> 2) & 077];
            if (sidx < pData.length-1) {
                dest[didx++] = sBase64EncMap[(pData[sidx+1] >>> 4) & 017 |
                                            (pData[sidx] << 4) & 077];
                dest[didx++] = sBase64EncMap[(pData[sidx+1] << 2) & 077];
            }
            else
                dest[didx++] = sBase64EncMap[(pData[sidx] << 4) & 077];
        }

        // add padding
        for ( ; didx < dest.length; didx++)
            dest[didx] = (byte) '=';

        return dest;
    }

    /**
     * This method decodes the given string using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param pData the base64-encoded string.
     * @return the decoded String
     */
    public final static String decodeToString(String pData) {
        if (pData == null)  return  null;

        try
        {
            return new String(decodeToByteArray(pData), "ISO8859_1");
        }
        catch (UnsupportedEncodingException e)
        {
            // doesn't reach here
            return null;
        }
    }

    /**
     * This method decodes the given byte array using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param pData the base64-encoded string.
     * @return the decoded String
     */
    public final static String decodeToString(byte [] pData) {
        try
        {
            return new String(decodeToByteArray(pData), "ISO8859_1");
        }
        catch (UnsupportedEncodingException e)
        {
            // doesn't reach here
            return null;
        }
    }

    /**
     * This method decodes the given string using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param pData the base64-encoded string.
     * @return the decoded String
     */
    public final static byte [] decodeToByteArray(String pData) {
        if (pData == null)  return  null;

        byte data[] = new byte[pData.length()];
        try
        {
            data = pData.getBytes("ISO8859_1");
        }
        catch (UnsupportedEncodingException e)
        {
            // doesn't reach here
        }

        return decodeToByteArray(data);
    }

    /**
     * This method decodes the given byte array using the base64-encoding
     * specified in RFC-1521 (Section 5.2).
     *
     * @param  pData the base64-encoded data.
     * @return the decoded <var>pData</var>.
     */
    public final static byte[] decodeToByteArray(byte[] pData) {
        if (pData == null)  return  null;

        int tail = pData.length;
        while (pData[tail-1] == '=')  tail--;

        byte dest[] = new byte[tail - pData.length/4];


        // ascii printable to 0-63 conversion
        for (int idx = 0; idx <pData.length; idx++)
            pData[idx] = sBase64DecMap[pData[idx]];

        // 4-byte to 3-byte conversion
        int sidx, didx;
        for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx += 3) {
            dest[didx]   = (byte) ( ((pData[sidx] << 2) & 255) |
                                    ((pData[sidx+1] >>> 4) & 003) );
            dest[didx+1] = (byte) ( ((pData[sidx+1] << 4) & 255) |
                                    ((pData[sidx+2] >>> 2) & 017) );
            dest[didx+2] = (byte) ( ((pData[sidx+2] << 6) & 255) |
                                    (pData[sidx+3] & 077) );
        }
        if (didx < dest.length)
            dest[didx]   = (byte) ( ((pData[sidx] << 2) & 255) |
                                    ((pData[sidx+1] >>> 4) & 003) );
        if (++didx < dest.length)
            dest[didx]   = (byte) ( ((pData[sidx+1] << 4) & 255) |
                                    ((pData[sidx+2] >>> 2) & 017) );

        return dest;
    }
}
