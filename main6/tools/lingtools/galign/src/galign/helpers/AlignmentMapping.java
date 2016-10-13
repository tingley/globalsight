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

import galign.helpers.tmx.TmxFile;
import galign.helpers.tmx.Tu;
import galign.helpers.tmx.Tuv;
import galign.helpers.util.EditUtil;
import galign.helpers.util.GAlignException;
import galign.helpers.util.GamEntityResolver;
import galign.helpers.util.XmlParser;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.xml.sax.InputSource;

import java.io.*;
import java.util.*;

/**
 * AlignmentMapping describes which source segments are aligned with
 * which target segments.
 *
 * This class is initialized from a GAM file.
 */
public class AlignmentMapping
{
    //
    // Private Classes
    //

    public class Align
    {
        public String m_source;
        public String m_target;
        public String m_approved;

        public void setApproved(boolean p_approved)
        {
            m_approved = p_approved ? "Y" : "N";
        }

        public void setSource(String p_source)
        {
            m_source = p_source;
        }

        public void setTarget(String p_target)
        {
            m_target = p_target;
        }

        public boolean isApproved()
        {
            return "Y".equals(m_approved);
        }

        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<align");
            result.append(" source=\"");
            result.append(m_source);
            result.append("\" target=\"");
            result.append(m_target);
            result.append("\" approved=\"");
            result.append(m_approved);
            result.append("\" />\n");

