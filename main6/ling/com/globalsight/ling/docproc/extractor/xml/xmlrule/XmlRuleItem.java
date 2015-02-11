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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.extractor.xml.Rule;
import org.apache.xpath.XPathAPI;
import com.globalsight.ling.docproc.extractor.xml.XPathAPIJdk;

public abstract class XmlRuleItem
{
    private boolean m_useJdkPath = false;
    public abstract String getName();
    
    public boolean accept(String name)
    {
        return getName() == null || getName().equals(name);
    }
    
    public void setUseJdkXPath(boolean useJdkPath)
    {
        m_useJdkPath = useJdkPath;
    }
    
    public boolean getUseJdkXPath()
    {
        return m_useJdkPath;
    }
    
    protected NodeList selectNodeList(Document toBeExtracted, String xpath) throws Exception
    {
        NodeList affectedNodes = null;
        if (getUseJdkXPath())
        {
            affectedNodes = XPathAPIJdk.selectNodeList(toBeExtracted
                .getDocumentElement(), xpath);
        }
        else
        {
            affectedNodes = XPathAPI.selectNodeList(toBeExtracted
                    .getDocumentElement(), xpath);
        }
        
        return affectedNodes;
    }
    
    /**
     * Replaces the xpath and adds the rule to the map for each node.
     * 
     * @param xpath
     *            the xpath to be replaced
     * @param count
     *            number of "XMLNS" occurance
     * @param namespaces
     *            name spaces of the xml to be extracted
     * @param root
     *            root element of the xml to be extracted
     * @param ruleMap
     *            the map used to keep the rule for each node
     * @param rule
     *            the rule for the node
     * @param isTranslatable
     *            whether it is a translate node
     * @throws ExtractorException
     */
    protected void replaceXpath(String xpath, int count, Object[] namespaces,
            Element root, Map ruleMap, Rule rule, boolean isTranslatable)
            throws ExtractorException
    {
        if (count > 0)
        {
            String temp = xpath;
            for (int i = 0; i < namespaces.length; i++)
            {
                xpath = temp.replaceFirst("XMLNS", (String) namespaces[i]);
                replaceXpath(xpath, count - 1, namespaces, root, ruleMap, rule,
                        isTranslatable);
            }
        }
        else
        {
            try
            {
                NodeList affectedNodes = XPathAPI.selectNodeList(root, xpath);
                if (isTranslatable)
                {
                    for (int l = 0; l < affectedNodes.getLength(); ++l)
                    {
                        Node node = affectedNodes.item(l);
                        Rule previousRule = (Rule) ruleMap.get(node);
                        Rule newRule = rule;
                        // if the node is already in ruleMap, rule
                        // object must be merged. Note that the
                        // existing rule cannot be modified,
                        // because it may be refered to by the other
                        // node.
                        if (previousRule != null)
                        {
                            newRule = previousRule.merge(rule);
                        }

                        ruleMap.put(node, newRule);
                    }
                }
                else
                {
                    for (int k = 0; k < affectedNodes.getLength(); ++k)
                    {
                        ruleMap.put(affectedNodes.item(k), rule);
                    }
                }
            }
            catch (Exception e)
            {
                throw new ExtractorException(e);
            }
        }
    }

    public abstract void applyRule(Node ruleNode, Document toBeExtracted,
            Map ruleMap, Object[] namespaces) throws Exception;

    /**
     * XPath cannot end with "/", so remove "/" if existed.
     * @param xpath
     * @return
     */
    protected static String fixXPath(String xpath)
    {
        if (xpath != null && xpath.trim().length() > 0 && xpath.endsWith("/"))
        {
            return xpath.substring(0, xpath.length() - 1);
        }

        return xpath;
    }
}
