/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

import com.globalsight.ling.aligner.AlignerException;
import com.globalsight.ling.aligner.AlignerExceptionConstants;
import com.globalsight.ling.aligner.AlignerResources;

/* 
 * This class is ported from Perl's String::CRC module.  The author of
 * the module disclaim their copyright. The following is their
 * copyright.

=head1 COPYRIGHT

Taken from Matt Dillon's Diablo distribution with permission.  

The authors of this package (David Sharnoff & Matthew Dillon) disclaim
all copyrights and release it into the public domain.

*/

public class CRC
{

    static private class CrcHash
    {
        int h1 = 0;
        int h2 = 0;
    }
    
    private int crcHashLimit = 0;

    /*
     * Poly: 0x00600340.00F0D50A
     */
    private static final int HINIT1 = 0xFAC432B1;
    private static final int HINIT2 = 0x0CD5E44A;

    private static final int POLY1 = 0x00600340;
    private static final int POLY2 = 0x00F0D50B;
    
    private CrcHash[] crcXor = new CrcHash[256];
    private CrcHash[] poly = new CrcHash[64+1];


    public CRC()
        throws AlignerException
    {
        for(int i = 0; i < crcXor.length; i++)
        {
            crcXor[i] = new CrcHash();
        }
        for(int i = 0; i < poly.length; i++)
        {
            poly[i] = new CrcHash();
        }
        
        // Default CRC bit length is 32.
        init(32);
    }

    public void setCrcLength(int crcLength)
        throws AlignerException
    {
        init(crcLength);
    }
    
    
    private void init(int crcLimit)
        throws AlignerException
    {
        if(crcLimit < 16 || crcLimit > 64)
        {
            throw new AlignerException
                (AlignerExceptionConstants.INVALID_CRC_LENGTH,
                 AlignerResources.getResource("InvalidCrcLength"));
        }
        
        crcHashLimit = crcLimit;
        
        /*
         * Polynomials to use for various crc sizes.  Start with the 64 bit
         * polynomial and shift it right to generate the polynomials for fewer
         * bits.  Note that the polynomial for N bits has no bit set above N-8.
         * This allows us to do a simple table-driven CRC.
         */

        poly[64].h1 = POLY1;
        poly[64].h2 = POLY2;
        for(int i = 63; i >= 16; --i) 
        {
            poly[i].h1 = poly[i+1].h1 >>> 1;
            poly[i].h2 = (poly[i+1].h2 >>> 1) | ((poly[i+1].h1 & 1) << 31) | 1;
        }

        for(int i = 0; i < 256; ++i) 
        {
            CrcHash hv = new CrcHash();

            int v = i;
            for(int j = 0; j < 8; ++j, v <<= 1)
            {
                hv.h1 <<= 1;
                if((hv.h2 & 0x80000000) != 0)
                    hv.h1 |= 1;
                hv.h2 = (hv.h2 << 1);
                if((v & 0x80) != 0)
                {
                    hv.h1 ^= poly[crcLimit].h1;
                    hv.h2 ^= poly[crcLimit].h2;
                }
            }
            crcXor[i] = hv;
        }
    }

    /*
     * testhash() - do the CRC.  
     *          The complexity is simply due to the programmable
     *		nature of the number of bits.   We extract the top 8 bits to
     *		use as a table lookup to obtain the polynomial XOR 8 bits at
     *		a time rather then 1 bit at a time.
     */

    public long calculate(byte[] b)
    {
        CrcHash hv = new CrcHash();
        hv.h1 = HINIT1;
        hv.h2 = HINIT2;

        if(crcHashLimit <= 32)
        {
            int s = crcHashLimit - 8;
            int m = (int)-1 >>> (32 - crcHashLimit);

            hv.h1 = 0;
            hv.h2 &= m;

            for(int i = 0; i < b.length; i++)
            {
                int j = (hv.h2 >>> s) & 255;
                /* printf("i = %d %08lx\n", i, CrcXor[i].h2); */
                hv.h2 = ((hv.h2 << 8) & m) ^ b[i] ^ crcXor[j].h2;
            }
        }
        else if(crcHashLimit < 32+8)
        {
            int s2 = 32 + 8 - crcHashLimit;	/* bits in byte from h2 */
            int m = (int)-1 >>> (64 - crcHashLimit);

            hv.h1 &= m;
            for(int i = 0; i < b.length; i++)
            {
                int j = ((hv.h1 << s2) | (hv.h2 >>> (32 - s2))) & 255;
                hv.h1 = (((hv.h1 << 8) ^ (int)(hv.h2 >>> 24)) & m) 
                    ^ crcXor[j].h1;
                hv.h2 = (hv.h2 << 8) ^ b[i] ^ crcXor[j].h2;
            }
        }
        else 
        {
            int s = crcHashLimit - 40;
            int m = (int)-1 >>> (64 - crcHashLimit);

            hv.h1 &= m;
            for(int i = 0; i < b.length; i++)
            {
                int j = (hv.h1 >>> s) & 255;
                hv.h1 = ((hv.h1 << 8) & m) 
                    ^ (int)(hv.h2 >>> 24) ^ crcXor[j].h1;
                hv.h2 = (hv.h2 << 8) ^ b[i] ^ crcXor[j].h2;
            }
        }
        /* printf("%08lx.%08lx\n", (long)hv.h1, (long)hv.h2); */
        long l = hv.h1;
        l = l << 32 | (hv.h2 & 0x00000000ffffffffL);
        return(l);
    }

    /*
     * Test code.
    static public void main(String[] args)
        throws AlignerException
    {
        long l;
        CRC crc = new CRC();
        crc.setCrcLength(16);
        l = crc.calculate("This is the test string".getBytes());
        System.out.println(l == 28315 ? "ok 1\n" : "not ok 1\n");

        crc.setCrcLength(32);
        l = crc.calculate("This is the test string".getBytes());
        System.out.println(l == 0xcd287ff2L ? "ok 2\n" : "not ok 2\n");
        System.out.println(Long.toHexString(l));

        crc.setCrcLength(48);
        l = crc.calculate("This is the test string".getBytes());
        System.out.println(l == 0xe94eb543d7d8L ? "ok 3\n" : "not ok 3\n");
        System.out.println(Long.toHexString(l));

        crc.setCrcLength(64);
        l = crc.calculate("This is the test string".getBytes());
        System.out.println(l == 0xa5c86ea7d0032b8fL ? "ok 4\n" : "not ok 4\n");
        System.out.println(Long.toHexString(l));

        crc.setCrcLength(65);

    }
    */
    
}
