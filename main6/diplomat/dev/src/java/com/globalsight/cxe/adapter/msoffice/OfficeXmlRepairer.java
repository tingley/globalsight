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

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultText;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

/**
 * @author Ares
 *
 */
public class OfficeXmlRepairer
{
	static private final Logger logger = Logger
            .getLogger(OfficeXmlRepairer.class);
	
    private static void forTextInWr(Element element)
    {
        List<Node> ts = element.selectNodes("//w:r/text()");
        
        for (Node t : ts)
        {
            Element wr = t.getParent();
            List els = wr.content();
            
            StringBuffer sb = new StringBuffer();
            Element wt = null;
            List<DefaultText> texts = new ArrayList<DefaultText>();
            
            for (Object el : els)
            {
                if (el instanceof DefaultText)
                {
                    DefaultText text = (DefaultText) el;
                    texts.add(text);
                    sb.append(text.getStringValue());
                }
                else if (el instanceof Element)
                {
                    Element elm = (Element) el;
                    if ("t".equals(elm.getName()))
                    {
                        wt = elm;
                        sb.append(elm.getStringValue());
                    }
                }
            }
            
            if (wt == null)
            {
                wt = wr.addElement("w:t");
                wt.addAttribute("xml:space", "preserve");
            }
            
            wt.setText(sb.toString());
            
            for (DefaultText text : texts)
            {
                wr.remove(text);
            }
        }
    }
    
    private static void forWtNotInWr(Element element)
    {
        List<Element> wts = element.selectNodes("//w:t");
        
        for (Element wt : wts)
        {
        	Element parent = wt.getParent();
        	if (parent == null || "r".equals(parent.getName()))
        		continue;
        	
        	List<Element> es = parent.elements();
            
        	int wtIndex = -1;
        	for (Element e : es)
            {
        		wtIndex++;
        		if (wt.equals(e))
        		{
        			break;
        		}
            }
        	
        	for (int i = 1; i < es.size(); i++)
        	{
        		int prefix = wtIndex - i;
        		int suffix = wtIndex + i;
        		
        		if (prefix < 0 && suffix > es.size() - 1)
        		{
        			break;
        		}
        		
        		if (prefix > -1)
        		{
        			Element prefixElement = es.get(prefix);
            		if ("r".equals(prefixElement.getName()))
            		{
            			List<Element> preWts = prefixElement.elements("t");
            			if (preWts.size() > 0)
            			{
            				String text = wt.getStringValue();
            				Element preWt = preWts.get(preWts.size() - 1);
            				preWt.setText(preWt.getStringValue() + text);
            				parent.remove(wt);
                			break;
            			}
            		}
        		}
        		
        		if (suffix < es.size())
        		{
        			Element sufixElement = es.get(prefix);
            		if ("r".equals(sufixElement.getName()))
            		{
            			List<Element> sufWts = sufixElement.elements("t");
            			if (sufWts.size() > 0)
            			{
            				String text = wt.getStringValue();
            				Element sufWt = sufWts.get(0);
            				sufWt.setText(text + sufWt.getStringValue());
            				parent.remove(wt);
                			break;
            			}
            		}
        		}
        	}
        }
    }
    
    private static void forNodesInWt(Element element)
    {
        List<Element> wts = element.selectNodes("//w:t");
        
        for (Element wt : wts)
        {
            List<Element> es = wt.elements();
            if (!wt.isTextOnly())
            {
                String text = wt.getStringValue();
                for (Element e : es)
                {
                    wt.remove(e);
                }
                
                wt.setText(text);
            }
        }
    }
    
