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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.everest.page.pageexport.style.DocxStyleUtil;
import com.globalsight.everest.page.pageexport.style.docx.Style;
import com.globalsight.everest.page.pageexport.style.docx.StyleStyle;
import com.globalsight.ling.docproc.extractor.msoffice2010.WordExtractor;
import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;

public class DocumentUtil 
{
	private static List<String> IGNORE_ATT = new ArrayList<String>();
	static
	{
		IGNORE_ATT.add("w:lang");
		IGNORE_ATT.add("w:bCs");
		IGNORE_ATT.add("w:iCs");
		IGNORE_ATT.add("w:szCs");
		IGNORE_ATT.add("w:w");
		IGNORE_ATT.add("w:spacing");
		IGNORE_ATT.add("w:shd");
	}
	
	private static List<String> IGNORE_FONT_ATT = new ArrayList<String>();
	static
	{
		IGNORE_FONT_ATT.add("w:cs");
		IGNORE_FONT_ATT.add("w:hint");
		IGNORE_FONT_ATT.add("w:eastAsia");
	}
	
	private XmlUtil util = new XmlUtil();
	private WordExtractor wordExtractor = null;

	public void handle(Document document)
	{
		removeSpellError(document);
		removeSmartTags(document);
		mergeSameNode(getWr(document));
		handleStyle(document);
		mergeNodes(getWr(document));
		handleHyperlink(document);
		handleInsDelStyle(document);
		
        mergeNodes(getWr(document));
	}
	
	private void handleInsDelStyle(Document document)
	{
		//do it again to reduce tag
		for (int i = 0; i < 3; i++)
		{
			mergeNoContentWrInIns(document);
			mergeNodes(getWr(document));
			handleInsStyle(document);
			handleDelStyle(document);
		}
	}
	
	private void removeSpellError(Document document)
	{
		List<Node> ns = util.getNodes(document, "w:proofErr");
		
		for (Node node : ns)
		{
			node.getParentNode().removeChild(node);
		}
	}
	
	private void handleStyle(Document document)
	{
		handleSimpleFld(document);
		handleFldChar(document);
		mergeNoContentWr(document);
		handleInternalStyle(document);
		handleHyperlink(document);
		handleComment(document);
		handleBookmark(document);
	}
	
	/**
	 * Remove smart tag can reduce the number of tag. Trados removed it too.
	 */
	private void removeSmartTags(Document document)
	{
		List<Node> ns =  util.getNodes(document, "w:smartTag");
		for (Node n : ns)
		{
			util.removeNode(n);
		}
	}
	
	private void handleInternalStyle(Document document)
	{
		List<String> internals = wordExtractor.getInternals();
		if (internals != null && internals.size() > 0)
		{
			List<Node> wrs =  util.getNodes(document, "w:r");
			for (Node wr : wrs)
			{
				List<Node> rStyles = util.getNodes(wr, "w:rStyle");
				for (Node r : rStyles)
				{
					NamedNodeMap attrs = r.getAttributes();
			        for (int j = 0; j < attrs.getLength(); ++j)
			        {
			            Node att = attrs.item(j);
			            String attname = att.getNodeName();
			            String value = att.getNodeValue();

			            if ("w:val".equals(attname) && internals.indexOf(value) > -1)
			            {
			            	StyleStyle s = new StyleStyle();
			    			s.removeStyle(r);
			            }
			        }
				}
			}
		}
	}
	
	private void handleHyperlinkWithAtt(Node n)
	{
		NodeList ns = n.getChildNodes();
		if (ns.getLength() != 1)
			return;
		
		Node wr = ns.item(0);
		if (wr == null || !"w:r".equals(wr.getNodeName()))
			return;
		
		Node wt = util.getNode(wr, "w:t");
		if (wt == null)
			return;
		
		util.removeNode(n);
		List<Node> cs = util.getChildNodes(wt);
		for (Node c : cs)
		{
			wt.removeChild(c);
			n.appendChild(c);
		}
		
		wt.appendChild(n);
	}
	
	private void handleBookmark(Document document)
	{
		List<Node> ns = util.getNodes(document, "w:bookmarkStart");
		for (Node n : ns)
		{
			Node id = util.getAttribute(n, "w:id");
			if (id == null)
				continue;
			
			Node next = n.getNextSibling();
			if (next == null)
				continue;
			
			if ("w:bookmarkEnd".equals(next.getNodeName()))
			{
				Node id2 = util.getAttribute(next, "w:id");
				if (id2 != null && id2.getNodeValue().equals(id.getNodeValue()))
				{
					n.getParentNode().removeChild(n);
					next.getParentNode().removeChild(next);
				}
				
				continue;
			}
		}
		
	}
	
