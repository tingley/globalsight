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
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import java.io.*;
import java.util.ArrayList;
import jxl.*;

public class ExcelReader
	implements IReader, TermbaseExceptionMessages
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExcelReader.class);

    //
    // Private Member Variables
    //
    private Termbase m_termbase;
    private ImportOptions m_options;
    private int m_entryCount;

    private ExcelReaderThread m_thread = null;
    private ReaderResultQueue m_results = null;
    private ReaderResult m_result;

    //
    // Constructors
    //

    public ExcelReader (ImportOptions p_options, Termbase p_termbase)
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
     * of Excel files.
     */
    public ImportOptions analyze()
    {
        m_entryCount = 0;

        try
        {
            analyzeFile(m_options.getFileName());

            m_options.setStatus(m_options.ANALYZED);
            m_options.setExpectedEntryCount(m_entryCount);
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
            m_thread = new ExcelReaderThread(m_results, m_options, m_termbase);
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
     * Reads a Excel file and checks the columns. If there's any error
     * in the file, an exception with a descriptive message is thrown.
     */
    private void analyzeFile(String p_url)
        throws Exception
    {
        //analyzeColumns(p_url);
        //countEntry(p_url);
                
        InputStream is = null;
        Workbook readBook = null;
        Sheet readSheet = null;
        int entryCount = 0;
        ArrayList columnDescriptors = null;
        try
        {
        	com.globalsight.terminology.importer.ImportOptions options =
        		(com.globalsight.terminology.importer.ImportOptions)m_options;
        	
            is = new FileInputStream(p_url);
            readBook = Workbook.getWorkbook(is);
            Sheet[] sheets = readBook.getSheets();
            
            for (int i = 0; i < sheets.length; i++)
            {
            	readSheet = readBook.getSheet(i);
                int rowsInSheet = readSheet.getRows();
                int entryStartRow = ImportUtil.determineStartRow(readSheet);
                int headerNumber = ImportUtil.findHeaderRowNumber(readSheet, entryStartRow, m_termbase);
                if (headerNumber < 0)
                {
                	continue;
                }
                Cell[] cells = readSheet.getRow(headerNumber);
                columnDescriptors = buildColumnDescriptors(cells, i);
                if (columnDescriptors == null || columnDescriptors.size() == 0)
                {
                	return;
                }
                options.putColumnDescriptors(i, columnDescriptors);
                options.putColumnHeaderRow(i, headerNumber);
                
                for (int j = headerNumber + 1; j < rowsInSheet; j++)
                {
                	cells = readSheet.getRow(j);
                	if (!ImportUtil.isEmptyOrIllegalRow(cells,columnDescriptors))
                	{
                		entryCount++;
                	}           	
                } 
              
            }
            if (options.getColumnDescriptors() == null)
            {
            	options.setError("Could not find any legal row descripting " +
            			"column properties.\r\nPlease make sure there is one row indicating " +
            			"the column header, and at least two language columns in this row.\r\nThese " +
            			"languages should be defined in the Terminology you are importing.\r\nGlobalSight " +
            			"will import term entries from this header row(exclusive)");
            	return;
            }          
            m_entryCount = entryCount;
        }
        catch (IOException ex)
        {
            throw new Exception("Error reading file (" +
                ex.getMessage() + ")");
        }
        catch (Exception ex)
        {
            CATEGORY.error("Error analyzing import file", ex);
            throw new Exception("Error reading file (" +
                ex.getMessage() + ")");
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Exception ignore) {}
            }
        }
    }

    
    private  ArrayList buildColumnDescriptors(Cell[] p_cells, int p_sheetNumber)
    {
    	 com.globalsight.terminology.importer.ImportOptions options =
    		 (com.globalsight.terminology.importer.ImportOptions)m_options;
    	 ArrayList columnDescriptors = new ArrayList();
    	 ColumnDescriptor col = null;
         for (int i = 0; i < p_cells.length; ++i)
         {
             col = options.createColumnDescriptor();
             if (!ImportUtil.isEmptyCell(p_cells[i]))
             {
            	 col.m_position = i;
            	 col.m_name = p_cells[i].getContents().trim();
            	 if (ImportUtil.isLanguageDefined(m_termbase, col.m_name))
            	 {
            		 col.m_termLanguage = ImportUtil.mapTermbaseDefinedLang(m_termbase, col.m_name);
            		 col.m_type = "term";
            	 }
            	 else if (ImportUtil.isTypeDefined(col.m_name))
            	 {
            		 col.m_termLanguage = "unknown";
            		 col.m_type = col.m_name.toLowerCase();
            		 if (col.m_type.equals("term"))
            		 {
            			 col.m_type = "skip";
            		 }
            	 }
            	 else
            	 {
            		 options.setError("Sheet " + p_sheetNumber + ": column " + i + 
            				 ", the column name: " + col.m_name + " is neither a language defined " +
            				 		"in the termbase you are importing nor any type GlobalSight can recognise. if you want to " +
            				 		"import it as term column, please make sure the header is a language defined in the termbase " +
            				 		"you are importing.");
            		 return null;
            	 }
                 col.m_associatedColumn = "" + ImportUtil.getAssociatedColumn(col.m_name, columnDescriptors);
                 col.m_encoding = "unknown";                
                 columnDescriptors.add(col);
             }        

         }
         return columnDescriptors;
    }   
    
}
