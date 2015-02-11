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
package com.globalsight.ling.docproc.extractor.msoffice2010;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class XmlUtil 
{
    static private final Logger s_logger = Logger
            .getLogger(HibernateUtil.class);
	
	/**
	 * Only remove the node, not the clailed.
	 */
	public void removeNode(Node n)
	{
		List<Node> ns = getChildNodes(n);
		Node parent = n.getParentNode();
		for (Node c : ns)
		{
			parent.insertBefore(c, n);
		}
		
		parent.removeChild(n);
	}
	
	public List<Node> getChildNodes(Node n)
	{
		List<Node> r = new ArrayList<Node>();
		
		if (n != null)
		{
			NodeList ns = n.getChildNodes();
			for (int i = 0; i < ns.getLength(); i++)
			{
				r.add(ns.item(i));
			}
		}
		
		return r;
	}
	
	public Node getNode(Node n1, String name, boolean deep)
	{
	    NodeList list = n1.getChildNodes();
	    for (int i = 0; i < list.getLength(); i++)
	    {
	    	Node n = list.item(i);
	    	if (n.getNodeName().equals(name))
	    		return n;
	    	
	    	if (deep)
	    	{
	    		Node r = getNode(n, name, deep);
		    	if (r != null)
		    		return r;
	    	}
	    }
	    
	    return null;
	}
	
	public Node getNode(Node n1, String name)
	{
	    return getNode(n1, name, true);
	}
	
	public List<Node> getNodes(Node n1, String name)
	{
		List<Node> rs = new ArrayList<Node>();
		
	    NodeList list = n1.getChildNodes();
	    for (int i = 0; i < list.getLength(); i++)
	    {
	    	Node n = list.item(i);
	    	if (n.getNodeName().equals(name))
	    		rs.add(n);
	    	
	    	rs.addAll(getNodes(n, name));
	    }
	    
	    return rs;
	}
	
	public void getAllNodes(Node n1, String name, List<Node> ns)
	{
	    NodeList list = n1.getChildNodes();
	    for (int i = 0; i < list.getLength(); i++)
	    {
	    	Node n = list.item(i);
	    	if (n.getNodeName().equals(name))
	    		ns.add(n);
	    	
	    	getAllNodes(n, name, ns);
	    }
	}
	
	/**
	 * There are some performance issue for list.getLength and item() method.
	 * 
	 * @param document
	 * @param name
	 * @return
	 */
	public List<Node> getNodes(Document document, String name) 
	{
		NodeList list = document.getElementsByTagName(name);
		List<Node> ns = new ArrayList<Node>();
		
		Node n = list.item(0);
		for (int i = 1; ; i++)
		{
			if (n == null)
				break;
			
			ns.add(n);
			n = list.item(i);
		}
		
		return ns;
	}
	
	public Document getDocument(File f) throws Exception 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "utf-8"));
        Document document = db.parse(new InputSource(br));
        
        return document;
	}
	
	public Document newDocument() throws Exception 
	{
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.newDocument();
	}
	
	public Document getDocument(Reader r)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
		try 
		{
			db = dbf.newDocumentBuilder();
			InputSource in = new InputSource(r);
			in.setEncoding("utf-8");
			Document document = db.parse(in);
			return document;
		} 
		catch (Exception e) 
		{
			s_logger.error(e);
		}
        
        return null;
	}

	public List<Node> getAttributes(Node node)
	{
		List<Node> result = new ArrayList<Node>();
		
		NamedNodeMap attrs1 = node.getAttributes();
		for (int i = 0; i < attrs1.getLength(); ++i)
        {
			result.add(attrs1.item(i));
        }
		
		return result;
	}
	
	public Node getAttribute(Node node, String name)
	{
		NamedNodeMap attrs1 = node.getAttributes();
		return attrs1.getNamedItem(name);
	}

    public void saveToFile(Document document, String path)
    {
    	try 
		{
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			OutputStreamWriter ou = new OutputStreamWriter(
					new FileOutputStream(path), "UTF-8");
            StreamResult result = new StreamResult(ou);
            transformer.transform(source, result);
		} 
		catch (Exception e) 
		{
			s_logger.error(e);
		}
    }
    
    public void saveToFileNoFormat(Document document, String path)
    {
        try 
        {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            OutputStreamWriter ou = new OutputStreamWriter(
                    new FileOutputStream(path), "UTF-8");
            StreamResult result = new StreamResult(ou);
            transformer.transform(source, result);
        } 
        catch (Exception e) 
        {
            s_logger.error(e);
        }
    }
    
    public void getXmlString(Node node, StringBuilder sb)
    {
    	if (node.getNodeType() == Node.TEXT_NODE)
    	{
    		sb.append(escapeString(node.getTextContent()));
    		return;
    	}
    	
    	// start
    	String name = node.getNodeName();
    	sb.append("&lt;");
    	sb.append(name);
    	NamedNodeMap attrs = node.getAttributes();
        for (int j = 0; j < attrs.getLength(); ++j)
        {
            Node att = attrs.item(j);
            String attname = att.getNodeName();
            String value = att.getNodeValue();
            
            sb.append(" ").append(attname).append("=\"");
            sb.append(escapeString(escapeString(value)));
            sb.append("\"");
        }
    	sb.append("&gt;");
    	
    	// child
    	List<Node> cs = getChildNodes(node);
		for (Node c : cs)
		{
			getXmlString(c, sb);
		}
    	
		// end
    	sb.append("&lt;/");
    	sb.append(node.getNodeName());
    	sb.append("&gt;");
    }
    
    public String escapeString(String s)
    {
    	return com.globalsight.diplomat.util.XmlUtil.escapeString(s);
    }
}
