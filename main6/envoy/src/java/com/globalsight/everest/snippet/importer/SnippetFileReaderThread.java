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
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.GsaStartElement;
import com.globalsight.ling.docproc.GsaEndElement;
import com.globalsight.ling.docproc.Output;

import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;

import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.snippet.SnippetImpl;
import com.globalsight.everest.snippet.importer.ImportUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

//java
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

/**
 * Reads Snippet files and produces Snippet objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class SnippetFileReaderThread
    extends Thread
{
    private static final Logger c_logger =
        Logger.getLogger(
            SnippetFileReaderThread.class.getName());

    private ReaderResultQueue m_results;
    private ImportOptions m_options;
    private int m_depth;

    //
    // Constructor
    //
    public SnippetFileReaderThread (ReaderResultQueue p_queue,
        ImportOptions p_options)
    {
        m_results = p_queue;
        m_options = p_options;
    }

    //
    // Thread methods
    //
    public void run()
    {
        ReaderResult result = null;

        try
        {
            result = new ReaderResult();

            String url = m_options.getFileName();
            String encoding = m_options.getEncoding();
            int count = 0;

            String name = null;
            String locale = null;
            String description = null;
            String content = null;

            c_logger.info("Snippet importer reading file " + url);

            // Extract the file. This can fail.
            Output output = ImportUtil.extractFile(url, encoding);

            Iterator it = output.documentElementIterator();
            while (it.hasNext())
            {
                DocumentElement item = (DocumentElement)it.next();

                if (item == null || item.type() != DocumentElement.GSA_START)
                {
                    continue;
                }

                result = new ReaderResult();
                ++count;

                try
                {
                    GsaStartElement element = (GsaStartElement)item;

                    name = element.getExtract();
                    description = element.getDescription();
                    locale = element.getLocale();
                    content = printSnippet(element, it);

                    GlobalSightLocale gsLocale = null;
                    if (locale != null && locale.length() > 0)
                    {
                        gsLocale = getGlobalSightLocale(locale);
                    }

                    // Create a new snippet with the name and content
                    // specified.
                    SnippetImpl s = new SnippetImpl(name, description,
                        gsLocale, content);

                    // Add to the result object and put on the queue.
                    result.setResultObject(s);
                }
                catch (Exception ex)
                {
                    c_logger.info("can't interpret snippet", ex);

                    result.setError("Snippet " + count + ": " +
                                    ex.getMessage());
                }

                m_results.put(result);
            }
        }
        catch (Throwable ex)
        {
            // Should never happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            c_logger.error("error reading snipppet file", ex);

            if (result != null)
            {
                result.setError("import error: " + ex.getMessage());
                m_results.put(result);
            }
        }
        finally
        {
            m_results.producerDone();
        }
    }

    /**
     * Iterates the Gxml DocumentElements after a GS Start tag and
     * prints the snippet content with embedded GS tags preserved as
     * Tags.
     */
    private String printSnippet(GsaStartElement p_root, Iterator p_it)
        throws Exception
    {
        if (!p_it.hasNext())
        {
            // error, but GS correctness in Gxml has been validated by
            // extractor already
            return null;
        }

        DocumentElement element = (DocumentElement)p_it.next();
        StringBuffer result = new StringBuffer();

        m_depth = 1;
        printSnippet1(element, p_it, result);

        return result.toString();
    }


    /**
     * Helper function for printSnippet().
     */
    private void printSnippet1(DocumentElement p_element, Iterator p_it,
        StringBuffer p_result)
        throws Exception
    {
        DocumentElement elem = p_element;

        // specifies if this is the first element in the while loop
        boolean isFirstElement = true;

        while (elem != null && m_depth > 0)
        {
            switch (elem.type())
            {
            case DocumentElement.GSA_START:
                GsaStartElement e = (GsaStartElement)elem;

                if (isExtractTag(e))
                {
                    throw new Exception(
                        "snippet can not contain embedded EXTRACT tags");
                }

                if (!e.getEmpty())
                {
                    ++m_depth;
                }

                p_result.append(elem.getText());
                break;

            case DocumentElement.GSA_END:
                --m_depth;
                if (m_depth > 0)
                {
                    p_result.append(elem.getText());
                }
                break;

            case DocumentElement.LOCALIZABLE:
            case DocumentElement.TRANSLATABLE:
                p_result.append(EditUtil.decodeXmlEntities(
                    EditUtil.stripTags(elem.getText())));
                break;

            case DocumentElement.SKELETON:
                // if this is the first element and it only has
                // white space it is skipped.
                // this is extra white space picked up after the <gs extract=..>
                // tag and not something the user placed there for the snippet
                if (!isFirstElement ||
                    elem.getText().trim().length() > 0)
                {
                    p_result.append(EditUtil.decodeXmlEntities(
                        EditUtil.stripTags(elem.getText())));
                }
                break;

            default:
                break;
            }

            if (p_it.hasNext())
            {
                elem = (DocumentElement)p_it.next();
                isFirstElement = false;
            }
            else
            {
                elem = null;
            }
        }
    }

    /**
     * Wraps the code for getting the locale manager and any
     * exceptions.
     */
    private GlobalSightLocale getGlobalSightLocale(String p_locale)
        throws Exception
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocaleByString(p_locale);
        }
        catch (Throwable ex)
        {
            c_logger.error("invalid locale " + p_locale, ex);
            throw new Exception("invalid locale `" + p_locale + "'");
        }
    }

    /**
     * Helper predicate testing whether the GSA Start Tag is an
     * extract tag or not. Should be moved to GsaStartElement.
     */
    private boolean isExtractTag(GsaStartElement p_elem)
    {
        return p_elem.getExtract() != null;
    }

}
