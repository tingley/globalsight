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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.globalsight.cvsoperation.entity.Repository;
import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.ui.UserOptionsPanel;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class RepositoryEditDialog extends JDialog
{
	private static final long serialVersionUID = -6977919839831423761L;
	
	static Logger log = Logger.getLogger(RepositoryEditDialog.class.getName());
	
	private JLabel lb1, lb2, lb3, lb4, lb5;
	
	private JTextField jtf1, jtf2, jtf3, jtf4, jtf5;
	
	private JButton jbSave, jbCancel;
	
	private int intCvsRepNum, maxRepIndex;
	
	private Repository selectedRep = null;
	
	private String addOrEdit = "";

	public RepositoryEditDialog(boolean modal)
	{
		intCvsRepNum = CVSConfigureHelper.getRepositoryNum();
		maxRepIndex = CVSConfigureHelper.getMaxRepositoryIndex();
		
		this.setResizable(false);
		this.setModal(modal);
		setSize(600,400);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		init();
		addActions();
	}
	
	public RepositoryEditDialog(Frame p_owner, boolean modal)
	{
		super(p_owner, "Repository Configuration");
		intCvsRepNum = CVSConfigureHelper.getRepositoryNum();
		maxRepIndex = CVSConfigureHelper.getMaxRepositoryIndex();
		
		this.setResizable(false);
		this.setModal(modal);
		setSize(600,400);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 200, p.y + 150);
		init();
		addActions();
	}
	
	private void init()
	{

		
		Container contentPane = this;
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		lb1 = new JLabel("CVS Server:", SwingConstants.LEFT);
		lb1.setMaximumSize(new Dimension(120,30));
		lb1.setMinimumSize(new Dimension(120,30));
		lb1.setPreferredSize(new Dimension(120,30));
		lb1.setHorizontalAlignment(SwingConstants.LEFT);
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb1, c);
		
		jtf1 = new JTextField(); 
		jtf1.setToolTipText("For example: host IP or host name");
		jtf1.setMaximumSize(new Dimension(250,30));
		jtf1.setMinimumSize(new Dimension(250,30));
		jtf1.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 180);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf1, c);
		
		lb2 = new JLabel("Server Port:", SwingConstants.LEFT);
		lb2.setMaximumSize(new Dimension(120,30));
		lb2.setMinimumSize(new Dimension(120,30));
		lb2.setPreferredSize(new Dimension(120,30));
		lb2.setHorizontalAlignment(SwingConstants.LEFT);
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb2, c);
		
		jtf2 = new JTextField();
		jtf2.setToolTipText("For example:default 2401 for PSERVER,22 for EXT");
		jtf2.setMaximumSize(new Dimension(250,30));
		jtf2.setMinimumSize(new Dimension(250,30));
		jtf2.setPreferredSize(new Dimension(250,30));
		String protocol = CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL);
		if ( protocol != null && protocol.equals("EXT")) {
			jtf2.setText("22");
		} else if ( protocol != null && protocol.equals("PSERVER")) {
			jtf2.setText("2401");
		}
		c.insets = new Insets(5, 2, 5, 180);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf2, c);
		
		
		lb3 = new JLabel("Repository Folder:", SwingConstants.LEFT);
		lb3.setMaximumSize(new Dimension(120,30));
		lb3.setMinimumSize(new Dimension(120,30));
		lb3.setPreferredSize(new Dimension(120,30));
		lb3.setHorizontalAlignment(SwingConstants.LEFT);
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb3, c);
		
		jtf3 = new JTextField();
		jtf3.setToolTipText("For example: /cvs/root/host");
		jtf3.setMaximumSize(new Dimension(250,30));
		jtf3.setMinimumSize(new Dimension(250,30));
		jtf3.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 180);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf3, c);
		
		lb4 = new JLabel("Local CVS BIN:", SwingConstants.LEFT);
		lb4.setMaximumSize(new Dimension(120,30));
		lb4.setMinimumSize(new Dimension(120,30));
		lb4.setPreferredSize(new Dimension(120,30));
		lb4.setHorizontalAlignment(SwingConstants.LEFT);
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb4, c);
		
		jtf4 = new JTextField();
		jtf4.setToolTipText("For example: C:\\Program Files\\cvsnt");
		jtf4.setMaximumSize(new Dimension(250,30));
		jtf4.setMinimumSize(new Dimension(250,30));
		jtf4.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 180);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf4, c);
		
		lb5 = new JLabel("CVS Sandbox:", SwingConstants.LEFT);
		lb5.setMaximumSize(new Dimension(120,30));
		lb5.setMinimumSize(new Dimension(120,30));
		lb5.setPreferredSize(new Dimension(120,30));
		lb5.setHorizontalAlignment(SwingConstants.LEFT);
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb5, c);
		
		jtf5 = new JTextField();
		jtf5.setToolTipText("CVS sandbox path");
		jtf5.setMaximumSize(new Dimension(250,30));
		jtf5.setMinimumSize(new Dimension(250,30));
		jtf5.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 180);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf5, c);
		
		jbSave = new JButton("Save");
		c.insets = new Insets(20, 2, 80, 20);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jbSave, c);
		
		jbCancel = new JButton("Cancel");
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jbCancel, c);
	}
	
	private void addActions()
	{
		jbSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String cvs_server = jtf1.getText().trim();
				String server_port = jtf2.getText().trim();
				String cvs_root = jtf3.getText().trim();
				String local_cvs_bin = jtf4.getText().trim();
				String cvs_sandbox = jtf5.getText().trim();
				if ( cvs_server != null && !"".equals(cvs_server) &&
					 server_port != null && !"".equals(server_port) &&
					 cvs_root != null && !"".equals(cvs_root) &&
					 local_cvs_bin != null && !"".equals(local_cvs_bin) &&
					 cvs_sandbox != null && !"".equals(cvs_sandbox) )
				{
					Repository tmpRep = new Repository(999, cvs_server, cvs_root, local_cvs_bin, cvs_sandbox);
					if ( !isRepositoryExisted(tmpRep) )
					{
						//edit repository
						if ( addOrEdit.equals(Constants.CVS_EDIT_REPOSITORY) ) 
						{
							String strRep = selectedRep.getIndex() + ";" + cvs_server + ";" + 
								server_port + ";" + cvs_root + ";" + local_cvs_bin + ";" + cvs_sandbox;
							CVSConfigureHelper.setProperty(Constants.CVS_REPOSITORY + "_" + selectedRep.getIndex(), strRep);
						}
						else //add new repository
						{
							maxRepIndex++;
							String strRep = maxRepIndex + ";" + cvs_server + ";" + 
								server_port + ";" + cvs_root + ";" + local_cvs_bin + ";" + cvs_sandbox;
							CVSConfigureHelper.setProperty(Constants.CVS_REPOSITORY + "_" + maxRepIndex, strRep);
							intCvsRepNum = CVSConfigureHelper.getRepositoryNum();
							maxRepIndex = CVSConfigureHelper.getMaxRepositoryIndex();
						}
						boolean ie = checkSandboxIfExist(cvs_sandbox);
						if ( ie )
						{
		        			AmbOptionPane.showMessageDialog("Repository info is saved Successfully", "Info", JOptionPane.INFORMATION_MESSAGE);							
						}
						else
						{
							AmbOptionPane.showMessageDialog("Repository info is saved Successfully," +
									"but the sandbox folder does not exist,please create it manually", "Info", JOptionPane.INFORMATION_MESSAGE);
						}

	        			//close current dialog
	        			RepositoryEditDialog.this.dispose();						
					}
					else 
					{
						AmbOptionPane.showMessageDialog("The repository info is existed", "Warning", JOptionPane.WARNING_MESSAGE);
					}

				}
				else if ( cvs_server == null || "".equals(cvs_server))
				{
    				AmbOptionPane.showMessageDialog("CVS server can't be null", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    				jtf1.setFocusable(true);
				}
				else if ( server_port == null || "".equals(server_port))
				{
    				AmbOptionPane.showMessageDialog("CVS server port can't be null", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    				jtf2.setFocusable(true);
				}
				else if ( cvs_root == null || "".equals(cvs_root))
				{
    				AmbOptionPane.showMessageDialog("Repository folder can't be null", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    				jtf3.setFocusable(true);
				}
				else if ( local_cvs_bin == null || "".equals(local_cvs_bin))
				{
    				AmbOptionPane.showMessageDialog("Local CVS Bin can't be null", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    				jtf4.setFocusable(true);
				}
				else if ( cvs_sandbox == null || "".equals(cvs_sandbox))
				{
    				AmbOptionPane.showMessageDialog("CVS sandbox can't be null", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    				jtf5.setFocusable(true);
				}
			}
		});
		
		jbCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//close current dialog window
				RepositoryEditDialog.this.dispose();
			}
		});
	}
	
	/**
	 * Set the "Repository" object for editing
	 * @param rep
	 */
	public void setSelectedRepository(Repository rep)
	{
		this.selectedRep = rep;
	}
	
	/**
	 * set the flag to indicate adding a new repository or editing an existed repository
	 * @param addOrEdit "add" or "edit"
	 */
	public void setAddOrEditFlag(String addOrEdit)
	{
		this.addOrEdit = addOrEdit;
	}
	
	public void setRepositoryFields(Repository rep)
	{
		int index = -1;
		String cvs_server = null;
		int cvs_server_port = -1;
		String cvs_root = null;
		String cvs_local_bin = null;
		String cvs_sandbox = null;
		
		if ( rep != null )
		{
			index = rep.getIndex();
			cvs_server = rep.getCvsServer();
			cvs_server_port = rep.getCvsServerPort();
			cvs_root = rep.getCvsRoot();
			cvs_local_bin = rep.getCvsLocalBin();
			cvs_sandbox = rep.getCvsSandbox();
		}
		//set cvs server
		if ( cvs_server != null ) {
			jtf1.setText(cvs_server);			
		}
		//set cvs server port
		if ( cvs_server_port != -1 ) {
			jtf2.setText((new Integer(cvs_server_port)).toString());			
		} else if ( CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL) != null && 
				CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL).equalsIgnoreCase("pserver")) {
			jtf2.setText("2401");
		} else if ( CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL) != null && 
				CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL).equalsIgnoreCase("ext")) {
			jtf2.setText("22");
		}
		//set cvs root
		if ( cvs_root != null ) {
			jtf3.setText(cvs_root);			
		}
		//set cvs local bin path
		if ( cvs_local_bin != null ) {
			jtf4.setText(cvs_local_bin);
		}
		//set cvs sandbox path
		if ( cvs_sandbox != null ) {
			jtf5.setText(cvs_sandbox);			
		}
	}
	
	private boolean isRepositoryExisted(Repository repository)
	{
		boolean boolRtn = false;
		List repList = CVSConfigureHelper.getRepositoryList();
		if ( repList != null && repList.size() > 0 )
		{
			for (int i=0; i< repList.size(); i++){
				Repository rep = (Repository) repList.get(i);
				if ( rep.equals(repository) ) {
					boolRtn = true;
				}
				break;
			}
		}
		
		return boolRtn;
	}
	
	/**
	 * Check if the sandbox folder is existed, otherwise create the folder
	 * @param folderPath for sandbox
	 */
	private boolean checkSandboxIfExist(String folderPath) 
	{
		boolean isCreated = false;
	    try 
	    {
	    	java.io.File filePath = new java.io.File(folderPath);
	    	if (!filePath.exists()) 
	    	{
	    		filePath.mkdirs();
	    	}
	    	isCreated = true;
	    }
	    catch (Exception e) 
	    {
	    	log.error("Create sandbox error");
	    }
	    
	    return isCreated;
	}
	
}
