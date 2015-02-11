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
import org.xml.sax.SAXException;

import com.globalsight.ling.docproc.extractor.xml.Rule;
import com.globalsight.ling.docproc.extractor.xml.XPathAPI;

public class NotTranslateRuleItem extends XmlRuleItem
{
    private final String NAME = "dont-translate";

    @Override
    public void applyRule(Node ruleNode, Document toBeExtracted, Map ruleMap,
            Object[] namespaces) throws SAXException
    {
        boolean isInline = false;
        String xpath = ruleNode.getAttributes().getNamedItem("path")
                .getNodeValue();
        NodeList affectedNodes = XPathAPI.selectNodeList(toBeExtracted
                .getDocumentElement(), xpath);
        NamedNodeMap attributes = ruleNode.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.equals("inline"))
            {
                isInline = (!attr.getNodeValue().equals("no"));
            }
        }
        
        if (affectedNodes.getLength() == 0)
        {
            if (xpath.indexOf("XMLNS") != -1)
            {
                Rule rule = new Rule();
                rule.setTranslate(false);
                rule.setInline(isInline);
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