	private List<Node> getNextWrs(Node n)
	{
		List<Node> ns = new ArrayList<Node>();
		
		Node next = n.getNextSibling();
		while (next != null && "w:r".equals(next.getNodeName()))
		{
			ns.add(next);
			next = next.getNextSibling();
		}
		
		return ns;
	}
	
	private boolean hasTextNode(Node wr)
	{
		return wr.getTextContent().length() > 0;
	}
	
	private List<Node> getPrefixNoContentWr(Node wr)
	{
		List<Node> wrs = new ArrayList<Node>();
		wr = wr.getPreviousSibling();
		
		while (wr != null)
		{
			Node wt = util.getNode(wr, "w:t", false);
			if (wt != null)
				return wrs;
			
			if (!hasTextNode(wr))
			{
				wrs.add(wr);
			}
			
			wr = wr.getNextSibling();
		}
		
		return new ArrayList<Node>();
	}
	
	private List<Node> getNoContentWr(Node wr)
	{
		List<Node> wrs = new ArrayList<Node>();
		wr = wr.getNextSibling();
		
		while (wr != null)
		{
			Node wt = util.getNode(wr, "w:t", false);
			if (wt != null)
				return wrs;
			
			int i = 0;
			Node wt2 = util.getNode(wr, "w:instrText", true);
            if (wt2 != null) 
                return wrs;
			
			if (!hasTextNode(wr))
			{
				wrs.add(wr);
			}
			
			wr = wr.getNextSibling();
		}
		
		return new ArrayList<Node>();
	}
	
	private void mergeNoContentWr(Document document)
	{
		List<Node> wrs = getWr(document);
		
		for (Node wr : wrs)
		{
			Node wt = util.getNode(wr, "w:t", false);
			if (wt != null)
			{
				List<Node> nwrs = getNoContentWr(wr);
				
				if (nwrs.size() > 0)
				{
					Element em = wr.getOwnerDocument().createElement("wr");
					for (Node nwr : nwrs)
					{
						nwr.getParentNode().removeChild(nwr);
						em.appendChild(nwr);
					}
					
					wt.appendChild(em);
				}
			}
		}
		
	}
	
	private void mergeNoContentWrInIns(Document document)
	{
		List<Node> ins = util.getNodes(document, "w:ins");
		for (Node n : ins)
		{
			List<Node> wrs = new ArrayList<Node>();
			getWr(n, wrs);
			
			for (Node wr : wrs)
			{
				Node wt = util.getNode(wr, "w:t", false);
				if (wt != null)
				{
					
					List<Node> nwrs = getPrefixNoContentWr(wr);
					if (nwrs.size() > 0)
					{
						Element em = wr.getOwnerDocument().createElement("wr");
						for (Node nwr : nwrs)
						{
							nwr.getParentNode().removeChild(nwr);
							em.appendChild(nwr);
						}
						
						wt.insertBefore(em, wt.getFirstChild());
					}
					
					nwrs = getNoContentWr(wr);
					
					if (nwrs.size() > 0)
					{
						Element em = wr.getOwnerDocument().createElement("wr");
						for (Node nwr : nwrs)
						{
							nwr.getParentNode().removeChild(nwr);
							em.appendChild(nwr);
						}
						
						wt.appendChild(em);
					}
				}
			}
		}
		List<Node> wrs = getWr(document);
		
		for (Node wr : wrs)
		{
			Node wt = util.getNode(wr, "w:t", false);
			if (wt != null)
			{
				List<Node> nwrs = getNoContentWr(wr);
				
				if (nwrs.size() > 0)
				{
					Element em = wr.getOwnerDocument().createElement("wr");
					for (Node nwr : nwrs)
					{
						nwr.getParentNode().removeChild(nwr);
						em.appendChild(nwr);
					}
					
					wt.appendChild(em);
				}
			}
		}
		
	}
	
