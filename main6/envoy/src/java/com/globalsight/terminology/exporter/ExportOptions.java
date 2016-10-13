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

package com.globalsight.terminology.exporter;

import org.apache.log4j.Logger;

import com.globalsight.exporter.ExporterException;

import com.globalsight.util.edit.EditUtil;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * This class collects all the options related to exporting
 * terminology files.  There are the following sets of options:
 *
 * SelectOptions - selects entries from the termbase (all, by language);
 * FileOptions   - (filename,) type, encoding;
 * OutputOptions - determines how entries are output to the file.
 * ColumnOptions - describes target columns for CSV output files;
 */
public class ExportOptions
    extends com.globalsight.exporter.ExportOptions
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            ExportOptions.class);

    static public final String TYPE_UNKNOWN = "unknown";
    static public final String TYPE_XML = "xml";  // native format
    static public final String TYPE_MTF = "mtf";  // MultiTerm 5+
    static public final String TYPE_MTW = "mtw";  // Not implemented
    static public final String TYPE_TBX = "tbx";  // Not implemented
    static public final String TYPE_CSV = "csv";  // Not implemented
    static public final String TYPE_RTF = "rtf";  // Not implemented
    static public final String TYPE_HTM = "htm";  // HTML

    static public final String SELECT_ALL      = "all";
    static public final String SELECT_LANGUAGE = "language";

    static public final String DUPLICATE_DISCARD = "discard";
    static public final String DUPLICATE_OUTPUT  = "output";

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
        /** One of "all", "language" */
        public String m_selectMode = "";

        /** If selectMode=language, the language to filter */
        public String m_selectLanguage = "";

        /**
         * If selectMode=language, duplicate resolution: discard,
         * output.
         */
        public String m_duplicateHandling = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<selectOptions>");
            result.append("<selectMode>");
            result.append(EditUtil.encodeXmlEntities(m_selectMode));
            result.append("</selectMode>");
            result.append("<selectLanguage>");
            result.append(EditUtil.encodeXmlEntities(m_selectLanguage));
            result.append("</selectLanguage>");
            result.append("<duplicateHandling>");
            result.append(EditUtil.encodeXmlEntities(m_duplicateHandling));
            result.append("</duplicateHandling>");
            result.append("</selectOptions>");

            return result.toString();
        }
    }

    /**
     * Extended filter conditions that are not applied in the
     * database but evaluated in memory.
     *
     * Example: "English-Definition contains 'text'".
     *
     * m_operator can be one of the OP_XXX constants.
     */
    public class FilterCondition
    {
        static public final String OP_CONTAINS = "contains";
        static public final String OP_CONTAINSNOT = "containsnot";
        static public final String OP_EQUALS = "equals";
        static public final String OP_EQUALSNOT = "equalsnot";
        static public final String OP_LESSTHAN = "lessthan";
        static public final String OP_GREATERTHAN = "greaterthan";
        static public final String OP_EXISTS = "exists";
        static public final String OP_EXISTSNOT = "existsnot";

        public String m_level;
        public String m_field;
        public String m_operator;
        public String m_value;
        public String m_matchcase;

        public String getLevel()
        {
            return m_level;
        }

        public String getField()
        {
            return m_field;
        }

        public String getOperator()
        {
            return m_operator;
        }

        public String getValue()
        {
            return m_value;
        }

        public boolean isMatchCase()
        {
            return m_matchcase.equalsIgnoreCase("true");
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<condition>");
            result.append("<level>");
            result.append(EditUtil.encodeXmlEntities(m_level));
            result.append("</level>");
            result.append("<field>");
            result.append(EditUtil.encodeXmlEntities(m_field));
            result.append("</field>");
            result.append("<operator>");
            result.append(EditUtil.encodeXmlEntities(m_operator));
            result.append("</operator>");
            result.append("<value>");
            result.append(EditUtil.encodeXmlEntities(m_value));
            result.append("</value>");
            result.append("<matchcase>");
            result.append(EditUtil.encodeXmlEntities(m_matchcase));
            result.append("</matchcase>");
            result.append("</condition>");

            return result.toString();
        }
    }


    /**
     * FilterOptions specify how to filter the raw entry list selected
     * in SelectOptions.
     */
    public class FilterOptions
    {
        /** Creation/modification user */
        public String m_createdBy = "";
        public String m_modifiedBy = "";

        /**
         * Creation/modification date ranges. The format is currently
         * determined by the UI to be DD/MM/YYYY. The backend will
         * decode the dates.
         */
        public String m_createdAfter = "";
        public String m_createdBefore = "";
        public String m_modifiedAfter = "";
        public String m_modifiedBefore = "";

        /** Entry status: proposed, reviewed, approved. */
        public String m_status = "";
        public String m_domain = "";
        public String m_project = "";

        /**
         * List of FilterCondition objects that describe filters that
         * are not applicable in the database but need to be evaluated
         * in memory.
         *
         * Example: "English-Definition contains 'text'".
         */
        public ArrayList m_conditions = new ArrayList();

        // TODO: field-specific filters based on termbase definition

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<filterOptions>");
            result.append("<createdby>");
            result.append(EditUtil.encodeXmlEntities(m_createdBy));
            result.append("</createdby>");
            result.append("<modifiedby>");
            result.append(EditUtil.encodeXmlEntities(m_modifiedBy));
            result.append("</modifiedby>");
            result.append("<createdafter>");
            result.append(EditUtil.encodeXmlEntities(m_createdAfter));
            result.append("</createdafter>");
            result.append("<createdbefore>");
            result.append(EditUtil.encodeXmlEntities(m_createdBefore));
            result.append("</createdbefore>");
            result.append("<modifiedafter>");
            result.append(EditUtil.encodeXmlEntities(m_modifiedAfter));
            result.append("</modifiedafter>");
            result.append("<modifiedbefore>");
            result.append(EditUtil.encodeXmlEntities(m_modifiedBefore));
            result.append("</modifiedbefore>");
            result.append("<status>");
            result.append(EditUtil.encodeXmlEntities(m_status));
            result.append("</status>");
            result.append("<domain>");
            result.append(EditUtil.encodeXmlEntities(m_domain));
            result.append("</domain>");
            result.append("<project>");
            result.append(EditUtil.encodeXmlEntities(m_project));
            result.append("</project>");

            result.append("<conditions>");

            for (int i = 0, max = m_conditions.size(); i < max; i++)
            {
                result.append(((FilterCondition)m_conditions.get(i)).asXML());
            }

            result.append("</conditions>");

            result.append("</filterOptions>");

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
         * For native export, export system fields or not. Allowed
         * values: "true", "false".
         */
        public String m_systemFields = "";

        /**
         * If FileOptions.type == CSV, this holds the column separator
         * character.
         */
        public String m_separator = "";

        /**
         * If FileOptions.type == CSV, says to write a header line.
         * Value must be "true" or "false".
         */
        public String m_writeHeader = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<outputOptions>");
            result.append("<systemFields>");
            result.append(EditUtil.encodeXmlEntities(m_systemFields));
            result.append("</systemFields>");
            result.append("<separator>");
            result.append(EditUtil.encodeXmlEntities(m_separator));
            result.append("</separator>");
            result.append("<writeHeader>");
            result.append(EditUtil.encodeXmlEntities(m_writeHeader));
            result.append("</writeHeader>");
            result.append("</outputOptions>");

            return result.toString();
        }
    }

    /**
     * Describes columns in a CSV file so that entries can be mapped
     * properly to the output file.
     */
    public class ColumnDescriptor
    {
        /** The index of this column. */
        public int m_position;
        /** Column name for the file header. */
        public String m_name;
        /** User-assigned type (data category) of this column. */
        public String m_type;
        /** If type=term, the term's language name. */
        public String m_termLanguage;
        /** Column encoding if different from file's primary encoding. */
        public String m_encoding;

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<column id=\"");
            result.append(m_position);
            result.append("\">");
            result.append("<name>");
            result.append(EditUtil.encodeXmlEntities(m_name));
            result.append("</name>");
            result.append("<type>");
            result.append(EditUtil.encodeXmlEntities(m_type));
            result.append("</type>");
            result.append("<termLanguage>");
            result.append(EditUtil.encodeXmlEntities(m_termLanguage));
            result.append("</termLanguage>");
            result.append("<encoding>");
            result.append(EditUtil.encodeXmlEntities(m_encoding));
            result.append("</encoding>");
            result.append("</column>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private SelectOptions m_selectOptions = new SelectOptions();
    private FilterOptions m_filterOptions = new FilterOptions();
    private OutputOptions m_outputOptions = new OutputOptions();
    /** List of ColumnDescriptor objects. */
    private ArrayList m_columns = new ArrayList();

    //
    // Public Methods
    //
    public SelectOptions getSelectOptions()
    {
        return m_selectOptions;
    }

    public FilterOptions getFilterOptions()
    {
        return m_filterOptions;
    }

    public OutputOptions getOutputOptions()
    {
        return m_outputOptions;
    }

    public void setSeparator(String p_sep)
    {
        m_outputOptions.m_separator = p_sep;
    }

    public String getSeparator()
    {
        return m_outputOptions.m_separator;
    }

    public boolean hasHeader()
    {
        return m_outputOptions.m_writeHeader.equalsIgnoreCase("true");
    }

    public String getSelectMode()
    {
        return m_selectOptions.m_selectMode;
    }

    public void setSelectMode(String p_mode)
    {
        m_selectOptions.m_selectMode = p_mode;
    }

    public String getSelectLanguage()
    {
        return m_selectOptions.m_selectLanguage;
    }

    public void setSelectLanguage(String p_language)
    {
        m_selectOptions.m_selectLanguage = p_language;
    }

    public String getSystemFields()
    {
        return m_outputOptions.m_systemFields;
    }

    public void setSystemFields(String p_include)
    {
        m_outputOptions.m_systemFields = p_include;
    }

    public ArrayList getColumns()
    {
        return m_columns;
    }

    public void clearColumns()
    {
        m_columns.clear();
    }

    public ColumnDescriptor createColumnDescriptor()
    {
        return new ColumnDescriptor();
    }

    public void addColumn(ColumnDescriptor p_desc)
    {
        m_columns.add(p_desc);
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
        result.append(m_filterOptions.asXML());
        result.append(m_outputOptions.asXML());

        result.append("<columnOptions>");

        for (int i = 0; i < m_columns.size(); ++i)
        {
            result.append(((ColumnDescriptor)m_columns.get(i)).asXML());
        }

        result.append("</columnOptions>");

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
            Node node = p_root.selectSingleNode("/exportOptions/selectOptions");
            m_selectOptions.m_selectMode = node.valueOf("selectMode");
            m_selectOptions.m_selectLanguage = node.valueOf("selectLanguage");
            m_selectOptions.m_duplicateHandling = node.valueOf("duplicateHandling");

            node = p_root.selectSingleNode("/exportOptions/filterOptions");
            m_filterOptions.m_createdBy = node.valueOf("createdby");
            m_filterOptions.m_modifiedBy = node.valueOf("modifiedby");
            m_filterOptions.m_createdAfter = node.valueOf("createdafter");
            m_filterOptions.m_createdBefore = node.valueOf("createdbefore");
            m_filterOptions.m_modifiedAfter = node.valueOf("modifiedafter");
            m_filterOptions.m_modifiedBefore = node.valueOf("modifiedbefore");
            m_filterOptions.m_status = node.valueOf("status");
            m_filterOptions.m_domain = node.valueOf("domain");
            m_filterOptions.m_project = node.valueOf("project");

            m_filterOptions.m_conditions.clear();

            List filters = p_root.selectNodes(
                "/exportOptions/filterOptions/conditions/condition");
            for (int i = 0; i < filters.size(); ++i)
            {
                Element filter = (Element)filters.get(i);
                FilterCondition condition = new FilterCondition();

                condition.m_level = filter.valueOf("level");
                condition.m_field = filter.valueOf("field");
                condition.m_operator = filter.valueOf("operator");
                condition.m_value = filter.valueOf("value");
                condition.m_matchcase = filter.valueOf("matchcase");

                m_filterOptions.m_conditions.add(condition);
            }

            node = p_root.selectSingleNode("/exportOptions/outputOptions");
            m_outputOptions.m_systemFields = node.valueOf("systemFields");
            m_outputOptions.m_separator = node.valueOf("separator");
            m_outputOptions.m_writeHeader = node.valueOf("writeHeader");

            m_columns.clear();

            List columns = p_root.selectNodes(
                "/exportOptions/columnOptions/column");
            for (int i = 0; i < columns.size(); ++i)
            {
                Element col = (Element)columns.get(i);
                ColumnDescriptor desc = new ColumnDescriptor();

                desc.m_position = Integer.parseInt(col.attributeValue("id"));
                desc.m_name = col.valueOf("name");
                desc.m_type = col.valueOf("type");
                desc.m_termLanguage = col.valueOf("termLanguage");
                desc.m_encoding = col.valueOf("encoding");

                m_columns.add(desc);
            }
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }
}
