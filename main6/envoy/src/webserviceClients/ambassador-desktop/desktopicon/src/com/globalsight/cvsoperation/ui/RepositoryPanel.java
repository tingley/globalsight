package com.globalsight.cvsoperation.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.globalsight.cvsoperation.entity.Repository;
import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.entity.User;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class RepositoryPanel extends JPanel 
{
	private static final long serialVersionUID = -4287281407152717691L;

	private CVSConfigurationMainPanel parent = null; 
	
	private JList jlRepositories;
	
	private DefaultListModel defListModel = new DefaultListModel();
	
	private JScrollPane jsRepositories;
	
	private JButton addBtn, editBtn, delBtn, closeBtn;
	
	public RepositoryPanel()
	{
		init();
		addActions();
	}
	
	public RepositoryPanel(CVSConfigurationMainPanel parent)
	{
		this.parent = parent;
		init();
		addActions();
	}
	
	private void init()
	{
    	Container contentPane = this;
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		List repList = CVSConfigureHelper.getRepositoryList();
		if ( repList != null && repList.size() > 0 ) 
		{
			for (int i=0; i<repList.size(); i++ )
			{
				Repository rep = (Repository) repList.get(i);
				defListModel.add(i, rep);	
			}			
		}
		
		jlRepositories = new JList(defListModel);
		jlRepositories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jsRepositories = new JScrollPane(jlRepositories);
		jsRepositories.setMinimumSize(new Dimension(550, 280));
		jsRepositories.setMaximumSize(new Dimension(550, 280));
		jsRepositories.setPreferredSize(new Dimension(550, 280));
		jsRepositories.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsRepositories.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 3;
		contentPane.add(jsRepositories, c);
		
		addBtn = new JButton("Add Repository");
		c.insets = new Insets(20, 10, 20, 10);
		addBtn.setMinimumSize(new Dimension(120,30));
		addBtn.setMaximumSize(new Dimension(120,30));
		addBtn.setPreferredSize(new Dimension(120,30));
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(addBtn, c);
		
		editBtn = new JButton("Edit Repository");
		c.insets = new Insets(20, 10, 20, 10);
		editBtn.setMinimumSize(new Dimension(120,30));
		editBtn.setMaximumSize(new Dimension(120,30));
		editBtn.setPreferredSize(new Dimension(120,30));
		if ( defListModel.size() == 0 ) {
			editBtn.setEnabled(false);
		}
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(editBtn, c);
		
		c.insets = new Insets(20, 10, 20, 10);
		delBtn = new JButton("Delete Repository");
		delBtn.setMinimumSize(new Dimension(140,30));
		delBtn.setMaximumSize(new Dimension(140,30));
		delBtn.setPreferredSize(new Dimension(140,30));
		if ( defListModel.size() == 0 ) {
			delBtn.setEnabled(false);
		}
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(delBtn, c);
		
		closeBtn = new JButton("Close");
		c.insets = new Insets(20, 10, 20, 10);
		closeBtn.setMinimumSize(new Dimension(110,30));
		editBtn.setMaximumSize(new Dimension(110,30));
		editBtn.setPreferredSize(new Dimension(110,30));
		c.gridx = 3;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(closeBtn, c);
	}
	
	private void addActions()
	{
		addBtn.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e)
    		{
    			RepositoryEditDialog repPanel = 
    				new RepositoryEditDialog(SwingHelper.getMainFrame(), true);
    			String actionCmd = e.getActionCommand();
    			repPanel.setTitle(actionCmd);
    			repPanel.setAddOrEditFlag(Constants.CVS_ADD_REPOSITORY);
    			repPanel.setVisible(true);
    			
    			fillDefaultListModel();
    		}
    	});
		
		editBtn.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e)
    		{
    			Object obj = jlRepositories.getSelectedValue();
    			if ( obj != null && obj instanceof Repository)
    			{
    				Repository selectedRep = (Repository) obj;
    				RepositoryEditDialog repPanel = 
    					new RepositoryEditDialog(SwingHelper.getMainFrame(), true);
    				repPanel.setSelectedRepository(selectedRep);
    				repPanel.setRepositoryFields(selectedRep);
    				String actionCmd = e.getActionCommand();
    				repPanel.setTitle(actionCmd);
    				repPanel.setAddOrEditFlag(Constants.CVS_EDIT_REPOSITORY);
    				repPanel.setVisible(true);
    				
    				fillDefaultListModel();
    			} else {
    				AmbOptionPane.showMessageDialog("No repository is selected", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    			}
    		}
    	});
		
		delBtn.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e)
    		{
    			Object obj = jlRepositories.getSelectedValue();
    			if ( obj != null && obj instanceof Repository)
    			{
    				if (AmbOptionPane.showConfirmDialog("Are you sure to delete selected repository? ", "Delete",
    						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
    				{
        				Repository rep = (Repository) obj;
            			String key = Constants.CVS_REPOSITORY + "_" + rep.getIndex();
            			CVSConfigureHelper.removeProperty(key);
        				AmbOptionPane.showMessageDialog("The selected repository has been deleted", 
        						"Info", JOptionPane.INFORMATION_MESSAGE);
        				fillDefaultListModel();
    				}
    			}
    			else
    			{
    				AmbOptionPane.showMessageDialog("No repository is selected", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    			}
 
    		}
    	});
		
		closeBtn.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e)
    		{
//    			RepositoryPanel.this.getParent().remove(RepositoryPanel.this);
    			if (parent != null)
    			{
    				parent.removeAll();
    				parent.dispose();
    			}
     		}
    	});
	}
	
	private void fillDefaultListModel()
	{
		List repList = CVSConfigureHelper.getRepositoryList();
		defListModel.clear();
		if ( repList != null && repList.size() > 0 )
		{
			for (int i=0; i<repList.size(); i++ )
			{
				Repository tempRep = (Repository) repList.get(i);
				defListModel.add(i, tempRep);				
			}
			editBtn.setEnabled(true);
			delBtn.setEnabled(true);
		}
		else
		{
			editBtn.setEnabled(false);
			delBtn.setEnabled(false);
		}
	}
}
