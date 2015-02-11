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
package test.javascript;

import java.lang.*;
import java.io.*;
import java.util.*;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DiplomatReader;
import com.globalsight.ling.docproc.extractor.javascript.Extractor;

public final class JsExtractor
{
    public static void main(String args[])
    {
        String str_baseDir = "C:/GS/ling/";
        String str_fileName = "";

        try
        {
            if (args.length == 0)
            {
                str_fileName = str_baseDir + "test/javascript/TbMenus.html";
            }
            else
            {
                str_fileName = args[0];
            }
            
            
            // Read in HTML file and create Input object
            EFInputData input = new EFInputData();
            input.setCodeset("8859_1");
            Locale locale = new Locale("en", "US");
            input.setLocale(locale);
            input.setURL("file:///" + str_fileName);

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
}
