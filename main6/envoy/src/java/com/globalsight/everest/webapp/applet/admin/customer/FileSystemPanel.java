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
package com.globalsight.everest.webapp.applet.admin.customer; 

import com.globalsight.everest.webapp.applet.common.EnvoyJPanel;
import com.globalsight.everest.webapp.applet.common.EnvoyJTable;
import com.globalsight.everest.webapp.applet.common.EnvoyButton;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyLabel;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;
import com.globalsight.everest.webapp.applet.common.SortedJListModel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
// Swing
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * The FileSystemPanel contains an exploring look tree that displays
 * the user's directory structures (only folders) and upon the selection
 * of a folder, the chilren (folders and files) will be displayed on the 
 * right side of it (in a table).
 * The user can make a selection and add the file to the list widget below
 * the table but either double clicking on it, clicking on the button, or 
 * on the selections menu item.  Once the files are added to the list, the
 * user can click on the upload button to upload the files from their own
 * directory to GlobalSight's docs directory.
 */

public class FileSystemPanel extends EnvoyJPanel 
{
    /**
     * A table displaying the files and folders of a selected
     * directory in the exploring like tree.
     */
    private EnvoyJTable m_fileSystemTable = null;
    private FileSystemView m_fileSystemView = null;
    private HashMap m_map = null;
    private JList m_selectedFilesList = null;
    private JTree m_tree = null;
    private JProgressBar m_progressBar = null;
    
