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

import CoffeeTable.Grid.GridAdapter;
import CoffeeTable.Grid.GridAttributes;
import CoffeeTable.Grid.GridData;
import CoffeeTable.Grid.GridDataInterface;
import CoffeeTable.Grid.GridEvent;
import CoffeeTable.Grid.GridPanel;
import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;
import com.globalsight.everest.servlet.ExceptionMessage;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyButton;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyGrid;
import com.globalsight.everest.webapp.applet.common.EnvoyLabel;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;
import com.globalsight.everest.webapp.applet.common.MessageDialog;
import com.globalsight.util.collections.HashtableValueOrderWalker;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
* The DBColumnPanel, designed to add, modify, and removedb columns.
*/

public class DBColumnPanel extends EnvoyGrid implements EnvoyAppletConstants
{

    private static final int NEW = 1;
    private static final int MODIFY = 2;
    private static final int REMOVE = 4;
    private static final int TRANSPOSE = 7;
    private final String m_columnWidths="50, 50, 50, 50, 50";
    private GridPanel m_grid = null;
    private GridData m_gridData;
    private String m_title;
    private String[] m_dialogLabels = null;
    private String[] m_dialogButtons = null;
    private HashtableValueOrderWalker m_mode_pairs;
    private HashtableValueOrderWalker m_knownFormatType_pairs;
    private HashtableValueOrderWalker m_xmlRule_pairs;
    private EnvoyButton newButton;
    private EnvoyButton editButton;
    private EnvoyButton removeButton;
    private EnvoyButton previousButton;
    private EnvoyButton cancelButton;
    private EnvoyButton nextButton;
    private EnvoyButton m_upButton;
    private EnvoyButton m_downButton;    
    
