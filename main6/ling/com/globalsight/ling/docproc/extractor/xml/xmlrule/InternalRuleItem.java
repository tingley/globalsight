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

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.xml.Rule;

public class InternalRuleItem extends XmlRuleItem
{
    public static final String NAME = "internal";
    private static final String PATH = "path";

    @Override
    public void applyRule(Node ruleNode, Document toBeExtracted, Map ruleMap,
            Object[] namespaces) throws Exception
    {
        NamedNodeMap attributes = ruleNode.getAttributes();
        String xpath = attributes.getNamedItem(PATH).getNodeValue();
        xpath = fixXPath(xpath);

        NodeList affectedNodes = selectNodeList(toBeExtracted, xpath);

        if (affectedNodes != null)
        {
            for (int l = 0; l < affectedNodes.getLength(); ++l)
            {
                Node node = affectedNodes.item(l);
                updateInternal(ruleMap, node);
            }
        }
    }

    private void updateInternal(Map ruleMap, Node node)
    {
        Rule rule = (Rule) ruleMap.get(node);
        if (rule == null)
        {
            rule = new Rule();
            ruleMap.put(node, rule);
        }

        rule.setTranslate(true);
        rule.setInternal(true);
        rule.setInline(true);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

}
