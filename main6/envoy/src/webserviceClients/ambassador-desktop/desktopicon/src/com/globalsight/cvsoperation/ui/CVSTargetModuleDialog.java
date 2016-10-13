package com.globalsight.cvsoperation.ui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.globalsight.action.QueryAction;
import com.globalsight.cvsoperation.entity.FileRename;
import com.globalsight.cvsoperation.entity.TargetModuleMapping;
import com.globalsight.cvsoperation.util.ModuleMappingHelper;
import com.globalsight.cvsoperation.util.XMLParser;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.StringUtil;
import com.globalsight.util.SwingHelper;

public class CVSTargetModuleDialog extends JDialog {

	private static final long serialVersionUID = -5784018348506811582L;

	static Logger log = Logger.getLogger(CVSTargetModuleDialog.class.getName());
	
	private JLabel jl1, jl2, jl3;
	
	private JTextField jtf1;
	
	private JComboBox jcb1;
	
	private JCheckBox jcbFileRename;
	
	private JTable jtb;
	
	private DefaultTableModel model;
	
	private JButton selectBtn, addBtn, editBtn, removeBtn, saveBtn, cancelBtn;
	
	private Container content = this;
	
	private boolean isOKReturn = false, isAdd = true, isInit = true;
	
	private String[] keys, values;
	
	private String sourceLocale, shortSourceLocale, sourceModule;
	
	private QueryAction queryAction = new QueryAction();
	
	private ArrayList<TargetModuleMapping> targetModules = new ArrayList<TargetModuleMapping>();
	
	private TargetModuleMapping targetModule = new TargetModuleMapping();
	
	public CVSTargetModuleDialog(Frame p_owner, boolean modal)
	{
		super(p_owner);
		this.setResizable(false);
		this.setSize(800, 450);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		init();
		initAction();
	}

	private void init() {
		content.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(2, 2, 2, 2);
		
		//"Select Target Module" button
		selectBtn = new JButton("Select Target Module...");
		selectBtn.setPreferredSize(new Dimension(170,30));
		selectBtn.setToolTipText("Select target module");
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.anchor = GridBagConstraints.WEST;
		content.add(selectBtn, c1);
		
		//"Target file rename" checkbox
		jcbFileRename = new JCheckBox("Target File Rename");
		c1.gridx = 1;
		c1.gridy = 0;
		c1.anchor = GridBagConstraints.WEST;
		content.add(jcbFileRename, c1);
		
		//"Add File Rename" button
		addBtn = new JButton("Add File Rename");
		addBtn.setPreferredSize(new Dimension(160,30));
		addBtn.setToolTipText("Add file rename");
		addBtn.setEnabled(false);
		c1.gridx = 2;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.anchor = GridBagConstraints.EAST;
		content.add(addBtn, c1);

		//"Add File Rename" button
		removeBtn = new JButton("Delete File Rename");
		removeBtn.setPreferredSize(new Dimension(160,30));
		removeBtn.setToolTipText("Delete file rename");
		removeBtn.setEnabled(false);
		c1.gridx = 3;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.anchor = GridBagConstraints.EAST;
		content.add(removeBtn, c1);

		//Target Locales
		jl2 = new JLabel("Target Module: ");
		c1.gridx = 0;
		c1.gridy = 1;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.anchor = GridBagConstraints.WEST;
		content.add(jl2, c1);
		
		jtf1 = new JTextField(20);
		jtf1.setEditable(false);
		c1.gridx = 0;
		c1.gridy = 2;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;
		content.add(jtf1, c1);

		String[] headers = new String[]{"", "Source Filename", "Target Filename", "Locale"};
		String[][] tableData = null;
		model = new DefaultTableModel(tableData, headers) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		jtb = new JTable(model);
		jtb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jtb.getColumnModel().getColumn(0).setMinWidth(0);
		jtb.getColumnModel().getColumn(0).setMaxWidth(0);
		jtb.getColumnModel().getColumn(1).setPreferredWidth(200);
		jtb.getColumnModel().getColumn(1).setMinWidth(200);
		jtb.getColumnModel().getColumn(2).setMinWidth(100);

		JScrollPane jscp = new JScrollPane(jtb);
		jscp.setPreferredSize(new Dimension(400, 300));
		c1.gridx = 1;
		c1.gridy = 1;
		c1.gridwidth = 3;
		c1.gridheight = 4;
		content.add(jscp, c1);
		
		//Associated source locale
		jl3 = new JLabel("Associated Target Locale: ");
		c1.gridx = 0;
		c1.gridy = 3;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		content.add(jl3, c1);
		
		jcb1 = new JComboBox(new DefaultComboBoxModel());
		jcb1.setPreferredSize(new Dimension(20, 20));
		jcb1.setMaximumSize(new Dimension(20, 20));
		c1.gridx = 0;
		c1.gridy = 4;
		c1.gridwidth = 1;
		c1.anchor = GridBagConstraints.NORTHWEST;
		content.add(jcb1, c1);
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(2, 8, 2, 8);
		
		saveBtn = new JButton("Save");
		saveBtn.setPreferredSize(new Dimension(100,30));
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		buttonPanel.add(saveBtn, c2);
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.setPreferredSize(new Dimension(100,30));
		c2.gridx = 1;
		c2.gridy = 0;
		buttonPanel.add(cancelBtn, c2);
		
		c1.gridx = 1;
		c1.gridy = 6;
		content.add(buttonPanel, c1);
	}

