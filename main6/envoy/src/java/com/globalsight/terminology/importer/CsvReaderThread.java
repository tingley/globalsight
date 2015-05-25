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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.globalsight.importer.ImportOptions;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.globalsight.util.StringUtil;
import com.sun.org.apache.regexp.internal.RE;

/**
 * Reads CSV files and produces Entry objects by putting ReaderResult objects
 * into a ReaderResultQueue.
 */
public class CsvReaderThread extends Thread
{
    private static final Logger CATEGORY = Logger
            .getLogger(CsvReaderThread.class);

    private ReaderResultQueue m_results;
    private ReaderResult m_result = null;
    private ImportOptions m_options;
    private Termbase m_termbase;

    private DocumentFactory m_factory = DocumentFactory.getInstance();

    //
    // Constructor
    //
    public CsvReaderThread(ReaderResultQueue p_q, ImportOptions p_options,
            Termbase p_termbase)
    {
        m_results = p_q;
        m_options = p_options;
        m_termbase = p_termbase;
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

            CATEGORY.info("Reader thread: start reading " + url);

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

            boolean isFirstLine = true;

            // Added for encoding comma within quoted string
            CsvReaderUtil csvRUtil = new CsvReaderUtil();
            csvRUtil.setColumnChooseList(m_options);
            boolean _IfEncode = false;
            List columnChooseList = csvRUtil.getColumnChooseList();

            // then read and process all other lines
            while ((line = reader.readLine()) != null)
            {
                if (isFirstLine)
                {
                    String encoding = m_options.getEncoding();

                    if (encoding != null && encoding.length() > 0)
                    {
                        encoding = CodesetMapper.getJavaEncoding(encoding);
                        line = StringUtil.removeBom(line, encoding);
                    }

                    isFirstLine = false;
                }

                m_result = m_results.hireResult();

                try
                {
                    if (ImportUtil.isEmptyLine(line))
                    {
                        continue;
                    }

                    _IfEncode = csvRUtil.ifEncodeForCSV(line);
                    if (_IfEncode)
                    {
                        line = csvRUtil.encodeForCSV(line);
                    }

                    String[] columns = regexp.split(line + " ");

                    for (int i = 0, max = columns.length; i < max; ++i)
                    {
                        // columns[i] = columns[i].trim();
                        columns[i] = csvRUtil.csvDelQuotation(columns[i]);
                    }

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("line " + reader.getLineNumber() + " ("
                                + columns.length + " columns) `" + line + "'");
                    }

                    // Sanity check of column count
                    // if (columns.length != expectedColumns)
                    if (checkColumns(columnChooseList, columns))
                    {
                        /*
                         * m_result.setError("line " + reader.getLineNumber() +
                         * ": expected " + expectedColumns + " columns, " +
                         * "found " + columns.length + "; ignoring line");
                         */

                        m_result.setError("line " + reader.getLineNumber()
                                + ": Found Invalid Data Issue"
                                + "; ignoring line");

                        CATEGORY.warn(m_result.getMessage());

                        boolean done = m_results.put(m_result);
                        m_result = null;

                        // Stop reading the TMX file.
                        if (done)
                        {
                            throw new ThreadDeath();
                        }

                        continue;
                    }

                    if (_IfEncode)
                    {
                        columns = csvRUtil.decodeForCSV(columns);
                    }

                    Document dom = buildEntry(columns);
                    Entry entry = new Entry(dom);

                    m_result.setResultObject(entry);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug(entry.getXml());
                    }
                }
                catch (Exception ex)
                {
                    m_result.setError("line " + reader.getLineNumber() + ": "
                            + ex.getMessage());
                }

                boolean done = m_results.put(m_result);
                m_result = null;

