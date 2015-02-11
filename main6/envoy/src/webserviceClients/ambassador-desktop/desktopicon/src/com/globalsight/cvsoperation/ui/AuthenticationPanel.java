package com.globalsight.cvsoperation.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;

public class AuthenticationPanel extends JPanel 
{
	private static final long serialVersionUID = -1379512273465316532L;
	
	private CVSConfigurationMainPanel parent = null; 

	private JLabel lb1, lb2, lb3;
	
	private JComboBox jcb1;
	
	private JTextField jtf;
	
	private JPasswordField jtfPwd;
	
	private JButton jbSave, jbClose;
	
    public AuthenticationPanel()
    {
    	init();
    	addActions();
    }
    
    public AuthenticationPanel(CVSConfigurationMainPanel parent)
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
		//Protocol
		lb1 = new JLabel("Protocol:", SwingConstants.LEFT);
		lb1.setMaximumSize(new Dimension(100,30));
		lb1.setMinimumSize(new Dimension(100,30));
		lb1.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb1, c);
		//
//		String[] protocols = {"Secure Shell(:ext:)", "Password Server(:pserver:)"};
		String[] protocols = {"EXT", "PSERVER"};
		jcb1 = new JComboBox(protocols);
		String protocol = CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL);
		if ( protocol != null )
		{
			jcb1.setSelectedItem(protocol);			
		}
		jcb1.setMaximumSize(new Dimension(250,30));
		jcb1.setMinimumSize(new Dimension(250,30));
		jcb1.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 200);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jcb1, c);
		//Username
		lb2 = new JLabel("Username:", SwingConstants.LEFT);
		lb2.setMaximumSize(new Dimension(100,30));
		lb2.setMinimumSize(new Dimension(100,30));
		lb2.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb2, c);
		//
		jtf = new JTextField();
		String username = CVSConfigureHelper.getProperty(Constants.CVS_USERNAME);
		if ( username != null )
		{
			jtf.setText(username);
		}
		jtf.setMaximumSize(new Dimension(250,30));
		jtf.setMinimumSize(new Dimension(250,30));
		jtf.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 200);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtf, c);
		//Password
		lb3 = new JLabel("Password:", SwingConstants.LEFT);
		lb3.setMaximumSize(new Dimension(100,30));
		lb3.setMinimumSize(new Dimension(100,30));
		lb3.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(lb3, c);
		//
		jtfPwd = new JPasswordField();
		String password = CVSConfigureHelper.getProperty(Constants.CVS_PASSWORD);
		if ( password != null )
		{
			jtfPwd.setText(password);
		}
		jtfPwd.setMaximumSize(new Dimension(250,30));
		jtfPwd.setMinimumSize(new Dimension(250,30));
		jtfPwd.setPreferredSize(new Dimension(250,30));
		c.insets = new Insets(5, 2, 5, 200);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jtfPwd, c);
		
		jbSave = new JButton("Save");
		jbSave.setMaximumSize(new Dimension(100,30));
		jbSave.setMinimumSize(new Dimension(100,30));
		jbSave.setPreferredSize(new Dimension(100,30));
		c.insets = new Insets(15, 2, 140, 3);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jbSave, c);
		//Cancel btn
		jbClose = new JButton("Close");
		jbClose.setMaximumSize(new Dimension(100,30));
		jbClose.setMinimumSize(new Dimension(100,30));
		jbClose.setPreferredSize(new Dimension(100,30));
		jbClose.setVisible(true);
		c.insets = new Insets(15, 2, 140, 300);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		contentPane.add(jbClose, c);
		
    }
    
    private void addActions()
    {
    	jbSave.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e)
    		{
    			String selectedProtocol = ((String) jcb1.getSelectedItem()).trim();
    			String username = jtf.getText().trim();
    			String pwd = new String(jtfPwd.getPassword());
    			
    			if ( selectedProtocol != null && !"".equals(selectedProtocol.trim()) &&
    				 username != null && !"".equals(username.trim()) &&
    				 pwd != null && !"".equals(pwd.trim()) )
    			{
    				CVSConfigureHelper.setProperty(Constants.CVS_PROTOCOL, selectedProtocol);
    				CVSConfigureHelper.setProperty(Constants.CVS_USERNAME, username);
    				CVSConfigureHelper.setProperty(Constants.CVS_PASSWORD, pwd);
        			AmbOptionPane.showMessageDialog("Saved Successfully", 
    						"Info", JOptionPane.INFORMATION_MESSAGE);
    			} 
    			else if (selectedProtocol == null || "".equals(selectedProtocol.trim()))
   				{
    				AmbOptionPane.showMessageDialog("CVS protocol can't be null", 
    						"Warning", JOptionPane.WARNING_MESSAGE);
    				jcb1.setFocusable(true);
    			}
    			else if (username == null || "".equals(username.trim()))
    			{
					AmbOptionPane.showMessageDialog("CVS username can't be null", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					jtf.setFocusable(true);
    			}
    			else if (pwd == null || "".equals(pwd.trim()))
    			{
					AmbOptionPane.showMessageDialog("CVS password can't be null", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					jtfPwd.setFocusable(true);
    			}
    		}
    	});
    	
    	jbClose.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e)
    		{
    			//saved info previously
    			String savedProtocol = CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL);
    			String savedUsername = CVSConfigureHelper.getProperty(Constants.CVS_USERNAME);
    			String savedPassword = CVSConfigureHelper.getProperty(Constants.CVS_PASSWORD);
    			//current info in UI
    			String selectedProtocol = ((String) jcb1.getSelectedItem()).trim();
    			String username = jtf.getText().trim();
    			String pwd = new String(jtfPwd.getPassword());
    			
    			if ( (savedProtocol != null && !savedProtocol.equals(selectedProtocol)) ||
    			     (savedUsername != null && !savedUsername.equals(username)) ||
    			     (savedPassword != null && !savedPassword.equals(pwd))	)
    			{
    				if (AmbOptionPane.showConfirmDialog("Authentication information is changed. Exit without saving? ", "Close",
    						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
    				{
    	    			if (parent != null)
    	    			{
    	    				parent.removeAll();
    	    				parent.dispose();
    	    			}
    				}
    			}
    			else 
    			{
	    			if (parent != null)
	    			{
	    				parent.removeAll();
	    				parent.dispose();
	    			}
    			}

    		}
    	});
    }
}
