package com.globalsight.ui.attribute;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ColumnInfoRender implements TableCellRenderer
{
    private static final long serialVersionUID = 3205551879088228514L;


    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        JLabel label = new JLabel();
        label.setFont(table.getFont());
        if (value == null)
        {
            label.setText("");
        }
        ColumnInfo info = (ColumnInfo) value;
        if (info.isFromSuprerCompany())
        {
            label.setForeground(new Color(255, 102, 0));
        }
        label.setText(info.getValue());
        
        return label;
    }
}
