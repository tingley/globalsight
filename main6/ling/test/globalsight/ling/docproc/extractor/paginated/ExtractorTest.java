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
package test.globalsight.ling.docproc.extractor.paginated;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.extractor.paginated.Extractor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
public class ExtractorTest
{
    public static void main(String[] args)
        throws ExtractorException, FileNotFoundException, IOException
    {
        if(args.length < 2)
        {
            System.err.println("USAGE: ExtractTest xml_file encoding");
            System.exit(1);
        }
        EFInputData input = new EFInputData();
        input.setCodeset(args[1]);
        java.util.Locale locale = new java.util.Locale("en", "US");
        input.setLocale(locale);
        input.setURL("file:" + args[0]);
        Output output = new Output();
        /* hack by Andrew to make this compile.  Extractor contructor should
         * take a Connection. */
        //AbstractExtractor extractor = new Extractor();
        AbstractExtractor extractor = new Extractor(null);
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();
        FileOutputStream fo = new FileOutputStream(args[0] + ".diplomat");
        OutputStreamWriter writer = new OutputStreamWriter(fo, "UTF-8");
        writer.write(((Extractor)extractor).getDiplomatizedXml());
        writer.flush();
        writer.close();
    }
}
