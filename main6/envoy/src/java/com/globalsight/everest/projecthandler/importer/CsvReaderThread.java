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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.DocumentFactory;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.importer.ImportOptions;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.sun.org.apache.regexp.internal.RE;

/**
 * Reads CSV files and produces Entry objects by putting ReaderResult objects
 * into a ReaderResultQueue.
 */
public class CsvReaderThread extends Thread
{
    private static final Logger CATEGORY = Logger
            .getLogger(CsvReaderThread.class.getName());

    private ReaderResultQueue m_results;
    private ReaderResult m_result;
    private ImportOptions m_options;
    private Project m_project;

    private DocumentFactory m_factory = DocumentFactory.getInstance();

    //
    // Constructor
    //
    public CsvReaderThread(ReaderResultQueue p_queue, ImportOptions p_options,
            Project p_project)
    {
        m_results = p_queue;
        m_options = p_options;
        m_project = p_project;

        this.setName("Project Data Reader Thread");
    }

    //
    // Thread methods
    //
    public void run()
    {
        try
        {
            String url = m_options.getFileName();
            LineNumberReader reader = getReader(url);

            CATEGORY.debug("Reader thread: start reading " + url);

            int expectedColumns = getColumnCount(m_options);
            String separator = m_options.getSeparator();

            // Build a regexp from the separator char and be careful
            // to protect special chars like '|' (make them "\|").
            RE regexp = ImportUtil.getDelimiterRegexp(separator);

            String line;

            // skip header line if requested
            if (m_options.isIgnoreHeader())
            {
                do
                {
                    line = reader.readLine();
                } while (line != null && ImportUtil.isEmptyLine(line));
            }

            HashMap map = new HashMap();
            // then read and process all other lines
            while ((line = reader.readLine()) != null)
            {
                m_result = m_results.hireResult();

                try
                {
                    if (ImportUtil.isEmptyLine(line))
                    {
                        continue;
                    }

                    String[] columns = regexp.split(line + " ");

                    for (int i = 0; i < columns.length; ++i)
                    {
                        columns[i] = columns[i].trim();
                    }

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("line " + reader.getLineNumber() + " ("
                                + columns.length + " columns) `" + line + "'");
                    }

                    // Sanity check of column count
                    if (columns.length != expectedColumns)
                    {
                        m_result.setError("line " + reader.getLineNumber()
                                + ": expected " + expectedColumns
                                + " columns, " + "found only " + columns.length
                                + "; ignoring line");

                        CATEGORY.error(m_result.getMessage());
                    }
                    else
                    {
                        Entry entry = buildEntry(columns);

                        if (CATEGORY.isDebugEnabled())
                        {
                            CATEGORY.debug("Entry = " + entry);
                        }

                        m_result.setResultObject(entry);
                    }
                }
                catch (Exception ex)
                {
                    m_result.setError("line " + reader.getLineNumber() + ": "
                            + ex.getMessage());
                }

                boolean done = m_results.put(m_result);
                m_result = null;

                // Stop reading the input file.
                if (done)
                {
                    throw new ThreadDeath();
                }
            }
        }
        catch (ThreadDeath ignore)
        {
            CATEGORY.info("ReaderThread: interrupted");
        }
        catch (Throwable ex)
        {
            // Should never happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            CATEGORY.error("unexpected error", ex);
        }
        finally
        {
            if (m_result != null)
            {
                m_results.fireResult(m_result);
            }

            m_results.producerDone();
            m_results = null;
        }

        CATEGORY.debug("Reader thread: done reading");
    }

    /**
     * <p>
     * Converts a list of column values into an Entry.
     * </p>
     */
    private Entry buildEntry(String[] p_columns) throws Exception
    {
        com.globalsight.everest.projecthandler.importer.ImportOptions options = (com.globalsight.everest.projecthandler.importer.ImportOptions) m_options;

        Set userIds = m_project.getUserIds();
        ArrayList descriptors = options.getColumns();
        Entry entry = new Entry();
        boolean isInvalid = false;
        // Iterate all columns in the current CSV line.
        for (int i = 0; (!isInvalid && i < p_columns.length); i++)
        {
            String col = p_columns[i];

            com.globalsight.everest.projecthandler.importer.ImportOptions.ColumnDescriptor desc = (com.globalsight.everest.projecthandler.importer.ImportOptions.ColumnDescriptor) descriptors
                    .get(i);

            if (desc.m_type.equals("skip"))
            {
                continue;
            }
            // TODO: implement column types, convert values (dates)
            // and put in a result object.
            else if (desc.m_type.equals("username"))
            {
                if (userIds.contains(col))
                {
                    entry.setUsername(col);
                }
                else
                {
                    isInvalid = true;
                }
            }
            else if (desc.m_type.equals("activityname"))
            {
                entry.setSubject(col);
            }
            else if (desc.m_type.equals("activitytype"))
            {
                entry.setActivityType(col);
            }
            else if (desc.m_type.equals("startdate"))
            {
                entry.setStartDate(col);
                entry.setStartDateFormatType(desc.m_subtype);
            }
            else if (desc.m_type.equals("enddate"))
            {
                entry.setEndDate(col);
                entry.setEndDateFormatType(desc.m_subtype);
            }
            else
            {
                throw new Exception("invalid column descriptor " + desc.m_type);
            }
        }

        return entry;
    }

    //
    // Helper Methods
    //

    private LineNumberReader getReader(String p_url) throws IOException
    {
        String encoding = m_options.getEncoding();

        if (encoding == null || encoding.length() == 0)
        {
            encoding = "windows-1252";
        }
        encoding = CodesetMapper.getJavaEncoding(encoding);

        return new LineNumberReader(new InputStreamReader(new FileInputStream(
                p_url), encoding));
    }

    /** Returns the number of columns defined for the import file. */
    private int getColumnCount(ImportOptions p_options)
    {
        return ((com.globalsight.everest.projecthandler.importer.ImportOptions) p_options)
                .getColumns().size();
    }
}
