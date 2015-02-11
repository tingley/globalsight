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
//Added --GlobalSight--
package com.globalsight.ling.docproc.extractor.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The methods in this class are convenience methods into the low-level XPath
 * API with Jdk XPath.
 * 
 * @see <A href="http://www.w3.org/TR/xpath">http://www.w3.org/TR/xpath</A>
 */
public class XPathAPIJdk
{
    /**
     * Use an XPath string to select a single node. XPath namespace prefixes are
     * resolved from the contextNode's Document.
     * 
     */
    public static Node selectSingleNode(Node contextNode, String strXPath) throws Exception
    {
        // Have the XObject return its result as a NodeSet.
        NodeList nl = selectNodeList(contextNode, strXPath);

        // Return the first node, or null
        return (nl.getLength() > 0) ? (Node) nl.item(0) : null;
    }

    /**
     * Use an XPath string to select a nodelist. XPath namespace prefixes are
     * resolved from the contextNode's Document.
     */
    public static NodeList selectNodeList(Node contextNode, String strXPath) throws Exception
    {
        return eval(contextNode, strXPath);
    }

    public static NodeList eval(Node contextNode, String strXPath) throws Exception
    {
        XPathFactory xpathFactory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
        XPath xpath = xpathFactory.newXPath();
        NamespaceContext nsp = new XPathJdkNamespaceResolver(contextNode.getOwnerDocument());
        xpath.setNamespaceContext(nsp);

        try
        {
            XPathExpression expr = xpath.compile(strXPath);
            Object result = expr.evaluate(contextNode, XPathConstants.NODESET);

            NodeList r = (NodeList) result;
            return r;
        }
        catch (XPathExpressionException xe)
        {
            // ignore xpath exception for : do not have special name space define
            // for example : miss xmlns:mc in document.xml 
            if (xe.getCause().toString().contains(".XPathStylesheetDOM3Exception"))
            {
                return new XmlNodeListEmpty();
            }
            else
            {
                throw xe;
            }
        }
    }
}
