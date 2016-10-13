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
package com.globalsight.diplomat.util;

import java.util.Vector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.net.URL;

import org.apache.xerces.parsers.DOMParser;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.XmlParser;

/**
 * Abstract class to define common behavior for xml generators.
 */
public class XmlUtil
{
    static private final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(XmlUtil.class);

    // these constants are the name of the DTD and the root element
    private static final String PRSXMLDTD = "/com/globalsight/resources/xml/PrsXml.dtd";
    private static final String PRSXMLROOT = "paginatedResultSetXml";
    private static final String EFXMLDTD = "/com/globalsight/resources/xml/EventFlowXml.dtd";
    private static final String EFXMLROOT = "eventFlowXml";
    private static final String PUXMLDTD = "/com/globalsight/resources/xml/PreviewUrlXml.dtd";
    private static final String PUXMLROOT = "previewUrlXml";
    private static final String L10NREQXMLDTD = "/com/globalsight/resources/xml/L10nRequestXml.dtd";
    private static final String L10NREQXMLROOT = "l10nRequestXml";

    private static final String ESCAPABLE_CHARS = "<>&'\"";
    private static final String LT = "&lt;";
    private static final String GT = "&gt;";
    private static final String AMP = "&amp;";
    private static final String APOS = "&apos;";
    private static final String QUOT = "&quot;";
    private static final String[] ESCAPED_STRINGS =
    { LT, GT, AMP, APOS, QUOT };

    // Singleton object
    private static XmlUtil s_singleton = new XmlUtil();

    // private constructor
    private XmlUtil()
    {
    }

    // private members
    private String[] m_PrsXmlDtd = null;
    private String[] m_EventFlowXmlDtd = null;
    private String[] m_PreviewUrlXmlDtd = null;
    private String[] m_L10nRequestXmlDtd = null;

    // private utility for reading the DTD from the CLASSPATH
    private String[] readDtd(String p_dtd, String p_rootElement)
    {
        String[] dtd = null;
        try
        {
            Vector v = new Vector();
            URL u = XmlUtil.class.getResource(p_dtd);
            InputStream is = u.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String s = null;
            while ((s = br.readLine()) != null)
                v.add(s);

            // now make an embedded DTD
            dtd = new String[v.size() + 2];
            dtd[0] = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE "
                    + p_rootElement + " [";
            for (int i = 0; i < v.size(); i++)
                dtd[i + 1] = (String) v.get(i);
            dtd[v.size() + 1] = "]>\n";
        }
        catch (Exception e)
        {
            Logger.getLogger().printStackTrace(Logger.ERROR,
                    "Reading DTD: " + p_dtd, e);
        }

        return dtd;
    }

    private String[] getPrsXmlDtd()
    {
        if (m_PrsXmlDtd == null)
            m_PrsXmlDtd = readDtd(PRSXMLDTD, PRSXMLROOT);
        return m_PrsXmlDtd;
    }

    private String[] getEventFlowXmlDtd()
    {
        if (m_EventFlowXmlDtd == null)
            m_EventFlowXmlDtd = readDtd(EFXMLDTD, EFXMLROOT);
        return m_EventFlowXmlDtd;
    }

    private String[] getPreviewUrlXmlDtd()
    {
        if (m_PreviewUrlXmlDtd == null)
            m_PreviewUrlXmlDtd = readDtd(PUXMLDTD, PUXMLROOT);
        return m_PreviewUrlXmlDtd;
    }

    private String[] getL10nRequestXmlDtd()
    {
        if (m_L10nRequestXmlDtd == null)
            m_L10nRequestXmlDtd = readDtd(L10NREQXMLDTD, L10NREQXMLROOT);
        return m_L10nRequestXmlDtd;
    }

    /**
     * Return the EventFlowXml DTD as an array of strings.
     * 
     * @return the current array of strings
     */
    public static String[] eventFlowXmlDtd()
    {
        return filter(s_singleton.getEventFlowXmlDtd(), false);
    }

    /**
     * Return the PaginatedResultSetXml DTD as an array of strings.
     * 
     * @return the current array of strings
     */
    public static String[] paginatedResultSetXmlDtd()
    {
        return filter(s_singleton.getPrsXmlDtd(), false);
    }

    /**
     * Return the PreviewUrlXml DTD as an array of strings.
     * 
     * @return the current array of strings
     */
    public static String[] previewUrlXmlDtd()
    {
        return filter(s_singleton.getPreviewUrlXmlDtd(), false);
    }

    private static String[] filter(String[] p_array, boolean p_includeComments)
    {
        String[] result = p_array;
        if (!p_includeComments)
        {
            Vector v = new Vector();
            for (int i = 0; i < p_array.length; i++)
            {
                String s = p_array[i];
                if (s.indexOf("<!--") < 0)
                {
                    v.addElement(s);
                }
            }
            result = new String[v.size()];
            for (int i = 0; i < v.size(); i++)
            {
                result[i] = (String) v.elementAt(i);
            }
        }
        return result;
    }

