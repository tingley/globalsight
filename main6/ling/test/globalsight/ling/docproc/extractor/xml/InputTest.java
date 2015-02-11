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
package test.globalsight.ling.docproc.extractor.xml;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.Output;
import java.io.Reader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Locale;
public class InputTest extends AbstractExtractor
{
    public static void main(String[] args)
        throws ExtractorException, FileNotFoundException, IOException
    {
        if(args.length < 2)
        {
            System.err.println("USAGE: java InputTest file_name encoding");
            System.exit(1);
        }
        EFInputData input = new EFInputData();
        input.setCodeset(args[1]);
        input.setLocale(new Locale("en", "US"));
        input.setInput(readFile(args[0]));
        Output output = new Output();
        AbstractExtractor extractor = new InputTest();
        extractor.init(input, output);
        Reader reader = extractor.readInput();
        int ch = reader.read();
        System.out.println("The first char is: 0x" + Integer.toHexString(ch));
    }
    private static byte[] readFile(String fileName)
        throws FileNotFoundException, IOException
    {
        File file = new File(fileName);
        long len = file.length();
        byte[] buf = new byte[(int)len];
        FileInputStream in = new FileInputStream(file);
        in.read(buf);
        return buf;
    }
    public void extract() throws ExtractorException
    {
    }
    public void loadRules() throws ExtractorException
    {
    }
}
