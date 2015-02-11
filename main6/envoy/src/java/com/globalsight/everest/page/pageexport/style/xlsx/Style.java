package com.globalsight.everest.page.pageexport.style.xlsx;

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

        for (int i = 0; i < found; i++)
        {
            handleStyleNode(bNodes.item(i));
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
     * Gets the node name that will be added to rPr node.
     * 
     * @return the node name. Should not be null.
     */
    protected abstract String getAddNodeName();

    /**
     * Gets the node value that will be added to rPr node.
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
     * &lt;r&gt;
     *     &lt;rPr&gt;
     *         &lt;rFonts hint="eastAsia" /&gt;
     *     &lt;/rPr&gt;
     *     &lt;t&gt;this is &lt;b&gt;a&lt;/b&gt; example&lt;/t&gt;
     * &lt;/r&gt;
     * </pre>
     * 
     * After change: *
     * 
     * <pre>
     * &lt;r&gt;
     *     &lt;rPr&gt;
     *         &lt;rFonts hint="eastAsia" /&gt;
     *     &lt;/rPr&gt;
     *     &lt;t&gt;this is /t&gt;
     * &lt;/r&gt;
     * &lt;r&gt;
     *     &lt;rPr&gt;
     *         &lt;rFonts hint="eastAsia" /&gt;
     *         &lt;br/&gt;
     *     &lt;/rPr&gt;
     *     &lt;t&gt;a&lt;/t&gt;
     * &lt;/r&gt;
     * &lt;r&gt;
     *     &lt;rPr&gt;
     *         &lt;rFonts hint="eastAsia" /&gt;
     *     &lt;/rPr&gt;
     *     &lt;t&gt; example&lt;/t&gt;
     * &lt;/r&gt;
     * </pre>
     * 
     * @param bNode
     */
    protected void handleStyleNode(Node bNode)
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

        if (wtNode.getNodeName().equals("t"))
        {
            if (bNode.getTextContent().length() == 0)
            {
                bNode.getParentNode().removeChild(bNode);
                return;
            }

            if (wrNode.getNodeName().equals("si"))
            {
                Node n = wrNode.getOwnerDocument().createElement("r");
                wrNode.insertBefore(n, wtNode);
                wrNode.removeChild(wtNode);
                n.appendChild(wtNode);
                root = wrNode;
                wrNode = n;
            }

            if (wrNode.getNodeName().equals("r"))
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
    }
    
    protected void updateStyle(Node cNode, Node cloneNode, Node wtNode,
            Node wrNode, Node root)
    {
        if (cNode.getNodeName().equals(getNodeName()))
        {
            addrPrNode(cloneNode, getAddNodeName(),
                    getAddNodeValue(), wtNode.getNodeName());
            // Style node can be nested.
        }

        changeText(cloneNode, wtNode.getNodeName(), cNode);

        root.insertBefore(cloneNode, wrNode);

        // The br style should be removed.
        if (cNode.getNextSibling() != null)
        {
            removeBr(cloneNode);
        }
    }

    /**
     * Removes br node from the specified nodes.
     * 
     * @param wr
     */
    private void removeBr(Node wr)
    {
        Node br = getChild(wr, "br");
        if (br != null)
            wr.removeChild(br);
    }

    private void addrPrNode(Node node, String name, String value, String wtName)
    {
        Node n = getChild(node, "rPr");

        if (n == null)
        {
            n = node.getOwnerDocument().createElement("rPr");
            Node t = getChild(node, wtName);
            node.insertBefore(n, t);
        }

        Node b = getChild(n, name);

        if (b == null)
        {
            Element e = node.getOwnerDocument().createElement(name);
            if (value != null)
            {
                e.setAttribute("val", value);
            }

            n.appendChild(e);
        }
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
                if ("t".equals(t.getNodeName()) && t instanceof Element)
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

}
