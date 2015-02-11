package com.globalsight.ling.docproc.extractor.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.log4j.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorInterface;

public class XmlFilterChecker implements EntityResolver, ErrorHandler
{
    private static final Logger s_log = Logger
            .getLogger(XmlFilterChecker.class);

    public static void checkWellFormed(Reader p_reader) throws Exception
    {
        try
        {
            XmlFilterChecker checker = new XmlFilterChecker();
            
            XMLReader reader = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser");
            reader.setEntityResolver(checker);
            reader.setErrorHandler(checker);
            
            reader.parse(new InputSource(p_reader));
        }
        catch (SAXException sax)
        {
            s_log.error("Check Well-Formedness failed.");
            throw sax;
        }
        catch (Exception e)
        {
            s_log.error("Check Well-Formedness failed.");
            throw e;
        }

        s_log.info("Check Well-Formedness successful.");
    }

    public static void checkWellFormed(String data) throws Exception
    {
        String newData = data.replace("&nbsp;", "&amp;nbsp;");
        Reader r = new StringReader(newData);
        checkWellFormed(r);
    }

    /**
     * Overrides EntityResolver#resolveEntity.
     * 
     * The purpose of this method is to read Schemarules.dtd from resource and
     * feed it to the validating parser, but what it really does is returning a
     * null byte array to the XML parser.
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
            IOException
    {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    public void error(SAXParseException e) throws SAXException
    {
        throw new SAXException("XML parse error at\n  line " + e.getLineNumber() + "\n  column "
                + e.getColumnNumber() + "\n  Message:" + e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void warning(SAXParseException e)
    {
        String msg = "XML parse warning at\n  line " + e.getLineNumber() + "\n  column "
                + e.getColumnNumber() + "\n  Message:" + e.getMessage();

    }
}