    /**
     * Convert all the escapable characters in the given string into their
     * string (escaped) equivalents. Return the converted string.
     * 
     * @param p_str
     *            the string to be modified.
     * 
     * @return the escaped result.
     */
    public static String escapeString(String p_str)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < p_str.length(); i++)
        {
            char c = p_str.charAt(i);
            int index = ESCAPABLE_CHARS.indexOf((int) c);
            if (index > -1)
            {
                sb.append(ESCAPED_STRINGS[index]);
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert all the escaped strings in the given string into their character
     * (unescaped) equivalents. Return the converted string.
     * 
     * @param p_str
     *            the string to be modified.
     * 
     * @return the escaped result.
     */
    public static String unescapeString(String p_str)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < p_str.length(); i++)
        {
            char c = p_str.charAt(i);
            boolean found = false;
            if (c == '&')
            {
                for (int j = 0; !found && j < ESCAPED_STRINGS.length; j++)
                {
                    String s = ESCAPED_STRINGS[j];
                    int index = p_str.indexOf(s, i);
                    if (found = (index == i))
                    {
                        sb.append(ESCAPABLE_CHARS.charAt(j));
                        i += (s.length() - 1);
                    }
                }
            }

            if (!found)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Return a formatted string representing the L10nRequestXml without
     * comments.
     * 
     * @return a formatted string.
     */
    public static String formattedL10nRequestXmlDtd()
    {
        return format(filter(s_singleton.getL10nRequestXmlDtd(), false));
    }

    /**
     * Return a formatted string representing the preview Url Xml without
     * comments.
     * 
     * @return a formatted string.
     */
    public static String formattedPreviewUrlXmlDtd()
    {
        return format(filter(s_singleton.getPreviewUrlXmlDtd(), false));
    }

    /**
     * Return a formatted string representing the paginated result set xml
     * without comments.
     * 
     * @return a formatted string.
     */
    public static String formattedPaginatedResultSetXmlDtd()
    {
        return format(filter(s_singleton.getPrsXmlDtd(), false));
    }

    /**
     * Return a formatted string representing the event flow xml without
     * comments.
     * 
     * @return a formatted string.
     */
    public static String formattedEventFlowXmlDtd()
    {
        return format(filter(s_singleton.getEventFlowXmlDtd(), false));
    }

    /**
     * Return a formatted string representing the preview Url Xml with comments.
     * 
     * @return a formatted string.
     */
    public static String formattedPreviewUrlXmlDtdWithComments()
    {
        return format(filter(s_singleton.getPreviewUrlXmlDtd(), true));
    }

    /**
     * Return a formatted string representing the L10nRequest Xml with comments.
     * 
     * @return a formatted string.
     */
    public static String formattedL10nRequestXmlDtdWithComments()
    {
        return format(filter(s_singleton.getL10nRequestXmlDtd(), true));
    }

    /**
     * Return a formatted string representing the paginated result set xml with
     * comments.
     * 
     * @return a formatted string.
     */
    public static String formattedPaginatedResultSetXmlDtdWithComments()
    {
        return format(filter(s_singleton.getPrsXmlDtd(), true));
    }

    /**
     * Return a formatted string representing the event flow xml with comments.
     * 
     * @return a formatted string.
     */
    public static String formattedEventFlowXmlDtdWithComments()
    {
        return format(filter(s_singleton.getEventFlowXmlDtd(), true));
    }

    /* Format the given set array of strings into an actual DTD file. */
    private static String format(String[] p_dtdText)
    {
        StringBuffer sb = new StringBuffer();
        String tab = "    ";
        for (int i = 0; i < p_dtdText.length; i++)
        {
            String s = p_dtdText[i];
            int indent = ((i > 0 && i < p_dtdText.length - 1) ? 1 : 0);
            if (indent > 0 && !s.startsWith("<"))
            {
                indent++;
            }
            for (int j = 0; j < indent; j++)
            {
                sb.append(tab);
            }
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String format(String xml)
    {
        return format(xml, null);
    }

    public static String format(String xml, EntityResolver entityResolver) {
        XmlParser parser = XmlParser.hire();
        if (entityResolver != null) {
            parser.setEntityResolver(entityResolver);
        }
        org.dom4j.Document dom = parser.parseXml(xml);
        OutputFormat format = new OutputFormat();
        format.setNewlines(true);
        format.setIndent(true);       

        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        try
        {
            xmlWriter.write(dom);
            xmlWriter.close();
        }
        catch (IOException e)
        {
            return xml;
        }
        finally
        {
            try
            {
                xmlWriter.close();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }

        return writer.getBuffer().toString();        
    }

    /**
     * Parses the given string with DOM (no validation)
     * 
     * @return the root element
     */
    public static Element parseForRootElement(String p_xml)
            throws SAXException, IOException
    {
        StringReader sr = new StringReader(p_xml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false); // don't
        // validate
        parser.parse(is);
        return parser.getDocument().getDocumentElement();
    }
    
    /**
     * Construct an empty entity resolver to avoid invalid entity link problem
     * in DOCTYPE.
     */
    public static EntityResolver getNullEntityResolver() {
        EntityResolver result = new EntityResolver() {
            String emptyDtd = "";
            ByteArrayInputStream byteIs = new ByteArrayInputStream(emptyDtd
                    .getBytes());

            public InputSource resolveEntity(String publicId, String systemId)
                    throws IOException
            {
                return new InputSource(byteIs);
            }
        };
        
        return result;
    }
}
