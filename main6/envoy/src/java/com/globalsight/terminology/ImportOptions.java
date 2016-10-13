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

package com.globalsight.terminology;

import org.apache.log4j.Logger;

import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.util.XmlParser;

import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;

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
    implements TermbaseExceptionMessages
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ImportOptions.class);

    /** Import file has been initialized (not analyzed yet). */
    public static final String OK = "ok";
    /** Import file has been analyzed. */
    public static final String ANALYZED = "analyzed";
    /** Import file has an error. */
    public static final String ERROR = "error";

    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_XML = "xml";
    public static final String TYPE_CSV = "csv";
    public static final String TYPE_MTW = "mtw";
    public static final String TYPE_MTF = "mtf";  // MultiTerm 5+
    public static final String TYPE_TBX = "tbx";

    //
    // Private Classes
    //

    /**
     * FileOptions describes the file name, file type, and primary
     * file encoding.
     */
    public class FileOptions
    {
        /** The file name of the file to be imported. */
        public String m_name = "";

        /** File type: one of "XML", "CSV", "MTW". */
        public String m_type = "";

        /**
         * IANA-encoding of the file, not the Java encoding.
         *
         * @see com.globalsight.ling.common.CodesetMapper
         */
        public String m_encoding = "";

        /** If type == CSV, this holds the column separator character. */
        public String m_separator = "";

        /**
         * If type == CSV, says first line is header line. Value must
         * be "true" or "false".
         */
        public String m_ignoreHeader = "";

        /**
         * Expected number of entries as calculated by counting lines
         * in CSV or <conceptGrp>s in XML files.
         */
        public String m_entryCount = "0";

        /**
         * Import status; "ok" in the beginning, "analyzed" if the
         * file has been analyzed (checked for correctness), and
         * "error" when an unrecoverable error has been detected, for
         * instance an invalid XML file.
         */
        public String m_status = OK;

        /**
         * Error message when m_status is "error".
         */
        public String m_errorMessage = "";


        public String getXml()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<fileOptions>");
            result.append("<fileName>");
            result.append(EditUtil.encodeXmlEntities(m_name));
            result.append("</fileName>");
            result.append("<fileType>");
            result.append(EditUtil.encodeXmlEntities(m_type));
            result.append("</fileType>");
            result.append("<fileEncoding>");
            result.append(EditUtil.encodeXmlEntities(m_encoding));
            result.append("</fileEncoding>");
            result.append("<separator>");
            result.append(EditUtil.encodeXmlEntities(m_separator));
            result.append("</separator>");
            result.append("<ignoreHeader>");
            result.append(EditUtil.encodeXmlEntities(m_ignoreHeader));
            result.append("</ignoreHeader>");
            result.append("<entryCount>");
            result.append(EditUtil.encodeXmlEntities(m_entryCount));
            result.append("</entryCount>");
            result.append("<status>");
            result.append(EditUtil.encodeXmlEntities(m_status));
            result.append("</status>");
            result.append("<errorMessage>");
            result.append(EditUtil.encodeXmlEntities(m_errorMessage));
            result.append("</errorMessage>");
            result.append("</fileOptions>");

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
            result.append("</syncOptions>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private FileOptions m_fileOptions = new FileOptions();
    private SyncOptions m_syncOptions = new SyncOptions();
    /** List of ColumnDescriptor objects. */
    private ArrayList m_columns = new ArrayList();

    //
    // Constructors
    //
    protected ImportOptions ()
    {
    }

    protected ImportOptions (String p_options)
        throws TermbaseException
    {
        init(p_options);
    }

    //
    // Public Methods
    //

    public void setFileName(String p_filename)
    {
        m_fileOptions.m_name = p_filename;
    }

    public String getFileName()
    {
        return m_fileOptions.m_name;
    }

    public void setEncoding(String p_encoding)
    {
        m_fileOptions.m_encoding = p_encoding;
    }

    public String getEncoding()
    {
        return m_fileOptions.m_encoding;
    }

    public void setFileType(String p_type)
    {
        m_fileOptions.m_type = p_type;
    }

    public String getFileType()
    {
        return m_fileOptions.m_type;
    }

    public void setSeparator(String p_sep)
    {
        m_fileOptions.m_separator = p_sep;
    }

    public String getSeparator()
    {
        return m_fileOptions.m_separator;
    }

    public FileOptions getFileOptions()
    {
        return m_fileOptions;
    }

    public boolean isIgnoreHeader()
    {
        return m_fileOptions.m_ignoreHeader.equalsIgnoreCase("true");
    }

    public void setExpectedEntryCount(int p_count)
    {
        m_fileOptions.m_entryCount = String.valueOf(p_count);
    }

    public int getExpectedEntryCount()
    {
        return Integer.parseInt(m_fileOptions.m_entryCount);
    }

    public void setStatus(String p_status)
    {
        m_fileOptions.m_status = p_status;
    }

    public String getStatus()
    {
        return m_fileOptions.m_status;
    }

    public void setError(String p_message)
    {
        m_fileOptions.m_status = ERROR;
        m_fileOptions.m_errorMessage = p_message;
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


    /**
     * Returns an ImportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<importOptions>");

        result.append(m_fileOptions.getXml());

        result.append("<columnOptions>");

        for (int i = 0; i < m_columns.size(); ++i)
        {
            result.append(((ColumnDescriptor)m_columns.get(i)).getXml());
        }

        result.append("</columnOptions>");

        result.append(m_syncOptions.getXml());

        result.append("</importOptions>");


        return result.toString();
    }


    //
    // Private Methods
    //

    /**
     * Reads and validates a ImportOptions XML string.
     *
     * Xml Format:
     */
    private void init (String p_options)
        throws TermbaseException
    {
        XmlParser parser = null;
        Document dom;

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_options);
        }
        finally
        {
            XmlParser.fire(parser);
        }

        try
        {
            Element root = dom.getRootElement();

            Element elem = (Element)root.selectSingleNode(
                "/importOptions/fileOptions");

            m_fileOptions.m_name = elem.elementText("fileName");
            m_fileOptions.m_type = elem.elementText("fileType");
            m_fileOptions.m_encoding = elem.elementText("fileEncoding");
            m_fileOptions.m_separator = elem.elementText("separator");
            m_fileOptions.m_ignoreHeader = elem.elementText("ignoreHeader");
            m_fileOptions.m_entryCount = elem.elementText("entryCount");
            m_fileOptions.m_status = elem.elementText("status");
            m_fileOptions.m_errorMessage = elem.elementText("errorMessage");

            List columns = root.selectNodes(
                "/importOptions/columnOptions/column");
            for (int i = 0; i < columns.size(); ++i)
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

            elem = (Element)root.selectSingleNode(
                "/importOptions/syncOptions");

            m_syncOptions.m_syncMode = elem.elementText("syncMode");
            m_syncOptions.m_syncLanguage = elem.elementText("syncLanguage");
            m_syncOptions.m_syncAction = elem.elementText("syncAction");
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }

    /**
     * Throws an INVALID_IMPORT_OPTIONS exception.
     */
    private void error(String p_reason, Exception p_exception)
        throws TermbaseException
    {
        String[] args = { p_reason };

        throw new TermbaseException(MSG_INVALID_IMPORT_OPTIONS, args,
            p_exception);
    }
}
