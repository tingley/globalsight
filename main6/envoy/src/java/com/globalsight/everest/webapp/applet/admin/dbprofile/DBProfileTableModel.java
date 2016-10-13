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
package com.globalsight.everest.webapp.applet.admin.dbprofile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;

/**
 * The DBProfileTableModel is an extension of the AbstractTableModel which is
 * used as a model for the DB Column Profile table.
 */
public class DBProfileTableModel extends AbstractTableModel implements
        Serializable
{
    // the names for each column.
    private static String[] m_columnNames =
    {};
    // a map that contains the collection of db column profile arrays.
    private Map<Integer, Object[]> m_dbProfiles = new HashMap<Integer, Object[]>();

    private static final int COLUMN_NAME = 0;
    private static final int COLUMN_LABEL = 1;
    private static final int COLUMN_TABLE = 2;
    private static final int COLUMN_FORMAT = 3;
    private static final int COLUMN_MODE = 4;

    /**
     * Constructs a user role table model.
     */
    public DBProfileTableModel(String[] p_columnNames)
    {
        m_columnNames = p_columnNames;
    }

    public DBProfileTableModel(String[] p_columnNames, List<Object[]> dbProfiles)
    {
        m_columnNames = p_columnNames;
        packData(dbProfiles);
    }

    /**
     * Gets the number of columns managed by the data source object. This method
     * is used by JTable in order to determine how many columns should be
     * created and displayed on initialization.
     * 
     * @return The number of columns to be displayed.
     */
    public int getColumnCount()
    {
        return m_columnNames.length;
    }

    /**
     * Gets the name of the column at columnIndex. This is used to initialize
     * the table's column header name. Note, this name does not need to be
     * unique. Two columns on a table can have the same name.
     * 
     * @param p_column
     *            - The index of the column for which the name should be
     *            returned.
     * @return The column name.
     */
    public String getColumnName(int p_column)
    {
        return m_columnNames[p_column] == null ? "" : m_columnNames[p_column];
    }

    /**
     * Gets the number of records managed by the data source object. This method
     * is used by JTable in order to determine how many rows should be created
     * and displayed. This method should be quick, as it is called by JTable
     * quite frequently.
     * 
     * @return The number of columns to be displayed.
     */
    public int getRowCount()
    {
        return m_dbProfiles == null ? 0 : m_dbProfiles.size();
    }

    /**
     * Gets an attribute value for the cell at the specified column and row.
     * 
     * @param p_row
     *            - The row whose value is to be looked up.
     * @param p_column
     *            - The column whose value is to be looked up.
     * @return The value of the cell based on a specified column and row.
     */
    public Object getValueAt(int p_row, int p_column)
    {
        Object[] dbProfile = m_dbProfiles.get(p_row);
        String value = "";
        switch (p_column)
        {
            case COLUMN_NAME:
                value = ((DatabaseColumnImpl) dbProfile[0]).toString();
                break;
            case COLUMN_LABEL:
                value = (String) dbProfile[1];
                break;
            case COLUMN_TABLE:
                value = (String) dbProfile[2];
                break;
            case COLUMN_FORMAT:
                value = (String) dbProfile[3];
                break;
            case COLUMN_MODE:
                value = (String) dbProfile[4];
                break;
            default:
                value = "N/A"; // indicate a problem here
                break;
        }

        return value;
    }

    /**
     * Determines whether the cell at a specified row and column is editable.
     * 
     * @return True if the cell is editable. Otherwise, setValueAt() on the cell
     *         will not change the value of that cell.
     */
    public boolean isCellEditable(int p_row, int p_column)
    {
        return false;
    }

    /**
     * Updates the table model based on the given db profile info.
     */
    public void updateTableModel(List<Object[]> dbProfiles)
    {
        m_dbProfiles.clear();
        packData(dbProfiles);
        // inform the listeners about the table change
        fireTableChanged(null);
    }

    /**
     * Updates the table model based on the given db profile info.
     */
    public void updateTableModel(Map<Integer, Object[]> dbProfiles)
    {
        m_dbProfiles = dbProfiles;
        // inform the listeners about the table change
        fireTableChanged(null);
    }

    /**
     * Re-constructs the profiles according to row id mapping.
     * 
     * @param dbProfiles
     *            - the db profiles.
     */
    private void packData(List<Object[]> dbProfiles)
    {
        if (dbProfiles != null && !dbProfiles.isEmpty())
        {
            for (int i = 0; i < dbProfiles.size(); i++)
            {
                m_dbProfiles.put(i, dbProfiles.get(i));
            }
        }
    }

    /**
     * Gets the db column profile information in the table model.
     */
    public Map<Integer, Object[]> getDBProfiles()
    {
        return m_dbProfiles;
    }

    /**
     * Moves the selected row up.
     * 
     * @param selectedRow
     *            - the selected row.
     */
    public void moveUp(int selectedRow)
    {
        Object[] rowUp = m_dbProfiles.get(selectedRow - 1);
        m_dbProfiles.put(selectedRow - 1, m_dbProfiles.get(selectedRow));
        m_dbProfiles.put(selectedRow, rowUp);
    }

    /**
     * Moves the selected row down.
     * 
     * @param selectedRow
     *            - the selected row.
     */
    public void moveDown(int selectedRow)
    {
        Object[] rowDown = m_dbProfiles.get(selectedRow + 1);
        m_dbProfiles.put(selectedRow + 1, m_dbProfiles.get(selectedRow));
        m_dbProfiles.put(selectedRow, rowDown);
    }
}
