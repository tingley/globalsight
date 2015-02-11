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

import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;
import com.globalsight.everest.servlet.ExceptionMessage;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyButton;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyFonts;
import com.globalsight.everest.webapp.applet.common.EnvoyGrid;
import com.globalsight.everest.webapp.applet.common.EnvoyJTable;
import com.globalsight.everest.webapp.applet.common.EnvoyLabel;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;
import com.globalsight.everest.webapp.applet.common.MessageDialog;
import com.globalsight.util.collections.HashtableValueOrderWalker;

/**
 * The DBColumnPanel, designed to add, modify, and remove db columns.
 */

public class DBColumnPanel extends EnvoyGrid
{
    private static final int NEW = 1;
    private static final int MODIFY = 2;
    private static final int REMOVE = 4;
    private List<Object[]> m_data = null;
    // the db profile table
    private EnvoyJTable m_dbProfileTable;
    private String m_title;
    private String[] m_dialogLabels = null;
    private String[] m_dialogButtons = null;
    private Hashtable<String, String> m_i18nContents = null;
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
     * 
     * @param p_objects
     *            - The data used for populating the panel components.
     */
    public void populate(Vector p_objects)
    {
        setLayout(new EnvoyLineLayout(0, 0, 0, 0));
        this.setBackground(ENVOY_WHITE);
        String[] labels = (String[]) p_objects.elementAt(0);
        String[] header = (String[]) p_objects.elementAt(1);
        m_data = (List<Object[]>) p_objects.elementAt(2);
        m_dialogLabels = (String[]) p_objects.elementAt(3);
        m_dialogButtons = (String[]) p_objects.elementAt(4);
        final String[] imageNames = (String[]) p_objects.elementAt(5);
        m_mode_pairs = (HashtableValueOrderWalker) p_objects.elementAt(6);
        m_knownFormatType_pairs = (HashtableValueOrderWalker) p_objects
                .elementAt(7);
        m_xmlRule_pairs = (HashtableValueOrderWalker) p_objects.elementAt(8);
        m_i18nContents = (Hashtable<String, String>) p_objects.elementAt(9);
        m_title = m_dialogLabels[0];

        // create db profile table
        createDBProfileTable(header, m_data);
        JScrollPane dbProfileSp = new JScrollPane(m_dbProfileTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // Create Buttons.
        newButton = new EnvoyButton(labels[0]);
        int widthNew = GlobalEnvoy.getStringWidth(labels[0]) + 10;

        editButton = new EnvoyButton(labels[1]);
        int widthEdit = GlobalEnvoy.getStringWidth(labels[1]) + 10;

        removeButton = new EnvoyButton(labels[2]);
        int widthRemove = GlobalEnvoy.getStringWidth(labels[2]) + 10;

        previousButton = new EnvoyButton(labels[3]);
        int widthPrevious = GlobalEnvoy.getStringWidth(labels[3]) + 10;

        cancelButton = new EnvoyButton(labels[4]);
        int widthCancel = GlobalEnvoy.getStringWidth(labels[4]) + 10;

        nextButton = new EnvoyButton(labels[5]);
        int widthNext = GlobalEnvoy.getStringWidth(labels[5]) + 10;

        m_upButton = new EnvoyButton(labels[6]);
        int widthUpButton = GlobalEnvoy.getStringWidth(labels[6]) + 10;
        // int widthUpButton = 200;

        m_downButton = new EnvoyButton(labels[7]);
        int widthDownButton = GlobalEnvoy.getStringWidth(labels[7]) + 10;

        int arrowWidth = widthUpButton > widthDownButton ? widthUpButton
                : widthDownButton;

        setEnableButtons();

        previousButton.setEnabled(true);
        previousButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                getEnvoyApplet().appendDataToPostConnection(null, "pre3URL");
            }
        });
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                getEnvoyApplet().appendDataToPostConnection(null, "cancel3URL");
            }
        });
        if (m_dbProfileTable.getRowCount() > 0)
            nextButton.setEnabled(true);
        else
            nextButton.setEnabled(false);
        nextButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                getEnvoyApplet().appendDataToPostConnection(null, "next3URL");
            }
        });

        // "newButton" listeners.
        newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // performAction(NEW);
                update(NEW);
            }
        });

        // "editButton" listeners.
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                update(MODIFY);
            }
        });

        // "removeButton" listeners.
        removeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (getRemoveConfirmDlg())
                {
                    update(REMOVE);
                }
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
                if (m_dbProfileTable.getSelectedRowCount() > 1
                        && m_dbProfileTable.getRowCount() > 1)
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
                if (m_dbProfileTable.getSelectedRowCount() < m_dbProfileTable
                        .getRowCount() && m_dbProfileTable.getRowCount() > 1)
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
        this.add(getBorderedGridPanel(dbProfileSp), new EnvoyConstraints(
                GRID_WIDTH, GRID_HEIGHT, 1, EnvoyConstraints.LEFT,
                EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(getArrowPanel(arrowVector, arrowWidth), new EnvoyConstraints(
                arrowWidth, GRID_HEIGHT, 1, EnvoyConstraints.LEFT,
                EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE, EnvoyConstraints.END_OF_LINE));
        this.add(cancelButton, new EnvoyConstraints(widthCancel, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(), new EnvoyConstraints(5, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(removeButton, new EnvoyConstraints(widthRemove, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(), new EnvoyConstraints(5, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(editButton, new EnvoyConstraints(widthEdit, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(), new EnvoyConstraints(5, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(newButton, new EnvoyConstraints(widthNew, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));

        this.add(new EnvoyLabel(), new EnvoyConstraints(arrowWidth, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE, EnvoyConstraints.END_OF_LINE));
        this.add(previousButton, new EnvoyConstraints(widthPrevious, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(new EnvoyLabel(), new EnvoyConstraints(5, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        this.add(nextButton, new EnvoyConstraints(widthNext, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));

        this.add(new EnvoyLabel(), new EnvoyConstraints(arrowWidth, 24, 1,
                EnvoyConstraints.RIGHT, EnvoyConstraints.X_NOT_RESIZABLE,
                EnvoyConstraints.Y_NOT_RESIZABLE, EnvoyConstraints.END_OF_LINE));
    }

    /**
     * Creates the table that displays the db column profiles.
     * 
     * @param data
     * @param header
     */
    private void createDBProfileTable(String[] columns, List<Object[]> data)
    {
        DBProfileTableModel model = new DBProfileTableModel(columns, data);

        m_dbProfileTable = new EnvoyJTable(model);
        m_dbProfileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_dbProfileTable.setRowHeight(20);
        m_dbProfileTable.setFont(EnvoyFonts.getCellFont());
        m_dbProfileTable.setShowVerticalLines(false);
        m_dbProfileTable.setGridColor(ENVOY_BLUE);
        m_dbProfileTable.setSelectionBackground(ENVOY_BLUE);
        m_dbProfileTable.setSelectionForeground(ENVOY_WHITE);
        m_dbProfileTable.setOpaque(false);

        JTableHeader header = m_dbProfileTable.getTableHeader();
        header.setDefaultRenderer(new DBProfileTableHeaderCellRenderer());
        header.setUpdateTableInRealTime(true);
        header.setReorderingAllowed(false);
        // add table listener
        m_dbProfileTable.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e)
                        && e.getClickCount() == 2
                        && m_dbProfileTable.getSelectedRow() > -1)
                {
                    update(MODIFY);
                }
                if (SwingUtilities.isLeftMouseButton(e)
                        && e.getClickCount() == 1
                        && m_dbProfileTable.getSelectedRow() > -1)
                {
                    setEnableButtons();
                }
            }
        });
    }

    /**
     * Get the title of the grid. Not used, but required because of abstract
     * super class EnvoyGrid
     * 
     * @return The grid's title.
     */
    public String getTitle()
    {
        return m_title;
    }

    private DatabaseColumnImpl getSelectedDBColumn()
    {
        Map<Integer, Object[]> dbProfiles = ((DBProfileTableModel) m_dbProfileTable
                .getModel()).getDBProfiles();
        Object[] dbProfile = dbProfiles.get(m_dbProfileTable.getSelectedRow());
        if (dbProfile != null)
        {
            return (DatabaseColumnImpl) dbProfile[0];
        }
        return null;
    }

    /**
     * Get the dialog for confirming removal of a user from the system.
     */
    private boolean getRemoveConfirmDlg()
    {
        String btnLables[] =
        { m_dialogButtons[5], m_dialogButtons[5], m_dialogButtons[5],
                m_dialogButtons[3], m_dialogButtons[4] }; // ok, cancel
        Hashtable hashtable = new Hashtable();
        hashtable.put(EnvoyAppletConstants.BTN_LABELS, btnLables);
        hashtable.put(EnvoyAppletConstants.MESSAGE, m_dialogLabels[8]);
        return MessageDialog.getMessageDialog(getParentFrame(),
                m_dialogLabels[7], hashtable);
    }

    private void updateGrid(int p_command, DatabaseColumnImpl p_dbcolumn)
    {
        Integer integer;
        DBProfileTableModel model = (DBProfileTableModel) m_dbProfileTable
                .getModel();
        Map<Integer, Object[]> dbProfiles = model.getDBProfiles();
        switch (p_command)
        {
            case NEW: // Adding an dbcolumn.
                Object[] newProfile = new Object[5];
                newProfile[0] = p_dbcolumn;
                newProfile[1] = p_dbcolumn.getLabel();
                newProfile[2] = p_dbcolumn.getTableName();
                integer = new Integer((int) p_dbcolumn.getFormatType());
                newProfile[3] = m_knownFormatType_pairs.get(integer);
                integer = new Integer((int) p_dbcolumn.getContentMode());
                newProfile[4] = m_mode_pairs.get(integer);
                dbProfiles.put(m_dbProfileTable.getRowCount(), newProfile);
                model.updateTableModel(dbProfiles);
                break;
            case MODIFY: // Modifying an dbcolumn.
                Object[] modifiedProfile = new Object[5];
                modifiedProfile[0] = p_dbcolumn;
                modifiedProfile[1] = p_dbcolumn.getLabel();
                modifiedProfile[2] = p_dbcolumn.getTableName();
                integer = new Integer((int) p_dbcolumn.getFormatType());
                modifiedProfile[3] = m_knownFormatType_pairs.get(integer);
                integer = new Integer((int) p_dbcolumn.getContentMode());
                modifiedProfile[4] = m_mode_pairs.get(integer);
                dbProfiles.put(m_dbProfileTable.getSelectedRow(),
                        modifiedProfile);
                model.updateTableModel(dbProfiles);
                break;
            case REMOVE: // Removing a dbcolumn.
                dbProfiles.remove(m_dbProfileTable.getSelectedRow());
                model.updateTableModel(new ArrayList<Object[]>(dbProfiles
                        .values()));
                break;
        }
    }

    // perfom action based on the button click.
    private Vector performAction(int p_command)
    {
        Hashtable hashtable = new Hashtable();
        hashtable.put(BTN_LABELS, m_dialogButtons);
        hashtable.put(LABELS, m_dialogLabels);
        hashtable.put(I18N_CONTENT, m_i18nContents);
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
                if (m_dbProfileTable.getRowCount() < 1)
                {
                    nextButton.setEnabled(false);
                }
                break;
        }
        return objs.size() > 1 ? getEnvoyApplet().appendDataToPostConnection(
                objs, "null") : null;
    }

    // perform the action and update the grid
    private void update(int p_command)
    {
        Vector result = performAction(p_command);
        if (result == null && p_command == REMOVE)
        {
            updateGrid(p_command, null);
        }
        else if (result != null
                && !(result.elementAt(0) instanceof ExceptionMessage))
        {
            updateGrid(p_command, (DatabaseColumnImpl) result.elementAt(1));
        }
        setEnableButtons();
    }

    // moves a row up
    private void moveRowUp()
    {
        int selectedRow = m_dbProfileTable.getSelectedRow();
        if (selectedRow > 0)
        {
            DBProfileTableModel model = (DBProfileTableModel) m_dbProfileTable
                    .getModel();
            model.moveUp(selectedRow);
            m_dbProfileTable.setRowSelectionInterval(selectedRow - 1,
                    selectedRow - 1);
        }
        // update the buttons.
        setEnableUpButton();
        setEnableDownButton();
    }

    // moves a row down
    private void moveRowDown()
    {
        int selectedRow = m_dbProfileTable.getSelectedRow();
        if (selectedRow > -1
                && selectedRow < m_dbProfileTable.getRowCount() - 1)
        {
            DBProfileTableModel model = (DBProfileTableModel) m_dbProfileTable
                    .getModel();
            model.moveDown(selectedRow);
            m_dbProfileTable.setRowSelectionInterval(selectedRow + 1,
                    selectedRow + 1);
        }
        // update the buttons.
        setEnableUpButton();
        setEnableDownButton();
    }

    // update the button status.
    private void setEnableButtons()
    {
        setEnableUpButton();
        setEnableDownButton();
        setEnableRemoveButton();
        setEnableEditButton();
    }

    // update the remove button status.
    private void setEnableRemoveButton()
    {
        removeButton.setEnabled(m_dbProfileTable.getSelectedRow() > -1);
    }

    // update the edit button status.
    private void setEnableEditButton()
    {
        editButton.setEnabled(m_dbProfileTable.getSelectedRow() > -1);
    }

    // update the up button status.
    private void setEnableUpButton()
    {
        m_upButton.setEnabled(m_dbProfileTable.getSelectedRow() > 0
                && m_dbProfileTable.getRowCount() > 1);
    }

    // update the down button status.
    private void setEnableDownButton()
    {
        m_downButton.setEnabled(m_dbProfileTable.getSelectedRow() > -1
                && m_dbProfileTable.getSelectedRow() < m_dbProfileTable
                        .getRowCount() - 1
                && m_dbProfileTable.getRowCount() > 1);
    }

    protected Panel getArrowPanel(Vector p_buttons, int arrowWidth)
    {
        Panel arrowPanel = new Panel(new EnvoyLineLayout(0, 0, 0, 0));
        arrowPanel.setBackground(ENVOY_WHITE);
        for (int i = 0; i < p_buttons.size(); i++)
        {
            if (p_buttons.elementAt(i) instanceof String)
            {
                arrowPanel
                        .add(new EnvoyLabel("", java.awt.Label.CENTER,
                                arrowWidth, ARROW_HEIGHT),
                                new EnvoyConstraints(
                                        arrowWidth,
                                        ARROW_HEIGHT,
                                        1,
                                        EnvoyConstraints.LEFT,
                                        EnvoyConstraints.X_NOT_RESIZABLE,
                                        ((String) p_buttons.elementAt(i))
                                                .equals("end") ? EnvoyConstraints.Y_RESIZABLE
                                                : EnvoyConstraints.Y_NOT_RESIZABLE,
                                        EnvoyConstraints.END_OF_LINE));
            }
            else
            {
                EnvoyButton arrowToBeAddedToPanel = (EnvoyButton) p_buttons
                        .elementAt(i);
                arrowPanel.add(arrowToBeAddedToPanel, new EnvoyConstraints(
                        arrowWidth, ARROW_HEIGHT, 1, EnvoyConstraints.LEFT,
                        EnvoyConstraints.X_NOT_RESIZABLE,
                        EnvoyConstraints.Y_NOT_RESIZABLE,
                        EnvoyConstraints.END_OF_LINE));
            }
        }
        return arrowPanel;
    }
}
