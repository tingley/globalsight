package com.globalsight.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import com.globalsight.entity.User;
import com.globalsight.util.Constants;
import com.globalsight.util.UsefulTools;
import com.globalsight.util2.ConfigureHelperV2;
import com.globalsight.util2.FileUtils;
import com.globalsight.util2.HtmlUtils;

public class UsersDialog extends JDialog
{
	private static final long serialVersionUID = 13579L;

	static Logger log = Logger.getLogger(UsersDialog.class.getName());

	private User m_user = null;

	private JList jlist = null;

	private boolean onlyview = false;

	public UsersDialog(Frame p_owner, boolean p_onlyview)
	{
		super(p_owner, "User list ", true);
		this.onlyview = p_onlyview;
		initPanel();
		setSize(new Dimension(400, 250));
		Point p = p_owner.getLocation();

		setLocation(
				(int) p.getX() + (p_owner.getWidth() - this.getWidth()) / 2,
				(int) p.getY() + (p_owner.getHeight() - this.getHeight()) / 2);
	}

	private void initPanel()
	{
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		List users = null;
		try
		{
			users = ConfigureHelperV2.readAllUsers();
		}
		catch (Exception e1)
		{
			log.error("Error when reading all users in "
					+ Constants.CONFIGURE_XML, e1);
		}

		if (users == null || users.isEmpty())
		{
			jlist = new JList(new String[] { "No user in configure file" });
		}
		else
		{
			User[] us = new User[users.size()];
			int i = 0;
			for (Iterator iter = users.iterator(); iter.hasNext(); i++)
			{
				User u = (User) iter.next();
				us[i] = u;
			}
			jlist = new JList(us);
		}
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane sp = new JScrollPane(jlist);
		contentPane.add(sp, BorderLayout.CENTER);

		JPanel p = new JPanel();
		JButton okButton = new JButton("Select User");
		okButton.setSize(14, 4);
		JButton viewButton = new JButton("View User");
		viewButton.setSize(14, 4);
		if (!onlyview) p.add(okButton);
		p.add(viewButton);
		contentPane.add(p, BorderLayout.SOUTH);

		jlist.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				boolean isLeft = MouseEvent.BUTTON1 == e.getButton();
				boolean isSelected = jlist.getSelectedIndex() != -1;
				int count = e.getClickCount();
				if (isLeft && count == 2 && isSelected)
				{
					Object obj = jlist.getSelectedValue();
					if (obj instanceof User)
					{
						m_user = (User) obj;
					}
					setVisible(false);
				}
			}
		});

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object obj = jlist.getSelectedValue();
				if (obj instanceof User)
				{
					m_user = (User) obj;
				}
				setVisible(false);
			}
		});

		viewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object obj = jlist.getSelectedValue();
				if (obj instanceof User)
				{
					showUserInfo((User) obj);
				}
			}
		});
	}

	public User getSelectUser()
	{
		return m_user;
	}

	public static void showUserInfo(User p_user)
	{
		try
		{
			String content = HtmlUtils.getUserInforInHTML(p_user);

			File html = File.createTempFile("GSUserInfo", ".html");
			FileUtils.write(html, content, "UTF-8");
			UsefulTools.openFile(html);
		}
		catch (Exception e)
		{
			log.error("Error occurred when show info of " + p_user, e);
		}
	}
}
