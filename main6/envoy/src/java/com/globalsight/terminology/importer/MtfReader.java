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

import org.apache.log4j.Logger;

import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.IReader;

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;

import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;


import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;

/**
 * Reads MultiTerm MTF files and produces Entry objects.
 */
public class MtfReader
    implements IReader, TermbaseExceptionMessages
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            MtfReader.class);

    //
    // Private Member Variables
    //
    private Termbase m_termbase;
    private ImportOptions m_options;
    private int m_entryCount;

    private MtfReaderThread m_thread = null;
    private ReaderResultQueue m_results = null;
    private ReaderResult m_result;

    //
    // Constructors
    //

    public MtfReader (ImportOptions p_options, Termbase p_termbase)
    {
        m_termbase = p_termbase;
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
            m_thread = new MtfReaderThread(m_results, m_options, m_termbase);
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
     * Reads an XML file and checks for correctness. If there's any
     * error in the file, an exception is thrown.
     */
    private void analyzeXml(String p_url)
        throws Exception
    {
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        CATEGORY.debug("Analyzing document: " + p_url);

        // enable element complete notifications to conserve memory
        reader.addHandler("/mtf/conceptGrp",
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

                        // TODO: validate entry and report errors.
                    }
                }
            );

        Document document = reader.read(p_url);

        // all done
    }
}
