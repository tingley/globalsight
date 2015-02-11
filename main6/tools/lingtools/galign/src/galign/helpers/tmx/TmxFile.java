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
package galign.helpers.tmx;

import galign.helpers.tmx.DtdResolver;
import galign.helpers.tmx.TmxConstants;
import galign.helpers.tmx.TmxHeader;

import galign.helpers.util.EditUtil;
import galign.helpers.util.GAlignException;
import galign.helpers.util.TmxEntityResolver;
import galign.helpers.util.XmlParser;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class represents both TMX and GTMX files, Globalsight's native
 * variant of TMX.  GTMX contains more attributes on internal TMX tags
 * (BPT, PH, IT) than the original TMX, and it shuns unknown tags
 * (UT).
 *
 * Instances of this class read an entire (G)TMX file and parse its
 * content into an internal object model.
 *
 * This implementation is tailored for the aligner, meaning it only
 * supports a minimal number of optional attributes and is meant to
 * read existing files, not create new ones.
 *
 * @see http://www.lisa.org/tmx
 */
public class TmxFile
    implements TmxConstants
{
    //
    // Private Members
    //
    private TmxHeader m_header;
    private String m_tmxVersion;
    private ArrayList m_tus = new ArrayList();
    private String m_fileName;

    //
    // Constructors
    //
    public TmxFile ()
    {
    }

    public TmxFile (String p_fileName)
    {
        m_fileName = p_fileName;
    }

    /**
     * @throws org.xml.sax.SAXException
     * @throws org.dom4j.DocumentException
     */
    public void loadFromFile(String p_fileName)
        throws Exception
    {
        clearTus();

        m_fileName = p_fileName;

        // Dom4j 1.1 had problems parsing XML files correctly so we
        // used Xerces. Now with 1.5 and AElfred 2 the problems may
        // have been fixed.

        // Validation of XML files is not supported by AElfred so we
        // must use Xerces. But since some TMX files contain no DTD
        // decl we just don't validate.
        //SAXReader reader = new SAXReader();
        //reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        //reader.setEntityResolver(DtdResolver.getInstance());
        //reader.setValidation(true);

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.dom4j.io.aelfred2.SAXDriver");
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader.setEntityResolver(new TmxEntityResolver());
        reader.setValidation(false);

        InputSource src = new InputSource(new FileInputStream(p_fileName));

        init(reader, src);
    }

    //
    // Public Methods
    //

    public ArrayList getTus()
    {
        return m_tus;
    }

    public void clearTus()
    {
        m_tus.clear();
    }

    public void addTu(Tu p_arg)
    {
        m_tus.add(p_arg);
    }

    public void save()
        throws IOException, GAlignException
    {
        try {
            FileWriter out = new FileWriter(m_fileName);
            out.write(getXml());
            out.close();
        }
        catch (FileNotFoundException e)
        {
            throw new GAlignException("error.cannotWriteGAM", e);
        }
    }


    //
    // Private Methods
    //

    protected String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

        result.append(m_header.getTmxDeclaration());
        result.append("\n<tmx version=\"");
        result.append(m_tmxVersion);
        result.append("\" >\n");
        result.append(m_header.getHeaderXml());
        result.append("<body>\n");

        for (int i = 0, max = m_tus.size(); i < max; ++i)
        {
            result.append(((Tu)m_tus.get(i)).getXml());
        }

        result.append("</body>\n");
        result.append("</tmx>\n");

        return result.toString();
    }

    /**
     * Reads and validates a TMX XML string.
     */
    protected void init(SAXReader p_reader, InputSource p_input)
        throws org.dom4j.DocumentException
    {
        SAXReader reader = p_reader;

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx",
            new ElementHandler ()
                {
                    final public void onStart(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        m_tmxVersion = element.attributeValue("version");
                    }

                    final public void onEnd(ElementPath path)
                    {
                    }
                }
            );

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/header",
            new ElementHandler ()
                {
                    final public void onStart(ElementPath path)
                    {
                    }

                    final public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        m_header = new TmxHeader(element);
                        m_header.setTmxVersion(m_tmxVersion);

                        // prune the current element to reduce memory
                        element.detach();

                        element = null;
                    }
                }
            );

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu",
            new ElementHandler ()
                {
                    final public void onStart(ElementPath path)
                    {
                    }

                    final public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        addTu(new Tu(element));

                        // prune the current element to reduce memory
                        element.detach();
                        element = null;
                    }
                }
            );

        Document document = reader.read(p_input);
        // all done.
    }

    //
    // Test Code
    //

    static public void main(String[] argv)
        throws Exception
    {
        TmxFile tmx = new TmxFile();

        if (argv.length == 0)
        {
            System.err.println("Please specify TMX file.");
            return;
        }

        tmx.loadFromFile(argv[0]);

        System.out.println("Loaded TMX file " + argv[0] + ".");
        System.out.println("Number of TUs: " + tmx.getTus().size());
        System.out.println(tmx.getXml());
    }
}