	//<w:r w:rsidR="008106EF">
	//	<w:fldChar w:fldCharType="begin" />
	//</w:r>
	//<w:r w:rsidR="008106EF">
	//	<w:instrText xml:space="preserve"> HY</w:instrText>
	//</w:r>
	//<w:r w:rsidR="008106EF">
	//	<w:instrText xml:space="preserve">PERLINK "http://cdn.tripadvisor.com/pdfs/email/32NewPromoteAwards_US.pdf" \t "_blank" </w:instrText>
	//</w:r>
	//<w:r w:rsidR="008106EF">
	//	<w:fldChar w:fldCharType="separate" />
	//</w:r>
	//<w:r w:rsidRPr="00A02F53">
	//	<w:rPr>
	//		<w:rStyle w:val="Hyperlink" />
	//	</w:rPr>
	//	<w:t>new tip sheet</w:t>
	//</w:r>
	private void handleFldChar(Document document)
	{
		List<Node> ns = util.getNodes(document, "w:fldChar");
		for (Node n : ns)
		{
			Node att = util.getAttribute(n, "w:fldCharType");
			List<Node> prefix = new ArrayList<Node>();
			
			if (att != null && "begin".equals(att.getNodeValue()))
			{
				prefix.clear();
				
				Node wr = n.getParentNode();
				if (wr == null || !"w:r".equals(wr.getNodeName()))
					continue;
				
				prefix.add(wr);
				
				boolean found = false;
				Node next = wr.getNextSibling();
				while (true)
				{
					prefix.add(next);
					
					next = next.getNextSibling();
					if (next == null || !"w:r".equals(next.getNodeName()))
					{
						break;
					}
					
					Node f = util.getNode(next, "w:fldChar", false);
					if (f != null)
					{
						att = util.getAttribute(f, "w:fldCharType");
						if (att != null && "separate".equals(att.getNodeValue()))
						{
							found = true;
							prefix.add(next);
							
							List<Node> wrs = getNextWrs(next);
							if (wrs.size() > 1)
							{
								mergeSameNode(wrs);
								mergeNodes(getNextWrs(next));
							}
							
							next = next.getNextSibling(); 
							
							break;
						}
					}
					else
					{
						f = util.getNode(next, "w:instrText", false);
						if (f == null)
							break;
					}
				}
				
				if (!found)
				{
					continue;
				}
				
				if (next == null || !"w:r".equals(next.getNodeName()))
				{
					continue;
				}
				
				Node wt = util.getNode(next, "w:t", false);
				if (wt == null)
				{
					continue;
				}
				
				Node end = next.getNextSibling();
				if (end == null)
				{
					continue;
				}
				
				Node f = util.getNode(end, "w:fldChar", false);
				if (f != null)
				{
					att = util.getAttribute(f, "w:fldCharType");
					if (att != null && "end".equals(att.getNodeValue()))
					{
						// start
						Element start = wt.getOwnerDocument().createElement("fldChar");
						start.setAttribute("type", "begin");
						for (Node p : prefix)
						{
							p.getParentNode().removeChild(p);
							start.appendChild(p);
						}
						wt.insertBefore(start, wt.getFirstChild());
						
						// end
						Element endNode = wt.getOwnerDocument().createElement("fldChar");
						endNode.setAttribute("type", "end");
						end.getParentNode().removeChild(end);
						endNode.appendChild(end);
						wt.appendChild(endNode);
					}
				}
			}
		}
	}
	
	private void handleSimpleFld(Document document)
	{
        List<Node> ns = util.getNodes(document, "w:fldSimple");
		
		for (Node n : ns)
		{
			Node wr = n.getFirstChild();
			if (!"w:r".equals(wr.getNodeName()) || wr.getNextSibling() != null)
				continue;
			
			Node wt = util.getNode(wr, "w:t");
			if (wt == null)
				continue;
			
			util.removeNode(n);
			List<Node> cs = util.getChildNodes(wt);
			for (Node c : cs)
			{
				wt.removeChild(c);
				n.appendChild(c);
			}
			
			wt.appendChild(n);
		}
	}
	
