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

import com.globalsight.ling.common.CssEscapeSequence;
import com.globalsight.ling.common.NativeEnDecoderException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <P>Test class for CssEscapeSequence
 *
 * @see com.globalsight.ling.common.CssEscapeSequence
 */
public class Codec
    extends TestCase
{
    String directory = null;

    public static void main (String[] args) 
    {
        String[] myArgs = { Codec.class.getName() };
        junit.swingui.LoadingTestRunner.main(myArgs);
    }

    public Codec (String name) 
    {
        super(name); 
    }

    public static Test suite() 
    {
        return new TestSuite(Codec.class); 
    }

    protected void setUp() 
    {
        directory = "C:/GS/ling/test/css";
    }


    public void testEncode()
        throws NativeEnDecoderException
    {
        CssEscapeSequence codec = new CssEscapeSequence ();
        String s, t;
        
        s = "abcd\\'\"\b\f\n\r\tefgh";
        System.out.println("Encode " + s);
        t = codec.encode(s);
        System.out.println("-----> " + t);
    }


    public void testDecode()
        throws NativeEnDecoderException
    {
        CssEscapeSequence codec = new CssEscapeSequence ();
        String s, t;

        s = "\\00c0\\x21\\1000abcd\\\\\\'\\\"\\b\\f\\n\\r\\tefg\\h";
        System.out.println("Decode " + s);
        t = codec.decode(s);
        System.out.println("-----> " + t);
    }

    public void testDecode1()
        throws NativeEnDecoderException
    {
        CssEscapeSequence codec = new CssEscapeSequence ();
        String s, t;

        s = "ab\\cd \\'\"\b\f\t\\cd efgh";
        System.out.println("Decode " + s);
        t = codec.decode(s);
        System.out.println("-----> " + t);
    }
    
    public void testRoundTrip()
        throws NativeEnDecoderException
    {
        CssEscapeSequence codec = new CssEscapeSequence ();
        String s, t;

        s = "ab\\cd \\'\"\b\f\t\\cd efgh";
        t = codec.decode(codec.encode(s));
        System.out.println("De/Encode " + s + "\n--------> " + t);
        assertEquals("roundtrip en/decoding failed", s, t);
        
        s = "\\00c0\\x21\\1000abcd\\\\\\'\\\"\\b\\f\\n\\r\\tefg\\h";
        t = codec.decode(codec.encode(s));
        System.out.println("De/Encode " + s + "\n--------> " + t);
        assertEquals("roundtrip en/decoding failed", s, t);
    }
}

