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
import com.globalsight.ling.docproc.ExtractorException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.*;

/**
 * <p>A tool class that accepts an input file and runs it through the
 * Extractors.  The results are written to stdout.</p>
 *
 * <p>The format of an input file is automatically derived from its
 * extension.  Known extensions are:</p>
 * <dl>
 * <dt>htm, html<dd>HTML files
 * <dt>css<dd>Cascaded Style Sheet
 * <dt>js<dd>JavaScript
 * <dt>properties<dd>Java property file
 * <dt>xml<dd>XML file <small>also .xxml, so output can be redirected to .xml</small>
 * <dt>xsl, xslt<dd>XSL(T) style sheet
 * <dt>cfm<dd>Cold Fusion file
 * <dt>txt, text<dd>Text file
 * </dl>
 *
 *
 * <p>Usage:</p>
 * <pre>
 *   java Diplomat [-r rules] [-e encoding] [-l locale] file"
 *   -h:          show this help.
 *   -r rules:    use rules file 'rules'.
 *   -e encoding: source file encoding, {@link <A
 * href="ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets">IANA</A>-style. E.g. 'ISO-8859-1'.
 *   -l locale:   source file locale, eg 'fr_CA'.
 *
 * <p>See {@link <A
 * href="ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets">IANA
 * Character Sets</A>}.</p>
 */
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
            "Usage: java com.globalsight.ling.util.Diplomat " +
            "[-r rules] [-e encoding] [-l locale] file",
            "Converts a file to GXML.  The result is written to stdout (UTF-8).",
            "Recognized input file formats: .htm .html .dhtml .jhtml ",
            "\t.xml .xsl .xslt .css .js .vb .vbs .properties",
            "\t.txt .text .cfm .jsp .asp .c++ .cpp .hpp .h",
            "Options:",
            "\t-h: show this help.",
            "\t-d: debug flag; print intermediate GXML results to stderr.",
            "\t-p: use paragraph segmentation, not sentence segmentation.",
            "\t-r rules: specifies the rules file to use for XML files.",
            "\t-e encoding: IANA encoding of the input file, e.g. 'ISO-8859-1'.",
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
                    diplomat.setEncoding(str_encoding);
                    break;
                case 'l':
                case 'L':
                    str_locale = getopt.getStringParameter();
                    diplomat.setLocale(str_locale);
                    break;
                case 'd':
                case 'D':
                    diplomat.setDebug(true);
                    break;
                case 'p':
                case 'P':
                    diplomat.setSentenceSegmentation(false);
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