    private static void forWrInWr(Element element)
    {
        List<Node> ts = element.selectNodes("//w:r/w:r");
        
        for (Node t : ts)
        {
            Element wr = t.getParent();
            
            if (wr == null)
                continue;
            
            List els = wr.content();
            
            StringBuffer sb = new StringBuffer();
            Element wt = null;
            List<Element> wrs = new ArrayList<Element>();
            
            for (Object el : els)
            {
                if (el instanceof Element)
                {
                    Element elm = (Element) el;
                    if ("t".equals(elm.getName()))
                    {
                        wt = elm;
                        sb.append(elm.getStringValue());
                    }
                    else if ("r".equals(elm.getName()))
                    {
                        sb.append(elm.getStringValue());
                        wrs.add(elm);
                    }
                }
            }
            
            if (wt == null)
            {
                wt = wr.addElement("w:t");
                wt.addAttribute("xml:space", "preserve");
            }
            
            wt.setText(sb.toString());
            
            for (Element w : wrs)
            {
                wr.remove(w);
            }
        }
    }
    
    private static void forHyperlinkInWr(Element element)
    {
        List<Node> ts = element.selectNodes("//w:r/w:hyperlink/w:r");
        
        for (Node t : ts)
        {
            Element hyperlink = t.getParent();
            
            if (hyperlink == null)
                continue;
            
            Element wr = hyperlink.getParent();
            if (wr == null)
                continue;
            
            Element wrParent = wr.getParent();
            if (wrParent == null)
                continue;
            
            boolean beforeWt = false;
            
            List els = wr.content();
            
            for (Object el : els)
            {
                if (el instanceof Element)
                {
                    Element elm = (Element) el;
                    if ("t".equals(elm.getName()))
                    {
                    	beforeWt = false;
                    	break;
                    }
                    else if (hyperlink.equals(elm))
                    {
                    	beforeWt = true;
                    	break;
                    }
                }
            }
            
            wr.remove(hyperlink);
            
            List es = wrParent.elements();
            int index = es.indexOf(wr);
            index = beforeWt ? index : index + 1;
            
            hyperlink.setParent(wrParent);
            es.add(index, hyperlink);
        }
    }
    
    
    private static void repairDocFiles(File f)throws Exception 
	{
		if (!f.exists())
			return;
		
		String content = FileUtil.readFile(f, "utf-8");
		
		XmlParser parser = new XmlParser();
		parser.setErrorHandler(new ErrorHandler() 
		{
			@Override
			public void warning(SAXParseException arg0) throws SAXException 
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void fatalError(SAXParseException arg0) throws SAXException 
			{
				return;
			}
			
			@Override
			public void error(SAXParseException e) throws SAXException 
			{
				String s = e.getMessage();
		        if (s.matches("Attribute .*? was already specified for element[\\s\\S]*"))
		            return;

		        throw new SAXException("XML parse error at\n  line "
		                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
		                + "\n  Message:" + e.getMessage());
			}
		});
		
		org.dom4j.Document document = parser.parseXml(content);
		Element element = document.getRootElement();

		forHyperlinkInWr(element);
		forNodesInWt(element);
		forWtNotInWr(element);
		forTextInWr(element);
		forWrInWr(element);
		
		Writer fileWriter = new OutputStreamWriter(new FileOutputStream(f), "UTF-8") ;
		XMLWriter xmlWriter = new XMLWriter(fileWriter);
		xmlWriter.write(document);
		xmlWriter.close();
		
		if (content.contains("</mc:AlternateContent>"))
		{
		    forAlternateContent(f);
		}
	}
    
	private static void repairDocs(String path)throws Exception 
	{
		String docPath = path + "/word/document.xml";
		File f = new File(docPath);
		if (!f.exists())
			return;
		
		repairDocFiles(f);
		
		File root = new File(path + "/word");
		List<File> fs = FileUtil.getAllFiles(root, new FileFilter() 
		{
			@Override
			public boolean accept(File pathname) 
			{
				if (pathname.isFile())
				{
					String name = pathname.getName();
					if (name.startsWith("footer") && name.endsWith(".xml"))
						return true;
					
					if (name.startsWith("header") && name.endsWith(".xml"))
						return true;
				}
				return false;
			}
		});
		
		for (File f1 : fs)
		{
			repairDocFiles(f1);
		}
	}
	
