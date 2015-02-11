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

import java.text.MessageFormat;
import com.globalsight.ling.common.RegEx;

// Collection of utility functions
public class Util
{
    
    // This method is based on the Perl code shown below.
    //          my $i = 1;
    //          my %hash;
    //          # Compute CRC for ExactMatch.
    //          (my $crctext = $text) =~ s/\[%%(\d+)]/
    //                                    $hash{$1} = $i;
    //                                        "[%%" . $i++ . "]"
    //                                            /gex;
    //          my $exact_crc = crc($crctext, 32);
    public static long getExactCrc(String text, CRC crc)
        throws AlignerException, Exception
    {
        // get CRC value of exact segment
        StringBuffer buf = new StringBuffer();
        int begin = 0;
        int end = begin;
        int count = 1;
        
        while((end = text.indexOf("[%%", end)) != -1)
        {
            buf.append(text.substring(begin, end));
            buf.append("[%%" + count + "]");
            count++;
            
            if((end = text.indexOf("]", end)) == -1)
            {
                // unlikely
                Object[] args = {new Integer(end), text};
                throw new AlignerException
                    (AlignerExceptionConstants.INVALID_SUBFLOW,
                     MessageFormat.format
                     (AlignerResources.getResource("IllFormedSubflow"), args));
            }
            begin = end + 1;
            
        }
        // last part of segment
        buf.append(text.substring(begin));

        // calculate CRC
        return crc.calculate(buf.toString().getBytes("UTF8"));
    }
    

    // get CRC value of text only segment
    // This method is based on the Perl code shown below.
    //          $crctext = TMUtil::get_segment_text($crctext);
    //          $crctext =~ s/\s+|\&amp;nbsp;?//go;
    //          $crctext = uc($crctext);
    //          my $text_only_crc = crc($crctext, 32);
    public static long getTextOnlyCrc(String text, CRC crc)
        throws AlignerException, Exception
    {
        // strip all TMX tags
        String stripped = stripTmx(text);

        // strip all the whitespaces and &nbsp;
        String textOnly = null;
        textOnly = RegEx.substituteAll(stripped,
                                       "\\s+|\\&amp;nbsp;?", "");
        textOnly = perlUpperCase(textOnly);
        
        // calculate CRC
        return crc.calculate(textOnly.getBytes("UTF8"));
    }


    // mimic Perl's uc() function
    public static String perlUpperCase(String str)
    {
        StringBuffer buf = new StringBuffer();

        int len = str.length();
        for(int i = 0; i < len; i++)
        {
            char c = str.charAt(i);
            if(c >= 'a' && c <= 'z')
            {
                c = Character.toUpperCase(c);
            }
            buf.append(c);
        }
        return buf.toString();
        
    }
    

    // strip all TMX tags
    // This method is a copy of TMUtil::get_segment_text in CAP
    private static String stripTmx(String text)
        throws Exception
    {
        String stripped = text;
        stripped = RegEx.substituteAll(stripped, "<sub[^>]*>[^<]*</sub>", "");
        stripped = RegEx.substituteAll(stripped, "<bpt[^>]*>[^<]*</bpt>", "");
        stripped = RegEx.substituteAll(stripped, "<ept[^>]*>[^<]*</ept>", "");
        stripped = RegEx.substituteAll(stripped, "<ph[^>]*>[^<]*</ph>", "");
        stripped = RegEx.substituteAll(stripped, "<it[^>]*>[^<]*</it>", "");
        stripped = RegEx.substituteAll(stripped, "<ut[^>]*>[^<]*</ut>", "");
        stripped = RegEx.substituteAll(stripped, "&amp;", "&");
        stripped = RegEx.substituteAll(stripped, "&lt;", "<");
        stripped = RegEx.substituteAll(stripped, "&gt;", ">");
        stripped = RegEx.substituteAll(stripped, "&quot;", "\"");
        stripped = RegEx.substituteAll(stripped, "&apos;", "\'");
        return stripped;
    }
    
}