    /**
    * Populate the data of the grid panel.
    * @param p_objects - The data used for populating the panel components.
    */
    public void populate(Vector p_objects)
    {
        setLayout(new EnvoyLineLayout(0, 0, 0, 0));
        this.setBackground(ENVOY_WHITE);
        String[] labels = (String[])p_objects.elementAt(0);
        String[] header = (String[])p_objects.elementAt(1);
        m_gridData = (GridData)p_objects.elementAt(2);
        m_dialogLabels = (String[])p_objects.elementAt(3);
        m_dialogButtons = (String[])p_objects.elementAt(4);
        final String[] imageNames = (String[])p_objects.elementAt(5);
        m_mode_pairs = (HashtableValueOrderWalker)p_objects.elementAt(6);
        m_knownFormatType_pairs = (HashtableValueOrderWalker)p_objects.elementAt(7);
        m_xmlRule_pairs = (HashtableValueOrderWalker)p_objects.elementAt(8);
        m_title = m_dialogLabels[0];

        // grid
        m_grid = new GridPanel(m_gridData.getNumRows(), m_gridData.getNumCols());
        m_grid.setNumCols(header.length);

        if (m_gridData == null)
        {
            // we're in create mode
            m_grid.setNumRows(0);
        } else
        {
            m_grid.setNumRows(m_gridData.getNumRows());
            m_grid.setGridData(m_gridData, true);
        }

        setDisplayOptions(m_grid);
        // set up attributes for the 2nd column. WORD WRAP ON for the driver.
        m_grid.setColAttributes(2, new GridAttributes(this, CELL_FONT, ENVOY_BLACK, ENVOY_WHITE,
                                                      GridPanel.JUST_LEFT | GridPanel.WORD_WRAP, GridPanel.TEXT), true);

        // The columns should *not* be sortable since the order is important!
        m_grid.setSortEnable(false);

        // enable selection options.
        m_grid.setCellSelection(false);
        m_grid.setRowSelection(true);
        m_grid.setColSelection(false);
        m_grid.setMultipleSelection(false);
        // set the col widths.
        m_grid.setColWidths(m_columnWidths);

        // set header, column and row style
        m_grid.setColHeaders(header);
        m_grid.setAutoResizeColumns(true);
        m_grid.setAutoResizeRows(true);
        m_grid.autoResizeRows(true);
        m_grid.setRowNumbers(false);

        // Create Buttons.
        newButton = new EnvoyButton(labels[0]);
        int widthNew = GlobalEnvoy.getStringWidth(labels[0]) + 10;

        editButton = new EnvoyButton(labels[1]);
        int widthEdit= GlobalEnvoy.getStringWidth(labels[1]) + 10;

        removeButton = new EnvoyButton(labels[2]);
        int widthRemove = GlobalEnvoy.getStringWidth(labels[2]) + 10;

        previousButton = new EnvoyButton(labels[3]);
        int widthPrevious = GlobalEnvoy.getStringWidth(labels[3]) + 10;

        cancelButton = new EnvoyButton(labels[4]);
        int widthCancel = GlobalEnvoy.getStringWidth(labels[4]) + 10;

        nextButton = new EnvoyButton(labels[5]);
        int widthNext = GlobalEnvoy.getStringWidth(labels[5]) + 10;

        m_upButton= new EnvoyButton(labels[6]);
        int widthUpButton = GlobalEnvoy.getStringWidth(labels[6]) + 10;
        //int widthUpButton = 200;

        m_downButton= new EnvoyButton(labels[7]);
        int widthDownButton = GlobalEnvoy.getStringWidth(labels[7]) + 10;

        int arrowWidth = widthUpButton > widthDownButton ? widthUpButton : widthDownButton;

        // Disable the buttons.
        editButton.setEnabled(false);
        removeButton.setEnabled(false);
        m_upButton.setEnabled(false);
        m_downButton.setEnabled(false);


        previousButton.setEnabled(true);
        previousButton.addActionListener(new ActionListener() {
                                             public void actionPerformed(ActionEvent event) {
                                                 getEnvoyApplet().appendDataToPostConnection(null, "pre3URL");
                                             }
                                         });
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent event) {
                                               getEnvoyApplet().appendDataToPostConnection(null, "cancel3URL");
                                           }
                                       });
        if (m_grid.getNumRows() > 0)
            nextButton.setEnabled(true);
        else
            nextButton.setEnabled(false);
        nextButton.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent event) {
                                             getEnvoyApplet().appendDataToPostConnection(null, "next3URL");
                                         }
                                     });


        // "newButton" listeners.
        newButton.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e)
                                        {
                                            //performAction(NEW);
                                            update(NEW);
                                        }
                                    });

        // "editButton" listeners.
        editButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e)
                                           {
                                               if (m_grid.getFirstSelectedRow() > 0)
                                                   update(MODIFY);
                                           }
                                       });

        // "removeButton" listeners.
        removeButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e)
                                           {
                                               if (m_grid.getFirstSelectedRow() > 0)
                                               {
                                                   if (getRemoveConfirmDlg())
                                                   {
                                                       /*performAction(REMOVE);
                                                       m_grid.deleteRow(m_grid.getFirstSelectedRow());*/
                                                       update(REMOVE);
                                                   }
                                               }
                                           }
                                       });


        m_grid.addGridListener(new GridAdapter() {
                                   public void gridDoubleClicked(GridEvent e)
                                   {
                                       if (m_grid.getFirstSelectedRow() > 0)
                                           update(MODIFY);
                                   }
                                   public void gridSelChanged(GridEvent e)
                                   {
                                       boolean isEnabled = e.getRow() > 0;

                                       editButton.setEnabled(isEnabled);
                                       removeButton.setEnabled(isEnabled);
                                       setEnableUpButton();
                                       setEnableDownButton();
                                   }
                               });

        // up button listeners for the side buttons.
        m_upButton.addActionListener(new ActionListener()
                                     {
                                         public void actionPerformed(ActionEvent e) 
                                         {
                                             moveRowUp();
                                         }
                                     });
        m_upButton.addMouseListener(new MouseAdapter()
                                    {
                                        public void mouseExited(MouseEvent e)
                                        {
                                            if (m_grid.getLastSelectedRow() > 1 && m_grid.getNumRows() > 1)
                                                m_upButton.setEnabled(true);
                                        }
                                    });

        // down button listeners for the side buttons.
        m_downButton.addActionListener(new ActionListener()
                                       {
                                           public void actionPerformed(ActionEvent e)
                                           {
                                               moveRowDown();
                                           }
                                       });
        m_downButton.addMouseListener(new MouseAdapter()
                                      {
                                          public void mouseExited(MouseEvent e)
                                          {
                                              if (m_grid.getLastSelectedRow() < m_grid.getNumRows() && m_grid.getNumRows() > 1)
                                                  m_downButton.setEnabled(true);
                                          }
                                      });

        // add arrows
        Vector arrowVector = new Vector();
        arrowVector.addElement("blank");
        arrowVector.addElement("blank");
        arrowVector.addElement("blank");
        arrowVector.addElement("blank");
        arrowVector.addElement(m_upButton);
        arrowVector.addElement("blank");
        arrowVector.addElement(m_downButton);
        arrowVector.addElement("end");

        // add to panel
        this.add(getBorderedGridPanel(m_grid),
                 new EnvoyConstraints(GRID_WIDTH, GRID_HEIGHT, 1, 
                                      EnvoyConstraints.LEFT,
                                      EnvoyConstraints.X_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(getArrowPanel(arrowVector, arrowWidth),
                 new EnvoyConstraints(arrowWidth, GRID_HEIGHT, 1, 
                                      EnvoyConstraints.LEFT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.END_OF_LINE));
        this.add(cancelButton,
                 new EnvoyConstraints(widthCancel, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(5, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(removeButton,
                 new EnvoyConstraints(widthRemove, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(5, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(editButton,
                 new EnvoyConstraints(widthEdit, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(5, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(newButton,
                 new EnvoyConstraints(widthNew, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));

        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(arrowWidth, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.END_OF_LINE));
        this.add(previousButton,
                 new EnvoyConstraints(widthPrevious, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(5, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(nextButton,
                 new EnvoyConstraints(widthNext, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));

        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(arrowWidth, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.END_OF_LINE));
    }

    /**
    * Get the title of the grid.
    * Not used, but required because of abstract super class EnvoyGrid
    * @return The grid's title.
    */
    public String getTitle()
    {
        return m_title;
    }

    private DatabaseColumnImpl getSelectedDBColumn() {
        return(DatabaseColumnImpl)m_grid.getCellData(1, m_grid.getFirstSelectedRow());
    }

    /**
    * Get the dialog for confirming removal of a user from the system.
    */
    private boolean getRemoveConfirmDlg()
    {
        String btnLables[] = {m_dialogButtons[5], m_dialogButtons[5],
            m_dialogButtons[5], m_dialogButtons[3], m_dialogButtons[4]}; //ok, cancel
        Hashtable hashtable = new Hashtable();
        hashtable.put(EnvoyAppletConstants.BTN_LABELS, btnLables);
        hashtable.put(EnvoyAppletConstants.MESSAGE, m_dialogLabels[8]);
        return MessageDialog.getMessageDialog(getParentFrame(), m_dialogLabels[7], hashtable);
    }

    private void updateGrid(int p_command, DatabaseColumnImpl p_dbcolumn) {
        Integer Itmp;
        switch (p_command)
        {
        case NEW: // Adding an dbcolumn.
            int num = m_gridData.getNumRows();
            m_gridData.setNumRows(num+1);
            m_grid.setNumRows(num+1);
            m_gridData.setCellData(1, num+1, p_dbcolumn);
            m_gridData.setCellData(2, num+1, p_dbcolumn.getLabel());
            m_gridData.setCellData(3, num+1, p_dbcolumn.getTableName());
            Itmp = new Integer((int)p_dbcolumn.getFormatType());
            m_gridData.setCellData(4, num+1, m_knownFormatType_pairs.get(Itmp));
            Itmp = new Integer((int)p_dbcolumn.getContentMode());
            m_gridData.setCellData(5, num+1, m_mode_pairs.get(Itmp));
            m_grid.setGridData(m_gridData, true);
            m_grid.autoResizeRow(num+1, true);
            break;
        case MODIFY: // Modifying an dbcolumn.
            m_grid.setCellData(1, m_grid.getFirstSelectedRow(), p_dbcolumn, true);
            m_grid.setCellData(2, m_grid.getFirstSelectedRow(), p_dbcolumn.getLabel(), true);
            m_grid.setCellData(3, m_grid.getFirstSelectedRow(), p_dbcolumn.getTableName(), true);
            Itmp = new Integer((int)p_dbcolumn.getFormatType());
            m_grid.setCellData(4, m_grid.getFirstSelectedRow(), m_knownFormatType_pairs.get(Itmp), true);
            Itmp = new Integer((int)p_dbcolumn.getContentMode());
            m_grid.setCellData(5, m_grid.getFirstSelectedRow(), m_mode_pairs.get(Itmp), true);
            m_grid.autoResizeRow(m_grid.getFirstSelectedRow(), true);
            break;
        case REMOVE: // Removing a dbcolumn.
            m_grid.deleteRow(m_grid.getFirstSelectedRow());
        }
    }


    // perfom action based on the button click.
    private Vector performAction(int p_command)
    {        
        Hashtable hashtable = new Hashtable();
        hashtable.put(BTN_LABELS, m_dialogButtons);
        hashtable.put(LABELS, m_dialogLabels);
        Vector objs = new Vector();
        switch (p_command)
        {
        case NEW: // Adding a dbcolumn.
            objs.addElement(new Integer(NEW));
            hashtable.put(DBCOLUMN_FORMATPAIRS, m_knownFormatType_pairs);
            hashtable.put(DBCOLUMN_RULEPAIRS, m_xmlRule_pairs);
            hashtable.put(DBCOLUMN_MODEPAIRS, m_mode_pairs);
            Vector addData = CreateModifyDBColumnDialog.getDialog(this,
                                                                  m_dialogLabels[9], hashtable);

            if (addData != null && addData.size() > 0)
            {
                objs.addElement(addData.elementAt(0));
		nextButton.setEnabled(true);
            }
            break;
        case MODIFY: // Modifying a dbcolumn.
            objs.addElement(new Integer(MODIFY));
            hashtable.put(DBCOLUMN_MOD, getSelectedDBColumn());
            hashtable.put(DBCOLUMN_FORMATPAIRS, m_knownFormatType_pairs);
            hashtable.put(DBCOLUMN_RULEPAIRS, m_xmlRule_pairs);
            hashtable.put(DBCOLUMN_MODEPAIRS, m_mode_pairs);
            Vector modData = CreateModifyDBColumnDialog.getDialog(this,
                                                                  m_dialogLabels[11], hashtable);
            if (modData != null && modData.size() > 0)
                objs.addElement(modData.elementAt(0));
            break;
        case REMOVE: // Removing a dbcolumn.
            objs.addElement(new Integer(REMOVE));
            objs.addElement(getSelectedDBColumn());
            if (m_grid.getNumRows() <= 1)
            {
                nextButton.setEnabled(false);
            }
            //updateGrid(REMOVE, null);
            //getEnvoyApplet().appendDataToPostConnection(objs, "null");//p_targetUrl);
            break;
        }
        return objs.size() > 1 ?
        getEnvoyApplet().appendDataToPostConnection(objs, "null") :
        null;
    }

    // perform the action and update the grid
    private void update(int p_command)
    {
        Vector result = performAction(p_command);
        if (result == null && p_command == REMOVE)
        {
            updateGrid(p_command, null);
        } else if (result != null && 
                   !(result.elementAt(0) instanceof ExceptionMessage))
        {
            updateGrid(p_command, (DatabaseColumnImpl)result.elementAt(1));
        }
    }

    // move a row up
    private void moveRowUp()
    {
        int selectedRow = m_grid.getFirstSelectedRow();

        // transpose column numbers
        DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl)m_grid.getCellData(1, selectedRow);
        DatabaseColumnImpl dbcolumn1 = (DatabaseColumnImpl)m_grid.getCellData(1, selectedRow-1);
        long ltmp = dbcolumn.getColumnNumber();
        dbcolumn.setColumnNumber(dbcolumn1.getColumnNumber());
        dbcolumn1.setColumnNumber(ltmp);

        // communicate transpose to handler
        Vector objs = new Vector();
        objs.addElement(new Integer(TRANSPOSE));
        objs.addElement(dbcolumn);
        getEnvoyApplet().appendDataToPostConnection(objs, "null");

        Vector rowData = m_grid.getRowData(selectedRow);
        m_grid.setRowData(selectedRow, m_grid.getRowData(selectedRow-1), true);
        m_grid.setRowData(selectedRow-1, rowData, true);
        m_grid.deselectRow(selectedRow, true);
        m_grid.selectRow(selectedRow-1, true);
        // update the buttons.
        setEnableUpButton();
        setEnableDownButton();
    }

    // move a row down
    private void moveRowDown()
    {
        int selectedRow = m_grid.getFirstSelectedRow();

        // transpose column numbers
        DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl)m_grid.getCellData(1, selectedRow);
        DatabaseColumnImpl dbcolumn1 = (DatabaseColumnImpl)m_grid.getCellData(1, selectedRow+1);
        long ltmp = dbcolumn.getColumnNumber();
        dbcolumn.setColumnNumber(dbcolumn1.getColumnNumber());
        dbcolumn1.setColumnNumber(ltmp);

        // communicate transpose to handler
        Vector objs = new Vector();
        objs.addElement(new Integer(TRANSPOSE));
        objs.addElement(dbcolumn1);
        getEnvoyApplet().appendDataToPostConnection(objs, "null");

        Vector rowData = m_grid.getRowData(selectedRow);
        m_grid.setRowData(selectedRow, m_grid.getRowData(selectedRow+1), true);
        m_grid.setRowData(selectedRow+1, rowData, true);
        m_grid.deselectRow(selectedRow, true);
        m_grid.selectRow(selectedRow+1, true);
        // update the buttons.
        setEnableUpButton();
        setEnableDownButton();
    }

    // update the up button status.
    private void setEnableUpButton()
    {
        m_upButton.setEnabled(m_grid.getFirstSelectedRow() > 1 &&
                              m_grid.getNumRows() > 1);     
    }

    // update the down button status.
    private void setEnableDownButton()
    {
        m_downButton.setEnabled(
                               m_grid.getFirstSelectedRow() > 0 &&
                               m_grid.getFirstSelectedRow() < m_grid.getNumRows() &&
                               m_grid.getNumRows() > 1);       
    }

    protected Panel getArrowPanel(Vector p_buttons, int arrowWidth)
    {
        Panel arrowPanel = new Panel(new EnvoyLineLayout(0, 0, 0, 0));
        arrowPanel.setBackground(ENVOY_WHITE);
        for (int i = 0; i < p_buttons.size(); i++)
        {
            if (p_buttons.elementAt(i) instanceof String)
            {
                arrowPanel.add(new EnvoyLabel("", java.awt.Label.CENTER, arrowWidth, ARROW_HEIGHT),
                               new EnvoyConstraints(arrowWidth, ARROW_HEIGHT,
                                                    1, EnvoyConstraints.LEFT,
                                                    EnvoyConstraints.X_NOT_RESIZABLE,
                                                    ((String)p_buttons.elementAt(i)).equals("end") ?
                                                    EnvoyConstraints.Y_RESIZABLE : EnvoyConstraints.Y_NOT_RESIZABLE,
                                                    EnvoyConstraints.END_OF_LINE));
            } else
            {
                EnvoyButton arrowToBeAddedToPanel = (EnvoyButton)p_buttons.elementAt(i);
                arrowPanel.add(arrowToBeAddedToPanel,
                               new EnvoyConstraints(arrowWidth, ARROW_HEIGHT,
                                                    1, EnvoyConstraints.LEFT,
                                                    EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                                                    EnvoyConstraints.END_OF_LINE));
            }
        }
        return arrowPanel;
    }
}
