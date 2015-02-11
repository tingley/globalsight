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

package com.globalsight.everest.snippet.importer;

import org.apache.log4j.Logger;

// globalsight
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.GsaEndElement;
import com.globalsight.ling.docproc.GsaStartElement;
import com.globalsight.ling.docproc.Output;


//java
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

//import java.util.*;

/**
 * Reads Snippet files.  Works with SnippetFileReaderThread to
 * produce Snippet objects to be added to the SnippetLibrary.
 */
public class SnippetFileReader
    implements IReader
{
    private static final Logger c_logger =
        Logger.getLogger(
            SnippetFileReader.class);

    //
    // Private Member Variables
    //
    private ImportOptions m_options;

    private SnippetFileReaderThread m_thread = null;
    private ReaderResultQueue m_results = new ReaderResultQueue (100);
    private ReaderResult m_result;

    //
    // Constructors
    //

    public SnippetFileReader (ImportOptions p_options)
    {
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
     * Analyzes the file: checks correctness of the file and computes
     * how many elements it contains.
     */
    public ImportOptions analyze()
    {
        try
        {
            String url = m_options.getFileName();
            String encoding = m_options.getEncoding();
            int entryCount;

            c_logger.debug("analyzing file " + url);

            Output output = ImportUtil.extractFile(url, encoding);
            entryCount = countSnippets(output);

            m_options.setStatus(m_options.ANALYZED);
            m_options.setExpectedEntryCount(entryCount);
        }
        catch (Throwable e)
        {
            c_logger.error("error reading snipppet file", e);

            m_options.setError("Error analyzing snipppet file: " +
                e.getMessage());
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
            m_thread = new SnippetFileReaderThread(m_results, m_options);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        m_thread = null;
    }

    public int countSnippets(Output p_output)
    {
        int result = 0;
        int openSnippets = 0;

        for (Iterator it = p_output.documentElementIterator(); it.hasNext(); )
        {
            DocumentElement element = (DocumentElement)it.next();

            if (element instanceof GsaStartElement)
            {
                GsaStartElement gs = (GsaStartElement)element;

                // Only count top-level snippets, not embedded ones.
                if (openSnippets == 0)
                {
                    result++;
                }

                if (!gs.getEmpty())
                {
                    ++openSnippets;
                }
            }
            else if (element instanceof GsaEndElement)
            {
                --openSnippets;
            }
        }

        return result;
    }
}
