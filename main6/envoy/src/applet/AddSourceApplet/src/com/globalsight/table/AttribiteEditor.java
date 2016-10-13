package com.globalsight.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import com.globalsight.vo.FileProfileVo;

public class AttribiteEditor extends AbstractCellEditor implements
        TableCellEditor, ActionListener
{
    private static final long serialVersionUID = 3183874486060373420L;
    private JList jList;
    private Font font;
    private JPopupMenu menu;
    private RowVo vo;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column)
    {
        font = table.getFont();
        return getListEditor((RowVo) value);
    }

    private JComponent getListEditor(RowVo rowVo)
    {
        vo = rowVo;
        DefaultListModel model = new DefaultListModel();

        List<FileProfileVo> selectedOptions = rowVo.getFileProfiles();
        for (FileProfileVo option : selectedOptions)
        {
            model.addElement(option);
        }

        jList = new JList(model);
        jList.setFont(font);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setSelectedIndex(rowVo.getSelectIndex());

        jList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                vo.setSelectIndex(jList.getSelectedIndex());
                menu.setVisible(false);
                stopCellEditing();
            }
        });
        jList.setVisibleRowCount(selectedOptions.size());

        JLabel label = new JLabel();
        label.setFont(font);
        label.setText(rowVo.getSelectFileProfile());
        label.addMouseListener(new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {

            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                JLabel label = (JLabel) e.getSource();
                menu = new JPopupMenu();
                JScrollPane pane = new JScrollPane();
                pane.setViewportView(jList);
                pane.setBorder(null);
                if (jList.getModel().getSize() > 4)
                {
                    menu.setPreferredSize(new Dimension(label.getSize().width, 80));
                }
                else
                {
                    jList.setFixedCellWidth(label.getSize().width);
                }
                
                menu.setLayout(new BorderLayout());
                menu.add(pane, BorderLayout.CENTER);
                menu.show(label, 0, 19);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                stopCellEditing();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
            }
        });

        return label;
    }

    @Override
    public Object getCellEditorValue()
    {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
    }
}
