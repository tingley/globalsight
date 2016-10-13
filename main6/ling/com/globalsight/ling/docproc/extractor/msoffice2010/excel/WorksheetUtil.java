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
package com.globalsight.ling.docproc.extractor.msoffice2010.excel;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;

public class WorksheetUtil 
{
    private XmlUtil util = new XmlUtil();
    
    private static List<String> IGNORE_ATTS = new ArrayList<String>();
    static
    {
    	IGNORE_ATTS.add("dirty");
    	IGNORE_ATTS.add("err");
    	IGNORE_ATTS.add("lang");
    	IGNORE_ATTS.add("altLang");
    }
	
	public void handle(Document document)
	{
		mergeSameNodes(document);
		mergeNodes(document);
	}
	
	private void mergeSameNodes(Document document)
	{
		List<Node> ns = util.getNodes(document, "r");
		
		for (int i = 0; i < ns.size() - 1; i++)
		{
			Node n1 = ns.get(i);
			Node n2 = ns.get(i + 1);
			
			if (canMergeAsSame(n1, n2))
			{
				mergeTo(n1, n2);
			}
		}
	}
	
	private void mergeNodes(Document document)
	{
		List<Node> ns = util.getNodes(document, "r");
		
		for (int i = 0; i < ns.size() - 1; i++)
		{
			Node n1 = ns.get(i);
			Node n2 = ns.get(i + 1);
			
			if (canMerge(n1, n2))
			{
				mergeTo(n1, n2);
			}
		}
	}
	
	private boolean canMergeAsSame(Node node1, Node node2)
	{
	    Node next = node1;
        while(true)
        {
            next = next.getNextSibling();
            if (next == null || next.getNodeType() != Node.TEXT_NODE)
                break;
        }
        
		if (!node2.equals(next))
			return false;
		
		Node n1 = util.getNode(node1, "rPr", false);
		Node n2 = util.getNode(node2, "rPr", false);
		
		List<Node> listc1 = util.getChildNodes(n1);
		List<Node> listc2 = util.getChildNodes(n2);
		
		for (int i = 0; i < listc1.size(); i++)
		{
			Node c1 = listc1.get(i);
			for (int j = 0; j < listc2.size(); j++)
			{
				Node c2 = listc2.get(j);
				if (isSameNode(c1, c2))
				{
					listc1.remove(c1);
					listc2.remove(c2);
					
					i--;
					break;
				}
			}
		}

		
		if (listc1.size() > 0 || listc2.size() > 0)
			return false;
		
		return true;
	}
	
	private boolean canMerge(Node n1, Node n2)
	{
	    Node next = n1;
	    while(true)
	    {
	        next = next.getNextSibling();
	        if (next == null || next.getNodeType() != Node.TEXT_NODE)
	            break;
	    }
	    
		if (!n2.equals(next))
			return false;
		
		return isSameAtt(n1, n2);
	}
	
	private void mergeTo(Node n1, Node n2)
	{
		Node wt1 = util.getNode(n1, "t", false);
		Node wt2 = util.getNode(n2, "t", false);
		
		if (wt1 != null && wt2 != null)
		{
			List<Node> cs = util.getChildNodes(wt1);
			Node f = wt2.getFirstChild();
			
			for (int i = 0; i < cs.size() ; i++)
			{
				Node c = cs.get(i);
				wt2.insertBefore(c, f);
				
				Element e = (Element) wt2;
	            e.setAttribute("xml:space", "preserve");
			}
			
			n1.getParentNode().removeChild(n1);
		}
	}
	
	private boolean isSameAtt(Node r1, Node r2)
	{
	    Node n1 = util.getNode(r1, "rPr", false);
        Node n2 = util.getNode(r2, "rPr", false);
        
		Node wt1 = util.getNode(r1, "t", false);
		Node wt2 = util.getNode(r2, "t", false);
		
		if (wt1 == null || wt2 == null)
			return false;
        
		List<Node> listc1 = util.getChildNodes(n1);
		List<Node> listc2 = util.getChildNodes(n2);
		
		for (int i = 0; i < listc1.size(); i++)
		{
			Node c1 = listc1.get(i);
			for (Node c2 : listc2)
			{
				if (isSameNode(c1, c2))
				{
					listc1.remove(c1);
					listc2.remove(c2);
					break;
				}
			}
		}
		
		
		addAtt(wt1, listc1);
		addAtt(wt2, listc2);
		
		return true;
	}
	
