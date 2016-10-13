package com.globalsight.everest.page.pageexport.style.pptx;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    protected abstract String getAddNodeName();

    /**
     * Gets the node value that will be added to w:rPr node.
     * 
     * @return the node value. Should be null if no value.
     */
    protected abstract String getAddNodeValue();

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
     *         &lt;w:br/&gt;
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
    protected void handleStyleNode(Node bNode)
    {
        Node atNode = bNode.getParentNode();
        Node arNode = atNode.getParentNode();
        Node root = arNode.getParentNode();
        
        if (atNode == null || arNode == null || root == null)
        {
            return;
        }

        if (arNode.getNodeName().equals("a:r"))
        {
            Node cNode = atNode.getFirstChild();
            while (cNode != null)
            {
                Node cloneNode = arNode.cloneNode(true);
                updateStyle(cNode, cloneNode, atNode, arNode, root);

                // The br style should be removed.
                if (cNode.getNextSibling() != null)
                {
                    removeBr(cloneNode);
                }

                cNode = cNode.getNextSibling();
            }

            root.removeChild(arNode);
        }
    }
    
    protected void updateStyle(Node cNode, Node cloneNode, Node atNode,
			Node arNode, Node root)
    {
        if (cNode.getNodeName().equals(getNodeName()))
        {
            addrPrNode(cloneNode, getAddNodeName(), getAddNodeValue(),
                    atNode.getNodeName());
            // Style node can be nested.
        }

        changeText(cloneNode, atNode.getNodeName(), cNode);

        root.insertBefore(cloneNode, arNode);
    }

    /**
     * Removes br node from the specified nodes.
     * 
     * @param wr
     */
    private void removeBr(Node wr)
    {
        Node br = getChild(wr, "w:br");
        if (br != null)
            wr.removeChild(br);
    }

    private void addrPrNode(Node node, String name, String value, String atName)
    {
        Node n = getChild(node, "a:rPr");
        Element e = null;
        if (n == null)
        {
            e = node.getOwnerDocument().createElement("a:rPr");
            Node t = getChild(node, atName);
            node.insertBefore(e, t);
        }
        else
        {
            e = (Element) n;
        }

        e.setAttribute(name, value);
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
    private void changeText(Node node, String textName, Node styleNode)
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

    protected Node getChild(Node node, String name)
    {
        Node n = node.getFirstChild();
        while (n != null && !name.equals(n.getNodeName()))
        {
            n = n.getNextSibling();
        }

        return n;
    }

}
