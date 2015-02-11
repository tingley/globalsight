package com.globalsight.ui.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.globalsight.action.Action;
import com.globalsight.action.GetAttributeAction;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.ui.attribute.vo.Attributes;
import com.globalsight.ui.attribute.vo.JobAttributeVo;
import com.globalsight.util.Constants;
import com.globalsight.util.XmlUtil;

public class AttributeTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = -6925503107760192229L;
    private String[] columnNames = {"Name", "Type", "Required","Value"};
    private List<JobAttributeVo> attributes = new ArrayList<JobAttributeVo>();
    private long projectId;
    
    private static Map<String, String> TYPES = new HashMap<String, String>();
    static
    {
        TYPES.put("text", "Text");
        TYPES.put("integer", "Integer");
        TYPES.put("float", "Float");
        TYPES.put("choiceList", "Choice List");
        TYPES.put("date", "Date");
        TYPES.put("file", "File");
    }
    
    public AttributeTableModel()
    {
//        Attributes a = XmlUtil.load(Attributes.class, "a.xml");
//        attributes = (List<JobAttributeVo>)a.getAttributes();
    }
   

    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    public boolean isCellEditable(int row, int col) {
        return col == 3;
    }
    
    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return attributes.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        JobAttributeVo attribute = attributes.get(rowIndex);

        if (columnIndex == 0)
        {
            return new ColumnInfo(attribute.getDisplayName(), attribute.isFromSuperCompany());
        }
            
        
        if (columnIndex == 1)
        {
            String key = attribute.getType();
            if (TYPES.containsKey(key))
            {
                key = TYPES.get(key);
            }
            
            return new ColumnInfo(key, attribute.isFromSuperCompany());
        }
        
        if (columnIndex == 2)
            return attribute.isRequired();
        
        return attribute;
    }
    
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 2)
            return Boolean.class;
        
        if (columnIndex == 3)
            return JobAttributeVo.class;
        
        return ColumnInfo.class;
    }

//    public void setValueAt(Object value, int rowIndex, int columnIndex)
//    {
//        super.setValueAt(value, rowIndex, columnIndex);
////        Object ob = attributes.get(rowIndex);
////        if (ob instanceof ListAttribute)
////        {
////            ListAttribute listAttribute = (ListAttribute) ob;
////            System.out.println(value);
////        }
////        else if (ob instanceof IntegerAttribute)
////        {
////            System.out.println(value);
////        }
//        
//        fireTableCellUpdated(rowIndex, columnIndex);
//    }


    public List<JobAttributeVo> getAttributes()
    {
        return attributes;
    }


    public void setAttributes(List<JobAttributeVo> attributes)
    {
        this.attributes = attributes;
    }
    
    public boolean isRequiredAttributeSeted()
    {
        for (JobAttributeVo vo : attributes)
        {
            if (vo.isRequired() && !vo.isSetted())
                return false;
        }
        
        return true;
    }

    public void setProjectId(long projectId)
    {
        if (projectId == -1)
        {
            this.projectId = -1;
            attributes.clear();
            return;
        }
        
        if (this.projectId != projectId)
        {
            this.projectId = projectId;
            try
            {
                String xml = GetAttributeAction.getAttributesByProjectId(projectId);
                Attributes a = XmlUtil.string2Object(Attributes.class, xml);
                attributes = (List<JobAttributeVo>)a.getAttributes();
                fireTableDataChanged();
            }
            catch (Exception e)
            {
                AmbOptionPane.showMessageDialog(Constants.MSG_FAIL_GET_ATTRIBUTE,
                        "Warning", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public boolean hasAttribute()
    {
        return attributes != null && attributes.size() > 0;
    }
}