package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import com.globalsight.everest.page.pageexport.style.StyleFactory;
import com.globalsight.everest.page.pageexport.style.StyleUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.XmlParser;

public class PptxRepairer extends OfficeRepairer 
{
	public PptxRepairer(String path) 
	{
		super(path);
	}

	@Override
	protected boolean accept() 
	{
		File root = new File(path + "/ppt");
		
		return root.exists();
	}
	
	private List<File> getPptxRepairFiles()
	{
		File root = new File(path + "/ppt");
		
		return FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                String name = pathname.getName();
                
                if (name.endsWith(".xml"))
                {
//                    if (name.startsWith("slide"))
//                        return true;
//                    
//                    if (name.startsWith("notesSlide"))
//                        return true;
//                    
//                    if (name.startsWith("data"))
                        return true;
                }
                
                return  false;
            }
        });
	}
	
	@Override
	protected void repair()throws Exception 
    {
        List<File> fs = getPptxRepairFiles();
        
        for (File f : fs)
        {
            try
            {
                StyleUtil util = StyleFactory.getStyleUtil(StyleFactory.PPTX);
                util.updateBeforeExport(f.getAbsolutePath());
                
                String content = FileUtil.readFile(f, "utf-8");
                
                XmlParser parser = new XmlParser();
                org.dom4j.Document document = parser.parseXml(content);
                Element element = document.getRootElement();
                
                @SuppressWarnings("unchecked")
                List<Element> ats = element.selectNodes("//a:t");
                
                for (Element at : ats)
                {
                    if (at == null)
                        continue;
                    
                    @SuppressWarnings("unchecked")
                    List<Element> es = at.elements();
                    if (!at.isTextOnly())
                    {
                        String text = at.getStringValue();
                        for (Element e : es)
                        {
                            at.remove(e);
                        }
                        
                        at.setText(text);
                    }
                }
                
                Writer fileWriter = new OutputStreamWriter(new FileOutputStream(f), "UTF-8") ;
                XMLWriter xmlWriter = new XMLWriter(fileWriter);
                xmlWriter.write(document);
                xmlWriter.close();
            }
            catch(Exception e)
            {
                //do nothing.
            }
        }
    }
}
