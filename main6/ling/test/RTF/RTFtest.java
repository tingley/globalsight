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
package test.RTF;

import com.globalsight.ling.tw.offline.rtf.RTFWriter;
import com.globalsight.ling.tw.offline.rtf.RTFWriterAnsi;

import com.globalsight.ling.tw.offline.OfflinePageData;

import test.Arguments;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class RTFtest
{

    public static void main(String argv[])
    {
        String str_fileName;
        boolean b_unicodeRtf = false;
    
        try
        {
            Arguments getopt = new Arguments ();
            int c;
    
            getopt.setUsage(new String[]
                    {
                    "Usage: java RTFWriter [-u] file",
                    "Converts a p-tag file to Workbench RTF.",
                    "The result is written to stdout.",
                    "\t-h: show this help.",
                    "\t-u: generate unicode rtf for Word2k, default is ansi.",
                    } );
    
    
            getopt.parseArgumentTokens(argv, new char[] {'e'});
            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                    case 'u':
                    case 'U':
                        b_unicodeRtf = true;
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
    
            OfflinePageData page = new OfflinePageData ();
    
            page.loadOfflineTextFile(new File (str_fileName), "UTF8");
    
            // If you really need to load format two
            // you have to provide encoding.
            // page.load(new File (str_fileName), "UnicodeLittle");
    
            if (b_unicodeRtf)
            {
                RTFWriter writer = new RTFWriter ();
                writer.writeRTF(page, System.out, false);
            }
            else
            {
                RTFWriterAnsi writer = new RTFWriterAnsi ();
                writer.writeRTF(page, System.out, false);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}