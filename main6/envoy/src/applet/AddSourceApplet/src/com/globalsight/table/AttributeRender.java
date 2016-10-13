package com.globalsight.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class AttributeRender extends JLabel implements TableCellRenderer
{
    private static final long serialVersionUID = -8848515479689762207L;
    public final static int ROW_HEIGHT = 21;
    
    public Component getTableCellRendererComponent(JTable table, Object object,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        this.setFont(table.getFont());
        
        if (object != null)
        {
            RowVo rowVo = (RowVo)object;
            String s = rowVo.getSelectFileProfile();
            this.setText(s);
            if ("".equals(s))
            {
                setOpaque(true);
                setBackground(Color.yellow);
            }
            else
            {
                setOpaque(false);
            }
        }

        return this;
    }
}
