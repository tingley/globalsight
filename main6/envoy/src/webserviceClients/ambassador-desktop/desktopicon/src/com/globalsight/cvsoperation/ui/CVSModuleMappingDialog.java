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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.globalsight.action.QueryAction;
import com.globalsight.cvsoperation.entity.FileRename;
import com.globalsight.cvsoperation.entity.ModuleMapping;
import com.globalsight.cvsoperation.entity.SourceModuleMapping;
import com.globalsight.cvsoperation.entity.TargetModuleMapping;
import com.globalsight.cvsoperation.util.ModuleMappingHelper;
import com.globalsight.entity.User;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.WebClientHelper;
import com.globalsight.util2.CacheUtil;
import com.globalsight.www.webservices.Ambassador;

public class CVSModuleMappingDialog extends JDialog {

	private static final long serialVersionUID = 1435585600846726737L;

	static Logger log = Logger.getLogger(CVSModuleMappingDialog.class.getName());
	
	private Container contentPane = this;
	
	private JButton addBtn, removeBtn, editBtn, closeBtn;
	
	private JTable jtb; 
	
	private ArrayList<SourceModuleMapping> sourceModules = new ArrayList<SourceModuleMapping>();
	
	private ModuleMappingHelper helper = new ModuleMappingHelper();
	
	private User user = CacheUtil.getInstance().getCurrentUser();
	
	public CVSModuleMappingDialog() {
		initPanel();
		initAction();
	}
	
	public CVSModuleMappingDialog(Frame p_owner, boolean modal)
	{
		super(p_owner);
		this.setResizable(false);
		this.setSize(830, 530);
		Point p = SwingHelper.getMainFrame().getLocation();
		super.setLocation(p.x + 115, p.y + 90);
		this.setModal(modal);
		
		initPanel();
		initAction();
	}
	
	private void initPanel() {
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(2, 2, 2, 2);

		//Add JTable to show locale mapping
		String[] headers = new String[]{"ID", "Source Locale", "Source Module", "Target Locale", "Target Module"};
		//Add data to table
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
		jtb.getColumnModel().getColumn(1).setMinWidth(150);
		jtb.getColumnModel().getColumn(2).setPreferredWidth(300);
		jtb.getColumnModel().getColumn(2).setMinWidth(200);
		jtb.getColumnModel().getColumn(3).setMinWidth(150);
		jtb.getColumnModel().getColumn(4).setPreferredWidth(300);
		jtb.getColumnModel().getColumn(4).setMinWidth(200);
		User u = CacheUtil.getInstance().getCurrentUser();
		setTableData(jtb, helper.getAllModuleMappings(u.getName())); 
		JScrollPane jscp = new JScrollPane(jtb);
		jscp.setPreferredSize(new Dimension(800, 400));
		
		c1.gridx = 0;
		c1.gridy = 0;
		c1.gridwidth = 4;
		c1.gridheight = 1;
		contentPane.add(jscp, c1);
		
		//Add button 'Add Mapping'
		addBtn = new JButton("Add Mapping");
		addBtn.setMinimumSize(new Dimension(160,30));
		addBtn.setMaximumSize(new Dimension(160,30));
		addBtn.setPreferredSize(new Dimension(160,30));
		addBtn.setToolTipText("Add mapping");
		c1.gridx = 0;
		c1.gridy = 1;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		contentPane.add(addBtn, c1);
		
		//Add button 'Remove Mapping'
		removeBtn = new JButton("Remove Mapping");
		removeBtn.setMinimumSize(new Dimension(160,30));
		removeBtn.setMaximumSize(new Dimension(160,30));
		removeBtn.setPreferredSize(new Dimension(160,30));
		removeBtn.setToolTipText("Remove mapping");
		c1.gridx = 1;
		c1.gridy = 1;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		contentPane.add(removeBtn, c1);
		
		//Add button 'Edit Mapping'
		editBtn = new JButton("Edit Mapping");
		editBtn.setMinimumSize(new Dimension(160,30));
		editBtn.setMaximumSize(new Dimension(160,30));
		editBtn.setPreferredSize(new Dimension(160,30));
		editBtn.setToolTipText("Edit mapping");
		c1.gridx = 2;
		c1.gridy = 1;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		contentPane.add(editBtn, c1);

		//Add button 'Close'
		closeBtn = new JButton("Close");
		closeBtn.setMinimumSize(new Dimension(160,30));
		closeBtn.setMaximumSize(new Dimension(160,30));
		closeBtn.setPreferredSize(new Dimension(160,30));
		closeBtn.setToolTipText("Close");
		c1.gridx = 3;
		c1.gridy = 1;
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.anchor = GridBagConstraints.EAST;
		contentPane.add(closeBtn, c1);
	}
	
	private void initAction() {
		//Add button action
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CVSMappingDialog addDialog = new CVSMappingDialog(SwingHelper.getMainFrame(), true);
				addDialog.setTitle("Add Module Mapping");
				addDialog.setSourceModule(null);
				addDialog.setVisible(true);
				setTableData(jtb, helper.getAllModuleMappings(user.getName()));
			}
		});
		
		//Remove button action
		removeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jtb.getSelectedRowCount() > 0) {
					if (AmbOptionPane.showConfirmDialog("Are you sure need to REMOVE this record?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						DefaultTableModel dtm = (DefaultTableModel)jtb.getModel();
						int[] rows = jtb.getSelectedRows();
						for (int i=rows.length-1;i>=0;i--) {
							String id = (String)dtm.getValueAt(rows[i], 0);
							helper.delete(id);
						}
						setTableData(jtb, helper.getAllModuleMappings(user.getName()));
					}
				} else {
					AmbOptionPane.showMessageDialog("Please select at least one record first.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		//Edit button action
		editBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jtb.getSelectedRowCount() > 0) {
					DefaultTableModel model = (DefaultTableModel)jtb.getModel();
					String id = (String)model.getValueAt(jtb.getSelectedRow(), 0);
					CVSMappingDialog dialog = new CVSMappingDialog(SwingHelper.getMainFrame(), true);
					dialog.setTitle("Edit Module Mapping ");
					dialog.setAdd(false);
					dialog.setId(id);
					dialog.setVisible(true);
					setTableData(jtb, helper.getAllModuleMappings(user.getName()));
				} else {
					AmbOptionPane.showMessageDialog("Please select at least one record first.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}); 
		
		//Close button action
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CVSModuleMappingDialog.this.dispose();
			}
		});

		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				ModuleMappingHelper helper = new ModuleMappingHelper();
				helper.closeDBConnection();
			}
		});
	}
	
	private int getFirstModule(SourceModuleMapping s) {
		SourceModuleMapping t = null;
		for (int i=0;i<sourceModules.size();i++) {
			t = sourceModules.get(i);
			if (t.getModule().equals(s.getModule()) && t.getLocale().equals(s.getLocale())) {
				return i;
			}
		}
		return 0;
	}
	
	private void setTableData(JTable jt, ArrayList<ModuleMapping> mappings) {
		DefaultTableModel dtm = (DefaultTableModel)jt.getModel();
		dtm.setRowCount(0);
		if (mappings == null || mappings.size() == 0)
			return;
		ModuleMapping mm = null;
		for (int i=0;i<mappings.size();i++) {
			mm = mappings.get(i);
			dtm.addRow(new Object[]{mm.getId(), mm.getSourceLocale(), mm.getSourceModule(), mm.getTargetLocale(), mm.getTargetModule()});
		}
		jt.repaint();
	}
}
