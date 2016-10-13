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
// java
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.lang.Long;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
// com.globalsight
import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;
import com.globalsight.everest.webapp.applet.common.AbstractEnvoyDialog;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyLabel;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.util.collections.HashtableValueOrderWalker;

/**
 * The dialog is used for creating a Database Column
 *
 */
public class CreateModifyDBColumnDialog extends AbstractEnvoyDialog implements EnvoyAppletConstants
{
  private TextField m_nameField;
  private TextField m_columnLabel;
  private TextField m_tableName;

  private String m_defaultChoose;
  private Choice m_modeChoice;
  private Choice m_knownFormatTypeChoice;
  private Choice m_xmlRuleChoice;
  // need this to store the correspoding items.
  private Vector m_modeVector;
  private Vector m_knownFormatTypeVector;
  private Vector m_xmlRuleVector;

  private HashtableValueOrderWalker m_mode_pairs;
  private HashtableValueOrderWalker m_knownFormat_pairs;
  private HashtableValueOrderWalker m_xmlRule_pairs;

  private EnvoyLabel m_xmlRuleLabel;

  private Vector m_values = new Vector();
  private Frame m_parent;
  private String m_mode;

  private int m_width;
  private int m_height;
  private Panel m_panel;

  private boolean m_format_xml_selected_old = false;
  private boolean m_format_xml_selected_new;
  String[] labels = (String[])getValue(LABELS);
  //////////////////////////////////////////////////////////////////////////////////
  //  Begin:  Constructor
  //////////////////////////////////////////////////////////////////////////////////
  /**
   * Create a new DBColumnDialog.
   * @param p_parent - The parent frame component.
   * @param p_title - The title of the dialog.
   * @param p_hashtable - Contains the labels, list of XML rule files.
  */
  public CreateModifyDBColumnDialog(Panel p_parent, String p_title, Hashtable p_hashtable)
  {
    super(((DBColumnPanel)p_parent).getParentFrame(), p_title, p_hashtable);
    m_parent = ((DBColumnPanel)p_parent).getParentFrame();
    updateButtonStatus(isDirty());
  }
  //////////////////////////////////////////////////////////////////////////////////
  //  End:  Constructor  ///////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////
  //  Begin:  Abstract Methods  ////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////
  /**
   * Get the panel that should be displayed in this dialog.
   * @return The editor panel.
   */
    public Panel getEditorPanel() {
    labels = (String[])getValue(LABELS);
    m_defaultChoose = labels[6];
    m_mode_pairs = (HashtableValueOrderWalker)getValue(DBCOLUMN_MODEPAIRS);
    m_knownFormat_pairs = (HashtableValueOrderWalker)getValue(DBCOLUMN_FORMATPAIRS);
    m_xmlRule_pairs = (HashtableValueOrderWalker)getValue(DBCOLUMN_RULEPAIRS);
    Panel panel = new Panel(new EnvoyLineLayout(5, 5, 5, 5));
    setPanel(panel);
    panel.setBackground(ENVOY_WHITE);

	  // text field.
    m_nameField = new TextField();
    m_nameField.setEditable(true);
    m_columnLabel = new TextField();
    m_columnLabel.setEditable(true);
    m_tableName = new TextField();
    m_tableName.setEditable(true);

    // CONTENTMODE
    m_modeChoice = new Choice();
    m_modeVector = new Vector();
    m_modeChoice.addItem(m_defaultChoose); //choose...
    m_modeVector.addElement(null);
    for (int i=0; i < m_mode_pairs.size(); i++)
    {
      Integer num = (Integer)m_mode_pairs.getKey(i);
      m_modeChoice.addItem((String)m_mode_pairs.getValue(i));
      m_modeVector.addElement(num);
    }

    // KNOWN_FORMAT_TYPE
    m_knownFormatTypeChoice = new Choice();
    m_knownFormatTypeVector = new Vector();
    m_knownFormatTypeChoice.addItem(m_defaultChoose); //choose...
    m_knownFormatTypeVector.addElement(null);
    for (int i=0; i < m_knownFormat_pairs.size(); i++)
    {
        Integer num = (Integer)m_knownFormat_pairs.getKey(i);
        m_knownFormatTypeChoice.addItem((String)m_knownFormat_pairs.getValue(i));
        m_knownFormatTypeVector.addElement(num);
    }

    // XML_RULE
    m_xmlRuleChoice = new Choice();
    m_xmlRuleVector = new Vector();
    for (int i=0; i < m_xmlRule_pairs.size(); i++)
    {
      Integer num = (Integer)m_xmlRule_pairs.getKey(i);
      m_xmlRuleChoice.addItem((String)m_xmlRule_pairs.getValue(i));
      m_xmlRuleVector.addElement(num);
    }


	  // add listeners to determine if a change has occurred
    m_nameField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent e) {
        updateButtonStatus(isDirty());
		  }
	  });
	  m_columnLabel.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent e) {
        updateButtonStatus(isDirty());
		  }
	  });
	  m_tableName.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent e) {
        updateButtonStatus(isDirty());
		  }
	  });
	  m_modeChoice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtonStatus(isDirty());
		  }
	  });
	  m_knownFormatTypeChoice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // display XML rules choice
        m_format_xml_selected_old = m_format_xml_selected_new;
        m_format_xml_selected_new = m_knownFormatTypeChoice.getSelectedItem().equalsIgnoreCase("XML");
        if (m_format_xml_selected_old && !m_format_xml_selected_new) {
          // change from xml to non xml
          XmltoNonXmlDispose();
        }
        else if (!m_format_xml_selected_old && m_format_xml_selected_new) {
          // change from non xml to xml
          NonXmltoXmlDispose();
        }
        updateButtonStatus(isDirty());
		  }
	  });
	  m_xmlRuleChoice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtonStatus(isDirty());
		  }
	  });

    m_width = 130;
    m_height = 24;

    populateDialog();

    // dbcolumn title
    Font panelFont = new Font("Arial", Font.BOLD, 18);
    Label titleLabel = null;
    if (m_mode.equals("CreateOrRemove"))
        titleLabel = new Label(labels[10], Label.LEFT);
    else
        titleLabel = new Label(labels[12], Label.LEFT);
    titleLabel.setFont(panelFont);

    panelFont = new Font("Arial", Font.BOLD, 12);
    EnvoyLabel nameLabel = new EnvoyLabel(labels[0], Label.LEFT, m_width, m_height);
    nameLabel.setFont(panelFont);
    EnvoyLabel columnLabelLabel = new EnvoyLabel(labels[1], Label.LEFT, m_width, m_height);
    columnLabelLabel.setFont(panelFont);
    EnvoyLabel tableNameLabel = new EnvoyLabel(labels[2], Label.LEFT, m_width, m_height);
    tableNameLabel.setFont(panelFont);
    EnvoyLabel modeLabel = new EnvoyLabel(labels[3], Label.LEFT, m_width, m_height);
    modeLabel.setFont(panelFont);
    EnvoyLabel knownFormatTypeLabel = new EnvoyLabel(labels[4], Label.LEFT, m_width, m_height);
    knownFormatTypeLabel.setFont(panelFont);
    // make xmlRule labels available for all methods in class
    m_xmlRuleLabel = new EnvoyLabel(labels[5], Label.LEFT, m_width, m_height);
    m_xmlRuleLabel.setFont(panelFont);

    panel.add(titleLabel,
		  new EnvoyConstraints(getDialogWidth(), m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_NOT_RESIZABLE,
				       EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(nameLabel,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(m_nameField,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.CENTER,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(columnLabelLabel,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(m_columnLabel,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.CENTER,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(tableNameLabel,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(m_tableName,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.CENTER,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(modeLabel,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(m_modeChoice,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.CENTER,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(knownFormatTypeLabel,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
    panel.add(m_knownFormatTypeChoice,
		  new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.CENTER,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));

    if (m_format_xml_selected_old && !m_format_xml_selected_new)
    {
        // change from xml to non xml
        XmltoNonXml(panel);
    }
    else if (!m_format_xml_selected_old && m_format_xml_selected_new) {
        // change from non xml to xml
        NonXmltoXml(panel);
    }

//System.out.println("getEditorPanel:  m_format_xml_selected_old "+m_format_xml_selected_old+" m_format_xml_selected_new "+m_format_xml_selected_new);
    return panel;
  }

  private void populateDialog()
  {
    if (containsKey(DBCOLUMN_MOD))
    {
        Integer Itmp;
//        Enumeration enumeration;
        m_mode = "Modify";
	    DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl)getValue(DBCOLUMN_MOD);
        //m_nameField.setEnabled(false);
	    m_nameField.setText(dbcolumn.getColumnName());
	    m_columnLabel.setText(dbcolumn.getLabel());
	    m_tableName.setText(dbcolumn.getTableName());
	    m_columnLabel.setText(dbcolumn.getLabel());

        // handle content mode choices
        m_modeChoice = new Choice();
        m_modeVector = new Vector();
        Itmp = new Integer((int)dbcolumn.getContentMode());
        choiceUpdate(m_modeChoice, m_modeVector,
            m_mode_pairs, Itmp);

        // handle content knownFormatType choices
        m_knownFormatTypeChoice = new Choice();
        m_knownFormatTypeVector = new Vector();
        Itmp = new Integer((int)dbcolumn.getFormatType());
        choiceUpdate(m_knownFormatTypeChoice, m_knownFormatTypeVector,
            m_knownFormat_pairs, Itmp);
        m_knownFormatTypeChoice.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                // display XML rules choice
                m_format_xml_selected_old = m_format_xml_selected_new;
                m_format_xml_selected_new = m_knownFormatTypeChoice.getSelectedItem().equalsIgnoreCase("XML");
                if (m_format_xml_selected_old && !m_format_xml_selected_new)
                {
                    // change from xml to non xml
                    XmltoNonXmlDispose();
                }
                else if (!m_format_xml_selected_old && m_format_xml_selected_new)
                {
                    // change from non xml to xml
                    NonXmltoXmlDispose();
                }
                updateButtonStatus(isDirty());
		  }
        });

        // if necessary, handle content XML_RULE choices
        String chosenFormat = (String)m_knownFormat_pairs.get(Itmp);
        if (chosenFormat.equalsIgnoreCase("XML"))
        {
            m_format_xml_selected_old = m_format_xml_selected_new;
            m_format_xml_selected_new = true;
            m_xmlRuleChoice = new Choice();
            m_xmlRuleVector = new Vector();
            Itmp = new Integer((int)dbcolumn.getXmlRuleId());
            if (Itmp.intValue() == 0)
            {
                Itmp = new Integer((int)-1);
            }
            choiceUpdate(m_xmlRuleChoice, m_xmlRuleVector,
                m_xmlRule_pairs, Itmp);
        }
    }
    else
    {
        m_mode = "CreateOrRemove";
    }
  }

  /**
   * Perform a specific action when the ok button is clicked.
   */
  public void performAction()
  {
    Integer Itmp;
    Vector fileExtensionSelected = new Vector();
//System.out.println("performAction begin m_mode "+m_mode);
    if (m_mode.equals("CreateOrRemove")) {
    // create a new rule file and save to database
      DatabaseColumnImpl p_dbcolumn = new DatabaseColumnImpl();
      p_dbcolumn.setColumnName(m_nameField.getText().trim());
      p_dbcolumn.setLabel(m_columnLabel.getText().trim());
      p_dbcolumn.setTableName(m_tableName.getText().trim());
      Itmp = (Integer)m_knownFormatTypeVector.elementAt(m_knownFormatTypeChoice.getSelectedIndex());
      p_dbcolumn.setFormatType(Itmp.longValue());
      Itmp = (Integer)m_modeVector.elementAt(m_modeChoice.getSelectedIndex());
      p_dbcolumn.setContentMode(Itmp.intValue());
      Itmp = (Integer)m_xmlRuleVector.elementAt(m_xmlRuleChoice.getSelectedIndex());
      if (Itmp.longValue() == -1)
      {
        p_dbcolumn.setXmlRuleId(0);
      }
      else
      {
        p_dbcolumn.setXmlRuleId(Itmp.longValue());
      }
	    m_values.addElement(p_dbcolumn);
    } else if (m_mode.equals("Modify")){
	    DatabaseColumnImpl modifiedDBColumn = (DatabaseColumnImpl)getValue(DBCOLUMN_MOD);
        modifiedDBColumn.setLabel(m_columnLabel.getText().trim());
        modifiedDBColumn.setTableName(m_tableName.getText().trim());
        Itmp = (Integer)m_knownFormatTypeVector.elementAt(m_knownFormatTypeChoice.getSelectedIndex());
	    modifiedDBColumn.setFormatType(Itmp.longValue());
        Itmp = (Integer)m_modeVector.elementAt(m_modeChoice.getSelectedIndex());
        modifiedDBColumn.setContentMode(Itmp.intValue());
        Itmp = (Integer)m_xmlRuleVector.elementAt(m_xmlRuleChoice.getSelectedIndex());
        if (Itmp.longValue() == -1)
        {
            modifiedDBColumn.setXmlRuleId(0);
        }
        else
        {
            modifiedDBColumn.setXmlRuleId(Itmp.longValue());
        }
	    m_values.addElement(modifiedDBColumn);
    }

    dispose();

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
    return 500;
  }

  /**
   * Get the width for the dialog to be displayed.
   * @return The dialog width.
   */
  public int getDialogWidth()
  {
    return 360;
  }
  //////////////////////////////////////////////////////////////////////////////////
  //  End:  Override Methods
  //////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////
  //  Begin:  Local Methods
  //////////////////////////////////////////////////////////////////////////////////
  // set the panel
  private void setPanel(Panel p_panel) {
    m_panel = p_panel;
  }

  // get the panel
  private Panel getPanel() {
    return m_panel;
  }

  // return the dialog values
  private Vector doModal() {
    show();
//System.out.println("doModal m_values "+m_values);
    return m_values;
  }

  private void XmltoNonXmlDispose() {
    Panel panel = getPanel();

    XmltoNonXml(panel);

    // "trick" to keep text visible after changing dialog boxes
    m_nameField.setText(m_nameField.getText());
    m_columnLabel.setText(m_columnLabel.getText());
    m_tableName.setText(m_tableName.getText());

    // dispose of dialog used to select xml format
    dispose();
    setVisible(true);
  }

  // handle xml to non xml rule choice
  private void XmltoNonXml(Panel panel) {

    // remove xml rule choices
    panel.remove(m_xmlRuleLabel);
    panel.remove(m_xmlRuleChoice);
  }

  private void NonXmltoXmlDispose() {
    Panel panel = getPanel();

    NonXmltoXml(panel);

    // "trick" to keep text visible after changing dialog boxes
    m_nameField.setText(m_nameField.getText());
    m_columnLabel.setText(m_columnLabel.getText());
    m_tableName.setText(m_tableName.getText());

    // dispose of dialog used to select xml format
    dispose();
    setVisible(true);
  }

  // handle non xml to xml rule choice
  private void NonXmltoXml(Panel panel) {

    // add xml rule choices
    panel.add(m_xmlRuleLabel,
      new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.LEFT,
				       EnvoyConstraints.X_NOT_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.NOT_END_OF_LINE));
    panel.add(m_xmlRuleChoice,
      new EnvoyConstraints(m_width, m_height, 1, EnvoyConstraints.CENTER,
				       EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
				       EnvoyConstraints.END_OF_LINE));
  }

  // determines whether we're in the dirty mode.
  private boolean isDirty()
  {
      String nameField = m_nameField.getText();
      String defaultChoice = labels[6];
      String columnField = m_columnLabel.getText();
      String tableField = m_tableName.getText();
      return (nameField != null && !nameField.equals("") &&
              columnField != null && !columnField.equals("") &&
              tableField != null && !tableField.equals("") &&
              (m_modeChoice.getSelectedIndex() >= 0) && !m_modeChoice.getSelectedItem().equals(defaultChoice) &&
              (m_knownFormatTypeChoice.getSelectedIndex() >= 0) && !m_knownFormatTypeChoice.getSelectedItem().equals(defaultChoice));
  }

  //////////////////////////////////////////////////////////////////////////////////
  //  End:  Local Methods
  //////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////
  //  Begin:  Helper Methods
  //////////////////////////////////////////////////////////////////////////////////
  // Call me to invoke this dialog!
  public static Vector getDialog(Panel p_parent, String p_title, Hashtable p_hashtable)
  {
    CreateModifyDBColumnDialog dlg = new CreateModifyDBColumnDialog(p_parent, p_title, p_hashtable);
    return dlg.doModal();
  }
  //////////////////////////////////////////////////////////////////////////////////
  //  End:  Helper Methods
  //////////////////////////////////////////////////////////////////////////////////
}
