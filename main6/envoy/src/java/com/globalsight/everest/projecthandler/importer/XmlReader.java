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

import com.globalsight.everest.projecthandler.importer.ImportUtil;
import com.globalsight.everest.projecthandler.importer.XmlReaderThread;

import com.globalsight.everest.projecthandler.Project;

import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;


import org.dom4j.*;
import org.dom4j.io.SAXReader;

import org.xml.sax.InputSource;

import java.io.*;
import java.util.*;

/**
 * Reads XML files and produces objects.
 */
public class XmlReader
    implements IReader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            XmlReader.class);

    //
    // Private Member Variables
    //
    private /*TODO*/Object m_database;
    private ImportOptions m_options;

    private XmlReaderThread m_thread = null;
    private ReaderResultQueue m_results = null;
    private ReaderResult m_result;

    private int m_entryCount;

    //
    // Constructors
    //

    public XmlReader (ImportOptions p_options, /*TODO*/Object p_database)
    {
        m_database = p_database;
        setImportOptions(p_options);
    }

    //
    // Interface Implementation -- IReader
    //

    public void setImportOptions(ImportOptions p_options)
    {
        m_options = p_options;
    }

    public boolean hasNext()
    {
        // Ensure the thread is running
        startThread();

        m_result = m_results.get();

        if (m_result != null)
        {
            return true;
        }

        // No more results, clean up
        stopThread();
        return false;
    }

    public ReaderResult next()
    {
        return m_result;
    }

    /**
     * Analyzes the import file and returns an updated ImportOptions
     * object with a status whether the file is syntactically correct,
     * the number of expected entries, and column descriptors in case
     * of CSV files.
     */
    public ImportOptions analyze()
    {
        m_entryCount = 0;

        try
        {
            if (!m_options.getStatus().equals(m_options.ANALYZED))
            {
                // We check if the file is a valid XML file.
                analyzeXml(m_options.getFileName());

                m_options.setStatus(m_options.ANALYZED);
                m_options.setExpectedEntryCount(m_entryCount);
            }
        }
        catch (Exception ex)
        {
            m_options.setError(ex.getMessage());
        }

        return m_options;
    }

    //
    // Private Methods
    //
    private void startThread()
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (100);
            m_thread = new XmlReaderThread(m_results, m_options, m_database);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        if (m_thread != null)
        {
            m_results.consumerDone();
            m_results = null;
            m_thread = null;
        }
    }


    /**
     * Reads an XML file and checks its correctness by validating
     * against the TMX DTD. If there's any error in the file, an
     * exception is thrown.
     */
    private void analyzeXml(String p_url)
        throws Exception
    {
        CATEGORY.debug("Analyzing document: " + p_url);

        SAXReader reader = new SAXReader();

        // TODO: Read the DTD and validate.
        // See com.globalsight.everest.tm.util.DtdResolver;

        // reader.setEntityResolver(DtdResolver.getInstance());
        // reader.setValidation(true);

        // enable element complete notifications to conserve memory
        //TODO
        reader.addHandler("/projectdata/data",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                        ++m_entryCount;
                    }

                    public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        // prune the current element to reduce memory
                        element.detach();
                    }
                }
            );

        Document document = reader.read(p_url);

        // all done
    }
}
