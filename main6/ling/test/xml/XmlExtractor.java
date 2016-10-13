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
package test.xml;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.globalsight.ling.common.XmlWriter;

import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DiplomatReader;
import com.globalsight.ling.docproc.extractor.xml.Extractor;

public final class XmlExtractor
{
    public static void main(String args[])
    {
        String str_fileName;
        String str_rulesFileName;
        URL url_fileName;
        URL url_rulesFileName;

        try
        {
            if (args.length != 2)
            {
                System.err.println("Usage: extractor file.xml file.rules");
                System.exit(1);
            }

            str_fileName = args[0];
            str_rulesFileName = args[1];
            url_fileName = fileToURL(str_fileName);
            url_rulesFileName = fileToURL(str_rulesFileName);

            // Read in HTML file and create Input object
            EFInputData input = new EFInputData();
            input.setCodeset("8859_1");
            Locale locale = new Locale("en", "US");
            input.setLocale(locale);
            input.setURL(url_fileName.toString());

            // Rules
            File f_rulesFile = new File (str_rulesFileName);
            byte[] a_rules = new byte[(int)f_rulesFile.length()];
            FileInputStream rulesReader = new FileInputStream(str_rulesFileName);
            rulesReader.read(a_rules, 0, a_rules.length);
            input.setRules(new String (a_rules));

            // Extraction
            Output output = new Output();
            Extractor extractor = new Extractor();
            extractor.init(input, output);
            extractor.loadRules();
            extractor.extract();

            // Print segments
            System.out.println(DiplomatWriter.WriteXML(output)); // before

            // da = new DiplomatAttribute();
            // DiplomatReader dr =
            //   new DiplomatReader(DiplomatWriter.WriteXML(da, output));
            // o = dr.getOutput();
            // print DiplomatWriter.WriteXML(da, o);    // after
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }


    //
    // static helper methods
    //

    static private URL fileToURL(String sfile)
        throws Exception
    {
        File file = new File (sfile);
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");

        if (fSep != null && fSep.length() == 1)
        {
            path = path.replace(fSep.charAt(0), '/');
        }

        if (path.length() > 0 && path.charAt(0) != '/')
        {
            path = '/' + path;
        }

        try
        {
            return new URL ("file", null, path);
        }
        catch (java.net.MalformedURLException e)
        {
            // According to the spec this could only happen if the file
            // protocol were not recognized.
            throw new Exception ("unexpected MalformedURLException");
        }
    }
}
