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

package com.globalsight.everest.tm.util.trados;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;

/**
 * <p>
 * Trados-TMX Converter for HTML.
 * </p>
 *
 * <p>
 * This tool reads a Trados TMX file with content from Trados' HTML filter and
 * writes it out as GXML.
 * </p>
 */
public class TradosHtmlTmxToGxml
{
    static public final String s_TOOLNAME = "GlobalSight TradosHtmlTmxToGxml";
    static public final String s_TOOLVERSION = "1.0";

    private Logger m_logger = null;

    private int m_entryCount = 0;
    private int m_errorCount = 0;
    private int m_fileCount = 0;
    private String m_filename;

    private PrintWriter m_writer;

    private String m_version = "";
    private Tmx m_header = null;
    private Tmx m_newHeader = null;

    private boolean m_tuError;

    private HtmlEntities m_htmlDecoder = new HtmlEntities();
    private DiplomatAPI m_diplomat = new DiplomatAPI();

    private SAXReader m_segmentReader = null;

    //
    // Constructors
    //

    public TradosHtmlTmxToGxml()
    {
    }

    public TradosHtmlTmxToGxml(Logger p_logger)
    {
        m_logger = p_logger;
    }

    // *************************************************

    public void debug(String p_message)
    {
        if (m_logger != null)
        {
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug(p_message);
            }
        }
        else
        {
            System.err.println(p_message);
        }
    }

    public void info(String p_message)
    {
        if (m_logger != null)
        {
            m_logger.info(p_message);
        }
        else
        {
            System.err.println(p_message);
        }
    }

    // *************************************************

    public String getBaseName(String p_name)
    {
        return p_name.substring(0, p_name.lastIndexOf("."));
    }

    public String getExtension(String p_name)
    {
        return p_name.substring(p_name.lastIndexOf(".") + 1);
    }

    public void startOutputFile(String p_base) throws Exception
    {
        m_fileCount++;

        m_filename = p_base + "-gxml.tmx";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(m_filename)),
                "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    }

    public void closeOutputFile() throws Exception
    {
        m_writer.println("</body>");
        m_writer.println("</tmx>");
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.println(p_message);
    }

    // ******************************************

    public String handleTuv(String p_segment) throws Throwable
    {
        try
        {
            // debug("Segment=" + p_segment);

            Output output = extractSegment(p_segment);
            String gxml = getFirstSegment(output);

            // debug("GXML=" + gxml);

            return gxml;
        }
        catch (Throwable ex)
        {
            info("Segment could not be converted: " + ex.getMessage() + "\n"
                    + p_segment);

            throw ex;
        }
    }

    public Output extractSegment(String p_segment) throws Exception
    {
        m_diplomat.reset();
        m_diplomat.setSourceString(p_segment);
        m_diplomat.setInputFormat(IFormatNames.FORMAT_HTML);
        m_diplomat.setEncoding("unicode");
        m_diplomat.setLocale(Locale.US);
        m_diplomat.setSentenceSegmentation(false);

        m_diplomat.extract();

        return m_diplomat.getOutput();
    }

    // After System4 paragraph segmentation, return the first
    // translatable segment.
    public String getFirstSegment(Output p_output)
    {
        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;

                    if (elem.hasSegments())
                    {
                        SegmentNode node = (SegmentNode) elem.getSegments()
                                .get(0);

                        return node.getSegment();
                    }

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    // LocalizableElement elem = (LocalizableElement)de;
                    // return elem.getChunk();

                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }

        return "";
    }

    /**
     * Removes all TMX 1.4 <ut> elements from the segment. <ut> is special since
     * it does not surround embedded tags but text, witch must be pulled out of
     * the <ut> and added to the parent segment.
     */
    private Element removeUtElements(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findUtElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element ut = (Element) elems.get(i);

            ut.detach();
        }

        return p_seg;
    }

    private void findUtElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <ut> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findUtElements(p_result, (Element) child);
            }
        }

        if (p_element.getName().equals("ut"))
        {
            p_result.add(p_element);
        }
    }

    public void setOldHeader(Element p_element)
    {
        m_header = new Tmx(p_element);
    }

    public void createNewHeader()
    {
        m_newHeader = new Tmx();
        m_newHeader.setTmxVersion(Tmx.TMX_GS);
        m_newHeader.setCreationTool(s_TOOLNAME);
        m_newHeader.setCreationToolVersion(s_TOOLVERSION);
        m_newHeader.setSegmentationType("sentence");
        m_newHeader.setOriginalFormat(m_header.getOriginalFormat());
        m_newHeader.setAdminLang(Tmx.DEFAULT_ADMINLANG);
        m_newHeader.setSourceLang(m_header.getSourceLang());
        // TODO: Datatype should be a constant in Tmx.java. The data
        // type should probably be a native identifier ("g-tmx"?)
        // because now we have native data and don't care about where
        // it came from.
        m_newHeader.setDatatype("html");

        m_writer.println(m_newHeader.getTmxDeclaration());
        m_writer.println(m_newHeader.getTmxXml());
        m_writer.println(m_newHeader.getHeaderXml());
        m_writer.println("<body>");
    }

    /**
     * Parses an XML string into a document.
     */
    public Document parse(String p_xml)
    {
        try
        {
            // Don't validate here since main parser has already validated.
            if (m_segmentReader == null)
            {
                m_segmentReader = new SAXReader();
                m_segmentReader
                        .setXMLReaderClassName("org.dom4j.io.aelfred.SAXDriver");
                m_segmentReader.setValidation(false);
            }

            return m_segmentReader.read(new StringReader(p_xml));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String convertToGxml(String p_url) throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        info("Converting TMX file to GXML: `" + p_url + "'");

        startOutputFile(baseName);

        m_entryCount = 0;

        // Reading from a file, need to use Xerces.
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        reader.setEntityResolver(DtdResolver.getInstance());
        reader.setValidation(true);

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
                Element element = path.getCurrent();

                m_version = element.attributeValue("version");
            }

            public void onEnd(ElementPath path)
            {
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/header", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();
                setOldHeader(element);
                createNewHeader();

                // prune the current element to reduce memory
                element.detach();

                element = null;
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
                ++m_entryCount;
                m_tuError = false;
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                if (m_tuError)
                {
                    m_errorCount++;
                }
                else
                {
                    writeEntry(element.asXML());
                }

                // prune the current element to reduce memory
                element.detach();

                element = null;

                if (m_entryCount % 1000 == 0)
                {
                    debug("Entry " + m_entryCount);
                }
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu/tuv/seg", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                try
                {
                    element = removeUtElements(element);

                    String gxml = handleTuv(element.getText());
                    Document doc = parse("<root>" + gxml + "</root>");

                    // Remove old content of seg
                    List content = element.content();
                    for (int i = content.size() - 1; i >= 0; --i)
                    {
                        ((Node) content.get(i)).detach();
                    }

                    // Add new GXML content (backwards)
                    content = doc.getRootElement().content();
                    Collections.reverse(content);
                    for (int i = content.size() - 1; i >= 0; --i)
                    {
                        Node node = (Node) content.get(i);
                        element.add(node.detach());
                    }
                }
                catch (Throwable ex)
                {
                    m_tuError = true;
                }
            }
        });

        Document document = reader.read(p_url);

        closeOutputFile();

        info("Processed " + m_entryCount + " TUs into file `" + m_filename
                + "', " + m_errorCount + " errors.");

        return m_filename;
    }

    static public void main(String[] argv) throws Exception
    {
        TradosHtmlTmxToGxml a = new TradosHtmlTmxToGxml();

        if (argv.length != 1)
        {
            System.err.println("Usage: TradosHtmlTmxToGxml FILE\n");
            System.err.println("Converts Trados TMX containing "
                    + "HTML data to GXML.\n");
            System.exit(1);
        }

        a.convertToGxml(argv[0]);
    }
}
