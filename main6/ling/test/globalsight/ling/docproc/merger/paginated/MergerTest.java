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
package test.globalsight.ling.docproc.merger.paginated;

import com.globalsight.ling.docproc.merger.paginated.PaginatedMerger;
import com.globalsight.ling.docproc.ExtractorException;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;

public class MergerTest
{
    public static void main(String[] args)
        throws ExtractorException, FileNotFoundException, IOException
    {
        if(args.length < 2)
        {
            System.err.println("USAGE: MergerTest paginated_file encoding");
            System.exit(1);
        }
        
        // PaginatedMerger#merge takes two parameters.
        // 1. entire PRSXML as a String
        // 2. character encoding of the merged PRSXML
        // It returns the merged PRSXML as a byte array.
        PaginatedMerger merger = new PaginatedMerger();
        byte[] bytes = merger.merge(readFile(args[0]), args[1]);

        // write out the result in a file
        FileOutputStream writer = new FileOutputStream(args[0] + ".merged");
        writer.write(bytes);
        writer.flush();
        writer.close();

    }

    private static String readFile(String fileName)
        throws FileNotFoundException, IOException
    {
        FileReader in = new FileReader(fileName);
        StringBuffer buf = new StringBuffer();
        int ch;
        while((ch = in.read()) != -1)
        {
            buf.append((char)ch);
        }
        return buf.toString();
    }
    
}
