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
package galign.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.io.IOException;
import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import galign.Setup;
import galign.data.ProjectNode;

/*
import galign.data.Locales;
import galign.data.Encodings;
*/

import galign.helpers.AlignmentMapping;
import galign.helpers.AlignmentPackage;
import galign.helpers.Callback;
import galign.helpers.BrowserLauncher;
import galign.helpers.filefilter.GenericFileFilter;
import galign.helpers.filefilter.PackageFileFilter;
import galign.helpers.filefilter.TmxFileFilter;
import galign.helpers.project.ProjectHelper;
import galign.helpers.project.ProjectTreeHelper;
import galign.helpers.tmx.TmxFile;
import galign.helpers.ui.ActionPropsSetter;
import galign.helpers.util.GAlignException;

import galign.data.Project;

import galign.ui.view.AboutDlg;
import galign.ui.view.GAlignFrm;
import galign.ui.view.NoProjectPnl;
import galign.ui.view.ProjectPnl;
import galign.ui.view.SelectedAlignmentPnl;

public class Start
{
    private static final String ABOUT_STR =
        "<html>&nbsp;&nbsp;" +
        "<font size='+1'><b>GlobalSight GAlign v1.0</b></font><hr>" +
        "&nbsp;&nbsp;<b>GlobalSight Aligner</b><br><br>" +
        "</html>";

    private GAlignFrm galignFrm;
    private NoProjectPnl noProjectPnl = new NoProjectPnl();
    private ProjectPnl projectPnl = new ProjectPnl();
    private SelectedAlignmentPnl selectedAlignmentPnl = new SelectedAlignmentPnl();
    private AlignmentPanel alignmentPnl = new AlignmentPanel(selectedAlignmentPnl);
    private JPanel currentPnl;
    private ProjectNode m_openedAlignment;
    private ProjectNode m_selectedAlignment;
    private ProjectHelper helper;

    private JFileChooser m_fileChooser = new JFileChooser();
    private FileFilter m_gapFileFilter = new PackageFileFilter();
    private FileFilter m_tmxFileFilter = new TmxFileFilter();
    private JPopupMenu m_popupMenu = new JPopupMenu();


    public void init()
    {
        galignFrm = new GAlignFrm();

        currentPnl = noProjectPnl;
        galignFrm.infoPane.setViewportView(currentPnl);
        galignFrm.contentPane.setViewportView(null);

        prepareMenus();
        prepareToolbar();
        preparePopupMenus();
        prepareControls();
        prepareButtons();

        // galignFrm.setExtendedState(JFrame.MAXIMIZED_BOTH);
        galignFrm.setIconImage(Setup.getIcon().getImage());
        galignFrm.setLocationRelativeTo(null);
        galignFrm.show();
    }

    public void setSelectedAlignment(ProjectNode node)
    {
        m_selectedAlignment = node;
    }

    public void setOpenedAlignment(ProjectNode node)
    {
        m_openedAlignment = node;
    }

    public void updateAlignMenus()
    {
        if ((m_openedAlignment != null && m_openedAlignment.equals(m_selectedAlignment))
             || m_selectedAlignment == null)
        {
            openAlignmentAction.setEnabled(false);
        }
        else
        {
            openAlignmentAction.setEnabled(true);
        }
        if (m_openedAlignment != null)
        {
            saveAlignmentAction.setEnabled(true);
            closeAlignmentAction.setEnabled(true);
            approveAllAction.setEnabled(true);
            //exportAsTmxAction.setEnabled(true);
        }
        else
        {
            saveAlignmentAction.setEnabled(false);
            closeAlignmentAction.setEnabled(false);
            approveAllAction.setEnabled(false);
            //exportAsTmxAction.setEnabled(false);
        }
        if (m_selectedAlignment != null)
            removeAlignmentAction.setEnabled(true);
        else
            removeAlignmentAction.setEnabled(false);
    }

