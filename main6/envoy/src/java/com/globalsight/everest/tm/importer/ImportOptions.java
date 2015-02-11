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

package com.globalsight.everest.tm.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.globalsight.importer.ImporterException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * This class collects all the options related to importing TM files. There are
 * the following sets of options:
 * 
 * LocaleOptions - which source and target locales to import; Synchronization
 * Options - determining how entries are added to the database when duplicates
 * exists.
 * 
 */
public class ImportOptions extends com.globalsight.importer.ImportOptions
{
    private static final Logger CATEGORY = Logger
            .getLogger(ImportOptions.class);

    // File types.
    public static final String TYPE_UNKNOWN = "unknown";
    // native format
    public static final String TYPE_XML = "xml";
    // TMX (level 1)
    public static final String TYPE_TMX1 = "tmx1";
    // TMX (level 2)
    public static final String TYPE_TMX2 = "tmx2";
    // Trados TMX (RTF)
    public static final String TYPE_TTMX_RTF = "ttmx-rtf";
    // Trados TMX (HTML)
    public static final String TYPE_TTMX_HTML = "ttmx-html";
    // Trados TMX (STagger for FrameMaker)
    public static final String TYPE_TTMX_FM = "ttmx-fm";
    // Trados TMX (STagger for FrameMaker+SGML)
    public static final String TYPE_TTMX_FM_SGML = "ttmx-fm-sgml";
    // Trados TMX (STagger for Interleaf)
    public static final String TYPE_TTMX_IL = "ttmx-il";
    // Trados TMX (Story Collector for QuarkXPress)
    public static final String TYPE_TTMX_XPTAG = "ttmx-xptag";
    // WorldServer TMX
    public static final String TYPE_TMX_WORLD_SERVER = "tmx-worldserver";

    /**
     * How to deal with duplicate TUs: merge file data into TM data, overwrite
     * TM data from file content (think of added/removed TUVs), or discard file
     * TU.
     */
    public static final String SYNC_OVERWRITE = "overwrite";
    public static final String SYNC_MERGE = "merge";
    public static final String SYNC_DISCARD = "discard";

    //
    // Private Classes
    //

    /**
     * Holds the source and target locales found in the TMX file, and the
     * locales selected by the user that should be imported. If the selected
     * source is "all", all will be imported.
     */
    public class LocaleOptions
    {
        public ArrayList m_sourceLocales = new ArrayList();
        public ArrayList m_targetLocales = new ArrayList();
        public String m_selectedSource = "";
        public ArrayList m_selectedTargets = new ArrayList();

        public void clear()
        {
            m_sourceLocales.clear();
            m_targetLocales.clear();
            m_selectedSource = "";
            m_selectedTargets.clear();
        }

        public void setSourceLocales(Collection p_locales)
        {
            m_sourceLocales.clear();
            m_sourceLocales.addAll(p_locales);
            SortUtil.sort(m_sourceLocales);
        }

        public void setTargetLocales(Collection p_locales)
        {
            m_targetLocales.clear();
            m_targetLocales.addAll(p_locales);
            SortUtil.sort(m_targetLocales);
        }

        public void setSelectedSource(String p_selectedSource)
        {
            m_selectedSource = p_selectedSource;
        }

        public void setSelectedTargets(Collection p_selectedTargets)
        {
            m_selectedTargets.clear();
            m_selectedTargets.addAll(p_selectedTargets);
            SortUtil.sort(m_selectedTargets);
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<localeOptions>");
            result.append("<sourceLocales>");
            for (int i = 0, max = m_sourceLocales.size(); i < max; i++)
            {
                String locale = (String) m_sourceLocales.get(i);
                result.append("<locale>");
                result.append(locale);
                result.append("</locale>");
            }
            result.append("</sourceLocales>");
            result.append("<targetLocales>");
            for (int i = 0, max = m_targetLocales.size(); i < max; i++)
            {
                String locale = (String) m_targetLocales.get(i);
                result.append("<locale>");
                result.append(locale);
                result.append("</locale>");
            }
            result.append("</targetLocales>");
            result.append("<selectedSource>");
            result.append(m_selectedSource);
            result.append("</selectedSource>");
            result.append("<selectedTargets>");
            for (int i = 0, max = m_selectedTargets.size(); i < max; i++)
            {
                String locale = (String) m_selectedTargets.get(i);
                result.append("<locale>");
                result.append(locale);
                result.append("</locale>");
            }
            result.append("</selectedTargets>");
            result.append("</localeOptions>");

            return result.toString();
        }
    }

