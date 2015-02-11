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


package com.globalsight.diplomat.servlet.config;

public class DJ_ColumnProfile
{
    private int m_columnNumber = 0;
    private String m_tableName = "";
    private long m_dataType = 0;
    private long m_rule = 0;
    private int m_contentMode;
    private String m_label;
    
    public DJ_ColumnProfile (int p_columnNumber, String p_tableName, long p_dataType, 
        long p_rule, int p_contentMode, String p_label)
    {
        m_columnNumber = p_columnNumber;
        m_tableName = p_tableName;
        m_dataType = p_dataType;
        m_rule = p_rule;
        m_contentMode = p_contentMode;
        m_label = p_label;
    }
    
    public int getColumnNumber() { return m_columnNumber; }
    public String getTableName() { return m_tableName; }
    public long getDataType() { return m_dataType; }
    public long getRule() { return m_rule; }
    public int getContentMode() { return m_contentMode; }
    public String getLabel() { return m_label; }
    
    public void setColumnNumber(int p_columnNumber) { m_columnNumber = p_columnNumber; }
    public void setTableName(String p_tableName) { m_tableName = p_tableName; }
    public void setDataType(long p_dataType) { m_dataType = p_dataType; }
    public void setRule(long p_rule) { m_rule = p_rule; }
    public void setContentMode(int p_contentMode) { m_contentMode = p_contentMode; }
    public void setLabel(String p_label) { m_label = p_label; }
}