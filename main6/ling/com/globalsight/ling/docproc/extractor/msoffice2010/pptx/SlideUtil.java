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
package com.globalsight.ling.docproc.extractor.msoffice2010.pptx;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;

public class SlideUtil 
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
		handleBr(document);
		mergeSameNodes(document);
		mergeNodes(document);
	}
	
	private void handleBr(Document document)
	{
        List<Node> ns = util.getNodes(document, "a:br");
		
		for (int i = 0; i < ns.size(); i++)
		{
			Node br = ns.get(i);
			
			Node n1 = br.getPreviousSibling();
			Node n2 = br.getNextSibling();
			
			if (n1 != null && n2 != null && "a:r".equals(n1.getNodeName()) && "a:r".equals(n2.getNodeName()))
			{
				Node at = util.getNode(n1, "a:t");
				if (at != null)
				{
					br.getParentNode().removeChild(br);
					Element e = at.getOwnerDocument().createElement("atBr");
					e.setAttribute("styleType", "br");
					e.appendChild(br);
					at.appendChild(e);
				}
			}
		}
	}
	
	private void mergeSameNodes(Document document)
	{
		List<Node> ns = util.getNodes(document, "a:r");
		
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
		List<Node> ns = util.getNodes(document, "a:r");
		
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
		if (!node2.equals(node1.getNextSibling()))
			return false;
		
		Node n1 = util.getNode(node1, "a:rPr", false);
		Node n2 = util.getNode(node2, "a:rPr", false);
		
		Node wt1 = util.getNode(n1.getParentNode(), "a:t", false);
		Node wt2 = util.getNode(n2.getParentNode(), "a:t", false);
		
		if (wt1 == null || wt2 == null)
			return false;
        
		List<Node> list1 = getAtts(n1);
		List<Node> list2 = getAtts(n2);
		
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
		
		for (Node att1 : list1)
		{
			boolean found = false;
			
			for (Node att2 : list2)
			{
				if (equalsAtt(att1, att2))
				{
					list2.remove(att2);
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				if (isIgnoreAtt(att1))
				{
					((Element)n1).removeAttribute(att1.getNodeName());
					continue;
				}
				
				return false;
			}
		}
		
		for (Node att2 : list2)
		{
			if (isIgnoreAtt(att2))
			{
				((Element)n2).removeAttribute(att2.getNodeName());
				continue;
			}
			
			return false;
		}
		
		return true;
	}
	
	private boolean canMerge(Node n1, Node n2)
	{
		if (!n2.equals(n1.getNextSibling()))
			return false;
		
		Node rPr1 = util.getNode(n1, "a:rPr", false);
		Node rPr2 = util.getNode(n2, "a:rPr", false);

		return isSameAtt(rPr1, rPr2);
	}
	
	private void mergeTo(Node n1, Node n2)
	{
		Node wt1 = util.getNode(n1, "a:t", false);
		Node wt2 = util.getNode(n2, "a:t", false);
		
		if (wt1 != null && wt2 != null)
		{
			List<Node> cs = util.getChildNodes(wt1);
			Node f = wt2.getFirstChild();
			
			for (int i = 0; i < cs.size() ; i++)
			{
				Node c = cs.get(i);
				wt2.insertBefore(c, f);
			}
			
			n1.getParentNode().removeChild(n1);
		}
	}
	
	private List<Node> getAtts(Node root) 
	{
		return util.getAttributes(root);
	}
	
	private boolean isIgnoreAtt(Node n)
	{
		String name = n.getNodeName();
		String value = n.getNodeValue();
		
		if ("baseline".equals(name) && "0".equals(value))
		{
			return true;
		}
		
		if ("smtClean".equals(name) && "0".equals(value))
		{
			return true;
		}
		
		if (IGNORE_ATTS.contains(name))
		{
			return true;
		}
		
		return false;
	}
	
	private boolean equalsAtt(Node att1, Node att2)
	{
		String name1 = att1.getNodeName();
		String value1 = att1.getNodeValue();
		
		String name2 = att2.getNodeName();
		String value2 = att2.getNodeValue();
		
		if (!name1.equals(name2))
			return false;
		
		if (value1 != null && !value1.equals(value2))
			return false;
		
		return true;
	}
	
	private boolean isSameAtt(Node n1, Node n2)
	{
        List<Node> rprs1 = new ArrayList<Node>();
        List<Node> rprs2 = new ArrayList<Node>();
        
		Node wt1 = util.getNode(n1.getParentNode(), "a:t", false);
		Node wt2 = util.getNode(n2.getParentNode(), "a:t", false);
		
		if (wt1 == null || wt2 == null)
			return false;
        
		List<Node> list1 = getAtts(n1);
		List<Node> list2 = getAtts(n2);
		
		List<Node> listc1 = util.getChildNodes(n1);
		List<Node> listc2 = util.getChildNodes(n2);
		
		for (Node att1 : list1)
		{
			boolean found = false;
			
			for (Node att2 : list2)
			{
				if (equalsAtt(att1, att2))
				{
					list2.remove(att2);
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				if (isIgnoreAtt(att1))
				{
					((Element)n1).removeAttribute(att1.getNodeName());
					continue;
				}
				
				rprs1.add(att1);
			}
		}
		
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
		
		for (Node att2 : list2)
		{
			if (isIgnoreAtt(att2))
			{
				((Element)n2).removeAttribute(att2.getNodeName());
				continue;
			}
			
			rprs2.add(att2);
		}
		
		List<Node> removedAtt = new ArrayList<Node>();
		for (Node att : rprs1)
		{
			String s = getStyleName(att);
			if (s != null)
			{
				addAtt(att, wt1, s);
				((Element)n1).removeAttribute(att.getNodeName());
				removedAtt.add(att);
			}
		}
		
		rprs1.removeAll(removedAtt);
		addAtt(rprs1, wt1, listc1);
		removeAtt(rprs1, n1, listc1);
		
		removedAtt = new ArrayList<Node>();
		for (Node att : rprs2)
		{
			String s = getStyleName(att);
			if (s != null)
			{
				addAtt(att, wt2, s);
				((Element)n2).removeAttribute(att.getNodeName());
				removedAtt.add(att);
			}
		}
		
		rprs2.removeAll(removedAtt);
		addAtt(rprs2, wt2, listc2);
		removeAtt(rprs2, n2, listc2);
		
		return true;
	}
	
	private String getStyleName(Node att)
	{
		String name = att.getNodeName();
		String value = att.getNodeValue();
		if ("b".equals(name) && "1".equals(value))
			return "bold";
		if ("i".equals(name) && "1".equals(value))
			return "italic";
		if ("u".equals(name) && "sng".equals(value))
			return "ulined";
		
		if ("baseline".equals(name) && "30000".equals(value))
			return "office-sup";
		if ("baseline".equals(name) && "-25000".equals(value))
			return "office-sub";
		
		return null;
	}
	
//	private String getStyleName(Node att)
//	{
//		if (att == null)
//			return null;
//		
//		
//	}
	
	private void addAtt(Node att, Node at, String style)
	{
		Element e = at.getOwnerDocument().createElement("atStyle");
		e.setAttribute(att.getNodeName(), att.getNodeValue());
		e.setAttribute("styleType", style);
		
		List<Node> cs = util.getChildNodes(at);
		
		for (Node c : cs)
		{
			at.removeChild(c);
			e.appendChild(c);
		}
		
		at.appendChild(e);
	}
	
	private void removeAtt(List<Node> rprs1, Node n1, List<Node> attcs)
	{
		for (Node att : rprs1)
		{
			((Element)n1).removeAttribute(att.getNodeName());
		}
		
//		for (Node att : attcs)
//		{
//			n1.removeChild(att);
//		}
	}
	private void addAtt(List<Node> atts, Node at, List<Node> attcs)
	{
		if (atts.size() == 0 && attcs.size() == 0)
			return ;
		
		Element e = at.getOwnerDocument().createElement("atStyle");
		
		for (Node att : atts)
		{
			e.setAttribute(att.getNodeName(), att.getNodeValue());
		}
		
		if (attcs.size() > 0)
		{
			Element ec = at.getOwnerDocument().createElement("atStyleChild");
			e.appendChild(ec);
			for (Node attc : attcs)
			{
			    // record the order of the attribute.
			    Element attce = (Element) attc;
			    Node previous = attce.getPreviousSibling();
			    String pName = previous == null ? "null" : previous.getNodeName();
			    attce.setAttribute("gs-Previous", pName);
			    
			    Node next = attce.getNextSibling();
                String pName2 = next == null ? "null" : next.getNodeName();
                attce.setAttribute("gs-next", pName2);
			}
			
			for (Node attc : attcs)
            {
                ec.appendChild(attc);
            }
		}
		
		List<Node> cs = util.getChildNodes(at);
		
		for (Node c : cs)
		{
			at.removeChild(c);
			e.appendChild(c);
		}
		
		at.appendChild(e);
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
