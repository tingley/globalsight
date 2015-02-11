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

import java.util.HashSet;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.xml.Rule;

public class OtherRuleItem extends XmlRuleItem
{
    private Rule createRule(Node ruleNode)
    {
        Rule rule = new Rule();
        NamedNodeMap attributes = ruleNode.getAttributes();
        for (int k = 0; k < attributes.getLength(); ++k)
        {
            Node attr = attributes.item(k);
            String attrName = attr.getNodeName();
            String nodeValue = attr.getNodeValue();

            if (attrName.equals("loctype"))
            {
                rule.setTranslatable("translatable".equals(nodeValue));
            }
            else if (attrName.equals("priority"))
            {
                rule.setPriority(Integer.parseInt(attr.getNodeValue()));
            }
            else if (attrName.equals("datatype"))
            {
                if (nodeValue != null && nodeValue.length() > 0)
                {
                    rule.setDataFormat(nodeValue);
                }
            }
            else if (attrName.equals("type"))
            {
                if (nodeValue != null && nodeValue.length() > 0)
                {
                    rule.setType(nodeValue);
                }
            }
            else if (attrName.equals("containedInHtml"))
            {
                rule.setContainedInHtml((!"no".equals(nodeValue)));
            }
            else if (attrName.equals("inline"))
            {
                rule.setInline(!"no".equals(nodeValue));
            }
            else if (attrName.equals("movable"))
            {
                rule.setMovable(!"no".equals(nodeValue));
            }
            else if (attrName.equals("erasable"))
            {
                rule.setErasable(!"no".equals(nodeValue));
            }
            // this is not-count attribute
            else if (attrName.equals("words"))
            {
                String value = attr.getNodeValue();
                HashSet words = new HashSet();
                words.add(value);
                rule.setWords(words);
            }
        }

        return rule;
    }

    @Override
    public void applyRule(Node ruleNode, Document toBeExtracted, Map ruleMap,
            Object[] namespaces) throws Exception
    {
        NamedNodeMap attributes = ruleNode.getAttributes();

        // find the nodes this rule applies to
        String xpath = attributes.getNamedItem("path").getNodeValue();
        xpath = fixXPath(xpath);
        NodeList affectedNodes = selectNodeList(toBeExtracted, xpath);
        if (affectedNodes.getLength() == 0)
        {
            if (xpath.indexOf("XMLNS") != -1)
            {
                String temp = xpath;
                String[] s = temp.split("XMLNS");
                int count = s.length - 1;
                for (int m = 0; m < namespaces.length; m++)
                {
                    Rule rule = createRule(ruleNode);
                    xpath = temp.replaceFirst("XMLNS", (String) namespaces[m]);
                    replaceXpath(xpath, count - 1, namespaces, toBeExtracted
                            .getDocumentElement(), ruleMap, rule, true);
                }
            }
        }
        else
        {
            for (int l = 0; l < affectedNodes.getLength(); ++l)
            {
                Rule rule = createRule(ruleNode);
                Node node = affectedNodes.item(l);
                Rule previousRule = (Rule) ruleMap.get(node);
                Rule newRule = rule;
                // if the node is already in ruleMap, rule object must be
                // merged. Note that the existing rule cannot be modified,
                // because it may be refered to by the other node.
                if (previousRule != null)
                {
                    newRule = previousRule.merge(rule);
                }

                ruleMap.put(node, newRule);
            }
        }

    }

    @Override
    public String getName()
    {
        return null;
    }
}
