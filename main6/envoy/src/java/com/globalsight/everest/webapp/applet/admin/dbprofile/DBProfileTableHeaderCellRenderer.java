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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyFonts;

/**
 * This is a header cell renderer for the db column profile table.
 * 
 */
public class DBProfileTableHeaderCellRenderer extends DefaultTableCellRenderer
{
    /**
     * Constructs a <code>DBProfileTableHeaderCellRenderer</code>.
     * <P>
     * The horizontal text position is set as appropriate to a table header
     * cell, and the opaque property is set to true.
     */
    public DBProfileTableHeaderCellRenderer()
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
        setBackground(EnvoyAppletConstants.ENVOY_BLUE);
        setForeground(EnvoyAppletConstants.ENVOY_WHITE);
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));

        return this;
    }
}
