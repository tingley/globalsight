package com.globalsight.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import com.globalsight.util.ConfigureHelper;

public class DependenceMsgDialog extends JDialog
{
	private static Logger log = Logger.getLogger(DependenceMsgDialog.class.getName());
	private JTextArea msgText = null;
	private JScrollPane msgPane = null;

	
	public DependenceMsgDialog(Frame p_owner)
	{
		super(p_owner, "Message: ", true);
		init();
	}

	private void init()
	{
		initMsgBox();
		setSize(400, 300);
		setResizable(true);
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 4;
		c.weightx = c.weighty = 1.0;
		getContentPane().add(msgPane, c);
		
		JButton okButton = new JButton("Ok, Do Check next time.");
		okButton.setSize(14, 4);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = c.weighty = 0.0;
		getContentPane().add(okButton, c);
		
		JButton noCheckButton = new JButton("I know, and Do not Check again.");
		okButton.setSize(14, 4);
		c.gridx = 0;
		c.gridy = 6;
		getContentPane().add(noCheckButton, c);
		
		getRootPane().setDefaultButton(okButton);
		
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		noCheckButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ConfigureHelper.turnOffCheckDependence();
				setVisible(false);
			}
		});
	}

	private void initMsgBox()
	{
		msgText = new JTextArea();
		msgText.setEditable(false);
		msgText.setWrapStyleWord(true);
		msgText.setText("Please waiting...");
		
		msgPane = new JScrollPane(msgText);
	}
	
	public void setMsg(String p_msg)
	{
		msgText.setText(p_msg);
	}
	
}
