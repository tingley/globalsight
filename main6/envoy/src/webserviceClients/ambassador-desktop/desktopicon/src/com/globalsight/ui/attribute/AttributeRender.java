package com.globalsight.ui.attribute;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.globalsight.ui.attribute.vo.DateJobAttributeVo;
import com.globalsight.ui.attribute.vo.FileJobAttributeVo;
import com.globalsight.ui.attribute.vo.FloatJobAttributeVo;
import com.globalsight.ui.attribute.vo.IntJobAttributeVo;
import com.globalsight.ui.attribute.vo.ListJobAttributeVo;
import com.globalsight.ui.attribute.vo.TextJobAttributeVo;

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
            if (object instanceof ListJobAttributeVo)
            {
                ListJobAttributeVo attribute = (ListJobAttributeVo) object;
                List<String> options = attribute.getSelectedOptions();
                this.setText(attribute.getLabel());
            }
            else if (object instanceof DateJobAttributeVo)
            {
                DateJobAttributeVo attribute = (DateJobAttributeVo) object;
                table.setRowHeight(row, ROW_HEIGHT);
                this.setText(attribute.getLabel());
            }
            else if (object instanceof IntJobAttributeVo)
            {
                IntJobAttributeVo attribute = (IntJobAttributeVo) object;
                table.setRowHeight(row, ROW_HEIGHT);
                this.setText(attribute.getLabel());
            }
            else if (object instanceof FloatJobAttributeVo)
            {
                FloatJobAttributeVo attribute = (FloatJobAttributeVo) object;
                table.setRowHeight(row, ROW_HEIGHT);
                this.setText(attribute.getLabel());
            }
            else if (object instanceof FileJobAttributeVo)
            {
                FileJobAttributeVo attribute = (FileJobAttributeVo) object;
                this.setText(attribute.getLabel());
            }
            else if (object instanceof TextJobAttributeVo)
            {
                TextJobAttributeVo attribute = (TextJobAttributeVo) object;
                table.setRowHeight(row, ROW_HEIGHT);
                this.setText(attribute.getLabel());
            }
        }
        else
        {
            table.setRowHeight(row, ROW_HEIGHT);
        }

        return this;
    }
}
