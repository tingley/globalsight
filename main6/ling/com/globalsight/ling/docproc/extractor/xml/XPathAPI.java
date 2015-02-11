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

import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.apache.xalan.xpath.XPathSupport;
import org.apache.xalan.xpath.XPath;
import org.apache.xalan.xpath.XPathProcessorImpl;
import org.apache.xalan.xpath.xml.XMLParserLiaisonDefault;
import org.apache.xalan.xpath.xml.PrefixResolverDefault;
import org.apache.xalan.xpath.XObject;

import org.apache.xalan.xpath.XNull;              // for complete docs
import org.apache.xalan.xpath.XRTreeFrag;         // for complete docs

/**
 * The methods in this class are convenience methods into the
 * low-level XPath API.  We would like to eventually move these
 * methods into the XPath core, but would like to do some peer
 * review first to make sure we have it right.
 * Please note that these methods execute pure XPaths. They do not
 * implement those parts of XPath extended by XSLT, such as the
 * document() function).  If you want to install XSLT functions, you
 * have to use the low-level API.
 * These functions tend to be a little slow, since a number of objects must be
 * created for each evaluation.  A faster way is to precompile the
 * XPaths using the low-level API, and then just use the XPaths
 * over and over.
 * @see <A href="http://www.w3.org/TR/xpath">http://www.w3.org/TR/xpath</A>
 */
public class XPathAPI
{
  /**
   * Use an XPath string to select a single node. XPath namespace
   * prefixes are resolved from the context node, which may not
   * be what you want (see the next method).
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @return The first node found that matches the XPath, or null.
   */
  public static Node selectSingleNode(Node contextNode, String str)
    throws SAXException
  {
    return selectSingleNode(contextNode, str, contextNode);
  }

  /**
   * Use an XPath string to select a single node.
   * XPath namespace prefixes are resolved from the namespaceNode.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return The first node found that matches the XPath, or null.
   */
  public static Node selectSingleNode(Node contextNode, String str, Node namespaceNode)
    throws SAXException
  {
    // Have the XObject return its result as a NodeSet.
    NodeList nl = selectNodeList(contextNode, str, namespaceNode);

    // Return the first node, or null
    return (nl.getLength() > 0) ? nl.item(0) : null;
  }

 /**
   * Use an XPath string to select a nodelist.
   * XPath namespace prefixes are resolved from the contextNode.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @return A nodelist, should never be null.
   */
  public static NodeList selectNodeList(Node contextNode, String str)
    throws SAXException
  {
    return selectNodeList(contextNode, str, contextNode);
  }

 /**
   * Use an XPath string to select a nodelist.
   * XPath namespace prefixes are resolved from the namespaceNode.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return A nodelist, should never be null.
   */
  public static NodeList selectNodeList(Node contextNode, String str, Node namespaceNode)
    throws SAXException
  {
    // Execute the XPath, and have it return the result
    XObject list = eval(contextNode, str, namespaceNode);

    // Have the XObject return its result as a NodeSet.
    return list.nodeset();

  }

 /**
   * Evaluate XPath string to an XObject.  Using this method,
   * XPath namespace prefixes will be resolved from the namespaceNode.
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return An XObject, which can be used to obtain a string, number, nodelist, etc, should never be null.
   * @see org.apache.xalan.xpath.XObject
   * @see org.apache.xalan.xpath.XNull
   * @see org.apache.xalan.xpath.XBoolean
   * @see org.apache.xalan.xpath.XNumber
   * @see org.apache.xalan.xpath.XString
   * @see org.apache.xalan.xpath.XRTreeFrag
   */
  public static XObject eval(Node contextNode, String str)
    throws SAXException
  {
    return eval(contextNode, str, contextNode);
  }

 /**
   * Evaluate XPath string to an XObject.
   * XPath namespace prefixes are resolved from the namespaceNode.
   * The implementation of this is a little slow, since it creates
   * a number of objects each time it is called.  This could be optimized
   * to keep the same objects around, but then thread-safety issues would arise.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return An XObject, which can be used to obtain a string, number, nodelist, etc, should never be null.
   * @see org.apache.xalan.xpath.XObject
   * @see org.apache.xalan.xpath.XNull
   * @see org.apache.xalan.xpath.XBoolean
   * @see org.apache.xalan.xpath.XNumber
   * @see org.apache.xalan.xpath.XString
   * @see org.apache.xalan.xpath.XRTreeFrag
   */
  public static XObject eval(Node contextNode, String str, Node namespaceNode)
    throws SAXException
  {
    // Since we don't have a XML Parser involved here, install some default support
    // for things like namespaces, etc.
    // (Changed from: XPathSupportDefault xpathSupport = new XPathSupportDefault();
    //    because XPathSupportDefault is weak in a number of areas... perhaps
    //    XPathSupportDefault should be done away with.)
    XPathSupport xpathSupport = new XMLParserLiaisonDefault();

    if(null == namespaceNode)
      namespaceNode = contextNode;

    // Create an object to resolve namespace prefixes.
    // XPath namespaces are resolved from the input context node's document element
    // if it is a root node, or else the current context node (for lack of a better
    // resolution space, given the simplicity of this sample code).
    PrefixResolverDefault prefixResolver = new PrefixResolverDefault((namespaceNode.getNodeType() == Node.DOCUMENT_NODE)
                                                         ? ((Document)namespaceNode).getDocumentElement() :
                                                           namespaceNode);

    // Create the XPath object.
    XPath xpath = new XPath();

    // Create a XPath parser.
    XPathProcessorImpl parser = new XPathProcessorImpl(xpathSupport);
    parser.initXPath(xpath, str, prefixResolver);

    // Execute the XPath, and have it return the result
    return xpath.execute(xpathSupport, contextNode, prefixResolver);
  }
}
