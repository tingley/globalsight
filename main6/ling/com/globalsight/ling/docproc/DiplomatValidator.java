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
package com.globalsight.ling.docproc;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;


/**
 * Validates a Diplomat XML string as produced by DiplomatAPI#extract().
 */
public class DiplomatValidator
    extends DefaultHandler
{
    /**
     * <p>The SGML document type declaration of a Diplomat XML
     * file.  Should not be hardcoded but stored in cxe/config.</p>
     */
    public static final String DIPLOMAT_DOCTYPE_DECL =
        "\n<!DOCTYPE diplomat [\n" +
        "<!-- diplomat.dtd created on May 15, 2000 -->\n" +
        "<!-- version 1.0 -->\n" +
        "<!-- by Shigemichi Yazawa, GlobalSight Corp. -->\n" +
        "<!-- Mon Jun 17 15:33:38 2002 Cornelis Van Der Laan: -->\n" +
        "<!-- Renamed GSA to GS, value of DELETE is no \"yes\", not 1. -->\n" +
        "<!-- Thu Jan 29 00:54:22 2004 Cornelis Van Der Laan: -->\n" +
        "<!-- Added 'i' attribute to IT to allow split/merge -->\n" +
        "<!-- to convert combined IT back to BPT/EPT.  -->\n" +
        "\n" +
        "<!ELEMENT diplomat (translatable|localizable|skeleton|gs)*>\n" +
        "<!ATTLIST diplomat \n" +
        "   version     CDATA   #REQUIRED\n" +
        "   locale      CDATA   #REQUIRED\n" +
        "   datatype    CDATA   #REQUIRED\n" +
        "   targetEncoding  CDATA   #IMPLIED\n" +
        "   wordcount   CDATA   #IMPLIED>\n" +
        "\n" +
        "<!ELEMENT translatable (segment)+>\n" +
        "<!ATTLIST translatable\n" +
        "   blockId     CDATA   #REQUIRED\n" +
        "   datatype    CDATA   #IMPLIED\n" +
        "   type        CDATA   \"text\"\n" +
        "   wordcount   CDATA   #IMPLIED>\n" +
        "\n" +
        "<!ELEMENT localizable (#PCDATA|bpt|ept|it|ph|ut)*>\n" +
        "<!ATTLIST localizable \n" +
        "   blockId     CDATA   #REQUIRED\n" +
        "   datatype    CDATA   #IMPLIED\n" +
        "   type        CDATA   #IMPLIED\n" +
        "   wordcount   CDATA   #IMPLIED>\n" +
        "\n" +
        "<!ELEMENT segment (#PCDATA|bpt|ept|it|ph|ut|localizable)*>\n" +
        "<!ATTLIST segment\n" +
        "   segmentId   CDATA   #REQUIRED\n" +
        "   wordcount   CDATA   #IMPLIED>\n" +
        "\n" +
        "<!ELEMENT gs (translatable|localizable|skeleton|presentation|gs)*>\n" +
        "<!ATTLIST gs\n" +
        "   extract     CDATA   #IMPLIED\n" +
        "   add         CDATA   #IMPLIED\n" +
        "   delete      (yes|no) \"yes\"\n" +
        "   added       CDATA   #IMPLIED\n" +
        "   deleted     CDATA   #IMPLIED\n" +
        "   name        CDATA   #IMPLIED\n" +
        "   id          CDATA   #IMPLIED>\n" +
        "\n" +
        "<!ELEMENT skeleton (#PCDATA)>\n" +
        "\n" +
        "<!ELEMENT bpt (#PCDATA|sub)*>\n" +
        "<!ATTLIST bpt\n" +
        "   i    CDATA  #REQUIRED\n" +
        "   x    CDATA  #IMPLIED\n" +
        "   type CDATA  #IMPLIED\n" +
        "   erasable (yes|no)  \"no\"\n" +
        "   movable  (yes|no)  \"yes\">\n" +
        "\n" +
        "<!ELEMENT ept (#PCDATA|sub)*>\n" +
        "<!ATTLIST ept\n" +
        "   i        CDATA  #REQUIRED>\n" +
        "\n" +
        "<!ELEMENT sub (#PCDATA|bpt|ept|it|ph|ut)*>\n" +
        "<!ATTLIST sub\n" +
        "   locType     (translatable|localizable) \"translatable\"\n" +
        "   wordcount   CDATA   #IMPLIED\n" +
        "   datatype    CDATA   #IMPLIED\n" +
        "   type        CDATA   #IMPLIED>\n" +
        "\n" +
        "<!ELEMENT it (#PCDATA|sub)*>\n" +
        "<!ATTLIST it\n" +
        "   pos  (begin|end)    #REQUIRED\n" +
        "   i    CDATA  #IMPLIED\n" +
        "   x    CDATA  #IMPLIED\n" +
        "   type CDATA  #IMPLIED\n" +
        "   erasable (yes|no)  \"no\"\n" +
        "   movable  (yes|no)  \"yes\">\n" +
        "\n" +
        "<!ELEMENT ph (#PCDATA|sub)*>\n" +
        "<!ATTLIST ph\n" +
        "   assoc   (p|f|b) #IMPLIED\n" +
        "   x       CDATA   #IMPLIED\n" +
        "   type    CDATA   #IMPLIED\n" +
        "   erasable (yes|no)  \"no\"\n" +
        "   movable  (yes|no)  \"yes\">\n" +
        "\n" +
        "<!ELEMENT ut (#PCDATA)>\n" +
        "<!ATTLIST ut\n" +
        "   x   CDATA   #IMPLIED\n" +
        "   erasable (yes|no)  \"no\"\n" +
        "   movable  (yes|no)  \"yes\">\n" +
        "]>";

    // SY 10/28/03
    // Changed the parser from xerces to dom4j due to bad characters
    // (half surrogate) contained in some Word HTML. Xerces barks,
    // dom4j doesn't care. Although it is obvious which is better
    // parser, we don't want to spend too much time on this.
    //
    // CL 01/29/04
    // Unfortunately the AElfred parser can't validate, so DOM4J is
    // missing the point. This is a debugging aid, not a product feature.
    //
    // XERCES: org.apache.xerces.parsers.SAXParser
    // DOM4J:  org.dom4j.io.aelfred.SAXDriver

    /** Default XML parser name. */
    private static final String
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    /** Error return string. */
    private String str_Error = null;

    //
    // Public methods
    //

    /**
     * Validates a Diplomat XML string and returns an error string on
     * error or null if validation was successful.  The Diplomat DTD
     * is inserted automatically into the input string.
     */
    public String validate(String p_xml)
    {
        try
        {
            // Append dtd after <?xml .. ?> xml declaration.
            int i_at = p_xml.indexOf("?>");
            String str_xml = p_xml.substring(0, i_at + 2) +
              DIPLOMAT_DOCTYPE_DECL + p_xml.substring(i_at + 3);

            InputSource input = new InputSource (new StringReader (str_xml));
            XMLReader parser = (XMLReader)Class.forName(DEFAULT_PARSER_NAME).
              newInstance();
            parser.setContentHandler(this);
            parser.setErrorHandler(this);

            parser.setFeature( "http://xml.org/sax/features/validation", true);
            parser.setFeature( "http://xml.org/sax/features/namespaces", true);

            parser.parse(input);
        }
        catch (org.xml.sax.SAXParseException spe)
        {
            // error already set
        }
        catch (org.xml.sax.SAXException se)
        {
            if (se.getException() != null)
            {
                str_Error = se.getException().toString();
            }
            else
            {
                str_Error = se.toString();
            }
        }
        catch (Throwable e)
        {
            str_Error = e.toString();
        }

        return str_Error;
    }

    //
    // ErrorHandler Interface methods
    //

    public void warning(SAXParseException ex)
        throws SAXException
    {
        str_Error = "[Warning] " + getLocationString(ex) +
          ": " + ex.getMessage();
        throw ex;
    }

    public void error(SAXParseException ex)
        throws SAXException
    {
        str_Error = "[Error] " + getLocationString(ex) +
          ": " + ex.getMessage();
        throw ex;
    }

    public void fatalError(SAXParseException ex)
        throws SAXException
    {
        str_Error = "[Fatal Error] " + getLocationString(ex) +
          ": " + ex.getMessage();
        throw ex;
    }

    //
    // Private methods
    //

    /** Returns a string of the location. */
    private static String getLocationString(SAXParseException ex)
    {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null)
        {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
            {
                systemId = systemId.substring(index + 1);
            }
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    }
}
