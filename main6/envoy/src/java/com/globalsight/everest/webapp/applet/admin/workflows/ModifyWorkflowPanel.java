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
package com.globalsight.everest.webapp.applet.admin.workflows;

import CoffeeTable.Grid.GridAdapter;
import CoffeeTable.Grid.GridAttributes;
import CoffeeTable.Grid.GridData;
import CoffeeTable.Grid.GridEvent;
import CoffeeTable.Grid.GridPanel;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.webapp.applet.common.AbstractEnvoyDialog;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyButton;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyGrid;
import com.globalsight.everest.webapp.applet.common.EnvoyLabel;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.webapp.applet.common.EnvoyWordWrapper;
import com.globalsight.everest.webapp.applet.common.MessageDialog;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.TimerDefinition;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowDataItem;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.util.date.DateHelper;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The ModifyWorkflowPanel is responsible for modifying a workflow template,
 * and modifying an existing one.
 */
public class ModifyWorkflowPanel
    extends EnvoyGrid
    implements ActivityParentable
{
    private final GridPanel m_grid = new GridPanel();
    private Vector m_dialogInfo = null;
    private final EnvoyButton upButton =
        new EnvoyButton(getImage(EnvoyButton.ARROW_UP_RELEASED_IMG_URL),
                        getImage(EnvoyButton.ARROW_UP_PRESSED_IMG_URL),
                        getImage(EnvoyButton.ARROW_UP_DISABLED_IMG_URL));
    private final EnvoyButton downButton =
        new EnvoyButton(getImage(EnvoyButton.ARROW_DOWN_RELEASED_IMG_URL),
                        getImage(EnvoyButton.ARROW_DOWN_PRESSED_IMG_URL),
                        getImage(EnvoyButton.ARROW_DOWN_DISABLED_IMG_URL));
    private EnvoyButton modifyButton = null;
    private EnvoyButton removeButton = null;
    private EnvoyButton cancelButton = null;
    private EnvoyButton saveButton = null;
    private static final String DONE_URL = "doneURL";
    private static final String CANCEL_URL = "cancelURL";
    private static final String READY_URL = "readyURL";
    private boolean m_isReady = false;

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  EnvoyGrid Implementation  ////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    /**
     * Populate the data of the grid panel.
     * @param p_objects - The data used for populating the panel components.
     */
    public void populate(Vector p_objects)
    {
        setLayout(new EnvoyLineLayout(0, 0, 0, 0));
        setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        String[] header = (String[])p_objects.elementAt(0);
        // NOTE: the button create boolean must come before the createGridPanel method.
        //       this boolean must be set before creating the panel.
        // create the GRID PANEL.
        createGridPanel((String[])p_objects.elementAt(1));
        GridData gridData = (GridData)p_objects.elementAt(2);
        m_dialogInfo = (Vector)p_objects.elementAt(3);
        m_isReady = ((Boolean)p_objects.elementAt(4)).booleanValue();
        // set the number of columns for the grid.
        m_grid.setNumCols(header.length);
        m_grid.setNumRows(gridData.getNumRows());
        m_grid.setGridData(gridData, true);
        // enable selection options.
        m_grid.setCellSelection(false);
        m_grid.setRowSelection(true);
        m_grid.setColSelection(false);
        m_grid.setMultipleSelection(false);
        // set display the options.
        setDisplayOptions(m_grid);
        checkForCompletedTasks();
        // configure resizing.
        m_grid.setResizeColumns(true);
        m_grid.setResizeRows(false);
        m_grid.setResizeColHeaders(true);
        m_grid.setResizeRowHeaders(false);
        m_grid.setAutoResizeColumns(true);
        m_grid.setAutoResizeRows(true);
        m_grid.autoResizeRows(true);
        m_grid.setColHeaders(header);
        m_grid.setRowNumbers(false);
        // disable sorting
        m_grid.setSortEnable(false);
        // update done button status
        setEnableDoneButton();
    }

    /**
     * Get the title of the grid.
     * @return The grid's title.
     */
    public String getTitle()
    {
        return "";
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  EnvoyGrid Implementation  //////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Local Methods  ///////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    // create grid panel
    private void createGridPanel(String[] p_labels)
    {
        ////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////  BEGIN: SIDE BUTTONS  ////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////
        Panel sideButtonPanel = new Panel();
        sideButtonPanel.setLayout(new EnvoyLineLayout(0, 0, 0, 0));
        // add buttons to the side panel.
        sideButtonPanel.add(new EnvoyLabel(),
                            new EnvoyConstraints(35, 10, 1, EnvoyConstraints.CENTER,
                                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                                 EnvoyConstraints.Y_RESIZABLE,
                                                 EnvoyConstraints.END_OF_LINE));
        sideButtonPanel.add(upButton,
                            new EnvoyConstraints(upButton.getWidth(), upButton.getHeight(),
                                                 1, EnvoyConstraints.LEFT,
                                                 EnvoyConstraints.X_RESIZABLE,
                                                 EnvoyConstraints.Y_RESIZABLE,
                                                 EnvoyConstraints.END_OF_LINE));
        sideButtonPanel.add(downButton,
                            new EnvoyConstraints(downButton.getWidth(), downButton.getHeight(),
                                                 1, EnvoyConstraints.LEFT,
                                                 EnvoyConstraints.X_RESIZABLE,
                                                 EnvoyConstraints.Y_RESIZABLE,
                                                 EnvoyConstraints.END_OF_LINE));
        sideButtonPanel.add(new EnvoyLabel(),
                            new EnvoyConstraints(35, 10, 1, EnvoyConstraints.CENTER,
                                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                                 EnvoyConstraints.Y_RESIZABLE,
                                                 EnvoyConstraints.END_OF_LINE));

        // button listeners for the side buttons.
        upButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent e)
                                  {
                                      moveRowUp();
                                  }
                              });
        upButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   upButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   upButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        upButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     if (m_grid.getLastSelectedRow() > 1 && m_grid.getNumRows() > 1 && isOKToMoveTaskUp())
                                         upButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     upButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     upButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });

        downButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent e)
                                  {
                                      moveRowDown();
                                  }
                              });
        downButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   downButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   downButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        downButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     if (m_grid.getLastSelectedRow() < m_grid.getNumRows() && m_grid.getNumRows() > 1)
                                         downButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     downButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     downButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });
        // disable buttons.
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        Panel gridAndSideButtonPanel = new Panel(new EnvoyLineLayout(0, 0, 0, 0));
        gridAndSideButtonPanel.
            add(m_grid,
                new EnvoyConstraints(447, 320, 1,
                                     EnvoyConstraints.CENTER,
                                     EnvoyConstraints.X_RESIZABLE,
                                     EnvoyConstraints.Y_RESIZABLE,
                                     EnvoyConstraints.NOT_END_OF_LINE));
        gridAndSideButtonPanel.
            add(sideButtonPanel,
                new EnvoyConstraints(35, 320, 1,
                                     EnvoyConstraints.LEFT,
                                     EnvoyConstraints.X_NOT_RESIZABLE,
                                     EnvoyConstraints.Y_RESIZABLE,
                                     EnvoyConstraints.END_OF_LINE));
        ////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////  END: SIDE BUTTONS  //////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////  BEGIN: GRID BUTTONS  /////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////

        final EnvoyButton newButton = 
            new EnvoyButton(getImage(p_labels[0]), getImage(p_labels[1]));
        modifyButton =
            new EnvoyButton(getImage(p_labels[2]),
                            getImage(p_labels[3]),
                            getImage(p_labels[4]));
        removeButton =
            new EnvoyButton(getImage(p_labels[5]),
                            getImage(p_labels[6]),
                            getImage(p_labels[7]));
        cancelButton = 
            new EnvoyButton(getImage(p_labels[8]), getImage(p_labels[9]));
        saveButton =
            new EnvoyButton(getImage(p_labels[10]),
                            getImage(p_labels[11]),
                            getImage(p_labels[12]));

        // button listeners.
        newButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent e)
                                  {
                                      insertRow(m_grid.getLastSelectedRow());
                                  }
                              });
        newButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   newButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   newButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        newButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     newButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     newButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     newButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });

        modifyButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent e) {
                                      if (m_grid.getFirstSelectedRow() > 0)
                                          editRow();
                                  }
                              });
        modifyButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   modifyButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   modifyButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        modifyButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     modifyButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     modifyButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     modifyButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });

        removeButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent e)
                                  {
                                      deleteRow();
                                      checkForCompletedTasks();
                                  }
                              });
        removeButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   removeButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   removeButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        removeButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     removeButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     removeButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     removeButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });

        saveButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent event) {
                                      performAction(DONE_URL);
                                  }
                              });
        saveButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   saveButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   saveButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        saveButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     saveButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     saveButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     saveButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });

        cancelButton.
            addActionListener(new ActionListener()
                              {
                                  public void actionPerformed(ActionEvent event) {
                                      performAction(CANCEL_URL);
                                  }
                              });
        cancelButton.
            addKeyListener(new KeyAdapter()
                           {
                               public void keyPressed(KeyEvent e)
                               {
                                   cancelButton.setState(EnvoyButton.PRESSED_STATE);
                               }
                               public void keyReleased(KeyEvent e)
                               {
                                   cancelButton.setState(EnvoyButton.RELEASED_STATE);
                               }
                           });
        cancelButton.
            addMouseListener(new MouseAdapter()
                             {
                                 public void mouseExited(MouseEvent e)
                                 {
                                     cancelButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                                 public void mousePressed(MouseEvent e)
                                 {
                                     cancelButton.setState(EnvoyButton.PRESSED_STATE);
                                 }
                                 public void mouseReleased(MouseEvent e)
                                 {
                                     cancelButton.setState(EnvoyButton.RELEASED_STATE);
                                 }
                             });

        // disable buttons.
        newButton.setEnabled(true);
        modifyButton.setEnabled(false);
        removeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        ////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////  END: GRID BUTTONS  ///////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////

        // add listeners to the grid.
        m_grid.
            addGridListener(new GridAdapter()
                            {
                                public void gridDoubleClicked(GridEvent e)
                                {
                                    if (m_grid.getLastSelectedRow() > 0 && isTaskEditable())
                                        editRow();
                                }
                                public void gridCellsClicked(GridEvent event)
                                {
                                    setEnableUpButton();
                                    setEnableDownButton();
                                    boolean isEnabled = isTaskEditable();
                                    boolean isRemovable = getSelectedTaskState() != WorkflowConstants.TASK_ACTIVE;
                                    modifyButton.setEnabled(isEnabled);
                                    removeButton.setEnabled(isEnabled && isRemovable);          
                                }
                                public void gridSelChanged(GridEvent event)
                                {
                                    setEnableUpButton();
                                    setEnableDownButton();
                                    if (event.getRow() != 0)
                                    {
                                        if (isTaskEditable())
                                        {
                                            if (getSelectedTaskState() == WorkflowConstants.TASK_ACTIVE)
                                            {
                                                m_grid.setHighlightTextColor(Color.red);
                                            }
                                            else
                                            {
                                                m_grid.setHighlightTextColor(EnvoyAppletConstants.ENVOY_WHITE);
                                            }
                                            m_grid.setHighlightColor(EnvoyAppletConstants.ENVOY_BLUE);
                                        }
                                        else
                                        {
                                            m_grid.setHighlightColor(EnvoyAppletConstants.ENVOY_WHITE);
                                            m_grid.setHighlightTextColor(EnvoyAppletConstants.ENVOY_GREY);
                                        }
                                    }
                                }
                            });
        // add to panel
        add(getBorderedGridPanel(gridAndSideButtonPanel),
            new EnvoyConstraints(GRID_WIDTH, GRID_HEIGHT, 1,
                                 EnvoyConstraints.LEFT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.END_OF_LINE));

        add(newButton,
            new EnvoyConstraints(45, 24, 1,
                                 EnvoyConstraints.RIGHT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

	add(new EnvoyLabel(),
		 new EnvoyConstraints(5, 24, 1, 
                                      EnvoyConstraints.RIGHT,
				      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
				      EnvoyConstraints.NOT_END_OF_LINE));

        add(modifyButton,
            new EnvoyConstraints(45, 24, 1,
                                 EnvoyConstraints.RIGHT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

	add(new EnvoyLabel(),
		 new EnvoyConstraints(5, 24, 1, 
                                      EnvoyConstraints.RIGHT,
				      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
				      EnvoyConstraints.NOT_END_OF_LINE));

        add(removeButton,
            new EnvoyConstraints(54, 24, 1,
                                 EnvoyConstraints.RIGHT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

	add(new EnvoyLabel(),
		 new EnvoyConstraints(5, 24, 1, 
                                      EnvoyConstraints.RIGHT,
				      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
				      EnvoyConstraints.NOT_END_OF_LINE));

        add(cancelButton,
            new EnvoyConstraints(40, 24, 1,
                                 EnvoyConstraints.RIGHT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

	add(new EnvoyLabel(),
		 new EnvoyConstraints(5, 24, 1, 
                                      EnvoyConstraints.RIGHT,
				      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
				      EnvoyConstraints.NOT_END_OF_LINE));

        add(saveButton,
            new EnvoyConstraints(25, 24, 1,
                                 EnvoyConstraints.RIGHT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

        add(new EnvoyLabel("", 1, 35, 12),
            new EnvoyConstraints(35, BUTTON_HEIGHT, 1,
                                 EnvoyConstraints.RIGHT,
                                 EnvoyConstraints.X_NOT_RESIZABLE,
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.END_OF_LINE));
    }

    private void checkForCompletedTasks()
    {
        Color clr = null;
        for (int i = m_grid.getNumRows() ; i > 0 ; i--)
        {
            int taskState = ((Integer)m_grid.getCellData(7, i)).intValue();

            if (taskState == WorkflowConstants.TASK_COMPLETED)
            {
                clr = EnvoyAppletConstants.ENVOY_GREY;                
            }
            else if (taskState == WorkflowConstants.TASK_ACTIVE)
            {
                clr = EnvoyAppletConstants.ENVOY_GREEN;
            }
            else
                clr = EnvoyAppletConstants.ENVOY_BLACK;

            m_grid.setRowAttributes(i, new GridAttributes(this, 
                                                          CELL_FONT,
                                                          clr,
                                                          EnvoyAppletConstants.ENVOY_WHITE,
                                                          GridPanel.JUST_LEFT, 
                                                          GridPanel.TEXT), true);
        }
    }

    // Save the template.
    private void performAction(String p_targetURL)
    {
        Vector dataToBeSent = new Vector();
        String targetUrl = m_isReady ? READY_URL : p_targetURL;

        if (p_targetURL.equals(DONE_URL))
        {
            //prevent user from clicking on applet buttons
            saveInProcess(Cursor.WAIT_CURSOR, false);
            
            dataToBeSent.addElement("save");
            dataToBeSent.addElement(addWorkflowTasks());
            getEnvoyApplet().appendDataToPostConnection(dataToBeSent, targetUrl);           
            // in case of return to the same screen, reset cursor
            saveInProcess(-1, true);
        }
        else if (p_targetURL.equals(CANCEL_URL))
        {
            getEnvoyApplet().appendDataToPostConnection(null, targetUrl);
        }
    }


    // during save process, show an hourglass and disable save button.
    // after save process, show system's default curson and enable save button.
    private void saveInProcess(int p_cursorType, boolean p_isEnabled)
    {
        // determine cursor type
        Cursor cursor = p_cursorType == -1 ? 
            Cursor.getDefaultCursor() : 
            new Cursor(p_cursorType);

        this.setCursor(cursor);
        saveButton.setEnabled(p_isEnabled);
    }


    // insert a row
    private void insertRow(int p_row)
    {
        Vector data = getActivityDlgValues(null);
        if (data != null && data.size() > 0)
        {
            // convert data from dlg values to (workflow task instance and also displayable griddata.)
            dlgValuesToWorkflowTaskInstance(data, p_row, true);
            // insert after the selected row if not insert at the end of the table.
            int insertedRow = whereToInsert(p_row);
            m_grid.insertRow(insertedRow);
            m_grid.setRowData(insertedRow, data, true);
            resequence();

            // update done button's status
            setEnableDoneButton();
            // disable buttons since no selection is made in the table.
            modifyButton.setEnabled(false);
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }

    // print the grid...
    private void editRow()
    {
        int rowNum = m_grid.getFirstSelectedRow();
        Vector rowData = m_grid.getRowData(rowNum);
        Vector data = getActivityDlgValues(rowData);
        if (data != null && data.size() > 2)
        {
            dlgValuesToWorkflowTaskInstance(data, rowNum, false);
            m_grid.setRowData(rowNum, data, true);
        }
    }

    // delete a row
    private void deleteRow()
    {
        int selectedRow = m_grid.getLastSelectedRow();
        m_grid.deleteRow(selectedRow);
        resequence();
        // disable buttons since no selection is made in the table.
        modifyButton.setEnabled(false);
        removeButton.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
    }

    // move a row up
    private void moveRowUp()
    {
        int selectedRow = m_grid.getFirstSelectedRow();
        Vector rowData = m_grid.getRowData(selectedRow);
        m_grid.setRowData(selectedRow, m_grid.getRowData(selectedRow-1), true);
        m_grid.setRowData(selectedRow-1, rowData, true);
        m_grid.selectRow(selectedRow-1, true);//select
        m_grid.deselectRow(selectedRow, true);
        // update the buttons.
        setEnableUpButton();
        setEnableDownButton();
    }

    // move a row down
    private void moveRowDown()
    {
        int selectedRow = m_grid.getFirstSelectedRow();
        Vector rowData = m_grid.getRowData(selectedRow);
        m_grid.setRowData(selectedRow, m_grid.getRowData(selectedRow+1), true);
        m_grid.setRowData(selectedRow+1, rowData, true);
        m_grid.selectRow(selectedRow+1, true);
        m_grid.deselectRow(selectedRow, true);
        // update the buttons.
        setEnableUpButton();
        setEnableDownButton();
    }

    // repaint the view
    private void resequence()
    {
        invalidate();
        validate();
        repaint();
    }

    /*
     * get a workflow task object based on the info returned from the Activity dialog.
     * @param p_rowNum This is needed for the sequence of the workflow task.
     */
    private void dlgValuesToWorkflowTaskInstance(Vector p_data,
                                                 int p_seqNum,
                                                 boolean p_isNewTask)
    {
        Activity activity = (Activity)p_data.elementAt(0);
        String[] roles = (String[])p_data.elementAt(1);
        long acceptMillis = Long.parseLong((String)p_data.elementAt(2));
        long completeMillis = Long.parseLong((String)p_data.elementAt(3));
        boolean isUser = ((Boolean)p_data.elementAt(4)).booleanValue();
        
        String[] labels = (String[])m_dialogInfo.elementAt(0);
        String dAbbr = labels[10];
        String hAbbr = labels[11];
        String mAbbr = labels[12];
        String dt = DateHelper.daysHoursMinutes(acceptMillis, dAbbr, hAbbr, mAbbr);
        p_data.setElementAt(dt, 2);
        dt = DateHelper.daysHoursMinutes(completeMillis, dAbbr, hAbbr, mAbbr);
        p_data.setElementAt(dt, 3);
        Vector timerList = createTimerDefs(acceptMillis, completeMillis);
        WorkflowTaskInstance taskInstance = null;


        if (p_isNewTask)
        {
            // if there is rate information
            if (p_data.size() > 5)
            {
                Rate expenseRate = (Rate)p_data.elementAt(5);
                Rate revenueRate = (Rate)p_data.elementAt(6);
                //boolean isOnlySelected = ((Boolean)p_data.elementAt(?)).booleanValue();
                boolean isOnlySelected = true;
                int rateSelectionCriteria = WorkflowConstants.USE_ONLY_SELECTED_RATE;
                if(isOnlySelected)
                {
                    rateSelectionCriteria = WorkflowConstants.USE_ONLY_SELECTED_RATE;
                }
                else
                {
                    rateSelectionCriteria = WorkflowConstants.USE_SELECTED_RATE_UNTIL_ACCEPTANCE;
                }
                
                taskInstance =
                    new WorkflowTaskInstance(roles,
                                             isUser,
                                             activity,
                                             WorkflowConstants.ACTIVITY,
                                             p_seqNum, timerList, -1,
                                             expenseRate.getId(), revenueRate.getId(), rateSelectionCriteria);
                //add elements in the right order
                p_data.setElementAt(taskInstance, 5);
                p_data.addElement(new Integer(WorkflowConstants.TASK_DEACTIVE));
                p_data.addElement(expenseRate);
                p_data.addElement(revenueRate);
            }   
            else
            {
                taskInstance  =
                    new WorkflowTaskInstance(roles,
                                             isUser,
                                             activity,
                                             WorkflowConstants.ACTIVITY,
                                             p_seqNum, timerList, -1);

                p_data.addElement(taskInstance);
                p_data.addElement(new Integer(WorkflowConstants.TASK_DEACTIVE));
            }            
        }
        else
        {
            taskInstance = (WorkflowTaskInstance)p_data.elementAt(5);
            taskInstance.setActivity(activity);
            taskInstance.setSequence(p_seqNum);
            taskInstance.setTimerDefinitions(timerList);
            taskInstance.setRoles(roles);
            taskInstance.setRoleType(isUser);
            // if costing data specified
            if (p_data.size() > 7)
            {
                taskInstance.setExpenseRateId(((Rate)p_data.elementAt(7)).getId());
                taskInstance.setRevenueRateId(((Rate)p_data.elementAt(8)).getId());
            }
        }

    }

    /* Create and return a vector of timer definitions based on the given */
    /* values for accept time in milliseconds & complete time in milliseconds */
    private Vector createTimerDefs(long p_acceptMillis, long p_completeMillis)
    {
        Vector timerDefs = new Vector();
        if (p_acceptMillis > 0)
        {
            timerDefs.addElement(newTd(WorkflowConstants.ACCEPT,
                                       p_acceptMillis));
        }
        timerDefs.addElement(newTd(WorkflowConstants.COMPLETE,
                                   p_completeMillis));
        return timerDefs;
    }

    /* Create and return a single Email timer definition based on the given */
    /* name and value */
    private TimerDefinition newTd(String p_name, long p_value)
    {
        Vector v = new Vector();
        v.addElement(new WorkflowDataItem(p_name, "" + p_value));
        return new TimerDefinition(p_name, 
                                   WorkflowConstants.RELATIVE,
                                   WorkflowConstants.SENDMAIL,
                                   v);
    }

    // popup the activity dialog
    private Vector getActivityDlgValues(Vector p_rowData)
    {
        String[] labels = (String[])m_dialogInfo.elementAt(0);
        String[] gridLabels = (String[])m_dialogInfo.elementAt(1);
        String[] btnLables = (String[])m_dialogInfo.elementAt(2);
        Vector activities = (Vector)m_dialogInfo.elementAt(3);
        String[] messages = (String[])m_dialogInfo.elementAt(4);

        Hashtable hashtable = new Hashtable();
        hashtable.put(LABELS, labels);
        hashtable.put(GRID_HEADER_LABELS, gridLabels);
        hashtable.put(BTN_LABELS, btnLables);
        hashtable.put(ACTIVITIES, activities);
        hashtable.put(MESSAGE, messages);

        // if the dialog is set up for rates - then costing is enabled
        if (m_dialogInfo.size() > 5)
        {
            Hashtable rates = (Hashtable)m_dialogInfo.elementAt(5);
            hashtable.put(RATES, rates);
            hashtable.put(COSTING_ENABLED, Boolean.TRUE);
        }
        else
        {
            hashtable.put(COSTING_ENABLED, Boolean.FALSE);
        }
        
        if (p_rowData != null)
        {
            String timeToComplete = p_rowData.elementAt(2).toString();
            int completeSeparatorIndex = timeToComplete.indexOf(" ");

            // the values to be sent to the activity dialog.... this is for "edit task"
            Vector values = new Vector();
            values.addElement(p_rowData.elementAt(0).toString()); // 0 activity.
            values.addElement(p_rowData.elementAt(1).toString()); // 1 role.
            WorkflowTaskInstance wft = 
                (WorkflowTaskInstance)p_rowData.elementAt(5);
            values.addElement(new Long(wft.getAcceptTime())); // 2 accept time
            values.addElement(new Long(wft.getCompletedTime())); // 3 complete time
            values.addElement(new Boolean(wft.getRoleType())); // 4 user or role
            values.addElement(wft); // 5. task
            values.addElement(p_rowData.elementAt(6)); // 6. task state
            hashtable.put(VALUES, values);
        }
        return ActivityDialog.getActivityDialog(this, "", hashtable);
    }

    // editable task if not completed state.
    private boolean isTaskEditable()
    {
        return getSelectedTaskState() != WorkflowConstants.TASK_COMPLETED;
    }

    // determines whether the state of a row is deactive
    private boolean isRowStateDeactive(int p_row)
    {
        int taskState = ((Integer)m_grid.getCellData(7, p_row)).intValue();        
        return taskState == WorkflowConstants.TASK_DEACTIVE;        
    }


    private boolean isOKToMoveTaskUp()
    {
        return isRowStateDeactive(m_grid.getLastSelectedRow()-1);   
    }

    private int getSelectedTaskState()
    {
        return((Integer)m_grid.getCellData(7, m_grid.getLastSelectedRow())).intValue();

    }

    private int whereToInsert(int selectedRow)
    {
        if (selectedRow == 0) // nothing selected
            return m_grid.getNumRows()+1;
        if (isTaskEditable())
            return selectedRow + 1;
        else
            return m_grid.getNumRows()+1;
    }

    // update the save button status.
    private void setEnableDoneButton()
    {
        saveButton.setEnabled(isDirty());
    }

    // update the up button status.
    private void setEnableUpButton()
    {
        if (m_grid.getLastSelectedRow() > 1 && m_grid.getNumRows() > 1)
        {
            upButton.setEnabled(isOKToMoveTaskUp());        
        }
        else
        {
            upButton.setEnabled(false);
        }
    }

    // update the down button status.
    private void setEnableDownButton()
    {
        int selectedRow = m_grid.getLastSelectedRow();
        if (selectedRow < m_grid.getNumRows() && selectedRow > 0 
            && m_grid.getNumRows() > 1)
        {
            downButton.setEnabled(isRowStateDeactive(selectedRow));
        }
        else
        {
            downButton.setEnabled(false);
        }
    }

    // determines whether we're in dirty mode
    private boolean isDirty()
    {
        return(m_grid.getNumRows() > 0);
    }

    // popup the error dialog
    private boolean getErrorDlg(String p_message)
    {
        String[] btnLables = (String[])m_dialogInfo.elementAt(2);
        Hashtable hashtable = new Hashtable();
        hashtable.put(BTN_LABELS, btnLables);
        hashtable.put(MESSAGE, parseText(p_message));

        return MessageDialog.getMessageDialog(getParentFrame(), "", hashtable, AbstractEnvoyDialog.ERROR_TYPE);
    }

    // popup the error dialog
    private boolean getPromptDlg(String p_message)
    {
        String[] btnLables = (String[])m_dialogInfo.elementAt(2);
        Hashtable hashtable = new Hashtable();
        hashtable.put(BTN_LABELS, btnLables);
        hashtable.put(MESSAGE, p_message);//parseText(p_message));

        return MessageDialog.getMessageDialog(getParentFrame(), "", hashtable);
    }

    // parse the message (word wrapping).
    private String parseText(String p_text)
    {
        EnvoyWordWrapper wrap = new EnvoyWordWrapper();
        return "";//wrap.parseText(p_text, m_descField);
    }

    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Helper Methods
    //////////////////////////////////////////////////////////////////////////////////
    public GridData getRoleInfo(String p_activityName, boolean p_isUser)
    {
        // go to the servlet to filter the grid info.
        Vector request = new Vector();
        request.addElement(p_isUser ? "user" : "role");
        request.addElement(p_activityName);
        Vector inputFromServlet =
            getEnvoyApplet().appendDataToPostConnection(request, "null");
        return(GridData)inputFromServlet.elementAt(0);
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Helper Methods  ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Methods to be Overridden by the subclass  ////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    protected Vector addWorkflowTasks()
    {
        Vector workflowTasks = new Vector();
        for (int i = 1 ; i <= m_grid.getNumRows() ; i++)
        {
            WorkflowTaskInstance data = 
                (WorkflowTaskInstance)m_grid.getCellData(6, i);
            // update sequence (due to a moveUp, moveDown, or insert)
            data.setSequence(i - 1);
            workflowTasks.addElement(data);
        }
        return workflowTasks;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Methods to be Overridden by the subclass
    //////////////////////////////////////////////////////////////////////////////////
}

