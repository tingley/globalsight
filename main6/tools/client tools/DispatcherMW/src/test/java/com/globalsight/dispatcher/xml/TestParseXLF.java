package com.globalsight.dispatcher.xml;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.globalsight.dispatcher.bo.JobBO;
import com.globalsight.dispatcher.controller.TranslateXLFController;

public class TestParseXLF extends TranslateXLFController
{
    public static void main(String[] args)
    {
        String fileName = "./testFile/test.xlf";
//        parseXLFWrapper(fileName);
        
//        fileName = "./testFile/c_changing_networking_configuration.html.xlf";
//        parseXLFWrapper(fileName);
//        
//        fileName = "./testFile/tasks_itunes_Rent_Movie_14023_es_ES_xliff_small.xlf";
//        parseXLFWrapper(fileName);
//        
//        fileName = "./testFile/test6.xlf";
//        parseXLFWrapper(fileName);
//        
//        fileName = "./testFile/Welcome.html.sdlxliff";
//        parseXLFWrapper(fileName);
//        
//        fileName = "test.xlf";
//        parseXLFWrapper(fileName);
        
        fileName = "./testFile/c_changing_networking_configuration.html-update4.xlf";
        parseXLFWrapper(fileName);
        
//        fileName = "./testFile/test6.xlf";
//        testInnerXML(fileName);
        
    }
    
    public static void testInnerXML(String p_filePath)
    {
        List<String> srcSegments = new ArrayList<String>();

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document read_doc = builder.build(new File(p_filePath));
            // Get Root Element
            Element root = read_doc.getRootElement();
            Namespace namespace = root.getNamespace();
            Element fileElem = root.getChild("file", namespace);

            List<?> list = fileElem.getChild("body", namespace).getChildren("trans-unit", namespace);
            for (int i = 0; i < list.size(); i++)
            {
                Element tuElem = (Element) list.get(i);
                Element srcElem = tuElem.getChild("source", namespace);
                // Get Source Segment
                if (srcElem != null && srcElem.getContentSize() > 0)
                {
                    String source = getInnerXMLString2(srcElem);
                    srcSegments.add(source);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Parse XLF file error: " + e);
        }
        
        for (int i = 0; i < srcSegments.size(); i++)
        {
            System.out.println("SourceSegments[" + i + "]: \t" + srcSegments.get(i));
        }
    }
    
    public static void parseXLFWrapper(String p_fileName)
    {
        JobBO job = new JobBO("2014");
        File file = new File(p_fileName);
        parseXLF(job, file);
        System.out.println("***********************************************");
        System.out.println("Parse File: \t\t" + file.getAbsolutePath());
        System.out.println("Source Language: \t" + job.getSourceLanguage());
        System.out.println("Target Language: \t" + job.getTargetLanguage());
        System.out.println("SourceSegments Size: \t" + (job.getSourceSegments() == null ? 0 : job.getSourceSegments().length));
        
        for (int i = 0; i < job.getSourceSegments().length; i++)
        {
            System.out.println("SourceSegments[" + i + "]: \t" + job.getSourceSegments()[i]);
        }
    }
    
    private static void parseXLF(JobBO p_job, File p_srcFile)
    {
        if (p_srcFile == null || !p_srcFile.exists())
            return;

        String srcLang, trgLang;
        List<String> srcSegments = new ArrayList<String>();

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document read_doc = builder.build(p_srcFile);
            // Get Root Element
            Element root = read_doc.getRootElement();
            Namespace namespace = root.getNamespace();
            Element fileElem = root.getChild("file", namespace);
//            System.out.println(root.getName() + "\t" + root.getNamespace() + "\t" + root.getText());
            
//            for(Element elem :root.getChildren()){
//                System.out.println(elem.getName() + "\t" + elem.getNamespace() + "\t" + elem.getText());
//            }
            
//            if(fileElem == null){
//                System.out.println("Can not find file element.");
//                return;
//            }
            
            // Get Source/Target Language
            srcLang = fileElem.getAttributeValue(XLF_SOURCE_LANGUAGE);
            trgLang = fileElem.getAttributeValue(XLF_TARGET_LANGUAGE);
            List<?> list = fileElem.getChild("body", namespace).getChildren("trans-unit", namespace);
            for (int i = 0; i < list.size(); i++)
            {
                Element tuElem = (Element) list.get(i);
                Element srcElem = tuElem.getChild("source", namespace);
//                printElement(srcElem);
                // Get Source Segment 
                if (srcElem != null && srcElem.getContentSize() > 0)
                {
                    String source = getInnerXMLString2(srcElem);
                    srcSegments.add(source);
                }
            }
            
            p_job.setSourceLanguage(srcLang);
            p_job.setTargetLanguage(trgLang);
            p_job.setSourceSegments(srcSegments);
        }
        catch (Exception e)
        {
//            logger.error("Parse XLF file error: ", e);
            System.out.println("Parse XLF file error: " + e);
        }
    }
    
    private static void printElement(Element p_elem)
    {
        try
        {
            System.out.println("getName:\t" + p_elem.getName());
            System.out.println("getText:\t" + p_elem.getText());
            System.out.println("getTextNormalize:\t" + p_elem.getTextNormalize());
            System.out.println("getTextTrim:\t" + p_elem.getTextTrim());
            System.out.println("getXMLBaseURI:\t" + p_elem.getXMLBaseURI());
            System.out.println("toString:\t" + p_elem.toString());
            System.out.println("outputString(Element):\t" + new XMLOutputter().outputString(p_elem));
            System.out.println("outputString(List<Content>):\t" + new XMLOutputter().outputString(p_elem.getContent()));
        }
        catch (URISyntaxException e)
        {
            System.out.println("Parse element error.\n" + e);
        }
    }
    
    public static String getInnerXMLString2(Element element)
    {
        String elementString = new XMLOutputter().outputString(element);
        int start, end;
        start = elementString.indexOf(">") + 1;
        end = elementString.lastIndexOf("</");
        if (end > 0)
        {
            StringBuilder result = new StringBuilder();
            for (String part : elementString.substring(start, end).split("\r|\n"))
            {
                result.append(part.trim());
            }
            return result.toString();
        }
        else
            return null;
    }
}
