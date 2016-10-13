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
package test.globalsight.ling.docproc;

import java.util.Locale;
import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.ling.docproc.GlobalsightRuleBasedBreakIterator;

public class RBBITest {
    
    public RBBITest()     
    {
    }
    
    public static void main(String args[]) 
    {        
       String test1 = " U.S.A.|France ";                       
       String test2 = " 939-7529.* note ";         
       String test3 = " p.m.(seasonal) ";
       String test4 = " p.m. (seasonal) ";        
       String test5 = " p.m.(Seasonal) ";        
       String test6 = " 9 a.m.&nbsp;- ";        
       String test7 = " 10 a.m.-6 p.m. ";        
       String test8 = " DinoLand U.S.A. \u00ae ";  
       String test9 = " Tel.: ++41-31-357-7011 ";  
       
       String test10 = "end. \u00a1Ahora";  
       String test11 = "queen. . . ";  
       String test12 = "end. *now";
       String test13 = "../../../images/navs/nav1_on.gif";
       
       testSegmenter(test1);
       testSegmenter(test2);
       testSegmenter(test3);
       testSegmenter(test4);
       testSegmenter(test5);
       testSegmenter(test6);
       testSegmenter(test7);
       testSegmenter(test8);
       testSegmenter(test9);
       testSegmenter(test10);     
       testSegmenter(test11);     
       testSegmenter(test12);  
       testSegmenter(test13);  
    }
    
    private static void testSegmenter(String p_test)
    {
        // do Locale sensitive sentence breaking
        GlobalsightBreakIterator si;
        si = GlobalsightRuleBasedBreakIterator.getSentenceInstance(new Locale("en", "US"));
        si.setText(p_test);
        int iStart = si.first();      
        for (int iEnd = si.next(); iEnd != GlobalsightBreakIterator.DONE;
            iStart = iEnd, iEnd = si.next()) 
        {
            System.out.println(p_test.substring(iStart, iEnd));
        }
        
        System.out.println("=========================");
    }
}
