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

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.Entry;

import com.globalsight.importer.ImportOptions;

import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;


import org.dom4j.*;
import org.dom4j.io.SAXReader;

/**
 * Reads GTXML files and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class GTXmlReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            GTXmlReaderThread.class);

    private ReaderResultQueue m_results;
    private ReaderResult m_result = null;
    private ImportOptions m_options;
    private Termbase m_termbase;
    private DocumentFactory m_factory;

    //
    // Constructor
    //
    public GTXmlReaderThread (ReaderResultQueue p_queue,
        ImportOptions p_options, Termbase p_termbase)
    {
        m_results = p_queue;
        m_options = p_options;
        m_termbase = p_termbase;
        m_factory = new DocumentFactory();
    }

    //
    // Thread methods
    //
    public void run()
    {
        try
        {
            SAXReader reader = new SAXReader();
            reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

            // enable pruning to call me back as each Element is complete
            reader.addHandler("/entries/conceptGrp",
                new ElementHandler ()
                    {
                        public void onStart(ElementPath path)
                        {
                        }

                        public void onEnd(ElementPath path)
                        {
                            Element element = path.getCurrent();

                            // prune the current element to reduce memory
                            element.detach();

                            Document doc = m_factory.createDocument(element);
                            Entry entry = new Entry(doc);

                            m_result = m_results.hireResult();
                            m_result.setResultObject(entry);

                            boolean done = m_results.put(m_result);
                            m_result = null;

                            // Stop reading the TMX file.
                            if (done)
                            {
                                throw new ThreadDeath();
                            }
                        }
                    }
                );

            String url = m_options.getFileName();

            reader.read(url);
        }
        catch (ThreadDeath ignore)
        {
            CATEGORY.info("ReaderThread: interrupted.");
        }
        catch (Throwable ignore)
        {
            // Should never happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
        }
        finally
        {
            if (m_result != null)
            {
                m_results.fireResult(m_result);
                m_result = null;
            }

            m_results.producerDone();
            m_results = null;

            CATEGORY.debug("ReaderThread: done.");
        }
    }
}


