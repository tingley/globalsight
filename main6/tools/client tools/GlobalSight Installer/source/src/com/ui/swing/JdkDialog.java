/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.config.properties.InstallValues;
import com.config.properties.Resource;
import com.util.UIUtil;

/**
 * A dialog that let user specify the path of GlobalSight server. <br>
 * If no path is specified, system will exit.
 * 
 */
public class JdkDialog extends JDialog {
	private static final long serialVersionUID = -2158159981016286935L;

	private JTextField pathField = new JTextField();
	private JButton cancelButton = new JButton(Resource.get("button.cancel"));
	private JButton tryButton = new JButton(Resource.get("button.select"));

	private static final int DIALOG_WIDTH = 470;
	private static final int DIALOG_HEIGHT = 250;
	private JTextField textField;
	JFileChooser jfc = new JFileChooser();

	/**
	 * Init.
	 */
	public JdkDialog() {
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		setLocation();
		setResizable(false);
		setTitle(Resource.get("title.specifyServerPath"));

		initUI();
		initAction();

		this.setModal(true);
		this.setVisible(true);
	}

	/**
	 * Init the UI.
	 */
	private void initUI() {
		getContentPane().setLayout(null);
		
		JTextArea test = new JTextArea(Resource.get("msg.jdk"));
        test.setSize(DIALOG_WIDTH - 50, 70);
        test.setLocation(20, 20);
        test.setBackground(this.getBackground());
        test.setEditable(false);
        test.setLineWrap(true);
        test.setFont(UIUtil.getFrameFont());
        this.add(test);

		cancelButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
		cancelButton.setLocation(270,
				178);
		getContentPane().add(cancelButton);

		tryButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
		tryButton.setLocation(360,
				178);
		getContentPane().add(tryButton);

		JLabel lblNewLabel = new JLabel(Resource.get("lb.jdk.home") + ":");
		lblNewLabel.setBounds(20, 94, 280, 15);
		getContentPane().add(lblNewLabel);

		textField = new JTextField();
		textField.setBounds(20, 121, 320, 21);
		getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnNewButton = new JButton(Resource.get("button.browser"));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jfc.setFileSelectionMode(1);
				int state = jfc.showOpenDialog(null);
				if (state == 1) {
					return;
				} else {
					File f = jfc.getSelectedFile();
					String path = f.getAbsolutePath();
					textField.setText(path);
				}

			}
		});
		btnNewButton.setBounds(347, 120, 93, 23);
		getContentPane().add(btnNewButton);
	}

	private void setLocation() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Point location = new Point((screen.width - DIALOG_WIDTH) / 2,
				(screen.height - DIALOG_HEIGHT) / 2);

		setLocation(location);
	}

	public String getServerPath() {
		return pathField.getText();
	}

	private void initAction() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});

		tryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String javaHome = textField.getText().trim();
				if (new File(javaHome + "/bin").exists())
				{
					InstallValues.setJavaHome(javaHome);
					setVisible(false);
				}
				else
				{
					JOptionPane.showMessageDialog(null, Resource.get("msg.jdk.home.wrong"), Resource
			                .get("title.mainFrame"), JOptionPane.INFORMATION_MESSAGE);
				}
				
			}
		});

	}

	private void exit() {
		if (JOptionPane.showConfirmDialog(this, Resource.get("confirm.exit"),
				Resource.get("title.exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			System.exit(0);
		} else {
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
	}

	public static void main(String[] args) {
		UIUtil.setLookAndFeel();
		new JdkDialog();
	}
}
