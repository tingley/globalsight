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

package com.globalsight.ling.tw;

import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.util.ClassUtil;

public class PseudoErrorCheckerTest
{

    @Test
    public void testIsTrgTagListValid() throws Exception
    {
        PseudoErrorChecker checker = new PseudoErrorChecker();
        String pTagName = "[${getText('plan.planPriceTiers')}]";
        String sTagName = "[${getText(&apos;plan.planPriceTiers&apos;)}]";

        TagNode node = new TagNode(TagNode.INTERNAL, sTagName, new Hashtable());
        PseudoData data = new PseudoData();
        data.addSourceTagItem(node);

        checker.processTag(pTagName, "");
        ClassUtil.updateField(checker, "m_PseudoData", data);
        Boolean returnValue = (Boolean) ClassUtil.testMethod(checker,
                "isTrgTagListValid");
        Assert.assertTrue(returnValue);
    }
    
    //@Test
    public void testFixTagMoved() throws Exception
    {
        PseudoErrorChecker checker = new PseudoErrorChecker();
        String targetS = " ADDED0 CCC[g1][/g1] ADDED1 [g2]’s Role with JDLink[/g2] ADDED2 [x3] ADDED3 ";
        String sourceS = "[g1]CCC[/g1][g2]’s Role with JDLink[/g2][x3]";

        PseudoData data = new PseudoData();
        data.setPTagTargetString(targetS);
        data.setPTagSourceString(sourceS);
        TagNode node = new TagNode("bpt", "g1", new Hashtable());
        data.addSourceTagItem(node);
        node = new TagNode("ept", "/g1", new Hashtable());
        data.addSourceTagItem(node);
        node = new TagNode("bpt", "g2", new Hashtable());
        data.addSourceTagItem(node);
        node = new TagNode("ept", "/g2", new Hashtable());
        data.addSourceTagItem(node);
        node = new TagNode("internal", "x3", new Hashtable());
        data.addSourceTagItem(node);
        data.setDataType("office-xml");

        String result = checker.check(data, sourceS, 0, "UTF-8", 0, "UTF-8");
        String newTrg = checker.getNewTarget();

        System.out.println(result);
        System.out.println(newTrg);
        
        String expected = "[g1] ADDED0 CCC[/g1][g2] ADDED1 ’s Role with JDLink ADDED2  ADDED3 [/g2][x3]";
        Assert.assertNull(result);
        Assert.assertEquals(expected, newTrg);
    }
    
    //@Test
    public void testFixTagMoved2() throws Exception
    {
        PseudoErrorChecker checker = new PseudoErrorChecker();
        String targetS = "CCC[x3] ADDED0 ";
        String sourceS = "CCC[x3]";

        PseudoData data = new PseudoData();
        data.setPTagTargetString(targetS);
        data.setPTagSourceString(sourceS);
        TagNode node = new TagNode("internal", "x3", new Hashtable());
        data.addSourceTagItem(node);
        data.setDataType("office-xml");

        String result = checker.check(data, sourceS, 0, "UTF-8", 0, "UTF-8");
        String newTrg = checker.getNewTarget();

        System.out.println(result);
        System.out.println(newTrg);
        
        String expected = "CCC ADDED0 [x3]";
        Assert.assertNull(result);
        Assert.assertEquals(expected, newTrg);
    }
    
    //@Test
    public void testFixTagMoved3() throws Exception
    {
        PseudoErrorChecker checker = new PseudoErrorChecker();
        String targetS = " ADDED0 [x3]CCC";
        String sourceS = "[x3]CCC";

        PseudoData data = new PseudoData();
        data.setPTagTargetString(targetS);
        data.setPTagSourceString(sourceS);
        TagNode node = new TagNode("internal", "x3", new Hashtable());
        data.addSourceTagItem(node);
        data.setDataType("office-xml");

        String result = checker.check(data, sourceS, 0, "UTF-8", 0, "UTF-8");
        String newTrg = checker.getNewTarget();

        System.out.println(result);
        System.out.println(newTrg);
        
        String expected = "[x3] ADDED0 CCC";
        Assert.assertNull(result);
        Assert.assertEquals(expected, newTrg);
    }
    
    //@Test
    public void testFixTagMoved4() throws Exception
    {
        PseudoErrorChecker checker = new PseudoErrorChecker();
        String targetS = "[x3]ccc";
        String sourceS = "[x3]";

        PseudoData data = new PseudoData();
        data.setPTagTargetString(targetS);
        data.setPTagSourceString(sourceS);
        TagNode node = new TagNode("internal", "x3", new Hashtable());
        data.addSourceTagItem(node);
        data.setDataType("office-xml");

        String result = checker.check(data, sourceS, 0, "UTF-8", 0, "UTF-8");
        String newTrg = checker.getNewTarget();

        System.out.println(result);
        System.out.println(newTrg);
        
        String expectedResult = "Text cannot be added into segment ([x3]) which only contain tags";
        String expected = "";
        Assert.assertEquals(expectedResult, result);
        Assert.assertEquals(expected, newTrg);
    }

}
