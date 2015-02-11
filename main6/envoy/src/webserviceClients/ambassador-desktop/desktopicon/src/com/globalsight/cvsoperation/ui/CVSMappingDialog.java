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
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.dom4j.*;

import com.globalsight.action.QueryAction;
import com.globalsight.cvsoperation.entity.ModuleMapping;
import com.globalsight.cvsoperation.entity.SourceModuleMapping;
import com.globalsight.cvsoperation.entity.TargetModuleMapping;
import com.globalsight.cvsoperation.util.ModuleMappingHelper;
import com.globalsight.cvsoperation.util.XMLParser;
import com.globalsight.entity.User;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.StringUtil;
import com.globalsight.util.SwingHelper;
import com.globalsight.util2.CacheUtil;

public class CVSMappingDialog extends JDialog {

	private static final long serialVersionUID = -3490984219094565781L;
	
	static Logger log = Logger.getLogger(CVSMappingDialog.class.getName());
	
	private JLabel jl1, jl2, jl3;
	
	private JTextField jtf1;
	
	private JComboBox jcb1;
	
	private JTable jtb;
	
	private JButton selectBtn, addBtn, editBtn, removeBtn, saveBtn, cancelBtn;
	
	private Container content = this;
	
	private boolean isAdd = true, isOKReturn = false;
	
	private QueryAction queryAction = new QueryAction();
	
	private String[] keys, values;
	
	private ModuleMappingHelper helper = new ModuleMappingHelper();
	
	private String id = "";
	
	private SourceModuleMapping sourceModule = new SourceModuleMapping();

	private ArrayList<TargetModuleMapping> targetModules = new ArrayList<TargetModuleMapping>();
	
	public CVSMappingDialog(Frame p_owner, boolean modal)
	{
		super(p_owner);
		this.setResizable(false);
		this.setSize(700, 500);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		init();
		initAction();
		initData();
	}

	private void init() {
		content.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(2, 2, 2, 2);
		
		//Source module label
		jl1 = new JLabel("Source Module:");
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.anchor = GridBagConstraints.WEST;
		content.add(jl1, c1);
		
		jtf1 = new JTextField(30);
		jtf1.setEditable(false);
		c1.gridx = 1;
		c1.gridy = 0;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;
		content.add(jtf1, c1);
		
		//"Select Source Module" button
		selectBtn = new JButton("Select Source Module...");
		selectBtn.setPreferredSize(new Dimension(170,30));
		selectBtn.setToolTipText("Select source module");
		c1.gridx = 2;
		c1.gridy = 0;
		c1.gridwidth = 1;
		content.add(selectBtn, c1);

		//Associated source locale
		jl2 = new JLabel("Associated Source Locale: ");
		c1.gridx = 0;
		c1.gridy = 1;
		c1.gridwidth = 3;
		c1.gridheight = 1;
		content.add(jl2, c1);
		
		jcb1 = new JComboBox();
		jcb1.setPreferredSize(new Dimension(20, 20));
		jcb1.setMaximumSize(new Dimension(20, 20));
		c1.gridx = 1;
		c1.gridy = 2;
		c1.gridwidth = 1;
		content.add(jcb1, c1);
		
		//Target Locales
		jl3 = new JLabel("Target Locale(s): ");
		c1.gridx = 0;
		c1.gridy = 3;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.anchor = GridBagConstraints.NORTHWEST;
		content.add(jl3, c1);
		
		String[] headers = new String[]{"", "Module", "Target Locale"};
		String[][] tableData = null;
		DefaultTableModel model = new DefaultTableModel(tableData, headers) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		jtb = new JTable(model);
		jtb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jtb.getColumnModel().getColumn(0).setMinWidth(0);
		jtb.getColumnModel().getColumn(0).setMaxWidth(0);
		jtb.getColumnModel().getColumn(1).setMinWidth(250);
		jtb.getColumnModel().getColumn(2).setMinWidth(150);

		JScrollPane jscp = new JScrollPane(jtb);
		jscp.setPreferredSize(new Dimension(400, 300));
		c1.gridx = 1;
		c1.gridy = 3;
		c1.gridwidth = 1;
		content.add(jscp, c1);
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(2, 2, 2, 2);
		addBtn = new JButton("Add Target Locale...");
		addBtn.setPreferredSize(new Dimension(170,30));
		addBtn.setToolTipText("Add target locale");
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		buttonPanel.add(addBtn, c2);
		
		editBtn = new JButton("Edit Target Locale...");
		editBtn.setPreferredSize(new Dimension(170,30));
		editBtn.setToolTipText("Edit target locale");
		c2.gridx = 0;
		c2.gridy = 1;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		buttonPanel.add(editBtn, c2);
		
		removeBtn = new JButton("Remove Target Locale...");
		removeBtn.setPreferredSize(new Dimension(170,30));
		removeBtn.setToolTipText("Remove target locale");
		c2.gridx = 0;
		c2.gridy = 2;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		buttonPanel.add(removeBtn, c2);

		c1.gridx = 2;
		c1.gridy = 3;
		c1.anchor = GridBagConstraints.NORTH;
		content.add(buttonPanel, c1);

		buttonPanel = new JPanel(new GridBagLayout());
		c2 = new GridBagConstraints();
		c2.insets = new Insets(2, 2, 2, 2);
		
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
		c1.gridy = 4;
		content.add(buttonPanel, c1);
	}
	
