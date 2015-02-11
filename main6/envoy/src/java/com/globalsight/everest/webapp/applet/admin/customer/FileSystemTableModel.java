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
package com.globalsight.everest.webapp.applet.admin.customer;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.event.TableModelEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Locale;
import java.util.TimeZone;
import java.io.File;
import java.io.Serializable;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.util.comparator.FileComparator;
import com.globalsight.everest.webapp.applet.common.EnvoyJTable;


/**
 * The FileSystemTableModel is an extension of the AbstractTableModel
 * which is used as a model for the File's table.  It also serves as
 * a column listener for table. 
 */
public class FileSystemTableModel 
            extends AbstractTableModel 
            implements Serializable
{
    // The name for each column.
    private static String[] m_columnNames = {};    
    // A list that contains a collection of rows.
    private File[] m_files = null;    
    // the sorted column
    private int m_sortCol = 0;
    // determines whether the sorting is done in ascending order
    private boolean m_sortAsc = true;

    private FileComparator m_fileComparator = null;
    private Timestamp m_timestamp = null;

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Construct a file system table model.
    */
    public FileSystemTableModel(String[] p_columnNames,
                                TimeZone p_userTimeZone,
                                Locale p_userLocale)
    {
        m_columnNames = p_columnNames;
        m_timestamp = new Timestamp(Timestamp.DEFAULT_DATE_STYLE, 
                                    Timestamp.DEFAULT_DATE_STYLE, 
                                    Timestamp.TIME_AND_DATE, 
                                    p_userTimeZone);
        m_timestamp.setLocale(p_userLocale);

        m_fileComparator = new FileComparator(
            m_sortCol, p_userLocale, m_sortAsc);
    }

    /**
     * Get the table's column event handling object.
     * @param p_table - The table that should listen to a mouse click on it's column header.
     * @return The column's event handling object used for sorting table.
     */
    public ColumnListener getColumnListener(EnvoyJTable p_table)
    {
        return new ColumnListener(p_table);
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  Begin: TableModel Implementation
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Get the number of columns managed by the data source object.
    * This method is used by JTable in order to determine how many columns
    * should be created and displayed on initialization.
    * @return The number of columns to be displayed.
    */
    public int getColumnCount()
    {
        return m_columnNames.length;
    }

    /**
     * Get the name of the column at columnIndex. This is used to initialize
     * the table's column header name. Note, this name does not need to be
     * unique. Two columns on a table can have the same name.
     * @param p_column - The index of the column for which the name should be returned.
     * @return The column name.
     */
    public String getColumnName(int p_column)
    {
        String str = m_columnNames[p_column] == null ? 
                     "" : m_columnNames[p_column];
        if (p_column == m_sortCol)
            str += m_sortAsc ? " >>" : " <<";

        return str;
    }

    /**
    * Get the number of records managed by the data source object.
    * This method is used by JTable in order to determine how many rows
    * should be created and displayed. This method should be quick, as it is called
    * by JTable quite frequently.
    * @return The number of columns to be displayed.
    */
    public int getRowCount()
    {
        return m_files == null ? 0 : m_files.length;
    }

    /**
    * Get an attribute value for the cell at the specified column and row.
    * @param p_row - The row whose value is to be looked up.
    * @param p_column - The column whose value is to be looked up.
    * @return The value of the cell based on a specified column and row.
    */
    public Object getValueAt(int p_row, int p_column)
    {
        if (m_files == null)
        {
            return null;
        }
        try
        {
            if (p_column == 0)
            {
                //return m_files[p_row].getName();
                return m_files[p_row];
            }
            else if (p_column == 1)
            {
                if (m_files[p_row].isDirectory())
                {
                    return "";
                }
                else
                {
                    // Get the file size and format it
                    long size = m_files[p_row].length();
                    if (size > 1024)
                    {
                        long r = size%1024;
                        size = ((r != 0) ? ((size/1024)+1) : (size/1024));
                    }
                    else
                    {
                        // if it's less than or equals to 1024, it's 1KB
                        size = 1;
                    }

                    return size + "KB";                        
                }
            }
            else if (p_column == 2)
            {
                m_timestamp.setTimeInMillis(
                    m_files[p_row].lastModified());
                return m_timestamp.toString();
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            return "";
        }
    }
    /**
    * Determines whether the cell at a specified row and column is editable.
    * @return True if the cell is editable. Otherwise, setValueAt() on the 
    *         cell will not change the value of that cell.
    */
    public boolean isCellEditable(int p_row, int p_column)
    {
        return false;        
    }       
    //////////////////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////////////////



    //////////////////////////////////////////////////////////////////////////////////
    //  Begin: Local Methods
    //////////////////////////////////////////////////////////////////////////////////
    /**
     * Update the table model based on the given file array.
     */
    public void updateTableModel(File[] p_files)
    {

        int size = p_files == null ? -1 : p_files.length;

        if (size > 0)
        {
            m_files = p_files;

            // now sort the table based on the specifiec column index.
            java.util.Arrays.sort(m_files, m_fileComparator);                
        }
        else
        {
            m_files = null;
        }
        // inform the listeners about the table change
        fireTableChanged(null);
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  Begin: Inner Class
    //////////////////////////////////////////////////////////////////////////////////
    /*
    * An inner class that extends MouseAdapter and is used as a listener 
    * to the table's column heading for sorting purposes.
    */
    class ColumnListener extends MouseAdapter
    {
        protected EnvoyJTable m_table;

        public ColumnListener(EnvoyJTable p_table) 
        {
            m_table = p_table;
        }

        // a mouse click on the column
        public void mouseClicked(MouseEvent e) 
        {
            // get the index of the column that was clicked on.
            TableColumnModel colModel = m_table.getColumnModel();
            int columnModelIndex = 
                    colModel.getColumnIndexAtX(e.getX());

            int modelIndex = colModel.getColumn(
                columnModelIndex).getModelIndex();

            // make sure the column index is valid.
            if (modelIndex < 0)
                return;

            // if the clicked column index is the same as the one 
            // that was already sorted, reverse sorting.
            if (m_sortCol == modelIndex)
            {
                m_sortAsc = !m_sortAsc;
                m_fileComparator.reverseSortingOrder();
            }
            else // set the sorted column to be the one that is being clicked
            {
                m_sortCol = modelIndex;
                m_fileComparator.setSortColumn(m_sortCol);
            }

            // refresh the column heading names (to include >> 
            // or << in the sorted column header)
            for (int i=0; i < m_columnNames.length; i++)
            {
                TableColumn column = colModel.getColumn(i);                
                column.setHeaderValue(getColumnName(
                    column.getModelIndex()));
            }
            m_table.getTableHeader().repaint();  

            // now sort the table based on the specifiec column index.
            java.util.Arrays.sort(m_files, m_fileComparator);

            // send a table changed event to let the table know 
            // about the sorting (change in the model).
            m_table.tableChanged(new TableModelEvent(
                FileSystemTableModel.this));
            m_table.clearSelection();
            m_table.repaint(); 
        }
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Inner Class
    //////////////////////////////////////////////////////////////////////
}
