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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import util.CaseInsensitiveComparator;
import util.MapsAndPropertiesFilter;
import util.MergeInterface;
import util.PropertyList;
import util.Utilities;

public class MergePropertiesGUI
    extends InstallerFrame
    implements ActionListener, MergeInterface
{
    private static final long serialVersionUID = -3631016644563351375L;

    private InputField m_previousAmbassadorHome;
    private InputField m_currentAmbassadorHome;
    private JList m_filesList;
    private JButton m_checkAllButton;
    private JButton m_clearAllButton;
    private Font m_monoSpaceFont = new Font("Courier", Font.PLAIN, 12);
    private Color m_rowColor = Color.white;
    private Color m_rowAltColor = new Color(239, 239, 239);
    private JPanel m_fieldListPanel;
    private Hashtable<String, Object> m_fieldCheckBoxList = new Hashtable<String, Object>();
    private GridBagLayout m_gridbagLayout = new GridBagLayout();
    private GridBagConstraints m_gridbagConstraint = new GridBagConstraints();
    
    private MergeProperties m_mergeProperties = new MergeProperties(this);

    public MergePropertiesGUI(String p_previousAmbassadorHome, String p_currentAmbassadorHome)
        throws IOException
    {
        File destinationHome = new File(p_currentAmbassadorHome);
        File destinationDir = new File(p_currentAmbassadorHome + MergeProperties.PROPERTIES_PATH);
        if (!destinationDir.exists()) {
            System.out.println("The current GlobalSight home " 
                + destinationHome.getCanonicalPath() + " is not valid.");
            System.out.println("It does not contain the folder " 
                + MergeProperties.PROPERTIES_PATH + ".");
            System.out.println("Please specify the current GlobalSight home with the -currentHome option.");
            System.exit(1);
        }

        setVerticalGap(0);
        setHorizontalGap(0);

        // Set the layout to put each item in a separate row.  The
        // item's height is the default, while its width fills the
        // width of the panel.
        m_gridbagConstraint.gridwidth = GridBagConstraints.REMAINDER;
        m_gridbagConstraint.fill = GridBagConstraints.HORIZONTAL;
        m_gridbagConstraint.weightx = 1.0;

        addSelectFilesScreen(p_previousAmbassadorHome, p_currentAmbassadorHome);
        addSelectPropertiesScreen();
        setFirstScreen();
        
        setTitle("Merge Properties");
        m_installButton.setText("Update");

//      show();
        this.setVisible(true);
    }
    
    
    private void addSelectFilesScreen(String p_previousAmbassadorHome, String p_currentAmbassadorHome)
    throws IOException
    {
        File previousAmbassadorHome = new File(p_previousAmbassadorHome);
        if (previousAmbassadorHome.exists()) {
            p_previousAmbassadorHome = previousAmbassadorHome.getCanonicalPath();
        }

        // Create the controls to select the Ambassador home
        ArrayList<String> typesList = new ArrayList<String>();
        typesList.add("browse-directory");
        m_previousAmbassadorHome = new InputField(
                    "Previous GlobalSight Home", typesList, p_previousAmbassadorHome, 
                    getNumberOfScreens(), TitledBorder.TOP);

        m_currentAmbassadorHome = new InputField(
                    "Current GlobalSight Home", typesList, p_currentAmbassadorHome, 
                    getNumberOfScreens(), TitledBorder.TOP);

        JPanel topPanel = new JPanel(new GridLayout(1, 1, 0, 0));
        topPanel.add(m_previousAmbassadorHome);
        //topPanel.add(currentAmbassadorHome);
        
        // Read the names of the properties files from the 
        // current Ambassador home and sort the list
        File propertiesFolder = new File(p_currentAmbassadorHome + MergeProperties.PROPERTIES_PATH);
        MapsAndPropertiesFilter mapsAndPropertiesFilter = new MapsAndPropertiesFilter();
        String fileNames[] = propertiesFolder.list(mapsAndPropertiesFilter);
        CaseInsensitiveComparator caseInsensitiveComparator = new CaseInsensitiveComparator();
        Arrays.sort(fileNames, caseInsensitiveComparator);

        // Create the list of files to select
        m_filesList = new JList(fileNames);
        JScrollPane sourceScrollPane = new JScrollPane(m_filesList);
        titlePanel(sourceScrollPane, 
                "Properties Files", m_font.getSize());
        m_filesList.setFont(m_font);
        
        // Add the panels to a main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(sourceScrollPane, BorderLayout.CENTER);

        JPanel selectScreen = titleAndPadPanel(
                mainPanel,
                "Select Files", 
                "");

        sourceScrollPane.setBackground(m_bgColor);
        mainPanel.setBackground(m_bgColor);
        topPanel.setBackground(m_bgColor);
        m_previousAmbassadorHome.setFont(m_font);
        
        // Preselect a list of files
        Properties selectedList = loadProperties(MergeProperties.MERGEPROPERTIES_FILE);
        ArrayList<String> selectedFiles = parseList(selectedList.getProperty("merge_files"));
        selectedFiles.addAll(parseList(selectedList.getProperty("merge_comfirm_files")));
        int selectedIndices[] = new int[selectedFiles.size()];
        for (int i = 0; i < selectedFiles.size(); i++) {
            m_filesList.setSelectedValue(selectedFiles.get(i), false);
            selectedIndices[i] = m_filesList.getSelectedIndex();
        }
        m_filesList.setSelectedIndices(selectedIndices);
        
        addScreen(selectScreen);
    }

    private void addSelectPropertiesScreen()
    throws IOException
    {
        m_fieldListPanel = new JPanel(m_gridbagLayout);

        m_checkAllButton = new JButton("Check All");
        m_clearAllButton = new JButton("Clear All");
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(m_checkAllButton);
        bottomPanel.add(m_clearAllButton);

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(m_fieldListPanel, BorderLayout.NORTH);
        JScrollPane valueScrollPane = new JScrollPane(innerPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(valueScrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel statusScreen = titleAndPadPanel(
                mainPanel,
                "Select Properties", 
                "");

        m_checkAllButton.setBackground(m_bgColor);
        m_clearAllButton.setBackground(m_bgColor);
        innerPanel.setBackground(m_rowColor);
        mainPanel.setBackground(m_bgColor);
        bottomPanel.setBackground(m_bgColor);
        m_fieldListPanel.setBackground(m_rowColor);

        m_checkAllButton.setFont(m_font);
        m_clearAllButton.setFont(m_font);
        m_checkAllButton.addActionListener(this);
        m_clearAllButton.addActionListener(this);

        addScreen(statusScreen);
    }

    private String htmlEncode(String p_text)
    {
        if (p_text != null) {
            p_text = p_text.replaceAll("&", "&amp;");
            p_text = p_text.replaceAll("<", "&lt;");
        }
        
        return p_text;
    }

    private void addRow(String p_fileName, String p_key, String p_comment, 
        String p_message, String p_previousValue, 
        String p_currentValue, String p_status)
    {
        Color bgColor = (m_fieldListPanel.getComponentCount() % 2 == 1 ? m_rowAltColor : m_rowColor);
        p_comment = p_comment.replaceAll("\n", "<BR>");

        JComponent checkbox;
        JLabel label;
        if (m_mergeProperties.isCompareMode() && p_key != null) {
            checkbox = new JCheckBox("", true);
            m_fieldCheckBoxList.put(p_fileName + "|" + p_key, checkbox);
        }
        else {
            checkbox = new JLabel();
        }        

        label = new JLabel(
                    "<HTML><B>" + p_fileName + "</B>"
                    + p_comment 
                    + (p_key == null ? "" : p_key + "<FONT COLOR='blue'> " + p_message + "</FONT>")
                    + (p_previousValue != null
                            ? "<BR><FONT COLOR='blue'>- Previous value: </FONT>" + p_previousValue 
                            : "")
                    + (p_currentValue != null
                            ? "<BR><FONT COLOR='blue'>- Current value: &nbsp;</FONT>" + p_currentValue
                            : "")
                    + (p_status != null && !"".equals(p_status) 
                            ? "<BR><FONT COLOR='green'>" + p_status + "</FONT>"
                            : "")
                    + "</HTML>");

        checkbox.setPreferredSize(new Dimension(25, 15));
        label.setFont(m_monoSpaceFont);

        JPanel innerPanel = new JPanel(new FlowLayout());
        innerPanel.add(checkbox);
        innerPanel.add(label);
        
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.add(innerPanel, BorderLayout.WEST);
        rowPanel.setBackground(bgColor);
        checkbox.setBackground(bgColor);
        innerPanel.setBackground(bgColor);
        m_gridbagLayout.setConstraints(rowPanel, m_gridbagConstraint);

        m_fieldListPanel.add(rowPanel);        
        m_fieldListPanel.revalidate();
    }
    
    private void addCheckBox(String p_fileName, String p_key, String p_comment, 
        String p_message, String p_previousValue, 
        String p_currentValue)
    {
        addCheckBox(p_fileName, p_key, p_comment, p_message, p_previousValue, p_currentValue, null);
    }
    
    private void addCheckBox(String p_fileName)
    {
        addCheckBox(p_fileName, "", null, "", null, null, null);
    }
    
    private void addCheckBox(String p_fileName, String p_status)
    {
        addCheckBox(p_fileName, "", null, "", null, null, p_status);
    }
    
    private void addCheckBox(String p_fileName, String p_key, String p_comment, 
        String p_message, String p_previousValue, 
        String p_currentValue, String p_status)
    {
        p_comment = htmlEncode(p_comment);
        addRow(p_fileName, 
            p_key, 
            (p_comment == null ? "" : "<BR><FONT COLOR='gray'>" + p_comment + "</FONT>"), 
            htmlEncode(p_message), 
            htmlEncode(p_previousValue), 
            htmlEncode(p_currentValue), 
            p_status);
    }
    
    private void addNonCheckBox(String p_fileName, String p_text)
    {
        addNonCheckBox(p_fileName, p_text, null);
    }
    
    private void addNonCheckBox(String p_fileName, String p_text, String p_status)
    {
        p_text = htmlEncode(p_text);
        p_text = "<BR><FONT COLOR='blue'>" + p_text + "</FONT>";
        addRow(p_fileName, null, p_text, null, null, null, p_status);
    }
    
    private boolean isCheckboxSelected(String p_fileName, String p_key)
    {
        boolean isSelected = ((JCheckBox) m_fieldCheckBoxList.get(p_fileName + "|" + p_key)).isSelected();
        
        return isSelected;
    }
    
    private int isCheckboxSelectedForFile(String p_fileName)
    {
        int isSelected = EQUAL;
        
        for (Enumeration<?> e = m_fieldCheckBoxList.keys(); e.hasMoreElements() ;) {
            String key = e.nextElement().toString();
            if (key.startsWith(p_fileName + "|")) {
                isSelected = IGNORE;
                if (((JCheckBox) m_fieldCheckBoxList.get(key)).isSelected())
                {
                    isSelected = UPDATE;
                    
                    break;
                }
            }
        }
 
        return isSelected;
    }
    
    public int checkToUpdateValue(String p_fileName, String p_key, 
        String p_comment, String p_oldValue, String p_newValue)
    throws IOException
    {
        boolean doUpdate = false;
        
        // Key is ignored
        if (m_mergeProperties.shoudIgnore(p_key)) {
            return MergeInterface.EQUAL;
        }
        // Values are the same
        else if (PropertyList.compareValues(p_oldValue, p_newValue)) {
            return MergeInterface.EQUAL;
        }
        // Values are different
        else {
            if (m_mergeProperties.isCompareMode()) {
                addCheckBox(p_fileName, p_key, p_comment, "", p_oldValue, p_newValue);
                return MergeInterface.IGNORE;
            }
            else {
                doUpdate = isCheckboxSelected(p_fileName, p_key);
                addCheckBox(p_fileName, p_key, p_comment, "", p_oldValue, 
                    p_newValue, (doUpdate ? "Updated." : "Ignored."));
            }
        }
        
        return doUpdate ? MergeInterface.UPDATE : MergeInterface.IGNORE;
    }
    
    public int checkToAddKey(String p_fileName, String p_key, String p_comment, String p_value)
    throws IOException
    {
        if (m_mergeProperties.isCompareMode()) {
            addCheckBox(p_fileName, p_key, p_comment, "is in the "
                + "previous version but not in the current version", p_value, null);
        }
        else {
            boolean doUpdate = isCheckboxSelected(p_fileName, p_key);
 
            addCheckBox(p_fileName, p_key, p_comment, "", p_value, 
                null, (doUpdate ? "Added." : "Ignored."));
        
            return doUpdate ? MergeInterface.UPDATE : MergeInterface.IGNORE;
        }
        
        return MergeInterface.EQUAL;
    }
    
    public int checkToCopyFile(String p_fileName)
    throws IOException
    {
        if (m_mergeProperties.isCompareMode()) {
            addCheckBox(p_fileName);
            return MergeInterface.EQUAL;
        }
        else {
            boolean doUpdate = isCheckboxSelected(p_fileName, "");
            addCheckBox(p_fileName, (doUpdate ? "Copied." : "Ignored."));
            
            return doUpdate ? MergeInterface.UPDATE : MergeInterface.IGNORE;
        }
    }
    
    public void noDifference(String p_fileName)
    throws IOException
    {
        addNonCheckBox(p_fileName, "All values are the same.");
    }
    
    private void performMergeFromUI(int p_mergeMode)
    throws IOException
    {
        final File sourceDir = new File(m_previousAmbassadorHome.getValue() + MergeProperties.PROPERTIES_PATH);
        final File destinationDir = new File(m_currentAmbassadorHome.getValue() + MergeProperties.PROPERTIES_PATH);
        
        if (!sourceDir.exists()) {
            showErrorDialog("The folder " + sourceDir.getAbsolutePath() + " is not found.\n"
                + "Please reset the previous GlobalSight home directory.");
        }
        else if (sourceDir.getCanonicalPath().equals(destinationDir.getCanonicalPath())) {
            showErrorDialog("The previous GlobalSight home is the same as the current GlobalSight home.\n"
                + "Please reset the previous GlobalSight home directory.");
        }
        else if (sourceDir.exists() && destinationDir.exists()) {
            if (p_mergeMode != MergeProperties.MERGE_MODE) {
                m_fieldCheckBoxList.clear();
                m_fieldListPanel.removeAll();
                ActionEvent advanceScreen = new ActionEvent(m_nextButton, 0, "");
                super.actionPerformed(advanceScreen);
            }
            else {
                m_fieldListPanel.removeAll();
            }

            m_mergeProperties.setMode(p_mergeMode);

            final SwingWorker worker = new SwingWorker()
            {
                public void finished()
                {
                    m_quitButton.setEnabled(true);
                    m_backButton.setEnabled(true);
                    if (m_fieldCheckBoxList.size() > 0 && m_mergeProperties.isCompareMode())
                        m_installButton.setEnabled(true);
                }

                public Object construct()
                {
                    m_quitButton.setEnabled(false);
                    m_backButton.setEnabled(false);
                    m_installButton.setEnabled(false);

                    Object[] selectedFiles = m_filesList.getSelectedValues();
                    for (int i = 0; i < selectedFiles.length; i++) {
                        try {
                            String fileName = selectedFiles[i].toString();
                            if (m_mergeProperties.isCompareMode())
                            {
                                m_mergeProperties.mergeFiles(sourceDir.getCanonicalPath() + "/" + fileName, 
                                        destinationDir.getCanonicalPath());
                            }
                            else {
                                int doUpdate = isCheckboxSelectedForFile(fileName);
                                if (doUpdate == UPDATE) {
                                    m_mergeProperties.mergeFiles(sourceDir.getCanonicalPath() + "/" + fileName, 
                                            destinationDir.getCanonicalPath());
                                }
                                else if (doUpdate == IGNORE) {
                                    addNonCheckBox(fileName, "Not selected.", "Ignored.");
                                }
                                else if (doUpdate == EQUAL) {
                                    addNonCheckBox(fileName, "All values are the same.", "Ignored.");
                                }
                            }
                        }
                        catch (IOException ex) {
                        }
                    }
 
                    return null;
                }
            };
            worker.start();
        }
    }
    

    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();
        boolean doContinue = true;
        if (source instanceof JButton)
        {
            if (source.equals(m_checkAllButton)
                || source.equals(m_clearAllButton)) 
            {
                boolean isSelected = (source.equals(m_checkAllButton));
                for (Enumeration<?> en = m_fieldCheckBoxList.keys(); en.hasMoreElements(); ) {
                    Object key = en.nextElement();
                    Object checkbox = m_fieldCheckBoxList.get(key);
                    if (checkbox instanceof JCheckBox) { 
                        ((JCheckBox) checkbox).setSelected(isSelected);
                    }
                }
            }
            else if (source.equals(m_nextButton)
                || source.equals(m_installButton))
            {
                doContinue = false;
                
                try {
                    if (source.equals(m_nextButton)) {
                        performMergeFromUI(MergeProperties.COMPARE_MODE);
                    }
                    else if (source.equals(m_installButton)) {
                        performMergeFromUI(MergeProperties.MERGE_MODE);
                    }
                }
                catch (IOException ex) {
                    System.out.println(ex);
                    showErrorDialog(ex.getMessage());
                }
            }
        }

        if (doContinue)
        {
            super.actionPerformed(e);
        }
    }

    public static void main(String[] args)
    {
        Utilities.requireJava14();
        
        boolean noUI = false;
        String sourceHome = "../..";
        String targetHome = "..";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-noui")) {
                noUI = true;
            }
            else if (args[i].equalsIgnoreCase("-previousHome")) {
                sourceHome = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-currentHome")) {
                targetHome = args[++i];
            }
            else if (args[i].toLowerCase().startsWith("-h")) {
                System.out.println("Usage: ");
                System.out.println("    java -classpath . MergeProperties "
                        + "[-previousHome <previous GlobalSight Home>] "
                        + "[-currentHome <current GlobalSight Home]");
                System.exit(0);
            }
        }

        try {
            if (noUI) {
                MergeProperties.main(args);
            }
            else {
                new MergePropertiesGUI(sourceHome, targetHome);
            }
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
    }
}