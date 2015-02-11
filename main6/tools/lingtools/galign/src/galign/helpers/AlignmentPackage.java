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

package galign.helpers;

import galign.helpers.util.EditUtil;
import galign.helpers.util.GAlignException;
import galign.helpers.util.GapEntityResolver;
import galign.helpers.util.XmlParser;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.xml.sax.InputSource;

import java.io.*;
import java.util.*;

/**
 * AlignmentPackage describes which pre-aligned files are in an
 * alignment package, source &amp; target locales, and which GAM file
 * to use for each file pair.
 *
 * This class is initialized from a GAP file.
 */
public class AlignmentPackage
{
    //
    // Private Classes
    //

    /**
     * Describes an aligned file pair: original files, converted TMX
     * files, and the GAM file prepared by the server describing the
     * automatically computed segment alignments.
     */
    public class File
    {
        public String m_originalSourceFileName = "";
        public String m_originalTargetFileName = "";
        public String m_sourceCuvId = "";
        public String m_targetCuvId = "";
        public String m_sourceTmxFileName = "";
        public String m_targetTmxFileName = "";
        public String m_mappingFileName = "";
        public String m_state = "";

        public String getOriginalSourceFileName()
        {
            return m_originalSourceFileName;
        }

        public String getOriginalTargetFileName()
        {
            return m_originalTargetFileName;
        }

        public String getSourceTmxFileName()
        {
            return m_sourceTmxFileName;
        }

        public String getTargetTmxFileName()
        {
            return m_targetTmxFileName;
        }

        public String getMappingFileName()
        {
            return m_mappingFileName;
        }

        public String getState()
        {
            return m_state;
        }

        public void setCompletedState()
        {

            m_state = "COMPLETED";
        }

        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<files\n");
            result.append("\toriginal-source-file=\"");
            result.append(
                EditUtil.encodeXmlEntities(m_originalSourceFileName));
            result.append("\"\n\toriginal-target-file=\"");
            result.append(
                EditUtil.encodeXmlEntities(m_originalTargetFileName));
            result.append("\"\n\tsource-cuv-id=\"");
            result.append(m_sourceCuvId);
            result.append("\"\n\ttarget-cuv-id=\"");
            result.append(m_targetCuvId);
            result.append("\"\n\tsource-tmx=\"");
            result.append(EditUtil.encodeXmlEntities(m_sourceTmxFileName));
            result.append("\"\n\ttarget-tmx=\"");
            result.append(EditUtil.encodeXmlEntities(m_targetTmxFileName));
            result.append("\"\n\tgam=\"");
            result.append(
                EditUtil.encodeXmlEntities(m_mappingFileName));
            result.append("\"\n\tstate=\"");
            result.append(m_state);
            result.append("\" />\n");

            return result.toString();
        }
    }

    //
    // Private Members
    //

    private String m_version = "1.0";
    private String m_sourceLocale;
    private String m_targetLocale;
    private String m_fileName;
    private String m_dir;

    private ArrayList m_files = new ArrayList();

    //
    // Constructors
    //
    public AlignmentPackage ()
    {
    }

    public AlignmentPackage (String p_fileName)
        throws Exception
    {
        m_fileName = p_fileName;

        loadFromFile(p_fileName);
    }

    //
    // Public Methods
    //

    public String getPackageFileName()
    {
        return m_fileName;
    }

    public String getDirectory()
    {
        return m_dir;
    }

    public String getSourceLocale()
    {
        return m_sourceLocale;
    }

    public String getTargetLocale()
    {
        return m_targetLocale;
    }

    public ArrayList getFiles()
    {
        return m_files;
    }

    public void clearFiles()
    {
        m_files.clear();
    }

    public File createFile()
    {
        return new File();
    }

    public void addFile(File p_file)
    {
        m_files.add(p_file);
    }

    public void removeAlignment(File p_file)
    {
        for (int i = 0; i < m_files.size(); i ++)
        {
            File file = (File) m_files.get(i);
            if (file.equals(p_file))
            {
                m_files.remove(i);
                break;
            }
        }
    }

    public void save()
        throws IOException, GAlignException
    {
        try
        {
            PrintWriter out = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(m_fileName), "UTF-8")));

            out.write(getXml());
            out.close();
        }
        catch (FileNotFoundException e)
        {
            throw new GAlignException("error.removeAlignment", e);
        }
    }

    /**
     * Initializes an instance from a GAP file.
     *
     * @throws org.xml.sax.SAXException
     * @throws org.dom4j.DocumentException
     */
    public void loadFromFile(String p_fileName)
        throws Exception
    {
        // Dom4j 1.1 had problems parsing XML files correctly so we
        // used Xerces. Now with 1.5 and AElfred 2 the problems may
        // have been fixed.

        // Reading from a file, need to use Xerces
        // SAXReader reader = new SAXReader();
        //reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        //reader.setEntityResolver(DtdResolver.getInstance());
        //reader.setValidation(true);

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.dom4j.io.aelfred2.SAXDriver");
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader.setEntityResolver(new GapEntityResolver());
        reader.setValidation(false);

        Reader ioReader = new InputStreamReader(
            new FileInputStream(p_fileName), "UTF-8");
        InputSource src = new InputSource(ioReader);

        init(reader, src, new java.io.File(p_fileName).getParent());
    }

    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<!DOCTYPE gap SYSTEM \"gap.dtd\">\n");
        result.append("<gap version=\"");
        result.append(m_version);
        result.append("\" source-locale=\"");
        result.append(m_sourceLocale);
        result.append("\" target-locale=\"");
        result.append(m_targetLocale);
        result.append("\">\n");

        for (int i = 0, max = m_files.size(); i < max; ++i)
        {
            result.append(((File)m_files.get(i)).getXml());
        }

        result.append("</gap>\n");

        return result.toString();
    }

    //
    // Private Methods
    //

    /**
     * Reads and validates a GAP XML file.
     */
    protected void init(SAXReader p_reader, InputSource p_input, String p_dir)
        throws org.dom4j.DocumentException
    {
        Document dom = p_reader.read(p_input);
        Element root = dom.getRootElement();

        m_version = root.attributeValue("version");
        m_sourceLocale = root.attributeValue("source-locale");
        m_targetLocale = root.attributeValue("target-locale");
        m_dir = p_dir;

        m_files.clear();

        for (Iterator i = root.elementIterator("files"); i.hasNext();) 
        {
            Element item = (Element) i.next();
            File file = new File();

            file.m_originalSourceFileName =
                item.attributeValue("original-source-file");
            file.m_originalTargetFileName =
                item.attributeValue("original-target-file");
            file.m_sourceCuvId = item.attributeValue("source-cuv-id");
            file.m_targetCuvId = item.attributeValue("target-cuv-id");
            file.m_sourceTmxFileName = item.attributeValue("source-tmx");
            file.m_targetTmxFileName = item.attributeValue("target-tmx");
            file.m_mappingFileName = item.attributeValue("gam");
            file.m_state = item.attributeValue("state");

            addFile(file);
        }
    }

    //
    // Test Code
    //

    static public void main(String[] argv)
        throws Exception
    {
        AlignmentPackage gap = new AlignmentPackage();

        if (argv.length == 0)
        {
            System.err.println("Please specify GAP XML file.");
            return;
        }

        gap.loadFromFile(argv[0]);

        System.out.println("Loaded GAP.");
        System.out.println(gap.getXml());
    }
}