	private String getStyleName(Node att)
	{
		String name = att.getNodeName();
		if ("u".equalsIgnoreCase(name))
		    return "ulined";
		else if ("i".equalsIgnoreCase(name))
            return "italic";
		else if ("b".equalsIgnoreCase(name))
            return "bold";
		else if ("vertAlign".equalsIgnoreCase(name))
		{
		    NamedNodeMap n = att.getAttributes();
		    Node a = n.getNamedItem("val");
		    if ("superscript".equalsIgnoreCase(a.getNodeValue()))
		        return "office-sup";
		    else if ("subscript".equalsIgnoreCase(a.getNodeValue()))
                return "office-sub";
		}
		
		return null;
	}
	
	private void addAtt(Node at, List<Node> attcs)
	{
		if (attcs.size() == 0)
			return ;
		
		// record the order of the attribute.
		for (Node attc : attcs)
        {
            Element attce = (Element) attc;
            Node previous = attce.getPreviousSibling();
            String pName = previous == null ? "null" : previous.getNodeName();
            attce.setAttribute("gs-Previous", pName);
            
            Node next = attce.getNextSibling();
            String pName2 = next == null ? "null" : next.getNodeName();
            attce.setAttribute("gs-next", pName2);
        }
		
		// for b i u sub sup
		List<Node> sAtt = new ArrayList<Node>();
		for (Node attc : attcs)
        {
		    String style = getStyleName(attc);
            if (style != null){
                Element e = at.getOwnerDocument().createElement("atStyle");
                e.setAttribute("styleType", style);
                Element ec = at.getOwnerDocument().createElement("atStyleChild");
                e.appendChild(ec);
                ec.appendChild(attc);
                
                List<Node> cs = util.getChildNodes(at);
                for (Node c : cs)
                {
                    at.removeChild(c);
                    e.appendChild(c);
                }
                
                at.appendChild(e);
                
                sAtt.add(attc);
            }
        }
		
		// for others
		attcs.removeAll(sAtt);
		if (attcs.size() > 0)
		{
		    Element e = at.getOwnerDocument().createElement("atStyle");
			Element ec = at.getOwnerDocument().createElement("atStyleChild");
			e.appendChild(ec);
			for (Node attc : attcs)
			{
			    String style = getStyleName(attc);
			    if (style != null){
			        continue;
			    }
			    
				ec.appendChild(attc);
			}
			
			List<Node> cs = util.getChildNodes(at);
	        
	        for (Node c : cs)
	        {
	            at.removeChild(c);
	            e.appendChild(c);
	        }
	        
	        at.appendChild(e);
		}
	}

	private boolean isSameNode(Node n1, Node n2)
	{
		if (n1.getNodeType() != n2.getNodeType())
			return false;
		
		if (n1.getNodeName() != n2.getNodeName())
			return false;
		
		NamedNodeMap attrs1 = n1.getAttributes();
		NamedNodeMap attrs2 = n2.getAttributes();
		
		if (attrs1 == null && attrs2 == null)
			return true;
		
		if (attrs1 != null && attrs2 != null)
		{
			if (attrs1.getLength() != attrs2.getLength())
				return false;
			
			for (int i = 0; i < attrs1.getLength(); ++i)
	        {
	            Node att = attrs1.item(i);
	            String attname = att.getNodeName();
	            String value = att.getNodeValue();
	            
	            Node att2 = attrs2.item(i);
	            String attname2 = att2.getNodeName();
	            String value2 = att2.getNodeValue();
	            
	            if (!attname.equals(attname2))
	            	return false;
	            
	            if (!value.equals(value2))
	            	return false;
	        }
			
			List<Node> listc1 = util.getChildNodes(n1);
			List<Node> listc2 = util.getChildNodes(n2);
			
			if (listc1.size() != listc2.size())
				return false;
			
			for (int i = 0; i < listc1.size(); i++)
			{
				if (!isSameNode(listc1.get(i), listc2.get(i)))
					return false;
			}
			
			return true;
		}
		
		return false;
	}
}
