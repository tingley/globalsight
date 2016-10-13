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

package com.globalsight.everest.projecthandler.exporter;

import org.apache.log4j.Logger;

import com.globalsight.exporter.ExporterException;

import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * This class collects all the options related to exporting project
 * data to files. There are the following sets of options:
 *
 * SelectOptions - selects entries from the database (all, by name);
 * OutputOptions - determines how data is output to the file.
 */
public class ExportOptions
    extends com.globalsight.exporter.ExportOptions
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExportOptions.class);

    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_XML = "xml";  // native format
    public static final String TYPE_CSV = "csv";  // comma-separated

    public static final String SELECT_ALL      = "all";
    public static final String SELECT_FILTERED = "filter";

    //
    // Private Classes
    //

    /**
     * SelectOptions specify which entries to select from the termbase
     * for export, and how duplicate entries are handled (reached
     * through synonymous terms).
     */
    public class SelectOptions
    {
        /** One of "all", "filter" */
        public String m_selectMode = "";

        /** If selectMode=filter, the filter spec */
        public String m_selectFilter = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<selectOptions>");
            result.append("<selectMode>");
            result.append(EditUtil.encodeXmlEntities(m_selectMode));
            result.append("</selectMode>");
            result.append("<selectFilter>");
            result.append(EditUtil.encodeXmlEntities(m_selectFilter));
            result.append("</selectFilter>");
            result.append("</selectOptions>");

            return result.toString();
        }
    }

    /**
     * Specifies how entries are output to the file, e.g. field
     * mappings, field suppression, field addition.
     */
    public class OutputOptions
    {
        /**
         * Write a header line. Allowed values: "true", "false".
         */
        public String m_header = "";
        /**
         * CSV separator char.
         */
        public String m_separator = ",";

        /**
         * Some types may use subtypes to specify representation
         * formats, e.g. dates in M/d/YY format or dd/mm/yyyy or
         * whatever.
         */
        public String m_subtype;

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<outputOptions>");
            result.append("<header>");
            result.append(EditUtil.encodeXmlEntities(m_header));
            result.append("</header>");
            result.append("<separator>");
            result.append(EditUtil.encodeXmlEntities(m_separator));
            result.append("</separator>");
            result.append("<subtype>");
            result.append(EditUtil.encodeXmlEntities(m_subtype));
            result.append("</subtype>");
            result.append("</outputOptions>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private SelectOptions m_selectOptions = new SelectOptions();
    private OutputOptions m_outputOptions = new OutputOptions();

    //
    // Public Methods
    //
    public SelectOptions getSelectOptions()
    {
        return m_selectOptions;
    }

    public OutputOptions getOutputOptions()
    {
        return m_outputOptions;
    }

    public String getSelectMode()
    {
        return m_selectOptions.m_selectMode;
    }

    public void setSelectMode(String p_mode)
    {
        m_selectOptions.m_selectMode = p_mode;
    }

    public String getSelectFilter()
    {
        return m_selectOptions.m_selectFilter;
    }

    public void setSelectFilter(String p_filter)
    {
        m_selectOptions.m_selectFilter = p_filter;
    }

    public String getHeader()
    {
        return m_outputOptions.m_header;
    }

    public void setHeader(String p_header)
    {
        m_outputOptions.m_header = p_header;
    }

    public String getSeparator()
    {
        return m_outputOptions.m_separator;
    }

    public String getSeparatorChar()
    {
        if (m_outputOptions.m_separator.equals("tab"))
        {
            return "\t";
        }
        else if (m_outputOptions.m_separator.equals("comma"))
        {
            return ",";
        }
        else if (m_outputOptions.m_separator.equals("space"))
        {
            return " ";
        }

        return m_outputOptions.m_separator;
    }

    public void setSeparator(String p_separator)
    {
        m_outputOptions.m_separator = p_separator;
    }

    public String getSubType()
    {
        return m_outputOptions.m_subtype;
    }

    public void setSubType(String p_subtype)
    {
        m_outputOptions.m_subtype = p_subtype;
    }

    //
    // Overwritten Abstract Methods
    //

    /**
     * Returns an ExportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    protected String getOtherXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append(m_selectOptions.asXML());
        result.append(m_outputOptions.asXML());

        return result.toString();
    }

    /**
     * Reads and validates a ExportOptions XML string.
     */
    protected void initOther(Element p_root)
        throws ExporterException
    {
        try
        {
            Element elem = (Element)p_root.selectSingleNode("//selectOptions");
            m_selectOptions.m_selectMode = elem.elementText("selectMode");
            m_selectOptions.m_selectFilter = elem.elementText("selectFilter");

            elem = (Element)p_root.selectSingleNode("//outputOptions");
            m_outputOptions.m_header = elem.elementText("header");
            m_outputOptions.m_separator = elem.elementText("separator");
            m_outputOptions.m_subtype = elem.elementText("subtype");
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }
}
