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
package test.css;

import junit.framework.*;
import com.globalsight.ling.docproc.extractor.css.*;
import java.util.Vector;
import java.io.FileNotFoundException;

public class test 
    extends TestCase 
    implements IParseEvents
{
    String str_Directory = null;

    public static void main (String[] args) 
    {
        String[] myargs = { test.class.getName() };
        junit.swingui.LoadingTestRunner.main(myargs);
    }

    public test(String name) { super(name); }
    public static Test suite() { return new TestSuite(test.class); }

    protected void setUp() 
    {
        str_Directory = "C:/GS/ling/test/css";
    }

    public void testFile() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "test.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testFile1() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "test1.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testHtml4() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "html4.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testReadme() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "readme.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testDocBookX() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "docbookx.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testSteadyState() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "steadystate.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testMSDN() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "msdn.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testBaron1() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "baron1.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }

    public void testBaron2() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "baron2.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }


    // w3c-css1-test is intentionally wrong and can only be used for 
    // stress-testing a robust parser

    public void testW3C() 
        throws FileNotFoundException, ParseException
    {
        String str_file = str_Directory + "/" + "w3c-css1-test.css";
        Parser parser = new Parser(new java.io.FileInputStream(str_file));

        parser.setHandler(this);
        parser.Parse();
    }


    public void handleStart() {}
    public void handleFinish() {}

    public void handleWhite(String s) {}
    public void handleEndOfLine(String s) {}
    public void handleComment(String s) {}
    public void handleCDO(String s) {}
    public void handleCDC(String s) {}

    public void handleStartCharSet(String s) {}
    public void handleEndCharSet(String s) {}

    public void handleStartFontFace(String s) {}
    public void handleEndFontFace(String s) {}

    public void handleStartImport(String s) {}
    public void handleImport(String s) {}
    public void handleEndImport(String s) {}

    public void handleStartMedia(String s) {}
    public void handleMedia(String s) {}
    public void handleEndMedia(String s) {}

    public void handleStartAtRule(String s) {}

    public void handleStartBlock(String s) {}
    public void handleEndBlock(String s) {}

    public void handleStartDeclarations(String s) {}
    public void handleEndDeclarations(String s) {}

    public void handleToken(String s) {}
    public void handleDelimiter(String s) {}
    public void handleFunction(String s) {}

    public void handleStyle(String s) {}
    public void handleStartValues(String s) {}
    public void handleEndValues() {}
}

// Local Variables:
// c-echo-syntactic-information-p: t
// End:
