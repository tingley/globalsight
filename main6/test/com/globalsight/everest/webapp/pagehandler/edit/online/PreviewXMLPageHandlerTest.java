package com.globalsight.everest.webapp.pagehandler.edit.online;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.xalan.xpath.xdom.XercesLiaison;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.util.FileUtil;

public class PreviewXMLPageHandlerTest
{
    private File xmlFile = null;
    private File xslFile = null;
    private String codeset = null;

    @Before
    public void init()
    {
        String xml = com.globalsight.everest.unittest.util.FileUtil.getResourcePath(
                this.getClass(), "xml/source.xml");
        String xsl = com.globalsight.everest.unittest.util.FileUtil.getResourcePath(
                this.getClass(), "xml/stringset.xsl");

        xmlFile = new File(xml);
        xslFile = new File(xsl);
        codeset = "UTF-8";
    }
    
    /**
     * For GBS-1849 : popup editor XSL Preview displaying non-ASCII characters as '?'
     * @throws Exception
     */
    @Test
    public void testPreviewXmlWithXsl() throws Exception
    {
        try
        {
            // read data
            byte[] data = FileUtil.readFile(xmlFile, (int) xmlFile.length());
            String guessEncoding = FileUtil.guessEncoding(xmlFile);
            String xmldata = null;

            if (guessEncoding != null)
            {
                if (FileUtil.UTF8.equals(guessEncoding))
                {
                    byte[] newdata = new byte[data.length - 3];
                    newdata = Arrays.copyOfRange(data, 3, data.length);
                    data = newdata;
                }
                else if (FileUtil.UTF16BE.equals(guessEncoding)
                        || FileUtil.UTF16LE.equals(guessEncoding))
                {
                    byte[] newdata = new byte[data.length - 2];
                    newdata = Arrays.copyOfRange(data, 2, data.length);
                    data = newdata;
                }

                xmldata = new String(data, guessEncoding);
            }
            else
            {
                xmldata = new String(data, codeset);
            }

            // write data with utf-8
            File newxmlFile = File.createTempFile("~GS", ".xml");
            FileUtil.writeFile(newxmlFile, xmldata, "UTF-8");
            xmlFile = newxmlFile;

            // get html preview with xml and xsl
            XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new XercesLiaison());
            StringWriter sw = new StringWriter();
            FileInputStream xmlInputStream = new FileInputStream(xmlFile.getAbsolutePath());
            FileInputStream xslInputStream = new FileInputStream(xslFile.getAbsolutePath());
            InputStreamReader xmlReader = new InputStreamReader(xmlInputStream, "UTF-8");
            XSLTResultTarget xslResult = new XSLTResultTarget(sw);

            processor.process(new XSLTInputSource(xmlReader), new XSLTInputSource(xslInputStream),
                    xslResult);

            String html = sw.getBuffer().toString();

            // assert
            System.out.println(html);
            assertTrue(html.contains("<TD width=\"80%\"><B>String</B></TD>"));
            assertTrue(html.contains("<TD width=\"80%\">\u9898\u76ee</TD>"));
            assertTrue(html.contains("<TD width=\"80%\">\u5185\u5bb9</TD>"));
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(PreviewXMLPageHandlerTest.class);
    }
}
