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
package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * The UserRoleTableModel is an extension of the AbstractTableModel which is
 * used as a model for the User Role's table.
 */
public class UserRoleTableModel extends AbstractTableModel implements
        Serializable
{
    // the names for each column.
    private static String[] m_columnNames =
    {};
    // a map that contains the collection of user role arrays.
    private Map<Integer, Object[]> m_userRoles = new HashMap<Integer, Object[]>();

    private static final int COLUMN_FIRSTNAME = 0;
    private static final int COLUMN_LASTNAME = 1;
    private static final int COLUMN_USERNAME = 2;

    /**
     * Constructs a user role table model.
     */
    public UserRoleTableModel(String[] p_columnNames)
    {
        m_columnNames = p_columnNames;
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
        return m_userRoles == null ? 0 : m_userRoles.size();
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
        Object[] userRole = m_userRoles.get(p_row);
        String value = "";
        switch (p_column)
        {
            case COLUMN_FIRSTNAME:
                value = (String) userRole[0];
                break;
            case COLUMN_LASTNAME:
                value = (String) userRole[1];
                break;
            case COLUMN_USERNAME:
                value = (String) userRole[2];
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
     * Updates the table model based on the given user role info.
     */
    public void updateTableModel(List<Object[]> userRoles)
    {
        m_userRoles.clear();
        packData(userRoles);
        // inform the listeners about the table change
        fireTableChanged(null);
    }

    /**
     * Re-constructs the user roles according to row id mapping.
     * 
     * @param userRoles
     *            - the user roles.
     */
    private void packData(List<Object[]> userRoles)
    {
        if (userRoles != null && !userRoles.isEmpty())
        {
            for (int i = 0; i < userRoles.size(); i++)
            {
                m_userRoles.put(i, userRoles.get(i));
            }
        }
    }

    /**
     * Gets the user role information in the table model.
     */
    public Map<Integer, Object[]> getUserRoles()
    {
        return m_userRoles;
    }
}
