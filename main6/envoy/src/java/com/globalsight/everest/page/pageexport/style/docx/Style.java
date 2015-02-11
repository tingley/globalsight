package com.globalsight.everest.page.pageexport.style.docx;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.everest.page.pageexport.style.DocxStyleUtil;
import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;


/**
 * A abstract class that to handle the style node. For example <b> node in DOCX
 * document.xml file.
 * 
 */
public abstract class Style extends XmlUtil
{
    /**
     * Adds style to the DOCX document. Should not be null.
     * 
     * @param document
     *            the document that maybe have style node.
     * @return the number of found tag
     */
    public int addStyles(Document document)
    {
        NodeList bNodes = document.getElementsByTagName(getNodeName());
        int found = bNodes.getLength();
        List<Node> nodes = new ArrayList<Node>();

        for (int i = 0; i < found; i++)
        {
        	nodes.add(bNodes.item(i));
        }
        
        for (Node n : nodes)
        {
        	if (n != null)
        	{
        		handleStyleNode(n);
        	}
        }

        return found;
    }
    
    public void removeStyle(Node n)
    {
    	if (getAddNodeName() != n.getNodeName())
    		return;
    	
    	if (getAddNodeValue() != null)
		{
    		NamedNodeMap map = n.getAttributes();
			if (map == null)
			{
				return;
			}
			
			Node att = map.getNamedItem("w:val");
			if (att == null )
			{
				return;
			}
			
			if (!getAddNodeValue().equals(att.getNodeValue()))
			{
				return;
			}
		}
		
		Node rPr = n.getParentNode();
		if (rPr == null || !"w:rPr".equals(rPr.getNodeName()))
			return;
		
		Node wr = rPr.getParentNode();
		if (wr == null || !"w:r".equals(wr.getNodeName()))
			return;
		
		Node wt = getNode(wr, "w:t");
		if (wt != null)
		{
//			if (wt.getTextContent().trim().length() > 0)
//			{
				Element e = wt.getOwnerDocument().createElement(getNodeName());
				if (hasAttribute())
				{
					NamedNodeMap atts = n.getAttributes();
					for (int j = 0; j < atts.getLength(); j++)
					{
						Node att = atts.item(j);
						e.setAttribute(att.getNodeName(), att.getNodeValue());
					}
				}
				
				List<Node> cs = getChildNodes(wt);
				for (Node c : cs)
				{
					e.appendChild(c);
				}
				wt.appendChild(e);
//			}
		}
		
		removeNode(n);
    }
    
    public void removeStyles(Document document)
    {
    	List<Node> ns = getNodes(document, getAddNodeName());
		for (Node n : ns)
		{
			if (getAddNodeValue() != null && !getAddNodeValue().equals(n.getNodeValue()))
			{
				continue;
			}
			
			Node rPr = n.getParentNode();
			if (rPr == null || !"w:rPr".equals(rPr.getNodeName()))
				continue;
			
			Node wr = rPr.getParentNode();
			if (wr == null || !"w:r".equals(wr.getNodeName()))
				continue;
			
			Node wt = getNode(wr, "w:t");
			if (wt != null)
			{
				Element e = wt.getOwnerDocument().createElement(getNodeName());
				if (hasAttribute())
				{
					NamedNodeMap atts = n.getAttributes();
					for (int j = 0; j < atts.getLength(); j++)
					{
						Node att = atts.item(j);
						e.setAttribute(att.getNodeName(), att.getNodeValue());
					}
				}
				
				List<Node> cs = getChildNodes(wt);
				for (Node c : cs)
				{
					e.appendChild(c);
				}
				wt.appendChild(e);
			}
			
			removeNode(n);
		}
    }

    /**
     * Gets the style node name. Should not be null.
     * 
     * @return the name of the style node.
     */
    protected abstract String getNodeName();

    /**
     * Gets the node name that will be added to w:rPr node.
     * 
     * @return the node name. Should not be null.
     */
    public abstract String getAddNodeName();

    /**
     * Gets the node value that will be added to w:rPr node.
     * 
     * @return the node value. Should be null if no value.
     */
    protected abstract String getAddNodeValue();
    
    public abstract String getStyle();
    
    public static Style getStyle(Node att, List<Style> styles)
	{
		Style s = null;
		
		for (Style st : styles)
		{
			if (att.getNodeName().equals(st.getAddNodeName()))
			{
				if (!st.hasAttribute())
				{
					NamedNodeMap atts = att.getAttributes();
					if (atts == null || atts.getLength() == 0)
						return st;
				}
				
				String value = st.getAddNodeValue();
				if (value != null)
				{
					NamedNodeMap atts = att.getAttributes();
					if (atts != null)
					{
						Node val = atts.getNamedItem("w:val");
						if (val != null && val.getNodeValue().equals(value))
						{
							return st;
						}
					}
				}
				
				s = st;
			}
		}
		
		return s;
	}

	protected void updateStyle(Node cNode, Node cloneNode, Node wtNode,
			Node wrNode, Node root)
    {
    	if (cNode.getNodeName().equals(getNodeName()))
        {

        	addrPrNode(cloneNode, getAddNodeName(), getAddNodeValue(), 
        			wtNode.getNodeName(), cNode);
            // Style node can be nested.
        }

        changeText(cloneNode, wtNode.getNodeName(), cNode);

        root.insertBefore(cloneNode, wrNode);
	}
    
