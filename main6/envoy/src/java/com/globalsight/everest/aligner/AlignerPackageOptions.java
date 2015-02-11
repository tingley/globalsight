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

package com.globalsight.everest.aligner;

import org.apache.log4j.Logger;

import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;
import java.io.Serializable;

/**
 * This class collects all the options related to creating an Aligner
 * Package.  There are the following sets of options:
 *
 * Extraction Options - file types, rules, locales, encodings
 * Aligner Options    - determining how file should be aligned
 * File Pairs         - the file pairs to be aligned
 */
public class AlignerPackageOptions
    implements Serializable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AlignerPackageOptions.class);

    //
    // Private Classes
    //

    /**
     * Specifies the options needed to extract files: file type,
     * extraction rules, locales, encodings.
     */
    public class ExtractionOptions
    {
        public String m_formatType = "";
        public String m_rules = "";
        public String m_sourceEncoding = "";
        public String m_targetEncoding = "";
        public String m_sourceLocale = "";
        public String m_targetLocale = "";
        public ArrayList m_extensions = new ArrayList();

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<extractionOptions>");
            result.append("<formatType>");
            result.append(EditUtil.encodeXmlEntities(m_formatType));
            result.append("</formatType>");
            result.append("<rules>");
            result.append(EditUtil.encodeXmlEntities(m_rules));
            result.append("</rules>");
            result.append("<sourceEncoding>");
            result.append(EditUtil.encodeXmlEntities(m_sourceEncoding));
            result.append("</sourceEncoding>");
            result.append("<targetEncoding>");
            result.append(EditUtil.encodeXmlEntities(m_targetEncoding));
            result.append("</targetEncoding>");
            result.append("<sourceLocale>");
            result.append(EditUtil.encodeXmlEntities(m_sourceLocale));
            result.append("</sourceLocale>");
            result.append("<targetLocale>");
            result.append(EditUtil.encodeXmlEntities(m_targetLocale));
            result.append("</targetLocale>");

            result.append("<extensions>");
            for (int i = 0, max = m_extensions.size(); i < max; i++)
            {
                String ext = (String)m_extensions.get(i);

                result.append("<extension>");
                result.append(ext);
                result.append("</extension>");
            }
            result.append("</extensions>");

            result.append("</extractionOptions>");

            return result.toString();
        }
    }

    /**
     * Specifies the options needed to align files: currently only one
     * to ignore internal formatting and treat segments as pure text.
     */
    public class AlignerOptions
    {
        public String m_ignoreFormatting = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<alignerOptions>");
            result.append("<ignoreFormatting>");
            result.append(EditUtil.encodeXmlEntities(m_ignoreFormatting));
            result.append("</ignoreFormatting>");
            result.append("</alignerOptions>");

            return result.toString();
        }
    }

    /**
     * Holds a source and target file pair to be aligned. This class
     * can be instantiated by external clients directly.
     */
    static public class FilePair
        implements Comparable
    {
        private String m_source = "";
        private String m_target = "";

        public FilePair(String p_source, String p_target)
        {
            m_source = p_source;
            m_target = p_target;
        }

        public String getSource()
        {
            return m_source;
        }

        public String getTarget()
        {
            return m_target;
        }

        public boolean equals(Object o)
        {
            if (o instanceof FilePair)
            {
                FilePair other = (FilePair)o;

                return this.m_source.equals(other.m_source) &&
                    this.m_target.equals(other.m_target);
            }

            return false;
        }

        public int compareTo(Object o)
        {
            int result = -1;

            if (o instanceof FilePair)
            {
                FilePair other = (FilePair)o;

                result = this.m_source.compareTo(other.m_source);

                if (result == 0)
                {
                    result = this.m_target.compareTo(other.m_target);
                }
            }

            return result;
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<filePair>");
            result.append("<source>");
            result.append(EditUtil.encodeXmlEntities(m_source));
            result.append("</source>");
            result.append("<target>");
            result.append(EditUtil.encodeXmlEntities(m_target));
            result.append("</target>");
            result.append("</filePair>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private String m_packageName = "";
    private ExtractionOptions m_extractionOptions = new ExtractionOptions();
    private AlignerOptions m_alignerOptions = new AlignerOptions();
    private ArrayList m_filePairs = new ArrayList();

    //
    // Constructors
    //
    public AlignerPackageOptions()
    {
        super();
    }

    //
    // Public Methods
    //

    public String getPackageName()
    {
        return m_packageName;
    }

    public void clearFilePairs()
    {
        m_filePairs.clear();
    }

    public void addFilePair(FilePair p_pair)
    {
        m_filePairs.add(p_pair);
    }

    public void addAllFilePairs(Collection p_pairs)
    {
        m_filePairs.addAll(p_pairs);
    }

    public ArrayList getFilePairs()
    {
        return m_filePairs;
    }

    public String getFormatType()
    {
        return m_extractionOptions.m_formatType;
    }

    public String getRules()
    {
        return m_extractionOptions.m_rules;
    }

    public String getSourceEncoding()
    {
        return m_extractionOptions.m_sourceEncoding;
    }

    public String getTargetEncoding()
    {
        return m_extractionOptions.m_targetEncoding;
    }

    public String getSourceLocale()
    {
        return m_extractionOptions.m_sourceLocale;
    }

    public String getTargetLocale()
    {
        return m_extractionOptions.m_targetLocale;
    }

    public void clearExtensions()
    {
        m_extractionOptions.m_extensions.clear();
    }

    public void addExtension(String p_ext)
    {
        m_extractionOptions.m_extensions.add(p_ext);
    }

    public ArrayList getExtensions()
    {
        return m_extractionOptions.m_extensions;
    }

    public String getIgnoreFormatting()
    {
        return m_alignerOptions.m_ignoreFormatting;
    }

    /**
     * Returns an AlignerPackageOptions object XML string.  For easy
     * post processing in Java, make sure to not use any white space
     * or newlines.
     */
    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<alignerPackageOptions>");

        result.append("<packageName>");
        result.append(EditUtil.encodeXmlEntities(m_packageName));
        result.append("</packageName>");

        result.append(m_extractionOptions.asXML());
        result.append(m_alignerOptions.asXML());

        result.append("<filePairs>");
        for (int i = 0, max = m_filePairs.size(); i < max; i++)
        {
            FilePair pair = (FilePair)m_filePairs.get(i);

            result.append(pair.asXML());
        }
        result.append("</filePairs>");

        result.append("</alignerPackageOptions>");

        return result.toString();
    }

    /**
     * Initializes this object from an XML string.
     */
    public void init(String p_options)
        throws Exception
    {
        XmlParser parser = null;
        Document dom = null;
        Element elem;
        List nodes;

        clearFilePairs();
        clearExtensions();

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_options);

            Element root = dom.getRootElement();

            m_packageName = root.elementText("packageName");

            elem = (Element)root.selectSingleNode("//extractionOptions");
            m_extractionOptions.m_formatType = elem.elementText("formatType");
            m_extractionOptions.m_rules = elem.elementText("rules");
            m_extractionOptions.m_sourceEncoding = elem.elementText("sourceEncoding");
            m_extractionOptions.m_targetEncoding = elem.elementText("targetEncoding");
            m_extractionOptions.m_sourceLocale = elem.elementText("sourceLocale");
            m_extractionOptions.m_targetLocale = elem.elementText("targetLocale");

            nodes = root.selectNodes("//extractionOptions//extension");
            for (int i = 0, max = nodes.size(); i < max; i++)
            {
                elem = (Element)nodes.get(i);

                String ext = elem.getText();

                addExtension(ext);
            }

            nodes = root.selectNodes("//filePair");
            for (int i = 0, max = nodes.size(); i < max; i++)
            {
                elem = (Element)nodes.get(i);

                FilePair pair = new FilePair(
                    elem.elementText("source"), elem.elementText("target"));

                addFilePair(pair);
            }

            elem = (Element)root.selectSingleNode("//alignerOptions");
            m_alignerOptions.m_ignoreFormatting = elem.elementText("ignoreFormatting");
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }
}
