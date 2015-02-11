package com.globalsight.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

public class MsgDialog extends JDialog
{
	private static Logger log = Logger.getLogger(MsgDialog.class.getName());
	private JTextArea msgText = null;
	private JScrollPane msgPane = null;

	
	public MsgDialog(Frame p_owner)
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
		
		JButton okButton = new JButton("ok");
		okButton.setSize(14, 4);
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = c.weighty = 0.0;
		getContentPane().add(okButton, c);
		
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(msgText.getText().indexOf(MainFrame.START_FIREFOX_JSSH) != -1)
				{
					String [] args = {"/Applications/Firefox.app/Contents/MacOS/firefox-bin","-jssh"};
					try
					{
						Runtime.getRuntime().exec(args);
					}
					catch (IOException e1)
					{
						log.info("Unable to start firefox with jssh with : " + e1);
					}
				}
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
