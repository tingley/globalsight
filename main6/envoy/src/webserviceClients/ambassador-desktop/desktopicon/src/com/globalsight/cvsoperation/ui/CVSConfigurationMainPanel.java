package com.globalsight.cvsoperation.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class CVSConfigurationMainPanel extends JDialog 
{
	private static final long serialVersionUID = -3111705851209401554L;
	
	private JTabbedPane jtp = new JTabbedPane();
	
	private Container contentPane = this;
	
	private String protocol = CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL);
	private String username = CVSConfigureHelper.getProperty(Constants.CVS_USERNAME);
	private String password = CVSConfigureHelper.getProperty(Constants.CVS_PASSWORD);

	public CVSConfigurationMainPanel(Frame p_owner, boolean modal)
	{
		super(p_owner, "CVS Configuration");
		this.setResizable(false);
		this.setSize(600, 400);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		jtp.addTab("Authentication", new AuthenticationPanel(CVSConfigurationMainPanel.this));
		jtp.addTab("Repositories", new RepositoryPanel(CVSConfigurationMainPanel.this));
		if ( protocol != null && username != null && password != null ){
			jtp.setSelectedComponent(jtp.getComponent(1));
		}
		contentPane.add(jtp);
		addListeners();
	}
	
	private void addListeners()
	{
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
//				if (AmbOptionPane.showConfirmDialog("Exit CVS Configurations? ", "Exit",
//						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
//				{
					CVSConfigurationMainPanel.this.dispose();
//				}
//				else
//				{
//					setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//				}
			}
		});
	}
}
