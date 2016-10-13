package com.globalsight.cvsoperation.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.globalsight.cvsoperation.entity.CVSFile;
import com.globalsight.cvsoperation.entity.Module;
import com.globalsight.cvsoperation.entity.Repository;
import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.cvsoperation.util.ListDirAndFiles;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class CVSTreeDialog extends JDialog {
	private static final long serialVersionUID = -4427015441164776217L;

	private JTree moduleFilesTree;
	
	private JScrollPane jScrollPaneFiles1;
	
	private int width = SwingHelper.getMainFrame().getWidth();
	
	private Container contentPane = this;
	
	private JButton newFolderBtn, okBtn, cancelBtn;

	private String treeInfo;

	private DefaultMutableTreeNode targetNode = null;
	
	private boolean isOKReturn = false;
	
	private String CVSPath = "", CVSFullPath = "", CVSSandBox = "";

	public CVSTreeDialog(Frame p_owner, boolean modal)
	{
		super(p_owner);
		this.setResizable(false);
		this.setSize(600, 400);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		init();
		initAction();
	}

	private void init() {
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(2, 2, 2, 2);

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
		jScrollPaneFiles1.setMinimumSize(new Dimension(580, 300));
		jScrollPaneFiles1.setMaximumSize(new Dimension(580, 300));
		jScrollPaneFiles1.setPreferredSize(new Dimension(580,300));
		jScrollPaneFiles1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPaneFiles1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		contentPane.add(jScrollPaneFiles1, c1);
		
		JPanel tmp = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(2, 2, 2, 2);

		okBtn = new JButton("OK");
		okBtn.setPreferredSize(new Dimension(160,30));
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		c2.anchor = GridBagConstraints.CENTER;
		tmp.add(okBtn, c2);

		cancelBtn = new JButton("Cancel");
		cancelBtn.setPreferredSize(new Dimension(160,30));
		cancelBtn.setToolTipText("Make a new folder in selected CVS Repository");
		c2.gridx = 1;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		c2.anchor = GridBagConstraints.CENTER;
		tmp.add(cancelBtn, c2);
		
		c1.gridx = 0;
		c1.gridy = 1;
		contentPane.add(tmp, c1);
	}
	
	private void initAction(){
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

				//for double click
				if ( e.getClickCount() == 2 )
				{
					if ( selectedNodeType.equals(Constants.NODE_TYPE_PATH) )
					{
					}
				}
			}

			public void mouseReleased(MouseEvent e) {

			}
		});
		
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (moduleFilesTree.isSelectionEmpty()) 
				{
					AmbOptionPane.showMessageDialog("Please select one node first", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				TreePath path = moduleFilesTree.getSelectionPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				CVSFile selectedNodeCVSFile = (CVSFile) selectedNode.getUserObject();
				if ( selectedNodeCVSFile.getNodeType().equals(Constants.NODE_TYPE_FILE) || selectedNodeCVSFile.getNodeType().equals(Constants.NODE_TYPE_PATH))
				{
					//set value for variable 'targetNode'
					findRepOrModuleNode(selectedNode, Constants.NODE_TYPE_REPOSITORY);
					if ( targetNode != null )
					{
						Repository myRepository = ((CVSFile) targetNode.getUserObject()).getRepository();
						String CVSRoot = myRepository.getCvsRoot().replace("/", "");
						String CVSSandBox = myRepository.getCvsSandbox();
						
						targetNode = null;
						findRepOrModuleNode(selectedNode, Constants.NODE_TYPE_MODULE);
						String moduleName = "";
						if (targetNode != null) {
							Module selectedModule = ((CVSFile) targetNode.getUserObject()).getModule();
							moduleName = selectedModule.getModuleName();
						} 
							
						String cvsFullRoot = myRepository.getFullCVSRoot();
						
						File file = null;
						if (selectedNodeCVSFile.getNodeType().equals(Constants.NODE_TYPE_FILE))
							file = selectedNodeCVSFile.getFile();
						else
							file = new File("/" + selectedNodeCVSFile.getModulePath());
						String tmpPath = file.getPath();
						String sandBoxPath = myRepository.getCvsSandbox();
						treeInfo = tmpPath.replace(sandBoxPath, "");
						treeInfo = treeInfo.replace("\\", "/").concat("/");
						setCVSFullPath(CVSRoot + "\\" + moduleName + treeInfo);
						setCVSPath(treeInfo);
						setCVSSandBox(CVSSandBox);
					}
					targetNode = null;
					isOKReturn = true;
				}
				CVSTreeDialog.this.dispose();
			}
		});
		
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AmbOptionPane.showConfirmDialog("Are you sure to cancel the operation and exit?", "Info", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					isOKReturn = false;
					CVSTreeDialog.this.dispose();
				}
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

				List moduleList = CVSConfigureHelper.getModulesByRepositoryIndex(rep.getIndex());
				for (int j=0; j<moduleList.size(); j++)
				{
					//add "Module" to "root"
					Module module = (Module) moduleList.get(j);
					CVSFile moduleCVSFile = new CVSFile(Constants.NODE_TYPE_MODULE);
					moduleCVSFile.setModule(module);
					DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(moduleCVSFile);
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
									ListDirAndFiles.list( files[m], modulePathNode, false );									
								}
							}

						}
					}
					repositoryNode.add(moduleNode);
				}
				rootNode.add(repositoryNode);
			}
		}
		
		return rootNode;
	}
	
	public String getTreeInfo() {
		return this.treeInfo;
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

	public boolean isOKReturn() {
		return this.isOKReturn;
	}

	public String getCVSPath() {
		return CVSPath;
	}

	public void setCVSPath(String path) {
		CVSPath = path;
	}

	public String getCVSFullPath() {
		return CVSFullPath;
	}

	public void setCVSFullPath(String fullPath) {
		CVSFullPath = fullPath;
	}

	public String getCVSSandBox() {
		return CVSSandBox;
	}

	public void setCVSSandBox(String cVSSandBox) {
		CVSSandBox = cVSSandBox;
	}
	
	
}
