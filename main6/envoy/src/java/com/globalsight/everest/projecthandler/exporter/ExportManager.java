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

import com.globalsight.exporter.ExportOptions;

import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IExportManagerImpl;
import com.globalsight.exporter.IReader;
import com.globalsight.exporter.IWriter;
import com.globalsight.util.ReaderResult;

import com.globalsight.everest.projecthandler.Project;

import com.globalsight.everest.projecthandler.exporter.CsvWriter;
import com.globalsight.everest.projecthandler.exporter.ExportUtil;
import com.globalsight.everest.projecthandler.exporter.Reader;

import com.globalsight.util.SessionInfo;

import java.util.*;
import java.io.IOException;

/**
 * <p>The RMI interface implementation for the Project Data Exporter.</p>
 *
 * <p>Export is implemented by a Producer-Consumer pipeline that reads
 * data from the database and writes it to the export file.</p>
 */
public class ExportManager
    extends IExportManagerImpl
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExportManager.class);

    /** For constructing file names. */
    static final private String FILE_PREFIX = "project_export_";

    /**
     * For constructing file names: files will be overwritten after
     * each restart.
     */
    static private int COUNT = 0;

    //
    // Private Members
    //
    private Project m_database;

    //
    // Constructor
    //
    public ExportManager(Project p_database, SessionInfo p_session)
        throws ExporterException
    {
        super(p_session);

        m_database = p_database;

        super.init();

        CATEGORY.debug("Default Project export manager created.");
    }

    //
    // Overwritten Abstract Methods
    //

    protected ExportOptions createExportOptions()
        throws ExporterException
    {
        return new com.globalsight.everest.projecthandler.exporter.ExportOptions();
    }

    protected ExportOptions createExportOptions(String p_options)
        throws ExporterException
    {
        com.globalsight.everest.projecthandler.exporter.ExportOptions result =
            new com.globalsight.everest.projecthandler.exporter.ExportOptions();

        result.init(p_options);

        return result;
    }

    protected String createFilename(ExportOptions p_options)
        throws ExporterException
    {
        StringBuffer result = new StringBuffer();

        result.append(FILE_PREFIX);
        result.append(COUNT++);
        result.append(".");
        result.append(getFileExtension(p_options));

        return result.toString();
    }

    protected IReader createReader(ExportOptions p_options)
        throws ExporterException
    {
        CATEGORY.debug("Export reader created.");

        com.globalsight.everest.projecthandler.exporter.ExportOptions options =
            (com.globalsight.everest.projecthandler.exporter.ExportOptions)p_options;

        return new Reader (options, m_database, m_session);
    }

    protected IWriter createWriter(ExportOptions p_options)
        throws ExporterException
    {
        com.globalsight.everest.projecthandler.exporter.ExportOptions options =
            (com.globalsight.everest.projecthandler.exporter.ExportOptions)p_options;

        String type = options.getFileType();

        if (type != null && type.length() > 0)
        {
            if (type.equalsIgnoreCase(options.TYPE_XML))
            {
                CATEGORY.debug("Export writer created of type XML");

                return new XmlWriter(options, m_database);
            }
            else if (type.equalsIgnoreCase(options.TYPE_CSV))
            {
                CATEGORY.debug("Export writer created of type CSV");

                return new CsvWriter(options, m_database);
            }
        }

        return null;
    }

    //
    // Private Methods
    //

    /** Returns a file extension based on the export file type */
    private String getFileExtension(ExportOptions p_options)
    {
        com.globalsight.everest.projecthandler.exporter.ExportOptions options =
            (com.globalsight.everest.projecthandler.exporter.ExportOptions)p_options;

        String type = options.getFileType();

        // Native GlobalSight exports are XML for now...
        if (type.equalsIgnoreCase(options.TYPE_XML))
        {
            return "xml";
        }

        return "txt";
    }
}