                // Stop reading the TMX file.
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
                m_result = null;
            }

            m_results.producerDone();
            m_results = null;

            CATEGORY.debug("ReaderThread: done");
        }
    }

    /**
     * <p>
     * Converts a list of column values into an Entry.
     * </p>
     *
     * <p>
     * Algorithmic note: columns are managed in a priority queue. We retrieve
     * the first column and try to map it to the entry. If we can't because the
     * column depends on a later column (associatedColumn field), we add it to
     * the end of the queue. Rinse and repeat until the queue is empty.
     * </p>
     */
    private Document buildEntry(String[] p_columns) throws TermbaseException,
            Exception
    {
        com.globalsight.terminology.importer.ImportOptions options = (com.globalsight.terminology.importer.ImportOptions) m_options;

        Document result = m_factory.createDocument();
        Element root = result.addElement("conceptGrp");

        // Array that holds the elements as we construct them.
        Element[] nodes = new Element[p_columns.length];
        Element node;

        // An entry must have at least one term to be stored; check this.
        int termCount = 0;

        int index;
        String value;

        // Make copy of column descriptions so we can reorder.
        ArrayList queue = new ArrayList(options.getColumns());

        while (!queue.isEmpty())
        {
            ColumnDescriptor col = (ColumnDescriptor) queue.remove(0);

            index = col.m_position;

            // added for ArrayIndexOutOfBoundsException
            if (index >= p_columns.length || index < 0)
                value = "";
            else
                value = p_columns[index];

            if (col.m_type.equals("term"))
            {
                // build the node and add it to the array so later
                // nodes associated with it can find it (prevent
                // endless loop)
                node = buildTermGrp(value);
                nodes[index] = node;

                // only add termGrp to entry if term is non-empty
                if (value.length() > 0)
                {
                    ++termCount;
                    addTermGrp(root, node, col.m_termLanguage);
                }
                else
                {
                    continue;
                }
            }
            else if (col.m_type.equals("skip"))
            {
                continue;
            }
            else if (col.m_type.equals("source"))
            {
                int i = Integer.parseInt(col.m_associatedColumn);
                Element otherNode = nodes[i];

                if (otherNode == null)
                {
                    if (!isSkippedColumn(i))
                    {
                        // The column to which this column belongs has
                        // not been processed yet, but it will be
                        // processed eventually. Put this column at
                        // the end of the queue to be processed later.
                        queue.add(col);
                    }

                    continue;
                }

                // Now we know which column to associate this column with.
                String type = col.m_type.substring("term".length());

                node = buildSourceGrp(value);
                nodes[index] = node;

                addSourceGrp(otherNode, node);
            }
            // All concept-related attributes
            else if (col.m_type.startsWith("concept"))
            {
                String type = col.m_type.substring("concept".length());

                node = buildDescripGrp(value, type);
                nodes[index] = node;

                addConceptDescripGrp(root, node);
            }
            // All term-related attributes
            else if (col.m_type.startsWith("term"))
            {
                int i = Integer.parseInt(col.m_associatedColumn);
                Element termNode = nodes[i];

                if (termNode == null)
                {
                    if (!isSkippedColumn(i))
                    {
                        // Term column to which this column belongs
                        // has not been processed yet, but it will be
                        // processed eventually. Put this column at
                        // the end of the list to be processed later.
                        queue.add(col);
                    }

                    continue;
                }

                // Now we know which column to associate this column with.
                String type = col.m_type.substring("term".length());

                node = buildDescripGrp(value, type);
                nodes[index] = node;

                addTermDescripGrp(termNode, node);
            }
            else
            {
                throw new Exception("invalid column descriptor " + col.m_type);
            }
        }

        if (termCount == 0)
        {
            throw new Exception("no terms found, ignoring entry");
        }

        return result;
    }

    private boolean isSkippedColumn(int index)
    {
        ColumnDescriptor col = (ColumnDescriptor) ((com.globalsight.terminology.importer.ImportOptions) m_options)
                .getColumns().get(index);

        return col.m_type.equals("skip");
    }

    private void addTermGrp(Element p_root, Element p_term, String p_language)
            throws TermbaseException
    {
        Element langGrp = (Element) p_root
                .selectSingleNode("//languageGrp[language/@name='" + p_language
                        + "']");

        if (langGrp == null)
        {
            langGrp = buildLanguageGrp(p_language);
            p_root.add(langGrp);
        }

        langGrp.add(p_term);
    }

    private void addConceptDescripGrp(Element p_root, Element p_descrip)
    {
        p_root.add(p_descrip);
    }

    private void addTermDescripGrp(Element p_node, Element p_descrip)
    {
        p_node.add(p_descrip);
    }

    private void addSourceGrp(Element p_node, Element p_source)
    {
        p_node.add(p_source);
    }

    private Element buildTermGrp(String p_value)
    {
        Element grp = m_factory.createElement("termGrp");
        Element node = grp.addElement("term").addText(p_value);
        return grp;
    }

    private Element buildDescripGrp(String p_value, String p_type)
    {
        Element grp = m_factory.createElement("descripGrp");
        Element node = grp.addElement("descrip").addText(p_value);
        node.addAttribute("type", p_type);
        return grp;
    }

    private Element buildSourceGrp(String p_value)
    {
        Element grp = m_factory.createElement("sourceGrp");
        Element node = grp.addElement("source").addText(p_value);
        return grp;
    }

    private Element buildLanguageGrp(String p_language)
            throws TermbaseException
    {
        String locale = m_termbase.getLocaleByLanguage(p_language);

        Element grp = m_factory.createElement("languageGrp");
        Element node = grp.addElement("language");
        node.addAttribute("name", p_language);
        node.addAttribute("locale", locale);
        return grp;
    }

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
        com.globalsight.terminology.importer.ImportOptions options = (com.globalsight.terminology.importer.ImportOptions) p_options;

        return options.getColumns().size();
    }

    public boolean checkColumns(List columnChooseList, String[] columns)
    {
        if (columnChooseList != null && columnChooseList.size() > 0)
        {
            Iterator it = columnChooseList.iterator();
            int col, length = columns.length;
            while (it.hasNext())
            {
                col = (Integer) it.next();
                if (col >= length)// ||"".equals(columns[col])
                {
                    return true;
                }
            }
        }
        return false;
    }
}
