package com.globalsight.ui.attribute;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.globalsight.ui.attribute.vo.JobAttributeVo;

public class JobAttributeDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private AttributeTableModel model = null;
    private JTable jTable = null;
    private JScrollPane jScrollPane = null;
    private JPanel jPanel = null;

    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.height - getHeight()) / 2,
                (screen.width - getWidth()) / 2);
        setLocation(location);
    }

    /**
     * This method initializes jTable
     * 
     * @return javax.swing.JTable
     */
    private JTable getJTable()
    {
        if (jTable == null)
        {
            jTable = new JTable();
            model = new AttributeTableModel();
            jTable.setModel(model);
            jTable.setCellSelectionEnabled(true);
            jTable.setRowHeight(21);
            jTable
                    .setDefaultEditor(JobAttributeVo.class,
                            new AttribiteEditor());
            jTable.setDefaultRenderer(JobAttributeVo.class,
                    new AttributeRender());
            jTable.setDefaultRenderer(ColumnInfo.class,
                    new ColumnInfoRender());

            TableColumnModel columnModel = jTable.getColumnModel();

            TableColumn column = null;
            int size = columnModel.getColumnCount();
            for (int i = 0; i < size; i++)
            {
                column = columnModel.getColumn(i);
                if (i == size - 1)
                {
                    column.setPreferredWidth(150); // third column is bigger
                }
                else
                {
                    column.setPreferredWidth(50);
                }
            }

        }
        return jTable;
    }
    
    /**
     * Stops cell editing and set visible to true.  
     */
    public void display()
    {
    	TableCellEditor editor = getJTable().getCellEditor();
    	if (editor != null)
    	{
    		editor.stopCellEditing();
    	}
    	
    	this.setVisible(true);
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane()
    {
        if (jScrollPane == null)
        {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel()
    {
        if (jPanel == null)
        {
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setSize(new Dimension(253, 80));
            jPanel.setPreferredSize(new Dimension(51, 40));
            jPanel.setBorder(null);
            
            JButton button = new JButton("OK");
            button.setSize(180, 22);
            button.setPreferredSize(new Dimension(80, 28));
            button.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e)
                {
                    setVisible(false);
                    
                }});
            jPanel.add(button);
            
        }
        return jPanel;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param owner
     */
    public JobAttributeDialog(Frame owner)
    {
        super(owner);
        this.setModal(true);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setTitle("Set Job Attributes");
        this.setMinimumSize(new Dimension(350,240));
        this.setSize(690, 442);
        this.setContentPane(getJContentPane());
        setLocation();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.setPreferredSize(new Dimension(500, 400));
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
            jContentPane.add(getJPanel(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }
    
    public boolean isRequiredAttributeSeted()
    {
        return model.isRequiredAttributeSeted();
    }

    public void setProjectId(long projectId)
    {
        model.setProjectId(projectId);
    }

    public AttributeTableModel getModel()
    {
        return model;
    }
    
    public void clean()
    {
        setProjectId(-1);
    }
    
    public boolean hasAttribute()
    {
        return model.hasAttribute();
    }
} // @jve:decl-index=0:visual-constraint="46,9"
