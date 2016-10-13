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

package com.globalsight.everest.tm.util.ttx;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.tm.util.Ttx;

import com.globalsight.util.UTC;

import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.Text;


import org.dom4j.*;
import org.dom4j.io.SAXReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.regex.Pattern;

import java.util.*;
import java.io.*;

/**
 * <p>Trados-TTX to TMX Converter that writes out the TUs in a TTX
 * file in TMX format.</p>
 */
public class TtxToTmx
{
    static public final String s_TOOLNAME = "GlobalSight TtxToTmx";
    static public final String s_TOOLVERSION = "1.0";

    private Logger m_logger = null;

    private DocumentFactory m_factory = new DocumentFactory();

    private Ttx m_header;
    private String m_version = Ttx.TTX_20;

    private int m_entryCount = 0;
    private int m_errorCount = 0;
    private String m_filename;

    private PrintWriter m_writer;

    //
    // Constructors
    //

    public TtxToTmx ()
    {
    }

    public TtxToTmx (Logger p_logger)
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

    public void error(String p_message)
    {
        if (m_logger != null)
        {
            m_logger.error(p_message);
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

    public void startOutputFile(String p_base)
        throws Exception
    {
        m_filename = p_base + ".tmx";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
            new BufferedOutputStream(new FileOutputStream(m_filename)),
            "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

        // Parser must have read old header.
        Tmx header = createTmxHeader();

        m_writer.println(header.getTmxDeclaration());

        m_writer.println("<tmx version=\"" + header.getTmxVersion() + "\">");
        m_writer.print(header.getHeaderXml());
        m_writer.println("<body>");
    }

    public void closeOutputFile()
        throws Exception
    {
        m_writer.println("</body>");
        m_writer.println("</tmx>");
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.print(p_message);
    }

    // ******************************************

    /**
     * Removes TTX formatting elements (DF).
     */
    private void removeDfElements(Element p_tu)
    {
        ArrayList elems = new ArrayList();

        while (true)
        {
            elems.clear();

            findDfElements(elems, p_tu);

            if (elems.size() == 0)
            {
                break;
            }

            for (int i = 0, max = elems.size(); i < max; i++)
            {
                Element elem = (Element)elems.get(i);

                removeElement(elem);
            }
        }
    }

    /**
     * Finds TTX formatting elements (DF).
     */
    private void findDfElements(ArrayList p_result, Element p_element)
    {
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node)p_element.node(i);

            if (child instanceof Element)
            {
                findDfElements(p_result, (Element)child);
            }
        }

        String name = p_element.getName();

        if (name.equals(Ttx.DF))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Finds TTX/TMX unknown tag elements (UT).
     */
    private void findUtElements(ArrayList p_result, Element p_element)
    {
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node)p_element.node(i);

            if (child instanceof Element)
            {
                findUtElements(p_result, (Element)child);
            }
        }

        String name = p_element.getName();

