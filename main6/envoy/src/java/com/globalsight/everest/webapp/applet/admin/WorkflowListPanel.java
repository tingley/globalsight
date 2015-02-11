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
package com.globalsight.everest.webapp.applet.admin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Panel;
import java.awt.Label;
import java.awt.TextField;
import java.awt.TextArea;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.Point;

// Grid
import CoffeeTable.Grid.GridAttributes;
import CoffeeTable.Grid.GridDataInterface;
import CoffeeTable.Grid.GridData;
import CoffeeTable.Grid.GridPanel;
import CoffeeTable.Grid.GridAdapter;
import CoffeeTable.Grid.GridEvent;
// globalsight
import com.globalsight.everest.webapp.applet.common.*;
import com.globalsight.everest.webapp.applet.util.WorkflowInstanceAux;

public class WorkflowListPanel extends EnvoyGrid 
{
    private boolean m_isCreateMode = false;
    private GridPanel m_grid = null;
    private Vector m_dialogInfo = null;
    private Object m_dataObject = null;
    private Boolean m_isDispatchable = null;
    private String[] m_dlgTitles = null;

    private Button m_dispatchButton = null;
    private Button m_discardButton = null;
    private Button m_exportButton = null;
    private Button m_editButton = null;
    private Button m_statusButton = null;

    /**
     * Populate the data of the grid panel.
     * @param p_objects - The data used for populating the panel components.
     */
    public void populate(Vector p_objects) {
	String[] headers = (String[]) p_objects.elementAt(0);
        String[] labels = (String[]) p_objects.elementAt(1);
        m_dlgTitles = (String[]) p_objects.elementAt(2);
        Vector v = (Vector) p_objects.elementAt(3);
        m_isDispatchable = (Boolean) v.elementAt(0);
        GridDataInterface gridData = (GridDataInterface) v.elementAt(1);
	m_dialogInfo = (Vector) p_objects.elementAt(4);

        createGridPanel(headers, labels, m_dlgTitles);

        m_grid.setNumCols(headers.length);
        if (gridData == null) {
            // we're in create mode
            m_grid.setNumRows(0);
        } else {
            m_grid.setNumRows(gridData.getNumRows());
        }

        // Set the grid lines to red.
        m_grid.setLineColor(Color.red);

        // enable selection options.
        m_grid.setCellSelection(false);
        m_grid.setRowSelection(true);
        m_grid.setColSelection(false);
        m_grid.setMultipleSelection(false);

        // set column headers.
        m_grid.setColWidths("50, 100, 100, 100");

        // set column attributes.
        for (int i = 0; i < headers.length; i++) {
            m_grid.setColAttributes(i + 1, 
                                    new GridAttributes(this, CELL_FONT, 
                                                       ENVOY_BLACK, 
                                                       ENVOY_WHITE,
                                                       GridPanel.JUST_LEFT | 
                                                       GridPanel.WORD_WRAP, 
                                                       GridPanel.TEXT), true);            
        }

        if (gridData != null) {
            m_grid.setGridData(gridData, true);
        }

	// configure resizing.
	m_grid.setResizeColumns(true);
	m_grid.setResizeRows(false);
	m_grid.setResizeColHeaders(true);
	m_grid.setResizeRowHeaders(false);
        m_grid.setAutoResizeColumns(true);
        m_grid.autoResizeRows(true);
	m_grid.setColHeaders(headers);
	m_grid.setRowNumbers(false);

        //disable sorting
        m_grid.setSortEnable(false);

        enableButtonControls(false);
    }

    /**
     * Get the title of the grid.
     * @return The grid's title.
     */
    public String getTitle() {
        return "Workflow Instances";
    }

    private void enableButtonControls(boolean p_enable) {
        m_dispatchButton.setEnabled(p_enable);
        m_discardButton.setEnabled(p_enable);
        m_exportButton.setEnabled(p_enable);
        m_editButton.setEnabled(p_enable);
        m_statusButton.setEnabled(p_enable);
    }

