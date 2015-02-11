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

package com.globalsight.terminology.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import com.globalsight.importer.ImporterException;
import com.globalsight.util.edit.EditUtil;

/**
 * This class collects all the options related to importing
 * terminology files.  There are three sets of options:
 *
 * FileOptions             - filename, type, encoding;
 * ColumnOptions           - describing columns in CSV files;
 * Synchronization Options - determining how entries are added
 *                           to the database when duplicates exists.
 */
public class ImportOptions
    extends com.globalsight.importer.ImportOptions
{
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_XML = "xml";
    public static final String TYPE_CSV = "csv";
    public static final String TYPE_MTW = "mtw";  // MultiTerm 1.x
    public static final String TYPE_MTF = "mtf";  // MultiTerm 5+
    public static final String TYPE_TBX = "tbx";  // MultiTerm 5+
    public static final String TYPE_EXCEL = "excel";

    // These constants get used in the UI, don't modify them.
    public static final String SYNC_BY_NONE      = "add_as_new";
    public static final String SYNC_BY_CONCEPTID = "sync_on_concept";
    public static final String SYNC_BY_LANGUAGE  = "sync_on_language";

    public static final String SYNC_OVERWRITE = "overwrite";
    public static final String SYNC_MERGE     = "merge";
    public static final String SYNC_DISCARD   = "discard";

    public static final String NOSYNC_DISCARD   = "discard";
    public static final String NOSYNC_ADD       = "add";

    //
    // Private Classes
    //

    /**
     * Specifies the synchronization behavior during import, i.e. how
     * duplicates are detected, and how they are dealt with.
     */
    public class SyncOptions
    {
        /** One of "add_as_new", "conceptid", "language" */
        public String m_syncMode = "";

        /** If "language", this is the language name */
        public String m_syncLanguage = "";

        /** One of "overwrite", "merge", "discard" */
        public String m_syncAction = "";

        /** One of "add", "discard" */
        public String m_nosyncAction = "";

        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<syncOptions>");
            result.append("<syncMode>");
            result.append(EditUtil.encodeXmlEntities(m_syncMode));
            result.append("</syncMode>");
            result.append("<syncLanguage>");
            result.append(EditUtil.encodeXmlEntities(m_syncLanguage));
            result.append("</syncLanguage>");
            result.append("<syncAction>");
            result.append(EditUtil.encodeXmlEntities(m_syncAction));
            result.append("</syncAction>");
            result.append("<nosyncAction>");
            result.append(EditUtil.encodeXmlEntities(m_nosyncAction));
            result.append("</nosyncAction>");
            result.append("</syncOptions>");

            return result.toString();
        }
    }

    /**
     * Describes columns in a CSV file so they can be mapped properly
     * to the EntryStructure XML.
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
        /** If type=term, the term's language name. */
        public String m_termLanguage;
        /** Column encoding if different from file's primary encoding. */
        public String m_encoding;
        /**
         * The column this column is associated with.  Example: a
         * termDefinition is associated with a term contained in
         * another column.
         */
        public String m_associatedColumn;

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
            result.append("<termLanguage>");
            result.append(EditUtil.encodeXmlEntities(m_termLanguage));
            result.append("</termLanguage>");
            result.append("<encoding>");
            result.append(EditUtil.encodeXmlEntities(m_encoding));
            result.append("</encoding>");
            result.append("<associatedColumn>");
            result.append(EditUtil.encodeXmlEntities(m_associatedColumn));
            result.append("</associatedColumn>");
            result.append("</column>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private SyncOptions m_syncOptions = new SyncOptions();
    /** List of ColumnDescriptor objects. */
    private ArrayList m_columns = new ArrayList();
    // key: excel sheet number, value: list of columnDescriptor objects.
    private Map m_excelColumnDescriptors = null;
    // keyL excel sheet number, value: column header row number.
    private Map m_excelSheetColumnHeader = null;
    
    public void putColumnDescriptors(int p_sheetNumber, List p_colDescriptors)
    {
    	if (m_excelColumnDescriptors == null)
    	{
    		m_excelColumnDescriptors = new HashMap();
    	}
    	m_excelColumnDescriptors.put(new Integer(p_sheetNumber), p_colDescriptors);
    }
    
    public List getColumnDescriptors(int p_sheetNumber)
    {
    	if (m_excelColumnDescriptors != null)
    	{
    		return (List)m_excelColumnDescriptors.get(new Integer(p_sheetNumber));
    	}
    	return null;
    }
    
    public Map getColumnDescriptors()
    {
    	return m_excelColumnDescriptors;
    }
    
    public void putColumnHeaderRow(int p_sheetNumber, int p_columnHeaderRow)
    {
    	if (m_excelSheetColumnHeader == null)
    	{
    		m_excelSheetColumnHeader = new HashMap();
    	}
    	m_excelSheetColumnHeader.put(new Integer(p_sheetNumber), 
    			new Integer(p_columnHeaderRow));
    }
    
    public int getColumnHeaderRow(int p_sheetNumber)
    {
    	if (m_excelSheetColumnHeader != null)
    	{
    		Integer value = (Integer)m_excelSheetColumnHeader.get(new Integer(p_sheetNumber));
    		if (value != null)
    		{
    		    return value.intValue();
    		}
    	}
    	return -1;
    }
    
    public Map getColumnHeaderRow()
    {
    	return m_excelSheetColumnHeader;
    }

    //
    // Constructors
    //
    protected ImportOptions ()
    {
        super();
    }

    //
    // Public Methods
    //

    public void setSyncMode(String p_mode)
    {
        m_syncOptions.m_syncMode = p_mode;
    }

    public String getSyncMode()
    {
        return m_syncOptions.m_syncMode;
    }

    public void setSyncLanguage(String p_language)
    {
        m_syncOptions.m_syncLanguage = p_language;
    }

    public String getSyncLanguage()
    {
        return m_syncOptions.m_syncLanguage;
    }

    public void setSyncAction(String p_action)
    {
        m_syncOptions.m_syncAction = p_action;
    }

    public String getSyncAction()
    {
        return m_syncOptions.m_syncAction;
    }

    public void setNosyncAction(String p_action)
    {
        m_syncOptions.m_nosyncAction = p_action;
    }

    public String getNosyncAction()
    {
        return m_syncOptions.m_nosyncAction;
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
     * Returns an ImportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    protected String getOtherXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append(m_syncOptions.getXml());

        result.append("<columnOptions>");

        for (int i = 0, max = m_columns.size(); i < max; ++i)
        {
            result.append(((ColumnDescriptor)m_columns.get(i)).getXml());
        }

        result.append("</columnOptions>");

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
            Element elem = (Element)p_root.selectSingleNode("//syncOptions");
            m_syncOptions.m_syncMode = elem.elementText("syncMode");
            m_syncOptions.m_syncLanguage = elem.elementText("syncLanguage");
            m_syncOptions.m_syncAction = elem.elementText("syncAction");
            m_syncOptions.m_nosyncAction = elem.elementText("nosyncAction");

            m_columns.clear();

            List columns = p_root.selectNodes(
                "/importOptions/columnOptions/column");
            for (int i = 0, max = columns.size(); i < max; ++i)
            {
                Element col = (Element)columns.get(i);
                ColumnDescriptor desc = new ColumnDescriptor();

                desc.m_position = Integer.parseInt(col.attributeValue("id"));
                desc.m_name = col.elementText("name");
                desc.m_example = col.elementText("example");
                desc.m_type = col.elementText("type");
                desc.m_termLanguage = col.elementText("termLanguage");
                desc.m_encoding = col.elementText("encoding");
                desc.m_associatedColumn = col.elementText("associatedColumn");

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
