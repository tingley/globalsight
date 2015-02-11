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

import com.globalsight.everest.projecthandler.Project;

import com.globalsight.importer.IReader;
import com.globalsight.importer.ImporterException;
import com.globalsight.importer.ImportOptions;
import com.globalsight.util.IntHolder;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.globalsight.util.UTC;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.xml.sax.InputSource;

import java.sql.Timestamp;

import java.io.*;
import java.util.*;

/**
 * Reads TMX files and produces TU objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class XmlReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            XmlReaderThread.class);

    private ReaderResultQueue m_results;
    private ImportOptions m_options;
    private /*TODO*/Object m_database;
    private int m_count = 0;
    private ReaderResult m_result = null;

    //
    // Constructor
    //
    public XmlReaderThread (ReaderResultQueue p_queue,
        ImportOptions p_options, /*TODO*/Object p_database)
    {
        m_results = p_queue;
        m_options = p_options;
        m_database = p_database;

        this.setName("Xml Reader Thread");
    }

    //
    // Thread methods
    //
    public void run()
    {
        try
        {
            SAXReader reader = new SAXReader();

            // TODO: Read the DTD and validate.
            // See com.globalsight.everest.tm.util.DtdResolver;

            // reader.setEntityResolver(DtdResolver.getInstance());
            // reader.setValidation(true);

            reader.addHandler("/projectdata",
                new ElementHandler()
                    {
                        public void onStart(ElementPath path)
                        {
                            Element element = path.getCurrent();
                        }

                        public void onEnd(ElementPath path)
                        {
                        }
                    }
                );

            // enable pruning to call me back as each Element is complete
            reader.addHandler("/projectdata/data",
                new ElementHandler ()
                    {
                        public void onStart(ElementPath path)
                        {
                            m_count++;
                        }

                        public void onEnd(ElementPath path)
                        {
                            Element element = path.getCurrent();

                            // prune the current element to reduce memory
                            element.detach();

                            m_result = m_results.hireResult();

                            try
                            {
                                // TODO: Create data objects
                                Object o = /*createObject*/(element);

                                if (CATEGORY.isDebugEnabled())
                                {
                                    CATEGORY.debug(o);
                                }

                                m_result.setResultObject(o);
                            }
                            catch (Throwable ex)
                            {
                                m_result.setError(ex.toString());

                                CATEGORY.warn("Error in object " + m_count, ex);
                            }

                            boolean done = m_results.put(m_result);
                            m_result = null;

                            // Stop reading the file.
                            if (done)
                            {
                                throw new ThreadDeath();
                            }
                        }
                    }
                );

            String url = m_options.getFileName();

            Document document = reader.read(url);
        }
        catch (ThreadDeath ignore)
        {
            CATEGORY.info("ReaderThread: interrupted");
        }
        catch (Throwable ignore)
        {
            // Should never happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            CATEGORY.error("unexpected error", ignore);
        }
        finally
        {
            if (m_result != null)
            {
                m_results.fireResult(m_result);
            }

            m_results.producerDone();
            m_results = null;

            CATEGORY.debug("ReaderThread: done.");
        }
    }

    //
    // Private Methods
    //

}
