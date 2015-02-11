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
package test.globalsight.ling.common;

import java.util.Locale;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.globalsight.ling.common.PseudoTranslator;
import com.globalsight.ling.common.DiplomatBasicParserException;

/**
*/
public class PseudoTranslatorTest
extends TestCase
{
    /** Creates new PseudoTranslatorTest */
    public PseudoTranslatorTest(String p_name) 
    {
         super(p_name);
    }

    /*
    */
    public static void main (String args[]) 
    {
         TestRunner.run(suite());
    }
    
    public void setUp()
    {
    }
    
    public static Test suite()
    {
        return new TestSuite(PseudoTranslatorTest.class);
    }
    
    public void test1()
    {
        String in = "test a string";
        String out;
        PseudoTranslator pt = new PseudoTranslator();
        Exception ex = null;
        
        try
        {
            out = pt.makePseudoTranslation(in, Locale.SIMPLIFIED_CHINESE);
        }
        catch (DiplomatBasicParserException e)
        {
            ex = e;
        }
        
        assertNull(ex);
    }
}
