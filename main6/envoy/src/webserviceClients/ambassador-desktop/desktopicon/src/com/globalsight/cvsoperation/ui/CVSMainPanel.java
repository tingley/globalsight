package com.globalsight.cvsoperation.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import com.globalsight.cvsoperation.entity.CVSFile;
import com.globalsight.cvsoperation.entity.Module;
import com.globalsight.cvsoperation.entity.Repository;
import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.cvsoperation.util.ListDirAndFiles;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.ui.ClosableTabbedPane;
import com.globalsight.ui.CreateJobPanel;
import com.globalsight.ui.MainFrame;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class CVSMainPanel extends JPanel
{
	static Logger log = Logger.getLogger(CVSMainPanel.class.getName());
	
	private static final long serialVersionUID = 3263967987502216059L;
	
	private JButton jb1, jb2, jb3, jb4, jb5, jb6, jb7, jb8;
	
	private JScrollPane jScrollPaneFiles1, jScrollPaneFiles2;
	
	private JTree moduleFilesTree;
	
	private JList addedFilesList;
	
	private DefaultListModel defListModel = null;
	
	private JPopupMenu popMenu;
	
	private JMenuItem updateItem, expandAllItem, collapseAllItem;
	
//	private MouseListener mouseListener = null;
	
	private int width = SwingHelper.getMainFrame().getWidth();
	
	private int height = SwingHelper.getMainFrame().getHeight(); 
	
	private Container contentPane = this;
	
	private DefaultMutableTreeNode targetNode = null;
	
	//Added by Vincent
	private HashMap<File, String> cvsFileInfos = new HashMap<File, String>();
	private String cvsserver = "", sandBox = "", cvsmodule = "";
	
//	private TreePath selectedPath = null;
	
	public CVSMainPanel()
	{
		initPanel();
		initActions();
	}
	
	private void initPanel()
	{
//		Container contentPane = this;
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(2, 2, 2, 2);
		
		//Available CVS Module(s)
		JLabel label11 = new JLabel("Available CVS Module(s)");
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 2;
		c1.gridheight = 1;
		contentPane.add(label11, c1);
		
		//Job Contents
		JLabel label13 = new JLabel("Job Contents");
		c1.gridx = 3;
		c1.gridy = 0;
		c1.gridwidth = 2;
		c1.gridheight = 1;
		contentPane.add(label13, c1);
		
		//jScrollPaneFiles
		DefaultMutableTreeNode rootNode = fillModuleFilesTree();
		moduleFilesTree = new JTree(rootNode);
		moduleFilesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		for (int m=0; m<3; m++)
		{
			moduleFilesTree.expandRow(m);
		}
		jScrollPaneFiles1 = new JScrollPane(moduleFilesTree);
		jScrollPaneFiles1.setViewportView(moduleFilesTree);
		jScrollPaneFiles1.setMinimumSize(new Dimension(width/5*2-25, 400));
		jScrollPaneFiles1.setMaximumSize(new Dimension(width/5*2-25, 400));
		jScrollPaneFiles1.setPreferredSize(new Dimension(width/5*2-25,400));
		jScrollPaneFiles1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPaneFiles1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c1.gridx = 0;
		c1.gridy = 1;
		c1.gridwidth = 2;
		c1.gridheight = 6;
		contentPane.add(jScrollPaneFiles1, c1);
		
//		//Add to Job button
		jb1 = new JButton("Add to Job >>");
		jb1.setMinimumSize(new Dimension(160,30));
		jb1.setMaximumSize(new Dimension(160,30));
		jb1.setPreferredSize(new Dimension(160,30));
		jb1.setToolTipText("Add files to job");
		c1.gridx = 2;
		c1.gridy = 1;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		contentPane.add(jb1, c1);
		//Remove from Job button
		jb2 = new JButton("Remove from Job <<");
		jb2.setMinimumSize(new Dimension(160,30));
		jb2.setMaximumSize(new Dimension(160,30));
		jb2.setPreferredSize(new Dimension(160,30));
		jb2.setToolTipText("Remove files from job");
		jb2.setEnabled(false);
		c1.gridx = 2;
		c1.gridy = 3;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		contentPane.add(jb2, c1);
		
		//job contents
		defListModel = new DefaultListModel();
		addedFilesList = new JList(defListModel);
		addedFilesList.setAutoscrolls(true);
		addedFilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		addedFilesList.setTransferHandler(FileTransferHandler.install());
		jScrollPaneFiles2 = new JScrollPane(addedFilesList);
		jScrollPaneFiles2.setMinimumSize(new Dimension(width/5*2-25, 400));
		jScrollPaneFiles2.setMaximumSize(new Dimension(width/5*2-25, 400));
		jScrollPaneFiles2.setPreferredSize(new Dimension(width/5*2-25, 400));
		jScrollPaneFiles2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPaneFiles2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c1.gridx = 3;
		c1.gridy = 1;
		c1.gridwidth = 2;
		c1.gridheight = 6;
		contentPane.add(jScrollPaneFiles2, c1);

		JPanel panel = new JPanel();
		panel.setSize(width - 30, 25);
		c1.gridx = 0;
		c1.gridy = 8;
		c1.gridwidth = 5;
		c1.gridheight = 1;
		contentPane.add(panel, c1);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(4, 4, 4, 4);
		//Add Module button
		jb3 = new JButton("Add Module");
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		if ( CVSConfigureHelper.getRepositoryList() != null && 
				CVSConfigureHelper.getRepositoryList().size() > 0 &&
				CVSConfigureHelper.getProperty(Constants.CVS_USERNAME) != null &&
				!"".equals(CVSConfigureHelper.getProperty(Constants.CVS_USERNAME))) {
			jb3.setEnabled(true);			
		} else {
			jb3.setEnabled(false);
		}

		panel.add(jb3, c2);
		//Remove Module
		jb4 = new JButton("Remove Module");
		c2.gridx = 1;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		if ( CVSConfigureHelper.getAllModules() != null &&
				CVSConfigureHelper.getAllModules().size() > 0 )
		{
			jb4.setEnabled(true);			
		}
		else
		{
			jb4.setEnabled(false);
		}
		panel.add(jb4, c2);
		//Edit Module
		jb5 = new JButton("Edit Module");
		if ( CVSConfigureHelper.getAllModules() != null &&
				CVSConfigureHelper.getAllModules().size() > 0 )
		{
			jb5.setEnabled(true);			
		}
		else
		{
			jb5.setEnabled(false);
		}
		c2.gridx = 2;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		panel.add(jb5, c2);
		
		//Module mapping
		jb6 = new JButton("Module Mapping");
		c2.gridx = 3;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		jb6.setEnabled(true);
		panel.add(jb6, c2);
		//Submit job
		jb7 = new JButton("Submit to Create Job");
		c2.gridx = 4; 
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		jb7.setEnabled(false);
		panel.add(jb7, c2);
		//CVS Configuration
		jb8 = new JButton("CVS Configuration");
		c2.gridx = 5;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		panel.add(jb8, c2);
		
		popMenu = new JPopupMenu();
		updateItem = new JMenuItem("CVS Update");
		popMenu.add(updateItem);
		expandAllItem = new JMenuItem("Expand All");
		popMenu.add(expandAllItem);
		collapseAllItem = new JMenuItem("Collapse All");
		popMenu.add(collapseAllItem);
	}
	
	private void initActions()
	{
		//"Add to Job >>" button
		jb1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath[] selectedTreePaths = moduleFilesTree.getSelectionPaths();
				if ( selectedTreePaths != null && selectedTreePaths.length > 0 )
				{
					for (int i=0; i<selectedTreePaths.length; i++ )
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedTreePaths[i].getLastPathComponent();
						addSubFilesToJob(node);
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog("No file is selected to add", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
				}
				
				if ( defListModel.size() > 0 )
				{
					jb2.setEnabled(true);
					jb7.setEnabled(true);
				}
			}
		});
		
		//"Remove from Job <<" button
		jb2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object[] selectedValues = addedFilesList.getSelectedValues();
				if ( selectedValues != null && selectedValues.length > 0 )
				{
					for (int i=0; i<selectedValues.length; i++)
					{
						defListModel.removeElement(selectedValues[i]);		
						cvsFileInfos.remove(selectedValues[i]);
					}
					if ( defListModel.size() <= 0 )
					{
						jb2.setEnabled(false);
						jb7.setEnabled(false);
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog("No file is selected to remove", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		//"Add Module" button
		jb3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CVSModuleDialog moduleDialog = new CVSModuleDialog(SwingHelper.getMainFrame(), true);
				moduleDialog.setTitle(e.getActionCommand());
				moduleDialog.setAddOrEditFlag(Constants.CVS_ADD_MODULE);
				moduleDialog.setVisible(true);
				
				if ( moduleDialog.isOKClicked() )
				{
    				contentPane.removeAll();
    				initPanel();
    				initActions();							
				}
			}
		});
		
		//"Remove Module" button
		jb4.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultMutableTreeNode selectedTreeNode = 
					(DefaultMutableTreeNode) moduleFilesTree.getLastSelectedPathComponent();
				if ( selectedTreeNode != null )
				{
					CVSFile cvsFile = (CVSFile) selectedTreeNode.getUserObject();
					if ( cvsFile.getNodeType().equals(Constants.NODE_TYPE_MODULE) )
					{
	    				if (AmbOptionPane.showConfirmDialog("Are you sure to delete the selected module? ", "Delete",
	    						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
	    				{
							Module module = cvsFile.getModule();
	            			CVSConfigureHelper.removeModule(module);
	        				AmbOptionPane.showMessageDialog("The selected module has been deleted", 
	        						"Info", JOptionPane.INFORMATION_MESSAGE);
	        				
	        				contentPane.removeAll();
	        				initPanel();
	        				initActions();
	    				}
					}
					else
					{
						AmbOptionPane.showMessageDialog("The selected node is not a module node", 
	    						"Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog("No module node is selected", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		//"Edit Module" button
		jb5.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultMutableTreeNode selectedTreeNode = 
					(DefaultMutableTreeNode) moduleFilesTree.getLastSelectedPathComponent();
				if ( selectedTreeNode != null )
				{
					CVSFile cvsfile = (CVSFile) selectedTreeNode.getUserObject();
					if ( cvsfile.getNodeType().equals(Constants.NODE_TYPE_MODULE) )
					{
						Module module = cvsfile.getModule();
						CVSModuleDialog moduleDialog = new CVSModuleDialog(SwingHelper.getMainFrame(), true);
						moduleDialog.setTitle(e.getActionCommand());
						moduleDialog.setAddOrEditFlag(Constants.CVS_EDIT_MODULE);
						moduleDialog.setSelectedModule(module);
						moduleDialog.setModuleFields(module);
						moduleDialog.setVisible(true);
						//if "OK" button is clicked, refresh current UI
						if ( moduleDialog.isOKClicked() )
						{
	        				contentPane.removeAll();
	        				initPanel();
	        				initActions();							
						}
					}
					else
					{
						AmbOptionPane.showMessageDialog("The selected node is not a module node", 
	    						"Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog("No module node is selected", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}); 
		
		//Module Mapping
		jb6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CVSModuleMappingDialog dialog = new CVSModuleMappingDialog(SwingHelper.getMainFrame(), true);
				dialog.setTitle("Module Mapping");
				dialog.setVisible(true);
			}
		});
		
		//"Submit to Create Job"
		jb7.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//get files to add to job "File[]"
				File[] files = null;
				List<File> fileList = new ArrayList();
				
				Enumeration cvsfiles = defListModel.elements();
				while ( cvsfiles.hasMoreElements() )
				{
					File file = (File) cvsfiles.nextElement();
					fileList.add(file);
				}
				
				if (fileList != null && fileList.size() > 0)
				{
					files = new File[fileList.size()];
					for (int k=0; k<fileList.size(); k++ )
					{
						files[k] = (File) fileList.get(k);
					}
				}

				//Open or focus on create job panel
				MainFrame mf = SwingHelper.getMainFrame();
				ClosableTabbedPane ctp = mf.getCloseTabbedPanel();
				
				if (mf.isAddable(Constants.CREATE_JOB_TITLE))
				{
					CreateJobPanel createJobPanel = new CreateJobPanel();
					ctp.add(ctp.getTabCount(), Constants.CREATE_JOB_TITLE, createJobPanel);
					mf.addKeyListener(createJobPanel);
					createJobPanel.setFromCVS(true);
					createJobPanel.setCvsModules(cvsFileInfos);
					createJobPanel.addAllFiles(files);
				}
				else
				{
					ctp.setSelectedIndex(mf.getIndex(Constants.CREATE_JOB_TITLE)==-1?0:mf.getIndex(Constants.CREATE_JOB_TITLE));
					Component comp = ctp.getSelectedComponent();
					if ( comp instanceof CreateJobPanel)
					{
						((CreateJobPanel) comp).setFromCVS(true);
						((CreateJobPanel) comp).setCvsModules(cvsFileInfos);
						((CreateJobPanel) comp).addAllFiles(files);
					}
				}

			}
		});
		
		//"CVS Configuration" button
		jb8.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CVSConfigurationMainPanel cvsConMainPanel = 
					new CVSConfigurationMainPanel(SwingHelper.getMainFrame(), true);
				cvsConMainPanel.setVisible(true);
				
				contentPane.removeAll();
				initPanel();
				initActions();
			}
		});
		
		//file tree <JTree>
		moduleFilesTree.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) {
				TreePath path = moduleFilesTree.getPathForLocation(e.getX(), e.getY());
				if (path == null) 
				{
					return;
				}
				moduleFilesTree.setSelectionPath(path);
				
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				CVSFile selectedNodeCVSFile = (CVSFile) selectedNode.getUserObject();
				String selectedNodeType = selectedNodeCVSFile.getNodeType();

				//only show right clicking menu for 'File' nodes
				if ( e.getButton() == 3 )
				{
					popMenu.show(moduleFilesTree, e.getX(), e.getY());
				}
				
				if ( selectedNodeType.equals(Constants.NODE_TYPE_FILE))	{
					updateItem.setEnabled(true);
				} else {
					updateItem.setEnabled(false);
				}
				
				if ( selectedNode.isLeaf() ) {
					expandAllItem.setEnabled(false);
					collapseAllItem.setEnabled(false);
				} else {
					expandAllItem.setEnabled(true);
					collapseAllItem.setEnabled(true);
				}
				
				//for double click
				if ( e.getClickCount() == 2 )
				{
					if ( selectedNodeType.equals(Constants.NODE_TYPE_PATH) )
					{
//
					}
				}
			}

			public void mouseReleased(MouseEvent e) {

			}
		});
		
		//"update cvs" sub menu
		updateItem.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) 
			{
				try {
					TreePath path2 = moduleFilesTree.getSelectionPath();
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path2.getLastPathComponent();
					CVSFile selectedCVSFile = (CVSFile) selectedNode.getUserObject();
					
					//only update for 'File' node
					if ( selectedCVSFile.getNodeType().equals(Constants.NODE_TYPE_FILE))
					{
						//set value for variable 'targetNode'
						findRepOrModuleNode(selectedNode, Constants.NODE_TYPE_REPOSITORY);
						if ( targetNode != null )
						{
							Repository myRepository = ((CVSFile) targetNode.getUserObject()).getRepository();
							String cvsFullRoot = myRepository.getFullCVSRoot();
							
							String[] cmd;
							String workDir = null;
							
							File file = selectedCVSFile.getFile();
							if ( file.isDirectory())
							{
								cmd = new String[6];
								cmd[0] = "cvs";
								cmd[1] = "-d";
								cmd[2] = cvsFullRoot;
								cmd[3] = "update";
								cmd[4] = "-P";
								cmd[5] = "-d";
								workDir = file.getAbsolutePath();
							}
							else
							{
								cmd = new String[7];
								cmd[0] = "cvs";
								cmd[1] = "-d";
								cmd[2] = cvsFullRoot;
								cmd[3] = "update";
								cmd[4] = "-P";
								cmd[5] = "-d";
								cmd[6] = file.getName();
								String fullPath = file.getPath();
								workDir = fullPath.substring(0, fullPath.lastIndexOf(file.getName()));
							}
							
//							CmdExecuter cmdExecuter = new CmdExecuter();
//							String response = cmdExecuter.run(cmd, workDir);
							HashMap paramMap = new HashMap();
							paramMap.put(Constants.CVS_WORK_DIRECTORY, workDir);
							List cmdList = new ArrayList();
							cmdList.add(cmd);
							paramMap.put(Constants.CVS_COMMAND, cmdList);
							CVSWorkingOutput output = new CVSWorkingOutput();
							output.setParameters(paramMap);
//							SwingUtilities.invokeLater(output);
						}
						targetNode = null;
					}
				}
				catch (Exception ex) 
				{	
					log.error(ex.getMessage(), ex);
					ex.printStackTrace();
				}
			}

			public void mouseReleased(MouseEvent e) {

			}
		});
		
		//"expand all" sub menu
		expandAllItem.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) 
			{
				TreePath selectedPath = moduleFilesTree.getSelectionPath();
				expandAll(moduleFilesTree, selectedPath, true); 
			}

			public void mouseReleased(MouseEvent e) {

			}
		});
		
		//"collapse all" sub menu
		collapseAllItem.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) 
			{
				TreePath selectedPath = moduleFilesTree.getSelectionPath();
				expandAll(moduleFilesTree, selectedPath, false); 
			}

			public void mouseReleased(MouseEvent e) {

			}
		});
		
	}
	
	/**
	 * Add all files to file tree
	 * Every node is a 'CVSFile' object, invoke getNodeType() to get node type,
	 * (node type: root, repository, module, path, file), then invoke getXXX()
	 * to get corresponding object.
	 */
	private DefaultMutableTreeNode fillModuleFilesTree()
	{
		CVSFile rootCVSFile = new CVSFile(Constants.NODE_TYPE_ROOT);
		rootCVSFile.setRoot("CVS_ROOT");
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootCVSFile);
		
		List repList = CVSConfigureHelper.getRepositoryList();	
		if ( repList != null && repList.size() > 0 )
		{
			for ( int i=0; i<repList.size(); i++)
			{
				Repository rep = (Repository) repList.get(i);
				rep.setIsAllFlag(false);
				CVSFile repositoryCVSFile = new CVSFile(Constants.NODE_TYPE_REPOSITORY);
				repositoryCVSFile.setRepository(rep);
				DefaultMutableTreeNode repositoryNode = new DefaultMutableTreeNode(repositoryCVSFile);
				rootNode.add(repositoryNode);

				List moduleList = CVSConfigureHelper.getModulesByRepositoryIndex(rep.getIndex());
				for (int j=0; j<moduleList.size(); j++)
				{
					//add "Module" to "root"
					Module module = (Module) moduleList.get(j);
					CVSFile moduleCVSFile = new CVSFile(Constants.NODE_TYPE_MODULE);
					moduleCVSFile.setModule(module);
					DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(moduleCVSFile);
					repositoryNode.add(moduleNode);

					//add "module path" to "Module"
					Vector modulePaths = module.getModulePath();
					if ( modulePaths != null && modulePaths.size() > 0 )
					{
						for (int k=0; k<modulePaths.size(); k++)
						{
							String mpath = (String) modulePaths.get(k);
							CVSFile modulePathCVSFile = new CVSFile(Constants.NODE_TYPE_PATH);
							modulePathCVSFile.setModulePath(mpath);
							DefaultMutableTreeNode modulePathNode = new DefaultMutableTreeNode(modulePathCVSFile);
							moduleNode.add(modulePathNode);

							File pathFile = new File(rep.getCvsSandbox() + "\\" + mpath);
							File[] files = pathFile.listFiles();
							if ( files != null && files.length > 0 )
							{
								for ( int m=0; m<files.length; m++ )
								{
									ListDirAndFiles.list( files[m], modulePathNode );									
								}
							}

						}
					}
				}
			}
			
		}
		
		return rootNode;

	}

	private void addSubFilesToJob(DefaultMutableTreeNode cvsFileNode)
	{
		if ( cvsFileNode != null )
		{
			CVSFile cvsFile = (CVSFile) cvsFileNode.getUserObject();
			String nodeType = cvsFile.getNodeType();
			
			if ( nodeType.equals(Constants.NODE_TYPE_FILE))
			{
				File file = cvsFile.getFile();
				findRepOrModuleNode(cvsFileNode, Constants.NODE_TYPE_REPOSITORY);
				cvsserver = ((CVSFile)targetNode.getUserObject()).getRepository().getCvsRoot();
				sandBox = ((CVSFile)targetNode.getUserObject()).getRepository().getCvsSandbox();
				findRepOrModuleNode(cvsFileNode, Constants.NODE_TYPE_MODULE);
				cvsmodule = ((CVSFile)targetNode.getUserObject()).getModule().getModuleName();
				listFiles(file);
			}
			else
			{
				Enumeration childNodes = cvsFileNode.children();
				if ( childNodes != null )
				{
					while ( childNodes.hasMoreElements() )
					{
						DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
						addSubFilesToJob(childNode);
					}
				}

			}
		}
	}
	
	/**
	 * List files to file tree
	 * @param file <File>
	 */
	private void listFiles(File file)
	{
    	if ( !file.exists() )
    	{
    		log.error("The path '" + file + "' doesn't exist!");
    		AmbOptionPane.showMessageDialog("'" + file + "' is not accessible", 
					"Warning", JOptionPane.WARNING_MESSAGE);
	    }
	    else
	    {
    		if ( file.isFile() && !isFileExisted(file) ) 
	    	{
    			defListModel.addElement(file);
    			cvsFileInfos.put(file, getCVSPath(file));
	    	}
    		else
    		{
    	    	if ( !file.getName().equalsIgnoreCase("CVS") )
    	    	{
        			File[] files = file.listFiles();
        			if (files != null) {
	        			for ( int i=0; i<files.length; i++ )
	        			{
	        				listFiles(files[i]);
	        			}
        			}
    	    	}
    		}
	    }
	}
	
	/**
	 * Judge if this file has been added to job contents
	 * @param file <File>
	 * @return boolean
	 */
	private boolean isFileExisted(File file)
	{
		boolean isExisted = false;
		
		Enumeration addedFiles = defListModel.elements();
		while ( addedFiles.hasMoreElements() )
		{
			File tfile = (File) addedFiles.nextElement();
			if ( tfile.equals(file) )
			{
				isExisted = true;
			}
		}
		
		return isExisted;
	}
	
	/**
	 * Find target node for current node
	 * @param node: current node
	 * @param target: "Constants.NODE_TYPE_REPOSITORY" or "NODE_TYPE_MODULE"
	 * @return target node <DefaultMutableTreeNode>
	 */
	private void findRepOrModuleNode(DefaultMutableTreeNode currentNode, String target)
	{
		if ( currentNode != null )
		{
			CVSFile cvsFile = (CVSFile) currentNode.getUserObject();
			String nodeType = cvsFile.getNodeType();
			if ( nodeType.equals(target))
			{
				targetNode = currentNode;
			}
			else
			{
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
				findRepOrModuleNode(parentNode, target);
			}
		}
	}
	
	private void expandAll(JTree tree, TreePath parent, boolean expand) 
	{
		// Traverse children
		TreeNode node = (TreeNode)parent.getLastPathComponent();
		if (node.getChildCount() >= 0) 
		{
			for ( Enumeration e=node.children(); e.hasMoreElements(); ) 
			{
				TreeNode n = (TreeNode)e.nextElement();  
				TreePath path = parent.pathByAddingChild(n);  
				expandAll(tree, path, expand);  
			}   
		}  

		// Expansion or collapse must be done bottom-up  
		if (expand)	{  
			tree.expandPath(parent);  
		} else {  
			tree.collapsePath(parent);  
		}  
	}  

	public String getSandBox() {
		if (targetNode != null)
			return ((CVSFile)targetNode.getUserObject()).getRepository().getCvsSandbox();
		else
			return "";
	}
	
	public String getCVSPath(File p_file) {
		if (targetNode != null) {
			String filepath = p_file.getAbsolutePath();
			filepath = filepath.substring(filepath.indexOf(sandBox)+sandBox.length());
			filepath = filepath.substring(0, filepath.lastIndexOf(File.separator));
			return filepath.concat("/").replace('\\', '/');
		} else
			return "";
	}
}