    /**
     * Create the panel of buttons that lives inside the grid.
     */
    private void createGridPanel(String[] p_headers, String[] p_labels, String[] p_dlgTitles) {
        setLayout(new EnvoyLineLayout(5, 5, 5, 5));

	// grid
        m_grid = new GridPanel();
        m_grid.addGridListener(new GridAdapter() {
		public void gridDoubleClicked(GridEvent e) {
		    if (m_grid.getFirstSelectedRow() > 0) {
                    }
		}

                public void gridSelChanged(GridEvent e) {
                    if (m_grid.getFirstSelectedRow() > 0) {
                        enableButtonControls(true);
                    } else {
                        enableButtonControls(false);
                    }
                }
	    });

        // grid buttons
        Panel gridButtonPanel = new Panel(new EnvoyLineLayout(2,2,2,2));

        String dispatchLabel = p_labels[0];
        String discardLabel = p_labels[1];
        String exportLabel = p_labels[2];
        String modifyLabel = p_labels[3];
        String statusLabel = p_labels[4];

        // Create buttons.
        m_dispatchButton = new Button(dispatchLabel);
        m_discardButton = new Button(discardLabel);
        m_exportButton = new Button(exportLabel);
        m_editButton = new Button(modifyLabel);
        m_statusButton = new Button(statusLabel);

        Vector btnList = new Vector();
        btnList.addElement(dispatchLabel);
        btnList.addElement(discardLabel);
        btnList.addElement(exportLabel);
        btnList.addElement(modifyLabel);
        btnList.addElement(statusLabel);

        int result = GlobalEnvoy.getStringWidth(btnList);
        int buttonSize = result > 65 ? (result + 10) : 70;

        // place buttons on button panel.
        gridButtonPanel.add(m_grid, new EnvoyConstraints(80, 280, 1, EnvoyConstraints.LEFT,
                                                         EnvoyConstraints.X_RESIZABLE,
							 EnvoyConstraints.Y_RESIZABLE,
							 EnvoyConstraints.END_OF_LINE));
        gridButtonPanel.add(m_dispatchButton, new EnvoyConstraints(buttonSize, 24, 1, EnvoyConstraints.RIGHT,
                                                            EnvoyConstraints.X_NOT_RESIZABLE,
							    EnvoyConstraints.Y_NOT_RESIZABLE,
							    EnvoyConstraints.NOT_END_OF_LINE));
        gridButtonPanel.add(m_discardButton, new EnvoyConstraints(buttonSize, 24, 1, EnvoyConstraints.RIGHT,
							     EnvoyConstraints.X_NOT_RESIZABLE,
							     EnvoyConstraints.Y_NOT_RESIZABLE,
							     EnvoyConstraints.NOT_END_OF_LINE));
        gridButtonPanel.add(m_exportButton, new EnvoyConstraints(buttonSize, 24, 1, EnvoyConstraints.RIGHT,
                                                               EnvoyConstraints.X_NOT_RESIZABLE,
							       EnvoyConstraints.Y_NOT_RESIZABLE,
							       EnvoyConstraints.NOT_END_OF_LINE));
        gridButtonPanel.add(m_editButton, new EnvoyConstraints(buttonSize, 24, 1, EnvoyConstraints.RIGHT,
                                                               EnvoyConstraints.X_NOT_RESIZABLE,
							       EnvoyConstraints.Y_NOT_RESIZABLE,
							       EnvoyConstraints.NOT_END_OF_LINE));
        gridButtonPanel.add(m_statusButton, new EnvoyConstraints(buttonSize, 24, 1, EnvoyConstraints.RIGHT,
                                                               EnvoyConstraints.X_NOT_RESIZABLE,
							       EnvoyConstraints.Y_NOT_RESIZABLE,
							       EnvoyConstraints.END_OF_LINE));

        final String dispatchWorkflowLabel = m_dlgTitles[0];
        final String statusWorkflowLabel = m_dlgTitles[1];

        // button listeners.
        m_dispatchButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    EnvoyApplet applet = ((EnvoyApplet) GlobalEnvoy.getParentComponent());

                    if (m_grid.getFirstSelectedRow() > 0) {
                        Vector vRowData = m_grid.getRowData(m_grid.getFirstSelectedRow());
                        WorkflowInstanceAux aux = (WorkflowInstanceAux) vRowData.elementAt(0);

                        if (getStatusDialogValues(aux, dispatchWorkflowLabel)) {

                            m_grid.deleteRow(m_grid.getFirstSelectedRow());
                            aux.setAction(WorkflowInstanceAux.DO_DISPATCH);
                            //Serializable[] data = { aux };
			    Vector data = new Vector();
			    data.addElement(aux);
			    applet.appendDataToPostConnection(data, "null");
                        }
                    }
		}
	    });
        m_discardButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		}
	    });
        m_exportButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		}
	    });
        m_editButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    EnvoyApplet applet = ((EnvoyApplet) GlobalEnvoy.getParentComponent());

                    if (m_grid.getFirstSelectedRow() > 0) {
                        Vector vRowData = m_grid.getRowData(m_grid.getFirstSelectedRow());
                        WorkflowInstanceAux aux = (WorkflowInstanceAux) vRowData.elementAt(0);

                        aux.setAction(WorkflowInstanceAux.DO_MODIFY);

                        //Serializable[] data = { aux };
			Vector data = new Vector();
			data.addElement(aux);
			applet.appendDataToPostConnection(data, "modifyURL");
                    }
		}
	    });
        m_statusButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    EnvoyApplet applet = ((EnvoyApplet) GlobalEnvoy.getParentComponent());

                    if (m_grid.getFirstSelectedRow() > 0) {
                        Vector vRowData = m_grid.getRowData(m_grid.getFirstSelectedRow());
                        WorkflowInstanceAux aux = (WorkflowInstanceAux) vRowData.elementAt(0);

                        getStatusDialogValues(aux, statusWorkflowLabel);
                    }

                    // No need to send the object back to the pagehandler, since all
                    // we're doing is displaying data in the dialog.
                }
            });

        add(gridButtonPanel, new EnvoyConstraints(80, 240, 1, EnvoyConstraints.LEFT,
                                                  EnvoyConstraints.X_RESIZABLE,
						  EnvoyConstraints.Y_NOT_RESIZABLE,
						  EnvoyConstraints.END_OF_LINE));

        if (m_isDispatchable.booleanValue()) {
            m_dispatchButton.setEnabled(true);
        } else {
            m_dispatchButton.setEnabled(false);
        }
    }
    // popup the status dialog
    private boolean getStatusDialogValues(WorkflowInstanceAux aux, String dlgTitle) {
	String[] labels = (String[]) m_dialogInfo.elementAt(0);
	String[] btnLabels = (String[]) m_dialogInfo.elementAt(1);

        Hashtable hashtable = new Hashtable();
	hashtable.put(EnvoyAppletConstants.LABELS, labels);
        hashtable.put(EnvoyAppletConstants.BTN_LABELS, btnLabels);

        if (aux != null) {

            String[] values = { (new Long(aux.getWorkflowId())).toString(),
                                aux.getL10nProfileName(),
                                aux.getState(),
                                aux.getSourceLocale().getDisplayName(),
                                aux.getTargetLocale().getDisplayName() };

            hashtable.put(EnvoyAppletConstants.VALUES, values);
        }

        int type = 0;

        if (dlgTitle == m_dlgTitles[0]) {
            type = 1;
        }

        return WorkflowStatusDialog.getWorkflowStatusDialog(this, dlgTitle, hashtable, type);
    }

}


