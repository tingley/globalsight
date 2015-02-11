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
package com.globalsight.everest.webapp.applet.common;

import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;


/**
 * EnvoyJTable is a subclass of JTable which is used for avoiding a couple of swing bugs:
 * 
 * 1. When the auto resize mode is set to AUTO_RESIZE_NEXT_COLUMN, the 
 *    accommodateDelta(int resizingColumnIndex, int delta) method which 
 *    is called from sizeColumnsToFit(int resizingColumn) causes an exception 
 *    due to the wrong column index calculation.
 * 
 * 	   In the accommodateDelta method the "from" and "to" are calculated as follows:
 *    
 *       int columnCount = getColumnCount();
 *       int from = resizingColumnIndex;
 *       int to = columnCount;
 *
 *       // Use the mode to determine how to absorb the changes.
 *       switch (autoResizeMode)
 *       {
 *           case AUTO_RESIZE_NEXT_COLUMN:        from = from + 1; to = from + 1; break;
 *           case AUTO_RESIZE_SUBSEQUENT_COLUMNS: from = from + 1; to = columnCount; break;
 *           case AUTO_RESIZE_LAST_COLUMN:        from = columnCount - 1; to = from + 1; break;
 *           case AUTO_RESIZE_ALL_COLUMNS:        from = 0; to = columnCount; break;
 *           default:                             return;
 *       }
 *
 *       In AUTO_RESIZE_NEXT_COLUMN case, the variable from should always be less than the column count to prevent
 *       an ArrayIndexOutOfBoundsException inside the following for loop: 
 *
 *       int totalWidth = 0; 
 *       for (int i = from; i < to; i++)
 *       {
 *           TableColumn aColumn = columnModel.getColumn(i);	  // "i" should always be less than column count
 *           int input = aColumn.getWidth(); 
 *           totalWidth = totalWidth + input;			
 *       }
 *
 * 2. scrollRectToVisible bug -- shown in the "Implementation for the scrollRectToVisible" section.
 *
 */




public class EnvoyJTable extends JTable
{

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////

    /**
     * Constructs a default EnvoyJTable which is initialized with a default
     * data model, a default column model, and a default selection
     * model.
     */
    public EnvoyJTable() 
    {
        super();        
    }
    
    /**
     * Constructs a EnvoyJTable which is initialized with <i>dm</i> as the
     * data model, a default column model, and a default selection
     * model.
     *
     * @param dm        The data model for the table
     */
    public EnvoyJTable(TableModel dm) 
    {
        super(dm);
    }

    /**
     * Constructs a EnvoyJTable which is initialized with <i>dm</i> as the
     * data model, <i>cm</i> as the column model, and a default selection
     * model.
     *
     * @param dm        The data model for the table
     * @param cm        The column model for the table
     */
    public EnvoyJTable(TableModel dm, TableColumnModel cm) 
    {
        super(dm, cm);
    }
    
    /**
     * Constructs a EnvoyJTable with <i>numRows</i> and <i>numColumns</i> of
     * empty cells using the DefaultTableModel.  The columns will have
     * names of the form "A", "B", "C", etc.
     *
     * @param numRows           The number of rows the table holds
     * @param numColumns        The number of columns the table holds
     */
    public EnvoyJTable(int numRows, int numColumns) 
    {
        super(numRows, numColumns);
    }
    
    /**
     * Constructs a EnvoyJTable to display the values in the two dimensional array,
     * <i>rowData</i>, with column names, <i>columnNames</i>.
     * <i>rowData</i> is an Array of rows, so the value of the cell at row 1,
     * column 5 can be obtained with the following code:
     * <p>
     * <pre> rowData[1][5]; </pre>
     * <p>
     * All rows must be of the same length as <i>columnNames</i>.
     * <p>
     * @param rowData           The data for the new table
     * @param columnNames       Names of each column
     */
    public EnvoyJTable(final Object[][] rowData, final Object[] columnNames) 
    {
        super(rowData, columnNames);
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Override Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * This is a special case for the AUTO_RESIZE_NEXT_COLUMN resize mode.  
     * If the resizing is taking place at the left edge of the first 
     * column, or the right edge of the last column we'll set the 
     * sizeColumnsToFit to -1.
     */
    public void sizeColumnsToFit(int resizingColumn)
    {
        if ((getAutoResizeMode() == AUTO_RESIZE_NEXT_COLUMN         ||
             getAutoResizeMode() == AUTO_RESIZE_SUBSEQUENT_COLUMNS) && 
            (resizingColumn == (getColumnCount()-1)))
            super.sizeColumnsToFit(-1);
        else
        {
            super.sizeColumnsToFit(resizingColumn); 
        }

        // now repaint the table.
        resizeAndRepaint();
    }


    /**
    * Set the model to be this new model and then set the column size to fit.
    * @return newModel  The model set for this table.
    */
    public void setModel(TableModel newModel) 
    {
        super.setModel(newModel);

        sizeColumnsToFit(getColumnCount() > 0 ? (getColumnCount() -1) : 0);
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Display the selected row in the table.  The scrollRectToVisible of 
     * the JViewPort is buggy and does not set the selected row visible.
     * This method is a fix for:
     * scrollPane.getViewport().scrollRectToVisible(table.getCellRect(row, 1, false));
     *
     * The main purpose is to select a specific row and display it on the
     * table (maily for search purposes).
     *
     *  Example (how to use this method):               
     *  int rowIndex = <some search method that would return the index of the row that was found>;
     *  // select the row based on the search result index.
     *  table.setRowSelectionInterval(searchIndex, searchIndex);
     *  // now show the selected row
     *  table.showSelectedRow(searchIndex);
     *
     * @param row The selected row in the table. 			
     */    
    public void showSelectedRow(int row)
    {
        // get the rectangle of the selected row.
        Rectangle rectangle = getCellRect(row, 0, true);

        if (rectangle != null)
        {
            setSize(getWidth(),Integer.MAX_VALUE);
            scrollRectToVisible(rectangle);            
        }
    }    
    //////////////////////////////////////////////////////////////////////////////////
    //  END:  Implementation for the scrollRectToVisible bug
    //////////////////////////////////////////////////////////////////////////////////	
}