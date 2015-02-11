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

import com.globalsight.terminology.exporter.ExportUtil;
import com.globalsight.terminology.exporter.GTXmlWriter;
import com.globalsight.terminology.exporter.MtfWriter;
import com.globalsight.terminology.exporter.Reader;

import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IExportManagerImpl;
import com.globalsight.exporter.IReader;
import com.globalsight.exporter.IWriter;
import com.globalsight.util.ReaderResult;

import com.globalsight.terminology.Termbase;

import com.globalsight.util.SessionInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;
import java.io.IOException;

/**
 * <p>The RMI interface implementation for the Terminology Exporter.</p>
 *
 * <p>Export is implemented by a Producer-Consumer pipeline that reads
 * entries from the TM and writes them to the export file.</p>
 */
public class ExportManager
    extends IExportManagerImpl
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExportManager.class);

    /** For constructing file names. */
    static final private String FILE_PREFIX = "tb_export_";

    /**
     * For constructing file names: files will be overwritten after
     * each restart.
     */
    static private int COUNT = 0;

    //
    // Private Members
    //
    private Termbase m_database = null;

    //
    // Constructor
    //
    public ExportManager(Termbase p_database, SessionInfo p_session)
        throws ExporterException
    {
        super(p_session);

        m_database = p_database;

        super.init();
    }

    //
    // Overwritten Abstract Methods
    //

    protected ExportOptions createExportOptions()
        throws ExporterException
    {
        return new com.globalsight.terminology.exporter.ExportOptions();
    }

    protected ExportOptions createExportOptions(String p_options)
        throws ExporterException
    {
        com.globalsight.terminology.exporter.ExportOptions result =
            new com.globalsight.terminology.exporter.ExportOptions();

        result.init(p_options);

        return result;
    }

    protected String createFilename(ExportOptions p_options)
        throws ExporterException
    {
        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)p_options;

        StringBuffer result = new StringBuffer();
        String type = options.getFileType();

        result.append(FILE_PREFIX);
        result.append(COUNT++);

        if (type.equals(options.TYPE_XML))
        {
            result.append(".xml");
        }
        else if (type.equals(options.TYPE_MTF))
        {
            result.append("-mtf.xml");
        }
        else if (type.equals(options.TYPE_HTM))
        {
            result.append(".html");
        }
        else if (type.equals(options.TYPE_TBX)) 
        {
        	result.append(".tbx");
        }

        return result.toString();
    }

    protected IReader createReader(ExportOptions p_options)
        throws ExporterException
    {
        CATEGORY.debug("Export reader created.");

        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)p_options;

        return new Reader (options, m_database, m_session);
    }

    protected IWriter createWriter(ExportOptions p_options)
        throws ExporterException
    {
        com.globalsight.terminology.exporter.ExportOptions options =
            (com.globalsight.terminology.exporter.ExportOptions)p_options;

        String type = options.getFileType();

        if (type != null && type.length() > 0)
        {
            if (type.equalsIgnoreCase(options.TYPE_XML))
            {
                CATEGORY.debug("Export writer created of type XML");

                return new GTXmlWriter(options);
            }
            else if (type.equalsIgnoreCase(options.TYPE_MTF))
            {
                CATEGORY.debug("Export writer created of type MTF");

                return new MtfWriter(options);
            }
            else if (type.equalsIgnoreCase(options.TYPE_HTM))
            {
                CATEGORY.debug("Export writer created of type HTML");

                return new HtmlWriter(options, m_database);
            }
            else if (type.equalsIgnoreCase(options.TYPE_TBX))
            {
            	CATEGORY.debug("Export writer created of type TBX");
            	
            	return new TbxWriter(options);
            }
        }

        return null;
    }
}