        if (name.equals(Ttx.UT))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Removes am element by pulling up its children into the parent node.
     */
    private void removeElement(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <ut>'s content
        // instead of the <ut>.

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node)content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node)newContent.get(i);

            if (i == index)
            {
                parent.appendContent(p_element);
            }
            else
            {
                parent.add(node);
            }
        }
    }

    /**
     * Creates new elements for TU and TUV with lowercase names by
     * constructing new elements and inserting them in place of the
     * old ones.
     */
    private Element lowercaseElements(Element p_tu)
    {
        Element tu = m_factory.createElement("tu");
        tu.setAttributes(p_tu.attributes());

        List tuvs = p_tu.selectNodes("//Tuv");

        for (int i = 0, max = tuvs.size(); i < max; i++)
        {
            Element tuv = (Element)tuvs.get(i);
            // Detach the TUV or else TUVs will accumulate on
            // subsequent TUs (sic!).
            tuv.detach();

            Element newTuv = m_factory.createElement("tuv");
            newTuv.setAttributes(tuv.attributes());
            newTuv.setContent(tuv.content());

            tu.add(newTuv);
        }

        return tu;
    }

    /**
     * Removes all non-standard UT attributes like `Type="start"
     * RightEdge="angle" DisplayText="font"' (= all of them, ut has no
     * attributes in TMX).
     */
    private void fixUtElements(Element p_tu)
    {
        ArrayList elems = new ArrayList();

        findUtElements(elems, p_tu);

        for (int i = 0, max = elems.size(); i < max; i++)
        {
            Element elem = (Element)elems.get(i);

            elem.attributes().clear();
        }
    }

    /**
     * Lowercases relevant TMX attributes like &lt;tuv lang&gt;.
     */
    private void lowercaseAttributes(Element p_tu)
    {
        // TUVs have been lowercased already (need `.')
        List tuvs = p_tu.selectNodes(".//tuv");

        // Rename uppercase <tuv Lang> to lowercase lang.
        for (int i = 0, max = tuvs.size(); i < max; i++)
        {
            Element tuv = (Element)tuvs.get(i);

            Attribute attr = tuv.attribute(Ttx.LANG);
            tuv.remove(attr);

            tuv.addAttribute("xml:lang", attr.getValue());
        }
    }

    /**
     * Wraps textual content of TTX TUVs inside TMX &lt;seg&gt;.
     */
    private void addSegElement(Element p_tu)
    {
        ArrayList newTuvs = new ArrayList();
        List tuvs = p_tu.selectNodes(".//tuv");

        for (int i = 0, max = tuvs.size(); i < max; i++)
        {
            Element tuv = (Element)tuvs.get(i);

            Element seg = m_factory.createElement("seg");
            seg.setContent(tuv.content());
            tuv.content().clear();
            tuv.add(seg);
        }
    }

    /**
     * Removes all TTX &lt;df&gt; elements from the TU leaving any
     * &lt;ut&gt; in place. They will be handled by the normal TMX
     * import routines.
     */
    private Element cleanupTu(Element p_tu)
    {
        removeDfElements(p_tu);

        Element tu = lowercaseElements(p_tu);
        fixUtElements(tu);
        lowercaseAttributes(tu);
        addSegElement(tu);

        return tu;
    }

    public void setTtxHeader(Element p_element)
    {
        m_header = new Ttx(p_element);
    }

    public Tmx createTmxHeader()
    {
        Tmx result = new Tmx();

        result.setTmxVersion(Tmx.TMX_14);
        result.setCreationTool(s_TOOLNAME);
        result.setCreationToolVersion(s_TOOLVERSION);
        result.setSegmentationType(Tmx.SEGMENTATION_SENTENCE);
        result.setOriginalFormat("ttx");
        result.setAdminLang("EN-US");
        result.setSourceLang(m_header.getSourceLanguage());
        result.setDatatype(m_header.getDatatype());

        // Copy additional information into props for the record.
        result.addProp("OriginalCreationDate",
            UTC.valueOfNoSeparators(m_header.getCreationDate()));
        result.addProp("OriginalCreationTool",
            m_header.getCreationTool());
        result.addProp("OriginalCreationToolVersion",
            m_header.getCreationToolVersion());
        result.addProp(Ttx.SETTINGSNAME,
            m_header.getSettingsName());
        result.addProp(Ttx.SETTINGSPATH,
            m_header.getSettingsPath());
        result.addProp(Ttx.TARGETDEFAULTFONT,
            m_header.getTargetDefaultFont());
        result.addProp(Ttx.SOURCEDOCUMENTPATH,
            m_header.getSourceDocumentPath());

        return result;
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String convertTtxToTmx(String p_url)
        throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(baseName);

        info("Converting TTX file to TMX: `" + p_url + "'");

        m_entryCount = 0;

        // Reading from a file, need to use Xerces.
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        //reader.setEntityResolver(DtdResolver.getInstance());
        //reader.setValidation(true);

        // Fetch the version info early.
        reader.addHandler("/TRADOStag",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        m_version = element.attributeValue(Ttx.VERSION);
                    }

                    public void onEnd(ElementPath path)
                    {
                    }
                }
            );

        // Fetch the header info early.
        reader.addHandler("/TRADOStag/FrontMatter",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                    }

                    public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        setTtxHeader(element);

                        try
                        {
                            startOutputFile(baseName);
                        }
                        catch (Exception ex)
                        {
                            error(ex.toString());
                            System.exit(1);
                        }

                        // prune the current element to reduce memory
                        element.detach();
                        element = null;
                    }
                }
            );

        ElementHandler tuHandler = new ElementHandler ()
            {
                public void onStart(ElementPath path)
                {
                    ++m_entryCount;
                }

                public void onEnd(ElementPath path)
                {
                    Element element = path.getCurrent();

                    element = cleanupTu(element);

                    writeEntry(element.asXML());

                    // prune the current element to reduce memory
                    element.detach();
                    element = null;

                    if (m_entryCount % 50 == 0)
                    {
                        debug("Entry " + m_entryCount);
                    }
                }
            };

        // Path handlers cannot use "//", sooo specify all known paths.
        reader.addHandler("/TRADOStag/Body/Raw/Tu", tuHandler);
        reader.addHandler("/TRADOStag/Body/Raw/df/Tu", tuHandler);
        reader.addHandler("/TRADOStag/Body/Raw/ut/Tu", tuHandler);
        reader.addHandler("/TRADOStag/Body/Raw/df/ut/Tu", tuHandler);

        // Read in the entire file (it's not too big normally).
        Document document = reader.read(p_url);

        closeOutputFile();

        info("Processed " + m_entryCount + " TUs into file `" + m_filename + "'");

        return m_filename;
    }

    static public void main(String[] argv)
        throws Exception
    {
        TtxToTmx a = new TtxToTmx();

        if (argv.length != 1)
        {
            System.err.println("Usage: TtxToTmx FILE\n");
            System.exit(1);
        }

        a.convertTtxToTmx(argv[0]);
    }
}
