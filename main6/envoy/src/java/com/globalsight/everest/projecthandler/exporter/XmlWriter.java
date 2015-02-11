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

import com.globalsight.everest.projecthandler.Project;

import com.globalsight.everest.projecthandler.exporter.ExportUtil;

import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IWriter;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.UTC;

import com.globalsight.util.XmlParser;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;
import java.io.IOException;
import java.io.*;

/**
 * Writes entries to an XML file as directed by the conversion settings
 * in the supplied export options.
 */
public class XmlWriter
    implements IWriter
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            XmlWriter.class);

    //
    // Private Member Variables
    //
    private /*TODO*/Object m_database;
    private com.globalsight.everest.projecthandler.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;

    //
    // Constructors
    //

    public XmlWriter (ExportOptions p_options, /*TODO*/Object p_database)
    {
        m_database = p_database;
        setExportOptions(p_options);
    }

    //
    // Interface Implementation -- IWriter
    //

    public void setExportOptions(ExportOptions p_options)
    {
        m_options = (com.globalsight.everest.projecthandler.exporter.ExportOptions)
            p_options;
    }

    /**
     * Analyzes export options and returns an updated ExportOptions
     * object with a status whether the options are syntactically
     * correct.
     */
    public ExportOptions analyze()
    {
        return m_options;
    }

    /**
     * Writes the file header.
     */
    public void writeHeader(SessionInfo p_session)
        throws IOException
    {
        m_filename = m_options.getFileName();

        String directory = ExportUtil.getExportDirectory();
        String encoding = m_options.getJavaEncoding();

        if (encoding == null || encoding.length() == 0)
        {
            throw new IOException("invalid encoding " +
                m_options.getEncoding());
        }

        String filename = directory + m_filename;

        new File(filename).delete();

        m_output = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(filename), encoding)));

        // TODO
        m_output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        m_output.print("<projectdata>");

        checkIOError();
    }

    /**
     * Writes the file trailer and closes the file.
     */
    public void writeTrailer(SessionInfo p_session)
        throws IOException
    {
        m_output.print("</projectdata>");
        m_output.close();
    }

    /**
     * Writes the next few entries to the file.
     *
     * @param p_entries ArrayList of TU objects.
     */
    public void write(ArrayList p_entries, SessionInfo p_session)
        throws IOException
    {
        for (int i = 0; i < p_entries.size(); ++i)
        {
            Object o = p_entries.get(i);

            write(o, p_session);
        }
    }

    /**
     * Writes a single entry to the export file.
     *
     * @see Entry
     */
    public void write(Object p_entry, SessionInfo p_session)
        throws IOException
    {
        if (p_entry == null)
        {
            return;
        }

        // TODO: convert internal entries to display form.

        try
        {
            Object o = (Object)p_entry;

            // Goes through lengths not to throw an IO exception,
            // will check below.
            m_output.println("<data>test, test, test, test</data>");
        }
        catch (Throwable ignore)
        {
            CATEGORY.error("Can't handle entry, skipping.", ignore);
        }

        checkIOError();
    }

    //
    // Private Methods
    //

    private void checkIOError()
        throws IOException
    {
        // The JDK is so incredibly inconsistent (aka, stupid).
        // PrintWriter.println() does not throw exceptions.
        if (m_output.checkError())
        {
            throw new IOException("write error");
        }
    }
}