    /**
     * Constructor for creating a panel that will display 
     * the exploring like widgets.
     */
    public FileSystemPanel() 
    {
        super();
        m_fileSystemView = FileSystemView.getFileSystemView();
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Implementation of EnvoyJTable's abstract methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the title of the panel.
     *
     * @return The panel's title.
     */
    public String getTitle()
    {
        return "";
    }


    /**
     * Populate the data of the file system panel.
     * @param p_objects A Vector containing a HashMap which in turn
     * has all the data needed for this panel and its components.
     */
    public void populate(Vector p_objects) 
    {
        // get the info sent from the server
        m_map = (HashMap)p_objects.elementAt(0);
        
        JScrollPane sp = new JScrollPane(createTree());
        sp.setDoubleBuffered(true); // reduce flickering
        sp.setPreferredSize(new java.awt.Dimension(300, 400));

        // put the folder/file table in a scrollpane
        createTable();
        JScrollPane fileSp = new JScrollPane(
            m_fileSystemTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileSp.getViewport().setBackground(java.awt.Color.WHITE);
        fileSp.getViewport().setDoubleBuffered(true);

        JPanel jp = createSelectedFilesList(
            (String)m_map.get("selectedFiles"), 
            new SortedJListModel(new ArrayList()));

        JSplitPane vSplitPane = verticalSplitPain(fileSp, jp);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              true,
                                              sp,
                                              vSplitPane);

        splitPane.setDoubleBuffered(true);                                   
        // provide a collapse/expand widget        
        splitPane.setDividerSize(10);
        splitPane.setDividerLocation(200); //pixel count
        splitPane.setOneTouchExpandable(true);
        
        // the buttons.
        String cancelLabel = (String)m_map.get("cancelBtn");
        final EnvoyButton cancelButton = new EnvoyButton(cancelLabel);
        
        String previousLabel = (String)m_map.get("previousBtn");
        final EnvoyButton previousButton = new EnvoyButton(previousLabel);
        
        String uploadLabel = (String)m_map.get("upload");
        final EnvoyButton uploadButton = new EnvoyButton(uploadLabel);
        int widthNew = GlobalEnvoy.getStringWidth(uploadLabel) + 10;

        setLayout(new EnvoyLineLayout(2, 2, 2, 2));
        setBackground(ENVOY_WHITE);
        setDoubleBuffered(true);
        setPreferredSize(new java.awt.Dimension(450, 400));
        
        // button listeners.
        cancelButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    boolean ok = getEnvoyJApplet().getWarningDlg(
                        (String)m_map.get("warning"));
                    if (ok)
                    {
                        ((FileSystemApplet)getEnvoyJApplet()).
                            appendDataToPostConnection(
                                m_progressBar, null, "cancelURL");
                    }
                }
            });

        previousButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    boolean ok = getEnvoyJApplet().getWarningDlg(
                        (String)m_map.get("warning"));
                    if (ok)
                    {
                        ((FileSystemApplet)getEnvoyJApplet()).
                            appendDataToPostConnection(
                                m_progressBar, null, "previousURL");
                    }
                }
            });

        uploadButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    performUpload();
                }
            });

        add(splitPane,
            new EnvoyConstraints(400, 300, 1, 
                                 EnvoyConstraints.LEFT,
                                 EnvoyConstraints.X_RESIZABLE, 
                                 EnvoyConstraints.Y_RESIZABLE,
                                 EnvoyConstraints.END_OF_LINE));
        
        add(cancelButton,
            new EnvoyConstraints(
                GlobalEnvoy.getStringWidth(cancelLabel) + 10,
                24, 1, EnvoyConstraints.RIGHT,
                EnvoyConstraints.X_NOT_RESIZABLE, 
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        add(previousButton,
            new EnvoyConstraints(
                GlobalEnvoy.getStringWidth(previousLabel) + 10, 
                24, 1, EnvoyConstraints.RIGHT,
                EnvoyConstraints.X_NOT_RESIZABLE, 
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));
        add(uploadButton,
            new EnvoyConstraints(
                GlobalEnvoy.getStringWidth(uploadLabel) + 10, 
                24, 1, EnvoyConstraints.RIGHT,
                EnvoyConstraints.X_NOT_RESIZABLE, 
                EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.END_OF_LINE));
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Implementation of EnvoyJTable's abstract methods
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: JTree representing the directory structure
    //////////////////////////////////////////////////////////////////////
    /**
     * Create the JTree which is displayed on the left side and shows
     * the exploring like directory structure of the customer system.
     */
    private JTree createTree()
    {
        FileSystemTreeModel model = new FileSystemTreeModel(
            m_fileSystemView, (Locale)m_map.get("userLocale"));
        m_tree = new JTree(model);
        m_tree.setShowsRootHandles(false);
        m_tree.setCellRenderer(model);
        m_tree.addMouseListener(getMouseListener());
        m_tree.expandRow(1);

        return m_tree;
    }

    /**
     * Create a tree selection listener which is fired when the tree node is
     * clicked.  It will populate the table that lists the children of the 
     * selected folder.
     */
    private MouseListener getMouseListener() {
        return new MouseListener(){
            public void mouseClicked(MouseEvent me) {
                if(me.getModifiers() == InputEvent.BUTTON1_MASK) {
                    JTree srcTree = (JTree)me.getSource();
                    Object node = (srcTree).getLastSelectedPathComponent();
                
                    if (node != null) {
                        populateTable(
                            ((FileSystemTreeModel)srcTree.
                             getModel()).getChildren(node));
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }};
    }

    //////////////////////////////////////////////////////////////////////
    //  End: JTree representing the directory structure
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: File System Table
    //////////////////////////////////////////////////////////////////////
    /**
     * Create the table that displays the children of a selected folder
     * in the left side tree UI.  The table has three columns showing the
     * name, size, and date the file/folder was last modified.
     */
    private void createTable()
    {                                      
        String[] columnNames = {(String)m_map.get("name"), 
            (String)m_map.get("size"), 
            (String)m_map.get("modified")};

        FileSystemTableModel fstm = new FileSystemTableModel(
            columnNames, (TimeZone)m_map.get("userTimeZone"), 
            (Locale)m_map.get("userLocale"));

        m_fileSystemTable = new EnvoyJTable(fstm);
        m_fileSystemTable.setRowHeight(20);     
        m_fileSystemTable.setShowGrid(false);
        resizeTableColumn();
        //for table sorting 
        JTableHeader header = m_fileSystemTable.getTableHeader();
        header.setUpdateTableInRealTime(true);
        header.addMouseListener(fstm.getColumnListener(m_fileSystemTable));
        header.setReorderingAllowed(false);

        m_fileSystemTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); 
        m_fileSystemTable.setDefaultRenderer(Object.class, 
                                             new CustomCellRenderer());
        m_fileSystemTable.setOpaque(false);
        
        // add table listener
        addTableListeners();
    }

    /**
     * Add all files from the table to the list (ready for upload)
     */
    private void addAllFilesToList() 
    {
        int numOfRows = m_fileSystemTable.getRowCount();
        if (numOfRows > 0)
        {
            for (int i = 0; i < numOfRows; i++)
            {
                File selectedFile = (File)m_fileSystemTable.
                    getValueAt(i, 0);
                if (selectedFile.isDirectory())
                {
                    // should we add all of its children files to the list?
                }
                else
                {
                    addFileToList(selectedFile);
                }
            }
        }
    }

    /**
     * Add the selected file(s) to the list.
     */
    private void addFilesToList() {
        if(m_fileSystemTable.getSelectedRowCount() > 0) {
            int[] selectedRows = m_fileSystemTable.getSelectedRows();

            for (int i = 0; i < selectedRows.length; i++) {
                File selectedFile = (File)m_fileSystemTable.
                                     getValueAt(selectedRows[i], 0);

                if(selectedFile.isDirectory()) {
                    populateTable(selectedFile.listFiles());
                    /* Get the clicked directory's tree-path to expand the 
                       response node on tree.
                       The tree-path is a array of Node from the root.
                       So the clicked directory's tree-path is the selected
                       tree node path array adds the clicked directory's Node.
                    */
                    Object node = m_tree.getLastSelectedPathComponent();
                    
                    File[] fileChildrens = 
                        ((FileSystemTreeModel)m_tree.getModel())
                           .getChildren(node);
                    
                    Object[] treeselectedPath = 
                        m_tree.getSelectionPath().getPath();
                    
                    ArrayList array = new ArrayList();
                  
                    for(int x = 0; x < treeselectedPath.length; x++) {
                        array.add(treeselectedPath[x]);
                    }

                    int indexOfParent = 0;

                    //If the tree node's path is equals the clicked directory's
                    //path, the tree-path adds the node, and the new tree-path  
                    //is the clicked directory's tree-path.
                    for(int x = 0; x < fileChildrens.length; x++) {
                        if(fileChildrens[x].getPath()
                            .equals(selectedFile.getPath())) {
                            //get the tree node of the clicked directory
                            Object obj = 
                                ((FileSystemTreeModel)m_tree.getModel())
                                    .getChild(node, indexOfParent);
                            
                            array.add(obj);
                        }
                        
                        //Because the left tree don't show the hidden directory,
                        //so in the right table, the index must be the no-hidden
                        //directory index.
                        if(fileChildrens[x].isDirectory() && 
                                (!fileChildrens[x].isHidden())) {
                            indexOfParent++;
                        }
                    }

                    TreePath newTP = new TreePath(array.toArray()); 
                    m_tree.expandPath(newTP);
                    m_tree.scrollPathToVisible(newTP);
                    m_tree.setSelectionPath(newTP);
                }
                else {
                    addFileToList(selectedFile);
                }
            }
        }
    }

    /**
     * Add the table listeners.  A mouse listener for double clicking
     * and popup menu item and a table selection listener.
     */
    private void addTableListeners()
    {
        MouseListener mouseListener = new MouseAdapter()
        {
            public void mouseReleased(MouseEvent e)
            {
                // If it's a right click, do the popup menu
                if (SwingUtilities.isRightMouseButton(e))
                {
                    popupMenu(m_fileSystemTable, e.getX(), e.getY());
                }
            }

            public void mousePressed(MouseEvent e)
            {
                // If it's a left double click - do the edit
                if (SwingUtilities.isLeftMouseButton(e) && 
                    e.getClickCount() == 2)
                {
                    addFilesToList();
                }
            }
        };
        m_fileSystemTable.addMouseListener(mouseListener);      
        //add selection changed listener to the table for the row selection change.
        m_fileSystemTable.getSelectionModel().
            addListSelectionListener(
                new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        // ignore deselect events of the table.
                        if (e.getValueIsAdjusting())
                            return;
                        
                        tableSelectionChanged();
                    }
                });
    }

    /**
     * Set the table column size to a fixed setting.
     */
    private void resizeTableColumn()
    {
        /*** TomyD -- need to revisit the column sizes... **/
        m_fileSystemTable.getColumnModel().getColumn(0).setMinWidth(300);
        m_fileSystemTable.getColumnModel().getColumn(0).setMaxWidth(450);
        m_fileSystemTable.getColumnModel().getColumn(1).setMinWidth(100);
        m_fileSystemTable.getColumnModel().getColumn(1).setMaxWidth(100);
        m_fileSystemTable.getColumnModel().getColumn(2).setMinWidth(160);
        m_fileSystemTable.getColumnModel().getColumn(2).setMaxWidth(250);
        m_fileSystemTable.sizeColumnsToFit (0);
    }
    
    /**
     * Populate the menu items of the popup menu.
     */
    private void populatePopupMenu(Component p_comp, int x, int y, 
                                   JPopupMenu popupMenu)
    {
        final boolean isTable = p_comp instanceof JTable;
        // Determine whether there's a selection
        boolean hasSelection = isTable ? 
            m_fileSystemTable.getSelectedRowCount() > 0 :
            m_selectedFilesList.getSelectedIndex() >= 0;
        
        JMenuItem menuItem = new JMenuItem(
            isTable ? (String)m_map.get("addForUpload") : 
            (String)m_map.get("remove"));

        menuItem.setEnabled(hasSelection);
        menuItem.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {                    
                   if (isTable)
                   {
                       addFilesToList();
                   }
                   else
                   {
                       removeFileFromList();
                   }                                           
               }
           });
        popupMenu.add(menuItem);

        // the "Add All" and "Remove All" menu items
        menuItem = new JMenuItem(
            isTable ? (String)m_map.get("addAllForUpload") : 
            (String)m_map.get("removeAll"));

        menuItem.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {                    
                   if (isTable)
                   {
                       addAllFilesToList();
                   }
                   else
                   {
                       clearList();
                   }                                           
               }
           });
        popupMenu.add(menuItem);

    }

    /**
     * Populate the table when a folder in the tree view is being selected.
     */
    public void populateTable(File[] p_files)
    {
        //Make the hidden files not to be shown
        ArrayList<File> listFiles = new ArrayList<File>();

        for(int x = 0; x < p_files.length; x++) {
            if(!p_files[x].isHidden()) {
                listFiles.add(p_files[x]);
            }
        }

        ((FileSystemTableModel)m_fileSystemTable.getModel()).
            updateTableModel(listFiles.toArray(new File[listFiles.size()]));
        
        m_fileSystemTable.sizeColumnsToFit(0);
        resizeTableColumn();
    }

    /**
     * Create the popup menu upon a right mouseClick in the table.
     */
    private void popupMenu(Component p_component, int x, int y)
    {
        // Create a new popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        // Get it loaded
        populatePopupMenu(p_component, x, y, popupMenu);
        // now display it
        popupMenu.show(p_component, x, y);
    }

    /**
     * The table selection has changed.
     */
    protected void tableSelectionChanged()
    {
        // do we care about this?
    }

    /**
     * Create a vertical split pane and place the two specified components
     * in it.
     */
    private JSplitPane verticalSplitPain(Component p_top, 
                                         Component p_bottom)
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              true, p_top, p_bottom);
        splitPane.setDoubleBuffered(true);                                   
        // provide a collapse/expand widget        
        splitPane.setDividerSize(10);
        splitPane.setDividerLocation(260); //pixel count
        splitPane.setOneTouchExpandable(true);

        return splitPane;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: File System Table
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: List of Selected Files
    //////////////////////////////////////////////////////////////////////
    /**
     * Add selected files from the table to the list (ready for upload)
     */
    private void addFileToList(File p_file) 
    {
        SortedJListModel model = 
            (SortedJListModel)m_selectedFilesList.getModel();

        model.add(p_file);
    }

    /**
     * Clear the list by removing all of its items.
     */
    private void clearList() 
    {
        SortedJListModel model = 
            (SortedJListModel)m_selectedFilesList.getModel();
        model.clear();
    }

    /**
     * Create the add/remove (up/down) buttons and add them to
     * the list panel.
     */
    private void createListButtons(JPanel p_panel)
    {
        ImageIcon removeFromList = 
            new ImageIcon(getImage((String)m_map.get("moveUp")));
        ImageIcon addToList = 
            new ImageIcon(getImage((String) m_map.get("moveDown")));

        JButton addButton = new JButton(addToList);
        //addButton.setBorder(null);
        JButton removeButton = new JButton(removeFromList);
        //removeButton.setBorder(null);

        
        addButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event)
                {
                    addFilesToList();
                }});

        removeButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event)
                {
                    removeFileFromList();
                }}); 

        // add widgets to the panel
        p_panel.add(new EnvoyLabel(),
            new EnvoyConstraints(230, 
                                 removeFromList.getIconHeight(), 1, 
                                 EnvoyConstraints.CENTER,
                                 EnvoyConstraints.X_NOT_RESIZABLE, 
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));        

        p_panel.add(addButton,
            new EnvoyConstraints(addToList.getIconWidth(), 
                                 addToList.getIconHeight(), 1, 
                                 EnvoyConstraints.CENTER,
                                 EnvoyConstraints.X_NOT_RESIZABLE, 
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

        p_panel.add(removeButton,
            new EnvoyConstraints(removeFromList.getIconWidth(), 
                                 removeFromList.getIconHeight(), 1, 
                                 EnvoyConstraints.CENTER,
                                 EnvoyConstraints.X_NOT_RESIZABLE, 
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));

        m_progressBar = new JProgressBar(0, 100);
        m_progressBar.setValue(0);
        m_progressBar.setStringPainted(true);
        m_progressBar.setVisible(false);

        p_panel.add(new EnvoyLabel(),
            new EnvoyConstraints(50, 
                                 removeFromList.getIconHeight(), 1, 
                                 EnvoyConstraints.CENTER,
                                 EnvoyConstraints.X_NOT_RESIZABLE, 
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.NOT_END_OF_LINE));        
        p_panel.add(m_progressBar,
            new EnvoyConstraints(180, 
                                 removeFromList.getIconHeight(), 1, 
                                 EnvoyConstraints.CENTER,
                                 EnvoyConstraints.X_NOT_RESIZABLE, 
                                 EnvoyConstraints.Y_NOT_RESIZABLE,
                                 EnvoyConstraints.END_OF_LINE));        
    }
    
    /**
     * Create the list box widget which will have a list of selected
     * files for upload.
     */
    private JPanel createSelectedFilesList(String p_listBorder, 
                                           SortedJListModel p_model)
    {
        m_selectedFilesList = new JList();
        m_selectedFilesList.setModel(p_model);
        m_selectedFilesList.setFont(m_fileSystemTable.getFont());
        
        // have a panel with add/remove buttons and the list
        JPanel panel = new JPanel();
        panel.setLayout(new EnvoyLineLayout(2, 2, 2, 2));
        panel.setBackground(ENVOY_WHITE);
        panel.setDoubleBuffered(true);
        // create and add the up/down buttons to this panel
        createListButtons(panel);
        
        m_selectedFilesList.addMouseListener(getListMouseListener());
        m_selectedFilesList.addListSelectionListener(
            getListSelectionListener());
        
        panel.add(getBorderedPanel(
            p_listBorder,(Component)m_selectedFilesList),
            new EnvoyConstraints(300, 300, 1, 
                                 EnvoyConstraints.CENTER,
                                 EnvoyConstraints.X_RESIZABLE, 
                                 EnvoyConstraints.Y_RESIZABLE,
                                 EnvoyConstraints.END_OF_LINE));
        return panel;
    }

    /**
     * Create a bordered panel for the specified component and use the 
     * given title for that border.
     */
    private JPanel getBorderedPanel(String p_border, 
                                    Component p_component) 
    {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.setBackground(ENVOY_WHITE);
        panel.setDoubleBuffered(true);

        if(p_border != null) 
        {
            panel.setBorder(new TitledBorder(p_border));
        }
        
        JScrollPane sp = new JScrollPane(
            p_component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, "Center");
        return panel;        
    }
    
    /**
     * Create a mouse listener for the file selection list. The
     * listener is used for double clicking and popup menu item.
     */
    private MouseListener getListMouseListener() {
        MouseListener mouseListener = new MouseAdapter() {

                public void mouseReleased(MouseEvent e)
                {
                    // If it's a right click, do the popup menu
                    if (SwingUtilities.isRightMouseButton(e))
                    {
                        popupMenu(m_selectedFilesList, 
                                  e.getX(), e.getY());
                    }
                }

		public void mouseClicked(MouseEvent e) 
                {
                    if (e.getClickCount() == 2) 
                    {
                        removeFileFromList();                        
		    }
		} 
	    };
        return mouseListener;
    }

    /**
     * Add a selection listener to the list component.
     */
    private ListSelectionListener getListSelectionListener() {
        return new ListSelectionListener() {		
		public void valueChanged(ListSelectionEvent e) {
		    // ignore deselect events of the table.
		    if (e.getValueIsAdjusting())
			return;		    
		}
	    };
    }

    /**
     * Perform the upload process by sending the files to the server.
     * If there are no errors, go to the next page to display the 
     * upload result.
     */
    private void performUpload()
    {
        SortedJListModel model = 
            (SortedJListModel)m_selectedFilesList.getModel();
        Object[] values = model.getListItems();
        int sz = values == null ? -1 : values.length;
        if (sz > 0)
        {
            Vector info = new Vector();
            info.add(values);
            ((FileSystemApplet)getEnvoyJApplet()).appendDataToPostConnection(
                m_progressBar, info, "uploadURL");
        }
    }

    /**
     * Remove the selected file from the list.
     */
    private void removeFileFromList() 
    {
        SortedJListModel model = 
            (SortedJListModel)m_selectedFilesList.getModel();
        model.removeAll(m_selectedFilesList.getSelectedValues());
        // clear selection
        m_selectedFilesList.clearSelection();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: List of Selected Files
    //////////////////////////////////////////////////////////////////////
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Inner Class
    //////////////////////////////////////////////////////////////////////
    /**
     * This subclass of DefaultTableCellRenderer is used to add an icon
     * for a folder/file in the table just like an exploring UI.
     */
    class CustomCellRenderer extends DefaultTableCellRenderer
    {
        public CustomCellRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
        }
     
        public Component getTableCellRendererComponent(JTable table, 
                                           Object value, boolean sSelected,
                                           boolean hasFocus, int row, 
                                           int col) 
        {
            super.getTableCellRendererComponent(
                table, value, sSelected, hasFocus, row, col);

            if (col == 0)
            {   
                File f = (File)value;
                String name = f.getName();

                Icon icon = m_fileSystemView.getSystemIcon(f);
                setIcon(icon);

                // check for the drive names being null/blank
                setText((f.getName() == null || 
                         f.getName().length() == 0) ?
                        f.getAbsolutePath() : name);

                setHorizontalAlignment(JLabel.LEFT);
            } 
            else
            {
                setIcon(null);
            }
    
            return this;
        }
    } 
    //////////////////////////////////////////////////////////////////////
    //  End: Inner Class
    //////////////////////////////////////////////////////////////////////
}
