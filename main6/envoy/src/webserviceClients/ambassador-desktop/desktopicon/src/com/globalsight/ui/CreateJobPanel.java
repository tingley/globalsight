package com.globalsight.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;

import com.globalsight.action.GetAttributeAction;
import com.globalsight.action.QueryAction;
import com.globalsight.cvsoperation.entity.ModuleMapping;
import com.globalsight.cvsoperation.util.ModuleMappingHelper;
import com.globalsight.entity.FileProfile;
import com.globalsight.entity.User;
import com.globalsight.ui.attribute.AttributeTableModel;
import com.globalsight.ui.attribute.JobAttributeDialog;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.SortHelper;
import com.globalsight.util.StringHelper;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.ValidationHelper;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;

public class CreateJobPanel extends JPanel
{
    private static final long serialVersionUID = 198902272008L;

    // Added by Vincent Yan
    private boolean isFromCVS = false;
    private HashMap<File, String> cvsModules = new HashMap<File, String>();
    private String fpSourceLocale = "";
    JobAttributeDialog jobAttributeDialog = new JobAttributeDialog(
            SwingHelper.getMainFrame());

    // ///////////////////////////////////////////////////////////////
    // constructor
    // ///////////////////////////////////////////////////////////////
    public CreateJobPanel()
    {
        m_filesModel = new DefaultListModel();
        m_fileProfilesModel = new DefaultListModel();
        initPanel();
        initActions();
    }

    // ///////////////////////////////////////////////////////////////
    // public methods
    // ///////////////////////////////////////////////////////////////
    public void addAllFiles(File[] p_files)
    {
        if (p_files != null)
        {
            for (int i = 0; i < p_files.length; i++)
            {
                addFile(p_files[i]);
            }
        }

        if (m_filesModel.size() == 1)
        {
            m_filesList.setSelectedIndex(0);
        }
        else
        {
            if (m_filesList.getSelectedIndex() == -1
                    && m_filesModel.size() != 0)
            {
                jComboBoxFileProfiles.removeAllItems();
                jComboBoxFileProfiles.addItem(Constants.PLEASE_SELECT_FILE);
            }
        }

        setMaxFileNumAndMaxJobSize();
    }

    public void delFile(int p_index)
    {
        if (p_index != -1)
        {
            m_filesModel.remove(p_index);
            m_fileProfilesModel.remove(p_index);
            resumeOriComboBox();
        }
    }

    // Delete them from decreasing order for avoid
    // ArrayIndexOutOfBoundsException
    public void delFile(int[] p_indices)
    {
        int len = p_indices.length;
        for (int i = 1; i <= len; i++)
        {
            delFile(p_indices[len - i]);
        }
    }