	private static void forAlternateContent(File f) throws Exception
    {
	    String content = FileUtil.readFile(f, "utf-8");
        int startIndex = 0;
        String startTag = "<mc:AlternateContent>";
        String endTag = "</mc:AlternateContent>";
        String startTag_2 = "<w:txbxContent>";
        String endTag_2 = "</w:txbxContent>";
        StringBuffer allContent = new StringBuffer(content);
        StringBuffer newContent = new StringBuffer(content.length());
        
        StringIndex si = StringIndex.getValueBetween(allContent, startIndex, startTag, endTag);
        while(si != null)
        {
            StringBuffer sub = new StringBuffer(si.value);
            int s = si.start;
            int e = si.end;
            String pre = allContent.substring(0, s);
            String after = allContent.substring(e);
            newContent.append(pre);
            
            // handle <w:txbxContent> and </w:txbxContent>
            int tmpindex = 0;
            StringIndex tmpsi = StringIndex.getValueBetween(sub, tmpindex, startTag_2, endTag_2);
            if (tmpsi != null)
            {
                int s1 = tmpsi.start;
                int e1 = tmpsi.end;
                tmpindex = tmpsi.end;
                
                tmpsi = StringIndex.getValueBetween(sub, tmpindex, startTag_2, endTag_2);
                
                if (tmpsi != null)
                {
                    String c2 = tmpsi.value;
                    
                    sub.replace(s1, e1, c2);
                }
            }
            newContent.append(sub);
            
            startIndex = e;
            allContent = new StringBuffer(after);
            si = StringIndex.getValueBetween(allContent, startIndex, startTag, endTag);
        }
        
        newContent.append(allContent.toString());
        
        FileUtil.writeFile(f, newContent.toString(), "utf-8");
    }

    private static void repairPptx(String path)throws Exception 
    {
        path = path + "/ppt";
        File root = new File(path);
        if (!root.exists())
            return;
        
        List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
        {
            
            @Override
            public boolean accept(File pathname)
            {
                String name = pathname.getName();
                
                if (name.endsWith(".xml"))
                {
                    if (name.startsWith("slide"))
                        return true;
                    
                    if (name.startsWith("notesSlide"))
                        return true;
                    
                    if (name.startsWith("data"))
                        return true;
                }
                
                return  false;
            }
        });
        
