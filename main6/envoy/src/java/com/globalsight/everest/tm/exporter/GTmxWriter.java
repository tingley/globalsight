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

package com.globalsight.everest.tm.exporter;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.everest.tm.util.Tmx;

import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;

import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IWriter;

import com.globalsight.util.SessionInfo;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;
import java.io.IOException;
import java.io.*;

/**
 * Writes TU entries to a GTMX file (TMX with GlobalSight extensions) as
 * directed by the conversion settings in the supplied export options.
 */
public class GTmxWriter
    implements IWriter
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            GTmxWriter.class);

    //
    // Private Member Variables
    //
    private Tm m_database;
    private com.globalsight.everest.tm.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;

    private Tmx m_tmx;

    //
    // Constructors
    //

    public GTmxWriter (ExportOptions p_options, Tm p_database)
    {
        m_database = p_database;
        setExportOptions(p_options);
    }

    //
    // Interface Implementation -- IWriter
    //

    public void setExportOptions(ExportOptions p_options)
    {
        m_options = (com.globalsight.everest.tm.exporter.ExportOptions)
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
     * Writes the file header (eg for TBX).
     */
    public void writeHeader(SessionInfo p_session)
        throws IOException
    {
        m_filename = m_options.getFileName();

        String directory = ExportUtil.getExportDirectory();

        // XML files get written as Unicode (in UTF-8).
        m_output = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(
                directory + m_filename), "UTF8")));

        m_tmx = createTmxHeader(p_session);

        m_output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        m_output.print("<tmx version=\"");
        m_output.print(m_tmx.getTmxVersion());
        m_output.println("\">");
        m_output.println(m_tmx.getHeaderXml());
        m_output.println("<body>");

        checkIOError();
    }

    /**
     * Writes the file trailer (eg for TBX).
     */
    public void writeTrailer(SessionInfo p_session)
        throws IOException
    {
        m_output.println("</body>");
        m_output.println("</tmx>");

        m_output.close();
    }

    /**
     * Writes the next few entries to the file.
     *
     * @param p_entries ArrayList of Entry objects.
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
        BaseTmTu tu;

        try
        {
            // TODO
            // tu = (Tu)p_entry;
            // tu.setDom(convertMtf(dom));

            String xml = "<tu></tu>";

            m_output.println(xml);

            checkIOError();
        }
        catch (IOException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new IOException("internal error: " + ex.toString());
        }
    }

    /**
     * Creates a Tmx (header) structure holding all our header info.
     */
    private Tmx createTmxHeader(SessionInfo p_session)
    {
        Tmx result = new Tmx();

        // Mandatory attributes.
        result.setTmxVersion(Tmx.TMX_14);
        result.setCreationTool(Tmx.GLOBALSIGHT);
        result.setCreationToolVersion(Tmx.GLOBALSIGHTVERSION);
        result.setSegmentationType(Tmx.SEGMENTATION_SENTENCE);
        result.setOriginalFormat(Tmx.TMF_GXML);
        result.setAdminLang(Tmx.DEFAULT_ADMINLANG);

        // TODO: get source language from TM or ExportOptions
        // This is a default, individual TUs can overwrite this.
        result.setSourceLang(Tmx.DEFAULT_SOURCELANG);

        result.setDatatype(Tmx.DATATYPE_HTML);

        // Optional attributes.

        // original encoding: unknown.

        result.setCreationDate(m_database.getCreationDate());
        result.setCreationId(p_session.getUserName()
            /*TODO: m_database.getCreationUser()*/);

        // TODO: Don't have information about last modification.
        result.setChangeDate(new Date());
        result.setChangeId(p_session.getUserName()
            /*TODO: m_database.getModificationUser()*/);

        result.addNote("CvdL did this.");

        return result;
    }

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