    // ///////////////////////////////////////////////////////////////
    // private methods
    // ///////////////////////////////////////////////////////////////
    private void initPanel()
    {
        int width31 = SwingHelper.getMainFrame().getWidth() / 3 - 10;
        int height21 = SwingHelper.getMainFrame().getHeight() / 2 - 10;
        Container contentPane = this;
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.insets = new Insets(2, 2, 2, 2);

        // File(s) Label
        JLabel label2 = new JLabel(fileLableName, Label.RIGHT);
        c1.gridx = 0;
        c1.gridy = 0;
        c1.gridwidth = 3;
        c1.gridheight = 1;
        contentPane.add(label2, c1);

        // File Profile Label
        JLabel label3 = new JLabel(fileProfileIDLableName, Label.RIGHT);
        c1.gridx = 3;
        c1.gridy = 0;
        c1.gridwidth = 2;
        c1.gridheight = 1;
        contentPane.add(label3, c1);

        // Mapping Result Label
        JLabel label6 = new JLabel(mappingResult, Label.RIGHT);
        c1.gridx = 5;
        c1.gridy = 0;
        c1.gridwidth = 3;
        c1.gridheight = 1;
        contentPane.add(label6, c1);

        // Added files
        m_filesList = new JList(m_filesModel);
        m_filesList.setAutoscrolls(true);
        m_filesList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_filesList.setTransferHandler(FileTransferHandler.install());
        jScrollPaneFiles = new JScrollPane(m_filesList);
        jScrollPaneFiles.setMinimumSize(new Dimension(width31, 120));// 100
        jScrollPaneFiles
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        c1.gridx = 0;
        c1.gridy = 1;
        c1.gridwidth = 3;
        c1.gridheight = 4;
        contentPane.add(jScrollPaneFiles, c1);

        jComboBoxFileProfiles = new JComboBox();
        jComboBoxFileProfiles.setRenderer(new ComboBoxRenderer());
        if (m_filesModel.size() == 0)
            jComboBoxFileProfiles.addItem(Constants.PLEASE_ADD_FILE);
        else
            jComboBoxFileProfiles.addItem(Constants.PLEASE_SELECT_FILE);
        jComboBoxFileProfiles.setPreferredSize(new Dimension(220, 30));
        JScrollPane scrollPaneFP = new JScrollPane(jComboBoxFileProfiles);
        scrollPaneFP.setMinimumSize(new Dimension(250, 50));// 220,50
        scrollPaneFP
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPaneFP
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        c1.gridx = 3;
        c1.gridy = 1;
        c1.gridwidth = 2;
        c1.gridheight = 1;
        contentPane.add(scrollPaneFP, c1);
        // "Map Files" & "Clear Map" buttons
        mappingButton = new JButton(mappingButtonName);
        mappingButton.setToolTipText(mappingButtonName);
        mappingButton.setEnabled(false);
        cancelMapButton = new JButton(cancelMapButtonName);
        cancelMapButton.setToolTipText(cancelMapButtonName);
        cancelMapButton.setEnabled(true);
        c1.gridx = 3;
        c1.gridy = 3;
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(mappingButton, c1);
        c1.gridx = 4;
        c1.gridy = 3;
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(cancelMapButton, c1);

        m_fpsList = new JList(m_fileProfilesModel);
        m_fpsList.setAutoscrolls(true);
        m_fpsList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPaneResult = new JScrollPane(m_fpsList);
        jScrollPaneResult.setMinimumSize(new Dimension(width31, 120));// 100
        jScrollPaneResult
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        c1.gridx = 5;
        c1.gridy = 1;
        c1.gridwidth = 3;
        c1.gridheight = 4;
        contentPane.add(jScrollPaneResult, c1);

        // "Add File(s)" button
        addFileButton = new JButton(addButtonName);
        addFileButton.setToolTipText(addButtonName);
        c1.gridx = 0;
        c1.gridy = 5;
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(addFileButton, c1);
        // "Remove File(s)" button
        delFileButton = new JButton(delButtonName);
        delFileButton.setToolTipText(delButtonName);
        c1.gridx = 2;
        c1.gridy = 5;
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(delFileButton, c1);

        // jScrollPaneLocales
        localesBox = Box.createVerticalBox();
        JScrollPane jScrollPaneLocales = new JScrollPane(localesBox);
        jScrollPaneLocales.setBorder(BorderFactory
                .createTitledBorder("Target Locales"));
        jScrollPaneLocales
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPaneLocales.setMinimumSize(new Dimension(width31 - 70, 280));// 130
        c1.gridx = 5;// 5
        c1.gridy = 5;// 4
        c1.gridwidth = 3;
        c1.gridheight = 4;// 4
        contentPane.add(jScrollPaneLocales, c1);

        checkAllButton = new JButton(checkAllName);
        checkAllButton.setToolTipText(checkAllName);
        // checkAllButton.setFont(new Font("Arial",Font.PLAIN,10));
        c1.gridx = 5;// 4
        c1.gridy = 9;// 7
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(checkAllButton, c1);

        uncheckAllButton = new JButton(uncheckAllName);
        uncheckAllButton.setToolTipText(uncheckAllName);
        // uncheckAllButton.setFont(new Font("Arial",Font.PLAIN,10));
        c1.gridx = 7;// 5
        c1.gridy = 9;// 7
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(uncheckAllButton, c1);

        // Create job
        JPanel panel = new JPanel();
        panel.setSize(width31 * 2 - 100, height21);
        c1.gridx = 0;
        c1.gridy = 6;
        c1.gridwidth = 5;// 6
        c1.gridheight = 6;
        contentPane.add(panel, c1);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(5, 4, 5, 4);

        // job name
        int width_3 = width31 + 50;
        JLabel label1 = new JLabel(jobNameLableName, Label.RIGHT);
        c2.gridx = 0;
        c2.gridy = 0;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        panel.add(label1, c2);

        jTextFieldJobName = new JTextField("", 40);
        jTextFieldJobName.setMinimumSize(new Dimension(width_3, 30));
        jTextFieldJobName.setDocument(new MaxLengthDocument(150));
        c2.gridx = 1;
        c2.gridy = 0;
        c2.gridwidth = 3;
        c2.gridheight = 1;
        panel.add(jTextFieldJobName, c2);

        // job comment
        JLabel label4 = new JLabel(textNoteLableName, Label.RIGHT);
        c2.gridx = 0;
        c2.gridy = 1;
        c2.gridwidth = 1;
        c2.gridheight = 2;
        panel.add(label4, c2);

        jTextAreaNote = new JTextArea("", 10, 40);
        jTextAreaNote.setLineWrap(true);
        JScrollPane scrollPaneTextNoteArea = new JScrollPane(jTextAreaNote);
        scrollPaneTextNoteArea.setMinimumSize(new Dimension(width_3, 70));
        c2.gridx = 1;
        c2.gridy = 1;
        c2.gridwidth = 3;
        c2.gridheight = 2;
        panel.add(scrollPaneTextNoteArea, c2);

        // reference file
        JLabel label5 = new JLabel(noteAttachmentLableName, Label.RIGHT);
        c2.gridx = 0;
        c2.gridy = 3;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        panel.add(label5, c2);

        jTextFieldNoteAttachment = new JTextField("", 40);
        jTextFieldNoteAttachment.setEditable(false);
        jTextFieldNoteAttachment.setMinimumSize(new Dimension(width_3, 30));
        c2.gridx = 1;
        c2.gridy = 3;
        c2.gridwidth = 3;
        c2.gridheight = 1;
        panel.add(jTextFieldNoteAttachment, c2);

        addNoteAttachmentButton = new JButton(browseButtonName);
        c2.gridx = 4;
        c2.gridy = 3;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        panel.add(addNoteAttachmentButton, c2);

        // whether hide the job priority option
        boolean isShowJobPriority = false;
        try
        {
            isShowJobPriority = ConfigureHelperV2.readPrefJobPriority();
        }
        catch (Exception e)
        {
            log.error("error when hide the job priority option", e);
        }

        // Added by Vincent Yan, to fix #644
        // reference file
        JLabel label10 = new JLabel(jobPriority, Label.RIGHT);
        c2.gridx = 0;
        c2.gridy = 4;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        if (isShowJobPriority)
        {
            panel.add(label10, c2);
        }

        jComboBoxJobPriority = new JComboBox();
        jComboBoxJobPriority.setPreferredSize(new Dimension(160, 30));
        for (int i = 1; i < 6; i++)
        {
            jComboBoxJobPriority.addItem(String.valueOf(i));
        }
        jComboBoxJobPriority.setSelectedIndex(2);
        c2.gridx = 1;
        c2.gridy = 4;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        c2.anchor = GridBagConstraints.WEST;
        if (isShowJobPriority)
        {
            panel.add(jComboBoxJobPriority, c2);
        }
        // End of #644

        // option : max file number per job
        JLabel label11 = new JLabel(jobSplitting, Label.RIGHT);
        c2.gridx = 0;
        c2.gridy = 5;
        c2.gridwidth = 2;
        c2.gridheight = 1;
        panel.add(label11, c2);
        jRadioButtonMaxFileNumName = new JRadioButton(fileNumberPerJobName);
        jRadioButtonMaxFileNumName.setActionCommand(fileNumberPerJobName);
        jRadioButtonMaxFileNumName.setSelected(true);
        c2.insets = new Insets(5, 4, 5, 15);
        c2.gridx = 1;
        c2.gridy = 5;
        c2.gridwidth = 2;
        c2.gridheight = 1;
        panel.add(jRadioButtonMaxFileNumName, c2);
        // option: max job size per job
        jRadioButtonMaxJobSizeName = new JRadioButton(fileSizePerJobName);
        jRadioButtonMaxJobSizeName.setActionCommand(fileSizePerJobName);
        c2.insets = new Insets(5, 4, 5, 4);
        c2.gridx = 1;
        c2.gridy = 6;
        c2.gridwidth = 2;
        c2.gridheight = 1;
        panel.add(jRadioButtonMaxJobSizeName, c2);
        // add two options to buttongroup
        buttonGroupJobCreateOptionName = new ButtonGroup();
        buttonGroupJobCreateOptionName.add(jRadioButtonMaxFileNumName);
        buttonGroupJobCreateOptionName.add(jRadioButtonMaxJobSizeName);
        // textfield for max file num
        jTextFieldMaxFileNumName = new JTextField("0", 40);
        jTextFieldMaxFileNumName.setMinimumSize(new Dimension(60, 25));
        c2.insets = new Insets(5, 4, 5, 4);
        c2.gridx = 3;
        c2.gridy = 5;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        panel.add(jTextFieldMaxFileNumName, c2);
        // textfield for max job size
        jTextFieldMaxJobSizeName = new JTextField("0", 40);
        jTextFieldMaxJobSizeName.setMinimumSize(new Dimension(60, 25));
        c2.gridx = 3;
        c2.gridy = 6;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        panel.add(jTextFieldMaxJobSizeName, c2);

        // whether hide the job splitting option
        boolean isShowJobSplitting = false;
        try
        {
            isShowJobSplitting = ConfigureHelperV2.readPrefJobSplitting();
        }
        catch (Exception e)
        {
            log.error("error when hide the job splitting option", e);
        }

        if (!isShowJobSplitting)
        {
            label11.setVisible(false);
            jRadioButtonMaxJobSizeName.setVisible(false);// .setEnabled(false);
            jRadioButtonMaxFileNumName.setVisible(false);
            jTextFieldMaxFileNumName.setVisible(false);
            jTextFieldMaxJobSizeName.setVisible(false);
            c2.insets = new Insets(30, 4, 5, 4);
            panel.add(new JLabel(), c2);
        }

        // "set job attribute","create" and "cancel" buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        createButton = new JButton(createButtonName);
        buttonPanel.add(createButton);
        cancelButton = new JButton(cancelButtonName);
        buttonPanel.add(cancelButton);
        attibuteButton = new JButton(attributeButtonName);
        attibuteButton.setVisible(false);
        buttonPanel.add(attibuteButton);

        c2.gridx = 2;
        if (!isShowJobSplitting)
        {
            c2.insets = new Insets(10, 0, 4, 4);
        }
        c2.gridx = 1;
        c2.gridy = 7;
        c2.gridwidth = 3;
        c2.gridheight = 1;
        panel.add(buttonPanel, c2);

    }

