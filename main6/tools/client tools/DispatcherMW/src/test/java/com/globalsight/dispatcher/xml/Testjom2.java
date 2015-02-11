package com.globalsight.dispatcher.xml;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.globalsight.dispatcher.util.FileUtil;

public class Testjom2
{
    static String fileName = "./testFile/test-utf-16LE.xlf";    //test6.xlf
    
    public static void main(String[] args) throws IOException
    {
        Testjom2 test = new Testjom2();
//        test.clear();
        test.write();
        
//        String text = "<b>AAA-----" + new Date() + "</b>";
//        System.out.println(text.matches("<([A-Za-z0-9]*)\\\\b[^>]*>(.*?)</\\\\1>"));
//        System.out.println(text.matches("<([A-Za-z0-9]*)\\b[^>]*>(.*?)</\\1>"));
        
//        String xmlstr = "<B>Hello WOrld</B>";
//        createElement(xmlstr);
//        
//        xmlstr = "Mabuli";
//        createElement(xmlstr);
        
        char[] bomArr = {0xFE, 0xFF};
        printChar(bomArr[0]);
    }
    
    public static void printChar(char p_char){
        System.out.print(p_char + ",\t");
        System.out.print((int)p_char + ",\t");
    }
    
    public static void createElement(String p_xmlStr)
    {
        try
        {
            StringReader stringReader = new StringReader(p_xmlStr);
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(stringReader);
            Element elem = doc.getRootElement();
            System.out.println(elem.detach());
        }
        catch (Exception e)
        {
            System.out.println("Error for -->" + p_xmlStr + e);
        }
    }
    
    public void clear()
    {
        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(fileName);
            Element root = doc.getRootElement(); // Get root element
            Namespace ns = root.getNamespace();
            Element fileElem = root.getChild("file", ns);
            List<?> list = fileElem.getChild("body", ns).getChildren("trans-unit", ns);
            for (int i = 0; i < list.size(); i++)
            {
                Element elem = (Element) list.get(i);
                Element trgElem = elem.getChild("target", ns);
                if (trgElem != null){
                    trgElem.removeContent();
                } 
            }
            File trgFile = new File(fileName);
            XMLOutputter xmlOutput = new XMLOutputter();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trgFile), "UTF-8"));
            xmlOutput.output(doc, writer);
            System.out.println("Clear ****************************");
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }
    
    public void writeBOM()
    {
        File trgFile = new File(fileName.replace(".xlf", "-target.xlf"));
    }
    
    public void write() throws IOException
    {
        String text = "<b>LLL-----" + new Date() + "</b>";
//        text = "EMPTY<B>Hello World</B> is cool idea.";
        text = "北京欢迎你！中国共产党万岁。";
        System.out.println(text);
        Writer writer = null;
        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(fileName);
            Element root = doc.getRootElement(); // Get root element
            Namespace ns = root.getNamespace();
            Element fileElem = root.getChild("file", ns);
            List<?> list = fileElem.getChild("body", ns).getChildren("trans-unit", ns);
            for (int i = 0; i < list.size(); i++)
            {
                Element elem = (Element) list.get(i);
                Element trgElem = elem.getChild("target", ns);
                if (trgElem != null){
                    setTargetSegment(trgElem, text);
                }                    
                else
                {
                    trgElem = new Element("target");
                    setTargetSegment(trgElem, text);
                    elem.addContent(trgElem);
                }

            }
            File trgFile = new File(fileName.replace(".xlf", "-target.xlf"));
            XMLOutputter xmlOutput = new XMLOutputter();
            String encoding = FileUtil.getEncodingOfXml(new File(fileName));
//            encoding = "UTF-16BE";
            Format format = Format.getRawFormat();
            format.setEncoding(encoding);
//            writer = new OutputStreamWriter(new FileOutputStream(trgFile), format.getEncoding());
//            OutputStream fos = new BufferedOutputStream(new FileOutputStream(trgFile));
            OutputStream fos = new FileOutputStream(trgFile);
//            ByteArrayOutputStream fos = ByteArrayOutputStream();
            fos.write(255);
            fos.write(254);
            
            xmlOutput.setFormat(format);

//            writeBOM(writer, format.getEncoding());
//            xmlOutput.output(doc, writer);
            xmlOutput.output(doc, fos);
        }
        catch (JDOMException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        finally
        {
            if(writer != null)
                writer.close();
        }
    }
    
    // Write BOM Info for UTF-16BE/LE
    private void writeBOM(Writer p_writer, String p_encoding) throws IOException
    {
        if("UTF-16BE".equalsIgnoreCase(p_encoding))
        {
            p_writer.write(0xFEFF);
        }
        else if("UTF-16LE".equalsIgnoreCase(p_encoding))
        {
            p_writer.write(0xFFFE);
        }
    }
    
    private void setTargetSegment(Element p_trgElement, String p_target)
    {
        if(p_target == null || p_target.trim().length() == 0)
            return;
        
        try
        {
            StringReader stringReader = new StringReader("<target>" + p_target + "</target>");
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(stringReader);
            Element elem = doc.getRootElement().clone().detach();
            setNamespace(elem, p_trgElement.getNamespace());
//            p_trgElement.setContent(elem);
            
            
            for (int i = 0; i < elem.getContentSize(); i++)
            {
                p_trgElement.addContent(elem.getContent(i).clone().detach());
            }
        }
        catch (Exception e)
        {
            System.out.println("setTargetSegment error.\n" + e);
            p_trgElement.setText(p_target);
        }
    }
    
    private void setNamespace(Element p_element, Namespace p_namespace)
    {
        p_element.setNamespace(p_namespace);
        for(Element child : p_element.getChildren())
        {
            setNamespace(child, p_namespace);
        }
    }
}