    /**
     * Specifies the synchronization behavior during import, i.e. how duplicates
     * are detected, and how they are dealt with.
     */
    public class SyncOptions
    {
        /** One of "overwrite", "merge", "discard" */
        public String m_syncMode = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<syncOptions>");
            result.append("<syncMode>");
            result.append(EditUtil.encodeXmlEntities(m_syncMode));
            result.append("</syncMode>");
            result.append("</syncOptions>");

            return result.toString();
        }
    }

    public class SourceTmOptions
    {
        public String m_sourceTmName = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<sourceTmOptions>");
            result.append("<sourceTmName>");
            result.append(EditUtil.encodeXmlEntities(m_sourceTmName));
            result.append("</sourceTmName>");
            result.append("</sourceTmOptions>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private LocaleOptions m_localeOptions = new LocaleOptions();
    private SyncOptions m_syncOptions = new SyncOptions();
    private SourceTmOptions m_sourceTmOptions = new SourceTmOptions();

    //
    // Constructors
    //
    public ImportOptions()
    {
        super();
    }

    //
    // Public Methods
    //

    public void setSourceLocales(Collection p_locales)
    {
        m_localeOptions.setSourceLocales(p_locales);
    }

    public void setTargetLocales(Collection p_locales)
    {
        m_localeOptions.setTargetLocales(p_locales);
    }

    public String getSelectedSourceLocale()
    {
        return m_localeOptions.m_selectedSource;
    }

    public void setSelectedSource(String p_selectedSource)
    {
        m_localeOptions.setSelectedSource(p_selectedSource);
    }

    public void setSelectedTargets(Collection p_selectedTargets)
    {
        m_localeOptions.setSelectedTargets(p_selectedTargets);
    }

    public ArrayList getSelectedTargetLocales()
    {
        return m_localeOptions.m_selectedTargets;
    }

    public void setSyncMode(String p_mode)
    {
        m_syncOptions.m_syncMode = p_mode;
    }

    public String getSyncMode()
    {
        return m_syncOptions.m_syncMode;
    }

    public void setSourceTmName(String p_sourceTmName)
    {
        m_sourceTmOptions.m_sourceTmName = p_sourceTmName;
    }

    public String getSourceTmName()
    {
        return m_sourceTmOptions.m_sourceTmName;
    }

    //
    // Overwritten Abstract Methods
    //

    /**
     * Returns an ImportOptions object XML string. For easy post processing in
     * Java make sure to not use any white space or newlines.
     */
    protected String getOtherXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append(m_localeOptions.asXML());
        result.append(m_syncOptions.asXML());
        result.append(m_sourceTmOptions.asXML());

        return result.toString();
    }

    /**
     * Reads and validates a ImportOptions XML string.
     */
    protected void initOther(Element p_root) throws ImporterException
    {
        try
        {
            List list;
            Element locale;

            Element elem = (Element) p_root.selectSingleNode("//syncOptions");
            m_syncOptions.m_syncMode = elem.elementText("syncMode");

            elem = (Element) p_root.selectSingleNode("//sourceTmOptions");
            m_sourceTmOptions.m_sourceTmName = elem.elementText("sourceTmName");

            elem = (Element) p_root.selectSingleNode("//localeOptions");

            m_localeOptions.clear();

            list = elem.selectNodes("./sourceLocales/locale");
            for (int i = 0, max = list.size(); i < max; i++)
            {
                locale = (Element) list.get(i);
                m_localeOptions.m_sourceLocales.add(locale.getText());
            }

            list = elem.selectNodes("./targetLocales/locale");
            for (int i = 0, max = list.size(); i < max; i++)
            {
                locale = (Element) list.get(i);
                m_localeOptions.m_targetLocales.add(locale.getText());
            }

            m_localeOptions.m_selectedSource = elem
                    .elementText("selectedSource");

            list = elem.selectNodes("./selectedTargets/locale");
            for (int i = 0, max = list.size(); i < max; i++)
            {
                locale = (Element) list.get(i);
                m_localeOptions.m_selectedTargets.add(locale.getText());
            }
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }
}