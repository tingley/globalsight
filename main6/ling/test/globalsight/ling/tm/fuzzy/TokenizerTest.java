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
package test.globalsight.ling.tm.fuzzy;

import com.globalsight.ling.tm.fuzzy.Tokenizer;

import java.util.Locale;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.ling.docproc.GlobalsightRuleBasedBreakIterator;

public class TokenizerTest
    extends TestCase
{
    private String m_simpleEnglishTest = null;
    private static int SIMPLE_ENGLISH_COUNT;

    /**
    */
    public TokenizerTest(String p_name)
    {
        super(p_name);
    }

    /**
    */
    public static void main(String[] args)
    {
       TestRunner.run(suite());
    }

    /**
    */
    public void setUp()
    {
        SIMPLE_ENGLISH_COUNT = 7; // unique tokens - including space between words
        m_simpleEnglishTest = "one two three four five words";
    }

    /**
    */
    public static Test suite()
    {
        return new TestSuite(TokenizerTest.class);
    }

    /**
    */
    public void test1()
    {
        Tokenizer tokenizer = null;
        HashMap tokenMap = null;
        // get breakiterator for tokenizer - do it here to save
        // recreating it for every segment
        GlobalsightBreakIterator ruleBasedBreakIterator =
            GlobalsightRuleBasedBreakIterator.getWordInstance(Locale.ENGLISH);

        tokenizer = new Tokenizer();
        tokenMap = tokenizer.tokenize(m_simpleEnglishTest, Locale.ENGLISH, ruleBasedBreakIterator);

        assertEquals(tokenMap.size(), SIMPLE_ENGLISH_COUNT);
    }
    
     /**
    */
    public void test2()
    {
        Tokenizer tokenizer = null;
        HashMap tokenMap = null;
        
        GlobalsightBreakIterator ruleBasedBreakIterator =
            GlobalsightRuleBasedBreakIterator.getWordInstance(Locale.ENGLISH);

        tokenizer = new Tokenizer();
        tokenMap = tokenizer.tokenize("smallFile Title", Locale.ENGLISH, ruleBasedBreakIterator);
        tokenMap = tokenizer.tokenize("A small file.", Locale.ENGLISH, ruleBasedBreakIterator);
        tokenMap = tokenizer.tokenize("another para", Locale.ENGLISH, ruleBasedBreakIterator);
        tokenMap = tokenizer.tokenize("", Locale.ENGLISH, ruleBasedBreakIterator);       
        
        tokenMap = tokenizer.tokenize("smallFile Title", Locale.ENGLISH, ruleBasedBreakIterator);
        tokenMap = tokenizer.tokenize("A small file.", Locale.ENGLISH, ruleBasedBreakIterator);
        tokenMap = tokenizer.tokenize("another para", Locale.ENGLISH, ruleBasedBreakIterator);
        tokenMap = tokenizer.tokenize("#ffffff", Locale.ENGLISH, ruleBasedBreakIterator);      
        tokenMap = tokenizer.tokenize("Title", Locale.ENGLISH, ruleBasedBreakIterator); 
        int x = 1;
    }
    
     /**
    */
    public void test3()
    {
        Tokenizer tokenizer = null;
        HashMap tokenMap = null;
        // get breakiterator for tokenizer - do it here to save
        // recreating it for every segment
        GlobalsightBreakIterator ruleBasedBreakIterator =
            GlobalsightRuleBasedBreakIterator.getWordInstance(Locale.ENGLISH);

        tokenizer = new Tokenizer();
        tokenMap = tokenizer.tokenize("1", Locale.ENGLISH, ruleBasedBreakIterator); 
        int x = 1;
    }
    
     /**
    */
    public void test4()
    {
        Tokenizer tokenizer = null;
        HashMap tokenMap = null;
        // get breakiterator for tokenizer - do it here to save
        // recreating it for every segment
        GlobalsightBreakIterator ruleBasedBreakIterator =
            GlobalsightRuleBasedBreakIterator.getWordInstance(Locale.ENGLISH);

        tokenizer = new Tokenizer();
        String test = "http://www.nobel.se/literature/laureates/1953/index.html";
        tokenMap = tokenizer.tokenize(test, Locale.ENGLISH, ruleBasedBreakIterator); 
        int x = 1;
    }
}
