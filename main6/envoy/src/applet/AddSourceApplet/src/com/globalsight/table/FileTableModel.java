/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.table;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.globalsight.util.SortUtil;

public class FileTableModel extends AbstractTableModel
{
    private Map<String, String> resource;
    private int directNumber = 0;

    private static final long serialVersionUID = -6925503107760192229L;
    private String[] columnNames =
    { "File", "File Profile" };

    private List<RowVo> rowVos = new ArrayList<RowVo>();

    @Override
    public String getColumnName(int col)
    {
        return getColumnNames()[col];
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
        return col == 1;
    }

    @Override
    public int getColumnCount()
    {
        return getColumnNames().length;
    }

    @Override
    public int getRowCount()
    {
        return rowVos.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        RowVo row = rowVos.get(rowIndex);

        if (columnIndex == 0)
        {
            File f = row.getFile();
            String path = f.getPath();
            path = path.replace("\\", "/");

            String[] folders = path.split("/");
            int index = folders.length - 1 - directNumber;
            if (index < 0)
            {
                index = 0;
            }

            if (directNumber != -1)
            {
                StringBuffer displayPath = new StringBuffer();
                for (int i = index; i < folders.length - 1; i++)
                {
                    displayPath.append(folders[i]).append("/");
                }
                displayPath.append(folders[folders.length - 1]);
                return displayPath.toString();
            }

            return path;
        }

        return row;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 1)
            return File.class;

        // if (columnIndex == 2)
        // return List.class;

        return String.class;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex)
    {
        super.setValueAt(value, rowIndex, columnIndex);
        RowVo ob = rowVos.get(rowIndex);

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public List<RowVo> getRows()
    {
        return rowVos;
    }

    public void setRows(List<RowVo> rows)
    {
        this.rowVos = rows;
    }

    @Override
    public void fireTableDataChanged()
    {
        SortUtil.sort(rowVos, new Comparator<RowVo>()
        {
            @Override
            public int compare(RowVo o1, RowVo o2)
            {
                File f1 = o1.getFile();
                File f2 = o2.getFile();
                String n1 = f1.getName();
                String n2 = f2.getName();

                int i1 = n1.lastIndexOf(".");
                int i2 = n2.lastIndexOf(".");

                if (i1 < 0)
                    return -1;

                if (i2 < 0)
                    return 1;

                String t1 = n1.substring(i1);
                String t2 = n2.substring(i2);

                int type = t1.compareTo(t2);
                if (type != 0)
                    return type;

                return n1.compareTo(n2);
            }
        });

        super.fireTableDataChanged();
    }

    public Map<String, String> getResource()
    {
        return resource;
    }

    public void setResource(Map<String, String> resource)
    {
        this.resource = resource;
    }

    public String[] getColumnNames()
    {
        if (resource != null)
        {
            String file = resource.get("lb_file");
            if (file != null)
            {
                columnNames[0] = file;
            }

            String fileProfile = resource.get("lb_file_profile");
            if (fileProfile != null)
            {
                columnNames[1] = fileProfile;
            }

            resource = null;
        }
        return columnNames;
    }

    public void setDirectNumber(int directNumber)
    {
        this.directNumber = directNumber;
    }
}
