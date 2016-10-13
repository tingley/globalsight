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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.*;

public class CVSFileRenameDialog extends JDialog {

	private static final long serialVersionUID = 5561414013314834461L;
	
	static Logger log = Logger.getLogger(CVSTargetModuleDialog.class.getName());
	
	private JLabel jl1, jl2, jl3; 
	
	private JTextField jtf1, jtf2;
	
	private JComboBox jcb1;
	
	private JCheckBox jcbFileRename;
	
	private JTable jtb;
	
	private JButton selectBtn, addBtn, editBtn, removeBtn, saveBtn, cancelBtn;
	
	private Container content = this;

	private	GridBagConstraints c1 = new GridBagConstraints();

	private boolean isOKReturn = false, isAdd = true;
	
	private String sourceFilename, targetFilename;
	
	public CVSFileRenameDialog(Frame p_owner, boolean modal)
	{
		super(p_owner);
		this.setResizable(false);
		this.setSize(400, 200);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		init();
		initAction();
	}
	
	private void init() {
		content.setLayout(new GridBagLayout());
		c1.insets = new Insets(2, 2, 2, 2);
		
		jcbFileRename = new JCheckBox("User PERL RegEx");
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.anchor = GridBagConstraints.WEST;
		content.add(jcbFileRename, c1);
		
		jl1 = new JLabel("Available Files:");
		c1.gridx = 0;
		c1.gridy = 1;
		c1.gridwidth = 1;
		content.add(jl1, c1);
		
		jtf1 = new JTextField(20);
		c1.gridx = 0;
		c1.gridy = 2;
		c1.gridwidth = 2;
		c1.anchor = GridBagConstraints.NORTHWEST;
		content.add(jtf1, c1);
		
		jl2 = new JLabel("Rename Target File As:");
		c1.gridx = 0;
		c1.gridy = 3;
		c1.gridwidth = 1;
		content.add(jl2, c1);
		
		jtf2 = new JTextField(20);
		c1.gridx = 0;
		c1.gridy = 4;
		c1.gridwidth = 2;
		c1.anchor = GridBagConstraints.NORTHWEST;
		content.add(jtf2, c1);

		saveBtn = new JButton("Save");
		saveBtn.setPreferredSize(new Dimension(100,30));
		c1.gridx = 0;
		c1.gridy = 5;
		c1.gridwidth = 1;
		content.add(saveBtn, c1);
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.setPreferredSize(new Dimension(100,30));
		c1.gridx = 1;
		c1.gridy = 5;
		content.add(cancelBtn, c1);
		
	}
	
	private void initAction() {
		jcbFileRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcbFileRename.isSelected()) {
					jl1.setText(" ");
					jl2.setText(" ");
					jtf2.setVisible(false);
				} else {
					jl1.setText("Available Files:");
					jl2.setText("Rename Target File As:");
					jtf2.setVisible(true);
				}
			}
		});
		
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!jcbFileRename.isSelected()) {
					if (StringUtil.isEmpty(jtf1.getText())) { 
						AmbOptionPane.showMessageDialog("Please input 'Available Files' field", 
								"Warning", JOptionPane.WARNING_MESSAGE);
						return;
					} else
						setSourceFilename(jtf1.getText().trim());
					if (StringUtil.isEmpty(jtf2.getText())) {
						AmbOptionPane.showMessageDialog("Please input 'Rename Target File As' field", 
								"Warning", JOptionPane.WARNING_MESSAGE);
						return;
					} else 
						setTargetFilename(jtf2.getText().trim());
				} else {
					if (StringUtil.isEmpty(jtf1.getText())) {
						AmbOptionPane.showMessageDialog("Please input vaild RegEx", 
								"Warning", JOptionPane.WARNING_MESSAGE);
						return;
					} else {
						setSourceFilename(jtf1.getText().trim());
						setTargetFilename(jtf1.getText().trim());
					}
				}
				isOKReturn = true;
				CVSFileRenameDialog.this.dispose();
			}
		});
		
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AmbOptionPane.showConfirmDialog("Are you sure want to cancel?", "Info", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					isOKReturn = false;
					CVSFileRenameDialog.this.dispose();
				}
			}
		});

	}
	
	public String getSourceFilename() {
		return sourceFilename;
	}

	public void setSourceFilename(String sourceFilename) {
		this.sourceFilename = sourceFilename;
	}

	public String getTargetFilename() {
		return targetFilename;
	}

	public void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}
	
	public boolean isOKReturn() {
		return this.isOKReturn;
	}

	public boolean isAdd() {
		return isAdd;
	}

	public void setAdd(boolean isAdd) {
		this.isAdd = isAdd;
	}
}
