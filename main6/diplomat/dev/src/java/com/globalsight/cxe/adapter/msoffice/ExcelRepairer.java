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

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultText;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.page.pageexport.style.StyleFactory;
import com.globalsight.everest.page.pageexport.style.StyleUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

public class ExcelRepairer extends OfficeRepairer 
{
	public ExcelRepairer(String path) 
	{
		super(path);
	}

	@Override
	protected boolean accept() 
	{
	    File root = new File(path + "/xl");
	    
		return root.exists();
	}

	private List<File> getExcelRepairFiles()
	{
		File root = new File(path + "/xl");
		
		return FileUtil.getAllFiles(root, new FileFilter()
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
	}
	
    private List<File> getAllFiles()
    {
        File root = new File(path + "/xl");

        return FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if (pathname.isFile())
                {
                    String name = pathname.getName();
                    if (name.endsWith(".xml") || name.endsWith(".xml.rels"))
                        return true;
                }

                return false;
            }
        });
    }
    
    private void repairWt() throws Exception 
    {
        List<File> fs = getExcelRepairFiles();
        
        for (File f : fs)
        {
            String content = FileUtil.readFile(f, "utf-8");
            
            XmlParser parser = new XmlParser();
            org.dom4j.Document document = parser.parseXml(content);
            Element element = document.getRootElement();
            
            @SuppressWarnings("unchecked")
            List<Element> wts = element.selectNodes("//t");
            
            for (Element wt : wts)
            {
            	if (wt == null)
            		continue;
            	
            	@SuppressWarnings("unchecked")
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
	
    private void updateStyle()
    {
        StyleUtil util = StyleFactory.getStyleUtil(StyleFactory.XLSX);
        List<File> fs = getAllFiles();
        
        for (File f : fs) 
        {
        	util.updateBeforeExport(f.getAbsolutePath());
        }
        
//        ExcelStylerEpairer r = new ExcelStylerEpairer(path);
//        try 
//        {
//            r.updateShareString();
//        } 
//        catch (Exception e) 
//        {
//            //e.printStackTrace();
//        }
    }
    
    
	@Override
	protected void repair() throws Exception 
	{
		updateStyle();
		repairExcelSharedStrings();
		repairExcelStyle();
		repairWt();
        
	}
	
	private static List<Element> getElementByName(Element root, String name)
	{
		List<Element> elements = new ArrayList<Element>();
		
		@SuppressWarnings("unchecked")
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
	
	private void repairExcelStyle() 
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
	
	private void repairExcelSharedStrings()throws Exception 
	{
		File f = new File(path + "/xl/sharedStrings.xml");
		if (!f.exists())
			return;
		
		String content = FileUtil.readFile(f, "utf-8");
		
		XmlParser parser = new XmlParser();
		org.dom4j.Document document = parser.parseXml(content);
		Element element = document.getRootElement();
		List<Element> rs = getElementByName(element, "r");
		for (Element r : rs)
		{
			@SuppressWarnings("rawtypes")
			List els = r.content();
			
			StringBuffer sb = new StringBuffer();
			Element wt = null;
			List<DefaultText> texts = new ArrayList<DefaultText>();
			
			for (Object el : els)
			{
				if (el instanceof DefaultText)
                {
					DefaultText text = (DefaultText) el;
					String s = text.getStringValue();
					if ("\n".equals(s))
						continue;
					
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
}