	private void initAction() {
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcb1.getSelectedIndex() == -1) {
					AmbOptionPane.showMessageDialog("Please select one vaild source locale before this operation. ", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				CVSTargetModuleDialog dialog = new CVSTargetModuleDialog(SwingHelper.getMainFrame(), true);
				dialog.setTitle("Add Target Module");
				dialog.setAdd(true);
				int index = jcb1.getSelectedIndex();
				dialog.setTargetModules(targetModules);
				dialog.setSourceLocale(values[index]);
				dialog.setSourceModule(jtf1.getText());
				dialog.setVisible(true);
				if (dialog.isOKReturn()) {
					TargetModuleMapping targetModule = dialog.getTargetModule();
					if (targetModule.getLocale() != null && !targetModule.getLocale().equals("")) {
						DefaultTableModel tableModel = (DefaultTableModel) jtb.getModel();
						tableModel.addRow(new Object[]{String.valueOf(tableModel.getRowCount()+1), targetModule.getModule(), targetModule.getLocale()});
					}
					targetModules.add(targetModule);
				}
			}
		});
		editBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jtb.getSelectedRow() == -1) {
					AmbOptionPane.showMessageDialog("Please select one record first", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				CVSTargetModuleDialog dialog = new CVSTargetModuleDialog(SwingHelper.getMainFrame(), true);
				dialog.setTitle("Edit Target Module");
				dialog.setAdd(false);
				int index = jcb1.getSelectedIndex();
				int rowIndex = jtb.getSelectedRow();
				dialog.setTargetModules(targetModules);
				dialog.setSourceLocale(values[index]);
				dialog.setSourceModule(jtf1.getText());
				dialog.setTargetModule(targetModules.get(rowIndex));
				dialog.setVisible(true);
				if (dialog.isOKReturn()) {
					TargetModuleMapping targetModule = dialog.getTargetModule();
					if (targetModule.getLocale() != null && !targetModule.getLocale().equals("")) {
						DefaultTableModel tableModel = (DefaultTableModel) jtb.getModel();
						tableModel.setValueAt(targetModule.getID(), rowIndex, 0);
						tableModel.setValueAt(targetModule.getModule(), rowIndex, 1);
						tableModel.setValueAt(targetModule.getLocale(), rowIndex, 2);
					}
					targetModules.set(rowIndex, targetModule);
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
				if (AmbOptionPane.showConfirmDialog("Are you sure to delete current record?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					int[] rows = jtb.getSelectedRows();
					DefaultTableModel tableModel = (DefaultTableModel) jtb.getModel();
					for (int i=rows.length-1;i>=0;i--) {
						tableModel.removeRow(rows[i]);
						targetModules.remove(rows[i]);
					}
				}
			}
		});
		selectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CVSTreeDialog treeDialog = new CVSTreeDialog(SwingHelper.getMainFrame(), true);
				treeDialog.setTitle("Select Source Module");
				treeDialog.setVisible(true);
				if (treeDialog.isOKReturn()) {
					jtf1.setText(treeDialog.getCVSPath());
				}
			} 
		});
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (StringUtil.isEmpty(jtf1.getText())) {
					AmbOptionPane.showMessageDialog("Please select 'Source Module' field", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (jcb1.getSelectedIndex() == -1) {
					AmbOptionPane.showMessageDialog("Please select 'Source Locale' field", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (jtb.getRowCount() == 0) {
					AmbOptionPane.showMessageDialog("Please add target module and locale", 
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				isOKReturn = true;
				if (sourceModule == null) {
					sourceModule = new SourceModuleMapping();
				}
				sourceModule.setID(id);
				sourceModule.setModule(jtf1.getText());
				sourceModule.setLocale(getLocale((String)jcb1.getSelectedItem()));
				sourceModule.setFullLocale((String)jcb1.getSelectedItem());
				sourceModule.setTargetModules(targetModules);
				
				if (isAdd())
					helper.add(sourceModule);
				else
					helper.update(sourceModule);

				CVSMappingDialog.this.dispose();
			}
		});
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AmbOptionPane.showConfirmDialog("Are you sure to cancel the operation and exit?", "Info", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					CVSMappingDialog.this.dispose();
				}
			}
		});
		
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				exit();
			}
		});

	}

	private void exit() {
		if (!"".equals(jtf1.getText()) || jtb.getModel().getRowCount()>0) {
			if (AmbOptionPane.showConfirmDialog("Are you sure to cancel the operation and exit?", "Info", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CVSMappingDialog.this.dispose();
			}
		}
	}
	
	private void initData() {
		//JCombobox
		String xmlReturn;
		ArrayList locales = null;
		try {
			xmlReturn = queryAction.execute(new String[]{ QueryAction.q_getSourceLocales });
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
		} catch (Exception e) {
		}
		for (int i=0;i<values.length;i++)
			jcb1.addItem(values[i]);
	}
	
	private void setTableData(JTable jt, ArrayList<TargetModuleMapping> mappings) {
		if (mappings == null || mappings.size() == 0)
			return;
		DefaultTableModel dtm = (DefaultTableModel)jt.getModel();
		dtm.setRowCount(0);
		TargetModuleMapping tmm = null;
		for (int i=0;i<mappings.size();i++) {
			tmm = mappings.get(i);
			dtm.addRow(new Object[]{tmm.getID(), tmm.getModule(), tmm.getLocale()});
		}
		jt.repaint();
	}
	
	public boolean isAdd() {
		return isAdd;
	}

	public void setAdd(boolean isAdd) {
		this.isAdd = isAdd;
	}

	public boolean isOKReturn() {
		return isOKReturn;
	}

	public void setOKReturn(boolean isOKReturn) {
		this.isOKReturn = isOKReturn;
	}

	public SourceModuleMapping getSourceModule() {
		return sourceModule;
	}

	public void setSourceModule(SourceModuleMapping sourceModule) {
		this.sourceModule = sourceModule;
		if (sourceModule != null) {
			jtf1.setText(sourceModule.getModule());
			jcb1.setSelectedItem(sourceModule.getFullLocale());
			targetModules = sourceModule.getTargetModules();
			if (targetModules != null && targetModules.size()>0) {
				DefaultTableModel model = (DefaultTableModel)jtb.getModel();
				TargetModuleMapping tm = null;
				for (int i=0;i<targetModules.size();i++) {
					tm = targetModules.get(i);
					model.addRow(new Object[]{tm.getID(), tm.getModule(), tm.getLocale()});
				}
			}
		}
	}
	private String getLocale(String p) {
		if (p!=null)
			return p.substring(p.lastIndexOf("[")+1, p.lastIndexOf("]"));
		else
			return "";
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
		//Edit
		if (!isAdd()) {
			ModuleMapping mm = helper.getModuleMapping(id);
			jtf1.setText(mm.getSourceModule());
			jcb1.setSelectedItem(mm.getFullSourceLocale());
			
			User u = CacheUtil.getInstance().getCurrentUser();
			sourceModule = helper.getSourceModuleMapping(mm.getSourceLocale(), mm.getSourceModule(), u.getName());
			ArrayList<TargetModuleMapping> targets = sourceModule.getTargetModules();
			if (targets != null && targets.size()>0) {
				setTableData(jtb, targets);
			}
			
			jcb1.setEnabled(false);
			targetModules = sourceModule.getTargetModules();
		}

	}
}
