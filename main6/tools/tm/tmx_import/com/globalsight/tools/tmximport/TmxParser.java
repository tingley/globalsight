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
package com.globalsight.tools.tmximport;

import java.io.IOException;
import java.sql.Connection;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.SAXParser;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tm.Tm;
import com.globalsight.util.GlobalSightLocale;

/**
 * Parse a TMX file and save segments to database. Validates TMX files while
 * parsing.
 */
public class TmxParser extends DefaultHandler
{
    private static final REProgram DTD_SEARCH_PATTERN = createSearchPattern("tmx\\d+\\.dtd");

    private static REProgram createSearchPattern(String p_pattern)
    {
        REProgram pattern = null;
        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }
        return pattern;
    }

    // TMX element names
    private static final String TMX_HEADER = "header";
    private static final String TMX_TU = "tu";
    private static final String TMX_TUV = "tuv";
    private static final String TMX_SEG = "seg";
    private static final String TMX_SUB = "sub";
    private static final String TMX_UT = "ut";
    private static final String TMX_PH = "ph";
    private static final String SUB_LOCTYPE = " locType=\"localizable\"";
    private static final String SEGMENT_START = "<segment>";
    private static final String SEGMENT_END = "</segment>";

    // TMX attribute names
    private static final String TMX_SRCLANG = "srclang";
    private static final String TMX_DATATYPE = "datatype";
    private static final String TMX_XMLLANG = "xml:lang";
    private static final String TMX_LANG = "lang";

    private Connection m_connection;
    private XMLReader m_parser = new SAXParser();
    private String m_datatype = "unknown";
    private TuTmx m_tuTmx;
    private TuvTmx m_tuvTmx;
    private Tm m_tm;
    private LeverageGroup m_levGrp;
    private LocaleTmx m_localeTmx;
    private GlobalSightLocale m_sourceLocale;

    private Tu m_tu;
    private Tuv m_tuv;
    private StringBuffer m_segment;
    private boolean m_inSegment = false;

    // default constructor
    public TmxParser(Connection p_connection, Tm p_tm, LeverageGroup p_levGrp)
            throws Exception
    {
        m_connection = p_connection;
        m_tuTmx = new TuTmx(p_connection);
        m_tuvTmx = new TuvTmx(p_connection);
        m_tm = p_tm;
        m_levGrp = p_levGrp;
        m_localeTmx = new LocaleTmx(p_connection, new Lang2Locale());
        m_sourceLocale = null;

        m_parser.setContentHandler(this);
        m_parser.setErrorHandler(this);

        // prepare to read DTDs locally
        m_parser.setEntityResolver(this);

        // validate documents
        m_parser.setFeature("http://xml.org/sax/features/validation", true);
    }

    public void parse(String p_uri) throws Exception
    {
        m_parser.parse(p_uri);
    }

    //
    // DocumentHandler methods
    //

    /** Start element. */
    public void startElement(String p_namespaceURI, String p_localName,
            String p_qName, Attributes p_attrs) throws SAXException
    {
        try
        {
            // store datatype
            if (p_localName.equals(TMX_HEADER))
            {
                // determine datatype
                String datatype = p_attrs.getValue(TMX_DATATYPE);
                if (datatype != null)
                {
                    m_datatype = datatype;
                }

                // determine source locale
                String srcLocale = p_attrs.getValue(TMX_SRCLANG);
                if (srcLocale.equals("*all*"))
                {
                    m_sourceLocale = null;
                }
                else
                {
                    m_sourceLocale = m_localeTmx.get(srcLocale);
                }

            }
            // create TU object
            else if (p_localName.equals(TMX_TU))
            {
                String datatype = p_attrs.getValue(TMX_DATATYPE);
                m_tu = m_tuTmx.create(datatype == null ? m_datatype : datatype,
                        m_tm, m_levGrp);
                m_tuTmx.save(m_tu);
            }
            // create TUV object
            else if (p_localName.equals(TMX_TUV))
            {
                String locale = p_attrs.getValue(TMX_XMLLANG);
                if (locale == null)
                {
                    locale = p_attrs.getValue(TMX_LANG);
                }
                m_tuv = m_tuvTmx.create(m_localeTmx.get(locale), m_tu);
            }
            // start collecting segment text data
            else if (p_localName.equals(TMX_SEG))
            {
                m_segment = new StringBuffer(SEGMENT_START);
                m_inSegment = true;
            }
            else
            {
                if (m_inSegment)
                {
                    String elementName = p_localName;
                    if (elementName.equals(TMX_UT))
                    {
                        elementName = TMX_PH;
                    }

                    // add it to segment
                    m_segment.append('<');
                    m_segment.append(elementName);
                    if (p_attrs != null)
                    {
                        int len = p_attrs.getLength();
                        for (int i = 0; i < len; i++)
                        {
                            m_segment.append(' ');
                            m_segment.append(p_attrs.getQName(i));
                            m_segment.append("=\"");
                            m_segment.append(normalize(p_attrs.getValue(i)));
                            m_segment.append('"');
                        }
                    }

                    // add locType attribute to <sub> element
                    if (p_localName.equals(TMX_SUB))
                    {
                        m_segment.append(SUB_LOCTYPE);
                    }

                    m_segment.append('>');
                }
            }

        }
        catch (Exception e)
        {
            throw new SAXException(e);
        }
    }

    /** Characters. */
    public void characters(char ch[], int start, int length)
    {
        if (m_inSegment)
        {
            m_segment.append(normalize(new String(ch, start, length)));
        }
    }

    /** Ignorable whitespace. */
    public void ignorableWhitespace(char ch[], int start, int length)
    {
        if (m_inSegment)
        {
            characters(ch, start, length);
        }
    }

    /** End element. */
    public void endElement(String p_namespaceURI, String p_localName,
            String p_qName) throws SAXException
    {
        try
        {
            // save TUV
            if (p_localName.equals(TMX_TUV))
            {
                m_tuvTmx.setSegment((TuvImpl) m_tuv, m_segment.toString());
                m_tuvTmx.save((TuvImpl) m_tuv);

                // commit the change so that indexer doesn't complain
                // abount not finding TUV id
                m_connection.commit();

                // index source locale segment
                if (m_sourceLocale == null
                        || m_sourceLocale.equals(m_tuv.getGlobalSightLocale()))
                {
                    IndexerTmx.index(m_tuv);
                }
            }
            // stop collecting segment text data
            else if (p_localName.equals(TMX_SEG))
            {
                m_segment.append(SEGMENT_END);
                m_inSegment = false;
            }
            else
            {
                if (m_inSegment)
                {
                    String elementName = p_localName;
                    if (elementName.equals(TMX_UT))
                    {
                        elementName = TMX_PH;
                    }

                    // add it to segment
                    m_segment.append("</");
                    m_segment.append(elementName);
                    m_segment.append(">");
                }
            }
        }
        catch (Exception e)
        {
            throw new SAXException(e);
        }

    }

    //
    // ErrorHandler methods
    //

    /** Warning. */
    public void warning(SAXParseException ex)
    {
        System.err.println("[Warning] " + getLocationString(ex) + ": "
                + ex.getMessage());
    }

    /** Error. */
    public void error(SAXParseException ex) throws SAXException
    {
        System.err.println("[Error] " + getLocationString(ex) + ": "
                + ex.getMessage());
        throw ex;
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException
    {
        System.err.println("[Fatal Error] " + getLocationString(ex) + ": "
                + ex.getMessage());
        throw ex;
    }

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex)
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

    //
    // Protected static methods
    //

    /** Normalizes the given string. */
    protected String normalize(String s)
    {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++)
        {
            char ch = s.charAt(i);
            switch (ch)
            {
                case '<':
                    str.append("&lt;");
                    break;
                case '>':
                    str.append("&gt;");
                    break;
                case '&':
                    str.append("&amp;");
                    break;
                case '"':
                    str.append("&quot;");
                    break;
                default:
                    str.append(ch);
            }
        }

        return str.toString();

    }

    /**
     * Overrides EntityResolver#resolveEntity.
     *
     * Provide a right DTD to read. If unrecognizable DTD is specified, this
     * method returns a null stream.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
    {
        InputSource in = null;

        RE matcher = new RE(DTD_SEARCH_PATTERN, RE.MATCH_SINGLELINE);
        if (matcher.match(systemId))
        {
            InputStream ist = TmxParser.class.getResourceAsStream("/resources/"
                    + matcher.getParen(0));

            if (ist != null)
            {
                in = new InputSource(ist);
            }
        }

        if (in == null)
        {
            in = new InputSource(new ByteArrayInputStream(new byte[0]));
        }

        return in;
    }

}