            return result.toString();
        }
    }

    public class Remove
    {
        public String m_id;
        public String m_isSource;

        public void setIsSource(boolean p_isSource)
        {
            m_isSource = p_isSource ? "S" : "T";
        }

        public boolean isSource()
        {
            return "S".equals(m_isSource);
        }

        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<remove");
            result.append(" id=\"");
            result.append(m_id);
            result.append("\" locale=\"");
            result.append(m_isSource);
            result.append("\" />\n");

            return result.toString();
        }
    }

    public class Isolate
    {
        public String m_id;
        public String m_isSource;
        public String m_approved;

        public void setIsSource(boolean p_isSource)
        {
            m_isSource = p_isSource ? "S" : "T";
        }

        public boolean isSource()
        {
            return "S".equals(m_isSource);
        }

        public void setApproved(boolean p_approved)
        {
            m_approved = p_approved ? "Y" : "N";
        }

        public boolean isApproved()
        {
            return "Y".equals(m_approved);
        }

        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<isolate");
            result.append(" id=\"");
            result.append(m_id);
            result.append("\" locale=\"");
            result.append(m_isSource);
            result.append("\" approved=\"");
            result.append(m_approved);
            result.append("\" />\n");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private String m_version = "1.0";
    private String m_sourceTmxFileName;
    private String m_targetTmxFileName;
    private TmxFile m_sourceTmx;
    private TmxFile m_targetTmx;

    private ArrayList m_alignRecords = new ArrayList();
    private ArrayList m_removeRecords = new ArrayList();
    private ArrayList m_isolateRecords = new ArrayList();

    private String m_fileName;

    //
    // Constructors
    //

    public AlignmentMapping (String p_dir, String p_fileName)
        throws Exception
    {
        m_fileName = p_dir + File.separator + p_fileName;
        loadFromFile(m_fileName);

        // open the tmx files
        m_sourceTmx = new TmxFile();
        m_sourceTmx.loadFromFile(p_dir + File.separator + m_sourceTmxFileName);
        m_targetTmx = new TmxFile();
        m_targetTmx.loadFromFile(p_dir + File.separator + m_targetTmxFileName);
    }

    //
    // Public Methods
    //

    public TmxFile getSourceTmx()
    {
        return m_sourceTmx;
    }

    public TmxFile getTargetTmx()
    {
        return m_targetTmx;
    }

    public ArrayList getAlignRecords()
    {
        return m_alignRecords;
    }

    public void clearAlignRecords()
    {
        m_alignRecords.clear();
    }

    public Align createAlignRecord()
    {
        return new Align();
    }

    public void addAlignRecord(Align p_alignRecord)
    {
        m_alignRecords.add(p_alignRecord);
    }

    public void approveAlignRecord(String p_sourceId, String p_targetId)
    {
        boolean found = false;
        for (int i = 0; i < m_alignRecords.size(); i++)
        {
            Align align = (Align) m_alignRecords.get(i);
            if (align.m_source.equals(p_sourceId) &&
                align.m_target.equals(p_targetId))
            {
                align.m_approved = "Y";
                found = true;
                break;
            }
        }
        if (!found)
        {
            Align align = new Align();
            align.m_source = p_sourceId;
            align.m_target = p_targetId;
            align.m_approved = "Y";
            addAlignRecord(align);
        }
    }

    public void removeAlignment(String p_sourceId, String p_targetId)
    {
        for (int i = 0; i < m_alignRecords.size(); i++)
        {
            Align align = (Align) m_alignRecords.get(i);
            if (align.m_source.equals(p_sourceId) && align.m_target.equals(p_targetId))
            {
                m_alignRecords.remove(align);
                i--;
            }
        }
    }

    public void createRemoveRecord(String p_id, String p_type)
    {
        Remove remove = new Remove();
        remove.m_id = p_id;
        remove.m_isSource = p_type;
        addRemoveRecord(remove);
    }

    public ArrayList getRemoveRecords()
    {
        return m_removeRecords;
    }

    public void clearRemoveRecords()
    {
        m_removeRecords.clear();
    }

    public void addRemoveRecord(Remove p_removeRecord)
    {
        m_removeRecords.add(p_removeRecord);
    }

    public boolean isInRemoveList(String p_id, String p_isSource)
    {
        for (int i = 0; i < m_removeRecords.size(); i++)
        {
            Remove remove = (Remove)m_removeRecords.get(i);
            if (remove.m_id.equals(p_id) && remove.m_isSource.equals(p_isSource))
            {
                return true;
            }
        }
        return false;
    }


    public ArrayList getIsolateRecords()
    {
        return m_isolateRecords;
    }

    public void clearIsolateRecords()
    {
        m_isolateRecords.clear();
    }

    public Isolate createIsolateRecord()
    {
        return new Isolate();
    }

    public void addIsolateRecord(Isolate p_isolateRecord)
    {
        m_isolateRecords.add(p_isolateRecord);
    }

    /**
     * Replace 2 isolate records with an align record
     */
    public void replaceIsolatesWithAlign(String source_id, String target_id)
    {
        Align align = createAlignRecord();
        align.setApproved(true);
        align.setSource(source_id);
        align.setTarget(target_id);
        addAlignRecord(align);

        for (int i = 0; i < m_isolateRecords.size(); i++)
        {
            Isolate isolate = (Isolate)m_isolateRecords.get(i);
            if (isolate.m_id.equals(source_id)  || isolate.m_id.equals(target_id))
            {
                m_isolateRecords.remove(isolate);
                i--;
            }
        }
    }

    public String getSourceTmxFileName()
    {
        return m_sourceTmxFileName;
    }

    public String getTargetTmxFileName()
    {
        return m_targetTmxFileName;
    }

    /**
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
        reader.setEntityResolver(new GamEntityResolver());
        reader.setValidation(false);

        InputSource src = new InputSource(new FileInputStream(p_fileName));

        init(reader, src);
    }

    /**
     * Save the .gam file and .tmx files
     */
    public void save()
        throws IOException, GAlignException
    {
        // gam file
        FileWriter out = new FileWriter(m_fileName);
        out.write(getXml());
        out.close();

        /* don't need to save since they can't be edited at this point
        m_sourceTmx.save();
        m_targetTmx.save();
        */
    }

    public void exportAsTmx(String p_fileName)
        throws IOException
    {
        TmxFile result = new TmxFile(p_fileName);

        for (int i = 0, max = m_alignRecords.size(); i < max; i++)
        {
            Align align = (Align)m_alignRecords.get(i);

            Tuv srcTuv = getSourceTuv(align.m_source);
            Tuv trgTuv = getTargetTuv(align.m_target);

            Tu newTu = new Tu(String.valueOf(i + 1));
            newTu.addTuv(srcTuv);
            newTu.addTuv(trgTuv);

            result.addTu(newTu);
        }

        try
        {
            result.save();
        }
        catch (GAlignException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    private Tuv getSourceTuv(String p_id)
    {
        ArrayList tus = m_sourceTmx.getTus();

        for (int i = 0, max = tus.size(); i < max; i++)
        {
            Tu tu = (Tu)tus.get(i);

            if (tu.getId().equals(p_id))
            {
                // handle only 1 TUV per TU
                return (Tuv)(tu.getTuvs().get(0));
            }
        }

        return null;
    }

    private Tuv getTargetTuv(String p_id)
    {
        ArrayList tus = m_targetTmx.getTus();

        for (int i = 0, max = tus.size(); i < max; i++)
        {
            Tu tu = (Tu)tus.get(i);

            if (tu.getId().equals(p_id))
            {
                // handle only 1 TUV per TU
                return (Tuv)(tu.getTuvs().get(0));
            }
        }

        return null;
    }

    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<!DOCTYPE gam SYSTEM \"gam.dtd\">\n");
        result.append("<gam version=\"");
        result.append(m_version);
        result.append("\" source-tmx=\"");
        result.append(m_sourceTmxFileName);
        result.append("\" target-tmx=\"");
        result.append(m_targetTmxFileName);
        result.append("\">\n");

        for (int i = 0, max = m_alignRecords.size(); i < max; ++i)
        {
            result.append("  ");
            result.append(((Align)m_alignRecords.get(i)).getXml());
        }

        for (int i = 0, max = m_removeRecords.size(); i < max; ++i)
        {
            result.append("  ");
            result.append(((Remove)m_removeRecords.get(i)).getXml());
        }

/*  no longer need to save
        for (int i = 0, max = m_isolateRecords.size(); i < max; ++i)
        {
            result.append("  ");
            result.append(((Isolate)m_isolateRecords.get(i)).getXml());
        }
*/

        result.append("</gam>\n");

        return result.toString();
    }

    //
    // Private Methods
    //

    /**
     * Reads and validates a GAM XML file.
     */
    protected void init(SAXReader p_reader, InputSource p_input)
        throws org.dom4j.DocumentException
    {
        Document dom = p_reader.read(p_input);
        Element root = dom.getRootElement();

        m_version = root.attributeValue("version");
        m_sourceTmxFileName = root.attributeValue("source-tmx");
        m_targetTmxFileName = root.attributeValue("target-tmx");

        clearAlignRecords();

        for (Iterator i = root.elementIterator("align"); i.hasNext();)
        {
            // Example data: <align source="7,8" target="12,14" approved="N" />

            Element item = (Element)i.next();
            StringTokenizer tok1 = new StringTokenizer(item.attributeValue("source"),",");
            while (tok1.hasMoreTokens())
            {
                Align align = new Align();

                align.m_source = tok1.nextToken();
                StringTokenizer tok2 = new StringTokenizer(item.attributeValue("target"), " ,");
                boolean first = true;
                while (tok2.hasMoreTokens())
                {
                    if (first)
                    {
                        align.m_target = tok2.nextToken();
                        align.m_approved = item.attributeValue("approved");
                        addAlignRecord(align);
                        first = false;
                    }
                    else
                    {
                        Align align2 = new Align();
                        align2.m_source = align.m_source;
                        align2.m_target = tok2.nextToken();
                        align2.m_approved = item.attributeValue("approved");
                        addAlignRecord(align2);
                    }
                }
            }

        }

        clearRemoveRecords();

        for (Iterator i = root.elementIterator("remove"); i.hasNext();)
        {
            Element item = (Element)i.next();
            Remove remove = new Remove();

            remove.m_id = item.attributeValue("id");
            remove.m_isSource = item.attributeValue("locale");

            addRemoveRecord(remove);
        }

        clearIsolateRecords();

        for (Iterator i = root.elementIterator("isolate"); i.hasNext();)
        {
            Element item = (Element)i.next();
            Isolate isolate = new Isolate();

            isolate.m_id = item.attributeValue("id");
            isolate.m_isSource = item.attributeValue("locale");
            isolate.m_approved = item.attributeValue("approved");

            addIsolateRecord(isolate);
        }
    }

    //
    // Test Code
    //

    static public void main(String[] argv)
        throws Exception
    {

        if (argv.length == 0)
        {
            System.err.println("Please specify GAM XML file.");
            return;
        }

        File file = new File(argv[0]);
        AlignmentMapping gam = new AlignmentMapping(file.getParent(), file.getName());

        System.out.println("Loaded GAM.");
        System.out.println(gam.getXml());
    }
}