	private void handleComment(Document document)
	{
		List<Node> ns = util.getNodes(document, "w:commentRangeStart");
		
		for (Node n : ns)
		{
			Node id = util.getAttribute(n, "w:id");
			if (id == null)
				continue;
			
			List<Node> wrs = getNextWrs(n);
			if (wrs.size() > 1)
			{
				mergeSameNode(wrs);
				mergeNodes(getNextWrs(n));
			}
			
			wrs = getNextWrs(n);
			if (wrs.size() != 1)
				continue;
			
			Node wr = n.getNextSibling();
			Node end = wr.getNextSibling();
			if (end == null || !"w:commentRangeEnd".equals(end.getNodeName()))
				continue;
			
			Node id2 = util.getAttribute(end, "w:id");
			if (id2 == null || !id2.getNodeValue().equals(id.getNodeValue()))
				continue;
				
			Node wt = util.getNode(wr, "w:t");
			if (wt == null)
				continue;
			
			n.getParentNode().removeChild(n);
			end.getParentNode().removeChild(end);
			
			Element comment = wt.getOwnerDocument().createElement("comment");
			comment.setAttribute("w:id", id.getNodeValue());
			
			List<Node> cs = util.getChildNodes(wt);
			for (Node c : cs)
			{
				wt.removeChild(c);
				comment.appendChild(c);
			}
			
			wt.appendChild(comment);
		}
	}
	
	// <w:del w:id="1" w:author="Nicole Chen" w:date="2014-07-07T18:30:00Z">
	// <w:r w:rsidDel="00EA6D88">
	// <w:delText xml:space="preserve">Globalization</w:delText>
	// </w:r>
	// </w:del>
	private void handleDelStyle(Document document)
	{
		List<Node> ns =  util.getNodes(document, "w:del");
		for (Node n : ns)
		{
			boolean isPrevious = true;
			Node wt = null;
			Node n1 = n.getPreviousSibling();
			if (n1 != null )
			{
				String name = n1.getNodeName();
				if (name.equalsIgnoreCase("w:r"))
				{
					wt = util.getNode(n1, "w:t", false);
				}
			}
			
			if (wt == null)
			{
				isPrevious = false;
				n1 = n.getNextSibling();
				if (n1 != null )
				{
					String name = n1.getNodeName();
					if (name.equalsIgnoreCase("w:r"))
					{
						wt = util.getNode(n1, "w:t", false);
					}
				}
			}
			
			if (wt == null)
				continue;
			
			Element em = n.getOwnerDocument().createElement("wr");
			Node p = n.getParentNode();
			if (isPrevious)
			{
				wt.appendChild(em);
			}
			else
			{
				Node fc = wt.getFirstChild();
				if (fc != null)
				    wt.insertBefore(em, wt.getFirstChild());
				else
					wt.appendChild(em);
			}
			p.removeChild(n);
			em.appendChild(n);
		}
	}
	
	// <w:ins w:id="2" w:author="Nicole Chen" w:date="2014-07-07T18:30:00Z">
	// <w:r w:rsidR="00EA6D88">
	// <w:rPr>
	// <w:rFonts w:hint="eastAsia" />
	// </w:rPr>
	// <w:t>Localization</w:t>
	// </w:r>
	// <w:r w:rsidR="00EA6D88">
	// <w:t xml:space="preserve"></w:t>
	// </w:r>
	// </w:ins>
	// <w:r>
	// <w:t>Management System, helping you automate and manage the globalization
	// process.</w:t>
	// </w:r>
	private void handleInsStyle(Document document)
	{
		List<Node> ins = util.getNodes(document, "w:ins");
		for (Node n : ins)
		{
			NodeList ns = n.getChildNodes();
			if (ns.getLength() != 1)
				continue;
			
			Node wr = ns.item(0);
			if (wr == null || !"w:r".equals(wr.getNodeName()))
				continue;
			
			Node wt = util.getNode(wr, "w:t");
			if (wt == null)
			{
				boolean handle = false;
				// no content. merge to other wr.
				Node pwr = n.getPreviousSibling();
				if (pwr != null)
				{
					Node pwt = util.getNode(pwr, "w:t");
					if (pwt != null)
					{
						Element em = wr.getOwnerDocument().createElement("wr");
						em.appendChild(n);
						pwt.appendChild(em);
						handle = true;
					}
				}
				
				if (!handle)
				{
					pwr = n.getNextSibling();
					if (pwr != null)
					{
						Node pwt = util.getNode(pwr, "w:t");
						if (pwt != null)
						{
							Element em = wr.getOwnerDocument().createElement("wr");
							em.appendChild(n);
							pwt.insertBefore(em, pwt.getFirstChild());
						}
					}
				}
				
				continue;
			}
			
			util.removeNode(n);
			List<Node> cs = util.getChildNodes(wt);
			for (Node c : cs)
			{
				wt.removeChild(c);
				n.appendChild(c);
			}
			
			wt.appendChild(n);
		}
	}
	
