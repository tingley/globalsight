package com.globalsight.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.globalsight.action.Action;
import com.globalsight.action.TestServerAction;
import com.globalsight.entity.Host;
import com.globalsight.entity.User;
import com.globalsight.exception.NotSupportHttpsException;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.UsefulTools;
import com.globalsight.util.ValidationHelper;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;

public class UserOptionsPanel extends JPanel
{
	private static final long serialVersionUID = 1768786458546L;
	private String oldPort = "";

	public UserOptionsPanel()
	{
		initPanel();
		action();
		initFields();
		showUserInfo();
	}
	
	private JLabel getLineLabel(String content)
	{
	    return new JLabel(content, SwingConstants.LEFT){
            public void paint(Graphics g)
            {
                super.paint(g);
                g.setColor(new Color(142,195,235));
                g.drawLine(140,15,300,15);
                for (int i = 0; i <=10; i++)
                {
                    g.setColor(new Color(145 + 11*i,195 + 6*i,235 + 2*i));
                    g.drawLine(300 + i*10,15,320 + i*10,15);
                }
            }
        };
	}

	private void initPanel()
	{
		Container cPane = this;
		cPane.setLayout(new BorderLayout());

		// west : menu list
		Box west = Box.createVerticalBox();
		west.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		cPane.add(west, BorderLayout.WEST);
		west.add(Box.createVerticalStrut(20));
		m_menuList = new JList(m_menus);
		Color c = cPane.getBackground();
		m_menuList.setBackground(c);
		m_menuList.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		m_menuList.putClientProperty("JTree.lineStyle", "Horizontal");
		JScrollPane treeView = new JScrollPane(m_menuList);
		west.add(treeView);

		// center : host name directory minutes
		JPanel center = new JPanel();
//		center.setBackground(new Color(240,240,240));
		center.setLayout(new BorderLayout());
		cPane.add(center, BorderLayout.CENTER);

		centerContainer = Box.createVerticalBox();
		center.add(centerContainer, BorderLayout.CENTER);
		centerContainer.setBorder(BorderFactory.createEtchedBorder());
		// Hostname and port
		int h = 35;//50
		box_host = Box.createVerticalBox();
		Box hostname = Box.createHorizontalBox();//Hostname
		JLabel label1 = new JLabel(hostNameLableName, SwingConstants.CENTER);
		label1.setMaximumSize(new Dimension(100, h));
		hostname.add(label1);
		m_hostField = new JTextField();
		m_hostField.setToolTipText("For example, globalsight01.welocalize.com ");
		m_hostField.setMaximumSize(new Dimension(200, h));
		hostname.add(m_hostField);
		Box hostport = Box.createHorizontalBox();//HostPort
		JLabel label2 = new JLabel(portLableName, SwingConstants.CENTER);
		label2.setMaximumSize(new Dimension(100, h));
		hostport.add(label2);
		m_portField = new JTextField();
		m_portField.setToolTipText("positive number, like 80");
		m_portField.setMaximumSize(new Dimension(200, h));
		hostport.add(m_portField);
		
		Box hostssl = Box.createHorizontalBox();
        JLabel labelSSL = new JLabel(useSSLLableName, SwingConstants.CENTER);
        labelSSL.setMaximumSize(new Dimension(100, h));
        hostssl.add(labelSSL);
        m_useSSL = new JCheckBox();
        m_useSSL.setToolTipText("");
        hostssl.add(m_useSSL);
        hostssl.add(Box.createHorizontalStrut(100));
        //hostssl.setBorder(BorderFactory.createEtchedBorder());
        hostssl.setMaximumSize(new Dimension(300, h + 5));

        Box lineBox = Box.createHorizontalBox();
        lineBox.setMaximumSize(new Dimension(400, h));
        JLabel l = getLineLabel("    Network Setting");
        l.setMaximumSize(new Dimension(400, 100));
        lineBox.add(l);
        box_host.add(lineBox);
        
        box_host.add(hostname);
        box_host.add(hostssl);
        box_host.add(hostport);
        box_host.add(Box.createVerticalStrut(10));
        
        Box lineBox2 = Box.createHorizontalBox();
        lineBox.setMaximumSize(new Dimension(400, h));
        JLabel l2 = getLineLabel("Account Information");
        l2.setMaximumSize(new Dimension(400, 30));
        lineBox2.add(l2);
        box_host.add(lineBox2);

		// username and password
		box_user = Box.createVerticalBox();
		Box username = Box.createHorizontalBox();
		JLabel label3 = new JLabel(userNameLableName, SwingConstants.CENTER);
		label3.setMaximumSize(new Dimension(100, h));
		username.add(label3);
		m_usernameField = new JTextField();
		m_usernameField.setMaximumSize(new Dimension(200, h));
		username.add(m_usernameField);
		Box userpwd = Box.createHorizontalBox();
		JLabel label4 = new JLabel(passwordLableName, SwingConstants.CENTER);
		label4.setMaximumSize(new Dimension(100, h));
		userpwd.add(label4);
		m_passwordField = new JPasswordField();
		m_passwordField.setMaximumSize(new Dimension(200, h));
		userpwd.add(m_passwordField);
		
		box_user.add(username);
		box_user.add(Box.createVerticalStrut(10));
		box_user.add(userpwd);

		// minutes
		box_minutes = Box.createHorizontalBox();
		box_minutes.add(Box.createHorizontalStrut(20));
		JLabel label5 = new JLabel(disabledown, SwingConstants.CENTER);
		label5.setMaximumSize(new Dimension(200, h));
		box_minutes.add(label5);
		m_disableDown = new JCheckBox();
		box_minutes.add(m_disableDown);
		box_minutes.add(Box.createHorizontalStrut(20));
		JLabel label6 = new JLabel(minutesLableName);
		label6.setMaximumSize(new Dimension(220, h));
		box_minutes.add(label6);
		box_minutes.add(Box.createHorizontalStrut(5));
		m_minutesField = new JTextField(15);
		m_minutesField.setMaximumSize(new Dimension(375,h));
		m_minutesField.setMinimumSize(new Dimension(220,h));
		box_minutes.add(m_minutesField);
		box_minutes.add(Box.createHorizontalStrut(20));

		// directory
		box_dir = Box.createHorizontalBox();
		JLabel label7 = new JLabel(savepathLableName);
		label7.setMaximumSize(new Dimension(150, h));
		box_dir.add(label7);
		m_directoryField = new JTextField();
		m_directoryField.setEditable(false);
		m_directoryField.setMaximumSize(new Dimension(375, h));
//		m_directoryField.setPreferredSize(new Dimension(250, h));
		box_dir.add(m_directoryField);
		JLabel m_blankLabel = new JLabel(" ");
		m_blankLabel.setMinimumSize(new Dimension(30,h));
		box_dir.add(m_blankLabel);
		m_browseButton = new JButton(browseButtonName);
//		m_browseButton.setMaximumSize(new Dimension(100, h));
		box_dir.add(m_browseButton);
		box_dir.add(m_blankLabel);
		m_viewButton = new JButton(viewButtonName);
//		m_viewButton.setMaximumSize(new Dimension(100, h));
		box_dir.add(m_viewButton);
		box_dir.add(Box.createHorizontalStrut(10));

		// download from list
		box_download = Box.createHorizontalBox();
		Box allUser = Box.createVerticalBox();
		JLabel label_all = new JLabel("Available users");
		Box temp = Box.createHorizontalBox();
		temp.add(label_all);
		allUser.add(temp);
		m_usersModel = new DefaultListModel();
		m_usersList = new JList(m_usersModel);
		JScrollPane jsp1 = new JScrollPane(m_usersList);
		int style_jsp = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
		jsp1.setHorizontalScrollBarPolicy(style_jsp);
		jsp1.setMinimumSize(new Dimension(350, 160));
		jsp1.setMaximumSize(new Dimension(350, 160));
		jsp1.setPreferredSize(new Dimension(350, 160));
		allUser.add(jsp1);
		allUser.add(Box.createHorizontalStrut(200));
		Box buttons = Box.createVerticalBox();
		m_addButton = new JButton("   >>   ");
		m_removeButton = new JButton("   <<   ");
		m_addAllButton = new JButton("Add all");
		buttons.add(Box.createVerticalStrut(40));
		buttons.add(m_addButton);
		buttons.add(Box.createVerticalStrut(20));
		buttons.add(m_removeButton);
		buttons.add(Box.createVerticalStrut(20));
		buttons.add(m_addAllButton);
		buttons.add(Box.createVerticalStrut(40));
		Box downFrom = Box.createVerticalBox();
		JLabel label_down = new JLabel("Download jobs from following users");
		Box temp1 = Box.createHorizontalBox();
		temp1.add(label_down);
		downFrom.add(temp1);
		m_downModel = new DefaultListModel();
		m_downList = new JList(m_downModel);
		JScrollPane jsp2 = new JScrollPane(m_downList);
		jsp2.setHorizontalScrollBarPolicy(style_jsp);
		jsp2.setMinimumSize(new Dimension(200, 160));
		jsp2.setMaximumSize(new Dimension(200, 160));
		jsp2.setPreferredSize(new Dimension(200, 160));
		downFrom.add(jsp2);
		downFrom.add(Box.createHorizontalStrut(200));

		box_download.add(Box.createHorizontalStrut(20));
		box_download.add(allUser);
		box_download.add(Box.createHorizontalStrut(10));
		box_download.add(buttons);
		box_download.add(Box.createHorizontalStrut(10));
		box_download.add(downFrom);
		box_download.add(Box.createHorizontalStrut(20));

		// buttons
		m_logonButton = new JButton(logonButtonName);
//		m_logonButton.setToolTipText("logon with typed user, "
//				+ "and save this user if logging on successfully");
		m_logonButton.setMaximumSize(new Dimension(100, h));
		m_applyButton = new JButton(saveButtonName);
		m_applyButton.setMaximumSize(new Dimension(70, h));
//		m_applyButton.setToolTipText("Check Now");
		m_revertButton = new JButton(resetButtonName);
//		m_resetButton.setToolTipText("resume all the text field...");
		m_revertButton.setMaximumSize(new Dimension(80, h));
		m_selectUserButton = new JButton(selectUserButtonName);
//		m_selectUserButton
//				.setToolTipText("select user in local configure file");
		m_selectUserButton.setMaximumSize(new Dimension(120, h));
		m_testServerButton = new JButton(testButtonName);
//		m_selectUserButton.setToolTipText("test connection to the host below");
		m_testServerButton.setMaximumSize(new Dimension(100, h));
		Box box_button = Box.createHorizontalBox();
		box_button.add(Box.createHorizontalStrut(10));
		box_button.add(m_selectUserButton);
		box_button.add(Box.createHorizontalStrut(5));
		box_button.add(m_testServerButton);
		box_button.add(Box.createHorizontalStrut(30));
		box_button.add(m_logonButton);
		box_button.add(Box.createHorizontalStrut(20));
		box_button.add(m_applyButton);
		box_button.add(Box.createHorizontalStrut(5));
		box_button.add(m_revertButton);
		box_button.add(Box.createHorizontalStrut(5));
		
		Box box_lbCheckNow = Box.createHorizontalBox();
		JLabel lbCheckNow = new JLabel("Note: Click \"Apply\" button to check downloadable jobs.");
		box_lbCheckNow.add(lbCheckNow);

		// south : buttons
		Box south = Box.createVerticalBox();
		south.add(Box.createVerticalStrut(30));
		south.add(box_button);
		south.add(Box.createVerticalStrut(20));
		south.add(box_lbCheckNow);
		south.add(Box.createVerticalStrut(10));
		center.add(south, BorderLayout.SOUTH);
	}