    /**
     * Handles the style node. Take &lt;b&gt; node for example.
     * <p>
     * Original file:
     * 
     * <pre>
     * &lt;w:r&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rFonts w:hint="eastAsia" /&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t&gt;this is &lt;b&gt;a&lt;/b&gt; example&lt;/w:t&gt;
     * &lt;/w:r&gt;
     * </pre>
     * 
     * After change: *
     * 
     * <pre>
     * &lt;w:r&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rFonts w:hint="eastAsia" /&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t&gt;this is /w:t&gt;
     * &lt;/w:r&gt;
     * &lt;w:r&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rFonts w:hint="eastAsia" /&gt;
     *         &lt;w:b/&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t&gt;a&lt;/w:t&gt;
     * &lt;/w:r&gt;
     * &lt;w:r&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rFonts w:hint="eastAsia" /&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t&gt; example&lt;/w:t&gt;
     * &lt;/w:r&gt;
     * </pre>
     * 
     * @param bNode
     */
    public void handleStyleNode(Node bNode)
    {
        Node wtNode = bNode.getParentNode();
        if (wtNode == null)
        	return;
        
        Node wrNode = wtNode.getParentNode();
        if (wrNode == null)
        	return;
        
        Node root = wrNode.getParentNode();
        if (root == null)
        	return;

        if (wrNode.getNodeName().equals("w:r"))
        {
        	Node tab = getNode(wrNode, "w:tab");
        	boolean tabBeforeWt = false;
			if (tab != null)
			{
				Node sib = tab.getPreviousSibling();
				if (sib == null || !"w:t".equals(sib.getNodeName()))
					tabBeforeWt = true;
			}
			
			Node br = getNode(wrNode, "w:br");
			boolean brBeforeWt = false;
			if (br != null)
			{
				Node sib = br.getPreviousSibling();
				if (sib == null || !"w:t".equals(sib.getNodeName()))
					brBeforeWt = true;
			}
        	
            Node cNode = wtNode.getFirstChild();
            
            while (cNode != null)
            {
                Node cloneNode = wrNode.cloneNode(true);

                updateStyle(cNode, cloneNode, wtNode, wrNode, root);

                if (br != null)
                {
                	// cNode is not the first node.
                	if (brBeforeWt && cNode.getPreviousSibling() != null)
                		removeBr(cloneNode);
                	
                	// cNode is not the last node.
                	if (!brBeforeWt && cNode.getNextSibling() != null)
                		removeBr(cloneNode);
                }
                
                if (tab != null)
                {
                	// cNode is not the first node.
                	if (tabBeforeWt && cNode.getPreviousSibling() != null)
                		removeTab(cloneNode);
                	
                	// cNode is not the last node.
                	if (!tabBeforeWt && cNode.getNextSibling() != null)
                		removeTab(cloneNode);
                }
                
                cNode = cNode.getNextSibling();
            }

            root.removeChild(wrNode);
        }
    }
    
    /**
     * Removes tab node from the specified nodes.
     * 
     * @param wr
     */
    protected void removeTab(Node wr)
    {
    	Node wtab = getNode(wr, "w:tab");
   	    if (wtab != null)
   		    wtab.getParentNode().removeChild(wtab);
    }

    /**
     * Removes br node from the specified nodes.
     * 
     * @param wr
     */
    protected void removeBr(Node wr)
    {
        Node br = getChild(wr, "w:br");
        if (br != null)
            wr.removeChild(br);
    }

    private void addrPrNode(Node node, String name, String value, String wtName, Node cNode)
    {
        Node n = getChild(node, "w:rPr");

        if (n == null)
        {
            n = node.getOwnerDocument().createElement("w:rPr");
            Node t = getChild(node, wtName);
            node.insertBefore(n, t);
        }

        Node b = getChild(n, name);

        if (b != null)
        	b.getParentNode().removeChild(b);
        
        Element e = node.getOwnerDocument().createElement(name);
        
        if (hasAttribute())
        {
        	NamedNodeMap atts = cNode.getAttributes();
			for (int j = 0; j < atts.getLength(); j++)
			{
				Node att = atts.item(j);
				e.setAttribute(att.getNodeName(), att.getNodeValue());
			}
			
			String s = cNode.getTextContent();
			if (s == null || s.length() == 0)
			{
				List<Node> cn = getChildNodes(cNode);
				List<Style> styles = DocxStyleUtil.getAllStyles();
				for (Node ccn : cn)
				{
					boolean isStyle = false;
					for (Style st : styles)
					{
						if (st.getNodeName().equals(ccn.getNodeName()))
						{
							isStyle = true;
							break;
						}
					}
					
					if (!isStyle)
					{
						Node ccn1 = ccn.cloneNode(true);
						e.appendChild(ccn1);
					}
				}
			}
        }
        else if (value != null)
        {
            e.setAttribute("w:val", value);
        }

        n.appendChild(e);
    }

    /**
     * Addes one node the the specified node.
     * <p>
     * The style node may be nested.
     * 
     * @see handleStyleNode
     * 
     * @param node
     * @param textName
     * @param styleNode
     */
    protected void changeText(Node node, String textName, Node styleNode)
    {
        Node t = getChild(node, textName);

        if (t != null)
        {
            if (styleNode.getNodeType() == Node.TEXT_NODE)
            {
            	if (textName.equals(t.getNodeName()) && t instanceof Element)
            	{
            		Element e = (Element) t;
            	    e.setAttribute("xml:space", "preserve");
            	}

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

    protected Node getChild(Node node, String name)
    {
        Node n = node.getFirstChild();
        while (n != null && !name.equals(n.getNodeName()))
        {
            n = n.getNextSibling();
        }

        return n;
    }

    
    public boolean hasAttribute()
    {
    	return false;
    }
}
