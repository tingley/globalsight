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

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyFonts;

/**
 * This is a header cell renderer for the user role table.
 * 
 */
public class UserRoleTableHeaderCellRenderer extends DefaultTableCellRenderer
{
    /**
     * Constructs a <code>UserRoleTableHeaderCellRenderer</code>.
     * <P>
     * The horizontal text position is set as appropriate to a table header
     * cell, and the opaque property is set to true.
     */
    public UserRoleTableHeaderCellRenderer()
    {
        setHorizontalTextPosition(LEFT);
        setOpaque(true);
    }

    /**
     * Returns the table header cell renderer.
     * 
     * @param table
     *            the <code>JTable</code>.
     * @param value
     *            the value to assign to the header cell
     * @param isSelected
     *            This parameter is ignored.
     * @param hasFocus
     *            This parameter is ignored.
     * @param row
     *            This parameter is ignored.
     * @param column
     *            the column of the header cell to render
     * @return the table header cell renderer
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);

        setFont(EnvoyFonts.getHeaderFont());
        setBackground(getBackground(table, column));
        setForeground(EnvoyAppletConstants.ENVOY_WHITE);
        // setIcon(getIcon(table, column));
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));

        return this;
    }

    /**
     * Overloaded to return a background color suitable to the primary sorted
     * column.
     * 
     * @param table
     *            the <code>JTable</code>.
     * @param column
     *            the column index.
     * @return the background color.
     */
    protected Color getBackground(JTable table, int column)
    {
        SortKey sortKey = getSortKey(table, column);
        if (sortKey != null
                && table.convertColumnIndexToView(sortKey.getColumn()) == column)
        {
            // selected
            return EnvoyAppletConstants.ENVOY_BLUE_DARK;
        }
        // unselected
        return EnvoyAppletConstants.ENVOY_BLUE;
    }

    /**
     * Overloaded to return an icon suitable to the primary sorted column, or
     * null if the column is not the primary sort key.
     * 
     * @param table
     *            the <code>JTable</code>.
     * @param column
     *            the column index.
     * @return the sort icon, or null if the column is unsorted.
     */
    protected Icon getIcon(JTable table, int column)
    {
        SortKey sortKey = getSortKey(table, column);
        if (sortKey != null
                && table.convertColumnIndexToView(sortKey.getColumn()) == column)
        {
            switch (sortKey.getSortOrder())
            {
                case ASCENDING:
                    return UIManager.getIcon("Table.ascendingSortIcon");
                case DESCENDING:
                    return UIManager.getIcon("Table.descendingSortIcon");
            }
        }
        return null;
    }

    /**
     * Returns the current sort key, or null if the column is unsorted.
     * 
     * @param table
     *            the table
     * @param column
     *            the column index
     * @return the SortKey, or null if the column is unsorted
     */
    protected SortKey getSortKey(JTable table, int column)
    {
        RowSorter rowSorter = table.getRowSorter();
        if (rowSorter == null)
        {
            return null;
        }

        List sortedColumns = rowSorter.getSortKeys();
        if (sortedColumns.size() > 0)
        {
            return (SortKey) sortedColumns.get(0);
        }
        return null;
    }
}