	private void initFields()
	{
		User u = CacheUtil.getInstance().getCurrentUser();
		try
		{
			if (u == null) u = ConfigureHelperV2.readDefaultUser();
		}
		catch (Exception e)
		{
			log.error("error when reading default user", e);
		}
		setFields(u);
		color_oriColor = m_usernameField.getBackground();
	}

	private void action()
	{
		// menu tree action
		m_menuList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				Object obj = m_menuList.getSelectedValue();
				if (m_menus[0].equals(obj.toString()))
				{
					showUserInfo();
				}
				else if (m_menus[1].equals(obj.toString()))
				{
					showDownloadInfo();
				}
				else
				{
					showUserInfo();
				}
			}
		});
		// select user
		m_selectUserButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				UsersDialog ud = new UsersDialog(SwingHelper.getMainFrame(),
						false);
				ud.setVisible(true);
				User u = ud.getSelectUser();
				if (u != null)
				{
					setFields(u);
				}
			}
		});

		// test server
		m_testServerButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean isValid = checkHostFields();

				if (isValid)
				{
					String hostname = getHostName();
					String port = m_portField.getText();
					String useSSL = "" + m_useSSL.isSelected();
					TestServerAction testServerAction = new TestServerAction();
					try
					{
						String result = testServerAction
								.execute(new String[] { hostname, port, useSSL });
						if (Action.restartDI.equals(result))
						{
						    AmbOptionPane.showMessageDialog(Constants.INSTALLCERT_RESTART,
                                    "Warning", JOptionPane.WARNING_MESSAGE);
						}
						else
						{
    						AmbOptionPane.showMessageDialog(successTestingMessage,
    								"Success", JOptionPane.INFORMATION_MESSAGE);
    						log.info("Connect to server successfully. " + hostname
    								+ ":" + port);
						}
					}
					catch (NotSupportHttpsException e1)
                    {
                        AmbOptionPane.showMessageDialog(notHttpsForTest,
                                "Info", JOptionPane.INFORMATION_MESSAGE);
                        m_useSSL.setSelected(false);
                        log.warn(notHttpsForTest);
                    }
					catch (Exception e1)
					{
						AmbOptionPane.showMessageDialog(failTestingMessage,
								"Warning", JOptionPane.ERROR_MESSAGE);
						log.warn("Can not connect to server.\n" + e1);
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog(Constants.ERROR_FIELDS,
							"Warning", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// disable autodown
		m_disableDown.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean enable = !m_disableDown.isSelected();
				setDownloadRelatedEnable(enable);
			}
		});

		// add selected users to download list button
		m_addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int[] indices = m_usersList.getSelectedIndices();
				for (int i = indices.length - 1; i > -1; i--)
				{
					int index = indices[i];
					Object object = m_usersModel.get(index);
					if (object instanceof User)
					{
						moveElementToList(m_usersModel, m_downModel, index);
					}
				}
			}
		});

		// add all users to download list button
		m_addAllButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int size = m_usersModel.getSize();
				for (int i = size - 1; i > -1; i--)
				{
					Object object = m_usersModel.get(i);
					if (object instanceof User)
					{
						moveElementToList(m_usersModel, m_downModel, i);
					}
				}
			}
		});

		// remove selected user from download list button
		m_removeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int[] indices = m_downList.getSelectedIndices();
				for (int i = indices.length - 1; i > -1; i--)
				{
					int index = indices[i];
					Object object = m_downModel.get(index);
					if (object instanceof User)
					{
						moveElementToList(m_downModel, m_usersModel, index);
					}
				}
			}
		});

		// reset Account
		m_revertButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetFields();
			}
		});

		m_browseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String fileName = browseButtonAction();
				m_directoryField.setText(fileName);
			}
		});

		m_viewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (ValidationHelper.validateEmptyString(m_directoryField
						.getText()))
				{
					File file = new File(m_directoryField.getText());
					if (!file.exists())
					{
						file.mkdirs();
					}
					UsefulTools.openFile(file.getAbsolutePath());
				}
			}
		});

		m_logonButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (checkFields())
				{
					MainFrame rootPane = SwingHelper.getMainFrame();
					if (rootPane != null)
					{
						User user = getUserInPanel();
						boolean logon = rootPane.logon(user);
						if (logon)
						{
							try
							{
								ConfigureHelperV2.writeDefaultUser(user);
								setFields(user);
								AmbOptionPane.showMessageDialog(
										successLogonMessage, "Success",
										JOptionPane.INFORMATION_MESSAGE);
							}
							catch (Exception e1)
							{
								log.error("Error when save user : " + user, e1);
								AmbOptionPane.showMessageDialog(
										failSaveMessage, "Success",
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog(Constants.ERROR_FIELDS,
							"Warning", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		m_applyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (checkFields())
				{
					User user = getUserInPanel();
					try
					{
						List allUser = ConfigureHelperV2.readAllUsers(user
								.getHost());
						if (allUser.contains(user))
						{
							int index = allUser.indexOf(user);
							String cormName = ((User) allUser.get(index))
									.getCompanyName();
							user.setCompanyName(cormName);
						}
						ConfigureHelperV2.writeUser(user);
						User cu = CacheUtil.getInstance().getCurrentUser();
						cu.setMinutes(user.getMinutes());
						cu.setDownloadUsers(user.getDownloadUsers());
						if (user.equals(cu))
						{
							SwingHelper.getMainFrame().restartDownloadThread();
						}
						setFields(user);
						AmbOptionPane.showMessageDialog(successSaveMessage,
								"Success", JOptionPane.INFORMATION_MESSAGE);
					}
					catch (Exception e1)
					{
						log.error("Error when save user : " + user, e1);
						AmbOptionPane.showMessageDialog(failSaveMessage,
								"Success", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					AmbOptionPane.showMessageDialog(Constants.ERROR_FIELDS,
							"Warning", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		initFieldsActions();
	}

	private void initFieldsActions()
	{
		m_usernameField.getDocument().addDocumentListener(
				new DocumentListener()
				{
					public void changedUpdate(DocumentEvent e)
					{
						// do nothing
					}

					public void insertUpdate(DocumentEvent e)
					{
						initNewUser();
					}

					public void removeUpdate(DocumentEvent e)
					{
						initNewUser();
					}
				});

		m_hostField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				// do nothing
			}

			public void insertUpdate(DocumentEvent e)
			{
				initNewUser();
			}

			public void removeUpdate(DocumentEvent e)
			{
				initNewUser();
			}
		});

		m_portField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				// do nothing
			}

			public void insertUpdate(DocumentEvent e)
			{
				initNewUser();
			}

			public void removeUpdate(DocumentEvent e)
			{
				initNewUser();
			}
		});

		m_usernameField.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				m_usernameField.setBackground(color_oriColor);
			}

			public void focusLost(FocusEvent e)
			{
			}
		});

		m_passwordField.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				m_passwordField.setBackground(color_oriColor);
			}

			public void focusLost(FocusEvent e)
			{
			}
		});

		m_hostField.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				m_hostField.setBackground(color_oriColor);
			}

			public void focusLost(FocusEvent e)
			{
			}
		});

		m_portField.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				m_portField.setBackground(color_oriColor);
			}

			public void focusLost(FocusEvent e)
			{
			}
		});
		
		m_useSSL.addActionListener(new ActionListener()
		{
            public void actionPerformed(ActionEvent e)
            {
                String port = oldPort;
                oldPort = m_portField.getText();
                m_portField.setText(port);
            }}
		);
	}

	private void initNewUser()
	{
		m_passwordField.setText("");
		m_minutesField.setText(User.defaultMinutes);
		m_directoryField.setText(User.defaultSavePath);
		m_disableDown.setSelected(false);
		m_downModel.clear();
		m_usersModel.clear();
		setDownloadRelatedEnable(true);
	}

	private void moveElementToList(DefaultListModel from, DefaultListModel to,
			int index)
	{
		Object obj = from.get(index);
		from.remove(index);
		if (!to.contains(obj)) to.addElement(obj);
	}

	private boolean checkFields()
	{
		boolean isuserok = true;
		boolean ishostok = checkHostFields();
		if (!ValidationHelper.validateEmptyString(m_usernameField.getText()))
		{
			m_usernameField.setBackground(color_noticeColor);
			isuserok = false;
		}
		if (!ValidationHelper.validateEmptyString(getPassword()))
		{
			m_passwordField.setBackground(color_noticeColor);
			isuserok = false;
		}

		if (ishostok && !isuserok)
		{
			m_menuList.setSelectedIndex(0);
		}

		return ishostok & isuserok;
	}

	private boolean checkHostFields()
	{
		boolean ishostok = true;
		if (!ValidationHelper.validateHostName(m_hostField.getText()))
		{
			m_hostField.setBackground(color_noticeColor);
			ishostok = false;
		}
		if (!ValidationHelper.validatePositiveNumber(m_portField.getText()))
		{
			m_portField.setBackground(color_noticeColor);
			ishostok = false;
		}

		if (!ishostok)
		{
			m_menuList.setSelectedIndex(0);
		}

		return ishostok;
	}

	private void showUserInfo()
	{
		centerContainer.removeAll();
		centerContainer.add(Box.createVerticalStrut(20));
		centerContainer.add(box_host);
		centerContainer.add(Box.createVerticalStrut(10));
		centerContainer.add(box_user);
		centerContainer.add(Box.createVerticalStrut(30));
		centerContainer.repaint();
		centerContainer.validate();
	}

	private void showDownloadInfo()
	{
		centerContainer.removeAll();
		centerContainer.add(Box.createVerticalStrut(10));
		centerContainer.add(box_dir);
		centerContainer.add(Box.createVerticalStrut(5));
		centerContainer.add(box_minutes);
		centerContainer.add(Box.createVerticalStrut(20));
		centerContainer.add(box_download);
		centerContainer.repaint();
		centerContainer.validate();
	}

	private String getHostName()
	{
		String hostName = m_hostField.getText().trim().toLowerCase();
		// remove http:// or https://
		String http_prefix = "http://";
		if (hostName.startsWith(http_prefix))
		{
			hostName = hostName.substring(http_prefix.length());
		}
		return hostName;
	}

	private void setFields(User p_user)
	{
		if (p_user != null)
		{
			Host host = p_user.getHost();
			m_hostField.setText(host.getName());
			m_portField.setText(host.getPortString());
			m_usernameField.setText(p_user.getName());
			m_passwordField.setText(p_user.getPassword());
			m_directoryField.setText(p_user.getSavepath());
			m_minutesField.setText(p_user.getMinutes());
			boolean enabled = p_user.isAutoDownload();
			m_disableDown.setSelected(!enabled);
			m_useSSL.setSelected(p_user.isUseSSL());
			setDownloadRelatedEnable(enabled);

			// set download jobs from users list
			m_downModel.removeAllElements();
			m_usersModel.removeAllElements();
			User[] downloadUsers = p_user.getDownloadUsers();
			List allUsers = new ArrayList();
			try
			{
				allUsers = ConfigureHelperV2.readAllUsers(host);
			}
			catch (Exception e)
			{
				log.error("error when read all users from host" + host, e);
			}
			// filter available users list by company name
			String cormName = p_user.getCompanyName();
			List availableUsers = new ArrayList();
			if (p_user.isSuperUser())
			{
				availableUsers = allUsers;
			}
			else
			{
				for (Iterator iter = allUsers.iterator(); iter.hasNext();)
				{
					User user = (User) iter.next();
					if (cormName.equals(user.getCompanyName()))
					{
						availableUsers.add(user);
					}
				}
			}
			// set download jobs from users
			for (int i = 0; i < downloadUsers.length; i++)
			{
				User user = downloadUsers[i];
				if (availableUsers.contains(user))
				{
					availableUsers.remove(user);
					m_downModel.addElement(user);
				}
			}
			// set available user list
			for (Iterator iter = availableUsers.iterator(); iter.hasNext();)
			{
				User user = (User) iter.next();
				m_usersModel.addElement(user);
			}
		}
		else
		{
			m_hostField.setText("");
			m_portField.setText("");
			m_usernameField.setText("");
			m_passwordField.setText("");
			m_directoryField.setText(User.defaultSavePath);
			m_minutesField.setText(User.defaultMinutes);
			m_disableDown.setSelected(false);
			m_useSSL.setSelected(false);
			setDownloadRelatedEnable(true);
			m_downModel.removeAllElements();
			m_usersModel.removeAllElements();
		}
	}

	/**
     * enable or disenable the Components related to download
     * 
     * @param enabled
     */
	private void setDownloadRelatedEnable(boolean enabled)
	{
		m_minutesField.setEnabled(enabled);
		m_usersList.setEnabled(enabled);
		m_downList.setEnabled(enabled);
		m_addButton.setEnabled(enabled);
		m_addAllButton.setEnabled(enabled);
		m_removeButton.setEnabled(enabled);
	}

	private User getUserInPanel()
	{
		int port = Integer.parseInt(m_portField.getText());
		Host h = new Host(getHostName(), port);
		String name = m_usernameField.getText();
		String pwd = getPassword();
		String dir = m_directoryField.getText();
		String minutes = m_minutesField.getText();
		boolean autodown = !m_disableDown.isSelected();
		int size = m_downModel.getSize();
		User[] users = new User[size];
		for (int i = 0; i < users.length; i++)
		{
			users[i] = (User) m_downModel.get(i);
		}

		if ("".equals(dir.trim()))
		{
			dir = User.defaultSavePath;
			m_directoryField.setText(dir);
		}
		if (!ValidationHelper.validatePositiveNumber(minutes.trim()))
		{
			minutes = User.defaultMinutes;
			m_minutesField.setText(minutes);
		}
		else
		{
			int m = Integer.parseInt(minutes);
			if (m < User.defaultMs)
			{
				minutes = User.defaultMinutes;
				m_minutesField.setText(minutes);
			}
		}

		User u = new User(name, pwd, h, dir, minutes, autodown);
		u.setDownloadUsers(users);
		u.setUseSSL(m_useSSL.isSelected());

		return u;
	}

	private String getPassword()
	{
		char[] password_array = m_passwordField.getPassword();
		String password = new String(password_array);
		return password;
	}

	private String browseButtonAction()
	{
		JFileChooser fileChooser = new JFileChooser(m_directoryField.getText());
		fileChooser.setDialogTitle("Select Directory For Downloading");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int status = fileChooser.showOpenDialog(SwingHelper.getMainFrame());
		if (status == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fileChooser.getSelectedFile();
			return selectedFile.getPath();
		}
		else
		{
			return m_directoryField.getText();
		}
	}

	private void resetFields()
	{
		User user = null;
		try
		{
			user = ConfigureHelperV2.readDefaultUser();
		}
		catch (Exception e)
		{
			log.error("error when reading default user, "
					+ "UserOptionsPanel:resetFields()", e);
		}
		setFields(user);

		m_hostField.setBackground(color_oriColor);
		m_portField.setBackground(color_oriColor);
		m_usernameField.setBackground(color_oriColor);
		m_passwordField.setBackground(color_oriColor);
	}

	static Logger log = Logger.getLogger(UserOptionsPanel.class.getName());

	public static String notHttpsForLogin = "Failed to connect with https. Use http to continue?";
	
	private static String notHttpsForTest = "Https connection is not availabe. Using Http as the connection.";
	
	private static String successTestingMessage = "Connect to the server successfully";

	private static String successLogonMessage = "Logon and save configure successfully";

	private static String successSaveMessage = "Save configure successfully";

	private static String failSaveMessage = "Logon successfully, but save configure unsuccessfully. \nPlease view log";

	private static String failTestingMessage = "Failed to connect to server.";

	private static String hostNameLableName = "Host Name";

	private static String portLableName = "Host Port";
	
	private static String useSSLLableName = "Use https";

	private static String userNameLableName = "User Name";

	private static String passwordLableName = "Password";

	private static String selectUserButtonName = "Select user";

	private static String savepathLableName = "Downloaded Directory";

	private static String minutesLableName = "Automatic Downloading Minutes";

	private static String disabledown = "Disable Downloads";

	private static String logonButtonName = "Logon";

	private static String saveButtonName = "Apply";

	private static String resetButtonName = "Revert";

	private static String testButtonName = "Test Host";

	private static String viewButtonName = "View";

	private static String browseButtonName = "Browse...";

	private JTextField m_directoryField, m_minutesField;

	private JTextField m_hostField, m_portField;

	private JButton m_selectUserButton, m_testServerButton, m_viewButton,
			m_browseButton;

	private JTextField m_usernameField;

	private JPasswordField m_passwordField;

	private JButton m_revertButton, m_logonButton, m_applyButton;

	private JCheckBox m_disableDown, m_useSSL;

	private JList m_menuList;

	private JList m_usersList, m_downList;

	private DefaultListModel m_usersModel, m_downModel;

	private JButton m_addButton, m_removeButton, m_addAllButton;

	private String[] m_menus = { "  User Info  ", "  Download  " };

	private Box centerContainer, box_dir, box_host, box_user, box_minutes,
			box_download;

	private Color color_oriColor = null;

	private Color color_noticeColor = new Color(200, 255, 200);
}