    private void initActions()
    {
        addFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File[] files = filesChooserAction();
                addAllFiles(files);
            }
        });

        delFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[] indices = m_filesList.getSelectedIndices();
                delFile(indices);
                if (isFromCVS)
                {
                    if (m_filesModel.getSize() == 0)
                        addFileButton.setEnabled(true);
                    else
                        addFileButton.setEnabled(false);
                }
                Object obj = m_fpsList.getSelectedValue();
                if (obj != null && obj instanceof FileProfile)
                    showLocales((FileProfile) obj);
                else
                    showLocales(null);
                if (m_filesModel.size() == 0)
                    cleanlMap();

                // set max file num and max job size per job
                setMaxFileNumAndMaxJobSize();
            }
        });

        listFilesListener = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                removeJListActions();
                if (!e.getValueIsAdjusting())
                {
                    int[] indices = m_filesList.getSelectedIndices();
                    m_fpsList.setSelectedIndices(indices);
                    if (indices.length > 0)
                    {
                        fileProfileAction(1);
                    }
                }
                addJListActions();
            }
        };

        listResultListener = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                removeJListActions();
                if (!e.getValueIsAdjusting())
                {
                    int[] indices = m_fpsList.getSelectedIndices();
                    m_filesList.setSelectedIndices(indices);
                    if (indices.length > 0)
                    {
                        fileProfileAction(2);
                    }
                }
                addJListActions();
            }
        };

        addJListActions();

        jScrollPaneFiles.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener()
                {

                    public void adjustmentValueChanged(AdjustmentEvent e)
                    {
                        jScrollPaneResult.getVerticalScrollBar().setValue(
                                jScrollPaneFiles.getVerticalScrollBar()
                                        .getValue());
                    }

                });

        jScrollPaneResult.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener()
                {

                    public void adjustmentValueChanged(AdjustmentEvent e)
                    {
                        jScrollPaneFiles.getVerticalScrollBar().setValue(
                                jScrollPaneResult.getVerticalScrollBar()
                                        .getValue());
                    }

                });

        mappingButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                mappingAction();
            }
        });

        cancelMapButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cleanlMap();
            }
        });

        addNoteAttachmentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String fileName = fileChooserAction();
                try
                {
                    if (fileName == null)
                    {
                        jTextFieldNoteAttachment.setText(fileName);
                    }
                    else
                    {
                        long maximalFileSize = Long.parseLong(ConfigureHelper
                                .getAttachedFileSize()) * 1024 * 1024;
                        File file = new File(fileName);
                        if (file.length() > maximalFileSize)
                        {
                            AmbOptionPane.showMessageDialog(
                                    "Can not upload attachment larger than "
                                            + ConfigureHelper
                                                    .getAttachedFileSize()
                                            + "M!", "Warning");
                        }
                        else
                        {
                            jTextFieldNoteAttachment.setText(fileName);
                        }
                    }
                }
                catch (Exception e1)
                {
                    AmbOptionPane.showMessageDialog(
                            "Exception:\n" + e1.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        createButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createJobAction();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cleanPanel();
            }
        });

        // 02-05-2009 for GBS-354
        checkAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                checkAllTargetLocales(true);
            }
        });

        uncheckAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                checkAllTargetLocales(false);
            }
        });

        attibuteButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                showJobAttriubteDialog();
            }
        });
    }

    private void updateProjectId(FileProfile profile)
    {
        try
        {
            long projectId = GetAttributeAction
                    .getProjectIdByFileProfile(profile);
            jobAttributeDialog.setProjectId(projectId);
            attibuteButton.setVisible(jobAttributeDialog.hasAttribute());
        }
        catch (Exception e)
        {
            // AmbOptionPane.showMessageDialog(Constants.MSG_FAIL_GET_PROJECT,
            // "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showJobAttriubteDialog()
    {
        int size = m_fileProfilesModel.getSize();
        if (size == 0)
        {
            AmbOptionPane.showMessageDialog(Constants.MSG_NEED_MAP_FILE_FIRST,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object ob = m_fileProfilesModel.getElementAt(0);
        if (ob instanceof FileProfile)
        {
            FileProfile profile = (FileProfile) m_fileProfilesModel
                    .getElementAt(0);
            updateProjectId(profile);
            jobAttributeDialog.display();
        }
        else
        {
            AmbOptionPane.showMessageDialog(Constants.MSG_NEED_MAP_FILE_FIRST,
                    "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkAllTargetLocales(boolean bool)
    {
        localesBox.removeAll();
        Object obj = jComboBoxFileProfiles.getSelectedItem();

        if (obj instanceof FileProfile)
        {
            FileProfile fp = (FileProfile) obj;
            if (bool == true)
            {
                selectedLocales.addAll(Arrays.asList(fp.getTargetLocales()));
            }
            else
            {
                selectedLocales.clear();
            }
            showLocales(fp);
        }
    }

    private void showLocales(FileProfile fp)
    {
        localesBox.removeAll();
        if (fp != null)
        {
            l10n = fp.getL10nprofile();
            fpSourceLocale = fp.getSourceLocale();
            String[] tls = fp.getTargetLocales();
            // York added on 02/05/2009 for GBS-354
            Arrays.sort(tls);

            for (int i = 0; i < tls.length; i++)
            {
                String locale = tls[i];
                JCheckBox jcb = new JCheckBox(locale);
                if (selectedLocales.contains(locale))
                    jcb.setSelected(true);
                jcb.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        Object obj0 = m_fpsList.getSelectedValue();
                        if (e.getSource() instanceof JCheckBox
                                && obj0 instanceof FileProfile)
                        {
                            Component[] cs = localesBox.getComponents();
                            StringBuffer locales = new StringBuffer();
                            for (int i = 0; i < cs.length; i++)
                            {
                                Component c = cs[i];
                                if (c instanceof JCheckBox)
                                {
                                    JCheckBox jcb = (JCheckBox) c;
                                    if (jcb.isSelected())
                                    {
                                        if (isFromCVS)
                                        {
                                            int[] indexs = m_filesList
                                                    .getSelectedIndices();
                                            if (indexs != null
                                                    && indexs.length > 0)
                                            {
                                                File f = null;
                                                String sourceModule = "";
                                                String targetLocale = jcb
                                                        .getText();
                                                ModuleMappingHelper helper = new ModuleMappingHelper();
                                                ModuleMapping mm = null;
                                                boolean isOK = false;
                                                for (int k = 0; k < indexs.length; k++)
                                                {
                                                    f = (File) m_filesModel
                                                            .elementAt(indexs[k]);
                                                    sourceModule = (String) cvsModules
                                                            .get(f);
                                                    mm = helper
                                                            .getModuleMapping(
                                                                    fpSourceLocale,
                                                                    sourceModule,
                                                                    targetLocale,
                                                                    null);
                                                    if (mm != null)
                                                    {
                                                        // Find module-mapping
                                                        jcb.setForeground(Color.GREEN);
                                                        // FileProfile tmp_fp =
                                                        // (FileProfile)m_fileProfilesModel.elementAt(indexs[k]);
                                                        // String mmIds =
                                                        // tmp_fp.getModuleMappingIds();
                                                        // if
                                                        // (mmIds.indexOf(jcb.getText())
                                                        // < 0)
                                                        // mmIds += mm.getId() +
                                                        // ",";
                                                        // tmp_fp.setModuleMappingIds(mmIds);
                                                        // m_fileProfilesModel.set(indexs[k],
                                                        // tmp_fp);
                                                        isOK = true;
                                                    }
                                                    else
                                                    {
                                                        if (isOK)
                                                            jcb.setForeground(Color.YELLOW);
                                                        else
                                                            jcb.setForeground(Color.RED);
                                                    }
                                                }
                                            }
                                        }
                                        locales.append(jcb.getText()).append(
                                                ",");
                                    }
                                }
                            }
                            String[] tls = new String[0];
                            if (locales.length() > 4)
                            {
                                locales.deleteCharAt(locales.length() - 1);
                                tls = locales.toString().split(",");
                            }
                            selectedLocales.clear();
                            selectedLocales.addAll(Arrays.asList(tls));
                            for (int j = 0; j < m_fileProfilesModel.size(); j++)
                            {
                                Object obj = m_fileProfilesModel.get(j);
                                if (obj instanceof FileProfile)
                                {
                                    FileProfile fp = (FileProfile) obj;
                                    fp.setUsedTargetLocales(tls);
                                }
                            }
                        }
                    }
                });
                localesBox.add(jcb);
            }
        }
        localesBox.repaint();
        localesBox.validate();
        localesBox.getParent().validate();
    }

    private void addJListActions()
    {
        m_filesList.addListSelectionListener(listFilesListener);
        m_fpsList.addListSelectionListener(listResultListener);
    }

    private void removeJListActions()
    {
        m_filesList.removeListSelectionListener(listFilesListener);
        m_fpsList.removeListSelectionListener(listResultListener);
    }

    private void createJobAction()
    {
        if (canCreate())
        {
            String jobName = getJobName();
            String commentText = jTextAreaNote.getText();
            String attachFile = jTextFieldNoteAttachment.getText();
            String priority = jComboBoxJobPriority.getSelectedItem().toString();

            String jobCreateOption = null;
            String jobFileNumOrSize = null;
            if (jRadioButtonMaxJobSizeName.isSelected())
            {
                jobCreateOption = Constants.JOB_CREATE_OPTION_MAX_FILE_SIZE;
                jobFileNumOrSize = jTextFieldMaxJobSizeName.getText().trim();
            }
            else
            // jRadioButtonMaxFileNumName.isSelected()
            {
                jobCreateOption = Constants.JOB_CREATE_OPTION_MAX_FILE_NUM;
                jobFileNumOrSize = jTextFieldMaxFileNumName.getText().trim();
            }

            MainFrame rootPane = SwingHelper.getMainFrame();
            if (rootPane.logon())
            {
                UploadFilesDialog ufDialog = new UploadFilesDialog(rootPane);
                AttributeTableModel model = jobAttributeDialog.getModel();
                boolean isShowJobPriority = false;
                try
                {
                    isShowJobPriority = ConfigureHelperV2.readPrefJobPriority();
                }
                catch (Exception e)
                {
                    log.error("Error reading show job priority parameter", e);
                }
                if (!isShowJobPriority)
                {
                    priority = null;
                }
                if (!isFromCVS)
                {
                    ufDialog.commitJob(jobName, m_filesModel,
                            m_fileProfilesModel, commentText, attachFile,
                            jobCreateOption, jobFileNumOrSize, priority, model);
                }
                else
                {
                    ufDialog.commitCVSJob(jobName, m_filesModel,
                            m_fileProfilesModel, commentText, attachFile,
                            jobCreateOption, jobFileNumOrSize, priority, model);
                }

                ufDialog.setLocationRelativeTo(mappingButton);
                ufDialog.setFocusable(true);
                ufDialog.show();
                if (ConfigureHelper.isCleanAfterCreateJob())
                {
                    cleanPanel();
                }
            }
        }
    }

    private void mappingAction()
    {
        int[] indices = m_filesList.getSelectedIndices();
        int index = jComboBoxFileProfiles.getSelectedIndex();
        Object obj = jComboBoxFileProfiles.getSelectedItem();

        if (index != -1 && indices.length > 0 && obj instanceof FileProfile)
        {
            // get select locales and clone a new FileProfile
            FileProfile fp = (FileProfile) obj;
            if (selectedLocales.isEmpty())
            {
                selectedLocales.addAll(Arrays.asList(fp.getTargetLocales()));
            }
            String[] usedLocales = new String[selectedLocales.size()];
            fp.setUsedTargetLocales((String[]) selectedLocales
                    .toArray(usedLocales));
            showLocales(fp);
            // mapping all the files with same suffix
            String firstSuffix = getSuffix(m_filesModel.get(indices[0])
                    .toString());
            for (int i = 0; i < indices.length; i++)
            {
                String suffix = getSuffix(m_filesModel.get(indices[i])
                        .toString());
                if (i == 0 || suffix.equalsIgnoreCase(firstSuffix))
                {
                    m_fileProfilesModel.set(indices[i], fp);
                }
            }

            updateProjectId(fp);

            // Set job priority
            // Note: fp.getL10nprofile() can't get a l10nprofile Id at all, so
            // comment it.
            // try {
            // QueryAction query = new QueryAction();
            //
            // String result = query.execute(new String[] {
            // QueryAction.q_getPriorityByID, fp.getL10nprofile() });
            // priority = result;
            // } catch (Exception e) {
            priority = "3";
            // }
        }
    }

    private String fileChooserAction()
    {
        JFileChooser fileChooser = new JFileChooser(
                ConfigureHelper.getBaseFolder());
        fileChooser.setDialogTitle("Select The File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int status = fileChooser.showOpenDialog(SwingHelper.getMainFrame());
        if (status == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            ConfigureHelper.setBaseFolder(selectedFile.getParent());
            return selectedFile.getAbsolutePath();
        }
        else
        {
            return null;
        }
    }

    private File[] filesChooserAction()
    {
        JFileChooser fileChooser = new JFileChooser(
                ConfigureHelper.getBaseFolder());
        fileChooser.setDialogTitle("Select The File(s)");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        int status = fileChooser.showOpenDialog(SwingHelper.getMainFrame());
        if (status == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles != null && selectedFiles.length > 0)
            {
                ConfigureHelper.setBaseFolder(selectedFiles[0].getParent());
            }
            return selectedFiles;
        }
        else
        {
            return null;
        }
    }

    private String getSuffix(String fullName)
    {
        int position = fullName.lastIndexOf(File.separator);
        if (position == -1)
        {
            return "";
        }
        String filename = fullName.substring(position);
        int extPos = filename.lastIndexOf(".");
        if (extPos == -1)
        {
            return "";
        }
        else
        {
            return filename.substring(extPos + 1);
        }
    }

    private void cleanPanel()
    {
        jTextFieldJobName.setText("");
        m_filesList.removeAll();
        m_fpsList.removeAll();
        m_filesModel.removeAllElements();
        m_fileProfilesModel.removeAllElements();
        localesBox.removeAll();
        localesBox.repaint();
        localesBox.validate();
        resumeOriComboBox();
        jTextAreaNote.setText("");
        jTextFieldNoteAttachment.setText("");
        showIfAddSamefile = true;
        addSameFile = true;
        allFileProfiles = null;
        l10n = "";
        selectedLocales.clear();

        jTextFieldMaxFileNumName.setText("0");
        jTextFieldMaxJobSizeName.setText("0");

        attibuteButton.setVisible(false);
        jobAttributeDialog = new JobAttributeDialog(SwingHelper.getMainFrame());
    }

    private void cleanlMap()
    {
        showLocales(null);
        l10n = "";
        for (int i = 0; i < m_fileProfilesModel.size(); i++)
        {
            m_fileProfilesModel.set(i, Constants.PLEASE_MAPPING);
        }
        m_filesList.clearSelection();
        m_fpsList.clearSelection();
        selectedLocales.clear();
        resumeOriComboBox();
        attibuteButton.setVisible(false);
        jobAttributeDialog.clean();
    }

    private void fileProfileAction(int from)
    {
        // stop fileprofile thread first
        if (fpThread != null)
        {
            fpThread.stopMe();
        }
        // Always show first selected file's fileprofile
        int[] indices = new int[0];
        if (from == 1 || from == 0)
        {
            indices = m_filesList.getSelectedIndices();
        }
        else if (from == 2)
        {
            indices = m_fpsList.getSelectedIndices();
        }
        if (indices.length == 0)
            return;

        /**
         * if (isFromCVS) { if (!isSameModulePath(indices)) {
         * AmbOptionPane.showMessageDialog(Constants.ERROR_NOT_SAME_MODULE_PATH,
         * "Warning", JOptionPane.ERROR_MESSAGE); return; } }
         */

        // check if user logoned
        User user = CacheUtil.getInstance().getCurrentUser();
        if (user == null)
        {
            fpThread = new FileProfileThread();
            fpThread.start();
            if (from != 0)
                AmbOptionPane.showMessageDialog(Constants.ERROR_NOT_LOGON,
                        "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int index = indices[0];
        FileProfile fp = null;
        if (from != 0)
        {
            Object obj = m_fpsList.getModel().getElementAt(index);
            if (obj instanceof FileProfile)
            {
                fp = (FileProfile) obj;
            }
            showLocales(fp);
        }
        String suffix = getSuffix(m_filesModel.get(index).toString());
        if (!suffix.trim().equals(""))
        {
            jComboBoxFileProfiles.removeAllItems();
            QueryAction queryAction = new QueryAction();
            try
            {
                // get file profile from server
                if (allFileProfiles == null || allFileProfiles.size() == 0
                        || lastUser == null || !lastUser.equals(user))
                {
                    jComboBoxFileProfiles
                            .addItem("getting file profiles from server...");
                    String result = queryAction.execute(new String[]
                    { QueryAction.q_fileprofile });
                    allFileProfiles = StringHelper.split(result, "fileProfile");
                    jComboBoxFileProfiles.removeAllItems();
                    lastUser = user;
                }
                // get file profile by extension and l10nprofile
                fileProfiles = StringHelper.getFileProfiles(allFileProfiles,
                        l10n, suffix);
                fileProfilesTipsList = new ArrayList<String>();
                if (fileProfiles.size() == 0)
                {
                    log.info("No available FileProfile for this \"*." + suffix
                            + "\" file format.");
                    m_fileProfilesModel.set(index, Constants.NO_FP_MSG);
                    jComboBoxFileProfiles.addItem(Constants.NO_FP_MSG);
                    mappingButton.setEnabled(false);
                    return;
                }
                SortHelper.sortFileProfiles(fileProfiles);

                for (int i = 0; i < fileProfiles.size(); i++)
                {
                    FileProfile filePorfile = (FileProfile) fileProfiles.get(i);
                    jComboBoxFileProfiles.addItem(filePorfile);
                    String description = filePorfile.getDescription();
                    if (description == null || description.trim().length() == 0)
                    {
                        fileProfilesTipsList.add(filePorfile.getName());
                    }
                    else
                    {
                        fileProfilesTipsList.add(description);
                    }
                    if (fp != null && fp.getId().equals(filePorfile.getId()))
                    {
                        jComboBoxFileProfiles.setSelectedIndex(i);
                    }
                }
                mappingButton.setEnabled(true);
            }
            catch (Exception e1)
            {
                resumeOriComboBox();
                SwingHelper.getMainFrame().setStatus(Constants.MSG_ERROR_GETFP,
                        Constants.FAILURE);
                log.error(Constants.MSG_ERROR_GETFP, e1);
            }
        }
        else
        {
            jComboBoxFileProfiles.removeAllItems();
            jComboBoxFileProfiles.addItem(Constants.SHOULD_BE_FILE);
            m_fileProfilesModel.set(index, Constants.SHOULD_BE_FILE);
            AmbOptionPane.showMessageDialog(Constants.SHOULD_BE_FILE,
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addFile(File p_file)
    {
        String tmpPath = "";
        if (p_file != null && p_file.exists())
        {
            if (p_file.isDirectory())
            {
                File[] files = p_file.listFiles();
                addAllFiles(files);
            }
            else if (isZipFile(p_file))
            {
                try
                {
                    String dir = ConfigureHelperV2.readDefaultSavepath();
                    if (dir == null)
                    {
                        dir = new File(".").getAbsolutePath();
                    }
                    String temp_zip_folder = dir + File.separator + "temp_zip"
                            + File.separator + p_file.getName() + "_"
                            + System.currentTimeMillis();
                    List files = ZipIt.unpackZipPackage(
                            p_file.getAbsolutePath(), temp_zip_folder);

                    if (isFromCVS)
                    {
                        tmpPath = cvsModules.get(p_file);
                        cvsModules.remove(p_file);
                    }
                    File[] fileArray = new File[files.size()];
                    for (int i = 0; i < fileArray.length; i++)
                    {
                        fileArray[i] = new File(temp_zip_folder,
                                (String) files.get(i));
                        if (isFromCVS)
                        {
                            cvsModules.put(fileArray[i], tmpPath);
                        }
                    }

                    addAllFiles(fileArray);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    AmbOptionPane.showMessageDialog("Unzip " + p_file
                            + " error!", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                boolean isEmptyFile = false;
                boolean isSame = false;
                boolean add = true;
                // ignore file size == 0
                if (p_file.length() <= 0)
                {
                    isEmptyFile = true;
                    // If the pathName is too long, display as "...xxxxxx".
                    String absolutePath = p_file.getAbsolutePath();
                    if (absolutePath.length() > 80)
                    {
                        absolutePath = "..."
                                + absolutePath
                                        .substring(absolutePath.length() - 80);
                    }
                    AmbOptionPane.showMessageDialog((Constants.EMPTY_FILE
                            + "\n" + absolutePath), "Warning");
                }// check if file is already in file list
                else if (ConfigureHelper.checkAddSameFile())
                {
                    for (int i = 0; i < m_filesModel.size(); i++)
                    {
                        File file = (File) m_filesModel.get(i);
                        if (file.getAbsolutePath().equals(
                                p_file.getAbsolutePath()))
                        {
                            isSame = true;
                            break;
                        }
                    }
                    if (isSame && showIfAddSamefile)
                    {
                        int value = AmbOptionPane.showConfirmDialog(
                                Constants.ADD_SAME_FILE + "\n"
                                        + p_file.getAbsolutePath(),
                                "Add same file?", JOptionPane.YES_NO_OPTION);
                        if (JOptionPane.NO_OPTION == value)
                        {
                            addSameFile = false;
                        }
                        else
                        {
                            addSameFile = true;
                        }
                        /**
                         * configure "resource/configure/configure.xml"
                         * <checkaddsamefiletimes>s</checkaddsamefiletimes> to
                         * show option pane just one time in a job or
                         * <checkaddsamefiletimes>m</checkaddsamefiletimes> to
                         * show option pane each time when add same file
                         */
                        if (ConfigureHelper.isCheckAddSameFileOnce())
                        {
                            showIfAddSamefile = false;
                        }
                    }
                }

                if (isEmptyFile)
                {
                    add = false;
                }
                else if (isSame && !addSameFile)
                {
                    add = false;
                }

                if (add)
                {
                    m_filesModel.addElement(p_file);
                    m_fileProfilesModel.addElement(Constants.PLEASE_MAPPING);
                }
            }
        }
    }

    private boolean isZipFile(File p_file)
    {
        if (p_file.getPath().toLowerCase().endsWith(".zip"))
            return true;
        else
            return false;
    }

    private boolean canCreate()
    {
        // check if there is someone logon
        if (CacheUtil.getInstance().getCurrentUser() == null)
        {
            AmbOptionPane.showMessageDialog(Constants.ERROR_NOT_LOGON,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // check whether job name is empty
        if (!ValidationHelper.validateEmptyString(getJobName()))
        {
            AmbOptionPane.showMessageDialog(Constants.ERROR_EMPTY_JOB_STRING,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // validate job name characters
        if (!ValidationHelper.validateJobName(getJobName()))
        {
            AmbOptionPane.showMessageDialog(Constants.ERROR_JOB_NAME,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check file list is not empty
        if (m_filesModel.size() == 0)
        {
            AmbOptionPane.showMessageDialog(Constants.ERROR_NO_FILE, "Warning",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check all files related to fileprofile.
        Vector notMaped = new Vector();
        Vector noTarget = new Vector();
        for (int i = 0; i < m_fileProfilesModel.getSize(); i++)
        {
            Object obj = m_fileProfilesModel.get(i);
            Object file = m_filesModel.get(i);
            if (obj == null || !(obj instanceof FileProfile))
            {
                notMaped.add(file);
            }
            else
            {
                FileProfile fp = (FileProfile) obj;
                if (fp.getUsedTargetLocales().length == 0
                        || selectedLocales.size() == 0)
                {
                    noTarget.add(file);
                }
            }
        }
        if (notMaped.size() != 0 || noTarget.size() != 0)
        {
            StringBuffer files = new StringBuffer();
            files.append("<html>");
            if (notMaped.size() > 0)
                files.append("Files below are not mapped<ul>");
            for (int i = 0; i < notMaped.size(); i++)
            {
                files.append("<li>").append(notMaped.get(i)).append("</li>");
            }
            if (notMaped.size() > 0)
                files.append("</ul>");
            if (noTarget.size() > 0)
                files.append("Please select target locales");
            files.append("</html>");
            AmbOptionPane.showMessageDialog(Constants.ERROR_MAPPING_FP + "\n"
                    + files, "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // check if the file num or job size is valid

        if (jRadioButtonMaxFileNumName.isSelected())
        {
            String strMaxFileNum = jTextFieldMaxFileNumName.getText();
            try
            {
                int intMaxFileNum = (new Integer(strMaxFileNum)).intValue();
                if (intMaxFileNum <= 0)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                AmbOptionPane
                        .showMessageDialog(Constants.ERROR_INVALID_FILE_NUM
                                + ": " + strMaxFileNum, "Warning",
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if (jRadioButtonMaxJobSizeName.isSelected())
        {
            String strMaxJobSize = jTextFieldMaxJobSizeName.getText();
            try
            {
                double doubleMaxJobSize = (new Double(strMaxJobSize))
                        .doubleValue();
                if (doubleMaxJobSize <= 0)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                AmbOptionPane
                        .showMessageDialog(Constants.ERROR_INVALID_JOB_SIZE
                                + ": " + strMaxJobSize, "Warning",
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if (!jobAttributeDialog.isRequiredAttributeSeted())
        {
            AmbOptionPane.showMessageDialog(
                    Constants.MSG_SET_REQUIRED_ATTRIBUTE, "Warning",
                    JOptionPane.ERROR_MESSAGE);
            showJobAttriubteDialog();
            return false;
        }

        return true;
    }

    private void resumeOriComboBox()
    {
        jComboBoxFileProfiles.removeAllItems();
        if (m_filesModel.size() == 0)
            jComboBoxFileProfiles.addItem(Constants.PLEASE_ADD_FILE);
        else
            jComboBoxFileProfiles.addItem(Constants.PLEASE_SELECT_FILE);

        mappingButton.setEnabled(false);
    }

    private void setMaxFileNumAndMaxJobSize()
    {
        // set max file num per job
        String fileNum = "0";
        try
        {
            fileNum = String.valueOf(m_filesModel.size());
        }
        catch (Exception e)
        {
        }
        jTextFieldMaxFileNumName.setText(fileNum);

        // set max job size
        String fileSizeInKB = "0.0";
        if (m_filesModel != null && m_filesModel.size() > 0)
        {
            double fileSizeInBytes = 0;
            for (int i = 0; i < m_filesModel.size(); i++)
            {
                try
                {
                    File file = (File) m_filesModel.get(i);
                    fileSizeInBytes += file.length();
                }
                catch (Exception e)
                {
                }
            }

            DecimalFormat df = new DecimalFormat("0.0");
            fileSizeInKB = df.format(fileSizeInBytes / 1024);
        }

        jTextFieldMaxJobSizeName.setText(fileSizeInKB);
    }

    // private static final long serialVersionUID = 2800367301001078235L;

    static Logger log = Logger.getLogger(CreateJobPanel.class.getName());

    private static String jobNameLableName = "Job Name:";

    private static String fileLableName = "File(s)";

    private static String fileProfileIDLableName = "File Profile";

    private static String mappingResult = "Mapping Result";

    private static String textNoteLableName = "Job Comments:";

    private static String noteAttachmentLableName = "Attached File:";

    private static String attributeButtonName = "Set Job Attributes";

    private static String createButtonName = "Create";

    private static String cancelButtonName = "Cancel";

    private static String mappingButtonName = "Map files";

    private static String addButtonName = "Add File(s)";

    private static String delButtonName = "Remove File(s)";

    private static String checkAllName = "Check All";

    private static String uncheckAllName = "Uncheck All";

    private static String browseButtonName = "Browse";

    private static String cancelMapButtonName = "Clean Map";

    private static String jobPriority = "Job Priority:";

    private static String jobSplitting = "Job Splitting:";

    private static String fileNumberPerJobName = "Maximum number of files per job";

    private static String fileSizePerJobName = "Maximum total file size per job (KB)";

    private JTextField jTextFieldJobName, jTextFieldMaxFileNumName,
            jTextFieldMaxJobSizeName;

    private JRadioButton jRadioButtonMaxFileNumName,
            jRadioButtonMaxJobSizeName;

    private ButtonGroup buttonGroupJobCreateOptionName;

    private JComboBox jComboBoxFileProfiles, jComboBoxJobPriority;

    private JList m_filesList, m_fpsList;

    private DefaultListModel m_filesModel, m_fileProfilesModel;

    private Box localesBox;

    private ListSelectionListener listResultListener, listFilesListener;

    private JScrollPane jScrollPaneFiles, jScrollPaneResult;

    private JTextArea jTextAreaNote;

    private JTextField jTextFieldNoteAttachment;

    private JButton attibuteButton, createButton, cancelButton, mappingButton,
            cancelMapButton, addFileButton, delFileButton,
            addNoteAttachmentButton, checkAllButton, uncheckAllButton;

    private List fileProfiles = null, allFileProfiles = null;
    private List<String> fileProfilesTipsList = null;

    private User lastUser = null;

    private boolean showIfAddSamefile = true;

    private boolean addSameFile = true;

    private String l10n = "";

    private String priority = "";

    private FileProfileThread fpThread = null;

    private List selectedLocales = new ArrayList();

    class FileProfileThread extends Thread
    {
        boolean m_run = true;

        public FileProfileThread()
        {
            super("FileProfileThread");
        }

        public void run()
        {
            try
            {
                while (m_run)
                {
                    Thread.sleep(1000);
                    if (m_run)
                        fileProfileAction(0);
                }
            }
            catch (InterruptedException e)
            {
                log.error(e.getMessage(), e);
            }
        }

        public void stopMe()
        {
            m_run = false;
            fpThread = null;
        }
    }

    // Added by Vincent
    public boolean isFromCVS()
    {
        return isFromCVS;
    }

    public void setFromCVS(boolean isFromCVS)
    {
        this.isFromCVS = isFromCVS;
        addFileButton.setEnabled(false);
    }

    public HashMap<File, String> getCvsModules()
    {
        return cvsModules;
    }

    public void setCvsModules(HashMap<File, String> cvsModules)
    {
        this.cvsModules = cvsModules;
    }

    private boolean isSameModulePath(int[] p_indexs)
    {
        if (p_indexs == null || p_indexs.length == 0)
            return false;
        if (p_indexs.length == 1)
            return true;
        File f = (File) m_filesModel.get(p_indexs[0]);
        File t = null;
        for (int i = 1; i < p_indexs.length; i++)
        {
            t = (File) m_filesModel.elementAt(p_indexs[i]);
            if (!f.getAbsolutePath().equals(t.getAbsolutePath()))
                return false;
        }
        return true;
    }

    public JobAttributeDialog getJobAttributeDialog()
    {
        return jobAttributeDialog;
    }

    /**
     * Get the Job Name: trim()
     */
    public String getJobName()
    {
        return jTextFieldJobName.getText().trim();
    }
    
    private class ComboBoxRenderer extends BasicComboBoxRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
        {
            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if (-1 < index)
                {
                    list.setToolTipText(fileProfilesTipsList.get(index));
                }
            }
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
