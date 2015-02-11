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

}