	private void initAction() {
		selectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CVSTreeDialog treeDialog = new CVSTreeDialog(SwingHelper.getMainFrame(), true);
				treeDialog.setTitle("Select Target Module");
				treeDialog.setVisible(true);
				if (treeDialog.isOKReturn()) {
					jtf1.setText(treeDialog.getCVSPath());
				}
			}
		});
		jcb1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isInit) {
				}
			}
		});
		jcbFileRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcbFileRename.isSelected()) {
					addBtn.setEnabled(true);
					removeBtn.setEnabled(true);
				} else {
					addBtn.setEnabled(false);
					removeBtn.setEnabled(false);
				}
					
			}
		});
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CVSFileRenameDialog dialog = new CVSFileRenameDialog(SwingHelper.getMainFrame(), true);
				dialog.setTitle("Add File Rename");
				dialog.setVisible(true);
				if (dialog.isOKReturn()) {
					model = (DefaultTableModel)jtb.getModel();
					model.addRow(new Object[]{"", dialog.getSourceFilename(), dialog.getTargetFilename(), getLocale(values[jcb1.getSelectedIndex()])});
				}
			}
		});
		
		removeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jtb.getSelectedRow() == -1) {
					AmbOptionPane.showMessageDialog("Please select one record first", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				model = (DefaultTableModel)jtb.getModel();
				int[] rows = jtb.getSelectedRows();
				for (int i=rows.length-1;i>=0;i--) {
					String id = (String)model.getValueAt(rows[i], 0);
					model.removeRow(rows[i]);
				}
				if (jtb.getRowCount() == 0) {
					jcbFileRename.setSelected(false);
					addBtn.setEnabled(false);
					removeBtn.setEnabled(false);
				}
			}
		});
		
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (StringUtil.isEmpty(jtf1.getText())) {
					AmbOptionPane.showMessageDialog("Please select 'Target Module' field", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (jcb1.getSelectedIndex() == -1) {
					AmbOptionPane.showMessageDialog("Please select 'Target Locale' field", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				String s = (String)jcb1.getSelectedItem();
				TargetModuleMapping t = new TargetModuleMapping();
				t.setFullLocale(s);
				t.setLocale(getLocale(values[jcb1.getSelectedIndex()]));
				t.setModule(jtf1.getText());
				ArrayList<FileRename> files = new ArrayList<FileRename>();
				if (jtb.getRowCount() > 0) {
					FileRename f = null;
					model = (DefaultTableModel)jtb.getModel();
					for (int i = 0; i< jtb.getRowCount(); i++) {
						f = new FileRename();
						f.setSourceFilename((String)model.getValueAt(i, 1));
						f.setTargetFilename((String)model.getValueAt(i, 2));
						
						files.add(f);
					}
				}
				t.setFileRenames(files);
				if (!targetModule.equals(t)) {
					ModuleMappingHelper helper = new ModuleMappingHelper();
					if (helper.isExist(sourceLocale, sourceModule, s, jtf1.getText(), true) || isExist(s, jtf1.getText())) {
						AmbOptionPane.showMessageDialog("The mapping has been existed.", 
								"Warning", JOptionPane.WARNING_MESSAGE);
						jtf1.setText("");
						return;
					}
				}
				targetModule = t;
				isOKReturn = true;
				CVSTargetModuleDialog.this.dispose();
			}
		});

		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AmbOptionPane.showConfirmDialog("Are you sure want to cancel?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					isOKReturn = false;
					CVSTargetModuleDialog.this.dispose();
				}
			} 
		});
	}

	public boolean isOKReturn() {
		return isOKReturn;
	}

	public void setOKReturn(boolean isOKReturn) {
		this.isOKReturn = isOKReturn;
	}

	public boolean isAdd() {
		return isAdd;
	}

	public void setAdd(boolean isAdd) {
		this.isAdd = isAdd;
	}

	public String getSourceLocale() {
		return sourceLocale;
	}

	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
		String xmlReturn;
		ArrayList locales = null;
		try {
			xmlReturn = queryAction.execute(QueryAction.q_getTargetLocales, getLocale(getSourceLocale()));
			if (xmlReturn != null) {
				locales = XMLParser.parseLocales(xmlReturn);
				if (locales != null) {
					ArrayList key = (ArrayList)locales.get(0);
					ArrayList value = (ArrayList)locales.get(1);
					keys = new String[key.size()];
					values = new String[key.size()];
					key.toArray(keys);
					value.toArray(values);
				}
			} else {
				keys = new String[]{""};
				values = new String[]{""};
			}
			jcb1.removeAllItems();
			for (int i = 0; i < values.length; i++) {
				jcb1.addItem(values[i]);
			}
			jcb1.repaint();
			isInit = false;
		} catch (Exception e) {
		}
	}
	
	private String getLocale(String p) {
		if (p!=null)
			return p.substring(p.lastIndexOf("[")+1, p.lastIndexOf("]"));
		else
			return "";
	}
	
	public String getShortSourceLocale() {
		return getLocale(sourceLocale);
	}

	public TargetModuleMapping getTargetModule() {
		return targetModule;
	}

	public void setTargetModule(TargetModuleMapping targetModule) {
		isInit = true;
		this.targetModule = targetModule;
		if (targetModule != null) {
			jtf1.setText(targetModule.getModule());
			jcb1.setSelectedItem(targetModule.getFullLocale());
			ArrayList<FileRename> fileRenames = targetModule.getFileRenames();
			if (fileRenames != null && fileRenames.size() > 0) {
				jcbFileRename.setSelected(true);
				addBtn.setEnabled(true);
				removeBtn.setEnabled(true);

				FileRename fr = null;
				DefaultTableModel model = (DefaultTableModel)jtb.getModel();
				model.setRowCount(0);
				for (int i=0;i<fileRenames.size();i++) {
					fr = fileRenames.get(i);
					model.addRow(new Object[]{fr.getID(), fr.getSourceFilename(), fr.getTargetFilename(), targetModule.getLocale()});
				}
			}
		}
		isInit = false;
	}
	
	public void setTargetModules(ArrayList<TargetModuleMapping> tms) {
		if (tms == null)
			tms = new ArrayList<TargetModuleMapping>();
		this.targetModules = tms;
	}
	
	private boolean isExist(String locale, String module) {
		for (int i=0;i<targetModules.size();i++) {
			if (targetModules.get(i).getFullLocale().equals(locale) && targetModules.get(i).getModule().equals(module))
				return true;
		}
		return false;
	}
	
	public void setSourceModule(String p_sourceModule) {
		this.sourceModule = p_sourceModule;
	}
}
