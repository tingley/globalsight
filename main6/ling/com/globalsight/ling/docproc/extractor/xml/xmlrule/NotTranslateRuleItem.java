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

public class NotTranslateRuleItem extends XmlRuleItem
{
    public static final String NAME = "dont-translate";

    @Override
    public void applyRule(Node ruleNode, Document toBeExtracted, Map ruleMap,
            Object[] namespaces) throws Exception
    {
        boolean isInline = false;
        int priority = new Rule().getPriority();
        
        String xpath = ruleNode.getAttributes().getNamedItem("path")
                .getNodeValue();
        xpath = fixXPath(xpath);
        NodeList affectedNodes = selectNodeList(toBeExtracted, xpath);
        NamedNodeMap attributes = ruleNode.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.equals("inline"))
            {
                isInline = (!attr.getNodeValue().equals("no"));
            }
            else if (attrName.equals("priority"))
            {
                priority = Integer.parseInt(attr.getNodeValue());
            }
        }
        
        if (affectedNodes.getLength() == 0)
        {
            if (xpath.indexOf("XMLNS") != -1)
            {
                Rule rule = new Rule();
                rule.setTranslate(false);
                rule.setInline(isInline);
                rule.setPriority(priority);
                String temp = xpath;
                String[] s = temp.split("XMLNS");
                int count = s.length - 1;
                for (int m = 0; m < namespaces.length; m++)
                {
                    xpath = temp.replaceFirst("XMLNS",
                            (String) namespaces[m]);
                    replaceXpath(xpath, count - 1, namespaces,
                            toBeExtracted.getDocumentElement(), ruleMap,
                            rule, false);
                }
            }
        }
        else
        {
            for (int l = 0; l < affectedNodes.getLength(); ++l)
            {
                Node n = affectedNodes.item(l);
                Rule rule = new Rule();
                rule.setTranslate(false);
                rule.setInline(isInline);
                rule.setPriority(priority);
                
                if (ruleMap.containsKey(n))
                {
                    Rule pRule = (Rule) ruleMap.get(n);
                    rule = rule.merge(pRule);
                }
                
                ruleMap.put(n, rule);
            }
        }
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