	private void handleHyperlink(Document document)
	{
		List<Node> ns = util.getNodes(document, "w:hyperlink");
		for (Node n : ns)
		{
			if (n.getAttributes().getLength() > 0)
			{
				handleHyperlinkWithAtt(n);
				continue;
			}
			
			List<Node> wrs = new ArrayList<Node>();
			util.getAllNodes(n, "w:r", wrs);
			
			for (Node wr : wrs)
			{
				Node rPr = util.getNode(wr, "w:rPr", false);
				if (rPr == null)
				{
					rPr = wr.getOwnerDocument().createElement("w:rPr");
					wr.insertBefore(rPr, wr.getFirstChild());
				}
				
				Node hyperlink = null;
				
				NodeList list = rPr.getChildNodes();
			    for (int i = 0; i < list.getLength() && hyperlink == null; i++)
			    {
			    	Node ni = list.item(i);
			    	if (ni.getNodeName().equals("w:rStyle"))
			    	{
			    		NamedNodeMap atts = ni.getAttributes();
						for (int j = 0; j < atts.getLength(); j++)
						{
							Node att = atts.item(j);
							if ("w:val".equals(att.getNodeName()) && "Hyperlink".equals(att.getNodeValue()))
							{
								hyperlink = ni;
							}
						}
			    	}
			    }
			    
			    if (hyperlink == null)
			    {
			    	Element h = wr.getOwnerDocument().createElement("w:rStyle");
			    	h.setAttribute("w:val", "Hyperlink");
			    	rPr.appendChild(h);
			    }
			}
			
			util.removeNode(n);
		}
	}
	
	private void handleNoBreakHyphen(List<Node> ns)
	{
		for (int i = 0; i < ns.size() - 1; i++)
		{
			Node n1 = ns.get(i);
			Node n2 = ns.get(i + 1);
			
			if (!n2.equals(n1.getNextSibling()))
				continue;
			
			if (isHyphenWr(n2))
			{
				Node wt2 = util.getNode(n2, "w:t", false);
				if (wt2 == null)
				{
					Node wt1 = util.getNode(n1, "w:t", false);
					if (wt1 != null)
					{
						Node n = getLastTextNode(wt1);
						if (n != null)
						{
							n.setTextContent(n.getTextContent() + "-");
						}
						else
						{
							wt1.appendChild(wt1.getOwnerDocument().createTextNode("-"));
						}
						
						n2.getParentNode().removeChild(n2);
						ns.remove(i + 1);
						i--;
						continue;
					}
				}
				else
				{
					Node n = getFirstTextNode(wt2);
					if (n != null)
					{
						n.setTextContent("-" + n.getTextContent());
					}
					else
					{
						wt2.insertBefore(wt2.getOwnerDocument().createTextNode("-"), wt2.getFirstChild());
					}
					
					Node hyphen = util.getNode(n2, "w:noBreakHyphen", false);
					hyphen.getParentNode().removeChild(hyphen);
				}
			}
		}
	}
	
	private List<Node> getWr(Document document)
	{
		Node n = document.getFirstChild();
		
        List<Node> ns = new ArrayList<Node>();
        getWr(n, ns);
		return ns;
	}
	
	private void getWr(Node root, List<Node> ns)
	{
		Node n = root.getFirstChild();
		if (n == null)
			return;
		
		while (n != null)
		{
			if ("w:r".equals(n.getNodeName()) && !wordExtractor.isUnextractWr(n))
			{
				ns.add(n);
			}
			
			n = n.getNextSibling();
		}
		
		n = root.getFirstChild();
		while (n != null)
		{
			getWr(n, ns);
			n = n.getNextSibling();
		}
	}
	
	private void mergeSameNode(List<Node> ns)
	{
		handleNoBreakHyphen(ns);
		
		for (int i = 0; i < ns.size() - 1; i++)
		{
			Node n1 = ns.get(i);
			Node n2 = ns.get(i + 1);
			
			if (!n2.equals(n1.getNextSibling()))
				continue;
			
			if (canMergeAsSame(n1, n2))
			{
				mergeTo(n1, n2);
			}
		}
	}
	
