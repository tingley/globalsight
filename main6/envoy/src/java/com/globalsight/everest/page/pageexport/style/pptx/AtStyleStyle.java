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
package com.globalsight.everest.page.pageexport.style.pptx;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A style class extends Bold style
 */
public class AtStyleStyle extends Style
{

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getNodeName()
     */
    @Override
    protected String getNodeName()
    {
        return "atStyle";
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getAddNodeName()
     */
    @Override
    public String getAddNodeName()
    {
        return "atStyle";
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getAddNodeValue()
     */
    @Override
    protected String getAddNodeValue()
    {
        return null;
    }

//	@Override
//	public String getStyle() 
//	{
//		return "atStyle";
//	}
	
    /**
     * Adds style to the pptx document. Should not be null.
     * 
     * @param document
     *            the document that maybe have style node.
     * @return the number of found tag
     */
	@Override
	public void handleStyleNode(Node bNode)
    {
	    if (bNode == null)
            return;
	    
        Node wtNode = bNode.getParentNode();
        if (wtNode == null)
        	return;
        
        Node wrNode = wtNode.getParentNode();
        if (wrNode == null)
        	return;
        
        Node root = wrNode.getParentNode();
        if (root == null)
        	return;

        if (wrNode.getNodeName().equals("a:r"))
        {
            Node cNode = wtNode.getFirstChild();
            
            while (cNode != null)
            {
                Node cloneNode = wrNode.cloneNode(true);

                updateStyle(cNode, cloneNode, wtNode, wrNode, root);

                cNode = cNode.getNextSibling();
            }

            root.removeChild(wrNode);
        }
    }
	
	@Override
	protected void updateStyle(Node cNode, Node cloneNode, Node wtNode,
			Node wrNode, Node root)
    {
	    if (cNode.getNodeName().equals(getNodeName()))
        {
            Element n = (Element) getChild(cloneNode, "a:rPr");

            if (n == null)
            {
                n = cloneNode.getOwnerDocument().createElement("a:rPr");
                Node t = getChild(cloneNode, "a:t");
                cloneNode.insertBefore(n, t);
            }
            
            Node c1 = cNode.getFirstChild();
            if (c1 != null && "atStyleChild".equals(c1.getNodeName()))
            {
                List<Node> ns = getChildNodes(c1);
                List<Node> handledNode = new ArrayList<Node>();
                
                while (true)
                {
                    for (Node c : ns)
                    {
                        Element e = (Element) c;
                        String p = e.getAttribute("gs-Previous");
                        String ne = e.getAttribute("gs-next");
                        
                        boolean handle = false;
                        
                        if (p == null && ne == null) 
                        {
                            n.appendChild(c);
                            handle = true;
                            handledNode.add(n);
                        }
                        else if (p != null && p.length() > 0)
                        {
                            List<Node> cc = getChildNodes(n);
                            Node f = null;
                            for (Node ccc : cc)
                            {
                                if (p.equals(ccc.getNodeName()))
                                {
                                    f = ccc;
                                    break;
                                }
                            }
                            
                            if (f != null)
                            {
                                n.insertBefore(c, f.getNextSibling());
                                handle = true;
                                handledNode.add(c);
                            }
                        }
                        else if (ne != null && ne.length() > 0)
                        {
                            List<Node> cc = getChildNodes(n);
                            Node f = null;
                            for (Node ccc : cc)
                            {
                                if (ne.equals(ccc.getNodeName()))
                                {
                                    f = ccc;
                                    break;
                                }
                            }
                            
                            if (f != null)
                            {
                                n.insertBefore(c, f);
                                handle = true;
                                handledNode.add(c);
                            }
                        }
                        
                        if (handle)
                        {
                            e.removeAttribute("gs-Previous");
                            e.removeAttribute("gs-next");
                        }
                    }
                    
                    if (handledNode.size() == 0)
                    {
                        break;
                    }
                    else
                    {
                        ns.removeAll(handledNode);
                        handledNode.clear();
                    }
                }
                
                for (Node c : ns)
                {
                    Element e = (Element) c;
                    e.removeAttribute("gs-Previous");
                    e.removeAttribute("gs-next");
                    n.appendChild(e);
                }
                
                cNode.removeChild(c1);
            }
            
            List<Node> atts = getAttributes(cNode);
            for (Node att : atts)
            {
                String name = att.getNodeName();
                if (!"styleType".equals(name))
                {
                    n.setAttribute(att.getNodeName(), att.getNodeValue());
                }
            }
        }

        changeText(cloneNode, wtNode.getNodeName(), cNode);

        root.insertBefore(cloneNode, wrNode);
	}
	
	protected void changeText(Node node, String textName, Node styleNode)
    {
        Node t = getChild(node, textName);

        if (t != null)
        {
            if (styleNode.getNodeType() == Node.TEXT_NODE)
            {
                t.setTextContent(styleNode.getTextContent());
            }
            else if (styleNode.getNodeName().equals(getNodeName()))
            {
                // clean the node.
                t.setTextContent("");
                Node cNode = styleNode.getFirstChild();
                while (cNode != null)
                {
                    t.appendChild(cNode.cloneNode(true));
                    cNode = cNode.getNextSibling();
                }
            }
            else
            {
                t.setTextContent("");
                t.appendChild(styleNode.cloneNode(true));
            }
        }
    }
}