    public void updateAlignPopups()
    {
        if (m_openedAlignment != null && m_openedAlignment.equals(m_selectedAlignment))
        {
            m_popupMenu.getComponent(0).setEnabled(false); // open
        }
        else
        {
            m_popupMenu.getComponent(0).setEnabled(true); // open
        }
        if (m_openedAlignment != null)
        {
            if (m_openedAlignment.equals(m_selectedAlignment))
            {
                m_popupMenu.getComponent(1).setEnabled(true); // save
                m_popupMenu.getComponent(2).setEnabled(true); // close
            }
            else
            {
                m_popupMenu.getComponent(1).setEnabled(false); // save
                m_popupMenu.getComponent(2).setEnabled(false); // close
            }
        }
        else
        {
            m_popupMenu.getComponent(1).setEnabled(false); // save
            m_popupMenu.getComponent(2).setEnabled(false); // close
        }
    }

    protected void prepareMenus()
    {
        // Open
        ActionPropsSetter.setActionProps(openAction, galignFrm.openProjectMni);
        galignFrm.openProjectMni.setAction(openAction);
        galignFrm.openProjectMni.setIcon(null);

        // Close
        ActionPropsSetter.setActionProps(closeAction, galignFrm.closeProjectMni);
        galignFrm.closeProjectMni.setAction(closeAction);

        // Upload
        ActionPropsSetter.setActionProps(uploadAction, galignFrm.uploadMni);
        galignFrm.uploadMni.setAction(uploadAction);

        // Add recently used project files to File menu
        addRecentlyUsed();

        // add Exit Menu item
        galignFrm.exitMni.setMnemonic(java.awt.event.KeyEvent.VK_X);
        galignFrm.exitMni.setText(Setup.getString("menu.exit"));
        galignFrm.fileMnu.add(galignFrm.exitMni);

        // Exit
        galignFrm.exitMni.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (checkUnsavedEdits("msg.alignmentChanges3") == -1) return;
                exit();
            }
        });

        // Exit
        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run()
            {
                exit();
                System.exit(0);
            }
        });
        */
        galignFrm.setDefaultCloseOperation(
                javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        galignFrm.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                if (checkUnsavedEdits("msg.alignmentChanges3") == -1) return;
                exit();
            }
        });

        // Align: Open Alignment
        ActionPropsSetter.setActionProps(openAlignmentAction, galignFrm.openAlignmentMni);
        galignFrm.openAlignmentMni.setAction(openAlignmentAction);

        // Align: Save Alignment
        ActionPropsSetter.setActionProps(saveAlignmentAction, galignFrm.saveAlignmentMni);
        galignFrm.saveAlignmentMni.setAction(saveAlignmentAction);

        // Align: Close Alignment
        ActionPropsSetter.setActionProps(closeAlignmentAction, galignFrm.closeAlignmentMni);
        galignFrm.closeAlignmentMni.setAction(closeAlignmentAction);

        // Align: Remove Alignment
        ActionPropsSetter.setActionProps(removeAlignmentAction, galignFrm.removeAlignmentMni);
        galignFrm.removeAlignmentMni.setAction(removeAlignmentAction);

        //Align: Approve All Alignments
        ActionPropsSetter.setActionProps(approveAllAction, galignFrm.approveAllMni);
        galignFrm.approveAllMni.setAction(approveAllAction);

        // Align: Export As Tmx
        //ActionPropsSetter.setActionProps(exportAsTmxAction, galignFrm.exportAsTmxMni);
        //galignFrm.exportAsTmxMni.setAction(exportAsTmxAction);

        // Help
        galignFrm.helpMni.addActionListener(new ActionListener()
        {
            String url = "";

            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    if (url.equals(""))
                    {
                        url = Setup.getHelpFile().toURL().toExternalForm();
                    }

                    BrowserLauncher.openURL(url);
                }
                catch (IOException e)
                {
                    if (Setup.DEBUG)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        //About
        galignFrm.aboutMni.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                about();
            }
        });
    }

    protected void preparePopupMenus()
    {
        JMenuItem mi = new JMenuItem(Setup.getString("menu.openPage"));
        mi.setEnabled(true);
        mi.addActionListener(openAlignmentAction);
        m_popupMenu.add(mi);
        mi = new JMenuItem(Setup.getString("menu.savePage"));
        mi.setEnabled(false);
        mi.addActionListener(saveAlignmentAction);
        m_popupMenu.add(mi);
        mi = new JMenuItem(Setup.getString("menu.closePage"));
        mi.setEnabled(false);
        mi.addActionListener(closeAlignmentAction);
        m_popupMenu.add(mi);
        mi = new JMenuItem(Setup.getString("menu.removePage"));
        mi.setEnabled(true);
        mi.addActionListener(removeAlignmentAction);
        m_popupMenu.add(mi);
    }


    protected void prepareToolbar()
    {
        // Open
        galignFrm.openProjectBtn.setAction(openAction);
        galignFrm.openProjectBtn.setText("");

    }

    protected void prepareControls()
    {
        Setup.s_projectTree = ProjectTreeHelper.createEmptyProjectTree();
        galignFrm.projectTree.setModel(Setup.s_projectTree);

        // set up listeners
        galignFrm.projectTree.getSelectionModel().setSelectionMode(
                            TreeSelectionModel.SINGLE_TREE_SELECTION);
        galignFrm.projectTree.addMouseListener(new TreePopupListener(m_popupMenu, this));
        galignFrm.projectTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent ae)
            {
                ProjectNode node = (ProjectNode)
                           galignFrm.projectTree.getLastSelectedPathComponent();

                if (node != null && node.getNodeType() == ProjectNode.PAGE_NODE)
                {
                    setSelectedAlignment(node);
                    updateAlignMenus();
                }
            }
        });
    }

    protected void prepareButtons()
    {
        //Browse Buttons
        /*
        galignFrm.btnBrowseFile.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                selectFile();
            }
        });
        */
    }

    //
    // Actions
    //

    final public AbstractAction openAction = new AbstractAction("Open",
        new javax.swing.ImageIcon(getClass().getResource(
            "/resources/open.gif")))
    {
        public void actionPerformed(ActionEvent e)
        {
            openProject();
        }
    };


    final public AbstractAction closeAction = new AbstractAction("Close")
    {
        public void actionPerformed(ActionEvent e)
        {
            if (checkUnsavedEdits("msg.alignmentChanges3") == -1) return;
            closeProject();
        }
    };


    final public AbstractAction uploadAction = new AbstractAction("Upload")
    {
        public void actionPerformed(ActionEvent e)
        {
            uploadProject();
        }
    };

    final public AbstractAction recentlyUsedAction = new AbstractAction("Recent")
    {
        public void actionPerformed(ActionEvent e)
        {
            JMenuItem source = (JMenuItem)(e.getSource());
            String filename = (String)getValue(source.getLabel());
            if (checkUnsavedEdits("msg.alignmentChanges4") == -1) return;
            openProject(filename);
        }
    };


    final public AbstractAction openAlignmentAction = new AbstractAction("Open Alignment")
    {
        public void actionPerformed(ActionEvent e)
        {
            openAlignment();
        }
    };

    final public AbstractAction saveAlignmentAction = new AbstractAction("Save Alignment")
    {
        public void actionPerformed(ActionEvent e)
        {
            saveAlignment();
        }
    };

    final public AbstractAction closeAlignmentAction = new AbstractAction("Close Alignment")
    {
        public void actionPerformed(ActionEvent e)
        {
            if (checkUnsavedEdits("msg.alignmentChanges3") == -1) return;
            closeAlignment();
        }
    };

    final public AbstractAction removeAlignmentAction = new AbstractAction("Remove Alignment")
    {
        public void actionPerformed(ActionEvent e)
        {
            removeAlignment();
        }
    };

    final public AbstractAction approveAllAction = new AbstractAction("Approve All")
    {
        public void actionPerformed(ActionEvent e)
        {
            approveAll();
        }
    };

    final public AbstractAction exportAsTmxAction = new AbstractAction("Export as TMX")
    {
        public void actionPerformed(ActionEvent e)
        {
            exportAsTmx();
        }
    };

    //
    // Action Handlers
    //

    protected void about()
    {
        AboutDlg aboutDlg = new AboutDlg(galignFrm, true);
        //aboutDlg.lb_content.setIcon(Setup.getIcon());
        //aboutDlg.lb_content.setText(ABOUT_STR);

        aboutDlg.pack();
        aboutDlg.setLocationRelativeTo(galignFrm);
        aboutDlg.show();
    }

    protected void openProject()
    {
        if (checkUnsavedEdits("msg.alignmentChanges4") == -1) return;
        openProject(selectProjectFile());
    }

    protected void openProject(String p_filename)
    {
        final String filename = p_filename;
        // Check for currently open project, save if necessary

        if (filename != null)
        {
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    doOpenProject(filename);
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    protected void closeProject()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                noProject();
                Setup.s_project = null;
                updateAlignMenus();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }


    protected void uploadProject()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                doPrepareForUpload();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void openAlignment()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                doOpenAlignment();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void saveAlignment()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                doSaveAlignment();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Close the alignment (without saving) and
     * show project panel
     */
    protected void closeAlignment()
    {
        setOpenedAlignment(null);
        updateAlignMenus();
        currentPnl = projectPnl;
        galignFrm.infoPane.setViewportView(currentPnl);
        galignFrm.contentPane.setViewportView(null);
        galignFrm.lb_status.setText(" ");
    }

    protected void removeAlignment()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                doRemoveAlignment();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void approveAll()
    {

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                doApproveAll();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void exportAsTmx()
    {
        final String filename = selectExportFile();

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                doExportAsTmx(filename);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    //
    // Private Methods
    //

    // For now, open projects on the main thread.
    private void doOpenProject(String p_filename)
    {
        helper = new ProjectHelper();
        try
        {
            Setup.s_project = helper.openProject(p_filename);
        }
        catch (GAlignException e)
        {
            JOptionPane.showMessageDialog(galignFrm, e.getMessage(),
                     "", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Project project = Setup.s_project;

        if (project == null)
        {
            noProject();
        }
        else
        {
            closeAction.setEnabled(true);
            uploadAction.setEnabled(true);

            projectPnl.lb_projectName.setText(project.getName());
            projectPnl.lb_sourceLocale.setText(
                project.getSourceLocale().getDisplayName() + " [" + 
                project.getSourceLocale().toString() + "]");
            projectPnl.lb_targetLocale.setText(
                project.getTargetLocale().getDisplayName() + " [" +
                project.getTargetLocale().toString() + "]");

            currentPnl = projectPnl;
            galignFrm.infoPane.setViewportView(currentPnl);
            galignFrm.contentPane.setViewportView(null);

            Setup.s_projectTree =
                ProjectTreeHelper.createProjectTree(project);
            galignFrm.projectTree.setModel(Setup.s_projectTree);
            updateRecentlyUsed(p_filename);
            if (Setup.s_projectTree.getChildCount(Setup.s_projectTree.getRoot()) == 1)
            {
                // automatically open up the only page there is
                galignFrm.projectTree.addSelectionRow(1);
                doOpenAlignment();
            }
        }
        updateAlignMenus();
    }

    private void noProject()
    {
        closeAction.setEnabled(false);
        uploadAction.setEnabled(false);
        setOpenedAlignment(null);
        setSelectedAlignment(null);

        currentPnl = noProjectPnl;
        galignFrm.infoPane.setViewportView(currentPnl);
        galignFrm.contentPane.setViewportView(null);

        Setup.s_projectTree = ProjectTreeHelper.createEmptyProjectTree();
        galignFrm.projectTree.setModel(Setup.s_projectTree);
    }

    private void doPrepareForUpload()
    {
        if (checkUnsavedEdits("msg.alignmentChanges2") == -1) return;
        galignFrm.lb_status.setText(Setup.getString("msg.zipping"));
        try
        {
            helper.prepareForUpload(Setup.s_project);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(galignFrm, ex.getMessage(),
                     "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(galignFrm, Setup.getString("msg.uploadDone"));
        galignFrm.lb_status.setText(Setup.getString("msg.uploadDone"));

    }

    // Read in the GAM file and the TMX files.
    private void doOpenAlignment()
    {
        if (m_openedAlignment != null &&
            !m_selectedAlignment.equals(m_openedAlignment) &&
            alignmentPnl.m_changed)
        {
            // Opening a new alignment when one is currently opened and has been
            // changed.
            int res = JOptionPane.showConfirmDialog(null,
                                 Setup.getString("msg.alignmentChanges"));
            if (res == JOptionPane.YES_OPTION)
            {
                doSaveAlignment();
            }
            else if (res == JOptionPane.CANCEL_OPTION)
            {
                // reset the selected alignment
                TreePath path = galignFrm.projectTree.getNextMatch(
                    m_openedAlignment.toString(), 0, Position.Bias.Forward);
                galignFrm.projectTree.addSelectionPath(path);
                setSelectedAlignment(m_openedAlignment);
                return;
            }
        }
        try
        {
            galignFrm.lb_status.setText(Setup.getString("msg.openingAlignment"));
            // read gam file
            AlignmentMapping alignmentMapping = new AlignmentMapping(
                                Setup.s_project.getPath(),
                                m_selectedAlignment.getFileInfo().getMappingFileName());

            alignmentPnl.init(alignmentMapping);
            currentPnl = alignmentPnl;
            setOpenedAlignment(m_selectedAlignment);
            updateAlignMenus();
            selectedAlignmentPnl.init();
            galignFrm.infoPane.setViewportView(selectedAlignmentPnl);
            galignFrm.contentPane.setViewportView(currentPnl);
            galignFrm.lb_status.setText(Setup.getString("msg.openedAlignment") +
                         " " + m_selectedAlignment.toString());
        }
        catch (Exception ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }
    }

    private void doSaveAlignment()
    {
        try
        {
            // Write out the gam file
            alignmentPnl.save();
            // Update the "files" state to completed
            m_openedAlignment.getFileInfo().setCompletedState();
            // Write out the package so that completed state gets saved
            Setup.s_project.getAlignmentPackage().save();
        }
        catch (GAlignException e)
        {
            JOptionPane.showMessageDialog(galignFrm, e.getMessage(),
                     "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (IOException ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }
    }

    private void doRemoveAlignment()
    {
        try
        {
            // remove the alignment from the project file and save
            Setup.s_project.getAlignmentPackage().removeAlignment(
                        m_selectedAlignment.getFileInfo());
            Setup.s_project.getAlignmentPackage().save();

            // Check if the alignment is currently opened.  If so
            // close it.
            if (m_selectedAlignment.equals(m_openedAlignment))
                closeAlignment();

            // update the project tree
            Setup.s_projectTree.removeNodeFromParent(m_selectedAlignment);
            setSelectedAlignment(null);

            updateAlignMenus();
        }
        catch (GAlignException e)
        {
            JOptionPane.showMessageDialog(galignFrm, e.getMessage(),
                     "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (Exception ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }
    }

    private void doApproveAll()
    {
        try {
            alignmentPnl.approveAll();
        }
        catch (Exception ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }
    }

    private void doExportAsTmx(String filename)
    {
        try {
            alignmentPnl.exportAsTmx(filename);
        }
        catch (Exception ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }
    }

    private void addRecentlyUsed()
    {
        LinkedList list = Setup.getRecentlyUsed();
        int size = list.size();
        if (galignFrm.fileMnu.getItemCount() == 6)
        {
            galignFrm.fileMnu.add(new JSeparator());
        }
        for (int i = 0; i < list.size(); i++)
        {
            String fullname = (String)list.get(i);
            String displayName = getDisplayName(fullname, i);
            javax.swing.JMenuItem mi = new javax.swing.JMenuItem(displayName);
            mi.setEnabled(true);
            AbstractAction aa = recentlyUsedAction;
            mi.addActionListener(aa);
            mi.setMnemonic(java.awt.event.KeyEvent.VK_0+i);
            aa.putValue(displayName, fullname);
            galignFrm.fileMnu.add(mi, i+5);
        }
    }

    private void updateRecentlyUsed(String p_filename)
    {
        LinkedList list = Setup.getRecentlyUsed();

        // remove old menu items
        for (int i = 0; i < list.size(); i++)
        {
            galignFrm.fileMnu.remove(5);
        }

        // update user database
        Setup.updateRecentlyUsed(p_filename);


        // add new menu items
        addRecentlyUsed();
    }

    private String getDisplayName(String name, int i)
    {
        int len = name.length();
        if (len < 30)
        {
            return i + " " + name;
        }
        String displayName = name.substring(len - 25, len);
        int index = displayName.indexOf('/');
        if (index < 0)
        {
            index = displayName.indexOf('\\');
        }
        if (index >= 0)
        {
            displayName = displayName.substring(index);
        }
        return i + " ..." + displayName;
    }

    private String selectExportFile()
    {
        String result = null;
        File cwd = Setup.getCurrentDirectory();
        m_fileChooser.setCurrentDirectory(cwd);
        m_fileChooser.setSelectedFile(null);
        m_fileChooser.resetChoosableFileFilters();
        m_fileChooser.setFileFilter(m_tmxFileFilter);
        m_fileChooser.setAcceptAllFileFilterUsed(false);

        int ret = m_fileChooser.showSaveDialog(galignFrm);
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            cwd = m_fileChooser.getCurrentDirectory();
            Setup.setCurrentDirectory(cwd);

            result = m_fileChooser.getSelectedFile().getAbsolutePath();
            if (!result.endsWith(".alp"))
            {
                result += ".alp";
            }
        }

        return result;
    }

    private String selectProjectFile()
    {
        String result = null;
        File cwd = Setup.getCurrentDirectory();

        m_fileChooser.setCurrentDirectory(cwd);
        m_fileChooser.setSelectedFile(null);
        // get rid of previously selected file
        m_fileChooser.updateUI();
        m_fileChooser.resetChoosableFileFilters();
        m_fileChooser.setFileFilter(m_gapFileFilter);
        m_fileChooser.setAcceptAllFileFilterUsed(false);

        int ret = m_fileChooser.showOpenDialog(galignFrm);
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            cwd = m_fileChooser.getCurrentDirectory();
            Setup.setCurrentDirectory(cwd);

            result = m_fileChooser.getSelectedFile().getAbsolutePath();
        }

        return result;
    }

    /*
     * Return -1 if canceling
     */
    private int checkUnsavedEdits(String msg)
    {
        if (alignmentPnl.m_changed)
        {
            int res = JOptionPane.showConfirmDialog(null,
                             Setup.getString(msg));
            if (res == JOptionPane.YES_OPTION)
            {
                doSaveAlignment();
            }
            else if (res == JOptionPane.CANCEL_OPTION)
            {
                return -1;
            }
            alignmentPnl.m_changed = false;
        }
        return 0;
    }


    private void exit()
    {
        Setup.saveUserSettings();

        // int option = JOptionPane.showConfirmDialog(galignFrm,
        //   "Exit GAlign?", "", JOptionPane.YES_NO_OPTION);
        // if (option != JOptionPane.YES_OPTION)
        // {
        //   return;
        // }

        System.exit(0);
    }

    //
    // Main
    //

    public static void main(String args[])
        throws Exception
    {
        Setup.init1();
        Setup.initLF();

        //Splash screen
        // AboutWin aboutWin = new AboutWin();
        // aboutWin.lb_content.setIcon(Setup.getIcon());
        // aboutWin.lb_content.setText(ABOUT_STR);
        // aboutWin.pack();
        // aboutWin.setLocationRelativeTo(null);
        // aboutWin.show();

        Setup.init2();

        new Start().init();

        // aboutWin.requestFocusInWindow();
        // aboutWin.setVisible(false);
        // aboutWin.dispose();
    }
}
