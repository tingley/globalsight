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

package com.globalsight.exporter;

import org.apache.log4j.Logger;

import com.globalsight.exporter.ExporterException;

import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

import com.globalsight.ling.common.CodesetMapper;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * This class collects the common options related to exporting files
 * (terminology+tm). There are the following sets of options:
 *
 * FileOptions   - (filename,) type, encoding;
 *
 * This class must be subclassed and enhanced with options specific to
 * the exported data and file type.
 */
public abstract class ExportOptions
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExportOptions.class);

    /** Export database has been initialized (not analyzed yet). */
    public static final String OK = "ok";
    /** Export database has been analyzed. */
    public static final String ANALYZED = "analyzed";
    /** Export database has an error. */
    public static final String ERROR = "error";

    /**
     * FileOptions describes the file name, file type, and primary
     * file encoding.
     */
    public class FileOptions
    {
        /** The file name of the file to export to. */
        public String m_name = "";

        /** File type: one of "XML", "CSV", "MTW". */
        public String m_type = "";

        /**
         * IANA-encoding of the output file, not the Java encoding.
         *
         * @see com.globalsight.ling.common.CodesetMapper
         */
        public String m_encoding = "";

        /**
         * Expected number of entries to be exported.
         */
        public String m_entryCount = "0";

        /**
         * Export status; "ok" in the beginning, "analyzed" if the
         * database has been analyzed (checked for how many entries
         * will be exported), and "error" when an unrecoverable error
         * has been detected..
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
    protected ExportOptions ()
    {
    }

    /* Cannot have the XML string constructor because derived
       subclasses haven't been fully constructed yet when we call the
       subclass' constructor helper (initOther()).

       So to initialize this object from an XML string:
       - o = new ExportOptions()
       - o.init(XML)

    protected ExportOptions (String p_options)
        throws ExporterException
    {
        init(p_options);
    }
    */

    //
    // Public Methods
    //
    public FileOptions getFileOptions()
    {
        return m_fileOptions;
    }

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

    /** IANA encoding of the output file. */
    public String getEncoding()
    {
        return m_fileOptions.m_encoding;
    }

    /** Java encoding of the output file. */
    public String getJavaEncoding()
    {
        return CodesetMapper.getJavaEncoding(m_fileOptions.m_encoding);
    }

    public void setFileType(String p_type)
    {
        m_fileOptions.m_type = p_type;
    }

    public String getFileType()
    {
        return m_fileOptions.m_type;
    }

    public void setExpectedEntryCount(int p_count)
    {
        m_fileOptions.m_entryCount = String.valueOf(p_count);
    }

    public int getExpectedEntryCount()
    {
        return Integer.parseInt(m_fileOptions.m_entryCount);
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

    public void setStatus(String p_status)
    {
        m_fileOptions.m_status = p_status;
    }

    public String getStatus()
    {
        return m_fileOptions.m_status;
    }

    /**
     * Returns an ExportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<exportOptions>");
        result.append(m_fileOptions.asXML());
        result.append(getOtherXml());
        result.append("</exportOptions>");

        return result.toString();
    }

    /**
     * Initializes this object (and derived objects) from an XML string.
     * Derived objects must implement initOther().
     */
    public void init(String p_options)
        throws ExporterException
    {
        XmlParser parser = null;
        Document dom;

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_options);

            Element root = dom.getRootElement();

            Element elem = (Element)root.selectSingleNode("//fileOptions");
            m_fileOptions.m_name = elem.elementText("fileName");
            m_fileOptions.m_type = elem.elementText("fileType");
            m_fileOptions.m_encoding = elem.elementText("fileEncoding");
            m_fileOptions.m_entryCount = elem.elementText("entryCount");
            m_fileOptions.m_status = elem.elementText("status");
            m_fileOptions.m_errorMessage = elem.elementText("errorMessage");

            initOther(root);
        }
        catch (ExporterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
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
        throws ExporterException;

    //
    // Protected Methods
    //

    /**
     * Throws an INVALID_EXPORT_OPTIONS exception.
     */
    protected void error(String p_reason, Exception p_exception)
        throws ExporterException
    {
        String[] args = { p_reason };

        throw new ExporterException(
            ExporterException.MSG_INVALID_EXPORT_OPTIONS,
            args, p_exception);
    }
}
