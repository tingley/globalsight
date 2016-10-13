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

package installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>This class creates an object used to obtain user input.  The object
 * has a label and a user-input field.  The field can be one of
 * several different types, such as a text field, a check box, or a popup
 * menu.  If specified, the user input will be validated when the object
 * loses focus.
 */

public class InputField
    extends JPanel
    implements ActionListener, FocusListener, KeyListener, ChangeListener
{
    private static final long serialVersionUID = -940356702130822796L;

    /**
     * Don't validate the user input.
     */
    public static final int DONT_VALIDATE = 0;

    /**
     * Validate that the user input is not blank.
     */
    public static final int VALIDATE_NOT_EMPTY = 1;

    /**
     * Validate that the user input is a valid integer.
     */
    public static final int VALIDATE_INTEGER = 2;

    /**
     * Validate that the user input is a valid double.
     */
    public static final int VALIDATE_DOUBLE = 3;

    /**
     * Validate that the user input is a file or directory that exists.
     */
    public static final int VALIDATE_EXISTS = 4;
    
    /**
     * Validate that the user input is a file or directory that exists or empty.
     */
    public static final int VALIDATE_EMPTY_EXISTS = 5;
    
    private static Locale s_locale = Locale.getDefault();
    private static final String INSTALL_UI_PROPERTIES = "data/installUI";

    private static String s_browseButtonName;
    private static String s_selectButtonName;
    private static String s_noValueToolTipText;
    private static String s_badIntegerToolTipText;
    private static String s_badDoubleToolTipText;
    private static String s_badFileToolTipText;
    private static String s_disabledToolTipText;

    private String m_toolTipText = null;
    
    private int m_labelPosition = TitledBorder.LEFT;
    
    private int validateMode = VALIDATE_NOT_EMPTY;  // default validation mode
    private int installerScreen = -1;               // the screen number the object is in
    private JLabel label;
    private String labelText;

    // The main control for user input
    private JComponent control;

    // The secondary control that can be used to update the main control,
    // such as a browse button.
    private JComponent secondaryControl = null;

    // The field this object is dependent on.  If the parent field has
    // a value of 0, this object is disabled.
    private ArrayList parents = new ArrayList();

    // The fields dependent on this object.  If this field has
    // a value of 0, the dependents are disabled.
    private ArrayList children = new ArrayList();
    private String m_browseFilter;

    private Color m_bgColor = null;

    // Default value if field is not set
    private String m_defaultValue = null;

    // List of actual values to return, for use in cases where the options
    // displayed in the UI is different than the values that should be returned.
    private String[] m_actualValues = null;
    private int sliderScale;

    // Use the same file chooser window for all fields, since only one
    // can be opened at a time anyway.  This way it will come up faster.
    private static JFileChooser fileChooser = new JFileChooser();

    public InputField(String p_labelText, ArrayList p_typesList,
            String p_currentValue, int p_currentScreen)
    {
        create(p_labelText, p_typesList,
                p_currentValue, p_currentScreen, TitledBorder.LEFT);
    }

    public InputField(String p_labelText, ArrayList p_typesList,
            String p_currentValue, int p_currentScreen, int p_labelPosition)
    {
        create(p_labelText, p_typesList,
                p_currentValue, p_currentScreen, p_labelPosition);
    }

    private void initialize()
    {
        if (s_browseButtonName == null) {
            ResourceBundle m_resources = ResourceBundle.getBundle(INSTALL_UI_PROPERTIES, s_locale);
    
            s_browseButtonName = m_resources.getString("browse");
            s_selectButtonName = m_resources.getString("select");
            s_noValueToolTipText = m_resources.getString("tooltip_no_value");
            s_badIntegerToolTipText = m_resources.getString("tooltip_bad_integer");
            s_badDoubleToolTipText = m_resources.getString("tooltip_bad_double");
            s_badFileToolTipText = m_resources.getString("tooltip_bad_file");
            s_disabledToolTipText = m_resources.getString("tooltip_disabled_control");
        }
    }
    
    private void create(String p_labelText, ArrayList p_typesList,
            String p_currentValue, int p_currentScreen, int p_labelPosition)
    {
        initialize();

        setLayout(new BorderLayout());

        labelText = p_labelText;
        label = new JLabel(p_labelText);
        installerScreen = p_currentScreen;

        // Default is a text field if the field type is not defined
        if (p_typesList == null || p_typesList.size() == 0
                || p_typesList.get(0).equals("integer")
                || p_typesList.get(0).equals("double")
                || p_typesList.get(0).equals("text-emptyable")) {
            createTextField(p_typesList, p_currentValue);
        }
        // Field type is a checkbox
        else if (p_typesList.get(0).equals("checkbox")) {
            createCheckBox(p_typesList, p_currentValue);
        }

        // If field type is directory, then define a text field, and a
        // browse button to browse directories
        else if (p_typesList.get(0).toString().startsWith("browse-")) {
            createDirectoryField(p_typesList, p_currentValue);
        }

        // If field type is slider, then define a text field, and a
        // slider
        else if (p_typesList.get(0).equals("slider")) {
            createSlider(p_typesList, p_currentValue);
        }

        // If the field type is defined but is not one of the above types,
        // then assume it is a list of menu items in a popup menu
        else {
            createMenu(p_typesList, p_currentValue);
        }

        control.addFocusListener(this);
        if (p_labelPosition == TitledBorder.LEFT)
            add(label, BorderLayout.WEST);
        else if (p_labelPosition == TitledBorder.RIGHT)
            add(label, BorderLayout.EAST);
        else
            titlePanel(this, p_labelText, p_labelPosition);
    }

    public static void setCurrentLocale(Locale p_locale)
    {
        s_locale = p_locale;
	}

    public void setValidationMode(int p_mode)
    {
        validateMode = p_mode;
    }

    public void setInstallerScreen(int p_screen)
    {
        installerScreen = p_screen;
    }

    public int getInstallerScreen()
    {
        return installerScreen;
    }

    public String getLabel()
    {
        return labelText;
    }

    public void setLabel(String p_labelText)
    {
        label.setText(p_labelText);
    }

    public void setLabelPosition(int p_position)
    {
        m_labelPosition = p_position;
    }

    public Dimension getLabelPreferredSize()
    {
        return label.getPreferredSize();
    }

    public void setLabelPreferredSize(Dimension p_size)
    {
        label.setPreferredSize(p_size);
    }

    public JComponent getControl()
    {
        return control;
    }

    /*
     * Get the number of ancestor generations, and therefore the number of
     * levels to indent.
     */
    private int getIndentLevel()
    {
        if (parents.size() == 0)
            return 1;
        else {
            InputField parent = ((InputField) parents.get(0));
            return 1 + parent.getIndentLevel();
        }
    }

    /**
     * Add another InputField to the list of dependents of the current field.
     */
    public void addChild(InputField p_child)
    {
        String indentMarker = "     ";
        
        children.add(p_child);
        p_child.parents.add(this);

        int indentLevel = getIndentLevel();
        for (int i = 1; i < indentLevel; i++) {
            indentMarker = "     " + indentMarker;
        }
        p_child.setLabel(indentMarker + p_child.getLabel());
    }

    public String getValue()
    {
        String text = null;
        if (control instanceof JComboBox) {
            if (m_actualValues != null) {
                text = m_actualValues[((JComboBox) control).getSelectedIndex()];
            }
            else {
                text = ((JComboBox) control).getSelectedItem().toString();
            }
        }
        else if (control instanceof JTextField) {
            text = ((JTextField) control).getText();
        }
        else if (control instanceof JCheckBox) {
            text = (((JCheckBox) control).isSelected() ? m_actualValues[1] : m_actualValues[0]);
        }

        if (text == null || text.equals("")) {
            if (m_defaultValue != null)
                text = m_defaultValue;
        }
        else {
            text = text.trim();
        }
        return text;
    }

    public void setValue(String p_value)
    {
        String text = null;
        if (control instanceof JComboBox) {
            ((JComboBox) control).setSelectedItem(p_value);
        }
        else if (control instanceof JTextField) {
            ((JTextField) control).setText(p_value);
        }
        else if (control instanceof JCheckBox) {
            ((JCheckBox) control).setSelected( p_value.equals(m_actualValues[1]) );
        }
    }

    public void setDefaultValue(String p_value)
    {
        m_defaultValue = p_value;
    }

    public boolean isValidValue()
    {
        return !label.getForeground().equals(Color.red);
    }

    public boolean isEnabled()
    {
        return control.isEnabled();
    }

    public void setToolTipText(String p_toolTipText)
    {
        m_toolTipText = p_toolTipText;
        label.setToolTipText(p_toolTipText);
        control.setToolTipText(p_toolTipText);
    }
    
    /**
     * Enable or disable the current field, along with its dependents.
     */
    public void setEnabled(boolean p_enabled)
    {
        String disabledMessage = null;
        if (parents.size() > 0) {
            InputField parent = ((InputField) parents.get(0));
            String parentName = parent.getLabel();
            
            Object[] arguments = { parentName };
            disabledMessage = MessageFormat.format(s_disabledToolTipText, arguments);
        }
                
        control.setEnabled(p_enabled);
        control.setToolTipText((p_enabled ? m_toolTipText : disabledMessage));
        if (secondaryControl != null) {
            secondaryControl.setEnabled(p_enabled);
            secondaryControl.setToolTipText((p_enabled ? null : disabledMessage));
        }

        validateLabel();
    }

    public void setBackground(Color p_color)
    {
        if (control instanceof JComboBox && !((JComboBox) control).isEditable()) {
            ((JComboBox) control).setBackground(p_color);
        }
        if (secondaryControl != null)
            secondaryControl.setBackground(p_color);
        m_bgColor = p_color;
    }

    private void decreaseControlHeight(JComponent p_control)
    {
        Dimension controlSize = p_control.getPreferredSize();
        controlSize.setSize((int) controlSize.getWidth(), 
                            (int) controlSize.getHeight()-4);
        p_control.setPreferredSize(controlSize);
    }


    public void setFont(Font p_font)
    {
        if (label != null)
            label.setFont(p_font);

        // For popup menus and buttons, decrease the height to take up less room
        if (control != null) {
            control.setFont(p_font);
            if (control instanceof JComboBox) {
                decreaseControlHeight(control);
            }
        }
        if (secondaryControl != null) {
            secondaryControl.setFont(p_font);
            if (secondaryControl instanceof JButton) {
                decreaseControlHeight(secondaryControl);
            }
        }
    }

    public void titlePanel(JComponent p_panel, String p_title, int p_labelPosition)
    {
        Border m_border = BorderFactory.createEtchedBorder();
        p_panel.setBorder(BorderFactory.createTitledBorder(
                m_border, p_title,
                TitledBorder.LEFT,
                p_labelPosition));
    }

    private void createTextField(ArrayList p_typesList, String p_currentValue)
    {
        control = new JTextField(p_currentValue, 10);
        if (p_typesList != null && p_typesList.size() > 0) {
            if (p_typesList.get(0).equals("integer"))
                setValidationMode(VALIDATE_INTEGER);
            else if (p_typesList.get(0).equals("double"))
                setValidationMode(VALIDATE_DOUBLE);
            else if (p_typesList.get(0).equals("text-emptyable"))
                setValidationMode(DONT_VALIDATE);
        }
        add(control, BorderLayout.CENTER);
    }

    private void createCheckBox(ArrayList p_typesList, String p_currentValue)
    {
        control = new JCheckBox();
        m_actualValues = new String[2];
        if (p_typesList.size() == 3) {
            m_actualValues[1] = p_typesList.get(1).toString();
            m_actualValues[0] = p_typesList.get(2).toString();
        }
        else {
            m_actualValues[1] = "true";
            m_actualValues[0] = "false";
        }
        ((JCheckBox) control).setSelected(m_actualValues[1].equals(p_currentValue));
        ((JCheckBox) control).addActionListener(this);
        control.setOpaque(false);
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(false);
        innerPanel.add(control, BorderLayout.WEST);
        add(innerPanel, BorderLayout.CENTER);
    }

    private void createDirectoryField(ArrayList p_typesList, String p_currentValue)
    {
        control = new JTextField(p_currentValue, 10);
        secondaryControl = new JButton(s_browseButtonName);
        ((JButton) secondaryControl).addActionListener(this);

        ((JTextField) control).addActionListener(this);
        String browseParam = p_typesList.get(0).toString();
        int index = browseParam.indexOf("-", 7);
        m_browseFilter = browseParam.substring(7, (index == -1 ? browseParam.length() : index));
        if (p_typesList.get(0).toString().endsWith("dontvalidate"))
            setValidationMode(VALIDATE_NOT_EMPTY);
        else if (p_typesList.get(0).toString().endsWith("emptyorexists"))
            setValidationMode(VALIDATE_EMPTY_EXISTS);
        else
            setValidationMode(VALIDATE_EXISTS);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(secondaryControl, BorderLayout.EAST);
        fieldPanel.add(control, BorderLayout.CENTER);
        add(fieldPanel, BorderLayout.CENTER);
    }

    private void createSlider(ArrayList p_typesList, String p_currentValue)
    {
        int min = Integer.parseInt((String) p_typesList.get(1));
        int max = Integer.parseInt((String) p_typesList.get(2));
        sliderScale = 1;
        if (p_typesList.size() > 3)
            sliderScale = Integer.parseInt((String) p_typesList.get(3));
        secondaryControl = new JSlider(min, max);
        try {
            int curValue = (int) (Double.parseDouble(p_currentValue) * sliderScale);
            if (curValue < min)
                curValue = min;
            else if (curValue > max)
                curValue = max;
            ((JSlider) secondaryControl).setValue(curValue);
        }
        catch (NumberFormatException ex) {
            // Not a number, so do nothing here and let validateLabel catch
            // the problem.
        }
        control = new JTextField(p_currentValue, 4);
        ((JSlider) secondaryControl).setMajorTickSpacing(10);

        ((JSlider) secondaryControl).addChangeListener(this);
        ((JTextField) control).addFocusListener(this);
        setValidationMode(sliderScale == 1 ? VALIDATE_INTEGER
                                           : VALIDATE_DOUBLE);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(control, BorderLayout.WEST);
        fieldPanel.add(secondaryControl, BorderLayout.CENTER);
        secondaryControl.setOpaque(false);
        fieldPanel.setOpaque(false);
        add(fieldPanel, BorderLayout.CENTER);
    }

    private void createMenu(ArrayList p_typesList, String p_currentValue)
    {
        // If the last menu item is 'etc', then the menu will be editable
        boolean isEditable = p_typesList.get(p_typesList.size()-1).equals("etc");
        if (isEditable)
            p_typesList.remove(p_typesList.size()-1);

        if (p_typesList.get(0).toString().indexOf("=") != -1) {
            m_actualValues = new String[p_typesList.size()];
            for (int i = 0; i < p_typesList.size(); i++) {
                String menuItem = p_typesList.get(i).toString();
                int index = menuItem.indexOf("=");
                p_typesList.set(i, menuItem.substring(0, index).trim());
                m_actualValues[i] = menuItem.substring(index+1).trim();
                if (m_actualValues[i].equals(p_currentValue))
                    p_currentValue = p_typesList.get(i).toString();
            }
        }

        control = new JComboBox(p_typesList.toArray());
        ((JComboBox) control).setEditable(isEditable);
        if (p_currentValue != null)
            ((JComboBox) control).setSelectedItem(p_currentValue);
        if (isEditable) {
            ((JComboBox) control).addActionListener(this);
        }
        add(control, BorderLayout.CENTER);
    }

    /**
     * Validate the control and set the label to red if the value is invalid.
     * The validity is dependent on the validation mode, which can be
     * VALIDATE_NOT_EMPTY, VALIDATE_INTEGER, VALIDATE_DOUBLE, VALIDATE_EXISTS,
     * or VALIDATE_EMPTY_EXISTS.
     */
    public void validateLabel()
    {
        if (validateMode == DONT_VALIDATE || control instanceof JCheckBox
                || ((JComponent) control).isEnabled() == false)
            label.setForeground(Color.black);
        else {
            String text = getValue();
            String errorToolTip = null;

            boolean validationResult = true;
            if (text != null) {
                switch (validateMode) {
                    case VALIDATE_NOT_EMPTY:
                            validationResult = !text.equals("");
                            errorToolTip = s_noValueToolTipText;
                            break;
                    case VALIDATE_INTEGER:
                            try {
                                Integer.parseInt(text);
                                validationResult = true;
                            }
                            catch (NumberFormatException ex) {
                                validationResult = false;
                            }
                            errorToolTip = s_badIntegerToolTipText;
                            break;
                    case VALIDATE_DOUBLE:
                            try {
                                Double.parseDouble(text);
                                validationResult = true;
                            }
                            catch (NumberFormatException ex) {
                                validationResult = false;
                            }
                            errorToolTip = s_badDoubleToolTipText;
                            break;
                    case VALIDATE_EXISTS:
                            File currentDirectory = new File(text);
                            validationResult = currentDirectory.exists();
                            errorToolTip = s_badFileToolTipText;
                            break;
                    case VALIDATE_EMPTY_EXISTS:
                        if (text == null || "".equals(text))
                            validationResult = true;
                        else
                        {
                            File fi = new File(text);
                            validationResult = fi.exists();
                            errorToolTip = s_badFileToolTipText;
                        }
                        break;
                }

                TitledBorder border = (TitledBorder) getBorder();
                if (border != null) {
                    border.setTitleColor(validationResult ? Color.black : Color.red);
                    control.setToolTipText(validationResult ? m_toolTipText : errorToolTip);
                    repaint();
                }
                label.setForeground(validationResult ? Color.black : Color.red);
                label.setToolTipText(validationResult ? m_toolTipText : errorToolTip);
            }
        }
        validateDependency();
    }

    /**
     * Enable or disable dependent controls if the value is blank or 0.
     */
    public void validateDependency()
    {
        boolean doEnable = true;
        if (!isEnabled()) {
            doEnable = false;
        }
        else if (control instanceof JCheckBox) {
            doEnable = ((JCheckBox) control).isSelected();
        }
		else if (control instanceof JComboBox) {
//        	doEnable = "weblogic".equals((String) ((JComboBox) control).getSelectedItem());
        }
        else {
            String value = getValue();
            doEnable = (!value.equals("") && !value.equals("0"));
        }

        for (int i = 0; i < children.size(); i++) {
            InputField child = ((InputField) children.get(i));
            boolean doEnableChild = doEnable;
            
            // If a child control has multiple parents, check each parent.
            if (!doEnableChild && child.parents.size() > 1) {
                for (int j = 0; j < child.parents.size(); j++) {
                    Object parent = ((InputField) child.parents.get(j)).getControl();
                    if (parent instanceof JCheckBox) {
                        doEnableChild = ((JCheckBox) parent).isSelected();
                    }
                    else if (parent instanceof JComboBox) {
                        doEnableChild = (((JComboBox) parent).getSelectedIndex()) != -1;
                    }
                    else {
                        String value = getValue();
                        doEnableChild = (!value.equals("") && !value.equals("0"));
                    }
                    if (doEnableChild)
                        break;
                }
            }
            
            child.setEnabled(doEnableChild);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        // If source is a button, it is the Browse button
        if (source instanceof JButton && control.isEnabled()) {
            if (m_browseFilter.equals("directory"))
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            else
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.resetChoosableFileFilters();
            if (!m_browseFilter.equals("directory") && !m_browseFilter.equals("all"))
                fileChooser.addChoosableFileFilter(new ExtensionFilter(m_browseFilter));

            JTextField ctrl = (JTextField) control;
            fileChooser.setSelectedFile(new File(ctrl.getText()));

            int returnVal = fileChooser.showDialog(ctrl, s_selectButtonName);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
               ctrl.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
        validateLabel();
    }

    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();

        // If the slider was changed, update the text field with the value
        if (source instanceof JSlider) {
            if (sliderScale == 1)
                ((JTextField) control).setText("" + ((JSlider) source).getValue());
            else
                ((JTextField) control).setText("" + ((double) ((JSlider) source).getValue() / sliderScale));
        }
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
        validateLabel();
    }

    public void focusGained(FocusEvent e)
    {
        if (control instanceof JTextField)
            ((JTextField) control).selectAll();
        else if (control instanceof JComboBox) {
            if (((JComboBox) control).isEditable())
                ((JComboBox) control).getEditor().selectAll();
        }
    }

    public void focusLost(FocusEvent e)
    {
        // If text field was changed and there is a slider associated with it,
        // update the slider position.
        if (secondaryControl != null && secondaryControl instanceof JSlider) {
            try {
                double value = Double.parseDouble(((JTextField) control).getText());
                ((JSlider) secondaryControl).setValue((int) (value * sliderScale));
            }
            catch (NumberFormatException ex) {
                // Not a number, so do nothing here and let validateLabel catch
                // the problem.
            }
        }
        validateLabel();
    }
}

