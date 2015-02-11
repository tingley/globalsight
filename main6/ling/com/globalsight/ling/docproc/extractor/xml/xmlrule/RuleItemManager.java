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

package com.globalsight.ling.docproc.extractor.xml.xmlrule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RuleItemManager
{
    public static List<XmlRuleItem> RULE_ITEM = null;
    static
    {
        RULE_ITEM = new ArrayList<XmlRuleItem>();
        RULE_ITEM.add(new NotTranslateRuleItem());
        RULE_ITEM.add(new SidRuleItem());
        RULE_ITEM.add(new InternalRuleItem());
        RULE_ITEM.add(new OtherRuleItem());
    }

    public synchronized static void applyRule(Node ruleNode,
            Document toBeExtracted, Map ruleMap, Object[] namespaces)
            throws Exception
    {
        String nodeName = ruleNode.getNodeName();
        for (XmlRuleItem item : RULE_ITEM)
        {
            if (item.accept(nodeName))
            {
                item.applyRule(ruleNode, toBeExtracted, ruleMap, namespaces);
                break;
            }
        }
    }
}
