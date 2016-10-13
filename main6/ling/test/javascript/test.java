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

import junit.framework.*;
import com.globalsight.ling.docproc.extractor.javascript.*;
import java.util.Vector;

public class test 
    extends TestCase 
    implements IParseEvents
{
    public static void main (String[] args) 
    {
        String[] myargs = { test.class.getName() };
        junit.swingui.LoadingTestRunner.main(myargs);
    }

    public test (String name) { super(name); }
    public static Test suite() { return new TestSuite(test.class); }

    String str_Directory = null;
    protected void setUp() 
    {
        str_Directory = "C:/GS/ling/test/javascript";
    }

    public void testToolbars() 
        throws java.io.FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "Toolbars.js";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.parse();
    }

    public void testTbMenues() 
        throws java.io.FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "TbMenus.js";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.parse();
    }

    public void handleStart() {}
    public void handleFinish() {}

    public void handleWhite(String s) {}
    public void handleEndOfLine(String s) {}
    public void handleComment(String s) {}
    public void handleCDO(String s) {}
    public void handleCDC(String s) {}

    public void handleLiteral(String s) { }
    public void handleString(String s) { }
    public void handleKeyword(String s) { }
    public void handleOperator(String s) { }
}
