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
package com.globalsight.ling.util;

import com.globalsight.ling.util.Arguments;

import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.L10nContent;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.*;

/**
 * <p>A tool class to convert a Diplomat XML file back to its original
 * format by stripping all Diplomat XML tags.  The result is written
 * to stdout.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   java Merger [-e encoding] [-g] file
 *   -e encoding: source file encoding, {@link <A
 * href="ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets">IANA</A>}-style. E.g. 'ISO-8859-1'.
 *   -g: output original <gsa> tags
 * </pre>
 */
public class Merger
{
    public static void main(String argv[])
    {
        Arguments getopt = new Arguments ();
        int c;

        boolean b_includeGsa = false;
        String str_encoding = null;
        String str_fileName;
        String str_diplomat;
        String str_result;

        try
        {
            getopt.setUsage(new String[]
                    {
                    "Usage: java com.globalsight.ling.util.Merger [-g] [-e enc] file",
                    "Converts a Diplomat XML file (in UTF-8) back to its original format.",
                    "The result is written to stdout.",
                    "\t-g: output original <gs> tags as well",
                    "\t-e enc: merge to the specified IANA encoding",
                    } );

            getopt.parseArgumentTokens(argv, new char[] {'e'});
            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                case 'g':
                case 'G':
                    b_includeGsa = true;
                    break;
                case 'e':
                case 'E':
                    str_encoding = getopt.getStringParameter();
                    break;
                case 'h':
                default:
                    getopt.printUsage();
                    System.exit(1);
                    break;
                }
            }

            str_fileName = getopt.getlistFiles();
            if (str_fileName == null)
            {
                getopt.printUsage();
                System.exit(1);
            }

            DiplomatAPI diplomat = new DiplomatAPI ();

            str_diplomat = readString (str_fileName, "utf-8");
            if (str_encoding == null)
            {
                str_result = diplomat.merge(str_diplomat, b_includeGsa);

                Writer writer = new BufferedWriter (
                    new OutputStreamWriter (System.out, "utf-8"));

                writer.write(str_result);
                writer.close();
            }
            else
            {
                byte[] a_result = diplomat.merge(str_diplomat, str_encoding,
                    b_includeGsa);

                str_result = new String(a_result, "UTF-8");

                Writer writer = new BufferedWriter (
                    new OutputStreamWriter (System.out));

                writer.write(str_result);
                writer.close();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    private static String readString (String p_fileName, String p_encoding)
        throws IOException
    {
        File f = new File (p_fileName);
        byte[] a_bytes = new byte [(int)f.length()];
        FileInputStream r = new FileInputStream(f);
        r.read(a_bytes, 0, a_bytes.length);

        return new String (a_bytes, p_encoding);
    }
}