	private void mergeNodes(List<Node> ns)
	{
		for (int i = 0; i < ns.size() - 1; i++)
		{
			Node n1 = ns.get(i);
			Node n2 = ns.get(i + 1);
			
			if (!n2.equals(n1.getNextSibling()))
				continue;
			
			if (canMerge(n1, n2))
			{
				mergeTo(n1, n2);
			}
		}
	}
	
	private Node getFirstTextNode(Node node)
	{
		Node c = node.getFirstChild();
		if (c == null)
			return null;
		
		
		if (Node.TEXT_NODE  == c.getNodeType())
			return c;
		
		Node r = getFirstTextNode(c);
		while (r == null && c != null)
		{
			c = c.getNextSibling();
			r = getFirstTextNode(c);
		}
		
		return r;
	}
	
	private Node getLastTextNode(Node node)
	{
		Node c = node.getLastChild();
		if (c == null)
			return null;
		
		
		if (Node.TEXT_NODE  == c.getNodeType())
			return c;
		
		Node r = getLastTextNode(c);
		while (r == null && c != null)
		{
			c = c.getPreviousSibling();
			r = getFirstTextNode(c);
		}
		
		return r;
	}
	
	private void addrPr(Node att, Node root)
	{
		Node n = util.getNode(root, "w:rPr");
		if (n == null)
		{
            n = root.getOwnerDocument().createElement("w:rPr");
            root.insertBefore(n, root.getFirstChild());
		}
		
		Node a = att.cloneNode(true);
		n.appendChild(a);
	}
	
	private boolean isHyphenWr(Node wr)
	{
		return util.getNode(wr, "w:noBreakHyphen", false) != null;
	}
	
