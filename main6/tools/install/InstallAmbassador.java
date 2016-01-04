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

import installer.InputField;
import installer.InstallerFrame;
import installer.SwingWorker;
import util.FileUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

import util.JarSignUtil;
import util.Utilities;

public class InstallAmbassador extends InstallerFrame implements
        ActionListener, WindowListener
{
    private static final long serialVersionUID = -1970811861866186170L;

    // Constants
    private static final String INSTALL_DISPLAY_PROPERTIES = "data/installDisplay";

    private static final String INSTALL_UI_PROPERTIES = "data/installAmbassador";

    private static final String INSTALL_VALUE_TYPES_PROPERTIES_FILE = "data/installValueTypes.properties";

    private static final String INSTALL_GROUPS_PROPERTIES_FILE = "data/installOrderUI.properties";

    private static Locale s_locale = Locale.getDefault();

    // Globals - structures to hold properties values
    private ResourceBundle m_installAmbassadorProperties;

    private Properties m_groupProperties;

    private ArrayList<String> m_installOrder = new ArrayList<String>();

    private Install m_installer = new Install();

    private RecursiveCopy m_copier = new RecursiveCopy();

    private String m_confirmAdvance;

    // List of the labels of the parameters, stored in the same order as
    // m_installOrder
    private ArrayList<InputField> m_inputFieldList = new ArrayList<InputField>();

    // Globals - GUI controls accessed by actionPerformed
    private JCheckBox m_copyConfigurationsCheckBox = new JCheckBox();

    private JCheckBox m_createNtServiceCheckBox = new JCheckBox();

    private JCheckBox m_createDataBaseCheckBox = new JCheckBox();

    private JCheckBox m_mergePropertiesCheckBox = new JCheckBox();

    private JButton m_loadSettingsButton = new JButton();

    // Label above the progress bar showing the current overall activity
    private JLabel m_groupCopyLabel = new JLabel();

    // Label above the progress bar showing the current file being acted upon
    private JLabel m_fileCopyLabel = new JLabel();

    private JProgressBar m_progressBar;

    // List of screen numbers where we need to do something special when
    // advancing to or from it.
    private int m_appServerScreenNum;

    private String previousAmbassadorHome = "../..";
    
    private static final String ALLOWD_PATH = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-0123456789./";
    
    private String JKS, keyPass, keyAlias;

    public InstallAmbassador()
    {
        super(s_locale);
        try
        {
            initialize();

            setTitle(m_installAmbassadorProperties.getString("window.title"));

            // Info screen, which displays a message about prerequisites
            createInfoScreen();

            // Install Parameters screens
            createInstallParameterScreens();

            // Create the install options screen.
            createInstallOptionsScreen();

            // Set the dependency among the parameters
            setParameterDependency();

            m_loadSettingsButton.setText(m_installAmbassadorProperties
                    .getString("load_settings"));
            m_loadSettingsButton.setFont(m_font.deriveFont(Font.PLAIN));
            buttonsPanel.add(m_loadSettingsButton, 1);
            m_loadSettingsButton.addActionListener(this);
            m_loadSettingsButton.setBackground(m_bgColor);

            // Set the width of all labels to the longest label, so that the
            // screens look consistent.
            double maxWidth = 0;
            for (int i = 0; i < m_inputFieldList.size(); i++)
            {
                Dimension curDim = ((InputField) m_inputFieldList.get(i))
                        .getLabelPreferredSize();

                if (maxWidth < curDim.getWidth())
                {
                    maxWidth = curDim.getWidth();
                }
            }

            for (int i = 0; i < m_inputFieldList.size(); i++)
            {
                Dimension curDim = ((InputField) m_inputFieldList.get(i))
                        .getLabelPreferredSize();

                curDim.setSize(maxWidth + 5, curDim.getHeight());
                ((InputField) m_inputFieldList.get(i))
                        .setLabelPreferredSize(curDim);
            }
            this.addWindowListener(this);

            setFirstScreen();

            setVisible(true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            showErrorDialogAndQuit(ex.toString());
        }
    }

    public void windowActivated(WindowEvent e)
    {
    }

    public void windowClosed(WindowEvent e)
    {
    }

    public void windowClosing(WindowEvent e)
    {
        saveInstallValues();
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e)
    {
    }

    public void windowDeiconified(WindowEvent e)
    {
    }

    public void windowIconified(WindowEvent e)
    {
    }

    public void windowOpened(WindowEvent e)
    {
    }

    private void initialize() throws IOException
    {
        m_installAmbassadorProperties = ResourceBundle.getBundle(
                INSTALL_UI_PROPERTIES, s_locale);

        if (!checkInstallPath())
        {
            showErrorDialogAndQuit(m_installAmbassadorProperties.getString("alert.path_containS_space"));
        }
        
        // Check the OS
        if (!m_installer.determineOperatingSystem())
        {
            Object[] args = { System.getProperty("os.name") };
            showErrorDialogAndQuit(MessageFormat.format(
                    m_installAmbassadorProperties
                            .getString("alert.unsupported_os"), args));
        }

        // The order of the parameters
        m_installer.loadOrder();
        // The default values
        m_installer.loadInstallValues();

        m_groupProperties = m_installer
                .loadProperties(INSTALL_GROUPS_PROPERTIES_FILE);

        m_confirmAdvance = m_installAmbassadorProperties
                .getString("alert.confirm_advance_screen");
        setVerticalGap(0);
        setHorizontalGap(0);

        // Set the tooltip to stay for 1 minute
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    }

    private static boolean checkInstallPath()
    {
        File f = new File(".");
        String path = f.getAbsolutePath();
        path = path.replace("\\", "/");
        int index = path.indexOf("/");
        path = path.substring(index + 1);
        for (char c : path.toCharArray())
        {
            if (ALLOWD_PATH.indexOf(c) < 0)
            {
                return false;
            }
        }
        
        return true;
    }
     
    private void createInfoScreen()
    {
        addScreen(createTextScreen(m_installAmbassadorProperties
                .getString("pre_install_message")));
    }

    private void createInstallParameterScreens() throws Exception
    {
        // Get the text of the parameters to display
        ResourceBundle installDisplay = ResourceBundle.getBundle(
                INSTALL_DISPLAY_PROPERTIES, s_locale);
        // The value types, which determine the type of control for
        // each parameter
        Properties installValueTypes;
        try
        {
            installValueTypes = m_installer
                    .loadProperties(INSTALL_VALUE_TYPES_PROPERTIES_FILE);
        }
        catch (IOException ex)
        {
            // If the value type properties file is not found, do
            // nothing, and the default type will be used for the
            // parameters.
            installValueTypes = null;
        }

        // Get the list of screens from the properties
        ArrayList<String> screensFromProperties = parseList(m_groupProperties
                .getProperty("screen_list"));

        // Set the layout to put each item in a separate row. The
        // item's height is the default, while its width fills the
        // width of the panel.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        for (int i = 0; i < screensFromProperties.size(); i++)
        {
            // Get the current screen name
            String screenKey = ((String) screensFromProperties.get(i)).trim();

            String parameterScreenTitle = m_installAmbassadorProperties
                    .getString(screenKey + ".title");

            // Check to see if the parameters are defined for this
            // screen, and throw an exception if not.
            keyExists(m_groupProperties, screenKey,
                    "The list of parameters for the screen " + screenKey
                            + " is not found.");

            // Get the controls in the current screen, and add them to
            // m_installOrder
            ArrayList<String> parameterList = parseList(m_groupProperties
                    .getProperty(screenKey));

            // Create the GUI for the current screen
            JPanel gridPanel = new JPanel(gridbag);

            // Put the panel inside another panel that is in the North
            // position. This way, the controls in the panel won't
            // expand vertically to fit the window.
            JPanel innerPanel = new JPanel(new BorderLayout());
            innerPanel.add(gridPanel, BorderLayout.NORTH);
            // Put the whole thing in a scroll pane
            JScrollPane scrollPane = new JScrollPane(innerPanel);

            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            JPanel parameterScreen = titleAndPadPanel(scrollPane,
                    parameterScreenTitle, "");

            // Add the controls to the current screen
            addParametersToScreen(parameterList, installDisplay,
                    installValueTypes, gridPanel);

            // Go through the controls and set their layout constraints
            Component[] params = gridPanel.getComponents();
            for (int j = 0; j < params.length; j++)
            {
                gridbag.setConstraints(params[j], c);
            }

            // Add the properties to the global properties list
            m_installOrder.addAll(parameterList);

            // Save the screen numbers so we can hide or show those screens
            // later
            if ("server_screen".equals(screenKey))
            {
                m_appServerScreenNum = getNumberOfScreens();
            }

            gridPanel.setBackground(m_bgColor);
            innerPanel.setBackground(m_bgColor);
            addScreen(parameterScreen);
        }
    }

    private void addParametersToScreen(ArrayList<String> p_parameterList,
            ResourceBundle p_installDisplay, Properties p_installValueTypes,
            JPanel p_gridPanel) throws Exception
    {
        for (int i = 0; i < p_parameterList.size(); i++)
        {
            String controlKey = ((String) p_parameterList.get(i)).trim();
            // Get the label
            String installDisplay = null;
            try
            {
                installDisplay = p_installDisplay.getString(controlKey).trim();
            }
            catch (Exception e)
            {
                System.err.println("Problem reading install display key: "
                        + controlKey);
                throw e;
            }
            // Get the value
            String installValue = m_installer.getInstallValue(controlKey);

            ArrayList<String> typesList = null;
            if (p_installValueTypes != null)
            {
                try
                {
                    typesList = parseList(p_installValueTypes
                            .getProperty(controlKey));
                }
                catch (java.util.MissingResourceException ex)
                {
                    // The type of control for this parameter is
                    // not defined, so the default type will be
                    // used.
                    typesList = null;
                }
            }

            InputField parameter = new InputField(installDisplay, typesList,
                    installValue, getNumberOfScreens());

            if (controlKey.endsWith("install_key")
                    || controlKey.endsWith("_adapters"))
            {
                parameter.setValidationMode(InputField.DONT_VALIDATE);
                parameter.setDefaultValue("0");
            }
            else
            {
                parameter.validateLabel();
            }

            try
            {
                String toolTip = p_installDisplay.getString(
                        controlKey + ".tooltip").trim();
                parameter.setToolTipText(toolTip);
            }
            catch (java.util.MissingResourceException ex)
            {
                // Tooltip is not defined for this parameter, so do nothing
            }

            parameter.setBackground(m_bgColor);
            parameter.setFont(m_font);

            m_inputFieldList.add(parameter);

            p_gridPanel.add(parameter);
        }
    }

    private void createInstallOptionsScreen()
    {
        String optionsScreenTitle = m_installAmbassadorProperties
                .getString("options_screen.title");
        boolean needCreateNtServiceOption = false;

        // Only add the NT Service check box for Windows, which will be
        // enabled/disabled
        // based on the appserver setting. For other OSes, don't display the
        // check box.
        if (m_installer.getOperatingSystem() == Install.OS_WINDOWS)
        {
            needCreateNtServiceOption = true;
        }

        ArrayList<JCheckBox> checkboxList = new ArrayList<JCheckBox>();
        checkboxList.add(m_copyConfigurationsCheckBox);
        checkboxList.add(m_mergePropertiesCheckBox);
        if (needCreateNtServiceOption)
        {
            checkboxList.add(m_createNtServiceCheckBox);
        }
        checkboxList.add(m_createDataBaseCheckBox);

        ArrayList<String> checkboxNameList = new ArrayList<String>();
        checkboxNameList.add(m_installAmbassadorProperties
                .getString("copy_configuration"));
        checkboxNameList.add(m_installAmbassadorProperties
                .getString("merge_properties"));
        if (needCreateNtServiceOption)
        {
            checkboxNameList.add(m_installAmbassadorProperties
                    .getString("create_nt_service"));
        }
        checkboxNameList.add(m_installAmbassadorProperties
                .getString("create_database"));

        JPanel checkboxesInnerPanel = new JPanel(new GridLayout(checkboxList
                .size(), 1));
        for (int i = 0; i < checkboxList.size(); i++)
        {
            JCheckBox checkbox = (JCheckBox) checkboxList.get(i);
            checkbox.setText((String) checkboxNameList.get(i));
            checkbox.setSelected(checkbox != m_createDataBaseCheckBox
                    && checkbox != m_mergePropertiesCheckBox);
            checkbox.setFont(m_font);
            checkbox.setBackground(m_bgColor);
            checkboxesInnerPanel.add(checkbox);
        }

        JPanel checkboxesOutterPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT));
        checkboxesOutterPanel.add(checkboxesInnerPanel);

        // Add the progress bar and the two labels for the progress bar
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel progressInnerPanel = new JPanel(new GridLayout(3, 1));
        m_progressBar = new JProgressBar();

        m_fileCopyLabel.setFont(m_font.deriveFont(Font.PLAIN,
                m_font.getSize() - 1));
        m_groupCopyLabel.setFont(m_font.deriveFont(Font.PLAIN));
        m_progressBar.setPreferredSize(new Dimension(520, 20));
        m_progressBar.setOpaque(false);
        m_fileCopyLabel.setPreferredSize(new Dimension(520, 20));

        progressInnerPanel.add(m_groupCopyLabel);
        progressInnerPanel.add(m_fileCopyLabel);
        progressInnerPanel.add(m_progressBar);

        m_copier.addActionListener(this);
        m_installer.addActionListener(this);

        progressPanel.add(progressInnerPanel);
        progressPanel.setSize(800, 30);

        JPanel checkboxAndProgressPanel = new JPanel(new BorderLayout());
        checkboxAndProgressPanel
                .add(checkboxesOutterPanel, BorderLayout.CENTER);
        checkboxAndProgressPanel.add(progressPanel, BorderLayout.SOUTH);
        setVerticalGap(30);
        setHorizontalGap(50);
        JPanel installOptionsScreen = titleAndPadPanel(
                checkboxAndProgressPanel, optionsScreenTitle, "");
        setVerticalGap(0);
        setHorizontalGap(0);

        checkboxAndProgressPanel.setBackground(m_bgColor);
        progressPanel.setBackground(m_bgColor);
        progressInnerPanel.setBackground(m_bgColor);
        checkboxesOutterPanel.setBackground(m_bgColor);
        checkboxesInnerPanel.setBackground(m_bgColor);

        addScreen(installOptionsScreen);
    }

    private void setParameterDependency() throws IOException,
            MissingResourceException
    {
        String parentList = m_groupProperties.getProperty("parent_controls");

        if (parentList != null)
        {
            ArrayList<String> parentControls = parseList(parentList);

            for (int i = 0; i < parentControls.size(); i++)
            {
                String key = (String) parentControls.get(i);
                keyExists(m_groupProperties, key,
                        "The list of parameters dependent on " + key
                                + " is not found.");

                String group = m_groupProperties.getProperty(key);
                if (group != null)
                {
                    // The key is the parent parameter. Using the key
                    // name, we find its index in the install display
                    // list. Using the index, we get the control.
                    InputField parent = (InputField) m_inputFieldList
                            .get(m_installOrder.indexOf(key));

                    // Parse the list of children parameters
                    ArrayList<String> childControls = parseList(group);
                    for (int j = 0; j < childControls.size(); j++)
                    {
                        // Find the child control using the index of
                        // the parameter
                        int index = m_installOrder
                                .indexOf(((String) childControls.get(j)).trim());
                        InputField child = (InputField) m_inputFieldList
                                .get(index);
                        parent.addChild(child);
                    }

                    parent.validateDependency();
                }
            }
        }
    }

    private void keyExists(Properties p_properties, String p_key,
            String p_message) throws MissingResourceException
    {
        if (!p_properties.containsKey(p_key))
        {
            MissingResourceException ex = new MissingResourceException(
                    p_message, getClass().getName(), p_key);
            throw ex;
        }
    }

    private void saveInstallValues()
    {
        String saveFailureText = m_installAmbassadorProperties
                .getString("save_param_failure");

        try
        {
            for (int i = 0; i < m_inputFieldList.size(); i++)
            {
                InputField parameter = (InputField) m_inputFieldList.get(i);

                String userInput = parameter.getValue();
                m_installer.setInstallValue(m_installOrder.get(i).toString(),
                        userInput);
            }

            m_installer.addAdditionalInstallValues();
            m_installer.storeUserInput();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            showErrorDialogAndQuit(saveFailureText + "\n" + ex.toString());
        }
    }

    private void startProgress(String textLabel)
    {
        String preparationText = m_installAmbassadorProperties
                .getString("preparing");

        m_groupCopyLabel.setText(textLabel);
        m_fileCopyLabel.setText(preparationText);
        m_progressBar.setValue(0);
    }

    private void endProgress()
    {
        m_fileCopyLabel.setText("");
        m_progressBar.setValue(m_progressBar.getMaximum());
    }

    private boolean validateJarSign()
    {
    	boolean enable = "true".equalsIgnoreCase(m_installer.getInstallValue("jar_sign_enable"));
    	if (enable)
    	{
            String keyStore = m_installer.getInstallValue("jar_sign_jks");
            keyStore = keyStore.trim();
            File r = new File(keyStore);
            if (!r.isFile())
            {
            	showErrorDialog(m_installAmbassadorProperties.getString("error.keystore_file"));
            	return false;
            }
            
            keyStore = r.getAbsolutePath();
            String keyPass = m_installer.getInstallValue("jar_sign_pwd");
            String keyAlias = m_installer.getInstallValue("jar_sign_keyAlias");
            keyPass = keyPass.trim();
            keyAlias = keyAlias.trim();
            if (JarSignUtil.validate(keyStore, keyPass, keyAlias))
            {
                JKS = keyStore;
                this.keyPass = keyPass;
                this.keyAlias = keyAlias;
            }
            else
            {
                int confirmation = showQuestionDialog(m_installAmbassadorProperties
                        .getString("alert.keystore_password"),
                        InstallerFrame.NO_OPTION);

                if (confirmation == InstallerFrame.NO_OPTION)
                {
                    return false;
                }
            }
    	}
    	
    	return true;
    }
    
    private void doInstall()
    {
        final String confirmCreateDBText = m_installAmbassadorProperties
                .getString("alert.confirm_create_db");
        final String installationSuccessText = m_installAmbassadorProperties
                .getString("installation_success");
        final String installationFailureText = m_installAmbassadorProperties
                .getString("installation_failure");
        final String updateDatabaseText = m_installAmbassadorProperties
                .getString("update_database");
        
        if (!validateJarSign())
        {
        	return;
        }

        // If Create Database is selected, then confirm before executing
        if (m_createDataBaseCheckBox.isSelected())
        {
            int confirmation = showQuestionDialog(confirmCreateDBText,
                    InstallerFrame.NO_OPTION);

            if (confirmation == InstallerFrame.NO_OPTION)
            {
                // If the user chose No, then do nothing here, which
                // returns control to the user.
                return;
            }
        }

        final SwingWorker worker = new SwingWorker()
        {
            public void finished()
            {
                StringBuilder gsUrl = new StringBuilder(m_installAmbassadorProperties.getString("login_message"));

                
                boolean enableSSL = "true".equalsIgnoreCase(m_installer.getInstallValue("server_ssl_enable"));
                boolean usePublicUrl = "true".equalsIgnoreCase(m_installer.getInstallValue("cap_public_url_enable"));

                if (usePublicUrl)
                {
                    gsUrl.append(m_installer.getInstallValue("cap_public_url"));
                }
                else if (enableSSL)
                {
                    gsUrl.append(m_installer.getInstallValue("cap_login_url_ssl"));
                }
                else
                {
                    String hostname = m_installer.getInstallValue("server_host");
                    String port = m_installer.getInstallValue("server_port");
                    gsUrl.append("http://" + hostname);

                    if (!"80".endsWith(port))
                    {
                        gsUrl.append(":").append(port);
                    }

                    gsUrl.append("/globalsight");
                }

                m_fileCopyLabel.setFont(m_font.deriveFont(Font.PLAIN, m_font
                        .getSize()));
                m_fileCopyLabel.setText(gsUrl.toString());
                m_groupCopyLabel.setText(installationSuccessText);
                m_quitButton.setText("Close");
                m_quitButton.setEnabled(true);

                if (m_mergePropertiesCheckBox.isSelected())
                {
                    setVisible(false);
                    try
                    {
                        new MergePropertiesGUI(previousAmbassadorHome,
                                Install.GS_HOME);
                    }
                    catch (IOException ex)
                    {
                        System.out.println(ex);
                    }
                }
            }

            private void copyConfigurationFiles() throws Exception
            {
                startProgress(m_copyConfigurationsCheckBox.getText());
                m_progressBar.setMaximum(m_installer.countConfigurationFiles());
                m_installer.processFiles();
                endProgress();
            }

            private void createDatabase() throws Exception
            {
                startProgress(m_createDataBaseCheckBox.getText());
                m_progressBar.setMaximum(m_installer
                        .countCreateDatabaseCommands());
                m_installer.createDatabaseTables();

                // Verify that the LEV_MATCH and FUZZY_IDX stored procedures are
                // valid
                // if (!m_installer.validate_stored_procedures())
                // {
                // // Invalid procedures, show error and quit
                // showErrorDialogAndQuit(invalidProcedureText);
                // }

                endProgress();
            }

            private void updateDatabase() throws Exception
            {
                startProgress(updateDatabaseText);
                m_progressBar.setMaximum(m_installer
                        .countUpdateDatabaseCommands());
                m_installer.updateDatabaseTables();
                endProgress();
            }
            
            private void signJar()
            {
            	if (JKS != null)
            	{
            		File root = new File(
                			Install.GS_HOME
            						+ "/jboss/server/standalone/deployments/globalsight.ear/globalsight-web.war/applet/lib");
            		JarSignUtil.updateJars(root, JKS, keyPass, keyAlias, m_fileCopyLabel, m_progressBar);
            	}
            }

            private void createNtService() throws IOException
            {
                m_installer.installGlobalSightService();
            }

            public Object construct()
            {
                m_quitButton.setEnabled(false);
                m_backButton.setEnabled(false);
                m_installButton.setEnabled(false);
                m_loadSettingsButton.setEnabled(false);

                try
                {
                    if (m_copyConfigurationsCheckBox.isSelected())
                    {
                        copyConfigurationFiles();
                    }

                    if (m_createNtServiceCheckBox.isEnabled()
                            && m_createNtServiceCheckBox.isSelected())
                    {
                        createNtService();
                    }

                    if (m_createDataBaseCheckBox.isSelected())
                    {
                        createDatabase();
                    }
                    else
                    {
                        updateDatabase();
                    }
                    //Create start menu in windows.
                    m_installer.createStartMenu();
                    startProgress("Sign applet jar");
                    signJar();
                    endProgress();
                    System.out.println("\nDone.");
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    showErrorDialogAndQuit(installationFailureText + "\n"
                            + ex.toString());
                }

                return null;
            }
        };
        worker.start();
    }

    public static void setCurrentLocale(Locale p_locale)
    {
        s_locale = p_locale;
        InputField.setCurrentLocale(p_locale);
    }

    private void loadValues()
    {
        JFileChooser chooser = new JFileChooser();
        chooser
                .setDialogTitle("Please select the previous Ambassador home directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (previousAmbassadorHome.equals("")
                || previousAmbassadorHome.equals("../.."))
        {
            chooser.setCurrentDirectory((new File(Install.GS_HOME))
                    .getParentFile());
        }
        else
        {
            chooser.setSelectedFile(new File(previousAmbassadorHome));
        }

        int returnVal = chooser.showDialog(this, "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            previousAmbassadorHome = chooser.getSelectedFile()
                    .getAbsolutePath();
            if (!previousAmbassadorHome.equals(""))
            {
                File installValues = new File(previousAmbassadorHome
                        + "/install/data/installValues.properties");
                if (installValues.exists())
                {
                    Install.setLastInstallValuesLocation(previousAmbassadorHome
                            + "/install/data/installValues.properties");
                    try
                    {
                        m_installer.loadInstallValues();
                        for (int i = 0; i < m_inputFieldList.size(); i++)
                        {
                            InputField parameter = (InputField) m_inputFieldList
                                    .get(i);

                            parameter.setValue(m_installer
                                    .getInstallValue(m_installOrder.get(i)
                                            .toString()));
                            parameter.validateLabel();
                        }
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                        showErrorDialog(ex.toString());
                    }
                }
                else
                {
                    showErrorDialog(installValues.getAbsolutePath()
                            + " does not exist.");
                }
            }
        }
    }
    
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        boolean doContinue = true;
        if (source instanceof JButton)
        {
            if (source.equals(m_loadSettingsButton))
            {
                loadValues();
            }
            else if (source.equals(m_installButton))
            {
            	
                saveInstallValues();
                m_installer.addAdditionalInstallValues();
                doInstall();
            }
            else if (source.equals(m_quitButton))
            {
                saveInstallValues();
                System.exit(0);
            }
            else if (source.equals(m_nextButton))
            {
                if (getCurrentScreen() == m_appServerScreenNum)
                {
                    boolean needCreateNtServiceOption = m_installer
                            .getOperatingSystem() == Install.OS_WINDOWS;

                    m_createNtServiceCheckBox
                            .setEnabled(needCreateNtServiceOption);
                    m_createNtServiceCheckBox
                            .setSelected(needCreateNtServiceOption);
                }

                boolean canAdvance = true;

                for (int i = 0; i < m_inputFieldList.size(); i++)
                {
                    InputField parameter = (InputField) m_inputFieldList.get(i);
                    if (parameter.getInstallerScreen() == getCurrentScreen()
                            && !parameter.isValidValue())
                    {
                        canAdvance = false;
                    }
                }

                if (!canAdvance)
                {
                    int confirmation = showQuestionDialog(m_confirmAdvance,
                            InstallerFrame.NO_OPTION);
                    doContinue = confirmation == InstallerFrame.YES_OPTION;
                }
            }
        }
        else if (source instanceof Install || source instanceof RecursiveCopy)
        {
            if (!e.getActionCommand().equals(""))
            {
                m_fileCopyLabel.setText(e.getActionCommand());
            }

            m_progressBar.setValue(m_progressBar.getValue() + 1);
        }

        if (doContinue)
        {
            super.actionPerformed(e);
        }
    }

    public static void main(String[] args)
    {
        Utilities.requireJava14();

        boolean useUI = true;

        // find out what the args were
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if ("-noUI".equals(arg))
            {
                useUI = false;
            }
            else if ("-locale".equals(arg) && i + 1 < args.length)
            {
                Locale locale = new Locale(args[i + 1].substring(0, 2),
                        args[i + 1].substring(3, 5));
                InstallAmbassador.setCurrentLocale(locale);
                ++i;
            }
            else if ("-properties".equals(arg) && i + 1 < args.length)
            {
                Install.setLastInstallValuesLocation(args[i + 1]);
                ++i;
            }
        }
        
        if (useUI)
        {
            System.out.println("Call with -noUI to turn off the Install GUI");
            new InstallAmbassador();
        }
        else
        {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    INSTALL_UI_PROPERTIES, s_locale);

            if (!checkInstallPath())
            {
                System.out.println(bundle.getString("alert.path_containS_space"));
                System.out.println(bundle.getString("alert.end"));
                try
                {
                    System.in.read();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                System.exit(1);
            }
            Install.main(args);
        }
    }
}
