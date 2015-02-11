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
import test.Arguments;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.ExtractorException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
public class Diplomat
{
    public static void main(String argv[])
    {
        String str_fileName;
        String str_rulesFileName = null;
        String str_encoding = null;
        String str_locale = null;
        try
        {
            Arguments getopt = new Arguments ();
            int c;
            getopt.setUsage(new String[]
                    {
                    "Usage: java Diplomat [-r rules] [-e encoding] [-l locale] file",
                    "Converts a file to Diplomat XML and runs the segmenter.",
                    "The result is written to stdout.",
                    "\t-h: show this help.",
                    "\t-r rules: use rules file 'rules'.",
                    "\t-e encoding: use IANA encoding, e.g. 'ISO-8859-1'.",
                    "\t-l locale: specifies source file locale, eg 'fr_CA'."
                    } );
            DiplomatAPI diplomat = new DiplomatAPI ();
            getopt.parseArgumentTokens(argv, new char[] {'r','e','l'});
            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                    case 'r':
                    case 'R':
                        str_rulesFileName = getopt.getStringParameter();
                        diplomat.setRuleFile(str_rulesFileName);
                        break;
                    case 'e':
                    case 'E':
                        str_encoding = getopt.getStringParameter();
                        diplomat.setIANAEncoding(str_encoding);
                        break;
                    case 'l':
                    case 'L':
                        str_locale = getopt.getStringParameter();
                        diplomat.setLocale(str_locale);
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
            System.err.println("Extracting " + str_fileName);
            diplomat.setSourceFile(str_fileName);
            String strDiplomatXml = diplomat.extract();
            Writer writer = new BufferedWriter (
              new OutputStreamWriter (System.out, "UTF-8"));
            writer.write(strDiplomatXml);
            writer.close();
            // System.err.println(strDiplomatXml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