        for (File f : fs)
        {
            String content = FileUtil.readFile(f, "utf-8");
            
            XmlParser parser = new XmlParser();
            org.dom4j.Document document = parser.parseXml(content);
            Element element = document.getRootElement();
            List<Element> wts = element.selectNodes("//a:t");
            
            for (Element wt : wts)
            {
            	if (wt == null)
            		continue;
            	
                List<Element> es = wt.elements();
                if (!wt.isTextOnly())
                {
                    String text = wt.getStringValue();
                    for (Element e : es)
                    {
                        wt.remove(e);
                    }
                    
                    wt.setText(text);
                }
            }
            
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(f), "UTF-8") ;
            XMLWriter xmlWriter = new XMLWriter(fileWriter);
            xmlWriter.write(document);
            xmlWriter.close();
        }
    }
	
	public static void repair(String path)
	{
		try 
		{
			repairDocs(path);
			repairExcel(path);
			repairPptx(path);
			
			repairExcelStyle(path);
		} 
		catch (Exception e) 
		{
			logger.error(e);
		}
    }
	
	private static void repairExcelStyle(String path)throws Exception 
    {
        String filepath = path + "/xl/styles.xml";
        File f = new File(filepath);
        if (!f.exists())
            return;

        try
        {
            String apath = f.getAbsolutePath();
            int index = apath.indexOf(OfficeXmlHelper.CONVERSION_DIR_NAME);
            String localeAndOthers = apath.substring(index
                    + OfficeXmlHelper.CONVERSION_DIR_NAME.length() + 1);
            boolean isRtlLocale = EditUtil.isRTLLocale(localeAndOthers);
            if (isRtlLocale)
            {
                String content = FileUtils.read(f, "utf-8");
                String newContent = OfficeXmlRepairer.fixRtlLocale(content);
                FileUtils.write(f, newContent, "utf-8");
            }
        }
        catch (Exception e)
        {
            // ignore exception
        }
    }
	
	private static List<Element> getElementByName(Element root, String name)
	{
		List<Element> elements = new ArrayList<Element>();
		List<Element> es = root.elements();
		for (Element e : es)
		{
			if (name.equals(e.getName()))
				elements.add(e);
			else
				elements.addAll(getElementByName(e, name));
		}
		
		return elements;
	}
	
	private static void repairExcel(String path)throws Exception 
	{
		repairExcelSharedStrings(path);
		
		 path = path + "/xl";
	     File root = new File(path);
	     if (!root.exists())
	        return;

        List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
        {
            
            @Override
            public boolean accept(File pathname)
            {
                String name = pathname.getName();
                
                if (name.endsWith(".xml"))
                {
                    if (name.startsWith("drawing"))
                        return true;                    
                }
                
                return  false;
            }
        });
        
        for (File f : fs)
        {
            String content = FileUtil.readFile(f, "utf-8");
            
            XmlParser parser = new XmlParser();
            org.dom4j.Document document = parser.parseXml(content);
            Element element = document.getRootElement();
            List<Element> wts = element.selectNodes("//a:t");
            
            for (Element wt : wts)
            {
            	if (wt == null)
            		continue;
            	
                List<Element> es = wt.elements();
                if (!wt.isTextOnly())
                {
                    String text = wt.getStringValue();
                    for (Element e : es)
                    {
                        wt.remove(e);
                    }
                    
                    wt.setText(text);
                }
            }
            
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(f), "UTF-8") ;
            XMLWriter xmlWriter = new XMLWriter(fileWriter);
            xmlWriter.write(document);
            xmlWriter.close();
        }
	}
	
	private static void repairExcelSharedStrings(String path)throws Exception 
	{
		path = path + "/xl/sharedStrings.xml";
		File f = new File(path);
		if (!f.exists())
			return;
		
		String content = FileUtil.readFile(f, "utf-8");
		
		XmlParser parser = new XmlParser();
		org.dom4j.Document document = parser.parseXml(content);
		Element element = document.getRootElement();
		List<Element> rs = getElementByName(element, "r");
		for (Element r : rs)
		{
			List els = r.content();
			StringBuffer sb = new StringBuffer();
			Element wt = null;
			List<DefaultText> texts = new ArrayList<DefaultText>();
			
			for (Object el : els)
			{
				if (el instanceof DefaultText)
                {
					DefaultText text = (DefaultText) el;
					texts.add(text);
					sb.append(text.getStringValue());
                }
				else if (el instanceof Element)
				{
					Element elm = (Element) el;
					if ("t".equals(elm.getName()))
					{
						wt = elm;
						sb.append(elm.getStringValue());
					}
				}
			}
			
			if (wt == null)
			{
				wt = r.addElement("t");
				wt.addAttribute("xml:space", "preserve");
			}
			
			if (sb.length() == 0)
				sb.append(" ");
			
			wt.clearContent();
			wt.addText(sb.toString());
			
			for (DefaultText text : texts)
			{
				r.remove(text);
			}
		}
		
		Writer fileWriter = new OutputStreamWriter(new FileOutputStream(f), "UTF-8") ;
		XMLWriter xmlWriter = new XMLWriter(fileWriter);
		xmlWriter.write(document);
		xmlWriter.close();
	}

    private static String s_wpStartTag = "<w:p ";
    private static String s_wpEndTag = "</w:p>";
    private static String s_wpPrStartTag = "<w:pPr";
    private static String s_wpPrEndTag = "</w:pPr>";
    private static String s_wbidiTag = "<w:bidi/>";
    private static String s_wrStartTag = "<w:r ";
    private static String s_wrEndTag = "</w:r>";
    private static String s_wrPrStartTag = "<w:rPr";
    private static String s_wrPrEndTag = "</w:rPr>";
    private static String s_wrtlTag = "<w:rtl/>";
	private static String s_wtEndTag = "</w:t>";
	private static String s_gtMark = ">";
	
	private static String s_apStartTag = "<a:p ";
	private static String s_apStartTag2 = "<a:p>";
    private static String s_apEndTag = "</a:p>";
    private static String s_apPrRtl = "rtl=\"1\"";
    private static String s_apPrStartTag = "<a:pPr";
	
    private static String s_cellXfsStartTag = "<cellXfs ";
    private static String s_cellXfsEndTag = "</cellXfs>";
    private static String s_xfStartTag = "<xf ";
    private static String s_xfEndTag = "</xf>";
    
    
	/**
	 * For Right to Left languages in office XML
	 * @param xmlContent
	 * @return
	 */
	public static String fixRtlLocale(String xmlContent)
	{
	    if (xmlContent == null || xmlContent.length() == 0)
	    {
	        return xmlContent;
	    }
	    
	    xmlContent = xmlContent.trim();
	    
	    if (xmlContent.endsWith("</w:document>"))
	    {
	        String result = fixRtlDocumentXmlWP(xmlContent);
	        //result = fixRtlDocumentXmlWR(result);
	        
	        return result;
	    }
	    else if (xmlContent.contains("</p:sld>"))
	    {
	        return fixRtlSlideXmlAP(xmlContent);
	    }
	    else if (xmlContent.contains("</styleSheet>"))
	    {
	        return fixRtlSheetStylesXml(xmlContent);
	    }
	    
	    return xmlContent;
	}

    private static String fixRtlDocumentXmlWP(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();

        StringIndex si = StringIndex.getValueBetween(src, 0, s_wpStartTag, s_wpEndTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            // add w:bidi for w:p in w:document XML
            if (v.contains(s_wbidiTag))
            {
                result.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0, s_wpPrStartTag, s_gtMark);
                if (tempSi == null)
                {
                    String sss = v.substring(0, 1);
                    tempSi = StringIndex.getValueBetween(temp, 0, sss, s_gtMark);
                    result.append(sss);
                    result.append(tempSi.value);
                    result.append(s_gtMark);
                    result.append(s_wpPrStartTag).append(s_gtMark);
                    result.append(s_wbidiTag).append(s_wpPrEndTag);
                    result.append(temp.substring(tempSi.end + 1));
                }
                else
                {
                    result.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        result.append(tempV.substring(0, tempV.length() - 1));
                        result.append(s_gtMark);
                        result.append(s_wbidiTag);
                        result.append(s_wpPrEndTag);
                    }
                    else
                    {
                        result.append(tempV);
                        result.append(s_gtMark);
                        result.append(s_wbidiTag);
                    }
                    result.append(temp.substring(tempSi.end + 1));
                }
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, s_wpStartTag, s_wpEndTag);
        }

        result.append(src);

        return result.toString();
    }
    
    private static String fixRtlDocumentXmlWR(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();

        StringIndex si = StringIndex.getValueBetween(src, 0, s_wrStartTag, s_wrEndTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            // add <w:rtl/> for w:r in w:document XML
            if (v.contains(s_wrtlTag))
            {
                result.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0, s_wrPrStartTag, s_gtMark);
                if (tempSi == null)
                {
                    String sss = v.substring(0, 1);
                    tempSi = StringIndex.getValueBetween(temp, 0, sss, s_gtMark);
                    result.append(sss);
                    result.append(tempSi.value);
                    result.append(s_gtMark);
                    result.append(s_wrPrStartTag).append(s_gtMark);
                    result.append(s_wrtlTag).append(s_wrPrEndTag);
                    result.append(temp.substring(tempSi.end + 1));
                }
                else
                {
                    result.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        result.append(tempV.substring(0, tempV.length() - 1));
                        result.append(s_gtMark);
                        result.append(s_wrtlTag);
                        result.append(s_wrPrEndTag);
                    }
                    else
                    {
                        result.append(tempV);
                        result.append(s_gtMark);
                        result.append(s_wrtlTag);
                    }
                    result.append(temp.substring(tempSi.end + 1));
                }
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, s_wrStartTag, s_wrEndTag);
        }

        result.append(src);

        return result.toString();
    }
    
    private static String fixRtlSlideXmlAP(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();

        StringIndex si = StringIndex.getValueBetween(src, 0, s_apStartTag, s_apEndTag);
        if (si == null)
            si = StringIndex.getValueBetween(src, 0, s_apStartTag2, s_apEndTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            // add rtl="1" for a:p in p:sld XML
            if (v.contains(s_apPrRtl))
            {
                result.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0, s_apPrStartTag, s_gtMark);
                if (tempSi == null)
                {
                    if (before.endsWith(s_apStartTag2))
                    {
                        result.append("<a:pPr rtl=\"1\"/>");
                        result.append(temp);
                    }
                    else
                    {
                        String sss = v.substring(0, 1);
                        tempSi = StringIndex.getValueBetween(temp, 0, sss, s_gtMark);
                        result.append(sss);
                        result.append(tempSi.value);
                        result.append(s_gtMark);
                        result.append("<a:pPr rtl=\"1\"/>");
                        result.append(temp.substring(tempSi.end + 1));
                    }
                }
                else
                {
                    result.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        result.append(tempV.substring(0, tempV.length() - 1));
                        result.append(" rtl=\"1\"/");
                    }
                    else
                    {
                        result.append(tempV);
                        result.append(" rtl=\"1\"");
                    }
                    result.append(temp.substring(tempSi.end));
                }
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, s_apStartTag, s_apEndTag);
            if (si == null)
                si = StringIndex.getValueBetween(src, 0, s_apStartTag2, s_apEndTag);
        }

        result.append(src);

        return result.toString();
    }
    
    private static String fixRtlSheetStylesXml(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();
        StringIndex si = StringIndex.getValueBetween(src, 0, s_cellXfsStartTag, s_cellXfsEndTag);
        String before = src.substring(0, si.start);
        String xfs = si.value;
        String after = src.substring(si.end);
        result.append(before);
        
        StringBuffer temp = new StringBuffer(xfs);
        si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_gtMark);
        while (si != null)
        {
            String b = temp.substring(0, si.start);
            String v = si.value;
            String a = temp.substring(si.end);
            
            if (v.endsWith("/"))
            {
                result.append(b);
                result.append(v.substring(0, v.length() - 1));
                result.append("><alignment readingOrder=\"2\"/></xf");
            }
            else
            {
                si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_xfEndTag);
                b = temp.substring(0, si.start);
                v = si.value;
                a = temp.substring(si.end);
                result.append(b);

                if (v.contains(" readingOrder=\"2\""))
                {
                    result.append(v);
                }
                else if (v.contains(" readingOrder=\"1\""))
                {
                    result.append(v.replace(" readingOrder=\"1\"", " readingOrder=\"2\""));
                }
                else if (v.contains("<alignment "))
                {
                    result.append(v.replace("<alignment ", "<alignment readingOrder=\"2\" "));
                }
                else
                {
                    result.append(v);
                    result.append("<alignment readingOrder=\"2\"/>");
                }
            }

            temp.delete(0, temp.length());
            temp.append(a);
            si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_gtMark);
        }

        result.append(temp);
        result.append(after);

        return result.toString();
    }
}
