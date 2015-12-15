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
package com.globalsight.cxe.adapter.msoffice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xalan.transformer.TransformerIdentityImpl;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.util.FileUtil;

public class ExcelStylerEpairer
{

    private String path;
    private HashMap<String, Node> style2font;
    private HashMap<String, String> s2style;
    private static List<String> STYLES = new ArrayList<String>();
    static
    {
        STYLES.add("u");
        STYLES.add("b");
        STYLES.add("i");
        STYLES.add("sub");
        STYLES.add("sup");
    }

    public ExcelStylerEpairer(String path)
    {
        super();
        this.path = path;
    }

    /**
     * Add font information if the share string don't have any font information.
     * 
     * @throws Exception
     */
    public void updateShareString() throws Exception
    {
        File f = new File(path + "/xl/sharedStrings.xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), "utf-8"));

        Document document = db.parse(new InputSource(br));
        NodeList bNodes = document.getElementsByTagName("si");
        for (int i = 0; i < bNodes.getLength(); i++)
        {
            Node n = bNodes.item(i);
            if (hasStyle(n) && noPre(n))
            {
                addPre(n, i);
            }

        }

        String filePath = path + "/xl/sharedStrings.xml";
        saveToFile(document, filePath);
    }

    /**
     * Get a map that contain the pair of shared string and style. The content
     * of v is shared string index, and the s attribute is style index.
     * 
     * @return
     * @throws Exception
     */
    private HashMap<String, String> getStyles() throws Exception
    {
        if (s2style != null)
            return s2style;

        s2style = new HashMap<String, String>();

        List<File> fs = FileUtil.getAllFiles(new File(path), new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if (pathname.isFile())
                {
                    String name = pathname.getName();
                    if (name.startsWith("sheet") && name.endsWith(".xml"))
                        return true;
                }

                return false;
            }
        });

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        for (File f : fs)
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f), "utf-8"));
            Document document = db.parse(new InputSource(br));
            NodeList bNodes = document.getElementsByTagName("v");
            for (int i = 0; i < bNodes.getLength(); i++)
            {
                Node v = bNodes.item(i);
                Node c = v.getParentNode();
                if ("c".equals(c.getNodeName()))
                {
                    Node arr = c.getAttributes().getNamedItem("s");
                    if (arr != null)
                    {
                        s2style.put(v.getTextContent(), arr.getNodeValue());
                    }
                }
            }
        }

        return s2style;
    }

    /**
     * Get a map that contain some pairs of style and font.
     * 
     * @return
     * @throws Exception
     */
    private HashMap<String, Node> getFonts() throws Exception
    {
        if (style2font == null)
        {
            style2font = new HashMap<String, Node>();

            File f = new File(path + "/xl/styles.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f), "utf-8"));
            Document document = db.parse(new InputSource(br));
            String xpath = "//*[local-name()=\"cellXfs\"]/*[local-name()=\"xf\"]";

            NodeList fs = document.getElementsByTagName("font");

            NodeList affectedNodes = XPathAPI.selectNodeList(document, xpath);
            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    org.w3c.dom.Element node = (org.w3c.dom.Element) affectedNodes
                            .item(i);
                    String fontId = node.getAttribute("fontId");
                    style2font.put("" + i, fs.item(Integer.parseInt(fontId)));
                }
            }
        }

        return style2font;
    }

    private boolean hasStyle(Node n)
    {
        if (n.getNodeType() != Node.ELEMENT_NODE)
        {
            return false;
        }

        Element e = (Element) n;

        for (String s : STYLES)
        {
            if (e.getElementsByTagName(s).getLength() > 0)
                return true;
        }
        return false;
    }

    private void addPre(Node n, int index) throws Exception
    {
        String s = "" + index;
        String style = getStyles().get(s);
        if (style != null)
        {
            Node font = getFonts().get(style);
            Node c = n.getFirstChild();
            if (c.getNodeType() == Node.TEXT_NODE)
                c = c.getNextSibling();

            if (c == null)
                return;

            Node r = null;

            if (c.getNodeName().equals("r"))
            {
                r = c;
            }
            else if (c.getNodeName().equals("t"))
            {
                r = c.getOwnerDocument().createElement("r");
                n.insertBefore(r, c);
                n.removeChild(c);
                r.appendChild(c);
            }

            if (r != null)
            {
                Node rPr = c.getOwnerDocument().createElement("rPr");
                r.insertBefore(rPr, r.getFirstChild());

                Node f = font.getFirstChild();
                while (f != null)
                {
                    if (f.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Node fc = c.getOwnerDocument().createElement(
                                f.getNodeName());
                        NamedNodeMap nm = f.getAttributes();

                        for (int j = 0; j < nm.getLength(); j++)
                        {
                            Node attNode = nm.item(j);
                            Attr att = c.getOwnerDocument().createAttribute(
                                    attNode.getNodeName());
                            att.setNodeValue(attNode.getNodeValue());
                            fc.getAttributes().setNamedItem(att);
                        }

                        rPr.appendChild(fc);
                    }

                    f = f.getNextSibling();
                }
            }
        }
    }

    private boolean noPre(Node n)
    {
        Node n1 = n.getFirstChild();
        if (n1.getNodeType() != Node.ELEMENT_NODE)
            n1 = n1.getNextSibling();

        if (n1 == null)
            return false;

        if ("t".equals(n1.getNodeName()))
            return true;

        if ("r".equals(n1.getNodeName()))
        {
            Node n2 = n.getFirstChild();
            while (n2 != null)
            {
                if (n2.getNodeName().equals("rPr"))
                {
                    return false;
                }

                n2 = n2.getNextSibling();
            }

            Node n3 = n.getFirstChild();
            if (n3 != null && n3.getNodeName().equals("r"))
            {
                Node n3Child = n3.getFirstChild();

                while (n3Child != null)
                {
                    if (n3Child.getNodeName().equals("rPr"))
                    {
                        return false;
                    }

                    n3Child = n3Child.getNextSibling();
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Saves the document to a XML files.
     * 
     * @param document
     *            the document to save.
     * @param path
     *            the file path.
     * @throws Exception
     */
    private void saveToFile(Document document, String path) throws Exception
    {
        TransformerIdentityImpl transformer = new TransformerIdentityImpl();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        FileOutputStream fout = new FileOutputStream(path);
        OutputStreamWriter ou = new OutputStreamWriter(fout, "UTF-8");
        StreamResult result = new StreamResult(ou);
        transformer.transform(source, result);
    }
}
