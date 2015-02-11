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

import com.globalsight.terminology.importer.ImportUtil;
import com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.Entry;
import com.globalsight.importer.ImportOptions;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import org.dom4j.*;
import java.util.*;
import java.io.*;

import jxl.*;

/**
 * Reads Excel files and produces Entry objects by putting ReaderResult
 * objects into a ReaderResultQueue.
 */
public class ExcelReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExcelReaderThread.class);

    private ReaderResultQueue m_results;
    private ReaderResult m_result = null;
    private ImportOptions m_options;
    private Termbase m_termbase;

    private DocumentFactory m_factory = DocumentFactory.getInstance();
    
    private static final int MAX_EMPTY_ROW = 5;

    //
    // Constructor
    //
    public ExcelReaderThread (ReaderResultQueue p_q, ImportOptions p_options,
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
            com.globalsight.terminology.importer.ImportOptions options =
                (com.globalsight.terminology.importer.ImportOptions)m_options;
            if (options.getColumnHeaderRow() == null)
            {
            	return;
            }
            String url = options.getFileName();
            InputStream is = new FileInputStream(url);
            WorkbookSettings setting = new WorkbookSettings();
            setting.setEncoding("ISO-8859-1");
            Workbook readBook = Workbook.getWorkbook(is, setting);
            Sheet[] sheets = readBook.getSheets();
            Sheet readSheet = null;
            Cell[] cells = null;
            for (int i = 0; i < sheets.length; i++)
            {
            	readSheet = sheets[i];
            	int rowsInSheet = readSheet.getRows();
            	int headerRowNumber = options.getColumnHeaderRow(i);
            	if (headerRowNumber < 0)
            	{
            		continue;
            	}
            	
            	List columnDescriptors = options.getColumnDescriptors(i);
            	int emptyRow = 0;
            	
                for (int j = headerRowNumber + 1; j < rowsInSheet; j++)
                {
                	m_result = m_results.hireResult();
                	cells = readSheet.getRow(j);
                	if (ImportUtil.isEmptyOrIllegalRow(cells, columnDescriptors))
                	{
                        // m_result.setError("sheet " + (i + 1) + " row " + (j +
                        // 1) +
                        // ": should at least contain two term columns; ignoring
                        // row");
	            		 CATEGORY.info("sheet " + (i + 1) + " row " + (j + 1) +
                               ": should at least contain two term columns; ignoring row");
	            		 
	            		 emptyRow++;
	            		 
	            		 if (emptyRow > MAX_EMPTY_ROW)
	            		 {
	            		     break;
	            		 }
	            		 
                		continue;
                	}

                	emptyRow = 0;                	
                	
                    Document dom = buildEntry(cells, columnDescriptors);
                    Entry entry = new Entry(dom);

                    m_result.setResultObject(entry);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug(entry.getXml());
                    }

                    boolean done = m_results.put(m_result);
                    m_result = null;

                    // Stop reading file.
                    if (done)
                    {
                        throw new ThreadDeath();
                    }

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
     * <p>Converts a list of column values into an Entry.</p>
     *
     * <p>Algorithmic note: columns are managed in a priority queue.
     * We retrieve the first column and try to map it to the entry.
     * If we can't because the column depends on a later column
     * (associatedColumn field), we add it to the end of the queue.
     * Rinse and repeat until the queue is empty.</p>
     */
    private Document buildEntry(Cell[] p_cells, List p_colDescriptors)
        throws TermbaseException,
               Exception
    {
        Document result = m_factory.createDocument();
        Element root = result.addElement("conceptGrp");
        ArrayList columnDes = new ArrayList(p_colDescriptors);

        // Array that holds the elements as we construct them.
        Element[] nodes = new Element[p_cells.length];
        Element node;

        // An entry must have at least one term to be stored; check this.
        int termCount = 0;

        int index;
        String value;

        for (int i = 0; i < columnDes.size(); i++)
        {
            ColumnDescriptor col = (ColumnDescriptor)columnDes.get(i);

            index = col.m_position;
            if (index >= p_cells.length)
            {
            	continue;
            }
            
            value = p_cells[index].getContents().trim();

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
                int ac = Integer.parseInt(col.m_associatedColumn);
                if (ac < 0)
                {
                	continue;
                }
                
                Element otherNode = nodes[ac];
                if (otherNode == null)
                {
                    if (!isSkippedColumn(ac))
                    {
                        // The column to which this column belongs has
                        // not been processed yet, but it will be
                        // processed eventually.  Put this column at
                        // the end of the queue to be processed later.
                    	columnDes.add(col);
                    }

                    continue;
                }

                // Now we know which column to associate this column with.
                //String type = col.m_type.substring("term".length());

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
                int ac = Integer.parseInt(col.m_associatedColumn);
                if (ac < 0)
                {
                	continue;
                }
                
                Element termNode = nodes[ac];
                if (termNode == null)
                {
                    if (!isSkippedColumn(ac))
                    {
                        // Term column to which this column belongs
                        // has not been processed yet, but it will be
                        // processed eventually. Put this column at
                        // the end of the list to be processed later.
                    	columnDes.add(col);
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
        ColumnDescriptor col = (ColumnDescriptor)
            ((com.globalsight.terminology.importer.ImportOptions)m_options).
            getColumns().get(index);

        return col.m_type.equals("skip");
    }

    private void addTermGrp(Element p_root, Element p_term, String p_language)
        throws TermbaseException
    {
        Element langGrp = (Element)p_root.selectSingleNode(
            "//languageGrp[language/@name='" + p_language + "']");

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
        Element grp  = m_factory.createElement("termGrp");
        Element node = grp.addElement("term").addText(p_value);
        return grp;
    }

    private Element buildDescripGrp(String p_value, String p_type)
    {
        Element grp  = m_factory.createElement("descripGrp");
        Element node = grp.addElement("descrip").addText(p_value);
        node.addAttribute("type", p_type);
        return grp;
    }

    private Element buildSourceGrp(String p_value)
    {
        Element grp  = m_factory.createElement("sourceGrp");
        Element node = grp.addElement("source").addText(p_value);
        return grp;
    }

    private Element buildLanguageGrp(String p_language)
        throws TermbaseException
    {
        String locale = m_termbase.getLocaleByLanguage(p_language);

        Element grp  = m_factory.createElement("languageGrp");
        Element node = grp.addElement("language");
        node.addAttribute("name", p_language);
        node.addAttribute("locale", locale);
        return grp;
    }

}
