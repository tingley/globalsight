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
package test.HtmlExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import test.Arguments;

import com.globalsight.ling.docproc.DiplomatAPI;

public class Merger
{
    public static void main(String argv[])
    {
        Arguments getopt = new Arguments();
        int c;
        boolean b_includeGsa = false;
        String str_fileName;
        String str_encoding = "utf-8";
        String str_diplomat;
        String str_result;
        try
        {
            getopt.setUsage(new String[]
            {
                    "Usage: java Merger [-g] file",
                    "Converts a Diplomat XML file (in UTF-8) back to its original format.",
                    "The result is written to stdout.",
                    "\t-e: encoding of the file (default: utf-8)",
                    "\t-g: output original <gsa> tags as well", });
            getopt.parseArgumentTokens(argv, new char[]
            { 'e', 'g' });
            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                    case 'e':
                    case 'E':
                        str_encoding = getopt.getStringParameter();
                        break;
                    case 'g':
                    case 'G':
                        b_includeGsa = true;
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
            DiplomatAPI diplomat = new DiplomatAPI();
            str_diplomat = readString(str_fileName, "utf-8");
            str_result = diplomat.merge(str_diplomat, b_includeGsa);
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    System.out, "utf-8"));
            writer.write(str_result);
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static String readString(String p_fileName, String p_encoding)
            throws IOException
    {
        File f = new File(p_fileName);
        byte[] a_bytes = new byte[(int) f.length()];
        FileInputStream r = new FileInputStream(f);
        r.read(a_bytes, 0, a_bytes.length);
        return new String(a_bytes, p_encoding);
    }
}
