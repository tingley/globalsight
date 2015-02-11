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

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Hashtable;
import java.util.Vector;

import CoffeeTable.Grid.GridAdapter;
import CoffeeTable.Grid.GridAttributes;
import CoffeeTable.Grid.GridData;
import CoffeeTable.Grid.GridEvent;
import CoffeeTable.Grid.GridPanel;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.webapp.applet.common.AbstractEnvoyDialog;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.date.DateHelper;

/**
 * The activity dialog is used for creating a task for a workflow template based on:
 * 1. an activity name which is selected from the combo-box.
 * 2. Time to accept the task.
 * 3. Time for completing the task.
 */
public class ActivityDialog extends AbstractEnvoyDialog implements
        EnvoyAppletConstants
{
    private static final long serialVersionUID = -5543222055447535060L;

    //
    // PRIVATE MEMBER VARIABLES
    //
    private Choice m_activityCombo;
    private TextField m_daysToAccept;
    private TextField m_hoursToAccept;
    private TextField m_minutesToAccept;
    private TextField m_daysToComplete;
    private TextField m_hoursToComplete;
    private TextField m_minutesToComplete;
    private String m_rateDefaultChoice; //the default rate name "Choose.."
    private Choice m_rateChoice;
    private Hashtable m_allRates; // all rates associated with the source/target pair
    private long m_selectedRateId = -1;      //for modified rates
    private Checkbox m_userCheckBox;
    private Checkbox m_roleCheckBox;
    private Vector m_values = new Vector();
    private ActivityParentable m_parent;
    private GridPanel m_grid;
    private boolean m_taskIsAccepted = false;
    private boolean m_costingEnabled;
    private boolean m_initialRoleType;
    private String m_role;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Create a new ActivityDialog.
     * @param p_parent - The parent frame component.
     * @param p_title - The title of the dialog.
     * @param p_hashtable - Contains the labels, list of activities, and the default values.
     */
    public ActivityDialog(ActivityParentable p_parent,
                          String p_title,
                          Hashtable p_hashtable)
    {
        super(p_parent.getParentFrame(), p_title, p_hashtable);
        m_parent = p_parent;
        populateDialog();
        // the ok button should be initially disabled
        updateButtonStatus(isDirty());
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Get the panel that should be displayed in this dialog.
     * @return The editor panel.
     */
    public Panel getEditorPanel()
    {
        final String[] labels = (String[])getValue(LABELS);
        final Panel panel = new Panel(new EnvoyLineLayout(5, 5, 5, 5));

        m_costingEnabled = ((Boolean)getValue(COSTING_ENABLED)).booleanValue();
        if (m_costingEnabled)
        {
            m_rateDefaultChoice = labels[21];
        }

        Label msgLabel =
            new Label((getValue(VALUES) == null ? 
                       labels[0] : labels[1]),
                      Label.LEFT);
        msgLabel.setFont(TITLE_FONT);
        panel.add(msgLabel,
                  new EnvoyConstraints(450, 25, 1, EnvoyConstraints.LEFT, EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE, EnvoyConstraints.END_OF_LINE));

        // activity label and combobox
        Vector activities = (Vector)getValue(ACTIVITIES);
        m_activityCombo = new Choice();
        m_activityCombo.addItem(labels[8]);
        for (int i=0; i< activities.size(); i++)
        {
            m_activityCombo.addItem(activities.elementAt(i).toString());
        }
        m_activityCombo.
            addItemListener(new ItemListener()
                            {
                                public void itemStateChanged(ItemEvent e)
                                {
                                    // first populate the grid based on selection
                                    populateRoleGrid();
                                    populateRateDropDown();
                                    updateButtonStatus(isDirty());
                                }
                            });
        Label activityLabel = new Label(labels[2]);
        activityLabel.setFont(HEADER_FONT);
        panel.add(activityLabel,
                  new EnvoyConstraints(getDialogWidth()/3, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_activityCombo,
                  new EnvoyConstraints(getDialogWidth()/2, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(new Label(),
                  new EnvoyConstraints(16, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.END_OF_LINE));


        // time to accept labels and input fields
        m_daysToAccept = new TextField();
        m_daysToAccept.setEditable(true);
        m_daysToAccept.
            addTextListener(new TextListener()
                            {
                                public void textValueChanged(TextEvent e)
                                {
                                    updateButtonStatus(isDirty());
                                }
                            });

        m_hoursToAccept = new TextField();
        m_hoursToAccept.setEditable(true);
        m_hoursToAccept.
            addTextListener(new TextListener()
                            {
                                public void textValueChanged(TextEvent e)
                                {
                                    updateButtonStatus(isDirty());
                                }
                            });

        m_minutesToAccept = new TextField();
        m_minutesToAccept.setEditable(true);
        m_minutesToAccept.
            addTextListener(new TextListener()
                            {
                                public void textValueChanged(TextEvent e)
                                {
                                    updateButtonStatus(isDirty());
                                }
                            });

        Label timeToAcceptLabel = new Label(labels[9]);
        timeToAcceptLabel.setFont(HEADER_FONT);
        Label dayAcceptLabel = new Label(labels[10], Label.LEFT);
        Label hourAcceptLabel = new Label(labels[11], Label.LEFT);
        Label minuteAcceptLabel = new Label(labels[12], Label.LEFT);

        panel.add(timeToAcceptLabel,
                  new EnvoyConstraints(getDialogWidth()/3, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_daysToAccept,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(dayAcceptLabel,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.CENTER,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_hoursToAccept,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(hourAcceptLabel,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.CENTER,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_minutesToAccept,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(minuteAcceptLabel,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.CENTER,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.END_OF_LINE));

        // time to complete labels and input fields
        m_daysToComplete = new TextField();
        m_daysToComplete.setEditable(true);
        m_daysToComplete.
            addTextListener(new TextListener()
                            {
                                public void textValueChanged(TextEvent e)
                                {
                                    updateButtonStatus(isDirty());
                                }
                            });

        m_hoursToComplete = new TextField();
        m_hoursToComplete.setEditable(true);
        m_hoursToComplete.
            addTextListener(new TextListener()
                            {
                                public void textValueChanged(TextEvent e)
                                {
                                    updateButtonStatus(isDirty());
                                }
                            });

        m_minutesToComplete = new TextField();
        m_minutesToComplete.setEditable(true);
        m_minutesToComplete.
            addTextListener(new TextListener()
                            {
                                public void textValueChanged(TextEvent e)
                                {
                                    updateButtonStatus(isDirty());
                                }
                            });

        Label timeToCompleteLabel = new Label(labels[3], Label.LEFT);
        timeToCompleteLabel.setFont(HEADER_FONT);
        Label dayCompleteLabel = new Label(labels[10], Label.LEFT);
        Label hourCompleteLabel = new Label(labels[11], Label.LEFT);
        Label minuteCompleteLabel = new Label(labels[12], Label.LEFT);

        panel.add(timeToCompleteLabel,
                  new EnvoyConstraints(getDialogWidth()/3, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_daysToComplete,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(dayCompleteLabel,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.CENTER,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_hoursToComplete,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(hourCompleteLabel,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.CENTER,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(m_minutesToComplete,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_NOT_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        panel.add(minuteCompleteLabel,
                  new EnvoyConstraints(getDialogWidth()/12, 24, 1,
                                       EnvoyConstraints.CENTER,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.END_OF_LINE));


        ///check if costing enabled
        if (m_costingEnabled)
        {
            // RATE TYPE
            m_rateChoice = new Choice();
            m_allRates = (Hashtable)getValue(RATES);
            populateRateDropDown();
            m_rateChoice.addItemListener(new ItemListener()
                {
                    public void itemStateChanged(ItemEvent e) 
                    {
                        if (e.getStateChange() == e.SELECTED)
                        {
                            updateButtonStatus(isDirty());
                        }
                    }
                });

            Label rateLabel = new Label(labels[13], Label.LEFT);
            rateLabel.setFont(HEADER_FONT);
            panel.add(rateLabel,
                      new EnvoyConstraints(getDialogWidth() /3, 24, 1, 
                                           EnvoyConstraints.LEFT,
                                           EnvoyConstraints.X_NOT_RESIZABLE, 
                                           EnvoyConstraints.Y_NOT_RESIZABLE,
                                           EnvoyConstraints.NOT_END_OF_LINE));
            panel.add(m_rateChoice, 
                      new EnvoyConstraints(getDialogWidth() / 3, 24, 1, 
                                           EnvoyConstraints.CENTER,
                                           EnvoyConstraints.X_RESIZABLE, 
                                           EnvoyConstraints.Y_NOT_RESIZABLE,
                                           EnvoyConstraints.END_OF_LINE));
        }

        // Create the radio button panel along with needed components 
        Label participantLabel = new Label(labels[4]);
        participantLabel.setFont(HEADER_FONT);

        Panel radioPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        panel.add(participantLabel, 
                  new EnvoyConstraints(getDialogWidth()/3, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.NOT_END_OF_LINE));
        CheckboxGroup cbg = new CheckboxGroup();
        m_userCheckBox = new Checkbox(labels[5], cbg, true);
        radioPanel.add(m_userCheckBox);
        m_userCheckBox.addItemListener(new ItemListener()
                                       {
                                           public void itemStateChanged(ItemEvent e)
                                           {
                                               m_grid.setVisible(true);
                                               populateRoleGrid();
                                           }
                                       });
        m_roleCheckBox = new Checkbox(labels[6], cbg, false);
        radioPanel.add(m_roleCheckBox);
        m_roleCheckBox.addItemListener(new ItemListener()
                                       {
                                           public void itemStateChanged(ItemEvent e)
                                           {
                                               m_grid.setVisible(false);
                                               populateRoleGrid();                                               
                                           }
                                       });
        panel.add(radioPanel,
                  new EnvoyConstraints(getDialogWidth()/2, 24, 3,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_NOT_RESIZABLE,
                                       EnvoyConstraints.END_OF_LINE));

        // create the grid panel
        m_grid = new GridPanel(0, 3);
        setDisplayOptions(m_grid);
        // set selection options.
        m_grid.setCellSelection(false);
        m_grid.setRowSelection(true);
        m_grid.setColSelection(false);
        m_grid.setMultipleSelection(false);
        // set fonts.
        m_grid.setGridAttributes(new GridAttributes(this, 
                                                    CELL_FONT,
                                                    ENVOY_BLACK,
                                                    ENVOY_WHITE,
                                                    GridPanel.JUST_LEFT,
                                                    GridPanel.TEXT));
        m_grid.setHeaderAttributes(new GridAttributes(this,
                                                      HEADER_FONT,
                                                      ENVOY_WHITE,
                                                      ENVOY_BLUE,
                                                      GridPanel.JUST_LEFT,
                                                      GridPanel.TEXT));
        // set headers.
        m_grid.setColHeaders((String[])getValue(GRID_HEADER_LABELS));
        // add grid listeners.
        m_grid.addGridListener(new GridAdapter()
                               {
                                   public void gridCellsClicked(GridEvent event)
                                   {
                                       updateButtonStatus(isDirty());
                                   }
                               });
        panel.add(m_grid,
                  new EnvoyConstraints(50, 24, 1,
                                       EnvoyConstraints.LEFT,
                                       EnvoyConstraints.X_RESIZABLE,
                                       EnvoyConstraints.Y_RESIZABLE,
                                       EnvoyConstraints.END_OF_LINE));
        return panel;
    }

    //populate dialog with default values
    private void populateDialog()
    {
        Vector values = (Vector)getValue(VALUES);

        if (values != null)
        {
            m_activityCombo.select((String)values.elementAt(0));
            m_role = (String)values.elementAt(1);
            m_initialRoleType = ((Boolean)values.elementAt(4)).booleanValue();
            if (!m_initialRoleType)
            {
                m_roleCheckBox.setState(true);
            }
            populateRoleGrid();
            populateTimeFields((Long)values.elementAt(2),
                               (Long)values.elementAt(3));
            populateRateDropDown();
        }
        else
        {
            m_activityCombo.select(0);
        }
    }

    private void populateTimeFields(Long p_acceptTime, Long p_completeTime)
    {
        m_taskIsAccepted = (p_acceptTime.longValue() == 0);
        long dhm[] = DateHelper.daysHoursMinutes(p_acceptTime.longValue());
        m_daysToAccept.setText("" + dhm[0]);
        m_hoursToAccept.setText("" + dhm[1]);
        m_minutesToAccept.setText("" + dhm[2]);
        dhm = DateHelper.daysHoursMinutes(p_completeTime.longValue());
        m_daysToComplete.setText("" + dhm[0]);
        m_hoursToComplete.setText("" + dhm[1]);
        m_minutesToComplete.setText("" + dhm[2]);
    }

    private void populateRoleGrid()
    {
        // ask the parent screen to get the grid data from the pagehandler.
        GridData gridData =
            m_parent.getRoleInfo(m_activityCombo.getSelectedItem(), m_userCheckBox.getState());
        // set the size of the grid and the data.	
        m_grid.setNumRows(gridData.getNumRows());
        m_grid.setGridData(gridData, true);
        m_grid.setColHeaders((String[])getValue(GRID_HEADER_LABELS));

        int height = getDialogHeight();
        // if the role is selected, enable the save button.
        // note: the ONLY single selection is not visible to the User.
        if (m_roleCheckBox.getState())
        {
            m_grid.setVisible(false);

            if (m_grid.getNumRows() > 0)
            {
                m_grid.selectRow(1, true);
            }
            if (m_costingEnabled)
            {
                height = 300;
            }
            else
            {
                height = 270;
            }
        }
        else
        {
            m_grid.setVisible(true);            
        }        

        // resize depending on the selection of the user or role radiobutton.
        resizeDialog(height);
        // update the grid after any changes
        m_grid.repaintGrid();
        //now that the grid is displayed, we can select the user role in the grid.
        selectUserRole(gridData);
        updateButtonStatus(isDirty());
    }

    
    /**
     * Perform a specific action when the ok button is clicked.
     */
    public void performAction()
    {
        long acceptMillis =
            (m_taskIsAccepted ? 0 : 
             DateHelper.milliseconds(parseLong(m_daysToAccept.getText()),
                                     parseLong(m_hoursToAccept.getText()),
                                     parseLong(m_minutesToAccept.getText())));
        long completeMillis =
            DateHelper.milliseconds(parseLong(m_daysToComplete.getText()),
                                    parseLong(m_hoursToComplete.getText()),
                                    parseLong(m_minutesToComplete.getText()));

        String[] messages = (String[])getValue(MESSAGE);

        /* accept time is allowed to be zero if the task has already been */
        /* accepted */
        if (completeMillis == 0 || (!m_taskIsAccepted && acceptMillis == 0))
        {
            ((ModifyWorkflowPanel)m_parent).getEnvoyApplet().getErrorDlg(messages[0], this);            
        }
        else
        {
            Vector values = (Vector)getValue(VALUES);          

            Vector activities = (Vector)getValue(ACTIVITIES);
            Activity act = (Activity)
                            activities.elementAt(m_activityCombo.getSelectedIndex()-1);
            m_values.addElement(act);       //0
            if (m_userCheckBox.getState())  //1
            {
                m_values.addElement(m_grid.getRowData(m_grid.getFirstSelectedRow()).elementAt(3));
            }
            else
            {
                m_values.addElement(m_grid.getRowData(1).elementAt(0));
            }
            m_values.addElement("" + acceptMillis);    //2
            m_values.addElement("" + completeMillis);  //3
            m_values.addElement(new Boolean(m_userCheckBox.getState()));  //4
            if (values != null) // if this is modify.
            {
                m_values.addElement(values.elementAt(5)); // task - 5
                m_values.addElement(values.elementAt(6)); // task state - 6
            }
            // will be the LAST one
            if (m_costingEnabled)
            {
                m_values.addElement(findRateFromChoice(act));   //5  or 7
            }
            dispose();
        }
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Abstract Methods
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Override Methods
    //////////////////////////////////////////////////////////////////////////////////
    /**
     * Get the height for the dialog to be displayed.
     * @return The dialog height.
     */
    public int getDialogHeight()
    {
        return 450;
    }

    /**
     * Get the width for the dialog to be displayed.
     * @return The dialog width.
     */
    public int getDialogWidth()
    {
        return 450;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Override Methods
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////

    // determines whether we're in the dirty mode.
    private boolean isDirty()
    {
        boolean isDirty = true;
        if (m_costingEnabled)
        { 
            // if no rates or it is the default "Choose..."
            if (m_rateChoice == null ||
                m_rateChoice.getItemCount() <= 0)
            {
                isDirty = false;
            }
        }
        return(isDirty && 
               timesAreValid() && 
               m_activityCombo.getSelectedIndex() >= 0 &&
               m_grid.getFirstSelectedRow() > 0);
    }

    /* ensure that all input time values are valid */
    private boolean timesAreValid()
    {
        return(acceptTimesAreValid() && completeTimesAreValid());
    }

    /* Return true if the accept times are valid */
    private boolean acceptTimesAreValid()
    {
        return(m_taskIsAccepted || 
               timesAreValid(m_daysToAccept,
                             m_hoursToAccept,
                             m_minutesToAccept));
    }

    /* Return true if the complete times are valid */
    private boolean completeTimesAreValid()
    {
        return timesAreValid(m_daysToComplete, 
                             m_hoursToComplete,
                             m_minutesToComplete);
    }

    /* Return true if all of the given times are valid */
    private boolean timesAreValid(TextField p_days,
                                  TextField p_hours,
                                  TextField p_minutes)
    {
        return numberIsValid(p_days, 0, 365) &&
            numberIsValid(p_hours, 0, 24 * 7) &&
            numberIsValid(p_minutes, 0, 12 * 60);
    }

    /* Return true if the contents of the given number field are between the */
    /* specified limits. */
    private boolean numberIsValid(TextField p_number, long p_low, long p_high)
    {
        String text = p_number.getText();
        boolean valid = false;
        if (text == null || text.length() == 0)
        {
            text = "0";
        }
        try
        {
            long x = Long.parseLong(text);
            valid = ((x >= p_low) && (x <= p_high));
        }
        catch (Exception e)
        {
        }
        if (!valid)
        {
            p_number.selectAll();
            p_number.requestFocus();
        }
        return valid;
    }

    /* Convert the given string into a long; 0 if it doesn't parse. */
    private long parseLong(String p_string)
    {
        long v = 0;
        try
        {
            v = Long.parseLong(p_string);
        }
        catch (NumberFormatException e)
        {
            // ignore
        }
        return v;
    }

    // return the dialog values
    private Vector doModal()
    {
        show();
        return m_values;
    }

    /* Populate the rates according to the activity selected. */
    private void populateRateDropDown()
    {
        if (m_costingEnabled)
        {
            // clear it of all - and repopulate
            m_rateChoice.removeAll();
            m_rateChoice.addItem(m_rateDefaultChoice);
            m_rateChoice.select(m_rateDefaultChoice);
            
            if (m_allRates != null && 
                m_allRates.size() > 0)
            {
                String selectedActivity = m_activityCombo.getSelectedItem();
                // get the rates associated with the particular activity
                Vector rates = (Vector)m_allRates.get(selectedActivity);
                if (rates != null && rates.size() > 0)
                {   
                    for (int i=0 ; i < rates.size() ; i++)
                    {
                        Rate rate = (Rate)rates.elementAt(i);
                        m_rateChoice.addItem(rate.getName());                                             
                        // if this is the chosen one from before
                        if (m_selectedRateId != -1)
                        {
                            if (m_selectedRateId == rate.getId())
                            {
                                m_rateChoice.select(rate.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    /* Find the rate object in the Hashtable from the
       rate chosen (specified by activity). */
    private Rate findRateFromChoice(Activity p_activity)
    {
        String rateName = m_rateChoice.getSelectedItem();
        boolean found = false;
        Rate r = null;
        // if one was chosen
        if (!rateName.equals(m_rateDefaultChoice))
        {
            Vector rates = (Vector)m_allRates.get(p_activity.getName());
            for (int i=0 ; !found && i < rates.size() ; i++)
            {
                r = (Rate)rates.elementAt(i);
                if (r.getName().equals(rateName))
                {
                    found = true;
                }
            }
        }
        return found ? r : null;
    }

    // select the user role during modification of an activity
    private void selectUserRole(GridData gridData)
    {
        // only make selection during "modify" state and for a "user role"
        if (m_role != null && m_initialRoleType)
        {
            int rows = gridData == null ? 0 : gridData.getNumRows();
            boolean isEqual = false;
            for (int i=0; !isEqual && i<rows; i++)
            {
                int rowNum = i+1;
                String role = (String)gridData.getRowHeaderData(rowNum);
                isEqual = role != null && role.equals(m_role);
                if (isEqual)
                {                
                    //m_grid.revealRow(rowNum); // is supposed to scroll up but not working yet
                    m_grid.selectRow(rowNum, true);
    
                }              
            }
        }         
    }


    //resize the dialog
    private void resizeDialog(int p_height)
    {
        int width   = getDialogWidth() + getInsets().left + getInsets().right;
        int height  = p_height + getInsets().top + getInsets().bottom;
        setSize(width, height);

        // resize the parent dialog as required by Netscape - defect 4941
        super.setSize(width, height);
        super.invalidate();
        super.validate();
        super.repaint();
    }


    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Helper Methods
    //////////////////////////////////////////////////////////////////////////////////
    // Call me to invoke this dialog!
    public static Vector getActivityDialog(ActivityParentable p_parent, String p_title, Hashtable p_hashtable)
    {
        ActivityDialog dlg = new ActivityDialog(p_parent, p_title, p_hashtable);
        return dlg.doModal();
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Helper Methods
    //////////////////////////////////////////////////////////////////////////////////
}
