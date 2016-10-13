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

import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.L10nContent;
import com.globalsight.ling.docproc.DiplomatMergerException;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

//import test.FileListBuilder;

//import junit.framework.TestCase;
//import junit.framework.Test;
//import junit.framework.TestSuite;

//import java.io.File;

public class MergerTest
{
    public static void main(String[] args)
        throws DiplomatMergerException, FileNotFoundException, IOException
    {
        if(args.length < 1)
        {
            System.err.println("USAGE: MergerTest diplomat_file");
            System.exit(1);
        }
        
	DiplomatMerger diplomatMerger = new DiplomatMerger();
	L10nContent l10ncontent = new L10nContent();
	
        diplomatMerger.init(readFile(args[0]), l10ncontent);
        diplomatMerger.merge();

        System.out.println(l10ncontent.getL10nContent());
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
