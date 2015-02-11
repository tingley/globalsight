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
// Comment text

package com.globalsight.everest.webapp.applet.admin.users;

import CoffeeTable.Grid.GridAdapter;
import CoffeeTable.Grid.GridData;
import CoffeeTable.Grid.GridDataInterface;
import CoffeeTable.Grid.GridEvent;
import CoffeeTable.Grid.GridPanel;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.applet.common.EnvoyButton;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyGrid;
import com.globalsight.everest.webapp.applet.common.EnvoyLabel;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;
import com.globalsight.everest.webapp.applet.common.MessageDialog;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Hashtable;
import java.util.Vector;

public class LocalePairPanel extends EnvoyGrid
{
    private GridPanel m_grid = null;
    private Vector m_dialogInfo = null;
    private Object m_dataObject = null;

    public LocalePairPanel() {
        this.setLayout(new EnvoyLineLayout(0, 0, 0, 0));
        this.setBackground(ENVOY_WHITE);
    }

    /**
     * Returns the name of the dialog. Necessary to implement EnvoyGrid interface.
     */
    public String getTitle() {
        return "";
    }

    /**
     * Populate the data of the grid panel.
     * @param p_objects A Vector containing, as its first element, an array of Strings
     *                  containing headers for the applet grid; as its second element,
     *                  an array of Strings containing labels for the applet buttons;
     *                  as its third element, a GridData object containing the data
     *                  model for the applet grid; as its fourth element, a Vector
     *                  containing localized Strings for the info dialog.
     */
    public void populate(Vector p_objects) {
        String[] headers = (String[]) p_objects.elementAt(0);
        String[] labels = (String[]) p_objects.elementAt(1);
        GridDataInterface gridData = (GridDataInterface) p_objects.elementAt(2);
        m_dialogInfo = (Vector) p_objects.elementAt(3);

        // grid
        m_grid = new GridPanel();
        m_grid.setNumCols(headers.length);
        if (gridData == null)
        {
            // we're in create mode
            m_grid.setNumRows(0);
        } else
        {
            m_grid.setNumRows(gridData.getNumRows());
            m_grid.setGridData(gridData, true);
        }
        // set column headers.
        m_grid.setColHeaders(headers);
        // get rid of row header.
        m_grid.setRowHeaderWidth(0);
        // set the numbering
        m_grid.setRowNumbers(false);
        // set the flags for center justification.
        m_grid.setGridJustification(GridPanel.JUST_CENTER);
        // configure resizing.
        m_grid.setResizeColumns(true);
        m_grid.setResizeRows(false);
        m_grid.setResizeColHeaders(true);
        m_grid.setResizeRowHeaders(false);
        m_grid.setAutoResizeColumns(true);

        //m_grid.autoResizeRows(true);
        // enable selection options.
        m_grid.setCellSelection(false);
        m_grid.setRowSelection(true);
        m_grid.setColSelection(false);
        m_grid.setMultipleSelection(false);
        // set display the options.
        setDisplayOptions(m_grid);
        
	//disable sorting
        m_grid.setSortEnable(true);
	m_grid.sortRows(1);

        //////////////////////////////////////////////////////////////////////////////
        // BEGIN: Create the buttons. ////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////

        // the buttons.
        final EnvoyButton newButton = new EnvoyButton(labels[0]);
        int widthNew = GlobalEnvoy.getStringWidth(labels[0]) + 10;

        final EnvoyButton editButton = new EnvoyButton(labels[1]);
        int widthEdit = GlobalEnvoy.getStringWidth(labels[1]) + 10;

        final EnvoyButton removeButton = new EnvoyButton(labels[2]);
        int widthRemove = GlobalEnvoy.getStringWidth(labels[2]) + 10;

        // set enable.
        editButton.setEnabled(false);
        removeButton.setEnabled(false);


        // button listeners.
        newButton.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent event) {
                                            Vector vDataToBeSent = new Vector();
                                            vDataToBeSent.addElement("new");
                                            getEnvoyApplet().appendDataToPostConnection(vDataToBeSent, "newURL");
                                        }
                                    });

        editButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent event) {
                                               performModify();
                                           }
                                       });

        removeButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               if (m_grid.getFirstSelectedRow() > 0)
                                               {
                                                   Vector vRowData = m_grid.getRowData(m_grid.getFirstSelectedRow());

                                                   if (getRemoveConfirmDlg())
                                                   {
                                                       Vector vDataToBeSent = new Vector();
                                                       vDataToBeSent.addElement("remove");
                                                       vDataToBeSent.addElement(vRowData.elementAt(0).toString());
                                                       vDataToBeSent.addElement(vRowData.elementAt(1).toString());
                                                       vDataToBeSent.addElement(vRowData.elementAt(2).toString());
                                                       vDataToBeSent.addElement(vRowData.elementAt(3).toString());

                                                       m_grid.deleteRow(m_grid.getFirstSelectedRow());

                                                       getEnvoyApplet().appendDataToPostConnection(vDataToBeSent, "null");
                                                   }
                                               }
                                           }
                                       });

        m_grid.addGridListener(new GridAdapter() {
                                   public void gridDoubleClicked(GridEvent e) {
                                       if (m_grid.getFirstSelectedRow() > 0)
                                       {
                                           performModify();
                                       }
                                   }

                                   public void gridSelChanged(GridEvent e) {
                                       if (m_grid.getFirstSelectedRow() > 0)
                                       {
                                           editButton.setEnabled(true);
                                           removeButton.setEnabled(true);
                                       } else
                                       {
                                           editButton.setEnabled(false);
                                           removeButton.setEnabled(false);
                                       }
                                   }
                               });


        this.add(getBorderedGridPanel(m_grid),
                 new EnvoyConstraints(GRID_WIDTH, GRID_HEIGHT, 1,
                                      EnvoyConstraints.LEFT,
                                      EnvoyConstraints.X_NOT_RESIZABLE,
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.END_OF_LINE));
        this.add(editButton,
                 new EnvoyConstraints(widthEdit, 24, 1, 
                                      EnvoyConstraints.LEFT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));

        this.add(new EnvoyLabel(),
                 new EnvoyConstraints(5, 24, 1, EnvoyConstraints.RIGHT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.NOT_END_OF_LINE));
        this.add(newButton,
                 new EnvoyConstraints(widthNew, 24, 1, 
                                      EnvoyConstraints.LEFT,
                                      EnvoyConstraints.X_NOT_RESIZABLE, 
                                      EnvoyConstraints.Y_NOT_RESIZABLE,
                                      EnvoyConstraints.END_OF_LINE));


    }

    // perform modify functionality.
    private void performModify()
    {
        if (m_grid.getFirstSelectedRow() > 0)
        {
            Vector vRowData = m_grid.getRowData(m_grid.getFirstSelectedRow());
            Vector vDataToBeSent = new Vector();
            vDataToBeSent.addElement("modify");
            vDataToBeSent.addElement(vRowData.elementAt(0).toString());
            vDataToBeSent.addElement(vRowData.elementAt(1).toString());
            vDataToBeSent.addElement(vRowData.elementAt(2).toString());
            vDataToBeSent.addElement(vRowData.elementAt(3).toString());
            getEnvoyApplet().appendDataToPostConnection(vDataToBeSent, "modifyURL");
        }
    }


    /**
     * Get the dialog for confirming removal of a user from the system.
     */
    private boolean getRemoveConfirmDlg() {
        String btnLables[] = (String[]) m_dialogInfo.elementAt(1);
        String promptMsgs[] = (String[]) m_dialogInfo.elementAt(2);

        String label = promptMsgs[0];
        String message = promptMsgs[1];

        Hashtable hashtable = new Hashtable();
        hashtable.put(BTN_LABELS, btnLables);
        hashtable.put(MESSAGE, message);

        return MessageDialog.getMessageDialog(getParentFrame(), label, hashtable);
    }
}



