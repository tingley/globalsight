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

package com.globalsight.importer;

import org.apache.log4j.Logger;

import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;


/**
 * This class collects all the options related to importing a file
 * with entries. There are two sets of options:
 *
 * FileOptions - filename, type, encoding;
 *
 * and Synchronization Options determining how entries are added to
 * the database when duplicates exists.
 */
public abstract class ImportOptions
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ImportOptions.class.getName());

    /** Import file has been initialized (not analyzed yet). */
    public static final String OK = "ok";
    /** Import file has been analyzed. */
    public static final String ANALYZED = "analyzed";
    /** Import file has an error. */
    public static final String ERROR = "error";

    //
    // Internal Classes
    //

    /**
     * FileOptions describes the file name, file type, and primary
     * file encoding.
     */
    public class FileOptions
    {
        /** The file name of the file to be imported. */
        public String m_name = "";

        /** File type: one of "XML", "CSV", "MTW". "Unknown" */
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


        public String asXML()
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

    //
    // Private Members
    //
    private FileOptions m_fileOptions = new FileOptions();

    //
    // Constructors
    //
    public ImportOptions ()
    {
    }

    /* Cannot have the XML string constructor because derived
       subclasses haven't been fully constructed yet when we call the
       subclass' constructor helper (initOther()).

       So to initialize this object from an XML string:
       - o = new ImportOptions()
       - o.init(XML)

    public ImportOptions (String p_options)
        throws ImporterException
    {
        init(p_options);
    }
    */

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
        // if the type isn't set yet  - get
        // it from the filename
        if (m_fileOptions.m_type == null ||
            m_fileOptions.m_type.length() <= 0)
        {
            return getTypeFromExtension();
        }

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

    public String getErrorMessage()
    {
        return m_fileOptions.m_errorMessage;
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

        result.append(m_fileOptions.asXML());
        result.append(getOtherXml());

        result.append("</importOptions>");

        return result.toString();
    }

    /**
     * Initializes this object (and derived objects) from an XML string.
     * Derived objects must implement initOther().
     */
    public void init(String p_options)
        throws ImporterException
    {
        XmlParser parser = null;
        Document dom = null;

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_options);

            Element root = dom.getRootElement();

            Node node = root.selectSingleNode("//fileOptions");

            // This used to use node.valueOf("childnode") but then
            // entities were incorrectly decoded by the Jaxen library,
            // i.e. "A&amp;B" became "A & B" instead of "A&B".

            Element elem = (Element)node;
            m_fileOptions.m_name = elem.elementText("fileName");
            m_fileOptions.m_type = elem.elementText("fileType");
            m_fileOptions.m_encoding = elem.elementText("fileEncoding");
            m_fileOptions.m_separator = elem.elementText("separator");
            m_fileOptions.m_ignoreHeader = elem.elementText("ignoreHeader");
            m_fileOptions.m_entryCount = elem.elementText("entryCount");
            m_fileOptions.m_status = elem.elementText("status");
            m_fileOptions.m_errorMessage = elem.elementText("errorMessage");

            initOther(root);
        }
        catch (ImporterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            CATEGORY.error("", e);
            // cast exception and throw
            error(e.getMessage(), e);
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    /**
     * Abstract method for subclasses to add their option groups into
     * the XML representation.
     */
    abstract protected String getOtherXml();

    /**
     * Abstract method for subclasses to add their option groups into
     * the XML representation.
     */
    abstract protected void initOther(Element p_root)
        throws ImporterException;

    //
    // Private Methods
    //

    /**
     * Throws an INVALID_IMPORT_OPTIONS exception.
     */
    protected void error(String p_reason, Exception p_exception)
        throws ImporterException
    {
        String[] args = { p_reason };
        throw new ImporterException(
            ImporterException.MSG_INVALID_IMPORT_OPTIONS,
            args, p_exception);
    }

    /** Returns the file extension. Caller will handle errors. */
    private String getTypeFromExtension()
    {
        String filename = getFileName();
        String ext = "";

        if (filename != null && filename.length() > 0)
        {
            int index = filename.lastIndexOf('.');
            if (index >= 0)
            {
                ext = filename.substring(index + 1);
            }
        }

        return ext;
    }

}

