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
package com.globalsight.ling.docproc.extractor.msoffice2010.word;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;

public class ChartSpaceUtil 
{
	private XmlUtil util = new XmlUtil();
	
	public void handle(Document document)
	{
		mergeNodes(document);
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
				else
				{
					if (att1.getNodeName().equals(att2.getNodeName()))
					{
						found = true;
						list2.remove(att2);
						
						if ("0".equals(att2.getNodeValue()))
						{
							addAtt(att1, wt1);
							
							break;
						}
						else
						{
							addAtt(att2, wt2);
							att2.setNodeValue(att1.getNodeValue());
							break;
						}
					}
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
		
		for (Node att2 : list2)
		{
			if (isIgnoreAtt(att2))
			{
				((Element)n2).removeAttribute(att2.getNodeName());
				continue;
			}
			
			rprs2.add(att2);
		}
		
		for (Node att : rprs1)
		{
			addAtt(att, wt1);
			((Element)n1).removeAttribute(att.getNodeName());
		}
		
		for (Node att : rprs2)
		{
			addAtt(att, wt2);
			((Element)n2).removeAttribute(att.getNodeName());
		}
		
		return true;
	}
	
	private void addAtt(Node att, Node at)
	{
		Element e = at.getOwnerDocument().createElement("atStyle");
		e.setAttribute(att.getNodeName(), att.getNodeValue());
		
		List<Node> cs = util.getChildNodes(at);
		
		for (Node c : cs)
		{
			at.removeChild(c);
			e.appendChild(c);
		}
		
		at.appendChild(e);
	}
}
