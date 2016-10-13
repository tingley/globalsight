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

package com.globalsight.everest.projecthandler.importer;

import org.apache.log4j.Logger;

import com.globalsight.importer.ImporterException;

import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * This class collects all the options related to importing
 * project-related data files. There are the following sets of
 * options:
 *
 * ImportOptions           - what to do with the specific import file;
 * Synchronization Options - determining how entries are added
 *                           to the database when duplicates exists.
 */
public class ImportOptions
    extends com.globalsight.importer.ImportOptions
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ImportOptions.class);

    // File types.
    public static final String TYPE_UNKNOWN = "unknown";
    // native format
    public static final String TYPE_XML = "xml";
    // comma-delimited
    public static final String TYPE_CSV = "csv";

    /**
     * How to deal with duplicate data: merge file data into database,
     * overwrite database from file content or discard file data.
     */
    public static final String SYNC_OVERWRITE = "overwrite";
    public static final String SYNC_MERGE = "merge";
    public static final String SYNC_DISCARD = "discard";

    //
    // Private Classes
    //

    /**
     * Describes columns in a CSV file so they can be mapped properly
     * to the Project XML.
     */
    public class ColumnDescriptor
    {
        /** The index of this column. */
        public int m_position;
        /** Column name from the file header or specified by the user. */
        public String m_name;
        /** Example value of this column taken from the file. */
        public String m_example;
        /** User-assigned type (data category) of this column. */
        public String m_type;
        /**
         * Some types may use subtypes to specify representation
         * formats, e.g. dates in M/d/YY format or dd/mm/yyyy or
         * whatever.
         */
        public String m_subtype;

        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<column id=\"");
            result.append(m_position);
            result.append("\">");
            result.append("<name>");
            result.append(EditUtil.encodeXmlEntities(m_name));
            result.append("</name>");
            result.append("<example>");
            result.append(EditUtil.encodeXmlEntities(m_example));
            result.append("</example>");
            result.append("<type>");
            result.append(EditUtil.encodeXmlEntities(m_type));
            result.append("</type>");
            result.append("<subtype>");
            result.append(EditUtil.encodeXmlEntities(m_subtype));
            result.append("</subtype>");
            result.append("</column>");

            return result.toString();
        }
    }

    /**
     * Specifies the synchronization behavior during import, i.e. how
     * duplicates are detected, and how they are dealt with.
     */
    public class SyncOptions
    {
        /** One of "add_as_new", "conceptid", "language" */
        public String m_syncMode = "";

        public String getXml()
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

    //
    // Private Members
    //

    /** List of ColumnDescriptor objects. */
    private ArrayList m_columns = new ArrayList();
    private SyncOptions m_syncOptions = new SyncOptions();

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

    public void setSyncMode(String p_mode)
    {
        m_syncOptions.m_syncMode = p_mode;
    }

    public String getSyncMode()
    {
        return m_syncOptions.m_syncMode;
    }

    //
    // Overwritten Abstract Methods
    //

    /**
     * Returns an ImportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    protected String getOtherXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<columnOptions>");

        for (int i = 0; i < m_columns.size(); ++i)
        {
            result.append(((ColumnDescriptor)m_columns.get(i)).getXml());
        }

        result.append("</columnOptions>");

        result.append(m_syncOptions.getXml());

        return result.toString();
    }


    /**
     * Reads and validates a ImportOptions XML string.
     *
     * Xml Format:
     */
    protected void initOther(Element p_root)
        throws ImporterException
    {
        try
        {
            clearColumns();

            List columns = p_root.selectNodes(
                "/importOptions/columnOptions/column");
            for (int i = 0; i < columns.size(); ++i)
            {
                Element col = (Element)columns.get(i);
                ColumnDescriptor desc = new ColumnDescriptor();

                desc.m_position = Integer.parseInt(col.attributeValue("id"));
                desc.m_name = col.elementText("name");
                desc.m_example = col.elementText("example");
                desc.m_type = col.elementText("type");
                desc.m_subtype = col.elementText("subtype");

                m_columns.add(desc);
            }

            Element elem = (Element)p_root.selectSingleNode(
                "/importOptions/syncOptions");

            m_syncOptions.m_syncMode = elem.elementText("syncMode");
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }
}
