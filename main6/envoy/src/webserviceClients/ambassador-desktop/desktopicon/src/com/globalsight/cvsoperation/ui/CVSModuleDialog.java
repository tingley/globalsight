package com.globalsight.cvsoperation.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.globalsight.cvsoperation.entity.Module;
import com.globalsight.cvsoperation.entity.Repository;
import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class CVSModuleDialog extends JDialog 
{
	private static final long serialVersionUID = -5510242783467800792L;
	
	private JLabel jl1, jl2, jl3, jl4, jl5;
	
	private JComboBox jcb1, jcb2;
	
	private JTextField jtf1, jtf2, jtf3; 
	
	private JButton jb1, jb2, jb3, jb4;
	
	private JScrollPane jsp;
	
	private JList jlist;
	
	private DefaultListModel defListModel = new DefaultListModel();
	
	private String addOrEditFlag = null;
	
	private Module module = null;
	
	private boolean isOKClicked = false;

	public CVSModuleDialog(Frame p_owner, boolean modal)
	{
		super(p_owner);
		this.setResizable(false);
		this.setSize(600, 400);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		init();
		addActions();
	}
	
	private void init()
	{
    	Container contentPane = this;
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		//
		jl1 = new JLabel("Repository:", SwingConstants.LEFT);
		jl1.setMaximumSize(new Dimension(130,30));
		jl1.setMinimumSize(new Dimension(130,30));
		jl1.setPreferredSize(new Dimension(130,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jl1, c);
		
		jcb1 = new JComboBox(CVSConfigureHelper.getRepositoryList().toArray());
		jcb1.setSelectedIndex(-1);
		jcb1.setMaximumSize(new Dimension(300,30));
		jcb1.setMinimumSize(new Dimension(300,30));
		jcb1.setPreferredSize(new Dimension(300,30));
		jcb1.setOpaque(true);
        jcb1.setRenderer(new ComboBoxRenderer());
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jcb1, c);
		
		//
		jl2 = new JLabel("Module Name:", SwingConstants.LEFT);
		jl2.setMaximumSize(new Dimension(130,30));
		jl2.setMinimumSize(new Dimension(130,30));
		jl2.setPreferredSize(new Dimension(130,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jl2, c);
		
		jtf1 = new JTextField();
		jtf1.setMaximumSize(new Dimension(300,30));
		jtf1.setMinimumSize(new Dimension(300,30));
		jtf1.setPreferredSize(new Dimension(300,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf1, c);
		
		//
		jl3 = new JLabel("Module Path:", SwingConstants.LEFT);
		jl3.setMaximumSize(new Dimension(130,30));
		jl3.setMinimumSize(new Dimension(130,30));
		jl3.setPreferredSize(new Dimension(130,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jl3, c);
		
		jtf2 = new JTextField();
		jtf2.setMaximumSize(new Dimension(300,30));
		jtf2.setMinimumSize(new Dimension(300,30));
		jtf2.setPreferredSize(new Dimension(300,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf2, c);
		
		jb1 = new JButton("Add");
		jb1.setMaximumSize(new Dimension(100,30));
		jb1.setMinimumSize(new Dimension(100,30));
		jb1.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(5, 2, 5, 30);
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jb1, c);
		
		//
		jlist = new JList(defListModel);
		jlist.setAutoscrolls(true);
		jsp = new JScrollPane(jlist);
		jsp.setMaximumSize(new Dimension(300,100));
		jsp.setMinimumSize(new Dimension(300,100));
		jsp.setPreferredSize(new Dimension(300,100));
		c.insets = new Insets(5, 2, 5, 2);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jsp, c);
		
		//
		jl4 = new JLabel("Branch/Tag/Revision:", SwingConstants.LEFT);
		jl4.setMaximumSize(new Dimension(130,30));
		jl4.setMinimumSize(new Dimension(130,30));
		jl4.setPreferredSize(new Dimension(130,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jl4, c);
		
		jtf3 = new JTextField();
		jtf3.setMaximumSize(new Dimension(300,30));
		jtf3.setMinimumSize(new Dimension(300,30));
		jtf3.setPreferredSize(new Dimension(300,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf3, c);
		
		jb2 = new JButton("Fetch list...");
		jb2.setMaximumSize(new Dimension(100,30));
		jb2.setMinimumSize(new Dimension(100,30));
		jb2.setPreferredSize(new Dimension(100,30));
		jb2.setEnabled(false);
		jb2.setVisible(false);
		c.insets = new Insets(5, 2, 5, 30);
		c.gridx = 2;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jb2, c);
		
		//
		jl5 = new JLabel("Project:");
		jl5.setMaximumSize(new Dimension(130,30));
		jl5.setMinimumSize(new Dimension(130,30));
		jl5.setPreferredSize(new Dimension(130,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jl5, c);
		
		jcb2 = new JComboBox(CVSConfigureHelper.getAllModuleProjects().toArray());
		jcb2.setSelectedIndex(-1);
		jcb2.setMaximumSize(new Dimension(300,30));
		jcb2.setMinimumSize(new Dimension(300,30));
		jcb2.setPreferredSize(new Dimension(300,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		jcb2.setEditable(true);
		contentPane.add(jcb2, c);
		
		//
		jb3 = new JButton("OK");
		jb3.setMaximumSize(new Dimension(100,30));
		jb3.setMinimumSize(new Dimension(100,30));
		jb3.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(10, 0, 5, 30);
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jb3, c);
		
		jb4 = new JButton("Cancel");
		jb4.setMaximumSize(new Dimension(100,30));
		jb4.setMinimumSize(new Dimension(100,30));
		jb4.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(10, 0, 5, 200);
		c.gridx = 1;
		c.gridy = 6;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jb4, c);
	}
	
	private void addActions()
	{
		//"Add" module path button
		jb1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String modulePath = jtf2.getText();
				if ( modulePath != null && !"".equals(modulePath))
				{
					//Make sure no duplicated module path is added for current module name
					Enumeration existedMPs = defListModel.elements();
					boolean isExisted = false;
					while ( existedMPs.hasMoreElements() ) 
					{
						String mp = (String) existedMPs.nextElement();
						if ( mp.equals(modulePath))
						{
							isExisted = true;
						}
					}
					if ( !isExisted )
					{
						defListModel.addElement(modulePath);					
					}
					else
					{
						AmbOptionPane.showMessageDialog("This module path has been added",
								"Warning", JOptionPane.WARNING_MESSAGE);
					}					
				}
			}
		});
		
		/*
		 * Double click one path to remove it
		 */
		jlist.addMouseListener(new MouseAdapter() 
		{
		    public void mouseClicked(MouseEvent e) 
		    {
		    	if (e.getClickCount() == 2) 
		    	{
					int index = jlist.locationToIndex(e.getPoint());
					if (index >= 0) 
					{
						Object path = defListModel.getElementAt(index);
						defListModel.removeElement(path);
						jtf2.setText((String) path);
					}
		    	}
		    }
		});
	
		//"OK" save configurations and checkout from cvs server
		jb3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				isOKClicked = true;//"OK" button is clicked to checkout
				
				Repository rep = (Repository) jcb1.getSelectedItem();
				String moduleName = jtf1.getText();
				Enumeration modulePaths = defListModel.elements();
				String btr = jtf3.getText();
				String project = (String) jcb2.getSelectedItem();

				if ( rep == null ) 
				{
					AmbOptionPane.showMessageDialog("No repository is selected",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				else if ( moduleName == null || "".equals(moduleName.trim()) )
				{
					AmbOptionPane.showMessageDialog("Module name can't be null",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				else if ( modulePaths == null || !modulePaths.hasMoreElements() )
				{
					AmbOptionPane.showMessageDialog("There should be at least one module path",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				else if ( btr == null || "".equals(btr.trim()))
				{
					AmbOptionPane.showMessageDialog("Branch|Tag|Revision can't be null",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				else if ( project == null || "".equals(project.trim()) )
				{
					AmbOptionPane.showMessageDialog("Project can't be null",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				
				boolean ifContinue = true;
				if ( rep != null && 
					 moduleName != null && !"".equals(moduleName.trim()) &&
					 modulePaths != null && modulePaths.hasMoreElements() &&
					 btr != null && !"".equals(btr.trim()) &&
					 project != null && !"".equals(project.trim()) )
				{
					//save module name (make sure no duplicated module name 
					//is saved for current repository
					String strMN = CVSConfigureHelper.getProperty(Constants.CVS_MODULE_NAME);
					if ( addOrEditFlag.equals(Constants.CVS_ADD_MODULE) )
					{
						boolean isExisted = false;
						if ( strMN != null && !"".equals(strMN.trim()) )
						{
							String[] mns = strMN.split(";");
							for (int i=0; i<mns.length; i++)
							{
								String[] mn = mns[i].split("<->");
								if ( mn[1].equals(moduleName))
								{
									isExisted = true;
								}
							}
							if ( !isExisted ) // module name is unique for all repositories
							{
								strMN = strMN + ";" + rep.getIndex() + "<->" + moduleName;
								CVSConfigureHelper.setProperty(Constants.CVS_MODULE_NAME, strMN);			
							}
							else 
							{
								AmbOptionPane.showMessageDialog(moduleName + " for current repository has been existed",
								"Warning", JOptionPane.WARNING_MESSAGE);
								ifContinue = false;
							}
						}
						else {
							CVSConfigureHelper.setProperty(Constants.CVS_MODULE_NAME, rep.getIndex() + "<->" + moduleName);		
						}
					}
					else
					{
						//edit module: no need to save module name 
						//module name is not allowed to edit once it is saved
					}

					if ( ifContinue )
					{
						//save module paths (no need to check if duplicated, see listener of jb1)
						CVSConfigureHelper.removeModulePath(rep.getIndex(), moduleName);
						String mp = CVSConfigureHelper.getProperty(Constants.CVS_MODULE_PATH);
						StringBuffer sb = new StringBuffer();
						while ( modulePaths.hasMoreElements() )
						{
							String path = (String) modulePaths.nextElement();
							if ( sb.length() == 0 )
							{
								sb.append(rep.getIndex() + "<->" + moduleName + "<->" + path);
							}
							else 
							{
								sb.append(";" + rep.getIndex() + "<->" + moduleName + "<->" + path);
							}
						}
						if (mp != null && !"".equals(mp.trim()))
						{
							mp = mp + ";" + sb.toString();
						}
						else 
						{
							mp = sb.toString();
						}
						CVSConfigureHelper.setProperty(Constants.CVS_MODULE_PATH, mp);
						
						//save BTR
						CVSConfigureHelper.removeModuleBTR(rep.getIndex(), moduleName);
						String btrs = CVSConfigureHelper.getProperty(Constants.CVS_BRANCH_TAG_REVISION);
						if ( btrs != null && !"".equals(btrs.trim()))
						{
							btrs = btrs + ";" + rep.getIndex() + "<->" + moduleName + "<->" + btr;
						}
						else
						{
							btrs = rep.getIndex() + "<->" + moduleName + "<->" + btr;
						}
						CVSConfigureHelper.setProperty(Constants.CVS_BRANCH_TAG_REVISION, btrs);
						
						//save project
						CVSConfigureHelper.removeModuleProject(rep.getIndex(), moduleName);
						String cvs_projects = CVSConfigureHelper.getProperty(Constants.CVS_PROJECT);
						if ( cvs_projects != null && !"".equals(cvs_projects.trim()))
						{
							cvs_projects = cvs_projects + ";" + rep.getIndex() + "<->" + moduleName + "<->" + project;
						}
						else
						{
							cvs_projects = rep.getIndex() + "<->" + moduleName + "<->" + project;
						}
						
						CVSConfigureHelper.setProperty(Constants.CVS_PROJECT, cvs_projects);
						
						//close current dialog
						CVSModuleDialog.this.dispose();	
						
						//invoke "checkout" method
						HashMap map = new HashMap();
						map.put(Constants.CVS_WORK_DIRECTORY, rep.getCvsSandbox());
						String cvsroot = "";
						if ("ext".equalsIgnoreCase(CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL))) 
						{
							cvsroot = ":" + CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL) + 
										":" + CVSConfigureHelper.getProperty(Constants.CVS_USERNAME) + 
										"@" + rep.getCvsServer() +
										":" + rep.getCvsRoot();
						}
						else 
						{
							cvsroot = ":" + CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL) + 
										":" + CVSConfigureHelper.getProperty(Constants.CVS_USERNAME) +
										":" + CVSConfigureHelper.getProperty(Constants.CVS_PASSWORD) +
										"@" + rep.getCvsServer() + //":" + rep.getCvsServerPort() +
										":" + rep.getCvsRoot();
						}
						Enumeration mPaths = defListModel.elements();
						List<String[]> cmdList = new ArrayList();
						while (	mPaths != null && mPaths.hasMoreElements() )
						{
							String mpath = ((String) mPaths.nextElement()).trim();
							String[] cmd = {"cvs", "-d", cvsroot, "checkout", "-r", btr.trim(), "-P", mpath };
							cmdList.add(cmd);
							cmd = new String[]{"cvs", "-d", cvsroot, "update", "-d" };
							cmdList.add(cmd);
						}
						map.put(Constants.CVS_COMMAND, cmdList);

						CVSWorkingOutput output = new CVSWorkingOutput();
						output.setParameters(map);
					}
				}
			}
		});
		
		// "Cancel" button
		jb4.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				isOKClicked = false;
				
				CVSModuleDialog.this.dispose();
			}
		});
	}
	
	public void setAddOrEditFlag(String flag)
	{
		this.addOrEditFlag = flag;
	}
	
	public void setSelectedModule(Module module)
	{
		this.module = module;
	}
	
	public void setModuleFields(Module module)
	{
		if (module != null)
		{
			//set repository
			List repList = CVSConfigureHelper.getRepositoryList();
			for (int i=0; i<repList.size(); i++ ) 
			{
				Repository rep = (Repository) repList.get(i);
				if ( rep.getIndex() == module.getRepositoryIndex() )
				{
					jcb1.setSelectedItem(rep);
				}
			}
			jcb1.setEnabled(false);
			//set module name
			jtf1.setText(module.getModuleName());
			jtf1.setEnabled(false);
			//set module path
			Vector mpaths = module.getModulePath();
			if ( mpaths != null && mpaths.size() > 0 )
			{
				for (int j=0; j<mpaths.size(); j++)
				{
					defListModel.addElement(mpaths.get(j));	
				}
			}
			//set branch|tag|revision
			jtf3.setText(module.getBranchTagRevision());
			//set project
			jcb2.setSelectedItem(module.getProject());
		}
	}
	
	public boolean isOKClicked()
	{
		return this.isOKClicked;
	}
	
    // combobox renderer
    class ComboBoxRenderer extends BasicComboBoxRenderer 
    {
		private static final long serialVersionUID = 7263688250629758010L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
		{
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if ( index > -1 ) {
                    String repositoryName = (String) value.toString();
                    list.setToolTipText(repositoryName);
                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            
            return this;
        }
    }
}
