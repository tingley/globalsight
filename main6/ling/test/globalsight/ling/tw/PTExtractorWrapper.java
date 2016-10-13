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
package test.globalsight.ling.tw;

import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.extractor.plaintext.Extractor;
import com.globalsight.ling.docproc.Output;
import java.util.Locale;
	
/**
 * Wrapper class to run the Plain Text extractor.
 */
public class PTExtractorWrapper {

    /**
     * Parsers the input file and returns a complete Diplomat file as a String.
     * @param p_strFilePath - the full input file path.
     * @exception com.globalsight.ling.docproc.ExtractorException
     */
    public String convertFile2DiplomatString(String p_inPath) throws ExtractorException
    {
        // Read in file and create Input object
        EFInputData input = new EFInputData();
        input.setCodeset("8859_1");
        Locale locale = new Locale("en", "US");
        input.setLocale(locale);
        input.setURL("file:///" + p_inPath);

        // Extraction
        Output output = new Output();
        Extractor extractor = new Extractor();
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();

        return(DiplomatWriter.WriteXML(output));
    }

    /**
     * Starts the application.
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args)
    {
        String strFName = "";

        try
        {
            if (args.length == 0)
            {
                System.out.println("Usage: Java DeveloperTestUtility [filename]");
                System.exit(0);
            }
            else
            {
                strFName = args[0];
            }
            DeveloperTestUtility PHT = new DeveloperTestUtility();
            PHT.doFiles(strFName);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * PTExtractorWrapper constructor comment.
     */
    public PTExtractorWrapper() {
        super();
    }}