	private boolean isIgnoreAtt(Node n)
	{
		if (IGNORE_ATT.indexOf(n.getNodeName()) > -1)
			return true;
		
		if ("w:rFonts".equals(n.getNodeName()))
		{
			List<Node> att = util.getAttributes(n);
			
			for (Node a1 : att)
			{
				String attname = a1.getNodeName();
				if (IGNORE_FONT_ATT.indexOf(attname) < 0)
					return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean canMergeAsComment(Node n1, Node n2)
	{
		Node wt1 = util.getNode(n1, "w:t", false);
		Node commentReference = util.getNode(n2, "w:commentReference", false);
		
		if (wt1 == null || commentReference == null)
			return false;
		
		List<Node> cs = util.getChildNodes(wt1);
		if (cs.size() > 0)
		{
			Node comment = cs.get(cs.size() - 1);
			if ("comment".equals(comment.getNodeName()))
			{
				Node id = util.getAttribute(comment, "w:id");
				if (id == null)
					return false;
				
				Node id2 = util.getAttribute(commentReference, "w:id");
				if (id2 == null)
					return false;
				
				if (id.getNodeValue().equals(id2.getNodeValue()))
				{
					n2.getParentNode().removeChild(n2);
					Element commentContent = n2.getOwnerDocument().createElement("commentContent");
					
					List<Node> atts = util.getAttributes(n2);
					for (Node att : atts)
					{
						commentContent.setAttribute(att.getNodeName(), att.getNodeValue());
					}
					
					List<Node> cs2 = util.getChildNodes(n2);
					for (Node c : cs2)
					{
						c.getParentNode().removeChild(c);
						commentContent.appendChild(c);
					}
					
					comment.appendChild(commentContent);
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean canMergeAsSame(Node n1, Node n2)
	{
		// has merged. 
		if (canMergeAsComment(n1, n2))
			return false;
		
		if (util.getNode(n2, "w:br", false) != null)
			return false;
		
		Node tab = util.getNode(n2, "w:tab", false);
		if (util.getNode(n2, "w:tab", false) != null)
		{
			Node sib = tab.getPreviousSibling();
			if (sib == null || !"w:t".equals(sib.getNodeName()))
				return false;
		}
		
		Node wt1 = util.getNode(n1, "w:t", false);
		Node wt2 = util.getNode(n2, "w:t", false);
		
		if (wt1 == null || wt2 == null)
			return false;
		
		Node rPr1 = util.getNode(n1, "w:rPr", false);
		Node rPr2 = util.getNode(n2, "w:rPr", false);
		
		List<Node> list1 = util.getChildNodes(rPr1);
		List<Node> list2 = util.getChildNodes(rPr2);
		
		for (Node att1 : list1)
		{
			boolean found = false;
			for (Node att2 : list2)
			{
				int n = isSameNode(att1, att2);
				
				if (n == 0)
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
					att1.getParentNode().removeChild(att1);
					continue;
				}
					
				return false;
			}
		}
		
		for (Node att2 : list2)
		{
			if (isIgnoreAtt(att2))
			{
				att2.getParentNode().removeChild(att2);
				continue;
			}
			
			return false;
		}
		
		return true;
	}
	
	private boolean canMerge(Node n1, Node n2)
	{
		if (util.getNode(n2, "w:br", false) != null)
			return false;
		
		Node tab = util.getNode(n2, "w:tab", false);
		if (util.getNode(n2, "w:tab", false) != null)
		{
			Node sib = tab.getPreviousSibling();
			if (sib == null || !"w:t".equals(sib.getNodeName()))
				return false;
		}
		
		Node wt1 = util.getNode(n1, "w:t", false);
		Node wt2 = util.getNode(n2, "w:t", false);
		
		if (wt1 == null || wt2 == null)
			return false;
		
		Node rPr1 = util.getNode(n1, "w:rPr", false);
		Node rPr2 = util.getNode(n2, "w:rPr", false);
		
        List<Node> rprs1 = new ArrayList<Node>();
        List<Node> rprs2 = new ArrayList<Node>();
        
        List<Node> rprs11 = new ArrayList<Node>();
        List<Node> rprs22 = new ArrayList<Node>();
        
        List<Node> removeRprs = new ArrayList<Node>();
        List<Node> addRprs = new ArrayList<Node>();
        
		List<Node> list1 = util.getChildNodes(rPr1);
		List<Node> list2 = util.getChildNodes(rPr2);
		
		Map<String, Style> m = getAllStylesMap();
		
		for (Node att1 : list1)
		{
			boolean found = false;
			
			for (Node att2 : list2)
			{
				int n = isSameNode(att1, att2);
				
				if (n == 0)
				{
					list2.remove(att2);
					found = true;
					break;
				}
				else if (n == 1)
				{
					Style s = m.get(att2.getNodeName());
					if (s != null)
					{
						addRprs.add(att1);
						found = true;
						break;
					}
				}
			}
			
			if (!found)
			{
				if (isIgnoreAtt(att1))
				{
					att1.getParentNode().removeChild(att1);
					continue;
				}
				
				rprs1.add(att1);
			}
		}
		
		for (Node att2 : list2)
		{
			if (isIgnoreAtt(att2))
			{
				att2.getParentNode().removeChild(att2);
				continue;
			}
			
			rprs2.add(att2);
		}
		
		for (Node att : rprs1)
		{
			Style s = m.get(att.getNodeName());
			if (s == null)
			{
				rprs11.add(att);
			}
		}
		rprs1.removeAll(rprs11);
		
		for (Node att : rprs2)
		{
			Style s = m.get(att.getNodeName());
			if (s == null)
			{
				rprs22.add(att);
			}
		}
		rprs2.removeAll(rprs22);
		
		for (Node r : removeRprs)
		{
			r.getParentNode().removeChild(r);
		}
		
		for (Node r : addRprs)
		{
			addrPr(r, n2);
		}
		
		List<Style> styles = DocxStyleUtil.getAllStyles();
		
		for (Node att : rprs1)
		{
			Style s = Style.getStyle(att, styles);
			s.removeStyle(att);
		}
		
		for (Node att : rprs2)
		{
			Style s = Style.getStyle(att, styles);
			s.removeStyle(att);
		}
		
		addAtt(rprs11, wt1);
		addAtt(rprs22, wt2);
		
		return true;
	}
	
	private void addAtt(List<Node> atts, Node at)
	{
		if (atts.size() == 0)
			return ;
		
		Element e = at.getOwnerDocument().createElement("rpr");
		Element ec = at.getOwnerDocument().createElement("rprChild");
		e.appendChild(ec);
		
		for (Node att : atts)
		{
			ec.appendChild(att);
		}
		
		List<Node> cs = util.getChildNodes(at);
		
		for (Node c : cs)
		{
			at.removeChild(c);
			e.appendChild(c);
		}
		
		at.appendChild(e);
	}
	
	private Map<String, Style> getAllStylesMap()
	{
		Map<String, Style> styles = new HashMap<String, Style>();
		List<Style> s = DocxStyleUtil.getAllStyles();
		for (Style st : s)
		{
			styles.put(st.getAddNodeName(), st);
		}
        return styles;
	}
	
	private void mergeText(Node n1, Node n2)
	{
		List<Node> cs = util.getChildNodes(n1);
		Node f = n2.getFirstChild();
		
		for (int i = 0; i < cs.size() - 1; i++)
		{
			Node c = cs.get(i);
			n2.insertBefore(c, f);
		}
		
		Node c = cs.get(cs.size() - 1);
		if (Node.TEXT_NODE != c.getNodeType() && isSameNode(c, f) == 0)
		{
			mergeText(c, f);
		}
		else
		{
			n2.insertBefore(c, f);
		}
	}
	
	private void mergeTo(Node n1, Node n2)
	{
		Node wt1 = util.getNode(n1, "w:t", false);
		Node wt2 = util.getNode(n2, "w:t", false);
		
		if (wt1 != null && wt2 != null)
		{
			mergeText(wt1, wt2);
			
			Node rPr2 = util.getNode(n2, "w:rPr", false);
			if (rPr2 == null)
			{
				Element e = n2.getOwnerDocument().createElement("w:rPr");
				n2.insertBefore(e, wt2);
			}
			
			Element e = (Element) wt2;
			e.setAttribute("xml:space", "preserve");
			
			if (util.getNode(n1, "w:br", false) != null)
			{
				Element br = n2.getOwnerDocument().createElement("w:br");
				n2.insertBefore(br, wt2);
			}
			
			if (util.getNode(n1, "w:tab", false) != null)
			{
				Element br = n2.getOwnerDocument().createElement("w:tab");
				n2.insertBefore(br, wt2);
			}
				
			n1.getParentNode().removeChild(n1);
		}
	}
	
	private boolean isSameFont(Node n1, Node n2)
	{
		List<Node> att1 = util.getAttributes(n1);
		List<Node> att2 = util.getAttributes(n2);
		
		for (Node a1 : att1)
		{
			String attname = a1.getNodeName();
			if (IGNORE_FONT_ATT.indexOf(attname) > -1)
				continue;
			
			boolean found = false;
			for (Node a2 : att2)
			{
				String value = a1.getNodeValue();
				
				String attname2 = a2.getNodeName();
	            String value2 = a2.getNodeValue();
	            
	            if (attname.equals(attname2))
	            {
	            	if (value == null && value2 == null 
	            			|| value != null && value.equals(value2))
	            	{
	            		found = true;
	            		att2.remove(a2);
	            		break;
	            	}
	            }
			}
			
			if (!found)
				return false;
		}
		
		for (Node a2 : att2)
		{
			String attname = a2.getNodeName();
			if (IGNORE_FONT_ATT.indexOf(attname) < 0)
				return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param n1
	 * @param n2
	 * @return 0: is same
	 *         1: is similar
	 *        -1: different
	 */
	private int isSameNode(Node n1, Node n2)
	{
		if (n1.getNodeType() != n2.getNodeType())
			return -1;
		
		if (n1.getNodeName() != n2.getNodeName())
			return -1;
		
		NamedNodeMap attrs1 = n1.getAttributes();
		NamedNodeMap attrs2 = n2.getAttributes();
		
		int result = 0;
		if (attrs1 != null)
		{
			if (attrs2 == null)
				return -1;
			
			if ("w:rFonts".equals(n2.getNodeName()))
			{
				if (isSameFont(n1, n2))
					return 0;
				
				result = 1;
			}
			else
			{
				if (attrs1.getLength() != attrs2.getLength())
					return -1;
				
				for (int i = 0; i < attrs1.getLength(); ++i)
		        {
		            Node att = attrs1.item(i);
		            String attname = att.getNodeName();
		            String value = att.getNodeValue();
		            
		            Node att2 = attrs2.item(i);
		            String attname2 = att2.getNodeName();
		            String value2 = att2.getNodeValue();
		            
		            if (!attname.equals(attname2))
		            	return -1;
		            
		            if (!value.equals(value2))
		            {
		            	if ("w:val".equals(attname))
		            	{
		            		result = 1;
		            	}
		            	else
		            	{
		            		return -1;
		            	}
		            }
		        }
			}
		}
		
		return result;
	}
	
	public WordExtractor getWordExtractor() {
		return wordExtractor;
	}

	public void setWordExtractor(WordExtractor wordExtractor) {
		this.wordExtractor = wordExtractor;
	}
}
